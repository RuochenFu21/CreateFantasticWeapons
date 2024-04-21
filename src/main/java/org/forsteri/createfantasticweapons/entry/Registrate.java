package org.forsteri.createfantasticweapons.entry;

import com.simibubi.create.AllCreativeModeTabs;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import org.forsteri.createfantasticweapons.CreateFantasticWeapons;
import org.forsteri.createfantasticweapons.content.bat.BaseballBat;
import org.forsteri.createfantasticweapons.content.bat.BatTiers;
import org.forsteri.createfantasticweapons.content.streetlamp.StreetLampBlock;
import org.forsteri.createfantasticweapons.content.streetlamp.StreetLampBlockEntity;
import org.forsteri.createfantasticweapons.content.streetlamp.StreetLampItem;
import org.forsteri.createfantasticweapons.content.streetlamp.StreetLampRenderer;

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

    public static BlockEntry<StreetLampBlock> LAMP = CreateFantasticWeapons.REGISTRATE
            .block("street_lamp", StreetLampBlock::new)
//            .initialProperties(() -> AllBlocks.ANDESITE_ALLOY_BLOCK)
            .properties(p -> p.noOcclusion().noCollission())
            .item(StreetLampItem::new)
            .model(NonNullBiConsumer.noop())
            .properties(p -> p.stacksTo(1))
            .build()
            .blockstate((ctx, p) -> p.getVariantBuilder(ctx.getEntry())
                    .forAllStates(state -> {
                        if (!state.getValue(StreetLampBlock.IS_GHOST))
                            return ConfiguredModel.builder()
                                    .modelFile(p.models().getExistingFile(p.modLoc("item/street_lamp")))
                                    .build();
                        return ConfiguredModel.builder()
                                .modelFile(p.models().getExistingFile(p.modLoc("block/street_lamp")))
                                .build();
                    }))
            .defaultLoot()
            .loot((b, p) -> b.add(Registrate.LAMP.get(), LootTable.lootTable().withPool(b.applyExplosionCondition(p, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(p)))
                    .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(p).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(StreetLampBlock.IS_GHOST, false))))))
            .register();

    public static BlockEntityEntry<StreetLampBlockEntity> LAMP_BE = CreateFantasticWeapons.REGISTRATE
            .blockEntity("street_lamp", StreetLampBlockEntity::new)
            .validBlock(LAMP)
            .renderer(() -> StreetLampRenderer::new)
            .register();


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
