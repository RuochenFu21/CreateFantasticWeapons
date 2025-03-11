package org.forsteri.createfantasticweapons.content.streetlamp;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.item.render.SimpleCustomRenderer;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.outliner.Outliner;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.fml.DistExecutor;
import org.forsteri.createfantasticweapons.CreateFantasticWeapons;
import org.forsteri.createfantasticweapons.entry.Registrate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

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

    @Nullable
    @Override
    protected BlockState getPlacementState(@NotNull BlockPlaceContext p_40613_) {
        var parent = super.getPlacementState(p_40613_);
        if (parent == null)
            return null;

        if (!p_40613_.getItemInHand().getOrCreateTag().contains("lamp_type"))
            return parent;

        String tag = p_40613_.getItemInHand().getOrCreateTag().getString("lamp_type");

        if (Arrays.stream(StreetLampAttachBlocks.values()).noneMatch(attach -> attach.name().equals(tag)))
            return null;

        parent = parent.setValue(StreetLampBlock.LAMP_TYPE, StreetLampAttachBlocks.valueOf(tag));
        return parent;
    }

    @OnlyIn(Dist.CLIENT)
    public void showBounds(BlockPlaceContext context, BlockPos pos) {
        if (!(context.getPlayer() instanceof LocalPlayer localPlayer))
            return;
        Outliner.getInstance().showAABB(Pair.of("street_lamp", pos), new AABB(pos).inflate(0, 1, 0))
                .colored(0xFF_ff5d6c);
        Create.lang().translate("large_water_wheel.not_enough_space")
                .color(0xFF_ff5d6c)
                .sendStatus(localPlayer);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        if (slot != EquipmentSlot.MAINHAND)
            return super.getAttributeModifiers(slot, stack);
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE,
                new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", 5 + blockOf(stack).addedDamage, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED,
                new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", -2.9D + blockOf(stack).addedAttackSpeed, AttributeModifier.Operation.ADDITION));
        return builder.build();
    }

    @Override
    public void appendHoverText(@NotNull ItemStack item, @Nullable Level p_40573_, @NotNull List<Component> tooltip, @NotNull TooltipFlag p_40575_) {
        super.appendHoverText(item, p_40573_, tooltip, p_40575_);

        tooltip.add(
                Component.translatable(blockOf(item).item.get().getDescriptionId()).withStyle(ChatFormatting.GRAY)
        );
    }

    public static StreetLampAttachBlocks blockOf(ItemStack stack) {
        StreetLampAttachBlocks block;

        Stuff: {
            if (!stack.getOrCreateTag().contains("lamp_type")) {
                block = StreetLampAttachBlocks.NONE;
                break Stuff;
            }

            String tag = stack.getOrCreateTag().getString("lamp_type");

            if (Arrays.stream(StreetLampAttachBlocks.values()).noneMatch(attach -> attach.name().equals(tag))) {
                block = StreetLampAttachBlocks.NONE;
                break Stuff;
            }

            block = StreetLampAttachBlocks.valueOf(tag);
        }

        return block;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(SimpleCustomRenderer.create(this, new StreetLampItemRenderer()));
    }

    @Override
    public boolean hurtEnemy(@NotNull ItemStack p_41395_, @NotNull LivingEntity hitEntity, LivingEntity hitter) {
        ItemStack item = hitter.getItemInHand(hitter.getUsedItemHand());
        if (!item.is(Registrate.LAMP.get().asItem()))
            return false;

        StreetLampAttachBlocks blockType = StreetLampItem.blockOf(item);
        hitEntity.setSecondsOnFire(blockType.ignitionTime);

        for (Supplier<MobEffectInstance> effect : blockType.effects) {
            hitEntity.addEffect(effect.get());
        }

        if (blockType == StreetLampAttachBlocks.SOUL_LANTERN) {
            if (hitter instanceof ServerPlayer player) {
                player.getCooldowns().addCooldown(this, 60);
            }
        }

        return true;
    }
}
