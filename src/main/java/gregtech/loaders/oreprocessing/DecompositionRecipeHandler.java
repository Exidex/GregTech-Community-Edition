package gregtech.loaders.oreprocessing;

import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.type.DustMaterial;
import gregtech.api.unification.material.type.FluidMaterial;
import gregtech.api.unification.material.type.Material;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.MaterialStack;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static gregtech.api.unification.material.type.Material.MatFlags.DECOMPOSITION_REQUIRES_HYDROGEN;
import static gregtech.api.unification.material.type.Material.MatFlags.DISABLE_DECOMPOSITION;

public class DecompositionRecipeHandler {

    public static void register() {
        OrePrefix.dust.addProcessingHandler(FluidMaterial.class, DecompositionRecipeHandler::processDecomposition);
    }

    public static void processDecomposition(OrePrefix decomposePrefix, FluidMaterial material) {
        if (material.materialComponents.isEmpty() ||
            (!material.hasFlag(Material.MatFlags.DECOMPOSITION_BY_ELECTROLYZING) &&
                !material.hasFlag(Material.MatFlags.DECOMPOSITION_BY_CENTRIFUGING)) ||
            //disable decomposition if explicitly disabled for this material or for one of it's components
            material.hasFlag(DISABLE_DECOMPOSITION)) return;
        
        ArrayList<ItemStack> outputs = new ArrayList<>();
        ArrayList<FluidStack> fluidOutputs = new ArrayList<>();
        int totalInputAmount = 0;

        //compute outputs
        for (MaterialStack component : material.materialComponents) {
            totalInputAmount += component.amount;
            if (component.material instanceof DustMaterial) {
                outputs.add(OreDictUnifier.get(OrePrefix.dust, component.material, (int) component.amount));
            } else if (component.material instanceof FluidMaterial) {
                FluidMaterial componentMaterial = (FluidMaterial) component.material;
                fluidOutputs.add(componentMaterial.getFluid((int) (1000 * component.amount)));
            }
        }

        //generate builder
        RecipeBuilder builder;
        if (material.hasFlag(Material.MatFlags.DECOMPOSITION_BY_ELECTROLYZING)) {
            builder = RecipeMaps.ELECTROLYZER_RECIPES.recipeBuilder()
                .duration((int) material.getProtons() * totalInputAmount * 8)
                .EUt(getElectrolyzingVoltage(material));
        } else {
            builder = RecipeMaps.CENTRIFUGE_RECIPES.recipeBuilder()
                .duration((int) material.getMass() * totalInputAmount * 2)
                .EUt(30);
        }
        builder.outputs(outputs);
        builder.fluidOutputs(fluidOutputs);

        //finish builder
        if (decomposePrefix == OrePrefix.dust) {
            builder.input(decomposePrefix, material, totalInputAmount);
        } else {
            builder.fluidInputs(material.getFluid(1000 * totalInputAmount));
        }
        if(material.hasFlag(DECOMPOSITION_REQUIRES_HYDROGEN)) {
            builder.fluidInputs(Materials.Hydrogen.getFluid(1000 * totalInputAmount));
        }

        //register recipe
        builder.buildAndRegister();
    }

    //todo think something better with this
    private static int getElectrolyzingVoltage(Material material) {
        List<Material> components = material.materialComponents.stream()
            .map(s -> s.material).collect(Collectors.toList());
        //titanium or tungsten-containing materials electrolyzing requires 1920
        if(components.contains(Materials.Tungsten) ||
            components.contains(Materials.Titanium))
            return 1920; //EV voltage (tungstate and scheelite electrolyzing)
        if(material == Materials.Salt ||
            material == Materials.RockSalt)
            return 30; //LV voltage for salt electrolyzing (early way of getting sodium)
        //otherwise, use logic that requires at least 120 EU/t for electrolyzing
        return Math.min(4, components.size()) * 30;
    }
    
}
