package com.flansmod.common.item;

import com.flansmod.client.render.armour.ArmourItemClientExtension;
import com.flansmod.client.render.armour.ArmourItemRenderer;
import com.flansmod.client.render.guns.AttachmentItemRenderer;
import com.flansmod.common.FlansMod;
import com.flansmod.common.types.JsonDefinition;
import com.flansmod.common.types.armour.ArmourDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class ArmourItem extends FlanItem implements Equipable
{
	@Override
	public ArmourDefinition Def() { return FlansMod.ARMOURS.Get(DefinitionLocation); }

	public ArmourItem(@Nonnull ResourceLocation defLoc, @Nonnull Properties properties)
	{
		super(defLoc, properties);
	}

	@Override
	public boolean CanBeCraftedFromParts() { return true; }
	@Override
	public boolean ShouldRenderAsIcon(@Nonnull ItemDisplayContext transformType) { return true; }

	@Override
	@OnlyIn(Dist.CLIENT)
	public void initializeClient(@Nonnull Consumer<IClientItemExtensions> consumer)
	{
		consumer.accept(ArmourItemClientExtension.of(this));
	}

	// Random parameter overrides
	public boolean isEnchantable(@Nonnull ItemStack i)
	{
		return Def().enchantable;
	}

	@Override
	@Nonnull
	public EquipmentSlot getEquipmentSlot()
	{
		return Def().armourType.ToSlot();
	}
}
