package com.flansmod.common.entity;

import com.flansmod.common.types.npc.NpcDefinition;
import com.flansmod.common.types.npc.NpcDefinitions;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;

public abstract class ShopkeeperEntity extends PathfinderMob implements Npc, Merchant
{
	public abstract NpcDefinition GetDef();
	@Nullable
	private Player TradingWithPlayer;
	@Nullable
	private MerchantOffers Offers;

	public ShopkeeperEntity(EntityType<? extends ShopkeeperEntity> entityType, Level level)
	{
		super(entityType, level);

		NpcDefinition def = GetDef();
	}

	@Override
	protected void registerGoals()
	{
		//goalSelector.addGoal(0,)

		goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0d, false));
		goalSelector.addGoal(7, new RandomStrollGoal(this, 1.0d));
		goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 12.0f));
		goalSelector.addGoal(8, new RandomLookAroundGoal(this));

		targetSelector.addGoal(1, new HurtByTargetGoal(this));
	}

	@Override
	@Nonnull
	public Iterable<ItemStack> getArmorSlots()
	{
		return new ArrayList<ItemStack>();
	}

	@Override
	@Nonnull
	public ItemStack getItemBySlot(EquipmentSlot slot)
	{
		return ItemStack.EMPTY;
	}

	@Override
	public void setItemSlot(EquipmentSlot slot, ItemStack stack)
	{

	}

	@Override
	@Nonnull
	public HumanoidArm getMainArm()
	{
		return HumanoidArm.RIGHT;
	}

	@Override
	public void setTradingPlayer(@Nullable Player player) { TradingWithPlayer = player; }
	@Nullable
	@Override
	public Player getTradingPlayer() { return TradingWithPlayer; }
	@Override
	@Nonnull
	public MerchantOffers getOffers()
	{
		if(Offers == null)
		{
			Offers = new MerchantOffers();
			RefreshTrades();
		}
		return Offers;
	}
	@Override
	public void overrideOffers(@Nonnull MerchantOffers offers) { Offers = offers; }

	private void RefreshTrades()
	{
		NpcDefinition def = GetDef();
		if(def.IsValid() && def.maxMerchantLevel > 0)
		{

		}
	}

	@Override
	public void notifyTrade(MerchantOffer offer)
	{

	}

	@Override
	public void notifyTradeUpdated(ItemStack p_45308_)
	{

	}

	@Override
	public int getVillagerXp()
	{
		return 0;
	}

	@Override
	public void overrideXp(int p_45309_)
	{

	}

	@Override
	public boolean showProgressBar()
	{
		return false;
	}

	@Override
	public SoundEvent getNotifyTradeSound()
	{
		return null;
	}

	@Override
	public boolean isClientSide() { return level.isClientSide; }
}
