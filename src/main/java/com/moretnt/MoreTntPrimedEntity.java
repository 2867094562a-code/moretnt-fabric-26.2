package com.moretnt;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * A real PrimedTnt entity carrying the More TNT variant that created it.
 *
 * <p>It deliberately retains vanilla TNT gravity, bounce, drag, fuse, smoke, swelling, and
 * short-fuse chain-reaction behaviour. Only the final detonation is replaced with the selected
 * utility effect.</p>
 */
public final class MoreTntPrimedEntity extends PrimedTnt {
	private static final String TAG_KIND = "moretnt_kind";
	private TntKind kind = TntKind.LUMBER;

	public MoreTntPrimedEntity(EntityType<? extends MoreTntPrimedEntity> type, Level level) {
		super(type, level);
	}

	public static MoreTntPrimedEntity create(Level level, BlockPos pos, TntKind kind, BlockState displayState) {
		MoreTntPrimedEntity entity = new MoreTntPrimedEntity(MoreTntMod.PRIMED_TNT, level);
		entity.kind = kind;
		entity.setPos(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
		RandomSource random = level.getRandom();
		double angle = random.nextDouble() * Math.PI * 2.0D;
		entity.setDeltaMovement(-Math.sin(angle) * 0.02D, 0.2D, -Math.cos(angle) * 0.02D);
		entity.setFuse(DEFAULT_FUSE_TIME);
		entity.setBlockState(displayState);
		entity.xo = entity.getX();
		entity.yo = entity.getY();
		entity.zo = entity.getZ();
		return entity;
	}

	public void shortenFuseForChainReaction() {
		setFuse(PrimedTnt.getRandomShortFuse(getFuse(), level().getRandom()));
	}

	@Override
	public void tick() {
		// This is the vanilla PrimedTnt tick flow. Keeping it here (rather than a stationary
		// block timer) is what gives the TNT normal gravity, bounce, knockback response, smoke,
		// swell/flash rendering, and physical chain reactions.
		handlePortal();
		applyGravity();
		move(MoverType.SELF, getDeltaMovement());
		applyEffectsFromBlocks();
		setDeltaMovement(getDeltaMovement().scale(getAirDrag()));
		if (onGround()) {
			setDeltaMovement(getDeltaMovement().multiply(0.7D, -0.5D, 0.7D));
		}

		int fuse = getFuse() - 1;
		setFuse(fuse);
		if (fuse <= 0) {
			discard();
			if (!level().isClientSide() && level() instanceof ServerLevel server && server.getGameRules().get(GameRules.TNT_EXPLODES)) {
				TntEffects.detonate(server, BlockPos.containing(getX(), getY(0.0625D), getZ()), kind);
			}
			return;
		}

		updateFluidInteraction();
		if (level().isClientSide()) {
			level().addParticle(ParticleTypes.SMOKE, getX(), getY() + 0.5D, getZ(), 0.0D, 0.0D, 0.0D);
		}
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putString(TAG_KIND, kind.id());
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		kind = TntKind.byId(input.getStringOr(TAG_KIND, TntKind.LUMBER.id()));
	}
}
