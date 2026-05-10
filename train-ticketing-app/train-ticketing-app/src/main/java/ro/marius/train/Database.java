package ro.marius.train;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class Database {

    private static final Properties props = new Properties();

    static {

        try (InputStream input = Database.class.getClassLoader().getResourceAsStream("config.properties")) {

            if(input== null) {
                throw new RuntimeException("config.properties file was not found.");
            }
            props.load(input);

        }catch(Exception e) {
            throw new RuntimeException("Could not load config.properties: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws Exception {

        var url=props.getProperty("db.url");
        var user=props.getProperty("db.user");
        var password=props.getProperty("db.password");
        return DriverManager.getConnection(url, user, password);
    }


    public static String getProperty(String key) {
        return props.getProperty(key);
    }
}