-- Indici per la tabella articles
CREATE INDEX idx_articles_featured ON articles(is_featured);
CREATE INDEX idx_articles_type ON articles(article_type);
CREATE INDEX idx_articles_category_type ON articles(category_id, article_type);

-- Indici per la tabella matches
CREATE INDEX idx_matches_date ON matches(date);
CREATE INDEX idx_matches_played ON matches(is_played);

-- Indice composito per query su partite future non giocate (getNextMatch)
CREATE INDEX idx_matches_played_date ON matches(is_played, date);

-- Indice per collegamento con API esterne
CREATE INDEX idx_matches_external_id ON matches(external_id);

-- Indice per competizione
CREATE INDEX idx_matches_competition ON matches(competition);
