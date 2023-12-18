package com.flansmod.common.actions.nodes;

import com.flansmod.common.FlansMod;
import com.flansmod.common.actions.ActionGroupInstance;
import com.flansmod.common.actions.ActionInstance;
import com.flansmod.common.actions.EActionResult;
import com.flansmod.common.entity.INpcRelationshipsCapability;
import com.flansmod.common.entity.NpcRelationshipsCapability;
import com.flansmod.common.entity.ShopkeeperEntity;
import com.flansmod.common.actions.contexts.ShooterContextPlayer;
import com.flansmod.common.types.guns.elements.ActionDefinition;
import com.flansmod.common.types.elements.ModifierDefinition;
import com.flansmod.common.types.npc.NpcDefinition;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;

import java.util.Optional;

public class SummonNpcAction extends ActionInstance
{
	public SummonNpcAction(@NotNull ActionGroupInstance group, @NotNull ActionDefinition def)
	{
		super(group, def);
	}

	@Override
	public EActionResult CanStart()
	{
		ResourceLocation npcID = EntityType();
		NpcDefinition npcDef = FlansMod.NPCS.Get(npcID);
		if(!npcDef.IsValid())
			return EActionResult.TryNextAction;

		EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(npcID);
		if(entityType == null)
			return EActionResult.TryNextAction;

		Level level = Group.Context.Gun.GetLevel();
		if(level == null)
			return EActionResult.TryNextAction;

		// Check for the specific conditions for summoning a Flan's Mod NPC
		if(Group.Context.Gun.GetShooter() instanceof ShooterContextPlayer playerContext)
		{
			LazyOptional<INpcRelationshipsCapability> relationshipCap = playerContext.Player.getCapability(NpcRelationshipsCapability.INSTANCE);
			if(relationshipCap.isPresent() && relationshipCap.resolve().isPresent())
			{
				int cooldownTicks = relationshipCap.resolve().get().GetCooldownTicks(npcID);
				if (cooldownTicks > 0)
				{
					playerContext.Player.sendSystemMessage(Component.translatable("action.summon_npc.on_cooldown", cooldownTicks / 20));
					return EActionResult.TryNextAction;
				}
			}
		}

		// Check for other matching NPCs in the nearby loaded chunks
		Vector3d pos = Group.Context.Gun.GetShootOrigin().Position;
		double checkRange = 400D;
		for(ShopkeeperEntity nearbyShopkeeper : level.getEntitiesOfClass(
			ShopkeeperEntity.class,
			new AABB(pos.x - checkRange, pos.y - checkRange, pos.z - checkRange,
				pos.x + checkRange, pos.y + checkRange, pos.z + checkRange)))
		{
			if(nearbyShopkeeper.GetDef().Location.equals(npcID))
			{
				if(Group.Context.Gun.GetShooter() instanceof ShooterContextPlayer playerContext)
					playerContext.Player.sendSystemMessage(Component.translatable("action.summon_npc.already_spawned"));
				return EActionResult.TryNextAction;
			}
		}

		return EActionResult.CanProcess;
	}

	public ResourceLocation EntityType()
	{
		return new ResourceLocation(Group.Context.ModifyString(ModifierDefinition.KEY_ENTITY_ID, ""));
	}

	@Override
	public void OnTriggerClient(int triggerIndex)
	{

	}

	@Override
	public void OnTriggerServer(int triggerIndex)
	{
		String entityID = EntityID();
		ResourceLocation npcID = new ResourceLocation(entityID);
		EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(npcID);
		if(entityType != null)
		{
			Level level = Group.Context.Gun.GetLevel();
			if(level != null)
			{
				Entity entity = entityType.create(level);
				if(entity != null)
				{
					Vec3 srcPos = Group.Context.Gun.GetShootOrigin().PositionVec3();
					entity.setPos(srcPos);
					Optional<Vec3> freePos = level.findFreePosition(
						entity,
						Shapes.create(entity.getBoundingBox()),
						srcPos,
						1d,
						1d,
						1d);
					entity.setPos(freePos.orElse(srcPos));
					level.addFreshEntity(entity);

					// Set our spawn cooldown
					if(Group.Context.Gun.GetShooter() instanceof ShooterContextPlayer playerContext
						&& entity instanceof ShopkeeperEntity shopkeeper)
					{
						LazyOptional<INpcRelationshipsCapability> relationshipCap = playerContext.Player.getCapability(NpcRelationshipsCapability.INSTANCE);
						if(relationshipCap.isPresent() && relationshipCap.resolve().isPresent())
						{
							relationshipCap.resolve().get().SetCooldownTicks(npcID, shopkeeper.GetDef().CooldownTicks(true));
						}
					}
				}

			}
			else FlansMod.LOGGER.warn("SpawnEntityAction[" + Def + "]: Could not find level");
		}
	}

	public String EntityID() { return Group.Context.ModifyString(ModifierDefinition.KEY_ENTITY_ID, ""); }
}
