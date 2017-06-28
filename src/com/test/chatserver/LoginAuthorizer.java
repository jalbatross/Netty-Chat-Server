package com.test.chatserver;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

/**
 * Responsible for communication with a Postgres database. Handles user 
 * login and password verification as well as password hashing using 
 * Argon2. 
 * 
 * @author joey
 *
 */

public class LoginAuthorizer {
    
    private final String dbUrl = "jdbc:postgresql://localhost/mytestdb";
    private final String user = "postgres";
    private final String password = "postgres";
    
    public LoginAuthorizer() { }
    
    public static void main(String[] args) throws Exception {
        try {
            
            String sqlUser = "INSERT INTO AUTH(name, hash) "
                    + "VALUES(?,?)";
                        
            long id = 0;
            String pw = "abcde";
            byte[] salt = "lolol".getBytes();
            
            Argon2 instance = Argon2Factory.create(32, 136);
            String hash = instance.hash(2, 65536, 1, pw);
            System.out.println(hash);
            System.out.println("len: " + hash.length());
            
            //verifyUser("admin","abcde");
            
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(dbUrl, user, password);
            System.out.println("Connected to the PostgreSQL server successfully.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
 
        return conn;
    }
    
    /**
     * Queries the postgres database for the username and password.
     * 
     * If the user is not in the DB, returns false.
     * 
     * Otherwise, queries the database for the Argon2 hash corresponding
     * to the provided username and uses Argon2 to verify that the 
     * password is correct for the hash.
     * 
     * If the password is correct, returns true. All other situations
     * return false
     * 
     * @param name     username in psql database
     * @param password password corresponding to username
     * @return true if username and password are correct and in db, false
     *         otherwise
     */
    public boolean verifyUser(String name, String password) throws Exception {
        Connection dbConn;
        Argon2 argon2 = Argon2Factory.create();
        try {
            dbConn = this.connect();
        
            //Check if username is in DB
            String query = "SELECT hash FROM auth WHERE name = ?";
            PreparedStatement preparedQuery = dbConn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            
            preparedQuery.setString(1, name);
            
            
            ResultSet rs = preparedQuery.executeQuery();
            
            if (!rs.next() || !argon2.verify(rs.getString(1), password) ){
                return false;
            }
            else{
                return true;
            }
            
            
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    

    /**
     * Helper function for verifyUser. Checks if username is a username
     * in the psql database.
     * 
     * @param username   a username
     * @return true if username is a username in the db, false otherwise
     */
    private boolean inDb(String username) {
     
        return false;
    }
    
    
}
