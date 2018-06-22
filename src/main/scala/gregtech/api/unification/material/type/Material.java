package gregtech.api.unification.material.type;

import com.google.common.base.CaseFormat;
import com.google.common.collect.ImmutableList;
import crafttweaker.annotations.ZenRegister;
import gregtech.api.unification.Element;
import gregtech.api.unification.material.MaterialIconSet;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.api.util.GTControlledRegistry;
import gregtech.api.util.GTLog;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import stanhebben.zenscript.annotations.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import static gregtech.api.GTValues.M;
import static gregtech.api.util.GTUtility.createFlag;

@ZenClass("mods.gregtech.material.Material")
@ZenRegister
public abstract class Material implements Comparable<Material> {

	public static GTControlledRegistry<Material> MATERIAL_REGISTRY = new GTControlledRegistry<>(1000);

	public static void freezeRegistry() {
        GTLog.logger.info("Freezing material registry...");
		MATERIAL_REGISTRY.freezeRegistry();
	}

	public static final class MatFlags {

	    private static Map<String, Entry<Long, Class<? extends Material>>> materialFlagRegistry = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

	    public static void registerMaterialFlag(String name, long value, Class<? extends Material> classFilter) {
	        if(materialFlagRegistry.containsKey(name))
	            throw new IllegalArgumentException("Flag with name " + name + " already registered!");
	        materialFlagRegistry.put(name, new SimpleEntry<>(value, classFilter));
        }

        public static void registerMaterialFlagsHolder(Class<?> holder, Class<? extends Material> lowerBounds) {
	        for(Field holderField : holder.getFields()) {
	            int modifiers = holderField.getModifiers();
	            if(holderField.getType() != long.class ||
                    !Modifier.isPublic(modifiers) ||
                    !Modifier.isStatic(modifiers) ||
                    !Modifier.isFinal(modifiers))
	                continue;
	            String flagName = holderField.getName();
                long flagValue;
                try {
                    flagValue = holderField.getLong(null);
                } catch (IllegalAccessException exception) {
                    throw new RuntimeException(exception);
                }
                registerMaterialFlag(flagName, flagValue, lowerBounds);
            }
        }

        public static long resolveFlag(String name, Class<? extends Material> selfClass) {
            Entry<Long, Class<? extends Material>> flagEntry = materialFlagRegistry.get(name);
            if(flagEntry == null)
                throw new IllegalArgumentException("Flag with name " + name + " not registered");
            else if(!flagEntry.getValue().isAssignableFrom(selfClass))
                throw new IllegalArgumentException("Flag " + name + " cannot be applied to material type " +
                    selfClass.getSimpleName() + ", lower bound is " + flagEntry.getValue().getSimpleName());
            return flagEntry.getKey();
        }

		/**
		 * Enables electrolyzer decomposition recipe generation
		 */
		public static final long DECOMPOSITION_BY_ELECTROLYZING = createFlag(0);

		/**
		 * Enables centrifuge decomposition recipe generation
		 */
		public static final long DECOMPOSITION_BY_CENTRIFUGING = createFlag(1);

        /**
         * Add to material if it has constantly burning aura
         */
        public static final long BURNING = createFlag(7);

		/**
		 * Add to material if it is some kind of flammable
		 */
		public static final long FLAMMABLE = createFlag(2);

		/**
		 * Add to material if it is some kind of explosive
		 */
		public static final long EXPLOSIVE = createFlag(4);

		/**
		 * Add to material to disable it's unification fully
		 */
		public static final long NO_UNIFICATION = createFlag(5);

		/**
		 * Add to material if any of it's items cannot be recycled to get scrub
		 */
		public static final long NO_RECYCLING = createFlag(6);

        /**
         * Disables decomposition recipe generation for this material and all materials that has it as component
         */
        public static final long DISABLE_DECOMPOSITION = createFlag(0);

        /**
         * Decomposition recipe requires hydrogen as additional input. Amount is equal to input amount
         */
        public static final long DECOMPOSITION_REQUIRES_HYDROGEN = createFlag(1);

        static {
            registerMaterialFlagsHolder(MatFlags.class, Material.class);
        }
	}

	/**
	 * Color of material in RGB format
	 */
	@ZenProperty("color")
    public final int materialRGB;

	/**
	 * Chemical formula of this material
	 */
	@ZenProperty
	public final String chemicalFormula;

	/**
	 * Icon set for this material meta-items generation
	 */
	@ZenProperty("iconSet")
	public final MaterialIconSet materialIconSet;

	/**
	 * List of this material component
	 */
	@ZenProperty("components")
	public final ImmutableList<MaterialStack> materialComponents;

	/**
	 * Generation flags of this material
	 * @see MatFlags
	 * @see DustMaterial.MatFlags
	 */
	@ZenProperty("generationFlagsRaw")
	protected long materialGenerationFlags;

	/**
	 * Element of this material consist of
	 */
	@ZenProperty
	public final Element element;

	private String calculateChemicalFormula() {
	    if(element != null) {
	        return element.name();
        }
        if(!materialComponents.isEmpty()) {
	        StringBuilder components = new StringBuilder();
	        for(MaterialStack component : materialComponents)
	            components.append(component.toString());
	        return components.toString();
        }
        return "";
    }

	public Material(int metaItemSubId, String name, int materialRGB, MaterialIconSet materialIconSet, ImmutableList<MaterialStack> materialComponents, long materialGenerationFlags, Element element) {
        this.materialRGB = materialRGB;
        this.materialIconSet = materialIconSet;
        this.materialComponents = materialComponents;
        this.materialGenerationFlags = verifyMaterialBits(materialGenerationFlags);
        this.element = element;
        this.chemicalFormula = calculateChemicalFormula();
        calculateDecompositionType();
        initializeMaterial();
        if (metaItemSubId > -1) {
            //if we have an generated metaitem, register ourselves with meta item ID
            MATERIAL_REGISTRY.register(metaItemSubId, name, this);
        } else {
            //if we doesn't, just put name mapping for this material
            MATERIAL_REGISTRY.putObject(name, this);
        }
    }

	protected void initializeMaterial() {
    }

	protected long verifyMaterialBits(long materialBits) {
		return materialBits;
	}

	public void addFlag(long... materialGenerationFlags) {
	    if(MATERIAL_REGISTRY.isFrozen()) {
	        throw new IllegalStateException("Cannot add flag to material when registry is frozen!");
        }
		long combined = 0;
		for (long materialGenerationFlag : materialGenerationFlags) {
			combined |= materialGenerationFlag;
		}
		this.materialGenerationFlags |= verifyMaterialBits(combined);
	}

	@ZenMethod("hasFlagRaw")
	public boolean hasFlag(long generationFlag) {
		return (materialGenerationFlags & generationFlag) != 0;
	}

	@ZenMethod
	public void addFlags(String... flagNames) {
        addFlag(convertMaterialFlags(getClass(), flagNames));
    }

    public static long convertMaterialFlags(Class<? extends Material> materialClass, String... flagNames) {
        long combined = 0;
        for(String flagName : flagNames) {
            long materialFlagId = MatFlags.resolveFlag(flagName, materialClass);
            combined |= materialFlagId;
        }
        return combined;
    }

	@ZenMethod
	public boolean hasFlag(String flagName) {
	    long materialFlagId = MatFlags.resolveFlag(flagName, getClass());
	    return hasFlag(materialFlagId);
    }

	protected void calculateDecompositionType() {
	    if(!materialComponents.isEmpty() &&
            !hasFlag(MatFlags.DECOMPOSITION_BY_CENTRIFUGING) &&
            !hasFlag(MatFlags.DECOMPOSITION_BY_ELECTROLYZING) &&
            !hasFlag(MatFlags.DISABLE_DECOMPOSITION)) {
	        boolean onlyFluidMaterials = true;
	        boolean onlyMetalMaterials = true;
	        for(MaterialStack materialStack : materialComponents) {
	            Material material = materialStack.material;
	            onlyFluidMaterials &= material.getClass() == FluidMaterial.class;
	            onlyMetalMaterials &= material.getClass() == IngotMaterial.class;
            }
            if(onlyFluidMaterials || onlyMetalMaterials) {
	            //if we contain only fluids or only metals, then centrifuging will do it's job
	            materialGenerationFlags |= MatFlags.DECOMPOSITION_BY_CENTRIFUGING;
            } else {
	            //otherwise, we use electrolyzing to break material into ions
	            materialGenerationFlags |= MatFlags.DECOMPOSITION_BY_ELECTROLYZING;
            }
        }
    }

    @ZenGetter("radioactive")
	public boolean isRadioactive() {
		if (element != null)
			return element.halfLifeSeconds >= 0;
		for (MaterialStack material : materialComponents)
			if (material.material.isRadioactive()) return true;
		return false;
	}

	@ZenGetter("protons")
	public long getProtons() {
		if (element != null)
			return element.getProtons();
		if (materialComponents.size() <= 0)
			return Element.Tc.getProtons();
		long totalProtons = 0, totalAmount = 0;
		for (MaterialStack material : materialComponents) {
			totalAmount += material.amount;
			totalProtons += material.amount * material.material.getProtons();
		}
		return (getDensity() * totalProtons) / (totalAmount * M);
	}

	@ZenGetter("neutrons")
	public long getNeutrons() {
		if (element != null)
			return element.getNeutrons();
		if (materialComponents.size() <= 0)
			return Element.Tc.getNeutrons();
		long totalProtons = 0, totalAmount = 0;
		for (MaterialStack material : materialComponents) {
			totalAmount += material.amount;
			totalProtons += material.amount * material.material.getNeutrons();
		}
		return (getDensity() * totalProtons) / (totalAmount * M);
	}

	@ZenGetter("mass")
	public long getMass() {
		if (element != null)
			return element.getMass();
		if (materialComponents.size() <= 0)
			return Element.Tc.getMass();
		long totalProtons = 0, totalAmount = 0;
		for (MaterialStack material : materialComponents) {
			totalAmount += material.amount;
			totalProtons += material.amount * material.material.getMass();
		}
		return (getDensity() * totalProtons) / (totalAmount * M);
	}

	@ZenGetter("density")
	public long getDensity() {
		return M;
	}

	@ZenGetter("camelCaseString")
	public String toCamelCaseString() {
		return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, toString());
	}

	@ZenGetter("unlocalizedName")
	public String getUnlocalizedName() {
	    return "material." + toString();
    }

	@SideOnly(Side.CLIENT)
    @ZenGetter("localizedName")
	public String getLocalizedName() {
		return I18n.format(getUnlocalizedName());
	}

	@Override
    @ZenMethod
	public int compareTo(Material material) {
		return toString().compareTo(material.toString());
	}

    @Override
    @ZenGetter("name")
    public String toString() {
        return MATERIAL_REGISTRY.getNameForObject(this);
    }

    @ZenOperator(OperatorType.MUL)
    public MaterialStack createMaterialStack(long amount) {
	    return new MaterialStack(this, amount);
    }

}