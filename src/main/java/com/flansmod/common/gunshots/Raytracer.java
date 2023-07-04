package com.flansmod.common.gunshots;

import com.flansmod.client.FlansModClient;
import com.flansmod.client.render.debug.DebugRenderer;
import com.flansmod.common.FlansMod;
import com.flansmod.util.Transform;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.SectionPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import org.joml.Vector3f;
import org.joml.Vector4f;

import javax.annotation.Nonnull;
import java.util.*;

public class Raytracer
{
    private static final int NUM_SNAPSHOTS_TO_KEEP = 100;
    private static HashMap<LevelAccessor, Raytracer> raytracers = new HashMap<>();
    public static Raytracer ForLevel(LevelAccessor level) { return raytracers.get(level); }


    private LevelAccessor world;
    private HashMap<Player, PlayerMovementHistory> playerMoves = new HashMap<Player, PlayerMovementHistory>();

    public Raytracer(LevelAccessor w)
    {
        world = w;
        raytracers.put(w, this);
    }

    public void hook()
    {
        MinecraftForge.EVENT_BUS.addListener(this::commonTick);
        MinecraftForge.EVENT_BUS.addListener(this::clientTick);
    }

    public void commonTick(TickEvent.LevelTickEvent event)
    {
        for(Player player : world.players())
        {
            PlayerMovementHistory moves = playerMoves.get(player);
            if(moves == null)
            {
               moves = new PlayerMovementHistory(NUM_SNAPSHOTS_TO_KEEP);
               playerMoves.put(player, moves);
            }

            PlayerSnapshot nextSnapshot = moves.GetNextSnapshotForWriting();
            nextSnapshot.SnapPlayer(player);
            moves.FinishedWriting();
        }
    }

    public void clientTick(TickEvent.ClientTickEvent event)
    {
        if(FlansMod.DEBUG)
        {
            for(var kvp : playerMoves.entrySet())
            {
                kvp.getValue().debugRender();
            }
        }
    }

    @Nonnull
    public PlayerSnapshot GetSnapshot(Player player, int nTicksAgo)
    {
        PlayerMovementHistory movementHistory = playerMoves.get(player);
        if (movementHistory != null)
        {
            return movementHistory.GetSnapshotNTicksAgo(nTicksAgo);
        }
        return PlayerSnapshot.INVALID;
    }

    public void CastBullet(Entity from,
                           Vec3 origin,
                           Vec3 motion,
                           double penetrationPowerVsBlocks,
                           double penetrationPowerVsEntities,
                           List<HitResult> outHitList)
    {
        outHitList.clear();

        double distanceRemaining = motion.length();

        Vec3 testPoint = origin;
        Vec3 endPoint = origin.add(motion);

        int numTests = 0;

        while(testPoint.distanceToSqr(endPoint) > 0.0001d)
        {
            // Get the next block hit and all entity hits inbetween
            List<HitResult> hitsFromThisSection = new ArrayList<>(8);
            // Move our test point to the end of the cast section
            testPoint = GetHitsUpToNextBlock(origin, endPoint, hitsFromThisSection);

            for(HitResult hit : hitsFromThisSection)
            {
                // Take the hit and calculate the penetration falloff
                boolean bCanPenetrate = false;
                boolean bShouldHit = true;
                switch (hit.getType())
                {
                    case ENTITY ->
                    {
                        EntityHitResult entHit = (EntityHitResult) hit;
                        if(entHit.getEntity() instanceof LivingEntity living)
                        {
                            bCanPenetrate = penetrationPowerVsEntities >= living.getArmorValue();
                        }
                        else bCanPenetrate = true;

                        bShouldHit = entHit.getEntity() != from;
                    }
                    case BLOCK ->
                    {
                        BlockHitResult blockHit = (BlockHitResult) hit;
                        if(from.level.isLoaded(blockHit.getBlockPos()))
                        {
                            bCanPenetrate = penetrationPowerVsBlocks >= from.level.getBlockState(blockHit.getBlockPos()).getBlock().defaultDestroyTime();
                        }
                        else bCanPenetrate = false;
                    }
                }

                if(bShouldHit)
                    outHitList.add(hit);
                if(!bCanPenetrate)
                {
                    testPoint = endPoint;
                    break;
                }
                else // To avoid repeat collisions, we need to move past the exit point of the current hit
                {
                    // TODO:
                    testPoint = endPoint;
                    break;
                }
            }

            numTests++;
            if(numTests > 100)
            {
                FlansMod.LOGGER.warn("Raytrace exceeded 100 raycasts, something is probably wrong");
                if(from.level.isClientSide)
                {
                    DebugRenderer.RenderLine(
                        origin,
                        100,
                        new Vector4f(1.0f, 1.0f, 1.0f, 1.0f),
                        endPoint.subtract(origin));
                }
                break;
            }
        }
    }

    @Nonnull
    private Vec3 GetHitsUpToNextBlock(Vec3 origin, Vec3 ray, List<HitResult> outResults)
    {
        Vec3 startPoint = origin;
        Vec3 endPoint = ray;

        // Run a vanilla raytrace against the terrain
        ClipContext clipContext = new ClipContext(
            startPoint,
            endPoint,
            ClipContext.Block.COLLIDER,
            ClipContext.Fluid.NONE,
            null
        );
        BlockHitResult blockHit = world.clip(clipContext);
        if(blockHit.getType() != HitResult.Type.MISS)
        {
            outResults.add(blockHit);
            // For the rest of the entity checks, just go up to this endPoint
            endPoint = blockHit.getLocation();
        }

        // Then find entities between the origin and this block hit (or the full ray)
        // This vanilla AABB check is going to be disgustingly slow if we shoot diagonally on all axes
        // TODO: Optimise into several AABBs in this case?
        AABB bounds = new AABB(startPoint, endPoint);
        for(Entity checkEnt : world.getEntities(null, bounds))
        {
            if(checkEnt instanceof Player checkPlayer)
            {
                // Do player snapshot check
                PlayerMovementHistory history = playerMoves.get(checkPlayer);
                if(history != null)
                {
                    history.GetSnapshotNTicksAgo(0).Raycast(startPoint, endPoint, outResults);
                    continue;
                }
            }

            // This may still happen for a player, if their movement history was not found for some reason
            Optional<Vec3> hit = checkEnt.getBoundingBox().clip(startPoint, endPoint);
            hit.ifPresent(vec3 -> outResults.add(new EntityHitResult(checkEnt, vec3)));
        }

        if(outResults.size() > 0)
        {
            outResults.sort(new CompareHits(origin));
        }

        return endPoint;
    }
    private static class CompareHits implements Comparator<HitResult>
    {
        private Vec3 origin;

        public CompareHits(Vec3 ori)
        {
            origin = ori;
        }

        @Override
        public int compare(HitResult o1, HitResult o2)
        {
            if(o1 == null || o1.getType() == HitResult.Type.MISS) return -1;
            if(o2 == null || o2.getType() == HitResult.Type.MISS) return 1;

            return Double.compare(o1.getLocation().distanceToSqr(origin),
                                  o2.getLocation().distanceToSqr(origin));
        }
    }
}
