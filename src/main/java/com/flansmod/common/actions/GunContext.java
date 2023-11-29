package com.flansmod.common.actions;

import com.flansmod.common.FlansMod;
import com.flansmod.common.gunshots.*;
import com.flansmod.common.item.*;
import com.flansmod.common.types.attachments.AttachmentDefinition;
import com.flansmod.common.types.attachments.EAttachmentType;
import com.flansmod.common.types.guns.elements.*;
import com.flansmod.common.types.elements.ModifierDefinition;
import com.flansmod.common.types.guns.*;
import com.flansmod.common.types.magazines.MagazineDefinition;
import com.flansmod.common.types.parts.PartDefinition;
import com.flansmod.common.types.vehicles.EPlayerInput;
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
	public static final GunContext INVALID = new GunContext(ItemStack.EMPTY, null)
	{
		@Override
		public void OnItemStackChanged(ItemStack stack) {}
		@Override
		public boolean UpdateFromItemStack() { return false; }
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
	public static GunContext GetGunContext(ItemStack stack)
	{
		// No caching, because we don't know where this stack is
		if(stack.getItem() instanceof GunItem gun)
			return new GunContextItem(stack);
		return INVALID;
	}

	@Nonnull
	public static GunContext GetGunContext(Inventory inventory, int slot)
	{
		// No caching, because we don't know where this inventory is
		if(inventory.getContainerSize() > slot && inventory.getItem(slot).getItem() instanceof GunItem gun)
			return new GunContextInventoryItem(inventory, slot, inventory.player.level);
		return INVALID;
	}

	@Nonnull
	public static GunContext GetGunContext(UUID shooterUUID, int slotIndex, boolean client)
	{
		ShooterContext shooterContext = ShooterContext.GetOrCreate(shooterUUID, client);
		if(shooterContext.IsValid())
			return shooterContext.GetOrCreate(slotIndex);

		return GunContext.INVALID;
	}

	@Nonnull
	public static GunContext GetGunContext(ShooterContext shooter, int inventorySlotIndex)
	{
		if(shooter.IsValid())
			return shooter.GetOrCreate(inventorySlotIndex);

		return INVALID;
	}

	@Nonnull
	public static GunContext GetGunContext(ShooterContext shooter, InteractionHand hand)
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
	// CONTEXT CACHES (Contained in the GunContext and built over time)
	// ---------------------------------------------------------------------------------------------------
	private final HashMap<EPlayerInput, GunInputContext> InputContextCache = new HashMap<>();
	private final HashMap<String, ActionGroupContext> ActionGroupContextCache = new HashMap<>();
	private final HashMap<Integer, String> CachedGroupPathNames = new HashMap<>();
	@Nonnull
	public GunInputContext GetInputContext(EPlayerInput inputType)
	{
		if(InputContextCache.containsKey(inputType))
			return InputContextCache.get(inputType);

		GunInputContext context = new GunInputContext(this, inputType);
		InputContextCache.put(inputType, context);
		return context;
	}
	@Nonnull
	public ActionGroupContext GetActionGroupContext(String groupPath)
	{
		if(groupPath == null)
			return ActionGroupContext.INVALID;

		if(ActionGroupContextCache.containsKey(groupPath))
			return ActionGroupContextCache.get(groupPath);

		ActionGroupContext context = new ActionGroupContext(this, groupPath);
		ActionGroupContextCache.put(groupPath, context);
		return context;
	}
	@Nonnull
	public ActionGroupContext GetActionGroupContextByHash(int groupPathHash)
	{
		if(!CachedGroupPathNames.containsKey(groupPathHash))
		{
			for(ActionGroupDefinition agDef : Def.actionGroups)
				CachedGroupPathNames.put(agDef.key.hashCode(), agDef.key);
			for(AttachmentDefinition attachmentDef : GetAttachmentDefinitions())
				for(ActionGroupDefinition agDef : attachmentDef.actionOverrides)
					CachedGroupPathNames.put(agDef.hashCode(), agDef.key);
		}
		return GetActionGroupContext(CachedGroupPathNames.get(groupPathHash));
	}
	@Nonnull
	public ActionGroupContext GetActionGroupContextSibling(ActionGroupContext original, String newKey)
	{
		if(original.IsAttachment())
			return GetActionGroupContext(original.GetAttachmentType() + "/" + original.GetAttachmentIndex() + "/" + newKey);
		return GetActionGroupContext(newKey);
	}
	// ---------------------------------------------------------------------------------------------------

	protected final List<ModifierDefinition> ModifierCache;
	private int ModifierHash;
	public ItemStack Stack;
	@Nonnull
	public final GunDefinition Def;
	@Nullable
	public final Level Level;

	// --------------------------------------------------------------------------
	// Abstractions
	// --------------------------------------------------------------------------
	public abstract void OnItemStackChanged(ItemStack stack);
	public abstract boolean UpdateFromItemStack();
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
		if(UpdateFromItemStack())
			return false;
		if(Stack.isEmpty())
			return false;
		if(Stack.getItem() instanceof FlanItem flanItem)
			return flanItem.Def().IsValid();
		return false;
	}
	protected GunContext(@Nonnull ItemStack stackAtTimeOfCreation, @Nullable Level level)
	{
		Stack = stackAtTimeOfCreation.copy();
		ModifierCache = new ArrayList<>();
		ModifierHash = 0;
		Def = CacheGunDefinition();
		Level = level;
	}
	/// Public accessors of accumulated stats
	// Make sure we process attachments when getting stats and results about the gun
	@Nonnull
	public GunDefinition CacheGunDefinition()
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
			hash ^= flanItem.GetPaintjobName(stack).hashCode();
			if(flanItem instanceof GunItem gunItem)
			{
				MagazineDefinition mag = gunItem.GetMagazineType(stack, Actions.DefaultPrimaryActionKey, 0);
				hash ^= (mag.hashCode() << 16) | (mag.hashCode() >> 16);
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
	protected CompoundTag GetTags(String key)
	{
		CompoundTag root = GetItemStack().getOrCreateTag();
		if(root.contains(key))
			return root.getCompound(key);
		return new CompoundTag();
	}
	protected void SetTags(String key, CompoundTag tags)
	{
		ItemStack updatedStack = Stack.copy();
		updatedStack.getOrCreateTag().put(key, tags);
		SetItemStack(updatedStack);
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
	// DEFINITION PEEKING
	// --------------------------------------------------------------------------
	@Nonnull
	public List<ReloadDefinition> GetReloadDefintions()
	{
		List<ReloadDefinition> results = new ArrayList<>(Arrays.asList(Def.reloads));
		for(AttachmentDefinition attachmentDef : GetAttachmentDefinitions())
		{
			results.addAll(Arrays.asList(attachmentDef.reloadOverrides));
		}
		return results;
	}
	@Nonnull
	private ReloadDefinition[] GetReloadDefinitionsForSubpath(ActionGroupContext groupContext)
	{
		if(groupContext.IsAttachment())
			return GetAttachmentDefinition(groupContext.GetAttachmentType(), groupContext.GetAttachmentIndex()).reloadOverrides;
		return Def.reloads;
	}
	@Nullable
	public ReloadDefinition GetReloadDefinitionContaining(ActionGroupContext groupContext)
	{
		for(ReloadDefinition reload : GetReloadDefinitionsForSubpath(groupContext))
		{
			if(reload.Contains(groupContext.GroupPath))
				return reload;
		}
		return null;
	}
	@Nonnull
	public List<ActionDefinition> GetPotentialPrimaryActions()
	{
		return GetPotentialActions(EPlayerInput.Fire1);
	}
	@Nonnull
	public List<ActionDefinition> GetPotentialSecondaryActions()
	{
		return GetPotentialActions(EPlayerInput.Fire2);
	}
	@Nonnull
	public List<ActionDefinition> GetPotentialActions(EPlayerInput inputType)
	{
		List<ActionDefinition> results = new ArrayList<>();
		GunInputContext inputContext = GetInputContext(inputType);
		for(var kvp : EvaluateInputHandler(inputContext))
		{
			results.addAll(Arrays.asList(kvp.getFirst().Def.actions));
		}
		return results;
	}

	// --------------------------------------------------------------------------
	// INPUTS
	// --------------------------------------------------------------------------
	public List<Pair<ActionGroupContext, Boolean>> EvaluateInputHandler(GunInputContext inputContext)
	{
		List<Pair<ActionGroupContext, Boolean>> results = new ArrayList<>();
		HandlerDefinition handler = Def.GetInputHandler(inputContext);
		for(HandlerNodeDefinition node : handler.nodes)
		{
			// TODO: if(!node.modalCheck.isEmpty())

			if(!node.canTriggerWhileReloading)
			{
				if(inputContext.Gun.GetActionStack().IsReloading())
					continue;
			}
			if(node.deferToAttachment)
			{
				AttachmentDefinition attachmentDef = GetAttachmentDefinition(node.attachmentType, node.attachmentIndex);
				if(attachmentDef.IsValid())
				{
					EvaluateAttachmentInputHandler(inputContext, node.attachmentType + "/" + node.attachmentIndex + "/", attachmentDef, results);
				}
			}
			else
			{
				ActionGroupDefinition actionGroupDef = Def.GetActionGroup(node.actionGroupToTrigger);
				if(actionGroupDef.IsValid())
				{
					results.add(Pair.of(ActionGroupContext.CreateFrom(inputContext.Gun, node.actionGroupToTrigger), node.andContinueEvaluating));
				}
			}
		}
		return results;
	}
	public void EvaluateAttachmentInputHandler(GunInputContext inputContext, String prefix, AttachmentDefinition attachmentDef, List<Pair<ActionGroupContext, Boolean>> results)
	{
		HandlerDefinition attachmentInputHandler = attachmentDef.GetInputHandler(inputContext);
		for(HandlerNodeDefinition node : attachmentInputHandler.nodes)
		{
			boolean matched = false;
			// TODO: if(!node.modalCheck.isEmpty())

			if(node.deferToAttachment)
			{
				FlansMod.LOGGER.warn(inputContext.Gun + ": Attachment is deferring to attachment in handlerOverrides");
			}
			else
			{
				ActionGroupDefinition actionGroupDef = attachmentDef.GetActionGroup(node.actionGroupToTrigger);
				if(actionGroupDef.IsValid())
				{
					results.add(Pair.of(
						ActionGroupContext.CreateFrom(inputContext.Gun, prefix + node.actionGroupToTrigger),
						node.andContinueEvaluating));
					matched = true;
				}
			}

			if(matched && !node.andContinueEvaluating)
				break;
		}
	}
	// --------------------------------------------------------------------------
	// ACTIONS
	// --------------------------------------------------------------------------
	@Nonnull
	public ActionGroupInstance GetOrCreateActionGroup(ActionGroupContext context)
	{
		ActionStack stack = context.Gun.GetActionStack();
		for(ActionGroupInstance instance : stack.GetActiveActionGroups())
			if(instance.Def.key.equals(context.GroupPath))
				return instance;
		return CreateActionGroup(context);
	}
	@Nonnull
	public ActionGroupInstance CreateActionGroup(ActionGroupContext context)
	{
		ActionGroupInstance groupInstance = new ActionGroupInstance(context);
		for(ActionDefinition actionDef : context.Def.actions)
		{
			ActionInstance actionInstance = Actions.InstanceAction(groupInstance, actionDef);
			if(actionInstance != null)
				groupInstance.AddAction(actionInstance);
		}
		return groupInstance;
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
	@Nonnull
	public List<AttachmentDefinition> GetAttachmentDefinitions()
	{
		List<AttachmentDefinition> defs = new ArrayList<>();
		for(ItemStack stack : GetAttachmentStacks())
		{
			if(stack.getItem() instanceof AttachmentItem attachmentItem)
				defs.add(attachmentItem.Def());
		}
		return defs;
	}
	public int GetNumAttachmentStacks(EAttachmentType attachType)
	{
		return Def.GetAttachmentSettings(attachType).numAttachmentSlots;
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
		return GunContext.GetGunContext(stack);
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
