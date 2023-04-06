package com.orijtech.btcpay;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpClient;
import java.net.URI;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

public final class BTCPay {
    private final String apiUrl;
    private final String authToken;
    
    public BTCPay(String storeUrl, String storeId, String authToken) {
        this.apiUrl = storeUrl+"/api/v1/stores/"+storeId;
        this.authToken = authToken;
    }

    public Invoice newInvoice(HttpClient cl, Invoice inv) throws IOException, InterruptedException {
        var mapper = new ObjectMapper();
        var reqJson = mapper.writeValueAsString(inv);
        var req = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl+"/invoices"))
                .header("Content-Type", "application/json")
                .header("Authorization", "token " + authToken)
                .POST(HttpRequest.BodyPublishers.ofString(reqJson))
                .build();
        var response = cl.send(req, HttpResponse.BodyHandlers.ofString());
        return mapper.readValue(response.body(), Invoice.class);
    }
    
    public static Callback parseCallback(String secret, String signature, byte[] payload) throws IOException {
        // signature is on the form "sha256-<hex-encoded digest>".
        if (!signature.startsWith("sha256=")) {
            throw new IOException("invalid signature");
        }
        signature = signature.substring(7);
        var sig = HexFormat.of().parseHex(signature);
        SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes("UTF-8"), "HmacSHA256");
        Mac mac;
        try {
            mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKey);
        } catch (InvalidKeyException e) {
            // The key is always valid.
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            // The HmacSHA256 algorithm is guaranteed to exist.
            throw new RuntimeException(e);
        }
        var dgst = mac.doFinal(payload);
        if (!MessageDigest.isEqual(dgst, sig)) {
            throw new IOException("invalid signature");
        }
        var mapper = new ObjectMapper();
        return mapper.readValue(payload, Callback.class);
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public final static class Callback {
        public String type;
        public String invoiceId;
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public final static class Invoice {
        public String id;
        public Metadata metadata;
        public double amount;
        public String currency;
        
        public final static class Metadata {
            public String orderId;
        }
    }
}
