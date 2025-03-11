package com.flansmod.common.entity.vehicle.controls;

import com.flansmod.common.FlansMod;
import com.flansmod.common.FlansRegistries;
import com.flansmod.common.actions.ActionType;
import com.flansmod.common.actions.nodes.AnimationAction;
import com.flansmod.common.actions.nodes.ShootAction;
import com.flansmod.common.types.vehicles.ControlSchemeDefinition;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ControlLogics
{
	public static final DeferredRegister<ControlLogicType<?>> BASE_CONTROL_LOGIC_TYPES = DeferredRegister.create(FlansRegistries.CONTROL_LOGIC_TYPE, FlansMod.MODID);

	public static final RegistryObject<ControlLogicType<?>> CONTROL_LOGIC_TYPE_LEGACY_CAR = BASE_CONTROL_LOGIC_TYPES.register("legacy_car", () -> new ControlLogicType<>(LegacyVehicleControlLogic::car));
	public static final RegistryObject<ControlLogicType<?>> CONTROL_LOGIC_TYPE_LEGACY_TANK = BASE_CONTROL_LOGIC_TYPES.register("legacy_tank", () -> new ControlLogicType<>(LegacyVehicleControlLogic::tank));

	public static final RegistryObject<ControlLogicType<?>> CONTROL_LOGIC_TYPE_CAR = BASE_CONTROL_LOGIC_TYPES.register("car", () -> new ControlLogicType<>(CarControlLogic::new));

	@Nullable
	public static ControlLogic InstanceControlLogic(@Nonnull ControlSchemeDefinition def)
	{
		ControlLogicType<? extends ControlLogic> logicType = FlansRegistries.CONTROL_LOGIC_TYPES.get().getValue(def.logicType);
		if(logicType != null)
			return logicType.createFunc().apply(def);

		return null;
	}
}
