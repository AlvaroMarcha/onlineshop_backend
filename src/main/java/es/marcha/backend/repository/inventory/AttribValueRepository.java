package es.marcha.backend.repository.inventory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import es.marcha.backend.model.inventory.AttribValue;

@Repository
public interface AttribValueRepository extends JpaRepository<AttribValue, Long>{}
