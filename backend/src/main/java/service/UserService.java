package service;

import enteties.User;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UserService {
    private final String dbUrl;

    public UserService(String dbUrl) {
        this.dbUrl = dbUrl;
        createTables();
    }

    private void createTables() {
        try (Connection con = connect();
             Statement st = con.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS users (" +
                    "id SERIAL PRIMARY KEY, " +
                    "username TEXT NOT NULL UNIQUE, " +
                    "password TEXT NOT NULL, " +
                    "avatar TEXT)");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized User create(String username, String password, String avatar) {
        checkUser(username, password);

        if (findByUsername(username) != null)
            throw new RuntimeException("username already taken: " + username);

        String sql = "INSERT INTO users(username, password, avatar) VALUES (?, ?, ?) RETURNING id";
        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, avatar);
            ResultSet rs = ps.executeQuery();
            long id = rs.next() ? rs.getLong(1) : 0;

            return new User(id, username, password, avatar);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized User findById(long id) {
        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT * FROM users WHERE id = ?")) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return readUser(rs);

            return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized User findByUsername(String username) {
        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT * FROM users WHERE username = ?")) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return readUser(rs);

            return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized User authenticate(String username, String password) {
        User user = findByUsername(username);
        if (user == null || !user.getPassword().equals(password))
            return null;

        return user;
    }

    public synchronized User updateAvatar(long id, String avatar) {
        String sql = "UPDATE users SET avatar = ? WHERE id = ?";
        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, avatar);
            ps.setLong(2, id);

            if (ps.executeUpdate() == 0)
                return null;

            return findById(id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(dbUrl);
    }

    private User readUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getLong("id"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("avatar")
        );
    }

    private void checkUser(String username, String password) {
        if (isEmpty(username) || isEmpty(password))
            throw new IllegalArgumentException("username/password must not be empty");
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}