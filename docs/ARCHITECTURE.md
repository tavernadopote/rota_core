# Arquitetura do Rota Core

## 1. Visao Geral da Arquitetura
O **Rota Core** foi projetado para atuar simultaneamente como:
1. **Hub de Interface do Usuario**: Uma tela mestra acessivel via tecla de atalho (`O`), menu de pausa (`PauseScreen`) e menu de mods do NeoForge.
2. **Biblioteca Compartilhada**: Conjunto de widgets, temas de cores, modais ricos, gerenciadores de arquivo JSON e servicos de identidade para os modulos da suite Rota Mods.

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           Rota Core (NeoForge)                          │
├──────────────────────────────┬──────────────────────────────────────────┤
│          Client UI           │              Config Engine               │
│  - RotaHubScreen             │  - RotaConfigHandler<T>                  │
│  - RotaCategoryScreen        │  - RotaConfigOption (Model)              │
│  - RotaModuleCategoriesScreen│  - Pure GSON Serialization               │
│  - Rich Modal Architecture   │  - Config Entry Types: Toggle, Slider,   │
│    (Confirm, ItemCatalog,    │    ItemPicker, StringInput, Multiline    │
│     OptionPicker @ Z=400)    │                                          │
├──────────────────────────────┼──────────────────────────────────────────┤
│        HUD Subsystem         │             Player Identity              │
│  - IRotaHudElement (Preview) │  - RotaPlayerIdentity                    │
│  - RotaHudRegistry           │  - 16 Dye Colors Palette                 │
│  - RotaHudLayoutManager      │  - JSON File Deterministic Persistence   │
│  - RotaHudEditScreen (Drag)  │                                          │
├──────────────────────────────┴──────────────────────────────────────────┤
│                             Core API                                    │
│  - RotaModule (Interface padronizada para os mods da suite)             │
│  - RotaCoreRegistry (Registro dinamico thread-safe)                     │
│  - RotaCategory (Agrupador hierarquico de configuracoes)                │
│  - CustomInspectorRenderer (Delegacao grafica do painel direito)        │
└─────────────────────────────────────────────────────────────────────────┘
                                   ▲
               ┌───────────────────┴───────────────────┐
               │                                       │
    ┌──────────────────────┐               ┌──────────────────────┐
    │  Rota Roads: Shaper  │               │   Rota Questbook     │
    │     (RotaModule)     │               │     (RotaModule)     │
    └──────────────────────┘               └──────────────────────┘
```

## 2. Componentes Principais

### `com.rotacore.api.RotaModule`
Interface implementada por modulos satelites:
- `getId()`: Identificador unico em minusculas (ex: `rota_roads`, `rota_questbook`).
- `getDisplayName()`: Nome exibido no Hub (ex: `Rota Roads: Shaper`, `Rota Questbook`).
- `getVersion()`: Versao semantica (ex: `1.4.0`, `1.0.0`).
- `getDescription()`: Descricao resumida de escopo e proposito.
- `getCategories()`: Lista de `RotaCategory` que compoem o menu de opcoes.
- `createConfigScreen(parent)`: Tela gerada automaticamente a partir das categorias ou customizada.

### `com.rotacore.api.RotaCoreRegistry`
Registro thread-safe baseado em `ConcurrentHashMap`:
- `register(RotaModule module)`: Registra um modulo na inicializacao do mod.
- `getAllModules()`: Retorna modulos ordenados pela prioridade de exibicao de abas.

### `com.rotacore.config.RotaConfigHandler<T>`
Manipulador generico de arquivos JSON em `.minecraft/config/<mod_id>.json`:
- Thread-safe e sem dependencias externas de terceiros.
- Executa validacoes e restauracao de padroes automaticamente se o arquivo estiver corrompido ou ausente.

### `com.rotacore.client.gui.RotaCategoryScreen` e `CustomInspectorRenderer`
Tela com divisao ergonomica em duas colunas:
- **Coluna Esquerda (~67%)**: `RotaConfigListWidget` com controles dinamicos, navegacao fluida e busca instantanea.
- **Coluna Direita (~33%)**: Inspector Panel com renderizacao continua de explicacoes, dicas e padroes recomendados.
- **CustomInspectorRenderer**: Permite que modulos satelites desenhem renderizacoes visuais ricas (cartoes graficos, diagramas vetoriais, pre-visualizacoes customizadas) no painel direito, suprimindo o cabecalho generico e descricoes padrao quando ativo.

### `com.rotacore.client.gui.modal` (Arquitetura de Modais Ricos)
Sistema de sobreposicao modal com foco exclusivo, isolamento de desfoque de fundo e camada elevada a Z=400:
- **`ConfirmModalWidget`**: Dialogo de confirmacao para operacoes de risco ou sobrescrita, com callbacks tipados.
- **`ItemCatalogModalWidget`**: Catalogo de itens com rolagem continua e busca instantanea para selecao diegetica de blocos ou itens.
- **`OptionPickerModalWidget`**: Modal de selecao rapida para listas enumeradas ou opcoes discretas.

### `com.rotacore.client.gui.hud` (Framework Universal de HUD)
Fornece gerenciamento centralizado de interface e sobreposicoes em tempo real:
- **`RotaHudAnchor`**: 9 regioes canonicas de tela (cantos, bordas e centro) e modo `CUSTOM`.
- **`RotaViewportShape`**: Suporte a mascaras de viewport em formatos `CIRCLE` e `SQUARE`.
- **`RotaHudGeometry`**: Algoritmos analiticos de scanlines circulares, teste euclidiano de contencao e molduras vetoriais.
- **`IRotaHudElement`**: Contrato padronizado para HUDs registrados por modulos satelites, agora com o metodo `renderPreview` para fornecer visualizacao representativa durante a edicao.
- **`RotaHudRegistry`**: Registro global centralizado para HUDs com controle dinamico de visibilidade.
- **`RotaHudLayoutManager`**: Algoritmo de empilhamento anti-colisao em cascata (bottom-up nos cantos inferiores e top-down nos superiores) e renderizacao centralizada.
- **`RotaHudEditScreen`**: Sandbox visual com arraste livre via mouse, encaixe magnetico (snap-to-grid), preview em tempo real dos elementos e persistencia automatica de coordenadas.

### `com.rotacore.player.RotaPlayerIdentity` (Servico de Identidade de Jogador)
Subsistema para gerenciar perfis locais e identidades cosméticas compartilhadas entre os mods da suite:
- Persistencia JSON deterministica sob diretorio do cliente.
- Mapeamento estrito para as 16 cores de corante do Minecraft Vanilla.
