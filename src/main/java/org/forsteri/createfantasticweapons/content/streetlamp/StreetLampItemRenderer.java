package org.forsteri.createfantasticweapons.content.streetlamp;

import com.jozufozu.flywheel.core.model.ModelUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModel;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModelRenderer;
import com.simibubi.create.foundation.item.render.PartialItemModelRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.level.block.state.BlockState;

public class StreetLampItemRenderer extends CustomRenderedItemModelRenderer {
    @Override
    protected void render(ItemStack stack, CustomRenderedItemModel model, PartialItemModelRenderer renderer, ItemDisplayContext transformType, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        renderer.render(model.getOriginalModel(), light);

        StreetLampAttachBlocks block = StreetLampItem.blockOf(stack);

        ms.translate(-7/32f, 10/16f, -4/16f);
        ms.scale(7/16f, 7/16f, 7/16f);

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
