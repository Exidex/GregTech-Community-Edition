package gregtech.common.tools;

import gregtech.api.items.toolitem.ToolMetaItem;
import gregtech.api.unification.material.type.SolidMaterial;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;

public class ToolTurbineRotor extends ToolBase implements ITurbineToolStats {

    @Override
    public float getMaxDurabilityMultiplier(ItemStack stack) {
        return 6.0f;
    }

    @Override
    public double getRotorEfficiency(ItemStack itemStack) {
        SolidMaterial primaryMaterial = ToolMetaItem.getPrimaryMaterial(itemStack);
        return primaryMaterial.toolSpeed / 24.0f;
    }
}
