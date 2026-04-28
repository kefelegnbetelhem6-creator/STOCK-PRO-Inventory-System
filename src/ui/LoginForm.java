package ui;

import db.DBConnection;
import util.Style;
import util.PasswordUtil;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class LoginForm extends JFrame {

    JTextField emailField; 
    JPasswordField passwordField;
    JCheckBox showPassword;

    public LoginForm() {
        setTitle("STOCK PRO | LOGIN");
        setSize(450, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        // --- MAIN BACKGROUND PANEL ---
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(Style.BG_DARK);
        mainPanel.setLayout(null);
        add(mainPanel);

        // --- TOP BRANDING SECTION ---
        JLabel iconLabel = new JLabel("🔒", SwingConstants.CENTER); // ቁልፍ አይኮን
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 70));
        iconLabel.setForeground(Style.ACCENT);
        iconLabel.setBounds(0, 40, 450, 80);
        mainPanel.add(iconLabel);

        JLabel title = new JLabel("INVENTORY LOGIN", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(Color.WHITE);
        title.setBounds(0, 120, 450, 40);
        mainPanel.add(title);

        // --- LOGIN CARD ---
        JPanel card = new JPanel();
        card.setBackground(Style.SIDEBAR_BG);
        card.setBounds(40, 180, 370, 330);
        card.setLayout(null);
        card.setBorder(BorderFactory.createLineBorder(new Color(50, 50, 70), 1));
        mainPanel.add(card);

        // Email Section
        JLabel eLabel = new JLabel("Email Address");
        eLabel.setForeground(new Color(180, 180, 180));
        eLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        eLabel.setBounds(35, 30, 150, 20);
        card.add(eLabel);

        emailField = new JTextField();
        styleField(emailField);
        emailField.setBounds(35, 55, 300, 40);
        card.add(emailField);

        // Password Section
        JLabel pLabel = new JLabel("Password");
        pLabel.setForeground(new Color(180, 180, 180));
        pLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pLabel.setBounds(35, 110, 100, 20);
        card.add(pLabel);

        passwordField = new JPasswordField();
        styleField(passwordField);
        passwordField.setBounds(35, 135, 300, 40);
        card.add(passwordField);

        // Show Password Checkbox
        showPassword = new JCheckBox("Show Password");
        showPassword.setBackground(Style.SIDEBAR_BG);
        showPassword.setForeground(Color.GRAY);
        showPassword.setFocusPainted(false);
        showPassword.setBounds(35, 185, 150, 20);
        showPassword.addActionListener(e -> {
            if (showPassword.isSelected()) passwordField.setEchoChar((char) 0);
            else passwordField.setEchoChar('●');
        });
        card.add(showPassword);

        // Login Button
        JButton loginBtn = new JButton("LOGIN");
        loginBtn.setBounds(35, 225, 300, 45);
        loginBtn.setBackground(Style.ACCENT);
        loginBtn.setForeground(Color.BLACK);
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginBtn.setFocusPainted(false);
        loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.add(loginBtn);

        // Create Account Link
        JLabel createLink = new JLabel("Don't have an account? Create one.", SwingConstants.CENTER);
        createLink.setForeground(new Color(36, 198, 220)); // ሰማያዊ ቀለም
        createLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        createLink.setBounds(0, 285, 370, 25);
        card.add(createLink);

        // --- ACTIONS & LOGIC ---
        loginBtn.addActionListener(e -> loginUser());

        // ENTER ቁልፍ ሲጫን LOGIN እንዲያደርግ
        KeyAdapter enterKey = new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) loginUser();
            }
        };
        emailField.addKeyListener(enterKey);
        passwordField.addKeyListener(enterKey);

        createLink.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                new RegisterForm();
                dispose();
            }
        });

        setVisible(true);
    }

    private void styleField(JTextField field) {
        field.setBackground(new Color(30, 30, 50));
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 80)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
    }

    void loginUser() {
        String email = emailField.getText().trim();
        String passRaw = new String(passwordField.getPassword()).trim();

        if (email.isEmpty() || passRaw.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your credentials!", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // ⭐ ፓስወርዱን Hash አድርጎ ከዳታቤዝ ጋር ማነጻጸር
        String hashedInput = PasswordUtil.hashPassword(passRaw);

        try {
            Connection con = DBConnection.getConnection();
            String sql = "SELECT * FROM users WHERE email=? AND password=?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, email);
            ps.setString(2, hashedInput);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");
                String user = rs.getString("username");

                // ⭐ ሚናን ለይቶ ወደ ትክክለኛው ዳሽቦርድ መላክ
                if (role.equalsIgnoreCase("admin")) {
                    new AdminDashboard(user);
                } else {
                    new UserDashboard(user);
                }
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Email or Password!", "Access Denied", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
        }
    }
}