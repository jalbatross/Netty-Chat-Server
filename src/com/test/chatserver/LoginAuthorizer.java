package com.test.chatserver;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

/**
 * Responsible for communication with a Postgres database. Handles user 
 * login and password verification as well as password hashing using 
 * Argon2. 
 * 
 * Requires a Postgres table named credentialsTableName that
 * has a column of usernames called usernameColumn and
 * a column of password hashes called passwordHashColumn.
 * 
 * For proper implementation, usernameColumn should have unique
 * String entries that are not case sensitive.
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
    
    private final String credentialsTableName = "auth";
    private final String usernameColumn = "name";
    private final String passwordHashColumn = "hash";
    
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
     * Verifies a username and password for login.
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
     * This implementation is considered to be more secure because we use
     * a char[] for our password parameter as opposed to String
     * 
     * @param name     username in psql database
     * @param password password corresponding to username as a char[]
     * @return         true if username and password are correct 
     *                 and in db, false otherwise
     */
    public boolean verifyLogin(String name, char[] password) throws Exception {
        
        if (!validUsername(name) || !validPassword(password)){
            return false;
        }
        
        Connection dbConn;
        Argon2 argon2 = Argon2Factory.create();
        try {
            dbConn = this.connect();
        
            //Query DB for password
            String query = "SELECT "+ passwordHashColumn + " "
                    + "FROM " + credentialsTableName + " "
                    + "WHERE " + usernameColumn + " = ?";

            PreparedStatement preparedQuery = dbConn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            
            preparedQuery.setString(1, name);
            
            ResultSet rs = preparedQuery.executeQuery();

            //Either empty resultset (user not in DB) OR bad password
            if (!rs.next() || !argon2.verify(rs.getString(1), password) ){
                Arrays.fill(password, '0');
                return false;
            }
            else{
                Arrays.fill(password, '0');
                return true;
            }   
        }
        catch (Exception e) {
            e.printStackTrace();
            Arrays.fill(password, '0');
            return false;
        }
    }

    /**
     * Verifies a username and password for login.
     * 
     * Checks valid formatting for username and password, then
     * queries the postgres database for the username and password.
     * 
     * If the username/password are malformed or the username is not
     * in the DB, returns false.
     * 
     * Otherwise, queries the database for the Argon2 hash corresponding
     * to the provided username and uses Argon2 verify function to verify that the 
     * password is correct for the hash.
     * 
     * If the password is correct, returns true. All other situations
     * return false
     * 
     * Recommended to use the implementation where char[] is used for the 
     * password parameter as opposed to String, as Strings are immutable
     * 
     * @param name     username in psql database
     * @param password password corresponding to username, as a String
     * @return         true if username and password are correct 
     *                 and in db, false otherwise
     */
    public boolean verifyLogin(String name, String password) throws Exception {

        if (!validUsername(name) || !validPassword(password)) {
            return false;
        }

        Connection dbConn;
        Argon2 argon2 = Argon2Factory.create();
        try {
            dbConn = this.connect();

            // Query DB for password
            String query = "SELECT "+ passwordHashColumn + " "
                    + "FROM " + credentialsTableName + " "
                    + "WHERE " + usernameColumn + " = ?";
            PreparedStatement preparedQuery = dbConn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

            preparedQuery.setString(1, name);

            ResultSet rs = preparedQuery.executeQuery();

            // Either empty resultset (user not in DB) OR bad password
            if (!rs.next() || !argon2.verify(rs.getString(1), password)) {
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Verifies a username and password for registration into the 
     * database.
     * 
     * If the username/password are malformed or not in accordance with
     * the specifications set by validUsername and validPassword, returns
     * false. If a username is found in the DB that is the same
     * as the username passed to the verifySignup function,
     * case insensitive, returns false.
     * 
     * 
     * 
     * @param name     A UTF-8 String
     * @param pwdChar  A char[] consisting of UTF-8 characters.
     * @return         True if the username/password combination is valid
     *                 and the database does not contain an entry with
     *                 the same username, false otherwise
     */
    public boolean verifySignup(String name, char[] password) {

        if (!validUsername(name) || !validPassword(password)) {
            return false;
        }

        Connection dbConn;
        Argon2 argon2 = Argon2Factory.create(20, 148);

        try {
            dbConn = this.connect();

            // Query DB for existence of username
            String query = "SELECT "+ passwordHashColumn + " "
                    + "FROM " + credentialsTableName + " "
                    + "WHERE " + usernameColumn + " = ? LIMIT 1";

            PreparedStatement preparedQuery = dbConn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            
            preparedQuery.setString(1, name);

            ResultSet rs = preparedQuery.executeQuery();

            // Empty result set means user not in DB
            if (!rs.next()) {
                query = "INSERT INTO " + credentialsTableName +
                        "(" + usernameColumn + ", " + passwordHashColumn+ ") "
                        + "VALUES(?,?)";
 
                preparedQuery = dbConn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                preparedQuery.setString(1,  name);
                preparedQuery.setString(2,  argon2.hash(2, 65536, 1, password));
                int returnedId = preparedQuery.executeUpdate();
                Arrays.fill(password, '0');
                return true;
            }
            else {
                Arrays.fill(password, '0');
                return false;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            Arrays.fill(password, '0');
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
    
    /**
     * Checks for valid password length. A password should be between 
     * the min and max password UTF-8 characters long.
     * @param pwd    A String password
     * @return       True if pwd is between class min/maxes
     */
    private boolean validPassword(String pwd) {
        return pwd.length() >=MIN_PW_LEN && pwd.length() <= MAX_PW_LEN;
    }
    
    public static void main (String[] args) {
        String myPass = "PlatinumP";
        
        Argon2 argon2 = Argon2Factory.create();
        
        String theHash = argon2.hash(2, 65536, 1, myPass);
        System.out.println(theHash);
        
        if (argon2.verify(theHash,myPass)) {
            System.out.println(myPass + " was the correct pw!");
            System.out.println("The hash was: " + theHash);
        }
        
        theHash = argon2.hash(2, 65536, 1, myPass);
        if (argon2.verify(theHash,myPass)) {
            System.out.println(myPass + " was the correct pw!");
            System.out.println("The hash was: " + theHash);
        }
    }

    
    
    
}
