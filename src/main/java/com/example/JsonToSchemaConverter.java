package com.example;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.odftoolkit.simple.SpreadsheetDocument;
import org.odftoolkit.simple.table.Table;

import com.example.SchemaRules.Rule;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonToSchemaConverter {

    public static void main(String[] args) throws Exception {
        SchemaRules rules = new SchemaRules();
        ObjectMapper mapper = new ObjectMapper();

        SpreadsheetDocument doc =
            SpreadsheetDocument.loadDocument(new File("input.ods"));
        Table sheet = doc.getSheetByIndex(0);

        for (int i = 0; i < sheet.getRowCount(); i++) {
            String raw = sheet.getRowByIndex(i)
                  .getCellByIndex(1)
                  .getDisplayText();
String payload = normalizeQuotes(raw);
if (payload == null || payload.isBlank()) continue;

JsonNode root = mapper.readTree(payload);
            ObjectNode schema = generateSchema(root, mapper, rules);
            System.out.println(schema.toPrettyString());
        }
    }

    private static ObjectNode generateSchema(JsonNode node,
                                             ObjectMapper mapper,
                                             SchemaRules rules)
    {
        ObjectNode root = mapper.createObjectNode()
            .put("$schema", "http://json-schema.org/draft-04/schema#")
            .put("type", "object");

        root.set("properties", buildProperties(node, mapper, rules));
        root.set("required" ,
            mapper.valueToTree(listRequired(node)));
        return root;
    }

    private static ObjectNode buildProperties(JsonNode node,
                                              ObjectMapper mapper,
                                              SchemaRules rules)
    {
        ObjectNode props = mapper.createObjectNode();
        if (!node.isObject()) return props;

        for (Iterator<Map.Entry<String, JsonNode>> it = node.fields();
             it.hasNext();)
        {
            Map.Entry<String, JsonNode> entry = it.next();
            String key = entry.getKey();
            JsonNode val = entry.getValue();

            ObjectNode def = props.putObject(key);
            Optional<Rule> maybe = rules.getRule(key);
            if (maybe.isPresent() && maybe.get().enums != null) {
                def.put("type", maybe.get().type);
                ArrayNode arr = def.putArray("enum");
                for (String e : maybe.get().enums) {
                    arr.add(e);
                }
            } else {
                String type = maybe.map(r -> r.type).orElse("string");
                def.put("type", type);
                maybe.map(r -> r.pattern)
                     .ifPresent(p -> def.put("pattern", p));
            }

            // Nested object support
            if (val.isObject()) {
                def.put("type", "object");
                def.set("properties",
                    buildProperties(val, mapper, rules));
                def.set("required",
                    mapper.valueToTree(listRequired(val)));
            }
        }
        return props;
    }

    private static List<String> listRequired(JsonNode node) {
        List<String> req = new ArrayList<>();
        node.fieldNames().forEachRemaining(req::add);
        return req;
    }

/**
 * Replace curly “smart” quotes with straight ASCII quotes so Jackson can parse JSON.
 */
private static String normalizeQuotes(String s) {
    if (s == null) return null;
    return s
        .replace('\u201C', '"')
        .replace('\u201D', '"')
        .replace('“', '"')
        .replace('”', '"');
}

}
