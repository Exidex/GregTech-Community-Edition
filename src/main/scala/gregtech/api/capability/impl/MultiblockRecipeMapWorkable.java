package gregtech.api.capability.impl;

import gregtech.api.capability.IEnergyContainer;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.recipes.Recipe;

public class MultiblockRecipeMapWorkable extends RecipeMapWorkableHandler {


    public MultiblockRecipeMapWorkable(RecipeMapMultiblockController tileEntity) {
        super(tileEntity, tileEntity.recipeMap);
    }

    @Override
    public void update() {
    }

    public void updateWorkable() {
        super.update();
    }

    public IEnergyContainer getEnergyContainer() {
        RecipeMapMultiblockController controller = (RecipeMapMultiblockController) metaTileEntity;
        return controller.getEnergyContainer();
    }

    @Override
    protected boolean setupAndConsumeRecipeInputs(Recipe recipe) {
        RecipeMapMultiblockController controller = (RecipeMapMultiblockController) metaTileEntity;
        if(controller.checkRecipe(recipe, false) &&
            super.setupAndConsumeRecipeInputs(recipe)) {
            controller.checkRecipe(recipe, true);
            return true;
        } else return false;
    }

    @Override
    protected long getEnergyStored() {
        return getEnergyContainer().getEnergyStored();
    }

    @Override
    protected long getEnergyCapacity() {
        return getEnergyContainer().getEnergyCapacity();
    }

    @Override
    protected boolean drawEnergy(int recipeEUt) {
        long resultEnergy = getEnergyStored() - recipeEUt;
        if(resultEnergy >= 0L && resultEnergy <= getEnergyCapacity()) {
            getEnergyContainer().addEnergy(-recipeEUt);
            return true;
        } else return false;
    }

    @Override
    protected long getMaxVoltage() {
        return Math.max(getEnergyContainer().getInputVoltage(), getEnergyContainer().getOutputVoltage());
    }
}
