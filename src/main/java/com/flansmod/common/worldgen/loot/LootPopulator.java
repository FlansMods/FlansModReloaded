package com.flansmod.common.worldgen.loot;

import com.flansmod.packs.vendersgame.VendersGameMod;
import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Supplier;

public class LootPopulator extends LootModifier
{
	public static final Supplier<Codec<LootPopulator>> CODEC = Suppliers.memoize(() -> RecordCodecBuilder.create(inst -> codecStart(inst)
		.and(LootEntry.CODEC.listOf().fieldOf("entries").forGetter(m -> m.Entries))
		.apply(inst, LootPopulator::new)
	));

	private final List<LootEntry> Entries;
	private static class LootEntry
	{
		public static final Codec<LootEntry> CODEC = RecordCodecBuilder.create(inst -> inst.group(
			ExtraCodecs.POSITIVE_FLOAT.optionalFieldOf("chance", 1.0f).forGetter(m -> m.Chance),
			Codec.INT.optionalFieldOf("min_stack_size", 1).forGetter(m -> m.MinStackSize),
			Codec.INT.optionalFieldOf("max_stack_size", 1).forGetter(m -> m.MaxStackSize),
			ItemStack.CODEC.optionalFieldOf("item", ItemStack.EMPTY).forGetter(m -> m.Item))
			.apply(inst, LootEntry::new));

		public final float Chance;
		public final int MinStackSize;
		public final int MaxStackSize;
		public final ItemStack Item;

		public LootEntry(float chance, int minStack, int maxStack, ItemStack item)
		{
			Chance = chance;
			MinStackSize = minStack;
			MaxStackSize = maxStack;
			Item = item;
		}
	}

	public LootPopulator(final LootItemCondition[] conditionsIn, final List<LootEntry> entries) {
		super(conditionsIn);
		this.Entries = entries;
	}

	@Override
	@Nonnull
	protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context)
	{
		for(LootEntry entry : Entries)
		{
			if(context.getRandom().nextFloat() < entry.Chance)
			{
				int count = entry.MinStackSize == entry.MaxStackSize ? entry.MinStackSize : context.getRandom().nextInt(entry.MinStackSize, entry.MaxStackSize);
				generatedLoot.add(entry.Item.copyWithCount(count));
			}
		}
		return generatedLoot;
	}

	@Override
	public Codec<? extends IGlobalLootModifier> codec() {
		return CODEC.get();
	}
}
