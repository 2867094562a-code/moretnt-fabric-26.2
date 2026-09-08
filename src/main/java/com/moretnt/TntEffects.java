package com.moretnt;

import java.util.Set;
import java.util.function.Predicate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

/** Server-side effects. Selective variants never touch containers or unbreakable blocks. */
final class TntEffects {
	private static final Set<Block> VANILLA_ORES = Set.of(
		Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE, Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE,
		Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE, Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE,
		Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE, Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE,
		Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE, Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE,
		Blocks.NETHER_GOLD_ORE, Blocks.NETHER_QUARTZ_ORE, Blocks.ANCIENT_DEBRIS
	);

	private TntEffects() {
	}

	static void detonate(ServerLevel level, BlockPos center, TntKind kind) {
		boomSound(level, center);
		primeNearbyTnt(level, center, Math.min(kind.radius(), 12));
		switch (kind) {
			case WATER -> fill(level, center, kind.radius(), Blocks.WATER.defaultBlockState());
			case LAVA -> fill(level, center, kind.radius(), Blocks.LAVA.defaultBlockState());
			case FROST -> replaceWater(level, center, kind.radius());
			case FIRE -> ignite(level, center, kind.radius());
			case GLOW -> fill(level, center, kind.radius(), Blocks.GLOWSTONE.defaultBlockState());
			case SPONGE -> drain(level, center, kind.radius());
			case TUNNEL -> tunnel(level, center, true);
			case SHAFT -> shaft(level, center);
			case TRENCH -> trench(level, center);
			case SURFACE -> surface(level, center, kind.radius());
			case MEGA -> level.explode(null, center.getX() + 0.5D, center.getY() + 0.5D, center.getZ() + 0.5D, 16.0F, false, Level.ExplosionInteraction.TNT);
			case COLOSSAL -> level.explode(null, center.getX() + 0.5D, center.getY() + 0.5D, center.getZ() + 0.5D, 48.0F, false, Level.ExplosionInteraction.TNT);
			case NUCLEAR -> level.explode(null, center.getX() + 0.5D, center.getY() + 0.5D, center.getZ() + 0.5D, 64.0F, false, Level.ExplosionInteraction.TNT);
			case SUPER_NUCLEAR -> level.explode(null, center.getX() + 0.5D, center.getY() + 0.5D, center.getZ() + 0.5D, 96.0F, false, Level.ExplosionInteraction.TNT);
			case BEDROCK -> breakBedrock(level, center, kind.radius());
			case VOID -> selective(level, center, kind.radius(), state -> !isOre(state), false);
			case QUARRY -> selective(level, center, kind.radius(), TntEffects::isMineableRockOrOre, true);
			case DEMOLITION -> selective(level, center, kind.radius(), state -> !isOre(state), true);
			default -> selective(level, center, kind.radius(), state -> matches(kind, state), true);
		}
	}

	/**
	 * Selective and environment effects do not use vanilla's block-damaging explosion path, so
	 * they explicitly pass their blast on to nearby TNT. Each target becomes a physical primed
	 * entity with a randomized short fuse, exactly like vanilla TNT chain reactions.
	 */
	private static void primeNearbyTnt(ServerLevel level, BlockPos center, int radius) {
		forEachSphere(center, radius, pos -> {
			BlockState state = level.getBlockState(pos);
			if (state.getBlock() instanceof UtilityTntBlock moreTnt) {
				moreTnt.primeFromChain(level, pos, state);
			} else if (state.is(Blocks.TNT)) {
				TntBlock.prime(level, pos);
			}
		});
	}

	private static void boomSound(ServerLevel level, BlockPos pos) {
		level.playSound(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 2.0F, 0.9F);
	}

	private static void selective(ServerLevel level, BlockPos center, int radius, Predicate<BlockState> target, boolean drops) {
		forEachSphere(center, radius, pos -> {
			BlockState state = level.getBlockState(pos);
			if (target.test(state) && canBreak(level, pos, state)) {
				level.destroyBlock(pos, drops, null, 512);
			}
		});
	}

	private static void fill(ServerLevel level, BlockPos center, int radius, BlockState replacement) {
		forEachSphere(center, radius, pos -> {
			if (level.getBlockState(pos).isAir()) {
				level.setBlock(pos, replacement, 3);
			}
		});
	}

	private static void replaceWater(ServerLevel level, BlockPos center, int radius) {
		forEachSphere(center, radius, pos -> {
			if (level.getBlockState(pos).getFluidState().isSourceOfType(Fluids.WATER)) {
				level.setBlock(pos, Blocks.ICE.defaultBlockState(), 3);
			}
		});
	}

	private static void ignite(ServerLevel level, BlockPos center, int radius) {
		forEachSphere(center, radius, pos -> {
			if (level.getBlockState(pos).isAir() && !level.getBlockState(pos.below()).isAir()) {
				level.setBlock(pos, Blocks.FIRE.defaultBlockState(), 3);
			}
		});
	}

	private static void drain(ServerLevel level, BlockPos center, int radius) {
		forEachSphere(center, radius, pos -> {
			if (!level.getBlockState(pos).getFluidState().isEmpty()) {
				level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
			}
		});
	}

	private static void tunnel(ServerLevel level, BlockPos center, boolean northSouth) {
		for (int forward = -12; forward <= 12; forward++) {
			for (int side = -1; side <= 1; side++) {
				for (int y = -1; y <= 1; y++) {
					BlockPos pos = northSouth ? center.offset(side, y, forward) : center.offset(forward, y, side);
					breakIfMineable(level, pos, true);
				}
			}
		}
	}

	private static void shaft(ServerLevel level, BlockPos center) {
		for (int y = -12; y <= 12; y++) {
			for (int x = -1; x <= 1; x++) {
				for (int z = -1; z <= 1; z++) {
					breakIfMineable(level, center.offset(x, y, z), true);
				}
			}
		}
	}

	private static void trench(ServerLevel level, BlockPos center) {
		for (int x = -12; x <= 12; x++) {
			for (int z = -2; z <= 2; z++) {
				for (int y = -3; y <= 1; y++) {
					breakIfMineable(level, center.offset(x, y, z), true);
				}
			}
		}
	}

	private static void surface(ServerLevel level, BlockPos center, int radius) {
		for (int x = -radius; x <= radius; x++) {
			for (int z = -radius; z <= radius; z++) {
				if (x * x + z * z > radius * radius) {
					continue;
				}
				for (int y = -1; y <= 1; y++) {
					BlockPos pos = center.offset(x, y, z);
					BlockState state = level.getBlockState(pos);
					if (matches(TntKind.SOIL, state) && canBreak(level, pos, state)) {
						level.destroyBlock(pos, true, null, 512);
					}
				}
			}
		}
	}

	private static void breakBedrock(ServerLevel level, BlockPos center, int radius) {
		forEachSphere(center, radius, pos -> {
			if (level.getBlockState(pos).getBlock() == Blocks.BEDROCK) {
				level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
			}
		});
	}

	private static void breakIfMineable(ServerLevel level, BlockPos pos, boolean drops) {
		BlockState state = level.getBlockState(pos);
		if (isMineableRockOrOre(state) && canBreak(level, pos, state)) {
			level.destroyBlock(pos, drops, null, 512);
		}
	}

	private static void forEachSphere(BlockPos center, int radius, java.util.function.Consumer<BlockPos> action) {
		int radiusSquared = radius * radius;
		for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -radius, -radius), center.offset(radius, radius, radius))) {
			int x = pos.getX() - center.getX();
			int y = pos.getY() - center.getY();
			int z = pos.getZ() - center.getZ();
			if (x * x + y * y + z * z <= radiusSquared) {
				action.accept(pos.immutable());
			}
		}
	}

	private static boolean canBreak(ServerLevel level, BlockPos pos, BlockState state) {
		return !state.isAir() && state.getDestroySpeed(level, pos) >= 0.0F && level.getBlockEntity(pos) == null;
	}

	private static boolean matches(TntKind kind, BlockState state) {
		Block block = state.getBlock();
		return switch (kind) {
			case LUMBER -> state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES) || block == Blocks.BAMBOO || block == Blocks.BAMBOO_BLOCK;
			case SOIL -> state.is(BlockTags.DIRT) || state.is(BlockTags.MUD) || state.is(BlockTags.SAND) || block == Blocks.GRAVEL || block == Blocks.CLAY || block == Blocks.SOUL_SAND || block == Blocks.SOUL_SOIL;
			case STONE -> state.is(BlockTags.BASE_STONE_OVERWORLD) || block == Blocks.COBBLESTONE || block == Blocks.COBBLED_DEEPSLATE || block == Blocks.TUFF || block == Blocks.CALCITE;
			case ORE_MINER -> isOre(state);
			case ORE_SAFE -> !isOre(state);
			case CROP -> state.is(BlockTags.CROPS) || state.is(BlockTags.FLOWERS) || block == Blocks.VINE || block == Blocks.GLOW_LICHEN || block == Blocks.SUGAR_CANE || block == Blocks.CACTUS;
			case GLASS -> block == Blocks.GLASS || block == Blocks.GLASS_PANE || block == Blocks.TINTED_GLASS || state.is(BlockTags.IMPERMEABLE);
			case WOOL -> state.is(BlockTags.WOOL) || state.is(BlockTags.WOOL_CARPETS) || state.is(BlockTags.BEDS);
			case CONCRETE -> state.is(BlockTags.CONCRETE) || state.is(BlockTags.CONCRETE_POWDERS) || state.is(BlockTags.TERRACOTTA) || state.is(BlockTags.GLAZED_TERRACOTTA);
			case ICE -> state.is(BlockTags.ICE) || state.is(BlockTags.SNOW);
			case NETHER -> block == Blocks.NETHERRACK || block == Blocks.BASALT || block == Blocks.SMOOTH_BASALT || block == Blocks.BLACKSTONE || block == Blocks.GILDED_BLACKSTONE || block == Blocks.SOUL_SOIL;
			case ENDSTONE -> block == Blocks.END_STONE || block == Blocks.PURPUR_BLOCK || block == Blocks.PURPUR_PILLAR || block == Blocks.END_STONE_BRICKS;
			case WOOD -> state.is(BlockTags.LOGS) || state.is(BlockTags.PLANKS) || state.is(BlockTags.WOODEN_STAIRS) || state.is(BlockTags.WOODEN_SLABS) || state.is(BlockTags.WOODEN_FENCES) || state.is(BlockTags.WOODEN_DOORS) || state.is(BlockTags.WOODEN_TRAPDOORS);
			case OBSIDIAN -> block == Blocks.OBSIDIAN || block == Blocks.CRYING_OBSIDIAN || block == Blocks.RESPAWN_ANCHOR;
			default -> false;
		};
	}

	private static boolean isMineableRockOrOre(BlockState state) {
		return isOre(state) || matches(TntKind.STONE, state) || matches(TntKind.NETHER, state) || matches(TntKind.ENDSTONE, state);
	}

	private static boolean isOre(BlockState state) {
		if (VANILLA_ORES.contains(state.getBlock())) {
			return true;
		}
		Identifier id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
		String path = id.getPath();
		return path.endsWith("_ore") || path.contains("ore_") || path.startsWith("ore_") || path.contains("ancient_debris");
	}
}
