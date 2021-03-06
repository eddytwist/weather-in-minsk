package org.example.util;

import org.example.db.Repository;
import org.example.exceptions.NoConnectionException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * The <code>Parser</code> class implements <code>WeatherInfo</code> interface and used for parsing the HTML page.
 * After parsing it puts data to MySQL database.
 * If connection with internet is lost, Parser gets last data updates from DB.
 * @autor Eduard Ivanov
 * @since 2020-10-08
 */
public class Parser implements WeatherInfo{

    /**
     * The Logger Log4j gets logs and puts them into /logs/logfile/log.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Parser.class);

    /**
     * The URL field of HTML page for parsing.
     */
    private final static String GISMETEO_URL = "https://www.gismeteo.by/weather-minsk-4248/";

    /**
     * The HTML page that we got parsed.
     */
    private Document page;

    /**
     * The MySQL database instance.
     */
    private final Repository repository = new Repository();

    /**
     * @return a parsed via Jsoup page for further selection.
     * @throws NoConnectionException if it's not possible to connect to the page.
     * @exception MalformedURLException if HTML page could not be read.
     */
    private Document getPage() throws NoConnectionException {
        try {
            page = Jsoup.parse(new URL(GISMETEO_URL), 10000);
            LOG.info("The page was found.");
        } catch (MalformedURLException e) {
            LOG.error(e.getMessage(), e);
            e.printStackTrace();
        } catch (IOException e) {
            LOG.error("Troubles with connection, data will be taken from the database.", e);
            throw new NoConnectionException("No connection with the internet.");
        }
        return page;
    }

    /**
     * Does finishing selections and concatenation, and makes the String ready for printing.
     * @return a String from parsed page, or prints data from DB if internet connection is lost.
     */
    @Override
    public String getWeatherInfo() {
        String todayWeather;
        String todayDate;
        String timeNow;
        try {
            page = getPage();
            LOG.info("The page was parsed.");
        } catch (NoConnectionException e) {
            repository.printWeatherInfo();
            LOG.error(e.getMessage(), e);
            return "That was the last weather forecast update from database.";
        }
        Element tableWth = page.select("div[class=tabs _center]").first();
        todayWeather = tableWth.select("div[class=tab-weather]").select("span[class=unit unit_temperature_c]").first().text();
        todayDate = tableWth.select("div[class=tab  tooltip]").select("div[class=tab-content]").select("div[class=date]").text();
        timeNow = tableWth.select("div[id=time]").first().text();
        repository.insertWeatherInfo(todayDate, timeNow, todayWeather);
        LOG.info("Printed data: {} {} {}", todayDate, timeNow, todayWeather);
        return ("Welcome to the weather forecast!" +
                "\nToday is: " + todayDate +
                "\nTime now: " + timeNow +
                "\nTemperature: " + todayWeather +
                "\nHave a nice day!");
    }
}
