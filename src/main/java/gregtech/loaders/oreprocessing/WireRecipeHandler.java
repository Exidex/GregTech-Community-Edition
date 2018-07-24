package gregtech.loaders.oreprocessing;

import gregtech.api.items.OreDictNames;
import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.type.DustMaterial.MatFlags;
import gregtech.api.unification.material.type.IngotMaterial;
import gregtech.api.unification.material.type.Material;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.api.util.GTUtility;
import gregtech.common.items.MetaItems;
import gregtech.common.pipelike.cables.CableFactory;
import gregtech.common.pipelike.cables.WireProperties;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.ArrayUtils;

import static gregtech.api.GTValues.M;

public class WireRecipeHandler {

    public static void register() {
        OrePrefix.wireGtSingle.addProcessingHandler(IngotMaterial.class, WireRecipeHandler::processWireSingle);
        for(OrePrefix wirePrefix : WIRE_DOUBLING_ORDER) {
            wirePrefix.addProcessingHandler(IngotMaterial.class, WireRecipeHandler::generateWireRecipe);
        }
    }

    private static final OrePrefix[] WIRE_DOUBLING_ORDER = new OrePrefix[] {
        OrePrefix.wireGtSingle, OrePrefix.wireGtDouble, OrePrefix.wireGtQuadruple, OrePrefix.wireGtOctal, OrePrefix.wireGtHex
    };

    private static final OrePrefix[] CABLE_DOUBLING_ORDER = new OrePrefix[] {
        OrePrefix.cableGtSingle, OrePrefix.cableGtDouble, OrePrefix.cableGtQuadruple, OrePrefix.cableGtOctal, OrePrefix.cableGtHex
    };

    public static void processWireSingle(OrePrefix wirePrefix, IngotMaterial material) {
        RecipeMaps.EXTRUDER_RECIPES.recipeBuilder()
            .input(OrePrefix.ingot, material)
            .notConsumable(MetaItems.SHAPE_EXTRUDER_WIRE)
            .outputs(OreDictUnifier.get(OrePrefix.wireGtSingle, material, 2))
            .duration((int) material.getMass() * 2)
            .EUt(6 * getVoltageMultiplier(material))
            .buildAndRegister();

        RecipeMaps.WIREMILL_RECIPES.recipeBuilder()
            .input(OrePrefix.ingot, material)
            .outputs(OreDictUnifier.get(OrePrefix.wireGtSingle, material, 2))
            .duration((int) material.getMass())
            .EUt(getVoltageMultiplier(material))
            .buildAndRegister();

        if (!material.hasFlag(MatFlags.NO_WORKING)) {
            ModHandler.addShapedRecipe(String.format("%s_wire_single", material),
                OreDictUnifier.get(OrePrefix.wireGtSingle, material), "Xx",
                'X', new UnificationEntry(OrePrefix.plate, material));
        }
    }

    public static void generateWireRecipe(OrePrefix wirePrefix, IngotMaterial material) {
        int cableAmount = (int) (wirePrefix.materialAmount * 2 / M);
        OrePrefix cablePrefix = OrePrefix.valueOf("cable" + wirePrefix.name().substring(4));
        ItemStack cableStack = OreDictUnifier.get(cablePrefix, material);

        if (isPaperInsulatedCable(material)) {
            if (cableAmount <= 7) {
                Object[] ingredients = new Object[2 + cableAmount];
                ingredients[0] = new UnificationEntry(wirePrefix, material);
                ingredients[ingredients.length - 1] = OreDictNames.string;
                for (int i = 1; i < ingredients.length - 1; i++) {
                    ingredients[i] = new ItemStack(Blocks.CARPET, 1, EnumDyeColor.BLACK.getMetadata());
                }
                ModHandler.addShapelessRecipe(String.format("%s_cable_%d", material, cableAmount), cableStack, ingredients);
            }
        }

        if(isPaperInsulatedCable(material)) {
            ItemStack carpetStack = new ItemStack(Blocks.CARPET, cableAmount, EnumDyeColor.BLACK.getMetadata());
            RecipeMaps.PACKER_RECIPES.recipeBuilder()
                .input(wirePrefix, material)
                .inputs(carpetStack)
                .outputs(cableStack)
                .duration(100).EUt(8)
                .buildAndRegister();

            RecipeMaps.UNPACKER_RECIPES.recipeBuilder()
                .input(cablePrefix, material)
                .outputs(OreDictUnifier.get(wirePrefix, material))
                .outputs(carpetStack)
                .duration(100).EUt(8)
                .buildAndRegister();

        } else {
            if(wirePrefix != OrePrefix.wireGtSingle) {
                RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                    .input(OrePrefix.wireGtSingle, material, cableAmount).circuitMeta(24 + ArrayUtils.indexOf(WIRE_DOUBLING_ORDER, wirePrefix))
                    .fluidInputs(Materials.Rubber.getFluid(144 * cableAmount))
                    .outputs(cableStack)
                    .duration(150).EUt(8)
                    .buildAndRegister();
            }

            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(wirePrefix, material).circuitMeta(24)
                .fluidInputs(Materials.Rubber.getFluid(144 * cableAmount))
                .outputs(cableStack)
                .duration(150).EUt(8)
                .buildAndRegister();

            RecipeMaps.UNPACKER_RECIPES.recipeBuilder()
                .input(cablePrefix, material)
                .outputs(OreDictUnifier.get(wirePrefix, material))
                .outputs(OreDictUnifier.get(OrePrefix.plate, Materials.Rubber))
                .duration(100).EUt(8)
                .buildAndRegister();
        }

        int wireIndex = ArrayUtils.indexOf(WIRE_DOUBLING_ORDER, wirePrefix);

        if(wireIndex < WIRE_DOUBLING_ORDER.length - 1) {
            ModHandler.addShapelessRecipe(String.format("%s_wire_%s_doubling", material, wirePrefix),
                OreDictUnifier.get(WIRE_DOUBLING_ORDER[wireIndex + 1], material),
                new UnificationEntry(wirePrefix, material),
                new UnificationEntry(wirePrefix, material));

            ModHandler.addShapelessRecipe(String.format("%s_cable_%s_doubling", material, wirePrefix),
                OreDictUnifier.get(CABLE_DOUBLING_ORDER[wireIndex + 1], material),
                new UnificationEntry(cablePrefix, material),
                new UnificationEntry(cablePrefix, material));
        }

        if(wireIndex > 0) {
            ModHandler.addShapelessRecipe(String.format("%s_wire_%s_splitting", material, wirePrefix),
                OreDictUnifier.get(WIRE_DOUBLING_ORDER[wireIndex - 1], material, 2),
                new UnificationEntry(wirePrefix, material));

            ModHandler.addShapelessRecipe(String.format("%s_cable_%s_splitting", material, wirePrefix),
                OreDictUnifier.get(CABLE_DOUBLING_ORDER[wireIndex - 1], material, 2),
                new UnificationEntry(cablePrefix, material));
        }
    }

    public static boolean isPaperInsulatedCable(IngotMaterial material) {
        WireProperties wireProperties = CableFactory.INSTANCE.getRegisteredProperty(material);
        return wireProperties != null && GTUtility.getTierByVoltage(wireProperties.getVoltage()) <= 1;
    }

    private static int getVoltageMultiplier(Material material) {
        return material instanceof IngotMaterial && ((IngotMaterial) material)
            .blastFurnaceTemperature >= 2800 ? 32 : 8;
    }

}
