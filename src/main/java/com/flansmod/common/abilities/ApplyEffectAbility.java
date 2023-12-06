package com.flansmod.common.abilities;

import com.flansmod.common.actions.contexts.GunContext;
import com.flansmod.common.types.abilities.AbilityDefinition;
import com.flansmod.util.Maths;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ApplyEffectAbility extends StackableAbility
{
	@Nullable
	public final MobEffect Effect;

	public ApplyEffectAbility(AbilityDefinition def, int level)
	{
		super(def, level);
		Effect = ForgeRegistries.MOB_EFFECTS.getValue(def.Location);
	}

	@Override
	public void Trigger(@Nonnull GunContext gun, @Nullable HitResult hit)
	{
		super.Trigger(gun, hit);
		if(Effect != null)
		{
			for (Entity entity : GetEntityTargets(gun, hit))
			{
				if (entity instanceof LivingEntity living)
				{
					living.addEffect(
						new MobEffectInstance(Effect, GetDurationTicks(), Maths.Ceil(GetAmount())),
						gun.GetShooter().Owner());
				}
			}
		}
	}

	@Override
	public void End(@Nonnull GunContext gun)
	{

	}

	@Override
	public void Tick()
	{

	}
}
