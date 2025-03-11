package org.forsteri.createfantasticweapons.content.bigsyringe;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllFluids;
import com.simibubi.create.content.fluids.potion.PotionFluid;
import com.simibubi.create.content.fluids.potion.PotionFluidHandler;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import net.createmod.catnip.lang.LangBuilder;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Vanishable;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class BigSyringe extends Item implements Vanishable {
    private final Multimap<Attribute, AttributeModifier> defaultModifiers;

    public BigSyringe(Properties p_41383_) {
        super(p_41383_);
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        float attackDamage = 5;
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", attackDamage, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", -2.4, AttributeModifier.Operation.ADDITION));
        this.defaultModifiers = builder.build();
    }

    public boolean canAttackBlock(@NotNull BlockState p_43291_, @NotNull Level p_43292_, @NotNull BlockPos p_43293_, Player p_43294_) {
        return !p_43294_.isCreative();
    }

    public boolean hurtEnemy(ItemStack p_43278_, @NotNull LivingEntity p_43279_, @NotNull LivingEntity p_43280_) {
        p_43278_.hurtAndBreak(1, p_43280_, (p_43296_) -> p_43296_.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        return true;
    }

    public boolean mineBlock(@NotNull ItemStack p_43282_, @NotNull Level p_43283_, BlockState p_43284_, @NotNull BlockPos p_43285_, @NotNull LivingEntity p_43286_) {
        if (p_43284_.getDestroySpeed(p_43283_, p_43285_) != 0.0F) {
            p_43282_.hurtAndBreak(2, p_43286_, (p_43276_) -> p_43276_.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        }

        return true;
    }

    public @NotNull Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(@NotNull EquipmentSlot p_43274_) {
        return p_43274_ == EquipmentSlot.MAINHAND ? this.defaultModifiers : super.getDefaultAttributeModifiers(p_43274_);
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
        return ToolActions.SWORD_SWEEP == toolAction;
    }

    public static Optional<FluidStack> fluidOf(ItemStack stack) {
        FluidStack fluid;

        {
            if (!stack.getOrCreateTag().contains("potion")) {
                return Optional.empty();
            }

            CompoundTag potion = stack.getOrCreateTag().getCompound("potion");
            int amount = stack.getOrCreateTag().getInt("amount");

            if (amount <= 0) {
                stack.getOrCreateTag().remove("potion");
                return Optional.empty();
            }


            fluid = new FluidStack(AllFluids.POTION.get()
                    .getSource(), amount);

            fluid.setTag(potion);
        }

        return Optional.of(fluid);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack item, @Nullable Level p_40573_, @NotNull List<Component> tooltip, @NotNull TooltipFlag p_40575_) {
        super.appendHoverText(item, p_40573_, tooltip, p_40575_);

        Optional<FluidStack> fluid = fluidOf(item);
        if (fluid.isEmpty()) return;

        LangBuilder mb = CreateLang.translate("generic.unit.millibuckets");
        tooltip.add(
                CreateLang.fluidName(fluid.get())
                        .add(CreateLang.text(" "))
                        .style(ChatFormatting.GRAY)
                        .add(CreateLang.number(fluid.get().getAmount())
                                .add(mb)
                                .style(ChatFormatting.BLUE))
                        .component()
        );
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        ItemStack itemStack = context.getItemInHand();
        Optional<FluidStack> fluidOf = fluidOf(itemStack);
        if (!level.getBlockState(pos).is(AllBlocks.BASIN.get()))
            return super.useOn(context);

        if (!(level.getBlockEntity(pos) instanceof BasinBlockEntity basin))
            return super.useOn(context);

        for (SmartFluidTankBehaviour behaviour : basin.getTanks()) {
            Optional<? extends IFluidHandler> cap = behaviour.getCapability().resolve();
            if (cap.isEmpty())
                continue;

            IFluidHandler handler = cap.get();
            FluidStack fluidStack = handler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE);
            if (!fluidStack.getFluid().isSame(AllFluids.POTION.get()))
                continue;

            if (fluidOf.isPresent() && !fluidOf.get().isFluidEqual(fluidStack))
                continue;

            int drainAmount = fluidOf.map(stack -> 4000 - stack.getAmount()).orElse(4000);
            CompoundTag itemTag = itemStack.getOrCreateTag();
            FluidStack draining = fluidStack.copy();
            draining.setAmount(drainAmount);

            FluidStack drained = handler.drain(draining, IFluidHandler.FluidAction.EXECUTE);

            if (drained.isEmpty())
                continue;

            itemTag.put("potion", drained.getOrCreateTag());
            itemTag.putInt("amount", itemTag.getInt("amount") + drained.getAmount());
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
    
    public static void criticalHit(CriticalHitEvent event) {
        ItemStack mainhandItem = event.getEntity().getMainHandItem();
        CompoundTag tag = mainhandItem.getOrCreateTag();
        if (fluidOf(mainhandItem).isEmpty())
            return;
        FluidStack current = fluidOf(mainhandItem).get();
        current.setAmount(tag.getInt("amount"));

        if (!(current.getFluid() instanceof PotionFluid))
            return;

        if (!(event.getTarget() instanceof LivingEntity livingTarget))
            return;

        if (!livingTarget.isAffectedByPotions())
            return;

        PotionFluid.BottleType bottleType = NBTHelper.readEnum(tag, "Bottle", PotionFluid.BottleType.class);

        int drainedAmount = bottleType == PotionFluid.BottleType.REGULAR ? 250 : 150;

        if (current.getAmount() < drainedAmount)
            return;

        ItemStack bottlified = PotionFluidHandler.fillBottle(null, current);

        if (!event.getEntity().level().isClientSide()) {
            for(MobEffectInstance mobeffectinstance : PotionUtils.getMobEffects(bottlified)) {
                if (mobeffectinstance.getEffect().isInstantenous()) {
                    mobeffectinstance.getEffect().applyInstantenousEffect(event.getEntity(), event.getEntity(), livingTarget, mobeffectinstance.getAmplifier(), 1.0D);
                } else {
                    livingTarget.addEffect(new MobEffectInstance(mobeffectinstance));
                }
            }
        }

        current.setAmount(current.getAmount() - drainedAmount);

        tag.put("potion", current.getOrCreateTag());
        tag.putInt("amount", current.getAmount());
    }

    public static void sweep(Player player) {
        ItemStack mainhandItem = player.getMainHandItem();
        CompoundTag tag = mainhandItem.getOrCreateTag();
        if (fluidOf(mainhandItem).isEmpty())
            return;
        FluidStack current = fluidOf(mainhandItem).get();
        current.setAmount(tag.getInt("amount"));

        if (!(current.getFluid() instanceof PotionFluid))
            return;

        int drainedAmount = 10;

        if (current.getAmount() < drainedAmount)
            return;

        current.setAmount(current.getAmount() - drainedAmount);

        tag.put("potion", current.getOrCreateTag());
        tag.putInt("amount", current.getAmount());
    }

    public static void sweepATarget(Player player, LivingEntity target) {
        ItemStack mainhandItem = player.getMainHandItem();
        FluidStack current = fluidOf(mainhandItem).get();
        ItemStack bottlified = PotionFluidHandler.fillBottle(null, current);

        if (!player.level().isClientSide()) {
            for(MobEffectInstance mobeffectinstance : PotionUtils.getMobEffects(bottlified)) {
                if (mobeffectinstance.getEffect().isInstantenous()) {
                    mobeffectinstance.getEffect().applyInstantenousEffect(player, player, target, mobeffectinstance.getAmplifier(), 1.0D);
                } else {
                    target.addEffect(new MobEffectInstance(mobeffectinstance));
                }
            }
        }
    }
}
