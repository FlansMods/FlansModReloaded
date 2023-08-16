package com.flansmod.common.crafting;

import com.flansmod.common.FlansMod;
import com.flansmod.common.actions.EActionInput;
import com.flansmod.common.item.FlanItem;
import com.flansmod.common.item.GunItem;
import com.flansmod.common.types.crafting.WorkbenchDefinition;
import com.flansmod.common.types.elements.PaintableDefinition;
import com.flansmod.common.types.elements.PaintjobDefinition;
import com.flansmod.common.types.guns.GunDefinition;
import com.flansmod.common.types.magazines.MagazineDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Clearable;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;
import java.util.Set;

public class WorkbenchBlockEntity extends BlockEntity implements MenuProvider, Clearable
{
	public final WorkbenchDefinition Def;
	public final RestrictedContainer GunContainer;
	public final RestrictedContainer PaintCanContainer;
	public final RestrictedContainer MagUpgradeContainer;
	public final RestrictedContainer CraftingInputContainer;
	public final RestrictedContainer CraftingOutputContainer;
	public final RestrictedContainer MaterialContainer;
	public final RestrictedContainer BatteryContainer;
	public final RestrictedContainer FuelContainer;
	private EnergyStorage EnergyStorage;
	private static final double INTERACT_RANGE = 5.0d;

	public static final int DATA_LIT_TIME = 0;
	public static final int DATA_LIT_DURATION = 1;
	public static final int DATA_FORGE_ENERGY = 2;
	public int LitTime = 0;
	public int LitDuration = 0;

	protected final ContainerData DataAccess = new ContainerData()
	{
		@Override
		public int get(int id)
		{
			switch (id)
			{
				case DATA_LIT_TIME -> { return LitTime; }
				case DATA_LIT_DURATION -> { return LitDuration; }
				case DATA_FORGE_ENERGY -> { return EnergyStorage == null ? 0 : EnergyStorage.getEnergyStored(); }
				default -> { return 0; }
			}
		}

		@Override
		public void set(int id, int value)
		{
			switch (id)
			{
				case DATA_LIT_TIME -> { LitTime = value; }
				case DATA_LIT_DURATION -> { LitDuration = value; }
				case DATA_FORGE_ENERGY -> { if(EnergyStorage != null) EnergyStorage.receiveEnergy(value - EnergyStorage.getEnergyStored(), false); }
			}
		}

		@Override
		public int getCount() { return 3; }
	};

	public static class WorkbenchBlockEntityTypeHolder implements BlockEntityType.BlockEntitySupplier<WorkbenchBlockEntity>
	{
		private final ResourceLocation DefLoc;

		public WorkbenchBlockEntityTypeHolder(ResourceLocation defLoc)
		{
			DefLoc = defLoc;
		}

		@Override
		public WorkbenchBlockEntity create(BlockPos pos, BlockState state)
		{
			return new WorkbenchBlockEntity(DefLoc, pos, state);
		}

		public BlockEntityType<WorkbenchBlockEntity> CreateType()
		{
			return new BlockEntityType<WorkbenchBlockEntity>(this, Set.of(), null)
			{
				@Override
				public boolean isValid(BlockState state) { return state.getBlock() instanceof WorkbenchBlock; }
			};
		}
	}

	public WorkbenchBlockEntity(ResourceLocation defLoc, BlockPos pos, BlockState state)
	{
		super(ForgeRegistries.BLOCK_ENTITY_TYPES.getValue(defLoc), pos, state);
		Def = FlansMod.WORKBENCHES.Get(defLoc);

		if(Def.gunModifying.isActive)
		{
			GunContainer = new RestrictedContainer(
				this,
				INTERACT_RANGE,
				1,
				1,
				(stack) -> { return stack.getItem() instanceof GunItem; }
			);
			PaintCanContainer = new RestrictedContainer(
				this,
				INTERACT_RANGE,
				1,
				64,
				(stack) -> { return stack.getItem() == FlansMod.RAINBOW_PAINT_CAN_ITEM.get(); }
			);
			MagUpgradeContainer = new RestrictedContainer(
				this,
				INTERACT_RANGE,
				1,
				64,
				(stack) -> { return stack.getItem() == FlansMod.MAG_UPGRADE_ITEM.get(); }
			);
		}
		else
		{
			GunContainer = new RestrictedContainer(this);
			PaintCanContainer = new RestrictedContainer(this);
			MagUpgradeContainer = new RestrictedContainer(this);
		}

		if(Def.gunCrafting.isActive)
		{
			CraftingInputContainer = new RestrictedContainer(
				this,
				INTERACT_RANGE,
				Def.gunCrafting.GetMaxInputSlots(),
				64,
				(stack) -> { return true; } );
			CraftingOutputContainer = new RestrictedContainer(
				this,
				INTERACT_RANGE,
				1,
				64,
				(stack) -> { return false; } );
		}
		else
		{
			CraftingInputContainer = new RestrictedContainer(this);
			CraftingOutputContainer = new RestrictedContainer(this);
		}

		if(Def.itemHolding.slots.length > 0)
		{
			MaterialContainer = new RestrictedContainer(
				this,
				INTERACT_RANGE,
				Def.itemHolding.slots.length,
				Def.itemHolding.maxStackSize,
				(stack) -> { return true; }
			);
		}
		else MaterialContainer = new RestrictedContainer(this);

		if(Def.energy.maxFE > 0)
		{
			EnergyStorage = new EnergyStorage(Def.energy.maxFE);
			BatteryContainer = new RestrictedContainer(
				this,
				INTERACT_RANGE,
				Def.energy.numBatterySlots,
				Def.energy.batterySlotStackSize,
				(stack) -> { return stack.getCapability(ForgeCapabilities.ENERGY).isPresent(); }
			);
			FuelContainer = new RestrictedContainer(
				this,
				INTERACT_RANGE,
				Def.energy.numSolidFuelSlots,
				64,
				(stack) -> { return ForgeHooks.getBurnTime(stack, RecipeType.SMELTING) > 0.0f; }
			);
		}
		else
		{
			BatteryContainer = new RestrictedContainer(this);
			FuelContainer = new RestrictedContainer(this);
		}
	}

	@Override
	protected void saveAdditional(CompoundTag tags)
	{
		super.saveAdditional(tags);
		tags.put("gun", GunContainer.save(new CompoundTag()));
		tags.put("paintcans", PaintCanContainer.save(new CompoundTag()));
		tags.put("magupgrades", MagUpgradeContainer.save(new CompoundTag()));
		tags.put("input", CraftingInputContainer.save(new CompoundTag()));
		tags.put("output", CraftingOutputContainer.save(new CompoundTag()));
		tags.put("materials", MaterialContainer.save(new CompoundTag()));
		tags.put("battery", BatteryContainer.save(new CompoundTag()));
		tags.put("fuel", FuelContainer.save(new CompoundTag()));
		if(EnergyStorage != null)
			tags.put("energy", EnergyStorage.serializeNBT());
	}

	@Override
	public void load(CompoundTag tags)
	{
		super.load(tags);
		GunContainer.load(tags.getCompound("gun"));
		PaintCanContainer.load(tags.getCompound("paintcans"));
		MagUpgradeContainer.load(tags.getCompound("magupgrades"));
		CraftingInputContainer.load(tags.getCompound("input"));
		CraftingOutputContainer.load(tags.getCompound("output"));
		MaterialContainer.load(tags.getCompound("materials"));
		BatteryContainer.load(tags.getCompound("battery"));
		FuelContainer.load(tags.getCompound("fuel"));
		if(EnergyStorage != null)
			EnergyStorage.deserializeNBT(tags.getCompound("energy"));
	}

	@Override
	public Component getDisplayName()
	{
		return null;
	}

	@Override
	public void clearContent()
	{
		GunContainer.clearContent();
		PaintCanContainer.clearContent();
		MagUpgradeContainer.clearContent();
		CraftingInputContainer.clearContent();
		CraftingOutputContainer.clearContent();
		MaterialContainer.clearContent();
		BatteryContainer.clearContent();
		FuelContainer.clearContent();
	}


	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int containerID, Inventory inventory, Player player)
	{
		return new WorkbenchMenu(containerID,
			inventory,
			Def,
			GunContainer,
			PaintCanContainer,
			MagUpgradeContainer,
			CraftingInputContainer,
			CraftingOutputContainer,
			MaterialContainer,
			BatteryContainer,
			FuelContainer,
			DataAccess);
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, WorkbenchBlockEntity workbench)
	{
		if(workbench.EnergyStorage != null)
		{
			if (workbench.LitTime > 0)
			{
				workbench.LitTime--;
				workbench.EnergyStorage.receiveEnergy(workbench.Def.energy.solidFEPerFuelTime, false);
				if (workbench.LitTime <= 0)
				{
					workbench.LitDuration = 0;
					setChanged(level, pos, state);
				}
			}

			if (workbench.LitTime <= 0 && workbench.EnergyStorage.getEnergyStored() < workbench.EnergyStorage.getMaxEnergyStored())
			{
				if (workbench.FuelContainer.getContainerSize() > 0)
				{
					int burnTime = ForgeHooks.getBurnTime(workbench.FuelContainer.getItem(0), RecipeType.SMELTING);
					if (burnTime > 0)
					{
						workbench.FuelContainer.removeItem(0, 1);
						workbench.LitDuration = burnTime;
						workbench.LitTime = burnTime;
						setChanged(level, pos, state);
					}
				}
			}
		}
	}

	public void CraftGun(Player player, int gunIndex)
	{
		// TODO: Get neighbours
		CraftGun(player, new Container[] { MaterialContainer }, Def, gunIndex);
	}
	public static void CraftGun(Player player, Container[] attachedContainers, WorkbenchDefinition def, int gunIndex)
	{
		// TODO: Survival checks, recipes, unlocks

		if(def.gunCrafting.isActive)
		{
			//def.gunCrafting.pages
		}

		//if(gunContainer.getContainerSize() > 0 && !gunContainer.getItem(0).isEmpty() && gunContainer.getItem(0).getItem() instanceof FlanItem flanItem)
		//{
		//	ItemStack gunStack = gunContainer.getItem(0);
		//	PaintableDefinition paintableDefinition = flanItem.GetPaintDef(gunStack);
		//	if (paintableDefinition.paintjobs.length > 0)
		//	{
		//		if(skinIndex == 0)
		//		{
		//			flanItem.SetPaintjobName(gunStack, "default");
		//		}
		//		else
		//		{
		//			flanItem.SetPaintjobName(gunStack, paintableDefinition.paintjobs[skinIndex - 1].textureName);
		//		}
		//	}
		//}
	}

	public static int GetPaintUpgradeCost(Container gunContainer, int paintIndex)
	{
		if (gunContainer.getContainerSize() <= 0)
			return 0;
		if(paintIndex == 0)
			return 0;

		if (gunContainer.getItem(0).getItem() instanceof FlanItem flanItem)
		{
			PaintableDefinition paintableDefinition = flanItem.GetPaintDef();
			if (paintableDefinition.IsValid())
			{
				if (0 <= paintIndex - 1 && paintIndex - 1 < paintableDefinition.paintjobs.length)
				{
					return paintableDefinition.paintjobs[paintIndex - 1].paintBucketsRequired;
				}
			}
		}
		return 0;
	}
	public static boolean CanPaintGun(Player player, Container gunContainer, Container paintCanContainer, int skinIndex)
	{
		// Check some obvious errors
		if(skinIndex < 0)
			return false;
		if (gunContainer.getContainerSize() <= 0)
			return false;
		if(gunContainer.getItem(0).isEmpty())
			return false;
		if(gunContainer.getItem(0).getItem() instanceof FlanItem flanItem)
		{
			// We can always revert to the default skin, no matter what, and for free.
			if(skinIndex == 0)
				return true;

			ItemStack gunStack = gunContainer.getItem(0);

			// Check if the item is paintable and our selection makes sense
			PaintableDefinition paintableDefinition = flanItem.GetPaintDef();
			if (!paintableDefinition.IsValid())
				return false;
			if (skinIndex >= paintableDefinition.paintjobs.length + 1)
				return false;
			PaintjobDefinition paintjobDefinition = paintableDefinition.paintjobs[skinIndex - 1];
			if (paintjobDefinition == null)
				return false;

			// If this skin has entitlements, check them out
			if (!paintjobDefinition.entitlementKey.isEmpty())
			{
				// TODO: Entitlement checks
			}

			// Creative players get to skip the cost checks
			if (player.isCreative())
				return true;

			// Now we just need to check the cost
			int paintCost = paintjobDefinition.paintBucketsRequired;
			if (paintCanContainer.getItem(0).getCount() < paintCost)
				return false;

			// All checks passed, let's paint!
			return true;
		}
		return false;
	}

	public void PaintGun(Player player, int skinIndex) { PaintGun(player, GunContainer, PaintCanContainer, skinIndex); }
	public static void PaintGun(Player player, Container gunContainer, Container paintCanContainer, int skinIndex)
	{
		if(CanPaintGun(player, gunContainer, paintCanContainer, skinIndex))
		{
			if (gunContainer.getContainerSize() > 0 && !gunContainer.getItem(0).isEmpty() && gunContainer.getItem(0).getItem() instanceof FlanItem flanItem)
			{
				ItemStack gunStack = gunContainer.getItem(0);
				PaintableDefinition paintableDefinition = flanItem.GetPaintDef();
				if (paintableDefinition.IsValid())
				{
					if (skinIndex == 0)
					{
						flanItem.SetPaintjobName(gunStack, "default");
					} else
					{
						PaintjobDefinition paintjobDefinition = paintableDefinition.paintjobs[skinIndex - 1];
						paintCanContainer.getItem(0).setCount(paintCanContainer.getItem(0).getCount() - paintjobDefinition.paintBucketsRequired);
						flanItem.SetPaintjobName(gunStack, paintjobDefinition.textureName);
					}
				}
			}
		}
	}

	public static int GetMagUpgradeCost(Container gunContainer, int magIndex)
	{
		if (gunContainer.getContainerSize() <= 0)
			return 0;

		if (gunContainer.getItem(0).getItem() instanceof GunItem gunItem)
		{
			List<MagazineDefinition> mags = gunItem.Def().primaryMagazines.GetMatchingMagazines();
			if (0 <= magIndex && magIndex < mags.size())
			{
				return gunItem.Def().primaryMagazines.baseCostToSwap + mags.get(magIndex).upgradeCost;
			}
			return gunItem.Def().primaryMagazines.baseCostToSwap;
		}
		return 0;
	}
	public static boolean CanSelectMagazine(Player player, Container gunContainer, Container magUpgradeContainer, int magIndex)
	{
		// Check some obvious errors
		if(magIndex < 0)
			return false;
		if (gunContainer.getContainerSize() <= 0)
			return false;
		if(gunContainer.getItem(0).isEmpty())
			return false;
		if(gunContainer.getItem(0).getItem() instanceof GunItem gunItem)
		{
			ItemStack gunStack = gunContainer.getItem(0);

			// Check if the item has mag options and our selection makes sense
			List<MagazineDefinition> mags = gunItem.Def().primaryMagazines.GetMatchingMagazines();
			if(magIndex >= mags.size())
				return false;

			MagazineDefinition mag = mags.get(magIndex);
			if(!mag.IsValid())
				return false;

			// Creative players get to skip the cost checks
			if (player.isCreative())
				return true;

			// Now we just need to check the cost
			int magCost = mag.upgradeCost + gunItem.Def().primaryMagazines.baseCostToSwap;
			if (magUpgradeContainer.getItem(0).getCount() < magCost)
				return false;

			// All checks passed, let's swap the mags!
			return true;
		}
		return false;
	}
	public void SelectMagazine(Player player, int magIndex) { SelectMagazine(player, GunContainer, MagUpgradeContainer, magIndex); }
	public static void SelectMagazine(Player player, Container gunContainer, Container magUpgradeContainer, int magIndex)
	{
		if(CanSelectMagazine(player, gunContainer, magUpgradeContainer, magIndex))
		{
			if (gunContainer.getContainerSize() > 0 && !gunContainer.getItem(0).isEmpty() && gunContainer.getItem(0).getItem() instanceof GunItem gunItem)
			{
				ItemStack gunStack = gunContainer.getItem(0);
				List<MagazineDefinition> mags = gunItem.Def().primaryMagazines.GetMatchingMagazines();
				if (0 <= magIndex && magIndex < mags.size())
				{
					int magCost = mags.get(magIndex).upgradeCost + gunItem.Def().primaryMagazines.baseCostToSwap;
					magUpgradeContainer.getItem(0).setCount(magUpgradeContainer.getItem(0).getCount() - magCost);
					gunItem.SetMagazineType(gunStack, EActionInput.PRIMARY, 0, mags.get(magIndex));
				}
				else
				{
					FlansMod.LOGGER.warn(player.getName().getString() + " tried to set mag index " + magIndex + " on gun (" + gunItem.Def().Location + ") with only " + mags.size() + " mag options");
				}
			}
		}
	}
}
