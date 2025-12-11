package com.whoisthespy.service;

import com.whoisthespy.entity.Game;
import com.whoisthespy.entity.User;
import com.whoisthespy.repository.GameRepository;
import com.whoisthespy.repository.UserRepository;
import com.whoisthespy.service.NameGenerationService.GeneratedName;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class GameService {
    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private final NameGenerationService nameGenerationService;
    
    private static final String CIVILIAN_WORDS = "cat,dog,house,car,tree,book,phone,computer,table,chair";
    private static final String SPY_WORDS = "animal,pet,home,vehicle,plant,object,device,machine,furniture,seat";
    
    public Game createGame(UUID hostUserId) {
        User host = userRepository.findById(hostUserId)
            .orElseThrow(() -> new IllegalArgumentException("Host user not found: " + hostUserId));
        
        Game game = new Game();
        game.setJoinCode(generateJoinCode());
        game.setHostUserId(hostUserId);
        game.setPlayers(new ArrayList<>(List.of(hostUserId)));
        game.setGameState(Game.GameState.WAITING);
        
        return gameRepository.save(game);
    }
    
    public Game joinGame(String joinCode, UUID userId) {
        Game game = gameRepository.findByJoinCode(joinCode)
            .orElseThrow(() -> new IllegalArgumentException("Game not found with join code: " + joinCode));
        
        if (game.getGameState() != Game.GameState.WAITING) {
            throw new IllegalStateException("Game is not accepting new players");
        }
        
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User not found: " + userId);
        }
        
        if (!game.getPlayers().contains(userId)) {
            game.getPlayers().add(userId);
            gameRepository.save(game);
        }
        
        return game;
    }
    
    public Game startGame(UUID gameId, UUID hostUserId, Integer numberOfSpies) {
        Game game = gameRepository.findById(gameId)
            .orElseThrow(() -> new IllegalArgumentException("Game not found: " + gameId));
        
        if (!game.getHostUserId().equals(hostUserId)) {
            throw new IllegalStateException("Only the host can start the game");
        }
        
        if (game.getGameState() != Game.GameState.WAITING) {
            throw new IllegalStateException("Game is already started or finished");
        }
        
        if (numberOfSpies == null || numberOfSpies < 1 || numberOfSpies >= game.getPlayers().size()) {
            throw new IllegalArgumentException("Invalid number of spies");
        }
        
        // Generate name with summary using AI
        GeneratedName generatedName = nameGenerationService.generateName();
        game.setGeneratedName(generatedName.getName());
        game.setGeneratedSummary(generatedName.getSummary());
        
        // Assign words (keep for backward compatibility, but use generated name as primary)
        String[] civilianWords = CIVILIAN_WORDS.split(",");
        String[] spyWords = SPY_WORDS.split(",");
        Random random = new Random();
        
        game.setCivilianWord(civilianWords[random.nextInt(civilianWords.length)]);
        game.setSpyWord(spyWords[random.nextInt(spyWords.length)]);
        
        // Assign spies
        List<UUID> players = new ArrayList<>(game.getPlayers());
        Collections.shuffle(players);
        game.setSpyUserIds(players.subList(0, numberOfSpies));
        game.setNumberOfSpies(numberOfSpies);
        
        game.setGameState(Game.GameState.RUNNING);
        game.setCurrentTurnIndex(0);
        
        return gameRepository.save(game);
    }
    
    public Game nextTurn(UUID gameId, UUID hostUserId) {
        Game game = gameRepository.findById(gameId)
            .orElseThrow(() -> new IllegalArgumentException("Game not found: " + gameId));
        
        if (!game.getHostUserId().equals(hostUserId)) {
            throw new IllegalStateException("Only the host can advance turns");
        }
        
        if (game.getGameState() != Game.GameState.RUNNING) {
            throw new IllegalStateException("Game is not running");
        }
        
        game.setCurrentTurnIndex(game.getCurrentTurnIndex() + 1);
        return gameRepository.save(game);
    }
    
    public Game getGame(UUID gameId) {
        return gameRepository.findById(gameId)
            .orElseThrow(() -> new IllegalArgumentException("Game not found: " + gameId));
    }
    
    public Game generateNewName(UUID gameId) {
        Game game = gameRepository.findById(gameId)
            .orElseThrow(() -> new IllegalArgumentException("Game not found: " + gameId));
        
        GeneratedName generatedName = nameGenerationService.generateName();
        game.setGeneratedName(generatedName.getName());
        game.setGeneratedSummary(generatedName.getSummary());
        
        return gameRepository.save(game);
    }
    
    private String generateJoinCode() {
        Random random = new Random();
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        // Ensure uniqueness
        while (gameRepository.findByJoinCode(code.toString()).isPresent()) {
            code = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                code.append(chars.charAt(random.nextInt(chars.length())));
            }
        }
        
        return code.toString();
    }
}

