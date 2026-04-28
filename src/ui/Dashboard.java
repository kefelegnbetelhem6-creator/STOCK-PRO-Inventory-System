package ui;

import db.DBConnection;
import util.Style;
import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Dashboard extends JFrame {
    String role, username;
    JLabel metric1Lbl, metric2Lbl, metric3Lbl, metric4Lbl, timeLbl;
    boolean isAdmin;

    public Dashboard(String role, String username) {
        this.role = role;
        this.username = username;
        this.isAdmin = role.equalsIgnoreCase("admin");

        setTitle("STOCK PRO | " + (isAdmin ? "Administrator" : "Staff") + " Dashboard");
        setSize(1400, 850);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // --- 1. SIDEBAR (የግራ አዝራሮች) ---
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(280, 850));
        sidebar.setBackground(Style.SIDEBAR_BG);
        sidebar.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 10));

        JLabel logo = new JLabel("STOCK PRO");
        logo.setFont(Style.TITLE_FONT);
        logo.setForeground(Style.ACCENT);
        logo.setBorder(BorderFactory.createEmptyBorder(30, 0, 40, 0));
        sidebar.add(logo);

        // ለሁሉም ተጠቃሚ የሚታዩ አዝራሮች
        sidebar.add(createNavButton("System Dashboard", "/icons/home.png", e -> refreshDashboard()));
        sidebar.add(createNavButton("Inventory List", "/icons/view.png", e -> { new ViewProducts(role, username); dispose(); }));
        sidebar.add(createNavButton("Process Sale", "/icons/add.png", e -> { new SalesForm(role, username); dispose(); }));

        // ⭐ ለአድሚን ብቻ የሚታዩ አዝራሮች (ተጠቃሚ ሲገባ እነዚህ ጭራሽ አይታዩም)
        if (isAdmin) {
            sidebar.add(createNavButton("Add New Stock", "/icons/add.png", e -> { new AddProduct(role, username); dispose(); }));
            sidebar.add(createNavButton("Customer Database", "/icons/customers.png", e -> { new ManageCustomers(role, username); dispose(); }));
            sidebar.add(createNavButton("Manage Suppliers", "/icons/customers.png", e -> { new ManageSuppliers(role, username); dispose(); }));
            sidebar.add(createNavButton("Sales Analytics", "/icons/report.png", e -> { new SalesHistory(role, username); dispose(); }));
        }

        sidebar.add(createNavButton("Account Settings", "/icons/settings.png", e -> new SettingsForm(username)));
        
        JButton logoutBtn = createNavButton("Logout System", "/icons/logout.png", e -> {
            if(JOptionPane.showConfirmDialog(this, "Are you sure you want to Logout?", "Confirm", 0) == 0) {
                new LoginForm(); dispose();
            }
        });
        sidebar.add(logoutBtn);

        add(sidebar, BorderLayout.WEST);

        // --- 2. MAIN CONTENT AREA ---
        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setBackground(Style.BG_DARK);

        // --- HEADER (Welcome & Time) ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Style.BG_DARK);
        header.setBorder(BorderFactory.createEmptyBorder(30, 40, 10, 40));
        
        JLabel welcome = new JLabel("Welcome back, " + username.toUpperCase() + " [" + role.toUpperCase() + "]");
        welcome.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        welcome.setForeground(Color.LIGHT_GRAY);
        
        timeLbl = new JLabel();
        timeLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        timeLbl.setForeground(Style.ACCENT);
        startClock(); 

        header.add(welcome, BorderLayout.WEST);
        header.add(timeLbl, BorderLayout.EAST);
        mainContent.add(header, BorderLayout.NORTH);

        // --- CENTER AREA (Metric Cards) ---
        JPanel centerPanel = new JPanel(new BorderLayout(0, 30));
        centerPanel.setBackground(Style.BG_DARK);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 40, 40));

        JPanel cardPanel = new JPanel(new GridLayout(1, 4, 20, 0));
        cardPanel.setBackground(Style.BG_DARK);
        
        metric1Lbl = new JLabel("0");
        metric2Lbl = new JLabel("0");
        metric3Lbl = new JLabel("0");
        metric4Lbl = new JLabel("0");

        // ⭐ አድሚን እና ተራ ተጠቃሚ የሚያዩት መረጃ ይለያያል
        if (isAdmin) {
            cardPanel.add(createMetricCard("TODAY'S REVENUE", metric1Lbl, Style.ACCENT));
            cardPanel.add(createMetricCard("TOTAL PRODUCTS", metric2Lbl, new Color(52, 152, 219)));
            cardPanel.add(createMetricCard("LOW STOCK ALERT", metric3Lbl, Style.ERROR));
            cardPanel.add(createMetricCard("TOTAL ASSET VALUE", metric4Lbl, Color.ORANGE));
        } else {
            cardPanel.add(createMetricCard("ITEMS SOLD TODAY", metric1Lbl, Style.ACCENT));
            cardPanel.add(createMetricCard("TOTAL PRODUCTS", metric2Lbl, new Color(52, 152, 219)));
            cardPanel.add(createMetricCard("LOW STOCK ALERT", metric3Lbl, Style.ERROR));
            cardPanel.add(createMetricCard("SYSTEM STATUS", metric4Lbl, Color.CYAN));
        }
        
        centerPanel.add(cardPanel, BorderLayout.NORTH);

        // Activity Logs
        JPanel activityPanel = new JPanel(new BorderLayout());
        activityPanel.setBackground(Style.SIDEBAR_BG);
        activityPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JLabel activityTitle = new JLabel("Recent Activity");
        activityTitle.setFont(Style.CARD_FONT);
        activityTitle.setForeground(Color.LIGHT_GRAY);
        JTextArea logs = new JTextArea("\n  [INFO] Session started for " + username + "\n  [OK] Secure database connection active.");
        logs.setBackground(Style.SIDEBAR_BG);
        logs.setForeground(new Color(100, 100, 120));
        logs.setEditable(false);
        activityPanel.add(activityTitle, BorderLayout.NORTH);
        activityPanel.add(logs, BorderLayout.CENTER);
        centerPanel.add(activityPanel, BorderLayout.CENTER);

        mainContent.add(centerPanel, BorderLayout.CENTER);
        add(mainContent, BorderLayout.CENTER);

        fetchLiveMetrics();
        setVisible(true);
    }

    private void startClock() {
        Timer timer = new Timer(1000, e -> {
            timeLbl.setText(new SimpleDateFormat("EEEE, dd MMM yyyy | HH:mm:ss").format(new Date()));
        });
        timer.start();
    }

    private void refreshDashboard() { fetchLiveMetrics(); }

    private JPanel createMetricCard(String title, JLabel valLbl, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Style.CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(50, 50, 70), 1),
            BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));
        JLabel titleLbl = new JLabel(title);
        titleLbl.setForeground(new Color(150, 150, 170));
        titleLbl.setFont(Style.CARD_FONT);
        valLbl.setForeground(color);
        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 30));
        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valLbl, BorderLayout.CENTER);
        return card;
    }

    private JButton createNavButton(String text, String iconPath, java.awt.event.ActionListener action) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(250, 55));
        btn.setBackground(Style.SIDEBAR_BG);
        btn.setForeground(new Color(180, 180, 190));
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setIcon(Style.getIcon(iconPath, 24, 24));
        btn.setIconTextGap(20);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createEmptyBorder(0, 25, 0, 0));
        btn.addActionListener(action);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { 
                btn.setBackground(new Color(40, 40, 65)); btn.setForeground(Style.ACCENT);
            }
            public void mouseExited(java.awt.event.MouseEvent e) { 
                btn.setBackground(Style.SIDEBAR_BG); btn.setForeground(new Color(180, 180, 190));
            }
        });
        return btn;
    }

    private void fetchLiveMetrics() {
        try (Connection con = DBConnection.getConnection()) {
            if (isAdmin) {
                // Admin: Revenue and Assets
                ResultSet rs1 = con.createStatement().executeQuery("SELECT SUM(total_price) FROM sales WHERE DATE(sale_date) = CURDATE()");
                if (rs1.next()) metric1Lbl.setText("$" + String.format("%,.2f", rs1.getDouble(1)));
                
                ResultSet rs4 = con.createStatement().executeQuery("SELECT SUM(quantity * price) FROM products");
                if (rs4.next()) metric4Lbl.setText("$" + String.format("%,.2f", rs4.getDouble(1)));
            } else {
                // Staff: Item counts only
                ResultSet rs1 = con.createStatement().executeQuery("SELECT SUM(quantity_sold) FROM sales WHERE DATE(sale_date) = CURDATE()");
                if (rs1.next()) metric1Lbl.setText(String.valueOf(rs1.getInt(1)));
                
                metric4Lbl.setText("OPERATIONAL");
            }

            // Shared metrics
            ResultSet rs2 = con.createStatement().executeQuery("SELECT COUNT(*) FROM products");
            if (rs2.next()) metric2Lbl.setText(String.valueOf(rs2.getInt(1)));

            ResultSet rs3 = con.createStatement().executeQuery("SELECT COUNT(*) FROM products WHERE quantity <= min_stock");
            if (rs3.next()) metric3Lbl.setText(String.valueOf(rs3.getInt(1)));

        } catch (Exception e) { e.printStackTrace(); }
    }
}