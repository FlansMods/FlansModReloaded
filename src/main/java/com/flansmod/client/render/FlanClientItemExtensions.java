package com.flansmod.client.render;

import com.flansmod.client.render.models.FlansModelRegistry;
import com.flansmod.common.item.GunItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class FlanClientItemExtensions implements IClientItemExtensions
{
    @Nonnull
    protected final FlanItemModelRenderer renderer;

    protected FlanClientItemExtensions(@Nonnull FlanItemModelRenderer renderer)
    {
        this.renderer = renderer;
    }

    @Nonnull
    public static FlanClientItemExtensions create(@Nonnull Item item, @Nonnull FlanItemModelRenderer renderer)
    {
        FlansModelRegistry.PreRegisterRenderer(item, renderer);
        return new FlanClientItemExtensions(renderer);
    }

    @Override
    @Nullable
    public HumanoidModel.ArmPose getArmPose(LivingEntity entityLiving, InteractionHand hand, ItemStack itemStack)
    {
        if(itemStack.getItem() instanceof GunItem)
            return HumanoidModel.ArmPose.BOW_AND_ARROW;
        return HumanoidModel.ArmPose.ITEM;
    }

    @Override
    public FlanItemModelRenderer getCustomRenderer()
    {
        return renderer;
    }
}
