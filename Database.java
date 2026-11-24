import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
    private static String url = "jdbc:sqlite:spotify_analyzer.db";

    public static void setPath(String path) {
        url = "jdbc:sqlite:" + path;
    }

    public static Connection connect() throws SQLException {
        Connection c = DriverManager.getConnection(url);
        try (Statement s = c.createStatement()){
            s.execute("PRAGMA foreign_keys = ON");
        }
        return c;
    }

    public static void init() throws SQLException {
        try (Connection c = connect();
        Statement s = c.createStatement()) {
            s.execute( """
            CREATE TABLE IF NOT EXISTS users (
            user_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            username TEXT NOT NULL UNIQUE,
            password TEXT NOT NULL,
            a_token TEXT,
            r_token TEXT,
            token_expires INTEGER
            )""");

            s.execute("""
            CREATE TABLE IF NOT EXISTS playlists(
            playlist_ID TEXT PRIMARY KEY,
            user_ID INTEGER NOT NULL REFERENCES users(user_ID) ON DELETE CASCADE,
            name TEXT NOT NULL,
            time TEXT
            )""");

            s.execute("""
            CREATE TABLE IF NOT EXISTS matrix (
            matrix_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            playlist_ID TEXT NOT NULL REFERENCES playlists(playlist_ID) ON DELETE CASCADE,
            danceability REAL,
            tempo REAL,
            song_key INTEGER
            )""");

            s.execute("""
            CREATE TABLE IF NOT EXISTS recommended (
            recommended_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            user_ID INTEGER NOT NULL REFERENCES users(user_ID) ON DELETE CASCADE,
            song_name TEXT NOT NULL,
            genre TEXT,
            artist TEXT,
            status TEXT DEFAULT 'new'
            )""");
        }
    }
}
