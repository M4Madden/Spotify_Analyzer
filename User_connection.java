import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** All reads/writes for the users table: login data + Spotify tokens. */
public class User_connection {

    /** Holds what Spotify auth needs back out of the DB. */
    public record Tokens(String access, String refresh, long expiresAtEpoch) {}

    // ---------- login ----------

    /** @return new user_ID, or -1 if the username is taken. */
    public static int register(String username, String password) throws SQLException {
        String sql = "INSERT INTO users(username, password) VALUES(?, ?)";
        try (Connection c = Database.connect();
             PreparedStatement ps = c.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) {
                return k.next() ? k.getInt(1) : -1;
            }
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("UNIQUE")) return -1;
            throw e;
        }
    }

    /** @return user_ID on success, -1 on bad username/password. */
    public static int login(String username, String password) throws SQLException {
        try (Connection c = Database.connect();
             PreparedStatement ps = c.prepareStatement(
                 "SELECT user_ID FROM users WHERE username = ? AND password = ?")) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("user_ID") : -1;
            }
        }
    }

    // ---------- Spotify tokens ----------

    /** Store/replace both tokens after the first Spotify link. */
    public static void saveTokens(int userId, String access, String refresh, int expiresInSeconds) throws SQLException {
        long expiresAt = System.currentTimeMillis() / 1000 + expiresInSeconds;
        try (Connection c = Database.connect();
             PreparedStatement ps = c.prepareStatement(
                 "UPDATE users SET a_token = ?, r_token = ?, token_expires = ? WHERE user_ID = ?")) {
            ps.setString(1, access);
            ps.setString(2, refresh);
            ps.setLong(3, expiresAt);
            ps.setInt(4, userId);
            ps.executeUpdate();
        }
    }

    /** Called after a refresh. Spotify only sometimes returns a new refresh token; null keeps the old one. */
    public static void updateAccessToken(int userId, String access, String newRefreshOrNull, int expiresInSeconds) throws SQLException {
        long expiresAt = System.currentTimeMillis() / 1000 + expiresInSeconds;
        try (Connection c = Database.connect();
             PreparedStatement ps = c.prepareStatement(
                 "UPDATE users SET a_token = ?, r_token = COALESCE(?, r_token), token_expires = ? WHERE user_ID = ?")) {
            ps.setString(1, access);
            ps.setString(2, newRefreshOrNull);
            ps.setLong(3, expiresAt);
            ps.setInt(4, userId);
            ps.executeUpdate();
        }
    }

    /** @return tokens, or null if the user has not linked Spotify yet. */
    public static Tokens getTokens(int userId) throws SQLException {
        try (Connection c = Database.connect();
             PreparedStatement ps = c.prepareStatement("SELECT a_token, r_token, token_expires FROM users WHERE user_ID = ?")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next() || rs.getString("r_token") == null) return null;
                return new Tokens(rs.getString("a_token"), rs.getString("r_token"), rs.getLong("token_expires"));
            }
        }
    }
}
