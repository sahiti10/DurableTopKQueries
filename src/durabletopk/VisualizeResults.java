package durabletopk;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;

public class VisualizeResults extends JPanel {
    private final Map<String, Integer> runtimeMap = new LinkedHashMap<>();
    private final Map<String, Integer> memoryMap = new LinkedHashMap<>();

    public VisualizeResults(String csvFile) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(csvFile));
        String line = reader.readLine(); 

        while ((line = reader.readLine()) != null) {
            String[] tokens = line.split(",");

            String algoName = tokens[0];
            if (!algoName.contains("k=10")) continue; 

            int runtime = Integer.parseInt(tokens[2]);
            int memory = Integer.parseInt(tokens[3]);

            runtimeMap.put(algoName, Math.max(1, runtime));
            memoryMap.put(algoName, Math.max(1, memory));
        }

        reader.close();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (runtimeMap.isEmpty()) {
            g.drawString("No results to display. Check dataset filter or CSV file.", 50, 100);
            return;
        }

        int width = getWidth() - 100;
        int height = getHeight() - 150;
        int x = 50;
        int barCount = runtimeMap.size();
        if (barCount == 0) return;

        int maxRuntime = runtimeMap.values().stream().max(Integer::compareTo).orElse(1);
        int maxMemory = memoryMap.values().stream().max(Integer::compareTo).orElse(1);
        int barWidth = (width / barCount) / 2;

        int i = 0;
        for (String key : runtimeMap.keySet()) {
            int runtimeBar = (int) ((runtimeMap.get(key) / (double) maxRuntime) * height);
            int memoryBar = (int) ((memoryMap.get(key) / (double) maxMemory) * height);

            int barX = x + i * barWidth * 2;

            g.setColor(new Color(100, 149, 237));
            g.fillRect(barX, height - runtimeBar + 50, barWidth - 5, runtimeBar);
            g.setColor(Color.BLACK);
            g.drawString(runtimeMap.get(key) + " ms", barX, height - runtimeBar + 40);

            g.setColor(new Color(144, 238, 144));
            g.fillRect(barX + barWidth, height - memoryBar + 50, barWidth - 5, memoryBar);
            g.setColor(Color.BLACK);
            g.drawString(memoryMap.get(key) + " MB", barX + barWidth, height - memoryBar + 40);

            g.drawString(key, barX, height + 70);
            i++;
        }

        g.drawString("Blue: Runtime | Green: Memory Usage", x, height + 90);
    }

    public static void main(String[] args) throws IOException {
        JFrame frame = new JFrame("AR(1) Durable Top-k Visualization");
        VisualizeResults chart = new VisualizeResults("results_summary.csv");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 650);
        frame.add(chart);
        frame.setVisible(true);
    }
}
