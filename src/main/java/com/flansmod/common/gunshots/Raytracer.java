package com.flansmod.common.gunshots;

import com.flansmod.common.projectiles.CasingEntity;
import com.flansmod.physics.client.DebugRenderer;
import com.flansmod.common.FlansMod;
import com.flansmod.common.entity.vehicle.VehicleEntity;
import com.flansmod.physics.common.util.Transform;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.joml.Vector4f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class Raytracer
{
    private static final int NUM_SNAPSHOTS_TO_KEEP = 100;
    private static final HashMap<LevelAccessor, Raytracer> Raytracers = new HashMap<>();

    @Nonnull
    public static Raytracer ForLevel(@Nonnull LevelAccessor level)
    {
        if(!Raytracers.containsKey(level))
        {
            Raytracer newInstance = new Raytracer(level);
            newInstance.hook();
            Raytracers.put(level, newInstance);
        }
        return Raytracers.get(level);
    }
    @Nonnull
    private final LevelAccessor World;
    @Nonnull
    private final HashMap<Player, PlayerMovementHistory> PlayerMovementHistories = new HashMap<Player, PlayerMovementHistory>();

    public Raytracer(@Nonnull LevelAccessor w)
    {
        World = w;
        Raytracers.put(w, this);
    }

    public void hook()
    {
        MinecraftForge.EVENT_BUS.addListener(this::commonTick);
        MinecraftForge.EVENT_BUS.addListener(this::clientTick);
    }

    public void commonTick(@Nonnull TickEvent.LevelTickEvent event)
    {
        if(event.phase == TickEvent.Phase.START && event.level == World)
        {
            List<Player> playerList = new ArrayList<>(World.players());
            for (Player player : playerList)
            {
                PlayerMovementHistory moves = PlayerMovementHistories.get(player);
                if (moves == null)
                {
                    moves = new PlayerMovementHistory(NUM_SNAPSHOTS_TO_KEEP);
                    PlayerMovementHistories.put(player, moves);
                }

                moves.TakeSnapshot(player);
            }
        }
    }

    public void clientTick(@Nonnull TickEvent.ClientTickEvent event)
    {
        if(FlansMod.DEBUG || Minecraft.getInstance().getEntityRenderDispatcher().shouldRenderHitBoxes())
        {
            for(var kvp : PlayerMovementHistories.entrySet())
            {
                if(Minecraft.getInstance().options.getCameraType().isFirstPerson())
                {
                    if(kvp.getKey().isLocalPlayer())
                        continue;
                    if(ServerLifecycleHooks.getCurrentServer() != null)
                    {
                        if(ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers().size() == 1)
                            continue;
                    }
                }
                kvp.getValue().debugRender(!(World instanceof ServerLevel));
            }
        }
    }

    @Nonnull
    public PlayerSnapshot GetSnapshot(@Nonnull Player player, int nTicksAgo)
    {
        PlayerMovementHistory movementHistory = PlayerMovementHistories.get(player);
        if (movementHistory != null)
        {
            return movementHistory.GetSnapshotNTicksAgo(nTicksAgo);
        }
        return PlayerSnapshot.INVALID;
    }

    @Nullable
    public HitResult CastBullet(@Nullable Entity from,
                                @Nonnull Vec3 origin,
                                @Nonnull Vec3 motion)
    {
        List<HitResult> hits = new ArrayList<>();
        CastBullet(from, origin, motion, 0.0f, 0.0f, hits);
        if(hits.size() > 0)
        {
            return hits.get(0);
        }
        return null;
    }

    public void CastBullet(@Nullable Entity from,
                           @Nonnull Vec3 origin,
                           @Nonnull Vec3 motion,
                           double penetrationPowerVsBlocks,
                           double penetrationPowerVsEntities,
                           @Nonnull List<HitResult> outHitList)
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
                        if(World.isAreaLoaded(blockHit.getBlockPos(), 1))
                        {
                            bCanPenetrate = penetrationPowerVsBlocks >= World.getBlockState(blockHit.getBlockPos()).getBlock().defaultDestroyTime();
                        }
                        else bCanPenetrate = false;
                    }
                }

                if(bShouldHit)
                {
                    outHitList.add(hit);
                    if (!bCanPenetrate)
                    {
                        testPoint = endPoint;
                        break;
                    } else // To avoid repeat collisions, we need to move past the exit point of the current hit
                    {
                        // TODO:
                        testPoint = endPoint;
                        break;
                    }
                }
            }

            numTests++;
            if(numTests > 100)
            {
                FlansMod.LOGGER.warn("Raytrace exceeded 100 raycasts, something is probably wrong");
                if(World.isClientSide())
                {
                    DebugRenderer.renderLine(
                        Transform.fromPos(origin),
                        100,
                        new Vector4f(1.0f, 1.0f, 1.0f, 1.0f),
                        endPoint.subtract(origin));
                }
                break;
            }
        }
    }

    @Nonnull
    private Vec3 GetHitsUpToNextBlock(@Nonnull Vec3 origin,
                                      @Nonnull Vec3 ray,
                                      @Nonnull List<HitResult> outResults)
    {
        Vec3 endPoint = ray;

        // Run a vanilla raytrace against the terrain
        ClipContext clipContext = new ClipContext(
            origin,
            endPoint,
            ClipContext.Block.COLLIDER,
            ClipContext.Fluid.NONE,
            null
        );
        BlockHitResult blockHit = World.clip(clipContext);
        if(blockHit.getType() != HitResult.Type.MISS)
        {
            outResults.add(blockHit);
            // For the rest of the entity checks, just go up to this endPoint
            endPoint = blockHit.getLocation();
        }

        // Then find entities between the origin and this block hit (or the full ray)
        // This vanilla AABB check is going to be disgustingly slow if we shoot diagonally on all axes
        // TODO: Optimise into several AABBs in this case?
        AABB bounds = new AABB(origin, endPoint);
        for(Entity checkEnt : World.getEntities(null, bounds))
        {
            if(checkEnt instanceof VehicleEntity vehicle)
            {
                vehicle.Raycast(origin, endPoint, 0f, outResults);
                continue;
            }
            else if(checkEnt instanceof Player checkPlayer)
            {
                // Do player snapshot check
                PlayerMovementHistory history = PlayerMovementHistories.get(checkPlayer);
                if(history != null)
                {
                    history.GetSnapshotNTicksAgo(0).Raycast(checkPlayer, origin, endPoint, outResults);
                    continue;
                }
            }
            if(checkEnt instanceof CasingEntity) continue;
            // This may still happen for a player, if their movement history was not found for some reason
            Optional<Vec3> hit = checkEnt.getBoundingBox().clip(origin, endPoint);
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
        private final Vec3 origin;

        public CompareHits(@Nonnull Vec3 ori)
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
