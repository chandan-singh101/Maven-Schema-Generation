package com.example;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.odftoolkit.simple.SpreadsheetDocument;
import org.odftoolkit.simple.table.Row;
import org.odftoolkit.simple.table.Table;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonToSchemaConverter {

    public static void main(String[] args) throws Exception {
        // Load the .ods file from the project root
        File odsFile = new File("input.ods");
        SpreadsheetDocument document = SpreadsheetDocument.loadDocument(odsFile);
        Table sheet = document.getSheetByIndex(0);

        ObjectMapper mapper = new ObjectMapper();
        int rowCount = sheet.getRowCount();

        for (int i = 0; i < rowCount; i++) {
            Row row = sheet.getRowByIndex(i);
            String jsonPayload = row.getCellByIndex(1).getDisplayText();

            if (jsonPayload != null && !jsonPayload.isBlank()) {
                try {
                    JsonNode jsonNode = mapper.readTree(jsonPayload);
                    ObjectNode schema = generateSchema(jsonNode, mapper);
                    System.out.println("Schema for row " + (i + 1) + ":");
                    System.out.println(mapper
                        .writerWithDefaultPrettyPrinter()
                        .writeValueAsString(schema));
                } catch (Exception e) {
                    System.err.println("Error on row " + (i + 1) 
                        + ": " + e.getMessage());
                }
            }
        }
    }

    private static ObjectNode generateSchema(JsonNode node, ObjectMapper mapper) {
        ObjectNode root = mapper.createObjectNode();
        root.put("$schema", "http://json-schema.org/draft-04/schema#");
        root.put("type", "object");
        root.set("properties", buildProperties(node, mapper));
        root.set("required", mapper.valueToTree(listRequired(node)));
        return root;
    }

    private static ObjectNode buildProperties(JsonNode node, ObjectMapper mapper) {
        ObjectNode props = mapper.createObjectNode();
        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String key = entry.getKey();
                JsonNode val = entry.getValue();

                if (val.isTextual()) {
                    props.putObject(key).put("type", "string");
                } else if (val.isNumber()) {
                    props.putObject(key).put("type", "number");
                } else if (val.isBoolean()) {
                    props.putObject(key).put("type", "boolean");
                } else if (val.isObject()) {
                    ObjectNode nested = props.putObject(key)
                                             .put("type", "object");
                    nested.set("properties", buildProperties(val, mapper));
                    nested.set("required", 
                        mapper.valueToTree(listRequired(val)));
                }
                // Add support for arrays, nulls, etc., if needed
            }
        }
        return props;
    }

    private static List<String> listRequired(JsonNode node) {
        List<String> req = new ArrayList<>();
        node.fieldNames().forEachRemaining(req::add);
        return req;
    }
}
