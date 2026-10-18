package com.rotacore.client.gui;

import com.rotacore.RotaCore;
import com.rotacore.api.RotaCoreRegistry;
import com.rotacore.api.RotaModule;
import com.rotacore.client.gui.theme.RotaTheme;
import com.rotacore.client.gui.widget.RotaButton;
import com.rotacore.client.gui.widget.RotaSwitch;
import com.rotacore.config.RotaCoreConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;

/**
 * Tela de gerenciamento de módulos do Rota Core.
 * Permite ativar ou desativar o botão de cada mod na tela oficial de Opções do Minecraft (OptionsScreen)
 * e acessar diretamente as configurações de cada mod via botão de engrenagem (⚙).
 */
public class RotaCoreModulesScreen extends Screen {

    private final Screen parentScreen;
    private ModuleListWidget listWidget;
    private EditBox searchBox;
    private Object focusedItem; // RotaModule or String (for Hub item)

    public RotaCoreModulesScreen(Screen parentScreen) {
        super(Component.literal("Gerenciador de Módulos & Opções"));
        this.parentScreen = parentScreen;
    }

    public void setFocusedItem(Object item) {
        this.focusedItem = item;
    }

    @Override
    protected void init() {
        int listWidth = (int) (this.width * 0.67);
        int topY = 36;
        int bottomHeight = 36;
        int listHeight = this.height - topY - bottomHeight;

        // 1. Lista de Módulos à Esquerda
        this.listWidget = new ModuleListWidget(this.minecraft, listWidth, listHeight, topY, 32, this);
        this.listWidget.rebuild(RotaCoreRegistry.getAllModules());
        this.addRenderableWidget(this.listWidget);

        // 2. Botão Voltar no Canto Superior Esquerdo
        this.addRenderableWidget(
                new RotaButton(12, 8, 85, 20, Component.literal("← Voltar"), btn -> {
                    saveAndClose();
                })
        );

        // 3. Botão Salvar no Rodapé Esquerdo
        this.addRenderableWidget(
                new RotaButton(12, this.height - 28, 140, 20, Component.literal("Salvar Alterações"), btn -> {
                    saveAndClose();
                }).setAccent(true)
        );

        // 4. Caixa de Busca no Rodapé Direito
        int inspectorX = listWidth + 8;
        int inspectorWidth = this.width - inspectorX - 12;

        this.searchBox = new EditBox(this.font, inspectorX, this.height - 28, inspectorWidth, 20, Component.literal("Buscar..."));
        this.searchBox.setHint(Component.literal("§8Buscar módulo..."));
        this.searchBox.setResponder(query -> this.listWidget.setFilter(query));
        this.addRenderableWidget(this.searchBox);
    }

    private void saveAndClose() {
        RotaCore.saveConfig();
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parentScreen);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        // Título alinhado à direita do botão Voltar (sem sobreposição)
        int titleX = 105;
        int listWidth = (int) (this.width * 0.65);
        int maxTitleWidth = listWidth - titleX - 8;
        String titleStr = "§6§lRota Mods Hub";
        if (this.font.width(titleStr) > maxTitleWidth) {
            titleStr = this.font.plainSubstrByWidth(titleStr, maxTitleWidth - 8) + "...";
        }
        graphics.drawString(this.font, titleStr, titleX, 14, RotaTheme.TEXT_TITLE, false);

        // Divisória Vertical
        int topY = 36;
        int bottomY = this.height - 36;
        graphics.fill(listWidth + 2, topY, listWidth + 3, bottomY, RotaTheme.BORDER_SECONDARY);

        // Painel Inspector à Direita (~35% da Tela)
        renderInspectorPanel(graphics, listWidth + 8, topY, this.width - listWidth - 18, bottomY - topY);
    }

    private void renderInspectorPanel(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, RotaTheme.PANEL_BG);
        graphics.renderOutline(x, y, width, height, RotaTheme.BORDER_SECONDARY);

        int innerX = x + 12;
        int innerY = y + 10;
        int innerWidth = width - 24;

        if (focusedItem instanceof RotaModule mod) {
            // Título do Módulo
            graphics.drawString(this.font, "§6§l" + mod.getDisplayName().getString(), innerX, innerY, RotaTheme.TEXT_TITLE, false);
            graphics.drawString(this.font, "§8Versão instalada: " + mod.getVersion(), innerX, innerY + 12, RotaTheme.TEXT_MUTED, false);
            graphics.fill(innerX, innerY + 24, innerX + innerWidth, innerY + 25, RotaTheme.BORDER_SECONDARY);

            // Descrição do Mod
            int textY = innerY + 30;
            String desc = mod.getDescription().getString();
            List<FormattedCharSequence> descLines = this.font.split(Component.literal("§f" + desc), innerWidth);
            for (FormattedCharSequence line : descLines) {
                graphics.drawString(this.font, line, innerX, textY, RotaTheme.TEXT_BODY, false);
                textY += 11;
            }

            textY += 12;
            graphics.fill(innerX - 4, textY - 2, innerX + innerWidth + 4, textY + 1, 0xFF2A2F3A);
            textY += 4;
            graphics.drawString(this.font, "§e§lIntegração de Interface:", innerX, textY, RotaTheme.TEXT_TITLE, false);
            textY += 14;

            String tipSwitch = "§a• Interruptor (Opções):§7 Quando ATIVADO, este mod adiciona um botão oficial idêntico aos botões nativos do Minecraft na tela de Opções (OptionsScreen).";
            for (FormattedCharSequence line : this.font.split(Component.literal(tipSwitch), innerWidth)) {
                graphics.drawString(this.font, line, innerX, textY, 0xFFCCCCCC, false);
                textY += 11;
            }

            textY += 8;
            String tipGear = "§6• Engrenagem (⚙):§7 Abre diretamente a tela de personalização deste mod para alterar suas opções.";
            for (FormattedCharSequence line : this.font.split(Component.literal(tipGear), innerWidth)) {
                graphics.drawString(this.font, line, innerX, textY, 0xFFCCCCCC, false);
                textY += 11;
            }

        } else if ("HUB".equals(focusedItem)) {
            graphics.drawString(this.font, "§6§lRota Mods Hub", innerX, innerY, RotaTheme.TEXT_TITLE, false);
            graphics.drawString(this.font, "§8Painel Central da Suíte Rota", innerX, innerY + 12, RotaTheme.TEXT_MUTED, false);
            graphics.fill(innerX, innerY + 24, innerX + innerWidth, innerY + 25, RotaTheme.BORDER_SECONDARY);

            int textY = innerY + 30;
            String tipHub = "§fControla a exibição do botão universal do Rota Hub na tela de Opções oficial e no menu de pausa (ESC).";
            for (FormattedCharSequence line : this.font.split(Component.literal(tipHub), innerWidth)) {
                graphics.drawString(this.font, line, innerX, textY, RotaTheme.TEXT_BODY, false);
                textY += 11;
            }
        } else {
            graphics.drawString(this.font, "§6Ajuda & Informações", innerX, innerY, RotaTheme.TEXT_TITLE, false);
            graphics.fill(innerX, innerY + 14, innerX + innerWidth, innerY + 15, RotaTheme.BORDER_SECONDARY);

            int textY = innerY + 22;
            String helpText = "§7Passe o cursor sobre qualquer mod à esquerda para ver os detalhes da integração com o menu de opções oficial.";
            for (FormattedCharSequence line : this.font.split(Component.literal(helpText), innerWidth)) {
                graphics.drawString(this.font, line, innerX, textY, RotaTheme.TEXT_MUTED, false);
                textY += 11;
            }
        }
    }

    @Override
    public void onClose() {
        saveAndClose();
    }

    // =========================================================================
    // LISTA DE MÓDULOS E ENTRADAS
    // =========================================================================

    public static class ModuleListWidget extends ContainerObjectSelectionList<ModuleEntry> {
        private final RotaCoreModulesScreen screen;
        private final List<RotaModule> allModules = new ArrayList<>();
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
            applyFilter();
        }

        public void setFilter(String filter) {
            this.filter = filter != null ? filter.trim().toLowerCase() : "";
            applyFilter();
        }

        private void applyFilter() {
            this.clearEntries();

            // Entrada global do Hub
            if (filter.isEmpty() || "hub".contains(filter) || "rota".contains(filter)) {
                this.addEntry(new HubSettingsEntry(screen));
            }

            // Entradas individuais de cada mod
            for (RotaModule mod : allModules) {
                String name = mod.getDisplayName().getString().toLowerCase();
                String id = mod.getId().toLowerCase();
                if (filter.isEmpty() || name.contains(filter) || id.contains(filter)) {
                    this.addEntry(new ModuleRowEntry(mod, screen));
                }
            }
            this.setScrollAmount(0);
        }

        @Override
        public int getRowWidth() {
            return this.width - 24;
        }

        @Override
        protected int getScrollbarPosition() {
            return this.width - 8;
        }
    }

    public static abstract class ModuleEntry extends ContainerObjectSelectionList.Entry<ModuleEntry> {}

    public static class ModuleRowEntry extends ModuleEntry {
        private final RotaModule module;
        private final RotaCoreModulesScreen screen;
        private final List<GuiEventListener> children = new ArrayList<>();
        private final List<NarratableEntry> narratables = new ArrayList<>();

        private final RotaSwitch toggleSwitch;
        private final RotaButton gearButton;

        public ModuleRowEntry(RotaModule module, RotaCoreModulesScreen screen) {
            this.module = module;
            this.screen = screen;

            RotaCoreConfig config = RotaCore.getConfig();
            boolean isEnabled = config.isModuleEnabledInOptions(module.getId());

            // 1. Interruptor moderno para aparecer nas Opções
            this.toggleSwitch = new RotaSwitch(
                    0, 0, 96, 20, isEnabled,
                    val -> config.setModuleEnabledInOptions(module.getId(), val)
            );
            this.children.add(toggleSwitch);
            this.narratables.add(toggleSwitch);

            // 2. Botão com ícone de engrenagem para abrir as configurações do mod
            this.gearButton = new RotaButton(
                    0, 0, 24, 20, Component.literal("⚙"),
                    btn -> {
                        if (Minecraft.getInstance() != null) {
                            Minecraft.getInstance().setScreen(module.createConfigScreen(screen));
                        }
                    }
            );
            this.gearButton.setTooltip(Tooltip.create(Component.literal("Abrir configurações de " + module.getDisplayName().getString())));
            this.gearButton.setAccent(true);
            this.children.add(gearButton);
            this.narratables.add(gearButton);
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

            if (isHovered) {
                graphics.fill(left, top, left + width - 4, top + height, 0x1EFFFFFF);
                screen.setFocusedItem(module);
            }

            // Título do Mod e Versão à Esquerda
            int textY = top + (height - 18) / 2;
            graphics.drawString(mc.font, module.getDisplayName(), left + 8, textY, isHovered ? 0xFFFFAA00 : 0xFFFFFFFF, false);
            graphics.drawString(mc.font, "v" + module.getVersion(), left + 8, textY + 10, RotaTheme.TEXT_MUTED, false);

            // Controles alinhados à direita: [Interruptor] [⚙]
            int gearX = left + width - 32;
            int toggleX = gearX - 102;
            int controlY = top + (height - 20) / 2;

            toggleSwitch.setX(toggleX);
            toggleSwitch.setY(controlY);
            toggleSwitch.render(graphics, mouseX, mouseY, partialTick);

            gearButton.setX(gearX);
            gearButton.setY(controlY);
            gearButton.render(graphics, mouseX, mouseY, partialTick);
        }
    }

    public static class HubSettingsEntry extends ModuleEntry {
        private final RotaCoreModulesScreen screen;
        private final List<GuiEventListener> children = new ArrayList<>();
        private final List<NarratableEntry> narratables = new ArrayList<>();

        private final RotaSwitch toggleSwitch;

        public HubSettingsEntry(RotaCoreModulesScreen screen) {
            this.screen = screen;
            RotaCoreConfig config = RotaCore.getConfig();

            this.toggleSwitch = new RotaSwitch(
                    0, 0, 96, 20, config.showHubInOptionsScreen,
                    val -> config.showHubInOptionsScreen = val
            );
            this.children.add(toggleSwitch);
            this.narratables.add(toggleSwitch);
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

            if (isHovered) {
                graphics.fill(left, top, left + width - 4, top + height, 0x1EFFFFFF);
                screen.setFocusedItem("HUB");
            }

            int textY = top + (height - 18) / 2;
            graphics.drawString(mc.font, "§6Rota Mods Hub (Geral)", left + 8, textY, 0xFFFFAA00, false);
            graphics.drawString(mc.font, "Exibir botão do Hub na tela de Opções", left + 8, textY + 10, RotaTheme.TEXT_MUTED, false);

            int toggleX = left + width - 32 - 102;
            int controlY = top + (height - 20) / 2;

            toggleSwitch.setX(toggleX);
            toggleSwitch.setY(controlY);
            toggleSwitch.render(graphics, mouseX, mouseY, partialTick);
        }
    }
}
