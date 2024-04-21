package org.forsteri.createfantasticweapons.content.streetlamp;

import com.jozufozu.flywheel.core.model.ModelUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.forsteri.createfantasticweapons.entry.Registrate;

public class StreetLampRenderer extends SmartBlockEntityRenderer<StreetLampBlockEntity> {
    public StreetLampRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(StreetLampBlockEntity blockEntity, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        super.renderSafe(blockEntity, partialTicks, ms, buffer, light, overlay);

        if (blockEntity.getLevel() == null)
            return;

        if (blockEntity.getLevel().getBlockState(blockEntity.getBlockPos()).getBlock() != Registrate.LAMP.get())
            return;

        BlockState state = blockEntity.getLevel().getBlockState(blockEntity.getBlockPos());
        StreetLampAttachBlocks block = state.getValue(StreetLampBlock.LAMP_TYPE);

        ms.translate(0, 1, 0);
        ms.translate(9/32f, 4/32f, 8/32f);
        ms.scale(7/16f, 7/16f, 7/16f);

        if (state.getValue(StreetLampBlock.IS_GHOST))
            return;

        if (!(block.item.get() instanceof BlockItem blockItem))
            return;

        BlockState blockState = blockItem.getBlock().defaultBlockState();

        if (blockState.is(Blocks.REDSTONE_LAMP))
            blockState = blockState.setValue(RedstoneLampBlock.LIT,true);

        Minecraft mc = Minecraft.getInstance();
        mc.getBlockRenderer()
                .renderSingleBlock(blockState, ms, buffer, light, overlay, ModelUtil.VIRTUAL_DATA, RenderType.cutoutMipped());
    }
}
