package com.flansmod.common.abilities;

import com.flansmod.client.FlansModClient;
import com.flansmod.common.FlansMod;
import com.flansmod.common.actions.contexts.GunContext;
import com.flansmod.common.actions.contexts.TargetsContext;
import com.flansmod.common.actions.contexts.TriggerContext;
import com.flansmod.common.types.Constants;
import com.flansmod.common.types.JsonDefinition;
import com.flansmod.common.types.abilities.elements.AbilityEffectDefinition;
import com.flansmod.util.Maths;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AbilityEffectApplyDecal implements IAbilityEffect
{
	@Nonnull
	private final ResourceLocation DecalTexture;
	private final StatHolder DecalDuration;
	private final boolean RandomizeRotation;

	public AbilityEffectApplyDecal(@Nonnull AbilityEffectDefinition def)
	{
		DecalTexture = new ResourceLocation(def.ModifyString(Constants.DECAL_TEXTURE, "")).withPrefix("textures/").withSuffix(".png");
		DecalDuration = new StatHolder(Constants.DECAL_DURATION, def);
		RandomizeRotation = def.ModifyBoolean(Constants.DECAL_RANDOMIZE_ROTATION, true);

		if(JsonDefinition.IsValidLocation(DecalTexture))
			FlansMod.LOGGER.error("Could not resolve decal location " + DecalTexture);
	}

	@Override
	public void TriggerClient(@Nonnull GunContext gun, @Nonnull TriggerContext trigger, @Nonnull TargetsContext targets, @Nullable AbilityStack stacks)
	{
		Direction normal = trigger.Hit instanceof BlockHitResult blockHit
			? blockHit.getDirection() : Direction.UP;

		float duration = DecalDuration.Get(gun, stacks);

		targets.ForEachPosition((triggerAt) -> {
			FlansModClient.DECAL_RENDERER.AddDecal(
				DecalTexture,
				triggerAt,
				normal,
				RandomizeRotation ? (gun.GetRandom().nextFloat() * 360.0f) : 0f,
				Maths.Ceil(duration * 20f));
		});
	}
}
