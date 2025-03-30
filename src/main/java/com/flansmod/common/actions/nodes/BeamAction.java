package com.flansmod.common.actions.nodes;

import com.flansmod.common.actions.ActionGroupInstance;
import com.flansmod.common.actions.ActionInstance;
import com.flansmod.common.actions.contexts.TriggerContext;
import com.flansmod.common.types.abilities.elements.EAbilityTrigger;
import com.flansmod.common.types.guns.elements.ActionDefinition;
import com.flansmod.physics.common.util.Maths;
import com.flansmod.physics.common.util.Transform;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;

import static com.flansmod.common.types.Constants.*;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class BeamAction extends ActionInstance
{
	private static class CachedTicker
	{
		public CachedTicker(int t) { ticks = t; }
		public int ticks;
	}

	// BeamVolume

	private final float BeamLength;
	private final float BeamStartRadius;
	private final float BeamEndRadius;
	private final float BeamMaxRadius;
	private final boolean VariableRadiusBeam;

	private final boolean CastEntities;
	private final boolean CastBlocks;

	private final boolean ProcessBlocksOverTime;
	private final boolean ProcessBlocksFactorInBreakSpeed;
	private final boolean TriggerRepeatedlyOnBlocks;
	private final int ProcessBlockDefaultTicks;
	private final boolean TriggerRepeatedlyOnEntities;
	private final int TriggerIntervalOnEntities;

	public Transform CachedTransform;
	public Map<Entity, CachedTicker> CachedEntityHits = new HashMap<>();
	public Map<BlockPos, CachedTicker> CachedBlockHits = new HashMap<>();


	public BeamAction(@Nonnull ActionGroupInstance group, @Nonnull ActionDefinition def)
	{
		super(group, def);

		BeamLength = group.Context.ModifyFloat(STAT_BEAM_LENGTH).apply(1.0f);
		BeamStartRadius = group.Context.ModifyFloat(STAT_BEAM_START_RADIUS).apply(1.0f);
		BeamEndRadius = group.Context.ModifyFloat(STAT_BEAM_END_RADIUS).apply(1.0f);
		BeamMaxRadius = Maths.max(BeamStartRadius, BeamEndRadius);
		VariableRadiusBeam = !Maths.approx(BeamStartRadius, BeamEndRadius);

		CastEntities = group.Context.ModifyBoolean(STAT_BEAM_HIT_ENTITIES, true);
		CastBlocks = group.Context.ModifyBoolean(STAT_BEAM_HIT_BLOCKS, true);

		TriggerRepeatedlyOnEntities = group.Context.ModifyBoolean(STAT_BEAM_TRIGGER_ENTITY_REPEATS, true);
		TriggerIntervalOnEntities = Maths.ceil(group.Context.ModifyFloat(STAT_BEAM_TRIGGER_ENTITY_INTERVAL).apply(1.0f) * 20f);

		TriggerRepeatedlyOnBlocks = group.Context.ModifyBoolean(STAT_BEAM_PROCESS_BLOCKS_REPEATS, true);
		ProcessBlocksOverTime = group.Context.ModifyBoolean(STAT_BEAM_PROCESS_BLOCKS, true);
		ProcessBlocksFactorInBreakSpeed = group.Context.ModifyBoolean(STAT_BEAM_PROCESS_BLOCKS_BREAK_SPEED, true);
		ProcessBlockDefaultTicks = Maths.ceil(group.Context.ModifyFloat(STAT_BEAM_PROCESS_BLOCKS_TIME).apply(1.0f) * 20f);

		CachedTransform = group.Context.Gun.GetShootOrigin();
	}

	public int GetTicksForBlock(@Nonnull Level level, @Nonnull BlockPos pos)
	{
		if(!ProcessBlocksOverTime)
			return 0;

		if(ProcessBlocksFactorInBreakSpeed)
		{
			BlockState blockState = level.getBlockState(pos);
			float destroySpeed = blockState.getDestroySpeed(level, pos);
			if(destroySpeed == 0f)
				return 0;
			return Maths.ceil(ProcessBlockDefaultTicks / destroySpeed);
		}

		return ProcessBlockDefaultTicks;
	}

	@Override
	public void OnStartClient()
	{

	}

	@Override
	public void OnTickClient()
	{
		Transform transform = Group.Context.Gun.GetShootOrigin();
		UpdateBeam(transform,
			this::OnEntityEnterBeamClient,
			this::OnEntityLeaveBeamClient,
			this::OnBlockEnterBeamClient,
			this::OnBlockLeaveBeamClient);
		UpdateTickersInBeam(
			this::OnBlockLeaveBeamClient);
	}
	@Override
	public void OnTickServer()
	{
		Transform transform = Group.Context.Gun.GetShootOrigin();
		UpdateBeam(transform,
			this::OnEntityEnterBeamServer,
			this::OnEntityLeaveBeamServer,
			this::OnBlockEnterBeamServer,
			this::OnBlockLeaveBeamServer);
		UpdateTickersInBeam(
			this::OnBlockLeaveBeamServer);
	}

	private boolean IsIgnoredByBeam(@Nonnull BlockState blockState)
	{
		return blockState.isAir();
	}

	public void UpdateTickersInBeam(@Nonnull Consumer<BlockPos> blockLeaveFunc)
	{
		Level level = Group.Context.Gun.GetLevel();

		for(var kvp : CachedEntityHits.entrySet())
		{
			if(kvp.getValue().ticks == 0)
				continue;

			kvp.getValue().ticks--;
			if(kvp.getValue().ticks == 0)
			{
				// Send the "BeamStay" event
				Group.Context.Gun.GetActionStack().EvaluateTrigger(
					EAbilityTrigger.BeamStayInterval,
					Group.Context,
					TriggerContext.hit(Group.Context.Gun.GetShooter(), new EntityHitResult(kvp.getKey())));

				// Then start the ticker again, if that's required
				if(TriggerRepeatedlyOnEntities)
					kvp.getValue().ticks = TriggerIntervalOnEntities;
			}
		}

		List<BlockPos> invalidatedBlockPositions = new ArrayList<>();
		for(var kvp : CachedBlockHits.entrySet())
		{
			if(kvp.getValue().ticks == 0)
				continue;

			kvp.getValue().ticks--;
			if(kvp.getValue().ticks == 0)
			{
				// Send the "BeamStay" event
				Group.Context.Gun.GetActionStack().EvaluateTrigger(
					EAbilityTrigger.BeamStayInterval,
					Group.Context,
					TriggerContext.hit(Group.Context.Gun.GetShooter(), new BlockHitResult(kvp.getKey().getCenter(), Direction.UP, kvp.getKey(), true)));

				if(level != null)
				{
					// Here, we check the blockState again, as it may have been changed by the trigger
					BlockState blockState = level.getBlockState(kvp.getKey());
					if(IsIgnoredByBeam(blockState))
					{
						invalidatedBlockPositions.add(kvp.getKey());
					}
					else
					{
						// Then start the ticker again, if that's required.
						if (TriggerRepeatedlyOnBlocks)
						{
							kvp.getValue().ticks = GetTicksForBlock(level, kvp.getKey());
						}
					}
				}
			}
		}

		for(BlockPos invalid : invalidatedBlockPositions)
		{
			blockLeaveFunc.accept(invalid);
			CachedBlockHits.remove(invalid);
		}
	}

	public void UpdateBeam(@Nonnull Transform transform,
						   @Nonnull Consumer<Entity> entityEnterFunc,
						   @Nonnull Consumer<Entity> entityLeaveFunc,
						   @Nonnull Consumer<BlockPos> blockEnterFunc,
						   @Nonnull Consumer<BlockPos> blockLeaveFunc)
	{
		Level level = Group.Context.Gun.GetLevel();
		if(level != null)
		{
			if (!CachedTransform.isApprox(transform, Maths.EpsilonF))
			{
				if (CastEntities)
				{
					// Cast entities,
					List<Entity> newHits = CastBeamVolume_Entities(transform);
					// Gather our list of removals, i.e. cached ones that did not show up in the new cast
					List<Entity> removedHits = new ArrayList<>(CachedEntityHits.size());
					for(Entity prevEntity : CachedEntityHits.keySet())
					{
						if(!newHits.contains(prevEntity))
							removedHits.add(prevEntity);
					}
					// Then trim our hitlist down to just the new list
					for (int i = newHits.size() - 1; i >= 0; i--)
					{
						if(CachedEntityHits.containsKey(newHits.get(i)))
							newHits.remove(i);
					}

					// Process removals
					for(Entity removedHit : removedHits)
					{
						entityLeaveFunc.accept(removedHit);
						CachedEntityHits.remove(removedHit);
					}
					// Process additions
					for(Entity newHit : newHits)
					{
						CachedEntityHits.put(newHit, new CachedTicker(TriggerIntervalOnEntities));
						entityEnterFunc.accept(newHit);
					}
				}
				if (CastBlocks)
				{
					// Cast entities,
					List<BlockPos> newHits = CastBeamVolume_Blocks(transform);
					// Gather our list of removals, i.e. cached ones that did not show up in the new cast
					List<BlockPos> removedHits = new ArrayList<>(CachedBlockHits.size());
					for(BlockPos prevBlock : CachedBlockHits.keySet())
					{
						if(!newHits.contains(prevBlock))
							removedHits.add(prevBlock);
					}
					// Then trim our hitlist down to just the new list
					for (int i = newHits.size() - 1; i >= 0; i--)
					{
						if(CachedBlockHits.containsKey(newHits.get(i)))
							newHits.remove(i);
					}

					// Process removals
					for(BlockPos removedHit : removedHits)
					{
						blockLeaveFunc.accept(removedHit);
						CachedBlockHits.remove(removedHit);
					}
					// Process additions
					for(BlockPos newHit : newHits)
					{
						int ticks = GetTicksForBlock(level, newHit);
						CachedBlockHits.put(newHit, new CachedTicker(ticks));
						blockEnterFunc.accept(newHit);
					}
				}
			}
		}
	}


	public void OnEntityEnterBeamClient(@Nonnull Entity entity)
	{

	}
	public void OnEntityLeaveBeamClient(@Nonnull Entity entity)
	{

	}
	public void OnBlockEnterBeamClient(@Nonnull BlockPos blockPos)
	{

	}
	public void OnBlockLeaveBeamClient(@Nonnull BlockPos blockPos)
	{

	}

	public void OnEntityEnterBeamServer(@Nonnull Entity entity)
	{
		Group.Context.Gun.GetActionStack().EvaluateTrigger(
			EAbilityTrigger.BeamEnter,
			Group.Context,
			TriggerContext.hit(Group.Context.Gun.GetShooter(), new EntityHitResult(entity)));

	}
	public void OnEntityLeaveBeamServer(@Nonnull Entity entity)
	{
		Group.Context.Gun.GetActionStack().EvaluateTrigger(
			EAbilityTrigger.BeamLeave,
			Group.Context,
			TriggerContext.hit(Group.Context.Gun.GetShooter(), new EntityHitResult(entity)));
	}
	public void OnBlockEnterBeamServer(@Nonnull BlockPos blockPos)
	{
		Group.Context.Gun.GetActionStack().EvaluateTrigger(
			EAbilityTrigger.BeamEnter,
			Group.Context,
			TriggerContext.hit(Group.Context.Gun.GetShooter(), new BlockHitResult(blockPos.getCenter(), Direction.UP, blockPos, true)));

	}
	public void OnBlockLeaveBeamServer(@Nonnull BlockPos blockPos)
	{
		Group.Context.Gun.GetActionStack().EvaluateTrigger(
			EAbilityTrigger.BeamLeave,
			Group.Context,
			TriggerContext.hit(Group.Context.Gun.GetShooter(), new BlockHitResult(blockPos.getCenter(), Direction.UP, blockPos, true)));

	}

	@Nonnull
	public List<Entity> CastBeamVolume_Entities(@Nonnull Transform beamOrigin)
	{
		Entity shooter = Group.Context.Gun.GetShooter().Entity();
		Level level = Group.Context.Gun.GetLevel();
		if(level != null)
		{
			Vec3 startPos = beamOrigin.positionVec3();
			Vec3 deltaPos = beamOrigin.forward().scale(BeamLength);
			Vec3 endPos = startPos.add(deltaPos);

			AABB beamBounds = new AABB(startPos, endPos).inflate(Maths.max(BeamStartRadius, BeamEndRadius));
			return level.getEntities(shooter, beamBounds, entity -> {

				// !! Currently just checks for entity centers

				Vec3 beamRelativePos = entity.position().subtract(startPos);
				double beamDotEntSq = beamRelativePos.dot(deltaPos);
				// Before the start of the beam or after the end
				if(beamDotEntSq < 0.0f || beamDotEntSq > BeamLength * BeamLength)
					return false;

				double nearestBeamPointT = Maths.sqrt(beamDotEntSq)/BeamLength;
				Vec3 nearestBeamPointRelative = deltaPos.scale(nearestBeamPointT);
				Vec3 entToBeam = beamRelativePos.subtract(nearestBeamPointRelative);
				double distSq = entToBeam.lengthSqr();
				// Quick check to see if we are too far
				if(distSq > BeamMaxRadius * BeamMaxRadius)
					return false;

				if(VariableRadiusBeam)
				{
					double radiusAtT = Maths.lerp(BeamStartRadius, BeamEndRadius, nearestBeamPointT);
					if(distSq > radiusAtT * radiusAtT)
						return false;
				}

				return true;
			});
		}
		return List.of();
	}

	@Nonnull
	public List<BlockPos> CastBeamVolume_Blocks(@Nonnull Transform beamOrigin)
	{
		List<BlockPos> blocks = new ArrayList<>();
		Level level = Group.Context.Gun.GetLevel();
		if(level != null)
		{
			// Level.clip is for single rays, zero width
			// level.clip(new ClipContext())

			Vec3 startPos = beamOrigin.positionVec3();
			Vec3 deltaPos = beamOrigin.forward().scale(BeamLength);
			Vec3 endPos = startPos.add(deltaPos);
			AABB beamBounds = new AABB(startPos, endPos).inflate(Maths.max(BeamStartRadius, BeamEndRadius));

			// Slow, brute force approach
			for(int i = Maths.floor(beamBounds.minX); i < Maths.ceil(beamBounds.maxX); i++)
			{
				for(int j = Maths.floor(beamBounds.minY); j < Maths.ceil(beamBounds.maxY); j++)
				{
					for(int k = Maths.floor(beamBounds.minZ); k < Maths.ceil(beamBounds.maxZ); k++)
					{
						BlockPos blockPos = new BlockPos(i, j, k);

						// Better checks?
						if(IsIgnoredByBeam(level.getBlockState(blockPos)))
							continue;

						Vec3 beamRelativePos = blockPos.getCenter().subtract(startPos);
						double beamDotBlockSq = beamRelativePos.dot(deltaPos);
						// Before the start of the beam or after the end
						if(beamDotBlockSq < 0.0f || beamDotBlockSq > BeamLength * BeamLength)
							continue;


						double nearestBeamPointT = Maths.sqrt(beamDotBlockSq)/BeamLength;
						Vec3 nearestBeamPointRelative = deltaPos.scale(nearestBeamPointT);
						Vec3 blockToBeam = beamRelativePos.subtract(nearestBeamPointRelative);
						double distSq = blockToBeam.lengthSqr();
						// Quick check to see if we are too far
						if(distSq > BeamMaxRadius * BeamMaxRadius)
							continue;

						if(VariableRadiusBeam)
						{
							double radiusAtT = Maths.lerp(BeamStartRadius, BeamEndRadius, nearestBeamPointT);
							if(distSq > radiusAtT * radiusAtT)
								continue;
						}


						blocks.add(blockPos);

					}
				}
			}
		}
		return blocks;
	}

	@Override
	public void OnTriggerClient(int triggerIndex)
	{

	}

	@Override
	public void OnTriggerServer(int triggerIndex)
	{

	}
}
