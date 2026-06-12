package cl.sda1085.ofertas.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Modelo requerido (JSON) para registrar o actualizar una puja/oferta en el sistema.")

public class OfertaRequestDTO {

    @Schema(description = "Monto económico de la puja que se desea postular.", example = "175000.00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "El monto es obligatorio.")
    @Positive(message = "El monto debe ser mayor a cero.")
    private BigDecimal monto;

    @Schema(description = "Identificador único del usuario comprador proveniente del microservicio 'usuarios'.", example = "6", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "El ID del usuario es obligatorio.")
    private Long idUsuario;

    @Schema(description = "Identificador único de la subasta activa proveniente del microservicio 'subastas'.", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "El ID de la subasta es obligatorio.")
    private Long idSubasta;
}
