-- Table pour stocker les résultats des quiz des étudiants
CREATE TABLE IF NOT EXISTS `resultat_quiz` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `examen_id` int(11) NOT NULL,
  `id_user_id` int(11) NOT NULL,
  `score` int(11) NOT NULL,
  `total_points` int(11) NOT NULL,
  `date_passage` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_resultat_quiz_examen` (`examen_id`),
  KEY `fk_resultat_quiz_user` (`id_user_id`),
  CONSTRAINT `fk_resultat_quiz_examen` FOREIGN KEY (`examen_id`) REFERENCES `examen` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_resultat_quiz_user` FOREIGN KEY (`id_user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci; 