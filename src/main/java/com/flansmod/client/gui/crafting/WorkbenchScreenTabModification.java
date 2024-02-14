package com.flansmod.client.gui.crafting;

import com.flansmod.client.FlansModClient;
import com.flansmod.client.gui.FMScreen;
import com.flansmod.common.FlansMod;
import com.flansmod.common.actions.Actions;
import com.flansmod.common.crafting.WorkbenchBlockEntity;
import com.flansmod.common.crafting.menus.WorkbenchMenu;
import com.flansmod.common.crafting.menus.WorkbenchMenuModification;
import com.flansmod.common.item.FlanItem;
import com.flansmod.common.item.GunItem;
import com.flansmod.common.types.elements.EFilterType;
import com.flansmod.common.types.elements.LocationFilterDefinition;
import com.flansmod.common.types.elements.ModifierDefinition;
import com.flansmod.common.types.elements.PaintableDefinition;
import com.flansmod.common.types.guns.GunDefinition;
import com.flansmod.common.types.guns.elements.MagazineSlotSettingsDefinition;
import com.flansmod.common.types.magazines.MagazineDefinition;
import com.flansmod.util.Maths;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class WorkbenchScreenTabModification extends WorkbenchScreenTab<WorkbenchMenuModification>
{
	private static final ResourceLocation MOD_BG = new ResourceLocation(FlansMod.MODID, "textures/gui/gun_modification_table.png");
	private static final int MOD_W = 256;
	private static final int MOD_H = 256;

	private static final int ATTACHMENT_SLOTS_ORIGIN_X = 5;
	private static final int ATTACHMENT_SLOTS_ORIGIN_Y = 26;

	private static final int PAINT_BUCKET_SLOT_ORIGIN_X = 76;
	private static final int PAINT_BUCKET_SLOT_ORIGIN_Y = 25;
	private static final int MAG_UPGRADE_SLOT_ORIGIN_X = 76;
	private static final int MAG_UPGRADE_SLOT_ORIGIN_Y = 83;

	private static final int SKIN_SELECTOR_ORIGIN_X = 96;
	private static final int SKIN_SELECTOR_ORIGIN_Y = 16;
	private static final int SKINS_PER_ROW = 4;
	private static final int SKIN_ROWS = 3;
	private final Button[] SkinButtons = new Button[SKINS_PER_ROW * SKIN_ROWS];

	private static final int MAGAZINE_SELECTOR_ORIGIN_X = 96;
	private static final int MAGAZINE_SELECTOR_ORIGIN_Y = 74;
	private static final int MAGAZINES_PER_ROW = 4;
	private static final int MAGAZINE_ROWS = 2;
	private final Button[] MagazineButtons = new Button[MAGAZINES_PER_ROW * MAGAZINE_ROWS];

	private float GunAngle = 2.0f;
	private float GunAngularVelocity = 5.0f;

	public WorkbenchScreenTabModification(@Nonnull WorkbenchMenuModification menu, @Nonnull Inventory inventory, @Nonnull Component title)
	{
		super(menu, inventory, title);
	}

	@Override
	protected boolean IsTabPresent() { return Workbench.Def.gunModifying.isActive; }
	@Override
	@Nonnull
	protected Component GetTitle() { return Component.translatable("workbench.tab_modification"); }
	@Override
	protected void InitTab()
	{
		if(IsTabPresent())
		{
			for (int i = 0; i < SKINS_PER_ROW; i++)
			{
				for (int j = 0; j < SKIN_ROWS; j++)
				{
					final int index = i + j * SKINS_PER_ROW;
					SkinButtons[index] = Button.builder(
							Component.empty(),
							(t) ->
							{
								NetworkedButtonPress(WorkbenchMenuModification.BUTTON_SELECT_SKIN_0 + index);
								//SelectSkin(index);
							})
						.bounds(xOrigin + SKIN_SELECTOR_ORIGIN_X + 18 * i, yOrigin + SKIN_SELECTOR_ORIGIN_Y + 18 * j, 18, 18)
						.build();
					addWidget(SkinButtons[index]);
				}
			}

			for (int i = 0; i < MAGAZINES_PER_ROW; i++)
			{
				for (int j = 0; j < MAGAZINE_ROWS; j++)
				{
					final int index = i + j * MAGAZINES_PER_ROW;
					MagazineButtons[index] = Button.builder(
							Component.empty(),
							(t) ->
							{
								NetworkedButtonPress(WorkbenchMenuModification.BUTTON_SELECT_MAGAZINE_0 + index);
								//SelectSkin(index);
							})
						.bounds(xOrigin + MAGAZINE_SELECTOR_ORIGIN_X + 18 * i, yOrigin + MAGAZINE_SELECTOR_ORIGIN_Y + 18 * j, 18, 18)
						.build();
					addWidget(MagazineButtons[index]);
				}
			}
		}
	}
	@Override
	protected void OnTabSelected(boolean selected) { UpdateTab(selected); }
	@Override
	protected void UpdateTab(boolean selected)
	{
		GunAngularVelocity *= Maths.ExpF(-FlansModClient.FrameDeltaSeconds() * 0.25f);
		GunAngle += FlansModClient.FrameDeltaSeconds() * GunAngularVelocity;

		if(IsTabPresent())
		{
			int numSkinButtons = 0;

			if (selected && Workbench.GunContainer.getContainerSize() > 0 && Workbench.GunContainer.getItem(0).getItem() instanceof FlanItem flanItem)
			{
				PaintableDefinition paintableDef = flanItem.GetPaintDef();
				if (paintableDef.IsValid())
				{
					numSkinButtons = paintableDef.paintjobs.length + 1;
				}
			}

			for (int i = 0; i < SKINS_PER_ROW; i++)
			{
				for (int j = 0; j < SKIN_ROWS; j++)
				{
					final int index = i + j * SKINS_PER_ROW;
					if (SkinButtons[index] != null)
						SkinButtons[index].active = index < numSkinButtons;
				}
			}

			int numMagButtons = 0;
			if (selected && Workbench.GunContainer.getContainerSize() > 0 && Workbench.GunContainer.getItem(0).getItem() instanceof FlanItem flanItem)
			{
				if(flanItem.Def() instanceof GunDefinition gunDefinition)
				{
					numMagButtons = gunDefinition.GetMagazineSettings(Actions.DefaultPrimaryActionKey).GetMatchingMagazines().size();
				}
			}

			for (int i = 0; i < MAGAZINES_PER_ROW; i++)
			{
				for (int j = 0; j < MAGAZINE_ROWS; j++)
				{
					final int index = i + j * MAGAZINES_PER_ROW;
					if (MagazineButtons[index] != null)
						MagazineButtons[index].active = index < numMagButtons;
				}
			}
		}
	}
	@Override
	protected boolean OnMouseScroll(int xMouse, int yMouse, double scroll)
	{
		if(scroll != 0 && xMouse >= xOrigin + imageWidth)
		{
			GunAngularVelocity += scroll * 2.0f;
			return true;
		}
		return false;
	}

	@Override
	protected boolean RenderTooltip(@Nonnull GuiGraphics graphics, int xMouse, int yMouse)
	{
		if(InBox(xMouse, yMouse, xOrigin + PAINT_BUCKET_SLOT_ORIGIN_X, 18, yOrigin + PAINT_BUCKET_SLOT_ORIGIN_Y, 18))
		{
			graphics.renderTooltip(font, Component.translatable("workbench.slot.paint_can"), xMouse, yMouse);
			return true;
		}
		if(InBox(xMouse, yMouse, xOrigin + MAG_UPGRADE_SLOT_ORIGIN_X, 18, yOrigin + MAG_UPGRADE_SLOT_ORIGIN_Y, 18))
		{
			graphics.renderTooltip(font, Component.translatable("workbench.slot.mag_upgrade"), xMouse, yMouse);
			return true;
		}
		if (InBox(xMouse, yMouse, xOrigin + ATTACHMENT_SLOTS_ORIGIN_X + 26, 24, yOrigin + ATTACHMENT_SLOTS_ORIGIN_Y + 26, 24))
		{
			graphics.renderTooltip(font, Component.translatable("workbench.slot.gun"), xMouse, yMouse);
			return true;
		}

		if (Workbench.GunContainer.getContainerSize() >= 0)
		{
			// If we have a gun in that slot, we should render the modification slots that are allowed for this gun
			if (!Workbench.GunContainer.isEmpty() && Workbench.GunContainer.getItem(0).getItem() instanceof FlanItem flanItem)
			{
				ItemStack gunStack = Workbench.GunContainer.getItem(0);

				for (WorkbenchMenuModification.ModSlot modSlot : WorkbenchMenuModification.ModSlot.values())
				{
					if(flanItem.HasAttachmentSlot(modSlot.attachType, modSlot.attachIndex))
					{
						if (InBox(xMouse, yMouse, xOrigin + ATTACHMENT_SLOTS_ORIGIN_X + modSlot.x * 26, 24, yOrigin + ATTACHMENT_SLOTS_ORIGIN_Y + modSlot.y * 26, 24))
						{
							graphics.renderTooltip(font, Component.translatable("workbench.slot.attachments." + modSlot.attachType.toString().toLowerCase()), xMouse, yMouse);
							return true;
						}
					}
				}

				PaintableDefinition paintableDefinition = flanItem.GetPaintDef();
				if(paintableDefinition.paintjobs.length > 0)
				{
					// Default skin button
					if(InBox(xMouse, yMouse, xOrigin + SKIN_SELECTOR_ORIGIN_X, 18, yOrigin + SKIN_SELECTOR_ORIGIN_Y, 18))
					{
						List<FormattedCharSequence> lines = new ArrayList<>();
						lines.add(Component.translatable("paintjob.default").getVisualOrderText());
						lines.add(Component.translatable("paintjob.free_to_swap").getVisualOrderText());
						graphics.renderTooltip(font, lines, xMouse, yMouse);
						return true;
					}
					// Other skin buttons
					for(int p = 0; p < paintableDefinition.paintjobs.length; p++)
					{
						int xIndex = (p + 1) % SKINS_PER_ROW;
						int yIndex = (p + 1) / SKINS_PER_ROW;
						if(InBox(xMouse, yMouse, xOrigin + SKIN_SELECTOR_ORIGIN_X + 18 * xIndex, 18, yOrigin + SKIN_SELECTOR_ORIGIN_Y + 18 * yIndex, 18))
						{
							List<FormattedCharSequence> lines = new ArrayList<>();
							lines.add(Component.translatable("paintjob." + flanItem.DefinitionLocation.getNamespace() + "." + paintableDefinition.paintjobs[p].textureName).getVisualOrderText());
							int paintCost = WorkbenchBlockEntity.GetPaintUpgradeCost(Workbench.GunContainer, p + 1);
							if(paintCost == 1)
								lines.add(Component.translatable("paintjob.cost.1").getVisualOrderText());
							else lines.add(Component.translatable("paintjob.cost", paintCost).getVisualOrderText());

							graphics.renderTooltip(font, lines, xMouse, yMouse);
							return true;
						}
					}
				}

				if (flanItem.Def() instanceof GunDefinition gunDef)
				{
					// TODO: Multiple mag settings
					MagazineSlotSettingsDefinition magSettings = gunDef.GetMagazineSettings(Actions.DefaultPrimaryActionKey);
					List<MagazineDefinition> matchingMags = magSettings.GetMatchingMagazines();
					for (int j = 0; j < MAGAZINE_ROWS; j++)
					{
						for (int i = 0; i < MAGAZINES_PER_ROW; i++)
						{
							final int index = j * MAGAZINES_PER_ROW + i;
							if(index < matchingMags.size())
							{
								if(InBox(xMouse, yMouse, xOrigin + MAGAZINE_SELECTOR_ORIGIN_X + 18 * i, 18, yOrigin + MAGAZINE_SELECTOR_ORIGIN_Y + 18 * j, 18))
								{
									List<FormattedCharSequence> lines = new ArrayList<>();
									lines.add(Component.translatable("magazine." + matchingMags.get(i).Location.getNamespace() + "." + matchingMags.get(i).Location.getPath()).getVisualOrderText());
									lines.add(Component.translatable("magazine.num_rounds", matchingMags.get(i).numRounds).getVisualOrderText());
									for(LocationFilterDefinition idFilter : matchingMags.get(i).matchingBullets.itemIDFilters)
									{
										if(idFilter.filterType == EFilterType.Allow)
											for(ResourceLocation resLoc : idFilter.matchResourceLocations)
												lines.add(Component.translatable("magazine.match_bullet_name", resLoc).getVisualOrderText());
									}
									for(LocationFilterDefinition tagFilter : matchingMags.get(i).matchingBullets.itemTagFilters)
									{
										for(ResourceLocation resLoc : tagFilter.matchResourceLocations)
										{
											String line = tagFilter.filterType == EFilterType.Allow ? "magazine.required_bullet_tag" : "magazine.disallowed_bullet_tag";
											lines.add(Component.translatable(line, resLoc.toString()).getVisualOrderText());
										}
									}
									int magCost = WorkbenchBlockEntity.GetMagUpgradeCost(Workbench.GunContainer, index);
									if(magCost == 1)
										lines.add(Component.translatable("magazine.cost.1").getVisualOrderText());
									else lines.add(Component.translatable("magazine.cost", magCost).getVisualOrderText());


									for(ModifierDefinition modifier : matchingMags.get(i).modifiers)
									{
										for(Component modString : modifier.GetModifierStrings())
											lines.add(modString.getVisualOrderText());
									}

									graphics.renderTooltip(font, lines, xMouse, yMouse);
									return true;
								}
							}
						}
					}
				}
			}
		}

		return false;
	}

	@Override
	protected void RenderBG(@Nonnull GuiGraphics graphics, int xMouse, int yMouse)
	{
		// Render the gun before the background so it ends up behind
		if(Workbench.GunContainer.getContainerSize() > 0)
		{
			Render3DGun(graphics, xOrigin + imageWidth + 64, yOrigin + 64, GunAngle, -45f, Workbench.GunContainer.getItem(0));
		}


		graphics.blit(MOD_BG, xOrigin, yOrigin, 0, 0, imageWidth, imageHeight, MOD_W, MOD_H);
		if (Workbench.GunContainer.getContainerSize() >= 0)
		{
			// Render the slot BG for the gun slot
			graphics.blit(MOD_BG, xOrigin + ATTACHMENT_SLOTS_ORIGIN_X + 26, yOrigin + ATTACHMENT_SLOTS_ORIGIN_Y + 26, 198, 26, 22, 22, MOD_W, MOD_H);

			// Paint Can Slot
			graphics.blit(MOD_BG, xOrigin + PAINT_BUCKET_SLOT_ORIGIN_X, yOrigin + PAINT_BUCKET_SLOT_ORIGIN_Y, 208, 201, 18, 18, MOD_W, MOD_H);
			// Mag Slot
			graphics.blit(MOD_BG, xOrigin + MAG_UPGRADE_SLOT_ORIGIN_X, yOrigin + MAG_UPGRADE_SLOT_ORIGIN_Y, 190, 201, 18, 18, MOD_W, MOD_H);

			// If we have a gun in that slot, we should render the modification slots that are allowed for this gun
			if (!Workbench.GunContainer.isEmpty() && Workbench.GunContainer.getItem(0).getItem() instanceof FlanItem flanItem)
			{
				ItemStack gunStack = Workbench.GunContainer.getItem(0);
				for (WorkbenchMenuModification.ModSlot modSlot : WorkbenchMenuModification.ModSlot.values())
				{
					// If this item has this slot, blit the slot BG in
					if (flanItem.HasAttachmentSlot(modSlot.attachType, modSlot.attachIndex))
					{
						graphics.blit(MOD_BG,
							xOrigin + ATTACHMENT_SLOTS_ORIGIN_X + 26 * modSlot.x,
							yOrigin + ATTACHMENT_SLOTS_ORIGIN_Y + 26 * modSlot.y,
							172 + 26 * modSlot.x,
							26 * modSlot.y,
							22, 22,
							MOD_W, MOD_H);
					}
				}

				PaintableDefinition paintableDefinition = flanItem.GetPaintDef();
				if(paintableDefinition.paintjobs.length > 0)
				{
					// Default skin button
					if(FlanItem.GetPaintjobName(gunStack).equals("default"))
					{
						graphics.blit(MOD_BG, xOrigin + SKIN_SELECTOR_ORIGIN_X, yOrigin + SKIN_SELECTOR_ORIGIN_Y, 172, 201, 18, 18, MOD_W, MOD_H);
					}
					else graphics.blit(MOD_BG, xOrigin + SKIN_SELECTOR_ORIGIN_X, yOrigin + SKIN_SELECTOR_ORIGIN_Y, 172, 165, 18, 18, MOD_W, MOD_H);

					// Other skin buttons
					for(int p = 0; p < paintableDefinition.paintjobs.length; p++)
					{
						int xIndex = (p + 1) % SKINS_PER_ROW;
						int yIndex = (p + 1) / SKINS_PER_ROW;
						if(FlanItem.GetPaintjobName(gunStack).equals(paintableDefinition.paintjobs[p].textureName))
						{
							graphics.blit(MOD_BG, xOrigin + SKIN_SELECTOR_ORIGIN_X + 18 * xIndex, yOrigin + SKIN_SELECTOR_ORIGIN_Y + 18 * yIndex, 172, 201, 18, 18, MOD_W, MOD_H);
						}
						else graphics.blit(MOD_BG, xOrigin + SKIN_SELECTOR_ORIGIN_X + 18 * xIndex, yOrigin + SKIN_SELECTOR_ORIGIN_Y + 18 * yIndex, 172, 165, 18, 18, MOD_W, MOD_H);
					}
				}


				// Magazine selector
				if(flanItem instanceof GunItem gunItem)
				{
					MagazineSlotSettingsDefinition magSettings = gunItem.Def().GetMagazineSettings(Actions.DefaultPrimaryActionKey);
					List<MagazineDefinition> matchingMags = magSettings.GetMatchingMagazines();
					MagazineDefinition currentMagType = gunItem.GetMagazineType(gunStack, Actions.DefaultPrimaryActionKey, 0);
					for(int i = 0; i < matchingMags.size(); i++)
					{
						int xIndex = i % SKINS_PER_ROW;
						int yIndex = i / SKINS_PER_ROW;

						if(matchingMags.get(i) == currentMagType)
						{
							graphics.blit(MOD_BG, xOrigin + MAGAZINE_SELECTOR_ORIGIN_X + xIndex * 18, yOrigin + MAGAZINE_SELECTOR_ORIGIN_Y + yIndex * 18, 172, 201, 18, 18, MOD_W, MOD_H);
						}
						else graphics.blit(MOD_BG, xOrigin + MAGAZINE_SELECTOR_ORIGIN_X + xIndex * 18, yOrigin + MAGAZINE_SELECTOR_ORIGIN_Y + yIndex * 18, 172, 165, 18, 18, MOD_W, MOD_H);
					}

				}
			}
		}
	}

	@Override
	protected void RenderFG(@Nonnull GuiGraphics graphics, int xMouse, int yMouse)
	{
		if (Workbench.GunContainer.getContainerSize() >= 0)
		{// If we have a gun in that slot, we should render the modification slots that are allowed for this gun
			if (!Workbench.GunContainer.isEmpty() && Workbench.GunContainer.getItem(0).getItem() instanceof FlanItem flanItem)
			{
				ItemStack gunStack = Workbench.GunContainer.getItem(0);

				PaintableDefinition paintableDefinition = flanItem.GetPaintDef();
				if (paintableDefinition.paintjobs.length > 0)
				{
					// Render default skin
					{
						ItemStack paintedStack = gunStack.copy();
						FlanItem.SetPaintjobName(paintedStack, "default");
						RenderGUIItem(graphics, SKIN_SELECTOR_ORIGIN_X + 1,  SKIN_SELECTOR_ORIGIN_Y + 1, paintedStack, false);
					}

					// And other skins
					for (int p = 0; p < paintableDefinition.paintjobs.length; p++)
					{
						int xIndex = (p + 1) % SKINS_PER_ROW;
						int yIndex = (p + 1) / SKINS_PER_ROW;
						ItemStack paintedStack = gunStack.copy();
						FlanItem.SetPaintjobName(paintedStack, paintableDefinition.paintjobs[p].textureName);
						RenderGUIItem(graphics,SKIN_SELECTOR_ORIGIN_X + 1 + 18 * xIndex, SKIN_SELECTOR_ORIGIN_Y + 1 + 18 * yIndex, paintedStack, false);
					}
				}

				if(flanItem.Def() instanceof GunDefinition gunDef)
				{
					MagazineSlotSettingsDefinition magSettings = gunDef.GetMagazineSettings(Actions.DefaultPrimaryActionKey);
					List<MagazineDefinition> matchingMags = magSettings.GetMatchingMagazines();
					for(int i = 0; i < matchingMags.size(); i++)
					{
						int xIndex = i % MAGAZINES_PER_ROW;
						int yIndex = i / MAGAZINES_PER_ROW;

						// RENDER MAG
						TextureAtlasSprite sprite = FlansModClient.MAGAZINE_ATLAS.GetIcon(matchingMags.get(i).Location);
						graphics.blit(MAGAZINE_SELECTOR_ORIGIN_X + 1 + xIndex * 18, MAGAZINE_SELECTOR_ORIGIN_Y + 1 + yIndex * 18, 0, 16, 16, sprite);
					}

				}
			}
		}
	}
}
