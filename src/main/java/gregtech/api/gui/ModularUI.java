package gregtech.api.gui;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableBiMap;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.*;
import gregtech.api.gui.widgets.ProgressWidget.MoveType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;

import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

/**
 * ModularUI is user-interface implementation concrete, based on widgets system
 * Each widget acts unique and manage different things
 * All widget information is synced to client from server for correct rendering
 * Widgets and UI are both-sided, so widgets should equal on both sides
 * However widget data will sync, widgets themself, background, sizes and other important info will not
 * To open and create ModularUI, see {@link UIFactory}
 *
 */
public final class ModularUI implements SizeProvider {

    public final ImmutableBiMap<Integer, Widget> guiWidgets;

    public final TextureArea backgroundPath;
    private int screenWidth, screenHeight;
    private final int width, height;

    public boolean isJEIHandled;

    /**
     * UIHolder of this modular UI
     */
    public final IUIHolder holder;
    public final EntityPlayer entityPlayer;

    public ModularUI(ImmutableBiMap<Integer, Widget> guiWidgets, TextureArea backgroundPath, int width, int height, IUIHolder holder, EntityPlayer entityPlayer) {
        this.guiWidgets = guiWidgets;
        this.backgroundPath = backgroundPath;
        this.width = width;
        this.height = height;
        this.holder = holder;
        this.entityPlayer = entityPlayer;
    }

    public void updateScreenSize(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    public void initWidgets() {
        guiWidgets.values().forEach(widget -> {
            widget.setGui(this);
            widget.setSizes(this);
            widget.initWidget();
        });
    }

    public static Builder defaultBuilder() {
        return new Builder(GuiTextures.BACKGROUND, 176, 166);
    }

    public static Builder borderedBuilder() {
        return new Builder(GuiTextures.BORDERED_BACKGROUND, 195, 136);
    }

    public static Builder extendedBuilder() {
        return new Builder(GuiTextures.BACKGROUND_EXTENDED, 176, 216);
    }

    public static Builder builder(TextureArea background, int width, int height) {
        return new Builder(background, width, height);
    }

    @Override
    public int getScreenWidth() {
        return screenWidth;
    }

    @Override
    public int getScreenHeight() {
        return screenHeight;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    /**
     * Simple builder for  ModularUI objects
     */
    public static class Builder {

        private ImmutableBiMap.Builder<Integer, Widget> widgets = ImmutableBiMap.builder();
        private TextureArea background;
        private int width, height;
        private int nextFreeWidgetId = 0;

        public Builder(TextureArea background, int width, int height) {
            Preconditions.checkNotNull(background);
            this.background = background;
            this.width = width;
            this.height = height;
        }

        public Builder widget(Widget widget) {
            Preconditions.checkNotNull(widget);
            widgets.put(nextFreeWidgetId++, widget);
            return this;
        }

        public Builder label(int x, int y, String localizationKey) {
            return widget(new LabelWidget(x, y, localizationKey));
        }

        public Builder label(int x, int y, String localizationKey, int color) {
            return widget(new LabelWidget(x, y, localizationKey, color, new Object[0]));
        }

        public Builder image(int x, int y, int width, int height, TextureArea area) {
            return widget(new ImageWidget(x, y, width, height, area));
        }

        public Builder dynamicLabel(int x, int y, Supplier<String> text, int color) {
            return widget(new DynamicLabelWidget(x, y, text, color));
        }

        public Builder slot(IItemHandlerModifiable itemHandler, int slotIndex, int x, int y, TextureArea... overlays) {
            return widget(new SlotWidget(itemHandler, slotIndex, x, y).setBackgroundTexture(overlays));
        }

        public Builder progressBar(DoubleSupplier progressSupplier, int x, int y, int width, int height, TextureArea texture, MoveType moveType) {
            return widget(new ProgressWidget(progressSupplier, x, y, width, height, texture, moveType));
        }

        public Builder squareOfSlots(IItemHandlerModifiable itemHandler, int startIndex, int size, boolean allowTakeStack, boolean allowPutStack, TextureArea... backgrounds) {
            int sizeSqrt = (int) Math.sqrt(size);
            return groupOfSlots(itemHandler, startIndex, sizeSqrt, sizeSqrt, allowTakeStack, allowPutStack, backgrounds);
        }

        public Builder groupOfSlots(IItemHandlerModifiable itemHandler, int startIndex, int width, int height, boolean allowTakeStack, boolean allowPutStack, TextureArea... backgrounds) {
            int startX = 88 - (width * 9);
            int startY = 45 - (height * 9);
            for(int x = 0; x < width; x++) {
                for(int y = 0; y < height; y++) {
                    this.widget(new SlotWidget(itemHandler, startIndex + height * y + x,
                        startX + 18 * x, startY + 18 * y,
                        allowTakeStack, allowPutStack).setBackgroundTexture(backgrounds));
                }
            }
            return this;
        }

        public Builder bindPlayerInventory(InventoryPlayer inventoryPlayer) {
            bindPlayerInventory(inventoryPlayer, GuiTextures.SLOT);
            return this;
        }

        public Builder bindPlayerInventory(InventoryPlayer inventoryPlayer, int startY) {
            bindPlayerInventory(inventoryPlayer, GuiTextures.SLOT, 8, startY);
            return this;
        }

        public Builder bindPlayerInventory(InventoryPlayer inventoryPlayer, TextureArea imageLocation) {
            return bindPlayerInventory(inventoryPlayer, imageLocation, 8, 84);
        }

        public Builder bindPlayerInventory(InventoryPlayer inventoryPlayer, TextureArea imageLocation, int x, int y) {
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 9; col++) {
                    this.widget(new SlotWidget(new PlayerMainInvWrapper(inventoryPlayer), col + (row + 1) * 9, x + col * 18, y + row * 18)
                        .setBackgroundTexture(imageLocation)
                        .markAsPlayerInventory());
                }
            }
            return bindPlayerHotbar(inventoryPlayer, imageLocation, x, y + 58);
        }

        public Builder bindPlayerHotbar(InventoryPlayer inventoryPlayer, TextureArea imageLocation, int x, int y) {
            for (int slot = 0; slot < 9; slot++) {
                this.widget(new SlotWidget(new PlayerMainInvWrapper(inventoryPlayer), slot, x + slot * 18, y)
                    .setBackgroundTexture(imageLocation)
                    .markAsPlayerInventory());
            }
            return this;
        }

        public ModularUI build(IUIHolder holder, EntityPlayer player) {
            return new ModularUI(widgets.build(), background, width, height, holder, player);
        }

    }

}
