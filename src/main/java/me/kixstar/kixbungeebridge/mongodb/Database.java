package me.kixstar.kixbungeebridge.mongodb;

import me.kixstar.kixbungeebridge.Config;
import me.kixstar.kixbungeebridge.Location;
import me.kixstar.kixbungeebridge.mongodb.entities.Home;
import me.kixstar.kixbungeebridge.mongodb.entities.KixPlayerData;
import me.kixstar.kixbungeebridge.mongodb.entities.WarpData;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.ogm.datastore.impl.DatastoreProviderType;

public class Database {

    private static SessionFactory sf;

    public static void bind() {
        Configuration cfg = new Configuration();

        cfg.addAnnotatedClass(Home.class);
        cfg.addAnnotatedClass(KixPlayerData.class);
        cfg.addAnnotatedClass(WarpData.class);

        //note: Location isn't inside entities because it's shared between RabbitMQ and MongoDB
        cfg.addAnnotatedClass(Location.class);

        cfg.getProperties().setProperty("hibernate.ogm.datastore.provider", DatastoreProviderType.MONGODB.name());
        cfg.getProperties().setProperty("hibernate.ogm.datastore.database", "KixServerData");
        cfg.getProperties().setProperty("hibernate.ogm.datastore.create_database", "true");

        cfg.getProperties().setProperty("hibernate.connection.password", Config.getMongoDBUsername());
        cfg.getProperties().setProperty("hibernate.connection.username", Config.getMongoDBPassword());
        cfg.getProperties().setProperty("hibernate.ogm.datastore.host" , Config.getMongoDBHost());

        sf = cfg.buildSessionFactory();

    }

    public static Session getCurrentSession() {
        return sf.getCurrentSession();
    }
}
