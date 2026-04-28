package ui;

import db.DBConnection;
import util.Style;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.sql.*;

public class SalesForm extends JFrame {
    JComboBox<String> productCombo, customerCombo;
    JTextField qtyField;
    JLabel priceLabel, stockLabel, totalLabel;
    double unitPrice = 0.0;
    int availableStock = 0;
    String currentRole, currentUser;

    public SalesForm(String role, String user) {
        this.currentRole = role;
        this.currentUser = user;

        setTitle("STOCK PRO | SALES TERMINAL");
        setSize(550, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(Style.BG_DARK);
        setLayout(new BorderLayout());

        // --- 1. HEADER ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(22, 22, 38));
        headerPanel.setPreferredSize(new Dimension(550, 80));
        JLabel headerTitle = new JLabel("NEW TRANSACTION", SwingConstants.CENTER);
        headerTitle.setFont(Style.TITLE_FONT);
        headerTitle.setForeground(Style.ACCENT);
        headerPanel.add(headerTitle, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // --- 2. FORM CARD ---
        JPanel card = new JPanel(new GridLayout(10, 1, 10, 5));
        card.setBackground(Style.SIDEBAR_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(20, 50, 20, 50),
            BorderFactory.createLineBorder(new Color(50, 50, 70))
        ));

        customerCombo = createStyledCombo();
        productCombo = createStyledCombo();
        qtyField = new JTextField();
        styleField(qtyField);

        stockLabel = new JLabel("Available Stock: 0");
        stockLabel.setForeground(Color.GRAY);
        priceLabel = new JLabel("Unit Price: $0.00");
        priceLabel.setForeground(Color.GRAY);
        totalLabel = new JLabel("TOTAL: $0.00");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        totalLabel.setForeground(Color.ORANGE);
        totalLabel.setHorizontalAlignment(SwingConstants.CENTER);

        card.add(createLabel("Select Customer:"));
        card.add(customerCombo);
        card.add(createLabel("Select Product:"));
        card.add(productCombo);
        card.add(stockLabel);
        card.add(createLabel("Quantity:"));
        card.add(qtyField);
        card.add(priceLabel);
        card.add(new JLabel(" "));
        card.add(totalLabel);
        add(card, BorderLayout.CENTER);

        // --- 3. BUTTONS ---
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 20));
        btnPanel.setBackground(Style.BG_DARK);
        JButton sellBtn = new JButton("COMPLETE SALE");
        sellBtn.setPreferredSize(new Dimension(200, 45));
        sellBtn.setBackground(Style.ACCENT);
        JButton backBtn = new JButton("CANCEL");
        btnPanel.add(sellBtn); btnPanel.add(backBtn);
        add(btnPanel, BorderLayout.SOUTH);

        // --- 4. ACTIONS ---
        loadData();
        productCombo.addActionListener(e -> updateProductDetails());
        qtyField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { calculateTotal(); }
            public void removeUpdate(DocumentEvent e) { calculateTotal(); }
            public void changedUpdate(DocumentEvent e) { calculateTotal(); }
        });

        sellBtn.addActionListener(e -> finalizeSale());
        backBtn.addActionListener(e -> {
            if (currentRole.equalsIgnoreCase("admin")) new AdminDashboard(currentUser);
            else new UserDashboard(currentUser);
            dispose();
        });

        setVisible(true);
    }

    private void calculateTotal() {
        try {
            String qtyStr = qtyField.getText().trim();
            if (qtyStr.isEmpty()) { totalLabel.setText("TOTAL: $0.00"); return; }
            int qty = Integer.parseInt(qtyStr);
            totalLabel.setText("TOTAL: $" + String.format("%,.2f", qty * unitPrice));
            if (qty > availableStock) totalLabel.setForeground(Style.ERROR);
            else totalLabel.setForeground(Color.ORANGE);
        } catch (Exception e) { totalLabel.setText("INVALID QTY"); }
    }

    private void updateProductDetails() {
        String prod = (String) productCombo.getSelectedItem();
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT quantity, price FROM products WHERE name = ?");
            ps.setString(1, prod);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                availableStock = rs.getInt(1);
                unitPrice = rs.getDouble(2);
                stockLabel.setText("Available Stock: " + availableStock);
                priceLabel.setText("Unit Price: $" + String.format("%.2f", unitPrice));
                calculateTotal();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void finalizeSale() {
        String prod = (String) productCombo.getSelectedItem();
        String cust = (String) customerCombo.getSelectedItem();
        int qty = Integer.parseInt(qtyField.getText());

        if (qty > availableStock) {
            JOptionPane.showMessageDialog(this, "Shortage: Not enough stock!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            // 1. Stock መቀነስ
            PreparedStatement up = con.prepareStatement("UPDATE products SET quantity = quantity - ? WHERE name = ?");
            up.setInt(1, qty); up.setString(2, prod);
            up.executeUpdate();

            // 2. ሽያጭ መመዝገብ
            PreparedStatement sp = con.prepareStatement("INSERT INTO sales(product_name, customer_name, quantity_sold, total_price, sold_by) VALUES(?,?,?,?,?)");
            sp.setString(1, prod); sp.setString(2, cust); sp.setInt(3, qty);
            sp.setDouble(4, qty * unitPrice); sp.setString(5, currentUser);
            sp.executeUpdate();

            // ⭐ 3. LOW STOCK NOTIFICATION (አንቺ የጠየቅሽው ክፍል)
            // ሽያጩ ካለቀ በኋላ የቀረውን እቃ ብዛት ከ min_stock ጋር ያወዳድራል
            PreparedStatement checkPs = con.prepareStatement("SELECT quantity, min_stock FROM products WHERE name = ?");
            checkPs.setString(1, prod);
            ResultSet rs = checkPs.executeQuery();
            if (rs.next()) {
                int remaining = rs.getInt("quantity");
                int minLimit = rs.getInt("min_stock");
                
                if (remaining <= minLimit) {
                    String msg = "⚠️ ATTENTION: LOW STOCK ALERT!\n\n" +
                                 "Product: " + prod + "\n" +
                                 "Remaining Qty: " + remaining + "\n" +
                                 "Alert Threshold: " + minLimit + "\n\n" +
                                 "Please restock this item immediately.";
                    JOptionPane.showMessageDialog(this, msg, "Inventory Warning", JOptionPane.WARNING_MESSAGE);
                }
            }

            JOptionPane.showMessageDialog(this, "Transaction Completed!");
            if (currentRole.equalsIgnoreCase("admin")) new AdminDashboard(currentUser);
            else new UserDashboard(currentUser);
            dispose();
            
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadData() {
        try (Connection con = DBConnection.getConnection()) {
            ResultSet rs1 = con.createStatement().executeQuery("SELECT name FROM products");
            while (rs1.next()) productCombo.addItem(rs1.getString(1));
            ResultSet rs2 = con.createStatement().executeQuery("SELECT name FROM customers");
            while (rs2.next()) customerCombo.addItem(rs2.getString(1));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private JLabel createLabel(String t) {
        JLabel l = new JLabel(t); l.setForeground(Color.WHITE);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12)); return l;
    }

    private JComboBox<String> createStyledCombo() {
        JComboBox<String> cb = new JComboBox<>();
        cb.setBackground(new Color(45, 45, 65)); cb.setForeground(Color.WHITE);
        return cb;
    }

    private void styleField(JTextField f) {
        f.setBackground(new Color(45, 45, 65)); f.setForeground(Color.WHITE);
        f.setCaretColor(Color.WHITE); f.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    }
}