package gregtech.common.pipelike.itempipes;

import gregtech.api.GregTechAPI;
import gregtech.api.pipelike.BlockPipeLike;
import gregtech.api.pipelike.ITilePipeLike;
import gregtech.api.pipelike.PipeFactory;
import gregtech.api.unification.material.type.GemMaterial;
import gregtech.api.unification.material.type.IngotMaterial;
import gregtech.api.unification.material.type.Material;
import gregtech.api.worldentries.pipenet.WorldPipeNet;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import static gregtech.api.unification.material.Materials.*;

public class ItemPipeFactory extends PipeFactory<TypeItemPipe, ItemPipeProperties, IItemHandler> {

    public static final ItemPipeFactory INSTANCE = new ItemPipeFactory();

    private ItemPipeFactory() {
        super("item_pipe", CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, TypeItemPipe.class, ItemPipeProperties.class);
        registerDefaultItemPipes();
    }

    private void registerDefaultItemPipes() {
        registerItemPipe(Brass, 1);
        registerItemPipe(WroughtIron, 1);
        registerItemPipe(Nickel, 1);
        registerItemPipe(Electrum, 2);
        registerItemPipe(Cobalt, 2);
        registerItemPipe(Aluminium, 2);
        registerItemPipe(Platinum, 4);
        registerItemPipe(Osmium, 8);
    }

    public void registerItemPipe(Material material, int transferCapacity) {
        registerItemPipe(material, transferCapacity, 1, 1);
    }

    public void registerItemPipe(Material material, int transferCapacity, int tickRateMultiplier, int routingValueMultiplier) {
        registerPropertyForMaterial(material, new ItemPipeProperties(transferCapacity, tickRateMultiplier, routingValueMultiplier));
    }

    @Override
    protected void initBlock(BlockPipeLike<TypeItemPipe, ItemPipeProperties, IItemHandler> block, Material material, ItemPipeProperties materialProperty) {
        block.setCreativeTab(GregTechAPI.TAB_GREGTECH);
        block.setSoundType(material instanceof IngotMaterial ? SoundType.METAL : material.toString().contains("wood") ? SoundType.WOOD : SoundType.STONE);
        block.setHarvestLevel("wrench", material instanceof IngotMaterial ? ((IngotMaterial) material).harvestLevel : material instanceof GemMaterial ? ((GemMaterial) material).harvestLevel : 1);
        block.setHardness(4.0f);
        block.setResistance(4.5f);
        block.setLightOpacity(1);
    }

    @Override
    protected ItemPipeProperties createActualProperty(TypeItemPipe baseProperty, ItemPipeProperties materialProperty) {
        int capacity = baseProperty.transferCapacity * materialProperty.getTransferCapacity();
        int tickRate = baseProperty.tickRate * materialProperty.getTickRate();
        long routingValue = (long) baseProperty.baseRoutingValue * materialProperty.getRoutingValue() * tickRate / capacity;
        return new ItemPipeProperties(capacity, tickRate, (int) routingValue);
    }

    @Override
    protected void onEntityCollided(Entity entity, ITilePipeLike<TypeItemPipe, ItemPipeProperties> tile) {}

    @Override
    public void onBreakingTile(ITilePipeLike<TypeItemPipe, ItemPipeProperties> tile) {
        if (tile == null) return;
        ItemPipeNet net = getPipeNetAt(tile);
        if (net != null) {
            NonNullList<ItemStack> inventoryContents = NonNullList.create();
            net.cleanBufferedItems(tile.getTilePos(), inventoryContents);
            for(ItemStack itemStack : inventoryContents) {
                Block.spawnAsEntity(tile.getTileWorld(), tile.getTilePos(), itemStack);
            }
        }
    }

    @Override
    public int getDefaultColor() {
        return 0x777777;
    }

    @Override
    public IItemHandler createCapability(ITilePipeLike<TypeItemPipe, ItemPipeProperties> tile) {
        return new ItemPipeHandler(tile);
    }

    @Override
    public IItemHandler onGettingNetworkCapability(IItemHandler capability, EnumFacing facing) {
        if (capability instanceof ItemPipeHandler && facing != null) {
            return new ItemPipeHandler.SidedHandler((ItemPipeHandler) capability, facing);
        }
        return capability;
    }

    @Override
    public ItemPipeNet createPipeNet(WorldPipeNet worldNet) {
        return new ItemPipeNet(worldNet);
    }

    @Override
    public ItemPipeNet getPipeNetAt(ITilePipeLike<TypeItemPipe, ItemPipeProperties> tile) {
        return (ItemPipeNet) super.getPipeNetAt(tile);
    }

    @Override
    public ItemPipeProperties createEmptyProperty() {
        return new ItemPipeProperties();
    }
}
