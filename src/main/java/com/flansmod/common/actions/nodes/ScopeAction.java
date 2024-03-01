package com.flansmod.common.actions.nodes;

import com.flansmod.common.actions.ActionGroupInstance;
import com.flansmod.common.actions.contexts.GunContext;
import com.flansmod.common.types.Constants;
import com.flansmod.common.types.guns.elements.ActionDefinition;
import com.flansmod.common.types.elements.ModifierDefinition;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

public class ScopeAction extends AimDownSightAction
{
	public ScopeAction(@Nonnull ActionGroupInstance group, @Nonnull ActionDefinition def)
	{
		super(group, def);
	}

	public boolean ApplyOverlay()
	{
		return true;
	}

	public ResourceLocation GetOverlayLocation()
	{
		String loc = ZoomOverlay();
		if(loc.contains(":"))
		{
			String namespace = loc.substring(0, loc.indexOf(":"));
			String id = loc.substring(loc.indexOf(":") + 1);
			if(id.contains("textures/gui/"))
				return new ResourceLocation(namespace, id + ".png");
			else if(id.contains("gui/"))
				return new ResourceLocation(namespace, "textures/" + id + ".png");
			return new ResourceLocation(namespace, "textures/gui/" + id + ".png");
		}
		return new ResourceLocation("flansmod", "textures/gui/" + loc + ".png");
	}
	public String ZoomOverlay() { return Group.Context.ModifyString(Constants.STAT_ZOOM_SCOPE_OVERLAY, Def.scopeOverlay); }

	@Override
	public boolean ShouldRender(GunContext context)
	{
		return false;
	}
}
