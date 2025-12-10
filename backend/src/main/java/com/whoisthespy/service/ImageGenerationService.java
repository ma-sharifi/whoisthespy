package com.whoisthespy.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class ImageGenerationService {
    
    @Value("${spring.ai.google.genai.api-key:AIzaSyCNuThstIKfhVAuYhd8r53K3ZPvhuU6DwY}")
    private String apiKey;
    
    @Value("${app.image.storage-path:/tmp/whoisthespy/images}")
    private String storagePath;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/imagen-3.0-generate-001:predict";
    
    public String generateAndStoreImage(String word, String role) {
        try {
            // Create storage directory if it doesn't exist
            Path storageDir = Paths.get(storagePath);
            if (!Files.exists(storageDir)) {
                Files.createDirectories(storageDir);
            }
            
            // Generate prompt based on role
            String prompt = role.equals("spy") 
                ? "A simple, abstract illustration related to: " + word
                : "A clear, detailed illustration of: " + word;
            
            log.info("Generating image with prompt: {} using Gemini API", prompt);
            
            // Call Google Gemini Imagen API
            String imageBase64 = callGeminiImageAPI(prompt);
            
            if (imageBase64 == null || imageBase64.isEmpty()) {
                throw new RuntimeException("Failed to generate image: empty response from API");
            }
            
            // Decode base64 and save
            byte[] imageBytes = java.util.Base64.getDecoder().decode(imageBase64);
            String imageId = UUID.randomUUID().toString();
            String fileName = imageId + ".png";
            Path imagePath = storageDir.resolve(fileName);
            Files.write(imagePath, imageBytes);
            
            log.info("Image saved to: {}", imagePath);
            
            return "/api/images/" + imageId;
            
        } catch (Exception e) {
            log.error("Error generating image", e);
            throw new RuntimeException("Failed to generate image: " + e.getMessage(), e);
        }
    }
    
    private String callGeminiImageAPI(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> instance = new HashMap<>();
            instance.put("prompt", prompt);
            requestBody.put("instances", new Object[]{instance});
            
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("sampleCount", 1);
            parameters.put("aspectRatio", "1:1");
            parameters.put("safetyFilterLevel", "block_some");
            parameters.put("personGeneration", "allow_all");
            requestBody.put("parameters", parameters);
            
            String url = GEMINI_API_URL + "?key=" + apiKey;
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                if (body.containsKey("predictions") && ((java.util.List<?>) body.get("predictions")).size() > 0) {
                    Map<String, Object> prediction = (Map<String, Object>) ((java.util.List<?>) body.get("predictions")).get(0);
                    if (prediction.containsKey("bytesBase64Encoded")) {
                        return (String) prediction.get("bytesBase64Encoded");
                    }
                }
            }
            
            log.error("Unexpected response from Gemini API: {}", response.getBody());
            return null;
            
        } catch (Exception e) {
            log.error("Error calling Gemini API", e);
            throw new RuntimeException("Failed to call Gemini API: " + e.getMessage(), e);
        }
    }
    
    public Resource getImageResource(String imageId) {
        try {
            Path imagePath = Paths.get(storagePath).resolve(imageId + ".png");
            Resource resource = new UrlResource(imagePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Image not found: " + imageId);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error loading image: " + imageId, e);
        }
    }
}
