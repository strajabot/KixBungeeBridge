package me.kixstar.kixbungeebridge.feature.homes;

import com.google.common.base.Preconditions;
import me.kixstar.kixbungeebridge.KixBungeeBridge;
import me.kixstar.kixbungeebridge.Location;
import me.kixstar.kixbungeebridge.mongodb.player.KixPlayer;
import me.kixstar.kixbungeebridge.mongodb.player.KixPlayerData;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class HomeService {

    /** NOTE: permissions are not checked by this method, instead they are checked inside the command listeners
     *  However, the maximum number of houses of the player is checked here.
     */
    public static CompletableFuture setHome(
            @NotNull String playerUUID,
            @NotNull String homeName,
            @NotNull Location location
    ) {
        Preconditions.checkNotNull(playerUUID, "Argument \"playerUUID\" can't be null");
        Preconditions.checkNotNull(homeName, "Argument \"homeName\" can't be null");
        Preconditions.checkNotNull(location, "Argument \"location\" can't be null");

        KixPlayer kixPlayer = KixPlayer.getPlayer(playerUUID);

        CompletableFuture<Void> setHomeFuture = CompletableFuture.runAsync(() -> {
            kixPlayer.lock();
            KixPlayerData playerData = kixPlayer.getData();
            int maxHomes = getMaxHomes(playerUUID);
            Map<String, Location> homes = playerData.getHomes();
            //no need to check number of homes when setting the "default" home or if an existing home is edited
            if(homeName == "default" || homes.containsKey(homeName))  {
                homes.put(homeName, location);
                playerData.setHomes(homes);
                kixPlayer.updateData();
                return;
            }
            if(homes.size() + 1 > maxHomes) throw new HomeSlotsMaxedException(playerUUID, homeName);
            homes.put(homeName, location);
            playerData.setHomes(homes);
            kixPlayer.updateData();
            return;
        });

        setHomeFuture.whenComplete((r,t) -> {
            //unlock the object even if the update completes exceptionally
            kixPlayer.unlock();
        });

        return setHomeFuture;

    }

    /**
     * Completes with the Location of the Player's home that is named homeName
     * Completes with null if the Player doesn't have a home named homeName.
     *
     * @param playerUUID UUID of the player
     * @param homeName name of the home
     * @return CompletableFuture which completes with the Location of the home or null (read desc.)
     */
    @NotNull
    public static CompletableFuture<Location> getHome(
            @NotNull String playerUUID,
            @NotNull String homeName
    ) {
        Preconditions.checkNotNull(playerUUID, "Argument \"playerUUID\" can't be null");
        Preconditions.checkNotNull(homeName, "Argument \"homeName\" can't be null");

        KixPlayer kixPlayer = KixPlayer.getPlayer(playerUUID);

        CompletableFuture<Location> getHomeFuture = CompletableFuture.supplyAsync(() -> {
            kixPlayer.lock();
            KixPlayerData playerData = kixPlayer.getData();
            //todo: probably should implement read/write lock instead of a simple lock to improve read performance at some point
            Map<String, Location> homes = playerData.getHomes();
            return homes.get(homeName);
        });

        getHomeFuture.whenComplete((result, ex) -> {
            //unlock the object even if the update completes exceptionally
            kixPlayer.unlock();
        });

        return getHomeFuture;

    }

    /**
     * Returns A map of home names and their locations
     * note: home "default" is the home that gets set the player runs /sethome without arguments
     * the Location of the "default" home can be null, this means that it hasn't been set yet.
     *
     * @param playerUUID UUID of the player
     * @return CompletableFuture
     */
    @NotNull
    public static CompletableFuture<Map<String, Location>> getHomes(
            @NotNull String playerUUID
    ) {
        Preconditions.checkNotNull(playerUUID, "Argument \"playerUUID\" can't be null");

        KixPlayer kixPlayer = KixPlayer.getPlayer(playerUUID);

        CompletableFuture<Map<String, Location>> getHomesFuture = CompletableFuture.supplyAsync(() -> {
            kixPlayer.lock();
            KixPlayerData playerData = kixPlayer.getData();
            //todo: probably should implement read/write lock instead of a simple lock to improve read performance at some point
            return playerData.getHomes();
        });

        getHomesFuture.whenComplete((result, ex) -> {
            //unlock the object even if the update completes exceptionally
            kixPlayer.unlock();
        });

        return getHomesFuture;

    }

    /**
     * Gets the maximum number of homes that this player can own
     * This is read from LuckPerms metadata, the key is "max-homes"
     * @param playerUUID UUID of the player
     * @return maximum number of homes
     */
    @NotNull
    public static int getMaxHomes(@NotNull String playerUUID) {
        Preconditions.checkNotNull(playerUUID, "Argument \"playerUUID\" can't be null");
        LuckPerms luckPerms = KixBungeeBridge.getLuckPerms();
        User user = luckPerms.getUserManager().getUser(playerUUID);
        if(user == null) return 0;
        Optional<QueryOptions> options = luckPerms.getContextManager().getQueryOptions(user);
        if(!options.isPresent()) return 0;
        CachedMetaData metaData = user.getCachedData().getMetaData(options.get());
        String maxHomes = metaData.getMetaValue("max-homes");
        if(maxHomes == null) return 0;
        try {
            return Integer.parseInt(maxHomes);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return 0;
        }
    }

}
