package com.test.chatserver;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.abstractj.kalium.NaCl;
import org.abstractj.kalium.crypto.Password;
import org.abstractj.kalium.encoders.Encoder;

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
        
            String sql = "CREATE TABLE AUTH (" +
                    " id serial NOT NULL ," +
                    " name VARCHAR(12)," + 
                    " hash CHAR(255)," + 
                    " PRIMARY KEY (id))"; 
            Statement stmt = conn.createStatement();
            //stmt.executeUpdate(sql);
        
            //System.out.println("made sql table in db!");
            
            String pw = "abcde";
            byte[] salt = "lolol".getBytes();
            long memlim = NaCl.Sodium.PWHASH_SCRYPTSALSA208SHA256_MEMLIMIT_INTERACTIVE;
            
            Password pwd = new Password();
            pwd.hash(255, pw.getBytes(), Encoder.UTF_8, salt, 
                    NaCl.Sodium.CRYPTO_PWHASH_SCRYPTSALSA208SHA256_OPSLIMIT_INTERACTIVE,
                    memlim)
            
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
