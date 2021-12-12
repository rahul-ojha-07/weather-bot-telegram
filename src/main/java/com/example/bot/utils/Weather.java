package com.example.bot.utils;

import com.vdurmont.emoji.EmojiParser;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.validation.constraints.NotNull;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class Weather {
    private final static String API_NOW = "http://api.openweathermap.org/data/2.5/weather?";
    private final static String API_5DAYS_3H = "http://api.openweathermap.org/data/2.5//forecast?";
    private final static String API_PLACE = "http://api.openweathermap.org/data/2.5/weather?q=";
    private final static String API_KEY = "&appid=" + BotProperties.getProperty("API_KEY");
    private static final Logger LOGGER = LogManager.getLogger(Weather.class);
    private String latitude;
    private String longitude;
    private String place;

    public Weather(double latitude, double longitude) {
        LOGGER.info("generate Weather...");
        this.latitude = "lat=" + latitude;
        this.longitude = "&lon=" + longitude;
    }

    public Weather(String place) {
        this.place = place;
    }

    /**
     * @param weatherInfo
     */
    public static boolean hasBigChanges(String[] weatherInfo) {
        boolean description = weatherInfo[1].contains("shower rain") || weatherInfo[1].contains("rain")
                || weatherInfo[1].contains("thunderstorm") || weatherInfo[1].contains("snow");

        boolean speed_wind = Double.parseDouble(weatherInfo[4]) > 5.;
        return description || speed_wind;
    }

    private String getURLNow() {
        return API_NOW + this.latitude + this.longitude + API_KEY;
    }

    private String getURL5Days_3H() {
        return API_5DAYS_3H + this.latitude + this.longitude + API_KEY;
    }

    private String getURLPlace(String place) {
        return API_PLACE + URLEncoder.encode(this.place, StandardCharsets.UTF_8) + API_KEY;
    }

    /**
     * @param url - url link with API content
     * @return String with API content
     */
    private String getAPIContentForJSON(String url) {
        StringBuilder responseStrBuilder = new StringBuilder();
        try {
            // create HttpClient
            HttpClient httpclient = HttpClientBuilder.create().build();

            // make GET request to the given URL
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));

            // receive response as inputStream
            InputStream inputStream = httpResponse.getEntity().getContent();
            BufferedReader streamReader = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8));

            String inputStr;
            while ((inputStr = streamReader.readLine()) != null) {
                responseStrBuilder.append(inputStr);
            }
        } catch (IOException e) {
            LOGGER.info("Trouble with URL " + url);
        }
        return responseStrBuilder.toString();
    }

    /**
     * @return array with information about weather type: result[0] = Shuzenji (city)
     * result[1] = clear sky result[2] = 281.52 (Kelvin) result[3] = 278.99
     * (feels_like) result[4] = 0.47 (seed wind)
     */
    public String[] getWeatherNow() {
        String[] weatherInfo = new String[5];
        JSONObject obj = new JSONObject(getAPIContentForJSON(getURLNow()));
        LOGGER.info("get URL ");

        if (!obj.get("cod").toString().equals("200")) {
            String ownerName = BotProperties.getProperty("OWNER_NAME");
            weatherInfo = new String[2];
            weatherInfo[0] = obj.get("cod").toString();
            weatherInfo[1] = "There is a problem in Our End We will resolve it soon!\nIn the meanwhile Please send a Message to " + ownerName;
            LOGGER.error("Error Code: " + weatherInfo[0] + " Error Message: " + weatherInfo[1]);
        } else {

            weatherInfo[0] = obj.get("name").toString();
            weatherInfo[1] = obj.getJSONArray("weather").getJSONObject(0).get("description").toString();
            weatherInfo[2] = obj.getJSONObject("main").get("temp").toString();
            weatherInfo[3] = obj.getJSONObject("main").get("feels_like").toString();
            weatherInfo[4] = obj.getJSONObject("wind").get("speed").toString();
        }
        return weatherInfo;
    }

    public String[] getWeather3H() {
        String[] weatherInfo = new String[6];
        JSONObject obj = new JSONObject(getAPIContentForJSON(getURL5Days_3H()));
        LOGGER.info("get URL ");

        weatherInfo[0] = obj.getJSONObject("city").get("name").toString();
        JSONArray list = obj.getJSONArray("list");

        double temp = Double.parseDouble(list.getJSONObject(0).getJSONObject("main").get("temp").toString());

        for (Object o : list) {
            JSONObject elem = (JSONObject) o;

            weatherInfo[1] = elem.getJSONArray("weather").getJSONObject(0).get("description").toString();
            weatherInfo[2] = elem.getJSONObject("main").get("temp").toString();
            weatherInfo[3] = elem.getJSONObject("main").get("feels_like").toString();
            weatherInfo[4] = elem.getJSONObject("wind").get("speed").toString();
            weatherInfo[5] = elem.get("dt_txt").toString();

            boolean tempChanges = Math.abs(temp - Double.parseDouble(weatherInfo[2])) > 7;
            temp = Double.parseDouble(weatherInfo[2]);
            if (hasBigChanges(weatherInfo) || tempChanges) {
                break;
            }
        }
        return weatherInfo;
    }

    public String[] getWeatherPlace() {
        String[] weatherInfo;
        LOGGER.info("get URL ");
        String URL = getURLPlace(place);
        JSONObject obj = new JSONObject(getAPIContentForJSON(URL));


        if (!obj.get("cod").toString().equals("200") && !obj.get("cod").toString().equals("404")) {
            String ownerName = BotProperties.getProperty("OWNER_NAME");
            weatherInfo = new String[2];
            weatherInfo[0] = obj.get("cod").toString();
            weatherInfo[1] = "There is a problem in Our End We will resolve it soon!\nIn the meanwhile Please send a Message to " + ownerName;
            LOGGER.error("Error Code: " + weatherInfo[0] + " Error Message: " + weatherInfo[1]);
        } else if (obj.get("cod").toString().equals("404")) {
            String ownerName = BotProperties.getProperty("OWNER_NAME");
            weatherInfo = new String[2];
            weatherInfo[0] = obj.get("cod").toString();
            weatherInfo[1] = "There is no Place with The name: " + place + "\nin our DataBase please try again with\ndifferent name!";
            LOGGER.debug("Error Code: " + weatherInfo[0] + " Error Message: " + weatherInfo[1]);
        } else {
            weatherInfo = new String[5];
            weatherInfo[0] = obj.get("name").toString();
            weatherInfo[1] = obj.getJSONArray("weather").getJSONObject(0).get("description").toString();
            weatherInfo[2] = obj.getJSONObject("main").get("temp").toString();
            weatherInfo[3] = obj.getJSONObject("main").get("feels_like").toString();
            weatherInfo[4] = obj.getJSONObject("wind").get("speed").toString();
            DBUser db = new DBUser();
            double lat = Double.parseDouble(obj.getJSONObject("coord").get("lat").toString());
            double lon = Double.parseDouble(obj.getJSONObject("coord").get("lon").toString());
            if (!db.isLocationPresent(lat, lon)) {
                db.setLocationWeather(lat, lon, place);
            }
        }
        return weatherInfo;
    }


    public String errorWrap(@NotNull String[] message) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(message[1]).append(EmojiParser.parseToUnicode(":disappointed:"));
        return stringBuilder.toString();
    }

    /**
     * translate weather for telegram bot
     *
     * @param weather - massive with information
     * @return - String with message
     */
    public String toWrap(@NotNull String[] weather) {
        StringBuilder stringBuilder = new StringBuilder();
        double temp = Double.parseDouble(weather[2]) - 273.15;
        double tempFeelsLike = Double.parseDouble(weather[3]) - 273.15;

        stringBuilder.append(EmojiParser.parseToUnicode(":house: ")).append("Location: ").append(weather[0])
                .append('\n').append(EmojiParser.parseToUnicode(":white_sun_small_cloud: "))
                .append("Weather condition: ").append(weather[1]).append('\n')
                .append(EmojiParser.parseToUnicode(":thermometer: ")).append("Temperature: ")
                .append(String.format(Locale.US, "%.2f", temp)).append(" C\n")
                .append(EmojiParser.parseToUnicode(":thermometer: ")).append("Feels like: ")
                .append(String.format(Locale.US, "%.2f", tempFeelsLike)).append(" C\n")
                .append(EmojiParser.parseToUnicode(":cloud_tornado: ")).append("Speed wind: ").append(weather[4])
                .append(" meter/sec\n");

        return stringBuilder.toString();
    }
}
