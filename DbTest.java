import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/** Self-contained DB test. No Spotify account or JUnit needed. Uses a throwaway .db file. */
public class DbTest {
    static int passed = 0, failed = 0;

    static void check(String name, boolean ok) {
        System.out.println((ok ? "PASS  " : "FAIL  ") + name);
        if (ok) passed++; else failed++;
    }

    public static void main(String[] args) throws Exception {
        File f = File.createTempFile("spotify_test", ".db");
        f.deleteOnExit();
        Database.setPath(f.getAbsolutePath());
        Database.init();
        Database.init(); // twice: must be safe to re-run

        // --- tables exist ---
        try (Connection c = Database.connect(); Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT name FROM sqlite_master WHERE type='table'")) {
            String all = "";
            while (rs.next()) all += rs.getString(1) + ",";
            check("4 tables created", all.contains("users") && all.contains("playlists")
                    && all.contains("matrix") && all.contains("recommended"));
        }

        // --- login ---
        int id = User_connection.register("cam", "pass123");
        check("register returns id", id > 0);
        check("duplicate username rejected", User_connection.register("cam", "other") == -1);
        check("login good password", User_connection.login("cam", "pass123") == id);
        check("login wrong password", User_connection.login("cam", "nope") == -1);
        check("login unknown user", User_connection.login("ghost", "pass123") == -1);
        try (Connection c = Database.connect(); Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT password FROM users WHERE user_ID=" + id)) {
            rs.next();
            check("password not stored as plaintext", !rs.getString(1).contains("pass123"));
        }

        // --- tokens ---
        check("no tokens before linking", User_connection.getTokens(id) == null);
        User_connection.saveTokens(id, "ACCESS1", "REFRESH1", 3600);
        User_connection.Tokens t = UserDao.getTokens(id);
        check("tokens saved", t != null && t.access().equals("ACCESS1") && t.refresh().equals("REFRESH1"));
        check("expiry is in the future", t.expiresAtEpoch() > System.currentTimeMillis() / 1000);

        User_connection.updateAccessToken(id, "ACCESS2", null, 3600);
        t = User_connection.getTokens(id);
        check("refresh updates access token", t.access().equals("ACCESS2"));
        check("null refresh keeps old refresh token", t.refresh().equals("REFRESH1"));
        User_connection.updateAccessToken(id, "ACCESS3", "REFRESH2", 3600);
        check("new refresh token replaces old", User_connection.getTokens(id).refresh().equals("REFRESH2"));

        // --- other tables + foreign keys ---
        try (Connection c = Database.connect()) {
            try (PreparedStatement ps = c.prepareStatement("INSERT INTO playlists VALUES('pl1',?,'Gym','2026-11-12T13:00:00')")) {
                ps.setInt(1, id); ps.executeUpdate();
            }
            c.createStatement().execute("INSERT INTO matrix(playlist_ID,danceability,tempo,song_key) VALUES('pl1',0.8,120.5,5)");
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO recommended(user_ID,song_name,genre,artist) VALUES(?,'Song','pop','Artist')")) {
                ps.setInt(1, id); ps.executeUpdate();
            }
            boolean fkBlocked = false;
            try {
                c.createStatement().execute("INSERT INTO playlists VALUES('bad',9999,'x','x')");
            } catch (Exception e) { fkBlocked = true; }
            check("foreign key blocks orphan playlist", fkBlocked);

            c.createStatement().execute("DELETE FROM users WHERE user_ID=" + id);
            check("deleting user cascades", count(c, "playlists") == 0
                    && count(c, "matrix") == 0 && count(c, "recommended") == 0);
        }

        System.out.println("\n" + passed + " passed, " + failed + " failed");
        System.exit(failed == 0 ? 0 : 1);
    }

    static int count(Connection c, String table) throws Exception {
        try (ResultSet rs = c.createStatement().executeQuery("SELECT COUNT(*) FROM " + table)) {
            rs.next(); return rs.getInt(1);
        }
    }
}
