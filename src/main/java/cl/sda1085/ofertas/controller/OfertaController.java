package cl.sda1085.ofertas.controller;

import cl.sda1085.ofertas.dto.OfertaRequestDTO;
import cl.sda1085.ofertas.dto.OfertaResponseDTO;
import cl.sda1085.ofertas.service.OfertaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Slf4j
@RestController
@RequestMapping("/api/ofertas")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Ofertas", description = "Controlador para el seguimiento y registro de pujas de arte.")

public class OfertaController {

    //Conexión con 'service'
    private final OfertaService ofertaService;

    @Operation(summary = "Obtener una oferta por su ID", description = "Devuelve los datos de una puja junto con sus hipervínculos de navegación REST.")
    @ApiResponse(responseCode = "200", description = "Oferta localizada.")
    @ApiResponse(responseCode = "404", description = "La oferta solicitada no existe.")
    @GetMapping("/{id}")
    public ResponseEntity<OfertaResponseDTO> obtenerOfertaPorId(@PathVariable Long id) {

        OfertaResponseDTO dto = ofertaService.obtenerPorId(id)
                .orElseThrow(() -> new RuntimeException("Oferta no encontrada con el ID: " + id + "."));

        //Enlaces HATEOAS
        dto.add(linkTo(methodOn(OfertaController.class).obtenerOfertaPorId(id)).withSelfRel());
        dto.add(linkTo(methodOn(OfertaController.class).listarTodasLasOfertas()).withRel("lista-completa-ofertas"));

        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Listar todas las ofertas.", description = "Retorna el historial completo de ofertas del sistema.")
    @GetMapping
    public ResponseEntity<List<OfertaResponseDTO>> listarTodasLasOfertas() {
        return ResponseEntity.ok(ofertaService.obtenerTodas());
    }

    //==============================
    //CRUD estándar
    //==============================

    /*@GetMapping
    public ResponseEntity<List<OfertaResponseDTO>> obtenerTodas() {
        log.info("Recibida petición GET para listar todas las ofertas.");
        return ResponseEntity.ok(ofertaService.obtenerTodas());
    }*/

    /*@GetMapping("/{id}")
    public ResponseEntity <OfertaResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ofertaService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }*/

    @PostMapping
    public ResponseEntity<OfertaResponseDTO> crearOferta(
            @Valid @RequestBody OfertaRequestDTO dto){

        log.warn("Petición POST recibida para crear oferta de usuario: {}", dto.getIdUsuario());
        return ResponseEntity.status(HttpStatus.CREATED).body(ofertaService.guardar(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OfertaResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody OfertaRequestDTO dto){

        log.info("Recibida petición PUT para actualizar la oferta ID: {}", id);

        return ofertaService.actualizar(id, dto)
                .map(response -> {
                    log.debug("Respuesta exitosa para actualización de ID: {}", id);
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    log.warn("Respuesta 404: Oferta ID {} no encontrada para actualizar", id);
                    return ResponseEntity.notFound().build();
                });
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void>  eliminar(@PathVariable Long id) {
        log.warn("Petición de eliminación para la oferta ID: {}", id);
        ofertaService.eliminar(id);

        return ResponseEntity.noContent().build();
    }


    //==============================
    //CRUD personalizado
    //==============================

    //Busca todas las ofertas realizadas por un usuario específico
    //GET /api/ofertas/usuario/{idUsuario}
    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<OfertaResponseDTO>> obtenerPorUsuario(@PathVariable Long idUsuario) {
        log.info("Recibida petición GET para obtener ofertas del usuario ID: {}", idUsuario);

        List<OfertaResponseDTO> ofertas = ofertaService.obtenerOfertasPorUsuario(idUsuario);
        log.info("Se enviaron " + ofertas.size() + "ofertas para el usuario ID: {}" + idUsuario + ".");

        return ResponseEntity.ok(ofertas);
    }

    //Cuenta cuántas ofertas se han realizado en una subasta
    //GET /api/ofertas/subasta/{idSubasta}/total
    @GetMapping("/subasta/{idSubasta}/total")
    public ResponseEntity<Long> contarOfertas(@PathVariable Long idSubasta) {
        log.info("Recibida petición GET para contar ofertas de la subasta ID: {}", idSubasta);

        Long total = ofertaService.contarOfertasPorSubasta(idSubasta);
        log.info("Total contabilizado para subasta {}: {}", idSubasta, total);

        return ResponseEntity.ok(total);
    }

    //Busca ofertas que superen un monto en una subasta específica.
    //GET /api/ofertas/subasta/{idSubasta}/mayores-a?monto=1000
    @GetMapping("/subasta/{idSubasta}/mayores-a")
    public ResponseEntity<List<OfertaResponseDTO>> obtenerOfertasMayores(
            @PathVariable Long idSubasta,
            @RequestParam BigDecimal monto) {

        log.info("Recibida petición GET para ofertas en subasta {} superiores a: {}", idSubasta, monto);

        List<OfertaResponseDTO> ofertas = ofertaService.obtenerOfertasMayoresA(idSubasta, monto);
        log.info("Encontradas" + ofertas.size() + " ofertas que superan el monto solicitado");

        return ResponseEntity.ok(ofertas);
    }

    //Verifica si un usuario ya ha realizado alguna oferta en una subasta
    //GET /api/ofertas/verificar/usuario/{idUsuario}/subasta/{idSubasta}
    @GetMapping("/verificar/usuario/{idUsuario}/subasta/{idSubasta}")
    public ResponseEntity<Boolean> verificarParticipacion(
            @PathVariable Long idUsuario,
            @PathVariable Long idSubasta) {

        log.info("Recibida petición GET para verificar participación del usuario {} en subasta {}", idUsuario, idSubasta);
        boolean participo = ofertaService.verificarSiUsuarioOferto(idUsuario, idSubasta);

        log.info("Resultado de verificación para usuario" + idUsuario + "en subasta" + idSubasta + ": " + participo + ".");
        return ResponseEntity.ok(participo);
    }

    //Obtiene las 3 ofertas más altas de una subasta
    //GET /api/ofertas/subasta/{idSubasta}/top3
    @GetMapping("/subasta/{idSubasta}/top3")
    public ResponseEntity<List<OfertaResponseDTO>> obtenerTop3(@PathVariable Long idSubasta) {

        log.info("Recibida petición GET para obtener el TOP 3 de la subasta ID: {}", idSubasta);
        List<OfertaResponseDTO> top3 = ofertaService.obtenerTop3Subasta(idSubasta);

        log.info("Enviando TOP" + top3.size() + " de ofertas para la subasta" + idSubasta + ".");
        return ResponseEntity.ok(top3);
    }
}
