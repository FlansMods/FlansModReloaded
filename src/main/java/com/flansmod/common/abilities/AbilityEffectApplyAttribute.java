package com.flansmod.common.abilities;

import com.flansmod.common.actions.contexts.GunContext;
import com.flansmod.common.actions.contexts.TriggerContext;
import com.flansmod.common.actions.contexts.TargetsContext;
import com.flansmod.common.types.Constants;
import com.flansmod.common.types.abilities.elements.AbilityEffectDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class AbilityEffectApplyAttribute implements IAbilityEffect
{
	public final Attribute Attrib;
	public final UUID IdentifyingKey;

	@Nonnull
	private final StatHolder AttributeMultiplier;

	public AbilityEffectApplyAttribute(@Nonnull AbilityEffectDefinition def)
	{
		Attrib = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(def.ModifyString(Constants.STAT_ATTRIBUTE_ID, "")));
		IdentifyingKey = UUID.randomUUID();

		AttributeMultiplier = new StatHolder(Constants.STAT_ATTRIBUTE_MULTIPLIER, def);
	}

	@Override
	public void TriggerServer(@Nonnull GunContext gun, @Nonnull TriggerContext trigger, @Nonnull TargetsContext targets, @Nullable AbilityStack stacks)
	{
		targets.ForEachEntity((entity) -> {
			if(entity instanceof LivingEntity living)
			{
				AttributeInstance instance = living.getAttribute(Attrib);
				if(instance != null)
				{
					instance.removeModifier(IdentifyingKey);
					instance.addTransientModifier(new AttributeModifier(
						IdentifyingKey, "Flan's Ability Effect", AttributeMultiplier.Get(gun, stacks), AttributeModifier.Operation.ADDITION)
					);
				}
			}
		});
	}

	@Override
	public boolean CanBeContinuous() { return true; }

	@Override
	public void EndServer(@Nonnull GunContext gun, @Nullable AbilityStack stacks)
	{
		// HOW do we get the entity list back?? TODO
		//triggerContext.TriggerOnEntities(Def.targetType, (triggerOn) -> {
		//	if(triggerOn instanceof LivingEntity living)
		//	{
		//		AttributeInstance instance = living.getAttribute(Attrib);
		//		if (instance != null)
		//		{
		//			instance.removeModifier(IdentifyingKey);
		//		}
		//	}
		//});
	}
}
