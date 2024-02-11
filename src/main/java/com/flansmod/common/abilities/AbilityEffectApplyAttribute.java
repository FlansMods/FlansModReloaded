package com.flansmod.common.abilities;

import com.flansmod.common.actions.contexts.GunContext;
import com.flansmod.common.actions.contexts.TargetsContext;
import com.flansmod.common.types.abilities.elements.AbilityEffectDefinition;
import com.flansmod.common.types.elements.ModifierDefinition;
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
	public final float BaseMultiplier;

	public AbilityEffectApplyAttribute(@Nonnull AbilityEffectDefinition def)
	{
		Attrib = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(def.ModifyString(ModifierDefinition.KEY_MOB_EFFECT_ID, "")));
		BaseMultiplier = def.ModifyFloat(ModifierDefinition.STAT_ATTRIBUTE_MULTIPLIER, 1.0f);
		IdentifyingKey = UUID.randomUUID();
	}

	@Override
	public void Trigger(@Nonnull GunContext gun, @Nonnull TargetsContext targets, @Nullable AbilityStack stacks, int tier)
	{
		targets.ForEachEntity((entity) -> {
			if(entity instanceof LivingEntity living)
			{
				AttributeInstance instance = living.getAttribute(Attrib);
				if(instance != null)
				{
					instance.removeModifier(IdentifyingKey);
					instance.addTransientModifier(new AttributeModifier(
						IdentifyingKey, "Flan's Ability Effect", AttributeMultiplier(gun, stacks), AttributeModifier.Operation.ADDITION)
					);
				}
			}
		});
	}

	@Override
	public void End(@Nonnull GunContext gun, @Nullable AbilityStack stacks)
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

	private float AttributeMultiplier(@Nonnull GunContext gun, @Nullable AbilityStack stacks)
	{
		float intensity = gun.ModifyFloat(ModifierDefinition.STAT_ATTRIBUTE_MULTIPLIER, BaseMultiplier);
		if(stacks != null)
		{
			intensity *= stacks.GetIntensity();
		}
		return intensity;
	}

}
