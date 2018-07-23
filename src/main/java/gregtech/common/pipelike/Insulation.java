package gregtech.common.pipelike;

import gregtech.api.pipelike.IBaseProperty;
import gregtech.api.unification.ore.OrePrefix;
import net.minecraft.util.IStringSerializable;

public enum Insulation implements IBaseProperty, IStringSerializable {

    WIRE_SINGLE("wire_single", 0.2f, 1, 2, OrePrefix.wireGtSingle, -1),
    WIRE_DOUBLE("wire_double", 0.3f, 2, 2, OrePrefix.wireGtDouble, -1),
    WIRE_QUADRUPLE("wire_quadruple", 0.4f, 4, 3, OrePrefix.wireGtQuadruple, -1),
    WIRE_OCTAL("wire_octal", 0.6f, 8, 3, OrePrefix.wireGtOctal, -1),
    WIRE_HEX("wire_hex", 1.0f, 16, 3, OrePrefix.wireGtHex, -1),

    CABLE_SINGLE("cable_single", 0.2f, 1, 1, OrePrefix.cableGtSingle, 0),
    CABLE_DOUBLE("cable_double", 0.3f, 2, 1, OrePrefix.cableGtDouble, 1),
    CABLE_QUADRUPLE("cable_quadruple", 0.4f, 4, 1, OrePrefix.cableGtQuadruple, 2),
    CABLE_OCTAL("cable_octal", 0.6f, 8, 1, OrePrefix.cableGtOctal, 3),
    CABLE_HEX("cable_hex", 1.0f, 16, 1, OrePrefix.cableGtHex, 4);

    public final String name;
    public final float thickness;
    public final int amperage;
    public final int lossMultiplier;
    public final OrePrefix orePrefix;
    public final int insulationLevel;

    Insulation(String name, float thickness, int amperage, int lossMultiplier, OrePrefix orePrefix, int insulated) {
        this.name = name;
        this.thickness = thickness;
        this.amperage = amperage;
        this.orePrefix = orePrefix;
        this.insulationLevel = insulated;
        this.lossMultiplier = lossMultiplier;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public OrePrefix getOrePrefix() {
        return orePrefix;
    }

    @Override
    public float getThickness() {
        return thickness;
    }

    @Override
    public boolean isColorable() {
        return insulationLevel > -1;
    }
}
