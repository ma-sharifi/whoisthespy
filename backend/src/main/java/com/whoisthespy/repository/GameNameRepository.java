package com.whoisthespy.repository;

import com.whoisthespy.entity.GameName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GameNameRepository extends JpaRepository<GameName, UUID> {
    List<GameName> findByCategory(String category);
    
    @Query(value = "SELECT * FROM game_names WHERE category = :category ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    GameName findRandomByCategory(String category);
}
