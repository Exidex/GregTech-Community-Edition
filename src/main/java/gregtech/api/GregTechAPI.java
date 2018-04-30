package gregtech.api;

import gregtech.api.block.machines.BlockMachine;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.type.DustMaterial;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.ore.StoneType;
import gregtech.api.unification.stack.SimpleItemStack;
import gregtech.api.util.BaseCreativeTab;
import gregtech.api.util.GTControlledRegistry;
import gregtech.api.util.GTWorldGen;
import gregtech.api.util.IBlockOre;
import gregtech.common.items.MetaItems;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;

import java.util.*;

public class GregTechAPI {

    public static BlockMachine MACHINE;

    public static final BaseCreativeTab TAB_GREGTECH =
        new BaseCreativeTab(GTValues.MODID + ".main", () -> MetaItems.BATTERY_HULL_HV.getStackForm());
    public static final BaseCreativeTab TAB_GREGTECH_MATERIALS =
        new BaseCreativeTab(GTValues.MODID + ".materials", () -> OreDictUnifier.get(OrePrefix.ingot, Materials.Aluminium), true);
    public static final BaseCreativeTab TAB_GREGTECH_ORES =
        new BaseCreativeTab(GTValues.MODID + ".ores", () -> MetaItems.JACKHAMMER.getStackForm(), true);

    public static final GTControlledRegistry<MetaTileEntity> META_TILE_ENTITY_REGISTRY = new GTControlledRegistry<>(Short.MAX_VALUE);

    public static <T extends MetaTileEntity> T registerMetaTileEntity(int id, T sampleMetaTileEntity) {
        META_TILE_ENTITY_REGISTRY.register(id, sampleMetaTileEntity.metaTileEntityId, sampleMetaTileEntity);
        return sampleMetaTileEntity;
    }

    public static final Map<Fluid, EnumDyeColor> LIQUID_DYE_MAP = new HashMap<>();

    public static final Map<Integer, ResourceLocation> soundList = new HashMap<>();

    /**
     * Registered tool items list
     * Damageable and electric items are accepted.
     */
    public static final HashSet<SimpleItemStack> crowbarList = new HashSet<>(),
            screwdriverList = new HashSet<>(),
            wrenchList = new HashSet<>(),
            softHammerList = new HashSet<>(),
            hardHammerList = new HashSet<>(),
            solderingToolList = new HashSet<>(),
            solderingMetalList = new HashSet<>();

    /**
     * The List of Hazmat Armors
     */
    public static final HashSet<SimpleItemStack> gasHazmatList = new HashSet<>(),
            bioHazmatList = new HashSet<>(),
            frostHazmatList = new HashSet<>(),
            heatHazmatList = new HashSet<>(),
            radioHazmatList = new HashSet<>(),
            electroHazmatList = new HashSet<>();

    /**
     * The List of Dimensions, which are Whitelisted for the Teleporter. This list should not contain other Planets.
     * Mystcraft Dimensions and other Dimensional Things should be allowed.
     * Mystcraft and Twilight Forest are automatically considered a Dimension, without being in this List.
     */
    public static final Collection<Integer> dimensionalList = new HashSet<>();

    /**
     * The List of all GT worldgens, except for ore veins.
     */
    public static final List<GTWorldGen> worldgenList = new ArrayList<>();

    /**
     * Table of all ore blocks
     */
    public static final Map<DustMaterial, Map<StoneType, IBlockOre>> oreBlockTable = new HashMap<>(); 

    private static int size = 0; /* Used to assign Minecraft IDs to our SoundEvents. We don't use them. */
    /* TODO SOUNDS
    static {
        size = SoundEvent.REGISTRY.getKeys().size();

        dimensionalList.add(-1);
        dimensionalList.add(0);
        dimensionalList.add(1);

        soundList.put(0, new ResourceLocation("entity.arrow.shoot"));
        soundList.put(1, new ResourceLocation("block.anvil.use"));
        soundList.put(2, new ResourceLocation("block.anvil.break"));
        soundList.put(3, new ResourceLocation("block.stone_button.click_on"));
        soundList.put(4, new ResourceLocation("block.fire.extinguish"));
        soundList.put(5, new ResourceLocation("entity.generic.explode"));
        soundList.put(6, new ResourceLocation("item.flintandsteel.use"));

        registerSound(100, new ResourceLocation("ic2", "tools.Wrench"));
        registerSound(101, new ResourceLocation("ic2", "tools.RubberTrampoline"));
        registerSound(102, new ResourceLocation("ic2", "tools.Painter"));
        registerSound(103, new ResourceLocation("ic2", "tools.BatteryUse"));
        registerSound(104, new ResourceLocation("ic2", "tools.chainsaw.ChainsawUseOne"));
        registerSound(105, new ResourceLocation("ic2", "tools.chainsaw.ChainsawUseTwo"));
        registerSound(106, new ResourceLocation("ic2", "tools.drill.DrillSoft"));
        registerSound(107, new ResourceLocation("ic2", "tools.drill.DrillHard"));
        registerSound(108, new ResourceLocation("ic2", "tools.ODScanner"));

        registerSound(200, new ResourceLocation("ic2", "machines.ExtractorOp"));
        registerSound(201, new ResourceLocation("ic2", "machines.MaceratorOp"));
        registerSound(202, new ResourceLocation("ic2", "machines.InductionLoop"));
        registerSound(203, new ResourceLocation("ic2", "machines.CompressorOp"));
        registerSound(204, new ResourceLocation("ic2", "machines.RecyclerOp"));
        registerSound(205, new ResourceLocation("ic2", "machines.MinerOp"));
        registerSound(206, new ResourceLocation("ic2", "machines.PumpOp"));
        registerSound(207, new ResourceLocation("ic2", "machines.ElectroFurnaceLoop"));
        registerSound(208, new ResourceLocation("ic2", "machines.InductionLoop"));
        registerSound(209, new ResourceLocation("ic2", "machines.MachineOverload"));
        registerSound(210, new ResourceLocation("ic2", "machines.InterruptOne"));
        registerSound(211, new ResourceLocation("ic2", "machines.KaChing"));
        registerSound(212, new ResourceLocation("ic2", "machines.MagnetizerLoop"));
    }

    public static void registerSound(int id, ResourceLocation loc) {
        SoundEvent e = new SoundEvent(loc);
        if(!SoundEvent.REGISTRY.containsKey(loc)) {
            SoundEvent.REGISTRY.register(size++, loc, e);
        }
        soundList.put(id, loc);
    }
    */
}