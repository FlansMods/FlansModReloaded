package com.flansmod.common.types.bullets;

import com.flansmod.common.types.Constants;
import com.flansmod.common.types.JsonField;

public class HitscanDefinition
{
	// Projectile / Hitscan shared settings
	@JsonField(ModifiedBy = Constants.STAT_SHOOT_SHOT_COUNT, Docs = "Number of raycasts to create", Min = 0, Max = 128)
	public int shotCount = 1;
	@JsonField(ModifiedBy = Constants.STAT_SHOOT_SPLASH_RADIUS, Docs = "The radius within which to apply splash effects. If 0, any Impacts on splash won't trigger")
	public float splashRadius = 0.0f;
	@JsonField(Docs = "Impact settings. You probably want at least a ShotPosition, or ShotEntity and ShotBlock")
	public ImpactDefinition[] impacts = new ImpactDefinition[0];

	@JsonField(Docs = "How much stuff can this bullet pass through?")
	public float penetrationPower = 0.0f;


	public boolean HasSplash() { return splashRadius > 0f; }
}
