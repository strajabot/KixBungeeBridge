package me.kixstar.kixbungeebridge.feature.homes;

import com.google.common.base.Preconditions;
import me.kixstar.kixbungeebridge.KixBungeeBridge;
import me.kixstar.kixbungeebridge.Location;
import me.kixstar.kixbungeebridge.mongodb.abstraction.DatabaseLock;
import me.kixstar.kixbungeebridge.mongodb.abstraction.player.KixPlayer;
import me.kixstar.kixbungeebridge.mongodb.entities.Home;
import me.kixstar.kixbungeebridge.mongodb.entities.KixPlayerData;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
import org.jetbrains.annotations.NotNull;

import java.util.*;
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

        KixPlayer kixPlayer = KixPlayer.get(playerUUID);

        final DatabaseLock<KixPlayerData> playerLock = kixPlayer.writeLock();

        CompletableFuture<Void> setHomeFuture = CompletableFuture.runAsync(() -> {
            playerLock.lock();
            KixPlayerData playerData = kixPlayer.getData();
            int maxHomes = getMaxHomes(playerUUID);
            List<Home> homes = playerData.getHomes();
            ListIterator<Home> iterator = homes.listIterator();

            while(iterator.hasNext()) {
                Home home = iterator.next();
                if(!home.getName().equals(homeName)) continue;
                //replace the old home with new one
                iterator.set(new Home(homeName, location));
                //update the local cache and then trigger an update to the database
                playerData.setHomes(homes);
                kixPlayer.updateData();
                return;
            }

            if(homes.size() + 1 > maxHomes)throw new HomeSlotsMaxedException(playerUUID, homeName);
            homes.add(new Home(homeName, location));
            //update the local cache and then trigger an update to the database
            playerData.setHomes(homes);
            kixPlayer.updateData();
            return;
        });

        setHomeFuture.whenComplete((r, t) -> {
            //unlock the object even if the update completes exceptionally
            playerLock.unlock();
        });

        return setHomeFuture;

    }

    @NotNull
    public static CompletableFuture<Void> deleteHome(
            @NotNull String playerUUID,
            @NotNull String homeName
    ) {
        Preconditions.checkNotNull(playerUUID, "Argument \"playerUUID\" can't be null");
        Preconditions.checkNotNull(homeName, "Argument \"homeName\" can't be null");

        KixPlayer kixPlayer = KixPlayer.get(playerUUID);

        final DatabaseLock<KixPlayerData> playerLock = kixPlayer.writeLock();

        CompletableFuture<Void> deleteHomeFuture = CompletableFuture.runAsync(() -> {
            playerLock.lock();
            KixPlayerData playerData = kixPlayer.getData();
            List<Home> homes = playerData.getHomes();
            ListIterator<Home> iterator = homes.listIterator();

            while(iterator.hasNext()) {
                Home home = iterator.next();
                if(!home.getName().equals(homeName)) continue;
                //remove the home
                iterator.remove();
                //update the local cache and then trigger an update to the database
                playerData.setHomes(homes);
                kixPlayer.updateData();
                return;
            }
            //runs only if home couldn't be found
            throw new HomeNotExistException(playerUUID, homeName);

        });

        deleteHomeFuture.whenComplete((result, ex) ->
                //unlock the object even if the update completes exceptionally
                playerLock.unlock()
        );

        return deleteHomeFuture;
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

        KixPlayer kixPlayer = KixPlayer.get(playerUUID);

        final DatabaseLock<KixPlayerData> playerLock = kixPlayer.readLock();

        CompletableFuture<Location> getHomeFuture = CompletableFuture.supplyAsync(() -> {
            playerLock.lock();
            KixPlayerData playerData = kixPlayer.getData();
            List<Home> homes = playerData.getHomes();
            ListIterator<Home> iterator = homes.listIterator();

            while(iterator.hasNext()) {
                Home home = iterator.next();
                if(home.getName().equals(homeName)) return home.getLocation();
            }
            return null;
        });

        getHomeFuture.whenComplete((result, ex) -> {
            //unlock the object even if the update completes exceptionally
            playerLock.unlock();
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
    public static CompletableFuture<List<Home>> getHomes(
            @NotNull String playerUUID
    ) {
        Preconditions.checkNotNull(playerUUID, "Argument \"playerUUID\" can't be null");

        KixPlayer kixPlayer = KixPlayer.get(playerUUID);

        final DatabaseLock<KixPlayerData> playerLock = kixPlayer.readLock();

        CompletableFuture<List<Home>> getHomesFuture = CompletableFuture.supplyAsync(() -> {
            playerLock.lock();
            KixPlayerData playerData = kixPlayer.getData();
            //todo: probably should implement read/write lock instead of a simple lock to improve read performance at some point
            return playerData.getHomes();
        });

        getHomesFuture.whenComplete((result, ex) -> {
            //unlock the object even if the update completes exceptionally
            playerLock.unlock();
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
