package me.kixstar.kixbungeebridge.database.entities;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

import javax.persistence.*;

/**
 * We can ignore nullable warnings since they are caused by the no-arg constructor which only gets called by Hibernate,
 * and Hibernate guarantees that all of the columns aren't nullable.
 */
@Embeddable
public class Location {

    @SuppressWarnings("NullableProblems")
    @NotNull
    @Column(name = "server_handle", nullable = false)
    private String serverName;

    @SuppressWarnings("NullableProblems")
    @NotNull
    @Column(name = "world_name", nullable = false)
    private String worldName;

    private double x;
    private double y;
    private double z;

    private double yaw;
    private double pitch;

    private Location() { }

    public Location(
            @NotNull String serverName,
            @NotNull String worldName,
            double x,
            double y,
            double z,
            float yaw,
            float pitch
    ) {
        Preconditions.checkNotNull(serverName, "Argument \"serverName\" can't be null");
        Preconditions.checkNotNull(worldName, "Argument \"worldName\" can't be null");
        this.serverName = serverName;
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    @NotNull
    public String getServerName() {
        return serverName;
    }

    @NotNull
    public String getWorldName() {
        return worldName;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public double getYaw() {
        return yaw;
    }

    public double getPitch() {
        return pitch;
    }

}
