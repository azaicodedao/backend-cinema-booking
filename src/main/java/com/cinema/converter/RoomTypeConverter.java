package com.cinema.converter;

import com.cinema.enums.RoomType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class RoomTypeConverter implements AttributeConverter<RoomType, String> {

    @Override
    public String convertToDatabaseColumn(RoomType attribute) {
        if (attribute == null) return null;
        
        // Remove the leading underscore for DB storage if it exists (e.g. _2D -> 2D)
        String name = attribute.name();
        if (name.startsWith("_")) {
            return name.substring(1);
        }
        return name;
    }

    @Override
    public RoomType convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) return null;

        // Add leading underscore for matching Enum if it starts with a digit
        if (Character.isDigit(dbData.charAt(0))) {
            return RoomType.valueOf("_" + dbData);
        }
        
        try {
            return RoomType.valueOf(dbData);
        } catch (IllegalArgumentException e) {
            // Log or handle unexpected values
            return null;
        }
    }
}
