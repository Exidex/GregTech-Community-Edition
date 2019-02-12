package gregtech.api.unification.ore;

import gregtech.api.unification.material.Materials;
import gregtech.common.blocks.BlockGranite;
import gregtech.common.blocks.BlockGranite.GraniteVariant;
import gregtech.common.blocks.BlockMineral;
import gregtech.common.blocks.BlockMineral.MineralVariant;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.blocks.StoneBlock.ChiselingVariant;
import net.minecraft.block.BlockRedSandstone;
import net.minecraft.block.BlockSandStone;
import net.minecraft.block.BlockStone;
import net.minecraft.block.SoundType;
import net.minecraft.init.Blocks;

public class StoneTypes {

    public static StoneType STONE = new StoneType(0, "stone", OrePrefix.ore, Materials.Stone, "blocks/stone", Blocks.STONE::getDefaultState, state -> state.getBlock() instanceof BlockStone && state.getValue(BlockStone.VARIANT) == BlockStone.EnumType.STONE);
    public static StoneType GRANITE = new StoneType(1, "granite", OrePrefix.ore, Materials.Stone, "blocks/stone_granite", () -> Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.GRANITE), state -> state.getBlock() instanceof BlockStone && state.getValue(BlockStone.VARIANT) == BlockStone.EnumType.GRANITE);
    public static StoneType DIORITE = new StoneType(2, "diorite", OrePrefix.ore, Materials.Stone, "blocks/stone_diorite", () -> Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.DIORITE), state -> state.getBlock() instanceof BlockStone && state.getValue(BlockStone.VARIANT) == BlockStone.EnumType.DIORITE);
    public static StoneType ANDESITE = new StoneType(3, "andesite", OrePrefix.ore, Materials.Stone, "blocks/stone_andesite", () -> Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.ANDESITE), state -> state.getBlock() instanceof BlockStone && state.getValue(BlockStone.VARIANT) == BlockStone.EnumType.ANDESITE);
    public static StoneType GRAVEL = new StoneType(4, "gravel", OrePrefix.oreGravel, Materials.Flint, "shovel", 0x2, "blocks/gravel", Blocks.GRAVEL::getDefaultState, state -> state.getBlock() == Blocks.GRAVEL).setTextureForAllFacing("blocks/gravel").setSoundType(SoundType.GROUND);
    public static StoneType BEDROCK = new StoneType(5, "bedrock", OrePrefix.ore, Materials.Stone, 0x1, "blocks/bedrock", Blocks.BEDROCK::getDefaultState, state -> state.getBlock() == Blocks.BEDROCK).setTextureForAllFacing("blocks/bedrock");
    public static StoneType NETHERRACK = new StoneType(6, "netherrack", OrePrefix.oreNetherrack, Materials.Netherrack, "blocks/netherrack", Blocks.NETHERRACK::getDefaultState, state -> state.getBlock() == Blocks.NETHERRACK);
    public static StoneType ENDSTONE = new StoneType(7, "endstone", OrePrefix.oreEndstone, Materials.Endstone, "blocks/end_stone", Blocks.END_STONE::getDefaultState, state -> state.getBlock() == Blocks.END_STONE);
    public static StoneType SANDSTONE = new StoneType(8, "sandstone", OrePrefix.oreSand, Materials.SiliconDioxide, 0, "blocks/sandstone_normal", () -> Blocks.SANDSTONE.getDefaultState().withProperty(BlockSandStone.TYPE, BlockSandStone.EnumType.DEFAULT), state -> state.getBlock() instanceof BlockSandStone && state.getValue(BlockSandStone.TYPE) == BlockSandStone.EnumType.DEFAULT).setTextureForDownFacing("blocks/sandstone_bottom").setTextureForUpFacing("blocks/sandstone_top").setTextureForAllSide("blocks/sandstone_normal");
    public static StoneType REDSANDSTONE = new StoneType(9, "red_sandstone", OrePrefix.oreSand, Materials.SiliconDioxide, 0, "blocks/red_sandstone_normal", Blocks.RED_SANDSTONE::getDefaultState, state -> state.getBlock() instanceof BlockRedSandstone && state.getValue(BlockRedSandstone.TYPE) == BlockRedSandstone.EnumType.DEFAULT).setTextureForDownFacing("blocks/red_sandstone_bottom").setTextureForUpFacing("blocks/red_sandstone_top").setTextureForAllSide("blocks/red_sandstone_normal");
    public static StoneType BLACK_GRANITE = new StoneType(10, "black_granite", OrePrefix.oreBlackgranite, Materials.GraniteBlack, "gregtech:blocks/stones/granite/granite_black_stone", () -> MetaBlocks.GRANITE.withVariant(GraniteVariant.BLACK_GRANITE, ChiselingVariant.NORMAL), state -> state.getBlock() instanceof BlockGranite && ((BlockGranite) state.getBlock()).getVariant(state) == GraniteVariant.BLACK_GRANITE);
    public static StoneType RED_GRANITE = new StoneType(11, "red_granite", OrePrefix.oreRedgranite, Materials.GraniteRed, "gregtech:blocks/stones/granite/granite_red_stone", () -> MetaBlocks.GRANITE.withVariant(GraniteVariant.RED_GRANITE, ChiselingVariant.NORMAL), state -> state.getBlock() instanceof BlockGranite && ((BlockGranite) state.getBlock()).getVariant(state) == GraniteVariant.RED_GRANITE);
    public static StoneType MARBLE = new StoneType(12, "marble", OrePrefix.oreMarble, Materials.Marble, "gregtech:blocks/stones/marble/marble_stone", () -> MetaBlocks.MINERAL.withVariant(MineralVariant.MARBLE, ChiselingVariant.NORMAL), state -> state.getBlock() instanceof BlockMineral && ((BlockMineral) state.getBlock()).getVariant(state) == BlockMineral.MineralVariant.MARBLE);
    public static StoneType BASALT = new StoneType(13, "basalt", OrePrefix.oreBasalt, Materials.Basalt, "gregtech:blocks/stones/basalt/basalt_stone", () -> MetaBlocks.MINERAL.withVariant(MineralVariant.BASALT, ChiselingVariant.NORMAL), state -> state.getBlock() instanceof BlockMineral && ((BlockMineral) state.getBlock()).getVariant(state) == BlockMineral.MineralVariant.BASALT);

}
