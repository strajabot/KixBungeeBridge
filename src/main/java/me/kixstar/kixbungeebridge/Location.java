package me.kixstar.kixbungeebridge;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class Location {

    @NotNull
    @Column(name = "server-name", nullable = false)
    private String serverName;

    @NotNull
    @Column(name = "world-name", nullable = false)
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

    public String getServerName() {
        return serverName;
    }

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
