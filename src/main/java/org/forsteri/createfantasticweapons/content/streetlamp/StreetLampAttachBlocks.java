package org.forsteri.createfantasticweapons.content.streetlamp;

import com.simibubi.create.AllBlocks;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.function.Supplier;

public enum StreetLampAttachBlocks implements StringRepresentable {
    NONE(Items.AIR::asItem, 0, 0, 0),
    LANTERN(Items.LANTERN::asItem, 1, 0, 0),
    CAMPFIRE(Items.CAMPFIRE::asItem, 0, 1, 0),
    JACK_O_LANTERN(Items.JACK_O_LANTERN::asItem, 0, 0, 0),
    REDSTONE_LAMP(Items.REDSTONE_LAMP::asItem, 1, 0, 0.2f),
    SOUL_LANTERN(Items.SOUL_LANTERN::asItem, 1, 0, 0, () -> new MobEffectInstance(MobEffects.BLINDNESS, 2)),
    // 3s cooldown
    SHROOMLIGHT(Items.SHROOMLIGHT::asItem, 1, 0, 0),
    GLOWSTONE(Items.GLOWSTONE::asItem, 1, 0, 0, () -> new MobEffectInstance(MobEffects.GLOWING, 10)),
    NIXIE_TUBE(AllBlocks.ORANGE_NIXIE_TUBE::asItem, 2, 0, 0.1f),
    ROSE_QUARTZ_LAMP(AllBlocks.ROSE_QUARTZ_LAMP::asItem, 2, 0, 0),
    BLAZE_BURNER(AllBlocks.BLAZE_BURNER::asItem, 2, 2, 0),
    SEA_LANTERN(Items.SEA_LANTERN::asItem, 2, 0, 0),
    END_ROD(Items.END_ROD::asItem, 3, 0, 0),
    BEACON(Items.BEACON::asItem, 4, 0, 0);

    public final Supplier<Item> item;
    public final int addedDamage;
    public final int ignitionTime;
    public final float addedAttackSpeed;
    public final Supplier<MobEffectInstance>[] effects;

    StreetLampAttachBlocks(Supplier<Item> item, int addedDamage, int ignitionTime, float addedAttackSpeed, Supplier<MobEffectInstance>... effects) {
        this.item = item;
        this.addedDamage = addedDamage;
        this.ignitionTime = ignitionTime;
        this.addedAttackSpeed = addedAttackSpeed;
        this.effects = effects;
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase();
    }
}
