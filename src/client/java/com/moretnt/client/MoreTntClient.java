package com.moretnt.client;

import com.moretnt.MoreTntMod;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.TntRenderer;

/** Renders utility TNT entities with the vanilla primed-TNT swell and flash animation. */
public final class MoreTntClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.register(MoreTntMod.PRIMED_TNT, TntRenderer::new);
	}
}
