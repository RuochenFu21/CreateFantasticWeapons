package org.forsteri.createfantasticweapons.content.propellerleggings;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.simibubi.create.content.equipment.armor.BacktankItem;
import com.simibubi.create.content.equipment.armor.BacktankUtil;
import com.simibubi.create.content.equipment.armor.BaseArmorItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.entity.living.LivingEvent;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class PropellerLeggings extends BaseArmorItem {
    public PropellerLeggings(ArmorMaterial armorMaterial, Properties properties, ResourceLocation textureLoc) {
        super(armorMaterial, Type.LEGGINGS, properties, textureLoc);
    }

    public static final EquipmentSlot SLOT = EquipmentSlot.LEGS;

    @Nullable
    public static PropellerLeggings getWornBy(Entity entity) {
        if (!(entity instanceof LivingEntity livingEntity)) {
            return null;
        }
        if (!(livingEntity.getItemBySlot(SLOT).getItem() instanceof PropellerLeggings item)) {
            return null;
        }
        return item;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int p_41407_, boolean p_41408_) {
        if (!(entity instanceof LivingEntity livingEntity))
            return;
        boolean haveAirRemaining = BacktankUtil.hasAirRemaining(livingEntity.getItemBySlot(EquipmentSlot.CHEST));
        stack.getOrCreateTag().putBoolean("Propel", haveAirRemaining);
        if (entity instanceof Player player)
            haveAirRemaining |= player.isCreative();
        if (haveAirRemaining & livingEntity.isShiftKeyDown()) {
            livingEntity.addEffect(new MobEffectInstance(MobEffects.JUMP, 3, 3, false, false, false));
        }

        super.inventoryTick(stack, level, livingEntity, p_41407_, p_41408_);
    }

    public static void onJumpEvent(LivingEvent.LivingJumpEvent event) {
        if (event.getEntity().isShiftKeyDown())
            BacktankUtil.canAbsorbDamage(event.getEntity(), 200);
    }

    @Override
    public boolean canBeDepleted() {
        return false;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }
}
