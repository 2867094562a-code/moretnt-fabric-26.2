package com.moretnt;

import java.util.function.Consumer;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

/** Adds the purpose of each TNT type directly to its item tooltip. */
public final class MoreTntItem extends BlockItem {
	private final TntKind kind;

	public MoreTntItem(Block block, Item.Properties properties, TntKind kind) {
		super(block, properties);
		this.kind = kind;
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, context, display, tooltip, flag);
		tooltip.accept(Component.translatable("tooltip.moretnt." + kind.id()));
	}
}
