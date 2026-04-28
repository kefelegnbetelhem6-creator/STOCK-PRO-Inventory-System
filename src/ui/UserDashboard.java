/**
 * Project: STOCK PRO - Enterprise Inventory System
 * Module: Staff Elite Portal (Compact, Responsive & Visual)
 * Author: Beta
 */

package ui;

import db.DBConnection;
import util.Style;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UserDashboard extends JFrame {
    String username;
    JLabel soldQtyLbl, stockVarietyLbl, lowStockAlertLbl, timeLbl, greetingLbl, shiftLbl;
    JTable recentSalesTable;
    DefaultTableModel tableModel;
    JProgressBar salesTargetBar;
    long loginTime;

    public UserDashboard(String username) {
        this.username = username;
        this.loginTime = System.currentTimeMillis();
        
        setTitle("STOCK PRO | STAFF ELITE PORTAL - " + username.toUpperCase());
        setSize(1350, 750); 
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // --- 1. SIDEBAR (Compact Navigation with Visual Analytics) ---
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(240, 750));
        sidebar.setBackground(Style.SIDEBAR_BG);
        sidebar.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 8));

        JLabel logo = new JLabel("STOCK PRO");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        logo.setForeground(Style.ACCENT);
        logo.setBorder(BorderFactory.createEmptyBorder(20, 0, 30, 0));
        sidebar.add(logo);

        // Sidebar Navigation Buttons
        sidebar.add(createNavButton("Dashboard View", "/icons/home.png", e -> loadStaffData()));
        sidebar.add(createNavButton("Process New Sale", "/icons/add.png", e -> { new SalesForm("user", username); dispose(); }));
        sidebar.add(createNavButton("Inventory Check", "/icons/view.png", e -> { new ViewProducts("user", username); dispose(); }));
        
        // ⭐ አዲሱ የግራፍ አዝራር (Visual Analytics)
        sidebar.add(createNavButton("Visual Analytics", "/icons/report.png", e -> { new ChartForm(); }));
        
        sidebar.add(createNavButton("Customer Database", "/icons/customers.png", e -> { new ManageCustomers("user", username); dispose(); }));
        sidebar.add(createNavButton("My Sales History", "/icons/report.png", e -> { new SalesHistory("user", username); dispose(); }));
        sidebar.add(createNavButton("Account Settings", "/icons/settings.png", e -> new SettingsForm(username)));
        
        sidebar.add(Box.createVerticalStrut(100)); // ክፍተት

        JButton logoutBtn = createNavButton("Logout System", "/icons/logout.png", e -> {
            UIManager.put("OptionPane.messageForeground", Color.WHITE);
            UIManager.put("OptionPane.background", Style.SIDEBAR_BG);
            UIManager.put("Panel.background", Style.SIDEBAR_BG);
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Logout", JOptionPane.YES_NO_OPTION);
            if(confirm == JOptionPane.YES_OPTION) {
                new LoginForm(); dispose();
            }
        });
        sidebar.add(logoutBtn);
        add(sidebar, BorderLayout.WEST);

        // --- 2. MAIN CONTENT AREA ---
        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setBackground(Style.BG_DARK);

        // --- HEADER ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Style.BG_DARK);
        header.setBorder(BorderFactory.createEmptyBorder(15, 35, 10, 35));

        greetingLbl = new JLabel("Good Day, " + username.toUpperCase());
        greetingLbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        greetingLbl.setForeground(Color.WHITE);

        timeLbl = new JLabel();
        timeLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        timeLbl.setForeground(Style.ACCENT);
        startClock(); 

        header.add(greetingLbl, BorderLayout.WEST);
        header.add(timeLbl, BorderLayout.EAST);
        mainContent.add(header, BorderLayout.NORTH);

        // --- 3. CENTER PANEL (Metrics, Progress & Table) ---
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(Style.BG_DARK);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 35, 15, 35));

        // METRICS ROW
        JPanel cardPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        cardPanel.setBackground(Style.BG_DARK);
        cardPanel.setMaximumSize(new Dimension(1600, 110));
        
        soldQtyLbl = new JLabel("0");
        stockVarietyLbl = new JLabel("0");
        lowStockAlertLbl = new JLabel("0");

        cardPanel.add(createMetricCard("MY SALES TODAY", soldQtyLbl, Style.ACCENT));
        cardPanel.add(createMetricCard("IN-STOCK VARIETIES", stockVarietyLbl, Color.CYAN));
        cardPanel.add(createMetricCard("STOCK WARNINGS", lowStockAlertLbl, Style.ERROR));
        centerPanel.add(cardPanel);
        
        centerPanel.add(Box.createVerticalStrut(20));

        // PROGRESS BAR & SHIFT INFO
        JPanel shiftRow = new JPanel(new GridLayout(1, 2, 30, 0));
        shiftRow.setBackground(Style.BG_DARK);
        shiftRow.setMaximumSize(new Dimension(1600, 70));

        JPanel targetPanel = new JPanel(new BorderLayout());
        targetPanel.setBackground(Style.BG_DARK);
        JLabel targetTitle = new JLabel("Daily Sales Target Progress");
        targetTitle.setForeground(Color.GRAY);
        salesTargetBar = new JProgressBar(0, 50);
        salesTargetBar.setStringPainted(true);
        salesTargetBar.setForeground(Style.ACCENT);
        targetPanel.add(targetTitle, BorderLayout.NORTH);
        targetPanel.add(salesTargetBar, BorderLayout.CENTER);

        shiftLbl = new JLabel("Shift Duration: 00:00:00");
        shiftLbl.setForeground(Color.LIGHT_GRAY);
        shiftLbl.setHorizontalAlignment(SwingConstants.RIGHT);

        shiftRow.add(targetPanel);
        shiftRow.add(shiftLbl);
        centerPanel.add(shiftRow);

        centerPanel.add(Box.createVerticalStrut(20));

        // RECENT TRANSACTIONS TABLE
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Style.SIDEBAR_BG);
        tablePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(50, 50, 70)), " MY RECENT SALES FEED ", 0, 0, new Font("Segoe UI", Font.BOLD, 11), Style.ACCENT));
        
        tableModel = new DefaultTableModel(new String[]{"Product Name", "Quantity", "Customer", "Time"}, 0);
        recentSalesTable = new JTable(tableModel);
        setupTableStyle();
        
        JScrollPane scroll = new JScrollPane(recentSalesTable);
        scroll.getViewport().setBackground(Style.SIDEBAR_BG);
        tablePanel.add(scroll, BorderLayout.CENTER);

        centerPanel.add(tablePanel);
        mainContent.add(centerPanel, BorderLayout.CENTER);

        // STATUS BAR
        JPanel status = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        status.setBackground(new Color(20, 20, 30));
        status.add(new JLabel(" ● Portal Status: Operational") {{ setForeground(Color.GREEN); setFont(new Font("Arial", Font.PLAIN, 10)); }});
        status.add(new JLabel(" ● Database: Connected") {{ setForeground(Color.GREEN); setFont(new Font("Arial", Font.PLAIN, 10)); }});
        mainContent.add(status, BorderLayout.SOUTH);

        add(mainContent, BorderLayout.CENTER);

        loadStaffData();
        updateGreeting();
        setVisible(true);
    }

    private void setupTableStyle() {
        recentSalesTable.setBackground(Style.SIDEBAR_BG);
        recentSalesTable.setForeground(Color.WHITE);
        recentSalesTable.setRowHeight(30);
        recentSalesTable.getTableHeader().setBackground(new Color(30, 30, 50));
        recentSalesTable.getTableHeader().setForeground(Color.WHITE);
        recentSalesTable.setShowGrid(false);
    }

    private void updateGreeting() {
        int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
        if (hour < 12) greetingLbl.setText("Good Morning, " + username.toUpperCase());
        else if (hour < 18) greetingLbl.setText("Good Afternoon, " + username.toUpperCase());
        else greetingLbl.setText("Good Evening, " + username.toUpperCase());
    }

    private void startClock() {
        new Timer(1000, e -> {
            timeLbl.setText(new SimpleDateFormat("EEEE, dd MMM | HH:mm:ss").format(new Date()));
            long diff = System.currentTimeMillis() - loginTime;
            long s = (diff / 1000) % 60;
            long m = (diff / (1000 * 60)) % 60;
            long h = (diff / (1000 * 60 * 60)) % 24;
            shiftLbl.setText(String.format("Session Duration: %02d:%02d:%02d", h, m, s));
        }).start();
    }

    private void loadStaffData() {
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps1 = con.prepareStatement("SELECT SUM(quantity_sold) FROM sales WHERE sold_by = ? AND DATE(sale_date) = CURDATE()");
            ps1.setString(1, username);
            ResultSet rs1 = ps1.executeQuery();
            if (rs1.next()) {
                int totalSold = rs1.getInt(1);
                soldQtyLbl.setText(String.valueOf(totalSold));
                salesTargetBar.setValue(totalSold);
            }
            ResultSet rs2 = con.createStatement().executeQuery("SELECT COUNT(*) FROM products");
            if (rs2.next()) stockVarietyLbl.setText(String.valueOf(rs2.getInt(1)));
            ResultSet rs3 = con.createStatement().executeQuery("SELECT COUNT(*) FROM products WHERE quantity <= min_stock");
            if (rs3.next()) lowStockAlertLbl.setText(String.valueOf(rs3.getInt(1)));

            tableModel.setRowCount(0);
            PreparedStatement psT = con.prepareStatement("SELECT product_name, quantity_sold, customer_name, TIME(sale_date) FROM sales WHERE sold_by = ? ORDER BY sale_date DESC LIMIT 10");
            psT.setString(1, username);
            ResultSet rsT = psT.executeQuery();
            while(rsT.next()) tableModel.addRow(new Object[]{rsT.getString(1), rsT.getInt(2), rsT.getString(3), rsT.getString(4)});
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private JButton createNavButton(String text, String iconPath, java.awt.event.ActionListener action) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(220, 42));
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
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(new Color(45, 45, 70)); btn.setForeground(Style.ACCENT); }
            public void mouseExited(java.awt.event.MouseEvent e) { btn.setBackground(Style.SIDEBAR_BG); btn.setForeground(new Color(180, 180, 190)); }
        });
        return btn;
    }

    private JPanel createMetricCard(String title, JLabel valLbl, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Style.SIDEBAR_BG);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(50, 50, 70), 1), BorderFactory.createEmptyBorder(10, 15, 10, 15)));
        JLabel titleLbl = new JLabel(title);
        titleLbl.setForeground(Color.GRAY);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        valLbl.setForeground(color);
        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 24));
        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valLbl, BorderLayout.CENTER);
        return card;
    }
}