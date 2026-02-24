package es.marcha.backend.services.user;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.marcha.backend.dto.response.user.AddressResponseDTO;
import es.marcha.backend.exception.AddressException;
import es.marcha.backend.exception.UserException;
import es.marcha.backend.mapper.AddressMapper;
import es.marcha.backend.model.user.Address;
import es.marcha.backend.model.user.User;
import es.marcha.backend.repository.user.AddressRepository;
import es.marcha.backend.repository.user.UserRepository;
import jakarta.transaction.Transactional;

@Service
public class AddressService {
    // Attribs
    @Autowired
    private AddressRepository aRepository;
    @Autowired
    private UserRepository uRepository;

    public static final String ADDRESS_DELETED = "ADDRESS WAS DELETED";

    public List<AddressResponseDTO> getAllAddressesByUserId(long id) {
        List<AddressResponseDTO> addresses = aRepository.findAllByUserId(id).stream()
                .map(AddressMapper::toAddressdDTO)
                .toList();

        if (addresses.isEmpty()) {
            throw new AddressException(AddressException.ADDRESSES_NOT_FOUND);
        }

        return addresses;
    }

    public AddressResponseDTO getAddressById(long id) {
        return aRepository.findById(id)
                .map(AddressMapper::toAddressdDTO)
                .orElseThrow(() -> new AddressException());
    }

    public AddressResponseDTO saveAddress(Address address) {
        User user = uRepository.findById(address.getUser().getId())
                .orElseThrow(() -> new UserException());
        address.setUser(user);
        try {
            return AddressMapper.toAddressdDTO(aRepository.save(address));
        } catch (Exception e) {
            throw new AddressException(AddressException.FAILED_SAVE);
        }
    }

    /**
     * Actualiza una dirección existente en base a los datos proporcionados.
     * <p>
     * El método busca la dirección por su identificador. Si no existe,
     * lanza una {@link AddressException}.
     * Una vez encontrada, actualiza únicamente los campos modificables
     * y persiste los cambios en la base de datos.
     * </p>
     *
     * @param updatedAddress objeto {@link Address} que contiene el ID de la
     *                       dirección
     *                       y los nuevos valores a actualizar.
     * @return {@link AddressResponseDTO} con la información actualizada de la
     *         dirección.
     * @throws AddressException si la dirección no existe.
     */
    @Transactional
    public AddressResponseDTO updateAddress(Address updatedAddress) {
        System.out.println(updatedAddress.toString());
        Address address = aRepository.findById(updatedAddress.getId())
                .orElseThrow(() -> new AddressException());

        address.setDefault(updatedAddress.isDefault());
        address.setType(updatedAddress.getType());
        address.setAddressLine1(updatedAddress.getAddressLine1());
        address.setAddressLine2(updatedAddress.getAddressLine2());
        address.setCountry(updatedAddress.getCountry());
        address.setCity(updatedAddress.getCity());
        address.setPostalCode(updatedAddress.getPostalCode());

        return AddressMapper.toAddressdDTO(aRepository.save(address));
    }

    /**
     * Elimina una dirección existente por su identificador.
     * <p>
     * Si la dirección no existe, se lanza una {@link AddressException}.
     * La eliminación se realiza dentro de una transacción para garantizar
     * la consistencia de los datos.
     * </p>
     *
     * @param id identificador de la dirección a eliminar.
     * @return mensaje de confirmación indicando que la dirección fue eliminada.
     * @throws AddressException si la dirección no existe.
     */
    @Transactional
    public String deleteAddress(long id) {
        Address address = aRepository.findById(id).orElseThrow(() -> new AddressException());
        aRepository.delete(address);
        return ADDRESS_DELETED;
    }

}
