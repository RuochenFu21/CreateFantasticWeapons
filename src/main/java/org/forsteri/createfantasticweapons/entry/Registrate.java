package org.forsteri.createfantasticweapons.entry;

import com.simibubi.create.AllCreativeModeTabs;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import org.forsteri.createfantasticweapons.CreateFantasticWeapons;
import org.forsteri.createfantasticweapons.content.bat.BaseballBat;
import org.forsteri.createfantasticweapons.content.bat.BatTiers;

public class Registrate {
    private static final DeferredRegister<CreativeModeTab> REGISTER
            = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CreateFantasticWeapons.MOD_ID);

    private static ItemEntry<BaseballBat> METAL_BAT;

    static {
        for (BatTiers tier : BatTiers.values()) {
            ItemEntry<BaseballBat> bat = bat(tier);
            if (tier == BatTiers.METAL)
                METAL_BAT = bat;
        }

        REGISTER.register("fantastic_weapons",
                () -> CreativeModeTab.builder()
                        .title(Component.translatable("itemGroup.fantastic_weapons"))
                        .withTabsBefore(AllCreativeModeTabs.PALETTES_CREATIVE_TAB.getId())
                        .icon(() -> new ItemStack(Registrate.METAL_BAT))
                        .displayItems(
                                (parameters, output) ->
                                        output.acceptAll(CreateFantasticWeapons.REGISTRATE.getAll(Registries.ITEM).stream().map(
                                                regObj -> new ItemStack(regObj.get())).toList()))
                        .build());

        CreateFantasticWeapons.REGISTRATE.addRawLang("itemGroup.fantastic_weapons", "Create: Fantastic Weapons");
    }


    public static void register(IEventBus modEventBus) {
        REGISTER.register(modEventBus);
    }

    public static ItemEntry<BaseballBat> bat(BatTiers tier) {
        return CreateFantasticWeapons.REGISTRATE
                .item(tier.name().toLowerCase() + "_bat", p -> new BaseballBat(tier, p))
                .properties(p -> p.stacksTo(1).defaultDurability(tier.durability))
                .model((ctx, p) -> p.withExistingParent(ctx.getName(), p.modLoc("item/bat"))
                        .texture("1", p.modLoc("item/" + tier.name().toLowerCase() + "_bat")))
                .register();
    }
}
