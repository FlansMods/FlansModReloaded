package com.flansmod.common.types.magazines;

import com.flansmod.common.types.Definitions;

public class MagazineDefinitions extends Definitions<MagazineDefinition>
{
	public MagazineDefinitions()
	{
		super(MagazineDefinition.FOLDER,
			MagazineDefinition.class,
			MagazineDefinition.INVALID,
			MagazineDefinition::new);
	}
}
