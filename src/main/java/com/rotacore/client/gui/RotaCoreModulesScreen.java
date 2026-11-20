/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.AbstractSelectionList$Entry
 *  net.minecraft.client.gui.components.ContainerObjectSelectionList
 *  net.minecraft.client.gui.components.ContainerObjectSelectionList$Entry
 *  net.minecraft.client.gui.components.EditBox
 *  net.minecraft.client.gui.components.Tooltip
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.narration.NarratableEntry
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.util.FormattedCharSequence
 */
package com.rotacore.client.gui;

import com.rotacore.RotaCore;
import com.rotacore.api.RotaCoreRegistry;
import com.rotacore.api.RotaModule;
import com.rotacore.client.gui.hud.RotaHudEditScreen;
import com.rotacore.client.gui.widget.RotaButton;
import com.rotacore.client.gui.widget.RotaSwitch;
import com.rotacore.config.RotaCoreConfig;
import com.rotacore.world.naming.RouteNamingStyle;
import com.rotacore.world.routes.RoadCurvatureMode;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;

public class RotaCoreModulesScreen
extends Screen {
    private final Screen parentScreen;
    private ModuleListWidget listWidget;
    private EditBox searchBox;
    private Object focusedItem;

    public RotaCoreModulesScreen(Screen parentScreen) {
        super((Component)Component.literal((String)"Gerenciador de M\u00f3dulos & Op\u00e7\u00f5es"));
        this.parentScreen = parentScreen;
    }

    public void setFocusedItem(Object item) {
        this.focusedItem = item;
    }

    protected void init() {
        int listWidth = (int)((double)this.width * 0.67);
        int topY = 36;
        int bottomHeight = 36;
        int listHeight = this.height - topY - bottomHeight;
        this.listWidget = new ModuleListWidget(this.minecraft, listWidth, listHeight, topY, 32, this);
        this.listWidget.rebuild(RotaCoreRegistry.getAllModules());
        this.addRenderableWidget((GuiEventListener)this.listWidget);
        this.addRenderableWidget((GuiEventListener)new RotaButton(12, 8, 85, 20, (Component)Component.literal((String)"\u2190 Voltar"), btn -> this.saveAndClose()));
        this.addRenderableWidget((GuiEventListener)new RotaButton(12, this.height - 28, 140, 20, (Component)Component.literal((String)"Salvar Altera\u00e7\u00f5es"), btn -> this.saveAndClose()).setAccent(true));
        int inspectorX = listWidth + 8;
        int inspectorWidth = this.width - inspectorX - 12;
        this.searchBox = new EditBox(this.font, inspectorX, this.height - 28, inspectorWidth, 20, (Component)Component.literal((String)"Buscar..."));
        this.searchBox.setHint((Component)Component.literal((String)"\u00a78Buscar m\u00f3dulo..."));
        this.searchBox.setResponder(query -> this.listWidget.setFilter((String)query));
        this.addRenderableWidget((GuiEventListener)this.searchBox);
    }

    private void saveAndClose() {
        RotaCore.saveConfig();
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parentScreen);
        }
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        int titleX = 105;
        int listWidth = (int)((double)this.width * 0.65);
        int maxTitleWidth = listWidth - titleX - 8;
        Object titleStr = "\u00a76\u00a7lRota Mods Hub";
        if (this.font.width((String)titleStr) > maxTitleWidth) {
            titleStr = this.font.plainSubstrByWidth((String)titleStr, maxTitleWidth - 8) + "...";
        }
        graphics.drawString(this.font, (String)titleStr, titleX, 14, -22016, false);
        int topY = 36;
        int bottomY = this.height - 36;
        graphics.fill(listWidth + 2, topY, listWidth + 3, bottomY, -12303292);
        this.renderInspectorPanel(graphics, listWidth + 8, topY, this.width - listWidth - 18, bottomY - topY);
    }

    private void renderInspectorPanel(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, -871296751);
        graphics.renderOutline(x, y, width, height, -11184811);
        int innerX = x + 12;
        int innerY = y + 10;
        int innerWidth = width - 24;
        Object object = this.focusedItem;
        if (object instanceof RotaModule) {
            RotaModule mod = (RotaModule)object;
            graphics.drawString(this.font, "\u00a76\u00a7l" + mod.getDisplayName().getString(), innerX, innerY, -22016, false);
            graphics.drawString(this.font, "\u00a78Vers\u00e3o instalada: " + mod.getVersion(), innerX, innerY + 12, -6246720, false);
            graphics.fill(innerX, innerY + 24, innerX + innerWidth, innerY + 25, -11905688);
            int textY = innerY + 30;
            String desc = mod.getDescription().getString();
            List descLines = this.font.split((FormattedText)Component.literal((String)("\u00a7f" + desc)), innerWidth);
            for (Object line : descLines) {
                graphics.drawString(this.font, (FormattedCharSequence)line, innerX, textY, -1906448, false);
                textY += 11;
            }
            graphics.fill(innerX - 4, (textY += 12) - 2, innerX + innerWidth + 4, textY + 1, -14012614);
            graphics.drawString(this.font, "\u00a7e\u00a7lIntegra\u00e7\u00e3o de Interface:", innerX, textY += 4, -22016, false);
            textY += 14;
            String tipSwitch = "\u00a7a\u2022 Interruptor (Op\u00e7\u00f5es):\u00a77 Quando ATIVADO, este mod adiciona um bot\u00e3o oficial id\u00eantico aos bot\u00f5es nativos do Minecraft na tela de Op\u00e7\u00f5es (OptionsScreen).";
            for (Object line : this.font.split((FormattedText)Component.literal((String)tipSwitch), innerWidth)) {
                graphics.drawString(this.font, (FormattedCharSequence)line, innerX, textY, -3355444, false);
                textY += 11;
            }
            textY += 8;
            String tipGear = "\u00a76\u2022 Engrenagem (\u2699):\u00a77 Abre diretamente a tela de personaliza\u00e7\u00e3o deste mod para alterar suas op\u00e7\u00f5es.";
            for (FormattedCharSequence line : this.font.split((FormattedText)Component.literal((String)tipGear), innerWidth)) {
                graphics.drawString(this.font, line, innerX, textY, -3355444, false);
                textY += 11;
            }
        } else if ("HUB".equals(this.focusedItem)) {
            graphics.drawString(this.font, "\u00a76\u00a7lRota Mods Hub", innerX, innerY, -22016, false);
            graphics.drawString(this.font, "\u00a78Painel Central da Su\u00edte Rota", innerX, innerY + 12, -6246720, false);
            graphics.fill(innerX, innerY + 24, innerX + innerWidth, innerY + 25, -11905688);
            int textY = innerY + 30;
            String tipHub = "\u00a7fControla a exibi\u00e7\u00e3o do bot\u00e3o universal do Rota Hub na tela de Op\u00e7\u00f5es oficial e no menu de pausa (ESC).";
            for (FormattedCharSequence line : this.font.split((FormattedText)Component.literal((String)tipHub), innerWidth)) {
                graphics.drawString(this.font, line, innerX, textY, -1906448, false);
                textY += 11;
            }
        } else if ("HUD_EDITOR".equals(this.focusedItem)) {
            graphics.drawString(this.font, "\u00a76\u00a7lEditor de HUDs", innerX, innerY, -22016, false);
            graphics.drawString(this.font, "\u00a78Sandbox Visual & Auto-Layout", innerX, innerY + 12, -6246720, false);
            graphics.fill(innerX, innerY + 24, innerX + innerWidth, innerY + 25, -11905688);
            int textY = innerY + 30;
            String tipHud = "\u00a7fPermite arrastar livremente qualquer elemento de HUD com o mouse pela tela, utilizando encaixe magn\u00e9tico ou posicionamento autom\u00e1tico anti-colis\u00e3o nas \u00e2ncoras regionais.";
            for (FormattedCharSequence line : this.font.split((FormattedText)Component.literal((String)tipHud), innerWidth)) {
                graphics.drawString(this.font, line, innerX, textY, -1906448, false);
                textY += 11;
            }
        } else if ("ROUTE_NAMING".equals(this.focusedItem)) {
            config = RotaCore.getConfig();
            graphics.drawString(this.font, "\u00a76\u00a7lEstilo de Nomes Geopol\u00edticos", innerX, innerY, -22016, false);
            graphics.drawString(this.font, "\u00a78Zonas Territoriais & Rotas", innerX, innerY + 12, -6246720, false);
            graphics.fill(innerX, innerY + 24, innerX + innerWidth, innerY + 25, -11905688);
            int textY = innerY + 30;
            String currentStyleText = "\u00a7eAtivo: \u00a7f" + (config.routeNamingStyle != null ? config.routeNamingStyle.getDisplayName() : "Fantasia");
            graphics.drawString(this.font, currentStyleText, innerX, textY, -22016, false);
            textY += 13;
            String desc = config.routeNamingStyle != null ? config.routeNamingStyle.getDescription() : "";
            for (Object line : this.font.split((FormattedText)Component.literal((String)("\u00a77" + desc)), innerWidth)) {
                graphics.drawString(this.font, (FormattedCharSequence)line, innerX, textY, -6246720, false);
                textY += 11;
            }
            graphics.fill(innerX - 4, (textY += 10) - 2, innerX + innerWidth + 4, textY + 1, -14012614);
            graphics.drawString(this.font, "\u00a7e\u00a7lEstilos Dispon\u00edveis:", innerX, textY += 4, -22016, false);
            textY += 14;
            String tipFantasy = "\u00a76\u2022 Fantasia / Reinos:\u00a77 Nomes medievais imersivos para reinos, baronias e estradas reais hist\u00f3ricas.";
            for (Object line : this.font.split((FormattedText)Component.literal((String)tipFantasy), innerWidth)) {
                graphics.drawString(this.font, (FormattedCharSequence)line, innerX, textY, -3355444, false);
                textY += 11;
            }
            textY += 6;
            String tipNumeric = "\u00a7a\u2022 Num\u00e9rico / Setorial:\u00a77 Identifica\u00e7\u00e3o t\u00e1tica e limpa (Zona 01, Rota 04).";
            for (Object line : this.font.split((FormattedText)Component.literal((String)tipNumeric), innerWidth)) {
                graphics.drawString(this.font, (FormattedCharSequence)line, innerX, textY, -3355444, false);
                textY += 11;
            }
            textY += 6;
            String tipGeographic = "\u00a7b\u2022 Geogr\u00e1fico:\u00a77 Nomes baseados em relevo e biomas (Plan\u00edcies Orientais, Savana).";
            for (Object line : this.font.split((FormattedText)Component.literal((String)tipGeographic), innerWidth)) {
                graphics.drawString(this.font, (FormattedCharSequence)line, innerX, textY, -3355444, false);
                textY += 11;
            }
            textY += 6;
            String tipDisabled = "\u00a7c\u2022 Desativado:\u00a77 N\u00e3o atribui nomes \u00e0s zonas e rotas.";
            for (FormattedCharSequence line : this.font.split((FormattedText)Component.literal((String)tipDisabled), innerWidth)) {
                graphics.drawString(this.font, line, innerX, textY, -3355444, false);
                textY += 11;
            }
        } else if ("ROAD_CURVATURE".equals(this.focusedItem)) {
            config = RotaCore.getConfig();
            RoadCurvatureMode mode = config.roadCurvatureMode != null ? config.roadCurvatureMode : RoadCurvatureMode.BALANCED;
            graphics.drawString(this.font, (Component)Component.translatable((String)"rota_core.config.road_curvature.inspector_title"), innerX, innerY, -22016, false);
            graphics.drawString(this.font, (Component)Component.translatable((String)"rota_core.config.road_curvature.inspector_subtitle"), innerX, innerY + 12, -6246720, false);
            graphics.fill(innerX, innerY + 24, innerX + innerWidth, innerY + 25, -11905688);
            int textY = innerY + 30;
            MutableComponent activeLabel = Component.literal((String)"\u00a7eAtivo: \u00a7f").append((Component)Component.translatable((String)mode.getTranslationKey()));
            graphics.drawString(this.font, (Component)activeLabel, innerX, textY, -22016, false);
            textY += 13;
            MutableComponent descComp = Component.translatable((String)mode.getDescriptionKey());
            for (Object line : this.font.split((FormattedText)descComp, innerWidth)) {
                graphics.drawString(this.font, (FormattedCharSequence)line, innerX, textY, -6246720, false);
                textY += 11;
            }
            graphics.fill(innerX - 4, (textY += 10) - 2, innerX + innerWidth + 4, textY + 1, -14012614);
            graphics.drawString(this.font, (Component)Component.translatable((String)"rota_core.config.curvature.rule_title"), innerX, textY += 4, -22016, false);
            textY += 14;
            MutableComponent ruleStep = Component.translatable((String)"rota_core.config.curvature.rule_step");
            for (Object line : this.font.split((FormattedText)ruleStep, innerWidth)) {
                graphics.drawString(this.font, (FormattedCharSequence)line, innerX, textY, -3355444, false);
                textY += 11;
            }
            textY += 6;
            MutableComponent rulePlateau = Component.translatable((String)"rota_core.config.curvature.rule_plateau");
            for (FormattedCharSequence line : this.font.split((FormattedText)rulePlateau, innerWidth)) {
                graphics.drawString(this.font, line, innerX, textY, -3355444, false);
                textY += 11;
            }
            textY += 6;
            MutableComponent ruleBridge = Component.translatable((String)"rota_core.config.curvature.rule_bridge");
            for (FormattedCharSequence line : this.font.split((FormattedText)ruleBridge, innerWidth)) {
                graphics.drawString(this.font, line, innerX, textY, -3355444, false);
                textY += 11;
            }
        } else {
            graphics.drawString(this.font, "\u00a76Ajuda & Informa\u00e7\u00f5es", innerX, innerY, -22016, false);
            graphics.fill(innerX, innerY + 14, innerX + innerWidth, innerY + 15, -11905688);
            int textY = innerY + 22;
            String helpText = "\u00a77Passe o cursor sobre qualquer mod \u00e0 esquerda para ver os detalhes da integra\u00e7\u00e3o com o menu de op\u00e7\u00f5es oficial.";
            for (FormattedCharSequence line : this.font.split((FormattedText)Component.literal((String)helpText), innerWidth)) {
                graphics.drawString(this.font, line, innerX, textY, -6246720, false);
                textY += 11;
            }
        }
    }

    public void onClose() {
        this.saveAndClose();
    }

    public static class ModuleListWidget
    extends ContainerObjectSelectionList<ModuleEntry> {
        private final RotaCoreModulesScreen screen;
        private final List<RotaModule> allModules = new ArrayList<RotaModule>();
        private String filter = "";

        public ModuleListWidget(Minecraft minecraft, int width, int height, int y, int itemHeight, RotaCoreModulesScreen screen) {
            super(minecraft, width, height, y, itemHeight);
            this.screen = screen;
        }

        public void rebuild(List<RotaModule> modules) {
            this.allModules.clear();
            if (modules != null) {
                this.allModules.addAll(modules);
            }
            this.applyFilter();
        }

        public void setFilter(String filter) {
            this.filter = filter != null ? filter.trim().toLowerCase() : "";
            this.applyFilter();
        }

        private void applyFilter() {
            this.clearEntries();
            if (this.filter.isEmpty() || "hub".contains(this.filter) || "rota".contains(this.filter)) {
                this.addEntry((AbstractSelectionList.Entry)new HubSettingsEntry(this.screen));
            }
            if (this.filter.isEmpty() || "hud".contains(this.filter) || "sandbox".contains(this.filter) || "posicao".contains(this.filter)) {
                this.addEntry((AbstractSelectionList.Entry)new HudEditorEntry(this.screen));
            }
            if (this.filter.isEmpty() || "nome".contains(this.filter) || "estilo".contains(this.filter) || "rota".contains(this.filter) || "reino".contains(this.filter) || "naming".contains(this.filter) || "zona".contains(this.filter)) {
                this.addEntry((AbstractSelectionList.Entry)new RouteNamingStyleEntry(this.screen));
            }
            if (this.filter.isEmpty() || "curva".contains(this.filter) || "estrada".contains(this.filter) || "serpente".contains(this.filter) || "relevo".contains(this.filter) || "curvature".contains(this.filter) || "road".contains(this.filter)) {
                this.addEntry((AbstractSelectionList.Entry)new RoadCurvatureModeEntry(this.screen));
            }
            for (RotaModule mod : this.allModules) {
                String name = mod.getDisplayName().getString().toLowerCase();
                String id = mod.getId().toLowerCase();
                if (!this.filter.isEmpty() && !name.contains(this.filter) && !id.contains(this.filter)) continue;
                this.addEntry((AbstractSelectionList.Entry)new ModuleRowEntry(mod, this.screen));
            }
            this.setScrollAmount(0.0);
        }

        public int getRowWidth() {
            return this.width - 24;
        }

        protected int getScrollbarPosition() {
            return this.width - 8;
        }
    }

    public static class RoadCurvatureModeEntry
    extends ModuleEntry {
        private final RotaCoreModulesScreen screen;
        private final List<GuiEventListener> children = new ArrayList<GuiEventListener>();
        private final List<NarratableEntry> narratables = new ArrayList<NarratableEntry>();
        private final RotaButton cycleButton;

        public RoadCurvatureModeEntry(RotaCoreModulesScreen screen) {
            this.screen = screen;
            RotaCoreConfig config = RotaCore.getConfig();
            RoadCurvatureMode currentMode = config.roadCurvatureMode != null ? config.roadCurvatureMode : RoadCurvatureMode.BALANCED;
            this.cycleButton = new RotaButton(0, 0, 116, 20, (Component)Component.translatable((String)currentMode.getTranslationKey()), btn -> {
                if (config.roadCurvatureMode == null) {
                    config.roadCurvatureMode = RoadCurvatureMode.BALANCED;
                }
                config.roadCurvatureMode = config.roadCurvatureMode.next();
                btn.setMessage((Component)Component.translatable((String)config.roadCurvatureMode.getTranslationKey()));
            });
            this.cycleButton.setAccent(true);
            this.children.add((GuiEventListener)this.cycleButton);
            this.narratables.add((NarratableEntry)this.cycleButton);
        }

        public List<? extends GuiEventListener> children() {
            return this.children;
        }

        public List<? extends NarratableEntry> narratables() {
            return this.narratables;
        }

        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isHovered, float partialTick) {
            Minecraft mc = Minecraft.getInstance();
            if (isHovered) {
                graphics.fill(left, top, left + width - 4, top + height, 0x1EFFFFFF);
                this.screen.setFocusedItem("ROAD_CURVATURE");
            }
            int textY = top + (height - 18) / 2;
            graphics.drawString(mc.font, (Component)Component.translatable((String)"rota_core.config.road_curvature.title"), left + 8, textY, -22016, false);
            graphics.drawString(mc.font, (Component)Component.translatable((String)"rota_core.config.road_curvature.subtitle"), left + 8, textY + 10, -6246720, false);
            int btnX = left + width - 32 - 116;
            int controlY = top + (height - 20) / 2;
            this.cycleButton.setX(btnX);
            this.cycleButton.setY(controlY);
            this.cycleButton.render(graphics, mouseX, mouseY, partialTick);
        }
    }

    public static class RouteNamingStyleEntry
    extends ModuleEntry {
        private final RotaCoreModulesScreen screen;
        private final List<GuiEventListener> children = new ArrayList<GuiEventListener>();
        private final List<NarratableEntry> narratables = new ArrayList<NarratableEntry>();
        private final RotaButton cycleButton;

        public RouteNamingStyleEntry(RotaCoreModulesScreen screen) {
            this.screen = screen;
            RotaCoreConfig config = RotaCore.getConfig();
            this.cycleButton = new RotaButton(0, 0, 116, 20, (Component)Component.literal((String)(config.routeNamingStyle != null ? config.routeNamingStyle.getDisplayName() : "Fantasia")), btn -> {
                if (config.routeNamingStyle == null) {
                    config.routeNamingStyle = RouteNamingStyle.FANTASY_KINGDOM;
                }
                config.routeNamingStyle = config.routeNamingStyle.next();
                btn.setMessage((Component)Component.literal((String)config.routeNamingStyle.getDisplayName()));
            });
            this.cycleButton.setAccent(true);
            this.children.add((GuiEventListener)this.cycleButton);
            this.narratables.add((NarratableEntry)this.cycleButton);
        }

        public List<? extends GuiEventListener> children() {
            return this.children;
        }

        public List<? extends NarratableEntry> narratables() {
            return this.narratables;
        }

        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isHovered, float partialTick) {
            Minecraft mc = Minecraft.getInstance();
            if (isHovered) {
                graphics.fill(left, top, left + width - 4, top + height, 0x1EFFFFFF);
                this.screen.setFocusedItem("ROUTE_NAMING");
            }
            int textY = top + (height - 18) / 2;
            graphics.drawString(mc.font, "\u00a76Estilo de Nomes (Zonas & Rotas)", left + 8, textY, -22016, false);
            graphics.drawString(mc.font, "Padr\u00e3o de nomenclatura geopol\u00edtica e vi\u00e1ria", left + 8, textY + 10, -6246720, false);
            int btnX = left + width - 32 - 116;
            int controlY = top + (height - 20) / 2;
            this.cycleButton.setX(btnX);
            this.cycleButton.setY(controlY);
            this.cycleButton.render(graphics, mouseX, mouseY, partialTick);
        }
    }

    public static class HudEditorEntry
    extends ModuleEntry {
        private final RotaCoreModulesScreen screen;
        private final List<GuiEventListener> children = new ArrayList<GuiEventListener>();
        private final List<NarratableEntry> narratables = new ArrayList<NarratableEntry>();
        private final RotaButton editButton;

        public HudEditorEntry(RotaCoreModulesScreen screen) {
            this.screen = screen;
            this.editButton = new RotaButton(0, 0, 96, 20, (Component)Component.literal((String)"Editar HUDs"), btn -> {
                if (Minecraft.getInstance() != null) {
                    Minecraft.getInstance().setScreen((Screen)new RotaHudEditScreen(screen));
                }
            });
            this.editButton.setAccent(true);
            this.children.add((GuiEventListener)this.editButton);
            this.narratables.add((NarratableEntry)this.editButton);
        }

        public List<? extends GuiEventListener> children() {
            return this.children;
        }

        public List<? extends NarratableEntry> narratables() {
            return this.narratables;
        }

        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isHovered, float partialTick) {
            Minecraft mc = Minecraft.getInstance();
            if (isHovered) {
                graphics.fill(left, top, left + width - 4, top + height, 0x1EFFFFFF);
                this.screen.setFocusedItem("HUD_EDITOR");
            }
            int textY = top + (height - 18) / 2;
            graphics.drawString(mc.font, "\u00a76Posicionamento de HUDs (Sandbox)", left + 8, textY, -22016, false);
            graphics.drawString(mc.font, "Personalizar coordenadas livres com o mouse", left + 8, textY + 10, -6246720, false);
            int btnX = left + width - 32 - 102;
            int controlY = top + (height - 20) / 2;
            this.editButton.setX(btnX);
            this.editButton.setY(controlY);
            this.editButton.render(graphics, mouseX, mouseY, partialTick);
        }
    }

    public static class HubSettingsEntry
    extends ModuleEntry {
        private final RotaCoreModulesScreen screen;
        private final List<GuiEventListener> children = new ArrayList<GuiEventListener>();
        private final List<NarratableEntry> narratables = new ArrayList<NarratableEntry>();
        private final RotaSwitch toggleSwitch;

        public HubSettingsEntry(RotaCoreModulesScreen screen) {
            this.screen = screen;
            RotaCoreConfig config = RotaCore.getConfig();
            this.toggleSwitch = new RotaSwitch(0, 0, 96, 20, config.showHubInOptionsScreen, val -> {
                config.showHubInOptionsScreen = val;
            });
            this.children.add((GuiEventListener)this.toggleSwitch);
            this.narratables.add((NarratableEntry)this.toggleSwitch);
        }

        public List<? extends GuiEventListener> children() {
            return this.children;
        }

        public List<? extends NarratableEntry> narratables() {
            return this.narratables;
        }

        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isHovered, float partialTick) {
            Minecraft mc = Minecraft.getInstance();
            if (isHovered) {
                graphics.fill(left, top, left + width - 4, top + height, 0x1EFFFFFF);
                this.screen.setFocusedItem("HUB");
            }
            int textY = top + (height - 18) / 2;
            graphics.drawString(mc.font, "\u00a76Rota Mods Hub (Geral)", left + 8, textY, -22016, false);
            graphics.drawString(mc.font, "Exibir bot\u00e3o do Hub na tela de Op\u00e7\u00f5es", left + 8, textY + 10, -6246720, false);
            int toggleX = left + width - 32 - 102;
            int controlY = top + (height - 20) / 2;
            this.toggleSwitch.setX(toggleX);
            this.toggleSwitch.setY(controlY);
            this.toggleSwitch.render(graphics, mouseX, mouseY, partialTick);
        }
    }

    public static class ModuleRowEntry
    extends ModuleEntry {
        private final RotaModule module;
        private final RotaCoreModulesScreen screen;
        private final List<GuiEventListener> children = new ArrayList<GuiEventListener>();
        private final List<NarratableEntry> narratables = new ArrayList<NarratableEntry>();
        private final RotaSwitch toggleSwitch;
        private final RotaButton gearButton;

        public ModuleRowEntry(RotaModule module, RotaCoreModulesScreen screen) {
            this.module = module;
            this.screen = screen;
            RotaCoreConfig config = RotaCore.getConfig();
            boolean isEnabled = config.isModuleEnabledInOptions(module.getId());
            this.toggleSwitch = new RotaSwitch(0, 0, 96, 20, isEnabled, val -> config.setModuleEnabledInOptions(module.getId(), (boolean)val));
            this.children.add((GuiEventListener)this.toggleSwitch);
            this.narratables.add((NarratableEntry)this.toggleSwitch);
            this.gearButton = new RotaButton(0, 0, 24, 20, (Component)Component.literal((String)"\u2699"), btn -> {
                if (Minecraft.getInstance() != null) {
                    Minecraft.getInstance().setScreen(module.createConfigScreen(screen));
                }
            });
            this.gearButton.setTooltip(Tooltip.create((Component)Component.literal((String)("Abrir configura\u00e7\u00f5es de " + module.getDisplayName().getString()))));
            this.gearButton.setAccent(true);
            this.children.add((GuiEventListener)this.gearButton);
            this.narratables.add((NarratableEntry)this.gearButton);
        }

        public List<? extends GuiEventListener> children() {
            return this.children;
        }

        public List<? extends NarratableEntry> narratables() {
            return this.narratables;
        }

        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isHovered, float partialTick) {
            Minecraft mc = Minecraft.getInstance();
            if (isHovered) {
                graphics.fill(left, top, left + width - 4, top + height, 0x1EFFFFFF);
                this.screen.setFocusedItem(this.module);
            }
            int textY = top + (height - 18) / 2;
            graphics.drawString(mc.font, this.module.getDisplayName(), left + 8, textY, isHovered ? -22016 : -1, false);
            graphics.drawString(mc.font, "v" + this.module.getVersion(), left + 8, textY + 10, -6246720, false);
            int gearX = left + width - 32;
            int toggleX = gearX - 102;
            int controlY = top + (height - 20) / 2;
            this.toggleSwitch.setX(toggleX);
            this.toggleSwitch.setY(controlY);
            this.toggleSwitch.render(graphics, mouseX, mouseY, partialTick);
            this.gearButton.setX(gearX);
            this.gearButton.setY(controlY);
            this.gearButton.render(graphics, mouseX, mouseY, partialTick);
        }
    }

    public static abstract class ModuleEntry
    extends ContainerObjectSelectionList.Entry<ModuleEntry> {
    }
}

