package com.flansmod.common.types.bullets;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.JsonDefinition;
import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.elements.ShotDefinition;
import net.minecraft.resources.ResourceLocation;

public class BulletDefinition extends JsonDefinition
{
	public static final BulletDefinition INVALID = new BulletDefinition(new ResourceLocation(FlansMod.MODID, "bullets/null"));
	public static final String TYPE = "bullet";
	@Override
	public String GetTypeName() { return TYPE; }

	public BulletDefinition(ResourceLocation resLoc)
	{
		super(resLoc);
	}

	@JsonField
	public float gravityFactor = 0.25f;
	@JsonField
	public int maxStackSize = 64;
	@JsonField
	public ShotDefinition ShootStats = new ShotDefinition();

}
