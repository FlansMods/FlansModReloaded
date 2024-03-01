package com.flansmod.common.types.abilities.elements;

import com.flansmod.common.actions.contexts.TriggerContext;
import com.flansmod.common.types.JsonField;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TriggerConditionDefinition
{
	@JsonField
	public ETriggerConditionType conditionType = ETriggerConditionType.CheckActionGroupPath;
	@JsonField
	public String[] allowedValues = new String[0];

	public boolean Matches(@Nonnull TriggerContext triggerContext)
	{
		switch(conditionType)
		{
			case CheckActionGroupPath -> { return IsAllowed(triggerContext.ActionGroupPath); }
			case CheckOwnerEntityType -> { return IsAllowed(triggerContext.Owner); }
			case CheckShooterEntityType -> { return IsAllowed(triggerContext.Shooter); }
			default -> { return false; }
		}
	}

	private boolean IsAllowed(@Nullable Entity check)
	{
		if(check == null)
			return allowedValues.length == 0;

		for(String allowed : allowedValues)
		{
			TagKey<EntityType<?>> entityTag = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(allowed));
			if(check.getType().is(entityTag))
				return true;
		}
		return false;
	}
	private boolean IsAllowed(@Nullable String check)
	{
		if(check == null)
			return allowedValues.length == 0;

		for(String allowed : allowedValues)
			if(allowed.equals(check))
				return true;

		return false;
	}
}

