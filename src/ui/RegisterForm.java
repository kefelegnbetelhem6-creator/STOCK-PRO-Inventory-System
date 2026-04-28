package ui;

import db.DBConnection;
import util.Style;
import util.PasswordUtil; // ⭐ አዲሱ import
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.regex.Pattern;

public class RegisterForm extends JFrame {

    JTextField usernameField, emailField;
    JPasswordField passwordField, confirmField;

    Color bgMain = new Color(26, 26, 46);   
    Color accentColor = new Color(78, 204, 163); 

    public RegisterForm() {
        setTitle("Inventory System | Register");
        setSize(450, 500);
        setLocationRelativeTo(null);
        setLayout(null);
        setResizable(false);

        getContentPane().setBackground(bgMain);

        JLabel title = new JLabel("CREATE ACCOUNT", SwingConstants.CENTER);
        title.setBounds(0, 20, 450, 40);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(accentColor);
        add(title);

        int labelX = 60;
        int fieldX = 170;
        int width = 200;

        addLabel("Username:", labelX, 90);
        usernameField = createField(fieldX, 90, width);

        addLabel("Email Address:", labelX, 140);
        emailField = createField(fieldX, 140, width);

        addLabel("Password:", labelX, 190);
        passwordField = createPassField(fieldX, 190, width);

        addLabel("Confirm Pass:", labelX, 240);
        confirmField = createPassField(fieldX, 240, width);

        JButton registerBtn = new JButton("REGISTER");
        registerBtn.setBounds(100, 310, 250, 40);
        registerBtn.setBackground(accentColor);
        registerBtn.setForeground(bgMain);
        registerBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        registerBtn.setFocusPainted(false);
        registerBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        add(registerBtn);

        JLabel back = new JLabel("Already have an account? Login here", SwingConstants.CENTER);
        back.setBounds(0, 370, 450, 25);
        back.setForeground(new Color(0, 150, 255));
        back.setCursor(new Cursor(Cursor.HAND_CURSOR));
        add(back);

        registerBtn.addActionListener(e -> registerUser());

        back.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                new LoginForm();
                dispose();
            }
        });

        setVisible(true);
    }

    private void addLabel(String text, int x, int y) {
        JLabel lbl = new JLabel(text);
        lbl.setBounds(x, y, 100, 25);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        add(lbl);
    }

    private JTextField createField(int x, int y, int w) {
        JTextField f = new JTextField();
        f.setBounds(x, y, w, 35);
        f.setBackground(new Color(45, 45, 70));
        f.setForeground(Color.WHITE);
        f.setCaretColor(Color.WHITE);
        f.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        add(f);
        return f;
    }

    private JPasswordField createPassField(int x, int y, int w) {
        JPasswordField f = new JPasswordField();
        f.setBounds(x, y, w, 35);
        f.setBackground(new Color(45, 45, 70));
        f.setForeground(Color.WHITE);
        f.setCaretColor(Color.WHITE);
        f.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        add(f);
        return f;
    }

    void registerUser() {
        String user = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String pass = new String(passwordField.getPassword()).trim();
        String confirm = new String(confirmField.getPassword()).trim();

        if (user.isEmpty() || email.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields!", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!Pattern.matches("^[A-Za-z0-9+_.-]+@(.+)$", email)) {
            JOptionPane.showMessageDialog(this, "Invalid Email Address!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!pass.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Connection con = DBConnection.getConnection();

            String checkSql = "SELECT * FROM users WHERE username=? OR email=?";
            PreparedStatement checkPs = con.prepareStatement(checkSql);
            checkPs.setString(1, user);
            checkPs.setString(2, email);
            ResultSet rs = checkPs.executeQuery();

            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "Username or Email already exists!");
                return;
            }

            // ⭐ ፓስወርዱን Hash የማድረጊያ መስመር (Security Upgrade)
            String hashedPassword = PasswordUtil.hashPassword(pass);

            String sql = "INSERT INTO users(username, email, password, role) VALUES(?,?,?,?)";
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, user);
            ps.setString(2, email);
            ps.setString(3, hashedPassword); // አሁን የሚገባው የማይነበበው ኮድ ነው
            ps.setString(4, "user");

            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Account created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

            new LoginForm();
            dispose();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
        }
    }
}