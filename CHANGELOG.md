# Version v1.9.0 - Texture Preview Inspector Cards & Dynamic Customization

## Overview
Version **v1.9.0** extends the configuration framework with texture preview cards (`withTexturePreview`), allowing options such as visual cartographic themes and presets to display live synthetic relief or texture graphics directly inside the Inspector panel (`RotaCategoryScreen`).

---

# Version v1.6.0 - Centralized HUD API, Anti-Collision Layout & Sandbox Editor

## Overview
Version **v1.6.0** introduces the **Universal HUD Framework** (`com.rotacore.client.gui.hud`), providing region-based anchor layouts, automatic anti-collision stacking, circular scanlines, and an interactive mouse-driven sandbox editor (`RotaHudEditScreen`) with persistent coordinates.

---

# Version v1.2.0 - Settlement Cadastral Zoning & Boundary Models

## Overview
Version **v1.2.0** introduces the **Settlement Zoning & Cadastral Framework** (`SettlementZone` and `SettlementBounds`), providing shared spatial models for urban bounds, structural reservations, and safe approach perimeters for the entire Rota Mods suite.

---

## Key Features
- **SettlementBounds:** High-performance 2D/3D spatial bounding model with boundary expansion, geometric unions, intersection tests, and native NBT serialization.
- **SettlementZone:**
  - **Structural Bounds (Purple):** Macro cadastral reservation representing the total structural territory.
  - **Urban Core Bounds (Green):** Strict bounding box encompassing registered village buildings and houses.
  - **Approach Perimeter Bounds (Yellow):** Fair approach perimeter (8–10 blocks margin outside urban buildings) for road connections and infrastructure anchoring.
  - **Street Endpoints & Pieces:** Structured registry of village road endpoints and individual piece boxes.
  - **Perimeter Projection Utilities:** Deterministic boundary raycasting to anchor incoming roads cleanly at the yellow approach perimeter without invading residential cores.

<br/>

---
---

<br/>

# Notas da Versão v1.2.0 - Zoneamento Cadastral de Assentamentos *(Português do Brasil)*

## Visão Geral
A versão **v1.2.0** introduz o **Framework de Zoneamento Cadastral de Assentamentos** (`SettlementZone` e `SettlementBounds`), fornecendo modelos espaciais compartilhados para delimitação urbana, reserva estrutural e perímetro de aproximação viária para toda a suíte Rota Mods.

---

## Principais Novidades
- **SettlementBounds:** Modelo espacial 2D/3D de alto desempenho com suporte a expansão perimétrica, união geométrica, checagem de interseção e serialização NBT nativa.
- **SettlementZone:**
  - **Perímetro Estrutural (Roxo):** Bounding box macro representando a reserva total da estrutura no mundo.
  - **Núcleo Urbano (Verde):** Envelope estrito contendo exclusivamente as casas e construções reais da vila.
  - **Perímetro de Aproximação (Amarelo):** Margem justa de aproximação segura (8 a 10 blocos ao redor das casas) para conexão de estradas.
  - **Saídas Viárias e Peças:** Registro estruturado de extremidades de ruas da vila (`streetEndpoints`) e caixas de peças individuais.
  - **Projeção de Aproximação:** Cálculo geométrico determinístico de ancoragem na borda amarela, evitando travessia ou destruição de casas.
