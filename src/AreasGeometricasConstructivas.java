
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import javax.swing.*;

public class AreasGeometricasConstructivas extends JFrame {

    static final Color[] COLOR = {
        new Color(45, 115, 215),
        new Color(235, 140, 45),
        new Color(155, 80, 190),
        new Color(25, 155, 120)
    };

    static final String[] FORMAS2 = {
        "Rectángulo", "Círculo", "Elipse", "Polígono regular"
    };

    static final String[] FORMAS3 = {
        "Cubo", "Prisma rectangular", "Esfera",
        "Cilindro", "Cono", "Toro (dona)"
    };

    static final String[] SIGNOS = {" ∪ ", " ∩ ", " − "};

    public AreasGeometricasConstructivas() {
        super("Áreas Geométricas Constructivas");

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Áreas 2D", new Taller(false));
        tabs.addTab("Sólidos 3D", new Taller(true));

        add(tabs);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1180, 760);
        setMinimumSize(new Dimension(1040, 700));
        setLocationRelativeTo(null);
    }

    static class Figura {

        int tipo;

        // X, Y, Z, ancho, alto, profundidad y lados.
        double[] d = {0, 0, 0, 70, 80, 60, 5};

        Shape forma() {
            double x = d[0], y = d[1], w = d[3], h = d[4];

            if (tipo == 0) {
                return new Rectangle2D.Double(x, y, w, h);
            }

            if (tipo < 3) {
                return new Ellipse2D.Double(
                        x, y, w, tipo == 1 ? w : h
                );
            }

            Path2D p = new Path2D.Double();

            for (int i = 0; i < (int) d[6]; i++) {
                double a = -Math.PI / 2 + 2 * Math.PI * i / d[6];
                double px = x + w / 2 + w / 2 * Math.cos(a);
                double py = y + w / 2 + w / 2 * Math.sin(a);

                if (i == 0) {
                    p.moveTo(px, py);
                } else {
                    p.lineTo(px, py);
                }
            }

            p.closePath();
            return p;
        }

        boolean contiene(double x, double y, double z) {
            x -= d[0];
            y -= d[1];
            z -= d[2];

            double r = d[3] / 2, h = d[4] / 2;

            switch (tipo) {
                case 0:
                    return Math.abs(x) <= r
                            && Math.abs(y) <= r && Math.abs(z) <= r;
                case 1:
                    return Math.abs(x) <= r
                            && Math.abs(y) <= h && Math.abs(z) <= d[5] / 2;
                case 2:
                    return x * x + y * y + z * z <= r * r;
                case 3:
                    return x * x + z * z <= r * r && Math.abs(y) <= h;
                case 4:
                    double radio = r * (h - y) / (2 * h);
                    return Math.abs(y) <= h
                            && x * x + z * z <= radio * radio;
                default:
                    double q = Math.sqrt(x * x + z * z) - d[3] * .35;
                    return q * q + y * y <= Math.pow(d[3] * .15, 2);
            }
        }
    }

    static boolean operar(boolean a, boolean b, int op) {
        return op == 0 ? a || b : op == 1 ? a && b : a && !b;
    }

    static Area operar(Shape a, Shape b, int op) {
        Area r = new Area(a), s = new Area(b);

        if (op == 0) {
            r.add(s);
        } else if (op == 1) {
            r.intersect(s);
        } else {
            r.subtract(s);
        }

        return r;
    }

    static class Taller extends JPanel {

        final boolean es3D;
        final Figura[] f = {new Figura(), new Figura(), new Figura()};

        final JComboBox<String> figura = new JComboBox<>(
                new String[]{"A (azul)", "B (naranja)"}
        );

        final JComboBox<String> forma;
        final JPanel campos = new JPanel(new GridLayout(0, 2, 6, 5));
        final JPanel paso2;
        final JButton agregar = new JButton("+ Agregar figura C");
        final JLabel formula = new JLabel(), estado = new JLabel();
        final Vista entrada, salida;
        final javax.swing.Timer espera;

        final int[] op = {0, 2};
        final JToggleButton[][] botones = new JToggleButton[2][3];

        boolean conC = false, cargando = false;
        double giro = .65, inclinacion = .45, zoom = 1;

        ArrayList<Cara> originales = new ArrayList<>();
        ArrayList<Cara> resultado = new ArrayList<>();

        static final int N = 48;
        static final double PASO = 5, MIN = -120;

        Taller(boolean es3D) {
            this.es3D = es3D;
            forma = new JComboBox<>(es3D ? FORMAS3 : FORMAS2);

            for (int i = 0; i < 3; i++) {
                f[i].tipo = i == 0 ? 0 : (es3D ? 2 : 1);
                f[i].d = es3D
                        ? new double[]{(i - 1) * 20, 0, 0, 70, 80, 60, 5}
                        : new double[]{100 + i * 70, 130 + i * 25, 0, 220, 190, 60, 5};
            }

            f[2].d[3] = es3D ? 30 : 85;

            setLayout(new BorderLayout(12, 12));
            setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

            JPanel lateral = new JPanel();
            lateral.setLayout(new BoxLayout(lateral, BoxLayout.Y_AXIS));
            lateral.setPreferredSize(new Dimension(280, 600));

            lateral.add(new JLabel("1. Edita la figura seleccionada"));
            lateral.add(figura);
            lateral.add(forma);
            lateral.add(campos);
            lateral.add(Box.createVerticalStrut(12));
            lateral.add(agregar);
            lateral.add(Box.createVerticalStrut(12));
            lateral.add(operaciones("2. Combina A y B", 0));

            paso2 = operaciones("3. Combina el resultado con C", 1);
            paso2.setVisible(false);
            lateral.add(paso2);

            formula.setFont(new Font("SansSerif", Font.BOLD, 17));
            lateral.add(formula);

            JButton ejemplo = new JButton(
                    es3D ? "Ejemplo: cubo perforado" : "Ejemplo: unión con hueco"
            );
            ejemplo.addActionListener(e -> ejemplo());
            lateral.add(ejemplo);

            if (es3D) {
                JButton vista = new JButton("Restablecer vista");
                vista.addActionListener(e -> {
                    giro = .65;
                    inclinacion = .45;
                    zoom = 1;
                    repintar();
                });
                lateral.add(vista);
            }

            lateral.add(Box.createVerticalGlue());

            for (Component c : lateral.getComponents()) {
                if (c instanceof JComponent) {
                    ((JComponent) c).setAlignmentX(Component.LEFT_ALIGNMENT);
                }
            }

            figura.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
            forma.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

            JScrollPane scroll = new JScrollPane(lateral);
            scroll.setBorder(null);
            scroll.setPreferredSize(new Dimension(300, 600));
            scroll.getVerticalScrollBar().setUnitIncrement(16);
            add(scroll, BorderLayout.WEST);

            entrada = new Vista(this, false);
            salida = new Vista(this, true);

            JPanel vistas = new JPanel(new GridLayout(1, 2, 10, 0));
            vistas.add(tarjeta("Figuras originales", entrada));
            vistas.add(tarjeta("Resultado", salida));
            add(vistas, BorderLayout.CENTER);

            JPanel pie = new JPanel(new GridLayout(0, 1, 0, 4));
            pie.add(estado);

            pie.add(new JLabel(es3D
                    ? "Arrastra para girar · Rueda para zoom · Mueve sólidos con X, Y y Z."
                    : "Selecciona A, B o C y arrástrala en la vista izquierda. X: derecha; Y: abajo."));

            pie.add(new JLabel(es3D
                    ? "3D aproximado por bloques de 5 unidades. El diámetro del toro es el exterior."
                    : "Las operaciones se actualizan al mover o editar las figuras."));

            add(pie, BorderLayout.SOUTH);

            espera = new javax.swing.Timer(100, e -> calcular());
            espera.setRepeats(false);

            figura.addActionListener(e -> {
                if (!cargando) {
                    cargar();
                    repintar();
                }
            });

            forma.addActionListener(e -> {
                if (!cargando) {
                    actual().tipo = forma.getSelectedIndex();
                    cargar();
                    actualizar();
                }
            });

            agregar.addActionListener(e -> {
                activarC(!conC);
                actualizar();
            });

            cargar();
            actualizar();
        }

        JPanel tarjeta(String titulo, JPanel contenido) {
            JPanel p = new JPanel(new BorderLayout());
            p.setBorder(BorderFactory.createTitledBorder(titulo));
            p.add(contenido);
            return p;
        }

        JPanel operaciones(String titulo, int paso) {
            JPanel p = new JPanel(new GridLayout(0, 1, 0, 3));
            p.setBorder(BorderFactory.createTitledBorder(titulo));

            ButtonGroup grupo = new ButtonGroup();
            String[] nombres = {"Unir", "Intersectar", "Restar"};
            String[] ayuda = {
                "Reúne ambas figuras.",
                "Conserva solo la parte común.",
                "Quita la segunda figura de la primera."
            };

            for (int i = 0; i < 3; i++) {
                int n = i;
                JToggleButton b = new JToggleButton(nombres[i], op[paso] == i);
                b.setToolTipText(ayuda[i]);
                grupo.add(b);
                p.add(b);
                botones[paso][i] = b;

                b.addActionListener(e -> {
                    op[paso] = n;
                    actualizar();
                });
            }

            return p;
        }

        Figura actual() {
            return f[Math.max(0, figura.getSelectedIndex())];
        }

        void activarC(boolean activa) {
            cargando = true;
            conC = activa;

            if (activa && figura.getItemCount() == 2) {
                figura.addItem("C (morado)");
            }

            if (!activa && figura.getItemCount() == 3) {
                figura.setSelectedIndex(0);
                figura.removeItemAt(2);
            }

            if (activa) {
                figura.setSelectedIndex(2);
            }

            agregar.setText(activa ? "− Quitar figura C" : "+ Agregar figura C");
            paso2.setVisible(activa);
            cargando = false;
            cargar();
        }

        void cargar() {
            cargando = true;
            Figura a = actual();
            forma.setSelectedIndex(a.tipo);
            campos.removeAll();

            campo(es3D ? "Centro X" : "Posición X", 0, es3D ? -50 : 0, es3D ? 50 : 300);
            campo(es3D ? "Centro Y" : "Posición Y", 1, es3D ? -50 : 0, es3D ? 50 : 200);

            if (es3D) {
                campo("Centro Z", 2, -50, 50);
            }

            String nombre = es3D
                    ? (a.tipo < 2 ? "Ancho / lado" : "Diámetro")
                    : (a.tipo == 1 || a.tipo == 3 ? "Diámetro" : "Ancho");

            campo(nombre, 3, 20, es3D ? 100 : 300);

            if (es3D ? (a.tipo == 1 || a.tipo == 3 || a.tipo == 4)
                    : (a.tipo == 0 || a.tipo == 2)) {
                campo("Altura", 4, 20, es3D ? 100 : 300);
            }

            if (es3D && a.tipo == 1) {
                campo("Profundidad", 5, 20, 100);
            }
            if (!es3D && a.tipo == 3) {
                campo("Lados", 6, 3, 12);
            }

            cargando = false;
            revalidate();
            repaint();
        }

        void campo(String nombre, int indice, int min, int max) {
            Figura a = actual();
            JSpinner s = new JSpinner(
                    new SpinnerNumberModel((int) a.d[indice], min, max, 1)
            );

            ((JSpinner.DefaultEditor) s.getEditor()).getTextField()
                    .setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);

            s.addChangeListener(e -> {
                if (!cargando) {
                    a.d[indice] = ((Number) s.getValue()).doubleValue();
                    actualizar();
                }
            });

            campos.add(new JLabel(nombre));
            campos.add(s);
        }

        Area area() {
            Area r = operar(f[0].forma(), f[1].forma(), op[0]);
            return conC ? operar(r, f[2].forma(), op[1]) : r;
        }

        void repintar() {
            entrada.repaint();
            salida.repaint();
        }

        void actualizar() {
            String s = "A" + SIGNOS[op[0]] + "B";
            formula.setText(conC ? "(" + s + ")" + SIGNOS[op[1]] + "C" : s);

            if (es3D) {
                estado.setText("Calculando volumen…");
                espera.restart();
            } else {
                estado.setText(area().isEmpty()
                        ? "Resultado vacío."
                        : "Verde: resultado · Contorno oscuro: figura seleccionada.");
                repintar();
            }
        }

        void ejemplo() {
            for (int i = 0; i < 3; i++) {
                f[i].tipo = i == 0 ? 0 : (es3D ? (i == 1 ? 2 : 3) : 1);
                f[i].d = es3D
                        ? new double[]{0, 0, 0, i == 0 ? 80 : i == 1 ? 40 : 35, 100, 60, 5}
                        : new double[]{100 + 70 * i, 130 + 25 * i, 0, i == 2 ? 85 : 220, 190, 60, 5};
            }

            op[0] = 0;
            op[1] = 2;
            botones[0][0].setSelected(true);
            botones[1][2].setSelected(true);
            activarC(true);
            actualizar();
        }

        void calcular() {
            byte[] a = new byte[N * N * N], b = new byte[N * N * N];
            int total = 0;

            for (int x = 0; x < N; x++) {
                for (int y = 0; y < N; y++) {
                    for (int z = 0; z < N; z++) {
                        double px = MIN + (x + .5) * PASO;
                        double py = MIN + (y + .5) * PASO;
                        double pz = MIN + (z + .5) * PASO;

                        boolean fa = f[0].contiene(px, py, pz);
                        boolean fb = f[1].contiene(px, py, pz);
                        boolean fc = conC && f[2].contiene(px, py, pz);

                        int i = (x * N + y) * N + z;
                        a[i] = (byte) (fc ? 3 : fb ? 2 : fa ? 1 : 0);

                        boolean r = operar(fa, fb, op[0]);
                        if (conC) {
                            r = operar(r, fc, op[1]);
                        }

                        if (r) {
                            b[i] = 4;
                            total++;
                        }
                    }
                }
            }

            originales = malla(a);
            resultado = malla(b);

            estado.setText(total == 0
                    ? "Resultado vacío en esta resolución."
                    : "Volumen aproximado: " + total * 125L
                    + " unidades cúbicas. Las superficies ocultan el interior.");

            repintar();
        }

        static ArrayList<Cara> malla(byte[] datos) {
            ArrayList<Cara> caras = new ArrayList<>();
            int[][] dir = {
                {-1, 0, 0}, {1, 0, 0}, {0, -1, 0},
                {0, 1, 0}, {0, 0, -1}, {0, 0, 1}
            };

            for (int x = 0; x < N; x++) {
                for (int y = 0; y < N; y++) {
                    for (int z = 0; z < N; z++) {
                        int c = datos[(x * N + y) * N + z];
                        if (c == 0) {
                            continue;
                        }

                        for (int k = 0; k < 6; k++) {
                            int nx = x + dir[k][0];
                            int ny = y + dir[k][1];
                            int nz = z + dir[k][2];

                            if (nx < 0 || ny < 0 || nz < 0
                                    || nx >= N || ny >= N || nz >= N
                                    || datos[(nx * N + ny) * N + nz] == 0) {
                                caras.add(new Cara(
                                        MIN + (x + .5) * PASO,
                                        MIN + (y + .5) * PASO,
                                        MIN + (z + .5) * PASO, k, c - 1
                                ));
                            }
                        }
                    }
                }
            }

            return caras;
        }
    }

    static class Cara {

        final double x, y, z;
        final int lado, color;

        Cara(double x, double y, double z, int lado, int color) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.lado = lado;
            this.color = color;
        }
    }

    static class Vista extends JPanel {

        final Taller t;
        final boolean fin;
        boolean arrastrando;
        int mx, my;
        double dx, dy;

        Vista(Taller t, boolean fin) {
            this.t = t;
            this.fin = fin;
            setBackground(Color.WHITE);

            MouseAdapter mouse = new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    if (!SwingUtilities.isLeftMouseButton(e)) {
                        return;
                    }

                    mx = e.getX();
                    my = e.getY();
                    Point2D p = punto(e);

                    arrastrando = !fin && !t.es3D
                            && t.actual().forma().contains(p);

                    if (arrastrando) {
                        dx = p.getX() - t.actual().d[0];
                        dy = p.getY() - t.actual().d[1];
                    }
                }

                public void mouseDragged(MouseEvent e) {
                    if ((e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) == 0) {
                        return;
                    }

                    if (t.es3D) {
                        t.giro += (e.getX() - mx) * .01;
                        t.inclinacion = Math.max(-1.4, Math.min(
                                1.4, t.inclinacion + (e.getY() - my) * .01
                        ));
                        mx = e.getX();
                        my = e.getY();
                        t.repintar();
                    } else if (arrastrando) {
                        Point2D p = punto(e);
                        t.actual().d[0] = Math.max(
                                0, Math.min(300, Math.round(p.getX() - dx))
                        );
                        t.actual().d[1] = Math.max(
                                0, Math.min(200, Math.round(p.getY() - dy))
                        );
                        t.cargar();
                        t.actualizar();
                    }
                }

                public void mouseReleased(MouseEvent e) {
                    arrastrando = false;
                }

                public void mouseWheelMoved(MouseWheelEvent e) {
                    if (t.es3D) {
                        t.zoom = Math.max(
                                .5, Math.min(3, t.zoom - e.getWheelRotation() * .1)
                        );
                        t.repintar();
                    }
                }
            };

            addMouseListener(mouse);
            addMouseMotionListener(mouse);
            addMouseWheelListener(mouse);
        }

        double escala() {
            return Math.max(.01, Math.min(
                    (getWidth() - 24) / 600.0,
                    (getHeight() - 24) / 500.0
            ));
        }

        Point2D punto(MouseEvent e) {
            double s = escala();
            return new Point2D.Double(
                    (e.getX() - (getWidth() - 600 * s) / 2) / s,
                    (e.getY() - (getHeight() - 500 * s) / 2) / s
            );
        }

        double[] rotar(double x, double y, double z) {
            double a = x * Math.cos(t.giro) - z * Math.sin(t.giro);
            double b = x * Math.sin(t.giro) + z * Math.cos(t.giro);

            return new double[]{
                a,
                y * Math.cos(t.inclinacion) - b * Math.sin(t.inclinacion),
                y * Math.sin(t.inclinacion) + b * Math.cos(t.inclinacion)
            };
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D q = (Graphics2D) g.create();
            q.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            if (t.es3D) {
                pintar3D(q);
            } else {
                pintar2D(q);
            }

            q.dispose();
        }

        void pintar2D(Graphics2D g) {
            double s = escala();
            g.translate((getWidth() - 600 * s) / 2, (getHeight() - 500 * s) / 2);
            g.scale(s, s);
            g.setColor(new Color(232, 237, 242));

            for (int x = 0; x <= 600; x += 50) {
                g.drawLine(x, 0, x, 500);
            }
            for (int y = 0; y <= 500; y += 50) {
                g.drawLine(0, y, 600, y);
            }

            if (fin) {
                g.setColor(COLOR[3]);
                g.fill(t.area());
            } else {
                for (int i = 0; i < (t.conC ? 3 : 2); i++) {
                    Color c = COLOR[i];
                    g.setColor(new Color(
                            c.getRed(), c.getGreen(), c.getBlue(), 100
                    ));
                    g.fill(t.f[i].forma());
                    g.setColor(c);
                    g.draw(t.f[i].forma());
                }

                g.setColor(Color.DARK_GRAY);
                g.setStroke(new BasicStroke(3));
                g.draw(t.actual().forma());
            }
        }

        void pintar3D(Graphics2D g) {
            ArrayList<Cara> caras = new ArrayList<>(
                    fin ? t.resultado : t.originales
            );
            caras.sort(Comparator.comparingDouble(
                    c -> rotar(c.x, c.y, c.z)[2]
            ));

            double s = Math.min(getWidth(), getHeight()) / 220.0 * t.zoom;

            for (Cara c : caras) {
                int eje = c.lado / 2;
                double signo = c.lado % 2 == 0 ? -1 : 1;
                double[] normal = new double[3];
                normal[eje] = signo;

                double frente = rotar(normal[0], normal[1], normal[2])[2];
                if (frente <= 0) {
                    continue;
                }

                Polygon pol = new Polygon();
                int u = (eje + 1) % 3, v = (eje + 2) % 3;

                for (int k = 0; k < 4; k++) {
                    double[] p = {c.x, c.y, c.z};
                    p[eje] += signo * Taller.PASO / 2;
                    p[u] += (k == 0 || k == 3 ? -1 : 1) * Taller.PASO / 2;
                    p[v] += (k < 2 ? -1 : 1) * Taller.PASO / 2;

                    double[] r = rotar(p[0], p[1], p[2]);
                    pol.addPoint(
                            (int) Math.round(getWidth() / 2.0 + r[0] * s),
                            (int) Math.round(getHeight() / 2.0 - r[1] * s)
                    );
                }

                Color b = COLOR[c.color];
                double luz = .58 + .35 * frente;

                g.setColor(new Color(
                        (int) (b.getRed() * luz),
                        (int) (b.getGreen() * luz),
                        (int) (b.getBlue() * luz)
                ));
                g.fillPolygon(pol);
                g.drawPolygon(pol);
            }

            if (caras.isEmpty()) {
                g.setColor(Color.GRAY);
                g.drawString("Sin volumen visible", 20, 30);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(
                        UIManager.getSystemLookAndFeelClassName()
                );
            } catch (Exception ignored) {
            }

            new AreasGeometricasConstructivas().setVisible(true);
        });
    }
}
