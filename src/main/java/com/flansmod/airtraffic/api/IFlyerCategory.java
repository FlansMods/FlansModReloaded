package com.flansmod.airtraffic.api;

public interface IFlyerCategory
{
	int getMinAirstripWidth();
	int getMinAirstripLength();
	double getMinExitIncline();
	double getMinEntryIncline();
	double getMinCruisingHeight();

	double getTurningCircleRadius();

	boolean isVerticalTakeoff();

}
