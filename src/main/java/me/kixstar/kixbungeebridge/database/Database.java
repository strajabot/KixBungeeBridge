package me.kixstar.kixbungeebridge.database;

import me.kixstar.kixbungeebridge.Config;
import me.kixstar.kixbungeebridge.database.entities.Location;
import me.kixstar.kixbungeebridge.database.entities.HomeData;
import me.kixstar.kixbungeebridge.database.entities.KixPlayerData;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class Database {

    private static SessionFactory sf;

    public static void bind() {
        Configuration cfg = new Configuration();

        cfg.addAnnotatedClass(HomeData.class);
        cfg.addAnnotatedClass(KixPlayerData.class);

        //note: Location isn't inside entities because it's shared between RabbitMQ and MySQL
        cfg.addAnnotatedClass(Location.class);

        if(!Config.isProd()) {
            cfg.setProperty("hibernate.show_sql", "true");
            cfg.setProperty("hibernate.format_sql", "true");
            cfg.setProperty("hibernate.use_sql_comments", "true");
        }

        cfg.setProperty("hibernate.connection.driver_class", "com.mysql.jdbc.Driver");
        cfg.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL5Dialect");
        //"hibernate.hbm2ddl.auto" set to "create" enables auto creation of tables
        cfg.setProperty("hibernate.hbm2ddl.auto" , "update");
        cfg.setProperty("javax.persistence.create-database-schemas", "true");

        cfg.setProperty("hibernate.connection.username", Config.getDBUsername());
        cfg.setProperty("hibernate.connection.password", Config.getDBPassword());
        cfg.setProperty("hibernate.connection.url" , Config.getDBHost());

        sf = cfg.buildSessionFactory();

    }

    public static Session getNewSession() {
        return sf.openSession();
    }
}
