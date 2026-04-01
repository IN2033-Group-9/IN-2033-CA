import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

public class SA_LOGIN_API {

    private Connection conn;

    public SA_LOGIN_API(Connection conn) {
        this.conn = conn;
    }

    /**
     * Hash passwords using SHA-256
     */
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(password.getBytes());

            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available");
        }
    }

    /**
     * LOGIN
     * Check username + password against database
     */
    public boolean login(String username, String password) {

        try {
            String sql = "SELECT password_hash FROM ca_users WHERE username = ?";
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, username);

            ResultSet rs = ps.executeQuery();

            // If user exists
            if (rs.next()) {
                String storedHash = rs.getString("password_hash");

                // Compare stored hash with input password hash
                return storedHash.equals(hashPassword(password));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * CREATE ACCOUNT
     */
    public boolean createAccount(String username, String password) {

        try {
            // Check if user already exists
            String checkSql = "SELECT user_id FROM ca_users WHERE username = ?";
            PreparedStatement checkPs = conn.prepareStatement(checkSql);
            checkPs.setString(1, username);

            ResultSet rs = checkPs.executeQuery();

            if (rs.next()) {
                // Username already exists
                return false;
            }

            // Insert new user
            String insertSql = "INSERT INTO ca_users (user_id, username, password_hash, role_id) VALUES (?, ?, ?, ?)";

            PreparedStatement ps = conn.prepareStatement(insertSql);

            // makles a random id as user_id is a primary key and doesnt use auto increment
            int userId = (int)(Math.random() * 100000);

            ps.setInt(1, userId);
            ps.setString(2, username);
            ps.setString(3, hashPassword(password));

            // Default role
            ps.setInt(4, 1);

            ps.executeUpdate();

            return true;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * REMOVE ACCOUNT
     */
    public boolean removeAccount(String username) {

        try {
            String sql = "DELETE FROM ca_users WHERE username = ?";
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, username);

            int rowsAffected = ps.executeUpdate();

            // If at least 1 row deleted → success
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
}