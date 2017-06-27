package com.test.chatserver;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;



public class PostgresTest {
    private final String url = "jdbc:postgresql://localhost/mytestdb";
    private final String user = "postgres";
    private final String password = "postgres";
    
    public static void main(String[] args) {
        PostgresTest test = new PostgresTest();
        test.connect();
    }
    
    public Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to the PostgreSQL server successfully.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
 
        return conn;
    }
}
