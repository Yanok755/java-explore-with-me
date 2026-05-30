package ru.practicum.stats.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.practicum.stats.dto.EndpointHit;
import ru.practicum.stats.dto.ViewStats;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
public class StatsClient {
    private final WebClient webClient;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StatsClient(@Value("${stats-server.url:http://localhost:9090}") String serverUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(serverUrl)
                .build();
    }

    public void saveHit(String app, String uri, String ip, LocalDateTime timestamp) {
        EndpointHit hit = EndpointHit.builder()
                .app(app)
                .uri(uri)
                .ip(ip)
                .timestamp(timestamp)
                .build();

        webClient.post()
                .uri("/hit")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(hit)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnError(error -> log.error("Error saving hit: {}", error.getMessage()))
                .onErrorResume(error -> Mono.empty())
                .subscribe();
    }

    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        StringBuilder uriBuilder = new StringBuilder("/stats?start=")
                .append(encode(start.format(FORMATTER)))
                .append("&end=")
                .append(encode(end.format(FORMATTER)));

        if (uris != null && !uris.isEmpty()) {
            uriBuilder.append("&uris=").append(String.join(",", uris));
        }

        if (unique != null && unique) {
            uriBuilder.append("&unique=true");
        }

        return webClient.get()
                .uri(uriBuilder.toString())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<ViewStats>>() {})
                .block();
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
