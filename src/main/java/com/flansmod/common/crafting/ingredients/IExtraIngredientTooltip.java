package com.flansmod.common.crafting.ingredients;

import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import java.util.List;

public interface IExtraIngredientTooltip
{
	void GenerateTooltip(@Nonnull List<Component> lines, boolean advanced);
}
