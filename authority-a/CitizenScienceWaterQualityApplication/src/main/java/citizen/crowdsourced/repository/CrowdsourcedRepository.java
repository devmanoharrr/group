package citizen.crowdsourced.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import citizen.crowdsourced.model.CrowdsourcedRecord;
import java.util.List;

public interface CrowdsourcedRepository extends JpaRepository<CrowdsourcedRecord, String> {
     List<CrowdsourcedRecord> findByCitizenId(String citizenId);
}