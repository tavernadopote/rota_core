# Guia de Configuracao e Uso do Rota Core

## 1. Atalhos e Acesso ao Hub
O **Rota Hub** pode ser aberto de tres maneiras no jogo:
1. **Pelo Menu de Pausa (ESC)**: Um botao estilizado `Rota Mods` fica posicionado discretamente no canto superior direito do menu de pausa.
2. **Pela Tecla de Atalho**: Por padrao, pressione a tecla `O` a qualquer momento em jogo (configuravel em *Opcoes > Controles > Atribuicao de Teclas > Rota Mods*).
3. **Pelo Menu de Mods do NeoForge**: Na lista de mods instalados, selecione **Rota Core** (ou qualquer mod Rota) e clique em **Configuracoes**.

## 2. Visao Geral das Telas

### Tela 1: Rota Hub (Visao Geral & Abas)
- **Aba Visao Geral**: Exibe todos os modulos Rota instalados na instancia, suas versoes e botao direto para configurar.
- **Abas dos Modulos**: Clique na aba de qualquer mod instalado (ex: *Rota Roads: Shaper*, *Rota Questbook*) para acessar imediatamente a sua central de personalizacao.

### Tela 2: Grade de Categorias
- Mostra os botoes de cada area do mod selecionado em colunas organizadas (suporte nativo para ate 6 categorias).
- Passar o mouse sobre um botao exibe um resumo daquela categoria no rodape da tela.

### Tela 3: Painel de Opcoes (2 Colunas com Inspector)
- **Lista de Opcoes (Esquerda)**:
  - **Switches**: Alternadores booleanos de liga/desliga com animacao suave.
  - **Sliders**: Seletores numericos com step configuravel e formatacao textual.
  - **Seletores de Itens (`ITEM_ENTRY`)**: Clique na linha para abrir o modal de catalogo de itens com busca em tempo real.
  - **Entradas de Texto (`STRING_INPUT`)**: Campos de texto de linha unica com validacao em tempo de digitacao.
  - **Entradas Multilinhas (`MULTILINE_STRING_INPUT`)**: Campos de texto multilinhas para descricoes, listas e notas com suporte a quebras de linha e rolagem interna.
  - **Botoes de Acao**: Botoes de acao secundaria e botoes de perigo para redefinicao ou exclusao.
- **Painel Inspector (Lateral Direita)**: Ao passar o cursor ou interagir com qualquer opcao, o painel exibe:
  - Titulo oficial da opcao.
  - Explicacao aprofundada de seu funcionamento fisico/logico no jogo.
  - Dicas e padroes recomendados.
  - **Custom Inspector Renderer**: Quando um modulo define um renderizador proprio, o painel direito exibe diagramas, pre-visualizacoes visuais e cartoes graficos dedicados.
- **Busca em Tempo Real**: Digite qualquer termo na barra de busca inferior para filtrar as opcoes instantaneamente.

### Tela 4: Editor Sandbox de HUD (`RotaHudEditScreen`)
- Acesse via configuracoes de HUD de qualquer modulo ou atalho configurado.
- Permite arrastar os elementos da interface diretamente pela tela, alinhar com o grid magnetico de 9 ancoras e testar posicoes utilizando pre-visualizacoes representativas em tempo real (`renderPreview`).
