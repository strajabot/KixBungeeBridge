package me.kixstar.kixbungeebridge.database.entities;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

import javax.persistence.*;
import java.io.Serializable;

/**
 * We can ignore nullable warnings since they are caused by the no-arg constructor which only gets called by Hibernate,
 * and Hibernate guarantees that all of the columns aren't nullable.
 */
@Entity
public class HomeData implements Serializable {

    @SuppressWarnings("NullableProblems")
    @NotNull
    @EmbeddedId
    @Column(name = "home_id", nullable = false)
    private HomeID id;

    @SuppressWarnings("NullableProblems")
    @NotNull
    @Embedded
    @Column(nullable = false)
    private Location location;

    //not
    public HomeData() {}

    public HomeData(
            @NotNull String ownerUUID,
            @NotNull String homeName,
            @NotNull Location location
    ) {
        Preconditions.checkNotNull(ownerUUID, "Argument \"ownerUUID\" can't be null");
        Preconditions.checkNotNull(homeName, "Argument \"homeName\" can't be null");
        Preconditions.checkNotNull(location, "Argument \"location\" can't be null");
        this.id = new HomeID(ownerUUID, homeName);
        this.location = location;
    }

    @NotNull
    public String getName() {
        return this.id.getName();
    }

    @NotNull
    public String getOwnerUUID() {
        return this.id.getOwnerUUID();
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
