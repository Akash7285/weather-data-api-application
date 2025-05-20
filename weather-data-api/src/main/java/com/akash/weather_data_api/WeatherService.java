package com.akash.weather_data_api;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class WeatherService {

    private final WebClient weatherWebClient;
    private final WebClient geoWebClient;

    public WeatherService(WebClient weatherWebClient, WebClient geoWebClient) {
        this.weatherWebClient = weatherWebClient;
        this.geoWebClient = geoWebClient;
    }

    public GeoCodingResponseDTO.Location getCoordinates(String location) {
        GeoCodingResponseDTO response = geoWebClient.get()
                .uri(uriBuilder -> uriBuilder.path("/v1/search")
                        .queryParam("name", location)
                        .queryParam("count", 1)
                        .queryParam("language", "en")
                        .build())
                .retrieve()
                .bodyToMono(GeoCodingResponseDTO.class)
                .block();

        if (response != null && response.getResults() != null && !response.getResults().isEmpty()) {
            return response.getResults().get(0);
        }
        return null;
    }

    public WeatherResponseDTO getWeather(String location) {
        GeoCodingResponseDTO.Location coordinates = getCoordinates(location);
        if (coordinates == null) {
            return null;
        }

        WeatherApiResponseDTO response = weatherWebClient.get()
                .uri(uriBuilder -> uriBuilder.path("/v1/forecast")
                        .queryParam("latitude", coordinates.getLatitude())
                        .queryParam("longitude", coordinates.getLongitude())
                        .queryParam("current_weather", "true")
                        .build())
                .retrieve()
                .bodyToMono(WeatherApiResponseDTO.class)
                .block();

        WeatherResponseDTO weatherResponse = response.getCurrentWeather();

        // 🛠 Correct Time Parsing
        if (weatherResponse.getTime() != null) {
            LocalDateTime localDateTime = LocalDateTime.parse(weatherResponse.getTime());
            ZonedDateTime utcTime = localDateTime.atZone(ZoneId.of("UTC"));
            ZonedDateTime indiaTime = utcTime.withZoneSameInstant(ZoneId.of("Asia/Kolkata"));
            String formattedTime = indiaTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            weatherResponse.setTime(formattedTime);
        }

        weatherResponse.setWeatherDescription(getWeatherDescription(weatherResponse.getWeatherCode()));
        weatherResponse.setWeatherIcon(getWeatherIcon(weatherResponse.getWeatherCode()));

        return weatherResponse;
    }

    private String getWeatherDescription(int code) {
        Map<Integer, String> weatherDescriptions = Map.ofEntries(
                Map.entry(0, "Clear sky"),
                Map.entry(1, "Mainly clear"),
                Map.entry(2, "Partly cloudy"),
                Map.entry(3, "Overcast"),
                Map.entry(45, "Fog"),
                Map.entry(48, "Depositing rime fog"),
                Map.entry(51, "Light drizzle"),
                Map.entry(53, "Moderate drizzle"),
                Map.entry(55, "Heavy drizzle"),
                Map.entry(56, "Freezing drizzle"),
                Map.entry(57, "Heavy freezing drizzle"),
                Map.entry(61, "Light rain"),
                Map.entry(63, "Moderate rain"),
                Map.entry(65, "Heavy rain"),
                Map.entry(80, "Rain showers"),
                Map.entry(81, "Heavy rain showers"),
                Map.entry(82, "Violent rain showers"),
                Map.entry(95, "Thunderstorm"),
                Map.entry(96, "Thunderstorm with hail"),
                Map.entry(99, "Severe thunderstorm with hail")
        );
        return weatherDescriptions.getOrDefault(code, "Unknown Weather");
    }

    private String getWeatherIcon(int code) {
        Map<Integer, String> weatherIcons = Map.ofEntries(
                Map.entry(0, "☀️"),
                Map.entry(1, "🌤️"),
                Map.entry(2, "⛅"),
                Map.entry(3, "☁️"),
                Map.entry(45, "🌫️"),
                Map.entry(48, "🌁"),
                Map.entry(51, "🌦️"),
                Map.entry(53, "🌧️"),
                Map.entry(55, "🌧️"),
                Map.entry(56, "🌨️"),
                Map.entry(57, "❄️"),
                Map.entry(61, "🌧️"),
                Map.entry(63, "🌧️"),
                Map.entry(65, "🌧️"),
                Map.entry(80, "🌦️"),
                Map.entry(81, "🌧️"),
                Map.entry(82, "⛈️"),
                Map.entry(95, "🌩️"),
                Map.entry(96, "⛈️"),
                Map.entry(99, "⛈️")
        );
        return weatherIcons.getOrDefault(code, "❓");
    }
}
