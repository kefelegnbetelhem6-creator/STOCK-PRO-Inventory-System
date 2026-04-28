package ui;

import db.DBConnection;
import util.Style;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.File;

/**
 * STOCK PRO - Enterprise Inventory System
 * Module: Administrator Dashboard (Master Controller)
 * Author: Beta
 */

public class AdminDashboard extends JFrame {
    String username;
    JLabel totalProdLbl, lowStockLbl, totalValueLbl, revenueLbl, timeLbl, topItemLbl;
    JTable lowStockTable;
    DefaultTableModel lowStockModel;
    JProgressBar stockHealthBar;

    public AdminDashboard(String username) {
        this.username = username;
        
        setTitle("STOCK PRO | ENTERPRISE ADMINISTRATOR - " + username.toUpperCase());
        setSize(1350, 780); 
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // --- 1. SIDEBAR (Navigation) ---
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(240, 780));
        sidebar.setBackground(Style.SIDEBAR_BG);
        sidebar.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 8));

        JLabel logo = new JLabel("STOCK PRO");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        logo.setForeground(Style.ACCENT);
        logo.setBorder(BorderFactory.createEmptyBorder(20, 0, 30, 0));
        sidebar.add(logo);

        // Sidebar Buttons
        sidebar.add(createNavButton("System Dashboard", "/icons/home.png", e -> loadMetrics()));
        sidebar.add(createNavButton("Inventory List", "/icons/view.png", e -> { new ViewProducts("admin", username); dispose(); }));
        sidebar.add(createNavButton("Add New Stock", "/icons/add.png", e -> { new AddProduct("admin", username); dispose(); }));
        sidebar.add(createNavButton("Process Sale", "/icons/add.png", e -> { new SalesForm("admin", username); dispose(); }));
        sidebar.add(createNavButton("Customers", "/icons/customers.png", e -> { new ManageCustomers("admin", username); dispose(); }));
        sidebar.add(createNavButton("Suppliers", "/icons/customers.png", e -> { new ManageSuppliers("admin", username); dispose(); }));
        sidebar.add(createNavButton("Sales Analytics", "/icons/report.png", e -> { new SalesHistory("admin", username); dispose(); }));
        
        // ⭐ አዲሱ About አዝራር
        sidebar.add(createNavButton("About System", "/icons/report.png", e -> showAboutDialog()));
        
        sidebar.add(Box.createVerticalStrut(20)); // Spacing
sidebar.add(createNavButton("Visual Stock Chart", "/icons/report.png", e -> {
    new ChartForm(); // ግራፉን ይከፍታል
}));
    sidebar.add(createNavButton("Logout System", "/icons/logout.png", e -> {
    UIManager.put("OptionPane.messageForeground", Color.WHITE);
    UIManager.put("OptionPane.background", Style.SIDEBAR_BG);
    UIManager.put("Panel.background", Style.SIDEBAR_BG);
    UIManager.put("Button.background", Color.WHITE); // አዝራሮቹ እንዲታዩ

    int confirm = JOptionPane.showConfirmDialog(
        this, 
        "Are you sure you want to logout?", 
        "Logout Confirmation", 
        JOptionPane.YES_NO_OPTION, 
        JOptionPane.QUESTION_MESSAGE
    );

    if (confirm == JOptionPane.YES_OPTION) {
        new LoginForm(); 
        dispose();
    }
}));
        add(sidebar, BorderLayout.WEST);

        // --- 2. MAIN CONTENT AREA ---
        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setBackground(Style.BG_DARK);

        // --- HEADER ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Style.BG_DARK);
        header.setBorder(BorderFactory.createEmptyBorder(15, 30, 10, 30));

        JPanel headerLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        headerLeft.setBackground(Style.BG_DARK);
        JTextField searchField = new JTextField(" Search Inventory...", 15);
        searchField.setBackground(new Color(45, 45, 65));
        searchField.setForeground(Color.GRAY);
        
        JButton refreshBtn = new JButton("REFRESH");
        refreshBtn.setBackground(Style.ACCENT);
        refreshBtn.setFont(new Font("Segoe UI", Font.BOLD, 10));
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.addActionListener(e -> loadMetrics());

        headerLeft.add(searchField);
        headerLeft.add(refreshBtn);

        timeLbl = new JLabel();
        timeLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        timeLbl.setForeground(Style.ACCENT);
        startClock();

        header.add(headerLeft, BorderLayout.WEST);
        header.add(timeLbl, BorderLayout.EAST);
        mainContent.add(header, BorderLayout.NORTH);

        // --- CENTER PANEL (Metrics & Tables) ---
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(Style.BG_DARK);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 30, 10, 30));

        // METRICS ROW
        JPanel cardPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        cardPanel.setBackground(Style.BG_DARK);
        cardPanel.setMaximumSize(new Dimension(1600, 100));
        
        revenueLbl = new JLabel("$0.00");
        totalProdLbl = new JLabel("0");
        lowStockLbl = new JLabel("0");
        totalValueLbl = new JLabel("$0.00");
        
        cardPanel.add(createMetricCard("TODAY'S REVENUE", revenueLbl, Style.ACCENT));
        cardPanel.add(createMetricCard("ACTIVE PRODUCTS", totalProdLbl, new Color(52, 152, 219)));
        cardPanel.add(createMetricCard("LOW STOCK ALERTS", lowStockLbl, Style.ERROR));
        cardPanel.add(createMetricCard("TOTAL ASSET VALUE", totalValueLbl, Color.ORANGE));
        centerPanel.add(cardPanel);
        centerPanel.add(Box.createVerticalStrut(15));

        // MIDDLE ROW
        JPanel middleRow = new JPanel(new GridLayout(1, 2, 20, 0));
        middleRow.setBackground(Style.BG_DARK);
        middleRow.setMaximumSize(new Dimension(1600, 350));

        JPanel tablePanel = createSectionPanel("CRITICAL STOCK ITEMS");
        lowStockModel = new DefaultTableModel(new String[]{"Item Name", "Available Quantity"}, 0);
        lowStockTable = new JTable(lowStockModel);
        lowStockTable.setBackground(Style.SIDEBAR_BG);
        lowStockTable.setForeground(Color.WHITE);
        lowStockTable.setRowHeight(25);
        tablePanel.add(new JScrollPane(lowStockTable), BorderLayout.CENTER);

        JPanel rightTools = new JPanel(new GridLayout(2, 1, 0, 10));
        rightTools.setBackground(Style.BG_DARK);

        JPanel healthPanel = createSectionPanel("INVENTORY HEALTH");
        stockHealthBar = new JProgressBar(0, 100);
        stockHealthBar.setStringPainted(true);
        stockHealthBar.setForeground(Style.ACCENT);
        healthPanel.add(stockHealthBar, BorderLayout.CENTER);
        
        topItemLbl = new JLabel("Top Product: Loading...");
        topItemLbl.setForeground(Color.LIGHT_GRAY);
        healthPanel.add(topItemLbl, BorderLayout.SOUTH);

        JPanel btnGrid = new JPanel(new GridLayout(2, 2, 8, 8));
        btnGrid.setBackground(Style.BG_DARK);
        btnGrid.add(createToolBtn("Backup DB", e -> backupDatabase()));
        btnGrid.add(createToolBtn("Export PDF", e -> exportToPDF()));
        btnGrid.add(createToolBtn("System Logs", e -> showSystemLogs()));
        btnGrid.add(createToolBtn("Settings", e -> new SettingsForm(username)));

        rightTools.add(healthPanel);
        rightTools.add(btnGrid);
        middleRow.add(tablePanel);
        middleRow.add(rightTools);
        centerPanel.add(middleRow);

        // --- BOTTOM STATUS BAR ---
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusBar.setBackground(new Color(20, 20, 30));
        statusBar.add(new JLabel(" ● Database: Online ") {{ setForeground(Color.GREEN); setFont(new Font("Arial", Font.PLAIN, 10)); }});
        statusBar.add(new JLabel(" ● User Auth: Encrypted ") {{ setForeground(Color.CYAN); setFont(new Font("Arial", Font.PLAIN, 10)); }});
        
        mainContent.add(centerPanel, BorderLayout.CENTER);
        mainContent.add(statusBar, BorderLayout.SOUTH);

        add(mainContent, BorderLayout.CENTER);

        loadMetrics();
        setVisible(true);
    }

    // ⭐ አዲሱ About Dialog ሜተድ
    private void showAboutDialog() {
        String aboutText = "<html><body style='width: 280px; padding: 10px; color: white;'>"
                + "<h2 style='color: #4ecca3;'>STOCK PRO v2.0</h2>"
                + "<p>Advanced Enterprise Inventory & Sales Management System.</p>"
               // + "<br><b>Developer:</b> <br>"
                + "<b>Technology:</b> Java Swing, MySQL, SHA-256<br><br>"
                + "<hr><p style='font-size: 9px;'>© 2024 STOCK PRO. All Rights Reserved.</p>"
                + "</body></html>";
        
        UIManager.put("OptionPane.background", Style.SIDEBAR_BG);
        UIManager.put("Panel.background", Style.SIDEBAR_BG);
        JOptionPane.showMessageDialog(this, aboutText, "About System", JOptionPane.PLAIN_MESSAGE);
    }

    private void backupDatabase() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String path = chooser.getSelectedFile().getAbsolutePath() + "_backup.sql";
            String command = "mysqldump -u root inventory_db -r \"" + path + "\"";
            try {
                Runtime.getRuntime().exec(command);
                JOptionPane.showMessageDialog(this, "Backup Successful: " + path);
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Check MySQL Path!"); }
        }
    }

    private void showSystemLogs() {
        JDialog dialog = new JDialog(this, "System Logs", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        JTextArea logArea = new JTextArea();
        logArea.setBackground(new Color(20, 20, 30));
        logArea.setForeground(Color.GREEN);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        StringBuilder sb = new StringBuilder("--- RECENT TRANSACTIONS ---\n\n");
        try (Connection con = DBConnection.getConnection()) {
            ResultSet rs = con.createStatement().executeQuery("SELECT sale_date, product_name FROM sales ORDER BY sale_date DESC LIMIT 10");
            while (rs.next()) sb.append("[").append(rs.getTimestamp(1)).append("] SOLD: ").append(rs.getString(2)).append("\n");
        } catch (Exception e) { sb.append("Error."); }
        logArea.setText(sb.toString());
        dialog.add(new JScrollPane(logArea));
        dialog.setVisible(true);
    }

    private void exportToPDF() {
        try { lowStockTable.print(); } catch (Exception e) {}
    }

    private void startClock() {
        new Timer(1000, e -> timeLbl.setText(new SimpleDateFormat("EEE, dd MMM yyyy | HH:mm:ss").format(new Date()))).start();
    }

    private void loadMetrics() {
        try (Connection con = DBConnection.getConnection()) {
            ResultSet rs1 = con.createStatement().executeQuery("SELECT SUM(total_price) FROM sales WHERE DATE(sale_date) = CURDATE()");
            if (rs1.next()) revenueLbl.setText("$" + String.format("%,.0f", rs1.getDouble(1)));
            ResultSet rs2 = con.createStatement().executeQuery("SELECT COUNT(*) FROM products");
            int total = 0; if (rs2.next()) { total = rs2.getInt(1); totalProdLbl.setText(String.valueOf(total)); }
            ResultSet rs3 = con.createStatement().executeQuery("SELECT COUNT(*) FROM products WHERE quantity <= min_stock");
            int low = 0; if (rs3.next()) { low = rs3.getInt(1); lowStockLbl.setText(String.valueOf(low)); }
            ResultSet rs4 = con.createStatement().executeQuery("SELECT SUM(quantity * price) FROM products");
            if (rs4.next()) totalValueLbl.setText("$" + String.format("%,.0f", rs4.getDouble(1)));
            ResultSet rs5 = con.createStatement().executeQuery("SELECT product_name FROM sales GROUP BY product_name ORDER BY SUM(quantity_sold) DESC LIMIT 1");
            if(rs5.next()) topItemLbl.setText("Top Seller: " + rs5.getString(1).toUpperCase());
            lowStockModel.setRowCount(0);
            ResultSet rsT = con.createStatement().executeQuery("SELECT name, quantity FROM products WHERE quantity <= min_stock LIMIT 5");
            while(rsT.next()) lowStockModel.addRow(new Object[]{rsT.getString(1), rsT.getInt(2)});
            if(total > 0) stockHealthBar.setValue(((total - low) * 100) / total);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private JButton createNavButton(String text, String iconPath, java.awt.event.ActionListener action) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(220, 40));
        btn.setBackground(Style.SIDEBAR_BG);
        btn.setForeground(new Color(180, 180, 190));
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setIcon(Style.getIcon(iconPath, 18, 18));
        btn.setIconTextGap(15);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        if(action != null) btn.addActionListener(action);
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(new Color(45, 45, 70)); btn.setForeground(Style.ACCENT); }
            public void mouseExited(java.awt.event.MouseEvent e) { btn.setBackground(Style.SIDEBAR_BG); btn.setForeground(new Color(180, 180, 190)); }
        });
        return btn;
    }

    private JButton createToolBtn(String t, java.awt.event.ActionListener action) {
        JButton b = new JButton(t);
        b.setBackground(new Color(40, 40, 60));
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 11));
        b.setFocusPainted(false);
        if(action != null) b.addActionListener(action);
        return b;
    }

    private JPanel createMetricCard(String title, JLabel valLbl, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Style.SIDEBAR_BG);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(50, 50, 70), 1), BorderFactory.createEmptyBorder(10, 15, 10, 15)));
        JLabel titleLbl = new JLabel(title);
        titleLbl.setForeground(Color.GRAY);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        valLbl.setForeground(color);
        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valLbl, BorderLayout.CENTER);
        return card;
    }

    private JPanel createSectionPanel(String title) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Style.SIDEBAR_BG);
        p.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(50, 50, 70)), title, 0, 0, new Font("Segoe UI", Font.BOLD, 11), Style.ACCENT));
        return p;
    }
}