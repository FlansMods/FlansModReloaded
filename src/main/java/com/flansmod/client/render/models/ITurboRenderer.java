package com.flansmod.client.render.models;


import com.flansmod.client.render.RenderContext;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ITurboRenderer
{
	void RenderDirect(@Nullable Entity heldByEntity, @Nullable ItemStack stack, @Nonnull RenderContext renderContext);
}
