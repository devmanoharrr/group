package kf7014.crowdsourceddata.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RestExceptionHandlerTest {

    private RestExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new RestExceptionHandler();
    }

    @Test
    void handleIllegalArgument_withMessage_returnsBadRequest() {
        IllegalArgumentException exception = new IllegalArgumentException("Postcode is required");

        ResponseEntity<Map<String, Object>> response = handler.handleIllegalArgument(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Postcode is required", response.getBody().get("error"));
    }

    @Test
    void handleIllegalArgument_withNullMessage_returnsBadRequest() {
        IllegalArgumentException exception = new IllegalArgumentException((String) null);

        ResponseEntity<Map<String, Object>> response = handler.handleIllegalArgument(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertNull(body.get("error"));
    }

    @Test
    void handleIllegalArgument_withEmptyMessage_returnsBadRequest() {
        IllegalArgumentException exception = new IllegalArgumentException("");

        ResponseEntity<Map<String, Object>> response = handler.handleIllegalArgument(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("", body.get("error"));
    }

    @Test
    void handleIllegalArgument_withLongMessage_returnsBadRequest() {
        String longMessage = "A".repeat(1000);
        IllegalArgumentException exception = new IllegalArgumentException(longMessage);

        ResponseEntity<Map<String, Object>> response = handler.handleIllegalArgument(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(longMessage, body.get("error"));
    }

    @Test
    void handleValidation_withMethodArgumentNotValidException_returnsBadRequest() {
        MethodArgumentNotValidException exception = createMethodArgumentNotValidException();

        ResponseEntity<Map<String, Object>> response = handler.handleValidation(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("Validation failed", body.get("error"));
    }

    @Test
    void handleValidation_withNullException_handlesGracefully() {
        // This test verifies the handler doesn't crash with null
        // In practice, Spring would not pass null, but we test defensive coding
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        when(exception.getMessage()).thenReturn(null);

        ResponseEntity<Map<String, Object>> response = handler.handleValidation(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("Validation failed", body.get("error"));
    }

    @Test
    void handleIllegalArgument_responseBodyIsMap() {
        IllegalArgumentException exception = new IllegalArgumentException("Test error");

        ResponseEntity<Map<String, Object>> response = handler.handleIllegalArgument(exception);

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertTrue(body instanceof Map);
        assertTrue(body.containsKey("error"));
    }

    @Test
    void handleValidation_responseBodyIsMap() {
        MethodArgumentNotValidException exception = createMethodArgumentNotValidException();

        ResponseEntity<Map<String, Object>> response = handler.handleValidation(exception);

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertTrue(body instanceof Map);
        assertTrue(body.containsKey("error"));
    }

    @Test
    void handleIllegalArgument_withSpecialCharacters_handlesCorrectly() {
        IllegalArgumentException exception = new IllegalArgumentException("Error: <script>alert('xss')</script>");

        ResponseEntity<Map<String, Object>> response = handler.handleIllegalArgument(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.get("error").toString().contains("script"));
    }

    @Test
    void handleIllegalArgument_withUnicodeCharacters_handlesCorrectly() {
        IllegalArgumentException exception = new IllegalArgumentException("Error: 测试中文");

        ResponseEntity<Map<String, Object>> response = handler.handleIllegalArgument(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.get("error").toString().contains("测试"));
    }

    private MethodArgumentNotValidException createMethodArgumentNotValidException() {
        Object target = new Object();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "target");
        bindingResult.addError(new FieldError("target", "field", "Field is required"));
        
        // Create a mock since we can't easily create MethodArgumentNotValidException without a MethodParameter
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);
        return exception;
    }
}

