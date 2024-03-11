package com.flansmod.common.actions.nodes;

import com.flansmod.common.actions.ActionGroupInstance;
import com.flansmod.common.actions.ActionInstance;
import com.flansmod.common.types.Constants;
import com.flansmod.common.types.elements.ModifierDefinition;
import com.flansmod.common.types.guns.elements.ActionDefinition;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

public class AttachEffectAction extends ActionInstance
{
	public int TicksSinceTrigger = 0;

	public AttachEffectAction(@Nonnull ActionGroupInstance group, @Nonnull ActionDefinition def)
	{
		super(group, def);
	}

	@Override
	public boolean PropogateToServer() { return false; }
	@Override
	public void OnTriggerClient(int triggerIndex)
	{
		TicksSinceTrigger = 0;
	}
	@Override
	public void OnTriggerServer(int triggerIndex) { }

	@Override
	public void OnTickClient()
	{
		TicksSinceTrigger++;
	}

	@Nonnull
	public String GetRelativeAPName() { return Group.Context.GetSibling(AttachPoint()); }

	@Nonnull
	public String AttachPoint() { return Group.Context.ModifyString(Constants.FLASH_ATTACH_POINT, "shoot_origin"); }
	@Nonnull
	public ResourceLocation EffectModelLocation() { return new ResourceLocation(Group.Context.ModifyString(Constants.FLASH_MODEL, "flansmod:effects/muzzle_flash_small")); }
	@Nonnull
	public ResourceLocation EffectTextureLocation() { return new ResourceLocation(Group.Context.ModifyString(Constants.FLASH_TEXTURE, "flansmod:textures/skins/muzzle_flash_small.png")); }
}
