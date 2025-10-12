package es.marcha.backend.repository.inventory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import es.marcha.backend.model.inventory.Attribute;

@Repository
public interface AttributeRepository extends JpaRepository<Attribute, Long>{}
