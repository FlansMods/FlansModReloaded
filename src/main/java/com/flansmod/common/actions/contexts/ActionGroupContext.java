package com.flansmod.common.actions.contexts;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.guns.elements.EActionType;
import com.flansmod.common.gunshots.ModifierStack;
import com.flansmod.common.item.BulletItem;
import com.flansmod.common.item.GunItem;
import com.flansmod.common.types.attachments.AttachmentDefinition;
import com.flansmod.common.types.attachments.EAttachmentType;
import com.flansmod.common.types.guns.elements.*;
import com.flansmod.common.types.elements.ModifierDefinition;
import com.flansmod.common.types.magazines.EAmmoConsumeMode;
import com.flansmod.common.types.magazines.EAmmoLoadMode;
import com.flansmod.common.types.magazines.MagazineDefinition;
import com.flansmod.util.Maths;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ActionGroupContext
{
	public static final ActionGroupContext INVALID = new ActionGroupContext(GunContext.INVALID, "");

	@Nonnull
	public final GunContext Gun;
	// --------------------------------------------------------------------------
	// Action Group Path
	// 		Should be of one of the following forms
	//		"shoot"
	//		"sights/ads"
	//		"sights/1/ads"
	//		"reload_primary_start"
	// --------------------------------------------------------------------------
	@Nonnull
	public final String GroupPath;
	@Nonnull
	public final ActionGroupDefinition Def;

	// Helpers
	public boolean IsAttachment()
	{
		return GroupPath.contains("/");
	}

	public boolean IsValid()
	{
		return Gun.IsValid() && !GroupPath.isEmpty();
	}

	public static ActionGroupContext CreateFrom(GunContext gunContext, String groupPath)
	{
		if (gunContext.IsValid())
			return gunContext.GetActionGroupContext(groupPath);
		return INVALID;
	}

	protected ActionGroupContext(@Nonnull GunContext gun, @Nonnull String groupPath)
	{
		Gun = gun;
		GroupPath = groupPath;
		Def = CacheGroupDef();
	}

	@Nonnull
	public static String CreateGroupPath(@Nonnull String actionGroupKey)
	{
		return actionGroupKey;
	}
	@Nonnull
	public static String CreateGroupPath(@Nonnull EAttachmentType attachmentType, @Nonnull String actionGroupKey)
	{
		return attachmentType + "/" + actionGroupKey;
	}
	@Nonnull
	public static String CreateGroupPath(@Nonnull EAttachmentType attachmentType, int attachmentIndex, @Nonnull String actionGroupKey)
	{
		return attachmentType + "/" + attachmentIndex + "/" + actionGroupKey;
	}
	@Nonnull
	public static EAttachmentType GetAttachmentType(String actionGroupPath)
	{
		String[] components = actionGroupPath.split("/");
		switch(components.length)
		{
			// Either "Barrel/shoot_origin" or "Barrel/0/shoot_origin" format
			case 2, 3 -> { return EAttachmentType.Parse(components[0]); }
			default -> { return EAttachmentType.Generic; }
		}
	}
	public static int GetAttachmentIndex(String actionGroupPath)
	{
		String[] components = actionGroupPath.split("/");
		switch(components.length)
		{
			// "Barrel/shoot_origin" format
			case 2 -> { return 0; }
			// "Barrel/0/shoot_origin" format
			case 3 -> { return Integer.parseInt(components[1]); }
			default -> { return -1; }
		}
	}
	@Nonnull
	public static String GetActionGroupKey(String actionGroupPath)
	{
		String[] components = actionGroupPath.split("/");
		if (components.length == 0)
			return "";
		// Any valid format "shoot_origin", "Barrel/shoot_origin" or "Barrel/0/shoot_origin"
		return components[components.length - 1];
	}

	@Nonnull
	public EAttachmentType GetAttachmentType() { return GetAttachmentType(GroupPath); }
	public int GetAttachmentIndex() { return GetAttachmentIndex(GroupPath); }
	@Nonnull
	public String GetActionGroupKey() { return GetActionGroupKey(GroupPath); }

	@Nonnull
	private ActionGroupDefinition CacheGroupDef()
	{
		if (IsAttachment())
		{
			String[] components = GroupPath.split("/");
			EAttachmentType attachmentType = EAttachmentType.Parse(components[0]);
			int index = 0;
			String subPath = components[1];
			if (components.length >= 3)
			{
				index = Integer.parseInt(components[1]);
				subPath = components[2];
			}
			AttachmentDefinition attachment = Gun.GetAttachmentDefinition(attachmentType, index);
			if (attachment.IsValid())
			{
				return attachment.GetActionGroup(subPath);
			}
		}
		return Gun.CacheGunDefinition().GetActionGroup(GroupPath);
	}

	// --------------------------------------------------------------------------
	// ItemStack Operations
	// --------------------------------------------------------------------------
	@Nonnull
	protected String GetRootTagKey()
	{
		// The Magazine tags will be named based on which reload we are part of
		ReloadDefinition reloadDef = Gun.GetReloadDefinitionContaining(this);
		return reloadDef != null ? reloadDef.key : GroupPath;
	}

	@Nonnull
	protected CompoundTag GetRootTag()
	{
		return Gun.GetTags(GetRootTagKey());
	}

	protected void SetRootTag(CompoundTag tags)
	{
		Gun.SetTags(GetRootTagKey(), tags);
	}

	@Nonnull
	protected CompoundTag GetMagTag(int magIndex)
	{
		if (Gun.GetItemStack().getItem() instanceof GunItem gunItem)
			return gunItem.GetMagTag(Gun.GetItemStack(), GroupPath, magIndex);
		return new CompoundTag();
	}

	protected void SetMagTag(int magIndex, CompoundTag tags)
	{
		CompoundTag updatedTags = GetRootTag().copy();
		updatedTags.put("mag_" + magIndex, tags);
		SetRootTag(updatedTags);
	}
	// --------------------------------------------------------------------------

	// --------------------------------------------------------------------------
	// MAGAZINES
	// --------------------------------------------------------------------------
	@Nonnull
	public MagazineDefinition GetMagazineType(int magIndex)
	{
		if (Gun.GetItemStack().getItem() instanceof GunItem gunItem)
		{
			return gunItem.GetMagazineType(Gun.GetItemStack(), GroupPath, magIndex);
		}
		return MagazineDefinition.INVALID;
	}

	public int GetMagazineSize(int magIndex)
	{
		return GetMagazineType(magIndex).numRounds;
	}

	public void SetMagazineType(int magIndex, @Nonnull MagazineDefinition magDef)
	{
		int newMagSize = magDef.numRounds;
		Item[] bulletsInMag = ExtractCompactStacks(magIndex);
		Item[] resultingBulletsInMag = new Item[newMagSize];
		List<ItemStack> expelBullets = new ArrayList<>();

		CompoundTag updatedTags = GetMagTag(magIndex).copy();
		updatedTags.putString("type", magDef.GetLocationString());
		SetMagTag(magIndex, updatedTags);

		int bulletCount = 0;
		for(int i = 0; i < bulletsInMag.length; i++)
		{
			if(bulletsInMag[i] != Items.APPLE)
			{
				if(bulletCount < newMagSize)
				{
					// Move this bullet into the new array
					resultingBulletsInMag[bulletCount] = bulletsInMag[i];
				}
				else
				{
					// Eject this bullet
					expelBullets.add(new ItemStack(bulletsInMag[i], 1));
				}
				bulletCount++;
			}
		}

		// Re-assign the remaining bullets to the mag
		CompactStacks(magIndex, resultingBulletsInMag);
		Gun.ExpelItems(expelBullets);
	}

	// --------------------------------------------------------------------------
	// BULLETS
	// --------------------------------------------------------------------------
	@Nonnull
	public ItemStack[] GetCombinedBulletStacks(int magIndex)
	{
		if(Gun.GetItemStack().getItem() instanceof GunItem gunItem)
			return gunItem.GetCombinedBulletStacks(Gun.GetItemStack(), GroupPath, magIndex);
		return new ItemStack[0];
	}
	@Nonnull
	public ItemStack GetBulletAtIndex(int magIndex, int bulletIndex)
	{
		if(Gun.GetItemStack().getItem() instanceof GunItem gunItem)
			return gunItem.GetBulletAtIndex(Gun.GetItemStack(), GroupPath, magIndex, bulletIndex);
		return ItemStack.EMPTY;
	}
	public int GetNumBulletsInMag(int magIndex)
	{
		if(Gun.GetItemStack().getItem() instanceof GunItem gunItem)
			return gunItem.GetNumBulletsInMag(Gun.GetItemStack(), GroupPath, magIndex);
		return 0;
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
			items[bulletIndex] = Items.APPLE;
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
			case ActionGroupContext.INVALID_FIRE_INDEX -> {
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
		boolean isCreative = Gun.GetShooter().IsCreative();
		if(bulletStack.getItem() instanceof BulletItem bulletItem)
		{
			// Extract, set, and compact stacks
			Item[] items = ExtractCompactStacks(magIndex);
			items[bulletIndex] = bulletStack.getItem();
			CompactStacks(magIndex, items);

			if(!isCreative)
				bulletStack.setCount(bulletStack.getCount() - 1);
		}
		return bulletStack;
	}
	@Nonnull
	public ItemStack LoadBullets(int magIndex, ItemStack bulletStack)
	{
		boolean isCreative = Gun.GetShooter().IsCreative();
		if(bulletStack.getItem() instanceof BulletItem bulletItem)
		{
			// Extract, set, and compact stacks
			Item[] items = ExtractCompactStacks(magIndex);
			MagazineDefinition magDef = GetMagazineType(magIndex);
			for(int i = 0; i < magDef.numRounds; i++)
			{
				if(items[i] == null || items[i] == Items.APPLE)
				{
					items[i] = bulletStack.getItem();
					if(!isCreative)
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
		if(Gun.GetItemStack().getItem() instanceof GunItem gunItem)
			return gunItem.ExtractCompactStacks(Gun.GetItemStack(), GroupPath, magIndex);
		return new Item[0];
	}
	private void CompactStacks(int magIndex, @Nonnull Item[] items)
	{
		if(items.length == 0)
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
			ItemStack previousLump = new ItemStack(
				currentlyParsing == Items.AIR ? Items.APPLE : currentlyParsing,
				i - sameSinceIndex + 1);


			CompoundTag stackTag = new CompoundTag();
			previousLump.save(stackTag);
			bulletsTag.put(Integer.toString(sameSinceIndex), stackTag);
			sameSinceIndex = i+1;
			currentlyParsing = compareAgainst;
		}

		CompoundTag updatedMagTags = GetMagTag(magIndex).copy();
		updatedMagTags.put("bullets", bulletsTag);
		SetMagTag(magIndex, updatedMagTags);
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
				if(!stack.isEmpty() && stack.getItem() != Items.APPLE)
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
						if(!stack.isEmpty() && stack.getItem() != Items.APPLE)
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
						if(stack.getItem() == Items.APPLE)
							return Integer.parseInt(key);
					}
				}
				return 0;
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
		return 0; //GetTagOrDefault("chamber", 0);
	}

	public void SetCurrentChamber(int chamber)
	{
		//GetRootTag().putInt("chamber", chamber);
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
		return Gun.GetActionStack().IsReloading();
	}
	public boolean CanBeReloaded(int magIndex)
	{
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

		if(Gun.GetShooter().GetAttachedInventory() != null)
		{
			int matchSlot = FindSlotWithMatchingAmmo(magIndex, Gun.GetShooter().GetAttachedInventory());
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
								if(bulletIndex != Inventory.NOT_FOUND_INDEX)
								{
									stack = LoadOneBulletIntoSlot(magIndex, bulletIndex, stack);
									inventory.setItem(i, stack);
									inventory.setChanged();
								}
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
		for(ActionDefinition def : Def.actions)
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
		Gun.Apply(modStack);

		// No need to maintain a separate cache, this is just reading from the definition
		for(ActionDefinition def : Def.actions)
			for(ModifierDefinition mod : def.modifiers)
				modStack.Modify(mod, 1.0f);
	}
	public float ModifyFloat(String key, float baseValue)
	{
		ModifierStack stack = new ModifierStack(key, GroupPath);
		Apply(stack);
		return stack.ApplyTo(baseValue);
	}
	public boolean ModifyBoolean(String key, boolean baseValue)
	{
		ModifierStack stack = new ModifierStack(key, GroupPath);
		Apply(stack);
		return stack.ApplyTo(baseValue);
	}
	public String ModifyString(String key, String defaultValue)
	{
		ModifierStack stack = new ModifierStack(key, GroupPath);
		Apply(stack);
		return stack.ApplyTo(defaultValue);
	}
	public <T extends Enum<T>> Enum<T> ModifyEnum(String key, T defaultValue, Class<T> clazz)
	{
		String modified = ModifyString(key, defaultValue.toString());
		return Enum.valueOf(clazz, modified);
	}
	public ERepeatMode RepeatMode() { return (ERepeatMode) ModifyEnum(ModifierDefinition.STAT_GROUP_REPEAT_MODE, Def.repeatMode, ERepeatMode.class); }
	public float RepeatDelaySeconds() { return ModifyFloat(ModifierDefinition.STAT_GROUP_REPEAT_DELAY, Def.repeatDelay); }
	public float RepeatDelayTicks() { return RepeatDelaySeconds() * 20.0f; }
	public int RepeatCount() { return Maths.Ceil(ModifyFloat(ModifierDefinition.STAT_GROUP_REPEAT_COUNT, Def.repeatCount)); }
	public float SpinUpDuration() { return ModifyFloat(ModifierDefinition.STAT_GROUP_SPIN_UP_DURATION, Def.spinUpDuration); }
	public int RoundsPerMinute() { return RepeatDelaySeconds() <= 0.00001f ? 0 : Maths.Ceil(60.0f / RepeatDelaySeconds()); }
	public float Loudness() { return ModifyFloat(ModifierDefinition.STAT_GROUP_LOUDNESS, Def.loudness); }
	public float Volume() { return ModifyFloat(ModifierDefinition.STAT_GROUP_LOUDNESS, 1.0f); }
	public float Pitch() { return ModifyFloat(ModifierDefinition.STAT_SOUND_PITCH, 1.0f); }

	@Override
	public String toString()
	{
		return Gun + ":" + GroupPath;
	}

	// UTIL

	public void Save(CompoundTag tags)
	{
		CompoundTag gunTags = new CompoundTag();
		Gun.Save(gunTags);
		tags.put("gun", gunTags);
		tags.putInt("groupHash", GroupPath.hashCode());
	}

	public static ActionGroupContext Load(CompoundTag tags, boolean client)
	{
		int groupPathHash = tags.getInt("groupHash");
		GunContext gunContext = GunContext.Load(tags.getCompound("gun"), client);
		if(gunContext.IsValid())
			return gunContext.GetActionGroupContextByHash(groupPathHash);

		// If we don't have a good gun context, there's no point making an action group context
		return ActionGroupContext.INVALID;
	}
}
