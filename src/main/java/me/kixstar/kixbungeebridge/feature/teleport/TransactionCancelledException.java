package me.kixstar.kixbungeebridge.feature.teleport;

public class TransactionCancelledException extends Exception {

    public TransactionCancelledException() {
        super("Cannot change state of a cancelled transaction");
    }
}
