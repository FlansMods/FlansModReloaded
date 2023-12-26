package com.flansmod.common.entity;

import com.flansmod.common.types.elements.EDamageSourceType;
import com.flansmod.common.types.npc.NpcDefinition;
import com.flansmod.common.types.npc.elements.ENpcActionType;
import com.flansmod.common.types.npc.elements.EVoiceLineType;
import com.flansmod.common.types.npc.elements.MerchantOfferDefinition;
import com.flansmod.common.types.npc.elements.VoiceLineDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.npc.*;
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
	private static final EntityDataAccessor<VillagerData> DATA_VILLAGER_DATA = SynchedEntityData.defineId(ShopkeeperEntity.class, EntityDataSerializers.VILLAGER_DATA);

	public abstract NpcDefinition GetDef();
	@Nullable
	private Player TradingWithPlayer;
	public List<Player> PlayersInRange = new ArrayList<>();
	@Nullable
	private MerchantOffers Offers;
	@Nullable
	private BlockPos WanderAroundBlock;
	private final NonNullList<ItemStack> HandItems = NonNullList.withSize(2, ItemStack.EMPTY);
	private final NonNullList<ItemStack> ArmourItems = NonNullList.withSize(4, ItemStack.EMPTY);

	public ShopkeeperEntity(EntityType<? extends ShopkeeperEntity> entityType, Level level)
	{
		super(entityType, level);

		NpcDefinition def = GetDef();
	}



	// -----------------------------------------------------------------------------------
	// NBT, net sync etc.
	// -----------------------------------------------------------------------------------
	@Override
	protected void defineSynchedData()
	{
		super.defineSynchedData();
		entityData.define(DATA_VILLAGER_DATA, new VillagerData(VillagerType.PLAINS, VillagerProfession.NONE, 1));
	}
	@Override
	public void addAdditionalSaveData(@Nonnull CompoundTag tags)
	{
		super.addAdditionalSaveData(tags);
		MerchantOffers merchantoffers = getOffers();
		if (!merchantoffers.isEmpty())
			tags.put("Offers", merchantoffers.createTag());
		if (WanderAroundBlock != null)
			tags.put("WanderTarget", NbtUtils.writeBlockPos(WanderAroundBlock));
	}
	@Override
	public void readAdditionalSaveData(@Nonnull CompoundTag tags)
	{
		super.readAdditionalSaveData(tags);
		if (tags.contains("Offers", 10))
			Offers = new MerchantOffers(tags.getCompound("Offers"));
		if (tags.contains("WanderTarget"))
			WanderAroundBlock = NbtUtils.readBlockPos(tags.getCompound("WanderTarget"));
	}

	// -----------------------------------------------------------------------------------
	// Inventory
	// -----------------------------------------------------------------------------------
	@Override
	@Nonnull
	public Iterable<ItemStack> getArmorSlots() { return ArmourItems; }
	@Override
	@Nonnull
	public ItemStack getItemBySlot(@Nonnull EquipmentSlot slot)
	{
		if(slot.isArmor())
			return ArmourItems.get(slot.getIndex());
		else return HandItems.get(slot.getIndex());
	}
	@Override
	public void setItemSlot(@Nonnull EquipmentSlot slot, @Nonnull ItemStack stack)
	{
		if(slot.isArmor())
			ArmourItems.set(slot.getIndex(), stack);
		else HandItems.set(slot.getIndex(), stack);
	}
	@Override
	@Nonnull
	public HumanoidArm getMainArm()
	{
		NpcDefinition def = GetDef();
		return def != null && !def.isRightHanded ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
	}

	// -----------------------------------------------------------------------------------
	// Trade offers
	// -----------------------------------------------------------------------------------
	@Override
	public void setTradingPlayer(@Nullable Player player) { TradingWithPlayer = player; }
	@Nullable
	@Override
	public Player getTradingPlayer() { return TradingWithPlayer; }
	public boolean IsTrading() {
		return TradingWithPlayer != null;
	}
	@Override
	@Nonnull
	public MerchantOffers getOffers()
	{
		RefreshTrades();
		return Offers;
	}
	@Override
	public void overrideOffers(@Nonnull MerchantOffers offers) { Offers = offers; }
	@Override
	public void notifyTrade(@Nonnull MerchantOffer offer)
	{
		int inputCount = offer.getCostA().getCount() + offer.getCostB().getCount();
		int resultCount = offer.getResult().getCount();

		// Is it a buy or a sell? Well they are kinda the same, so best guess is lots of items for one item is a buy
		if (inputCount >= resultCount)
		{
			TryPlayVoiceLine(EVoiceLineType.SoldToPlayer);
		}
		else
		{
			TryPlayVoiceLine(EVoiceLineType.BoughtFromPlayer);
		}
	}
	@Override
	public void notifyTradeUpdated(@Nonnull ItemStack stack)
	{

	}
	private void RefreshTrades()
	{
		if(Offers == null)
			Offers = new MerchantOffers();

		NpcDefinition def = GetDef();
		if(def.IsValid() && def.maxMerchantLevel > 0)
		{
			List<MerchantOfferDefinition> validOffers = new ArrayList<>();
			float totalWeight = 0.0f;
			for(MerchantOfferDefinition offerDef : def.offers)
			{
				validOffers.add(offerDef);
				totalWeight += offerDef.weighting;
			}

			while(Offers.size() < def.maxOffersToGive && validOffers.size() > 0)
			{
				float pick = random.nextFloat() * totalWeight;
				for(int i = 0; i < validOffers.size(); i++)
				{
					MerchantOfferDefinition offerDef = validOffers.get(i);
					if(pick <= offerDef.weighting)
					{
						ItemStack stackA = offerDef.inputs.length >= 1 ? offerDef.inputs[0].CreateStack() : ItemStack.EMPTY;
						ItemStack stackB = offerDef.inputs.length >= 2 ? offerDef.inputs[1].CreateStack() : ItemStack.EMPTY;

						Offers.add(new MerchantOffer(
							stackA,
							stackB,
							offerDef.output.CreateStack(),
							0,
							offerDef.maxUses,
							offerDef.merchantXP,
							offerDef.priceMultiplier,
							offerDef.demand));
						validOffers.remove(i);
						totalWeight -= offerDef.weighting;
						break;
					}
					else pick -= offerDef.weighting;
				}
			}
		}
	}


	// -----------------------------------------------------------------------------------
	// Misc stuff
	// -----------------------------------------------------------------------------------
	@Override
	public int getVillagerXp() { return 0; }
	@Override
	public void overrideXp(int xp) {}
	@Override
	public boolean showProgressBar() { return false; }
	@Override
	public boolean removeWhenFarAway(double distance) { return false; }
	@Override
	public boolean isClientSide() { return level().isClientSide; }

	// -----------------------------------------------------------------------------------
	// SFX and Voice Lines
	// -----------------------------------------------------------------------------------
	@Nullable
	private VoiceLineDefinition PickVoiceLine(EVoiceLineType voiceLineType)
	{
		List<VoiceLineDefinition> validVoiceLines = new ArrayList<>();
		NpcDefinition def = GetDef();
		if(def.IsValid())
			for(VoiceLineDefinition voiceLineDefinition : def.voiceLines)
				if(voiceLineDefinition.type == voiceLineType)
					validVoiceLines.add(voiceLineDefinition);
		if(validVoiceLines.size() > 0)
			return validVoiceLines.get(random.nextInt(validVoiceLines.size()));
		return null;
	}
	private void TryPlayVoiceLine(@Nonnull EVoiceLineType voiceLineType)
	{
		TryPlayVoiceLineWithFallback(voiceLineType, null);
	}
	private void TryPlayVoiceLineWithFallback(@Nonnull EVoiceLineType voiceLineType, @Nullable SoundEvent fallbackSound)
	{
		VoiceLineDefinition voiceLine = PickVoiceLine(voiceLineType);
		if(voiceLine != null)
		{
			// Play the voice line
			PlayVoiceLine(voiceLine);
		}
		else if(fallbackSound != null)
		{
			playSound(fallbackSound);
		}
	}
	private void PlayVoiceLine(VoiceLineDefinition voiceLine)
	{
		voiceLine.audioClip.PlayUnLODed(level(), position());
		for(Player player : level().getNearbyPlayers(TargetingConditions.forNonCombat(), this, getBoundingBox().inflate(50d)))
		{
			player.sendSystemMessage(Component.translatable(voiceLine.unlocalisedString));
		}
	}

	@Override
	@Nonnull
	public SoundEvent getNotifyTradeSound()
	{
		return SoundEvents.WANDERING_TRADER_TRADE;
	}
	@Override
	public void playAmbientSound()
	{
		//TryPlayVoiceLine(EVoiceLineType.Chat);
	}
	@Override
	protected void playHurtSound(@Nonnull DamageSource source)
	{
		TryPlayVoiceLine(EVoiceLineType.Hurt);
	}
	@Override
	@Nullable
	protected SoundEvent getDeathSound() { return null; }

	// -----------------------------------------------------------------------------------
	// Health, Death, Despawn etc
	// -----------------------------------------------------------------------------------
	@Override
	public void die(@Nonnull DamageSource source)
	{
		Entity entity = source.getEntity();
		if (entity instanceof Player playerKilledMe)
			SetRelationshipTo(playerKilledMe, ENpcRelationship.Hostile);
		TryPlayVoiceLine(EVoiceLineType.Die);

		super.die(source);
	}
	@Override
	public boolean hurt(@Nonnull DamageSource source, float amount)
	{
		Entity entity = source.getEntity();
		TryPlayVoiceLine(EVoiceLineType.Hurt);
		if (entity instanceof Player playerHurtMe)
		{
			SetRelationshipTo(playerHurtMe, ENpcRelationship.Hostile);
		}
		if(GetDef().Can(ENpcActionType.Hostile_TeleportAway))
		{
			TeleportAway();
		}

		return super.hurt(source, amount);
	}
	@Override
	public boolean isInvulnerableTo(@Nonnull DamageSource source)
	{
		NpcDefinition npcDef = GetDef();
		if(npcDef != null)
		{
			EDamageSourceType sourceType = EDamageSourceType.MatchType(source);
			return npcDef.IsInvulnerableTo(sourceType);
		}
		return false;
	}
	public void TeleportAway()
	{
		TryPlayVoiceLine(EVoiceLineType.Goodbye);
		for(Player player : level().getEntitiesOfClass(Player.class, getBoundingBox().inflate(50d)))
		{
			OnLeavePlayer(player);
		}
		remove(RemovalReason.CHANGED_DIMENSION);
	}



	// -----------------------------------------------------------------------------------
	// Player interactions
	// -----------------------------------------------------------------------------------
	@Override
	@Nonnull
	public InteractionResult mobInteract(@Nonnull Player player, @Nonnull InteractionHand hand)
	{
		if (isAlive()
			&& !IsTrading()
			&& !isSleeping()
			&& !player.isSecondaryUseActive())
		{
			boolean hasNoOffers = getOffers().isEmpty();
			if (hand == InteractionHand.MAIN_HAND)
			{
				player.awardStat(Stats.TALKED_TO_VILLAGER);
			}
			if (hasNoOffers)
			{
				return InteractionResult.sidedSuccess(level().isClientSide);
			}
			else
			{
				if (!level().isClientSide)
				{
					setTradingPlayer(player);
					openTradingScreen(player, getDisplayName(), GetLevelForPlayer(player));
				}

				return InteractionResult.sidedSuccess(this.level().isClientSide);
			}
		}
		else return super.mobInteract(player, hand);
	}

	// -------------------------------------------------------------------------------------
	// Relationships
	// -------------------------------------------------------------------------------------
	public void OnLeavePlayer(Player player)
	{
		ENpcRelationship relationship = GetRelationshipTo(player);
		switch(relationship)
		{
			case Hostile -> {
				SetCooldownForPlayer(player, GetDef().CooldownTicks(false));
				SetRelationshipTo(player, ENpcRelationship.PreviouslyMetHostile);
			}
			case Friendly, NotMet -> {
				SetCooldownForPlayer(player, GetDef().CooldownTicks(true));
				SetRelationshipTo(player, ENpcRelationship.PreviouslyMetFriendly);
			}
		}
	}

	public int GetLevelForPlayer(Player player)
	{
		LazyOptional<INpcRelationshipsCapability> relationshipsCap = player.getCapability(NpcRelationshipsCapability.INSTANCE, Direction.UP);
		if(relationshipsCap.isPresent() && relationshipsCap.resolve().isPresent())
			return relationshipsCap.resolve().get().GetLevel(GetDef().Location);
		return 0;
	}

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

	// -------------------------------------------------------------------------------------
	// AI Stuff
	// -------------------------------------------------------------------------------------
	public void SetWanderAround(@Nullable BlockPos pos) { WanderAroundBlock = pos; }
	@Nullable
	public BlockPos GetWanderAround() { return WanderAroundBlock; }
	@Override
	protected void registerGoals()
	{
		NpcDefinition def = GetDef();

		//goalSelector.addGoal(0,)

		// Hello and goodbye actions

		goalSelector.addGoal(0, new Goodbye(this, EVoiceLineType.Goodbye));
		goalSelector.addGoal(1, new Hello(this, EVoiceLineType.Hello));
		goalSelector.addGoal(1, new Hello(this, EVoiceLineType.HelloHappy));
		goalSelector.addGoal(1, new Hello(this, EVoiceLineType.HelloUnhappy));

		// Hostile response actions
		if(def.Can(ENpcActionType.Hostile_TeleportAway))
			goalSelector.addGoal(2, new TeleportAway(this));

		if(def.Can(ENpcActionType.Hostile_Retaliate))
			goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0d, false));
		if(def.Can(ENpcActionType.Neutral_Wander))
			goalSelector.addGoal(7, new RandomStrollGoal(this, 1.0d));

		goalSelector.addGoal(8, new RandomLookAroundGoal(this));
		if(def.Can(ENpcActionType.Neutral_LookAtPlayer))
			goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 12.0f));

//if(def.Can(ENpcActionType.Neutral_LookAtAnimals))

		if(def.Can(ENpcActionType.Neutral_Idle))
			goalSelector.addGoal(10, new Talk(this, EVoiceLineType.Chat));

		targetSelector.addGoal(1, new HurtByTargetGoal(this));
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

	protected static final double CHAT_RADIUS = 10d;

	private static class Hello extends Goal
	{
		private final EVoiceLineType VoiceLineType;
		private final ShopkeeperEntity Shopkeeper;
		private Player TalkToTarget;
		public Hello(ShopkeeperEntity shopkeeper, EVoiceLineType type)
		{
			Shopkeeper = shopkeeper;
			VoiceLineType = type;
		}
		@Override
		public boolean canUse()
		{
			for(Player player : Shopkeeper.level().getNearbyPlayers(TargetingConditions.forNonCombat(), Shopkeeper, Shopkeeper.getBoundingBox().inflate(CHAT_RADIUS)))
			{
				if(!Shopkeeper.PlayersInRange.contains(player))
				{
					ENpcRelationship relationship = Shopkeeper.GetRelationshipTo(player);

					TalkToTarget = player;
					switch(relationship)
					{
						case NotMet -> { return VoiceLineType == EVoiceLineType.Hello; }
						case Friendly -> { return VoiceLineType == EVoiceLineType.HelloHappy; }
						case Hostile -> { return VoiceLineType == EVoiceLineType.HelloUnhappy; }
					}
				}
			}
			return false;
		}
		@Override
		public void start()
		{
			Shopkeeper.getLookControl().setLookAt(TalkToTarget);
			Shopkeeper.TryPlayVoiceLine(VoiceLineType);
			Shopkeeper.SetRelationshipTo(TalkToTarget, ENpcRelationship.Friendly);
		}
	}

	private static class Goodbye extends Goal
	{
		private final EVoiceLineType VoiceLineType;
		private final ShopkeeperEntity Shopkeeper;
		private Player TalkToTarget;
		public Goodbye(ShopkeeperEntity shopkeeper, EVoiceLineType type)
		{
			Shopkeeper = shopkeeper;
			VoiceLineType = type;
		}
		@Override
		public boolean canUse()
		{
			for(Player previous : Shopkeeper.PlayersInRange)
			{
				if(previous.isDeadOrDying() || previous.isRemoved() || previous.distanceToSqr(Shopkeeper) > CHAT_RADIUS * CHAT_RADIUS)
				{
					if(Shopkeeper.GetRelationshipTo(previous) == ENpcRelationship.Friendly)
					{
						TalkToTarget = previous;
						return true;
					}
				}
			}
			return false;
		}
		@Override
		public void start()
		{
			Shopkeeper.getLookControl().setLookAt(TalkToTarget);
			ENpcRelationship relationship = Shopkeeper.GetRelationshipTo(TalkToTarget);
			switch(relationship)
			{
				case Friendly -> Shopkeeper.SetRelationshipTo(TalkToTarget, ENpcRelationship.PreviouslyMetFriendly);
				case NotMet -> Shopkeeper.SetRelationshipTo(TalkToTarget, ENpcRelationship.NotMet);
				case Hostile -> Shopkeeper.SetRelationshipTo(TalkToTarget, ENpcRelationship.PreviouslyMetHostile);
			}
			if(relationship == ENpcRelationship.Friendly)
			{
				Shopkeeper.TryPlayVoiceLine(VoiceLineType);
			}
			Shopkeeper.PlayersInRange.remove(TalkToTarget);
		}
	}

	private static class Talk extends Goal
	{
		private final EVoiceLineType VoiceLineType;
		private final ShopkeeperEntity Shopkeeper;
		private Player TalkToTarget;
		private int Cooldown = 0;

		public Talk(ShopkeeperEntity shopkeeper, EVoiceLineType type)
		{
			Shopkeeper = shopkeeper;
			VoiceLineType = type;
		}

		@Override
		public boolean canUse()
		{
			TalkToTarget = Shopkeeper.level().getNearestPlayer(Shopkeeper, CHAT_RADIUS);
			if (TalkToTarget != null)
			{
				ENpcRelationship relationship = Shopkeeper.GetRelationshipTo(TalkToTarget);
				switch (VoiceLineType)
				{
					case Chat, SoldToPlayer, BoughtFromPlayer -> {
						return relationship == ENpcRelationship.Friendly;
					}
				}
			}
			return false;
		}

		@Override
		public boolean isInterruptable() {
			return true;
		}
		@Override
		public boolean canContinueToUse() {
			return Cooldown > 0;
		}
		@Override
		public void start()
		{
			Cooldown = 200; // 10s delay
			Shopkeeper.TryPlayVoiceLine(VoiceLineType);
		}

		@Override
		public void tick()
		{
			if(TalkToTarget != null)
				Shopkeeper.getLookControl().setLookAt(TalkToTarget);

			Cooldown--;
		}
	}
}
