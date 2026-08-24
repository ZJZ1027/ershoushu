package com.basepro.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.std.StdScalarSerializer;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * LocalDateTime 与前端之间统一用毫秒时间戳交换：前端 dayjs 可直接解析，也避免时区歧义。
 */
@Configuration
public class JacksonConfig {

    @Bean
    public JacksonModule timestampModule() {
        SimpleModule module = new SimpleModule("timestamp");
        module.addSerializer(LocalDateTime.class, new EpochMillisSerializer());
        module.addDeserializer(LocalDateTime.class, new EpochMillisDeserializer());
        return module;
    }

    static class EpochMillisSerializer extends StdScalarSerializer<LocalDateTime> {

        EpochMillisSerializer() {
            super(LocalDateTime.class);
        }

        @Override
        public void serialize(LocalDateTime value, JsonGenerator gen, SerializationContext context)
                throws JacksonException {
            gen.writeNumber(value.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        }

    }

    static class EpochMillisDeserializer extends ValueDeserializer<LocalDateTime> {

        private static final DateTimeFormatter PATTERN = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        @Override
        public LocalDateTime deserialize(JsonParser parser, DeserializationContext context) throws JacksonException {
            String text = parser.getValueAsString();
            if (text == null || text.isBlank()) {
                return null;
            }
            // 兼容两种入参：毫秒时间戳、yyyy-MM-dd HH:mm:ss 或 ISO 字符串
            if (text.chars().allMatch(Character::isDigit)) {
                return LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(text)), ZoneId.systemDefault());
            }
            return text.contains("T") ? LocalDateTime.parse(text) : LocalDateTime.parse(text, PATTERN);
        }

    }

}
