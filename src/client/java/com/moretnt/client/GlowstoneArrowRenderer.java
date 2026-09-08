package com.moretnt.client;

import com.moretnt.GlowstoneArrowEntity;
import com.moretnt.MoreTntMod;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.resources.Identifier;

/** Reuses the vanilla arrow geometry with the mod's warm-glow arrow sprite. */
public final class GlowstoneArrowRenderer extends ArrowRenderer<GlowstoneArrowEntity, ArrowRenderState> {
	private static final Identifier TEXTURE = MoreTntMod.id("textures/entity/projectiles/glowstone_light_arrow.png");

	public GlowstoneArrowRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public ArrowRenderState createRenderState() {
		return new ArrowRenderState();
	}

	@Override
	protected Identifier getTextureLocation(ArrowRenderState state) {
		return TEXTURE;
	}
}
