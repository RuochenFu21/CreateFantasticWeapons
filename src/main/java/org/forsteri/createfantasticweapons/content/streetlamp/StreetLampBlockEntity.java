package org.forsteri.createfantasticweapons.content.streetlamp;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.forsteri.createfantasticweapons.entry.Registrate;

import java.util.List;

public class StreetLampBlockEntity extends SmartBlockEntity{
    public StreetLampBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void tick() {
        super.tick();
        assert level != null;
        if (isGhost(getBlockPos())) {
            if (isNotNotGhost(getBlockPos().above()) && isNotNotGhost(getBlockPos().below()))
                level.setBlockAndUpdate(getBlockPos(), Blocks.AIR.defaultBlockState());
        } else {
            for (BlockPos pos : new BlockPos[] {getBlockPos().above(), getBlockPos().below()}) {
                if (!isGhost(pos)) {
                    level.destroyBlock(getBlockPos(), true);
                    break;
                }
            }
        }
    }

    public boolean isGhost(BlockPos pos) {
        assert level != null;
        if (!level.getBlockState(pos).getBlock().equals(Registrate.LAMP.get()))
            return false;
        return level.getBlockState(pos).getValue(StreetLampBlock.IS_GHOST);
    }

    public boolean isNotNotGhost(BlockPos pos) {
        assert level != null;
        if (!level.getBlockState(pos).getBlock().equals(Registrate.LAMP.get()))
            return true;
        return level.getBlockState(pos).getValue(StreetLampBlock.IS_GHOST);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {

    }
}
