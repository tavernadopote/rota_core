/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.EditBox
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.util.FormattedCharSequence
 */
package com.rotacore.client.gui;

import com.rotacore.api.RotaCategory;
import com.rotacore.client.gui.widget.RotaButton;
import com.rotacore.client.gui.widget.RotaConfigListWidget;
import com.rotacore.config.RotaConfigOption;
import com.rotacore.config.RotaIconPreview;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;

public class RotaCategoryScreen
extends Screen {
    private final Screen parentHub;
    private final RotaCategory category;
    private final Runnable onSaveAction;
    private RotaConfigListWidget listWidget;
    private EditBox searchBox;
    private RotaConfigOption focusedOption;
    private double inspectorScrollAmount = 0.0;

    public RotaCategoryScreen(Screen parentHub, RotaCategory category) {
        this(parentHub, category, null);
    }

    public RotaCategoryScreen(Screen parentHub, RotaCategory category, Runnable onSaveAction) {
        super(category.getTitle());
        this.parentHub = parentHub;
        this.category = category;
        this.onSaveAction = onSaveAction;
    }

    public Screen getParentHub() {
        return this.parentHub;
    }

    public RotaConfigOption getFocusedOption() {
        return this.focusedOption;
    }

    public double getInspectorScrollAmount() {
        return this.inspectorScrollAmount;
    }

    public void setInspectorScrollAmount(double scrollAmount) {
        this.inspectorScrollAmount = Math.max(0.0, scrollAmount);
    }

    public void setFocusedOption(RotaConfigOption option) {
        if (option != null && option.getType() != RotaConfigOption.Type.HEADER) {
            if (this.focusedOption != option) {
                this.inspectorScrollAmount = 0.0;
            }
            this.focusedOption = option;
        }
    }

    public static double calculateMaxScroll(int totalContentHeight, int visibleHeight) {
        return Math.max(0.0, (double)(totalContentHeight - visibleHeight));
    }

    public static double clampScroll(double scrollAmount, double maxScroll) {
        return Math.max(0.0, Math.min(maxScroll, scrollAmount));
    }

    public static double applyScrollDelta(double currentScroll, double scrollY, double maxScroll) {
        return Math.max(0.0, Math.min(maxScroll, currentScroll - scrollY * 16.0));
    }

    public static boolean shouldRenderTip(RotaConfigOption option) {
        if (option == null || !option.hasTip()) {
            return false;
        }
        return !option.hasTexturePreview() || option.isTipExplicitlyForced();
    }

    public static boolean shouldRenderDefaultValue(RotaConfigOption option) {
        if (option == null) {
            return false;
        }
        if (option.hasTexturePreview()) {
            return false;
        }
        if (!option.shouldShowDefaultValue()) {
            return false;
        }
        String defaultVal = option.getDefaultValueText();
        return defaultVal != null && !defaultVal.trim().isEmpty() && !defaultVal.equals("\u2014");
    }

    protected void init() {
        int listWidth = (int)((double)this.width * 0.65);
        int topY = 36;
        int bottomHeight = 36;
        int listHeight = this.height - topY - bottomHeight;
        double savedScroll = this.listWidget != null ? this.listWidget.getScrollAmount() : 0.0;
        this.listWidget = new RotaConfigListWidget(this.minecraft, listWidth, listHeight, topY, 28, this);
        List<RotaConfigOption> options = this.category.getOptions();
        this.listWidget.setOptions(options);
        if (savedScroll > 0.0) {
            this.listWidget.setScrollAmount(savedScroll);
        }
        if (!options.isEmpty()) {
            for (RotaConfigOption opt : options) {
                if (opt.getType() == RotaConfigOption.Type.HEADER) continue;
                this.focusedOption = opt;
                this.inspectorScrollAmount = 0.0;
                break;
            }
        }
        this.addRenderableWidget((GuiEventListener)this.listWidget);
        this.addRenderableWidget((GuiEventListener)new RotaButton(12, 8, 85, 20, (Component)Component.literal((String)"\u2190 Voltar"), btn -> this.saveAndClose()));
        this.addRenderableWidget((GuiEventListener)new RotaButton(12, this.height - 28, 130, 20, (Component)Component.literal((String)"Restaurar Padr\u00f5es"), btn -> {
            this.category.resetDefaults();
            this.listWidget.setOptions(this.category.getOptions());
        }));
        this.addRenderableWidget((GuiEventListener)new RotaButton(150, this.height - 28, 120, 20, (Component)Component.literal((String)"Salvar Altera\u00e7\u00f5es"), btn -> this.saveAndClose()));
        int inspectorX = listWidth + 8;
        int inspectorWidth = this.width - inspectorX - 10;
        this.searchBox = new EditBox(this.font, inspectorX, this.height - 28, inspectorWidth, 20, (Component)Component.literal((String)"Buscar..."));
        this.searchBox.setHint((Component)Component.literal((String)"\u00a78Buscar op\u00e7\u00e3o..."));
        this.searchBox.setResponder(query -> this.listWidget.setFilter((String)query));
        this.addRenderableWidget((GuiEventListener)this.searchBox);
    }

    private void saveAndClose() {
        if (this.onSaveAction != null) {
            this.onSaveAction.run();
        }
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parentHub);
        }
    }

    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (this.minecraft != null && this.minecraft.level != null) {
            this.renderTransparentBackground(graphics);
        } else {
            this.renderMenuBackground(graphics);
        }
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int listWidth = (int)((double)this.width * 0.65);
        int topY = 36;
        int bottomY = this.height - 36;
        graphics.fill(0, 0, this.width, topY, -267382768);
        graphics.fill(0, bottomY, this.width, this.height, -267382768);
        graphics.fill(0, topY - 1, this.width, topY, -13421773);
        graphics.fill(0, bottomY, this.width, bottomY + 1, -13421773);
        graphics.fill(8, topY, listWidth, bottomY, -300674028);
        graphics.renderOutline(8, topY, listWidth - 8, bottomY - topY, -14013910);
        super.render(graphics, mouseX, mouseY, partialTick);
        int titleX = 105;
        int maxTitleWidth = listWidth - titleX - 8;
        Object titleStr = this.category.getTitle().getString();
        if (this.font.width((String)titleStr) > maxTitleWidth) {
            titleStr = this.font.plainSubstrByWidth((String)titleStr, maxTitleWidth - 8) + "...";
        }
        graphics.drawString(this.font, (String)titleStr, titleX, 14, -22016, false);
        graphics.fill(listWidth + 2, topY, listWidth + 3, bottomY, -12303292);
        this.renderInspectorPanel(graphics, listWidth + 8, topY, this.width - listWidth - 18, bottomY - topY, mouseX, mouseY, partialTick);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int visibleHeight;
        int innerWidth;
        int totalContentHeight;
        double maxScroll;
        int listWidth = (int)((double)this.width * 0.65);
        int inspectorX = listWidth + 8;
        int topY = 36;
        int bottomY = this.height - 36;
        int inspectorWidth = this.width - inspectorX - 10;
        int inspectorHeight = bottomY - topY;
        if (mouseX >= (double)inspectorX && mouseX <= (double)(inspectorX + inspectorWidth) && mouseY >= (double)topY && mouseY <= (double)bottomY && this.focusedOption != null && (maxScroll = RotaCategoryScreen.calculateMaxScroll(totalContentHeight = this.calculateInspectorContentHeight(this.focusedOption, innerWidth = inspectorWidth - 24), visibleHeight = inspectorHeight - 20)) > 0.0) {
            this.inspectorScrollAmount = RotaCategoryScreen.applyScrollDelta(this.inspectorScrollAmount, scrollY, maxScroll);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    public int calculateInspectorContentHeight(RotaConfigOption option, int innerWidth) {
        boolean canEdit;
        if (option == null) {
            return 0;
        }
        int total = 0;
        boolean hasCustom = option.hasCustomInspectorRenderer();
        if (!hasCustom) {
            int titleLinesCount = this.font != null ? this.font.split((FormattedText)Component.literal((String)("\u00a76\u00a7l" + option.getTitle().getString())), innerWidth).size() : 1;
            total += titleLinesCount * 12 + 10;
        }
        if (!(canEdit = option.canPlayerEdit(this.minecraft))) {
            total += 20;
        }
        if (!hasCustom) {
            String desc = option.getDescription();
            if (desc != null && !desc.isEmpty()) {
                int descLinesCount = this.font != null ? this.font.split((FormattedText)Component.literal((String)("\u00a7f" + desc)), innerWidth).size() : Math.max(1, (int)Math.ceil((double)desc.length() * 6.0 / (double)Math.max(1, innerWidth)));
                total += descLinesCount * 11 + 8;
            }
            if (RotaCategoryScreen.shouldRenderTip(option)) {
                String tip = option.getExampleTip();
                int tipLinesCount = this.font != null ? this.font.split((FormattedText)Component.literal((String)("\u00a7b" + tip)), innerWidth).size() : Math.max(1, (int)Math.ceil((double)tip.length() * 6.0 / (double)Math.max(1, innerWidth)));
                total += tipLinesCount * 11 + 24;
            }
            if (RotaCategoryScreen.shouldRenderDefaultValue(option)) {
                String defaultVal = option.getDefaultValueText();
                String fullDefault = "\u00a77Padr\u00e3o recomendado: \u00a7a" + defaultVal;
                int defLinesCount = this.font != null ? this.font.split((FormattedText)Component.literal((String)fullDefault), innerWidth).size() : 1;
                total += defLinesCount * 11 + 8;
            }
        }
        if (option.hasTexturePreview()) {
            int n;
            boolean hasCaption;
            int pad = 4;
            int optW = option.getTexturePreviewWidth();
            int optH = option.getTexturePreviewHeight();
            int maxPreviewW = Math.max(16, innerWidth - pad * 2);
            int previewH = optH;
            if (optW > maxPreviewW) {
                float scale = (float)maxPreviewW / (float)optW;
                previewH = Math.max(16, (int)((float)optH * scale));
            }
            boolean bl = hasCaption = option.getTexturePreviewCaption() != null;
            if (this.font != null) {
                Objects.requireNonNull(this.font);
                n = 9;
            } else {
                n = 9;
            }
            int fontHeight = n;
            int captionSpacing = hasCaption ? 4 + fontHeight : 0;
            int cardH = pad + previewH + pad + captionSpacing;
            total += cardH + 16;
        }
        if (option.hasIconPreviews()) {
            int slotSize = 22;
            int gap = 3;
            int cols = Math.max(1, (innerWidth + gap) / (slotSize + gap));
            int count = option.getIconPreviews().size();
            int rows = (count + cols - 1) / cols;
            int gridHeight = rows * (slotSize + gap);
            total += gridHeight + 25;
        }
        if (hasCustom) {
            total += option.getCustomInspectorHeight() + 16;
        }
        return total;
    }

    private void renderInspectorPanel(GuiGraphics graphics, int x, int y, int width, int height, int mouseX, int mouseY, float partialTick) {
        graphics.fill(x, y, x + width, y + height, -15198184);
        graphics.renderOutline(x, y, width, height, -11184811);
        int innerX = x + 12;
        int innerWidth = width - 24;
        if (this.focusedOption == null) {
            int innerY = y + 10;
            graphics.drawString(this.font, "\u00a76Ajuda & Descri\u00e7\u00e3o", innerX, innerY, -22016, false);
            graphics.fill(innerX, innerY + 12, innerX + innerWidth, innerY + 13, -12303292);
            List lines = this.font.split((FormattedText)Component.literal((String)"\u00a77Passe o mouse ou clique sobre qualquer configura\u00e7\u00e3o \u00e0 esquerda para ver a explica\u00e7\u00e3o detalhada, valores padr\u00e3o e dicas de funcionamento."), innerWidth);
            int textY = innerY + 20;
            for (FormattedCharSequence line : lines) {
                graphics.drawString(this.font, line, innerX, textY, -3355444, false);
                textY += 11;
            }
            return;
        }
        int totalContentHeight = this.calculateInspectorContentHeight(this.focusedOption, innerWidth);
        int visibleHeight = height - 20;
        double maxScroll = RotaCategoryScreen.calculateMaxScroll(totalContentHeight, visibleHeight);
        this.inspectorScrollAmount = RotaCategoryScreen.clampScroll(this.inspectorScrollAmount, maxScroll);
        graphics.enableScissor(x + 1, y + 1, x + width - 1, y + height - 1);
        int curY = y + 10 - (int)this.inspectorScrollAmount;
        boolean hasCustom = this.focusedOption.hasCustomInspectorRenderer();
        if (!hasCustom) {
            List titleLines = this.font.split((FormattedText)Component.literal((String)("\u00a76\u00a7l" + this.focusedOption.getTitle().getString())), innerWidth);
            for (FormattedCharSequence tLine : titleLines) {
                graphics.drawString(this.font, tLine, innerX, curY, -22016, false);
                curY += 12;
            }
            graphics.fill(innerX, curY + 2, innerX + innerWidth, curY + 3, -11184811);
            curY += 8;
        }
        boolean canEdit = this.focusedOption.canPlayerEdit(this.minecraft);
        int textY = curY;
        if (!canEdit) {
            graphics.fill(innerX - 2, textY - 2, innerX + innerWidth + 2, textY + 14, 0x44CC2222);
            graphics.renderOutline(innerX - 2, textY - 2, innerWidth + 4, 16, -5622989);
            graphics.drawString(this.font, "\u00a7c[Bloqueado pelo Servidor - Requer Admin]", innerX + 2, textY + 2, -43691, false);
            textY += 20;
        }
        if (!hasCustom) {
            String desc = this.focusedOption.getDescription();
            if (desc != null && !desc.isEmpty()) {
                List descLines = this.font.split((FormattedText)Component.literal((String)("\u00a7f" + desc)), innerWidth);
                for (FormattedCharSequence line : descLines) {
                    graphics.drawString(this.font, line, innerX, textY, -1118482, false);
                    textY += 11;
                }
            }
            textY += 8;
            if (RotaCategoryScreen.shouldRenderTip(this.focusedOption)) {
                graphics.fill(innerX - 4, textY - 2, innerX + innerWidth + 4, textY + 1, -13421773);
                graphics.drawString(this.font, "\u00a7e\u00a7lDica / Exemplo:", innerX, textY += 4, -22016, false);
                textY += 12;
                List tipLines = this.font.split((FormattedText)Component.literal((String)("\u00a7b" + this.focusedOption.getExampleTip())), innerWidth);
                for (FormattedCharSequence line : tipLines) {
                    graphics.drawString(this.font, line, innerX, textY, -7807489, false);
                    textY += 11;
                }
                textY += 8;
            }
            if (RotaCategoryScreen.shouldRenderDefaultValue(this.focusedOption)) {
                List defaultLines = this.font.split((FormattedText)Component.literal((String)("\u00a77Padr\u00e3o recomendado: \u00a7a" + this.focusedOption.getDefaultValueText())), innerWidth);
                for (FormattedCharSequence line : defaultLines) {
                    graphics.drawString(this.font, line, innerX, textY, -5592406, false);
                    textY += 11;
                }
                textY += 8;
            }
        }
        if (hasCustom) {
            int customH = this.focusedOption.getCustomInspectorHeight();
            try {
                this.focusedOption.getCustomInspectorRenderer().render(graphics, innerX, textY, innerWidth, customH, partialTick);
            }
            catch (Throwable defaultLines) {
                // empty catch block
            }
            textY += customH + 8;
        }
        if (this.focusedOption.hasTexturePreview()) {
            int n;
            boolean hasCaption;
            graphics.fill(innerX, textY += 6, innerX + innerWidth, textY + 1, -12303292);
            textY += 8;
            int pad = 4;
            int optW = this.focusedOption.getTexturePreviewWidth();
            int optH = this.focusedOption.getTexturePreviewHeight();
            int maxPreviewW = Math.max(16, innerWidth - pad * 2);
            int previewW = optW;
            int previewH = optH;
            if (previewW > maxPreviewW) {
                float scale = (float)maxPreviewW / (float)previewW;
                previewW = maxPreviewW;
                previewH = Math.max(16, (int)((float)optH * scale));
            }
            int cardW = previewW + pad * 2;
            int cardX = innerX + Math.max(0, (innerWidth - cardW) / 2);
            int cardY = textY;
            boolean bl = hasCaption = this.focusedOption.getTexturePreviewCaption() != null;
            if (this.font != null) {
                Objects.requireNonNull(this.font);
                n = 9;
            } else {
                n = 9;
            }
            int fontHeight = n;
            int captionSpacing = hasCaption ? 4 + fontHeight : 0;
            int cardH = pad + previewH + pad + captionSpacing;
            graphics.fill(cardX, cardY, cardX + cardW, cardY + cardH, -16118768);
            graphics.renderOutline(cardX, cardY, cardW, cardH, -12303292);
            try {
                graphics.blit(this.focusedOption.getTexturePreviewLocation(), cardX + pad, cardY + pad, previewW, previewH, 0.0f, 0.0f, optW, optH, optW, optH);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            if (hasCaption) {
                int captionY = cardY + pad + previewH + 4;
                graphics.drawCenteredString(this.font, this.focusedOption.getTexturePreviewCaption(), cardX + cardW / 2, captionY, -5592406);
            }
            textY = cardY + cardH + 2;
        }
        RotaIconPreview hoveredPreview = null;
        if (this.focusedOption.hasIconPreviews()) {
            List<RotaIconPreview> previews = this.focusedOption.getIconPreviews();
            graphics.fill(innerX, textY += 6, innerX + innerWidth, textY + 1, -12303292);
            graphics.drawString(this.font, "\u00a76\u00a7l\u00cdcones Inclu\u00eddos \u00a77(" + previews.size() + "):", innerX, textY += 6, -22016, false);
            textY += 13;
            int slotSize = 22;
            int gap = 3;
            int cols = Math.max(1, (innerWidth + gap) / (slotSize + gap));
            for (int i = 0; i < previews.size(); ++i) {
                int col = i % cols;
                int row = i / cols;
                int slotX = innerX + col * (slotSize + gap);
                int slotY = textY + row * (slotSize + gap);
                boolean isHovered = mouseX >= slotX && mouseX < slotX + slotSize && mouseY >= slotY && mouseY < slotY + slotSize && mouseY >= y + 1 && mouseY <= y + height - 1;
                graphics.fill(slotX, slotY, slotX + slotSize, slotY + slotSize, -300016098);
                graphics.renderOutline(slotX, slotY, slotSize, slotSize, isHovered ? -22016 : -12303292);
                RotaIconPreview prev = previews.get(i);
                int iconOffset = (slotSize - 16) / 2;
                try {
                    graphics.blit(prev.texture(), slotX + iconOffset, slotY + iconOffset, 16, 16, 0.0f, 0.0f, 1, 1, 1, 1);
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
                if (!isHovered) continue;
                hoveredPreview = prev;
            }
        }
        graphics.disableScissor();
        if (maxScroll > 0.0) {
            int barX = x + width - 5;
            int trackH = height - 4;
            int thumbH = Math.max(16, (int)((float)visibleHeight / (float)totalContentHeight * (float)trackH));
            int thumbY = y + 2 + (int)((float)this.inspectorScrollAmount / (float)maxScroll * (float)(trackH - thumbH));
            graphics.fill(barX, y + 2, barX + 3, y + 2 + trackH, 0x44000000);
            graphics.fill(barX, thumbY, barX + 3, thumbY + thumbH, -5592406);
        }
        if (hoveredPreview != null && hoveredPreview.label() != null && !hoveredPreview.label().getString().isEmpty()) {
            graphics.renderTooltip(this.font, hoveredPreview.label(), mouseX, mouseY);
        }
    }

    public void refreshOptions() {
        if (this.listWidget != null) {
            double savedScroll = this.listWidget.getScrollAmount();
            List<RotaConfigOption> options = this.category.getOptions();
            this.listWidget.setOptions(options);
            this.listWidget.setScrollAmount(savedScroll);
        }
    }

    public RotaConfigListWidget getListWidget() {
        return this.listWidget;
    }

    public void onClose() {
        this.saveAndClose();
    }
}

