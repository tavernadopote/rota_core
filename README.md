# Rota Core

**Rota Core** é a biblioteca central, leve e nativa da suíte de mods **Rota Mods** (criada por Leandro / Rota Mods).

Desenvolvida especialmente para **Minecraft 1.21.1 (NeoForge)**, ela substitui bibliotecas volumosas e externas por uma solução 100% nativa, de alta performance e com identidade visual única.

---

## Principais Funcionalidades

- **Zoneamento Macro-Regional e Orquestração de Mundo (`RegionalGrid`)**: Particionamento determinístico $O(1)$ do mundo em células regionais que ditam a localização lógica e harmônica de vilas e postos avançados.
- **Classificação Universal de Estruturas (`StructureZoneRegistry`)**: Compatibilidade nativa com tags oficiais do Minecraft (`#minecraft:village`) e mods de estruturas como ChoiceTheorem's Overhauled Village (CTOV), Towns and Towers, etc.
- **Hub Universal de Mods (Rota Hub)**: Um painel central com abas que unifica a navegação e configuração de todos os mods da suíte (`Rota Roads`, `Rota Questbook`, etc.).
- **Integração no Menu de Pausa (ESC)**: Botão estilizado `Rota Mods` inserido de forma limpa no menu de pausa do jogador e tecla de atalho configurável (`O`).
- **Widgets Visuais Nativos**: Botões com hover dourado (`RotaButton`), seletores numéricos (`RotaSlider`), interruptores toggle modernos (`RotaSwitch`), abas superiores (`RotaTabButton`), campos de texto de linha única (`STRING_INPUT`) e multilinhas (`MULTILINE_STRING_INPUT`).
- **Arquitetura de Modais Ricos (Z=400)**: Modais isolados com tratamento de desfoque de fundo e foco de teclado (`ConfirmModalWidget`, `ItemCatalogModalWidget`, `OptionPickerModalWidget`).
- **Layout Inspector de 2 Colunas e Custom Inspector**: Tela com controles roláveis à esquerda e painel de inspeção à direita com descrições detalhadas, suporte a delegação gráfica personalizada via `CustomInspectorRenderer` e busca instantânea.
- **Gerenciador de Configuração JSON Sem Dependências**: Serialização e desserialização automática em `config/<mod_id>.json` utilizando o GSON nativo do Minecraft.
- **Gerenciador Central de HUD e Editor Sandbox**: Barramento unificado para registro de elementos na tela com layout automático, empilhamento anti-colisão inteligente, modo preview representativo no editor (`renderPreview`) e editor interativo de arrastar e soltar (`RotaHudEditScreen`).
- **Subsistema de Identidade de Jogador (`RotaPlayerIdentity`)**: Persistência determinística em arquivo JSON com vinculação nativa às 16 cores de corante do Minecraft.
- **Sistema de Nomes de Vilas por DataPacks e i18n**: Resolução determinística e multilíngue de nomes de assentamentos com hierarquia tripla de prioridade (Waystones, DataPacks e Procedural Nativo).
- **Vocação Comercial de Assentamentos (`SettlementCommercialData`)**: Análise de postos de trabalho de aldeões e geração de perfis econômicos para mapas e interfaces.
- **Governança de Configurações (Cliente vs Servidor / Admin)**: Separação formal entre preferências cosméticas do cliente e opções de impacto no mundo, com travas de segurança e desativação visual automática para jogadores sem privilégio de operador em servidores multiplayer.
- **Leveza Extrema**: Arquivo jar super otimizado de altíssima performance.

---

## Central de HUD e Editor Sandbox

O Rota Core oferece um subsistema completo para que qualquer mod registre seus widgets visuais na interface do jogador (HUD).

### Registro de Elementos de HUD

Para registrar um elemento no HUD:

```java
public class MyCompassHudElement implements IRotaHudElement {
    @Override
    public ResourceLocation getId() {
        return ResourceLocation.fromNamespaceAndPath("my_mod", "compass");
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Bússola de Navegação");
    }

    @Override
    public RotaHudAnchor getDefaultAnchor() {
        return RotaHudAnchor.TOP_LEFT;
    }

    @Override
    public int getWidth() {
        return 64;
    }

    @Override
    public int getHeight() {
        return 64;
    }

    @Override
    public void render(GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) {
        // Renderização em jogo normal
    }

    @Override
    public void renderPreview(GuiGraphics graphics, int screenWidth, int screenHeight) {
        // Renderização representativa quando dentro do RotaHudEditScreen
    }
}
```

Registre o elemento na inicialização do cliente:

```java
RotaHudRegistry.register(new MyCompassHudElement());
```

---

## Arquitetura de Modais e Entradas Ricas

O Rota Core disponibiliza componentes para criação de interfaces sofisticadas:

### Modais Ricos (Z=400)
- **ConfirmModalWidget**: Exibe caixa de diálogo para confirmações de ações com botões de confirmar e cancelar.
- **ItemCatalogModalWidget**: Catálogo de itens completo com barra de pesquisa instantânea e rolagem contínua para seleção diegética.
- **OptionPickerModalWidget**: Seletor modal para enumerações e listas de opções.

### Tipos de Entrada em RotaConfigEntry
- `ITEM_ENTRY`: Linha de configuração com clique direto para seleção de item através do `ItemCatalogModalWidget`.
- `STRING_INPUT`: Campo de texto de linha única com validação em tempo real.
- `MULTILINE_STRING_INPUT`: Campo de texto multilinhas com quebra automática e suporte a descrições longas.

---

## Renderizador de Inspector Customizado (`CustomInspectorRenderer`)

Mods satélites podem customizar totalmente o painel direito de inspeção em `RotaCategoryScreen`:

```java
category.setCustomInspectorRenderer((graphics, mouseX, mouseY, panelX, panelY, panelWidth, panelHeight) -> {
    // Desenha diagramas, cartões visuais ou pré-visualizações ricas
});
```

Quando um `CustomInspectorRenderer` é registrado, o cabeçalho genérico e a descrição padrão de categoria são automaticamente suprimidos para dar controle visual absoluto ao mod satélite.

---

## Identidade de Jogador (`RotaPlayerIdentity`)

O subsistema `RotaPlayerIdentity` permite associar identificadores e preferências de cores aos jogadores:
- Armazenamento determinístico em JSON local (`config/rota_identity.json`).
- Suporte a 16 cores de corante do Minecraft Vanilla (`DyeColor`).

---

## Sistema de Nomes de Vilas por DataPacks

O Rota Core inclui um sistema completo de nomes procedurais e determinísticos para vilas e assentamentos.

### Estrutura de Arquivo de Nomes de Vilas (`data/<namespace>/settlement_names/<cultura>.json`)

```json
{
  "replace": false,
  "culture": "taiga",
  "prefixes": ["Pinho", "Bruma", "Bosque", "Frio", "Lobo", "Abeto", "Gélido", "Nórdico", "Fjord"],
  "suffixes": ["-gard", "-vik", "-holm", "-boreal", "-fjord", "Frio", "Escuro", "do Norte", "Val"],
  "unique_names": ["Pinheiral", "Fjordfrio", "Taigaburgo", "Bruma Alta", "Nordgard", "Ulfvik"],
  "specific_names": {
    "1200,-450": "Fiorde Real"
  },
  "translations": {
    "pt_br": {
      "Fjordfrio": "Fiorde Frio",
      "Bruma Alta": "Bruma Alta",
      "Fiorde Real": "Fiorde Real"
    },
    "en_us": {
      "Fjordfrio": "Cold Fjord",
      "Bruma Alta": "High Mist",
      "Fiorde Real": "Royal Fjord"
    }
  }
}
```

---

## Vocação Comercial e Econômica (`SettlementCommercialData`)

O Rota Core analisa os blocos de trabalho de aldeões nos assentamentos e classifica a vila em vocações econômicas:
- **Agropecuária** (`AGRICULTURE`)
- **Metalurgia e Armaria** (`METALLURGY`)
- **Pescaria e Navegação** (`FISHING`)
- **Alquimia e Conhecimento** (`SCHOLARSHIP`)
- **Manufatura e Alvenaria** (`CRAFTING`)
- **Comércio Geral** (`GENERAL_COMMERCE`)

---

## Como Utilizar em Outros Mods (API)

### 1. Implemente a Interface `RotaModule`

```java
public class MyModModule implements RotaModule {
    @Override
    public String getId() { return "my_mod"; }

    @Override
    public Component getDisplayName() { return Component.literal("Meu Mod Rota"); }

    @Override
    public String getVersion() { return "1.4.0"; }

    @Override
    public Component getDescription() { return Component.literal("Descrição do que meu mod faz."); }

    @Override
    public Screen createConfigScreen(Screen parent) {
        return new MyModConfigScreen(parent);
    }
}
```

### 2. Registre no `RotaCoreRegistry`

```java
RotaCoreRegistry.register(new MyModModule());
```

---

## Requisitos

- **Minecraft**: 1.21.1
- **Mod Loader**: NeoForge (≥ 21.1.65)
- **Java**: 21
- **Dependências Externas**: Nenhuma (Zero)!

---

## Licença e Termos de Uso

Este projeto adota o modelo **Todos os Direitos Reservados / Source-Available**.
O código-fonte está disponível publicamente para estudo e referência técnica, o consumo da API para criação de mods integrados/addons é expressamente permitido, e a inclusão dos binários oficiais em modpacks é autorizada. Clones, bifurcações públicas não autorizadas (ex: "Rota Core 2"), redistribuição de binários alterados e uso comercial são estritamente proibidos. Consulte o arquivo [LICENSE](LICENSE) para os termos legais completos.
