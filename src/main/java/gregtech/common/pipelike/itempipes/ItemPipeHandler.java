package gregtech.common.pipelike.itempipes;

import gregtech.api.pipelike.ITilePipeLike;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

public class ItemPipeHandler implements IItemHandler {

    final ITilePipeLike<TypeItemPipe, ItemPipeProperties> tile;
    final int capacity;
    EnumFacing facing = null;

    public ItemPipeHandler(ITilePipeLike<TypeItemPipe, ItemPipeProperties> tile) {
        this.tile = tile;
        this.capacity = tile.getTileProperty().getTransferCapacity();
    }

    @Override
    public int getSlots() {
        return capacity;
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        validateSlotIndex(slot);
        ItemPipeNet net = getPipeNet();
        return net == null ? ItemStack.EMPTY : net.getItemStackInSlot(tile.getTilePos(), slot);
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) return ItemStack.EMPTY;
        validateSlotIndex(slot);
        ItemPipeNet net = getPipeNet();
        return net == null ? stack : net.insertItem(this, stack, simulate, facing.getOpposite());
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount == 0) return ItemStack.EMPTY;
        validateSlotIndex(slot);
        ItemPipeNet net = getPipeNet();
        return net == null ? ItemStack.EMPTY : net.extractItem(tile.getTilePos(), slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64;
    }

    private ItemPipeNet getPipeNet() {
        return ItemPipeFactory.INSTANCE.getPipeNetAt(tile);
    }

    private void validateSlotIndex(int slot) {
        if (slot < 0 || slot >= capacity) throw new RuntimeException("Slot " + slot + " not in valid range - [0," + capacity + ")");
    }

    static class SidedHandler extends ItemPipeHandler {
        SidedHandler(ItemPipeHandler handler, EnumFacing facing) {
            super(handler.tile);
            this.facing = facing;
        }
    }
}
