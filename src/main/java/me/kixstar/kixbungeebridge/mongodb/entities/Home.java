package me.kixstar.kixbungeebridge.mongodb.entities;

import com.google.common.base.Preconditions;
import me.kixstar.kixbungeebridge.Location;
import org.hibernate.annotations.GenericGenerator;
import org.jetbrains.annotations.NotNull;

import javax.persistence.*;
import java.io.Serializable;

@Embeddable
public class Home implements Serializable {

    private String name;

    @OneToOne
    private Location location;

    public Home() {}

    public Home(
            @NotNull String name,
            @NotNull Location location
    ) {
        Preconditions.checkNotNull(name, "Argument \"name\" can't be null");
        Preconditions.checkNotNull(location, "Argument \"location\" can't be null");
        this.name = name;
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public void setName(@NotNull String name) {
        Preconditions.checkNotNull(name, "Argument \"name\" can't be null");
        this.name = name;
    }

    @NotNull
    public Location getLocation() {
        return location;
    }

    public void setLocation(@NotNull Location location) {
        Preconditions.checkNotNull(location, "Argument \"location\" can't be null");
        this.location = location;
    }

}
