package com.test.chatserver;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;



public class PostgresTest {
    private final String url = "jdbc:postgresql://localhost/mytestdb";
    private final String user = "postgres";
    private final String password = "postgres";
    
    public static void main(String[] args) throws Exception {
        try {
            PostgresTest test = new PostgresTest();
            Connection conn = test.connect();
            
            
            String sqlUser = "INSERT INTO AUTH(name, hash) "
                    + "VALUES(?,?)";
            
            PreparedStatement pstmt = conn.prepareStatement(sqlUser,
                    Statement.RETURN_GENERATED_KEYS);
            
            long id = 0;
            String pw = "abcde";
            byte[] salt = "lolol".getBytes();
            
            Argon2 instance = Argon2Factory.create(20, 148);
            String hash = instance.hash(2, 65536, 1, pw);
            System.out.println(hash);
            System.out.println("len: " + hash.length());
            
            
            pstmt.setString(1, "admin");
            pstmt.setString(2,  hash);
            
            //int affectedRows = pstmt.executeUpdate();
            
            //Query DB and use Argon2 to verify correct pw
            
            String query = "SELECT hash FROM auth WHERE name = 'admin'";
            Statement queryStatement = conn.createStatement();
            ResultSet rs = queryStatement.executeQuery(query);
            
            rs.next();
            
            if(instance.verify(rs.getString(1), pw )) {
                System.out.println("Correct password!");
            }
            else {
                System.out.println("wrong password");
            }
            
            
            
            
            
            conn.close();
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
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
