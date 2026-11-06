package de.playerboard;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;

public class PlayerBoardMod implements ModInitializer {
	public static final String MOD_ID = "playerboard";

	public static Block PLAYER_BOARD;
	public static Item PLAYER_BOARD_ITEM;
	public static BlockEntityType<PlayerBoardBlockEntity> PLAYER_BOARD_ENTITY;

	@Override
	public void onInitialize() {
		Identifier id = Identifier.fromNamespaceAndPath(MOD_ID, "player_board");

		// Block
		ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, id);
		PLAYER_BOARD = Registry.register(BuiltInRegistries.BLOCK, id,
				new PlayerBoardBlock(BlockBehaviour.Properties.of()
						.setId(blockKey)
						.strength(1.5f)
						.sound(SoundType.STONE)
						.noOcclusion()
						.pushReaction(PushReaction.BLOCK)));

		// Item
		ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id);
		PLAYER_BOARD_ITEM = Registry.register(BuiltInRegistries.ITEM, id,
				new BlockItem(PLAYER_BOARD, new Item.Properties().setId(itemKey).useBlockDescriptionPrefix()));

		// BlockEntity (nur der mittlere Block der 3x3-Tafel hat eine)
		PLAYER_BOARD_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id,
				FabricBlockEntityTypeBuilder.<PlayerBoardBlockEntity>create(PlayerBoardBlockEntity::new, PLAYER_BOARD).build());

		// Creative-Tab
		ResourceKey<CreativeModeTab> tabKey = ResourceKey.create(
				BuiltInRegistries.CREATIVE_MODE_TAB.key(), Identifier.fromNamespaceAndPath(MOD_ID, "main"));
		CreativeModeTab tab = FabricCreativeModeTab.builder()
				.icon(() -> new ItemStack(PLAYER_BOARD_ITEM))
				.title(Component.translatable("creativeTab.playerboard"))
				.displayItems((params, output) -> output.accept(PLAYER_BOARD_ITEM))
				.build();
		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, tabKey, tab);
	}
}
