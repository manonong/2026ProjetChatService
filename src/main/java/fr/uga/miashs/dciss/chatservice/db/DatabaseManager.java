package fr.uga.miashs.dciss.chatservice.db;

import java.sql.*;

public class DatabaseManager {

    private Connection cnx;

    public DatabaseManager() {
        try {
            cnx = DriverManager.getConnection("jdbc:derby:target/chatDB;create=true");
            initTables();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void initTables() throws SQLException {
        Statement stmt = cnx.createStatement();

        //TABLE USER ******************
        try {
            stmt.executeUpdate(
                "CREATE TABLE User (userId INT AUTO_INCREMENT PRIMARY KEY NOT NULL, nom VARCHAR(20) NOT NULL UNIQUE)"
            );
        } catch (SQLException ignored) {}
        
        //TABLE GROUP *****************
        try {
            stmt.executeUpdate(
                "CREATE TABLE Group (groupId INT AUTO_INCREMENT PRIMARY KEY NOT NULL, ownerId INT NOT NULL, nomGroup VARCHAR(20) NOT NULL, nbMembers INT NOT NULL)"
            );
        } catch (SQLException ignored) {}

        //TABLE GROUPMEMBERS **********
        try {
            stmt.executeUpdate(
                "CREATE TABLE GroupMembers (groupId INT, userId INT, PRIMARY KEY (groupId, userId))"
            );
        } catch (SQLException ignored) {}

        //TABLE CONTACT ****************
        // try {
        //     stmt.executeUpdate(
        //         "CREATE TABLE Contact (ownerId INT NOT NULL, contactId INT NOT NULL, nickname VARCHAR(20) NOT NULL, PRIMARY KEY (userId, contactId))"
        //     );
        // } catch (SQLException ignored) {}

        //TABLE HISTORIQUE *************
        //TODO Créer la table Historique
    }


    // ===== GROUP =====
    public void insertGroup(int groupId, int ownerId) {
        try {
            PreparedStatement ps = cnx.prepareStatement(
                "INSERT INTO Groups VALUES (?, ?)"
            );
            ps.setInt(1, groupId);
            ps.setInt(2, ownerId);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ===== MEMBER =====
    public void insertMember(int groupId, int userId) {
        try {
            PreparedStatement ps = cnx.prepareStatement(
                "INSERT INTO GroupMembers VALUES (?, ?)"
            );
            ps.setInt(1, groupId);
            ps.setInt(2, userId);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}