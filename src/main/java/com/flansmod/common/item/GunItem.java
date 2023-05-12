package com.flansmod.common.item;

import com.flansmod.client.render.FlanClientItemExtensions;
import com.flansmod.client.render.guns.GunItemRenderer;
import com.flansmod.client.render.models.TurboRig;
import com.flansmod.common.FlansMod;
import com.flansmod.common.actions.*;
import com.flansmod.common.types.elements.ActionDefinition;
import com.flansmod.common.types.guns.GunContext;
import com.flansmod.common.types.guns.GunDefinition;
import com.flansmod.common.types.guns.IGunInterpreter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;

public class GunItem extends FlanItem
{
    private ResourceLocation definitionLocation;
    public GunDefinition Def() { return FlansMod.GUNS.get(definitionLocation); }

    // TODO: Place more generally private ActionStack GunActions;
    
    public GunItem(ResourceLocation defLoc, Properties properties)
    {
        super(properties);
    
        definitionLocation = defLoc;
    }

    private Properties HackySetBeforeSuper(ResourceLocation defLoc, Properties properties)
    {
        definitionLocation = defLoc;
        return properties;
    }


    @OnlyIn(Dist.CLIENT)
    public void ClientHandleMouse(Player player, ItemStack stack, InputEvent.InteractionKeyMappingTriggered event)
    {
        if(event.isAttack()) // Primary actions
        {
            FlansMod.GUNSHOTS.ClientShoot(player, event.getHand(), EActionSet.PRIMARY);
        }
        else if(event.isUseItem()) // Secondary actions
        {
            FlansMod.GUNSHOTS.ClientShoot(player, event.getHand(), EActionSet.SECONDARY);
        }
    }



    @Nonnull
    @Override
    public InteractionResult useOn(UseOnContext context)
    {
        /*
        GunDefinition def = Def();
        GunContext gunContext = GunContext.CreateFromContext(context);

        int actionsStarted = 0;
        for(ActionDefinition actionDef : def.secondaryActions)
        {
            Action action = Actions.CreateAction(actionDef);
            if(action.CanStart(gunContext))
            {
                GunActions.AddAction(action);
                actionsStarted++;
            }
        }
        actionsStarted > 0 ? InteractionResult.SUCCESS :
*/

        return InteractionResult.FAIL;
    }

    public boolean canAttackBlock(BlockState blockState, Level world, BlockPos blockPos, Player player)
    {
        return false;
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction toolAction)
    {
        return FlansToolActions.DEFAULT_GUN_ACTIONS.contains(toolAction);
    }

    @Nonnull
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand)
    {
        ItemStack itemstack = player.getItemInHand(hand);
        return InteractionResultHolder.pass(itemstack);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer)
    {
        consumer.accept(FlanClientItemExtensions.create(this, new GunItemRenderer()));
    }

    // Random parameter overrides
    public boolean isEnchantable(ItemStack i) { return false; }
}
