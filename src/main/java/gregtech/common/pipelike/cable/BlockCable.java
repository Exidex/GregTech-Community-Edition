package gregtech.common.pipelike.cable;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.damagesources.DamageSources;
import gregtech.api.pipenet.block.BlockPipe;
import gregtech.api.unification.material.type.Material;
import gregtech.api.util.GTUtility;
import gregtech.common.pipelike.cable.net.EnergyNet;
import gregtech.common.pipelike.cable.net.WorldENet;
import gregtech.common.pipelike.cable.tile.TileEntityCable;
import gregtech.common.render.CableRenderer;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class BlockCable extends BlockPipe<Insulation, WireProperties, WorldENet> implements ITileEntityProvider {

    public BlockCable(Material material, WireProperties cableProperties) {
        super(material, cableProperties);
        setHarvestLevel("cutter", 1);
    }

    @Override
    public Class<Insulation> getPipeTypeClass() {
        return Insulation.class;
    }

    @Override
    public WorldENet getWorldPipeNet(World world) {
        return WorldENet.getWorldENet(world);
    }

    @Override
    public int getActiveNodeConnections(IBlockAccess world, BlockPos nodePos) {
        int activeNodeConnections = 0;
        for(EnumFacing side : EnumFacing.VALUES) {
            BlockPos offsetPos = nodePos.offset(side);
            TileEntity tileEntity = world.getTileEntity(offsetPos);
            //do not connect to null cables and ignore cables
            if(tileEntity == null || getPipeTileEntity(tileEntity) != null) continue;
            EnumFacing opposite = side.getOpposite();
            IEnergyContainer energyContainer = tileEntity.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, opposite);
            if(energyContainer != null) {
                activeNodeConnections |= 1 << side.getIndex();
            }
        }
        return activeNodeConnections;
    }

    /*@Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if(!worldIn.isRemote) {
            EnergyNet energyNet = getWorldPipeNet(worldIn).getNetFromPos(pos);
            playerIn.sendMessage(new TextComponentString("Energy Net: " + energyNet));
            if(energyNet != null) {
                playerIn.sendMessage(new TextComponentString("Current Max Voltage: " + energyNet.getLastMaxVoltage()));
                playerIn.sendMessage(new TextComponentString("Current Total Amperage: " + energyNet.getLastAmperage()));
                playerIn.sendMessage(new TextComponentString("All Nodes: " + energyNet.getAllNodes().keySet()));
                playerIn.sendMessage(new TextComponentString("Active Nodes: " +
                    energyNet.getAllNodes().entrySet().stream().filter(entry -> entry.getValue().isActive).map(Entry::getKey).collect(Collectors.toList())));
            }
        }
        return false;
    }*/

    @Override
    public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
        if(!worldIn.isRemote && state.getValue(pipeVariantProperty).insulationLevel == -1 && entityIn instanceof EntityLivingBase) {
            EntityLivingBase entityLiving = (EntityLivingBase) entityIn;
            EnergyNet energyNet = getWorldPipeNet(worldIn).getNetFromPos(pos);
            if(energyNet != null && !GTUtility.isWearingFullElectroHazmat(entityLiving)) {
                long voltage = energyNet.getLastMaxVoltage();
                long amperage = energyNet.getLastAmperage();
                if(voltage > 0L && amperage > 0L) {
                    float damageAmount = (GTUtility.getTierByVoltage(voltage) + 1) * amperage * 4;
                    entityLiving.attackEntityFrom(DamageSources.getElectricDamage(), damageAmount);
                }
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return CableRenderer.BLOCK_RENDER_TYPE;
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityCable();
    }
}
