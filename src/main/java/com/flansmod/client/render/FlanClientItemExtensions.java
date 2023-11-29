package com.flansmod.client.render;

import com.flansmod.common.item.GunItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

public class FlanClientItemExtensions implements IClientItemExtensions
{
    protected FlanItemModelRenderer renderer;

    protected FlanClientItemExtensions(FlanItemModelRenderer renderer)
    {
        this.renderer = renderer;
    }

    public static FlanClientItemExtensions create(Item item, FlanItemModelRenderer renderer)
    {
        FlanModelRegistration.PreRegisterRenderer(item, renderer);
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
