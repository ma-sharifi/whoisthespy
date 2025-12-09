package com.whoisthespy.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.image.ImageGeneration;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageGenerationService {
    private final OpenAiImageModel imageModel;
    
    @Value("${app.image.storage-path:/tmp/whoisthespy/images}")
    private String storagePath;
    
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
            
            log.info("Generating image with prompt: {}", prompt);
            
            // Generate image using Spring AI
            ImageResponse response = imageModel.call(
                new ImagePrompt(prompt)
            );
            
            if (response == null || response.getResult() == null) {
                throw new RuntimeException("Failed to generate image: null response");
            }
            
            // Spring AI returns a list of ImageGeneration objects
            List<ImageGeneration> imageGenerations = response.getResult().getOutput();
            if (imageGenerations == null || imageGenerations.isEmpty()) {
                throw new RuntimeException("Failed to generate image: empty output");
            }
            
            ImageGeneration imageGeneration = imageGenerations.get(0);
            String imageUrl = imageGeneration.getUrl();
            
            if (imageUrl == null || imageUrl.isEmpty()) {
                // Try alternative: get b64_json if URL is not available
                String b64Json = imageGeneration.getB64Json();
                if (b64Json != null && !b64Json.isEmpty()) {
                    // Decode base64 and save
                    byte[] imageBytes = java.util.Base64.getDecoder().decode(b64Json);
                    String imageId = UUID.randomUUID().toString();
                    String fileName = imageId + ".png";
                    Path imagePath = storageDir.resolve(fileName);
                    Files.write(imagePath, imageBytes);
                    log.info("Image saved from base64 to: {}", imagePath);
                    return "/api/images/" + imageId;
                }
                throw new RuntimeException("Generated image URL is empty and no base64 data available");
            }
            
            // Download and save image locally
            String imageId = UUID.randomUUID().toString();
            String fileName = imageId + ".png";
            Path imagePath = storageDir.resolve(fileName);
            
            downloadImage(imageUrl, imagePath);
            
            log.info("Image saved to: {}", imagePath);
            
            return "/api/images/" + imageId;
            
        } catch (Exception e) {
            log.error("Error generating image", e);
            throw new RuntimeException("Failed to generate image: " + e.getMessage(), e);
        }
    }
    
    private void downloadImage(String imageUrl, Path targetPath) throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        byte[] imageBytes = restTemplate.getForObject(imageUrl, byte[].class);
        
        if (imageBytes == null) {
            throw new RuntimeException("Failed to download image from URL");
        }
        
        Files.write(targetPath, imageBytes);
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

