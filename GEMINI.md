# Diretrizes Globais de Desenvolvimento de Mods de Minecraft (Leandro / Rota Mods)

## 1. Padrão de Versionamento, Ciclo de Vida e Segurança de Código
- **Versões Fechadas de Marco Oficial (Autorização Exclusiva do Usuário)**:
  * O mod só recebe uma nova numeração de versão fechada oficial (ex: `v1.0.0`, `v1.0.1`, `v1.1.0`, `v2.0.0`), criação de pasta oficial em `/MODS/<Mod>/versions/` e Tag semântica no Git mediante **ordem e autorização expressa do usuário**.
  * O usuário é a única autoridade que define quando um ciclo de desenvolvimento está maduro para ser consolidado em uma versão fechada pública (Beta ou Release no CurseForge).
- **Fluxo Consultivo de Sugestão de Numeração**:
  * Ao ser solicitada a consolidação de uma versão fechada, o assistente DEVE analisar tecnicamente o escopo das mudanças concluídas:
    - Correções de bugs, polimento ou estabilidade: propor incremento de PATCH (ex: de `v1.0.0` para `v1.0.1`).
    - Novos recursos, novas telas ou mecânicas adicionadas: propor incremento de MINOR (ex: de `v1.0.0` para `v1.1.0`).
    - Mudanças estruturais que quebrem compatibilidade ou saltos de versão de Minecraft: propor MAJOR (ex: `v2.0.0`).
  * O assistente apresenta a sugestão fundamentada ao usuário e **aguarda sua aprovação expressa** antes de aplicar a numeração oficial.
- **Versões de Desenvolvimento Interno e Laboratório (Controladas pelo Assistente)**:
  * Durante o processo de refinamento, triagem e testes intermediários, o assistente pode gerar quantas builds locais forem necessárias para a pasta de mods da instância de teste (utilizando sufixos como `-dev.1`, `-dev.2` ou mantendo a base de trabalho), sem inflar a numeração oficial nem criar dezenas de versões públicas fantasmas.
  * Cada avanço e ajuste deve ser preservado e rastreável via commits descritivos no Git local.
- **Preservação de Código Estável**: Nunca refazer ou apagar algoritmos funcionais sem criar uma nova branch ou garantir que a versão estável anterior esteja 100% preservada e recuperável via Git.

## 2. Repositório Central de Distribuição e Publicação
Todo mod desenvolvido deve manter sua pasta oficial de distribuição atualizada em:
`/home/leo/Documentos/projetos minecraft/MODS/<Nome do Mod>/`

Estrutura obrigatória por mod:
```text
/home/leo/Documentos/projetos minecraft/MODS/<Nome do Mod>/
├── README.md                          # Visão geral, requisitos, comandos e funcionamento
├── CURSEFORGE_DESCRIPTION.md          # Página formatada em Markdown/HTML pronta para o CurseForge
├── CURSEFORGE_SUMMARY.txt             # Resumo curto bilíngue (1-2 linhas) para o sumário
├── docs/
│   ├── ARCHITECTURE.md                # Arquitetura interna, fluxo de dados e classes principais
│   ├── CONFIGURATION_GUIDE.md         # Documentação de todas as opções de configuração
│   └── DIRETRIZES_MOD.md              # Diretrizes técnicas específicas deste mod
└── versions/
    └── v<X.Y.Z>/
        ├── <mod_id>-<X.Y.Z>.jar       # Arquivo compilado e testado pronto para publicação
        ├── CHANGELOG.md               # Notas detalhadas da versão e correções
        └── RELEASE_INFO.json          # Metadados JSON (versões, loader, checksum SHA256)
```

## 3. Padrão de Comunicação, Documentação e Textos Públicos
- **Tom Honesto, Claro e Técnico**: Todas as descrições, sumários, notas de versão (changelogs), READMEs e páginas públicas (CurseForge, GitHub, etc.) devem apresentar com clareza aquilo que o mod realmente entrega no estágio atual.
- **Sem Linguagem de Vendedor**: Proibido usar tons de propaganda exagerada, promessas irreais ou alfinetadas a projetos concorrentes.
- **Evolução e Ecossistema**: Deixar explícito que os projetos estão em fase inicial/contínua de desenvolvimento e foram desenhados para conversar e se integrar com os futuros mods da série Rota, sem inventar prazos ou datas fixas.
- **Proibição Estrita de Emojis e Figurinhas**: É terminantemente proibido o uso de emojis, figurinhas ou ícones decorativos nas respostas, relatórios, planos, títulos, tópicos e documentações sem autorização expressa do usuário. Sempre que possível, o foco deve ser 100% textual. O uso de símbolos gráficos é restrito a casos raros e excepcionais de botões de interface compactos (ex: ícone de engrenagem para configurações ao invés de palavra), quando expressamente solicitado pelo usuário ou quando uma oportunidade técnica evidente de economia de espaço em UI for identificada e aprovada.

## 4. Governança de Modelos de IA e Eficiência de Tokens
- **Nível Low (Flash / Flash-Lite)**: Respostas simples, confirmações, triagens rápidas ou diálogos que não realizam pesquisas profundas nem modificam ou criam códigos.
- **Nível Medium (Pro / Search / Research)**: Pesquisas precisas no código, consultas a logs, investigações detalhadas de dependências ou buscas de informações online.
- **Nível High (Pro / Deep Coding)**: Codificação, refatorações, criação de novas classes/algoritmos, implementação de regras e desenvolvimento de mods e aplicativos onde a qualidade e a exatidão máxima do código são indispensáveis.

## 5. Diretriz de Trabalho com Planos e Subagentes Especializados
- **Aprovação Prévia Obrigatória em Artefato Interativo**: Toda tarefa de modificação ou adição de código deve primeiro ser apresentada na forma de um **Plano de Execução detalhado** em artefato configurado com formato interativo (`RequestFeedback: true`), permitindo que o usuário visualize o documento no painel lateral e aprove a execução diretamente pelo botão interativo ("Proceed"), ou utilize mensagens subsequentes para refinar, adicionar novos requisitos ou iterar o plano antes da confirmação.
- **Delegação a Subagentes Especializados com Nível Adequado**: Após a aprovação do plano pelo usuário, as etapas de codificação e execução devem ser atribuídas aos subagentes especializados correspondentes ao escopo da tarefa, mantendo o processo estruturado e auditável. O parâmetro `Model` da invocação do subagente DEVE respeitar estritamente a Governança de Modelos da Seção 4:
  * Tarefas de codificação, refatoração, novos algoritmos e compilação de mods (Nível High): invocar subagente obrigatoriamente com `Model: "pro"`.
  * Tarefas de pesquisa de código, investigações estruturais e auditoria de logs (Nível Medium): invocar com `Model: "pro"` ou modelo analítico adequado.
  * Tarefas simples de triagem, verificações leves ou lookups rápidos (Nível Low): invocar com `Model: "flash"`.

## 6. Consulta Obrigatória às Diretrizes Específicas do Mod Trabalhado
- **Ordem de Leitura Estrita**: Ao iniciar qualquer tarefa em um mod, o assistente DEVE:
  1. Ler primeiramente as presentes **Diretrizes Globais** neste arquivo (`GEMINI.md`).
  2. Em seguida, ler OBRIGATORIAMENTE o arquivo de diretrizes específicas do mod trabalhado na vez:
     - Rota Roads: Shaper: `DIRETRIZES_MOD.md` na raiz do mod (ou em `docs/DIRETRIZES_MOD.md`).
     - Rota Map: Cartographer: `DIRETRIZES_MOD.md` na raiz do mod (ou em `docs/DIRETRIZES_MOD.md`).
     - Rota Core: `DIRETRIZES_MOD.md` na raiz do mod (ou em `docs/DIRETRIZES_MOD.md`).
     - Rota Questbook: `DIRETRIZES_MOD.md` na raiz do mod (ou em `docs/DIRETRIZES_MOD.md`).
- **Isolamento de Escopo**: É proibido aplicar regras de engenharia de um mod em outro (por exemplo, regras de slabs e aterros de estradas do Rota Roads não se aplicam a janelas do Rota Map). O assistente deve atuar estritamente dentro do escopo e das regras do mod ativo no momento.

## 7. Governança de Configurações: Cliente vs Servidor e Privilégios de Administrador
- **Separação Rígida de Escopo**:
  * **Configurações de Cliente (Local / Superficial)**: Opções estritamente cosméticas que alteram apenas a interface local do jogador (ex: formato de minimapa, temas visuais, opacidade de linhas, escolha de pacotes de ícones, filtros de exibição). Podem ser alteradas livremente por qualquer jogador, a qualquer momento, seja em singleplayer ou conectado a um servidor.
  * **Configurações de Servidor / Mundo (Gameplay / Profundo)**: Opções que afetam a integridade da jogabilidade, o mundo ou regras globais (ex: permissão de teleporte pelo mapa, geração e traçado de estradas no Rota Roads, zoneamento cadastral de vilas, geração de terrenos).
- **Controle de Acesso em Multiplayer vs Singleplayer**:
  * **Singleplayer (Mundo Local)**: O jogador é o dono do mundo, possuindo autoridade plena para alterar tanto opções de cliente quanto opções de servidor diretamente pelas telas do mod.
  * **Multiplayer (Servidores / Dedicados)**: Jogadores comuns NÃO podem alterar opções de servidor. A interface de configurações deve bloquear, desativar ou ocultar essas opções para clientes sem privilégios de Operador/Administrador (nível de permissão $\ge 2$). Apenas administradores e operadores do servidor podem editar opções globais e de servidor, que devem ser sincronizadas ou aplicadas exclusivamente pelo lado do servidor.
- **Adaptação Obrigatória de Novos Recursos**: Qualquer nova funcionalidade que execute ações com impacto no mundo (ex: teleporte para coordenadas da tela, comandos executados pelo servidor) deve obrigatoriamente respeitar a trava de autorização e política do servidor configurada pelo administrador.
