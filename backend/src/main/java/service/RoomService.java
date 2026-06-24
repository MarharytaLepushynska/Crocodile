package service;

import enteties.Room;
import enteties.RoomUser;
import enums.PlayerRole;
import enums.RoomStatus;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RoomService {
    private final String dbUrl;

    public RoomService(String dbUrl) {
        this.dbUrl = dbUrl;
        createTables();
    }

    private void createTables() {
        try (Connection con = connect();
             Statement st = con.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS rooms (" +
                    "id SERIAL PRIMARY KEY, " +
                    "creator_id INTEGER NOT NULL, " +
                    "number_of_rounds INTEGER NOT NULL, " +
                    "duration_of_round INTEGER NOT NULL, " +
                    "invite_code TEXT NOT NULL UNIQUE, " +
                    "status TEXT NOT NULL DEFAULT 'WAITING')");

            st.executeUpdate("CREATE TABLE IF NOT EXISTS room_users (" +
                    "id SERIAL PRIMARY KEY, " +
                    "room_id INTEGER NOT NULL, " +
                    "user_id INTEGER NOT NULL, " +
                    "role TEXT NOT NULL DEFAULT 'GUESSER', " +
                    "score INTEGER NOT NULL DEFAULT 0, " +
                    "UNIQUE(room_id, user_id))");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized Room createRoom(long creatorId, int numberOfRounds, int durationOfRound) {
        if (numberOfRounds <= 0 || durationOfRound <= 0)
            throw new IllegalArgumentException("rounds and duration must be positive");

        String inviteCode = UUID.randomUUID().toString().substring(0, 8);

        String sql = "INSERT INTO rooms(creator_id, number_of_rounds, duration_of_round, invite_code, status) " +
                "VALUES (?, ?, ?, ?, 'WAITING') RETURNING id";
        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, creatorId);
            ps.setInt(2, numberOfRounds);
            ps.setInt(3, durationOfRound);
            ps.setString(4, inviteCode);
            ResultSet rs = ps.executeQuery();
            long id = rs.next() ? rs.getLong(1) : 0;

            Room room = new Room(id, creatorId, numberOfRounds, durationOfRound, inviteCode, RoomStatus.WAITING);
            join(room.getId(), creatorId); // адміністратор одразу стає учасником
            return room;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized Room findById(long id) {
        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT * FROM rooms WHERE id = ?")) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return readRoom(rs);

            return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized Room findByInviteCode(String inviteCode) {
        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT * FROM rooms WHERE invite_code = ?")) {
            ps.setString(1, inviteCode);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return readRoom(rs);

            return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized RoomUser join(long roomId, long userId) {
        Room room = findById(roomId);
        if (room == null)
            throw new RuntimeException("room not found: " + roomId);

        if (room.getStatus() != RoomStatus.WAITING)
            throw new RuntimeException("cannot join, game already started");

        RoomUser existing = findRoomUser(roomId, userId);
        if (existing != null)
            return existing;

        String sql = "INSERT INTO room_users(room_id, user_id, role, score) VALUES (?, ?, 'GUESSER', 0) RETURNING id";
        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, roomId);
            ps.setLong(2, userId);
            ResultSet rs = ps.executeQuery();
            long id = rs.next() ? rs.getLong(1) : 0;

            return new RoomUser(id, roomId, userId, PlayerRole.GUESSER, 0);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void leave(long roomId, long userId) {
        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(
                     "DELETE FROM room_users WHERE room_id = ? AND user_id = ?")) {
            ps.setLong(1, roomId);
            ps.setLong(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized RoomUser findRoomUser(long roomId, long userId) {
        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT * FROM room_users WHERE room_id = ? AND user_id = ?")) {
            ps.setLong(1, roomId);
            ps.setLong(2, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return readRoomUser(rs);

            return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized List<RoomUser> getRoomUsers(long roomId) {
        List<RoomUser> result = new ArrayList<>();
        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT * FROM room_users WHERE room_id = ?")) {
            ps.setLong(1, roomId);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                result.add(readRoomUser(rs));

            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void setStatus(long roomId, RoomStatus status) {
        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE rooms SET status = ? WHERE id = ?")) {
            ps.setString(1, status.name());
            ps.setLong(2, roomId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void addScore(long roomId, long userId, int points) {
        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE room_users SET score = score + ? WHERE room_id = ? AND user_id = ?")) {
            ps.setInt(1, points);
            ps.setLong(2, roomId);
            ps.setLong(3, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized boolean isCreator(long roomId, long userId) {
        Room room = findById(roomId);
        return room != null && room.getCreatorId() == userId;
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(
                dbUrl,
                System.getenv("DB_USER"),
                System.getenv("DB_PASSWORD")
        );
    }

    private Room readRoom(ResultSet rs) throws SQLException {
        return new Room(
                rs.getLong("id"),
                rs.getLong("creator_id"),
                rs.getInt("number_of_rounds"),
                rs.getInt("duration_of_round"),
                rs.getString("invite_code"),
                RoomStatus.valueOf(rs.getString("status"))
        );
    }

    private RoomUser readRoomUser(ResultSet rs) throws SQLException {
        return new RoomUser(
                rs.getLong("id"),
                rs.getLong("room_id"),
                rs.getLong("user_id"),
                PlayerRole.valueOf(rs.getString("role")),
                rs.getInt("score")
        );
    }
}
