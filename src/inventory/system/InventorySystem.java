
package inventory.system;
import db.DBConnection;
import ui.LoginForm;
import ui.SplashScreen;
public class InventorySystem {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
     
        if(DBConnection.getConnection() != null){
            System.out.println(" database Connected!");
        } else {
            System.out.println("Failed!");
        }
  new SplashScreen();
    }
}