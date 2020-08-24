package me.kixstar.kixbungeebridge.mongodb.entities;

import com.google.common.base.Preconditions;
import me.kixstar.kixbungeebridge.Location;

import org.jetbrains.annotations.NotNull;

import javax.persistence.Id;
import javax.persistence.OneToOne;

public class WarpData {

    @NotNull
    @Id
    private String name;

    @NotNull
    private String permission;

    @NotNull
    @OneToOne
    private Location location;

    public WarpData(
            String name,
            String permission,
            Location location

    ) {
        Preconditions.checkNotNull(name, "Argument \"name\" can't be null");
        Preconditions.checkNotNull(permission, "Argument \"permission\" can't be null");
        Preconditions.checkNotNull(location, "Argument \"location\" can't be null");
        this.name = name;
        this.permission = permission;
        this.location = location;
    }
}
