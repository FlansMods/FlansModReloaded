package com.flansmod.common.actions.nodes;

import com.flansmod.client.render.FirstPersonManager;
import com.flansmod.common.FlansMod;
import com.flansmod.common.FlansModConfig;
import com.flansmod.common.actions.ActionGroupInstance;
import com.flansmod.common.actions.ActionInstance;
import com.flansmod.common.actions.contexts.*;
import com.flansmod.common.gunshots.GunshotCollection;
import com.flansmod.common.projectiles.CasingEntity;
import com.flansmod.common.types.Constants;
import com.flansmod.common.types.guns.elements.ActionDefinition;
import com.flansmod.common.types.magazines.MagazineDefinition;
import com.flansmod.physics.common.util.MinecraftHelpers;
import com.flansmod.physics.common.util.Transform;
import com.flansmod.physics.common.util.TransformStack;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class EjectCasingAction extends ActionInstance {

    public static final int ID = 2;

    public CasingEntity casingEntity;

    public Vec3 position = Vec3.ZERO;
    public Vec3 velocity = Vec3.ZERO;
    public Vec3 dir = Vec3.ZERO;
    public Vec3 playerPosition = Vec3.ZERO;
    public String spawnCasing = "";

    public EjectCasingAction(@NotNull ActionGroupInstance group, @NotNull ActionDefinition def) {
        super(group, def);
    }

    public static class CasingNetData extends ActionInstance.NetData
    {
        public static final int ID = 2;
        public static final EjectCasingAction.CasingNetData Invalid = new EjectCasingAction.CasingNetData();

        public Vec3 Position;
        public Vec3 Velocity;
        public Vec3 Dir;
        public Vec3 PlayerPos;
        public String ItemToSpawn;


        public CasingNetData()
        {
            Position = Vec3.ZERO;
            Velocity = Vec3.ZERO;
            Dir = Vec3.ZERO;
        }

        public CasingNetData(Vec3 pos, Vec3 vel, Vec3 d, Vec3 playerPos, String s)
        {
            Position = pos;
            Velocity = vel;
            Dir = d;
            PlayerPos = playerPos;
            ItemToSpawn = s;
        }

        @Override
        public int GetID()
        {
            return ID;
        }

        @Override
        public void Encode(FriendlyByteBuf buf)
        {
            buf.writeFloat((float) Position.x());
            buf.writeFloat((float) Position.y());
            buf.writeFloat((float) Position.z());
            buf.writeFloat((float) Velocity.x());
            buf.writeFloat((float) Velocity.y());
            buf.writeFloat((float) Velocity.z());
            buf.writeFloat((float) Dir.x());
            buf.writeFloat((float) Dir.y());
            buf.writeFloat((float) Dir.z());
            buf.writeFloat((float) PlayerPos.x());
            buf.writeFloat((float) PlayerPos.y());
            buf.writeFloat((float) PlayerPos.z());

            CompoundTag tag = new CompoundTag();
            tag.putString("bulletToSpawn", ItemToSpawn);
            buf.writeNbt(tag);
            //buf.writeCharSequence(toString().toCharArray(),)
        }

        @Override
        public void Decode(FriendlyByteBuf buf)
        {
            Position = new Vec3(buf.readFloat(),buf.readFloat(),buf.readFloat());
            Velocity = new Vec3(buf.readFloat(),buf.readFloat(),buf.readFloat());
            Dir = new Vec3(buf.readFloat(),buf.readFloat(),buf.readFloat());
            PlayerPos = new Vec3(buf.readFloat(),buf.readFloat(),buf.readFloat());
            ItemToSpawn = buf.readNbt().getString("bulletToSpawn");
        }
    }

    @Override
    public void OnTriggerClient(int triggerIndex)
    {
        if(Group.Context.Gun instanceof GunContextPlayer playerGunContext) {
            GunContext gunContext = Group.Context.Gun;

            ItemDisplayContext transformType;
            if (!Minecraft.getInstance().options.getCameraType().isFirstPerson() || !gunContext.GetShooter().IsLocalPlayerOwner()) {
                transformType = MinecraftHelpers.getThirdPersonTransformType(gunContext.GetShooter().IsLocalPlayerOwner(), playerGunContext.GetHand());
            } else {
                transformType = MinecraftHelpers.getFirstPersonTransformType(playerGunContext.GetHand());
            }


            TransformStack transformStack = TransformStack.empty();
            Transform origin = FirstPersonManager.GetWorldSpaceAPTransform(gunContext, transformType, ActionGroupContext.CreateGroupPath(AttachPoint()));
            Transform direction = FirstPersonManager.GetWorldSpaceAPTransform(gunContext, transformType, ActionGroupContext.CreateGroupPath(EjectDirection()));

            transformStack = TransformStack.empty();
            transformStack.add(direction);
            Vec3 look = transformStack.top().forward();

            float speed = (float) EjectSpeed();

            transformStack = TransformStack.empty();
            transformStack.add(origin);
            Vec3 position = transformStack.top().positionVec3();
            Vec3 velocity = look.scale(speed * (0.8f + (Math.random() * 0.4f)));

            //float speed2 = (float) (speed * (0.8f + (Math.random() * 0.4f)));
            this.position = position;
            this.velocity = velocity;
            this.dir = transformStack.top().forward();
            this.playerPosition = gunContext.GetShooter().Entity().position();
            //this.spawnCasing = Group.Context.GetMagazineType(0).spawnBulletCasing;
        }
    }

    public ShootAction getShot(){
        for(ActionInstance instance : Group.GetActions()){
            if(instance instanceof ShootAction)
            {
                return (ShootAction) instance;
            }
        }
        return null;
    }

    @Nonnull
    @Override
    public ActionInstance.NetData GetNetDataForTrigger(int triggerIndex)
    {
        if(Group.Context.Gun instanceof GunContextPlayer playerGunContext) {
            GunContext gunContext = Group.Context.Gun;

            ItemDisplayContext transformType;
            if (!Minecraft.getInstance().options.getCameraType().isFirstPerson() || !gunContext.GetShooter().IsLocalPlayerOwner()) {
                transformType = MinecraftHelpers.getThirdPersonTransformType(gunContext.GetShooter().IsLocalPlayerOwner(), playerGunContext.GetHand());
            } else {
                transformType = MinecraftHelpers.getFirstPersonTransformType(playerGunContext.GetHand());
            }


            TransformStack transformStack = TransformStack.empty();
            Transform origin = FirstPersonManager.GetWorldSpaceAPTransform(gunContext, transformType, ActionGroupContext.CreateGroupPath(AttachPoint()));
            Transform direction = FirstPersonManager.GetWorldSpaceAPTransform(gunContext, transformType, ActionGroupContext.CreateGroupPath(EjectDirection()));

            transformStack = TransformStack.empty();
            transformStack.add(direction);
            Vec3 look = transformStack.top().forward();

            float speed = (float) EjectSpeed();

            transformStack = TransformStack.empty();
            transformStack.add(origin);
            Vec3 position = transformStack.top().positionVec3();
            Vec3 velocity = look.scale(speed * (0.8f + (Math.random() * 0.4f)));

            //float speed2 = (float) (speed * (0.8f + (Math.random() * 0.4f)));
            this.position = position;
            this.velocity = velocity;
            this.dir = transformStack.top().forward();
            this.playerPosition = gunContext.GetShooter().Entity().position();
            //this.spawnCasing = Group.Context.GetMagazineType(0).spawnBulletCasing;

        }
        if (position != Vec3.ZERO)
            return new EjectCasingAction.CasingNetData(position,velocity,dir,playerPosition,spawnCasing);
        return EjectCasingAction.CasingNetData.Invalid;
    }

    @Override
    public void UpdateFromNetData(ActionInstance.NetData netData, int triggerIndex)
    {
        if(netData instanceof EjectCasingAction.CasingNetData shootNetData)
        {
            position = shootNetData.Position;
            velocity = shootNetData.Velocity;
            dir = shootNetData.Dir;
            playerPosition = shootNetData.PlayerPos;
            //spawnCasing = shootNetData.ItemToSpawn;
        }
    }

    @Override
    public void OnTriggerServer(int triggerIndex) {
        casingEntity = null;
        MagazineDefinition magDef = Group.Context.GetMagazineType(0);
        if(!magDef.ejectCasings) return;
        if(casingEntity == null)
        {
            if(Group.Context.Gun instanceof GunContextPlayer playerGunContext) {

                CasingEntity casing = new CasingEntity(FlansMod.ENT_TYPE_CASING.get(), Group.Context.Gun.GetLevel());

                GunContext context = Group.Context.Gun;
                casing.InitContext(Group.Context);

                Vec3 playerPosNew = context.GetShooter().Entity().position();

                Vec3 offset = new Vec3(
                        playerPosNew.x()-playerPosition.x(),
                        playerPosNew.y()-playerPosition.y(),
                        playerPosNew.z()-playerPosition.z());

                position = position.add(offset);

                casing.setPos(position);
                casing.SetVelocity(velocity);
                casing.RecalculateFacing(dir);
                casing.setOldPosAndRot();
                casing.Action = this;
                this.spawnCasing = Group.Context.GetMagazineType(0).spawnBulletCasing;
                casing.itemToSpawn = spawnCasing;
                Group.Context.Gun.GetLevel().addFreshEntity(casing);
                casingEntity = casing;
            }
        }
//        if(Group.Context.Gun instanceof GunContextPlayer playerGunContext) {
//
//            CasingEntity casing = new CasingEntity(FlansMod.ENT_TYPE_CASING.get(), Group.Context.Gun.GetLevel());
//
//            GunContext context = Group.Context.Gun;
//            casing.InitContext(Group.Context);
//            casing.InitAction(Save());
//
//            casing.setPos(Group.Context.Gun.GetShooter().Entity().getEyePosition());
//            casing.Action = this;
//
//            Group.Context.Gun.GetLevel().addFreshEntity(casing);
//            casingEntity = casing;
//        }

    }

    @Override
    public boolean PropogateToServer() {
        return true;
    }

    @Override
    public void OnTickClient() {
        super.OnTickClient();
        if(position != null){

        }
    }

    @Override
    public void OnTickServer() {
        super.OnTickServer();
    }

    @Nonnull
    public String AttachPoint() { return Group.Context.ModifyString(Constants.CASING_EJECT_POINT+Def.id, "casing_eject"); }
    @Nonnull
    public String EjectDirection() { return Group.Context.ModifyString(Constants.CASING_EJECT_DIRECTION+Def.id, "casing_eject_direction"); }
    public float EjectSpeed() { return Group.Context.ModifyFloat(Constants.CASING_EJECT_SPEED+Def.id).get();}


    public CompoundTag Save(){
        CompoundTag tag = new CompoundTag();
        tag.putFloat("speed",EjectSpeed());
        tag.putString("attachPoint",AttachPoint());
        tag.putString("ejectDirection",EjectDirection());

        return tag;
    }
}
