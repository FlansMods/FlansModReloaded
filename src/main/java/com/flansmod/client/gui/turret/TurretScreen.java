package com.flansmod.client.gui.turret;

import com.flansmod.client.gui.FMScreen;
import com.flansmod.common.FlansMod;
import com.flansmod.common.blocks.TurretContainerMenu;
import com.flansmod.util.Maths;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;

public class TurretScreen extends FMScreen<TurretContainerMenu>
{
    private static final ResourceLocation TURRET_BG = new ResourceLocation(FlansMod.MODID, "textures/gui/turret.png");
    private static final int TURRET_BG_W = 256;
    private static final int TURRET_BG_H = 256;

    private static final int SLOTS_PER_ROW = 9;
    private static final int MAX_ROWS = 3;

    private static final int SLOTS_ORIGIN_X = 5;
    private static final int SLOTS_ORIGIN_Y = 43;

    public int xOrigin;
    public int yOrigin;

    public TurretScreen(@Nonnull TurretContainerMenu menu, @Nonnull Inventory inventory, @Nonnull Component title)
    {
        super(menu, inventory, title);

        imageWidth = 172;
        imageHeight = 218;
    }

    @Override
    protected void init() {
        super.init();
        xOrigin = width / 2 - imageWidth / 2;
        yOrigin = height / 2 - imageHeight / 2;

    }
    @Override
    protected void renderBg(@Nonnull GuiGraphics graphics, float f, int xMouse, int yMouse)
    {
        graphics.blit(TURRET_BG, xOrigin, yOrigin, 0, 0, imageWidth, imageHeight, TURRET_BG_W, TURRET_BG_H);

        int gunSlotCount = 1;
        graphics.blit(TURRET_BG, xOrigin + 5, yOrigin + 23, 173, 1, 18, 18, TURRET_BG_W, TURRET_BG_H);
        int ammoSlotCount = menu.TurretContainer.getContainerSize() - gunSlotCount;
        for(int j = 0; j < MAX_ROWS; j++)
        {
            int numSlotsOnThisRow = Maths.Min(ammoSlotCount - j * SLOTS_PER_ROW, SLOTS_PER_ROW);
            if (numSlotsOnThisRow > 0)
            {
                graphics.blit(TURRET_BG, xOrigin + SLOTS_ORIGIN_X, yOrigin + SLOTS_ORIGIN_Y + 18 * j, 5, 136, 18 * numSlotsOnThisRow, 18, TURRET_BG_W, TURRET_BG_H);
            }
        }
    }
}
