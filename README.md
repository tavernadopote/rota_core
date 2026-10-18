# Rota Core 🌐

**Rota Core** é a biblioteca central, leve e nativa da suíte de mods **Rota Mods** (criada por Leandro / Taverna do Pote).

Desenvolvida especialmente para **Minecraft 1.21.1 (NeoForge)**, ela substitui bibliotecas volumosas e externas (como Cloth Config) por uma solução 100% nativa, de alta performance e com identidade visual única.

---

## ✨ Principais Funcionalidades

- **★ Hub Universal de Mods (Rota Hub)**: Um painel central com abas que unifica a navegação e configuração de todos os mods da suíte (`Rota Roads`, `Rota Quests`, etc.).
- **Integração no Menu de Pausa (ESC)**: Botão estilizado `★ Rota Mods` inserido de forma limpa no menu de pausa do jogador e tecla de atalho configurável (`O`).
- **Widgets Visuais Nativos**: Botões com hover dourado (`RotaButton`), seletores numéricos (`RotaSlider`), interruptores toggle modernos (`RotaSwitch`) e abas superiores (`RotaTabButton`).
- **Layout Inspector de 2 Colunas**: Tela com controles roláveis à esquerda e painel de inspeção à direita com descrições detalhadas, valores padrão recomendados, dicas em tempo real e barra de busca instantânea.
- **Gerenciador de Configuração JSON Sem Dependências**: Serialização e desserialização automática em `config/<mod_id>.json` utilizando o GSON nativo do Minecraft.
- **Leveza Extrema**: Arquivo jar com apenas **~43 KB** (mais de 25x menor que alternativas legadas).

---

## 🛠️ Como Utilizar em Outros Mods (API)

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

## 📦 Requisitos

- **Minecraft**: 1.21.1
- **Mod Loader**: NeoForge (≥ 21.1.65)
- **Java**: 21
- **Dependências Externas**: Nenhuma (Zero)!

---

## 📜 Licença

Distribuído sob a licença MIT. Criado por Leandro / Rota Mods.
