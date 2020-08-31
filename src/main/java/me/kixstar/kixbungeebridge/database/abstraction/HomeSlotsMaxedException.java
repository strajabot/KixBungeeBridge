package me.kixstar.kixbungeebridge.database.abstraction;


public class HomeSlotsMaxedException extends RuntimeException {

    public final String playerUUID;
    public final String homeName;

    public HomeSlotsMaxedException(String playerUUID, String homeName) {
        //generic error message. Usually overridden to give player / console more info
        super("Player with UUID \"" + playerUUID + "\" has no more available home slots so home \"" + homeName + "\" can't be set");
        this.playerUUID = playerUUID;
        this.homeName = homeName;
    }

}
