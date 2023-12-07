package com.flansmod.common.abilities;

import com.flansmod.common.FlansMod;
import com.flansmod.common.actions.contexts.GunContext;
import com.flansmod.common.types.abilities.AbilityDefinition;
import com.flansmod.common.types.abilities.elements.EAbilityTarget;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.UUID;

public class ApplyAttributeAbility extends StackableAbility
{
	public final Attribute Attrib;
	public final UUID IdentifyingKey;

	public ApplyAttributeAbility(AbilityDefinition def, int level)
	{
		super(def, level);
		if(def.effectParameters.length > 0)
			Attrib = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(def.effectParameters[0]));
		else
			Attrib = ForgeRegistries.ATTRIBUTES.getValue(ForgeRegistries.ATTRIBUTES.getDefaultKey());
		IdentifyingKey = UUID.randomUUID();

		if(def.targetType == EAbilityTarget.ShotEntity || def.targetType == EAbilityTarget.SplashedEntities)
			FlansMod.LOGGER.warn("ApplyAttributeAbility in " + def + " cannot target " + def.targetType);
	}

	@Override
	public void Trigger(@Nonnull GunContext gun, @Nullable HitResult hit)
	{
		super.Trigger(gun, hit);

		// We cannot add attributes to things we hit, because we won't be responsible for removing them
		for(Entity triggerOn : GetEntityTargets(gun, null))
		{
			if(triggerOn instanceof LivingEntity living)
			{
				AttributeInstance instance = living.getAttribute(Attrib);
				if(instance != null)
				{
					instance.removeModifier(IdentifyingKey);
					instance.addTransientModifier(new AttributeModifier(
						IdentifyingKey, "Flan's Ability Effect", GetIntensity(), AttributeModifier.Operation.ADDITION)
					);
				}
			}
		}
	}

	@Override
	public void End(@Nonnull GunContext gun)
	{
		super.End(gun);
		for(Entity triggerOn : GetEntityTargets(gun, null))
		{
			if(triggerOn instanceof LivingEntity living)
			{
				AttributeInstance instance = living.getAttribute(Attrib);
				if (instance != null)
				{
					instance.removeModifier(IdentifyingKey);
				}
			}
		}
	}

}
