package pt.tecnico.Common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author postgresqltutorial.com
 */
public class DatabaseImpl{

    private final String url = "jdbc:postgresql://localhost:5432/Ransom";
    private final String user = "postgres";
    private final String password = "1234";


    /**
     * Connect to the PostgreSQL database-
     *
     * @return a Connection object
     */
    public Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to the PostgreSQL server successfully.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return conn;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        DatabaseImpl app = new DatabaseImpl();
        app.connect();
    }
}