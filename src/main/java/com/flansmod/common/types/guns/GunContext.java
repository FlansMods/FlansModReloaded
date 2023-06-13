package com.flansmod.common.types.guns;

import com.flansmod.common.actions.EActionSet;
import com.flansmod.common.item.AttachmentItem;
import com.flansmod.common.item.GunItem;
import com.flansmod.common.types.attachments.AttachmentDefinition;
import com.flansmod.common.types.attachments.EAttachmentType;
import com.flansmod.common.types.elements.ActionDefinition;
import com.flansmod.common.types.elements.ModifierDefinition;
import com.flansmod.util.Transform;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class GunContext
{
	public static final GunContext INVALID = new GunContext(null, Transform.Identity(), null, null);

	// Source variables
	public ItemStack stack;
	public Entity shootFrom;
	public Transform shootOrigin;
	public Entity owner;

	// Cached variables?
	private CachedGunStats cachedPrimaryAction = null;
	private CachedGunStats cachedSecondaryAction = null;

	public DamageSource CreateDamageSource()
	{
		return new IndirectEntityDamageSource(
			"gun",
			shootFrom,
			owner
		);
	}

	public boolean IsValid()
	{
		return stack != null && shootFrom != null;
	}

	@Nonnull
	public static GunContext TryCreateFromEntity(Entity entity, InteractionHand hand)
	{
		if(entity instanceof LivingEntity living)
			return CreateFromLiving(living, hand);
		else // if(IFlanGunWielder)
			return GunContext.INVALID;
	}

	@Nonnull
	public static GunContext CreateFromLiving(LivingEntity entity, InteractionHand hand)
	{
		if(entity.getItemInHand(hand).getItem() instanceof GunItem gun)
		{
			return new GunContext(entity, CreateLookTransform(entity), entity, entity.getItemInHand(hand));
		}
		return GunContext.INVALID;
	}

	@Nonnull
	public static GunContext CreateFromPlayer(Player player, InteractionHand hand)
	{
		if(player.getItemInHand(hand).getItem() instanceof GunItem gun)
		{
			return new GunContext(player, CreateLookTransform(player), player, player.getItemInHand(hand));
		}
		return GunContext.INVALID;
	}

	@Nonnull
	public static GunContext CreateFromContext(UseOnContext useOnContext)
	{
		if(useOnContext.getItemInHand().getItem() instanceof GunItem gun)
		{
			return new GunContext(
				useOnContext.getPlayer(),
				CreateLookTransform(useOnContext.getPlayer()),
				useOnContext.getPlayer(),
				useOnContext.getItemInHand());
		}
		return GunContext.INVALID;
	}

	private GunContext(Entity shootFrom, Transform shootOrigin, Entity owner, ItemStack stack)
	{
		this.shootFrom = shootFrom;
		this.shootOrigin = shootOrigin;
		this.owner = owner;
		this.stack = stack;
	}

	@Nonnull
	private static Transform CreateLookTransform(Entity entity)
	{
		if(entity == null)
			return Transform.Identity();
		Transform transform = new Transform(entity.getEyePosition());
		transform.RotateLocalEuler(entity.getXRot(), entity.getYRot(), 0.0f);
		return transform;
	}

	/// Public accessors of accumulated stats
	// Make sure we process attachments when getting stats and results about the gun
	public GunDefinition GunDef()
	{
		if(stack != null && stack.getItem() instanceof GunItem gunItem)
			return gunItem.Def();
		return GunDefinition.INVALID;
	}

	public ItemStack GetGunStack()
	{
		return stack;
	}

	public ItemStack GetBulletStack(int index)
	{
		CompoundTag ammoTags = GetOrCreateTags("ammo");
		String key = Integer.toString(index);
		if(!ammoTags.contains(key))
		{
			CompoundTag itemTags = new CompoundTag();
			ItemStack empty = ItemStack.EMPTY.copy();
			empty.save(itemTags);
			ammoTags.put(key, itemTags);
			return empty;
		}

		return ItemStack.of(ammoTags.getCompound(key));
	}

	public ItemStack GetAttachmentStack(EAttachmentType attachType)
	{
		CompoundTag attachmentTags = GetOrCreateTags("attachments");
		if(attachmentTags.contains(attachType.toString()))
		{
			CompoundTag forThisType = attachmentTags.getCompound(attachType.toString());
			return ItemStack.of(forThisType);
		}
		return ItemStack.EMPTY.copy();
	}

	public List<ItemStack> GetAttachmentStacks()
	{
		List<ItemStack> stacks = new ArrayList<>();
		CompoundTag attachmentTags = GetOrCreateTags("attachments");
		for(String key : attachmentTags.getAllKeys())
		{
			CompoundTag attachmentStackTags = attachmentTags.getCompound(key);
			stacks.add(ItemStack.of(attachmentStackTags));
		}
		return stacks;
	}

	public List<ModifierDefinition> GetApplicableModifiers(String stat, EActionSet actionSet)
	{
		List<ModifierDefinition> applicableModifiers = new ArrayList<>();
		List<ItemStack> attachmentStacks = GetAttachmentStacks();
		for(ItemStack attachmentStack : attachmentStacks)
		{
			if(attachmentStack.getItem() instanceof AttachmentItem attachmentItem)
			{
				AttachmentDefinition attachDef = attachmentItem.Def();
				for(ModifierDefinition modifierDef : attachDef.modifiers)
				{
					if(modifierDef.AppliesTo(stat, actionSet))
					{
						applicableModifiers.add(modifierDef);
					}
				}
			}
		}
		return applicableModifiers;
	}

	public Map<String, List<ModifierDefinition>> GetAllApplicableModifiers(EActionSet actionSet)
	{
		Map<String, List<ModifierDefinition>> results = new IdentityHashMap<>();
		List<ItemStack> attachmentStacks = GetAttachmentStacks();
		for(ItemStack attachmentStack : attachmentStacks)
		{
			if(attachmentStack.getItem() instanceof AttachmentItem attachmentItem)
			{
				AttachmentDefinition attachDef = attachmentItem.Def();
				for(ModifierDefinition modifierDef : attachDef.modifiers)
				{
					if(modifierDef.AppliesTo(actionSet))
					{
						List<ModifierDefinition> modList = results.get(modifierDef.Stat);
						if(modList == null)
						{
							modList = new ArrayList<>();
							results.put(modifierDef.Stat, modList);
						}
						modList.add(modifierDef);
					}
				}
			}
		}
		return results;
	}

	public boolean HasAction(EActionSet actionSet) { return GetShootActionDefinition(actionSet) != null; }
	@Nonnull
	public ActionDefinition[] GetActionDefinitions(EActionSet actionSet)
	{
		if(GunDef() == null)
			return new ActionDefinition[0];

		return GunDef().GetActions(actionSet);
	}
	@Nullable
	public ActionDefinition GetShootActionDefinition(EActionSet actionSet)
	{
		if(GunDef() == null)
			return null;

		// TODO: Check attachments for changes in action
		// e.g. An underslung grenade launcher attachment

		for(ActionDefinition def : GunDef().GetActions(actionSet))
			if(def.actionType == EActionType.Shoot)
				return def;

		return null;
	}
	@Nonnull
	public ActionDefinition[] GetLookAtActions()
	{
		if(GunDef() == null)
			return new ActionDefinition[0];

		return GunDef().lookAtActions;
	}


	public CachedGunStats GetStatBlock(EActionSet actionSet) { return GetStatCache(actionSet); }
	public float VerticalRecoil(EActionSet actionSet) { return GetStatCache(actionSet).VerticalRecoil();}
	public float HorizontalRecoil(EActionSet actionSet) { return GetStatCache(actionSet).HorizontalRecoil();}
	public float Spread(EActionSet actionSet) { return GetStatCache(actionSet).Spread();}
	public float Speed(EActionSet actionSet) { return GetStatCache(actionSet).Speed();}
	public int Count(EActionSet actionSet) { return GetStatCache(actionSet).Count();}
	public float TimeToNextShot(EActionSet actionSet) { return GetStatCache(actionSet).TimeToNextShot();}
	public float PenetrationPower(EActionSet actionSet) { return GetStatCache(actionSet).PenetrationPower();}
	public float BaseDamage(EActionSet actionSet) { return GetStatCache(actionSet).BaseDamage();}
	public float Knockback(EActionSet actionSet) { return GetStatCache(actionSet).Knockback();}
	public float MultiplierVsPlayers(EActionSet actionSet) { return GetStatCache(actionSet).MultiplierVsPlayers();}
	public float MultiplierVsVehicles(EActionSet actionSet) { return GetStatCache(actionSet).MultiplierVsVehicles();}
	public float SplashDamageRadius(EActionSet actionSet) { return GetStatCache(actionSet).SplashDamageRadius();}
	public float SplashDamageFalloff(EActionSet actionSet) { return GetStatCache(actionSet).SplashDamageFalloff();}
	public float SetFireToTarget(EActionSet actionSet) { return GetStatCache(actionSet).SetFireToTarget();}
	public float FireSpreadRadius(EActionSet actionSet) { return GetStatCache(actionSet).FireSpreadRadius();}
	public float FireSpreadAmount(EActionSet actionSet) { return GetStatCache(actionSet).FireSpreadAmount();}
	public ESpreadPattern SpreadPattern(EActionSet actionSet) { return GetStatCache(actionSet).SpreadPattern();}


	private CompoundTag GetOrCreateTags(String key)
	{
		CompoundTag root = stack.getOrCreateTag();
		if(!root.contains(key))
		{
			root.put(key, new CompoundTag());
		}
		return root.getCompound(key);
	}

	@Nonnull
	private CachedGunStats GetStatCache(EActionSet actionSet)
	{
		switch(actionSet)
		{
			case PRIMARY:
				if(cachedPrimaryAction == null)
					cachedPrimaryAction = CalculateStats(actionSet);
				return cachedPrimaryAction;
			case SECONDARY:
				if(cachedSecondaryAction == null)
					cachedSecondaryAction = CalculateStats(actionSet);
				return cachedSecondaryAction;
			default:
				return new CachedGunStats();
		}
	}

	private CachedGunStats CalculateStats(EActionSet actionSet)
	{
		CachedGunStats stats = new CachedGunStats();

		ActionDefinition shootAction = GetShootActionDefinition(actionSet);
		if(shootAction != null)
		{
			stats.InitializeFrom(shootAction.shootStats);
		}

		var modifierMap = GetAllApplicableModifiers(actionSet);
		for(var kvp : modifierMap.entrySet())
		{
			stats.ApplyModifiers(kvp.getKey(), kvp.getValue());
		}

		return stats;
	}
}
