package ui;

import util.Style;
import javax.swing.*;
import java.awt.*;

public class SplashScreen extends JWindow {
    private JProgressBar progressBar;

    public SplashScreen() {
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Style.BG_DARK);
        content.setBorder(BorderFactory.createLineBorder(Style.ACCENT, 2));

        // Logo/Title Area
        JLabel label = new JLabel("STOCK PRO ENTERPRISE", SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 30));
        label.setForeground(Style.ACCENT);
        content.add(label, BorderLayout.CENTER);

        // Progress Bar
        progressBar = new JProgressBar(0, 100);
        progressBar.setForeground(Style.ACCENT);
        progressBar.setBackground(new Color(30, 30, 50));
        progressBar.setBorderPainted(false);
        progressBar.setPreferredSize(new Dimension(0, 5));
        content.add(progressBar, BorderLayout.SOUTH);

        setContentPane(content);
        setSize(500, 300);
        setLocationRelativeTo(null);
        setVisible(true);

        // ለ 3 ሰከንድ እንዲቆይ ማድረግ
        try {
            for (int i = 0; i <= 100; i++) {
                Thread.sleep(30); // ፍጥነቱ
                progressBar.setValue(i);
            }
        } catch (Exception e) {}
        
        dispose(); 
        new LoginForm(); 
    }
}