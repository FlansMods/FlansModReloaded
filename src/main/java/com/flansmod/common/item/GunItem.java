package com.flansmod.common.item;

import com.flansmod.client.render.FlanClientItemExtensions;
import com.flansmod.client.render.guns.GunItemRenderer;
import com.flansmod.common.FlansMod;
import com.flansmod.common.actions.*;
import com.flansmod.common.gunshots.*;
import com.flansmod.common.types.elements.ActionDefinition;
import com.flansmod.common.types.guns.ERepeatMode;
import com.flansmod.common.types.guns.GunDefinition;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class GunItem extends FlanItem
{
    private ResourceLocation definitionLocation;
    public GunDefinition Def() { return FlansMod.GUNS.Get(definitionLocation); }

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

    public GunContext GetContext(ItemStack stack)
    {
        return GunContext.CreateFrom(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack,
                                @Nullable Level level,
                                List<Component> tooltips,
                                TooltipFlag flags)
    {
        GunContext gunContext = GetContext(stack);
        if(gunContext.IsValid())
        {
            ActionContext actionContext = ActionContext.CreateFrom(gunContext, EActionInput.PRIMARY);
            if (actionContext.IsValid())
            {
                ActionDefinition actionDef = actionContext.GetShootActionDefinition();
                if(actionDef != null)
                {
                    CachedActionStats actionStats = actionContext.BuildActionStatCache(actionDef);
                    CachedGunStats gunStats = actionContext.BuildGunStatCache(actionDef);

                    Component fireRateString = actionStats.RepeatMode == ERepeatMode.SemiAuto ?
                        Component.translatable("tooltip.format.singlefire") :
                        Component.translatable("tooltip.format.fullautorpm", actionStats.RoundsPerMinute());

                    tooltips.add(Component.translatable(
                        "tooltip.format.primarystatline",
                        gunStats.BaseDamage,
                        gunStats.VerticalRecoil,
                        fireRateString,
                        gunStats.Spread));

                    if(flags.isAdvanced() || Minecraft.getInstance().options.keyShift.isDown())
                    {
                        tooltips.add(Component.translatable("tooltip.format.damage.advanced", gunStats.BaseDamage));
                        tooltips.add(Component.translatable("tooltip.format.recoil.advanced", gunStats.VerticalRecoil));
                        switch(actionStats.RepeatMode)
                        {
                            case Toggle -> { tooltips.add(Component.translatable("tooltip.format.toggle.advanced")); }
                            case FullAuto -> { tooltips.add(Component.translatable("tooltip.format.fullautorpm.advanced", actionStats.RoundsPerMinute())); }
                            case SemiAuto -> { tooltips.add(Component.translatable("tooltip.format.singlefire.advanced")); }
                            case Minigun -> { tooltips.add(Component.translatable("tooltip.format.minigunrpm.advanced", actionStats.RoundsPerMinute())); }
                            case BurstFire -> { tooltips.add(Component.translatable("tooltip.format.burstfirerpm.advanced", actionStats.RoundsPerMinute())); }
                        }
                        tooltips.add(Component.translatable("tooltip.format.spread.advanced", gunStats.Spread));
                    }
                }
            }

            for (ItemStack attachmentStack : gunContext.GetAttachmentStacks())
            {
                tooltips.add(Component.translatable("tooltip.format.attached", attachmentStack.getHoverName()));
            }

            int primaryBullets = gunContext.GetNumBulletStacks(EActionInput.PRIMARY);
            if(primaryBullets == 1)
            {
                ItemStack bulletStack = gunContext.GetBulletStack(EActionInput.PRIMARY, 0);
                if(!bulletStack.isEmpty())
                {
                    if (bulletStack.isDamageableItem())
                        tooltips.add(Component.translatable("tooltip.format.single_bullet_stack_with_durability", bulletStack.getHoverName(), bulletStack.getMaxDamage() - bulletStack.getDamageValue(), bulletStack.getMaxDamage()));
                    else
                        tooltips.add(Component.translatable("tooltip.format.single_bullet_stack", bulletStack.getHoverName()));
                }
            }
            else
            {
                HashMap<Item, ItemStack> bulletCounts = new HashMap<>();
                for (int i = 0; i < primaryBullets; i++)
                {
                    ItemStack bulletStack = gunContext.GetBulletStack(EActionInput.PRIMARY, i);
                    if(!bulletStack.isEmpty())
                    {
                        if (!bulletCounts.containsKey(bulletStack.getItem()))
                        {
                            bulletCounts.put(bulletStack.getItem(), bulletStack.copy());
                        } else
                        {
                            bulletCounts.replace(bulletStack.getItem(), bulletStack.copyWithCount(bulletCounts.get(bulletStack.getItem()).getCount() + 1));
                        }
                    }
                }
                for(var kvp : bulletCounts.entrySet())
                {
                    tooltips.add(Component.translatable("tooltip.format.multiple_bullet_stack", kvp.getValue().getCount(), kvp.getValue().getHoverName()));
                }
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void ClientHandleMouse(Player player, ItemStack stack, InputEvent.InteractionKeyMappingTriggered event)
    {
        if(event.isAttack()) // Primary actions
        {
          //  FlansModClient.ACTIONS_CLIENT.ClientInputEvent(player, event.getHand(), EActionInput.PRIMARY);
            //player.startUsingItem(event.getHand());
        }
        else if(event.isUseItem()) // Secondary actions
        {
            //FlansModClient.ACTIONS_CLIENT.ClientInputEvent(player, event.getHand(), EActionInput.SECONDARY);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void ClientUpdateUsing(Player player, ItemStack stack, LivingEntityUseItemEvent.Tick event)
    {
       // int useRemaining = FlansModClient.ACTIONS_CLIENT.ClientInputHeldUpdate(player, event.getEntity().getUsedItemHand());

       // event.setDuration(useRemaining);
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
