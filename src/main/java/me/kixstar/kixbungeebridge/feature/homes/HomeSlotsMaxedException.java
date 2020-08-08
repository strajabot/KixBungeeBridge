package me.kixstar.kixbungeebridge.feature.homes;


public class HomeSlotsMaxedException extends RuntimeException {

    public final String playerUUID;
    public final String homeName;

    public HomeSlotsMaxedException(String playerUUID, String homeName) {
        super("Player with UUID \"" + playerUUID + "\" has no more available home slots so home \"" + homeName + "\" can't be set");
        this.playerUUID = playerUUID;
        this.homeName = homeName;
    }

}
