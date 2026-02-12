package es.marcha.backend.services.ecommerce;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.marcha.backend.exception.ProductException;
import es.marcha.backend.model.ecommerce.Subcategory;
import es.marcha.backend.repository.ecommerce.SubcategoryRepository;

@Service
public class SubcategoryService {
    // Attribs
    @Autowired
    private SubcategoryRepository subcatRepository;

    /**
     * Recupera una lista de subcategorías a partir de una lista de identificadores.
     *
     * Realiza una consulta al repositorio para obtener todas las subcategorías
     * cuyos IDs coincidan con los proporcionados. Si no se encuentra ninguna
     * subcategoría asociada a los IDs indicados, se lanza una excepción.
     *
     * Nota: Este método valida únicamente que la lista resultante no esté vacía.
     * No garantiza que todos los IDs enviados existan en base de datos.
     *
     * @param categoryIds Lista de identificadores de subcategorías a recuperar
     * @return Lista de subcategorías encontradas
     * @throws ProductException si no se encuentra ninguna subcategoría
     */
    public List<Subcategory> getAllSubcategories(List<Long> categoryIds) {
        List<Subcategory> subcategories = subcatRepository.findAllById(categoryIds);
        if (subcategories.isEmpty()) {
            throw new ProductException(ProductException.FAILED_FETCH_SUBCATEGORY);
        }
        return subcategories;
    }

    public Subcategory getSubcategoryById(long id) {
        return subcatRepository.findById(id)
                .orElseThrow(() -> new ProductException(ProductException.FAILED_FETCH_SUBCATEGORY));
    }

}
