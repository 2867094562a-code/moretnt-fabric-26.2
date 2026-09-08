package com.moretnt;

import java.util.Map;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

/** Adds small stacks of Glowstone Light Arrows to selected vanilla exploration chests. */
final class MoreTntLoot {
	private static final Map<ResourceKey<LootTable>, LootSpec> GLOWSTONE_ARROW_LOOT = Map.of(
		BuiltInLootTables.SIMPLE_DUNGEON, new LootSpec(0.45F, 4.0F, 12.0F),
		BuiltInLootTables.ABANDONED_MINESHAFT, new LootSpec(0.35F, 4.0F, 12.0F),
		BuiltInLootTables.STRONGHOLD_CORRIDOR, new LootSpec(0.35F, 6.0F, 16.0F),
		BuiltInLootTables.SHIPWRECK_SUPPLY, new LootSpec(0.30F, 5.0F, 15.0F),
		BuiltInLootTables.UNDERWATER_RUIN_BIG, new LootSpec(0.30F, 4.0F, 12.0F),
		BuiltInLootTables.VILLAGE_FLETCHER, new LootSpec(0.20F, 4.0F, 10.0F),
		BuiltInLootTables.BURIED_TREASURE, new LootSpec(0.20F, 12.0F, 24.0F)
	);

	private MoreTntLoot() {
	}

	static void register() {
		LootTableEvents.MODIFY.register((key, table, source, registries) -> {
			LootSpec spec = GLOWSTONE_ARROW_LOOT.get(key);
			if (spec == null) {
				return;
			}
			table.withPool(LootPool.lootPool()
				.setRolls(ConstantValue.exactly(1.0F))
				.when(LootItemRandomChanceCondition.randomChance(spec.chance()))
				.add(LootItem.lootTableItem(MoreTntMod.GLOWSTONE_LIGHT_ARROW)
					.apply(SetItemCountFunction.setCount(UniformGenerator.between(spec.minimum(), spec.maximum())))));
		});
	}

	private record LootSpec(float chance, float minimum, float maximum) {
	}
}
