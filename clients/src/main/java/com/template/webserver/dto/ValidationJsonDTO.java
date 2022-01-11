package com.template.webserver.dto;

public class ValidationJsonDTO {

    private boolean isJsonValidated;
    private String jsonValidationMessage;
    private String optionalInfo;

    public ValidationJsonDTO(
            boolean isJsonValidated,
            String jsonValidationMessage) {
        this.isJsonValidated = isJsonValidated;
        this.jsonValidationMessage = jsonValidationMessage;
    }

    public ValidationJsonDTO(
            boolean isJsonValidated,
            String jsonValidationMessage,
            String optionalInfo) {
        this.isJsonValidated = isJsonValidated;
        this.jsonValidationMessage = jsonValidationMessage;
        this.optionalInfo = optionalInfo;
    }

    public boolean isJsonValidated() {
        return isJsonValidated;
    }

    public void setJsonValidated(boolean jsonValidated) {
        isJsonValidated = jsonValidated;
    }

    public String getJsonValidationMessage() {
        return jsonValidationMessage;
    }

    public void setJsonValidationMessage(String jsonValidationMessage) {
        this.jsonValidationMessage = jsonValidationMessage;
    }

    public String getOptionalInfo() {
        return optionalInfo;
    }

    public void setOptionalInfo(String optionalInfo) {
        this.optionalInfo = optionalInfo;
    }
}
