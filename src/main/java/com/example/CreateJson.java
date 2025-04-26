package com.example;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Converts raw JSON payloads into JSON Schema definitions (draft-2020-12).
 */
public class CreateJson {

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Generate a full JSON-Schema (draft-2020-12) for the given payload.
     * @param rawJson the example JSON payload
     * @return a pretty-printed JSON Schema string
     */
    public String toSchema(String rawJson) throws Exception {
        JsonNode root = mapper.readTree(rawJson);
        ObjectNode schema = mapper.createObjectNode()
            .put("$schema", "https://json-schema.org/draft/2020-12/schema")
            .put("type", "object")
            .put("additionalProperties", false);

        schema.set("properties", buildProperties(root));
        schema.set("required", mapper.valueToTree(listRequired(root)));
        return mapper.writerWithDefaultPrettyPrinter()
                     .writeValueAsString(schema);
    }

    /**
     * Recursively build the "properties" section of the JSON Schema.
     */
    private ObjectNode buildProperties(JsonNode node) {
        ObjectNode props = mapper.createObjectNode();
        if (!node.isObject()) return props;

        node.fields().forEachRemaining(entry -> {
            String key = entry.getKey();
            JsonNode val = entry.getValue();
            ObjectNode def = mapper.createObjectNode();

            if (val.isNull() || (val.isTextual() && val.asText().isEmpty())) {
                // null or empty string
                ArrayNode types = def.putArray("type");
                types.add("string").add("null");
                def.put("pattern", "^$");
                def.put("description", "Must be either null or an empty string");

            } else if (val.isTextual()) {
                String text = val.asText();
                def.put("type", "string");
                String pattern;
                if (text.matches("^\\d+$")) {
                    // all digits
                    pattern = "^\\d{" + text.length() + "}$";
                } else if (text.matches("^[a-zA-Z0-9]+$")) {
                    // alphanumeric
                    pattern = "^[a-zA-Z0-9]{" + text.length() + "}$";
                } else {
                    // any chars, fixed length
                    pattern = "^.{" + text.length() + "}$";
                }
                def.put("pattern", pattern);

            } else if (val.isInt() || val.isLong()) {
                def.put("type", "integer");

            } else if (val.isDouble() || val.isFloat()) {
                def.put("type", "number");

            } else if (val.isBoolean()) {
                def.put("type", "boolean");

            } else if (val.isObject()) {
                // nested object
                def.put("type", "object");
                def.set("properties", buildProperties(val));
                def.set("required", mapper.valueToTree(listRequired(val)));

            } else if (val.isArray()) {
                // array handling
                def.put("type", "array");
                if (val.size() > 0) {
                    JsonNode firstElem = val.get(0);
                    if (firstElem.isObject()) {
                        // array of objects → generate subschema
                        try {
                            def.set("items", mapper.readTree(toSchema(firstElem.toString())));
                        } catch (JsonMappingException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (JsonProcessingException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    } else {
                        // array of primitives → infer schema for first element
                        ObjectNode itemSchema = mapper.createObjectNode();
                        if (firstElem.isTextual()) {
                            String text = firstElem.asText();
                            itemSchema.put("type", "string");
                            String pat;
                            if (text.matches("^\\d+$")) pat = "^\\d{" + text.length() + "}$";
                            else if (text.matches("^[a-zA-Z0-9]+$")) pat = "^[a-zA-Z0-9]{" + text.length() + "}$";
                            else pat = "^.{" + text.length() + "}$";
                            itemSchema.put("pattern", pat);
                        } else if (firstElem.isInt() || firstElem.isLong()) {
                            itemSchema.put("type", "integer");
                        } else if (firstElem.isDouble() || firstElem.isFloat()) {
                            itemSchema.put("type", "number");
                        } else if (firstElem.isBoolean()) {
                            itemSchema.put("type", "boolean");
                        } else {
                            itemSchema.put("type", "string");
                        }
                        def.set("items", itemSchema);
                    }
                } else {
                    // empty array → default to string items
                    def.set("items", mapper.createObjectNode().put("type", "string"));
                    def.put("description", "Array is empty, defaulting items to string type");
                }

            } else {
                // fallback
                def.put("type", "string");
            }

            props.set(key, def);
        });

        return props;
    }

    /**
     * List all field names (for "required").
     */
    private List<String> listRequired(JsonNode node) {
        List<String> req = new ArrayList<>();
        node.fieldNames().forEachRemaining(req::add);
        return req;
    }
}
