-- MySQL dump 10.15  Distrib 10.0.15-MariaDB, for debian-linux-gnu (x86_64)
--
-- Host: localhost    Database: tags
-- ------------------------------------------------------
-- Server version	10.0.15-MariaDB-1~trusty-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `Artist`
--

DROP TABLE IF EXISTS `Artist`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Artist` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `Name` varchar(150) NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Artist`
--

LOCK TABLES `Artist` WRITE;
/*!40000 ALTER TABLE `Artist` DISABLE KEYS */;
INSERT INTO `Artist` VALUES (1,'Hudson Mohawke'),(2,'Jorge Negrete'),(3,'Tiger Lou'),(4,'Waldemar Bastos'),(5,'Lena Philipsson'),(6,'Shawn Colvin'),(7,'Dying Fetus'),(8,'Emery');
/*!40000 ALTER TABLE `Artist` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `TT`
--

DROP TABLE IF EXISTS `TT`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `TT` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `TrackID` int(11) NOT NULL,
  `TagID` int(11) NOT NULL,
  `Count` int(11) NOT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `TrackID` (`TrackID`,`TagID`),
  KEY `TagID` (`TagID`),
  CONSTRAINT `TT_ibfk_1` FOREIGN KEY (`TrackID`) REFERENCES `Track` (`ID`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `TT_ibfk_2` FOREIGN KEY (`TagID`) REFERENCES `Tag` (`ID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=71 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `TT`
--

LOCK TABLES `TT` WRITE;
/*!40000 ALTER TABLE `TT` DISABLE KEYS */;
INSERT INTO `TT` VALUES (1,1,1,100),(2,1,2,80),(3,1,3,40),(4,1,4,40),(5,1,5,40),(6,1,6,20),(7,1,7,20),(8,1,8,20),(9,1,9,20),(10,1,10,20),(11,1,11,20),(12,1,12,20),(13,2,13,100),(14,2,14,100),(15,2,15,50),(16,2,16,50),(17,2,17,50),(18,2,18,50),(19,2,19,50),(20,2,20,50),(21,2,21,50),(22,2,22,50),(23,2,23,50),(24,2,24,50),(25,2,25,50),(26,2,26,50),(27,3,27,100),(28,4,28,100),(29,4,29,50),(30,4,30,50),(31,4,31,50),(32,4,32,50),(33,4,33,50),(34,4,34,50),(35,4,35,50),(36,4,36,50),(37,4,37,50),(38,4,38,50),(39,4,39,50),(40,5,40,100),(41,5,41,100),(42,5,42,100),(43,5,43,100),(44,6,44,100),(45,7,45,100),(46,7,46,75),(47,7,47,37),(48,7,48,25),(49,7,49,12),(50,7,50,12),(51,7,51,12),(52,7,52,12),(53,7,53,12),(54,8,54,100),(55,8,55,66),(56,8,56,55),(57,8,57,55),(58,8,58,44),(59,8,59,11),(60,8,60,11),(61,8,61,11),(62,8,62,11),(63,8,63,11),(64,8,64,11),(65,8,65,11),(66,8,66,11),(67,8,67,11),(68,8,68,11),(69,8,69,11),(70,8,70,0);
/*!40000 ALTER TABLE `TT` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Tag`
--

DROP TABLE IF EXISTS `Tag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Tag` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `Name` varchar(150) NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=71 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Tag`
--

LOCK TABLES `Tag` WRITE;
/*!40000 ALTER TABLE `Tag` DISABLE KEYS */;
INSERT INTO `Tag` VALUES (1,'chipmunks on synths'),(2,'instrumental hip-hop'),(3,'Hip-Hop'),(4,'experimental'),(5,'themdrums'),(6,'electronic'),(7,'Avant-Garde'),(8,'dubstep'),(9,'garagedancing'),(10,'hiphop garagedancing'),(11,'streamable track wants'),(12,'TurntableFm01'),(13,'ranchera'),(14,'El Hijo del Pueblo'),(15,'latin'),(16,'bolero'),(17,'mestiza'),(18,'pateticos'),(19,'Lo Mejor de Jorge Negrete'),(20,'Vozbrillante'),(21,'Cafeteria La Sirena'),(22,'outskirts of expansion'),(23,'familiarity'),(24,'ran5'),(25,'perelmarinete'),(26,'Mexico lindo y Querido'),(27,'getragen'),(28,'africa'),(29,'chillout'),(30,'world'),(31,'favorit'),(32,'fado'),(33,'angola'),(34,'nastrojowe'),(35,'blandband'),(36,'africana'),(37,'chechornia'),(38,'lunajavkia'),(39,'double-ender 8'),(40,'swedish'),(41,'pop'),(42,'1991'),(43,'rakt over disc'),(44,'cover'),(45,'Technical Death Metal'),(46,'Brutal Death Metal'),(47,'death metal'),(48,'grindcore'),(49,'metal'),(50,'death'),(51,'Technical Brutal Death Metal'),(52,'Ethos Of Coercion'),(53,'nosz kurwa jakie stopy'),(54,'post-hardcore'),(55,'emo'),(56,'rock'),(57,'emocore'),(58,'hardcore'),(59,'CHristian Puck'),(60,'rendon'),(61,'Emery Live'),(62,'Bone Palace Ballet Tour'),(63,'acadia'),(64,'beastcore'),(65,'bam'),(66,'screaming'),(67,'00s'),(68,'FUCKING AWESOME'),(69,'screamo'),(70,'seen live');
/*!40000 ALTER TABLE `Tag` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Track`
--

DROP TABLE IF EXISTS `Track`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Track` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `Name` varchar(150) NOT NULL,
  `ArtistID` int(11) NOT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `Name` (`Name`,`ArtistID`),
  KEY `ArtistID` (`ArtistID`),
  CONSTRAINT `Track_ibfk_1` FOREIGN KEY (`ArtistID`) REFERENCES `Artist` (`ID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Track`
--

LOCK TABLES `Track` WRITE;
/*!40000 ALTER TABLE `Track` DISABLE KEYS */;
INSERT INTO `Track` VALUES (6,'(Looking For) The Heart Of Saturday',6),(5,'006',5),(2,'El hijo del pueblo',2),(7,'Ethos of Coercion',7),(4,'N Gana',4),(1,'No One Could Ever',1),(3,'Pilots',3),(8,'Rock-N-Rule',8);
/*!40000 ALTER TABLE `Track` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2015-01-05 13:57:25
