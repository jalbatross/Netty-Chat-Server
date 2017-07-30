package game;

import java.util.ArrayList;

public class RPS extends ServerGame {
    
    private String[] playerChoices;
    private String player1;
    private String player2;
    private boolean gameCompleted = false;
    private boolean player1Submitted = false;
    private boolean player2Submitted = false;
    public static final int NUM_PLAYERS = 2;
    
    
    public RPS(int numPlayers, ArrayList<String> players) throws Exception {
        super(numPlayers, players);
        if (numPlayers != NUM_PLAYERS) {
            throw new Exception("Invalid num players for RPS, got " + numPlayers);
        }
        playerChoices = new String[NUM_PLAYERS];
        playerChoices[0] = playerChoices[1] = "rock";
        player1 = players.get(0);
        player2 = players.get(1);
    }
    
    public void processChoice (String choice, String player) throws Exception {
        if (!players.contains(player)) {
            throw new Exception("Player: " + player + " is not in this game.");
        }
        choice = choice.toLowerCase();
        
        int playerId = playerNumber(player);
        
        if (playerId == 0 && player1Submitted) {
            System.out.println("[RPS] P1 tried to submit again");
            return;
        }
        else if (playerId == 1 && player2Submitted) {
            System.out.println("[RPS] P2 tried to submit again");
            return;
        }
        if (!choice.contentEquals("rock") 
            && !choice.contentEquals("paper") 
            && !choice.contentEquals("scissors")) {
            choice = "rock";
        }
        playerChoices[playerId] = choice;
        if (playerId == 0) {
            player1Submitted = true;
        }
        else {
            player2Submitted = true;
        }
        
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
        if (playerChoices[0].contentEquals("rock") && playerChoices[1].contentEquals("scissors")) {
            return player1 + " won!";
        }
        else if (playerChoices[0].contentEquals("rock") && playerChoices[1].contentEquals("paper")) {
            return player2 + " won!";
        }
        else if (playerChoices[0].contentEquals("rock") && playerChoices[1].contentEquals("rock")) {
            return "The match was a draw.";
        }
        else if (playerChoices[0].contentEquals("paper") && playerChoices[1].contentEquals("scissors")) {
            return player2 + " won!";
        }
        else if (playerChoices[0].contentEquals("paper") && playerChoices[1].contentEquals("rock")) {
            return player1 + " won!";
        }
        else if (playerChoices[0].contentEquals("paper") && playerChoices[1].contentEquals("paper")) {
            return "The match was a draw";
        }
        else if (playerChoices[0].contentEquals("scissors") && playerChoices[1].contentEquals("scissors")) {
            return "The match was a draw";
        }
        else if (playerChoices[0].contentEquals("scissors") && playerChoices[1].contentEquals("rock")) {
            return player2 + " won!";
        }
        else if (playerChoices[0].contentEquals("scissors") && playerChoices[1].contentEquals("paper")) {
            return player1 + " won!";
        }
        
        return "ERROR";
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
        
        for (int i = 0; i < NUM_PLAYERS; i++) {
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
    
    public boolean readyToDeclare() {
        return player1Submitted && player2Submitted;
    }

}
