package com.flansmod.mixin;

import com.flansmod.common.actions.ActionGroupInstance;
import com.flansmod.common.actions.ActionInstance;
import com.flansmod.common.actions.ActionStack;
import com.flansmod.common.actions.contexts.GunContext;
import com.flansmod.common.actions.contexts.ShooterContext;
import com.flansmod.common.actions.nodes.AimDownSightAction;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MouseHandler.class)
public class MixinMouseHandler
{
	@Redirect(
		method = "turnPlayer()V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/player/LocalPlayer;isScoping()Z"))
	private boolean isScoping(LocalPlayer player)
	{
		if(player != null)
		{
			ShooterContext shooterContext = ShooterContext.of(player);
			if(shooterContext.IsValid())
			{
				for(GunContext gunContext : shooterContext.GetAllGunContexts())
				{
					ActionStack actionStack = gunContext.GetActionStack();
					for(ActionGroupInstance actionGroupInstance : actionStack.GetActiveActionGroups())
					{
						for(ActionInstance actionInstance : actionGroupInstance.GetActions())
						{
							if(actionInstance instanceof AimDownSightAction adsAction)
							{
								return true;
							}
						}
					}
				}
			}
			return player.isScoping();
		}
		return false;
	}
}
