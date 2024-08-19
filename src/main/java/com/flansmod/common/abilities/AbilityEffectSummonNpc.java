package com.flansmod.common.abilities;

import com.flansmod.common.FlansModConfig;
import com.flansmod.common.actions.contexts.*;
import com.flansmod.common.entity.INpcRelationshipsCapability;
import com.flansmod.common.entity.NpcRelationshipsCapability;
import com.flansmod.common.entity.ShopkeeperEntity;
import com.flansmod.common.types.Constants;
import com.flansmod.common.types.abilities.elements.AbilityEffectDefinition;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public class AbilityEffectSummonNpc implements IAbilityEffect
{
	private final ResourceLocation NpcID;
	@Nullable
	private final EntityType<? extends Entity> EntityType;

	public AbilityEffectSummonNpc(@Nonnull AbilityEffectDefinition def)
	{
		NpcID = new ResourceLocation(def.ModifyString(Constants.KEY_ENTITY_TAG, ""));
		EntityType = ForgeRegistries.ENTITY_TYPES.getValue(NpcID);
	}

	public boolean CanSummonNpc(@Nonnull Player player)
	{
		// Server config hook
		if(!FlansModConfig.AllowSummonNpc.get())
			return false;
		double checkRange = FlansModConfig.SummonNpcMinDistance.get();
		// ------------------

		LazyOptional<INpcRelationshipsCapability> relationshipCap = player.getCapability(NpcRelationshipsCapability.INSTANCE);
		if(relationshipCap.isPresent() && relationshipCap.resolve().isPresent())
		{
			long endCooldownTick = relationshipCap.resolve().get().GetEndCooldownTick(NpcID);
			if (player.level().getGameTime() < endCooldownTick)
			{
				long ticks = endCooldownTick - player.level().getGameTime();
				player.sendSystemMessage(Component.translatable("action.summon_npc.on_cooldown", ticks / 20));
				return false;
			}
		}

		// Check for other matching NPCs in the nearby loaded chunks
		for(ShopkeeperEntity nearbyShopkeeper : player.level().getEntitiesOfClass(
			ShopkeeperEntity.class,
			new AABB(player.getX() - checkRange, player.getY() - checkRange, player.getZ() - checkRange,
				player.getX() + checkRange, player.getY() + checkRange, player.getZ()  + checkRange)))
		{
			if(nearbyShopkeeper.GetDef().Location.equals(NpcID))
			{
				player.sendSystemMessage(Component.translatable("action.summon_npc.already_spawned"));
				return false;
			}
		}

		return true;
	}

	@Override
	public void TriggerServer(@Nonnull ActionGroupContext actionGroup, @Nonnull TriggerContext trigger, @Nonnull TargetsContext targets, @Nullable AbilityStack stacks)
	{
		if(EntityType != null)
		{
			Level level = actionGroup.Gun.GetLevel();
			if(level != null && actionGroup.Gun.GetShooter() instanceof ShooterContextPlayer playerContext)
			{
				if (CanSummonNpc(playerContext.Player))
				{
					targets.ForOnePosition((pos) ->
					{
						Entity entity = EntityType.create(level);
						if (entity != null)
						{
							entity.setPos(pos);
							Optional<Vec3> freePos = level.findFreePosition(
								entity,
								Shapes.create(entity.getBoundingBox()),
								pos,
								1d,
								1d,
								1d);
							entity.setPos(freePos.orElse(pos));
							level.addFreshEntity(entity);

							// Set our spawn cooldown
							if (entity instanceof ShopkeeperEntity shopkeeper)
							{
								LazyOptional<INpcRelationshipsCapability> relationshipCap = playerContext.Player.getCapability(NpcRelationshipsCapability.INSTANCE);
								if (relationshipCap.isPresent() && relationshipCap.resolve().isPresent())
								{
									relationshipCap.resolve().get().SetEndCooldownTick(NpcID, shopkeeper.level().getGameTime() + shopkeeper.GetDef().CooldownTicks(true));
								}
							}
						}
					});
				}
			}
		}
	}
}
