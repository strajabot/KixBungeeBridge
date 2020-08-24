package me.kixstar.kixbungeebridge.mongodb.abstraction;

import org.hibernate.LockMode;

public enum LockType {
    READ(false),
    WRITE(true);

    private LockMode mode;

    LockType(boolean isWriteLock) {
        if (isWriteLock) {
            this.mode = LockMode.PESSIMISTIC_WRITE;
        } else {
            this.mode = LockMode.PESSIMISTIC_READ;
        }
    }

    public LockMode getMode() {
        return mode;
    }
}
