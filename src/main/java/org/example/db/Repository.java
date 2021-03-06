package org.example.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * The <code>Repository</code> class does operations on the database.
 * @autor Eduard Ivanov
 * @since 2020-10-08
 */
public class Repository {

    /**
     * The Logger Log4j gets logs and puts them into /logs/logfile/log.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Repository.class);

    /**
     * An instance for getting connection to database.
     */
    private Connection connection;

    {
        createDatabase();
        createTable();
    }

    /**
     * Printing data from database.
     * @exception  SQLException if trouble with database connection.
     * @exception  ClassNotFoundException if MySQL driver has not found.
     */
    public void printWeatherInfo() {
        String sql = "SELECT * FROM weather_table ORDER BY id DESC LIMIT 1";
        try {
            connection = ConnectorDB.getConnection();
            LOG.info("Got connection to DB for getting data.");
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            if (resultSet.next())
                System.out.println("Welcome to the weather forecast!" +
                        "\nToday is: " + resultSet.getString(2) +
                        "\nTime now: " + resultSet.getString(3) +
                        "\nTemperature: " + resultSet.getString(4));
            LOG.info("The weather was printed from DB.");
            LOG.info("Printed weather info: {} {} {}", resultSet.getString(2), resultSet.getString(3), resultSet.getString(4));
            connection.close();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            LOG.error("Connection failed, can't print data from DB.", e);
        }
    }

    /**
     * Insert data into MySQL database.
     * @exception SQLException if trouble with database connection.
     * @exception ClassNotFoundException if MySQL driver has not found.
     */
    public void insertWeatherInfo(String date, String time, String temperature) {
        String sql = "INSERT INTO weather_table (date, time, temperature) VALUES (?, ?, ?)";
        try {
            connection = ConnectorDB.getConnection();
            LOG.info("Got connection to DB for insert data.");
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, date);
            preparedStatement.setString(2, time);
            preparedStatement.setString(3, temperature);
            preparedStatement.executeUpdate();
            LOG.info("The weather info was added to the DB.");
            LOG.info("Transferred data to DB: {} {} {}", date, time, temperature);
            connection.close();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            LOG.error("Connection failed, can't insert data into DB.", e);
        }
    }

    /**
     * Creates MySQL database on server.
     * @exception SQLException if trouble with server connection.
     * @exception ClassNotFoundException if MySQL driver has not found.
     */
    public void createDatabase() {
        String sql = "CREATE DATABASE IF NOT EXISTS weather_db";
        try {
            connection = ConnectorDB.getConnectionForCreatingDB();
            Statement statement = connection.createStatement();
            statement.executeUpdate(sql);
            connection.close();
            LOG.info("The MySQL database was created.");
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            LOG.error("Connection failed, database wasn't created.", e);
        }
    }

    /**
     * Creates a table in the MySQL database.
     * @exception SQLException if trouble with database connection.
     * @exception ClassNotFoundException if MySQL driver has not found.
     */
    public void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS weather_table"
                + " (id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,"
                + " date VARCHAR(20) NOT NULL,"
                + " time VARCHAR(10) NOT NULL,"
                + " temperature VARCHAR(20) NOT NULL)";
        try {
            connection = ConnectorDB.getConnection();
            Statement statement = connection.createStatement();
            statement.executeUpdate(sql);
            connection.close();
            initializeTable();
            LOG.info("The table was created in database.");
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            LOG.error("Connection failed, table wasn't created.", e);
        }
    }

    /**
     * Initialises the table in the MySQL database.
     * @exception SQLException if trouble with database connection.
     * @exception ClassNotFoundException if MySQL driver has not found.
     */
    public void initializeTable() {
        String sql = "INSERT INTO weather_table (date, time, temperature)"
                + " SELECT ?, ?, ?"
                + " WHERE NOT EXISTS (SELECT * FROM weather_table)";
        try {
            connection = ConnectorDB.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, "today");
            preparedStatement.setString(2, "now");
            preparedStatement.setString(3, "awesome weather");
            preparedStatement.executeUpdate();
            connection.close();
            LOG.info("The table was created in database.");
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            LOG.error("Connection failed, table wasn't initialized.", e);
        }
    }
}