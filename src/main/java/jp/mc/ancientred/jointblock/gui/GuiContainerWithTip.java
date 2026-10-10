package jp.mc.ancientred.jointblock.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * 1.12.2 {@code GuiContainerWithTip} (a {@code GuiContainer} that additionally rendered the
 * hovered tooltip every frame).  1.20.1's {@link AbstractContainerScreen} defines
 * {@code renderTooltip} but never calls it (vanilla screens such as {@code InventoryScreen} call it
 * from their own {@code render} override), so {@link #render} below keeps calling it exactly as the
 * 1.12.2 class did; the class otherwise only carries the shared type/menu plumbing.
 */
@OnlyIn(Dist.CLIENT)
public abstract class GuiContainerWithTip<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {

    public GuiContainerWithTip(T menu, Inventory playerInventory) {
        super(menu, playerInventory, Component.empty());
        // 1.12.2 set field_146291_p (allowUserInput) = false here; that flag no longer exists in
        // 1.20.1 (screen input is always dispatched to the widgets first).
    }

    /**
     * 1.12.2 {@code GuiScreen.actionPerformed(GuiButton)} hook.  The 1.20.1 widgets report a press
     * through {@code IJBGuiWidget} (see {@code GuiButtonJB}); subclasses override this.
     */
    protected void actionPerformed(jp.mc.ancientred.jointblock.gui.widget.IJBGuiWidget par1GuiButton) {
    }

    /**
     * 1.12.2 {@code GuiContainer.func_73863_a} drew the default in-world background
     * ({@code func_146276_q_()} = the {@code -1072689136 -> -804253680} gradient) before
     * {@code drawGuiContainerBackgroundLayer}.  1.20.1 {@code AbstractContainerScreen.render} no
     * longer calls {@code renderBackground}, so the closest equivalent is to do exactly what vanilla
     * 1.20.1 container screens do: call {@link #renderBackground(GuiGraphics)} first and then the
     * standard container draw.
     */
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        // 1.12.2 GuiContainerWithTip.func_73863_a ended with func_191948_b (renderHoveredToolTip).
        // 1.20.1 needs the same call: AbstractContainerScreen defines renderTooltip but no vanilla
        // path invokes it for a subclass (InventoryScreen and friends call it in their own render).
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    /**
     * 1.12.2 {@code drawGuiContainerBackgroundLayer}.  Abstract in 1.20.1 exactly as it was in
     * 1.12.2 ({@code GuiContainerWithTip} never implemented it either); every concrete screen draws
     * its own background texture.
     */
    @Override
    protected abstract void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY);
}
