package com.atm.auth.service;

import com.atm.auth.entity.User;
import com.atm.auth.repository.UserRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
@Transactional
public class TwoFactorAuthService {

    @Autowired
    private UserRepository userRepository;

    @Value("${atm.app.mfa.issuer:ATM Security System}")
    private String mfaIssuer;

    private final GoogleAuthenticator gAuth = new GoogleAuthenticator();

    public String generateSecretKey() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[20];
        random.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes).substring(0, 32).replaceAll("[=/+]", "");
    }

    public String generateQRCode(String username, String secretKey) throws WriterException, IOException {
        GoogleAuthenticatorKey key = new GoogleAuthenticatorKey.Builder(secretKey).build();
        String qrUrl = GoogleAuthenticatorQRGenerator.getOtpAuthURL(mfaIssuer, username, key);
        
        BitMatrix bitMatrix = new com.google.zxing.qrcode.QRCodeWriter()
                .encode(qrUrl, BarcodeFormat.QR_CODE, 200, 200);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
        
        return Base64.getEncoder().encodeToString(outputStream.toByteArray());
    }

    public boolean verifyCode(String secretKey, int code) {
        return gAuth.authorize(secretKey, code);
    }

    public String enableTwoFactor(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String secretKey = generateSecretKey();
        user.setMfaSecret(secretKey);
        userRepository.save(user);

        return secretKey;
    }

    public void confirmTwoFactor(String username, int verificationCode) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getMfaSecret() == null) {
            throw new RuntimeException("MFA not initialized for user");
        }

        if (!verifyCode(user.getMfaSecret(), verificationCode)) {
            throw new RuntimeException("Invalid verification code");
        }

        user.setMfaEnabled(true);
        user.setBackupCodes(generateBackupCodes());
        userRepository.save(user);
    }

    public void disableTwoFactor(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setMfaEnabled(false);
        user.setMfaSecret(null);
        user.setBackupCodes(null);
        userRepository.save(user);
    }

    public boolean verifyTwoFactor(String username, int code) {
        User user = userRepository.findByUsername(username)
                .orElse(null);

        if (user == null || !user.getMfaEnabled()) {
            return false;
        }

        // Check if it's a backup code
        if (isBackupCode(user, code)) {
            consumeBackupCode(user, code);
            return true;
        }

        // Check TOTP code
        return verifyCode(user.getMfaSecret(), code);
    }

    private String generateBackupCodes() {
        List<String> codes = new ArrayList<>();
        SecureRandom random = new SecureRandom();
        
        for (int i = 0; i < 10; i++) {
            int code = 100000 + random.nextInt(900000);
            codes.add(String.valueOf(code));
        }
        
        return String.join(",", codes);
    }

    private boolean isBackupCode(User user, int code) {
        if (user.getBackupCodes() == null) {
            return false;
        }
        
        String[] codes = user.getBackupCodes().split(",");
        for (String codeStr : codes) {
            if (codeStr.trim().equals(String.valueOf(code))) {
                return true;
            }
        }
        return false;
    }

    private void consumeBackupCode(User user, int code) {
        if (user.getBackupCodes() == null) {
            return;
        }
        
        String[] codes = user.getBackupCodes().split(",");
        List<String> remainingCodes = new ArrayList<>();
        
        for (String codeStr : codes) {
            if (!codeStr.trim().equals(String.valueOf(code))) {
                remainingCodes.add(codeStr.trim());
            }
        }
        
        user.setBackupCodes(String.join(",", remainingCodes));
        userRepository.save(user);
    }

    public String getRemainingBackupCodes(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getBackupCodes() == null) {
            return "0";
        }

        return String.valueOf(user.getBackupCodes().split(",").length);
    }
}
