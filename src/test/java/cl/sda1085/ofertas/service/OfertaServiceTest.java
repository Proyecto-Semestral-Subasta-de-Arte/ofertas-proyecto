package cl.sda1085.ofertas.service;

import cl.sda1085.ofertas.dto.OfertaResponseDTO;
import cl.sda1085.ofertas.model.Oferta;
import cl.sda1085.ofertas.repository.OfertaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OfertaServiceTest {

    @Mock
    private OfertaRepository ofertaRepository;

    @InjectMocks
    private OfertaService ofertaService;

    //Elementos del DataInitializer
    private Oferta oferta1Mock;
    private Oferta oferta2Mock;
    private Oferta oferta3Mock;
    private Oferta oferta14Mock;

    @BeforeEach
    void setUp() {

        //Asignamos IDs ficticios (1, 2, 3, 14) para simular la BD de XAMPP de forma aislada
        oferta1Mock = new Oferta(1L, new BigDecimal("160000"), LocalDateTime.now().minusHours(5), 5L, 1L);
        oferta2Mock = new Oferta(2L, new BigDecimal("175000"), LocalDateTime.now().minusHours(2), 6L, 1L);
        oferta3Mock = new Oferta(3L, new BigDecimal("310000"), LocalDateTime.now().minusDays(1), 7L, 2L);
        oferta14Mock = new Oferta(14L, new BigDecimal("850000.00"), LocalDateTime.now().minusDays(3), 11L, 14L);}

    @Test
    @DisplayName("Debería retornar DTO válido para la oferta 1.")
    void obtenerOferta1Exitoso() {

        //ARRANGE
        when(ofertaRepository.findById(1L)).thenReturn(Optional.of(oferta1Mock));

        //ACT
        OfertaResponseDTO resultado = ofertaService.obtenerPorId(1L).get();

        //ASSERT
        assertNotNull(resultado);
        assertEquals(new BigDecimal("160000"), resultado.getMonto());
        assertEquals(5L, resultado.getIdUsuario());
        assertEquals(1L, resultado.getIdSubasta());
        verify(ofertaRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Debería retornar DTO válido para la oferta 2.")
    void obtenerOferta2Exitoso() {

        //ARRANGE
        when(ofertaRepository.findById(2L)).thenReturn(Optional.of(oferta2Mock));

        //ACT
        OfertaResponseDTO resultado = ofertaService.obtenerPorId(2L).get();

        //ASSERT
        assertNotNull(resultado);
        assertEquals(new BigDecimal("175000"), resultado.getMonto());
        assertEquals(6L, resultado.getIdUsuario());
        verify(ofertaRepository, times(1)).findById(2L);
    }

    @Test
    @DisplayName("Debería retornar DTO válido para la oferta 3.")
    void obtenerOferta3Exitoso() {

        //ARRANGE
        when(ofertaRepository.findById(3L)).thenReturn(Optional.of(oferta3Mock));

        //ACT
        OfertaResponseDTO resultado = ofertaService.obtenerPorId(3L).get();

        //ASSERT
        assertNotNull(resultado);
        assertEquals(new BigDecimal("310000"), resultado.getMonto());
        assertEquals(7L, resultado.getIdUsuario());
        assertEquals(2L, resultado.getIdSubasta());
        verify(ofertaRepository, times(1)).findById(3L);
    }

    @Test
    @DisplayName("Debería retornar DTO válido para la oferta 14.")
    void obtenerOferta14Exitoso() {

        //ARRANGE
        when(ofertaRepository.findById(14L)).thenReturn(Optional.of(oferta14Mock));

        //ACT
        OfertaResponseDTO resultado = ofertaService.obtenerPorId(14L).get();

        //ASSERT
        assertNotNull(resultado);
        assertEquals(new BigDecimal("850000.00"), resultado.getMonto());
        assertEquals(14L, resultado.getIdSubasta());
        verify(ofertaRepository, times(1)).findById(14L);
    }

    @Test
    @DisplayName("Debería retornar un Optional vacío cuando la oferta no existe.")
    void obtenerOfertaInexistente() {

        //ARRANGE
        when(ofertaRepository.findById(99L)).thenReturn(Optional.empty());

        //ACT
        Optional<cl.sda1085.ofertas.dto.OfertaResponseDTO> resultado = ofertaService.obtenerPorId(99L);

        //ASSERT
        assertNotNull(resultado, "El objeto de retorno no debería ser nulo.");
        assertTrue(resultado.isEmpty(), "El 'optional' debería estar vacío para un ID inexistente.");

        verify(ofertaRepository, times(1)).findById(99L);
    }
}
