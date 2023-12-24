package org.forsteri.createfantasticweapons.content.bat;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class BaseballBat extends Item implements IClientItemExtensions {
    protected BatTiers batTier;

    protected Multimap<Attribute, AttributeModifier> defaultModifiers;

    public BaseballBat(BatTiers batTier, Properties p_41383_) {
        super(p_41383_);
        this.batTier = batTier;
        makeModifiers();
    }

    protected void makeModifiers() {
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE,
                new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", this.batTier.getDamage() - 1, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED,
                new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", -2.75D, AttributeModifier.Operation.ADDITION));
        defaultModifiers = builder.build();
    }

    @SuppressWarnings("deprecation")
    public @NotNull Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(@NotNull EquipmentSlot p_43274_) {
        return p_43274_ == EquipmentSlot.MAINHAND ? this.defaultModifiers : super.getDefaultAttributeModifiers(p_43274_);
    }

    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level p_40672_, Player p_40673_, @NotNull InteractionHand p_40674_) {
        ItemStack projectile = p_40673_.getItemInHand(InteractionHand.OFF_HAND);
        ItemStack stack = p_40673_.getItemInHand(p_40674_);

        if (!PROJECTILE_ITEMS.containsKey(projectile.getItem()))
            return InteractionResultHolder.pass(stack);

        p_40673_.startUsingItem(p_40674_);
        return InteractionResultHolder.consume(stack);
    }

    public int getUseDuration(@NotNull ItemStack p_40680_) {
        return TIC_TAKE_TO_THROW;
    }

    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack p_40678_) {
        return UseAnim.NONE;
    }

    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity entity) {
        if (!(entity instanceof Player player)) return stack;

        ItemStack itemstack = player.getItemInHand(InteractionHand.OFF_HAND);

        if (!PROJECTILE_ITEMS.containsKey(itemstack.getItem())) return stack;

        if (itemstack.isEmpty()) return stack;

        boolean flag1 = (itemstack.getItem() instanceof ArrowItem && ((ArrowItem)itemstack.getItem()).isInfinite(itemstack, stack, player));

        if (!level.isClientSide) {
            Projectile projectile = PROJECTILE_ITEMS.get(itemstack.getItem()).get(level, player, itemstack);
            projectile.shootFromRotation(player, -80, player.getYRot(), 0.0F, 0.3F, 0F);
            projectile.setOwner(player);
            level.addFreshEntity(projectile);
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F) + 1 * 0.5F);
        if (!flag1 && !player.getAbilities().instabuild) {
            itemstack.shrink(1);
            if (itemstack.isEmpty()) {
                player.getInventory().removeItem(itemstack);
            }
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        player.getCooldowns().addCooldown(this, 20);
        if (!player.getOffhandItem().is(Items.AIR))
            player.getCooldowns().addCooldown(player.getOffhandItem().getItem(), 20);

        return stack;
    }

    public static final int TIC_TAKE_TO_THROW = 5;

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entityLiving, InteractionHand hand, ItemStack itemStack) {
                if (itemStack.isEmpty())
                    return HumanoidModel.ArmPose.EMPTY;

                if (entityLiving.getUsedItemHand() != hand)
                    return HumanoidModel.ArmPose.EMPTY;

                return HumanoidModel.ArmPose.create("EXAMPLE", false, (model, entity, arm) -> {
                    if (entity.getUseItemRemainingTicks() <= 0)
                        return;
//                            if (arm == HumanoidArm.RIGHT)
//                                return;
                    model.leftArm.xRot = - (float) Math.PI * (entity.getTicksUsingItem() / (float) TIC_TAKE_TO_THROW) / 2;
                });
            }
        });

        super.initializeClient(consumer);
    }

    @FunctionalInterface
    public interface ProjectileSupplier {
        Projectile get(Level level, LivingEntity entity, ItemStack stack);
    }

    public static final Map<Item, ProjectileSupplier> PROJECTILE_ITEMS = new HashMap<>();
    public static final List<Class<? extends Projectile>> HITTABLE_PROJECTILES = new ArrayList<>();

    static {
        PROJECTILE_ITEMS.put(Items.SNOWBALL, (level, entity, stack) -> new Snowball(level, entity));
        HITTABLE_PROJECTILES.add(Snowball.class);
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        Level level = entity.level();
        List<Entity> entities = level.getEntities((Entity) null, entity.getBoundingBox().inflate(3, 3, 3), entity1 -> entity1 instanceof Projectile && HITTABLE_PROJECTILES.contains(entity1.getClass()));
        entities.forEach(entity1 -> {
            if (entity1 instanceof Projectile projectile) {
//                projectile.setPos(entity.getX(), entity.getY(), entity.getZ());

                Vec3 vec3 = entity.getLookAngle();
                projectile.setDeltaMovement(vec3);
                if (projectile instanceof AbstractHurtingProjectile hurtingProjectile) {
                    hurtingProjectile.xPower = vec3.x * 0.1D;
                    hurtingProjectile.yPower = vec3.y * 0.1D;
                    hurtingProjectile.zPower = vec3.z * 0.1D;
                }
                projectile.setOwner(entity);
            }
        });

        return super.onEntitySwing(stack, entity);
    }
}
