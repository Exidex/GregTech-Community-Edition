package gregtech.common.pipelike.fluidpipe;

import gregtech.api.pipenet.block.BlockPipe;
import gregtech.api.pipenet.block.ItemBlockPipe;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class ItemBlockFluidPipe extends ItemBlockPipe<FluidPipeType, FluidPipeProperties> {

    public ItemBlockFluidPipe(BlockPipe<FluidPipeType, FluidPipeProperties, ?> block) {
        super(block);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        FluidPipeType fluidPipeType = blockPipe.getPipeType(stack);
        FluidPipeProperties pipeProperties = blockPipe.getProperties(fluidPipeType);
        tooltip.add(I18n.translateToLocalFormatted("gregtech.fluid_pipe.throughput", pipeProperties.throughput));
        tooltip.add(I18n.translateToLocalFormatted("gregtech.fluid_pipe.max_temperature", pipeProperties.maxFluidTemperature));
        if(!pipeProperties.gasProof) tooltip.add(I18n.translateToLocal("gregtech.fluid_pipe.non_gas_proof"));
    }
}
