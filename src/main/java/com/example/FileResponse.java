package com.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileResponse {

    private static final String BAD_TEMPLATE = """
        {
          "priority": 2,
          "request": {
            "method": "POST",
            "urlPath": "/%s"
          },
          "response": {
            "status": 400,
            "jsonBody": {
              "status": 400,
              "error": "Bad Request"
            }
          }
        }
        """;

    private static final String SUCCESS_TEMPLATE = """
        {
          "priority": 1,
          "request": {
            "method": "POST",
            "urlPath": "/%s",
            "bodyPatterns": [
              { "matchesJsonSchema": %s }
            ]
          },
          "response": {
            "status": 200,
            "jsonBody": %s
          }
        }
        """;

    public void writeBad(Path mappingsDir, String folderName) throws IOException {
        String content = String.format(BAD_TEMPLATE, folderName);
        Files.writeString(mappingsDir.resolve("badResponse.json"), content);
    }

    public void writeSuccess(Path mappingsDir, String folderName, String schemaJson, String exampleResponse) throws IOException {
        String content = String.format(SUCCESS_TEMPLATE,
            folderName,
            // embed schema JSON inline (ensure it's a single line or properly escaped)
            schemaJson.replace("\n", " "),
            exampleResponse.trim()
        );
        Files.writeString(mappingsDir.resolve("successResponse.json"), content);
    }
}

