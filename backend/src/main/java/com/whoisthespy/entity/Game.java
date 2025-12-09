package com.whoisthespy.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "games")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "join_code", unique = true, nullable = false, length = 6)
    private String joinCode;
    
    @Column(name = "host_user_id", nullable = false)
    private UUID hostUserId;
    
    @ElementCollection
    @CollectionTable(name = "game_players", joinColumns = @JoinColumn(name = "game_id"))
    @Column(name = "user_id")
    private List<UUID> players = new ArrayList<>();
    
    @Column(name = "number_of_spies")
    private Integer numberOfSpies;
    
    @ElementCollection
    @CollectionTable(name = "game_spies", joinColumns = @JoinColumn(name = "game_id"))
    @Column(name = "user_id")
    private List<UUID> spyUserIds = new ArrayList<>();
    
    @Column(name = "current_turn_index")
    private Integer currentTurnIndex = 0;
    
    @Column(name = "civilian_word")
    private String civilianWord;
    
    @Column(name = "spy_word")
    private String spyWord;
    
    @Column(name = "current_image_url")
    private String currentImageUrl;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "game_state")
    private GameState gameState = GameState.WAITING;
    
    public enum GameState {
        WAITING, RUNNING, FINISHED
    }
}

