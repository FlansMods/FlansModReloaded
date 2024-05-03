package com.flansmod.common.types.elements;

public enum EPlayerInput
{
	MoveForward,
	MoveBackward,
	MoveLeft,
	MoveRight,

	YawLeft,
	YawRight,
	RollLeft,
	RollRight,
	PitchUp,
	PitchDown,

	Fire1,
	Fire2,
	Fire3,
	Reload1,
	Reload2,
	Reload3,

	// Could be used by vehicles, handbrake for example
	Jump,
	Sprint,

	// Some examples are LookAt for guns, BarrelRoll for vehicles
	SpecialKey1,
	// Some examples are ModeSwitch for guns
	SpecialKey2,

	// Generally used for vehicles
	GearUp,
	GearDown,
}
