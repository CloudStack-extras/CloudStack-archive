-- MySQL dump 10.11
--
-- Host: localhost    Database: cloud
-- ------------------------------------------------------
-- Server version	5.0.77

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
-- Table structure for table `account`
--

DROP TABLE IF EXISTS `account`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `account` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `account_name` varchar(100) default NULL COMMENT 'an account name set by the creator of the account, defaults to username for single accounts',
  `type` int(1) unsigned NOT NULL,
  `domain_id` bigint(20) unsigned default NULL,
  `state` varchar(10) NOT NULL default 'enabled',
  `removed` datetime default NULL COMMENT 'date removed',
  `cleanup_needed` tinyint(1) NOT NULL default '0',
  PRIMARY KEY  (`id`),
  KEY `i_account__domain_id` (`domain_id`),
  KEY `i_account__cleanup_needed` (`cleanup_needed`),
  KEY `i_account__removed` (`removed`),
  KEY `i_account__account_name__domain_id__removed` (`account_name`,`domain_id`,`removed`),
  CONSTRAINT `fk_account__domain_id` FOREIGN KEY (`domain_id`) REFERENCES `domain` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=71 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `account`
--

LOCK TABLES `account` WRITE;
/*!40000 ALTER TABLE `account` DISABLE KEYS */;
INSERT INTO `account` VALUES (1,'system',1,1,'enabled',NULL,0),(2,'admin',1,1,'enabled',NULL,0),(3,'postpaid',2,3,'enabled',NULL,0),(4,'postpaid',2,4,'enabled',NULL,0),(5,'terminate',2,6,'enabled','2011-02-10 10:55:02',0),(6,'terminate',2,7,'enabled','2011-02-10 10:54:36',1),(7,'terminate',2,8,'enabled',NULL,0),(8,'mkashikar',2,9,'enabled',NULL,0),(9,'marankalle',2,10,'enabled',NULL,0),(10,'pminc',2,11,'enabled','2011-02-10 23:38:41',0),(11,'pminc2',2,12,'disabled','2011-02-10 23:57:05',0),(12,'pminc2',2,13,'enabled',NULL,0),(13,'dnoland',2,14,'enabled',NULL,0),(14,'manish',2,15,'enabled',NULL,0),(15,'padmin',2,16,'enabled',NULL,0),(16,'suspension',2,17,'enabled',NULL,0),(17,'dnoland',2,18,'enabled',NULL,0),(18,'msquare',2,19,'enabled',NULL,0),(19,'opsadmin',2,1,'enabled',NULL,0),(20,'bdone',2,20,'enabled',NULL,0),(21,'bdone',2,21,'enabled',NULL,0),(22,'bdoneuser',0,21,'enabled',NULL,0),(23,'bdone',2,22,'enabled',NULL,0),(24,'bdtwouser',0,22,'enabled',NULL,0),(25,'dnoland1',2,23,'enabled',NULL,0),(26,'locktest',2,24,'disabled','2011-02-23 09:40:37',0),(27,'locktest',2,25,'disabled','2011-02-23 10:38:29',0),(28,'locktest',2,26,'disabled','2011-02-23 10:38:29',0),(29,'lockadmin',2,27,'disabled','2011-02-23 10:38:28',0),(30,'lockuser',0,27,'disabled','2011-02-23 10:38:28',0),(31,'locktest',2,28,'enabled',NULL,0),(32,'locktest',2,29,'enabled',NULL,0),(33,'locksecond',0,29,'enabled',NULL,0),(34,'gplains',2,30,'enabled',NULL,0),(35,'zynga',2,32,'enabled',NULL,0),(36,'zynga',2,33,'enabled',NULL,0),(37,'zynga',2,34,'enabled',NULL,0),(38,'Zuora-Msquare',2,35,'enabled',NULL,0),(39,'partnetisisue',2,36,'enabled',NULL,0),(40,'cindy',2,37,'enabled',NULL,0),(41,'agarwal123',2,38,'enabled',NULL,0),(42,'shweta',2,39,'enabled',NULL,0),(43,'shweta',2,40,'enabled',NULL,0),(44,'shweta',2,41,'enabled',NULL,0),(45,'manish123',2,42,'enabled',NULL,0),(46,'manish123',2,43,'enabled',NULL,0),(47,'manish123',2,44,'enabled',NULL,0),(48,'manish12',0,44,'enabled',NULL,0),(49,'test1',0,40,'enabled',NULL,0),(50,'test2',0,40,'enabled',NULL,0),(51,'test2',2,45,'enabled',NULL,0),(52,'Koteswar',2,46,'enabled',NULL,0),(53,'murali',0,21,'enabled',NULL,0),(54,'atmos',2,47,'enabled',NULL,0),(55,'atmos',2,48,'enabled',NULL,0),(56,'atmosuser',0,48,'enabled',NULL,0),(57,'pmorris',2,49,'enabled',NULL,0),(58,'pmorris',2,50,'enabled',NULL,0),(59,'pmorris',2,51,'enabled',NULL,0),(60,'pmorris',2,52,'enabled',NULL,0),(61,'pauluser',0,52,'enabled',NULL,0),(62,'magarwal',2,53,'enabled',NULL,0),(63,'manishtest',2,54,'enabled',NULL,0),(64,'dnoland2',2,55,'enabled',NULL,0),(65,'kkagarwal',2,56,'enabled',NULL,0),(66,'kkagarwal',2,57,'enabled',NULL,0),(67,'kkuser',0,57,'enabled',NULL,0),(68,'dnoland2',2,58,'enabled',NULL,0),(69,'dnolandterm',2,59,'enabled','2011-05-16 17:52:14',0),(70,'deepa',2,60,'enabled',NULL,0);
/*!40000 ALTER TABLE `account` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `account_network_ref`
--

DROP TABLE IF EXISTS `account_network_ref`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `account_network_ref` (
  `id` bigint(20) unsigned NOT NULL auto_increment COMMENT 'id',
  `account_id` bigint(20) unsigned NOT NULL COMMENT 'account id',
  `network_id` bigint(20) unsigned NOT NULL COMMENT 'network id',
  `is_owner` smallint(1) NOT NULL COMMENT 'is the owner of the network',
  PRIMARY KEY  (`id`),
  KEY `fk_account_network_ref__account_id` (`account_id`),
  KEY `fk_account_network_ref__networks_id` (`network_id`),
  CONSTRAINT `fk_account_network_ref__account_id` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_account_network_ref__networks_id` FOREIGN KEY (`network_id`) REFERENCES `networks` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `account_network_ref`
--

LOCK TABLES `account_network_ref` WRITE;
/*!40000 ALTER TABLE `account_network_ref` DISABLE KEYS */;
INSERT INTO `account_network_ref` VALUES (1,1,200,1),(2,1,201,1),(3,1,202,1),(4,1,203,1),(5,4,204,1),(6,2,205,1),(7,6,206,1),(8,24,207,1),(9,22,208,1),(10,26,209,1),(11,30,210,1),(12,20,211,1),(13,3,212,1),(14,42,213,1),(15,51,214,1),(16,56,215,1),(17,61,216,1),(18,60,217,1),(19,1,218,1),(20,1,219,1),(21,1,220,1),(22,1,221,1),(23,2,222,1),(24,69,223,1),(25,70,224,1);
/*!40000 ALTER TABLE `account_network_ref` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `account_vlan_map`
--

DROP TABLE IF EXISTS `account_vlan_map`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `account_vlan_map` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `account_id` bigint(20) unsigned NOT NULL COMMENT 'account id. foreign key to account table',
  `vlan_db_id` bigint(20) unsigned NOT NULL COMMENT 'database id of vlan. foreign key to vlan table',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`),
  KEY `i_account_vlan_map__account_id` (`account_id`),
  KEY `i_account_vlan_map__vlan_id` (`vlan_db_id`),
  CONSTRAINT `fk_account_vlan_map__account_id` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_account_vlan_map__vlan_id` FOREIGN KEY (`vlan_db_id`) REFERENCES `vlan` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `account_vlan_map`
--

LOCK TABLES `account_vlan_map` WRITE;
/*!40000 ALTER TABLE `account_vlan_map` DISABLE KEYS */;
/*!40000 ALTER TABLE `account_vlan_map` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `alert`
--

DROP TABLE IF EXISTS `alert`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `alert` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `type` int(1) unsigned NOT NULL,
  `pod_id` bigint(20) unsigned default NULL,
  `data_center_id` bigint(20) unsigned NOT NULL,
  `subject` varchar(999) default NULL COMMENT 'according to SMTP spec, max subject length is 1000 including the CRLF character, so allow enough space to fit long pod/zone/host names',
  `sent_count` int(3) unsigned NOT NULL,
  `created` datetime default NULL COMMENT 'when this alert type was created',
  `last_sent` datetime default NULL COMMENT 'Last time the alert was sent',
  `resolved` datetime default NULL COMMENT 'when the alert status was resolved (available memory no longer at critical level, etc.)',
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=258 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `alert`
--

LOCK TABLES `alert` WRITE;
/*!40000 ALTER TABLE `alert` DISABLE KEYS */;
INSERT INTO `alert` VALUES (1,13,0,0,'Management server node 192.168.130.151 is up',1,'2011-02-10 05:28:53','2011-02-10 05:28:53',NULL),(2,13,0,0,'Management server node 192.168.130.151 is up',1,'2011-02-10 05:37:07','2011-02-10 05:37:07',NULL),(3,13,0,0,'Management server node 192.168.130.151 is up',1,'2011-02-10 06:42:23','2011-02-10 06:42:23',NULL),(4,13,0,0,'Management server node 192.168.130.151 is up',1,'2011-02-10 12:07:50','2011-02-10 12:07:50',NULL),(5,7,1,1,'VM (name: i-4-3-VM, id: 3) stopped unexpectedly on host name: ezh-sit-xen1.vmopsdev.net(id:1), availability zone: Zone 1, pod: Pod 1',1,'2011-02-10 12:07:58','2011-02-10 12:07:58',NULL),(6,13,0,0,'Management server node 192.168.130.151 is up',1,'2011-02-10 12:33:27','2011-02-10 12:33:27',NULL),(7,13,0,0,'Management server node 192.168.130.151 is up',1,'2011-02-10 12:47:46','2011-02-10 12:47:46',NULL),(8,13,0,0,'Management server node 192.168.130.151 is up',1,'2011-02-12 01:37:48','2011-02-12 01:37:48',NULL),(9,13,0,0,'Management server node 127.0.0.1 is up',1,'2011-02-15 00:20:06','2011-02-15 00:20:06',NULL),(10,6,1,1,'Host is down, name: ezh-sit-xen1.vmopsdev.net (id:1), availability zone: Zone 1, pod: Pod 1',1,'2011-02-15 23:54:55','2011-02-15 23:54:55','2011-05-16 22:24:25'),(11,9,1,1,'Unable to restart v-1-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:1), availability zone: Zone 1, pod: Pod 1',1,'2011-02-15 23:54:56','2011-02-15 23:54:56',NULL),(12,7,1,1,'Unable to restart s-2-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:1), availability zone: Zone 1, pod: Pod 1',1,'2011-02-15 23:54:56','2011-02-15 23:54:56',NULL),(13,7,1,1,'Unable to restart i-4-3-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:1), availability zone: Zone 1, pod: Pod 1',1,'2011-02-15 23:54:56','2011-02-15 23:54:56',NULL),(14,8,1,1,'Unable to restart r-4-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:1), availability zone: Zone 1, pod: Pod 1',1,'2011-02-15 23:54:56','2011-02-15 23:54:56',NULL),(15,7,1,1,'Unable to restart i-4-7-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:1), availability zone: Zone 1, pod: Pod 1',1,'2011-02-15 23:54:56','2011-02-15 23:54:56',NULL),(16,7,1,1,'Unable to restart i-4-8-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:1), availability zone: Zone 1, pod: Pod 1',1,'2011-02-15 23:54:56','2011-02-15 23:54:56',NULL),(17,7,1,1,'VM (name: i-4-3-VM, id: 3) stopped unexpectedly on host name: ezh-sit-xen1.vmopsdev.net(id:1), availability zone: Zone 1, pod: Pod 1',1,'2011-02-15 23:55:27','2011-02-15 23:55:27',NULL),(18,7,1,1,'VM (name: i-4-7-VM, id: 7) stopped unexpectedly on host name: ezh-sit-xen1.vmopsdev.net(id:1), availability zone: Zone 1, pod: Pod 1',1,'2011-02-15 23:55:28','2011-02-15 23:55:28',NULL),(19,7,1,1,'VM (name: i-4-8-VM, id: 8) stopped unexpectedly on host name: ezh-sit-xen1.vmopsdev.net(id:1), availability zone: Zone 1, pod: Pod 1',1,'2011-02-15 23:55:28','2011-02-15 23:55:28',NULL),(20,2,NULL,1,'System Alert: Low Available Storage in pod Pod 1 of availablity zone Zone 1',1,'2011-02-25 20:40:24','2011-02-25 20:40:24','2011-05-16 22:24:25'),(21,2,NULL,1,'System Alert: Low Available Storage in pod Pod 1 of availablity zone Zone 1',1,'2011-02-25 20:45:24','2011-02-25 20:45:24',NULL),(22,13,0,0,'Management server node 127.0.0.1 is up',1,'2011-03-01 21:35:00','2011-03-01 21:35:00',NULL),(23,18,1,1,'Secondary Storage Vm rebooted in zone: Zone 1, secStorageVm: s-2-VM, public IP: 66.149.231.114, private IP: 192.168.154.233',1,'2011-03-01 21:47:27','2011-03-01 21:47:27',NULL),(24,0,NULL,1,'System Alert: Low Available Memory in pod Pod 1 of availablity zone Zone 1',1,'2011-03-09 10:55:13','2011-03-09 10:55:13','2011-05-16 22:24:25'),(25,0,NULL,1,'System Alert: Low Available Memory in pod Pod 1 of availablity zone Zone 1',1,'2011-03-09 11:00:13','2011-03-09 11:00:13',NULL),(26,13,0,0,'Management server node 127.0.0.1 is up',1,'2011-03-10 23:53:59','2011-03-10 23:53:59',NULL),(27,12,0,0,'No usage server process running',1,'2011-04-16 00:50:26','2011-04-16 00:50:26',NULL),(28,13,0,0,'Management server node 127.0.0.1 is up',1,'2011-04-18 17:42:34','2011-04-18 17:42:34',NULL),(29,13,0,0,'Management server node 127.0.0.1 is up',1,'2011-04-29 23:51:50','2011-04-29 23:51:50',NULL),(30,13,0,0,'Management server node 127.0.0.1 is up',1,'2011-04-30 00:17:28','2011-04-30 00:17:28',NULL),(31,6,1,1,'Unable to eject host 15a16fce-865c-412e-8a16-841f968f8641',1,'2011-05-06 22:08:18','2011-05-06 22:08:18',NULL),(32,6,2,2,'Host is down, name: ezh-sit-xen1.vmopsdev.net (id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-09 23:00:19','2011-05-09 23:00:19',NULL),(33,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-09 23:00:20','2011-05-09 23:00:20',NULL),(34,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-09 23:00:20','2011-05-09 23:00:20',NULL),(35,9,2,2,'VM (name: v-40-VM, id: 40) stopped unexpectedly on host 4',1,'2011-05-09 23:08:04','2011-05-09 23:08:04',NULL),(36,9,2,2,'VM (name: v-40-VM, id: 40) stopped unexpectedly on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-09 23:08:05','2011-05-09 23:08:05',NULL),(37,7,2,2,'VM (name: s-41-VM, id: 41) stopped unexpectedly on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-09 23:08:05','2011-05-09 23:08:05',NULL),(38,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-09 23:08:06','2011-05-09 23:08:06',NULL),(39,6,2,2,'Host is down, name: ezh-sit-xen1.vmopsdev.net (id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-09 23:14:26','2011-05-09 23:14:26',NULL),(40,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-09 23:14:27','2011-05-09 23:14:27',NULL),(41,7,2,2,'VM (name: s-41-VM, id: 41) stopped unexpectedly on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-09 23:18:27','2011-05-09 23:18:27',NULL),(42,6,2,2,'Host is down, name: ezh-sit-xen1.vmopsdev.net (id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-09 23:36:17','2011-05-09 23:36:17',NULL),(43,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-09 23:36:18','2011-05-09 23:36:18',NULL),(44,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-09 23:36:18','2011-05-09 23:36:18',NULL),(45,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-09 23:47:18','2011-05-09 23:47:18',NULL),(46,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-09 23:47:18','2011-05-09 23:47:18',NULL),(47,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-09 23:58:18','2011-05-09 23:58:18',NULL),(48,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-09 23:58:18','2011-05-09 23:58:18',NULL),(49,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 00:09:18','2011-05-10 00:09:18',NULL),(50,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 00:09:18','2011-05-10 00:09:18',NULL),(51,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 00:20:18','2011-05-10 00:20:18',NULL),(52,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 00:20:18','2011-05-10 00:20:18',NULL),(53,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 00:31:19','2011-05-10 00:31:19',NULL),(54,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 00:31:19','2011-05-10 00:31:19',NULL),(55,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 00:42:19','2011-05-10 00:42:19',NULL),(56,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 00:42:19','2011-05-10 00:42:19',NULL),(57,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 00:53:19','2011-05-10 00:53:19',NULL),(58,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 00:53:19','2011-05-10 00:53:19',NULL),(59,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 01:04:19','2011-05-10 01:04:19',NULL),(60,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 01:04:19','2011-05-10 01:04:19',NULL),(61,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 01:15:19','2011-05-10 01:15:19',NULL),(62,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 01:15:19','2011-05-10 01:15:19',NULL),(63,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 01:26:20','2011-05-10 01:26:20',NULL),(64,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 01:26:20','2011-05-10 01:26:20',NULL),(65,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 01:37:20','2011-05-10 01:37:20',NULL),(66,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 01:37:20','2011-05-10 01:37:20',NULL),(67,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 01:48:20','2011-05-10 01:48:20',NULL),(68,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 01:48:20','2011-05-10 01:48:20',NULL),(69,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 01:59:20','2011-05-10 01:59:20',NULL),(70,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 01:59:20','2011-05-10 01:59:20',NULL),(71,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 02:10:20','2011-05-10 02:10:20',NULL),(72,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 02:10:20','2011-05-10 02:10:20',NULL),(73,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 02:21:20','2011-05-10 02:21:20',NULL),(74,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 02:21:20','2011-05-10 02:21:20',NULL),(75,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 02:32:21','2011-05-10 02:32:21',NULL),(76,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 02:32:21','2011-05-10 02:32:21',NULL),(77,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 02:43:21','2011-05-10 02:43:21',NULL),(78,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 02:43:21','2011-05-10 02:43:21',NULL),(79,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 02:54:21','2011-05-10 02:54:21',NULL),(80,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 02:54:21','2011-05-10 02:54:21',NULL),(81,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 03:05:21','2011-05-10 03:05:21',NULL),(82,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 03:05:21','2011-05-10 03:05:21',NULL),(83,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 03:16:21','2011-05-10 03:16:21',NULL),(84,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 03:16:21','2011-05-10 03:16:21',NULL),(85,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 03:27:21','2011-05-10 03:27:21',NULL),(86,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 03:27:21','2011-05-10 03:27:21',NULL),(87,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 03:38:22','2011-05-10 03:38:22',NULL),(88,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 03:38:22','2011-05-10 03:38:22',NULL),(89,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 03:49:22','2011-05-10 03:49:22',NULL),(90,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 03:49:22','2011-05-10 03:49:22',NULL),(91,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 04:00:22','2011-05-10 04:00:22',NULL),(92,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 04:00:22','2011-05-10 04:00:22',NULL),(93,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 04:10:38','2011-05-10 04:10:38',NULL),(94,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 04:10:38','2011-05-10 04:10:38',NULL),(95,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 04:21:38','2011-05-10 04:21:38',NULL),(96,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 04:21:38','2011-05-10 04:21:38',NULL),(97,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 04:32:38','2011-05-10 04:32:38',NULL),(98,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 04:32:38','2011-05-10 04:32:38',NULL),(99,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 04:43:38','2011-05-10 04:43:38',NULL),(100,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 04:43:39','2011-05-10 04:43:39',NULL),(101,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 04:54:39','2011-05-10 04:54:39',NULL),(102,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 04:54:39','2011-05-10 04:54:39',NULL),(103,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 05:05:39','2011-05-10 05:05:39',NULL),(104,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 05:05:39','2011-05-10 05:05:39',NULL),(105,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 05:16:39','2011-05-10 05:16:39',NULL),(106,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 05:16:39','2011-05-10 05:16:39',NULL),(107,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 05:27:39','2011-05-10 05:27:39',NULL),(108,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 05:27:39','2011-05-10 05:27:39',NULL),(109,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 05:38:40','2011-05-10 05:38:40',NULL),(110,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 05:38:40','2011-05-10 05:38:40',NULL),(111,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 05:49:40','2011-05-10 05:49:40',NULL),(112,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 05:49:40','2011-05-10 05:49:40',NULL),(113,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 06:00:40','2011-05-10 06:00:40',NULL),(114,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 06:00:40','2011-05-10 06:00:40',NULL),(115,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 06:11:44','2011-05-10 06:11:44',NULL),(116,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 06:11:44','2011-05-10 06:11:44',NULL),(117,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 06:22:40','2011-05-10 06:22:40',NULL),(118,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 06:22:40','2011-05-10 06:22:40',NULL),(119,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 06:33:40','2011-05-10 06:33:40',NULL),(120,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 06:33:40','2011-05-10 06:33:40',NULL),(121,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 06:44:41','2011-05-10 06:44:41',NULL),(122,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 06:44:41','2011-05-10 06:44:41',NULL),(123,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 06:55:41','2011-05-10 06:55:41',NULL),(124,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 06:55:41','2011-05-10 06:55:41',NULL),(125,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 07:06:41','2011-05-10 07:06:41',NULL),(126,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 07:06:41','2011-05-10 07:06:41',NULL),(127,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 07:17:41','2011-05-10 07:17:41',NULL),(128,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 07:17:41','2011-05-10 07:17:41',NULL),(129,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 07:28:41','2011-05-10 07:28:41',NULL),(130,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 07:28:41','2011-05-10 07:28:41',NULL),(131,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 07:39:42','2011-05-10 07:39:42',NULL),(132,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 07:39:42','2011-05-10 07:39:42',NULL),(133,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 07:50:42','2011-05-10 07:50:42',NULL),(134,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 07:50:42','2011-05-10 07:50:42',NULL),(135,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 08:01:42','2011-05-10 08:01:42',NULL),(136,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 08:01:42','2011-05-10 08:01:42',NULL),(137,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 08:12:42','2011-05-10 08:12:42',NULL),(138,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 08:12:42','2011-05-10 08:12:42',NULL),(139,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 08:23:42','2011-05-10 08:23:42',NULL),(140,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 08:23:43','2011-05-10 08:23:43',NULL),(141,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 08:34:43','2011-05-10 08:34:43',NULL),(142,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 08:34:43','2011-05-10 08:34:43',NULL),(143,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 08:45:43','2011-05-10 08:45:43',NULL),(144,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 08:45:43','2011-05-10 08:45:43',NULL),(145,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 08:56:43','2011-05-10 08:56:43',NULL),(146,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 08:56:43','2011-05-10 08:56:43',NULL),(147,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 09:07:44','2011-05-10 09:07:44',NULL),(148,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 09:07:44','2011-05-10 09:07:44',NULL),(149,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 09:18:44','2011-05-10 09:18:44',NULL),(150,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 09:18:44','2011-05-10 09:18:44',NULL),(151,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 09:29:44','2011-05-10 09:29:44',NULL),(152,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 09:29:44','2011-05-10 09:29:44',NULL),(153,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 09:40:45','2011-05-10 09:40:45',NULL),(154,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 09:40:45','2011-05-10 09:40:45',NULL),(155,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 09:51:45','2011-05-10 09:51:45',NULL),(156,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 09:51:45','2011-05-10 09:51:45',NULL),(157,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 10:02:45','2011-05-10 10:02:45',NULL),(158,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 10:02:45','2011-05-10 10:02:45',NULL),(159,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 10:13:45','2011-05-10 10:13:45',NULL),(160,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 10:13:45','2011-05-10 10:13:45',NULL),(161,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 10:24:45','2011-05-10 10:24:45',NULL),(162,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 10:24:46','2011-05-10 10:24:46',NULL),(163,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 10:35:46','2011-05-10 10:35:46',NULL),(164,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 10:35:46','2011-05-10 10:35:46',NULL),(165,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 10:46:46','2011-05-10 10:46:46',NULL),(166,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 10:46:46','2011-05-10 10:46:46',NULL),(167,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 10:57:46','2011-05-10 10:57:46',NULL),(168,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 10:57:46','2011-05-10 10:57:46',NULL),(169,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 11:08:46','2011-05-10 11:08:46',NULL),(170,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 11:08:46','2011-05-10 11:08:46',NULL),(171,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 11:19:46','2011-05-10 11:19:46',NULL),(172,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 11:19:47','2011-05-10 11:19:47',NULL),(173,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 11:30:47','2011-05-10 11:30:47',NULL),(174,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 11:30:47','2011-05-10 11:30:47',NULL),(175,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 11:41:47','2011-05-10 11:41:47',NULL),(176,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 11:41:47','2011-05-10 11:41:47',NULL),(177,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 11:52:47','2011-05-10 11:52:47',NULL),(178,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 11:52:47','2011-05-10 11:52:47',NULL),(179,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 12:03:47','2011-05-10 12:03:47',NULL),(180,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 12:03:47','2011-05-10 12:03:47',NULL),(181,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 12:14:47','2011-05-10 12:14:47',NULL),(182,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 12:14:47','2011-05-10 12:14:47',NULL),(183,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 12:25:48','2011-05-10 12:25:48',NULL),(184,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 12:25:48','2011-05-10 12:25:48',NULL),(185,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 12:36:48','2011-05-10 12:36:48',NULL),(186,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 12:36:48','2011-05-10 12:36:48',NULL),(187,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 12:47:48','2011-05-10 12:47:48',NULL),(188,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 12:47:48','2011-05-10 12:47:48',NULL),(189,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 12:58:48','2011-05-10 12:58:48',NULL),(190,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 12:58:48','2011-05-10 12:58:48',NULL),(191,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 13:09:48','2011-05-10 13:09:48',NULL),(192,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 13:09:48','2011-05-10 13:09:48',NULL),(193,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 13:20:48','2011-05-10 13:20:48',NULL),(194,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 13:20:48','2011-05-10 13:20:48',NULL),(195,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 13:31:48','2011-05-10 13:31:48',NULL),(196,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 13:31:48','2011-05-10 13:31:48',NULL),(197,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 13:42:48','2011-05-10 13:42:48',NULL),(198,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 13:42:49','2011-05-10 13:42:49',NULL),(199,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 13:53:49','2011-05-10 13:53:49',NULL),(200,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 13:53:49','2011-05-10 13:53:49',NULL),(201,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 14:04:49','2011-05-10 14:04:49',NULL),(202,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 14:04:49','2011-05-10 14:04:49',NULL),(203,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 14:15:49','2011-05-10 14:15:49',NULL),(204,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 14:15:49','2011-05-10 14:15:49',NULL),(205,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 14:26:51','2011-05-10 14:26:51',NULL),(206,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 14:26:51','2011-05-10 14:26:51',NULL),(207,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 14:37:50','2011-05-10 14:37:50',NULL),(208,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 14:37:50','2011-05-10 14:37:50',NULL),(209,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 14:48:50','2011-05-10 14:48:50',NULL),(210,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 14:48:50','2011-05-10 14:48:50',NULL),(211,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 14:59:50','2011-05-10 14:59:50',NULL),(212,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 14:59:50','2011-05-10 14:59:50',NULL),(213,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 15:10:50','2011-05-10 15:10:50',NULL),(214,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 15:10:50','2011-05-10 15:10:50',NULL),(215,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 15:21:51','2011-05-10 15:21:51',NULL),(216,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 15:21:51','2011-05-10 15:21:51',NULL),(217,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 15:32:51','2011-05-10 15:32:51',NULL),(218,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 15:32:51','2011-05-10 15:32:51',NULL),(219,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 15:43:51','2011-05-10 15:43:51',NULL),(220,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 15:43:51','2011-05-10 15:43:51',NULL),(221,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 15:54:51','2011-05-10 15:54:51',NULL),(222,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 15:54:51','2011-05-10 15:54:51',NULL),(223,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 16:05:52','2011-05-10 16:05:52',NULL),(224,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 16:05:53','2011-05-10 16:05:53',NULL),(225,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 16:16:51','2011-05-10 16:16:51',NULL),(226,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 16:16:51','2011-05-10 16:16:51',NULL),(227,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 16:27:52','2011-05-10 16:27:52',NULL),(228,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 16:27:52','2011-05-10 16:27:52',NULL),(229,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 16:38:52','2011-05-10 16:38:52',NULL),(230,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 16:38:52','2011-05-10 16:38:52',NULL),(231,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 16:49:52','2011-05-10 16:49:52',NULL),(232,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 16:49:52','2011-05-10 16:49:52',NULL),(233,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 17:00:52','2011-05-10 17:00:52',NULL),(234,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 17:00:52','2011-05-10 17:00:52',NULL),(235,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 17:11:53','2011-05-10 17:11:53',NULL),(236,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 17:11:53','2011-05-10 17:11:53',NULL),(237,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 17:22:53','2011-05-10 17:22:53',NULL),(238,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 17:22:53','2011-05-10 17:22:53',NULL),(239,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 17:33:53','2011-05-10 17:33:53',NULL),(240,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 17:33:53','2011-05-10 17:33:53',NULL),(241,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 17:44:53','2011-05-10 17:44:53',NULL),(242,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 17:44:53','2011-05-10 17:44:53',NULL),(243,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 17:55:53','2011-05-10 17:55:53',NULL),(244,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 17:55:53','2011-05-10 17:55:53',NULL),(245,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 18:06:54','2011-05-10 18:06:54',NULL),(246,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 18:06:54','2011-05-10 18:06:54',NULL),(247,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 18:17:54','2011-05-10 18:17:54',NULL),(248,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 18:17:54','2011-05-10 18:17:54',NULL),(249,9,2,2,'Unable to restart v-40-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 18:28:54','2011-05-10 18:28:54',NULL),(250,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 18:28:54','2011-05-10 18:28:54',NULL),(251,9,2,2,'VM (name: v-40-VM, id: 40) stopped unexpectedly on host 4',1,'2011-05-10 18:29:08','2011-05-10 18:29:08',NULL),(252,9,2,2,'VM (name: v-40-VM, id: 40) stopped unexpectedly on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 18:29:09','2011-05-10 18:29:09',NULL),(253,7,2,2,'VM (name: s-41-VM, id: 41) stopped unexpectedly on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 18:29:09','2011-05-10 18:29:09',NULL),(254,7,2,2,'Unable to restart s-41-VM which was running on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 18:29:10','2011-05-10 18:29:10',NULL),(255,7,2,2,'VM (name: s-41-VM, id: 41) stopped unexpectedly on host name: ezh-sit-xen1.vmopsdev.net(id:4), availability zone: Cupertino Lab, pod: Rack 8',1,'2011-05-10 18:40:09','2011-05-10 18:40:09',NULL),(256,13,0,0,'Management server node 127.0.0.1 is up',1,'2011-05-16 20:59:26','2011-05-16 20:59:26',NULL),(257,12,0,0,'No usage server process running',1,'2011-05-16 21:59:25','2011-05-16 21:59:25',NULL);
/*!40000 ALTER TABLE `alert` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `async_job`
--

DROP TABLE IF EXISTS `async_job`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `async_job` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `user_id` bigint(20) unsigned NOT NULL,
  `account_id` bigint(20) unsigned NOT NULL,
  `session_key` varchar(64) default NULL COMMENT 'all async-job manage to apply session based security enforcement',
  `instance_type` varchar(64) default NULL COMMENT 'instance_type and instance_id work together to allow attaching an instance object to a job',
  `instance_id` bigint(20) unsigned default NULL,
  `job_cmd` varchar(64) NOT NULL COMMENT 'command name',
  `job_cmd_originator` varchar(64) default NULL COMMENT 'command originator',
  `job_cmd_info` text COMMENT 'command parameter info',
  `job_cmd_ver` int(1) default NULL COMMENT 'command version',
  `callback_type` int(1) default NULL COMMENT 'call back type, 0 : polling, 1 : email',
  `callback_address` varchar(128) default NULL COMMENT 'call back address by callback_type',
  `job_status` int(1) default NULL COMMENT 'general job execution status',
  `job_process_status` int(1) default NULL COMMENT 'job specific process status for asynchronize progress update',
  `job_result_code` int(1) default NULL COMMENT 'job result code, specify error code corresponding to result message',
  `job_result` text COMMENT 'job result info',
  `job_init_msid` bigint(20) default NULL COMMENT 'the initiating msid',
  `job_complete_msid` bigint(20) default NULL COMMENT 'completing msid',
  `created` datetime default NULL COMMENT 'date created',
  `last_updated` datetime default NULL COMMENT 'date created',
  `last_polled` datetime default NULL COMMENT 'date polled',
  `removed` datetime default NULL COMMENT 'date removed',
  PRIMARY KEY  (`id`),
  KEY `i_async__user_id` (`user_id`),
  KEY `i_async__account_id` (`account_id`),
  KEY `i_async__instance_type_id` (`instance_type`,`instance_id`),
  KEY `i_async__job_cmd` (`job_cmd`),
  KEY `i_async__created` (`created`),
  KEY `i_async__last_updated` (`last_updated`),
  KEY `i_async__last_poll` (`last_polled`),
  KEY `i_async__removed` (`removed`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `async_job`
--

LOCK TABLES `async_job` WRITE;
/*!40000 ALTER TABLE `async_job` DISABLE KEYS */;
INSERT INTO `async_job` VALUES (1,1,1,NULL,NULL,NULL,'com.cloud.api.commands.DeleteDomainCmd',NULL,'{\"id\":\"59\",\"cleanup\":\"true\",\"ctxUserId\":\"1\",\"ctxAccountId\":\"1\",\"ctxStartEventId\":\"491\"}',0,0,NULL,1,0,0,'com.cloud.api.response.SuccessResponse/null/{\"success\":true}',6602001285584,6602001285584,'2011-05-16 17:52:14','2011-05-16 17:52:56',NULL,NULL);
/*!40000 ALTER TABLE `async_job` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `certificate`
--

DROP TABLE IF EXISTS `certificate`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `certificate` (
  `id` bigint(20) unsigned NOT NULL auto_increment COMMENT 'id',
  `certificate` text COMMENT 'the actual custom certificate being stored in the db',
  `updated` varchar(1) default NULL COMMENT 'status of the certificate',
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `certificate`
--

LOCK TABLES `certificate` WRITE;
/*!40000 ALTER TABLE `certificate` DISABLE KEYS */;
INSERT INTO `certificate` VALUES (1,NULL,'N');
/*!40000 ALTER TABLE `certificate` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cluster`
--

DROP TABLE IF EXISTS `cluster`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `cluster` (
  `id` bigint(20) unsigned NOT NULL auto_increment COMMENT 'id',
  `name` varchar(255) NOT NULL COMMENT 'name for the cluster',
  `guid` varchar(255) default NULL COMMENT 'guid for the cluster',
  `pod_id` bigint(20) unsigned NOT NULL COMMENT 'pod id',
  `data_center_id` bigint(20) unsigned NOT NULL COMMENT 'data center id',
  `hypervisor_type` varchar(32) default NULL,
  `cluster_type` varchar(64) default 'CloudManaged',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`),
  UNIQUE KEY `i_cluster__pod_id__name` (`pod_id`,`name`),
  UNIQUE KEY `guid` (`guid`),
  KEY `fk_cluster__data_center_id` (`data_center_id`),
  CONSTRAINT `fk_cluster__data_center_id` FOREIGN KEY (`data_center_id`) REFERENCES `data_center` (`id`),
  CONSTRAINT `fk_cluster__pod_id` FOREIGN KEY (`pod_id`) REFERENCES `host_pod_ref` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `cluster`
--

LOCK TABLES `cluster` WRITE;
/*!40000 ALTER TABLE `cluster` DISABLE KEYS */;
INSERT INTO `cluster` VALUES (1,'Cluster 1',NULL,1,1,'XenServer','CloudManaged'),(2,'Xen Cluster','026f0c41-ea48-7226-4ccc-fd0ffe914285',2,2,'XenServer','CloudManaged');
/*!40000 ALTER TABLE `cluster` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cluster_details`
--

DROP TABLE IF EXISTS `cluster_details`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `cluster_details` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `cluster_id` bigint(20) unsigned NOT NULL COMMENT 'cluster id',
  `name` varchar(255) NOT NULL,
  `value` varchar(255) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `fk_cluster_details__cluster_id` (`cluster_id`),
  CONSTRAINT `fk_cluster_details__cluster_id` FOREIGN KEY (`cluster_id`) REFERENCES `cluster` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `cluster_details`
--

LOCK TABLES `cluster_details` WRITE;
/*!40000 ALTER TABLE `cluster_details` DISABLE KEYS */;
/*!40000 ALTER TABLE `cluster_details` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `configuration`
--

DROP TABLE IF EXISTS `configuration`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `configuration` (
  `category` varchar(255) NOT NULL default 'Advanced',
  `instance` varchar(255) NOT NULL,
  `component` varchar(255) NOT NULL default 'management-server',
  `name` varchar(255) NOT NULL,
  `value` varchar(4095) default NULL,
  `description` varchar(1024) default NULL,
  PRIMARY KEY  (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `configuration`
--

LOCK TABLES `configuration` WRITE;
/*!40000 ALTER TABLE `configuration` DISABLE KEYS */;
INSERT INTO `configuration` VALUES ('Advanced','DEFAULT','management-server','account.cleanup.interval','86400','The interval in seconds between cleanup for removed accounts'),('Alert','DEFAULT','management-server','alert.email.addresses','dnoland@cloud.com','Comma separated list of email addresses used for sending alerts.'),('Alert','DEFAULT','management-server','alert.email.sender','alerts-ezh-sit@vmopsdev.net','Sender of alert email (will be in the From header of the email).'),('Alert','DEFAULT','management-server','alert.smtp.host','localhost','SMTP hostname used for sending out email alerts.'),('Alert','DEFAULT','management-server','alert.smtp.password',NULL,'Password for SMTP authentication (applies only if alert.smtp.useAuth is true).'),('Alert','DEFAULT','management-server','alert.smtp.port','25','Port the SMTP server is listening on.'),('Alert','DEFAULT','management-server','alert.smtp.useAuth',NULL,'If true, use SMTP authentication when sending emails.'),('Alert','DEFAULT','management-server','alert.smtp.username',NULL,'Username for SMTP authentication (applies only if alert.smtp.useAuth is true).'),('Alert','DEFAULT','AgentManager','alert.wait',NULL,'Seconds to wait before alerting on a disconnected agent'),('Advanced','DEFAULT','management-server','allow.public.user.templates','false','If false, users will not be able to create public templates.'),('Usage','DEFAULT','management-server','capacity.check.period','300000','The interval in milliseconds between capacity checks'),('Advanced','DEFAULT','management-server','capacity.skipcounting.hours','3600','Seconds to wait before release VM\'s cpu and memory when VM in stopped state'),('Advanced','DEFAULT','management-server','check.pod.cidrs','true','If true, different pods must belong to different CIDR subnets.'),('Hidden','DEFAULT','management-server','cloud.identifier','a29c7668-8a6c-4dd5-8319-487e4646affa','A unique identifier for the cloud.'),('Advanced','DEFAULT','AgentManager','cmd.wait','7200','Time to wait for some heavy time-consuming commands'),('Console Proxy','DEFAULT','AgentManager','consoleproxy.capacity.standby','10','The minimal number of console proxy viewer sessions that system is able to serve immediately(standby capacity)'),('Console Proxy','DEFAULT','AgentManager','consoleproxy.capacityscan.interval','30000','The time interval(in millisecond) to scan whether or not system needs more console proxy to ensure minimal standby capacity'),('Console Proxy','DEFAULT','AgentManager','consoleproxy.cmd.port','8001','Console proxy command port that is used to communicate with management server'),('Console Proxy','DEFAULT','AgentManager','consoleproxy.cpu.mhz','512','CPU speed (in MHz) used to create new console proxy VMs'),('Console Proxy','DEFAULT','AgentManager','consoleproxy.disable.rpfilter','true','disable rp_filter on console proxy VM public interface'),('Console Proxy','DEFAULT','AgentManager','consoleproxy.launch.max','10','maximum number of console proxy instances per zone can be launched'),('Console Proxy','DEFAULT','AgentManager','consoleproxy.loadscan.interval','10000','The time interval(in milliseconds) to scan console proxy working-load info'),('Console Proxy','DEFAULT','AgentManager','consoleproxy.ram.size','256','RAM size (in MB) used to create new console proxy VMs'),('Console Proxy','DEFAULT','AgentManager','consoleproxy.restart','true','Console proxy restart flag, defaulted to true'),('Console Proxy','DEFAULT','AgentManager','consoleproxy.session.max','50','The max number of viewer sessions console proxy is configured to serve for'),('Console Proxy','DEFAULT','AgentManager','consoleproxy.session.timeout','300000','Timeout(in milliseconds) that console proxy tries to maintain a viewer session before it times out the session for no activity'),('Console Proxy','DEFAULT','AgentManager','consoleproxy.url.domain','realhostip.com','Console proxy url domain'),('Advanced','DEFAULT','management-server','control.cidr','169.254.0.0/16','Changes the cidr for the control network traffic.  Defaults to using link local.  Must be unique within pods'),('Advanced','DEFAULT','management-server','control.gateway','169.254.0.1','gateway for the control network traffic'),('Usage','DEFAULT','management-server','cpu.capacity.threshold','0.85','Percentage (as a value between 0 and 1) of cpu utilization above which alerts will be sent about low cpu available.'),('Advanced','DEFAULT','management-server','cpu.overprovisioning.factor','2','Used for CPU overprovisioning calculation; available CPU will be (actualCpuCapacity * cpu.overprovisioning.factor)'),('Advanced','DEFAULT','management-server','direct.attach.network.externalIpAllocator.enabled','false','Direct-attach VMs using external DHCP server'),('Advanced','DEFAULT','management-server','direct.attach.network.externalIpAllocator.url',NULL,'Direct-attach VMs using external DHCP server (API url)'),('Advanced','DEFAULT','management-server','direct.attach.security.groups.enabled','false','Ec2-style distributed firewall for direct-attach VMs'),('Network','DEFAULT','management-server','direct.network.no.default.route','false','Direct Network Dhcp Server should not send a default route'),('Premium','DEFAULT','management-server','enable.usage.server','true','Flag for enabling usage'),('Advanced','DEFAULT','management-server','event.purge.delay','0','Events older than specified number days will be purged'),('Advanced','DEFAULT','UserVmManager','expunge.delay','86400','Determines how long to wait before actually expunging destroyed vm. The default value = the default value of expunge.interval'),('Advanced','DEFAULT','UserVmManager','expunge.interval','86400','The interval to wait before running the expunge thread.'),('Advanced','DEFAULT','UserVmManager','expunge.workers','1','Number of workers performing expunge '),('Advanced','DEFAULT','management-server','extract.url.cleanup.interval','120','The interval to wait before cleaning up the extract URL\'s '),('Network','DEFAULT','AgentManager','guest.domain.suffix','cloud.internal','Default domain name for vms inside virtualized networks fronted by router'),('Network','DEFAULT','AgentManager','guest.ip.network','10.1.1.1','The network address of the guest virtual network. Virtual machines will be assigned an IP in this subnet.'),('Network','DEFAULT','AgentManager','guest.netmask','255.255.255.0','The netmask of the guest virtual network.'),('Network','DEFAULT','management-server','guest.vlan.bits','12','The number of bits to reserve for the VLAN identifier in the guest subnet.'),('Advanced','DEFAULT','management-server','host','192.168.130.151','The ip address of management server'),('Advanced','DEFAULT','management-server','host.capacity.checker.interval','3600','Seconds to wait before recalculating host\'s capacity'),('Advanced','DEFAULT','management-server','host.capacity.checker.wait','3600','Seconds to wait before starting host capacity background checker'),('Advanced','DEFAULT','AgentManager','host.retry','2','Number of times to retry hosts for creating a volume'),('Advanced','DEFAULT','management-server','host.stats.interval','60000','The interval in milliseconds when host stats are retrieved from agents.'),('Advanced','DEFAULT','management-server','hypervisor.list','XenServer','The list of hypervisors that this deployment will use.'),('Hidden','DEFAULT','none','init','true',NULL),('Advanced','DEFAULT','AgentManager','instance.name','VM','Name of the deployment instance.'),('Advanced','DEFAULT','management-server','integration.api.port','8096','Defaul API port'),('Advanced','DEFAULT','HighAvailabilityManager','investigate.retry.interval','60','Time in seconds between VM pings when agent is disconnected'),('Advanced','DEFAULT','management-server','job.cancel.threshold.minutes','60','Time (in minutes) for async-jobs to be forcely cancelled if it has been in process for long'),('Advanced','DEFAULT','management-server','job.expire.minutes','1440','Time (in minutes) for async-jobs to be kept in system'),('Advanced','DEFAULT','management-server','kvm.private.network.device',NULL,'Specify the private bridge on host for private network'),('Advanced','DEFAULT','management-server','kvm.public.network.device',NULL,'Specify the public bridge on host for public network'),('Advanced','DEFAULT','management-server','linkLocalIp.nums','10','The number of link local ip that needed by domR(in power of 2)'),('Advanced','DEFAULT','management-server','management.network.cidr',NULL,'The cidr of management server network'),('Advanced','DEFAULT','management-server','max.template.iso.size','50','The maximum size for a downloaded template or ISO (in GB).'),('Usage','DEFAULT','management-server','memory.capacity.threshold','0.85','Percentage (as a value between 0 and 1) of memory utilization above which alerts will be sent about low memory available.'),('Advanced','DEFAULT','HighAvailabilityManager','migrate.retry.interval','120','Time in seconds between migration retries'),('Advanced','DEFAULT','management-server','mount.parent','/var/lib/cloud/mnt','The mount point on the Management Server for Secondary Storage.'),('Advanced','DEFAULT','management-server','network.gc.interval','600','Seconds to wait before checking for networks to shutdown'),('Advanced','DEFAULT','management-server','network.gc.wait','600','Seconds to wait before shutting down a network that\'s not in used'),('Network','DEFAULT','none','network.guest.cidr.limit','22','size limit for guest cidr; cant be less than this value'),('Network','DEFAULT','management-server','network.throttling.rate','200','Default data transfer rate in megabits per second allowed.'),('Network','DEFAULT','management-server','open.vswitch.tunnel.network','false','enable/disable open vswitch tunnel network(no vlan)'),('Network','DEFAULT','management-server','open.vswitch.vlan.network','false','enable/disable vlan remapping of  open vswitch network'),('Advanced','DEFAULT','AgentManager','ping.interval','60','Ping interval in seconds'),('Advanced','DEFAULT','AgentManager','ping.timeout','2.5','Multiplier to ping.interval before announcing an agent has timed out'),('Advanced','DEFAULT','AgentManager','port','8250','Port to listen on for agent connection.'),('Usage','DEFAULT','management-server','private.ip.capacity.threshold','0.85','Percentage (as a value between 0 and 1) of private IP address space utilization above which alerts will be sent.'),('Usage','DEFAULT','management-server','public.ip.capacity.threshold','0.85','Percentage (as a value between 0 and 1) of public IP address space utilization above which alerts will be sent.'),('Network','DEFAULT','AgentManager','remote.access.vpn.client.iprange','10.1.2.1-10.1.2.8','The range of ips to be allocated to remote access vpn clients. The first ip in the range is used by the VPN server'),('Network','DEFAULT','AgentManager','remote.access.vpn.psk.length','24','The length of the ipsec preshared key (minimum 8, maximum 256)'),('Network','DEFAULT','AgentManager','remote.access.vpn.user.limit','8','The maximum number of VPN users that can be created per account'),('Advanced','DEFAULT','HighAvailabilityManager','restart.retry.interval','600','Time in seconds between retries to restart a vm'),('Advanced','DEFAULT','none','router.cpu.mhz','500','Default CPU speed (MHz) for router VM.'),('Advanced','DEFAULT','none','router.ram.size','128','Default RAM for router VM in MB.'),('Advanced','DEFAULT','none','router.stats.interval','300','Interval to report router statistics.'),('Advanced','DEFAULT','none','router.template.id','1','Default ID for template.'),('Hidden','DEFAULT','database','schema.level','2.2','The schema level of this database'),('Hidden','DEFAULT','management-server','secondary.storage.vm','true','Deploys a VM per zone to manage secondary storage if true, otherwise secondary storage is mounted on management server'),('Advanced','DEFAULT','management-server','secstorage.allowed.internal.sites','192.168.0.0/16','Comma separated list of cidrs internal to the datacenter that can host template download servers'),('Hidden','DEFAULT','management-server','secstorage.copy.password','cE5xwmtsqxbcckq','Password used to authenticate zone-to-zone template copy requests'),('Advanced','DEFAULT','management-server','secstorage.encrypt.copy','true','Use SSL method used to encrypt copy traffic between zones'),('Advanced','DEFAULT','management-server','secstorage.ssl.cert.domain','realhostip.com','SSL certificate used to encrypt copy traffic between zones'),('Advanced','DEFAULT','AgentManager','secstorage.vm.cpu.mhz',NULL,'CPU speed (in MHz) used to create new secondary storage vms'),('Advanced','DEFAULT','AgentManager','secstorage.vm.ram.size',NULL,'RAM size (in MB) used to create new secondary storage vms'),('Hidden','DEFAULT','management-server','security.hash.key','97346339-71fc-4b56-87e1-d5af3a04b040','for generic key-ed hash'),('Hidden','DEFAULT','management-server','security.singlesignon.key','hA3vs0qcVXYPIGR3pW1OAXoucKwX8avbOMBnDC-Q95DMsL-szc6WqFdLoWnHJTuTLg2MRq1Xf7v26Lef8EUMJw','A Single Sign-On key used for logging into the cloud'),('Advanced','DEFAULT','management-server','security.singlesignon.tolerance.millis','60000','The allowable clock difference in milliseconds between when an SSO login request is made and when it is received.'),('Snapshots','DEFAULT','none','snapshot.delta.max','16','max delta snapshots between two full snapshots.'),('Snapshots','DEFAULT','none','snapshot.max.daily','8','Maximum daily snapshots for a volume'),('Snapshots','DEFAULT','none','snapshot.max.hourly','8','Maximum hourly snapshots for a volume'),('Snapshots','DEFAULT','none','snapshot.max.monthly','8','Maximum monthly snapshots for a volume'),('Snapshots','DEFAULT','none','snapshot.max.weekly','8','Maximum weekly snapshots for a volume'),('Snapshots','DEFAULT','none','snapshot.poll.interval','300','The time interval in seconds when the management server polls for snapshots to be scheduled.'),('Hidden','DEFAULT','management-server','ssh.privatekey','-----BEGIN RSA PRIVATE KEY-----\nMIIEoQIBAAKCAQEA9A3RN1BFR6r3WyFlM7aPir1JAfq911nZ+dGH9PURbjdZZjKH\nKTc+1A0SMGSijAENjEauR5Bh1KvgQTeqkxOtzKojdrYK27omGv0UkPsjHHfTe4uo\na9J8UbhKP2WMFah6SEOTrTxu8xzuxYAY/QBZch967B3oAYUwiUSOKi1YDSPGlRY/\nAi2sIBTXbdvoLDmNQM74fNTFqqMGuYZyETgGZRsyVmtZdB0OXR5gxEr7Cuzae0QS\ncWG5u7cV8sfTSLB4u9k2Afq1lyWgxCJZM5AESz2px6bICrzZEmOFWut/TF9KT2Q/\nqC9pFj8Oo+1EWQMTN3s6bgi1+Fxb/vfNaXkoUQIBIwKCAQA3yKTZcW7rz057OtVN\nphIuVyahXeI/2gXv9WDniHkDP9nreUN3IpIElUX8bsYRX1ONxwNDjrdGja+Z4NaI\nBH9/PNTn7xhsvNWCgv1i95L/MVTpCfqcTV4+kIX/3LJOF+FvmmrlxKulSHEXMzjp\nXypjOmU9SKoO+d81UX+UnKZpZtEQTgqCbFqjM0mBhJYuTqZxk3OBXvU3AStTTQTJ\nlywPWP/vX7M6FiC65NE/iRcHxcMXICBA5PYJ8UZ1Az2+TllPHz+3wqovEc6YoxcX\nEOah0jTc46lwKJgOS26z8wjUftrk0t2SXl75i6u6sInGyCJ1UxGzqMBrTwMLPHcJ\n6KFrAoGBAPwNt/s/M3mF3NAdZQ7L/lM/rgTM+2H7AeW+7Up19FEiweOoz26Cg/f4\nDoMiV8pWZY4JHAgRBE04dxtSuPmDIWdojSTAZ+bw9gdGywxl187olm2Ktgsa1LjD\nzdJGXrULEZkEOW/stoxWRPLs8jkpsKdNxSs3B48YxBEQtNd80FXbAoGBAPfgCNXI\nYCYQN6W50shVoyHQO9pZ2fLe/ANuKDKpTlR/M+72WTrTeEczZsor+2vyhhbs+31E\nc7gXwGZgIKuk+EqZI1p9nyYYQbGhjcfIzkRer5T2jLelNocQC67BRYOforOiEEZb\nG/08NwxmNrBpptUoaqHE/t0KHp2dGm0hTxBDAoGAQNBT4YVHwCm8cAePA84OMqn5\njDS1q3sPHdICgNyApyYx23SNHGq0OHL8atzyANRjQcfUAhMBGyvG2yPmbAvGwtG9\n5OEExls38z4W9I83flkQvRUKPV6runt+EX/PNd5GWowOxQJa06EnrC5M6iCpxJ74\nL68QkoK2EwRLwmHsbdkCgYAVPxazPQ+OO+eDO9A9DqeVLxsStz6RKQ5Jb9ePUFcr\n0GOJgtRywas5TYx3uqCFmHHktTogrhiMH0sBdfQr+DKKDSBJlb0vJqaLkYEuYatH\nsFgxVvYeXp5GF1F1YQX1VtNue5r+t1orphNY1Y+ouJlFeH4rH4OPSgKfy6Mt7OmF\nDQKBgQDCOU9vywC1hd7yOadmB1JDSa4uwCg38fY6QR+jXkbVPF2xn+Pq8LboJwlj\nJEQcv3Q+UGEZ9/qbL6B6iB7tXAyivkIcJeFrHQyDlliyLPscG4cCzbrpgFgcvEwh\n47LGVONGDuMYWozWjN8A/HBt3H3DcC1IDTVT38D/QSGaWsv1ig==\n-----END RSA PRIVATE KEY-----','Private key for the entire CloudStack'),('Hidden','DEFAULT','management-server','ssh.publickey','ssh-rsa AAAAB3NzaC1yc2EAAAABIwAAAQEA9A3RN1BFR6r3WyFlM7aPir1JAfq911nZ+dGH9PURbjdZZjKHKTc+1A0SMGSijAENjEauR5Bh1KvgQTeqkxOtzKojdrYK27omGv0UkPsjHHfTe4uoa9J8UbhKP2WMFah6SEOTrTxu8xzuxYAY/QBZch967B3oAYUwiUSOKi1YDSPGlRY/Ai2sIBTXbdvoLDmNQM74fNTFqqMGuYZyETgGZRsyVmtZdB0OXR5gxEr7Cuzae0QScWG5u7cV8sfTSLB4u9k2Afq1lyWgxCJZM5AESz2px6bICrzZEmOFWut/TF9KT2Q/qC9pFj8Oo+1EWQMTN3s6bgi1+Fxb/vfNaXkoUQ== cloud@cloudstack.vmopsdev.net','Public key for the entire CloudStack'),('Advanced','DEFAULT','AgentManager','start.retry','10','Number of times to retry create and start commands'),('Advanced','DEFAULT','HighAvailabilityManager','stop.retry.interval','600','Time in seconds between retries to stop or destroy a vm'),('Usage','DEFAULT','management-server','storage.allocated.capacity.threshold','0.85','Percentage (as a value between 0 and 1) of allocated storage utilization above which alerts will be sent about low storage available.'),('Usage','DEFAULT','management-server','storage.capacity.threshold','0.85','Percentage (as a value between 0 and 1) of storage utilization above which alerts will be sent about low storage available.'),('Advanced','DEFAULT','none','storage.cleanup.enabled','true','Enables/disables the storage cleanup thread.'),('Advanced','DEFAULT','none','storage.cleanup.interval','86400','The interval to wait before running the storage cleanup thread.'),('Storage','DEFAULT','management-server','storage.max.volume.size','2000','The maximum size for a volume in GB.'),('Storage','DEFAULT','StorageAllocator','storage.overprovisioning.factor','2','Used for storage overprovisioning calculation; available storage will be (actualStorageSize * storage.overprovisioning.factor)'),('Storage','DEFAULT','management-server','storage.stats.interval','60000','The interval in milliseconds when storage stats (per host) are retrieved from agents.'),('Advanced','DEFAULT','management-server','system.vm.auto.reserve.capacity','true','Indicates whether or not to automatically reserver system VM standby capacity.'),('Advanced','DEFAULT','management-server','system.vm.use.local.storage','false','Indicates whether to use local storage pools or shared storage pools for system VMs.'),('Storage','DEFAULT','AgentManager','total.retries','4','The number of times each command sent to a host should be retried in case of failure.'),('Advanced','DEFAULT','AgentManager','update.wait','600','Time to wait before alerting on a updating agent'),('Premium','DEFAULT','management-server','usage.execution.timezone',NULL,'The timezone to use for usage job execution time'),('Premium','DEFAULT','management-server','usage.stats.job.aggregation.range','1440','The range of time for aggregating the user statistics specified in minutes (e.g. 1440 for daily, 60 for hourly.'),('Premium','DEFAULT','management-server','usage.stats.job.exec.time','00:15','The time at which the usage statistics aggregation job will run as an HH24:MM time, e.g. 00:30 to run at 12:30am.'),('Premium','DEFAULT','management-server','use.local.storage','false','Should we use the local storage if it\'s available?'),('Advanced','DEFAULT','management-server','vm.allocation.algorithm','random','If \'random\', hosts within a pod will be randomly considered for VM/volume allocation. If \'firstfit\', they will be considered on a first-fit basis.'),('Advanced','DEFAULT','management-server','vm.op.cancel.interval','3600','Seconds to wait before cancelling a operation'),('Advanced','DEFAULT','management-server','vm.op.cleanup.interval','86400','Interval to run the thread that cleans up the vm operations in seconds'),('Advanced','DEFAULT','management-server','vm.op.cleanup.wait','3600','Seconds to wait before cleanuping up any vm work items'),('Advanced','DEFAULT','management-server','vm.op.lock.state.retry','5','Times to retry locking the state of a VM for operations'),('Advanced','DEFAULT','management-server','vm.op.wait.interval','120','Seconds to wait before checking if a previous operation has succeeded'),('Advanced','DEFAULT','management-server','vm.stats.interval','60000','The interval in milliseconds when vm stats are retrieved from agents.'),('Advanced','DEFAULT','management-server','vm.tranisition.wait.interval','3600','Seconds to wait before taking over a VM in transition state'),('Advanced','DEFAULT','management-server','vmware.guest.vswitch',NULL,'Specify the vSwitch on host for guest network'),('Advanced','DEFAULT','management-server','vmware.private.vswitch',NULL,'Specify the vSwitch on host for private network'),('Advanced','DEFAULT','management-server','vmware.public.vswitch',NULL,'Specify the vSwitch on host for public network'),('Advanced','DEFAULT','AgentManager','wait','1800','Time to wait for control commands to return'),('Advanced','DEFAULT','AgentManager','workers','5','Number of worker threads.'),('Advanced','DEFAULT','management-server','xen.bond.storage.nics',NULL,'Attempt to bond the two networks if found'),('Hidden','DEFAULT','management-server','xen.create.pools.in.pod','false','Should we automatically add XenServers into pools that are inside a Pod'),('Advanced','DEFAULT','management-server','xen.guest.network.device',NULL,'Specify for guest network name label'),('Advanced','DEFAULT','management-server','xen.heartbeat.interval','60','heartbeat to use when implementing XenServer Self Fencing'),('Advanced','DEFAULT','management-server','xen.max.product.version','5.6.0','Maximum XenServer version'),('Advanced','DEFAULT','management-server','xen.max.version','3.4.2','Maximum Xen version'),('Advanced','DEFAULT','management-server','xen.max.xapi.version','1.3','Maximum Xapi Tool Stack version'),('Advanced','DEFAULT','management-server','xen.min.product.version','0.1.1','Minimum XenServer version'),('Advanced','DEFAULT','management-server','xen.min.version','3.3.1','Minimum Xen version'),('Advanced','DEFAULT','management-server','xen.min.xapi.version','1.3','Minimum Xapi Tool Stack version'),('Network','DEFAULT','management-server','xen.private.network.device',NULL,'Specify when the private network name is different'),('Network','DEFAULT','management-server','xen.public.network.device',NULL,'[ONLY IF THE PUBLIC NETWORK IS ON A DEDICATED NIC]:The network name label of the physical device dedicated to the public network on a XenServer host'),('Advanced','DEFAULT','management-server','xen.setup.multipath','false','Setup the host to do multipath'),('Network','DEFAULT','management-server','xen.storage.network.device1','cloud-stor1','Specify when there are storage networks'),('Network','DEFAULT','management-server','xen.storage.network.device2','cloud-stor2','Specify when there are storage networks');
/*!40000 ALTER TABLE `configuration` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `console_proxy`
--

DROP TABLE IF EXISTS `console_proxy`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `console_proxy` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `public_mac_address` varchar(17) default NULL COMMENT 'mac address of the public facing network card',
  `public_ip_address` varchar(15) default NULL COMMENT 'public ip address for the console proxy',
  `public_netmask` varchar(15) default NULL COMMENT 'public netmask used for the console proxy',
  `ram_size` int(10) unsigned NOT NULL default '512' COMMENT 'memory to use in mb',
  `active_session` int(10) NOT NULL default '0' COMMENT 'active session number',
  `last_update` datetime default NULL COMMENT 'Last session update time',
  `session_details` blob COMMENT 'session detail info',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `public_mac_address` (`public_mac_address`),
  UNIQUE KEY `public_ip_address` (`public_ip_address`),
  CONSTRAINT `fk_console_proxy__id` FOREIGN KEY (`id`) REFERENCES `vm_instance` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=41 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `console_proxy`
--

LOCK TABLES `console_proxy` WRITE;
/*!40000 ALTER TABLE `console_proxy` DISABLE KEYS */;
INSERT INTO `console_proxy` VALUES (1,NULL,NULL,NULL,0,0,'2011-04-09 11:03:12','{\"connections\":[]}\n'),(40,'06:d0:be:00:00:0d','172.16.64.47','255.255.252.0',512,0,'2011-05-11 21:49:32','{\"connections\":[]}\n');
/*!40000 ALTER TABLE `console_proxy` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `data_center`
--

DROP TABLE IF EXISTS `data_center`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `data_center` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `name` varchar(255) default NULL,
  `description` varchar(255) default NULL,
  `dns1` varchar(255) NOT NULL,
  `dns2` varchar(255) default NULL,
  `internal_dns1` varchar(255) NOT NULL,
  `internal_dns2` varchar(255) default NULL,
  `gateway` varchar(15) default NULL,
  `netmask` varchar(15) default NULL,
  `vnet` varchar(255) default NULL,
  `router_mac_address` varchar(17) NOT NULL default '02:00:00:00:00:01' COMMENT 'mac address for the router within the domain',
  `mac_address` bigint(20) unsigned NOT NULL default '1' COMMENT 'Next available mac address for the ethernet card interacting with public internet',
  `guest_network_cidr` varchar(18) default NULL,
  `domain` varchar(100) default NULL COMMENT 'Network domain name of the Vms of the zone',
  `domain_id` bigint(20) unsigned default NULL COMMENT 'domain id for the parent domain to this zone (null signifies public zone)',
  `networktype` varchar(255) NOT NULL default 'Basic' COMMENT 'Network type of the zone',
  `dns_provider` char(64) default 'VirtualRouter',
  `gateway_provider` char(64) default 'VirtualRouter',
  `firewall_provider` char(64) default 'VirtualRouter',
  `dhcp_provider` char(64) default 'VirtualRouter',
  `lb_provider` char(64) default 'VirtualRouter',
  `vpn_provider` char(64) default 'VirtualRouter',
  `userdata_provider` char(64) default 'VirtualRouter',
  `enable` tinyint(4) NOT NULL default '1' COMMENT 'Is this data center enabled for activities',
  `is_security_group_enabled` tinyint(4) NOT NULL default '0' COMMENT '1: enabled, 0: not',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `data_center`
--

LOCK TABLES `data_center` WRITE;
/*!40000 ALTER TABLE `data_center` DISABLE KEYS */;
INSERT INTO `data_center` VALUES (1,'Delete me',NULL,'192.168.10.254','','192.168.10.254','',NULL,NULL,'200-299','02:00:00:00:00:01',24,'10.1.1.0/24',NULL,NULL,'Advanced','VirtualRouter','VirtualRouter','VirtualRouter','VirtualRouter','VirtualRouter','VirtualRouter','VirtualRouter',1,0),(2,'Cupertino Lab',NULL,'72.52.126.11','72.52.126.12','192.168.110.254','192.168.110.253',NULL,NULL,'1080-1159','02:00:00:00:00:01',16,'10.1.3.0/24',NULL,NULL,'Advanced','VirtualRouter','VirtualRouter','VirtualRouter','VirtualRouter','VirtualRouter','VirtualRouter','VirtualRouter',1,0);
/*!40000 ALTER TABLE `data_center` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `disk_offering`
--

DROP TABLE IF EXISTS `disk_offering`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `disk_offering` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `domain_id` bigint(20) unsigned default NULL,
  `name` varchar(255) NOT NULL,
  `display_text` varchar(4096) default NULL COMMENT 'Descrianaption text set by the admin for display purpose only',
  `disk_size` bigint(20) unsigned NOT NULL COMMENT 'disk space in mbs',
  `type` varchar(32) default NULL COMMENT 'inheritted by who?',
  `tags` varchar(4096) default NULL COMMENT 'comma separated tags about the disk_offering',
  `recreatable` tinyint(1) unsigned NOT NULL default '0' COMMENT 'The root disk is always recreatable',
  `use_local_storage` tinyint(1) unsigned NOT NULL default '0' COMMENT 'Indicates whether local storage pools should be used',
  `unique_name` varchar(32) default NULL COMMENT 'unique name',
  `system_use` tinyint(1) unsigned NOT NULL default '0' COMMENT 'is this offering for system used only',
  `customized` tinyint(1) unsigned NOT NULL default '0' COMMENT '0 implies not customized by default',
  `removed` datetime default NULL COMMENT 'date removed',
  `created` datetime default NULL COMMENT 'date the disk offering was created',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `unique_name` (`unique_name`)
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `disk_offering`
--

LOCK TABLES `disk_offering` WRITE;
/*!40000 ALTER TABLE `disk_offering` DISABLE KEYS */;
INSERT INTO `disk_offering` VALUES (1,NULL,'Small Instance','Small Instance, $0.05 per hour',0,'Service',NULL,0,0,NULL,0,1,'2011-02-10 06:37:00','2011-02-10 05:28:32'),(2,NULL,'Medium Instance','Medium Instance, $0.10 per hour',0,'Service',NULL,0,0,NULL,0,1,'2011-02-10 06:37:07','2011-02-10 05:28:32'),(3,1,'Small','Small Disk, 5 GB',5120,'Disk',NULL,0,0,NULL,0,0,'2011-02-10 06:37:14','2011-02-10 05:28:32'),(4,1,'Medium','Medium Disk, 20 GB',20480,'Disk',NULL,0,0,NULL,0,0,'2011-02-10 06:37:20','2011-02-10 05:28:32'),(5,1,'Large','Large Disk, 100 GB',102400,'Disk',NULL,0,0,NULL,0,0,'2011-02-10 06:37:25','2011-02-10 05:28:32'),(6,NULL,'System Offering For Console Proxy',NULL,0,'Service',NULL,1,0,'Cloud.com-ConsoleProxy',1,1,NULL,'2011-02-10 05:28:51'),(7,NULL,'System Offering For Secondary Storage VM',NULL,0,'Service',NULL,1,0,'Cloud.com-SecondaryStorage',1,1,NULL,'2011-02-10 05:28:51'),(8,NULL,'System Offering For Software Router',NULL,0,'Service',NULL,1,0,'Cloud.Com-SoftwareRouter',1,1,NULL,'2011-02-10 05:28:51'),(9,2,'Compute Small','1CPU, 2GB RAM, 160GB Storage',0,'Service',NULL,0,0,NULL,0,1,NULL,'2011-02-10 06:36:37'),(10,2,'Compute Medium','2CPU, 4GB RAM, 160GB Storage',0,'Service',NULL,0,0,NULL,0,1,NULL,'2011-02-10 06:36:37'),(11,2,'Compute Large','4CPU, 8GB RAM, 160GB Storage',0,'Service',NULL,0,0,NULL,0,1,NULL,'2011-02-10 06:36:37'),(12,2,'Compute Xlarge','8CPU, 16GB RAM, 160GB Storage',0,'Service',NULL,0,0,NULL,0,1,NULL,'2011-02-10 06:36:38'),(13,2,'Storage Primary - 50 GB','Storage Primary - 50 GB',5120,'Disk',NULL,0,0,NULL,0,0,NULL,'2011-02-10 06:36:38'),(14,2,'Storage Primary - 100 GB','Storage Primary - 100 GB',5120,'Disk',NULL,0,0,NULL,0,0,NULL,'2011-02-10 06:36:38'),(15,2,'Storage Primary - 150 GB','Storage Primary - 150 GB',5120,'Disk',NULL,0,0,NULL,0,0,NULL,'2011-02-10 06:36:38'),(16,2,'Storage Primary - 200 GB','Storage Primary - 200 GB',5120,'Disk',NULL,0,0,NULL,0,0,NULL,'2011-02-10 06:36:38'),(17,2,'Storage Primary - 250 GB','Storage Primary - 250 GB',5120,'Disk',NULL,0,0,NULL,0,0,NULL,'2011-02-10 06:36:38'),(18,2,'Storage Primary - 300 GB','Storage Primary - 300 GB',5120,'Disk',NULL,0,0,NULL,0,0,NULL,'2011-02-10 06:36:38'),(19,2,'Storage Primary - 350 GB','Storage Primary - 350 GB',5120,'Disk',NULL,0,0,NULL,0,0,NULL,'2011-02-10 06:36:38'),(20,2,'Storage Primary - 400 GB','Storage Primary - 400 GB',5120,'Disk',NULL,0,0,NULL,0,0,NULL,'2011-02-10 06:36:38'),(21,2,'Storage Primary - 500 GB','Storage Primary - 500 GB',5120,'Disk',NULL,0,0,NULL,0,0,NULL,'2011-02-10 06:36:38'),(22,2,'Storage Primary - 600 GB','Storage Primary - 600 GB',5120,'Disk',NULL,0,0,NULL,0,0,NULL,'2011-02-10 06:36:38'),(23,2,'Storage Primary - 700 GB','Storage Primary - 700 GB',5120,'Disk',NULL,0,0,NULL,0,0,NULL,'2011-02-10 06:36:38'),(24,2,'Storage Primary - 800 GB','Storage Primary - 800 GB',5120,'Disk',NULL,0,0,NULL,0,0,NULL,'2011-02-10 06:36:38'),(25,20,'Compute Small','1CPU, 2GB RAM, 160GB Storage',0,'Service',NULL,0,0,NULL,0,1,NULL,'2011-02-17 14:47:37'),(26,20,'Storage Primary - 50GB','Storage Primary - 50GB',5120,'Disk',NULL,0,0,NULL,0,0,NULL,'2011-02-17 14:48:58');
/*!40000 ALTER TABLE `disk_offering` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `domain`
--

DROP TABLE IF EXISTS `domain`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `domain` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `parent` bigint(20) unsigned default NULL,
  `name` varchar(255) default NULL,
  `owner` bigint(20) unsigned NOT NULL,
  `path` varchar(255) default NULL,
  `level` int(10) NOT NULL default '0',
  `child_count` int(10) NOT NULL default '0',
  `next_child_seq` bigint(20) unsigned NOT NULL default '1',
  `removed` datetime default NULL COMMENT 'date removed',
  `state` char(32) NOT NULL default 'Active' COMMENT 'state of the domain',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `parent` (`parent`,`name`,`removed`)
) ENGINE=InnoDB AUTO_INCREMENT=61 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `domain`
--

LOCK TABLES `domain` WRITE;
/*!40000 ALTER TABLE `domain` DISABLE KEYS */;
INSERT INTO `domain` VALUES (1,NULL,'ROOT',2,'/',0,3,4,NULL,'Active'),(2,1,'ezh',2,'/ezh/',1,33,40,NULL,'Active'),(3,2,'AA001000',1,'/ezh/AA001000/',2,1,2,NULL,'Active'),(4,3,'AA001000-cloudpsteam',1,'/ezh/AA001000/AA001000-cloudpsteam/',3,0,1,NULL,'Active'),(5,2,'AA001001',1,'/ezh/AA001001/',2,0,1,NULL,'Active'),(6,2,'AA001002',1,'/ezh/AA001002/',2,0,2,'2011-02-10 10:55:02','Active'),(7,6,'AA001002-terminateproject',1,'/ezh/AA001002/AA001002-terminateproject/',3,0,1,'2011-02-10 10:55:02','Active'),(8,2,'AA001002',1,'/ezh/AA001002/',2,0,1,NULL,'Active'),(9,2,'AA001003',1,'/ezh/AA001003/',2,0,1,NULL,'Active'),(10,2,'AA001004',1,'/ezh/AA001004/',2,0,1,NULL,'Active'),(11,2,'AA001005',1,'/ezh/AA001005/',2,0,1,'2011-02-10 23:38:41','Active'),(12,2,'AA001006',1,'/ezh/AA001006/',2,0,1,'2011-02-10 23:57:05','Active'),(13,2,'AA001006',1,'/ezh/AA001006/',2,0,1,NULL,'Active'),(14,2,'AA001007',1,'/ezh/AA001007/',2,1,2,NULL,'Active'),(15,2,'AA001008',1,'/ezh/AA001008/',2,1,2,NULL,'Active'),(16,15,'AA001008-Project-One',1,'/ezh/AA001008/AA001008-Project-One/',3,0,1,NULL,'Active'),(17,2,'AA001009',1,'/ezh/AA001009/',2,0,1,NULL,'Active'),(18,14,'AA001007-Bombiana',1,'/ezh/AA001007/AA001007-Bombiana/',3,0,1,NULL,'Active'),(19,2,'AA001010',1,'/ezh/AA001010/',2,0,1,NULL,'Active'),(20,1,'AA001011',1,'/AA001011/',1,2,3,NULL,'Active'),(21,20,'AA001011-Bee-Dee-One',1,'/AA001011/AA001011-Bee-Dee-One/',2,0,1,NULL,'Active'),(22,20,'AA001011-Bee-Dee-Two',1,'/AA001011/AA001011-Bee-Dee-Two/',2,0,1,NULL,'Active'),(23,2,'AA001012',1,'/ezh/AA001012/',2,0,1,NULL,'Active'),(24,2,'AA001013',1,'/ezh/AA001013/',2,0,1,'2011-02-23 09:40:38','Active'),(25,2,'AA001013',1,'/ezh/AA001013/',2,0,3,'2011-02-23 10:38:30','Active'),(26,25,'AA001013-lockproject',1,'/ezh/AA001013/AA001013-lockproject/',3,0,1,'2011-02-23 10:38:29','Active'),(27,25,'AA001013-lockadminproject',1,'/ezh/AA001013/AA001013-lockadminproject/',3,0,1,'2011-02-23 10:38:29','Active'),(28,2,'AA001013',1,'/ezh/AA001013/',2,1,2,NULL,'Active'),(29,28,'AA001013-locksecondproject',1,'/ezh/AA001013/AA001013-locksecondproject/',3,0,1,NULL,'Active'),(30,2,'AA001014',1,'/ezh/AA001014/',2,0,1,NULL,'Active'),(31,2,'AA001015',1,'/ezh/AA001015/',2,0,1,NULL,'Active'),(32,2,'AA001016',1,'/ezh/AA001016/',2,0,1,NULL,'Active'),(33,2,'AA001017',1,'/ezh/AA001017/',2,0,1,NULL,'Active'),(34,2,'AA001018',1,'/ezh/AA001018/',2,0,1,NULL,'Active'),(35,2,'AA001019',1,'/ezh/AA001019/',2,0,1,NULL,'Active'),(36,1,'AA001020',1,'/AA001020/',1,0,1,NULL,'Active'),(37,2,'AA001021',1,'/ezh/AA001021/',2,0,1,NULL,'Active'),(38,2,'AA001022',1,'/ezh/AA001022/',2,0,1,NULL,'Active'),(39,2,'AA001023',1,'/ezh/AA001023/',2,3,4,NULL,'Active'),(40,39,'AA001023-fdsadgfddddddsagfds',1,'/ezh/AA001023/AA001023-fdsadgfddddddsagfds/',3,0,1,NULL,'Active'),(41,39,'AA001023-fgdsagalk,hvb',1,'/ezh/AA001023/AA001023-fgdsagalk,hvb/',3,0,1,NULL,'Active'),(42,2,'AA001024',1,'/ezh/AA001024/',2,0,1,NULL,'Active'),(43,2,'AA001025',1,'/ezh/AA001025/',2,1,2,NULL,'Active'),(44,43,'AA001025-Manish-First-Project',1,'/ezh/AA001025/AA001025-Manish-First-Project/',3,0,1,NULL,'Active'),(45,39,'AA001023-hkhklk;kljkljklklklkl',1,'/ezh/AA001023/AA001023-hkhklk;kljkljklklklkl/',3,0,1,NULL,'Active'),(46,2,'AA001026',1,'/ezh/AA001026/',2,0,1,NULL,'Active'),(47,2,'AA001027',1,'/ezh/AA001027/',2,1,2,NULL,'Active'),(48,47,'AA001027-Atmos-Project-1',1,'/ezh/AA001027/AA001027-Atmos-Project-1/',3,0,1,NULL,'Active'),(49,2,'AA001028',1,'/ezh/AA001028/',2,0,1,NULL,'Active'),(50,2,'AA001029',1,'/ezh/AA001029/',2,0,1,NULL,'Active'),(51,2,'AA001030',1,'/ezh/AA001030/',2,1,2,NULL,'Active'),(52,51,'AA001030-pmorris-project1',1,'/ezh/AA001030/AA001030-pmorris-project1/',3,0,1,NULL,'Active'),(53,2,'AA001031',1,'/ezh/AA001031/',2,0,1,NULL,'Active'),(54,2,'AA001032',1,'/ezh/AA001032/',2,0,1,NULL,'Active'),(55,2,'AA001033',1,'/ezh/AA001033/',2,1,2,NULL,'Active'),(56,2,'AA001034',1,'/ezh/AA001034/',2,1,2,NULL,'Active'),(57,56,'AA001034-KKproject',1,'/ezh/AA001034/AA001034-KKproject/',3,0,1,NULL,'Active'),(58,55,'AA001033-test_for_ticket',1,'/ezh/AA001033/AA001033-test_for_ticket/',3,0,1,NULL,'Active'),(59,2,'AA001035',1,'/ezh/AA001035/',2,0,1,'2011-05-16 17:52:56','Inactive'),(60,2,'AA001036',1,'/ezh/AA001036/',2,0,1,NULL,'Active');
/*!40000 ALTER TABLE `domain` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `domain_router`
--

DROP TABLE IF EXISTS `domain_router`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `domain_router` (
  `id` bigint(20) unsigned NOT NULL COMMENT 'Primary Key',
  `ram_size` int(10) unsigned NOT NULL default '128' COMMENT 'memory to use in mb',
  `domain` varchar(255) default NULL COMMENT 'domain',
  `public_mac_address` varchar(17) default NULL COMMENT 'mac address of the public facing network card',
  `public_ip_address` varchar(15) default NULL COMMENT 'public ip address used for source net',
  `public_netmask` varchar(15) default NULL COMMENT 'netmask used for the domR',
  `guest_mac_address` varchar(17) default NULL COMMENT 'mac address of the pod facing network card',
  `guest_netmask` varchar(15) default NULL COMMENT 'netmask used for the guest network',
  `guest_ip_address` varchar(15) default NULL COMMENT ' ip address in the guest network',
  `network_id` bigint(20) unsigned NOT NULL default '0' COMMENT 'network configuration that this domain router belongs to',
  `role` varchar(64) NOT NULL COMMENT 'type of role played by this router',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`),
  CONSTRAINT `fk_domain_router__id` FOREIGN KEY (`id`) REFERENCES `vm_instance` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='information about the domR instance';
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `domain_router`
--

LOCK TABLES `domain_router` WRITE;
/*!40000 ALTER TABLE `domain_router` DISABLE KEYS */;
INSERT INTO `domain_router` VALUES (4,0,'cs4cloud.internal','06:a3:b2:00:00:0f','66.149.231.112','255.255.255.0',NULL,NULL,'10.1.1.1',204,'DHCP_FIREWALL_LB_PASSWD_USERDATA'),(6,0,'cs2cloud.internal','06:7f:90:00:00:0e','66.149.231.111','255.255.255.0',NULL,NULL,'10.1.1.1',205,'DHCP_FIREWALL_LB_PASSWD_USERDATA'),(10,0,'cs6cloud.internal','06:7a:84:00:00:06','66.149.231.103','255.255.255.0',NULL,NULL,'10.1.1.1',206,'DHCP_FIREWALL_LB_PASSWD_USERDATA'),(13,0,'cs18cloud.internal','06:c0:96:00:00:09','66.149.231.106','255.255.255.0',NULL,NULL,'10.1.1.1',207,'DHCP_FIREWALL_LB_PASSWD_USERDATA'),(15,0,'cs16cloud.internal','06:28:22:00:00:0a','66.149.231.107','255.255.255.0',NULL,NULL,'10.1.1.1',208,'DHCP_FIREWALL_LB_PASSWD_USERDATA'),(18,0,'cs1acloud.internal','06:82:58:00:00:07','66.149.231.104','255.255.255.0',NULL,NULL,'10.1.1.1',209,'DHCP_FIREWALL_LB_PASSWD_USERDATA'),(20,0,'cs1ecloud.internal','06:dd:08:00:00:16','66.149.231.119','255.255.255.0',NULL,NULL,'10.1.1.1',210,'DHCP_FIREWALL_LB_PASSWD_USERDATA'),(22,0,'cs14cloud.internal','06:c4:9a:00:00:13','66.149.231.116','255.255.255.0',NULL,NULL,'10.1.1.1',211,'DHCP_FIREWALL_LB_PASSWD_USERDATA'),(36,128,NULL,'06:28:46:00:00:04','66.149.231.101','255.255.255.0',NULL,NULL,'10.1.1.1',215,'DHCP_FIREWALL_LB_PASSWD_USERDATA'),(43,128,NULL,'06:79:82:00:00:09','172.16.64.43','255.255.252.0',NULL,NULL,'10.1.3.1',222,'DHCP_FIREWALL_LB_PASSWD_USERDATA'),(45,128,NULL,'06:d7:3c:00:00:08','172.16.64.42','255.255.252.0',NULL,NULL,'10.1.3.1',223,'DHCP_FIREWALL_LB_PASSWD_USERDATA'),(47,128,NULL,'06:23:ea:00:00:06','172.16.64.40','255.255.252.0',NULL,NULL,'10.1.3.1',224,'DHCP_FIREWALL_LB_PASSWD_USERDATA');
/*!40000 ALTER TABLE `domain_router` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `event`
--

DROP TABLE IF EXISTS `event`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `event` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `type` varchar(32) NOT NULL,
  `state` varchar(32) NOT NULL default 'Completed',
  `description` varchar(1024) NOT NULL,
  `user_id` bigint(20) unsigned NOT NULL,
  `account_id` bigint(20) unsigned NOT NULL,
  `created` datetime NOT NULL,
  `level` varchar(16) NOT NULL,
  `start_id` bigint(20) unsigned NOT NULL default '0',
  `parameters` varchar(1024) default NULL,
  PRIMARY KEY  (`id`),
  KEY `i_event__created` (`created`),
  KEY `i_event__user_id` (`user_id`),
  KEY `i_event__account_id` (`account_id`),
  KEY `i_event__level_id` (`level`),
  KEY `i_event__type_id` (`type`)
) ENGINE=InnoDB AUTO_INCREMENT=492 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `event`
--

LOCK TABLES `event` WRITE;
/*!40000 ALTER TABLE `event` DISABLE KEYS */;
INSERT INTO `event` VALUES (1,'USER.LOGOUT','Completed','user has logged out',2,2,'2011-02-10 06:24:25','INFO',0,NULL),(2,'USER.LOGOUT','Completed','user has logged out',2,2,'2011-02-10 06:50:16','INFO',0,NULL),(3,'NETWORK.CREATE','Completed','Successfully completed creating network. Network Id: 204',4,0,'2011-02-10 08:42:57','INFO',0,NULL),(4,'VM.CREATE','Created','Successfully created entity for deploying Vm. Vm Id: 3',4,4,'2011-02-10 08:43:05','INFO',0,NULL),(5,'VM.CREATE','Scheduled','Scheduled async job for starting Vm. Vm Id: 3',4,4,'2011-02-10 08:43:05','INFO',4,NULL),(6,'VM.CREATE','Started','Starting job for starting Vm. Vm Id: 3',4,4,'2011-02-10 08:43:05','INFO',4,NULL),(7,'VM.CREATE','Completed','Successfully completed starting Vm. Vm Id: 3',4,4,'2011-02-10 08:44:45','INFO',4,NULL),(8,'NETWORK.CREATE','Completed','Successfully completed creating network. Network Id: 205',2,0,'2011-02-10 08:46:25','INFO',0,NULL),(9,'VM.CREATE','Created','Successfully created entity for deploying Vm. Vm Id: 5',2,2,'2011-02-10 08:46:42','INFO',0,NULL),(10,'VM.CREATE','Scheduled','Scheduled async job for starting Vm. Vm Id: 5',2,2,'2011-02-10 08:46:42','INFO',9,NULL),(11,'VM.CREATE','Started','Starting job for starting Vm. Vm Id: 5',2,2,'2011-02-10 08:46:42','INFO',9,NULL),(12,'VM.CREATE','Completed','Successfully completed starting Vm. Vm Id: 5',2,2,'2011-02-10 08:47:32','INFO',9,NULL),(13,'VM.CREATE','Created','Successfully created entity for deploying Vm. Vm Id: 7',4,4,'2011-02-10 08:47:36','INFO',0,NULL),(14,'VM.CREATE','Scheduled','Scheduled async job for starting Vm. Vm Id: 7',4,4,'2011-02-10 08:47:36','INFO',13,NULL),(15,'VM.CREATE','Started','Starting job for starting Vm. Vm Id: 7',4,4,'2011-02-10 08:47:36','INFO',13,NULL),(16,'VM.CREATE','Completed','Successfully completed starting Vm. Vm Id: 7',4,4,'2011-02-10 08:47:48','INFO',13,NULL),(17,'VM.STOP','Scheduled','Scheduled async job for stopping user vm: 5',2,2,'2011-02-10 08:48:21','INFO',0,NULL),(18,'VM.STOP','Started','Starting job for stopping Vm. Vm Id: 5',2,2,'2011-02-10 08:48:21','INFO',17,NULL),(19,'VM.STOP','Completed','Successfully completed stopping Vm. Vm Id: 5',2,2,'2011-02-10 08:48:45','INFO',17,NULL),(20,'VM.CREATE','Created','Successfully created entity for deploying Vm. Vm Id: 8',4,4,'2011-02-10 08:48:51','INFO',0,NULL),(21,'VM.CREATE','Scheduled','Scheduled async job for starting Vm. Vm Id: 8',4,4,'2011-02-10 08:48:51','INFO',20,NULL),(22,'VM.CREATE','Started','Starting job for starting Vm. Vm Id: 8',4,4,'2011-02-10 08:48:51','INFO',20,NULL),(23,'VM.CREATE','Completed','Successfully completed starting Vm. Vm Id: 8',4,4,'2011-02-10 08:49:02','INFO',20,NULL),(24,'VOLUME.EXTRACT','Scheduled','Scheduled async job for Extraction job',2,2,'2011-02-10 08:50:00','INFO',0,NULL),(25,'VOLUME.EXTRACT','Started','Starting job for extracting volume. Volume Id: 6',2,2,'2011-02-10 08:50:00','INFO',24,NULL),(26,'SNAPSHOT.CREATE','Scheduled','Scheduled async job for creating snapshot for volume: 6',2,2,'2011-02-10 08:50:44','INFO',0,NULL),(27,'SNAPSHOT.CREATE','Started','Starting job for creating snapshot',2,2,'2011-02-10 08:50:44','INFO',26,NULL),(28,'VM.STOP','Scheduled','Scheduled async job for stopping user vm: 7',4,4,'2011-02-10 08:51:17','INFO',0,NULL),(29,'VM.STOP','Started','Starting job for stopping Vm. Vm Id: 7',4,4,'2011-02-10 08:51:17','INFO',28,NULL),(30,'VOLUME.EXTRACT','Completed','Successfully completed extracting volume. Volume Id: 6',2,2,'2011-02-10 08:52:41','INFO',24,NULL),(31,'VM.STOP','Completed','Successfully completed stopping Vm. Vm Id: 7',4,4,'2011-02-10 08:53:08','INFO',28,NULL),(32,'SNAPSHOT.CREATE','Completed','Successfully completed creating snapshot',2,2,'2011-02-10 08:53:09','INFO',26,NULL),(33,'TEMPLATE.CREATE','Scheduled','Scheduled async job for creating template: CentOS55 - Xen',1,2,'2011-02-10 09:07:39','INFO',0,NULL),(34,'VM.DESTROY','Scheduled','Scheduled async job for destroying vm: 5',2,2,'2011-02-10 09:08:38','INFO',0,NULL),(35,'VM.DESTROY','Started','Starting job for destroying Vm. Vm Id: 5',2,2,'2011-02-10 09:08:38','INFO',34,NULL),(36,'VM.DESTROY','Completed','Successfully completed destroying Vm. Vm Id: 5',2,2,'2011-02-10 09:08:38','INFO',34,NULL),(37,'TEMPLATE.CREATE','Scheduled','Scheduled async job for creating template: CentOS55LAMP - Xen',1,2,'2011-02-10 09:10:39','INFO',0,NULL),(38,'NET.IPASSIGN','Created','Successfully created entity for allocating Ip. Ip Id: 17',4,4,'2011-02-10 09:11:42','INFO',0,NULL),(39,'NET.IPASSIGN','Scheduled','Scheduled async job for associating ip to network id: 204 in zone 1',4,4,'2011-02-10 09:11:42','INFO',38,NULL),(40,'NET.IPASSIGN','Started','Starting job for associating Ip. Ip Id: 17',4,4,'2011-02-10 09:11:43','INFO',38,NULL),(41,'NET.IPASSIGN','Completed','Successfully completed associating Ip. Ip Id: 17',4,4,'2011-02-10 09:11:50','INFO',38,NULL),(42,'TEMPLATE.CREATE','Scheduled','Scheduled async job for creating template: CentOS55LAMP_BP - Xen',1,2,'2011-02-10 09:13:39','INFO',0,NULL),(43,'TEMPLATE.CREATE','Scheduled','Scheduled async job for creating template: CentOS55MySQL - Xen',1,2,'2011-02-10 09:16:39','INFO',0,NULL),(44,'TEMPLATE.CREATE','Scheduled','Scheduled async job for creating template: CentOS55MySQL_BP - Xen',1,2,'2011-02-10 09:19:39','INFO',0,NULL),(45,'NET.RULEADD','Created','Successfully created entity for creating forwarding rule. Rule Id: 1',4,4,'2011-02-10 09:20:20','INFO',0,NULL),(46,'NET.RULEADD','Scheduled','Scheduled async job for Applying port forwarding  rule for Ip: 66.149.231.117 with virtual machine:3',4,4,'2011-02-10 09:20:20','INFO',45,NULL),(47,'NET.RULEADD','Started','Starting job for applying forwarding rule. Rule Id: 1',4,4,'2011-02-10 09:20:21','INFO',45,NULL),(48,'NET.RULEADD','Completed','Successfully completed applying forwarding rule. Rule Id: 1',4,4,'2011-02-10 09:20:22','INFO',45,NULL),(49,'TEMPLATE.CREATE','Scheduled','Scheduled async job for creating template: RHEL55 - Xen',1,2,'2011-02-10 09:22:39','INFO',0,NULL),(50,'LB.CREATE','Completed','Successfully completed creating load balancer. Load balancer Id: 2',4,0,'2011-02-10 09:23:42','INFO',0,NULL),(51,'LB.ASSIGN.TO.RULE','Scheduled','Scheduled async job for applying instances for load balancer: 2 (ids: 3)',4,4,'2011-02-10 09:23:47','INFO',0,NULL),(52,'LB.ASSIGN.TO.RULE','Started','Starting job for assigning to load balancer. Load balancer Id: 2 VmIds: 3',4,4,'2011-02-10 09:23:47','INFO',51,NULL),(53,'LB.ASSIGN.TO.RULE','Completed','Successfully completed assigning to load balancer. Load balancer Id: 2 VmIds: 3',4,4,'2011-02-10 09:23:52','INFO',51,NULL),(54,'LB.ASSIGN.TO.RULE','Scheduled','Scheduled async job for applying instances for load balancer: 2 (ids: 7)',4,4,'2011-02-10 09:23:56','INFO',0,NULL),(55,'LB.ASSIGN.TO.RULE','Started','Starting job for assigning to load balancer. Load balancer Id: 2 VmIds: 7',4,4,'2011-02-10 09:23:56','INFO',54,NULL),(56,'LB.ASSIGN.TO.RULE','Completed','Successfully completed assigning to load balancer. Load balancer Id: 2 VmIds: 7',4,4,'2011-02-10 09:24:00','INFO',54,NULL),(57,'LB.ASSIGN.TO.RULE','Scheduled','Scheduled async job for applying instances for load balancer: 2 (ids: 7)',4,4,'2011-02-10 09:24:04','INFO',0,NULL),(58,'LB.ASSIGN.TO.RULE','Started','Starting job for assigning to load balancer. Load balancer Id: 2 VmIds: 7',4,4,'2011-02-10 09:24:04','INFO',57,NULL),(59,'LB.ASSIGN.TO.RULE','Completed','Error while assigning to load balancer. Load balancer Id: 2 VmIds: 7',4,4,'2011-02-10 09:24:04','ERROR',57,NULL),(60,'LB.ASSIGN.TO.RULE','Scheduled','Scheduled async job for applying instances for load balancer: 2 (ids: 8)',4,4,'2011-02-10 09:24:14','INFO',0,NULL),(61,'LB.ASSIGN.TO.RULE','Started','Starting job for assigning to load balancer. Load balancer Id: 2 VmIds: 8',4,4,'2011-02-10 09:24:14','INFO',60,NULL),(62,'LB.ASSIGN.TO.RULE','Completed','Successfully completed assigning to load balancer. Load balancer Id: 2 VmIds: 8',4,4,'2011-02-10 09:24:18','INFO',60,NULL),(63,'TEMPLATE.CREATE','Scheduled','Scheduled async job for creating template: RHEL55LAMP_BP - Xen',1,2,'2011-02-10 09:25:40','INFO',0,NULL),(64,'TEMPLATE.CREATE','Scheduled','Scheduled async job for creating template: Ubuntu1010 - Xen',1,2,'2011-02-10 09:28:40','INFO',0,NULL),(65,'TEMPLATE.CREATE','Scheduled','Scheduled async job for creating template: Windows2003SP2_32bit - Xen',1,2,'2011-02-10 09:31:40','INFO',0,NULL),(66,'TEMPLATE.CREATE','Scheduled','Scheduled async job for creating template: Windows2008R2 - Xen',1,2,'2011-02-10 09:34:40','INFO',0,NULL),(67,'TEMPLATE.CREATE','Scheduled','Scheduled async job for creating template: Windows2008R2_32bit - Xen',1,2,'2011-02-10 09:37:40','INFO',0,NULL),(68,'TEMPLATE.CREATE','Scheduled','Scheduled async job for creating template: Windows2008R2_BP - Xen',1,2,'2011-02-10 09:40:40','INFO',0,NULL),(69,'TEMPLATE.CREATE','Scheduled','Scheduled async job for creating template: Windows2008R2_IIS - Xen',1,2,'2011-02-10 09:43:40','INFO',0,NULL),(70,'TEMPLATE.CREATE','Scheduled','Scheduled async job for creating template: Windows2008R2_IIS_BP - Xen',1,2,'2011-02-10 09:46:40','INFO',0,NULL),(71,'TEMPLATE.CREATE','Scheduled','Scheduled async job for creating template: Windows2008R2_SQL2008 - Xen',1,2,'2011-02-10 09:49:40','INFO',0,NULL),(72,'TEMPLATE.CREATE','Scheduled','Scheduled async job for creating template: Windows2008R2_SQL2008_BP - Xen',1,2,'2011-02-10 09:52:41','INFO',0,NULL),(73,'TEMPLATE.CREATE','Scheduled','Scheduled async job for creating template: Windows2008R2_WISA - Xen',1,2,'2011-02-10 09:55:41','INFO',0,NULL),(74,'TEMPLATE.CREATE','Scheduled','Scheduled async job for creating template: Windows2008R2_WISA_BP - Xen',1,2,'2011-02-10 09:58:41','INFO',0,NULL),(75,'TEMPLATE.CREATE','Scheduled','Scheduled async job for creating template: Windows7SP1 - Xen',1,2,'2011-02-10 10:01:41','INFO',0,NULL),(76,'TEMPLATE.CREATE','Scheduled','Scheduled async job for creating template: WindowsXPSP3 - Xen',1,2,'2011-02-10 10:04:41','INFO',0,NULL),(77,'NETWORK.CREATE','Completed','Successfully completed creating network. Network Id: 206',6,0,'2011-02-10 10:49:41','INFO',0,NULL),(78,'VM.CREATE','Created','Successfully created entity for deploying Vm. Vm Id: 9',6,6,'2011-02-10 10:49:48','INFO',0,NULL),(79,'VM.CREATE','Scheduled','Scheduled async job for starting Vm. Vm Id: 9',6,6,'2011-02-10 10:49:48','INFO',78,NULL),(80,'VM.CREATE','Started','Starting job for starting Vm. Vm Id: 9',6,6,'2011-02-10 10:49:48','INFO',78,NULL),(81,'VM.CREATE','Completed','Successfully completed starting Vm. Vm Id: 9',6,6,'2011-02-10 10:52:26','INFO',78,NULL),(82,'DOMAIN.DELETE','Scheduled','Scheduled async job for deleting domain: 6',1,1,'2011-02-10 10:54:36','INFO',0,NULL),(83,'VM.START','Scheduled','Scheduled async job for starting user vm: 7',3,4,'2011-02-10 13:26:37','INFO',0,NULL),(84,'VM.START','Started','Starting job for starting Vm. Vm Id: 7',3,4,'2011-02-10 13:26:37','INFO',83,NULL),(85,'VM.START','Completed','Successfully completed starting Vm. Vm Id: 7',3,4,'2011-02-10 13:26:47','INFO',83,NULL),(86,'VM.START','Scheduled','Scheduled async job for starting user vm: 3',3,4,'2011-02-10 13:27:24','INFO',0,NULL),(87,'VM.START','Started','Starting job for starting Vm. Vm Id: 3',3,4,'2011-02-10 13:27:25','INFO',86,NULL),(88,'VM.START','Completed','Successfully completed starting Vm. Vm Id: 3',3,4,'2011-02-10 13:27:36','INFO',86,NULL),(89,'DOMAIN.DELETE','Scheduled','Scheduled async job for deleting domain: 11',1,1,'2011-02-10 23:38:41','INFO',0,NULL),(90,'ACCOUNT.DISABLE','Scheduled','Scheduled async job for disabling account: pminc2 in domain: 12',1,1,'2011-02-10 23:56:22','INFO',0,NULL),(91,'ACCOUNT.DISABLE','Scheduled','Scheduled async job for disabling account: pminc2 in domain: 12',1,1,'2011-02-10 23:56:40','INFO',0,NULL),(92,'DOMAIN.DELETE','Scheduled','Scheduled async job for deleting domain: 12',1,1,'2011-02-10 23:57:05','INFO',0,NULL),(93,'ACCOUNT.DISABLE','Scheduled','Scheduled async job for disabling account: suspension in domain: 17',1,1,'2011-02-11 12:07:55','INFO',0,NULL),(94,'VPN.REMOTE.ACCESS.CREATE','Scheduled','Scheduled async job for Create Remote Access VPN for account 2 using public ip id=11',2,2,'2011-02-15 20:11:42','INFO',0,NULL),(95,'ROUTER.START','Scheduled','Scheduled async job for starting router: 6',2,1,'2011-02-15 20:19:20','INFO',0,NULL),(96,'VPN.REMOTE.ACCESS.CREATE','Scheduled','Scheduled async job for Create Remote Access VPN for account 2 using public ip id=11',2,2,'2011-02-15 20:20:31','INFO',0,NULL),(97,'VPN.USER.ADD','Scheduled','Scheduled async job for Add Remote Access VPN user for account 2 username= david',2,2,'2011-02-15 20:21:56','INFO',0,NULL),(98,'VM.CREATE','Created','Successfully created entity for deploying Vm. Vm Id: 11',2,2,'2011-02-15 20:33:45','INFO',0,NULL),(99,'VM.CREATE','Scheduled','Scheduled async job for starting Vm. Vm Id: 11',2,2,'2011-02-15 20:33:45','INFO',98,NULL),(100,'VM.CREATE','Started','Starting job for starting Vm. Vm Id: 11',2,2,'2011-02-15 20:33:45','INFO',98,NULL),(101,'VM.CREATE','Completed','Successfully completed starting Vm. Vm Id: 11',2,2,'2011-02-15 20:35:27','INFO',98,NULL),(102,'VPN.USER.REMOVE','Scheduled','Scheduled async job for Remove Remote Access VPN user for account 2 username= david',2,2,'2011-02-15 20:49:44','INFO',0,NULL),(103,'VPN.REMOTE.ACCESS.DESTROY','Scheduled','Scheduled async job for Delete Remote Access VPN for account 2 for  ip id=11',2,2,'2011-02-15 20:49:54','INFO',0,NULL),(104,'VM.DESTROY','Scheduled','Scheduled async job for destroying vm: 11',2,2,'2011-02-15 20:50:19','INFO',0,NULL),(105,'VM.DESTROY','Started','Starting job for destroying Vm. Vm Id: 11',2,2,'2011-02-15 20:50:19','INFO',104,NULL),(106,'ROUTER.STOP','Scheduled','Scheduled async job for stopping router: 6',2,1,'2011-02-15 20:50:35','INFO',0,NULL),(107,'VM.DESTROY','Completed','Successfully completed destroying Vm. Vm Id: 11',2,2,'2011-02-15 20:50:44','INFO',104,NULL),(108,'NETWORK.CREATE','Completed','Successfully completed creating network. Network Id: 207',24,0,'2011-02-17 14:49:43','INFO',0,NULL),(109,'VM.CREATE','Created','Successfully created entity for deploying Vm. Vm Id: 12',24,24,'2011-02-17 14:49:57','INFO',0,NULL),(110,'VM.CREATE','Scheduled','Scheduled async job for starting Vm. Vm Id: 12',24,24,'2011-02-17 14:49:57','INFO',109,NULL),(111,'VM.CREATE','Started','Starting job for starting Vm. Vm Id: 12',24,24,'2011-02-17 14:49:57','INFO',109,NULL),(112,'VM.CREATE','Completed','Successfully completed starting Vm. Vm Id: 12',24,24,'2011-02-17 14:52:28','INFO',109,NULL),(113,'NETWORK.CREATE','Completed','Successfully completed creating network. Network Id: 208',22,0,'2011-02-17 14:53:20','INFO',0,NULL),(114,'VM.CREATE','Created','Successfully created entity for deploying Vm. Vm Id: 14',22,22,'2011-02-17 14:53:44','INFO',0,NULL),(115,'VM.CREATE','Scheduled','Scheduled async job for starting Vm. Vm Id: 14',22,22,'2011-02-17 14:53:44','INFO',114,NULL),(116,'VM.CREATE','Started','Starting job for starting Vm. Vm Id: 14',22,22,'2011-02-17 14:53:44','INFO',114,NULL),(117,'VM.CREATE','Completed','Successfully completed starting Vm. Vm Id: 14',22,22,'2011-02-17 14:56:01','INFO',114,NULL),(118,'VM.CREATE','Created','Successfully created entity for deploying Vm. Vm Id: 16',2,2,'2011-02-21 04:49:43','INFO',0,NULL),(119,'VM.CREATE','Scheduled','Scheduled async job for starting Vm. Vm Id: 16',2,2,'2011-02-21 04:49:43','INFO',118,NULL),(120,'VM.CREATE','Started','Starting job for starting Vm. Vm Id: 16',2,2,'2011-02-21 04:49:43','INFO',118,NULL),(121,'VM.CREATE','Completed','Successfully completed starting Vm. Vm Id: 16',2,2,'2011-02-21 04:51:47','INFO',118,NULL),(122,'NETWORK.CREATE','Completed','Successfully completed creating network. Network Id: 209',26,0,'2011-02-23 09:00:50','INFO',0,NULL),(123,'VM.CREATE','Created','Successfully created entity for deploying Vm. Vm Id: 17',26,26,'2011-02-23 09:01:05','INFO',0,NULL),(124,'VM.CREATE','Scheduled','Scheduled async job for starting Vm. Vm Id: 17',26,26,'2011-02-23 09:01:05','INFO',123,NULL),(125,'VM.CREATE','Started','Starting job for starting Vm. Vm Id: 17',26,26,'2011-02-23 09:01:06','INFO',123,NULL),(126,'VM.CREATE','Completed','Successfully completed starting Vm. Vm Id: 17',26,26,'2011-02-23 09:02:14','INFO',123,NULL),(127,'NET.IPASSIGN','Created','Successfully created entity for allocating Ip. Ip Id: 5',26,26,'2011-02-23 09:04:48','INFO',0,NULL),(128,'NET.IPASSIGN','Scheduled','Scheduled async job for associating ip to network id: 209 in zone 1',26,26,'2011-02-23 09:04:48','INFO',127,NULL),(129,'NET.IPASSIGN','Started','Starting job for associating Ip. Ip Id: 5',26,26,'2011-02-23 09:04:48','INFO',127,NULL),(130,'NET.IPASSIGN','Completed','Successfully completed associating Ip. Ip Id: 5',26,26,'2011-02-23 09:04:53','INFO',127,NULL),(131,'ACCOUNT.DISABLE','Scheduled','Scheduled async job for disabling account: locktest in domain: 24',1,1,'2011-02-23 09:19:51','INFO',0,NULL),(132,'DOMAIN.DELETE','Scheduled','Scheduled async job for deleting domain: 24',1,1,'2011-02-23 09:40:37','INFO',0,NULL),(133,'NETWORK.CREATE','Completed','Successfully completed creating network. Network Id: 210',30,0,'2011-02-23 10:19:49','INFO',0,NULL),(134,'VM.CREATE','Created','Successfully created entity for deploying Vm. Vm Id: 19',30,30,'2011-02-23 10:20:15','INFO',0,NULL),(135,'VM.CREATE','Scheduled','Scheduled async job for starting Vm. Vm Id: 19',30,30,'2011-02-23 10:20:15','INFO',134,NULL),(136,'VM.CREATE','Started','Starting job for starting Vm. Vm Id: 19',30,30,'2011-02-23 10:20:15','INFO',134,NULL),(137,'VM.CREATE','Completed','Successfully completed starting Vm. Vm Id: 19',30,30,'2011-02-23 10:21:05','INFO',134,NULL),(138,'NET.IPASSIGN','Created','Successfully created entity for allocating Ip. Ip Id: 20',30,30,'2011-02-23 10:22:09','INFO',0,NULL),(139,'NET.IPASSIGN','Scheduled','Scheduled async job for associating ip to network id: 210 in zone 1',30,30,'2011-02-23 10:22:09','INFO',138,NULL),(140,'NET.IPASSIGN','Started','Starting job for associating Ip. Ip Id: 20',30,30,'2011-02-23 10:22:09','INFO',138,NULL),(141,'NET.IPASSIGN','Completed','Successfully completed associating Ip. Ip Id: 20',30,30,'2011-02-23 10:22:14','INFO',138,NULL),(142,'ACCOUNT.DISABLE','Scheduled','Scheduled async job for disabling account: locktest in domain: 25',1,1,'2011-02-23 10:34:49','INFO',0,NULL),(143,'ACCOUNT.DISABLE','Scheduled','Scheduled async job for disabling account: locktest in domain: 26',1,1,'2011-02-23 10:34:49','INFO',0,NULL),(144,'ACCOUNT.DISABLE','Scheduled','Scheduled async job for disabling account: lockadmin in domain: 27',1,1,'2011-02-23 10:34:49','INFO',0,NULL),(145,'ACCOUNT.DISABLE','Scheduled','Scheduled async job for disabling account: lockuser in domain: 27',1,1,'2011-02-23 10:34:49','INFO',0,NULL),(146,'DOMAIN.DELETE','Scheduled','Scheduled async job for deleting domain: 25',1,1,'2011-02-23 10:38:28','INFO',0,NULL),(147,'SSVM.START','Scheduled','Scheduled async job for starting system vm: 2',2,2,'2011-03-01 21:31:43','INFO',0,NULL),(148,'SSVM.REBOOT','Scheduled','Scheduled async job for rebooting system vm: 2',2,2,'2011-03-01 21:47:14','INFO',0,NULL),(149,'ISO.DELETE','Scheduled','Scheduled async job for Deleting iso 225',2,2,'2011-03-02 04:22:38','INFO',0,NULL),(150,'SNAPSHOT.CREATE','Scheduled','Scheduled async job for creating snapshot for volume: 19',2,2,'2011-03-08 12:35:47','INFO',0,NULL),(151,'SNAPSHOT.CREATE','Started','Starting job for creating snapshot',2,2,'2011-03-08 12:35:47','INFO',150,NULL),(152,'SNAPSHOT.CREATE','Completed','Successfully completed creating snapshot',2,2,'2011-03-08 12:38:32','INFO',150,NULL),(153,'SNAPSHOT.CREATE','Scheduled','Scheduled async job for creating snapshot for volume: 4',2,4,'2011-03-08 12:38:44','INFO',0,NULL),(154,'SNAPSHOT.CREATE','Started','Starting job for creating snapshot',2,4,'2011-03-08 12:38:44','INFO',153,NULL),(155,'SNAPSHOT.CREATE','Completed','Successfully completed creating snapshot',2,4,'2011-03-08 12:38:44','INFO',153,NULL),(156,'SNAPSHOT.CREATE','Scheduled','Scheduled async job for creating snapshot for volume: 4',2,4,'2011-03-08 12:42:55','INFO',0,NULL),(157,'SNAPSHOT.CREATE','Started','Starting job for creating snapshot',2,4,'2011-03-08 12:42:55','INFO',156,NULL),(158,'SNAPSHOT.CREATE','Completed','Successfully completed creating snapshot',2,4,'2011-03-08 12:43:51','INFO',156,NULL),(159,'SNAPSHOT.CREATE','Scheduled','Scheduled async job for creating snapshot for volume: 3',2,4,'2011-03-08 12:45:15','INFO',0,NULL),(160,'SNAPSHOT.CREATE','Started','Starting job for creating snapshot',2,4,'2011-03-08 12:45:15','INFO',159,NULL),(161,'SNAPSHOT.CREATE','Completed','Successfully completed creating snapshot',2,4,'2011-03-08 12:45:15','INFO',159,NULL),(162,'SNAPSHOT.CREATE','Scheduled','Scheduled async job for creating snapshot for volume: 16',2,22,'2011-03-08 12:46:06','INFO',0,NULL),(163,'SNAPSHOT.CREATE','Started','Starting job for creating snapshot',2,22,'2011-03-08 12:46:06','INFO',162,NULL),(164,'SNAPSHOT.CREATE','Completed','Successfully completed creating snapshot',2,22,'2011-03-08 12:46:06','INFO',162,NULL),(165,'SNAPSHOT.CREATE','Scheduled','Scheduled async job for creating snapshot for volume: 16',20,22,'2011-03-08 13:26:44','INFO',0,NULL),(166,'SNAPSHOT.CREATE','Started','Starting job for creating snapshot',20,22,'2011-03-08 13:26:44','INFO',165,NULL),(167,'SNAPSHOT.CREATE','Completed','Successfully completed creating snapshot',20,22,'2011-03-08 13:26:44','INFO',165,NULL),(168,'SNAPSHOT.CREATE','Scheduled','Scheduled async job for creating snapshot for volume: 17',20,22,'2011-03-08 13:27:07','INFO',0,NULL),(169,'SNAPSHOT.CREATE','Started','Starting job for creating snapshot',20,22,'2011-03-08 13:27:07','INFO',168,NULL),(170,'SNAPSHOT.CREATE','Completed','Successfully completed creating snapshot',20,22,'2011-03-08 13:27:07','INFO',168,NULL),(171,'SNAPSHOT.CREATE','Scheduled','Scheduled async job for creating snapshot for volume: 14',20,24,'2011-03-08 13:30:06','INFO',0,NULL),(172,'SNAPSHOT.CREATE','Started','Starting job for creating snapshot',20,24,'2011-03-08 13:30:06','INFO',171,NULL),(173,'SNAPSHOT.CREATE','Completed','Successfully completed creating snapshot',20,24,'2011-03-08 13:31:00','INFO',171,NULL),(174,'SNAPSHOT.CREATE','Scheduled','Scheduled async job for creating snapshot for volume: 14',24,24,'2011-03-08 13:40:15','INFO',0,NULL),(175,'SNAPSHOT.CREATE','Started','Starting job for creating snapshot',24,24,'2011-03-08 13:40:15','INFO',174,NULL),(176,'SNAPSHOT.CREATE','Completed','Successfully completed creating snapshot',24,24,'2011-03-08 13:40:15','INFO',174,NULL),(177,'SNAPSHOT.CREATE','Scheduled','Scheduled async job for creating snapshot for volume: 14',24,24,'2011-03-08 13:43:18','INFO',0,NULL),(178,'SNAPSHOT.CREATE','Started','Starting job for creating snapshot',24,24,'2011-03-08 13:43:18','INFO',177,NULL),(179,'SNAPSHOT.CREATE','Completed','Successfully completed creating snapshot',24,24,'2011-03-08 13:43:18','INFO',177,NULL),(180,'SNAPSHOT.CREATE','Scheduled','Scheduled async job for creating snapshot for volume: 14',24,24,'2011-03-08 13:44:14','INFO',0,NULL),(181,'SNAPSHOT.CREATE','Started','Starting job for creating snapshot',24,24,'2011-03-08 13:44:14','INFO',180,NULL),(182,'SNAPSHOT.CREATE','Completed','Successfully completed creating snapshot',24,24,'2011-03-08 13:44:15','INFO',180,NULL),(183,'SNAPSHOT.CREATE','Scheduled','Scheduled async job for creating snapshot for volume: 14',23,24,'2011-03-09 10:31:30','INFO',0,NULL),(184,'SNAPSHOT.CREATE','Started','Starting job for creating snapshot',23,24,'2011-03-09 10:31:30','INFO',183,NULL),(185,'SNAPSHOT.CREATE','Completed','Successfully completed creating snapshot',23,24,'2011-03-09 10:31:32','INFO',183,NULL),(186,'NETWORK.CREATE','Completed','Successfully completed creating network. Network Id: 211',20,0,'2011-03-09 10:51:23','INFO',0,NULL),(187,'VM.CREATE','Created','Successfully created entity for deploying Vm. Vm Id: 21',20,20,'2011-03-09 10:51:45','INFO',0,NULL),(188,'VM.CREATE','Scheduled','Scheduled async job for starting Vm. Vm Id: 21',20,20,'2011-03-09 10:51:45','INFO',187,NULL),(189,'VM.CREATE','Started','Starting job for starting Vm. Vm Id: 21',20,20,'2011-03-09 10:51:45','INFO',187,NULL),(190,'VM.CREATE','Completed','Successfully completed starting Vm. Vm Id: 21',20,20,'2011-03-09 10:52:52','INFO',187,NULL),(191,'SNAPSHOT.CREATE','Scheduled','Scheduled async job for creating snapshot for volume: 24',20,20,'2011-03-09 10:53:41','INFO',0,NULL),(192,'SNAPSHOT.CREATE','Started','Starting job for creating snapshot',20,20,'2011-03-09 10:53:41','INFO',191,NULL),(193,'SNAPSHOT.CREATE','Completed','Successfully completed creating snapshot',20,20,'2011-03-09 10:56:11','INFO',191,NULL),(194,'SNAPSHOT.CREATE','Scheduled','Scheduled async job for creating snapshot for volume: 24',20,20,'2011-03-09 11:01:01','INFO',0,NULL),(195,'SNAPSHOT.CREATE','Started','Starting job for creating snapshot',20,20,'2011-03-09 11:01:01','INFO',194,NULL),(196,'SNAPSHOT.CREATE','Completed','Successfully completed creating snapshot',20,20,'2011-03-09 11:01:01','INFO',194,NULL),(197,'SNAPSHOT.CREATE','Scheduled','Scheduled async job for creating snapshot for volume: 24',20,20,'2011-03-09 11:02:06','INFO',0,NULL),(198,'SNAPSHOT.CREATE','Started','Starting job for creating snapshot',20,20,'2011-03-09 11:02:06','INFO',197,NULL),(199,'SNAPSHOT.CREATE','Completed','Successfully completed creating snapshot',20,20,'2011-03-09 11:02:06','INFO',197,NULL),(200,'SNAPSHOT.CREATE','Scheduled','Scheduled async job for creating snapshot for volume: 24',20,20,'2011-03-09 11:03:16','INFO',0,NULL),(201,'SNAPSHOT.CREATE','Started','Starting job for creating snapshot',20,20,'2011-03-09 11:03:16','INFO',200,NULL),(202,'SNAPSHOT.CREATE','Completed','Successfully completed creating snapshot',20,20,'2011-03-09 11:03:16','INFO',200,NULL),(203,'SNAPSHOT.DELETE','Scheduled','Scheduled async job for deleting snapshot: 1',2,2,'2011-03-09 11:04:59','INFO',0,NULL),(204,'SNAPSHOT.DELETE','Started','Starting job for deleting snapshot. Snapshot Id: 1',2,2,'2011-03-09 11:04:59','INFO',203,NULL),(205,'SNAPSHOT.DELETE','Completed','Successfully completed deleting snapshot. Snapshot Id: 1',2,2,'2011-03-09 11:05:01','INFO',203,NULL),(206,'SNAPSHOT.CREATE','Scheduled','Scheduled async job for creating snapshot for volume: 13',23,24,'2011-03-09 11:05:41','INFO',0,NULL),(207,'SNAPSHOT.CREATE','Started','Starting job for creating snapshot',23,24,'2011-03-09 11:05:41','INFO',206,NULL),(208,'SNAPSHOT.CREATE','Completed','Successfully completed creating snapshot',23,24,'2011-03-09 11:05:41','INFO',206,NULL),(209,'SNAPSHOT.DELETE','Scheduled','Scheduled async job for deleting snapshot: 2',2,2,'2011-03-09 11:10:52','INFO',0,NULL),(210,'SNAPSHOT.DELETE','Started','Starting job for deleting snapshot. Snapshot Id: 2',2,2,'2011-03-09 11:10:52','INFO',209,NULL),(211,'SNAPSHOT.DELETE','Completed','Successfully completed deleting snapshot. Snapshot Id: 2',2,2,'2011-03-09 11:10:55','INFO',209,NULL),(212,'SNAPSHOT.CREATE','Scheduled','Scheduled async job for creating snapshot for volume: 24',20,20,'2011-03-09 11:13:26','INFO',0,NULL),(213,'SNAPSHOT.CREATE','Started','Starting job for creating snapshot',20,20,'2011-03-09 11:13:26','INFO',212,NULL),(214,'SNAPSHOT.CREATE','Completed','Successfully completed creating snapshot',20,20,'2011-03-09 11:13:26','INFO',212,NULL),(215,'SNAPSHOT.CREATE','Scheduled','Scheduled async job for creating snapshot for volume: 24',20,20,'2011-03-09 11:15:51','INFO',0,NULL),(216,'SNAPSHOT.CREATE','Started','Starting job for creating snapshot',20,20,'2011-03-09 11:15:51','INFO',215,NULL),(217,'SNAPSHOT.CREATE','Completed','Successfully completed creating snapshot',20,20,'2011-03-09 11:15:51','INFO',215,NULL),(218,'VM.CREATE','Created','Successfully created entity for deploying Vm. Vm Id: 23',20,20,'2011-03-09 11:17:52','INFO',0,NULL),(219,'VM.CREATE','Scheduled','Scheduled async job for starting Vm. Vm Id: 23',20,20,'2011-03-09 11:17:52','INFO',218,NULL),(220,'VM.CREATE','Started','Starting job for starting Vm. Vm Id: 23',20,20,'2011-03-09 11:17:52','INFO',218,NULL),(221,'VM.CREATE','Completed','Successfully completed starting Vm. Vm Id: 23',20,20,'2011-03-09 11:19:29','INFO',218,NULL),(222,'VM.CREATE','Created','Successfully created entity for deploying Vm. Vm Id: 24',20,20,'2011-03-09 11:20:02','INFO',0,NULL),(223,'VM.CREATE','Scheduled','Scheduled async job for starting Vm. Vm Id: 24',20,20,'2011-03-09 11:20:02','INFO',222,NULL),(224,'VM.CREATE','Started','Starting job for starting Vm. Vm Id: 24',20,20,'2011-03-09 11:20:02','INFO',222,NULL),(225,'VM.CREATE','Completed','Successfully completed starting Vm. Vm Id: 24',20,20,'2011-03-09 11:20:03','INFO',222,NULL),(226,'SNAPSHOT.CREATE','Scheduled','Scheduled async job for creating snapshot for volume: 26',20,20,'2011-03-09 11:21:05','INFO',0,NULL),(227,'SNAPSHOT.CREATE','Started','Starting job for creating snapshot',20,20,'2011-03-09 11:21:05','INFO',226,NULL),(228,'SNAPSHOT.CREATE','Completed','Successfully completed creating snapshot',20,20,'2011-03-09 11:21:05','INFO',226,NULL),(229,'SNAPSHOT.CREATE','Scheduled','Scheduled async job for creating snapshot for volume: 27',20,20,'2011-03-09 11:21:35','INFO',0,NULL),(230,'SNAPSHOT.CREATE','Started','Starting job for creating snapshot',20,20,'2011-03-09 11:21:36','INFO',229,NULL),(231,'SNAPSHOT.CREATE','Completed','Successfully completed creating snapshot',20,20,'2011-03-09 11:21:36','INFO',229,NULL),(232,'SNAPSHOT.DELETE','Scheduled','Scheduled async job for deleting snapshot: 4',2,4,'2011-03-09 11:22:22','INFO',0,NULL),(233,'SNAPSHOT.DELETE','Started','Starting job for deleting snapshot. Snapshot Id: 4',2,4,'2011-03-09 11:22:22','INFO',232,NULL),(234,'SNAPSHOT.DELETE','Completed','Successfully completed deleting snapshot. Snapshot Id: 4',2,4,'2011-03-09 11:22:22','INFO',232,NULL),(235,'SNAPSHOT.CREATE','Scheduled','Scheduled async job for creating snapshot for volume: 24',2,20,'2011-03-09 11:23:06','INFO',0,NULL),(236,'SNAPSHOT.CREATE','Started','Starting job for creating snapshot',2,20,'2011-03-09 11:23:06','INFO',235,NULL),(237,'SNAPSHOT.CREATE','Completed','Successfully completed creating snapshot',2,20,'2011-03-09 11:23:06','INFO',235,NULL),(238,'SNAPSHOT.CREATE','Scheduled','Scheduled async job for creating snapshot for volume: 13',2,24,'2011-03-09 11:23:54','INFO',0,NULL),(239,'SNAPSHOT.CREATE','Started','Starting job for creating snapshot',2,24,'2011-03-09 11:23:54','INFO',238,NULL),(240,'SNAPSHOT.CREATE','Completed','Successfully completed creating snapshot',2,24,'2011-03-09 11:23:54','INFO',238,NULL),(241,'SNAPSHOT.CREATE','Scheduled','Scheduled async job for creating snapshot for volume: 13',23,24,'2011-03-09 11:25:58','INFO',0,NULL),(242,'SNAPSHOT.CREATE','Started','Starting job for creating snapshot',23,24,'2011-03-09 11:25:58','INFO',241,NULL),(243,'SNAPSHOT.CREATE','Completed','Successfully completed creating snapshot',23,24,'2011-03-09 11:25:58','INFO',241,NULL),(244,'SNAPSHOT.CREATE','Scheduled','Scheduled async job for creating snapshot for volume: 24',2,20,'2011-03-09 11:27:55','INFO',0,NULL),(245,'SNAPSHOT.CREATE','Started','Starting job for creating snapshot',2,20,'2011-03-09 11:27:55','INFO',244,NULL),(246,'SNAPSHOT.CREATE','Completed','Successfully completed creating snapshot',2,20,'2011-03-09 11:27:55','INFO',244,NULL),(247,'SNAPSHOT.CREATE','Scheduled','Scheduled async job for creating snapshot for volume: 24',2,20,'2011-03-09 11:28:48','INFO',0,NULL),(248,'SNAPSHOT.CREATE','Started','Starting job for creating snapshot',2,20,'2011-03-09 11:28:48','INFO',247,NULL),(249,'SNAPSHOT.CREATE','Completed','Successfully completed creating snapshot',2,20,'2011-03-09 11:29:00','INFO',247,NULL),(250,'SNAPSHOT.CREATE','Scheduled','Scheduled async job for creating snapshot for volume: 13',23,24,'2011-03-09 11:29:58','INFO',0,NULL),(251,'SNAPSHOT.CREATE','Started','Starting job for creating snapshot',23,24,'2011-03-09 11:29:58','INFO',250,NULL),(252,'SNAPSHOT.CREATE','Completed','Successfully completed creating snapshot',23,24,'2011-03-09 11:32:56','INFO',250,NULL),(253,'SNAPSHOT.CREATE','Scheduled','Scheduled async job for creating snapshot for volume: 26',20,20,'2011-03-09 11:40:45','INFO',0,NULL),(254,'SNAPSHOT.CREATE','Started','Starting job for creating snapshot',20,20,'2011-03-09 11:40:45','INFO',253,NULL),(255,'SNAPSHOT.CREATE','Completed','Successfully completed creating snapshot',20,20,'2011-03-09 11:42:18','INFO',253,NULL),(256,'USER.LOGOUT','Completed','user has logged out',2,2,'2011-03-09 21:18:14','INFO',0,NULL),(257,'NETWORK.CREATE','Completed','Successfully completed creating network. Network Id: 212',3,0,'2011-03-10 00:27:14','INFO',0,NULL),(258,'VM.CREATE','Created','Successfully created entity for deploying Vm. Vm Id: 25',3,3,'2011-03-10 00:28:27','INFO',0,NULL),(259,'VM.CREATE','Scheduled','Scheduled async job for starting Vm. Vm Id: 25',3,3,'2011-03-10 00:28:27','INFO',258,NULL),(260,'VM.CREATE','Started','Starting job for starting Vm. Vm Id: 25',3,3,'2011-03-10 00:28:27','INFO',258,NULL),(261,'VM.CREATE','Completed','Successfully completed starting Vm. Vm Id: 25',3,3,'2011-03-10 00:28:28','INFO',258,NULL),(262,'VM.CREATE','Created','Successfully created entity for deploying Vm. Vm Id: 26',2,2,'2011-03-24 01:41:04','INFO',0,NULL),(263,'VM.CREATE','Scheduled','Scheduled async job for starting Vm. Vm Id: 26',2,2,'2011-03-24 01:41:04','INFO',262,NULL),(264,'VM.CREATE','Started','Starting job for starting Vm. Vm Id: 26',2,2,'2011-03-24 01:41:04','INFO',262,NULL),(265,'VM.CREATE','Completed','Successfully completed starting Vm. Vm Id: 26',2,2,'2011-03-24 01:41:05','INFO',262,NULL),(266,'VM.CREATE','Created','Successfully created entity for deploying Vm. Vm Id: 27',2,2,'2011-03-24 01:42:27','INFO',0,NULL),(267,'VM.CREATE','Scheduled','Scheduled async job for starting Vm. Vm Id: 27',2,2,'2011-03-24 01:42:27','INFO',266,NULL),(268,'VM.CREATE','Started','Starting job for starting Vm. Vm Id: 27',2,2,'2011-03-24 01:42:27','INFO',266,NULL),(269,'VM.CREATE','Completed','Successfully completed starting Vm. Vm Id: 27',2,2,'2011-03-24 01:42:27','INFO',266,NULL),(270,'VOLUME.CREATE','Created','Successfully created entity for creating volume. Volume Id: 32',2,2,'2011-03-28 12:03:38','INFO',0,NULL),(271,'VOLUME.CREATE','Scheduled','Scheduled async job for creating volume: hgjg',2,2,'2011-03-28 12:03:38','INFO',270,NULL),(272,'VOLUME.CREATE','Started','Starting job for creating volume. Volume Id: 32',2,2,'2011-03-28 12:03:38','INFO',270,NULL),(273,'VOLUME.CREATE','Completed','Successfully completed creating volume. Volume Id: 32',2,2,'2011-03-28 12:03:38','INFO',270,NULL),(274,'VOLUME.ATTACH','Scheduled','Scheduled async job for attaching volume: 32 to vm: 16',2,2,'2011-03-28 12:04:12','INFO',0,NULL),(275,'VOLUME.ATTACH','Started','Starting job for attaching volume. Volume Id: 32 VmId: 16',2,2,'2011-03-28 12:04:12','INFO',274,NULL),(276,'VOLUME.ATTACH','Completed','Successfully completed attaching volume. Volume Id: 32 VmId: 16',2,2,'2011-03-28 12:04:14','INFO',274,NULL),(277,'NET.IPASSIGN','Created','Successfully created entity for allocating Ip. Ip Id: 10',2,2,'2011-03-28 12:07:03','INFO',0,NULL),(278,'NET.IPASSIGN','Scheduled','Scheduled async job for associating ip to network id: 205 in zone 1',2,2,'2011-03-28 12:07:03','INFO',277,NULL),(279,'NET.IPASSIGN','Started','Starting job for associating Ip. Ip Id: 10',2,2,'2011-03-28 12:07:03','INFO',277,NULL),(280,'NET.IPASSIGN','Completed','Successfully completed associating Ip. Ip Id: 10',2,2,'2011-03-28 12:07:08','INFO',277,NULL),(281,'NET.RULEADD','Created','Successfully created entity for creating forwarding rule. Rule Id: 3',2,2,'2011-03-28 12:07:30','INFO',0,NULL),(282,'NET.RULEADD','Scheduled','Scheduled async job for Applying port forwarding  rule for Ip: 66.149.231.110 with virtual machine:16',2,2,'2011-03-28 12:07:30','INFO',281,NULL),(283,'NET.RULEADD','Started','Starting job for applying port forwarding rule. Rule Id: 3',2,2,'2011-03-28 12:07:30','INFO',281,NULL),(284,'NET.RULEADD','Completed','Successfully completed applying port forwarding rule. Rule Id: 3',2,2,'2011-03-28 12:07:31','INFO',281,NULL),(285,'LB.CREATE','Completed','Successfully completed creating load balancer. Load balancer Id: 4',2,2,'2011-03-28 12:08:00','INFO',0,NULL),(286,'NETWORK.CREATE','Completed','Successfully completed creating network. Network Id: 213',42,42,'2011-03-29 07:55:23','INFO',0,NULL),(287,'VM.CREATE','Created','Successfully created entity for deploying Vm. Vm Id: 28',42,42,'2011-03-29 07:55:28','INFO',0,NULL),(288,'VM.CREATE','Scheduled','Scheduled async job for starting Vm. Vm Id: 28',42,42,'2011-03-29 07:55:28','INFO',287,NULL),(289,'VM.CREATE','Started','Starting job for starting Vm. Vm Id: 28',42,42,'2011-03-29 07:55:28','INFO',287,NULL),(290,'VM.CREATE','Completed','Successfully completed starting Vm. Vm Id: 28',42,42,'2011-03-29 07:55:28','INFO',287,NULL),(291,'VM.DESTROY','Scheduled','Scheduled async job for destroying vm: 3',2,4,'2011-03-29 08:01:12','INFO',0,NULL),(292,'VM.DESTROY','Started','Starting job for destroying Vm. Vm Id: 3',2,4,'2011-03-29 08:01:12','INFO',291,NULL),(293,'VM.DESTROY','Completed','Successfully completed destroying Vm. Vm Id: 3',2,4,'2011-03-29 08:01:12','INFO',291,NULL),(294,'VM.DESTROY','Scheduled','Scheduled async job for destroying vm: 7',2,4,'2011-03-29 08:01:24','INFO',0,NULL),(295,'VM.DESTROY','Started','Starting job for destroying Vm. Vm Id: 7',2,4,'2011-03-29 08:01:24','INFO',294,NULL),(296,'VM.DESTROY','Completed','Successfully completed destroying Vm. Vm Id: 7',2,4,'2011-03-29 08:01:24','INFO',294,NULL),(297,'VM.DESTROY','Scheduled','Scheduled async job for destroying vm: 8',2,4,'2011-03-29 08:01:36','INFO',0,NULL),(298,'VM.DESTROY','Started','Starting job for destroying Vm. Vm Id: 8',2,4,'2011-03-29 08:01:37','INFO',297,NULL),(299,'VM.DESTROY','Completed','Successfully completed destroying Vm. Vm Id: 8',2,4,'2011-03-29 08:01:37','INFO',297,NULL),(300,'VM.DESTROY','Scheduled','Scheduled async job for destroying vm: 28',2,42,'2011-03-29 08:02:21','INFO',0,NULL),(301,'VM.DESTROY','Started','Starting job for destroying Vm. Vm Id: 28',2,42,'2011-03-29 08:02:21','INFO',300,NULL),(302,'VM.DESTROY','Completed','Successfully completed destroying Vm. Vm Id: 28',2,42,'2011-03-29 08:02:22','INFO',300,NULL),(303,'VM.CREATE','Created','Successfully created entity for deploying Vm. Vm Id: 29',42,42,'2011-03-29 08:19:04','INFO',0,NULL),(304,'VM.CREATE','Scheduled','Scheduled async job for starting Vm. Vm Id: 29',42,42,'2011-03-29 08:19:04','INFO',303,NULL),(305,'VM.CREATE','Started','Starting job for starting Vm. Vm Id: 29',42,42,'2011-03-29 08:19:04','INFO',303,NULL),(306,'VM.CREATE','Completed','Successfully completed starting Vm. Vm Id: 29',42,42,'2011-03-29 08:19:04','INFO',303,NULL),(307,'NETWORK.CREATE','Completed','Successfully completed creating network. Network Id: 214',51,51,'2011-03-29 12:19:10','INFO',0,NULL),(308,'VM.CREATE','Created','Successfully created entity for deploying Vm. Vm Id: 30',51,51,'2011-03-29 12:19:17','INFO',0,NULL),(309,'VM.CREATE','Scheduled','Scheduled async job for starting Vm. Vm Id: 30',51,51,'2011-03-29 12:19:17','INFO',308,NULL),(310,'VM.CREATE','Started','Starting job for starting Vm. Vm Id: 30',51,51,'2011-03-29 12:19:17','INFO',308,NULL),(311,'VM.CREATE','Completed','Successfully completed starting Vm. Vm Id: 30',51,51,'2011-03-29 12:19:18','INFO',308,NULL),(312,'VM.CREATE','Created','Successfully created entity for deploying Vm. Vm Id: 31',51,51,'2011-03-30 06:12:03','INFO',0,NULL),(313,'VM.CREATE','Scheduled','Scheduled async job for starting Vm. Vm Id: 31',51,51,'2011-03-30 06:12:03','INFO',312,NULL),(314,'VM.CREATE','Started','Starting job for starting Vm. Vm Id: 31',51,51,'2011-03-30 06:12:03','INFO',312,NULL),(315,'VM.CREATE','Completed','Successfully completed starting Vm. Vm Id: 31',51,51,'2011-03-30 06:12:03','INFO',312,NULL),(316,'NET.IPASSIGN','Created','Successfully created entity for allocating Ip. Ip Id: 2',42,42,'2011-03-30 08:13:49','INFO',0,NULL),(317,'NET.IPASSIGN','Scheduled','Scheduled async job for associating ip to network id: 213 in zone 1',42,42,'2011-03-30 08:13:49','INFO',316,NULL),(318,'NET.IPASSIGN','Started','Starting job for associating Ip. Ip Id: 2',42,42,'2011-03-30 08:13:50','INFO',316,NULL),(319,'NET.IPASSIGN','Completed','Successfully completed associating Ip. Ip Id: 2',42,42,'2011-03-30 08:13:50','INFO',316,NULL),(320,'SNAPSHOT.CREATE','Scheduled','Scheduled async job for creating snapshot for volume Id:14',1,1,'2011-04-01 12:04:38','INFO',0,NULL),(321,'SNAPSHOT.CREATE','Started','Starting job for creating snapshot',1,24,'2011-04-01 12:04:38','INFO',1,NULL),(322,'SNAPSHOT.CREATE','Completed','Successfully completed creating snapshot',1,24,'2011-04-01 12:04:38','INFO',1,NULL),(323,'VM.CREATE','Created','Successfully created entity for deploying Vm. Vm Id: 32',2,2,'2011-04-04 16:28:22','INFO',0,NULL),(324,'VM.CREATE','Scheduled','Scheduled async job for starting Vm. Vm Id: 32',2,2,'2011-04-04 16:28:22','INFO',323,NULL),(325,'VM.CREATE','Started','Starting job for starting Vm. Vm Id: 32',2,2,'2011-04-04 16:28:22','INFO',323,NULL),(326,'VM.CREATE','Completed','Successfully completed starting Vm. Vm Id: 32',2,2,'2011-04-04 16:28:22','INFO',323,NULL),(327,'VM.DESTROY','Scheduled','Scheduled async job for destroying vm: 32',2,2,'2011-04-04 16:29:20','INFO',0,NULL),(328,'VM.DESTROY','Started','Starting job for destroying Vm. Vm Id: 32',2,2,'2011-04-04 16:29:20','INFO',327,NULL),(329,'VM.DESTROY','Completed','Successfully completed destroying Vm. Vm Id: 32',2,2,'2011-04-04 16:29:21','INFO',327,NULL),(330,'VM.CREATE','Created','Error while creating entity for deploying Vm',20,20,'2011-04-04 16:47:23','ERROR',0,NULL),(331,'SNAPSHOT.CREATE','Scheduled','Scheduled async job for creating snapshot for volume: 24',20,20,'2011-04-04 17:04:28','INFO',0,NULL),(332,'SNAPSHOT.CREATE','Started','Starting job for creating snapshot',20,20,'2011-04-04 17:04:28','INFO',331,NULL),(333,'SNAPSHOT.CREATE','Completed','Successfully completed creating snapshot',20,20,'2011-04-04 17:04:28','INFO',331,NULL),(334,'SNAPSHOT.CREATE','Scheduled','Scheduled async job for creating snapshot for volume: 24',20,20,'2011-04-04 17:10:25','INFO',0,NULL),(335,'SNAPSHOT.CREATE','Started','Starting job for creating snapshot',20,20,'2011-04-04 17:10:26','INFO',334,NULL),(336,'SNAPSHOT.CREATE','Completed','Successfully completed creating snapshot',20,20,'2011-04-04 17:10:26','INFO',334,NULL),(337,'SNAPSHOT.CREATE','Scheduled','Scheduled async job for creating snapshot for volume: 24',20,20,'2011-04-04 18:00:30','INFO',0,NULL),(338,'SNAPSHOT.CREATE','Started','Starting job for creating snapshot',20,20,'2011-04-04 18:00:30','INFO',337,NULL),(339,'SNAPSHOT.CREATE','Completed','Successfully completed creating snapshot',20,20,'2011-04-04 18:01:09','INFO',337,NULL),(340,'SNAPSHOT.CREATE','Scheduled','Scheduled async job for creating snapshot for volume: 24',20,20,'2011-04-04 19:04:48','INFO',0,NULL),(341,'SNAPSHOT.CREATE','Started','Starting job for creating snapshot',20,20,'2011-04-04 19:04:48','INFO',340,NULL),(342,'SNAPSHOT.CREATE','Completed','Successfully completed creating snapshot',20,20,'2011-04-04 19:04:48','INFO',340,NULL),(343,'USER.LOGOUT','Completed','user has logged out',2,2,'2011-04-04 19:36:55','INFO',0,NULL),(344,'VM.CREATE','Created','Error while creating entity for deploying Vm',20,20,'2011-04-04 20:06:47','ERROR',0,NULL),(345,'VM.CREATE','Created','Successfully created entity for deploying Vm. Vm Id: 33',20,20,'2011-04-04 20:09:43','INFO',0,NULL),(346,'VM.CREATE','Scheduled','Scheduled async job for starting Vm. Vm Id: 33',20,20,'2011-04-04 20:09:43','INFO',345,NULL),(347,'VM.CREATE','Started','Starting job for starting Vm. Vm Id: 33',20,20,'2011-04-04 20:09:43','INFO',345,NULL),(348,'VM.CREATE','Completed','Successfully completed starting Vm. Vm Id: 33',20,20,'2011-04-04 20:09:44','INFO',345,NULL),(349,'VM.CREATE','Created','Successfully created entity for deploying Vm. Vm Id: 34',20,20,'2011-04-04 20:11:27','INFO',0,NULL),(350,'VM.CREATE','Scheduled','Scheduled async job for starting Vm. Vm Id: 34',20,20,'2011-04-04 20:11:27','INFO',349,NULL),(351,'VM.CREATE','Started','Starting job for starting Vm. Vm Id: 34',20,20,'2011-04-04 20:11:27','INFO',349,NULL),(352,'VM.CREATE','Completed','Successfully completed starting Vm. Vm Id: 34',20,20,'2011-04-04 20:11:27','INFO',349,NULL),(353,'VM.DESTROY','Scheduled','Scheduled async job for destroying vm: 33',20,20,'2011-04-04 20:12:56','INFO',0,NULL),(354,'VM.DESTROY','Started','Starting job for destroying Vm. Vm Id: 33',20,20,'2011-04-04 20:12:56','INFO',353,NULL),(355,'VM.DESTROY','Completed','Successfully completed destroying Vm. Vm Id: 33',20,20,'2011-04-04 20:12:56','INFO',353,NULL),(356,'VM.DESTROY','Scheduled','Scheduled async job for destroying vm: 34',20,20,'2011-04-04 20:13:00','INFO',0,NULL),(357,'VM.DESTROY','Started','Starting job for destroying Vm. Vm Id: 34',20,20,'2011-04-04 20:13:00','INFO',356,NULL),(358,'VM.DESTROY','Completed','Successfully completed destroying Vm. Vm Id: 34',20,20,'2011-04-04 20:13:00','INFO',356,NULL),(359,'VM.STOP','Scheduled','Scheduled async job for stopping user vm: 23',2,20,'2011-04-09 10:07:59','INFO',0,NULL),(360,'VM.STOP','Started','Starting job for stopping Vm. Vm Id: 23',2,20,'2011-04-09 10:07:59','INFO',359,NULL),(361,'VM.STOP','Completed','Successfully completed stopping Vm. Vm Id: 23',2,20,'2011-04-09 10:08:32','INFO',359,NULL),(362,'VM.DESTROY','Scheduled','Scheduled async job for destroying vm: 23',2,20,'2011-04-09 10:09:08','INFO',0,NULL),(363,'VM.DESTROY','Started','Starting job for destroying Vm. Vm Id: 23',2,20,'2011-04-09 10:09:08','INFO',362,NULL),(364,'VM.DESTROY','Completed','Successfully completed destroying Vm. Vm Id: 23',2,20,'2011-04-09 10:09:08','INFO',362,NULL),(365,'VM.STOP','Scheduled','Scheduled async job for stopping user vm: 14',2,22,'2011-04-09 10:10:23','INFO',0,NULL),(366,'VM.STOP','Started','Starting job for stopping Vm. Vm Id: 14',2,22,'2011-04-09 10:10:23','INFO',365,NULL),(367,'VM.STOP','Completed','Successfully completed stopping Vm. Vm Id: 14',2,22,'2011-04-09 10:10:57','INFO',365,NULL),(368,'VM.DESTROY','Scheduled','Scheduled async job for destroying vm: 14',2,22,'2011-04-09 10:11:12','INFO',0,NULL),(369,'VM.DESTROY','Started','Starting job for destroying Vm. Vm Id: 14',2,22,'2011-04-09 10:11:13','INFO',368,NULL),(370,'VM.DESTROY','Completed','Successfully completed destroying Vm. Vm Id: 14',2,22,'2011-04-09 10:11:13','INFO',368,NULL),(371,'VM.STOP','Scheduled','Scheduled async job for stopping user vm: 12',2,24,'2011-04-09 10:11:46','INFO',0,NULL),(372,'VM.STOP','Started','Starting job for stopping Vm. Vm Id: 12',2,24,'2011-04-09 10:11:46','INFO',371,NULL),(373,'VM.STOP','Completed','Successfully completed stopping Vm. Vm Id: 12',2,24,'2011-04-09 10:12:33','INFO',371,NULL),(374,'VM.DESTROY','Scheduled','Scheduled async job for destroying vm: 12',2,24,'2011-04-09 10:12:46','INFO',0,NULL),(375,'VM.DESTROY','Started','Starting job for destroying Vm. Vm Id: 12',2,24,'2011-04-09 10:12:46','INFO',374,NULL),(376,'VM.DESTROY','Completed','Successfully completed destroying Vm. Vm Id: 12',2,24,'2011-04-09 10:12:46','INFO',374,NULL),(377,'VM.STOP','Scheduled','Scheduled async job for stopping user vm: 21',2,20,'2011-04-09 10:14:05','INFO',0,NULL),(378,'VM.STOP','Started','Starting job for stopping Vm. Vm Id: 21',2,20,'2011-04-09 10:14:07','INFO',377,NULL),(379,'VM.STOP','Completed','Successfully completed stopping Vm. Vm Id: 21',2,20,'2011-04-09 10:14:44','INFO',377,NULL),(380,'VM.DESTROY','Scheduled','Scheduled async job for destroying vm: 21',2,20,'2011-04-09 10:14:58','INFO',0,NULL),(381,'VM.DESTROY','Started','Starting job for destroying Vm. Vm Id: 21',2,20,'2011-04-09 10:14:58','INFO',380,NULL),(382,'VM.DESTROY','Completed','Successfully completed destroying Vm. Vm Id: 21',2,20,'2011-04-09 10:14:58','INFO',380,NULL),(383,'NETWORK.CREATE','Completed','Successfully completed creating network. Network Id: 215',56,56,'2011-04-09 10:25:42','INFO',0,NULL),(384,'VM.CREATE','Created','Successfully created entity for deploying Vm. Vm Id: 35',56,56,'2011-04-09 10:26:38','INFO',0,NULL),(385,'VM.CREATE','Scheduled','Scheduled async job for starting Vm. Vm Id: 35',56,56,'2011-04-09 10:26:38','INFO',384,NULL),(386,'VM.CREATE','Started','Starting job for starting Vm. Vm Id: 35',56,56,'2011-04-09 10:26:38','INFO',384,NULL),(387,'VM.CREATE','Completed','Successfully completed starting Vm. Vm Id: 35',56,56,'2011-04-09 10:27:56','INFO',384,NULL),(388,'NETWORK.CREATE','Completed','Successfully completed creating network. Network Id: 216',61,61,'2011-04-25 12:53:14','INFO',0,NULL),(389,'VM.CREATE','Created','Successfully created entity for deploying Vm. Vm Id: 37',61,61,'2011-04-25 12:53:30','INFO',0,NULL),(390,'VM.CREATE','Scheduled','Scheduled async job for starting Vm. Vm Id: 37',61,61,'2011-04-25 12:53:30','INFO',389,NULL),(391,'VM.CREATE','Started','Starting job for starting Vm. Vm Id: 37',61,61,'2011-04-25 12:53:31','INFO',389,NULL),(392,'VM.CREATE','Completed','Successfully completed starting Vm. Vm Id: 37',61,61,'2011-04-25 12:53:31','INFO',389,NULL),(393,'VM.CREATE','Created','Successfully created entity for deploying Vm. Vm Id: 38',61,61,'2011-04-25 12:54:45','INFO',0,NULL),(394,'VM.CREATE','Scheduled','Scheduled async job for starting Vm. Vm Id: 38',61,61,'2011-04-25 12:54:45','INFO',393,NULL),(395,'VM.CREATE','Started','Starting job for starting Vm. Vm Id: 38',61,61,'2011-04-25 12:54:45','INFO',393,NULL),(396,'VM.CREATE','Completed','Successfully completed starting Vm. Vm Id: 38',61,61,'2011-04-25 12:54:46','INFO',393,NULL),(397,'NETWORK.CREATE','Completed','Successfully completed creating network. Network Id: 217',60,60,'2011-04-25 12:56:50','INFO',0,NULL),(398,'VM.CREATE','Created','Successfully created entity for deploying Vm. Vm Id: 39',60,60,'2011-04-25 12:57:07','INFO',0,NULL),(399,'VM.CREATE','Scheduled','Scheduled async job for starting Vm. Vm Id: 39',60,60,'2011-04-25 12:57:07','INFO',398,NULL),(400,'VM.CREATE','Started','Starting job for starting Vm. Vm Id: 39',60,60,'2011-04-25 12:57:07','INFO',398,NULL),(401,'VM.CREATE','Completed','Successfully completed starting Vm. Vm Id: 39',60,60,'2011-04-25 12:57:07','INFO',398,NULL),(402,'SNAPSHOT.CREATE','Scheduled','Scheduled async job for creating snapshot for volume Id:14',1,1,'2011-05-01 12:00:42','INFO',0,NULL),(403,'SNAPSHOT.CREATE','Started','Starting job for creating snapshot',1,24,'2011-05-01 12:00:42','INFO',1,NULL),(404,'SNAPSHOT.CREATE','Completed','Successfully completed creating snapshot',1,24,'2011-05-01 12:00:42','INFO',1,NULL),(405,'MAINT.PREPARE.PS','Scheduled','Scheduled async job for preparing storage pool: 200 for maintenance',2,2,'2011-05-06 22:14:00','INFO',0,NULL),(406,'MAINT.PREPARE.PS','Scheduled','Scheduled async job for preparing storage pool: 201 for maintenance',2,2,'2011-05-06 22:15:07','INFO',0,NULL),(407,'MAINT.CANCEL.PS','Scheduled','Scheduled async job for canceling maintenance for primary storage pool: 201',2,2,'2011-05-06 22:15:31','INFO',0,NULL),(408,'MAINT.PREPARE.PS','Scheduled','Scheduled async job for preparing storage pool: 200 for maintenance',2,2,'2011-05-06 22:17:11','INFO',0,NULL),(409,'SSVM.START','Scheduled','Scheduled async job for destroying system vm: 1',2,2,'2011-05-06 22:18:53','INFO',0,NULL),(410,'SSVM.START','Scheduled','Scheduled async job for destroying system vm: 2',2,2,'2011-05-06 22:19:08','INFO',0,NULL),(411,'MAINT.PREPARE.PS','Scheduled','Scheduled async job for preparing storage pool: 200 for maintenance',2,2,'2011-05-06 22:19:39','INFO',0,NULL),(412,'ACCOUNT.DISABLE','Scheduled','Scheduled async job for disabling account: pminc2 in domain: 13',1,12,'2011-05-09 01:02:39','INFO',0,NULL),(413,'TEMPLATE.COPY','Scheduled','Scheduled async job for copying template: 204 from zone: 1 to zone: 2',2,2,'2011-05-11 21:05:10','INFO',0,NULL),(414,'TEMPLATE.COPY','Scheduled','Scheduled async job for copying template: 205 from zone: 1 to zone: 2',2,2,'2011-05-11 21:05:23','INFO',0,NULL),(415,'TEMPLATE.DELETE','Scheduled','Scheduled async job for Deleting template 204',2,2,'2011-05-11 21:38:47','INFO',0,NULL),(416,'TEMPLATE.DELETE','Scheduled','Scheduled async job for Deleting template 205',2,2,'2011-05-11 21:39:10','INFO',0,NULL),(417,'TEMPLATE.DELETE','Scheduled','Scheduled async job for Deleting template 206',2,2,'2011-05-11 21:39:31','INFO',0,NULL),(418,'TEMPLATE.DELETE','Scheduled','Scheduled async job for Deleting template 207',2,2,'2011-05-11 21:39:40','INFO',0,NULL),(419,'TEMPLATE.DELETE','Scheduled','Scheduled async job for Deleting template 202',2,2,'2011-05-11 21:39:46','INFO',0,NULL),(420,'TEMPLATE.DELETE','Scheduled','Scheduled async job for Deleting template 203',2,2,'2011-05-11 21:39:51','INFO',0,NULL),(421,'TEMPLATE.DELETE','Scheduled','Scheduled async job for Deleting template 221',2,2,'2011-05-11 21:39:56','INFO',0,NULL),(422,'TEMPLATE.DELETE','Scheduled','Scheduled async job for Deleting template 220',2,2,'2011-05-11 21:40:04','INFO',0,NULL),(423,'TEMPLATE.DELETE','Scheduled','Scheduled async job for Deleting template 217',2,2,'2011-05-11 21:40:13','INFO',0,NULL),(424,'TEMPLATE.DELETE','Scheduled','Scheduled async job for Deleting template 216',2,2,'2011-05-11 21:40:18','INFO',0,NULL),(425,'TEMPLATE.DELETE','Scheduled','Scheduled async job for Deleting template 219',2,2,'2011-05-11 21:40:24','INFO',0,NULL),(426,'TEMPLATE.DELETE','Scheduled','Scheduled async job for Deleting template 218',2,2,'2011-05-11 21:40:29','INFO',0,NULL),(427,'TEMPLATE.DELETE','Scheduled','Scheduled async job for Deleting template 212',2,2,'2011-05-11 21:40:34','INFO',0,NULL),(428,'TEMPLATE.DELETE','Scheduled','Scheduled async job for Deleting template 215',2,2,'2011-05-11 21:40:40','INFO',0,NULL),(429,'TEMPLATE.DELETE','Scheduled','Scheduled async job for Deleting template 213',2,2,'2011-05-11 21:40:45','INFO',0,NULL),(430,'TEMPLATE.DELETE','Scheduled','Scheduled async job for Deleting template 214',2,2,'2011-05-11 21:40:51','INFO',0,NULL),(431,'TEMPLATE.DELETE','Scheduled','Scheduled async job for Deleting template 208',2,2,'2011-05-11 21:40:55','INFO',0,NULL),(432,'TEMPLATE.DELETE','Scheduled','Scheduled async job for Deleting template 209',2,2,'2011-05-11 21:41:00','INFO',0,NULL),(433,'TEMPLATE.DELETE','Scheduled','Scheduled async job for Deleting template 211',2,2,'2011-05-11 21:41:06','INFO',0,NULL),(434,'TEMPLATE.DELETE','Scheduled','Scheduled async job for Deleting template 210',2,2,'2011-05-11 21:41:11','INFO',0,NULL),(435,'NETWORK.CREATE','Completed','Successfully completed creating network. Network Id: 222',2,2,'2011-05-11 21:43:25','INFO',0,NULL),(436,'VM.CREATE','Created','Successfully created entity for deploying Vm. Vm Id: 42',2,2,'2011-05-11 21:43:38','INFO',0,NULL),(437,'VM.CREATE','Scheduled','Scheduled async job for starting Vm. Vm Id: 42',2,2,'2011-05-11 21:43:38','INFO',436,NULL),(438,'VM.CREATE','Started','Starting job for starting Vm. Vm Id: 42',2,2,'2011-05-11 21:43:38','INFO',436,NULL),(439,'VM.CREATE','Completed','Successfully completed starting Vm. Vm Id: 42',2,2,'2011-05-11 21:46:03','INFO',436,NULL),(440,'VM.STOP','Scheduled','Scheduled async job for stopping user vm: 42',2,2,'2011-05-11 21:46:20','INFO',0,NULL),(441,'VM.STOP','Started','Starting job for stopping Vm. Vm Id: 42',2,2,'2011-05-11 21:46:20','INFO',440,NULL),(442,'VM.STOP','Completed','Successfully completed stopping Vm. Vm Id: 42',2,2,'2011-05-11 21:46:25','INFO',440,NULL),(443,'TEMPLATE.DELETE','Scheduled','Scheduled async job for Deleting template 2',2,1,'2011-05-11 21:46:51','INFO',0,NULL),(444,'TEMPLATE.DELETE','Scheduled','Scheduled async job for Deleting template 4',2,1,'2011-05-11 21:46:59','INFO',0,NULL),(445,'TEMPLATE.DELETE','Scheduled','Scheduled async job for Deleting template 227',2,2,'2011-05-11 21:47:55','INFO',0,NULL),(446,'TEMPLATE.DELETE','Scheduled','Scheduled async job for Deleting template 226',2,42,'2011-05-11 21:48:01','INFO',0,NULL),(447,'TEMPLATE.DELETE','Scheduled','Scheduled async job for Deleting template 7',2,1,'2011-05-11 21:48:30','INFO',0,NULL),(448,'SNAPSHOT.CREATE','Scheduled','Scheduled async job for creating snapshot for volume: 51',2,2,'2011-05-11 21:51:28','INFO',0,NULL),(449,'SNAPSHOT.CREATE','Started','Starting job for creating snapshot',2,2,'2011-05-11 21:51:28','INFO',448,NULL),(450,'SNAPSHOT.CREATE','Completed','Successfully completed creating snapshot',2,2,'2011-05-11 21:53:36','INFO',448,NULL),(451,'TEMPLATE.CREATE','Scheduled','Scheduled async job for creating template: CentOS55 - Xen',1,2,'2011-05-11 21:58:34','INFO',0,NULL),(452,'MAINT.PREPARE.PS','Scheduled','Scheduled async job for preparing storage pool: 200 for maintenance',2,2,'2011-05-11 21:59:31','INFO',0,NULL),(453,'MAINT.CANCEL.PS','Scheduled','Scheduled async job for canceling maintenance for primary storage pool: 200',2,2,'2011-05-11 22:00:01','INFO',0,NULL),(454,'TEMPLATE.CREATE','Scheduled','Scheduled async job for creating template: CentOS55LAMP - Xen',1,2,'2011-05-11 22:01:35','INFO',0,NULL),(455,'TEMPLATE.CREATE','Scheduled','Scheduled async job for creating template: CentOS55LAMP_BP - Xen',1,2,'2011-05-11 22:04:35','INFO',0,NULL),(456,'TEMPLATE.CREATE','Scheduled','Scheduled async job for creating template: CentOS55MySQL - Xen',1,2,'2011-05-11 22:07:35','INFO',0,NULL),(457,'TEMPLATE.CREATE','Scheduled','Scheduled async job for creating template: CentOS55MySQL_BP - Xen',1,2,'2011-05-11 22:10:36','INFO',0,NULL),(458,'TEMPLATE.CREATE','Scheduled','Scheduled async job for creating template: RHEL55 - Xen',1,2,'2011-05-11 22:13:36','INFO',0,NULL),(459,'TEMPLATE.CREATE','Scheduled','Scheduled async job for creating template: RHEL55LAMP_BP - Xen',1,2,'2011-05-11 22:16:36','INFO',0,NULL),(460,'TEMPLATE.CREATE','Scheduled','Scheduled async job for creating template: Ubuntu1010 - Xen',1,2,'2011-05-11 22:19:36','INFO',0,NULL),(461,'TEMPLATE.CREATE','Scheduled','Scheduled async job for creating template: Windows2003SP2_32bit - Xen',1,2,'2011-05-11 22:22:37','INFO',0,NULL),(462,'TEMPLATE.CREATE','Scheduled','Scheduled async job for creating template: Windows2008R2 - Xen',1,2,'2011-05-11 22:25:37','INFO',0,NULL),(463,'TEMPLATE.CREATE','Scheduled','Scheduled async job for creating template: Windows2008R2_32bit - Xen',1,2,'2011-05-11 22:28:37','INFO',0,NULL),(464,'TEMPLATE.CREATE','Scheduled','Scheduled async job for creating template: Windows2008R2_BP - Xen',1,2,'2011-05-11 22:31:37','INFO',0,NULL),(465,'TEMPLATE.CREATE','Scheduled','Scheduled async job for creating template: Windows2008R2_IIS - Xen',1,2,'2011-05-11 22:34:37','INFO',0,NULL),(466,'TEMPLATE.CREATE','Scheduled','Scheduled async job for creating template: Windows2008R2_IIS_BP - Xen',1,2,'2011-05-11 22:37:37','INFO',0,NULL),(467,'TEMPLATE.CREATE','Scheduled','Scheduled async job for creating template: Windows2008R2_SQL2008 - Xen',1,2,'2011-05-11 22:40:37','INFO',0,NULL),(468,'TEMPLATE.CREATE','Scheduled','Scheduled async job for creating template: Windows2008R2_SQL2008_BP - Xen',1,2,'2011-05-11 22:43:38','INFO',0,NULL),(469,'TEMPLATE.CREATE','Scheduled','Scheduled async job for creating template: Windows2008R2_WISA - Xen',1,2,'2011-05-11 22:46:38','INFO',0,NULL),(470,'TEMPLATE.CREATE','Scheduled','Scheduled async job for creating template: Windows2008R2_WISA_BP - Xen',1,2,'2011-05-11 22:49:38','INFO',0,NULL),(471,'TEMPLATE.CREATE','Scheduled','Scheduled async job for creating template: Windows7SP1 - Xen',1,2,'2011-05-11 22:52:38','INFO',0,NULL),(472,'TEMPLATE.CREATE','Scheduled','Scheduled async job for creating template: WindowsXPSP3 - Xen',1,2,'2011-05-11 22:55:38','INFO',0,NULL),(473,'NETWORK.CREATE','Completed','Successfully completed creating network. Network Id: 223',69,69,'2011-05-12 00:20:22','INFO',0,NULL),(474,'VM.CREATE','Created','Successfully created entity for deploying Vm. Vm Id: 44',69,69,'2011-05-12 00:20:32','INFO',0,NULL),(475,'VM.CREATE','Scheduled','Scheduled async job for starting Vm. Vm Id: 44',69,69,'2011-05-12 00:20:32','INFO',474,NULL),(476,'VM.CREATE','Started','Starting job for starting Vm. Vm Id: 44',69,69,'2011-05-12 00:20:32','INFO',474,NULL),(477,'VM.CREATE','Completed','Successfully completed starting Vm. Vm Id: 44',69,69,'2011-05-12 00:22:34','INFO',474,NULL),(478,'NETWORK.CREATE','Completed','Successfully completed creating network. Network Id: 224',70,70,'2011-05-12 18:48:41','INFO',0,NULL),(479,'VM.CREATE','Created','Successfully created entity for deploying Vm. Vm Id: 46',70,70,'2011-05-12 18:48:49','INFO',0,NULL),(480,'VM.CREATE','Scheduled','Scheduled async job for starting Vm. Vm Id: 46',70,70,'2011-05-12 18:48:49','INFO',479,NULL),(481,'VM.CREATE','Started','Starting job for starting Vm. Vm Id: 46',70,70,'2011-05-12 18:48:49','INFO',479,NULL),(482,'VM.CREATE','Completed','Successfully completed starting Vm. Vm Id: 46',70,70,'2011-05-12 18:49:39','INFO',479,NULL),(483,'NET.IPASSIGN','Created','Successfully created entity for allocating Ip. Ip Id: 27',70,70,'2011-05-12 18:52:02','INFO',0,NULL),(484,'NET.IPASSIGN','Scheduled','Scheduled async job for associating ip to network id: 224 in zone 2',70,70,'2011-05-12 18:52:02','INFO',483,NULL),(485,'NET.IPASSIGN','Started','Starting job for associating Ip. Ip Id: 27',70,70,'2011-05-12 18:52:02','INFO',483,NULL),(486,'NET.IPASSIGN','Completed','Successfully completed associating Ip. Ip Id: 27',70,70,'2011-05-12 18:52:06','INFO',483,NULL),(487,'NET.RULEADD','Created','Successfully created entity for creating forwarding rule. Rule Id: 5',70,70,'2011-05-12 18:52:41','INFO',0,NULL),(488,'NET.RULEADD','Scheduled','Scheduled async job for Applying port forwarding  rule for Ip: 172.16.64.46 with virtual machine:46',70,70,'2011-05-12 18:52:41','INFO',487,NULL),(489,'NET.RULEADD','Started','Starting job for applying port forwarding rule. Rule Id: 5',70,70,'2011-05-12 18:52:41','INFO',487,NULL),(490,'NET.RULEADD','Completed','Successfully completed applying port forwarding rule. Rule Id: 5',70,70,'2011-05-12 18:52:42','INFO',487,NULL),(491,'DOMAIN.DELETE','Scheduled','Scheduled async job for deleting domain: 59',1,1,'2011-05-16 17:52:14','INFO',0,NULL);
/*!40000 ALTER TABLE `event` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `firewall_rules`
--

DROP TABLE IF EXISTS `firewall_rules`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `firewall_rules` (
  `id` bigint(20) unsigned NOT NULL auto_increment COMMENT 'id',
  `ip_address_id` bigint(20) unsigned NOT NULL COMMENT 'id of the corresponding ip address',
  `start_port` int(10) NOT NULL COMMENT 'starting port of a port range',
  `end_port` int(10) NOT NULL COMMENT 'end port of a port range',
  `state` char(32) NOT NULL COMMENT 'current state of this rule',
  `protocol` char(16) NOT NULL default 'TCP' COMMENT 'protocol to open these ports for',
  `purpose` char(32) NOT NULL COMMENT 'why are these ports opened?',
  `account_id` bigint(20) unsigned NOT NULL COMMENT 'owner id',
  `domain_id` bigint(20) unsigned NOT NULL COMMENT 'domain id',
  `network_id` bigint(20) unsigned NOT NULL COMMENT 'network id',
  `xid` char(40) NOT NULL COMMENT 'external id',
  `created` datetime default NULL COMMENT 'Date created',
  PRIMARY KEY  (`id`),
  KEY `fk_firewall_rules__ip_address_id` (`ip_address_id`),
  KEY `fk_firewall_rules__network_id` (`network_id`),
  KEY `fk_firewall_rules__account_id` (`account_id`),
  KEY `fk_firewall_rules__domain_id` (`domain_id`),
  CONSTRAINT `fk_firewall_rules__account_id` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_firewall_rules__domain_id` FOREIGN KEY (`domain_id`) REFERENCES `domain` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_firewall_rules__ip_address_id` FOREIGN KEY (`ip_address_id`) REFERENCES `user_ip_address` (`id`),
  CONSTRAINT `fk_firewall_rules__network_id` FOREIGN KEY (`network_id`) REFERENCES `networks` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `firewall_rules`
--

LOCK TABLES `firewall_rules` WRITE;
/*!40000 ALTER TABLE `firewall_rules` DISABLE KEYS */;
INSERT INTO `firewall_rules` VALUES (2,17,1234,1234,'Active','tcp','LoadBalancing',4,4,204,'c29da8fe-5eb8-4eeb-8e49-f62d8ed5e157','2011-02-10 09:23:42'),(3,10,22,22,'Active','tcp','PortForwarding',2,1,205,'7f84811f-4000-4bf4-9fee-eec1de3de802','2011-03-28 12:07:30'),(4,10,80,80,'Add','tcp','LoadBalancing',2,1,205,'9d9a102d-670a-497b-a24b-25612253c6e4','2011-03-28 12:08:00'),(5,27,80,80,'Active','tcp','PortForwarding',70,60,224,'32733d45-f6a8-4b1c-b948-2d762e0df990','2011-05-12 18:52:41');
/*!40000 ALTER TABLE `firewall_rules` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `guest_os`
--

DROP TABLE IF EXISTS `guest_os`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `guest_os` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `category_id` bigint(20) unsigned NOT NULL,
  `name` varchar(255) default NULL,
  `display_name` varchar(255) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `fk_guest_os__category_id` (`category_id`),
  CONSTRAINT `fk_guest_os__category_id` FOREIGN KEY (`category_id`) REFERENCES `guest_os_category` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=138 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `guest_os`
--

LOCK TABLES `guest_os` WRITE;
/*!40000 ALTER TABLE `guest_os` DISABLE KEYS */;
INSERT INTO `guest_os` VALUES (1,1,NULL,'CentOS 4.5 (32-bit)'),(2,1,NULL,'CentOS 4.6 (32-bit)'),(3,1,NULL,'CentOS 4.7 (32-bit)'),(4,1,NULL,'CentOS 4.8 (32-bit)'),(5,1,NULL,'CentOS 5.0 (32-bit)'),(6,1,NULL,'CentOS 5.0 (64-bit)'),(7,1,NULL,'CentOS 5.1 (32-bit)'),(8,1,NULL,'CentOS 5.1 (64-bit)'),(9,1,NULL,'CentOS 5.2 (32-bit)'),(10,1,NULL,'CentOS 5.2 (64-bit)'),(11,1,NULL,'CentOS 5.3 (32-bit)'),(12,1,NULL,'CentOS 5.3 (64-bit)'),(13,1,NULL,'CentOS 5.4 (32-bit)'),(14,1,NULL,'CentOS 5.4 (64-bit)'),(15,2,NULL,'Debian GNU/Linux 5.0 (32-bit)'),(16,3,NULL,'Oracle Enterprise Linux 5.0 (32-bit)'),(17,3,NULL,'Oracle Enterprise Linux 5.0 (64-bit)'),(18,3,NULL,'Oracle Enterprise Linux 5.1 (32-bit)'),(19,3,NULL,'Oracle Enterprise Linux 5.1 (64-bit)'),(20,3,NULL,'Oracle Enterprise Linux 5.2 (32-bit)'),(21,3,NULL,'Oracle Enterprise Linux 5.2 (64-bit)'),(22,3,NULL,'Oracle Enterprise Linux 5.3 (32-bit)'),(23,3,NULL,'Oracle Enterprise Linux 5.3 (64-bit)'),(24,3,NULL,'Oracle Enterprise Linux 5.4 (32-bit)'),(25,3,NULL,'Oracle Enterprise Linux 5.4 (64-bit)'),(26,4,NULL,'Red Hat Enterprise Linux 4.5 (32-bit)'),(27,4,NULL,'Red Hat Enterprise Linux 4.6 (32-bit)'),(28,4,NULL,'Red Hat Enterprise Linux 4.7 (32-bit)'),(29,4,NULL,'Red Hat Enterprise Linux 4.8 (32-bit)'),(30,4,NULL,'Red Hat Enterprise Linux 5.0 (32-bit)'),(31,4,NULL,'Red Hat Enterprise Linux 5.0 (64-bit)'),(32,4,NULL,'Red Hat Enterprise Linux 5.1 (32-bit)'),(33,4,NULL,'Red Hat Enterprise Linux 5.1 (64-bit)'),(34,4,NULL,'Red Hat Enterprise Linux 5.2 (32-bit)'),(35,4,NULL,'Red Hat Enterprise Linux 5.2 (64-bit)'),(36,4,NULL,'Red Hat Enterprise Linux 5.3 (32-bit)'),(37,4,NULL,'Red Hat Enterprise Linux 5.3 (64-bit)'),(38,4,NULL,'Red Hat Enterprise Linux 5.4 (32-bit)'),(39,4,NULL,'Red Hat Enterprise Linux 5.4 (64-bit)'),(40,5,NULL,'SUSE Linux Enterprise Server 9 SP4 (32-bit)'),(41,5,NULL,'SUSE Linux Enterprise Server 10 SP1 (32-bit)'),(42,5,NULL,'SUSE Linux Enterprise Server 10 SP1 (64-bit)'),(43,5,NULL,'SUSE Linux Enterprise Server 10 SP2 (32-bit)'),(44,5,NULL,'SUSE Linux Enterprise Server 10 SP2 (64-bit)'),(45,5,NULL,'SUSE Linux Enterprise Server 10 SP3 (64-bit)'),(46,5,NULL,'SUSE Linux Enterprise Server 11 (32-bit)'),(47,5,NULL,'SUSE Linux Enterprise Server 11 (64-bit)'),(48,6,NULL,'Windows 7 (32-bit)'),(49,6,NULL,'Windows 7 (64-bit)'),(50,6,NULL,'Windows Server 2003 Enterprise Edition(32-bit)'),(51,6,NULL,'Windows Server 2003 Enterprise Edition(64-bit)'),(52,6,NULL,'Windows Server 2008 (32-bit)'),(53,6,NULL,'Windows Server 2008 (64-bit)'),(54,6,NULL,'Windows Server 2008 R2 (64-bit)'),(55,6,NULL,'Windows 2000 Server SP4 (32-bit)'),(56,6,NULL,'Windows Vista (32-bit)'),(57,6,NULL,'Windows XP SP2 (32-bit)'),(58,6,NULL,'Windows XP SP3 (32-bit)'),(59,10,NULL,'Other Ubuntu (32-bit)'),(60,7,NULL,'Other (32-bit)'),(61,6,NULL,'Windows 2000 Server'),(62,6,NULL,'Windows 98'),(63,6,NULL,'Windows 95'),(64,6,NULL,'Windows NT 4'),(65,6,NULL,'Windows 3.1'),(66,4,NULL,'Red Hat Enterprise Linux 3(32-bit)'),(67,4,NULL,'Red Hat Enterprise Linux 3(64-bit)'),(68,7,NULL,'Open Enterprise Server'),(69,7,NULL,'Asianux 3(32-bit)'),(70,7,NULL,'Asianux 3(64-bit)'),(72,2,NULL,'Debian GNU/Linux 5(64-bit)'),(73,2,NULL,'Debian GNU/Linux 4(32-bit)'),(74,2,NULL,'Debian GNU/Linux 4(64-bit)'),(75,7,NULL,'Other 2.6x Linux (32-bit)'),(76,7,NULL,'Other 2.6x Linux (64-bit)'),(77,8,NULL,'Novell Netware 6.x'),(78,8,NULL,'Novell Netware 5.1'),(79,9,NULL,'Sun Solaris 10(32-bit)'),(80,9,NULL,'Sun Solaris 10(64-bit)'),(81,9,NULL,'Sun Solaris 9(Experimental)'),(82,9,NULL,'Sun Solaris 8(Experimental)'),(83,9,NULL,'FreeBSD (32-bit)'),(84,9,NULL,'FreeBSD (64-bit)'),(85,9,NULL,'SCO OpenServer 5'),(86,9,NULL,'SCO UnixWare 7'),(87,6,NULL,'Windows Server 2003 DataCenter Edition(32-bit)'),(88,6,NULL,'Windows Server 2003 DataCenter Edition(64-bit)'),(89,6,NULL,'Windows Server 2003 Standard Edition(32-bit)'),(90,6,NULL,'Windows Server 2003 Standard Edition(64-bit)'),(91,6,NULL,'Windows Server 2003 Web Edition'),(92,6,NULL,'Microsoft Small Bussiness Server 2003'),(93,6,NULL,'Windows XP (32-bit)'),(94,6,NULL,'Windows XP (64-bit)'),(95,6,NULL,'Windows 2000 Advanced Server'),(96,5,NULL,'SUSE Linux Enterprise 8(32-bit)'),(97,5,NULL,'SUSE Linux Enterprise 8(64-bit)'),(98,7,NULL,'Other Linux (32-bit)'),(99,7,NULL,'Other Linux (64-bit)'),(100,10,NULL,'Other Ubuntu (64-bit)'),(101,6,NULL,'Windows Vista (64-bit)'),(102,6,NULL,'DOS'),(103,7,NULL,'Other (64-bit)'),(104,7,NULL,'OS/2'),(105,6,NULL,'Windows 2000 Professional'),(106,4,NULL,'Red Hat Enterprise Linux 4(64-bit)'),(107,5,NULL,'SUSE Linux Enterprise 9(32-bit)'),(108,5,NULL,'SUSE Linux Enterprise 9(64-bit)'),(109,5,NULL,'SUSE Linux Enterprise 10(32-bit)'),(110,5,NULL,'SUSE Linux Enterprise 10(64-bit)'),(111,1,NULL,'CentOS 5.5 (32-bit)'),(112,1,NULL,'CentOS 5.5 (64-bit)'),(113,4,NULL,'Red Hat Enterprise Linux 5.5 (32-bit)'),(114,4,NULL,'Red Hat Enterprise Linux 5.5 (64-bit)'),(115,4,NULL,'Fedora 13'),(116,4,NULL,'Fedora 12'),(117,4,NULL,'Fedora 11'),(118,4,NULL,'Fedora 10'),(119,4,NULL,'Fedora 9'),(120,4,NULL,'Fedora 8'),(121,10,NULL,'Ubuntu 10.04 (32-bit)'),(122,10,NULL,'Ubuntu 9.10 (32-bit)'),(123,10,NULL,'Ubuntu 9.04 (32-bit)'),(124,10,NULL,'Ubuntu 8.10 (32-bit)'),(125,10,NULL,'Ubuntu 8.04 (32-bit)'),(126,10,NULL,'Ubuntu 10.04 (64-bit)'),(127,10,NULL,'Ubuntu 9.10 (64-bit)'),(128,10,NULL,'Ubuntu 9.04 (64-bit)'),(129,10,NULL,'Ubuntu 8.10 (64-bit)'),(130,10,NULL,'Ubuntu 8.04 (64-bit)'),(131,10,NULL,'Red Hat Enterprise Linux 2'),(132,2,NULL,'Debian GNU/Linux 6(32-bit)'),(133,2,NULL,'Debian GNU/Linux 6(64-bit)'),(134,3,NULL,'Oracle Enterprise Linux 5.5 (32-bit)'),(135,3,NULL,'Oracle Enterprise Linux 5.5 (64-bit)'),(136,4,NULL,'Red Hat Enterprise Linux 6.0 (32-bit)'),(137,4,NULL,'Red Hat Enterprise Linux 6.0 (64-bit)');
/*!40000 ALTER TABLE `guest_os` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `guest_os_category`
--

DROP TABLE IF EXISTS `guest_os_category`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `guest_os_category` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `guest_os_category`
--

LOCK TABLES `guest_os_category` WRITE;
/*!40000 ALTER TABLE `guest_os_category` DISABLE KEYS */;
INSERT INTO `guest_os_category` VALUES (1,'CentOS'),(2,'Debian'),(3,'Oracle'),(4,'RedHat'),(5,'SUSE'),(6,'Windows'),(7,'Other'),(8,'Novel'),(9,'Unix'),(10,'Ubuntu');
/*!40000 ALTER TABLE `guest_os_category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `guest_os_hypervisor`
--

DROP TABLE IF EXISTS `guest_os_hypervisor`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `guest_os_hypervisor` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `hypervisor_type` varchar(32) NOT NULL,
  `guest_os_name` varchar(255) NOT NULL,
  `guest_os_id` bigint(20) unsigned NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=256 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `guest_os_hypervisor`
--

LOCK TABLES `guest_os_hypervisor` WRITE;
/*!40000 ALTER TABLE `guest_os_hypervisor` DISABLE KEYS */;
INSERT INTO `guest_os_hypervisor` VALUES (1,'XenServer','CentOS 4.5 (32-bit)',1),(2,'XenServer','CentOS 4.6 (32-bit)',2),(3,'XenServer','CentOS 4.7 (32-bit)',3),(4,'XenServer','CentOS 4.8 (32-bit)',4),(5,'XenServer','CentOS 5.0 (32-bit)',5),(6,'XenServer','CentOS 5.0 (64-bit)',6),(7,'XenServer','CentOS 5.1 (32-bit)',7),(8,'XenServer','CentOS 5.1 (32-bit)',8),(9,'XenServer','CentOS 5.2 (32-bit)',9),(10,'XenServer','CentOS 5.2 (64-bit)',10),(11,'XenServer','CentOS 5.3 (32-bit)',11),(12,'XenServer','CentOS 5.3 (64-bit)',12),(13,'XenServer','CentOS 5.4 (32-bit)',13),(14,'XenServer','CentOS 5.4 (64-bit)',14),(15,'XenServer','Debian Lenny 5.0 (32-bit)',15),(16,'XenServer','Oracle Enterprise Linux 5.0 (32-bit)',16),(17,'XenServer','Oracle Enterprise Linux 5.0 (64-bit)',17),(18,'XenServer','Oracle Enterprise Linux 5.1 (32-bit)',18),(19,'XenServer','Oracle Enterprise Linux 5.1 (64-bit)',19),(20,'XenServer','Oracle Enterprise Linux 5.2 (32-bit)',20),(21,'XenServer','Oracle Enterprise Linux 5.2 (64-bit)',21),(22,'XenServer','Oracle Enterprise Linux 5.3 (32-bit)',22),(23,'XenServer','Oracle Enterprise Linux 5.3 (64-bit)',23),(24,'XenServer','Oracle Enterprise Linux 5.4 (32-bit)',24),(25,'XenServer','Oracle Enterprise Linux 5.4 (64-bit)',25),(26,'XenServer','Red Hat Enterprise Linux 4.5 (32-bit)',26),(27,'XenServer','Red Hat Enterprise Linux 4.6 (32-bit)',27),(28,'XenServer','Red Hat Enterprise Linux 4.7 (32-bit)',28),(29,'XenServer','Red Hat Enterprise Linux 4.8 (32-bit)',29),(30,'XenServer','Red Hat Enterprise Linux 5.0 (32-bit)',30),(31,'XenServer','Red Hat Enterprise Linux 5.0 (64-bit)',31),(32,'XenServer','Red Hat Enterprise Linux 5.1 (32-bit)',32),(33,'XenServer','Red Hat Enterprise Linux 5.1 (64-bit)',33),(34,'XenServer','Red Hat Enterprise Linux 5.2 (32-bit)',34),(35,'XenServer','Red Hat Enterprise Linux 5.2 (64-bit)',35),(36,'XenServer','Red Hat Enterprise Linux 5.3 (32-bit)',36),(37,'XenServer','Red Hat Enterprise Linux 5.3 (64-bit)',37),(38,'XenServer','Red Hat Enterprise Linux 5.4 (32-bit)',38),(39,'XenServer','Red Hat Enterprise Linux 5.4 (64-bit)',39),(40,'XenServer','SUSE Linux Enterprise Server 9 SP4 (32-bit)',40),(41,'XenServer','SUSE Linux Enterprise Server 10 SP1 (32-bit)',41),(42,'XenServer','SUSE Linux Enterprise Server 10 SP1 (64-bit)',42),(43,'XenServer','SUSE Linux Enterprise Server 10 SP2 (32-bit)',43),(44,'XenServer','SUSE Linux Enterprise Server 10 SP2 (64-bit)',44),(45,'XenServer','SUSE Linux Enterprise Server 10 SP3 (64-bit)',45),(46,'XenServer','SUSE Linux Enterprise Server 11 (32-bit)',46),(47,'XenServer','SUSE Linux Enterprise Server 11 (64-bit)',47),(48,'XenServer','Windows 7 (32-bit)',48),(49,'XenServer','Windows 7 (64-bit)',49),(50,'XenServer','Windows Server 2003 (32-bit)',50),(51,'XenServer','Windows Server 2003 (64-bit)',51),(52,'XenServer','Windows Server 2008 (32-bit)',52),(53,'XenServer','Windows Server 2008 (64-bit)',53),(54,'XenServer','Windows Server 2008 R2 (64-bit)',54),(55,'XenServer','Windows 2000 SP4 (32-bit)',55),(56,'XenServer','Windows Vista (32-bit)',56),(57,'XenServer','Windows XP SP2 (32-bit)',57),(58,'XenServer','Windows XP SP3 (32-bit)',58),(59,'XenServer','Other install media',59),(60,'XenServer','Other install media',100),(61,'XenServer','Other install media',60),(62,'XenServer','Other install media',103),(63,'XenServer','Other install media',121),(64,'XenServer','Other install media',126),(65,'XenServer','Other install media',122),(66,'XenServer','Other install media',127),(67,'XenServer','Other install media',123),(68,'XenServer','Other install media',128),(69,'XenServer','Other install media',124),(70,'XenServer','Other install media',129),(71,'XenServer','Other install media',125),(72,'XenServer','Other install media',130),(73,'VmWare','Microsoft Windows 7(32-bit)',48),(74,'VmWare','Microsoft Windows 7(64-bit)',49),(75,'VmWare','Microsoft Windows Server 2008 R2(64-bit)',54),(76,'VmWare','Microsoft Windows Server 2008(32-bit)',52),(77,'VmWare','Microsoft Windows Server 2008(64-bit)',53),(78,'VmWare','Microsoft Windows Server 2003, Enterprise Edition (32-bit)',50),(79,'VmWare','Microsoft Windows Server 2003, Enterprise Edition (64-bit)',51),(80,'VmWare','Microsoft Windows Server 2003, Datacenter Edition (32-bit)',87),(81,'VmWare','Microsoft Windows Server 2003, Datacenter Edition (64-bit)',88),(82,'VmWare','Microsoft Windows Server 2003, Standard Edition (32-bit)',89),(83,'VmWare','Microsoft Windows Server 2003, Standard Edition (64-bit)',90),(84,'VmWare','Microsoft Windows Server 2003, Web Edition',91),(85,'VmWare','Microsoft Small Bussiness Server 2003',92),(86,'VmWare','Microsoft Windows Vista (32-bit)',56),(87,'VmWare','Microsoft Windows Vista (64-bit)',101),(88,'VmWare','Microsoft Windows XP Professional (32-bit)',93),(89,'VmWare','Microsoft Windows XP Professional (32-bit)',57),(90,'VmWare','Microsoft Windows XP Professional (32-bit)',58),(91,'VmWare','Microsoft Windows XP Professional (64-bit)',94),(92,'VmWare','Microsoft Windows 2000 Advanced Server',95),(93,'VmWare','Microsoft Windows 2000 Server',61),(94,'VmWare','Microsoft Windows 2000 Professional',105),(95,'VmWare','Microsoft Windows 2000 Server',55),(96,'VmWare','Microsoft Windows 98',62),(97,'VmWare','Microsoft Windows 95',63),(98,'VmWare','Microsoft Windows NT 4',64),(99,'VmWare','Microsoft Windows 3.1',65),(100,'VmWare','Red Hat Enterprise Linux 5(32-bit)',30),(101,'VmWare','Red Hat Enterprise Linux 5(32-bit)',32),(102,'VmWare','Red Hat Enterprise Linux 5(32-bit)',34),(103,'VmWare','Red Hat Enterprise Linux 5(32-bit)',36),(104,'VmWare','Red Hat Enterprise Linux 5(32-bit)',38),(105,'VmWare','Red Hat Enterprise Linux 5(64-bit)',31),(106,'VmWare','Red Hat Enterprise Linux 5(64-bit)',33),(107,'VmWare','Red Hat Enterprise Linux 5(64-bit)',35),(108,'VmWare','Red Hat Enterprise Linux 5(64-bit)',37),(109,'VmWare','Red Hat Enterprise Linux 5(64-bit)',39),(110,'VmWare','Red Hat Enterprise Linux 4(32-bit)',26),(111,'VmWare','Red Hat Enterprise Linux 4(32-bit)',27),(112,'VmWare','Red Hat Enterprise Linux 4(32-bit)',28),(113,'VmWare','Red Hat Enterprise Linux 4(32-bit)',29),(114,'VmWare','Red Hat Enterprise Linux 4(64-bit)',106),(115,'VmWare','Red Hat Enterprise Linux 3(32-bit)',66),(116,'VmWare','Red Hat Enterprise Linux 3(64-bit)',67),(117,'VmWare','Red Hat Enterprise Linux 2',131),(118,'VmWare','Suse Linux Enterprise 11(32-bit)',46),(119,'VmWare','Suse Linux Enterprise 11(64-bit)',47),(120,'VmWare','Suse Linux Enterprise 10(32-bit)',41),(121,'VmWare','Suse Linux Enterprise 10(32-bit)',43),(122,'VmWare','Suse Linux Enterprise 10(64-bit)',42),(123,'VmWare','Suse Linux Enterprise 10(64-bit)',44),(124,'VmWare','Suse Linux Enterprise 10(64-bit)',45),(125,'VmWare','Suse Linux Enterprise 10(32-bit)',109),(126,'VmWare','Suse Linux Enterprise 10(64-bit)',110),(127,'VmWare','Suse Linux Enterprise 8/9(32-bit)',40),(128,'VmWare','Suse Linux Enterprise 8/9(32-bit)',96),(129,'VmWare','Suse Linux Enterprise 8/9(64-bit)',97),(130,'VmWare','Suse Linux Enterprise 8/9(32-bit)',107),(131,'VmWare','Suse Linux Enterprise 8/9(64-bit)',108),(132,'VmWare','Open Enterprise Server',68),(133,'VmWare','Asianux 3(32-bit)',69),(134,'VmWare','Asianux 3(64-bit)',70),(135,'VmWare','Debian GNU/Linux 5(32-bit)',15),(136,'VmWare','Debian GNU/Linux 5(64-bit)',72),(137,'VmWare','Debian GNU/Linux 4(32-bit)',73),(138,'VmWare','Debian GNU/Linux 4(64-bit)',74),(139,'VmWare','Ubuntu Linux (32-bit)',59),(140,'VmWare','Ubuntu Linux (32-bit)',121),(141,'VmWare','Ubuntu Linux (32-bit)',122),(142,'VmWare','Ubuntu Linux (32-bit)',123),(143,'VmWare','Ubuntu Linux (32-bit)',124),(144,'VmWare','Ubuntu Linux (32-bit)',125),(145,'VmWare','Ubuntu Linux (64-bit)',100),(146,'VmWare','Ubuntu Linux (64-bit)',126),(147,'VmWare','Ubuntu Linux (64-bit)',127),(148,'VmWare','Ubuntu Linux (64-bit)',128),(149,'VmWare','Ubuntu Linux (64-bit)',129),(150,'VmWare','Ubuntu Linux (64-bit)',130),(151,'VmWare','Other 2.6x Linux (32-bit)',75),(152,'VmWare','Other 2.6x Linux (64-bit)',76),(153,'VmWare','Other Linux (32-bit)',98),(154,'VmWare','Other Linux (64-bit)',99),(155,'VmWare','Novell Netware 6.x',77),(156,'VmWare','Novell Netware 5.1',78),(157,'VmWare','Sun Solaris 10(32-bit)',79),(158,'VmWare','Sun Solaris 10(64-bit)',80),(159,'VmWare','Sun Solaris 9(Experimental)',81),(160,'VmWare','Sun Solaris 8(Experimental)',82),(161,'VmWare','FreeBSD (32-bit)',83),(162,'VmWare','FreeBSD (64-bit)',84),(163,'VmWare','OS/2',104),(164,'VmWare','SCO OpenServer 5',85),(165,'VmWare','SCO UnixWare 7',86),(166,'VmWare','DOS',102),(167,'VmWare','Other (32-bit)',60),(168,'VmWare','Other (64-bit)',103),(169,'KVM','CentOS 4.5',1),(170,'KVM','CentOS 4.6',2),(171,'KVM','CentOS 4.7',3),(172,'KVM','CentOS 4.8',4),(173,'KVM','CentOS 5.0',5),(174,'KVM','CentOS 5.0',6),(175,'KVM','CentOS 5.1',7),(176,'KVM','CentOS 5.1',8),(177,'KVM','CentOS 5.2',9),(178,'KVM','CentOS 5.2',10),(179,'KVM','CentOS 5.3',11),(180,'KVM','CentOS 5.3',12),(181,'KVM','CentOS 5.4',13),(182,'KVM','CentOS 5.4',14),(183,'KVM','CentOS 5.5',111),(184,'KVM','CentOS 5.5',112),(185,'KVM','Red Hat Enterprise Linux 4.5',26),(186,'KVM','Red Hat Enterprise Linux 4.6',27),(187,'KVM','Red Hat Enterprise Linux 4.7',28),(188,'KVM','Red Hat Enterprise Linux 4.8',29),(189,'KVM','Red Hat Enterprise Linux 5.0',30),(190,'KVM','Red Hat Enterprise Linux 5.0',31),(191,'KVM','Red Hat Enterprise Linux 5.1',32),(192,'KVM','Red Hat Enterprise Linux 5.1',33),(193,'KVM','Red Hat Enterprise Linux 5.2',34),(194,'KVM','Red Hat Enterprise Linux 5.2',35),(195,'KVM','Red Hat Enterprise Linux 5.3',36),(196,'KVM','Red Hat Enterprise Linux 5.3',37),(197,'KVM','Red Hat Enterprise Linux 5.4',38),(198,'KVM','Red Hat Enterprise Linux 5.4',39),(199,'KVM','Red Hat Enterprise Linux 5.5',113),(200,'KVM','Red Hat Enterprise Linux 5.5',114),(201,'KVM','Red Hat Enterprise Linux 4',106),(202,'KVM','Red Hat Enterprise Linux 3',66),(203,'KVM','Red Hat Enterprise Linux 3',67),(204,'KVM','Red Hat Enterprise Linux 2',131),(205,'KVM','Fedora 13',115),(206,'KVM','Fedora 12',116),(207,'KVM','Fedora 11',117),(208,'KVM','Fedora 10',118),(209,'KVM','Fedora 9',119),(210,'KVM','Fedora 8',120),(211,'KVM','Ubuntu 10.04',121),(212,'KVM','Ubuntu 10.04',126),(213,'KVM','Ubuntu 9.10',122),(214,'KVM','Ubuntu 9.10',127),(215,'KVM','Ubuntu 9.04',123),(216,'KVM','Ubuntu 9.04',128),(217,'KVM','Ubuntu 8.10',124),(218,'KVM','Ubuntu 8.10',129),(219,'KVM','Ubuntu 8.04',125),(220,'KVM','Ubuntu 8.04',130),(221,'KVM','Debian GNU/Linux 5',15),(222,'KVM','Debian GNU/Linux 5',72),(223,'KVM','Debian GNU/Linux 4',73),(224,'KVM','Debian GNU/Linux 4',74),(225,'KVM','Other Linux 2.6x',75),(226,'KVM','Other Linux 2.6x',76),(227,'KVM','Other Ubuntu',59),(228,'KVM','Other Ubuntu',100),(229,'KVM','Other Linux',98),(230,'KVM','Other Linux',99),(231,'KVM','Windows 7',48),(232,'KVM','Windows 7',49),(233,'KVM','Windows Server 2003',50),(234,'KVM','Windows Server 2003',51),(235,'KVM','Windows Server 2003',87),(236,'KVM','Windows Server 2003',88),(237,'KVM','Windows Server 2003',89),(238,'KVM','Windows Server 2003',90),(239,'KVM','Windows Server 2003',91),(240,'KVM','Windows Server 2003',92),(241,'KVM','Windows Server 2008',52),(242,'KVM','Windows Server 2008',53),(243,'KVM','Windows 2000',55),(244,'KVM','Windows 2000',61),(245,'KVM','Windows 2000',95),(246,'KVM','Windows 98',62),(247,'KVM','Windows Vista',56),(248,'KVM','Windows Vista',101),(249,'KVM','Windows XP SP2',57),(250,'KVM','Windows XP SP3',58),(251,'KVM','Windows XP ',93),(252,'KVM','Windows XP ',94),(253,'KVM','DOS',102),(254,'KVM','Other',60),(255,'KVM','Other',103);
/*!40000 ALTER TABLE `guest_os_hypervisor` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `host`
--

DROP TABLE IF EXISTS `host`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `host` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `name` varchar(255) NOT NULL,
  `status` varchar(32) NOT NULL,
  `type` varchar(32) NOT NULL,
  `private_ip_address` varchar(15) NOT NULL,
  `private_netmask` varchar(15) default NULL,
  `private_mac_address` varchar(17) default NULL,
  `storage_ip_address` varchar(15) NOT NULL,
  `storage_netmask` varchar(15) default NULL,
  `storage_mac_address` varchar(17) default NULL,
  `storage_ip_address_2` varchar(15) default NULL,
  `storage_mac_address_2` varchar(17) default NULL,
  `storage_netmask_2` varchar(15) default NULL,
  `cluster_id` bigint(20) unsigned default NULL COMMENT 'foreign key to cluster',
  `public_ip_address` varchar(15) default NULL,
  `public_netmask` varchar(15) default NULL,
  `public_mac_address` varchar(17) default NULL,
  `proxy_port` int(10) unsigned default NULL,
  `data_center_id` bigint(20) unsigned NOT NULL,
  `pod_id` bigint(20) unsigned default NULL,
  `cpus` int(10) unsigned default NULL,
  `speed` int(10) unsigned default NULL,
  `url` varchar(255) default NULL COMMENT 'iqn for the servers',
  `fs_type` varchar(32) default NULL,
  `hypervisor_type` varchar(32) default NULL COMMENT 'hypervisor type, can be NONE for storage',
  `ram` bigint(20) unsigned default NULL,
  `resource` varchar(255) default NULL COMMENT 'If it is a local resource, this is the class name',
  `version` varchar(40) NOT NULL,
  `parent` varchar(255) default NULL COMMENT 'parent path for the storage server',
  `total_size` bigint(20) unsigned default NULL COMMENT 'TotalSize',
  `capabilities` varchar(255) default NULL COMMENT 'host capabilities in comma separated list',
  `guid` varchar(255) default NULL,
  `available` int(1) unsigned NOT NULL default '1' COMMENT 'Is this host ready for more resources?',
  `setup` int(1) unsigned NOT NULL default '0' COMMENT 'Is this host already setup?',
  `dom0_memory` bigint(20) unsigned NOT NULL COMMENT 'memory used by dom0 for computing and routing servers',
  `last_ping` int(10) unsigned NOT NULL COMMENT 'time in seconds from the start of machine of the last ping',
  `mgmt_server_id` bigint(20) unsigned default NULL COMMENT 'ManagementServer this host is connected to.',
  `disconnected` datetime default NULL COMMENT 'Time this was disconnected',
  `created` datetime default NULL COMMENT 'date the host first signed on',
  `removed` datetime default NULL COMMENT 'date removed if not null',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `guid` (`guid`),
  KEY `i_host__removed` (`removed`),
  KEY `i_host__last_ping` (`last_ping`),
  KEY `i_host__status` (`status`),
  KEY `i_host__data_center_id` (`data_center_id`),
  KEY `i_host__pod_id` (`pod_id`),
  KEY `fk_host__cluster_id` (`cluster_id`),
  CONSTRAINT `fk_host__cluster_id` FOREIGN KEY (`cluster_id`) REFERENCES `cluster` (`id`),
  CONSTRAINT `fk_host__pod_id` FOREIGN KEY (`pod_id`) REFERENCES `host_pod_ref` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `host`
--

LOCK TABLES `host` WRITE;
/*!40000 ALTER TABLE `host` DISABLE KEYS */;
INSERT INTO `host` VALUES (1,'ezh-sit-xen1.vmopsdev.net','Alert','Routing','192.168.154.43','255.255.255.0','00:15:c5:fa:84:47','192.168.154.43','255.255.255.0','00:15:c5:fa:84:47','192.168.154.43','00:15:c5:fa:84:47','255.255.255.0',NULL,NULL,NULL,NULL,NULL,1,1,2,3200,'iqn.2010-08.com.example:c9866fd0',NULL,'XenServer',3320174592,'com.cloud.hypervisor.xen.resource.XenServer56Resource','2.2.2',NULL,NULL,'xen-3.0-x86_64 , xen-3.0-x86_32p , hvm-3.0-x86_32 , hvm-3.0-x86_32p , hvm-3.0-x86_64',NULL,1,1,0,1274140476,NULL,'2011-04-18 17:42:31','2011-02-10 06:57:19','2011-05-06 22:11:29'),(2,'nfs://nfs1.lab.vmops.com/export/home/david/ezh-sit-secondary','Alert','SecondaryStorage','192.168.10.231','255.255.255.0','06:37:04:00:00:01','192.168.154.233','255.255.255.0','06:37:04:00:00:01',NULL,NULL,NULL,NULL,'66.149.231.114','255.255.255.0','06:b8:b6:00:00:11',NULL,1,1,NULL,NULL,'nfs://nfs1.lab.vmops.com/export/home/david/ezh-sit-secondary',NULL,'None',0,NULL,'2.2.1','/mnt/SecStorage/67350d35',1925386469376,NULL,'nfs://nfs1.lab.vmops.com/export/home/david/ezh-sit-secondary',1,0,0,1272605407,NULL,'2011-04-18 17:42:31','2011-02-10 07:02:13',NULL),(3,'v-1-VM','Alert','ConsoleProxy','192.168.154.234','255.255.255.0','06:6d:58:00:00:02','192.168.154.234','255.255.255.0','06:6d:58:00:00:02',NULL,NULL,NULL,NULL,'66.149.231.115','255.255.255.0','06:30:a6:00:00:12',80,1,1,NULL,NULL,'NoIqn',NULL,NULL,0,NULL,'2.2.1',NULL,NULL,NULL,'Proxy.1-ConsoleProxyResource',1,0,0,1272605407,NULL,'2011-04-18 17:42:31','2011-02-10 07:04:58',NULL),(4,'ezh-sit-xen1.vmopsdev.net','Up','Routing','192.168.181.5','255.255.255.0','b8:ac:6f:8e:95:32','192.168.181.5','255.255.255.0','b8:ac:6f:8e:95:32','192.168.181.5','b8:ac:6f:8e:95:32','255.255.255.0',2,NULL,NULL,NULL,NULL,2,2,4,2260,'iqn.2005-03.org.open-iscsi:97bf0f86560',NULL,'XenServer',3701658240,'com.cloud.hypervisor.xen.resource.XenServer56Resource','2.2.2',NULL,NULL,'xen-3.0-x86_64 , xen-3.0-x86_32p , hvm-3.0-x86_32 , hvm-3.0-x86_32p , hvm-3.0-x86_64','fd4e7bb7-e628-4d04-9604-fffd1cabfeb0',1,1,0,1274985043,6602001285584,'2011-05-16 20:59:23','2011-05-06 22:05:24',NULL),(5,'nfs://nfs2.lab.vmops.com/export/profserv/ezh-sit/secondary','Up','SecondaryStorage','192.168.110.232','255.255.255.0','06:8e:64:00:00:04','192.168.181.238','255.255.255.0','06:8e:64:00:00:04',NULL,NULL,NULL,NULL,'172.16.64.48','255.255.252.0','06:f2:2e:00:00:0e',NULL,2,2,NULL,NULL,'nfs://nfs2.lab.vmops.com/export/profserv/ezh-sit/secondary',NULL,'None',0,NULL,'2.2.2','/mnt/SecStorage/5e574315',1930519904256,NULL,'nfs://nfs2.lab.vmops.com/export/profserv/ezh-sit/secondary',1,0,0,1274985034,6602001285584,'2011-05-16 20:59:23','2011-05-06 22:06:45',NULL),(6,'v-40-VM','Up','ConsoleProxy','192.168.181.237','255.255.255.0','06:22:d6:00:00:03','192.168.181.237','255.255.255.0','06:22:d6:00:00:03',NULL,NULL,NULL,NULL,'172.16.64.47','255.255.252.0','06:d0:be:00:00:0d',80,2,2,NULL,NULL,'NoIqn',NULL,NULL,0,NULL,'2.2.2',NULL,NULL,NULL,'Proxy.40-ConsoleProxyResource',1,0,0,1274985083,6602001285584,'2011-05-16 20:59:23','2011-05-06 22:09:15',NULL);
/*!40000 ALTER TABLE `host` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `host_details`
--

DROP TABLE IF EXISTS `host_details`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `host_details` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `host_id` bigint(20) unsigned NOT NULL COMMENT 'host id',
  `name` varchar(255) NOT NULL,
  `value` varchar(255) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `fk_host_details__host_id` (`host_id`),
  CONSTRAINT `fk_host_details__host_id` FOREIGN KEY (`host_id`) REFERENCES `host` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=37 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `host_details`
--

LOCK TABLES `host_details` WRITE;
/*!40000 ALTER TABLE `host_details` DISABLE KEYS */;
INSERT INTO `host_details` VALUES (16,2,'mount.parent','dummy'),(17,2,'mount.path','dummy'),(18,2,'orig.url','nfs://nfs1.lab.vmops.com/export/home/david/ezh-sit-secondary'),(19,4,'public.network.device','Pool-wide network associated with eth0'),(20,4,'private.network.device','Pool-wide network associated with eth0'),(21,4,'com.cloud.network.Networks.RouterPrivateIpStrategy','DcGlobal'),(22,4,'Hypervisor.Version','3.4.2'),(23,4,'Host.OS','XenServer'),(24,4,'Host.OS.Kernel.Version','2.6.27.42-0.1.1.xs5.6.0.44.111158xen'),(25,4,'wait','600'),(26,4,'storage.network.device2','cloud-stor2'),(27,4,'password','password'),(28,4,'storage.network.device1','cloud-stor1'),(29,4,'url','192.168.181.5'),(30,4,'username','root'),(31,4,'guest.network.device','Pool-wide network associated with eth0'),(32,4,'can_bridge_firewall','false'),(33,4,'Host.OS.Version','5.6.0'),(34,5,'mount.parent','dummy'),(35,5,'mount.path','dummy'),(36,5,'orig.url','nfs://nfs2.lab.vmops.com/export/profserv/ezh-sit/secondary');
/*!40000 ALTER TABLE `host_details` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `host_master`
--

DROP TABLE IF EXISTS `host_master`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `host_master` (
  `id` bigint(20) unsigned NOT NULL auto_increment COMMENT 'id',
  `type` varchar(32) NOT NULL,
  `service_address` varchar(255) NOT NULL,
  `admin` varchar(32) NOT NULL,
  `password` varchar(32) default NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`),
  UNIQUE KEY `i_host_master__service_address` (`service_address`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `host_master`
--

LOCK TABLES `host_master` WRITE;
/*!40000 ALTER TABLE `host_master` DISABLE KEYS */;
/*!40000 ALTER TABLE `host_master` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `host_pod_ref`
--

DROP TABLE IF EXISTS `host_pod_ref`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `host_pod_ref` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `name` varchar(255) NOT NULL,
  `data_center_id` bigint(20) unsigned NOT NULL,
  `gateway` varchar(255) NOT NULL COMMENT 'gateway for the pod',
  `cidr_address` varchar(15) NOT NULL COMMENT 'CIDR address for the pod',
  `cidr_size` bigint(20) unsigned NOT NULL COMMENT 'CIDR size for the pod',
  `description` varchar(255) default NULL COMMENT 'store private ip range in startIP-endIP format',
  `enabled` tinyint(4) NOT NULL default '1' COMMENT 'Is this Pod enabled for activity',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`),
  UNIQUE KEY `name` (`name`,`data_center_id`),
  KEY `i_host_pod_ref__data_center_id` (`data_center_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `host_pod_ref`
--

LOCK TABLES `host_pod_ref` WRITE;
/*!40000 ALTER TABLE `host_pod_ref` DISABLE KEYS */;
INSERT INTO `host_pod_ref` VALUES (1,'Pod 1',1,'192.168.154.1','192.168.154.0',24,'192.168.154.233-192.168.154.235',1),(2,'Rack 8',2,'192.168.181.1','192.168.181.0',24,'192.168.181.235-192.168.181.239',1);
/*!40000 ALTER TABLE `host_pod_ref` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `host_tags`
--

DROP TABLE IF EXISTS `host_tags`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `host_tags` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `host_id` bigint(20) unsigned NOT NULL COMMENT 'host id',
  `tag` varchar(255) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `fk_host_tags__host_id` (`host_id`),
  CONSTRAINT `fk_host_tags__host_id` FOREIGN KEY (`host_id`) REFERENCES `host` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `host_tags`
--

LOCK TABLES `host_tags` WRITE;
/*!40000 ALTER TABLE `host_tags` DISABLE KEYS */;
/*!40000 ALTER TABLE `host_tags` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hypervsior_properties`
--

DROP TABLE IF EXISTS `hypervsior_properties`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `hypervsior_properties` (
  `hypervisor` varchar(32) NOT NULL COMMENT 'hypervisor type',
  `max_storage_devices` int(10) NOT NULL COMMENT 'maximum number of storage devices',
  `cdrom_device` int(10) NOT NULL COMMENT 'device id reserved for cdrom',
  `max_network_devices` int(10) NOT NULL COMMENT 'maximum number of network devices',
  UNIQUE KEY `hypervisor` (`hypervisor`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `hypervsior_properties`
--

LOCK TABLES `hypervsior_properties` WRITE;
/*!40000 ALTER TABLE `hypervsior_properties` DISABLE KEYS */;
/*!40000 ALTER TABLE `hypervsior_properties` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `instance_group`
--

DROP TABLE IF EXISTS `instance_group`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `instance_group` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `account_id` bigint(20) unsigned NOT NULL COMMENT 'owner.  foreign key to account table',
  `name` varchar(255) NOT NULL,
  `removed` datetime default NULL COMMENT 'date the group was removed',
  `created` datetime default NULL COMMENT 'date the group was created',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`),
  KEY `fk_instance_group__account_id` (`account_id`),
  CONSTRAINT `fk_instance_group__account_id` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `instance_group`
--

LOCK TABLES `instance_group` WRITE;
/*!40000 ALTER TABLE `instance_group` DISABLE KEYS */;
INSERT INTO `instance_group` VALUES (1,4,'second',NULL,'2011-02-10 08:47:36'),(2,4,'third',NULL,'2011-02-10 08:48:51'),(3,2,'adsa',NULL,'2011-02-21 04:49:43');
/*!40000 ALTER TABLE `instance_group` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `instance_group_vm_map`
--

DROP TABLE IF EXISTS `instance_group_vm_map`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `instance_group_vm_map` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `group_id` bigint(20) unsigned NOT NULL,
  `instance_id` bigint(20) unsigned NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `fk_instance_group_vm_map___group_id` (`group_id`),
  KEY `fk_instance_group_vm_map___instance_id` (`instance_id`),
  CONSTRAINT `fk_instance_group_vm_map___group_id` FOREIGN KEY (`group_id`) REFERENCES `instance_group` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_instance_group_vm_map___instance_id` FOREIGN KEY (`instance_id`) REFERENCES `user_vm` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `instance_group_vm_map`
--

LOCK TABLES `instance_group_vm_map` WRITE;
/*!40000 ALTER TABLE `instance_group_vm_map` DISABLE KEYS */;
INSERT INTO `instance_group_vm_map` VALUES (3,3,16);
/*!40000 ALTER TABLE `instance_group_vm_map` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `launch_permission`
--

DROP TABLE IF EXISTS `launch_permission`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `launch_permission` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `template_id` bigint(20) unsigned NOT NULL,
  `account_id` bigint(20) unsigned NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `i_launch_permission_template_id` (`template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `launch_permission`
--

LOCK TABLES `launch_permission` WRITE;
/*!40000 ALTER TABLE `launch_permission` DISABLE KEYS */;
/*!40000 ALTER TABLE `launch_permission` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `load_balancer`
--

DROP TABLE IF EXISTS `load_balancer`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `load_balancer` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `name` varchar(255) NOT NULL,
  `description` varchar(4096) default NULL,
  `account_id` bigint(20) unsigned NOT NULL,
  `ip_address` varchar(15) NOT NULL,
  `public_port` varchar(10) NOT NULL,
  `private_port` varchar(10) NOT NULL,
  `algorithm` varchar(255) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `load_balancer`
--

LOCK TABLES `load_balancer` WRITE;
/*!40000 ALTER TABLE `load_balancer` DISABLE KEYS */;
/*!40000 ALTER TABLE `load_balancer` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `load_balancer_vm_map`
--

DROP TABLE IF EXISTS `load_balancer_vm_map`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `load_balancer_vm_map` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `load_balancer_id` bigint(20) unsigned NOT NULL,
  `instance_id` bigint(20) unsigned NOT NULL,
  `revoke` tinyint(1) unsigned NOT NULL default '0' COMMENT '1 is when rule is set for Revoke',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `load_balancer_id` (`load_balancer_id`,`instance_id`),
  KEY `fk_load_balancer_vm_map__instance_id` (`instance_id`),
  CONSTRAINT `fk_load_balancer_vm_map__instance_id` FOREIGN KEY (`instance_id`) REFERENCES `vm_instance` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_load_balancer_vm_map__load_balancer_id` FOREIGN KEY (`load_balancer_id`) REFERENCES `load_balancing_rules` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `load_balancer_vm_map`
--

LOCK TABLES `load_balancer_vm_map` WRITE;
/*!40000 ALTER TABLE `load_balancer_vm_map` DISABLE KEYS */;
/*!40000 ALTER TABLE `load_balancer_vm_map` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `load_balancing_rules`
--

DROP TABLE IF EXISTS `load_balancing_rules`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `load_balancing_rules` (
  `id` bigint(20) unsigned NOT NULL,
  `name` varchar(255) NOT NULL,
  `description` varchar(4096) default NULL COMMENT 'description',
  `default_port_start` int(10) NOT NULL COMMENT 'default private port range start',
  `default_port_end` int(10) NOT NULL COMMENT 'default destination port range end',
  `algorithm` varchar(255) NOT NULL,
  PRIMARY KEY  (`id`),
  CONSTRAINT `fk_load_balancing_rules__id` FOREIGN KEY (`id`) REFERENCES `firewall_rules` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `load_balancing_rules`
--

LOCK TABLES `load_balancing_rules` WRITE;
/*!40000 ALTER TABLE `load_balancing_rules` DISABLE KEYS */;
INSERT INTO `load_balancing_rules` VALUES (2,'venkat',NULL,22,22,'roundrobin'),(4,'dsg',NULL,80,80,'roundrobin');
/*!40000 ALTER TABLE `load_balancing_rules` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mshost`
--

DROP TABLE IF EXISTS `mshost`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `mshost` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `msid` bigint(20) unsigned NOT NULL COMMENT 'management server id derived from MAC address',
  `name` varchar(255) default NULL,
  `version` varchar(255) default NULL,
  `service_ip` varchar(15) NOT NULL,
  `service_port` int(11) NOT NULL,
  `last_update` datetime default NULL COMMENT 'Last record update time',
  `removed` datetime default NULL COMMENT 'date removed if not null',
  `alert_count` int(11) NOT NULL default '0',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `msid` (`msid`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `mshost`
--

LOCK TABLES `mshost` WRITE;
/*!40000 ALTER TABLE `mshost` DISABLE KEYS */;
INSERT INTO `mshost` VALUES (1,6602001285584,'ezh-sit-cs1.vmopsdev.net','2.2.2','127.0.0.1',9090,'2011-05-16 22:25:33',NULL,0);
/*!40000 ALTER TABLE `mshost` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `network_offerings`
--

DROP TABLE IF EXISTS `network_offerings`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `network_offerings` (
  `id` bigint(20) unsigned NOT NULL auto_increment COMMENT 'id',
  `name` varchar(64) NOT NULL COMMENT 'network offering',
  `display_text` varchar(255) NOT NULL COMMENT 'text to display to users',
  `nw_rate` smallint(5) unsigned default NULL COMMENT 'network rate throttle mbits/s',
  `mc_rate` smallint(5) unsigned default NULL COMMENT 'mcast rate throttle mbits/s',
  `concurrent_connections` int(10) unsigned default NULL COMMENT 'concurrent connections supported on this network',
  `traffic_type` varchar(32) NOT NULL COMMENT 'traffic type carried on this network',
  `tags` varchar(4096) default NULL COMMENT 'tags supported by this offering',
  `system_only` int(1) unsigned NOT NULL default '0' COMMENT 'Is this network offering for system use only',
  `specify_vlan` int(1) unsigned NOT NULL default '0' COMMENT 'Should the user specify vlan',
  `service_offering_id` bigint(20) unsigned default NULL COMMENT 'service offering id that this network offering is tied to',
  `created` datetime NOT NULL COMMENT 'time the entry was created',
  `removed` datetime default NULL COMMENT 'time the entry was removed',
  `default` int(1) unsigned NOT NULL default '0' COMMENT '1 if network offering is default',
  `availability` varchar(255) NOT NULL COMMENT 'availability of the network',
  `dns_service` int(1) unsigned NOT NULL default '0' COMMENT 'true if network offering provides dns service',
  `gateway_service` int(1) unsigned NOT NULL default '0' COMMENT 'true if network offering provides gateway service',
  `firewall_service` int(1) unsigned NOT NULL default '0' COMMENT 'true if network offering provides firewall service',
  `lb_service` int(1) unsigned NOT NULL default '0' COMMENT 'true if network offering provides lb service',
  `userdata_service` int(1) unsigned NOT NULL default '0' COMMENT 'true if network offering provides user data service',
  `vpn_service` int(1) unsigned NOT NULL default '0' COMMENT 'true if network offering provides vpn service',
  `dhcp_service` int(1) unsigned NOT NULL default '0' COMMENT 'true if network offering provides dhcp service',
  `guest_type` char(32) NOT NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`),
  UNIQUE KEY `name` (`name`),
  UNIQUE KEY `service_offering_id` (`service_offering_id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `network_offerings`
--

LOCK TABLES `network_offerings` WRITE;
/*!40000 ALTER TABLE `network_offerings` DISABLE KEYS */;
INSERT INTO `network_offerings` VALUES (1,'System-Public-Network','System Offering for System-Public-Network',NULL,NULL,NULL,'Public',NULL,1,0,NULL,'2011-02-10 05:28:33',NULL,0,'Required',0,0,0,0,0,0,0,''),(2,'System-Management-Network','System Offering for System-Management-Network',NULL,NULL,NULL,'Management',NULL,1,0,NULL,'2011-02-10 05:28:33',NULL,0,'Required',0,0,0,0,0,0,0,''),(3,'System-Control-Network','System Offering for System-Control-Network',NULL,NULL,NULL,'Control',NULL,1,0,NULL,'2011-02-10 05:28:33',NULL,0,'Required',0,0,0,0,0,0,0,''),(4,'System-Storage-Network','System Offering for System-Storage-Network',NULL,NULL,NULL,'Storage',NULL,1,0,NULL,'2011-02-10 05:28:33',NULL,0,'Required',0,0,0,0,0,0,0,''),(5,'System-Guest-Network','System Offering for System-Guest-Network',NULL,NULL,NULL,'Guest',NULL,1,0,NULL,'2011-02-10 05:28:33',NULL,0,'Required',0,0,0,0,0,0,0,'Direct'),(6,'DefaultVirtualizedNetworkOffering','Virtual Vlan',NULL,NULL,NULL,'Guest',NULL,0,0,NULL,'2011-02-10 05:28:33',NULL,1,'Required',1,1,1,1,1,1,1,'Virtual'),(7,'DefaultDirectNetworkOffering','Direct',NULL,NULL,NULL,'Guest',NULL,0,1,NULL,'2011-02-10 05:28:33',NULL,1,'Optional',1,0,0,0,1,0,1,'Direct');
/*!40000 ALTER TABLE `network_offerings` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `network_rule_config`
--

DROP TABLE IF EXISTS `network_rule_config`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `network_rule_config` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `security_group_id` bigint(20) unsigned NOT NULL,
  `public_port` varchar(10) default NULL,
  `private_port` varchar(10) default NULL,
  `protocol` varchar(16) NOT NULL default 'TCP',
  `create_status` varchar(32) default NULL COMMENT 'rule creation status',
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `network_rule_config`
--

LOCK TABLES `network_rule_config` WRITE;
/*!40000 ALTER TABLE `network_rule_config` DISABLE KEYS */;
/*!40000 ALTER TABLE `network_rule_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `networks`
--

DROP TABLE IF EXISTS `networks`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `networks` (
  `id` bigint(20) unsigned NOT NULL auto_increment COMMENT 'id',
  `name` varchar(255) default NULL COMMENT 'name for this network',
  `display_text` varchar(255) default NULL COMMENT 'display text for this network',
  `traffic_type` varchar(32) NOT NULL COMMENT 'type of traffic going through this network',
  `broadcast_domain_type` varchar(32) NOT NULL COMMENT 'type of broadcast domain used',
  `broadcast_uri` varchar(255) default NULL COMMENT 'broadcast domain specifier',
  `gateway` varchar(15) default NULL COMMENT 'gateway for this network configuration',
  `cidr` varchar(18) default NULL COMMENT 'network cidr',
  `mode` varchar(32) default NULL COMMENT 'How to retrieve ip address in this network',
  `network_offering_id` bigint(20) unsigned NOT NULL COMMENT 'network offering id that this configuration is created from',
  `data_center_id` bigint(20) unsigned NOT NULL COMMENT 'data center id that this configuration is used in',
  `guru_name` varchar(255) NOT NULL COMMENT 'who is responsible for this type of network configuration',
  `state` varchar(32) NOT NULL COMMENT 'what state is this configuration in',
  `related` bigint(20) unsigned NOT NULL COMMENT 'related to what other network configuration',
  `domain_id` bigint(20) unsigned NOT NULL COMMENT 'foreign key to domain id',
  `account_id` bigint(20) unsigned NOT NULL COMMENT 'owner of this network',
  `dns1` varchar(255) default NULL COMMENT 'comma separated DNS list',
  `dns2` varchar(255) default NULL COMMENT 'comma separated DNS list',
  `guru_data` varchar(1024) default NULL COMMENT 'data stored by the network guru that setup this network',
  `set_fields` bigint(20) unsigned NOT NULL default '0' COMMENT 'which fields are set already',
  `guest_type` char(32) default NULL COMMENT 'type of guest network',
  `shared` int(1) unsigned NOT NULL default '0' COMMENT '0 if network is shared, 1 if network dedicated',
  `network_domain` varchar(255) default NULL COMMENT 'domain',
  `reservation_id` char(40) default NULL COMMENT 'reservation id',
  `is_default` int(1) unsigned NOT NULL default '0' COMMENT '1 if network is default',
  `created` datetime NOT NULL COMMENT 'date created',
  `removed` datetime default NULL COMMENT 'date removed if not null',
  `is_security_group_enabled` tinyint(4) NOT NULL default '0' COMMENT '1: enabled, 0: not',
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=225 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `networks`
--

LOCK TABLES `networks` WRITE;
/*!40000 ALTER TABLE `networks` DISABLE KEYS */;
INSERT INTO `networks` VALUES (200,NULL,NULL,'Public','Vlan',NULL,NULL,NULL,'Static',1,1,'PublicNetworkGuru','Setup',200,1,1,NULL,NULL,NULL,0,NULL,1,NULL,NULL,0,'2011-02-10 06:53:28',NULL,0),(201,NULL,NULL,'Management','Native',NULL,NULL,NULL,'Static',2,1,'PodBasedNetworkGuru','Setup',201,1,1,NULL,NULL,NULL,0,NULL,1,NULL,NULL,0,'2011-02-10 06:53:28',NULL,0),(202,NULL,NULL,'Control','LinkLocal',NULL,'169.254.0.1','169.254.0.0/16','Static',3,1,'ControlNetworkGuru','Setup',202,1,1,NULL,NULL,NULL,0,NULL,1,NULL,NULL,0,'2011-02-10 06:53:28',NULL,0),(203,NULL,NULL,'Storage','Native',NULL,NULL,NULL,'Static',4,1,'PodBasedNetworkGuru','Setup',203,1,1,NULL,NULL,NULL,0,NULL,1,NULL,NULL,0,'2011-02-10 06:53:28',NULL,0),(204,'Virtual Network','A dedicated virtualized network for your account.  The broadcast domain is contained within a VLAN and all public network access is routed out by a virtual router.','Guest','Vlan',NULL,'10.1.1.1','10.1.1.0/24','Dhcp',6,1,'ExternalGuestNetworkGuru','Allocated',204,4,4,NULL,NULL,NULL,0,'Virtual',0,'cs4cloud.internal','bd648766-5cc5-4482-8a77-3b65b86f3ae7',1,'2011-02-10 08:42:57',NULL,0),(205,'Virtual Network','A dedicated virtualized network for your account.  The broadcast domain is contained within a VLAN and all public network access is routed out by a virtual router.','Guest','Vlan',NULL,'10.1.1.1','10.1.1.0/24','Dhcp',6,1,'ExternalGuestNetworkGuru','Allocated',205,1,2,'192.168.10.254','',NULL,0,'Virtual',0,'cs2cloud.internal','d1e95915-292e-45ea-9502-2a058cb78fda',1,'2011-02-10 08:46:25',NULL,0),(206,'Virtual Network','A dedicated virtualized network for your account.  The broadcast domain is contained within a VLAN and all public network access is routed out by a virtual router.','Guest','Vlan',NULL,'10.1.1.1','10.1.1.0/24','Dhcp',6,1,'ExternalGuestNetworkGuru','Allocated',206,7,6,NULL,NULL,NULL,0,'Virtual',0,'cs6cloud.internal','51e6618f-df2a-4d0d-91ee-4ed2b05589c5',1,'2011-02-10 10:49:41',NULL,0),(207,'Virtual Network','A dedicated virtualized network for your account.  The broadcast domain is contained within a VLAN and all public network access is routed out by a virtual router.','Guest','Vlan',NULL,'10.1.1.1','10.1.1.0/24','Dhcp',6,1,'ExternalGuestNetworkGuru','Allocated',207,22,24,'192.168.10.254',NULL,NULL,0,'Virtual',0,'cs18cloud.internal','7027b513-80ff-437d-8499-6231d116c7ae',1,'2011-02-17 14:49:43',NULL,0),(208,'Virtual Network','A dedicated virtualized network for your account.  The broadcast domain is contained within a VLAN and all public network access is routed out by a virtual router.','Guest','Vlan',NULL,'10.1.1.1','10.1.1.0/24','Dhcp',6,1,'ExternalGuestNetworkGuru','Allocated',208,21,22,'192.168.10.254',NULL,NULL,0,'Virtual',0,'cs16cloud.internal','61869ade-01a3-4abc-b9d3-42c9a0b3a819',1,'2011-02-17 14:53:20',NULL,0),(209,'Virtual Network','A dedicated virtualized network for your account.  The broadcast domain is contained within a VLAN and all public network access is routed out by a virtual router.','Guest','Vlan',NULL,'10.1.1.1','10.1.1.0/24','Dhcp',6,1,'ExternalGuestNetworkGuru','Destroy',209,24,26,NULL,NULL,NULL,0,'Virtual',0,'cs1acloud.internal','dbc73bab-8150-41d4-8d5a-7e1501cf265c',1,'2011-02-23 09:00:50','2011-02-23 09:40:38',0),(210,'Virtual Network','A dedicated virtualized network for your account.  The broadcast domain is contained within a VLAN and all public network access is routed out by a virtual router.','Guest','Vlan',NULL,'10.1.1.1','10.1.1.0/24','Dhcp',6,1,'ExternalGuestNetworkGuru','Destroy',210,27,30,NULL,NULL,NULL,0,'Virtual',0,'cs1ecloud.internal','8b15cec0-dc27-4de5-92ce-dea585fd0b46',1,'2011-02-23 10:19:49','2011-02-23 10:38:29',0),(211,'Virtual Network','A dedicated virtualized network for your account.  The broadcast domain is contained within a VLAN and all public network access is routed out by a virtual router.','Guest','Vlan',NULL,'10.1.1.1','10.1.1.0/24','Dhcp',6,1,'ExternalGuestNetworkGuru','Allocated',211,20,20,'192.168.10.254',NULL,NULL,0,'Virtual',0,'cs14cloud.internal','abae43fd-7c0c-4db3-9daa-01f2c9401f79',1,'2011-03-09 10:51:22',NULL,0),(212,'Virtual Network','A dedicated virtualized network for your account.  The broadcast domain is contained within a VLAN and all public network access is routed out by a virtual router.','Guest','Vlan',NULL,'10.1.1.1','10.1.1.0/24','Dhcp',6,1,'ExternalGuestNetworkGuru','Allocated',212,3,3,NULL,NULL,NULL,0,'Virtual',0,'cs3cloud.internal',NULL,1,'2011-03-10 00:27:14',NULL,0),(213,'Virtual Network','A dedicated virtualized network for your account.  The broadcast domain is contained within a VLAN and all public network access is routed out by a virtual router.','Guest','Vlan',NULL,'10.1.1.1','10.1.1.0/24','Dhcp',6,1,'ExternalGuestNetworkGuru','Allocated',213,39,42,NULL,NULL,NULL,0,'Virtual',0,'cs2acloud.internal',NULL,1,'2011-03-29 07:55:22',NULL,0),(214,'Virtual Network','A dedicated virtualized network for your account.  The broadcast domain is contained within a VLAN and all public network access is routed out by a virtual router.','Guest','Vlan',NULL,'10.1.1.1','10.1.1.0/24','Dhcp',6,1,'ExternalGuestNetworkGuru','Allocated',214,45,51,NULL,NULL,NULL,0,'Virtual',0,'cs33cloud.internal',NULL,1,'2011-03-29 12:19:09',NULL,0),(215,'Virtual Network','A dedicated virtualized network for your account.  The broadcast domain is contained within a VLAN and all public network access is routed out by a virtual router.','Guest','Vlan',NULL,'10.1.1.1','10.1.1.0/24','Dhcp',6,1,'ExternalGuestNetworkGuru','Allocated',215,48,56,'192.168.10.254','',NULL,0,'Virtual',0,'cs38cloud.internal','afcd739e-bd61-4c86-8a79-7d1e5b9b541b',1,'2011-04-09 10:25:42',NULL,0),(216,'Virtual Network','A dedicated virtualized network for your account.  The broadcast domain is contained within a VLAN and all public network access is routed out by a virtual router.','Guest','Vlan',NULL,'10.1.1.1','10.1.1.0/24','Dhcp',6,1,'ExternalGuestNetworkGuru','Allocated',216,52,61,NULL,NULL,NULL,0,'Virtual',0,'cs3dcloud.internal',NULL,1,'2011-04-25 12:53:14',NULL,0),(217,'Virtual Network','A dedicated virtualized network for your account.  The broadcast domain is contained within a VLAN and all public network access is routed out by a virtual router.','Guest','Vlan',NULL,'10.1.1.1','10.1.1.0/24','Dhcp',6,1,'ExternalGuestNetworkGuru','Allocated',217,52,60,NULL,NULL,NULL,0,'Virtual',0,'cs3ccloud.internal',NULL,1,'2011-04-25 12:56:50',NULL,0),(218,NULL,NULL,'Public','Vlan',NULL,NULL,NULL,'Static',1,2,'PublicNetworkGuru','Setup',218,1,1,NULL,NULL,NULL,0,NULL,1,NULL,NULL,0,'2011-05-06 22:02:13',NULL,0),(219,NULL,NULL,'Management','Native',NULL,NULL,NULL,'Static',2,2,'PodBasedNetworkGuru','Setup',219,1,1,NULL,NULL,NULL,0,NULL,1,NULL,NULL,0,'2011-05-06 22:02:14',NULL,0),(220,NULL,NULL,'Control','LinkLocal',NULL,'169.254.0.1','169.254.0.0/16','Static',3,2,'ControlNetworkGuru','Setup',220,1,1,NULL,NULL,NULL,0,NULL,1,NULL,NULL,0,'2011-05-06 22:02:14',NULL,0),(221,NULL,NULL,'Storage','Native',NULL,NULL,NULL,'Static',4,2,'PodBasedNetworkGuru','Setup',221,1,1,NULL,NULL,NULL,0,NULL,1,NULL,NULL,0,'2011-05-06 22:02:14',NULL,0),(222,'Virtual Network','A dedicated virtualized network for your account.  The broadcast domain is contained within a VLAN and all public network access is routed out by a virtual router.','Guest','Vlan',NULL,'10.1.3.1','10.1.3.0/24','Dhcp',6,2,'ExternalGuestNetworkGuru','Allocated',222,1,2,'72.52.126.11','72.52.126.12',NULL,0,'Virtual',0,'cs2cloud.internal','fc53c04e-0e7c-4129-9dab-a9d7a07f5e96',1,'2011-05-11 21:43:25',NULL,0),(223,'Virtual Network','A dedicated virtualized network for your account.  The broadcast domain is contained within a VLAN and all public network access is routed out by a virtual router.','Guest','Vlan',NULL,'10.1.3.1','10.1.3.0/24','Dhcp',6,2,'ExternalGuestNetworkGuru','Destroy',223,59,69,'72.52.126.11','72.52.126.12',NULL,0,'Virtual',0,'cs45cloud.internal','e0a385ee-0511-42d6-8bba-aaf2b218bca5',1,'2011-05-12 00:20:22','2011-05-16 17:52:56',0),(224,'Virtual Network','A dedicated virtualized network for your account.  The broadcast domain is contained within a VLAN and all public network access is routed out by a virtual router.','Guest','Vlan','vlan://1128','10.1.3.1','10.1.3.0/24','Dhcp',6,2,'ExternalGuestNetworkGuru','Implemented',224,60,70,NULL,NULL,NULL,0,'Virtual',0,'cs46cloud.internal','2f2ad287-f40f-4089-bd34-009f41ed51d8',1,'2011-05-12 18:48:41',NULL,0);
/*!40000 ALTER TABLE `networks` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `nics`
--

DROP TABLE IF EXISTS `nics`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `nics` (
  `id` bigint(20) unsigned NOT NULL auto_increment COMMENT 'id',
  `instance_id` bigint(20) unsigned default NULL COMMENT 'vm instance id',
  `mac_address` varchar(17) default NULL COMMENT 'mac address',
  `ip4_address` varchar(15) default NULL COMMENT 'ip4 address',
  `netmask` varchar(15) default NULL COMMENT 'netmask for ip4 address',
  `gateway` varchar(15) default NULL COMMENT 'gateway',
  `ip_type` varchar(32) default NULL COMMENT 'type of ip',
  `broadcast_uri` varchar(255) default NULL COMMENT 'broadcast uri',
  `network_id` bigint(20) unsigned NOT NULL COMMENT 'network configuration id',
  `mode` varchar(32) default NULL COMMENT 'mode of getting ip address',
  `state` varchar(32) NOT NULL COMMENT 'state of the creation',
  `strategy` varchar(32) NOT NULL COMMENT 'reservation strategy',
  `reserver_name` varchar(255) default NULL COMMENT 'Name of the component that reserved the ip address',
  `reservation_id` varchar(64) default NULL COMMENT 'id for the reservation',
  `device_id` int(10) default NULL COMMENT 'device id for the network when plugged into the virtual machine',
  `update_time` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP COMMENT 'time the state was changed',
  `isolation_uri` varchar(255) default NULL COMMENT 'id for isolation',
  `ip6_address` varchar(32) default NULL COMMENT 'ip6 address',
  `default_nic` tinyint(4) NOT NULL COMMENT 'None',
  `created` datetime NOT NULL COMMENT 'date created',
  `removed` datetime default NULL COMMENT 'date removed if not null',
  `vm_type` char(32) default NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`),
  KEY `fk_nics__instance_id` (`instance_id`),
  KEY `fk_nics__networks_id` (`network_id`),
  CONSTRAINT `fk_nics__instance_id` FOREIGN KEY (`instance_id`) REFERENCES `vm_instance` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_nics__networks_id` FOREIGN KEY (`network_id`) REFERENCES `networks` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=77 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `nics`
--

LOCK TABLES `nics` WRITE;
/*!40000 ALTER TABLE `nics` DISABLE KEYS */;
INSERT INTO `nics` VALUES (8,4,'06:a3:b2:00:00:0f','66.149.231.112','255.255.255.0','66.149.231.1','Ip4','vlan://66',200,'Static','Allocated','Create','PublicNetworkGuru',NULL,2,'2011-03-10 23:53:10','vlan://66',NULL,1,'2011-02-10 08:43:53',NULL,'DomainRouter'),(9,4,'02:00:62:29:00:02','10.1.1.1','255.255.255.0',NULL,NULL,NULL,204,'Dhcp','Allocated','Start','ExternalGuestNetworkGuru','d8949de3-cac2-4ee1-bd99-e3c0acb703bb',0,'2011-03-10 23:53:10',NULL,NULL,0,'2011-02-10 08:43:53',NULL,'DomainRouter'),(10,4,NULL,NULL,NULL,NULL,NULL,NULL,202,NULL,'Allocated','Start','ControlNetworkGuru','d8949de3-cac2-4ee1-bd99-e3c0acb703bb',1,'2011-03-10 23:53:10',NULL,NULL,0,'2011-02-10 08:43:53',NULL,'DomainRouter'),(12,6,'06:7f:90:00:00:0e','66.149.231.111','255.255.255.0','66.149.231.1','Ip4','vlan://66',200,'Static','Allocated','Create','PublicNetworkGuru',NULL,2,'2011-05-06 22:19:40','vlan://66',NULL,1,'2011-02-10 08:46:43',NULL,'DomainRouter'),(13,6,'02:00:78:34:00:02','10.1.1.1','255.255.255.0',NULL,NULL,NULL,205,'Dhcp','Allocated','Start','ExternalGuestNetworkGuru','64ae6fc3-08b2-473a-a018-3433e163d8d2',0,'2011-05-06 22:19:40',NULL,NULL,0,'2011-02-10 08:46:43',NULL,'DomainRouter'),(14,6,NULL,NULL,NULL,NULL,NULL,NULL,202,NULL,'Allocated','Start','ControlNetworkGuru','64ae6fc3-08b2-473a-a018-3433e163d8d2',1,'2011-05-06 22:19:40',NULL,NULL,0,'2011-02-10 08:46:43',NULL,'DomainRouter'),(18,10,'06:7a:84:00:00:06','66.149.231.103','255.255.255.0','66.149.231.1','Ip4','vlan://66',200,'Static','Allocated','Create','PublicNetworkGuru',NULL,2,'2011-03-10 23:53:10','vlan://66',NULL,1,'2011-02-10 10:51:24',NULL,'DomainRouter'),(19,10,'02:00:6e:a0:00:02','10.1.1.1','255.255.255.0',NULL,NULL,NULL,206,'Dhcp','Allocated','Start','ExternalGuestNetworkGuru','18756493-d799-43e2-8ecd-94de52bda6f9',0,'2011-03-10 23:53:10',NULL,NULL,0,'2011-02-10 10:51:24',NULL,'DomainRouter'),(20,10,NULL,NULL,NULL,NULL,NULL,NULL,202,NULL,'Allocated','Start','ControlNetworkGuru','18756493-d799-43e2-8ecd-94de52bda6f9',1,'2011-03-10 23:53:10',NULL,NULL,0,'2011-02-10 10:51:24',NULL,'DomainRouter'),(23,13,'06:c0:96:00:00:09','66.149.231.106','255.255.255.0','66.149.231.1','Ip4','vlan://66',200,'Static','Allocated','Create','PublicNetworkGuru',NULL,2,'2011-04-09 10:42:11','vlan://66',NULL,1,'2011-02-17 14:51:25',NULL,'DomainRouter'),(24,13,'02:00:57:b8:00:02','10.1.1.1','255.255.255.0',NULL,NULL,NULL,207,'Dhcp','Allocated','Start','ExternalGuestNetworkGuru','11561ebb-8873-45a6-a870-f08c5134d080',0,'2011-04-09 10:42:11',NULL,NULL,0,'2011-02-17 14:51:25',NULL,'DomainRouter'),(25,13,NULL,NULL,NULL,NULL,NULL,NULL,202,NULL,'Allocated','Start','ControlNetworkGuru','11561ebb-8873-45a6-a870-f08c5134d080',1,'2011-04-09 10:42:11',NULL,NULL,0,'2011-02-17 14:51:25',NULL,'DomainRouter'),(27,15,'06:28:22:00:00:0a','66.149.231.107','255.255.255.0','66.149.231.1','Ip4','vlan://66',200,'Static','Allocated','Create','PublicNetworkGuru',NULL,2,'2011-04-09 10:32:01','vlan://66',NULL,1,'2011-02-17 14:55:10',NULL,'DomainRouter'),(28,15,'02:00:13:41:00:02','10.1.1.1','255.255.255.0',NULL,NULL,NULL,208,'Dhcp','Allocated','Start','ExternalGuestNetworkGuru','2fe5170e-91c9-4df8-8252-dc0123553253',0,'2011-04-09 10:32:01',NULL,NULL,0,'2011-02-17 14:55:10',NULL,'DomainRouter'),(29,15,NULL,NULL,NULL,NULL,NULL,NULL,202,NULL,'Allocated','Start','ControlNetworkGuru','2fe5170e-91c9-4df8-8252-dc0123553253',1,'2011-04-09 10:32:01',NULL,NULL,0,'2011-02-17 14:55:10',NULL,'DomainRouter'),(30,16,'02:00:28:e4:00:04','10.1.1.186','255.255.255.0','10.1.1.1',NULL,NULL,205,NULL,'Allocated','Start','ExternalGuestNetworkGuru','d1e95915-292e-45ea-9502-2a058cb78fda',0,'2011-05-11 21:59:31',NULL,NULL,1,'2011-02-21 04:49:43',NULL,'User'),(40,22,'06:c4:9a:00:00:13','66.149.231.116','255.255.255.0','66.149.231.1','Ip4','vlan://66',200,'Static','Allocated','Create','PublicNetworkGuru',NULL,2,'2011-04-09 10:42:22','vlan://66',NULL,1,'2011-03-09 10:51:46',NULL,'DomainRouter'),(41,22,'02:00:35:8e:00:02','10.1.1.1','255.255.255.0',NULL,NULL,NULL,211,'Dhcp','Allocated','Start','ExternalGuestNetworkGuru','4b114ca8-89ee-4d86-b6f2-4de043580945',0,'2011-04-09 10:42:22',NULL,NULL,0,'2011-03-09 10:51:46',NULL,'DomainRouter'),(42,22,NULL,NULL,NULL,NULL,NULL,NULL,202,NULL,'Allocated','Start','ControlNetworkGuru','4b114ca8-89ee-4d86-b6f2-4de043580945',1,'2011-04-09 10:42:22',NULL,NULL,0,'2011-03-09 10:51:46',NULL,'DomainRouter'),(55,35,'02:00:61:c6:00:01','10.1.1.79','255.255.255.0','10.1.1.1',NULL,NULL,215,NULL,'Allocated','Start','ExternalGuestNetworkGuru','afcd739e-bd61-4c86-8a79-7d1e5b9b541b',0,'2011-05-11 21:59:31',NULL,NULL,1,'2011-04-09 10:26:38',NULL,'User'),(56,36,'06:28:46:00:00:04','66.149.231.101','255.255.255.0','66.149.231.1','Ip4','vlan://66',200,'Static','Allocated','Create','PublicNetworkGuru',NULL,2,'2011-05-11 21:59:31','vlan://66',NULL,1,'2011-04-09 10:26:50',NULL,'DomainRouter'),(57,36,'02:00:31:8b:00:02','10.1.1.1','255.255.255.0',NULL,NULL,NULL,215,'Dhcp','Allocated','Start','ExternalGuestNetworkGuru','2ec1fc12-1308-493d-8039-45bdd23966e2',0,'2011-05-11 21:59:32',NULL,NULL,0,'2011-04-09 10:26:50',NULL,'DomainRouter'),(58,36,NULL,NULL,NULL,NULL,NULL,NULL,202,NULL,'Allocated','Start','ControlNetworkGuru','2ec1fc12-1308-493d-8039-45bdd23966e2',1,'2011-05-11 21:59:32',NULL,NULL,0,'2011-04-09 10:26:50',NULL,'DomainRouter'),(59,40,'06:d0:be:00:00:0d','172.16.64.47','255.255.252.0','172.16.64.1','Ip4','vlan://516',218,'Static','Reserved','Create','PublicNetworkGuru',NULL,2,'2011-05-10 18:29:34','vlan://516',NULL,1,'2011-05-06 22:06:57',NULL,'ConsoleProxy'),(60,40,'0e:00:a9:fe:01:82','169.254.1.130','255.255.0.0','169.254.0.1','Ip4',NULL,220,NULL,'Reserved','Start','ControlNetworkGuru','c4dac56f-e6d8-48b7-b83d-29162b48bc33',0,'2011-05-10 18:29:34',NULL,NULL,0,'2011-05-06 22:06:57',NULL,'ConsoleProxy'),(61,40,'06:22:d6:00:00:03','192.168.181.237','255.255.255.0','192.168.181.1','Ip4',NULL,219,NULL,'Reserved','Start','PodBasedNetworkGuru','c4dac56f-e6d8-48b7-b83d-29162b48bc33',1,'2011-05-10 18:29:34',NULL,NULL,0,'2011-05-06 22:06:57',NULL,'ConsoleProxy'),(62,41,'06:f2:2e:00:00:0e','172.16.64.48','255.255.252.0','172.16.64.1','Ip4','vlan://516',218,'Static','Reserved','Create','PublicNetworkGuru',NULL,2,'2011-05-10 18:40:10','vlan://516',NULL,1,'2011-05-06 22:06:57',NULL,'SecondaryStorageVm'),(63,41,'0e:00:a9:fe:00:ba','169.254.0.186','255.255.0.0','169.254.0.1','Ip4',NULL,220,NULL,'Reserved','Start','ControlNetworkGuru','7400955e-f6c3-4482-b521-d2ac66ef7e99',0,'2011-05-10 18:40:10',NULL,NULL,0,'2011-05-06 22:06:57',NULL,'SecondaryStorageVm'),(64,41,'06:8e:64:00:00:04','192.168.181.238','255.255.255.0','192.168.181.1','Ip4',NULL,219,NULL,'Reserved','Start','PodBasedNetworkGuru','7400955e-f6c3-4482-b521-d2ac66ef7e99',1,'2011-05-10 18:40:10',NULL,NULL,0,'2011-05-06 22:06:58',NULL,'SecondaryStorageVm'),(65,42,'02:00:68:76:00:01','10.1.3.145','255.255.255.0','10.1.3.1',NULL,NULL,222,NULL,'Allocated','Start','ExternalGuestNetworkGuru','fc53c04e-0e7c-4129-9dab-a9d7a07f5e96',0,'2011-05-11 21:46:25',NULL,NULL,1,'2011-05-11 21:43:38',NULL,'User'),(66,43,'06:79:82:00:00:09','172.16.64.43','255.255.252.0','172.16.64.1','Ip4','vlan://516',218,'Static','Allocated','Create','PublicNetworkGuru',NULL,2,'2011-05-11 22:10:04','vlan://516',NULL,1,'2011-05-11 21:44:57',NULL,'DomainRouter'),(67,43,'02:00:45:94:00:02','10.1.3.1','255.255.255.0',NULL,NULL,NULL,222,'Dhcp','Allocated','Start','ExternalGuestNetworkGuru','b8526eca-5f4f-463c-805a-b2dbe572b14d',0,'2011-05-11 22:10:04',NULL,NULL,0,'2011-05-11 21:44:57',NULL,'DomainRouter'),(68,43,NULL,NULL,NULL,NULL,NULL,NULL,220,NULL,'Allocated','Start','ControlNetworkGuru','b8526eca-5f4f-463c-805a-b2dbe572b14d',1,'2011-05-11 22:10:04',NULL,NULL,0,'2011-05-11 21:44:57',NULL,'DomainRouter'),(73,46,'02:00:22:d4:00:01','10.1.3.47','255.255.255.0','10.1.3.1',NULL,'vlan://1128',224,NULL,'Reserved','Start','ExternalGuestNetworkGuru','2f2ad287-f40f-4089-bd34-009f41ed51d8',0,'2011-05-12 18:49:31','vlan://1128',NULL,1,'2011-05-12 18:48:49',NULL,'User'),(74,47,'06:23:ea:00:00:06','172.16.64.40','255.255.252.0','172.16.64.1','Ip4','vlan://516',218,'Static','Reserved','Create','PublicNetworkGuru',NULL,2,'2011-05-12 18:48:51','vlan://516',NULL,1,'2011-05-12 18:48:50',NULL,'DomainRouter'),(75,47,'02:00:7a:16:00:02','10.1.3.1','255.255.255.0',NULL,NULL,'vlan://1128',224,'Dhcp','Reserved','Start','ExternalGuestNetworkGuru','30ff8edd-f496-431c-94e3-5ffc8901cbc6',0,'2011-05-12 18:48:51','vlan://1128',NULL,0,'2011-05-12 18:48:50',NULL,'DomainRouter'),(76,47,'0e:00:a9:fe:00:19','169.254.0.25','255.255.0.0','169.254.0.1','Ip4',NULL,220,NULL,'Reserved','Start','ControlNetworkGuru','30ff8edd-f496-431c-94e3-5ffc8901cbc6',1,'2011-05-12 18:48:51',NULL,NULL,0,'2011-05-12 18:48:50',NULL,'DomainRouter');
/*!40000 ALTER TABLE `nics` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `op_dc_ip_address_alloc`
--

DROP TABLE IF EXISTS `op_dc_ip_address_alloc`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `op_dc_ip_address_alloc` (
  `id` bigint(20) unsigned NOT NULL auto_increment COMMENT 'primary key',
  `ip_address` varchar(15) NOT NULL COMMENT 'ip address',
  `data_center_id` bigint(20) unsigned NOT NULL COMMENT 'data center it belongs to',
  `pod_id` bigint(20) unsigned NOT NULL COMMENT 'pod it belongs to',
  `instance_id` bigint(20) unsigned default NULL COMMENT 'instance id',
  `reservation_id` char(40) default NULL COMMENT 'reservation id',
  `taken` datetime default NULL COMMENT 'Date taken',
  `mac_address` bigint(20) unsigned NOT NULL COMMENT 'mac address for management ips',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `i_op_dc_ip_address_alloc__ip_address__data_center_id` (`ip_address`,`data_center_id`),
  KEY `i_op_dc_ip_address_alloc__pod_id__data_center_id__taken` (`pod_id`,`data_center_id`,`taken`,`instance_id`),
  KEY `i_op_dc_ip_address_alloc__pod_id` (`pod_id`),
  CONSTRAINT `fk_op_dc_ip_address_alloc__pod_id` FOREIGN KEY (`pod_id`) REFERENCES `host_pod_ref` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `op_dc_ip_address_alloc`
--

LOCK TABLES `op_dc_ip_address_alloc` WRITE;
/*!40000 ALTER TABLE `op_dc_ip_address_alloc` DISABLE KEYS */;
INSERT INTO `op_dc_ip_address_alloc` VALUES (1,'192.168.154.233',1,1,NULL,NULL,NULL,1),(2,'192.168.154.234',1,1,NULL,NULL,NULL,2),(3,'192.168.154.235',1,1,NULL,NULL,NULL,3),(4,'192.168.181.235',2,2,NULL,NULL,NULL,1),(5,'192.168.181.236',2,2,NULL,NULL,NULL,2),(6,'192.168.181.237',2,2,61,'c4dac56f-e6d8-48b7-b83d-29162b48bc33','2011-05-10 18:29:34',3),(7,'192.168.181.238',2,2,64,'7400955e-f6c3-4482-b521-d2ac66ef7e99','2011-05-10 18:40:10',4),(8,'192.168.181.239',2,2,NULL,NULL,NULL,5);
/*!40000 ALTER TABLE `op_dc_ip_address_alloc` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `op_dc_link_local_ip_address_alloc`
--

DROP TABLE IF EXISTS `op_dc_link_local_ip_address_alloc`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `op_dc_link_local_ip_address_alloc` (
  `id` bigint(20) unsigned NOT NULL auto_increment COMMENT 'primary key',
  `ip_address` varchar(15) NOT NULL COMMENT 'ip address',
  `data_center_id` bigint(20) unsigned NOT NULL COMMENT 'data center it belongs to',
  `pod_id` bigint(20) unsigned NOT NULL COMMENT 'pod it belongs to',
  `instance_id` bigint(20) unsigned default NULL COMMENT 'instance id',
  `reservation_id` char(40) default NULL COMMENT 'reservation id used to reserve this network',
  `taken` datetime default NULL COMMENT 'Date taken',
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2043 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `op_dc_link_local_ip_address_alloc`
--

LOCK TABLES `op_dc_link_local_ip_address_alloc` WRITE;
/*!40000 ALTER TABLE `op_dc_link_local_ip_address_alloc` DISABLE KEYS */;
INSERT INTO `op_dc_link_local_ip_address_alloc` VALUES (1,'169.254.0.2',1,1,NULL,NULL,NULL),(2,'169.254.0.3',1,1,NULL,NULL,NULL),(3,'169.254.0.4',1,1,NULL,NULL,NULL),(4,'169.254.0.5',1,1,NULL,NULL,NULL),(5,'169.254.0.6',1,1,NULL,NULL,NULL),(6,'169.254.0.7',1,1,NULL,NULL,NULL),(7,'169.254.0.8',1,1,NULL,NULL,NULL),(8,'169.254.0.9',1,1,NULL,NULL,NULL),(9,'169.254.0.10',1,1,NULL,NULL,NULL),(10,'169.254.0.11',1,1,NULL,NULL,NULL),(11,'169.254.0.12',1,1,NULL,NULL,NULL),(12,'169.254.0.13',1,1,NULL,NULL,NULL),(13,'169.254.0.14',1,1,NULL,NULL,NULL),(14,'169.254.0.15',1,1,NULL,NULL,NULL),(15,'169.254.0.16',1,1,NULL,NULL,NULL),(16,'169.254.0.17',1,1,NULL,NULL,NULL),(17,'169.254.0.18',1,1,NULL,NULL,NULL),(18,'169.254.0.19',1,1,NULL,NULL,NULL),(19,'169.254.0.20',1,1,NULL,NULL,NULL),(20,'169.254.0.21',1,1,NULL,NULL,NULL),(21,'169.254.0.22',1,1,NULL,NULL,NULL),(22,'169.254.0.23',1,1,NULL,NULL,NULL),(23,'169.254.0.24',1,1,NULL,NULL,NULL),(24,'169.254.0.25',1,1,NULL,NULL,NULL),(25,'169.254.0.26',1,1,NULL,NULL,NULL),(26,'169.254.0.27',1,1,NULL,NULL,NULL),(27,'169.254.0.28',1,1,NULL,NULL,NULL),(28,'169.254.0.29',1,1,NULL,NULL,NULL),(29,'169.254.0.30',1,1,NULL,NULL,NULL),(30,'169.254.0.31',1,1,NULL,NULL,NULL),(31,'169.254.0.32',1,1,NULL,NULL,NULL),(32,'169.254.0.33',1,1,NULL,NULL,NULL),(33,'169.254.0.34',1,1,NULL,NULL,NULL),(34,'169.254.0.35',1,1,NULL,NULL,NULL),(35,'169.254.0.36',1,1,NULL,NULL,NULL),(36,'169.254.0.37',1,1,NULL,NULL,NULL),(37,'169.254.0.38',1,1,NULL,NULL,NULL),(38,'169.254.0.39',1,1,NULL,NULL,NULL),(39,'169.254.0.40',1,1,NULL,NULL,NULL),(40,'169.254.0.41',1,1,NULL,NULL,NULL),(41,'169.254.0.42',1,1,NULL,NULL,NULL),(42,'169.254.0.43',1,1,NULL,NULL,NULL),(43,'169.254.0.44',1,1,NULL,NULL,NULL),(44,'169.254.0.45',1,1,NULL,NULL,NULL),(45,'169.254.0.46',1,1,NULL,NULL,NULL),(46,'169.254.0.47',1,1,NULL,NULL,NULL),(47,'169.254.0.48',1,1,NULL,NULL,NULL),(48,'169.254.0.49',1,1,NULL,NULL,NULL),(49,'169.254.0.50',1,1,NULL,NULL,NULL),(50,'169.254.0.51',1,1,NULL,NULL,NULL),(51,'169.254.0.52',1,1,NULL,NULL,NULL),(52,'169.254.0.53',1,1,NULL,NULL,NULL),(53,'169.254.0.54',1,1,NULL,NULL,NULL),(54,'169.254.0.55',1,1,NULL,NULL,NULL),(55,'169.254.0.56',1,1,NULL,NULL,NULL),(56,'169.254.0.57',1,1,NULL,NULL,NULL),(57,'169.254.0.58',1,1,NULL,NULL,NULL),(58,'169.254.0.59',1,1,NULL,NULL,NULL),(59,'169.254.0.60',1,1,NULL,NULL,NULL),(60,'169.254.0.61',1,1,NULL,NULL,NULL),(61,'169.254.0.62',1,1,NULL,NULL,NULL),(62,'169.254.0.63',1,1,NULL,NULL,NULL),(63,'169.254.0.64',1,1,NULL,NULL,NULL),(64,'169.254.0.65',1,1,NULL,NULL,NULL),(65,'169.254.0.66',1,1,NULL,NULL,NULL),(66,'169.254.0.67',1,1,NULL,NULL,NULL),(67,'169.254.0.68',1,1,NULL,NULL,NULL),(68,'169.254.0.69',1,1,NULL,NULL,NULL),(69,'169.254.0.70',1,1,NULL,NULL,NULL),(70,'169.254.0.71',1,1,NULL,NULL,NULL),(71,'169.254.0.72',1,1,NULL,NULL,NULL),(72,'169.254.0.73',1,1,NULL,NULL,NULL),(73,'169.254.0.74',1,1,NULL,NULL,NULL),(74,'169.254.0.75',1,1,NULL,NULL,NULL),(75,'169.254.0.76',1,1,NULL,NULL,NULL),(76,'169.254.0.77',1,1,NULL,NULL,NULL),(77,'169.254.0.78',1,1,NULL,NULL,NULL),(78,'169.254.0.79',1,1,NULL,NULL,NULL),(79,'169.254.0.80',1,1,NULL,NULL,NULL),(80,'169.254.0.81',1,1,NULL,NULL,NULL),(81,'169.254.0.82',1,1,NULL,NULL,NULL),(82,'169.254.0.83',1,1,NULL,NULL,NULL),(83,'169.254.0.84',1,1,NULL,NULL,NULL),(84,'169.254.0.85',1,1,NULL,NULL,NULL),(85,'169.254.0.86',1,1,NULL,NULL,NULL),(86,'169.254.0.87',1,1,NULL,NULL,NULL),(87,'169.254.0.88',1,1,NULL,NULL,NULL),(88,'169.254.0.89',1,1,NULL,NULL,NULL),(89,'169.254.0.90',1,1,NULL,NULL,NULL),(90,'169.254.0.91',1,1,NULL,NULL,NULL),(91,'169.254.0.92',1,1,NULL,NULL,NULL),(92,'169.254.0.93',1,1,NULL,NULL,NULL),(93,'169.254.0.94',1,1,NULL,NULL,NULL),(94,'169.254.0.95',1,1,NULL,NULL,NULL),(95,'169.254.0.96',1,1,NULL,NULL,NULL),(96,'169.254.0.97',1,1,NULL,NULL,NULL),(97,'169.254.0.98',1,1,NULL,NULL,NULL),(98,'169.254.0.99',1,1,NULL,NULL,NULL),(99,'169.254.0.100',1,1,NULL,NULL,NULL),(100,'169.254.0.101',1,1,NULL,NULL,NULL),(101,'169.254.0.102',1,1,NULL,NULL,NULL),(102,'169.254.0.103',1,1,NULL,NULL,NULL),(103,'169.254.0.104',1,1,NULL,NULL,NULL),(104,'169.254.0.105',1,1,NULL,NULL,NULL),(105,'169.254.0.106',1,1,NULL,NULL,NULL),(106,'169.254.0.107',1,1,NULL,NULL,NULL),(107,'169.254.0.108',1,1,NULL,NULL,NULL),(108,'169.254.0.109',1,1,NULL,NULL,NULL),(109,'169.254.0.110',1,1,NULL,NULL,NULL),(110,'169.254.0.111',1,1,NULL,NULL,NULL),(111,'169.254.0.112',1,1,NULL,NULL,NULL),(112,'169.254.0.113',1,1,NULL,NULL,NULL),(113,'169.254.0.114',1,1,NULL,NULL,NULL),(114,'169.254.0.115',1,1,NULL,NULL,NULL),(115,'169.254.0.116',1,1,NULL,NULL,NULL),(116,'169.254.0.117',1,1,NULL,NULL,NULL),(117,'169.254.0.118',1,1,NULL,NULL,NULL),(118,'169.254.0.119',1,1,NULL,NULL,NULL),(119,'169.254.0.120',1,1,NULL,NULL,NULL),(120,'169.254.0.121',1,1,NULL,NULL,NULL),(121,'169.254.0.122',1,1,NULL,NULL,NULL),(122,'169.254.0.123',1,1,NULL,NULL,NULL),(123,'169.254.0.124',1,1,NULL,NULL,NULL),(124,'169.254.0.125',1,1,NULL,NULL,NULL),(125,'169.254.0.126',1,1,NULL,NULL,NULL),(126,'169.254.0.127',1,1,NULL,NULL,NULL),(127,'169.254.0.128',1,1,NULL,NULL,NULL),(128,'169.254.0.129',1,1,NULL,NULL,NULL),(129,'169.254.0.130',1,1,NULL,NULL,NULL),(130,'169.254.0.131',1,1,NULL,NULL,NULL),(131,'169.254.0.132',1,1,NULL,NULL,NULL),(132,'169.254.0.133',1,1,NULL,NULL,NULL),(133,'169.254.0.134',1,1,NULL,NULL,NULL),(134,'169.254.0.135',1,1,NULL,NULL,NULL),(135,'169.254.0.136',1,1,NULL,NULL,NULL),(136,'169.254.0.137',1,1,NULL,NULL,NULL),(137,'169.254.0.138',1,1,NULL,NULL,NULL),(138,'169.254.0.139',1,1,NULL,NULL,NULL),(139,'169.254.0.140',1,1,NULL,NULL,NULL),(140,'169.254.0.141',1,1,NULL,NULL,NULL),(141,'169.254.0.142',1,1,NULL,NULL,NULL),(142,'169.254.0.143',1,1,NULL,NULL,NULL),(143,'169.254.0.144',1,1,NULL,NULL,NULL),(144,'169.254.0.145',1,1,NULL,NULL,NULL),(145,'169.254.0.146',1,1,NULL,NULL,NULL),(146,'169.254.0.147',1,1,NULL,NULL,NULL),(147,'169.254.0.148',1,1,NULL,NULL,NULL),(148,'169.254.0.149',1,1,NULL,NULL,NULL),(149,'169.254.0.150',1,1,NULL,NULL,NULL),(150,'169.254.0.151',1,1,NULL,NULL,NULL),(151,'169.254.0.152',1,1,NULL,NULL,NULL),(152,'169.254.0.153',1,1,NULL,NULL,NULL),(153,'169.254.0.154',1,1,NULL,NULL,NULL),(154,'169.254.0.155',1,1,NULL,NULL,NULL),(155,'169.254.0.156',1,1,NULL,NULL,NULL),(156,'169.254.0.157',1,1,NULL,NULL,NULL),(157,'169.254.0.158',1,1,NULL,NULL,NULL),(158,'169.254.0.159',1,1,NULL,NULL,NULL),(159,'169.254.0.160',1,1,NULL,NULL,NULL),(160,'169.254.0.161',1,1,NULL,NULL,NULL),(161,'169.254.0.162',1,1,NULL,NULL,NULL),(162,'169.254.0.163',1,1,NULL,NULL,NULL),(163,'169.254.0.164',1,1,NULL,NULL,NULL),(164,'169.254.0.165',1,1,NULL,NULL,NULL),(165,'169.254.0.166',1,1,NULL,NULL,NULL),(166,'169.254.0.167',1,1,NULL,NULL,NULL),(167,'169.254.0.168',1,1,NULL,NULL,NULL),(168,'169.254.0.169',1,1,NULL,NULL,NULL),(169,'169.254.0.170',1,1,NULL,NULL,NULL),(170,'169.254.0.171',1,1,NULL,NULL,NULL),(171,'169.254.0.172',1,1,NULL,NULL,NULL),(172,'169.254.0.173',1,1,NULL,NULL,NULL),(173,'169.254.0.174',1,1,NULL,NULL,NULL),(174,'169.254.0.175',1,1,NULL,NULL,NULL),(175,'169.254.0.176',1,1,NULL,NULL,NULL),(176,'169.254.0.177',1,1,NULL,NULL,NULL),(177,'169.254.0.178',1,1,NULL,NULL,NULL),(178,'169.254.0.179',1,1,NULL,NULL,NULL),(179,'169.254.0.180',1,1,NULL,NULL,NULL),(180,'169.254.0.181',1,1,NULL,NULL,NULL),(181,'169.254.0.182',1,1,NULL,NULL,NULL),(182,'169.254.0.183',1,1,NULL,NULL,NULL),(183,'169.254.0.184',1,1,NULL,NULL,NULL),(184,'169.254.0.185',1,1,NULL,NULL,NULL),(185,'169.254.0.186',1,1,NULL,NULL,NULL),(186,'169.254.0.187',1,1,NULL,NULL,NULL),(187,'169.254.0.188',1,1,NULL,NULL,NULL),(188,'169.254.0.189',1,1,NULL,NULL,NULL),(189,'169.254.0.190',1,1,NULL,NULL,NULL),(190,'169.254.0.191',1,1,NULL,NULL,NULL),(191,'169.254.0.192',1,1,NULL,NULL,NULL),(192,'169.254.0.193',1,1,NULL,NULL,NULL),(193,'169.254.0.194',1,1,NULL,NULL,NULL),(194,'169.254.0.195',1,1,NULL,NULL,NULL),(195,'169.254.0.196',1,1,NULL,NULL,NULL),(196,'169.254.0.197',1,1,NULL,NULL,NULL),(197,'169.254.0.198',1,1,NULL,NULL,NULL),(198,'169.254.0.199',1,1,NULL,NULL,NULL),(199,'169.254.0.200',1,1,NULL,NULL,NULL),(200,'169.254.0.201',1,1,NULL,NULL,NULL),(201,'169.254.0.202',1,1,NULL,NULL,NULL),(202,'169.254.0.203',1,1,NULL,NULL,NULL),(203,'169.254.0.204',1,1,NULL,NULL,NULL),(204,'169.254.0.205',1,1,NULL,NULL,NULL),(205,'169.254.0.206',1,1,NULL,NULL,NULL),(206,'169.254.0.207',1,1,NULL,NULL,NULL),(207,'169.254.0.208',1,1,NULL,NULL,NULL),(208,'169.254.0.209',1,1,NULL,NULL,NULL),(209,'169.254.0.210',1,1,NULL,NULL,NULL),(210,'169.254.0.211',1,1,NULL,NULL,NULL),(211,'169.254.0.212',1,1,NULL,NULL,NULL),(212,'169.254.0.213',1,1,NULL,NULL,NULL),(213,'169.254.0.214',1,1,NULL,NULL,NULL),(214,'169.254.0.215',1,1,NULL,NULL,NULL),(215,'169.254.0.216',1,1,NULL,NULL,NULL),(216,'169.254.0.217',1,1,NULL,NULL,NULL),(217,'169.254.0.218',1,1,NULL,NULL,NULL),(218,'169.254.0.219',1,1,NULL,NULL,NULL),(219,'169.254.0.220',1,1,NULL,NULL,NULL),(220,'169.254.0.221',1,1,NULL,NULL,NULL),(221,'169.254.0.222',1,1,NULL,NULL,NULL),(222,'169.254.0.223',1,1,NULL,NULL,NULL),(223,'169.254.0.224',1,1,NULL,NULL,NULL),(224,'169.254.0.225',1,1,NULL,NULL,NULL),(225,'169.254.0.226',1,1,NULL,NULL,NULL),(226,'169.254.0.227',1,1,NULL,NULL,NULL),(227,'169.254.0.228',1,1,NULL,NULL,NULL),(228,'169.254.0.229',1,1,NULL,NULL,NULL),(229,'169.254.0.230',1,1,NULL,NULL,NULL),(230,'169.254.0.231',1,1,NULL,NULL,NULL),(231,'169.254.0.232',1,1,NULL,NULL,NULL),(232,'169.254.0.233',1,1,NULL,NULL,NULL),(233,'169.254.0.234',1,1,NULL,NULL,NULL),(234,'169.254.0.235',1,1,NULL,NULL,NULL),(235,'169.254.0.236',1,1,NULL,NULL,NULL),(236,'169.254.0.237',1,1,NULL,NULL,NULL),(237,'169.254.0.238',1,1,NULL,NULL,NULL),(238,'169.254.0.239',1,1,NULL,NULL,NULL),(239,'169.254.0.240',1,1,NULL,NULL,NULL),(240,'169.254.0.241',1,1,NULL,NULL,NULL),(241,'169.254.0.242',1,1,NULL,NULL,NULL),(242,'169.254.0.243',1,1,NULL,NULL,NULL),(243,'169.254.0.244',1,1,NULL,NULL,NULL),(244,'169.254.0.245',1,1,NULL,NULL,NULL),(245,'169.254.0.246',1,1,NULL,NULL,NULL),(246,'169.254.0.247',1,1,NULL,NULL,NULL),(247,'169.254.0.248',1,1,NULL,NULL,NULL),(248,'169.254.0.249',1,1,NULL,NULL,NULL),(249,'169.254.0.250',1,1,NULL,NULL,NULL),(250,'169.254.0.251',1,1,NULL,NULL,NULL),(251,'169.254.0.252',1,1,NULL,NULL,NULL),(252,'169.254.0.253',1,1,NULL,NULL,NULL),(253,'169.254.0.254',1,1,NULL,NULL,NULL),(254,'169.254.0.255',1,1,NULL,NULL,NULL),(255,'169.254.1.0',1,1,NULL,NULL,NULL),(256,'169.254.1.1',1,1,NULL,NULL,NULL),(257,'169.254.1.2',1,1,NULL,NULL,NULL),(258,'169.254.1.3',1,1,NULL,NULL,NULL),(259,'169.254.1.4',1,1,NULL,NULL,NULL),(260,'169.254.1.5',1,1,NULL,NULL,NULL),(261,'169.254.1.6',1,1,NULL,NULL,NULL),(262,'169.254.1.7',1,1,NULL,NULL,NULL),(263,'169.254.1.8',1,1,NULL,NULL,NULL),(264,'169.254.1.9',1,1,NULL,NULL,NULL),(265,'169.254.1.10',1,1,NULL,NULL,NULL),(266,'169.254.1.11',1,1,NULL,NULL,NULL),(267,'169.254.1.12',1,1,NULL,NULL,NULL),(268,'169.254.1.13',1,1,NULL,NULL,NULL),(269,'169.254.1.14',1,1,NULL,NULL,NULL),(270,'169.254.1.15',1,1,NULL,NULL,NULL),(271,'169.254.1.16',1,1,NULL,NULL,NULL),(272,'169.254.1.17',1,1,NULL,NULL,NULL),(273,'169.254.1.18',1,1,NULL,NULL,NULL),(274,'169.254.1.19',1,1,NULL,NULL,NULL),(275,'169.254.1.20',1,1,NULL,NULL,NULL),(276,'169.254.1.21',1,1,NULL,NULL,NULL),(277,'169.254.1.22',1,1,NULL,NULL,NULL),(278,'169.254.1.23',1,1,NULL,NULL,NULL),(279,'169.254.1.24',1,1,NULL,NULL,NULL),(280,'169.254.1.25',1,1,NULL,NULL,NULL),(281,'169.254.1.26',1,1,NULL,NULL,NULL),(282,'169.254.1.27',1,1,NULL,NULL,NULL),(283,'169.254.1.28',1,1,NULL,NULL,NULL),(284,'169.254.1.29',1,1,NULL,NULL,NULL),(285,'169.254.1.30',1,1,NULL,NULL,NULL),(286,'169.254.1.31',1,1,NULL,NULL,NULL),(287,'169.254.1.32',1,1,NULL,NULL,NULL),(288,'169.254.1.33',1,1,NULL,NULL,NULL),(289,'169.254.1.34',1,1,NULL,NULL,NULL),(290,'169.254.1.35',1,1,NULL,NULL,NULL),(291,'169.254.1.36',1,1,NULL,NULL,NULL),(292,'169.254.1.37',1,1,NULL,NULL,NULL),(293,'169.254.1.38',1,1,NULL,NULL,NULL),(294,'169.254.1.39',1,1,NULL,NULL,NULL),(295,'169.254.1.40',1,1,NULL,NULL,NULL),(296,'169.254.1.41',1,1,NULL,NULL,NULL),(297,'169.254.1.42',1,1,NULL,NULL,NULL),(298,'169.254.1.43',1,1,NULL,NULL,NULL),(299,'169.254.1.44',1,1,NULL,NULL,NULL),(300,'169.254.1.45',1,1,NULL,NULL,NULL),(301,'169.254.1.46',1,1,NULL,NULL,NULL),(302,'169.254.1.47',1,1,NULL,NULL,NULL),(303,'169.254.1.48',1,1,NULL,NULL,NULL),(304,'169.254.1.49',1,1,NULL,NULL,NULL),(305,'169.254.1.50',1,1,NULL,NULL,NULL),(306,'169.254.1.51',1,1,NULL,NULL,NULL),(307,'169.254.1.52',1,1,NULL,NULL,NULL),(308,'169.254.1.53',1,1,NULL,NULL,NULL),(309,'169.254.1.54',1,1,NULL,NULL,NULL),(310,'169.254.1.55',1,1,NULL,NULL,NULL),(311,'169.254.1.56',1,1,NULL,NULL,NULL),(312,'169.254.1.57',1,1,NULL,NULL,NULL),(313,'169.254.1.58',1,1,NULL,NULL,NULL),(314,'169.254.1.59',1,1,NULL,NULL,NULL),(315,'169.254.1.60',1,1,NULL,NULL,NULL),(316,'169.254.1.61',1,1,NULL,NULL,NULL),(317,'169.254.1.62',1,1,NULL,NULL,NULL),(318,'169.254.1.63',1,1,NULL,NULL,NULL),(319,'169.254.1.64',1,1,NULL,NULL,NULL),(320,'169.254.1.65',1,1,NULL,NULL,NULL),(321,'169.254.1.66',1,1,NULL,NULL,NULL),(322,'169.254.1.67',1,1,NULL,NULL,NULL),(323,'169.254.1.68',1,1,NULL,NULL,NULL),(324,'169.254.1.69',1,1,NULL,NULL,NULL),(325,'169.254.1.70',1,1,NULL,NULL,NULL),(326,'169.254.1.71',1,1,NULL,NULL,NULL),(327,'169.254.1.72',1,1,NULL,NULL,NULL),(328,'169.254.1.73',1,1,NULL,NULL,NULL),(329,'169.254.1.74',1,1,NULL,NULL,NULL),(330,'169.254.1.75',1,1,NULL,NULL,NULL),(331,'169.254.1.76',1,1,NULL,NULL,NULL),(332,'169.254.1.77',1,1,NULL,NULL,NULL),(333,'169.254.1.78',1,1,NULL,NULL,NULL),(334,'169.254.1.79',1,1,NULL,NULL,NULL),(335,'169.254.1.80',1,1,NULL,NULL,NULL),(336,'169.254.1.81',1,1,NULL,NULL,NULL),(337,'169.254.1.82',1,1,NULL,NULL,NULL),(338,'169.254.1.83',1,1,NULL,NULL,NULL),(339,'169.254.1.84',1,1,NULL,NULL,NULL),(340,'169.254.1.85',1,1,NULL,NULL,NULL),(341,'169.254.1.86',1,1,NULL,NULL,NULL),(342,'169.254.1.87',1,1,NULL,NULL,NULL),(343,'169.254.1.88',1,1,NULL,NULL,NULL),(344,'169.254.1.89',1,1,NULL,NULL,NULL),(345,'169.254.1.90',1,1,NULL,NULL,NULL),(346,'169.254.1.91',1,1,NULL,NULL,NULL),(347,'169.254.1.92',1,1,NULL,NULL,NULL),(348,'169.254.1.93',1,1,NULL,NULL,NULL),(349,'169.254.1.94',1,1,NULL,NULL,NULL),(350,'169.254.1.95',1,1,NULL,NULL,NULL),(351,'169.254.1.96',1,1,NULL,NULL,NULL),(352,'169.254.1.97',1,1,NULL,NULL,NULL),(353,'169.254.1.98',1,1,NULL,NULL,NULL),(354,'169.254.1.99',1,1,NULL,NULL,NULL),(355,'169.254.1.100',1,1,NULL,NULL,NULL),(356,'169.254.1.101',1,1,NULL,NULL,NULL),(357,'169.254.1.102',1,1,NULL,NULL,NULL),(358,'169.254.1.103',1,1,NULL,NULL,NULL),(359,'169.254.1.104',1,1,NULL,NULL,NULL),(360,'169.254.1.105',1,1,NULL,NULL,NULL),(361,'169.254.1.106',1,1,NULL,NULL,NULL),(362,'169.254.1.107',1,1,NULL,NULL,NULL),(363,'169.254.1.108',1,1,NULL,NULL,NULL),(364,'169.254.1.109',1,1,NULL,NULL,NULL),(365,'169.254.1.110',1,1,NULL,NULL,NULL),(366,'169.254.1.111',1,1,NULL,NULL,NULL),(367,'169.254.1.112',1,1,NULL,NULL,NULL),(368,'169.254.1.113',1,1,NULL,NULL,NULL),(369,'169.254.1.114',1,1,NULL,NULL,NULL),(370,'169.254.1.115',1,1,NULL,NULL,NULL),(371,'169.254.1.116',1,1,NULL,NULL,NULL),(372,'169.254.1.117',1,1,NULL,NULL,NULL),(373,'169.254.1.118',1,1,NULL,NULL,NULL),(374,'169.254.1.119',1,1,NULL,NULL,NULL),(375,'169.254.1.120',1,1,NULL,NULL,NULL),(376,'169.254.1.121',1,1,NULL,NULL,NULL),(377,'169.254.1.122',1,1,NULL,NULL,NULL),(378,'169.254.1.123',1,1,NULL,NULL,NULL),(379,'169.254.1.124',1,1,NULL,NULL,NULL),(380,'169.254.1.125',1,1,NULL,NULL,NULL),(381,'169.254.1.126',1,1,NULL,NULL,NULL),(382,'169.254.1.127',1,1,NULL,NULL,NULL),(383,'169.254.1.128',1,1,NULL,NULL,NULL),(384,'169.254.1.129',1,1,NULL,NULL,NULL),(385,'169.254.1.130',1,1,NULL,NULL,NULL),(386,'169.254.1.131',1,1,NULL,NULL,NULL),(387,'169.254.1.132',1,1,NULL,NULL,NULL),(388,'169.254.1.133',1,1,NULL,NULL,NULL),(389,'169.254.1.134',1,1,NULL,NULL,NULL),(390,'169.254.1.135',1,1,NULL,NULL,NULL),(391,'169.254.1.136',1,1,NULL,NULL,NULL),(392,'169.254.1.137',1,1,NULL,NULL,NULL),(393,'169.254.1.138',1,1,NULL,NULL,NULL),(394,'169.254.1.139',1,1,NULL,NULL,NULL),(395,'169.254.1.140',1,1,NULL,NULL,NULL),(396,'169.254.1.141',1,1,NULL,NULL,NULL),(397,'169.254.1.142',1,1,NULL,NULL,NULL),(398,'169.254.1.143',1,1,NULL,NULL,NULL),(399,'169.254.1.144',1,1,NULL,NULL,NULL),(400,'169.254.1.145',1,1,NULL,NULL,NULL),(401,'169.254.1.146',1,1,NULL,NULL,NULL),(402,'169.254.1.147',1,1,NULL,NULL,NULL),(403,'169.254.1.148',1,1,NULL,NULL,NULL),(404,'169.254.1.149',1,1,NULL,NULL,NULL),(405,'169.254.1.150',1,1,NULL,NULL,NULL),(406,'169.254.1.151',1,1,NULL,NULL,NULL),(407,'169.254.1.152',1,1,NULL,NULL,NULL),(408,'169.254.1.153',1,1,NULL,NULL,NULL),(409,'169.254.1.154',1,1,NULL,NULL,NULL),(410,'169.254.1.155',1,1,NULL,NULL,NULL),(411,'169.254.1.156',1,1,NULL,NULL,NULL),(412,'169.254.1.157',1,1,NULL,NULL,NULL),(413,'169.254.1.158',1,1,NULL,NULL,NULL),(414,'169.254.1.159',1,1,NULL,NULL,NULL),(415,'169.254.1.160',1,1,NULL,NULL,NULL),(416,'169.254.1.161',1,1,NULL,NULL,NULL),(417,'169.254.1.162',1,1,NULL,NULL,NULL),(418,'169.254.1.163',1,1,NULL,NULL,NULL),(419,'169.254.1.164',1,1,NULL,NULL,NULL),(420,'169.254.1.165',1,1,NULL,NULL,NULL),(421,'169.254.1.166',1,1,NULL,NULL,NULL),(422,'169.254.1.167',1,1,NULL,NULL,NULL),(423,'169.254.1.168',1,1,NULL,NULL,NULL),(424,'169.254.1.169',1,1,NULL,NULL,NULL),(425,'169.254.1.170',1,1,NULL,NULL,NULL),(426,'169.254.1.171',1,1,NULL,NULL,NULL),(427,'169.254.1.172',1,1,NULL,NULL,NULL),(428,'169.254.1.173',1,1,NULL,NULL,NULL),(429,'169.254.1.174',1,1,NULL,NULL,NULL),(430,'169.254.1.175',1,1,NULL,NULL,NULL),(431,'169.254.1.176',1,1,NULL,NULL,NULL),(432,'169.254.1.177',1,1,NULL,NULL,NULL),(433,'169.254.1.178',1,1,NULL,NULL,NULL),(434,'169.254.1.179',1,1,NULL,NULL,NULL),(435,'169.254.1.180',1,1,NULL,NULL,NULL),(436,'169.254.1.181',1,1,NULL,NULL,NULL),(437,'169.254.1.182',1,1,NULL,NULL,NULL),(438,'169.254.1.183',1,1,NULL,NULL,NULL),(439,'169.254.1.184',1,1,NULL,NULL,NULL),(440,'169.254.1.185',1,1,NULL,NULL,NULL),(441,'169.254.1.186',1,1,NULL,NULL,NULL),(442,'169.254.1.187',1,1,NULL,NULL,NULL),(443,'169.254.1.188',1,1,NULL,NULL,NULL),(444,'169.254.1.189',1,1,NULL,NULL,NULL),(445,'169.254.1.190',1,1,NULL,NULL,NULL),(446,'169.254.1.191',1,1,NULL,NULL,NULL),(447,'169.254.1.192',1,1,NULL,NULL,NULL),(448,'169.254.1.193',1,1,NULL,NULL,NULL),(449,'169.254.1.194',1,1,NULL,NULL,NULL),(450,'169.254.1.195',1,1,NULL,NULL,NULL),(451,'169.254.1.196',1,1,NULL,NULL,NULL),(452,'169.254.1.197',1,1,NULL,NULL,NULL),(453,'169.254.1.198',1,1,NULL,NULL,NULL),(454,'169.254.1.199',1,1,NULL,NULL,NULL),(455,'169.254.1.200',1,1,NULL,NULL,NULL),(456,'169.254.1.201',1,1,NULL,NULL,NULL),(457,'169.254.1.202',1,1,NULL,NULL,NULL),(458,'169.254.1.203',1,1,NULL,NULL,NULL),(459,'169.254.1.204',1,1,NULL,NULL,NULL),(460,'169.254.1.205',1,1,NULL,NULL,NULL),(461,'169.254.1.206',1,1,NULL,NULL,NULL),(462,'169.254.1.207',1,1,NULL,NULL,NULL),(463,'169.254.1.208',1,1,NULL,NULL,NULL),(464,'169.254.1.209',1,1,NULL,NULL,NULL),(465,'169.254.1.210',1,1,NULL,NULL,NULL),(466,'169.254.1.211',1,1,NULL,NULL,NULL),(467,'169.254.1.212',1,1,NULL,NULL,NULL),(468,'169.254.1.213',1,1,NULL,NULL,NULL),(469,'169.254.1.214',1,1,NULL,NULL,NULL),(470,'169.254.1.215',1,1,NULL,NULL,NULL),(471,'169.254.1.216',1,1,NULL,NULL,NULL),(472,'169.254.1.217',1,1,NULL,NULL,NULL),(473,'169.254.1.218',1,1,NULL,NULL,NULL),(474,'169.254.1.219',1,1,NULL,NULL,NULL),(475,'169.254.1.220',1,1,NULL,NULL,NULL),(476,'169.254.1.221',1,1,NULL,NULL,NULL),(477,'169.254.1.222',1,1,NULL,NULL,NULL),(478,'169.254.1.223',1,1,NULL,NULL,NULL),(479,'169.254.1.224',1,1,NULL,NULL,NULL),(480,'169.254.1.225',1,1,NULL,NULL,NULL),(481,'169.254.1.226',1,1,NULL,NULL,NULL),(482,'169.254.1.227',1,1,NULL,NULL,NULL),(483,'169.254.1.228',1,1,NULL,NULL,NULL),(484,'169.254.1.229',1,1,NULL,NULL,NULL),(485,'169.254.1.230',1,1,NULL,NULL,NULL),(486,'169.254.1.231',1,1,NULL,NULL,NULL),(487,'169.254.1.232',1,1,NULL,NULL,NULL),(488,'169.254.1.233',1,1,NULL,NULL,NULL),(489,'169.254.1.234',1,1,NULL,NULL,NULL),(490,'169.254.1.235',1,1,NULL,NULL,NULL),(491,'169.254.1.236',1,1,NULL,NULL,NULL),(492,'169.254.1.237',1,1,NULL,NULL,NULL),(493,'169.254.1.238',1,1,NULL,NULL,NULL),(494,'169.254.1.239',1,1,NULL,NULL,NULL),(495,'169.254.1.240',1,1,NULL,NULL,NULL),(496,'169.254.1.241',1,1,NULL,NULL,NULL),(497,'169.254.1.242',1,1,NULL,NULL,NULL),(498,'169.254.1.243',1,1,NULL,NULL,NULL),(499,'169.254.1.244',1,1,NULL,NULL,NULL),(500,'169.254.1.245',1,1,NULL,NULL,NULL),(501,'169.254.1.246',1,1,NULL,NULL,NULL),(502,'169.254.1.247',1,1,NULL,NULL,NULL),(503,'169.254.1.248',1,1,NULL,NULL,NULL),(504,'169.254.1.249',1,1,NULL,NULL,NULL),(505,'169.254.1.250',1,1,NULL,NULL,NULL),(506,'169.254.1.251',1,1,NULL,NULL,NULL),(507,'169.254.1.252',1,1,NULL,NULL,NULL),(508,'169.254.1.253',1,1,NULL,NULL,NULL),(509,'169.254.1.254',1,1,NULL,NULL,NULL),(510,'169.254.1.255',1,1,NULL,NULL,NULL),(511,'169.254.2.0',1,1,NULL,NULL,NULL),(512,'169.254.2.1',1,1,NULL,NULL,NULL),(513,'169.254.2.2',1,1,NULL,NULL,NULL),(514,'169.254.2.3',1,1,NULL,NULL,NULL),(515,'169.254.2.4',1,1,NULL,NULL,NULL),(516,'169.254.2.5',1,1,NULL,NULL,NULL),(517,'169.254.2.6',1,1,NULL,NULL,NULL),(518,'169.254.2.7',1,1,NULL,NULL,NULL),(519,'169.254.2.8',1,1,NULL,NULL,NULL),(520,'169.254.2.9',1,1,NULL,NULL,NULL),(521,'169.254.2.10',1,1,NULL,NULL,NULL),(522,'169.254.2.11',1,1,NULL,NULL,NULL),(523,'169.254.2.12',1,1,NULL,NULL,NULL),(524,'169.254.2.13',1,1,NULL,NULL,NULL),(525,'169.254.2.14',1,1,NULL,NULL,NULL),(526,'169.254.2.15',1,1,NULL,NULL,NULL),(527,'169.254.2.16',1,1,NULL,NULL,NULL),(528,'169.254.2.17',1,1,NULL,NULL,NULL),(529,'169.254.2.18',1,1,NULL,NULL,NULL),(530,'169.254.2.19',1,1,NULL,NULL,NULL),(531,'169.254.2.20',1,1,NULL,NULL,NULL),(532,'169.254.2.21',1,1,NULL,NULL,NULL),(533,'169.254.2.22',1,1,NULL,NULL,NULL),(534,'169.254.2.23',1,1,NULL,NULL,NULL),(535,'169.254.2.24',1,1,NULL,NULL,NULL),(536,'169.254.2.25',1,1,NULL,NULL,NULL),(537,'169.254.2.26',1,1,NULL,NULL,NULL),(538,'169.254.2.27',1,1,NULL,NULL,NULL),(539,'169.254.2.28',1,1,NULL,NULL,NULL),(540,'169.254.2.29',1,1,NULL,NULL,NULL),(541,'169.254.2.30',1,1,NULL,NULL,NULL),(542,'169.254.2.31',1,1,NULL,NULL,NULL),(543,'169.254.2.32',1,1,NULL,NULL,NULL),(544,'169.254.2.33',1,1,NULL,NULL,NULL),(545,'169.254.2.34',1,1,NULL,NULL,NULL),(546,'169.254.2.35',1,1,NULL,NULL,NULL),(547,'169.254.2.36',1,1,NULL,NULL,NULL),(548,'169.254.2.37',1,1,NULL,NULL,NULL),(549,'169.254.2.38',1,1,NULL,NULL,NULL),(550,'169.254.2.39',1,1,NULL,NULL,NULL),(551,'169.254.2.40',1,1,NULL,NULL,NULL),(552,'169.254.2.41',1,1,NULL,NULL,NULL),(553,'169.254.2.42',1,1,NULL,NULL,NULL),(554,'169.254.2.43',1,1,NULL,NULL,NULL),(555,'169.254.2.44',1,1,NULL,NULL,NULL),(556,'169.254.2.45',1,1,NULL,NULL,NULL),(557,'169.254.2.46',1,1,NULL,NULL,NULL),(558,'169.254.2.47',1,1,NULL,NULL,NULL),(559,'169.254.2.48',1,1,NULL,NULL,NULL),(560,'169.254.2.49',1,1,NULL,NULL,NULL),(561,'169.254.2.50',1,1,NULL,NULL,NULL),(562,'169.254.2.51',1,1,NULL,NULL,NULL),(563,'169.254.2.52',1,1,NULL,NULL,NULL),(564,'169.254.2.53',1,1,NULL,NULL,NULL),(565,'169.254.2.54',1,1,NULL,NULL,NULL),(566,'169.254.2.55',1,1,NULL,NULL,NULL),(567,'169.254.2.56',1,1,NULL,NULL,NULL),(568,'169.254.2.57',1,1,NULL,NULL,NULL),(569,'169.254.2.58',1,1,NULL,NULL,NULL),(570,'169.254.2.59',1,1,NULL,NULL,NULL),(571,'169.254.2.60',1,1,NULL,NULL,NULL),(572,'169.254.2.61',1,1,NULL,NULL,NULL),(573,'169.254.2.62',1,1,NULL,NULL,NULL),(574,'169.254.2.63',1,1,NULL,NULL,NULL),(575,'169.254.2.64',1,1,NULL,NULL,NULL),(576,'169.254.2.65',1,1,NULL,NULL,NULL),(577,'169.254.2.66',1,1,NULL,NULL,NULL),(578,'169.254.2.67',1,1,NULL,NULL,NULL),(579,'169.254.2.68',1,1,NULL,NULL,NULL),(580,'169.254.2.69',1,1,NULL,NULL,NULL),(581,'169.254.2.70',1,1,NULL,NULL,NULL),(582,'169.254.2.71',1,1,NULL,NULL,NULL),(583,'169.254.2.72',1,1,NULL,NULL,NULL),(584,'169.254.2.73',1,1,NULL,NULL,NULL),(585,'169.254.2.74',1,1,NULL,NULL,NULL),(586,'169.254.2.75',1,1,NULL,NULL,NULL),(587,'169.254.2.76',1,1,NULL,NULL,NULL),(588,'169.254.2.77',1,1,NULL,NULL,NULL),(589,'169.254.2.78',1,1,NULL,NULL,NULL),(590,'169.254.2.79',1,1,NULL,NULL,NULL),(591,'169.254.2.80',1,1,NULL,NULL,NULL),(592,'169.254.2.81',1,1,NULL,NULL,NULL),(593,'169.254.2.82',1,1,NULL,NULL,NULL),(594,'169.254.2.83',1,1,NULL,NULL,NULL),(595,'169.254.2.84',1,1,NULL,NULL,NULL),(596,'169.254.2.85',1,1,NULL,NULL,NULL),(597,'169.254.2.86',1,1,NULL,NULL,NULL),(598,'169.254.2.87',1,1,NULL,NULL,NULL),(599,'169.254.2.88',1,1,NULL,NULL,NULL),(600,'169.254.2.89',1,1,NULL,NULL,NULL),(601,'169.254.2.90',1,1,NULL,NULL,NULL),(602,'169.254.2.91',1,1,NULL,NULL,NULL),(603,'169.254.2.92',1,1,NULL,NULL,NULL),(604,'169.254.2.93',1,1,NULL,NULL,NULL),(605,'169.254.2.94',1,1,NULL,NULL,NULL),(606,'169.254.2.95',1,1,NULL,NULL,NULL),(607,'169.254.2.96',1,1,NULL,NULL,NULL),(608,'169.254.2.97',1,1,NULL,NULL,NULL),(609,'169.254.2.98',1,1,NULL,NULL,NULL),(610,'169.254.2.99',1,1,NULL,NULL,NULL),(611,'169.254.2.100',1,1,NULL,NULL,NULL),(612,'169.254.2.101',1,1,NULL,NULL,NULL),(613,'169.254.2.102',1,1,NULL,NULL,NULL),(614,'169.254.2.103',1,1,NULL,NULL,NULL),(615,'169.254.2.104',1,1,NULL,NULL,NULL),(616,'169.254.2.105',1,1,NULL,NULL,NULL),(617,'169.254.2.106',1,1,NULL,NULL,NULL),(618,'169.254.2.107',1,1,NULL,NULL,NULL),(619,'169.254.2.108',1,1,NULL,NULL,NULL),(620,'169.254.2.109',1,1,NULL,NULL,NULL),(621,'169.254.2.110',1,1,NULL,NULL,NULL),(622,'169.254.2.111',1,1,NULL,NULL,NULL),(623,'169.254.2.112',1,1,NULL,NULL,NULL),(624,'169.254.2.113',1,1,NULL,NULL,NULL),(625,'169.254.2.114',1,1,NULL,NULL,NULL),(626,'169.254.2.115',1,1,NULL,NULL,NULL),(627,'169.254.2.116',1,1,NULL,NULL,NULL),(628,'169.254.2.117',1,1,NULL,NULL,NULL),(629,'169.254.2.118',1,1,NULL,NULL,NULL),(630,'169.254.2.119',1,1,NULL,NULL,NULL),(631,'169.254.2.120',1,1,NULL,NULL,NULL),(632,'169.254.2.121',1,1,NULL,NULL,NULL),(633,'169.254.2.122',1,1,NULL,NULL,NULL),(634,'169.254.2.123',1,1,NULL,NULL,NULL),(635,'169.254.2.124',1,1,NULL,NULL,NULL),(636,'169.254.2.125',1,1,NULL,NULL,NULL),(637,'169.254.2.126',1,1,NULL,NULL,NULL),(638,'169.254.2.127',1,1,NULL,NULL,NULL),(639,'169.254.2.128',1,1,NULL,NULL,NULL),(640,'169.254.2.129',1,1,NULL,NULL,NULL),(641,'169.254.2.130',1,1,NULL,NULL,NULL),(642,'169.254.2.131',1,1,NULL,NULL,NULL),(643,'169.254.2.132',1,1,NULL,NULL,NULL),(644,'169.254.2.133',1,1,NULL,NULL,NULL),(645,'169.254.2.134',1,1,NULL,NULL,NULL),(646,'169.254.2.135',1,1,NULL,NULL,NULL),(647,'169.254.2.136',1,1,NULL,NULL,NULL),(648,'169.254.2.137',1,1,NULL,NULL,NULL),(649,'169.254.2.138',1,1,NULL,NULL,NULL),(650,'169.254.2.139',1,1,NULL,NULL,NULL),(651,'169.254.2.140',1,1,NULL,NULL,NULL),(652,'169.254.2.141',1,1,NULL,NULL,NULL),(653,'169.254.2.142',1,1,NULL,NULL,NULL),(654,'169.254.2.143',1,1,NULL,NULL,NULL),(655,'169.254.2.144',1,1,NULL,NULL,NULL),(656,'169.254.2.145',1,1,NULL,NULL,NULL),(657,'169.254.2.146',1,1,NULL,NULL,NULL),(658,'169.254.2.147',1,1,NULL,NULL,NULL),(659,'169.254.2.148',1,1,NULL,NULL,NULL),(660,'169.254.2.149',1,1,NULL,NULL,NULL),(661,'169.254.2.150',1,1,NULL,NULL,NULL),(662,'169.254.2.151',1,1,NULL,NULL,NULL),(663,'169.254.2.152',1,1,NULL,NULL,NULL),(664,'169.254.2.153',1,1,NULL,NULL,NULL),(665,'169.254.2.154',1,1,NULL,NULL,NULL),(666,'169.254.2.155',1,1,NULL,NULL,NULL),(667,'169.254.2.156',1,1,NULL,NULL,NULL),(668,'169.254.2.157',1,1,NULL,NULL,NULL),(669,'169.254.2.158',1,1,NULL,NULL,NULL),(670,'169.254.2.159',1,1,NULL,NULL,NULL),(671,'169.254.2.160',1,1,NULL,NULL,NULL),(672,'169.254.2.161',1,1,NULL,NULL,NULL),(673,'169.254.2.162',1,1,NULL,NULL,NULL),(674,'169.254.2.163',1,1,NULL,NULL,NULL),(675,'169.254.2.164',1,1,NULL,NULL,NULL),(676,'169.254.2.165',1,1,NULL,NULL,NULL),(677,'169.254.2.166',1,1,NULL,NULL,NULL),(678,'169.254.2.167',1,1,NULL,NULL,NULL),(679,'169.254.2.168',1,1,NULL,NULL,NULL),(680,'169.254.2.169',1,1,NULL,NULL,NULL),(681,'169.254.2.170',1,1,NULL,NULL,NULL),(682,'169.254.2.171',1,1,NULL,NULL,NULL),(683,'169.254.2.172',1,1,NULL,NULL,NULL),(684,'169.254.2.173',1,1,NULL,NULL,NULL),(685,'169.254.2.174',1,1,NULL,NULL,NULL),(686,'169.254.2.175',1,1,NULL,NULL,NULL),(687,'169.254.2.176',1,1,NULL,NULL,NULL),(688,'169.254.2.177',1,1,NULL,NULL,NULL),(689,'169.254.2.178',1,1,NULL,NULL,NULL),(690,'169.254.2.179',1,1,NULL,NULL,NULL),(691,'169.254.2.180',1,1,NULL,NULL,NULL),(692,'169.254.2.181',1,1,NULL,NULL,NULL),(693,'169.254.2.182',1,1,NULL,NULL,NULL),(694,'169.254.2.183',1,1,NULL,NULL,NULL),(695,'169.254.2.184',1,1,NULL,NULL,NULL),(696,'169.254.2.185',1,1,NULL,NULL,NULL),(697,'169.254.2.186',1,1,NULL,NULL,NULL),(698,'169.254.2.187',1,1,NULL,NULL,NULL),(699,'169.254.2.188',1,1,NULL,NULL,NULL),(700,'169.254.2.189',1,1,NULL,NULL,NULL),(701,'169.254.2.190',1,1,NULL,NULL,NULL),(702,'169.254.2.191',1,1,NULL,NULL,NULL),(703,'169.254.2.192',1,1,NULL,NULL,NULL),(704,'169.254.2.193',1,1,NULL,NULL,NULL),(705,'169.254.2.194',1,1,NULL,NULL,NULL),(706,'169.254.2.195',1,1,NULL,NULL,NULL),(707,'169.254.2.196',1,1,NULL,NULL,NULL),(708,'169.254.2.197',1,1,NULL,NULL,NULL),(709,'169.254.2.198',1,1,NULL,NULL,NULL),(710,'169.254.2.199',1,1,NULL,NULL,NULL),(711,'169.254.2.200',1,1,NULL,NULL,NULL),(712,'169.254.2.201',1,1,NULL,NULL,NULL),(713,'169.254.2.202',1,1,NULL,NULL,NULL),(714,'169.254.2.203',1,1,NULL,NULL,NULL),(715,'169.254.2.204',1,1,NULL,NULL,NULL),(716,'169.254.2.205',1,1,NULL,NULL,NULL),(717,'169.254.2.206',1,1,NULL,NULL,NULL),(718,'169.254.2.207',1,1,NULL,NULL,NULL),(719,'169.254.2.208',1,1,NULL,NULL,NULL),(720,'169.254.2.209',1,1,NULL,NULL,NULL),(721,'169.254.2.210',1,1,NULL,NULL,NULL),(722,'169.254.2.211',1,1,NULL,NULL,NULL),(723,'169.254.2.212',1,1,NULL,NULL,NULL),(724,'169.254.2.213',1,1,NULL,NULL,NULL),(725,'169.254.2.214',1,1,NULL,NULL,NULL),(726,'169.254.2.215',1,1,NULL,NULL,NULL),(727,'169.254.2.216',1,1,NULL,NULL,NULL),(728,'169.254.2.217',1,1,NULL,NULL,NULL),(729,'169.254.2.218',1,1,NULL,NULL,NULL),(730,'169.254.2.219',1,1,NULL,NULL,NULL),(731,'169.254.2.220',1,1,NULL,NULL,NULL),(732,'169.254.2.221',1,1,NULL,NULL,NULL),(733,'169.254.2.222',1,1,NULL,NULL,NULL),(734,'169.254.2.223',1,1,NULL,NULL,NULL),(735,'169.254.2.224',1,1,NULL,NULL,NULL),(736,'169.254.2.225',1,1,NULL,NULL,NULL),(737,'169.254.2.226',1,1,NULL,NULL,NULL),(738,'169.254.2.227',1,1,NULL,NULL,NULL),(739,'169.254.2.228',1,1,NULL,NULL,NULL),(740,'169.254.2.229',1,1,NULL,NULL,NULL),(741,'169.254.2.230',1,1,NULL,NULL,NULL),(742,'169.254.2.231',1,1,NULL,NULL,NULL),(743,'169.254.2.232',1,1,NULL,NULL,NULL),(744,'169.254.2.233',1,1,NULL,NULL,NULL),(745,'169.254.2.234',1,1,NULL,NULL,NULL),(746,'169.254.2.235',1,1,NULL,NULL,NULL),(747,'169.254.2.236',1,1,NULL,NULL,NULL),(748,'169.254.2.237',1,1,NULL,NULL,NULL),(749,'169.254.2.238',1,1,NULL,NULL,NULL),(750,'169.254.2.239',1,1,NULL,NULL,NULL),(751,'169.254.2.240',1,1,NULL,NULL,NULL),(752,'169.254.2.241',1,1,NULL,NULL,NULL),(753,'169.254.2.242',1,1,NULL,NULL,NULL),(754,'169.254.2.243',1,1,NULL,NULL,NULL),(755,'169.254.2.244',1,1,NULL,NULL,NULL),(756,'169.254.2.245',1,1,NULL,NULL,NULL),(757,'169.254.2.246',1,1,NULL,NULL,NULL),(758,'169.254.2.247',1,1,NULL,NULL,NULL),(759,'169.254.2.248',1,1,NULL,NULL,NULL),(760,'169.254.2.249',1,1,NULL,NULL,NULL),(761,'169.254.2.250',1,1,NULL,NULL,NULL),(762,'169.254.2.251',1,1,NULL,NULL,NULL),(763,'169.254.2.252',1,1,NULL,NULL,NULL),(764,'169.254.2.253',1,1,NULL,NULL,NULL),(765,'169.254.2.254',1,1,NULL,NULL,NULL),(766,'169.254.2.255',1,1,NULL,NULL,NULL),(767,'169.254.3.0',1,1,NULL,NULL,NULL),(768,'169.254.3.1',1,1,NULL,NULL,NULL),(769,'169.254.3.2',1,1,NULL,NULL,NULL),(770,'169.254.3.3',1,1,NULL,NULL,NULL),(771,'169.254.3.4',1,1,NULL,NULL,NULL),(772,'169.254.3.5',1,1,NULL,NULL,NULL),(773,'169.254.3.6',1,1,NULL,NULL,NULL),(774,'169.254.3.7',1,1,NULL,NULL,NULL),(775,'169.254.3.8',1,1,NULL,NULL,NULL),(776,'169.254.3.9',1,1,NULL,NULL,NULL),(777,'169.254.3.10',1,1,NULL,NULL,NULL),(778,'169.254.3.11',1,1,NULL,NULL,NULL),(779,'169.254.3.12',1,1,NULL,NULL,NULL),(780,'169.254.3.13',1,1,NULL,NULL,NULL),(781,'169.254.3.14',1,1,NULL,NULL,NULL),(782,'169.254.3.15',1,1,NULL,NULL,NULL),(783,'169.254.3.16',1,1,NULL,NULL,NULL),(784,'169.254.3.17',1,1,NULL,NULL,NULL),(785,'169.254.3.18',1,1,NULL,NULL,NULL),(786,'169.254.3.19',1,1,NULL,NULL,NULL),(787,'169.254.3.20',1,1,NULL,NULL,NULL),(788,'169.254.3.21',1,1,NULL,NULL,NULL),(789,'169.254.3.22',1,1,NULL,NULL,NULL),(790,'169.254.3.23',1,1,NULL,NULL,NULL),(791,'169.254.3.24',1,1,NULL,NULL,NULL),(792,'169.254.3.25',1,1,NULL,NULL,NULL),(793,'169.254.3.26',1,1,NULL,NULL,NULL),(794,'169.254.3.27',1,1,NULL,NULL,NULL),(795,'169.254.3.28',1,1,NULL,NULL,NULL),(796,'169.254.3.29',1,1,NULL,NULL,NULL),(797,'169.254.3.30',1,1,NULL,NULL,NULL),(798,'169.254.3.31',1,1,NULL,NULL,NULL),(799,'169.254.3.32',1,1,NULL,NULL,NULL),(800,'169.254.3.33',1,1,NULL,NULL,NULL),(801,'169.254.3.34',1,1,NULL,NULL,NULL),(802,'169.254.3.35',1,1,NULL,NULL,NULL),(803,'169.254.3.36',1,1,NULL,NULL,NULL),(804,'169.254.3.37',1,1,NULL,NULL,NULL),(805,'169.254.3.38',1,1,NULL,NULL,NULL),(806,'169.254.3.39',1,1,NULL,NULL,NULL),(807,'169.254.3.40',1,1,NULL,NULL,NULL),(808,'169.254.3.41',1,1,NULL,NULL,NULL),(809,'169.254.3.42',1,1,NULL,NULL,NULL),(810,'169.254.3.43',1,1,NULL,NULL,NULL),(811,'169.254.3.44',1,1,NULL,NULL,NULL),(812,'169.254.3.45',1,1,NULL,NULL,NULL),(813,'169.254.3.46',1,1,NULL,NULL,NULL),(814,'169.254.3.47',1,1,NULL,NULL,NULL),(815,'169.254.3.48',1,1,NULL,NULL,NULL),(816,'169.254.3.49',1,1,NULL,NULL,NULL),(817,'169.254.3.50',1,1,NULL,NULL,NULL),(818,'169.254.3.51',1,1,NULL,NULL,NULL),(819,'169.254.3.52',1,1,NULL,NULL,NULL),(820,'169.254.3.53',1,1,NULL,NULL,NULL),(821,'169.254.3.54',1,1,NULL,NULL,NULL),(822,'169.254.3.55',1,1,NULL,NULL,NULL),(823,'169.254.3.56',1,1,NULL,NULL,NULL),(824,'169.254.3.57',1,1,NULL,NULL,NULL),(825,'169.254.3.58',1,1,NULL,NULL,NULL),(826,'169.254.3.59',1,1,NULL,NULL,NULL),(827,'169.254.3.60',1,1,NULL,NULL,NULL),(828,'169.254.3.61',1,1,NULL,NULL,NULL),(829,'169.254.3.62',1,1,NULL,NULL,NULL),(830,'169.254.3.63',1,1,NULL,NULL,NULL),(831,'169.254.3.64',1,1,NULL,NULL,NULL),(832,'169.254.3.65',1,1,NULL,NULL,NULL),(833,'169.254.3.66',1,1,NULL,NULL,NULL),(834,'169.254.3.67',1,1,NULL,NULL,NULL),(835,'169.254.3.68',1,1,NULL,NULL,NULL),(836,'169.254.3.69',1,1,NULL,NULL,NULL),(837,'169.254.3.70',1,1,NULL,NULL,NULL),(838,'169.254.3.71',1,1,NULL,NULL,NULL),(839,'169.254.3.72',1,1,NULL,NULL,NULL),(840,'169.254.3.73',1,1,NULL,NULL,NULL),(841,'169.254.3.74',1,1,NULL,NULL,NULL),(842,'169.254.3.75',1,1,NULL,NULL,NULL),(843,'169.254.3.76',1,1,NULL,NULL,NULL),(844,'169.254.3.77',1,1,NULL,NULL,NULL),(845,'169.254.3.78',1,1,NULL,NULL,NULL),(846,'169.254.3.79',1,1,NULL,NULL,NULL),(847,'169.254.3.80',1,1,NULL,NULL,NULL),(848,'169.254.3.81',1,1,NULL,NULL,NULL),(849,'169.254.3.82',1,1,NULL,NULL,NULL),(850,'169.254.3.83',1,1,NULL,NULL,NULL),(851,'169.254.3.84',1,1,NULL,NULL,NULL),(852,'169.254.3.85',1,1,NULL,NULL,NULL),(853,'169.254.3.86',1,1,NULL,NULL,NULL),(854,'169.254.3.87',1,1,NULL,NULL,NULL),(855,'169.254.3.88',1,1,NULL,NULL,NULL),(856,'169.254.3.89',1,1,NULL,NULL,NULL),(857,'169.254.3.90',1,1,NULL,NULL,NULL),(858,'169.254.3.91',1,1,NULL,NULL,NULL),(859,'169.254.3.92',1,1,NULL,NULL,NULL),(860,'169.254.3.93',1,1,NULL,NULL,NULL),(861,'169.254.3.94',1,1,NULL,NULL,NULL),(862,'169.254.3.95',1,1,NULL,NULL,NULL),(863,'169.254.3.96',1,1,NULL,NULL,NULL),(864,'169.254.3.97',1,1,NULL,NULL,NULL),(865,'169.254.3.98',1,1,NULL,NULL,NULL),(866,'169.254.3.99',1,1,NULL,NULL,NULL),(867,'169.254.3.100',1,1,NULL,NULL,NULL),(868,'169.254.3.101',1,1,NULL,NULL,NULL),(869,'169.254.3.102',1,1,NULL,NULL,NULL),(870,'169.254.3.103',1,1,NULL,NULL,NULL),(871,'169.254.3.104',1,1,NULL,NULL,NULL),(872,'169.254.3.105',1,1,NULL,NULL,NULL),(873,'169.254.3.106',1,1,NULL,NULL,NULL),(874,'169.254.3.107',1,1,NULL,NULL,NULL),(875,'169.254.3.108',1,1,NULL,NULL,NULL),(876,'169.254.3.109',1,1,NULL,NULL,NULL),(877,'169.254.3.110',1,1,NULL,NULL,NULL),(878,'169.254.3.111',1,1,NULL,NULL,NULL),(879,'169.254.3.112',1,1,NULL,NULL,NULL),(880,'169.254.3.113',1,1,NULL,NULL,NULL),(881,'169.254.3.114',1,1,NULL,NULL,NULL),(882,'169.254.3.115',1,1,NULL,NULL,NULL),(883,'169.254.3.116',1,1,NULL,NULL,NULL),(884,'169.254.3.117',1,1,NULL,NULL,NULL),(885,'169.254.3.118',1,1,NULL,NULL,NULL),(886,'169.254.3.119',1,1,NULL,NULL,NULL),(887,'169.254.3.120',1,1,NULL,NULL,NULL),(888,'169.254.3.121',1,1,NULL,NULL,NULL),(889,'169.254.3.122',1,1,NULL,NULL,NULL),(890,'169.254.3.123',1,1,NULL,NULL,NULL),(891,'169.254.3.124',1,1,NULL,NULL,NULL),(892,'169.254.3.125',1,1,NULL,NULL,NULL),(893,'169.254.3.126',1,1,NULL,NULL,NULL),(894,'169.254.3.127',1,1,NULL,NULL,NULL),(895,'169.254.3.128',1,1,NULL,NULL,NULL),(896,'169.254.3.129',1,1,NULL,NULL,NULL),(897,'169.254.3.130',1,1,NULL,NULL,NULL),(898,'169.254.3.131',1,1,NULL,NULL,NULL),(899,'169.254.3.132',1,1,NULL,NULL,NULL),(900,'169.254.3.133',1,1,NULL,NULL,NULL),(901,'169.254.3.134',1,1,NULL,NULL,NULL),(902,'169.254.3.135',1,1,NULL,NULL,NULL),(903,'169.254.3.136',1,1,NULL,NULL,NULL),(904,'169.254.3.137',1,1,NULL,NULL,NULL),(905,'169.254.3.138',1,1,NULL,NULL,NULL),(906,'169.254.3.139',1,1,NULL,NULL,NULL),(907,'169.254.3.140',1,1,NULL,NULL,NULL),(908,'169.254.3.141',1,1,NULL,NULL,NULL),(909,'169.254.3.142',1,1,NULL,NULL,NULL),(910,'169.254.3.143',1,1,NULL,NULL,NULL),(911,'169.254.3.144',1,1,NULL,NULL,NULL),(912,'169.254.3.145',1,1,NULL,NULL,NULL),(913,'169.254.3.146',1,1,NULL,NULL,NULL),(914,'169.254.3.147',1,1,NULL,NULL,NULL),(915,'169.254.3.148',1,1,NULL,NULL,NULL),(916,'169.254.3.149',1,1,NULL,NULL,NULL),(917,'169.254.3.150',1,1,NULL,NULL,NULL),(918,'169.254.3.151',1,1,NULL,NULL,NULL),(919,'169.254.3.152',1,1,NULL,NULL,NULL),(920,'169.254.3.153',1,1,NULL,NULL,NULL),(921,'169.254.3.154',1,1,NULL,NULL,NULL),(922,'169.254.3.155',1,1,NULL,NULL,NULL),(923,'169.254.3.156',1,1,NULL,NULL,NULL),(924,'169.254.3.157',1,1,NULL,NULL,NULL),(925,'169.254.3.158',1,1,NULL,NULL,NULL),(926,'169.254.3.159',1,1,NULL,NULL,NULL),(927,'169.254.3.160',1,1,NULL,NULL,NULL),(928,'169.254.3.161',1,1,NULL,NULL,NULL),(929,'169.254.3.162',1,1,NULL,NULL,NULL),(930,'169.254.3.163',1,1,NULL,NULL,NULL),(931,'169.254.3.164',1,1,NULL,NULL,NULL),(932,'169.254.3.165',1,1,NULL,NULL,NULL),(933,'169.254.3.166',1,1,NULL,NULL,NULL),(934,'169.254.3.167',1,1,NULL,NULL,NULL),(935,'169.254.3.168',1,1,NULL,NULL,NULL),(936,'169.254.3.169',1,1,NULL,NULL,NULL),(937,'169.254.3.170',1,1,NULL,NULL,NULL),(938,'169.254.3.171',1,1,NULL,NULL,NULL),(939,'169.254.3.172',1,1,NULL,NULL,NULL),(940,'169.254.3.173',1,1,NULL,NULL,NULL),(941,'169.254.3.174',1,1,NULL,NULL,NULL),(942,'169.254.3.175',1,1,NULL,NULL,NULL),(943,'169.254.3.176',1,1,NULL,NULL,NULL),(944,'169.254.3.177',1,1,NULL,NULL,NULL),(945,'169.254.3.178',1,1,NULL,NULL,NULL),(946,'169.254.3.179',1,1,NULL,NULL,NULL),(947,'169.254.3.180',1,1,NULL,NULL,NULL),(948,'169.254.3.181',1,1,NULL,NULL,NULL),(949,'169.254.3.182',1,1,NULL,NULL,NULL),(950,'169.254.3.183',1,1,NULL,NULL,NULL),(951,'169.254.3.184',1,1,NULL,NULL,NULL),(952,'169.254.3.185',1,1,NULL,NULL,NULL),(953,'169.254.3.186',1,1,NULL,NULL,NULL),(954,'169.254.3.187',1,1,NULL,NULL,NULL),(955,'169.254.3.188',1,1,NULL,NULL,NULL),(956,'169.254.3.189',1,1,NULL,NULL,NULL),(957,'169.254.3.190',1,1,NULL,NULL,NULL),(958,'169.254.3.191',1,1,NULL,NULL,NULL),(959,'169.254.3.192',1,1,NULL,NULL,NULL),(960,'169.254.3.193',1,1,NULL,NULL,NULL),(961,'169.254.3.194',1,1,NULL,NULL,NULL),(962,'169.254.3.195',1,1,NULL,NULL,NULL),(963,'169.254.3.196',1,1,NULL,NULL,NULL),(964,'169.254.3.197',1,1,NULL,NULL,NULL),(965,'169.254.3.198',1,1,NULL,NULL,NULL),(966,'169.254.3.199',1,1,NULL,NULL,NULL),(967,'169.254.3.200',1,1,NULL,NULL,NULL),(968,'169.254.3.201',1,1,NULL,NULL,NULL),(969,'169.254.3.202',1,1,NULL,NULL,NULL),(970,'169.254.3.203',1,1,NULL,NULL,NULL),(971,'169.254.3.204',1,1,NULL,NULL,NULL),(972,'169.254.3.205',1,1,NULL,NULL,NULL),(973,'169.254.3.206',1,1,NULL,NULL,NULL),(974,'169.254.3.207',1,1,NULL,NULL,NULL),(975,'169.254.3.208',1,1,NULL,NULL,NULL),(976,'169.254.3.209',1,1,NULL,NULL,NULL),(977,'169.254.3.210',1,1,NULL,NULL,NULL),(978,'169.254.3.211',1,1,NULL,NULL,NULL),(979,'169.254.3.212',1,1,NULL,NULL,NULL),(980,'169.254.3.213',1,1,NULL,NULL,NULL),(981,'169.254.3.214',1,1,NULL,NULL,NULL),(982,'169.254.3.215',1,1,NULL,NULL,NULL),(983,'169.254.3.216',1,1,NULL,NULL,NULL),(984,'169.254.3.217',1,1,NULL,NULL,NULL),(985,'169.254.3.218',1,1,NULL,NULL,NULL),(986,'169.254.3.219',1,1,NULL,NULL,NULL),(987,'169.254.3.220',1,1,NULL,NULL,NULL),(988,'169.254.3.221',1,1,NULL,NULL,NULL),(989,'169.254.3.222',1,1,NULL,NULL,NULL),(990,'169.254.3.223',1,1,NULL,NULL,NULL),(991,'169.254.3.224',1,1,NULL,NULL,NULL),(992,'169.254.3.225',1,1,NULL,NULL,NULL),(993,'169.254.3.226',1,1,NULL,NULL,NULL),(994,'169.254.3.227',1,1,NULL,NULL,NULL),(995,'169.254.3.228',1,1,NULL,NULL,NULL),(996,'169.254.3.229',1,1,NULL,NULL,NULL),(997,'169.254.3.230',1,1,NULL,NULL,NULL),(998,'169.254.3.231',1,1,NULL,NULL,NULL),(999,'169.254.3.232',1,1,NULL,NULL,NULL),(1000,'169.254.3.233',1,1,NULL,NULL,NULL),(1001,'169.254.3.234',1,1,NULL,NULL,NULL),(1002,'169.254.3.235',1,1,NULL,NULL,NULL),(1003,'169.254.3.236',1,1,NULL,NULL,NULL),(1004,'169.254.3.237',1,1,NULL,NULL,NULL),(1005,'169.254.3.238',1,1,NULL,NULL,NULL),(1006,'169.254.3.239',1,1,NULL,NULL,NULL),(1007,'169.254.3.240',1,1,NULL,NULL,NULL),(1008,'169.254.3.241',1,1,NULL,NULL,NULL),(1009,'169.254.3.242',1,1,NULL,NULL,NULL),(1010,'169.254.3.243',1,1,NULL,NULL,NULL),(1011,'169.254.3.244',1,1,NULL,NULL,NULL),(1012,'169.254.3.245',1,1,NULL,NULL,NULL),(1013,'169.254.3.246',1,1,NULL,NULL,NULL),(1014,'169.254.3.247',1,1,NULL,NULL,NULL),(1015,'169.254.3.248',1,1,NULL,NULL,NULL),(1016,'169.254.3.249',1,1,NULL,NULL,NULL),(1017,'169.254.3.250',1,1,NULL,NULL,NULL),(1018,'169.254.3.251',1,1,NULL,NULL,NULL),(1019,'169.254.3.252',1,1,NULL,NULL,NULL),(1020,'169.254.3.253',1,1,NULL,NULL,NULL),(1021,'169.254.3.254',1,1,NULL,NULL,NULL),(1022,'169.254.0.2',2,2,NULL,NULL,NULL),(1023,'169.254.0.3',2,2,NULL,NULL,NULL),(1024,'169.254.0.4',2,2,NULL,NULL,NULL),(1025,'169.254.0.5',2,2,NULL,NULL,NULL),(1026,'169.254.0.6',2,2,NULL,NULL,NULL),(1027,'169.254.0.7',2,2,NULL,NULL,NULL),(1028,'169.254.0.8',2,2,NULL,NULL,NULL),(1029,'169.254.0.9',2,2,NULL,NULL,NULL),(1030,'169.254.0.10',2,2,NULL,NULL,NULL),(1031,'169.254.0.11',2,2,NULL,NULL,NULL),(1032,'169.254.0.12',2,2,NULL,NULL,NULL),(1033,'169.254.0.13',2,2,NULL,NULL,NULL),(1034,'169.254.0.14',2,2,NULL,NULL,NULL),(1035,'169.254.0.15',2,2,NULL,NULL,NULL),(1036,'169.254.0.16',2,2,NULL,NULL,NULL),(1037,'169.254.0.17',2,2,NULL,NULL,NULL),(1038,'169.254.0.18',2,2,NULL,NULL,NULL),(1039,'169.254.0.19',2,2,NULL,NULL,NULL),(1040,'169.254.0.20',2,2,NULL,NULL,NULL),(1041,'169.254.0.21',2,2,NULL,NULL,NULL),(1042,'169.254.0.22',2,2,NULL,NULL,NULL),(1043,'169.254.0.23',2,2,NULL,NULL,NULL),(1044,'169.254.0.24',2,2,NULL,NULL,NULL),(1045,'169.254.0.25',2,2,76,'30ff8edd-f496-431c-94e3-5ffc8901cbc6','2011-05-12 18:48:51'),(1046,'169.254.0.26',2,2,NULL,NULL,NULL),(1047,'169.254.0.27',2,2,NULL,NULL,NULL),(1048,'169.254.0.28',2,2,NULL,NULL,NULL),(1049,'169.254.0.29',2,2,NULL,NULL,NULL),(1050,'169.254.0.30',2,2,NULL,NULL,NULL),(1051,'169.254.0.31',2,2,NULL,NULL,NULL),(1052,'169.254.0.32',2,2,NULL,NULL,NULL),(1053,'169.254.0.33',2,2,NULL,NULL,NULL),(1054,'169.254.0.34',2,2,NULL,NULL,NULL),(1055,'169.254.0.35',2,2,NULL,NULL,NULL),(1056,'169.254.0.36',2,2,NULL,NULL,NULL),(1057,'169.254.0.37',2,2,NULL,NULL,NULL),(1058,'169.254.0.38',2,2,NULL,NULL,NULL),(1059,'169.254.0.39',2,2,NULL,NULL,NULL),(1060,'169.254.0.40',2,2,NULL,NULL,NULL),(1061,'169.254.0.41',2,2,NULL,NULL,NULL),(1062,'169.254.0.42',2,2,NULL,NULL,NULL),(1063,'169.254.0.43',2,2,NULL,NULL,NULL),(1064,'169.254.0.44',2,2,NULL,NULL,NULL),(1065,'169.254.0.45',2,2,NULL,NULL,NULL),(1066,'169.254.0.46',2,2,NULL,NULL,NULL),(1067,'169.254.0.47',2,2,NULL,NULL,NULL),(1068,'169.254.0.48',2,2,NULL,NULL,NULL),(1069,'169.254.0.49',2,2,NULL,NULL,NULL),(1070,'169.254.0.50',2,2,NULL,NULL,NULL),(1071,'169.254.0.51',2,2,NULL,NULL,NULL),(1072,'169.254.0.52',2,2,NULL,NULL,NULL),(1073,'169.254.0.53',2,2,NULL,NULL,NULL),(1074,'169.254.0.54',2,2,NULL,NULL,NULL),(1075,'169.254.0.55',2,2,NULL,NULL,NULL),(1076,'169.254.0.56',2,2,NULL,NULL,NULL),(1077,'169.254.0.57',2,2,NULL,NULL,NULL),(1078,'169.254.0.58',2,2,NULL,NULL,NULL),(1079,'169.254.0.59',2,2,NULL,NULL,NULL),(1080,'169.254.0.60',2,2,NULL,NULL,NULL),(1081,'169.254.0.61',2,2,NULL,NULL,NULL),(1082,'169.254.0.62',2,2,NULL,NULL,NULL),(1083,'169.254.0.63',2,2,NULL,NULL,NULL),(1084,'169.254.0.64',2,2,NULL,NULL,NULL),(1085,'169.254.0.65',2,2,NULL,NULL,NULL),(1086,'169.254.0.66',2,2,NULL,NULL,NULL),(1087,'169.254.0.67',2,2,NULL,NULL,NULL),(1088,'169.254.0.68',2,2,NULL,NULL,NULL),(1089,'169.254.0.69',2,2,NULL,NULL,NULL),(1090,'169.254.0.70',2,2,NULL,NULL,NULL),(1091,'169.254.0.71',2,2,NULL,NULL,NULL),(1092,'169.254.0.72',2,2,NULL,NULL,NULL),(1093,'169.254.0.73',2,2,NULL,NULL,NULL),(1094,'169.254.0.74',2,2,NULL,NULL,NULL),(1095,'169.254.0.75',2,2,NULL,NULL,NULL),(1096,'169.254.0.76',2,2,NULL,NULL,NULL),(1097,'169.254.0.77',2,2,NULL,NULL,NULL),(1098,'169.254.0.78',2,2,NULL,NULL,NULL),(1099,'169.254.0.79',2,2,NULL,NULL,NULL),(1100,'169.254.0.80',2,2,NULL,NULL,NULL),(1101,'169.254.0.81',2,2,NULL,NULL,NULL),(1102,'169.254.0.82',2,2,NULL,NULL,NULL),(1103,'169.254.0.83',2,2,NULL,NULL,NULL),(1104,'169.254.0.84',2,2,NULL,NULL,NULL),(1105,'169.254.0.85',2,2,NULL,NULL,NULL),(1106,'169.254.0.86',2,2,NULL,NULL,NULL),(1107,'169.254.0.87',2,2,NULL,NULL,NULL),(1108,'169.254.0.88',2,2,NULL,NULL,NULL),(1109,'169.254.0.89',2,2,NULL,NULL,NULL),(1110,'169.254.0.90',2,2,NULL,NULL,NULL),(1111,'169.254.0.91',2,2,NULL,NULL,NULL),(1112,'169.254.0.92',2,2,NULL,NULL,NULL),(1113,'169.254.0.93',2,2,NULL,NULL,NULL),(1114,'169.254.0.94',2,2,NULL,NULL,NULL),(1115,'169.254.0.95',2,2,NULL,NULL,NULL),(1116,'169.254.0.96',2,2,NULL,NULL,NULL),(1117,'169.254.0.97',2,2,NULL,NULL,NULL),(1118,'169.254.0.98',2,2,NULL,NULL,NULL),(1119,'169.254.0.99',2,2,NULL,NULL,NULL),(1120,'169.254.0.100',2,2,NULL,NULL,NULL),(1121,'169.254.0.101',2,2,NULL,NULL,NULL),(1122,'169.254.0.102',2,2,NULL,NULL,NULL),(1123,'169.254.0.103',2,2,NULL,NULL,NULL),(1124,'169.254.0.104',2,2,NULL,NULL,NULL),(1125,'169.254.0.105',2,2,NULL,NULL,NULL),(1126,'169.254.0.106',2,2,NULL,NULL,NULL),(1127,'169.254.0.107',2,2,NULL,NULL,NULL),(1128,'169.254.0.108',2,2,NULL,NULL,NULL),(1129,'169.254.0.109',2,2,NULL,NULL,NULL),(1130,'169.254.0.110',2,2,NULL,NULL,NULL),(1131,'169.254.0.111',2,2,NULL,NULL,NULL),(1132,'169.254.0.112',2,2,NULL,NULL,NULL),(1133,'169.254.0.113',2,2,NULL,NULL,NULL),(1134,'169.254.0.114',2,2,NULL,NULL,NULL),(1135,'169.254.0.115',2,2,NULL,NULL,NULL),(1136,'169.254.0.116',2,2,NULL,NULL,NULL),(1137,'169.254.0.117',2,2,NULL,NULL,NULL),(1138,'169.254.0.118',2,2,NULL,NULL,NULL),(1139,'169.254.0.119',2,2,NULL,NULL,NULL),(1140,'169.254.0.120',2,2,NULL,NULL,NULL),(1141,'169.254.0.121',2,2,NULL,NULL,NULL),(1142,'169.254.0.122',2,2,NULL,NULL,NULL),(1143,'169.254.0.123',2,2,NULL,NULL,NULL),(1144,'169.254.0.124',2,2,NULL,NULL,NULL),(1145,'169.254.0.125',2,2,NULL,NULL,NULL),(1146,'169.254.0.126',2,2,NULL,NULL,NULL),(1147,'169.254.0.127',2,2,NULL,NULL,NULL),(1148,'169.254.0.128',2,2,NULL,NULL,NULL),(1149,'169.254.0.129',2,2,NULL,NULL,NULL),(1150,'169.254.0.130',2,2,NULL,NULL,NULL),(1151,'169.254.0.131',2,2,NULL,NULL,NULL),(1152,'169.254.0.132',2,2,NULL,NULL,NULL),(1153,'169.254.0.133',2,2,NULL,NULL,NULL),(1154,'169.254.0.134',2,2,NULL,NULL,NULL),(1155,'169.254.0.135',2,2,NULL,NULL,NULL),(1156,'169.254.0.136',2,2,NULL,NULL,NULL),(1157,'169.254.0.137',2,2,NULL,NULL,NULL),(1158,'169.254.0.138',2,2,NULL,NULL,NULL),(1159,'169.254.0.139',2,2,NULL,NULL,NULL),(1160,'169.254.0.140',2,2,NULL,NULL,NULL),(1161,'169.254.0.141',2,2,NULL,NULL,NULL),(1162,'169.254.0.142',2,2,NULL,NULL,NULL),(1163,'169.254.0.143',2,2,NULL,NULL,NULL),(1164,'169.254.0.144',2,2,NULL,NULL,NULL),(1165,'169.254.0.145',2,2,NULL,NULL,NULL),(1166,'169.254.0.146',2,2,NULL,NULL,NULL),(1167,'169.254.0.147',2,2,NULL,NULL,NULL),(1168,'169.254.0.148',2,2,NULL,NULL,NULL),(1169,'169.254.0.149',2,2,NULL,NULL,NULL),(1170,'169.254.0.150',2,2,NULL,NULL,NULL),(1171,'169.254.0.151',2,2,NULL,NULL,NULL),(1172,'169.254.0.152',2,2,NULL,NULL,NULL),(1173,'169.254.0.153',2,2,NULL,NULL,NULL),(1174,'169.254.0.154',2,2,NULL,NULL,NULL),(1175,'169.254.0.155',2,2,NULL,NULL,NULL),(1176,'169.254.0.156',2,2,NULL,NULL,NULL),(1177,'169.254.0.157',2,2,NULL,NULL,NULL),(1178,'169.254.0.158',2,2,NULL,NULL,NULL),(1179,'169.254.0.159',2,2,NULL,NULL,NULL),(1180,'169.254.0.160',2,2,NULL,NULL,NULL),(1181,'169.254.0.161',2,2,NULL,NULL,NULL),(1182,'169.254.0.162',2,2,NULL,NULL,NULL),(1183,'169.254.0.163',2,2,NULL,NULL,NULL),(1184,'169.254.0.164',2,2,NULL,NULL,NULL),(1185,'169.254.0.165',2,2,NULL,NULL,NULL),(1186,'169.254.0.166',2,2,NULL,NULL,NULL),(1187,'169.254.0.167',2,2,NULL,NULL,NULL),(1188,'169.254.0.168',2,2,NULL,NULL,NULL),(1189,'169.254.0.169',2,2,NULL,NULL,NULL),(1190,'169.254.0.170',2,2,NULL,NULL,NULL),(1191,'169.254.0.171',2,2,NULL,NULL,NULL),(1192,'169.254.0.172',2,2,NULL,NULL,NULL),(1193,'169.254.0.173',2,2,NULL,NULL,NULL),(1194,'169.254.0.174',2,2,NULL,NULL,NULL),(1195,'169.254.0.175',2,2,NULL,NULL,NULL),(1196,'169.254.0.176',2,2,NULL,NULL,NULL),(1197,'169.254.0.177',2,2,NULL,NULL,NULL),(1198,'169.254.0.178',2,2,NULL,NULL,NULL),(1199,'169.254.0.179',2,2,NULL,NULL,NULL),(1200,'169.254.0.180',2,2,NULL,NULL,NULL),(1201,'169.254.0.181',2,2,NULL,NULL,NULL),(1202,'169.254.0.182',2,2,NULL,NULL,NULL),(1203,'169.254.0.183',2,2,NULL,NULL,NULL),(1204,'169.254.0.184',2,2,NULL,NULL,NULL),(1205,'169.254.0.185',2,2,NULL,NULL,NULL),(1206,'169.254.0.186',2,2,63,'7400955e-f6c3-4482-b521-d2ac66ef7e99','2011-05-10 18:40:10'),(1207,'169.254.0.187',2,2,NULL,NULL,NULL),(1208,'169.254.0.188',2,2,NULL,NULL,NULL),(1209,'169.254.0.189',2,2,NULL,NULL,NULL),(1210,'169.254.0.190',2,2,NULL,NULL,NULL),(1211,'169.254.0.191',2,2,NULL,NULL,NULL),(1212,'169.254.0.192',2,2,NULL,NULL,NULL),(1213,'169.254.0.193',2,2,NULL,NULL,NULL),(1214,'169.254.0.194',2,2,NULL,NULL,NULL),(1215,'169.254.0.195',2,2,NULL,NULL,NULL),(1216,'169.254.0.196',2,2,NULL,NULL,NULL),(1217,'169.254.0.197',2,2,NULL,NULL,NULL),(1218,'169.254.0.198',2,2,NULL,NULL,NULL),(1219,'169.254.0.199',2,2,NULL,NULL,NULL),(1220,'169.254.0.200',2,2,NULL,NULL,NULL),(1221,'169.254.0.201',2,2,NULL,NULL,NULL),(1222,'169.254.0.202',2,2,NULL,NULL,NULL),(1223,'169.254.0.203',2,2,NULL,NULL,NULL),(1224,'169.254.0.204',2,2,NULL,NULL,NULL),(1225,'169.254.0.205',2,2,NULL,NULL,NULL),(1226,'169.254.0.206',2,2,NULL,NULL,NULL),(1227,'169.254.0.207',2,2,NULL,NULL,NULL),(1228,'169.254.0.208',2,2,NULL,NULL,NULL),(1229,'169.254.0.209',2,2,NULL,NULL,NULL),(1230,'169.254.0.210',2,2,NULL,NULL,NULL),(1231,'169.254.0.211',2,2,NULL,NULL,NULL),(1232,'169.254.0.212',2,2,NULL,NULL,NULL),(1233,'169.254.0.213',2,2,NULL,NULL,NULL),(1234,'169.254.0.214',2,2,NULL,NULL,NULL),(1235,'169.254.0.215',2,2,NULL,NULL,NULL),(1236,'169.254.0.216',2,2,NULL,NULL,NULL),(1237,'169.254.0.217',2,2,NULL,NULL,NULL),(1238,'169.254.0.218',2,2,NULL,NULL,NULL),(1239,'169.254.0.219',2,2,NULL,NULL,NULL),(1240,'169.254.0.220',2,2,NULL,NULL,NULL),(1241,'169.254.0.221',2,2,NULL,NULL,NULL),(1242,'169.254.0.222',2,2,NULL,NULL,NULL),(1243,'169.254.0.223',2,2,NULL,NULL,NULL),(1244,'169.254.0.224',2,2,NULL,NULL,NULL),(1245,'169.254.0.225',2,2,NULL,NULL,NULL),(1246,'169.254.0.226',2,2,NULL,NULL,NULL),(1247,'169.254.0.227',2,2,NULL,NULL,NULL),(1248,'169.254.0.228',2,2,NULL,NULL,NULL),(1249,'169.254.0.229',2,2,NULL,NULL,NULL),(1250,'169.254.0.230',2,2,NULL,NULL,NULL),(1251,'169.254.0.231',2,2,NULL,NULL,NULL),(1252,'169.254.0.232',2,2,NULL,NULL,NULL),(1253,'169.254.0.233',2,2,NULL,NULL,NULL),(1254,'169.254.0.234',2,2,NULL,NULL,NULL),(1255,'169.254.0.235',2,2,NULL,NULL,NULL),(1256,'169.254.0.236',2,2,NULL,NULL,NULL),(1257,'169.254.0.237',2,2,NULL,NULL,NULL),(1258,'169.254.0.238',2,2,NULL,NULL,NULL),(1259,'169.254.0.239',2,2,NULL,NULL,NULL),(1260,'169.254.0.240',2,2,NULL,NULL,NULL),(1261,'169.254.0.241',2,2,NULL,NULL,NULL),(1262,'169.254.0.242',2,2,NULL,NULL,NULL),(1263,'169.254.0.243',2,2,NULL,NULL,NULL),(1264,'169.254.0.244',2,2,NULL,NULL,NULL),(1265,'169.254.0.245',2,2,NULL,NULL,NULL),(1266,'169.254.0.246',2,2,NULL,NULL,NULL),(1267,'169.254.0.247',2,2,NULL,NULL,NULL),(1268,'169.254.0.248',2,2,NULL,NULL,NULL),(1269,'169.254.0.249',2,2,NULL,NULL,NULL),(1270,'169.254.0.250',2,2,NULL,NULL,NULL),(1271,'169.254.0.251',2,2,NULL,NULL,NULL),(1272,'169.254.0.252',2,2,NULL,NULL,NULL),(1273,'169.254.0.253',2,2,NULL,NULL,NULL),(1274,'169.254.0.254',2,2,NULL,NULL,NULL),(1275,'169.254.0.255',2,2,NULL,NULL,NULL),(1276,'169.254.1.0',2,2,NULL,NULL,NULL),(1277,'169.254.1.1',2,2,NULL,NULL,NULL),(1278,'169.254.1.2',2,2,NULL,NULL,NULL),(1279,'169.254.1.3',2,2,NULL,NULL,NULL),(1280,'169.254.1.4',2,2,NULL,NULL,NULL),(1281,'169.254.1.5',2,2,NULL,NULL,NULL),(1282,'169.254.1.6',2,2,NULL,NULL,NULL),(1283,'169.254.1.7',2,2,NULL,NULL,NULL),(1284,'169.254.1.8',2,2,NULL,NULL,NULL),(1285,'169.254.1.9',2,2,NULL,NULL,NULL),(1286,'169.254.1.10',2,2,NULL,NULL,NULL),(1287,'169.254.1.11',2,2,NULL,NULL,NULL),(1288,'169.254.1.12',2,2,NULL,NULL,NULL),(1289,'169.254.1.13',2,2,NULL,NULL,NULL),(1290,'169.254.1.14',2,2,NULL,NULL,NULL),(1291,'169.254.1.15',2,2,NULL,NULL,NULL),(1292,'169.254.1.16',2,2,NULL,NULL,NULL),(1293,'169.254.1.17',2,2,NULL,NULL,NULL),(1294,'169.254.1.18',2,2,NULL,NULL,NULL),(1295,'169.254.1.19',2,2,NULL,NULL,NULL),(1296,'169.254.1.20',2,2,NULL,NULL,NULL),(1297,'169.254.1.21',2,2,NULL,NULL,NULL),(1298,'169.254.1.22',2,2,NULL,NULL,NULL),(1299,'169.254.1.23',2,2,NULL,NULL,NULL),(1300,'169.254.1.24',2,2,NULL,NULL,NULL),(1301,'169.254.1.25',2,2,NULL,NULL,NULL),(1302,'169.254.1.26',2,2,NULL,NULL,NULL),(1303,'169.254.1.27',2,2,NULL,NULL,NULL),(1304,'169.254.1.28',2,2,NULL,NULL,NULL),(1305,'169.254.1.29',2,2,NULL,NULL,NULL),(1306,'169.254.1.30',2,2,NULL,NULL,NULL),(1307,'169.254.1.31',2,2,NULL,NULL,NULL),(1308,'169.254.1.32',2,2,NULL,NULL,NULL),(1309,'169.254.1.33',2,2,NULL,NULL,NULL),(1310,'169.254.1.34',2,2,NULL,NULL,NULL),(1311,'169.254.1.35',2,2,NULL,NULL,NULL),(1312,'169.254.1.36',2,2,NULL,NULL,NULL),(1313,'169.254.1.37',2,2,NULL,NULL,NULL),(1314,'169.254.1.38',2,2,NULL,NULL,NULL),(1315,'169.254.1.39',2,2,NULL,NULL,NULL),(1316,'169.254.1.40',2,2,NULL,NULL,NULL),(1317,'169.254.1.41',2,2,NULL,NULL,NULL),(1318,'169.254.1.42',2,2,NULL,NULL,NULL),(1319,'169.254.1.43',2,2,NULL,NULL,NULL),(1320,'169.254.1.44',2,2,NULL,NULL,NULL),(1321,'169.254.1.45',2,2,NULL,NULL,NULL),(1322,'169.254.1.46',2,2,NULL,NULL,NULL),(1323,'169.254.1.47',2,2,NULL,NULL,NULL),(1324,'169.254.1.48',2,2,NULL,NULL,NULL),(1325,'169.254.1.49',2,2,NULL,NULL,NULL),(1326,'169.254.1.50',2,2,NULL,NULL,NULL),(1327,'169.254.1.51',2,2,NULL,NULL,NULL),(1328,'169.254.1.52',2,2,NULL,NULL,NULL),(1329,'169.254.1.53',2,2,NULL,NULL,NULL),(1330,'169.254.1.54',2,2,NULL,NULL,NULL),(1331,'169.254.1.55',2,2,NULL,NULL,NULL),(1332,'169.254.1.56',2,2,NULL,NULL,NULL),(1333,'169.254.1.57',2,2,NULL,NULL,NULL),(1334,'169.254.1.58',2,2,NULL,NULL,NULL),(1335,'169.254.1.59',2,2,NULL,NULL,NULL),(1336,'169.254.1.60',2,2,NULL,NULL,NULL),(1337,'169.254.1.61',2,2,NULL,NULL,NULL),(1338,'169.254.1.62',2,2,NULL,NULL,NULL),(1339,'169.254.1.63',2,2,NULL,NULL,NULL),(1340,'169.254.1.64',2,2,NULL,NULL,NULL),(1341,'169.254.1.65',2,2,NULL,NULL,NULL),(1342,'169.254.1.66',2,2,NULL,NULL,NULL),(1343,'169.254.1.67',2,2,NULL,NULL,NULL),(1344,'169.254.1.68',2,2,NULL,NULL,NULL),(1345,'169.254.1.69',2,2,NULL,NULL,NULL),(1346,'169.254.1.70',2,2,NULL,NULL,NULL),(1347,'169.254.1.71',2,2,NULL,NULL,NULL),(1348,'169.254.1.72',2,2,NULL,NULL,NULL),(1349,'169.254.1.73',2,2,NULL,NULL,NULL),(1350,'169.254.1.74',2,2,NULL,NULL,NULL),(1351,'169.254.1.75',2,2,NULL,NULL,NULL),(1352,'169.254.1.76',2,2,NULL,NULL,NULL),(1353,'169.254.1.77',2,2,NULL,NULL,NULL),(1354,'169.254.1.78',2,2,NULL,NULL,NULL),(1355,'169.254.1.79',2,2,NULL,NULL,NULL),(1356,'169.254.1.80',2,2,NULL,NULL,NULL),(1357,'169.254.1.81',2,2,NULL,NULL,NULL),(1358,'169.254.1.82',2,2,NULL,NULL,NULL),(1359,'169.254.1.83',2,2,NULL,NULL,NULL),(1360,'169.254.1.84',2,2,NULL,NULL,NULL),(1361,'169.254.1.85',2,2,NULL,NULL,NULL),(1362,'169.254.1.86',2,2,NULL,NULL,NULL),(1363,'169.254.1.87',2,2,NULL,NULL,NULL),(1364,'169.254.1.88',2,2,NULL,NULL,NULL),(1365,'169.254.1.89',2,2,NULL,NULL,NULL),(1366,'169.254.1.90',2,2,NULL,NULL,NULL),(1367,'169.254.1.91',2,2,NULL,NULL,NULL),(1368,'169.254.1.92',2,2,NULL,NULL,NULL),(1369,'169.254.1.93',2,2,NULL,NULL,NULL),(1370,'169.254.1.94',2,2,NULL,NULL,NULL),(1371,'169.254.1.95',2,2,NULL,NULL,NULL),(1372,'169.254.1.96',2,2,NULL,NULL,NULL),(1373,'169.254.1.97',2,2,NULL,NULL,NULL),(1374,'169.254.1.98',2,2,NULL,NULL,NULL),(1375,'169.254.1.99',2,2,NULL,NULL,NULL),(1376,'169.254.1.100',2,2,NULL,NULL,NULL),(1377,'169.254.1.101',2,2,NULL,NULL,NULL),(1378,'169.254.1.102',2,2,NULL,NULL,NULL),(1379,'169.254.1.103',2,2,NULL,NULL,NULL),(1380,'169.254.1.104',2,2,NULL,NULL,NULL),(1381,'169.254.1.105',2,2,NULL,NULL,NULL),(1382,'169.254.1.106',2,2,NULL,NULL,NULL),(1383,'169.254.1.107',2,2,NULL,NULL,NULL),(1384,'169.254.1.108',2,2,NULL,NULL,NULL),(1385,'169.254.1.109',2,2,NULL,NULL,NULL),(1386,'169.254.1.110',2,2,NULL,NULL,NULL),(1387,'169.254.1.111',2,2,NULL,NULL,NULL),(1388,'169.254.1.112',2,2,NULL,NULL,NULL),(1389,'169.254.1.113',2,2,NULL,NULL,NULL),(1390,'169.254.1.114',2,2,NULL,NULL,NULL),(1391,'169.254.1.115',2,2,NULL,NULL,NULL),(1392,'169.254.1.116',2,2,NULL,NULL,NULL),(1393,'169.254.1.117',2,2,NULL,NULL,NULL),(1394,'169.254.1.118',2,2,NULL,NULL,NULL),(1395,'169.254.1.119',2,2,NULL,NULL,NULL),(1396,'169.254.1.120',2,2,NULL,NULL,NULL),(1397,'169.254.1.121',2,2,NULL,NULL,NULL),(1398,'169.254.1.122',2,2,NULL,NULL,NULL),(1399,'169.254.1.123',2,2,NULL,NULL,NULL),(1400,'169.254.1.124',2,2,NULL,NULL,NULL),(1401,'169.254.1.125',2,2,NULL,NULL,NULL),(1402,'169.254.1.126',2,2,NULL,NULL,NULL),(1403,'169.254.1.127',2,2,NULL,NULL,NULL),(1404,'169.254.1.128',2,2,NULL,NULL,NULL),(1405,'169.254.1.129',2,2,NULL,NULL,NULL),(1406,'169.254.1.130',2,2,60,'c4dac56f-e6d8-48b7-b83d-29162b48bc33','2011-05-10 18:29:34'),(1407,'169.254.1.131',2,2,NULL,NULL,NULL),(1408,'169.254.1.132',2,2,NULL,NULL,NULL),(1409,'169.254.1.133',2,2,NULL,NULL,NULL),(1410,'169.254.1.134',2,2,NULL,NULL,NULL),(1411,'169.254.1.135',2,2,NULL,NULL,NULL),(1412,'169.254.1.136',2,2,NULL,NULL,NULL),(1413,'169.254.1.137',2,2,NULL,NULL,NULL),(1414,'169.254.1.138',2,2,NULL,NULL,NULL),(1415,'169.254.1.139',2,2,NULL,NULL,NULL),(1416,'169.254.1.140',2,2,NULL,NULL,NULL),(1417,'169.254.1.141',2,2,NULL,NULL,NULL),(1418,'169.254.1.142',2,2,NULL,NULL,NULL),(1419,'169.254.1.143',2,2,NULL,NULL,NULL),(1420,'169.254.1.144',2,2,NULL,NULL,NULL),(1421,'169.254.1.145',2,2,NULL,NULL,NULL),(1422,'169.254.1.146',2,2,NULL,NULL,NULL),(1423,'169.254.1.147',2,2,NULL,NULL,NULL),(1424,'169.254.1.148',2,2,NULL,NULL,NULL),(1425,'169.254.1.149',2,2,NULL,NULL,NULL),(1426,'169.254.1.150',2,2,NULL,NULL,NULL),(1427,'169.254.1.151',2,2,NULL,NULL,NULL),(1428,'169.254.1.152',2,2,NULL,NULL,NULL),(1429,'169.254.1.153',2,2,NULL,NULL,NULL),(1430,'169.254.1.154',2,2,NULL,NULL,NULL),(1431,'169.254.1.155',2,2,NULL,NULL,NULL),(1432,'169.254.1.156',2,2,NULL,NULL,NULL),(1433,'169.254.1.157',2,2,NULL,NULL,NULL),(1434,'169.254.1.158',2,2,NULL,NULL,NULL),(1435,'169.254.1.159',2,2,NULL,NULL,NULL),(1436,'169.254.1.160',2,2,NULL,NULL,NULL),(1437,'169.254.1.161',2,2,NULL,NULL,NULL),(1438,'169.254.1.162',2,2,NULL,NULL,NULL),(1439,'169.254.1.163',2,2,NULL,NULL,NULL),(1440,'169.254.1.164',2,2,NULL,NULL,NULL),(1441,'169.254.1.165',2,2,NULL,NULL,NULL),(1442,'169.254.1.166',2,2,NULL,NULL,NULL),(1443,'169.254.1.167',2,2,NULL,NULL,NULL),(1444,'169.254.1.168',2,2,NULL,NULL,NULL),(1445,'169.254.1.169',2,2,NULL,NULL,NULL),(1446,'169.254.1.170',2,2,NULL,NULL,NULL),(1447,'169.254.1.171',2,2,NULL,NULL,NULL),(1448,'169.254.1.172',2,2,NULL,NULL,NULL),(1449,'169.254.1.173',2,2,NULL,NULL,NULL),(1450,'169.254.1.174',2,2,NULL,NULL,NULL),(1451,'169.254.1.175',2,2,NULL,NULL,NULL),(1452,'169.254.1.176',2,2,NULL,NULL,NULL),(1453,'169.254.1.177',2,2,NULL,NULL,NULL),(1454,'169.254.1.178',2,2,NULL,NULL,NULL),(1455,'169.254.1.179',2,2,NULL,NULL,NULL),(1456,'169.254.1.180',2,2,NULL,NULL,NULL),(1457,'169.254.1.181',2,2,NULL,NULL,NULL),(1458,'169.254.1.182',2,2,NULL,NULL,NULL),(1459,'169.254.1.183',2,2,NULL,NULL,NULL),(1460,'169.254.1.184',2,2,NULL,NULL,NULL),(1461,'169.254.1.185',2,2,NULL,NULL,NULL),(1462,'169.254.1.186',2,2,NULL,NULL,NULL),(1463,'169.254.1.187',2,2,NULL,NULL,NULL),(1464,'169.254.1.188',2,2,NULL,NULL,NULL),(1465,'169.254.1.189',2,2,NULL,NULL,NULL),(1466,'169.254.1.190',2,2,NULL,NULL,NULL),(1467,'169.254.1.191',2,2,NULL,NULL,NULL),(1468,'169.254.1.192',2,2,NULL,NULL,NULL),(1469,'169.254.1.193',2,2,NULL,NULL,NULL),(1470,'169.254.1.194',2,2,NULL,NULL,NULL),(1471,'169.254.1.195',2,2,NULL,NULL,NULL),(1472,'169.254.1.196',2,2,NULL,NULL,NULL),(1473,'169.254.1.197',2,2,NULL,NULL,NULL),(1474,'169.254.1.198',2,2,NULL,NULL,NULL),(1475,'169.254.1.199',2,2,NULL,NULL,NULL),(1476,'169.254.1.200',2,2,NULL,NULL,NULL),(1477,'169.254.1.201',2,2,NULL,NULL,NULL),(1478,'169.254.1.202',2,2,NULL,NULL,NULL),(1479,'169.254.1.203',2,2,NULL,NULL,NULL),(1480,'169.254.1.204',2,2,NULL,NULL,NULL),(1481,'169.254.1.205',2,2,NULL,NULL,NULL),(1482,'169.254.1.206',2,2,NULL,NULL,NULL),(1483,'169.254.1.207',2,2,NULL,NULL,NULL),(1484,'169.254.1.208',2,2,NULL,NULL,NULL),(1485,'169.254.1.209',2,2,NULL,NULL,NULL),(1486,'169.254.1.210',2,2,NULL,NULL,NULL),(1487,'169.254.1.211',2,2,NULL,NULL,NULL),(1488,'169.254.1.212',2,2,NULL,NULL,NULL),(1489,'169.254.1.213',2,2,NULL,NULL,NULL),(1490,'169.254.1.214',2,2,NULL,NULL,NULL),(1491,'169.254.1.215',2,2,NULL,NULL,NULL),(1492,'169.254.1.216',2,2,NULL,NULL,NULL),(1493,'169.254.1.217',2,2,NULL,NULL,NULL),(1494,'169.254.1.218',2,2,NULL,NULL,NULL),(1495,'169.254.1.219',2,2,NULL,NULL,NULL),(1496,'169.254.1.220',2,2,NULL,NULL,NULL),(1497,'169.254.1.221',2,2,NULL,NULL,NULL),(1498,'169.254.1.222',2,2,NULL,NULL,NULL),(1499,'169.254.1.223',2,2,NULL,NULL,NULL),(1500,'169.254.1.224',2,2,NULL,NULL,NULL),(1501,'169.254.1.225',2,2,NULL,NULL,NULL),(1502,'169.254.1.226',2,2,NULL,NULL,NULL),(1503,'169.254.1.227',2,2,NULL,NULL,NULL),(1504,'169.254.1.228',2,2,NULL,NULL,NULL),(1505,'169.254.1.229',2,2,NULL,NULL,NULL),(1506,'169.254.1.230',2,2,NULL,NULL,NULL),(1507,'169.254.1.231',2,2,NULL,NULL,NULL),(1508,'169.254.1.232',2,2,NULL,NULL,NULL),(1509,'169.254.1.233',2,2,NULL,NULL,NULL),(1510,'169.254.1.234',2,2,NULL,NULL,NULL),(1511,'169.254.1.235',2,2,NULL,NULL,NULL),(1512,'169.254.1.236',2,2,NULL,NULL,NULL),(1513,'169.254.1.237',2,2,NULL,NULL,NULL),(1514,'169.254.1.238',2,2,NULL,NULL,NULL),(1515,'169.254.1.239',2,2,NULL,NULL,NULL),(1516,'169.254.1.240',2,2,NULL,NULL,NULL),(1517,'169.254.1.241',2,2,NULL,NULL,NULL),(1518,'169.254.1.242',2,2,NULL,NULL,NULL),(1519,'169.254.1.243',2,2,NULL,NULL,NULL),(1520,'169.254.1.244',2,2,NULL,NULL,NULL),(1521,'169.254.1.245',2,2,NULL,NULL,NULL),(1522,'169.254.1.246',2,2,NULL,NULL,NULL),(1523,'169.254.1.247',2,2,NULL,NULL,NULL),(1524,'169.254.1.248',2,2,NULL,NULL,NULL),(1525,'169.254.1.249',2,2,NULL,NULL,NULL),(1526,'169.254.1.250',2,2,NULL,NULL,NULL),(1527,'169.254.1.251',2,2,NULL,NULL,NULL),(1528,'169.254.1.252',2,2,NULL,NULL,NULL),(1529,'169.254.1.253',2,2,NULL,NULL,NULL),(1530,'169.254.1.254',2,2,NULL,NULL,NULL),(1531,'169.254.1.255',2,2,NULL,NULL,NULL),(1532,'169.254.2.0',2,2,NULL,NULL,NULL),(1533,'169.254.2.1',2,2,NULL,NULL,NULL),(1534,'169.254.2.2',2,2,NULL,NULL,NULL),(1535,'169.254.2.3',2,2,NULL,NULL,NULL),(1536,'169.254.2.4',2,2,NULL,NULL,NULL),(1537,'169.254.2.5',2,2,NULL,NULL,NULL),(1538,'169.254.2.6',2,2,NULL,NULL,NULL),(1539,'169.254.2.7',2,2,NULL,NULL,NULL),(1540,'169.254.2.8',2,2,NULL,NULL,NULL),(1541,'169.254.2.9',2,2,NULL,NULL,NULL),(1542,'169.254.2.10',2,2,NULL,NULL,NULL),(1543,'169.254.2.11',2,2,NULL,NULL,NULL),(1544,'169.254.2.12',2,2,NULL,NULL,NULL),(1545,'169.254.2.13',2,2,NULL,NULL,NULL),(1546,'169.254.2.14',2,2,NULL,NULL,NULL),(1547,'169.254.2.15',2,2,NULL,NULL,NULL),(1548,'169.254.2.16',2,2,NULL,NULL,NULL),(1549,'169.254.2.17',2,2,NULL,NULL,NULL),(1550,'169.254.2.18',2,2,NULL,NULL,NULL),(1551,'169.254.2.19',2,2,NULL,NULL,NULL),(1552,'169.254.2.20',2,2,NULL,NULL,NULL),(1553,'169.254.2.21',2,2,NULL,NULL,NULL),(1554,'169.254.2.22',2,2,NULL,NULL,NULL),(1555,'169.254.2.23',2,2,NULL,NULL,NULL),(1556,'169.254.2.24',2,2,NULL,NULL,NULL),(1557,'169.254.2.25',2,2,NULL,NULL,NULL),(1558,'169.254.2.26',2,2,NULL,NULL,NULL),(1559,'169.254.2.27',2,2,NULL,NULL,NULL),(1560,'169.254.2.28',2,2,NULL,NULL,NULL),(1561,'169.254.2.29',2,2,NULL,NULL,NULL),(1562,'169.254.2.30',2,2,NULL,NULL,NULL),(1563,'169.254.2.31',2,2,NULL,NULL,NULL),(1564,'169.254.2.32',2,2,NULL,NULL,NULL),(1565,'169.254.2.33',2,2,NULL,NULL,NULL),(1566,'169.254.2.34',2,2,NULL,NULL,NULL),(1567,'169.254.2.35',2,2,NULL,NULL,NULL),(1568,'169.254.2.36',2,2,NULL,NULL,NULL),(1569,'169.254.2.37',2,2,NULL,NULL,NULL),(1570,'169.254.2.38',2,2,NULL,NULL,NULL),(1571,'169.254.2.39',2,2,NULL,NULL,NULL),(1572,'169.254.2.40',2,2,NULL,NULL,NULL),(1573,'169.254.2.41',2,2,NULL,NULL,NULL),(1574,'169.254.2.42',2,2,NULL,NULL,NULL),(1575,'169.254.2.43',2,2,NULL,NULL,NULL),(1576,'169.254.2.44',2,2,NULL,NULL,NULL),(1577,'169.254.2.45',2,2,NULL,NULL,NULL),(1578,'169.254.2.46',2,2,NULL,NULL,NULL),(1579,'169.254.2.47',2,2,NULL,NULL,NULL),(1580,'169.254.2.48',2,2,NULL,NULL,NULL),(1581,'169.254.2.49',2,2,NULL,NULL,NULL),(1582,'169.254.2.50',2,2,NULL,NULL,NULL),(1583,'169.254.2.51',2,2,NULL,NULL,NULL),(1584,'169.254.2.52',2,2,NULL,NULL,NULL),(1585,'169.254.2.53',2,2,NULL,NULL,NULL),(1586,'169.254.2.54',2,2,NULL,NULL,NULL),(1587,'169.254.2.55',2,2,NULL,NULL,NULL),(1588,'169.254.2.56',2,2,NULL,NULL,NULL),(1589,'169.254.2.57',2,2,NULL,NULL,NULL),(1590,'169.254.2.58',2,2,NULL,NULL,NULL),(1591,'169.254.2.59',2,2,NULL,NULL,NULL),(1592,'169.254.2.60',2,2,NULL,NULL,NULL),(1593,'169.254.2.61',2,2,NULL,NULL,NULL),(1594,'169.254.2.62',2,2,NULL,NULL,NULL),(1595,'169.254.2.63',2,2,NULL,NULL,NULL),(1596,'169.254.2.64',2,2,NULL,NULL,NULL),(1597,'169.254.2.65',2,2,NULL,NULL,NULL),(1598,'169.254.2.66',2,2,NULL,NULL,NULL),(1599,'169.254.2.67',2,2,NULL,NULL,NULL),(1600,'169.254.2.68',2,2,NULL,NULL,NULL),(1601,'169.254.2.69',2,2,NULL,NULL,NULL),(1602,'169.254.2.70',2,2,NULL,NULL,NULL),(1603,'169.254.2.71',2,2,NULL,NULL,NULL),(1604,'169.254.2.72',2,2,NULL,NULL,NULL),(1605,'169.254.2.73',2,2,NULL,NULL,NULL),(1606,'169.254.2.74',2,2,NULL,NULL,NULL),(1607,'169.254.2.75',2,2,NULL,NULL,NULL),(1608,'169.254.2.76',2,2,NULL,NULL,NULL),(1609,'169.254.2.77',2,2,NULL,NULL,NULL),(1610,'169.254.2.78',2,2,NULL,NULL,NULL),(1611,'169.254.2.79',2,2,NULL,NULL,NULL),(1612,'169.254.2.80',2,2,NULL,NULL,NULL),(1613,'169.254.2.81',2,2,NULL,NULL,NULL),(1614,'169.254.2.82',2,2,NULL,NULL,NULL),(1615,'169.254.2.83',2,2,NULL,NULL,NULL),(1616,'169.254.2.84',2,2,NULL,NULL,NULL),(1617,'169.254.2.85',2,2,NULL,NULL,NULL),(1618,'169.254.2.86',2,2,NULL,NULL,NULL),(1619,'169.254.2.87',2,2,NULL,NULL,NULL),(1620,'169.254.2.88',2,2,NULL,NULL,NULL),(1621,'169.254.2.89',2,2,NULL,NULL,NULL),(1622,'169.254.2.90',2,2,NULL,NULL,NULL),(1623,'169.254.2.91',2,2,NULL,NULL,NULL),(1624,'169.254.2.92',2,2,NULL,NULL,NULL),(1625,'169.254.2.93',2,2,NULL,NULL,NULL),(1626,'169.254.2.94',2,2,NULL,NULL,NULL),(1627,'169.254.2.95',2,2,NULL,NULL,NULL),(1628,'169.254.2.96',2,2,NULL,NULL,NULL),(1629,'169.254.2.97',2,2,NULL,NULL,NULL),(1630,'169.254.2.98',2,2,NULL,NULL,NULL),(1631,'169.254.2.99',2,2,NULL,NULL,NULL),(1632,'169.254.2.100',2,2,NULL,NULL,NULL),(1633,'169.254.2.101',2,2,NULL,NULL,NULL),(1634,'169.254.2.102',2,2,NULL,NULL,NULL),(1635,'169.254.2.103',2,2,NULL,NULL,NULL),(1636,'169.254.2.104',2,2,NULL,NULL,NULL),(1637,'169.254.2.105',2,2,NULL,NULL,NULL),(1638,'169.254.2.106',2,2,NULL,NULL,NULL),(1639,'169.254.2.107',2,2,NULL,NULL,NULL),(1640,'169.254.2.108',2,2,NULL,NULL,NULL),(1641,'169.254.2.109',2,2,NULL,NULL,NULL),(1642,'169.254.2.110',2,2,NULL,NULL,NULL),(1643,'169.254.2.111',2,2,NULL,NULL,NULL),(1644,'169.254.2.112',2,2,NULL,NULL,NULL),(1645,'169.254.2.113',2,2,NULL,NULL,NULL),(1646,'169.254.2.114',2,2,NULL,NULL,NULL),(1647,'169.254.2.115',2,2,NULL,NULL,NULL),(1648,'169.254.2.116',2,2,NULL,NULL,NULL),(1649,'169.254.2.117',2,2,NULL,NULL,NULL),(1650,'169.254.2.118',2,2,NULL,NULL,NULL),(1651,'169.254.2.119',2,2,NULL,NULL,NULL),(1652,'169.254.2.120',2,2,NULL,NULL,NULL),(1653,'169.254.2.121',2,2,NULL,NULL,NULL),(1654,'169.254.2.122',2,2,NULL,NULL,NULL),(1655,'169.254.2.123',2,2,NULL,NULL,NULL),(1656,'169.254.2.124',2,2,NULL,NULL,NULL),(1657,'169.254.2.125',2,2,NULL,NULL,NULL),(1658,'169.254.2.126',2,2,NULL,NULL,NULL),(1659,'169.254.2.127',2,2,NULL,NULL,NULL),(1660,'169.254.2.128',2,2,NULL,NULL,NULL),(1661,'169.254.2.129',2,2,NULL,NULL,NULL),(1662,'169.254.2.130',2,2,NULL,NULL,NULL),(1663,'169.254.2.131',2,2,NULL,NULL,NULL),(1664,'169.254.2.132',2,2,NULL,NULL,NULL),(1665,'169.254.2.133',2,2,NULL,NULL,NULL),(1666,'169.254.2.134',2,2,NULL,NULL,NULL),(1667,'169.254.2.135',2,2,NULL,NULL,NULL),(1668,'169.254.2.136',2,2,NULL,NULL,NULL),(1669,'169.254.2.137',2,2,NULL,NULL,NULL),(1670,'169.254.2.138',2,2,NULL,NULL,NULL),(1671,'169.254.2.139',2,2,NULL,NULL,NULL),(1672,'169.254.2.140',2,2,NULL,NULL,NULL),(1673,'169.254.2.141',2,2,NULL,NULL,NULL),(1674,'169.254.2.142',2,2,NULL,NULL,NULL),(1675,'169.254.2.143',2,2,NULL,NULL,NULL),(1676,'169.254.2.144',2,2,NULL,NULL,NULL),(1677,'169.254.2.145',2,2,NULL,NULL,NULL),(1678,'169.254.2.146',2,2,NULL,NULL,NULL),(1679,'169.254.2.147',2,2,NULL,NULL,NULL),(1680,'169.254.2.148',2,2,NULL,NULL,NULL),(1681,'169.254.2.149',2,2,NULL,NULL,NULL),(1682,'169.254.2.150',2,2,NULL,NULL,NULL),(1683,'169.254.2.151',2,2,NULL,NULL,NULL),(1684,'169.254.2.152',2,2,NULL,NULL,NULL),(1685,'169.254.2.153',2,2,NULL,NULL,NULL),(1686,'169.254.2.154',2,2,NULL,NULL,NULL),(1687,'169.254.2.155',2,2,NULL,NULL,NULL),(1688,'169.254.2.156',2,2,NULL,NULL,NULL),(1689,'169.254.2.157',2,2,NULL,NULL,NULL),(1690,'169.254.2.158',2,2,NULL,NULL,NULL),(1691,'169.254.2.159',2,2,NULL,NULL,NULL),(1692,'169.254.2.160',2,2,NULL,NULL,NULL),(1693,'169.254.2.161',2,2,NULL,NULL,NULL),(1694,'169.254.2.162',2,2,NULL,NULL,NULL),(1695,'169.254.2.163',2,2,NULL,NULL,NULL),(1696,'169.254.2.164',2,2,NULL,NULL,NULL),(1697,'169.254.2.165',2,2,NULL,NULL,NULL),(1698,'169.254.2.166',2,2,NULL,NULL,NULL),(1699,'169.254.2.167',2,2,NULL,NULL,NULL),(1700,'169.254.2.168',2,2,NULL,NULL,NULL),(1701,'169.254.2.169',2,2,NULL,NULL,NULL),(1702,'169.254.2.170',2,2,NULL,NULL,NULL),(1703,'169.254.2.171',2,2,NULL,NULL,NULL),(1704,'169.254.2.172',2,2,NULL,NULL,NULL),(1705,'169.254.2.173',2,2,NULL,NULL,NULL),(1706,'169.254.2.174',2,2,NULL,NULL,NULL),(1707,'169.254.2.175',2,2,NULL,NULL,NULL),(1708,'169.254.2.176',2,2,NULL,NULL,NULL),(1709,'169.254.2.177',2,2,NULL,NULL,NULL),(1710,'169.254.2.178',2,2,NULL,NULL,NULL),(1711,'169.254.2.179',2,2,NULL,NULL,NULL),(1712,'169.254.2.180',2,2,NULL,NULL,NULL),(1713,'169.254.2.181',2,2,NULL,NULL,NULL),(1714,'169.254.2.182',2,2,NULL,NULL,NULL),(1715,'169.254.2.183',2,2,NULL,NULL,NULL),(1716,'169.254.2.184',2,2,NULL,NULL,NULL),(1717,'169.254.2.185',2,2,NULL,NULL,NULL),(1718,'169.254.2.186',2,2,NULL,NULL,NULL),(1719,'169.254.2.187',2,2,NULL,NULL,NULL),(1720,'169.254.2.188',2,2,NULL,NULL,NULL),(1721,'169.254.2.189',2,2,NULL,NULL,NULL),(1722,'169.254.2.190',2,2,NULL,NULL,NULL),(1723,'169.254.2.191',2,2,NULL,NULL,NULL),(1724,'169.254.2.192',2,2,NULL,NULL,NULL),(1725,'169.254.2.193',2,2,NULL,NULL,NULL),(1726,'169.254.2.194',2,2,NULL,NULL,NULL),(1727,'169.254.2.195',2,2,NULL,NULL,NULL),(1728,'169.254.2.196',2,2,NULL,NULL,NULL),(1729,'169.254.2.197',2,2,NULL,NULL,NULL),(1730,'169.254.2.198',2,2,NULL,NULL,NULL),(1731,'169.254.2.199',2,2,NULL,NULL,NULL),(1732,'169.254.2.200',2,2,NULL,NULL,NULL),(1733,'169.254.2.201',2,2,NULL,NULL,NULL),(1734,'169.254.2.202',2,2,NULL,NULL,NULL),(1735,'169.254.2.203',2,2,NULL,NULL,NULL),(1736,'169.254.2.204',2,2,NULL,NULL,NULL),(1737,'169.254.2.205',2,2,NULL,NULL,NULL),(1738,'169.254.2.206',2,2,NULL,NULL,NULL),(1739,'169.254.2.207',2,2,NULL,NULL,NULL),(1740,'169.254.2.208',2,2,NULL,NULL,NULL),(1741,'169.254.2.209',2,2,NULL,NULL,NULL),(1742,'169.254.2.210',2,2,NULL,NULL,NULL),(1743,'169.254.2.211',2,2,NULL,NULL,NULL),(1744,'169.254.2.212',2,2,NULL,NULL,NULL),(1745,'169.254.2.213',2,2,NULL,NULL,NULL),(1746,'169.254.2.214',2,2,NULL,NULL,NULL),(1747,'169.254.2.215',2,2,NULL,NULL,NULL),(1748,'169.254.2.216',2,2,NULL,NULL,NULL),(1749,'169.254.2.217',2,2,NULL,NULL,NULL),(1750,'169.254.2.218',2,2,NULL,NULL,NULL),(1751,'169.254.2.219',2,2,NULL,NULL,NULL),(1752,'169.254.2.220',2,2,NULL,NULL,NULL),(1753,'169.254.2.221',2,2,NULL,NULL,NULL),(1754,'169.254.2.222',2,2,NULL,NULL,NULL),(1755,'169.254.2.223',2,2,NULL,NULL,NULL),(1756,'169.254.2.224',2,2,NULL,NULL,NULL),(1757,'169.254.2.225',2,2,NULL,NULL,NULL),(1758,'169.254.2.226',2,2,NULL,NULL,NULL),(1759,'169.254.2.227',2,2,NULL,NULL,NULL),(1760,'169.254.2.228',2,2,NULL,NULL,NULL),(1761,'169.254.2.229',2,2,NULL,NULL,NULL),(1762,'169.254.2.230',2,2,NULL,NULL,NULL),(1763,'169.254.2.231',2,2,NULL,NULL,NULL),(1764,'169.254.2.232',2,2,NULL,NULL,NULL),(1765,'169.254.2.233',2,2,NULL,NULL,NULL),(1766,'169.254.2.234',2,2,NULL,NULL,NULL),(1767,'169.254.2.235',2,2,NULL,NULL,NULL),(1768,'169.254.2.236',2,2,NULL,NULL,NULL),(1769,'169.254.2.237',2,2,NULL,NULL,NULL),(1770,'169.254.2.238',2,2,NULL,NULL,NULL),(1771,'169.254.2.239',2,2,NULL,NULL,NULL),(1772,'169.254.2.240',2,2,NULL,NULL,NULL),(1773,'169.254.2.241',2,2,NULL,NULL,NULL),(1774,'169.254.2.242',2,2,NULL,NULL,NULL),(1775,'169.254.2.243',2,2,NULL,NULL,NULL),(1776,'169.254.2.244',2,2,NULL,NULL,NULL),(1777,'169.254.2.245',2,2,NULL,NULL,NULL),(1778,'169.254.2.246',2,2,NULL,NULL,NULL),(1779,'169.254.2.247',2,2,NULL,NULL,NULL),(1780,'169.254.2.248',2,2,NULL,NULL,NULL),(1781,'169.254.2.249',2,2,NULL,NULL,NULL),(1782,'169.254.2.250',2,2,NULL,NULL,NULL),(1783,'169.254.2.251',2,2,NULL,NULL,NULL),(1784,'169.254.2.252',2,2,NULL,NULL,NULL),(1785,'169.254.2.253',2,2,NULL,NULL,NULL),(1786,'169.254.2.254',2,2,NULL,NULL,NULL),(1787,'169.254.2.255',2,2,NULL,NULL,NULL),(1788,'169.254.3.0',2,2,NULL,NULL,NULL),(1789,'169.254.3.1',2,2,NULL,NULL,NULL),(1790,'169.254.3.2',2,2,NULL,NULL,NULL),(1791,'169.254.3.3',2,2,NULL,NULL,NULL),(1792,'169.254.3.4',2,2,NULL,NULL,NULL),(1793,'169.254.3.5',2,2,NULL,NULL,NULL),(1794,'169.254.3.6',2,2,NULL,NULL,NULL),(1795,'169.254.3.7',2,2,NULL,NULL,NULL),(1796,'169.254.3.8',2,2,NULL,NULL,NULL),(1797,'169.254.3.9',2,2,NULL,NULL,NULL),(1798,'169.254.3.10',2,2,NULL,NULL,NULL),(1799,'169.254.3.11',2,2,NULL,NULL,NULL),(1800,'169.254.3.12',2,2,NULL,NULL,NULL),(1801,'169.254.3.13',2,2,NULL,NULL,NULL),(1802,'169.254.3.14',2,2,NULL,NULL,NULL),(1803,'169.254.3.15',2,2,NULL,NULL,NULL),(1804,'169.254.3.16',2,2,NULL,NULL,NULL),(1805,'169.254.3.17',2,2,NULL,NULL,NULL),(1806,'169.254.3.18',2,2,NULL,NULL,NULL),(1807,'169.254.3.19',2,2,NULL,NULL,NULL),(1808,'169.254.3.20',2,2,NULL,NULL,NULL),(1809,'169.254.3.21',2,2,NULL,NULL,NULL),(1810,'169.254.3.22',2,2,NULL,NULL,NULL),(1811,'169.254.3.23',2,2,NULL,NULL,NULL),(1812,'169.254.3.24',2,2,NULL,NULL,NULL),(1813,'169.254.3.25',2,2,NULL,NULL,NULL),(1814,'169.254.3.26',2,2,NULL,NULL,NULL),(1815,'169.254.3.27',2,2,NULL,NULL,NULL),(1816,'169.254.3.28',2,2,NULL,NULL,NULL),(1817,'169.254.3.29',2,2,NULL,NULL,NULL),(1818,'169.254.3.30',2,2,NULL,NULL,NULL),(1819,'169.254.3.31',2,2,NULL,NULL,NULL),(1820,'169.254.3.32',2,2,NULL,NULL,NULL),(1821,'169.254.3.33',2,2,NULL,NULL,NULL),(1822,'169.254.3.34',2,2,NULL,NULL,NULL),(1823,'169.254.3.35',2,2,NULL,NULL,NULL),(1824,'169.254.3.36',2,2,NULL,NULL,NULL),(1825,'169.254.3.37',2,2,NULL,NULL,NULL),(1826,'169.254.3.38',2,2,NULL,NULL,NULL),(1827,'169.254.3.39',2,2,NULL,NULL,NULL),(1828,'169.254.3.40',2,2,NULL,NULL,NULL),(1829,'169.254.3.41',2,2,NULL,NULL,NULL),(1830,'169.254.3.42',2,2,NULL,NULL,NULL),(1831,'169.254.3.43',2,2,NULL,NULL,NULL),(1832,'169.254.3.44',2,2,NULL,NULL,NULL),(1833,'169.254.3.45',2,2,NULL,NULL,NULL),(1834,'169.254.3.46',2,2,NULL,NULL,NULL),(1835,'169.254.3.47',2,2,NULL,NULL,NULL),(1836,'169.254.3.48',2,2,NULL,NULL,NULL),(1837,'169.254.3.49',2,2,NULL,NULL,NULL),(1838,'169.254.3.50',2,2,NULL,NULL,NULL),(1839,'169.254.3.51',2,2,NULL,NULL,NULL),(1840,'169.254.3.52',2,2,NULL,NULL,NULL),(1841,'169.254.3.53',2,2,NULL,NULL,NULL),(1842,'169.254.3.54',2,2,NULL,NULL,NULL),(1843,'169.254.3.55',2,2,NULL,NULL,NULL),(1844,'169.254.3.56',2,2,NULL,NULL,NULL),(1845,'169.254.3.57',2,2,NULL,NULL,NULL),(1846,'169.254.3.58',2,2,NULL,NULL,NULL),(1847,'169.254.3.59',2,2,NULL,NULL,NULL),(1848,'169.254.3.60',2,2,NULL,NULL,NULL),(1849,'169.254.3.61',2,2,NULL,NULL,NULL),(1850,'169.254.3.62',2,2,NULL,NULL,NULL),(1851,'169.254.3.63',2,2,NULL,NULL,NULL),(1852,'169.254.3.64',2,2,NULL,NULL,NULL),(1853,'169.254.3.65',2,2,NULL,NULL,NULL),(1854,'169.254.3.66',2,2,NULL,NULL,NULL),(1855,'169.254.3.67',2,2,NULL,NULL,NULL),(1856,'169.254.3.68',2,2,NULL,NULL,NULL),(1857,'169.254.3.69',2,2,NULL,NULL,NULL),(1858,'169.254.3.70',2,2,NULL,NULL,NULL),(1859,'169.254.3.71',2,2,NULL,NULL,NULL),(1860,'169.254.3.72',2,2,NULL,NULL,NULL),(1861,'169.254.3.73',2,2,NULL,NULL,NULL),(1862,'169.254.3.74',2,2,NULL,NULL,NULL),(1863,'169.254.3.75',2,2,NULL,NULL,NULL),(1864,'169.254.3.76',2,2,NULL,NULL,NULL),(1865,'169.254.3.77',2,2,NULL,NULL,NULL),(1866,'169.254.3.78',2,2,NULL,NULL,NULL),(1867,'169.254.3.79',2,2,NULL,NULL,NULL),(1868,'169.254.3.80',2,2,NULL,NULL,NULL),(1869,'169.254.3.81',2,2,NULL,NULL,NULL),(1870,'169.254.3.82',2,2,NULL,NULL,NULL),(1871,'169.254.3.83',2,2,NULL,NULL,NULL),(1872,'169.254.3.84',2,2,NULL,NULL,NULL),(1873,'169.254.3.85',2,2,NULL,NULL,NULL),(1874,'169.254.3.86',2,2,NULL,NULL,NULL),(1875,'169.254.3.87',2,2,NULL,NULL,NULL),(1876,'169.254.3.88',2,2,NULL,NULL,NULL),(1877,'169.254.3.89',2,2,NULL,NULL,NULL),(1878,'169.254.3.90',2,2,NULL,NULL,NULL),(1879,'169.254.3.91',2,2,NULL,NULL,NULL),(1880,'169.254.3.92',2,2,NULL,NULL,NULL),(1881,'169.254.3.93',2,2,NULL,NULL,NULL),(1882,'169.254.3.94',2,2,NULL,NULL,NULL),(1883,'169.254.3.95',2,2,NULL,NULL,NULL),(1884,'169.254.3.96',2,2,NULL,NULL,NULL),(1885,'169.254.3.97',2,2,NULL,NULL,NULL),(1886,'169.254.3.98',2,2,NULL,NULL,NULL),(1887,'169.254.3.99',2,2,NULL,NULL,NULL),(1888,'169.254.3.100',2,2,NULL,NULL,NULL),(1889,'169.254.3.101',2,2,NULL,NULL,NULL),(1890,'169.254.3.102',2,2,NULL,NULL,NULL),(1891,'169.254.3.103',2,2,NULL,NULL,NULL),(1892,'169.254.3.104',2,2,NULL,NULL,NULL),(1893,'169.254.3.105',2,2,NULL,NULL,NULL),(1894,'169.254.3.106',2,2,NULL,NULL,NULL),(1895,'169.254.3.107',2,2,NULL,NULL,NULL),(1896,'169.254.3.108',2,2,NULL,NULL,NULL),(1897,'169.254.3.109',2,2,NULL,NULL,NULL),(1898,'169.254.3.110',2,2,NULL,NULL,NULL),(1899,'169.254.3.111',2,2,NULL,NULL,NULL),(1900,'169.254.3.112',2,2,NULL,NULL,NULL),(1901,'169.254.3.113',2,2,NULL,NULL,NULL),(1902,'169.254.3.114',2,2,NULL,NULL,NULL),(1903,'169.254.3.115',2,2,NULL,NULL,NULL),(1904,'169.254.3.116',2,2,NULL,NULL,NULL),(1905,'169.254.3.117',2,2,NULL,NULL,NULL),(1906,'169.254.3.118',2,2,NULL,NULL,NULL),(1907,'169.254.3.119',2,2,NULL,NULL,NULL),(1908,'169.254.3.120',2,2,NULL,NULL,NULL),(1909,'169.254.3.121',2,2,NULL,NULL,NULL),(1910,'169.254.3.122',2,2,NULL,NULL,NULL),(1911,'169.254.3.123',2,2,NULL,NULL,NULL),(1912,'169.254.3.124',2,2,NULL,NULL,NULL),(1913,'169.254.3.125',2,2,NULL,NULL,NULL),(1914,'169.254.3.126',2,2,NULL,NULL,NULL),(1915,'169.254.3.127',2,2,NULL,NULL,NULL),(1916,'169.254.3.128',2,2,NULL,NULL,NULL),(1917,'169.254.3.129',2,2,NULL,NULL,NULL),(1918,'169.254.3.130',2,2,NULL,NULL,NULL),(1919,'169.254.3.131',2,2,NULL,NULL,NULL),(1920,'169.254.3.132',2,2,NULL,NULL,NULL),(1921,'169.254.3.133',2,2,NULL,NULL,NULL),(1922,'169.254.3.134',2,2,NULL,NULL,NULL),(1923,'169.254.3.135',2,2,NULL,NULL,NULL),(1924,'169.254.3.136',2,2,NULL,NULL,NULL),(1925,'169.254.3.137',2,2,NULL,NULL,NULL),(1926,'169.254.3.138',2,2,NULL,NULL,NULL),(1927,'169.254.3.139',2,2,NULL,NULL,NULL),(1928,'169.254.3.140',2,2,NULL,NULL,NULL),(1929,'169.254.3.141',2,2,NULL,NULL,NULL),(1930,'169.254.3.142',2,2,NULL,NULL,NULL),(1931,'169.254.3.143',2,2,NULL,NULL,NULL),(1932,'169.254.3.144',2,2,NULL,NULL,NULL),(1933,'169.254.3.145',2,2,NULL,NULL,NULL),(1934,'169.254.3.146',2,2,NULL,NULL,NULL),(1935,'169.254.3.147',2,2,NULL,NULL,NULL),(1936,'169.254.3.148',2,2,NULL,NULL,NULL),(1937,'169.254.3.149',2,2,NULL,NULL,NULL),(1938,'169.254.3.150',2,2,NULL,NULL,NULL),(1939,'169.254.3.151',2,2,NULL,NULL,NULL),(1940,'169.254.3.152',2,2,NULL,NULL,NULL),(1941,'169.254.3.153',2,2,NULL,NULL,NULL),(1942,'169.254.3.154',2,2,NULL,NULL,NULL),(1943,'169.254.3.155',2,2,NULL,NULL,NULL),(1944,'169.254.3.156',2,2,NULL,NULL,NULL),(1945,'169.254.3.157',2,2,NULL,NULL,NULL),(1946,'169.254.3.158',2,2,NULL,NULL,NULL),(1947,'169.254.3.159',2,2,NULL,NULL,NULL),(1948,'169.254.3.160',2,2,NULL,NULL,NULL),(1949,'169.254.3.161',2,2,NULL,NULL,NULL),(1950,'169.254.3.162',2,2,NULL,NULL,NULL),(1951,'169.254.3.163',2,2,NULL,NULL,NULL),(1952,'169.254.3.164',2,2,NULL,NULL,NULL),(1953,'169.254.3.165',2,2,NULL,NULL,NULL),(1954,'169.254.3.166',2,2,NULL,NULL,NULL),(1955,'169.254.3.167',2,2,NULL,NULL,NULL),(1956,'169.254.3.168',2,2,NULL,NULL,NULL),(1957,'169.254.3.169',2,2,NULL,NULL,NULL),(1958,'169.254.3.170',2,2,NULL,NULL,NULL),(1959,'169.254.3.171',2,2,NULL,NULL,NULL),(1960,'169.254.3.172',2,2,NULL,NULL,NULL),(1961,'169.254.3.173',2,2,NULL,NULL,NULL),(1962,'169.254.3.174',2,2,NULL,NULL,NULL),(1963,'169.254.3.175',2,2,NULL,NULL,NULL),(1964,'169.254.3.176',2,2,NULL,NULL,NULL),(1965,'169.254.3.177',2,2,NULL,NULL,NULL),(1966,'169.254.3.178',2,2,NULL,NULL,NULL),(1967,'169.254.3.179',2,2,NULL,NULL,NULL),(1968,'169.254.3.180',2,2,NULL,NULL,NULL),(1969,'169.254.3.181',2,2,NULL,NULL,NULL),(1970,'169.254.3.182',2,2,NULL,NULL,NULL),(1971,'169.254.3.183',2,2,NULL,NULL,NULL),(1972,'169.254.3.184',2,2,NULL,NULL,NULL),(1973,'169.254.3.185',2,2,NULL,NULL,NULL),(1974,'169.254.3.186',2,2,NULL,NULL,NULL),(1975,'169.254.3.187',2,2,NULL,NULL,NULL),(1976,'169.254.3.188',2,2,NULL,NULL,NULL),(1977,'169.254.3.189',2,2,NULL,NULL,NULL),(1978,'169.254.3.190',2,2,NULL,NULL,NULL),(1979,'169.254.3.191',2,2,NULL,NULL,NULL),(1980,'169.254.3.192',2,2,NULL,NULL,NULL),(1981,'169.254.3.193',2,2,NULL,NULL,NULL),(1982,'169.254.3.194',2,2,NULL,NULL,NULL),(1983,'169.254.3.195',2,2,NULL,NULL,NULL),(1984,'169.254.3.196',2,2,NULL,NULL,NULL),(1985,'169.254.3.197',2,2,NULL,NULL,NULL),(1986,'169.254.3.198',2,2,NULL,NULL,NULL),(1987,'169.254.3.199',2,2,NULL,NULL,NULL),(1988,'169.254.3.200',2,2,NULL,NULL,NULL),(1989,'169.254.3.201',2,2,NULL,NULL,NULL),(1990,'169.254.3.202',2,2,NULL,NULL,NULL),(1991,'169.254.3.203',2,2,NULL,NULL,NULL),(1992,'169.254.3.204',2,2,NULL,NULL,NULL),(1993,'169.254.3.205',2,2,NULL,NULL,NULL),(1994,'169.254.3.206',2,2,NULL,NULL,NULL),(1995,'169.254.3.207',2,2,NULL,NULL,NULL),(1996,'169.254.3.208',2,2,NULL,NULL,NULL),(1997,'169.254.3.209',2,2,NULL,NULL,NULL),(1998,'169.254.3.210',2,2,NULL,NULL,NULL),(1999,'169.254.3.211',2,2,NULL,NULL,NULL),(2000,'169.254.3.212',2,2,NULL,NULL,NULL),(2001,'169.254.3.213',2,2,NULL,NULL,NULL),(2002,'169.254.3.214',2,2,NULL,NULL,NULL),(2003,'169.254.3.215',2,2,NULL,NULL,NULL),(2004,'169.254.3.216',2,2,NULL,NULL,NULL),(2005,'169.254.3.217',2,2,NULL,NULL,NULL),(2006,'169.254.3.218',2,2,NULL,NULL,NULL),(2007,'169.254.3.219',2,2,NULL,NULL,NULL),(2008,'169.254.3.220',2,2,NULL,NULL,NULL),(2009,'169.254.3.221',2,2,NULL,NULL,NULL),(2010,'169.254.3.222',2,2,NULL,NULL,NULL),(2011,'169.254.3.223',2,2,NULL,NULL,NULL),(2012,'169.254.3.224',2,2,NULL,NULL,NULL),(2013,'169.254.3.225',2,2,NULL,NULL,NULL),(2014,'169.254.3.226',2,2,NULL,NULL,NULL),(2015,'169.254.3.227',2,2,NULL,NULL,NULL),(2016,'169.254.3.228',2,2,NULL,NULL,NULL),(2017,'169.254.3.229',2,2,NULL,NULL,NULL),(2018,'169.254.3.230',2,2,NULL,NULL,NULL),(2019,'169.254.3.231',2,2,NULL,NULL,NULL),(2020,'169.254.3.232',2,2,NULL,NULL,NULL),(2021,'169.254.3.233',2,2,NULL,NULL,NULL),(2022,'169.254.3.234',2,2,NULL,NULL,NULL),(2023,'169.254.3.235',2,2,NULL,NULL,NULL),(2024,'169.254.3.236',2,2,NULL,NULL,NULL),(2025,'169.254.3.237',2,2,NULL,NULL,NULL),(2026,'169.254.3.238',2,2,NULL,NULL,NULL),(2027,'169.254.3.239',2,2,NULL,NULL,NULL),(2028,'169.254.3.240',2,2,NULL,NULL,NULL),(2029,'169.254.3.241',2,2,NULL,NULL,NULL),(2030,'169.254.3.242',2,2,NULL,NULL,NULL),(2031,'169.254.3.243',2,2,NULL,NULL,NULL),(2032,'169.254.3.244',2,2,NULL,NULL,NULL),(2033,'169.254.3.245',2,2,NULL,NULL,NULL),(2034,'169.254.3.246',2,2,NULL,NULL,NULL),(2035,'169.254.3.247',2,2,NULL,NULL,NULL),(2036,'169.254.3.248',2,2,NULL,NULL,NULL),(2037,'169.254.3.249',2,2,NULL,NULL,NULL),(2038,'169.254.3.250',2,2,NULL,NULL,NULL),(2039,'169.254.3.251',2,2,NULL,NULL,NULL),(2040,'169.254.3.252',2,2,NULL,NULL,NULL),(2041,'169.254.3.253',2,2,NULL,NULL,NULL),(2042,'169.254.3.254',2,2,NULL,NULL,NULL);
/*!40000 ALTER TABLE `op_dc_link_local_ip_address_alloc` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `op_dc_vnet_alloc`
--

DROP TABLE IF EXISTS `op_dc_vnet_alloc`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `op_dc_vnet_alloc` (
  `id` bigint(20) unsigned NOT NULL auto_increment COMMENT 'primary id',
  `vnet` varchar(18) NOT NULL COMMENT 'vnet',
  `data_center_id` bigint(20) unsigned NOT NULL COMMENT 'data center the vnet belongs to',
  `reservation_id` char(40) default NULL COMMENT 'reservation id',
  `account_id` bigint(20) unsigned default NULL COMMENT 'account the vnet belongs to right now',
  `taken` datetime default NULL COMMENT 'Date taken',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `i_op_dc_vnet_alloc__vnet__data_center_id` (`vnet`,`data_center_id`),
  UNIQUE KEY `i_op_dc_vnet_alloc__vnet__data_center_id__account_id` (`vnet`,`data_center_id`,`account_id`),
  KEY `i_op_dc_vnet_alloc__dc_taken` (`data_center_id`,`taken`)
) ENGINE=InnoDB AUTO_INCREMENT=181 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `op_dc_vnet_alloc`
--

LOCK TABLES `op_dc_vnet_alloc` WRITE;
/*!40000 ALTER TABLE `op_dc_vnet_alloc` DISABLE KEYS */;
INSERT INTO `op_dc_vnet_alloc` VALUES (1,'200',1,NULL,NULL,NULL),(2,'201',1,NULL,NULL,NULL),(3,'202',1,NULL,NULL,NULL),(4,'203',1,NULL,NULL,NULL),(5,'204',1,NULL,NULL,NULL),(6,'205',1,NULL,NULL,NULL),(7,'206',1,NULL,NULL,NULL),(8,'207',1,NULL,NULL,NULL),(9,'208',1,NULL,NULL,NULL),(10,'209',1,NULL,NULL,NULL),(11,'210',1,NULL,NULL,NULL),(12,'211',1,NULL,NULL,NULL),(13,'212',1,NULL,NULL,NULL),(14,'213',1,NULL,NULL,NULL),(15,'214',1,NULL,NULL,NULL),(16,'215',1,NULL,NULL,NULL),(17,'216',1,NULL,NULL,NULL),(18,'217',1,NULL,NULL,NULL),(19,'218',1,NULL,NULL,NULL),(20,'219',1,NULL,NULL,NULL),(21,'220',1,NULL,NULL,NULL),(22,'221',1,NULL,NULL,NULL),(23,'222',1,NULL,NULL,NULL),(24,'223',1,NULL,NULL,NULL),(25,'224',1,NULL,NULL,NULL),(26,'225',1,NULL,NULL,NULL),(27,'226',1,NULL,NULL,NULL),(28,'227',1,NULL,NULL,NULL),(29,'228',1,NULL,NULL,NULL),(30,'229',1,NULL,NULL,NULL),(31,'230',1,NULL,NULL,NULL),(32,'231',1,NULL,NULL,NULL),(33,'232',1,NULL,NULL,NULL),(34,'233',1,NULL,NULL,NULL),(35,'234',1,NULL,NULL,NULL),(36,'235',1,NULL,NULL,NULL),(37,'236',1,NULL,NULL,NULL),(38,'237',1,NULL,NULL,NULL),(39,'238',1,NULL,NULL,NULL),(40,'239',1,NULL,NULL,NULL),(41,'240',1,NULL,NULL,NULL),(42,'241',1,NULL,NULL,NULL),(43,'242',1,NULL,NULL,NULL),(44,'243',1,NULL,NULL,NULL),(45,'244',1,NULL,NULL,NULL),(46,'245',1,NULL,NULL,NULL),(47,'246',1,NULL,NULL,NULL),(48,'247',1,NULL,NULL,NULL),(49,'248',1,NULL,NULL,NULL),(50,'249',1,NULL,NULL,NULL),(51,'250',1,NULL,NULL,NULL),(52,'251',1,NULL,NULL,NULL),(53,'252',1,NULL,NULL,NULL),(54,'253',1,NULL,NULL,NULL),(55,'254',1,NULL,NULL,NULL),(56,'255',1,NULL,NULL,NULL),(57,'256',1,NULL,NULL,NULL),(58,'257',1,NULL,NULL,NULL),(59,'258',1,NULL,NULL,NULL),(60,'259',1,NULL,NULL,NULL),(61,'260',1,NULL,NULL,NULL),(62,'261',1,NULL,NULL,NULL),(63,'262',1,NULL,NULL,NULL),(64,'263',1,NULL,NULL,NULL),(65,'264',1,NULL,NULL,NULL),(66,'265',1,NULL,NULL,NULL),(67,'266',1,NULL,NULL,NULL),(68,'267',1,NULL,NULL,NULL),(69,'268',1,NULL,NULL,NULL),(70,'269',1,NULL,NULL,NULL),(71,'270',1,NULL,NULL,NULL),(72,'271',1,NULL,NULL,NULL),(73,'272',1,NULL,NULL,NULL),(74,'273',1,NULL,NULL,NULL),(75,'274',1,NULL,NULL,NULL),(76,'275',1,NULL,NULL,NULL),(77,'276',1,NULL,NULL,NULL),(78,'277',1,NULL,NULL,NULL),(79,'278',1,NULL,NULL,NULL),(80,'279',1,NULL,NULL,NULL),(81,'280',1,NULL,NULL,NULL),(82,'281',1,NULL,NULL,NULL),(83,'282',1,NULL,NULL,NULL),(84,'283',1,NULL,NULL,NULL),(85,'284',1,NULL,NULL,NULL),(86,'285',1,NULL,NULL,NULL),(87,'286',1,NULL,NULL,NULL),(88,'287',1,NULL,NULL,NULL),(89,'288',1,NULL,NULL,NULL),(90,'289',1,NULL,NULL,NULL),(91,'290',1,NULL,NULL,NULL),(92,'291',1,NULL,NULL,NULL),(93,'292',1,NULL,NULL,NULL),(94,'293',1,NULL,NULL,NULL),(95,'294',1,NULL,NULL,NULL),(96,'295',1,NULL,NULL,NULL),(97,'296',1,NULL,NULL,NULL),(98,'297',1,NULL,NULL,NULL),(99,'298',1,NULL,NULL,NULL),(100,'299',1,NULL,NULL,NULL),(101,'1080',2,NULL,NULL,NULL),(102,'1081',2,NULL,NULL,NULL),(103,'1082',2,NULL,NULL,NULL),(104,'1083',2,NULL,NULL,NULL),(105,'1084',2,NULL,NULL,NULL),(106,'1085',2,NULL,NULL,NULL),(107,'1086',2,NULL,NULL,NULL),(108,'1087',2,NULL,NULL,NULL),(109,'1088',2,NULL,NULL,NULL),(110,'1089',2,NULL,NULL,NULL),(111,'1090',2,NULL,NULL,NULL),(112,'1091',2,NULL,NULL,NULL),(113,'1092',2,NULL,NULL,NULL),(114,'1093',2,NULL,NULL,NULL),(115,'1094',2,NULL,NULL,NULL),(116,'1095',2,NULL,NULL,NULL),(117,'1096',2,NULL,NULL,NULL),(118,'1097',2,NULL,NULL,NULL),(119,'1098',2,NULL,NULL,NULL),(120,'1099',2,NULL,NULL,NULL),(121,'1100',2,NULL,NULL,NULL),(122,'1101',2,NULL,NULL,NULL),(123,'1102',2,NULL,NULL,NULL),(124,'1103',2,NULL,NULL,NULL),(125,'1104',2,NULL,NULL,NULL),(126,'1105',2,NULL,NULL,NULL),(127,'1106',2,NULL,NULL,NULL),(128,'1107',2,NULL,NULL,NULL),(129,'1108',2,NULL,NULL,NULL),(130,'1109',2,NULL,NULL,NULL),(131,'1110',2,NULL,NULL,NULL),(132,'1111',2,NULL,NULL,NULL),(133,'1112',2,NULL,NULL,NULL),(134,'1113',2,NULL,NULL,NULL),(135,'1114',2,NULL,NULL,NULL),(136,'1115',2,NULL,NULL,NULL),(137,'1116',2,NULL,NULL,NULL),(138,'1117',2,NULL,NULL,NULL),(139,'1118',2,NULL,NULL,NULL),(140,'1119',2,NULL,NULL,NULL),(141,'1120',2,NULL,NULL,NULL),(142,'1121',2,NULL,NULL,NULL),(143,'1122',2,NULL,NULL,NULL),(144,'1123',2,NULL,NULL,NULL),(145,'1124',2,NULL,NULL,NULL),(146,'1125',2,NULL,NULL,NULL),(147,'1126',2,NULL,NULL,NULL),(148,'1127',2,NULL,NULL,NULL),(149,'1128',2,'2f2ad287-f40f-4089-bd34-009f41ed51d8',70,'2011-05-12 18:48:50'),(150,'1129',2,NULL,NULL,NULL),(151,'1130',2,NULL,NULL,NULL),(152,'1131',2,NULL,NULL,NULL),(153,'1132',2,NULL,NULL,NULL),(154,'1133',2,NULL,NULL,NULL),(155,'1134',2,NULL,NULL,NULL),(156,'1135',2,NULL,NULL,NULL),(157,'1136',2,NULL,NULL,NULL),(158,'1137',2,NULL,NULL,NULL),(159,'1138',2,NULL,NULL,NULL),(160,'1139',2,NULL,NULL,NULL),(161,'1140',2,NULL,NULL,NULL),(162,'1141',2,NULL,NULL,NULL),(163,'1142',2,NULL,NULL,NULL),(164,'1143',2,NULL,NULL,NULL),(165,'1144',2,NULL,NULL,NULL),(166,'1145',2,NULL,NULL,NULL),(167,'1146',2,NULL,NULL,NULL),(168,'1147',2,NULL,NULL,NULL),(169,'1148',2,NULL,NULL,NULL),(170,'1149',2,NULL,NULL,NULL),(171,'1150',2,NULL,NULL,NULL),(172,'1151',2,NULL,NULL,NULL),(173,'1152',2,NULL,NULL,NULL),(174,'1153',2,NULL,NULL,NULL),(175,'1154',2,NULL,NULL,NULL),(176,'1155',2,NULL,NULL,NULL),(177,'1156',2,NULL,NULL,NULL),(178,'1157',2,NULL,NULL,NULL),(179,'1158',2,NULL,NULL,NULL),(180,'1159',2,NULL,NULL,NULL);
/*!40000 ALTER TABLE `op_dc_vnet_alloc` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `op_ha_work`
--

DROP TABLE IF EXISTS `op_ha_work`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `op_ha_work` (
  `id` bigint(20) unsigned NOT NULL auto_increment COMMENT 'id',
  `instance_id` bigint(20) unsigned NOT NULL COMMENT 'vm instance that needs to be ha.',
  `type` varchar(32) NOT NULL COMMENT 'type of work',
  `vm_type` varchar(32) NOT NULL COMMENT 'VM type',
  `state` varchar(32) NOT NULL COMMENT 'state of the vm instance when this happened.',
  `mgmt_server_id` bigint(20) unsigned default NULL COMMENT 'management server that has taken up the work of doing ha',
  `host_id` bigint(20) unsigned default NULL COMMENT 'host that the vm is suppose to be on',
  `created` datetime NOT NULL COMMENT 'time the entry was requested',
  `tried` int(10) unsigned default NULL COMMENT '# of times tried',
  `taken` datetime default NULL COMMENT 'time it was taken by the management server',
  `step` varchar(32) NOT NULL COMMENT 'Step in the work',
  `time_to_try` bigint(20) default NULL COMMENT 'time to try do this work',
  `updated` bigint(20) unsigned NOT NULL COMMENT 'time the VM state was updated when it was stored into work queue',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`),
  KEY `i_op_ha_work__instance_id` (`instance_id`),
  KEY `i_op_ha_work__host_id` (`host_id`),
  KEY `i_op_ha_work__step` (`step`),
  KEY `i_op_ha_work__type` (`type`),
  KEY `i_op_ha_work__mgmt_server_id` (`mgmt_server_id`),
  CONSTRAINT `fk_op_ha_work__host_id` FOREIGN KEY (`host_id`) REFERENCES `host` (`id`),
  CONSTRAINT `fk_op_ha_work__instance_id` FOREIGN KEY (`instance_id`) REFERENCES `vm_instance` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_op_ha_work__mgmt_server_id` FOREIGN KEY (`mgmt_server_id`) REFERENCES `mshost` (`msid`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `op_ha_work`
--

LOCK TABLES `op_ha_work` WRITE;
/*!40000 ALTER TABLE `op_ha_work` DISABLE KEYS */;
INSERT INTO `op_ha_work` VALUES (1,3,'HA','User','Stopped',6602001285584,1,'2011-02-10 12:07:58',1,'2011-02-10 12:07:58','Error',1266933280,5),(8,3,'HA','User','Stopped',6602001285584,1,'2011-02-15 23:55:27',2,'2011-02-15 23:55:27','Error',1267396609,14),(9,7,'HA','User','Stopped',6602001285584,1,'2011-02-15 23:55:28',2,'2011-02-15 23:55:28','Error',1267396609,12),(10,8,'HA','User','Stopped',6602001285584,1,'2011-02-15 23:55:28',2,'2011-02-15 23:55:28','Error',1267396609,10);
/*!40000 ALTER TABLE `op_ha_work` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `op_host`
--

DROP TABLE IF EXISTS `op_host`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `op_host` (
  `id` bigint(20) unsigned NOT NULL COMMENT 'host id',
  `sequence` bigint(20) unsigned NOT NULL default '1' COMMENT 'sequence for the host communication',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`),
  CONSTRAINT `fk_op_host__id` FOREIGN KEY (`id`) REFERENCES `host` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `op_host`
--

LOCK TABLES `op_host` WRITE;
/*!40000 ALTER TABLE `op_host` DISABLE KEYS */;
INSERT INTO `op_host` VALUES (1,332658),(2,72439),(3,15),(4,22932),(5,13387),(6,10);
/*!40000 ALTER TABLE `op_host` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `op_host_capacity`
--

DROP TABLE IF EXISTS `op_host_capacity`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `op_host_capacity` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `host_id` bigint(20) unsigned default NULL,
  `data_center_id` bigint(20) unsigned NOT NULL,
  `pod_id` bigint(20) unsigned default NULL,
  `used_capacity` bigint(20) unsigned NOT NULL,
  `reserved_capacity` bigint(20) unsigned NOT NULL,
  `total_capacity` bigint(20) unsigned NOT NULL,
  `capacity_type` int(1) unsigned NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `i_op_host_capacity__host_type` (`host_id`,`capacity_type`),
  KEY `i_op_host_capacity__pod_id` (`pod_id`),
  KEY `i_op_host_capacity__data_center_id` (`data_center_id`),
  CONSTRAINT `fk_op_host_capacity__data_center_id` FOREIGN KEY (`data_center_id`) REFERENCES `data_center` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_op_host_capacity__pod_id` FOREIGN KEY (`pod_id`) REFERENCES `host_pod_ref` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=24021 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `op_host_capacity`
--

LOCK TABLES `op_host_capacity` WRITE;
/*!40000 ALTER TABLE `op_host_capacity` DISABLE KEYS */;
INSERT INTO `op_host_capacity` VALUES (3,1,1,1,0,4412,12800,1),(4,1,1,1,0,2147483648,3320174592,0),(7,200,1,1,1529733578752,0,1925386469376,2),(8,200,1,1,68266491904,0,3850772938752,3),(16630,2,1,1,1529733349376,0,1925386469376,6),(20621,4,2,2,2712,0,18080,1),(20622,4,2,2,1744830464,0,3701658240,0),(20627,201,2,2,928629882880,0,1930519904256,2),(20628,201,2,2,30937186304,0,3861039808512,3),(24017,NULL,1,NULL,15,0,20,4),(24018,NULL,2,NULL,5,0,10,4),(24019,NULL,1,1,0,0,3,5),(24020,NULL,2,2,2,0,5,5);
/*!40000 ALTER TABLE `op_host_capacity` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `op_host_upgrade`
--

DROP TABLE IF EXISTS `op_host_upgrade`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `op_host_upgrade` (
  `host_id` bigint(20) unsigned NOT NULL COMMENT 'host id',
  `version` varchar(20) NOT NULL COMMENT 'version',
  `state` varchar(20) NOT NULL COMMENT 'state',
  PRIMARY KEY  (`host_id`),
  UNIQUE KEY `host_id` (`host_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `op_host_upgrade`
--

LOCK TABLES `op_host_upgrade` WRITE;
/*!40000 ALTER TABLE `op_host_upgrade` DISABLE KEYS */;
/*!40000 ALTER TABLE `op_host_upgrade` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `op_it_work`
--

DROP TABLE IF EXISTS `op_it_work`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `op_it_work` (
  `id` char(40) NOT NULL default '' COMMENT 'id',
  `mgmt_server_id` bigint(20) unsigned default NULL COMMENT 'management server id',
  `created_at` bigint(20) unsigned NOT NULL COMMENT 'when was this work detail created',
  `thread` varchar(255) NOT NULL COMMENT 'thread name',
  `type` char(32) NOT NULL COMMENT 'type of work',
  `step` char(32) NOT NULL COMMENT 'state',
  `updated_at` bigint(20) unsigned NOT NULL COMMENT 'time it was taken over',
  `instance_id` bigint(20) unsigned NOT NULL COMMENT 'vm instance',
  `resource_type` char(32) default NULL COMMENT 'type of resource being worked on',
  `resource_id` bigint(20) unsigned default NULL COMMENT 'resource id being worked on',
  `vm_type` char(32) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `fk_op_it_work__mgmt_server_id` (`mgmt_server_id`),
  KEY `fk_op_it_work__instance_id` (`instance_id`),
  KEY `i_op_it_work__step` (`step`),
  CONSTRAINT `fk_op_it_work__instance_id` FOREIGN KEY (`instance_id`) REFERENCES `vm_instance` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_op_it_work__mgmt_server_id` FOREIGN KEY (`mgmt_server_id`) REFERENCES `mshost` (`msid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `op_it_work`
--

LOCK TABLES `op_it_work` WRITE;
/*!40000 ALTER TABLE `op_it_work` DISABLE KEYS */;
INSERT INTO `op_it_work` VALUES ('03e242ec-3420-484c-abdb-927bf9cdf355',6602001285584,1274140771,'Job-Executor-3','Starting','Done',1274140787,41,NULL,0,'SecondaryStorageVm'),('03eb61a8-1ff2-41c3-b5dc-860f038c9f09',6602001285584,1274140995,'Job-Executor-5','Starting','Done',1274140995,6,NULL,0,'DomainRouter'),('040df226-9233-44b7-9879-fcf3e17bbd57',6602001285584,1266921486,'Job-Executor-2','Starting','Done',1266921535,5,NULL,0,'User'),('05fb7288-9f40-4660-bb14-91408ea2ee2c',6602001285584,1267396608,'HA-Worker-1','Starting','Done',1267396609,7,NULL,0,'User'),('0718bd8d-1efc-496b-b283-a45efbe99834',6602001285584,1274396991,'CP-Scan-1','Starting','Done',1274396992,40,NULL,0,'ConsoleProxy'),('09bb2917-c4d4-4ef4-a277-957540b7a3ed',6602001285584,1274140251,'CP-Scan-1','Starting','Done',1274140383,40,NULL,0,'ConsoleProxy'),('11561ebb-8873-45a6-a870-f08c5134d080',6602001285584,1267533481,'Job-Executor-9','Starting','Done',1267533531,13,NULL,0,'DomainRouter'),('1329c8c6-8cd8-4fbf-b8a4-8f0193a555b2',6602001285584,1274397254,'CP-Scan-1','Starting','Done',1274397255,40,NULL,0,'ConsoleProxy'),('137de854-8674-4fc8-ba06-ee1c5d9e559b',6602001285584,1269254792,'Job-Executor-24','Starting','Done',1269254792,25,NULL,0,'User'),('18756493-d799-43e2-8ecd-94de52bda6f9',6602001285584,1266928793,'Job-Executor-29','Starting','Done',1266928844,10,NULL,0,'DomainRouter'),('1c81e568-89ab-4a65-a98c-07a3e929c6bb',6602001285584,1271433382,'Job-Executor-19','Starting','Done',1271433382,33,NULL,0,'User'),('1e273303-a8b0-49b2-a7c5-fe2a7319b992',6602001285584,1271420411,'Job-Executor-14','Starting','Done',1271420411,32,NULL,0,'User'),('1ebadf87-aa97-4041-b251-1c76d8bb402c',6602001285584,0,'Job-Executor-3','Starting','Done',1267383992,6,NULL,0,'DomainRouter'),('2649e310-a0bb-472e-8cfd-17c4005e4451',6602001285584,1274140753,'Job-Executor-3','Starting','Done',1274140771,40,NULL,0,'ConsoleProxy'),('2abc997f-b2b1-4241-82ac-ae8e8215e90b',6602001285584,1274397079,'CP-Scan-1','Starting','Done',1274397080,40,NULL,0,'ConsoleProxy'),('2eaf976b-c070-4f23-a884-ceff344d4ffe',6602001285584,1274397565,'HA-Worker-2','Starting','Done',1274397583,41,NULL,0,'SecondaryStorageVm'),('2ec1fc12-1308-493d-8039-45bdd23966e2',6602001285584,1271821103,'Job-Executor-25','Starting','Done',1271821154,36,NULL,0,'DomainRouter'),('2f2ad287-f40f-4089-bd34-009f41ed51d8',6602001285584,1274634891,'Job-Executor-26','Starting','Done',1274634941,46,NULL,0,'User'),('2fe5170e-91c9-4df8-8252-dc0123553253',6602001285584,1267533701,'Job-Executor-10','Starting','Done',1267533740,15,NULL,0,'DomainRouter'),('30ff8edd-f496-431c-94e3-5ffc8901cbc6',6602001285584,1274634893,'Job-Executor-26','Starting','Done',1274634929,47,NULL,0,'DomainRouter'),('3182f83d-47bd-4df0-bdb5-0b57194c6ae7',6602001285584,1274397225,'CP-Scan-1','Starting','Done',1274397226,40,NULL,0,'ConsoleProxy'),('3bd2ca01-c0d7-481a-b0fb-4217a793e40d',6602001285584,1274570020,'Job-Executor-25','Starting','Done',1274570060,45,NULL,0,'DomainRouter'),('3c0260b7-870e-4cae-ad59-4e7c60b89b01',6602001285584,1266921611,'Job-Executor-2','Starting','Done',1266921622,8,NULL,0,'User'),('447cfa82-b227-4e2e-a509-017fc71b8fad',6602001285584,1274397020,'CP-Scan-1','Starting','Done',1274397021,40,NULL,0,'ConsoleProxy'),('44ea2503-ede0-45c3-8046-81a035c25db6',6602001285584,1266921487,'Job-Executor-2','Starting','Done',1266921525,6,NULL,0,'DomainRouter'),('49751446-b3b7-4393-a38c-4c24a71c6952',6602001285584,1267396666,'CP-Scan-1','Starting','Done',1267396712,1,NULL,0,'ConsoleProxy'),('4b114ca8-89ee-4d86-b6f2-4de043580945',6602001285584,1269206939,'Job-Executor-11','Starting','Done',1269206992,22,NULL,0,'DomainRouter'),('4b567e72-34e0-4784-93e3-910c17586cfa',6602001285584,1267396609,'HA-Worker-2','Starting','Done',1267396609,8,NULL,0,'User'),('51e6618f-df2a-4d0d-91ee-4ed2b05589c5',6602001285584,1266928699,'Job-Executor-29','Starting','Done',1266928854,9,NULL,0,'User'),('53c4a40f-827b-4d01-8d02-4bd36fb0cd5e',6602001285584,1266915372,'CP-Scan-1','Starting','Done',1266915528,1,NULL,0,'ConsoleProxy'),('6006e1ec-c63b-4b2b-a7a1-7e792cd5a13a',6602001285584,1274397166,'CP-Scan-1','Starting','Done',1274397168,40,NULL,0,'ConsoleProxy'),('61869ade-01a3-4abc-b9d3-42c9a0b3a819',6602001285584,1267533617,'Job-Executor-10','Starting','Done',1267533751,14,NULL,0,'User'),('6405ac82-9804-4bfd-87d8-0835101387fe',6602001285584,1274396961,'CP-Scan-1','Starting','Done',1274396964,40,NULL,0,'ConsoleProxy'),('64ae6fc3-08b2-473a-a018-3433e163d8d2',6602001285584,1267835809,'Job-Executor-11','Starting','Done',1267835836,6,NULL,0,'DomainRouter'),('6880a746-4166-41aa-a5ce-f2afbd6a1cd1',6602001285584,1270440377,'Job-Executor-2','Starting','Done',1270440377,27,NULL,0,'User'),('6baa7a05-6b30-45df-af81-b60176209a4f',6602001285584,1274397284,'CP-Scan-1','Starting','Done',1274397285,40,NULL,0,'ConsoleProxy'),('6e97fb37-caec-481f-8085-01d4f8b08a73',6602001285584,1266937933,'Job-Executor-1','Starting','Done',1266937944,3,NULL,0,'User'),('7027b513-80ff-437d-8499-6231d116c7ae',6602001285584,1267533395,'Job-Executor-9','Starting','Done',1267533542,12,NULL,0,'User'),('71ae5390-6b4c-47ad-811a-f3c97c04cc05',6602001285584,0,'Job-Executor-1','Starting','Done',1266937897,7,NULL,0,'User'),('7400955e-f6c3-4482-b521-d2ac66ef7e99',6602001285584,1274465633,'HA-Worker-1','Starting','Done',1274465651,41,NULL,0,'SecondaryStorageVm'),('75e1a4a0-78af-4dfe-a1f8-e7aecee67013',6602001285584,1268019205,'Job-Executor-12','Starting','Done',1268019259,18,NULL,0,'DomainRouter'),('81725168-a8ae-4849-b416-8b9b299fa92e',6602001285584,1274397108,'CP-Scan-1','Starting','Done',1274397109,40,NULL,0,'ConsoleProxy'),('85d3e9b0-4342-49e7-9a7f-0cda471a734a',6602001285584,1268569436,'Job-Executor-22','Starting','Done',1268569480,2,NULL,0,'SecondaryStorageVm'),('8b15cec0-dc27-4de5-92ce-dea585fd0b46',6602001285584,1268023842,'Job-Executor-17','Starting','Done',1268023891,19,NULL,0,'User'),('8d4c3e2e-eafb-484d-b7d2-57cb25f2f0c2',6602001285584,0,'HA-Worker-1','Starting','Done',1266933280,3,NULL,0,'User'),('8d6ee4ad-364b-4654-8143-3b902d616969',6602001285584,1274397049,'CP-Scan-1','Starting','Done',1274397050,40,NULL,0,'ConsoleProxy'),('8f182be5-76f2-42d0-98bd-7ae87c05e674',6602001285584,1274397137,'CP-Scan-1','Starting','Done',1274397138,40,NULL,0,'ConsoleProxy'),('8fe48797-eaf3-442c-aa04-8567de27a71a',6602001285584,1274397313,'CP-Scan-1','Starting','Done',1274397335,40,NULL,0,'ConsoleProxy'),('940e1b9e-dc4a-40e5-a154-7c27b7cef207',6602001285584,1270885492,'Job-Executor-8','Starting','Done',1270885492,29,NULL,0,'User'),('9b1756d3-c098-48c7-92aa-41fc1c523fc0',6602001285584,1274140664,'Job-Executor-2','Starting','Done',1274140664,2,NULL,0,'SecondaryStorageVm'),('9e94d5e3-fd99-4dd9-a05f-27809ea7e493',6602001285584,1271433484,'Job-Executor-20','Starting','Done',1271433484,34,NULL,0,'User'),('a1c25aad-487f-4466-b6ee-fd1181b01f77',6602001285584,1274140850,'Job-Executor-4','Starting','Done',1274140850,1,NULL,0,'ConsoleProxy'),('aa9f51e9-ee55-4462-b35c-e18d7281760b',6602001285584,1270440297,'Job-Executor-1','Starting','Done',1270440297,26,NULL,0,'User'),('abae43fd-7c0c-4db3-9daa-01f2c9401f79',6602001285584,1269206938,'Job-Executor-11','Starting','Done',1269207003,21,NULL,0,'User'),('abec3380-b643-42c8-b8df-5c763af8ca41',6602001285584,1267396608,'HA-Worker-0','Starting','Done',1267396608,3,NULL,0,'User'),('afcd739e-bd61-4c86-8a79-7d1e5b9b541b',6602001285584,1271821092,'Job-Executor-25','Starting','Done',1271821167,35,NULL,0,'User'),('b26cf03a-a34d-48b7-8856-8b5908b244f8',6602001285584,1273179771,'Job-Executor-2','Starting','Done',1273179771,38,NULL,0,'User'),('b4959af5-b18d-413f-9eaf-3e389231e390',6602001285584,1269208596,'Job-Executor-19','Starting','Done',1269208596,24,NULL,0,'User'),('b8526eca-5f4f-463c-805a-b2dbe572b14d',6602001285584,1274560837,'Job-Executor-9','Starting','Done',1274560888,43,NULL,0,'DomainRouter'),('bcd6e5c2-a419-44a8-9042-ec9f70cf93d0',6602001285584,1274140251,'SS-Scan-1','Starting','Done',1274140354,41,NULL,0,'SecondaryStorageVm'),('bd648766-5cc5-4482-8a77-3b65b86f3ae7',6602001285584,1266921274,'Job-Executor-1','Starting','Done',1266921372,3,NULL,0,'User'),('c0138da0-9831-491b-9dcd-cee60fffb6ff',6602001285584,1274561691,'Job-Executor-12','Starting','Done',1274561691,36,NULL,0,'DomainRouter'),('c4dac56f-e6d8-48b7-b83d-29162b48bc33',6602001285584,1274465014,'CP-Scan-1','Starting','Done',1274465043,40,NULL,0,'ConsoleProxy'),('c93e078d-3966-4f6e-99d9-04588d5ee4ec',6602001285584,1273179697,'Job-Executor-1','Starting','Done',1273179697,37,NULL,0,'User'),('cdf0ec36-532b-42be-8686-18e629758d46',6602001285584,1274464989,'HA-Worker-2','Starting','Done',1274464989,41,NULL,0,'SecondaryStorageVm'),('d1e95915-292e-45ea-9502-2a058cb78fda',6602001285584,1267835725,'Job-Executor-11','Starting','Done',1267835847,16,NULL,0,'User'),('d27d1654-9905-4f18-b7f3-91943ace3bc6',6602001285584,1270899567,'Job-Executor-9','Starting','Done',1270899568,30,NULL,0,'User'),('d2d9567d-a9f3-4b9a-88fa-66ec301c51bc',6602001285584,1273179909,'Job-Executor-3','Starting','Done',1273179909,39,NULL,0,'User'),('d2e870b4-26b9-48f0-81c0-58d1e2d057ad',6602001285584,1270884109,'Job-Executor-6','Starting','Done',1270884109,28,NULL,0,'User'),('d35bc661-40a2-4d67-86a8-54ca2cecbe57',6602001285584,1274561719,'Job-Executor-12','Starting','Done',1274561719,6,NULL,0,'DomainRouter'),('d5ce371d-54fc-40fd-9bfc-bf5f86ab4759',6602001285584,1274397196,'CP-Scan-1','Starting','Done',1274397197,40,NULL,0,'ConsoleProxy'),('d8949de3-cac2-4ee1-bd99-e3c0acb703bb',6602001285584,1266921321,'Job-Executor-1','Starting','Done',1266921360,4,NULL,0,'DomainRouter'),('dbc73bab-8150-41d4-8d5a-7e1501cf265c',6602001285584,1268019204,'Job-Executor-12','Starting','Done',1268019271,17,NULL,0,'User'),('e0a385ee-0511-42d6-8bba-aaf2b218bca5',6602001285584,1274569953,'Job-Executor-25','Starting','Done',1274570071,44,NULL,0,'User'),('e14d056a-d06a-47e3-a491-85bf101263dd',6602001285584,1274397489,'CP-Scan-1','Starting','Done',1274397509,40,NULL,0,'ConsoleProxy'),('e3419ca5-0ae4-46cc-83b6-b6f1fb1e99a5',6602001285584,1269208469,'Job-Executor-19','Starting','Done',1269208563,23,NULL,0,'User'),('e668209f-7dc7-4979-8bf3-d104f047d77d',6602001285584,1266921539,'Job-Executor-2','Starting','Done',1266921551,7,NULL,0,'User'),('e690c60d-e567-456f-bd2d-33116d4cc614',6602001285584,1266915372,'SS-Scan-1','Starting','Done',1266915559,2,NULL,0,'SecondaryStorageVm'),('eb216d26-24e8-4243-8c4c-b814e5d0157f',6602001285584,1274396958,'HA-Worker-3','Starting','Done',1274396958,41,NULL,0,'SecondaryStorageVm'),('ef198add-9a5a-43bf-9931-836c272267b2',6602001285584,1270962424,'Job-Executor-10','Starting','Done',1270962424,31,NULL,0,'User'),('f889022c-ae42-4170-9734-19248927e5eb',6602001285584,1267384790,'Job-Executor-6','Starting','Done',1267384889,11,NULL,0,'User'),('fc53c04e-0e7c-4129-9dab-a9d7a07f5e96',6602001285584,1274560759,'Job-Executor-9','Starting','Done',1274560901,42,NULL,0,'User'),('ff649199-bbef-4b8f-ad1f-b2d46ea5ab19',6602001285584,1268023843,'Job-Executor-17','Starting','Done',1268023881,20,NULL,0,'DomainRouter');
/*!40000 ALTER TABLE `op_it_work` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `op_lock`
--

DROP TABLE IF EXISTS `op_lock`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `op_lock` (
  `key` varchar(128) NOT NULL COMMENT 'primary key of the table',
  `mac` varchar(17) NOT NULL COMMENT 'mac address of who acquired this lock',
  `ip` varchar(15) NOT NULL COMMENT 'ip address of who acquired this lock',
  `thread` varchar(255) NOT NULL COMMENT 'Thread that acquired this lock',
  `acquired_on` timestamp NOT NULL default CURRENT_TIMESTAMP COMMENT 'Time acquired',
  `waiters` int(11) NOT NULL default '0' COMMENT 'How many have waited for this',
  PRIMARY KEY  (`key`),
  UNIQUE KEY `key` (`key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `op_lock`
--

LOCK TABLES `op_lock` WRITE;
/*!40000 ALTER TABLE `op_lock` DISABLE KEYS */;
/*!40000 ALTER TABLE `op_lock` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `op_networks`
--

DROP TABLE IF EXISTS `op_networks`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `op_networks` (
  `id` bigint(20) unsigned NOT NULL,
  `mac_address_seq` bigint(20) unsigned NOT NULL default '1' COMMENT 'mac address',
  `nics_count` int(10) unsigned NOT NULL default '0' COMMENT '# of nics',
  `gc` tinyint(3) unsigned NOT NULL default '1' COMMENT 'gc this network or not',
  `check_for_gc` tinyint(3) unsigned NOT NULL default '1' COMMENT 'check this network for gc or not',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`),
  CONSTRAINT `fk_op_networks__id` FOREIGN KEY (`id`) REFERENCES `networks` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `op_networks`
--

LOCK TABLES `op_networks` WRITE;
/*!40000 ALTER TABLE `op_networks` DISABLE KEYS */;
INSERT INTO `op_networks` VALUES (200,1,6,0,1),(201,1,2,0,1),(202,1,6,0,1),(203,1,0,0,0),(204,5,0,1,0),(205,8,0,1,0),(206,3,0,1,0),(207,3,0,1,0),(208,3,0,1,0),(209,3,0,1,0),(210,3,0,1,0),(211,7,0,1,0),(212,2,0,1,1),(213,3,0,1,1),(214,3,0,1,1),(215,3,0,1,0),(216,3,0,1,1),(217,2,0,1,1),(218,1,0,0,0),(219,1,0,0,0),(220,1,0,0,0),(221,1,0,0,0),(222,3,0,1,0),(223,3,0,1,0),(224,3,1,1,1);
/*!40000 ALTER TABLE `op_networks` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `op_nwgrp_work`
--

DROP TABLE IF EXISTS `op_nwgrp_work`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `op_nwgrp_work` (
  `id` bigint(20) unsigned NOT NULL auto_increment COMMENT 'id',
  `instance_id` bigint(20) unsigned NOT NULL COMMENT 'vm instance that needs rules to be synced.',
  `mgmt_server_id` bigint(20) unsigned default NULL COMMENT 'management server that has taken up the work of doing rule sync',
  `created` datetime NOT NULL COMMENT 'time the entry was requested',
  `taken` datetime default NULL COMMENT 'time it was taken by the management server',
  `step` varchar(32) NOT NULL COMMENT 'Step in the work',
  `seq_no` bigint(20) unsigned default NULL COMMENT 'seq number to be sent to agent, uniquely identifies ruleset update',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`),
  KEY `i_op_nwgrp_work__instance_id` (`instance_id`),
  KEY `i_op_nwgrp_work__mgmt_server_id` (`mgmt_server_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `op_nwgrp_work`
--

LOCK TABLES `op_nwgrp_work` WRITE;
/*!40000 ALTER TABLE `op_nwgrp_work` DISABLE KEYS */;
/*!40000 ALTER TABLE `op_nwgrp_work` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `op_pod_vlan_alloc`
--

DROP TABLE IF EXISTS `op_pod_vlan_alloc`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `op_pod_vlan_alloc` (
  `id` bigint(20) unsigned NOT NULL auto_increment COMMENT 'primary id',
  `vlan` varchar(18) NOT NULL COMMENT 'vlan id',
  `data_center_id` bigint(20) unsigned NOT NULL COMMENT 'data center the pod belongs to',
  `pod_id` bigint(20) unsigned NOT NULL COMMENT 'pod the vlan belongs to',
  `account_id` bigint(20) unsigned default NULL COMMENT 'account the vlan belongs to right now',
  `taken` datetime default NULL COMMENT 'Date taken',
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `op_pod_vlan_alloc`
--

LOCK TABLES `op_pod_vlan_alloc` WRITE;
/*!40000 ALTER TABLE `op_pod_vlan_alloc` DISABLE KEYS */;
/*!40000 ALTER TABLE `op_pod_vlan_alloc` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `op_vm_ruleset_log`
--

DROP TABLE IF EXISTS `op_vm_ruleset_log`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `op_vm_ruleset_log` (
  `id` bigint(20) unsigned NOT NULL auto_increment COMMENT 'id',
  `instance_id` bigint(20) unsigned NOT NULL COMMENT 'vm instance that needs rules to be synced.',
  `created` datetime NOT NULL COMMENT 'time the entry was requested',
  `logsequence` bigint(20) unsigned default NULL COMMENT 'seq number to be sent to agent, uniquely identifies ruleset update',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `op_vm_ruleset_log`
--

LOCK TABLES `op_vm_ruleset_log` WRITE;
/*!40000 ALTER TABLE `op_vm_ruleset_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `op_vm_ruleset_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ovs_host_vlan_alloc`
--

DROP TABLE IF EXISTS `ovs_host_vlan_alloc`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `ovs_host_vlan_alloc` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `host_id` bigint(20) unsigned default NULL COMMENT 'host id',
  `account_id` bigint(20) unsigned default NULL COMMENT 'account id',
  `vlan` bigint(20) unsigned default NULL COMMENT 'vlan id under account #account_id on host #host_id',
  `ref` int(10) unsigned NOT NULL default '0' COMMENT 'reference count',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `ovs_host_vlan_alloc`
--

LOCK TABLES `ovs_host_vlan_alloc` WRITE;
/*!40000 ALTER TABLE `ovs_host_vlan_alloc` DISABLE KEYS */;
/*!40000 ALTER TABLE `ovs_host_vlan_alloc` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ovs_tunnel`
--

DROP TABLE IF EXISTS `ovs_tunnel`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `ovs_tunnel` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `from` bigint(20) unsigned NOT NULL default '0' COMMENT 'from host id',
  `to` bigint(20) unsigned NOT NULL default '0' COMMENT 'to host id',
  `key` int(10) unsigned default '0' COMMENT 'current gre key can be used',
  PRIMARY KEY  (`from`,`to`),
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `ovs_tunnel`
--

LOCK TABLES `ovs_tunnel` WRITE;
/*!40000 ALTER TABLE `ovs_tunnel` DISABLE KEYS */;
/*!40000 ALTER TABLE `ovs_tunnel` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ovs_tunnel_account`
--

DROP TABLE IF EXISTS `ovs_tunnel_account`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `ovs_tunnel_account` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `from` bigint(20) unsigned NOT NULL default '0' COMMENT 'from host id',
  `to` bigint(20) unsigned NOT NULL default '0' COMMENT 'to host id',
  `account` bigint(20) unsigned NOT NULL default '0' COMMENT 'account',
  `key` int(10) unsigned default NULL COMMENT 'gre key',
  `port_name` varchar(32) default NULL COMMENT 'in port on open vswitch',
  `state` varchar(16) default 'FAILED' COMMENT 'result of tunnel creatation',
  PRIMARY KEY  (`from`,`to`,`account`),
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `ovs_tunnel_account`
--

LOCK TABLES `ovs_tunnel_account` WRITE;
/*!40000 ALTER TABLE `ovs_tunnel_account` DISABLE KEYS */;
INSERT INTO `ovs_tunnel_account` VALUES (1,0,0,0,0,'lock','SUCCESS');
/*!40000 ALTER TABLE `ovs_tunnel_account` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ovs_tunnel_alloc`
--

DROP TABLE IF EXISTS `ovs_tunnel_alloc`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `ovs_tunnel_alloc` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `from` bigint(20) unsigned NOT NULL default '0' COMMENT 'from host id',
  `to` bigint(20) unsigned NOT NULL default '0' COMMENT 'to host id',
  `in_port` int(10) unsigned default NULL COMMENT 'in port on open vswitch',
  PRIMARY KEY  (`from`,`to`),
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `ovs_tunnel_alloc`
--

LOCK TABLES `ovs_tunnel_alloc` WRITE;
/*!40000 ALTER TABLE `ovs_tunnel_alloc` DISABLE KEYS */;
/*!40000 ALTER TABLE `ovs_tunnel_alloc` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ovs_vlan_mapping_dirty`
--

DROP TABLE IF EXISTS `ovs_vlan_mapping_dirty`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `ovs_vlan_mapping_dirty` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `account_id` bigint(20) unsigned default NULL COMMENT 'account id',
  `dirty` int(1) unsigned NOT NULL default '0' COMMENT '1 means vlan mapping of this account was changed',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `ovs_vlan_mapping_dirty`
--

LOCK TABLES `ovs_vlan_mapping_dirty` WRITE;
/*!40000 ALTER TABLE `ovs_vlan_mapping_dirty` DISABLE KEYS */;
/*!40000 ALTER TABLE `ovs_vlan_mapping_dirty` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ovs_vm_flow_log`
--

DROP TABLE IF EXISTS `ovs_vm_flow_log`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `ovs_vm_flow_log` (
  `id` bigint(20) unsigned NOT NULL auto_increment COMMENT 'id',
  `instance_id` bigint(20) unsigned NOT NULL COMMENT 'vm instance that needs flows to be synced.',
  `created` datetime NOT NULL COMMENT 'time the entry was requested',
  `logsequence` bigint(20) unsigned default NULL COMMENT 'seq number to be sent to agent, uniquely identifies flow update',
  `vm_name` varchar(255) NOT NULL COMMENT 'vm name',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `ovs_vm_flow_log`
--

LOCK TABLES `ovs_vm_flow_log` WRITE;
/*!40000 ALTER TABLE `ovs_vm_flow_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `ovs_vm_flow_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ovs_work`
--

DROP TABLE IF EXISTS `ovs_work`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `ovs_work` (
  `id` bigint(20) unsigned NOT NULL auto_increment COMMENT 'id',
  `instance_id` bigint(20) unsigned NOT NULL COMMENT 'vm instance that needs rules to be synced.',
  `mgmt_server_id` bigint(20) unsigned default NULL COMMENT 'management server that has taken up the work of doing rule sync',
  `created` datetime NOT NULL COMMENT 'time the entry was requested',
  `taken` datetime default NULL COMMENT 'time it was taken by the management server',
  `step` varchar(32) NOT NULL COMMENT 'Step in the work',
  `seq_no` bigint(20) unsigned default NULL COMMENT 'seq number to be sent to agent, uniquely identifies ruleset update',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `ovs_work`
--

LOCK TABLES `ovs_work` WRITE;
/*!40000 ALTER TABLE `ovs_work` DISABLE KEYS */;
/*!40000 ALTER TABLE `ovs_work` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pod_vlan_map`
--

DROP TABLE IF EXISTS `pod_vlan_map`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `pod_vlan_map` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `pod_id` bigint(20) unsigned NOT NULL COMMENT 'pod id. foreign key to pod table',
  `vlan_db_id` bigint(20) unsigned NOT NULL COMMENT 'database id of vlan. foreign key to vlan table',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`),
  KEY `i_pod_vlan_map__pod_id` (`pod_id`),
  KEY `i_pod_vlan_map__vlan_id` (`vlan_db_id`),
  CONSTRAINT `fk_pod_vlan_map__pod_id` FOREIGN KEY (`pod_id`) REFERENCES `host_pod_ref` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_pod_vlan_map__vlan_id` FOREIGN KEY (`vlan_db_id`) REFERENCES `vlan` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `pod_vlan_map`
--

LOCK TABLES `pod_vlan_map` WRITE;
/*!40000 ALTER TABLE `pod_vlan_map` DISABLE KEYS */;
/*!40000 ALTER TABLE `pod_vlan_map` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `port_forwarding_rules`
--

DROP TABLE IF EXISTS `port_forwarding_rules`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `port_forwarding_rules` (
  `id` bigint(20) unsigned NOT NULL COMMENT 'id',
  `instance_id` bigint(20) unsigned NOT NULL COMMENT 'vm instance id',
  `dest_ip_address` char(40) NOT NULL COMMENT 'id_address',
  `dest_port_start` int(10) NOT NULL COMMENT 'starting port of the port range to map to',
  `dest_port_end` int(10) NOT NULL COMMENT 'end port of the the port range to map to',
  PRIMARY KEY  (`id`),
  CONSTRAINT `fk_port_forwarding_rules__id` FOREIGN KEY (`id`) REFERENCES `firewall_rules` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `port_forwarding_rules`
--

LOCK TABLES `port_forwarding_rules` WRITE;
/*!40000 ALTER TABLE `port_forwarding_rules` DISABLE KEYS */;
INSERT INTO `port_forwarding_rules` VALUES (3,16,'10.1.1.186',22,22),(5,46,'10.1.3.47',80,80);
/*!40000 ALTER TABLE `port_forwarding_rules` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `remote_access_vpn`
--

DROP TABLE IF EXISTS `remote_access_vpn`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `remote_access_vpn` (
  `vpn_server_addr_id` bigint(20) unsigned NOT NULL,
  `account_id` bigint(20) unsigned NOT NULL,
  `network_id` bigint(20) unsigned NOT NULL,
  `domain_id` bigint(20) unsigned NOT NULL,
  `local_ip` varchar(15) NOT NULL,
  `ip_range` varchar(32) NOT NULL,
  `ipsec_psk` varchar(256) NOT NULL,
  `state` char(32) NOT NULL,
  PRIMARY KEY  (`vpn_server_addr_id`),
  UNIQUE KEY `vpn_server_addr_id` (`vpn_server_addr_id`),
  KEY `fk_remote_access_vpn__account_id` (`account_id`),
  KEY `fk_remote_access_vpn__domain_id` (`domain_id`),
  KEY `fk_remote_access_vpn__network_id` (`network_id`),
  CONSTRAINT `fk_remote_access_vpn__account_id` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_remote_access_vpn__domain_id` FOREIGN KEY (`domain_id`) REFERENCES `domain` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_remote_access_vpn__network_id` FOREIGN KEY (`network_id`) REFERENCES `networks` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_remote_access_vpn__vpn_server_addr_id` FOREIGN KEY (`vpn_server_addr_id`) REFERENCES `user_ip_address` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `remote_access_vpn`
--

LOCK TABLES `remote_access_vpn` WRITE;
/*!40000 ALTER TABLE `remote_access_vpn` DISABLE KEYS */;
/*!40000 ALTER TABLE `remote_access_vpn` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `resource_count`
--

DROP TABLE IF EXISTS `resource_count`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `resource_count` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `account_id` bigint(20) unsigned default NULL,
  `domain_id` bigint(20) unsigned default NULL,
  `type` varchar(255) default NULL,
  `count` bigint(20) NOT NULL default '0',
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=130 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `resource_count`
--

LOCK TABLES `resource_count` WRITE;
/*!40000 ALTER TABLE `resource_count` DISABLE KEYS */;
INSERT INTO `resource_count` VALUES (1,4,NULL,'public_ip',2),(2,NULL,4,'public_ip',2),(3,NULL,3,'public_ip',3),(4,NULL,2,'public_ip',12),(5,NULL,1,'public_ip',16),(6,4,NULL,'volume',1),(7,NULL,4,'volume',1),(8,NULL,3,'volume',1),(9,NULL,2,'volume',2),(10,NULL,1,'volume',6),(11,4,NULL,'user_vm',-4),(12,NULL,4,'user_vm',-4),(13,NULL,3,'user_vm',-4),(14,NULL,2,'user_vm',0),(15,NULL,1,'user_vm',-19),(16,2,NULL,'public_ip',3),(17,2,NULL,'volume',3),(18,2,NULL,'user_vm',-1),(19,2,NULL,'snapshot',1),(20,NULL,1,'snapshot',7),(21,2,NULL,'template',16),(22,NULL,1,'template',15),(23,6,NULL,'public_ip',1),(24,NULL,7,'public_ip',1),(25,NULL,6,'public_ip',1),(26,6,NULL,'volume',0),(27,NULL,7,'volume',0),(28,NULL,6,'volume',0),(29,6,NULL,'user_vm',1),(30,NULL,7,'user_vm',1),(31,NULL,6,'user_vm',1),(32,24,NULL,'public_ip',1),(33,NULL,22,'public_ip',1),(34,NULL,20,'public_ip',3),(35,24,NULL,'volume',1),(36,NULL,22,'volume',1),(37,NULL,20,'volume',3),(38,24,NULL,'user_vm',0),(39,NULL,22,'user_vm',0),(40,NULL,20,'user_vm',-2),(41,22,NULL,'public_ip',1),(42,NULL,21,'public_ip',1),(43,22,NULL,'volume',1),(44,NULL,21,'volume',1),(45,22,NULL,'user_vm',0),(46,NULL,21,'user_vm',0),(47,26,NULL,'public_ip',0),(48,NULL,24,'public_ip',0),(49,26,NULL,'volume',-1),(50,NULL,24,'volume',-1),(51,26,NULL,'user_vm',1),(52,NULL,24,'user_vm',1),(53,30,NULL,'public_ip',0),(54,NULL,27,'public_ip',0),(55,NULL,25,'public_ip',0),(56,30,NULL,'volume',-1),(57,NULL,27,'volume',-1),(58,NULL,25,'volume',-1),(59,30,NULL,'user_vm',1),(60,NULL,27,'user_vm',1),(61,NULL,25,'user_vm',1),(62,20,NULL,'template',2),(63,NULL,20,'template',2),(64,4,NULL,'snapshot',0),(65,NULL,4,'snapshot',0),(66,NULL,3,'snapshot',0),(67,NULL,2,'snapshot',0),(68,24,NULL,'snapshot',2),(69,NULL,22,'snapshot',2),(70,NULL,20,'snapshot',6),(71,20,NULL,'public_ip',1),(72,20,NULL,'volume',1),(73,20,NULL,'user_vm',-2),(74,20,NULL,'snapshot',4),(75,3,NULL,'public_ip',1),(76,3,NULL,'volume',0),(77,3,NULL,'user_vm',0),(78,42,NULL,'public_ip',2),(79,NULL,39,'public_ip',3),(80,42,NULL,'volume',0),(81,NULL,39,'volume',0),(82,42,NULL,'user_vm',-1),(83,NULL,39,'user_vm',-1),(84,51,NULL,'public_ip',1),(85,NULL,45,'public_ip',1),(86,51,NULL,'volume',0),(87,NULL,45,'volume',0),(88,51,NULL,'user_vm',0),(89,NULL,45,'user_vm',0),(90,42,NULL,'template',0),(91,NULL,39,'template',0),(92,NULL,2,'template',0),(93,56,NULL,'public_ip',1),(94,NULL,48,'public_ip',1),(95,NULL,47,'public_ip',1),(96,56,NULL,'volume',2),(97,NULL,48,'volume',2),(98,NULL,47,'volume',2),(99,56,NULL,'user_vm',0),(100,NULL,48,'user_vm',0),(101,NULL,47,'user_vm',0),(102,61,NULL,'public_ip',1),(103,NULL,52,'public_ip',2),(104,NULL,51,'public_ip',2),(105,61,NULL,'volume',0),(106,NULL,52,'volume',0),(107,NULL,51,'volume',0),(108,61,NULL,'user_vm',0),(109,NULL,52,'user_vm',0),(110,NULL,51,'user_vm',0),(111,60,NULL,'public_ip',1),(112,60,NULL,'volume',0),(113,60,NULL,'user_vm',0),(114,1,NULL,'user_vm',-16),(115,1,NULL,'public_ip',-2),(116,1,NULL,'volume',-2),(117,1,NULL,'template',-3),(118,69,NULL,'public_ip',0),(119,NULL,59,'public_ip',0),(120,69,NULL,'volume',-1),(121,NULL,59,'volume',-1),(122,69,NULL,'user_vm',1),(123,NULL,59,'user_vm',1),(124,70,NULL,'public_ip',2),(125,NULL,60,'public_ip',2),(126,70,NULL,'volume',2),(127,NULL,60,'volume',2),(128,70,NULL,'user_vm',1),(129,NULL,60,'user_vm',1);
/*!40000 ALTER TABLE `resource_count` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `resource_limit`
--

DROP TABLE IF EXISTS `resource_limit`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `resource_limit` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `domain_id` bigint(20) unsigned default NULL,
  `account_id` bigint(20) unsigned default NULL,
  `type` varchar(255) default NULL,
  `max` bigint(20) NOT NULL default '-1',
  PRIMARY KEY  (`id`),
  KEY `i_resource_limit__domain_id` (`domain_id`),
  KEY `i_resource_limit__account_id` (`account_id`),
  CONSTRAINT `fk_resource_limit__account_id` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_resource_limit__domain_id` FOREIGN KEY (`domain_id`) REFERENCES `domain` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `resource_limit`
--

LOCK TABLES `resource_limit` WRITE;
/*!40000 ALTER TABLE `resource_limit` DISABLE KEYS */;
INSERT INTO `resource_limit` VALUES (3,1,NULL,'snapshot',6),(5,1,NULL,'user_vm',20),(6,20,NULL,'user_vm',5);
/*!40000 ALTER TABLE `resource_limit` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `secondary_storage_vm`
--

DROP TABLE IF EXISTS `secondary_storage_vm`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `secondary_storage_vm` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `public_mac_address` varchar(17) default NULL COMMENT 'mac address of the public facing network card',
  `public_ip_address` varchar(15) default NULL COMMENT 'public ip address for the sec storage vm',
  `public_netmask` varchar(15) default NULL COMMENT 'public netmask used for the sec storage vm',
  `ram_size` int(10) unsigned NOT NULL default '512' COMMENT 'memory to use in mb',
  `guid` varchar(255) default NULL COMMENT 'copied from guid of secondary storage host',
  `nfs_share` varchar(255) default NULL COMMENT 'server and path exported by the nfs server ',
  `last_update` datetime default NULL COMMENT 'Last session update time',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `public_mac_address` (`public_mac_address`),
  UNIQUE KEY `public_ip_address` (`public_ip_address`),
  CONSTRAINT `fk_secondary_storage_vm__id` FOREIGN KEY (`id`) REFERENCES `vm_instance` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=42 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `secondary_storage_vm`
--

LOCK TABLES `secondary_storage_vm` WRITE;
/*!40000 ALTER TABLE `secondary_storage_vm` DISABLE KEYS */;
INSERT INTO `secondary_storage_vm` VALUES (2,'06:b8:b6:00:00:11','66.149.231.114','255.255.255.0',0,NULL,NULL,NULL),(41,'06:f2:2e:00:00:0e','172.16.64.48','255.255.252.0',512,NULL,NULL,NULL);
/*!40000 ALTER TABLE `secondary_storage_vm` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `security_group`
--

DROP TABLE IF EXISTS `security_group`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `security_group` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `name` varchar(255) NOT NULL,
  `description` varchar(4096) default NULL,
  `domain_id` bigint(20) unsigned NOT NULL,
  `account_id` bigint(20) unsigned NOT NULL,
  `account_name` varchar(100) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `fk_security_group__account_id` (`account_id`),
  KEY `fk_security_group__domain_id` (`domain_id`),
  KEY `i_security_group_name` (`name`),
  CONSTRAINT `fk_security_group__domain_id` FOREIGN KEY (`domain_id`) REFERENCES `domain` (`id`),
  CONSTRAINT `fk_security_group___account_id` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=33 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `security_group`
--

LOCK TABLES `security_group` WRITE;
/*!40000 ALTER TABLE `security_group` DISABLE KEYS */;
INSERT INTO `security_group` VALUES (1,'default','Default Security Group',36,39,'partnetisisue'),(2,'default','Default Security Group',37,40,'cindy'),(3,'default','Default Security Group',38,41,'agarwal123'),(4,'default','Default Security Group',39,42,'shweta'),(5,'default','Default Security Group',40,43,'shweta'),(6,'default','Default Security Group',41,44,'shweta'),(7,'default','Default Security Group',42,45,'manish123'),(8,'default','Default Security Group',43,46,'manish123'),(9,'default','Default Security Group',44,47,'manish123'),(10,'default','Default Security Group',44,48,'manish12'),(11,'default','Default Security Group',40,49,'test1'),(12,'default','Default Security Group',40,50,'test2'),(13,'default','Default Security Group',45,51,'test2'),(14,'default','Default Security Group',46,52,'Koteswar'),(15,'default','Default Security Group',21,53,'murali'),(16,'default','Default Security Group',47,54,'atmos'),(17,'default','Default Security Group',48,55,'atmos'),(18,'default','Default Security Group',48,56,'atmosuser'),(19,'default','Default Security Group',49,57,'pmorris'),(20,'default','Default Security Group',50,58,'pmorris'),(21,'default','Default Security Group',51,59,'pmorris'),(22,'default','Default Security Group',52,60,'pmorris'),(23,'default','Default Security Group',52,61,'pauluser'),(24,'default','Default Security Group',53,62,'magarwal'),(25,'default','Default Security Group',54,63,'manishtest'),(26,'default','Default Security Group',55,64,'dnoland2'),(27,'default','Default Security Group',56,65,'kkagarwal'),(28,'default','Default Security Group',57,66,'kkagarwal'),(29,'default','Default Security Group',57,67,'kkuser'),(30,'default','Default Security Group',58,68,'dnoland2'),(32,'default','Default Security Group',60,70,'deepa');
/*!40000 ALTER TABLE `security_group` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `security_group_vm_map`
--

DROP TABLE IF EXISTS `security_group_vm_map`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `security_group_vm_map` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `security_group_id` bigint(20) unsigned NOT NULL,
  `instance_id` bigint(20) unsigned NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `fk_security_group_vm_map___security_group_id` (`security_group_id`),
  KEY `fk_security_group_vm_map___instance_id` (`instance_id`),
  CONSTRAINT `fk_security_group_vm_map___instance_id` FOREIGN KEY (`instance_id`) REFERENCES `user_vm` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_security_group_vm_map___security_group_id` FOREIGN KEY (`security_group_id`) REFERENCES `security_group` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `security_group_vm_map`
--

LOCK TABLES `security_group_vm_map` WRITE;
/*!40000 ALTER TABLE `security_group_vm_map` DISABLE KEYS */;
/*!40000 ALTER TABLE `security_group_vm_map` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `security_ingress_rule`
--

DROP TABLE IF EXISTS `security_ingress_rule`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `security_ingress_rule` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `security_group_id` bigint(20) unsigned NOT NULL,
  `start_port` varchar(10) default NULL,
  `end_port` varchar(10) default NULL,
  `protocol` varchar(16) NOT NULL default 'TCP',
  `allowed_network_id` bigint(20) unsigned default NULL,
  `allowed_security_group` varchar(255) default NULL COMMENT 'data duplicated from security_group table to avoid lots of joins when listing rules (the name of the group should be displayed rather than just id)',
  `allowed_sec_grp_acct` varchar(100) default NULL COMMENT 'data duplicated from security_group table to avoid lots of joins when listing rules (the name of the group owner should be displayed)',
  `allowed_ip_cidr` varchar(44) default NULL,
  `create_status` varchar(32) default NULL COMMENT 'rule creation status',
  PRIMARY KEY  (`id`),
  KEY `i_security_ingress_rule_network_id` (`security_group_id`),
  KEY `i_security_ingress_rule_allowed_network` (`allowed_network_id`),
  CONSTRAINT `fk_security_ingress_rule___allowed_network_id` FOREIGN KEY (`allowed_network_id`) REFERENCES `security_group` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_security_ingress_rule___security_group_id` FOREIGN KEY (`security_group_id`) REFERENCES `security_group` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `security_ingress_rule`
--

LOCK TABLES `security_ingress_rule` WRITE;
/*!40000 ALTER TABLE `security_ingress_rule` DISABLE KEYS */;
/*!40000 ALTER TABLE `security_ingress_rule` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sequence`
--

DROP TABLE IF EXISTS `sequence`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `sequence` (
  `name` varchar(64) NOT NULL COMMENT 'name of the sequence',
  `value` bigint(20) unsigned NOT NULL COMMENT 'sequence value',
  PRIMARY KEY  (`name`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `sequence`
--

LOCK TABLES `sequence` WRITE;
/*!40000 ALTER TABLE `sequence` DISABLE KEYS */;
INSERT INTO `sequence` VALUES ('networks_seq',225),('private_mac_address_seq',1),('public_mac_address_seq',1),('snapshots_seq',1),('storage_pool_seq',202),('vm_instance_seq',48),('vm_template_seq',248),('volume_seq',1);
/*!40000 ALTER TABLE `sequence` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `service_offering`
--

DROP TABLE IF EXISTS `service_offering`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `service_offering` (
  `id` bigint(20) unsigned NOT NULL,
  `cpu` int(10) unsigned NOT NULL COMMENT '# of cores',
  `speed` int(10) unsigned NOT NULL COMMENT 'speed per core in mhz',
  `ram_size` bigint(20) unsigned NOT NULL,
  `nw_rate` smallint(5) unsigned default '200' COMMENT 'network rate throttle mbits/s',
  `mc_rate` smallint(5) unsigned default '10' COMMENT 'mcast rate throttle mbits/s',
  `ha_enabled` tinyint(1) unsigned NOT NULL default '0' COMMENT 'Enable HA',
  `guest_ip_type` varchar(255) NOT NULL default 'Virtualized' COMMENT 'Type of guest network -- direct or virtualized',
  `host_tag` varchar(255) default NULL COMMENT 'host tag specified by the service_offering',
  PRIMARY KEY  (`id`),
  CONSTRAINT `fk_service_offering__id` FOREIGN KEY (`id`) REFERENCES `disk_offering` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `service_offering`
--

LOCK TABLES `service_offering` WRITE;
/*!40000 ALTER TABLE `service_offering` DISABLE KEYS */;
INSERT INTO `service_offering` VALUES (1,1,500,512,200,10,0,'Direct',NULL),(2,1,1000,1024,200,10,0,'Direct',NULL),(6,1,512,1024,0,0,1,'Virtual',NULL),(7,1,500,256,0,0,1,'Virtual',NULL),(8,1,500,128,0,0,1,'Virtual',NULL),(9,1,1200,256,200,10,1,'Virtual',NULL),(10,1,1200,256,200,10,1,'Virtual',NULL),(11,1,1200,256,200,10,1,'Virtual',NULL),(12,1,1200,256,200,10,1,'Virtual',NULL),(25,1,1200,256,200,10,1,'Virtual',NULL);
/*!40000 ALTER TABLE `service_offering` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `snapshot_policy`
--

DROP TABLE IF EXISTS `snapshot_policy`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `snapshot_policy` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `volume_id` bigint(20) unsigned NOT NULL,
  `schedule` varchar(100) NOT NULL COMMENT 'schedule time of execution',
  `timezone` varchar(100) NOT NULL COMMENT 'the timezone in which the schedule time is specified',
  `interval` int(4) NOT NULL default '4' COMMENT 'backup schedule, e.g. hourly, daily, etc.',
  `max_snaps` int(8) NOT NULL default '0' COMMENT 'maximum number of snapshots to maintain',
  `active` tinyint(1) unsigned NOT NULL COMMENT 'Is the policy active',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `volume_id` (`volume_id`),
  KEY `i_snapshot_policy__volume_id` (`volume_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `snapshot_policy`
--

LOCK TABLES `snapshot_policy` WRITE;
/*!40000 ALTER TABLE `snapshot_policy` DISABLE KEYS */;
INSERT INTO `snapshot_policy` VALUES (1,0,'00','GMT',4,0,1),(2,14,'00:00:1','Etc/GMT+12',3,2,1);
/*!40000 ALTER TABLE `snapshot_policy` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `snapshot_policy_ref`
--

DROP TABLE IF EXISTS `snapshot_policy_ref`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `snapshot_policy_ref` (
  `snap_id` bigint(20) unsigned NOT NULL,
  `volume_id` bigint(20) unsigned NOT NULL,
  `policy_id` bigint(20) unsigned NOT NULL,
  UNIQUE KEY `snap_id` (`snap_id`,`policy_id`),
  KEY `i_snapshot_policy_ref__snap_id` (`snap_id`),
  KEY `i_snapshot_policy_ref__volume_id` (`volume_id`),
  KEY `i_snapshot_policy_ref__policy_id` (`policy_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `snapshot_policy_ref`
--

LOCK TABLES `snapshot_policy_ref` WRITE;
/*!40000 ALTER TABLE `snapshot_policy_ref` DISABLE KEYS */;
/*!40000 ALTER TABLE `snapshot_policy_ref` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `snapshot_schedule`
--

DROP TABLE IF EXISTS `snapshot_schedule`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `snapshot_schedule` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `volume_id` bigint(20) unsigned NOT NULL COMMENT 'The volume for which this snapshot is being taken',
  `policy_id` bigint(20) unsigned NOT NULL COMMENT 'One of the policyIds for which this snapshot was taken',
  `scheduled_timestamp` datetime NOT NULL COMMENT 'Time at which the snapshot was scheduled for execution',
  `async_job_id` bigint(20) unsigned default NULL COMMENT 'If this schedule is being executed, it is the id of the create aysnc_job. Before that it is null',
  `snapshot_id` bigint(20) unsigned default NULL COMMENT 'If this schedule is being executed, then the corresponding snapshot has this id. Before that it is null',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `volume_id` (`volume_id`),
  KEY `i_snapshot_schedule__volume_id` (`volume_id`),
  KEY `i_snapshot_schedule__policy_id` (`policy_id`),
  KEY `i_snapshot_schedule__async_job_id` (`async_job_id`),
  KEY `i_snapshot_schedule__snapshot_id` (`snapshot_id`),
  KEY `i_snapshot_schedule__scheduled_timestamp` (`scheduled_timestamp`),
  CONSTRAINT `fk__snapshot_schedule_async_job_id` FOREIGN KEY (`async_job_id`) REFERENCES `async_job` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk__snapshot_schedule_policy_id` FOREIGN KEY (`policy_id`) REFERENCES `snapshot_policy` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk__snapshot_schedule_snapshot_id` FOREIGN KEY (`snapshot_id`) REFERENCES `snapshots` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk__snapshot_schedule_volume_id` FOREIGN KEY (`volume_id`) REFERENCES `volumes` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `snapshot_schedule`
--

LOCK TABLES `snapshot_schedule` WRITE;
/*!40000 ALTER TABLE `snapshot_schedule` DISABLE KEYS */;
INSERT INTO `snapshot_schedule` VALUES (3,14,2,'2011-06-01 12:00:00',NULL,NULL);
/*!40000 ALTER TABLE `snapshot_schedule` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `snapshots`
--

DROP TABLE IF EXISTS `snapshots`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `snapshots` (
  `id` bigint(20) unsigned NOT NULL auto_increment COMMENT 'Primary Key',
  `account_id` bigint(20) unsigned NOT NULL COMMENT 'owner.  foreign key to account table',
  `volume_id` bigint(20) unsigned NOT NULL COMMENT 'volume it belongs to. foreign key to volume table',
  `status` varchar(32) default NULL COMMENT 'snapshot creation status',
  `path` varchar(255) default NULL COMMENT 'Path',
  `name` varchar(255) NOT NULL COMMENT 'snapshot name',
  `snapshot_type` int(4) NOT NULL COMMENT 'type of snapshot, e.g. manual, recurring',
  `type_description` varchar(25) default NULL COMMENT 'description of the type of snapshot, e.g. manual, recurring',
  `created` datetime default NULL COMMENT 'Date Created',
  `removed` datetime default NULL COMMENT 'Date removed.  not null if removed',
  `backup_snap_id` varchar(255) default NULL COMMENT 'Back up uuid of the snapshot',
  `prev_snap_id` bigint(20) unsigned default NULL COMMENT 'Id of the most recent snapshot',
  `hypervisor_type` varchar(32) NOT NULL COMMENT 'hypervisor that the snapshot was taken under',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`),
  KEY `i_snapshots__account_id` (`account_id`),
  KEY `i_snapshots__volume_id` (`volume_id`),
  KEY `i_snapshots__removed` (`removed`),
  KEY `i_snapshots__name` (`name`),
  KEY `i_snapshots__snapshot_type` (`snapshot_type`),
  KEY `i_snapshots__prev_snap_id` (`prev_snap_id`),
  CONSTRAINT `fk_snapshots__account_id` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=37 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `snapshots`
--

LOCK TABLES `snapshots` WRITE;
/*!40000 ALTER TABLE `snapshots` DISABLE KEYS */;
INSERT INTO `snapshots` VALUES (1,2,6,'BackedUp','2be2b08a-0e80-4b5f-b6e4-2b379b4f734a','i-2-5-VM_ROOT-5_20110210085044',0,'MANUAL','2011-02-10 08:50:44','2011-03-09 11:04:59',NULL,0,'XenServer'),(2,2,19,'BackedUp','5e78f36d-a607-4ce7-b1df-35b821e9531b','i-2-16-VM_ROOT-16_20110308123547',0,'MANUAL','2011-03-08 12:35:47','2011-03-09 11:10:52',NULL,0,'XenServer'),(3,4,4,'Creating',NULL,'i-4-3-VM_DATA-3_20110308123844',0,'MANUAL','2011-03-08 12:38:44',NULL,NULL,0,'XenServer'),(4,4,4,'BackedUp','3b2ee016-029e-4763-a283-da718e899714','i-4-3-VM_DATA-3_20110308124255',0,'MANUAL','2011-03-08 12:42:55','2011-03-09 11:22:22',NULL,0,'XenServer'),(5,4,3,'Creating',NULL,'i-4-3-VM_ROOT-3_20110308124515',0,'MANUAL','2011-03-08 12:45:15',NULL,NULL,0,'XenServer'),(6,22,16,'Creating',NULL,'i-22-14-VM_ROOT-14_20110308124606',0,'MANUAL','2011-03-08 12:46:06',NULL,NULL,0,'XenServer'),(7,22,16,'Creating',NULL,'i-22-14-VM_ROOT-14_20110308132644',0,'MANUAL','2011-03-08 13:26:44',NULL,NULL,0,'XenServer'),(8,22,17,'Creating',NULL,'i-22-14-VM_DATA-14_20110308132707',0,'MANUAL','2011-03-08 13:27:07',NULL,NULL,0,'XenServer'),(9,24,14,'BackedUp','d8ea6271-a08c-4b08-9f95-945fabb382a0','i-24-12-VM_DATA-12_20110308133006',0,'MANUAL','2011-03-08 13:30:06',NULL,'e67e2d3f-a440-47e8-9055-9c2b0438a3e0',0,'XenServer'),(10,24,14,'Creating',NULL,'i-24-12-VM_DATA-12_20110308134015',0,'MANUAL','2011-03-08 13:40:15',NULL,NULL,0,'XenServer'),(11,24,14,'Creating',NULL,'i-24-12-VM_DATA-12_20110308134318',0,'MANUAL','2011-03-08 13:43:18',NULL,NULL,0,'XenServer'),(12,24,14,'BackedUp','d8ea6271-a08c-4b08-9f95-945fabb382a0','i-24-12-VM_DATA-12_20110308134414',0,'MANUAL','2011-03-08 13:44:14',NULL,'e67e2d3f-a440-47e8-9055-9c2b0438a3e0',9,'XenServer'),(13,24,14,'BackedUp','d8ea6271-a08c-4b08-9f95-945fabb382a0','i-24-12-VM_DATA-12_20110309103130',0,'MANUAL','2011-03-09 10:31:30',NULL,'e67e2d3f-a440-47e8-9055-9c2b0438a3e0',12,'XenServer'),(14,20,24,'BackedUp','fc1d109f-0acb-48c6-9b75-bad1c774771c','i-20-21-VM_ROOT-21_20110309105341',0,'MANUAL','2011-03-09 10:53:41',NULL,'338fa8d3-558c-48f9-95f2-b1bfd8d0c0cc',0,'XenServer'),(15,20,24,'Creating',NULL,'i-20-21-VM_ROOT-21_20110309110101',0,'MANUAL','2011-03-09 11:01:01',NULL,NULL,0,'XenServer'),(16,20,24,'Creating',NULL,'i-20-21-VM_ROOT-21_20110309110206',0,'MANUAL','2011-03-09 11:02:06',NULL,NULL,0,'XenServer'),(17,20,24,'Creating',NULL,'i-20-21-VM_ROOT-21_20110309110316',0,'MANUAL','2011-03-09 11:03:16',NULL,NULL,0,'XenServer'),(18,24,13,'Creating',NULL,'i-24-12-VM_ROOT-12_20110309110541',0,'MANUAL','2011-03-09 11:05:41',NULL,NULL,0,'XenServer'),(19,20,24,'Creating',NULL,'i-20-21-VM_ROOT-21_20110309111326',0,'MANUAL','2011-03-09 11:13:26',NULL,NULL,0,'XenServer'),(20,20,24,'Creating',NULL,'i-20-21-VM_ROOT-21_20110309111551',0,'MANUAL','2011-03-09 11:15:51',NULL,NULL,0,'XenServer'),(21,20,26,'Creating',NULL,'i-20-23-VM_ROOT-23_20110309112105',0,'MANUAL','2011-03-09 11:21:05',NULL,NULL,0,'XenServer'),(22,20,27,'Creating',NULL,'i-20-23-VM_DATA-23_20110309112135',0,'MANUAL','2011-03-09 11:21:35',NULL,NULL,0,'XenServer'),(23,20,24,'Creating',NULL,'i-20-21-VM_ROOT-21_20110309112306',0,'MANUAL','2011-03-09 11:23:06',NULL,NULL,0,'XenServer'),(24,24,13,'Creating',NULL,'i-24-12-VM_ROOT-12_20110309112354',0,'MANUAL','2011-03-09 11:23:54',NULL,NULL,0,'XenServer'),(25,24,13,'Creating',NULL,'i-24-12-VM_ROOT-12_20110309112558',0,'MANUAL','2011-03-09 11:25:58',NULL,NULL,0,'XenServer'),(26,20,24,'Creating',NULL,'i-20-21-VM_ROOT-21_20110309112755',0,'MANUAL','2011-03-09 11:27:55',NULL,NULL,0,'XenServer'),(27,20,24,'BackedUp','fab5ce61-f754-45ee-acc4-022f50f8e54a','i-20-21-VM_ROOT-21_20110309112848',0,'MANUAL','2011-03-09 11:28:48',NULL,'1c46650c-f71c-442a-a799-5a1932f163cc',14,'XenServer'),(28,24,13,'BackedUp','a7c3ad9f-1a7f-4d46-ba24-f41a9d43d9c5','i-24-12-VM_ROOT-12_20110309112958',0,'MANUAL','2011-03-09 11:29:58',NULL,'a5c31f40-a772-48a4-ba7e-030148e63c84',0,'XenServer'),(29,20,26,'BackedUp','4c8c88bb-c396-4380-8bc4-4e821da96e6a','i-20-23-VM_ROOT-23_20110309114045',0,'MANUAL','2011-03-09 11:40:45',NULL,'94e6c437-9273-41b3-b96a-67c557110370',0,'XenServer'),(30,24,14,'Creating',NULL,'i-24-12-VM_DATA-12_20110401120438',1,'RECURRING','2011-04-01 12:04:38',NULL,NULL,0,'XenServer'),(31,20,24,'Creating',NULL,'i-20-21-VM_ROOT-21_20110404170428',0,'MANUAL','2011-04-04 17:04:28',NULL,NULL,0,'XenServer'),(32,20,24,'Creating',NULL,'i-20-21-VM_ROOT-21_20110404171025',0,'MANUAL','2011-04-04 17:10:25',NULL,NULL,0,'XenServer'),(33,20,24,'BackedUp','edfc63fb-a6a1-4eb5-8057-b9ab6ae94c4d','i-20-21-VM_ROOT-21_20110404180030',0,'MANUAL','2011-04-04 18:00:30',NULL,'a4b4af5f-22bf-4bf0-9d3c-3a07d6b2630c',27,'XenServer'),(34,20,24,'Creating',NULL,'i-20-21-VM_ROOT-21_20110404190448',0,'MANUAL','2011-04-04 19:04:48',NULL,NULL,0,'XenServer'),(35,24,14,'Creating',NULL,'detached_DATA-12_20110501120042',1,'RECURRING','2011-05-01 12:00:42',NULL,NULL,0,'XenServer'),(36,2,51,'BackedUp','617224ea-1ef9-4fdf-b255-9162673af40c','i-2-42-VM_ROOT-42_20110511215128',0,'MANUAL','2011-05-11 21:51:28',NULL,'15501547-08c6-43a9-8ad5-3266f54ab066',0,'XenServer');
/*!40000 ALTER TABLE `snapshots` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ssh_keypairs`
--

DROP TABLE IF EXISTS `ssh_keypairs`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `ssh_keypairs` (
  `id` bigint(20) unsigned NOT NULL auto_increment COMMENT 'id',
  `account_id` bigint(20) unsigned NOT NULL COMMENT 'owner, foreign key to account table',
  `domain_id` bigint(20) unsigned NOT NULL COMMENT 'domain, foreign key to domain table',
  `keypair_name` varchar(256) NOT NULL COMMENT 'name of the key pair',
  `fingerprint` varchar(128) NOT NULL COMMENT 'fingerprint for the ssh public key',
  `public_key` varchar(5120) NOT NULL COMMENT 'public key of the ssh key pair',
  PRIMARY KEY  (`id`),
  KEY `fk_ssh_keypair__account_id` (`account_id`),
  KEY `fk_ssh_keypair__domain_id` (`domain_id`),
  CONSTRAINT `fk_ssh_keypairs__account_id` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_ssh_keypairs__domain_id` FOREIGN KEY (`domain_id`) REFERENCES `domain` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `ssh_keypairs`
--

LOCK TABLES `ssh_keypairs` WRITE;
/*!40000 ALTER TABLE `ssh_keypairs` DISABLE KEYS */;
/*!40000 ALTER TABLE `ssh_keypairs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `stack_maid`
--

DROP TABLE IF EXISTS `stack_maid`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `stack_maid` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `msid` bigint(20) unsigned NOT NULL,
  `thread_id` bigint(20) unsigned NOT NULL,
  `seq` int(10) unsigned NOT NULL,
  `cleanup_delegate` varchar(128) default NULL,
  `cleanup_context` text,
  `created` datetime default NULL,
  PRIMARY KEY  (`id`),
  KEY `i_stack_maid_msid_thread_id` (`msid`,`thread_id`),
  KEY `i_stack_maid_seq` (`msid`,`seq`),
  KEY `i_stack_maid_created` (`created`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `stack_maid`
--

LOCK TABLES `stack_maid` WRITE;
/*!40000 ALTER TABLE `stack_maid` DISABLE KEYS */;
/*!40000 ALTER TABLE `stack_maid` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `storage_pool`
--

DROP TABLE IF EXISTS `storage_pool`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `storage_pool` (
  `id` bigint(20) unsigned NOT NULL,
  `name` varchar(255) default NULL COMMENT 'should be NOT NULL',
  `uuid` varchar(255) default NULL,
  `pool_type` varchar(32) NOT NULL,
  `port` int(10) unsigned NOT NULL,
  `data_center_id` bigint(20) unsigned NOT NULL,
  `pod_id` bigint(20) unsigned default NULL,
  `cluster_id` bigint(20) unsigned default NULL COMMENT 'foreign key to cluster',
  `available_bytes` bigint(20) unsigned default NULL,
  `capacity_bytes` bigint(20) unsigned default NULL,
  `host_address` varchar(255) NOT NULL COMMENT 'FQDN or IP of storage server',
  `path` varchar(255) NOT NULL COMMENT 'Filesystem path that is shared',
  `created` datetime default NULL COMMENT 'date the pool created',
  `removed` datetime default NULL COMMENT 'date removed if not null',
  `update_time` datetime default NULL,
  `status` varchar(32) default NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`),
  KEY `i_storage_pool__pod_id` (`pod_id`),
  KEY `fk_storage_pool__cluster_id` (`cluster_id`),
  CONSTRAINT `fk_storage_pool__cluster_id` FOREIGN KEY (`cluster_id`) REFERENCES `cluster` (`id`),
  CONSTRAINT `fk_storage_pool__pod_id` FOREIGN KEY (`pod_id`) REFERENCES `host_pod_ref` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `storage_pool`
--

LOCK TABLES `storage_pool` WRITE;
/*!40000 ALTER TABLE `storage_pool` DISABLE KEYS */;
INSERT INTO `storage_pool` VALUES (200,'primary1','0410b43d-3097-3d72-8b90-dcff6417b31d','NetworkFilesystem',2049,1,1,1,395652890624,1925386469376,'nfs1.lab.vmops.com','/export/home/david/ezh-sit-primary1','2011-02-10 07:00:56',NULL,NULL,'ErrorInMaintenance'),(201,'primary1','6af18b66-0268-3c7b-9864-431a31fd4428','NetworkFilesystem',2049,2,2,2,1001890021376,1930519904256,'nfs2.lab.vmops.com','/export/profserv/ezh-sit/primary1','2011-05-06 22:06:03',NULL,NULL,'Up');
/*!40000 ALTER TABLE `storage_pool` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `storage_pool_details`
--

DROP TABLE IF EXISTS `storage_pool_details`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `storage_pool_details` (
  `id` bigint(20) unsigned NOT NULL auto_increment COMMENT 'id',
  `pool_id` bigint(20) unsigned NOT NULL COMMENT 'pool the detail is related to',
  `name` varchar(255) NOT NULL COMMENT 'name of the detail',
  `value` varchar(255) NOT NULL COMMENT 'value of the detail',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`),
  KEY `fk_storage_pool__pool_id` (`pool_id`),
  KEY `i_storage_pool_details__name__value` (`name`(128),`value`(128)),
  CONSTRAINT `fk_storage_pool_details__pool_id` FOREIGN KEY (`pool_id`) REFERENCES `storage_pool` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `storage_pool_details`
--

LOCK TABLES `storage_pool_details` WRITE;
/*!40000 ALTER TABLE `storage_pool_details` DISABLE KEYS */;
/*!40000 ALTER TABLE `storage_pool_details` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `storage_pool_host_ref`
--

DROP TABLE IF EXISTS `storage_pool_host_ref`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `storage_pool_host_ref` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `host_id` bigint(20) unsigned NOT NULL,
  `pool_id` bigint(20) unsigned NOT NULL,
  `created` datetime NOT NULL,
  `last_updated` datetime default NULL,
  `local_path` varchar(255) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `storage_pool_host_ref`
--

LOCK TABLES `storage_pool_host_ref` WRITE;
/*!40000 ALTER TABLE `storage_pool_host_ref` DISABLE KEYS */;
INSERT INTO `storage_pool_host_ref` VALUES (2,4,201,'2011-05-06 22:06:05',NULL,'/mnt/6af18b66-0268-3c7b-9864-431a31fd4428');
/*!40000 ALTER TABLE `storage_pool_host_ref` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `storage_pool_work`
--

DROP TABLE IF EXISTS `storage_pool_work`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `storage_pool_work` (
  `id` bigint(20) unsigned NOT NULL auto_increment COMMENT 'id',
  `pool_id` bigint(20) unsigned NOT NULL COMMENT 'storage pool associated with the vm',
  `vm_id` bigint(20) unsigned NOT NULL COMMENT 'vm identifier',
  `stopped_for_maintenance` tinyint(3) unsigned NOT NULL default '0' COMMENT 'this flag denoted whether the vm was stopped during maintenance',
  `started_after_maintenance` tinyint(3) unsigned NOT NULL default '0' COMMENT 'this flag denoted whether the vm was started after maintenance',
  `mgmt_server_id` bigint(20) unsigned NOT NULL COMMENT 'management server id',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`),
  UNIQUE KEY `pool_id` (`pool_id`,`vm_id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `storage_pool_work`
--

LOCK TABLES `storage_pool_work` WRITE;
/*!40000 ALTER TABLE `storage_pool_work` DISABLE KEYS */;
INSERT INTO `storage_pool_work` VALUES (1,200,2,1,0,6602001285584),(2,200,1,1,0,6602001285584),(3,200,6,1,0,6602001285584),(4,200,16,1,0,6602001285584),(5,200,35,1,0,6602001285584),(6,200,36,1,0,6602001285584),(7,201,40,1,1,6602001285584),(8,201,41,1,1,6602001285584);
/*!40000 ALTER TABLE `storage_pool_work` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sync_queue`
--

DROP TABLE IF EXISTS `sync_queue`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `sync_queue` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `sync_objtype` varchar(64) NOT NULL,
  `sync_objid` bigint(20) unsigned NOT NULL,
  `queue_proc_msid` bigint(20) default NULL,
  `queue_proc_number` bigint(20) default NULL COMMENT 'process number, increase 1 for each iteration',
  `queue_proc_time` datetime default NULL COMMENT 'last time to process the queue',
  `created` datetime default NULL COMMENT 'date created',
  `last_updated` datetime default NULL COMMENT 'date created',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `i_sync_queue__objtype__objid` (`sync_objtype`,`sync_objid`),
  KEY `i_sync_queue__created` (`created`),
  KEY `i_sync_queue__last_updated` (`last_updated`),
  KEY `i_sync_queue__queue_proc_time` (`queue_proc_time`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `sync_queue`
--

LOCK TABLES `sync_queue` WRITE;
/*!40000 ALTER TABLE `sync_queue` DISABLE KEYS */;
INSERT INTO `sync_queue` VALUES (1,'network',204,NULL,5,NULL,'2011-02-10 09:11:42','2011-02-10 09:24:18'),(2,'ipaddress',17,NULL,1,NULL,'2011-02-10 09:20:20','2011-02-10 09:20:22'),(3,'ipaddress',11,NULL,3,NULL,'2011-02-15 20:11:42','2011-02-15 20:49:56'),(4,'network',209,NULL,1,NULL,'2011-02-23 09:04:48','2011-02-23 09:04:53'),(5,'network',210,NULL,1,NULL,'2011-02-23 10:22:09','2011-02-23 10:22:14'),(6,'network',205,NULL,2,NULL,'2011-03-28 12:07:03','2011-03-28 12:07:31'),(7,'network',213,NULL,1,NULL,'2011-03-30 08:13:49','2011-03-30 08:13:50'),(8,'network',224,NULL,2,NULL,'2011-05-12 18:52:02','2011-05-12 18:52:42');
/*!40000 ALTER TABLE `sync_queue` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sync_queue_item`
--

DROP TABLE IF EXISTS `sync_queue_item`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `sync_queue_item` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `queue_id` bigint(20) unsigned NOT NULL,
  `content_type` varchar(64) default NULL,
  `content_id` bigint(20) default NULL,
  `queue_proc_msid` bigint(20) default NULL COMMENT 'owner msid when the queue item is being processed',
  `queue_proc_number` bigint(20) default NULL COMMENT 'used to distinguish raw items and items being in process',
  `created` datetime default NULL COMMENT 'time created',
  PRIMARY KEY  (`id`),
  KEY `i_sync_queue_item__queue_id` (`queue_id`),
  KEY `i_sync_queue_item__created` (`created`),
  CONSTRAINT `fk_sync_queue_item__queue_id` FOREIGN KEY (`queue_id`) REFERENCES `sync_queue` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `sync_queue_item`
--

LOCK TABLES `sync_queue_item` WRITE;
/*!40000 ALTER TABLE `sync_queue_item` DISABLE KEYS */;
/*!40000 ALTER TABLE `sync_queue_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `template_host_ref`
--

DROP TABLE IF EXISTS `template_host_ref`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `template_host_ref` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `host_id` bigint(20) unsigned NOT NULL,
  `pool_id` bigint(20) unsigned default NULL,
  `template_id` bigint(20) unsigned NOT NULL,
  `created` datetime NOT NULL,
  `last_updated` datetime default NULL,
  `job_id` varchar(255) default NULL,
  `download_pct` int(10) unsigned default NULL,
  `size` bigint(20) unsigned default NULL,
  `physical_size` bigint(20) unsigned default '0',
  `download_state` varchar(255) default NULL,
  `error_str` varchar(255) default NULL,
  `local_path` varchar(255) default NULL,
  `install_path` varchar(255) default NULL,
  `url` varchar(255) default NULL,
  `destroyed` tinyint(1) default NULL COMMENT 'indicates whether the template_host entry was destroyed by the user or not',
  `is_copy` tinyint(1) NOT NULL default '0' COMMENT 'indicates whether this was copied ',
  PRIMARY KEY  (`id`),
  KEY `i_template_host_ref__host_id` (`host_id`),
  KEY `i_template_host_ref__template_id` (`template_id`),
  CONSTRAINT `fk_template_host_ref__host_id` FOREIGN KEY (`host_id`) REFERENCES `host` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_template_host_ref__template_id` FOREIGN KEY (`template_id`) REFERENCES `vm_template` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=54 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `template_host_ref`
--

LOCK TABLES `template_host_ref` WRITE;
/*!40000 ALTER TABLE `template_host_ref` DISABLE KEYS */;
INSERT INTO `template_host_ref` VALUES (1,2,NULL,1,'2011-02-10 07:02:13','2011-03-10 23:54:04',NULL,100,2101252608,2101252608,'DOWNLOADED',NULL,NULL,'template/tmpl/1/1//adfac0e6-6c64-4fe4-9b36-e98226d8627a.vhd','http://download.cloud.com/releases/2.2.0/systemvm.vhd.bz2',0,0),(2,2,NULL,3,'2011-02-10 07:02:13','2011-03-10 23:54:04',NULL,100,718929920,718929920,'DOWNLOADED',NULL,NULL,'template/tmpl/1/3//a1a21f1a-fe4c-4b8d-b3ec-91fc0b8a28b5.qcow2','http://download.cloud.com/releases/2.2.0/systemvm.qcow2.bz2',0,0),(3,2,NULL,8,'2011-02-10 07:02:13','2011-03-10 23:54:04',NULL,100,314404864,314404864,'DOWNLOADED',NULL,NULL,'template/tmpl/1/8//63fd485d-41a3-4dd5-814e-344a46cecf72.ova','http://download.cloud.com/releases/2.2.0/systemvm.ova',0,0),(4,2,NULL,2,'2011-02-10 07:02:13','2011-03-10 23:54:04','216afe44-0502-444b-a7bc-7071c2bb0e39',100,8589934592,1708331520,'DOWNLOADED','Install completed successfully at 2/10/11 7:47 AM','/mnt/SecStorage/578ab03d/template/tmpl/1/2/dnld5798297788330421704tmp_','template/tmpl/1/2//8b3222ac-5610-3151-9c95-814a15367953.vhd','http://download.cloud.com/templates/builtin/f59f18fb-ae94-4f97-afd2-f84755767aca.vhd.bz2',1,0),(7,2,NULL,202,'2011-02-10 09:09:19','2011-03-10 23:54:04',NULL,100,8589934592,1712534016,'DOWNLOADED',NULL,NULL,'template/tmpl/2/202//713ee3ee-a271-43b3-bf90-b6f74e848e93.vhd',NULL,1,0),(8,2,NULL,203,'2011-02-10 09:12:44','2011-03-10 23:54:04',NULL,100,8589934592,1712534016,'DOWNLOADED',NULL,NULL,'template/tmpl/2/203//ffa8ae43-ff4e-4514-b6b2-c18161dfaaec.vhd',NULL,1,0),(9,2,NULL,204,'2011-02-10 09:15:34','2011-03-10 23:54:04',NULL,100,8589934592,1712534016,'DOWNLOADED',NULL,NULL,'template/tmpl/2/204//7b82d6c6-c612-4468-a6a2-f33c5e9ab3da.vhd',NULL,1,0),(10,2,NULL,205,'2011-02-10 09:18:22','2011-03-10 23:54:04',NULL,100,8589934592,1712534016,'DOWNLOADED',NULL,NULL,'template/tmpl/2/205//f4524131-3c07-483d-990a-855847d1c923.vhd',NULL,1,0),(11,2,NULL,206,'2011-02-10 09:21:35','2011-03-10 23:54:04',NULL,100,8589934592,1712534016,'DOWNLOADED',NULL,NULL,'template/tmpl/2/206//6d295900-70c6-454e-bf64-6b0de3629d73.vhd',NULL,1,0),(12,2,NULL,207,'2011-02-10 09:24:34','2011-03-10 23:54:04',NULL,100,8589934592,1712534016,'DOWNLOADED',NULL,NULL,'template/tmpl/2/207//aa919eac-76ae-4b5b-b37e-3cdb86ca8d3c.vhd',NULL,1,0),(13,2,NULL,208,'2011-02-10 09:27:29','2011-03-10 23:54:04',NULL,100,8589934592,1712534016,'DOWNLOADED',NULL,NULL,'template/tmpl/2/208//524f698a-c354-4e87-b361-cd24f48c92d0.vhd',NULL,1,0),(14,2,NULL,209,'2011-02-10 09:30:34','2011-03-10 23:54:04',NULL,100,8589934592,1712534016,'DOWNLOADED',NULL,NULL,'template/tmpl/2/209//d06a2fdf-5358-41e0-8222-180146d46acc.vhd',NULL,1,0),(15,2,NULL,210,'2011-02-10 09:33:31','2011-03-10 23:54:04',NULL,100,8589934592,1712534016,'DOWNLOADED',NULL,NULL,'template/tmpl/2/210//22643546-22dd-428a-ae33-d56d6cc21c54.vhd',NULL,1,0),(16,2,NULL,211,'2011-02-10 09:36:37','2011-03-10 23:54:04',NULL,100,8589934592,1712534016,'DOWNLOADED',NULL,NULL,'template/tmpl/2/211//bd4c1f37-9bbe-4cd7-bc29-599342813a40.vhd',NULL,1,0),(17,2,NULL,212,'2011-02-10 09:39:36','2011-03-10 23:54:04',NULL,100,8589934592,1712534016,'DOWNLOADED',NULL,NULL,'template/tmpl/2/212//1366c2ac-3739-4694-b4aa-0bd58998f2b8.vhd',NULL,1,0),(18,2,NULL,213,'2011-02-10 09:42:28','2011-03-10 23:54:04',NULL,100,8589934592,1712534016,'DOWNLOADED',NULL,NULL,'template/tmpl/2/213//7f015f07-5d31-48bf-83ad-c37c07d70f15.vhd',NULL,1,0),(19,2,NULL,214,'2011-02-10 09:45:34','2011-03-10 23:54:04',NULL,100,8589934592,1712534016,'DOWNLOADED',NULL,NULL,'template/tmpl/2/214//066e2c5a-e3c1-40e4-be52-b7291e36ad40.vhd',NULL,1,0),(20,2,NULL,215,'2011-02-10 09:48:23','2011-03-10 23:54:04',NULL,100,8589934592,1712534016,'DOWNLOADED',NULL,NULL,'template/tmpl/2/215//926a34df-42da-4907-aafa-2e513444b4ac.vhd',NULL,1,0),(21,2,NULL,216,'2011-02-10 09:51:43','2011-03-10 23:54:04',NULL,100,8589934592,1712534016,'DOWNLOADED',NULL,NULL,'template/tmpl/2/216//6dc09313-b91e-418c-86d1-728b3d8c9599.vhd',NULL,1,0),(22,2,NULL,217,'2011-02-10 09:54:41','2011-03-10 23:54:04',NULL,100,8589934592,1712534016,'DOWNLOADED',NULL,NULL,'template/tmpl/2/217//a52b641d-3c56-4fbf-ad2a-c8f2f25adbd7.vhd',NULL,1,0),(23,2,NULL,218,'2011-02-10 09:57:44','2011-03-10 23:54:04',NULL,100,8589934592,1712534016,'DOWNLOADED',NULL,NULL,'template/tmpl/2/218//1ddf46ef-9236-4a00-a654-5abd61afd42c.vhd',NULL,1,0),(24,2,NULL,219,'2011-02-10 10:00:38','2011-03-10 23:54:04',NULL,100,8589934592,1712534016,'DOWNLOADED',NULL,NULL,'template/tmpl/2/219//a93feeb2-ef2f-449c-89c3-4e08492d9bba.vhd',NULL,1,0),(25,2,NULL,220,'2011-02-10 10:03:22','2011-03-10 23:54:04',NULL,100,8589934592,1712534016,'DOWNLOADED',NULL,NULL,'template/tmpl/2/220//085a434d-fc47-4194-84a6-a95937fa7ab9.vhd',NULL,1,0),(26,2,NULL,221,'2011-02-10 10:06:22','2011-03-10 23:54:04',NULL,100,8589934592,1712534016,'DOWNLOADED',NULL,NULL,'template/tmpl/2/221//b9c82eff-2179-4ae4-b93a-8f01f50b1bfb.vhd',NULL,1,0),(27,2,NULL,222,'2011-03-01 21:30:43','2011-03-01 21:30:43','jobid0000',0,0,0,'DOWNLOAD_ERROR','Storage agent or storage VM disconnected',NULL,NULL,'http://nfs1.lab.vmops.com/isos_64bit/ubuntu-10.10-server-amd64.iso',0,0),(28,2,NULL,223,'2011-03-01 21:52:55','2011-03-10 23:54:04','2f7b26da-d9f6-4d0c-af8d-29b5a0cbba5f',100,27903,27903,'DOWNLOADED','Install completed successfully at 3/1/11 9:53 PM','/mnt/SecStorage/67350d35/template/tmpl/20/223/dnld2128853830882736688tmp_','template/tmpl/20/223//223-20-09d5124d-9362-3b3c-a340-b43a93ee7f34.iso','http://sourceforge.net/projects/openfiler/files/openfiler-distribution-iso-x86/openfiler-2.3-x86-disc1.iso',0,0),(29,2,NULL,224,'2011-03-01 22:35:10','2011-03-10 23:54:04','d7cc2324-686c-4cc0-814c-07b77405197e',100,27903,27903,'DOWNLOADED','Install completed successfully at 3/1/11 10:36 PM','/mnt/SecStorage/67350d35/template/tmpl/2/224/dnld7018806959453184126tmp_','template/tmpl/2/224//224-2-2a8aabf2-d3c9-38d5-91ee-ad933ec95bdc.iso','http://sourceforge.net/projects/openfiler/files/openfiler-distribution-iso-x86/openfiler-2.3-x86-disc1.iso',0,0),(32,5,NULL,1,'2011-05-06 22:06:45','2011-05-16 20:59:30',NULL,100,2101252608,2101252608,'DOWNLOADED',NULL,NULL,'template/tmpl/1/1//b8762363-14f8-47f1-b63f-7c2c0ae79a08.vhd','http://download.cloud.com/releases/2.2.0/systemvm.vhd.bz2',0,0),(33,5,NULL,3,'2011-05-06 22:06:45','2011-05-06 22:06:45',NULL,100,0,0,'DOWNLOADED',NULL,NULL,'template/tmpl/1/3/','http://download.cloud.com/releases/2.2.0/systemvm.qcow2.bz2',0,0),(34,5,NULL,8,'2011-05-06 22:06:45','2011-05-06 22:06:45',NULL,100,0,0,'DOWNLOADED',NULL,NULL,'template/tmpl/1/8/','http://download.cloud.com/releases/2.2.0/systemvm.ova',0,0),(35,5,NULL,2,'2011-05-06 22:06:45','2011-05-10 18:40:24','65a32d08-b343-4b72-941b-923136ea6002',100,8589934592,1708331520,'DOWNLOADED','Install completed successfully at 5/6/11 10:31 PM','/mnt/SecStorage/1bc2cdee/template/tmpl/1/2/dnld1338083016789668048tmp_','template/tmpl/1/2//152c9298-90dc-3e91-a800-c7e143a745cb.vhd','http://download.cloud.com/templates/builtin/f59f18fb-ae94-4f97-afd2-f84755767aca.vhd.bz2',1,0),(39,5,NULL,228,'2011-05-11 22:01:29','2011-05-16 20:59:30',NULL,100,8589934592,1708331520,'DOWNLOADED',NULL,NULL,'template/tmpl/2/228//87307f5e-24b8-4abc-8fb2-4c4afd71522b.vhd',NULL,0,0),(40,5,NULL,229,'2011-05-11 22:04:22','2011-05-16 20:59:30',NULL,100,8589934592,1708331520,'DOWNLOADED',NULL,NULL,'template/tmpl/2/229//e03153da-a1df-44d5-994d-a4e552a2ff10.vhd',NULL,0,0),(41,5,NULL,230,'2011-05-11 22:07:46','2011-05-16 20:59:30',NULL,100,8589934592,1708331520,'DOWNLOADED',NULL,NULL,'template/tmpl/2/230//1c41ac18-013c-4765-90ae-21ed0392b86e.vhd',NULL,0,0),(42,5,NULL,232,'2011-05-11 22:14:04','2011-05-16 20:59:30',NULL,100,8589934592,1708331520,'DOWNLOADED',NULL,NULL,'template/tmpl/2/232//a5a975d4-de25-4b25-a6e2-d25c195cb66a.vhd',NULL,0,0),(43,5,NULL,234,'2011-05-11 22:20:00','2011-05-16 20:59:30',NULL,100,8589934592,1708331520,'DOWNLOADED',NULL,NULL,'template/tmpl/2/234//becc4fd5-20d9-4809-9601-4017e17de53a.vhd',NULL,0,0),(44,5,NULL,236,'2011-05-11 22:24:11','2011-05-16 20:59:30',NULL,100,8589934592,1708331520,'DOWNLOADED',NULL,NULL,'template/tmpl/2/236//d3a53a14-14b9-4c14-8b8c-529c9320290a.vhd',NULL,0,0),(45,5,NULL,237,'2011-05-11 22:28:11','2011-05-16 20:59:30',NULL,100,8589934592,1708331520,'DOWNLOADED',NULL,NULL,'template/tmpl/2/237//300705ea-e933-46fb-93f9-860b040f2da9.vhd',NULL,0,0),(46,5,NULL,238,'2011-05-11 22:30:56','2011-05-16 20:59:30',NULL,100,8589934592,1708331520,'DOWNLOADED',NULL,NULL,'template/tmpl/2/238//288e2597-7d5f-4966-9b99-0ba22b59cb07.vhd',NULL,0,0),(47,5,NULL,239,'2011-05-11 22:33:48','2011-05-16 20:59:30',NULL,100,8589934592,1708331520,'DOWNLOADED',NULL,NULL,'template/tmpl/2/239//e0ca8ca9-2368-4195-beda-50e0d967309f.vhd',NULL,0,0),(48,5,NULL,240,'2011-05-11 22:37:15','2011-05-16 20:59:30',NULL,100,8589934592,1708331520,'DOWNLOADED',NULL,NULL,'template/tmpl/2/240//13d6e1dc-9c63-428e-adc6-af812ae032a5.vhd',NULL,0,0),(49,5,NULL,241,'2011-05-11 22:39:56','2011-05-16 20:59:30',NULL,100,8589934592,1708331520,'DOWNLOADED',NULL,NULL,'template/tmpl/2/241//31ca58bc-e96f-4c0d-bfd3-4546e84a86f2.vhd',NULL,0,0),(50,5,NULL,242,'2011-05-11 22:44:19','2011-05-16 20:59:30',NULL,100,8589934592,1708331520,'DOWNLOADED',NULL,NULL,'template/tmpl/2/242//63e903dd-8a41-4d7d-a5c8-eeaa69ebedba.vhd',NULL,0,0),(51,5,NULL,244,'2011-05-11 22:49:48','2011-05-16 20:59:30',NULL,100,8589934592,1708331520,'DOWNLOADED',NULL,NULL,'template/tmpl/2/244//9413f508-5699-4695-842d-ccecc49498f4.vhd',NULL,0,0),(52,5,NULL,245,'2011-05-11 22:53:25','2011-05-16 20:59:30',NULL,100,8589934592,1708331520,'DOWNLOADED',NULL,NULL,'template/tmpl/2/245//37e3ea3e-ae8a-4575-b59c-ae2c81705b67.vhd',NULL,0,0),(53,5,NULL,247,'2011-05-11 22:59:25','2011-05-16 20:59:30',NULL,100,8589934592,1708331520,'DOWNLOADED',NULL,NULL,'template/tmpl/2/247//2a1b6663-7d72-443e-ae4a-58138a33edf2.vhd',NULL,0,0);
/*!40000 ALTER TABLE `template_host_ref` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `template_spool_ref`
--

DROP TABLE IF EXISTS `template_spool_ref`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `template_spool_ref` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `pool_id` bigint(20) unsigned NOT NULL,
  `template_id` bigint(20) unsigned NOT NULL,
  `created` datetime NOT NULL,
  `last_updated` datetime default NULL,
  `job_id` varchar(255) default NULL,
  `download_pct` int(10) unsigned default NULL,
  `download_state` varchar(255) default NULL,
  `error_str` varchar(255) default NULL,
  `local_path` varchar(255) default NULL,
  `install_path` varchar(255) default NULL,
  `template_size` bigint(20) unsigned NOT NULL COMMENT 'the size of the template on the pool',
  `marked_for_gc` tinyint(1) unsigned NOT NULL default '0' COMMENT 'if true, the garbage collector will evict the template from this pool.',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `i_template_spool_ref__template_id__pool_id` (`template_id`,`pool_id`),
  KEY `fk_template_spool_ref__pool_id` (`pool_id`),
  CONSTRAINT `fk_template_spool_ref__pool_id` FOREIGN KEY (`pool_id`) REFERENCES `storage_pool` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_template_spool_ref__template_id` FOREIGN KEY (`template_id`) REFERENCES `vm_template` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `template_spool_ref`
--

LOCK TABLES `template_spool_ref` WRITE;
/*!40000 ALTER TABLE `template_spool_ref` DISABLE KEYS */;
INSERT INTO `template_spool_ref` VALUES (1,200,1,'2011-02-10 07:02:22',NULL,NULL,100,'DOWNLOADED',NULL,'d9a866fe-3d56-47a7-86a6-1d333358fb56','d9a866fe-3d56-47a7-86a6-1d333358fb56',2101252608,0),(6,200,202,'2011-02-17 14:53:44',NULL,NULL,100,'DOWNLOADED',NULL,'9656f22a-ac57-487e-9fae-5c853b7f4533','9656f22a-ac57-487e-9fae-5c853b7f4533',1712534016,0),(7,200,220,'2011-02-21 04:49:43',NULL,NULL,100,'DOWNLOADED',NULL,'3dc950f5-b41d-47a7-a19f-4ab011a8fc81','3dc950f5-b41d-47a7-a19f-4ab011a8fc81',1712534016,0),(8,201,1,'2011-05-06 22:06:58',NULL,NULL,100,'DOWNLOADED',NULL,'ec6a526d-6aeb-46f5-b2b8-8c33d2df5e9b','ec6a526d-6aeb-46f5-b2b8-8c33d2df5e9b',2101252608,0),(9,201,2,'2011-05-11 21:43:38',NULL,NULL,100,'DOWNLOADED',NULL,'f4b48d5d-6308-4665-b0ff-c6fc68d8b75f','f4b48d5d-6308-4665-b0ff-c6fc68d8b75f',1708331520,0),(10,201,228,'2011-05-12 00:20:32',NULL,NULL,100,'DOWNLOADED',NULL,'275642be-cb67-48a4-92f5-1a78b8fcc7fb','275642be-cb67-48a4-92f5-1a78b8fcc7fb',1708331520,0);
/*!40000 ALTER TABLE `template_spool_ref` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `template_zone_ref`
--

DROP TABLE IF EXISTS `template_zone_ref`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `template_zone_ref` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `zone_id` bigint(20) unsigned NOT NULL,
  `template_id` bigint(20) unsigned NOT NULL,
  `created` datetime NOT NULL,
  `last_updated` datetime default NULL,
  `removed` datetime default NULL COMMENT 'date removed if not null',
  PRIMARY KEY  (`id`),
  KEY `i_template_zone_ref__zone_id` (`zone_id`),
  KEY `i_template_zone_ref__template_id` (`template_id`),
  CONSTRAINT `fk_template_zone_ref__template_id` FOREIGN KEY (`template_id`) REFERENCES `vm_template` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_template_zone_ref__zone_id` FOREIGN KEY (`zone_id`) REFERENCES `data_center` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=56 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `template_zone_ref`
--

LOCK TABLES `template_zone_ref` WRITE;
/*!40000 ALTER TABLE `template_zone_ref` DISABLE KEYS */;
INSERT INTO `template_zone_ref` VALUES (1,1,1,'2011-02-10 07:02:13','2011-02-10 07:02:13',NULL),(2,1,2,'2011-02-10 07:02:13','2011-02-10 07:02:13','2011-05-11 21:46:51'),(3,1,3,'2011-02-10 07:02:13','2011-02-10 07:02:13',NULL),(4,1,4,'2011-02-10 07:02:13','2011-02-10 07:02:13','2011-05-11 21:47:00'),(5,1,7,'2011-02-10 07:02:13','2011-02-10 07:02:13','2011-05-11 21:48:30'),(6,1,8,'2011-02-10 07:02:13','2011-02-10 07:02:13',NULL),(7,1,202,'2011-02-10 09:09:19','2011-02-10 09:09:19','2011-05-11 21:39:46'),(8,1,203,'2011-02-10 09:12:44','2011-02-10 09:12:44','2011-05-11 21:39:51'),(9,1,204,'2011-02-10 09:15:34','2011-02-10 09:15:34','2011-05-11 21:38:47'),(10,1,205,'2011-02-10 09:18:22','2011-02-10 09:18:22','2011-05-11 21:39:10'),(11,1,206,'2011-02-10 09:21:35','2011-02-10 09:21:35','2011-05-11 21:39:31'),(12,1,207,'2011-02-10 09:24:34','2011-02-10 09:24:34','2011-05-11 21:39:40'),(13,1,208,'2011-02-10 09:27:29','2011-02-10 09:27:29','2011-05-11 21:40:55'),(14,1,209,'2011-02-10 09:30:34','2011-02-10 09:30:34','2011-05-11 21:41:00'),(15,1,210,'2011-02-10 09:33:31','2011-02-10 09:33:31','2011-05-11 21:41:11'),(16,1,211,'2011-02-10 09:36:37','2011-02-10 09:36:37','2011-05-11 21:41:06'),(17,1,212,'2011-02-10 09:39:36','2011-02-10 09:39:36','2011-05-11 21:40:34'),(18,1,213,'2011-02-10 09:42:28','2011-02-10 09:42:28','2011-05-11 21:40:45'),(19,1,214,'2011-02-10 09:45:34','2011-02-10 09:45:34','2011-05-11 21:40:51'),(20,1,215,'2011-02-10 09:48:23','2011-02-10 09:48:23','2011-05-11 21:40:40'),(21,1,216,'2011-02-10 09:51:43','2011-02-10 09:51:43','2011-05-11 21:40:18'),(22,1,217,'2011-02-10 09:54:41','2011-02-10 09:54:41','2011-05-11 21:40:13'),(23,1,218,'2011-02-10 09:57:44','2011-02-10 09:57:44','2011-05-11 21:40:29'),(24,1,219,'2011-02-10 10:00:38','2011-02-10 10:00:38','2011-05-11 21:40:25'),(25,1,220,'2011-02-10 10:03:22','2011-02-10 10:03:22','2011-05-11 21:40:04'),(26,1,221,'2011-02-10 10:06:22','2011-02-10 10:06:22','2011-05-11 21:39:57'),(27,1,222,'2011-03-01 21:30:43','2011-03-01 21:30:43',NULL),(28,1,223,'2011-03-01 21:52:55','2011-03-01 21:52:55',NULL),(29,1,224,'2011-03-01 22:35:10','2011-03-01 22:35:10',NULL),(30,1,225,'2011-03-02 04:22:17','2011-03-02 04:22:17','2011-03-02 04:22:38'),(31,1,226,'2011-03-30 08:15:56','2011-03-30 08:15:56','2011-05-11 21:48:01'),(32,2,1,'2011-05-06 22:06:46','2011-05-06 22:06:46',NULL),(33,2,2,'2011-05-06 22:06:46','2011-05-06 22:06:46','2011-05-11 21:46:51'),(34,2,3,'2011-05-06 22:06:46','2011-05-06 22:06:46',NULL),(35,2,4,'2011-05-06 22:06:46','2011-05-06 22:06:46','2011-05-11 21:47:00'),(36,2,7,'2011-05-06 22:06:46','2011-05-06 22:06:46','2011-05-11 21:48:30'),(37,2,8,'2011-05-06 22:06:46','2011-05-06 22:06:46',NULL),(38,2,204,'2011-05-11 21:05:10','2011-05-11 21:05:10',NULL),(39,2,205,'2011-05-11 21:05:23','2011-05-11 21:05:23',NULL),(40,2,227,'2011-05-11 21:38:30','2011-05-11 21:38:30','2011-05-11 21:47:55'),(41,2,228,'2011-05-11 22:01:29','2011-05-11 22:01:29',NULL),(42,2,229,'2011-05-11 22:04:22','2011-05-11 22:04:22',NULL),(43,2,230,'2011-05-11 22:07:46','2011-05-11 22:07:46',NULL),(44,2,232,'2011-05-11 22:14:04','2011-05-11 22:14:04',NULL),(45,2,234,'2011-05-11 22:20:00','2011-05-11 22:20:00',NULL),(46,2,236,'2011-05-11 22:24:11','2011-05-11 22:24:11',NULL),(47,2,237,'2011-05-11 22:28:11','2011-05-11 22:28:11',NULL),(48,2,238,'2011-05-11 22:30:56','2011-05-11 22:30:56',NULL),(49,2,239,'2011-05-11 22:33:48','2011-05-11 22:33:48',NULL),(50,2,240,'2011-05-11 22:37:15','2011-05-11 22:37:15',NULL),(51,2,241,'2011-05-11 22:39:56','2011-05-11 22:39:56',NULL),(52,2,242,'2011-05-11 22:44:19','2011-05-11 22:44:19',NULL),(53,2,244,'2011-05-11 22:49:48','2011-05-11 22:49:48',NULL),(54,2,245,'2011-05-11 22:53:24','2011-05-11 22:53:24',NULL),(55,2,247,'2011-05-11 22:59:25','2011-05-11 22:59:25',NULL);
/*!40000 ALTER TABLE `template_zone_ref` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `upload`
--

DROP TABLE IF EXISTS `upload`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `upload` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `host_id` bigint(20) unsigned NOT NULL,
  `type_id` bigint(20) unsigned NOT NULL,
  `type` varchar(255) default NULL,
  `mode` varchar(255) default NULL,
  `created` datetime NOT NULL,
  `last_updated` datetime default NULL,
  `job_id` varchar(255) default NULL,
  `upload_pct` int(10) unsigned default NULL,
  `upload_state` varchar(255) default NULL,
  `error_str` varchar(255) default NULL,
  `url` varchar(255) default NULL,
  `install_path` varchar(255) default NULL,
  PRIMARY KEY  (`id`),
  KEY `i_upload__host_id` (`host_id`),
  KEY `i_upload__type_id` (`type_id`),
  CONSTRAINT `fk_upload__host_id` FOREIGN KEY (`host_id`) REFERENCES `host` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `upload`
--

LOCK TABLES `upload` WRITE;
/*!40000 ALTER TABLE `upload` DISABLE KEYS */;
/*!40000 ALTER TABLE `upload` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usage_event`
--

DROP TABLE IF EXISTS `usage_event`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `usage_event` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `type` varchar(32) NOT NULL,
  `account_id` bigint(20) unsigned NOT NULL,
  `created` datetime NOT NULL,
  `zone_id` bigint(20) unsigned NOT NULL,
  `resource_id` bigint(20) unsigned default NULL,
  `resource_name` varchar(255) default NULL,
  `offering_id` bigint(20) unsigned default NULL,
  `template_id` bigint(20) unsigned default NULL,
  `size` bigint(20) unsigned default NULL,
  `processed` tinyint(4) NOT NULL default '0',
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=371 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `usage_event`
--

LOCK TABLES `usage_event` WRITE;
/*!40000 ALTER TABLE `usage_event` DISABLE KEYS */;
INSERT INTO `usage_event` VALUES (1,'TEMPLATE.CREATE',1,'2011-02-10 07:46:40',1,2,'CentOS 5.3(64-bit) no GUI (XenServer)',NULL,NULL,8589934592,0),(2,'NET.IPASSIGN',4,'2011-02-10 08:42:57',1,12,'66.149.231.112',NULL,NULL,1,0),(3,'VOLUME.CREATE',4,'2011-02-10 08:43:05',1,3,'ROOT-3',NULL,2,8589934592,0),(4,'VOLUME.CREATE',4,'2011-02-10 08:43:05',1,4,'DATA-3',13,NULL,5368709120,0),(5,'VM.CREATE',4,'2011-02-10 08:43:05',1,3,'i-4-3-VM',9,2,NULL,0),(6,'VM.START',4,'2011-02-10 08:44:45',1,3,'i-4-3-VM',9,2,NULL,0),(7,'NETWORK.OFFERING.CREATE',4,'2011-02-10 08:44:45',1,3,'i-4-3-VM',6,NULL,1,0),(8,'NET.IPASSIGN',2,'2011-02-10 08:46:25',1,11,'66.149.231.111',NULL,NULL,1,0),(9,'VOLUME.CREATE',2,'2011-02-10 08:46:42',1,6,'ROOT-5',NULL,2,8589934592,0),(10,'VM.CREATE',2,'2011-02-10 08:46:42',1,5,'i-2-5-VM',9,2,NULL,0),(11,'VM.START',2,'2011-02-10 08:47:32',1,5,'i-2-5-VM',9,2,NULL,0),(12,'NETWORK.OFFERING.CREATE',2,'2011-02-10 08:47:32',1,5,'i-2-5-VM',6,NULL,1,0),(13,'VOLUME.CREATE',4,'2011-02-10 08:47:36',1,8,'ROOT-7',NULL,2,8589934592,0),(14,'VM.CREATE',4,'2011-02-10 08:47:36',1,7,'i-4-7-VM',9,2,NULL,0),(15,'VM.START',4,'2011-02-10 08:47:48',1,7,'i-4-7-VM',9,2,NULL,0),(16,'NETWORK.OFFERING.CREATE',4,'2011-02-10 08:47:48',1,7,'i-4-7-VM',6,NULL,1,0),(17,'VM.STOP',2,'2011-02-10 08:48:45',1,5,'i-2-5-VM',9,2,NULL,0),(18,'NETWORK.OFFERING.DELETE',2,'2011-02-10 08:48:45',1,5,NULL,6,NULL,NULL,0),(19,'VOLUME.CREATE',4,'2011-02-10 08:48:50',1,9,'ROOT-8',NULL,2,8589934592,0),(20,'VM.CREATE',4,'2011-02-10 08:48:50',1,8,'i-4-8-VM',9,2,NULL,0),(21,'VM.START',4,'2011-02-10 08:49:01',1,8,'i-4-8-VM',9,2,NULL,0),(22,'NETWORK.OFFERING.CREATE',4,'2011-02-10 08:49:01',1,8,'i-4-8-VM',6,NULL,1,0),(23,'VM.STOP',4,'2011-02-10 08:53:08',1,7,'i-4-7-VM',9,2,NULL,0),(24,'NETWORK.OFFERING.DELETE',4,'2011-02-10 08:53:08',1,7,NULL,6,NULL,NULL,0),(25,'SNAPSHOT.CREATE',2,'2011-02-10 08:53:09',1,1,'i-2-5-VM_ROOT-5_20110210085044',NULL,NULL,8589934592,0),(26,'VOLUME.DELETE',2,'2011-02-10 09:08:38',1,6,'ROOT-5',NULL,NULL,NULL,0),(27,'VM.DESTROY',2,'2011-02-10 09:08:38',1,5,'i-2-5-VM',9,2,NULL,0),(28,'TEMPLATE.CREATE',2,'2011-02-10 09:09:19',1,202,'CentOS55 - Xen',NULL,NULL,8589934592,0),(29,'NET.IPASSIGN',4,'2011-02-10 09:11:50',1,17,'66.149.231.117',NULL,NULL,0,0),(30,'TEMPLATE.CREATE',2,'2011-02-10 09:12:44',1,203,'CentOS55LAMP - Xen',NULL,NULL,8589934592,0),(31,'TEMPLATE.CREATE',2,'2011-02-10 09:15:34',1,204,'CentOS55LAMP_BP - Xen',NULL,NULL,8589934592,0),(32,'TEMPLATE.CREATE',2,'2011-02-10 09:18:22',1,205,'CentOS55MySQL - Xen',NULL,NULL,8589934592,0),(33,'NET.RULEADD',4,'2011-02-10 09:20:20',0,1,NULL,NULL,NULL,NULL,0),(34,'TEMPLATE.CREATE',2,'2011-02-10 09:21:35',1,206,'CentOS55MySQL_BP - Xen',NULL,NULL,8589934592,0),(35,'LB.CREATE',4,'2011-02-10 09:23:42',1,2,NULL,NULL,NULL,NULL,0),(36,'TEMPLATE.CREATE',2,'2011-02-10 09:24:34',1,207,'RHEL55 - Xen',NULL,NULL,8589934592,0),(37,'TEMPLATE.CREATE',2,'2011-02-10 09:27:29',1,208,'RHEL55LAMP_BP - Xen',NULL,NULL,8589934592,0),(38,'TEMPLATE.CREATE',2,'2011-02-10 09:30:34',1,209,'Ubuntu1010 - Xen',NULL,NULL,8589934592,0),(39,'TEMPLATE.CREATE',2,'2011-02-10 09:33:31',1,210,'Windows2003SP2_32bit - Xen',NULL,NULL,8589934592,0),(40,'TEMPLATE.CREATE',2,'2011-02-10 09:36:37',1,211,'Windows2008R2 - Xen',NULL,NULL,8589934592,0),(41,'TEMPLATE.CREATE',2,'2011-02-10 09:39:36',1,212,'Windows2008R2_32bit - Xen',NULL,NULL,8589934592,0),(42,'TEMPLATE.CREATE',2,'2011-02-10 09:42:28',1,213,'Windows2008R2_BP - Xen',NULL,NULL,8589934592,0),(43,'TEMPLATE.CREATE',2,'2011-02-10 09:45:34',1,214,'Windows2008R2_IIS - Xen',NULL,NULL,8589934592,0),(44,'TEMPLATE.CREATE',2,'2011-02-10 09:48:23',1,215,'Windows2008R2_IIS_BP - Xen',NULL,NULL,8589934592,0),(45,'TEMPLATE.CREATE',2,'2011-02-10 09:51:43',1,216,'Windows2008R2_SQL2008 - Xen',NULL,NULL,8589934592,0),(46,'TEMPLATE.CREATE',2,'2011-02-10 09:54:41',1,217,'Windows2008R2_SQL2008_BP - Xen',NULL,NULL,8589934592,0),(47,'TEMPLATE.CREATE',2,'2011-02-10 09:57:44',1,218,'Windows2008R2_WISA - Xen',NULL,NULL,8589934592,0),(48,'TEMPLATE.CREATE',2,'2011-02-10 10:00:38',1,219,'Windows2008R2_WISA_BP - Xen',NULL,NULL,8589934592,0),(49,'TEMPLATE.CREATE',2,'2011-02-10 10:03:22',1,220,'Windows7SP1 - Xen',NULL,NULL,8589934592,0),(50,'TEMPLATE.CREATE',2,'2011-02-10 10:06:22',1,221,'WindowsXPSP3 - Xen',NULL,NULL,8589934592,0),(51,'NET.IPASSIGN',6,'2011-02-10 10:49:41',1,3,'66.149.231.103',NULL,NULL,1,0),(52,'VOLUME.CREATE',6,'2011-02-10 10:49:48',1,10,'ROOT-9',NULL,202,8589934592,0),(53,'VM.CREATE',6,'2011-02-10 10:49:48',1,9,'i-6-9-VM',9,202,NULL,0),(54,'VM.START',6,'2011-02-10 10:52:26',1,9,'i-6-9-VM',9,202,NULL,0),(55,'NETWORK.OFFERING.CREATE',6,'2011-02-10 10:52:26',1,9,'i-6-9-VM',6,NULL,1,0),(56,'VM.STOP',6,'2011-02-10 10:55:01',1,9,'i-6-9-VM',9,202,NULL,0),(57,'NETWORK.OFFERING.DELETE',6,'2011-02-10 10:55:01',1,9,NULL,6,NULL,NULL,0),(58,'VOLUME.DELETE',6,'2011-02-10 10:55:01',1,10,'ROOT-9',NULL,NULL,NULL,0),(59,'VM.DESTROY',6,'2011-02-10 10:55:02',1,9,'i-6-9-VM',9,202,NULL,0),(60,'VM.STOP',4,'2011-02-10 12:07:58',1,3,'i-4-3-VM',9,2,NULL,0),(61,'NETWORK.OFFERING.DELETE',4,'2011-02-10 12:07:58',1,3,NULL,6,NULL,NULL,0),(62,'VM.START',4,'2011-02-10 13:26:47',1,7,'i-4-7-VM',9,2,NULL,0),(63,'NETWORK.OFFERING.CREATE',4,'2011-02-10 13:26:47',1,7,'i-4-7-VM',6,NULL,1,0),(64,'VM.START',4,'2011-02-10 13:27:36',1,3,'i-4-3-VM',9,2,NULL,0),(65,'NETWORK.OFFERING.CREATE',4,'2011-02-10 13:27:36',1,3,'i-4-3-VM',6,NULL,1,0),(66,'VOLUME.DELETE',2,'2011-02-11 12:47:39',1,6,'ROOT-5',NULL,NULL,NULL,0),(67,'VOLUME.CREATE',2,'2011-02-15 20:33:45',1,12,'ROOT-11',NULL,202,8589934592,0),(68,'VM.CREATE',2,'2011-02-15 20:33:45',1,11,'i-2-11-VM',9,202,NULL,0),(69,'VM.START',2,'2011-02-15 20:35:26',1,11,'i-2-11-VM',9,202,NULL,0),(70,'NETWORK.OFFERING.CREATE',2,'2011-02-15 20:35:26',1,11,'i-2-11-VM',6,NULL,1,0),(71,'VM.STOP',2,'2011-02-15 20:50:44',1,11,'i-2-11-VM',9,202,NULL,0),(72,'NETWORK.OFFERING.DELETE',2,'2011-02-15 20:50:44',1,11,NULL,6,NULL,NULL,0),(73,'VOLUME.DELETE',2,'2011-02-15 20:50:44',1,12,'ROOT-11',NULL,NULL,NULL,0),(74,'VM.DESTROY',2,'2011-02-15 20:50:44',1,11,'i-2-11-VM',9,202,NULL,0),(76,'VOLUME.DELETE',2,'2011-02-17 00:19:52',1,12,'ROOT-11',NULL,NULL,NULL,0),(78,'NET.IPASSIGN',24,'2011-02-17 14:49:43',1,6,'66.149.231.106',NULL,NULL,1,0),(79,'VOLUME.CREATE',24,'2011-02-17 14:49:57',1,13,'ROOT-12',NULL,207,8589934592,0),(80,'VOLUME.CREATE',24,'2011-02-17 14:49:57',1,14,'DATA-12',26,NULL,5368709120,0),(81,'VM.CREATE',24,'2011-02-17 14:49:57',1,12,'i-24-12-VM',25,207,NULL,0),(82,'VM.START',24,'2011-02-17 14:52:28',1,12,'i-24-12-VM',25,207,NULL,0),(83,'NETWORK.OFFERING.CREATE',24,'2011-02-17 14:52:28',1,12,'i-24-12-VM',6,NULL,1,0),(84,'NET.IPASSIGN',22,'2011-02-17 14:53:20',1,7,'66.149.231.107',NULL,NULL,1,0),(85,'VOLUME.CREATE',22,'2011-02-17 14:53:44',1,16,'ROOT-14',NULL,202,8589934592,0),(86,'VOLUME.CREATE',22,'2011-02-17 14:53:44',1,17,'DATA-14',26,NULL,5368709120,0),(87,'VM.CREATE',22,'2011-02-17 14:53:44',1,14,'i-22-14-VM',25,202,NULL,0),(88,'VM.START',22,'2011-02-17 14:56:01',1,14,'i-22-14-VM',25,202,NULL,0),(89,'NETWORK.OFFERING.CREATE',22,'2011-02-17 14:56:01',1,14,'i-22-14-VM',6,NULL,1,0),(94,'VOLUME.CREATE',2,'2011-02-21 04:49:43',1,19,'ROOT-16',NULL,220,8589934592,0),(95,'VM.CREATE',2,'2011-02-21 04:49:43',1,16,'i-2-16-VM',25,220,NULL,0),(96,'VM.START',2,'2011-02-21 04:51:47',1,16,'i-2-16-VM',25,220,NULL,0),(97,'NETWORK.OFFERING.CREATE',2,'2011-02-21 04:51:47',1,16,'i-2-16-VM',6,NULL,1,0),(100,'NET.IPASSIGN',26,'2011-02-23 09:00:50',1,4,'66.149.231.104',NULL,NULL,1,0),(101,'VOLUME.CREATE',26,'2011-02-23 09:01:05',1,20,'ROOT-17',NULL,202,8589934592,0),(102,'VM.CREATE',26,'2011-02-23 09:01:05',1,17,'i-26-17-VM',9,202,NULL,0),(103,'VM.START',26,'2011-02-23 09:02:13',1,17,'i-26-17-VM',9,202,NULL,0),(104,'NETWORK.OFFERING.CREATE',26,'2011-02-23 09:02:14',1,17,'i-26-17-VM',6,NULL,1,0),(105,'NET.IPASSIGN',26,'2011-02-23 09:04:53',1,5,'66.149.231.105',NULL,NULL,0,0),(106,'VM.STOP',26,'2011-02-23 09:20:16',1,17,'i-26-17-VM',9,202,NULL,0),(107,'NETWORK.OFFERING.DELETE',26,'2011-02-23 09:20:16',1,17,NULL,6,NULL,NULL,0),(108,'VOLUME.DELETE',26,'2011-02-23 09:40:37',1,20,'ROOT-17',NULL,NULL,NULL,0),(109,'VM.DESTROY',26,'2011-02-23 09:40:37',1,17,'i-26-17-VM',9,202,NULL,0),(110,'NET.IPRELEASE',26,'2011-02-23 09:40:37',1,4,'66.149.231.104',NULL,NULL,1,0),(111,'NET.IPRELEASE',26,'2011-02-23 09:40:37',1,5,'66.149.231.105',NULL,NULL,0,0),(112,'NET.IPASSIGN',30,'2011-02-23 10:19:49',1,19,'66.149.231.119',NULL,NULL,1,0),(113,'VOLUME.CREATE',30,'2011-02-23 10:20:15',1,22,'ROOT-19',NULL,202,8589934592,0),(114,'VM.CREATE',30,'2011-02-23 10:20:15',1,19,'i-30-19-VM',9,202,NULL,0),(115,'VM.START',30,'2011-02-23 10:21:05',1,19,'i-30-19-VM',9,202,NULL,0),(116,'NETWORK.OFFERING.CREATE',30,'2011-02-23 10:21:05',1,19,'i-30-19-VM',6,NULL,1,0),(117,'NET.IPASSIGN',30,'2011-02-23 10:22:14',1,20,'66.149.231.120',NULL,NULL,0,0),(118,'VM.STOP',30,'2011-02-23 10:35:14',1,19,'i-30-19-VM',9,202,NULL,0),(119,'NETWORK.OFFERING.DELETE',30,'2011-02-23 10:35:14',1,19,NULL,6,NULL,NULL,0),(120,'VOLUME.DELETE',30,'2011-02-23 10:38:28',1,22,'ROOT-19',NULL,NULL,NULL,0),(121,'VM.DESTROY',30,'2011-02-23 10:38:28',1,19,'i-30-19-VM',9,202,NULL,0),(122,'NET.IPRELEASE',30,'2011-02-23 10:38:29',1,19,'66.149.231.119',NULL,NULL,1,0),(123,'NET.IPRELEASE',30,'2011-02-23 10:38:29',1,20,'66.149.231.120',NULL,NULL,0,0),(130,'ISO.CREATE',20,'2011-03-01 21:53:08',1,223,'BD1-Open Filer',NULL,NULL,27903,0),(131,'ISO.CREATE',2,'2011-03-01 22:35:13',1,224,'BD1-Bee Dee Test',NULL,NULL,27903,0),(132,'ISO.DELETE',2,'2011-03-02 04:22:38',1,225,NULL,NULL,NULL,NULL,0),(139,'SNAPSHOT.CREATE',2,'2011-03-08 12:38:32',1,2,'i-2-16-VM_ROOT-16_20110308123547',NULL,NULL,8589934592,0),(140,'SNAPSHOT.CREATE',4,'2011-03-08 12:43:51',1,4,'i-4-3-VM_DATA-3_20110308124255',NULL,NULL,5368709120,0),(141,'SNAPSHOT.CREATE',24,'2011-03-08 13:31:00',1,9,'i-24-12-VM_DATA-12_20110308133006',NULL,NULL,5368709120,0),(143,'NET.IPASSIGN',20,'2011-03-09 10:51:23',1,16,'66.149.231.116',NULL,NULL,1,0),(144,'VOLUME.CREATE',20,'2011-03-09 10:51:45',1,24,'ROOT-21',NULL,220,8589934592,0),(145,'VM.CREATE',20,'2011-03-09 10:51:45',1,21,'i-20-21-VM',25,220,NULL,0),(146,'VM.START',20,'2011-03-09 10:52:52',1,21,'i-20-21-VM',25,220,NULL,0),(147,'NETWORK.OFFERING.CREATE',20,'2011-03-09 10:52:52',1,21,'i-20-21-VM',6,NULL,1,0),(148,'SNAPSHOT.CREATE',20,'2011-03-09 10:56:11',1,14,'i-20-21-VM_ROOT-21_20110309105341',NULL,NULL,8589934592,0),(149,'SNAPSHOT.DELETE',2,'2011-03-09 11:04:59',0,1,'i-2-5-VM_ROOT-5_20110210085044',NULL,NULL,0,0),(150,'SNAPSHOT.DELETE',2,'2011-03-09 11:10:52',0,2,'i-2-16-VM_ROOT-16_20110308123547',NULL,NULL,0,0),(151,'VOLUME.CREATE',20,'2011-03-09 11:17:52',1,26,'ROOT-23',NULL,221,8589934592,0),(152,'VOLUME.CREATE',20,'2011-03-09 11:17:52',1,27,'DATA-23',26,NULL,5368709120,0),(153,'VM.CREATE',20,'2011-03-09 11:17:52',1,23,'i-20-23-VM',25,221,NULL,0),(154,'VM.START',20,'2011-03-09 11:19:28',1,23,'i-20-23-VM',25,221,NULL,0),(155,'NETWORK.OFFERING.CREATE',20,'2011-03-09 11:19:29',1,23,'i-20-23-VM',6,NULL,1,0),(156,'VOLUME.CREATE',20,'2011-03-09 11:20:02',1,28,'ROOT-24',NULL,221,8589934592,0),(157,'VM.CREATE',20,'2011-03-09 11:20:02',1,24,'i-20-24-VM',25,221,NULL,0),(158,'SNAPSHOT.DELETE',4,'2011-03-09 11:22:22',0,4,'i-4-3-VM_DATA-3_20110308124255',NULL,NULL,0,0),(159,'SNAPSHOT.CREATE',20,'2011-03-09 11:29:00',1,27,'i-20-21-VM_ROOT-21_20110309112848',NULL,NULL,8589934592,0),(160,'SNAPSHOT.CREATE',24,'2011-03-09 11:32:55',1,28,'i-24-12-VM_ROOT-12_20110309112958',NULL,NULL,8589934592,0),(161,'SNAPSHOT.CREATE',20,'2011-03-09 11:42:18',1,29,'i-20-23-VM_ROOT-23_20110309114045',NULL,NULL,8589934592,0),(163,'NET.IPASSIGN',3,'2011-03-10 00:27:14',1,18,'66.149.231.118',NULL,NULL,1,0),(164,'VOLUME.CREATE',3,'2011-03-10 00:28:27',1,29,'ROOT-25',NULL,220,8589934592,0),(165,'VM.CREATE',3,'2011-03-10 00:28:27',1,25,'i-3-25-VM',12,220,NULL,0),(180,'VOLUME.CREATE',2,'2011-03-24 01:41:04',1,30,'ROOT-26',NULL,220,8589934592,0),(181,'VM.CREATE',2,'2011-03-24 01:41:04',1,26,'i-2-26-VM',25,220,NULL,0),(182,'VOLUME.CREATE',2,'2011-03-24 01:42:26',1,31,'ROOT-27',NULL,220,8589934592,0),(183,'VM.CREATE',2,'2011-03-24 01:42:26',1,27,'i-2-27-VM',25,220,NULL,0),(188,'VOLUME.CREATE',2,'2011-03-28 12:03:37',1,32,'hgjg',18,NULL,5368709120,0),(189,'NET.IPASSIGN',2,'2011-03-28 12:07:08',1,10,'66.149.231.110',NULL,NULL,0,0),(190,'NET.RULEADD',2,'2011-03-28 12:07:30',0,3,NULL,NULL,NULL,NULL,0),(191,'LB.CREATE',2,'2011-03-28 12:08:00',1,4,NULL,NULL,NULL,NULL,0),(193,'NET.IPASSIGN',42,'2011-03-29 07:55:22',1,13,'66.149.231.113',NULL,NULL,1,0),(194,'VOLUME.CREATE',42,'2011-03-29 07:55:28',1,33,'ROOT-28',NULL,220,8589934592,0),(195,'VM.CREATE',42,'2011-03-29 07:55:28',1,28,'i-42-28-VM',9,220,NULL,0),(196,'VOLUME.DELETE',4,'2011-03-29 08:01:12',1,3,'ROOT-3',NULL,NULL,NULL,0),(197,'VM.DESTROY',4,'2011-03-29 08:01:12',1,3,'i-4-3-VM',9,2,NULL,0),(198,'VOLUME.DELETE',4,'2011-03-29 08:01:24',1,8,'ROOT-7',NULL,NULL,NULL,0),(199,'VM.DESTROY',4,'2011-03-29 08:01:24',1,7,'i-4-7-VM',9,2,NULL,0),(200,'VOLUME.DELETE',4,'2011-03-29 08:01:37',1,9,'ROOT-8',NULL,NULL,NULL,0),(201,'VM.DESTROY',4,'2011-03-29 08:01:37',1,8,'i-4-8-VM',9,2,NULL,0),(202,'VOLUME.DELETE',42,'2011-03-29 08:02:22',1,33,'ROOT-28',NULL,NULL,NULL,0),(203,'VM.DESTROY',42,'2011-03-29 08:02:22',1,28,'i-42-28-VM',9,220,NULL,0),(204,'VOLUME.CREATE',42,'2011-03-29 08:19:04',1,34,'ROOT-29',NULL,202,8589934592,0),(205,'VM.CREATE',42,'2011-03-29 08:19:04',1,29,'i-42-29-VM',9,202,NULL,0),(206,'NET.IPASSIGN',51,'2011-03-29 12:19:09',1,9,'66.149.231.109',NULL,NULL,1,0),(207,'VOLUME.CREATE',51,'2011-03-29 12:19:17',1,35,'ROOT-30',NULL,202,8589934592,0),(208,'VM.CREATE',51,'2011-03-29 12:19:17',1,30,'i-51-30-VM',12,202,NULL,0),(210,'VOLUME.CREATE',51,'2011-03-30 06:12:02',1,36,'ROOT-31',NULL,220,8589934592,0),(211,'VM.CREATE',51,'2011-03-30 06:12:02',1,31,'i-51-31-VM',9,220,NULL,0),(212,'NET.IPASSIGN',42,'2011-03-30 08:13:50',1,2,'66.149.231.102',NULL,NULL,0,0),(214,'VOLUME.DELETE',4,'2011-03-30 23:52:01',1,3,'ROOT-3',NULL,NULL,NULL,0),(215,'NET.RULEDELETE',4,'2011-03-30 23:52:02',0,1,NULL,NULL,NULL,NULL,0),(216,'VOLUME.DELETE',4,'2011-03-30 23:52:03',1,8,'ROOT-7',NULL,NULL,NULL,0),(217,'VOLUME.DELETE',4,'2011-03-30 23:52:05',1,9,'ROOT-8',NULL,NULL,NULL,0),(222,'VOLUME.CREATE',2,'2011-04-04 16:28:22',1,37,'ROOT-32',NULL,220,8589934592,0),(223,'VM.CREATE',2,'2011-04-04 16:28:22',1,32,'i-2-32-VM',25,220,NULL,0),(224,'VOLUME.DELETE',2,'2011-04-04 16:29:21',1,37,'ROOT-32',NULL,NULL,NULL,0),(225,'VM.DESTROY',2,'2011-04-04 16:29:21',1,32,'i-2-32-VM',25,220,NULL,0),(226,'SNAPSHOT.CREATE',20,'2011-04-04 18:01:09',1,33,'i-20-21-VM_ROOT-21_20110404180030',NULL,NULL,8589934592,0),(227,'VOLUME.CREATE',20,'2011-04-04 20:09:43',1,38,'ROOT-33',NULL,220,8589934592,0),(228,'VM.CREATE',20,'2011-04-04 20:09:43',1,33,'i-20-33-VM',25,220,NULL,0),(229,'VOLUME.CREATE',20,'2011-04-04 20:11:27',1,39,'ROOT-34',NULL,220,8589934592,0),(230,'VM.CREATE',20,'2011-04-04 20:11:27',1,34,'i-20-34-VM',25,220,NULL,0),(231,'VOLUME.DELETE',20,'2011-04-04 20:12:56',1,38,'ROOT-33',NULL,NULL,NULL,0),(232,'VM.DESTROY',20,'2011-04-04 20:12:56',1,33,'i-20-33-VM',25,220,NULL,0),(233,'VOLUME.DELETE',20,'2011-04-04 20:13:00',1,39,'ROOT-34',NULL,NULL,NULL,0),(234,'VM.DESTROY',20,'2011-04-04 20:13:00',1,34,'i-20-34-VM',25,220,NULL,0),(240,'VM.STOP',20,'2011-04-09 10:08:32',1,23,'i-20-23-VM',25,221,NULL,0),(241,'NETWORK.OFFERING.DELETE',20,'2011-04-09 10:08:32',1,23,NULL,6,NULL,NULL,0),(242,'VOLUME.DELETE',20,'2011-04-09 10:09:08',1,26,'ROOT-23',NULL,NULL,NULL,0),(243,'VM.DESTROY',20,'2011-04-09 10:09:08',1,23,'i-20-23-VM',25,221,NULL,0),(244,'VM.STOP',22,'2011-04-09 10:10:57',1,14,'i-22-14-VM',25,202,NULL,0),(245,'NETWORK.OFFERING.DELETE',22,'2011-04-09 10:10:57',1,14,NULL,6,NULL,NULL,0),(246,'VOLUME.DELETE',22,'2011-04-09 10:11:13',1,16,'ROOT-14',NULL,NULL,NULL,0),(247,'VM.DESTROY',22,'2011-04-09 10:11:13',1,14,'i-22-14-VM',25,202,NULL,0),(248,'VM.STOP',24,'2011-04-09 10:12:33',1,12,'i-24-12-VM',25,207,NULL,0),(249,'NETWORK.OFFERING.DELETE',24,'2011-04-09 10:12:33',1,12,NULL,6,NULL,NULL,0),(250,'VOLUME.DELETE',24,'2011-04-09 10:12:46',1,13,'ROOT-12',NULL,NULL,NULL,0),(251,'VM.DESTROY',24,'2011-04-09 10:12:46',1,12,'i-24-12-VM',25,207,NULL,0),(252,'VM.STOP',20,'2011-04-09 10:14:44',1,21,'i-20-21-VM',25,220,NULL,0),(253,'NETWORK.OFFERING.DELETE',20,'2011-04-09 10:14:44',1,21,NULL,6,NULL,NULL,0),(254,'VOLUME.DELETE',20,'2011-04-09 10:14:58',1,24,'ROOT-21',NULL,NULL,NULL,0),(255,'VM.DESTROY',20,'2011-04-09 10:14:58',1,21,'i-20-21-VM',25,220,NULL,0),(256,'NET.IPASSIGN',56,'2011-04-09 10:25:42',1,1,'66.149.231.101',NULL,NULL,1,0),(257,'VOLUME.CREATE',56,'2011-04-09 10:26:38',1,40,'ROOT-35',NULL,202,8589934592,0),(258,'VOLUME.CREATE',56,'2011-04-09 10:26:38',1,41,'DATA-35',13,NULL,5368709120,0),(259,'VM.CREATE',56,'2011-04-09 10:26:38',1,35,'i-56-35-VM',9,202,NULL,0),(260,'VM.START',56,'2011-04-09 10:27:56',1,35,'i-56-35-VM',9,202,NULL,0),(261,'NETWORK.OFFERING.CREATE',56,'2011-04-09 10:27:56',1,35,'i-56-35-VM',6,NULL,1,0),(264,'VOLUME.DELETE',24,'2011-04-10 23:51:03',1,13,'ROOT-12',NULL,NULL,NULL,0),(265,'VOLUME.DELETE',22,'2011-04-10 23:51:03',1,16,'ROOT-14',NULL,NULL,NULL,0),(266,'VOLUME.DELETE',20,'2011-04-10 23:51:04',1,24,'ROOT-21',NULL,NULL,NULL,0),(267,'VOLUME.DELETE',20,'2011-04-10 23:51:05',1,26,'ROOT-23',NULL,NULL,NULL,0),(274,'NET.IPASSIGN',61,'2011-04-25 12:53:14',1,4,'66.149.231.104',NULL,NULL,1,0),(275,'VOLUME.CREATE',61,'2011-04-25 12:53:30',1,43,'ROOT-37',NULL,221,8589934592,0),(276,'VOLUME.CREATE',61,'2011-04-25 12:53:30',1,44,'DATA-37',13,NULL,5368709120,0),(277,'VM.CREATE',61,'2011-04-25 12:53:30',1,37,'i-61-37-VM',9,221,NULL,0),(278,'VOLUME.CREATE',61,'2011-04-25 12:54:45',1,45,'ROOT-38',NULL,202,8589934592,0),(279,'VOLUME.CREATE',61,'2011-04-25 12:54:45',1,46,'DATA-38',13,NULL,5368709120,0),(280,'VM.CREATE',61,'2011-04-25 12:54:45',1,38,'i-61-38-VM',9,202,NULL,0),(281,'NET.IPASSIGN',60,'2011-04-25 12:56:50',1,8,'66.149.231.108',NULL,NULL,1,0),(282,'VOLUME.CREATE',60,'2011-04-25 12:57:07',1,47,'ROOT-39',NULL,202,8589934592,0),(283,'VOLUME.CREATE',60,'2011-04-25 12:57:07',1,48,'DATA-39',13,NULL,5368709120,0),(284,'VM.CREATE',60,'2011-04-25 12:57:07',1,39,'i-60-39-VM',9,202,NULL,0),(291,'TEMPLATE.CREATE',1,'2011-05-06 22:31:55',2,2,'CentOS 5.3(64-bit) no GUI (XenServer)',NULL,NULL,8589934592,0),(297,'TEMPLATE.DELETE',2,'2011-05-11 21:38:47',1,204,NULL,NULL,NULL,NULL,0),(298,'TEMPLATE.DELETE',2,'2011-05-11 21:39:10',1,205,NULL,NULL,NULL,NULL,0),(299,'TEMPLATE.DELETE',2,'2011-05-11 21:39:31',1,206,NULL,NULL,NULL,NULL,0),(300,'TEMPLATE.DELETE',2,'2011-05-11 21:39:40',1,207,NULL,NULL,NULL,NULL,0),(301,'TEMPLATE.DELETE',2,'2011-05-11 21:39:46',1,202,NULL,NULL,NULL,NULL,0),(302,'TEMPLATE.DELETE',2,'2011-05-11 21:39:51',1,203,NULL,NULL,NULL,NULL,0),(303,'TEMPLATE.DELETE',2,'2011-05-11 21:39:57',1,221,NULL,NULL,NULL,NULL,0),(304,'TEMPLATE.DELETE',2,'2011-05-11 21:40:04',1,220,NULL,NULL,NULL,NULL,0),(305,'TEMPLATE.DELETE',2,'2011-05-11 21:40:13',1,217,NULL,NULL,NULL,NULL,0),(306,'TEMPLATE.DELETE',2,'2011-05-11 21:40:18',1,216,NULL,NULL,NULL,NULL,0),(307,'TEMPLATE.DELETE',2,'2011-05-11 21:40:25',1,219,NULL,NULL,NULL,NULL,0),(308,'TEMPLATE.DELETE',2,'2011-05-11 21:40:29',1,218,NULL,NULL,NULL,NULL,0),(309,'TEMPLATE.DELETE',2,'2011-05-11 21:40:34',1,212,NULL,NULL,NULL,NULL,0),(310,'TEMPLATE.DELETE',2,'2011-05-11 21:40:40',1,215,NULL,NULL,NULL,NULL,0),(311,'TEMPLATE.DELETE',2,'2011-05-11 21:40:45',1,213,NULL,NULL,NULL,NULL,0),(312,'TEMPLATE.DELETE',2,'2011-05-11 21:40:51',1,214,NULL,NULL,NULL,NULL,0),(313,'TEMPLATE.DELETE',2,'2011-05-11 21:40:55',1,208,NULL,NULL,NULL,NULL,0),(314,'TEMPLATE.DELETE',2,'2011-05-11 21:41:00',1,209,NULL,NULL,NULL,NULL,0),(315,'TEMPLATE.DELETE',2,'2011-05-11 21:41:06',1,211,NULL,NULL,NULL,NULL,0),(316,'TEMPLATE.DELETE',2,'2011-05-11 21:41:11',1,210,NULL,NULL,NULL,NULL,0),(317,'TEMPLATE.CREATE',2,'2011-05-11 21:41:56',2,227,'CentOS 5.3 64-bit',NULL,NULL,8589934592,0),(318,'NET.IPASSIGN',2,'2011-05-11 21:43:25',2,24,'172.16.64.43',NULL,NULL,1,0),(319,'VOLUME.CREATE',2,'2011-05-11 21:43:38',2,51,'ROOT-42',NULL,2,8589934592,0),(320,'VM.CREATE',2,'2011-05-11 21:43:38',2,42,'i-2-42-VM',25,2,NULL,0),(321,'VM.START',2,'2011-05-11 21:46:03',2,42,'i-2-42-VM',25,2,NULL,0),(322,'NETWORK.OFFERING.CREATE',2,'2011-05-11 21:46:03',2,42,'i-2-42-VM',6,NULL,1,0),(323,'VM.STOP',2,'2011-05-11 21:46:24',2,42,'i-2-42-VM',25,2,NULL,0),(324,'NETWORK.OFFERING.DELETE',2,'2011-05-11 21:46:25',2,42,NULL,6,NULL,NULL,0),(325,'TEMPLATE.DELETE',1,'2011-05-11 21:46:51',1,2,NULL,NULL,NULL,NULL,0),(326,'TEMPLATE.DELETE',1,'2011-05-11 21:46:51',2,2,NULL,NULL,NULL,NULL,0),(327,'TEMPLATE.DELETE',1,'2011-05-11 21:47:00',1,4,NULL,NULL,NULL,NULL,0),(328,'TEMPLATE.DELETE',1,'2011-05-11 21:47:00',2,4,NULL,NULL,NULL,NULL,0),(329,'TEMPLATE.DELETE',2,'2011-05-11 21:47:55',2,227,NULL,NULL,NULL,NULL,0),(330,'TEMPLATE.DELETE',42,'2011-05-11 21:48:02',1,226,NULL,NULL,NULL,NULL,0),(331,'TEMPLATE.DELETE',1,'2011-05-11 21:48:30',1,7,NULL,NULL,NULL,NULL,0),(332,'TEMPLATE.DELETE',1,'2011-05-11 21:48:30',2,7,NULL,NULL,NULL,NULL,0),(333,'SNAPSHOT.CREATE',2,'2011-05-11 21:53:36',2,36,'i-2-42-VM_ROOT-42_20110511215128',NULL,NULL,8589934592,0),(334,'TEMPLATE.CREATE',2,'2011-05-11 22:01:29',2,228,'CentOS55 - Xen',NULL,NULL,8589934592,0),(335,'TEMPLATE.CREATE',2,'2011-05-11 22:04:22',2,229,'CentOS55LAMP - Xen',NULL,NULL,8589934592,0),(336,'TEMPLATE.CREATE',2,'2011-05-11 22:07:46',2,230,'CentOS55LAMP_BP - Xen',NULL,NULL,8589934592,0),(337,'TEMPLATE.CREATE',2,'2011-05-11 22:14:04',2,232,'CentOS55MySQL_BP - Xen',NULL,NULL,8589934592,0),(338,'TEMPLATE.CREATE',2,'2011-05-11 22:20:00',2,234,'RHEL55LAMP_BP - Xen',NULL,NULL,8589934592,0),(339,'TEMPLATE.CREATE',2,'2011-05-11 22:24:11',2,236,'Windows2003SP2_32bit - Xen',NULL,NULL,8589934592,0),(340,'TEMPLATE.CREATE',2,'2011-05-11 22:28:11',2,237,'Windows2008R2 - Xen',NULL,NULL,8589934592,0),(341,'TEMPLATE.CREATE',2,'2011-05-11 22:30:56',2,238,'Windows2008R2_32bit - Xen',NULL,NULL,8589934592,0),(342,'TEMPLATE.CREATE',2,'2011-05-11 22:33:48',2,239,'Windows2008R2_BP - Xen',NULL,NULL,8589934592,0),(343,'TEMPLATE.CREATE',2,'2011-05-11 22:37:15',2,240,'Windows2008R2_IIS - Xen',NULL,NULL,8589934592,0),(344,'TEMPLATE.CREATE',2,'2011-05-11 22:39:56',2,241,'Windows2008R2_IIS_BP - Xen',NULL,NULL,8589934592,0),(345,'TEMPLATE.CREATE',2,'2011-05-11 22:44:19',2,242,'Windows2008R2_SQL2008 - Xen',NULL,NULL,8589934592,0),(346,'TEMPLATE.CREATE',2,'2011-05-11 22:49:48',2,244,'Windows2008R2_WISA - Xen',NULL,NULL,8589934592,0),(347,'TEMPLATE.CREATE',2,'2011-05-11 22:53:25',2,245,'Windows2008R2_WISA_BP - Xen',NULL,NULL,8589934592,0),(348,'TEMPLATE.CREATE',2,'2011-05-11 22:59:25',2,247,'WindowsXPSP3 - Xen',NULL,NULL,8589934592,0),(350,'NET.IPASSIGN',69,'2011-05-12 00:20:22',2,23,'172.16.64.42',NULL,NULL,1,0),(351,'VOLUME.CREATE',69,'2011-05-12 00:20:31',2,53,'ROOT-44',NULL,228,8589934592,0),(352,'VM.CREATE',69,'2011-05-12 00:20:31',2,44,'i-69-44-VM',9,228,NULL,0),(353,'VM.START',69,'2011-05-12 00:22:34',2,44,'i-69-44-VM',9,228,NULL,0),(354,'NETWORK.OFFERING.CREATE',69,'2011-05-12 00:22:34',2,44,'i-69-44-VM',6,NULL,1,0),(355,'NET.IPASSIGN',70,'2011-05-12 18:48:41',2,21,'172.16.64.40',NULL,NULL,1,0),(356,'VOLUME.CREATE',70,'2011-05-12 18:48:49',2,55,'ROOT-46',NULL,228,8589934592,0),(357,'VOLUME.CREATE',70,'2011-05-12 18:48:49',2,56,'DATA-46',13,NULL,5368709120,0),(358,'VM.CREATE',70,'2011-05-12 18:48:49',2,46,'i-70-46-VM',9,228,NULL,0),(359,'VM.START',70,'2011-05-12 18:49:39',2,46,'i-70-46-VM',9,228,NULL,0),(360,'NETWORK.OFFERING.CREATE',70,'2011-05-12 18:49:39',2,46,'i-70-46-VM',6,NULL,1,0),(361,'NET.IPASSIGN',70,'2011-05-12 18:52:06',2,27,'172.16.64.46',NULL,NULL,0,0),(362,'NET.RULEADD',70,'2011-05-12 18:52:41',0,5,NULL,NULL,NULL,NULL,0),(366,'VM.STOP',69,'2011-05-16 17:52:45',2,44,'i-69-44-VM',9,228,NULL,0),(367,'NETWORK.OFFERING.DELETE',69,'2011-05-16 17:52:45',2,44,NULL,6,NULL,NULL,0),(368,'VOLUME.DELETE',69,'2011-05-16 17:52:45',2,53,'ROOT-44',NULL,NULL,NULL,0),(369,'VM.DESTROY',69,'2011-05-16 17:52:45',2,44,'i-69-44-VM',9,228,NULL,0),(370,'NET.IPRELEASE',69,'2011-05-16 17:52:55',2,23,'172.16.64.42',NULL,NULL,1,0);
/*!40000 ALTER TABLE `usage_event` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `user` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `username` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `account_id` bigint(20) unsigned NOT NULL,
  `firstname` varchar(255) default NULL,
  `lastname` varchar(255) default NULL,
  `email` varchar(255) default NULL,
  `state` varchar(10) NOT NULL default 'enabled',
  `api_key` varchar(255) default NULL,
  `secret_key` varchar(255) default NULL,
  `created` datetime NOT NULL COMMENT 'date created',
  `removed` datetime default NULL COMMENT 'date removed',
  `timezone` varchar(30) default NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `i_user__api_key` (`api_key`),
  KEY `i_user__secret_key_removed` (`secret_key`,`removed`),
  KEY `i_user__removed` (`removed`),
  KEY `i_user__account_id` (`account_id`),
  CONSTRAINT `fk_user__account_id` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=71 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'system','',1,'system','cloud',NULL,'enabled',NULL,NULL,'2011-02-10 05:28:28',NULL,NULL),(2,'admin','5f4dcc3b5aa765d61d8327deb882cf99',2,'admin','cloud','','enabled',NULL,NULL,'2011-02-10 05:28:28',NULL,NULL),(3,'postpaid','efa7b79ba8614495eeb36d31563f3a22',3,'venkat','n','venkat@cloud.com','enabled','42b6gfGn6lmsjCi1Cfo5UFE0h7t4ieYc26hWCN8M_kyBnuIuU-WJqf4mie0yw6EzLq-eA9cAenu-D77kx2SZwA','QT5tWM-_rWvSkz5NFaLEjrzuSJ_irOJyORk6PLXF4PlCSNXUlTH-8FEqBu27cLgUY-xhr37QjDfIW19im6tPsA','2011-02-10 08:35:39',NULL,NULL),(4,'postpaid','efa7b79ba8614495eeb36d31563f3a22',4,'venkat','n','venkat@cloud.com','enabled','NORuvRxueh6Akba_M7bSorpNbPz-ijziBCWvPcifxgqqADHNzGY6vT5pem18loFMGyznkEEfSCTjw_Q8ixe2yg','bnAxNZ5ChksR16YIq9YaOoXKYOjEtoqW6kO_vsKhxZxsxyyAkhBYhOVMoLhs4CP22YebdgWtW6noJJuL8QW4UQ','2011-02-10 08:38:57',NULL,NULL),(5,'terminate','ba74fceef14e0f6d49557415ff382a67',5,'venkat','n','venkat@cloud.com','enabled','umNF_nI2xYLYhshGJadJfARnBckeVoNe1n8V7IbO8rf7uARGutM3cmaOSBSkHWSadnhRAZKS4GZFFKDzsZ3cfA','nM4KYX69XnfnX5vVYPqM9W7fkpGiZ8w6q85XHwmgilscJZYRKt6TB0AKO3R3tmPQ_OebSjNx6aX6sbzpp3j31Q','2011-02-10 10:44:10','2011-02-10 10:55:02',NULL),(6,'terminate','ba74fceef14e0f6d49557415ff382a67',6,'venkat','n','venkat@cloud.com','enabled','ReMlJB6obmIvR2KK5XoXEs1hPaOFw7NPAYWGMSlAq7fNfXZdd7wXgl38QEMEibazdLWH79mUaX7rJtU-WnBp9w','uy0Fp9E0ERYJimwZyyAAKeXxJ5cY5aKdMucDI8rITtFhnzrdIYyHXkmgMc1sbVP-d13tjiZ-Ef9t0wiPdU-Eaw','2011-02-10 10:46:53','2011-02-10 10:54:36',NULL),(7,'terminate','ba74fceef14e0f6d49557415ff382a67',7,'venkat','n','venkat@cloud.com','enabled','U-VQB7zWeIc6UuiUWCOfgZCz39IkcCi1sAkVcMVxKLGQnTgZx5LMGUt0Q9h-rA1h45SethtWrDq-3pJmeyXptA','iH4B4i7WS1oCfDFbrKl3aTWYSUGhc11yF5aF8kNl2x-ZAyX141E8_3S0pC3O0dVHvQ5gtrnAB6eBtND37grULA','2011-02-10 10:56:51',NULL,NULL),(8,'mkashikar','4137ac399d9763e6f88ab915e3243a9f',8,'Meenal','Kashikar','prachi@cloud.com','enabled','EKfC2UqVVaGO46J943F9TWJ0XCljxNCVdJp_qHo4JuksDL5K9JZH3Ez6x7vX4UtpsxDI84BYs7ZAOP-MjVXdEQ','GATFHmNkr8w06vL5ohJjYw7OOzb9ASFYjMuFuZT80cPdHM7pYrNMfIyrjgg1DxLufDuFLRiAMRRp0NHnTWs5IQ','2011-02-10 21:15:38',NULL,NULL),(9,'marankalle','6adc89f822a97f1dcd7380da4e7f840b',9,'Mukta','Arankalle','prachi@cloud.com','enabled','LIrVBFsiwzCENVYKklKyvkz30I1f5WG2_atMYI-XI75uKWw6j4XBZmyT4-dT3M0VjG74fYtx9k_gR91P7dRYhA','niH6AlD457et1Qt_RiVjpQnAgnz1BBoN4U2vaixTwAbFBt3XUWy1yTEQsxrpDeOxF8_laLJhZpuwfGu1T9-niw','2011-02-10 21:35:35',NULL,NULL),(10,'pminc','2eec804d2ff814eda4e7548b09ad04f0',10,'Paul','Morriss','paul@cloud.com','enabled','El1dkfm7jdG4d1bcxVMGrPFjkvUr4FA_dEuuYbZEchIVUCxjlPbZrGQfa6B995EG-ZGqn4vdSeZ6H8AUr5Tijw','4cScLEnMQ11Mv_Hx9qxTg1Aoifd4MWe5Ef4Nd7ERk_tq6ezCaApwUNNQ6O2QaWlFMF5InubEbnk8QzmYw_s0OQ','2011-02-10 23:18:29','2011-02-10 23:38:41',NULL),(11,'pminc2','99f778e80473f7d2cae692d07d2c5f8c',11,'Paul','Morriss','paul@cloud.com','enabled','VcRs5ZUwscz10iV44GsPsB84CRHbBROvFmDkZkDIUvX8cSZ9lvvnV7MVajP-eowhdQZ7Z_YCNBSLicWaYKT8FA','9KQKjvtBmB2xCY5e0ZY4mss7gl2_8dRjFqOj7vOdKG4AiD5m5OKPBWHjSPokLcpI9nmarqvko8f0btJAWFuKBg','2011-02-10 23:54:42','2011-02-10 23:57:05',NULL),(12,'pminc2','99f778e80473f7d2cae692d07d2c5f8c',12,'Paul','Morriss','paul@cloud.com','enabled','hyKH03VNmDR2iJHcOhbvWtCv9QenoJ3y_UFr7nPE9fbjUgpphaNlwKqnfRFmVHCQF4f8tkOVHI_myILkZOR9uQ','Er4ddFVBuy8bZSr32XAHeu7JgtbQB5Daoc6JM12keemvRJfbh9PnFA0vevDR8ZvFubDnH7lYntWvVuo2Y3hwYA','2011-02-10 23:58:36',NULL,NULL),(13,'dnoland','a35be08dde8023c7e0d39f5abf134f98',13,'David','Noland','dnoland@cloud.com','enabled','dGX8mKie7cHNCCpV1HQGECQQOk1ABu7pY8PGPWn_jdZDPxV2Ac1JWBt9DHsXgI9SXLBe12b88FCx6sBUEumuOw','skirGImdUNs9E9IkJdVMMawxkZJwqvP5VlpkxsxXnLRm7nJMhcRividGypIUFyIuVc7LVAD4c65ZvDNZaU38Jg','2011-02-11 00:17:31',NULL,NULL),(14,'manish','4dab9119996d9a39eae333fdf5df8f3e',14,'Manish','Agarwal','manish@cloud.com','enabled','tQ2ktZhAAgLTqMr7SeN1H7u2JyfW3Y4b7TbQY1BqbY7f3dwRjE2kvqPAXlnx-fd82jUjMTfrCb6YNwl1tqEv7g','F1TB_Nia3fO0mgOEQYNnOXmJd6TRV9gGRW1e7-m5g_rXB6J6nJhX4nBpbewJsILOzgEV35wjRoXFo_6dRE4MzQ','2011-02-11 06:02:01',NULL,NULL),(15,'padmin','220d16be4d79984c673614942b8459dc',15,'padmin','Agarwal','manish@cloud.com','enabled','8F4m3WycR3oAp5kxyUwKSy4CEkcFtG1G8pNZPi0uC2qV8SpQH48xTx465DGxn8RJpiupqr4njmveXyozwjflRA','bax6rMV5t7-u7i58hXgWneGbZYVsrE5KmNHJq_JrJX9Nz6zq0L9qGnVE-88ixuH1Ek7ebDSiY5YY7opocBiDvg','2011-02-11 06:28:31',NULL,NULL),(16,'suspension','f38b73f6abddb5b30bf0b22a0ddab677',16,'venkatn','n','venkat@cloud.com','enabled','gT3wUpvpjNyau0ScNF0sC1DsBznbUv0csdHmI-mYPhXiP-RGUigHbWGVcm1KbKAEhDCFjPCVd2wP2esJLTmILA','3vAb1GtGPp1k_cOQhy0gpXjKlZ1M1FqoijZYQsTG5jdRYsDu_SCoptCVd67I7e8n7eRYw_5wTUMsHzQSzwUecA','2011-02-11 12:03:15',NULL,NULL),(17,'dnoland','a35be08dde8023c7e0d39f5abf134f98',17,'David','Noland','dnoland@cloud.com','enabled','aXbExBxSkcpOxMTTtBGi8XCYWgB7yZbPAfiNExBd4IyxH4M6SmpgGFkTM1xoqdjItlB70irq6ZCNoPTmZ8s7yg','mmpdkdounRYVCf8Dt5aPqrqyjET92976i8RRNzlyQlcCjZensOwKJ2zYyJfyZ6AMJS44FA8FiukPo8iNuyiarg','2011-02-11 23:04:15',NULL,NULL),(18,'msquare','4db161f66e6cb07ed1a210a02aff27d6',18,'Manish','Agarwal','manish@cloud.com','enabled','t8wjOEnhfTKHgwo4LNI9X5s0quauXy4mc_bKvFdDTPKOzmvpEsv3RSgUtBtPUJJkHIUSH9mkwKQTTrIlJFgE8g','CaM4Eb4lzMa8gv5P7SLJPPzoMvSIvqAao19eEvcdc2Z7x_lUhWEFcntPZyPaC420l1utlZBsxtQVZTsviRPQHQ','2011-02-15 07:45:05',NULL,NULL),(19,'opsadmin','f5c8a3c6fa9729d8003257037db05f49',19,'Ops','Admin','manish@cloud.com','enabled','olSHJC1GVon52lOPerdSmaxEA3kMLZmROQGJjNZos3OOHtV7LdjAGK-cYwQUCNeVSTyZSWquyqIg8ZJ82qkshA','fHf_jmws7u9rZaOfrElp1OnIE-GoNHqLkGLgvJBbM6vHaNJ2EsyTwnKESRN4-18hyNDYG8FLR1EC6qsoFVHAMQ','2011-02-15 07:58:43',NULL,NULL),(20,'bdone','aa43960139e0922b4787b76c123bcc8c',20,'BD','One','manish@cloud.com','enabled','8UNhrADLMittZTD_oHWC1d-ZZTeTBpZwZrj1dYqmQliE731VCSQXcgaRVWeUIuZL3-utIKIEDlVOsvS8nnUUUQ','ko4uEd3zJSfj22iy9smIrfOnlVHfI4rjN6t5Kx7tp-NHhrSclWnIAlt2lFp5JJ2I_DPI8RdCVncO839ByhQqww','2011-02-17 14:12:57',NULL,NULL),(21,'bdone','aa43960139e0922b4787b76c123bcc8c',21,'BD','One','manish@cloud.com','enabled','ueNFvhgMVnJcZo0YGCC3814kqd0irge64nQP1IlpF-68TwqNVQ42deyDOkOlvSwYYDu6kRtL2a6VTbbzO-XcbA','RL6rRI0kUFOc9dV0NlGf3Yyc7IqtZ2fxyVqhI_98Y4els20BJzedR7Tmgx9A7I5mEKfZDV2jQVyUUoXbnaSKXg','2011-02-17 14:20:44',NULL,NULL),(22,'bdoneuser','e2c186cb1be2997ac7d2e8e0bca7c497',22,'bdoneuser','User','manish@cloud.com','enabled','uJLsisNbiHO1vqwz5DVEebMST03CdDOmqiWsyW7_FWLf1m3qGHRmoeTmekGdDfbvxTme4GS-tSODQpF4lJMF_Q','Pp4GrUust52sYpbZ67S-m3a-jKTkchEyStgpU9V1DM4f4xYem7rOqQlxNHKDiKHOlP--tGcQtWAWDsj8Gq3toQ','2011-02-17 14:22:08',NULL,NULL),(23,'bdone','aa43960139e0922b4787b76c123bcc8c',23,'BD','One','manish@cloud.com','enabled','UFK5_A6rO6dzKL6-Gg8eSQFgbgs_p5OVVkOLbKsNnYah4DZIjD0cWVxnuOn-TiSYJQCrQvzvXR-WqOgqk5TKyQ','hxTOgHSDIgcrFGVNVk8_35LLf2cUv22HisXjLYvJbkCwiy9zXgEgf-xpOnuBvOTlPGVmVqvX9SyMffCpLnQsIQ','2011-02-17 14:25:46',NULL,NULL),(24,'bdtwouser','5e43ed95e31724353f6156b88c26fd8d',24,'bdtwouser','User','manish@cloud.com','enabled','jZ_GvHVXPdiPm6W_XUu02AfAWHg_CrWi8VZ60O018gR9WmlylnK1ErHq9Gs88EgY4XFFKQfnI_LHefFpMsUlFw','sawQ5HU_I9GBioj1qxfrMUWrD6jNfISNnByzIikORQArU5qtA2MIbdbObdZtzhIeSnINKyH5hMGW3RPQGwBX7Q','2011-02-17 14:27:09',NULL,NULL),(25,'dnoland1','f14717f5e5b3f0b56586cdfb74208798',25,'David','Noland','dnoland@cloud.com','enabled','hjziugbIBUMFSMiLcdXCqVrJlgL_HAjujI9EDw7tuxnR_zsS7isRFxhM9qcDcMilhCvDHQu9L-JcXoE1voW3LQ','jIfgjf06ILwx1MAsxoysDFMIPi44-ejWoTS0n8rjeVkmvI1IkwbDded5pbfJNKc1mXxRpBpcWDeQtleVUdVq7w','2011-02-22 03:59:40',NULL,NULL),(26,'locktest','82b41ab4c1059fde4774e6892ff03481',26,'venkatn','n','venkat@cloud.com','enabled','sE8-YEf_eii3rm9Rcnmy2pCmHkq90_n2wuTlE7_HuFBydUpvpXj0P9Z56hiSIOuXK62ImbnQTYmVpmvRMc4_Eg','3_6Tvy_8f8SfQlQ8CXCIvnSHh6LC1rgIb1Mey_pQtYNFcikQLWm93Cn2_JO6cyYePaXPHRyyPPeu0dRu2dOrlA','2011-02-23 08:57:39','2011-02-23 09:40:37',NULL),(27,'locktest','82b41ab4c1059fde4774e6892ff03481',27,'venkatn','n','venkat@cloud.com','enabled','wvRktnQgDTo1x77MNov6ooX6vgWUNONaIRUEqd-r2ke53SlNixp5wBEe3MugoLlBTgNkCHHEvPRqQjGfW7c4ug','7On2OXcUDt1HyqkUIjqvFEfl7I0WQCi_1NKAbLjnXCNHVjuRDHX2aTb1opNkS_1trtnrfkDJeg_epfqT3Esthw','2011-02-23 09:57:17','2011-02-23 10:38:30',NULL),(28,'locktest','82b41ab4c1059fde4774e6892ff03481',28,'venkatn','n','venkat@cloud.com','enabled','fzR7Sog4iyEpt87Lo_bBpUjg09eq1KYh5AL89FWnP_CHiEBffyee7alsKzX55vHNTSv52Cudavqq44BCHlJeSQ','qqpejcpH93JGYmDCsCyKuThf-2igX6QZxPoyj5OW1dxrZ2Sj-NTcrf_8jh-ShnMevFc4boYs23MwCyiBw6jsIQ','2011-02-23 10:01:10','2011-02-23 10:38:29',NULL),(29,'lockadmin','2f736ead431bcbc196ef5d499816ffa8',29,'venkatn','n','venkat@cloud.com','enabled','L-v6IgIlI52iry8-mng8aphNAiNeg0s5PvRroLzQAtzN6XKs0xjYHIAguY8oOlhzOYAUPEqk-PX-Np4fr0UgPw','TwEHXJO2gF2fCEaNKuMlWb-_GGwGaG5Qq0G6sQ1xX7yYqozkfF48V1LI3z-Jur4jQHSfo-w7C7V6sI_-lc9U6Q','2011-02-23 10:06:44','2011-02-23 10:38:28',NULL),(30,'lockuser','092481cfc9e1c40bac81fbeefbab7ad8',30,'venkatn','n','venkat@cloud.com','enabled','_vsiv0GPEZFU-wwVudHoeI9duPN38_t3ahDJUt3qvToG083fLzc-S7igYisbXUoVw26FFa6F49A3o5KaAkD7sA','MedGkoz4PqwX9dfN7CztF95lGNvq3BWLvOolVc7kcDv6msrMb9APbGqBMD9mVb6SK5ip52J3V3e2Ah7aeco7Cg','2011-02-23 10:15:38','2011-02-23 10:38:28',NULL),(31,'locktest','82b41ab4c1059fde4774e6892ff03481',31,'venkatn','n','venkat@cloud.com','enabled','i-NHSwFBdzl0fDENjqIuPmEU1bbPqm_ZfPchVGMac2axGAxRszwRY8N2SQnI84s5QSICzz1sU49P7E-DU7lc5A','QqMBI-AGG3OxosEgLwvvCuca0Q7JAOh2JmbOzCe5XXslnX_JWJ2QE3BVHBQkpSi2JDbQc6kz9f5qlUBjRCx78w','2011-02-23 10:42:20',NULL,NULL),(32,'locktest','82b41ab4c1059fde4774e6892ff03481',32,'venkatn','n','venkat@cloud.com','enabled','hJUp_82jP_JocH16-56TY8wAeXiAT40YtHyBcmlFOvU5oheSbjXTmqauhAuNuFtRZ4Xv9Lg3e52nI0hH79mGXg','_hGlwhTPpU-84tN9qwpv5raiyDdI5UL3hCp-AqC-7BhOjDsY3Cv9bFCbzxNNDHOoIYwgVuQMrnrtP0kQKyemdw','2011-02-23 10:45:39',NULL,NULL),(33,'locksecond','69f7098339fa05c005dca295ab271d19',33,'venkatn','n','venkat@cloud.com','enabled','-aJk4yncSj2_UCe9rkqJikT77ekvyIIvC6GzzTMyJLyRfiDiVH5pK3FqB6oAIkJ71-fOyUjq_HjCHXJCWq_zRQ','GUs7yrcnRn0iVJOwyY3vJvF275gzfSIzqi3PNdavmiZ7iYD4sa3hPFRyV190Le9v3lS_EtPT_kbFSyz5LGULEw','2011-02-23 10:46:52',NULL,NULL),(34,'gplains','daea933638a2e864ba08ef48d9544d27',34,'Great','Plains','manish@cloud.com','enabled','R2jcycStrTKZ0cZjH_esBIQk9_Mhy0n17nld2RF25kEVMe8DSHaSFHlrA4evcVCoNe7FupXc6lvOTCMdrf9tdA','Bf8af_BqFZOcLNx0-7mSOSVXPomJWsV3Qmhd90acnSq8DBhHjEJ3ArY98g3n3RrbjxqpR221xf2LWBkHlxzdmw','2011-02-28 09:01:33',NULL,NULL),(35,'zynga','e829f3e30628938bbd8931904bddfe50',35,'Zynga','Zynga','manish@cloud.com','enabled','Lbd_cT-fROBArHe_Cxxil3waAeJv4nkfj3rmiCPJD_w7vFNFf12DsZFSxyj65QW9IO_o0lp9EBjRryEEN6mSVg','f-CgyCTmNDtTXvqlvE1pYP0uTVJJfTud1gSfMX6EsvR8YUogx8teNEaWMVg0iJ2PvprN4J6k6MTKAjK2bTzl2w','2011-03-02 02:49:33',NULL,NULL),(36,'zynga','8d79675b62edc0766ff48768ab106544',36,'Zynga','Zynga','manish@cloud.com','enabled','_YdsXumO9tXHsDFaXQrr9JkgxLXSWK7-x2Fph5oKXLeb2nH6U9nyEdd5prdoi2SE5KW9FYupJ6g1JUrzbefKZA','c0ykoyS1qjns67jrGUPa6Qo41Dqahv9CacLfHwOPMqbZFDRpwphjhvi9Wk7Z7rq8HJtU3_Yp3JjA_--a2h7bOw','2011-03-02 10:37:57',NULL,NULL),(37,'zynga','c6d8a5907ebfd620838d6cd387f00357',37,'Zynga','Zynga','manish@cloud.com','enabled','VG_jhvokDqeJ0HDdFV9zztxDNqjhVtiP6wUBeoUBlDN1r64OHCKwJp5opX09ZTd9SP6lT_sg-Ehv24uiM_18dw','2ObTt_qsr474lqRw-M2UO97hxhn-COR8HE4bWGEPceXuKx1jXPyWrgXcybyHtM9FCmZVEIcLeRHr1k7Wh19Vjw','2011-03-02 10:41:08',NULL,NULL),(38,'Zuora-Msquare','62a9c534081b851420a2ba2c752893b8',38,'Agarwal','Manish','manish@cloud.com','enabled','0zKadC8z2Rh77k8qwsgXXG-owMYooNr1ZNgqQARQq3KkN1tezPm9g_LBWt0XSxMbH1JaO_4CXXm966Kgmk__1Q','Ib7WYekoFAcYD-Et-6sgVP2QB0zyvhdMI5vvXvVfGMNmWpnX_eoSpRjICb_6AMgNB80u3CLTl0e1P_pE2Tis_Q','2011-03-10 10:43:58',NULL,NULL),(39,'partnetisisue','bce5b1db2159fd9f31470fbecdfb3808',39,'venkatn','n','venkat@cloud.com','enabled','jzf-MkNxAqCFlYOEj1TwmrH7euO_9OTnX7aYFFm85b5B2aMeHARwRNeeQx3TDfumcj5T4XjKpphanCaLBU9Nyg','9SYJrKWMNOdC1Cqi0ezhHU4pa6E1f11uW75HidZX7K3H3zpOcM_AHaPjsKFPwGwxKYSv_37eBZ5bwtDIXpsEMg','2011-03-15 09:51:09',NULL,NULL),(40,'cindy','1d7d1fe6105675c5b91bf62963de59c3',40,'cindy','cindy','cindy@cloud.com','enabled','V_hyqCRBtA-zHhzYAsbQHdfMdsyiu3OomVxXeT7Xg-gXXHHs9YVEYgqdFCoLtPF41zX2mfbuotsGPST1fHS-cg','zNIA4sPwuqE44k_H07TxxkOH-9xc1SXIwVgmCnIEBl83tbYUFQ-4h3wlMOOn2MzrVAGMx2XwumExz5GmqyMnzw','2011-03-16 22:38:54',NULL,NULL),(41,'agarwal123','56e9af361a26302932c358737d5ed4e9',41,'Manish','Agarwal','manish@cloud.com','enabled','0sk_-GjL_H8ZpOVKS6-260SZPkb0tPrSdSqikO-_06QvagHkIXG063q-MkxosElKBqHmh9oPqrLsUe0qrBPtwA','0FWw7R_ZhKr-osiJ0x47jwFgj1JZJmekrdDfwZmII_RCP8ojCJGVYCOD2mWjiHeuIV3GZD72S_g8-oYlU8e5pQ','2011-03-28 06:19:36',NULL,NULL),(42,'shweta','38ec3eabbdc21b526f3dff1029b06f13',42,'shweta','Agarwal','shweta@cloud.com','enabled','HYQgW9K3Y6nF3A6TgLnSJrnxJ6P7oCuQZN26NkZIx1rr6gbIwERN766cDYJdirYSVrMf6s-jQh8WrJvFPYrsqA','5tGEDArWy4YnNowaRXKiJorriAX9vIzyTKwNdnQHJnasDYJ3X7bvCMEurrS3xCE3m9FkQKN41Ge10h99zFac1A','2011-03-29 06:49:10',NULL,NULL),(43,'shweta','38ec3eabbdc21b526f3dff1029b06f13',43,'shweta','Agarwal','shweta@cloud.com','enabled','xXjOcuSEDlSvAm6o9gpw3conMXxyhANyu-ziYs3Fziw6bMjKBHE24BYo-C6JbOELLO-wBFc7kJZv58rj4tBFJQ','oZakSlUdrVadfbwhtrKwqTOBCuy5AUzuLQhVl8EQtlpash1lFjghUIrEY5IEow7NwH6EoDcjPTz8t55wEzyWvw','2011-03-29 07:05:56',NULL,NULL),(44,'shweta','38ec3eabbdc21b526f3dff1029b06f13',44,'shweta','Agarwal','shweta@cloud.com','enabled','bZLjJYQpeF68Wa-F3pZdhAGOUyPw6f7oZLEBAAEkTGeN6PQXyeWJplDf32D9afnGVIMc0zKryemLAttieYLpew','mxxAjHISWjZYClay92NK0hdgxVsyk1mTsRIYhWcTKVwxBQr1Uazqr7b1cGzVPiEXNIMyF5Ubriq_yV2gPg8GKg','2011-03-29 07:06:49',NULL,NULL),(45,'manish123','7f214c8dbc6756f99858510ae4abbf2d',45,'Manish','Agarwal','manish@cloud.com','enabled','oc5Pa16ayaY4ashr7aUFm039yRj_PYNnWAY24xtLQ5pPJGWKFiIcO7o281bUIRxZIEEUFoPR_rktR14D5cFz8Q','Lhou4YLemw-L-7-zvfzG4zEGcLHzfJiOSg6ED3O8mQgID4qSv--nd3XGeKQW0efwb6lEvlkAnD1XMw3be2opVA','2011-03-29 09:30:25',NULL,NULL),(46,'manish123','c556c5f0aed518fdfcabcd5434bbf529',46,'Manish','Agarwal','manish@cloud.com','enabled','a89bApllvnJzTuHXEnvHQ3uQwbVQV6oz9VNCJhj0I2NhFpeu-wtr4XYb5V6fbHNcHZkBnCTDo8F4sdR8CT6Xlw','CtfAwqBjj0_0KgFu0VjbegAvNNTfK7rPGSJNgr_wQbQKavyeeyr01lvqvsEh85_h4ZgDCZVz8NG8_Sp6bE-Mrw','2011-03-29 09:31:29',NULL,NULL),(47,'manish123','c556c5f0aed518fdfcabcd5434bbf529',47,'Manish','Agarwal','manish@cloud.com','enabled','Wn9Q8g8Eli46fwzRiZUGcuBPweulNRpkNOt7_Pbf3TB1eZlbmGz4_w168kS8sYQCCzs8q_ul6zcwmv0qeG7ggQ','8PCtxXWmkuYhNwHDU4EdUbzg2aF0jd9dpwi386NGFMOC-6ECHs8pRgeVkOhrugJ8R7ET47ZlocK-CX-t6Rv7cg','2011-03-29 10:26:40',NULL,NULL),(48,'manish12','9ce9dedf69609f2aae062a243a00d489',48,'Manish','Agarwal','manish@cloud.com','enabled',NULL,NULL,'2011-03-29 10:27:00',NULL,NULL),(49,'test1','d7afda2508c437c69bde95fdae4e892d',49,'Test','one','shweta@cloud.com','enabled','vZgiR2ex5Lezxp4BFOSQGdIS8u9x-0Zhuf1bxuOuvIQZ2AHT7hqE10SOPO9sdH9JAm-dh_B21wAQVVa9-P2EFw','Ycri7RzmylpdpE-cV9iIGcLG2yxu-Wh-_ktZRUgxISYo6AvT0v7YD7zZUI65N47QGZJ9CaUyS_htYYYlf7opww','2011-03-29 12:11:57',NULL,NULL),(50,'test2','b71fb5c010ba5449cd1ffa587b002381',50,'Test','one','shweta@cloud.com','enabled','WpRSKGll5wE8FUuVUA6e7aRGms4eGML1-Gq2QNlOkF0QF1EvqQIu0oGi0dT-mZ7w978OnD2FRcomacBpJrZr8Q','gZPnQOUC5BgXmjy1Rq-hUS14jtJ01EC4HnAd7R3vRHWLprYi84yxlPY6yxAPk0pWLcsMp5lOG-YgsB29nS3LHg','2011-03-29 12:12:21',NULL,NULL),(51,'test2','b71fb5c010ba5449cd1ffa587b002381',51,'Test','one','shweta@cloud.com','enabled','MbGDHtXK8QvK57Ety6iw7ve0ZblEWhxE9lRBKfX6Bo12-yOyAZd6XeecZwbaYUOsH_4hoHlQshnbFAZAldheAQ','dJhr36yd6eQ9MnnrG25oExu28y4Bk_Vp-Oy2cWg7nZMSGMyw-JjSaHU2UapJCWEQLaY24gI2pOtwei91M7nREQ','2011-03-29 12:12:59',NULL,NULL),(52,'Koteswar','db0c961262b93c1c399999b2707df136',52,'Koteswar','R','v-sailendra@cloud.com','enabled','gCyoRERmggYFq4K5mveKe3XOaVT1UGiacpRl1Q3ZP52jsv85-ZlXsp5T-B7J7u377YGNf_46hLlmwx725ujMmA','kt2l3dzD4FIS0dRId2Oq3ZaV2FGP3AhsYGdo3oaEs8yLmKuyb379cWTlr0-2vEt0QlH48XWLbzqfd1E4yN6sGw','2011-03-31 04:52:34',NULL,NULL),(53,'murali','5f4dcc3b5aa765d61d8327deb882cf99',53,'murali','reddy','murali@cloud.com','enabled',NULL,NULL,'2011-04-04 18:27:58',NULL,NULL),(54,'atmos','f221cb454c451872ccf74e0c357458da',54,'Atmos','Guy','manish@cloud.com','enabled','ibTX5-nRzNNpQKa7K62kqm9uDzlcqN7MSThIGJSiZ5e8CLA0QZ1nGa1Kl6l97S3K6iUMkVi-yf1XYYxM9af65Q','QdoHfU0HAxQxebl2fmClWcl0LJ_KczrClnsmO5Ow6BiLolC75e386_LX1Yoc7VIu2GH2QDllUzoiZ6-YiDv6ew','2011-04-09 10:19:03',NULL,NULL),(55,'atmos','f221cb454c451872ccf74e0c357458da',55,'Atmos','Guy','manish@cloud.com','enabled','HP8opw4U35hLWCrTh6SAOnKGHW7OTUp8bahX0y2_wxwbpfswsnHdSgjlddyxDbDAMqsK2EZN7GwgQpbhCVBBFA','DohE8MVB22_fwvCE2OqxSuXjp3zrkIrRfTUccuwm8stB0YuF6QMgs3d22kPrd4uTHf7usOdmTFsTNiY7qsMo-g','2011-04-09 10:21:11',NULL,NULL),(56,'atmosuser','5ec9ed952437ca5da53fd4b805dd1dbb',56,'Atmos','User','manish@cloud.com','enabled','YV6Q4Wvzr_LJHu41vyKW33B-gwAQ37xxNiM6momKxCOPnsq5mDRi95-Xg4S76B8TYa0ad4MKPRIKMMkwW2N09A','Q_e6cjsTbYKp_133fafT_KkXLUQOwBD0zI-vrAGRATz-WoO8wqCbdQ-dioGYfLpNGutZUYWBT7q683UjGHCOcQ','2011-04-09 10:22:11',NULL,NULL),(57,'pmorris','91751ce4f721409ba6a00797e94a0b6d',57,'Paul','Morris','manish@cloud.com','enabled','jQLYKxeeJGlWgHudeyAvEPZH8CzVct9LZ6JzVNGI4P0FgOS_LLlAdKK_TdgCtlg4wYSncHizFn6bJ6O42LXaog','8ccg_pGJI0UuHWy9ial-5d0cYokHFlZ-NM6tE5L9PculewShhQSIcrEUSEG_tSEjljUSxwciqIJKqFSVSdMiOQ','2011-04-25 12:33:11',NULL,NULL),(58,'pmorris','070fe11cdbe23fb41f35db46a340a9f1',58,'Paul','Morris','manish@cloud.com','enabled','-nhaDxGzQjbSz8gAS9Ul6_g1GwTMOJV13BPH_P3QL5zo75-luzISlJbv05CUH7dj_7C63w89tcSMzokHi_Oq1g','dulykBy8kXMCO5mnpTcSGAOenUJrx-BxXYfrcHBHLIRAaD0PrmezJ7iCQRVUHAAlNN5nEPArZLKaIX3udx2fWA','2011-04-25 12:38:05',NULL,NULL),(59,'pmorris','cac166655dd6841a5687abde39a938d9',59,'Paul','Morris','manish@cloud.com','enabled','FBY0nF9VfkBVLS23-uUASNsDsAJg4JikMrdqf0xj-bWZcQgY6t1Vpt-w7X29Cgtv68ptBfhMDwBKaL8OjDXGiw','X6NDOBoF7akaBdVIiSsse83BNL6jSEb1lcxg5UXsZdrenVDuIqpIUy4xDDcnfJkuf1SWoobMNaulBjHDlDYWdA','2011-04-25 12:42:52',NULL,NULL),(60,'pmorris','cac166655dd6841a5687abde39a938d9',60,'Paul','Morris','manish@cloud.com','enabled','i3CleBXrDbIoVrxQPlkmAd7hlAFfoEsjOSo9-aXLB05PyKXFBys1QS0dtgvK3OU_3TaXzJQZQW4ZGvLSRxkzlA','E-92N2egMOfp0stOACAo3poR2VLuOpzQswgLZxEbs-YHNucyD9lAOPE0Fc-AFRWOvp6d9WxyE2V-HeNi2ELfig','2011-04-25 12:44:14',NULL,NULL),(61,'pauluser','d427ccb07c6c1a70481e3c50491a585e',61,'Paul','User','manish@cloud.com','enabled','d4pJCNWQgwy25HeFJNbW41wmOEmWK6y3JoFmXJvLIO4EWPm0d_rVzIz-QfuUxmPu0OukcR15KBDZ8AZupQhKXA','fRF1LtZrlYaUzpHM8saja-9TadXkU4Da7tR2oT-3W14_Uu646s-tlUMdLatlVcstgzCKZ3qjLga1qdWYcpNUCQ','2011-04-25 12:45:24',NULL,NULL),(62,'magarwal','dcac50e02ee238963fae7e2d163183e5',62,'Manish','Agarwal','manish@cloud.com','enabled','mXVvpebmFk5vF1kxhlpVIWNkCexaTU72M99M3l1timcIBCWjovzcx433u7myLblBYfgp7TQGfTIC2nUQEvfnyA','W-H9LpUFDA-bSWuDonm_IyEDDNiOCmgy_ED9oDFS8s2wZE9PlZo34Pd4GABQJMkE02zhq83s-XipGiy26zs8yA','2011-04-25 15:56:56',NULL,NULL),(63,'manishtest','06918bc8133c9ecc54ee63160740b029',63,'bdtwo','Agarwal','manish@cloud.com','enabled','zJda2pof3mGKOEiQrlI3Ht4IFD7ZicWwEHETOsuUrT2pJwG7fQoMSzTVzCfbyQV7JZhYLzsfzOWgimu6_VpBoA','d8Ltmft_V4-fatzr_zhFXzOS5VSx0BZdbEzQCR8OWjRpV2hyEVU-V-u16JnzAW4T5QMmzac3BmWZi0ihPcGhyw','2011-04-28 13:18:16',NULL,NULL),(64,'dnoland2','f0d23752162a38abac2986c061f2d44e',64,'David','Noland','dnoland@cloud.com','enabled','cL2qiofR8ivHuzr8ciQn7sm5t9BEcZdf8dvalAsCcA_uRd8KHpzpkJj4qoQd-PFzsRPtcEHWOK1iPBB_y3y7SQ','ZK4H-m3TmSGz8y3vlPc-uKHIOaHiOV4jd59h0iAJquWvlPM19tb0KOPqtbWhceV96PfDjNB3a8byzuKK2d0HkA','2011-04-28 18:21:02',NULL,NULL),(65,'kkagarwal','f81f457b9539c7c4d816883799c7ee82',65,'Krishan','Agarwal','manish@cloud.com','enabled','pcde5Wy5AbyxTgIVJAFLN-DQqaw4dtSi4YcTqc0gjWUVzog6pkNqwQgaldUMgY3p7tPoa3UqdnCzsBuYXkN0JQ','ak3ngRTH_kp29BntE9CfQDaALpikMSQPPky9xF5ocTeRAP4FK4jFAxqrD4mMDh5MgJIxZMYdz0EQ-UuTDiuCLA','2011-05-10 19:45:18',NULL,NULL),(66,'kkagarwal','f81f457b9539c7c4d816883799c7ee82',66,'Krishan','Agarwal','manish@cloud.com','enabled','tsJFI0bMfaHCGnUcNFELTT3WMwSnVLUPMC2K6BBOHeUkBiP4SvPOdDG3gOl287NCPtEDBEv57EC0chhl-Aw9Gw','uDWGeK6nojGGNbfj0QbWZxMyupctUS3WX65nkqRMtH-PvyZJxal7ST5YiudbHmsHrfHKWFDav6LNiEnVPRPUkA','2011-05-10 19:55:59',NULL,NULL),(67,'kkuser','690c0b68426cd14bef410b05c56672b9',67,'kkuser','agarwal','manish@cloud.com','enabled','V1qNM0QuENAqH3rH4Y5bcRJ8Ki1P8YbFZPAIU0IO6LMNpSlZgwT72B3l-S0gbIfSWr7AGd_sOQD-5Aj9Cs5gIA','ETMGFKzu8-OF7KGOpJ7_f_w9mH0jg0esq1r3_-VK7k7nagf2ydSEZCOv-GYyGKdLe-lfspGq-XjjtB95N9F1LQ','2011-05-10 19:56:50',NULL,NULL),(68,'dnoland2','f0d23752162a38abac2986c061f2d44e',68,'David','Noland','dnoland@cloud.com','enabled','YdG5qD_YROAK1-XRJLfOv7AOI5Dw6PYp5syfDEoiMWH8Tdk38tOxOaKCO_g5x7sbwCWRbO_4HusqOSYfMpRwOw','jvi4eIc5eO-MUhW3lHUU-Xpc9zQ6KdsYWGuWjUcCWVAu93BbIOGu84FDO4r6NsLpjl2avUtqS2KRDIz3Y-FDcg','2011-05-10 21:21:28',NULL,NULL),(69,'dnolandterm','15c7e3745c087876d99fc8cef21f6574',69,'David','Noland','dnoland@cloud.com','enabled','LagmlNgYmemKduHlGs2koEEsgvvXvPMP6bDS-zhA0kunqBSYiVo10yG3U7JEjyGoVUZjknJ5JT39VHH8VTtzMg','ZBPSqSgFRzmzRQDBFSYUBUVyjkJ_qnqcBw9auJu5R-Z_7qDkkZsD9PMGwiPW7vUPYPV-Le1OIRDIaqRzTzVZEg','2011-05-11 21:01:30','2011-05-16 17:52:14',NULL),(70,'deepa','60b05f1019c5ed1223b89a592a3049d4',70,'deepa','Agarwal','manish@cloud.com','enabled','EJSOHJ2ceTBFhueFlUzOWSL7qv08i_xZ1OTzDc9HH-PIoFpOrPVhtQnW2Wdmb6nQMrfhBt1U-Urw27JglDJKjg','APHEDXLS_KRQXJVm4Sup_kUFnmWZMrgpqGbmpNOfSVluq7E5QlmtafyYeDwXXbYIL88Te9a0LUf4mAgQyZwQsQ','2011-05-12 18:44:46',NULL,NULL);
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_ip_address`
--

DROP TABLE IF EXISTS `user_ip_address`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `user_ip_address` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `account_id` bigint(20) unsigned default NULL,
  `domain_id` bigint(20) unsigned default NULL,
  `public_ip_address` char(40) NOT NULL,
  `data_center_id` bigint(20) unsigned NOT NULL COMMENT 'zone that it belongs to',
  `source_nat` int(1) unsigned NOT NULL default '0',
  `allocated` datetime default NULL COMMENT 'Date this ip was allocated to someone',
  `vlan_db_id` bigint(20) unsigned NOT NULL,
  `one_to_one_nat` int(1) unsigned NOT NULL default '0',
  `vm_id` bigint(20) unsigned default NULL COMMENT 'vm id the one_to_one nat ip is assigned to',
  `state` char(32) NOT NULL default 'Free' COMMENT 'state of the ip address',
  `mac_address` bigint(20) unsigned NOT NULL COMMENT 'mac address of this ip',
  `source_network_id` bigint(20) unsigned NOT NULL COMMENT 'network id ip belongs to',
  `network_id` bigint(20) unsigned default NULL COMMENT 'network this public ip address is associated with',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`),
  UNIQUE KEY `public_ip_address` (`public_ip_address`,`source_network_id`),
  KEY `fk_user_ip_address__source_network_id` (`source_network_id`),
  KEY `fk_user_ip_address__network_id` (`network_id`),
  KEY `fk_user_ip_address__account_id` (`account_id`),
  KEY `fk_user_ip_address__vlan_db_id` (`vlan_db_id`),
  KEY `fk_user_ip_address__data_center_id` (`data_center_id`),
  KEY `i_user_ip_address__allocated` (`allocated`),
  KEY `i_user_ip_address__source_nat` (`source_nat`),
  KEY `fk_user_ip_address__vm_id` (`vm_id`),
  CONSTRAINT `fk_user_ip_address__account_id` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`),
  CONSTRAINT `fk_user_ip_address__data_center_id` FOREIGN KEY (`data_center_id`) REFERENCES `data_center` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_user_ip_address__network_id` FOREIGN KEY (`network_id`) REFERENCES `networks` (`id`),
  CONSTRAINT `fk_user_ip_address__source_network_id` FOREIGN KEY (`source_network_id`) REFERENCES `networks` (`id`),
  CONSTRAINT `fk_user_ip_address__vlan_db_id` FOREIGN KEY (`vlan_db_id`) REFERENCES `vlan` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_user_ip_address__vm_id` FOREIGN KEY (`vm_id`) REFERENCES `vm_instance` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `user_ip_address`
--

LOCK TABLES `user_ip_address` WRITE;
/*!40000 ALTER TABLE `user_ip_address` DISABLE KEYS */;
INSERT INTO `user_ip_address` VALUES (1,56,48,'66.149.231.101',1,1,'2011-04-09 10:25:42',1,0,NULL,'Allocated',4,200,215),(2,42,39,'66.149.231.102',1,0,'2011-03-30 08:13:49',1,0,NULL,'Allocated',5,200,213),(3,6,7,'66.149.231.103',1,1,'2011-02-10 10:49:41',1,0,NULL,'Releasing',6,200,206),(4,61,52,'66.149.231.104',1,1,'2011-04-25 12:53:14',1,0,NULL,'Allocated',7,200,216),(5,NULL,NULL,'66.149.231.105',1,0,NULL,1,0,NULL,'Free',8,200,NULL),(6,24,22,'66.149.231.106',1,1,'2011-02-17 14:49:43',1,0,NULL,'Allocated',9,200,207),(7,22,21,'66.149.231.107',1,1,'2011-02-17 14:53:20',1,0,NULL,'Allocated',10,200,208),(8,60,52,'66.149.231.108',1,1,'2011-04-25 12:56:50',1,0,NULL,'Allocated',11,200,217),(9,51,45,'66.149.231.109',1,1,'2011-03-29 12:19:09',1,0,NULL,'Allocated',12,200,214),(10,2,1,'66.149.231.110',1,0,'2011-03-28 12:07:03',1,0,NULL,'Allocated',13,200,205),(11,2,1,'66.149.231.111',1,1,'2011-02-10 08:46:25',1,0,NULL,'Allocated',14,200,205),(12,4,4,'66.149.231.112',1,1,'2011-02-10 08:42:57',1,0,NULL,'Allocated',15,200,204),(13,42,39,'66.149.231.113',1,1,'2011-03-29 07:55:22',1,0,NULL,'Allocated',16,200,213),(14,NULL,NULL,'66.149.231.114',1,0,NULL,1,0,NULL,'Free',17,200,NULL),(15,NULL,NULL,'66.149.231.115',1,0,NULL,1,0,NULL,'Free',18,200,NULL),(16,20,20,'66.149.231.116',1,1,'2011-03-09 10:51:23',1,0,NULL,'Allocated',19,200,211),(17,4,4,'66.149.231.117',1,0,'2011-02-10 09:11:42',1,0,NULL,'Allocated',20,200,204),(18,3,3,'66.149.231.118',1,1,'2011-03-10 00:27:14',1,0,NULL,'Allocated',21,200,212),(19,NULL,NULL,'66.149.231.119',1,0,NULL,1,0,NULL,'Free',22,200,NULL),(20,NULL,NULL,'66.149.231.120',1,0,NULL,1,0,NULL,'Free',23,200,NULL),(21,70,60,'172.16.64.40',2,1,'2011-05-12 18:48:41',2,0,NULL,'Allocated',6,218,224),(22,NULL,NULL,'172.16.64.41',2,0,NULL,2,0,NULL,'Free',7,218,NULL),(23,NULL,NULL,'172.16.64.42',2,0,NULL,2,0,NULL,'Free',8,218,NULL),(24,2,1,'172.16.64.43',2,1,'2011-05-11 21:43:25',2,0,NULL,'Allocated',9,218,222),(25,NULL,NULL,'172.16.64.44',2,0,NULL,2,0,NULL,'Free',10,218,NULL),(26,NULL,NULL,'172.16.64.45',2,0,NULL,2,0,NULL,'Free',11,218,NULL),(27,70,60,'172.16.64.46',2,0,'2011-05-12 18:52:02',2,0,NULL,'Allocated',12,218,224),(28,1,1,'172.16.64.47',2,0,'2011-05-06 22:06:57',2,0,NULL,'Allocated',13,218,NULL),(29,1,1,'172.16.64.48',2,0,'2011-05-06 22:06:57',2,0,NULL,'Allocated',14,218,NULL),(30,NULL,NULL,'172.16.64.49',2,0,NULL,2,0,NULL,'Free',15,218,NULL);
/*!40000 ALTER TABLE `user_ip_address` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_statistics`
--

DROP TABLE IF EXISTS `user_statistics`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `user_statistics` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `data_center_id` bigint(20) unsigned NOT NULL,
  `account_id` bigint(20) unsigned NOT NULL,
  `public_ip_address` varchar(15) default NULL,
  `device_id` bigint(20) unsigned NOT NULL,
  `device_type` varchar(32) NOT NULL,
  `net_bytes_received` bigint(20) unsigned NOT NULL default '0',
  `net_bytes_sent` bigint(20) unsigned NOT NULL default '0',
  `current_bytes_received` bigint(20) unsigned NOT NULL default '0',
  `current_bytes_sent` bigint(20) unsigned NOT NULL default '0',
  `network_id` bigint(20) unsigned default NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`),
  UNIQUE KEY `account_id` (`account_id`,`data_center_id`,`device_id`,`device_type`),
  KEY `i_user_statistics__account_id` (`account_id`),
  KEY `i_user_statistics__account_id_data_center_id` (`account_id`,`data_center_id`),
  CONSTRAINT `fk_user_statistics__account_id` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `user_statistics`
--

LOCK TABLES `user_statistics` WRITE;
/*!40000 ALTER TABLE `user_statistics` DISABLE KEYS */;
INSERT INTO `user_statistics` VALUES (1,1,4,NULL,4,'DomainRouter',0,0,63528,21828,NULL),(2,1,2,NULL,6,'DomainRouter',232,0,796412,345278,NULL),(3,1,6,NULL,10,'DomainRouter',44,0,0,0,NULL),(4,1,24,NULL,13,'DomainRouter',212610,0,0,0,NULL),(5,1,22,NULL,15,'DomainRouter',225358,0,0,0,NULL),(6,1,26,NULL,18,'DomainRouter',140,0,0,0,NULL),(7,1,30,NULL,20,'DomainRouter',228,0,0,0,NULL),(8,1,20,NULL,22,'DomainRouter',152252,0,0,0,NULL),(9,1,56,NULL,36,'DomainRouter',0,0,39352,0,215),(10,2,2,NULL,43,'DomainRouter',0,0,0,0,222),(11,2,69,NULL,45,'DomainRouter',7181587,786788,0,0,223),(12,2,70,NULL,47,'DomainRouter',0,0,5871078,691554,224);
/*!40000 ALTER TABLE `user_statistics` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_vm`
--

DROP TABLE IF EXISTS `user_vm`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `user_vm` (
  `id` bigint(20) unsigned NOT NULL,
  `iso_id` bigint(20) unsigned default NULL,
  `display_name` varchar(255) default NULL,
  `guest_ip_address` varchar(15) default NULL COMMENT 'ip address within the guest network',
  `guest_mac_address` varchar(17) default NULL COMMENT 'mac address within the guest network',
  `guest_netmask` varchar(15) default NULL COMMENT 'netmask within the guest network',
  `user_data` varchar(2048) default NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`),
  CONSTRAINT `fk_user_vm__id` FOREIGN KEY (`id`) REFERENCES `vm_instance` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `user_vm`
--

LOCK TABLES `user_vm` WRITE;
/*!40000 ALTER TABLE `user_vm` DISABLE KEYS */;
INSERT INTO `user_vm` VALUES (3,NULL,NULL,NULL,NULL,NULL,NULL),(5,NULL,'test',NULL,NULL,NULL,NULL),(7,NULL,'second',NULL,NULL,NULL,NULL),(8,NULL,'third',NULL,NULL,NULL,NULL),(9,NULL,NULL,NULL,NULL,NULL,NULL),(11,NULL,'testing',NULL,NULL,NULL,NULL),(12,NULL,'BD2-User',NULL,NULL,NULL,NULL),(14,NULL,'BD1-User',NULL,NULL,NULL,NULL),(16,NULL,'sds',NULL,NULL,NULL,NULL),(17,NULL,NULL,NULL,NULL,NULL,NULL),(19,NULL,NULL,NULL,NULL,NULL,NULL),(21,NULL,'BDone',NULL,NULL,NULL,NULL),(23,NULL,'BDone-1',NULL,NULL,NULL,NULL),(24,NULL,'BDone-1',NULL,NULL,NULL,NULL),(25,NULL,NULL,NULL,NULL,NULL,NULL),(26,NULL,NULL,NULL,NULL,NULL,NULL),(27,NULL,NULL,NULL,NULL,NULL,NULL),(28,NULL,NULL,NULL,NULL,NULL,NULL),(29,NULL,NULL,NULL,NULL,NULL,NULL),(30,NULL,NULL,NULL,NULL,NULL,NULL),(31,NULL,NULL,NULL,NULL,NULL,NULL),(32,NULL,NULL,NULL,NULL,NULL,NULL),(33,NULL,NULL,NULL,NULL,NULL,NULL),(34,NULL,NULL,NULL,NULL,NULL,NULL),(35,NULL,'Atmos-User',NULL,NULL,NULL,NULL),(37,NULL,'pauluser-VM1',NULL,NULL,NULL,NULL),(38,NULL,'pauluser-VM1',NULL,NULL,NULL,NULL),(39,NULL,'pmorris-VM1',NULL,NULL,NULL,NULL),(42,NULL,'vmforsnap',NULL,NULL,NULL,NULL),(44,NULL,'david term test',NULL,NULL,NULL,NULL),(46,NULL,'Deepa',NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `user_vm` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_vm_details`
--

DROP TABLE IF EXISTS `user_vm_details`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `user_vm_details` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `vm_id` bigint(20) unsigned NOT NULL COMMENT 'vm id',
  `name` varchar(255) NOT NULL,
  `value` varchar(1024) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `fk_user_vm_details__vm_id` (`vm_id`),
  CONSTRAINT `fk_user_vm_details__vm_id` FOREIGN KEY (`vm_id`) REFERENCES `user_vm` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `user_vm_details`
--

LOCK TABLES `user_vm_details` WRITE;
/*!40000 ALTER TABLE `user_vm_details` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_vm_details` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vlan`
--

DROP TABLE IF EXISTS `vlan`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `vlan` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `vlan_id` varchar(255) default NULL,
  `vlan_gateway` varchar(255) default NULL,
  `vlan_netmask` varchar(255) default NULL,
  `description` varchar(255) default NULL,
  `vlan_type` varchar(255) default NULL,
  `data_center_id` bigint(20) unsigned NOT NULL,
  `network_id` bigint(20) unsigned NOT NULL COMMENT 'id of corresponding network offering',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `vlan`
--

LOCK TABLES `vlan` WRITE;
/*!40000 ALTER TABLE `vlan` DISABLE KEYS */;
INSERT INTO `vlan` VALUES (1,'66','66.149.231.1','255.255.255.0','66.149.231.101-66.149.231.120','VirtualNetwork',1,200),(2,'516','172.16.64.1','255.255.252.0','172.16.64.40-172.16.64.49','VirtualNetwork',2,218);
/*!40000 ALTER TABLE `vlan` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vm_instance`
--

DROP TABLE IF EXISTS `vm_instance`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `vm_instance` (
  `id` bigint(20) unsigned NOT NULL,
  `name` varchar(255) NOT NULL,
  `instance_name` varchar(255) NOT NULL COMMENT 'name of the vm instance running on the hosts',
  `state` varchar(32) NOT NULL,
  `vm_template_id` bigint(20) unsigned default NULL,
  `guest_os_id` bigint(20) unsigned NOT NULL,
  `private_mac_address` varchar(17) default NULL,
  `private_ip_address` varchar(15) default NULL,
  `private_netmask` varchar(15) default NULL,
  `pod_id` bigint(20) unsigned default NULL,
  `data_center_id` bigint(20) unsigned NOT NULL COMMENT 'Data Center the instance belongs to',
  `host_id` bigint(20) unsigned default NULL,
  `last_host_id` bigint(20) unsigned default NULL COMMENT 'tentative host for first run or last host that it has been running on',
  `proxy_id` bigint(20) unsigned default NULL COMMENT 'console proxy allocated in previous session',
  `proxy_assign_time` datetime default NULL COMMENT 'time when console proxy was assigned',
  `vnc_password` varchar(255) NOT NULL COMMENT 'vnc password',
  `ha_enabled` tinyint(1) NOT NULL default '0' COMMENT 'Should HA be enabled for this VM',
  `mirrored_vols` tinyint(1) NOT NULL default '0' COMMENT 'Are the volumes mirrored',
  `update_count` bigint(20) unsigned NOT NULL default '0' COMMENT 'date state was updated',
  `update_time` datetime default NULL COMMENT 'date the destroy was requested',
  `created` datetime NOT NULL COMMENT 'date created',
  `removed` datetime default NULL COMMENT 'date removed if not null',
  `type` varchar(32) NOT NULL COMMENT 'type of vm it is',
  `account_id` bigint(20) unsigned NOT NULL COMMENT 'user id of owner',
  `domain_id` bigint(20) unsigned NOT NULL,
  `service_offering_id` bigint(20) unsigned NOT NULL COMMENT 'service offering id',
  `reservation_id` char(40) default NULL COMMENT 'reservation id',
  `hypervisor_type` char(32) default NULL COMMENT 'hypervisor type',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`),
  KEY `i_vm_instance__removed` (`removed`),
  KEY `i_vm_instance__type` (`type`),
  KEY `i_vm_instance__pod_id` (`pod_id`),
  KEY `i_vm_instance__update_time` (`update_time`),
  KEY `i_vm_instance__update_count` (`update_count`),
  KEY `i_vm_instance__state` (`state`),
  KEY `i_vm_instance__data_center_id` (`data_center_id`),
  KEY `fk_vm_instance__host_id` (`host_id`),
  KEY `fk_vm_instance__last_host_id` (`last_host_id`),
  KEY `i_vm_instance__template_id` (`vm_template_id`),
  KEY `i_vm_instance__account_id` (`account_id`),
  KEY `i_vm_instance__service_offering_id` (`service_offering_id`),
  CONSTRAINT `fk_vm_instance__account_id` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`),
  CONSTRAINT `fk_vm_instance__host_id` FOREIGN KEY (`host_id`) REFERENCES `host` (`id`),
  CONSTRAINT `fk_vm_instance__last_host_id` FOREIGN KEY (`last_host_id`) REFERENCES `host` (`id`),
  CONSTRAINT `fk_vm_instance__service_offering_id` FOREIGN KEY (`service_offering_id`) REFERENCES `service_offering` (`id`),
  CONSTRAINT `fk_vm_instance__template_id` FOREIGN KEY (`vm_template_id`) REFERENCES `vm_template` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `vm_instance`
--

LOCK TABLES `vm_instance` WRITE;
/*!40000 ALTER TABLE `vm_instance` DISABLE KEYS */;
INSERT INTO `vm_instance` VALUES (1,'v-1-VM','v-1-VM','Expunging',1,15,NULL,NULL,NULL,1,1,NULL,1,1,'2011-02-10 07:27:23','ffdd8e350c41caa5',0,0,20,'2011-05-06 22:18:53','2011-02-10 07:02:21','2011-05-06 22:18:54','ConsoleProxy',1,1,6,'a1c25aad-487f-4466-b6ee-fd1181b01f77','XenServer'),(2,'s-2-VM','s-2-VM','Expunging',1,15,'06:37:04:00:00:01','192.168.154.233',NULL,1,1,NULL,1,1,'2011-03-01 21:32:40','d78c1455dcafe25f',1,0,20,'2011-05-06 22:19:09','2011-02-10 07:02:21','2011-05-06 22:19:09','SecondaryStorageVm',1,1,7,'9b1756d3-c098-48c7-92aa-41fc1c523fc0','XenServer'),(3,'i-4-3-VM','i-4-3-VM','Expunging',2,11,'02:00:09:85:00:01','10.1.1.242',NULL,1,1,NULL,1,1,'2011-02-10 08:47:37','c35bbef9235a77b',1,0,18,'2011-03-30 23:52:01','2011-02-10 08:43:05','2011-03-30 23:52:03','User',4,4,9,'abec3380-b643-42c8-b8df-5c763af8ca41','XenServer'),(4,'r-4-VM','r-4-VM','Stopped',1,15,'0e:00:a9:fe:03:9c','169.254.3.156',NULL,1,1,NULL,1,NULL,NULL,'70f1bcf40b2729db',1,0,10,'2011-02-15 23:55:27','2011-02-10 08:43:53',NULL,'DomainRouter',4,4,8,NULL,'XenServer'),(5,'i-2-5-VM','i-2-5-VM','Expunging',2,11,'02:00:43:51:00:01','10.1.1.173',NULL,1,1,NULL,1,NULL,NULL,'e873e4d3991f64e5',1,0,7,'2011-02-11 12:47:39','2011-02-10 08:46:42','2011-02-11 12:47:40','User',2,1,9,NULL,'XenServer'),(6,'r-6-VM','r-6-VM','Stopped',1,15,'0e:00:a9:fe:02:90','169.254.2.144',NULL,1,1,NULL,1,1,'2011-02-15 20:50:31','f14f4b226cb167a0',1,0,21,'2011-05-11 22:00:01','2011-02-10 08:46:43',NULL,'DomainRouter',2,1,8,'d35bc661-40a2-4d67-86a8-54ca2cecbe57','XenServer'),(7,'i-4-7-VM','i-4-7-VM','Expunging',2,11,'02:00:37:5e:00:03','10.1.1.208',NULL,1,1,NULL,1,1,'2011-02-10 13:26:57','c9f0ec738d7e69ad',1,0,16,'2011-03-30 23:52:03','2011-02-10 08:47:35','2011-03-30 23:52:05','User',4,4,9,'05fb7288-9f40-4660-bb14-91408ea2ee2c','XenServer'),(8,'i-4-8-VM','i-4-8-VM','Expunging',2,11,'02:00:6a:d6:00:04','10.1.1.85',NULL,1,1,NULL,1,1,'2011-02-10 09:14:23','e70fd753aade51b1',1,0,14,'2011-03-30 23:52:05','2011-02-10 08:48:50','2011-03-30 23:52:06','User',4,4,9,'4b567e72-34e0-4784-93e3-910c17586cfa','XenServer'),(9,'i-6-9-VM','i-6-9-VM','Expunging',202,12,'02:00:20:a7:00:01','10.1.1.239',NULL,1,1,NULL,1,NULL,NULL,'de779a37660c0ad0',1,0,6,'2011-02-10 10:55:01','2011-02-10 10:49:48','2011-02-10 10:55:02','User',6,7,9,NULL,'XenServer'),(10,'r-10-VM','r-10-VM','Stopped',1,15,'0e:00:a9:fe:01:3e','169.254.1.62',NULL,1,1,NULL,1,NULL,NULL,'ad3ddf7c88c8cef8',1,0,5,'2011-02-10 11:42:30','2011-02-10 10:51:24',NULL,'DomainRouter',6,7,8,NULL,'XenServer'),(11,'i-2-11-VM','i-2-11-VM','Expunging',202,12,'02:00:29:d9:00:03','10.1.1.180',NULL,1,1,NULL,1,NULL,NULL,'9f4fa6dc54645de',1,0,7,'2011-02-17 00:19:52','2011-02-15 20:33:45','2011-02-17 00:19:53','User',2,1,9,NULL,'XenServer'),(12,'i-24-12-VM','i-24-12-VM','Expunging',207,12,'02:00:7f:dc:00:01','10.1.1.11',NULL,1,1,NULL,1,1,'2011-02-17 14:53:38','40110317088bcd51',1,0,9,'2011-04-10 23:51:03','2011-02-17 14:49:57','2011-04-10 23:51:03','User',24,22,25,NULL,'XenServer'),(13,'r-13-VM','r-13-VM','Stopped',1,15,'0e:00:a9:fe:02:d8','169.254.2.216',NULL,1,1,NULL,1,NULL,NULL,'cffff4c1534bb34d',1,0,7,'2011-04-09 10:42:11','2011-02-17 14:51:25',NULL,'DomainRouter',24,22,8,NULL,'XenServer'),(14,'i-22-14-VM','i-22-14-VM','Expunging',202,12,'02:00:4f:9e:00:01','10.1.1.253',NULL,1,1,NULL,1,1,'2011-02-17 14:59:20','d0043d6cc05319df',1,0,9,'2011-04-10 23:51:03','2011-02-17 14:53:44','2011-04-10 23:51:04','User',22,21,25,NULL,'XenServer'),(15,'r-15-VM','r-15-VM','Stopped',1,15,'0e:00:a9:fe:01:27','169.254.1.39',NULL,1,1,NULL,1,NULL,NULL,'d9f3b36c1a2ab7d4',1,0,7,'2011-04-09 10:32:01','2011-02-17 14:55:10',NULL,'DomainRouter',22,21,8,NULL,'XenServer'),(16,'i-2-16-VM','i-2-16-VM','Stopped',220,12,'02:00:28:e4:00:04','10.1.1.186',NULL,1,1,NULL,1,1,'2011-02-23 05:49:14','cf0cdfdecf1612a9',1,0,7,'2011-05-11 21:59:31','2011-02-21 04:49:43',NULL,'User',2,1,25,NULL,'XenServer'),(17,'i-26-17-VM','i-26-17-VM','Expunging',202,12,'02:00:37:5f:00:01','10.1.1.146',NULL,1,1,NULL,1,NULL,NULL,'30662d6bec80a95f',1,0,6,'2011-02-23 09:40:37','2011-02-23 09:01:05','2011-02-23 09:40:37','User',26,24,9,NULL,'XenServer'),(18,'r-18-VM','r-18-VM','Expunging',1,15,'0e:00:a9:fe:02:c4','169.254.2.196',NULL,1,1,NULL,1,NULL,NULL,'e86f3a0f639f4670',1,0,6,'2011-02-23 09:40:37','2011-02-23 09:01:06','2011-02-23 09:40:38','DomainRouter',26,24,8,NULL,'XenServer'),(19,'i-30-19-VM','i-30-19-VM','Expunging',202,12,'02:00:63:0c:00:01','10.1.1.222',NULL,1,1,NULL,1,NULL,NULL,'fe99a8c15d17a153',1,0,6,'2011-02-23 10:38:28','2011-02-23 10:20:15','2011-02-23 10:38:28','User',30,27,9,NULL,'XenServer'),(20,'r-20-VM','r-20-VM','Expunging',1,15,'0e:00:a9:fe:03:4e','169.254.3.78',NULL,1,1,NULL,1,NULL,NULL,'23b5ab0415646860',1,0,6,'2011-02-23 10:38:29','2011-02-23 10:20:16','2011-02-23 10:38:29','DomainRouter',30,27,8,NULL,'XenServer'),(21,'i-20-21-VM','i-20-21-VM','Expunging',220,12,'02:00:05:8e:00:01','10.1.1.129',NULL,1,1,NULL,1,1,'2011-03-09 11:14:49','4d40a0d803664048',1,0,8,'2011-04-10 23:51:04','2011-03-09 10:51:45','2011-04-10 23:51:05','User',20,20,25,NULL,'XenServer'),(22,'r-22-VM','r-22-VM','Stopped',1,15,'0e:00:a9:fe:00:62','169.254.0.98',NULL,1,1,NULL,1,NULL,NULL,'43403352c4197930',1,0,6,'2011-04-09 10:42:22','2011-03-09 10:51:46',NULL,'DomainRouter',20,20,8,NULL,'XenServer'),(23,'i-20-23-VM','i-20-23-VM','Expunging',221,12,'02:00:42:d9:00:03','10.1.1.230',NULL,1,1,NULL,1,NULL,NULL,'47ede15cd7619223',1,0,8,'2011-04-10 23:51:05','2011-03-09 11:17:52','2011-04-10 23:51:06','User',20,20,25,NULL,'XenServer'),(24,'i-20-24-VM','i-20-24-VM','Expunging',221,12,NULL,NULL,NULL,NULL,1,NULL,NULL,NULL,NULL,'583189c7a71f4977',1,0,4,'2011-03-10 21:34:06','2011-03-09 11:20:02','2011-03-10 21:34:06','User',20,20,25,'b4959af5-b18d-413f-9eaf-3e389231e390','XenServer'),(25,'i-3-25-VM','i-3-25-VM','Expunging',220,12,NULL,NULL,NULL,NULL,1,NULL,NULL,NULL,NULL,'9eb558664b0910a2',1,0,4,'2011-03-11 23:53:52','2011-03-10 00:28:27','2011-03-11 23:53:52','User',3,3,12,'137de854-8674-4fc8-ba06-ee1c5d9e559b','XenServer'),(26,'i-2-26-VM','i-2-26-VM','Expunging',220,12,NULL,NULL,NULL,NULL,1,NULL,NULL,NULL,NULL,'4fb9b432c4904894',1,0,4,'2011-03-25 23:52:30','2011-03-24 01:41:03','2011-03-25 23:52:30','User',2,1,25,'aa9f51e9-ee55-4462-b35c-e18d7281760b','XenServer'),(27,'i-2-27-VM','i-2-27-VM','Expunging',220,12,NULL,NULL,NULL,NULL,1,NULL,NULL,NULL,NULL,'fa173daa093699a4',1,0,4,'2011-03-25 23:52:30','2011-03-24 01:42:26','2011-03-25 23:52:30','User',2,1,25,'6880a746-4166-41aa-a5ce-f2afbd6a1cd1','XenServer'),(28,'i-42-28-VM','i-42-28-VM','Expunging',220,12,NULL,NULL,NULL,NULL,1,NULL,NULL,NULL,NULL,'2eaa0161dcc62f4e',1,0,5,'2011-03-30 23:52:06','2011-03-29 07:55:28','2011-03-30 23:52:06','User',42,39,9,'d2e870b4-26b9-48f0-81c0-58d1e2d057ad','XenServer'),(29,'i-42-29-VM','i-42-29-VM','Expunging',202,12,NULL,NULL,NULL,NULL,1,NULL,NULL,NULL,NULL,'5da4ac6b64074189',1,0,4,'2011-03-30 23:52:06','2011-03-29 08:19:03','2011-03-30 23:52:06','User',42,39,9,'940e1b9e-dc4a-40e5-a154-7c27b7cef207','XenServer'),(30,'i-51-30-VM','i-51-30-VM','Expunging',202,12,NULL,NULL,NULL,NULL,1,NULL,NULL,NULL,NULL,'4931295038fcbc20',1,0,4,'2011-03-30 23:52:06','2011-03-29 12:19:17','2011-03-30 23:52:07','User',51,45,12,'d27d1654-9905-4f18-b7f3-91943ace3bc6','XenServer'),(31,'i-51-31-VM','i-51-31-VM','Expunging',220,12,NULL,NULL,NULL,NULL,1,NULL,NULL,NULL,NULL,'90985e5907d81387',1,0,4,'2011-03-31 23:52:01','2011-03-30 06:12:02','2011-03-31 23:52:01','User',51,45,9,'ef198add-9a5a-43bf-9931-836c272267b2','XenServer'),(32,'i-2-32-VM','i-2-32-VM','Expunging',220,12,NULL,NULL,NULL,NULL,1,NULL,NULL,NULL,NULL,'d490b165f2e7f97',1,0,5,'2011-04-05 23:51:32','2011-04-04 16:28:22','2011-04-05 23:51:32','User',2,1,25,'1e273303-a8b0-49b2-a7c5-fe2a7319b992','XenServer'),(33,'i-20-33-VM','i-20-33-VM','Expunging',220,12,NULL,NULL,NULL,NULL,1,NULL,NULL,NULL,NULL,'b59bed08337cf9ed',1,0,5,'2011-04-05 23:51:32','2011-04-04 20:09:43','2011-04-05 23:51:32','User',20,20,25,'1c81e568-89ab-4a65-a98c-07a3e929c6bb','XenServer'),(34,'i-20-34-VM','i-20-34-VM','Expunging',220,12,NULL,NULL,NULL,NULL,1,NULL,NULL,NULL,NULL,'a69fcccc6995bc69',1,0,5,'2011-04-05 23:51:32','2011-04-04 20:11:27','2011-04-05 23:51:32','User',20,20,25,'9e94d5e3-fd99-4dd9-a05f-27809ea7e493','XenServer'),(35,'i-56-35-VM','i-56-35-VM','Stopped',202,12,'02:00:61:c6:00:01','10.1.1.79',NULL,1,1,NULL,1,1,'2011-04-09 10:28:08','ea97973638583170',1,0,5,'2011-05-11 21:59:31','2011-04-09 10:26:38',NULL,'User',56,48,9,NULL,'XenServer'),(36,'r-36-VM','r-36-VM','Stopped',1,15,'0e:00:a9:fe:02:ed','169.254.2.237',NULL,1,1,NULL,1,NULL,NULL,'abbb098e0d0e9809',1,0,7,'2011-05-11 21:59:32','2011-04-09 10:26:50',NULL,'DomainRouter',56,48,8,'c0138da0-9831-491b-9dcd-cee60fffb6ff','XenServer'),(37,'i-61-37-VM','i-61-37-VM','Expunging',221,12,NULL,NULL,NULL,NULL,1,NULL,NULL,NULL,NULL,'bf1aa7476e56ddc4',1,0,4,'2011-04-26 17:44:54','2011-04-25 12:53:30','2011-04-26 17:44:54','User',61,52,9,'c93e078d-3966-4f6e-99d9-04588d5ee4ec','XenServer'),(38,'i-61-38-VM','i-61-38-VM','Expunging',202,12,NULL,NULL,NULL,NULL,1,NULL,NULL,NULL,NULL,'fbfd7ba7bf1bde19',1,0,4,'2011-04-26 17:44:54','2011-04-25 12:54:45','2011-04-26 17:44:54','User',61,52,9,'b26cf03a-a34d-48b7-8856-8b5908b244f8','XenServer'),(39,'i-60-39-VM','i-60-39-VM','Expunging',202,12,NULL,NULL,NULL,NULL,1,NULL,NULL,NULL,NULL,'3efc74294211bf84',1,0,4,'2011-04-26 17:44:54','2011-04-25 12:57:07','2011-04-26 17:44:54','User',60,52,9,'d2d9567d-a9f3-4b9a-88fa-66ec301c51bc','XenServer'),(40,'v-40-VM','v-40-VM','Running',1,15,'06:22:d6:00:00:03','192.168.181.237',NULL,2,2,4,4,NULL,NULL,'be4361bbcc8fcc46',0,0,58,'2011-05-16 20:59:42','2011-05-06 22:06:57',NULL,'ConsoleProxy',1,1,6,'c4dac56f-e6d8-48b7-b83d-29162b48bc33','XenServer'),(41,'s-41-VM','s-41-VM','Running',1,15,'06:8e:64:00:00:04','192.168.181.238',NULL,2,2,4,4,NULL,NULL,'4974a28c0ad36e13',1,0,23,'2011-05-16 20:59:42','2011-05-06 22:06:57',NULL,'SecondaryStorageVm',1,1,7,'7400955e-f6c3-4482-b521-d2ac66ef7e99','XenServer'),(42,'i-2-42-VM','i-2-42-VM','Stopped',2,11,'02:00:68:76:00:01','10.1.3.145',NULL,2,2,NULL,4,40,'2011-05-11 21:46:14','174f6439bb92c6a0',1,0,5,'2011-05-11 21:46:25','2011-05-11 21:43:38',NULL,'User',2,1,25,NULL,'XenServer'),(43,'r-43-VM','r-43-VM','Stopped',1,15,'0e:00:a9:fe:01:1c','169.254.1.28',NULL,2,2,NULL,4,NULL,NULL,'4ea4da382d0b7d6f',1,0,5,'2011-05-11 22:10:04','2011-05-11 21:44:57',NULL,'DomainRouter',2,1,8,NULL,'XenServer'),(44,'i-69-44-VM','i-69-44-VM','Expunging',228,12,'02:00:40:8f:00:01','10.1.3.180',NULL,2,2,NULL,4,NULL,NULL,'6c20beb1c3458d75',1,0,6,'2011-05-16 17:52:45','2011-05-12 00:20:31','2011-05-16 17:52:45','User',69,59,9,NULL,'XenServer'),(45,'r-45-VM','r-45-VM','Expunging',1,15,'0e:00:a9:fe:01:4f','169.254.1.79',NULL,2,2,NULL,4,NULL,NULL,'7d10cabb0a000153',1,0,6,'2011-05-16 17:52:55','2011-05-12 00:21:42','2011-05-16 17:52:55','DomainRouter',69,59,8,NULL,'XenServer'),(46,'i-70-46-VM','i-70-46-VM','Running',228,12,'02:00:22:d4:00:01','10.1.3.47',NULL,2,2,4,4,NULL,NULL,'9e1497fd55e10d0b',1,0,4,'2011-05-16 20:59:42','2011-05-12 18:48:49',NULL,'User',70,60,9,'2f2ad287-f40f-4089-bd34-009f41ed51d8','XenServer'),(47,'r-47-VM','r-47-VM','Running',1,15,'0e:00:a9:fe:00:19','169.254.0.25',NULL,2,2,4,4,NULL,NULL,'661d70ce972adeaa',1,0,4,'2011-05-16 20:59:42','2011-05-12 18:48:50',NULL,'DomainRouter',70,60,8,'30ff8edd-f496-431c-94e3-5ffc8901cbc6','XenServer');
/*!40000 ALTER TABLE `vm_instance` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vm_template`
--

DROP TABLE IF EXISTS `vm_template`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `vm_template` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `unique_name` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `public` int(1) unsigned NOT NULL,
  `featured` int(1) unsigned NOT NULL,
  `type` varchar(32) default NULL,
  `hvm` int(1) unsigned NOT NULL COMMENT 'requires HVM',
  `bits` int(6) unsigned NOT NULL COMMENT '32 bit or 64 bit',
  `url` varchar(255) default NULL COMMENT 'the url where the template exists externally',
  `format` varchar(32) NOT NULL COMMENT 'format for the template',
  `created` datetime NOT NULL COMMENT 'Date created',
  `removed` datetime default NULL COMMENT 'Date removed if not null',
  `account_id` bigint(20) unsigned NOT NULL COMMENT 'id of the account that created this template',
  `checksum` varchar(255) default NULL COMMENT 'checksum for the template root disk',
  `display_text` varchar(4096) default NULL COMMENT 'Description text set by the admin for display purpose only',
  `enable_password` int(1) unsigned NOT NULL default '1' COMMENT 'true if this template supports password reset',
  `guest_os_id` bigint(20) unsigned NOT NULL COMMENT 'the OS of the template',
  `bootable` int(1) unsigned NOT NULL default '1' COMMENT 'true if this template represents a bootable ISO',
  `prepopulate` int(1) unsigned NOT NULL default '0' COMMENT 'prepopulate this template to primary storage',
  `cross_zones` int(1) unsigned NOT NULL default '0' COMMENT 'Make this template available in all zones',
  `extractable` int(1) unsigned NOT NULL default '0' COMMENT 'Is this template extractable',
  `hypervisor_type` varchar(32) default NULL COMMENT 'hypervisor that the template is belonged to',
  PRIMARY KEY  (`id`),
  KEY `i_vm_template__removed` (`removed`),
  KEY `i_vm_template__public` (`public`)
) ENGINE=InnoDB AUTO_INCREMENT=248 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `vm_template`
--

LOCK TABLES `vm_template` WRITE;
/*!40000 ALTER TABLE `vm_template` DISABLE KEYS */;
INSERT INTO `vm_template` VALUES (1,'routing-1','SystemVM Template (XenServer)',0,0,'SYSTEM',0,64,'http://download.cloud.com/releases/2.2.0/systemvm.vhd.bz2','VHD','2011-02-10 04:58:22',NULL,1,'c33dfaf0937b35c25ef6a0fdd98f24d3','SystemVM Template (XenServer)',0,15,1,0,1,0,'XenServer'),(2,'centos53-x86_64','CentOS 5.3(64-bit) no GUI (XenServer)',0,0,'BUILTIN',0,64,'http://download.cloud.com/templates/builtin/f59f18fb-ae94-4f97-afd2-f84755767aca.vhd.bz2','VHD','2011-02-10 04:58:22','2011-05-11 21:46:51',1,'b63d854a9560c013142567bbae8d98cf','CentOS 5.3(64-bit) no GUI (XenServer)',0,11,1,0,1,1,'XenServer'),(3,'routing-3','SystemVM Template (KVM)',0,0,'SYSTEM',0,64,'http://download.cloud.com/releases/2.2.0/systemvm.qcow2.bz2','QCOW2','2011-02-10 04:58:22',NULL,1,'ec463e677054f280f152fcc264255d2f','SystemVM Template (KVM)',0,15,1,0,1,0,'KVM'),(4,'centos55-x86_64','CentOS 5.5(64-bit) no GUI (KVM)',1,1,'BUILTIN',0,64,'http://download.cloud.com/templates/builtin/eec2209b-9875-3c8d-92be-c001bd8a0faf.qcow2.bz2','QCOW2','2011-02-10 04:58:22','2011-05-11 21:47:00',1,'1da20ae69b54f761f3f733dce97adcc0','CentOS 5.5(64-bit) no GUI (KVM)',0,112,1,0,1,1,'KVM'),(7,'centos53-x64','CentOS 5.3(64-bit) no GUI (vSphere)',1,1,'BUILTIN',0,64,'http://download.cloud.com/releases/2.2.0/CentOS5.3-x86_64.ova','OVA','2011-02-10 04:58:22','2011-05-11 21:48:30',1,'f6f881b7f2292948d8494db837fe0f47','CentOS 5.3(64-bit) no GUI (vSphere)',0,12,1,0,1,1,'VMware'),(8,'routing-8','SystemVM Template (vSphere)',0,0,'SYSTEM',0,32,'http://download.cloud.com/releases/2.2.0/systemvm.ova','OVA','2011-02-10 04:58:22',NULL,1,'3c9d4c704af44ebd1736e1bc78cec1fa','SystemVM Template (vSphere)',0,15,1,0,1,0,'VMware'),(200,'xs-tools.iso','xs-tools.iso',1,1,'PERHOST',1,64,NULL,'ISO','2011-02-10 05:28:51',NULL,1,NULL,'xen-pv-drv-iso',0,1,0,0,0,1,'None'),(201,'vmware-tools.iso','vmware-tools.iso',1,1,'PERHOST',1,64,NULL,'ISO','2011-02-10 05:28:51',NULL,1,NULL,'VMware Tools Installer ISO',0,1,0,0,0,1,'VMware'),(202,'713ee3ee-a271-43b3-bf90-b6f74e848e93','CentOS55 - Xen',1,1,'USER',0,64,NULL,'VHD','2011-02-10 09:07:39','2011-05-11 21:39:47',2,NULL,'CentOS55 - Xen',0,12,1,0,0,1,'XenServer'),(203,'ffa8ae43-ff4e-4514-b6b2-c18161dfaaec','CentOS55LAMP - Xen',1,1,'USER',0,64,NULL,'VHD','2011-02-10 09:10:39','2011-05-11 21:39:51',2,NULL,'CentOS55LAMP - Xen',0,12,1,0,0,1,'XenServer'),(204,'7b82d6c6-c612-4468-a6a2-f33c5e9ab3da','CentOS55LAMP_BP - Xen',1,1,'USER',0,64,NULL,'VHD','2011-02-10 09:13:39','2011-05-11 21:38:47',2,NULL,'CentOS55LAMP_BP - Xen',0,12,1,0,0,1,'XenServer'),(205,'f4524131-3c07-483d-990a-855847d1c923','CentOS55MySQL - Xen',1,1,'USER',0,64,NULL,'VHD','2011-02-10 09:16:39','2011-05-11 21:39:10',2,NULL,'CentOS55MySQL - Xen',0,12,1,0,0,1,'XenServer'),(206,'6d295900-70c6-454e-bf64-6b0de3629d73','CentOS55MySQL_BP - Xen',1,1,'USER',0,64,NULL,'VHD','2011-02-10 09:19:39','2011-05-11 21:39:31',2,NULL,'CentOS55MySQL_BP - Xen',0,12,1,0,0,1,'XenServer'),(207,'aa919eac-76ae-4b5b-b37e-3cdb86ca8d3c','RHEL55 - Xen',1,1,'USER',0,64,NULL,'VHD','2011-02-10 09:22:39','2011-05-11 21:39:40',2,NULL,'RHEL55 - Xen',0,12,1,0,0,1,'XenServer'),(208,'524f698a-c354-4e87-b361-cd24f48c92d0','RHEL55LAMP_BP - Xen',1,1,'USER',0,64,NULL,'VHD','2011-02-10 09:25:39','2011-05-11 21:40:55',2,NULL,'RHEL55LAMP_BP - Xen',0,12,1,0,0,1,'XenServer'),(209,'d06a2fdf-5358-41e0-8222-180146d46acc','Ubuntu1010 - Xen',1,1,'USER',0,64,NULL,'VHD','2011-02-10 09:28:40','2011-05-11 21:41:00',2,NULL,'Ubuntu1010 - Xen',0,12,1,0,0,1,'XenServer'),(210,'22643546-22dd-428a-ae33-d56d6cc21c54','Windows2003SP2_32bit - Xen',1,1,'USER',0,64,NULL,'VHD','2011-02-10 09:31:40','2011-05-11 21:41:11',2,NULL,'Windows2003SP2_32bit - Xen',0,12,1,0,0,1,'XenServer'),(211,'bd4c1f37-9bbe-4cd7-bc29-599342813a40','Windows2008R2 - Xen',1,1,'USER',0,64,NULL,'VHD','2011-02-10 09:34:40','2011-05-11 21:41:06',2,NULL,'Windows2008R2 - Xen',0,12,1,0,0,1,'XenServer'),(212,'1366c2ac-3739-4694-b4aa-0bd58998f2b8','Windows2008R2_32bit - Xen',1,1,'USER',0,64,NULL,'VHD','2011-02-10 09:37:40','2011-05-11 21:40:34',2,NULL,'Windows2008R2_32bit - Xen',0,12,1,0,0,1,'XenServer'),(213,'7f015f07-5d31-48bf-83ad-c37c07d70f15','Windows2008R2_BP - Xen',1,1,'USER',0,64,NULL,'VHD','2011-02-10 09:40:40','2011-05-11 21:40:45',2,NULL,'Windows2008R2_BP - Xen',0,12,1,0,0,1,'XenServer'),(214,'066e2c5a-e3c1-40e4-be52-b7291e36ad40','Windows2008R2_IIS - Xen',1,1,'USER',0,64,NULL,'VHD','2011-02-10 09:43:40','2011-05-11 21:40:51',2,NULL,'Windows2008R2_IIS - Xen',0,12,1,0,0,1,'XenServer'),(215,'926a34df-42da-4907-aafa-2e513444b4ac','Windows2008R2_IIS_BP - Xen',1,1,'USER',0,64,NULL,'VHD','2011-02-10 09:46:40','2011-05-11 21:40:40',2,NULL,'Windows2008R2_IIS_BP - Xen',0,12,1,0,0,1,'XenServer'),(216,'6dc09313-b91e-418c-86d1-728b3d8c9599','Windows2008R2_SQL2008 - Xen',1,1,'USER',0,64,NULL,'VHD','2011-02-10 09:49:40','2011-05-11 21:40:19',2,NULL,'Windows2008R2_SQL2008 - Xen',0,12,1,0,0,1,'XenServer'),(217,'a52b641d-3c56-4fbf-ad2a-c8f2f25adbd7','Windows2008R2_SQL2008_BP - Xen',1,1,'USER',0,64,NULL,'VHD','2011-02-10 09:52:41','2011-05-11 21:40:13',2,NULL,'Windows2008R2_SQL2008_BP - Xen',0,12,1,0,0,1,'XenServer'),(218,'1ddf46ef-9236-4a00-a654-5abd61afd42c','Windows2008R2_WISA - Xen',1,1,'USER',0,64,NULL,'VHD','2011-02-10 09:55:41','2011-05-11 21:40:29',2,NULL,'Windows2008R2_WISA - Xen',0,12,1,0,0,1,'XenServer'),(219,'a93feeb2-ef2f-449c-89c3-4e08492d9bba','Windows2008R2_WISA_BP - Xen',1,1,'USER',0,64,NULL,'VHD','2011-02-10 09:58:41','2011-05-11 21:40:25',2,NULL,'Windows2008R2_WISA_BP - Xen',0,12,1,0,0,1,'XenServer'),(220,'085a434d-fc47-4194-84a6-a95937fa7ab9','Windows7SP1 - Xen',1,1,'USER',0,64,NULL,'VHD','2011-02-10 10:01:41','2011-05-11 21:40:04',2,NULL,'Windows7SP1 - Xen',0,12,1,0,0,1,'XenServer'),(221,'b9c82eff-2179-4ae4-b93a-8f01f50b1bfb','WindowsXPSP3 - Xen',1,1,'USER',0,64,NULL,'VHD','2011-02-10 10:04:41','2011-05-11 21:39:57',2,NULL,'WindowsXPSP3 - Xen',0,12,1,0,0,1,'XenServer'),(222,'222-20-ae3f6d85-c658-369b-8a34-afa547459c38','BD1-Ubuntu 10.10 server 64bit',0,0,'USER',1,64,'http://nfs1.lab.vmops.com/isos_64bit/ubuntu-10.10-server-amd64.iso','ISO','2011-03-01 21:30:43',NULL,20,NULL,'Ubuntu 10.10 server 64bit',0,126,1,0,0,1,'None'),(223,'223-20-09d5124d-9362-3b3c-a340-b43a93ee7f34','XXX-Open Filer',1,0,'USER',1,64,'http://sourceforge.net/projects/openfiler/files/openfiler-distribution-iso-x86/openfiler-2.3-x86-disc1.iso','ISO','2011-03-01 21:52:55',NULL,20,NULL,'Open Filer',0,111,1,0,0,1,'None'),(224,'224-2-2a8aabf2-d3c9-38d5-91ee-ad933ec95bdc','BD1-Bee Dee Test',1,1,'USER',1,64,'http://sourceforge.net/projects/openfiler/files/openfiler-distribution-iso-x86/openfiler-2.3-x86-disc1.iso','ISO','2011-03-01 22:35:10',NULL,2,NULL,'Bee Dee Test',0,60,1,0,0,1,'None'),(225,'225-2-6eda8113-3669-39cf-a668-e85d36788cbc','testme',0,0,'USER',1,64,'http://nfs1.lab.vmops.com/test%20space.iso','ISO','2011-03-02 04:22:17','2011-03-02 04:22:38',2,NULL,'test',0,69,1,0,0,1,'None'),(226,'226-42-7990b361-af39-3836-b037-1e5dd1b15803','dads',0,0,'USER',1,64,'http://10.91.28.6/templates/centos53-x86_64/latest/f59f18fb-ae94-4f97-afd2-f84755767aca.vhd.bz2','VHD','2011-03-30 08:15:56','2011-05-11 21:48:02',42,NULL,'dsaf',0,12,1,0,0,0,'XenServer'),(227,'227-2-90319708-9166-3f22-ad2b-df6411737f33','CentOS 5.3 64-bit',0,0,'USER',1,64,'http://nfs1.lab.vmops.com/templates/centos53-x86_64/latest/f59f18fb-ae94-4f97-afd2-f84755767aca.vhd.bz2','VHD','2011-05-11 21:38:30','2011-05-11 21:47:55',2,NULL,'CentOS 5.3 64-bit',0,12,1,0,0,0,'XenServer'),(228,'87307f5e-24b8-4abc-8fb2-4c4afd71522b','CentOS55 - Xen',1,1,'USER',0,64,NULL,'VHD','2011-05-11 21:58:34',NULL,2,NULL,'CentOS55 - Xen',0,12,1,0,0,1,'XenServer'),(229,'e03153da-a1df-44d5-994d-a4e552a2ff10','CentOS55LAMP - Xen',1,1,'USER',0,64,NULL,'VHD','2011-05-11 22:01:35',NULL,2,NULL,'CentOS55LAMP - Xen',0,12,1,0,0,1,'XenServer'),(230,'1c41ac18-013c-4765-90ae-21ed0392b86e','CentOS55LAMP_BP - Xen',1,1,'USER',0,64,NULL,'VHD','2011-05-11 22:04:35',NULL,2,NULL,'CentOS55LAMP_BP - Xen',0,12,1,0,0,1,'XenServer'),(231,'1518b62ef89-caef-345b-be79-41b4fe93d71e','CentOS55MySQL - Xen',1,1,'USER',0,64,NULL,'RAW','2011-05-11 22:07:35',NULL,2,NULL,'CentOS55MySQL - Xen',0,12,1,0,0,1,'XenServer'),(232,'a5a975d4-de25-4b25-a6e2-d25c195cb66a','CentOS55MySQL_BP - Xen',1,1,'USER',0,64,NULL,'VHD','2011-05-11 22:10:35',NULL,2,NULL,'CentOS55MySQL_BP - Xen',0,12,1,0,0,1,'XenServer'),(233,'151fa98806b-eda2-3adc-adc6-c2418587e916','RHEL55 - Xen',1,1,'USER',0,64,NULL,'RAW','2011-05-11 22:13:36',NULL,2,NULL,'RHEL55 - Xen',0,12,1,0,0,1,'XenServer'),(234,'becc4fd5-20d9-4809-9601-4017e17de53a','RHEL55LAMP_BP - Xen',1,1,'USER',0,64,NULL,'VHD','2011-05-11 22:16:36',NULL,2,NULL,'RHEL55LAMP_BP - Xen',0,12,1,0,0,1,'XenServer'),(235,'15148beb4c8-b11c-3fdd-81db-ba7ba5aaeac8','Ubuntu1010 - Xen',1,1,'USER',0,64,NULL,'RAW','2011-05-11 22:19:36',NULL,2,NULL,'Ubuntu1010 - Xen',0,12,1,0,0,1,'XenServer'),(236,'d3a53a14-14b9-4c14-8b8c-529c9320290a','Windows2003SP2_32bit - Xen',1,1,'USER',0,64,NULL,'VHD','2011-05-11 22:22:37',NULL,2,NULL,'Windows2003SP2_32bit - Xen',0,12,1,0,0,1,'XenServer'),(237,'300705ea-e933-46fb-93f9-860b040f2da9','Windows2008R2 - Xen',1,1,'USER',0,64,NULL,'VHD','2011-05-11 22:25:37',NULL,2,NULL,'Windows2008R2 - Xen',0,12,1,0,0,1,'XenServer'),(238,'288e2597-7d5f-4966-9b99-0ba22b59cb07','Windows2008R2_32bit - Xen',1,1,'USER',0,64,NULL,'VHD','2011-05-11 22:28:37',NULL,2,NULL,'Windows2008R2_32bit - Xen',0,12,1,0,0,1,'XenServer'),(239,'e0ca8ca9-2368-4195-beda-50e0d967309f','Windows2008R2_BP - Xen',1,1,'USER',0,64,NULL,'VHD','2011-05-11 22:31:37',NULL,2,NULL,'Windows2008R2_BP - Xen',0,12,1,0,0,1,'XenServer'),(240,'13d6e1dc-9c63-428e-adc6-af812ae032a5','Windows2008R2_IIS - Xen',1,1,'USER',0,64,NULL,'VHD','2011-05-11 22:34:37',NULL,2,NULL,'Windows2008R2_IIS - Xen',0,12,1,0,0,1,'XenServer'),(241,'31ca58bc-e96f-4c0d-bfd3-4546e84a86f2','Windows2008R2_IIS_BP - Xen',1,1,'USER',0,64,NULL,'VHD','2011-05-11 22:37:37',NULL,2,NULL,'Windows2008R2_IIS_BP - Xen',0,12,1,0,0,1,'XenServer'),(242,'63e903dd-8a41-4d7d-a5c8-eeaa69ebedba','Windows2008R2_SQL2008 - Xen',1,1,'USER',0,64,NULL,'VHD','2011-05-11 22:40:37',NULL,2,NULL,'Windows2008R2_SQL2008 - Xen',0,12,1,0,0,1,'XenServer'),(243,'151b7f77b7d-5582-30b9-b77f-a500b0ca1740','Windows2008R2_SQL2008_BP - Xen',1,1,'USER',0,64,NULL,'RAW','2011-05-11 22:43:38',NULL,2,NULL,'Windows2008R2_SQL2008_BP - Xen',0,12,1,0,0,1,'XenServer'),(244,'9413f508-5699-4695-842d-ccecc49498f4','Windows2008R2_WISA - Xen',1,1,'USER',0,64,NULL,'VHD','2011-05-11 22:46:38',NULL,2,NULL,'Windows2008R2_WISA - Xen',0,12,1,0,0,1,'XenServer'),(245,'37e3ea3e-ae8a-4575-b59c-ae2c81705b67','Windows2008R2_WISA_BP - Xen',1,1,'USER',0,64,NULL,'VHD','2011-05-11 22:49:38',NULL,2,NULL,'Windows2008R2_WISA_BP - Xen',0,12,1,0,0,1,'XenServer'),(246,'1518e4cd692-454c-3aa4-a5ba-0dac8e15bca2','Windows7SP1 - Xen',1,1,'USER',0,64,NULL,'RAW','2011-05-11 22:52:38',NULL,2,NULL,'Windows7SP1 - Xen',0,12,1,0,0,1,'XenServer'),(247,'2a1b6663-7d72-443e-ae4a-58138a33edf2','WindowsXPSP3 - Xen',1,1,'USER',0,64,NULL,'VHD','2011-05-11 22:55:38',NULL,2,NULL,'WindowsXPSP3 - Xen',0,12,1,0,0,1,'XenServer');
/*!40000 ALTER TABLE `vm_template` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `volumes`
--

DROP TABLE IF EXISTS `volumes`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `volumes` (
  `id` bigint(20) unsigned NOT NULL auto_increment COMMENT 'Primary Key',
  `account_id` bigint(20) unsigned NOT NULL COMMENT 'owner.  foreign key to account table',
  `domain_id` bigint(20) unsigned NOT NULL COMMENT 'the domain that the owner belongs to',
  `pool_id` bigint(20) unsigned default NULL COMMENT 'pool it belongs to. foreign key to storage_pool table',
  `instance_id` bigint(20) unsigned default NULL COMMENT 'vm instance it belongs to. foreign key to vm_instance table',
  `device_id` bigint(20) unsigned default NULL COMMENT 'which device inside vm instance it is ',
  `name` varchar(255) default NULL COMMENT 'A user specified name for the volume',
  `size` bigint(20) unsigned NOT NULL COMMENT 'total size',
  `folder` varchar(255) default NULL COMMENT 'The folder where the volume is saved',
  `path` varchar(255) default NULL COMMENT 'Path',
  `pod_id` bigint(20) unsigned default NULL COMMENT 'pod this volume belongs to',
  `data_center_id` bigint(20) unsigned NOT NULL COMMENT 'data center this volume belongs to',
  `iscsi_name` varchar(255) default NULL COMMENT 'iscsi target name',
  `host_ip` varchar(15) default NULL COMMENT 'host ip address for convenience',
  `volume_type` varchar(64) NOT NULL COMMENT 'root, swap or data',
  `resource_type` varchar(64) default NULL COMMENT 'pool-based or host-based',
  `pool_type` varchar(64) default NULL COMMENT 'type of the pool',
  `disk_offering_id` bigint(20) unsigned NOT NULL COMMENT 'can be null for system VMs',
  `template_id` bigint(20) unsigned default NULL COMMENT 'fk to vm_template.id',
  `first_snapshot_backup_uuid` varchar(255) default NULL COMMENT 'The first snapshot that was ever taken for this volume',
  `recreatable` tinyint(1) unsigned NOT NULL default '0' COMMENT 'Is this volume recreatable?',
  `created` datetime default NULL COMMENT 'Date Created',
  `attached` datetime default NULL COMMENT 'Date Attached',
  `updated` datetime default NULL COMMENT 'Date updated for attach/detach',
  `removed` datetime default NULL COMMENT 'Date removed.  not null if removed',
  `status` varchar(32) default NULL COMMENT 'Async API volume creation status',
  `state` varchar(32) default NULL COMMENT 'State machine',
  `source_id` bigint(20) unsigned default NULL COMMENT 'id for the source',
  `source_type` varchar(32) default NULL COMMENT 'source from which the volume is created -- snapshot, diskoffering, template, blank',
  `chain_info` text COMMENT 'save possible disk chain info in primary storage',
  PRIMARY KEY  (`id`),
  KEY `i_volumes__removed` (`removed`),
  KEY `i_volumes__pod_id` (`pod_id`),
  KEY `i_volumes__data_center_id` (`data_center_id`),
  KEY `i_volumes__account_id` (`account_id`),
  KEY `i_volumes__pool_id` (`pool_id`),
  KEY `i_volumes__instance_id` (`instance_id`),
  CONSTRAINT `fk_volumes__account_id` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`),
  CONSTRAINT `fk_volumes__instance_id` FOREIGN KEY (`instance_id`) REFERENCES `vm_instance` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_volumes__pool_id` FOREIGN KEY (`pool_id`) REFERENCES `storage_pool` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=58 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `volumes`
--

LOCK TABLES `volumes` WRITE;
/*!40000 ALTER TABLE `volumes` DISABLE KEYS */;
INSERT INTO `volumes` VALUES (1,1,1,200,2,0,'ROOT-2',2097152000,'/export/home/david/ezh-sit-primary1','254a5b3b-a69e-4cd2-9b7c-29c8ea12729d',1,1,NULL,NULL,'ROOT',NULL,'NetworkFilesystem',7,1,NULL,1,'2011-02-10 07:02:21',NULL,NULL,NULL,'Created','Destroy',NULL,NULL,NULL),(2,1,1,200,1,0,'ROOT-1',2097152000,'/export/home/david/ezh-sit-primary1','08573011-8dfa-4846-91cd-d06b6a778eca',1,1,NULL,NULL,'ROOT',NULL,'NetworkFilesystem',6,1,NULL,1,'2011-02-10 07:02:22',NULL,NULL,NULL,'Created','Destroy',NULL,NULL,NULL),(3,4,4,200,3,0,'ROOT-3',8589934592,'/export/home/david/ezh-sit-primary1','300cdecb-f025-4c6f-a72d-77f3871e24f4',1,1,NULL,NULL,'ROOT',NULL,'NetworkFilesystem',9,2,NULL,1,'2011-02-10 08:43:05',NULL,NULL,'2011-03-30 23:52:02','Created','Destroy',NULL,NULL,NULL),(4,4,4,200,NULL,NULL,'DATA-3',5368709120,'/export/home/david/ezh-sit-primary1','46ec3afe-31ce-477e-bf48-165568652fba',1,1,NULL,NULL,'DATADISK',NULL,'NetworkFilesystem',13,NULL,NULL,1,'2011-02-10 08:43:05',NULL,'2011-03-30 23:52:01',NULL,'Created','Ready',NULL,NULL,NULL),(5,4,4,200,4,0,'ROOT-4',2097152000,'/export/home/david/ezh-sit-primary1','b2a960de-b072-4a8f-a425-357863e76879',1,1,NULL,NULL,'ROOT',NULL,'NetworkFilesystem',8,1,NULL,1,'2011-02-10 08:43:53',NULL,NULL,NULL,'Created','Ready',NULL,NULL,NULL),(6,2,1,200,5,0,'ROOT-5',8589934592,'/export/home/david/ezh-sit-primary1','eedeced2-6c45-4cfd-878a-f5be266bd414',1,1,NULL,NULL,'ROOT',NULL,'NetworkFilesystem',9,2,NULL,1,'2011-02-10 08:46:42',NULL,NULL,'2011-02-11 12:47:40','Created','Destroy',NULL,NULL,NULL),(7,2,1,200,6,0,'ROOT-6',2097152000,'/export/home/david/ezh-sit-primary1','c68aa785-f4b1-4cc1-8e6e-d6342ce8b086',1,1,NULL,NULL,'ROOT',NULL,'NetworkFilesystem',8,1,NULL,1,'2011-02-10 08:46:43',NULL,NULL,NULL,'Created','Ready',NULL,NULL,NULL),(8,4,4,200,7,0,'ROOT-7',8589934592,'/export/home/david/ezh-sit-primary1','3ee2977d-b9a9-4903-94c8-8c514cb0beb8',1,1,NULL,NULL,'ROOT',NULL,'NetworkFilesystem',9,2,NULL,1,'2011-02-10 08:47:36',NULL,NULL,'2011-03-30 23:52:05','Created','Destroy',NULL,NULL,NULL),(9,4,4,200,8,0,'ROOT-8',8589934592,'/export/home/david/ezh-sit-primary1','b6a82291-8930-4230-9b70-ed316199cd65',1,1,NULL,NULL,'ROOT',NULL,'NetworkFilesystem',9,2,NULL,1,'2011-02-10 08:48:50',NULL,NULL,'2011-03-30 23:52:06','Created','Destroy',NULL,NULL,NULL),(10,6,7,200,9,0,'ROOT-9',8589934592,'/export/home/david/ezh-sit-primary1','99ef4a93-7083-4e40-abb0-22bef54fdd42',1,1,NULL,NULL,'ROOT',NULL,'NetworkFilesystem',9,202,NULL,1,'2011-02-10 10:49:48',NULL,NULL,'2011-02-10 10:55:02','Created','Destroy',NULL,NULL,NULL),(11,6,7,200,10,0,'ROOT-10',2097152000,'/export/home/david/ezh-sit-primary1','02c9aa25-fd15-4b27-9649-800cbe403070',1,1,NULL,NULL,'ROOT',NULL,'NetworkFilesystem',8,1,NULL,1,'2011-02-10 10:51:24',NULL,NULL,NULL,'Created','Ready',NULL,NULL,NULL),(12,2,1,200,11,0,'ROOT-11',8589934592,'/export/home/david/ezh-sit-primary1','1d8b7763-c48f-472a-aa4b-7e2a52101fe7',1,1,NULL,NULL,'ROOT',NULL,'NetworkFilesystem',9,202,NULL,1,'2011-02-15 20:33:45',NULL,NULL,'2011-02-17 00:19:53','Created','Destroy',NULL,NULL,NULL),(13,24,22,200,12,0,'ROOT-12',8589934592,'/export/home/david/ezh-sit-primary1','267d6dc4-abc1-4560-bdfc-4bbd809ea11d',1,1,NULL,NULL,'ROOT',NULL,'NetworkFilesystem',25,207,NULL,1,'2011-02-17 14:49:57',NULL,NULL,'2011-04-10 23:51:03','Created','Destroy',NULL,NULL,NULL),(14,24,22,200,NULL,NULL,'DATA-12',5368709120,'/export/home/david/ezh-sit-primary1','39b00c38-049b-458d-8174-86ff3f7bbcc4',1,1,NULL,NULL,'DATADISK',NULL,'NetworkFilesystem',26,NULL,NULL,1,'2011-02-17 14:49:57',NULL,'2011-04-10 23:51:03',NULL,'Created','Ready',NULL,NULL,NULL),(15,24,22,200,13,0,'ROOT-13',2097152000,'/export/home/david/ezh-sit-primary1','c1e4ad13-efe3-47cc-b3dd-e1e5361bc4ed',1,1,NULL,NULL,'ROOT',NULL,'NetworkFilesystem',8,1,NULL,1,'2011-02-17 14:51:25',NULL,NULL,NULL,'Created','Ready',NULL,NULL,NULL),(16,22,21,200,14,0,'ROOT-14',8589934592,'/export/home/david/ezh-sit-primary1','b0d0f448-0d4a-4e66-9df2-cbac8dbfb7a7',1,1,NULL,NULL,'ROOT',NULL,'NetworkFilesystem',25,202,NULL,1,'2011-02-17 14:53:44',NULL,NULL,'2011-04-10 23:51:04','Created','Destroy',NULL,NULL,NULL),(17,22,21,200,NULL,NULL,'DATA-14',5368709120,'/export/home/david/ezh-sit-primary1','5e1f6f2b-6d94-4489-b162-eee1128d800d',1,1,NULL,NULL,'DATADISK',NULL,'NetworkFilesystem',26,NULL,NULL,1,'2011-02-17 14:53:44',NULL,'2011-04-10 23:51:03',NULL,'Created','Ready',NULL,NULL,NULL),(18,22,21,200,15,0,'ROOT-15',2097152000,'/export/home/david/ezh-sit-primary1','0c56dded-d057-4a2e-bbac-f89a102e2945',1,1,NULL,NULL,'ROOT',NULL,'NetworkFilesystem',8,1,NULL,1,'2011-02-17 14:55:10',NULL,NULL,NULL,'Created','Ready',NULL,NULL,NULL),(19,2,1,200,16,0,'ROOT-16',8589934592,'/export/home/david/ezh-sit-primary1','f751a1b1-eaff-4cb7-bcf8-8ad1d4fb7cf2',1,1,NULL,NULL,'ROOT',NULL,'NetworkFilesystem',25,220,NULL,1,'2011-02-21 04:49:43',NULL,NULL,NULL,'Created','Ready',NULL,NULL,NULL),(20,26,24,200,17,0,'ROOT-17',8589934592,'/export/home/david/ezh-sit-primary1','08734a93-14e4-4cc4-93d2-e90fec123a6d',1,1,NULL,NULL,'ROOT',NULL,'NetworkFilesystem',9,202,NULL,1,'2011-02-23 09:01:05',NULL,NULL,'2011-02-23 09:40:37','Created','Destroy',NULL,NULL,NULL),(21,26,24,200,18,0,'ROOT-18',2097152000,'/export/home/david/ezh-sit-primary1','10f9bdbf-53de-4182-bccc-bfd920526697',1,1,NULL,NULL,'ROOT',NULL,'NetworkFilesystem',8,1,NULL,1,'2011-02-23 09:01:06',NULL,NULL,'2011-02-23 09:40:38','Created','Destroy',NULL,NULL,NULL),(22,30,27,200,19,0,'ROOT-19',8589934592,'/export/home/david/ezh-sit-primary1','2b75fcf9-c7cb-4618-abd0-88c6829c9e4c',1,1,NULL,NULL,'ROOT',NULL,'NetworkFilesystem',9,202,NULL,1,'2011-02-23 10:20:15',NULL,NULL,'2011-02-23 10:38:28','Created','Destroy',NULL,NULL,NULL),(23,30,27,200,20,0,'ROOT-20',2097152000,'/export/home/david/ezh-sit-primary1','2fa4e42c-6a0d-4102-aa32-0ddc25479305',1,1,NULL,NULL,'ROOT',NULL,'NetworkFilesystem',8,1,NULL,1,'2011-02-23 10:20:16',NULL,NULL,'2011-02-23 10:38:29','Created','Destroy',NULL,NULL,NULL),(24,20,20,200,21,0,'ROOT-21',8589934592,'/export/home/david/ezh-sit-primary1','4f1e914d-3b28-4b51-b8cf-7638e338c2d1',1,1,NULL,NULL,'ROOT',NULL,'NetworkFilesystem',25,220,NULL,1,'2011-03-09 10:51:45',NULL,NULL,'2011-04-10 23:51:05','Created','Destroy',NULL,NULL,NULL),(25,20,20,200,22,0,'ROOT-22',2097152000,'/export/home/david/ezh-sit-primary1','bbb04664-eb22-4866-9f49-d2cc02c23d35',1,1,NULL,NULL,'ROOT',NULL,'NetworkFilesystem',8,1,NULL,1,'2011-03-09 10:51:46',NULL,NULL,NULL,'Created','Ready',NULL,NULL,NULL),(26,20,20,200,23,0,'ROOT-23',8589934592,'/export/home/david/ezh-sit-primary1','23e87cf6-d8ae-49f3-bb74-0edeba46d85b',1,1,NULL,NULL,'ROOT',NULL,'NetworkFilesystem',25,221,NULL,1,'2011-03-09 11:17:52',NULL,NULL,'2011-04-10 23:51:06','Created','Destroy',NULL,NULL,NULL),(27,20,20,200,NULL,NULL,'DATA-23',5368709120,'/export/home/david/ezh-sit-primary1','e26167a4-96c6-4c80-a9b6-f01b154cfb04',1,1,NULL,NULL,'DATADISK',NULL,'NetworkFilesystem',26,NULL,NULL,1,'2011-03-09 11:17:52',NULL,'2011-04-10 23:51:05',NULL,'Created','Ready',NULL,NULL,NULL),(28,20,20,NULL,24,0,'ROOT-24',8589934592,NULL,NULL,NULL,1,NULL,NULL,'ROOT',NULL,NULL,25,221,NULL,0,'2011-03-09 11:20:02',NULL,NULL,'2011-03-09 21:34:12','Creating','Destroy',NULL,NULL,NULL),(29,3,3,NULL,25,0,'ROOT-25',8589934592,NULL,NULL,NULL,1,NULL,NULL,'ROOT',NULL,NULL,12,220,NULL,0,'2011-03-10 00:28:27',NULL,NULL,'2011-03-10 21:34:06','Creating','Destroy',NULL,NULL,NULL),(30,2,1,NULL,26,0,'ROOT-26',8589934592,NULL,NULL,NULL,1,NULL,NULL,'ROOT',NULL,NULL,25,220,NULL,0,'2011-03-24 01:41:04',NULL,NULL,'2011-03-24 23:52:36','Creating','Destroy',NULL,NULL,NULL),(31,2,1,NULL,27,0,'ROOT-27',8589934592,NULL,NULL,NULL,1,NULL,NULL,'ROOT',NULL,NULL,25,220,NULL,0,'2011-03-24 01:42:26',NULL,NULL,'2011-03-24 23:52:36','Creating','Destroy',NULL,NULL,NULL),(32,2,1,200,16,1,'hgjg',5368709120,'/export/home/david/ezh-sit-primary1','6afedab2-37bd-496b-9101-f412913fb04a',1,1,NULL,NULL,'DATADISK','STORAGE_POOL','NetworkFilesystem',18,NULL,NULL,0,'2011-03-28 12:03:37','2011-03-28 12:04:14','2011-03-28 12:04:14',NULL,'Created','Ready',NULL,NULL,NULL),(33,42,39,NULL,28,0,'ROOT-28',8589934592,NULL,NULL,NULL,1,NULL,NULL,'ROOT',NULL,NULL,9,220,NULL,0,'2011-03-29 07:55:28',NULL,NULL,'2011-03-29 23:52:07','Creating','Destroy',NULL,NULL,NULL),(34,42,39,NULL,29,0,'ROOT-29',8589934592,NULL,NULL,NULL,1,NULL,NULL,'ROOT',NULL,NULL,9,202,NULL,0,'2011-03-29 08:19:04',NULL,NULL,'2011-03-29 23:52:07','Creating','Destroy',NULL,NULL,NULL),(35,51,45,NULL,30,0,'ROOT-30',8589934592,NULL,NULL,NULL,1,NULL,NULL,'ROOT',NULL,NULL,12,202,NULL,0,'2011-03-29 12:19:17',NULL,NULL,'2011-03-29 23:52:07','Creating','Destroy',NULL,NULL,NULL),(36,51,45,NULL,31,0,'ROOT-31',8589934592,NULL,NULL,NULL,1,NULL,NULL,'ROOT',NULL,NULL,9,220,NULL,0,'2011-03-30 06:12:02',NULL,NULL,'2011-03-30 23:52:02','Creating','Destroy',NULL,NULL,NULL),(37,2,1,NULL,32,0,'ROOT-32',8589934592,NULL,NULL,NULL,1,NULL,NULL,'ROOT',NULL,NULL,25,220,NULL,0,'2011-04-04 16:28:22',NULL,NULL,'2011-04-04 23:51:33','Creating','Destroy',NULL,NULL,NULL),(38,20,20,NULL,33,0,'ROOT-33',8589934592,NULL,NULL,NULL,1,NULL,NULL,'ROOT',NULL,NULL,25,220,NULL,0,'2011-04-04 20:09:43',NULL,NULL,'2011-04-04 23:51:34','Creating','Destroy',NULL,NULL,NULL),(39,20,20,NULL,34,0,'ROOT-34',8589934592,NULL,NULL,NULL,1,NULL,NULL,'ROOT',NULL,NULL,25,220,NULL,0,'2011-04-04 20:11:27',NULL,NULL,'2011-04-04 23:51:34','Creating','Destroy',NULL,NULL,NULL),(40,56,48,200,35,0,'ROOT-35',8589934592,'/export/home/david/ezh-sit-primary1','18260095-b183-44b9-9dc4-05b190a32baf',1,1,NULL,NULL,'ROOT',NULL,'NetworkFilesystem',9,202,NULL,1,'2011-04-09 10:26:38',NULL,NULL,NULL,'Created','Ready',NULL,NULL,NULL),(41,56,48,200,35,1,'DATA-35',5368709120,'/export/home/david/ezh-sit-primary1','695f0b38-5d62-43be-b556-23f1b083ba8a',1,1,NULL,NULL,'DATADISK',NULL,'NetworkFilesystem',13,NULL,NULL,1,'2011-04-09 10:26:38',NULL,NULL,NULL,'Created','Ready',NULL,NULL,NULL),(42,56,48,200,36,0,'ROOT-36',2097152000,'/export/home/david/ezh-sit-primary1','9b2951d4-d604-46dd-b356-6318033fe4c6',1,1,NULL,NULL,'ROOT',NULL,'NetworkFilesystem',8,1,NULL,1,'2011-04-09 10:26:50',NULL,NULL,NULL,'Created','Ready',NULL,NULL,NULL),(43,61,52,NULL,37,0,'ROOT-37',8589934592,NULL,NULL,NULL,1,NULL,NULL,'ROOT',NULL,NULL,9,221,NULL,0,'2011-04-25 12:53:30',NULL,NULL,'2011-04-25 17:45:02','Creating','Destroy',NULL,NULL,NULL),(44,61,52,NULL,37,1,'DATA-37',5368709120,NULL,NULL,NULL,1,NULL,NULL,'DATADISK',NULL,NULL,13,NULL,NULL,0,'2011-04-25 12:53:30',NULL,NULL,'2011-04-25 17:45:02','Creating','Destroy',NULL,NULL,NULL),(45,61,52,NULL,38,0,'ROOT-38',8589934592,NULL,NULL,NULL,1,NULL,NULL,'ROOT',NULL,NULL,9,202,NULL,0,'2011-04-25 12:54:45',NULL,NULL,'2011-04-25 17:45:02','Creating','Destroy',NULL,NULL,NULL),(46,61,52,NULL,38,1,'DATA-38',5368709120,NULL,NULL,NULL,1,NULL,NULL,'DATADISK',NULL,NULL,13,NULL,NULL,0,'2011-04-25 12:54:45',NULL,NULL,'2011-04-25 17:45:02','Creating','Destroy',NULL,NULL,NULL),(47,60,52,NULL,39,0,'ROOT-39',8589934592,NULL,NULL,NULL,1,NULL,NULL,'ROOT',NULL,NULL,9,202,NULL,0,'2011-04-25 12:57:07',NULL,NULL,'2011-04-25 17:45:02','Creating','Destroy',NULL,NULL,NULL),(48,60,52,NULL,39,1,'DATA-39',5368709120,NULL,NULL,NULL,1,NULL,NULL,'DATADISK',NULL,NULL,13,NULL,NULL,0,'2011-04-25 12:57:07',NULL,NULL,'2011-04-25 17:45:02','Creating','Destroy',NULL,NULL,NULL),(49,1,1,201,40,0,'ROOT-40',2097152000,'/export/profserv/ezh-sit/primary1','80116f68-dfd5-4dc4-b43d-0c7cfdf26e14',2,2,NULL,NULL,'ROOT',NULL,'NetworkFilesystem',6,1,NULL,1,'2011-05-06 22:06:57',NULL,NULL,NULL,'Created','Ready',NULL,NULL,NULL),(50,1,1,201,41,0,'ROOT-41',2097152000,'/export/profserv/ezh-sit/primary1','3ac92b18-6ae9-404e-8443-19db0acf4732',2,2,NULL,NULL,'ROOT',NULL,'NetworkFilesystem',7,1,NULL,1,'2011-05-06 22:06:58',NULL,NULL,NULL,'Created','Ready',NULL,NULL,NULL),(51,2,1,201,42,0,'ROOT-42',8589934592,'/export/profserv/ezh-sit/primary1','fe2cef84-5751-45aa-873e-1a44c43eb6d5',2,2,NULL,NULL,'ROOT',NULL,'NetworkFilesystem',25,2,NULL,1,'2011-05-11 21:43:38',NULL,NULL,NULL,'Created','Ready',NULL,NULL,NULL),(52,2,1,201,43,0,'ROOT-43',2097152000,'/export/profserv/ezh-sit/primary1','f508db8c-7be7-405b-a62a-f86c9c83cdd8',2,2,NULL,NULL,'ROOT',NULL,'NetworkFilesystem',8,1,NULL,1,'2011-05-11 21:44:57',NULL,NULL,NULL,'Created','Ready',NULL,NULL,NULL),(53,69,59,201,44,0,'ROOT-44',8589934592,'/export/profserv/ezh-sit/primary1','8a5015df-ecf9-4284-80ec-790b3b994012',2,2,NULL,NULL,'ROOT',NULL,'NetworkFilesystem',9,228,NULL,1,'2011-05-12 00:20:31',NULL,NULL,'2011-05-16 17:52:45','Created','Destroy',NULL,NULL,NULL),(54,69,59,201,45,0,'ROOT-45',2097152000,'/export/profserv/ezh-sit/primary1','7f53e13e-d457-46fc-a57c-50af45028571',2,2,NULL,NULL,'ROOT',NULL,'NetworkFilesystem',8,1,NULL,1,'2011-05-12 00:21:42',NULL,NULL,'2011-05-16 17:52:55','Created','Destroy',NULL,NULL,NULL),(55,70,60,201,46,0,'ROOT-46',8589934592,'/export/profserv/ezh-sit/primary1','5d6cbc8b-c41f-4adc-a12a-e1068bfc1e64',2,2,NULL,NULL,'ROOT',NULL,'NetworkFilesystem',9,228,NULL,1,'2011-05-12 18:48:49',NULL,NULL,NULL,'Created','Ready',NULL,NULL,NULL),(56,70,60,201,46,1,'DATA-46',5368709120,'/export/profserv/ezh-sit/primary1','d9f56434-4ee4-4b69-af83-a086566ef4fc',2,2,NULL,NULL,'DATADISK',NULL,'NetworkFilesystem',13,NULL,NULL,1,'2011-05-12 18:48:49',NULL,NULL,NULL,'Created','Ready',NULL,NULL,NULL),(57,70,60,201,47,0,'ROOT-47',2097152000,'/export/profserv/ezh-sit/primary1','cd30e0b9-4c03-477f-9484-ef04f7e2db9b',2,2,NULL,NULL,'ROOT',NULL,'NetworkFilesystem',8,1,NULL,1,'2011-05-12 18:48:50',NULL,NULL,NULL,'Created','Ready',NULL,NULL,NULL);
/*!40000 ALTER TABLE `volumes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vpn_users`
--

DROP TABLE IF EXISTS `vpn_users`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `vpn_users` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `owner_id` bigint(20) unsigned NOT NULL,
  `domain_id` bigint(20) unsigned NOT NULL,
  `username` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `state` char(32) NOT NULL COMMENT 'What state is this vpn user in',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`),
  UNIQUE KEY `i_vpn_users__account_id__username` (`owner_id`,`username`),
  KEY `fk_vpn_users__domain_id` (`domain_id`),
  KEY `i_vpn_users_username` (`username`),
  CONSTRAINT `fk_vpn_users__domain_id` FOREIGN KEY (`domain_id`) REFERENCES `domain` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_vpn_users__owner_id` FOREIGN KEY (`owner_id`) REFERENCES `account` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `vpn_users`
--

LOCK TABLES `vpn_users` WRITE;
/*!40000 ALTER TABLE `vpn_users` DISABLE KEYS */;
/*!40000 ALTER TABLE `vpn_users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2011-05-16 22:52:43
