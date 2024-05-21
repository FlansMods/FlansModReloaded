package com.flansmod.common.entity.vehicle;

import com.flansmod.common.entity.ITransformPair;
import com.flansmod.util.Transform;
import com.flansmod.util.TransformStack;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ITransformEntity
{
	void SyncTransformToEntity();
	void SyncEntityToTransform();

	void SetWorldToEntity(@Nonnull Transform transform);
	@Nonnull ITransformPair GetWorldToEntity();
	@Nonnull ITransformPair GetEntityToAP(@Nonnull String apPath);

	// Velocity handling
	@Nonnull Vec3 GetVelocity();
	void SetVelocity(@Nonnull Vec3 velocityMetersPerSecond);
	void ApplyVelocity();



	// Lots of default getters for quick access
	//@Nonnull default Transform GetWorldToEntityPrevious() { return GetWorldToEntity().GetPrevious(); }
	//@Nonnull default Transform GetWorldToEntityCurrent() { return GetWorldToEntity().GetCurrent(); }
	//@Nonnull default Transform GetWorldToEntity(float dt) { return GetWorldToEntity().GetDelta(dt); }
	//@Nonnull default Transform GetEntityToAPPrevious(@Nonnull String apPath) { return GetEntityToAP(apPath).GetPrevious(); }
	//@Nonnull default Transform GetEntityToAPCurrent(@Nonnull String apPath) { return GetEntityToAP(apPath).GetCurrent(); }
	//@Nonnull default Transform GetEntityToAP(@Nonnull String apPath, float dt) { return GetEntityToAP(apPath).GetDelta(dt); }
	@Nonnull default ITransformPair GetWorldToAP(@Nonnull String apPath) {
		return ITransformPair.compose(GetWorldToEntity(), GetEntityToAP(apPath));
	}
	//@Nonnull default Transform GetWorldToAPPrevious(@Nonnull String apPath) { return GetWorldToAP(apPath).GetPrevious(); }
	//@Nonnull default Transform GetWorldToAPCurrent(@Nonnull String apPath) { return GetWorldToAP(apPath).GetCurrent(); }
	//@Nonnull default Transform GetWorldToAP(@Nonnull String apPath, float dt) { return GetWorldToAP(apPath).GetDelta(dt);

}
