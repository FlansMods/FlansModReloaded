package com.flansmod.airtraffic.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public interface IAirport extends IAirportConst
{
	void setName(@Nonnull String newName);

	@Nullable IRunway tryLoadRunwayForEdit(int index);


	void transferOwnership(@Nonnull UUID playerID);
}
