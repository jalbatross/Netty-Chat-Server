package game;

import java.util.ArrayList;

/**
 * RPS is an extension of ServerGame which represents a standard
 * Rock-Paper-Scissors game between two human players.
 * 
 * @author @jalbatross (Joey Albano) Aug 13, 2017
 *
 */

public class RPS extends ServerGame {
   
    //byte[0] contains player1's choice 
    //byte[1] contains player2's choice
    private byte[] playerChoices = new byte[2];
    
    public static final byte ROCK = 0;
    public static final byte PAPER = 1;
    public static final byte SCISSORS = 2;
    
    public static final int MIN_PLAYERS = 2;
    public static final int MAX_PLAYERS = 2;
    public static final GameType TYPE = GameType.RPS;
    
    private String player1;
    private String player2;
    private byte winner = -1;
    private boolean gameCompleted = false;
    private boolean player1Submitted = false;
    private boolean player2Submitted = false;
    
    public RPS(ArrayList<String> players) throws Exception {
        super(players);
    }
   
    
    @Override
    public int minPlayers() {
        return MIN_PLAYERS;
    }
    
    
    @Override
    public int maxPlayers() {
        return MAX_PLAYERS;
    }

    
    /**
     * Processes a turn for the RPS game.
     * 
     * Each turnAction should be a byte[] of length 1 containing the
     * desired action as a byte.
     * A byte of 0 corresponds to ROCK
     * A byte of 1 corresponds to PAPER
     * A byte of 2 correponds to SCISSORS
     * 
     * If turnAction contains a byte other than these, the action will be
     * interpreted as ROCK.
     * 
     * If the sender of turnAction is not in the game OR if the length
     * of turnAction is not exactly 1, processAction throws an Exception
     * which should be caught by the caller and used to evict the player from
     * the RPS game.
     */
    @Override
    public void processAction(byte[] turnAction, String player) throws Exception {
        if (!players.contains(player)) {
            throw new Exception("Player: " + player + " is not in this game.");
        }
        if (turnAction.length != 1) {
            throw new Exception("Player: " + player + " sent bad turnAction data");
            
        }
        if (gameCompleted) {
            return;
        }
        
        int index = players.get(0).contentEquals(player) ? 0 : 1;
        byte action = turnAction[0];
        
        if (action > 3|| action < 0) {
            action = 0;
        }
        
        playerChoices[index] = action;
        
        if (index == 0) {
            player1Submitted = true;
        }
        else {
            player2Submitted = true;
        }
    }

    @Override
    public boolean gameOver() {
        return gameCompleted;
    }
    
    
    /**
     * Returns the winner of the RPS game as a String
     * @return   Winner of the RPS game
     */
    public String declareWinner() {
        if (gameCompleted || !readyToDeclare()) {
            return "ERROR";
        }
        gameCompleted = true;
        
        if (playerChoices[0] == ROCK && playerChoices[1] == SCISSORS) {
            return player1 + " won!";
        }
        else if (playerChoices[0] == ROCK && playerChoices[1] == PAPER) {
            return player2 + " won!";
        }
        else if (playerChoices[0] == ROCK && playerChoices[1] == ROCK) {
            return "The match was a draw.";
        }
        else if (playerChoices[0] == PAPER && playerChoices[1] == SCISSORS) {
            return player2 + " won!";
        }
        else if (playerChoices[0] == PAPER && playerChoices[1] == ROCK) {
            return player1 + " won!";
        }
        else if (playerChoices[0] == PAPER && playerChoices[1] == PAPER) {
            return "The match was a draw";
        }
        else if (playerChoices[0] == SCISSORS && playerChoices[1] == SCISSORS) {
            return "The match was a draw";
        }
        else if (playerChoices[0] == SCISSORS && playerChoices[1] == ROCK) {
            return player2 + " won!";
        }
        else if (playerChoices[0] == SCISSORS && playerChoices[1] == PAPER) {
            return player1 + " won!";
        }
        
        return "ERROR";
    }
    
    /**
     * Returns the result of the game in a size 3 byte array.
     * result =  [player1Choice, player2Choice, winner]
     * 
     * player1Choice and player2Choice are single bytes between 0 and
     * 2 inclusive.
     * 
     * ROCK: 0
     * PAPER: 1
     * SCISSORS: 2
     * 
     * winner is a single byte corresponding to the player index starting
     * from 0 of the RPS game's winner. In the case of a draw, winner is
     * 2. winner is -1 if there is an error.
     * 
     * @return byte[3] of player1's choice, player2's choice, and the 
     *         game result.
     */
    public byte[] result() {
        byte[] result = new byte[3];
        result[0] = playerChoices[0];
        result[1] = playerChoices[1];
        result[2] = winnerByte();
        
        return result;
    }
    
    /**
     * Returns the id (0 or 1) of the player. If the player is not in
     * the game, returns -1.
     * 
     * @param player       Username of a player
     * @return             RPS game's ID of the player if they are in the game,
     *                     -1 otherwise.
     */
    private int playerNumber(String player) {
        if (!players.contains(player)) {
            return -1;
        }
        
        for (int i = 0; i < 6; i++) {
            if (players.get(i).equalsIgnoreCase(player)) {
                return i;
            }
        }
        
        return -1;
    }
    
    public String gameState() {
        String ret = new String();
        ret += "--- Players ---\n" + 
        "Player 1: " + player1 + ", Choice: " + playerChoices[0] + "\n" +
        "Player 2: " + player2 + ", Choice: " + playerChoices[1] + "\n";
        
        if (gameCompleted) {
            ret += "Game completed\n";
        }
        else {
            ret += "Game in progress\n";
        }
        return ret;
    }
    
    private byte winnerByte() {
        if (!readyToDeclare()) {
            return -1;
        }
        else {
            //Draw
            if (playerChoices[0] == playerChoices[1]) {
                return 3;
            }
            
            /**
             * In other words, given any of the three choices, adding two
             * to that choice and taking the result modulo 3 gives you
             * the option that loses to the choice we started with.
             * 
             * For example, starting with scissors (2), adding 2 to this 
             * gives us 4, and taking the result modulo 3 is 1, which is
             * paper. Scissors beats paper as expected.
             */
            return (playerChoices[0] == ( (playerChoices[1] + 2) % 3) ) ? (byte)1: (byte)0;
        }
    }
    
    public boolean readyToDeclare() {
        return player1Submitted && player2Submitted;
    }


}
