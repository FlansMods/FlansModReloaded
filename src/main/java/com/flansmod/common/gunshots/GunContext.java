package com.flansmod.common.gunshots;

import com.flansmod.common.actions.ActionStack;
import com.flansmod.common.actions.EActionInput;
import com.flansmod.common.item.AttachmentItem;
import com.flansmod.common.item.BulletItem;
import com.flansmod.common.item.GunItem;
import com.flansmod.common.types.attachments.AttachmentDefinition;
import com.flansmod.common.types.attachments.EAttachmentType;
import com.flansmod.common.types.elements.ActionDefinition;
import com.flansmod.common.types.elements.ModifierDefinition;
import com.flansmod.common.types.guns.*;
import net.minecraft.nbt.CompoundTag;
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
	public abstract Inventory GetAttachedInventory();
	public abstract boolean CanPerformTwoHandedAction();
	// Not necessarily valid to ask for a hand, but in cases where it is valid, use this
	public InteractionHand GetHand() { return null; }

	public boolean IsValid() { return !GetItemStack().isEmpty(); }

	// Cached variables?
	private CachedGunStats cachedPrimaryAction = null;
	private CachedGunStats cachedSecondaryAction = null;

	protected GunContext() {}

	/// Public accessors of accumulated stats
	// Make sure we process attachments when getting stats and results about the gun
	@Nonnull
	public GunDefinition GunDef()
	{
		if(GetItemStack() != null && GetItemStack().getItem() instanceof GunItem gunItem)
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

	public boolean IsReloadInProgress()
	{
		ActionStack actionStack = GetActionStack();
		return actionStack != null && actionStack.IsReloading();
	}

	public int GetCurrentChamber()
	{
		return GetItemStack().getOrCreateTag().contains("chamber") ? GetItemStack().getTag().getInt("chamber") : 0;
	}

	public void SetCurrentChamber(int chamber)
	{
		GetItemStack().getOrCreateTag().putInt("chamber", chamber);
	}

	public void AdvanceChamber()
	{
		int chamber = GetCurrentChamber();
		chamber++;
		if(chamber >= GunDef().numBullets)
			chamber = 0;
		SetCurrentChamber(chamber);
	}

	public int GetNextBulletSlotToLoad()
	{
		for(int i = 0; i < GunDef().numBullets; i++)
		{
			if(GetBulletStack(i).isEmpty())
				return i;
		}
		return Inventory.NOT_FOUND_INDEX;
	}

	public void LoadOne(int bulletSlot)
	{
		if(bulletSlot < 0 || bulletSlot >= GunDef().numBullets)
			return;

		// TODO: Reloading needs to support both inputs
		ActionDefinition shootActionDef = GetShootActionDefinition(EActionInput.PRIMARY);
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

	// --------------------------------------------------------------------------
	// ACTIONS
	// --------------------------------------------------------------------------
	public abstract ActionStack GetActionStack();
	public abstract boolean CanPerformActions();

	public boolean HasAction(EActionInput inputType)
	{
		return GetActionDefinitions(inputType).length > 0;
	}
	@Nonnull
	public ActionDefinition[] GetActionDefinitions(EActionInput inputType)
	{
		return GunDef().GetActions(inputType);
	}
	public ActionDefinition GetShootActionDefinition(EActionInput inputType)
	{
		// TODO: Check attachments for changes in action
		// e.g. An underslung grenade launcher attachment

		for(ActionDefinition def : GunDef().GetActions(inputType))
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
