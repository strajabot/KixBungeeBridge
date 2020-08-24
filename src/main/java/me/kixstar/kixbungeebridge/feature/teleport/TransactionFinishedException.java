package me.kixstar.kixbungeebridge.feature.teleport;

public class TransactionFinishedException extends Exception {

    public TransactionFinishedException() {
        super("Cannot change state of a finished transaction");
    }
}
