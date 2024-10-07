package com.flansmod.common.types.blocks;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.JsonDefinition;
import com.flansmod.common.types.JsonField;
import com.flansmod.common.types.blocks.elements.BlockDefinition;
import com.flansmod.common.types.blocks.elements.TurretMotionSettingsDefinition;
import com.flansmod.common.types.blocks.elements.TurretSideDefinition;
import com.flansmod.common.types.crafting.elements.ItemHoldingDefinition;
import com.flansmod.common.types.elements.ItemCollectionDefinition;
import com.flansmod.common.types.elements.ItemDefinition;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import javax.annotation.Nonnull;

public class TurretBlockDefinition extends JsonDefinition
{
    public static final TurretBlockDefinition INVALID = new TurretBlockDefinition(new ResourceLocation(FlansMod.MODID, "turrets/null"));
    public static final String TYPE = "turret";
    public static final String FOLDER = "turrets";
    @Override
    public String GetTypeName() { return TYPE; }

    public TurretBlockDefinition(ResourceLocation resLoc)
    {
        super(resLoc);
    }

    @JsonField
    public ItemDefinition itemSettings = new ItemDefinition();
    @JsonField
    public BlockDefinition blockSettings = new BlockDefinition();

    @JsonField(Docs = "If none are set, this will be a static turret")
    public TurretMotionSettingsDefinition[] motionSettings = new TurretMotionSettingsDefinition[0];
    @JsonField
    public TurretSideDefinition defaultSideSettings = new TurretSideDefinition();
    @JsonField
    public TurretSideDefinition[] overrideSideSettings = new TurretSideDefinition[0];

    @JsonField(Docs = "If there is no slot, you should set up an embeddedGun")
    public boolean hasGunSlot = false;
    @JsonField
    public ResourceLocation embeddedGun = InvalidLocation;
    @JsonField(Docs = "If hasGunSlot is set to true, this specifies which guns are allowed")
    public ItemCollectionDefinition allowedGuns = new ItemCollectionDefinition();
    @JsonField
    public ItemHoldingDefinition ammoSlots = new ItemHoldingDefinition();

    @Nonnull
    public TurretSideDefinition GetSideDefinition(@Nonnull Direction side)
    {
        for(TurretSideDefinition sideDef : overrideSideSettings)
            if(sideDef.side == side)
                return sideDef;
        return defaultSideSettings;
    }
}
