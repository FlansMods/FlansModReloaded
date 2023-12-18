package com.flansmod.common.actions.contexts;

import com.flansmod.common.FlansMod;
import com.flansmod.common.abilities.ApplyModifierAbility;
import com.flansmod.common.actions.*;
import com.flansmod.common.gunshots.*;
import com.flansmod.common.item.*;
import com.flansmod.common.types.abilities.AbilityDefinition;
import com.flansmod.common.types.attachments.AttachmentDefinition;
import com.flansmod.common.types.attachments.EAttachmentType;
import com.flansmod.common.types.guns.elements.*;
import com.flansmod.common.types.elements.ModifierDefinition;
import com.flansmod.common.types.guns.*;
import com.flansmod.common.types.parts.PartDefinition;
import com.flansmod.common.types.vehicles.EPlayerInput;
import com.flansmod.util.Transform;
import com.mojang.datafixers.util.Pair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
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
			{
				String groupPath = ActionGroupContext.CreateGroupPath(agDef.key);
				CachedGroupPathNames.put(groupPath.hashCode(), groupPath);
			}
			for(EAttachmentType attachmentType : EAttachmentType.values())
			{
				for (int i = 0; i < GetNumAttachmentStacks(attachmentType); i++)
				{
					AttachmentDefinition attachmentDefintion = GetAttachmentDefinition(attachmentType, i);
					if(attachmentDefintion.IsValid())
					{
						for(ActionGroupDefinition agDef : attachmentDefintion.actionOverrides)
						{
							String groupPath = ActionGroupContext.CreateGroupPath(attachmentType, i, agDef.key);
							CachedGroupPathNames.put(groupPath.hashCode(), groupPath);
						}
					}
				}
			}
		}
		return GetActionGroupContext(CachedGroupPathNames.get(groupPathHash));
	}
	@Nonnull
	public ActionGroupContext GetActionGroupContextSibling(ActionGroupContext original, String newKey)
	{
		if(original.IsAttachment())
		{
			ActionGroupContext.CreateGroupPath(original.GetAttachmentType(), original.GetAttachmentIndex(), newKey);
		}
		return GetActionGroupContext(newKey);
	}
	// ---------------------------------------------------------------------------------------------------

	private final HashMap<ModifierDefinition, Float> ModifierCache;
	private int ModifierHash;
	public ItemStack Stack;
	@Nonnull
	public final GunDefinition Def;

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
	@Nullable
	public Level GetLevel() { return null; }
	@Nullable
	public Transform GetPosition() { return null; }

	// --------------------------------------------------------------------------
	// Helpers
	// --------------------------------------------------------------------------

	protected void AddModifierToCache(ModifierDefinition modDef, float multiplier)
	{
		ModifierCache.put(modDef, ModifierCache.getOrDefault(modDef, 0.0f) + multiplier);
	}

	public ItemStack GetItemStack() { return Stack; }
	public void SetItemStack(ItemStack stack)
	{
		Stack = stack;
		OnItemStackChanged(stack);
	}
	public Transform GetShootOrigin()
	{
		return GetShooter().GetShootOrigin();
	}
	public boolean IsValid()
	{
		if(UpdateFromItemStack())
			return false;
		if(Stack.isEmpty())
			return false;
		return Def.IsValid();
	}
	@Nonnull
	public UUID GetUUID()
	{
		return FlanItem.GetGunID(Stack);
	}
	protected GunContext(@Nonnull ItemStack stackAtTimeOfCreation)
	{
		Stack = stackAtTimeOfCreation.copy();
		ModifierCache = new HashMap<>();
		ModifierHash = 0;
		Def = CacheGunDefinition();
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

	// --------------------------------------------------------------------------
	// Stat Cache
	// --------------------------------------------------------------------------
	@Nonnull
	public Map<ModifierDefinition, Float> GetModifiers()
	{
		int updatedModifierHash = HashModifierSources() ^ HashAttachmentModifiers() ^ HashAbilityModifiers();
		if(updatedModifierHash != ModifierHash)
		{
			ModifierCache.clear();
			RecalculateAttachmentModifierCache();
			RecalculateAbilityModifierCache();
			RecalculateModifierCache();
			ModifierHash = updatedModifierHash;
		}
		return ModifierCache;
	}
	public void Apply(ModifierStack modStack)
	{
		GetShooter().Apply(modStack);

		for(var kvp : GetModifiers().entrySet())
			modStack.Modify(kvp.getKey(), kvp.getValue());
	}

	// --------------------------------------------------------------------------
	// ItemStack Operations
	// --------------------------------------------------------------------------
	@Nonnull
	protected CompoundTag GetTags(@Nonnull String key)
	{
		CompoundTag root = GetItemStack().getOrCreateTag();
		if(root.contains(key))
			return root.getCompound(key);
		return new CompoundTag();
	}
	protected void SetTags(@Nonnull String key, @Nonnull CompoundTag tags)
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
					EvaluateAttachmentInputHandler(inputContext,
						ActionGroupContext.CreateGroupPath(node.attachmentType, node.attachmentIndex, ""),
						attachmentDef,
						results);
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
	public int GetNumAttachments()
	{
		return GetAttachmentStacks().size();
	}
	public int GetNumAttachmentStacks(EAttachmentType attachType)
	{
		return Def.GetAttachmentSettings(attachType).numAttachmentSlots;
	}
	@Nonnull
	public ItemStack GetAttachmentStack(EAttachmentType attachType, int slot)
	{
		if(GetItemStack().getItem() instanceof FlanItem flanItem)
			return flanItem.GetAttachmentInSlot(GetItemStack(), attachType, slot);
		return ItemStack.EMPTY;
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

	private int HashAttachmentModifiers()
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
				for(ModifierDefinition modDef : attachmentItem.Def().modifiers)
					AddModifierToCache(modDef, 1.0f);
	}

	// -----------------------------------------------------------------------------------------------------------------
	// Abilities
	// -----------------------------------------------------------------------------------------------------------------
	@Nonnull
	public Map<AbilityDefinition, Integer> GetAbilities()
	{
		if(GetItemStack().getItem() instanceof FlanItem flanItem)
		{
			return flanItem.GetAbilities(GetItemStack());
		}
		return Map.of();
	}

	public int HashAbilityModifiers()
	{
		int hash = 0;
		ActionStack stack = GetActionStack();
		if(stack.IsValid())
		{
			for(ApplyModifierAbility modAbility : stack.GetActiveModifierAbilities())
			{
				hash ^= modAbility.hashCode();
			}
		}
		return hash;
	}

	public void RecalculateAbilityModifierCache()
	{
		ActionStack stack = GetActionStack();
		if(stack.IsValid())
			for(ApplyModifierAbility modAbility : stack.GetActiveModifierAbilities())
				for(ModifierDefinition modDef : modAbility.GetModifiers())
					AddModifierToCache(modDef, modAbility.GetIntensity(this));
	}

	// -----------------------------------------------------------------------------------------------------------------
	// Util methods
	// -----------------------------------------------------------------------------------------------------------------

	public boolean ExpelItems(@Nonnull List<ItemStack> stacks)
	{
		Level level = GetLevel();
		if(level == null)
			return false;
		if(level.isClientSide)
			return false;

		for(ItemStack stack : stacks)
		{
			ItemEntity itemEntity = new ItemEntity(EntityType.ITEM, level);
			itemEntity.setItem(stack);
			itemEntity.setPos(GetShootOrigin().PositionVec3());
			level.addFreshEntity(itemEntity);
		}

		return true;
	}

	public void Save(CompoundTag tags)
	{
		// Save a copy of the ItemStack
		CompoundTag stackTags = new CompoundTag();
		Stack.save(stackTags);
		tags.put("stack", stackTags);

		// Then put extra details if they make sense
		tags.putInt("slot", GetInventorySlotIndex());

		// And if our shooter is set, write that out
		// TODO: Should this not be more like a heirarchy where these are child NBT nodes of shooter/null?
		CompoundTag shooterTags = new CompoundTag();
		GetShooter().Save(shooterTags);
		tags.put("shooter", shooterTags);
	}

	public static GunContext Load(CompoundTag tags, boolean client)
	{
		// Stack
		ItemStack stack = ItemStack.of(tags.getCompound("stack"));
		UUID gunID = FlanItem.GetGunID(stack);

		// Slot
		int slot = tags.getInt("slot");

		// Shooter
		ShooterContext shooter = ShooterContext.Load(tags.getCompound("shooter"), client);
		if(shooter.IsValid())
			return shooter.CreateContext(gunID);

		// Last option, we don't know who made this, but we can give an isolated context to allow _some_ functionality
		return GunContextCache.CreateWithoutCaching(stack);
	}

	@Override
	public String toString()
	{
		return "GunContext:" + GetItemStack().toString();
	}
}
