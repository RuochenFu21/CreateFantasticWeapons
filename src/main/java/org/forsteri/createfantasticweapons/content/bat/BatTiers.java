package org.forsteri.createfantasticweapons.content.bat;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

public enum BatTiers {
    COPPER(5, AllBlocks.COPPER_CASING::asItem, 131),
    ANDESITE(6, AllBlocks.ANDESITE_CASING::asItem, 250),
    BRASS(7, AllBlocks.BRASS_CASING::asItem, 1561),
    METAL(8, AllItems.STURDY_SHEET::asItem, 2031);

    private final int damage;
    public final Supplier<Item> repair;
    public final int durability;

    BatTiers(int damage, Supplier<Item> repair, int durability) {
        this.damage = damage;
        this.repair = repair;
        this.durability = durability;
    }

    public int getDamage() {
        return damage;
    }
}
