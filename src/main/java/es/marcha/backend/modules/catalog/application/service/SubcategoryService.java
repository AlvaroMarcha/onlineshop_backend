package es.marcha.backend.modules.catalog.application.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.marcha.backend.modules.catalog.application.dto.response.SubcategoryResponseDTO;
import es.marcha.backend.core.error.exception.ProductException;
import es.marcha.backend.modules.catalog.application.mapper.SubcategoryMapper;
import es.marcha.backend.modules.catalog.domain.model.Subcategory;
import es.marcha.backend.modules.catalog.infrastructure.persistence.SubcategoryRepository;
import es.marcha.backend.core.shared.utils.ProductUtils;
import jakarta.transaction.Transactional;

@Service
public class SubcategoryService {
    // Attribs
    @Autowired
    private SubcategoryRepository subcatRepository;

    public static final String SUBCATEGORY_DELETED = "SUBCATEGORY WAS DELETED";

    public List<SubcategoryResponseDTO> getAllSubcategories() {
        List<SubcategoryResponseDTO> subcategories = subcatRepository.findAll()
                .stream()
                .map(SubcategoryMapper::toResponseDTO)
                .toList();

        if (subcategories.isEmpty()) {
            throw new ProductException(ProductException.FAILED_FETCH_SUBCATEGORY);
        }
        return subcategories;
    }

    /**
     * Recupera una lista de subcategorías como DTOs a partir de una lista de
     * identificadores.
     *
     * Este método consulta el repositorio para obtener todas las subcategorías
     * cuyos IDs coincidan con los proporcionados y las convierte a
     * {@link SubcategoryResponseDTO} mediante el mapper.
     * <p>
     * Si no se encuentra ninguna subcategoría asociada a los IDs indicados,
     * se lanza una {@link ProductException}.
     * <p>
     * Nota: Este método valida únicamente que la lista resultante no esté vacía.
     * No garantiza que todos los IDs enviados existan en la base de datos; la
     * lista retornada puede contener solo los subcategorías existentes.
     *
     * @param categoryIds Lista de identificadores de subcategorías a recuperar.
     * @return Lista de DTOs de subcategorías encontradas.
     * @throws ProductException si no se encuentra ninguna subcategoría.
     */
    public List<SubcategoryResponseDTO> getAllSubcategories(List<Long> categoryIds) {
        List<SubcategoryResponseDTO> subcategories = subcatRepository.findAllById(categoryIds)
                .stream()
                .map(SubcategoryMapper::toResponseDTO)
                .toList();

        if (subcategories.isEmpty()) {
            throw new ProductException(ProductException.FAILED_FETCH_SUBCATEGORY);
        }
        return subcategories;
    }

    /**
     * Obtiene una lista de subcategorías a partir de una lista de IDs.
     * <p>
     * Este método consulta el repositorio y devuelve las entidades completas
     * de Subcategory correspondientes a los IDs proporcionados. Si no se encuentra
     * ninguna subcategoría, lanza una excepción.
     *
     * @param categoryIds Lista de IDs de subcategorías a buscar.
     * @return Lista de subcategorías correspondientes a los IDs.
     * @throws ProductException Si no se encuentra ninguna subcategoría.
     */
    public List<Subcategory> getAllSubcategoriesHandler(List<Long> categoryIds) {
        // Lista nula o vacía es válida: el producto se crea sin subcategorías
        if (categoryIds == null || categoryIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Subcategory> subcategories = subcatRepository.findAllById(categoryIds);

        if (subcategories.isEmpty()) {
            throw new ProductException(ProductException.FAILED_FETCH_SUBCATEGORY);
        }
        return subcategories;
    }

    /**
     * Obtiene una subcategoría por su ID y la convierte a DTO para la respuesta.
     * <p>
     * Este método busca la subcategoría en el repositorio, la transforma a
     * SubcategoryResponseDTO usando el mapper y la devuelve. Si no existe,
     * lanza una excepción.
     *
     * @param id ID de la subcategoría a buscar.
     * @return DTO de la subcategoría correspondiente al ID.
     * @throws ProductException Si no se encuentra la subcategoría.
     */
    public SubcategoryResponseDTO getSubcategoryById(long id) {
        return subcatRepository.findById(id)
                .map(SubcategoryMapper::toResponseDTO)
                .orElseThrow(() -> new ProductException(ProductException.FAILED_FETCH_SUBCATEGORY));
    }

    /**
     * Crea una nueva subcategoría en la base de datos y devuelve su DTO.
     *
     * Este método valida que el objeto {@link Subcategory} no sea nulo
     * y que el nombre no esté vacío. Si la validación falla, se lanza
     * una {@link ProductException}.
     * <p>
     * Luego, persiste la subcategoría en el repositorio y la transforma
     * a {@link SubcategoryResponseDTO} mediante el mapper antes de retornarla.
     *
     * @param subcategory La entidad de subcategoría a crear.
     * @return DTO de la subcategoría recién creada.
     * @throws ProductException Si el objeto subcategoría es nulo o su nombre está
     *                          vacío,
     *                          o si falla la creación en la base de datos.
     */
    @Transactional
    public SubcategoryResponseDTO createSubcategory(Subcategory subcategory) {
        if (subcategory == null || subcategory.getName().trim().isEmpty()) {
            throw new ProductException(ProductException.FAILED_CREATE_SUBCATEGORY);
        }

        subcategory.setActive(true);
        subcategory.setSlug(ProductUtils.createSlug(subcategory.getName()));
        subcategory.setCreatedAt(LocalDateTime.now());
        Subcategory savedSubcategory = subcatRepository.save(subcategory);
        return SubcategoryMapper.toResponseDTO(savedSubcategory);
    }

    /**
     * Actualiza una subcategoría existente en la base de datos y devuelve su DTO.
     *
     * Este método busca la subcategoría por su ID. Si no existe, lanza
     * una {@link ProductException}. Luego, actualiza los campos `name`,
     * `description`, `slug` y `updatedAt` con los valores de la entidad
     * proporcionada y guarda los cambios en el repositorio.
     *
     * @param subcategory La entidad de subcategoría con los nuevos valores y el ID
     *                    existente.
     * @return DTO de la subcategoría actualizada.
     * @throws ProductException Si no se encuentra la subcategoría por el ID
     *                          proporcionado.
     */
    @Transactional
    public SubcategoryResponseDTO updateSubcategory(Subcategory subcategory) {
        Subcategory updatedSubcategory = subcatRepository.findById(subcategory.getId())
                .orElseThrow(() -> new ProductException(ProductException.FAILED_UPDATE_SUBCATEGORY));

        updatedSubcategory.setName(subcategory.getName());
        updatedSubcategory.setDescription(subcategory.getDescription());
        updatedSubcategory.setSlug(ProductUtils.createSlug(updatedSubcategory.getName()));
        updatedSubcategory.setUpdatedAt(LocalDateTime.now());

        return SubcategoryMapper.toResponseDTO(subcatRepository.save(updatedSubcategory));
    }

    /**
     * Elimina una subcategoría de la base de datos a partir de su ID.
     *
     * Este método busca la subcategoría por el ID proporcionado. Si no se
     * encuentra,
     * lanza una {@link ProductException}. Una vez encontrada, elimina la entidad
     * del
     * repositorio y devuelve un mensaje de confirmación.
     *
     * @param id ID de la subcategoría a eliminar.
     * @return Mensaje de confirmación de la eliminación.
     * @throws ProductException Si no se encuentra la subcategoría con el ID
     *                          proporcionado.
     */
    public String deleteSubcategory(long id) {
        Subcategory subcategory = subcatRepository.findById(id)
                .orElseThrow(() -> new ProductException(ProductException.FAILED_DELETE_SUBCATEGORY));

        subcatRepository.delete(subcategory);
        return SUBCATEGORY_DELETED;
    }
}
