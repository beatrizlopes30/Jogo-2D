# Herói contra o Rei Slime

Jogo 2D feito em Java Swing (Opção 1, mocinho contra vilão). O herói enfrenta os slimes, que são os ajudantes do vilão, e depois luta contra o **Rei Slime**.

## Como jogar

- Atire nos slimes: eles surgem pelas bordas da tela e perseguem o herói.
- A cada **5 slimes derrotados** o nível sobe, e os slimes ficam mais rápidos e aparecem com mais frequência.
- No **nível 4** chega o vilão, o **Rei Slime**. Ele anda pelo topo da tela, dispara rajadas de 3 tiros e tem uma barra de vida. Acerte **30 tiros** nele para vencer.
- Você tem **3 vidas**. Encostar num slime, no chefe ou levar um tiro do chefe tira uma vida, e o herói fica alguns instantes invencível (pisca).
- As moedas dão pontos extras.

| Ação | Pontos |
|---|---|
| Derrotar um slime | +10 |
| Acertar o Rei Slime | +5 |
| Pegar moeda | +5 |
| Vencer o Rei Slime | +100 |

## Controles

| Tecla | Ação |
|---|---|
| Setas | Mover |
| Espaço | Atirar (na direção em que o herói andou por último) |
| Botão **Iniciar jogo / Reiniciar** ou Enter | Começar ou recomeçar a partida |

## Como executar

Requisito: **Java 11 ou superior**.

```bash
git clone https://github.com/beatrizlopes30/Jogo-2D.git
cd Jogo-2D
java Jogo.java
```

> Execute o comando **dentro da pasta do projeto**, porque os sprites são carregados da pasta `sprites/`.

Se preferir compilar antes:

```bash
javac Jogo.java
java Jogo
```

## Requisitos da proposta atendidos

| Requisito | Onde está no código |
|---|---|
| Janela gráfica e cenário com imagem de fundo | `main` (JFrame) e `paintComponent` (chão em `floor.png`) |
| Pelo menos dois sprites | Herói, slime, Rei Slime, tiros, moeda e coração (pasta `sprites/`) |
| Movimento pelas setas | `tecla` e `moverHeroi` |
| Disparo com a barra de espaço | `tecla` (`VK_SPACE`) e `atirar` |
| Inimigos se movendo com temporizadores | `timerJogo`, `timerSlimes` e `timerChefe` |
| Detecção de colisões | `Objeto.colide` (retângulos) e `verificarColisoes` |
| Pontos, vidas e abates | `pontos`, `vidas` e `abates`, mostrados em `desenharPlacar` |
| Mensagens de vitória e derrota | `vencer`, `perderVida` e `desenharMensagem` |
| Botão para iniciar ou reiniciar | `JButton` criado no `main`, que chama `iniciar()` |

**Extras:** níveis de dificuldade, chefe com barra de vida e padrão de tiro, animação de explosões e herói piscando ao levar dano.

## Estrutura

```
Jogo.java        código do jogo
sprites/         imagens: player, enemy, chefe, tiro, tiro_chefe, coin, heart, floor
LEIA-ME.txt      instruções rápidas
Jogo2D.zip       pacote com o jogo completo para entrega
```

## Tecnologias

- Java
- Swing / AWT (janela, botão, desenho e teclado)
- `javax.swing.Timer` para o loop do jogo (~60 FPS), para criar os slimes e para os tiros do chefe
