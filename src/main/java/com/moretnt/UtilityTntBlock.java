package com.moretnt;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;

/** A standard-physics TNT block whose final effect depends on its {@link TntKind}. */
public final class UtilityTntBlock extends Block {
	private static final net.minecraft.world.level.block.state.properties.BooleanProperty UNSTABLE = BlockStateProperties.UNSTABLE;
	private final TntKind kind;

	public UtilityTntBlock(TntKind kind, BlockBehaviour.Properties properties) {
		super(properties);
		this.kind = kind;
		registerDefaultState(stateDefinition.any().setValue(UNSTABLE, false));
	}

	@Override
	protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
		if (oldState.getBlock() != this && (level.hasNeighborSignal(pos) || hasHeatSource(level, pos))) {
			prime(level, pos, state, false);
		}
	}

	@Override
	protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, net.minecraft.world.level.redstone.Orientation orientation, boolean movedByPiston) {
		if (level.hasNeighborSignal(pos) || hasHeatSource(level, pos)) {
			prime(level, pos, state, false);
		}
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (!stack.is(Items.FLINT_AND_STEEL) && !stack.is(Items.FIRE_CHARGE)) {
			return super.useItemOn(stack, state, level, pos, player, hand, hit);
		}

		if (!level.isClientSide() && prime(level, pos, state, false)) {
			if (stack.is(Items.FLINT_AND_STEEL)) {
				stack.hurtAndBreak(1, player, hand);
			} else {
				stack.consume(1, player);
			}
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public void wasExploded(ServerLevel level, BlockPos pos, net.minecraft.world.level.Explosion explosion) {
		if (level.getGameRules().get(GameRules.TNT_EXPLODES)) {
			BlockState state = level.getBlockState(pos);
			MoreTntPrimedEntity entity = spawnPrimed(level, pos, state);
			entity.shortenFuseForChainReaction();
		}
	}

	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		if (!level.isClientSide() && !player.getAbilities().instabuild && state.getValue(UNSTABLE)) {
			prime(level, pos, state, false);
		}
		return super.playerWillDestroy(level, pos, state, player);
	}

	@Override
	protected void onProjectileHit(Level level, BlockState state, BlockHitResult hit, Projectile projectile) {
		if (level instanceof ServerLevel server && projectile.isOnFire() && projectile.mayInteract(server, hit.getBlockPos())) {
			prime(level, hit.getBlockPos(), state, false);
		}
	}

	@Override
	public boolean dropFromExplosion(net.minecraft.world.level.Explosion explosion) {
		return false;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(UNSTABLE);
	}

	/** Used by utility effects as well as ordinary vanilla ignition routes. */
	boolean primeFromChain(ServerLevel level, BlockPos pos, BlockState state) {
		return prime(level, pos, state, true);
	}

	private boolean prime(Level level, BlockPos pos, BlockState state, boolean shortFuse) {
		if (level.isClientSide() || !(level instanceof ServerLevel server) || !server.getGameRules().get(GameRules.TNT_EXPLODES) || state.getBlock() != this) {
			return false;
		}

		MoreTntPrimedEntity entity = spawnPrimed(server, pos, state);
		if (shortFuse) {
			entity.shortenFuseForChainReaction();
		}
		level.removeBlock(pos, false);
		level.playSound(null, pos, SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
		return true;
	}

	private MoreTntPrimedEntity spawnPrimed(ServerLevel level, BlockPos pos, BlockState state) {
		MoreTntPrimedEntity entity = MoreTntPrimedEntity.create(level, pos, kind, state.setValue(UNSTABLE, false));
		level.addFreshEntity(entity);
		return entity;
	}

	private static boolean hasHeatSource(Level level, BlockPos pos) {
		for (Direction direction : Direction.values()) {
			BlockState neighbor = level.getBlockState(pos.relative(direction));
			if (neighbor.getBlock() instanceof net.minecraft.world.level.block.BaseFireBlock || neighbor.getFluidState().is(net.minecraft.tags.FluidTags.LAVA)) {
				return true;
			}
		}
		return false;
	}
}
