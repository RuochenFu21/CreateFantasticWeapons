package org.forsteri.createfantasticweapons.content.bat;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class BaseballBat extends Item {
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

        if (!PROJECTILE_ITEMS.contains(projectile.getItem()))
            return InteractionResultHolder.pass(stack);

        p_40673_.startUsingItem(p_40674_);
        return InteractionResultHolder.consume(stack);
    }

    public static float getPowerForTime(int p_40662_) {
        float f = (float)p_40662_ / 20.0F;
        f = (f * f + f * 2.0F) / 3.0F;
        if (f > 1.0F) {
            f = 1.0F;
        }

        return f;
    }

    public int getUseDuration(@NotNull ItemStack p_40680_) {
        return 72000;
    }

    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack p_40678_) {
        return UseAnim.BOW;
    }

    public void releaseUsing(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity entity, int tickUsed) {
        if (!(entity instanceof Player player)) return;

        ItemStack itemstack = player.getItemInHand(InteractionHand.OFF_HAND);

        if (!PROJECTILE_ITEMS.contains(itemstack.getItem())) return;

        int i = this.getUseDuration(stack) - tickUsed;
        if (i < 0) return;

        if (itemstack.isEmpty()) return;

        float f = getPowerForTime(i);
        if ((double)f < 0.1D) return;

        boolean flag1 = (itemstack.getItem() instanceof ArrowItem && ((ArrowItem)itemstack.getItem()).isInfinite(itemstack, stack, player));

        if (!level.isClientSide) {
            Projectile projectile = getProjectile(level, player, itemstack);
            projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, f * 3.0F, 1.0F);
            level.addFreshEntity(projectile);
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F) + f * 0.5F);
        if (!flag1 && !player.getAbilities().instabuild) {
            itemstack.shrink(1);
            if (itemstack.isEmpty()) {
                player.getInventory().removeItem(itemstack);
            }
        }

        player.awardStat(Stats.ITEM_USED.get(this));
    }

    protected Projectile getProjectile(Level level, LivingEntity entity, ItemStack stack) {
        if (stack.getItem() instanceof SnowballItem)
            return new Snowball(level, entity);

        throw new IllegalArgumentException("Unknown projectile for " + stack.getItem());
    }

    public static final List<Item> PROJECTILE_ITEMS = Arrays.asList(Items.SNOWBALL);

}
