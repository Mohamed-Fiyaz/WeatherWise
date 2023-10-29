package com.example.weatherwise;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.sql.Timestamp;

@SpringBootApplication
@Controller
public class WeatherWiseApplication {

    private final String openWeatherApiKey = "XYZ";

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/weather")
    public String getWeather(@RequestParam("location") String location, Model model) {
        // Make the API request to OpenWeatherMap and parse the JSON response
        String apiUrl = "https://api.openweathermap.org/data/2.5/weather?q=" + location + "&appid=" + openWeatherApiKey;
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(apiUrl, String.class);

        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            Weather weather = parseWeatherResponse(responseEntity.getBody());
            model.addAttribute("weather", weather);

            // Insert the location into the database
            double temperature = weather.getTemperature();
            insertLocationIntoDatabase(location, temperature);

            return "weather";
        } else {
            return "error";
        }
    }

    @GetMapping("/search-history")
    public String searchHistory(Model model) {
        List<SearchEntry> searchHistory = retrieveSearchHistoryFromDatabase();
        model.addAttribute("searchHistory", searchHistory);
        return "search-history";
    }

    @PostMapping("/clear-history")
    public String clearSearchHistory() {
        clearSearchHistoryInDatabase();
        return "redirect:/search-history";
    }

    // Define the Weather class to represent weather data
    public class Weather {
        private String cityName;
        private double temperature;
        private String weatherDescription;
        private double windSpeed;

        // Getters and setters

        public String getCityName() {
            return cityName;
        }

        public void setCityName(String cityName) {
            this.cityName = cityName;
        }

        public double getTemperature() {
            return temperature;
        }

        public void setTemperature(double temperature) {
            this.temperature = temperature;
        }

        public String getWeatherDescription() {
            return weatherDescription;
        }

        public void setWeatherDescription(String weatherDescription) {
            this.weatherDescription = weatherDescription;
        }

        public double getWindSpeed() {
            return windSpeed;
        }

        public void setWindSpeed(double windSpeed) {
            this.windSpeed = windSpeed;
        }
    }

    // Define the SearchEntry class to represent search history entries
    public class SearchEntry {
        private String locationName;
        private Timestamp searchTime;
        private double temperature;

        public SearchEntry(String locationName, Timestamp searchTime, double temperature) {
            this.locationName = locationName;
            this.searchTime = searchTime;
            this.temperature = temperature;
        }

        public String getLocationName() {
            return locationName;
        }

        public Timestamp getSearchTime() {
            return searchTime;
        }

        public double getTemperature() {
            return temperature;
        }
    }

    // Parse the JSON response from OpenWeatherMap
    private Weather parseWeatherResponse(String responseBody) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(responseBody);

            Weather weather = new Weather();
            weather.setCityName(rootNode.get("name").asText());

            // Convert temperature from Kelvin to Celsius
            double temperatureKelvin = rootNode.get("main").get("temp").asDouble();
            double temperatureCelsius = temperatureKelvin - 273.15;
            weather.setTemperature(temperatureCelsius);

            weather.setWeatherDescription(rootNode.get("weather").get(0).get("description").asText());

            // Set wind speed
            double windSpeed = rootNode.get("wind").get("speed").asDouble();
            weather.setWindSpeed(windSpeed);

            return weather;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void insertLocationIntoDatabase(String location, double temperature) {
        String url = "jdbc:postgresql://localhost:5432/weatherwise";
        String user = "xyz";
        String password = "abc";

        try {
            // Establish the database connection
            Connection con = DriverManager.getConnection(url, user, password);

            // Define the SQL query with placeholders for location, search_time, and temperature
            String sql = "INSERT INTO search_history (location_name, search_time, temperature) VALUES (?, ?, ?)";

            // Create a PreparedStatement to safely insert data
            PreparedStatement preparedStatement = con.prepareStatement(sql);

            // Set the values for the placeholders
            preparedStatement.setString(1, location);

            // Set the current timestamp for search_time
            preparedStatement.setTimestamp(2, new Timestamp(System.currentTimeMillis()));

            // Round the temperature to two decimal places
            double roundedTemperature = Math.round(temperature * 100.0) / 100.0;
            preparedStatement.setDouble(3, roundedTemperature);

            // Execute the query
            int rowsInserted = preparedStatement.executeUpdate();

            if (rowsInserted > 0) {
                System.out.println("Data inserted successfully");
            } else {
                System.out.println("Data insertion failed");
            }

            // Close the resources
            preparedStatement.close();
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private List<SearchEntry> retrieveSearchHistoryFromDatabase() {
        List<SearchEntry> searchHistory = new ArrayList<>();
        String url = "jdbc:postgresql://localhost:5432/weatherwise";
        String user = "xyz";
        String password = "abc";

        try {
            Connection con = DriverManager.getConnection(url, user, password);
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT location_name, search_time, temperature FROM search_history");

            while (rs.next()) {
                String locationName = rs.getString("location_name");
                Timestamp searchTime = rs.getTimestamp("search_time");
                double temperature = rs.getDouble("temperature");

                searchHistory.add(new SearchEntry(locationName, searchTime, temperature));
            }

            rs.close();
            st.close();
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return searchHistory;
    }

    private void clearSearchHistoryInDatabase() {
        String url = "jdbc:postgresql://localhost:5432/weatherwise";
        String user = "xyz";
        String password = "abc";

        try {
            Connection con = DriverManager.getConnection(url, user, password);
            Statement st = con.createStatement();
            st.executeUpdate("DELETE FROM search_history");

            st.close();
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping("/error-500")
    public String handleServerError() {
        return "error";
    }

    public static void main(String[] args) {
        SpringApplication.run(WeatherWiseApplication.class, args);
    }
}
