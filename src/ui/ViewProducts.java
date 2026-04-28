/**
 * Project: STOCK PRO - Enterprise Inventory System
 * Module: Inventory Tracker (Scrollable & Responsive)
 * Author: Beta
 */

package ui;

import db.DBConnection;
import util.Style;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.sql.*;

public class ViewProducts extends JFrame {

    JTable table;
    JTextField idField, nameField, qtyField, priceField, searchField;
    DefaultTableModel model;
    String currentRole, currentUser;

    // 🔵 የ Blue-Black ጭብጥ ከለሮች
    Color alertBlueBlack = new Color(20, 30, 48); 
    Color highlightBlue = new Color(36, 198, 220);
    Color zebraColor = new Color(25, 25, 45);

    public ViewProducts(String role, String username) {
        this.currentRole = role;
        this.currentUser = username;

        setTitle("STOCK PRO | INVENTORY TRACKER - [" + role.toUpperCase() + "]");
        setSize(1200, 750); // ቁመቱን በትንሹ ቀንሰነዋል
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        // --- 1. MAIN CONTENT WRAPPER ---
        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BorderLayout(10, 10));
        mainContent.setBackground(Style.BG_DARK);

        // --- 2. SEARCH PANEL (NORTH) ---
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBackground(Style.SIDEBAR_BG);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

        JLabel titleLbl = new JLabel("STOCK INVENTORY LIST");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLbl.setForeground(Color.WHITE);

        JPanel searchBoxPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        searchBoxPanel.setBackground(Style.SIDEBAR_BG);
        
        searchField = new JTextField(" Search products...", 15);
        searchField.setPreferredSize(new Dimension(200, 30));
        searchField.setBackground(new Color(45, 45, 65));
        searchField.setForeground(Color.WHITE);
        searchField.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 100)));

        JButton resetBtn = createStyledButton("REFRESH", new Color(52, 152, 219));
        searchBoxPanel.add(searchField);
        searchBoxPanel.add(resetBtn);

        searchPanel.add(titleLbl, BorderLayout.WEST);
        searchPanel.add(searchBoxPanel, BorderLayout.EAST);
        mainContent.add(searchPanel, BorderLayout.NORTH);

        // --- 3. TABLE PANEL (CENTER) ---
        model = new DefaultTableModel(new String[]{"ID", "Product Name", "Quantity", "Price ($)", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        applyTableStyling();
        
        JScrollPane tableScroll = new JScrollPane(table);
        // ⭐ የሠንጠረዡን ቁመት በመገደብ አዝራሮቹ እንዲወጡ እናደርጋለን
        tableScroll.setPreferredSize(new Dimension(1000, 400)); 
        tableScroll.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        tableScroll.getViewport().setBackground(Style.BG_DARK);
        mainContent.add(tableScroll, BorderLayout.CENTER);

        // --- 4. CONTROL PANEL (SOUTH) ---
        JPanel controlPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        controlPanel.setBackground(Style.SIDEBAR_BG);
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 15, 10));
        
        // Input Fields Row
        JPanel inputs = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        inputs.setBackground(Style.SIDEBAR_BG);
        idField = createField(3, false);
        nameField = createField(12, true);
        qtyField = createField(5, true);
        priceField = createField(7, true);

        inputs.add(new JLabel("ID:") {{ setForeground(Color.GRAY); }}); inputs.add(idField);
        inputs.add(new JLabel("Name:") {{ setForeground(Color.WHITE); }}); inputs.add(nameField);
        inputs.add(new JLabel("Qty:") {{ setForeground(Color.WHITE); }}); inputs.add(qtyField);
        inputs.add(new JLabel("Price:") {{ setForeground(Color.WHITE); }}); inputs.add(priceField);

        // Button Row
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        buttons.setBackground(Style.SIDEBAR_BG);
        JButton updateBtn = createStyledButton("UPDATE ITEM", Style.ACCENT);
        JButton deleteBtn = createStyledButton("DELETE ITEM", Style.ERROR);
        JButton backBtn = createStyledButton("← BACK TO DASHBOARD", Color.DARK_GRAY);
        backBtn.setForeground(Color.WHITE);

        buttons.add(updateBtn);
        buttons.add(deleteBtn);
        buttons.add(backBtn);

        controlPanel.add(inputs);
        controlPanel.add(buttons);
        mainContent.add(controlPanel, BorderLayout.SOUTH);

        // ⭐ 5. መላውን ገጽ SCROLLABLE ማድረግ (ይህ ነው መፍትሄው)
        JScrollPane mainScroll = new JScrollPane(mainContent);
        mainScroll.setBorder(null);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16); // ፍጥነቱን ለመጨመር
        add(mainScroll);

        // --- ACTIONS ---
        loadData();
        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { searchData(); }
        });
        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                idField.setText(model.getValueAt(row, 0).toString());
                nameField.setText(model.getValueAt(row, 1).toString());
                qtyField.setText(model.getValueAt(row, 2).toString());
                priceField.setText(model.getValueAt(row, 3).toString());
            }
        });
        resetBtn.addActionListener(e -> { searchField.setText(""); loadData(); });
        updateBtn.addActionListener(e -> updateProduct());
        deleteBtn.addActionListener(e -> deleteProduct());
        backBtn.addActionListener(e -> {
            if (currentRole.equalsIgnoreCase("admin")) new AdminDashboard(currentUser);
            else new UserDashboard(currentUser);
            dispose();
        });

        if (role.equalsIgnoreCase("user")) {
            updateBtn.setEnabled(false);
            deleteBtn.setEnabled(false);
        }

        setVisible(true);
    }

    private void applyTableStyling() {
        table.setRowHeight(40);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setBackground(Style.BG_DARK);
        table.setForeground(Color.WHITE);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setBackground(new Color(30, 30, 50));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setPreferredSize(new Dimension(0, 45));

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                int qty = Integer.parseInt(table.getValueAt(row, 2).toString());
                if (isSelected) {
                    c.setBackground(highlightBlue); c.setForeground(Color.BLACK);
                } else {
                    if (qty <= 5) {
                        c.setBackground(alertBlueBlack); c.setForeground(highlightBlue);
                    } else {
                        c.setBackground(row % 2 == 0 ? Style.BG_DARK : zebraColor);
                        c.setForeground(Color.WHITE);
                    }
                }
                setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
                return c;
            }
        });
    }

    private JTextField createField(int cols, boolean edit) {
        JTextField f = new JTextField(cols);
        f.setEditable(edit);
        f.setBackground(new Color(35, 35, 55));
        f.setForeground(Color.WHITE);
        f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(70, 70, 90)), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        return f;
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(bg == Style.ACCENT ? Color.BLACK : Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    void loadData() {
        model.setRowCount(0);
        try (Connection con = DBConnection.getConnection()) {
            ResultSet rs = con.createStatement().executeQuery("SELECT * FROM products");
            while (rs.next()) {
                int qty = rs.getInt("quantity");
                model.addRow(new Object[]{rs.getInt("id"), rs.getString("name"), qty, rs.getDouble("price"), (qty <= 5 ? "LOW STOCK" : "AVAILABLE")});
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    void searchData() {
        model.setRowCount(0);
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM products WHERE name LIKE ?");
            ps.setString(1, "%" + searchField.getText().trim() + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int qty = rs.getInt("quantity");
                model.addRow(new Object[]{rs.getInt("id"), rs.getString("name"), qty, rs.getDouble("price"), (qty <= 5 ? "LOW STOCK" : "AVAILABLE")});
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    void updateProduct() {
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("UPDATE products SET name=?, quantity=?, price=? WHERE id=?");
            ps.setString(1, nameField.getText());
            ps.setInt(2, Integer.parseInt(qtyField.getText()));
            ps.setDouble(3, Double.parseDouble(priceField.getText()));
            ps.setInt(4, Integer.parseInt(idField.getText()));
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Success: Product updated.");
            loadData();
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Error in updating!"); }
    }

    void deleteProduct() {
        if (idField.getText().isEmpty()) return;
        if (JOptionPane.showConfirmDialog(this, "Delete permanently?", "Confirm", 0) == 0) {
            try (Connection con = DBConnection.getConnection()) {
                PreparedStatement ps = con.prepareStatement("DELETE FROM products WHERE id=?");
                ps.setInt(1, Integer.parseInt(idField.getText()));
                ps.executeUpdate();
                loadData();
                idField.setText(""); nameField.setText(""); qtyField.setText(""); priceField.setText("");
            } catch (Exception e) { e.printStackTrace(); }
        }
    }
}