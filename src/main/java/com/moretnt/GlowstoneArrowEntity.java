package com.moretnt;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

/** A normal arrow flight model that converts its first block impact into one glowstone block. */
public final class GlowstoneArrowEntity extends AbstractArrow {
	private static final int GLOWING_DURATION_TICKS = 20 * 10;
	public GlowstoneArrowEntity(EntityType<GlowstoneArrowEntity> type, Level level) {
		super(type, level);
	}

	public GlowstoneArrowEntity(Level level, LivingEntity shooter, ItemStack ammo, ItemStack weapon) {
		super(MoreTntMod.GLOWSTONE_LIGHT_ARROW_ENTITY, shooter, level, ammo, weapon);
	}

	public GlowstoneArrowEntity(Level level, double x, double y, double z, ItemStack ammo) {
		super(MoreTntMod.GLOWSTONE_LIGHT_ARROW_ENTITY, level);
		setPos(x, y, z);
		setPickupItemStack(ammo);
	}

	@Override
	protected void onHitBlock(BlockHitResult hit) {
		super.onHitBlock(hit);
		if (!level().isClientSide() && level() instanceof ServerLevel server) {
			BlockPos placement = hit.getBlockPos().relative(hit.getDirection());
			TntEffects.placeGlowstone(server, placement);
			spawnGlowBurst(server, hit.getLocation().x, hit.getLocation().y, hit.getLocation().z);
			discard();
		}
	}

	@Override
	protected void onHitEntity(EntityHitResult hit) {
		super.onHitEntity(hit);
		if (!level().isClientSide() && level() instanceof ServerLevel server && hit.getEntity() instanceof LivingEntity target) {
			target.addEffect(new MobEffectInstance(MobEffects.GLOWING, GLOWING_DURATION_TICKS), this);
			spawnGlowBurst(server, hit.getLocation().x, hit.getLocation().y, hit.getLocation().z);
		}
	}

	private static void spawnGlowBurst(ServerLevel level, double x, double y, double z) {
		level.sendParticles(ParticleTypes.GLOW, x, y, z, 24, 0.20D, 0.20D, 0.20D, 0.035D);
		level.sendParticles(ParticleTypes.END_ROD, x, y, z, 8, 0.12D, 0.12D, 0.12D, 0.020D);
	}

	@Override
	protected ItemStack getDefaultPickupItem() {
		return MoreTntMod.GLOWSTONE_LIGHT_ARROW.getDefaultInstance();
	}
}
