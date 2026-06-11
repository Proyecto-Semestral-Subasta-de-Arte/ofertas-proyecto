package cl.sda1085.ofertas.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfertaRequestDTO {

    @NotNull(message = "El monto es obligatorio.")
    @Positive(message = "El monto debe ser mayor a cero.")
    private BigDecimal monto;

    @NotNull(message = "El ID del usuario es obligatorio.")
    private Long idUsuario;

    @NotNull(message = "El ID de la subasta es obligatorio.")
    private Long idSubasta;
}
