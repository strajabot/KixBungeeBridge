package me.kixstar.kixbungeebridge.feature.homes;

public class HomeNotExistException extends RuntimeException {

    public final String playerUUID;
    public final String homeName;

    public HomeNotExistException(String playerUUID, String homeName) {
        //generic error message. Usually overridden to give player / console more info
        super("Player with UUID \"" + playerUUID + "\" doesn't own a home \"" + homeName + "\"");
        this.playerUUID = playerUUID;
        this.homeName = homeName;
    }
}
