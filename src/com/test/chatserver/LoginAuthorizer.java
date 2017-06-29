package com.test.chatserver;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.lang3.StringUtils;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

/**
 * Responsible for communication with a Postgres database. Handles user 
 * login and password verification as well as password hashing using 
 * Argon2. 
 * 
 * @author jalbatross (Joey Albano)
 *
 */

public class LoginAuthorizer {

    public static final int MIN_PW_LEN = 1;
    public static final int MAX_PW_LEN = 42;
    
    public static final int MIN_USER_LEN = 1;
    public static final int MAX_USER_LEN = 16;
   
    private final String dbUrl = "jdbc:postgresql://localhost/mytestdb";
    private final String user = "postgres";
    private final String password = "postgres";
    
    public LoginAuthorizer() { }
    
    public Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(dbUrl, user, password);
            System.out.println("Connected to the PostgreSQL server successfully.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println("failed to connect");
        }
 
        return conn;
    }
    
    /**
     * Verifies a username and password.
     * 
     * Checks valid formatting for username and password, then
     * queries the postgres database for the username and password.
     * 
     * If the username/password are malformed or the username is not
     * in the DB, returns false.
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
    public boolean verifyUser(String name, char[] password) throws Exception {
        
        if (!validUsername(name) || !validPassword(password)){
            return false;
        }
        
        Connection dbConn;
        Argon2 argon2 = Argon2Factory.create();
        try {
            dbConn = this.connect();
        
            //Query DB for password
            String query = "SELECT hash FROM auth WHERE name = ?";
            PreparedStatement preparedQuery = dbConn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            
            preparedQuery.setString(1, name);
            
            
            ResultSet rs = preparedQuery.executeQuery();
            
            //Either empty resultset (user not in DB) OR bad password
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
    public boolean inDb(String username) {
     
        return false;
    }
    
    /**
     * Checks formatting and structure of a username. A valid username
     * has the following characteristics:
     * 
     * Between MIN_USER_LEN and MAX_USER_LEN UTF-8 characters long, 
     * alphanumeric, not all whitespace
     * 
     * @param username  A username string
     * @return          True if valid, false otherwise
     */
    private boolean validUsername(String username) {
        if (username.length() > MAX_USER_LEN || 
            username.length() < MIN_USER_LEN) {
            return false;
        }
        else if (StringUtils.isBlank(username) || 
                !StringUtils.isAlphanumericSpace(username)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Checks for valid password length. A password should be between 
     * the min and max password UTF-8 characters long.
     * @param pwd    A char[] password
     * @return       True if pwd is between class min/maxes
     */
    private boolean validPassword(char[] pwd) {
        return pwd.length >=MIN_PW_LEN && pwd.length <= MAX_PW_LEN;
    }
    
    
}
