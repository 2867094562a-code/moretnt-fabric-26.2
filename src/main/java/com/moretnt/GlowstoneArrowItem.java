package com.moretnt;

import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** A vanilla-compatible arrow item that turns into a single-use glowstone light on block impact. */
public final class GlowstoneArrowItem extends ArrowItem {
	public GlowstoneArrowItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public AbstractArrow createArrow(Level level, ItemStack ammo, LivingEntity shooter, ItemStack weapon) {
		return new GlowstoneArrowEntity(level, shooter, ammo.copyWithCount(1), weapon);
	}

	@Override
	public Projectile asProjectile(Level level, Position position, ItemStack stack, Direction direction) {
		GlowstoneArrowEntity arrow = new GlowstoneArrowEntity(level, position.x(), position.y(), position.z(), stack.copyWithCount(1));
		arrow.pickup = AbstractArrow.Pickup.ALLOWED;
		return arrow;
	}
}
