package org.forsteri.createfantasticweapons.content.streetlamp;

import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.Pair;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import org.forsteri.createfantasticweapons.entry.Registrate;
import org.jetbrains.annotations.NotNull;

public class StreetLampItem extends BlockItem {
    public StreetLampItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    @Override
    public @NotNull InteractionResult place(@NotNull BlockPlaceContext ctx) {
        InteractionResult result = super.place(ctx);
        BlockPos pos;
        Make: {
            pos = ctx.getClickedPos();
            if (result != InteractionResult.FAIL) {
                break Make;
            }
            if (!ctx.getLevel().getBlockState(ctx.getClickedPos().below()).isAir()) {
                result = super.place(BlockPlaceContext.at(ctx, ctx.getClickedPos()
                        .relative(Direction.UP), Direction.UP));
                pos = ctx.getClickedPos().relative(Direction.UP);
            }

            if (result == InteractionResult.FAIL && ctx.getLevel()
                    .isClientSide()) {
                final BlockPos loc = pos;
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> showBounds(ctx, loc));
            }
        }

        if (result.consumesAction()) {
            for (BlockPos loc : new BlockPos[] {pos.above(), pos.below()}) {
                ctx.getLevel().setBlockAndUpdate(loc, Registrate.LAMP.getDefaultState().setValue(StreetLampBlock.IS_GHOST, true));
            }
        }

        return result;
    }

    @OnlyIn(Dist.CLIENT)
    public void showBounds(BlockPlaceContext context, BlockPos pos) {
        if (!(context.getPlayer() instanceof LocalPlayer localPlayer))
            return;
        CreateClient.OUTLINER.showAABB(Pair.of("street_lamp", pos), new AABB(pos).inflate(0, 1, 0))
                .colored(0xFF_ff5d6c);
        Lang.translate("large_water_wheel.not_enough_space")
                .color(0xFF_ff5d6c)
                .sendStatus(localPlayer);
    }
}
