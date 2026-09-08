package com.moretnt;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

/** A normal arrow flight model that converts its first block impact into one glowstone block. */
public final class GlowstoneArrowEntity extends AbstractArrow {
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
			discard();
		}
	}

	@Override
	protected ItemStack getDefaultPickupItem() {
		return MoreTntMod.GLOWSTONE_LIGHT_ARROW.getDefaultInstance();
	}
}
