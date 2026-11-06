package de.playerboard.client;

import de.playerboard.PlayerBoardMod;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

public class PlayerBoardClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		BlockEntityRenderers.register(PlayerBoardMod.PLAYER_BOARD_ENTITY, PlayerBoardRenderer::new);
	}
}
