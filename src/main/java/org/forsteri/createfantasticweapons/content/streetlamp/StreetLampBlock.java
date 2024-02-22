package org.forsteri.createfantasticweapons.content.streetlamp;

import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.forsteri.createfantasticweapons.entry.Registrate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StreetLampBlock extends Block implements IBE<StreetLampBlockEntity> {
    public static BooleanProperty IS_GHOST = BooleanProperty.create("is_ghost");

    public StreetLampBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(IS_GHOST, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> state) {
        super.createBlockStateDefinition(state.add(IS_GHOST));
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
}
