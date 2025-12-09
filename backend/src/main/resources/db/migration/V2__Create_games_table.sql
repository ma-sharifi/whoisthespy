CREATE TABLE games (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    join_code VARCHAR(6) NOT NULL UNIQUE,
    host_user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    number_of_spies INTEGER,
    current_turn_index INTEGER DEFAULT 0,
    civilian_word VARCHAR(255),
    spy_word VARCHAR(255),
    current_image_url VARCHAR(500),
    game_state VARCHAR(20) NOT NULL DEFAULT 'WAITING',
    CONSTRAINT check_game_state CHECK (game_state IN ('WAITING', 'RUNNING', 'FINISHED'))
);

CREATE TABLE game_players (
    game_id UUID NOT NULL REFERENCES games(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    PRIMARY KEY (game_id, user_id)
);

CREATE TABLE game_spies (
    game_id UUID NOT NULL REFERENCES games(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    PRIMARY KEY (game_id, user_id)
);

CREATE INDEX idx_games_join_code ON games(join_code);
CREATE INDEX idx_games_host_user_id ON games(host_user_id);
CREATE INDEX idx_game_players_game_id ON game_players(game_id);
CREATE INDEX idx_game_spies_game_id ON game_spies(game_id);

