package com.example.model;

public class ServiceDefinition {
    private final String folderName;
    private final String rawRequestPayload;
    private final String rawResponsePayload;

    public ServiceDefinition(String folderName, String rawRequestPayload, String rawResponsePayload) {
        this.folderName = folderName;
        this.rawRequestPayload = rawRequestPayload;
        this.rawResponsePayload = rawResponsePayload;
    }

    public String getFolderName() {
        return folderName;
    }

    public String getRawRequestPayload() {
        return rawRequestPayload;
    }

    public String getRawResponsePayload() {
        return rawResponsePayload;
    }
}