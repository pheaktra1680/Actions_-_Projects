package com.example.ActionAndProject.service;

import org.springframework.stereotype.Service;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class TelegramService {
    private final String BOT_TOKEN = "8342645250:AAGxfZ6BkpqbgERo1H6AezyXq9tdOy03cn8";
    private final String CHAT_ID = "1484435282";

    public void sendOTP(String staffId, String otp) {
        try {
            // 1. Create your message
            String rawText = "🔐 *CBD Teams Security*\n\n" +
                    "Hello " + staffId + ",\n" +
                    "Your recovery code is: *" + otp + "*\n\n" +
                    "This code expires in 5 minutes.";

            // 2. ENCODE the text (This fixes the "Illegal character" error)
            String encodedText = URLEncoder.encode(rawText, StandardCharsets.UTF_8.toString());

            // 3. Build the URL with the ENCODED text
            String urlString = "https://api.telegram.org/bot" + BOT_TOKEN +
                    "/sendMessage?chat_id=" + CHAT_ID +
                    "&text=" + encodedText + "&parse_mode=Markdown";

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            // Just to check if it worked
            int responseCode = conn.getResponseCode();
            System.out.println("Telegram sent! Response code: " + responseCode);

            conn.disconnect();
        } catch (Exception e) {
            System.err.println("Error sending Telegram: " + e.getMessage());
            e.printStackTrace();
        }
    }
}