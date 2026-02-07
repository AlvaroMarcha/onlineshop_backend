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

import es.marcha.backend.dto.response.AddressResponseDTO;
import es.marcha.backend.model.user.Address;
import es.marcha.backend.services.user.AddressService;

@RestController
@RequestMapping("/address")
public class AddressController {
    @Autowired
    private AddressService aService;

    @GetMapping("/{id}")
    public ResponseEntity<List<AddressResponseDTO>> getAllAddressByUser(@PathVariable Long id) {
        List<AddressResponseDTO> addresses = aService.getAllAddressesByUserId(id);
        return new ResponseEntity<>(addresses, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<AddressResponseDTO> saveAddress(@RequestBody Address address) {
        AddressResponseDTO savedAddress = aService.saveAddress(address);
        return new ResponseEntity<>(savedAddress, HttpStatus.OK);
    }

    @PutMapping
    public ResponseEntity<AddressResponseDTO> updateAddress(@RequestBody Address address) {
        AddressResponseDTO updatedAddress = aService.updateAddress(address);
        return new ResponseEntity<>(updatedAddress, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAddress(@PathVariable long id) {
        String msg = aService.deleteAddress(id);
        return new ResponseEntity<>(msg, HttpStatus.OK);
    }

}
