package com.flansmod.client.render.bullets;

import com.flansmod.client.render.FlanItemModelRenderer;
import com.flansmod.client.render.IClientFlanItemExtensions;
import com.flansmod.client.render.models.FlansModelRegistry;
import com.flansmod.client.render.models.ITurboRenderer;
import com.flansmod.common.item.BulletItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BulletItemClientExtension implements IClientFlanItemExtensions
{
	@Nonnull
	public final BulletItem Item;
	@Nonnull
	public BulletItemRenderer ItemRenderer;

	protected BulletItemClientExtension(@Nonnull BulletItem item)
	{
		Item = item;
		ItemRenderer = new BulletItemRenderer(item);
	}

	@Nonnull
	public ResourceLocation GetLocation() { return Item.DefinitionLocation;	}
	@Override
	@Nonnull
	public BulletItemRenderer getCustomRenderer() { return ItemRenderer; }
	@Nonnull
	public static BulletItemClientExtension of(@Nonnull BulletItem item)
	{
		BulletItemClientExtension clientExt = new BulletItemClientExtension(item);
		FlansModelRegistry.PreRegisterModel(clientExt::GetLocation);
		return clientExt;
	}

}
