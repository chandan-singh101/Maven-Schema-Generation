// package com.example;

// import java.io.File;
// import java.util.ArrayList;
// import java.util.List;

// import org.odftoolkit.simple.SpreadsheetDocument;
// import org.odftoolkit.simple.table.Table;

// import com.fasterxml.jackson.databind.JsonNode;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.fasterxml.jackson.databind.node.ObjectNode;

// public class JsonToSchemaConverter {

//     public static void main(String[] args) throws Exception {
//         ObjectMapper mapper = new ObjectMapper();
//         SpreadsheetDocument doc = SpreadsheetDocument.loadDocument(new File("input.ods"));
//         Table sheet = doc.getSheetByIndex(0);

//         for (int i = 0; i < sheet.getRowCount(); i++) {
//             String raw = sheet.getRowByIndex(i).getCellByIndex(1).getDisplayText();
//             String payload = normalizeQuotes(raw);
//             if (payload == null || payload.isBlank()) continue;

//             JsonNode root = mapper.readTree(payload);
//             ObjectNode schema = generateSchema(root, mapper);
//             System.out.println(schema.toPrettyString());
//         }
//     }

//     private static ObjectNode generateSchema(JsonNode node, ObjectMapper mapper) {
//         ObjectNode root = mapper.createObjectNode()
//             .put("$schema", "https://json-schema.org/draft/2020-12/schema")
//             .put("type", "object")
//             .put("additionalProperties", false);

//         root.set("properties", buildProperties(node, mapper));
//         root.set("required", mapper.valueToTree(listRequired(node)));
//         return root;
//     }

//     private static ObjectNode buildProperties(JsonNode node, ObjectMapper mapper) {
//         ObjectNode props = mapper.createObjectNode();
//         if (!node.isObject()) return props;
    
//         node.fields().forEachRemaining(entry -> {
//             String key = entry.getKey();
//             JsonNode val = entry.getValue();
//             ObjectNode def = mapper.createObjectNode();
    
//             if (val.isNull() || (val.isTextual() && val.asText().isEmpty())) {
//                 def.put("type", mapper.createArrayNode().add("string").add("null"));
//                 def.put("pattern", "^$");
//                 def.put("description", "Must be either null or an empty string");
//             } else if (val.isTextual()) {
//                 def.put("type", "string");
//                 String text = val.asText();
//                 String pattern;
//                 if (text.matches("^\\d+$")) {
//                     pattern = "^\\d{" + text.length() + "}$";
//                 } else if (text.matches("^[a-zA-Z0-9]+$")) {
//                     pattern = "^[a-zA-Z0-9]{" + text.length() + "}$";
//                 } else {
//                     pattern = "^.{" + text.length() + "}$";
//                 }
//                 def.put("pattern", pattern);
//             } else if (val.isInt() || val.isLong()) {
//                 def.put("type", "integer");
//             } else if (val.isDouble() || val.isFloat()) {
//                 def.put("type", "number");
//             } else if (val.isBoolean()) {
//                 def.put("type", "boolean");
//             } else if (val.isObject()) {
//                 def.put("type", "object");
//                 def.set("properties", buildProperties(val, mapper));
//                 def.set("required", mapper.valueToTree(listRequired(val)));
//             } else if (val.isArray()) {
//                 def.put("type", "array");
//                 if (val.size() > 0) {
//                     JsonNode firstElem = val.get(0);
//                     if (firstElem.isObject()) {
//                         def.set("items", generateSchema(firstElem, mapper));
//                     } else {
//                         ObjectNode itemSchema = mapper.createObjectNode();
//                         if (firstElem.isTextual()) {
//                             String text = firstElem.asText();
//                             if (text.matches("^\\d+$")) {
//                                 itemSchema.put("type", "string");
//                                 itemSchema.put("pattern", "^\\d{" + text.length() + "}$");
//                             } else if (text.matches("^[a-zA-Z0-9]+$")) {
//                                 itemSchema.put("type", "string");
//                                 itemSchema.put("pattern", "^[a-zA-Z0-9]{" + text.length() + "}$");
//                             } else {
//                                 itemSchema.put("type", "string");
//                                 itemSchema.put("pattern", "^.{" + text.length() + "}$");
//                             }
//                         } else if (firstElem.isInt() || firstElem.isLong()) {
//                             itemSchema.put("type", "integer");
//                         } else if (firstElem.isDouble() || firstElem.isFloat()) {
//                             itemSchema.put("type", "number");
//                         } else if (firstElem.isBoolean()) {
//                             itemSchema.put("type", "boolean");
//                         } else {
//                             itemSchema.put("type", "string");
//                         }
//                         def.set("items", itemSchema);
//                     }
//                 } else {
//                     def.set("items", mapper.createObjectNode().put("type", "string"));
//                     def.put("description", "Array is empty, defaulting items to string type");
//                 }
//             } else {
//                 def.put("type", "string");
//             }
    
//             props.set(key, def);
//         });
    
//         return props;
//     }
    
    
//     private static List<String> listRequired(JsonNode node) {
//         List<String> req = new ArrayList<>();
//         node.fieldNames().forEachRemaining(req::add);
//         return req;
//     }

//     private static String normalizeQuotes(String s) {
//         if (s == null) return null;
//         return s.replace('\u201C', '"')
//                 .replace('\u201D', '"')
//                 .replace('“', '"')
//                 .replace('”', '"');
//     }
// }