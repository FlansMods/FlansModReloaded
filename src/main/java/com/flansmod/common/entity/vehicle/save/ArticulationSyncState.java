package com.flansmod.common.entity.vehicle.save;

public class ArticulationSyncState
{
	public ArticulationSyncState() {

	}
	public ArticulationSyncState(float param, float velocity)
	{
		Parameter = param;
		Velocity = velocity;
	}

	public float Parameter;
	public float Velocity;

	public void SetParameter(float value) { Parameter = value; }
	public void SetVelocity(float value) { Velocity = value; }
	public float GetParameter() { return Parameter; }
	public float GetVelocity() { return Velocity; }
}
