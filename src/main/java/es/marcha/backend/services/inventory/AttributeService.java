package es.marcha.backend.services.inventory;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import es.marcha.backend.model.inventory.AttribValue;
import es.marcha.backend.model.inventory.Attribute;
import es.marcha.backend.repository.inventory.AttribValueRepository;
import es.marcha.backend.repository.inventory.AttributeRepository;
import jakarta.transaction.Transactional;

@Service
public class AttributeService {
    @Autowired
    private AttributeRepository attributeRepository;

    @Autowired
    private AttribValueRepository valueRepository;

    @Transactional
    public Attribute createAttribute(String name, List<String> values) {
        Attribute attribute = new Attribute();
        attribute.setName(name);

        // Crear los valores asociados al atributo
        List<AttribValue> valueEntities = values.stream()
            .map(v -> {
                AttribValue val = new AttribValue();
                val.setValue(v);
                val.setAttribute(attribute);
                return val;
            })
            .collect(Collectors.toList());

        attribute.setValues(valueEntities);

        // Guardar el atributo con sus valores (cascade se encarga del resto)
        return attributeRepository.save(attribute);
    }

    @Transactional
    public AttribValue addValueToAttribute(Long attributeId, String newValue) {
    Attribute attribute = attributeRepository.findById(attributeId)
        .orElseThrow(() -> new RuntimeException("Atributo no encontrado"));

    AttribValue value = new AttribValue();
    value.setValue(newValue);
    value.setAttribute(attribute);

    return valueRepository.save(value);
}
    
}
