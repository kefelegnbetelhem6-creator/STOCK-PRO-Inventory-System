/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package db;
import java.sql.*; 
public  class DBConnection {
     public static Connection getConnection() {
        try {
            return DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/inventory_db",
                "root",
                ""
            );
        } catch(Exception e){
            System.out.println(e);
            return null;
        }
    }
}

