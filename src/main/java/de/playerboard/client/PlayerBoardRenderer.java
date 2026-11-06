package de.playerboard.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import de.playerboard.PlayerBoardBlock;
import de.playerboard.PlayerBoardBlockEntity;
import java.util.Comparator;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.PlayerInfo;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Zeichnet die Spielerliste (wie die Tab-Anzeige) auf die gesamte 3x3 Flaeche.
 * Koordinatensystem: Ursprung = Mitte des Mittelblocks, Tafel reicht von -1.5 bis +1.5.
 */
public class PlayerBoardRenderer implements BlockEntityRenderer<PlayerBoardBlockEntity, PlayerBoardRenderState> {
	private static final int ROWS = 14;      // Zeilen pro Spalte
	private static final int MAX_COLS = 3;   // max. Spalten (=> 42 Namen)
	private static final float COL_W = 100f; // Spaltenbreite in Font-Pixeln
	private static final float LINE_H = 10f;
	private static final float HEADER_H = 14f;
	private static final float USABLE = 2.76f; // von 3.0 Bloecken nutzbar (Rand)

	private final Font font;

	public PlayerBoardRenderer(BlockEntityRendererProvider.Context context) {
		this.font = context.font();
	}

	@Override
	public PlayerBoardRenderState createRenderState() {
		return new PlayerBoardRenderState();
	}

	@Override
	public void extractRenderState(PlayerBoardBlockEntity be, PlayerBoardRenderState state, float partialTick,
			Vec3 cameraPos, ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
		BlockEntityRenderer.super.extractRenderState(be, state, partialTick, cameraPos, crumbling);
		state.facing = be.getBlockState().getValue(PlayerBoardBlock.FACING);

		state.names.clear();
		ClientPacketListener connection = Minecraft.getInstance().getConnection();
		if (connection == null) return;
		for (PlayerInfo info : connection.getOnlinePlayers()) {
			Component display = info.getTabListDisplayName();
			state.names.add(display != null ? display : Component.literal(info.getProfile().name()));
		}
		state.names.sort(Comparator.comparing(c -> c.getString().toLowerCase(Locale.ROOT)));
	}

	@Override
	public void submit(PlayerBoardRenderState state, PoseStack pose, SubmitNodeCollector queue, CameraRenderState camera) {
		int total = state.names.size();
		int cols = Math.max(1, Math.min(MAX_COLS, (total + ROWS - 1) / ROWS));
		int capacity = cols * ROWS;

		// Skalierung so waehlen, dass Spalten und Zeilen auf 3x3 passen
		float s = Math.min(USABLE / (cols * COL_W), USABLE / (HEADER_H + ROWS * LINE_H));

		pose.pushPose();
		pose.translate(0.5, 0.5, 0.5);                                   // Blockmitte
		pose.mulPose(Axis.YP.rotationDegrees(-state.facing.toYRot()));  // zur Blickrichtung drehen
		pose.translate(0f, 0f, -0.5f + 1f / 16f + 0.005f);               // auf die Oberflaeche der Platte
		pose.scale(s, -s, s);                                            // 1 Font-Pixel = s Bloecke, y nach unten

		float contentH = HEADER_H + ROWS * LINE_H;
		float top = -contentH / 2f;
		float left = -(cols * COL_W) / 2f;

		// Kopfzeile
		Component header = Component.translatable("text.playerboard.header", total);
		queue.submitText(pose, -font.width(header) / 2f, top, header.getVisualOrderText(),
				false, Font.DisplayMode.NORMAL, state.lightCoords, 0xFFFFD700, 0, 0);

		// Namen
		for (int i = 0; i < Math.min(total, capacity); i++) {
			Component name = state.names.get(i);
			if (i == capacity - 1 && total > capacity) {
				name = Component.translatable("text.playerboard.more", total - capacity + 1);
			}
			int col = i / ROWS;
			int row = i % ROWS;
			float x = left + col * COL_W + 4f;
			float y = top + HEADER_H + row * LINE_H;
			queue.submitText(pose, x, y, name.getVisualOrderText(),
					false, Font.DisplayMode.NORMAL, state.lightCoords, 0xFFFFFFFF, 0, 0);
		}

		pose.popPose();
	}
}
