package com.flansmod.common.abilities;

import com.flansmod.common.FlansMod;
import com.flansmod.common.actions.ActionGroupInstance;
import com.flansmod.common.actions.ActionInstance;
import com.flansmod.common.actions.EActionResult;
import com.flansmod.common.actions.contexts.GunContext;
import com.flansmod.common.actions.contexts.TargetsContext;
import com.flansmod.common.entity.INpcRelationshipsCapability;
import com.flansmod.common.entity.NpcRelationshipsCapability;
import com.flansmod.common.entity.ShopkeeperEntity;
import com.flansmod.common.actions.contexts.ShooterContextPlayer;
import com.flansmod.common.types.abilities.elements.AbilityEffectDefinition;
import com.flansmod.common.types.guns.elements.ActionDefinition;
import com.flansmod.common.types.elements.ModifierDefinition;
import com.flansmod.common.types.npc.NpcDefinition;
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
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;

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
		NpcID = new ResourceLocation(def.ModifyString(ModifierDefinition.KEY_ENTITY_TAG, ""));
		EntityType = ForgeRegistries.ENTITY_TYPES.getValue(NpcID);
	}

	public boolean CanSummonNpc(@Nonnull Player player)
	{
		LazyOptional<INpcRelationshipsCapability> relationshipCap = player.getCapability(NpcRelationshipsCapability.INSTANCE);
		if(relationshipCap.isPresent() && relationshipCap.resolve().isPresent())
		{
			int cooldownTicks = relationshipCap.resolve().get().GetCooldownTicks(NpcID);
			if (cooldownTicks > 0)
			{
				player.sendSystemMessage(Component.translatable("action.summon_npc.on_cooldown", cooldownTicks / 20));
				return false;
			}
		}

		// Check for other matching NPCs in the nearby loaded chunks
		double checkRange = 400D;
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
	public void Trigger(@Nonnull GunContext gun, @Nonnull TargetsContext targets, @Nullable AbilityStack stacks)
	{
		if(EntityType != null)
		{
			Level level = gun.GetLevel();
			if(level != null && gun.GetShooter() instanceof ShooterContextPlayer playerContext)
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
									relationshipCap.resolve().get().SetCooldownTicks(NpcID, shopkeeper.GetDef().CooldownTicks(true));
								}
							}
						}
					});
				}
			}
		}
	}
	@Override
	public void End(@Nonnull GunContext gun, @Nullable AbilityStack stacks)
	{

	}
}
