package org.forsteri.createfantasticweapons.content.superfishingrod;

import com.simibubi.create.content.equipment.armor.BacktankUtil;
import com.simibubi.create.content.equipment.clipboard.ClipboardBlockItem;
import com.simibubi.create.content.equipment.clipboard.ClipboardOverrides;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import org.forsteri.createfantasticweapons.entry.Registrate;

import java.util.Collections;

public class SuperFishingRod extends FishingRodItem {
    public SuperFishingRod(Properties p_41285_) {
        super(p_41285_);
    }

    @SuppressWarnings("t")
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand p_41292_) {
        ItemStack itemstack = player.getItemInHand(p_41292_);
        if (player.fishing != null) {
            if (!level.isClientSide) {
                net.minecraftforge.event.entity.player.ItemFishedEvent event = null;
                if (player.fishing.getHookedIn() != null) {
                    Entity entityHooked = player.fishing.getHookedIn();
                    if (entityHooked != null) {
                        Vec3 vec3 = (new Vec3(entityHooked.getX() - player.getX(), entityHooked.getY() - player.getY(), entityHooked.getZ() - player.getZ())).scale(-0.08);
                        entityHooked.setDeltaMovement(entityHooked.getDeltaMovement().add(vec3));
                    }
                    level.broadcastEntityEvent(player.fishing, (byte) 31);
                }
                int i = player.fishing.retrieve(itemstack);

                if (i != 0 && !BacktankUtil.canAbsorbDamage(player, 900/i))
                    itemstack.hurtAndBreak(i, player, (p_41288_) -> {
                        p_41288_.broadcastBreakEvent(p_41292_);
                    });
            }

            level.playSound((Player)null, player.getX(), player.getY(), player.getZ(), SoundEvents.FISHING_BOBBER_RETRIEVE, SoundSource.NEUTRAL, 1.0F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
            player.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
        } else {
            level.playSound((Player)null, player.getX(), player.getY(), player.getZ(), SoundEvents.FISHING_BOBBER_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
            if (!level.isClientSide) {
                int k = EnchantmentHelper.getFishingSpeedBonus(itemstack);
                int j = EnchantmentHelper.getFishingLuckBonus(itemstack);
                FishingHook hook = new FishingHook(player, level, j, k);
                level.addFreshEntity(hook);
                hook.setNoGravity(true);
                hook.setDeltaMovement(hook.getDeltaMovement().scale(2));
            }

            player.awardStat(Stats.ITEM_USED.get(this));
            player.gameEvent(GameEvent.ITEM_INTERACT_START);
        }

        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }

    public void registerModelOverrides() {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> registerModelOverridesClient(this));
    }

    @OnlyIn(Dist.CLIENT)
    public static void registerModelOverridesClient(SuperFishingRod item) {
        ItemProperties.register(item, new ResourceLocation("cast"), (p_174585_, p_174586_, p_174587_, p_174588_) -> {
            if (p_174587_ == null) {
                return 0.0F;
            } else {
                boolean flag = p_174587_.getMainHandItem() == p_174585_;
                boolean flag1 = p_174587_.getOffhandItem() == p_174585_;
                if (p_174587_.getMainHandItem().getItem() instanceof FishingRodItem) {
                    flag1 = false;
                }

                return (flag || flag1) && p_174587_ instanceof Player && ((Player)p_174587_).fishing != null ? 1.0F : 0.0F;
            }
        });
    }

}
