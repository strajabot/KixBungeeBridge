package me.kixstar.kixbungeebridge.feature.warps;

import com.google.common.base.Preconditions;
import me.kixstar.kixbungeebridge.database.entities.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

/** Services only check for permissions that are dependant on the database entries, example:
 *  this service doesn't check for permission "kix.utilities.warp.set" since that doesn't rely on data from the database
 *  however it does check if the player has the permission to modify a warp that already exists based on that warp's
 *  visibility permission (if the warp has the visibility permission of "kix.utilities.warp.visibility.admin-warps" and the player
 *  doesn't have that permission the warp won't be updated).
 */
public class WarpService {

    @NotNull
    public static CompletableFuture<Void> setWarp(
            @NotNull String warpName,
            @NotNull Location location,
            @Nullable String visibility
    ) {
        Preconditions.checkNotNull(warpName, "Argument \"warpName\" can't be null");
        Preconditions.checkNotNull(location, "Argument \"location\" can't be null");

        //todo: implement
        return new CompletableFuture<>();
    }

}
