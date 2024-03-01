package com.flansmod.common.item;

import com.flansmod.client.render.FlanClientItemExtensions;
import com.flansmod.client.render.guns.AttachmentItemRenderer;
import com.flansmod.client.render.guns.GunItemRenderer;
import com.flansmod.common.FlansMod;
import com.flansmod.common.types.abilities.CraftingTraitDefinition;
import com.flansmod.common.types.abilities.elements.CraftingTraitProviderDefinition;
import com.flansmod.common.types.attachments.AttachmentDefinition;
import com.flansmod.common.types.elements.ModifierDefinition;
import com.flansmod.common.types.guns.elements.AbilityDefinition;
import com.flansmod.common.types.guns.elements.ActionGroupDefinition;
import com.flansmod.common.types.guns.elements.HandlerDefinition;
import com.flansmod.common.types.guns.elements.HandlerNodeDefinition;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public class AttachmentItem extends FlanItem
{
	@Override
	public AttachmentDefinition Def() { return FlansMod.ATTACHMENTS.Get(DefinitionLocation); }

	public AttachmentItem(ResourceLocation defLoc, Properties properties)
	{
		super(defLoc, properties);
	}
	@Override
	public boolean CanBeCraftedFromParts() { return false; }
	@Override
	public boolean ShouldRenderAsIcon(@Nonnull ItemDisplayContext transformType) { return true; }

	@Override
	@OnlyIn(Dist.CLIENT)
	public void initializeClient(Consumer<IClientItemExtensions> consumer)
	{
		consumer.accept(FlanClientItemExtensions.create(this, new AttachmentItemRenderer(this)));
	}

	// Random parameter overrides
	public boolean isEnchantable(ItemStack i) { return false; }

	@Override
	public void appendHoverText(@Nonnull ItemStack stack,
								@Nullable Level level,
								@Nonnull List<Component> tooltips,
								@Nonnull TooltipFlag flags)
	{
		super.appendHoverText(stack, level, tooltips, flags);

		boolean expanded = InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), InputConstants.KEY_RSHIFT)
			|| InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), InputConstants.KEY_LSHIFT);

		AttachmentDefinition def = Def();
		if(def.IsValid())
		{
			for(ModifierDefinition mod : def.modifiers)
			{
				tooltips.addAll(mod.GetModifierStrings());
			}

			for(HandlerDefinition handler : def.handlerOverrides)
			{
				Component inputComponent = Component.translatable("player.input." + handler.inputType.toString().toLowerCase());
				tooltips.add(Component.translatable("attachment.add_input", inputComponent));
			}

			for(CraftingTraitProviderDefinition provider : def.abilities)
			{
				CraftingTraitDefinition trait = FlansMod.TRAITS.Get(provider.trait);
				if(trait.IsValid())
				{
					tooltips.add(FlanItem.CreateTraitComponent(trait, provider.level, expanded));
				}
			}
		}
	}
}
