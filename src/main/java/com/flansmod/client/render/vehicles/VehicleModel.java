package com.flansmod.client.render.vehicles;

import com.flansmod.client.render.FlanItemModel;
import net.minecraft.client.resources.model.BakedModel;

public class VehicleModel extends FlanItemModel
{
	public VehicleModel(BakedModel template, String modID, String gunName)
	{
		super(template, modID, gunName);

		addParts("body",
			"sights",
			"stock",
			"grip",
			"barrel",
			"magazine",
			"break_action"
		);

	}
}
