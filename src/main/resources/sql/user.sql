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
-- Index pour la table `user`
--
ALTER TABLE `user`
  ADD PRIMARY KEY (`id`);

--
-- AUTO_INCREMENT pour les tables déchargées
--

--
-- AUTO_INCREMENT pour la table `user`
--
ALTER TABLE `user`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=122;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
