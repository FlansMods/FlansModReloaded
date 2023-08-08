package com.flansmod.common.gunshots;

import com.flansmod.common.FlansMod;
import com.flansmod.common.actions.*;
import com.flansmod.common.item.BulletItem;
import com.flansmod.common.types.elements.ActionDefinition;
import com.flansmod.common.types.elements.ActionGroupDefinition;
import com.flansmod.common.types.elements.ModifierDefinition;
import com.flansmod.common.types.guns.EActionType;
import com.flansmod.common.types.magazines.EAmmoConsumeMode;
import com.flansmod.common.types.guns.ERepeatMode;
import com.flansmod.common.types.guns.GunDefinition;
import com.flansmod.common.types.magazines.EAmmoLoadMode;
import com.flansmod.common.types.magazines.MagazineDefinition;
import com.flansmod.util.Maths;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lwjgl.opengl._3DFXTextureCompressionFXT1;

import javax.annotation.Nonnull;

public class ActionGroupContext
{
	public static final ActionGroupContext INVALID = new ActionGroupContext(GunContext.INVALID, EActionInput.PRIMARY);

	public final GunContext Gun;
	public final EActionInput InputType;

	// Shooter references
	public ShooterContext Shooter() { return Gun.GetShooter(); }
	public Entity Owner() { return Gun.GetShooter().IsValid() ? Gun.GetShooter().Owner() : null; }
	public Entity Entity() { return Gun.GetShooter().IsValid() ? Gun.GetShooter().Entity() : null; }

	// Gun references
	public GunContext Gun() { return Gun; }
	public GunDefinition GunDef() { return Gun.GunDef(); }
	public ActionGroupDefinition GroupDef() { return Gun.GunDef().GetActionGroup(InputType); }
	@Nonnull
	public ActionStack ActionStack() { return Gun.GetActionStack(); }

	public boolean IsValid() { return Gun().IsValid(); }

	public static ActionGroupContext CreateFrom(GunContext gunContext, EActionInput inputType)
	{
		if(gunContext.IsValid())
			return new ActionGroupContext(gunContext, inputType);
		return INVALID;
	}

	protected ActionGroupContext(@Nonnull GunContext gun, @Nonnull EActionInput inputType)
	{
		Gun = gun;
		InputType = inputType;
	}

	// --------------------------------------------------------------------------
	// ItemStack Operations
	// --------------------------------------------------------------------------
	@Nonnull
	protected CompoundTag GetRootTag()
	{
		return Gun.GetOrCreateTags(InputType.GetRootTagName());
	}
	@Nonnull
	protected CompoundTag GetMagTag(int magIndex)
	{
		CompoundTag rootTag = GetRootTag();

		final String magTag = "mag_" + magIndex;
		if (!rootTag.contains(magTag))
			rootTag.put(magTag, new CompoundTag());
		return rootTag.getCompound(magTag);
	}
	public int GetTagOrDefault(String key, int def)
	{
		if(GetRootTag().contains(key))
			return GetRootTag().getInt(key);
		return def;
	}
	// --------------------------------------------------------------------------

	// --------------------------------------------------------------------------
	// ACTIONS AND RELOADS
	// --------------------------------------------------------------------------
	public ActionDefinition[] GetActionDefinitions()
	{
		return GunDef().GetActions(InputType);
	}

	public Action[] CreateActions()
	{
		ActionDefinition[] actionDefs = GunDef().GetActions(InputType);
		Action[] actions = new Action[actionDefs.length];
		for (int i = 0; i < actionDefs.length; i++)
		{
			actions[i] = Actions.CreateAction(GunDef().GetActionGroup(InputType), actionDefs[i], InputType);
		}
		return actions;
	}

	public ReloadProgress[] CreateReloads()
	{
		if(InputType.IsReload())
		{
			return new ReloadProgress[] {
				new ReloadProgress(GunDef().GetReload(InputType), InputType),
			};
		}
		return new ReloadProgress[0];
	}


	// --------------------------------------------------------------------------
	// MAGAZINES
	// --------------------------------------------------------------------------
	@Nonnull
	public MagazineDefinition GetMagazineType(int magIndex)
	{
		// Get the root tag for our magazine
		CompoundTag magTags = GetMagTag(magIndex);
		if(magTags.contains("type"))
		{
			String type = magTags.getString("type");
			ResourceLocation magLoc = new ResourceLocation(type);
			return FlansMod.MAGAZINES.Get(magLoc);
		}
		return MagazineDefinition.INVALID;
	}

	public void SetMagazineType(int magIndex, MagazineDefinition magDef)
	{
		CompoundTag magTags = GetMagTag(magIndex);
		magTags.putString("type", magDef.GetLocationString());
	}

	public int GetMagazineSize(int magIndex)
	{
		return GetMagazineType(magIndex).numRounds;
	}

	// --------------------------------------------------------------------------
	// BULLETS
	// --------------------------------------------------------------------------
	@Nonnull
	public ItemStack[] GetCombinedBulletStacks(int magIndex)
	{
		CompoundTag magTags = GetMagTag(magIndex);
		if(magTags.contains("bullets"))
		{
			CompoundTag bulletTags = magTags.getCompound("bullets");
			ItemStack[] stacks = new ItemStack[bulletTags.size()];
			int stackIndex = 0;
			for(String key : bulletTags.getAllKeys())
			{
				stacks[stackIndex] = ItemStack.of(bulletTags.getCompound(key));
				stackIndex++;
			}
			return stacks;
		}
		return new ItemStack[0];
	}
	@Nonnull
	public ItemStack GetBulletAtIndex(int magIndex, int bulletIndex)
	{
		CompoundTag magTags = GetMagTag(magIndex);
		if (magTags.contains("bullets"))
		{
			CompoundTag bulletTags = magTags.getCompound("bullets");
			for(String key : bulletTags.getAllKeys())
			{
				int startIndex = Integer.parseInt(key);
				ItemStack stack = ItemStack.of(bulletTags.getCompound(key));
				int endIndex = startIndex + stack.getCount();
				if(startIndex <= bulletIndex && bulletIndex < endIndex)
				{
					return stack.copyWithCount(1);
				}
			}
		}
		return ItemStack.EMPTY;
	}
	@Nonnull
	protected ItemStack ConsumeBulletAtIndex(int magIndex, int bulletIndex)
	{
		MagazineDefinition magDef = GetMagazineType(magIndex);
		if(0 <= bulletIndex && bulletIndex < magDef.numRounds)
		{
			// Extract, set empty, and compact stacks
			Item[] items = ExtractCompactStacks(magIndex);
			ItemStack returnStack = new ItemStack(items[bulletIndex], 1);
			items[bulletIndex] = Items.AIR;
			CompactStacks(magIndex, items);
			return returnStack;
		}
		FlansMod.LOGGER.warn("Failed to consume bullet " + bulletIndex + " from mag " + magIndex + " in " + this);
		return ItemStack.EMPTY;
	}
	@Nonnull
	public ItemStack ConsumeOneBullet(int magIndex)
	{
		int indexToFire = GetNextIndexToFire(magIndex);
		switch(indexToFire)
		{
			//case ALL_SPECIAL_FIRE_INDEX -> {
			//	MagazineDefinition magDef = GetMagazineType(magIndex);
			//	for(int i = 0; i < magDef.numRounds; i++)
			//		ConsumeBulletAtIndex(magIndex, i);
			//}
			case INVALID_FIRE_INDEX -> {
				// No-op
			}
			default -> {
				return ConsumeBulletAtIndex(magIndex, indexToFire);
			}
		}

		return ItemStack.EMPTY;
	}
	@Nonnull
	public ItemStack LoadOneBulletIntoSlot(int magIndex, int bulletIndex, ItemStack bulletStack)
	{
		if(bulletStack.getItem() instanceof BulletItem bulletItem)
		{
			// Extract, set, and compact stacks
			Item[] items = ExtractCompactStacks(magIndex);
			items[bulletIndex] = bulletStack.getItem();
			CompactStacks(magIndex, items);

			bulletStack.setCount(bulletStack.getCount() - 1);
		}
		return bulletStack;
	}
	@Nonnull
	public ItemStack LoadBullets(int magIndex, ItemStack bulletStack)
	{
		if(bulletStack.getItem() instanceof BulletItem bulletItem)
		{
			// Extract, set, and compact stacks
			Item[] items = ExtractCompactStacks(magIndex);
			MagazineDefinition magDef = GetMagazineType(magIndex);
			for(int i = 0; i < magDef.numRounds; i++)
			{
				if(items[i] == Items.AIR)
				{
					items[i] = bulletStack.getItem();
					bulletStack.setCount(bulletStack.getCount() - 1);
				}

				if(bulletStack.isEmpty())
					break;
			}
			CompactStacks(magIndex, items);
		}
		return bulletStack;
	}

	@Nonnull
	private Item[] ExtractCompactStacks(int magIndex)
	{
		MagazineDefinition magDef = GetMagazineType(magIndex);
		CompoundTag magTags = GetMagTag(magIndex);
		Item[] items = new Item[magDef.numRounds];
		if (magTags.contains("bullets"))
		{
			CompoundTag bulletTags = magTags.getCompound("bullets");
			for(String key : bulletTags.getAllKeys())
			{
				int startIndex = Integer.parseInt(key);
				ItemStack stack = ItemStack.of(bulletTags.getCompound(key));
				int endIndex = startIndex + stack.getCount();
				for(int i = startIndex; i < endIndex; i++)
				{
					if(0 <= i && i < magDef.numRounds)
						items[i] = stack.getItem();
				}
			}
		}
		return items;
	}

	private void CompactStacks(int magIndex, Item[] items)
	{
		if(items == null || items.length == 0)
			return;

		CompoundTag bulletsTag = new CompoundTag();

		// Run through the items and compact them into stacks
		int sameSinceIndex = 0;
		Item currentlyParsing = items[0];
		for(int i = 0; i < items.length; i++)
		{
			// If we are the same item, no problem
			Item compareAgainst = i < items.length - 1 ? items[i+1] : null;
			if(compareAgainst == currentlyParsing)
				continue;

			// If we differ, then the previous lump needs to be placed into a tag
			ItemStack previousLump = new ItemStack(currentlyParsing, i - sameSinceIndex + 1);
			CompoundTag stackTag = new CompoundTag();
			previousLump.save(stackTag);
			bulletsTag.put(Integer.toString(sameSinceIndex), stackTag);
			sameSinceIndex = i;
			currentlyParsing = compareAgainst;
		}

		GetMagTag(magIndex).put("bullets", bulletsTag);
	}

	@Nonnull
	public ItemStack GetNextBulletToBeFired(int magIndex)
	{
		return GetBulletAtIndex(magIndex, GetNextIndexToFire(magIndex));
	}

	public boolean ContainsAnyBullets(int magIndex)
	{
		CompoundTag magTags = GetMagTag(magIndex);
		if (magTags.contains("bullets"))
		{
			CompoundTag bulletTags = magTags.getCompound("bullets");
			for (String key : bulletTags.getAllKeys())
			{
				ItemStack stack = ItemStack.of(bulletTags.getCompound(key));
				if(!stack.isEmpty())
					return true;
			}
		}
		return false;
	}

	// --------------------------------------------------------------------------
	// CHAMBERING and FIRING INDEX
	// --------------------------------------------------------------------------
	public static final int INVALID_FIRE_INDEX = -1;
	public static final int ALL_SPECIAL_FIRE_INDEX = -2;
	public int GetNextIndexToFire(int magIndex)
	{
		CompoundTag magTags = GetMagTag(magIndex);
		MagazineDefinition magDef = GetMagazineType(magIndex);
		int fireIndex = INVALID_FIRE_INDEX;

		switch(magDef.ammoConsumeMode)
		{
			case RoundRobin -> {
				fireIndex = GetCurrentChamber();
			}
			case LastNonEmpty, FirstNonEmpty -> {

				if (magTags.contains("bullets"))
				{
					CompoundTag bulletTags = magTags.getCompound("bullets");
					for (String key : bulletTags.getAllKeys())
					{
						int startIndex = Integer.parseInt(key);
						ItemStack stack = ItemStack.of(bulletTags.getCompound(key));
						int endIndex = startIndex + stack.getCount();
						if(!stack.isEmpty())
						{
							if(magDef.ammoConsumeMode == EAmmoConsumeMode.LastNonEmpty)
							{
								fireIndex = endIndex - 1;
							}
							else // FirstNonEmpty
							{
								fireIndex = startIndex;
								break;
							}
						}
					}
				}
			}
			case Simultaneous -> {
				fireIndex = ALL_SPECIAL_FIRE_INDEX;
			}
		}
		return fireIndex;
	}

	public int GetNextIndexToLoad(int magIndex)
	{
		CompoundTag magTags = GetMagTag(magIndex);
		MagazineDefinition magDef = GetMagazineType(magIndex);
		switch(magDef.ammoLoadMode)
		{
			case FullMag -> {
				return ALL_SPECIAL_FIRE_INDEX;
			}
			case OneBulletAtATime -> {
				if (magTags.contains("bullets"))
				{
					CompoundTag bulletTags = magTags.getCompound("bullets");

					// Find the first empty stack and return the first index in it
					for (String key : bulletTags.getAllKeys())
					{
						ItemStack stack = ItemStack.of(bulletTags.getCompound(key));
						if(stack.isEmpty())
							return Integer.parseInt(key);
					}
				}
				return INVALID_FIRE_INDEX;
			}
			case OneBulletAtATime_Revolver -> {
				return GetCurrentChamber();
			}
			default -> {
				return INVALID_FIRE_INDEX;
			}
		}
	}

	public int GetCurrentChamber()
	{
		return GetTagOrDefault("chamber", 0);
	}

	public void SetCurrentChamber(int chamber)
	{
		GetRootTag().putInt("chamber", chamber);
	}

	public void AdvanceChamber()
	{
		int chamber = GetCurrentChamber();
		chamber++;
		if(chamber >= GetMagazineSize(0))
			chamber = 0;
		SetCurrentChamber(chamber);
	}

	// --------------------------------------------------------------------------
	// RELOADS
	// --------------------------------------------------------------------------
	public boolean IsReloadInProgress()
	{
		return Gun().GetActionStack().IsReloading();
	}
	public boolean CanBeReloaded(int magIndex)
	{
		if(!IsShootAction())
			return false;

		for(int i = 0; i < GetMagazineSize(magIndex); i++)
		{
			if(GetBulletAtIndex(magIndex, i).isEmpty())
				return true;
		}
		return false;
	}
	public boolean CanPerformReloadFromAttachedInventory(int magIndex)
	{
		if(!CanBeReloaded(magIndex))
			return false;

		if(Gun().GetShooter().GetAttachedInventory() != null)
		{
			int matchSlot = FindSlotWithMatchingAmmo(magIndex, Gun().GetShooter().GetAttachedInventory());
			return matchSlot != Inventory.NOT_FOUND_INDEX;
		}

		return false;
	}
	public int FindSlotWithMatchingAmmo(int magIndex, Container inventory)
	{
		MagazineDefinition magDef = GetMagazineType(magIndex);
		//MagazineSlotSettingsDefinition magSettings = Gun().GunDef().GetMagazineSettings(inputType);
		if(magDef.IsValid())
		{
			for (int i = 0; i < inventory.getContainerSize(); i++)
			{
				ItemStack stack = inventory.getItem(i);
				if (stack.isEmpty())
					continue;
				if (stack.getItem() instanceof BulletItem bullet)
				{
					if (magDef.GetMatchingBullets().contains(bullet.Def()))
					{
						return i;
					}
				}
			}
		}
		return Inventory.NOT_FOUND_INDEX;
	}
	public void LoadOne(int magIndex, Container inventory)
	{
		if(inventory != null)
		{
			MagazineDefinition magDef = GetMagazineType(magIndex);
			if (magDef.IsValid())
			{
				for (int i = 0; i < inventory.getContainerSize(); i++)
				{
					ItemStack stack = inventory.getItem(i);
					if (stack.isEmpty())
						continue;
					if (stack.getItem() instanceof BulletItem bullet)
					{
						if (magDef.GetMatchingBullets().contains(bullet.Def()))
						{
							if (magDef.ammoLoadMode == EAmmoLoadMode.FullMag)
							{
								stack = LoadBullets(magIndex, stack);
								inventory.setItem(i, stack);
								inventory.setChanged();
							} else
							{
								// Both of these modes are LoadOne, but differ in how they select an index
								int bulletIndex = GetNextIndexToLoad(magIndex);
								stack = LoadOneBulletIntoSlot(magIndex, bulletIndex, stack);
								inventory.setItem(i, stack);
								inventory.setChanged();
								// And break because we only want to load one bullet
								break;
							}
						}
					}
				}
			}
		}
	}

	// --------------------------------------------------------------------------
	// SHOOTING
	// --------------------------------------------------------------------------
	public boolean IsShootAction() { return GetShootActionDefinition().IsValid(); }
	public ActionDefinition GetShootActionDefinition()
	{
		for(ActionDefinition def : GetActionDefinitions())
			if(def.actionType == EActionType.Shoot)
				return def;
		return ActionDefinition.Invalid;
	}
	public boolean CanShoot(int magIndex)
	{
		return ContainsAnyBullets(magIndex);
	}

	// --------------------------------------------------------------------------
	// STAT CACHE
	// --------------------------------------------------------------------------
	public void Apply(ModifierStack modStack)
	{
		Gun().Apply(modStack);

		// No need to maintain a separate cache, this is just reading from the definition
		for(ActionDefinition def : GetActionDefinitions())
			for(ModifierDefinition mod : def.modifiers)
				modStack.Apply(mod);
	}
	public float ModifyFloat(String key, float baseValue)
	{
		ModifierStack stack = new ModifierStack(key, InputType);
		Apply(stack);
		return stack.ApplyTo(baseValue);
	}
	public String ModifyString(String key, String defaultValue)
	{
		ModifierStack stack = new ModifierStack(key, InputType);
		Apply(stack);
		return stack.ApplyTo(defaultValue);
	}
	public <T extends Enum<T>> Enum<T> ModifyEnum(String key, T defaultValue, Class<T> clazz)
	{
		String modified = ModifyString(key, defaultValue.toString());
		return Enum.valueOf(clazz, modified);
	}
	public ERepeatMode RepeatMode() { return (ERepeatMode) ModifyEnum("repeat_mode", GroupDef().repeatMode, ERepeatMode.class); }
	public float RepeatDelay() { return ModifyFloat("repeat_delay", GroupDef().repeatDelay); }
	public int RepeatCount() { return Maths.Ceil(ModifyFloat("repeat_count", GroupDef().repeatCount)); }
	public float SpinUpDuration() { return ModifyFloat("spin_up_duration", GroupDef().spinUpDuration); }
	public int RoundsPerMinute() { return RepeatDelay() <= 0.00001f ? 0 : Maths.Ceil(60.0f / RepeatDelay()); }
}
