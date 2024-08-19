package com.flansmod.common.types;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.Lazy;

import javax.annotation.Nonnull;

public record LazyDefinition<TDefType extends JsonDefinition>(@Nonnull ResourceLocation Loc,
															  @Nonnull Lazy<TDefType> DefGetter)
{
	@Nonnull
	public static <TDefType extends JsonDefinition> LazyDefinition<TDefType> of(@Nonnull ResourceLocation loc, @Nonnull Definitions<TDefType> definitionLookup)
	{
		return new LazyDefinition<>(loc, Lazy.of(() -> definitionLookup.Get(loc)));
	}
}
