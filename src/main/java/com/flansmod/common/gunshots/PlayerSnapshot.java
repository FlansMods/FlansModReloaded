package com.flansmod.common.gunshots;

import com.flansmod.common.FlansMod;
import com.flansmod.util.Maths;
import com.flansmod.util.Transform;
import com.flansmod.util.TransformStack;
import com.mojang.math.Transformation;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

public class PlayerSnapshot
{
    public static final PlayerSnapshot INVALID = new PlayerSnapshot();

    public boolean valid;
    public Player player;
    public Vec3 rootPos;
    //public ArrayList<PlayerHitbox> hitboxes;
    public PlayerHitbox[] hitboxes;

    public long time;

    public PlayerSnapshot()
    {
        valid = false;
        hitboxes = new PlayerHitbox[EPlayerHitArea.NUM_AREAS];
        rootPos = Vec3.ZERO;
        for(int i = 0; i < EPlayerHitArea.NUM_AREAS; i++)
        {
            hitboxes[i] = new PlayerHitbox(EPlayerHitArea.values()[i], Transform.IDENTITY, Maths.IdentityPosF());
        }
    }

    public void SnapPlayer(Player p)
    {
        valid = true;
        player = p;

        TransformStack transformStack = new TransformStack();
        transformStack.add(Transform.FromPos("\"Pos\"", p.getPosition(0.0f)));

        // This block of code is good fun. We need to get the CLIENT ONLY poses onto the server
        // float attackTime = getAttackAnim()
        boolean shouldSit = player.isPassenger() && player.getVehicle() != null && player.getVehicle().shouldRiderSit();
        float yBody = player.yBodyRot;
        float yHead = player.yHeadRot;
        float yNeck = yHead - yBody;

        if(shouldSit && player.getVehicle() instanceof LivingEntity livingVehicle)
        {
            yBody = livingVehicle.yBodyRot;
            yNeck = yHead - yBody;
            yNeck = Maths.ClampDegrees(yNeck);
            yNeck = Maths.Clamp(yNeck, -85f, 85f);

            yBody = yHead - yNeck;
            if(yNeck * yNeck > 2500.0f)
                yBody += yNeck * 0.2f;

            yNeck = yHead - yBody;
        }

        float xHead = player.getXRot();
        // Skip: Dinnerbone flip code

        if(player.hasPose(Pose.SLEEPING))
        {
            Direction bedDir = player.getBedOrientation();
            if(bedDir != null)
            {
                float eyeHeight = player.getEyeHeight(Pose.STANDING) - 0.1f;
                transformStack.add(Transform.FromPos("\"SleepPose\"", new Vec3(-bedDir.getStepX() * eyeHeight, 0.0d, -bedDir.getStepZ() * eyeHeight)));
            }
        }

        float bob = player.tickCount; // + dT
        // -------------- Setup Rotations -------------
        // player, pose, bob, yBody, dT
        {
            if (player.isFullyFrozen())
                yBody += Maths.CosF(player.tickCount * 3.25f) * Maths.PiF * 0.4f;

            if (!player.hasPose(Pose.SLEEPING))
                transformStack.add(Transform.FromEuler("\"PlayerYaw\"", 0f, 180f + yBody, 0f));

            if (player.deathTime > 0)
            {
                float deathTime = (player.deathTime - 1.0f) / 20.0f * 1.6f; // deathTime + dT
                deathTime = Maths.SqrtF(deathTime);
                deathTime = Maths.Clamp(deathTime, Float.MIN_VALUE, 1.0f);

                transformStack.add(Transform.FromEuler("\"DeathAnim\"", 0f, 0f, deathTime * 90.0f));
            }
            else if (player.isAutoSpinAttack())
            {
                transformStack.add(Transform.FromEuler("\"SpinAttack\"", -90.0f - player.getXRot(), player.tickCount * -75.0f, 0f));
            }
            else if (player.hasPose(Pose.SLEEPING))
            {
                Direction bedDir = player.getBedOrientation();
                float bedAngle = bedDir != null ? (90.0f - bedDir.toYRot()) : yBody;
                transformStack.add(Transform.FromEuler("\"Sleeping\"", 0f, bedAngle, 0f));
                transformStack.add(Transform.FromEuler("\"Sleeping\"", 0f, 270.0f, 90.0f));
            }
        }
        // else dinnerbone, nty

        // Scale(-1, -1, 1)
        // this.Scale(?)
        transformStack.add(Transform.FromPos("\"UnapplyEyeLine\"", new Vec3(0f, 0f, 0f)));
        float anim8 = 0f, anim5 = 0f;
        if(!shouldSit && player.isAlive())
        {
            anim8 = player.walkAnimation.speed();
            anim5 = player.walkAnimation.position();
            anim8 = Maths.Clamp(anim8, Float.MIN_VALUE, 1.0f);
        }
        // this.prepareMobModel (player, anim5, anim8, dT)
        // this.setupAnim (player, anim5, anim8, bob, yNeck, xHead)
        // render

        Vector3f bodyHalfSize = new Vector3f(0.25f, 0.35f, 0.15f);
        Vector3f headHalfSize = new Vector3f(0.25f, 0.25f, 0.25f);
        Vector3f armHalfSize = new Vector3f(0.15f, 0.25f, 0.15f);

        switch(player.getPose())
        {
            default -> {
                transformStack.PushSaveState();
                {
                    // Body

                    transformStack.add(Transform.FromPos("\"BodyCenter\"", 0d, 1.05d, 0d));
                    UpdateHitbox(EPlayerHitArea.BODY, transformStack.Top(), bodyHalfSize);

                    // Head
                    transformStack.add(Transform.FromPos("\"NeckOffset\"", 0d, 0.35d, 0d)); // Then add the neck pivot point
                    transformStack.add(Transform.FromEuler("\"NeckRot\"", p.xRotO, yNeck, 0.0f)); // Rotate around the neck
                    transformStack.add(Transform.FromPos("\"HeadHalfHeight\"", 0d, 0.25d, 0d)); // Add half a head height
                    UpdateHitbox(EPlayerHitArea.HEAD, transformStack.Top(), headHalfSize);
                }
                transformStack.PopSaveState();

                // LLeg
                transformStack.add(Transform.FromPos("\"LeftArmPose\"", 0d, 1.05d, 0d));
                UpdateHitbox(EPlayerHitArea.LEFTARM, transformStack.Top(), armHalfSize);
            }
            case CROUCHING -> {
                // Body
                transformStack.add(Transform.FromPos("\"CrouchedBody\"", 0d, 0.8d, 0.15d));
                transformStack.add(Transform.FromEuler("\"CrouchedPose\"", 0.5f * Maths.RadToDegF, 0f, 0f));
                UpdateHitbox(EPlayerHitArea.BODY, transformStack.Top(), bodyHalfSize);

                // Head
                transformStack.add(Transform.FromPos("\"NeckOffset\"", 0d, 0.35d, 0d)); // Then add the neck pivot point
                transformStack.add(Transform.FromEuler("\"ReturnToAngle0\"", -0.5f * Maths.RadToDegF, 0f, 0f));
                transformStack.add(Transform.FromEuler("\"NeckRot\"", p.xRotO, yNeck, 0.0f)); // Rotate around the neck
                transformStack.add(Transform.FromPos("\"HeadHalfHeight\"", 0d, 0.25d, 0d)); // Add half a head height
                UpdateHitbox(EPlayerHitArea.HEAD, transformStack.Top(), headHalfSize);
            }
        }
    }

    public void UpdateHitbox(EPlayerHitArea area, Transform centerPoint, Vector3f halfExtents)
    {
        hitboxes[area.ordinal()].transform = centerPoint;
        hitboxes[area.ordinal()].halfExtents = halfExtents;
    }

    public void Raycast(Vec3 origin, Vec3 motion, List<HitResult> results)
    {
        Vec3 localPos = origin.subtract(rootPos);

        for(PlayerHitbox hitbox : hitboxes)
        {
            PlayerHitResult hit = hitbox.Raycast(player, localPos, motion);
            if(hit != null)
            {
                results.add(hit);
            }
        }
    }

    public Transform GetMuzzlePosition()
    {
        return hitboxes[EPlayerHitArea.RIGHTARM.ordinal()].transform;
    }

    // ----------------------------------------------------------
    // Debug Rendering
    @OnlyIn(Dist.CLIENT)
    public void debugRender(Vector4f colour)
    {
        if(!FlansMod.DEBUG)
            return;

        for(PlayerHitbox hitbox : hitboxes)
            if(hitbox != null)
                hitbox.debugRender(colour);
    }


}
