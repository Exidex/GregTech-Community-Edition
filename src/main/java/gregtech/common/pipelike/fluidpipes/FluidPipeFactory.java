package gregtech.common.pipelike.fluidpipes;

import gregtech.api.GregTechAPI;
import gregtech.api.pipelike.BlockPipeLike;
import gregtech.api.pipelike.ITilePipeLike;
import gregtech.api.pipelike.PipeFactory;
import gregtech.api.unification.material.type.GemMaterial;
import gregtech.api.unification.material.type.IngotMaterial;
import gregtech.api.unification.material.type.Material;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.GTUtility;
import gregtech.api.worldentries.pipenet.WorldPipeNet;
import gregtech.common.pipelike.fluidpipes.pipenet.FluidPipeNet;
import net.minecraft.block.SoundType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

import static gregtech.api.unification.material.MarkerMaterials.Tier.Superconductor;
import static gregtech.api.unification.material.MarkerMaterials.Tier.Ultimate;
import static gregtech.api.unification.material.Materials.*;

public class FluidPipeFactory extends PipeFactory<TypeFluidPipe, FluidPipeProperties, IFluidHandler> {

    public static final FluidPipeFactory INSTANCE = new FluidPipeFactory();

    private FluidPipeFactory() {
        super("fluid_pipe", CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, TypeFluidPipe.class, FluidPipeProperties.class);
        registerDefaultFluidPipes();
    }

    private void registerDefaultFluidPipes() {
        registerFluidPipe(Wood, 100, 350, false);
        setOnlyMediumSized(Wood);

        registerFluidPipe(Copper, 200, 1000);
        registerFluidPipe(Bronze, 400, 2000);
        registerFluidPipe(Steel, 800, 2500);
        registerFluidPipe(StainlessSteel, 1200, 3000);
        registerFluidPipe(Titanium, 1600, 5000);
        registerFluidPipe(TungstenSteel, 2000, 7500);

        registerFluidPipe(Plastic, 1200, 350);
        registerFluidPipe(Polytetrafluoroethylene, 2000, 600);

        registerFluidPipe(Ultimate, 24000, 1500);
        setOnlyMediumSized(Ultimate);
        specifyMaterialColor(Ultimate, 0xC80000);

        registerFluidPipe(Superconductor, 800, 100000);
        setOnlyMediumSized(Superconductor);
        OrePrefix.pipeSmall.setIgnored(Superconductor);
        OrePrefix.pipeLarge.setIgnored(Superconductor);
        specifyMaterialColor(Superconductor, 0xFFFF00);
    }

    public void setOnlyMediumSized(Material material) {
        OrePrefix.pipeTiny.setIgnored(material);
        OrePrefix.pipeHuge.setIgnored(material);
        OrePrefix.pipeQuadruple.setIgnored(material);
        OrePrefix.pipeNonuple.setIgnored(material);
        OrePrefix.pipeSexdecuple.setIgnored(material);
    }

    public void registerFluidPipe(Material material, int fluidCapacity, int heatLimit) {
        registerPropertyForMaterial(material, new FluidPipeProperties(fluidCapacity, heatLimit, true));
    }

    public void registerFluidPipe(Material material, int fluidCapacity, int heatLimit, boolean isGasProof) {
        registerPropertyForMaterial(material, new FluidPipeProperties(fluidCapacity, heatLimit, isGasProof));
    }

    @Override
    protected void initBlock(BlockPipeLike<TypeFluidPipe, FluidPipeProperties, IFluidHandler> block, Material material, FluidPipeProperties materialProperty) {
        block.setCreativeTab(GregTechAPI.TAB_GREGTECH);
        block.setSoundType(material instanceof IngotMaterial ? SoundType.METAL : material.toString().contains("wood") ? SoundType.WOOD : SoundType.STONE);
        block.setHarvestLevel(material.toString().contains("wood") ? "axe" : "wrench", material instanceof IngotMaterial ? ((IngotMaterial) material).harvestLevel : material instanceof GemMaterial ? ((GemMaterial) material).harvestLevel : 1);
        block.setHardness(4.0f);
        block.setResistance(4.5f);
        block.setLightOpacity(1);
    }

    @Override
    protected FluidPipeProperties createActualProperty(TypeFluidPipe baseProperty, FluidPipeProperties materialProperty) {
        return new FluidPipeProperties(baseProperty.fluidCapacityMultiplier * materialProperty.getFluidCapacity(),
            baseProperty.multiple,
            materialProperty.getHeatLimit(),
            materialProperty.isGasProof());
    }

    @Override
    protected void onEntityCollided(Entity entity, ITilePipeLike<TypeFluidPipe, FluidPipeProperties> tile) {
        if (entity instanceof EntityLivingBase) {
            FluidPipeNet net = getPipeNetAt(tile);
            if (net != null) {
                applyThermalDamage((EntityLivingBase) entity, Arrays.stream(net.getTankProperties(tile.getTilePos(), null))
                    .map(IFluidTankProperties::getContents)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()), 1.0F);
            }
        }
    }

    @Override
    public int getDefaultColor() {
        return 0x777777;
    }

    @Override
    public FluidPipeHandler createCapability(ITilePipeLike<TypeFluidPipe, FluidPipeProperties> tile) {
        return new FluidPipeHandler(tile);
    }

    @Override
    public IFluidHandler onGettingNetworkCapability(IFluidHandler capability, EnumFacing facing) {
        if (capability instanceof FluidPipeHandler && facing != null) {
            return new FluidPipeHandler.SidedHandler((FluidPipeHandler) capability, facing);
        }
        return capability;
    }

    @Override
    public FluidPipeNet createPipeNet(WorldPipeNet worldNet) {
        return new FluidPipeNet(worldNet);
    }

    @Override
    public FluidPipeNet getPipeNetAt(ITilePipeLike<TypeFluidPipe, FluidPipeProperties> tile) {
        return (FluidPipeNet) super.getPipeNetAt(tile);
    }

    @Override
    public FluidPipeProperties createEmptyProperty() {
        return new FluidPipeProperties();
    }

    public static void applyGasLeakingDamage(EntityLivingBase entity, Collection<FluidStack> fluidStacks) {
        applyThermalDamage(entity, fluidStacks, 2.0F);
    }

    private static void applyThermalDamage(EntityLivingBase entity, Collection<FluidStack> fluidStacks, float multiplier) {
        float min = 0.0F, max = 0.0F;
        for (FluidStack stack : fluidStacks) {
            float damage = getThermalDamage(stack) * multiplier;
            if (damage > max) max = damage;
            if (damage < min) min = damage;
        }
        if (max == -min) return;
        if (max < -min || !GTUtility.applyHeatDamage(entity, max)) {
            GTUtility.applyFrostDamage(entity, -min);
        }
    }

    private static float getThermalDamage(FluidStack stack) {
        int temperature = stack.getFluid().getTemperature(stack);
        if (temperature > 300) return (temperature - 300) / 50.0F;
        if (temperature < 270) return (temperature - 270) / 25.0F;
        return 0.0F;
    }

    public static int MASK_RENDER_EXPOSED = 1 << 12;

    /**
     * @return 0: {@param tile} is not a multipipe
     *          -1: no access allowed
     *          other: 1 + index of the subpipe that is available
     */
    public int getMultiPipeAccessAtSide(ITilePipeLike<TypeFluidPipe, ?> tile, EnumFacing fromFacing) {
        if (tile == null) return -1;
        if (tile.getBaseProperty().multiple == 1) return 0;
        return -1;//TODO actual logic
    }

    @Override
    protected int isPipeAccessibleAtSide(ITilePipeLike<TypeFluidPipe, ?> tile, IBlockAccess world, BlockPos pos, EnumFacing fromFacing, int fromColor, float selfThickness) {
        ITilePipeLike<TypeFluidPipe, ?> sideTile = getTile(world, pos);
        int index = getMultiPipeAccessAtSide(tile, fromFacing);
        int sideIndex = getMultiPipeAccessAtSide(sideTile, fromFacing.getOpposite());
        if (sideTile == null) return index < 0 ? 1 : 0;
        if (index < 0 && sideIndex == 0) return -1;
        if ((index < 0 || sideIndex < 0) && tile.getBaseProperty().multiple != sideTile.getBaseProperty().multiple) return 1;
        if ((sideTile.getInternalConnections() & (ITilePipeLike.MASK_BLOCKED << fromFacing.getIndex())) != 0) return 1;
        if (fromColor != getDefaultColor() && sideTile.getColor() != getDefaultColor() && fromColor != sideTile.getColor()) return 1;
        return selfThickness <= sideTile.getBaseProperty().getThickness() ? 2 : 3;
    }

    /**
     * @return bitmask for render
     *          =  (render exposed, 6 bits)     << 12
     *           | (render side, 6 bits)        << 6
     *           | (formal connection, 6 bits)
     */
    @Override
    public int getRenderMask(ITilePipeLike<TypeFluidPipe, ?> tile, IBlockAccess world, BlockPos pos) {
        int blockedConnection = tile.getInternalConnections();
        int connectedSideMask = blockedConnection >> 12 & 0b111111;
        BlockPos.PooledMutableBlockPos sidePos = BlockPos.PooledMutableBlockPos.retain().setPos(pos);
        for (EnumFacing facing : EnumFacing.VALUES) if ((blockedConnection & ITilePipeLike.MASK_BLOCKED << facing.getIndex()) == 0) {
            EnumFacing opposite = facing.getOpposite();
            sidePos.move(facing);
            switch (isPipeAccessibleAtSide(tile, world, sidePos, facing.getOpposite(), tile.getColor(), tile.getBaseProperty().getThickness())) {
                case 0: if (!tile.hasCapabilityAtSide(capability, facing)) break;
                case 3: connectedSideMask |= MASK_RENDER_SIDE << facing.getIndex();
                case 2: connectedSideMask |= MASK_FORMAL_CONNECTION << facing.getIndex(); break;
                case -1: connectedSideMask |= MASK_RENDER_EXPOSED << facing.getIndex(); break;
            }
            sidePos.move(opposite);
        }
        sidePos.release();
        return connectedSideMask;
    }
}
