CREATE TABLE standings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    team_id BIGINT UNIQUE,
    team_name VARCHAR(100) NOT NULL,
    team_crest VARCHAR(255),
    position INTEGER NOT NULL,
    played_games INTEGER NOT NULL,
    won INTEGER NOT NULL,
    draw INTEGER NOT NULL,
    lost INTEGER NOT NULL,
    points INTEGER NOT NULL,
    goals_for INTEGER NOT NULL,
    goals_against INTEGER NOT NULL,
    goal_difference INTEGER NOT NULL,
    form VARCHAR(20),
    home_wins INTEGER,
    home_draw INTEGER,
    home_losses INTEGER,
    away_wins INTEGER,
    away_draw INTEGER,
    away_losses INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Indici per performance
CREATE INDEX idx_standings_position ON standings(position);
CREATE INDEX idx_standings_team_id ON standings(team_id);
CREATE INDEX idx_standings_points ON standings(points DESC);
CREATE INDEX idx_standings_team_name ON standings(team_name);
CREATE INDEX idx_standings_milan_search ON standings(team_name);
CREATE INDEX idx_standings_position_points ON standings(position ASC, points DESC);