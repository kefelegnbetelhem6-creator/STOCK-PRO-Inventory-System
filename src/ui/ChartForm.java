/**
 * Project: STOCK PRO - Enterprise Inventory System
 * Module: Visual Analytics (Stock Level Chart)
 * Library: JFreeChart Integration
 */

package ui;

import db.DBConnection;
import util.Style;
import java.awt.*;
import javax.swing.*;
import java.sql.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

public class ChartForm extends JFrame {

    public ChartForm() {
        setTitle("STOCK PRO | INVENTORY ANALYTICS CHART");
        setSize(900, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(Style.BG_DARK);
        setLayout(new BorderLayout());

        // --- 1. HEADER ---
        JLabel header = new JLabel("PRODUCT STOCK LEVEL ANALYSIS", SwingConstants.CENTER);
        header.setFont(Style.TITLE_FONT);
        header.setForeground(Style.ACCENT);
        header.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(header, BorderLayout.NORTH);

        // --- 2. CREATE CHART PANEL ---
        ChartPanel chartPanel = createStockChart();
        chartPanel.setBorder(BorderFactory.createEmptyBorder(10, 30, 20, 30));
        chartPanel.setBackground(Style.BG_DARK);
        add(chartPanel, BorderLayout.CENTER);

        // --- 3. BOTTOM BUTTON ---
        JButton closeBtn = new JButton("CLOSE CHART");
        closeBtn.setBackground(new Color(60, 60, 70));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFocusPainted(false);
        closeBtn.addActionListener(e -> dispose());
        
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setBackground(Style.BG_DARK);
        footer.add(closeBtn);
        add(footer, BorderLayout.SOUTH);

        setVisible(true);
    }

    private ChartPanel createStockChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        try (Connection con = DBConnection.getConnection()) {
            // ዳታቤዝ ውስጥ ያሉትን እቃዎችና ብዛታቸውን ማምጣት
            String sql = "SELECT name, quantity FROM products ORDER BY quantity DESC";
            ResultSet rs = con.createStatement().executeQuery(sql);

            while (rs.next()) {
                dataset.addValue(rs.getInt("quantity"), "Stock Level", rs.getString("name"));
            }

        } catch (SQLException e) {
            System.err.println("Chart Data Error: " + e.getMessage());
        }

        // ግራፉን መፍጠር (Bar Chart)
        JFreeChart chart = ChartFactory.createBarChart(
                null,                   // ርዕስ (ቀድመን ስለጻፍነው null አልነው)
                "Product Names",        // X-Axis label
                "Current Quantity",     // Y-Axis label
                dataset,                // ዳታው
                PlotOrientation.VERTICAL, 
                false,                  // Legend አያስፈልግም
                true, 
                false
        );

        // ⭐ ግራፉን ይበልጥ ፕሮፌሽናል የማድረጊያ ስራ (Styling)
        chart.setBackgroundPaint(Style.BG_DARK); // የውጭው ዳራ
        
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(new Color(30, 30, 50)); // የውስጡ ዳራ
        plot.setRangeGridlinePaint(Color.GRAY);         // የመስመሮቹ ከለር
        
        // የባሮቹን ከለር ወደ ሰማያዊ መቀየር
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(36, 198, 220)); 
        renderer.setBarPainter(new org.jfree.chart.renderer.category.StandardBarPainter());

        // የጽሁፍ ከለሮችን ነጭ ማድረግ
        plot.getDomainAxis().setTickLabelPaint(Color.WHITE);
        plot.getDomainAxis().setLabelPaint(Style.ACCENT);
        plot.getRangeAxis().setTickLabelPaint(Color.WHITE);
        plot.getRangeAxis().setLabelPaint(Style.ACCENT);

        return new ChartPanel(chart);
    }
}