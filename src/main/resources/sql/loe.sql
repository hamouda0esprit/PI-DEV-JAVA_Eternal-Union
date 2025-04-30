-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Hôte : 127.0.0.1
-- Généré le : mar. 15 avr. 2025 à 16:21
-- Version du serveur : 10.4.32-MariaDB
-- Version de PHP : 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de données : `loe`
--

-- --------------------------------------------------------

--
-- Structure de la table `course`
--

CREATE TABLE `course` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `title` varchar(255) NOT NULL,
  `image` varchar(255) NOT NULL,
  `subject` varchar(255) NOT NULL,
  `rate` int(11) NOT NULL,
  `last_update` datetime NOT NULL COMMENT '(DC2Type:datetime_immutable)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `discussion`
--

CREATE TABLE `discussion` (
  `id` int(11) NOT NULL,
  `event_id` int(11) NOT NULL,
  `caption` varchar(255) DEFAULT NULL,
  `media` varchar(255) DEFAULT NULL,
  `created_at` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `doctrine_migration_versions`
--

CREATE TABLE `doctrine_migration_versions` (
  `version` varchar(191) NOT NULL,
  `executed_at` datetime DEFAULT NULL,
  `execution_time` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Déchargement des données de la table `doctrine_migration_versions`
--

INSERT INTO `doctrine_migration_versions` (`version`, `executed_at`, `execution_time`) VALUES
('DoctrineMigrations\\Version20250218163704', '2025-02-18 17:37:25', 560),
('DoctrineMigrations\\Version20250218174951', '2025-02-18 18:49:58', 10),
('DoctrineMigrations\\Version20250219204134', '2025-02-19 21:41:47', 46),
('DoctrineMigrations\\Version20250228234241', '2025-03-01 00:42:59', 50),
('DoctrineMigrations\\Version20250305002550', '2025-03-05 02:46:40', 446),
('DoctrineMigrations\\Version20250305004000', '2025-03-05 02:46:40', 211),
('DoctrineMigrations\\Version20250305014723', '2025-03-05 02:47:28', 322),
('DoctrineMigrations\\Version20250305015746', '2025-03-05 02:57:52', 19);

-- --------------------------------------------------------

--
-- Structure de la table `evenement`
--

CREATE TABLE `evenement` (
  `id` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  `description` longtext DEFAULT NULL,
  `dateevent` date NOT NULL,
  `location` varchar(255) NOT NULL,
  `time` time DEFAULT NULL,
  `iduser` int(11) NOT NULL,
  `photo` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `evenement_user`
--

CREATE TABLE `evenement_user` (
  `evenement_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `examen`
--

CREATE TABLE `examen` (
  `id` int(11) NOT NULL,
  `note` double DEFAULT NULL,
  `description` varchar(100) NOT NULL,
  `date` date NOT NULL,
  `matiere` varchar(30) NOT NULL,
  `duree` int(11) NOT NULL,
  `nbr_essai` int(11) NOT NULL,
  `type` varchar(30) NOT NULL,
  `titre` varchar(255) NOT NULL,
  `id_user_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `feedback`
--

CREATE TABLE `feedback` (
  `id` int(11) NOT NULL,
  `examen_id` int(11) NOT NULL,
  `contenu` longtext NOT NULL,
  `date_creation` datetime NOT NULL,
  `user_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `fnotifications`
--

CREATE TABLE `fnotifications` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `forum_id` int(11) NOT NULL,
  `message` varchar(255) NOT NULL,
  `is_read` tinyint(1) NOT NULL,
  `date_time` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `forum`
--

CREATE TABLE `forum` (
  `id` int(11) NOT NULL,
  `id_user_id` int(11) NOT NULL,
  `title` varchar(255) NOT NULL,
  `description` longtext NOT NULL,
  `media` varchar(255) DEFAULT NULL,
  `type_media` varchar(255) NOT NULL,
  `rate_forum` int(11) NOT NULL,
  `date_time` datetime NOT NULL,
  `subject` varchar(255) NOT NULL,
  `aiprompt_responce` longtext NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `forum`
--

INSERT INTO `forum` (`id`, `id_user_id`, `title`, `description`, `media`, `type_media`, `rate_forum`, `date_time`, `subject`, `aiprompt_responce`) VALUES
(1, 2, 'yo bro', 'OK BRO MOUCH LEZEM', 'images/forum/474180226_611044111820838_4218858557603566673_n-67b513de2db75.jpg', 'image', 0, '2025-02-19 00:12:35', 'Physique', '');

-- --------------------------------------------------------

--
-- Structure de la table `item`
--

CREATE TABLE `item` (
  `id` int(11) NOT NULL,
  `lesson_id` int(11) DEFAULT NULL,
  `type_item` varchar(255) NOT NULL,
  `content` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `lesson`
--

CREATE TABLE `lesson` (
  `id` int(11) NOT NULL,
  `course_id` int(11) DEFAULT NULL,
  `title` varchar(255) NOT NULL,
  `description` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `messenger_messages`
--

CREATE TABLE `messenger_messages` (
  `id` bigint(20) NOT NULL,
  `body` longtext NOT NULL,
  `headers` longtext NOT NULL,
  `queue_name` varchar(190) NOT NULL,
  `created_at` datetime NOT NULL COMMENT '(DC2Type:datetime_immutable)',
  `available_at` datetime NOT NULL COMMENT '(DC2Type:datetime_immutable)',
  `delivered_at` datetime DEFAULT NULL COMMENT '(DC2Type:datetime_immutable)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `notification`
--

CREATE TABLE `notification` (
  `id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `questions`
--

CREATE TABLE `questions` (
  `id` int(11) NOT NULL,
  `examen_id` int(11) DEFAULT NULL,
  `nbr_points` int(11) NOT NULL,
  `question` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `quiz`
--

CREATE TABLE `quiz` (
  `id` int(11) NOT NULL,
  `title` varchar(255) NOT NULL,
  `difficulty` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `quiz_completion`
--

CREATE TABLE `quiz_completion` (
  `id` int(11) NOT NULL,
  `quiz_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `completed` tinyint(1) NOT NULL,
  `can_retake` tinyint(1) NOT NULL,
  `completed_at` datetime NOT NULL COMMENT '(DC2Type:datetime_immutable)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `rating`
--

CREATE TABLE `rating` (
  `id` int(11) NOT NULL,
  `student_id` int(11) NOT NULL,
  `examen_id` int(11) NOT NULL,
  `stars` int(11) NOT NULL,
  `created_at` datetime NOT NULL COMMENT '(DC2Type:datetime_immutable)',
  `feedback` longtext DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `reponses`
--

CREATE TABLE `reponses` (
  `id` int(11) NOT NULL,
  `questions_id` int(11) DEFAULT NULL,
  `reponse` varchar(255) NOT NULL,
  `etat` tinyint(1) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `responces`
--

CREATE TABLE `responces` (
  `id` int(11) NOT NULL,
  `id_forum_id` int(11) NOT NULL,
  `id_user_id` int(11) NOT NULL,
  `comment` longtext NOT NULL,
  `media` varchar(255) DEFAULT NULL,
  `type_media` varchar(255) DEFAULT NULL,
  `date_time` datetime NOT NULL,
  `rate` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `responces`
--

INSERT INTO `responces` (`id`, `id_forum_id`, `id_user_id`, `comment`, `media`, `type_media`, `date_time`, `rate`) VALUES
(1, 1, 2, 'SALUT MON ENFANT', '', '', '2025-02-19 00:13:37', 0);

-- --------------------------------------------------------

--
-- Structure de la table `resultat_quiz`
--

CREATE TABLE `resultat_quiz` (
  `id` int(11) NOT NULL,
  `examen_id` int(11) NOT NULL,
  `score` int(11) NOT NULL,
  `total_points` int(11) NOT NULL,
  `date_passage` datetime NOT NULL,
  `id_user_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `user`
--

CREATE TABLE `user` (
  `id` int(11) NOT NULL,
  `name` varchar(128) NOT NULL,
  `email` varchar(128) NOT NULL,
  `phone` int(11) NOT NULL,
  `type` varchar(255) NOT NULL,
  `rate` double NOT NULL,
  `date_of_birth` date NOT NULL,
  `password` varchar(128) NOT NULL,
  `img` varchar(128) NOT NULL,
  `score` int(11) NOT NULL,
  `bio` varchar(255) NOT NULL DEFAULT 'Vous n''avez pas encore de bio',
  `verified` smallint(6) NOT NULL DEFAULT 0,
  `google_id` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `user`
--

INSERT INTO `user` (`id`, `name`, `email`, `phone`, `type`, `rate`, `date_of_birth`, `password`, `img`, `score`, `bio`, `verified`, `google_id`) VALUES
(2, 'oi', 'nigga@gmail.com', 21212112, 'admin', 0, '1925-01-01', '$2y$13$eOAlvCYxgrOaG.fchSc0B.g0idOjGE3IyPha0dcKZEIJQLj9efZs2', '67b512db3f073.jpg', 0, 'Vous n\'avez pas encore de bio', 0, NULL),
(101, 'Yasine Ben Rbiaa', 'ademdh112@gmail.com', 0, 'teacher', 0, '1925-01-01', '$2y$13$wZSxyR8VeZOhiNeOIq35l.DrS1BWNqgygEU/UA3bTP7s5FIKG8pHi', '67b63f234b0b4.jpg', 0, 'Vous n\'avez pas encore de bio', 0, NULL),
(102, 'Mustapha Aziz Belkadhi', 'Mustapha123@gmail.com', 0, 'student', 0, '1925-01-01', '$2y$13$x6ZmK330nAeHBJHEHjROzOZY5I6Frh7mh68sTU/.O7TE.VWlM0nKy', '67b650f7d2254.jpg', 0, 'Vous n\'avez pas encore de bio', 1, NULL),
(103, 'Teacher1414', 'Yasine12314@gmail.com', 0, 'student', 0, '1925-01-01', '$2y$13$Dpd7kdLjktVUosmGA9OZQeqD9zU6kAW/JIOj/7CPXb0ep23TPj7tC', '67b6519046a39.jpg', 0, 'Vous n\'avez pas encore de bio', 0, NULL),
(104, 'Errorserrors', 'Errorserrors@gmail.com', 0, 'student', 0, '1925-01-01', '$2y$13$hcHvI633N3S1J0vAWh6uF.geUtWx0Ra1bLOD9v4qqjmhJsOQv/W9m', '67b651b5749ce.png', 0, 'Vous n\'avez pas encore de bio', 0, NULL),
(105, 'ErrorserrorsErrorserrorsErrorserrors', 'Yasine123@gmail.com', 0, 'teacher', 0, '1925-01-01', '$2y$13$WDEYrG/DuSy6AooNXoa4GO8n/5khuiKyj99ttjtB85eV1im8Jlh4.', '67b651e6eb528.png', 0, 'Vous n\'avez pas encore de bio', 0, NULL),
(106, 'Adem Dhouib', 'Adem1234@gmail.com', 0, 'teacher', 0, '1925-01-01', '$2y$13$g1sUkWBKFp4215mfKs7VS.NVx31Jrt5rcP2QBXLKYmnLwssv35aya', '67b6536838c4c.jpg', 0, 'Vous n\'avez pas encore de bio', 0, NULL),
(107, 'Test6998745', 'Test6998745@gmail.com', 0, 'teacher', 0, '1925-01-01', '$2y$13$cTzZqmivDTLmvdOWSd1Q/u14n6I5iGp3Dg.ApLTn.3jd51VU9AW6K', '67b653bbe83f0.png', 0, 'Vous n\'avez pas encore de bio', 0, NULL),
(108, 'Test69987455', 'Test69987455@gmail.com', 0, 'teacher', 0, '1925-01-01', '$2y$13$Vkt7rXQ.RTsOiEkNSls7rOkzlTsBuog2L940NwbWK3W4.vv9KjvXO', '67b653e3b6eae.png', 0, 'Vous n\'avez pas encore de bio', 0, NULL),
(109, 'Test699874556', 'Test699874556@gmail.com', 0, 'teacher', 0, '1925-01-01', '$2y$13$x6pcN/TQpV9dSPu.V5VmuO6vXURdsws6nMbTExuC/LxWvqkTERHja', '67b6544590916.jpg', 0, 'Vous n\'avez pas encore de bio', 0, NULL),
(110, 'Mustapha Aziz Belkadhi', 'Mustapha123@gmail.com', 0, 'teacher', 0, '1925-01-01', '$2y$13$fXNvh86i1d7CvcDl6QMisuDLljUwhPYLubQczZdU8vK7TYODHLR7.', '67b654f77f416.png', 0, 'Vous n\'avez pas encore de bio', 1, NULL),
(111, 'Yasine123Yasine123', 'Yasine123@gmail.com', 0, 'teacher', 0, '1925-01-01', '$2y$13$KeLO2qJIY61JI2ig5L2.IemT.n5vjbLpC2HEc8Za.o50/0qOV6tNm', '67b65540e2fea.jpg', 0, 'Vous n\'avez pas encore de bio', 0, NULL),
(112, 'Mustapha Aziz Belkadhi', 'workenxaimelespatat@gmail.com', 0, 'teacher', 0, '1925-01-01', '$2y$13$rOi4WmvemFXz3NZCO96jpeiHDyDQ6ucnjPgD1j9BQtk3IsbS8sxMW', '67b65f7178472.jpg', 0, 'Vous n\'avez pas encore de bio', 1, NULL),
(114, '_WorKenX_', 'workenxaimelespatat@gmail.com', 0, 'student', 0, '2000-01-01', '', 'workenxaimelespatat-at-gmail-67c39258a356a.jpg', 0, 'Vous n\'avez pas encore de bio', 1, NULL),
(116, 'Lyaaqa', 'lyaaqaoff@gmail.com', 0, 'student', 0, '2000-01-01', '', 'default.jpg', 0, 'Vous n\'avez pas encore de bio', 1, NULL),
(117, 'Aziz zizou', 'workenxaimelesboobs@gmail.com', 0, 'student', 0, '2000-01-01', '', 'workenxaimelesboobs-at-gmail-67c4533dd4af1.jpg', 0, 'Vous n\'avez pas encore de bio', 1, NULL),
(119, 'HAmouda77', 'hamoudabenabdennebi77@gmail.com', 0, 'teacher', 0, '1925-01-01', '$2y$13$1Ia7NabZDd6fuS7/2wEfZ.nE0khaQ/mmHfHOjG92bdHLBrXd4AyFS', 'user_119.png', 0, 'Vous n\'avez pas encore de bio', 1, NULL),
(120, '_WorKenX_', 'workenxaimelespatat@gmail.com', 0, 'student', 0, '2000-01-01', '', 'workenxaimelespatat-at-gmail-67c7b266c6f30.jpg', 0, 'Vous n\'avez pas encore de bio', 1, '106663907517130208705'),
(121, 'Test User 5096cf32', 'test1744726013631@example.com', 0, 'regular', 0, '2025-04-15', 'password123', 'default.jpg', 0, 'Vous n\'avez pas encore de bio', 0, NULL);

--
-- Index pour les tables déchargées
--

--
-- Index pour la table `course`
--
ALTER TABLE `course`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_169E6FB9A76ED395` (`user_id`);

--
-- Index pour la table `discussion`
--
ALTER TABLE `discussion`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_C0B9F90F71F7E88B` (`event_id`);

--
-- Index pour la table `doctrine_migration_versions`
--
ALTER TABLE `doctrine_migration_versions`
  ADD PRIMARY KEY (`version`);

--
-- Index pour la table `evenement`
--
ALTER TABLE `evenement`
  ADD PRIMARY KEY (`id`);

--
-- Index pour la table `evenement_user`
--
ALTER TABLE `evenement_user`
  ADD PRIMARY KEY (`evenement_id`,`user_id`),
  ADD KEY `IDX_2EC0B3C4FD02F13` (`evenement_id`),
  ADD KEY `IDX_2EC0B3C4A76ED395` (`user_id`);

--
-- Index pour la table `examen`
--
ALTER TABLE `examen`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_514C8FEC79F37AE5` (`id_user_id`);

--
-- Index pour la table `feedback`
--
ALTER TABLE `feedback`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_D22944585C8659A` (`examen_id`),
  ADD KEY `IDX_D2294458A76ED395` (`user_id`);

--
-- Index pour la table `fnotifications`
--
ALTER TABLE `fnotifications`
  ADD PRIMARY KEY (`id`);

--
-- Index pour la table `forum`
--
ALTER TABLE `forum`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_852BBECD79F37AE5` (`id_user_id`);

--
-- Index pour la table `item`
--
ALTER TABLE `item`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_1F1B251ECDF80196` (`lesson_id`);

--
-- Index pour la table `lesson`
--
ALTER TABLE `lesson`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_F87474F3591CC992` (`course_id`);

--
-- Index pour la table `messenger_messages`
--
ALTER TABLE `messenger_messages`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_75EA56E0FB7336F0` (`queue_name`),
  ADD KEY `IDX_75EA56E0E3BD61CE` (`available_at`),
  ADD KEY `IDX_75EA56E016BA31DB` (`delivered_at`);

--
-- Index pour la table `notification`
--
ALTER TABLE `notification`
  ADD PRIMARY KEY (`id`);

--
-- Index pour la table `questions`
--
ALTER TABLE `questions`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_8ADC54D55C8659A` (`examen_id`);

--
-- Index pour la table `quiz`
--
ALTER TABLE `quiz`
  ADD PRIMARY KEY (`id`);

--
-- Index pour la table `quiz_completion`
--
ALTER TABLE `quiz_completion`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_71B91042853CD175` (`quiz_id`),
  ADD KEY `IDX_71B91042A76ED395` (`user_id`);

--
-- Index pour la table `rating`
--
ALTER TABLE `rating`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_D8892622CB944F1A` (`student_id`),
  ADD KEY `IDX_D88926225C8659A` (`examen_id`);

--
-- Index pour la table `reponses`
--
ALTER TABLE `reponses`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_1E512EC6BCB134CE` (`questions_id`);

--
-- Index pour la table `responces`
--
ALTER TABLE `responces`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_2D793CE479175645` (`id_forum_id`),
  ADD KEY `IDX_2D793CE479F37AE5` (`id_user_id`);

--
-- Index pour la table `resultat_quiz`
--
ALTER TABLE `resultat_quiz`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_2A776B35C8659A` (`examen_id`),
  ADD KEY `IDX_2A776B379F37AE5` (`id_user_id`);

--
-- Index pour la table `user`
--
ALTER TABLE `user`
  ADD PRIMARY KEY (`id`);

--
-- AUTO_INCREMENT pour les tables déchargées
--

--
-- AUTO_INCREMENT pour la table `course`
--
ALTER TABLE `course`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `discussion`
--
ALTER TABLE `discussion`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `evenement`
--
ALTER TABLE `evenement`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `examen`
--
ALTER TABLE `examen`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `feedback`
--
ALTER TABLE `feedback`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `fnotifications`
--
ALTER TABLE `fnotifications`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `forum`
--
ALTER TABLE `forum`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT pour la table `item`
--
ALTER TABLE `item`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `lesson`
--
ALTER TABLE `lesson`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `messenger_messages`
--
ALTER TABLE `messenger_messages`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `notification`
--
ALTER TABLE `notification`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `questions`
--
ALTER TABLE `questions`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `quiz`
--
ALTER TABLE `quiz`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `quiz_completion`
--
ALTER TABLE `quiz_completion`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `rating`
--
ALTER TABLE `rating`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `reponses`
--
ALTER TABLE `reponses`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `responces`
--
ALTER TABLE `responces`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT pour la table `resultat_quiz`
--
ALTER TABLE `resultat_quiz`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `user`
--
ALTER TABLE `user`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=122;

--
-- Contraintes pour les tables déchargées
--

--
-- Contraintes pour la table `course`
--
ALTER TABLE `course`
  ADD CONSTRAINT `FK_169E6FB9A76ED395` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

--
-- Contraintes pour la table `discussion`
--
ALTER TABLE `discussion`
  ADD CONSTRAINT `FK_C0B9F90F71F7E88B` FOREIGN KEY (`event_id`) REFERENCES `evenement` (`id`);

--
-- Contraintes pour la table `evenement_user`
--
ALTER TABLE `evenement_user`
  ADD CONSTRAINT `FK_2EC0B3C4A76ED395` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `FK_2EC0B3C4FD02F13` FOREIGN KEY (`evenement_id`) REFERENCES `evenement` (`id`) ON DELETE CASCADE;

--
-- Contraintes pour la table `examen`
--
ALTER TABLE `examen`
  ADD CONSTRAINT `FK_514C8FEC79F37AE5` FOREIGN KEY (`id_user_id`) REFERENCES `user` (`id`);

--
-- Contraintes pour la table `feedback`
--
ALTER TABLE `feedback`
  ADD CONSTRAINT `FK_D22944585C8659A` FOREIGN KEY (`examen_id`) REFERENCES `examen` (`id`),
  ADD CONSTRAINT `FK_D2294458A76ED395` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

--
-- Contraintes pour la table `forum`
--
ALTER TABLE `forum`
  ADD CONSTRAINT `FK_852BBECD79F37AE5` FOREIGN KEY (`id_user_id`) REFERENCES `user` (`id`);

--
-- Contraintes pour la table `item`
--
ALTER TABLE `item`
  ADD CONSTRAINT `FK_1F1B251ECDF80196` FOREIGN KEY (`lesson_id`) REFERENCES `lesson` (`id`);

--
-- Contraintes pour la table `lesson`
--
ALTER TABLE `lesson`
  ADD CONSTRAINT `FK_F87474F3591CC992` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`);

--
-- Contraintes pour la table `questions`
--
ALTER TABLE `questions`
  ADD CONSTRAINT `FK_8ADC54D55C8659A` FOREIGN KEY (`examen_id`) REFERENCES `examen` (`id`);

--
-- Contraintes pour la table `quiz_completion`
--
ALTER TABLE `quiz_completion`
  ADD CONSTRAINT `FK_71B91042853CD175` FOREIGN KEY (`quiz_id`) REFERENCES `quiz` (`id`),
  ADD CONSTRAINT `FK_71B91042A76ED395` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

--
-- Contraintes pour la table `rating`
--
ALTER TABLE `rating`
  ADD CONSTRAINT `FK_D88926225C8659A` FOREIGN KEY (`examen_id`) REFERENCES `examen` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `FK_D8892622CB944F1A` FOREIGN KEY (`student_id`) REFERENCES `user` (`id`);

--
-- Contraintes pour la table `reponses`
--
ALTER TABLE `reponses`
  ADD CONSTRAINT `FK_1E512EC6BCB134CE` FOREIGN KEY (`questions_id`) REFERENCES `questions` (`id`);

--
-- Contraintes pour la table `responces`
--
ALTER TABLE `responces`
  ADD CONSTRAINT `FK_2D793CE479175645` FOREIGN KEY (`id_forum_id`) REFERENCES `forum` (`id`),
  ADD CONSTRAINT `FK_2D793CE479F37AE5` FOREIGN KEY (`id_user_id`) REFERENCES `user` (`id`);

--
-- Contraintes pour la table `resultat_quiz`
--
ALTER TABLE `resultat_quiz`
  ADD CONSTRAINT `FK_2A776B35C8659A` FOREIGN KEY (`examen_id`) REFERENCES `examen` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `FK_2A776B379F37AE5` FOREIGN KEY (`id_user_id`) REFERENCES `user` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
