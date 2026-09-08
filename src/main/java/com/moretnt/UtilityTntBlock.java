package com.moretnt;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

/** Stationary, redstone-compatible TNT with a four-second fuse. */
public final class UtilityTntBlock extends Block {
	private static final int FUSE_TICKS = 80;
	private static final BooleanProperty LIT = BlockStateProperties.LIT;
	private final TntKind kind;

	public UtilityTntBlock(TntKind kind, BlockBehaviour.Properties properties) {
		super(properties);
		this.kind = kind;
		registerDefaultState(stateDefinition.any().setValue(LIT, false));
	}

	@Override
	protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
		if (oldState.getBlock() != this && level.hasNeighborSignal(pos)) {
			arm(level, pos, state);
		}
	}

	@Override
	protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, net.minecraft.world.level.redstone.Orientation orientation, boolean movedByPiston) {
		if (level.hasNeighborSignal(pos)) {
			arm(level, pos, state);
		}
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (!stack.is(Items.FLINT_AND_STEEL) && !stack.is(Items.FIRE_CHARGE)) {
			return super.useItemOn(stack, state, level, pos, player, hand, hit);
		}

		if (!level.isClientSide() && arm(level, pos, state)) {
			if (stack.is(Items.FLINT_AND_STEEL)) {
				stack.hurtAndBreak(1, player, hand);
			} else {
				stack.consume(1, player);
			}
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (state.getBlock() == this && state.getValue(LIT)) {
			level.removeBlock(pos, false);
			TntEffects.detonate(level, pos, kind);
		}
	}

	@Override
	public void wasExploded(ServerLevel level, BlockPos pos, net.minecraft.world.level.Explosion explosion) {
		if (level.getGameRules().get(GameRules.TNT_EXPLODES)) {
			TntEffects.detonate(level, pos, kind);
		}
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(LIT);
	}

	private boolean arm(Level level, BlockPos pos, BlockState state) {
		if (level.isClientSide() || state.getValue(LIT) || level instanceof ServerLevel server && !server.getGameRules().get(GameRules.TNT_EXPLODES)) {
			return false;
		}

		level.setBlock(pos, state.setValue(LIT, true), 3);
		level.scheduleTick(pos, this, FUSE_TICKS);
		level.playSound(null, pos, SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
		return true;
	}
}
