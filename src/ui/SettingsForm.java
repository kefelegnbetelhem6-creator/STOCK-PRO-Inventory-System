package ui;

import db.DBConnection;
import util.Style;
import util.PasswordUtil;
import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class SettingsForm extends JFrame {

    JPasswordField currentPass, newPass, confirmPass;
    JCheckBox showPass;
    String username;

    public SettingsForm(String username) {
        this.username = username;

        setTitle("STOCK PRO | ACCOUNT SECURITY");
        setSize(450, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(Style.BG_DARK);
        mainPanel.setLayout(new BorderLayout());
        add(mainPanel);

        // --- 1. HEADER ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(22, 22, 38));
        header.setPreferredSize(new Dimension(450, 80));
        
        JLabel title = new JLabel("CHANGE PASSWORD", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Style.ACCENT);
        header.add(title, BorderLayout.CENTER);
        mainPanel.add(header, BorderLayout.NORTH);

        // --- 2. FORM CARD ---
        JPanel card = new JPanel(new GridLayout(7, 1, 5, 5));
        card.setBackground(Style.SIDEBAR_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(30, 40, 30, 40),
            BorderFactory.createLineBorder(new Color(50, 50, 70))
        ));

        currentPass = createPassField();
        newPass = createPassField();
        confirmPass = createPassField();

        showPass = new JCheckBox("Show Passwords");
        showPass.setBackground(Style.SIDEBAR_BG);
        showPass.setForeground(Color.GRAY);
        showPass.setFocusPainted(false);
        showPass.addActionListener(e -> {
            char echo = showPass.isSelected() ? (char) 0 : '●';
            currentPass.setEchoChar(echo);
            newPass.setEchoChar(echo);
            confirmPass.setEchoChar(echo);
        });

        card.add(createLabel("Current Password:"));
        card.add(currentPass);
        card.add(createLabel("New Password:"));
        card.add(newPass);
        card.add(createLabel("Confirm New Password:"));
        card.add(confirmPass);
        card.add(showPass);

        mainPanel.add(card, BorderLayout.CENTER);

        // --- 3. BOTTOM BUTTONS ---
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        btnPanel.setBackground(Style.BG_DARK);

        JButton updateBtn = new JButton("UPDATE PASSWORD");
        updateBtn.setPreferredSize(new Dimension(180, 40));
        updateBtn.setBackground(Style.ACCENT);
        updateBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JButton cancelBtn = new JButton("CANCEL");
        cancelBtn.setPreferredSize(new Dimension(120, 40));
        cancelBtn.setBackground(new Color(60, 60, 70));
        cancelBtn.setForeground(Color.WHITE);

        btnPanel.add(updateBtn);
        btnPanel.add(cancelBtn);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);

        // ACTIONS
        updateBtn.addActionListener(e -> updatePassword());
        cancelBtn.addActionListener(e -> dispose());

        setVisible(true);
    }

    private JLabel createLabel(String t) {
        JLabel l = new JLabel(t);
        l.setForeground(Color.WHITE);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return l;
    }

    private JPasswordField createPassField() {
        JPasswordField f = new JPasswordField();
        f.setBackground(new Color(45, 45, 65));
        f.setForeground(Color.WHITE);
        f.setCaretColor(Color.WHITE);
        f.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        return f;
    }

    private void updatePassword() {
        String current = new String(currentPass.getPassword());
        String next = new String(newPass.getPassword());
        String confirm = new String(confirmPass.getPassword());

        if (current.isEmpty() || next.isEmpty() || confirm.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields!");
            return;
        }

        if (!next.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "New passwords do not match!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            // 1. የድሮውን ፓስወርድ Hash አድርጎ መመርመር
            String hashedCurrent = PasswordUtil.hashPassword(current);
            String checkSql = "SELECT * FROM users WHERE username=? AND password=?";
            PreparedStatement psCheck = con.prepareStatement(checkSql);
            psCheck.setString(1, username);
            psCheck.setString(2, hashedCurrent);
            ResultSet rs = psCheck.executeQuery();

            if (!rs.next()) {
                JOptionPane.showMessageDialog(this, "Current password is incorrect!", "Security Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 2. አዲሱን ፓስወርድ Hash አድርጎ መመዝገብ
            String hashedNext = PasswordUtil.hashPassword(next);
            String updateSql = "UPDATE users SET password=? WHERE username=?";
            PreparedStatement psUpdate = con.prepareStatement(updateSql);
            psUpdate.setString(1, hashedNext);
            psUpdate.setString(2, username);
            psUpdate.executeUpdate();

            JOptionPane.showMessageDialog(this, "Password Updated Successfully!");
            dispose();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
        }
    }
}