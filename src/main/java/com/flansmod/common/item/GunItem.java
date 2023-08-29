package com.flansmod.common.item;

import com.flansmod.client.render.FlanClientItemExtensions;
import com.flansmod.client.render.guns.GunItemRenderer;
import com.flansmod.common.FlansMod;
import com.flansmod.common.actions.*;
import com.flansmod.common.gunshots.*;
import com.flansmod.common.types.elements.ModifierDefinition;
import com.flansmod.common.types.guns.ERepeatMode;
import com.flansmod.common.types.guns.GunDefinition;
import com.flansmod.common.types.magazines.MagazineDefinition;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
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
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class GunItem extends FlanItem
{
    public GunDefinition Def() { return FlansMod.GUNS.Get(DefinitionLocation); }

    // TODO: Place more generally private ActionStack GunActions;
    
    public GunItem(ResourceLocation defLoc, Properties properties)
    {
        super(defLoc, properties);
    }

    public GunContext GetContext(ItemStack stack)
    {
        return GunContext.GetOrCreate(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack,
                                @Nullable Level level,
                                @NotNull List<Component> tooltips,
                                TooltipFlag flags)
    {
        super.appendHoverText(stack, level, tooltips, flags);

        GunContext gunContext = GetContext(stack);
        if(gunContext.IsValid())
        {
            ActionGroupContext actionContext = ActionGroupContext.CreateFrom(gunContext, EActionInput.PRIMARY);
            if (actionContext.IsValid())
            {
                boolean advanced = flags.isAdvanced();
                boolean expanded = Minecraft.getInstance().options.keyShift.isDown();
                Component fireRateString = actionContext.RepeatMode() == ERepeatMode.SemiAuto ?
                    Component.translatable("tooltip.format.singlefire") :
                    Component.translatable("tooltip.format.fullautorpm", actionContext.RoundsPerMinute());

                // To calculate base "gun stats" without taking the bullet into consideration, assume a bullet with default stats
                GunshotContext gunshotContext = GunshotContext.CreateFrom(actionContext);
                if(gunshotContext.IsValid())
                {
                    if(expanded)
                    {
                        tooltips.add(Component.translatable("tooltip.format."+ ModifierDefinition.STAT_IMPACT_DAMAGE + ".advanced", gunshotContext.ImpactDamage()));
                        tooltips.add(Component.translatable("tooltip.format."+ ModifierDefinition.STAT_SHOT_VERTICAL_RECOIL + ".advanced", gunshotContext.VerticalRecoil()));
                        tooltips.add(Component.translatable("tooltip.format."+ ModifierDefinition.STAT_SHOT_SPREAD + ".advanced", gunshotContext.Spread()));
                    }
                    else
                    {
                        tooltips.add(Component.translatable(
                            "tooltip.format.primarystatline",
                            gunshotContext.ImpactDamage(),
                            gunshotContext.VerticalRecoil(),
                            fireRateString,
                            gunshotContext.Spread()));
                    }
                }

                if(!gunshotContext.IsValid() || expanded)
                {
                    switch(actionContext.RepeatMode())
                    {
                        case Toggle -> { tooltips.add(Component.translatable("tooltip.format.toggle.advanced")); }
                        case FullAuto -> { tooltips.add(Component.translatable("tooltip.format.fullautorpm.advanced", actionContext.RoundsPerMinute())); }
                        case SemiAuto -> { tooltips.add(Component.translatable("tooltip.format.singlefire.advanced")); }
                        case Minigun -> { tooltips.add(Component.translatable("tooltip.format.minigunrpm.advanced", actionContext.RoundsPerMinute())); }
                        case BurstFire -> { tooltips.add(Component.translatable("tooltip.format.burstfirerpm.advanced", actionContext.RoundsPerMinute())); }
                    }
                }

                MagazineDefinition magDef = actionContext.GetMagazineType(0);
                tooltips.add(Component.translatable("magazine." + magDef.Location.getNamespace() + "." + magDef.Location.getPath()));
                int primaryBullets = actionContext.GetMagazineSize(0);
                if(primaryBullets == 1)
                {
                    ItemStack bulletStack = actionContext.GetBulletAtIndex(0, 0);
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
                        ItemStack bulletStack = actionContext.GetBulletAtIndex(0, i);
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
    }

    public CompoundTag GetRootTag(ItemStack stack, EActionInput inputType)
    {
        return stack.getOrCreateTag().getCompound(inputType.GetRootTagName());
    }

    public CompoundTag GetMagTag(ItemStack stack, EActionInput inputType, int magIndex)
    {
        CompoundTag rootTag = GetRootTag(stack, inputType);
        final String magTag = "mag_" + magIndex;
        if (!rootTag.contains(magTag))
            rootTag.put(magTag, new CompoundTag());
        return rootTag.getCompound(magTag);
    }

    public MagazineDefinition GetMagazineType(ItemStack stack, EActionInput inputType, int magIndex)
    {
        // Get the root tag for our magazine
        CompoundTag magTags = GetMagTag(stack, inputType, magIndex);
        if(magTags.contains("type"))
        {
            String type = magTags.getString("type");
            ResourceLocation magLoc = new ResourceLocation(type);
            return FlansMod.MAGAZINES.Get(magLoc);
        }
        List<MagazineDefinition> matches = Def().GetMagazineSettings(inputType).GetMatchingMagazines();
        if(matches.size() > 0)
        {
            //FlansMod.LOGGER.warn("ItemStack " + stack + " had no mag type tag, but default found.");
            SetMagazineType(stack, inputType, magIndex, matches.get(0));
            return matches.get(0);
        }
        FlansMod.LOGGER.warn("ItemStack " + stack + " had no mag type tag, and no default for that gun found.");
        return MagazineDefinition.INVALID;
    }

    public void SetMagazineType(ItemStack stack, EActionInput inputType, int magIndex, MagazineDefinition magDef)
    {
        CompoundTag magTags = GetMagTag(stack, inputType, magIndex);
        magTags.putString("type", magDef.GetLocationString());
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
