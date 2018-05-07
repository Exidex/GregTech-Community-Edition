package gregtech.api.metatileentity;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.texture.TextureUtils;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.google.common.base.Preconditions;
import gregtech.api.GregTechAPI;
import gregtech.api.capability.impl.FluidHandlerProxy;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.ItemHandlerProxy;
import gregtech.api.gui.ModularUI;
import gregtech.api.render.Textures;
import gregtech.api.util.GTUtility;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.*;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public abstract class MetaTileEntity {

    public static final Cuboid6[] FULL_CUBE_COLLISION = new Cuboid6[] {Cuboid6.full};

    public final String metaTileEntityId;
    MetaTileEntityHolder holder;

    protected IItemHandlerModifiable importItems;
    protected IItemHandlerModifiable exportItems;

    protected IItemHandler itemInventory;

    protected FluidTankList importFluids;
    protected FluidTankList exportFluids;

    protected IFluidHandler fluidInventory;

    protected List<MTETrait> mteTraits = new ArrayList<>();

    protected EnumFacing frontFacing = EnumFacing.NORTH;
    protected int paintingColor = 0xFFFFFF;

    protected int[] sidedRedstoneOutput = new int[6];

    public MetaTileEntity(String metaTileEntityId) {
        this.metaTileEntityId = metaTileEntityId;
        initializeInventory();
    }

    protected void initializeInventory() {
        this.importItems = createImportItemHandler();
        this.exportItems = createExportItemHandler();
        this.itemInventory = new ItemHandlerProxy(importItems, exportItems);

        this.importFluids = createImportFluidHandler();
        this.exportFluids = createExportFluidHandler();
        this.fluidInventory = new FluidHandlerProxy(importFluids, exportFluids);
    }

    public MetaTileEntityHolder getHolder() {
        return holder;
    }

    public abstract MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder);

    public World getWorld() {
        return holder == null ? null : holder.getWorld();
    }

    public BlockPos getPos() {
        return holder == null ? null : holder.getPos();
    }

    public void markDirty() {
        if(holder != null)
            holder.markDirty();
    }

    public long getTimer() {
        return holder == null ? 0L : holder.getTimer();
    }

    public void writeCustomData(int discriminator, Consumer<PacketBuffer> dataWriter) {
        if(holder != null) {
            holder.writeCustomData(discriminator, dataWriter);
        }
    }

    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
    }

    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite getParticleTexture() {
        return TextureUtils.getMissingSprite();
    }

    /**
     * ItemStack currently being rendered by this meta tile entity
     * Use this to obtain itemstack-specific data like contained fluid, painting color
     * Generally useful in combination with {@link #writeItemStackData(net.minecraft.nbt.NBTTagCompound)}
     */
    @SideOnly(Side.CLIENT)
    protected ItemStack renderContextStack;

    @SideOnly(Side.CLIENT)
    public void setRenderContextStack(ItemStack itemStack) {
        this.renderContextStack = itemStack;
    }

    /**
     * Renders this meta tile entity
     * Note that you shouldn't refer to world-related information in this method, because it
     * will be called on ItemStacks too
     * @param renderState render state (either chunk batched or item)
     * @param pipeline default set of pipeline transformations
     */
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        TextureAtlasSprite atlasSprite = TextureUtils.getMissingSprite();
        IVertexOperation[] renderPipeline = ArrayUtils.add(pipeline, new ColourMultiplier(getPaintingColorForRendering()));
        for(EnumFacing face : EnumFacing.VALUES) {
            Textures.renderFace(renderState, translation, renderPipeline, face, Cuboid6.full, atlasSprite);
        }
    }

    @SideOnly(Side.CLIENT)
    public int getPaintingColorForRendering() {
        if(getWorld() == null && renderContextStack != null) {
            NBTTagCompound tagCompound = renderContextStack.getTagCompound();
            if(tagCompound != null && tagCompound.hasKey("PaintingColor", NBT.TAG_INT)) {
                return tagCompound.getInteger("PaintingColor");
            }
        }
        return paintingColor;
    }

    /**
     * Called from ItemBlock to initialize this MTE with data contained in ItemStack
     * @param itemStack itemstack of itemblock
     */
    public void initFromItemStackData(NBTTagCompound itemStack) {
        if(itemStack.hasKey("PaintingColor", NBT.TAG_INT)) {
            this.paintingColor = itemStack.getInteger("PaintingColor");
        }
    }

    /**
     * Called to write MTE specific data when it is destroyed to save it's state
     * into itemblock, which can be placed later to get {@link #initFromItemStackData} called
     * @param itemStack itemstack from which this MTE is being placed
     */
    public void writeItemStackData(NBTTagCompound itemStack) {
        if(this.paintingColor != 0xFFFFFFFF) { //for machines to stack
            itemStack.setInteger("PaintingColor", this.paintingColor);
        }
    }

    public final String getMetaName() {
        return "gregtech.machine." + metaTileEntityId;
    }

    public final String getMetaFullName() {
        return "gregtech.machine." + metaTileEntityId + ".name";
    }

    /**
     * Adds a trait to this meta tile entity
     * traits are objects linked with meta tile entity and performing certian
     * actions. usually traits implement capabilities
     * @param trait trait object to add
     * @return index of trait in list
     */
    int addMetaTileEntityTrait(MTETrait trait) {
        this.mteTraits.add(trait);
        return mteTraits.size() - 1;
    }

    protected IItemHandlerModifiable createImportItemHandler() {
        return new ItemStackHandler(0);
    }

    protected IItemHandlerModifiable createExportItemHandler() {
        return new ItemStackHandler(0);
    }

    protected FluidTankList createImportFluidHandler() {
        return new FluidTankList();
    }

    protected FluidTankList createExportFluidHandler() {
        return new FluidTankList();
    }

    protected boolean openGUIOnRightClick() {
        return true;
    }

    /**
     * Creates a UI instance for player opening inventory of this meta tile entity
     * @param entityPlayer player opening inventory
     * @return freshly created UI instance
     */
    protected abstract ModularUI createUI(EntityPlayer entityPlayer);

    /**
     * Called when player clicks on specific side of this meta tile entity
     * @return true if something happened, so animation will be played
     */
    public boolean onRightClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if(!playerIn.isSneaking() && openGUIOnRightClick()) {
            if(!getWorld().isRemote) {
                MetaTileEntityUIFactory.INSTANCE.openUI(getHolder(), (EntityPlayerMP) playerIn);
            }
            return true;
        }
        return false;
    }

    /**
     * Called when player clicks wrench on specific side of this meta tile entity
     * @return true if something happened, so wrench will get damaged and animation will be played
     */
    public boolean onWrenchClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if(playerIn.isSneaking()) {
            if(facing == getFrontFacing() || !isValidFrontFacing(facing))
                return false;
            //todo wrench sound here
            setFrontFacing(facing);
            return true;
        }
        return false;
    }

    /**
     * Called when player clicks screwdriver on specific side of this meta tile entity
     * @return true if something happened, so screwdriver will get damaged and animation will be played
     */
    public boolean onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        return false;
    }

    public void onLeftClick(EntityPlayer player) {
    }

    public boolean canConnectRedstone(@Nullable EnumFacing side) {
        return false;
    }

    public int getComparatorValue() {
        return 0;
    }

    public void update() {
        for(MTETrait mteTrait : this.mteTraits) {
            mteTrait.update();
        }
    }

    public final ItemStack getStackForm(int amount) {
        int metaTileEntityIntId = GregTechAPI.META_TILE_ENTITY_REGISTRY.getIdByObjectName(metaTileEntityId);
        return new ItemStack(GregTechAPI.MACHINE, amount, metaTileEntityIntId);
    }

    public final ItemStack getStackForm() {
        return getStackForm(1);
    }

    /**
     * Add special drops which this meta tile entity contains here
     * Meta tile entity item is ALREADY added into this list
     * Do NOT add inventory contents in this list - it will be dropped automatically when breakBlock is called
     * This will only be called if meta tile entity is broken with proper tool (i.e wrench)
     *
     * @param dropsList list of meta tile entity drops
     * @param harvester harvester of this meta tile entity, or null
     */
    public void getDrops(NonNullList<ItemStack> dropsList, @Nullable EntityPlayer harvester) {
    }

    /**
     * Called to obtain list of AxisAlignedBB used for collision testing, highlight rendering
     * and ray tracing this meta tile entity's block in world
     * @return list of collision boxes
     */
    public Cuboid6[] getCollisionBox() {
        return FULL_CUBE_COLLISION;
    }

    /**
     * @return tool required to dismantle this meta tile entity properly
     */
    public String getHarvestTool() {
        return "wrench";
    }

    /**
     * @return minimal level of tool required to dismantle this meta tile entity properly
     */
    public int getHarvestLevel() {
        return 1;
    }

    /**
     * @return true if this meta tile entity should serialze it's export and import inventories
     * Useful when you use your own unified inventory and don't need these dummies to be saved
     */
    protected boolean shouldSerializeInventories() {
        return true;
    }

    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        data.setInteger("FrontFacing", frontFacing.getIndex());
        data.setInteger("PaintingColor", paintingColor);

        if(shouldSerializeInventories()) {
            GTUtility.writeItems(importItems, "ImportInventory", data);
            GTUtility.writeItems(exportItems, "ExportInventory", data);

            data.setTag("ImportFluidInventory", importFluids.serializeNBT());
            data.setTag("ExportFluidInventory", exportFluids.serializeNBT());
        }

        for(MTETrait mteTrait : this.mteTraits) {
            data.setTag(mteTrait.getName(), mteTrait.serializeNBT());
        }

        return data;
    }

    public void readFromNBT(NBTTagCompound data) {
        this.frontFacing = EnumFacing.VALUES[data.getInteger("FrontFacing")];
        this.paintingColor = data.getInteger("PaintingColor");

        if(shouldSerializeInventories()) {
            GTUtility.readItems(importItems, "ImportInventory", data);
            GTUtility.readItems(exportItems, "ExportInventory", data);

            importFluids.deserializeNBT(data.getCompoundTag("ImportFluidInventory"));
            exportFluids.deserializeNBT(data.getCompoundTag("ExportFluidInventory"));
        }

        for(MTETrait mteTrait : this.mteTraits) {
            NBTTagCompound traitCompound = data.getCompoundTag(mteTrait.getName());
            mteTrait.deserializeNBT(traitCompound);
        }
    }

    public void writeInitialSyncData(PacketBuffer buf) {
        buf.writeByte(this.frontFacing.getIndex());
        buf.writeInt(this.paintingColor);
        for (EnumFacing side : EnumFacing.VALUES) {
            buf.writeInt(sidedRedstoneOutput[side.getIndex()]);
        }
        for(MTETrait mteTrait : this.mteTraits) {
            mteTrait.writeInitialData(buf);
        }
    }

    public void receiveInitialSyncData(PacketBuffer buf) {
        this.frontFacing = EnumFacing.VALUES[buf.readByte()];
        this.paintingColor = buf.readInt();
        for (EnumFacing side : EnumFacing.VALUES) {
            this.sidedRedstoneOutput[side.getIndex()] = buf.readInt();
        }
        for(MTETrait mteTrait : this.mteTraits) {
            mteTrait.receiveInitialData(buf);
        }
    }

    public void receiveCustomData(int dataId, PacketBuffer buf) {
        if(dataId == -1) {
            this.frontFacing = EnumFacing.VALUES[buf.readByte()];
        } else if(dataId == -2) {
            this.paintingColor = buf.readInt();
        } else if(dataId == -3) {
            this.sidedRedstoneOutput[buf.readByte()] = buf.readInt();
        } else {
            for(MTETrait mteTrait : this.mteTraits) {
                mteTrait.readSyncData(dataId, buf);
            }
        }
    }

    public boolean hasCapability(Capability<?> capability, EnumFacing side) {
        if((capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY &&
            getFluidInventory().getTankProperties().length > 0) ||
            (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY &&
            getItemInventory().getSlots() > 0))
            return true;
        for(MTETrait mteTrait : this.mteTraits) {
            if(mteTrait.getImplementingCapability() == capability)
                return true;
        }
        return false;
    }

    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY &&
            getFluidInventory().getTankProperties().length > 0) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(getFluidInventory());
        } else if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY &&
            getItemInventory().getSlots() > 0) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(getItemInventory());
        }
        for(MTETrait mteTrait : this.mteTraits) {
            if(mteTrait.getImplementingCapability() == capability)
                //noinspection unchecked
                return (T) mteTrait;
        }
        return null;
    }

    public boolean fillInternalTankFromFluidContainer(IItemHandlerModifiable importItems, IItemHandlerModifiable exportItems, int inputSlot, int outputSlot) {
        ItemStack inputContainerStack = importItems.extractItem(inputSlot, 1, true);
        FluidActionResult result = FluidUtil.tryEmptyContainer(inputContainerStack, importFluids, Integer.MAX_VALUE, null, false);
        if(result.isSuccess()) {
            ItemStack remainingItem = result.getResult();
            if(!remainingItem.isEmpty() && !importItems.insertItem(outputSlot, remainingItem, true).isEmpty())
                return false;
            FluidUtil.tryEmptyContainer(inputContainerStack, importFluids, Integer.MAX_VALUE, null, true);
            importItems.extractItem(inputSlot, 1, false);
            importItems.insertItem(outputSlot, remainingItem, false);
            return true;
        }
        return false;
    }

    public boolean fillContainerFromInternalTank(IItemHandlerModifiable importItems, IItemHandlerModifiable exportItems, int inputSlot, int outputSlot) {
        ItemStack emptyContainer = importItems.extractItem(inputSlot, 1, true);
        FluidActionResult result = FluidUtil.tryFillContainer(emptyContainer, exportFluids, Integer.MAX_VALUE, null, false);
        if(result.isSuccess()) {
            ItemStack remainingItem = result.getResult();
            if(!remainingItem.isEmpty() && !importItems.insertItem(outputSlot, remainingItem, true).isEmpty())
                return false;
            FluidUtil.tryFillContainer(emptyContainer, exportFluids, Integer.MAX_VALUE, null, true);
            importItems.extractItem(inputSlot, 1, false);
            importItems.insertItem(outputSlot, remainingItem, false);
            return true;
        }
        return false;
    }

    public void pushFluidsIntoNearbyHandlers(EnumFacing... allowedFaces) {
        for(EnumFacing nearbyFacing : allowedFaces) {
            IFluidHandler fluidHandler = FluidUtil.getFluidHandler(getWorld(),
                getPos().offset(nearbyFacing), nearbyFacing.getOpposite());
            if(fluidHandler == null) continue;
            for(int tankIndex = 0; tankIndex < exportFluids.getTanks(); tankIndex++) {
                IFluidTank tank = exportFluids.getTankAt(tankIndex);
                FluidStack fluidStack = tank.getFluid();
                if(fluidStack != null && fluidHandler.fill(fluidStack, false) != 0) {
                    int filledAmount = fluidHandler.fill(fluidStack, true);
                    tank.drain(filledAmount, true);
                }
            }
        }
    }

    public void pushItemsIntoNearbyHandlers(EnumFacing... allowedFaces) {
        for(EnumFacing nearbyFacing : allowedFaces) {
            TileEntity tileEntity = getWorld().getTileEntity(getPos().offset(nearbyFacing));
            if(tileEntity == null) continue;
            IItemHandler itemHandler = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, nearbyFacing.getOpposite());
            if(itemHandler == null) continue;
            for(int slotIndex = 0; slotIndex < exportItems.getSlots(); slotIndex++) {
                ItemStack stackInSlot = exportItems.getStackInSlot(slotIndex);
                if(stackInSlot.isEmpty()) continue;
                for(int hisSlotIndex = 0; hisSlotIndex < itemHandler.getSlots(); hisSlotIndex++) {
                    ItemStack remainingStack = itemHandler.insertItem(hisSlotIndex, stackInSlot, false);
                    if(remainingStack != stackInSlot) {
                        stackInSlot = remainingStack;
                        exportItems.setStackInSlot(slotIndex, remainingStack);
                    }
                    if(remainingStack.isEmpty()) break;
                }
            }
        }
    }

    public void pullItemsFromNearbyHandlers(EnumFacing... allowedFaces) {
        for(EnumFacing nearbyFacing : allowedFaces) {
            TileEntity tileEntity = getWorld().getTileEntity(getPos().offset(nearbyFacing));
            if(tileEntity == null) continue;
            IItemHandler itemHandler = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, nearbyFacing.getOpposite());
            if(itemHandler == null) continue;
            for(int slotIndex = 0; slotIndex < itemHandler.getSlots(); slotIndex++) {
                ItemStack stackInSlot = itemHandler.extractItem(slotIndex, itemHandler.getSlotLimit(slotIndex), true);
                if(stackInSlot.isEmpty()) continue;
                for(int hisSlotIndex = 0; hisSlotIndex < importItems.getSlots(); hisSlotIndex++) {
                    ItemStack remainingStack = importItems.insertItem(hisSlotIndex, stackInSlot, true);
                    if(remainingStack != stackInSlot) {
                        ItemStack actualStack = itemHandler.extractItem(slotIndex, stackInSlot.getCount() - remainingStack.getCount(), false);
                        stackInSlot = importItems.insertItem(hisSlotIndex, actualStack, false);
                    }
                    if(stackInSlot.isEmpty()) break;
                }
            }
        }
    }

    public void pullFluidsFromNearbyHandlers(EnumFacing... allowedFaces) {
        for(EnumFacing nearbyFacing : allowedFaces) {
            IFluidHandler fluidHandler = FluidUtil.getFluidHandler(getWorld(),
                getPos().offset(nearbyFacing), nearbyFacing.getOpposite());
            if(fluidHandler == null) continue;
            FluidStack fluidStack = fluidHandler.drain(Integer.MAX_VALUE, false);
            if(fluidStack == null || fluidStack.amount == 0) continue;
            int canInsertAmount = importFluids.fill(fluidStack, false);
            if(canInsertAmount > 0) {
                fluidStack = fluidHandler.drain(canInsertAmount, true);
                importFluids.fill(fluidStack, true);
            }
        }
    }

    public static boolean isItemHandlerEmpty(IItemHandler handler) {
        for(int i = 0; i < handler.getSlots(); i++) {
            if(!handler.getStackInSlot(i).isEmpty())
                return false;
        }
        return true;
    }

    public static boolean addItemsToItemHandler(IItemHandler handler, boolean simulate, NonNullList<ItemStack> items) {
        boolean notAllInserted = false;
        List<ItemStack> stacks = new ArrayList<>(items); //copy collection
        for (ItemStack stack : stacks) {
            notAllInserted |= !ItemHandlerHelper.insertItemStacked(handler, stack, simulate).isEmpty();
            if (notAllInserted && simulate) return false;
        }
        return !notAllInserted;
    }

    public static boolean addFluidsToFluidHandler(IFluidHandler handler, boolean simulate, List<FluidStack> items) {
        boolean notAllInserted = false;
        items = new ArrayList<>(items); //copy collection
        for (FluidStack stack : items) {
            int filled = handler.fill(stack, !simulate);
            notAllInserted |= !(filled == stack.amount);
            if (notAllInserted && simulate) return false;
        }
        return !notAllInserted;
    }

    public final int getOutputRedstoneSignal(@Nullable EnumFacing side) {
        return side == null ? Arrays.stream(sidedRedstoneOutput)
            .max().orElse(0) : sidedRedstoneOutput[side.getIndex()];
    }

    public final void setOutputRedstoneSignal(EnumFacing side, int strength) {
        Preconditions.checkNotNull(side, "side");
        this.sidedRedstoneOutput[side.getIndex()] = strength;
        if (!getWorld().isRemote) {
            markDirty();
            writeCustomData(-3, buf -> {
                buf.writeByte(side.getIndex());
                buf.writeInt(strength);
            });
        }
    }

    public void setFrontFacing(EnumFacing frontFacing) {
        Preconditions.checkNotNull(frontFacing, "frontFacing");
        this.frontFacing = frontFacing;
        if (!getWorld().isRemote) {
            markDirty();
            writeCustomData(-1, buf -> buf.writeByte(frontFacing.getIndex()));
            mteTraits.forEach(trait -> trait.onFrontFacingSet(frontFacing));
        }
    }

    public void setPaintingColor(int paintingColor) {
        this.paintingColor = paintingColor;
        if (!getWorld().isRemote) {
            markDirty();
            writeCustomData(-2, buf -> buf.writeInt(paintingColor));
        }
    }

    public boolean isValidFrontFacing(EnumFacing facing) {
        return facing != EnumFacing.UP && facing != EnumFacing.DOWN;
    }

    public final int getInputRedstoneSignal(EnumFacing side) {
        return getWorld().getStrongPower(getPos().offset(side));
    }

    public void clearMachineInventory(NonNullList<ItemStack> itemBuffer) {
        clearInventory(itemBuffer, importItems);
        clearInventory(itemBuffer, exportItems);
    }

    protected static void clearInventory(NonNullList<ItemStack> itemBuffer, IItemHandlerModifiable inventory) {
        for(int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stackInSlot = inventory.getStackInSlot(i);
            if(!stackInSlot.isEmpty()) {
                inventory.setStackInSlot(i, ItemStack.EMPTY);
                itemBuffer.add(stackInSlot);
            }
        }
    }

    /**
     * Called from breakBlock right before meta tile entity destruction
     * at this stage tile entity inventory is already dropped on ground, but drops aren't fetched yet
     * tile entity will still get getDrops called after this, if player broke block
     */
    public void onRemoval() {
    }

    public EnumFacing getFrontFacing() {
        return frontFacing;
    }

    public int getPaintingColor() {
        return paintingColor;
    }

    public IItemHandler getItemInventory() {
        return itemInventory;
    }

    public IFluidHandler getFluidInventory() {
        return fluidInventory;
    }

    public IItemHandlerModifiable getImportItems() {
        return importItems;
    }

    public IItemHandlerModifiable getExportItems() {
        return exportItems;
    }

    public FluidTankList getImportFluids() {
        return importFluids;
    }

    public FluidTankList getExportFluids() {
        return exportFluids;
    }

}