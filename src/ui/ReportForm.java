package ui;

import db.DBConnection;
import util.Style;
import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.FileWriter;
import java.io.IOException;

public class ReportForm extends JFrame {

    JTextArea reportArea;
    Color bgMain = new Color(26, 26, 46);
    Color cardBg = new Color(22, 33, 62);
    Color accentColor = new Color(78, 204, 163);

    public ReportForm() {
        setTitle("STOCK PRO | Advanced Analytics Report");
        setSize(850, 900);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // --- 1. MODERN HEADER PANEL ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(cardBg);
        headerPanel.setPreferredSize(new Dimension(850, 70));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));

        // Title on the Left
        JLabel titleLbl = new JLabel("SYSTEM ANALYTICS REPORT");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLbl.setForeground(Color.WHITE);
        headerPanel.add(titleLbl, BorderLayout.WEST);

        // Buttons on the Right
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 5));
        actionPanel.setBackground(cardBg);

        // ⭐ አዲሱ ሪፍሬሽ ስታይል (ከሌሎቹ ተለይቶ)
        JButton refreshBtn = new JButton("↻"); // ክብ ፍላጻ ምልክት
        refreshBtn.setToolTipText("Refresh Data");
        refreshBtn.setFont(new Font("Segoe UI", Font.BOLD, 22));
        refreshBtn.setForeground(accentColor);
        refreshBtn.setBackground(cardBg);
        refreshBtn.setBorder(null); // ቦርደር የሌለው ዘመናዊ መልክ
        refreshBtn.setFocusPainted(false);
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton exportBtn = createActionBtn("Export Excel (CSV)", Color.ORANGE, Color.BLACK);
        JButton printBtn = createActionBtn("Print Report", accentColor, Color.BLACK);

        actionPanel.add(refreshBtn);
        actionPanel.add(new JSeparator(JSeparator.VERTICAL)); // መስመር መለያ
        actionPanel.add(exportBtn);
        actionPanel.add(printBtn);

        headerPanel.add(actionPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // --- 2. REPORT AREA ---
        reportArea = new JTextArea();
        reportArea.setEditable(false);
        reportArea.setBackground(new Color(15, 15, 25)); // ይበልጥ ጥቁር የሆነ ዳራ
        reportArea.setForeground(new Color(200, 200, 200));
        reportArea.setFont(new Font("Monospaced", Font.PLAIN, 14)); 
        reportArea.setMargin(new Insets(40, 40, 40, 40));

        JScrollPane scroll = new JScrollPane(reportArea);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);

        // --- 3. ACTIONS ---
        loadReport();

        // Refresh ስራ (ሲነካ ጽሁፉ ይሽከረከራል)
        refreshBtn.addActionListener(e -> {
            loadReport();
            // ለተጠቃሚው እንዲታወቅ ትንሽ animation (Optional)
            refreshBtn.setForeground(Color.WHITE);
            Timer timer = new Timer(500, ev -> refreshBtn.setForeground(accentColor));
            timer.setRepeats(false);
            timer.start();
        });
        
        printBtn.addActionListener(e -> printReport());
        exportBtn.addActionListener(e -> exportToCSV());

        setVisible(true);
    }

    private JButton createActionBtn(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        return btn;
    }

    private void printReport() {
        try {
            reportArea.print(new java.text.MessageFormat("Inventory Analytics"), new java.text.MessageFormat("STOCK PRO - Page {0}"));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Print Error: " + ex.getMessage());
        }
    }

    void loadReport() {
        StringBuilder sb = new StringBuilder();
        String timestamp = new SimpleDateFormat("EEEE, dd MMM yyyy | HH:mm:ss").format(new Date());

        sb.append("================================================================\n");
        sb.append("                 STOCK PRO ENTERPRISE SYSTEM                    \n");
        sb.append("                 Generated: ").append(timestamp).append("\n");
        sb.append("================================================================\n\n");

        sb.append("1. REAL-TIME INVENTORY STATUS\n");
        sb.append("----------------------------------------------------------------\n");
        sb.append(String.format("%-8s %-25s %-10s %-10s %-10s\n", "ID", "Product Name", "Qty", "Price", "Value"));
        sb.append("----------------------------------------------------------------\n");

        double totalStockValue = 0;
        int lowStockCount = 0;

        try (Connection con = DBConnection.getConnection()) {
            ResultSet rs = con.createStatement().executeQuery("SELECT * FROM products");
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                int qty = rs.getInt("quantity");
                double price = rs.getDouble("price");
                int minStock = rs.getInt("min_stock");

                double lineTotal = qty * price;
                totalStockValue += lineTotal;
                String alert = (qty <= minStock) ? " [ALERT]" : "";
                if (qty <= minStock) lowStockCount++;

                sb.append(String.format("%-8d %-25s %-10d %-10.2f %-10.2f%s\n", 
                        id, 
                        name.length() > 22 ? name.substring(0, 20) + ".." : name, 
                        qty, price, lineTotal, alert));
            }

            sb.append("\n2. EXECUTIVE BUSINESS SUMMARY\n");
            sb.append("----------------------------------------------------------------\n");
            sb.append(String.format("Total Asset Value in Stock:   $%,.2f\n", totalStockValue));
            sb.append(String.format("Critical Stock Warnings:      %d items\n", lowStockCount));
            sb.append("----------------------------------------------------------------\n");
            
            reportArea.setText(sb.toString());
        } catch (SQLException e) { 
            reportArea.setText("DATABASE ERROR: " + e.getMessage());
        }
    }

    private void exportToCSV() {
        String fileName = "Stock_Report_" + System.currentTimeMillis() + ".csv";
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.append("ID,Product Name,Quantity,Price,Total Value\n");
            try (Connection con = DBConnection.getConnection()) {
                ResultSet rs = con.createStatement().executeQuery("SELECT * FROM products");
                while (rs.next()) {
                    writer.append(rs.getInt("id") + "," + rs.getString("name") + "," + 
                                  rs.getInt("quantity") + "," + rs.getDouble("price") + "," + 
                                  (rs.getInt("quantity") * rs.getDouble("price")) + "\n");
                }
            }
            JOptionPane.showMessageDialog(this, "Exported successfully to " + fileName);
            Desktop.getDesktop().open(new java.io.File(fileName));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Export Error: " + e.getMessage());
        }
    }
}