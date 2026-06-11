package cl.sda1085.ofertas.repository;

import cl.sda1085.ofertas.model.Oferta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface OfertaRepository extends JpaRepository<Oferta, Long> {

    //Obtener la oferta más alta de una subasta (El posible ganador)
    Optional<Oferta> findFirstByIdSubastaOrderByMontoDesc(Long idSubasta);

    //Buscar todas las ofertas realizadas por un usuario específico
    List<Oferta> findByIdUsuario(Long idUsuario);

    //Contar cuántas ofertas se han realizado en una subasta
    Long countByIdSubasta(Long idSubasta);

    //Buscar ofertas que superen un monto determinado en una subasta específica
    List<Oferta> findByIdSubastaAndMontoGreaterThan(Long idSubasta, BigDecimal monto);

    //Verificar si un usuario ya ha realizado alguna oferta en una subasta
    boolean existsByIdUsuarioAndIdSubasta(Long idUsuario, Long idSubasta);

    //Obtener las 3 ofertas más altas de una subasta
    List<Oferta> findTop3ByIdSubastaOrderByMontoDesc(Long idSubasta);
}
