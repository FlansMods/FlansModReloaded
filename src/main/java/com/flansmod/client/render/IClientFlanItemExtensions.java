package com.flansmod.client.render;

import com.flansmod.client.render.models.ITurboRenderer;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import javax.annotation.Nullable;

public interface IClientFlanItemExtensions extends IClientItemExtensions
{
	@Nullable
	default ITurboRenderer GetTurboRenderer() { return (ITurboRenderer) getCustomRenderer(); }
}
