package cl.sda1085.ofertas.service;

import cl.sda1085.ofertas.dto.OfertaRequestDTO;
import cl.sda1085.ofertas.dto.OfertaResponseDTO;
import cl.sda1085.ofertas.model.Oferta;
import cl.sda1085.ofertas.repository.OfertaRepository;
import cl.sda1085.ofertas.webclient.SubastaClient;
import cl.sda1085.ofertas.webclient.UsuarioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor

public class OfertaService {

    private final OfertaRepository ofertaRepository;
    private final UsuarioClient usuarioClient;
    private final SubastaClient subastaClient;

    //==============================
    //CRUD estándar
    //==============================

    //Método de apoyo para encapsulamiento de datos
    private OfertaResponseDTO mapToResponseDTO(Oferta oferta) {
        return new OfertaResponseDTO(
                oferta.getId(),
                oferta.getMonto(),
                oferta.getFechaHora(),
                oferta.getIdUsuario(),
                oferta.getIdSubasta()
        );
    }

    //Obtener todas las ofertas
    public List<OfertaResponseDTO> obtenerTodas() {
        log.info("Iniciando recuperación de todas las ofertas.");

        List<Oferta> ofertas = ofertaRepository.findAll();
        log.debug("Se recuperaron" + ofertas.size() + "ofertas de la base de datos.");

        return ofertas.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    //Obtener oferta por ID
    public Optional<OfertaResponseDTO> obtenerPorId(Long id) {
        return ofertaRepository.findById(id)
                .map(oferta -> {
                    log.debug("Oferta encontrada para el ID: {}", id);
                    return mapToResponseDTO(oferta);
                })
                .or(() -> {
                    log.warn("No se encontró la oferta con ID: {}", id);
                    return  Optional.empty();
                });
    }

    //Guardar (crear) oferta
    public OfertaResponseDTO guardar(OfertaRequestDTO dto) {
        log.info("Registrando nueva oferta: Monto {} para la subasta ID {} por el usuario ID {}",
                dto.getMonto(), dto.getIdSubasta(), dto.getIdUsuario());

        //Validar que el usuario existe
        usuarioClient.obtenerUsuarioPorId(dto.getIdUsuario());

        //Validar que la subasta existe
        subastaClient.obtenerSubastaPorId(dto.getIdSubasta());

        Oferta oferta = new Oferta();
        oferta.setMonto(dto.getMonto());
        oferta.setIdUsuario(dto.getIdUsuario());
        oferta.setIdSubasta(dto.getIdSubasta());  //La oferta suele ligarse al ID de la SUBASTA.
        oferta.setFechaHora(LocalDateTime.now());  //Registro del momento exacto de la puja.

        Oferta ofertaGuardada = ofertaRepository.save(oferta);
        log.debug("Oferta guardada exitosamente con ID: {}", ofertaGuardada.getId());

        return mapToResponseDTO(ofertaGuardada);
    }

    //Actualizar oferta
    public Optional<OfertaResponseDTO> actualizar(Long id, OfertaRequestDTO dto){
        log.info("Iniciando actualización de la oferta con ID: {}", id);

        return ofertaRepository.findById(id)
                .map(ofertaExistente -> {
                            log.debug("Oferta encontrada. Actualizando monto de {} a {}",
                                    ofertaExistente.getMonto(), dto.getMonto());

                            ofertaExistente.setMonto(dto.getMonto());
                            ofertaExistente.setIdUsuario(dto.getIdUsuario());
                            ofertaExistente.setIdSubasta(dto.getIdSubasta());

                            Oferta actualizada = ofertaRepository.save(ofertaExistente);
                            log.info("Oferta ID: {} actualizada exitosamente", id);
                            return mapToResponseDTO(actualizada);

                        }).or(() -> {
                            log.warn("No se pudo actualizar: La oferta con ID no existe", id);
                            return Optional.empty();
                });
    }

    //Eliminar oferta
    public void eliminar(Long id) {
        log.info("Intentando eliminar la oferta con ID: {}", id);

        if (ofertaRepository.existsById(id)) {
            ofertaRepository.deleteById(id);
            log.info("Oferta con ID: {} eliminada correctamente", id);
        } else {
            log.error("Error al eliminar: No se encontró la oferta con ID: {}", id);
        }
    }


    //==============================
    //CRUD personalizado
    //==============================

    //Obtener la oferta más alta (Posible ganador)
    public Optional<OfertaResponseDTO> obtenerOfertaMasAlta(Long idSubasta) {
        log.info("Buscando la oferta más alta para la subasta ID: {}", idSubasta);
        Optional <Oferta> oferta = ofertaRepository.findFirstByIdSubastaOrderByMontoDesc(idSubasta);

        if (oferta.isPresent()) {
            log.debug("Oferta más alta encontrada: ID", oferta.get().getId());
        } else {
            log.debug("No se encontraron ofertas para la subasta ID: {}", idSubasta);
        }

        return oferta.map(this::mapToResponseDTO);
    }

    //Buscar ofertas de un usuario específico
    public List<OfertaResponseDTO> obtenerOfertasPorUsuario(Long idUsuario) {
        log.info("Buscando ofertas del usuario ID: {}", idUsuario);

        List<Oferta> ofertas = ofertaRepository.findByIdUsuario(idUsuario);
        log.debug("El usuario " + idUsuario + "tiene" + ofertas.size() + "oferta registradas.");

        return ofertas.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    //Buscar ofertas que superen un monto en una subasta
    public List<OfertaResponseDTO> obtenerOfertasMayoresA(Long idSubasta, BigDecimal monto) {
        log.info("Buscando ofertas en subasta {} superiores a : {}", idSubasta, monto);

        List<Oferta> ofertas = ofertaRepository.findByIdSubastaAndMontoGreaterThan(idSubasta,monto);
        log.debug("Se encontraron" + ofertas.size() + " ofertas que superan el monto" + monto + ".");

        return ofertas.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    //Contar ofertas de una subasta
    public Long contarOfertasPorSubasta(Long idSubasta) {
        log.info("Contando ofertas para la subasa ID: {}", idSubasta);

        Long total = ofertaRepository.countByIdSubasta(idSubasta);
        log.debug("Total de ofertas para la subasta {}: {}", idSubasta, total);

        return total;
    }

    //Verificar si un usuario ya participó
    public boolean verificarSiUsuarioOferto(Long idUsuario, Long idSubasta) {
        log.info("Verificando participacón del usuario {} en subasta {}", idUsuario,idSubasta);

        boolean existe = ofertaRepository.existsByIdUsuarioAndIdSubasta(idUsuario, idSubasta);
        log.debug("¿Usuario" + idUsuario + "ya participó?:" + existe);

        return existe;
    }

    //Obtener el TOP 3 de ofertas
    public List<OfertaResponseDTO> obtenerTop3Subasta(Long idSubasta) {
        log.info("Recuperando el top 3 de ofertas para la subasta ID: {}" + idSubasta + ".");

        List<Oferta> topOfertas = ofertaRepository.findTop3ByIdSubastaOrderByMontoDesc(idSubasta);
        log.debug("TOP 3 recuperado. Cantidad de ofertas obtenidas: " + topOfertas.size() + ".");

        return topOfertas.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
}
