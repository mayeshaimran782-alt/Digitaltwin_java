import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class Main extends JFrame {

    JPanel chatPanel;
    JTextField inputField;
    JLabel predictionLabel;
    JButton sendButton;

    CreativeDigitalTwin twin = new CreativeDigitalTwin();

    Color bgDark   = new Color(24, 24, 36);
    Color bgLight  = new Color(40, 40, 55);
    Color userColor = new Color(60, 125, 240);
    Color botColor  = new Color(220, 220, 230);
    Color accent    = new Color(100, 210, 255);

    public Main() {
        super("Creative Digital Twin");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(420, 720);
        setLocationRelativeTo(null);

        UIManager.put("Panel.background", bgDark);
        UIManager.put("Button.background", accent);
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("TextField.background", bgLight);
        UIManager.put("TextField.foreground", Color.WHITE);

        setLayout(new BorderLayout());
        getContentPane().setBackground(bgDark);

        chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setBackground(bgDark);

        JScrollPane scroll = new JScrollPane(chatPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(bgDark);
        bottom.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        predictionLabel = new JLabel("  ");
        predictionLabel.setForeground(Color.LIGHT_GRAY);
        predictionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        bottom.add(predictionLabel, BorderLayout.NORTH);

        inputField = new JTextField();
        inputField.setForeground(Color.WHITE);
        inputField.setBackground(bgLight);
        inputField.setCaretColor(Color.WHITE);
        inputField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        inputField.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 8));

        sendButton = new JButton("Send");
        sendButton.setForeground(new Color(20, 20, 40));
        sendButton.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));

        bottom.add(inputField, BorderLayout.CENTER);
        bottom.add(sendButton, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);

        scroll.getVerticalScrollBar().setBackground(bgDark);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        sendButton.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());
        inputField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                predictionLabel.setText("💭 " + twin.estimateNext());
            }
        });

        // Opening message
        SwingUtilities.invokeLater(() ->
            addBubble("I'm starting to mirror your mind, like a quiet, curious reflection.", false)
        );

        setVisible(true);
    }

    void sendMessage() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;
        addBubble(text, true);
        String reply = twin.respond(text);
        addBubble(reply, false, true);
        inputField.setText("");
    }

    void addBubble(String msg, boolean user) { addBubble(msg, user, false); }

    void addBubble(String msg, boolean user, boolean autoScroll) {
        JPanel panel = new JPanel(new FlowLayout(user ? FlowLayout.RIGHT : FlowLayout.LEFT, 6, 2));
        panel.setBackground(bgDark);
        panel.setOpaque(false);

        JLabel bubble = new JLabel("<html><p style='width:190px'>" + msg + "</p></html>");
        bubble.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        bubble.setOpaque(true);
        bubble.setBackground(user ? userColor : botColor);
        bubble.setForeground(user ? Color.WHITE : new Color(30, 30, 50));
        bubble.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        panel.add(bubble);
        chatPanel.add(panel);
        chatPanel.revalidate();

        if (autoScroll) {
            SwingUtilities.invokeLater(() -> {
                JScrollPane sp = (JScrollPane) ((JPanel) getContentPane().getComponent(0)).getParent();
                // safer: walk up to find the scroll pane
                Container c = chatPanel.getParent();
                while (c != null && !(c instanceof JScrollPane)) c = c.getParent();
                if (c instanceof JScrollPane) {
                    JScrollBar bar = ((JScrollPane) c).getVerticalScrollBar();
                    bar.setValue(bar.getMaximum());
                }
            });
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);
    }
}

// ================== CREATIVE DIGITAL TWIN ==================

class CreativeDigitalTwin {

    double biasProcrastination = 0.5;
    double biasHesitation      = 0.5;
    double riskTolerance       = 0.5;
    boolean initialized        = false;

    Map<String, Integer> wordCount  = new HashMap<>();
    java.util.List<String> recentSamples = new ArrayList<>();
    double learningRate = 0.1;

    public String respond(String input) {
        updateModelFrom(input.toLowerCase());
        initialized = true;

        return insight() + "<br><br>" +
               narrative(input) + "<br><br>" +
               advice(input) + "<br><br>" +
               futurePaths(input);
    }

    void updateModelFrom(String input) {
        for (String w : input.split("\\s+"))
            wordCount.merge(w, 1, Integer::sum);

        if (input.contains("later")) {
            biasProcrastination = Math.min(1.0, biasProcrastination + learningRate);
            biasHesitation      = Math.min(1.0, biasHesitation      + learningRate);
        }
        if (input.contains("idk") || input.contains("unsure"))
            biasHesitation = Math.min(1.0, biasHesitation + learningRate);
        if (input.contains("afraid") || input.contains("scared") || input.contains("risk"))
            riskTolerance = Math.max(0.0, riskTolerance - learningRate);
        if (input.contains("feel") || input.contains("stressed"))
            biasHesitation = Math.min(1.0, biasHesitation + learningRate);
        if (input.contains("start") || input.contains("now") || input.contains("go")) {
            biasProcrastination = Math.max(0.0, biasProcrastination - learningRate);
            riskTolerance       = Math.min(1.0, riskTolerance       + learningRate);
        }

        recentSamples.add(input);
        if (recentSamples.size() > 7) recentSamples.remove(0);
    }

    String insight() {
        if (biasProcrastination > 0.7)
            return "You let many ideas sit in the launch bay. You build castles in the planning stage, " +
                   "then quietly forget to cross the bridge.";
        if (biasProcrastination < 0.3)
            return "You're the first foot out of the door. You start before you fully decide; " +
                   "your momentum becomes your thinking.";
        if (biasHesitation > 0.7)
            return "You replay your moves before you make them. You draft messages, " +
                   "imagine consequences, then rewrite again and again.";
        if (riskTolerance < 0.3)
            return "You treat risk like a cliff edge. You prefer the flat, safe land, " +
                   "even if it means slower growth.";
        if (riskTolerance > 0.7)
            return "You lean toward the edge. You're willing to jump into the unknown " +
                   "if it feels like expansion.";
        return "You're still negotiating with yourself. A bit of pause, a bit of delay, " +
               "a bit of courage — you're in the middle of your own calibration.";
    }

    String narrative(String input) {
        input = input.toLowerCase();
        if (input.contains("exam") || input.contains("study"))
            return "Imagine your exam as a narrow door at the end of a hallway. " +
                   "You keep walking back and forth, checking the lock, thinking of scenarios, " +
                   "until the key turns by itself, and you're already through.";
        if (input.contains("friend") || input.contains("text") || input.contains("message"))
            return "You hold each message like a fragile bird. You adjust every word, " +
                   "test its weight, and sometimes let it fly before you even decide. " +
                   "Other times, you close your hand and it vanishes.";
        if (input.contains("job") || input.contains("career"))
            return "You see your future as a path split into many branches. " +
                   "You stand at the fork, reading every sign, imagining every consequence. " +
                   "You don't always notice how far you've walked already.";
        return "Your thoughts loop like waves against a pier. Some ideas crash loudly, " +
               "some fade into the water, some leave a small mark on the wood.";
    }

    String advice(String input) {
        input = input.toLowerCase();
        if (!input.contains("should"))
            return "You're still mapping the territory. No decision is pressed yet.";

        double costAct   = (1.0 - riskTolerance) * 0.4;
        double costDelay = biasProcrastination   * 0.8;
        double costAvoid = 0.9;

        double[] costs  = {costAct, costDelay, costAvoid};
        String[] labels = {"Act", "Delay", "Avoid"};
        int best = 0;
        for (int i = 1; i < 3; i++) if (costs[i] < costs[best]) best = i;

        String ad = "If you see choices as doors:<br>" +
                    "A: 'Act' → cost "   + String.format("%.2f", costAct)   + "<br>" +
                    "B: 'Delay' → cost " + String.format("%.2f", costDelay) + "<br>" +
                    "C: 'Avoid' → cost " + String.format("%.2f", costAvoid) + "<br>" +
                    "Your mind leans toward: <b>" + labels[best] + "</b><br><br>";

        if (labels[best].equals("Act"))
            return ad + "Try opening the door now. The first step will echo the loudest, " +
                        "then everything gets quieter and clearer.";
        if (labels[best].equals("Delay"))
            return ad + "You're allowed to wait a little. But remember: every extra minute " +
                        "outside the door adds invisible weight to your shoulders.";
        return ad + "Avoiding it is a choice. Just know that this choice will echo " +
                    "as a quiet story you tell yourself later.";
    }

    String futurePaths(String input) {
        double scoreAct   = (1.0 - biasProcrastination) * (riskTolerance + 0.5);
        double scoreDelay = (1.0 - riskTolerance) * 0.7;
        double scoreAvoid = biasProcrastination * 0.3;

        return "Possible futures unfolding:<br>" +
               "• <b>Act</b> → score " + String.format("%.2f", scoreAct) + "<br>" +
               "  You practice the skill, tolerate the discomfort, and confidence grows.<br>" +
               "• <b>Delay</b> → score " + String.format("%.2f", scoreDelay) + "<br>" +
               "  You feel safe in the moment, but the tension slowly turns into a small scar.<br>" +
               "• <b>Avoid</b> → score " + String.format("%.2f", scoreAvoid) + "<br>" +
               "  You step away, and this decision becomes a quiet, recurring regret.";
    }

    String estimateNext() {
        int laterCount = wordCount.getOrDefault("later", 0);
        int total = wordCount.values().stream().mapToInt(Integer::intValue).sum();
        if (total == 0) return "I'm listening to your rhythm.";
        double laterFreq = (double) laterCount / total;
        if (laterFreq > 0.15)
            return "You might say 'later' again — a small promise you keep making to tomorrow.";
        if (laterFreq > 0.05)
            return "You might delay this. Your thoughts are still stretching it out.";
        return "Your pattern is shifting. Maybe this time, you'll say 'now'.";
    }
}