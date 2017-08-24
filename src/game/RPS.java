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
    
    public static final byte NO_DATA = -1;
    public static final byte ROCK = 0;
    public static final byte PAPER = 1;
    public static final byte SCISSORS = 2;
    
    public static final int MIN_PLAYERS = 2;
    public static final int MAX_PLAYERS = 2;
    public static final GameType TYPE = GameType.RPS;
    
    private String player1;
    private String player2;
    
    private boolean gameCompleted = false;
    
    private int p1Wins = 0;
    private int p2Wins = 0;
    
    private boolean player1Submitted = false;
    private boolean player2Submitted = false;
    
    private short bestOf;
    
    private int numUpdates = 0;
    
    /**
     * Constructor for RPS object, requires an ArrayList of players and
     * a short corresponding to the 'best of' amount of amount for a player
     * to win a match. 
     * 
     * @param players    ArrayList<String> of player IDs
     * @param bestOf     Double the number of games - 1 the winning player must
     *                   win
     * @throws Exception    NullPointerException if players is null or empty   
     */
    public RPS(ArrayList<String> players, short bestOf) throws Exception {
        super(players);
        playerChoices[0] = playerChoices[1] = NO_DATA;
        if (bestOf > 7 || bestOf < 0) {
            bestOf = 3;
        }
        this.bestOf = bestOf;
    }
   
    
    @Override
    public int minPlayers() {
        return MIN_PLAYERS;
    }
    
    
    @Override
    public int maxPlayers() {
        return MAX_PLAYERS;
    }
    
    @Override
    public short bestOf() {
        return bestOf;
    }
    
    @Override
    public GameType type() {
        return TYPE;
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
        
        //Don't allow inputs if game is done or winner has been decided for round
        if (gameCompleted || readyToDeclare()) {
            return;
        }
        
        int index = players.get(0).contentEquals(player) ? 0 : 1;
        byte action = turnAction[0];
        
        System.out.println("[RPS] Processing action : " + action + " for " + player + " and index is " + index); 
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
        
        //Update wins if both players submitted
        if (readyToDeclare()) {
            updateWins();
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
     * Returns the state of the game in a size 3 byte array.
     * result =  [player1Choice, player2Choice, winner]
     * 
     * player1Choice and player2Choice are single bytes between -1 and
     * 2 inclusive.
     * 
     * NO_DATA: -1 (the player has not submitted a choice yet)
     * ROCK: 0
     * PAPER: 1
     * SCISSORS: 2
     * 
     * winner is a single byte corresponding to the player index starting
     * from 0 of the RPS game's winner. 
     * NO_DATA: -1 (the game is not over yet)
     * P1 WIN: 0
     * P2 WIN: 1
     * DRAW: 2
     * 
     * Increments numUpdates by 1 every time gameState is called.
     * 
     * @return byte[3] of player1's choice, player2's choice, and the 
     *         game's winner.
     */
    public byte[] gameState() {
        byte[] result = new byte[3];
        result[0] = playerChoices[0];
        result[1] = playerChoices[1];
        result[2] = winnerByte();
        System.out.println("[RPS] gameState called: ");
        for (int i = 0; i < result.length; i++) {
            System.out.print(result[i] + " ");
        }
        
        if (result[2] == 0) {
            System.out.println("[RPS] Player 1 won.");
        }
        else if (result[2] == 1) {
            System.out.println("[RPS] Player 2 won.");
        }
        
        if (p1Wins == ((bestOf + 1) / 2) || p2Wins == ((bestOf + 1) / 2)) {
            gameCompleted = true;
        }
        
        numUpdates++;
        
        return result;
    }
    
    /**
     * Updates the number of wins for one of the players based on the
     * winnerByte(). If winnerByte is 0, player 1's wins are incremented.
     * If winnerByte is 1, player 2's wins are incremented.
     */
    private void updateWins() {
        if (winnerByte() == 0) {
            p1Wins++;
        }
        else if (winnerByte() == 1) {
            p2Wins++;
        }
        else if (winnerByte() == 2) {
            System.out.println("[RPS] Draw");
        }
        else {
            System.out.println("[RPS] ERROR: Bad winner byte while updating wins.");
        }
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
    
    /**
     * Returns the game state as a String in the following format
     * ---Players----
     * Player 1: player1Name, Choice: player1Choice
     * Player 2: player2Name, Choice: player2Choice
     * 
     * Game completed (if game is completed), Game in progress otherwise
     */
    public String toString() {
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
    
    /**
     * Determines the winner of the game as a byte based on playerChoices[].
     * Returns -1 if both players have not submitted their choices
     * yet.
     * 
     * 0 corresponds to p1 winning, 1 corresponds to p2 winning, and 
     * 2 corresponds to a draw.
     * 
     * Winner is determined corresponding to the rules of RPS.
     * 
     * @return   byte     0 if p1win, 1 if p2win, 2 if draw, -1 otherwise
     */
    private byte winnerByte() {
        if (!readyToDeclare()) {
            return NO_DATA;
        }
        else {
            //Draw
            if (playerChoices[0] == playerChoices[1]) {
                return 2;
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
    
    /**
     * Number of times gameState() has been called before being reset.
     * @return        
     */
    public int numUpdates() {
        return numUpdates;
    }
    
    /**
     * RPS game is prepared to declare a winner.
     * @return  True if both players have submitted their choices for the round,
     *          false otherwise.
     */
    private boolean readyToDeclare() {
        return player1Submitted && player2Submitted;
    }
    
    /**
     * Resets the state of the game by setting player1Submitted and player2Sumitted
     * to false, clearing the player choices and the number of times gameState has
     * been called (numUpdates).
     */
    public void resetState() {
        player1Submitted = false;
        player2Submitted = false;
        
        playerChoices[0] = playerChoices[1] = -1;
        
        numUpdates = 0;
    }


}
