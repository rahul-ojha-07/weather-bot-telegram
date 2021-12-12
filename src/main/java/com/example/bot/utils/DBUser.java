package com.example.bot.utils;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;


public class DBUser {
    private static final Logger LOGGER = LogManager.getLogger(DBUser.class);
    private final String url;
    private final String user;
    private final String passwd;

    public DBUser()  {
        LOGGER.info("Creating DBUser...");
        url = BotProperties.getProperty("JDBC_DATABASE_URL");
        user = BotProperties.getProperty("DATABASE_USER");
        passwd = BotProperties.getProperty("DATABASE_PASS");
    }

    /**
     * Check user in database table users
     *
     * @param userId - user of bot
     * @return true, if user in base users
     */
    public boolean isUserId(Long userId) {
        String query = "SELECT lat, lon FROM users WHERE user_id=?";
        LOGGER.info("Connecting to DB...");
        try (Connection con = DriverManager.getConnection(url, user, passwd);
             PreparedStatement pst = con.prepareStatement(query)
        ) {
            LOGGER.info("Check id in base users...");
            pst.setLong(1, userId);
            ResultSet rs = pst.executeQuery();

            LOGGER.info("Closing DB Connection...");
            return rs.next();
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Add user in base users
     *
     * @param userId - user of bot
     */
    public void setUserID(Long userId) {
        String query = "INSERT INTO users(user_id) SELECT ? WHERE NOT EXISTS (SELECT user_id FROM users WHERE user_id=?)";
        LOGGER.info("Connecting to DB...");
        try (Connection con = DriverManager.getConnection(url, user, passwd)
        ) {
            LOGGER.info("Add in users...");
            PreparedStatement pst = con.prepareStatement(query);
            pst.setLong(1, userId);
            pst.setLong(2, userId);
            pst.executeUpdate();
            createNewMessage(userId);
            LOGGER.info("Closing DB Connection...");
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Add user name in base users
     *
     * @param userId     - user of bot
     * @param tgUserName - user name of userId
     */
    public void updateUserName(Long userId, String tgUserName) {
        String query = "UPDATE users SET user_name=? WHERE user_id=?";
        LOGGER.info("Connecting to DB...");
        try (Connection con = DriverManager.getConnection(url, user, passwd);
             PreparedStatement pst = con.prepareStatement(query)) {
            LOGGER.info("update user name in base users...");
            pst.setString(1, tgUserName);
            pst.setLong(2, userId);
            pst.executeUpdate();
            LOGGER.info("Closing DB Connection...");
        } catch (SQLException e) {

            LOGGER.error(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * add lon in base
     *
     * @param lon    - longitude of user
     * @param userId - user id
     */
    public void updateLonUser(double lon, Long userId) {
        String query = "UPDATE users SET lon=? WHERE user_id=?";
        LOGGER.info("Connecting to DB...");
        try (Connection con = DriverManager.getConnection(url, user, passwd);
             PreparedStatement pst = con.prepareStatement(query)) {
            pst.setDouble(1, lon);
            pst.setLong(2, userId);
            LOGGER.info("update lon in base users...");
            pst.executeUpdate();
            LOGGER.info("Closing DB Connection...");
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * add lat in base
     *
     * @param lat    - latitude of user
     * @param userId - user id
     */
    public void updateLatUser(double lat, Long userId) {
        String query = "UPDATE users SET lat=? WHERE user_id=?";
        LOGGER.info("Connecting to DB...");
        try (Connection con = DriverManager.getConnection(url, user, passwd);
             PreparedStatement pst = con.prepareStatement(query)) {
            pst.setDouble(1, lat);
            pst.setLong(2, userId);
            LOGGER.info("update lat in base users...");
            pst.executeUpdate();
        } catch (SQLException e) {

            LOGGER.error(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Update lat, Long, userName in DB for userId
     *
     * @param lat
     * @param lon
     * @param userName
     * @param userId
     */
    public void updateUserData(double lat, double lon, String userName, Long userId) {
        String query = "UPDATE users SET lat=?,lon=?,user_name=? WHERE user_id=?";
        LOGGER.info("Connecting to DB...");
        try (Connection con = DriverManager.getConnection(url, user, passwd);
             PreparedStatement pst = con.prepareStatement(query)) {
            pst.setDouble(1, lat);
            pst.setDouble(2, lon);
            pst.setString(3, userName);
            pst.setLong(4, userId);
            LOGGER.info("updating User Data in users...");
            pst.executeUpdate();
            LOGGER.info("Closing DB Connection...");
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * @param userId - id user in
     * @return latitude this user
     */
    public double getLatUser(Long userId) {
        String query = "SELECT lat FROM users WHERE user_id=?";
        LOGGER.info("Connecting to DB...");
        try (Connection con = DriverManager.getConnection(url, user, passwd);
             PreparedStatement pst = con.prepareStatement(query)
        ) {
            pst.setLong(1, userId);
            ResultSet rs = pst.executeQuery();
            rs.next();
            LOGGER.info("Closing DB Connection...");
            return rs.getDouble(1);
        } catch (SQLException e) {

            LOGGER.error(e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * @param userId - id user in
     * @return longitude this user
     */
    public double getLonUser(Long userId) {
        String query = "SELECT lon FROM users WHERE user_id=?";
        LOGGER.info("Connecting to DB...");
        try (Connection con = DriverManager.getConnection(url, user, passwd);
             PreparedStatement pst = con.prepareStatement(query)
        ) {
            pst.setLong(1, userId);
            ResultSet rs = pst.executeQuery();
            rs.next();
            LOGGER.info("Closing DB Connection...");
            return rs.getDouble(1);
        } catch (SQLException e) {

            LOGGER.error(e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * delete the user from the DB
     *
     * @param user_id
     */
    public void deleteUser(Long user_id) {
        String query = "DELETE FROM users WHERE user_id=?";
        LOGGER.info("Connecting to DB...");
        try (Connection con = DriverManager.getConnection(url, user, passwd);
             PreparedStatement pst = con.prepareStatement(query)) {
            LOGGER.info("Delete user from bd ...");
            pst.setLong(1, user_id);
            pst.executeUpdate();
            LOGGER.info("Closing DB Connection...");
        } catch (SQLException e) {

            LOGGER.error(e.getMessage());
            e.printStackTrace();
        }
    }

    /************************************************** Message Data *************************************************/

    public boolean isUserIdMessage(Long userId) {
        LOGGER.info("Connecting to DB...");

        String query = "SELECT last_message FROM message WHERE user_id=?";
        try (Connection con = DriverManager.getConnection(url, user, passwd);
             PreparedStatement pst = con.prepareStatement(query)
        ) {
            LOGGER.info("Checking last_message in message...");
            pst.setLong(1, userId);
            ResultSet rs = pst.executeQuery();
            LOGGER.info("Closing DB Connection...");
            return rs.next();
        } catch (SQLException e) {

            LOGGER.error(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public void createNewMessage(Long userId) {
        String query = "INSERT INTO message(user_id) SELECT ? WHERE NOT EXISTS (SELECT user_id FROM message WHERE user_id=?)";

        LOGGER.info("Connecting to DB...");
        try (Connection con = DriverManager.getConnection(url, user, passwd);
             PreparedStatement pst = con.prepareStatement(query)) {

            LOGGER.info("Add id for message...");
            pst.setLong(1, userId);
            pst.setLong(2, userId);
            pst.executeUpdate();
            LOGGER.info("Closing DB Connection...");
        } catch (SQLException e) {

            LOGGER.error(e.getMessage());
            e.printStackTrace();
        }

    }

    public void updateCurrentMessage(Long userId, String message) {
        String query = "Update message SET last_message=? WHERE user_id=?";
        LOGGER.info("Connecting to DB...");
        try (Connection con = DriverManager.getConnection(url, user, passwd);
             PreparedStatement pst = con.prepareStatement(query)) {
            LOGGER.info("Update message for user " + userId);
            pst.setString(1, message);
            pst.setLong(2, userId);
            pst.executeUpdate();
            LOGGER.info("Closing DB Connection...");
        } catch (SQLException e) {

            LOGGER.error(e.getMessage());
            e.printStackTrace();
        }
    }

    public String getLastMessage(Long userId) {
        String query = "SELECT last_message FROM message WHERE user_id=?";
        LOGGER.info("Connecting to DB...");
        try (Connection con = DriverManager.getConnection(url, user, passwd);
             PreparedStatement pst = con.prepareStatement(query)) {
            LOGGER.info("Get last message for user " + userId);
            pst.setLong(1, userId);
            ResultSet rSet = pst.executeQuery();
            rSet.next();
            LOGGER.info("Closing DB Connection...");
            return rSet.getString(1);
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /************************************************ Weather Data ****************************************************/
    public boolean isLocationPresent(double lat, double lon) {
        LOGGER.info("Connecting to DB...");

        String query = "SELECT place_name FROM weather WHERE lat=? AND lon=?";
        try (Connection con = DriverManager.getConnection(url, user, passwd);
             PreparedStatement pst = con.prepareStatement(query)
        ) {
            LOGGER.info("Checking location in weather...");
            pst.setDouble(1, lat);
            pst.setDouble(2, lon);
            ResultSet rs = pst.executeQuery();
            LOGGER.info("Closing DB Connection...");
            return rs.next();
        } catch (SQLException e) {

            LOGGER.error(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


    public void setLocationWeather(double lat, double lon, String placeName) {
        String query = "INSERT INTO weather(lat,lon,place_name) VALUES(?,?,?)";
        LOGGER.info("Connecting to DB...");
        try (Connection con = DriverManager.getConnection(url, user, passwd)
        ) {
            LOGGER.info("Add in weather...");
            PreparedStatement pst = con.prepareStatement(query);
            pst.setDouble(1, lat);
            pst.setDouble(2, lon);
            pst.setString(3, placeName);
            pst.executeUpdate();
            LOGGER.info("Closing DB Connection...");
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
        }
    }


    public Map<String, Double> getLocationWeather(String place) {
        String query = "SELECT lat,lon FROM weather WHERE place_name=?";
        LOGGER.info("Connecting to DB...");
        try (Connection con = DriverManager.getConnection(url, user, passwd);
             PreparedStatement pst = con.prepareStatement(query)) {
            LOGGER.info("lat Long for place: " + place);
            pst.setString(1, place);
            ResultSet rSet = pst.executeQuery();
            rSet.next();
            Map<String, Double> map = new HashMap<>();
            map.put("lat", rSet.getDouble("lat"));
            map.put("lon", rSet.getDouble("lon"));
            LOGGER.info("Closing DB Connection...");
            return map;
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public void setAsCurrentLocationPlace(Long userId, String place) {

        Map<String, Double> map = getLocationWeather(place);
        updateLatUser(map.get("lat"), userId);
        updateLonUser(map.get("lon"), userId);
        LOGGER.info("Updated as current Location!");

    }


}
