-- MySQL dump 10.13  Distrib 5.5.17, for osx10.6 (i386)
--
-- Host: localhost    Database: cloud_usage
-- ------------------------------------------------------
-- Server version	5.5.17

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

USE cloud_usage;
DROP TABLE IF EXISTS `account`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `account` (
  `id` bigint(20) unsigned NOT NULL,
  `account_name` varchar(100) DEFAULT NULL COMMENT 'an account name set by the creator of the account, defaults to username for single accounts',
  `type` int(1) unsigned NOT NULL,
  `domain_id` bigint(20) unsigned DEFAULT NULL,
  `state` varchar(10) NOT NULL DEFAULT 'enabled',
  `removed` datetime DEFAULT NULL COMMENT 'date removed',
  `cleanup_needed` tinyint(1) NOT NULL DEFAULT '0',
  `network_domain` varchar(100) DEFAULT NULL COMMENT 'Network domain name of the Vms of the account',
  PRIMARY KEY (`id`),
  KEY `i_account__removed` (`removed`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `account`
--

LOCK TABLES `account` WRITE;
/*!40000 ALTER TABLE `account` DISABLE KEYS */;
/*!40000 ALTER TABLE `account` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cloud_usage`
--

DROP TABLE IF EXISTS `cloud_usage`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cloud_usage` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `zone_id` bigint(20) unsigned NOT NULL,
  `account_id` bigint(20) unsigned NOT NULL,
  `domain_id` bigint(20) unsigned NOT NULL,
  `description` varchar(1024) NOT NULL,
  `usage_display` varchar(255) NOT NULL,
  `usage_type` int(1) unsigned DEFAULT NULL,
  `raw_usage` double unsigned NOT NULL,
  `vm_instance_id` bigint(20) unsigned DEFAULT NULL,
  `vm_name` varchar(255) DEFAULT NULL,
  `offering_id` bigint(20) unsigned DEFAULT NULL,
  `template_id` bigint(20) unsigned DEFAULT NULL,
  `usage_id` bigint(20) unsigned DEFAULT NULL,
  `type` varchar(32) DEFAULT NULL,
  `size` bigint(20) unsigned DEFAULT NULL,
  `network_id` bigint(20) unsigned DEFAULT NULL,
  `start_date` datetime NOT NULL,
  `end_date` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `i_cloud_usage__account_id` (`account_id`),
  KEY `i_cloud_usage__domain_id` (`domain_id`),
  KEY `i_cloud_usage__start_date` (`start_date`),
  KEY `i_cloud_usage__end_date` (`end_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cloud_usage`
--

LOCK TABLES `cloud_usage` WRITE;
/*!40000 ALTER TABLE `cloud_usage` DISABLE KEYS */;
/*!40000 ALTER TABLE `cloud_usage` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `event`
--

DROP TABLE IF EXISTS `event`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `event` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `type` varchar(32) NOT NULL,
  `description` varchar(1024) NOT NULL,
  `user_id` bigint(20) unsigned NOT NULL,
  `account_id` bigint(20) unsigned NOT NULL,
  `created` datetime NOT NULL,
  `level` varchar(16) NOT NULL,
  `parameters` varchar(1024) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `event`
--

LOCK TABLES `event` WRITE;
/*!40000 ALTER TABLE `event` DISABLE KEYS */;
/*!40000 ALTER TABLE `event` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usage_event`
--

DROP TABLE IF EXISTS `usage_event`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `usage_event` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `type` varchar(32) NOT NULL,
  `account_id` bigint(20) unsigned NOT NULL,
  `created` datetime NOT NULL,
  `zone_id` bigint(20) unsigned NOT NULL,
  `resource_id` bigint(20) unsigned DEFAULT NULL,
  `resource_name` varchar(255) DEFAULT NULL,
  `offering_id` bigint(20) unsigned DEFAULT NULL,
  `template_id` bigint(20) unsigned DEFAULT NULL,
  `size` bigint(20) unsigned DEFAULT NULL,
  `resource_type` varchar(32) DEFAULT NULL,
  `processed` tinyint(4) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `i_usage_event__created` (`created`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usage_event`
--

LOCK TABLES `usage_event` WRITE;
/*!40000 ALTER TABLE `usage_event` DISABLE KEYS */;
/*!40000 ALTER TABLE `usage_event` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usage_ip_address`
--

DROP TABLE IF EXISTS `usage_ip_address`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `usage_ip_address` (
  `id` bigint(20) unsigned NOT NULL,
  `account_id` bigint(20) unsigned NOT NULL,
  `domain_id` bigint(20) unsigned NOT NULL,
  `zone_id` bigint(20) unsigned NOT NULL,
  `public_ip_address` varchar(15) NOT NULL,
  `is_source_nat` smallint(1) NOT NULL,
  `assigned` datetime NOT NULL,
  `released` datetime DEFAULT NULL,
  UNIQUE KEY `id` (`id`,`assigned`),
  KEY `i_usage_ip_address__account_id` (`account_id`),
  KEY `i_usage_ip_address__assigned` (`assigned`),
  KEY `i_usage_ip_address__released` (`released`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usage_ip_address`
--

LOCK TABLES `usage_ip_address` WRITE;
/*!40000 ALTER TABLE `usage_ip_address` DISABLE KEYS */;
/*!40000 ALTER TABLE `usage_ip_address` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usage_job`
--

DROP TABLE IF EXISTS `usage_job`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `usage_job` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `host` varchar(255) DEFAULT NULL,
  `pid` int(5) DEFAULT NULL,
  `job_type` int(1) DEFAULT NULL,
  `scheduled` int(1) DEFAULT NULL,
  `start_millis` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT 'start time in milliseconds of the aggregation range used by this job',
  `end_millis` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT 'end time in milliseconds of the aggregation range used by this job',
  `exec_time` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT 'how long in milliseconds it took for the job to execute',
  `start_date` datetime DEFAULT NULL COMMENT 'start date of the aggregation range used by this job',
  `end_date` datetime DEFAULT NULL COMMENT 'end date of the aggregation range used by this job',
  `success` int(1) DEFAULT NULL,
  `heartbeat` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `i_usage_job__end_millis` (`end_millis`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usage_job`
--

LOCK TABLES `usage_job` WRITE;
/*!40000 ALTER TABLE `usage_job` DISABLE KEYS */;
/*!40000 ALTER TABLE `usage_job` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usage_load_balancer_policy`
--

DROP TABLE IF EXISTS `usage_load_balancer_policy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `usage_load_balancer_policy` (
  `id` bigint(20) unsigned NOT NULL,
  `zone_id` bigint(20) unsigned NOT NULL,
  `account_id` bigint(20) unsigned NOT NULL,
  `domain_id` bigint(20) unsigned NOT NULL,
  `created` datetime NOT NULL,
  `deleted` datetime DEFAULT NULL,
  KEY `i_usage_load_balancer_policy__account_id` (`account_id`),
  KEY `i_usage_load_balancer_policy__created` (`created`),
  KEY `i_usage_load_balancer_policy__deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usage_load_balancer_policy`
--

LOCK TABLES `usage_load_balancer_policy` WRITE;
/*!40000 ALTER TABLE `usage_load_balancer_policy` DISABLE KEYS */;
/*!40000 ALTER TABLE `usage_load_balancer_policy` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usage_network`
--

DROP TABLE IF EXISTS `usage_network`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `usage_network` (
  `account_id` bigint(20) unsigned NOT NULL,
  `zone_id` bigint(20) unsigned NOT NULL,
  `host_id` bigint(20) unsigned NOT NULL,
  `host_type` varchar(32) DEFAULT NULL,
  `network_id` bigint(20) unsigned DEFAULT NULL,
  `bytes_sent` bigint(20) unsigned NOT NULL DEFAULT '0',
  `bytes_received` bigint(20) unsigned NOT NULL DEFAULT '0',
  `net_bytes_received` bigint(20) unsigned NOT NULL DEFAULT '0',
  `net_bytes_sent` bigint(20) unsigned NOT NULL DEFAULT '0',
  `current_bytes_received` bigint(20) unsigned NOT NULL DEFAULT '0',
  `current_bytes_sent` bigint(20) unsigned NOT NULL DEFAULT '0',
  `event_time_millis` bigint(20) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`account_id`,`zone_id`,`host_id`,`event_time_millis`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usage_network`
--

LOCK TABLES `usage_network` WRITE;
/*!40000 ALTER TABLE `usage_network` DISABLE KEYS */;
/*!40000 ALTER TABLE `usage_network` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usage_network_offering`
--

DROP TABLE IF EXISTS `usage_network_offering`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `usage_network_offering` (
  `zone_id` bigint(20) unsigned NOT NULL,
  `account_id` bigint(20) unsigned NOT NULL,
  `domain_id` bigint(20) unsigned NOT NULL,
  `vm_instance_id` bigint(20) unsigned NOT NULL,
  `network_offering_id` bigint(20) unsigned NOT NULL,
  `is_default` smallint(1) NOT NULL,
  `created` datetime NOT NULL,
  `deleted` datetime DEFAULT NULL,
  KEY `i_usage_network_offering__account_id` (`account_id`),
  KEY `i_usage_network_offering__created` (`created`),
  KEY `i_usage_network_offering__deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usage_network_offering`
--

LOCK TABLES `usage_network_offering` WRITE;
/*!40000 ALTER TABLE `usage_network_offering` DISABLE KEYS */;
/*!40000 ALTER TABLE `usage_network_offering` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usage_port_forwarding`
--

DROP TABLE IF EXISTS `usage_port_forwarding`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `usage_port_forwarding` (
  `id` bigint(20) unsigned NOT NULL,
  `zone_id` bigint(20) unsigned NOT NULL,
  `account_id` bigint(20) unsigned NOT NULL,
  `domain_id` bigint(20) unsigned NOT NULL,
  `created` datetime NOT NULL,
  `deleted` datetime DEFAULT NULL,
  KEY `i_usage_port_forwarding__account_id` (`account_id`),
  KEY `i_usage_port_forwarding__created` (`created`),
  KEY `i_usage_port_forwarding__deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usage_port_forwarding`
--

LOCK TABLES `usage_port_forwarding` WRITE;
/*!40000 ALTER TABLE `usage_port_forwarding` DISABLE KEYS */;
/*!40000 ALTER TABLE `usage_port_forwarding` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usage_security_group`
--

DROP TABLE IF EXISTS `usage_security_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `usage_security_group` (
  `id` bigint(20) unsigned NOT NULL,
  `zone_id` bigint(20) unsigned NOT NULL,
  `account_id` bigint(20) unsigned NOT NULL,
  `domain_id` bigint(20) unsigned NOT NULL,
  `vm_id` bigint(20) unsigned NOT NULL,
  `num_rules` bigint(20) unsigned NOT NULL,
  `created` datetime NOT NULL,
  `deleted` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usage_security_group`
--

LOCK TABLES `usage_security_group` WRITE;
/*!40000 ALTER TABLE `usage_security_group` DISABLE KEYS */;
/*!40000 ALTER TABLE `usage_security_group` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usage_storage`
--

DROP TABLE IF EXISTS `usage_storage`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `usage_storage` (
  `id` bigint(20) unsigned NOT NULL,
  `zone_id` bigint(20) unsigned NOT NULL,
  `account_id` bigint(20) unsigned NOT NULL,
  `domain_id` bigint(20) unsigned NOT NULL,
  `storage_type` int(1) unsigned NOT NULL,
  `source_id` bigint(20) unsigned DEFAULT NULL,
  `size` bigint(20) unsigned NOT NULL,
  `created` datetime NOT NULL,
  `deleted` datetime DEFAULT NULL,
  UNIQUE KEY `id` (`id`,`storage_type`,`zone_id`,`created`),
  KEY `i_usage_storage__account_id` (`account_id`),
  KEY `i_usage_storage__created` (`created`),
  KEY `i_usage_storage__deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usage_storage`
--

LOCK TABLES `usage_storage` WRITE;
/*!40000 ALTER TABLE `usage_storage` DISABLE KEYS */;
/*!40000 ALTER TABLE `usage_storage` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usage_vm_instance`
--

DROP TABLE IF EXISTS `usage_vm_instance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `usage_vm_instance` (
  `usage_type` int(1) unsigned DEFAULT NULL,
  `zone_id` bigint(20) unsigned NOT NULL,
  `account_id` bigint(20) unsigned NOT NULL,
  `vm_instance_id` bigint(20) unsigned NOT NULL,
  `vm_name` varchar(255) NOT NULL,
  `service_offering_id` bigint(20) unsigned NOT NULL,
  `template_id` bigint(20) unsigned NOT NULL,
  `hypervisor_type` varchar(255) DEFAULT NULL,
  `start_date` datetime NOT NULL,
  `end_date` datetime DEFAULT NULL,
  UNIQUE KEY `vm_instance_id` (`vm_instance_id`,`usage_type`,`start_date`),
  KEY `i_usage_vm_instance__account_id` (`account_id`),
  KEY `i_usage_vm_instance__start_date` (`start_date`),
  KEY `i_usage_vm_instance__end_date` (`end_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usage_vm_instance`
--

LOCK TABLES `usage_vm_instance` WRITE;
/*!40000 ALTER TABLE `usage_vm_instance` DISABLE KEYS */;
/*!40000 ALTER TABLE `usage_vm_instance` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usage_volume`
--

DROP TABLE IF EXISTS `usage_volume`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `usage_volume` (
  `id` bigint(20) unsigned NOT NULL,
  `zone_id` bigint(20) unsigned NOT NULL,
  `account_id` bigint(20) unsigned NOT NULL,
  `domain_id` bigint(20) unsigned NOT NULL,
  `disk_offering_id` bigint(20) unsigned DEFAULT NULL,
  `template_id` bigint(20) unsigned DEFAULT NULL,
  `size` bigint(20) unsigned DEFAULT NULL,
  `created` datetime NOT NULL,
  `deleted` datetime DEFAULT NULL,
  UNIQUE KEY `id` (`id`,`created`),
  KEY `i_usage_volume__account_id` (`account_id`),
  KEY `i_usage_volume__created` (`created`),
  KEY `i_usage_volume__deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usage_volume`
--

LOCK TABLES `usage_volume` WRITE;
/*!40000 ALTER TABLE `usage_volume` DISABLE KEYS */;
/*!40000 ALTER TABLE `usage_volume` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_statistics`
--

DROP TABLE IF EXISTS `user_statistics`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_statistics` (
  `id` bigint(20) unsigned NOT NULL,
  `data_center_id` bigint(20) unsigned NOT NULL,
  `account_id` bigint(20) unsigned NOT NULL,
  `public_ip_address` varchar(15) DEFAULT NULL,
  `device_id` bigint(20) unsigned NOT NULL,
  `device_type` varchar(32) NOT NULL,
  `network_id` bigint(20) unsigned DEFAULT NULL,
  `net_bytes_received` bigint(20) unsigned NOT NULL DEFAULT '0',
  `net_bytes_sent` bigint(20) unsigned NOT NULL DEFAULT '0',
  `current_bytes_received` bigint(20) unsigned NOT NULL DEFAULT '0',
  `current_bytes_sent` bigint(20) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`),
  UNIQUE KEY `account_id` (`account_id`,`data_center_id`,`public_ip_address`,`device_id`,`device_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_statistics`
--

LOCK TABLES `user_statistics` WRITE;
/*!40000 ALTER TABLE `user_statistics` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_statistics` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2012-02-01 12:35:06


