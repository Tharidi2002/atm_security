package com.atm.auth.service;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

@Slf4j
@Service
public class TotpService {
    
    @Value("${totp.issuer}")
    private String issuer;
    
    @Value("${totp.digits}")
    private int digits;
    
    @Value("${totp.period}")
    private int period;
    
    private final GoogleAuthenticator googleAuthenticator = new GoogleAuthenticator();
    
    public String generateSecret() {
        GoogleAuthenticatorKey key = googleAuthenticator.createCredentials();
        return key.getKey();
    }
    
    public String generateQrCodeUrl(String secret, String username) {
        GoogleAuthenticatorKey key = new GoogleAuthenticatorKey.Builder(secret)
                .build();
        
        return GoogleAuthenticatorQRGenerator.getOtpAuthTotpURL(issuer, username, key);
    }
    
    public boolean verifyCode(String secret, String code) {
        try {
            int verificationCode = Integer.parseInt(code);
            return googleAuthenticator.authorize(secret, verificationCode);
        } catch (NumberFormatException e) {
            log.error("Invalid TOTP code format: {}", code);
            return false;
        }
    }
    
    public String generateQrCodeImage(String qrUrl) {
        try {
            // This is a simplified implementation
            // In production, you would use a proper QR code library like ZXing
            BufferedImage image = generateDummyQrImage(qrUrl);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            log.error("Error generating QR code image", e);
            return null;
        }
    }
    
    private BufferedImage generateDummyQrImage(String text) {
        // Dummy QR code generation - replace with proper library in production
        BufferedImage image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        
        // Create a simple pattern
        for (int x = 0; x < 200; x++) {
            for (int y = 0; y < 200; y++) {
                int color = (x + y) % 2 == 0 ? 0xFFFFFF : 0x000000;
                image.setRGB(x, y, color);
            }
        }
        
        return image;
    }
}
