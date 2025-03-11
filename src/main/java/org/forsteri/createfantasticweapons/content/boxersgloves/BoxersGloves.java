package org.forsteri.createfantasticweapons.content.boxersgloves;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.content.equipment.armor.BacktankUtil;
import com.simibubi.create.infrastructure.config.AllConfigs;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.Vanishable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.forsteri.createfantasticweapons.content.bigsyringe.BigSyringe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class BoxersGloves extends Item implements Vanishable {
    private final Multimap<Attribute, AttributeModifier> defaultModifiers;

    public final boolean extraBouncy;

    public BoxersGloves(Properties p_41383_, boolean extraBouncy) {
        super(p_41383_);
        this.extraBouncy = extraBouncy;
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        float attackDamage = 3;
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", attackDamage, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", 0, AttributeModifier.Operation.ADDITION));
        if (extraBouncy)
            builder.put(Attributes.ATTACK_KNOCKBACK, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", 1, AttributeModifier.Operation.ADDITION));
        this.defaultModifiers = builder.build();
    }

    public @NotNull Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(@NotNull EquipmentSlot p_43274_) {
        return p_43274_ == EquipmentSlot.MAINHAND ? this.defaultModifiers : super.getDefaultAttributeModifiers(p_43274_);
    }

    public boolean hurtEnemy(ItemStack p_43278_, LivingEntity p_43279_, LivingEntity p_43280_) {
        p_43278_.hurtAndBreak(2, p_43280_, (p_43296_) -> {
            p_43296_.broadcastBreakEvent(EquipmentSlot.MAINHAND);
        });
        return true;
    }

    public boolean mineBlock(ItemStack p_43282_, Level p_43283_, BlockState p_43284_, BlockPos p_43285_, LivingEntity p_43286_) {
        if (p_43284_.getDestroySpeed(p_43283_, p_43285_) != 0.0F) {
            p_43282_.hurtAndBreak(4, p_43286_, (p_43276_) -> {
                p_43276_.broadcastBreakEvent(EquipmentSlot.MAINHAND);
            });
        }

        return true;
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (player.getFoodData()
                .getFoodLevel() == 0)
            return InteractionResultHolder.fail(itemstack);

        if (player.getMainHandItem().getItem() instanceof BoxersGloves && player.getOffhandItem().getItem() == player.getMainHandItem().getItem()) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(itemstack);
        } else {
            return InteractionResultHolder.fail(itemstack);
        }
    }

    @Override
    public void onUseTick(Level level, LivingEntity user, ItemStack stack, int p_41431_) {
        if (p_41431_ % 5 != 0)
            return;
        if (!(user instanceof Player player))
            return;

        user.getMainHandItem().hurtAndBreak(1, player, (living) -> {
            living.broadcastBreakEvent(EquipmentSlot.MAINHAND);
        });
        user.getOffhandItem().hurtAndBreak(1, player, (living) -> {
            living.broadcastBreakEvent(EquipmentSlot.OFFHAND);
        });

        if (!BacktankUtil.canAbsorbDamage(user, 400))
            player.causeFoodExhaustion(0.75F);
        for(LivingEntity livingentity : level.getEntitiesOfClass(LivingEntity.class, user.getBoundingBox().inflate(player.getEntityReach() - 1, player.getEntityReach() - 1, player.getEntityReach() - 1))) {
            double entityReachSq = Mth.square(player.getEntityReach()); // Use entity reach instead of constant 9.0. Vanilla uses bottom center-to-center checks here, so don't update this to use canReach, since it uses closest-corner checks.
            Vec3 eyePositionDisplacement = livingentity.getEyePosition().subtract(player.getEyePosition());
            if (livingentity != player && !player.isAlliedTo(livingentity)
                    && (!(livingentity instanceof ArmorStand)
                    || !((ArmorStand)livingentity).isMarker())
                    && player.distanceToSqr(livingentity) < entityReachSq
                    && eyePositionDisplacement.dot(player.getLookAngle()) / (eyePositionDisplacement.length() * player.getLookAngle().length()) >= 0.2) {
                Vec3 normalizedDisplacement = eyePositionDisplacement.normalize().scale(-1);
                livingentity.knockback(extraBouncy ? 2 : 0.1, normalizedDisplacement.x, normalizedDisplacement.z);
                livingentity.hurt(player.damageSources().playerAttack(player), 2.5F);
            }
        }
    }

    public int getUseDuration(ItemStack p_40680_) {
        return 72000;
    }

    public UseAnim getUseAnimation(ItemStack p_40678_) {
        return UseAnim.CUSTOM;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public boolean applyForgeHandTransform(PoseStack poseStack, LocalPlayer player, HumanoidArm arm, ItemStack itemInHand, float partialTick, float equipProcess, float swingProcess) {
                int i = arm == HumanoidArm.RIGHT ? 1 : -1;
                float progress = player.getTicksUsingItem() == 0 ? 0 : player.getTicksUsingItem() + (arm == HumanoidArm.RIGHT ? 0 : (float) 2.5);
                poseStack.translate((float)i * 0.56F, -0.52F, -0.72F + (2 * Math.abs(progress / 5 - Math.floor(progress / 5 + 0.5))) * -0.6F);
                return true;
            }

            private static final HumanoidModel.ArmPose EXAMPLE_POSE = HumanoidModel.ArmPose.create("EXAMPLE", true, (model, entity, arm) -> {
                int tick = entity.getTicksUsingItem();
                int revolutionTick = 5;
                double radian = Math.PI * 2 * tick / revolutionTick;
                model.leftArm.xRot = (float) (Math.PI/4 * Math.sin(radian) - Math.PI/4 + model.head.xRot - Math.PI/8);
                model.leftArm.zRot = (float) (Math.PI/4 * Math.cos(radian) + Math.PI/16);
                model.rightArm.xRot = (float) (Math.PI/4 * Math.sin(-radian) - Math.PI/4 + model.head.xRot - Math.PI/8);
                model.rightArm.zRot = (float) (Math.PI/4 * Math.cos(-radian) + Math.PI/16);
            });

            @Override
            public HumanoidModel.@Nullable ArmPose getArmPose(LivingEntity entityLiving, InteractionHand hand, ItemStack itemStack) {
                if (!itemStack.isEmpty()) {
                    if (entityLiving.getUsedItemHand() == hand && entityLiving.getUseItemRemainingTicks() > 0) {
                        return EXAMPLE_POSE;
                    }
                }
                return HumanoidModel.ArmPose.EMPTY;
            }
        });
    }
}
