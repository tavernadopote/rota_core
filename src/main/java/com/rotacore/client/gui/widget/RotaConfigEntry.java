package com.rotacore.client.gui.widget;

import com.rotacore.client.gui.RotaCategoryScreen;
import com.rotacore.client.gui.theme.RotaTheme;
import com.rotacore.config.RotaConfigOption;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;

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

    public RotaConfigEntry(RotaConfigOption option, RotaCategoryScreen parentScreen, int controlWidth) {
        this.option = option;
        this.parentScreen = parentScreen;

        int btnWidth = Math.max(120, controlWidth);
        int btnHeight = 20;

        switch (option.getType()) {
            case BOOLEAN -> {
                RotaSwitch toggle = new RotaSwitch(
                        0, 0, btnWidth, btnHeight,
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
                        0, 0, btnWidth, btnHeight,
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
                        0, 0, btnWidth, btnHeight,
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
                        0, 0, btnWidth, btnHeight,
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
                RotaButton button = new RotaButton(
                        0, 0, btnWidth, btnHeight,
                        Component.literal(option.getActionButtonText()),
                        btn -> option.triggerAction()
                );
                this.controlWidget = button;
                this.children.add(button);
                this.narratables.add(button);
            }
            case HEADER -> {
                // Header não possui controle interativo
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

        // Notifica o painel lateral quando o mouse estiver sobre o item
        if (isHovered && option.getType() != RotaConfigOption.Type.HEADER) {
            parentScreen.setFocusedOption(option);
        }

        if (option.getType() == RotaConfigOption.Type.HEADER) {
            int textY = top + (height - 9) / 2;
            graphics.drawString(mc.font, option.getTitle(), left + 6, textY, RotaTheme.TEXT_TITLE, false);
            int lineY = textY + 11;
            graphics.fill(left + 6, lineY, left + width - 12, lineY + 1, RotaTheme.BORDER_SECONDARY);
            return;
        }

        // Destaque suave ao passar o cursor
        if (isHovered) {
            graphics.fill(left, top, left + width - 4, top + height, 0x1EFFFFFF);
        }

        // Título da opção
        int textY = top + (height - 9) / 2;
        int maxTextWidth = width - 140;
        String titleStr = option.getTitle().getString();
        if (mc.font.width(titleStr) > maxTextWidth) {
            titleStr = mc.font.plainSubstrByWidth(titleStr, maxTextWidth - 8) + "...";
        }
        graphics.drawString(mc.font, titleStr, left + 6, textY, isHovered ? 0xFFFFFFFF : RotaTheme.TEXT_BODY, false);

        // Renderiza o controle posicionado à direita
        int controlX = left + width - 130;
        int controlY = top + (height - 20) / 2;

        if (controlWidget != null) {
            controlWidget.setX(controlX);
            controlWidget.setY(controlY);
            controlWidget.render(graphics, mouseX, mouseY, partialTick);
        }
    }
}
