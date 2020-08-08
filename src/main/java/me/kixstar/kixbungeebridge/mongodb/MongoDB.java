package me.kixstar.kixbungeebridge.mongodb;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import me.kixstar.kixbungeebridge.Config;
import org.bson.Document;

public class MongoDB {

    private static MongoClient client;
    private static MongoDatabase database;

    private static MongoCollection<Document> playerCollection;

    public static void bind() {
        client = MongoClients.create(Config.getMongoDB());
        database = client.getDatabase("ServerData");

        playerCollection = database.getCollection("PlayerData");

    }

    public static void unbind() {
        client.close();
    }

    public static MongoClient getClient() {
        return client;
    }

    public static MongoDatabase getDatabase() {
        return database;
    }

    public static MongoCollection<Document> getPlayerCollection() {
        return playerCollection;
    }

}
