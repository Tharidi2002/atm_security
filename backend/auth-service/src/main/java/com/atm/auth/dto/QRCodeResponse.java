package com.atm.auth.dto;

public class QRCodeResponse {
    private String qrCode;
    private String secretKey;
    private String message;

    public QRCodeResponse() {}

    public QRCodeResponse(String qrCode, String secretKey) {
        this.qrCode = qrCode;
        this.secretKey = secretKey;
    }

    public QRCodeResponse(String message) {
        this.message = message;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
