package es.marcha.backend.dto.response.user;

import java.time.LocalDateTime;

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
public class TermsResponseDTO {
    private String termsVersion;
    private LocalDateTime termsAcceptedAt;
}
