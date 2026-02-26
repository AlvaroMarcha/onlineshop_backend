package es.marcha.backend.controller.user;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.marcha.backend.dto.response.user.AddressResponseDTO;
import es.marcha.backend.model.user.Address;
import es.marcha.backend.services.user.AddressService;

@RestController
@RequestMapping("/address")
public class AddressController {
    @Autowired
    private AddressService aService;

    /**
     * Obtiene todas las direcciones asociadas a un usuario.
     *
     * @param id El ID del usuario cuyas direcciones se desean obtener.
     * @return {@link ResponseEntity} con la lista de {@link AddressResponseDTO} y código HTTP 200 OK.
     */
    @GetMapping("/{id}")
    public ResponseEntity<List<AddressResponseDTO>> getAllAddressByUser(@PathVariable Long id) {
        List<AddressResponseDTO> addresses = aService.getAllAddressesByUserId(id);
        return new ResponseEntity<>(addresses, HttpStatus.OK);
    }

    /**
     * Crea y persiste una nueva dirección asociada a un usuario.
     *
     * @param address La dirección a guardar. Debe incluir el ID del usuario al que pertenece.
     * @return {@link ResponseEntity} con la {@link AddressResponseDTO} creada y código HTTP 200 OK.
     */
    @PostMapping
    public ResponseEntity<AddressResponseDTO> saveAddress(@RequestBody Address address) {
        AddressResponseDTO savedAddress = aService.saveAddress(address);
        return new ResponseEntity<>(savedAddress, HttpStatus.OK);
    }

    /**
     * Actualiza los datos de una dirección existente.
     *
     * @param address La dirección con los nuevos datos. Debe incluir un ID válido.
     * @return {@link ResponseEntity} con la {@link AddressResponseDTO} actualizada y código HTTP 200 OK.
     */
    @PutMapping
    public ResponseEntity<AddressResponseDTO> updateAddress(@RequestBody Address address) {
        AddressResponseDTO updatedAddress = aService.updateAddress(address);
        return new ResponseEntity<>(updatedAddress, HttpStatus.OK);
    }

    /**
     * Elimina una dirección por su ID.
     *
     * @param id El ID de la dirección a eliminar.
     * @return {@link ResponseEntity} con un mensaje de confirmación y código HTTP 200 OK.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAddress(@PathVariable long id) {
        String msg = aService.deleteAddress(id);
        return new ResponseEntity<>(msg, HttpStatus.OK);
    }

}
