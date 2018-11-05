package gregtech.api.worldgen.filler;

import com.google.gson.JsonObject;
import gregtech.api.worldgen.config.OreConfigUtils;
import net.minecraft.block.state.IBlockState;

import java.util.function.Function;

public class SimpleBlockFiller extends BlockFiller {

    private Function<IBlockState, IBlockState> blockStateFiller;

    public SimpleBlockFiller() {
    }

    public SimpleBlockFiller(Function<IBlockState, IBlockState> blockStateFiller) {
        this.blockStateFiller = blockStateFiller;
    }

    @Override
    public void loadFromConfig(JsonObject object) {
        this.blockStateFiller = OreConfigUtils.createBlockStateFiller(object.get("value"));
    }

    @Override
    public IBlockState getStateForGeneration(IBlockState currentState, int x, int y, int z) {
        return blockStateFiller.apply(currentState);
    }

}
