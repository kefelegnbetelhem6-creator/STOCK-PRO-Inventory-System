package ui;

import db.DBConnection;
import util.Style;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.regex.Pattern; // ለስልክ ቁጥር ምርመራ የሚያስፈልግ

public class ManageSuppliers extends JFrame {
    JTextField compF, contF, phoneF, idF;
    JTable table;
    DefaultTableModel model;
    String currentRole, currentUser;

    public ManageSuppliers(String role, String user) {
        this.currentRole = role;
        this.currentUser = user;

        setTitle("STOCK PRO | SUPPLIER DIRECTORY");
        setSize(1100, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(Style.BG_DARK);
        setLayout(new BorderLayout(15, 15));

        // --- 1. Header ---
        JLabel header = new JLabel("SUPPLIER MANAGEMENT", SwingConstants.CENTER);
        header.setFont(Style.TITLE_FONT);
        header.setForeground(Style.ACCENT);
        header.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        add(header, BorderLayout.NORTH);

        // --- 2. Form Panel (Left Side) ---
        JPanel form = new JPanel(new GridLayout(12, 1, 5, 5));
        form.setBackground(Style.BG_DARK);
        form.setPreferredSize(new Dimension(320, 0));
        form.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 10));

        idF = new JTextField(); idF.setEditable(false);
        compF = createField(); 
        contF = createField(); 
        phoneF = createField();

        form.add(createLabel("Supplier ID (Auto):")); form.add(idF);
        form.add(createLabel("Company Name:")); form.add(compF);
        form.add(createLabel("Contact Person (Name):")); form.add(contF);
        form.add(createLabel("Phone Number (09/07...):")); form.add(phoneF);

        JButton addBtn = createBtn("SAVE SUPPLIER", Style.ACCENT);
        JButton delBtn = createBtn("DELETE SUPPLIER", Style.ERROR);
        JButton backBtn = createBtn("BACK TO DASHBOARD", Color.DARK_GRAY);
        backBtn.setForeground(Color.WHITE);

        form.add(new JLabel(" "));
        form.add(addBtn); 
        form.add(delBtn);
        form.add(backBtn);
        add(form, BorderLayout.WEST);

        // Access Control
        if (!role.equalsIgnoreCase("admin")) {
            addBtn.setEnabled(false);
            delBtn.setEnabled(false);
        }

        // --- 3. Table (Center) ---
        model = new DefaultTableModel(new String[]{"ID", "Company", "Contact Person", "Phone"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(35);
        table.setBackground(new Color(30, 30, 50));
        table.setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        add(new JScrollPane(table), BorderLayout.CENTER);

        // --- 4. ACTIONS ---
        loadData();
        addBtn.addActionListener(e -> save());
        delBtn.addActionListener(e -> deleteSupplier());
        
        backBtn.addActionListener(e -> {
            if (currentRole.equalsIgnoreCase("admin")) new AdminDashboard(currentUser);
            else new UserDashboard(currentUser);
            dispose();
        });

        table.getSelectionModel().addListSelectionListener(e -> {
            int r = table.getSelectedRow();
            if(r >= 0) {
                idF.setText(model.getValueAt(r,0).toString());
                compF.setText(model.getValueAt(r,1).toString());
                contF.setText(model.getValueAt(r,2).toString());
                phoneF.setText(model.getValueAt(r,3).toString());
            }
        });

        setVisible(true);
    }

    private JLabel createLabel(String t) {
        JLabel l = new JLabel(t); l.setForeground(Color.LIGHT_GRAY);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12)); return l;
    }

    private JTextField createField() {
        JTextField f = new JTextField();
        f.setBackground(new Color(45, 45, 65)); f.setForeground(Color.WHITE);
        f.setCaretColor(Color.WHITE);
        f.setBorder(BorderFactory.createLineBorder(new Color(70, 70, 90))); return f;
    }

    private JButton createBtn(String t, Color c) {
        JButton b = new JButton(t); b.setBackground(c);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setFocusPainted(false); b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    // ⭐ ዳታው ትክክል መሆኑን መመርመሪያ (Validation)
    private boolean isPhoneValid(String phone) {
        // Regex: በ 07 ወይም 09 ይጀምር፣ ከዚያ 8 ቁጥሮች ይከተሉ (ጠቅላላ 10)
        String phoneRegex = "^(07|09)\\d{8}$";
        return Pattern.matches(phoneRegex, phone);
    }

    private void save() {
        String company = compF.getText().trim();
        String contact = contF.getText().trim();
        String phone = phoneF.getText().trim();

        if(company.isEmpty() || contact.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields!");
            return;
        }

        // ⭐ የስልክ ቁጥር ምርመራ እዚህ ጋር ይካሄዳል
        if (!isPhoneValid(phone)) {
            JOptionPane.showMessageDialog(this, 
                "Invalid Phone Number!\nMust start with 07 or 09 and be exactly 10 digits long.", 
                "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("INSERT INTO suppliers(company_name, contact_person, phone) VALUES(?,?,?)");
            ps.setString(1, company);
            ps.setString(2, contact);
            ps.setString(3, phone);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Supplier Registered Successfully!");
            loadData();
            clear();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void deleteSupplier() {
        String id = idF.getText();
        if(id.isEmpty()) return;
        if(JOptionPane.showConfirmDialog(this, "Delete this supplier?", "Confirm", 0) == 0) {
            try (Connection con = DBConnection.getConnection()) {
                PreparedStatement ps = con.prepareStatement("DELETE FROM suppliers WHERE id = ?");
                ps.setInt(1, Integer.parseInt(id));
                ps.executeUpdate();
                loadData();
                clear();
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private void clear() {
        idF.setText(""); compF.setText(""); contF.setText(""); phoneF.setText("");
    }

    private void loadData() {
        model.setRowCount(0);
        try (Connection con = DBConnection.getConnection()) {
            ResultSet rs = con.createStatement().executeQuery("SELECT * FROM suppliers");
            while(rs.next()) {
                model.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4)});
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}