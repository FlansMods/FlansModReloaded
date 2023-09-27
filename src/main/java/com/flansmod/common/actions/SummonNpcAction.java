package com.flansmod.common.actions;

import com.flansmod.common.FlansMod;
import com.flansmod.common.entity.INpcRelationshipsCapability;
import com.flansmod.common.entity.NpcRelationshipsCapability;
import com.flansmod.common.entity.ShopkeeperEntity;
import com.flansmod.common.gunshots.ActionGroupContext;
import com.flansmod.common.gunshots.ShooterContextPlayer;
import com.flansmod.common.types.elements.ActionDefinition;
import com.flansmod.common.types.elements.ModifierDefinition;
import com.flansmod.common.types.npc.NpcDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;

import java.util.List;

public class SummonNpcAction extends SpawnEntityAction
{
	public SummonNpcAction(@NotNull ActionGroup group, @NotNull ActionDefinition def)
	{
		super(group, def);
	}

	@Override
	public boolean CanStart(ActionGroupContext context)
	{
		ResourceLocation npcID = EntityType(context);
		NpcDefinition npcDef = FlansMod.NPCS.Get(npcID);
		if(!npcDef.IsValid())
			return false;

		EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(npcID);
		if(entityType == null)
			return false;

		Level level = context.Level();
		if(level == null)
			return false;

		// Check for the specific conditions for summoning a Flan's Mod NPC
		if(context.Shooter() instanceof ShooterContextPlayer playerContext)
		{
			LazyOptional<INpcRelationshipsCapability> relationshipCap = playerContext.Player.getCapability(NpcRelationshipsCapability.INSTANCE);
			if(relationshipCap.isPresent() && relationshipCap.resolve().isPresent())
				if(relationshipCap.resolve().get().GetCooldownTicks(npcID) > 0)
					return false;
		}

		// Check for other matching NPCs in the nearby loaded chunks
		Vector3d pos = context.Shooter().GetShootOrigin().Position();
		double checkRange = 400D;
		for(ShopkeeperEntity nearbyShopkeeper : level.getEntitiesOfClass(
			ShopkeeperEntity.class,
			new AABB(pos.x - checkRange, pos.y - checkRange, pos.z - checkRange,
				pos.x + checkRange, pos.y + checkRange, pos.z + checkRange)))
		{
			if(nearbyShopkeeper.GetDef().Location.equals(npcID))
				return false;
		}

		return true;
	}

	public ResourceLocation EntityType(ActionGroupContext context)
	{
		return new ResourceLocation(context.ModifyString(ModifierDefinition.STAT_ENTITY_ID, ""));
	}
}
