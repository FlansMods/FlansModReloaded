package com.flansmod.common.item;

import com.flansmod.client.render.FlanClientItemExtensions;
import com.flansmod.client.render.bullets.BulletItemRenderer;
import com.flansmod.client.render.guns.GunItemRenderer;
import com.flansmod.common.FlansMod;
import com.flansmod.common.types.abilities.CraftingTraitDefinition;
import com.flansmod.common.types.abilities.elements.AbilityTargetDefinition;
import com.flansmod.common.types.abilities.elements.CraftingTraitProviderDefinition;
import com.flansmod.common.types.attachments.AttachmentDefinition;
import com.flansmod.common.types.bullets.BulletDefinition;
import com.flansmod.common.types.elements.ModifierDefinition;
import com.flansmod.common.types.guns.elements.AbilityDefinition;
import com.flansmod.common.types.guns.elements.HandlerDefinition;
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
import net.minecraftforge.common.extensions.IForgeItem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public class BulletItem extends FlanItem implements IForgeItem
{
	@Override
	public BulletDefinition Def() { return FlansMod.BULLETS.Get(DefinitionLocation); }

	public BulletItem(ResourceLocation defLoc, Properties properties)
	{
		super(defLoc, properties);
	}
	@Override
	public boolean CanBeCraftedFromParts() { return false; }
	@Override
	public boolean ShouldRenderAsIcon(@Nonnull ItemDisplayContext transformType) { return true; }

	@Override
	public boolean isDamageable(ItemStack stack)
	{
		return Def().GetItemDurability() > 0;
	}

	@Override
	public boolean canBeDepleted()
	{
		return Def().GetItemDurability() > 0;
	}

	@Override
	public int getMaxDamage(ItemStack stack)
	{
		return Def().GetItemDurability();
	}

	@Override
	public int getMaxStackSize(ItemStack stack)
	{
		if(stack.getDamageValue() == 0)
			return Def().GetMaxStackSize();
		else
			return 1;
	}

	// Random parameter overrides
	public boolean isEnchantable(ItemStack i) { return false; }

	@Override
	@OnlyIn(Dist.CLIENT)
	public void initializeClient(Consumer<IClientItemExtensions> consumer)
	{
		consumer.accept(FlanClientItemExtensions.create(this, new BulletItemRenderer(this)));
	}

	@Override
	public void appendHoverText(@Nonnull ItemStack stack,
								@Nullable Level level,
								@Nonnull List<Component> tooltips,
								@Nonnull TooltipFlag flags)
	{
		super.appendHoverText(stack, level, tooltips, flags);

		boolean expanded = InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), InputConstants.KEY_RSHIFT)
			|| InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), InputConstants.KEY_LSHIFT);

		BulletDefinition def = Def();
		if(def.IsValid())
		{
			for(AbilityDefinition ability : def.triggers)
			{
				tooltips.add(ability.GetTooltip(expanded));
			}

			//for(HandlerDefinition handler : def.handlerOverrides)
			//{
			//	Component inputComponent = Component.translatable("player.input." + handler.inputType.toString().toLowerCase());
			//	tooltips.add(Component.translatable("attachment.add_input", inputComponent));
			//}
//
			//for(CraftingTraitProviderDefinition provider : def.abilities)
			//{
			//	CraftingTraitDefinition trait = FlansMod.TRAITS.Get(provider.trait);
			//	if(trait.IsValid())
			//	{
			//		tooltips.add(FlanItem.CreateTraitComponent(trait, provider.level, expanded));
			//	}
			//}
		}
	}
}
