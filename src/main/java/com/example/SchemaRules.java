// package com.example;

// import java.io.InputStream;
// import java.util.*;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.fasterxml.jackson.core.type.TypeReference;

// public class SchemaRules {
//     public static class Rule {
//         public String type;
//         public String pattern;
//         public List<String> enums;
//     }

//     private final Map<String, Rule> rules = new HashMap<>();
//     private final ObjectMapper mapper = new ObjectMapper();

//     public SchemaRules() throws Exception {
//         Properties props = new Properties();
//         try (InputStream in = getClass()
//                 .getResourceAsStream("/application.properties")) {
//             props.load(in);
//         }

//         for (String rawKeys : props.stringPropertyNames()) {
//             // rawKeys: e.g. "UniqueId,CustomerId,..."
//             String[] aliases = rawKeys.split("\\s*,\\s*");
//             String value = props.getProperty(rawKeys);
//             // value: e.g. "string,^[0-9]{10}$" or "string,enum,[...]"
//             String[] parts = value.split("\\s*,\\s*", 3);
//             Rule rule = new Rule();
//             rule.type = parts[0];
//             if ("enum".equalsIgnoreCase(parts[1])) {
//                 // parts[2] is a JSON array literal
//                 rule.enums = mapper.readValue(
//                     parts[2], new TypeReference<List<String>>() {});
//             } else {
//                 rule.pattern = parts[1];
//             }
//             // Register the same rule under each alias
//             for (String alias : aliases) {
//                 rules.put(alias, rule);
//             }
//         }
//     }

//     /** Returns the rule for exactly this key, or empty if none. */
//     public Optional<Rule> getRule(String key) {
//         return Optional.ofNullable(rules.get(key));
//     }
// }
