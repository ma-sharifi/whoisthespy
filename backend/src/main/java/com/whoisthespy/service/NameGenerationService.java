package com.whoisthespy.service;

import com.whoisthespy.entity.GameName;
import com.whoisthespy.repository.GameNameRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NameGenerationService {
    
    private final GameNameRepository gameNameRepository;
    
    public static class GeneratedName {
        private String name;
        private String summary;
        
        public GeneratedName(String name, String summary) {
            this.name = name;
            this.summary = summary;
        }
        
        public String getName() {
            return name;
        }
        
        public String getSummary() {
            return summary;
        }
    }
    
    public GeneratedName generateName() {
        try {
            log.info("Fetching random location name from database");
            
            // Get a random location name from the database
            GameName gameName = gameNameRepository.findRandomByCategory("location");
            
            if (gameName == null) {
                log.warn("No location names found in database, using fallback");
                return new GeneratedName("Unknown Location", "A location for discussion in the game.");
            }
            
            // Generate a summary based on the location name
            String summary = generateSummaryForLocation(gameName.getName());
            
            return new GeneratedName(gameName.getName(), summary);
            
        } catch (Exception e) {
            log.error("Error generating name from database", e);
            throw new RuntimeException("Failed to generate name: " + e.getMessage(), e);
        }
    }
    
    private String generateSummaryForLocation(String locationName) {
        // Generate a simple summary for the location
        // This is a basic implementation - you could enhance this with more detailed descriptions
        return "A famous and recognizable location that players can discuss. " +
               "This place is well-known around the world and has cultural or historical significance.";
    }
}
