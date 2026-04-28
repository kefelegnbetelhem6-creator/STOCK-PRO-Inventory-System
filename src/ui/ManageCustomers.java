/**
 * Project: STOCK PRO - Enterprise Inventory System
 * Module: Customer Management (with Email & Phone Validation)
 * Author: Beta
 */

package ui;

import db.DBConnection;
import util.Style;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.regex.Pattern;

public class ManageCustomers extends JFrame {
    JTextField nameF, phoneF, emailF, idF, searchField;
    JTable table;
    DefaultTableModel model;
    String currentRole, currentUser;

    // 🎨 UI Colors
    Color zebraColor = new Color(25, 25, 45);
    Color highlightColor = new Color(36, 198, 220);

    public ManageCustomers(String role, String username) {
        this.currentRole = role;
        this.currentUser = username;

        setTitle("STOCK PRO | CUSTOMER DATABASE");
        setSize(1200, 780); 
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        // --- 1. MAIN CONTENT WRAPPER ---
        JPanel mainContent = new JPanel(new BorderLayout(10, 10));
        mainContent.setBackground(Style.BG_DARK);

        // --- 2. NORTH: HEADER & SEARCH ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(22, 22, 38));
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30));

        JLabel headerTitle = new JLabel("CUSTOMER MANAGEMENT");
        headerTitle.setFont(Style.TITLE_FONT);
        headerTitle.setForeground(Style.ACCENT);
        topPanel.add(headerTitle, BorderLayout.WEST);

        searchField = new JTextField(" Search by name or phone...", 15);
        searchField.setPreferredSize(new Dimension(220, 35));
        searchField.setBackground(new Color(45, 45, 65));
        searchField.setForeground(Color.WHITE);
        topPanel.add(searchField, BorderLayout.EAST);
        mainContent.add(topPanel, BorderLayout.NORTH);

        // --- 3. WEST: INPUT FORM (Sidebar) ---
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Style.BG_DARK);
        form.setPreferredSize(new Dimension(320, 600));
        form.setBorder(BorderFactory.createEmptyBorder(20, 30, 0, 10));

        idF = createStyledField(false);
        nameF = createStyledField(true);
        phoneF = createStyledField(true);
        emailF = createStyledField(true);

        form.add(createLabel("Reference ID:")); form.add(idF);
        form.add(Box.createVerticalStrut(10));
        form.add(createLabel("Full Name:")); form.add(nameF);
        form.add(Box.createVerticalStrut(10));
        form.add(createLabel("Phone (09/07):")); form.add(phoneF);
        form.add(Box.createVerticalStrut(10));
        form.add(createLabel("Email Address:")); form.add(emailF);
        form.add(Box.createVerticalStrut(25));

        JButton addBtn = createActionBtn("REGISTER CUSTOMER", Style.ACCENT);
        JButton delBtn = createActionBtn("DELETE RECORD", Style.ERROR);
        JButton backBtn = createActionBtn("← BACK TO DASHBOARD", Color.DARK_GRAY);
        backBtn.setForeground(Color.WHITE);

        // 🔒 Role Protection
        if (!role.equalsIgnoreCase("admin")) {
            delBtn.setEnabled(false);
            delBtn.setToolTipText("Only Admins can delete records");
        }

        form.add(addBtn);
        form.add(Box.createVerticalStrut(10));
        form.add(delBtn);
        form.add(Box.createVerticalStrut(10));
        form.add(backBtn);
        
        mainContent.add(form, BorderLayout.WEST);

        // --- 4. CENTER: DATA TABLE ---
        model = new DefaultTableModel(new String[]{"ID", "Name", "Phone", "Email"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        setupTableUI();
        
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createEmptyBorder(10, 10, 30, 30));
        tableScroll.getViewport().setBackground(Style.BG_DARK);
        mainContent.add(tableScroll, BorderLayout.CENTER);

        // መላውን ገጽ Scroll እንዲሆን ማድረግ
        JScrollPane mainScroll = new JScrollPane(mainContent);
        mainScroll.setBorder(null);
        add(mainScroll);

        // --- ACTIONS ---
        loadCustomers();
        
        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { searchData(searchField.getText().trim()); }
            public void mouseClicked(MouseEvent e) { if(searchField.getText().contains("Search")) searchField.setText(""); }
        });

        table.getSelectionModel().addListSelectionListener(e -> {
            int r = table.getSelectedRow();
            if(r >= 0) {
                idF.setText(model.getValueAt(r,0).toString());
                nameF.setText(model.getValueAt(r,1).toString());
                phoneF.setText(model.getValueAt(r,2).toString());
                emailF.setText(model.getValueAt(r,3).toString());
            }
        });

        addBtn.addActionListener(e -> saveCustomer());
        delBtn.addActionListener(e -> deleteCustomer());
        
        backBtn.addActionListener(e -> {
            if (currentRole.equalsIgnoreCase("admin")) new AdminDashboard(currentUser);
            else new UserDashboard(currentUser);
            dispose();
        });

        setVisible(true);
    }

    private void setupTableUI() {
        table.setRowHeight(40);
        table.setBackground(Style.BG_DARK);
        table.setForeground(Color.WHITE);
        table.setShowGrid(false);
        table.getTableHeader().setBackground(new Color(30, 30, 50));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setPreferredSize(new Dimension(0, 45));

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (isSelected) {
                    c.setBackground(highlightColor); c.setForeground(Color.BLACK);
                } else {
                    c.setBackground(row % 2 == 0 ? Style.BG_DARK : zebraColor);
                    c.setForeground(Color.WHITE);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
                return c;
            }
        });
    }

    private JLabel createLabel(String t) {
        JLabel l = new JLabel(t);
        l.setForeground(Color.LIGHT_GRAY);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JTextField createStyledField(boolean ed) {
        JTextField f = new JTextField();
        f.setEditable(ed);
        f.setMaximumSize(new Dimension(300, 35));
        f.setBackground(new Color(45, 45, 65));
        f.setForeground(Color.WHITE);
        f.setCaretColor(Color.WHITE);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(70, 70, 90)),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        return f;
    }

    private JButton createActionBtn(String t, Color c) {
        JButton b = new JButton(t);
        b.setBackground(c);
        b.setMaximumSize(new Dimension(260, 40));
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        return b;
    }

    // ⭐ የተቀየረው የ Save ሜተድ (ከኢሜይል ማረጋገጫ ጋር)
    private void saveCustomer() {
        String name = nameF.getText().trim();
        String phone = phoneF.getText().trim();
        String email = emailF.getText().trim();

        // 1. የስም ምርመራ
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter customer name!", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 2. የስልክ ቁጥር ምርመራ (09/07 እና 10 አሃዝ)
        if(!Pattern.matches("^(09|07)\\d{8}$", phone)) {
            JOptionPane.showMessageDialog(this, "Invalid Phone! Must be 10 digits starting with 09 or 07.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // ⭐ 3. የኢሜይል ምርመራ (Regex Validation)
        // ይህ ሎጂክ ኢሜይሉ የግድ @ እና . መያዙን ያረጋግጣል
        String emailRegex = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
        if (!Pattern.matches(emailRegex, email)) {
            JOptionPane.showMessageDialog(this, "Invalid Email Address!\nExample: customer@gmail.com", "Validation Error", JOptionPane.ERROR_MESSAGE);
            emailF.requestFocus(); // ሳጥኑ ላይ እንዲመለስ
            return;
        }

        // 4. ወደ ዳታቤዝ ማስገባት
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("INSERT INTO customers(name, phone, email) VALUES(?,?,?)");
            ps.setString(1, name);
            ps.setString(2, phone);
            ps.setString(3, email);
            ps.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "Customer Registered Successfully!");
            loadCustomers();
            // ሳጥኖቹን ባዶ ማድረግ
            nameF.setText(""); phoneF.setText(""); emailF.setText("");
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void deleteCustomer() {
        if(idF.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a customer to delete!");
            return;
        }
        if(JOptionPane.showConfirmDialog(this, "Are you sure?", "Confirm", 0) == 0) {
            try (Connection con = DBConnection.getConnection()) {
                PreparedStatement ps = con.prepareStatement("DELETE FROM customers WHERE id=?");
                ps.setInt(1, Integer.parseInt(idF.getText()));
                ps.executeUpdate();
                loadCustomers();
                idF.setText(""); nameF.setText(""); phoneF.setText(""); emailF.setText("");
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private void loadCustomers() {
        model.setRowCount(0);
        try (Connection con = DBConnection.getConnection()) {
            ResultSet rs = con.createStatement().executeQuery("SELECT * FROM customers ORDER BY id DESC");
            while(rs.next()) model.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4)});
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void searchData(String q) {
        if(q.contains("Search")) return;
        model.setRowCount(0);
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM customers WHERE name LIKE ? OR phone LIKE ?");
            ps.setString(1, "%"+q+"%"); ps.setString(2, "%"+q+"%");
            ResultSet rs = ps.executeQuery();
            while(rs.next()) model.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4)});
        } catch (Exception e) { e.printStackTrace(); }
    }
}