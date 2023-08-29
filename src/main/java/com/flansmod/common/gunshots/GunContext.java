package com.flansmod.common.gunshots;

import com.flansmod.common.actions.ActionStack;
import com.flansmod.common.actions.EActionInput;
import com.flansmod.common.item.*;
import com.flansmod.common.types.attachments.AttachmentDefinition;
import com.flansmod.common.types.attachments.EAttachmentType;
import com.flansmod.common.types.elements.ActionDefinition;
import com.flansmod.common.types.elements.ModifierDefinition;
import com.flansmod.common.types.guns.*;
import com.flansmod.common.types.parts.PartDefinition;
import com.flansmod.util.MinecraftHelpers;
import com.mojang.datafixers.util.Pair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Inventory;
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
		@Override
		public int HashModifierSources() { return 0; }
		@Override
		public void RecalculateModifierCache() { }
	};

	// ---------------------------------------------------------------------------------------------------
	// GUN CONTEXT CACHE (Caches are contained within ShooterContext)
	// ---------------------------------------------------------------------------------------------------
	@Nonnull
	public static GunContext GetOrCreate(ItemStack stack)
	{
		// No caching, because we don't know where this stack is
		if(stack.getItem() instanceof GunItem gun)
			return new GunContextItem(stack);
		return INVALID;
	}

	@Nonnull
	public static GunContext GetOrCreate(Inventory inventory, int slot)
	{
		// No caching, because we don't know where this inventory is
		if(inventory.getContainerSize() > slot && inventory.getItem(slot).getItem() instanceof GunItem gun)
			return new GunContextInventoryItem(inventory, slot);
		return INVALID;
	}

	@Nonnull
	public static GunContext GetOrCreate(UUID shooterUUID, int slotIndex, boolean client)
	{
		ShooterContext shooterContext = ShooterContext.GetOrCreate(shooterUUID, client);
		if(shooterContext.IsValid())
			return shooterContext.GetOrCreate(slotIndex);

		return GunContext.INVALID;
	}

	@Nonnull
	public static GunContext GetOrCreate(ShooterContext shooter, int inventorySlotIndex)
	{
		if(shooter.IsValid())
			return shooter.GetOrCreate(inventorySlotIndex);

		return INVALID;
	}

	@Nonnull
	public static GunContext GetOrCreate(ShooterContext shooter, InteractionHand hand)
	{
		if(shooter.IsValid())
			if(shooter instanceof ShooterContextPlayer playerContext)
			{
				return playerContext.GetOrCreate(hand == InteractionHand.MAIN_HAND ? playerContext.Player.getInventory().selected : Inventory.SLOT_OFFHAND);
			}
			else
			{
				return shooter.GetOrCreate(hand == InteractionHand.MAIN_HAND ? 0 : 1);
			}
		return INVALID;
	}

	// ---------------------------------------------------------------------------------------------------
	// ACTION GROUP CONTEXT CACHE (Contained in the GunContext and built over time)
	// ---------------------------------------------------------------------------------------------------
	private final HashMap<EActionInput, ActionGroupContext> ActionGroupContextCache = new HashMap<>();
	@Nonnull
	public ActionGroupContext GetOrCreate(EActionInput inputType)
	{
		if(ActionGroupContextCache.containsKey(inputType))
			return ActionGroupContextCache.get(inputType);

		ActionGroupContext context = new ActionGroupContext(this, inputType);
		ActionGroupContextCache.put(inputType, context);
		return context;
	}
	// ---------------------------------------------------------------------------------------------------



	// --------------------------------------------------------------------------
	// Abstractions
	// --------------------------------------------------------------------------
	@Nonnull
	public abstract ItemStack GetItemStack();
	public abstract void SetItemStack(ItemStack stack);
	public abstract DamageSource CreateDamageSource();
	@Nonnull
	public abstract ShooterContext GetShooter();
	public abstract Container GetAttachedInventory();
	public abstract boolean CanPerformTwoHandedAction();
	// Not necessarily valid to ask for a hand, but in cases where it is valid, use this
	public int GetInventorySlotIndex() { return Inventory.NOT_FOUND_INDEX; }
	public abstract int HashModifierSources();
	public abstract void RecalculateModifierCache();
	@Nonnull
	public abstract ActionStack GetActionStack();
	public abstract boolean CanPerformActions();


	// --------------------------------------------------------------------------
	// Helpers
	// --------------------------------------------------------------------------
	public boolean IsValid() { return !GetItemStack().isEmpty(); }
	protected GunContext()
	{
		ModifierCache = new ArrayList<>();
		ModifierHash = 0;
	}
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
		return GetShooter().IsValid() ? GetShooter().Level() : null;
	}

	// --------------------------------------------------------------------------
	// Stat Cache
	// --------------------------------------------------------------------------
	protected final List<ModifierDefinition> ModifierCache;
	private int ModifierHash;
	@Nonnull
	public List<ModifierDefinition> GetModifiers()
	{
		int updatedModifierHash = HashModifierSources() ^ HashAttachments();
		if(updatedModifierHash != ModifierHash)
		{
			ModifierCache.clear();
			RecalculateAttachmentModifierCache();
			RecalculateModifierCache();
			// And base modifiers
			for(ActionDefinition action : GunDef().GetActions(EActionInput.PRIMARY))
				if(action.IsValid() && action.actionType == EActionType.Shoot)
					ModifierCache.addAll(Arrays.asList(action.modifiers));
			for(ActionDefinition action : GunDef().GetActions(EActionInput.SECONDARY))
				if(action.IsValid() && action.actionType == EActionType.Shoot)
					ModifierCache.addAll(Arrays.asList(action.modifiers));
			ModifierHash = updatedModifierHash;
		}
		return ModifierCache;
	}
	public void Apply(ModifierStack modStack)
	{
		GetShooter().Apply(modStack);

		for(ModifierDefinition mod : GetModifiers())
			modStack.Apply(mod);
	}

	// --------------------------------------------------------------------------
	// ItemStack Operations
	// --------------------------------------------------------------------------
	@Nonnull
	protected CompoundTag GetOrCreateTags(String key)
	{
		CompoundTag root = GetItemStack().getOrCreateTag();
		if(!root.contains(key))
		{
			root.put(key, new CompoundTag());
		}
		return root.getCompound(key);
	}

	private void UpdateTags(CompoundTag tag)
	{
		GetItemStack().setTag(tag);
	}

	@Nullable
	private CompoundTag TryGetTags(String key)
	{
		if(!GetItemStack().hasTag())
			return null;
		if(!GetItemStack().getOrCreateTag().contains(key))
			return null;
		return GetItemStack().getOrCreateTag().getCompound(key);
	}

	@Nonnull
	private CompoundTag GetOrCreatePrimaryTags() { return GetOrCreateTags("primary"); }
	@Nonnull
	private CompoundTag GetOrCreateSecondaryTags() { return GetOrCreateTags("secondary"); }
	@Nullable
	private CompoundTag GetOrCreateActionTags(EActionInput input)
	{
		switch(input)
		{
			case PRIMARY, RELOAD_PRIMARY -> { return GetOrCreatePrimaryTags(); }
			case SECONDARY, RELOAD_SECONDARY -> { return GetOrCreateSecondaryTags(); }
		}
		return null;
	}


	// --------------------------------------------------------------------------
	// CRAFTING HISTORY
	// --------------------------------------------------------------------------
	// Only remember parts that we used, not arbitrary item stacks with NBT
	public PartDefinition[] GetCraftingInputs()
	{
		if(GetItemStack().getItem() instanceof FlanItem flanItem)
		{
			return flanItem.GetCraftingInputs(GetItemStack());
		}
		return new PartDefinition[0];
	}

	public void SetCraftingInputs(ItemStack[] stacks)
	{
		if(GetItemStack().getItem() instanceof FlanItem flanItem)
		{
			flanItem.SetCraftingInputs(GetItemStack(), stacks);
		}
	}

	// --------------------------------------------------------------------------
	// ACTIONS
	// --------------------------------------------------------------------------
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
		if(GetItemStack().getItem() instanceof FlanItem flanItem)
		{
			return flanItem.GetPaintjobName(GetItemStack());
		}
		return "default";
	}

	public void SetPaintjobName(String paint)
	{
		if(GetItemStack().getItem() instanceof FlanItem flanItem)
		{
			flanItem.SetPaintjobName(GetItemStack(), paint);
		}
	}

	// --------------------------------------------------------------------------
	// ATTACHMENTS
	// --------------------------------------------------------------------------
	@Nonnull
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
	@Nonnull
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
	@Nonnull
	public ItemStack RemoveAttachmentFromSlot(EAttachmentType attachType, int slot)
	{
		ItemStack gunStack = GetItemStack();
		if(gunStack.getItem() instanceof FlanItem flanItem)
		{
			return flanItem.RemoveAttachmentFromSlot(gunStack, attachType, slot);
		}
		return ItemStack.EMPTY;
	}
	@Nonnull
	public List<ItemStack> GetAttachmentStacks()
	{
		ItemStack gunStack = GetItemStack();
		if(gunStack.getItem() instanceof FlanItem flanItem)
		{
			return flanItem.GetAttachmentStacks(gunStack);
		}
		return new ArrayList<>();
	}

	private int HashAttachments()
	{
		int hash = 0xa77ac4;
		for(ItemStack stack : GetAttachmentStacks())
		 	if(stack.getItem() instanceof FlanItem flanItem)
			{
				int defHash = flanItem.Def().hashCode();
				hash ^= (defHash << 16) | (defHash >> 16);
			}
		return hash;
	}
	private void RecalculateAttachmentModifierCache()
	{
		ModifierCache.clear();
		for(ItemStack stack : GetAttachmentStacks())
			if(stack.getItem() instanceof AttachmentItem attachmentItem)
				ModifierCache.addAll(Arrays.asList(attachmentItem.Def().modifiers));
	}

	/*
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
	 */


	@Override
	public String toString()
	{
		return "Gun:" + GetItemStack().toString();
	}
}
