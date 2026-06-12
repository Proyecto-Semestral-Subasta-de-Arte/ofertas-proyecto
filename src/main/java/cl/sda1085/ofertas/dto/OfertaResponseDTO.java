package cl.sda1085.ofertas.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.hateoas.RepresentationModel;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor

@EqualsAndHashCode(callSuper = false)
@Builder
@Schema(description = "Estructura de respuesta que retorna el servidor con los detalles consolidados de la puja junto a los enlaces HATEOAS.")
public class OfertaResponseDTO extends RepresentationModel<OfertaResponseDTO> {

    //DTO de salida (respuesta).
    //No existen las anotaciones de validación.

    @Schema(description = "Identificador único incremental autogenerado en la base de datos de ofertas.", example = "2")
    private Long id;

    @Schema(description = "Monto final validado y almacenado para la puja.", example = "175000.00")
    private BigDecimal monto;

    @Schema(description = "Fecha y hora exacta en la que se interceptó e ingresó la oferta al sistema.", example = "2026-06-11T21:15:30")
    private LocalDateTime fechaHora;

    @Schema(description = "ID del usuario comprador que realizó la oferta.", example = "6")
    private Long idUsuario;

    @Schema(description = "ID de la subasta en la cual se compitió.", example = "1")
    private Long idSubasta;
}
