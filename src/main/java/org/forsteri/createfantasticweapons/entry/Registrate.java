package org.forsteri.createfantasticweapons.entry;

import com.simibubi.create.AllCreativeModeTabs;
import com.simibubi.create.AllTags;
import com.simibubi.create.Create;
import com.simibubi.create.content.equipment.armor.BacktankItem;
import com.simibubi.create.content.equipment.clipboard.ClipboardBlockItem;
import com.simibubi.create.foundation.data.AssetLookup;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorMaterials;
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
import org.forsteri.createfantasticweapons.content.bigsyringe.BigSyringe;
import org.forsteri.createfantasticweapons.content.boxersgloves.BoxersGloves;
import org.forsteri.createfantasticweapons.content.propellerleggings.PropellerLeggings;
import org.forsteri.createfantasticweapons.content.streetlamp.StreetLampBlock;
import org.forsteri.createfantasticweapons.content.streetlamp.StreetLampBlockEntity;
import org.forsteri.createfantasticweapons.content.streetlamp.StreetLampItem;
import org.forsteri.createfantasticweapons.content.streetlamp.StreetLampRenderer;
import org.forsteri.createfantasticweapons.content.superfishingrod.SuperFishingRod;

import static com.simibubi.create.AllTags.forgeItemTag;
import static com.simibubi.create.Create.REGISTRATE;

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

    public static  ItemEntry<BigSyringe> BIG_SYRINGE = CreateFantasticWeapons.REGISTRATE
            .item("big_syringe", BigSyringe::new)
            .properties(p -> p.stacksTo(1).defaultDurability(250))
            .model((ctx, p) -> p.getExistingFile(p.modLoc("item/big_syringe")))
            .register();

    public static  ItemEntry<BoxersGloves> BOXERS_GLOVE = CreateFantasticWeapons.REGISTRATE
            .item("boxers_glove", (prop) -> new BoxersGloves(prop, false))
            .properties(p -> p.stacksTo(1).defaultDurability(600))
            .model((ctx, p) -> p.getExistingFile(p.modLoc("item/boxers_glove")))
            .register();

    public static  ItemEntry<BoxersGloves> SLIME_BOXERS_GLOVE = CreateFantasticWeapons.REGISTRATE
            .item("slime_boxers_glove", (prop) -> new BoxersGloves(prop, true))
            .properties(p -> p.stacksTo(1).defaultDurability(600))
            .model((ctx, p) -> p.getExistingFile(p.modLoc("item/slime_boxers_glove")))
            .register();

    public static  ItemEntry<SuperFishingRod> SUPER_FISHING_ROD = CreateFantasticWeapons.REGISTRATE
            .item("super_fishing_rod", SuperFishingRod::new)
            .properties(p -> p.stacksTo(1).defaultDurability(300))
            .model((ctx, p) -> p.getExistingFile(p.modLoc("item/super_fishing_rod")))
            .onRegister(SuperFishingRod::registerModelOverrides)
            .register();

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

    public static ItemEntry<PropellerLeggings> PROPELLER_LEGGINGS = CreateFantasticWeapons.REGISTRATE
            .item("propeller_leggings",
                    p -> new PropellerLeggings(ArmorMaterials.IRON, p, new ResourceLocation(CreateFantasticWeapons.MOD_ID, "propeller_leggings")))
            .tag(forgeItemTag("armors/leggings"))
            .register();

    public static ItemEntry<PropellerLeggings> NETHERITE_PROPELLER_LEGGINGS = CreateFantasticWeapons.REGISTRATE
            .item("netherite_propeller_leggings",
                    p -> new PropellerLeggings(ArmorMaterials.IRON, p, new ResourceLocation(CreateFantasticWeapons.MOD_ID, "netherite_propeller_leggings")))
            .tag(forgeItemTag("armors/leggings"))
            .register();


    public static void register(IEventBus modEventBus) {
        REGISTER.register(modEventBus);
    }

    public static ItemEntry<BaseballBat> bat(BatTiers tier) {
        return CreateFantasticWeapons.REGISTRATE
                .item(tier.name().toLowerCase() + "_bat", p -> new BaseballBat(tier, p))
                .properties(p -> p.stacksTo(1).defaultDurability(tier.durability))
                .model((ctx, p) -> p.withExistingParent(ctx.getName(), p.modLoc("item/bat"))
                        .texture("1", p.modLoc("item/" + tier.name().toLowerCase() + "_bat"))
                        .texture("particle", p.modLoc("item/" + tier.name().toLowerCase() + "_bat")))
                .register();
    }
}
