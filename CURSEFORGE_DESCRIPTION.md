# Rota Core

**Rota Core** is the lightweight, native core library and unified configuration hub for the **Rota Mods** ecosystem, developed by **Leandro (Rota Mods)** for **Minecraft NeoForge 1.21.1**.

It provides a high-performance foundation for world generation algorithms, deterministic regional zoning, settlement spatial analysis, rich modal dialogs, universal HUD rendering, and a cohesive, native multi-tabbed in-game settings interface.

---

### Key Features

- **Unified Rota Hub (`O` Key / Mod Menu)**: Access all installed Rota Mods (`Rota Roads: Shaper`, `Rota Map: Cartographer`, `Rota Questbook`, and future expansions) within a single, integrated multi-tab interface.
- **Two-Column Inspector Interface**: Configure settings on the left pane while viewing real-time explanations, technical context, recommended defaults, and tooltips in the live Inspector panel on the right.
- **Custom Inspector Rendering**: Extensible API allowing companion mods to draw rich visual components, diagrams, and live preview cards in the inspector pane with automatic suppression of generic category headers.
- **Rich Modal Subsystem (Z=400)**: Built-in high-precedence modal widgets including `ConfirmModalWidget` for risk confirmation, `ItemCatalogModalWidget` with smooth scrolling and instant search, and `OptionPickerModalWidget` for discrete choices.
- **Expanded Configuration Types**: Native support for item picker rows (`ITEM_ENTRY`), single-line text inputs (`STRING_INPUT`), and multi-line descriptions (`MULTILINE_STRING_INPUT`) with auto-wrapping.
- **Universal 9-Anchor HUD Framework & Live Editor**: Centralized overlay bar with anti-collision cascading layouts, custom viewport clipping, and an interactive drag-and-drop visual editor (`RotaHudEditScreen`) supporting element preview rendering (`renderPreview`).
- **Player Identity Subsystem (`RotaPlayerIdentity`)**: Deterministic JSON-backed identity persistence mapped across the 16 Minecraft dye colors for cross-mod user preferences and marker tinting.
- **Instant Search & Filtering**: Quickly locate any configuration setting across all modules using the responsive search bar.
- **Deterministic Regional Grid Architecture**: Mathematical foundation utilized by companion mods (such as *Rota Roads*) for consistent regional planning and seed-based structure coordination.
- **Settlement Spatial Boundaries (`SettlementZone`)**: Advanced spatial analysis providing three-tier settlement zoning (structural bounds, real building envelope, and safety approach perimeters) for companion mods.
- **Clean JSON Configuration**: High-speed configuration storage leveraging Minecraft's native serialization engine without bulky external dependencies.
- **Server & Client Optimized**: Lightweight and safe for both dedicated servers and single-player clients. User interface routines are automatically omitted on dedicated servers with zero overhead.

---

### Required By

- **Rota Roads: Shaper**
- **Rota Map: Cartographer**
- **Rota Questbook**

---

### Compatibility & Technical Specs

- **Minecraft**: `1.21.1`
- **Mod Loader**: `NeoForge` ($\ge$ 21.1.65)
- **Java**: `21`
- **Side**: Client and Server (Both)

<br/>

---
---

<br/>

# Rota Core *(Português do Brasil)*

O **Rota Core** é a biblioteca base nativa e central de configurações do ecossistema **Rota Mods**, desenvolvida por **Leandro (Rota Mods)** para **Minecraft NeoForge 1.21.1**.

Ele oferece uma base leve e de alto desempenho para algoritmos de geração de relevo, zoneamento regional determinístico, análise espacial de assentamentos, diálogos modais ricos, framework universal de HUD e um menu unificado em abas integrado ao jogo.

---

### Principais Recursos

- **Hub Unificado Rota (Tecla `O` / Menu de Mods)**: Acesse e configure todos os mods da suíte instalados (`Rota Roads: Shaper`, `Rota Map: Cartographer`, `Rota Questbook` e futuros lançamentos) em uma única interface centralizada em abas.
- **Interface com Painel Inspetor Duplo**: Navegue pelas opções na coluna esquerda enquanto lê explicações completas, dicas de uso e valores padrão recomendados no painel lateral à direita.
- **Renderizador de Inspector Customizado**: API extensível que permite a mods companheiros desenhar componentes visuais ricos, diagramas e cartões de pré-visualização ao vivo no painel direito, suprimindo automaticamente títulos genéricos de categoria.
- **Arquitetura de Modais Ricos (Z=400)**: Modais nativos de alta precedência visual incluindo `ConfirmModalWidget` para confirmação de ações críticas, `ItemCatalogModalWidget` com rolagem contínua e busca rápida de itens, e `OptionPickerModalWidget` para escolhas discretas.
- **Novos Tipos de Entrada de Configuração**: Suporte nativo a linhas de seleção de itens (`ITEM_ENTRY`), campos de texto de linha única (`STRING_INPUT`) e campos de texto multilinhas (`MULTILINE_STRING_INPUT`) com quebra automática de texto.
- **Framework de HUD Universal de 9 Âncoras e Editor Visual**: Barramento centralizado de overlays com empilhamento anti-colisão, recorte de viewport e editor visual de arrastar e soltar (`RotaHudEditScreen`) com suporte a modo de preview representativo (`renderPreview`).
- **Subsistema de Identidade de Jogador (`RotaPlayerIdentity`)**: Persistência determinística em JSON com suporte às 16 cores de corante do Minecraft para preferências de perfil e tintura compartilhadas entre mods da suíte.
- **Busca Rápida de Opções**: Localize instantaneamente qualquer parâmetro de configuração digitando na barra de pesquisa integrada.
- **Grade Regional Determinística**: Base matemática utilizada por módulos companheiros (como o *Rota Roads*) para planejamento territorial consistente e leitura de estruturas baseada na semente (seed).
- **Zonas Espaciais de Assentamento (`SettlementZone`)**: Análise espacial avançada fornecendo zoneamento em três níveis (área estrutural, envelope real das construções e perímetro de aproximação segura) para mods companheiros.
- **Configuração Limpa em JSON**: Carregamento e salvamento rápidos utilizando o mecanismo nativo do jogo, sem depender de bibliotecas externas pesadas.
- **Otimizado para Servidores e Clientes**: Totalmente seguro para servidores dedicados e clientes single-player. As rotinas visuais de interface gráfica são desligadas automaticamente no lado do servidor com zero impacto no desempenho.

---

### Obrigatório Para

- **Rota Roads: Shaper**
- **Rota Map: Cartographer**
- **Rota Questbook**

---

### Compatibilidade e Requisitos

- **Minecraft**: `1.21.1`
- **Mod Loader**: `NeoForge` ($\ge$ 21.1.65)
- **Java**: `21`
- **Ambiente**: Cliente e Servidor (Ambos)
