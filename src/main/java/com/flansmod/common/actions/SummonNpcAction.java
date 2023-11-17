package com.flansmod.common.actions;

import com.flansmod.common.FlansMod;
import com.flansmod.common.entity.INpcRelationshipsCapability;
import com.flansmod.common.entity.NpcRelationshipsCapability;
import com.flansmod.common.entity.ShopkeeperEntity;
import com.flansmod.common.gunshots.ShooterContextPlayer;
import com.flansmod.common.types.guns.elements.ActionDefinition;
import com.flansmod.common.types.elements.ModifierDefinition;
import com.flansmod.common.types.npc.NpcDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;

public class SummonNpcAction extends SpawnEntityAction
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

		Level level = Group.Context.Gun.Level;
		if(level == null)
			return EActionResult.TryNextAction;

		// Check for the specific conditions for summoning a Flan's Mod NPC
		if(Group.Context.Gun.GetShooter() instanceof ShooterContextPlayer playerContext)
		{
			LazyOptional<INpcRelationshipsCapability> relationshipCap = playerContext.Player.getCapability(NpcRelationshipsCapability.INSTANCE);
			if(relationshipCap.isPresent() && relationshipCap.resolve().isPresent())
				if(relationshipCap.resolve().get().GetCooldownTicks(npcID) > 0)
					return EActionResult.TryNextAction;
		}

		// Check for other matching NPCs in the nearby loaded chunks
		Vector3d pos = Group.Context.Gun.GetShooter().GetShootOrigin().Position();
		double checkRange = 400D;
		for(ShopkeeperEntity nearbyShopkeeper : level.getEntitiesOfClass(
			ShopkeeperEntity.class,
			new AABB(pos.x - checkRange, pos.y - checkRange, pos.z - checkRange,
				pos.x + checkRange, pos.y + checkRange, pos.z + checkRange)))
		{
			if(nearbyShopkeeper.GetDef().Location.equals(npcID))
				return EActionResult.TryNextAction;
		}

		return EActionResult.CanProcess;
	}

	public ResourceLocation EntityType()
	{
		return new ResourceLocation(Group.Context.ModifyString(ModifierDefinition.KEY_ENTITY_ID, ""));
	}
}
