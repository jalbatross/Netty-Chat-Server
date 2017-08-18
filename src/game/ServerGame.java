package game;

import java.util.ArrayList;

/**
 * ServerGame is a template class for the Games that will be hosted
 * on the server. Subclasses of ServerGame are responsible for their
 * implementations of how they handle player turns and for deciding
 * when each game is completed.
 * 
 * Each subclass is required to implement the minPlayer() and maxPlayer()
 * functions.
 * 
 * Each subclass should also declare a static final constant variable specifying
 * its GameType.
 * 
 * @author @jalbatross (Joey Albano) Aug 13, 2017
 *
 */

public abstract class ServerGame {

    protected final ArrayList<String> players;
    
    public ServerGame(ArrayList<String> players) throws Exception {
        if (players == null) {
            throw new Exception("Invalid Game Data Received");
        }
        if (players.size() < minPlayers() || players.size() > maxPlayers()) {
            throw new Exception("Invalid num players for Game");
        }
        
        this.players = players;
    }
    
    /**
     * Returns the players arrayList. Its size will be equal to the number
     * of players used to start the game, but the number of actual players
     * playing may change over the course of a game.
     * 
     * @see{@link numPlayers()}
     * 
     * @return       ArrayList<String> of player usernames in the ServerGame
     */
    public ArrayList<String> players() {
        return this.players;
    }
    
    /**
     * Returns the number of players currently in the game. This is
     * NOT always the same as players.size() because of the possibility
     * that players leave throughout the course of the game. Players who
     * were previously in the game but have left have their references in
     * players changed to the empty String.
     * 
     * @return   Number of players currently active in the ServerGame.
     */
    public int numPlayers() {
        int validPlayers = 0;
        for (String playerName: players) {
            if (!playerName.contentEquals("")) {
                validPlayers++;
            }
        }
        return validPlayers;
    }
   
    /**
     * removePlayer iterates through players. If it finds a String
     * matching playerName, it replaces the String found at that
     * index with "".
     * 
     * @param playerName    A player's name
     * @return              True if the player's name is set to "",
     *                      false otherwise.
     */
    public boolean removePlayer(String playerName) {
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).contentEquals(playerName)) {
                players.set(i,"");
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Minimum number of players required to play the ServerGame.
     * Specify as a class constant.
     * 
     */
    public abstract int minPlayers();
    
    /**
     * Maximum number of players allowed to play the ServerGame.
     * Specify as a class constant.
     */
    public abstract int maxPlayers();
    
    /**
     * Returns the maximum number of games to be played.
     */
    public abstract short bestOf();
    
    /**
     * Type of ServerGame. 
     * Specify as a class constant.
     */
    public abstract GameType type();
    
    /**
     * A function that processes a series of bytes as a particular player's
     * turnAction. If player is not found in players, this function should
     * throw an exception to indicate that.
     * 
     * @param turnAction       Byte array of actions as determined by subclass
     * @param player           Username of player in the ServerGame
     * @throws Exception       Throw exception if player is not in the Game.
     */
    public abstract void processAction(byte[] turnAction, String player) throws Exception;
    
    /**
     * Specifies the current gamestate as a byte[].
     * 
     * @return
     */
    public abstract byte[] gameState();
    
    /**
     * Indicates whether or not the ServerGame is completed.
     * @return true if the game has ended, false otherwise
     */
    public abstract boolean gameOver();

}
