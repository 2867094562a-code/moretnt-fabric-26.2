package com.moretnt;

import java.util.function.Consumer;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

/** Adds the purpose of each TNT type directly to its item tooltip. */
public final class MoreTntItem extends BlockItem {
	private final TntKind kind;

	public MoreTntItem(Block block, Item.Properties properties, TntKind kind) {
		super(block, properties);
		this.kind = kind;
	}

	/**
	 * Glow TNT is intentionally a throwable charge rather than a placeable block. It still uses
	 * the same physical primed-TNT entity as every other variant, so gravity, collisions, fuse,
	 * and the normal detonation rules are retained.
	 */
	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (kind != TntKind.GLOW) {
			return super.use(level, player, hand);
		}
		return throwGlowCharge(level, player, player.getItemInHand(hand));
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		if (kind != TntKind.GLOW) {
			return super.useOn(context);
		}
		Player player = context.getPlayer();
		return player == null ? InteractionResult.FAIL : throwGlowCharge(context.getLevel(), player, context.getItemInHand());
	}

	private InteractionResult throwGlowCharge(Level level, Player player, ItemStack stack) {
		if (!level.isClientSide()) {
			Vec3 look = player.getLookAngle();
			MoreTntPrimedEntity entity = MoreTntPrimedEntity.create(level, player.blockPosition(), kind, getBlock().defaultBlockState());
			entity.setPos(
				player.getX() + look.x * 0.65D,
				player.getEyeY() - 0.20D + look.y * 0.65D,
				player.getZ() + look.z * 0.65D
			);
			entity.setDeltaMovement(look.scale(0.90D).add(0.0D, 0.15D, 0.0D));
			entity.setFuse(30);
			level.addFreshEntity(entity);
			level.playSound(null, BlockPos.containing(entity.position()), SoundEvents.TNT_PRIMED, SoundSource.PLAYERS, 1.0F, 1.0F);
			if (!player.getAbilities().instabuild) {
				stack.consume(1, player);
			}
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, context, display, tooltip, flag);
		tooltip.accept(Component.translatable("tooltip.moretnt." + kind.id()));
	}
}
