import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Random;
import javax.imageio.ImageIO;

/**
 * HEROI CONTRA O REI SLIME - jogo 2D em Java Swing (Opcao 1: mocinho contra vilao).
 *
 * O heroi enfrenta os slimes (ajudantes do vilao). A cada 5 slimes derrotados o nivel sobe.
 * No nivel 4 aparece o vilao, o Rei Slime. Derrote-o para vencer.
 *
 * Controles: setas movem, ESPACO atira, botao "Iniciar/Reiniciar" (ou ENTER) comeca o jogo.
 */
public class Jogo extends JPanel implements ActionListener, KeyListener {

    // ===================== CONFIGURACOES =====================
    static final int LARGURA = 640, ALTURA = 480;   // tamanho da janela do jogo
    static final int TAM = 32;                      // tamanho dos personagens na tela
    static final int TAM_CHEFE = 96;                // o chefe e 3x maior
    static final double VEL_HEROI = 4, VEL_TIRO = 9, VEL_TIRO_CHEFE = 4;
    static final int ABATES_POR_NIVEL = 5;          // slimes derrotados para subir de nivel
    static final int NIVEL_DO_CHEFE = 4;            // nivel em que o vilao aparece
    static final int VIDA_CHEFE = 30;               // tiros necessarios para derrotar o vilao

    enum Estado { MENU, JOGANDO, VITORIA, DERROTA }

    /** Tudo que aparece na tela (heroi, slime, tiro, moeda, chefe, explosao) e um Objeto. */
    static class Objeto {
        double x, y;      // posicao (canto superior esquerdo)
        double vx, vy;    // velocidade (usada pelos tiros)
        int tamanho;
        int vida;         // vida do chefe ou duracao da explosao

        Objeto(double x, double y, int tamanho) { this.x = x; this.y = y; this.tamanho = tamanho; }

        /** Retangulo de colisao, um pouco menor que a imagem para a colisao ficar justa. */
        Rectangle caixa() {
            int margem = tamanho / 6;
            return new Rectangle((int) x + margem, (int) y + margem, tamanho - 2 * margem, tamanho - 2 * margem);
        }

        boolean colide(Objeto outro) { return caixa().intersects(outro.caixa()); }

        boolean foraDaTela() { return x < -tamanho || x > LARGURA || y < -tamanho || y > ALTURA; }

        double centroX() { return x + tamanho / 2.0; }
        double centroY() { return y + tamanho / 2.0; }
    }

    // ===================== IMAGENS (SPRITES) =====================
    Image imgHeroi = carregar("player"), imgSlime = carregar("enemy"), imgChefe = carregar("chefe");
    Image imgTiro = carregar("tiro"), imgTiroChefe = carregar("tiro_chefe");
    Image imgMoeda = carregar("coin"), imgChao = carregar("floor"), imgCoracao = carregar("heart");

    static Image carregar(String nome) {
        try {
            return ImageIO.read(new File("sprites/" + nome + ".png"));
        } catch (Exception e) {
            throw new RuntimeException("Nao encontrei sprites/" + nome + ".png - rode o jogo dentro da pasta do projeto.", e);
        }
    }

    // ===================== ESTADO DO JOGO =====================
    Estado estado = Estado.MENU;
    Objeto heroi, chefe, moeda;
    ArrayList<Objeto> slimes = new ArrayList<>();
    ArrayList<Objeto> tiros = new ArrayList<>();        // tiros do heroi
    ArrayList<Objeto> tirosChefe = new ArrayList<>();   // tiros do vilao
    ArrayList<Objeto> explosoes = new ArrayList<>();
    int pontos, vidas, nivel, abates;
    int invencivel;          // quadros restantes de invencibilidade apos levar dano
    int recarga;             // quadros ate poder atirar de novo
    int quadro;              // contador de quadros (usado nas animacoes)
    double dirX, dirY;       // direcao do tiro = ultima direcao em que o heroi andou
    boolean cima, baixo, esquerda, direita, atirando;   // teclas pressionadas
    Random sorteio = new Random();
    JButton botao;

    // ===================== TEMPORIZADORES =====================
    Timer timerJogo = new Timer(16, this);                     // loop principal: ~60 quadros por segundo
    Timer timerSlimes = new Timer(1500, e -> criarSlime());   // cria um slime a cada 1,5 segundo
    Timer timerChefe = new Timer(900, e -> chefeAtira());      // o vilao atira a cada 0,9 segundo

    Jogo(JButton botao) {
        this.botao = botao;
        setPreferredSize(new Dimension(LARGURA, ALTURA));
        setFocusable(true);
        addKeyListener(this);
        timerJogo.start();
    }

    /** Chamado pelo botao (ou ENTER): zera tudo e comeca uma partida. */
    void iniciar() {
        heroi = new Objeto(LARGURA / 2 - TAM / 2, ALTURA - 90, TAM);
        chefe = null;
        slimes.clear(); tiros.clear(); tirosChefe.clear(); explosoes.clear();
        pontos = 0; vidas = 3; nivel = 1; abates = 0;
        invencivel = 60; recarga = 0;
        dirX = 0; dirY = -1;   // comeca atirando para cima
        novaMoeda();
        timerSlimes.setDelay(1500);
        timerSlimes.restart();
        timerChefe.stop();
        estado = Estado.JOGANDO;
        botao.setText("Reiniciar");
        requestFocusInWindow();
    }

    // ===================== LOOP PRINCIPAL =====================
    /** Executado pelo timerJogo a cada 16 ms. */
    public void actionPerformed(ActionEvent e) {
        if (estado == Estado.JOGANDO) {
            moverHeroi();
            atirar();
            moverTiros();
            moverSlimes();
            moverChefe();
            verificarColisoes();
            if (invencivel > 0) invencivel--;
        }
        atualizarExplosoes();
        quadro++;
        repaint();   // pede para redesenhar a tela (chama paintComponent)
    }

    void moverHeroi() {
        double mx = 0, my = 0;
        if (esquerda) mx -= 1;
        if (direita) mx += 1;
        if (cima) my -= 1;
        if (baixo) my += 1;
        if (mx != 0 || my != 0) {
            double d = Math.hypot(mx, my);   // normaliza: na diagonal anda na mesma velocidade
            dirX = mx / d;
            dirY = my / d;
            heroi.x += dirX * VEL_HEROI;
            heroi.y += dirY * VEL_HEROI;
        }
        // nao deixa o heroi sair da tela
        heroi.x = Math.max(0, Math.min(LARGURA - TAM, heroi.x));
        heroi.y = Math.max(0, Math.min(ALTURA - TAM, heroi.y));
    }

    void atirar() {
        if (recarga > 0) recarga--;
        if (atirando && recarga == 0) {
            Objeto tiro = new Objeto(heroi.centroX() - 8, heroi.centroY() - 8, 16);
            tiro.vx = dirX * VEL_TIRO;
            tiro.vy = dirY * VEL_TIRO;
            tiros.add(tiro);
            recarga = 12;   // ~5 tiros por segundo
        }
    }

    void moverTiros() {
        for (Objeto t : tiros) { t.x += t.vx; t.y += t.vy; }
        for (Objeto t : tirosChefe) { t.x += t.vx; t.y += t.vy; }
        tiros.removeIf(Objeto::foraDaTela);
        tirosChefe.removeIf(Objeto::foraDaTela);
    }

    /** Os slimes andam sozinhos em direcao ao heroi, mais rapido a cada nivel. */
    void moverSlimes() {
        double vel = Math.min(1 + nivel * 0.35, 3.2);   // limite para o jogo continuar possivel
        for (Objeto s : slimes) {
            double dx = heroi.x - s.x, dy = heroi.y - s.y;
            double d = Math.hypot(dx, dy) + 0.001;
            s.x += dx / d * vel;
            s.y += dy / d * vel;
        }
    }

    /** O vilao desce ate o topo da tela e depois anda de um lado para o outro. */
    void moverChefe() {
        if (chefe == null) return;
        if (chefe.y < 30) chefe.y += 1;   // entrada
        chefe.x = (LARGURA - TAM_CHEFE) / 2.0 + Math.sin(quadro * 0.02) * 230;
    }

    // ===================== CRIACAO DE INIMIGOS =====================
    /** Chamado pelo timerSlimes: cria um slime em uma das 4 bordas da tela. */
    void criarSlime() {
        if (estado != Estado.JOGANDO || slimes.size() >= 10) return;
        double x, y;
        switch (sorteio.nextInt(4)) {
            case 0:  x = sorteio.nextInt(LARGURA - TAM); y = -TAM; break;    // em cima
            case 1:  x = sorteio.nextInt(LARGURA - TAM); y = ALTURA; break;  // embaixo
            case 2:  x = -TAM; y = sorteio.nextInt(ALTURA - TAM); break;     // esquerda
            default: x = LARGURA; y = sorteio.nextInt(ALTURA - TAM);         // direita
        }
        slimes.add(new Objeto(x, y, TAM));
    }

    void chamarChefe() {
        chefe = new Objeto((LARGURA - TAM_CHEFE) / 2.0, -TAM_CHEFE, TAM_CHEFE);
        chefe.vida = VIDA_CHEFE;
        timerSlimes.setDelay(2500);   // menos ajudantes durante a luta com o chefe
        timerChefe.start();
    }

    /** Chamado pelo timerChefe: o vilao atira 3 bolas em leque na direcao do heroi. */
    void chefeAtira() {
        if (estado != Estado.JOGANDO || chefe == null || chefe.y < 0) return;
        double angulo = Math.atan2(heroi.centroY() - chefe.centroY(), heroi.centroX() - chefe.centroX());
        for (int i = -1; i <= 1; i++) {
            double a = angulo + i * 0.3;
            Objeto t = new Objeto(chefe.centroX() - 10, chefe.centroY() - 10, 20);
            t.vx = Math.cos(a) * VEL_TIRO_CHEFE;
            t.vy = Math.sin(a) * VEL_TIRO_CHEFE;
            tirosChefe.add(t);
        }
    }

    void novaMoeda() {
        moeda = new Objeto(20 + sorteio.nextInt(LARGURA - TAM - 40), 60 + sorteio.nextInt(ALTURA - TAM - 80), TAM);
    }

    // ===================== COLISOES =====================
    void verificarColisoes() {
        // 1) tiro do heroi acerta slime ou chefe
        for (int i = tiros.size() - 1; i >= 0; i--) {
            Objeto tiro = tiros.get(i);
            boolean acertou = false;
            for (int j = slimes.size() - 1; j >= 0; j--) {
                if (tiro.colide(slimes.get(j))) {
                    explodir(slimes.remove(j), 40);
                    slimeDerrotado();
                    acertou = true;
                    break;
                }
            }
            if (!acertou && chefe != null && tiro.colide(chefe)) {
                chefe.vida--;
                pontos += 5;
                explodir(tiro, 20);
                acertou = true;
                if (chefe.vida <= 0) vencer();
            }
            if (acertou) tiros.remove(i);
        }

        // 2) heroi pega a moeda (bonus)
        if (heroi.colide(moeda)) {
            pontos += 5;
            novaMoeda();
        }

        // 3) heroi e atingido (slime, tiro do chefe ou o proprio chefe)
        if (invencivel > 0) return;
        for (int j = slimes.size() - 1; j >= 0; j--) {
            if (heroi.colide(slimes.get(j))) {
                explodir(slimes.remove(j), 40);
                perderVida();
                return;
            }
        }
        for (int j = tirosChefe.size() - 1; j >= 0; j--) {
            if (heroi.colide(tirosChefe.get(j))) {
                tirosChefe.remove(j);
                perderVida();
                return;
            }
        }
        if (chefe != null && heroi.colide(chefe)) perderVida();
    }

    void slimeDerrotado() {
        abates++;
        pontos += 10;
        if (chefe == null && abates % ABATES_POR_NIVEL == 0) {
            nivel++;
            timerSlimes.setDelay(Math.max(600, 1500 - nivel * 200));   // slimes aparecem mais rapido
            if (nivel == NIVEL_DO_CHEFE) chamarChefe();
        }
    }

    void perderVida() {
        vidas--;
        invencivel = 90;   // 1,5 segundo piscando sem levar dano
        if (vidas <= 0) {
            estado = Estado.DERROTA;
            pararTimers();
        }
    }

    void vencer() {
        pontos += 100;
        explodir(chefe, 160);
        for (Objeto s : slimes) explodir(s, 40);
        slimes.clear();
        tirosChefe.clear();
        chefe = null;
        estado = Estado.VITORIA;
        pararTimers();
    }

    void pararTimers() {
        timerSlimes.stop();
        timerChefe.stop();
    }

    // ===================== EXPLOSOES (ANIMACAO) =====================
    void explodir(Objeto alvo, int tamanhoMax) {
        Objeto e = new Objeto(alvo.centroX(), alvo.centroY(), tamanhoMax);
        e.vida = 20;   // dura 20 quadros
        explosoes.add(e);
    }

    void atualizarExplosoes() {
        for (Objeto e : explosoes) e.vida--;
        explosoes.removeIf(e -> e.vida <= 0);
    }

    // ===================== DESENHO =====================
    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // cenario de fundo: a imagem do chao repetida pela tela
        for (int x = 0; x < LARGURA; x += TAM)
            for (int y = 0; y < ALTURA; y += TAM)
                g.drawImage(imgChao, x, y, TAM, TAM, null);

        if (estado == Estado.MENU) {
            desenharMenu(g);
            return;
        }

        g.drawImage(imgMoeda, (int) moeda.x, (int) moeda.y, TAM, TAM, null);
        for (Objeto s : slimes) g.drawImage(imgSlime, (int) s.x, (int) s.y, TAM, TAM, null);
        if (chefe != null) g.drawImage(imgChefe, (int) chefe.x, (int) chefe.y, TAM_CHEFE, TAM_CHEFE, null);
        for (Objeto t : tiros) g.drawImage(imgTiro, (int) t.x, (int) t.y, t.tamanho, t.tamanho, null);
        for (Objeto t : tirosChefe) g.drawImage(imgTiroChefe, (int) t.x, (int) t.y, t.tamanho, t.tamanho, null);

        // o heroi pisca enquanto esta invencivel
        if (estado != Estado.DERROTA && invencivel / 5 % 2 == 0)
            g.drawImage(imgHeroi, (int) heroi.x, (int) heroi.y, TAM, TAM, null);

        // explosoes: circulos que crescem e somem
        for (Objeto e : explosoes) {
            double progresso = 1 - e.vida / 20.0;
            int r = (int) (e.tamanho * progresso / 2);
            g.setColor(new Color(255, 160, 30, (int) (220 * (1 - progresso))));
            g.fillOval((int) e.x - r, (int) e.y - r, r * 2, r * 2);
        }

        desenharPlacar(g);

        if (estado == Estado.VITORIA)
            desenharMensagem(g, "VITORIA!", "Voce derrotou o Rei Slime!", new Color(255, 215, 60));
        if (estado == Estado.DERROTA)
            desenharMensagem(g, "FIM DE JOGO", "O Rei Slime venceu desta vez...", new Color(255, 80, 80));
    }

    void desenharPlacar(Graphics2D g) {
        for (int i = 0; i < vidas; i++) g.drawImage(imgCoracao, 8 + i * 28, 6, 24, 24, null);
        g.setFont(new Font("Monospaced", Font.BOLD, 16));
        g.setColor(Color.WHITE);
        g.drawString("Pontos: " + pontos + "   Abates: " + abates + "   Nivel: " + nivel, 110, 24);

        if (chefe != null) {   // barra de vida do vilao
            int largura = 300, x = (LARGURA - largura) / 2, y = ALTURA - 26;
            g.setColor(new Color(0, 0, 0, 160));
            g.fillRect(x - 2, y - 2, largura + 4, 16);
            g.setColor(new Color(200, 30, 50));
            g.fillRect(x, y, largura * chefe.vida / VIDA_CHEFE, 12);
            g.setColor(Color.WHITE);
            g.drawString("REI SLIME", x + largura / 2 - 40, y - 6);
        }
    }

    void desenharMenu(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 170));
        g.fillRect(0, 0, LARGURA, ALTURA);
        g.drawImage(imgChefe, LARGURA / 2 - 48, 50, TAM_CHEFE, TAM_CHEFE, null);
        escreverCentro(g, "HEROI CONTRA O REI SLIME", 190, 30, new Color(255, 215, 60));
        escreverCentro(g, "Derrote os slimes e enfrente o Rei Slime no nivel " + NIVEL_DO_CHEFE + "!", 240, 15, Color.WHITE);
        escreverCentro(g, "Setas: mover     Espaco: atirar", 290, 16, Color.WHITE);
        escreverCentro(g, "Moedas dao pontos extras. Voce tem 3 vidas.", 320, 15, Color.LIGHT_GRAY);
        if (quadro / 30 % 2 == 0)
            escreverCentro(g, "Clique em \"Iniciar jogo\" ou aperte ENTER", 390, 16, new Color(120, 220, 255));
    }

    void desenharMensagem(Graphics2D g, String titulo, String subtitulo, Color cor) {
        g.setColor(new Color(0, 0, 0, 170));
        g.fillRect(0, 0, LARGURA, ALTURA);
        escreverCentro(g, titulo, ALTURA / 2 - 30, 44, cor);
        escreverCentro(g, subtitulo, ALTURA / 2 + 10, 18, Color.WHITE);
        escreverCentro(g, "Pontuacao final: " + pontos, ALTURA / 2 + 40, 18, Color.WHITE);
        escreverCentro(g, "Clique em \"Reiniciar\" ou aperte ENTER", ALTURA / 2 + 90, 16, new Color(120, 220, 255));
    }

    void escreverCentro(Graphics2D g, String texto, int y, int tamanhoFonte, Color cor) {
        g.setFont(new Font("Monospaced", Font.BOLD, tamanhoFonte));
        int x = (LARGURA - g.getFontMetrics().stringWidth(texto)) / 2;
        g.setColor(cor);
        g.drawString(texto, x, y);
    }

    // ===================== TECLADO =====================
    public void keyPressed(KeyEvent e) { tecla(e.getKeyCode(), true); }

    public void keyReleased(KeyEvent e) { tecla(e.getKeyCode(), false); }

    public void keyTyped(KeyEvent e) {}

    void tecla(int codigo, boolean pressionada) {
        switch (codigo) {
            case KeyEvent.VK_LEFT:  esquerda = pressionada; break;
            case KeyEvent.VK_RIGHT: direita = pressionada; break;
            case KeyEvent.VK_UP:    cima = pressionada; break;
            case KeyEvent.VK_DOWN:  baixo = pressionada; break;
            case KeyEvent.VK_SPACE: atirando = pressionada; break;
            case KeyEvent.VK_ENTER:
                if (pressionada && estado != Estado.JOGANDO) iniciar();
                break;
        }
    }

    // ===================== JANELA =====================
    public static void main(String[] args) {
        JFrame janela = new JFrame("Heroi contra o Rei Slime");
        JButton botao = new JButton("Iniciar jogo");
        Jogo jogo = new Jogo(botao);

        botao.setFocusable(false);               // o teclado continua indo para o jogo
        botao.addActionListener(e -> jogo.iniciar());
        JPanel barra = new JPanel();
        barra.add(botao);

        janela.add(jogo, BorderLayout.CENTER);
        janela.add(barra, BorderLayout.SOUTH);
        janela.pack();
        janela.setResizable(false);
        janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        janela.setLocationRelativeTo(null);
        janela.setVisible(true);
        jogo.requestFocusInWindow();
    }
}
