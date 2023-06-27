package com.flansmod.common.types.guns;

import com.flansmod.common.actions.EActionSet;
import com.flansmod.common.item.AttachmentItem;
import com.flansmod.common.item.BulletItem;
import com.flansmod.common.item.GunItem;
import com.flansmod.common.types.attachments.AttachmentDefinition;
import com.flansmod.common.types.attachments.EAttachmentType;
import com.flansmod.common.types.elements.ActionDefinition;
import com.flansmod.common.types.elements.ModifierDefinition;
import com.flansmod.util.Maths;
import com.flansmod.util.Transform;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class GunContext
{
	public static final GunContext INVALID = new GunContext(null, Transform.Identity(), null, null);

	// Source variables
	public ItemStack stack;
	public Entity shootFrom;
	public int subEntityIndex;
	public Transform shootOrigin;
	public Entity owner;

	// Cached variables?
	private CachedGunStats cachedPrimaryAction = null;
	private CachedGunStats cachedSecondaryAction = null;

	public DamageSource CreateDamageSource()
	{
		return new IndirectEntityDamageSource(
			"gun",
			shootFrom,
			owner
		);
	}

	public boolean IsValidForRender() { return stack != null; }
	public boolean IsValidForUse()
	{
		return stack != null && shootFrom != null;
	}
	public InteractionHand GetHand() { return subEntityIndex == 1 ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND; }
	public void SetHand(InteractionHand hand) { subEntityIndex = hand == InteractionHand.MAIN_HAND ? 0 : 1; }

	public static GunContext TryCreateFromItemStack(ItemStack stack)
	{
		return new GunContext(null, Transform.Identity(), null, stack);
	}

	@Nonnull
	public static GunContext TryCreateFromEntity(Entity entity, InteractionHand hand)
	{
		if(entity instanceof LivingEntity living)
			return CreateFromLiving(living, hand);
		else // if(IFlanGunWielder)
			return GunContext.INVALID;
	}

	@Nonnull
	public static GunContext CreateFromLiving(LivingEntity entity, InteractionHand hand)
	{
		if(entity.getItemInHand(hand).getItem() instanceof GunItem gun)
		{
			return new GunContext(entity, CreateLookTransform(entity), entity, entity.getItemInHand(hand));
		}
		return GunContext.INVALID;
	}

	@Nonnull
	public static GunContext CreateFromPlayer(Player player, InteractionHand hand)
	{
		if(player.getItemInHand(hand).getItem() instanceof GunItem gun)
		{
			return new GunContext(player, CreateLookTransform(player), player, player.getItemInHand(hand));
		}
		return GunContext.INVALID;
	}

	@Nonnull
	public static GunContext CreateFromContext(UseOnContext useOnContext)
	{
		if(useOnContext.getItemInHand().getItem() instanceof GunItem gun)
		{
			return new GunContext(
				useOnContext.getPlayer(),
				CreateLookTransform(useOnContext.getPlayer()),
				useOnContext.getPlayer(),
				useOnContext.getItemInHand());
		}
		return GunContext.INVALID;
	}

	private GunContext(Entity shootFrom, Transform shootOrigin, Entity owner, ItemStack stack)
	{
		this.shootFrom = shootFrom;
		this.shootOrigin = shootOrigin;
		this.owner = owner;
		this.stack = stack;
	}

	@Nonnull
	private static Transform CreateLookTransform(Entity entity)
	{
		if(entity == null)
			return Transform.Identity();
		Transform transform = new Transform(entity.getEyePosition());
		transform = transform.RotateLocalEuler(entity.getXRot(), entity.getYRot(), 0.0f);
		return transform;
	}

	/// Public accessors of accumulated stats
	// Make sure we process attachments when getting stats and results about the gun
	public GunDefinition GunDef()
	{
		if(stack != null && stack.getItem() instanceof GunItem gunItem)
			return gunItem.Def();
		return GunDefinition.INVALID;
	}

	public Inventory GetAttachedInventory()
	{
		if(shootFrom != null)
		{
			if(shootFrom instanceof Player player)
			{
				return player.getInventory();
			}
			//else if(shootFrom instanceof )
		}
		return null;
	}

	public boolean HasItemInOtherHand()
	{
		if(shootFrom instanceof Player player)
		{
			if(GetHand() == InteractionHand.MAIN_HAND)
				return !player.getItemInHand(InteractionHand.OFF_HAND).isEmpty();
			else
				return !player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty();
		}
		return false;
	}

	public GunContext GetGunInOtherHand()
	{
		if(shootFrom instanceof Player player)
		{
			if(GetHand() == InteractionHand.MAIN_HAND)
				return CreateFromPlayer(player, InteractionHand.OFF_HAND);
			else
				return CreateFromPlayer(player, InteractionHand.MAIN_HAND);
		}
		return GunContext.INVALID;
	}

	public ItemStack GetGunStack()
	{
		return stack;
	}

	public ItemStack GetBulletStack(int index)
	{
		CompoundTag ammoTags = GetOrCreateTags("ammo");
		String key = Integer.toString(index);
		if(!ammoTags.contains(key))
		{
			CompoundTag itemTags = new CompoundTag();
			ItemStack empty = ItemStack.EMPTY.copy();
			empty.save(itemTags);
			ammoTags.put(key, itemTags);
			return empty;
		}

		return ItemStack.of(ammoTags.getCompound(key));
	}

	public void SetBulletStack(int index, ItemStack stack)
	{
		if(stack.isEmpty())
			stack = ItemStack.EMPTY;
		CompoundTag ammoTags = GetOrCreateTags("ammo");
		String key = Integer.toString(index);
		CompoundTag itemTags = new CompoundTag();
		stack.save(itemTags);
		ammoTags.put(key, itemTags);
	}

	public boolean CanPerformReload()
	{
		for(int i = 0; i < GunDef().numBullets; i++)
		{
			if(GetBulletStack(i).isEmpty())
				return true;
		}
		return false;
	}

	public int GetNextBulletSlotToLoad(EActionSet actionSet)
	{
		for(int i = 0; i < GunDef().numBullets; i++)
		{
			if(GetBulletStack(i).isEmpty())
				return i;
		}
		return Inventory.NOT_FOUND_INDEX;
	}

	public void LoadOne(EActionSet actionSet, int bulletSlot)
	{
		if(bulletSlot < 0 || bulletSlot >= GunDef().numBullets)
			return;

		ActionDefinition shootActionDef = GetShootActionDefinition(actionSet);
		Inventory attachedInventory = GetAttachedInventory();
		if(shootActionDef != null && attachedInventory != null)
		{
			int slot = FindSlotWithMatchingAmmo(shootActionDef, attachedInventory);
			if(slot != Inventory.NOT_FOUND_INDEX)
			{
				ItemStack stackToLoad = attachedInventory.removeItem(slot, 1);
				SetBulletStack(bulletSlot, stackToLoad);
			}
		}
	}

	public int FindSlotWithMatchingAmmo(ActionDefinition shootActionDef, Inventory inventory)
	{
		for(int i = 0; i < inventory.getContainerSize(); i++)
		{
			ItemStack stack = inventory.getItem(i);
			if(stack.isEmpty())
				continue;
			if(stack.getItem() instanceof BulletItem bullet)
			{
				if(shootActionDef.shootStats[0].GetMatchingBullets().contains(bullet.Def()))
				{
					return i;
				}
			}
		}
		return Inventory.NOT_FOUND_INDEX;
	}

	public int GetCurrentChamber()
	{
		return stack.getOrCreateTag().contains("chamber") ? stack.getTag().getInt("chamber") : 0;
	}

	public void SetCurrentChamber(int chamber)
	{
		stack.getOrCreateTag().putInt("chamber", chamber);
	}

	public void AdvanceChamber()
	{
		int chamber = GetCurrentChamber();
		chamber++;
		if(chamber >= GunDef().numBullets)
			chamber = 0;
		SetCurrentChamber(chamber);
	}

	public AttachmentDefinition GetAttachmentDefinition(EAttachmentType attachType)
	{
		CompoundTag attachmentTags = GetOrCreateTags("attachments");
		if(attachmentTags.contains(attachType.toString()))
		{
			CompoundTag forThisType = attachmentTags.getCompound(attachType.toString());
			ItemStack stack = ItemStack.of(forThisType);
			if(stack.getItem() instanceof AttachmentItem attachmentItem)
			{
				return attachmentItem.Def();
			}
		}
		return AttachmentDefinition.INVALID;
	}

	public ItemStack GetAttachmentStack(EAttachmentType attachType)
	{
		CompoundTag attachmentTags = GetOrCreateTags("attachments");
		if(attachmentTags.contains(attachType.toString()))
		{
			CompoundTag forThisType = attachmentTags.getCompound(attachType.toString());
			return ItemStack.of(forThisType);
		}
		return ItemStack.EMPTY.copy();
	}

	public List<ItemStack> GetAttachmentStacks()
	{
		List<ItemStack> stacks = new ArrayList<>();
		CompoundTag attachmentTags = GetOrCreateTags("attachments");
		for(String key : attachmentTags.getAllKeys())
		{
			CompoundTag attachmentStackTags = attachmentTags.getCompound(key);
			stacks.add(ItemStack.of(attachmentStackTags));
		}
		return stacks;
	}

	public String GetPaintjobName()
	{
		if(stack.hasTag())
		{
			return stack.getTag().getString("paint");
		}
		return "default";
	}

	public void SetPaintjobName(String paint)
	{
		stack.getOrCreateTag().putString("paint", paint);
	}

	public List<ModifierDefinition> GetApplicableModifiers(String stat, EActionSet actionSet)
	{
		List<ModifierDefinition> applicableModifiers = new ArrayList<>();
		List<ItemStack> attachmentStacks = GetAttachmentStacks();
		for(ItemStack attachmentStack : attachmentStacks)
		{
			if(attachmentStack.getItem() instanceof AttachmentItem attachmentItem)
			{
				AttachmentDefinition attachDef = attachmentItem.Def();
				for(ModifierDefinition modifierDef : attachDef.modifiers)
				{
					if(modifierDef.AppliesTo(stat, actionSet))
					{
						applicableModifiers.add(modifierDef);
					}
				}
			}
		}
		return applicableModifiers;
	}

	public Map<String, List<ModifierDefinition>> GetAllApplicableModifiers(EActionSet actionSet)
	{
		Map<String, List<ModifierDefinition>> results = new IdentityHashMap<>();
		List<ItemStack> attachmentStacks = GetAttachmentStacks();
		for(ItemStack attachmentStack : attachmentStacks)
		{
			if(attachmentStack.getItem() instanceof AttachmentItem attachmentItem)
			{
				AttachmentDefinition attachDef = attachmentItem.Def();
				for(ModifierDefinition modifierDef : attachDef.modifiers)
				{
					if(modifierDef.AppliesTo(actionSet))
					{
						List<ModifierDefinition> modList = results.get(modifierDef.Stat);
						if(modList == null)
						{
							modList = new ArrayList<>();
							results.put(modifierDef.Stat, modList);
						}
						modList.add(modifierDef);
					}
				}
			}
		}
		return results;
	}

	public boolean HasAction(EActionSet actionSet) { return GetShootActionDefinition(actionSet) != null; }
	@Nonnull
	public ActionDefinition[] GetActionDefinitions(EActionSet actionSet)
	{
		if(GunDef() == null)
			return new ActionDefinition[0];

		return GunDef().GetActions(actionSet);
	}
	@Nullable
	public ActionDefinition GetShootActionDefinition(EActionSet actionSet)
	{
		if(GunDef() == null)
			return null;

		// TODO: Check attachments for changes in action
		// e.g. An underslung grenade launcher attachment

		for(ActionDefinition def : GunDef().GetActions(actionSet))
			if(def.actionType == EActionType.Shoot)
				return def;

		return null;
	}
	@Nonnull
	public ActionDefinition[] GetLookAtActions()
	{
		if(GunDef() == null)
			return new ActionDefinition[0];

		return GunDef().lookAtActions;
	}
	@Nonnull
	public ActionDefinition[] GetReloadActions(EReloadStage stage)
	{
		if(GunDef() == null)
			return new ActionDefinition[0];
		return GunDef().reload.GetReloadActions(stage);
	}


	public CachedGunStats GetStatBlock(EActionSet actionSet) { return GetStatCache(actionSet); }
	public float VerticalRecoil(EActionSet actionSet) { return GetStatCache(actionSet).VerticalRecoil();}
	public float HorizontalRecoil(EActionSet actionSet) { return GetStatCache(actionSet).HorizontalRecoil();}
	public float Spread(EActionSet actionSet) { return GetStatCache(actionSet).Spread();}
	public float Speed(EActionSet actionSet) { return GetStatCache(actionSet).Speed();}
	public int Count(EActionSet actionSet) { return GetStatCache(actionSet).Count();}
	public float TimeToNextShot(EActionSet actionSet) { return GetStatCache(actionSet).TimeToNextShot();}
	public float PenetrationPower(EActionSet actionSet) { return GetStatCache(actionSet).PenetrationPower();}
	public float BaseDamage(EActionSet actionSet) { return GetStatCache(actionSet).BaseDamage();}
	public float Knockback(EActionSet actionSet) { return GetStatCache(actionSet).Knockback();}
	public float MultiplierVsPlayers(EActionSet actionSet) { return GetStatCache(actionSet).MultiplierVsPlayers();}
	public float MultiplierVsVehicles(EActionSet actionSet) { return GetStatCache(actionSet).MultiplierVsVehicles();}
	public float SplashDamageRadius(EActionSet actionSet) { return GetStatCache(actionSet).SplashDamageRadius();}
	public float SplashDamageFalloff(EActionSet actionSet) { return GetStatCache(actionSet).SplashDamageFalloff();}
	public float SetFireToTarget(EActionSet actionSet) { return GetStatCache(actionSet).SetFireToTarget();}
	public float FireSpreadRadius(EActionSet actionSet) { return GetStatCache(actionSet).FireSpreadRadius();}
	public float FireSpreadAmount(EActionSet actionSet) { return GetStatCache(actionSet).FireSpreadAmount();}
	public ESpreadPattern SpreadPattern(EActionSet actionSet) { return GetStatCache(actionSet).SpreadPattern();}


	private CompoundTag GetOrCreateTags(String key)
	{
		CompoundTag root = stack.getOrCreateTag();
		if(!root.contains(key))
		{
			root.put(key, new CompoundTag());
		}
		return root.getCompound(key);
	}

	@Nonnull
	private CachedGunStats GetStatCache(EActionSet actionSet)
	{
		switch(actionSet)
		{
			case PRIMARY:
				if(cachedPrimaryAction == null)
					cachedPrimaryAction = CalculateStats(actionSet);
				return cachedPrimaryAction;
			case SECONDARY:
				if(cachedSecondaryAction == null)
					cachedSecondaryAction = CalculateStats(actionSet);
				return cachedSecondaryAction;
			default:
				return new CachedGunStats();
		}
	}

	private CachedGunStats CalculateStats(EActionSet actionSet)
	{
		CachedGunStats stats = new CachedGunStats();

		ActionDefinition shootAction = GetShootActionDefinition(actionSet);
		if(shootAction != null)
		{
			stats.InitializeFrom(shootAction.shootStats[0]);
		}

		var modifierMap = GetAllApplicableModifiers(actionSet);
		for(var kvp : modifierMap.entrySet())
		{
			stats.ApplyModifiers(kvp.getKey(), kvp.getValue());
		}

		return stats;
	}
}
