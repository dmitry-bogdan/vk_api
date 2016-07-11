package com.example.service;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Description:
 * Creation date: 11.07.2016 11:40
 *
 * @author sks
 */
@Converter
public class LocalDateTimeConverter  implements AttributeConverter<LocalDateTime, Timestamp> {

    @Override
    public java.sql.Timestamp convertToDatabaseColumn(LocalDateTime entityValue) {
        if (entityValue != null) {
            return java.sql.Timestamp.valueOf(entityValue);
        }
        return null;
    }

    @Override
    public LocalDateTime convertToEntityAttribute(java.sql.Timestamp databaseValue) {
        if (databaseValue != null) {
            return databaseValue.toLocalDateTime();
        }
        return null;
    }

}
