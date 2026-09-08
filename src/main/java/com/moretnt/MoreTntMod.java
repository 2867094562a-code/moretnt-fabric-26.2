package com.moretnt;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

/** Registers the 30 More TNT blocks and their matching inventory items. */
public final class MoreTntMod implements ModInitializer {
	public static final String MOD_ID = "moretnt";
	private static final List<Item> TNT_ITEMS = new ArrayList<>();

	@Override
	public void onInitialize() {
		for (TntKind kind : TntKind.values()) {
			Identifier id = id(kind.id());
			ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, id);
			Block block = Registry.register(
				BuiltInRegistries.BLOCK,
				blockKey,
				new UtilityTntBlock(kind, BlockBehaviour.Properties.ofFullCopy(Blocks.TNT).setId(blockKey).ignitedByLava())
			);
			ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id);
			TNT_ITEMS.add(Registry.register(BuiltInRegistries.ITEM, itemKey, new MoreTntItem(block, new Item.Properties().setId(itemKey), kind)));
		}

		ResourceKey<CreativeModeTab> tabKey = ResourceKey.create(Registries.CREATIVE_MODE_TAB, id("moretnt"));
		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, tabKey, CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
			.title(Component.translatable("itemGroup.moretnt"))
			.icon(() -> TNT_ITEMS.getFirst().getDefaultInstance())
			.displayItems((parameters, output) -> TNT_ITEMS.forEach(output::accept))
			.build());
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
