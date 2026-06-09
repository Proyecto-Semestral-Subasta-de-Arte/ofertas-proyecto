package cl.sda1085.ofertas.dto;

import lombok.*;
import org.springframework.hateoas.RepresentationModel;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor

@EqualsAndHashCode(callSuper = false)
@Builder

public class OfertaResponseDTO extends RepresentationModel<OfertaResponseDTO> {

    //DTO de salida (respuesta).
    //No existen las anotaciones de validación.

    private Long id;
    private BigDecimal monto;
    private LocalDateTime fechaHora;
    private Long idUsuario;
    private Long idSubasta;
}
