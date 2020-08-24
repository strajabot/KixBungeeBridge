package me.kixstar.kixbungeebridge.mongodb.abstraction.warp;

import com.google.common.base.Preconditions;
import me.kixstar.kixbungeebridge.mongodb.Database;
import me.kixstar.kixbungeebridge.mongodb.abstraction.DatabaseLock;
import me.kixstar.kixbungeebridge.mongodb.abstraction.LockType;
import me.kixstar.kixbungeebridge.mongodb.entities.WarpData;
import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.mongodb.client.model.Filters.eq;

public class Warp {

    @NotNull
    public static Warp get(@NotNull String name) {
        Preconditions.checkNotNull(name, "Argument \"name\" can't be null");

        return new Warp(name);
    }

    private Warp(@NotNull String name) {
        Preconditions.checkNotNull(name, "Argument \"name\" can't be null");
        this.name = name;

    }

    @NotNull
    private String name;

    private WarpData warpData;

    @Nullable
    public WarpData getData() {
        Session session = Database.getCurrentSession();
        Transaction transaction = session.beginTransaction();
        this.warpData = session.get(WarpData.class, this.name);
        transaction.commit();

        return warpData;
    }

    public void setData(@NotNull WarpData data) {
        this.setData(data);
    }

    public void updateData() {
        Session session = Database.getCurrentSession();
        Transaction transaction = session.beginTransaction();
        session.persist(this.warpData);
        transaction.commit();
    }

    public DatabaseLock<WarpData> readLock() {
        return new DatabaseLock<>(this.getData(), LockType.READ);
    }

    public DatabaseLock<WarpData> writeLock() {
        return new DatabaseLock<>(this.getData(), LockType.WRITE);
    }

}
