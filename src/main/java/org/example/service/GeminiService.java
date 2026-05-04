package org.example.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;

@Service
public class GeminiService {

    private final String API_KEY = "AIzaSyAbdB_SzuSw61wviGYU4o9R0W1iZfPJosA";

    public String analizarAudio(byte[] audioData, String prompt) {
        try {
            String API_KEY = "AIzaSyAbdB_SzuSw61wviGYU4o9R0W1iZfPJosA";
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + API_KEY;
            String audioBase64 = Base64.getEncoder().encodeToString(audioData);

            ObjectMapper mapper = new ObjectMapper();

            Map<String, Object> inlineData = new HashMap<>();
            inlineData.put("mime_type", "audio/ogg");
            inlineData.put("data", audioBase64);

            Map<String, Object> audioPart = new HashMap<>();
            audioPart.put("inline_data", inlineData);

            Map<String, Object> textPart = new HashMap<>();
            textPart.put("text", prompt);

            Map<String, Object> content = new HashMap<>();
            content.put("parts", Arrays.asList(textPart, audioPart));

            Map<String, Object> payloadMap = new HashMap<>();
            payloadMap.put("contents", Arrays.asList(content));

            String jsonPayload = mapper.writeValueAsString(payloadMap);

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(jsonPayload, headers);
            ResponseEntity<JsonNode> response = restTemplate.postForEntity(url, entity, JsonNode.class);

            String textoIA = response.getBody()
                    .path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();

            return textoIA.replace("```json", "").replace("```", "").trim();

        }  catch (org.springframework.web.client.HttpClientErrorException e) {
        System.err.println("❌ Error de Cliente: " + e.getResponseBodyAsString());
        return "{\"esValido\": false}";
    } catch (Exception e) {
        System.err.println("❌ Error General: " + e.getMessage());
        return "{\"esValido\": false}";
    }
    }

    public String analizarTexto(String prompt) {
        try {
            String API_KEY = "AIzaSyAbdB_SzuSw61wviGYU4o9R0W1iZfPJosA";
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + API_KEY;

            ObjectMapper mapper = new ObjectMapper();

            Map<String, Object> textPart = new HashMap<>();
            textPart.put("text", prompt);

            Map<String, Object> content = new HashMap<>();
            content.put("parts", Arrays.asList(textPart));

            Map<String, Object> payloadMap = new HashMap<>();
            payloadMap.put("contents", Arrays.asList(content));

            String jsonPayload = mapper.writeValueAsString(payloadMap);

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(jsonPayload, headers);
            ResponseEntity<JsonNode> response = restTemplate.postForEntity(url, entity, JsonNode.class);

            String textoIA = response.getBody()
                    .path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();

            return textoIA.replace("```json", "").replace("```", "").trim();

        } catch (Exception e) {
            System.err.println("❌ Error en analizarTexto: " + e.getMessage());
            return "{\"esValido\": false}";
        }
    }

}