package de.playerboard.client;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;

public class PlayerBoardRenderState extends BlockEntityRenderState {
	public Direction facing = Direction.NORTH;
	public final List<Component> names = new ArrayList<>();
}
