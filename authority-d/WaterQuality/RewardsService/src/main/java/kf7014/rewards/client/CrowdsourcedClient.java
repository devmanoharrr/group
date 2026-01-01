package kf7014.rewards.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Component
public class CrowdsourcedClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${crowdsourced.baseUrl:http://localhost:8081}")
    private String baseUrl;

    public List<ObservationDto> findByCitizenId(String citizenId) {
        String url = baseUrl + "/api/observations?citizenId=" + citizenId;
        ResponseEntity<ObservationDto[]> response = restTemplate.getForEntity(url, ObservationDto[].class);
        ObservationDto[] body = response.getBody();
        if (body == null) return List.of();
        return Arrays.asList(body);
    }

    public static class ObservationDto {
        public String id;
        public String citizenId;
        public String postcode;
        public Measurements measurements;
        public java.util.List<String> observations;
        public java.util.List<String> imageData;

        public static class Measurements {
            public Double temperatureCelsius;
            public Double ph;
            public Double alkalinityMgPerL;
            public Double turbidityNtu;
        }
    }
}


