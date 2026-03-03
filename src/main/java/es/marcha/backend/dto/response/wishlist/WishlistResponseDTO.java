package es.marcha.backend.dto.response.wishlist;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
public class WishlistResponseDTO {

    private long id;
    private long userId;
    private List<WishlistItemResponseDTO> items;
    private int totalItems;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
