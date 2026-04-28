/**
 * Project: STOCK PRO - Enterprise Inventory System
 * Module: New Product Entry (with Data Validation)
 * Security: Administrator Access Only
 * Author: Beta
 */

package ui;

import db.DBConnection;
import util.Style;
import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class AddProduct extends JFrame {
    JTextField nameField, qtyField, priceField, minStockField;
    String currentRole, currentUser;

    public AddProduct(String role, String username) {
        this.currentRole = role;
        this.currentUser = username;

        setTitle("STOCK PRO | NEW PRODUCT ENTRY - [" + username.toUpperCase() + "]");
        setSize(550, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(Style.BG_DARK);
        setLayout(new BorderLayout());

        // --- 1. HEADER AREA ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(22, 22, 38));
        headerPanel.setPreferredSize(new Dimension(550, 100));
        
        JLabel headerTitle = new JLabel("ADD NEW INVENTORY ITEM", SwingConstants.CENTER);
        headerTitle.setFont(Style.TITLE_FONT);
        headerTitle.setForeground(Style.ACCENT);
        headerPanel.add(headerTitle, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // --- 2. CENTER CONTENT (The Entry Card) ---
        JPanel cardContainer = new JPanel(null);
        cardContainer.setBackground(Style.BG_DARK);
        
        JPanel card = new JPanel(null);
        card.setBounds(50, 20, 430, 430);
        card.setBackground(Style.SIDEBAR_BG);
        card.setBorder(BorderFactory.createLineBorder(new Color(50, 50, 70), 1));

        int labelX = 40, fieldX = 180, width = 210, height = 35;
        
        // Product Name
        addLabelToCard(card, "Product Name:", labelX, 40);
        nameField = createField(fieldX, 40, width, height);
        card.add(nameField);

        // Quantity
        addLabelToCard(card, "Initial Quantity:", labelX, 110);
        qtyField = createField(fieldX, 110, width, height);
        card.add(qtyField);

        // Unit Price
        addLabelToCard(card, "Unit Price ($):", labelX, 180);
        priceField = createField(fieldX, 180, width, height);
        card.add(priceField);

        // Min Stock Level
        addLabelToCard(card, "Min Stock Level:", labelX, 250);
        minStockField = createField(fieldX, 250, width, height);
        minStockField.setText("5");
        card.add(minStockField);

        // Save Button (Inside Card)
        JButton saveBtn = new JButton("SAVE TO INVENTORY");
        saveBtn.setBounds(40, 340, 350, 45);
        saveBtn.setBackground(Style.ACCENT);
        saveBtn.setForeground(Color.BLACK);
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        saveBtn.setFocusPainted(false);
        saveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.add(saveBtn);

        cardContainer.add(card);
        add(cardContainer, BorderLayout.CENTER);

        // --- 3. BOTTOM AREA (Back Button) ---
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setBackground(Style.BG_DARK);
        footer.setPreferredSize(new Dimension(550, 80));

        JButton backBtn = new JButton("← Back to Dashboard");
        backBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        backBtn.setBackground(Style.BG_DARK);
        backBtn.setForeground(Color.GRAY);
        backBtn.setBorder(null);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        footer.add(backBtn);
        
        add(footer, BorderLayout.SOUTH);

        // --- ACTIONS ---
        saveBtn.addActionListener(e -> saveProduct());
        
        backBtn.addActionListener(e -> {
            if (currentRole.equalsIgnoreCase("admin")) new AdminDashboard(currentUser);
            else new UserDashboard(currentUser);
            dispose();
        });

        setVisible(true);
    }

    private void addLabelToCard(JPanel card, String text, int x, int y) {
        JLabel l = new JLabel(text);
        l.setBounds(x, y, 130, 25);
        l.setForeground(Color.WHITE);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        card.add(l);
    }

    private JTextField createField(int x, int y, int w, int h) {
        JTextField f = new JTextField();
        f.setBounds(x, y, w, h);
        f.setBackground(new Color(45, 45, 65));
        f.setForeground(Color.WHITE);
        f.setCaretColor(Color.WHITE);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(70, 70, 90)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        return f;
    }

    private void saveProduct() {
        // 1. Validation
        String name = nameField.getText().trim();
        String qtyStr = qtyField.getText().trim();
        String priceStr = priceField.getText().trim();
        String minStr = minStockField.getText().trim();

        if (name.isEmpty() || qtyStr.isEmpty() || priceStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int qty = Integer.parseInt(qtyStr);
            double price = Double.parseDouble(priceStr);
            int minStock = Integer.parseInt(minStr);

            if (qty < 0 || price < 0) {
                JOptionPane.showMessageDialog(this, "Numbers cannot be negative!");
                return;
            }

            // 2. Database Operation
            try (Connection con = DBConnection.getConnection()) {
                String sql = "INSERT INTO products(name, quantity, price, min_stock) VALUES(?,?,?,?)";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setString(1, name);
                ps.setInt(2, qty);
                ps.setDouble(3, price);
                ps.setInt(4, minStock);

                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Product successfully registered!", "Success", JOptionPane.INFORMATION_MESSAGE);
                
                // Reset fields
                nameField.setText("");
                qtyField.setText("");
                priceField.setText("");
                minStockField.setText("5");
                nameField.requestFocus();
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for Quantity and Price!", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage());
        }
    }
}