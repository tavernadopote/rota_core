/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.AbstractWidget
 *  net.minecraft.client.gui.components.ContainerObjectSelectionList$Entry
 *  net.minecraft.client.gui.components.EditBox
 *  net.minecraft.client.gui.components.MultiLineEditBox
 *  net.minecraft.client.gui.components.Tooltip
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.narration.NarratableEntry
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.ItemStack
 */
package com.rotacore.client.gui.widget;

import com.rotacore.client.gui.RotaCategoryScreen;
import com.rotacore.client.gui.widget.RotaButton;
import com.rotacore.client.gui.widget.RotaSlider;
import com.rotacore.client.gui.widget.RotaSwitch;
import com.rotacore.config.RotaConfigOption;
import java.util.ArrayList;
import java.util.List;
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

public class RotaConfigEntry
extends ContainerObjectSelectionList.Entry<RotaConfigEntry> {
    private final RotaConfigOption option;
    private final RotaCategoryScreen parentScreen;
    private final List<GuiEventListener> children = new ArrayList<GuiEventListener>();
    private final List<NarratableEntry> narratables = new ArrayList<NarratableEntry>();
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
        int mainBtnWidth = hasSecondary ? btnWidth - 24 : btnWidth;
        int btnHeight = 20;
        switch (option.getType()) {
            case BOOLEAN: {
                RotaSwitch toggle = new RotaSwitch(0, 0, mainBtnWidth, btnHeight, option.getBooleanValue(), option::setBooleanValue);
                this.controlWidget = toggle;
                this.children.add((GuiEventListener)toggle);
                this.narratables.add((NarratableEntry)toggle);
                break;
            }
            case INT_SLIDER: {
                int min = option.getIntMin();
                int max = option.getIntMax();
                double range = Math.max(1, max - min);
                double initialRatio = (double)(option.getIntValue() - min) / range;
                RotaSlider slider = new RotaSlider(0, 0, mainBtnWidth, btnHeight, initialRatio, ratio -> {
                    int current = min + (int)Math.round(ratio * range);
                    return Component.literal((String)option.getIntFormatter().apply(current));
                }, ratio -> {
                    int current = min + (int)Math.round(ratio * range);
                    option.setIntValue(current);
                });
                this.controlWidget = slider;
                this.children.add((GuiEventListener)slider);
                this.narratables.add((NarratableEntry)slider);
                break;
            }
            case DOUBLE_SLIDER: {
                double min = option.getDoubleMin();
                double max = option.getDoubleMax();
                double range = Math.max(1.0E-4, max - min);
                double initialRatio = (option.getDoubleValue() - min) / range;
                RotaSlider slider = new RotaSlider(0, 0, mainBtnWidth, btnHeight, initialRatio, ratio -> {
                    double current = min + ratio * range;
                    return Component.literal((String)option.getDoubleFormatter().apply(current));
                }, ratio -> {
                    double current = min + ratio * range;
                    option.setDoubleValue(current);
                });
                this.controlWidget = slider;
                this.children.add((GuiEventListener)slider);
                this.narratables.add((NarratableEntry)slider);
                break;
            }
            case CYCLE: {
                RotaButton button = new RotaButton(0, 0, mainBtnWidth, btnHeight, (Component)Component.literal((String)option.getCycleText()), btn -> {
                    option.triggerCycle();
                    btn.setMessage((Component)Component.literal((String)option.getCycleText()));
                });
                this.controlWidget = button;
                this.children.add((GuiEventListener)button);
                this.narratables.add((NarratableEntry)button);
                break;
            }
            case ACTION: {
                int actionWidth = mainBtnWidth;
                if (option.getActionIconSupplier() != null) {
                    actionWidth = Math.max(140, actionWidth);
                }
                RotaButton button = new RotaButton(0, 0, actionWidth, btnHeight, (Component)Component.literal((String)option.getActionButtonText()), btn -> option.triggerAction());
                if (option.isDangerAction()) {
                    button.setDanger(true);
                }
                if (option.getActionIconSupplier() != null) {
                    button.setIconSupplier(option.getActionIconSupplier());
                }
                this.controlWidget = button;
                this.children.add((GuiEventListener)button);
                this.narratables.add((NarratableEntry)button);
                break;
            }
            case STRING_INPUT: {
                Minecraft mc = Minecraft.getInstance();
                EditBox editBox = new EditBox(mc != null ? mc.font : null, 0, 0, mainBtnWidth, btnHeight, option.getTitle());
                editBox.setValue(option.getStringValue());
                editBox.setResponder(option::setStringValue);
                this.controlWidget = editBox;
                this.children.add((GuiEventListener)editBox);
                this.narratables.add((NarratableEntry)editBox);
                break;
            }
            case MULTILINE_STRING_INPUT: {
                Minecraft mc = Minecraft.getInstance();
                if (mc != null && mc.font != null) {
                    MultiLineEditBox multiLineBox = new MultiLineEditBox(mc.font, 0, 0, mainBtnWidth, option.getMultilineHeight(), (Component)Component.empty(), option.getTitle());
                    multiLineBox.setValue(option.getStringValue());
                    multiLineBox.setValueListener(option::setStringValue);
                    this.controlWidget = multiLineBox;
                } else {
                    EditBox stubBox = new EditBox(null, 0, 0, mainBtnWidth, option.getMultilineHeight(), option.getTitle());
                    stubBox.setValue(option.getStringValue());
                    stubBox.setResponder(option::setStringValue);
                    this.controlWidget = stubBox;
                }
                this.children.add((GuiEventListener)this.controlWidget);
                this.narratables.add((NarratableEntry)this.controlWidget);
                break;
            }
            case ITEM_ENTRY: {
                if (option.hasReorderActions()) {
                    this.upButton = new RotaButton(0, 0, 16, btnHeight, (Component)Component.literal((String)"\u25b2"), btn -> option.moveUp());
                    this.upButton.setTooltip(Tooltip.create((Component)Component.literal((String)"Mover para cima")));
                    this.children.add((GuiEventListener)this.upButton);
                    this.narratables.add((NarratableEntry)this.upButton);
                    this.downButton = new RotaButton(0, 0, 16, btnHeight, (Component)Component.literal((String)"\u25bc"), btn -> option.moveDown());
                    this.downButton.setTooltip(Tooltip.create((Component)Component.literal((String)"Mover para baixo")));
                    this.children.add((GuiEventListener)this.downButton);
                    this.narratables.add((NarratableEntry)this.downButton);
                }
                if (!option.hasItemToggle()) break;
                this.itemToggle = new RotaSwitch(0, 0, 70, btnHeight, option.isItemActive(), option::setItemActive);
                this.children.add((GuiEventListener)this.itemToggle);
                this.narratables.add((NarratableEntry)this.itemToggle);
                break;
            }
        }
        if (hasSecondary) {
            this.secondaryButton = new RotaButton(0, 0, 20, 20, (Component)Component.literal((String)"\u2699"), btn -> {
                if (option.getSecondaryAction() != null) {
                    option.getSecondaryAction().run();
                }
            });
            if (option.getSecondaryActionTooltip() != null) {
                this.secondaryButton.setTooltip(Tooltip.create((Component)option.getSecondaryActionTooltip()));
            }
            this.children.add((GuiEventListener)this.secondaryButton);
            this.narratables.add((NarratableEntry)this.secondaryButton);
        }
        Minecraft mc = Minecraft.getInstance();
        if (!option.isActive() || mc != null && !option.canPlayerEdit(mc)) {
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
        return this.option;
    }

    public List<? extends GuiEventListener> children() {
        return this.children;
    }

    public List<? extends NarratableEntry> narratables() {
        return this.narratables;
    }

    public void render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isHovered, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        boolean canEdit = this.option.canPlayerEdit(mc);
        if (isHovered && this.option.getType() != RotaConfigOption.Type.HEADER && this.parentScreen != null) {
            this.parentScreen.setFocusedOption(this.option);
        }
        if (this.option.getType() == RotaConfigOption.Type.HEADER) {
            int textY = top + (height - 9) / 2;
            if (mc != null && mc.font != null) {
                graphics.drawString(mc.font, this.option.getTitle(), left + 6, textY, -22016, false);
            }
            int lineY = textY + 11;
            graphics.fill(left + 6, lineY, left + width - 12, lineY + 1, -11184811);
            return;
        }
        if (isHovered) {
            graphics.fill(left, top, left + width - 4, top + height, 0x22FFFFFF);
        }
        int textY = top + (height - 9) / 2;
        int controlY = top + (height - 20) / 2;
        if (this.option.getType() == RotaConfigOption.Type.ITEM_ENTRY) {
            ResourceLocation tex;
            int slotX = left + 4;
            int slotY = top + (height - 18) / 2;
            graphics.fill(slotX, slotY, slotX + 18, slotY + 18, -13158601);
            graphics.renderOutline(slotX, slotY, 18, 18, -11184811);
            if (this.option.hasItemStackIcon()) {
                ItemStack iconStack = this.option.getItemStackIcon();
                if (iconStack != null && !iconStack.isEmpty()) {
                    graphics.renderFakeItem(iconStack, slotX + 1, slotY + 1);
                }
            } else if (this.option.hasTextureIcon() && (tex = this.option.getTextureIcon()) != null) {
                graphics.blit(tex, slotX + 1, slotY + 1, 0.0f, 0.0f, 16, 16, 16, 16);
            }
            int currRight = left + width - 6;
            if (this.secondaryButton != null) {
                this.secondaryButton.active = canEdit;
                this.secondaryButton.setX(currRight -= 20);
                this.secondaryButton.setY(controlY);
                this.secondaryButton.render(graphics, mouseX, mouseY, partialTick);
                currRight -= 4;
            }
            if (this.itemToggle != null) {
                this.itemToggle.active = canEdit;
                this.itemToggle.setX(currRight -= this.itemToggle.getWidth());
                this.itemToggle.setY(controlY);
                this.itemToggle.render(graphics, mouseX, mouseY, partialTick);
                currRight -= 4;
            }
            if (this.downButton != null) {
                this.downButton.active = canEdit;
                this.downButton.setX(currRight -= 16);
                this.downButton.setY(controlY);
                this.downButton.render(graphics, mouseX, mouseY, partialTick);
                currRight -= 2;
            }
            if (this.upButton != null) {
                this.upButton.active = canEdit;
                this.upButton.setX(currRight -= 16);
                this.upButton.setY(controlY);
                this.upButton.render(graphics, mouseX, mouseY, partialTick);
                currRight -= 4;
            }
            int titleX = left + 28;
            int maxTextWidth = Math.max(10, currRight - titleX - 4);
            Object titleStr = this.option.getTitle().getString();
            if (!canEdit) {
                titleStr = "\u00a78[Bloqueado] \u00a77" + (String)titleStr;
            }
            if (mc != null && mc.font != null) {
                if (mc.font.width((String)titleStr) > maxTextWidth) {
                    titleStr = mc.font.plainSubstrByWidth((String)titleStr, maxTextWidth - 8) + "...";
                }
                int titleColor = !canEdit ? -7829368 : (isHovered ? -1 : -2236963);
                graphics.drawString(mc.font, (String)titleStr, titleX, textY, titleColor, false);
            }
            return;
        }
        if (this.option.getType() == RotaConfigOption.Type.MULTILINE_STRING_INPUT) {
            Object titleStr = this.option.getTitle().getString();
            if (!canEdit) {
                titleStr = "\u00a78[Bloqueado] \u00a77" + (String)titleStr;
            }
            if (mc != null && mc.font != null) {
                int titleColor = !canEdit ? -7829368 : -22016;
                graphics.drawString(mc.font, (String)titleStr, left + 6, top + 4, titleColor, false);
            }
            int boxX = left + 6;
            int boxY = top + 16;
            int boxW = width - 16;
            if (this.controlWidget != null) {
                this.controlWidget.active = canEdit;
                this.controlWidget.setX(boxX);
                this.controlWidget.setY(boxY);
                this.controlWidget.setWidth(boxW);
                this.controlWidget.setHeight(this.option.getMultilineHeight());
                this.controlWidget.render(graphics, mouseX, mouseY, partialTick);
            }
            return;
        }
        int totalRightWidth = (this.controlWidget != null ? this.controlWidget.getWidth() : 0) + (this.secondaryButton != null ? this.secondaryButton.getWidth() + 4 : 0);
        int controlX = left + width - totalRightWidth - 8;
        int maxTextWidth = Math.max(10, controlX - (left + 6) - 8);
        Object titleStr = this.option.getTitle().getString();
        if (!canEdit) {
            titleStr = "\u00a78[Bloqueado] \u00a77" + (String)titleStr;
        }
        if (mc != null && mc.font != null) {
            if (mc.font.width((String)titleStr) > maxTextWidth) {
                titleStr = mc.font.plainSubstrByWidth((String)titleStr, maxTextWidth - 8) + "...";
            }
            int titleColor = !canEdit ? -7829368 : (isHovered ? -1 : -2236963);
            graphics.drawString(mc.font, (String)titleStr, left + 6, textY, titleColor, false);
        }
        if (this.controlWidget != null) {
            this.controlWidget.active = canEdit;
            this.controlWidget.setX(controlX);
            this.controlWidget.setY(controlY);
            this.controlWidget.render(graphics, mouseX, mouseY, partialTick);
        }
        if (this.secondaryButton != null) {
            this.secondaryButton.active = canEdit;
            int secondaryX = controlX + (this.controlWidget != null ? this.controlWidget.getWidth() : 0) + 4;
            this.secondaryButton.setX(secondaryX);
            this.secondaryButton.setY(controlY);
            this.secondaryButton.render(graphics, mouseX, mouseY, partialTick);
        }
    }

    public AbstractWidget getControlWidget() {
        return this.controlWidget;
    }

    public RotaButton getSecondaryButton() {
        return this.secondaryButton;
    }

    public RotaButton getUpButton() {
        return this.upButton;
    }

    public RotaButton getDownButton() {
        return this.downButton;
    }

    public RotaSwitch getItemToggle() {
        return this.itemToggle;
    }

    public int getEntryHeight() {
        if (this.option.getType() == RotaConfigOption.Type.MULTILINE_STRING_INPUT) {
            return this.option.getMultilineHeight() + 24;
        }
        return 28;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.option.canPlayerEdit(Minecraft.getInstance())) {
            return false;
        }
        boolean handled = super.mouseClicked(mouseX, mouseY, button);
        if (!handled && this.option.getType() == RotaConfigOption.Type.ITEM_ENTRY && this.option.getActionCallback() != null) {
            this.option.triggerAction();
            return true;
        }
        return handled;
    }

    public boolean charTyped(char codePoint, int modifiers) {
        if (!this.option.canPlayerEdit(Minecraft.getInstance())) {
            return false;
        }
        for (GuiEventListener child : this.children) {
            if (!child.charTyped(codePoint, modifiers)) continue;
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!this.option.canPlayerEdit(Minecraft.getInstance())) {
            return false;
        }
        for (GuiEventListener child : this.children) {
            if (!child.keyPressed(keyCode, scanCode, modifiers)) continue;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}

