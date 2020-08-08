package me.kixstar.kixbungeebridge.mongodb.player;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;


//todo: implement database storage
public class KixPlayer {



    @NotNull
    public static KixPlayer getPlayer(@NotNull String playerUUID) {
        Preconditions.checkNotNull(playerUUID, "Argument \"playerUUID\" can't be null");
        //todo: implement
        return null;
    }

    @NotNull
    public KixPlayerData getData() {
        //implement getting data from the database
        return null;
    }

    public void setData(@NotNull KixPlayerData playerData) {
        Preconditions.checkNotNull(playerData, "Argument \"playerData\" can't be null");
    }

    public void updateData() {
        //todo: update the database
    }

    public void lock() {
        //todo: acquire a lock on the user's document in the database.
    }

    public void unlock() {
        //todo: free the lock in the database.
        //maybe call KixPlayer::updateData every time this is called since the lock is used only when updating.
    }
}
