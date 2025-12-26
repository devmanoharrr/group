package kf7014.crowdsourceddata.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import kf7014.crowdsourceddata.model.WaterObservation;
import kf7014.crowdsourceddata.repository.WaterObservationRepository;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Component
public class SeedDataLoader implements ApplicationRunner {

    private final WaterObservationRepository repository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SeedDataLoader(WaterObservationRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (repository.count() == 0) {
            loadSeedData();
        }
    }

    private void loadSeedData() throws IOException {
        ClassPathResource resource = new ClassPathResource("data/seed-observations.json");
        if (!resource.exists()) return;
        try (InputStream is = resource.getInputStream()) {
            List<WaterObservation> observations = objectMapper.readValue(is, new TypeReference<List<WaterObservation>>() {});
            repository.saveAll(observations);
        }
    }
}


