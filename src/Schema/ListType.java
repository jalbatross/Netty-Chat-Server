package Schema;

/**
 * Types of Lists used in the FBB Schema.
 * 
 * Should be used for the type field of Schema.List objects.
 * 
 * @see{@link Schema.List}
 * 
 * @author @jalbatross (Joey Albano) Aug 9 2017
 *
 */

public final class ListType {
    //Game Lobby User list
    public static final String GAME_LOBBY_USERS = "gameLobbyUsers";
    
    //List of users of a regular chat lobby
    public static final String USERS = "users";
    
    //List of server global chat lobbies
    public static final String LOBBIES = "lobbies";
    
    //List of server currently hosted game lobbies
    public static final String GAMES = "games";
}
