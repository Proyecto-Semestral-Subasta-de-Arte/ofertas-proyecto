package cl.sda1085.ofertas.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    //Errores de validación (@Valid en los DTOs)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException ex) {
        log.error("Error de validación detectado: {}", ex.getMessage());
        Map<String, String> errores = new LinkedHashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                errores.put(error.getField(), error.getDefaultMessage()));  //Usamos merge por si un mismo campo tiene varios errores

        return ResponseEntity.badRequest().body(errores);
    }

    //Errores de lógica de negocio (RuntimeException personalizados)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {

        log.error("Ocurrió un error de negocio {} - Tipo: {}", ex.getMessage(), ex.getClass().getName());
        Map<String, String> error = new LinkedHashMap<>();

        //Agregamos una llave descriptiva para que el JSON sea más claro
        error.put("MENSAJE", ex.getMessage());
        error.put("TIPO", "Error de Negocio");

        return ResponseEntity.badRequest().body(error);
    }
}
