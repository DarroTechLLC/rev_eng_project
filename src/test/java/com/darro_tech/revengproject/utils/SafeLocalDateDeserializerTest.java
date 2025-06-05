package com.darro_tech.revengproject.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

class SafeLocalDateDeserializerTest {

    private SafeLocalDateDeserializer deserializer;

    @Mock
    private JsonParser jsonParser;

    @Mock
    private DeserializationContext context;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        deserializer = new SafeLocalDateDeserializer();
    }

    @Test
    void deserialize_WithValidISODate_ShouldReturnCorrectDate() throws IOException {
        // Given
        when(jsonParser.getText()).thenReturn("2023-10-01");

        // When
        LocalDate result = deserializer.deserialize(jsonParser, context);

        // Then
        assertNotNull(result);
        assertEquals(2023, result.getYear());
        assertEquals(10, result.getMonthValue());
        assertEquals(1, result.getDayOfMonth());
    }

    @Test
    void deserialize_WithSingleDigitYear_ShouldFixAndReturnDate() throws IOException {
        // Given
        when(jsonParser.getText()).thenReturn("2-10-01");

        // When
        LocalDate result = deserializer.deserialize(jsonParser, context);

        // Then
        assertNotNull(result);
        assertEquals(2002, result.getYear());
        assertEquals(10, result.getMonthValue());
        assertEquals(1, result.getDayOfMonth());
    }

    @Test
    void deserialize_WithDoubleDigitYear_ShouldFixAndReturnDate() throws IOException {
        // Given
        when(jsonParser.getText()).thenReturn("20-10-01");

        // When
        LocalDate result = deserializer.deserialize(jsonParser, context);

        // Then
        assertNotNull(result);
        assertEquals(2020, result.getYear());
        assertEquals(10, result.getMonthValue());
        assertEquals(1, result.getDayOfMonth());
    }

    @Test
    void deserialize_WithTripleDigitYear_ShouldFixAndReturnDate() throws IOException {
        // Given
        when(jsonParser.getText()).thenReturn("202-10-01");

        // When
        LocalDate result = deserializer.deserialize(jsonParser, context);

        // Then
        assertNotNull(result);
        assertEquals(2202, result.getYear());
        assertEquals(10, result.getMonthValue());
        assertEquals(1, result.getDayOfMonth());
    }

    @Test
    void deserialize_WithNaNValues_ShouldReturnCurrentDate() throws IOException {
        // Given
        when(jsonParser.getText()).thenReturn("NaN-NaN-01");

        // When
        LocalDate result = deserializer.deserialize(jsonParser, context);

        // Then
        assertNotNull(result);
        // Should be today's date
        assertEquals(LocalDate.now().getYear(), result.getYear());
        assertEquals(LocalDate.now().getMonthValue(), result.getMonthValue());
        assertEquals(LocalDate.now().getDayOfMonth(), result.getDayOfMonth());
    }

    @Test
    void deserialize_WithEmptyString_ShouldReturnCurrentDate() throws IOException {
        // Given
        when(jsonParser.getText()).thenReturn("");

        // When
        LocalDate result = deserializer.deserialize(jsonParser, context);

        // Then
        assertNotNull(result);
        // Should be today's date
        assertEquals(LocalDate.now().getYear(), result.getYear());
        assertEquals(LocalDate.now().getMonthValue(), result.getMonthValue());
        assertEquals(LocalDate.now().getDayOfMonth(), result.getDayOfMonth());
    }

    @Test
    void deserialize_WithNullString_ShouldReturnCurrentDate() throws IOException {
        // Given
        when(jsonParser.getText()).thenReturn(null);

        // When
        LocalDate result = deserializer.deserialize(jsonParser, context);

        // Then
        assertNotNull(result);
        // Should be today's date
        assertEquals(LocalDate.now().getYear(), result.getYear());
        assertEquals(LocalDate.now().getMonthValue(), result.getMonthValue());
        assertEquals(LocalDate.now().getDayOfMonth(), result.getDayOfMonth());
    }

    @Test
    void deserialize_WithCompletelyInvalidFormat_ShouldReturnCurrentDate() throws IOException {
        // Given
        when(jsonParser.getText()).thenReturn("not-a-date");

        // When
        LocalDate result = deserializer.deserialize(jsonParser, context);

        // Then
        assertNotNull(result);
        // Should be today's date
        assertEquals(LocalDate.now().getYear(), result.getYear());
        assertEquals(LocalDate.now().getMonthValue(), result.getMonthValue());
        assertEquals(LocalDate.now().getDayOfMonth(), result.getDayOfMonth());
    }

    @Test
    void deserialize_WithFiveDigitYear_ShouldFixAndReturnDate() throws IOException {
        // Given
        when(jsonParser.getText()).thenReturn("20001-02-01");

        // When
        LocalDate result = deserializer.deserialize(jsonParser, context);

        // Then
        assertNotNull(result);
        assertEquals(2000, result.getYear());
        assertEquals(2, result.getMonthValue());
        assertEquals(1, result.getDayOfMonth());
    }
}
