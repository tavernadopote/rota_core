package com.rotacore.client.gui.widget;

import com.rotacore.client.gui.RotaCategoryScreen;
import com.rotacore.client.gui.theme.RotaTheme;
import com.rotacore.config.RotaConfigOption;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Linha renderizável na lista de configurações da RotaCategoryScreen.
 */
public class RotaConfigEntry extends ContainerObjectSelectionList.Entry<RotaConfigEntry> {

    private final RotaConfigOption option;
    private final RotaCategoryScreen parentScreen;
    private final List<GuiEventListener> children = new ArrayList<>();
    private final List<NarratableEntry> narratables = new ArrayList<>();

    private AbstractWidget controlWidget;
    private RotaButton secondaryButton;
    private RotaButton upButton;
    private RotaButton downButton;
    private RotaSwitch itemToggle;

    public RotaConfigEntry(RotaConfigOption option, RotaCategoryScreen parentScreen, int controlWidth) {
        this.option = option;
        this.parentScreen = parentScreen;

        int btnWidth = Math.max(120, controlWidth);
        boolean hasSecondary = option.hasSecondaryAction();
        int mainBtnWidth = hasSecondary ? (btnWidth - 24) : btnWidth;
        int btnHeight = 20;

        switch (option.getType()) {
            case BOOLEAN -> {
                RotaSwitch toggle = new RotaSwitch(
                        0, 0, mainBtnWidth, btnHeight,
                        option.getBooleanValue(),
                        option::setBooleanValue
                );
                this.controlWidget = toggle;
                this.children.add(toggle);
                this.narratables.add(toggle);
            }
            case INT_SLIDER -> {
                int min = option.getIntMin();
                int max = option.getIntMax();
                double range = Math.max(1, max - min);
                double initialRatio = (option.getIntValue() - min) / range;

                RotaSlider slider = new RotaSlider(
                        0, 0, mainBtnWidth, btnHeight,
                        initialRatio,
                        ratio -> {
                            int current = min + (int) Math.round(ratio * range);
                            return Component.literal(option.getIntFormatter().apply(current));
                        },
                        ratio -> {
                            int current = min + (int) Math.round(ratio * range);
                            option.setIntValue(current);
                        }
                );
                this.controlWidget = slider;
                this.children.add(slider);
                this.narratables.add(slider);
            }
            case DOUBLE_SLIDER -> {
                double min = option.getDoubleMin();
                double max = option.getDoubleMax();
                double range = Math.max(0.0001, max - min);
                double initialRatio = (option.getDoubleValue() - min) / range;

                RotaSlider slider = new RotaSlider(
                        0, 0, mainBtnWidth, btnHeight,
                        initialRatio,
                        ratio -> {
                            double current = min + ratio * range;
                            return Component.literal(option.getDoubleFormatter().apply(current));
                        },
                        ratio -> {
                            double current = min + ratio * range;
                            option.setDoubleValue(current);
                        }
                );
                this.controlWidget = slider;
                this.children.add(slider);
                this.narratables.add(slider);
            }
            case CYCLE -> {
                RotaButton button = new RotaButton(
                        0, 0, mainBtnWidth, btnHeight,
                        Component.literal(option.getCycleText()),
                        btn -> {
                            option.triggerCycle();
                            btn.setMessage(Component.literal(option.getCycleText()));
                        }
                );
                this.controlWidget = button;
                this.children.add(button);
                this.narratables.add(button);
            }
            case ACTION -> {
                int actionWidth = mainBtnWidth;
                if (option.getActionIconSupplier() != null) {
                    actionWidth = Math.max(140, actionWidth);
                }
                RotaButton button = new RotaButton(
                        0, 0, actionWidth, btnHeight,
                        Component.literal(option.getActionButtonText()),
                        btn -> option.triggerAction()
                );
                if (option.isDangerAction()) {
                    button.setDanger(true);
                }
                if (option.getActionIconSupplier() != null) {
                    button.setIconSupplier(option.getActionIconSupplier());
                }
                this.controlWidget = button;
                this.children.add(button);
                this.narratables.add(button);
            }
            case STRING_INPUT -> {
                Minecraft mc = Minecraft.getInstance();
                EditBox editBox = new EditBox(
                        mc != null ? mc.font : null,
                        0, 0, mainBtnWidth, btnHeight,
                        option.getTitle()
                );
                editBox.setValue(option.getStringValue());
                editBox.setResponder(option::setStringValue);
                this.controlWidget = editBox;
                this.children.add(editBox);
                this.narratables.add(editBox);
            }
            case MULTILINE_STRING_INPUT -> {
                Minecraft mc = Minecraft.getInstance();
                if (mc != null && mc.font != null) {
                    MultiLineEditBox multiLineBox = new MultiLineEditBox(
                            mc.font,
                            0, 0,
                            mainBtnWidth, option.getMultilineHeight(),
                            Component.empty(),
                            option.getTitle()
                    );
                    multiLineBox.setValue(option.getStringValue());
                    multiLineBox.setValueListener(option::setStringValue);
                    this.controlWidget = multiLineBox;
                } else {
                    EditBox stubBox = new EditBox(
                            null,
                            0, 0,
                            mainBtnWidth, option.getMultilineHeight(),
                            option.getTitle()
                    );
                    stubBox.setValue(option.getStringValue());
                    stubBox.setResponder(option::setStringValue);
                    this.controlWidget = stubBox;
                }
                this.children.add(this.controlWidget);
                this.narratables.add(this.controlWidget);
            }
            case ITEM_ENTRY -> {
                if (option.hasReorderActions()) {
                    this.upButton = new RotaButton(
                            0, 0, 16, btnHeight,
                            Component.literal("▲"),
                            btn -> option.moveUp()
                    );
                    this.upButton.setTooltip(Tooltip.create(Component.literal("Mover para cima")));
                    this.children.add(this.upButton);
                    this.narratables.add(this.upButton);

                    this.downButton = new RotaButton(
                            0, 0, 16, btnHeight,
                            Component.literal("▼"),
                            btn -> option.moveDown()
                    );
                    this.downButton.setTooltip(Tooltip.create(Component.literal("Mover para baixo")));
                    this.children.add(this.downButton);
                    this.narratables.add(this.downButton);
                }

                if (option.hasItemToggle()) {
                    this.itemToggle = new RotaSwitch(
                            0, 0, 70, btnHeight,
                            option.isItemActive(),
                            option::setItemActive
                    );
                    this.children.add(this.itemToggle);
                    this.narratables.add(this.itemToggle);
                }
            }
            case HEADER -> {
                // Header não possui controle interativo
            }
        }

        if (hasSecondary) {
            this.secondaryButton = new RotaButton(
                    0, 0, 20, 20,
                    Component.literal("⚙"),
                    btn -> {
                        if (option.getSecondaryAction() != null) {
                            option.getSecondaryAction().run();
                        }
                    }
            );
            if (option.getSecondaryActionTooltip() != null) {
                this.secondaryButton.setTooltip(Tooltip.create(option.getSecondaryActionTooltip()));
            }
            this.children.add(this.secondaryButton);
            this.narratables.add(this.secondaryButton);
        }

        Minecraft mc = Minecraft.getInstance();
        if (!option.isActive() || (mc != null && !option.canPlayerEdit(mc))) {
            if (this.controlWidget != null) {
                this.controlWidget.active = false;
            }
            if (this.secondaryButton != null) {
                this.secondaryButton.active = false;
            }
            if (this.upButton != null) {
                this.upButton.active = false;
            }
            if (this.downButton != null) {
                this.downButton.active = false;
            }
            if (this.itemToggle != null) {
                this.itemToggle.active = false;
            }
        }
    }

    public RotaConfigOption getOption() {
        return option;
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return children;
    }

    @Override
    public List<? extends NarratableEntry> narratables() {
        return narratables;
    }

    @Override
    public void render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isHovered, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        boolean canEdit = option.canPlayerEdit(mc);

        // Notifica o painel lateral quando o mouse estiver sobre o item
        if (isHovered && option.getType() != RotaConfigOption.Type.HEADER) {
            if (parentScreen != null) {
                parentScreen.setFocusedOption(option);
            }
        }

        if (option.getType() == RotaConfigOption.Type.HEADER) {
            int textY = top + (height - 9) / 2;
            if (mc != null && mc.font != null) {
                graphics.drawString(mc.font, option.getTitle(), left + 6, textY, 0xFFFFAA00, false);
            }
            int lineY = textY + 11;
            graphics.fill(left + 6, lineY, left + width - 12, lineY + 1, 0xFF555555);
            return;
        }

        // Destaque suave ao passar o cursor
        if (isHovered) {
            graphics.fill(left, top, left + width - 4, top + height, 0x22FFFFFF);
        }

        int textY = top + (height - 9) / 2;
        int controlY = top + (height - 20) / 2;

        if (option.getType() == RotaConfigOption.Type.ITEM_ENTRY) {
            // Slot do item à esquerda (18x18px)
            int slotX = left + 4;
            int slotY = top + (height - 18) / 2;
            graphics.fill(slotX, slotY, slotX + 18, slotY + 18, 0xFF373737);
            graphics.renderOutline(slotX, slotY, 18, 18, 0xFF555555);

            if (option.hasItemStackIcon()) {
                ItemStack iconStack = option.getItemStackIcon();
                if (iconStack != null && !iconStack.isEmpty()) {
                    graphics.renderFakeItem(iconStack, slotX + 1, slotY + 1);
                }
            } else if (option.hasTextureIcon()) {
                ResourceLocation tex = option.getTextureIcon();
                if (tex != null) {
                    graphics.blit(tex, slotX + 1, slotY + 1, 0, 0, 16, 16, 16, 16);
                }
            }

            // Controles à direita: [ ▲ ] [ ▼ ] [ ATIVADO / DESATIVADO ] [ ⚙ ]
            int currRight = left + width - 6;

            if (secondaryButton != null) {
                currRight -= 20;
                secondaryButton.active = canEdit;
                secondaryButton.setX(currRight);
                secondaryButton.setY(controlY);
                secondaryButton.render(graphics, mouseX, mouseY, partialTick);
                currRight -= 4;
            }

            if (itemToggle != null) {
                currRight -= itemToggle.getWidth();
                itemToggle.active = canEdit;
                itemToggle.setX(currRight);
                itemToggle.setY(controlY);
                itemToggle.render(graphics, mouseX, mouseY, partialTick);
                currRight -= 4;
            }

            if (downButton != null) {
                currRight -= 16;
                downButton.active = canEdit;
                downButton.setX(currRight);
                downButton.setY(controlY);
                downButton.render(graphics, mouseX, mouseY, partialTick);
                currRight -= 2;
            }

            if (upButton != null) {
                currRight -= 16;
                upButton.active = canEdit;
                upButton.setX(currRight);
                upButton.setY(controlY);
                upButton.render(graphics, mouseX, mouseY, partialTick);
                currRight -= 4;
            }

            // Título após o slot (left + 28)
            int titleX = left + 28;
            int maxTextWidth = Math.max(10, currRight - titleX - 4);
            String titleStr = option.getTitle().getString();
            if (!canEdit) {
                titleStr = "§8[Bloqueado] §7" + titleStr;
            }
            if (mc != null && mc.font != null) {
                if (mc.font.width(titleStr) > maxTextWidth) {
                    titleStr = mc.font.plainSubstrByWidth(titleStr, maxTextWidth - 8) + "...";
                }
                int titleColor = !canEdit ? 0xFF888888 : (isHovered ? 0xFFFFFFFF : 0xFFDDDDDD);
                graphics.drawString(mc.font, titleStr, titleX, textY, titleColor, false);
            }
            return;
        }

        if (option.getType() == RotaConfigOption.Type.MULTILINE_STRING_INPUT) {
            // Título no topo da linha
            String titleStr = option.getTitle().getString();
            if (!canEdit) {
                titleStr = "§8[Bloqueado] §7" + titleStr;
            }
            if (mc != null && mc.font != null) {
                int titleColor = !canEdit ? 0xFF888888 : 0xFFFFAA00;
                graphics.drawString(mc.font, titleStr, left + 6, top + 4, titleColor, false);
            }

            // MultiLineEditBox cobrindo a largura total útil
            int boxX = left + 6;
            int boxY = top + 16;
            int boxW = width - 16;
            if (controlWidget != null) {
                controlWidget.active = canEdit;
                controlWidget.setX(boxX);
                controlWidget.setY(boxY);
                controlWidget.setWidth(boxW);
                controlWidget.setHeight(option.getMultilineHeight());
                controlWidget.render(graphics, mouseX, mouseY, partialTick);
            }
            return;
        }

        // Renderiza o controle posicionado à direita
        int totalRightWidth = (controlWidget != null ? controlWidget.getWidth() : 0)
                + (secondaryButton != null ? (secondaryButton.getWidth() + 4) : 0);
        int controlX = left + width - totalRightWidth - 8;

        // Título da opção
        int maxTextWidth = Math.max(10, controlX - (left + 6) - 8);
        String titleStr = option.getTitle().getString();
        if (!canEdit) {
            titleStr = "§8[Bloqueado] §7" + titleStr;
        }
        if (mc != null && mc.font != null) {
            if (mc.font.width(titleStr) > maxTextWidth) {
                titleStr = mc.font.plainSubstrByWidth(titleStr, maxTextWidth - 8) + "...";
            }
            int titleColor = !canEdit ? 0xFF888888 : (isHovered ? 0xFFFFFFFF : 0xFFDDDDDD);
            graphics.drawString(mc.font, titleStr, left + 6, textY, titleColor, false);
        }

        if (controlWidget != null) {
            controlWidget.active = canEdit;
            controlWidget.setX(controlX);
            controlWidget.setY(controlY);
            controlWidget.render(graphics, mouseX, mouseY, partialTick);
        }

        if (secondaryButton != null) {
            secondaryButton.active = canEdit;
            int secondaryX = controlX + (controlWidget != null ? controlWidget.getWidth() : 0) + 4;
            secondaryButton.setX(secondaryX);
            secondaryButton.setY(controlY);
            secondaryButton.render(graphics, mouseX, mouseY, partialTick);
        }
    }

    public AbstractWidget getControlWidget() {
        return controlWidget;
    }

    public RotaButton getSecondaryButton() {
        return secondaryButton;
    }

    public RotaButton getUpButton() {
        return upButton;
    }

    public RotaButton getDownButton() {
        return downButton;
    }

    public RotaSwitch getItemToggle() {
        return itemToggle;
    }

    public int getEntryHeight() {
        if (option.getType() == RotaConfigOption.Type.MULTILINE_STRING_INPUT) {
            return option.getMultilineHeight() + 24;
        }
        return 28;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!option.canPlayerEdit(Minecraft.getInstance())) {
            return false;
        }
        boolean handled = super.mouseClicked(mouseX, mouseY, button);
        if (!handled && option.getType() == RotaConfigOption.Type.ITEM_ENTRY && option.getActionCallback() != null) {
            option.triggerAction();
            return true;
        }
        return handled;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (!option.canPlayerEdit(Minecraft.getInstance())) {
            return false;
        }
        for (GuiEventListener child : children) {
            if (child.charTyped(codePoint, modifiers)) {
                return true;
            }
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!option.canPlayerEdit(Minecraft.getInstance())) {
            return false;
        }
        for (GuiEventListener child : children) {
            if (child.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
