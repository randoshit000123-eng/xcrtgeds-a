package de.playerboard;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * Eine 3x3 Tafel. Jeder der 9 Bloecke hat die Position (U,V) in der Tafel:
 * U = 0..2 von links nach rechts (aus Sicht des Betrachters), V = 0..2 von unten nach oben.
 * (1,1) ist der Mittelblock: er hat die BlockEntity und zeichnet die gesamte Tafel.
 */
public class PlayerBoardBlock extends BaseEntityBlock {
	public static final MapCodec<PlayerBoardBlock> CODEC = simpleCodec(PlayerBoardBlock::new);
	public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
	public static final IntegerProperty U = IntegerProperty.create("u", 0, 2);
	public static final IntegerProperty V = IntegerProperty.create("v", 0, 2);

	// 1/16 dicke Platte an der Rueckseite (Wandseite)
	private static final VoxelShape SHAPE_NORTH = Block.box(0, 0, 15, 16, 16, 16);
	private static final VoxelShape SHAPE_SOUTH = Block.box(0, 0, 0, 16, 16, 1);
	private static final VoxelShape SHAPE_WEST = Block.box(15, 0, 0, 16, 16, 16);
	private static final VoxelShape SHAPE_EAST = Block.box(0, 0, 0, 1, 16, 16);

	public PlayerBoardBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(U, 1).setValue(V, 1));
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, U, V);
	}

	// ---------- Geometrie-Helfer ----------

	public static BlockPos partPos(BlockPos center, Direction facing, int u, int v) {
		Direction right = facing.getCounterClockWise(); // "rechts" aus Sicht des Betrachters
		return center.relative(right, u - 1).above(v - 1);
	}

	public static BlockPos centerOf(BlockPos pos, BlockState state) {
		Direction right = state.getValue(FACING).getCounterClockWise();
		return pos.relative(right, 1 - state.getValue(U)).above(1 - state.getValue(V));
	}

	// ---------- Platzieren ----------

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		Direction clicked = ctx.getClickedFace();
		Direction facing = clicked.getAxis().isHorizontal() ? clicked : ctx.getHorizontalDirection().getOpposite();
		BlockPos center = ctx.getClickedPos();
		Level level = ctx.getLevel();

		for (int u = 0; u < 3; u++) {
			for (int v = 0; v < 3; v++) {
				BlockPos p = partPos(center, facing, u, v);
				if (level.isOutsideBuildHeight(p) || !level.getBlockState(p).canBeReplaced()) {
					return null; // nicht genug Platz fuer 3x3
				}
			}
		}
		return defaultBlockState().setValue(FACING, facing).setValue(U, 1).setValue(V, 1);
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
		super.setPlacedBy(level, pos, state, placer, stack);
		if (level.isClientSide()) return;
		Direction facing = state.getValue(FACING);
		for (int u = 0; u < 3; u++) {
			for (int v = 0; v < 3; v++) {
				if (u == 1 && v == 1) continue;
				level.setBlock(partPos(pos, facing, u, v), state.setValue(U, u).setValue(V, v), Block.UPDATE_ALL);
			}
		}
	}

	// ---------- Abbauen: gesamte Tafel entfernen, 1x droppen ----------

	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		if (!level.isClientSide()) {
			Direction facing = state.getValue(FACING);
			BlockPos center = centerOf(pos, state);
			if (!player.isCreative()) {
				popResource(level, pos, new ItemStack(this.asItem()));
			}
			for (int u = 0; u < 3; u++) {
				for (int v = 0; v < 3; v++) {
					BlockPos p = partPos(center, facing, u, v);
					if (p.equals(pos)) continue;
					BlockState other = level.getBlockState(p);
					if (other.is(this) && other.getValue(FACING) == facing) {
						level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
					}
				}
			}
		}
		return super.playerWillDestroy(level, pos, state, player);
	}

	// ---------- BlockEntity / Optik ----------

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		if (state.getValue(U) == 1 && state.getValue(V) == 1) {
			return new PlayerBoardBlockEntity(pos, state);
		}
		return null;
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
		return switch (state.getValue(FACING)) {
			case SOUTH -> SHAPE_SOUTH;
			case WEST -> SHAPE_WEST;
			case EAST -> SHAPE_EAST;
			default -> SHAPE_NORTH;
		};
	}
}
