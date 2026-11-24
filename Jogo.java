import javax.swing.*; import java.awt.*; import java.awt.event.*; import java.io.File; import java.util.*; import javax.imageio.ImageIO;

public class Jogo extends JPanel implements ActionListener, KeyListener {
    static final int W = 640, H = 480, S = 32;
    Image jogador = img("player"), inimigo = img("enemy"), moeda = img("coin"), chao = img("floor"), coracao = img("heart");
    boolean[] teclas = new boolean[256]; boolean fim;
    double px, py; int mx, my, pontos, nivel, vidas, invencivel; Random rnd = new Random();
    ArrayList<double[]> inimigos = new ArrayList<>();

    static Image img(String n) { try { return ImageIO.read(new File("sprites/" + n + ".png")); } catch (Exception e) { throw new RuntimeException(e); } }

    Jogo() { setPreferredSize(new Dimension(W, H)); setFocusable(true); addKeyListener(this); reiniciar(); new javax.swing.Timer(16, this).start(); }

    void reiniciar() { px = W / 2; py = H / 2; pontos = 0; nivel = 1; vidas = 3; fim = false; inimigos.clear(); novaMoeda(); novoInimigo(); }
    void novaMoeda() { mx = rnd.nextInt(W - S); my = 40 + rnd.nextInt(H - S - 40); }
    void novoInimigo() { inimigos.add(new double[]{ rnd.nextBoolean() ? 0 : W - S, rnd.nextInt(H - S) }); invencivel = 60; }

    public void actionPerformed(ActionEvent e) { // loop do jogo (~60 FPS)
        if (!fim) {
            if (teclas[KeyEvent.VK_LEFT] || teclas[KeyEvent.VK_A]) px -= 4;
            if (teclas[KeyEvent.VK_RIGHT] || teclas[KeyEvent.VK_D]) px += 4;
            if (teclas[KeyEvent.VK_UP] || teclas[KeyEvent.VK_W]) py -= 4;
            if (teclas[KeyEvent.VK_DOWN] || teclas[KeyEvent.VK_S]) py += 4;
            px = Math.max(0, Math.min(W - S, px)); py = Math.max(0, Math.min(H - S, py));
            if (invencivel > 0) invencivel--;
            for (double[] i : inimigos) { // inimigos perseguem o jogador
                double dx = px - i[0], dy = py - i[1], d = Math.hypot(dx, dy) + 0.001, v = 1 + nivel * 0.3;
                i[0] += dx / d * v; i[1] += dy / d * v;
                if (d < S - 8 && invencivel == 0) { vidas--; invencivel = 90; fim = vidas == 0; }
            }
            if (Math.hypot(px - mx, py - my) < S) { pontos++; novaMoeda(); if (pontos % 5 == 0) { nivel++; novoInimigo(); } }
        }
        repaint();
    }

    protected void paintComponent(Graphics g) {
        for (int x = 0; x < W; x += S) for (int y = 0; y < H; y += S) g.drawImage(chao, x, y, S, S, null);
        g.drawImage(moeda, mx, my, S, S, null);
        for (double[] i : inimigos) g.drawImage(inimigo, (int) i[0], (int) i[1], S, S, null);
        if (invencivel / 5 % 2 == 0) g.drawImage(jogador, (int) px, (int) py, S, S, null); // pisca quando invencivel
        for (int i = 0; i < vidas; i++) g.drawImage(coracao, 8 + i * 28, 6, 24, 24, null);
        g.setColor(Color.WHITE); g.setFont(new Font("Monospaced", Font.BOLD, 18));
        g.drawString("Pontos: " + pontos + "  Nivel: " + nivel, W - 270, 24);
        if (fim) { g.setColor(new Color(0, 0, 0, 170)); g.fillRect(0, 0, W, H); g.setColor(Color.WHITE);
            g.drawString("FIM DE JOGO! " + pontos + " pontos - ENTER reinicia", 110, H / 2); }
    }

    public void keyPressed(KeyEvent e) { if (e.getKeyCode() < 256) teclas[e.getKeyCode()] = true; if (fim && e.getKeyCode() == KeyEvent.VK_ENTER) reiniciar(); }
    public void keyReleased(KeyEvent e) { if (e.getKeyCode() < 256) teclas[e.getKeyCode()] = false; }
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] a) {
        JFrame f = new JFrame("Rogue Movel"); f.add(new Jogo()); f.pack(); f.setResizable(false);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); f.setLocationRelativeTo(null); f.setVisible(true);
    }
}
