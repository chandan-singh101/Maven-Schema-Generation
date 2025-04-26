package com.example;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.odftoolkit.simple.SpreadsheetDocument;
import org.odftoolkit.simple.table.Table;

import com.example.model.ServiceDefinition;

public class ReadOds {

    private final String odsPath;

    public ReadOds(String odsPath) {
        this.odsPath = odsPath;
    }

    public List<ServiceDefinition> readAll() throws Exception {
        SpreadsheetDocument doc = SpreadsheetDocument.loadDocument(new File(odsPath));
        Table sheet = doc.getSheetByIndex(0);
        List<ServiceDefinition> list = new ArrayList<>();

        for (int i = 0; i < sheet.getRowCount(); i++) {
            String folder = sheet.getRowByIndex(i).getCellByIndex(0).getDisplayText().trim();
            String rawRequest = sheet.getRowByIndex(i).getCellByIndex(1).getDisplayText();
            String rawResponse = sheet.getRowByIndex(i).getCellByIndex(2).getDisplayText();

            if (folder.isBlank()) continue;
            list.add(new ServiceDefinition(
                folder,
                normalizeQuotes(rawRequest),
                normalizeQuotes(rawResponse)
            ));
        }

        return list;
    }

    private String normalizeQuotes(String s) {
        if (s == null) return "";
        return s.replace('“','"').replace('”','"')
                .replace('\u201C','"').replace('\u201D','"');
    }
}
