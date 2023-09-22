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

import java.util.*;

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
            return "weather";
        } else {
            return "error";
        }
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

    @RequestMapping("/error-500")
    public String handleServerError() {
        return "error";
    }

    public static void main(String[] args) {
        SpringApplication.run(WeatherWiseApplication.class, args);
    }
}
