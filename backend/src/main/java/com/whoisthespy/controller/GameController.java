package com.whoisthespy.controller;

import com.whoisthespy.entity.Game;
import com.whoisthespy.service.GameService;
import com.whoisthespy.service.ImageGenerationService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/game")
@RequiredArgsConstructor
public class GameController {
    private final GameService gameService;
    private final ImageGenerationService imageGenerationService;
    private final SimpMessagingTemplate messagingTemplate;
    
    @PostMapping("/create")
    public ResponseEntity<GameResponse> createGame(@RequestBody CreateGameRequest request) {
        try {
            Game game = gameService.createGame(request.getHostUserId());
            broadcastPlayersUpdate(game.getId(), game.getPlayers());
            return ResponseEntity.status(HttpStatus.CREATED).body(new GameResponse(game));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/join")
    public ResponseEntity<GameResponse> joinGame(@RequestBody JoinGameRequest request) {
        try {
            Game game = gameService.joinGame(request.getJoinCode(), request.getUserId());
            broadcastPlayersUpdate(game.getId(), game.getPlayers());
            return ResponseEntity.ok(new GameResponse(game));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/start")
    public ResponseEntity<GameResponse> startGame(@RequestBody StartGameRequest request) {
        try {
            Game game = gameService.startGame(
                request.getGameId(), 
                request.getHostUserId(), 
                request.getNumberOfSpies()
            );
            broadcastGameUpdate(game);
            return ResponseEntity.ok(new GameResponse(game));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/{gameId}")
    public ResponseEntity<GameResponse> getGame(@PathVariable UUID gameId) {
        try {
            Game game = gameService.getGame(gameId);
            return ResponseEntity.ok(new GameResponse(game));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping("/{gameId}/nextTurn")
    public ResponseEntity<GameResponse> nextTurn(
            @PathVariable UUID gameId, 
            @RequestBody NextTurnRequest request) {
        try {
            Game game = gameService.nextTurn(gameId, request.getHostUserId());
            broadcastTurnUpdate(game);
            return ResponseEntity.ok(new GameResponse(game));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/{gameId}/generateImage")
    public ResponseEntity<ImageResponse> generateImage(
            @PathVariable UUID gameId,
            @RequestBody GenerateImageRequest request) {
        try {
            Game game = gameService.getGame(gameId);
            
            if (!game.getHostUserId().equals(request.getHostUserId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            String word = request.getWord() != null ? request.getWord() : game.getCivilianWord();
            String role = request.getRole() != null ? request.getRole() : "civilian";
            
            String imageUrl = imageGenerationService.generateAndStoreImage(word, role);
            game = gameService.updateGameImageUrl(gameId, imageUrl);
            
            // Broadcast image update
            Map<String, Object> imageUpdate = new HashMap<>();
            imageUpdate.put("imageUrl", imageUrl);
            imageUpdate.put("gameId", gameId);
            messagingTemplate.convertAndSend("/topic/game/" + gameId + "/image", imageUpdate);
            
            return ResponseEntity.ok(new ImageResponse(imageUrl));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    private void broadcastPlayersUpdate(UUID gameId, java.util.List<UUID> players) {
        Map<String, Object> update = new HashMap<>();
        update.put("players", players);
        messagingTemplate.convertAndSend("/topic/game/" + gameId + "/players", update);
    }
    
    private void broadcastGameUpdate(Game game) {
        Map<String, Object> update = new HashMap<>();
        update.put("gameState", game.getGameState());
        update.put("players", game.getPlayers());
        update.put("numberOfSpies", game.getNumberOfSpies());
        messagingTemplate.convertAndSend("/topic/game/" + game.getId() + "/players", update);
    }
    
    private void broadcastTurnUpdate(Game game) {
        Map<String, Object> update = new HashMap<>();
        update.put("currentTurnIndex", game.getCurrentTurnIndex());
        messagingTemplate.convertAndSend("/topic/game/" + game.getId() + "/turn", update);
    }
    
    @Data
    public static class CreateGameRequest {
        private UUID hostUserId;
    }
    
    @Data
    public static class JoinGameRequest {
        private String joinCode;
        private UUID userId;
    }
    
    @Data
    public static class StartGameRequest {
        private UUID gameId;
        private UUID hostUserId;
        private Integer numberOfSpies;
    }
    
    @Data
    public static class NextTurnRequest {
        private UUID hostUserId;
    }
    
    @Data
    public static class GenerateImageRequest {
        private UUID hostUserId;
        private String word;
        private String role;
    }
    
    @Data
    public static class GameResponse {
        private UUID id;
        private String joinCode;
        private UUID hostUserId;
        private java.util.List<UUID> players;
        private Integer numberOfSpies;
        private java.util.List<UUID> spyUserIds;
        private Integer currentTurnIndex;
        private String civilianWord;
        private String spyWord;
        private String currentImageUrl;
        private Game.GameState gameState;
        
        public GameResponse(Game game) {
            this.id = game.getId();
            this.joinCode = game.getJoinCode();
            this.hostUserId = game.getHostUserId();
            this.players = game.getPlayers();
            this.numberOfSpies = game.getNumberOfSpies();
            this.spyUserIds = game.getSpyUserIds();
            this.currentTurnIndex = game.getCurrentTurnIndex();
            this.civilianWord = game.getCivilianWord();
            this.spyWord = game.getSpyWord();
            this.currentImageUrl = game.getCurrentImageUrl();
            this.gameState = game.getGameState();
        }
    }
    
    @Data
    public static class ImageResponse {
        private String imageUrl;
        
        public ImageResponse(String imageUrl) {
            this.imageUrl = imageUrl;
        }
    }
}

