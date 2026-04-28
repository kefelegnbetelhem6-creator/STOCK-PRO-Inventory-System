package util;
import java.awt.*;
import javax.swing.*;

public class Style {
    public static final Color BG_DARK = new Color(15, 15, 26);    
    public static final Color SIDEBAR_BG = new Color(22, 22, 38); 
    public static final Color ACCENT = new Color(78, 204, 163);  
    public static final Color ERROR = new Color(233, 69, 96);    
    public static final Color CARD_BG = new Color(30, 30, 50);

    public static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font CARD_FONT = new Font("Segoe UI", Font.BOLD, 14);

    public static ImageIcon getIcon(String path, int w, int h) {
        try {
            java.net.URL imgURL = Style.class.getResource(path);
            if (imgURL != null) {
                Image img = new ImageIcon(imgURL).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
                return new ImageIcon(img);
            }
        } catch (Exception e) {}
        return null;
    }
}