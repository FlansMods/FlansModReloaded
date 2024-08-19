package com.flansmod.client.render.vehicles;

import com.flansmod.client.render.IClientFlanItemExtensions;
import com.flansmod.client.render.models.FlansModelRegistry;
import com.flansmod.common.item.VehicleItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import javax.annotation.Nonnull;

public class VehicleItemClientExtension implements IClientFlanItemExtensions
{
	@Nonnull
	public final VehicleItem Item;
	@Nonnull
	public VehicleItemRenderer ItemRenderer;

	protected VehicleItemClientExtension(@Nonnull VehicleItem item)
	{
		Item = item;
		ItemRenderer = new VehicleItemRenderer(item);
	}

	@Nonnull
	public ResourceLocation GetLocation() { return Item.DefinitionLocation;	}
	@Override
	@Nonnull
	public VehicleItemRenderer getCustomRenderer() { return ItemRenderer; }
	@Nonnull
	public static VehicleItemClientExtension of(@Nonnull VehicleItem item)
	{
		VehicleItemClientExtension clientExt = new VehicleItemClientExtension(item);
		FlansModelRegistry.PreRegisterModel(clientExt::GetLocation);
		return clientExt;
	}

}
