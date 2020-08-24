package me.kixstar.kixbungeebridge.mongodb.abstraction;

import com.google.common.base.Preconditions;
import me.kixstar.kixbungeebridge.mongodb.Database;
import org.hibernate.*;
import org.jetbrains.annotations.NotNull;

public class DatabaseLock<T> {

    @NotNull
    private T entity;

    @NotNull
    private LockType lockType;

    private volatile boolean isLocked = false;

    public DatabaseLock(@NotNull T entity, @NotNull LockType lockType) {
        Preconditions.checkNotNull(entity, "Argument \"entity\" can't be null");
        Preconditions.checkNotNull(lockType, "Argument \"lockType\" can't be null");
        this.entity = entity;
        this.lockType = lockType;
    }

    public void lock() {
        //setup options
        if(isLocked) throw new RuntimeException("Can't lock object that is already locked");

        LockOptions lockOptions = new LockOptions();
        lockOptions.setLockMode(this.lockType.getMode());
        lockOptions.setScope(true);
        //5 seconds timeout
        lockOptions.setTimeOut(5000);

        Session session = Database.getCurrentSession();
        Transaction transaction = session.beginTransaction();
        session.buildLockRequest(lockOptions).lock(this.entity);
        transaction.commit();
        this.isLocked = true;
    }

    public void unlock() {
        //todo: create a custom error for this
        if(!this.isLocked) throw new RuntimeException("Can't unlock object which isn't locked");
        //calling Session::lock() with LockMode.NONE unlocks the object

        Session session = Database.getCurrentSession();
        Transaction transaction = session.beginTransaction();
        session.lock(this.entity, LockMode.NONE);
        transaction.commit();

        this.isLocked = false;
    }

}
