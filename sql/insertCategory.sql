INSERT INTO categories (name) VALUES 
('Calciomercato'),
('Match Report'),
('Interviste'),
('Tattica'),
('Primavera'),
('Storia Rossonera'),
('Champions League'),
('Serie A'),
('Coppa Italia')
ON DUPLICATE KEY UPDATE name=name;