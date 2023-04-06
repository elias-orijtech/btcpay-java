package com.orijtech.btcpay;

import java.util.Base64;
import java.net.http.HttpClient;
import java.io.IOException;
import java.util.HexFormat;

// CLI is a simple demonstration that creates an invoice.
public class CLI {
    public static void main(String[] args) throws IOException, InterruptedException {
        var payload = HexFormat.of().parseHex("7b0a20202264656c69766572794964223a20224342434258514e426a457742367567354d6131367276222c0a202022776562686f6f6b4964223a20223572534154636d4643564b444863577a615862713243222c0a2020226f726967696e616c44656c69766572794964223a20225f5f746573745f5f66363334313534652d396630322d346661362d393330362d6635643865613133383731365f5f746573745f5f222c0a2020226973526564656c6976657279223a2066616c73652c0a20202274797065223a2022496e766f69636543726561746564222c0a20202274696d657374616d70223a20313638303831373033312c0a20202273746f72654964223a20224557526a734c3146447a7a6b3162796e41453962555559325258625a3634635633564c324347645639325061222c0a202022696e766f6963654964223a20225f5f746573745f5f63366334373734662d383461652d346262382d393163332d3731633730373138633361355f5f746573745f5f222c0a2020226d65746164617461223a206e756c6c0a7d");
        var callback = BTCPay.parseCallback(
            // The webhook secret from the BTCPay administration interface
            "4CFxdz9TKsQPgFnSXAVU269iFAq4",
            // The signature from the callback `Btcpay-Sig` header, which must match
            // `sha256=HMAC256(secret, payload)`.
            "sha256=eef46373dafe64aeb54428159bb1cc1d01da628b9fef8c500f4e352c40f5f5df",
            // The json payload.
            payload
        );
        System.out.println("Authenticated callback, type " + callback.type + ", invoice id " + callback.invoiceId);

        var storeURL = args[0];
        var storeID = args[1];
        var apiKey = args[2];
        var invReq = new BTCPay.Invoice();
        invReq.metadata = new BTCPay.Invoice.Metadata();
        invReq.metadata.orderId = "12345";
        invReq.amount = 0.12;
        invReq.currency = "USD";
        var http = HttpClient.newHttpClient();
        var client = new BTCPay(storeURL, storeID, apiKey);
        var inv = client.newInvoice(http, invReq);
        System.out.println("Invoice created, id: " + inv.id);
    }
}
