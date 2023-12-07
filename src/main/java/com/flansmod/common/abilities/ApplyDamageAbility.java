package com.flansmod.common.abilities;

import com.flansmod.common.actions.contexts.GunContext;
import com.flansmod.common.types.abilities.AbilityDefinition;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class ApplyDamageAbility extends InstantAbility
{
	public ApplyDamageAbility(AbilityDefinition def, int level)
	{
		super(def, level);
	}

	@Override
	public void Trigger(@Nonnull GunContext gun, @Nullable HitResult hit)
	{
		super.Trigger(gun, hit);
		switch(Def.targetType)
		{
			case Owner -> {
				gun.GetShooter().Owner().hurt(DamageSource.MAGIC, GetIntensity(gun));
			}
			case Shooter -> {
				gun.GetShooter().Entity().hurt(DamageSource.MAGIC, GetIntensity(gun));
			}
			case ShotEntity -> {
				if(hit instanceof EntityHitResult entityHit)
					entityHit.getEntity().hurt(DamageSource.MAGIC, GetIntensity(gun));
			}
		}
	}
}
