package me.kixstar.kixbungeebridge.rabbitmq;

public class Location {

    private String serverName;
    private String worldName;

    private double x;
    private double y;
    private double z;

    private double yaw;
    private double pitch;

    Location() { }

    public Location(
            String serverName,
            String worldName,
            double x,
            double y,
            double z,
            double yaw,
            double pitch
    ) {
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
