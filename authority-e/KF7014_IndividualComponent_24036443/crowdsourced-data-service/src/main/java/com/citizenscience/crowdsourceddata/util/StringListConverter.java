package com.citizenscience.crowdsourceddata.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Attribute converter that stores string lists as comma-separated TEXT.
 *
 * Allows SQLite to persist Base64 images without relying on unsupported BLOB
 * operations, while keeping entity getters exposed as lists.
 */
@Converter
public class StringListConverter implements AttributeConverter<List<String>, String> {

    /**
     * Serialises the list into a comma-separated string.
     *
     * @param list list of strings to persist
     * @return flattened comma-separated representation
     */
    @Override
    public String convertToDatabaseColumn(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        return list.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.joining(","));
    }

    /**
     * Rehydrates the comma-separated string into a list.
     *
     * @param data persisted representation from the database
     * @return mutable list of strings suitable for the entity
     */
    @Override
    public List<String> convertToEntityAttribute(String data) {
        if (data == null || data.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(data.split(",")));
    }
}
