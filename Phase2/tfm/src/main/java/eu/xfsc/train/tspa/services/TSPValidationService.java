package eu.xfsc.train.tspa.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.util.Set;
import java.util.Locale;

@Service
public class TSPValidationService {
    
    private final JsonSchema schema;
    private final ObjectMapper mapper;

    public TSPValidationService() throws Exception {
        Locale.setDefault(Locale.ENGLISH);
        
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
        InputStream schemaStream = new ClassPathResource("schemas/tsp-schema.json").getInputStream();
        schema = factory.getSchema(schemaStream);
        mapper = new ObjectMapper();
    }

    public ValidationResult validateTSP(String jsonStr) {
        try {
            JsonNode jsonNode = mapper.readTree(jsonStr);
            Set<ValidationMessage> validationResult = schema.validate(jsonNode);
            
            if (validationResult.isEmpty()) {
                return new ValidationResult(true, null);
            } else {
                String errors = validationResult.stream()
                    .map(ValidationMessage::getMessage)
                    .reduce((a, b) -> a + "; " + b)
                    .orElse("");
                return new ValidationResult(false, errors);
            }
        } catch (Exception e) {
            return new ValidationResult(false, "Invalid JSON format: " + e.getMessage());
        }
    }

    public static class ValidationResult {
        private final boolean valid;
        private final String errors;

        public ValidationResult(boolean valid, String errors) {
            this.valid = valid;
            this.errors = errors;
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrors() {
            return errors;
        }
    }
} 