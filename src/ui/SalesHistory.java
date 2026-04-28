package ui;

import db.DBConnection;
import util.Style;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class SalesHistory extends JFrame {
    JTable table;
    DefaultTableModel model;
    JLabel totalRevLbl;
    String currentRole, currentUser;

    public SalesHistory(String role, String user) {
        this.currentRole = role;
        this.currentUser = user;

        setTitle("STOCK PRO | SALES TRANSACTION RECORDS");
        setSize(1200, 750); // ቁመቱን በትንሹ ቀንሰነዋል (ከ800 ወደ 750)
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        // --- 1. MAIN PANEL WITH SCROLL ---
        JPanel mainContent = new JPanel(new BorderLayout(15, 15));
        mainContent.setBackground(Style.BG_DARK);

        // --- 2. HEADER ---
        JLabel header = new JLabel("DETAILED SALES RECORDS", SwingConstants.CENTER);
        header.setFont(Style.TITLE_FONT);
        header.setForeground(Style.ACCENT);
        header.setBorder(BorderFactory.createEmptyBorder(25, 0, 15, 0));
        mainContent.add(header, BorderLayout.NORTH);

        // --- 3. TABLE (CENTER) ---
        model = new DefaultTableModel(new String[]{"ID", "Product", "Customer", "Qty", "Revenue ($)", "Sold By", "Date"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        setupTableStyle();
        
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        tableScroll.getViewport().setBackground(Style.BG_DARK);
        mainContent.add(tableScroll, BorderLayout.CENTER);

        // --- 4. FOOTER (SOUTH) ---
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Style.BG_DARK);
        footer.setBorder(BorderFactory.createEmptyBorder(10, 40, 30, 40));

        totalRevLbl = new JLabel("TOTAL REVENUE: $0.00");
        totalRevLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        totalRevLbl.setForeground(Color.ORANGE);

        JButton backBtn = new JButton("← BACK TO DASHBOARD");
        backBtn.setPreferredSize(new Dimension(220, 40));
        backBtn.setBackground(new Color(60, 60, 60));
        backBtn.setForeground(Color.WHITE);
        backBtn.setFocusPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        backBtn.addActionListener(e -> {
            if (currentRole.equalsIgnoreCase("admin")) new AdminDashboard(currentUser);
            else new UserDashboard(currentUser);
            dispose();
        });

        footer.add(totalRevLbl, BorderLayout.WEST);
        footer.add(backBtn, BorderLayout.EAST);
        mainContent.add(footer, BorderLayout.SOUTH);

        // ⭐ መላውን ገጽ በ Scroll Pane ውስጥ መክተት (ስክሪኑ ካነሰ እንዲሸበለል)
        JScrollPane mainScroll = new JScrollPane(mainContent);
        mainScroll.setBorder(null);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);
        add(mainScroll);

        loadSales();
        setVisible(true);
    }

    private void setupTableStyle() {
        table.setRowHeight(40);
        table.setBackground(new Color(30, 30, 50));
        table.setForeground(Color.WHITE);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setSelectionBackground(Style.ACCENT);
        table.setSelectionForeground(Color.BLACK);
        table.getTableHeader().setBackground(new Color(20, 20, 35));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
    }

    private void loadSales() {
        model.setRowCount(0);
        double total = 0;
        try (Connection con = DBConnection.getConnection()) {
            ResultSet rs = con.createStatement().executeQuery("SELECT * FROM sales ORDER BY sale_date DESC");
            while (rs.next()) {
                double rev = rs.getDouble("total_price");
                total += rev;
                model.addRow(new Object[]{
                    rs.getInt("id"), rs.getString("product_name"), rs.getString("customer_name"),
                    rs.getInt("quantity_sold"), String.format("%.2f", rev),
                    rs.getString("sold_by"), rs.getTimestamp("sale_date")
                });
            }
            totalRevLbl.setText("TOTAL REVENUE: $" + String.format("%,.2f", total));
        } catch (Exception e) { e.printStackTrace(); }
    }
}