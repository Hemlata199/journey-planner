import javax.swing.*;
import java.awt.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;

/** Desktop app for the journey planner. Run: java -cp out App */
public class App extends JFrame {
    private Network net;
    private final Map<String, JCheckBox> closures = new LinkedHashMap<>();

    private final JTextArea routesArea = new JTextArea(12, 22);
    private final JTextField fromField = new JTextField(6);
    private final JTextField toField = new JTextField(6);
    private final JPanel closurePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    private final JTextArea resultArea = new JTextArea(5, 40);

    public App() {
        super("City Journey Planner - Fewest Buses");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // LEFT: network editor
        JPanel left = new JPanel(new BorderLayout(5, 5));
        left.setBorder(BorderFactory.createTitledBorder("Bus network (one route per line, e.g. R1: 1 2 7)"));
        routesArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        routesArea.setText("R1: 1 2 3\nR2: 3 4 5\nR3: 1 9 5\n");
        JButton loadBtn = new JButton("Load network");
        JButton openBtn = new JButton("Open file...");
        JPanel btns = new JPanel(new GridLayout(1, 2, 5, 5));
        btns.add(loadBtn); btns.add(openBtn);
        left.add(new JScrollPane(routesArea), BorderLayout.CENTER);
        left.add(btns, BorderLayout.SOUTH);
        add(left, BorderLayout.WEST);

        // RIGHT: query, closures, result
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));

        JPanel query = new JPanel(new FlowLayout(FlowLayout.LEFT));
        query.setBorder(BorderFactory.createTitledBorder("Journey"));
        JButton findBtn = new JButton("Find journey");
        query.add(new JLabel("From stop:")); query.add(fromField);
        query.add(new JLabel("To stop:")); query.add(toField);
        query.add(findBtn);

        closurePanel.setBorder(BorderFactory.createTitledBorder("Closed routes (tick to disable)"));

        resultArea.setEditable(false);
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        resultArea.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        JScrollPane resultScroll = new JScrollPane(resultArea);
        resultScroll.setBorder(BorderFactory.createTitledBorder("Result"));

        right.add(query); right.add(closurePanel); right.add(resultScroll);
        add(right, BorderLayout.CENTER);

        loadBtn.addActionListener(e -> loadNetwork());
        openBtn.addActionListener(e -> openFile());
        findBtn.addActionListener(e -> runQuery());
        toField.addActionListener(e -> runQuery());
        fromField.addActionListener(e -> runQuery());

        loadNetwork();
        pack();
        setMinimumSize(new Dimension(760, 380));
        setLocationRelativeTo(null);
    }

    private void loadNetwork() {
        Map<String, List<String>> routes = new LinkedHashMap<>();
        for (String line : routesArea.getText().split("\\R")) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            String[] p = line.split(":", 2);
            if (p.length < 2 || p[1].trim().isEmpty()) {
                resultArea.setText("Bad line (use 'R1: 1 2 7'): " + line);
                return;
            }
            routes.put(p[0].trim(), Arrays.asList(p[1].trim().split("[\\s,]+")));
        }
        net = new Network(routes);          // index built once here
        closurePanel.removeAll();
        closures.clear();
        for (String r : routes.keySet()) {
            JCheckBox cb = new JCheckBox(r);
            cb.addActionListener(e -> { if (!fromField.getText().isBlank()) runQuery(); });
            closures.put(r, cb);
            closurePanel.add(cb);
        }
        closurePanel.revalidate(); closurePanel.repaint();
        resultArea.setText("Loaded " + routes.size() + " routes. Enter two stops and click Find journey.");
    }

    private void openFile() {
        JFileChooser fc = new JFileChooser(".");
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                routesArea.setText(Files.readString(fc.getSelectedFile().toPath()));
                loadNetwork();
            } catch (Exception ex) {
                resultArea.setText("Could not read file: " + ex.getMessage());
            }
        }
    }

    private void runQuery() {
        String from = fromField.getText().trim(), to = toField.getText().trim();
        if (from.isEmpty() || to.isEmpty()) { resultArea.setText("Please enter both stops."); return; }
        Set<String> inactive = new HashSet<>();
        closures.forEach((r, cb) -> { if (cb.isSelected()) inactive.add(r); });
        Network.Result res = net.query(from, to, inactive);
        resultArea.setText(res == null ? "No journey exists" : res.toString());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new App().setVisible(true));
    }
}
