# Rogue Móvel

Jogo 2D feito em Java Swing: colete moedas e fuja dos slimes. A cada 5 moedas o nível sobe e surge um novo inimigo.

## Como jogar

- Pegue as moedas para ganhar pontos.
- Fuja dos slimes: eles perseguem você e ficam mais rápidos a cada nível.
- Você começa com 3 vidas. Ao levar dano, fica alguns instantes invencível (o personagem pisca).
- A cada 5 moedas o nível sobe e aparece mais um inimigo.
- Quando as vidas acabam, é fim de jogo.

## Controles

| Tecla | Ação |
|---|---|
| Setas ou W A S D | Mover |
| Enter | Reiniciar após o fim de jogo |

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

## Estrutura

```
Jogo.java        código do jogo (loop, movimento, colisões e desenho)
sprites/         imagens do jogador, inimigo, moeda, chão e coração
LEIA-ME.txt      instruções rápidas
RogueMovel.zip   pacote com o jogo completo
```

## Tecnologias

- Java
- Swing / AWT (janela, desenho e teclado)
- `javax.swing.Timer` para o loop do jogo (~60 FPS)
