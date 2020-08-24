package me.kixstar.kixbungeebridge.mongodb.abstraction.player;

import com.google.common.base.Preconditions;
import me.kixstar.kixbungeebridge.mongodb.Database;
import me.kixstar.kixbungeebridge.mongodb.abstraction.DatabaseLock;
import me.kixstar.kixbungeebridge.mongodb.abstraction.LockType;
import me.kixstar.kixbungeebridge.mongodb.entities.KixPlayerData;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//todo: implement database storage
public class KixPlayer {


    @NotNull
    public static KixPlayer get(@NotNull String playerUUID) {
        //syntactic sugar
        Preconditions.checkNotNull(playerUUID, "Argument \"playerUUID\" can't be null");
        return new KixPlayer(playerUUID);
    }

    @NotNull
    private String playerUUID;

    @Nullable
    private KixPlayerData playerData;

    private KixPlayer(@NotNull String playerUUID) {
        Preconditions.checkNotNull(playerUUID, "Argument \"playerUUID\" can't be null");
        this.playerUUID = playerUUID;
    }

    @NotNull
    public KixPlayerData getData() {
        Session session = Database.getCurrentSession();
        Transaction transaction = session.beginTransaction();
        KixPlayerData playerData = session.get(KixPlayerData.class, this.playerUUID);

        if(playerData == null) {
            playerData = new KixPlayerData(this.playerUUID);
            //this may be unnecessary
            session.persist(playerData);

        }
        transaction.commit();

        this.playerData = playerData;
        return playerData;
    }

    public void setData(@NotNull KixPlayerData playerData) {
        Preconditions.checkNotNull(playerData, "Argument \"playerData\" can't be null");

    }

    public void updateData() {
        Session session = Database.getCurrentSession();
        Transaction transaction = session.beginTransaction();
        session.persist(this.playerData);
        transaction.commit();
    }

    public DatabaseLock<KixPlayerData> readLock() {
        return new DatabaseLock<>(this.getData(), LockType.READ);
    }

    public DatabaseLock<KixPlayerData> writeLock() {
        return new DatabaseLock<>(this.getData(), LockType.WRITE);
    }
}
