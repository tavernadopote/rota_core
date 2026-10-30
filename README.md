# Rota Core

**Rota Core** é a biblioteca central, leve e nativa da suíte de mods **Rota Mods** (criada por Leandro / Taverna do Pote).

Desenvolvida especialmente para **Minecraft 1.21.1 (NeoForge)**, ela substitui bibliotecas volumosas e externas (como Cloth Config) por uma solução 100% nativa, de alta performance e com identidade visual única.

---

## Principais Funcionalidades

- **Zoneamento Macro-Regional e Orquestração de Mundo (`RegionalGrid`)**: Particionamento determinístico $O(1)$ do mundo em células regionais que ditam a localização lógica e harmônica de vilas e postos avançados.
- **Classificação Universal de Estruturas (`StructureZoneRegistry`)**: Compatibilidade nativa com tags oficiais do Minecraft (`#minecraft:village`) e mods de estruturas como ChoiceTheorem's Overhauled Village (CTOV), Towns and Towers, etc.
- **Hub Universal de Mods (Rota Hub)**: Um painel central com abas que unifica a navegação e configuração de todos os mods da suíte (`Rota Roads`, `Rota Quests`, etc.).
- **Integração no Menu de Pausa (ESC)**: Botão estilizado `Rota Mods` inserido de forma limpa no menu de pausa do jogador e tecla de atalho configurável (`O`).
- **Widgets Visuais Nativos**: Botões com hover dourado (`RotaButton`), seletores numéricos (`RotaSlider`), interruptores toggle modernos (`RotaSwitch`) e abas superiores (`RotaTabButton`).
- **Layout Inspector de 2 Colunas**: Tela com controles roláveis à esquerda e painel de inspeção à direita com descrições detalhadas, valores padrão recomendados, dicas em tempo real e barra de busca instantânea.
- **Gerenciador de Configuração JSON Sem Dependências**: Serialização e desserialização automática em `config/<mod_id>.json` utilizando o GSON nativo do Minecraft.
- **Gerenciador Central de HUD e Editor Sandbox**: Barramento unificado para registro de elementos na tela com layout automático, empilhamento anti-colisão inteligente e editor interativo de arrastar e soltar (drag & drop).
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
    public int getWidth() { return 64; }

    @Override
    public int getHeight() { return 64; }

    @Override
    public boolean isVisible() { return true; }

    @Override
    public void render(GuiGraphics graphics, float partialTick) {
        // Renderização customizada do widget
    }
}

// Registro no RotaHudRegistry:
RotaHudRegistry.register(new MyCompassHudElement());
```

### Editor Interativo Sandbox

O jogador pode acessar a tela de edição visual (`RotaHudEditScreen`) pelo Rota Hub ou por atalho configurado. No editor:
- Os elementos são exibidos com bordas delimitadoras interativas.
- O jogador pode arrastar e soltar os widgets livremente com o mouse ou encaixá-los em âncoras pré-definidas (cantos e laterais da tela).
- O gerenciador `RotaHudLayoutManager` aplica empilhamento anti-colisão automático para impedir sobreposições.
- As posições personalizadas são salvas automaticamente em disco no arquivo de configuração do Rota Core.

---

## Sistema de Nomes de Vilas e DataPacks

O Rota Core inclui um carregador automático de pacotes de nomes de vilas que escuta a pasta de DataPacks do mundo.

### Hierarquia de Resolução

Ao identificar uma vila ou assentamento, o serviço `SettlementNamingService` segue a seguinte prioridade estrita:
1. **Prioridade 1 (Waystones)**: Se o mod Waystones estiver instalado e uma waystone estiver presente dentro do perímetro urbano do assentamento, o nome oficial da waystone é adotado.
2. **Prioridade 2 (DataPacks - Coordenadas Específicas)**: Nomes cadastrados para coordenadas `x,z` específicas via DataPack.
3. **Prioridade 3 (Gerador Procedural Nativo)**: Geração determinística $O(1)$ baseada na semente mundial, coordenadas centrais e bioma ou cultura da vila.

### Tutorial de Criação de DataPack de Nomes

Crie o arquivo JSON dentro do seu DataPack no caminho:
`data/<namespace>/rota_core/village_names/<cultura>.json`

Exemplo para uma cultura nórdica/taiga (`taiga.json` ou `cultura_nordica.json`):

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

#### Campos do Arquivo JSON:
- `replace` (booleano): se `true`, substitui completamente quaisquer listas carregadas anteriormente para essa cultura. Se `false`, mescla adicionando novos nomes sem repetições.
- `culture` (string): identificador da cultura ou bioma (`plains`, `desert`, `savanna`, `taiga`, `snowy` ou identificador customizado).
- `prefixes` (lista de strings): prefixos procedurais.
- `suffixes` (lista de strings): sufixos procedurais (podem iniciar com hífen como `"-gard"` ou minúsculas como `"val"`).
- `unique_names` (lista de strings): nomes únicos completos sorteados deterministicamente com probabilidade balanceada.
- `specific_names` (mapa `"X,Z": "Nome"`): atribui nomes fixos e específicos para coordenadas centrais exatas de vilas.
- `translations` (mapa de dicionários por código de idioma): fornece traduções automáticas para múltiplos idiomas com suporte a fallback ordenado (`pt_br`, `en_us`, etc.).

---

## Vocação Comercial e Econômica (`SettlementCommercialData`)

O Rota Core analisa os blocos de trabalho de aldeões nos assentamentos (composteiras, fornalhas de fusão, bancadas de ferraria, púlpitos, barris, teares, pedreiros, suportes de poções, etc.) e classifica a vila em vocações econômicas:
- **Agropecuária** (`AGRICULTURE`)
- **Metalurgia e Armaria** (`METALLURGY`)
- **Pescaria e Navegação** (`FISHING`)
- **Alquimia e Conhecimento** (`SCHOLARSHIP`)
- **Manufatura e Alvenaria** (`CRAFTING`)
- **Comércio Geral** (`GENERAL_COMMERCE`)

Esses dados econômicos são persistidos na estrutura `SettlementZone` via tags NBT e integrados ao tooltip de visualização cartográfica no mapa.

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
    public String getVersion() { return "1.0.0"; }

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

Pronto! Seu mod já aparecerá automaticamente com aba própria e cartão no Rota Hub.

---

## Requisitos

- **Minecraft**: 1.21.1
- **Mod Loader**: NeoForge (≥ 21.1.65)
- **Java**: 21
- **Dependências Externas**: Nenhuma (Zero)!

---

## Licença

Distribuído sob a licença MIT. Criado por Leandro / Rota Mods.
