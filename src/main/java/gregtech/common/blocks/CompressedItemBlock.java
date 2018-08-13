package gregtech.common.blocks;

import gregtech.api.unification.material.type.Material;
import gregtech.api.unification.ore.OrePrefix;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CompressedItemBlock extends ItemBlock {

    public final BlockCompressed block;

    public CompressedItemBlock(BlockCompressed block) {
        super(block);
        this.block = block;
        setHasSubtypes(true);
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }

    @SuppressWarnings("deprecation")
    public IBlockState getBlockState(ItemStack stack) {
        return block.getStateFromMeta(getMetadata(stack.getItemDamage()));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public String getItemStackDisplayName(ItemStack stack) {
        Material material = getBlockState(stack).getValue(block.variantProperty);
        return OrePrefix.block.getLocalNameForItem(material);
    }

}
