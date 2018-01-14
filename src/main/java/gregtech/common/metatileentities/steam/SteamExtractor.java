package gregtech.common.metatileentities.steam;

import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.metatileentity.*;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.util.GTResourceLocation;
import gregtech.api.util.GTUtility;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

public class SteamExtractor extends WorkableSteamMetaTileEntity<Recipe> {

    public SteamExtractor(IMetaTileEntityFactory factory, RecipeMap<Recipe, ?> recipeMap) {
        super(factory, recipeMap);
    }

    @Override
    public boolean onScrewdriverRightClick(EnumFacing side, EntityPlayer player, EnumHand hand, float clickX, float clickY, float clickZ) {
        return false;
    }

    @Override
    public boolean onWrenchRightClick(EnumFacing side, EnumFacing wrenchingSide, EntityPlayer player, EnumHand hand, float clickX, float clickY, float clickZ) {
        return false;
    }

    @Override
    public IItemHandlerModifiable createImportItemHandler() {
        return new ItemStackHandler(1);
    }

    @Override
    public IItemHandlerModifiable createExportItemHandler() {
        return new ItemStackHandler(1);
    }

    @Override
    public ModularUI<? extends IMetaTileEntity> createUI(EntityPlayer player) {
        GTResourceLocation slotImageLocation = new GTResourceLocation("textures/gui/bronze/slot_bronze.png");
        return ModularUI.<SteamExtractor>builder(new GTResourceLocation("textures/gui/bronze/bronze_gui.png"), 176, 166)
            .widget(0, new LabelWidget<>(6, 6, GTUtility.sided(() -> I18n.format(this.factory.getUnlocalizedName()), this.factory::getUnlocalizedName)))
            .widget(1, new SlotWidget<SteamExtractor>(this.importItems, 0, 53, 25)
                .setImageLocation(slotImageLocation)
                .setBackgroundLocation(new GTResourceLocation("textures/gui/bronze/slot_bronze_bronze_extractor_background.png"))
                .setOnSlotChanged(this::markDirty))
            .widget(2, new ProgressWidget<SteamExtractor>(78, 24, false,false)
                .setImageLocation(new GTResourceLocation("textures/gui/bronze/progress_bar_bronze_extractor.png"))
                .setFilledImageLocation(new GTResourceLocation("textures/gui/bronze/progress_bar_bronze_extractor_filled.png"))
                .setImageWidthHeight(20,18)
                .setImageUV(0,0))
            .widget(3, new SlotWidget<SteamExtractor>(this.exportItems, 0, 107, 25, true, false)
                .setImageLocation(slotImageLocation)
                .setOnSlotChanged(this::markDirty))
            .widget(4, new LabelWidget<>(8, 166 - 96 + 2, player.inventory.getDisplayName().getUnformattedText())) // 166 - gui height, 96 + 2 - from vanilla code
            .bindPlayerInventory(player.inventory, 5, slotImageLocation)
            .build(this, player);
    }

    @Override
    public boolean onRightClick(EnumFacing side, EntityPlayer player, EnumHand hand, float clickX, float clickY, float clickZ) {
        if (player instanceof EntityPlayerMP) {
            MetaTileEntityUIFactory.INSTANCE.openUI(this, (EntityPlayerMP) player);
        }
        return true;
    }
}
