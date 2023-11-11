package com.flansmod.common.gunshots;

import com.flansmod.common.FlansMod;
import com.flansmod.common.actions.*;
import com.flansmod.common.item.*;
import com.flansmod.common.types.attachments.AttachmentDefinition;
import com.flansmod.common.types.attachments.EAttachmentType;
import com.flansmod.common.types.elements.ActionDefinition;
import com.flansmod.common.types.elements.ActionGroupDefinition;
import com.flansmod.common.types.elements.ActionGroupOverrideDefinition;
import com.flansmod.common.types.elements.ModifierDefinition;
import com.flansmod.common.types.guns.*;
import com.flansmod.common.types.parts.PartDefinition;
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
	public static final GunContext INVALID = new GunContext(ItemStack.EMPTY)
	{
		@Override
		public void OnItemStackChanged(ItemStack stack) {}
		@Override
		public boolean IsItemStackStillInPlace() { return false; }
		@Override
		public DamageSource CreateDamageSource() { return null; }
		@Override
		@Nonnull
		public ShooterContext GetShooter() { return ShooterContext.INVALID; }
		@Override
		public Inventory GetAttachedInventory() { return null; }
		@Override
		public boolean CanPerformTwoHandedAction() { return false; }
		@Override
		@Nonnull
		public ActionStack GetActionStack() { return ActionStack.Invalid; }
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

	protected final List<ModifierDefinition> ModifierCache;
	private int ModifierHash;
	protected ItemStack Stack;

	// --------------------------------------------------------------------------
	// Abstractions
	// --------------------------------------------------------------------------
	public abstract void OnItemStackChanged(ItemStack stack);
	public abstract boolean IsItemStackStillInPlace();
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
	public ItemStack GetItemStack() { return Stack; }
	public void SetItemStack(ItemStack stack)
	{
		if(StackUpdateWouldInvalidate(stack))
		{
			FlansMod.LOGGER.error("Trying to update GunStack with an invalidating change: " + Stack + " to " + stack);
			return;
		}

		Stack = stack;
		OnItemStackChanged(stack);
	}
	public boolean IsValid()
	{
		if(Stack.isEmpty())
			return false;
		if(Stack.getItem() instanceof FlanItem flanItem)
			return flanItem.Def().IsValid();
		return false;
	}
	protected GunContext(ItemStack stackAtTimeOfCreation)
	{
		Stack = stackAtTimeOfCreation.copy();
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

	// This should identify "the same gun", or things that do not change during use
	// e.g. You can hash the "CraftedFrom" list, but not the "Ammo" currently in the gun
	// TODO: Potentially just add an NBT tag with a UUID
	public static int HashGunOrigins(ItemStack stack)
	{
		int hash = 0;
		if(stack.getItem() instanceof FlanItem flanItem)
		{
			hash = flanItem.DefinitionLocation.hashCode();
			for(PartDefinition part : flanItem.GetCraftingInputs(stack))
			{
				hash ^= (part.hashCode() << 16) | (part.hashCode() >> 16);
			}
		}
		return hash;
	}
	public boolean StackUpdateWouldInvalidate(ItemStack stack)
	{
		int newHash = HashGunOrigins(stack);
		int oldHash = HashGunOrigins(Stack);
		return newHash != oldHash;
	}

	// --------------------------------------------------------------------------
	// Stat Cache
	// --------------------------------------------------------------------------
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
	public List<ActionDefinition> GetActionDefinitions(EActionInput inputType)
	{
		// First check if any attachments are modifying our actions
		List<ActionGroupOverrideDefinition> overrides = new ArrayList<>();
		for(ItemStack attachmentStack : GetAttachmentStacks())
		{
			if(attachmentStack.getItem() instanceof AttachmentItem attachmentItem)
			{
				attachmentItem.Def().GetOverrides(inputType, overrides);
			}
		}

		return Actions.EvaluateActionOverrides(GunDef().GetActionGroup(inputType), inputType, overrides);
	}
	@Nonnull
	public ActionGroup CreateActionGroup(EActionInput inputType)
	{
		// First check if any attachments are modifying our actions
		List<ActionGroupOverrideDefinition> overrides = new ArrayList<>();
		for(ItemStack attachmentStack : GetAttachmentStacks())
		{
			if(attachmentStack.getItem() instanceof AttachmentItem attachmentItem)
			{
				attachmentItem.Def().GetOverrides(inputType, overrides);
			}
		}
		return Actions.CreateActionGroup(GunDef().GetActionGroup(inputType), inputType, overrides);
	}
	@Nonnull
	public ReloadProgress[] CreateReloads(EActionInput inputType)
	{
		if(inputType.IsReload())
		{
			return new ReloadProgress[] {
				new ReloadProgress(GunDef().GetReload(inputType), inputType),
			};
		}
		return new ReloadProgress[0];
	}
	@Nullable
	public ActionGroup GetExistingActionGroup(EActionInput inputType)
	{
		return GetActionStack().FindMatchingActiveGroup(GunDef().GetActionGroup(inputType));
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

	// -----------------------------------------------------------------------------------------------------------------
	// Util methods
	// -----------------------------------------------------------------------------------------------------------------

	public void Save(CompoundTag tags)
	{
		CompoundTag stackTags = new CompoundTag();
		Stack.save(stackTags);
		tags.put("stack", stackTags);
		tags.putInt("slot", GetInventorySlotIndex());

		CompoundTag shooterTags = new CompoundTag();
		GetShooter().Save(shooterTags);
		tags.put("shooter", shooterTags);
	}

	public static GunContext Load(CompoundTag tags, boolean client)
	{
		ItemStack stack = ItemStack.of(tags.getCompound("stack"));
		int slot = tags.getInt("slot");
		int contextHash = HashGunOrigins(stack);
		ShooterContext shooter = ShooterContext.Load(tags.getCompound("shooter"), client);
		if(shooter.IsValid())
			return shooter.CreateOldGunContext(slot, contextHash, stack);

		// Last option, we don't know who made this, but we can give an isolated context to allow _some_ functionality
		return GunContext.GetOrCreate(stack);
	}

	@Override
	public int hashCode()
	{
		return HashGunOrigins(Stack);
	}
	@Override
	public boolean equals(Object other)
	{
		if(other == this) return true;
		if(other instanceof GunContext otherContext)
		{
			return HashGunOrigins(Stack) == HashGunOrigins(otherContext.Stack);
		}
		return false;
	}
	@Override
	public String toString()
	{
		return "GunContext:" + GetItemStack().toString();
	}
}
