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
-- Current Database: `cloud`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `cloud` /*!40100 DEFAULT CHARACTER SET latin1 */;

USE `cloud`;

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
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `account`
--

LOCK TABLES `account` WRITE;
/*!40000 ALTER TABLE `account` DISABLE KEYS */;
INSERT INTO `account` VALUES (1,'system',1,1,'enabled',NULL,0),(2,'admin',1,1,'enabled',NULL,0),(3,'nimbus',0,2,'enabled',NULL,0);
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
  CONSTRAINT `fk_account_network_ref__networks_id` FOREIGN KEY (`network_id`) REFERENCES `networks` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_account_network_ref__account_id` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `account_network_ref`
--

LOCK TABLES `account_network_ref` WRITE;
/*!40000 ALTER TABLE `account_network_ref` DISABLE KEYS */;
INSERT INTO `account_network_ref` VALUES (1,1,200,1),(2,1,201,1),(3,1,202,1),(4,1,203,1);
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
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `alert`
--

LOCK TABLES `alert` WRITE;
/*!40000 ALTER TABLE `alert` DISABLE KEYS */;
INSERT INTO `alert` VALUES (1,13,0,0,'Management server node 192.168.130.241 is up',1,'2011-04-26 01:24:32','2011-04-26 01:24:32',NULL),(2,12,0,0,'No usage server process running',1,'2011-04-26 02:24:31','2011-04-26 02:24:31',NULL),(3,13,0,0,'Management server node 192.168.130.241 is up',1,'2011-04-26 02:31:05','2011-04-26 02:31:05',NULL),(4,12,0,0,'No usage server process running',1,'2011-04-26 03:31:03','2011-04-26 03:31:03',NULL),(5,13,0,0,'Management server node 192.168.130.241 is up',1,'2011-04-26 03:40:36','2011-04-26 03:40:36',NULL);
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
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `async_job`
--

LOCK TABLES `async_job` WRITE;
/*!40000 ALTER TABLE `async_job` DISABLE KEYS */;
INSERT INTO `async_job` VALUES (1,2,2,NULL,NULL,NULL,'com.cloud.api.commands.DeployVMCmd',NULL,'{\"sessionkey\":\"f+khWwzg8cqbf3uQLVldsrgcNIg\\u003d\",\"ctxUserId\":\"2\",\"serviceOfferingId\":\"9\",\"zoneId\":\"1\",\"templateId\":\"2\",\"response\":\"json\",\"id\":\"3\",\"hypervisor\":\"XenServer\",\"diskOfferingId\":\"10\",\"_\":\"1303787815971\",\"ctxAccountId\":\"2\",\"group\":\"Admin-VM-1\",\"ctxStartEventId\":\"1\",\"displayname\":\"Admin-VM-1\"}',0,0,NULL,1,0,0,'com.cloud.api.response.UserVmResponse/virtualmachine/{\"id\":3,\"name\":\"i-2-3-VM\",\"displayname\":\"Admin-VM-1\",\"account\":\"admin\",\"domainid\":1,\"domain\":\"ROOT\",\"created\":\"2011-04-25T23:16:56-0400\",\"state\":\"Running\",\"haenable\":false,\"groupid\":1,\"group\":\"Admin-VM-1\",\"zoneid\":1,\"zonename\":\"ZONE\",\"hostid\":2,\"hostname\":\"xenserver-chandan-164-2\",\"templateid\":2,\"templatename\":\"CentOS 5.3(64-bit) no GUI (XenServer)\",\"templatedisplaytext\":\"CentOS 5.3(64-bit) no GUI (XenServer)\",\"passwordenabled\":false,\"serviceofferingid\":9,\"serviceofferingname\":\"Little Instance\",\"cpunumber\":1,\"cpuspeed\":100,\"memory\":128,\"guestosid\":11,\"rootdeviceid\":0,\"rootdevicetype\":\"NetworkFilesystem\",\"securitygroup\":[],\"nic\":[{\"id\":7,\"networkid\":203,\"netmask\":\"255.255.255.0\",\"gateway\":\"192.168.164.1\",\"ipaddress\":\"192.168.164.123\",\"isolationuri\":\"ec2://untagged\",\"broadcasturi\":\"vlan://untagged\",\"traffictype\":\"Guest\",\"type\":\"Direct\",\"isdefault\":true}],\"hypervisor\":\"XenServer\"}',6603060216574,6603060216574,'2011-04-26 03:16:57','2011-04-26 03:18:43','2011-04-26 03:18:37',NULL),(2,2,2,NULL,NULL,NULL,'com.cloud.api.commands.DeployVMCmd',NULL,'{\"sessionkey\":\"f+khWwzg8cqbf3uQLVldsrgcNIg\\u003d\",\"ctxUserId\":\"2\",\"serviceOfferingId\":\"9\",\"zoneId\":\"1\",\"templateId\":\"2\",\"response\":\"json\",\"id\":\"5\",\"hypervisor\":\"XenServer\",\"_\":\"1303788928337\",\"ctxAccountId\":\"2\",\"group\":\"Admin-VM-2\",\"ctxStartEventId\":\"5\",\"displayname\":\"Admin-VM-2\"}',0,0,NULL,1,0,0,'com.cloud.api.response.UserVmResponse/virtualmachine/{\"id\":5,\"name\":\"i-2-5-VM\",\"displayname\":\"Admin-VM-2\",\"account\":\"admin\",\"domainid\":1,\"domain\":\"ROOT\",\"created\":\"2011-04-25T23:35:28-0400\",\"state\":\"Running\",\"haenable\":false,\"groupid\":2,\"group\":\"Admin-VM-2\",\"zoneid\":1,\"zonename\":\"ZONE\",\"hostid\":2,\"hostname\":\"xenserver-chandan-164-2\",\"templateid\":2,\"templatename\":\"CentOS 5.3(64-bit) no GUI (XenServer)\",\"templatedisplaytext\":\"CentOS 5.3(64-bit) no GUI (XenServer)\",\"passwordenabled\":false,\"serviceofferingid\":9,\"serviceofferingname\":\"Little Instance\",\"cpunumber\":1,\"cpuspeed\":100,\"memory\":128,\"guestosid\":11,\"rootdeviceid\":0,\"rootdevicetype\":\"NetworkFilesystem\",\"securitygroup\":[],\"nic\":[{\"id\":10,\"networkid\":203,\"netmask\":\"255.255.255.0\",\"gateway\":\"192.168.164.1\",\"ipaddress\":\"192.168.164.128\",\"isolationuri\":\"ec2://untagged\",\"broadcasturi\":\"vlan://untagged\",\"traffictype\":\"Guest\",\"type\":\"Direct\",\"isdefault\":true}],\"hypervisor\":\"XenServer\"}',6603060216574,6603060216574,'2011-04-26 03:35:28','2011-04-26 03:35:36',NULL,NULL),(3,3,3,NULL,NULL,NULL,'com.cloud.api.commands.DeployVMCmd',NULL,'{\"sessionkey\":\"m+qsefxurI3sowrmmjEEKXCj+3I\\u003d\",\"ctxUserId\":\"3\",\"serviceOfferingId\":\"9\",\"zoneId\":\"1\",\"templateId\":\"2\",\"response\":\"json\",\"id\":\"6\",\"hypervisor\":\"XenServer\",\"diskOfferingId\":\"10\",\"_\":\"1303789021036\",\"ctxAccountId\":\"3\",\"group\":\"Nimbus-VM-1\",\"ctxStartEventId\":\"10\",\"displayname\":\"Nimbus-VM-1\"}',0,0,NULL,1,0,0,'com.cloud.api.response.UserVmResponse/virtualmachine/{\"id\":6,\"name\":\"i-3-6-VM\",\"displayname\":\"Nimbus-VM-1\",\"account\":\"nimbus\",\"domainid\":2,\"domain\":\"CHILD\",\"created\":\"2011-04-25T23:37:01-0400\",\"state\":\"Running\",\"haenable\":false,\"groupid\":3,\"group\":\"Nimbus-VM-1\",\"zoneid\":1,\"zonename\":\"ZONE\",\"templateid\":2,\"templatename\":\"CentOS 5.3(64-bit) no GUI (XenServer)\",\"templatedisplaytext\":\"CentOS 5.3(64-bit) no GUI (XenServer)\",\"passwordenabled\":false,\"serviceofferingid\":9,\"serviceofferingname\":\"Little Instance\",\"cpunumber\":1,\"cpuspeed\":100,\"memory\":128,\"guestosid\":11,\"rootdeviceid\":0,\"rootdevicetype\":\"NetworkFilesystem\",\"securitygroup\":[],\"nic\":[{\"id\":11,\"networkid\":203,\"netmask\":\"255.255.255.0\",\"gateway\":\"192.168.164.1\",\"ipaddress\":\"192.168.164.122\",\"traffictype\":\"Guest\",\"type\":\"Direct\",\"isdefault\":true}],\"hypervisor\":\"XenServer\"}',6603060216574,6603060216574,'2011-04-26 03:37:01','2011-04-26 03:37:10',NULL,NULL),(4,3,3,NULL,NULL,NULL,'com.cloud.api.commands.DeployVMCmd',NULL,'{\"sessionkey\":\"m+qsefxurI3sowrmmjEEKXCj+3I\\u003d\",\"ctxUserId\":\"3\",\"serviceOfferingId\":\"9\",\"zoneId\":\"1\",\"templateId\":\"2\",\"response\":\"json\",\"id\":\"7\",\"hypervisor\":\"XenServer\",\"_\":\"1303789036380\",\"ctxAccountId\":\"3\",\"group\":\"Nimbus-VM-2\",\"ctxStartEventId\":\"14\",\"displayname\":\"Nimbus-VM-2\"}',0,0,NULL,1,0,0,'com.cloud.api.response.UserVmResponse/virtualmachine/{\"id\":7,\"name\":\"i-3-7-VM\",\"displayname\":\"Nimbus-VM-2\",\"account\":\"nimbus\",\"domainid\":2,\"domain\":\"CHILD\",\"created\":\"2011-04-25T23:37:16-0400\",\"state\":\"Running\",\"haenable\":false,\"groupid\":4,\"group\":\"Nimbus-VM-2\",\"zoneid\":1,\"zonename\":\"ZONE\",\"templateid\":2,\"templatename\":\"CentOS 5.3(64-bit) no GUI (XenServer)\",\"templatedisplaytext\":\"CentOS 5.3(64-bit) no GUI (XenServer)\",\"passwordenabled\":false,\"serviceofferingid\":9,\"serviceofferingname\":\"Little Instance\",\"cpunumber\":1,\"cpuspeed\":100,\"memory\":128,\"guestosid\":11,\"rootdeviceid\":0,\"rootdevicetype\":\"NetworkFilesystem\",\"securitygroup\":[],\"nic\":[{\"id\":12,\"networkid\":203,\"netmask\":\"255.255.255.0\",\"gateway\":\"192.168.164.1\",\"ipaddress\":\"192.168.164.126\",\"traffictype\":\"Guest\",\"type\":\"Direct\",\"isdefault\":true}],\"hypervisor\":\"XenServer\"}',6603060216574,6603060216574,'2011-04-26 03:37:16','2011-04-26 03:37:25',NULL,NULL),(5,2,2,NULL,NULL,NULL,'com.cloud.api.commands.CreateSnapshotCmd',NULL,'{\"id\":\"1\",\"response\":\"json\",\"sessionkey\":\"kQzH8LhRCN9VxZ6zxoT58bjNAtk\\u003d\",\"ctxUserId\":\"2\",\"_\":\"1303790746219\",\"volumeid\":\"9\",\"ctxAccountId\":\"2\",\"ctxStartEventId\":\"19\"}',0,0,NULL,1,0,0,'com.cloud.api.response.SnapshotResponse/snapshot/{\"id\":1,\"account\":\"nimbus\",\"domainid\":2,\"domain\":\"CHILD\",\"snapshottype\":\"MANUAL\",\"volumeid\":9,\"volumename\":\"ROOT-7\",\"volumetype\":\"ROOT\",\"created\":\"2011-04-26T00:05:46-0400\",\"name\":\"i-3-7-VM_ROOT-7_20110426040546\",\"intervaltype\":\"MANUAL\"}',6603060216574,6603060216574,'2011-04-26 04:05:46','2011-04-26 04:11:27','2011-04-26 04:11:26',NULL),(6,2,2,NULL,NULL,NULL,'com.cloud.api.commands.CreateSnapshotCmd',NULL,'{\"id\":\"2\",\"response\":\"json\",\"sessionkey\":\"kQzH8LhRCN9VxZ6zxoT58bjNAtk\\u003d\",\"ctxUserId\":\"2\",\"_\":\"1303790750377\",\"volumeid\":\"7\",\"ctxAccountId\":\"2\",\"ctxStartEventId\":\"21\"}',0,0,NULL,1,0,0,'com.cloud.api.response.SnapshotResponse/snapshot/{\"id\":2,\"account\":\"nimbus\",\"domainid\":2,\"domain\":\"CHILD\",\"snapshottype\":\"MANUAL\",\"volumeid\":7,\"volumename\":\"ROOT-6\",\"volumetype\":\"ROOT\",\"created\":\"2011-04-26T00:05:50-0400\",\"name\":\"i-3-6-VM_ROOT-6_20110426040550\",\"intervaltype\":\"MANUAL\"}',6603060216574,6603060216574,'2011-04-26 04:05:50','2011-04-26 04:11:30','2011-04-26 04:11:20',NULL),(7,2,2,NULL,NULL,NULL,'com.cloud.api.commands.CreateSnapshotCmd',NULL,'{\"id\":\"3\",\"response\":\"json\",\"sessionkey\":\"kQzH8LhRCN9VxZ6zxoT58bjNAtk\\u003d\",\"ctxUserId\":\"2\",\"_\":\"1303790754992\",\"volumeid\":\"8\",\"ctxAccountId\":\"2\",\"ctxStartEventId\":\"23\"}',0,0,NULL,1,0,0,'com.cloud.api.response.SnapshotResponse/snapshot/{\"id\":3,\"account\":\"nimbus\",\"domainid\":2,\"domain\":\"CHILD\",\"snapshottype\":\"MANUAL\",\"volumeid\":8,\"volumename\":\"DATA-6\",\"volumetype\":\"DATADISK\",\"created\":\"2011-04-26T00:05:55-0400\",\"name\":\"i-3-6-VM_DATA-6_20110426040555\",\"intervaltype\":\"MANUAL\"}',6603060216574,6603060216574,'2011-04-26 04:05:55','2011-04-26 04:06:43','2011-04-26 04:06:35',NULL),(8,2,2,NULL,NULL,NULL,'com.cloud.api.commands.CreateSnapshotCmd',NULL,'{\"id\":\"4\",\"response\":\"json\",\"sessionkey\":\"kQzH8LhRCN9VxZ6zxoT58bjNAtk\\u003d\",\"ctxUserId\":\"2\",\"_\":\"1303790759068\",\"volumeid\":\"6\",\"ctxAccountId\":\"2\",\"ctxStartEventId\":\"25\"}',0,0,NULL,1,0,0,'com.cloud.api.response.SnapshotResponse/snapshot/{\"id\":4,\"account\":\"admin\",\"domainid\":1,\"domain\":\"ROOT\",\"snapshottype\":\"MANUAL\",\"volumeid\":6,\"volumename\":\"ROOT-5\",\"volumetype\":\"ROOT\",\"created\":\"2011-04-26T00:05:59-0400\",\"name\":\"i-2-5-VM_ROOT-5_20110426040559\",\"intervaltype\":\"MANUAL\"}',6603060216574,6603060216574,'2011-04-26 04:05:59','2011-04-26 04:11:25','2011-04-26 04:11:19',NULL),(9,2,2,NULL,NULL,NULL,'com.cloud.api.commands.CreateSnapshotCmd',NULL,'{\"id\":\"5\",\"response\":\"json\",\"sessionkey\":\"kQzH8LhRCN9VxZ6zxoT58bjNAtk\\u003d\",\"ctxUserId\":\"2\",\"_\":\"1303790763258\",\"volumeid\":\"3\",\"ctxAccountId\":\"2\",\"ctxStartEventId\":\"27\"}',0,0,NULL,1,0,0,'com.cloud.api.response.SnapshotResponse/snapshot/{\"id\":5,\"account\":\"admin\",\"domainid\":1,\"domain\":\"ROOT\",\"snapshottype\":\"MANUAL\",\"volumeid\":3,\"volumename\":\"ROOT-3\",\"volumetype\":\"ROOT\",\"created\":\"2011-04-26T00:06:03-0400\",\"name\":\"i-2-3-VM_ROOT-3_20110426040603\",\"intervaltype\":\"MANUAL\"}',6603060216574,6603060216574,'2011-04-26 04:06:03','2011-04-26 04:11:30','2011-04-26 04:11:23',NULL),(10,2,2,NULL,NULL,NULL,'com.cloud.api.commands.CreateSnapshotCmd',NULL,'{\"id\":\"6\",\"response\":\"json\",\"sessionkey\":\"kQzH8LhRCN9VxZ6zxoT58bjNAtk\\u003d\",\"ctxUserId\":\"2\",\"_\":\"1303790767641\",\"volumeid\":\"4\",\"ctxAccountId\":\"2\",\"ctxStartEventId\":\"29\"}',0,0,NULL,1,0,0,'com.cloud.api.response.SnapshotResponse/snapshot/{\"id\":6,\"account\":\"admin\",\"domainid\":1,\"domain\":\"ROOT\",\"snapshottype\":\"MANUAL\",\"volumeid\":4,\"volumename\":\"DATA-3\",\"volumetype\":\"DATADISK\",\"created\":\"2011-04-26T00:06:07-0400\",\"name\":\"i-2-3-VM_DATA-3_20110426040607\",\"intervaltype\":\"MANUAL\"}',6603060216574,6603060216574,'2011-04-26 04:06:07','2011-04-26 04:07:10','2011-04-26 04:07:07',NULL),(11,2,2,NULL,NULL,NULL,'com.cloud.api.commands.CreateTemplateCmd',NULL,'{\"sessionkey\":\"kQzH8LhRCN9VxZ6zxoT58bjNAtk\\u003d\",\"ctxUserId\":\"2\",\"isPublic\":\"true\",\"ostypeid\":\"12\",\"isfeatured\":\"true\",\"response\":\"json\",\"id\":\"202\",\"snapshotid\":\"5\",\"passwordEnabled\":\"false\",\"name\":\"TemplateFromSnapshot-1\",\"displaytext\":\"TemplateFromSnapshot-1\",\"_\":\"1303791888959\",\"ctxAccountId\":\"2\",\"ctxStartEventId\":\"37\"}',0,0,NULL,1,0,0,'com.cloud.api.response.TemplateResponse/template/{\"id\":202,\"name\":\"TemplateFromSnapshot-1\",\"displaytext\":\"TemplateFromSnapshot-1\",\"ispublic\":true,\"created\":\"2011-04-26T00:33:28-0400\",\"isready\":true,\"passwordenabled\":false,\"isfeatured\":false,\"crossZones\":false,\"ostypeid\":12,\"ostypename\":\"CentOS 5.3 (64-bit)\",\"account\":\"admin\",\"zoneid\":1,\"zonename\":\"ZONE\",\"domain\":\"ROOT\",\"domainid\":1}',6603060216574,6603060216574,'2011-04-26 04:24:49','2011-04-26 04:33:28','2011-04-26 04:33:19',NULL),(12,2,2,NULL,NULL,NULL,'com.cloud.api.commands.CreateTemplateCmd',NULL,'{\"sessionkey\":\"kQzH8LhRCN9VxZ6zxoT58bjNAtk\\u003d\",\"ctxUserId\":\"2\",\"isPublic\":\"true\",\"ostypeid\":\"12\",\"isfeatured\":\"true\",\"response\":\"json\",\"id\":\"203\",\"snapshotid\":\"4\",\"passwordEnabled\":\"false\",\"name\":\"TemplateFromSnapshot-2\",\"displaytext\":\"TemplateFromSnapshot-2\",\"_\":\"1303791902585\",\"ctxAccountId\":\"2\",\"ctxStartEventId\":\"38\"}',0,0,NULL,1,0,0,'com.cloud.api.response.TemplateResponse/template/{\"id\":203,\"name\":\"TemplateFromSnapshot-2\",\"displaytext\":\"TemplateFromSnapshot-2\",\"ispublic\":true,\"created\":\"2011-04-26T00:33:29-0400\",\"isready\":true,\"passwordenabled\":false,\"isfeatured\":false,\"crossZones\":false,\"ostypeid\":12,\"ostypename\":\"CentOS 5.3 (64-bit)\",\"account\":\"admin\",\"zoneid\":1,\"zonename\":\"ZONE\",\"domain\":\"ROOT\",\"domainid\":1}',6603060216574,6603060216574,'2011-04-26 04:25:02','2011-04-26 04:33:29','2011-04-26 04:33:23',NULL),(13,2,2,NULL,NULL,NULL,'com.cloud.api.commands.CreateTemplateCmd',NULL,'{\"sessionkey\":\"kQzH8LhRCN9VxZ6zxoT58bjNAtk\\u003d\",\"ctxUserId\":\"2\",\"isPublic\":\"true\",\"ostypeid\":\"12\",\"isfeatured\":\"true\",\"response\":\"json\",\"id\":\"204\",\"snapshotid\":\"2\",\"passwordEnabled\":\"false\",\"name\":\"TemplateFromSnapshot-3\",\"displaytext\":\"TemplateFromSnapshot-3\",\"_\":\"1303791916148\",\"ctxAccountId\":\"2\",\"ctxStartEventId\":\"39\"}',0,0,NULL,1,0,0,'com.cloud.api.response.TemplateResponse/template/{\"id\":204,\"name\":\"TemplateFromSnapshot-3\",\"displaytext\":\"TemplateFromSnapshot-3\",\"ispublic\":true,\"created\":\"2011-04-26T00:33:36-0400\",\"isready\":true,\"passwordenabled\":false,\"isfeatured\":false,\"crossZones\":false,\"ostypeid\":12,\"ostypename\":\"CentOS 5.3 (64-bit)\",\"account\":\"nimbus\",\"zoneid\":1,\"zonename\":\"ZONE\",\"domain\":\"CHILD\",\"domainid\":2}',6603060216574,6603060216574,'2011-04-26 04:25:16','2011-04-26 04:33:37','2011-04-26 04:33:36',NULL),(14,2,2,NULL,NULL,NULL,'com.cloud.api.commands.CreateTemplateCmd',NULL,'{\"sessionkey\":\"kQzH8LhRCN9VxZ6zxoT58bjNAtk\\u003d\",\"ctxUserId\":\"2\",\"isPublic\":\"true\",\"ostypeid\":\"12\",\"isfeatured\":\"true\",\"response\":\"json\",\"id\":\"205\",\"snapshotid\":\"1\",\"passwordEnabled\":\"false\",\"name\":\"TemplateFromSnapshot-4\",\"displaytext\":\"TemplateFromSnapshot-4\",\"_\":\"1303791930490\",\"ctxAccountId\":\"2\",\"ctxStartEventId\":\"40\"}',0,0,NULL,1,0,0,'com.cloud.api.response.TemplateResponse/template/{\"id\":205,\"name\":\"TemplateFromSnapshot-4\",\"displaytext\":\"TemplateFromSnapshot-4\",\"ispublic\":true,\"created\":\"2011-04-26T00:33:42-0400\",\"isready\":true,\"passwordenabled\":false,\"isfeatured\":false,\"crossZones\":false,\"ostypeid\":12,\"ostypename\":\"CentOS 5.3 (64-bit)\",\"account\":\"nimbus\",\"zoneid\":1,\"zonename\":\"ZONE\",\"domain\":\"CHILD\",\"domainid\":2}',6603060216574,6603060216574,'2011-04-26 04:25:30','2011-04-26 04:33:42','2011-04-26 04:33:40',NULL),(15,2,2,NULL,NULL,NULL,'com.cloud.api.commands.CreateTemplateCmd',NULL,'{\"sessionkey\":\"kQzH8LhRCN9VxZ6zxoT58bjNAtk\\u003d\",\"ctxUserId\":\"2\",\"isPublic\":\"true\",\"ostypeid\":\"103\",\"isfeatured\":\"true\",\"response\":\"json\",\"id\":\"206\",\"snapshotid\":\"6\",\"passwordEnabled\":\"false\",\"name\":\"TemplateFromSnapshot-5-Data\",\"displaytext\":\"TemplateFromSnapshot-5-Data\",\"_\":\"1303791987001\",\"ctxAccountId\":\"2\",\"ctxStartEventId\":\"41\"}',0,0,NULL,1,0,0,'com.cloud.api.response.TemplateResponse/template/{\"id\":206,\"name\":\"TemplateFromSnapshot-5-Data\",\"displaytext\":\"TemplateFromSnapshot-5-Data\",\"ispublic\":true,\"created\":\"2011-04-26T00:27:23-0400\",\"isready\":true,\"passwordenabled\":false,\"isfeatured\":false,\"crossZones\":false,\"ostypeid\":103,\"ostypename\":\"Other (64-bit)\",\"account\":\"admin\",\"zoneid\":1,\"zonename\":\"ZONE\",\"domain\":\"ROOT\",\"domainid\":1}',6603060216574,6603060216574,'2011-04-26 04:26:27','2011-04-26 04:27:23','2011-04-26 04:27:17',NULL),(16,2,2,NULL,NULL,NULL,'com.cloud.api.commands.CreateTemplateCmd',NULL,'{\"sessionkey\":\"kQzH8LhRCN9VxZ6zxoT58bjNAtk\\u003d\",\"ctxUserId\":\"2\",\"isPublic\":\"true\",\"ostypeid\":\"103\",\"isfeatured\":\"true\",\"response\":\"json\",\"id\":\"207\",\"snapshotid\":\"3\",\"passwordEnabled\":\"false\",\"name\":\"TemplateFromSnapshot-6-Data\",\"displaytext\":\"TemplateFromSnapshot-6-Data\",\"_\":\"1303792001049\",\"ctxAccountId\":\"2\",\"ctxStartEventId\":\"42\"}',0,0,NULL,1,0,0,'com.cloud.api.response.TemplateResponse/template/{\"id\":207,\"name\":\"TemplateFromSnapshot-6-Data\",\"displaytext\":\"TemplateFromSnapshot-6-Data\",\"ispublic\":true,\"created\":\"2011-04-26T00:27:38-0400\",\"isready\":true,\"passwordenabled\":false,\"isfeatured\":false,\"crossZones\":false,\"ostypeid\":103,\"ostypename\":\"Other (64-bit)\",\"account\":\"nimbus\",\"zoneid\":1,\"zonename\":\"ZONE\",\"domain\":\"CHILD\",\"domainid\":2}',6603060216574,6603060216574,'2011-04-26 04:26:41','2011-04-26 04:27:38','2011-04-26 04:27:31',NULL);
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
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `cluster`
--

LOCK TABLES `cluster` WRITE;
/*!40000 ALTER TABLE `cluster` DISABLE KEYS */;
INSERT INTO `cluster` VALUES (1,'xenCluster164','fb5245e2-250d-0660-9259-0d4a8ecadb9c',1,1,'XenServer','CloudManaged');
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
INSERT INTO `configuration` VALUES ('Advanced','DEFAULT','management-server','account.cleanup.interval','86400','The interval in seconds between cleanup for removed accounts'),('Alert','DEFAULT','management-server','alert.email.addresses',NULL,'Comma separated list of email addresses used for sending alerts.'),('Alert','DEFAULT','management-server','alert.email.sender',NULL,'Sender of alert email (will be in the From header of the email).'),('Alert','DEFAULT','management-server','alert.smtp.host',NULL,'SMTP hostname used for sending out email alerts.'),('Alert','DEFAULT','management-server','alert.smtp.password',NULL,'Password for SMTP authentication (applies only if alert.smtp.useAuth is true).'),('Alert','DEFAULT','management-server','alert.smtp.port','465','Port the SMTP server is listening on.'),('Alert','DEFAULT','management-server','alert.smtp.useAuth',NULL,'If true, use SMTP authentication when sending emails.'),('Alert','DEFAULT','management-server','alert.smtp.username',NULL,'Username for SMTP authentication (applies only if alert.smtp.useAuth is true).'),('Alert','DEFAULT','AgentManager','alert.wait',NULL,'Seconds to wait before alerting on a disconnected agent'),('Advanced','DEFAULT','management-server','allow.public.user.templates','true','If false, users will not be able to create public templates.'),('Usage','DEFAULT','management-server','capacity.check.period','300000','The interval in milliseconds between capacity checks'),('Advanced','DEFAULT','management-server','capacity.skipcounting.hours','3600','Seconds to wait before release VM\'s cpu and memory when VM in stopped state'),('Advanced','DEFAULT','management-server','check.pod.cidrs','true','If true, different pods must belong to different CIDR subnets.'),('Hidden','DEFAULT','management-server','cloud.identifier','733c5af3-fad2-44bf-8a70-6ab841d617ca','A unique identifier for the cloud.'),('Advanced','DEFAULT','AgentManager','cmd.wait','7200','Time to wait for some heavy time-consuming commands'),('Console Proxy','DEFAULT','AgentManager','consoleproxy.capacity.standby','10','The minimal number of console proxy viewer sessions that system is able to serve immediately(standby capacity)'),('Console Proxy','DEFAULT','AgentManager','consoleproxy.capacityscan.interval','30000','The time interval(in millisecond) to scan whether or not system needs more console proxy to ensure minimal standby capacity'),('Console Proxy','DEFAULT','AgentManager','consoleproxy.cmd.port','8001','Console proxy command port that is used to communicate with management server'),('Console Proxy','DEFAULT','AgentManager','consoleproxy.cpu.mhz','512','CPU speed (in MHz) used to create new console proxy VMs'),('Console Proxy','DEFAULT','AgentManager','consoleproxy.disable.rpfilter','true','disable rp_filter on console proxy VM public interface'),('Console Proxy','DEFAULT','AgentManager','consoleproxy.launch.max','10','maximum number of console proxy instances per zone can be launched'),('Console Proxy','DEFAULT','AgentManager','consoleproxy.loadscan.interval','10000','The time interval(in milliseconds) to scan console proxy working-load info'),('Console Proxy','DEFAULT','AgentManager','consoleproxy.ram.size','1024','RAM size (in MB) used to create new console proxy VMs'),('Console Proxy','DEFAULT','AgentManager','consoleproxy.restart','true','Console proxy restart flag, defaulted to true'),('Console Proxy','DEFAULT','AgentManager','consoleproxy.session.max','50','The max number of viewer sessions console proxy is configured to serve for'),('Console Proxy','DEFAULT','AgentManager','consoleproxy.session.timeout','300000','Timeout(in milliseconds) that console proxy tries to maintain a viewer session before it times out the session for no activity'),('Console Proxy','DEFAULT','AgentManager','consoleproxy.url.domain','realhostip.com','Console proxy url domain'),('Advanced','DEFAULT','management-server','control.cidr','169.254.0.0/16','Changes the cidr for the control network traffic.  Defaults to using link local.  Must be unique within pods'),('Advanced','DEFAULT','management-server','control.gateway','169.254.0.1','gateway for the control network traffic'),('Usage','DEFAULT','management-server','cpu.capacity.threshold','0.85','Percentage (as a value between 0 and 1) of cpu utilization above which alerts will be sent about low cpu available.'),('Advanced','DEFAULT','management-server','cpu.overprovisioning.factor','1','Used for CPU overprovisioning calculation; available CPU will be (actualCpuCapacity * cpu.overprovisioning.factor)'),('Advanced','DEFAULT','management-server','direct.attach.network.externalIpAllocator.enabled','false','Direct-attach VMs using external DHCP server'),('Advanced','DEFAULT','management-server','direct.attach.network.externalIpAllocator.url',NULL,'Direct-attach VMs using external DHCP server (API url)'),('Advanced','DEFAULT','management-server','direct.attach.security.groups.enabled','false','Ec2-style distributed firewall for direct-attach VMs'),('Network','DEFAULT','management-server','direct.network.no.default.route','false','Direct Network Dhcp Server should not send a default route'),('Premium','DEFAULT','management-server','enable.usage.server','true','Flag for enabling usage'),('Advanced','DEFAULT','management-server','event.purge.delay','0','Events older than specified number days will be purged'),('Advanced','DEFAULT','UserVmManager','expunge.delay','60','Determines how long to wait before actually expunging destroyed vm. The default value = the default value of expunge.interval'),('Advanced','DEFAULT','UserVmManager','expunge.interval','60','The interval to wait before running the expunge thread.'),('Advanced','DEFAULT','UserVmManager','expunge.workers','1','Number of workers performing expunge '),('Advanced','DEFAULT','management-server','extract.url.cleanup.interval','120','The interval to wait before cleaning up the extract URL\'s '),('Network','DEFAULT','AgentManager','guest.domain.suffix','cloud.internal','Default domain name for vms inside virtualized networks fronted by router'),('Network','DEFAULT','AgentManager','guest.ip.network','10.1.1.1','The network address of the guest virtual network. Virtual machines will be assigned an IP in this subnet.'),('Network','DEFAULT','AgentManager','guest.netmask','255.255.255.0','The netmask of the guest virtual network.'),('Network','DEFAULT','management-server','guest.vlan.bits','12','The number of bits to reserve for the VLAN identifier in the guest subnet.'),('Advanced','DEFAULT','management-server','host','192.168.130.241','The ip address of management server'),('Advanced','DEFAULT','management-server','host.capacity.checker.interval','3600','Seconds to wait before recalculating host\'s capacity'),('Advanced','DEFAULT','management-server','host.capacity.checker.wait','3600','Seconds to wait before starting host capacity background checker'),('Advanced','DEFAULT','AgentManager','host.retry','2','Number of times to retry hosts for creating a volume'),('Advanced','DEFAULT','management-server','host.stats.interval','60000','The interval in milliseconds when host stats are retrieved from agents.'),('Advanced','DEFAULT','management-server','hypervisor.list','KVM,XenServer,VMware','The list of hypervisors that this deployment will use.'),('Hidden','DEFAULT','none','init','true',NULL),('Advanced','DEFAULT','AgentManager','instance.name','VM','Name of the deployment instance.'),('Advanced','DEFAULT','management-server','integration.api.port','8096','Defaul API port'),('Advanced','DEFAULT','HighAvailabilityManager','investigate.retry.interval','60','Time in seconds between VM pings when agent is disconnected'),('Advanced','DEFAULT','management-server','job.cancel.threshold.minutes','60','Time (in minutes) for async-jobs to be forcely cancelled if it has been in process for long'),('Advanced','DEFAULT','management-server','job.expire.minutes','1440','Time (in minutes) for async-jobs to be kept in system'),('Advanced','DEFAULT','management-server','kvm.private.network.device',NULL,'Specify the private bridge on host for private network'),('Advanced','DEFAULT','management-server','kvm.public.network.device',NULL,'Specify the public bridge on host for public network'),('Advanced','DEFAULT','management-server','linkLocalIp.nums','10','The number of link local ip that needed by domR(in power of 2)'),('Advanced','DEFAULT','management-server','max.template.iso.size','50','The maximum size for a downloaded template or ISO (in GB).'),('Usage','DEFAULT','management-server','memory.capacity.threshold','0.85','Percentage (as a value between 0 and 1) of memory utilization above which alerts will be sent about low memory available.'),('Advanced','DEFAULT','HighAvailabilityManager','migrate.retry.interval','120','Time in seconds between migration retries'),('Advanced','DEFAULT','management-server','mount.parent','/var/lib/cloud/mnt','The mount point on the Management Server for Secondary Storage.'),('Advanced','DEFAULT','management-server','network.gc.interval','600','Seconds to wait before checking for networks to shutdown'),('Advanced','DEFAULT','management-server','network.gc.wait','600','Seconds to wait before shutting down a network that\'s not in used'),('Network','DEFAULT','management-server','network.throttling.rate','200','Default data transfer rate in megabits per second allowed.'),('Network','DEFAULT','management-server','open.vswitch.tunnel.network','false','enable/disable open vswitch tunnel network(no vlan)'),('Network','DEFAULT','management-server','open.vswitch.vlan.network','false','enable/disable vlan remapping of  open vswitch network'),('Advanced','DEFAULT','AgentManager','ping.interval','60','Ping interval in seconds'),('Advanced','DEFAULT','AgentManager','ping.timeout','2.5','Multiplier to ping.interval before announcing an agent has timed out'),('Advanced','DEFAULT','AgentManager','port','8250','Port to listen on for agent connection.'),('Usage','DEFAULT','management-server','private.ip.capacity.threshold','0.85','Percentage (as a value between 0 and 1) of private IP address space utilization above which alerts will be sent.'),('Usage','DEFAULT','management-server','public.ip.capacity.threshold','0.85','Percentage (as a value between 0 and 1) of public IP address space utilization above which alerts will be sent.'),('Network','DEFAULT','AgentManager','remote.access.vpn.client.iprange','10.1.2.1-10.1.2.8','The range of ips to be allocated to remote access vpn clients. The first ip in the range is used by the VPN server'),('Network','DEFAULT','AgentManager','remote.access.vpn.psk.length','24','The length of the ipsec preshared key (minimum 8, maximum 256)'),('Network','DEFAULT','AgentManager','remote.access.vpn.user.limit','8','The maximum number of VPN users that can be created per account'),('Advanced','DEFAULT','HighAvailabilityManager','restart.retry.interval','600','Time in seconds between retries to restart a vm'),('Advanced','DEFAULT','management-server','router.cleanup.interval','3600','Time in seconds identifies when to stop router when there are no user vms associated with it'),('Advanced','DEFAULT','none','router.cpu.mhz','500','Default CPU speed (MHz) for router VM.'),('Advanced','DEFAULT','none','router.ram.size','128','Default RAM for router VM in MB.'),('Advanced','DEFAULT','none','router.stats.interval','300','Interval to report router statistics.'),('Advanced','DEFAULT','none','router.template.id','1','Default ID for template.'),('Hidden','DEFAULT','database','schema.level','2.2','The schema level of this database'),('Hidden','DEFAULT','management-server','secondary.storage.vm','true','Deploys a VM per zone to manage secondary storage if true, otherwise secondary storage is mounted on management server'),('Advanced','DEFAULT','management-server','secstorage.allowed.internal.sites','192.168.110.231','Comma separated list of cidrs internal to the datacenter that can host template download servers'),('Hidden','DEFAULT','management-server','secstorage.copy.password','eZ2bbbgpbhwepds','Password used to authenticate zone-to-zone template copy requests'),('Advanced','DEFAULT','management-server','secstorage.encrypt.copy','true','Use SSL method used to encrypt copy traffic between zones'),('Advanced','DEFAULT','management-server','secstorage.ssl.cert.domain','realhostip.com','SSL certificate used to encrypt copy traffic between zones'),('Advanced','DEFAULT','AgentManager','secstorage.vm.cpu.mhz',NULL,'CPU speed (in MHz) used to create new secondary storage vms'),('Advanced','DEFAULT','AgentManager','secstorage.vm.ram.size',NULL,'RAM size (in MB) used to create new secondary storage vms'),('Hidden','DEFAULT','management-server','security.hash.key','7f8fbf79-8229-4080-88d4-d5f3ff3cda9a','for generic key-ed hash'),('Hidden','DEFAULT','management-server','security.singlesignon.key','8Ds-6NbzUTYG7oH-polbMIwEg1MkHJUXFkAvzqYxNZY7eyo5LY7jPAInL-4MaQu16naJrYAoK6be8-yvdhv2Cw','A Single Sign-On key used for logging into the cloud'),('Advanced','DEFAULT','management-server','security.singlesignon.tolerance.millis','300000','The allowable clock difference in milliseconds between when an SSO login request is made and when it is received.'),('Snapshots','DEFAULT','none','snapshot.delta.max','16','max delta snapshots between two full snapshots.'),('Snapshots','DEFAULT','none','snapshot.max.daily','8','Maximum daily snapshots for a volume'),('Snapshots','DEFAULT','none','snapshot.max.hourly','8','Maximum hourly snapshots for a volume'),('Snapshots','DEFAULT','none','snapshot.max.monthly','8','Maximum monthly snapshots for a volume'),('Snapshots','DEFAULT','none','snapshot.max.weekly','8','Maximum weekly snapshots for a volume'),('Snapshots','DEFAULT','none','snapshot.poll.interval','300','The time interval in seconds when the management server polls for snapshots to be scheduled.'),('Hidden','DEFAULT','management-server','ssh.privatekey','-----BEGIN RSA PRIVATE KEY-----\nMIIEogIBAAKCAQEAsSM9TpEiESEkhutTLdfthIrpxsfAJYhb7ZnlgEDh1A5FGsRg\nJ8SjW/THqDRDPk0rFvrqdimVYfs7G7L5CYa2cdAG7rH1k5KNiVeyQmBDjgkbcxtc\n31hhYn6OyK4BxAjetdFTkV5WG6q48Ad0bJm30pB/0UV23mzSguWIBPZlzuDHSiKt\nYuylOM38xGJCJ1cwbmIHefoZp9Kr5/dkSNDGfAApYZ2IFLPoCw1winUcvEkB/PqD\n2bpvlRFw+NQD7fofzMLf6m8wCXp0nBq96jbwtJNoGgWv50WswTAnd4EemcBlxAzN\nRdCbka2ZsqQaoFepTtDwXmzFayl7eOMXKacSbQIBIwKCAQEAksVtT7oU6Z8eQ+eO\nEAqu24kK1+dWEHhMK0T//I2H6je1mdXpRYWr7RtGWCtNqKZW5yesU0cGvuYbCFJ2\nkt1VVvzSi0L+rXlt9XSMYuIMFpnU6liAJsWShM9DG05ZPAdZchPXhxOmbrIG8sRZ\nJsiCXgKzDHtifcfhn6g9gHRi+91wHwVN+zx11rUqDuMkHBrFpyytA1vGfa5hUfO1\n9WVxc6QPejFKsAov7p5Ip5Rbz2irhwQXuSu4PD0UhFfi5YT8PJ0+OprZGiAGBtMS\n/pgN27dfgma27bbVRl1b749LBO3Ur3H2x6RYcR9gDI+GhPlKPvSbk3ECxs74ZDfr\nm+L2CwKBgQDV49wEks4CvtK89G22pPH5BNwqrGP0bWINy4sIDBHmlAHd6RsbSRDU\nl3jWQAL383dDU036/shxnPJfWwSSTdH2JFFOqwZUPbFX0IDe5gCQsDRNHNotPAwD\nlDwC6EvOIgbR83Tg6NqkA7NWkk22SBZTzpchpJSWOqmiXuMjwyeQIQKBgQDUAwtK\nso8LbgYetSkHpxjEc59kRR6N8jQ6T5WsxSEiBCaiuOXU3LFDiI7zJ+s6ZJE2RIRI\n6c7h0rreXzBonM5Irryea3AuDVvXuol4pM5PSqUaekWdEL4aiJUfiJEzuGWTICjd\nAhUi4iPYCFm5Koc5r9TMV1iemNWBLkmzvAOozQKBgBJVXABkWs0JCr+9Lfm2XeIk\n/O23AUDWLPnePx3yZ+8xQf0T+wJWuEy8hrNHULYqz7VQSIM6aPPLn78HzTEcnPfX\nOitQfOKmM8WyyTetUIFuMF5hjwsxCFgUBSTSFSBMDzaRNedkaoMWQpJkT83LqiRw\nyyAkDLxrbaA0BNct/BOrAoGADB1uXAo0D0gdm1rPJQI77ff6esIfAMwC/ASMNcIf\nJoPk81sF0aY2A9vq6VK8/AW/J6wk0Pdq6FUvP+gupuRjjx/thWU5nDtHE6RCXqpU\n7paNF3SzhVFp8uM7uKi31xHZ6yZovCwBNTF+ZB27+/PNNn8TeWQTsUqP23785bL4\n5RMCgYEAwaU8Ps1YFFzZoGK+JAzFGakm9TMXE5CudcRzTwD0SAKSSwfbo7/TZiXI\nWlqBeMfa+VzFBHW96q5bGWGlmNiupstZIoQho8gvzlxdxFtQ0SpeHWxFK8fKUGwJ\nEyHpl6yLsJQagMjO9D2jVsluzqhTI+TPWYj9X+e9ABtPIgKCEdw=\n-----END RSA PRIVATE KEY-----','Private key for the entire CloudStack'),('Hidden','DEFAULT','management-server','ssh.publickey','ssh-rsa AAAAB3NzaC1yc2EAAAABIwAAAQEAsSM9TpEiESEkhutTLdfthIrpxsfAJYhb7ZnlgEDh1A5FGsRgJ8SjW/THqDRDPk0rFvrqdimVYfs7G7L5CYa2cdAG7rH1k5KNiVeyQmBDjgkbcxtc31hhYn6OyK4BxAjetdFTkV5WG6q48Ad0bJm30pB/0UV23mzSguWIBPZlzuDHSiKtYuylOM38xGJCJ1cwbmIHefoZp9Kr5/dkSNDGfAApYZ2IFLPoCw1winUcvEkB/PqD2bpvlRFw+NQD7fofzMLf6m8wCXp0nBq96jbwtJNoGgWv50WswTAnd4EemcBlxAzNRdCbka2ZsqQaoFepTtDwXmzFayl7eOMXKacSbQ== cloud@i-11-246304-VM','Public key for the entire CloudStack'),('Advanced','DEFAULT','AgentManager','start.retry','10','Number of times to retry create and start commands'),('Advanced','DEFAULT','HighAvailabilityManager','stop.retry.interval','600','Time in seconds between retries to stop or destroy a vm'),('Usage','DEFAULT','management-server','storage.allocated.capacity.threshold','0.85','Percentage (as a value between 0 and 1) of allocated storage utilization above which alerts will be sent about low storage available.'),('Usage','DEFAULT','management-server','storage.capacity.threshold','0.85','Percentage (as a value between 0 and 1) of storage utilization above which alerts will be sent about low storage available.'),('Advanced','DEFAULT','none','storage.cleanup.enabled','true','Enables/disables the storage cleanup thread.'),('Advanced','DEFAULT','none','storage.cleanup.interval','86400','The interval to wait before running the storage cleanup thread.'),('Storage','DEFAULT','management-server','storage.max.volume.size','2000','The maximum size for a volume in GB.'),('Storage','DEFAULT','StorageAllocator','storage.overprovisioning.factor','2','Used for storage overprovisioning calculation; available storage will be (actualStorageSize * storage.overprovisioning.factor)'),('Storage','DEFAULT','management-server','storage.stats.interval','60000','The interval in milliseconds when storage stats (per host) are retrieved from agents.'),('Advanced','DEFAULT','management-server','system.vm.auto.reserve.capacity','true','Indicates whether or not to automatically reserver system VM standby capacity.'),('Advanced','DEFAULT','management-server','system.vm.use.local.storage','false','Indicates whether to use local storage pools or shared storage pools for system VMs.'),('Storage','DEFAULT','AgentManager','total.retries','4','The number of times each command sent to a host should be retried in case of failure.'),('Advanced','DEFAULT','AgentManager','update.wait','600','Time to wait before alerting on a updating agent'),('Premium','DEFAULT','management-server','usage.execution.timezone',NULL,'The timezone to use for usage job execution time'),('Premium','DEFAULT','management-server','usage.stats.job.aggregation.range','1440','The range of time for aggregating the user statistics specified in minutes (e.g. 1440 for daily, 60 for hourly.'),('Premium','DEFAULT','management-server','usage.stats.job.exec.time','00:15','The time at which the usage statistics aggregation job will run as an HH24:MM time, e.g. 00:30 to run at 12:30am.'),('Premium','DEFAULT','management-server','use.local.storage','false','Should we use the local storage if it\'s available?'),('Advanced','DEFAULT','management-server','vm.allocation.algorithm','random','If \'random\', hosts within a pod will be randomly considered for VM/volume allocation. If \'firstfit\', they will be considered on a first-fit basis.'),('Advanced','DEFAULT','management-server','vm.op.cancel.interval','3600','Seconds to wait before cancelling a operation'),('Advanced','DEFAULT','management-server','vm.op.cleanup.interval','86400','Interval to run the thread that cleans up the vm operations in seconds'),('Advanced','DEFAULT','management-server','vm.op.cleanup.wait','3600','Seconds to wait before cleanuping up any vm work items'),('Advanced','DEFAULT','management-server','vm.op.lock.state.retry','5','Times to retry locking the state of a VM for operations'),('Advanced','DEFAULT','management-server','vm.op.wait.interval','120','Seconds to wait before checking if a previous operation has succeeded'),('Advanced','DEFAULT','management-server','vm.stats.interval','60000','The interval in milliseconds when vm stats are retrieved from agents.'),('Advanced','DEFAULT','management-server','vm.tranisition.wait.interval','3600','Seconds to wait before taking over a VM in transition state'),('Advanced','DEFAULT','management-server','vmware.guest.vswitch',NULL,'Specify the vSwitch on host for guest network'),('Advanced','DEFAULT','management-server','vmware.private.vswitch',NULL,'Specify the vSwitch on host for private network'),('Advanced','DEFAULT','management-server','vmware.public.vswitch',NULL,'Specify the vSwitch on host for public network'),('Advanced','DEFAULT','AgentManager','wait','1800','Time to wait for control commands to return'),('Advanced','DEFAULT','AgentManager','workers','5','Number of worker threads.'),('Advanced','DEFAULT','management-server','xen.bond.storage.nics',NULL,'Attempt to bond the two networks if found'),('Hidden','DEFAULT','management-server','xen.create.pools.in.pod','false','Should we automatically add XenServers into pools that are inside a Pod'),('Advanced','DEFAULT','management-server','xen.guest.network.device',NULL,'Specify for guest network name label'),('Advanced','DEFAULT','management-server','xen.heartbeat.interval','60','heartbeat to use when implementing XenServer Self Fencing'),('Advanced','DEFAULT','management-server','xen.max.product.version','5.6.0','Maximum XenServer version'),('Advanced','DEFAULT','management-server','xen.max.version','3.4.2','Maximum Xen version'),('Advanced','DEFAULT','management-server','xen.max.xapi.version','1.3','Maximum Xapi Tool Stack version'),('Advanced','DEFAULT','management-server','xen.min.product.version','0.1.1','Minimum XenServer version'),('Advanced','DEFAULT','management-server','xen.min.version','3.3.1','Minimum Xen version'),('Advanced','DEFAULT','management-server','xen.min.xapi.version','1.3','Minimum Xapi Tool Stack version'),('Network','DEFAULT','management-server','xen.private.network.device',NULL,'Specify when the private network name is different'),('Network','DEFAULT','management-server','xen.public.network.device',NULL,'[ONLY IF THE PUBLIC NETWORK IS ON A DEDICATED NIC]:The network name label of the physical device dedicated to the public network on a XenServer host'),('Advanced','DEFAULT','management-server','xen.setup.multipath','false','Setup the host to do multipath'),('Network','DEFAULT','management-server','xen.storage.network.device1','cloud-stor1','Specify when there are storage networks'),('Network','DEFAULT','management-server','xen.storage.network.device2','cloud-stor2','Specify when there are storage networks');
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
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `console_proxy`
--

LOCK TABLES `console_proxy` WRITE;
/*!40000 ALTER TABLE `console_proxy` DISABLE KEYS */;
INSERT INTO `console_proxy` VALUES (1,'06:4a:74:00:00:1a','192.168.164.125','255.255.255.0',0,0,'2011-04-26 22:15:47','{\"connections\":[]}\n');
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
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `data_center`
--

LOCK TABLES `data_center` WRITE;
/*!40000 ALTER TABLE `data_center` DISABLE KEYS */;
INSERT INTO `data_center` VALUES (1,'ZONE',NULL,'72.52.126.11','72.52.126.12','192.168.110.254','192.168.110.253',NULL,NULL,NULL,'02:00:00:00:00:01',32,NULL,NULL,NULL,'Basic','VirtualRouter','VirtualRouter','VirtualRouter','VirtualRouter','VirtualRouter','VirtualRouter','VirtualRouter',1);
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
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `disk_offering`
--

LOCK TABLES `disk_offering` WRITE;
/*!40000 ALTER TABLE `disk_offering` DISABLE KEYS */;
INSERT INTO `disk_offering` VALUES (1,NULL,'Small Instance','Small Instance, $0.05 per hour',0,'Service',NULL,0,0,NULL,0,1,NULL,'2011-04-26 01:24:13'),(2,NULL,'Medium Instance','Medium Instance, $0.10 per hour',0,'Service',NULL,0,0,NULL,0,1,NULL,'2011-04-26 01:24:13'),(3,1,'Small','Small Disk, 5 GB',5120,'Disk',NULL,0,0,NULL,0,0,NULL,'2011-04-26 01:24:13'),(4,1,'Medium','Medium Disk, 20 GB',20480,'Disk',NULL,0,0,NULL,0,0,NULL,'2011-04-26 01:24:13'),(5,1,'Large','Large Disk, 100 GB',102400,'Disk',NULL,0,0,NULL,0,0,NULL,'2011-04-26 01:24:13'),(6,NULL,'System Offering For Console Proxy',NULL,0,'Service',NULL,1,0,'Cloud.com-ConsoleProxy',1,1,NULL,'2011-04-26 01:24:30'),(7,NULL,'System Offering For Secondary Storage VM',NULL,0,'Service',NULL,1,0,'Cloud.com-SecondaryStorage',1,1,NULL,'2011-04-26 01:24:30'),(8,NULL,'System Offering For Software Router',NULL,0,'Service',NULL,1,0,'Cloud.Com-SoftwareRouter',1,1,NULL,'2011-04-26 01:24:30'),(9,NULL,'Little Instance','Little Instance',0,'Service',NULL,0,0,NULL,0,1,NULL,'2011-04-26 02:28:50'),(10,1,'Little','Little Disk, 1 GB',1024,'Disk',NULL,0,0,NULL,0,0,NULL,'2011-04-26 02:29:05');
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
  PRIMARY KEY  (`id`),
  UNIQUE KEY `parent` (`parent`,`name`,`removed`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `domain`
--

LOCK TABLES `domain` WRITE;
/*!40000 ALTER TABLE `domain` DISABLE KEYS */;
INSERT INTO `domain` VALUES (1,NULL,'ROOT',2,'/',0,1,2,NULL),(2,1,'CHILD',2,'/CHILD/',1,0,1,NULL);
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
INSERT INTO `domain_router` VALUES (4,0,NULL,NULL,NULL,NULL,NULL,NULL,'192.168.164.107',203,'DHCP_USERDATA');
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
) ENGINE=InnoDB AUTO_INCREMENT=43 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `event`
--

LOCK TABLES `event` WRITE;
/*!40000 ALTER TABLE `event` DISABLE KEYS */;
INSERT INTO `event` VALUES (1,'VM.CREATE','Created','Successfully created entity for deploying Vm. Vm Id: 3',2,2,'2011-04-26 03:16:56','INFO',0,NULL),(2,'VM.CREATE','Scheduled','Scheduled async job for starting Vm. Vm Id: 3',2,2,'2011-04-26 03:16:56','INFO',1,NULL),(3,'VM.CREATE','Started','Starting job for starting Vm. Vm Id: 3',2,2,'2011-04-26 03:16:57','INFO',1,NULL),(4,'VM.CREATE','Completed','Successfully completed starting Vm. Vm Id: 3',2,2,'2011-04-26 03:18:43','INFO',1,NULL),(5,'VM.CREATE','Created','Successfully created entity for deploying Vm. Vm Id: 5',2,2,'2011-04-26 03:35:28','INFO',0,NULL),(6,'VM.CREATE','Scheduled','Scheduled async job for starting Vm. Vm Id: 5',2,2,'2011-04-26 03:35:28','INFO',5,NULL),(7,'VM.CREATE','Started','Starting job for starting Vm. Vm Id: 5',2,2,'2011-04-26 03:35:28','INFO',5,NULL),(8,'VM.CREATE','Completed','Successfully completed starting Vm. Vm Id: 5',2,2,'2011-04-26 03:35:36','INFO',5,NULL),(9,'USER.LOGOUT','Completed','user has logged out',2,2,'2011-04-26 03:36:28','INFO',0,NULL),(10,'VM.CREATE','Created','Successfully created entity for deploying Vm. Vm Id: 6',3,3,'2011-04-26 03:37:01','INFO',0,NULL),(11,'VM.CREATE','Scheduled','Scheduled async job for starting Vm. Vm Id: 6',3,3,'2011-04-26 03:37:01','INFO',10,NULL),(12,'VM.CREATE','Started','Starting job for starting Vm. Vm Id: 6',3,3,'2011-04-26 03:37:01','INFO',10,NULL),(13,'VM.CREATE','Completed','Successfully completed starting Vm. Vm Id: 6',3,3,'2011-04-26 03:37:10','INFO',10,NULL),(14,'VM.CREATE','Created','Successfully created entity for deploying Vm. Vm Id: 7',3,3,'2011-04-26 03:37:16','INFO',0,NULL),(15,'VM.CREATE','Scheduled','Scheduled async job for starting Vm. Vm Id: 7',3,3,'2011-04-26 03:37:16','INFO',14,NULL),(16,'VM.CREATE','Started','Starting job for starting Vm. Vm Id: 7',3,3,'2011-04-26 03:37:16','INFO',14,NULL),(17,'VM.CREATE','Completed','Successfully completed starting Vm. Vm Id: 7',3,3,'2011-04-26 03:37:25','INFO',14,NULL),(18,'USER.LOGOUT','Completed','user has logged out',3,3,'2011-04-26 03:37:34','INFO',0,NULL),(19,'SNAPSHOT.CREATE','Scheduled','Scheduled async job for creating snapshot for volume: 9',2,3,'2011-04-26 04:05:46','INFO',0,NULL),(20,'SNAPSHOT.CREATE','Started','Starting job for creating snapshot',2,3,'2011-04-26 04:05:46','INFO',19,NULL),(21,'SNAPSHOT.CREATE','Scheduled','Scheduled async job for creating snapshot for volume: 7',2,3,'2011-04-26 04:05:50','INFO',0,NULL),(22,'SNAPSHOT.CREATE','Started','Starting job for creating snapshot',2,3,'2011-04-26 04:05:50','INFO',21,NULL),(23,'SNAPSHOT.CREATE','Scheduled','Scheduled async job for creating snapshot for volume: 8',2,3,'2011-04-26 04:05:55','INFO',0,NULL),(24,'SNAPSHOT.CREATE','Started','Starting job for creating snapshot',2,3,'2011-04-26 04:05:55','INFO',23,NULL),(25,'SNAPSHOT.CREATE','Scheduled','Scheduled async job for creating snapshot for volume: 6',2,2,'2011-04-26 04:05:59','INFO',0,NULL),(26,'SNAPSHOT.CREATE','Started','Starting job for creating snapshot',2,2,'2011-04-26 04:05:59','INFO',25,NULL),(27,'SNAPSHOT.CREATE','Scheduled','Scheduled async job for creating snapshot for volume: 3',2,2,'2011-04-26 04:06:03','INFO',0,NULL),(28,'SNAPSHOT.CREATE','Started','Starting job for creating snapshot',2,2,'2011-04-26 04:06:03','INFO',27,NULL),(29,'SNAPSHOT.CREATE','Scheduled','Scheduled async job for creating snapshot for volume: 4',2,2,'2011-04-26 04:06:07','INFO',0,NULL),(30,'SNAPSHOT.CREATE','Started','Starting job for creating snapshot',2,2,'2011-04-26 04:06:07','INFO',29,NULL),(31,'SNAPSHOT.CREATE','Completed','Successfully completed creating snapshot',2,3,'2011-04-26 04:06:43','INFO',23,NULL),(32,'SNAPSHOT.CREATE','Completed','Successfully completed creating snapshot',2,2,'2011-04-26 04:07:10','INFO',29,NULL),(33,'SNAPSHOT.CREATE','Completed','Successfully completed creating snapshot',2,2,'2011-04-26 04:11:25','INFO',25,NULL),(34,'SNAPSHOT.CREATE','Completed','Successfully completed creating snapshot',2,3,'2011-04-26 04:11:27','INFO',19,NULL),(35,'SNAPSHOT.CREATE','Completed','Successfully completed creating snapshot',2,2,'2011-04-26 04:11:30','INFO',27,NULL),(36,'SNAPSHOT.CREATE','Completed','Successfully completed creating snapshot',2,3,'2011-04-26 04:11:30','INFO',21,NULL),(37,'TEMPLATE.CREATE','Scheduled','Scheduled async job for creating template: TemplateFromSnapshot-1',2,2,'2011-04-26 04:24:49','INFO',0,NULL),(38,'TEMPLATE.CREATE','Scheduled','Scheduled async job for creating template: TemplateFromSnapshot-2',2,2,'2011-04-26 04:25:02','INFO',0,NULL),(39,'TEMPLATE.CREATE','Scheduled','Scheduled async job for creating template: TemplateFromSnapshot-3',2,3,'2011-04-26 04:25:16','INFO',0,NULL),(40,'TEMPLATE.CREATE','Scheduled','Scheduled async job for creating template: TemplateFromSnapshot-4',2,3,'2011-04-26 04:25:30','INFO',0,NULL),(41,'TEMPLATE.CREATE','Scheduled','Scheduled async job for creating template: TemplateFromSnapshot-5-Data',2,2,'2011-04-26 04:26:27','INFO',0,NULL),(42,'TEMPLATE.CREATE','Scheduled','Scheduled async job for creating template: TemplateFromSnapshot-6-Data',2,3,'2011-04-26 04:26:41','INFO',0,NULL);
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
  `is_static_nat` int(1) unsigned NOT NULL default '0' COMMENT '1 if firewall rule is one to one nat rule',
  `xid` char(40) NOT NULL COMMENT 'external id',
  `created` datetime default NULL COMMENT 'Date created',
  PRIMARY KEY  (`id`),
  KEY `fk_firewall_rules__ip_address_id` (`ip_address_id`),
  KEY `fk_firewall_rules__network_id` (`network_id`),
  KEY `fk_firewall_rules__account_id` (`account_id`),
  KEY `fk_firewall_rules__domain_id` (`domain_id`),
  CONSTRAINT `fk_firewall_rules__ip_address_id` FOREIGN KEY (`ip_address_id`) REFERENCES `user_ip_address` (`id`),
  CONSTRAINT `fk_firewall_rules__network_id` FOREIGN KEY (`network_id`) REFERENCES `networks` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_firewall_rules__account_id` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_firewall_rules__domain_id` FOREIGN KEY (`domain_id`) REFERENCES `domain` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `firewall_rules`
--

LOCK TABLES `firewall_rules` WRITE;
/*!40000 ALTER TABLE `firewall_rules` DISABLE KEYS */;
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
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `host`
--

LOCK TABLES `host` WRITE;
/*!40000 ALTER TABLE `host` DISABLE KEYS */;
INSERT INTO `host` VALUES (1,'nfs://192.168.110.232/export/home/chandan/pigeon/secondary224','Up','SecondaryStorage','192.168.110.232','255.255.255.0','06:13:1e:00:00:05','192.168.164.104','255.255.255.0','06:13:1e:00:00:05',NULL,NULL,NULL,NULL,'192.168.164.124','255.255.255.0','06:79:fe:00:00:19',NULL,1,1,NULL,NULL,'nfs://192.168.110.232/export/home/chandan/pigeon/secondary224',NULL,'None',0,NULL,'2.2','/mnt/SecStorage/6a304996',1930519904256,NULL,'nfs://192.168.110.232/export/home/chandan/pigeon/secondary224',1,0,0,1273297070,6603060216574,'2011-04-26 03:40:34','2011-04-26 02:55:46',NULL),(2,'xenserver-chandan-164-2','Up','Routing','192.168.164.2','255.255.255.0','bc:30:5b:d4:23:54','192.168.164.2','255.255.255.0','bc:30:5b:d4:23:54','192.168.164.2','bc:30:5b:d4:23:54','255.255.255.0',1,NULL,NULL,NULL,NULL,1,1,4,2260,'iqn.2005-03.org.open-iscsi:2f4cd84bfa8f',NULL,'XenServer',3701658240,'com.cloud.hypervisor.xen.resource.XenServer56Resource','2.2',NULL,NULL,'xen-3.0-x86_64 , xen-3.0-x86_32p , hvm-3.0-x86_32 , hvm-3.0-x86_32p , hvm-3.0-x86_64','81e61b14-2248-4cc6-81da-1047b57d680b',1,1,0,1273297070,6603060216574,'2011-04-26 03:40:34','2011-04-26 02:57:24',NULL),(3,'v-1-VM','Up','ConsoleProxy','192.168.164.100','255.255.255.0','06:97:9e:00:00:01','192.168.164.100','255.255.255.0','06:97:9e:00:00:01',NULL,NULL,NULL,NULL,'192.168.164.125','255.255.255.0','06:4a:74:00:00:1a',80,1,1,NULL,NULL,'NoIqn',NULL,NULL,0,NULL,'2.2',NULL,NULL,NULL,'Proxy.1-ConsoleProxyResource',1,0,0,1273297070,6603060216574,'2011-04-26 03:40:34','2011-04-26 03:01:15',NULL),(4,'xenserver-chandan-164-3','Up','Routing','192.168.164.3','255.255.255.0','bc:30:5b:d1:07:90','192.168.164.3','255.255.255.0','bc:30:5b:d1:07:90','192.168.164.3','bc:30:5b:d1:07:90','255.255.255.0',1,NULL,NULL,NULL,NULL,1,1,4,2260,'iqn.2011-04.com.example:a3522f75',NULL,'XenServer',3701658240,'com.cloud.hypervisor.xen.resource.XenServer56Resource','2.2',NULL,NULL,'xen-3.0-x86_64 , xen-3.0-x86_32p , hvm-3.0-x86_32 , hvm-3.0-x86_32p , hvm-3.0-x86_64','5df00ccf-446c-40dd-887a-332b2a7eb701',1,1,0,1273297033,6603060216574,NULL,'2011-04-26 21:28:00',NULL);
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
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `host_details`
--

LOCK TABLES `host_details` WRITE;
/*!40000 ALTER TABLE `host_details` DISABLE KEYS */;
INSERT INTO `host_details` VALUES (1,1,'mount.parent','dummy'),(2,1,'mount.path','dummy'),(3,1,'orig.url','nfs://192.168.110.232/export/home/chandan/pigeon/secondary224'),(4,2,'public.network.device','Pool-wide network associated with eth0'),(5,2,'private.network.device','Pool-wide network associated with eth0'),(6,2,'com.cloud.network.Networks.RouterPrivateIpStrategy','DcGlobal'),(7,2,'Hypervisor.Version','3.4.2'),(8,2,'Host.OS','XenServer'),(9,2,'Host.OS.Kernel.Version','2.6.27.42-0.1.1.xs5.6.0.44.111158xen'),(10,2,'wait','1800'),(11,2,'storage.network.device2','cloud-stor2'),(12,2,'password','password'),(13,2,'storage.network.device1','cloud-stor1'),(14,2,'url','192.168.164.2'),(15,2,'username','root'),(16,2,'guest.network.device','Pool-wide network associated with eth0'),(17,2,'can_bridge_firewall','false'),(18,2,'Host.OS.Version','5.6.0'),(19,4,'public.network.device','Pool-wide network associated with eth0'),(20,4,'private.network.device','Pool-wide network associated with eth0'),(21,4,'com.cloud.network.Networks.RouterPrivateIpStrategy','DcGlobal'),(22,4,'Hypervisor.Version','3.4.2'),(23,4,'Host.OS','XenServer'),(24,4,'Host.OS.Kernel.Version','2.6.27.42-0.1.1.xs5.6.0.44.111158xen'),(25,4,'wait','1800'),(26,4,'storage.network.device2','cloud-stor2'),(27,4,'password','password'),(28,4,'storage.network.device1','cloud-stor1'),(29,4,'url','192.168.164.3'),(30,4,'username','root'),(31,4,'guest.network.device','Pool-wide network associated with eth0'),(32,4,'can_bridge_firewall','false'),(33,4,'Host.OS.Version','5.6.0');
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
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `host_pod_ref`
--

LOCK TABLES `host_pod_ref` WRITE;
/*!40000 ALTER TABLE `host_pod_ref` DISABLE KEYS */;
INSERT INTO `host_pod_ref` VALUES (1,'POD1',1,'192.168.164.1','192.168.164.0',24,'192.168.164.100-192.168.164.104',1);
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
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `instance_group`
--

LOCK TABLES `instance_group` WRITE;
/*!40000 ALTER TABLE `instance_group` DISABLE KEYS */;
INSERT INTO `instance_group` VALUES (1,2,'Admin-VM-1',NULL,'2011-04-26 03:16:56'),(2,2,'Admin-VM-2',NULL,'2011-04-26 03:35:28'),(3,3,'Nimbus-VM-1',NULL,'2011-04-26 03:37:01'),(4,3,'Nimbus-VM-2',NULL,'2011-04-26 03:37:16');
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
  CONSTRAINT `fk_instance_group_vm_map___instance_id` FOREIGN KEY (`instance_id`) REFERENCES `user_vm` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_instance_group_vm_map___group_id` FOREIGN KEY (`group_id`) REFERENCES `instance_group` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `instance_group_vm_map`
--

LOCK TABLES `instance_group_vm_map` WRITE;
/*!40000 ALTER TABLE `instance_group_vm_map` DISABLE KEYS */;
INSERT INTO `instance_group_vm_map` VALUES (1,1,3),(2,2,5),(3,3,6),(4,4,7);
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
  CONSTRAINT `fk_load_balancer_vm_map__load_balancer_id` FOREIGN KEY (`load_balancer_id`) REFERENCES `load_balancing_rules` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_load_balancer_vm_map__instance_id` FOREIGN KEY (`instance_id`) REFERENCES `vm_instance` (`id`) ON DELETE CASCADE
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
INSERT INTO `mshost` VALUES (1,6603060216574,'i-11-246304-VM','2.2','192.168.130.241',9090,'2011-04-26 22:16:47',NULL,0);
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
INSERT INTO `network_offerings` VALUES (1,'System-Public-Network','System Offering for System-Public-Network',NULL,NULL,NULL,'Public',NULL,1,0,NULL,'2011-04-26 01:24:13',NULL,0,'Required',0,0,0,0,0,0,0),(2,'System-Management-Network','System Offering for System-Management-Network',NULL,NULL,NULL,'Management',NULL,1,0,NULL,'2011-04-26 01:24:14',NULL,0,'Required',0,0,0,0,0,0,0),(3,'System-Control-Network','System Offering for System-Control-Network',NULL,NULL,NULL,'Control',NULL,1,0,NULL,'2011-04-26 01:24:14',NULL,0,'Required',0,0,0,0,0,0,0),(4,'System-Storage-Network','System Offering for System-Storage-Network',NULL,NULL,NULL,'Storage',NULL,1,0,NULL,'2011-04-26 01:24:14',NULL,0,'Required',0,0,0,0,0,0,0),(5,'System-Guest-Network','System Offering for System-Guest-Network',NULL,NULL,NULL,'Guest',NULL,1,0,NULL,'2011-04-26 01:24:14',NULL,0,'Required',0,0,0,0,0,0,0),(6,'DefaultVirtualizedNetworkOffering','Virtual Vlan',NULL,NULL,NULL,'Guest',NULL,0,0,NULL,'2011-04-26 01:24:14',NULL,1,'Required',0,0,0,0,0,0,0),(7,'DefaultDirectNetworkOffering','Direct',NULL,NULL,NULL,'Public',NULL,0,0,NULL,'2011-04-26 01:24:14',NULL,1,'Required',0,0,0,0,0,0,0);
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
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=204 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `networks`
--

LOCK TABLES `networks` WRITE;
/*!40000 ALTER TABLE `networks` DISABLE KEYS */;
INSERT INTO `networks` VALUES (200,NULL,NULL,'Management','Native',NULL,NULL,NULL,'Static',2,1,'PodBasedNetworkGuru','Setup',200,1,1,NULL,NULL,NULL,0,NULL,1,NULL,NULL,0,'2011-04-26 02:39:56',NULL),(201,NULL,NULL,'Control','LinkLocal',NULL,'169.254.0.1','169.254.0.0/16','Static',3,1,'ControlNetworkGuru','Setup',201,1,1,NULL,NULL,NULL,0,NULL,1,NULL,NULL,0,'2011-04-26 02:39:56',NULL),(202,NULL,NULL,'Storage','Native',NULL,NULL,NULL,'Static',4,1,'PodBasedNetworkGuru','Setup',202,1,1,NULL,NULL,NULL,0,NULL,1,NULL,NULL,0,'2011-04-26 02:39:56',NULL),(203,NULL,NULL,'Guest','Native',NULL,NULL,NULL,'Dhcp',5,1,'DirectPodBasedNetworkGuru','Setup',203,1,1,NULL,NULL,NULL,0,'Direct',1,NULL,NULL,1,'2011-04-26 02:39:56',NULL);
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
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`),
  KEY `fk_nics__instance_id` (`instance_id`),
  KEY `fk_nics__networks_id` (`network_id`),
  CONSTRAINT `fk_nics__instance_id` FOREIGN KEY (`instance_id`) REFERENCES `vm_instance` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_nics__networks_id` FOREIGN KEY (`network_id`) REFERENCES `networks` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `nics`
--

LOCK TABLES `nics` WRITE;
/*!40000 ALTER TABLE `nics` DISABLE KEYS */;
INSERT INTO `nics` VALUES (1,2,'06:79:fe:00:00:19','192.168.164.124','255.255.255.0','192.168.164.1','Ip4','vlan://untagged',203,'Dhcp','Reserved','Start','DirectPodBasedNetworkGuru','b1d47f5a-edc8-4246-bcbb-e79b0e1d9007',2,'2011-04-26 03:00:11','ec2://untagged',NULL,1,'2011-04-26 02:59:03',NULL),(2,2,'0e:00:a9:fe:02:55','169.254.2.85','255.255.0.0','169.254.0.1','Ip4',NULL,201,NULL,'Reserved','Start','ControlNetworkGuru','b1d47f5a-edc8-4246-bcbb-e79b0e1d9007',0,'2011-04-26 03:00:12',NULL,NULL,0,'2011-04-26 02:59:03',NULL),(3,2,'06:13:1e:00:00:05','192.168.164.104','255.255.255.0','192.168.164.1','Ip4',NULL,200,NULL,'Reserved','Start','PodBasedNetworkGuru','b1d47f5a-edc8-4246-bcbb-e79b0e1d9007',1,'2011-04-26 03:00:12',NULL,NULL,0,'2011-04-26 02:59:03',NULL),(4,1,'06:4a:74:00:00:1a','192.168.164.125','255.255.255.0','192.168.164.1','Ip4','vlan://untagged',203,'Dhcp','Reserved','Start','DirectPodBasedNetworkGuru','591959ae-f08c-4b7c-86ce-5ece82b4e37d',2,'2011-04-26 03:00:12','ec2://untagged',NULL,1,'2011-04-26 02:59:03',NULL),(5,1,'0e:00:a9:fe:00:60','169.254.0.96','255.255.0.0','169.254.0.1','Ip4',NULL,201,NULL,'Reserved','Start','ControlNetworkGuru','591959ae-f08c-4b7c-86ce-5ece82b4e37d',0,'2011-04-26 03:00:12',NULL,NULL,0,'2011-04-26 02:59:03',NULL),(6,1,'06:97:9e:00:00:01','192.168.164.100','255.255.255.0','192.168.164.1','Ip4',NULL,200,NULL,'Reserved','Start','PodBasedNetworkGuru','591959ae-f08c-4b7c-86ce-5ece82b4e37d',1,'2011-04-26 03:00:12',NULL,NULL,0,'2011-04-26 02:59:03',NULL),(7,3,'06:93:bc:00:00:18','192.168.164.123','255.255.255.0','192.168.164.1','Ip4','vlan://untagged',203,NULL,'Reserved','Start','DirectPodBasedNetworkGuru','cee67e6c-bc23-4f5e-98ba-254c1639f18c',0,'2011-04-26 03:18:01','ec2://untagged',NULL,1,'2011-04-26 03:16:56',NULL),(8,4,'06:b5:60:00:00:08','192.168.164.107','255.255.255.0','192.168.164.1','Ip4','vlan://untagged',203,'Dhcp','Reserved','Start','DirectPodBasedNetworkGuru','a8e10db0-8baf-4771-875c-efacd802005b',0,'2011-04-26 03:18:01','ec2://untagged',NULL,1,'2011-04-26 03:18:01',NULL),(9,4,'0e:00:a9:fe:00:9a','169.254.0.154','255.255.0.0','169.254.0.1','Ip4',NULL,201,NULL,'Reserved','Start','ControlNetworkGuru','a8e10db0-8baf-4771-875c-efacd802005b',1,'2011-04-26 03:18:02',NULL,NULL,0,'2011-04-26 03:18:01',NULL),(10,5,'06:c8:20:00:00:1d','192.168.164.128','255.255.255.0','192.168.164.1','Ip4','vlan://untagged',203,NULL,'Reserved','Start','DirectPodBasedNetworkGuru','0f2ce974-b4c8-43ef-b190-3323f1417ee0',0,'2011-04-26 03:35:29','ec2://untagged',NULL,1,'2011-04-26 03:35:28',NULL),(11,6,'06:93:80:00:00:17','192.168.164.122','255.255.255.0','192.168.164.1','Ip4','vlan://untagged',203,NULL,'Reserved','Start','DirectPodBasedNetworkGuru','f7bf134b-1a90-447b-ae22-1567acf80773',0,'2011-04-26 03:37:02','ec2://untagged',NULL,1,'2011-04-26 03:37:01',NULL),(12,7,'06:96:f6:00:00:1b','192.168.164.126','255.255.255.0','192.168.164.1','Ip4','vlan://untagged',203,NULL,'Reserved','Start','DirectPodBasedNetworkGuru','3587e6cc-329d-4f5f-a68c-74d9547e882e',0,'2011-04-26 03:37:17','ec2://untagged',NULL,1,'2011-04-26 03:37:16',NULL);
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
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `op_dc_ip_address_alloc`
--

LOCK TABLES `op_dc_ip_address_alloc` WRITE;
/*!40000 ALTER TABLE `op_dc_ip_address_alloc` DISABLE KEYS */;
INSERT INTO `op_dc_ip_address_alloc` VALUES (1,'192.168.164.100',1,1,6,'591959ae-f08c-4b7c-86ce-5ece82b4e37d','2011-04-26 03:00:12',1),(2,'192.168.164.101',1,1,NULL,NULL,NULL,2),(3,'192.168.164.102',1,1,NULL,NULL,NULL,3),(4,'192.168.164.103',1,1,NULL,NULL,NULL,4),(5,'192.168.164.104',1,1,3,'b1d47f5a-edc8-4246-bcbb-e79b0e1d9007','2011-04-26 03:00:12',5);
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
) ENGINE=InnoDB AUTO_INCREMENT=1022 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `op_dc_link_local_ip_address_alloc`
--

LOCK TABLES `op_dc_link_local_ip_address_alloc` WRITE;
/*!40000 ALTER TABLE `op_dc_link_local_ip_address_alloc` DISABLE KEYS */;
INSERT INTO `op_dc_link_local_ip_address_alloc` VALUES (1,'169.254.0.2',1,1,NULL,NULL,NULL),(2,'169.254.0.3',1,1,NULL,NULL,NULL),(3,'169.254.0.4',1,1,NULL,NULL,NULL),(4,'169.254.0.5',1,1,NULL,NULL,NULL),(5,'169.254.0.6',1,1,NULL,NULL,NULL),(6,'169.254.0.7',1,1,NULL,NULL,NULL),(7,'169.254.0.8',1,1,NULL,NULL,NULL),(8,'169.254.0.9',1,1,NULL,NULL,NULL),(9,'169.254.0.10',1,1,NULL,NULL,NULL),(10,'169.254.0.11',1,1,NULL,NULL,NULL),(11,'169.254.0.12',1,1,NULL,NULL,NULL),(12,'169.254.0.13',1,1,NULL,NULL,NULL),(13,'169.254.0.14',1,1,NULL,NULL,NULL),(14,'169.254.0.15',1,1,NULL,NULL,NULL),(15,'169.254.0.16',1,1,NULL,NULL,NULL),(16,'169.254.0.17',1,1,NULL,NULL,NULL),(17,'169.254.0.18',1,1,NULL,NULL,NULL),(18,'169.254.0.19',1,1,NULL,NULL,NULL),(19,'169.254.0.20',1,1,NULL,NULL,NULL),(20,'169.254.0.21',1,1,NULL,NULL,NULL),(21,'169.254.0.22',1,1,NULL,NULL,NULL),(22,'169.254.0.23',1,1,NULL,NULL,NULL),(23,'169.254.0.24',1,1,NULL,NULL,NULL),(24,'169.254.0.25',1,1,NULL,NULL,NULL),(25,'169.254.0.26',1,1,NULL,NULL,NULL),(26,'169.254.0.27',1,1,NULL,NULL,NULL),(27,'169.254.0.28',1,1,NULL,NULL,NULL),(28,'169.254.0.29',1,1,NULL,NULL,NULL),(29,'169.254.0.30',1,1,NULL,NULL,NULL),(30,'169.254.0.31',1,1,NULL,NULL,NULL),(31,'169.254.0.32',1,1,NULL,NULL,NULL),(32,'169.254.0.33',1,1,NULL,NULL,NULL),(33,'169.254.0.34',1,1,NULL,NULL,NULL),(34,'169.254.0.35',1,1,NULL,NULL,NULL),(35,'169.254.0.36',1,1,NULL,NULL,NULL),(36,'169.254.0.37',1,1,NULL,NULL,NULL),(37,'169.254.0.38',1,1,NULL,NULL,NULL),(38,'169.254.0.39',1,1,NULL,NULL,NULL),(39,'169.254.0.40',1,1,NULL,NULL,NULL),(40,'169.254.0.41',1,1,NULL,NULL,NULL),(41,'169.254.0.42',1,1,NULL,NULL,NULL),(42,'169.254.0.43',1,1,NULL,NULL,NULL),(43,'169.254.0.44',1,1,NULL,NULL,NULL),(44,'169.254.0.45',1,1,NULL,NULL,NULL),(45,'169.254.0.46',1,1,NULL,NULL,NULL),(46,'169.254.0.47',1,1,NULL,NULL,NULL),(47,'169.254.0.48',1,1,NULL,NULL,NULL),(48,'169.254.0.49',1,1,NULL,NULL,NULL),(49,'169.254.0.50',1,1,NULL,NULL,NULL),(50,'169.254.0.51',1,1,NULL,NULL,NULL),(51,'169.254.0.52',1,1,NULL,NULL,NULL),(52,'169.254.0.53',1,1,NULL,NULL,NULL),(53,'169.254.0.54',1,1,NULL,NULL,NULL),(54,'169.254.0.55',1,1,NULL,NULL,NULL),(55,'169.254.0.56',1,1,NULL,NULL,NULL),(56,'169.254.0.57',1,1,NULL,NULL,NULL),(57,'169.254.0.58',1,1,NULL,NULL,NULL),(58,'169.254.0.59',1,1,NULL,NULL,NULL),(59,'169.254.0.60',1,1,NULL,NULL,NULL),(60,'169.254.0.61',1,1,NULL,NULL,NULL),(61,'169.254.0.62',1,1,NULL,NULL,NULL),(62,'169.254.0.63',1,1,NULL,NULL,NULL),(63,'169.254.0.64',1,1,NULL,NULL,NULL),(64,'169.254.0.65',1,1,NULL,NULL,NULL),(65,'169.254.0.66',1,1,NULL,NULL,NULL),(66,'169.254.0.67',1,1,NULL,NULL,NULL),(67,'169.254.0.68',1,1,NULL,NULL,NULL),(68,'169.254.0.69',1,1,NULL,NULL,NULL),(69,'169.254.0.70',1,1,NULL,NULL,NULL),(70,'169.254.0.71',1,1,NULL,NULL,NULL),(71,'169.254.0.72',1,1,NULL,NULL,NULL),(72,'169.254.0.73',1,1,NULL,NULL,NULL),(73,'169.254.0.74',1,1,NULL,NULL,NULL),(74,'169.254.0.75',1,1,NULL,NULL,NULL),(75,'169.254.0.76',1,1,NULL,NULL,NULL),(76,'169.254.0.77',1,1,NULL,NULL,NULL),(77,'169.254.0.78',1,1,NULL,NULL,NULL),(78,'169.254.0.79',1,1,NULL,NULL,NULL),(79,'169.254.0.80',1,1,NULL,NULL,NULL),(80,'169.254.0.81',1,1,NULL,NULL,NULL),(81,'169.254.0.82',1,1,NULL,NULL,NULL),(82,'169.254.0.83',1,1,NULL,NULL,NULL),(83,'169.254.0.84',1,1,NULL,NULL,NULL),(84,'169.254.0.85',1,1,NULL,NULL,NULL),(85,'169.254.0.86',1,1,NULL,NULL,NULL),(86,'169.254.0.87',1,1,NULL,NULL,NULL),(87,'169.254.0.88',1,1,NULL,NULL,NULL),(88,'169.254.0.89',1,1,NULL,NULL,NULL),(89,'169.254.0.90',1,1,NULL,NULL,NULL),(90,'169.254.0.91',1,1,NULL,NULL,NULL),(91,'169.254.0.92',1,1,NULL,NULL,NULL),(92,'169.254.0.93',1,1,NULL,NULL,NULL),(93,'169.254.0.94',1,1,NULL,NULL,NULL),(94,'169.254.0.95',1,1,NULL,NULL,NULL),(95,'169.254.0.96',1,1,5,'591959ae-f08c-4b7c-86ce-5ece82b4e37d','2011-04-26 03:00:12'),(96,'169.254.0.97',1,1,NULL,NULL,NULL),(97,'169.254.0.98',1,1,NULL,NULL,NULL),(98,'169.254.0.99',1,1,NULL,NULL,NULL),(99,'169.254.0.100',1,1,NULL,NULL,NULL),(100,'169.254.0.101',1,1,NULL,NULL,NULL),(101,'169.254.0.102',1,1,NULL,NULL,NULL),(102,'169.254.0.103',1,1,NULL,NULL,NULL),(103,'169.254.0.104',1,1,NULL,NULL,NULL),(104,'169.254.0.105',1,1,NULL,NULL,NULL),(105,'169.254.0.106',1,1,NULL,NULL,NULL),(106,'169.254.0.107',1,1,NULL,NULL,NULL),(107,'169.254.0.108',1,1,NULL,NULL,NULL),(108,'169.254.0.109',1,1,NULL,NULL,NULL),(109,'169.254.0.110',1,1,NULL,NULL,NULL),(110,'169.254.0.111',1,1,NULL,NULL,NULL),(111,'169.254.0.112',1,1,NULL,NULL,NULL),(112,'169.254.0.113',1,1,NULL,NULL,NULL),(113,'169.254.0.114',1,1,NULL,NULL,NULL),(114,'169.254.0.115',1,1,NULL,NULL,NULL),(115,'169.254.0.116',1,1,NULL,NULL,NULL),(116,'169.254.0.117',1,1,NULL,NULL,NULL),(117,'169.254.0.118',1,1,NULL,NULL,NULL),(118,'169.254.0.119',1,1,NULL,NULL,NULL),(119,'169.254.0.120',1,1,NULL,NULL,NULL),(120,'169.254.0.121',1,1,NULL,NULL,NULL),(121,'169.254.0.122',1,1,NULL,NULL,NULL),(122,'169.254.0.123',1,1,NULL,NULL,NULL),(123,'169.254.0.124',1,1,NULL,NULL,NULL),(124,'169.254.0.125',1,1,NULL,NULL,NULL),(125,'169.254.0.126',1,1,NULL,NULL,NULL),(126,'169.254.0.127',1,1,NULL,NULL,NULL),(127,'169.254.0.128',1,1,NULL,NULL,NULL),(128,'169.254.0.129',1,1,NULL,NULL,NULL),(129,'169.254.0.130',1,1,NULL,NULL,NULL),(130,'169.254.0.131',1,1,NULL,NULL,NULL),(131,'169.254.0.132',1,1,NULL,NULL,NULL),(132,'169.254.0.133',1,1,NULL,NULL,NULL),(133,'169.254.0.134',1,1,NULL,NULL,NULL),(134,'169.254.0.135',1,1,NULL,NULL,NULL),(135,'169.254.0.136',1,1,NULL,NULL,NULL),(136,'169.254.0.137',1,1,NULL,NULL,NULL),(137,'169.254.0.138',1,1,NULL,NULL,NULL),(138,'169.254.0.139',1,1,NULL,NULL,NULL),(139,'169.254.0.140',1,1,NULL,NULL,NULL),(140,'169.254.0.141',1,1,NULL,NULL,NULL),(141,'169.254.0.142',1,1,NULL,NULL,NULL),(142,'169.254.0.143',1,1,NULL,NULL,NULL),(143,'169.254.0.144',1,1,NULL,NULL,NULL),(144,'169.254.0.145',1,1,NULL,NULL,NULL),(145,'169.254.0.146',1,1,NULL,NULL,NULL),(146,'169.254.0.147',1,1,NULL,NULL,NULL),(147,'169.254.0.148',1,1,NULL,NULL,NULL),(148,'169.254.0.149',1,1,NULL,NULL,NULL),(149,'169.254.0.150',1,1,NULL,NULL,NULL),(150,'169.254.0.151',1,1,NULL,NULL,NULL),(151,'169.254.0.152',1,1,NULL,NULL,NULL),(152,'169.254.0.153',1,1,NULL,NULL,NULL),(153,'169.254.0.154',1,1,9,'a8e10db0-8baf-4771-875c-efacd802005b','2011-04-26 03:18:02'),(154,'169.254.0.155',1,1,NULL,NULL,NULL),(155,'169.254.0.156',1,1,NULL,NULL,NULL),(156,'169.254.0.157',1,1,NULL,NULL,NULL),(157,'169.254.0.158',1,1,NULL,NULL,NULL),(158,'169.254.0.159',1,1,NULL,NULL,NULL),(159,'169.254.0.160',1,1,NULL,NULL,NULL),(160,'169.254.0.161',1,1,NULL,NULL,NULL),(161,'169.254.0.162',1,1,NULL,NULL,NULL),(162,'169.254.0.163',1,1,NULL,NULL,NULL),(163,'169.254.0.164',1,1,NULL,NULL,NULL),(164,'169.254.0.165',1,1,NULL,NULL,NULL),(165,'169.254.0.166',1,1,NULL,NULL,NULL),(166,'169.254.0.167',1,1,NULL,NULL,NULL),(167,'169.254.0.168',1,1,NULL,NULL,NULL),(168,'169.254.0.169',1,1,NULL,NULL,NULL),(169,'169.254.0.170',1,1,NULL,NULL,NULL),(170,'169.254.0.171',1,1,NULL,NULL,NULL),(171,'169.254.0.172',1,1,NULL,NULL,NULL),(172,'169.254.0.173',1,1,NULL,NULL,NULL),(173,'169.254.0.174',1,1,NULL,NULL,NULL),(174,'169.254.0.175',1,1,NULL,NULL,NULL),(175,'169.254.0.176',1,1,NULL,NULL,NULL),(176,'169.254.0.177',1,1,NULL,NULL,NULL),(177,'169.254.0.178',1,1,NULL,NULL,NULL),(178,'169.254.0.179',1,1,NULL,NULL,NULL),(179,'169.254.0.180',1,1,NULL,NULL,NULL),(180,'169.254.0.181',1,1,NULL,NULL,NULL),(181,'169.254.0.182',1,1,NULL,NULL,NULL),(182,'169.254.0.183',1,1,NULL,NULL,NULL),(183,'169.254.0.184',1,1,NULL,NULL,NULL),(184,'169.254.0.185',1,1,NULL,NULL,NULL),(185,'169.254.0.186',1,1,NULL,NULL,NULL),(186,'169.254.0.187',1,1,NULL,NULL,NULL),(187,'169.254.0.188',1,1,NULL,NULL,NULL),(188,'169.254.0.189',1,1,NULL,NULL,NULL),(189,'169.254.0.190',1,1,NULL,NULL,NULL),(190,'169.254.0.191',1,1,NULL,NULL,NULL),(191,'169.254.0.192',1,1,NULL,NULL,NULL),(192,'169.254.0.193',1,1,NULL,NULL,NULL),(193,'169.254.0.194',1,1,NULL,NULL,NULL),(194,'169.254.0.195',1,1,NULL,NULL,NULL),(195,'169.254.0.196',1,1,NULL,NULL,NULL),(196,'169.254.0.197',1,1,NULL,NULL,NULL),(197,'169.254.0.198',1,1,NULL,NULL,NULL),(198,'169.254.0.199',1,1,NULL,NULL,NULL),(199,'169.254.0.200',1,1,NULL,NULL,NULL),(200,'169.254.0.201',1,1,NULL,NULL,NULL),(201,'169.254.0.202',1,1,NULL,NULL,NULL),(202,'169.254.0.203',1,1,NULL,NULL,NULL),(203,'169.254.0.204',1,1,NULL,NULL,NULL),(204,'169.254.0.205',1,1,NULL,NULL,NULL),(205,'169.254.0.206',1,1,NULL,NULL,NULL),(206,'169.254.0.207',1,1,NULL,NULL,NULL),(207,'169.254.0.208',1,1,NULL,NULL,NULL),(208,'169.254.0.209',1,1,NULL,NULL,NULL),(209,'169.254.0.210',1,1,NULL,NULL,NULL),(210,'169.254.0.211',1,1,NULL,NULL,NULL),(211,'169.254.0.212',1,1,NULL,NULL,NULL),(212,'169.254.0.213',1,1,NULL,NULL,NULL),(213,'169.254.0.214',1,1,NULL,NULL,NULL),(214,'169.254.0.215',1,1,NULL,NULL,NULL),(215,'169.254.0.216',1,1,NULL,NULL,NULL),(216,'169.254.0.217',1,1,NULL,NULL,NULL),(217,'169.254.0.218',1,1,NULL,NULL,NULL),(218,'169.254.0.219',1,1,NULL,NULL,NULL),(219,'169.254.0.220',1,1,NULL,NULL,NULL),(220,'169.254.0.221',1,1,NULL,NULL,NULL),(221,'169.254.0.222',1,1,NULL,NULL,NULL),(222,'169.254.0.223',1,1,NULL,NULL,NULL),(223,'169.254.0.224',1,1,NULL,NULL,NULL),(224,'169.254.0.225',1,1,NULL,NULL,NULL),(225,'169.254.0.226',1,1,NULL,NULL,NULL),(226,'169.254.0.227',1,1,NULL,NULL,NULL),(227,'169.254.0.228',1,1,NULL,NULL,NULL),(228,'169.254.0.229',1,1,NULL,NULL,NULL),(229,'169.254.0.230',1,1,NULL,NULL,NULL),(230,'169.254.0.231',1,1,NULL,NULL,NULL),(231,'169.254.0.232',1,1,NULL,NULL,NULL),(232,'169.254.0.233',1,1,NULL,NULL,NULL),(233,'169.254.0.234',1,1,NULL,NULL,NULL),(234,'169.254.0.235',1,1,NULL,NULL,NULL),(235,'169.254.0.236',1,1,NULL,NULL,NULL),(236,'169.254.0.237',1,1,NULL,NULL,NULL),(237,'169.254.0.238',1,1,NULL,NULL,NULL),(238,'169.254.0.239',1,1,NULL,NULL,NULL),(239,'169.254.0.240',1,1,NULL,NULL,NULL),(240,'169.254.0.241',1,1,NULL,NULL,NULL),(241,'169.254.0.242',1,1,NULL,NULL,NULL),(242,'169.254.0.243',1,1,NULL,NULL,NULL),(243,'169.254.0.244',1,1,NULL,NULL,NULL),(244,'169.254.0.245',1,1,NULL,NULL,NULL),(245,'169.254.0.246',1,1,NULL,NULL,NULL),(246,'169.254.0.247',1,1,NULL,NULL,NULL),(247,'169.254.0.248',1,1,NULL,NULL,NULL),(248,'169.254.0.249',1,1,NULL,NULL,NULL),(249,'169.254.0.250',1,1,NULL,NULL,NULL),(250,'169.254.0.251',1,1,NULL,NULL,NULL),(251,'169.254.0.252',1,1,NULL,NULL,NULL),(252,'169.254.0.253',1,1,NULL,NULL,NULL),(253,'169.254.0.254',1,1,NULL,NULL,NULL),(254,'169.254.0.255',1,1,NULL,NULL,NULL),(255,'169.254.1.0',1,1,NULL,NULL,NULL),(256,'169.254.1.1',1,1,NULL,NULL,NULL),(257,'169.254.1.2',1,1,NULL,NULL,NULL),(258,'169.254.1.3',1,1,NULL,NULL,NULL),(259,'169.254.1.4',1,1,NULL,NULL,NULL),(260,'169.254.1.5',1,1,NULL,NULL,NULL),(261,'169.254.1.6',1,1,NULL,NULL,NULL),(262,'169.254.1.7',1,1,NULL,NULL,NULL),(263,'169.254.1.8',1,1,NULL,NULL,NULL),(264,'169.254.1.9',1,1,NULL,NULL,NULL),(265,'169.254.1.10',1,1,NULL,NULL,NULL),(266,'169.254.1.11',1,1,NULL,NULL,NULL),(267,'169.254.1.12',1,1,NULL,NULL,NULL),(268,'169.254.1.13',1,1,NULL,NULL,NULL),(269,'169.254.1.14',1,1,NULL,NULL,NULL),(270,'169.254.1.15',1,1,NULL,NULL,NULL),(271,'169.254.1.16',1,1,NULL,NULL,NULL),(272,'169.254.1.17',1,1,NULL,NULL,NULL),(273,'169.254.1.18',1,1,NULL,NULL,NULL),(274,'169.254.1.19',1,1,NULL,NULL,NULL),(275,'169.254.1.20',1,1,NULL,NULL,NULL),(276,'169.254.1.21',1,1,NULL,NULL,NULL),(277,'169.254.1.22',1,1,NULL,NULL,NULL),(278,'169.254.1.23',1,1,NULL,NULL,NULL),(279,'169.254.1.24',1,1,NULL,NULL,NULL),(280,'169.254.1.25',1,1,NULL,NULL,NULL),(281,'169.254.1.26',1,1,NULL,NULL,NULL),(282,'169.254.1.27',1,1,NULL,NULL,NULL),(283,'169.254.1.28',1,1,NULL,NULL,NULL),(284,'169.254.1.29',1,1,NULL,NULL,NULL),(285,'169.254.1.30',1,1,NULL,NULL,NULL),(286,'169.254.1.31',1,1,NULL,NULL,NULL),(287,'169.254.1.32',1,1,NULL,NULL,NULL),(288,'169.254.1.33',1,1,NULL,NULL,NULL),(289,'169.254.1.34',1,1,NULL,NULL,NULL),(290,'169.254.1.35',1,1,NULL,NULL,NULL),(291,'169.254.1.36',1,1,NULL,NULL,NULL),(292,'169.254.1.37',1,1,NULL,NULL,NULL),(293,'169.254.1.38',1,1,NULL,NULL,NULL),(294,'169.254.1.39',1,1,NULL,NULL,NULL),(295,'169.254.1.40',1,1,NULL,NULL,NULL),(296,'169.254.1.41',1,1,NULL,NULL,NULL),(297,'169.254.1.42',1,1,NULL,NULL,NULL),(298,'169.254.1.43',1,1,NULL,NULL,NULL),(299,'169.254.1.44',1,1,NULL,NULL,NULL),(300,'169.254.1.45',1,1,NULL,NULL,NULL),(301,'169.254.1.46',1,1,NULL,NULL,NULL),(302,'169.254.1.47',1,1,NULL,NULL,NULL),(303,'169.254.1.48',1,1,NULL,NULL,NULL),(304,'169.254.1.49',1,1,NULL,NULL,NULL),(305,'169.254.1.50',1,1,NULL,NULL,NULL),(306,'169.254.1.51',1,1,NULL,NULL,NULL),(307,'169.254.1.52',1,1,NULL,NULL,NULL),(308,'169.254.1.53',1,1,NULL,NULL,NULL),(309,'169.254.1.54',1,1,NULL,NULL,NULL),(310,'169.254.1.55',1,1,NULL,NULL,NULL),(311,'169.254.1.56',1,1,NULL,NULL,NULL),(312,'169.254.1.57',1,1,NULL,NULL,NULL),(313,'169.254.1.58',1,1,NULL,NULL,NULL),(314,'169.254.1.59',1,1,NULL,NULL,NULL),(315,'169.254.1.60',1,1,NULL,NULL,NULL),(316,'169.254.1.61',1,1,NULL,NULL,NULL),(317,'169.254.1.62',1,1,NULL,NULL,NULL),(318,'169.254.1.63',1,1,NULL,NULL,NULL),(319,'169.254.1.64',1,1,NULL,NULL,NULL),(320,'169.254.1.65',1,1,NULL,NULL,NULL),(321,'169.254.1.66',1,1,NULL,NULL,NULL),(322,'169.254.1.67',1,1,NULL,NULL,NULL),(323,'169.254.1.68',1,1,NULL,NULL,NULL),(324,'169.254.1.69',1,1,NULL,NULL,NULL),(325,'169.254.1.70',1,1,NULL,NULL,NULL),(326,'169.254.1.71',1,1,NULL,NULL,NULL),(327,'169.254.1.72',1,1,NULL,NULL,NULL),(328,'169.254.1.73',1,1,NULL,NULL,NULL),(329,'169.254.1.74',1,1,NULL,NULL,NULL),(330,'169.254.1.75',1,1,NULL,NULL,NULL),(331,'169.254.1.76',1,1,NULL,NULL,NULL),(332,'169.254.1.77',1,1,NULL,NULL,NULL),(333,'169.254.1.78',1,1,NULL,NULL,NULL),(334,'169.254.1.79',1,1,NULL,NULL,NULL),(335,'169.254.1.80',1,1,NULL,NULL,NULL),(336,'169.254.1.81',1,1,NULL,NULL,NULL),(337,'169.254.1.82',1,1,NULL,NULL,NULL),(338,'169.254.1.83',1,1,NULL,NULL,NULL),(339,'169.254.1.84',1,1,NULL,NULL,NULL),(340,'169.254.1.85',1,1,NULL,NULL,NULL),(341,'169.254.1.86',1,1,NULL,NULL,NULL),(342,'169.254.1.87',1,1,NULL,NULL,NULL),(343,'169.254.1.88',1,1,NULL,NULL,NULL),(344,'169.254.1.89',1,1,NULL,NULL,NULL),(345,'169.254.1.90',1,1,NULL,NULL,NULL),(346,'169.254.1.91',1,1,NULL,NULL,NULL),(347,'169.254.1.92',1,1,NULL,NULL,NULL),(348,'169.254.1.93',1,1,NULL,NULL,NULL),(349,'169.254.1.94',1,1,NULL,NULL,NULL),(350,'169.254.1.95',1,1,NULL,NULL,NULL),(351,'169.254.1.96',1,1,NULL,NULL,NULL),(352,'169.254.1.97',1,1,NULL,NULL,NULL),(353,'169.254.1.98',1,1,NULL,NULL,NULL),(354,'169.254.1.99',1,1,NULL,NULL,NULL),(355,'169.254.1.100',1,1,NULL,NULL,NULL),(356,'169.254.1.101',1,1,NULL,NULL,NULL),(357,'169.254.1.102',1,1,NULL,NULL,NULL),(358,'169.254.1.103',1,1,NULL,NULL,NULL),(359,'169.254.1.104',1,1,NULL,NULL,NULL),(360,'169.254.1.105',1,1,NULL,NULL,NULL),(361,'169.254.1.106',1,1,NULL,NULL,NULL),(362,'169.254.1.107',1,1,NULL,NULL,NULL),(363,'169.254.1.108',1,1,NULL,NULL,NULL),(364,'169.254.1.109',1,1,NULL,NULL,NULL),(365,'169.254.1.110',1,1,NULL,NULL,NULL),(366,'169.254.1.111',1,1,NULL,NULL,NULL),(367,'169.254.1.112',1,1,NULL,NULL,NULL),(368,'169.254.1.113',1,1,NULL,NULL,NULL),(369,'169.254.1.114',1,1,NULL,NULL,NULL),(370,'169.254.1.115',1,1,NULL,NULL,NULL),(371,'169.254.1.116',1,1,NULL,NULL,NULL),(372,'169.254.1.117',1,1,NULL,NULL,NULL),(373,'169.254.1.118',1,1,NULL,NULL,NULL),(374,'169.254.1.119',1,1,NULL,NULL,NULL),(375,'169.254.1.120',1,1,NULL,NULL,NULL),(376,'169.254.1.121',1,1,NULL,NULL,NULL),(377,'169.254.1.122',1,1,NULL,NULL,NULL),(378,'169.254.1.123',1,1,NULL,NULL,NULL),(379,'169.254.1.124',1,1,NULL,NULL,NULL),(380,'169.254.1.125',1,1,NULL,NULL,NULL),(381,'169.254.1.126',1,1,NULL,NULL,NULL),(382,'169.254.1.127',1,1,NULL,NULL,NULL),(383,'169.254.1.128',1,1,NULL,NULL,NULL),(384,'169.254.1.129',1,1,NULL,NULL,NULL),(385,'169.254.1.130',1,1,NULL,NULL,NULL),(386,'169.254.1.131',1,1,NULL,NULL,NULL),(387,'169.254.1.132',1,1,NULL,NULL,NULL),(388,'169.254.1.133',1,1,NULL,NULL,NULL),(389,'169.254.1.134',1,1,NULL,NULL,NULL),(390,'169.254.1.135',1,1,NULL,NULL,NULL),(391,'169.254.1.136',1,1,NULL,NULL,NULL),(392,'169.254.1.137',1,1,NULL,NULL,NULL),(393,'169.254.1.138',1,1,NULL,NULL,NULL),(394,'169.254.1.139',1,1,NULL,NULL,NULL),(395,'169.254.1.140',1,1,NULL,NULL,NULL),(396,'169.254.1.141',1,1,NULL,NULL,NULL),(397,'169.254.1.142',1,1,NULL,NULL,NULL),(398,'169.254.1.143',1,1,NULL,NULL,NULL),(399,'169.254.1.144',1,1,NULL,NULL,NULL),(400,'169.254.1.145',1,1,NULL,NULL,NULL),(401,'169.254.1.146',1,1,NULL,NULL,NULL),(402,'169.254.1.147',1,1,NULL,NULL,NULL),(403,'169.254.1.148',1,1,NULL,NULL,NULL),(404,'169.254.1.149',1,1,NULL,NULL,NULL),(405,'169.254.1.150',1,1,NULL,NULL,NULL),(406,'169.254.1.151',1,1,NULL,NULL,NULL),(407,'169.254.1.152',1,1,NULL,NULL,NULL),(408,'169.254.1.153',1,1,NULL,NULL,NULL),(409,'169.254.1.154',1,1,NULL,NULL,NULL),(410,'169.254.1.155',1,1,NULL,NULL,NULL),(411,'169.254.1.156',1,1,NULL,NULL,NULL),(412,'169.254.1.157',1,1,NULL,NULL,NULL),(413,'169.254.1.158',1,1,NULL,NULL,NULL),(414,'169.254.1.159',1,1,NULL,NULL,NULL),(415,'169.254.1.160',1,1,NULL,NULL,NULL),(416,'169.254.1.161',1,1,NULL,NULL,NULL),(417,'169.254.1.162',1,1,NULL,NULL,NULL),(418,'169.254.1.163',1,1,NULL,NULL,NULL),(419,'169.254.1.164',1,1,NULL,NULL,NULL),(420,'169.254.1.165',1,1,NULL,NULL,NULL),(421,'169.254.1.166',1,1,NULL,NULL,NULL),(422,'169.254.1.167',1,1,NULL,NULL,NULL),(423,'169.254.1.168',1,1,NULL,NULL,NULL),(424,'169.254.1.169',1,1,NULL,NULL,NULL),(425,'169.254.1.170',1,1,NULL,NULL,NULL),(426,'169.254.1.171',1,1,NULL,NULL,NULL),(427,'169.254.1.172',1,1,NULL,NULL,NULL),(428,'169.254.1.173',1,1,NULL,NULL,NULL),(429,'169.254.1.174',1,1,NULL,NULL,NULL),(430,'169.254.1.175',1,1,NULL,NULL,NULL),(431,'169.254.1.176',1,1,NULL,NULL,NULL),(432,'169.254.1.177',1,1,NULL,NULL,NULL),(433,'169.254.1.178',1,1,NULL,NULL,NULL),(434,'169.254.1.179',1,1,NULL,NULL,NULL),(435,'169.254.1.180',1,1,NULL,NULL,NULL),(436,'169.254.1.181',1,1,NULL,NULL,NULL),(437,'169.254.1.182',1,1,NULL,NULL,NULL),(438,'169.254.1.183',1,1,NULL,NULL,NULL),(439,'169.254.1.184',1,1,NULL,NULL,NULL),(440,'169.254.1.185',1,1,NULL,NULL,NULL),(441,'169.254.1.186',1,1,NULL,NULL,NULL),(442,'169.254.1.187',1,1,NULL,NULL,NULL),(443,'169.254.1.188',1,1,NULL,NULL,NULL),(444,'169.254.1.189',1,1,NULL,NULL,NULL),(445,'169.254.1.190',1,1,NULL,NULL,NULL),(446,'169.254.1.191',1,1,NULL,NULL,NULL),(447,'169.254.1.192',1,1,NULL,NULL,NULL),(448,'169.254.1.193',1,1,NULL,NULL,NULL),(449,'169.254.1.194',1,1,NULL,NULL,NULL),(450,'169.254.1.195',1,1,NULL,NULL,NULL),(451,'169.254.1.196',1,1,NULL,NULL,NULL),(452,'169.254.1.197',1,1,NULL,NULL,NULL),(453,'169.254.1.198',1,1,NULL,NULL,NULL),(454,'169.254.1.199',1,1,NULL,NULL,NULL),(455,'169.254.1.200',1,1,NULL,NULL,NULL),(456,'169.254.1.201',1,1,NULL,NULL,NULL),(457,'169.254.1.202',1,1,NULL,NULL,NULL),(458,'169.254.1.203',1,1,NULL,NULL,NULL),(459,'169.254.1.204',1,1,NULL,NULL,NULL),(460,'169.254.1.205',1,1,NULL,NULL,NULL),(461,'169.254.1.206',1,1,NULL,NULL,NULL),(462,'169.254.1.207',1,1,NULL,NULL,NULL),(463,'169.254.1.208',1,1,NULL,NULL,NULL),(464,'169.254.1.209',1,1,NULL,NULL,NULL),(465,'169.254.1.210',1,1,NULL,NULL,NULL),(466,'169.254.1.211',1,1,NULL,NULL,NULL),(467,'169.254.1.212',1,1,NULL,NULL,NULL),(468,'169.254.1.213',1,1,NULL,NULL,NULL),(469,'169.254.1.214',1,1,NULL,NULL,NULL),(470,'169.254.1.215',1,1,NULL,NULL,NULL),(471,'169.254.1.216',1,1,NULL,NULL,NULL),(472,'169.254.1.217',1,1,NULL,NULL,NULL),(473,'169.254.1.218',1,1,NULL,NULL,NULL),(474,'169.254.1.219',1,1,NULL,NULL,NULL),(475,'169.254.1.220',1,1,NULL,NULL,NULL),(476,'169.254.1.221',1,1,NULL,NULL,NULL),(477,'169.254.1.222',1,1,NULL,NULL,NULL),(478,'169.254.1.223',1,1,NULL,NULL,NULL),(479,'169.254.1.224',1,1,NULL,NULL,NULL),(480,'169.254.1.225',1,1,NULL,NULL,NULL),(481,'169.254.1.226',1,1,NULL,NULL,NULL),(482,'169.254.1.227',1,1,NULL,NULL,NULL),(483,'169.254.1.228',1,1,NULL,NULL,NULL),(484,'169.254.1.229',1,1,NULL,NULL,NULL),(485,'169.254.1.230',1,1,NULL,NULL,NULL),(486,'169.254.1.231',1,1,NULL,NULL,NULL),(487,'169.254.1.232',1,1,NULL,NULL,NULL),(488,'169.254.1.233',1,1,NULL,NULL,NULL),(489,'169.254.1.234',1,1,NULL,NULL,NULL),(490,'169.254.1.235',1,1,NULL,NULL,NULL),(491,'169.254.1.236',1,1,NULL,NULL,NULL),(492,'169.254.1.237',1,1,NULL,NULL,NULL),(493,'169.254.1.238',1,1,NULL,NULL,NULL),(494,'169.254.1.239',1,1,NULL,NULL,NULL),(495,'169.254.1.240',1,1,NULL,NULL,NULL),(496,'169.254.1.241',1,1,NULL,NULL,NULL),(497,'169.254.1.242',1,1,NULL,NULL,NULL),(498,'169.254.1.243',1,1,NULL,NULL,NULL),(499,'169.254.1.244',1,1,NULL,NULL,NULL),(500,'169.254.1.245',1,1,NULL,NULL,NULL),(501,'169.254.1.246',1,1,NULL,NULL,NULL),(502,'169.254.1.247',1,1,NULL,NULL,NULL),(503,'169.254.1.248',1,1,NULL,NULL,NULL),(504,'169.254.1.249',1,1,NULL,NULL,NULL),(505,'169.254.1.250',1,1,NULL,NULL,NULL),(506,'169.254.1.251',1,1,NULL,NULL,NULL),(507,'169.254.1.252',1,1,NULL,NULL,NULL),(508,'169.254.1.253',1,1,NULL,NULL,NULL),(509,'169.254.1.254',1,1,NULL,NULL,NULL),(510,'169.254.1.255',1,1,NULL,NULL,NULL),(511,'169.254.2.0',1,1,NULL,NULL,NULL),(512,'169.254.2.1',1,1,NULL,NULL,NULL),(513,'169.254.2.2',1,1,NULL,NULL,NULL),(514,'169.254.2.3',1,1,NULL,NULL,NULL),(515,'169.254.2.4',1,1,NULL,NULL,NULL),(516,'169.254.2.5',1,1,NULL,NULL,NULL),(517,'169.254.2.6',1,1,NULL,NULL,NULL),(518,'169.254.2.7',1,1,NULL,NULL,NULL),(519,'169.254.2.8',1,1,NULL,NULL,NULL),(520,'169.254.2.9',1,1,NULL,NULL,NULL),(521,'169.254.2.10',1,1,NULL,NULL,NULL),(522,'169.254.2.11',1,1,NULL,NULL,NULL),(523,'169.254.2.12',1,1,NULL,NULL,NULL),(524,'169.254.2.13',1,1,NULL,NULL,NULL),(525,'169.254.2.14',1,1,NULL,NULL,NULL),(526,'169.254.2.15',1,1,NULL,NULL,NULL),(527,'169.254.2.16',1,1,NULL,NULL,NULL),(528,'169.254.2.17',1,1,NULL,NULL,NULL),(529,'169.254.2.18',1,1,NULL,NULL,NULL),(530,'169.254.2.19',1,1,NULL,NULL,NULL),(531,'169.254.2.20',1,1,NULL,NULL,NULL),(532,'169.254.2.21',1,1,NULL,NULL,NULL),(533,'169.254.2.22',1,1,NULL,NULL,NULL),(534,'169.254.2.23',1,1,NULL,NULL,NULL),(535,'169.254.2.24',1,1,NULL,NULL,NULL),(536,'169.254.2.25',1,1,NULL,NULL,NULL),(537,'169.254.2.26',1,1,NULL,NULL,NULL),(538,'169.254.2.27',1,1,NULL,NULL,NULL),(539,'169.254.2.28',1,1,NULL,NULL,NULL),(540,'169.254.2.29',1,1,NULL,NULL,NULL),(541,'169.254.2.30',1,1,NULL,NULL,NULL),(542,'169.254.2.31',1,1,NULL,NULL,NULL),(543,'169.254.2.32',1,1,NULL,NULL,NULL),(544,'169.254.2.33',1,1,NULL,NULL,NULL),(545,'169.254.2.34',1,1,NULL,NULL,NULL),(546,'169.254.2.35',1,1,NULL,NULL,NULL),(547,'169.254.2.36',1,1,NULL,NULL,NULL),(548,'169.254.2.37',1,1,NULL,NULL,NULL),(549,'169.254.2.38',1,1,NULL,NULL,NULL),(550,'169.254.2.39',1,1,NULL,NULL,NULL),(551,'169.254.2.40',1,1,NULL,NULL,NULL),(552,'169.254.2.41',1,1,NULL,NULL,NULL),(553,'169.254.2.42',1,1,NULL,NULL,NULL),(554,'169.254.2.43',1,1,NULL,NULL,NULL),(555,'169.254.2.44',1,1,NULL,NULL,NULL),(556,'169.254.2.45',1,1,NULL,NULL,NULL),(557,'169.254.2.46',1,1,NULL,NULL,NULL),(558,'169.254.2.47',1,1,NULL,NULL,NULL),(559,'169.254.2.48',1,1,NULL,NULL,NULL),(560,'169.254.2.49',1,1,NULL,NULL,NULL),(561,'169.254.2.50',1,1,NULL,NULL,NULL),(562,'169.254.2.51',1,1,NULL,NULL,NULL),(563,'169.254.2.52',1,1,NULL,NULL,NULL),(564,'169.254.2.53',1,1,NULL,NULL,NULL),(565,'169.254.2.54',1,1,NULL,NULL,NULL),(566,'169.254.2.55',1,1,NULL,NULL,NULL),(567,'169.254.2.56',1,1,NULL,NULL,NULL),(568,'169.254.2.57',1,1,NULL,NULL,NULL),(569,'169.254.2.58',1,1,NULL,NULL,NULL),(570,'169.254.2.59',1,1,NULL,NULL,NULL),(571,'169.254.2.60',1,1,NULL,NULL,NULL),(572,'169.254.2.61',1,1,NULL,NULL,NULL),(573,'169.254.2.62',1,1,NULL,NULL,NULL),(574,'169.254.2.63',1,1,NULL,NULL,NULL),(575,'169.254.2.64',1,1,NULL,NULL,NULL),(576,'169.254.2.65',1,1,NULL,NULL,NULL),(577,'169.254.2.66',1,1,NULL,NULL,NULL),(578,'169.254.2.67',1,1,NULL,NULL,NULL),(579,'169.254.2.68',1,1,NULL,NULL,NULL),(580,'169.254.2.69',1,1,NULL,NULL,NULL),(581,'169.254.2.70',1,1,NULL,NULL,NULL),(582,'169.254.2.71',1,1,NULL,NULL,NULL),(583,'169.254.2.72',1,1,NULL,NULL,NULL),(584,'169.254.2.73',1,1,NULL,NULL,NULL),(585,'169.254.2.74',1,1,NULL,NULL,NULL),(586,'169.254.2.75',1,1,NULL,NULL,NULL),(587,'169.254.2.76',1,1,NULL,NULL,NULL),(588,'169.254.2.77',1,1,NULL,NULL,NULL),(589,'169.254.2.78',1,1,NULL,NULL,NULL),(590,'169.254.2.79',1,1,NULL,NULL,NULL),(591,'169.254.2.80',1,1,NULL,NULL,NULL),(592,'169.254.2.81',1,1,NULL,NULL,NULL),(593,'169.254.2.82',1,1,NULL,NULL,NULL),(594,'169.254.2.83',1,1,NULL,NULL,NULL),(595,'169.254.2.84',1,1,NULL,NULL,NULL),(596,'169.254.2.85',1,1,2,'b1d47f5a-edc8-4246-bcbb-e79b0e1d9007','2011-04-26 03:00:12'),(597,'169.254.2.86',1,1,NULL,NULL,NULL),(598,'169.254.2.87',1,1,NULL,NULL,NULL),(599,'169.254.2.88',1,1,NULL,NULL,NULL),(600,'169.254.2.89',1,1,NULL,NULL,NULL),(601,'169.254.2.90',1,1,NULL,NULL,NULL),(602,'169.254.2.91',1,1,NULL,NULL,NULL),(603,'169.254.2.92',1,1,NULL,NULL,NULL),(604,'169.254.2.93',1,1,NULL,NULL,NULL),(605,'169.254.2.94',1,1,NULL,NULL,NULL),(606,'169.254.2.95',1,1,NULL,NULL,NULL),(607,'169.254.2.96',1,1,NULL,NULL,NULL),(608,'169.254.2.97',1,1,NULL,NULL,NULL),(609,'169.254.2.98',1,1,NULL,NULL,NULL),(610,'169.254.2.99',1,1,NULL,NULL,NULL),(611,'169.254.2.100',1,1,NULL,NULL,NULL),(612,'169.254.2.101',1,1,NULL,NULL,NULL),(613,'169.254.2.102',1,1,NULL,NULL,NULL),(614,'169.254.2.103',1,1,NULL,NULL,NULL),(615,'169.254.2.104',1,1,NULL,NULL,NULL),(616,'169.254.2.105',1,1,NULL,NULL,NULL),(617,'169.254.2.106',1,1,NULL,NULL,NULL),(618,'169.254.2.107',1,1,NULL,NULL,NULL),(619,'169.254.2.108',1,1,NULL,NULL,NULL),(620,'169.254.2.109',1,1,NULL,NULL,NULL),(621,'169.254.2.110',1,1,NULL,NULL,NULL),(622,'169.254.2.111',1,1,NULL,NULL,NULL),(623,'169.254.2.112',1,1,NULL,NULL,NULL),(624,'169.254.2.113',1,1,NULL,NULL,NULL),(625,'169.254.2.114',1,1,NULL,NULL,NULL),(626,'169.254.2.115',1,1,NULL,NULL,NULL),(627,'169.254.2.116',1,1,NULL,NULL,NULL),(628,'169.254.2.117',1,1,NULL,NULL,NULL),(629,'169.254.2.118',1,1,NULL,NULL,NULL),(630,'169.254.2.119',1,1,NULL,NULL,NULL),(631,'169.254.2.120',1,1,NULL,NULL,NULL),(632,'169.254.2.121',1,1,NULL,NULL,NULL),(633,'169.254.2.122',1,1,NULL,NULL,NULL),(634,'169.254.2.123',1,1,NULL,NULL,NULL),(635,'169.254.2.124',1,1,NULL,NULL,NULL),(636,'169.254.2.125',1,1,NULL,NULL,NULL),(637,'169.254.2.126',1,1,NULL,NULL,NULL),(638,'169.254.2.127',1,1,NULL,NULL,NULL),(639,'169.254.2.128',1,1,NULL,NULL,NULL),(640,'169.254.2.129',1,1,NULL,NULL,NULL),(641,'169.254.2.130',1,1,NULL,NULL,NULL),(642,'169.254.2.131',1,1,NULL,NULL,NULL),(643,'169.254.2.132',1,1,NULL,NULL,NULL),(644,'169.254.2.133',1,1,NULL,NULL,NULL),(645,'169.254.2.134',1,1,NULL,NULL,NULL),(646,'169.254.2.135',1,1,NULL,NULL,NULL),(647,'169.254.2.136',1,1,NULL,NULL,NULL),(648,'169.254.2.137',1,1,NULL,NULL,NULL),(649,'169.254.2.138',1,1,NULL,NULL,NULL),(650,'169.254.2.139',1,1,NULL,NULL,NULL),(651,'169.254.2.140',1,1,NULL,NULL,NULL),(652,'169.254.2.141',1,1,NULL,NULL,NULL),(653,'169.254.2.142',1,1,NULL,NULL,NULL),(654,'169.254.2.143',1,1,NULL,NULL,NULL),(655,'169.254.2.144',1,1,NULL,NULL,NULL),(656,'169.254.2.145',1,1,NULL,NULL,NULL),(657,'169.254.2.146',1,1,NULL,NULL,NULL),(658,'169.254.2.147',1,1,NULL,NULL,NULL),(659,'169.254.2.148',1,1,NULL,NULL,NULL),(660,'169.254.2.149',1,1,NULL,NULL,NULL),(661,'169.254.2.150',1,1,NULL,NULL,NULL),(662,'169.254.2.151',1,1,NULL,NULL,NULL),(663,'169.254.2.152',1,1,NULL,NULL,NULL),(664,'169.254.2.153',1,1,NULL,NULL,NULL),(665,'169.254.2.154',1,1,NULL,NULL,NULL),(666,'169.254.2.155',1,1,NULL,NULL,NULL),(667,'169.254.2.156',1,1,NULL,NULL,NULL),(668,'169.254.2.157',1,1,NULL,NULL,NULL),(669,'169.254.2.158',1,1,NULL,NULL,NULL),(670,'169.254.2.159',1,1,NULL,NULL,NULL),(671,'169.254.2.160',1,1,NULL,NULL,NULL),(672,'169.254.2.161',1,1,NULL,NULL,NULL),(673,'169.254.2.162',1,1,NULL,NULL,NULL),(674,'169.254.2.163',1,1,NULL,NULL,NULL),(675,'169.254.2.164',1,1,NULL,NULL,NULL),(676,'169.254.2.165',1,1,NULL,NULL,NULL),(677,'169.254.2.166',1,1,NULL,NULL,NULL),(678,'169.254.2.167',1,1,NULL,NULL,NULL),(679,'169.254.2.168',1,1,NULL,NULL,NULL),(680,'169.254.2.169',1,1,NULL,NULL,NULL),(681,'169.254.2.170',1,1,NULL,NULL,NULL),(682,'169.254.2.171',1,1,NULL,NULL,NULL),(683,'169.254.2.172',1,1,NULL,NULL,NULL),(684,'169.254.2.173',1,1,NULL,NULL,NULL),(685,'169.254.2.174',1,1,NULL,NULL,NULL),(686,'169.254.2.175',1,1,NULL,NULL,NULL),(687,'169.254.2.176',1,1,NULL,NULL,NULL),(688,'169.254.2.177',1,1,NULL,NULL,NULL),(689,'169.254.2.178',1,1,NULL,NULL,NULL),(690,'169.254.2.179',1,1,NULL,NULL,NULL),(691,'169.254.2.180',1,1,NULL,NULL,NULL),(692,'169.254.2.181',1,1,NULL,NULL,NULL),(693,'169.254.2.182',1,1,NULL,NULL,NULL),(694,'169.254.2.183',1,1,NULL,NULL,NULL),(695,'169.254.2.184',1,1,NULL,NULL,NULL),(696,'169.254.2.185',1,1,NULL,NULL,NULL),(697,'169.254.2.186',1,1,NULL,NULL,NULL),(698,'169.254.2.187',1,1,NULL,NULL,NULL),(699,'169.254.2.188',1,1,NULL,NULL,NULL),(700,'169.254.2.189',1,1,NULL,NULL,NULL),(701,'169.254.2.190',1,1,NULL,NULL,NULL),(702,'169.254.2.191',1,1,NULL,NULL,NULL),(703,'169.254.2.192',1,1,NULL,NULL,NULL),(704,'169.254.2.193',1,1,NULL,NULL,NULL),(705,'169.254.2.194',1,1,NULL,NULL,NULL),(706,'169.254.2.195',1,1,NULL,NULL,NULL),(707,'169.254.2.196',1,1,NULL,NULL,NULL),(708,'169.254.2.197',1,1,NULL,NULL,NULL),(709,'169.254.2.198',1,1,NULL,NULL,NULL),(710,'169.254.2.199',1,1,NULL,NULL,NULL),(711,'169.254.2.200',1,1,NULL,NULL,NULL),(712,'169.254.2.201',1,1,NULL,NULL,NULL),(713,'169.254.2.202',1,1,NULL,NULL,NULL),(714,'169.254.2.203',1,1,NULL,NULL,NULL),(715,'169.254.2.204',1,1,NULL,NULL,NULL),(716,'169.254.2.205',1,1,NULL,NULL,NULL),(717,'169.254.2.206',1,1,NULL,NULL,NULL),(718,'169.254.2.207',1,1,NULL,NULL,NULL),(719,'169.254.2.208',1,1,NULL,NULL,NULL),(720,'169.254.2.209',1,1,NULL,NULL,NULL),(721,'169.254.2.210',1,1,NULL,NULL,NULL),(722,'169.254.2.211',1,1,NULL,NULL,NULL),(723,'169.254.2.212',1,1,NULL,NULL,NULL),(724,'169.254.2.213',1,1,NULL,NULL,NULL),(725,'169.254.2.214',1,1,NULL,NULL,NULL),(726,'169.254.2.215',1,1,NULL,NULL,NULL),(727,'169.254.2.216',1,1,NULL,NULL,NULL),(728,'169.254.2.217',1,1,NULL,NULL,NULL),(729,'169.254.2.218',1,1,NULL,NULL,NULL),(730,'169.254.2.219',1,1,NULL,NULL,NULL),(731,'169.254.2.220',1,1,NULL,NULL,NULL),(732,'169.254.2.221',1,1,NULL,NULL,NULL),(733,'169.254.2.222',1,1,NULL,NULL,NULL),(734,'169.254.2.223',1,1,NULL,NULL,NULL),(735,'169.254.2.224',1,1,NULL,NULL,NULL),(736,'169.254.2.225',1,1,NULL,NULL,NULL),(737,'169.254.2.226',1,1,NULL,NULL,NULL),(738,'169.254.2.227',1,1,NULL,NULL,NULL),(739,'169.254.2.228',1,1,NULL,NULL,NULL),(740,'169.254.2.229',1,1,NULL,NULL,NULL),(741,'169.254.2.230',1,1,NULL,NULL,NULL),(742,'169.254.2.231',1,1,NULL,NULL,NULL),(743,'169.254.2.232',1,1,NULL,NULL,NULL),(744,'169.254.2.233',1,1,NULL,NULL,NULL),(745,'169.254.2.234',1,1,NULL,NULL,NULL),(746,'169.254.2.235',1,1,NULL,NULL,NULL),(747,'169.254.2.236',1,1,NULL,NULL,NULL),(748,'169.254.2.237',1,1,NULL,NULL,NULL),(749,'169.254.2.238',1,1,NULL,NULL,NULL),(750,'169.254.2.239',1,1,NULL,NULL,NULL),(751,'169.254.2.240',1,1,NULL,NULL,NULL),(752,'169.254.2.241',1,1,NULL,NULL,NULL),(753,'169.254.2.242',1,1,NULL,NULL,NULL),(754,'169.254.2.243',1,1,NULL,NULL,NULL),(755,'169.254.2.244',1,1,NULL,NULL,NULL),(756,'169.254.2.245',1,1,NULL,NULL,NULL),(757,'169.254.2.246',1,1,NULL,NULL,NULL),(758,'169.254.2.247',1,1,NULL,NULL,NULL),(759,'169.254.2.248',1,1,NULL,NULL,NULL),(760,'169.254.2.249',1,1,NULL,NULL,NULL),(761,'169.254.2.250',1,1,NULL,NULL,NULL),(762,'169.254.2.251',1,1,NULL,NULL,NULL),(763,'169.254.2.252',1,1,NULL,NULL,NULL),(764,'169.254.2.253',1,1,NULL,NULL,NULL),(765,'169.254.2.254',1,1,NULL,NULL,NULL),(766,'169.254.2.255',1,1,NULL,NULL,NULL),(767,'169.254.3.0',1,1,NULL,NULL,NULL),(768,'169.254.3.1',1,1,NULL,NULL,NULL),(769,'169.254.3.2',1,1,NULL,NULL,NULL),(770,'169.254.3.3',1,1,NULL,NULL,NULL),(771,'169.254.3.4',1,1,NULL,NULL,NULL),(772,'169.254.3.5',1,1,NULL,NULL,NULL),(773,'169.254.3.6',1,1,NULL,NULL,NULL),(774,'169.254.3.7',1,1,NULL,NULL,NULL),(775,'169.254.3.8',1,1,NULL,NULL,NULL),(776,'169.254.3.9',1,1,NULL,NULL,NULL),(777,'169.254.3.10',1,1,NULL,NULL,NULL),(778,'169.254.3.11',1,1,NULL,NULL,NULL),(779,'169.254.3.12',1,1,NULL,NULL,NULL),(780,'169.254.3.13',1,1,NULL,NULL,NULL),(781,'169.254.3.14',1,1,NULL,NULL,NULL),(782,'169.254.3.15',1,1,NULL,NULL,NULL),(783,'169.254.3.16',1,1,NULL,NULL,NULL),(784,'169.254.3.17',1,1,NULL,NULL,NULL),(785,'169.254.3.18',1,1,NULL,NULL,NULL),(786,'169.254.3.19',1,1,NULL,NULL,NULL),(787,'169.254.3.20',1,1,NULL,NULL,NULL),(788,'169.254.3.21',1,1,NULL,NULL,NULL),(789,'169.254.3.22',1,1,NULL,NULL,NULL),(790,'169.254.3.23',1,1,NULL,NULL,NULL),(791,'169.254.3.24',1,1,NULL,NULL,NULL),(792,'169.254.3.25',1,1,NULL,NULL,NULL),(793,'169.254.3.26',1,1,NULL,NULL,NULL),(794,'169.254.3.27',1,1,NULL,NULL,NULL),(795,'169.254.3.28',1,1,NULL,NULL,NULL),(796,'169.254.3.29',1,1,NULL,NULL,NULL),(797,'169.254.3.30',1,1,NULL,NULL,NULL),(798,'169.254.3.31',1,1,NULL,NULL,NULL),(799,'169.254.3.32',1,1,NULL,NULL,NULL),(800,'169.254.3.33',1,1,NULL,NULL,NULL),(801,'169.254.3.34',1,1,NULL,NULL,NULL),(802,'169.254.3.35',1,1,NULL,NULL,NULL),(803,'169.254.3.36',1,1,NULL,NULL,NULL),(804,'169.254.3.37',1,1,NULL,NULL,NULL),(805,'169.254.3.38',1,1,NULL,NULL,NULL),(806,'169.254.3.39',1,1,NULL,NULL,NULL),(807,'169.254.3.40',1,1,NULL,NULL,NULL),(808,'169.254.3.41',1,1,NULL,NULL,NULL),(809,'169.254.3.42',1,1,NULL,NULL,NULL),(810,'169.254.3.43',1,1,NULL,NULL,NULL),(811,'169.254.3.44',1,1,NULL,NULL,NULL),(812,'169.254.3.45',1,1,NULL,NULL,NULL),(813,'169.254.3.46',1,1,NULL,NULL,NULL),(814,'169.254.3.47',1,1,NULL,NULL,NULL),(815,'169.254.3.48',1,1,NULL,NULL,NULL),(816,'169.254.3.49',1,1,NULL,NULL,NULL),(817,'169.254.3.50',1,1,NULL,NULL,NULL),(818,'169.254.3.51',1,1,NULL,NULL,NULL),(819,'169.254.3.52',1,1,NULL,NULL,NULL),(820,'169.254.3.53',1,1,NULL,NULL,NULL),(821,'169.254.3.54',1,1,NULL,NULL,NULL),(822,'169.254.3.55',1,1,NULL,NULL,NULL),(823,'169.254.3.56',1,1,NULL,NULL,NULL),(824,'169.254.3.57',1,1,NULL,NULL,NULL),(825,'169.254.3.58',1,1,NULL,NULL,NULL),(826,'169.254.3.59',1,1,NULL,NULL,NULL),(827,'169.254.3.60',1,1,NULL,NULL,NULL),(828,'169.254.3.61',1,1,NULL,NULL,NULL),(829,'169.254.3.62',1,1,NULL,NULL,NULL),(830,'169.254.3.63',1,1,NULL,NULL,NULL),(831,'169.254.3.64',1,1,NULL,NULL,NULL),(832,'169.254.3.65',1,1,NULL,NULL,NULL),(833,'169.254.3.66',1,1,NULL,NULL,NULL),(834,'169.254.3.67',1,1,NULL,NULL,NULL),(835,'169.254.3.68',1,1,NULL,NULL,NULL),(836,'169.254.3.69',1,1,NULL,NULL,NULL),(837,'169.254.3.70',1,1,NULL,NULL,NULL),(838,'169.254.3.71',1,1,NULL,NULL,NULL),(839,'169.254.3.72',1,1,NULL,NULL,NULL),(840,'169.254.3.73',1,1,NULL,NULL,NULL),(841,'169.254.3.74',1,1,NULL,NULL,NULL),(842,'169.254.3.75',1,1,NULL,NULL,NULL),(843,'169.254.3.76',1,1,NULL,NULL,NULL),(844,'169.254.3.77',1,1,NULL,NULL,NULL),(845,'169.254.3.78',1,1,NULL,NULL,NULL),(846,'169.254.3.79',1,1,NULL,NULL,NULL),(847,'169.254.3.80',1,1,NULL,NULL,NULL),(848,'169.254.3.81',1,1,NULL,NULL,NULL),(849,'169.254.3.82',1,1,NULL,NULL,NULL),(850,'169.254.3.83',1,1,NULL,NULL,NULL),(851,'169.254.3.84',1,1,NULL,NULL,NULL),(852,'169.254.3.85',1,1,NULL,NULL,NULL),(853,'169.254.3.86',1,1,NULL,NULL,NULL),(854,'169.254.3.87',1,1,NULL,NULL,NULL),(855,'169.254.3.88',1,1,NULL,NULL,NULL),(856,'169.254.3.89',1,1,NULL,NULL,NULL),(857,'169.254.3.90',1,1,NULL,NULL,NULL),(858,'169.254.3.91',1,1,NULL,NULL,NULL),(859,'169.254.3.92',1,1,NULL,NULL,NULL),(860,'169.254.3.93',1,1,NULL,NULL,NULL),(861,'169.254.3.94',1,1,NULL,NULL,NULL),(862,'169.254.3.95',1,1,NULL,NULL,NULL),(863,'169.254.3.96',1,1,NULL,NULL,NULL),(864,'169.254.3.97',1,1,NULL,NULL,NULL),(865,'169.254.3.98',1,1,NULL,NULL,NULL),(866,'169.254.3.99',1,1,NULL,NULL,NULL),(867,'169.254.3.100',1,1,NULL,NULL,NULL),(868,'169.254.3.101',1,1,NULL,NULL,NULL),(869,'169.254.3.102',1,1,NULL,NULL,NULL),(870,'169.254.3.103',1,1,NULL,NULL,NULL),(871,'169.254.3.104',1,1,NULL,NULL,NULL),(872,'169.254.3.105',1,1,NULL,NULL,NULL),(873,'169.254.3.106',1,1,NULL,NULL,NULL),(874,'169.254.3.107',1,1,NULL,NULL,NULL),(875,'169.254.3.108',1,1,NULL,NULL,NULL),(876,'169.254.3.109',1,1,NULL,NULL,NULL),(877,'169.254.3.110',1,1,NULL,NULL,NULL),(878,'169.254.3.111',1,1,NULL,NULL,NULL),(879,'169.254.3.112',1,1,NULL,NULL,NULL),(880,'169.254.3.113',1,1,NULL,NULL,NULL),(881,'169.254.3.114',1,1,NULL,NULL,NULL),(882,'169.254.3.115',1,1,NULL,NULL,NULL),(883,'169.254.3.116',1,1,NULL,NULL,NULL),(884,'169.254.3.117',1,1,NULL,NULL,NULL),(885,'169.254.3.118',1,1,NULL,NULL,NULL),(886,'169.254.3.119',1,1,NULL,NULL,NULL),(887,'169.254.3.120',1,1,NULL,NULL,NULL),(888,'169.254.3.121',1,1,NULL,NULL,NULL),(889,'169.254.3.122',1,1,NULL,NULL,NULL),(890,'169.254.3.123',1,1,NULL,NULL,NULL),(891,'169.254.3.124',1,1,NULL,NULL,NULL),(892,'169.254.3.125',1,1,NULL,NULL,NULL),(893,'169.254.3.126',1,1,NULL,NULL,NULL),(894,'169.254.3.127',1,1,NULL,NULL,NULL),(895,'169.254.3.128',1,1,NULL,NULL,NULL),(896,'169.254.3.129',1,1,NULL,NULL,NULL),(897,'169.254.3.130',1,1,NULL,NULL,NULL),(898,'169.254.3.131',1,1,NULL,NULL,NULL),(899,'169.254.3.132',1,1,NULL,NULL,NULL),(900,'169.254.3.133',1,1,NULL,NULL,NULL),(901,'169.254.3.134',1,1,NULL,NULL,NULL),(902,'169.254.3.135',1,1,NULL,NULL,NULL),(903,'169.254.3.136',1,1,NULL,NULL,NULL),(904,'169.254.3.137',1,1,NULL,NULL,NULL),(905,'169.254.3.138',1,1,NULL,NULL,NULL),(906,'169.254.3.139',1,1,NULL,NULL,NULL),(907,'169.254.3.140',1,1,NULL,NULL,NULL),(908,'169.254.3.141',1,1,NULL,NULL,NULL),(909,'169.254.3.142',1,1,NULL,NULL,NULL),(910,'169.254.3.143',1,1,NULL,NULL,NULL),(911,'169.254.3.144',1,1,NULL,NULL,NULL),(912,'169.254.3.145',1,1,NULL,NULL,NULL),(913,'169.254.3.146',1,1,NULL,NULL,NULL),(914,'169.254.3.147',1,1,NULL,NULL,NULL),(915,'169.254.3.148',1,1,NULL,NULL,NULL),(916,'169.254.3.149',1,1,NULL,NULL,NULL),(917,'169.254.3.150',1,1,NULL,NULL,NULL),(918,'169.254.3.151',1,1,NULL,NULL,NULL),(919,'169.254.3.152',1,1,NULL,NULL,NULL),(920,'169.254.3.153',1,1,NULL,NULL,NULL),(921,'169.254.3.154',1,1,NULL,NULL,NULL),(922,'169.254.3.155',1,1,NULL,NULL,NULL),(923,'169.254.3.156',1,1,NULL,NULL,NULL),(924,'169.254.3.157',1,1,NULL,NULL,NULL),(925,'169.254.3.158',1,1,NULL,NULL,NULL),(926,'169.254.3.159',1,1,NULL,NULL,NULL),(927,'169.254.3.160',1,1,NULL,NULL,NULL),(928,'169.254.3.161',1,1,NULL,NULL,NULL),(929,'169.254.3.162',1,1,NULL,NULL,NULL),(930,'169.254.3.163',1,1,NULL,NULL,NULL),(931,'169.254.3.164',1,1,NULL,NULL,NULL),(932,'169.254.3.165',1,1,NULL,NULL,NULL),(933,'169.254.3.166',1,1,NULL,NULL,NULL),(934,'169.254.3.167',1,1,NULL,NULL,NULL),(935,'169.254.3.168',1,1,NULL,NULL,NULL),(936,'169.254.3.169',1,1,NULL,NULL,NULL),(937,'169.254.3.170',1,1,NULL,NULL,NULL),(938,'169.254.3.171',1,1,NULL,NULL,NULL),(939,'169.254.3.172',1,1,NULL,NULL,NULL),(940,'169.254.3.173',1,1,NULL,NULL,NULL),(941,'169.254.3.174',1,1,NULL,NULL,NULL),(942,'169.254.3.175',1,1,NULL,NULL,NULL),(943,'169.254.3.176',1,1,NULL,NULL,NULL),(944,'169.254.3.177',1,1,NULL,NULL,NULL),(945,'169.254.3.178',1,1,NULL,NULL,NULL),(946,'169.254.3.179',1,1,NULL,NULL,NULL),(947,'169.254.3.180',1,1,NULL,NULL,NULL),(948,'169.254.3.181',1,1,NULL,NULL,NULL),(949,'169.254.3.182',1,1,NULL,NULL,NULL),(950,'169.254.3.183',1,1,NULL,NULL,NULL),(951,'169.254.3.184',1,1,NULL,NULL,NULL),(952,'169.254.3.185',1,1,NULL,NULL,NULL),(953,'169.254.3.186',1,1,NULL,NULL,NULL),(954,'169.254.3.187',1,1,NULL,NULL,NULL),(955,'169.254.3.188',1,1,NULL,NULL,NULL),(956,'169.254.3.189',1,1,NULL,NULL,NULL),(957,'169.254.3.190',1,1,NULL,NULL,NULL),(958,'169.254.3.191',1,1,NULL,NULL,NULL),(959,'169.254.3.192',1,1,NULL,NULL,NULL),(960,'169.254.3.193',1,1,NULL,NULL,NULL),(961,'169.254.3.194',1,1,NULL,NULL,NULL),(962,'169.254.3.195',1,1,NULL,NULL,NULL),(963,'169.254.3.196',1,1,NULL,NULL,NULL),(964,'169.254.3.197',1,1,NULL,NULL,NULL),(965,'169.254.3.198',1,1,NULL,NULL,NULL),(966,'169.254.3.199',1,1,NULL,NULL,NULL),(967,'169.254.3.200',1,1,NULL,NULL,NULL),(968,'169.254.3.201',1,1,NULL,NULL,NULL),(969,'169.254.3.202',1,1,NULL,NULL,NULL),(970,'169.254.3.203',1,1,NULL,NULL,NULL),(971,'169.254.3.204',1,1,NULL,NULL,NULL),(972,'169.254.3.205',1,1,NULL,NULL,NULL),(973,'169.254.3.206',1,1,NULL,NULL,NULL),(974,'169.254.3.207',1,1,NULL,NULL,NULL),(975,'169.254.3.208',1,1,NULL,NULL,NULL),(976,'169.254.3.209',1,1,NULL,NULL,NULL),(977,'169.254.3.210',1,1,NULL,NULL,NULL),(978,'169.254.3.211',1,1,NULL,NULL,NULL),(979,'169.254.3.212',1,1,NULL,NULL,NULL),(980,'169.254.3.213',1,1,NULL,NULL,NULL),(981,'169.254.3.214',1,1,NULL,NULL,NULL),(982,'169.254.3.215',1,1,NULL,NULL,NULL),(983,'169.254.3.216',1,1,NULL,NULL,NULL),(984,'169.254.3.217',1,1,NULL,NULL,NULL),(985,'169.254.3.218',1,1,NULL,NULL,NULL),(986,'169.254.3.219',1,1,NULL,NULL,NULL),(987,'169.254.3.220',1,1,NULL,NULL,NULL),(988,'169.254.3.221',1,1,NULL,NULL,NULL),(989,'169.254.3.222',1,1,NULL,NULL,NULL),(990,'169.254.3.223',1,1,NULL,NULL,NULL),(991,'169.254.3.224',1,1,NULL,NULL,NULL),(992,'169.254.3.225',1,1,NULL,NULL,NULL),(993,'169.254.3.226',1,1,NULL,NULL,NULL),(994,'169.254.3.227',1,1,NULL,NULL,NULL),(995,'169.254.3.228',1,1,NULL,NULL,NULL),(996,'169.254.3.229',1,1,NULL,NULL,NULL),(997,'169.254.3.230',1,1,NULL,NULL,NULL),(998,'169.254.3.231',1,1,NULL,NULL,NULL),(999,'169.254.3.232',1,1,NULL,NULL,NULL),(1000,'169.254.3.233',1,1,NULL,NULL,NULL),(1001,'169.254.3.234',1,1,NULL,NULL,NULL),(1002,'169.254.3.235',1,1,NULL,NULL,NULL),(1003,'169.254.3.236',1,1,NULL,NULL,NULL),(1004,'169.254.3.237',1,1,NULL,NULL,NULL),(1005,'169.254.3.238',1,1,NULL,NULL,NULL),(1006,'169.254.3.239',1,1,NULL,NULL,NULL),(1007,'169.254.3.240',1,1,NULL,NULL,NULL),(1008,'169.254.3.241',1,1,NULL,NULL,NULL),(1009,'169.254.3.242',1,1,NULL,NULL,NULL),(1010,'169.254.3.243',1,1,NULL,NULL,NULL),(1011,'169.254.3.244',1,1,NULL,NULL,NULL),(1012,'169.254.3.245',1,1,NULL,NULL,NULL),(1013,'169.254.3.246',1,1,NULL,NULL,NULL),(1014,'169.254.3.247',1,1,NULL,NULL,NULL),(1015,'169.254.3.248',1,1,NULL,NULL,NULL),(1016,'169.254.3.249',1,1,NULL,NULL,NULL),(1017,'169.254.3.250',1,1,NULL,NULL,NULL),(1018,'169.254.3.251',1,1,NULL,NULL,NULL),(1019,'169.254.3.252',1,1,NULL,NULL,NULL),(1020,'169.254.3.253',1,1,NULL,NULL,NULL),(1021,'169.254.3.254',1,1,NULL,NULL,NULL);
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `op_dc_vnet_alloc`
--

LOCK TABLES `op_dc_vnet_alloc` WRITE;
/*!40000 ALTER TABLE `op_dc_vnet_alloc` DISABLE KEYS */;
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
  CONSTRAINT `fk_op_ha_work__instance_id` FOREIGN KEY (`instance_id`) REFERENCES `vm_instance` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_op_ha_work__host_id` FOREIGN KEY (`host_id`) REFERENCES `host` (`id`),
  CONSTRAINT `fk_op_ha_work__mgmt_server_id` FOREIGN KEY (`mgmt_server_id`) REFERENCES `mshost` (`msid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `op_ha_work`
--

LOCK TABLES `op_ha_work` WRITE;
/*!40000 ALTER TABLE `op_ha_work` DISABLE KEYS */;
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
INSERT INTO `op_host` VALUES (1,1160),(2,3680),(3,3),(4,82);
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
) ENGINE=InnoDB AUTO_INCREMENT=493 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `op_host_capacity`
--

LOCK TABLES `op_host_capacity` WRITE;
/*!40000 ALTER TABLE `op_host_capacity` DISABLE KEYS */;
INSERT INTO `op_host_capacity` VALUES (9,2,1,1,1912,0,9040,1),(10,2,1,1,2013265920,0,3701658240,0),(11,200,1,1,419624222720,0,1930519904256,2),(12,200,1,1,42798678016,0,3861039808512,3),(32,1,1,1,419624222720,0,1930519904256,6),(469,4,1,1,0,0,9040,1),(470,4,1,1,0,0,3701658240,0),(491,NULL,1,NULL,7,0,26,4),(492,NULL,1,1,2,0,5,5);
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
  PRIMARY KEY  (`id`),
  KEY `fk_op_it_work__mgmt_server_id` (`mgmt_server_id`),
  KEY `fk_op_it_work__instance_id` (`instance_id`),
  KEY `i_op_it_work__step` (`step`),
  CONSTRAINT `fk_op_it_work__mgmt_server_id` FOREIGN KEY (`mgmt_server_id`) REFERENCES `mshost` (`msid`),
  CONSTRAINT `fk_op_it_work__instance_id` FOREIGN KEY (`instance_id`) REFERENCES `vm_instance` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `op_it_work`
--

LOCK TABLES `op_it_work` WRITE;
/*!40000 ALTER TABLE `op_it_work` DISABLE KEYS */;
INSERT INTO `op_it_work` VALUES ('0f2ce974-b4c8-43ef-b190-3323f1417ee0',6603060216574,1273231375,'Job-Executor-2','Starting','Done',1273231383,5,NULL,0),('3587e6cc-329d-4f5f-a68c-74d9547e882e',6603060216574,1273231480,'Job-Executor-3','Starting','Done',1273231488,7,NULL,0),('591959ae-f08c-4b7c-86ce-5ece82b4e37d',6603060216574,1273229241,'CP-Scan-1','Starting','Done',1273229368,1,NULL,0),('a8e10db0-8baf-4771-875c-efacd802005b',6603060216574,1273230352,'Job-Executor-1','Starting','Done',1273230385,4,NULL,0),('b1d47f5a-edc8-4246-bcbb-e79b0e1d9007',6603060216574,1273229241,'SS-Scan-1','Starting','Done',1273229339,2,NULL,0),('cee67e6c-bc23-4f5e-98ba-254c1639f18c',6603060216574,1273230290,'Job-Executor-1','Starting','Done',1273230394,3,NULL,0),('f7bf134b-1a90-447b-ae22-1567acf80773',6603060216574,1273231465,'Job-Executor-3','Starting','Done',1273231474,6,NULL,0);
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
INSERT INTO `op_networks` VALUES (200,1,2,0,1),(201,1,3,0,1),(202,1,0,0,0),(203,1,7,1,1);
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
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `pod_vlan_map`
--

LOCK TABLES `pod_vlan_map` WRITE;
/*!40000 ALTER TABLE `pod_vlan_map` DISABLE KEYS */;
INSERT INTO `pod_vlan_map` VALUES (1,1,1);
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
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `resource_count`
--

LOCK TABLES `resource_count` WRITE;
/*!40000 ALTER TABLE `resource_count` DISABLE KEYS */;
INSERT INTO `resource_count` VALUES (1,2,NULL,'volume',3),(2,NULL,1,'volume',6),(3,2,NULL,'user_vm',2),(4,NULL,1,'user_vm',4),(5,2,NULL,'public_ip',2),(6,NULL,1,'public_ip',4),(7,3,NULL,'volume',3),(8,NULL,2,'volume',3),(9,3,NULL,'user_vm',2),(10,NULL,2,'user_vm',2),(11,3,NULL,'public_ip',2),(12,NULL,2,'public_ip',2),(13,3,NULL,'snapshot',3),(14,NULL,2,'snapshot',3),(15,NULL,1,'snapshot',6),(16,2,NULL,'snapshot',3),(17,2,NULL,'template',4),(18,NULL,1,'template',7),(19,3,NULL,'template',3),(20,NULL,2,'template',3);
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `resource_limit`
--

LOCK TABLES `resource_limit` WRITE;
/*!40000 ALTER TABLE `resource_limit` DISABLE KEYS */;
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
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `secondary_storage_vm`
--

LOCK TABLES `secondary_storage_vm` WRITE;
/*!40000 ALTER TABLE `secondary_storage_vm` DISABLE KEYS */;
INSERT INTO `secondary_storage_vm` VALUES (2,'06:79:fe:00:00:19','192.168.164.124','255.255.255.0',0,NULL,NULL,NULL);
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `security_group`
--

LOCK TABLES `security_group` WRITE;
/*!40000 ALTER TABLE `security_group` DISABLE KEYS */;
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
INSERT INTO `sequence` VALUES ('networks_seq',204),('private_mac_address_seq',1),('public_mac_address_seq',1),('snapshots_seq',1),('storage_pool_seq',201),('vm_instance_seq',8),('vm_template_seq',209),('volume_seq',1);
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
INSERT INTO `service_offering` VALUES (1,1,500,512,200,10,0,'Direct',NULL),(2,1,1000,1024,200,10,0,'Direct',NULL),(6,1,512,1024,0,0,1,'Virtual',NULL),(7,1,500,256,0,0,1,'Virtual',NULL),(8,1,500,128,0,0,1,'Virtual',NULL),(9,1,100,128,200,10,0,'Virtual',NULL);
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
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `snapshot_policy`
--

LOCK TABLES `snapshot_policy` WRITE;
/*!40000 ALTER TABLE `snapshot_policy` DISABLE KEYS */;
INSERT INTO `snapshot_policy` VALUES (1,0,'00','GMT',4,0,1);
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `snapshot_schedule`
--

LOCK TABLES `snapshot_schedule` WRITE;
/*!40000 ALTER TABLE `snapshot_schedule` DISABLE KEYS */;
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
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `snapshots`
--

LOCK TABLES `snapshots` WRITE;
/*!40000 ALTER TABLE `snapshots` DISABLE KEYS */;
INSERT INTO `snapshots` VALUES (1,3,9,'BackedUp','f6a80f17-3642-4a38-bb6e-0aceee4ae9aa','i-3-7-VM_ROOT-7_20110426040546',0,'MANUAL','2011-04-26 04:05:46',NULL,'8dcb93c7-e07f-4f06-a0a2-fa31f932b9d2',0,'XenServer'),(2,3,7,'BackedUp','6c2c98ba-d47b-4e10-838e-57a7b9fd3c7a','i-3-6-VM_ROOT-6_20110426040550',0,'MANUAL','2011-04-26 04:05:50',NULL,'db11747d-82af-4dfd-926a-77d1ddf2970e',0,'XenServer'),(3,3,8,'BackedUp','b5d46929-5435-407b-9041-f0f615c37e7d','i-3-6-VM_DATA-6_20110426040555',0,'MANUAL','2011-04-26 04:05:55',NULL,'9852ca44-5037-4308-b6ab-a71057abe574',0,'XenServer'),(4,2,6,'BackedUp','546ed361-7ee8-4c89-9866-d03bd4690f4d','i-2-5-VM_ROOT-5_20110426040559',0,'MANUAL','2011-04-26 04:05:59',NULL,'d0e4db21-f9f9-4480-bc80-d9a017084561',0,'XenServer'),(5,2,3,'BackedUp','5b575eed-2d72-449b-a944-32a8b78d09a3','i-2-3-VM_ROOT-3_20110426040603',0,'MANUAL','2011-04-26 04:06:03',NULL,'bc2cd098-d75f-44a0-b91c-b16266d6940c',0,'XenServer'),(6,2,4,'BackedUp','f2ac1393-4936-4af6-b162-e2fdbc24c323','i-2-3-VM_DATA-3_20110426040607',0,'MANUAL','2011-04-26 04:06:07',NULL,'c311ecef-5d38-459b-be16-3c2788d21fed',0,'XenServer');
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
  CONSTRAINT `fk_ssh_keypairs__domain_id` FOREIGN KEY (`domain_id`) REFERENCES `domain` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_ssh_keypairs__account_id` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`) ON DELETE CASCADE
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
INSERT INTO `storage_pool` VALUES (200,'xenPrimary164','ccbd267c-e4b5-384e-bdf0-f1fecf72aca2','NetworkFilesystem',2049,1,1,1,1510895681536,1930519904256,'192.168.110.232','/export/home/chandan/pigeon/primary220','2011-04-26 02:58:38',NULL,NULL,'Up');
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
INSERT INTO `storage_pool_host_ref` VALUES (1,2,200,'2011-04-26 02:58:39',NULL,'/mnt/ccbd267c-e4b5-384e-bdf0-f1fecf72aca2'),(2,4,200,'2011-04-26 21:28:01',NULL,'/mnt/ccbd267c-e4b5-384e-bdf0-f1fecf72aca2');
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `storage_pool_work`
--

LOCK TABLES `storage_pool_work` WRITE;
/*!40000 ALTER TABLE `storage_pool_work` DISABLE KEYS */;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `sync_queue`
--

LOCK TABLES `sync_queue` WRITE;
/*!40000 ALTER TABLE `sync_queue` DISABLE KEYS */;
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
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `template_host_ref`
--

LOCK TABLES `template_host_ref` WRITE;
/*!40000 ALTER TABLE `template_host_ref` DISABLE KEYS */;
INSERT INTO `template_host_ref` VALUES (1,1,NULL,1,'2011-04-26 02:55:46','2011-04-26 03:40:38',NULL,100,2101252608,2101252608,'DOWNLOADED',NULL,NULL,'template/tmpl/1/1//f6d43e0e-07c4-48cb-994f-c5ef5ecb00a5.vhd','http://download.cloud.com/releases/2.2.0/systemvm.vhd.bz2',0,0),(2,1,NULL,3,'2011-04-26 02:55:46','2011-04-26 03:40:38',NULL,100,718929920,718929920,'DOWNLOADED',NULL,NULL,'template/tmpl/1/3//921119d0-9adc-4c28-ba58-50ff894ce490.qcow2','http://download.cloud.com/releases/2.2.0/systemvm.qcow2.bz2',0,0),(3,1,NULL,8,'2011-04-26 02:55:46','2011-04-26 03:40:38',NULL,100,314404864,314404864,'DOWNLOADED',NULL,NULL,'template/tmpl/1/8//798e413f-e515-4f4c-9e9f-d28ab3d74833.ova','http://download.cloud.com/releases/2.2.0/systemvm.ova',0,0),(4,1,NULL,2,'2011-04-26 02:55:46','2011-04-26 03:40:38',NULL,100,8589934592,1708331520,'DOWNLOADED','',NULL,'template/tmpl/1/2//48e278b9-8112-33ba-aba8-4092ddc29ffa.vhd','http://download.cloud.com/templates/builtin/f59f18fb-ae94-4f97-afd2-f84755767aca.vhd.bz2',0,0),(5,1,NULL,4,'2011-04-26 02:55:46','2011-04-26 02:55:46',NULL,0,0,0,'NOT_DOWNLOADED',NULL,NULL,NULL,'http://download.cloud.com/templates/builtin/eec2209b-9875-3c8d-92be-c001bd8a0faf.qcow2.bz2',0,0),(6,1,NULL,7,'2011-04-26 02:55:46','2011-04-26 02:55:46',NULL,0,0,0,'NOT_DOWNLOADED',NULL,NULL,NULL,'http://download.cloud.com/releases/2.2.0/CentOS5.3-x86_64.ova',0,0),(7,1,NULL,206,'2011-04-26 04:27:23','2011-04-26 04:27:23',NULL,100,1073741824,5120,'DOWNLOADED',NULL,NULL,'template/tmpl/2/206/d5ceac55-2d77-42bb-a2e1-3259b441ad2f.vhd',NULL,0,0),(8,1,NULL,207,'2011-04-26 04:27:38','2011-04-26 04:27:38',NULL,100,1073741824,5120,'DOWNLOADED',NULL,NULL,'template/tmpl/3/207/6f686bfb-9cc1-4b8a-90b3-37da41258929.vhd',NULL,0,0),(9,1,NULL,202,'2011-04-26 04:33:28','2011-04-26 04:33:28',NULL,100,8589934592,19456,'DOWNLOADED',NULL,NULL,'template/tmpl/2/202/df268edc-5459-47bc-84da-5cdcbe9c86d7.vhd',NULL,0,0),(10,1,NULL,203,'2011-04-26 04:33:29','2011-04-26 04:33:29',NULL,100,8589934592,19456,'DOWNLOADED',NULL,NULL,'template/tmpl/2/203/15e61efe-1d72-49d1-b0d8-974faa598911.vhd',NULL,0,0),(11,1,NULL,204,'2011-04-26 04:33:36','2011-04-26 04:33:36',NULL,100,8589934592,19456,'DOWNLOADED',NULL,NULL,'template/tmpl/3/204/d1870983-f231-401b-acfd-3e7f2c8b7189.vhd',NULL,0,0),(12,1,NULL,205,'2011-04-26 04:33:42','2011-04-26 04:33:42',NULL,100,8589934592,19456,'DOWNLOADED',NULL,NULL,'template/tmpl/3/205/99e56ebb-a548-43e6-8c01-ca22e818c1c4.vhd',NULL,0,0),(13,1,NULL,208,'2011-04-26 21:30:45','2011-04-26 21:32:29','27a42b0d-a1f0-4547-a167-05132de7b9b5',100,4393723904,4393723904,'DOWNLOADED','Install completed successfully at 4/26/11 4:31 PM','/mnt/SecStorage/6a304996/template/tmpl/2/208/dnld8683902212725628481tmp_','template/tmpl/2/208//208-2-a63a32c9-bad8-3f24-8e1d-17c85cc838be.iso','http://nfs1.lab.vmops.com/isos_64bit/CentOS-5.5-x86_64-bin-DVDs/CentOS-5.5-x86_64-bin-DVD-1of2.iso',0,0);
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
  CONSTRAINT `fk_template_spool_ref__template_id` FOREIGN KEY (`template_id`) REFERENCES `vm_template` (`id`),
  CONSTRAINT `fk_template_spool_ref__pool_id` FOREIGN KEY (`pool_id`) REFERENCES `storage_pool` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `template_spool_ref`
--

LOCK TABLES `template_spool_ref` WRITE;
/*!40000 ALTER TABLE `template_spool_ref` DISABLE KEYS */;
INSERT INTO `template_spool_ref` VALUES (1,200,1,'2011-04-26 02:59:04',NULL,NULL,100,'DOWNLOADED',NULL,'1790e686-9c63-4395-a44b-102d7a735f43','1790e686-9c63-4395-a44b-102d7a735f43',2101252608,0),(2,200,2,'2011-04-26 03:16:57',NULL,NULL,100,'DOWNLOADED',NULL,'e84713ee-6a0f-4c0f-bf68-383edfcdbbe2','e84713ee-6a0f-4c0f-bf68-383edfcdbbe2',1708331520,0);
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
  CONSTRAINT `fk_template_zone_ref__zone_id` FOREIGN KEY (`zone_id`) REFERENCES `data_center` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_template_zone_ref__template_id` FOREIGN KEY (`template_id`) REFERENCES `vm_template` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `template_zone_ref`
--

LOCK TABLES `template_zone_ref` WRITE;
/*!40000 ALTER TABLE `template_zone_ref` DISABLE KEYS */;
INSERT INTO `template_zone_ref` VALUES (1,1,1,'2011-04-26 02:55:46','2011-04-26 02:55:46',NULL),(2,1,2,'2011-04-26 02:55:46','2011-04-26 02:55:46',NULL),(3,1,3,'2011-04-26 02:55:46','2011-04-26 02:55:46',NULL),(4,1,4,'2011-04-26 02:55:46','2011-04-26 02:55:46',NULL),(5,1,7,'2011-04-26 02:55:46','2011-04-26 02:55:46',NULL),(6,1,8,'2011-04-26 02:55:46','2011-04-26 02:55:46',NULL),(7,1,206,'2011-04-26 04:27:22','2011-04-26 04:27:22',NULL),(8,1,207,'2011-04-26 04:27:38','2011-04-26 04:27:38',NULL),(9,1,202,'2011-04-26 04:33:28','2011-04-26 04:33:28',NULL),(10,1,203,'2011-04-26 04:33:29','2011-04-26 04:33:29',NULL),(11,1,204,'2011-04-26 04:33:36','2011-04-26 04:33:36',NULL),(12,1,205,'2011-04-26 04:33:42','2011-04-26 04:33:42',NULL),(13,1,208,'2011-04-26 21:30:45','2011-04-26 21:30:45',NULL);
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
) ENGINE=InnoDB AUTO_INCREMENT=36 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `usage_event`
--

LOCK TABLES `usage_event` WRITE;
/*!40000 ALTER TABLE `usage_event` DISABLE KEYS */;
INSERT INTO `usage_event` VALUES (1,'VOLUME.CREATE',2,'2011-04-26 03:16:56',1,3,'ROOT-3',NULL,2,8589934592,0),(2,'VOLUME.CREATE',2,'2011-04-26 03:16:56',1,4,'DATA-3',10,NULL,1073741824,0),(3,'VM.CREATE',2,'2011-04-26 03:16:56',1,3,'i-2-3-VM',9,2,NULL,0),(4,'NET.IPASSIGN',2,'2011-04-26 03:18:01',1,19,'192.168.164.123',NULL,NULL,0,0),(5,'VM.START',2,'2011-04-26 03:18:43',1,3,'i-2-3-VM',9,2,NULL,0),(6,'NETWORK.OFFERING.CREATE',2,'2011-04-26 03:18:43',1,3,'i-2-3-VM',5,NULL,1,0),(7,'VOLUME.CREATE',2,'2011-04-26 03:35:28',1,6,'ROOT-5',NULL,2,8589934592,0),(8,'VM.CREATE',2,'2011-04-26 03:35:28',1,5,'i-2-5-VM',9,2,NULL,0),(9,'NET.IPASSIGN',2,'2011-04-26 03:35:29',1,24,'192.168.164.128',NULL,NULL,0,0),(10,'VM.START',2,'2011-04-26 03:35:36',1,5,'i-2-5-VM',9,2,NULL,0),(11,'NETWORK.OFFERING.CREATE',2,'2011-04-26 03:35:36',1,5,'i-2-5-VM',5,NULL,1,0),(12,'VOLUME.CREATE',3,'2011-04-26 03:37:01',1,7,'ROOT-6',NULL,2,8589934592,0),(13,'VOLUME.CREATE',3,'2011-04-26 03:37:01',1,8,'DATA-6',10,NULL,1073741824,0),(14,'VM.CREATE',3,'2011-04-26 03:37:01',1,6,'i-3-6-VM',9,2,NULL,0),(15,'NET.IPASSIGN',3,'2011-04-26 03:37:02',1,18,'192.168.164.122',NULL,NULL,0,0),(16,'VM.START',3,'2011-04-26 03:37:10',1,6,'i-3-6-VM',9,2,NULL,0),(17,'NETWORK.OFFERING.CREATE',3,'2011-04-26 03:37:10',1,6,'i-3-6-VM',5,NULL,1,0),(18,'VOLUME.CREATE',3,'2011-04-26 03:37:16',1,9,'ROOT-7',NULL,2,8589934592,0),(19,'VM.CREATE',3,'2011-04-26 03:37:16',1,7,'i-3-7-VM',9,2,NULL,0),(20,'NET.IPASSIGN',3,'2011-04-26 03:37:17',1,22,'192.168.164.126',NULL,NULL,0,0),(21,'VM.START',3,'2011-04-26 03:37:25',1,7,'i-3-7-VM',9,2,NULL,0),(22,'NETWORK.OFFERING.CREATE',3,'2011-04-26 03:37:25',1,7,'i-3-7-VM',5,NULL,1,0),(23,'SNAPSHOT.CREATE',3,'2011-04-26 04:06:43',1,3,'i-3-6-VM_DATA-6_20110426040555',NULL,NULL,1073741824,0),(24,'SNAPSHOT.CREATE',2,'2011-04-26 04:07:10',1,6,'i-2-3-VM_DATA-3_20110426040607',NULL,NULL,1073741824,0),(25,'SNAPSHOT.CREATE',2,'2011-04-26 04:11:24',1,4,'i-2-5-VM_ROOT-5_20110426040559',NULL,NULL,8589934592,0),(26,'SNAPSHOT.CREATE',3,'2011-04-26 04:11:27',1,1,'i-3-7-VM_ROOT-7_20110426040546',NULL,NULL,8589934592,0),(27,'SNAPSHOT.CREATE',2,'2011-04-26 04:11:30',1,5,'i-2-3-VM_ROOT-3_20110426040603',NULL,NULL,8589934592,0),(28,'SNAPSHOT.CREATE',3,'2011-04-26 04:11:30',1,2,'i-3-6-VM_ROOT-6_20110426040550',NULL,NULL,8589934592,0),(29,'TEMPLATE.CREATE',2,'2011-04-26 04:27:23',1,206,'TemplateFromSnapshot-5-Data',NULL,NULL,1073741824,0),(30,'TEMPLATE.CREATE',3,'2011-04-26 04:27:38',1,207,'TemplateFromSnapshot-6-Data',NULL,NULL,1073741824,0),(31,'TEMPLATE.CREATE',2,'2011-04-26 04:33:28',1,202,'TemplateFromSnapshot-1',NULL,NULL,8589934592,0),(32,'TEMPLATE.CREATE',2,'2011-04-26 04:33:29',1,203,'TemplateFromSnapshot-2',NULL,NULL,8589934592,0),(33,'TEMPLATE.CREATE',3,'2011-04-26 04:33:37',1,204,'TemplateFromSnapshot-3',NULL,NULL,8589934592,0),(34,'TEMPLATE.CREATE',3,'2011-04-26 04:33:42',1,205,'TemplateFromSnapshot-4',NULL,NULL,8589934592,0),(35,'ISO.CREATE',2,'2011-04-26 21:32:29',1,208,'CentOS-5-5-ISO-Download',NULL,NULL,4393723904,0);
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
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'system','',1,'system','cloud',NULL,'enabled',NULL,NULL,'2011-04-25 21:24:11',NULL,NULL),(2,'admin','5f4dcc3b5aa765d61d8327deb882cf99',2,'admin','cloud','','enabled',NULL,NULL,'2011-04-25 21:24:11',NULL,NULL),(3,'nimbus','5f4dcc3b5aa765d61d8327deb882cf99',3,'nimbus','nimbuslast','nimbus@gmail.com','enabled',NULL,NULL,'2011-04-26 03:36:26',NULL,'America/Los_Angeles');
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
  CONSTRAINT `fk_user_ip_address__source_network_id` FOREIGN KEY (`source_network_id`) REFERENCES `networks` (`id`),
  CONSTRAINT `fk_user_ip_address__network_id` FOREIGN KEY (`network_id`) REFERENCES `networks` (`id`),
  CONSTRAINT `fk_user_ip_address__account_id` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`),
  CONSTRAINT `fk_user_ip_address__vlan_db_id` FOREIGN KEY (`vlan_db_id`) REFERENCES `vlan` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_user_ip_address__data_center_id` FOREIGN KEY (`data_center_id`) REFERENCES `data_center` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `user_ip_address`
--

LOCK TABLES `user_ip_address` WRITE;
/*!40000 ALTER TABLE `user_ip_address` DISABLE KEYS */;
INSERT INTO `user_ip_address` VALUES (1,NULL,NULL,'192.168.164.105',1,0,NULL,1,0,NULL,'Free',6,203,NULL),(2,NULL,NULL,'192.168.164.106',1,0,NULL,1,0,NULL,'Free',7,203,NULL),(3,1,1,'192.168.164.107',1,0,'2011-04-26 03:18:01',1,0,NULL,'Allocated',8,203,NULL),(4,NULL,NULL,'192.168.164.108',1,0,NULL,1,0,NULL,'Free',9,203,NULL),(5,NULL,NULL,'192.168.164.109',1,0,NULL,1,0,NULL,'Free',10,203,NULL),(6,NULL,NULL,'192.168.164.110',1,0,NULL,1,0,NULL,'Free',11,203,NULL),(7,NULL,NULL,'192.168.164.111',1,0,NULL,1,0,NULL,'Free',12,203,NULL),(8,NULL,NULL,'192.168.164.112',1,0,NULL,1,0,NULL,'Free',13,203,NULL),(9,NULL,NULL,'192.168.164.113',1,0,NULL,1,0,NULL,'Free',14,203,NULL),(10,NULL,NULL,'192.168.164.114',1,0,NULL,1,0,NULL,'Free',15,203,NULL),(11,NULL,NULL,'192.168.164.115',1,0,NULL,1,0,NULL,'Free',16,203,NULL),(12,NULL,NULL,'192.168.164.116',1,0,NULL,1,0,NULL,'Free',17,203,NULL),(13,NULL,NULL,'192.168.164.117',1,0,NULL,1,0,NULL,'Free',18,203,NULL),(14,NULL,NULL,'192.168.164.118',1,0,NULL,1,0,NULL,'Free',19,203,NULL),(15,NULL,NULL,'192.168.164.119',1,0,NULL,1,0,NULL,'Free',20,203,NULL),(16,NULL,NULL,'192.168.164.120',1,0,NULL,1,0,NULL,'Free',21,203,NULL),(17,NULL,NULL,'192.168.164.121',1,0,NULL,1,0,NULL,'Free',22,203,NULL),(18,3,2,'192.168.164.122',1,0,'2011-04-26 03:37:02',1,0,NULL,'Allocated',23,203,NULL),(19,2,1,'192.168.164.123',1,0,'2011-04-26 03:18:01',1,0,NULL,'Allocated',24,203,NULL),(20,1,1,'192.168.164.124',1,0,'2011-04-26 03:00:11',1,0,NULL,'Allocated',25,203,NULL),(21,1,1,'192.168.164.125',1,0,'2011-04-26 03:00:12',1,0,NULL,'Allocated',26,203,NULL),(22,3,2,'192.168.164.126',1,0,'2011-04-26 03:37:17',1,0,NULL,'Allocated',27,203,NULL),(23,NULL,NULL,'192.168.164.127',1,0,NULL,1,0,NULL,'Free',28,203,NULL),(24,2,1,'192.168.164.128',1,0,'2011-04-26 03:35:29',1,0,NULL,'Allocated',29,203,NULL),(25,NULL,NULL,'192.168.164.129',1,0,NULL,1,0,NULL,'Free',30,203,NULL),(26,NULL,NULL,'192.168.164.130',1,0,NULL,1,0,NULL,'Free',31,203,NULL);
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
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`),
  UNIQUE KEY `account_id` (`account_id`,`data_center_id`,`device_id`,`device_type`),
  KEY `i_user_statistics__account_id` (`account_id`),
  KEY `i_user_statistics__account_id_data_center_id` (`account_id`,`data_center_id`),
  CONSTRAINT `fk_user_statistics__account_id` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `user_statistics`
--

LOCK TABLES `user_statistics` WRITE;
/*!40000 ALTER TABLE `user_statistics` DISABLE KEYS */;
INSERT INTO `user_statistics` VALUES (1,1,1,NULL,4,'DomainRouter',0,0,0,0);
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
INSERT INTO `user_vm` VALUES (3,NULL,'Admin-VM-1',NULL,NULL,NULL,NULL),(5,NULL,'Admin-VM-2',NULL,NULL,NULL,NULL),(6,NULL,'Nimbus-VM-1',NULL,NULL,NULL,NULL),(7,NULL,'Nimbus-VM-2',NULL,NULL,NULL,NULL);
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
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `vlan`
--

LOCK TABLES `vlan` WRITE;
/*!40000 ALTER TABLE `vlan` DISABLE KEYS */;
INSERT INTO `vlan` VALUES (1,'untagged','192.168.164.1','255.255.255.0','192.168.164.105-192.168.164.130','DirectAttached',1,203);
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
  CONSTRAINT `fk_vm_instance__host_id` FOREIGN KEY (`host_id`) REFERENCES `host` (`id`),
  CONSTRAINT `fk_vm_instance__last_host_id` FOREIGN KEY (`last_host_id`) REFERENCES `host` (`id`),
  CONSTRAINT `fk_vm_instance__template_id` FOREIGN KEY (`vm_template_id`) REFERENCES `vm_template` (`id`),
  CONSTRAINT `fk_vm_instance__account_id` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`),
  CONSTRAINT `fk_vm_instance__service_offering_id` FOREIGN KEY (`service_offering_id`) REFERENCES `service_offering` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `vm_instance`
--

LOCK TABLES `vm_instance` WRITE;
/*!40000 ALTER TABLE `vm_instance` DISABLE KEYS */;
INSERT INTO `vm_instance` VALUES (1,'v-1-VM','v-1-VM','Running',1,15,'06:97:9e:00:00:01','192.168.164.100',NULL,1,1,2,2,1,'2011-04-26 22:12:19','54724f3f50d5ed3b',0,0,4,'2011-04-26 03:40:43','2011-04-26 02:59:03',NULL,'ConsoleProxy',1,1,6,'591959ae-f08c-4b7c-86ce-5ece82b4e37d','XenServer'),(2,'s-2-VM','s-2-VM','Running',1,15,'06:13:1e:00:00:05','192.168.164.104',NULL,1,1,2,2,NULL,NULL,'8c413ab428969734',1,0,4,'2011-04-26 03:40:43','2011-04-26 02:59:03',NULL,'SecondaryStorageVm',1,1,7,'b1d47f5a-edc8-4246-bcbb-e79b0e1d9007','XenServer'),(3,'i-2-3-VM','i-2-3-VM','Running',2,11,'06:93:bc:00:00:18','192.168.164.123',NULL,1,1,2,2,1,'2011-04-26 03:27:26','97ee8656de37c8b4',0,0,4,'2011-04-26 03:40:43','2011-04-26 03:16:56',NULL,'User',2,1,9,'cee67e6c-bc23-4f5e-98ba-254c1639f18c','XenServer'),(4,'r-4-VM','r-4-VM','Running',1,15,'0e:00:a9:fe:00:9a','169.254.0.154',NULL,1,1,2,2,NULL,NULL,'47f682f752645b83',1,0,4,'2011-04-26 03:40:43','2011-04-26 03:18:01',NULL,'DomainRouter',1,1,8,'a8e10db0-8baf-4771-875c-efacd802005b','XenServer'),(5,'i-2-5-VM','i-2-5-VM','Running',2,11,'06:c8:20:00:00:1d','192.168.164.128',NULL,1,1,2,2,NULL,NULL,'d027c9d2cee0613d',0,0,4,'2011-04-26 03:40:43','2011-04-26 03:35:28',NULL,'User',2,1,9,'0f2ce974-b4c8-43ef-b190-3323f1417ee0','XenServer'),(6,'i-3-6-VM','i-3-6-VM','Running',2,11,'06:93:80:00:00:17','192.168.164.122',NULL,1,1,2,2,NULL,NULL,'ff6016d6ef59118c',0,0,4,'2011-04-26 03:40:43','2011-04-26 03:37:01',NULL,'User',3,2,9,'f7bf134b-1a90-447b-ae22-1567acf80773','XenServer'),(7,'i-3-7-VM','i-3-7-VM','Running',2,11,'06:96:f6:00:00:1b','192.168.164.126',NULL,1,1,2,2,NULL,NULL,'9e24f63520e2a53e',0,0,4,'2011-04-26 03:40:43','2011-04-26 03:37:16',NULL,'User',3,2,9,'3587e6cc-329d-4f5f-a68c-74d9547e882e','XenServer');
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
) ENGINE=InnoDB AUTO_INCREMENT=209 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `vm_template`
--

LOCK TABLES `vm_template` WRITE;
/*!40000 ALTER TABLE `vm_template` DISABLE KEYS */;
INSERT INTO `vm_template` VALUES (1,'routing-1','SystemVM Template (XenServer)',0,0,'SYSTEM',0,64,'http://download.cloud.com/releases/2.2.0/systemvm.vhd.bz2','VHD','2011-04-25 21:20:47',NULL,1,'c33dfaf0937b35c25ef6a0fdd98f24d3','SystemVM Template (XenServer)',0,15,1,0,1,0,'XenServer'),(2,'centos53-x86_64','CentOS 5.3(64-bit) no GUI (XenServer)',1,1,'BUILTIN',0,64,'http://download.cloud.com/templates/builtin/f59f18fb-ae94-4f97-afd2-f84755767aca.vhd.bz2','VHD','2011-04-25 21:20:47',NULL,1,'b63d854a9560c013142567bbae8d98cf','CentOS 5.3(64-bit) no GUI (XenServer)',0,11,1,0,1,1,'XenServer'),(3,'routing-3','SystemVM Template (KVM)',0,0,'SYSTEM',0,64,'http://download.cloud.com/releases/2.2.0/systemvm.qcow2.bz2','QCOW2','2011-04-25 21:20:47',NULL,1,'ec463e677054f280f152fcc264255d2f','SystemVM Template (KVM)',0,15,1,0,1,0,'KVM'),(4,'centos55-x86_64','CentOS 5.5(64-bit) no GUI (KVM)',1,1,'BUILTIN',0,64,'http://download.cloud.com/templates/builtin/eec2209b-9875-3c8d-92be-c001bd8a0faf.qcow2.bz2','QCOW2','2011-04-25 21:20:47',NULL,1,'1da20ae69b54f761f3f733dce97adcc0','CentOS 5.5(64-bit) no GUI (KVM)',0,112,1,0,1,1,'KVM'),(7,'centos53-x64','CentOS 5.3(64-bit) no GUI (vSphere)',1,1,'BUILTIN',0,64,'http://download.cloud.com/releases/2.2.0/CentOS5.3-x86_64.ova','OVA','2011-04-25 21:20:47',NULL,1,'f6f881b7f2292948d8494db837fe0f47','CentOS 5.3(64-bit) no GUI (vSphere)',0,12,1,0,1,1,'VMware'),(8,'routing-8','SystemVM Template (vSphere)',0,0,'SYSTEM',0,32,'http://download.cloud.com/releases/2.2.0/systemvm.ova','OVA','2011-04-25 21:20:47',NULL,1,'3c9d4c704af44ebd1736e1bc78cec1fa','SystemVM Template (vSphere)',0,15,1,0,1,0,'VMware'),(200,'xs-tools.iso','xs-tools.iso',1,1,'PERHOST',1,64,NULL,'ISO','2011-04-26 01:24:31',NULL,1,NULL,'xen-pv-drv-iso',0,1,0,0,0,1,'None'),(201,'vmware-tools.iso','vmware-tools.iso',1,1,'PERHOST',1,64,NULL,'ISO','2011-04-26 01:24:31',NULL,1,NULL,'VMware Tools Installer ISO',0,1,0,0,0,1,'VMware'),(202,'df268edc-5459-47bc-84da-5cdcbe9c86d7','TemplateFromSnapshot-1',1,1,'USER',0,64,NULL,'VHD','2011-04-26 04:24:49',NULL,2,NULL,'TemplateFromSnapshot-1',0,12,1,0,0,1,'XenServer'),(203,'15e61efe-1d72-49d1-b0d8-974faa598911','TemplateFromSnapshot-2',1,1,'USER',0,64,NULL,'VHD','2011-04-26 04:25:02',NULL,2,NULL,'TemplateFromSnapshot-2',0,12,1,0,0,1,'XenServer'),(204,'d1870983-f231-401b-acfd-3e7f2c8b7189','TemplateFromSnapshot-3',1,1,'USER',0,64,NULL,'VHD','2011-04-26 04:25:16',NULL,3,NULL,'TemplateFromSnapshot-3',0,12,1,0,0,1,'XenServer'),(205,'99e56ebb-a548-43e6-8c01-ca22e818c1c4','TemplateFromSnapshot-4',1,1,'USER',0,64,NULL,'VHD','2011-04-26 04:25:30',NULL,3,NULL,'TemplateFromSnapshot-4',0,12,1,0,0,1,'XenServer'),(206,'d5ceac55-2d77-42bb-a2e1-3259b441ad2f','TemplateFromSnapshot-5-Data',1,1,'USER',1,64,NULL,'VHD','2011-04-26 04:26:27',NULL,2,NULL,'TemplateFromSnapshot-5-Data',0,103,1,0,0,0,'XenServer'),(207,'6f686bfb-9cc1-4b8a-90b3-37da41258929','TemplateFromSnapshot-6-Data',1,1,'USER',1,64,NULL,'VHD','2011-04-26 04:26:41',NULL,3,NULL,'TemplateFromSnapshot-6-Data',0,103,1,0,0,0,'XenServer'),(208,'208-2-a63a32c9-bad8-3f24-8e1d-17c85cc838be','CentOS-5-5-ISO-Download',1,1,'USER',1,64,'http://nfs1.lab.vmops.com/isos_64bit/CentOS-5.5-x86_64-bin-DVDs/CentOS-5.5-x86_64-bin-DVD-1of2.iso','ISO','2011-04-26 21:30:45',NULL,2,NULL,'CentOS-5-5-ISO-Download',0,112,1,0,0,1,'None');
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
  CONSTRAINT `fk_volumes__pool_id` FOREIGN KEY (`pool_id`) REFERENCES `storage_pool` (`id`),
  CONSTRAINT `fk_volumes__instance_id` FOREIGN KEY (`instance_id`) REFERENCES `vm_instance` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `volumes`
--

LOCK TABLES `volumes` WRITE;
/*!40000 ALTER TABLE `volumes` DISABLE KEYS */;
INSERT INTO `volumes` VALUES (1,1,1,200,2,0,'ROOT-2',2097152000,'/export/home/chandan/pigeon/primary220','9ddbe615-a799-48b0-97b9-0a9021a56fa9',1,1,NULL,NULL,'ROOT',NULL,'NetworkFilesystem',7,1,NULL,1,'2011-04-26 02:59:03',NULL,NULL,NULL,'Created','Ready',NULL,NULL,NULL),(2,1,1,200,1,0,'ROOT-1',2097152000,'/export/home/chandan/pigeon/primary220','4b28d0f3-f009-4a7b-a2f8-729f13b3e789',1,1,NULL,NULL,'ROOT',NULL,'NetworkFilesystem',6,1,NULL,1,'2011-04-26 02:59:03',NULL,NULL,NULL,'Created','Ready',NULL,NULL,NULL),(3,2,1,200,3,0,'ROOT-3',8589934592,'/export/home/chandan/pigeon/primary220','8d823cf0-e5fd-4973-98f7-46c70958a102',1,1,NULL,NULL,'ROOT',NULL,'NetworkFilesystem',9,2,NULL,1,'2011-04-26 03:16:56',NULL,NULL,NULL,'Created','Ready',NULL,NULL,NULL),(4,2,1,200,3,1,'DATA-3',1073741824,'/export/home/chandan/pigeon/primary220','45047b04-567a-493d-8d11-a8d440148064',1,1,NULL,NULL,'DATADISK',NULL,'NetworkFilesystem',10,NULL,NULL,1,'2011-04-26 03:16:56',NULL,NULL,NULL,'Created','Ready',NULL,NULL,NULL),(5,1,1,200,4,0,'ROOT-4',2097152000,'/export/home/chandan/pigeon/primary220','acfe9e0d-c8ed-4d2d-a423-debceca4d3fe',1,1,NULL,NULL,'ROOT',NULL,'NetworkFilesystem',8,1,NULL,1,'2011-04-26 03:18:01',NULL,NULL,NULL,'Created','Ready',NULL,NULL,NULL),(6,2,1,200,5,0,'ROOT-5',8589934592,'/export/home/chandan/pigeon/primary220','2369348a-3778-4d19-aa37-42debca6ece4',1,1,NULL,NULL,'ROOT',NULL,'NetworkFilesystem',9,2,NULL,1,'2011-04-26 03:35:28',NULL,NULL,NULL,'Created','Ready',NULL,NULL,NULL),(7,3,2,200,6,0,'ROOT-6',8589934592,'/export/home/chandan/pigeon/primary220','6b967305-74e4-47ad-895c-70e7417d26d3',1,1,NULL,NULL,'ROOT',NULL,'NetworkFilesystem',9,2,NULL,1,'2011-04-26 03:37:01',NULL,NULL,NULL,'Created','Ready',NULL,NULL,NULL),(8,3,2,200,6,1,'DATA-6',1073741824,'/export/home/chandan/pigeon/primary220','ec616210-88b5-46cc-bb8f-f29852f66ca6',1,1,NULL,NULL,'DATADISK',NULL,'NetworkFilesystem',10,NULL,NULL,1,'2011-04-26 03:37:01',NULL,NULL,NULL,'Created','Ready',NULL,NULL,NULL),(9,3,2,200,7,0,'ROOT-7',8589934592,'/export/home/chandan/pigeon/primary220','c4e26d61-51a8-4f39-b0d3-f742bf7403c5',1,1,NULL,NULL,'ROOT',NULL,'NetworkFilesystem',9,2,NULL,1,'2011-04-26 03:37:16',NULL,NULL,NULL,'Created','Ready',NULL,NULL,NULL);
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
  CONSTRAINT `fk_vpn_users__owner_id` FOREIGN KEY (`owner_id`) REFERENCES `account` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_vpn_users__domain_id` FOREIGN KEY (`domain_id`) REFERENCES `domain` (`id`) ON DELETE CASCADE
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

-- Dump completed on 2011-04-26 22:21:29
