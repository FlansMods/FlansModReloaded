package com.flansmod.client.render.armour;

import com.flansmod.client.render.IClientFlanItemExtensions;
import com.flansmod.client.render.models.FlansModelRegistry;
import com.flansmod.common.item.ArmourItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import javax.annotation.Nonnull;

public class ArmourItemClientExtension implements IClientFlanItemExtensions
{
	@Nonnull
	public final ArmourItem Item;
	@Nonnull
	public ArmourItemRenderer ItemRenderer;

	protected ArmourItemClientExtension(@Nonnull ArmourItem item)
	{
		Item = item;
		ItemRenderer = new ArmourItemRenderer(item);
	}

	@Nonnull
	public ResourceLocation GetLocation() { return Item.DefinitionLocation;	}
	@Override
	@Nonnull
	public ArmourItemRenderer getCustomRenderer() { return ItemRenderer; }
	@Nonnull
	public static ArmourItemClientExtension of(@Nonnull ArmourItem item)
	{
		ArmourItemClientExtension clientExt = new ArmourItemClientExtension(item);
		FlansModelRegistry.PreRegisterModel(clientExt::GetLocation);
		return clientExt;
	}

}