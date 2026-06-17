package cl.sda1085.ofertas.controller;

import cl.sda1085.ofertas.dto.OfertaRequestDTO;
import cl.sda1085.ofertas.dto.OfertaResponseDTO;
import cl.sda1085.ofertas.service.OfertaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OfertaController.class)
@DisplayName("OfertaController – Tests Unitarios con HATEOAS.")
class OfertaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private OfertaService ofertaService;

    //Faker para emular datos comerciales realistas en las aserciones
    private final Faker faker = new Faker();

    private OfertaResponseDTO responseDTO;
    private OfertaRequestDTO requestDTO;
    private Long ofertaId;
    private Long subastaId;
    private Long usuarioId;

    @BeforeEach
    void setUp() {
        ofertaId = faker.number().numberBetween(1L, 1000L);
        subastaId = faker.number().numberBetween(1L, 500L);
        usuarioId = faker.number().numberBetween(1L, 500L);

        //Inicialización segura del DTO de respuesta (salida)
        responseDTO = new OfertaResponseDTO();
        responseDTO.setId(ofertaId);
        responseDTO.setMonto(BigDecimal.valueOf(faker.number().randomDouble(2, 5000, 1000000)));
        responseDTO.setFechaHora(LocalDateTime.now());
        responseDTO.setIdUsuario(usuarioId);
        responseDTO.setIdSubasta(subastaId);

        //Inicialización del DTO de petición (entrada)
        requestDTO = new OfertaRequestDTO();
        requestDTO.setMonto(responseDTO.getMonto());
        requestDTO.setIdUsuario(responseDTO.getIdUsuario());
        requestDTO.setIdSubasta(responseDTO.getIdSubasta());
    }


    //=========================
    //CRUD estándar
    //=========================

    //----------------------------------------------------------------------
    // GET --> /api/ofertas — listarTodasLasOfertas
    //----------------------------------------------------------------------
    @Test
    @DisplayName("GET /api/ofertas → 200 OK con el listado completo de ofertas.")
    void listarTodasLasOfertas_retornaHistorial() throws Exception {

        //GIVEN (ARRANGE)
        OfertaResponseDTO segundaOferta = new OfertaResponseDTO(
                faker.number().numberBetween(1001L, 2000L),
                BigDecimal.valueOf(1250000.00),
                LocalDateTime.now(),
                faker.number().numberBetween(1L, 100L),
                subastaId
        );
        given(ofertaService.obtenerTodas()).willReturn(List.of(responseDTO, segundaOferta));

        //WHEN & THEN (ACT & ASSERT)
        mockMvc.perform(get("/api/ofertas")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(ofertaId.intValue())))
                .andExpect(jsonPath("$[1].monto", is(1250000.00)));

        verify(ofertaService).obtenerTodas();
    }

    //----------------------------------------------------------------------
    //GET --> /api/ofertas/{id} — obtenerOfertaPorId
    //----------------------------------------------------------------------
    @Test
    @DisplayName("GET /api/ofertas/{id} → 200 OK e hipervínculos HAL cuando la oferta existe.")
    void obtenerOfertaPorId_cuandoExiste_retorna200ConLinks() throws Exception {

        //GIVEN (ARRANGE)
        given(ofertaService.obtenerPorId(ofertaId)).willReturn(Optional.of(responseDTO));

        //WHEN & THEN (ACT & ASSERT)
        mockMvc.perform(get("/api/ofertas/{id}", ofertaId)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(ofertaId.intValue())))
                .andExpect(jsonPath("$.monto", is(responseDTO.getMonto().doubleValue())))
                .andExpect(jsonPath("$._links.self.href", containsString("/api/ofertas/" + ofertaId)))
                .andExpect(jsonPath("$._links['lista-completa-ofertas'].href", containsString("/api/ofertas")));

        verify(ofertaService).obtenerPorId(ofertaId);
    }

    //----------------------------------------------------------------------
    //POST --> /api/ofertas — crearOferta
    //----------------------------------------------------------------------
    @Test
    @DisplayName("POST /api/ofertas → 201 Created con el payload persistido de la puja.")
    void crearOferta_conDatosValidos_retorna201() throws Exception {

        //GIVEN (ARRANGE)
        given(ofertaService.guardar(any(OfertaRequestDTO.class))).willReturn(responseDTO);

        //WHEN & THEN (ACT & ASSERT)
        mockMvc.perform(post("/api/ofertas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(ofertaId.intValue())))
                .andExpect(jsonPath("$.idUsuario", is(usuarioId.intValue())))
                .andExpect(jsonPath("$.idSubasta", is(subastaId.intValue())));

        verify(ofertaService).guardar(any(OfertaRequestDTO.class));
    }

    //----------------------------------------------------------------------
    //PUT --> /api/ofertas/{id} — actualizar
    //----------------------------------------------------------------------
    @Test
    @DisplayName("PUT /api/ofertas/{id} → 200 OK cuando la oferta se modifica exitosamente.")
    void actualizarOferta_existente_retorna200() throws Exception {

        //GIVEN (ARRANGE)
        BigDecimal nuevoMonto = BigDecimal.valueOf(1550000.75);
        responseDTO.setMonto(nuevoMonto);
        requestDTO.setMonto(nuevoMonto);

        given(ofertaService.actualizar(eq(ofertaId), any(OfertaRequestDTO.class))).willReturn(Optional.of(responseDTO));

        //WHEN & THEN (ACT & ASSERT)
        mockMvc.perform(put("/api/ofertas/{id}", ofertaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.monto", is(nuevoMonto.doubleValue())));

        verify(ofertaService).actualizar(eq(ofertaId), any(OfertaRequestDTO.class));
    }

    //----------------------------------------------------------------------
    //DELETE --> /api/ofertas/{id} — eliminar
    //----------------------------------------------------------------------
    @Test
    @DisplayName("DELETE /api/ofertas/{id} → 204 No Content al remover puja física.")
    void eliminarOferta_existente_retorna204() throws Exception {

        //GIVEN (ARRANGE)
        doNothing().when(ofertaService).eliminar(ofertaId);

        //WHEN & THEN (ACT & ASSERT)
        mockMvc.perform(delete("/api/ofertas/{id}", ofertaId))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(ofertaService).eliminar(ofertaId);
    }


    //=========================
    //CRUD personalizado
    //=========================

    //----------------------------------------------------------------------
    //GET --> /api/ofertas/usuario/{idUsuario} — obtenerPorUsuario
    //----------------------------------------------------------------------
    @Test
    @DisplayName("GET /api/ofertas/usuario/{idUsuario} → 200 OK con el listado de pujas del cliente.")
    void obtenerPorUsuario_conResultados_retornaLista() throws Exception {

        //GIVEN (ARRANGE)
        given(ofertaService.obtenerOfertasPorUsuario(usuarioId)).willReturn(List.of(responseDTO));

        //WHEN & THEN (ACT & ASSERT)
        mockMvc.perform(get("/api/ofertas/usuario/{idUsuario}", usuarioId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].idUsuario", is(usuarioId.intValue())));

        verify(ofertaService).obtenerOfertasPorUsuario(usuarioId);
    }

    //----------------------------------------------------------------------
    //GET --> /api/ofertas/subasta/{idSubasta}/total — contarOfertas
    //----------------------------------------------------------------------
    @Test
    @DisplayName("GET /api/ofertas/subasta/{idSubasta}/total → 200 OK con el conteo de pujas.")
    void contarOfertas_retornaEscalarLong() throws Exception {

        //GIVEN (ARRANGE)
        Long totalEsperado = 24L;
        given(ofertaService.contarOfertasPorSubasta(subastaId)).willReturn(totalEsperado);

        //WHEN & THEN (ACT & ASSERT)
        mockMvc.perform(get("/api/ofertas/subasta/{idSubasta}/total", subastaId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(totalEsperado.toString()));

        verify(ofertaService).contarOfertasPorSubasta(subastaId);
    }

    //----------------------------------------------------------------------
    //GET --> /api/ofertas/subasta/{idSubasta}/mayores-a — obtenerOfertasMayores
    //----------------------------------------------------------------------
    @Test
    @DisplayName("GET /api/ofertas/subasta/{idSubasta}/mayores-a → Filtro de montos mínimos.")
    void obtenerOfertasMayores_filtradoExitoso() throws Exception {

        //GIVEN (ARRANGE)
        BigDecimal montoFiltro = BigDecimal.valueOf(50000);
        given(ofertaService.obtenerOfertasMayoresA(subastaId, montoFiltro)).willReturn(List.of(responseDTO));

        //WHEN & THEN (ACT & ASSERT)
        mockMvc.perform(get("/api/ofertas/subasta/{idSubasta}/mayores-a", subastaId)
                        .param("monto", montoFiltro.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idSubasta", is(subastaId.intValue())));

        verify(ofertaService).obtenerOfertasMayoresA(subastaId, montoFiltro);
    }

    //----------------------------------------------------------------------
    //GET --> /api/ofertas/verificar/usuario/{idUsuario}/subasta/{idSubasta}
    //----------------------------------------------------------------------
    @Test
    @DisplayName("GET /api/ofertas/verificar/... → Retorna booleano de participación activa.")
    void verificarParticipacion_retornaTrue() throws Exception {

        //GIVEN (ARRANGE)
        given(ofertaService.verificarSiUsuarioOferto(usuarioId, subastaId)).willReturn(true);

        //WHEN & THEN (ACT & ASSERT)
        mockMvc.perform(get("/api/ofertas/verificar/usuario/{idUsuario}/subasta/{idSubasta}", usuarioId, subastaId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(ofertaService).verificarSiUsuarioOferto(usuarioId, subastaId);
    }

    //----------------------------------------------------------------------
    //GET --> /api/ofertas/subasta/{idSubasta}/top3 — obtenerTop3
    //----------------------------------------------------------------------
    @Test
    @DisplayName("GET /api/ofertas/subasta/{idSubasta}/top3 → Retorna podio de las mejores pujas.")
    void obtenerTop3_retornaMaximoTresElementos() throws Exception {

        //GIVEN (ARRANGE)
        given(ofertaService.obtenerTop3Subasta(subastaId)).willReturn(List.of(responseDTO));

        //WHEN & THEN (ACT & ASSERT)
        mockMvc.perform(get("/api/ofertas/subasta/{idSubasta}/top3", subastaId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(lessThanOrEqualTo(3))));

        verify(ofertaService).obtenerTop3Subasta(subastaId);
    }
}
