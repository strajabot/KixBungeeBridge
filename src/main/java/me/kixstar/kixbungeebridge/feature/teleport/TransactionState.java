package me.kixstar.kixbungeebridge.feature.teleport;

import java.util.concurrent.CompletableFuture;

public enum TransactionState {

    /* When a transaction is in this state teleport delay timer is started
    and the state is immediately switched to the CONTACT state. */
    INITIAL,

    /* When a transaction is in this state SubscribeTeleportPackets are
    sent to the player and target servers to inform them of the existence
    of the transaction. If both the player and target server correctly
    subscribe the state is switched to the INFORMATION state. Otherwise
    the state is switched to the CANCELED state. */
    CONTACT,

    /* When a tranasction is in this state two types of packets can be
    sent immediately:
        1. A LocationTeleportPacket if a player is teleporting to fixed
        coordinates on the target server.
        2. A PlayerTeleportPacket if is a player is teleporting to another
        player on the target server.
    If both servers respond with a ConfirmReadyTeleportPacket within 10
    seconds and the transaction's delay has passed the state is switched
    to the EXECUTION state. Otherwise the state is switched to CANCELED. */
    INFORMATION,

    /* When a tranasction is in this state a ExecuteTeleport packet is
    sent. if the servers respond with a ConfirmReadyTeleportPacket within
    10 seconds the player is connected to the target server if needed
    and the state is switched to the FINISHED state. Otherwise the state
    is switched to CANCELED.
     */
    EXECUTION,

    /* When a transaction is in this state the transaction is considered
    completed. No packet can be received from or sent to the transaction's
    channel anymore. */
    FINISHED,

    /* When a transaction is in this state a CancelTeleportPacket is sent.
    after the packet is sent the transaction is considered done and no
    packets can be received from or sent to the transaction's channel.
    All players that were part of the transaction are informed of the
    suspected cancellation reason. */
    CANCELED;
}
