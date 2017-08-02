package game;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum GameType {
    RPS("rps"),
    COUP("coup");
    
    private final String type;
    private GameType(final String type) {
        this.type = type;
    }
    private static final Map<String, GameType> typeMap = Collections.unmodifiableMap(initMapping());
   
    private static Map<String, GameType> initMapping() {
        Map<String, GameType> myMap = new HashMap<String, GameType>();
        myMap.put("rps", RPS);
        myMap.put("coup", COUP);
        
        return myMap;
    }
    
    public static boolean typeExists(String type) {
        return typeMap.containsKey(type);
    }
    
    public static GameType fromString(String type) {
        return typeMap.get(type);
    }
    
}
