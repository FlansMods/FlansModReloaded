package com.flansmod.common.types.npc;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.JsonDefinition;
import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.elements.ItemStackDefinition;
import com.flansmod.common.types.npc.elements.ENpcActionType;
import com.flansmod.common.types.npc.elements.MerchantOfferDefinition;
import com.flansmod.common.types.npc.elements.VoiceLineDefinition;
import com.flansmod.common.types.teams.ClassDefinition;
import com.flansmod.util.Maths;
import net.minecraft.resources.ResourceLocation;

public class NpcDefinition extends JsonDefinition
{
	public static final NpcDefinition INVALID = new NpcDefinition(new ResourceLocation(FlansMod.MODID, "npcs/null"));
	public static final String TYPE = "npc";
	public static final String FOLDER = "npcs";
	@Override
	public String GetTypeName() { return TYPE; }

	public NpcDefinition(ResourceLocation resLoc)
	{
		super(resLoc);
	}

	// General settings
	@JsonField
	public VoiceLineDefinition[] voiceLines = new VoiceLineDefinition[0];

	@JsonField()
	public ItemStackDefinition hat = new ItemStackDefinition();
	@JsonField()
	public ItemStackDefinition chest = new ItemStackDefinition();
	@JsonField()
	public ItemStackDefinition legs = new ItemStackDefinition();
	@JsonField()
	public ItemStackDefinition shoes = new ItemStackDefinition();
	@JsonField()
	public ItemStackDefinition mainHand = new ItemStackDefinition();
	@JsonField()
	public ItemStackDefinition offHand = new ItemStackDefinition();

	// Behaviour settings
	@JsonField
	public ENpcActionType[] validActions = new ENpcActionType[0];
	@JsonField
	public float cooldownSecondsFriendly = 120;
	@JsonField
	public float cooldownSecondsHostile = 300;

	public int CooldownTicks(boolean friendly) {
		return friendly ? Maths.Ceil(cooldownSecondsFriendly * 20.0f)
						: Maths.Ceil(cooldownSecondsHostile * 20.0f);
	}

	// Merchant settings
	@JsonField(Docs = "If set to 0, this NPC will not be considered a merchant")
	public int maxMerchantLevel = 0;
	@JsonField
	public int[] xpPerMerchantLevel = new int[0];
	@JsonField
	public MerchantOfferDefinition[] offers = new MerchantOfferDefinition[0];

	public boolean Can(ENpcActionType actionType)
	{
		for(ENpcActionType validActionType : validActions)
			if(validActionType == actionType)
				return true;
		return false;
	}

}
