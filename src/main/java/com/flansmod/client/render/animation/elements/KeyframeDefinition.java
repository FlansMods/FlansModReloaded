package com.flansmod.client.render.animation.elements;


import com.flansmod.common.types.JsonField;

public class KeyframeDefinition
{
	@JsonField
	public String name = "";
	@JsonField
	public PoseDefinition[] poses = new PoseDefinition[0];
	@JsonField
	public String[] parents = new String[0];

	public boolean HasPoseForPart(String partName)
	{
		for(int i = 0; i < poses.length; i++)
		{
			if(poses[i].applyTo.equals(partName))
				return true;
		}
		return false;
	}

	public PoseDefinition GetPoseForPart(String partName)
	{
		for(int i = 0; i < poses.length; i++)
		{
			if(poses[i].applyTo.equals(partName))
				return poses[i];
		}
		return PoseDefinition.Identity();
	}
}
