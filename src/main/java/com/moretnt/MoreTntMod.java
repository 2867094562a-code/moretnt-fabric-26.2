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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

/** Registers the 30 More TNT blocks and their matching inventory items. */
public final class MoreTntMod implements ModInitializer {
	public static final String MOD_ID = "moretnt";
	/** One primed entity type stores the selected TNT variant in its persistent data. */
	public static final EntityType<MoreTntPrimedEntity> PRIMED_TNT = Registry.register(
		BuiltInRegistries.ENTITY_TYPE,
		ResourceKey.create(Registries.ENTITY_TYPE, id("primed_more_tnt")),
		EntityType.Builder.of(MoreTntPrimedEntity::new, MobCategory.MISC)
			.sized(0.98F, 0.98F)
			.clientTrackingRange(10)
			.updateInterval(10)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, id("primed_more_tnt")))
	);
	public static final EntityType<GlowstoneArrowEntity> GLOWSTONE_LIGHT_ARROW_ENTITY = Registry.register(
		BuiltInRegistries.ENTITY_TYPE,
		ResourceKey.create(Registries.ENTITY_TYPE, id("glowstone_light_arrow")),
		EntityType.Builder.<GlowstoneArrowEntity>of(GlowstoneArrowEntity::new, MobCategory.MISC)
			.sized(0.5F, 0.5F)
			.clientTrackingRange(4)
			.updateInterval(20)
			.build(ResourceKey.create(Registries.ENTITY_TYPE, id("glowstone_light_arrow")))
	);
	public static final Item GLOWSTONE_LIGHT_ARROW = Registry.register(
		BuiltInRegistries.ITEM,
		ResourceKey.create(Registries.ITEM, id("glowstone_light_arrow")),
		new GlowstoneArrowItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id("glowstone_light_arrow"))))
	);
	private static final List<Item> TNT_ITEMS = new ArrayList<>();

	@Override
	public void onInitialize() {
		MoreTntLoot.register();
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
			.displayItems((parameters, output) -> {
				TNT_ITEMS.forEach(output::accept);
				output.accept(GLOWSTONE_LIGHT_ARROW);
			})
			.build());
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
