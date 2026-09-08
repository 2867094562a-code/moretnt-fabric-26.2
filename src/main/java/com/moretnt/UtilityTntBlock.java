package com.moretnt;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

/** Stationary, redstone-compatible TNT with a four-second fuse. */
public final class UtilityTntBlock extends Block {
	private static final int FUSE_STEP_TICKS = 5;
	private static final int FUSE_STEPS = 16;
	private static final BooleanProperty LIT = BlockStateProperties.LIT;
	private static final BooleanProperty UNSTABLE = BlockStateProperties.UNSTABLE;
	private static final BooleanProperty FLASH = BooleanProperty.create("flash");
	private static final IntegerProperty FUSE = IntegerProperty.create("fuse", 0, FUSE_STEPS);
	private final TntKind kind;

	public UtilityTntBlock(TntKind kind, BlockBehaviour.Properties properties) {
		super(properties);
		this.kind = kind;
		registerDefaultState(stateDefinition.any()
			.setValue(LIT, false)
			.setValue(UNSTABLE, false)
			.setValue(FLASH, false)
			.setValue(FUSE, 0));
	}

	@Override
	protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
		if (oldState.getBlock() != this && (level.hasNeighborSignal(pos) || hasHeatSource(level, pos))) {
			arm(level, pos, state);
		}
	}

	@Override
	protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, net.minecraft.world.level.redstone.Orientation orientation, boolean movedByPiston) {
		if (level.hasNeighborSignal(pos) || hasHeatSource(level, pos)) {
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
		if (state.getBlock() != this || !state.getValue(LIT)) {
			return;
		}

		int fuse = state.getValue(FUSE);
		if (fuse <= 1) {
			level.removeBlock(pos, false);
			TntEffects.detonate(level, pos, kind);
			return;
		}

		BlockState nextState = state.setValue(FUSE, fuse - 1).setValue(FLASH, !state.getValue(FLASH));
		level.setBlock(pos, nextState, 3);
		level.scheduleTick(pos, this, FUSE_STEP_TICKS);
	}

	@Override
	public void wasExploded(ServerLevel level, BlockPos pos, net.minecraft.world.level.Explosion explosion) {
		if (level.getGameRules().get(GameRules.TNT_EXPLODES)) {
			TntEffects.detonate(level, pos, kind);
		}
	}

	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		if (!level.isClientSide() && !player.getAbilities().instabuild && state.getValue(UNSTABLE)) {
			// The block is removed immediately after this hook, so an unstable command-placed TNT
			// uses the vanilla-style instant break trigger rather than silently cancelling its fuse.
			if (level instanceof ServerLevel server && server.getGameRules().get(GameRules.TNT_EXPLODES)) {
				TntEffects.detonate(server, pos, kind);
			}
		}
		return super.playerWillDestroy(level, pos, state, player);
	}

	@Override
	protected void onProjectileHit(Level level, BlockState state, BlockHitResult hit, Projectile projectile) {
		if (level instanceof ServerLevel server && projectile.isOnFire() && projectile.mayInteract(server, hit.getBlockPos())) {
			arm(level, hit.getBlockPos(), state);
		}
	}

	@Override
	public boolean dropFromExplosion(net.minecraft.world.level.Explosion explosion) {
		return false;
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (!state.getValue(LIT)) {
			return;
		}

		double x = pos.getX() + 0.5D + (random.nextFloat() - 0.5D) * 0.24D;
		double y = pos.getY() + 0.78D;
		double z = pos.getZ() + 0.5D + (random.nextFloat() - 0.5D) * 0.24D;
		level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0D, 0.02D, 0.0D);
		if (state.getValue(FLASH)) {
			level.addParticle(ParticleTypes.SMALL_FLAME, x, y + 0.08D, z, 0.0D, 0.01D, 0.0D);
		}
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(LIT, UNSTABLE, FLASH, FUSE);
	}

	private boolean arm(Level level, BlockPos pos, BlockState state) {
		if (level.isClientSide() || state.getValue(LIT) || level instanceof ServerLevel server && !server.getGameRules().get(GameRules.TNT_EXPLODES)) {
			return false;
		}

		level.setBlock(pos, state.setValue(LIT, true).setValue(FLASH, false).setValue(FUSE, FUSE_STEPS), 3);
		level.scheduleTick(pos, this, FUSE_STEP_TICKS);
		level.playSound(null, pos, SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
		return true;
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
