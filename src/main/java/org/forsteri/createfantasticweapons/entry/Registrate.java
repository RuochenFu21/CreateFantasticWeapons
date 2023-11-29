package org.forsteri.createfantasticweapons.entry;

import com.tterrag.registrate.util.entry.ItemEntry;
import org.forsteri.createfantasticweapons.CreateFantasticWeapons;
import org.forsteri.createfantasticweapons.content.bat.BaseballBat;
import org.forsteri.createfantasticweapons.content.bat.BatTiers;

public class Registrate {
    static {
        for (BatTiers tier : BatTiers.values()) {
            bat(tier);
        }
    }


    public static void register() {}

    public static ItemEntry<BaseballBat> bat(BatTiers tier) {
        return CreateFantasticWeapons.REGISTRATE
                .item(tier.name().toLowerCase() + "_bat", p -> new BaseballBat(tier, p))
                .model((ctx, p) -> p.withExistingParent(ctx.getName(), p.modLoc("item/bat"))
                        .texture("1", p.modLoc("item/" + tier.name().toLowerCase() + "_bat")))
                .register();
    }
}
