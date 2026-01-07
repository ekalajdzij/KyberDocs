package com.kyberdocs.docs.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class HexConverter implements AttributeConverter<byte[], String> {

    @Override
    public String convertToDatabaseColumn(byte[] attribute) {
        if (attribute == null) return null;
        return bytesToHex(attribute);
    }

    @Override
    public byte[] convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        return hexToBytes(dbData);
    }

    // Static helpers so Services can use them for Python responses too
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static byte[] hexToBytes(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}