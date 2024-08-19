package com.flansmod.common.entity;

import com.flansmod.common.entity.vehicle.ITransformEntity;
import com.flansmod.util.Transform;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ITransformChildEntity
{
	void SyncTransformToEntity();
	void SyncEntityToTransform();

	@Nonnull ITransformPair GetWorldTransform();
	void SetWorldTransform(@Nonnull Transform currentWorldTransform);


	//void SetOffsetFromAP(@Nonnull Transform currentTransform);
	//@Nonnull ITransformPair GetOffsetFromAP();
	@Nullable ITransformEntity GetParent();
	int GetIndex();


	//@Nonnull default Transform GetOffsetFromAPPrevious() { return GetOffsetFromAP().GetPrevious(); }
	//@Nonnull default Transform GetOffsetFromAPCurrent() { return GetOffsetFromAP().GetCurrent(); }
	//@Nonnull default Transform GetOffsetFromAPDelta(float dt) { return GetOffsetFromAP().GetDelta(dt); }
	//@Nonnull default ITransformPair GetWorldToAP() { return GetParent().GetWorldToAP(GetPathInHierarchy()); }
	//@Nonnull default Transform GetWorldToAPPrevious() { return GetWorldToAP().GetPrevious(); }
	//@Nonnull default Transform GetWorldToAPCurrent() { return GetWorldToAP().GetCurrent(); }
	//@Nonnull default Transform GetWorldToAPDelta(float dt) { return GetWorldToAP().GetDelta(dt); }
	//@Nonnull default ITransformPair GetWorldToEntity() { return ITransformPair.compose(GetWorldToAP(), GetOffsetFromAP()); }
	//@Nonnull default Transform GetWorldToEntityPrevious() { return GetWorldToEntity().GetPrevious(); }
	//@Nonnull default Transform GetWorldToEntityCurrent() { return GetWorldToEntity().GetCurrent(); }
	//@Nonnull default Transform GetWorldToEntityDelta(float dt) { return GetWorldToEntity().GetDelta(dt); }
	//default void SetWorldTransform(@Nonnull Transform currentTransform)
	//{
	//	Transform frame = GetWorldToAP().GetCurrent();
	//	SetOffsetFromAP(frame.GlobalToLocalTransform(currentTransform));
	//}
}
