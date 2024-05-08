package com.flansmod.common.item;

import com.flansmod.client.render.guns.GunItemClientExtension;
import com.flansmod.client.render.vehicles.VehicleItemClientExtension;
import com.flansmod.common.FlansMod;
import com.flansmod.common.types.JsonDefinition;
import com.flansmod.common.types.LazyDefinition;
import com.flansmod.common.types.vehicles.VehicleDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class VehicleItem extends FlanItem
{
	@Nonnull
	public final LazyDefinition<VehicleDefinition> DefRef;

	@Override
	public JsonDefinition Def()
	{
		return DefRef.DefGetter().get();
	}

	public VehicleItem(@Nonnull ResourceLocation defLoc, @Nonnull Item.Properties props)
	{
		super(defLoc, props);
		DefRef = LazyDefinition.of(defLoc, FlansMod.VEHICLES);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void initializeClient(@Nonnull Consumer<IClientItemExtensions> consumer)
	{
		consumer.accept(VehicleItemClientExtension.of(this));
	}
}
