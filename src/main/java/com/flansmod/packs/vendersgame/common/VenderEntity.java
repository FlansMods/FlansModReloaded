package com.flansmod.packs.vendersgame.common;

import com.flansmod.common.FlansMod;
import com.flansmod.common.entity.ShopkeeperEntity;
import com.flansmod.common.types.npc.NpcDefinition;
import com.flansmod.packs.vendersgame.VendersGameMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

public class VenderEntity extends ShopkeeperEntity
{
	public VenderEntity(EntityType<? extends ShopkeeperEntity> entityType, Level level)
	{
		super(entityType, level);
	}

	public static final ResourceLocation VENDER_LOC = new ResourceLocation(VendersGameMod.MODID, "vender");

	@Override
	public NpcDefinition GetDef()
	{
		return FlansMod.NPCS.Get(VENDER_LOC);
	}

	public static AttributeSupplier.Builder createAttributes()
	{
		return Monster.createMonsterAttributes()
			.add(Attributes.MAX_HEALTH, 40.0D)
			.add(Attributes.MOVEMENT_SPEED, (double)0.3F)
			.add(Attributes.ATTACK_DAMAGE, 7.0D)
			.add(Attributes.FOLLOW_RANGE, 64.0D);
	}

	@Override
	protected void registerGoals()
	{
		super.registerGoals();
	}
}
