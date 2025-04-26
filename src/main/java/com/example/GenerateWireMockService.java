package com.example;


import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.example.model.ServiceDefinition;

public class GenerateWireMockService {

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Usage: java -jar json-schema-generator.jar <path-to-ods>");
            System.exit(1);
        }

        ReadOds reader = new ReadOds(args[0]);
        CreateJson schemaGen = new CreateJson();
        FileResponse writer = new FileResponse();

        List<ServiceDefinition> services = reader.readAll();
        for (ServiceDefinition svc : services) {
            Path baseDir     = Path.of(svc.getFolderName());
            Path mappingsDir = baseDir.resolve("mappings");
            Path filesDir    = baseDir.resolve("__files");

            // 1. Create directories
            Files.createDirectories(mappingsDir);
            Files.createDirectories(filesDir);

            // 2. Generate JSON-Schema from the request payload
            String schema = schemaGen.toSchema(svc.getRawRequestPayload());

            // 3. Write the two mapping files
            writer.writeBad(mappingsDir, svc.getFolderName());
            writer.writeSuccess(mappingsDir,
                                svc.getFolderName(),
                                schema,
                                svc.getRawResponsePayload());
        }

        System.out.println("WireMock services generated for " + services.size() + " entries.");
    }
}
