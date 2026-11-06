package de.playerboard;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Traegt nur den Renderer. Die Spielerliste kommt clientseitig aus der Tab-Liste. */
public class PlayerBoardBlockEntity extends BlockEntity {
	public PlayerBoardBlockEntity(BlockPos pos, BlockState state) {
		super(PlayerBoardMod.PLAYER_BOARD_ENTITY, pos, state);
	}
}
