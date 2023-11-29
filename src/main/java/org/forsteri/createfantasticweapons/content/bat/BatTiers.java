package org.forsteri.createfantasticweapons.content.bat;

public enum BatTiers {
    COPPER(5),
    ANDESITE(6),
    BRASS(7),
    METAL(8);

    private final int damage;

    BatTiers(int damage) {
        this.damage = damage;
    }

    public int getDamage() {
        return damage;
    }
}
