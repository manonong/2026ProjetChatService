package fr.uga.miashs.dciss.chatservice.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private static final String URL = "jdbc:sqlite:chat.db";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return DriverManager.getConnection(URL);
    }

    // 初始化数据库
    public static void initDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // ===== messages 表 =====
            String messagesTable =
                    "CREATE TABLE IF NOT EXISTS messages (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "sender_id INTEGER, " +
                            "receiver_id INTEGER, " +
                            "content TEXT, " +
                            "type TEXT, " +
                            "created_at DATETIME DEFAULT CURRENT_TIMESTAMP" +
                            ");";

            // ===== groups 表 =====
            String groupsTable =
                    "CREATE TABLE IF NOT EXISTS groups (" +
                            "id INTEGER PRIMARY KEY, " +
                            "owner_id INTEGER" +
                            ");";

            // ===== group_members 表 =====
            String membersTable =
                    "CREATE TABLE IF NOT EXISTS group_members (" +
                            "group_id INTEGER, " +
                            "user_id INTEGER" +
                            ");";

            stmt.execute(messagesTable);
            stmt.execute(groupsTable);
            stmt.execute(membersTable);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ===== 保存消息 =====
    public static void saveMessage(int sender, int receiver, String content, String type) {
        String sql = "INSERT INTO messages (sender_id, receiver_id, content, type) VALUES (?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sender);
            ps.setInt(2, receiver);
            ps.setString(3, content);
            ps.setString(4, type);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ===== 查看历史 =====
    public static void showAllMessages() {
        String sql = "SELECT sender_id, receiver_id, content, created_at FROM messages";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("===== CHAT HISTORY =====");

            while (rs.next()) {
                System.out.println(
                        "[" + rs.getString("created_at") + "] "
                                + rs.getInt("sender_id") + " -> "
                                + rs.getInt("receiver_id") + " : "
                                + rs.getString("content")
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ===== 创建群 =====
    public void insertGroup(int groupId, int ownerId) {
        String sql = "INSERT INTO groups (id, owner_id) VALUES (?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, groupId);
            ps.setInt(2, ownerId);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ===== 添加成员 =====
    public void insertMember(int groupId, int userId) {
        String sql = "INSERT INTO group_members (group_id, user_id) VALUES (?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, groupId);
            ps.setInt(2, userId);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}