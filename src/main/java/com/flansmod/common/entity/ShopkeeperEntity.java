package com.flansmod.common.entity;

import com.flansmod.client.sound.SoundLODManager;
import com.flansmod.common.types.npc.NpcDefinition;
import com.flansmod.common.types.npc.elements.ENpcActionType;
import com.flansmod.common.types.npc.elements.EVoiceLineType;
import com.flansmod.common.types.npc.elements.VoiceLineDefinition;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

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

		// Hello and goodbye actions
		goalSelector.addGoal(0, new Talk(this, EVoiceLineType.Goodbye));
		goalSelector.addGoal(0, new Talk(this, EVoiceLineType.Hello));
		goalSelector.addGoal(0, new Talk(this, EVoiceLineType.HelloHappy));
		goalSelector.addGoal(0, new Talk(this, EVoiceLineType.HelloUnhappy));

		// Hostile response actions
		goalSelector.addGoal(1, new TeleportAway(this));


		goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0d, false));
		goalSelector.addGoal(7, new RandomStrollGoal(this, 1.0d));
		goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 12.0f));
		goalSelector.addGoal(8, new RandomLookAroundGoal(this));


		goalSelector.addGoal(9, new Talk(this, EVoiceLineType.Chat));

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

	public ENpcRelationship GetRelationshipTo(Player player)
	{
		LazyOptional<INpcRelationshipsCapability> relationshipsCap = player.getCapability(NpcRelationshipsCapability.INSTANCE, Direction.UP);
		if(relationshipsCap.isPresent() && relationshipsCap.resolve().isPresent())
			return relationshipsCap.resolve().get().GetRelationship(GetDef().Location);

		return ENpcRelationship.NotMet;
	}

	public void SetRelationshipTo(Player player, ENpcRelationship relationship)
	{
		LazyOptional<INpcRelationshipsCapability> relationshipsCap = player.getCapability(NpcRelationshipsCapability.INSTANCE, Direction.UP);
		relationshipsCap.ifPresent(relationships -> relationships.SetRelationship(GetDef().Location, relationship));
	}

	public void SetCooldownForPlayer(Player player, int cooldown)
	{
		LazyOptional<INpcRelationshipsCapability> relationshipsCap = player.getCapability(NpcRelationshipsCapability.INSTANCE, Direction.UP);
		relationshipsCap.ifPresent(relationships -> relationships.SetCooldownTicks(GetDef().Location, cooldown));
	}

	public void OnLeavePlayer(Player player)
	{
		ENpcRelationship relationship = GetRelationshipTo(player);
		if(relationship == ENpcRelationship.Hostile)
			SetCooldownForPlayer(player, GetDef().CooldownTicks(false));
		else
			SetCooldownForPlayer(player, GetDef().CooldownTicks(true));
	}


	private static class TeleportAway extends Goal
	{
		public final ShopkeeperEntity Shopkeeper;
		public TeleportAway(ShopkeeperEntity shopkeeper)
		{
			Shopkeeper = shopkeeper;
		}

		@Override
		public boolean canUse()
		{
			return Shopkeeper.GetDef().Can(ENpcActionType.Hostile_TeleportAway)
				&& Shopkeeper.getTarget() instanceof Player player
				&& Shopkeeper.GetRelationshipTo(player) == ENpcRelationship.Hostile;
		}

		@Override
		public void start()
		{
			// Play SFX
			Shopkeeper.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
			// Remove entity
			Shopkeeper.remove(RemovalReason.CHANGED_DIMENSION);
		}
	}

	private static class Talk extends Goal
	{
		private final EVoiceLineType VoiceLineType;
		private final List<VoiceLineDefinition> MatchingVoiceLines;
		@Nullable
		private LivingEntity Target;
		public final ShopkeeperEntity Shopkeeper;

		public Talk(ShopkeeperEntity shopkeeper, EVoiceLineType type)
		{
			Shopkeeper = shopkeeper;
			VoiceLineType = type;
			MatchingVoiceLines = new ArrayList<>();
			for(VoiceLineDefinition voiceLine : Shopkeeper.GetDef().voiceLines)
				if(voiceLine.type == VoiceLineType)
					MatchingVoiceLines.add(voiceLine);
		}

		@Override
		public boolean canUse()
		{
			Target = Shopkeeper.getTarget();
			if(Target instanceof Player player)
			{
				ENpcRelationship relationship = Shopkeeper.GetRelationshipTo(player);
				switch (VoiceLineType)
				{
					case Hello -> { return relationship == ENpcRelationship.NotMet; }
					case HelloHappy -> { return relationship == ENpcRelationship.PreviouslyMetFriendly; }
					case HelloUnhappy -> { return relationship == ENpcRelationship.PreviouslyMetHostile; }
					case Chat, SoldToPlayer, BoughtFromPlayer, Goodbye -> { return relationship == ENpcRelationship.Friendly; }
					case Hurt -> { return relationship == ENpcRelationship.Hostile; }
				}
			}
			return false;
		}

		@Override
		public void start()
		{
			Target = Shopkeeper.getTarget();
			if(Target instanceof Player player)
			{
				switch (VoiceLineType)
				{
					case Hello, HelloHappy, HelloUnhappy -> {
						Shopkeeper.SetRelationshipTo(player, ENpcRelationship.Friendly);
					}
				}
			}

			if(MatchingVoiceLines.size() == 1)
			{
				PlayVoiceLine(MatchingVoiceLines.get(0));
			}
			else if(MatchingVoiceLines.size() > 1)
			{
				PlayVoiceLine(MatchingVoiceLines.get(Shopkeeper.random.nextInt(MatchingVoiceLines.size())));
			}
		}

		private void PlayVoiceLine(VoiceLineDefinition voiceLine)
		{
			// Play SFX
			voiceLine.audioClip.PlayUnLODdedOnEntity(Shopkeeper);

			// Add text to chat / popup
			// TODO:
		}

		@Override
		public void tick()
		{
			if(Target != null)
				Shopkeeper.getLookControl().setLookAt(Target);
		}
	}
}
