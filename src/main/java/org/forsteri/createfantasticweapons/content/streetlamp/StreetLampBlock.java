package org.forsteri.createfantasticweapons.content.streetlamp;

import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.forsteri.createfantasticweapons.entry.Registrate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StreetLampBlock extends Block implements IBE<StreetLampBlockEntity> {
    public static BooleanProperty IS_GHOST = BooleanProperty.create("is_ghost");
    public static EnumProperty<StreetLampAttachBlocks> LAMP_TYPE = EnumProperty.create("lamp_type", StreetLampAttachBlocks.class);

    public StreetLampBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(IS_GHOST, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> state) {
        super.createBlockStateDefinition(state.add(IS_GHOST, LAMP_TYPE));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        if (!level.getBlockState(pos.above()).canBeReplaced())
            return null;
        if (!level.getBlockState(pos.below()).canBeReplaced())
            return null;

        return super.getStateForPlacement(context);
    }

    @Override
    public Class<StreetLampBlockEntity> getBlockEntityClass() {
        return StreetLampBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends StreetLampBlockEntity> getBlockEntityType() {
        return Registrate.LAMP_BE.get();
    }

    @SuppressWarnings("deprecation")
    @Override
    public @NotNull VoxelShape getVisualShape(@NotNull BlockState p_60479_, @NotNull BlockGetter p_60480_, @NotNull BlockPos p_60481_, @NotNull CollisionContext p_60482_) {
        return Shapes.empty();
    }

    @Override
    @SuppressWarnings("deprecation")
    public @NotNull VoxelShape getShape(@NotNull BlockState p_60555_, @NotNull BlockGetter p_60556_, @NotNull BlockPos p_60557_, @NotNull CollisionContext p_60558_) {
        return Shapes.box(3/16f, 0, 3/16f, 13/16f, 1, 13/16f);
    }

    @Override
    public @NotNull List<ItemStack> getDrops(@NotNull BlockState p_287732_, LootParams.@NotNull Builder p_287596_) {
        ArrayList<ItemStack> drops = new ArrayList<>();

        for (ItemStack drop : super.getDrops(p_287732_, p_287596_)) {
            if (drop.getItem() == Registrate.LAMP.get().asItem()) {
                drop.getOrCreateTag().putString("lamp_type", p_287732_.getValue(LAMP_TYPE).name());
            }
            drops.add(drop);
        }

        return drops;
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState p_60503_, @NotNull Level level, @NotNull BlockPos pos, Player p_60506_, @NotNull InteractionHand p_60507_, @NotNull BlockHitResult p_60508_) {
        if (p_60506_.isShiftKeyDown()) {
            level.destroyBlock(pos, true);
            return InteractionResult.SUCCESS;
        }

        for (StreetLampAttachBlocks block : StreetLampAttachBlocks.values()) {
            if (block.item.get() == p_60506_.getItemInHand(p_60507_).getItem()) {
                p_60506_.getItemInHand(p_60507_).shrink(1);

                if (p_60503_.getValue(IS_GHOST)) {
                    if (level.getBlockState(pos.above()).getBlock() == Registrate.LAMP.get() && !level.getBlockState(pos.above()).getValue(IS_GHOST)) {
                        level.setBlockAndUpdate(pos.above(), level.getBlockState(pos.above()).setValue(LAMP_TYPE, block));
                        level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5f, pos.getY() + 2.5f, pos.getZ() + 0.5f, p_60503_.getValue(LAMP_TYPE).item.get().getDefaultInstance()));
                        level.setBlockAndUpdate(pos.above(2), level.getBlockState(pos.above(2)).setValue(LAMP_TYPE, block));
                    } else {
                        level.setBlockAndUpdate(pos.below(), level.getBlockState(pos.below()).setValue(LAMP_TYPE, block));
                        level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, p_60503_.getValue(LAMP_TYPE).item.get().getDefaultInstance()));
                        level.setBlockAndUpdate(pos.below(2), level.getBlockState(pos.below(2)).setValue(LAMP_TYPE, block));
                    }
                } else {
                    level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5f, pos.getY() + 1.5f, pos.getZ() + 0.5f, p_60503_.getValue(LAMP_TYPE).item.get().getDefaultInstance()));
                    level.setBlockAndUpdate(pos.above(), level.getBlockState(pos.above()).setValue(LAMP_TYPE, block));
                    level.setBlockAndUpdate(pos.below(), level.getBlockState(pos.below()).setValue(LAMP_TYPE, block));
                }

                level.setBlockAndUpdate(pos, p_60503_.setValue(LAMP_TYPE, block));

                return InteractionResult.SUCCESS;
            }
        }

        return super.use(p_60503_, level, pos, p_60506_, p_60507_, p_60508_);
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        String name = state.getValue(StreetLampBlock.LAMP_TYPE).name();
        StreetLampAttachBlocks blockType;

        if (Arrays.stream(StreetLampAttachBlocks.values()).noneMatch(attach -> attach.name().equals(name)))
            return 0;

        blockType = StreetLampAttachBlocks.valueOf(name);

        Item item = blockType.item.get();

        if (!(item instanceof BlockItem blockItem))
            return 0;

        BlockState blockState = blockItem.getBlock().defaultBlockState();

        if (blockState.is(Blocks.REDSTONE_LAMP))
            blockState = blockState.setValue(RedstoneLampBlock.LIT,true);

        return blockState.getLightEmission();
    }
}
