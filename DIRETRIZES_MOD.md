# Diretrizes de Engenharia: Rota Core

Este documento define os padrões técnicos, regras arquiteturais e o propósito exato do **Rota Core** dentro do ecossistema Rota Mods. Deve ser lido obrigatoriamente antes de qualquer modificação no código.

---

## 1. Missão e Princípios do Rota Core
- O **Rota Core** é a biblioteca compartilhada e o ecossistema central de todos os mods da série Rota (`Rota Roads`, `Rota Map`, `Rota Questbook`).
- **NUNCA** adicionar mecânicas de gameplay específicas de um mod dentro do Core (ex: geração de pavimentos, carvers de estradas ou algoritmos topográficos pertencem aos seus respectivos mods).
- O Rota Core provê:
  1. O **Sistema Universal de Zoneamento Cadastral de Assentamentos (`SettlementZone`)**.
  2. O **Framework Oficial de Interfaces e Configurações** (Hub de módulos, telas de 2 colunas com lista de controles e painel Inspector de dicas, botões, sliders e switches estilizados).
  3. Utilitários matemáticos universais e estruturas de dados de grafos.

---

## 2. Sistema de Interfaces e Configurações Nativas (Framework Rota UI)
O Rota Core fornece a arquitetura que padroniza o visual de telas de configuração para toda a suíte:
- **`RotaHubScreen`**: Tela principal de gerenciamento aberta por tecla de atalho ou menu de mods, exibindo a barra superior de abas de módulos.
- **`RotaModuleCategoriesScreen`**: Tela de grade de categorias em 2 colunas, exibindo os cartões de categorias com descrição no hover.
- **`RotaCategoryScreen`**: Tela dividida em duas colunas:
  * **Lado Esquerdo (~67%)**: Lista rolável de opções interativas (`RotaConfigListWidget`, `RotaButton`, `RotaSwitch`, `RotaSlider`).
  * **Lado Direito (~33%)**: Painel fixo lateral (Inspector) com campo de busca no topo e caixa de texto de explicação detalhada e dicas para a opção que estiver com foco do cursor.
- Todos os mods da suíte que possuem tela de configuração devem implementar `RotaModule` e registrar suas categorias (`RotaCategory`) para se integrarem a esse framework.

---

## 3. Modelo Cadastral Trifásico de Vilas (`SettlementZone`)
Todas as vilas e estruturas de assentamento catalogadas no mundo são modeladas através de 3 anéis concêntricos:
1. **Reserva Estrutural (Borda Roxa - `structuralBounds`)**: Caixa delimitadora total da estrutura gerada nativamente pelo Minecraft/CTOV.
2. **Núcleo Urbano (Borda Verde - `urbanCoreBounds`)**: Envelope justo que delimita estritamente o conjunto real de casas e edificações habitadas.
3. **Perímetro de Aproximação (Borda Amarela - `approachPerimeterBounds`)**: Margem externa imediata de aproximação viária segura (ex: 8 a 10 blocos ao redor das casas), onde as vias externas devem ancorar sem invadir as casas.
- O Rota Core armazena e expõe esses limites de forma imutável e serializável para que o `Rota Roads` faça o traçado físico de estradas e o `Rota Map` faça a renderização cadastral na tela.

---

## 4. Governança de API e Compatibilidade
- Toda alteração em classes públicas de `com.rotacore.api` ou `com.rotacore.world.zoning` deve manter compatibilidade binária para não quebrar outros mods compilados contra versões anteriores do Core.
