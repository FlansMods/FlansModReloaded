package com.flansmod.common.gunshots;

import com.flansmod.common.actions.ActionStack;
import com.flansmod.common.actions.EActionInput;
import com.flansmod.common.item.AttachmentItem;
import com.flansmod.common.item.BulletItem;
import com.flansmod.common.item.FlanItem;
import com.flansmod.common.item.GunItem;
import com.flansmod.common.types.attachments.AttachmentDefinition;
import com.flansmod.common.types.attachments.EAttachmentType;
import com.flansmod.common.types.elements.ActionDefinition;
import com.flansmod.common.types.elements.ModifierDefinition;
import com.flansmod.common.types.guns.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public abstract class GunContext
{
	public static final GunContext INVALID = new GunContext()
	{
		@Override
		@Nonnull
		public ItemStack GetItemStack() { return ItemStack.EMPTY; }
		@Override
		public void SetItemStack(ItemStack stack) {}
		@Override
		public DamageSource CreateDamageSource() { return null; }
		@Override
		public ShooterContext GetShooter() { return null; }
		@Override
		public Inventory GetAttachedInventory() { return null; }
		@Override
		public boolean CanPerformTwoHandedAction() { return false; }
		@Override
		public ActionStack GetActionStack() { return null; }
		@Override
		public boolean CanPerformActions() { return false; }
	};

	private static HashMap<String, GunContext> ContextCache = new HashMap<>();

	@Nonnull
	public static GunContext CreateFrom(ItemStack stack)
	{
		if(stack.getItem() instanceof GunItem gun)
			return new GunContextItem(stack);
		return INVALID;
	}

	@Nonnull
	public static GunContext CreateFrom(Inventory inventory, int slot)
	{
		if(inventory.getContainerSize() > slot && inventory.getItem(slot).getItem() instanceof GunItem gun)
			return new GunContextInventoryItem(inventory, slot);
		return INVALID;
	}

	@Nonnull
	public static GunContext CreateFrom(ShooterContext shooter, InteractionHand hand)
	{
		if(shooter.IsValid())
		{
			if(shooter instanceof ShooterContextLiving livingShooter)
			{
				if (livingShooter.Shooter.getItemInHand(hand).getItem() instanceof GunItem)
				{
					if(livingShooter.Entity() instanceof Player player)
						return new GunContextPlayer(livingShooter, hand);
					else
						return new GunContextLiving(livingShooter, hand);
				}
			}
		}
		return INVALID;
	}

	// --------------------------------------------------------------------------
	// Abstractions
	// --------------------------------------------------------------------------
	@Nonnull
	public abstract ItemStack GetItemStack();
	public abstract void SetItemStack(ItemStack stack);
	public abstract DamageSource CreateDamageSource();
	public abstract ShooterContext GetShooter();
	public abstract Container GetAttachedInventory();
	public abstract boolean CanPerformTwoHandedAction();
	// Not necessarily valid to ask for a hand, but in cases where it is valid, use this
	public InteractionHand GetHand() { return null; }

	public boolean IsValid() { return !GetItemStack().isEmpty(); }

	protected GunContext() {}

	/// Public accessors of accumulated stats
	// Make sure we process attachments when getting stats and results about the gun
	@Nonnull
	public GunDefinition GunDef()
	{
		if(GetItemStack().getItem() instanceof GunItem gunItem)
			return gunItem.Def();
		return GunDefinition.INVALID;
	}

	@Nullable
	public Level Level()
	{
		return GetShooter().Level();
	}

	// --------------------------------------------------------------------------
	// ItemStack Operations
	// --------------------------------------------------------------------------
	private CompoundTag GetOrCreateTags(String key)
	{
		CompoundTag root = GetItemStack().getOrCreateTag();
		if(!root.contains(key))
		{
			root.put(key, new CompoundTag());
		}
		return root.getCompound(key);
	}


	// --------------------------------------------------------------------------
	// AMMO
	// --------------------------------------------------------------------------
	public ItemStack GetBulletStack(EActionInput inputType, int index)
	{
		String ammoKey = inputType.GetAmmoTagName();
		if(ammoKey == null)
			return ItemStack.EMPTY;

		CompoundTag ammoTags = GetOrCreateTags(ammoKey);
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

	public int GetNumBulletStacks(EActionInput inputType)
	{
		if(inputType.IsPrimary())
		{
			// TODO: This should be a child of the action?
			return GunDef().numBullets;
			//ActionDefinition actionDef = GetShootActionDefinition(inputType);
			//if (actionDef.IsValid() && actionDef.shootStats.length > 0)
			//{
			//	return actionDef.shootStats[0].bulletCount;
			//}
		}
		else if(inputType.IsSecondary())
		{
			// TODO:
			return 0;
		}


		return 0;
	}

	public void SetBulletStack(EActionInput inputType, int index, ItemStack stack)
	{
		String ammoKey = inputType.GetAmmoTagName();
		if(ammoKey == null)
			return;

		if(stack.isEmpty())
			stack = ItemStack.EMPTY;
		CompoundTag ammoTags = GetOrCreateTags(ammoKey);
		String key = Integer.toString(index);
		CompoundTag itemTags = new CompoundTag();
		stack.save(itemTags);
		ammoTags.put(key, itemTags);
	}

	public boolean CanBeReloaded(EActionInput inputType)
	{
		if(!IsShootAction(inputType))
			return false;

		for(int i = 0; i < GetNumBulletStacks(inputType); i++)
		{
			if(GetBulletStack(inputType, i).isEmpty())
				return true;
		}
		return false;
	}

	public boolean CanPerformReloadFromAttachedInventory(EActionInput inputType)
	{
		if(!CanBeReloaded(inputType))
			return false;

		if(GetAttachedInventory() != null)
		{
			int matchSlot = FindSlotWithMatchingAmmo(inputType, GetAttachedInventory());
			return matchSlot != Inventory.NOT_FOUND_INDEX;
		}

		return false;
	}

	public boolean IsReloadInProgress()
	{
		ActionStack actionStack = GetActionStack();
		return actionStack != null && actionStack.IsReloading();
	}

	public int GetCurrentChamber(EActionInput inputType)
	{
		String chamberKey = inputType.GetChamberTagName();
		if(chamberKey == null)
			return 0;
		return GetItemStack().getOrCreateTag().contains(chamberKey) ? GetItemStack().getTag().getInt(chamberKey) : 0;
	}

	public void SetCurrentChamber(EActionInput inputType, int chamber)
	{
		String chamberKey = inputType.GetChamberTagName();
		if(chamberKey == null)
			return;
		GetItemStack().getOrCreateTag().putInt(chamberKey, chamber);
	}

	public void AdvanceChamber(EActionInput inputType)
	{
		int chamber = GetCurrentChamber(inputType);
		chamber++;
		if(chamber >= GetNumBulletStacks(inputType))
			chamber = 0;
		SetCurrentChamber(inputType, chamber);
	}

	public int GetNextBulletSlotToLoad(EActionInput inputType)
	{
		for(int i = 0; i < GetNumBulletStacks(inputType); i++)
		{
			if(GetBulletStack(inputType, i).isEmpty())
				return i;
		}
		return Inventory.NOT_FOUND_INDEX;
	}

	public void LoadOne(EActionInput inputType, int bulletSlot)
	{
		if(bulletSlot < 0 || bulletSlot >= GetNumBulletStacks(inputType))
			return;

		ActionDefinition shootActionDef = GetShootActionDefinition(inputType);
		Container attachedInventory = GetAttachedInventory();
		if(shootActionDef != null && attachedInventory != null)
		{
			int slot = FindSlotWithMatchingAmmo(inputType, attachedInventory);
			if(slot != Inventory.NOT_FOUND_INDEX)
			{
				ItemStack stackToLoad;
				if(GetShooter().IsCreative())
				{
					stackToLoad = attachedInventory.getItem(slot).copyWithCount(1);
				}
				else
				{
					stackToLoad = attachedInventory.removeItem(slot, 1);
				}

				SetBulletStack(inputType, bulletSlot, stackToLoad);
			}
		}
	}

	public int FindSlotWithMatchingAmmo(EActionInput inputType, Container inventory)
	{
		ActionDefinition shootActionDef = GetShootActionDefinition(inputType);
		if(shootActionDef.IsValid() && shootActionDef.shootStats.length > 0)
		{
			for (int i = 0; i < inventory.getContainerSize(); i++)
			{
				ItemStack stack = inventory.getItem(i);
				if (stack.isEmpty())
					continue;
				if (stack.getItem() instanceof BulletItem bullet)
				{
					if (shootActionDef.shootStats[0].GetMatchingBullets().contains(bullet.Def()))
					{
						return i;
					}
				}
			}
		}
		return Inventory.NOT_FOUND_INDEX;
	}

	// --------------------------------------------------------------------------
	// ACTIONS
	// --------------------------------------------------------------------------
	@Nonnull
	public abstract ActionStack GetActionStack();
	public abstract boolean CanPerformActions();

	public boolean IsShootAction(EActionInput inputType)
	{
		EActionInput actionInputType = inputType.GetActionType();
		if(actionInputType != null)
			for(ActionDefinition def : GunDef().GetActions(actionInputType))
				if(def.actionType == EActionType.Shoot)
					return true;
		return false;
	}
	public boolean HasAction(EActionInput inputType)
	{
		return GetActionDefinitions(inputType).length > 0;
	}
	@Nonnull
	public ActionDefinition[] GetActionDefinitions(EActionInput inputType)
	{
		// First check if any attachments are modifying our actions
		for(ItemStack attachmentStack : GetAttachmentStacks())
		{
			if(attachmentStack.getItem() instanceof AttachmentItem attachmentItem)
			{
				if(attachmentItem.Def().ShouldReplaceAction(inputType))
				{
					return attachmentItem.Def().GetActions(inputType);
				}
			}
		}
		// Then defer to the gun definition
		return GunDef().GetActions(inputType);
	}
	public ActionDefinition GetShootActionDefinition(EActionInput inputType)
	{
		EActionInput shootType = inputType.GetActionType();
		if(shootType != null)
			for(ActionDefinition def : GetActionDefinitions(shootType))
				if(def.actionType == EActionType.Shoot)
					return def;
		return ActionDefinition.Invalid;
	}

	// --------------------------------------------------------------------------
	// PAINTJOBS
	// --------------------------------------------------------------------------
	public String GetPaintjobName()
	{
		if(GetItemStack().hasTag())
		{
			return GetItemStack().getTag().getString("paint");
		}
		return "default";
	}

	public void SetPaintjobName(String paint)
	{
		GetItemStack().getOrCreateTag().putString("paint", paint);
	}

	// --------------------------------------------------------------------------
	// ATTACHMENTS
	// --------------------------------------------------------------------------
	public AttachmentDefinition GetAttachmentDefinition(EAttachmentType attachType, int slot)
	{
		ItemStack attachmentStack = GetAttachmentStack(attachType, slot);
		if(attachmentStack.getItem() instanceof AttachmentItem attachmentItem)
		{
			return attachmentItem.Def();
		}
		return AttachmentDefinition.INVALID;
	}

	public int GetNumAttachmentStacks(EAttachmentType attachType)
	{
		return GunDef().GetAttachmentSettings(attachType).numAttachmentSlots;
	}

	public ItemStack GetAttachmentStack(EAttachmentType attachType, int slot)
	{
		ItemStack gunStack = GetItemStack();
		if(gunStack.getItem() instanceof FlanItem flanItem)
		{
			return flanItem.GetAttachmentInSlot(gunStack, attachType, slot);
		}
		return ItemStack.EMPTY.copy();
	}

	public void SetAttachmentStack(EAttachmentType attachType, int slot, ItemStack attachmentStack)
	{
		ItemStack gunStack = GetItemStack();
		if(gunStack.getItem() instanceof FlanItem flanItem)
		{
			flanItem.SetAttachmentInSlot(gunStack, attachType, slot, attachmentStack);
		}
	}

	public ItemStack RemoveAttachmentFromSlot(EAttachmentType attachType, int slot)
	{
		ItemStack gunStack = GetItemStack();
		if(gunStack.getItem() instanceof FlanItem flanItem)
		{
			return flanItem.RemoveAttachmentFromSlot(gunStack, attachType, slot);
		}
		return ItemStack.EMPTY;
	}

	public List<ItemStack> GetAttachmentStacks()
	{
		ItemStack gunStack = GetItemStack();
		if(gunStack.getItem() instanceof FlanItem flanItem)
		{
			return flanItem.GetAttachmentStacks(gunStack);
		}
		return new ArrayList<>();
	}

	public List<ModifierDefinition> GetApplicableModifiers(String stat, EActionInput actionSet)
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

	public List<ModifierDefinition> GetAllApplicableModifiers(EActionInput actionSet)
	{
		List<ModifierDefinition> results = new ArrayList<>();
		List<ItemStack> attachmentStacks = GetAttachmentStacks();
		for (ItemStack attachmentStack : attachmentStacks)
		{
			if (attachmentStack.getItem() instanceof AttachmentItem attachmentItem)
			{
				AttachmentDefinition attachDef = attachmentItem.Def();
				for (ModifierDefinition modifierDef : attachDef.modifiers)
				{
					if (modifierDef.AppliesTo(actionSet))
					{
						results.add(modifierDef);
					}
				}
			}
		}
		return results;
	}

	public Map<String, List<ModifierDefinition>> GetAllApplicableModifiersMap(EActionInput actionSet)
	{
		Map<String, List<ModifierDefinition>> results = new IdentityHashMap<>();
		List<ItemStack> attachmentStacks = GetAttachmentStacks();
		for (ItemStack attachmentStack : attachmentStacks)
		{
			if (attachmentStack.getItem() instanceof AttachmentItem attachmentItem)
			{
				AttachmentDefinition attachDef = attachmentItem.Def();
				for (ModifierDefinition modifierDef : attachDef.modifiers)
				{
					if (modifierDef.AppliesTo(actionSet))
					{
						List<ModifierDefinition> modList = results.get(modifierDef.Stat);
						if (modList == null)
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
	@Override
	public String toString()
	{
		return "Gun:" + GetItemStack().toString();
	}
}
