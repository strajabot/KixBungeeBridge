package me.kixstar.kixbungeebridge.database.entities;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/**
 * We can ignore nullable warnings since they are caused by the no-arg constructor which only gets called by Hibernate,
 * and Hibernate guarantees that all of the columns aren't nullable.
 *
 * HomeID should only be used by the Home Hibernate entity, you should instead look at that class if you need to get
 * the entity
 */
@Embeddable
public class HomeID implements Serializable {

    @SuppressWarnings("NullableProblems")
    @NotNull
    @Column(name = "player_uuid")
    private String ownerUUID;

    @SuppressWarnings("NullableProblems")
    @NotNull
    private String name;

    public HomeID() {}

    HomeID(@NotNull String ownerUUID, @NotNull String name) {
        Preconditions.checkNotNull(ownerUUID, "Argument \"playerUUID\" can't be null");
        Preconditions.checkNotNull(name, "Argument \"name\" can't be null");

        this.ownerUUID = ownerUUID;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HomeID homeID = (HomeID) o;
        return this.ownerUUID.equals(homeID.getOwnerUUID()) &&
                this.name.equals(homeID.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.ownerUUID, this.name);
    }

    @NotNull
    public String getOwnerUUID() {
        return this.ownerUUID;
    }

    @NotNull
    public String getName() {
        return this.name;
    }

}
