-- MySQL dump 10.13  Distrib 8.0.28, for Win64 (x86_64)
--
-- Host: localhost    Database: finalproject
-- ------------------------------------------------------
-- Server version	8.0.28

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `customers`
--

DROP TABLE IF EXISTS `customers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `customers` (
  `CUSTOMER_ID` int NOT NULL AUTO_INCREMENT,
  `CUSTOMER_NAME` varchar(100) NOT NULL,
  `ACCOUNT_NUMBER` mediumtext NOT NULL,
  `IDENTITY_TYPE` varchar(50) NOT NULL,
  `IDENTITY_NUMBER` varchar(50) NOT NULL,
  `DATE_OF_BIRTH` date DEFAULT NULL,
  `MOBILE_NUMBER` mediumtext NOT NULL,
  `EMAIL_ID` varchar(50) NOT NULL,
  `ADDRESS` varchar(50) NOT NULL,
  `SEX` varchar(20) NOT NULL,
  PRIMARY KEY (`CUSTOMER_ID`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `customers`
--

LOCK TABLES `customers` WRITE;
/*!40000 ALTER TABLE `customers` DISABLE KEYS */;
INSERT INTO `customers` VALUES (1,'SURAJ CHAKRABORTY','2271467230249','ADHAAR','949998822905','1998-01-22','9790704961','surajchakraborty82@gmail.com','BARDHAMAN,WEST BENGAL,PIN-713408','MALE'),(2,'SHIVAM GAUTAM','2274673029721','PAN','BHTPC5200K','1997-05-19','7582926135','shivamgautam727@gmail.com','GUNA,MADHYA PRADESH,PIN-721526','MALE'),(3,'RITU ROY','2564437290784','ADHAAR','928495623701','1990-09-27','7809550289','rituroy782@gmail.com','ALWAR,RAJASTHAN,PIN-713532','FEMALE'),(4,'PRIYANKA SHARMA','2764325890167','ADHAAR','972183903726','1996-07-15','9320865274','priyankasharma15@gmail.com','KANPUR,UTTAR PRADESH,PIN-743526','FEMALE'),(5,'PRITAM CHAUHAN','2962380792164','PAN','APSBK3729L','1994-03-20','9790704961','pc2986@gmail.com','MUMBAI,MAHARASTRA,PIN-732841','MALE');
/*!40000 ALTER TABLE `customers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hibernate_sequence`
--

DROP TABLE IF EXISTS `hibernate_sequence`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `hibernate_sequence` (
  `next_val` bigint DEFAULT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hibernate_sequence`
--

LOCK TABLES `hibernate_sequence` WRITE;
/*!40000 ALTER TABLE `hibernate_sequence` DISABLE KEYS */;
INSERT INTO `hibernate_sequence` VALUES (1);
/*!40000 ALTER TABLE `hibernate_sequence` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `transactions`
--

DROP TABLE IF EXISTS `transactions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `transactions` (
  `serial_number` int NOT NULL AUTO_INCREMENT,
  `customer_id` int NOT NULL,
  `initial_deposit` int NOT NULL,
  `available_balance` int NOT NULL,
  `transaction_mode` varchar(45) NOT NULL,
  `transaction_id` varchar(45) NOT NULL,
  `transaction_amount` int NOT NULL,
  `transaction_to` varchar(45) NOT NULL,
  `transaction_date` date DEFAULT NULL,
  `transaction_type` varchar(45) NOT NULL,
  PRIMARY KEY (`serial_number`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `transactions`
--

LOCK TABLES `transactions` WRITE;
/*!40000 ALTER TABLE `transactions` DISABLE KEYS */;
INSERT INTO `transactions` VALUES (1,1,100000,30000,'CHEQUE','949tfZ88Kp2G05',70000,'PRIYA BANSAL','2022-01-22','Debit'),(2,3,120000,40000,'NEFT','95ltf28hKp2G05',80000,'MAHADEV VERMA','2022-01-24','Debit'),(3,3,40000,32000,'UPI','9K52fA9cKp2L75',8000,'KISHAN AGARWAL','2022-01-28','Debit'),(4,4,220000,20000,'RTGS','91GfB28Mn20lo9',200000,'SURESH VERMA','2022-02-02','Debit'),(5,5,440000,420000,'UPI','95K7B28p204lA7',20000,'MANINDER KAUR','2022-02-05','Debit'),(6,4,20000,7000,'UPI','92G84P53Wn20o9',13000,'VIVEK SINGH','2022-02-15','Debit'),(7,5,420000,220000,'RTGS','9X3F82kCy80La6',200000,'VIKRAM MOHANTY','2022-02-10','Debit'),(8,2,210000,10000,'CHEQUE','91rtB3M72AbY9',200000,'VIKAS AWASTI','2022-02-14','Debit'),(9,1,30000,25000,'UPI','98K2n4U82R2Ga5',5000,'PRATAP ROY','2022-02-13','Debit'),(10,2,10000,2500,'UPI','9K2BfH49232Gv7',7500,'PRATAP ROY','2022-02-17','Debit'),(11,1,25000,15000,'NEFT','9K24tZ83Qs3M75',10000,'SUMAN YADAV','2022-02-25','Debit'),(12,4,7000,27000,'UPI','98B2kL56Tv7M30',20000,'SELF','2022-03-09','Credit');
/*!40000 ALTER TABLE `transactions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` int NOT NULL AUTO_INCREMENT,
  `Active` varchar(100) NOT NULL,
  `Password` mediumtext NOT NULL,
  `Roles` varchar(50) NOT NULL,
  `user_name` varchar(50) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'2','pass','ROLE_USER','user'),(2,'1','Suraj@101998','ROLE_ADMIN','Suraj');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2022-12-13 22:49:48
