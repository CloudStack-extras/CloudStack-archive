-- MySQL dump 10.13  Distrib 5.5.17, for osx10.6 (i386)
--
-- Host: localhost    Database: cloud
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
-- Current Database: `cloud`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `cloud` /*!40100 DEFAULT CHARACTER SET latin1 */;

drop database cloud;
drop database cloud_usage;
create database cloud;
create database cloud_usage;
use cloud;
--
-- Table structure for table `account`
--

DROP TABLE IF EXISTS `account`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `account` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `account_name` varchar(100) DEFAULT NULL COMMENT 'an account name set by the creator of the account, defaults to username for single accounts',
  `type` int(1) unsigned NOT NULL,
  `domain_id` bigint(20) unsigned DEFAULT NULL,
  `state` varchar(10) NOT NULL DEFAULT 'enabled',
  `removed` datetime DEFAULT NULL COMMENT 'date removed',
  `cleanup_needed` tinyint(1) NOT NULL DEFAULT '0',
  `network_domain` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `i_account__removed` (`removed`),
  KEY `i_account__domain_id` (`domain_id`),
  KEY `i_account__cleanup_needed` (`cleanup_needed`),
  KEY `i_account__account_name__domain_id__removed` (`account_name`,`domain_id`,`removed`),
  CONSTRAINT `fk_account__domain_id` FOREIGN KEY (`domain_id`) REFERENCES `domain` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `account`
--

LOCK TABLES `account` WRITE;
/*!40000 ALTER TABLE `account` DISABLE KEYS */;
INSERT INTO `account` VALUES (1,'system',1,1,'enabled',NULL,0,NULL),(2,'admin',1,1,'enabled',NULL,0,NULL),(3,'1',2,2,'enabled',NULL,0,NULL);
/*!40000 ALTER TABLE `account` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `account_network_ref`
--

DROP TABLE IF EXISTS `account_network_ref`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `account_network_ref` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
  `account_id` bigint(20) unsigned NOT NULL COMMENT 'account id',
  `network_id` bigint(20) unsigned NOT NULL COMMENT 'network id',
  `is_owner` smallint(1) NOT NULL COMMENT 'is the owner of the network',
  PRIMARY KEY (`id`),
  KEY `fk_account_network_ref__account_id` (`account_id`),
  KEY `fk_account_network_ref__networks_id` (`network_id`),
  CONSTRAINT `fk_account_network_ref__account_id` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_account_network_ref__networks_id` FOREIGN KEY (`network_id`) REFERENCES `networks` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `account_network_ref`
--

LOCK TABLES `account_network_ref` WRITE;
/*!40000 ALTER TABLE `account_network_ref` DISABLE KEYS */;
INSERT INTO `account_network_ref` VALUES (1,1,200,1),(2,1,201,1),(3,1,202,1),(4,1,203,1),(5,2,204,1),(6,1,205,1),(7,1,206,1),(8,2,207,1),(9,1,208,1),(10,3,209,1);
/*!40000 ALTER TABLE `account_network_ref` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `account_vlan_map`
--

DROP TABLE IF EXISTS `account_vlan_map`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `account_vlan_map` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `account_id` bigint(20) unsigned NOT NULL COMMENT 'account id. foreign key to account table',
  `vlan_db_id` bigint(20) unsigned NOT NULL COMMENT 'database id of vlan. foreign key to vlan table',
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`),
  KEY `i_account_vlan_map__account_id` (`account_id`),
  KEY `i_account_vlan_map__vlan_id` (`vlan_db_id`),
  CONSTRAINT `fk_account_vlan_map__account_id` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_account_vlan_map__vlan_id` FOREIGN KEY (`vlan_db_id`) REFERENCES `vlan` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

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
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `alert` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `type` int(1) unsigned NOT NULL,
  `pod_id` bigint(20) unsigned DEFAULT NULL,
  `data_center_id` bigint(20) unsigned NOT NULL,
  `subject` varchar(999) DEFAULT NULL COMMENT 'according to SMTP spec, max subject length is 1000 including the CRLF character, so allow enough space to fit long pod/zone/host names',
  `sent_count` int(3) unsigned NOT NULL,
  `created` datetime DEFAULT NULL COMMENT 'when this alert type was created',
  `last_sent` datetime DEFAULT NULL COMMENT 'Last time the alert was sent',
  `resolved` datetime DEFAULT NULL COMMENT 'when the alert status was resolved (available memory no longer at critical level, etc.)',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `alert`
--

LOCK TABLES `alert` WRITE;
/*!40000 ALTER TABLE `alert` DISABLE KEYS */;
INSERT INTO `alert` VALUES (1,13,0,0,'Management network CIDR is not properly configured and we are not able to find a default setting',1,'2012-02-28 19:29:22','2012-02-28 19:29:22',NULL),(2,13,0,0,'Management server node 127.0.0.1 is up',1,'2012-02-28 19:29:25','2012-02-28 19:29:25',NULL),(3,13,0,0,'Management network CIDR is not properly configured and we are not able to find a default setting',1,'2012-02-28 19:29:42','2012-02-28 19:29:42',NULL),(4,13,0,0,'Management server node 127.0.0.1 is up',1,'2012-02-28 19:29:45','2012-02-28 19:29:45',NULL),(5,2,NULL,1,'System Alert: Low Available Storage in pod POD1 of availablity zone ZONE1',1,'2012-02-28 19:35:12','2012-02-28 19:35:12','2012-02-28 22:03:49'),(6,7,NULL,1,'Failed to deploy Vm with Id: 3',1,'2012-02-28 21:40:33','2012-02-28 21:40:33',NULL),(7,7,NULL,1,'Failed to deploy Vm with Id: 4',1,'2012-02-28 21:40:40','2012-02-28 21:40:40',NULL),(8,7,NULL,1,'Failed to deploy Vm with Id: 5',1,'2012-02-28 21:42:08','2012-02-28 21:42:08',NULL),(9,13,0,0,'Management network CIDR is not properly configured and we are not able to find a default setting',1,'2012-02-28 21:43:18','2012-02-28 21:43:18',NULL),(10,13,0,0,'Management server node 127.0.0.1 is up',1,'2012-02-28 21:43:21','2012-02-28 21:43:21',NULL);
/*!40000 ALTER TABLE `alert` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `async_job`
--

DROP TABLE IF EXISTS `async_job`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `async_job` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) unsigned NOT NULL,
  `account_id` bigint(20) unsigned NOT NULL,
  `session_key` varchar(64) DEFAULT NULL COMMENT 'all async-job manage to apply session based security enforcement',
  `instance_type` varchar(64) DEFAULT NULL COMMENT 'instance_type and instance_id work together to allow attaching an instance object to a job',
  `instance_id` bigint(20) unsigned DEFAULT NULL,
  `job_cmd` varchar(64) NOT NULL COMMENT 'command name',
  `job_cmd_originator` varchar(64) DEFAULT NULL COMMENT 'command originator',
  `job_cmd_info` text COMMENT 'command parameter info',
  `job_cmd_ver` int(1) DEFAULT NULL COMMENT 'command version',
  `callback_type` int(1) DEFAULT NULL COMMENT 'call back type, 0 : polling, 1 : email',
  `callback_address` varchar(128) DEFAULT NULL COMMENT 'call back address by callback_type',
  `job_status` int(1) DEFAULT NULL COMMENT 'general job execution status',
  `job_process_status` int(1) DEFAULT NULL COMMENT 'job specific process status for asynchronize progress update',
  `job_result_code` int(1) DEFAULT NULL COMMENT 'job result code, specify error code corresponding to result message',
  `job_result` text COMMENT 'job result info',
  `job_init_msid` bigint(20) DEFAULT NULL COMMENT 'the initiating msid',
  `job_complete_msid` bigint(20) DEFAULT NULL COMMENT 'completing msid',
  `created` datetime DEFAULT NULL COMMENT 'date created',
  `last_updated` datetime DEFAULT NULL COMMENT 'date created',
  `last_polled` datetime DEFAULT NULL COMMENT 'date polled',
  `removed` datetime DEFAULT NULL COMMENT 'date removed',
  PRIMARY KEY (`id`),
  KEY `i_async_job__removed` (`removed`),
  KEY `i_async__user_id` (`user_id`),
  KEY `i_async__account_id` (`account_id`),
  KEY `i_async__instance_type_id` (`instance_type`,`instance_id`),
  KEY `i_async__job_cmd` (`job_cmd`),
  KEY `i_async__created` (`created`),
  KEY `i_async__last_updated` (`last_updated`),
  KEY `i_async__last_poll` (`last_polled`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `async_job`
--

LOCK TABLES `async_job` WRITE;
/*!40000 ALTER TABLE `async_job` DISABLE KEYS */;
INSERT INTO `async_job` VALUES (1,2,2,NULL,NULL,NULL,'com.cloud.api.commands.DeployVMCmd',NULL,'{\"id\":\"3\",\"response\":\"json\",\"templateId\":\"2\",\"sessionkey\":\"ceL8bA71BkfyMkfHTCE5FkRGkC4\\u003d\",\"ctxUserId\":\"2\",\"networkIds\":\"207\",\"hypervisor\":\"XenServer\",\"serviceOfferingId\":\"7\",\"_\":\"1330465232850\",\"ctxAccountId\":\"2\",\"ctxStartEventId\":\"24\",\"zoneId\":\"1\"}',0,0,NULL,2,0,530,'com.cloud.api.response.ExceptionResponse/null/{\"errorcode\":533,\"errortext\":\"Unable to create a deployment for VM[User|i-2-3-TEST]\"}',1,1,'2012-02-28 21:40:32','2012-02-28 21:40:33',NULL,NULL),(2,2,2,NULL,NULL,NULL,'com.cloud.api.commands.DeployVMCmd',NULL,'{\"id\":\"4\",\"response\":\"json\",\"templateId\":\"2\",\"sessionkey\":\"ceL8bA71BkfyMkfHTCE5FkRGkC4\\u003d\",\"ctxUserId\":\"2\",\"networkIds\":\"207,205,206,204\",\"hypervisor\":\"XenServer\",\"serviceOfferingId\":\"7\",\"_\":\"1330465240007\",\"ctxAccountId\":\"2\",\"ctxStartEventId\":\"28\",\"zoneId\":\"1\"}',0,0,NULL,2,0,530,'com.cloud.api.response.ExceptionResponse/null/{\"errorcode\":533,\"errortext\":\"Unable to create a deployment for VM[User|i-2-4-TEST]\"}',1,1,'2012-02-28 21:40:40','2012-02-28 21:40:40',NULL,NULL),(3,2,2,NULL,NULL,NULL,'com.cloud.api.commands.DeployVMCmd',NULL,'{\"id\":\"5\",\"response\":\"json\",\"templateId\":\"2\",\"sessionkey\":\"ceL8bA71BkfyMkfHTCE5FkRGkC4\\u003d\",\"ctxUserId\":\"2\",\"networkIds\":\"207\",\"hypervisor\":\"XenServer\",\"serviceOfferingId\":\"7\",\"_\":\"1330465328689\",\"ctxAccountId\":\"2\",\"ctxStartEventId\":\"32\",\"zoneId\":\"1\"}',0,0,NULL,2,0,530,'com.cloud.api.response.ExceptionResponse/null/{\"errorcode\":533,\"errortext\":\"Unable to create a deployment for VM[User|i-2-5-TEST]\"}',1,1,'2012-02-28 21:42:08','2012-02-28 21:42:08',NULL,NULL),(4,2,2,NULL,NULL,NULL,'com.cloud.api.commands.DeployVMCmd',NULL,'{\"id\":\"6\",\"response\":\"json\",\"templateId\":\"2\",\"sessionkey\":\"TjW/tSdO0Sx3dRcRgyHdEtwk5hs\\u003d\",\"ctxUserId\":\"2\",\"networkIds\":\"207\",\"hypervisor\":\"XenServer\",\"serviceOfferingId\":\"7\",\"_\":\"1330465451647\",\"ctxAccountId\":\"2\",\"ctxStartEventId\":\"37\",\"zoneId\":\"1\"}',0,0,NULL,1,0,0,'com.cloud.api.response.UserVmResponse/virtualmachine/{\"id\":6,\"name\":\"i-2-6-st\",\"displayname\":\"i-2-6-st\",\"account\":\"admin\",\"domainid\":1,\"domain\":\"ROOT\",\"created\":\"2012-02-28T13:44:11-0800\",\"state\":\"Running\",\"haenable\":false,\"zoneid\":1,\"zonename\":\"ZONE1\",\"hostid\":2,\"hostname\":\"xenserver-QA-2-1-48\",\"templateid\":2,\"templatename\":\"CentOS 5.3(64-bit) no GUI (XenServer)\",\"templatedisplaytext\":\"CentOS 5.3(64-bit) no GUI (XenServer)\",\"passwordenabled\":false,\"serviceofferingid\":7,\"serviceofferingname\":\"Small Instance\",\"cpunumber\":1,\"cpuspeed\":500,\"memory\":512,\"guestosid\":12,\"rootdeviceid\":0,\"rootdevicetype\":\"NetworkFilesystem\",\"securitygroup\":[],\"nic\":[{\"id\":13,\"networkid\":207,\"netmask\":\"255.255.255.0\",\"gateway\":\"10.10.10.1\",\"ipaddress\":\"10.10.10.84\",\"isolationuri\":\"vlan://1024\",\"broadcasturi\":\"vlan://1024\",\"traffictype\":\"Guest\",\"type\":\"Virtual\",\"isdefault\":true,\"macaddress\":\"02:00:76:79:00:04\"}],\"hypervisor\":\"XenServer\"}',1,1,'2012-02-28 21:44:11','2012-02-28 21:47:18','2012-02-28 21:46:02',NULL),(5,2,2,NULL,NULL,NULL,'com.cloud.api.commands.DeployVMCmd',NULL,'{\"sessionkey\":\"TjW/tSdO0Sx3dRcRgyHdEtwk5hs\\u003d\",\"ctxUserId\":\"2\",\"serviceOfferingId\":\"7\",\"zoneId\":\"1\",\"templateId\":\"2\",\"response\":\"json\",\"id\":\"8\",\"networkIds\":\"207,205,206,204\",\"hypervisor\":\"XenServer\",\"_\":\"1330465473780\",\"ctxAccountId\":\"2\",\"ctxStartEventId\":\"41\",\"displayname\":\"multipleNetworksVm\"}',0,0,NULL,1,0,0,'com.cloud.api.response.UserVmResponse/virtualmachine/{\"id\":8,\"name\":\"i-2-8-st\",\"displayname\":\"multipleNetworksVm\",\"account\":\"admin\",\"domainid\":1,\"domain\":\"ROOT\",\"created\":\"2012-02-28T13:44:33-0800\",\"state\":\"Running\",\"haenable\":false,\"zoneid\":1,\"zonename\":\"ZONE1\",\"hostid\":2,\"hostname\":\"xenserver-QA-2-1-48\",\"templateid\":2,\"templatename\":\"CentOS 5.3(64-bit) no GUI (XenServer)\",\"templatedisplaytext\":\"CentOS 5.3(64-bit) no GUI (XenServer)\",\"passwordenabled\":false,\"serviceofferingid\":7,\"serviceofferingname\":\"Small Instance\",\"cpunumber\":1,\"cpuspeed\":500,\"memory\":512,\"guestosid\":12,\"rootdeviceid\":0,\"rootdevicetype\":\"NetworkFilesystem\",\"securitygroup\":[],\"nic\":[{\"id\":20,\"networkid\":204,\"netmask\":\"255.255.0.0\",\"gateway\":\"172.20.0.1\",\"ipaddress\":\"172.20.0.100\",\"isolationuri\":\"vlan://20\",\"broadcasturi\":\"vlan://20\",\"traffictype\":\"Guest\",\"type\":\"Direct\",\"isdefault\":false,\"macaddress\":\"06:30:16:00:00:cb\"},{\"id\":19,\"networkid\":206,\"netmask\":\"255.255.0.0\",\"gateway\":\"172.22.0.1\",\"ipaddress\":\"172.22.0.100\",\"isolationuri\":\"vlan://22\",\"broadcasturi\":\"vlan://22\",\"traffictype\":\"Guest\",\"type\":\"Direct\",\"isdefault\":false,\"macaddress\":\"06:41:a2:00:00:f5\"},{\"id\":18,\"networkid\":205,\"netmask\":\"255.255.0.0\",\"gateway\":\"172.21.0.1\",\"ipaddress\":\"172.21.0.100\",\"isolationuri\":\"vlan://21\",\"broadcasturi\":\"vlan://21\",\"traffictype\":\"Guest\",\"type\":\"Direct\",\"isdefault\":false,\"macaddress\":\"06:da:96:00:00:e0\"},{\"id\":17,\"networkid\":207,\"netmask\":\"255.255.255.0\",\"gateway\":\"10.10.10.1\",\"ipaddress\":\"10.10.10.192\",\"isolationuri\":\"vlan://1024\",\"broadcasturi\":\"vlan://1024\",\"traffictype\":\"Guest\",\"type\":\"Virtual\",\"isdefault\":true,\"macaddress\":\"02:00:4e:da:00:06\"}],\"hypervisor\":\"XenServer\"}',1,1,'2012-02-28 21:44:33','2012-02-28 21:51:33','2012-02-28 21:46:03',NULL),(6,3,3,NULL,NULL,NULL,'com.cloud.api.commands.DeployVMCmd',NULL,'{\"sessionkey\":\"wSQNto2SEKA9+uFa/q+L19rOf5Q\\u003d\",\"ctxUserId\":\"3\",\"serviceOfferingId\":\"1\",\"zoneId\":\"1\",\"templateId\":\"2\",\"response\":\"json\",\"id\":\"10\",\"networkIds\":\"209,208,205,206\",\"hypervisor\":\"XenServer\",\"_\":\"1330465646420\",\"ctxAccountId\":\"3\",\"ctxStartEventId\":\"51\",\"displayname\":\"1svm\"}',0,0,NULL,1,0,0,'com.cloud.api.response.UserVmResponse/virtualmachine/{\"id\":10,\"name\":\"i-3-10-st\",\"displayname\":\"1svm\",\"account\":\"1\",\"domainid\":2,\"domain\":\"1\",\"created\":\"2012-02-28T13:47:26-0800\",\"state\":\"Running\",\"haenable\":false,\"zoneid\":1,\"zonename\":\"ZONE1\",\"templateid\":2,\"templatename\":\"CentOS 5.3(64-bit) no GUI (XenServer)\",\"templatedisplaytext\":\"CentOS 5.3(64-bit) no GUI (XenServer)\",\"passwordenabled\":false,\"serviceofferingid\":1,\"serviceofferingname\":\"Small Instance\",\"cpunumber\":1,\"cpuspeed\":500,\"memory\":512,\"guestosid\":12,\"rootdeviceid\":0,\"rootdevicetype\":\"NetworkFilesystem\",\"securitygroup\":[],\"nic\":[{\"id\":24,\"networkid\":208,\"netmask\":\"255.255.0.0\",\"gateway\":\"172.23.0.1\",\"ipaddress\":\"172.23.0.100\",\"traffictype\":\"Guest\",\"type\":\"Direct\",\"isdefault\":false,\"macaddress\":\"06:2f:4e:00:01:0a\"},{\"id\":23,\"networkid\":209,\"netmask\":\"255.255.255.0\",\"gateway\":\"10.10.10.1\",\"ipaddress\":\"10.10.10.21\",\"traffictype\":\"Guest\",\"type\":\"Virtual\",\"isdefault\":true,\"macaddress\":\"02:00:3e:ed:00:01\"},{\"id\":26,\"networkid\":206,\"netmask\":\"255.255.0.0\",\"gateway\":\"172.22.0.1\",\"ipaddress\":\"172.22.0.101\",\"traffictype\":\"Guest\",\"type\":\"Direct\",\"isdefault\":false,\"macaddress\":\"06:13:f4:00:00:f6\"},{\"id\":25,\"networkid\":205,\"netmask\":\"255.255.0.0\",\"gateway\":\"172.21.0.1\",\"ipaddress\":\"172.21.0.102\",\"traffictype\":\"Guest\",\"type\":\"Direct\",\"isdefault\":false,\"macaddress\":\"06:80:8e:00:00:e2\"}],\"hypervisor\":\"XenServer\"}',1,1,'2012-02-28 21:47:26','2012-02-28 21:51:37','2012-02-28 21:51:36',NULL);
/*!40000 ALTER TABLE `async_job` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cluster`
--

DROP TABLE IF EXISTS `cluster`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cluster` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name` varchar(255) DEFAULT NULL COMMENT 'name for the cluster',
  `guid` varchar(255) DEFAULT NULL COMMENT 'guid for the cluster',
  `pod_id` bigint(20) unsigned NOT NULL COMMENT 'pod id',
  `data_center_id` bigint(20) unsigned NOT NULL COMMENT 'data center id',
  `hypervisor_type` varchar(32) DEFAULT NULL,
  `cluster_type` varchar(64) DEFAULT 'CloudManaged',
  `allocation_state` varchar(32) NOT NULL DEFAULT 'Enabled' COMMENT 'Is this cluster enabled for allocation for new resources',
  `managed_state` varchar(32) NOT NULL DEFAULT 'Managed' COMMENT 'Is this cluster managed by cloudstack',
  `removed` datetime DEFAULT NULL COMMENT 'date removed if not null',
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`),
  UNIQUE KEY `guid` (`guid`),
  UNIQUE KEY `i_cluster__pod_id__name` (`pod_id`,`name`),
  KEY `fk_cluster__data_center_id` (`data_center_id`),
  KEY `i_cluster__allocation_state` (`allocation_state`),
  KEY `i_cluster__removed` (`removed`),
  CONSTRAINT `fk_cluster__data_center_id` FOREIGN KEY (`data_center_id`) REFERENCES `data_center` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_cluster__pod_id` FOREIGN KEY (`pod_id`) REFERENCES `host_pod_ref` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cluster`
--

LOCK TABLES `cluster` WRITE;
/*!40000 ALTER TABLE `cluster` DISABLE KEYS */;
INSERT INTO `cluster` VALUES (1,'alenazone1','e71b1f61-b4cc-60d0-b609-b77d21e20a24',1,1,'xenserver','CloudManaged','Enabled','Managed',NULL);
/*!40000 ALTER TABLE `cluster` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cluster_details`
--

DROP TABLE IF EXISTS `cluster_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cluster_details` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `cluster_id` bigint(20) unsigned NOT NULL COMMENT 'cluster id',
  `name` varchar(255) NOT NULL,
  `value` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_cluster_details__cluster_id` (`cluster_id`),
  CONSTRAINT `fk_cluster_details__cluster_id` FOREIGN KEY (`cluster_id`) REFERENCES `cluster` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cluster_details`
--

LOCK TABLES `cluster_details` WRITE;
/*!40000 ALTER TABLE `cluster_details` DISABLE KEYS */;
/*!40000 ALTER TABLE `cluster_details` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cmd_exec_log`
--

DROP TABLE IF EXISTS `cmd_exec_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cmd_exec_log` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
  `host_id` bigint(20) unsigned NOT NULL COMMENT 'host id of the system VM agent that command is sent to',
  `instance_id` bigint(20) unsigned NOT NULL COMMENT 'instance id of the system VM that command is executed on',
  `command_name` varchar(255) NOT NULL COMMENT 'command name',
  `weight` int(11) NOT NULL DEFAULT '1' COMMENT 'command weight in consideration of the load factor added to host that is executing the command',
  `created` datetime NOT NULL COMMENT 'date created',
  PRIMARY KEY (`id`),
  KEY `i_cmd_exec_log__host_id` (`host_id`),
  KEY `i_cmd_exec_log__instance_id` (`instance_id`),
  CONSTRAINT `fk_cmd_exec_log_ref__inst_id` FOREIGN KEY (`instance_id`) REFERENCES `vm_instance` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cmd_exec_log`
--

LOCK TABLES `cmd_exec_log` WRITE;
/*!40000 ALTER TABLE `cmd_exec_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `cmd_exec_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `configuration`
--

DROP TABLE IF EXISTS `configuration`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `configuration` (
  `category` varchar(255) NOT NULL DEFAULT 'Advanced',
  `instance` varchar(255) NOT NULL,
  `component` varchar(255) NOT NULL DEFAULT 'management-server',
  `name` varchar(255) NOT NULL,
  `value` varchar(4095) DEFAULT NULL,
  `description` varchar(1024) DEFAULT NULL,
  PRIMARY KEY (`name`),
  KEY `i_configuration__instance` (`instance`),
  KEY `i_configuration__name` (`name`),
  KEY `i_configuration__category` (`category`),
  KEY `i_configuration__component` (`component`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `configuration`
--

LOCK TABLES `configuration` WRITE;
/*!40000 ALTER TABLE `configuration` DISABLE KEYS */;
INSERT INTO `configuration` VALUES ('Advanced','DEFAULT','management-server','account.cleanup.interval','86400','null'),('Advanced','DEFAULT','none','agent.lb.enabled','false','If agent load balancing enabled in cluster setup'),('Advanced','DEFAULT','management-server','agent.load.threshold','0.7','Percentage (as a value between 0 and 1) of connected agents after which agent load balancing will start happening'),('Alert','DEFAULT','management-server','alert.email.addresses',NULL,'Comma separated list of email addresses used for sending alerts.'),('Alert','DEFAULT','management-server','alert.email.sender',NULL,'Sender of alert email (will be in the From header of the email).'),('Alert','DEFAULT','management-server','alert.smtp.host',NULL,'SMTP hostname used for sending out email alerts.'),('Alert','DEFAULT','management-server','alert.smtp.password',NULL,'Password for SMTP authentication (applies only if alert.smtp.useAuth is true).'),('Alert','DEFAULT','management-server','alert.smtp.port','465','Port the SMTP server is listening on.'),('Alert','DEFAULT','management-server','alert.smtp.useAuth',NULL,'If true, use SMTP authentication when sending emails.'),('Alert','DEFAULT','management-server','alert.smtp.username',NULL,'Username for SMTP authentication (applies only if alert.smtp.useAuth is true).'),('Advanced','DEFAULT','AgentManager','alert.wait','1800','null'),('Advanced','DEFAULT','management-server','allow.public.user.templates','true','If false, users will not be able to create public templates.'),('Advanced','DEFAULT','NetworkManager','allow.subdomain.network.access','true','Allow subdomains to use networks dedicated to their parent domain(s)'),('Storage','DEFAULT','none','backup.snapshot.wait','10800','In second, timeout for BackupSnapshotCommand'),('Advanced','DEFAULT','management-server','capacity.check.period','300000','null'),('Advanced','DEFAULT','management-server','capacity.skipcounting.hours','3600','Time (in seconds) to wait before release VM\'s cpu and memory when VM in stopped state'),('Advanced','DEFAULT','management-server','check.pod.cidrs','true','If true, different pods must belong to different CIDR subnets.'),('Hidden','DEFAULT','management-server','cloud.identifier','5cdc298c-61ac-4683-b0fd-81b55c479de6','A unique identifier for the cloud.'),('Advanced','DEFAULT','management-server','cluster.message.timeout.seconds','300','Time (in seconds) to wait before a inter-management server message post times out.'),('Console Proxy','DEFAULT','AgentManager','consoleproxy.capacity.standby','10','The minimal number of console proxy viewer sessions that system is able to serve immediately(standby capacity)'),('Console Proxy','DEFAULT','AgentManager','consoleproxy.capacityscan.interval','30000','The time interval(in millisecond) to scan whether or not system needs more console proxy to ensure minimal standby capacity'),('Console Proxy','DEFAULT','AgentManager','consoleproxy.cmd.port','8001','Console proxy command port that is used to communicate with management server'),('Console Proxy','DEFAULT','AgentManager','consoleproxy.cpu.mhz','500','CPU speed (in MHz) used to create new console proxy VMs'),('Console Proxy','DEFAULT','AgentManager','consoleproxy.disable.rpfilter','true','disable rp_filter on console proxy VM public interface'),('Console Proxy','DEFAULT','AgentManager','consoleproxy.launch.max','10','maximum number of console proxy instances per zone can be launched'),('Console Proxy','DEFAULT','AgentManager','consoleproxy.loadscan.interval','10000','The time interval(in milliseconds) to scan console proxy working-load info'),('Console Proxy','DEFAULT','AgentManager','consoleproxy.management.state','Auto','console proxy service management state'),('Console Proxy','DEFAULT','AgentManager','consoleproxy.management.state.last','Auto','last console proxy service management state'),('Console Proxy','DEFAULT','AgentManager','consoleproxy.ram.size','1024','RAM size (in MB) used to create new console proxy VMs'),('Console Proxy','DEFAULT','AgentManager','consoleproxy.restart','true','Console proxy restart flag, defaulted to true'),('Console Proxy','DEFAULT','AgentManager','consoleproxy.session.max','50','The max number of viewer sessions console proxy is configured to serve for'),('Console Proxy','DEFAULT','AgentManager','consoleproxy.session.timeout','300000','Timeout(in milliseconds) that console proxy tries to maintain a viewer session before it times out the session for no activity'),('Console Proxy','DEFAULT','AgentManager','consoleproxy.url.domain','realhostip.com','Console proxy url domain'),('Advanced','DEFAULT','management-server','control.cidr','169.254.0.0/16','Changes the cidr for the control network traffic.  Defaults to using link local.  Must be unique within pods'),('Advanced','DEFAULT','management-server','control.gateway','169.254.0.1','gateway for the control network traffic'),('Storage','DEFAULT','none','copy.volume.wait','10800','In second, timeout for copy volume command'),('Advanced','DEFAULT','management-server','cpu.capacity.threshold','0.85','percentage (as a value between 0 and 1) of cpu utilization above which alerts will be sent about low cpu available'),('Advanced','DEFAULT','management-server','cpu.overprovisioning.factor','1','Used for CPU overprovisioning calculation; available CPU will be (actualCpuCapacity * cpu.overprovisioning.factor)'),('Storage','DEFAULT','UserVmManager','create.private.template.from.snapshot.wait','10800','In second, timeout for CreatePrivateTemplateFromSnapshotCommand'),('Storage','DEFAULT','UserVmManager','create.private.template.from.volume.wait','10800','In second, timeout for CreatePrivateTemplateFromVolumeCommand'),('Storage','DEFAULT','none','create.volume.from.snapshot.wait','10800','In second, timeout for create template from snapshot'),('Advanced','DEFAULT','management-server','default.page.size','500','Default page size for API list* commands'),('Advanced','DEFAULT','null','default.zone','ZONE1','null'),('Advanced','DEFAULT','management-server','direct.agent.load.size','16','The number of direct agents to load each time'),('Advanced','DEFAULT','management-server','direct.attach.network.externalIpAllocator.enabled','false','Direct-attach VMs using external DHCP server'),('Advanced','DEFAULT','management-server','direct.attach.network.externalIpAllocator.url',NULL,'Direct-attach VMs using external DHCP server (API url)'),('Network','DEFAULT','management-server','direct.network.no.default.route','false','Direct Network Dhcp Server should not send a default route'),('Premium','DEFAULT','management-server','direct.network.stats.interval','86400','Interval (in seconds) to collect stats from Traffic Monitor'),('Premium','DEFAULT','management-server','enable.usage.server','true','Flag for enabling usage'),('Advanced','DEFAULT','management-server','encode.api.response','false','Do UTF-8 encoding for the api response, false by default'),('Advanced','DEFAULT','management-server','endpointe.url','http://localhost:8080/client/api','Endpointe Url'),('Advanced','DEFAULT','management-server','event.purge.delay','15','Events older than specified number days will be purged. Set this value to 0 to never delete events'),('Advanced','DEFAULT','UserVmManager','expunge.delay','60','Determines how long (in seconds) to wait before actually expunging destroyed vm. The default value = the default value of expunge.interval'),('Advanced','DEFAULT','UserVmManager','expunge.interval','60','the interval to wait before running the expunge thread'),('Advanced','DEFAULT','UserVmManager','expunge.workers','1','null'),('Advanced','DEFAULT','NetworkManager','external.network.stats.interval','300','Interval (in seconds) to report external network statistics.'),('Advanced','DEFAULT','management-server','extract.url.cleanup.interval','120','null'),('Advanced','DEFAULT','management-server','extract.url.expiration.interval','14400','The life of an extract URL after which it is deleted '),('Network','DEFAULT','management-server','firewall.rule.ui.enabled','false','enable/disable UI that separates firewall rules from NAT/LB rules'),('Advanced','DEFAULT','AgentManager','guest.domain.suffix','qatest-vmops.com','domain suffix for users'),('Network','DEFAULT','management-server','guest.vlan.bits','12','The number of bits to reserve for the VLAN identifier in the guest subnet.'),('Advanced','DEFAULT','management-server','host','10.216.133.62','The ip address of management server'),('Advanced','DEFAULT','management-server','host.capacity.checker.interval','3600','Time (in seconds) to wait before recalculating host\'s capacity'),('Advanced','DEFAULT','management-server','host.capacity.checker.wait','3600','Time (in seconds) to wait before starting host capacity background checker'),('Advanced','DEFAULT','management-server','host.capacityType.to.order.clusters','CPU','The host capacity type (CPU or RAM) is used by deployment planner to order clusters during VM resource allocation'),('Advanced','DEFAULT','AgentManager','host.retry','2','Number of times to retry hosts for creating a volume'),('Advanced','DEFAULT','management-server','host.stats.interval','60000','the interval in milliseconds when host stats are retrieved from agents'),('Advanced','DEFAULT','management-server','hypervisor.list','KVM,XenServer,VMware,BareMetal,Ovm','The list of hypervisors that this deployment will use.'),('Advanced','DEFAULT','none','init','true','null'),('Advanced','DEFAULT','AgentManager','instance.name','st','Name of the deployment instance'),('Advanced','DEFAULT','management-server','integration.api.port','8096','internal port used by the management server for servicing Integration API requests'),('Advanced','DEFAULT','HighAvailabilityManager','investigate.retry.interval','60','null'),('Advanced','DEFAULT','management-server','job.cancel.threshold.minutes','60','Time (in minutes) for async-jobs to be forcely cancelled if it has been in process for long'),('Advanced','DEFAULT','management-server','job.expire.minutes','1440','Time (in minutes) for async-jobs to be kept in system'),('Advanced','DEFAULT','management-server','json.content.type','text/javascript','Http response content type for .js files (default is text/javascript)'),('Advanced','DEFAULT','management-server','kvm.guest.network.device',NULL,'Specify the private bridge on host for private network'),('Advanced','DEFAULT','management-server','kvm.private.network.device',NULL,'Specify the private bridge on host for private network'),('Advanced','DEFAULT','management-server','kvm.public.network.device',NULL,'Specify the public bridge on host for public network'),('Advanced','DEFAULT','management-server','linkLocalIp.nums','10','The number of link local ip that needed by domR(in power of 2)'),('Advanced','DEFAULT','management-server','management.network.cidr',NULL,'The cidr of management server network'),('Account Defaults','DEFAULT','management-server','max.account.public.ips','20','The default maximum number of public IPs that can be consumed by an account'),('Account Defaults','DEFAULT','management-server','max.account.snapshots','20','The default maximum number of snapshots that can be created for an account'),('Account Defaults','DEFAULT','management-server','max.account.templates','20','The default maximum number of templates that can be deployed for an account'),('Account Defaults','DEFAULT','management-server','max.account.user.vms','20','The default maximum number of user VMs that can be deployed for an account'),('Account Defaults','DEFAULT','management-server','max.account.volumes','20','The default maximum number of volumes that can be created for an account'),('Advanced','DEFAULT','management-server','max.template.iso.size','50','The maximum size for a downloaded template or ISO (in GB).'),('Advanced','DEFAULT','management-server','mem.overprovisioning.factor','1','Used for memory overprovisioning calculation'),('Advanced','DEFAULT','management-server','memory.capacity.threshold','0.85','percentage (as a value between 0 and 1) of memory utilization above which alerts will be sent about low memory available'),('Advanced','DEFAULT','HighAvailabilityManager','migrate.retry.interval','120','null'),('Advanced','DEFAULT','AgentManager','migratewait','3600','Time (in seconds) to wait for VM migrate finish'),('Advanced','DEFAULT','management-server','mount.parent','/mnt','The mount point on the Management Server for Secondary Storage.'),('Advanced','DEFAULT','management-server','multicast.throttling.rate','10','default multicast rate in megabits per second allowed'),('Network','DEFAULT','management-server','network.disable.rpfilter','true','disable rp_filter on Domain Router VM public interfaces.'),('Advanced','DEFAULT','NetworkManager','network.dns.basiczone.updates','all','This parameter can take 2 values: all (default) and pod. It defines if DHCP/DNS requests have to be send to all dhcp servers in cloudstack, or only to the one in the same pod'),('Advanced','DEFAULT','management-server','network.gc.interval','600','Seconds to wait before checking for networks to shutdown'),('Advanced','DEFAULT','management-server','network.gc.wait','600','Time (in seconds) to wait before shutting down a network that\'s not in used'),('Network','DEFAULT','NetworkManager','network.guest.cidr.limit','22','size limit for guest cidr; can\'t be less than this value'),('Advanced','DEFAULT','management-server','network.loadbalancer.basiczone.elb.enabled','false','Whether the load balancing service is enabled for basic zones'),('Advanced','DEFAULT','management-server','network.loadbalancer.basiczone.elb.gc.interval.minutes','30','Garbage collection interval to destroy unused ELB vms in minutes. Minimum of 5'),('Advanced','DEFAULT','management-server','network.loadbalancer.basiczone.elb.network','guest','Whether the elastic load balancing service public ips are taken from the public or guest network'),('Advanced','DEFAULT','management-server','network.loadbalancer.basiczone.elb.vm.cpu.mhz','128','CPU speed for the elastic load balancer vm'),('Advanced','DEFAULT','management-server','network.loadbalancer.basiczone.elb.vm.ram.size','128','Memory in MB for the elastic load balancer vm'),('Advanced','DEFAULT','management-server','network.loadbalancer.basiczone.elb.vm.vcpu.num','1','Number of VCPU  for the elastic load balancer vm'),('Network','DEFAULT','management-server','network.loadbalancer.haproxy.stats.auth','admin1:AdMiN123','Load Balancer(haproxy) authetication string in the format username:password'),('Network','DEFAULT','management-server','network.loadbalancer.haproxy.stats.port','8081','Load Balancer(haproxy) stats port number.'),('Network','DEFAULT','management-server','network.loadbalancer.haproxy.stats.uri','/admin?stats','Load Balancer(haproxy) uri.'),('Network','DEFAULT','management-server','network.loadbalancer.haproxy.stats.visibility','global','Load Balancer(haproxy) stats visibilty, the value can be one of the following six parameters : global,guest-network,link-local,disabled,all,default'),('Network','DEFAULT','management-server','network.lock.timeout','600','Lock wait timeout (seconds) while implementing network'),('Network','DEFAULT','management-server','network.redundantrouter','false','enable/disable redundant virtual router'),('Network','DEFAULT','management-server','network.securitygroups.work.cleanup.interval','120','Time interval (seconds) in which finished work is cleaned up from the work table'),('Network','DEFAULT','management-server','network.securitygroups.work.lock.timeout','300','Lock wait timeout (seconds) while updating the security group work queue'),('Network','DEFAULT','management-server','network.securitygroups.work.per.agent.queue.size','100','The number of outstanding security group work items that can be queued to a host. If exceeded, work items will get dropped to conserve memory. Security Group Sync will take care of ensuring that the host gets updated eventually'),('Network','DEFAULT','management-server','network.securitygroups.workers.pool.size','50','Number of worker threads processing the security group update work queue'),('Advanced','DEFAULT','management-server','network.throttling.rate','200','default data transfer rate in megabits per second allowed per user'),('Network','DEFAULT','management-server','open.vswitch.tunnel.network','false','enable/disable open vswitch tunnel network(no vlan)'),('Network','DEFAULT','management-server','open.vswitch.vlan.network','false','enable/disable vlan remapping of  open vswitch network'),('Advanced','DEFAULT','management-server','ovm.guest.network.device',NULL,'Specify the private bridge on host for private network'),('Advanced','DEFAULT','management-server','ovm.private.network.device',NULL,'Specify the private bridge on host for private network'),('Advanced','DEFAULT','management-server','ovm.public.network.device',NULL,'Specify the public bridge on host for public network'),('Advanced','DEFAULT','AgentManager','ping.interval','60','null'),('Advanced','DEFAULT','AgentManager','ping.timeout','2.5','null'),('Advanced','DEFAULT','AgentManager','port','8250','port to listen on for agent connection'),('Storage','DEFAULT','none','primary.storage.download.wait','10800','In second, timeout for download template to primary storage'),('Advanced','DEFAULT','management-server','private.ip.capacity.threshold','0.85','percentage (as a value between 0 and 1) of private IP address space utilization above which alerts will be sent'),('Advanced','DEFAULT','management-server','public.ip.capacity.threshold','0.85','percentage (as a value between 0 and 1) of public IP address space utilization above which alerts will be sent'),('Network','DEFAULT','AgentManager','remote.access.vpn.client.iprange','10.1.2.1-10.1.2.8','The range of ips to be allocated to remote access vpn clients. The first ip in the range is used by the VPN server'),('Network','DEFAULT','AgentManager','remote.access.vpn.psk.length','24','The length of the ipsec preshared key (minimum 8, maximum 256)'),('Network','DEFAULT','AgentManager','remote.access.vpn.user.limit','8','The maximum number of VPN users that can be created per account'),('Advanced','DEFAULT','HighAvailabilityManager','restart.retry.interval','600','null'),('Advanced','DEFAULT','AgentManager','retries.per.host','2','The number of times each command sent to a host should be retried in case of failure.'),('Advanced','DEFAULT','NetworkManager','router.cpu.mhz','500','Default CPU speed (MHz) for router VM.'),('Advanced','DEFAULT','NetworkManager','router.extra.public.nics','2','specify extra public nics used for virtual router(up to 5)'),('Hidden','DEFAULT','NetworkManager','router.ram.size','128','Default RAM for router VM (in MB).'),('Advanced','DEFAULT','NetworkManager','router.stats.interval','300','Interval (in seconds) to report router statistics.'),('Advanced','DEFAULT','NetworkManager','router.template.id','1','Default ID for template.'),('Advanced','DEFAULT','null','secondary.storage.vm','true','null'),('Advanced','DEFAULT','management-server','secstorage.allowed.internal.sites',NULL,'Comma separated list of cidrs internal to the datacenter that can host template download servers'),('Advanced','DEFAULT','AgentManager','secstorage.capacity.standby','10','The minimal number of command execution sessions that system is able to serve immediately(standby capacity)'),('Advanced','DEFAULT','AgentManager','secstorage.cmd.execution.time.max','30','The max command execution time in minute'),('Hidden','DEFAULT','management-server','secstorage.copy.password','nB2mkjzpiihmdgf','Password used to authenticate zone-to-zone template copy requests'),('Advanced','DEFAULT','null','secstorage.encrypt.copy','true','null'),('Advanced','DEFAULT','AgentManager','secstorage.session.max','50','The max number of command execution sessions that a SSVM can handle'),('Advanced','DEFAULT','management-server','secstorage.ssl.cert.domain','realhostip.com','SSL certificate used to encrypt copy traffic between zones'),('Advanced','DEFAULT','AgentManager','secstorage.vm.cpu.mhz','500','CPU speed (in MHz) used to create new secondary storage vms'),('Advanced','DEFAULT','AgentManager','secstorage.vm.ram.size','256','RAM size (in MB) used to create new secondary storage vms'),('Hidden','DEFAULT','management-server','security.hash.key',NULL,'for generic key-ed hash'),('Hidden','DEFAULT','management-server','security.singlesignon.key','NuCyiAJ-RAVvGIYtAkjYMcdy_BsWUcdXH17qZlL8q1KskXk10erfOMCGKVy36A_4jLt5h2VQMJ8wHUgo9BArVw','A Single Sign-On key used for logging into the cloud'),('Advanced','DEFAULT','management-server','security.singlesignon.tolerance.millis','300000','The allowable clock difference in milliseconds between when an SSO login request is made and when it is received.'),('Advanced','DEFAULT','AgentManager','skip.steps','60','Cluster full sync skip steps count'),('Advanced','DEFAULT','SnapshotManager','snapshot.delta.max','16','max delta snapshots between two full snapshots.'),('Advanced','DEFAULT','SnapshotManager','snapshot.max.daily','8','Maximum daily snapshots for a volume'),('Advanced','DEFAULT','SnapshotManager','snapshot.max.hourly','8','Maximum hourly snapshots for a volume'),('Advanced','DEFAULT','SnapshotManager','snapshot.max.monthly','8','Maximum monthly snapshots for a volume'),('Advanced','DEFAULT','SnapshotManager','snapshot.max.weekly','8','Maximum weekly snapshots for a volume'),('Advanced','DEFAULT','SnapshotManager','snapshot.poll.interval','300','The time interval in seconds when the management server polls for snapshots to be scheduled.'),('Advanced','DEFAULT','SnapshotManager','snapshot.recurring.test','false','Flag for testing recurring snapshots'),('Advanced','DEFAULT','SnapshotManager','snapshot.test.days.per.month','30','Set it to a smaller value to take more recurring snapshots'),('Advanced','DEFAULT','SnapshotManager','snapshot.test.days.per.week','7','Set it to a smaller value to take more recurring snapshots'),('Advanced','DEFAULT','SnapshotManager','snapshot.test.hours.per.day','24','Set it to a smaller value to take more recurring snapshots'),('Advanced','DEFAULT','SnapshotManager','snapshot.test.minutes.per.hour','60','Set it to a smaller value to take more recurring snapshots'),('Advanced','DEFAULT','SnapshotManager','snapshot.test.months.per.year','12','Set it to a smaller value to take more recurring snapshots'),('Advanced','DEFAULT','SnapshotManager','snapshot.test.weeks.per.month','4','Set it to a smaller value to take more recurring snapshots'),('Hidden','DEFAULT','management-server','ssl.keystore','/u3+7QAAAAIAAAABAAAAAQAFbXlrZXkAAAEzgMQGLgAAArswggK3MA4GCisGAQQBKgIRAQEFAASCAqNP9lssE21+XUN5qq3WCahAM3OGC2+jciaqiR/V2o5k9kb4SixqdYeJ7Rv6xf9XsuepzWeqjtA6YkXDbBkJAl8X4EKJQiqMVvW3Istz0gHJqQYFTHjHKfkWUu7+Ef/ji2KBGonDNe7LVLSezlvqHor+nWwVljLpTMZI8o639ye0yulYrreDp0WziM8k3ciDTz3AGu2QoLY05pjKRYLMRTgp231ezAGw1TmvC+4gDaU03ArAxXk/eiC3Rdi9SpaE4RXfzDxn98PQt7YsLV1D/j03UdxmcYsf43mwIiaF4AztrHGOccIdwNq1WlQfC9NaZCWY3Eqk8O6257ks0lF2zjoLnCRfgLju+RxBvFE+z3nk0HHWfoJcIM3iptqfOUrbpiJ11u2Sy1POHe+8sKcLolnMn1SQuZ9RmXH6ryBcDCff2uG4RXtRw9F+LwsF6FQIhjeCFJRsW7eTmJXRTxY25SxqKA2xbx7lO4HRPbhNL7h5Z5tYrUZ8Nvt5mT4zJKWKAQ5HW0LVrmx4Xb84kHG/8U7QxEIwdxwUXW8h03B9HA+AOWkP7oi4t1V9hAF/LW9hNb3d27zztZK+duTxDAAOEUJmxoZjjflEYTn/aytq4W95lvXC6WK+NWfRN7vfN9nuU/A2aFqo4UIG2ARGCgKHsd4XWhqDpZH3mX/T6HndHEr4kmLZZD6YzYYfCIWTGFzis9cTuY3qQfR65z3tv82OxfAbhvQyMRDHV7mWWPEP76iwUMEXgyOYC10DcPe/ERp+yDLBxCbPywkE1G/pVpSDn61HZ17GT2V/3qyBL9PiTmuGkfELrtAaSMNCoKN0RaTYB0l4fdm/p9aKkkNKNPENSc9YOdYXu5sjHCicY66txYYiG2qp1UYlIVDgBirjSI+T2yjVUpIAAAABAAVYLjUwOQAAAlcwggJTMIIBvKADAgECAgROuIQaMA0GCSqGSIb3DQEBBQUAMG4xEDAOBgNVBAYTB1Vua25vd24xHzAdBgNVBAoTFm1hY2Jvb2twcm8uY2l0cml0ZS5uZXQxHzAdBgNVBAsTFm1hY2Jvb2twcm8uY2l0cml0ZS5uZXQxGDAWBgNVBAMTD0Nsb3Vkc3RhY2sgVXNlcjAeFw0xMTExMDgwMTIxMzBaFw0yMTExMDUwMTIxMzBaMG4xEDAOBgNVBAYTB1Vua25vd24xHzAdBgNVBAoTFm1hY2Jvb2twcm8uY2l0cml0ZS5uZXQxHzAdBgNVBAsTFm1hY2Jvb2twcm8uY2l0cml0ZS5uZXQxGDAWBgNVBAMTD0Nsb3Vkc3RhY2sgVXNlcjCBnzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEAiompkdiWEn6hUyEQmOqS+AW028TO9Zs2CCt8cvYnvKURhcPL33gLQNRKv+f4ymmt5VO12WUFCfx68bDpPqlvriTRWmHRuDIKBayVi8k/+eCY/cja4BP8UegwWPINvxIhik0iYcCCm/UGIQUOCz+wmJ45VAZVa7NJ2OMRsPaz7HUCAwEAATANBgkqhkiG9w0BAQUFAAOBgQA+sdHIcc3eVJpvAm9/1yQSFlLtcnbePRQQJ8ZQxnFKdxoIZ35WnlSQAgR58CE1n6+TZKMXLxwsfHrcSZPE/bhzOuoZHChGG0BIudFPX8ECeIOAqY5eIugXNtuzBM078E1tluXZ8bABxV4szl8bRRg7i20vJjEn9ilSPly+/O1yDTjEu9KkxdJURHnXk6j9xlpfKSsJ','SSL Keystore for the management servers'),('Advanced','DEFAULT','AgentManager','start.retry','6','Number of times to retry create and start commands'),('Advanced','DEFAULT','HighAvailabilityManager','stop.retry.interval','600','null'),('Advanced','DEFAULT','null','storage.allocated.capacity.threshold','0.85','null'),('Advanced','DEFAULT','management-server','storage.capacity.threshold','0.99','percentage (as a value between 0 and 1) of storage utilization above which alerts will be sent about low storage available'),('Advanced','DEFAULT','none','storage.cleanup.enabled','true','Enables/disables the storage cleanup thread.'),('Advanced','DEFAULT','none','storage.cleanup.interval','60','The interval (in seconds) to wait before running the storage cleanup thread.'),('Storage','DEFAULT','management-server','storage.max.volume.size','2000','The maximum size for a volume (in GB).'),('Advanced','DEFAULT','StorageAllocator','storage.overprovisioning.factor','10','Storage Allocator overprovisioning factor'),('Storage','DEFAULT','management-server','storage.pool.max.waitseconds','3600','Timeout (in seconds) to synchronize storage pool operations.'),('Advanced','DEFAULT','management-server','storage.stats.interval','60000','the interval in milliseconds when storage stats (per host) are retrieved from agents'),('Storage','DEFAULT','management-server','storage.template.cleanup.enabled','true','Enable/disable template cleanup activity, only take effect when overall storage cleanup is enabled'),('Advanced','DEFAULT','AgentManager','sync.interval','60','Cluster Delta sync interval in seconds'),('Advanced','DEFAULT','management-server','system.vm.auto.reserve.capacity','true','Indicates whether or not to automatically reserver system VM standby capacity.'),('Advanced','DEFAULT','null','system.vm.local.storage.required','false','null'),('Advanced','DEFAULT','ManagementServer','system.vm.use.local.storage','false','null'),('Advanced','DEFAULT','management-server','task.cleanup.retry.interval','600','Time (in seconds) to wait before retrying cleanup of tasks if the cleanup failed previously.  0 means to never retry.'),('Storage','DEFAULT','AgentManager','total.retries','4','The number of times each command sent to a host should be retried in case of failure.'),('Advanced','DEFAULT','AgentManager','update.wait','600','null'),('Advanced','DEFAULT','null','upgrade.url','http://example.com:8080/client/agent/update.zip','null'),('Advanced','DEFAULT','null','usage.aggregation.timezone','GMT','null'),('Premium','DEFAULT','management-server','usage.execution.timezone',NULL,'The timezone to use for usage job execution time'),('Advanced','DEFAULT','management-server','usage.stats.job.aggregation.range','15','the range of time for aggregating the user statistics specified in minutes (e.g. 1440 for daily, 60 for hourly)'),('Advanced','DEFAULT','management-server','usage.stats.job.exec.time','17:30','the time at which the usage statistics aggregation job will run as an HH24:MM time, e.g. 00:30 to run at 12:30am'),('Advanced','DEFAULT','NetworkManager','use.external.dns','false','Bypass the cloudstack dhcp/DNS server vm name service, use zone external dns1 and dns2'),('Advanced','DEFAULT','ManagementServer','use.local.storage','false','Indicates whether to use local storage pools or shared storage pools for system VMs.'),('Advanced','DEFAULT','management-server','use.user.concentrated.pod.allocation','true','If true, deployment planner applies the user concentration heuristic during VM resource allocation'),('Advanced','DEFAULT','management-server','vm.allocation.algorithm','random','If \'random\', hosts within a pod will be randomly considered for VM/volume allocation. If \'firstfit\', they will be considered on a first-fit basis.'),('Advanced','DEFAULT','management-server','vm.destory.forcestop','false','On destory, force-stop takes this value '),('Network','DEFAULT','management-server','vm.network.throttling.rate','200','Default data transfer rate in megabits per second allowed in User vm\'s default network.'),('Advanced','DEFAULT','management-server','vm.op.cancel.interval','3600','Time (in seconds) to wait before cancelling a operation'),('Advanced','DEFAULT','management-server','vm.op.cleanup.interval','86400','Interval to run the thread that cleans up the vm operations (in seconds)'),('Advanced','DEFAULT','management-server','vm.op.cleanup.wait','3600','Time (in seconds) to wait before cleanuping up any vm work items'),('Advanced','DEFAULT','management-server','vm.op.lock.state.retry','5','Times to retry locking the state of a VM for operations'),('Advanced','DEFAULT','management-server','vm.op.wait.interval','120','Time (in seconds) to wait before checking if a previous operation has succeeded'),('Advanced','DEFAULT','management-server','vm.stats.interval','60000','The interval (in milliseconds) when vm stats are retrieved from agents.'),('Advanced','DEFAULT','management-server','vm.tranisition.wait.interval','3600','Time (in seconds) to wait before taking over a VM in transition state'),('Advanced','DEFAULT','management-server','vmware.additional.vnc.portrange.size','1000','Start port number of additional VNC port range'),('Advanced','DEFAULT','management-server','vmware.additional.vnc.portrange.start','50000','Start port number of additional VNC port range'),('Advanced','DEFAULT','management-server','vmware.guest.vswitch',NULL,'Specify the vSwitch on host for guest network'),('Advanced','DEFAULT','management-server','vmware.management.portgroup','Management Network','Specify the management network name(for ESXi hosts)'),('Advanced','DEFAULT','management-server','vmware.percluster.host.max','8','maxmium hosts per vCenter cluster(do not let it grow over 8)'),('Advanced','DEFAULT','management-server','vmware.private.vswitch',NULL,'Specify the vSwitch on host for private network'),('Advanced','DEFAULT','management-server','vmware.public.vswitch',NULL,'Specify the vSwitch on host for public network'),('Advanced','DEFAULT','management-server','vmware.recycle.hung.wokervm','false','Specify whether or not to recycle hung worker VMs'),('Advanced','DEFAULT','management-server','vmware.reserve.cpu','false','Specify whether or not to reserve CPU based on CPU overprovisioning factor'),('Advanced','DEFAULT','management-server','vmware.reserve.mem','false','Specify whether or not to reserve memory based on memory overprovisioning factor'),('Advanced','DEFAULT','management-server','vmware.root.disk.controller','ide','Specify the default disk controller for root volumes, valid values are scsi, ide'),('Advanced','DEFAULT','management-server','vmware.service.console','Service Console','Specify the service console network name(for ESX hosts)'),('Advanced','DEFAULT','management-server','vmware.systemvm.nic.device.type','E1000','Specify the default network device type for system VMs, valid values are E1000, PCNet32, Vmxnet2, Vmxnet3'),('Advanced','DEFAULT','AgentManager','wait','3600','null'),('Advanced','DEFAULT','AgentManager','workers','5','Number of worker threads.'),('Advanced','DEFAULT','AgentManager','xapiwait','600','Time (in seconds) to wait for XAPI to return'),('Advanced','DEFAULT','management-server','xen.bond.storage.nics',NULL,'Attempt to bond the two networks if found'),('Hidden','DEFAULT','management-server','xen.create.pools.in.pod','false','Should we automatically add XenServers into pools that are inside a Pod'),('Advanced','DEFAULT','management-server','xen.guest.network.device',NULL,'Specify for guest network name label'),('Advanced','DEFAULT','management-server','xen.heartbeat.interval','60','heartbeat to use when implementing XenServer Self Fencing'),('Network','DEFAULT','management-server','xen.private.network.device',NULL,'Specify when the private network name is different'),('Network','DEFAULT','management-server','xen.public.network.device',NULL,'[ONLY IF THE PUBLIC NETWORK IS ON A DEDICATED NIC]:The network name label of the physical device dedicated to the public network on a XenServer host'),('Advanced','DEFAULT','management-server','xen.setup.multipath','false','Setup the host to do multipath'),('Network','DEFAULT','management-server','xen.storage.network.device1','cloud-stor1','Specify when there are storage networks'),('Network','DEFAULT','management-server','xen.storage.network.device2','cloud-stor2','Specify when there are storage networks');
/*!40000 ALTER TABLE `configuration` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `console_proxy`
--

DROP TABLE IF EXISTS `console_proxy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `console_proxy` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `public_mac_address` varchar(17) DEFAULT NULL COMMENT 'mac address of the public facing network card',
  `public_ip_address` char(40) DEFAULT NULL COMMENT 'public ip address for the console proxy',
  `public_netmask` varchar(15) DEFAULT NULL COMMENT 'public netmask used for the console proxy',
  `active_session` int(10) NOT NULL DEFAULT '0' COMMENT 'active session number',
  `last_update` datetime DEFAULT NULL COMMENT 'Last session update time',
  `session_details` blob COMMENT 'session detail info',
  PRIMARY KEY (`id`),
  UNIQUE KEY `public_mac_address` (`public_mac_address`),
  UNIQUE KEY `public_ip_address` (`public_ip_address`),
  CONSTRAINT `fk_console_proxy__id` FOREIGN KEY (`id`) REFERENCES `vm_instance` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `console_proxy`
--

LOCK TABLES `console_proxy` WRITE;
/*!40000 ALTER TABLE `console_proxy` DISABLE KEYS */;
INSERT INTO `console_proxy` VALUES (2,'06:4d:44:00:00:a7','10.223.120.165','255.255.255.0',0,NULL,NULL);
/*!40000 ALTER TABLE `console_proxy` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `data_center`
--

DROP TABLE IF EXISTS `data_center`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `data_center` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `dns1` varchar(255) NOT NULL,
  `dns2` varchar(255) DEFAULT NULL,
  `internal_dns1` varchar(255) NOT NULL,
  `internal_dns2` varchar(255) DEFAULT NULL,
  `gateway` varchar(15) DEFAULT NULL,
  `netmask` varchar(15) DEFAULT NULL,
  `vnet` varchar(255) DEFAULT NULL,
  `router_mac_address` varchar(17) NOT NULL DEFAULT '02:00:00:00:00:01' COMMENT 'mac address for the router within the domain',
  `mac_address` bigint(20) unsigned NOT NULL DEFAULT '1' COMMENT 'Next available mac address for the ethernet card interacting with public internet',
  `guest_network_cidr` varchar(18) DEFAULT NULL,
  `domain` varchar(100) DEFAULT NULL COMMENT 'Network domain name of the Vms of the zone',
  `domain_id` bigint(20) unsigned DEFAULT NULL COMMENT 'domain id for the parent domain to this zone (null signifies public zone)',
  `networktype` varchar(255) NOT NULL DEFAULT 'Basic' COMMENT 'Network type of the zone',
  `dns_provider` char(64) DEFAULT 'VirtualRouter',
  `gateway_provider` char(64) DEFAULT 'VirtualRouter',
  `firewall_provider` char(64) DEFAULT 'VirtualRouter',
  `dhcp_provider` char(64) DEFAULT 'VirtualRouter',
  `lb_provider` char(64) DEFAULT 'VirtualRouter',
  `vpn_provider` char(64) DEFAULT 'VirtualRouter',
  `userdata_provider` char(64) DEFAULT 'VirtualRouter',
  `is_security_group_enabled` tinyint(4) NOT NULL DEFAULT '0' COMMENT '1: enabled, 0: not',
  `allocation_state` varchar(32) NOT NULL DEFAULT 'Enabled' COMMENT 'Is this data center enabled for allocation for new resources',
  `zone_token` varchar(255) DEFAULT NULL,
  `removed` datetime DEFAULT NULL COMMENT 'date removed if not null',
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`),
  KEY `i_data_center__domain_id` (`domain_id`),
  KEY `i_data_center__allocation_state` (`allocation_state`),
  KEY `i_data_center__zone_token` (`zone_token`),
  KEY `i_data_center__removed` (`removed`),
  CONSTRAINT `fk_data_center__domain_id` FOREIGN KEY (`domain_id`) REFERENCES `domain` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `data_center`
--

LOCK TABLES `data_center` WRITE;
/*!40000 ALTER TABLE `data_center` DISABLE KEYS */;
INSERT INTO `data_center` VALUES (1,'ZONE1',NULL,'72.52.126.11','72.52.126.12','10.223.110.253',NULL,NULL,NULL,'1015-1029','02:00:00:00:00:01',287,'10.10.10.0/24',NULL,NULL,'Advanced','VirtualRouter','VirtualRouter','VirtualRouter','VirtualRouter','VirtualRouter','VirtualRouter','VirtualRouter',0,'Enabled','75d9bd7a-d160-3f61-b7f9-1681177ceded',NULL);
/*!40000 ALTER TABLE `data_center` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `data_center_details`
--

DROP TABLE IF EXISTS `data_center_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `data_center_details` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `dc_id` bigint(20) unsigned NOT NULL COMMENT 'dc id',
  `name` varchar(255) NOT NULL,
  `value` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_dc_details__dc_id` (`dc_id`),
  CONSTRAINT `fk_dc_details__dc_id` FOREIGN KEY (`dc_id`) REFERENCES `data_center` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `data_center_details`
--

LOCK TABLES `data_center_details` WRITE;
/*!40000 ALTER TABLE `data_center_details` DISABLE KEYS */;
/*!40000 ALTER TABLE `data_center_details` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `disk_offering`
--

DROP TABLE IF EXISTS `disk_offering`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `disk_offering` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `domain_id` bigint(20) unsigned DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `display_text` varchar(4096) DEFAULT NULL COMMENT 'Descrianaption text set by the admin for display purpose only',
  `disk_size` bigint(20) unsigned NOT NULL COMMENT 'disk space in byte',
  `type` varchar(32) DEFAULT NULL COMMENT 'inheritted by who?',
  `tags` varchar(4096) DEFAULT NULL COMMENT 'comma separated tags about the disk_offering',
  `recreatable` tinyint(1) unsigned NOT NULL DEFAULT '0' COMMENT 'The root disk is always recreatable',
  `use_local_storage` tinyint(1) unsigned NOT NULL DEFAULT '0' COMMENT 'Indicates whether local storage pools should be used',
  `unique_name` varchar(32) DEFAULT NULL COMMENT 'unique name',
  `system_use` tinyint(1) unsigned NOT NULL DEFAULT '0' COMMENT 'is this offering for system used only',
  `customized` tinyint(1) unsigned NOT NULL DEFAULT '0' COMMENT '0 implies not customized by default',
  `removed` datetime DEFAULT NULL COMMENT 'date removed',
  `created` datetime DEFAULT NULL COMMENT 'date the disk offering was created',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_name` (`unique_name`),
  KEY `i_disk_offering__removed` (`removed`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `disk_offering`
--

LOCK TABLES `disk_offering` WRITE;
/*!40000 ALTER TABLE `disk_offering` DISABLE KEYS */;
INSERT INTO `disk_offering` VALUES (1,NULL,'Small Instance','Small Instance [500MHZ CPU, 512MB MEM, 16GB Disk] - $0.10 per hour',0,'Service',NULL,0,0,NULL,0,1,NULL,'2012-02-28 19:21:37'),(2,NULL,'Medium Instance','Medium Instance [2GHZ CPU, 2GB MEM, 32GB Disk] - $0.20 per hour',0,'Service',NULL,0,0,NULL,0,1,NULL,'2012-02-28 19:21:37'),(3,NULL,'Large Instance','Large Instance [2GHZ CPU, 4GB MEM, 64GB Disk] - $0.30 per hour',0,'Service',NULL,0,0,NULL,0,1,NULL,'2012-02-28 19:21:37'),(4,1,'Small Disk','Small Disk [16GB Disk]',17179869184,'Disk',NULL,0,0,NULL,0,0,NULL,'2012-02-28 19:21:37'),(5,1,'Medium Disk','Medium Disk [32GB Disk]',34359738368,'Disk',NULL,0,0,NULL,0,0,NULL,'2012-02-28 19:21:37'),(6,1,'Large Disk','Large Disk [64GB Disk]',68719476736,'Disk',NULL,0,0,NULL,0,0,NULL,'2012-02-28 19:21:37'),(7,NULL,'Small Instance','Small Instance, $0.05 per hour',0,'Service',NULL,0,0,'Cloud.Com-Small Instance',0,1,NULL,'2012-02-28 19:29:19'),(8,NULL,'Medium Instance','Medium Instance, $0.10 per hour',0,'Service',NULL,0,0,'Cloud.Com-Medium Instance',0,1,NULL,'2012-02-28 19:29:19'),(9,NULL,'Small','Small Disk, 5 GB',5368709120,'Disk',NULL,0,0,'Cloud.Com-Small',0,0,NULL,'2012-02-28 19:29:19'),(10,NULL,'Medium','Medium Disk, 20 GB',21474836480,'Disk',NULL,0,0,'Cloud.Com-Medium',0,0,NULL,'2012-02-28 19:29:19'),(11,NULL,'Large','Large Disk, 100 GB',107374182400,'Disk',NULL,0,0,'Cloud.Com-Large',0,0,NULL,'2012-02-28 19:29:19'),(12,NULL,'System Offering For Secondary Storage VM',NULL,0,'Service',NULL,1,0,'Cloud.com-SecondaryStorage',1,1,NULL,'2012-02-28 19:29:22'),(13,NULL,'System Offering For Software Router',NULL,0,'Service',NULL,1,0,'Cloud.Com-SoftwareRouter',1,1,NULL,'2012-02-28 19:29:22'),(14,NULL,'System Offering For Console Proxy',NULL,0,'Service',NULL,1,0,'Cloud.com-ConsoleProxy',1,1,NULL,'2012-02-28 19:29:22'),(15,NULL,'System Offering For Elastic LB VM',NULL,0,'Service',NULL,1,0,'Cloud.Com-ElasticLBVm',1,1,NULL,'2012-02-28 19:29:22');
/*!40000 ALTER TABLE `disk_offering` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `domain`
--

DROP TABLE IF EXISTS `domain`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `domain` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `parent` bigint(20) unsigned DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `owner` bigint(20) unsigned NOT NULL,
  `path` varchar(255) NOT NULL,
  `level` int(10) NOT NULL DEFAULT '0',
  `child_count` int(10) NOT NULL DEFAULT '0',
  `next_child_seq` bigint(20) unsigned NOT NULL DEFAULT '1',
  `removed` datetime DEFAULT NULL COMMENT 'date removed',
  `state` char(32) NOT NULL DEFAULT 'Active' COMMENT 'state of the domain',
  `network_domain` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `parent` (`parent`,`name`,`removed`),
  KEY `i_domain__path` (`path`),
  KEY `i_domain__removed` (`removed`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `domain`
--

LOCK TABLES `domain` WRITE;
/*!40000 ALTER TABLE `domain` DISABLE KEYS */;
INSERT INTO `domain` VALUES (1,NULL,'ROOT',2,'/',0,1,2,NULL,'Active',NULL),(2,1,'1',2,'/1/',1,0,1,NULL,'Active',NULL);
/*!40000 ALTER TABLE `domain` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `domain_network_ref`
--

DROP TABLE IF EXISTS `domain_network_ref`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `domain_network_ref` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
  `domain_id` bigint(20) unsigned NOT NULL COMMENT 'domain id',
  `network_id` bigint(20) unsigned NOT NULL COMMENT 'network id',
  PRIMARY KEY (`id`),
  KEY `fk_domain_network_ref__domain_id` (`domain_id`),
  KEY `fk_domain_network_ref__networks_id` (`network_id`),
  CONSTRAINT `fk_domain_network_ref__domain_id` FOREIGN KEY (`domain_id`) REFERENCES `domain` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_domain_network_ref__networks_id` FOREIGN KEY (`network_id`) REFERENCES `networks` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `domain_network_ref`
--

LOCK TABLES `domain_network_ref` WRITE;
/*!40000 ALTER TABLE `domain_network_ref` DISABLE KEYS */;
INSERT INTO `domain_network_ref` VALUES (1,1,205),(2,2,208);
/*!40000 ALTER TABLE `domain_network_ref` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `domain_router`
--

DROP TABLE IF EXISTS `domain_router`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `domain_router` (
  `id` bigint(20) unsigned NOT NULL COMMENT 'Primary Key',
  `public_mac_address` varchar(17) DEFAULT NULL COMMENT 'mac address of the public facing network card',
  `public_ip_address` char(40) DEFAULT NULL COMMENT 'public ip address used for source net',
  `public_netmask` varchar(15) DEFAULT NULL COMMENT 'netmask used for the domR',
  `guest_netmask` varchar(15) DEFAULT NULL COMMENT 'netmask used for the guest network',
  `guest_ip_address` char(40) DEFAULT NULL COMMENT ' ip address in the guest network',
  `network_id` bigint(20) unsigned NOT NULL COMMENT 'network configuration that this domain router belongs to',
  `is_redundant_router` int(1) unsigned NOT NULL COMMENT 'if in redundant router mode',
  `priority` int(4) unsigned DEFAULT NULL COMMENT 'priority of router in the redundant router mode',
  `is_priority_bumpup` int(1) unsigned NOT NULL COMMENT 'if the priority has been bumped up',
  `redundant_state` varchar(64) NOT NULL COMMENT 'the state of redundant virtual router',
  `stop_pending` int(1) unsigned NOT NULL COMMENT 'if this router would be stopped after we can connect to it',
  `role` varchar(64) NOT NULL COMMENT 'type of role played by this router',
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`),
  CONSTRAINT `fk_domain_router__id` FOREIGN KEY (`id`) REFERENCES `vm_instance` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='information about the domR instance';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `domain_router`
--

LOCK TABLES `domain_router` WRITE;
/*!40000 ALTER TABLE `domain_router` DISABLE KEYS */;
INSERT INTO `domain_router` VALUES (7,'06:c0:dc:00:00:66','10.223.120.100','255.255.255.0',NULL,'10.10.10.1',207,0,0,0,'UNKNOWN',0,'DHCP_FIREWALL_LB_PASSWD_USERDATA'),(9,NULL,NULL,NULL,NULL,'172.21.0.101',205,0,0,0,'UNKNOWN',0,'DHCP_USERDATA'),(11,'06:d3:bc:00:00:c9','10.223.120.199','255.255.255.0',NULL,'10.10.10.1',209,0,0,0,'UNKNOWN',0,'DHCP_FIREWALL_LB_PASSWD_USERDATA'),(12,NULL,NULL,NULL,NULL,'172.22.0.102',206,0,0,0,'UNKNOWN',0,'DHCP_USERDATA'),(13,NULL,NULL,NULL,NULL,'172.23.0.101',208,0,0,0,'UNKNOWN',0,'DHCP_USERDATA'),(14,NULL,NULL,NULL,NULL,'172.20.0.101',204,0,0,0,'UNKNOWN',0,'DHCP_USERDATA');
/*!40000 ALTER TABLE `domain_router` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `elastic_lb_vm_map`
--

DROP TABLE IF EXISTS `elastic_lb_vm_map`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `elastic_lb_vm_map` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `ip_addr_id` bigint(20) unsigned NOT NULL,
  `elb_vm_id` bigint(20) unsigned NOT NULL,
  `lb_id` bigint(20) unsigned DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_elastic_lb_vm_map__ip_id` (`ip_addr_id`),
  KEY `fk_elastic_lb_vm_map__elb_vm_id` (`elb_vm_id`),
  KEY `fk_elastic_lb_vm_map__lb_id` (`lb_id`),
  CONSTRAINT `fk_elastic_lb_vm_map__ip_id` FOREIGN KEY (`ip_addr_id`) REFERENCES `user_ip_address` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_elastic_lb_vm_map__elb_vm_id` FOREIGN KEY (`elb_vm_id`) REFERENCES `vm_instance` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_elastic_lb_vm_map__lb_id` FOREIGN KEY (`lb_id`) REFERENCES `load_balancing_rules` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `elastic_lb_vm_map`
--

LOCK TABLES `elastic_lb_vm_map` WRITE;
/*!40000 ALTER TABLE `elastic_lb_vm_map` DISABLE KEYS */;
/*!40000 ALTER TABLE `elastic_lb_vm_map` ENABLE KEYS */;
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
  `state` varchar(32) NOT NULL DEFAULT 'Completed',
  `description` varchar(1024) NOT NULL,
  `user_id` bigint(20) unsigned NOT NULL,
  `account_id` bigint(20) unsigned NOT NULL,
  `created` datetime NOT NULL,
  `level` varchar(16) NOT NULL,
  `start_id` bigint(20) unsigned NOT NULL DEFAULT '0',
  `parameters` varchar(1024) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `i_event__created` (`created`),
  KEY `i_event__user_id` (`user_id`),
  KEY `i_event__account_id` (`account_id`),
  KEY `i_event__level_id` (`level`),
  KEY `i_event__type_id` (`type`)
) ENGINE=InnoDB AUTO_INCREMENT=57 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `event`
--

LOCK TABLES `event` WRITE;
/*!40000 ALTER TABLE `event` DISABLE KEYS */;
INSERT INTO `event` VALUES (1,'VLAN.IP.RANGE.CREATE','Completed','Successfully completed creating vlan ip range',1,1,'2012-02-28 19:29:51','INFO',0,NULL),(2,'CONFIGURATION.VALUE.EDIT','Completed','Successfully completed updating configuration.  Name: instance.name New Value: st',1,1,'2012-02-28 19:29:51','INFO',0,NULL),(3,'CONFIGURATION.VALUE.EDIT','Completed','Successfully completed updating configuration.  Name: usage.stats.job.exec.time New Value: 17:30',1,1,'2012-02-28 19:29:51','INFO',0,NULL),(4,'CONFIGURATION.VALUE.EDIT','Completed','Successfully completed updating configuration.  Name: usage.stats.job.aggregation.range New Value: 15',1,1,'2012-02-28 19:29:51','INFO',0,NULL),(5,'CONFIGURATION.VALUE.EDIT','Completed','Successfully completed updating configuration.  Name: start.retry New Value: 6',1,1,'2012-02-28 19:29:51','INFO',0,NULL),(6,'CONFIGURATION.VALUE.EDIT','Completed','Successfully completed updating configuration.  Name: storage.cleanup.interval New Value: 6000',1,1,'2012-02-28 19:29:51','INFO',0,NULL),(7,'CONFIGURATION.VALUE.EDIT','Completed','Successfully completed updating configuration.  Name: expunge.interval New Value: 60',1,1,'2012-02-28 19:29:51','INFO',0,NULL),(8,'CONFIGURATION.VALUE.EDIT','Completed','Successfully completed updating configuration.  Name: expunge.delay New Value: 60',1,1,'2012-02-28 19:29:51','INFO',0,NULL),(9,'CONFIGURATION.VALUE.EDIT','Completed','Successfully completed updating configuration.  Name: storage.overprovisioning.factor New Value: 10',1,1,'2012-02-28 19:29:51','INFO',0,NULL),(10,'CONFIGURATION.VALUE.EDIT','Completed','Successfully completed updating configuration.  Name: wait New Value: 3600',1,1,'2012-02-28 19:29:51','INFO',0,NULL),(11,'CONFIGURATION.VALUE.EDIT','Completed','Successfully completed updating configuration.  Name: storage.cleanup.interval New Value: 60',1,1,'2012-02-28 19:29:51','INFO',0,NULL),(12,'CONFIGURATION.VALUE.EDIT','Completed','Error while updating configuration.  Name: project.smtp.host New Value: smtp.gmail.com',1,1,'2012-02-28 19:29:51','ERROR',0,NULL),(13,'CONFIGURATION.VALUE.EDIT','Completed','Error while updating configuration.  Name: project.smtp.port New Value: 465',1,1,'2012-02-28 19:29:51','ERROR',0,NULL),(14,'CONFIGURATION.VALUE.EDIT','Completed','Error while updating configuration.  Name: project.smtp.useAuth New Value: true',1,1,'2012-02-28 19:29:51','ERROR',0,NULL),(15,'CONFIGURATION.VALUE.EDIT','Completed','Error while updating configuration.  Name: project.smtp.username New Value: alenavmops@gmail.com',1,1,'2012-02-28 19:29:51','ERROR',0,NULL),(16,'CONFIGURATION.VALUE.EDIT','Completed','Error while updating configuration.  Name: project.smtp.password New Value: *****',1,1,'2012-02-28 19:29:51','ERROR',0,NULL),(17,'CONFIGURATION.VALUE.EDIT','Completed','Error while updating configuration.  Name: project.email.sender New Value: alenavmops@gmail.com',1,1,'2012-02-28 19:29:51','ERROR',0,NULL),(18,'USER.LOGIN','Completed','user has logged in',2,2,'2012-02-28 19:33:34','INFO',0,NULL),(19,'USER.LOGIN','Completed','user has logged in',2,2,'2012-02-28 21:36:57','INFO',0,NULL),(20,'NETWORK.CREATE','Completed','Successfully completed creating network. Network Id: 204',2,2,'2012-02-28 21:39:19','INFO',0,NULL),(21,'NETWORK.CREATE','Completed','Successfully completed creating network. Network Id: 205',2,2,'2012-02-28 21:40:00','INFO',0,NULL),(22,'NETWORK.CREATE','Completed','Successfully completed creating network. Network Id: 206',2,2,'2012-02-28 21:40:25','INFO',0,NULL),(23,'NETWORK.CREATE','Completed','Successfully completed creating network. Network Id: 207',2,2,'2012-02-28 21:40:31','INFO',0,NULL),(24,'VM.CREATE','Created','Successfully created entity for deploying Vm. Vm Id: 3',2,2,'2012-02-28 21:40:32','INFO',0,NULL),(25,'VM.CREATE','Scheduled','Scheduled async job for starting Vm. Vm Id: 3',2,2,'2012-02-28 21:40:32','INFO',24,NULL),(26,'VM.CREATE','Started','Starting job for starting Vm. Vm Id: 3',2,2,'2012-02-28 21:40:32','INFO',24,NULL),(27,'VM.CREATE','Completed','Error while starting Vm. Vm Id: 3',2,2,'2012-02-28 21:40:33','ERROR',24,NULL),(28,'VM.CREATE','Created','Successfully created entity for deploying Vm. Vm Id: 4',2,2,'2012-02-28 21:40:40','INFO',0,NULL),(29,'VM.CREATE','Scheduled','Scheduled async job for starting Vm. Vm Id: 4',2,2,'2012-02-28 21:40:40','INFO',28,NULL),(30,'VM.CREATE','Started','Starting job for starting Vm. Vm Id: 4',2,2,'2012-02-28 21:40:40','INFO',28,NULL),(31,'VM.CREATE','Completed','Error while starting Vm. Vm Id: 4',2,2,'2012-02-28 21:40:40','ERROR',28,NULL),(32,'VM.CREATE','Created','Successfully created entity for deploying Vm. Vm Id: 5',2,2,'2012-02-28 21:42:08','INFO',0,NULL),(33,'VM.CREATE','Scheduled','Scheduled async job for starting Vm. Vm Id: 5',2,2,'2012-02-28 21:42:08','INFO',32,NULL),(34,'VM.CREATE','Started','Starting job for starting Vm. Vm Id: 5',2,2,'2012-02-28 21:42:08','INFO',32,NULL),(35,'VM.CREATE','Completed','Error while starting Vm. Vm Id: 5',2,2,'2012-02-28 21:42:08','ERROR',32,NULL),(36,'USER.LOGIN','Completed','user has logged in',2,2,'2012-02-28 21:43:28','INFO',0,NULL),(37,'VM.CREATE','Created','Successfully created entity for deploying Vm. Vm Id: 6',2,2,'2012-02-28 21:44:11','INFO',0,NULL),(38,'VM.CREATE','Scheduled','Scheduled async job for starting Vm. Vm Id: 6',2,2,'2012-02-28 21:44:11','INFO',37,NULL),(39,'VM.CREATE','Started','Starting job for starting Vm. Vm Id: 6',2,2,'2012-02-28 21:44:11','INFO',37,NULL),(40,'ZONE.VLAN.ASSIGN','Completed','Assigned Zone Vlan: 1024 Network Id: 207',2,2,'2012-02-28 21:44:11','INFO',0,NULL),(41,'VM.CREATE','Created','Successfully created entity for deploying Vm. Vm Id: 8',2,2,'2012-02-28 21:44:33','INFO',0,NULL),(42,'VM.CREATE','Scheduled','Scheduled async job for starting Vm. Vm Id: 8',2,2,'2012-02-28 21:44:33','INFO',41,NULL),(43,'VM.CREATE','Started','Starting job for starting Vm. Vm Id: 8',2,2,'2012-02-28 21:44:33','INFO',41,NULL),(44,'DOMAIN.CREATE','Completed','Successfully completed creating Domain. Domain Name: 1, Parent DomainId :1',2,1,'2012-02-28 21:46:05','INFO',0,NULL),(45,'ACCOUNT.CREATE','Completed','Successfully completed creating Account. Account Name: 1, Domain Id:2',2,1,'2012-02-28 21:46:21','INFO',0,NULL),(46,'NETWORK.CREATE','Completed','Successfully completed creating network. Network Id: 208',2,2,'2012-02-28 21:47:06','INFO',0,NULL),(47,'USER.LOGOUT','Completed','user has logged out',2,2,'2012-02-28 21:47:08','INFO',0,NULL),(48,'USER.LOGIN','Completed','user has logged in',3,3,'2012-02-28 21:47:12','INFO',0,NULL),(49,'NETWORK.CREATE','Completed','Successfully completed creating network. Network Id: 209',3,3,'2012-02-28 21:47:17','INFO',0,NULL),(50,'VM.CREATE','Completed','Successfully completed starting Vm. Vm Id: 6',2,2,'2012-02-28 21:47:18','INFO',37,NULL),(51,'VM.CREATE','Created','Successfully created entity for deploying Vm. Vm Id: 10',3,3,'2012-02-28 21:47:26','INFO',0,NULL),(52,'VM.CREATE','Scheduled','Scheduled async job for starting Vm. Vm Id: 10',3,3,'2012-02-28 21:47:26','INFO',51,NULL),(53,'VM.CREATE','Started','Starting job for starting Vm. Vm Id: 10',3,3,'2012-02-28 21:47:26','INFO',51,NULL),(54,'ZONE.VLAN.ASSIGN','Completed','Assigned Zone Vlan: 1028 Network Id: 209',3,3,'2012-02-28 21:47:26','INFO',0,NULL),(55,'VM.CREATE','Completed','Successfully completed starting Vm. Vm Id: 8',2,2,'2012-02-28 21:51:33','INFO',41,NULL),(56,'VM.CREATE','Completed','Successfully completed starting Vm. Vm Id: 10',3,3,'2012-02-28 21:51:37','INFO',51,NULL);
/*!40000 ALTER TABLE `event` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `firewall_rules`
--

DROP TABLE IF EXISTS `firewall_rules`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `firewall_rules` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
  `ip_address_id` bigint(20) unsigned NOT NULL COMMENT 'id of the corresponding ip address',
  `start_port` int(10) DEFAULT NULL COMMENT 'starting port of a port range',
  `end_port` int(10) DEFAULT NULL COMMENT 'end port of a port range',
  `state` char(32) NOT NULL COMMENT 'current state of this rule',
  `protocol` char(16) NOT NULL DEFAULT 'TCP' COMMENT 'protocol to open these ports for',
  `purpose` char(32) NOT NULL COMMENT 'why are these ports opened?',
  `account_id` bigint(20) unsigned NOT NULL COMMENT 'owner id',
  `domain_id` bigint(20) unsigned NOT NULL COMMENT 'domain id',
  `network_id` bigint(20) unsigned NOT NULL COMMENT 'network id',
  `xid` char(40) NOT NULL COMMENT 'external id',
  `created` datetime DEFAULT NULL COMMENT 'Date created',
  `icmp_code` int(10) DEFAULT NULL COMMENT 'The ICMP code (if protocol=ICMP). A value of -1 means all codes for the given ICMP type.',
  `icmp_type` int(10) DEFAULT NULL COMMENT 'The ICMP type (if protocol=ICMP). A value of -1 means all types.',
  `related` bigint(20) unsigned DEFAULT NULL COMMENT 'related to what other firewall rule',
  PRIMARY KEY (`id`),
  KEY `fk_firewall_rules__ip_address_id` (`ip_address_id`),
  KEY `fk_firewall_rules__network_id` (`network_id`),
  KEY `fk_firewall_rules__account_id` (`account_id`),
  KEY `fk_firewall_rules__domain_id` (`domain_id`),
  KEY `fk_firewall_rules__related` (`related`),
  KEY `i_firewall_rules__purpose` (`purpose`),
  CONSTRAINT `fk_firewall_rules__ip_address_id` FOREIGN KEY (`ip_address_id`) REFERENCES `user_ip_address` (`id`),
  CONSTRAINT `fk_firewall_rules__network_id` FOREIGN KEY (`network_id`) REFERENCES `networks` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_firewall_rules__account_id` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_firewall_rules__domain_id` FOREIGN KEY (`domain_id`) REFERENCES `domain` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_firewall_rules__related` FOREIGN KEY (`related`) REFERENCES `firewall_rules` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `firewall_rules`
--

LOCK TABLES `firewall_rules` WRITE;
/*!40000 ALTER TABLE `firewall_rules` DISABLE KEYS */;
/*!40000 ALTER TABLE `firewall_rules` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `firewall_rules_cidrs`
--

DROP TABLE IF EXISTS `firewall_rules_cidrs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `firewall_rules_cidrs` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
  `firewall_rule_id` bigint(20) unsigned NOT NULL COMMENT 'firewall rule id',
  `source_cidr` varchar(18) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_rule_cidrs` (`firewall_rule_id`,`source_cidr`),
  KEY `fk_firewall_cidrs_firewall_rules` (`firewall_rule_id`),
  CONSTRAINT `fk_firewall_cidrs_firewall_rules` FOREIGN KEY (`firewall_rule_id`) REFERENCES `firewall_rules` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `firewall_rules_cidrs`
--

LOCK TABLES `firewall_rules_cidrs` WRITE;
/*!40000 ALTER TABLE `firewall_rules_cidrs` DISABLE KEYS */;
/*!40000 ALTER TABLE `firewall_rules_cidrs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `guest_os`
--

DROP TABLE IF EXISTS `guest_os`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `guest_os` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `category_id` bigint(20) unsigned NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `display_name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_guest_os__category_id` (`category_id`),
  CONSTRAINT `fk_guest_os__category_id` FOREIGN KEY (`category_id`) REFERENCES `guest_os_category` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=206 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `guest_os`
--

LOCK TABLES `guest_os` WRITE;
/*!40000 ALTER TABLE `guest_os` DISABLE KEYS */;
INSERT INTO `guest_os` VALUES (1,1,NULL,'CentOS 4.5 (32-bit)'),(2,1,NULL,'CentOS 4.6 (32-bit)'),(3,1,NULL,'CentOS 4.7 (32-bit)'),(4,1,NULL,'CentOS 4.8 (32-bit)'),(5,1,NULL,'CentOS 5.0 (32-bit)'),(6,1,NULL,'CentOS 5.0 (64-bit)'),(7,1,NULL,'CentOS 5.1 (32-bit)'),(8,1,NULL,'CentOS 5.1 (64-bit)'),(9,1,NULL,'CentOS 5.2 (32-bit)'),(10,1,NULL,'CentOS 5.2 (64-bit)'),(11,1,NULL,'CentOS 5.3 (32-bit)'),(12,1,NULL,'CentOS 5.3 (64-bit)'),(13,1,NULL,'CentOS 5.4 (32-bit)'),(14,1,NULL,'CentOS 5.4 (64-bit)'),(15,2,NULL,'Debian GNU/Linux 5.0 (32-bit)'),(16,3,NULL,'Oracle Enterprise Linux 5.0 (32-bit)'),(17,3,NULL,'Oracle Enterprise Linux 5.0 (64-bit)'),(18,3,NULL,'Oracle Enterprise Linux 5.1 (32-bit)'),(19,3,NULL,'Oracle Enterprise Linux 5.1 (64-bit)'),(20,3,NULL,'Oracle Enterprise Linux 5.2 (32-bit)'),(21,3,NULL,'Oracle Enterprise Linux 5.2 (64-bit)'),(22,3,NULL,'Oracle Enterprise Linux 5.3 (32-bit)'),(23,3,NULL,'Oracle Enterprise Linux 5.3 (64-bit)'),(24,3,NULL,'Oracle Enterprise Linux 5.4 (32-bit)'),(25,3,NULL,'Oracle Enterprise Linux 5.4 (64-bit)'),(26,4,NULL,'Red Hat Enterprise Linux 4.5 (32-bit)'),(27,4,NULL,'Red Hat Enterprise Linux 4.6 (32-bit)'),(28,4,NULL,'Red Hat Enterprise Linux 4.7 (32-bit)'),(29,4,NULL,'Red Hat Enterprise Linux 4.8 (32-bit)'),(30,4,NULL,'Red Hat Enterprise Linux 5.0 (32-bit)'),(31,4,NULL,'Red Hat Enterprise Linux 5.0 (64-bit)'),(32,4,NULL,'Red Hat Enterprise Linux 5.1 (32-bit)'),(33,4,NULL,'Red Hat Enterprise Linux 5.1 (64-bit)'),(34,4,NULL,'Red Hat Enterprise Linux 5.2 (32-bit)'),(35,4,NULL,'Red Hat Enterprise Linux 5.2 (64-bit)'),(36,4,NULL,'Red Hat Enterprise Linux 5.3 (32-bit)'),(37,4,NULL,'Red Hat Enterprise Linux 5.3 (64-bit)'),(38,4,NULL,'Red Hat Enterprise Linux 5.4 (32-bit)'),(39,4,NULL,'Red Hat Enterprise Linux 5.4 (64-bit)'),(40,5,NULL,'SUSE Linux Enterprise Server 9 SP4 (32-bit)'),(41,5,NULL,'SUSE Linux Enterprise Server 10 SP1 (32-bit)'),(42,5,NULL,'SUSE Linux Enterprise Server 10 SP1 (64-bit)'),(43,5,NULL,'SUSE Linux Enterprise Server 10 SP2 (32-bit)'),(44,5,NULL,'SUSE Linux Enterprise Server 10 SP2 (64-bit)'),(45,5,NULL,'SUSE Linux Enterprise Server 10 SP3 (64-bit)'),(46,5,NULL,'SUSE Linux Enterprise Server 11 (32-bit)'),(47,5,NULL,'SUSE Linux Enterprise Server 11 (64-bit)'),(48,6,NULL,'Windows 7 (32-bit)'),(49,6,NULL,'Windows 7 (64-bit)'),(50,6,NULL,'Windows Server 2003 Enterprise Edition(32-bit)'),(51,6,NULL,'Windows Server 2003 Enterprise Edition(64-bit)'),(52,6,NULL,'Windows Server 2008 (32-bit)'),(53,6,NULL,'Windows Server 2008 (64-bit)'),(54,6,NULL,'Windows Server 2008 R2 (64-bit)'),(55,6,NULL,'Windows 2000 Server SP4 (32-bit)'),(56,6,NULL,'Windows Vista (32-bit)'),(57,6,NULL,'Windows XP SP2 (32-bit)'),(58,6,NULL,'Windows XP SP3 (32-bit)'),(59,10,NULL,'Other Ubuntu (32-bit)'),(60,7,NULL,'Other (32-bit)'),(61,6,NULL,'Windows 2000 Server'),(62,6,NULL,'Windows 98'),(63,6,NULL,'Windows 95'),(64,6,NULL,'Windows NT 4'),(65,6,NULL,'Windows 3.1'),(66,4,NULL,'Red Hat Enterprise Linux 3(32-bit)'),(67,4,NULL,'Red Hat Enterprise Linux 3(64-bit)'),(68,7,NULL,'Open Enterprise Server'),(69,7,NULL,'Asianux 3(32-bit)'),(70,7,NULL,'Asianux 3(64-bit)'),(72,2,NULL,'Debian GNU/Linux 5(64-bit)'),(73,2,NULL,'Debian GNU/Linux 4(32-bit)'),(74,2,NULL,'Debian GNU/Linux 4(64-bit)'),(75,7,NULL,'Other 2.6x Linux (32-bit)'),(76,7,NULL,'Other 2.6x Linux (64-bit)'),(77,8,NULL,'Novell Netware 6.x'),(78,8,NULL,'Novell Netware 5.1'),(79,9,NULL,'Sun Solaris 10(32-bit)'),(80,9,NULL,'Sun Solaris 10(64-bit)'),(81,9,NULL,'Sun Solaris 9(Experimental)'),(82,9,NULL,'Sun Solaris 8(Experimental)'),(83,9,NULL,'FreeBSD (32-bit)'),(84,9,NULL,'FreeBSD (64-bit)'),(85,9,NULL,'SCO OpenServer 5'),(86,9,NULL,'SCO UnixWare 7'),(87,6,NULL,'Windows Server 2003 DataCenter Edition(32-bit)'),(88,6,NULL,'Windows Server 2003 DataCenter Edition(64-bit)'),(89,6,NULL,'Windows Server 2003 Standard Edition(32-bit)'),(90,6,NULL,'Windows Server 2003 Standard Edition(64-bit)'),(91,6,NULL,'Windows Server 2003 Web Edition'),(92,6,NULL,'Microsoft Small Bussiness Server 2003'),(93,6,NULL,'Windows XP (32-bit)'),(94,6,NULL,'Windows XP (64-bit)'),(95,6,NULL,'Windows 2000 Advanced Server'),(96,5,NULL,'SUSE Linux Enterprise 8(32-bit)'),(97,5,NULL,'SUSE Linux Enterprise 8(64-bit)'),(98,7,NULL,'Other Linux (32-bit)'),(99,7,NULL,'Other Linux (64-bit)'),(100,10,NULL,'Other Ubuntu (64-bit)'),(101,6,NULL,'Windows Vista (64-bit)'),(102,6,NULL,'DOS'),(103,7,NULL,'Other (64-bit)'),(104,7,NULL,'OS/2'),(105,6,NULL,'Windows 2000 Professional'),(106,4,NULL,'Red Hat Enterprise Linux 4(64-bit)'),(107,5,NULL,'SUSE Linux Enterprise 9(32-bit)'),(108,5,NULL,'SUSE Linux Enterprise 9(64-bit)'),(109,5,NULL,'SUSE Linux Enterprise 10(32-bit)'),(110,5,NULL,'SUSE Linux Enterprise 10(64-bit)'),(111,1,NULL,'CentOS 5.5 (32-bit)'),(112,1,NULL,'CentOS 5.5 (64-bit)'),(113,4,NULL,'Red Hat Enterprise Linux 5.5 (32-bit)'),(114,4,NULL,'Red Hat Enterprise Linux 5.5 (64-bit)'),(115,4,NULL,'Fedora 13'),(116,4,NULL,'Fedora 12'),(117,4,NULL,'Fedora 11'),(118,4,NULL,'Fedora 10'),(119,4,NULL,'Fedora 9'),(120,4,NULL,'Fedora 8'),(121,10,NULL,'Ubuntu 10.04 (32-bit)'),(122,10,NULL,'Ubuntu 9.10 (32-bit)'),(123,10,NULL,'Ubuntu 9.04 (32-bit)'),(124,10,NULL,'Ubuntu 8.10 (32-bit)'),(125,10,NULL,'Ubuntu 8.04 (32-bit)'),(126,10,NULL,'Ubuntu 10.04 (64-bit)'),(127,10,NULL,'Ubuntu 9.10 (64-bit)'),(128,10,NULL,'Ubuntu 9.04 (64-bit)'),(129,10,NULL,'Ubuntu 8.10 (64-bit)'),(130,10,NULL,'Ubuntu 8.04 (64-bit)'),(131,10,NULL,'Red Hat Enterprise Linux 2'),(132,2,NULL,'Debian GNU/Linux 6(32-bit)'),(133,2,NULL,'Debian GNU/Linux 6(64-bit)'),(134,3,NULL,'Oracle Enterprise Linux 5.5 (32-bit)'),(135,3,NULL,'Oracle Enterprise Linux 5.5 (64-bit)'),(136,4,NULL,'Red Hat Enterprise Linux 6.0 (32-bit)'),(137,4,NULL,'Red Hat Enterprise Linux 6.0 (64-bit)'),(138,7,NULL,'None'),(139,7,NULL,'Other PV (32-bit)'),(140,7,NULL,'Other PV (64-bit)'),(141,9,NULL,'Sun Solaris 11 (64-bit)'),(142,9,NULL,'Sun Solaris 11 (32-bit)'),(143,6,NULL,'Windows PV'),(200,1,NULL,'Other CentOS (32-bit)'),(201,1,NULL,'Other CentOS (64-bit)'),(202,5,NULL,'Other SUSE Linux(32-bit)'),(203,5,NULL,'Other SUSE Linux(64-bit)'),(204,4,NULL,'Red Hat Enterprise Linux 6(32-bit)'),(205,4,NULL,'Red Hat Enterprise Linux 6(64-bit)');
/*!40000 ALTER TABLE `guest_os` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `guest_os_category`
--

DROP TABLE IF EXISTS `guest_os_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `guest_os_category` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

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
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `guest_os_hypervisor` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `hypervisor_type` varchar(32) NOT NULL,
  `guest_os_name` varchar(255) NOT NULL,
  `guest_os_id` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=266 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `guest_os_hypervisor`
--

LOCK TABLES `guest_os_hypervisor` WRITE;
/*!40000 ALTER TABLE `guest_os_hypervisor` DISABLE KEYS */;
INSERT INTO `guest_os_hypervisor` VALUES (1,'XenServer','CentOS 4.5 (32-bit)',1),(2,'XenServer','CentOS 4.6 (32-bit)',2),(3,'XenServer','CentOS 4.7 (32-bit)',3),(4,'XenServer','CentOS 4.8 (32-bit)',4),(5,'XenServer','CentOS 5.0 (32-bit)',5),(6,'XenServer','CentOS 5.0 (64-bit)',6),(7,'XenServer','CentOS 5.1 (32-bit)',7),(8,'XenServer','CentOS 5.1 (32-bit)',8),(9,'XenServer','CentOS 5.2 (32-bit)',9),(10,'XenServer','CentOS 5.2 (64-bit)',10),(11,'XenServer','CentOS 5.3 (32-bit)',11),(12,'XenServer','CentOS 5.3 (64-bit)',12),(13,'XenServer','CentOS 5.4 (32-bit)',13),(14,'XenServer','CentOS 5.4 (64-bit)',14),(15,'XenServer','Debian Lenny 5.0 (32-bit)',15),(16,'XenServer','Oracle Enterprise Linux 5.0 (32-bit)',16),(17,'XenServer','Oracle Enterprise Linux 5.0 (64-bit)',17),(18,'XenServer','Oracle Enterprise Linux 5.1 (32-bit)',18),(19,'XenServer','Oracle Enterprise Linux 5.1 (64-bit)',19),(20,'XenServer','Oracle Enterprise Linux 5.2 (32-bit)',20),(21,'XenServer','Oracle Enterprise Linux 5.2 (64-bit)',21),(22,'XenServer','Oracle Enterprise Linux 5.3 (32-bit)',22),(23,'XenServer','Oracle Enterprise Linux 5.3 (64-bit)',23),(24,'XenServer','Oracle Enterprise Linux 5.4 (32-bit)',24),(25,'XenServer','Oracle Enterprise Linux 5.4 (64-bit)',25),(26,'XenServer','Red Hat Enterprise Linux 4.5 (32-bit)',26),(27,'XenServer','Red Hat Enterprise Linux 4.6 (32-bit)',27),(28,'XenServer','Red Hat Enterprise Linux 4.7 (32-bit)',28),(29,'XenServer','Red Hat Enterprise Linux 4.8 (32-bit)',29),(30,'XenServer','Red Hat Enterprise Linux 5.0 (32-bit)',30),(31,'XenServer','Red Hat Enterprise Linux 5.0 (64-bit)',31),(32,'XenServer','Red Hat Enterprise Linux 5.1 (32-bit)',32),(33,'XenServer','Red Hat Enterprise Linux 5.1 (64-bit)',33),(34,'XenServer','Red Hat Enterprise Linux 5.2 (32-bit)',34),(35,'XenServer','Red Hat Enterprise Linux 5.2 (64-bit)',35),(36,'XenServer','Red Hat Enterprise Linux 5.3 (32-bit)',36),(37,'XenServer','Red Hat Enterprise Linux 5.3 (64-bit)',37),(38,'XenServer','Red Hat Enterprise Linux 5.4 (32-bit)',38),(39,'XenServer','Red Hat Enterprise Linux 5.4 (64-bit)',39),(40,'XenServer','SUSE Linux Enterprise Server 9 SP4 (32-bit)',40),(41,'XenServer','SUSE Linux Enterprise Server 10 SP1 (32-bit)',41),(42,'XenServer','SUSE Linux Enterprise Server 10 SP1 (64-bit)',42),(43,'XenServer','SUSE Linux Enterprise Server 10 SP2 (32-bit)',43),(44,'XenServer','SUSE Linux Enterprise Server 10 SP2 (64-bit)',44),(45,'XenServer','SUSE Linux Enterprise Server 10 SP3 (64-bit)',45),(46,'XenServer','SUSE Linux Enterprise Server 11 (32-bit)',46),(47,'XenServer','SUSE Linux Enterprise Server 11 (64-bit)',47),(48,'XenServer','Windows 7 (32-bit)',48),(49,'XenServer','Windows 7 (64-bit)',49),(50,'XenServer','Windows Server 2003 (32-bit)',50),(51,'XenServer','Windows Server 2003 (64-bit)',51),(52,'XenServer','Windows Server 2008 (32-bit)',52),(53,'XenServer','Windows Server 2008 (64-bit)',53),(54,'XenServer','Windows Server 2008 R2 (64-bit)',54),(55,'XenServer','Windows 2000 SP4 (32-bit)',55),(56,'XenServer','Windows Vista (32-bit)',56),(57,'XenServer','Windows XP SP2 (32-bit)',57),(58,'XenServer','Windows XP SP3 (32-bit)',58),(59,'XenServer','Other install media',59),(60,'XenServer','Other install media',100),(61,'XenServer','Other install media',60),(62,'XenServer','Other install media',103),(63,'XenServer','Other install media',121),(64,'XenServer','Other install media',126),(65,'XenServer','Other install media',122),(66,'XenServer','Other install media',127),(67,'XenServer','Other install media',123),(68,'XenServer','Other install media',128),(69,'XenServer','Other install media',124),(70,'XenServer','Other install media',129),(71,'XenServer','Other install media',125),(72,'XenServer','Other install media',130),(73,'XenServer','Other PV (32-bit)',139),(74,'XenServer','Other PV (64-bit)',140),(75,'VmWare','Microsoft Windows 7(32-bit)',48),(76,'VmWare','Microsoft Windows 7(64-bit)',49),(77,'VmWare','Microsoft Windows Server 2008 R2(64-bit)',54),(78,'VmWare','Microsoft Windows Server 2008(32-bit)',52),(79,'VmWare','Microsoft Windows Server 2008(64-bit)',53),(80,'VmWare','Microsoft Windows Server 2003, Enterprise Edition (32-bit)',50),(81,'VmWare','Microsoft Windows Server 2003, Enterprise Edition (64-bit)',51),(82,'VmWare','Microsoft Windows Server 2003, Datacenter Edition (32-bit)',87),(83,'VmWare','Microsoft Windows Server 2003, Datacenter Edition (64-bit)',88),(84,'VmWare','Microsoft Windows Server 2003, Standard Edition (32-bit)',89),(85,'VmWare','Microsoft Windows Server 2003, Standard Edition (64-bit)',90),(86,'VmWare','Microsoft Windows Server 2003, Web Edition',91),(87,'VmWare','Microsoft Small Bussiness Server 2003',92),(88,'VmWare','Microsoft Windows Vista (32-bit)',56),(89,'VmWare','Microsoft Windows Vista (64-bit)',101),(90,'VmWare','Microsoft Windows XP Professional (32-bit)',93),(91,'VmWare','Microsoft Windows XP Professional (32-bit)',57),(92,'VmWare','Microsoft Windows XP Professional (32-bit)',58),(93,'VmWare','Microsoft Windows XP Professional (64-bit)',94),(94,'VmWare','Microsoft Windows 2000 Advanced Server',95),(95,'VmWare','Microsoft Windows 2000 Server',61),(96,'VmWare','Microsoft Windows 2000 Professional',105),(97,'VmWare','Microsoft Windows 2000 Server',55),(98,'VmWare','Microsoft Windows 98',62),(99,'VmWare','Microsoft Windows 95',63),(100,'VmWare','Microsoft Windows NT 4',64),(101,'VmWare','Microsoft Windows 3.1',65),(102,'VmWare','Red Hat Enterprise Linux 5.0(32-bit)',30),(103,'VmWare','Red Hat Enterprise Linux 5.1(32-bit)',32),(104,'VmWare','Red Hat Enterprise Linux 5.2(32-bit)',34),(105,'VmWare','Red Hat Enterprise Linux 5.3(32-bit)',36),(106,'VmWare','Red Hat Enterprise Linux 5.4(32-bit)',38),(107,'VmWare','Red Hat Enterprise Linux 5.0(64-bit)',31),(108,'VmWare','Red Hat Enterprise Linux 5.1(64-bit)',33),(109,'VmWare','Red Hat Enterprise Linux 5.2(64-bit)',35),(110,'VmWare','Red Hat Enterprise Linux 5.3(64-bit)',37),(111,'VmWare','Red Hat Enterprise Linux 5.4(64-bit)',39),(112,'VmWare','Red Hat Enterprise Linux 4.5(32-bit)',26),(113,'VmWare','Red Hat Enterprise Linux 4.6(32-bit)',27),(114,'VmWare','Red Hat Enterprise Linux 4.7(32-bit)',28),(115,'VmWare','Red Hat Enterprise Linux 4.8(32-bit)',29),(116,'VmWare','Red Hat Enterprise Linux 4(64-bit)',106),(117,'VmWare','Red Hat Enterprise Linux 3(32-bit)',66),(118,'VmWare','Red Hat Enterprise Linux 3(64-bit)',67),(119,'VmWare','Red Hat Enterprise Linux 2',131),(120,'VmWare','Red Hat Enterprise Linux 6(32-bit)',204),(121,'VmWare','Red Hat Enterprise Linux 6(64-bit)',205),(122,'VmWare','Suse Linux Enterprise 11(32-bit)',46),(123,'VmWare','Suse Linux Enterprise 11(64-bit)',47),(124,'VmWare','Suse Linux Enterprise 10(32-bit)',41),(125,'VmWare','Suse Linux Enterprise 10(32-bit)',43),(126,'VmWare','Suse Linux Enterprise 10(64-bit)',42),(127,'VmWare','Suse Linux Enterprise 10(64-bit)',44),(128,'VmWare','Suse Linux Enterprise 10(64-bit)',45),(129,'VmWare','Suse Linux Enterprise 10(32-bit)',109),(130,'VmWare','Suse Linux Enterprise 10(64-bit)',110),(131,'VmWare','Suse Linux Enterprise 8/9(32-bit)',40),(132,'VmWare','Suse Linux Enterprise 8/9(32-bit)',96),(133,'VmWare','Suse Linux Enterprise 8/9(64-bit)',97),(134,'VmWare','Suse Linux Enterprise 8/9(32-bit)',107),(135,'VmWare','Suse Linux Enterprise 8/9(64-bit)',108),(136,'VmWare','Other Suse Linux Enterprise(32-bit)',202),(137,'VmWare','Other Suse Linux Enterprise(64-bit)',203),(138,'VmWare','Open Enterprise Server',68),(139,'VmWare','Asianux 3(32-bit)',69),(140,'VmWare','Asianux 3(64-bit)',70),(141,'VmWare','Debian GNU/Linux 5(32-bit)',15),(142,'VmWare','Debian GNU/Linux 5(64-bit)',72),(143,'VmWare','Debian GNU/Linux 4(32-bit)',73),(144,'VmWare','Debian GNU/Linux 4(64-bit)',74),(145,'VmWare','Ubuntu 10.04 (32-bit)',121),(146,'VmWare','Ubuntu 9.10 (32-bit)',122),(147,'VmWare','Ubuntu 9.04 (32-bit)',123),(148,'VmWare','Ubuntu 8.10 (32-bit)',124),(149,'VmWare','Ubuntu 8.04 (32-bit)',125),(150,'VmWare','Ubuntu 10.04 (64-bit)',126),(151,'VmWare','Ubuntu 9.10 (64-bit)',127),(152,'VmWare','Ubuntu 9.04 (64-bit)',128),(153,'VmWare','Ubuntu 8.10 (64-bit)',129),(154,'VmWare','Ubuntu 8.04 (64-bit)',130),(155,'VmWare','Ubuntu 10.10 (32-bit)',59),(156,'VmWare','Ubuntu 10.10 (64-bit)',100),(157,'VmWare','Other Ubuntu Linux (32-bit)',59),(158,'VmWare','Other Ubuntu (64-bit)',100),(159,'VmWare','Other 2.6x Linux (32-bit)',75),(160,'VmWare','Other 2.6x Linux (64-bit)',76),(161,'VmWare','Other Linux (32-bit)',98),(162,'VmWare','Other Linux (64-bit)',99),(163,'VmWare','Novell Netware 6.x',77),(164,'VmWare','Novell Netware 5.1',78),(165,'VmWare','Sun Solaris 10(32-bit)',79),(166,'VmWare','Sun Solaris 10(64-bit)',80),(167,'VmWare','Sun Solaris 9(Experimental)',81),(168,'VmWare','Sun Solaris 8(Experimental)',82),(169,'VmWare','FreeBSD (32-bit)',83),(170,'VmWare','FreeBSD (64-bit)',84),(171,'VmWare','OS/2',104),(172,'VmWare','SCO OpenServer 5',85),(173,'VmWare','SCO UnixWare 7',86),(174,'VmWare','DOS',102),(175,'VmWare','Other (32-bit)',60),(176,'VmWare','Other (64-bit)',103),(177,'VmWare','CentOS (32-bit)',200),(178,'VmWare','CentOS (64-bit)',201),(179,'KVM','CentOS 4.5',1),(180,'KVM','CentOS 4.6',2),(181,'KVM','CentOS 4.7',3),(182,'KVM','CentOS 4.8',4),(183,'KVM','CentOS 5.0',5),(184,'KVM','CentOS 5.0',6),(185,'KVM','CentOS 5.1',7),(186,'KVM','CentOS 5.1',8),(187,'KVM','CentOS 5.2',9),(188,'KVM','CentOS 5.2',10),(189,'KVM','CentOS 5.3',11),(190,'KVM','CentOS 5.3',12),(191,'KVM','CentOS 5.4',13),(192,'KVM','CentOS 5.4',14),(193,'KVM','CentOS 5.5',111),(194,'KVM','CentOS 5.5',112),(195,'KVM','Red Hat Enterprise Linux 4.5',26),(196,'KVM','Red Hat Enterprise Linux 4.6',27),(197,'KVM','Red Hat Enterprise Linux 4.7',28),(198,'KVM','Red Hat Enterprise Linux 4.8',29),(199,'KVM','Red Hat Enterprise Linux 5.0',30),(200,'KVM','Red Hat Enterprise Linux 5.0',31),(201,'KVM','Red Hat Enterprise Linux 5.1',32),(202,'KVM','Red Hat Enterprise Linux 5.1',33),(203,'KVM','Red Hat Enterprise Linux 5.2',34),(204,'KVM','Red Hat Enterprise Linux 5.2',35),(205,'KVM','Red Hat Enterprise Linux 5.3',36),(206,'KVM','Red Hat Enterprise Linux 5.3',37),(207,'KVM','Red Hat Enterprise Linux 5.4',38),(208,'KVM','Red Hat Enterprise Linux 5.4',39),(209,'KVM','Red Hat Enterprise Linux 5.5',113),(210,'KVM','Red Hat Enterprise Linux 5.5',114),(211,'KVM','Red Hat Enterprise Linux 4',106),(212,'KVM','Red Hat Enterprise Linux 3',66),(213,'KVM','Red Hat Enterprise Linux 3',67),(214,'KVM','Red Hat Enterprise Linux 2',131),(215,'KVM','Fedora 13',115),(216,'KVM','Fedora 12',116),(217,'KVM','Fedora 11',117),(218,'KVM','Fedora 10',118),(219,'KVM','Fedora 9',119),(220,'KVM','Fedora 8',120),(221,'KVM','Ubuntu 10.04',121),(222,'KVM','Ubuntu 10.04',126),(223,'KVM','Ubuntu 9.10',122),(224,'KVM','Ubuntu 9.10',127),(225,'KVM','Ubuntu 9.04',123),(226,'KVM','Ubuntu 9.04',128),(227,'KVM','Ubuntu 8.10',124),(228,'KVM','Ubuntu 8.10',129),(229,'KVM','Ubuntu 8.04',125),(230,'KVM','Ubuntu 8.04',130),(231,'KVM','Debian GNU/Linux 5',15),(232,'KVM','Debian GNU/Linux 5',72),(233,'KVM','Debian GNU/Linux 4',73),(234,'KVM','Debian GNU/Linux 4',74),(235,'KVM','Other Linux 2.6x',75),(236,'KVM','Other Linux 2.6x',76),(237,'KVM','Other Ubuntu',59),(238,'KVM','Other Ubuntu',100),(239,'KVM','Other Linux',98),(240,'KVM','Other Linux',99),(241,'KVM','Windows 7',48),(242,'KVM','Windows 7',49),(243,'KVM','Windows Server 2003',50),(244,'KVM','Windows Server 2003',51),(245,'KVM','Windows Server 2003',87),(246,'KVM','Windows Server 2003',88),(247,'KVM','Windows Server 2003',89),(248,'KVM','Windows Server 2003',90),(249,'KVM','Windows Server 2003',91),(250,'KVM','Windows Server 2003',92),(251,'KVM','Windows Server 2008',52),(252,'KVM','Windows Server 2008',53),(253,'KVM','Windows 2000',55),(254,'KVM','Windows 2000',61),(255,'KVM','Windows 2000',95),(256,'KVM','Windows 98',62),(257,'KVM','Windows Vista',56),(258,'KVM','Windows Vista',101),(259,'KVM','Windows XP SP2',57),(260,'KVM','Windows XP SP3',58),(261,'KVM','Windows XP ',93),(262,'KVM','Windows XP ',94),(263,'KVM','DOS',102),(264,'KVM','Other',60),(265,'KVM','Other',103);
/*!40000 ALTER TABLE `guest_os_hypervisor` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `host`
--

DROP TABLE IF EXISTS `host`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `host` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `status` varchar(32) NOT NULL,
  `type` varchar(32) NOT NULL,
  `private_ip_address` char(40) NOT NULL,
  `private_netmask` varchar(15) DEFAULT NULL,
  `private_mac_address` varchar(17) DEFAULT NULL,
  `storage_ip_address` char(40) DEFAULT NULL,
  `storage_netmask` varchar(15) DEFAULT NULL,
  `storage_mac_address` varchar(17) DEFAULT NULL,
  `storage_ip_address_2` char(40) DEFAULT NULL,
  `storage_mac_address_2` varchar(17) DEFAULT NULL,
  `storage_netmask_2` varchar(15) DEFAULT NULL,
  `cluster_id` bigint(20) unsigned DEFAULT NULL COMMENT 'foreign key to cluster',
  `public_ip_address` char(40) DEFAULT NULL,
  `public_netmask` varchar(15) DEFAULT NULL,
  `public_mac_address` varchar(17) DEFAULT NULL,
  `proxy_port` int(10) unsigned DEFAULT NULL,
  `data_center_id` bigint(20) unsigned NOT NULL,
  `pod_id` bigint(20) unsigned DEFAULT NULL,
  `cpus` int(10) unsigned DEFAULT NULL,
  `speed` int(10) unsigned DEFAULT NULL,
  `url` varchar(255) DEFAULT NULL COMMENT 'iqn for the servers',
  `fs_type` varchar(32) DEFAULT NULL,
  `hypervisor_type` varchar(32) DEFAULT NULL COMMENT 'hypervisor type, can be NONE for storage',
  `ram` bigint(20) unsigned DEFAULT NULL,
  `resource` varchar(255) DEFAULT NULL COMMENT 'If it is a local resource, this is the class name',
  `version` varchar(40) NOT NULL,
  `parent` varchar(255) DEFAULT NULL COMMENT 'parent path for the storage server',
  `total_size` bigint(20) unsigned DEFAULT NULL COMMENT 'TotalSize',
  `capabilities` varchar(255) DEFAULT NULL COMMENT 'host capabilities in comma separated list',
  `guid` varchar(255) DEFAULT NULL,
  `available` int(1) unsigned NOT NULL DEFAULT '1' COMMENT 'Is this host ready for more resources?',
  `setup` int(1) unsigned NOT NULL DEFAULT '0' COMMENT 'Is this host already setup?',
  `dom0_memory` bigint(20) unsigned NOT NULL COMMENT 'memory used by dom0 for computing and routing servers',
  `last_ping` int(10) unsigned NOT NULL COMMENT 'time in seconds from the start of machine of the last ping',
  `mgmt_server_id` bigint(20) unsigned DEFAULT NULL COMMENT 'ManagementServer this host is connected to.',
  `disconnected` datetime DEFAULT NULL COMMENT 'Time this was disconnected',
  `created` datetime DEFAULT NULL COMMENT 'date the host first signed on',
  `removed` datetime DEFAULT NULL COMMENT 'date removed if not null',
  `allocation_state` varchar(32) NOT NULL DEFAULT 'Enabled' COMMENT 'Is this host enabled for allocation for new resources',
  PRIMARY KEY (`id`),
  UNIQUE KEY `guid` (`guid`),
  KEY `i_host__removed` (`removed`),
  KEY `i_host__last_ping` (`last_ping`),
  KEY `i_host__status` (`status`),
  KEY `i_host__data_center_id` (`data_center_id`),
  KEY `i_host__allocation_state` (`allocation_state`),
  KEY `i_host__pod_id` (`pod_id`),
  KEY `fk_host__cluster_id` (`cluster_id`),
  CONSTRAINT `fk_host__pod_id` FOREIGN KEY (`pod_id`) REFERENCES `host_pod_ref` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_host__cluster_id` FOREIGN KEY (`cluster_id`) REFERENCES `cluster` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `host`
--

LOCK TABLES `host` WRITE;
/*!40000 ALTER TABLE `host` DISABLE KEYS */;
INSERT INTO `host` VALUES (1,'nfs://10.223.110.231/export/home/alena/secondary','Alert','SecondaryStorage','nfs','255.255.254.0','3c:07:54:3f:e4:50','10.216.133.62','255.255.254.0','3c:07:54:3f:e4:50',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,1,NULL,NULL,NULL,'nfs://10.223.110.231/export/home/alena/secondary',NULL,'None',0,NULL,'2.2.8.2012-02-28T19:10:16Z','b65c59d9-5204-353f-b409-ba7af91f7fe6',107374182400,NULL,'nfs://10.223.110.231/export/home/alena/secondary',1,0,0,1299274797,NULL,'2012-02-28 19:29:52','2012-02-28 19:29:52',NULL,'Enabled'),(2,'xenserver-QA-2-1-48','Disconnected','Routing','10.223.48.2','255.255.255.0','bc:30:5b:d4:54:f4','10.223.48.2','255.255.255.0','bc:30:5b:d4:54:f4','10.223.48.2','bc:30:5b:d4:54:f4','255.255.255.0',1,NULL,NULL,NULL,NULL,1,1,4,2260,'iqn.2005-03.org.open-iscsi:51cc05b6b1c',NULL,'XenServer',16190149248,'com.cloud.hypervisor.xen.resource.XenServer56Resource','2.2.8.2012-02-28T19:10:16Z',NULL,NULL,'xen-3.0-x86_64 , xen-3.0-x86_32p , hvm-3.0-x86_32 , hvm-3.0-x86_32p , hvm-3.0-x86_64','ac97f226-f055-42c5-9eca-83c9f3ae7e1d',1,1,0,1299283366,NULL,NULL,'2012-02-28 19:29:53',NULL,'Enabled'),(3,'s-1-TEST','Disconnected','SecondaryStorageVM','10.223.48.115','255.255.255.0','06:d8:c8:00:00:10','10.223.48.115','255.255.255.0','06:d8:c8:00:00:10',NULL,NULL,NULL,NULL,'10.223.120.132','255.255.255.0','06:ed:ec:00:00:86',NULL,1,1,NULL,NULL,'NoIqn',NULL,NULL,0,NULL,'2.2.8.2012-02-28T19:10:16Z',NULL,NULL,NULL,'s-1-TEST-PremiumSecondaryStorageResource',1,0,0,1299283366,NULL,NULL,'2012-02-28 19:33:03',NULL,'Enabled'),(4,'v-2-TEST','Disconnected','ConsoleProxy','10.223.48.167','255.255.255.0','06:69:86:00:00:44','10.223.48.167','255.255.255.0','06:69:86:00:00:44',NULL,NULL,NULL,NULL,'10.223.120.165','255.255.255.0','06:4d:44:00:00:a7',80,1,1,NULL,NULL,'NoIqn',NULL,NULL,0,NULL,'2.2.8.2012-02-28T19:10:16Z',NULL,NULL,NULL,'Proxy.2-ConsoleProxyResource',1,0,0,1299283366,NULL,NULL,'2012-02-28 19:33:45',NULL,'Enabled');
/*!40000 ALTER TABLE `host` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `host_details`
--

DROP TABLE IF EXISTS `host_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `host_details` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `host_id` bigint(20) unsigned NOT NULL COMMENT 'host id',
  `name` varchar(255) NOT NULL,
  `value` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_host_details__host_id` (`host_id`),
  CONSTRAINT `fk_host_details__host_id` FOREIGN KEY (`host_id`) REFERENCES `host` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `host_details`
--

LOCK TABLES `host_details` WRITE;
/*!40000 ALTER TABLE `host_details` DISABLE KEYS */;
INSERT INTO `host_details` VALUES (1,1,'mount.parent','dummy'),(2,1,'mount.path','dummy'),(3,1,'orig.url','nfs://10.223.110.231/export/home/alena/secondary'),(4,2,'public.network.device','Pool-wide network associated with eth0'),(5,2,'product_version','5.6.0'),(6,2,'com.cloud.network.Networks.RouterPrivateIpStrategy','DcGlobal'),(7,2,'private.network.device','Pool-wide network associated with eth0'),(8,2,'Hypervisor.Version','3.4.2'),(9,2,'Host.OS','XenServer'),(10,2,'Host.OS.Kernel.Version','2.6.27.42-0.1.1.xs5.6.0.44.111158xen'),(11,2,'wait','600'),(12,2,'storage.network.device2','cloud-stor2'),(13,2,'password','password'),(14,2,'storage.network.device1','cloud-stor1'),(15,2,'url','10.223.48.2'),(16,2,'username','root'),(17,2,'product_brand','XenServer'),(18,2,'guest.network.device','Pool-wide network associated with eth0'),(19,2,'can_bridge_firewall','false'),(20,2,'Host.OS.Version','5.6.0'),(21,2,'instance.name','TEST');
/*!40000 ALTER TABLE `host_details` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `host_pod_ref`
--

DROP TABLE IF EXISTS `host_pod_ref`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `host_pod_ref` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `data_center_id` bigint(20) unsigned NOT NULL,
  `gateway` varchar(255) NOT NULL COMMENT 'gateway for the pod',
  `cidr_address` varchar(15) NOT NULL COMMENT 'CIDR address for the pod',
  `cidr_size` bigint(20) unsigned NOT NULL COMMENT 'CIDR size for the pod',
  `description` varchar(255) DEFAULT NULL COMMENT 'store private ip range in startIP-endIP format',
  `allocation_state` varchar(32) NOT NULL DEFAULT 'Enabled' COMMENT 'Is this Pod enabled for allocation for new resources',
  `external_dhcp` tinyint(4) NOT NULL DEFAULT '0' COMMENT 'Is this Pod using external DHCP',
  `removed` datetime DEFAULT NULL COMMENT 'date removed if not null',
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`),
  UNIQUE KEY `name` (`name`,`data_center_id`),
  KEY `i_host_pod_ref__data_center_id` (`data_center_id`),
  KEY `i_host_pod_ref__allocation_state` (`allocation_state`),
  KEY `i_host_pod_ref__removed` (`removed`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `host_pod_ref`
--

LOCK TABLES `host_pod_ref` WRITE;
/*!40000 ALTER TABLE `host_pod_ref` DISABLE KEYS */;
INSERT INTO `host_pod_ref` VALUES (1,'POD1',1,'10.223.48.1','10.223.48.0',24,'10.223.48.100-10.223.48.200','Enabled',0,NULL);
/*!40000 ALTER TABLE `host_pod_ref` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `host_tags`
--

DROP TABLE IF EXISTS `host_tags`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `host_tags` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `host_id` bigint(20) unsigned NOT NULL COMMENT 'host id',
  `tag` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_host_tags__host_id` (`host_id`),
  CONSTRAINT `fk_host_tags__host_id` FOREIGN KEY (`host_id`) REFERENCES `host` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `host_tags`
--

LOCK TABLES `host_tags` WRITE;
/*!40000 ALTER TABLE `host_tags` DISABLE KEYS */;
/*!40000 ALTER TABLE `host_tags` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `inline_load_balancer_nic_map`
--

DROP TABLE IF EXISTS `inline_load_balancer_nic_map`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `inline_load_balancer_nic_map` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `load_balancer_id` bigint(20) unsigned NOT NULL,
  `public_ip_address` char(40) NOT NULL,
  `nic_id` bigint(20) unsigned DEFAULT NULL COMMENT 'nic id',
  PRIMARY KEY (`id`),
  UNIQUE KEY `nic_id` (`nic_id`),
  KEY `fk_inline_load_balancer_nic_map__load_balancer_id` (`load_balancer_id`),
  CONSTRAINT `fk_inline_load_balancer_nic_map__load_balancer_id` FOREIGN KEY (`load_balancer_id`) REFERENCES `load_balancing_rules` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_inline_load_balancer_nic_map__nic_id` FOREIGN KEY (`nic_id`) REFERENCES `nics` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `inline_load_balancer_nic_map`
--

LOCK TABLES `inline_load_balancer_nic_map` WRITE;
/*!40000 ALTER TABLE `inline_load_balancer_nic_map` DISABLE KEYS */;
/*!40000 ALTER TABLE `inline_load_balancer_nic_map` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `instance_group`
--

DROP TABLE IF EXISTS `instance_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `instance_group` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `account_id` bigint(20) unsigned NOT NULL COMMENT 'owner.  foreign key to account table',
  `name` varchar(255) NOT NULL,
  `removed` datetime DEFAULT NULL COMMENT 'date the group was removed',
  `created` datetime DEFAULT NULL COMMENT 'date the group was created',
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`),
  KEY `i_instance_group__removed` (`removed`),
  KEY `fk_instance_group__account_id` (`account_id`),
  CONSTRAINT `fk_instance_group__account_id` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `instance_group`
--

LOCK TABLES `instance_group` WRITE;
/*!40000 ALTER TABLE `instance_group` DISABLE KEYS */;
/*!40000 ALTER TABLE `instance_group` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `instance_group_vm_map`
--

DROP TABLE IF EXISTS `instance_group_vm_map`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `instance_group_vm_map` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `group_id` bigint(20) unsigned NOT NULL,
  `instance_id` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_instance_group_vm_map___group_id` (`group_id`),
  KEY `fk_instance_group_vm_map___instance_id` (`instance_id`),
  CONSTRAINT `fk_instance_group_vm_map___instance_id` FOREIGN KEY (`instance_id`) REFERENCES `user_vm` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_instance_group_vm_map___group_id` FOREIGN KEY (`group_id`) REFERENCES `instance_group` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `instance_group_vm_map`
--

LOCK TABLES `instance_group_vm_map` WRITE;
/*!40000 ALTER TABLE `instance_group_vm_map` DISABLE KEYS */;
/*!40000 ALTER TABLE `instance_group_vm_map` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `keystore`
--

DROP TABLE IF EXISTS `keystore`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `keystore` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name` varchar(64) NOT NULL COMMENT 'unique name for the certifiation',
  `certificate` text NOT NULL COMMENT 'the actual certificate being stored in the db',
  `key` text COMMENT 'private key associated wih the certificate',
  `domain_suffix` varchar(256) NOT NULL COMMENT 'DNS domain suffix associated with the certificate',
  `seq` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `keystore`
--

LOCK TABLES `keystore` WRITE;
/*!40000 ALTER TABLE `keystore` DISABLE KEYS */;
INSERT INTO `keystore` VALUES (1,'CPVMCertificate','-----BEGIN CERTIFICATE-----\nMIIFZTCCBE2gAwIBAgIHKBCduBUoKDANBgkqhkiG9w0BAQUFADCByjELMAkGA1UE\nBhMCVVMxEDAOBgNVBAgTB0FyaXpvbmExEzARBgNVBAcTClNjb3R0c2RhbGUxGjAY\nBgNVBAoTEUdvRGFkZHkuY29tLCBJbmMuMTMwMQYDVQQLEypodHRwOi8vY2VydGlm\naWNhdGVzLmdvZGFkZHkuY29tL3JlcG9zaXRvcnkxMDAuBgNVBAMTJ0dvIERhZGR5\nIFNlY3VyZSBDZXJ0aWZpY2F0aW9uIEF1dGhvcml0eTERMA8GA1UEBRMIMDc5Njky\nODcwHhcNMTIwMjAzMDMzMDQwWhcNMTcwMjA3MDUxMTIzWjBZMRkwFwYDVQQKDBAq\nLnJlYWxob3N0aXAuY29tMSEwHwYDVQQLDBhEb21haW4gQ29udHJvbCBWYWxpZGF0\nZWQxGTAXBgNVBAMMECoucmVhbGhvc3RpcC5jb20wggEiMA0GCSqGSIb3DQEBAQUA\nA4IBDwAwggEKAoIBAQCDT9AtEfs+s/I8QXp6rrCw0iNJ0+GgsybNHheU+JpL39LM\nTZykCrZhZnyDvwdxCoOfE38Sa32baHKNds+y2SHnMNsOkw8OcNucHEBX1FIpOBGp\nh9D6xC+umx9od6xMWETUv7j6h2u+WC3OhBM8fHCBqIiAol31/IkcqDxxsHlQ8S/o\nCfTlXJUY6Yn628OA1XijKdRnadV0hZ829cv/PZKljjwQUTyrd0KHQeksBH+YAYSo\n2JUl8ekNLsOi8/cPtfojnltzRI1GXi0ZONs8VnDzJ0a2gqZY+uxlz+CGbLnGnlN4\nj9cBpE+MfUE+35Dq121sTpsSgF85Mz+pVhn2S633AgMBAAGjggG+MIIBujAPBgNV\nHRMBAf8EBTADAQEAMB0GA1UdJQQWMBQGCCsGAQUFBwMBBggrBgEFBQcDAjAOBgNV\nHQ8BAf8EBAMCBaAwMwYDVR0fBCwwKjAooCagJIYiaHR0cDovL2NybC5nb2RhZGR5\nLmNvbS9nZHMxLTY0LmNybDBTBgNVHSAETDBKMEgGC2CGSAGG/W0BBxcBMDkwNwYI\nKwYBBQUHAgEWK2h0dHA6Ly9jZXJ0aWZpY2F0ZXMuZ29kYWRkeS5jb20vcmVwb3Np\ndG9yeS8wgYAGCCsGAQUFBwEBBHQwcjAkBggrBgEFBQcwAYYYaHR0cDovL29jc3Au\nZ29kYWRkeS5jb20vMEoGCCsGAQUFBzAChj5odHRwOi8vY2VydGlmaWNhdGVzLmdv\nZGFkZHkuY29tL3JlcG9zaXRvcnkvZ2RfaW50ZXJtZWRpYXRlLmNydDAfBgNVHSME\nGDAWgBT9rGEyk2xF1uLuhV+auud2mWjM5zArBgNVHREEJDAighAqLnJlYWxob3N0\naXAuY29tgg5yZWFsaG9zdGlwLmNvbTAdBgNVHQ4EFgQUZyJz9/QLy5TWIIscTXID\nE8Xk47YwDQYJKoZIhvcNAQEFBQADggEBAKiUV3KK16mP0NpS92fmQkCLqm+qUWyN\nBfBVgf9/M5pcT8EiTZlS5nAtzAE/eRpBeR3ubLlaAogj4rdH7YYVJcDDLLoB2qM3\nqeCHu8LFoblkb93UuFDWqRaVPmMlJRnhsRkL1oa2gM2hwQTkBDkP7w5FG1BELCgl\ngZI2ij2yxjge6pOEwSyZCzzbCcg9pN+dNrYyGEtB4k+BBnPA3N4r14CWbk+uxjrQ\n6j2Ip+b7wOc5IuMEMl8xwTyjuX3lsLbAZyFI9RCyofwA9NqIZ1GeB6Zd196rubQp\n93cmBqGGjZUs3wMrGlm7xdjlX6GQ9UvmvkMub9+lL99A5W50QgCmFeI=\n-----END CERTIFICATE-----\n','-----BEGIN PRIVATE KEY-----\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCDT9AtEfs+s/I8QXp6rrCw0iNJ\n0+GgsybNHheU+JpL39LMTZykCrZhZnyDvwdxCoOfE38Sa32baHKNds+y2SHnMNsOkw8OcNucHEBX\n1FIpOBGph9D6xC+umx9od6xMWETUv7j6h2u+WC3OhBM8fHCBqIiAol31/IkcqDxxsHlQ8S/oCfTl\nXJUY6Yn628OA1XijKdRnadV0hZ829cv/PZKljjwQUTyrd0KHQeksBH+YAYSo2JUl8ekNLsOi8/cP\ntfojnltzRI1GXi0ZONs8VnDzJ0a2gqZY+uxlz+CGbLnGnlN4j9cBpE+MfUE+35Dq121sTpsSgF85\nMz+pVhn2S633AgMBAAECggEAH/Szd9RxbVADenCA6wxKSa3KErRyq1YN8ksJeCKMAj0FIt0caruE\nqO11DebWW8cwQu1Otl/cYI6pmg24/BBldMrp9IELX/tNJo+lhPpRyGAxxC0eSXinFfoASb8d+jJd\nBd1mmemM6fSxqRlxSP4LrzIhjhR1g2CiyYuTsiM9UtoVKGyHwe7KfFwirUOJo3Mr18zUVNm7YqY4\nIVhOSq59zkH3ULBlYq4bG50jpxa5mNSCZ7IpafPY/kE/CbR+FWNt30+rk69T+qb5abg6+XGm+OAm\nbnQ18yZEqX6nJLk7Ch0cfA5orGgrTMOrM71wK7tBBDQ308kOxDGebx6j0qD36QKBgQDTRDr8kuhA\n9sUyKr9vk2DQCMpNvEeiwI3JRMqmmxpNAtg01aJ3Ya57vX5Fc+zcuV87kP6FM1xgpHQvnw5LWo2J\ns7ANwQcP8ricEW5zkZhSjI4ssMeAubmsHOloGxmLFYZqwx0JI7CWViGTLMcUlqKblmHcjeQDeDfP\nP1TaCItFmwKBgQCfHZwVvIcaDs5vxVpZ4ftvflIrW8qq0uOVK6QIf9A/YTGhCXl2qxxTg2A6+0rg\nZqI7zKzUDxIbVv0KlgCbpHDC9d5+sdtDB3wW2pimuJ3p1z4/RHb4n/lDwXCACZl1S5l24yXX2pFZ\nwdPCXmy5PYkHMssFLNhI24pprUIQs66M1QKBgQDQwjAjWisD3pRXESSfZRsaFkWJcM28hdbVFhPF\nc6gWhwQLmTp0CuL2RPXcPUPFi6sN2iWWi3zxxi9Eyz+9uBn6AsOpo56N5MME/LiOnETO9TKb+Ib6\nrQtKhjshcv3XkIqFPo2XdVvOAgglPO7vajX91iiXXuH7h7RmJud6l0y/lwKBgE+bi90gLuPtpoEr\nVzIDKz40ED5bNYHT80NNy0rpT7J2GVN9nwStRYXPBBVeZq7xCpgqpgmO5LtDAWULeZBlbHlOdBwl\nNhNKKl5wzdEUKwW0yBL1WSS5PQgWPwgARYP25/ggW22sj+49WIo1neXsEKPGWObk8e050f1fTt92\nVo1lAoGAb1gCoyBCzvi7sqFxm4V5oapnJeiQQJFjhoYWqGa26rQ+AvXXNuBcigIeDXNJPctSF0Uc\np11KbbCgiruBbckvM1vGsk6Sx4leRk+IFHRpJktFUek4o0eUg0shOsyyvyet48Dfg0a8FvcxROs0\ngD+IYds5doiob/hcm1hnNB/3vk4=\n-----END PRIVATE KEY-----\n','realhostip.com',NULL),(2,'root','-----BEGIN CERTIFICATE-----\nMIIE3jCCA8agAwIBAgICAwEwDQYJKoZIhvcNAQEFBQAwYzELMAkGA1UEBhMCVVMx\nITAfBgNVBAoTGFRoZSBHbyBEYWRkeSBHcm91cCwgSW5jLjExMC8GA1UECxMoR28g\nRGFkZHkgQ2xhc3MgMiBDZXJ0aWZpY2F0aW9uIEF1dGhvcml0eTAeFw0wNjExMTYw\nMTU0MzdaFw0yNjExMTYwMTU0MzdaMIHKMQswCQYDVQQGEwJVUzEQMA4GA1UECBMH\nQXJpem9uYTETMBEGA1UEBxMKU2NvdHRzZGFsZTEaMBgGA1UEChMRR29EYWRkeS5j\nb20sIEluYy4xMzAxBgNVBAsTKmh0dHA6Ly9jZXJ0aWZpY2F0ZXMuZ29kYWRkeS5j\nb20vcmVwb3NpdG9yeTEwMC4GA1UEAxMnR28gRGFkZHkgU2VjdXJlIENlcnRpZmlj\nYXRpb24gQXV0aG9yaXR5MREwDwYDVQQFEwgwNzk2OTI4NzCCASIwDQYJKoZIhvcN\nAQEBBQADggEPADCCAQoCggEBAMQt1RWMnCZM7DI161+4WQFapmGBWTtwY6vj3D3H\nKrjJM9N55DrtPDAjhI6zMBS2sofDPZVUBJ7fmd0LJR4h3mUpfjWoqVTr9vcyOdQm\nVZWt7/v+WIbXnvQAjYwqDL1CBM6nPwT27oDyqu9SoWlm2r4arV3aLGbqGmu75RpR\nSgAvSMeYddi5Kcju+GZtCpyz8/x4fKL4o/K1w/O5epHBp+YlLpyo7RJlbmr2EkRT\ncDCVw5wrWCs9CHRK8r5RsL+H0EwnWGu1NcWdrxcx+AuP7q2BNgWJCJjPOq8lh8BJ\n6qf9Z/dFjpfMFDniNoW1fho3/Rb2cRGadDAW/hOUoz+EDU8CAwEAAaOCATIwggEu\nMB0GA1UdDgQWBBT9rGEyk2xF1uLuhV+auud2mWjM5zAfBgNVHSMEGDAWgBTSxLDS\nkdRMEXGzYcs9of7dqGrU4zASBgNVHRMBAf8ECDAGAQH/AgEAMDMGCCsGAQUFBwEB\nBCcwJTAjBggrBgEFBQcwAYYXaHR0cDovL29jc3AuZ29kYWRkeS5jb20wRgYDVR0f\nBD8wPTA7oDmgN4Y1aHR0cDovL2NlcnRpZmljYXRlcy5nb2RhZGR5LmNvbS9yZXBv\nc2l0b3J5L2dkcm9vdC5jcmwwSwYDVR0gBEQwQjBABgRVHSAAMDgwNgYIKwYBBQUH\nAgEWKmh0dHA6Ly9jZXJ0aWZpY2F0ZXMuZ29kYWRkeS5jb20vcmVwb3NpdG9yeTAO\nBgNVHQ8BAf8EBAMCAQYwDQYJKoZIhvcNAQEFBQADggEBANKGwOy9+aG2Z+5mC6IG\nOgRQjhVyrEp0lVPLN8tESe8HkGsz2ZbwlFalEzAFPIUyIXvJxwqoJKSQ3kbTJSMU\nA2fCENZvD117esyfxVgqwcSeIaha86ykRvOe5GPLL5CkKSkB2XIsKd83ASe8T+5o\n0yGPwLPk9Qnt0hCqU7S+8MxZC9Y7lhyVJEnfzuz9p0iRFEUOOjZv2kWzRaJBydTX\nRE4+uXR21aITVSzGh6O1mawGhId/dQb8vxRMDsxuxN89txJx9OjxUUAiKEngHUuH\nqDTMBqLdElrRhjZkAzVvb3du6/KFUJheqwNTrZEjYx8WnM25sgVjOuH0aBsXBTWV\nU+4=\n-----END CERTIFICATE-----\n-----BEGIN CERTIFICATE-----\nMIIE+zCCBGSgAwIBAgICAQ0wDQYJKoZIhvcNAQEFBQAwgbsxJDAiBgNVBAcTG1Zh\nbGlDZXJ0IFZhbGlkYXRpb24gTmV0d29yazEXMBUGA1UEChMOVmFsaUNlcnQsIElu\nYy4xNTAzBgNVBAsTLFZhbGlDZXJ0IENsYXNzIDIgUG9saWN5IFZhbGlkYXRpb24g\nQXV0aG9yaXR5MSEwHwYDVQQDExhodHRwOi8vd3d3LnZhbGljZXJ0LmNvbS8xIDAe\nBgkqhkiG9w0BCQEWEWluZm9AdmFsaWNlcnQuY29tMB4XDTA0MDYyOTE3MDYyMFoX\nDTI0MDYyOTE3MDYyMFowYzELMAkGA1UEBhMCVVMxITAfBgNVBAoTGFRoZSBHbyBE\nYWRkeSBHcm91cCwgSW5jLjExMC8GA1UECxMoR28gRGFkZHkgQ2xhc3MgMiBDZXJ0\naWZpY2F0aW9uIEF1dGhvcml0eTCCASAwDQYJKoZIhvcNAQEBBQADggENADCCAQgC\nggEBAN6d1+pXGEmhW+vXX0iG6r7d/+TvZxz0ZWizV3GgXne77ZtJ6XCAPVYYYwhv\n2vLM0D9/AlQiVBDYsoHUwHU9S3/Hd8M+eKsaA7Ugay9qK7HFiH7Eux6wwdhFJ2+q\nN1j3hybX2C32qRe3H3I2TqYXP2WYktsqbl2i/ojgC95/5Y0V4evLOtXiEqITLdiO\nr18SPaAIBQi2XKVlOARFmR6jYGB0xUGlcmIbYsUfb18aQr4CUWWoriMYavx4A6lN\nf4DD+qta/KFApMoZFv6yyO9ecw3ud72a9nmYvLEHZ6IVDd2gWMZEewo+YihfukEH\nU1jPEX44dMX4/7VpkI+EdOqXG68CAQOjggHhMIIB3TAdBgNVHQ4EFgQU0sSw0pHU\nTBFxs2HLPaH+3ahq1OMwgdIGA1UdIwSByjCBx6GBwaSBvjCBuzEkMCIGA1UEBxMb\nVmFsaUNlcnQgVmFsaWRhdGlvbiBOZXR3b3JrMRcwFQYDVQQKEw5WYWxpQ2VydCwg\nSW5jLjE1MDMGA1UECxMsVmFsaUNlcnQgQ2xhc3MgMiBQb2xpY3kgVmFsaWRhdGlv\nbiBBdXRob3JpdHkxITAfBgNVBAMTGGh0dHA6Ly93d3cudmFsaWNlcnQuY29tLzEg\nMB4GCSqGSIb3DQEJARYRaW5mb0B2YWxpY2VydC5jb22CAQEwDwYDVR0TAQH/BAUw\nAwEB/zAzBggrBgEFBQcBAQQnMCUwIwYIKwYBBQUHMAGGF2h0dHA6Ly9vY3NwLmdv\nZGFkZHkuY29tMEQGA1UdHwQ9MDswOaA3oDWGM2h0dHA6Ly9jZXJ0aWZpY2F0ZXMu\nZ29kYWRkeS5jb20vcmVwb3NpdG9yeS9yb290LmNybDBLBgNVHSAERDBCMEAGBFUd\nIAAwODA2BggrBgEFBQcCARYqaHR0cDovL2NlcnRpZmljYXRlcy5nb2RhZGR5LmNv\nbS9yZXBvc2l0b3J5MA4GA1UdDwEB/wQEAwIBBjANBgkqhkiG9w0BAQUFAAOBgQC1\nQPmnHfbq/qQaQlpE9xXUhUaJwL6e4+PrxeNYiY+Sn1eocSxI0YGyeR+sBjUZsE4O\nWBsUs5iB0QQeyAfJg594RAoYC5jcdnplDQ1tgMQLARzLrUc+cb53S8wGd9D0Vmsf\nSxOaFIqII6hR8INMqzW/Rn453HWkrugp++85j09VZw==\n-----END CERTIFICATE-----\n-----BEGIN CERTIFICATE-----\nMIIC5zCCAlACAQEwDQYJKoZIhvcNAQEFBQAwgbsxJDAiBgNVBAcTG1ZhbGlDZXJ0\nIFZhbGlkYXRpb24gTmV0d29yazEXMBUGA1UEChMOVmFsaUNlcnQsIEluYy4xNTAz\nBgNVBAsTLFZhbGlDZXJ0IENsYXNzIDIgUG9saWN5IFZhbGlkYXRpb24gQXV0aG9y\naXR5MSEwHwYDVQQDExhodHRwOi8vd3d3LnZhbGljZXJ0LmNvbS8xIDAeBgkqhkiG\n9w0BCQEWEWluZm9AdmFsaWNlcnQuY29tMB4XDTk5MDYyNjAwMTk1NFoXDTE5MDYy\nNjAwMTk1NFowgbsxJDAiBgNVBAcTG1ZhbGlDZXJ0IFZhbGlkYXRpb24gTmV0d29y\nazEXMBUGA1UEChMOVmFsaUNlcnQsIEluYy4xNTAzBgNVBAsTLFZhbGlDZXJ0IENs\nYXNzIDIgUG9saWN5IFZhbGlkYXRpb24gQXV0aG9yaXR5MSEwHwYDVQQDExhodHRw\nOi8vd3d3LnZhbGljZXJ0LmNvbS8xIDAeBgkqhkiG9w0BCQEWEWluZm9AdmFsaWNl\ncnQuY29tMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDOOnHK5avIWZJV16vY\ndA757tn2VUdZZUcOBVXc65g2PFxTXdMwzzjsvUGJ7SVCCSRrCl6zfN1SLUzm1NZ9\nWlmpZdRJEy0kTRxQb7XBhVQ7/nHk01xC+YDgkRoKWzk2Z/M/VXwbP7RfZHM047QS\nv4dk+NoS/zcnwbNDu+97bi5p9wIDAQABMA0GCSqGSIb3DQEBBQUAA4GBADt/UG9v\nUJSZSWI4OB9L+KXIPqeCgfYrx+jFzug6EILLGACOTb2oWH+heQC1u+mNr0HZDzTu\nIYEZoDJJKPTEjlbVUjP9UNV+mWwD5MlM/Mtsq2azSiGM5bUMMj4QssxsodyamEwC\nW/POuZ6lcg5Ktz885hZo+L7tdEy8W9ViH0Pd\n-----END CERTIFICATE-----\n',NULL,'realhostip.com',0);
/*!40000 ALTER TABLE `keystore` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `launch_permission`
--

DROP TABLE IF EXISTS `launch_permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `launch_permission` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `template_id` bigint(20) unsigned NOT NULL,
  `account_id` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  KEY `i_launch_permission_template_id` (`template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `launch_permission`
--

LOCK TABLES `launch_permission` WRITE;
/*!40000 ALTER TABLE `launch_permission` DISABLE KEYS */;
/*!40000 ALTER TABLE `launch_permission` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `load_balancer_vm_map`
--

DROP TABLE IF EXISTS `load_balancer_vm_map`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `load_balancer_vm_map` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `load_balancer_id` bigint(20) unsigned NOT NULL,
  `instance_id` bigint(20) unsigned NOT NULL,
  `revoke` tinyint(1) unsigned NOT NULL DEFAULT '0' COMMENT '1 is when rule is set for Revoke',
  PRIMARY KEY (`id`),
  UNIQUE KEY `load_balancer_id` (`load_balancer_id`,`instance_id`),
  KEY `fk_load_balancer_vm_map__instance_id` (`instance_id`),
  CONSTRAINT `fk_load_balancer_vm_map__load_balancer_id` FOREIGN KEY (`load_balancer_id`) REFERENCES `load_balancing_rules` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_load_balancer_vm_map__instance_id` FOREIGN KEY (`instance_id`) REFERENCES `vm_instance` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

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
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `load_balancing_rules` (
  `id` bigint(20) unsigned NOT NULL,
  `name` varchar(255) NOT NULL,
  `description` varchar(4096) DEFAULT NULL COMMENT 'description',
  `default_port_start` int(10) NOT NULL COMMENT 'default private port range start',
  `default_port_end` int(10) NOT NULL COMMENT 'default destination port range end',
  `algorithm` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_load_balancing_rules__id` FOREIGN KEY (`id`) REFERENCES `firewall_rules` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

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
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `mshost` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `msid` bigint(20) unsigned NOT NULL COMMENT 'management server id derived from MAC address',
  `runid` bigint(20) NOT NULL DEFAULT '0' COMMENT 'run id, combined with msid to form a cluster session',
  `name` varchar(255) DEFAULT NULL,
  `state` varchar(10) NOT NULL DEFAULT 'Down',
  `version` varchar(255) DEFAULT NULL,
  `service_ip` char(40) NOT NULL,
  `service_port` int(11) NOT NULL,
  `last_update` datetime DEFAULT NULL COMMENT 'Last record update time',
  `removed` datetime DEFAULT NULL COMMENT 'date removed if not null',
  `alert_count` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `msid` (`msid`),
  KEY `i_mshost__removed` (`removed`),
  KEY `i_mshost__last_update` (`last_update`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mshost`
--

LOCK TABLES `mshost` WRITE;
/*!40000 ALTER TABLE `mshost` DISABLE KEYS */;
INSERT INTO `mshost` VALUES (1,1,1330465398083,'macbookpro.citrite.net','Down','2.2.8.2012-02-28T19:10:16Z','127.0.0.1',9090,'2012-02-28 22:06:20',NULL,0);
/*!40000 ALTER TABLE `mshost` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mshost_peer`
--

DROP TABLE IF EXISTS `mshost_peer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `mshost_peer` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `owner_mshost` bigint(20) unsigned NOT NULL,
  `peer_mshost` bigint(20) unsigned NOT NULL,
  `peer_runid` bigint(20) NOT NULL,
  `peer_state` varchar(10) NOT NULL DEFAULT 'Down',
  `last_update` datetime DEFAULT NULL COMMENT 'Last record update time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `i_mshost_peer__owner_peer_runid` (`owner_mshost`,`peer_mshost`,`peer_runid`),
  KEY `fk_mshost_peer__peer_mshost` (`peer_mshost`),
  CONSTRAINT `fk_mshost_peer__owner_mshost` FOREIGN KEY (`owner_mshost`) REFERENCES `mshost` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_mshost_peer__peer_mshost` FOREIGN KEY (`peer_mshost`) REFERENCES `mshost` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mshost_peer`
--

LOCK TABLES `mshost_peer` WRITE;
/*!40000 ALTER TABLE `mshost_peer` DISABLE KEYS */;
INSERT INTO `mshost_peer` VALUES (3,1,1,1330465398083,'Up','2012-02-28 21:43:20');
/*!40000 ALTER TABLE `mshost_peer` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `netapp_lun`
--

DROP TABLE IF EXISTS `netapp_lun`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `netapp_lun` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
  `lun_name` varchar(255) NOT NULL COMMENT 'lun name',
  `target_iqn` varchar(255) NOT NULL COMMENT 'target iqn',
  `path` varchar(255) NOT NULL COMMENT 'lun path',
  `size` bigint(20) NOT NULL COMMENT 'lun size',
  `volume_id` bigint(20) unsigned NOT NULL COMMENT 'parent volume id',
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`),
  KEY `i_netapp_lun__volume_id` (`volume_id`),
  KEY `i_netapp_lun__lun_name` (`lun_name`),
  CONSTRAINT `fk_netapp_lun__volume_id` FOREIGN KEY (`volume_id`) REFERENCES `netapp_volume` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `netapp_lun`
--

LOCK TABLES `netapp_lun` WRITE;
/*!40000 ALTER TABLE `netapp_lun` DISABLE KEYS */;
/*!40000 ALTER TABLE `netapp_lun` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `netapp_pool`
--

DROP TABLE IF EXISTS `netapp_pool`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `netapp_pool` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name` varchar(255) NOT NULL COMMENT 'name for the pool',
  `algorithm` varchar(255) NOT NULL COMMENT 'algorithm',
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `netapp_pool`
--

LOCK TABLES `netapp_pool` WRITE;
/*!40000 ALTER TABLE `netapp_pool` DISABLE KEYS */;
/*!40000 ALTER TABLE `netapp_pool` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `netapp_volume`
--

DROP TABLE IF EXISTS `netapp_volume`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `netapp_volume` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
  `ip_address` varchar(255) NOT NULL COMMENT 'ip address/fqdn of the volume',
  `pool_id` bigint(20) unsigned NOT NULL COMMENT 'id for the pool',
  `pool_name` varchar(255) NOT NULL COMMENT 'name for the pool',
  `aggregate_name` varchar(255) NOT NULL COMMENT 'name for the aggregate',
  `volume_name` varchar(255) NOT NULL COMMENT 'name for the volume',
  `volume_size` varchar(255) NOT NULL COMMENT 'volume size',
  `snapshot_policy` varchar(255) NOT NULL COMMENT 'snapshot policy',
  `snapshot_reservation` int(11) NOT NULL COMMENT 'snapshot reservation',
  `username` varchar(255) NOT NULL COMMENT 'username',
  `password` varchar(200) DEFAULT NULL COMMENT 'password',
  `round_robin_marker` int(11) DEFAULT NULL COMMENT 'This marks the volume to be picked up for lun creation, RR fashion',
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`),
  KEY `i_netapp_volume__pool_id` (`pool_id`),
  CONSTRAINT `fk_netapp_volume__pool_id` FOREIGN KEY (`pool_id`) REFERENCES `netapp_pool` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `netapp_volume`
--

LOCK TABLES `netapp_volume` WRITE;
/*!40000 ALTER TABLE `netapp_volume` DISABLE KEYS */;
/*!40000 ALTER TABLE `netapp_volume` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `network_offerings`
--

DROP TABLE IF EXISTS `network_offerings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `network_offerings` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name` varchar(64) NOT NULL COMMENT 'name of the network offering',
  `unique_name` varchar(64) NOT NULL COMMENT 'unique name of the network offering',
  `display_text` varchar(255) NOT NULL COMMENT 'text to display to users',
  `nw_rate` smallint(5) unsigned DEFAULT NULL COMMENT 'network rate throttle mbits/s',
  `mc_rate` smallint(5) unsigned DEFAULT NULL COMMENT 'mcast rate throttle mbits/s',
  `concurrent_connections` int(10) unsigned DEFAULT NULL COMMENT 'concurrent connections supported on this network',
  `traffic_type` varchar(32) NOT NULL COMMENT 'traffic type carried on this network',
  `tags` varchar(4096) DEFAULT NULL COMMENT 'tags supported by this offering',
  `system_only` int(1) unsigned NOT NULL DEFAULT '0' COMMENT 'Is this network offering for system use only',
  `specify_vlan` int(1) unsigned NOT NULL DEFAULT '0' COMMENT 'Should the user specify vlan',
  `service_offering_id` bigint(20) unsigned DEFAULT NULL COMMENT 'service offering id that this network offering is tied to',
  `created` datetime NOT NULL COMMENT 'time the entry was created',
  `removed` datetime DEFAULT NULL COMMENT 'time the entry was removed',
  `default` int(1) unsigned NOT NULL DEFAULT '0' COMMENT '1 if network offering is default',
  `availability` varchar(255) NOT NULL COMMENT 'availability of the network',
  `dns_service` int(1) unsigned NOT NULL DEFAULT '0' COMMENT 'true if network offering provides dns service',
  `gateway_service` int(1) unsigned NOT NULL DEFAULT '0' COMMENT 'true if network offering provides gateway service',
  `firewall_service` int(1) unsigned NOT NULL DEFAULT '0' COMMENT 'true if network offering provides firewall service',
  `lb_service` int(1) unsigned NOT NULL DEFAULT '0' COMMENT 'true if network offering provides lb service',
  `userdata_service` int(1) unsigned NOT NULL DEFAULT '0' COMMENT 'true if network offering provides user data service',
  `vpn_service` int(1) unsigned NOT NULL DEFAULT '0' COMMENT 'true if network offering provides vpn service',
  `dhcp_service` int(1) unsigned NOT NULL DEFAULT '0' COMMENT 'true if network offering provides dhcp service',
  `shared_source_nat_service` int(1) unsigned NOT NULL DEFAULT '0' COMMENT 'true if the network offering provides the shared source nat service',
  `guest_type` char(32) DEFAULT NULL COMMENT 'guest ip type of network offering',
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`),
  UNIQUE KEY `unique_name` (`unique_name`),
  UNIQUE KEY `service_offering_id` (`service_offering_id`),
  KEY `i_network_offerings__system_only` (`system_only`),
  KEY `i_network_offerings__removed` (`removed`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `network_offerings`
--

LOCK TABLES `network_offerings` WRITE;
/*!40000 ALTER TABLE `network_offerings` DISABLE KEYS */;
INSERT INTO `network_offerings` VALUES (1,'System-Public-Network','System-Public-Network','System Offering for System-Public-Network',0,0,NULL,'Public',NULL,1,0,NULL,'2012-02-28 19:29:19',NULL,1,'Required',0,0,0,0,0,0,0,0,NULL),(2,'System-Management-Network','System-Management-Network','System Offering for System-Management-Network',0,0,NULL,'Management',NULL,1,0,NULL,'2012-02-28 19:29:19',NULL,1,'Required',0,0,0,0,0,0,0,0,NULL),(3,'System-Control-Network','System-Control-Network','System Offering for System-Control-Network',0,0,NULL,'Control',NULL,1,0,NULL,'2012-02-28 19:29:19',NULL,1,'Required',0,0,0,0,0,0,0,0,NULL),(4,'System-Storage-Network','System-Storage-Network','System Offering for System-Storage-Network',0,0,NULL,'Storage',NULL,1,0,NULL,'2012-02-28 19:29:19',NULL,1,'Required',0,0,0,0,0,0,0,0,NULL),(5,'System-Guest-Network','System-Guest-Network','System-Guest-Network',NULL,NULL,NULL,'Guest',NULL,1,0,NULL,'2012-02-28 19:29:19',NULL,1,'Required',1,0,1,1,1,0,1,0,'Direct'),(6,'DefaultVirtualizedNetworkOffering','DefaultVirtualizedNetworkOffering','Virtual Vlan',NULL,NULL,NULL,'Guest',NULL,0,0,NULL,'2012-02-28 19:29:19',NULL,1,'Required',1,1,1,1,1,1,1,0,'Virtual'),(7,'DefaultDirectNetworkOffering','DefaultDirectNetworkOffering','Direct',NULL,NULL,NULL,'Guest',NULL,0,1,NULL,'2012-02-28 19:29:19',NULL,1,'Optional',1,0,0,1,1,0,1,0,'Direct');
/*!40000 ALTER TABLE `network_offerings` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `network_rule_config`
--

DROP TABLE IF EXISTS `network_rule_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `network_rule_config` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `security_group_id` bigint(20) unsigned NOT NULL,
  `public_port` varchar(10) DEFAULT NULL,
  `private_port` varchar(10) DEFAULT NULL,
  `protocol` varchar(16) NOT NULL DEFAULT 'TCP',
  `create_status` varchar(32) DEFAULT NULL COMMENT 'rule creation status',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `network_rule_config`
--

LOCK TABLES `network_rule_config` WRITE;
/*!40000 ALTER TABLE `network_rule_config` DISABLE KEYS */;
/*!40000 ALTER TABLE `network_rule_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `network_tags`
--

DROP TABLE IF EXISTS `network_tags`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `network_tags` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
  `network_id` bigint(20) unsigned NOT NULL COMMENT 'id of the network',
  `tag` varchar(255) NOT NULL COMMENT 'tag',
  PRIMARY KEY (`id`),
  UNIQUE KEY `network_id` (`network_id`,`tag`),
  CONSTRAINT `fk_network_tags__network_id` FOREIGN KEY (`network_id`) REFERENCES `networks` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `network_tags`
--

LOCK TABLES `network_tags` WRITE;
/*!40000 ALTER TABLE `network_tags` DISABLE KEYS */;
/*!40000 ALTER TABLE `network_tags` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `networks`
--

DROP TABLE IF EXISTS `networks`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `networks` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name` varchar(255) DEFAULT NULL COMMENT 'name for this network',
  `display_text` varchar(255) DEFAULT NULL COMMENT 'display text for this network',
  `traffic_type` varchar(32) NOT NULL COMMENT 'type of traffic going through this network',
  `broadcast_domain_type` varchar(32) NOT NULL COMMENT 'type of broadcast domain used',
  `broadcast_uri` varchar(255) DEFAULT NULL COMMENT 'broadcast domain specifier',
  `gateway` varchar(15) DEFAULT NULL COMMENT 'gateway for this network configuration',
  `cidr` varchar(18) DEFAULT NULL COMMENT 'network cidr',
  `mode` varchar(32) DEFAULT NULL COMMENT 'How to retrieve ip address in this network',
  `network_offering_id` bigint(20) unsigned NOT NULL COMMENT 'network offering id that this configuration is created from',
  `data_center_id` bigint(20) unsigned NOT NULL COMMENT 'data center id that this configuration is used in',
  `guru_name` varchar(255) NOT NULL COMMENT 'who is responsible for this type of network configuration',
  `state` varchar(32) NOT NULL COMMENT 'what state is this configuration in',
  `related` bigint(20) unsigned NOT NULL COMMENT 'related to what other network configuration',
  `domain_id` bigint(20) unsigned NOT NULL COMMENT 'foreign key to domain id',
  `account_id` bigint(20) unsigned NOT NULL COMMENT 'owner of this network',
  `dns1` varchar(255) DEFAULT NULL COMMENT 'comma separated DNS list',
  `dns2` varchar(255) DEFAULT NULL COMMENT 'comma separated DNS list',
  `guru_data` varchar(1024) DEFAULT NULL COMMENT 'data stored by the network guru that setup this network',
  `set_fields` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT 'which fields are set already',
  `guest_type` char(32) DEFAULT NULL COMMENT 'type of guest network',
  `shared` int(1) unsigned NOT NULL DEFAULT '0' COMMENT '0 if network is shared, 1 if network dedicated',
  `is_domain_specific` int(1) unsigned NOT NULL DEFAULT '0' COMMENT '1 if network is domain specific, 0 false otherwise',
  `network_domain` varchar(255) DEFAULT NULL COMMENT 'domain',
  `reservation_id` char(40) DEFAULT NULL COMMENT 'reservation id',
  `is_default` int(1) unsigned NOT NULL DEFAULT '0' COMMENT '1 if network is default',
  `created` datetime NOT NULL COMMENT 'date created',
  `removed` datetime DEFAULT NULL COMMENT 'date removed if not null',
  `is_security_group_enabled` tinyint(4) NOT NULL DEFAULT '0' COMMENT '1: enabled, 0: not',
  PRIMARY KEY (`id`),
  KEY `fk_networks__network_offering_id` (`network_offering_id`),
  KEY `fk_networks__data_center_id` (`data_center_id`),
  KEY `fk_networks__related` (`related`),
  KEY `fk_networks__account_id` (`account_id`),
  KEY `fk_networks__domain_id` (`domain_id`),
  KEY `i_networks__removed` (`removed`),
  CONSTRAINT `fk_networks__network_offering_id` FOREIGN KEY (`network_offering_id`) REFERENCES `network_offerings` (`id`),
  CONSTRAINT `fk_networks__data_center_id` FOREIGN KEY (`data_center_id`) REFERENCES `data_center` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_networks__related` FOREIGN KEY (`related`) REFERENCES `networks` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_networks__account_id` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`),
  CONSTRAINT `fk_networks__domain_id` FOREIGN KEY (`domain_id`) REFERENCES `domain` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=210 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `networks`
--

LOCK TABLES `networks` WRITE;
/*!40000 ALTER TABLE `networks` DISABLE KEYS */;
INSERT INTO `networks` VALUES (200,NULL,NULL,'Public','Vlan',NULL,NULL,NULL,'Static',1,1,'PublicNetworkGuru','Setup',200,1,1,NULL,NULL,NULL,0,NULL,1,0,NULL,NULL,0,'2012-02-28 19:29:51',NULL,0),(201,NULL,NULL,'Management','Native',NULL,NULL,NULL,'Static',2,1,'PodBasedNetworkGuru','Setup',201,1,1,NULL,NULL,NULL,0,NULL,1,0,NULL,NULL,0,'2012-02-28 19:29:51',NULL,0),(202,NULL,NULL,'Control','LinkLocal',NULL,'169.254.0.1','169.254.0.0/16','Static',3,1,'ControlNetworkGuru','Setup',202,1,1,NULL,NULL,NULL,0,NULL,1,0,NULL,NULL,0,'2012-02-28 19:29:51',NULL,0),(203,NULL,NULL,'Storage','Native',NULL,NULL,NULL,'Static',4,1,'PodBasedNetworkGuru','Setup',203,1,1,NULL,NULL,NULL,0,NULL,1,0,NULL,NULL,0,'2012-02-28 19:29:51',NULL,0),(204,'accountSpecific','accountSpecific','Guest','Vlan','vlan://20','172.20.0.1','172.20.0.0/16','Dhcp',7,1,'DirectNetworkGuru','Setup',204,1,2,NULL,NULL,NULL,0,'Direct',0,0,'cs2qatest-vmops.com',NULL,0,'2012-02-28 21:39:19',NULL,0),(205,'domainSpecific','domainSpecific','Guest','Vlan','vlan://21','172.21.0.1','172.21.0.0/16','Dhcp',7,1,'DirectNetworkGuru','Setup',205,1,1,NULL,NULL,NULL,0,'Direct',1,1,'cs1qatest-vmops.com',NULL,0,'2012-02-28 21:40:00',NULL,0),(206,'zoneWide','zoneWide','Guest','Vlan','vlan://22','172.22.0.1','172.22.0.0/16','Dhcp',7,1,'DirectNetworkGuru','Setup',206,1,1,NULL,NULL,NULL,0,'Direct',1,0,'cs1qatest-vmops.com',NULL,0,'2012-02-28 21:40:25',NULL,0),(207,'Virtual Network','A dedicated virtualized network for your account.  The broadcast domain is contained within a VLAN and all public network access is routed out by a virtual router.','Guest','Vlan','vlan://1024','10.10.10.1','10.10.10.0/24','Dhcp',6,1,'ExternalGuestNetworkGuru','Implemented',207,1,2,NULL,NULL,NULL,0,'Virtual',0,0,'cs2qatest-vmops.com','cd4b11ce-ae75-44c7-89ad-609c76ac0238',1,'2012-02-28 21:40:31',NULL,0),(208,'1s','1s','Guest','Vlan','vlan://23','172.23.0.1','172.23.0.0/16','Dhcp',7,1,'DirectNetworkGuru','Setup',208,1,1,NULL,NULL,NULL,0,'Direct',1,1,'cs1qatest-vmops.com',NULL,0,'2012-02-28 21:47:06',NULL,0),(209,'Virtual Network','A dedicated virtualized network for your account.  The broadcast domain is contained within a VLAN and all public network access is routed out by a virtual router.','Guest','Vlan','vlan://1028','10.10.10.1','10.10.10.0/24','Dhcp',6,1,'ExternalGuestNetworkGuru','Implemented',209,2,3,NULL,NULL,NULL,0,'Virtual',0,0,'cs3qatest-vmops.com','60fd9627-2eeb-4d6d-b9e2-f5a4c0808979',1,'2012-02-28 21:47:17',NULL,0);
/*!40000 ALTER TABLE `networks` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `nics`
--

DROP TABLE IF EXISTS `nics`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `nics` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
  `instance_id` bigint(20) unsigned DEFAULT NULL COMMENT 'vm instance id',
  `mac_address` varchar(17) DEFAULT NULL COMMENT 'mac address',
  `ip4_address` char(40) DEFAULT NULL COMMENT 'ip4 address',
  `netmask` varchar(15) DEFAULT NULL COMMENT 'netmask for ip4 address',
  `gateway` varchar(15) DEFAULT NULL COMMENT 'gateway',
  `ip_type` varchar(32) DEFAULT NULL COMMENT 'type of ip',
  `broadcast_uri` varchar(255) DEFAULT NULL COMMENT 'broadcast uri',
  `network_id` bigint(20) unsigned NOT NULL COMMENT 'network configuration id',
  `mode` varchar(32) DEFAULT NULL COMMENT 'mode of getting ip address',
  `state` varchar(32) NOT NULL COMMENT 'state of the creation',
  `strategy` varchar(32) NOT NULL COMMENT 'reservation strategy',
  `reserver_name` varchar(255) DEFAULT NULL COMMENT 'Name of the component that reserved the ip address',
  `reservation_id` varchar(64) DEFAULT NULL COMMENT 'id for the reservation',
  `device_id` int(10) DEFAULT NULL COMMENT 'device id for the network when plugged into the virtual machine',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'time the state was changed',
  `isolation_uri` varchar(255) DEFAULT NULL COMMENT 'id for isolation',
  `ip6_address` char(40) DEFAULT NULL COMMENT 'ip6 address',
  `default_nic` tinyint(4) NOT NULL COMMENT 'None',
  `vm_type` varchar(32) DEFAULT NULL COMMENT 'type of vm: System or User vm',
  `created` datetime NOT NULL COMMENT 'date created',
  `removed` datetime DEFAULT NULL COMMENT 'date removed if not null',
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`),
  KEY `fk_nics__instance_id` (`instance_id`),
  KEY `fk_nics__networks_id` (`network_id`),
  KEY `i_nics__removed` (`removed`),
  CONSTRAINT `fk_nics__instance_id` FOREIGN KEY (`instance_id`) REFERENCES `vm_instance` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_nics__networks_id` FOREIGN KEY (`network_id`) REFERENCES `networks` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=36 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `nics`
--

LOCK TABLES `nics` WRITE;
/*!40000 ALTER TABLE `nics` DISABLE KEYS */;
INSERT INTO `nics` VALUES (1,1,'06:ed:ec:00:00:86','10.223.120.132','255.255.255.0','10.223.120.1','Ip4','vlan://196',200,'Static','Reserved','Create','PublicNetworkGuru',NULL,2,'2012-02-28 19:30:42','vlan://196',NULL,1,'SecondaryStorageVm','2012-02-28 19:30:42',NULL),(2,1,'0e:00:a9:fe:00:d6','169.254.0.214','255.255.0.0','169.254.0.1','Ip4',NULL,202,'Static','Reserved','Start','ControlNetworkGuru','c178233a-17c2-441a-98bf-a5591b3eaea7',0,'2012-02-28 19:30:42',NULL,NULL,0,'SecondaryStorageVm','2012-02-28 19:30:42',NULL),(3,1,'06:d8:c8:00:00:10','10.223.48.115','255.255.255.0','10.223.48.1','Ip4',NULL,201,'Static','Reserved','Start','PodBasedNetworkGuru','c178233a-17c2-441a-98bf-a5591b3eaea7',1,'2012-02-28 19:30:42',NULL,NULL,0,'SecondaryStorageVm','2012-02-28 19:30:42',NULL),(4,2,'06:4d:44:00:00:a7','10.223.120.165','255.255.255.0','10.223.120.1','Ip4','vlan://196',200,'Static','Reserved','Create','PublicNetworkGuru',NULL,2,'2012-02-28 19:30:42','vlan://196',NULL,1,'ConsoleProxy','2012-02-28 19:30:42',NULL),(5,2,'0e:00:a9:fe:00:90','169.254.0.144','255.255.0.0','169.254.0.1','Ip4',NULL,202,'Static','Reserved','Start','ControlNetworkGuru','b9e363d4-075f-46b3-a226-49ad82a3d56e',0,'2012-02-28 19:30:42',NULL,NULL,0,'ConsoleProxy','2012-02-28 19:30:42',NULL),(6,2,'06:69:86:00:00:44','10.223.48.167','255.255.255.0','10.223.48.1','Ip4',NULL,201,'Static','Reserved','Start','PodBasedNetworkGuru','b9e363d4-075f-46b3-a226-49ad82a3d56e',1,'2012-02-28 19:30:42',NULL,NULL,0,'ConsoleProxy','2012-02-28 19:30:42',NULL),(13,6,'02:00:76:79:00:04','10.10.10.84','255.255.255.0','10.10.10.1','Ip4','vlan://1024',207,'Dhcp','Reserved','Start','ExternalGuestNetworkGuru','cd4b11ce-ae75-44c7-89ad-609c76ac0238',0,'2012-02-28 21:45:00','vlan://1024',NULL,1,'User','2012-02-28 21:44:11',NULL),(14,7,'06:c0:dc:00:00:66','10.223.120.100','255.255.255.0','10.223.120.1','Ip4','vlan://196',200,'Static','Reserved','Managed','PublicNetworkGuru',NULL,2,'2012-02-28 21:44:12','vlan://196',NULL,1,'DomainRouter','2012-02-28 21:44:12',NULL),(15,7,'02:00:22:be:00:05','10.10.10.1','255.255.255.0',NULL,'Ip4','vlan://1024',207,'Dhcp','Reserved','Start','ExternalGuestNetworkGuru','63fe2b29-2c2d-4299-b4bd-f1c2929d6e72',0,'2012-02-28 21:44:12','vlan://1024',NULL,0,'DomainRouter','2012-02-28 21:44:12',NULL),(16,7,'0e:00:a9:fe:01:5c','169.254.1.92','255.255.0.0','169.254.0.1','Ip4',NULL,202,'Static','Reserved','Start','ControlNetworkGuru','63fe2b29-2c2d-4299-b4bd-f1c2929d6e72',1,'2012-02-28 21:44:12',NULL,NULL,0,'DomainRouter','2012-02-28 21:44:12',NULL),(17,8,'02:00:4e:da:00:06','10.10.10.192','255.255.255.0','10.10.10.1','Ip4','vlan://1024',207,'Dhcp','Reserved','Start','ExternalGuestNetworkGuru','fabed3ae-abfc-46d9-a1f1-e435f3afaa70',0,'2012-02-28 21:45:04','vlan://1024',NULL,1,'User','2012-02-28 21:44:33',NULL),(18,8,'06:da:96:00:00:e0','172.21.0.100','255.255.0.0','172.21.0.1','Ip4','vlan://21',205,'Dhcp','Reserved','Create','DirectNetworkGuru',NULL,1,'2012-02-28 21:45:22','vlan://21',NULL,0,'User','2012-02-28 21:44:33',NULL),(19,8,'06:41:a2:00:00:f5','172.22.0.100','255.255.0.0','172.22.0.1','Ip4','vlan://22',206,'Dhcp','Reserved','Create','DirectNetworkGuru',NULL,2,'2012-02-28 21:47:29','vlan://22',NULL,0,'User','2012-02-28 21:44:33',NULL),(20,8,'06:30:16:00:00:cb','172.20.0.100','255.255.0.0','172.20.0.1','Ip4','vlan://20',204,'Dhcp','Reserved','Create','DirectNetworkGuru',NULL,3,'2012-02-28 21:49:07','vlan://20',NULL,0,'User','2012-02-28 21:44:33',NULL),(21,9,'06:6c:cc:00:00:e1','172.21.0.101','255.255.0.0','172.21.0.1','Ip4','vlan://21',205,'Dhcp','Reserved','Create','DirectNetworkGuru',NULL,0,'2012-02-28 21:45:22','vlan://21',NULL,1,'DomainRouter','2012-02-28 21:45:22',NULL),(22,9,'0e:00:a9:fe:02:7d','169.254.2.125','255.255.0.0','169.254.0.1','Ip4',NULL,202,'Static','Reserved','Start','ControlNetworkGuru','a88f9e42-6bc0-4bb5-860b-e36a9c57f920',1,'2012-02-28 21:45:22',NULL,NULL,0,'DomainRouter','2012-02-28 21:45:22',NULL),(23,10,'02:00:3e:ed:00:01','10.10.10.21','255.255.255.0','10.10.10.1','Ip4','vlan://1028',209,'Dhcp','Reserved','Start','ExternalGuestNetworkGuru','60fd9627-2eeb-4d6d-b9e2-f5a4c0808979',0,'2012-02-28 21:48:18','vlan://1028',NULL,1,'User','2012-02-28 21:47:26',NULL),(24,10,'06:2f:4e:00:01:0a','172.23.0.100','255.255.0.0','172.23.0.1','Ip4','vlan://23',208,'Dhcp','Reserved','Create','DirectNetworkGuru',NULL,1,'2012-02-28 21:48:56','vlan://23',NULL,0,'User','2012-02-28 21:47:26',NULL),(25,10,'06:80:8e:00:00:e2','172.21.0.102','255.255.0.0','172.21.0.1','Ip4','vlan://21',205,'Dhcp','Reserved','Create','DirectNetworkGuru',NULL,2,'2012-02-28 21:50:57','vlan://21',NULL,0,'User','2012-02-28 21:47:26',NULL),(26,10,'06:13:f4:00:00:f6','172.22.0.101','255.255.0.0','172.22.0.1','Ip4','vlan://22',206,'Dhcp','Reserved','Create','DirectNetworkGuru',NULL,3,'2012-02-28 21:51:18','vlan://22',NULL,0,'User','2012-02-28 21:47:26',NULL),(27,11,'06:d3:bc:00:00:c9','10.223.120.199','255.255.255.0','10.223.120.1','Ip4','vlan://196',200,'Static','Reserved','Managed','PublicNetworkGuru',NULL,2,'2012-02-28 21:47:26','vlan://196',NULL,1,'DomainRouter','2012-02-28 21:47:26',NULL),(28,11,'02:00:3b:83:00:02','10.10.10.1','255.255.255.0',NULL,'Ip4','vlan://1028',209,'Dhcp','Reserved','Start','ExternalGuestNetworkGuru','6eae5655-0926-40f0-9919-8a97d820781e',0,'2012-02-28 21:47:26','vlan://1028',NULL,0,'DomainRouter','2012-02-28 21:47:26',NULL),(29,11,'0e:00:a9:fe:02:f3','169.254.2.243','255.255.0.0','169.254.0.1','Ip4',NULL,202,'Static','Reserved','Start','ControlNetworkGuru','6eae5655-0926-40f0-9919-8a97d820781e',1,'2012-02-28 21:47:26',NULL,NULL,0,'DomainRouter','2012-02-28 21:47:26',NULL),(30,12,'06:76:f8:00:00:f7','172.22.0.102','255.255.0.0','172.22.0.1','Ip4','vlan://22',206,'Dhcp','Reserved','Create','DirectNetworkGuru',NULL,0,'2012-02-28 21:47:29','vlan://22',NULL,1,'DomainRouter','2012-02-28 21:47:29',NULL),(31,12,'0e:00:a9:fe:02:e2','169.254.2.226','255.255.0.0','169.254.0.1','Ip4',NULL,202,'Static','Reserved','Start','ControlNetworkGuru','10da1e5c-4382-4dbc-89a8-c80dda46bae7',1,'2012-02-28 21:47:29',NULL,NULL,0,'DomainRouter','2012-02-28 21:47:29',NULL),(32,13,'06:b4:80:00:01:0b','172.23.0.101','255.255.0.0','172.23.0.1','Ip4','vlan://23',208,'Dhcp','Reserved','Create','DirectNetworkGuru',NULL,0,'2012-02-28 21:48:57','vlan://23',NULL,1,'DomainRouter','2012-02-28 21:48:57',NULL),(33,13,'0e:00:a9:fe:02:a2','169.254.2.162','255.255.0.0','169.254.0.1','Ip4',NULL,202,'Static','Reserved','Start','ControlNetworkGuru','958b5026-95f2-4f3a-8482-93246756bd89',1,'2012-02-28 21:48:57',NULL,NULL,0,'DomainRouter','2012-02-28 21:48:57',NULL),(34,14,'06:f9:ee:00:00:cc','172.20.0.101','255.255.0.0','172.20.0.1','Ip4','vlan://20',204,'Dhcp','Reserved','Create','DirectNetworkGuru',NULL,0,'2012-02-28 21:49:07','vlan://20',NULL,1,'DomainRouter','2012-02-28 21:49:07',NULL),(35,14,'0e:00:a9:fe:03:d3','169.254.3.211','255.255.0.0','169.254.0.1','Ip4',NULL,202,'Static','Reserved','Start','ControlNetworkGuru','4157583e-79b2-48f0-be5d-6aad346c30b8',1,'2012-02-28 21:49:07',NULL,NULL,0,'DomainRouter','2012-02-28 21:49:07',NULL);
/*!40000 ALTER TABLE `nics` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `op_dc_ip_address_alloc`
--

DROP TABLE IF EXISTS `op_dc_ip_address_alloc`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `op_dc_ip_address_alloc` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `ip_address` char(40) NOT NULL COMMENT 'ip address',
  `data_center_id` bigint(20) unsigned NOT NULL COMMENT 'data center it belongs to',
  `pod_id` bigint(20) unsigned NOT NULL COMMENT 'pod it belongs to',
  `nic_id` bigint(20) unsigned DEFAULT NULL COMMENT 'nic id',
  `reservation_id` char(40) DEFAULT NULL COMMENT 'reservation id',
  `taken` datetime DEFAULT NULL COMMENT 'Date taken',
  `mac_address` bigint(20) unsigned NOT NULL COMMENT 'mac address for management ips',
  PRIMARY KEY (`id`),
  UNIQUE KEY `i_op_dc_ip_address_alloc__ip_address__data_center_id` (`ip_address`,`data_center_id`),
  KEY `fk_op_dc_ip_address_alloc__data_center_id` (`data_center_id`),
  KEY `i_op_dc_ip_address_alloc__pod_id__data_center_id__taken` (`pod_id`,`data_center_id`,`taken`,`nic_id`),
  KEY `i_op_dc_ip_address_alloc__pod_id` (`pod_id`),
  CONSTRAINT `fk_op_dc_ip_address_alloc__data_center_id` FOREIGN KEY (`data_center_id`) REFERENCES `data_center` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_op_dc_ip_address_alloc__pod_id` FOREIGN KEY (`pod_id`) REFERENCES `host_pod_ref` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=102 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `op_dc_ip_address_alloc`
--

LOCK TABLES `op_dc_ip_address_alloc` WRITE;
/*!40000 ALTER TABLE `op_dc_ip_address_alloc` DISABLE KEYS */;
INSERT INTO `op_dc_ip_address_alloc` VALUES (1,'10.223.48.100',1,1,NULL,NULL,NULL,1),(2,'10.223.48.101',1,1,NULL,NULL,NULL,2),(3,'10.223.48.102',1,1,NULL,NULL,NULL,3),(4,'10.223.48.103',1,1,NULL,NULL,NULL,4),(5,'10.223.48.104',1,1,NULL,NULL,NULL,5),(6,'10.223.48.105',1,1,NULL,NULL,NULL,6),(7,'10.223.48.106',1,1,NULL,NULL,NULL,7),(8,'10.223.48.107',1,1,NULL,NULL,NULL,8),(9,'10.223.48.108',1,1,NULL,NULL,NULL,9),(10,'10.223.48.109',1,1,NULL,NULL,NULL,10),(11,'10.223.48.110',1,1,NULL,NULL,NULL,11),(12,'10.223.48.111',1,1,NULL,NULL,NULL,12),(13,'10.223.48.112',1,1,NULL,NULL,NULL,13),(14,'10.223.48.113',1,1,NULL,NULL,NULL,14),(15,'10.223.48.114',1,1,NULL,NULL,NULL,15),(16,'10.223.48.115',1,1,3,'c178233a-17c2-441a-98bf-a5591b3eaea7','2012-02-28 19:30:42',16),(17,'10.223.48.116',1,1,NULL,NULL,NULL,17),(18,'10.223.48.117',1,1,NULL,NULL,NULL,18),(19,'10.223.48.118',1,1,NULL,NULL,NULL,19),(20,'10.223.48.119',1,1,NULL,NULL,NULL,20),(21,'10.223.48.120',1,1,NULL,NULL,NULL,21),(22,'10.223.48.121',1,1,NULL,NULL,NULL,22),(23,'10.223.48.122',1,1,NULL,NULL,NULL,23),(24,'10.223.48.123',1,1,NULL,NULL,NULL,24),(25,'10.223.48.124',1,1,NULL,NULL,NULL,25),(26,'10.223.48.125',1,1,NULL,NULL,NULL,26),(27,'10.223.48.126',1,1,NULL,NULL,NULL,27),(28,'10.223.48.127',1,1,NULL,NULL,NULL,28),(29,'10.223.48.128',1,1,NULL,NULL,NULL,29),(30,'10.223.48.129',1,1,NULL,NULL,NULL,30),(31,'10.223.48.130',1,1,NULL,NULL,NULL,31),(32,'10.223.48.131',1,1,NULL,NULL,NULL,32),(33,'10.223.48.132',1,1,NULL,NULL,NULL,33),(34,'10.223.48.133',1,1,NULL,NULL,NULL,34),(35,'10.223.48.134',1,1,NULL,NULL,NULL,35),(36,'10.223.48.135',1,1,NULL,NULL,NULL,36),(37,'10.223.48.136',1,1,NULL,NULL,NULL,37),(38,'10.223.48.137',1,1,NULL,NULL,NULL,38),(39,'10.223.48.138',1,1,NULL,NULL,NULL,39),(40,'10.223.48.139',1,1,NULL,NULL,NULL,40),(41,'10.223.48.140',1,1,NULL,NULL,NULL,41),(42,'10.223.48.141',1,1,NULL,NULL,NULL,42),(43,'10.223.48.142',1,1,NULL,NULL,NULL,43),(44,'10.223.48.143',1,1,NULL,NULL,NULL,44),(45,'10.223.48.144',1,1,NULL,NULL,NULL,45),(46,'10.223.48.145',1,1,NULL,NULL,NULL,46),(47,'10.223.48.146',1,1,NULL,NULL,NULL,47),(48,'10.223.48.147',1,1,NULL,NULL,NULL,48),(49,'10.223.48.148',1,1,NULL,NULL,NULL,49),(50,'10.223.48.149',1,1,NULL,NULL,NULL,50),(51,'10.223.48.150',1,1,NULL,NULL,NULL,51),(52,'10.223.48.151',1,1,NULL,NULL,NULL,52),(53,'10.223.48.152',1,1,NULL,NULL,NULL,53),(54,'10.223.48.153',1,1,NULL,NULL,NULL,54),(55,'10.223.48.154',1,1,NULL,NULL,NULL,55),(56,'10.223.48.155',1,1,NULL,NULL,NULL,56),(57,'10.223.48.156',1,1,NULL,NULL,NULL,57),(58,'10.223.48.157',1,1,NULL,NULL,NULL,58),(59,'10.223.48.158',1,1,NULL,NULL,NULL,59),(60,'10.223.48.159',1,1,NULL,NULL,NULL,60),(61,'10.223.48.160',1,1,NULL,NULL,NULL,61),(62,'10.223.48.161',1,1,NULL,NULL,NULL,62),(63,'10.223.48.162',1,1,NULL,NULL,NULL,63),(64,'10.223.48.163',1,1,NULL,NULL,NULL,64),(65,'10.223.48.164',1,1,NULL,NULL,NULL,65),(66,'10.223.48.165',1,1,NULL,NULL,NULL,66),(67,'10.223.48.166',1,1,NULL,NULL,NULL,67),(68,'10.223.48.167',1,1,6,'b9e363d4-075f-46b3-a226-49ad82a3d56e','2012-02-28 19:30:42',68),(69,'10.223.48.168',1,1,NULL,NULL,NULL,69),(70,'10.223.48.169',1,1,NULL,NULL,NULL,70),(71,'10.223.48.170',1,1,NULL,NULL,NULL,71),(72,'10.223.48.171',1,1,NULL,NULL,NULL,72),(73,'10.223.48.172',1,1,NULL,NULL,NULL,73),(74,'10.223.48.173',1,1,NULL,NULL,NULL,74),(75,'10.223.48.174',1,1,NULL,NULL,NULL,75),(76,'10.223.48.175',1,1,NULL,NULL,NULL,76),(77,'10.223.48.176',1,1,NULL,NULL,NULL,77),(78,'10.223.48.177',1,1,NULL,NULL,NULL,78),(79,'10.223.48.178',1,1,NULL,NULL,NULL,79),(80,'10.223.48.179',1,1,NULL,NULL,NULL,80),(81,'10.223.48.180',1,1,NULL,NULL,NULL,81),(82,'10.223.48.181',1,1,NULL,NULL,NULL,82),(83,'10.223.48.182',1,1,NULL,NULL,NULL,83),(84,'10.223.48.183',1,1,NULL,NULL,NULL,84),(85,'10.223.48.184',1,1,NULL,NULL,NULL,85),(86,'10.223.48.185',1,1,NULL,NULL,NULL,86),(87,'10.223.48.186',1,1,NULL,NULL,NULL,87),(88,'10.223.48.187',1,1,NULL,NULL,NULL,88),(89,'10.223.48.188',1,1,NULL,NULL,NULL,89),(90,'10.223.48.189',1,1,NULL,NULL,NULL,90),(91,'10.223.48.190',1,1,NULL,NULL,NULL,91),(92,'10.223.48.191',1,1,NULL,NULL,NULL,92),(93,'10.223.48.192',1,1,NULL,NULL,NULL,93),(94,'10.223.48.193',1,1,NULL,NULL,NULL,94),(95,'10.223.48.194',1,1,NULL,NULL,NULL,95),(96,'10.223.48.195',1,1,NULL,NULL,NULL,96),(97,'10.223.48.196',1,1,NULL,NULL,NULL,97),(98,'10.223.48.197',1,1,NULL,NULL,NULL,98),(99,'10.223.48.198',1,1,NULL,NULL,NULL,99),(100,'10.223.48.199',1,1,NULL,NULL,NULL,100),(101,'10.223.48.200',1,1,NULL,NULL,NULL,101);
/*!40000 ALTER TABLE `op_dc_ip_address_alloc` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `op_dc_link_local_ip_address_alloc`
--

DROP TABLE IF EXISTS `op_dc_link_local_ip_address_alloc`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `op_dc_link_local_ip_address_alloc` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `ip_address` char(40) NOT NULL COMMENT 'ip address',
  `data_center_id` bigint(20) unsigned NOT NULL COMMENT 'data center it belongs to',
  `pod_id` bigint(20) unsigned NOT NULL COMMENT 'pod it belongs to',
  `nic_id` bigint(20) unsigned DEFAULT NULL COMMENT 'instance id',
  `reservation_id` char(40) DEFAULT NULL COMMENT 'reservation id used to reserve this network',
  `taken` datetime DEFAULT NULL COMMENT 'Date taken',
  PRIMARY KEY (`id`),
  KEY `i_op_dc_link_local_ip_address_alloc__pod_id` (`pod_id`),
  KEY `i_op_dc_link_local_ip_address_alloc__data_center_id` (`data_center_id`),
  KEY `i_op_dc_link_local_ip_address_alloc__nic_id_reservation_id` (`nic_id`,`reservation_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1022 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `op_dc_link_local_ip_address_alloc`
--

LOCK TABLES `op_dc_link_local_ip_address_alloc` WRITE;
/*!40000 ALTER TABLE `op_dc_link_local_ip_address_alloc` DISABLE KEYS */;
INSERT INTO `op_dc_link_local_ip_address_alloc` VALUES (1,'169.254.0.2',1,1,NULL,NULL,NULL),(2,'169.254.0.3',1,1,NULL,NULL,NULL),(3,'169.254.0.4',1,1,NULL,NULL,NULL),(4,'169.254.0.5',1,1,NULL,NULL,NULL),(5,'169.254.0.6',1,1,NULL,NULL,NULL),(6,'169.254.0.7',1,1,NULL,NULL,NULL),(7,'169.254.0.8',1,1,NULL,NULL,NULL),(8,'169.254.0.9',1,1,NULL,NULL,NULL),(9,'169.254.0.10',1,1,NULL,NULL,NULL),(10,'169.254.0.11',1,1,NULL,NULL,NULL),(11,'169.254.0.12',1,1,NULL,NULL,NULL),(12,'169.254.0.13',1,1,NULL,NULL,NULL),(13,'169.254.0.14',1,1,NULL,NULL,NULL),(14,'169.254.0.15',1,1,NULL,NULL,NULL),(15,'169.254.0.16',1,1,NULL,NULL,NULL),(16,'169.254.0.17',1,1,NULL,NULL,NULL),(17,'169.254.0.18',1,1,NULL,NULL,NULL),(18,'169.254.0.19',1,1,NULL,NULL,NULL),(19,'169.254.0.20',1,1,NULL,NULL,NULL),(20,'169.254.0.21',1,1,NULL,NULL,NULL),(21,'169.254.0.22',1,1,NULL,NULL,NULL),(22,'169.254.0.23',1,1,NULL,NULL,NULL),(23,'169.254.0.24',1,1,NULL,NULL,NULL),(24,'169.254.0.25',1,1,NULL,NULL,NULL),(25,'169.254.0.26',1,1,NULL,NULL,NULL),(26,'169.254.0.27',1,1,NULL,NULL,NULL),(27,'169.254.0.28',1,1,NULL,NULL,NULL),(28,'169.254.0.29',1,1,NULL,NULL,NULL),(29,'169.254.0.30',1,1,NULL,NULL,NULL),(30,'169.254.0.31',1,1,NULL,NULL,NULL),(31,'169.254.0.32',1,1,NULL,NULL,NULL),(32,'169.254.0.33',1,1,NULL,NULL,NULL),(33,'169.254.0.34',1,1,NULL,NULL,NULL),(34,'169.254.0.35',1,1,NULL,NULL,NULL),(35,'169.254.0.36',1,1,NULL,NULL,NULL),(36,'169.254.0.37',1,1,NULL,NULL,NULL),(37,'169.254.0.38',1,1,NULL,NULL,NULL),(38,'169.254.0.39',1,1,NULL,NULL,NULL),(39,'169.254.0.40',1,1,NULL,NULL,NULL),(40,'169.254.0.41',1,1,NULL,NULL,NULL),(41,'169.254.0.42',1,1,NULL,NULL,NULL),(42,'169.254.0.43',1,1,NULL,NULL,NULL),(43,'169.254.0.44',1,1,NULL,NULL,NULL),(44,'169.254.0.45',1,1,NULL,NULL,NULL),(45,'169.254.0.46',1,1,NULL,NULL,NULL),(46,'169.254.0.47',1,1,NULL,NULL,NULL),(47,'169.254.0.48',1,1,NULL,NULL,NULL),(48,'169.254.0.49',1,1,NULL,NULL,NULL),(49,'169.254.0.50',1,1,NULL,NULL,NULL),(50,'169.254.0.51',1,1,NULL,NULL,NULL),(51,'169.254.0.52',1,1,NULL,NULL,NULL),(52,'169.254.0.53',1,1,NULL,NULL,NULL),(53,'169.254.0.54',1,1,NULL,NULL,NULL),(54,'169.254.0.55',1,1,NULL,NULL,NULL),(55,'169.254.0.56',1,1,NULL,NULL,NULL),(56,'169.254.0.57',1,1,NULL,NULL,NULL),(57,'169.254.0.58',1,1,NULL,NULL,NULL),(58,'169.254.0.59',1,1,NULL,NULL,NULL),(59,'169.254.0.60',1,1,NULL,NULL,NULL),(60,'169.254.0.61',1,1,NULL,NULL,NULL),(61,'169.254.0.62',1,1,NULL,NULL,NULL),(62,'169.254.0.63',1,1,NULL,NULL,NULL),(63,'169.254.0.64',1,1,NULL,NULL,NULL),(64,'169.254.0.65',1,1,NULL,NULL,NULL),(65,'169.254.0.66',1,1,NULL,NULL,NULL),(66,'169.254.0.67',1,1,NULL,NULL,NULL),(67,'169.254.0.68',1,1,NULL,NULL,NULL),(68,'169.254.0.69',1,1,NULL,NULL,NULL),(69,'169.254.0.70',1,1,NULL,NULL,NULL),(70,'169.254.0.71',1,1,NULL,NULL,NULL),(71,'169.254.0.72',1,1,NULL,NULL,NULL),(72,'169.254.0.73',1,1,NULL,NULL,NULL),(73,'169.254.0.74',1,1,NULL,NULL,NULL),(74,'169.254.0.75',1,1,NULL,NULL,NULL),(75,'169.254.0.76',1,1,NULL,NULL,NULL),(76,'169.254.0.77',1,1,NULL,NULL,NULL),(77,'169.254.0.78',1,1,NULL,NULL,NULL),(78,'169.254.0.79',1,1,NULL,NULL,NULL),(79,'169.254.0.80',1,1,NULL,NULL,NULL),(80,'169.254.0.81',1,1,NULL,NULL,NULL),(81,'169.254.0.82',1,1,NULL,NULL,NULL),(82,'169.254.0.83',1,1,NULL,NULL,NULL),(83,'169.254.0.84',1,1,NULL,NULL,NULL),(84,'169.254.0.85',1,1,NULL,NULL,NULL),(85,'169.254.0.86',1,1,NULL,NULL,NULL),(86,'169.254.0.87',1,1,NULL,NULL,NULL),(87,'169.254.0.88',1,1,NULL,NULL,NULL),(88,'169.254.0.89',1,1,NULL,NULL,NULL),(89,'169.254.0.90',1,1,NULL,NULL,NULL),(90,'169.254.0.91',1,1,NULL,NULL,NULL),(91,'169.254.0.92',1,1,NULL,NULL,NULL),(92,'169.254.0.93',1,1,NULL,NULL,NULL),(93,'169.254.0.94',1,1,NULL,NULL,NULL),(94,'169.254.0.95',1,1,NULL,NULL,NULL),(95,'169.254.0.96',1,1,NULL,NULL,NULL),(96,'169.254.0.97',1,1,NULL,NULL,NULL),(97,'169.254.0.98',1,1,NULL,NULL,NULL),(98,'169.254.0.99',1,1,NULL,NULL,NULL),(99,'169.254.0.100',1,1,NULL,NULL,NULL),(100,'169.254.0.101',1,1,NULL,NULL,NULL),(101,'169.254.0.102',1,1,NULL,NULL,NULL),(102,'169.254.0.103',1,1,NULL,NULL,NULL),(103,'169.254.0.104',1,1,NULL,NULL,NULL),(104,'169.254.0.105',1,1,NULL,NULL,NULL),(105,'169.254.0.106',1,1,NULL,NULL,NULL),(106,'169.254.0.107',1,1,NULL,NULL,NULL),(107,'169.254.0.108',1,1,NULL,NULL,NULL),(108,'169.254.0.109',1,1,NULL,NULL,NULL),(109,'169.254.0.110',1,1,NULL,NULL,NULL),(110,'169.254.0.111',1,1,NULL,NULL,NULL),(111,'169.254.0.112',1,1,NULL,NULL,NULL),(112,'169.254.0.113',1,1,NULL,NULL,NULL),(113,'169.254.0.114',1,1,NULL,NULL,NULL),(114,'169.254.0.115',1,1,NULL,NULL,NULL),(115,'169.254.0.116',1,1,NULL,NULL,NULL),(116,'169.254.0.117',1,1,NULL,NULL,NULL),(117,'169.254.0.118',1,1,NULL,NULL,NULL),(118,'169.254.0.119',1,1,NULL,NULL,NULL),(119,'169.254.0.120',1,1,NULL,NULL,NULL),(120,'169.254.0.121',1,1,NULL,NULL,NULL),(121,'169.254.0.122',1,1,NULL,NULL,NULL),(122,'169.254.0.123',1,1,NULL,NULL,NULL),(123,'169.254.0.124',1,1,NULL,NULL,NULL),(124,'169.254.0.125',1,1,NULL,NULL,NULL),(125,'169.254.0.126',1,1,NULL,NULL,NULL),(126,'169.254.0.127',1,1,NULL,NULL,NULL),(127,'169.254.0.128',1,1,NULL,NULL,NULL),(128,'169.254.0.129',1,1,NULL,NULL,NULL),(129,'169.254.0.130',1,1,NULL,NULL,NULL),(130,'169.254.0.131',1,1,NULL,NULL,NULL),(131,'169.254.0.132',1,1,NULL,NULL,NULL),(132,'169.254.0.133',1,1,NULL,NULL,NULL),(133,'169.254.0.134',1,1,NULL,NULL,NULL),(134,'169.254.0.135',1,1,NULL,NULL,NULL),(135,'169.254.0.136',1,1,NULL,NULL,NULL),(136,'169.254.0.137',1,1,NULL,NULL,NULL),(137,'169.254.0.138',1,1,NULL,NULL,NULL),(138,'169.254.0.139',1,1,NULL,NULL,NULL),(139,'169.254.0.140',1,1,NULL,NULL,NULL),(140,'169.254.0.141',1,1,NULL,NULL,NULL),(141,'169.254.0.142',1,1,NULL,NULL,NULL),(142,'169.254.0.143',1,1,NULL,NULL,NULL),(143,'169.254.0.144',1,1,5,'b9e363d4-075f-46b3-a226-49ad82a3d56e','2012-02-28 19:30:42'),(144,'169.254.0.145',1,1,NULL,NULL,NULL),(145,'169.254.0.146',1,1,NULL,NULL,NULL),(146,'169.254.0.147',1,1,NULL,NULL,NULL),(147,'169.254.0.148',1,1,NULL,NULL,NULL),(148,'169.254.0.149',1,1,NULL,NULL,NULL),(149,'169.254.0.150',1,1,NULL,NULL,NULL),(150,'169.254.0.151',1,1,NULL,NULL,NULL),(151,'169.254.0.152',1,1,NULL,NULL,NULL),(152,'169.254.0.153',1,1,NULL,NULL,NULL),(153,'169.254.0.154',1,1,NULL,NULL,NULL),(154,'169.254.0.155',1,1,NULL,NULL,NULL),(155,'169.254.0.156',1,1,NULL,NULL,NULL),(156,'169.254.0.157',1,1,NULL,NULL,NULL),(157,'169.254.0.158',1,1,NULL,NULL,NULL),(158,'169.254.0.159',1,1,NULL,NULL,NULL),(159,'169.254.0.160',1,1,NULL,NULL,NULL),(160,'169.254.0.161',1,1,NULL,NULL,NULL),(161,'169.254.0.162',1,1,NULL,NULL,NULL),(162,'169.254.0.163',1,1,NULL,NULL,NULL),(163,'169.254.0.164',1,1,NULL,NULL,NULL),(164,'169.254.0.165',1,1,NULL,NULL,NULL),(165,'169.254.0.166',1,1,NULL,NULL,NULL),(166,'169.254.0.167',1,1,NULL,NULL,NULL),(167,'169.254.0.168',1,1,NULL,NULL,NULL),(168,'169.254.0.169',1,1,NULL,NULL,NULL),(169,'169.254.0.170',1,1,NULL,NULL,NULL),(170,'169.254.0.171',1,1,NULL,NULL,NULL),(171,'169.254.0.172',1,1,NULL,NULL,NULL),(172,'169.254.0.173',1,1,NULL,NULL,NULL),(173,'169.254.0.174',1,1,NULL,NULL,NULL),(174,'169.254.0.175',1,1,NULL,NULL,NULL),(175,'169.254.0.176',1,1,NULL,NULL,NULL),(176,'169.254.0.177',1,1,NULL,NULL,NULL),(177,'169.254.0.178',1,1,NULL,NULL,NULL),(178,'169.254.0.179',1,1,NULL,NULL,NULL),(179,'169.254.0.180',1,1,NULL,NULL,NULL),(180,'169.254.0.181',1,1,NULL,NULL,NULL),(181,'169.254.0.182',1,1,NULL,NULL,NULL),(182,'169.254.0.183',1,1,NULL,NULL,NULL),(183,'169.254.0.184',1,1,NULL,NULL,NULL),(184,'169.254.0.185',1,1,NULL,NULL,NULL),(185,'169.254.0.186',1,1,NULL,NULL,NULL),(186,'169.254.0.187',1,1,NULL,NULL,NULL),(187,'169.254.0.188',1,1,NULL,NULL,NULL),(188,'169.254.0.189',1,1,NULL,NULL,NULL),(189,'169.254.0.190',1,1,NULL,NULL,NULL),(190,'169.254.0.191',1,1,NULL,NULL,NULL),(191,'169.254.0.192',1,1,NULL,NULL,NULL),(192,'169.254.0.193',1,1,NULL,NULL,NULL),(193,'169.254.0.194',1,1,NULL,NULL,NULL),(194,'169.254.0.195',1,1,NULL,NULL,NULL),(195,'169.254.0.196',1,1,NULL,NULL,NULL),(196,'169.254.0.197',1,1,NULL,NULL,NULL),(197,'169.254.0.198',1,1,NULL,NULL,NULL),(198,'169.254.0.199',1,1,NULL,NULL,NULL),(199,'169.254.0.200',1,1,NULL,NULL,NULL),(200,'169.254.0.201',1,1,NULL,NULL,NULL),(201,'169.254.0.202',1,1,NULL,NULL,NULL),(202,'169.254.0.203',1,1,NULL,NULL,NULL),(203,'169.254.0.204',1,1,NULL,NULL,NULL),(204,'169.254.0.205',1,1,NULL,NULL,NULL),(205,'169.254.0.206',1,1,NULL,NULL,NULL),(206,'169.254.0.207',1,1,NULL,NULL,NULL),(207,'169.254.0.208',1,1,NULL,NULL,NULL),(208,'169.254.0.209',1,1,NULL,NULL,NULL),(209,'169.254.0.210',1,1,NULL,NULL,NULL),(210,'169.254.0.211',1,1,NULL,NULL,NULL),(211,'169.254.0.212',1,1,NULL,NULL,NULL),(212,'169.254.0.213',1,1,NULL,NULL,NULL),(213,'169.254.0.214',1,1,2,'c178233a-17c2-441a-98bf-a5591b3eaea7','2012-02-28 19:30:42'),(214,'169.254.0.215',1,1,NULL,NULL,NULL),(215,'169.254.0.216',1,1,NULL,NULL,NULL),(216,'169.254.0.217',1,1,NULL,NULL,NULL),(217,'169.254.0.218',1,1,NULL,NULL,NULL),(218,'169.254.0.219',1,1,NULL,NULL,NULL),(219,'169.254.0.220',1,1,NULL,NULL,NULL),(220,'169.254.0.221',1,1,NULL,NULL,NULL),(221,'169.254.0.222',1,1,NULL,NULL,NULL),(222,'169.254.0.223',1,1,NULL,NULL,NULL),(223,'169.254.0.224',1,1,NULL,NULL,NULL),(224,'169.254.0.225',1,1,NULL,NULL,NULL),(225,'169.254.0.226',1,1,NULL,NULL,NULL),(226,'169.254.0.227',1,1,NULL,NULL,NULL),(227,'169.254.0.228',1,1,NULL,NULL,NULL),(228,'169.254.0.229',1,1,NULL,NULL,NULL),(229,'169.254.0.230',1,1,NULL,NULL,NULL),(230,'169.254.0.231',1,1,NULL,NULL,NULL),(231,'169.254.0.232',1,1,NULL,NULL,NULL),(232,'169.254.0.233',1,1,NULL,NULL,NULL),(233,'169.254.0.234',1,1,NULL,NULL,NULL),(234,'169.254.0.235',1,1,NULL,NULL,NULL),(235,'169.254.0.236',1,1,NULL,NULL,NULL),(236,'169.254.0.237',1,1,NULL,NULL,NULL),(237,'169.254.0.238',1,1,NULL,NULL,NULL),(238,'169.254.0.239',1,1,NULL,NULL,NULL),(239,'169.254.0.240',1,1,NULL,NULL,NULL),(240,'169.254.0.241',1,1,NULL,NULL,NULL),(241,'169.254.0.242',1,1,NULL,NULL,NULL),(242,'169.254.0.243',1,1,NULL,NULL,NULL),(243,'169.254.0.244',1,1,NULL,NULL,NULL),(244,'169.254.0.245',1,1,NULL,NULL,NULL),(245,'169.254.0.246',1,1,NULL,NULL,NULL),(246,'169.254.0.247',1,1,NULL,NULL,NULL),(247,'169.254.0.248',1,1,NULL,NULL,NULL),(248,'169.254.0.249',1,1,NULL,NULL,NULL),(249,'169.254.0.250',1,1,NULL,NULL,NULL),(250,'169.254.0.251',1,1,NULL,NULL,NULL),(251,'169.254.0.252',1,1,NULL,NULL,NULL),(252,'169.254.0.253',1,1,NULL,NULL,NULL),(253,'169.254.0.254',1,1,NULL,NULL,NULL),(254,'169.254.0.255',1,1,NULL,NULL,NULL),(255,'169.254.1.0',1,1,NULL,NULL,NULL),(256,'169.254.1.1',1,1,NULL,NULL,NULL),(257,'169.254.1.2',1,1,NULL,NULL,NULL),(258,'169.254.1.3',1,1,NULL,NULL,NULL),(259,'169.254.1.4',1,1,NULL,NULL,NULL),(260,'169.254.1.5',1,1,NULL,NULL,NULL),(261,'169.254.1.6',1,1,NULL,NULL,NULL),(262,'169.254.1.7',1,1,NULL,NULL,NULL),(263,'169.254.1.8',1,1,NULL,NULL,NULL),(264,'169.254.1.9',1,1,NULL,NULL,NULL),(265,'169.254.1.10',1,1,NULL,NULL,NULL),(266,'169.254.1.11',1,1,NULL,NULL,NULL),(267,'169.254.1.12',1,1,NULL,NULL,NULL),(268,'169.254.1.13',1,1,NULL,NULL,NULL),(269,'169.254.1.14',1,1,NULL,NULL,NULL),(270,'169.254.1.15',1,1,NULL,NULL,NULL),(271,'169.254.1.16',1,1,NULL,NULL,NULL),(272,'169.254.1.17',1,1,NULL,NULL,NULL),(273,'169.254.1.18',1,1,NULL,NULL,NULL),(274,'169.254.1.19',1,1,NULL,NULL,NULL),(275,'169.254.1.20',1,1,NULL,NULL,NULL),(276,'169.254.1.21',1,1,NULL,NULL,NULL),(277,'169.254.1.22',1,1,NULL,NULL,NULL),(278,'169.254.1.23',1,1,NULL,NULL,NULL),(279,'169.254.1.24',1,1,NULL,NULL,NULL),(280,'169.254.1.25',1,1,NULL,NULL,NULL),(281,'169.254.1.26',1,1,NULL,NULL,NULL),(282,'169.254.1.27',1,1,NULL,NULL,NULL),(283,'169.254.1.28',1,1,NULL,NULL,NULL),(284,'169.254.1.29',1,1,NULL,NULL,NULL),(285,'169.254.1.30',1,1,NULL,NULL,NULL),(286,'169.254.1.31',1,1,NULL,NULL,NULL),(287,'169.254.1.32',1,1,NULL,NULL,NULL),(288,'169.254.1.33',1,1,NULL,NULL,NULL),(289,'169.254.1.34',1,1,NULL,NULL,NULL),(290,'169.254.1.35',1,1,NULL,NULL,NULL),(291,'169.254.1.36',1,1,NULL,NULL,NULL),(292,'169.254.1.37',1,1,NULL,NULL,NULL),(293,'169.254.1.38',1,1,NULL,NULL,NULL),(294,'169.254.1.39',1,1,NULL,NULL,NULL),(295,'169.254.1.40',1,1,NULL,NULL,NULL),(296,'169.254.1.41',1,1,NULL,NULL,NULL),(297,'169.254.1.42',1,1,NULL,NULL,NULL),(298,'169.254.1.43',1,1,NULL,NULL,NULL),(299,'169.254.1.44',1,1,NULL,NULL,NULL),(300,'169.254.1.45',1,1,NULL,NULL,NULL),(301,'169.254.1.46',1,1,NULL,NULL,NULL),(302,'169.254.1.47',1,1,NULL,NULL,NULL),(303,'169.254.1.48',1,1,NULL,NULL,NULL),(304,'169.254.1.49',1,1,NULL,NULL,NULL),(305,'169.254.1.50',1,1,NULL,NULL,NULL),(306,'169.254.1.51',1,1,NULL,NULL,NULL),(307,'169.254.1.52',1,1,NULL,NULL,NULL),(308,'169.254.1.53',1,1,NULL,NULL,NULL),(309,'169.254.1.54',1,1,NULL,NULL,NULL),(310,'169.254.1.55',1,1,NULL,NULL,NULL),(311,'169.254.1.56',1,1,NULL,NULL,NULL),(312,'169.254.1.57',1,1,NULL,NULL,NULL),(313,'169.254.1.58',1,1,NULL,NULL,NULL),(314,'169.254.1.59',1,1,NULL,NULL,NULL),(315,'169.254.1.60',1,1,NULL,NULL,NULL),(316,'169.254.1.61',1,1,NULL,NULL,NULL),(317,'169.254.1.62',1,1,NULL,NULL,NULL),(318,'169.254.1.63',1,1,NULL,NULL,NULL),(319,'169.254.1.64',1,1,NULL,NULL,NULL),(320,'169.254.1.65',1,1,NULL,NULL,NULL),(321,'169.254.1.66',1,1,NULL,NULL,NULL),(322,'169.254.1.67',1,1,NULL,NULL,NULL),(323,'169.254.1.68',1,1,NULL,NULL,NULL),(324,'169.254.1.69',1,1,NULL,NULL,NULL),(325,'169.254.1.70',1,1,NULL,NULL,NULL),(326,'169.254.1.71',1,1,NULL,NULL,NULL),(327,'169.254.1.72',1,1,NULL,NULL,NULL),(328,'169.254.1.73',1,1,NULL,NULL,NULL),(329,'169.254.1.74',1,1,NULL,NULL,NULL),(330,'169.254.1.75',1,1,NULL,NULL,NULL),(331,'169.254.1.76',1,1,NULL,NULL,NULL),(332,'169.254.1.77',1,1,NULL,NULL,NULL),(333,'169.254.1.78',1,1,NULL,NULL,NULL),(334,'169.254.1.79',1,1,NULL,NULL,NULL),(335,'169.254.1.80',1,1,NULL,NULL,NULL),(336,'169.254.1.81',1,1,NULL,NULL,NULL),(337,'169.254.1.82',1,1,NULL,NULL,NULL),(338,'169.254.1.83',1,1,NULL,NULL,NULL),(339,'169.254.1.84',1,1,NULL,NULL,NULL),(340,'169.254.1.85',1,1,NULL,NULL,NULL),(341,'169.254.1.86',1,1,NULL,NULL,NULL),(342,'169.254.1.87',1,1,NULL,NULL,NULL),(343,'169.254.1.88',1,1,NULL,NULL,NULL),(344,'169.254.1.89',1,1,NULL,NULL,NULL),(345,'169.254.1.90',1,1,NULL,NULL,NULL),(346,'169.254.1.91',1,1,NULL,NULL,NULL),(347,'169.254.1.92',1,1,16,'63fe2b29-2c2d-4299-b4bd-f1c2929d6e72','2012-02-28 21:44:12'),(348,'169.254.1.93',1,1,NULL,NULL,NULL),(349,'169.254.1.94',1,1,NULL,NULL,NULL),(350,'169.254.1.95',1,1,NULL,NULL,NULL),(351,'169.254.1.96',1,1,NULL,NULL,NULL),(352,'169.254.1.97',1,1,NULL,NULL,NULL),(353,'169.254.1.98',1,1,NULL,NULL,NULL),(354,'169.254.1.99',1,1,NULL,NULL,NULL),(355,'169.254.1.100',1,1,NULL,NULL,NULL),(356,'169.254.1.101',1,1,NULL,NULL,NULL),(357,'169.254.1.102',1,1,NULL,NULL,NULL),(358,'169.254.1.103',1,1,NULL,NULL,NULL),(359,'169.254.1.104',1,1,NULL,NULL,NULL),(360,'169.254.1.105',1,1,NULL,NULL,NULL),(361,'169.254.1.106',1,1,NULL,NULL,NULL),(362,'169.254.1.107',1,1,NULL,NULL,NULL),(363,'169.254.1.108',1,1,NULL,NULL,NULL),(364,'169.254.1.109',1,1,NULL,NULL,NULL),(365,'169.254.1.110',1,1,NULL,NULL,NULL),(366,'169.254.1.111',1,1,NULL,NULL,NULL),(367,'169.254.1.112',1,1,NULL,NULL,NULL),(368,'169.254.1.113',1,1,NULL,NULL,NULL),(369,'169.254.1.114',1,1,NULL,NULL,NULL),(370,'169.254.1.115',1,1,NULL,NULL,NULL),(371,'169.254.1.116',1,1,NULL,NULL,NULL),(372,'169.254.1.117',1,1,NULL,NULL,NULL),(373,'169.254.1.118',1,1,NULL,NULL,NULL),(374,'169.254.1.119',1,1,NULL,NULL,NULL),(375,'169.254.1.120',1,1,NULL,NULL,NULL),(376,'169.254.1.121',1,1,NULL,NULL,NULL),(377,'169.254.1.122',1,1,NULL,NULL,NULL),(378,'169.254.1.123',1,1,NULL,NULL,NULL),(379,'169.254.1.124',1,1,NULL,NULL,NULL),(380,'169.254.1.125',1,1,NULL,NULL,NULL),(381,'169.254.1.126',1,1,NULL,NULL,NULL),(382,'169.254.1.127',1,1,NULL,NULL,NULL),(383,'169.254.1.128',1,1,NULL,NULL,NULL),(384,'169.254.1.129',1,1,NULL,NULL,NULL),(385,'169.254.1.130',1,1,NULL,NULL,NULL),(386,'169.254.1.131',1,1,NULL,NULL,NULL),(387,'169.254.1.132',1,1,NULL,NULL,NULL),(388,'169.254.1.133',1,1,NULL,NULL,NULL),(389,'169.254.1.134',1,1,NULL,NULL,NULL),(390,'169.254.1.135',1,1,NULL,NULL,NULL),(391,'169.254.1.136',1,1,NULL,NULL,NULL),(392,'169.254.1.137',1,1,NULL,NULL,NULL),(393,'169.254.1.138',1,1,NULL,NULL,NULL),(394,'169.254.1.139',1,1,NULL,NULL,NULL),(395,'169.254.1.140',1,1,NULL,NULL,NULL),(396,'169.254.1.141',1,1,NULL,NULL,NULL),(397,'169.254.1.142',1,1,NULL,NULL,NULL),(398,'169.254.1.143',1,1,NULL,NULL,NULL),(399,'169.254.1.144',1,1,NULL,NULL,NULL),(400,'169.254.1.145',1,1,NULL,NULL,NULL),(401,'169.254.1.146',1,1,NULL,NULL,NULL),(402,'169.254.1.147',1,1,NULL,NULL,NULL),(403,'169.254.1.148',1,1,NULL,NULL,NULL),(404,'169.254.1.149',1,1,NULL,NULL,NULL),(405,'169.254.1.150',1,1,NULL,NULL,NULL),(406,'169.254.1.151',1,1,NULL,NULL,NULL),(407,'169.254.1.152',1,1,NULL,NULL,NULL),(408,'169.254.1.153',1,1,NULL,NULL,NULL),(409,'169.254.1.154',1,1,NULL,NULL,NULL),(410,'169.254.1.155',1,1,NULL,NULL,NULL),(411,'169.254.1.156',1,1,NULL,NULL,NULL),(412,'169.254.1.157',1,1,NULL,NULL,NULL),(413,'169.254.1.158',1,1,NULL,NULL,NULL),(414,'169.254.1.159',1,1,NULL,NULL,NULL),(415,'169.254.1.160',1,1,NULL,NULL,NULL),(416,'169.254.1.161',1,1,NULL,NULL,NULL),(417,'169.254.1.162',1,1,NULL,NULL,NULL),(418,'169.254.1.163',1,1,NULL,NULL,NULL),(419,'169.254.1.164',1,1,NULL,NULL,NULL),(420,'169.254.1.165',1,1,NULL,NULL,NULL),(421,'169.254.1.166',1,1,NULL,NULL,NULL),(422,'169.254.1.167',1,1,NULL,NULL,NULL),(423,'169.254.1.168',1,1,NULL,NULL,NULL),(424,'169.254.1.169',1,1,NULL,NULL,NULL),(425,'169.254.1.170',1,1,NULL,NULL,NULL),(426,'169.254.1.171',1,1,NULL,NULL,NULL),(427,'169.254.1.172',1,1,NULL,NULL,NULL),(428,'169.254.1.173',1,1,NULL,NULL,NULL),(429,'169.254.1.174',1,1,NULL,NULL,NULL),(430,'169.254.1.175',1,1,NULL,NULL,NULL),(431,'169.254.1.176',1,1,NULL,NULL,NULL),(432,'169.254.1.177',1,1,NULL,NULL,NULL),(433,'169.254.1.178',1,1,NULL,NULL,NULL),(434,'169.254.1.179',1,1,NULL,NULL,NULL),(435,'169.254.1.180',1,1,NULL,NULL,NULL),(436,'169.254.1.181',1,1,NULL,NULL,NULL),(437,'169.254.1.182',1,1,NULL,NULL,NULL),(438,'169.254.1.183',1,1,NULL,NULL,NULL),(439,'169.254.1.184',1,1,NULL,NULL,NULL),(440,'169.254.1.185',1,1,NULL,NULL,NULL),(441,'169.254.1.186',1,1,NULL,NULL,NULL),(442,'169.254.1.187',1,1,NULL,NULL,NULL),(443,'169.254.1.188',1,1,NULL,NULL,NULL),(444,'169.254.1.189',1,1,NULL,NULL,NULL),(445,'169.254.1.190',1,1,NULL,NULL,NULL),(446,'169.254.1.191',1,1,NULL,NULL,NULL),(447,'169.254.1.192',1,1,NULL,NULL,NULL),(448,'169.254.1.193',1,1,NULL,NULL,NULL),(449,'169.254.1.194',1,1,NULL,NULL,NULL),(450,'169.254.1.195',1,1,NULL,NULL,NULL),(451,'169.254.1.196',1,1,NULL,NULL,NULL),(452,'169.254.1.197',1,1,NULL,NULL,NULL),(453,'169.254.1.198',1,1,NULL,NULL,NULL),(454,'169.254.1.199',1,1,NULL,NULL,NULL),(455,'169.254.1.200',1,1,NULL,NULL,NULL),(456,'169.254.1.201',1,1,NULL,NULL,NULL),(457,'169.254.1.202',1,1,NULL,NULL,NULL),(458,'169.254.1.203',1,1,NULL,NULL,NULL),(459,'169.254.1.204',1,1,NULL,NULL,NULL),(460,'169.254.1.205',1,1,NULL,NULL,NULL),(461,'169.254.1.206',1,1,NULL,NULL,NULL),(462,'169.254.1.207',1,1,NULL,NULL,NULL),(463,'169.254.1.208',1,1,NULL,NULL,NULL),(464,'169.254.1.209',1,1,NULL,NULL,NULL),(465,'169.254.1.210',1,1,NULL,NULL,NULL),(466,'169.254.1.211',1,1,NULL,NULL,NULL),(467,'169.254.1.212',1,1,NULL,NULL,NULL),(468,'169.254.1.213',1,1,NULL,NULL,NULL),(469,'169.254.1.214',1,1,NULL,NULL,NULL),(470,'169.254.1.215',1,1,NULL,NULL,NULL),(471,'169.254.1.216',1,1,NULL,NULL,NULL),(472,'169.254.1.217',1,1,NULL,NULL,NULL),(473,'169.254.1.218',1,1,NULL,NULL,NULL),(474,'169.254.1.219',1,1,NULL,NULL,NULL),(475,'169.254.1.220',1,1,NULL,NULL,NULL),(476,'169.254.1.221',1,1,NULL,NULL,NULL),(477,'169.254.1.222',1,1,NULL,NULL,NULL),(478,'169.254.1.223',1,1,NULL,NULL,NULL),(479,'169.254.1.224',1,1,NULL,NULL,NULL),(480,'169.254.1.225',1,1,NULL,NULL,NULL),(481,'169.254.1.226',1,1,NULL,NULL,NULL),(482,'169.254.1.227',1,1,NULL,NULL,NULL),(483,'169.254.1.228',1,1,NULL,NULL,NULL),(484,'169.254.1.229',1,1,NULL,NULL,NULL),(485,'169.254.1.230',1,1,NULL,NULL,NULL),(486,'169.254.1.231',1,1,NULL,NULL,NULL),(487,'169.254.1.232',1,1,NULL,NULL,NULL),(488,'169.254.1.233',1,1,NULL,NULL,NULL),(489,'169.254.1.234',1,1,NULL,NULL,NULL),(490,'169.254.1.235',1,1,NULL,NULL,NULL),(491,'169.254.1.236',1,1,NULL,NULL,NULL),(492,'169.254.1.237',1,1,NULL,NULL,NULL),(493,'169.254.1.238',1,1,NULL,NULL,NULL),(494,'169.254.1.239',1,1,NULL,NULL,NULL),(495,'169.254.1.240',1,1,NULL,NULL,NULL),(496,'169.254.1.241',1,1,NULL,NULL,NULL),(497,'169.254.1.242',1,1,NULL,NULL,NULL),(498,'169.254.1.243',1,1,NULL,NULL,NULL),(499,'169.254.1.244',1,1,NULL,NULL,NULL),(500,'169.254.1.245',1,1,NULL,NULL,NULL),(501,'169.254.1.246',1,1,NULL,NULL,NULL),(502,'169.254.1.247',1,1,NULL,NULL,NULL),(503,'169.254.1.248',1,1,NULL,NULL,NULL),(504,'169.254.1.249',1,1,NULL,NULL,NULL),(505,'169.254.1.250',1,1,NULL,NULL,NULL),(506,'169.254.1.251',1,1,NULL,NULL,NULL),(507,'169.254.1.252',1,1,NULL,NULL,NULL),(508,'169.254.1.253',1,1,NULL,NULL,NULL),(509,'169.254.1.254',1,1,NULL,NULL,NULL),(510,'169.254.1.255',1,1,NULL,NULL,NULL),(511,'169.254.2.0',1,1,NULL,NULL,NULL),(512,'169.254.2.1',1,1,NULL,NULL,NULL),(513,'169.254.2.2',1,1,NULL,NULL,NULL),(514,'169.254.2.3',1,1,NULL,NULL,NULL),(515,'169.254.2.4',1,1,NULL,NULL,NULL),(516,'169.254.2.5',1,1,NULL,NULL,NULL),(517,'169.254.2.6',1,1,NULL,NULL,NULL),(518,'169.254.2.7',1,1,NULL,NULL,NULL),(519,'169.254.2.8',1,1,NULL,NULL,NULL),(520,'169.254.2.9',1,1,NULL,NULL,NULL),(521,'169.254.2.10',1,1,NULL,NULL,NULL),(522,'169.254.2.11',1,1,NULL,NULL,NULL),(523,'169.254.2.12',1,1,NULL,NULL,NULL),(524,'169.254.2.13',1,1,NULL,NULL,NULL),(525,'169.254.2.14',1,1,NULL,NULL,NULL),(526,'169.254.2.15',1,1,NULL,NULL,NULL),(527,'169.254.2.16',1,1,NULL,NULL,NULL),(528,'169.254.2.17',1,1,NULL,NULL,NULL),(529,'169.254.2.18',1,1,NULL,NULL,NULL),(530,'169.254.2.19',1,1,NULL,NULL,NULL),(531,'169.254.2.20',1,1,NULL,NULL,NULL),(532,'169.254.2.21',1,1,NULL,NULL,NULL),(533,'169.254.2.22',1,1,NULL,NULL,NULL),(534,'169.254.2.23',1,1,NULL,NULL,NULL),(535,'169.254.2.24',1,1,NULL,NULL,NULL),(536,'169.254.2.25',1,1,NULL,NULL,NULL),(537,'169.254.2.26',1,1,NULL,NULL,NULL),(538,'169.254.2.27',1,1,NULL,NULL,NULL),(539,'169.254.2.28',1,1,NULL,NULL,NULL),(540,'169.254.2.29',1,1,NULL,NULL,NULL),(541,'169.254.2.30',1,1,NULL,NULL,NULL),(542,'169.254.2.31',1,1,NULL,NULL,NULL),(543,'169.254.2.32',1,1,NULL,NULL,NULL),(544,'169.254.2.33',1,1,NULL,NULL,NULL),(545,'169.254.2.34',1,1,NULL,NULL,NULL),(546,'169.254.2.35',1,1,NULL,NULL,NULL),(547,'169.254.2.36',1,1,NULL,NULL,NULL),(548,'169.254.2.37',1,1,NULL,NULL,NULL),(549,'169.254.2.38',1,1,NULL,NULL,NULL),(550,'169.254.2.39',1,1,NULL,NULL,NULL),(551,'169.254.2.40',1,1,NULL,NULL,NULL),(552,'169.254.2.41',1,1,NULL,NULL,NULL),(553,'169.254.2.42',1,1,NULL,NULL,NULL),(554,'169.254.2.43',1,1,NULL,NULL,NULL),(555,'169.254.2.44',1,1,NULL,NULL,NULL),(556,'169.254.2.45',1,1,NULL,NULL,NULL),(557,'169.254.2.46',1,1,NULL,NULL,NULL),(558,'169.254.2.47',1,1,NULL,NULL,NULL),(559,'169.254.2.48',1,1,NULL,NULL,NULL),(560,'169.254.2.49',1,1,NULL,NULL,NULL),(561,'169.254.2.50',1,1,NULL,NULL,NULL),(562,'169.254.2.51',1,1,NULL,NULL,NULL),(563,'169.254.2.52',1,1,NULL,NULL,NULL),(564,'169.254.2.53',1,1,NULL,NULL,NULL),(565,'169.254.2.54',1,1,NULL,NULL,NULL),(566,'169.254.2.55',1,1,NULL,NULL,NULL),(567,'169.254.2.56',1,1,NULL,NULL,NULL),(568,'169.254.2.57',1,1,NULL,NULL,NULL),(569,'169.254.2.58',1,1,NULL,NULL,NULL),(570,'169.254.2.59',1,1,NULL,NULL,NULL),(571,'169.254.2.60',1,1,NULL,NULL,NULL),(572,'169.254.2.61',1,1,NULL,NULL,NULL),(573,'169.254.2.62',1,1,NULL,NULL,NULL),(574,'169.254.2.63',1,1,NULL,NULL,NULL),(575,'169.254.2.64',1,1,NULL,NULL,NULL),(576,'169.254.2.65',1,1,NULL,NULL,NULL),(577,'169.254.2.66',1,1,NULL,NULL,NULL),(578,'169.254.2.67',1,1,NULL,NULL,NULL),(579,'169.254.2.68',1,1,NULL,NULL,NULL),(580,'169.254.2.69',1,1,NULL,NULL,NULL),(581,'169.254.2.70',1,1,NULL,NULL,NULL),(582,'169.254.2.71',1,1,NULL,NULL,NULL),(583,'169.254.2.72',1,1,NULL,NULL,NULL),(584,'169.254.2.73',1,1,NULL,NULL,NULL),(585,'169.254.2.74',1,1,NULL,NULL,NULL),(586,'169.254.2.75',1,1,NULL,NULL,NULL),(587,'169.254.2.76',1,1,NULL,NULL,NULL),(588,'169.254.2.77',1,1,NULL,NULL,NULL),(589,'169.254.2.78',1,1,NULL,NULL,NULL),(590,'169.254.2.79',1,1,NULL,NULL,NULL),(591,'169.254.2.80',1,1,NULL,NULL,NULL),(592,'169.254.2.81',1,1,NULL,NULL,NULL),(593,'169.254.2.82',1,1,NULL,NULL,NULL),(594,'169.254.2.83',1,1,NULL,NULL,NULL),(595,'169.254.2.84',1,1,NULL,NULL,NULL),(596,'169.254.2.85',1,1,NULL,NULL,NULL),(597,'169.254.2.86',1,1,NULL,NULL,NULL),(598,'169.254.2.87',1,1,NULL,NULL,NULL),(599,'169.254.2.88',1,1,NULL,NULL,NULL),(600,'169.254.2.89',1,1,NULL,NULL,NULL),(601,'169.254.2.90',1,1,NULL,NULL,NULL),(602,'169.254.2.91',1,1,NULL,NULL,NULL),(603,'169.254.2.92',1,1,NULL,NULL,NULL),(604,'169.254.2.93',1,1,NULL,NULL,NULL),(605,'169.254.2.94',1,1,NULL,NULL,NULL),(606,'169.254.2.95',1,1,NULL,NULL,NULL),(607,'169.254.2.96',1,1,NULL,NULL,NULL),(608,'169.254.2.97',1,1,NULL,NULL,NULL),(609,'169.254.2.98',1,1,NULL,NULL,NULL),(610,'169.254.2.99',1,1,NULL,NULL,NULL),(611,'169.254.2.100',1,1,NULL,NULL,NULL),(612,'169.254.2.101',1,1,NULL,NULL,NULL),(613,'169.254.2.102',1,1,NULL,NULL,NULL),(614,'169.254.2.103',1,1,NULL,NULL,NULL),(615,'169.254.2.104',1,1,NULL,NULL,NULL),(616,'169.254.2.105',1,1,NULL,NULL,NULL),(617,'169.254.2.106',1,1,NULL,NULL,NULL),(618,'169.254.2.107',1,1,NULL,NULL,NULL),(619,'169.254.2.108',1,1,NULL,NULL,NULL),(620,'169.254.2.109',1,1,NULL,NULL,NULL),(621,'169.254.2.110',1,1,NULL,NULL,NULL),(622,'169.254.2.111',1,1,NULL,NULL,NULL),(623,'169.254.2.112',1,1,NULL,NULL,NULL),(624,'169.254.2.113',1,1,NULL,NULL,NULL),(625,'169.254.2.114',1,1,NULL,NULL,NULL),(626,'169.254.2.115',1,1,NULL,NULL,NULL),(627,'169.254.2.116',1,1,NULL,NULL,NULL),(628,'169.254.2.117',1,1,NULL,NULL,NULL),(629,'169.254.2.118',1,1,NULL,NULL,NULL),(630,'169.254.2.119',1,1,NULL,NULL,NULL),(631,'169.254.2.120',1,1,NULL,NULL,NULL),(632,'169.254.2.121',1,1,NULL,NULL,NULL),(633,'169.254.2.122',1,1,NULL,NULL,NULL),(634,'169.254.2.123',1,1,NULL,NULL,NULL),(635,'169.254.2.124',1,1,NULL,NULL,NULL),(636,'169.254.2.125',1,1,22,'a88f9e42-6bc0-4bb5-860b-e36a9c57f920','2012-02-28 21:45:22'),(637,'169.254.2.126',1,1,NULL,NULL,NULL),(638,'169.254.2.127',1,1,NULL,NULL,NULL),(639,'169.254.2.128',1,1,NULL,NULL,NULL),(640,'169.254.2.129',1,1,NULL,NULL,NULL),(641,'169.254.2.130',1,1,NULL,NULL,NULL),(642,'169.254.2.131',1,1,NULL,NULL,NULL),(643,'169.254.2.132',1,1,NULL,NULL,NULL),(644,'169.254.2.133',1,1,NULL,NULL,NULL),(645,'169.254.2.134',1,1,NULL,NULL,NULL),(646,'169.254.2.135',1,1,NULL,NULL,NULL),(647,'169.254.2.136',1,1,NULL,NULL,NULL),(648,'169.254.2.137',1,1,NULL,NULL,NULL),(649,'169.254.2.138',1,1,NULL,NULL,NULL),(650,'169.254.2.139',1,1,NULL,NULL,NULL),(651,'169.254.2.140',1,1,NULL,NULL,NULL),(652,'169.254.2.141',1,1,NULL,NULL,NULL),(653,'169.254.2.142',1,1,NULL,NULL,NULL),(654,'169.254.2.143',1,1,NULL,NULL,NULL),(655,'169.254.2.144',1,1,NULL,NULL,NULL),(656,'169.254.2.145',1,1,NULL,NULL,NULL),(657,'169.254.2.146',1,1,NULL,NULL,NULL),(658,'169.254.2.147',1,1,NULL,NULL,NULL),(659,'169.254.2.148',1,1,NULL,NULL,NULL),(660,'169.254.2.149',1,1,NULL,NULL,NULL),(661,'169.254.2.150',1,1,NULL,NULL,NULL),(662,'169.254.2.151',1,1,NULL,NULL,NULL),(663,'169.254.2.152',1,1,NULL,NULL,NULL),(664,'169.254.2.153',1,1,NULL,NULL,NULL),(665,'169.254.2.154',1,1,NULL,NULL,NULL),(666,'169.254.2.155',1,1,NULL,NULL,NULL),(667,'169.254.2.156',1,1,NULL,NULL,NULL),(668,'169.254.2.157',1,1,NULL,NULL,NULL),(669,'169.254.2.158',1,1,NULL,NULL,NULL),(670,'169.254.2.159',1,1,NULL,NULL,NULL),(671,'169.254.2.160',1,1,NULL,NULL,NULL),(672,'169.254.2.161',1,1,NULL,NULL,NULL),(673,'169.254.2.162',1,1,33,'958b5026-95f2-4f3a-8482-93246756bd89','2012-02-28 21:48:57'),(674,'169.254.2.163',1,1,NULL,NULL,NULL),(675,'169.254.2.164',1,1,NULL,NULL,NULL),(676,'169.254.2.165',1,1,NULL,NULL,NULL),(677,'169.254.2.166',1,1,NULL,NULL,NULL),(678,'169.254.2.167',1,1,NULL,NULL,NULL),(679,'169.254.2.168',1,1,NULL,NULL,NULL),(680,'169.254.2.169',1,1,NULL,NULL,NULL),(681,'169.254.2.170',1,1,NULL,NULL,NULL),(682,'169.254.2.171',1,1,NULL,NULL,NULL),(683,'169.254.2.172',1,1,NULL,NULL,NULL),(684,'169.254.2.173',1,1,NULL,NULL,NULL),(685,'169.254.2.174',1,1,NULL,NULL,NULL),(686,'169.254.2.175',1,1,NULL,NULL,NULL),(687,'169.254.2.176',1,1,NULL,NULL,NULL),(688,'169.254.2.177',1,1,NULL,NULL,NULL),(689,'169.254.2.178',1,1,NULL,NULL,NULL),(690,'169.254.2.179',1,1,NULL,NULL,NULL),(691,'169.254.2.180',1,1,NULL,NULL,NULL),(692,'169.254.2.181',1,1,NULL,NULL,NULL),(693,'169.254.2.182',1,1,NULL,NULL,NULL),(694,'169.254.2.183',1,1,NULL,NULL,NULL),(695,'169.254.2.184',1,1,NULL,NULL,NULL),(696,'169.254.2.185',1,1,NULL,NULL,NULL),(697,'169.254.2.186',1,1,NULL,NULL,NULL),(698,'169.254.2.187',1,1,NULL,NULL,NULL),(699,'169.254.2.188',1,1,NULL,NULL,NULL),(700,'169.254.2.189',1,1,NULL,NULL,NULL),(701,'169.254.2.190',1,1,NULL,NULL,NULL),(702,'169.254.2.191',1,1,NULL,NULL,NULL),(703,'169.254.2.192',1,1,NULL,NULL,NULL),(704,'169.254.2.193',1,1,NULL,NULL,NULL),(705,'169.254.2.194',1,1,NULL,NULL,NULL),(706,'169.254.2.195',1,1,NULL,NULL,NULL),(707,'169.254.2.196',1,1,NULL,NULL,NULL),(708,'169.254.2.197',1,1,NULL,NULL,NULL),(709,'169.254.2.198',1,1,NULL,NULL,NULL),(710,'169.254.2.199',1,1,NULL,NULL,NULL),(711,'169.254.2.200',1,1,NULL,NULL,NULL),(712,'169.254.2.201',1,1,NULL,NULL,NULL),(713,'169.254.2.202',1,1,NULL,NULL,NULL),(714,'169.254.2.203',1,1,NULL,NULL,NULL),(715,'169.254.2.204',1,1,NULL,NULL,NULL),(716,'169.254.2.205',1,1,NULL,NULL,NULL),(717,'169.254.2.206',1,1,NULL,NULL,NULL),(718,'169.254.2.207',1,1,NULL,NULL,NULL),(719,'169.254.2.208',1,1,NULL,NULL,NULL),(720,'169.254.2.209',1,1,NULL,NULL,NULL),(721,'169.254.2.210',1,1,NULL,NULL,NULL),(722,'169.254.2.211',1,1,NULL,NULL,NULL),(723,'169.254.2.212',1,1,NULL,NULL,NULL),(724,'169.254.2.213',1,1,NULL,NULL,NULL),(725,'169.254.2.214',1,1,NULL,NULL,NULL),(726,'169.254.2.215',1,1,NULL,NULL,NULL),(727,'169.254.2.216',1,1,NULL,NULL,NULL),(728,'169.254.2.217',1,1,NULL,NULL,NULL),(729,'169.254.2.218',1,1,NULL,NULL,NULL),(730,'169.254.2.219',1,1,NULL,NULL,NULL),(731,'169.254.2.220',1,1,NULL,NULL,NULL),(732,'169.254.2.221',1,1,NULL,NULL,NULL),(733,'169.254.2.222',1,1,NULL,NULL,NULL),(734,'169.254.2.223',1,1,NULL,NULL,NULL),(735,'169.254.2.224',1,1,NULL,NULL,NULL),(736,'169.254.2.225',1,1,NULL,NULL,NULL),(737,'169.254.2.226',1,1,31,'10da1e5c-4382-4dbc-89a8-c80dda46bae7','2012-02-28 21:47:29'),(738,'169.254.2.227',1,1,NULL,NULL,NULL),(739,'169.254.2.228',1,1,NULL,NULL,NULL),(740,'169.254.2.229',1,1,NULL,NULL,NULL),(741,'169.254.2.230',1,1,NULL,NULL,NULL),(742,'169.254.2.231',1,1,NULL,NULL,NULL),(743,'169.254.2.232',1,1,NULL,NULL,NULL),(744,'169.254.2.233',1,1,NULL,NULL,NULL),(745,'169.254.2.234',1,1,NULL,NULL,NULL),(746,'169.254.2.235',1,1,NULL,NULL,NULL),(747,'169.254.2.236',1,1,NULL,NULL,NULL),(748,'169.254.2.237',1,1,NULL,NULL,NULL),(749,'169.254.2.238',1,1,NULL,NULL,NULL),(750,'169.254.2.239',1,1,NULL,NULL,NULL),(751,'169.254.2.240',1,1,NULL,NULL,NULL),(752,'169.254.2.241',1,1,NULL,NULL,NULL),(753,'169.254.2.242',1,1,NULL,NULL,NULL),(754,'169.254.2.243',1,1,29,'6eae5655-0926-40f0-9919-8a97d820781e','2012-02-28 21:47:26'),(755,'169.254.2.244',1,1,NULL,NULL,NULL),(756,'169.254.2.245',1,1,NULL,NULL,NULL),(757,'169.254.2.246',1,1,NULL,NULL,NULL),(758,'169.254.2.247',1,1,NULL,NULL,NULL),(759,'169.254.2.248',1,1,NULL,NULL,NULL),(760,'169.254.2.249',1,1,NULL,NULL,NULL),(761,'169.254.2.250',1,1,NULL,NULL,NULL),(762,'169.254.2.251',1,1,NULL,NULL,NULL),(763,'169.254.2.252',1,1,NULL,NULL,NULL),(764,'169.254.2.253',1,1,NULL,NULL,NULL),(765,'169.254.2.254',1,1,NULL,NULL,NULL),(766,'169.254.2.255',1,1,NULL,NULL,NULL),(767,'169.254.3.0',1,1,NULL,NULL,NULL),(768,'169.254.3.1',1,1,NULL,NULL,NULL),(769,'169.254.3.2',1,1,NULL,NULL,NULL),(770,'169.254.3.3',1,1,NULL,NULL,NULL),(771,'169.254.3.4',1,1,NULL,NULL,NULL),(772,'169.254.3.5',1,1,NULL,NULL,NULL),(773,'169.254.3.6',1,1,NULL,NULL,NULL),(774,'169.254.3.7',1,1,NULL,NULL,NULL),(775,'169.254.3.8',1,1,NULL,NULL,NULL),(776,'169.254.3.9',1,1,NULL,NULL,NULL),(777,'169.254.3.10',1,1,NULL,NULL,NULL),(778,'169.254.3.11',1,1,NULL,NULL,NULL),(779,'169.254.3.12',1,1,NULL,NULL,NULL),(780,'169.254.3.13',1,1,NULL,NULL,NULL),(781,'169.254.3.14',1,1,NULL,NULL,NULL),(782,'169.254.3.15',1,1,NULL,NULL,NULL),(783,'169.254.3.16',1,1,NULL,NULL,NULL),(784,'169.254.3.17',1,1,NULL,NULL,NULL),(785,'169.254.3.18',1,1,NULL,NULL,NULL),(786,'169.254.3.19',1,1,NULL,NULL,NULL),(787,'169.254.3.20',1,1,NULL,NULL,NULL),(788,'169.254.3.21',1,1,NULL,NULL,NULL),(789,'169.254.3.22',1,1,NULL,NULL,NULL),(790,'169.254.3.23',1,1,NULL,NULL,NULL),(791,'169.254.3.24',1,1,NULL,NULL,NULL),(792,'169.254.3.25',1,1,NULL,NULL,NULL),(793,'169.254.3.26',1,1,NULL,NULL,NULL),(794,'169.254.3.27',1,1,NULL,NULL,NULL),(795,'169.254.3.28',1,1,NULL,NULL,NULL),(796,'169.254.3.29',1,1,NULL,NULL,NULL),(797,'169.254.3.30',1,1,NULL,NULL,NULL),(798,'169.254.3.31',1,1,NULL,NULL,NULL),(799,'169.254.3.32',1,1,NULL,NULL,NULL),(800,'169.254.3.33',1,1,NULL,NULL,NULL),(801,'169.254.3.34',1,1,NULL,NULL,NULL),(802,'169.254.3.35',1,1,NULL,NULL,NULL),(803,'169.254.3.36',1,1,NULL,NULL,NULL),(804,'169.254.3.37',1,1,NULL,NULL,NULL),(805,'169.254.3.38',1,1,NULL,NULL,NULL),(806,'169.254.3.39',1,1,NULL,NULL,NULL),(807,'169.254.3.40',1,1,NULL,NULL,NULL),(808,'169.254.3.41',1,1,NULL,NULL,NULL),(809,'169.254.3.42',1,1,NULL,NULL,NULL),(810,'169.254.3.43',1,1,NULL,NULL,NULL),(811,'169.254.3.44',1,1,NULL,NULL,NULL),(812,'169.254.3.45',1,1,NULL,NULL,NULL),(813,'169.254.3.46',1,1,NULL,NULL,NULL),(814,'169.254.3.47',1,1,NULL,NULL,NULL),(815,'169.254.3.48',1,1,NULL,NULL,NULL),(816,'169.254.3.49',1,1,NULL,NULL,NULL),(817,'169.254.3.50',1,1,NULL,NULL,NULL),(818,'169.254.3.51',1,1,NULL,NULL,NULL),(819,'169.254.3.52',1,1,NULL,NULL,NULL),(820,'169.254.3.53',1,1,NULL,NULL,NULL),(821,'169.254.3.54',1,1,NULL,NULL,NULL),(822,'169.254.3.55',1,1,NULL,NULL,NULL),(823,'169.254.3.56',1,1,NULL,NULL,NULL),(824,'169.254.3.57',1,1,NULL,NULL,NULL),(825,'169.254.3.58',1,1,NULL,NULL,NULL),(826,'169.254.3.59',1,1,NULL,NULL,NULL),(827,'169.254.3.60',1,1,NULL,NULL,NULL),(828,'169.254.3.61',1,1,NULL,NULL,NULL),(829,'169.254.3.62',1,1,NULL,NULL,NULL),(830,'169.254.3.63',1,1,NULL,NULL,NULL),(831,'169.254.3.64',1,1,NULL,NULL,NULL),(832,'169.254.3.65',1,1,NULL,NULL,NULL),(833,'169.254.3.66',1,1,NULL,NULL,NULL),(834,'169.254.3.67',1,1,NULL,NULL,NULL),(835,'169.254.3.68',1,1,NULL,NULL,NULL),(836,'169.254.3.69',1,1,NULL,NULL,NULL),(837,'169.254.3.70',1,1,NULL,NULL,NULL),(838,'169.254.3.71',1,1,NULL,NULL,NULL),(839,'169.254.3.72',1,1,NULL,NULL,NULL),(840,'169.254.3.73',1,1,NULL,NULL,NULL),(841,'169.254.3.74',1,1,NULL,NULL,NULL),(842,'169.254.3.75',1,1,NULL,NULL,NULL),(843,'169.254.3.76',1,1,NULL,NULL,NULL),(844,'169.254.3.77',1,1,NULL,NULL,NULL),(845,'169.254.3.78',1,1,NULL,NULL,NULL),(846,'169.254.3.79',1,1,NULL,NULL,NULL),(847,'169.254.3.80',1,1,NULL,NULL,NULL),(848,'169.254.3.81',1,1,NULL,NULL,NULL),(849,'169.254.3.82',1,1,NULL,NULL,NULL),(850,'169.254.3.83',1,1,NULL,NULL,NULL),(851,'169.254.3.84',1,1,NULL,NULL,NULL),(852,'169.254.3.85',1,1,NULL,NULL,NULL),(853,'169.254.3.86',1,1,NULL,NULL,NULL),(854,'169.254.3.87',1,1,NULL,NULL,NULL),(855,'169.254.3.88',1,1,NULL,NULL,NULL),(856,'169.254.3.89',1,1,NULL,NULL,NULL),(857,'169.254.3.90',1,1,NULL,NULL,NULL),(858,'169.254.3.91',1,1,NULL,NULL,NULL),(859,'169.254.3.92',1,1,NULL,NULL,NULL),(860,'169.254.3.93',1,1,NULL,NULL,NULL),(861,'169.254.3.94',1,1,NULL,NULL,NULL),(862,'169.254.3.95',1,1,NULL,NULL,NULL),(863,'169.254.3.96',1,1,NULL,NULL,NULL),(864,'169.254.3.97',1,1,NULL,NULL,NULL),(865,'169.254.3.98',1,1,NULL,NULL,NULL),(866,'169.254.3.99',1,1,NULL,NULL,NULL),(867,'169.254.3.100',1,1,NULL,NULL,NULL),(868,'169.254.3.101',1,1,NULL,NULL,NULL),(869,'169.254.3.102',1,1,NULL,NULL,NULL),(870,'169.254.3.103',1,1,NULL,NULL,NULL),(871,'169.254.3.104',1,1,NULL,NULL,NULL),(872,'169.254.3.105',1,1,NULL,NULL,NULL),(873,'169.254.3.106',1,1,NULL,NULL,NULL),(874,'169.254.3.107',1,1,NULL,NULL,NULL),(875,'169.254.3.108',1,1,NULL,NULL,NULL),(876,'169.254.3.109',1,1,NULL,NULL,NULL),(877,'169.254.3.110',1,1,NULL,NULL,NULL),(878,'169.254.3.111',1,1,NULL,NULL,NULL),(879,'169.254.3.112',1,1,NULL,NULL,NULL),(880,'169.254.3.113',1,1,NULL,NULL,NULL),(881,'169.254.3.114',1,1,NULL,NULL,NULL),(882,'169.254.3.115',1,1,NULL,NULL,NULL),(883,'169.254.3.116',1,1,NULL,NULL,NULL),(884,'169.254.3.117',1,1,NULL,NULL,NULL),(885,'169.254.3.118',1,1,NULL,NULL,NULL),(886,'169.254.3.119',1,1,NULL,NULL,NULL),(887,'169.254.3.120',1,1,NULL,NULL,NULL),(888,'169.254.3.121',1,1,NULL,NULL,NULL),(889,'169.254.3.122',1,1,NULL,NULL,NULL),(890,'169.254.3.123',1,1,NULL,NULL,NULL),(891,'169.254.3.124',1,1,NULL,NULL,NULL),(892,'169.254.3.125',1,1,NULL,NULL,NULL),(893,'169.254.3.126',1,1,NULL,NULL,NULL),(894,'169.254.3.127',1,1,NULL,NULL,NULL),(895,'169.254.3.128',1,1,NULL,NULL,NULL),(896,'169.254.3.129',1,1,NULL,NULL,NULL),(897,'169.254.3.130',1,1,NULL,NULL,NULL),(898,'169.254.3.131',1,1,NULL,NULL,NULL),(899,'169.254.3.132',1,1,NULL,NULL,NULL),(900,'169.254.3.133',1,1,NULL,NULL,NULL),(901,'169.254.3.134',1,1,NULL,NULL,NULL),(902,'169.254.3.135',1,1,NULL,NULL,NULL),(903,'169.254.3.136',1,1,NULL,NULL,NULL),(904,'169.254.3.137',1,1,NULL,NULL,NULL),(905,'169.254.3.138',1,1,NULL,NULL,NULL),(906,'169.254.3.139',1,1,NULL,NULL,NULL),(907,'169.254.3.140',1,1,NULL,NULL,NULL),(908,'169.254.3.141',1,1,NULL,NULL,NULL),(909,'169.254.3.142',1,1,NULL,NULL,NULL),(910,'169.254.3.143',1,1,NULL,NULL,NULL),(911,'169.254.3.144',1,1,NULL,NULL,NULL),(912,'169.254.3.145',1,1,NULL,NULL,NULL),(913,'169.254.3.146',1,1,NULL,NULL,NULL),(914,'169.254.3.147',1,1,NULL,NULL,NULL),(915,'169.254.3.148',1,1,NULL,NULL,NULL),(916,'169.254.3.149',1,1,NULL,NULL,NULL),(917,'169.254.3.150',1,1,NULL,NULL,NULL),(918,'169.254.3.151',1,1,NULL,NULL,NULL),(919,'169.254.3.152',1,1,NULL,NULL,NULL),(920,'169.254.3.153',1,1,NULL,NULL,NULL),(921,'169.254.3.154',1,1,NULL,NULL,NULL),(922,'169.254.3.155',1,1,NULL,NULL,NULL),(923,'169.254.3.156',1,1,NULL,NULL,NULL),(924,'169.254.3.157',1,1,NULL,NULL,NULL),(925,'169.254.3.158',1,1,NULL,NULL,NULL),(926,'169.254.3.159',1,1,NULL,NULL,NULL),(927,'169.254.3.160',1,1,NULL,NULL,NULL),(928,'169.254.3.161',1,1,NULL,NULL,NULL),(929,'169.254.3.162',1,1,NULL,NULL,NULL),(930,'169.254.3.163',1,1,NULL,NULL,NULL),(931,'169.254.3.164',1,1,NULL,NULL,NULL),(932,'169.254.3.165',1,1,NULL,NULL,NULL),(933,'169.254.3.166',1,1,NULL,NULL,NULL),(934,'169.254.3.167',1,1,NULL,NULL,NULL),(935,'169.254.3.168',1,1,NULL,NULL,NULL),(936,'169.254.3.169',1,1,NULL,NULL,NULL),(937,'169.254.3.170',1,1,NULL,NULL,NULL),(938,'169.254.3.171',1,1,NULL,NULL,NULL),(939,'169.254.3.172',1,1,NULL,NULL,NULL),(940,'169.254.3.173',1,1,NULL,NULL,NULL),(941,'169.254.3.174',1,1,NULL,NULL,NULL),(942,'169.254.3.175',1,1,NULL,NULL,NULL),(943,'169.254.3.176',1,1,NULL,NULL,NULL),(944,'169.254.3.177',1,1,NULL,NULL,NULL),(945,'169.254.3.178',1,1,NULL,NULL,NULL),(946,'169.254.3.179',1,1,NULL,NULL,NULL),(947,'169.254.3.180',1,1,NULL,NULL,NULL),(948,'169.254.3.181',1,1,NULL,NULL,NULL),(949,'169.254.3.182',1,1,NULL,NULL,NULL),(950,'169.254.3.183',1,1,NULL,NULL,NULL),(951,'169.254.3.184',1,1,NULL,NULL,NULL),(952,'169.254.3.185',1,1,NULL,NULL,NULL),(953,'169.254.3.186',1,1,NULL,NULL,NULL),(954,'169.254.3.187',1,1,NULL,NULL,NULL),(955,'169.254.3.188',1,1,NULL,NULL,NULL),(956,'169.254.3.189',1,1,NULL,NULL,NULL),(957,'169.254.3.190',1,1,NULL,NULL,NULL),(958,'169.254.3.191',1,1,NULL,NULL,NULL),(959,'169.254.3.192',1,1,NULL,NULL,NULL),(960,'169.254.3.193',1,1,NULL,NULL,NULL),(961,'169.254.3.194',1,1,NULL,NULL,NULL),(962,'169.254.3.195',1,1,NULL,NULL,NULL),(963,'169.254.3.196',1,1,NULL,NULL,NULL),(964,'169.254.3.197',1,1,NULL,NULL,NULL),(965,'169.254.3.198',1,1,NULL,NULL,NULL),(966,'169.254.3.199',1,1,NULL,NULL,NULL),(967,'169.254.3.200',1,1,NULL,NULL,NULL),(968,'169.254.3.201',1,1,NULL,NULL,NULL),(969,'169.254.3.202',1,1,NULL,NULL,NULL),(970,'169.254.3.203',1,1,NULL,NULL,NULL),(971,'169.254.3.204',1,1,NULL,NULL,NULL),(972,'169.254.3.205',1,1,NULL,NULL,NULL),(973,'169.254.3.206',1,1,NULL,NULL,NULL),(974,'169.254.3.207',1,1,NULL,NULL,NULL),(975,'169.254.3.208',1,1,NULL,NULL,NULL),(976,'169.254.3.209',1,1,NULL,NULL,NULL),(977,'169.254.3.210',1,1,NULL,NULL,NULL),(978,'169.254.3.211',1,1,35,'4157583e-79b2-48f0-be5d-6aad346c30b8','2012-02-28 21:49:07'),(979,'169.254.3.212',1,1,NULL,NULL,NULL),(980,'169.254.3.213',1,1,NULL,NULL,NULL),(981,'169.254.3.214',1,1,NULL,NULL,NULL),(982,'169.254.3.215',1,1,NULL,NULL,NULL),(983,'169.254.3.216',1,1,NULL,NULL,NULL),(984,'169.254.3.217',1,1,NULL,NULL,NULL),(985,'169.254.3.218',1,1,NULL,NULL,NULL),(986,'169.254.3.219',1,1,NULL,NULL,NULL),(987,'169.254.3.220',1,1,NULL,NULL,NULL),(988,'169.254.3.221',1,1,NULL,NULL,NULL),(989,'169.254.3.222',1,1,NULL,NULL,NULL),(990,'169.254.3.223',1,1,NULL,NULL,NULL),(991,'169.254.3.224',1,1,NULL,NULL,NULL),(992,'169.254.3.225',1,1,NULL,NULL,NULL),(993,'169.254.3.226',1,1,NULL,NULL,NULL),(994,'169.254.3.227',1,1,NULL,NULL,NULL),(995,'169.254.3.228',1,1,NULL,NULL,NULL),(996,'169.254.3.229',1,1,NULL,NULL,NULL),(997,'169.254.3.230',1,1,NULL,NULL,NULL),(998,'169.254.3.231',1,1,NULL,NULL,NULL),(999,'169.254.3.232',1,1,NULL,NULL,NULL),(1000,'169.254.3.233',1,1,NULL,NULL,NULL),(1001,'169.254.3.234',1,1,NULL,NULL,NULL),(1002,'169.254.3.235',1,1,NULL,NULL,NULL),(1003,'169.254.3.236',1,1,NULL,NULL,NULL),(1004,'169.254.3.237',1,1,NULL,NULL,NULL),(1005,'169.254.3.238',1,1,NULL,NULL,NULL),(1006,'169.254.3.239',1,1,NULL,NULL,NULL),(1007,'169.254.3.240',1,1,NULL,NULL,NULL),(1008,'169.254.3.241',1,1,NULL,NULL,NULL),(1009,'169.254.3.242',1,1,NULL,NULL,NULL),(1010,'169.254.3.243',1,1,NULL,NULL,NULL),(1011,'169.254.3.244',1,1,NULL,NULL,NULL),(1012,'169.254.3.245',1,1,NULL,NULL,NULL),(1013,'169.254.3.246',1,1,NULL,NULL,NULL),(1014,'169.254.3.247',1,1,NULL,NULL,NULL),(1015,'169.254.3.248',1,1,NULL,NULL,NULL),(1016,'169.254.3.249',1,1,NULL,NULL,NULL),(1017,'169.254.3.250',1,1,NULL,NULL,NULL),(1018,'169.254.3.251',1,1,NULL,NULL,NULL),(1019,'169.254.3.252',1,1,NULL,NULL,NULL),(1020,'169.254.3.253',1,1,NULL,NULL,NULL),(1021,'169.254.3.254',1,1,NULL,NULL,NULL);
/*!40000 ALTER TABLE `op_dc_link_local_ip_address_alloc` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `op_dc_vnet_alloc`
--

DROP TABLE IF EXISTS `op_dc_vnet_alloc`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `op_dc_vnet_alloc` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'primary id',
  `vnet` varchar(18) NOT NULL COMMENT 'vnet',
  `data_center_id` bigint(20) unsigned NOT NULL COMMENT 'data center the vnet belongs to',
  `reservation_id` char(40) DEFAULT NULL COMMENT 'reservation id',
  `account_id` bigint(20) unsigned DEFAULT NULL COMMENT 'account the vnet belongs to right now',
  `taken` datetime DEFAULT NULL COMMENT 'Date taken',
  PRIMARY KEY (`id`),
  UNIQUE KEY `i_op_dc_vnet_alloc__vnet__data_center_id` (`vnet`,`data_center_id`),
  UNIQUE KEY `i_op_dc_vnet_alloc__vnet__data_center_id__account_id` (`vnet`,`data_center_id`,`account_id`),
  KEY `i_op_dc_vnet_alloc__dc_taken` (`data_center_id`,`taken`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `op_dc_vnet_alloc`
--

LOCK TABLES `op_dc_vnet_alloc` WRITE;
/*!40000 ALTER TABLE `op_dc_vnet_alloc` DISABLE KEYS */;
INSERT INTO `op_dc_vnet_alloc` VALUES (1,'1015',1,NULL,NULL,NULL),(2,'1016',1,NULL,NULL,NULL),(3,'1017',1,NULL,NULL,NULL),(4,'1018',1,NULL,NULL,NULL),(5,'1019',1,NULL,NULL,NULL),(6,'1020',1,NULL,NULL,NULL),(7,'1021',1,NULL,NULL,NULL),(8,'1022',1,NULL,NULL,NULL),(9,'1023',1,NULL,NULL,NULL),(10,'1024',1,'cd4b11ce-ae75-44c7-89ad-609c76ac0238',2,'2012-02-28 21:44:11'),(11,'1025',1,NULL,NULL,NULL),(12,'1026',1,NULL,NULL,NULL),(13,'1027',1,NULL,NULL,NULL),(14,'1028',1,'60fd9627-2eeb-4d6d-b9e2-f5a4c0808979',3,'2012-02-28 21:47:26'),(15,'1029',1,NULL,NULL,NULL);
/*!40000 ALTER TABLE `op_dc_vnet_alloc` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `op_ha_work`
--

DROP TABLE IF EXISTS `op_ha_work`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `op_ha_work` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
  `instance_id` bigint(20) unsigned NOT NULL COMMENT 'vm instance that needs to be ha.',
  `type` varchar(32) NOT NULL COMMENT 'type of work',
  `vm_type` varchar(32) NOT NULL COMMENT 'VM type',
  `state` varchar(32) NOT NULL COMMENT 'state of the vm instance when this happened.',
  `mgmt_server_id` bigint(20) unsigned DEFAULT NULL COMMENT 'management server that has taken up the work of doing ha',
  `host_id` bigint(20) unsigned DEFAULT NULL COMMENT 'host that the vm is suppose to be on',
  `created` datetime NOT NULL COMMENT 'time the entry was requested',
  `tried` int(10) unsigned DEFAULT NULL COMMENT '# of times tried',
  `taken` datetime DEFAULT NULL COMMENT 'time it was taken by the management server',
  `step` varchar(32) NOT NULL COMMENT 'Step in the work',
  `time_to_try` bigint(20) DEFAULT NULL COMMENT 'time to try do this work',
  `updated` bigint(20) unsigned NOT NULL COMMENT 'time the VM state was updated when it was stored into work queue',
  PRIMARY KEY (`id`),
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
/*!40101 SET character_set_client = @saved_cs_client */;

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
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `op_host` (
  `id` bigint(20) unsigned NOT NULL COMMENT 'host id',
  `sequence` bigint(20) unsigned NOT NULL DEFAULT '1' COMMENT 'sequence for the host communication',
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`),
  CONSTRAINT `fk_op_host__id` FOREIGN KEY (`id`) REFERENCES `host` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `op_host`
--

LOCK TABLES `op_host` WRITE;
/*!40000 ALTER TABLE `op_host` DISABLE KEYS */;
INSERT INTO `op_host` VALUES (1,1),(2,1),(3,1),(4,1);
/*!40000 ALTER TABLE `op_host` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `op_host_capacity`
--

DROP TABLE IF EXISTS `op_host_capacity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `op_host_capacity` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `host_id` bigint(20) unsigned DEFAULT NULL,
  `data_center_id` bigint(20) unsigned NOT NULL,
  `pod_id` bigint(20) unsigned DEFAULT NULL,
  `cluster_id` bigint(20) unsigned DEFAULT NULL COMMENT 'foreign key to cluster',
  `used_capacity` bigint(20) NOT NULL,
  `reserved_capacity` bigint(20) NOT NULL,
  `total_capacity` bigint(20) NOT NULL,
  `capacity_type` int(1) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  KEY `i_op_host_capacity__host_type` (`host_id`,`capacity_type`),
  KEY `i_op_host_capacity__pod_id` (`pod_id`),
  KEY `i_op_host_capacity__data_center_id` (`data_center_id`),
  KEY `i_op_host_capacity__cluster_id` (`cluster_id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `op_host_capacity`
--

LOCK TABLES `op_host_capacity` WRITE;
/*!40000 ALTER TABLE `op_host_capacity` DISABLE KEYS */;
INSERT INTO `op_host_capacity` VALUES (3,200,1,1,1,42547019776,0,118107783823360,3),(4,NULL,1,NULL,NULL,14,0,185,4),(5,NULL,1,1,NULL,2,0,101,5),(6,2,1,1,1,3758096384,0,16190149248,0),(7,2,1,1,1,5500,0,9040,1);
/*!40000 ALTER TABLE `op_host_capacity` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `op_host_transfer`
--

DROP TABLE IF EXISTS `op_host_transfer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `op_host_transfer` (
  `id` bigint(20) unsigned NOT NULL COMMENT 'Id of the host',
  `initial_mgmt_server_id` bigint(20) unsigned DEFAULT NULL COMMENT 'management server the host is transfered from',
  `future_mgmt_server_id` bigint(20) unsigned DEFAULT NULL COMMENT 'management server the host is transfered to',
  `state` varchar(32) NOT NULL COMMENT 'the transfer state of the host',
  `created` datetime NOT NULL COMMENT 'date created',
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`),
  KEY `fk_op_host_transfer__initial_mgmt_server_id` (`initial_mgmt_server_id`),
  KEY `fk_op_host_transfer__future_mgmt_server_id` (`future_mgmt_server_id`),
  CONSTRAINT `fk_op_host_transfer__id` FOREIGN KEY (`id`) REFERENCES `host` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_op_host_transfer__initial_mgmt_server_id` FOREIGN KEY (`initial_mgmt_server_id`) REFERENCES `mshost` (`msid`),
  CONSTRAINT `fk_op_host_transfer__future_mgmt_server_id` FOREIGN KEY (`future_mgmt_server_id`) REFERENCES `mshost` (`msid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `op_host_transfer`
--

LOCK TABLES `op_host_transfer` WRITE;
/*!40000 ALTER TABLE `op_host_transfer` DISABLE KEYS */;
/*!40000 ALTER TABLE `op_host_transfer` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `op_host_upgrade`
--

DROP TABLE IF EXISTS `op_host_upgrade`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `op_host_upgrade` (
  `host_id` bigint(20) unsigned NOT NULL COMMENT 'host id',
  `version` varchar(20) NOT NULL COMMENT 'version',
  `state` varchar(20) NOT NULL COMMENT 'state',
  PRIMARY KEY (`host_id`),
  UNIQUE KEY `host_id` (`host_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

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
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `op_it_work` (
  `id` char(40) NOT NULL DEFAULT '' COMMENT 'reservation id',
  `mgmt_server_id` bigint(20) unsigned DEFAULT NULL COMMENT 'management server id',
  `created_at` bigint(20) unsigned NOT NULL COMMENT 'when was this work detail created',
  `thread` varchar(255) NOT NULL COMMENT 'thread name',
  `type` char(32) NOT NULL COMMENT 'type of work',
  `vm_type` char(32) NOT NULL COMMENT 'type of vm',
  `step` char(32) NOT NULL COMMENT 'state',
  `updated_at` bigint(20) unsigned NOT NULL COMMENT 'time it was taken over',
  `instance_id` bigint(20) unsigned NOT NULL COMMENT 'vm instance',
  `resource_type` char(32) DEFAULT NULL COMMENT 'type of resource being worked on',
  `resource_id` bigint(20) unsigned DEFAULT NULL COMMENT 'resource id being worked on',
  PRIMARY KEY (`id`),
  KEY `fk_op_it_work__mgmt_server_id` (`mgmt_server_id`),
  KEY `fk_op_it_work__instance_id` (`instance_id`),
  KEY `i_op_it_work__step` (`step`),
  CONSTRAINT `fk_op_it_work__mgmt_server_id` FOREIGN KEY (`mgmt_server_id`) REFERENCES `mshost` (`msid`),
  CONSTRAINT `fk_op_it_work__instance_id` FOREIGN KEY (`instance_id`) REFERENCES `vm_instance` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `op_it_work`
--

LOCK TABLES `op_it_work` WRITE;
/*!40000 ALTER TABLE `op_it_work` DISABLE KEYS */;
INSERT INTO `op_it_work` VALUES ('08e1e22f-9091-4fce-b474-3dfe75f752f2',1,1330465310,'Job-Executor-3','Starting','User','Done',1330465310,5,NULL,0),('10da1e5c-4382-4dbc-89a8-c80dda46bae7',1,1330465642,'Job-Executor-2','Starting','DomainRouter','Done',1330465702,12,NULL,0),('3b59b06c-6048-471e-a874-0c076e74aa8d',1,1330465190,'Job-Executor-1','Starting','User','Done',1330465190,3,NULL,0),('4157583e-79b2-48f0-be5d-6aad346c30b8',1,1330465702,'Job-Executor-2','Starting','DomainRouter','Done',1330465822,14,NULL,0),('60fd9627-2eeb-4d6d-b9e2-f5a4c0808979',1,1330465642,'Job-Executor-3','Starting','User','Done',1330465882,10,NULL,0),('63fe2b29-2c2d-4299-b4bd-f1c2929d6e72',1,1330465402,'Job-Executor-1','Starting','DomainRouter','Done',1330465462,7,NULL,0),('6eae5655-0926-40f0-9919-8a97d820781e',1,1330465642,'Job-Executor-3','Starting','DomainRouter','Done',1330465642,11,NULL,0),('958b5026-95f2-4f3a-8482-93246756bd89',1,1330465702,'Job-Executor-3','Starting','DomainRouter','Done',1330465762,13,NULL,0),('a88f9e42-6bc0-4bb5-860b-e36a9c57f920',1,1330465522,'Job-Executor-2','Starting','DomainRouter','Done',1330465582,9,NULL,0),('b9e363d4-075f-46b3-a226-49ad82a3d56e',1,1330457390,'consoleproxy-1','Starting','ConsoleProxy','Done',1330457570,2,NULL,0),('c178233a-17c2-441a-98bf-a5591b3eaea7',1,1330457390,'secstorage-1','Starting','SecondaryStorageVm','Done',1330457570,1,NULL,0),('cd4b11ce-ae75-44c7-89ad-609c76ac0238',1,1330465402,'Job-Executor-1','Starting','User','Done',1330465582,6,NULL,0),('ea4b5549-d7ea-4574-8df8-b6f0f2a4dcce',1,1330465190,'Job-Executor-2','Starting','User','Done',1330465190,4,NULL,0),('fabed3ae-abfc-46d9-a1f1-e435f3afaa70',1,1330465462,'Job-Executor-2','Starting','User','Done',1330465882,8,NULL,0);
/*!40000 ALTER TABLE `op_it_work` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `op_lock`
--

DROP TABLE IF EXISTS `op_lock`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `op_lock` (
  `key` varchar(128) NOT NULL COMMENT 'primary key of the table',
  `mac` varchar(17) NOT NULL COMMENT 'management server id of the server that holds this lock',
  `ip` char(40) NOT NULL COMMENT 'name of the thread that holds this lock',
  `thread` varchar(255) NOT NULL COMMENT 'Thread id that acquired this lock',
  `acquired_on` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Time acquired',
  `waiters` int(11) NOT NULL DEFAULT '0' COMMENT 'How many have the thread acquired this lock (reentrant)',
  PRIMARY KEY (`key`),
  UNIQUE KEY `key` (`key`),
  KEY `i_op_lock__mac_ip_thread` (`mac`,`ip`,`thread`)
) ENGINE=MEMORY DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

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
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `op_networks` (
  `id` bigint(20) unsigned NOT NULL,
  `mac_address_seq` bigint(20) unsigned NOT NULL DEFAULT '1' COMMENT 'mac address',
  `nics_count` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '# of nics',
  `gc` tinyint(3) unsigned NOT NULL DEFAULT '1' COMMENT 'gc this network or not',
  `check_for_gc` tinyint(3) unsigned NOT NULL DEFAULT '1' COMMENT 'check this network for gc or not',
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`),
  CONSTRAINT `fk_op_networks__id` FOREIGN KEY (`id`) REFERENCES `networks` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `op_networks`
--

LOCK TABLES `op_networks` WRITE;
/*!40000 ALTER TABLE `op_networks` DISABLE KEYS */;
INSERT INTO `op_networks` VALUES (200,1,0,0,0),(201,1,0,0,0),(202,1,0,0,0),(203,1,0,0,0),(204,1,1,1,1),(205,1,2,1,1),(206,1,2,1,1),(207,7,2,1,1),(208,1,1,1,1),(209,3,1,1,1);
/*!40000 ALTER TABLE `op_networks` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `op_nwgrp_work`
--

DROP TABLE IF EXISTS `op_nwgrp_work`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `op_nwgrp_work` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
  `instance_id` bigint(20) unsigned NOT NULL COMMENT 'vm instance that needs rules to be synced.',
  `mgmt_server_id` bigint(20) unsigned DEFAULT NULL COMMENT 'management server that has taken up the work of doing rule sync',
  `created` datetime NOT NULL COMMENT 'time the entry was requested',
  `taken` datetime DEFAULT NULL COMMENT 'time it was taken by the management server',
  `step` varchar(32) NOT NULL COMMENT 'Step in the work',
  `seq_no` bigint(20) unsigned DEFAULT NULL COMMENT 'seq number to be sent to agent, uniquely identifies ruleset update',
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`),
  KEY `i_op_nwgrp_work__instance_id` (`instance_id`),
  KEY `i_op_nwgrp_work__mgmt_server_id` (`mgmt_server_id`),
  KEY `i_op_nwgrp_work__taken` (`taken`),
  KEY `i_op_nwgrp_work__step` (`step`),
  KEY `i_op_nwgrp_work__seq_no` (`seq_no`)
) ENGINE=MEMORY DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

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
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `op_pod_vlan_alloc` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'primary id',
  `vlan` varchar(18) NOT NULL COMMENT 'vlan id',
  `data_center_id` bigint(20) unsigned NOT NULL COMMENT 'data center the pod belongs to',
  `pod_id` bigint(20) unsigned NOT NULL COMMENT 'pod the vlan belongs to',
  `account_id` bigint(20) unsigned DEFAULT NULL COMMENT 'account the vlan belongs to right now',
  `taken` datetime DEFAULT NULL COMMENT 'Date taken',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

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
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `op_vm_ruleset_log` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
  `instance_id` bigint(20) unsigned NOT NULL COMMENT 'vm instance that needs rules to be synced.',
  `created` datetime NOT NULL COMMENT 'time the entry was requested',
  `logsequence` bigint(20) unsigned DEFAULT NULL COMMENT 'seq number to be sent to agent, uniquely identifies ruleset update',
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`),
  UNIQUE KEY `u_op_vm_ruleset_log__instance_id` (`instance_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

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
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ovs_host_vlan_alloc` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `host_id` bigint(20) unsigned DEFAULT NULL COMMENT 'host id',
  `account_id` bigint(20) unsigned DEFAULT NULL COMMENT 'account id',
  `vlan` bigint(20) unsigned DEFAULT NULL COMMENT 'vlan id under account #account_id on host #host_id',
  `ref` int(10) unsigned NOT NULL DEFAULT '0' COMMENT 'reference count',
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

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
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ovs_tunnel` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `from` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT 'from host id',
  `to` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT 'to host id',
  `key` int(10) unsigned DEFAULT '0' COMMENT 'current gre key can be used',
  PRIMARY KEY (`from`,`to`),
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

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
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ovs_tunnel_account` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `from` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT 'from host id',
  `to` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT 'to host id',
  `account` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT 'account',
  `key` int(10) unsigned DEFAULT NULL COMMENT 'gre key',
  `port_name` varchar(32) DEFAULT NULL COMMENT 'in port on open vswitch',
  `state` varchar(16) DEFAULT 'FAILED' COMMENT 'result of tunnel creatation',
  PRIMARY KEY (`from`,`to`,`account`),
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

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
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ovs_tunnel_alloc` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `from` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT 'from host id',
  `to` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT 'to host id',
  `in_port` int(10) unsigned DEFAULT NULL COMMENT 'in port on open vswitch',
  PRIMARY KEY (`from`,`to`),
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

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
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ovs_vlan_mapping_dirty` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `account_id` bigint(20) unsigned DEFAULT NULL COMMENT 'account id',
  `dirty` int(1) unsigned NOT NULL DEFAULT '0' COMMENT '1 means vlan mapping of this account was changed',
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

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
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ovs_vm_flow_log` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
  `instance_id` bigint(20) unsigned NOT NULL COMMENT 'vm instance that needs flows to be synced.',
  `created` datetime NOT NULL COMMENT 'time the entry was requested',
  `logsequence` bigint(20) unsigned DEFAULT NULL COMMENT 'seq number to be sent to agent, uniquely identifies flow update',
  `vm_name` varchar(255) NOT NULL COMMENT 'vm name',
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

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
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ovs_work` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
  `instance_id` bigint(20) unsigned NOT NULL COMMENT 'vm instance that needs rules to be synced.',
  `mgmt_server_id` bigint(20) unsigned DEFAULT NULL COMMENT 'management server that has taken up the work of doing rule sync',
  `created` datetime NOT NULL COMMENT 'time the entry was requested',
  `taken` datetime DEFAULT NULL COMMENT 'time it was taken by the management server',
  `step` varchar(32) NOT NULL COMMENT 'Step in the work',
  `seq_no` bigint(20) unsigned DEFAULT NULL COMMENT 'seq number to be sent to agent, uniquely identifies ruleset update',
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

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
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `pod_vlan_map` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `pod_id` bigint(20) unsigned NOT NULL COMMENT 'pod id. foreign key to pod table',
  `vlan_db_id` bigint(20) unsigned NOT NULL COMMENT 'database id of vlan. foreign key to vlan table',
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`),
  KEY `i_pod_vlan_map__pod_id` (`pod_id`),
  KEY `i_pod_vlan_map__vlan_id` (`vlan_db_id`),
  CONSTRAINT `fk_pod_vlan_map__pod_id` FOREIGN KEY (`pod_id`) REFERENCES `host_pod_ref` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_pod_vlan_map__vlan_id` FOREIGN KEY (`vlan_db_id`) REFERENCES `vlan` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

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
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `port_forwarding_rules` (
  `id` bigint(20) unsigned NOT NULL COMMENT 'id',
  `instance_id` bigint(20) unsigned NOT NULL COMMENT 'vm instance id',
  `dest_ip_address` char(40) NOT NULL COMMENT 'id_address',
  `dest_port_start` int(10) NOT NULL COMMENT 'starting port of the port range to map to',
  `dest_port_end` int(10) NOT NULL COMMENT 'end port of the the port range to map to',
  PRIMARY KEY (`id`),
  KEY `fk_port_forwarding_rules__instance_id` (`instance_id`),
  CONSTRAINT `fk_port_forwarding_rules__id` FOREIGN KEY (`id`) REFERENCES `firewall_rules` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_port_forwarding_rules__instance_id` FOREIGN KEY (`instance_id`) REFERENCES `vm_instance` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

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
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `remote_access_vpn` (
  `vpn_server_addr_id` bigint(20) unsigned NOT NULL,
  `account_id` bigint(20) unsigned NOT NULL,
  `network_id` bigint(20) unsigned NOT NULL,
  `domain_id` bigint(20) unsigned NOT NULL,
  `local_ip` char(40) NOT NULL,
  `ip_range` varchar(32) NOT NULL,
  `ipsec_psk` varchar(256) NOT NULL,
  `state` char(32) NOT NULL,
  PRIMARY KEY (`vpn_server_addr_id`),
  UNIQUE KEY `vpn_server_addr_id` (`vpn_server_addr_id`),
  KEY `fk_remote_access_vpn__account_id` (`account_id`),
  KEY `fk_remote_access_vpn__domain_id` (`domain_id`),
  KEY `fk_remote_access_vpn__network_id` (`network_id`),
  CONSTRAINT `fk_remote_access_vpn__account_id` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_remote_access_vpn__domain_id` FOREIGN KEY (`domain_id`) REFERENCES `domain` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_remote_access_vpn__network_id` FOREIGN KEY (`network_id`) REFERENCES `networks` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_remote_access_vpn__vpn_server_addr_id` FOREIGN KEY (`vpn_server_addr_id`) REFERENCES `user_ip_address` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

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
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `resource_count` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `account_id` bigint(20) unsigned DEFAULT NULL,
  `domain_id` bigint(20) unsigned DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `count` bigint(20) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `i_resource_count__type_accountId` (`type`,`account_id`),
  UNIQUE KEY `i_resource_count__type_domaintId` (`type`,`domain_id`),
  KEY `fk_resource_count__account_id` (`account_id`),
  KEY `fk_resource_count__domain_id` (`domain_id`),
  KEY `i_resource_count__type` (`type`),
  CONSTRAINT `fk_resource_count__account_id` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_resource_count__domain_id` FOREIGN KEY (`domain_id`) REFERENCES `domain` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `resource_count`
--

LOCK TABLES `resource_count` WRITE;
/*!40000 ALTER TABLE `resource_count` DISABLE KEYS */;
INSERT INTO `resource_count` VALUES (1,NULL,1,'user_vm',3),(2,NULL,1,'public_ip',2),(3,NULL,1,'volume',3),(4,NULL,1,'snapshot',0),(5,NULL,1,'template',0),(6,1,NULL,'user_vm',0),(7,1,NULL,'public_ip',0),(8,1,NULL,'volume',0),(9,1,NULL,'snapshot',0),(10,1,NULL,'template',0),(11,2,NULL,'user_vm',2),(12,2,NULL,'public_ip',1),(13,2,NULL,'volume',2),(14,2,NULL,'snapshot',0),(15,2,NULL,'template',0),(16,NULL,2,'user_vm',1),(17,NULL,2,'public_ip',1),(18,NULL,2,'volume',1),(19,NULL,2,'snapshot',0),(20,NULL,2,'template',0),(21,3,NULL,'user_vm',1),(22,3,NULL,'public_ip',1),(23,3,NULL,'volume',1),(24,3,NULL,'snapshot',0),(25,3,NULL,'template',0);
/*!40000 ALTER TABLE `resource_count` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `resource_limit`
--

DROP TABLE IF EXISTS `resource_limit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `resource_limit` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `domain_id` bigint(20) unsigned DEFAULT NULL,
  `account_id` bigint(20) unsigned DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `max` bigint(20) NOT NULL DEFAULT '-1',
  PRIMARY KEY (`id`),
  KEY `i_resource_limit__domain_id` (`domain_id`),
  KEY `i_resource_limit__account_id` (`account_id`),
  CONSTRAINT `fk_resource_limit__account_id` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_resource_limit__domain_id` FOREIGN KEY (`domain_id`) REFERENCES `domain` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

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
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `secondary_storage_vm` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `public_mac_address` varchar(17) DEFAULT NULL COMMENT 'mac address of the public facing network card',
  `public_ip_address` char(40) DEFAULT NULL COMMENT 'public ip address for the sec storage vm',
  `public_netmask` varchar(15) DEFAULT NULL COMMENT 'public netmask used for the sec storage vm',
  `guid` varchar(255) DEFAULT NULL COMMENT 'copied from guid of secondary storage host',
  `nfs_share` varchar(255) DEFAULT NULL COMMENT 'server and path exported by the nfs server ',
  `last_update` datetime DEFAULT NULL COMMENT 'Last session update time',
  `role` varchar(64) NOT NULL DEFAULT 'templateProcessor' COMMENT 'work role of secondary storage host(templateProcessor | commandExecutor)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `public_mac_address` (`public_mac_address`),
  UNIQUE KEY `public_ip_address` (`public_ip_address`),
  CONSTRAINT `fk_secondary_storage_vm__id` FOREIGN KEY (`id`) REFERENCES `vm_instance` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `secondary_storage_vm`
--

LOCK TABLES `secondary_storage_vm` WRITE;
/*!40000 ALTER TABLE `secondary_storage_vm` DISABLE KEYS */;
INSERT INTO `secondary_storage_vm` VALUES (1,'06:ed:ec:00:00:86','10.223.120.132','255.255.255.0',NULL,NULL,NULL,'templateProcessor');
/*!40000 ALTER TABLE `secondary_storage_vm` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `security_group`
--

DROP TABLE IF EXISTS `security_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `security_group` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `description` varchar(4096) DEFAULT NULL,
  `domain_id` bigint(20) unsigned NOT NULL,
  `account_id` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`,`account_id`),
  KEY `fk_security_group__account_id` (`account_id`),
  KEY `fk_security_group__domain_id` (`domain_id`),
  KEY `i_security_group_name` (`name`),
  CONSTRAINT `fk_security_group__domain_id` FOREIGN KEY (`domain_id`) REFERENCES `domain` (`id`),
  CONSTRAINT `fk_security_group__account_id` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `security_group`
--

LOCK TABLES `security_group` WRITE;
/*!40000 ALTER TABLE `security_group` DISABLE KEYS */;
INSERT INTO `security_group` VALUES (1,'default','Default Security Group',1,2),(2,'default','Default Security Group',2,3);
/*!40000 ALTER TABLE `security_group` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `security_group_vm_map`
--

DROP TABLE IF EXISTS `security_group_vm_map`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `security_group_vm_map` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `security_group_id` bigint(20) unsigned NOT NULL,
  `instance_id` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_security_group_vm_map___security_group_id` (`security_group_id`),
  KEY `fk_security_group_vm_map___instance_id` (`instance_id`),
  CONSTRAINT `fk_security_group_vm_map___instance_id` FOREIGN KEY (`instance_id`) REFERENCES `user_vm` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_security_group_vm_map___security_group_id` FOREIGN KEY (`security_group_id`) REFERENCES `security_group` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

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
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `security_ingress_rule` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `security_group_id` bigint(20) unsigned NOT NULL,
  `start_port` varchar(10) DEFAULT NULL,
  `end_port` varchar(10) DEFAULT NULL,
  `protocol` varchar(16) NOT NULL DEFAULT 'TCP',
  `allowed_network_id` bigint(20) unsigned DEFAULT NULL,
  `allowed_ip_cidr` varchar(44) DEFAULT NULL,
  `create_status` varchar(32) DEFAULT NULL COMMENT 'rule creation status',
  PRIMARY KEY (`id`),
  KEY `i_security_ingress_rule_network_id` (`security_group_id`),
  KEY `i_security_ingress_rule_allowed_network` (`allowed_network_id`),
  CONSTRAINT `fk_security_ingress_rule___allowed_network_id` FOREIGN KEY (`allowed_network_id`) REFERENCES `security_group` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_security_ingress_rule___security_group_id` FOREIGN KEY (`security_group_id`) REFERENCES `security_group` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

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
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sequence` (
  `name` varchar(64) NOT NULL COMMENT 'name of the sequence',
  `value` bigint(20) unsigned NOT NULL COMMENT 'sequence value',
  PRIMARY KEY (`name`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sequence`
--

LOCK TABLES `sequence` WRITE;
/*!40000 ALTER TABLE `sequence` DISABLE KEYS */;
INSERT INTO `sequence` VALUES ('checkpoint_seq',1),('networks_seq',210),('private_mac_address_seq',1),('public_mac_address_seq',1),('storage_pool_seq',201),('vm_instance_seq',15),('vm_template_seq',202),('volume_seq',1);
/*!40000 ALTER TABLE `sequence` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `service_offering`
--

DROP TABLE IF EXISTS `service_offering`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `service_offering` (
  `id` bigint(20) unsigned NOT NULL,
  `cpu` int(10) unsigned NOT NULL COMMENT '# of cores',
  `speed` int(10) unsigned NOT NULL COMMENT 'speed per core in mhz',
  `ram_size` bigint(20) unsigned NOT NULL,
  `nw_rate` smallint(5) unsigned DEFAULT '200' COMMENT 'network rate throttle mbits/s',
  `mc_rate` smallint(5) unsigned DEFAULT '10' COMMENT 'mcast rate throttle mbits/s',
  `ha_enabled` tinyint(1) unsigned NOT NULL DEFAULT '0' COMMENT 'Enable HA',
  `limit_cpu_use` tinyint(1) unsigned NOT NULL DEFAULT '0' COMMENT 'Limit the CPU usage to service offering',
  `host_tag` varchar(255) DEFAULT NULL COMMENT 'host tag specified by the service_offering',
  `default_use` tinyint(1) unsigned NOT NULL DEFAULT '0' COMMENT 'is this offering a default system offering',
  `vm_type` varchar(32) DEFAULT NULL COMMENT 'type of offering specified for system offerings',
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_service_offering__id` FOREIGN KEY (`id`) REFERENCES `disk_offering` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `service_offering`
--

LOCK TABLES `service_offering` WRITE;
/*!40000 ALTER TABLE `service_offering` DISABLE KEYS */;
INSERT INTO `service_offering` VALUES (1,1,500,512,NULL,NULL,0,0,NULL,0,NULL),(2,1,2000,2048,NULL,NULL,0,0,NULL,0,NULL),(3,2,2000,4096,NULL,NULL,0,0,NULL,0,NULL),(7,1,500,512,NULL,NULL,0,0,NULL,0,NULL),(8,1,1000,1024,NULL,NULL,0,0,NULL,0,NULL),(12,1,500,256,NULL,NULL,0,0,NULL,1,'secondarystoragevm'),(13,1,500,128,NULL,NULL,1,0,NULL,1,'domainrouter'),(14,1,500,1024,0,0,0,0,NULL,1,'consoleproxy'),(15,1,128,128,0,0,1,0,NULL,1,'elasticloadbalancervm');
/*!40000 ALTER TABLE `service_offering` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `snapshot_policy`
--

DROP TABLE IF EXISTS `snapshot_policy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `snapshot_policy` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `volume_id` bigint(20) unsigned NOT NULL,
  `schedule` varchar(100) NOT NULL COMMENT 'schedule time of execution',
  `timezone` varchar(100) NOT NULL COMMENT 'the timezone in which the schedule time is specified',
  `interval` int(4) NOT NULL DEFAULT '4' COMMENT 'backup schedule, e.g. hourly, daily, etc.',
  `max_snaps` int(8) NOT NULL DEFAULT '0' COMMENT 'maximum number of snapshots to maintain',
  `active` tinyint(1) unsigned NOT NULL COMMENT 'Is the policy active',
  PRIMARY KEY (`id`),
  KEY `i_snapshot_policy__volume_id` (`volume_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `snapshot_policy`
--

LOCK TABLES `snapshot_policy` WRITE;
/*!40000 ALTER TABLE `snapshot_policy` DISABLE KEYS */;
/*!40000 ALTER TABLE `snapshot_policy` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `snapshot_schedule`
--

DROP TABLE IF EXISTS `snapshot_schedule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `snapshot_schedule` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `volume_id` bigint(20) unsigned NOT NULL COMMENT 'The volume for which this snapshot is being taken',
  `policy_id` bigint(20) unsigned NOT NULL COMMENT 'One of the policyIds for which this snapshot was taken',
  `scheduled_timestamp` datetime NOT NULL COMMENT 'Time at which the snapshot was scheduled for execution',
  `async_job_id` bigint(20) unsigned DEFAULT NULL COMMENT 'If this schedule is being executed, it is the id of the create aysnc_job. Before that it is null',
  `snapshot_id` bigint(20) unsigned DEFAULT NULL COMMENT 'If this schedule is being executed, then the corresponding snapshot has this id. Before that it is null',
  PRIMARY KEY (`id`),
  UNIQUE KEY `volume_id` (`volume_id`,`policy_id`),
  KEY `i_snapshot_schedule__volume_id` (`volume_id`),
  KEY `i_snapshot_schedule__policy_id` (`policy_id`),
  KEY `i_snapshot_schedule__async_job_id` (`async_job_id`),
  KEY `i_snapshot_schedule__snapshot_id` (`snapshot_id`),
  KEY `i_snapshot_schedule__scheduled_timestamp` (`scheduled_timestamp`),
  CONSTRAINT `fk__snapshot_schedule_snapshot_id` FOREIGN KEY (`snapshot_id`) REFERENCES `snapshots` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk__snapshot_schedule_async_job_id` FOREIGN KEY (`async_job_id`) REFERENCES `async_job` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk__snapshot_schedule_policy_id` FOREIGN KEY (`policy_id`) REFERENCES `snapshot_policy` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk__snapshot_schedule_volume_id` FOREIGN KEY (`volume_id`) REFERENCES `volumes` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

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
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `snapshots` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
  `data_center_id` bigint(20) unsigned NOT NULL,
  `account_id` bigint(20) unsigned NOT NULL COMMENT 'owner.  foreign key to account table',
  `domain_id` bigint(20) unsigned NOT NULL COMMENT 'the domain that the owner belongs to',
  `volume_id` bigint(20) unsigned NOT NULL COMMENT 'volume it belongs to. foreign key to volume table',
  `disk_offering_id` bigint(20) unsigned NOT NULL COMMENT 'from original volume',
  `status` varchar(32) DEFAULT NULL COMMENT 'snapshot creation status',
  `path` varchar(255) DEFAULT NULL COMMENT 'Path',
  `name` varchar(255) NOT NULL COMMENT 'snapshot name',
  `snapshot_type` int(4) NOT NULL COMMENT 'type of snapshot, e.g. manual, recurring',
  `type_description` varchar(25) DEFAULT NULL COMMENT 'description of the type of snapshot, e.g. manual, recurring',
  `size` bigint(20) unsigned NOT NULL COMMENT 'original disk size of snapshot',
  `created` datetime DEFAULT NULL COMMENT 'Date Created',
  `removed` datetime DEFAULT NULL COMMENT 'Date removed.  not null if removed',
  `backup_snap_id` varchar(255) DEFAULT NULL COMMENT 'Back up uuid of the snapshot',
  `swift_id` bigint(20) unsigned DEFAULT NULL COMMENT 'which swift',
  `swift_name` varchar(255) DEFAULT NULL COMMENT 'Back up name in swift',
  `sechost_id` bigint(20) unsigned DEFAULT NULL COMMENT 'secondary storage host id',
  `prev_snap_id` bigint(20) unsigned DEFAULT NULL COMMENT 'Id of the most recent snapshot',
  `hypervisor_type` varchar(32) NOT NULL COMMENT 'hypervisor that the snapshot was taken under',
  `version` varchar(32) DEFAULT NULL COMMENT 'snapshot version',
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`),
  KEY `i_snapshots__removed` (`removed`),
  KEY `i_snapshots__account_id` (`account_id`),
  KEY `i_snapshots__volume_id` (`volume_id`),
  KEY `i_snapshots__name` (`name`),
  KEY `i_snapshots__snapshot_type` (`snapshot_type`),
  KEY `i_snapshots__prev_snap_id` (`prev_snap_id`),
  CONSTRAINT `fk_snapshots__account_id` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `snapshots`
--

LOCK TABLES `snapshots` WRITE;
/*!40000 ALTER TABLE `snapshots` DISABLE KEYS */;
/*!40000 ALTER TABLE `snapshots` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ssh_keypairs`
--

DROP TABLE IF EXISTS `ssh_keypairs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ssh_keypairs` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
  `account_id` bigint(20) unsigned NOT NULL COMMENT 'owner, foreign key to account table',
  `domain_id` bigint(20) unsigned NOT NULL COMMENT 'domain, foreign key to domain table',
  `keypair_name` varchar(256) NOT NULL COMMENT 'name of the key pair',
  `fingerprint` varchar(128) NOT NULL COMMENT 'fingerprint for the ssh public key',
  `public_key` varchar(5120) NOT NULL COMMENT 'public key of the ssh key pair',
  PRIMARY KEY (`id`),
  KEY `fk_ssh_keypairs__account_id` (`account_id`),
  KEY `fk_ssh_keypairs__domain_id` (`domain_id`),
  CONSTRAINT `fk_ssh_keypairs__domain_id` FOREIGN KEY (`domain_id`) REFERENCES `domain` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_ssh_keypairs__account_id` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

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
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `stack_maid` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `msid` bigint(20) unsigned NOT NULL,
  `thread_id` bigint(20) unsigned NOT NULL,
  `seq` int(10) unsigned NOT NULL,
  `cleanup_delegate` varchar(128) DEFAULT NULL,
  `cleanup_context` text,
  `created` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `i_stack_maid_msid_thread_id` (`msid`,`thread_id`),
  KEY `i_stack_maid_seq` (`msid`,`seq`),
  KEY `i_stack_maid_created` (`created`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

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
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `storage_pool` (
  `id` bigint(20) unsigned NOT NULL,
  `name` varchar(255) DEFAULT NULL COMMENT 'should be NOT NULL',
  `uuid` varchar(255) DEFAULT NULL,
  `pool_type` varchar(32) NOT NULL,
  `port` int(10) unsigned NOT NULL,
  `data_center_id` bigint(20) unsigned NOT NULL,
  `pod_id` bigint(20) unsigned DEFAULT NULL,
  `cluster_id` bigint(20) unsigned DEFAULT NULL COMMENT 'foreign key to cluster',
  `available_bytes` bigint(20) unsigned DEFAULT NULL,
  `capacity_bytes` bigint(20) unsigned DEFAULT NULL,
  `host_address` varchar(255) NOT NULL COMMENT 'FQDN or IP of storage server',
  `path` varchar(255) NOT NULL COMMENT 'Filesystem path that is shared',
  `created` datetime DEFAULT NULL COMMENT 'date the pool created',
  `removed` datetime DEFAULT NULL COMMENT 'date removed if not null',
  `update_time` datetime DEFAULT NULL,
  `status` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`),
  UNIQUE KEY `uuid` (`uuid`),
  KEY `i_storage_pool__pod_id` (`pod_id`),
  KEY `fk_storage_pool__cluster_id` (`cluster_id`),
  KEY `i_storage_pool__removed` (`removed`),
  CONSTRAINT `fk_storage_pool__pod_id` FOREIGN KEY (`pod_id`) REFERENCES `host_pod_ref` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_storage_pool__cluster_id` FOREIGN KEY (`cluster_id`) REFERENCES `cluster` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `storage_pool`
--

LOCK TABLES `storage_pool` WRITE;
/*!40000 ALTER TABLE `storage_pool` DISABLE KEYS */;
INSERT INTO `storage_pool` VALUES (200,'primary storage 1','bfc22261-8b98-3dce-81f6-323ec590bd4d','NetworkFilesystem',2049,1,1,1,1277654269952,11810778316800,'10.223.110.231','/export/home/alena/secondary','2012-02-28 19:29:57',NULL,NULL,'Up');
/*!40000 ALTER TABLE `storage_pool` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `storage_pool_details`
--

DROP TABLE IF EXISTS `storage_pool_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `storage_pool_details` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
  `pool_id` bigint(20) unsigned NOT NULL COMMENT 'pool the detail is related to',
  `name` varchar(255) NOT NULL COMMENT 'name of the detail',
  `value` varchar(255) NOT NULL COMMENT 'value of the detail',
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`),
  KEY `fk_storage_pool_details__pool_id` (`pool_id`),
  KEY `i_storage_pool_details__name__value` (`name`(128),`value`(128)),
  CONSTRAINT `fk_storage_pool_details__pool_id` FOREIGN KEY (`pool_id`) REFERENCES `storage_pool` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `storage_pool_details`
--

LOCK TABLES `storage_pool_details` WRITE;
/*!40000 ALTER TABLE `storage_pool_details` DISABLE KEYS */;
INSERT INTO `storage_pool_details` VALUES (1,200,'alenatags','true');
/*!40000 ALTER TABLE `storage_pool_details` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `storage_pool_host_ref`
--

DROP TABLE IF EXISTS `storage_pool_host_ref`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `storage_pool_host_ref` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `host_id` bigint(20) unsigned NOT NULL,
  `pool_id` bigint(20) unsigned NOT NULL,
  `created` datetime NOT NULL,
  `last_updated` datetime DEFAULT NULL,
  `local_path` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_storage_pool_host_ref__host_id` (`host_id`),
  KEY `fk_storage_pool_host_ref__pool_id` (`pool_id`),
  CONSTRAINT `fk_storage_pool_host_ref__host_id` FOREIGN KEY (`host_id`) REFERENCES `host` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_storage_pool_host_ref__pool_id` FOREIGN KEY (`pool_id`) REFERENCES `storage_pool` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `storage_pool_host_ref`
--

LOCK TABLES `storage_pool_host_ref` WRITE;
/*!40000 ALTER TABLE `storage_pool_host_ref` DISABLE KEYS */;
INSERT INTO `storage_pool_host_ref` VALUES (1,2,200,'2012-02-28 19:29:59',NULL,'/mnt/bfc22261-8b98-3dce-81f6-323ec590bd4d');
/*!40000 ALTER TABLE `storage_pool_host_ref` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `storage_pool_work`
--

DROP TABLE IF EXISTS `storage_pool_work`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `storage_pool_work` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
  `pool_id` bigint(20) unsigned NOT NULL COMMENT 'storage pool associated with the vm',
  `vm_id` bigint(20) unsigned NOT NULL COMMENT 'vm identifier',
  `stopped_for_maintenance` tinyint(3) unsigned NOT NULL DEFAULT '0' COMMENT 'this flag denoted whether the vm was stopped during maintenance',
  `started_after_maintenance` tinyint(3) unsigned NOT NULL DEFAULT '0' COMMENT 'this flag denoted whether the vm was started after maintenance',
  `mgmt_server_id` bigint(20) unsigned NOT NULL COMMENT 'management server id',
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`),
  UNIQUE KEY `pool_id` (`pool_id`,`vm_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `storage_pool_work`
--

LOCK TABLES `storage_pool_work` WRITE;
/*!40000 ALTER TABLE `storage_pool_work` DISABLE KEYS */;
/*!40000 ALTER TABLE `storage_pool_work` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `swift`
--

DROP TABLE IF EXISTS `swift`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `swift` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `hostname` varchar(255) DEFAULT NULL,
  `account` varchar(255) DEFAULT NULL COMMENT ' account in swift',
  `username` varchar(255) DEFAULT NULL COMMENT ' username in swift',
  `token` varchar(255) DEFAULT NULL COMMENT 'token for this user',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `swift`
--

LOCK TABLES `swift` WRITE;
/*!40000 ALTER TABLE `swift` DISABLE KEYS */;
/*!40000 ALTER TABLE `swift` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sync_queue`
--

DROP TABLE IF EXISTS `sync_queue`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sync_queue` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `sync_objtype` varchar(64) NOT NULL,
  `sync_objid` bigint(20) unsigned NOT NULL,
  `queue_proc_msid` bigint(20) DEFAULT NULL,
  `queue_proc_number` bigint(20) DEFAULT NULL COMMENT 'process number, increase 1 for each iteration',
  `queue_proc_time` datetime DEFAULT NULL COMMENT 'last time to process the queue',
  `created` datetime DEFAULT NULL COMMENT 'date created',
  `last_updated` datetime DEFAULT NULL COMMENT 'date created',
  PRIMARY KEY (`id`),
  UNIQUE KEY `i_sync_queue__objtype__objid` (`sync_objtype`,`sync_objid`),
  KEY `i_sync_queue__created` (`created`),
  KEY `i_sync_queue__last_updated` (`last_updated`),
  KEY `i_sync_queue__queue_proc_time` (`queue_proc_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

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
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sync_queue_item` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `queue_id` bigint(20) unsigned NOT NULL,
  `content_type` varchar(64) DEFAULT NULL,
  `content_id` bigint(20) DEFAULT NULL,
  `queue_proc_msid` bigint(20) DEFAULT NULL COMMENT 'owner msid when the queue item is being processed',
  `queue_proc_number` bigint(20) DEFAULT NULL COMMENT 'used to distinguish raw items and items being in process',
  `created` datetime DEFAULT NULL COMMENT 'time created',
  PRIMARY KEY (`id`),
  KEY `i_sync_queue_item__queue_id` (`queue_id`),
  KEY `i_sync_queue_item__created` (`created`),
  KEY `i_sync_queue_item__queue_proc_number` (`queue_proc_number`),
  KEY `i_sync_queue_item__queue_proc_msid` (`queue_proc_msid`),
  CONSTRAINT `fk_sync_queue_item__queue_id` FOREIGN KEY (`queue_id`) REFERENCES `sync_queue` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

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
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `template_host_ref` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `host_id` bigint(20) unsigned NOT NULL,
  `pool_id` bigint(20) unsigned DEFAULT NULL,
  `template_id` bigint(20) unsigned NOT NULL,
  `created` datetime NOT NULL,
  `last_updated` datetime DEFAULT NULL,
  `job_id` varchar(255) DEFAULT NULL,
  `download_pct` int(10) unsigned DEFAULT NULL,
  `size` bigint(20) unsigned DEFAULT NULL,
  `physical_size` bigint(20) unsigned DEFAULT '0',
  `download_state` varchar(255) DEFAULT NULL,
  `error_str` varchar(255) DEFAULT NULL,
  `local_path` varchar(255) DEFAULT NULL,
  `install_path` varchar(255) DEFAULT NULL,
  `url` varchar(255) DEFAULT NULL,
  `destroyed` tinyint(1) DEFAULT NULL COMMENT 'indicates whether the template_host entry was destroyed by the user or not',
  `is_copy` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'indicates whether this was copied ',
  PRIMARY KEY (`id`),
  KEY `i_template_host_ref__host_id` (`host_id`),
  KEY `i_template_host_ref__template_id` (`template_id`),
  CONSTRAINT `fk_template_host_ref__host_id` FOREIGN KEY (`host_id`) REFERENCES `host` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_template_host_ref__template_id` FOREIGN KEY (`template_id`) REFERENCES `vm_template` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `template_host_ref`
--

LOCK TABLES `template_host_ref` WRITE;
/*!40000 ALTER TABLE `template_host_ref` DISABLE KEYS */;
INSERT INTO `template_host_ref` VALUES (1,1,NULL,9,'2012-02-28 19:29:52','2012-02-28 19:29:52',NULL,100,0,0,'DOWNLOADED',NULL,NULL,'template/tmpl/1/9/','http://download.cloud.com/releases/2.2.0/systemvm.vhd.bz2',0,0),(2,1,NULL,8,'2012-02-28 19:29:52','2012-02-28 19:29:52',NULL,100,0,0,'DOWNLOADED',NULL,NULL,'template/tmpl/1/8/','http://download.cloud.com/releases/2.2.0/systemvm.ova',0,0),(3,1,NULL,3,'2012-02-28 19:29:52','2012-02-28 21:43:24',NULL,100,757071872,757071872,'DOWNLOADED',NULL,NULL,'template/tmpl/1/3//adb6033f-c806-4884-938a-1ec398fc5ff0.qcow2','http://download.cloud.com/releases/2.2.0/systemvm.qcow2.bz2',0,0),(4,1,NULL,1,'2012-02-28 19:29:52','2012-02-28 21:43:24',NULL,100,2101252608,2101252608,'DOWNLOADED',NULL,NULL,'template/tmpl/1/1//2f097f45-4a83-4bfc-be00-5e0474b0af51.vhd','http://download.cloud.com/releases/2.2.0/systemvm.vhd.bz2',0,0),(5,1,NULL,2,'2012-02-28 19:33:04','2012-02-28 21:43:24','088be720-b912-47cc-92a4-2cd5f42cd2fe',100,8589934592,1708331520,'DOWNLOADED','','/mnt/SecStorage/b65c59d9-5204-353f-b409-ba7af91f7fe6/template/tmpl/1/2/dnld2518836130830637987tmp_','template/tmpl/1/2//6746f34b-f49e-3c83-827e-951b11e8d01f.vhd','http://download.cloud.com/templates/builtin/f59f18fb-ae94-4f97-afd2-f84755767aca.vhd.bz2',0,0),(6,1,NULL,4,'2012-02-28 19:33:04','2012-02-28 19:33:04',NULL,0,0,0,'NOT_DOWNLOADED',NULL,NULL,NULL,'http://download.cloud.com/releases/2.2.0/eec2209b-9875-3c8d-92be-c001bd8a0faf.qcow2.bz2',0,0),(7,1,NULL,7,'2012-02-28 19:33:04','2012-02-28 19:33:04',NULL,0,0,0,'NOT_DOWNLOADED',NULL,NULL,NULL,'http://download.cloud.com/releases/2.2.0/CentOS5.3-x86_64.ova',0,0);
/*!40000 ALTER TABLE `template_host_ref` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `template_spool_ref`
--

DROP TABLE IF EXISTS `template_spool_ref`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `template_spool_ref` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `pool_id` bigint(20) unsigned NOT NULL,
  `template_id` bigint(20) unsigned NOT NULL,
  `created` datetime NOT NULL,
  `last_updated` datetime DEFAULT NULL,
  `job_id` varchar(255) DEFAULT NULL,
  `download_pct` int(10) unsigned DEFAULT NULL,
  `download_state` varchar(255) DEFAULT NULL,
  `error_str` varchar(255) DEFAULT NULL,
  `local_path` varchar(255) DEFAULT NULL,
  `install_path` varchar(255) DEFAULT NULL,
  `template_size` bigint(20) unsigned NOT NULL COMMENT 'the size of the template on the pool',
  `marked_for_gc` tinyint(1) unsigned NOT NULL DEFAULT '0' COMMENT 'if true, the garbage collector will evict the template from this pool.',
  PRIMARY KEY (`id`),
  UNIQUE KEY `i_template_spool_ref__template_id__pool_id` (`template_id`,`pool_id`),
  KEY `fk_template_spool_ref__pool_id` (`pool_id`),
  CONSTRAINT `fk_template_spool_ref__template_id` FOREIGN KEY (`template_id`) REFERENCES `vm_template` (`id`),
  CONSTRAINT `fk_template_spool_ref__pool_id` FOREIGN KEY (`pool_id`) REFERENCES `storage_pool` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `template_spool_ref`
--

LOCK TABLES `template_spool_ref` WRITE;
/*!40000 ALTER TABLE `template_spool_ref` DISABLE KEYS */;
INSERT INTO `template_spool_ref` VALUES (2,200,1,'2012-02-28 19:30:42',NULL,NULL,100,'DOWNLOADED',NULL,'2fb887df-c6b4-496b-8163-1640dfd83103','2fb887df-c6b4-496b-8163-1640dfd83103',2101252608,0),(3,200,2,'2012-02-28 21:45:11',NULL,NULL,100,'DOWNLOADED',NULL,'2bae5d45-cb42-4d29-bacc-ff41b4315772','2bae5d45-cb42-4d29-bacc-ff41b4315772',1708331520,0);
/*!40000 ALTER TABLE `template_spool_ref` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `template_zone_ref`
--

DROP TABLE IF EXISTS `template_zone_ref`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `template_zone_ref` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `zone_id` bigint(20) unsigned NOT NULL,
  `template_id` bigint(20) unsigned NOT NULL,
  `created` datetime NOT NULL,
  `last_updated` datetime DEFAULT NULL,
  `removed` datetime DEFAULT NULL COMMENT 'date removed if not null',
  PRIMARY KEY (`id`),
  KEY `i_template_zone_ref__zone_id` (`zone_id`),
  KEY `i_template_zone_ref__template_id` (`template_id`),
  KEY `i_template_zone_ref__removed` (`removed`),
  CONSTRAINT `fk_template_zone_ref__zone_id` FOREIGN KEY (`zone_id`) REFERENCES `data_center` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_template_zone_ref__template_id` FOREIGN KEY (`template_id`) REFERENCES `vm_template` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `template_zone_ref`
--

LOCK TABLES `template_zone_ref` WRITE;
/*!40000 ALTER TABLE `template_zone_ref` DISABLE KEYS */;
INSERT INTO `template_zone_ref` VALUES (1,1,1,'2012-02-28 19:29:52','2012-02-28 19:29:52',NULL),(2,1,2,'2012-02-28 19:29:52','2012-02-28 19:33:04',NULL),(3,1,3,'2012-02-28 19:29:52','2012-02-28 19:29:52',NULL),(4,1,4,'2012-02-28 19:29:52','2012-02-28 19:33:04',NULL),(5,1,7,'2012-02-28 19:29:52','2012-02-28 19:33:04',NULL),(6,1,8,'2012-02-28 19:29:52','2012-02-28 19:29:52',NULL),(7,1,9,'2012-02-28 19:29:52','2012-02-28 19:29:52',NULL);
/*!40000 ALTER TABLE `template_zone_ref` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `upload`
--

DROP TABLE IF EXISTS `upload`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `upload` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `host_id` bigint(20) unsigned NOT NULL,
  `type_id` bigint(20) unsigned NOT NULL,
  `type` varchar(255) DEFAULT NULL,
  `mode` varchar(255) DEFAULT NULL,
  `created` datetime NOT NULL,
  `last_updated` datetime DEFAULT NULL,
  `job_id` varchar(255) DEFAULT NULL,
  `upload_pct` int(10) unsigned DEFAULT NULL,
  `upload_state` varchar(255) DEFAULT NULL,
  `error_str` varchar(255) DEFAULT NULL,
  `url` varchar(255) DEFAULT NULL,
  `install_path` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `i_upload__host_id` (`host_id`),
  KEY `i_upload__type_id` (`type_id`),
  CONSTRAINT `fk_upload__host_id` FOREIGN KEY (`host_id`) REFERENCES `host` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

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
) ENGINE=InnoDB AUTO_INCREMENT=46 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usage_event`
--

LOCK TABLES `usage_event` WRITE;
/*!40000 ALTER TABLE `usage_event` DISABLE KEYS */;
INSERT INTO `usage_event` VALUES (1,'VOLUME.CREATE',2,'2012-02-28 21:40:32',1,3,'ROOT-3',NULL,2,8589934592,NULL,0),(2,'VM.CREATE',2,'2012-02-28 21:40:32',1,3,'i-2-3-TEST',7,2,NULL,'XenServer',0),(3,'VM.DESTROY',2,'2012-02-28 21:40:33',1,3,'i-2-3-TEST',7,2,NULL,'XenServer',0),(4,'VOLUME.DELETE',2,'2012-02-28 21:40:33',1,3,'ROOT-3',NULL,NULL,NULL,NULL,0),(5,'NET.IPASSIGN',2,'2012-02-28 21:40:40',1,123,'172.21.0.100',NULL,NULL,0,'Direct',0),(6,'NET.IPASSIGN',2,'2012-02-28 21:40:40',1,144,'172.22.0.100',NULL,NULL,0,'Direct',0),(7,'NET.IPASSIGN',2,'2012-02-28 21:40:40',1,102,'172.20.0.100',NULL,NULL,0,'Direct',0),(8,'VOLUME.CREATE',2,'2012-02-28 21:40:40',1,4,'ROOT-4',NULL,2,8589934592,NULL,0),(9,'VM.CREATE',2,'2012-02-28 21:40:40',1,4,'i-2-4-TEST',7,2,NULL,'XenServer',0),(10,'VM.DESTROY',2,'2012-02-28 21:40:40',1,4,'i-2-4-TEST',7,2,NULL,'XenServer',0),(11,'VOLUME.DELETE',2,'2012-02-28 21:40:40',1,4,'ROOT-4',NULL,NULL,NULL,NULL,0),(12,'VOLUME.CREATE',2,'2012-02-28 21:42:08',1,5,'ROOT-5',NULL,2,8589934592,NULL,0),(13,'VM.CREATE',2,'2012-02-28 21:42:08',1,5,'i-2-5-TEST',7,2,NULL,'XenServer',0),(14,'VM.DESTROY',2,'2012-02-28 21:42:08',1,5,'i-2-5-TEST',7,2,NULL,'XenServer',0),(15,'VOLUME.DELETE',2,'2012-02-28 21:42:08',1,5,'ROOT-5',NULL,NULL,NULL,NULL,0),(16,'VOLUME.CREATE',2,'2012-02-28 21:44:11',1,6,'ROOT-6',NULL,2,8589934592,NULL,0),(17,'VM.CREATE',2,'2012-02-28 21:44:11',1,6,'i-2-6-st',7,2,NULL,'XenServer',0),(18,'NET.IPASSIGN',2,'2012-02-28 21:44:12',1,1,'10.223.120.100',NULL,NULL,1,'',0),(19,'NET.IPRELEASE',2,'2012-02-28 21:44:19',1,123,'172.21.0.100',NULL,NULL,0,'Direct',0),(20,'NET.IPRELEASE',2,'2012-02-28 21:44:19',1,144,'172.22.0.100',NULL,NULL,0,'Direct',0),(21,'NET.IPRELEASE',2,'2012-02-28 21:44:19',1,102,'172.20.0.100',NULL,NULL,0,'Direct',0),(22,'NET.IPASSIGN',2,'2012-02-28 21:44:33',1,123,'172.21.0.100',NULL,NULL,0,'Direct',0),(23,'NET.IPASSIGN',2,'2012-02-28 21:44:33',1,144,'172.22.0.100',NULL,NULL,0,'Direct',0),(24,'NET.IPASSIGN',2,'2012-02-28 21:44:33',1,102,'172.20.0.100',NULL,NULL,0,'Direct',0),(25,'VOLUME.CREATE',2,'2012-02-28 21:44:33',1,8,'ROOT-8',NULL,2,8589934592,NULL,0),(26,'VM.CREATE',2,'2012-02-28 21:44:33',1,8,'i-2-8-st',7,2,NULL,'XenServer',0),(27,'NETWORK.OFFERING.ASSIGN',2,'2012-02-28 21:47:18',1,6,'i-2-6-st',6,NULL,1,NULL,0),(28,'VM.START',2,'2012-02-28 21:47:18',1,6,'i-2-6-st',7,2,NULL,'XenServer',0),(29,'NET.IPASSIGN',3,'2012-02-28 21:47:26',1,165,'172.23.0.100',NULL,NULL,0,'Direct',0),(30,'NET.IPASSIGN',3,'2012-02-28 21:47:26',1,125,'172.21.0.102',NULL,NULL,0,'Direct',0),(31,'NET.IPASSIGN',3,'2012-02-28 21:47:26',1,145,'172.22.0.101',NULL,NULL,0,'Direct',0),(32,'VOLUME.CREATE',3,'2012-02-28 21:47:26',1,10,'ROOT-10',NULL,2,8589934592,NULL,0),(33,'VM.CREATE',3,'2012-02-28 21:47:26',1,10,'i-3-10-st',1,2,NULL,'XenServer',0),(34,'NET.IPASSIGN',3,'2012-02-28 21:47:26',1,100,'10.223.120.199',NULL,NULL,1,'',0),(35,'NET.IPASSIGN',2,'2012-02-28 21:49:07',1,103,'172.20.0.101',NULL,NULL,0,'Direct',0),(36,'NETWORK.OFFERING.ASSIGN',2,'2012-02-28 21:51:33',1,8,'i-2-8-st',6,NULL,1,NULL,0),(37,'NETWORK.OFFERING.ASSIGN',2,'2012-02-28 21:51:33',1,8,'i-2-8-st',7,NULL,0,NULL,0),(38,'NETWORK.OFFERING.ASSIGN',2,'2012-02-28 21:51:33',1,8,'i-2-8-st',7,NULL,0,NULL,0),(39,'NETWORK.OFFERING.ASSIGN',2,'2012-02-28 21:51:33',1,8,'i-2-8-st',7,NULL,0,NULL,0),(40,'VM.START',2,'2012-02-28 21:51:33',1,8,'i-2-8-st',7,2,NULL,'XenServer',0),(41,'NETWORK.OFFERING.ASSIGN',3,'2012-02-28 21:51:37',1,10,'i-3-10-st',6,NULL,1,NULL,0),(42,'NETWORK.OFFERING.ASSIGN',3,'2012-02-28 21:51:37',1,10,'i-3-10-st',7,NULL,0,NULL,0),(43,'NETWORK.OFFERING.ASSIGN',3,'2012-02-28 21:51:37',1,10,'i-3-10-st',7,NULL,0,NULL,0),(44,'NETWORK.OFFERING.ASSIGN',3,'2012-02-28 21:51:37',1,10,'i-3-10-st',7,NULL,0,NULL,0),(45,'VM.START',3,'2012-02-28 21:51:37',1,10,'i-3-10-st',1,2,NULL,'XenServer',0);
/*!40000 ALTER TABLE `usage_event` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `username` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `account_id` bigint(20) unsigned NOT NULL,
  `firstname` varchar(255) DEFAULT NULL,
  `lastname` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `state` varchar(10) NOT NULL DEFAULT 'enabled',
  `api_key` varchar(255) DEFAULT NULL,
  `secret_key` varchar(255) DEFAULT NULL,
  `created` datetime NOT NULL COMMENT 'date created',
  `removed` datetime DEFAULT NULL COMMENT 'date removed',
  `timezone` varchar(30) DEFAULT NULL,
  `registration_token` varchar(255) DEFAULT NULL,
  `is_registered` tinyint(4) NOT NULL DEFAULT '0' COMMENT '1: yes, 0: no',
  PRIMARY KEY (`id`),
  UNIQUE KEY `i_user__api_key` (`api_key`),
  KEY `i_user__removed` (`removed`),
  KEY `i_user__secret_key_removed` (`secret_key`,`removed`),
  KEY `i_user__account_id` (`account_id`),
  CONSTRAINT `fk_user__account_id` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'system','',1,'system','cloud',NULL,'enabled',NULL,NULL,'2012-02-28 11:21:37',NULL,NULL,NULL,0),(2,'admin','5f4dcc3b5aa765d61d8327deb882cf99',2,'Admin','User','admin@mailprovider.com','enabled',NULL,NULL,'2012-02-28 11:21:37',NULL,NULL,NULL,0),(3,'1','c4ca4238a0b923820dcc509a6f75849b',3,'1','1','1@gmail.com','enabled',NULL,NULL,'2012-02-28 21:46:21',NULL,NULL,NULL,0);
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_ip_address`
--

DROP TABLE IF EXISTS `user_ip_address`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_ip_address` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `account_id` bigint(20) unsigned DEFAULT NULL,
  `domain_id` bigint(20) unsigned DEFAULT NULL,
  `public_ip_address` char(40) NOT NULL,
  `data_center_id` bigint(20) unsigned NOT NULL COMMENT 'zone that it belongs to',
  `source_nat` int(1) unsigned NOT NULL DEFAULT '0',
  `allocated` datetime DEFAULT NULL COMMENT 'Date this ip was allocated to someone',
  `vlan_db_id` bigint(20) unsigned NOT NULL,
  `one_to_one_nat` int(1) unsigned NOT NULL DEFAULT '0',
  `vm_id` bigint(20) unsigned DEFAULT NULL COMMENT 'vm id the one_to_one nat ip is assigned to',
  `state` char(32) NOT NULL DEFAULT 'Free' COMMENT 'state of the ip address',
  `mac_address` bigint(20) unsigned NOT NULL COMMENT 'mac address of this ip',
  `source_network_id` bigint(20) unsigned NOT NULL COMMENT 'network id ip belongs to',
  `network_id` bigint(20) unsigned DEFAULT NULL COMMENT 'network this public ip address is associated with',
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`),
  UNIQUE KEY `public_ip_address` (`public_ip_address`,`source_network_id`),
  KEY `fk_user_ip_address__source_network_id` (`source_network_id`),
  KEY `fk_user_ip_address__network_id` (`network_id`),
  KEY `fk_user_ip_address__account_id` (`account_id`),
  KEY `fk_user_ip_address__vm_id` (`vm_id`),
  KEY `fk_user_ip_address__vlan_db_id` (`vlan_db_id`),
  KEY `fk_user_ip_address__data_center_id` (`data_center_id`),
  KEY `i_user_ip_address__allocated` (`allocated`),
  KEY `i_user_ip_address__source_nat` (`source_nat`),
  CONSTRAINT `fk_user_ip_address__source_network_id` FOREIGN KEY (`source_network_id`) REFERENCES `networks` (`id`),
  CONSTRAINT `fk_user_ip_address__network_id` FOREIGN KEY (`network_id`) REFERENCES `networks` (`id`),
  CONSTRAINT `fk_user_ip_address__account_id` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`),
  CONSTRAINT `fk_user_ip_address__vm_id` FOREIGN KEY (`vm_id`) REFERENCES `vm_instance` (`id`),
  CONSTRAINT `fk_user_ip_address__vlan_db_id` FOREIGN KEY (`vlan_db_id`) REFERENCES `vlan` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_user_ip_address__data_center_id` FOREIGN KEY (`data_center_id`) REFERENCES `data_center` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=186 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_ip_address`
--

LOCK TABLES `user_ip_address` WRITE;
/*!40000 ALTER TABLE `user_ip_address` DISABLE KEYS */;
INSERT INTO `user_ip_address` VALUES (1,2,1,'10.223.120.100',1,1,'2012-02-28 21:44:12',1,0,NULL,'Allocated',102,200,207),(2,NULL,NULL,'10.223.120.101',1,0,NULL,1,0,NULL,'Free',103,200,NULL),(3,NULL,NULL,'10.223.120.102',1,0,NULL,1,0,NULL,'Free',104,200,NULL),(4,NULL,NULL,'10.223.120.103',1,0,NULL,1,0,NULL,'Free',105,200,NULL),(5,NULL,NULL,'10.223.120.104',1,0,NULL,1,0,NULL,'Free',106,200,NULL),(6,NULL,NULL,'10.223.120.105',1,0,NULL,1,0,NULL,'Free',107,200,NULL),(7,NULL,NULL,'10.223.120.106',1,0,NULL,1,0,NULL,'Free',108,200,NULL),(8,NULL,NULL,'10.223.120.107',1,0,NULL,1,0,NULL,'Free',109,200,NULL),(9,NULL,NULL,'10.223.120.108',1,0,NULL,1,0,NULL,'Free',110,200,NULL),(10,NULL,NULL,'10.223.120.109',1,0,NULL,1,0,NULL,'Free',111,200,NULL),(11,NULL,NULL,'10.223.120.110',1,0,NULL,1,0,NULL,'Free',112,200,NULL),(12,NULL,NULL,'10.223.120.111',1,0,NULL,1,0,NULL,'Free',113,200,NULL),(13,NULL,NULL,'10.223.120.112',1,0,NULL,1,0,NULL,'Free',114,200,NULL),(14,NULL,NULL,'10.223.120.113',1,0,NULL,1,0,NULL,'Free',115,200,NULL),(15,NULL,NULL,'10.223.120.114',1,0,NULL,1,0,NULL,'Free',116,200,NULL),(16,NULL,NULL,'10.223.120.115',1,0,NULL,1,0,NULL,'Free',117,200,NULL),(17,NULL,NULL,'10.223.120.116',1,0,NULL,1,0,NULL,'Free',118,200,NULL),(18,NULL,NULL,'10.223.120.117',1,0,NULL,1,0,NULL,'Free',119,200,NULL),(19,NULL,NULL,'10.223.120.118',1,0,NULL,1,0,NULL,'Free',120,200,NULL),(20,NULL,NULL,'10.223.120.119',1,0,NULL,1,0,NULL,'Free',121,200,NULL),(21,NULL,NULL,'10.223.120.120',1,0,NULL,1,0,NULL,'Free',122,200,NULL),(22,NULL,NULL,'10.223.120.121',1,0,NULL,1,0,NULL,'Free',123,200,NULL),(23,NULL,NULL,'10.223.120.122',1,0,NULL,1,0,NULL,'Free',124,200,NULL),(24,NULL,NULL,'10.223.120.123',1,0,NULL,1,0,NULL,'Free',125,200,NULL),(25,NULL,NULL,'10.223.120.124',1,0,NULL,1,0,NULL,'Free',126,200,NULL),(26,NULL,NULL,'10.223.120.125',1,0,NULL,1,0,NULL,'Free',127,200,NULL),(27,NULL,NULL,'10.223.120.126',1,0,NULL,1,0,NULL,'Free',128,200,NULL),(28,NULL,NULL,'10.223.120.127',1,0,NULL,1,0,NULL,'Free',129,200,NULL),(29,NULL,NULL,'10.223.120.128',1,0,NULL,1,0,NULL,'Free',130,200,NULL),(30,NULL,NULL,'10.223.120.129',1,0,NULL,1,0,NULL,'Free',131,200,NULL),(31,NULL,NULL,'10.223.120.130',1,0,NULL,1,0,NULL,'Free',132,200,NULL),(32,NULL,NULL,'10.223.120.131',1,0,NULL,1,0,NULL,'Free',133,200,NULL),(33,1,1,'10.223.120.132',1,0,'2012-02-28 19:30:42',1,0,NULL,'Allocated',134,200,NULL),(34,NULL,NULL,'10.223.120.133',1,0,NULL,1,0,NULL,'Free',135,200,NULL),(35,NULL,NULL,'10.223.120.134',1,0,NULL,1,0,NULL,'Free',136,200,NULL),(36,NULL,NULL,'10.223.120.135',1,0,NULL,1,0,NULL,'Free',137,200,NULL),(37,NULL,NULL,'10.223.120.136',1,0,NULL,1,0,NULL,'Free',138,200,NULL),(38,NULL,NULL,'10.223.120.137',1,0,NULL,1,0,NULL,'Free',139,200,NULL),(39,NULL,NULL,'10.223.120.138',1,0,NULL,1,0,NULL,'Free',140,200,NULL),(40,NULL,NULL,'10.223.120.139',1,0,NULL,1,0,NULL,'Free',141,200,NULL),(41,NULL,NULL,'10.223.120.140',1,0,NULL,1,0,NULL,'Free',142,200,NULL),(42,NULL,NULL,'10.223.120.141',1,0,NULL,1,0,NULL,'Free',143,200,NULL),(43,NULL,NULL,'10.223.120.142',1,0,NULL,1,0,NULL,'Free',144,200,NULL),(44,NULL,NULL,'10.223.120.143',1,0,NULL,1,0,NULL,'Free',145,200,NULL),(45,NULL,NULL,'10.223.120.144',1,0,NULL,1,0,NULL,'Free',146,200,NULL),(46,NULL,NULL,'10.223.120.145',1,0,NULL,1,0,NULL,'Free',147,200,NULL),(47,NULL,NULL,'10.223.120.146',1,0,NULL,1,0,NULL,'Free',148,200,NULL),(48,NULL,NULL,'10.223.120.147',1,0,NULL,1,0,NULL,'Free',149,200,NULL),(49,NULL,NULL,'10.223.120.148',1,0,NULL,1,0,NULL,'Free',150,200,NULL),(50,NULL,NULL,'10.223.120.149',1,0,NULL,1,0,NULL,'Free',151,200,NULL),(51,NULL,NULL,'10.223.120.150',1,0,NULL,1,0,NULL,'Free',152,200,NULL),(52,NULL,NULL,'10.223.120.151',1,0,NULL,1,0,NULL,'Free',153,200,NULL),(53,NULL,NULL,'10.223.120.152',1,0,NULL,1,0,NULL,'Free',154,200,NULL),(54,NULL,NULL,'10.223.120.153',1,0,NULL,1,0,NULL,'Free',155,200,NULL),(55,NULL,NULL,'10.223.120.154',1,0,NULL,1,0,NULL,'Free',156,200,NULL),(56,NULL,NULL,'10.223.120.155',1,0,NULL,1,0,NULL,'Free',157,200,NULL),(57,NULL,NULL,'10.223.120.156',1,0,NULL,1,0,NULL,'Free',158,200,NULL),(58,NULL,NULL,'10.223.120.157',1,0,NULL,1,0,NULL,'Free',159,200,NULL),(59,NULL,NULL,'10.223.120.158',1,0,NULL,1,0,NULL,'Free',160,200,NULL),(60,NULL,NULL,'10.223.120.159',1,0,NULL,1,0,NULL,'Free',161,200,NULL),(61,NULL,NULL,'10.223.120.160',1,0,NULL,1,0,NULL,'Free',162,200,NULL),(62,NULL,NULL,'10.223.120.161',1,0,NULL,1,0,NULL,'Free',163,200,NULL),(63,NULL,NULL,'10.223.120.162',1,0,NULL,1,0,NULL,'Free',164,200,NULL),(64,NULL,NULL,'10.223.120.163',1,0,NULL,1,0,NULL,'Free',165,200,NULL),(65,NULL,NULL,'10.223.120.164',1,0,NULL,1,0,NULL,'Free',166,200,NULL),(66,1,1,'10.223.120.165',1,0,'2012-02-28 19:30:42',1,0,NULL,'Allocated',167,200,NULL),(67,NULL,NULL,'10.223.120.166',1,0,NULL,1,0,NULL,'Free',168,200,NULL),(68,NULL,NULL,'10.223.120.167',1,0,NULL,1,0,NULL,'Free',169,200,NULL),(69,NULL,NULL,'10.223.120.168',1,0,NULL,1,0,NULL,'Free',170,200,NULL),(70,NULL,NULL,'10.223.120.169',1,0,NULL,1,0,NULL,'Free',171,200,NULL),(71,NULL,NULL,'10.223.120.170',1,0,NULL,1,0,NULL,'Free',172,200,NULL),(72,NULL,NULL,'10.223.120.171',1,0,NULL,1,0,NULL,'Free',173,200,NULL),(73,NULL,NULL,'10.223.120.172',1,0,NULL,1,0,NULL,'Free',174,200,NULL),(74,NULL,NULL,'10.223.120.173',1,0,NULL,1,0,NULL,'Free',175,200,NULL),(75,NULL,NULL,'10.223.120.174',1,0,NULL,1,0,NULL,'Free',176,200,NULL),(76,NULL,NULL,'10.223.120.175',1,0,NULL,1,0,NULL,'Free',177,200,NULL),(77,NULL,NULL,'10.223.120.176',1,0,NULL,1,0,NULL,'Free',178,200,NULL),(78,NULL,NULL,'10.223.120.177',1,0,NULL,1,0,NULL,'Free',179,200,NULL),(79,NULL,NULL,'10.223.120.178',1,0,NULL,1,0,NULL,'Free',180,200,NULL),(80,NULL,NULL,'10.223.120.179',1,0,NULL,1,0,NULL,'Free',181,200,NULL),(81,NULL,NULL,'10.223.120.180',1,0,NULL,1,0,NULL,'Free',182,200,NULL),(82,NULL,NULL,'10.223.120.181',1,0,NULL,1,0,NULL,'Free',183,200,NULL),(83,NULL,NULL,'10.223.120.182',1,0,NULL,1,0,NULL,'Free',184,200,NULL),(84,NULL,NULL,'10.223.120.183',1,0,NULL,1,0,NULL,'Free',185,200,NULL),(85,NULL,NULL,'10.223.120.184',1,0,NULL,1,0,NULL,'Free',186,200,NULL),(86,NULL,NULL,'10.223.120.185',1,0,NULL,1,0,NULL,'Free',187,200,NULL),(87,NULL,NULL,'10.223.120.186',1,0,NULL,1,0,NULL,'Free',188,200,NULL),(88,NULL,NULL,'10.223.120.187',1,0,NULL,1,0,NULL,'Free',189,200,NULL),(89,NULL,NULL,'10.223.120.188',1,0,NULL,1,0,NULL,'Free',190,200,NULL),(90,NULL,NULL,'10.223.120.189',1,0,NULL,1,0,NULL,'Free',191,200,NULL),(91,NULL,NULL,'10.223.120.190',1,0,NULL,1,0,NULL,'Free',192,200,NULL),(92,NULL,NULL,'10.223.120.191',1,0,NULL,1,0,NULL,'Free',193,200,NULL),(93,NULL,NULL,'10.223.120.192',1,0,NULL,1,0,NULL,'Free',194,200,NULL),(94,NULL,NULL,'10.223.120.193',1,0,NULL,1,0,NULL,'Free',195,200,NULL),(95,NULL,NULL,'10.223.120.194',1,0,NULL,1,0,NULL,'Free',196,200,NULL),(96,NULL,NULL,'10.223.120.195',1,0,NULL,1,0,NULL,'Free',197,200,NULL),(97,NULL,NULL,'10.223.120.196',1,0,NULL,1,0,NULL,'Free',198,200,NULL),(98,NULL,NULL,'10.223.120.197',1,0,NULL,1,0,NULL,'Free',199,200,NULL),(99,NULL,NULL,'10.223.120.198',1,0,NULL,1,0,NULL,'Free',200,200,NULL),(100,3,2,'10.223.120.199',1,1,'2012-02-28 21:47:26',1,0,NULL,'Allocated',201,200,209),(101,NULL,NULL,'10.223.120.200',1,0,NULL,1,0,NULL,'Free',202,200,NULL),(102,2,1,'172.20.0.100',1,0,'2012-02-28 21:44:33',2,0,NULL,'Allocated',203,204,NULL),(103,2,1,'172.20.0.101',1,0,'2012-02-28 21:49:07',2,0,NULL,'Allocated',204,204,NULL),(104,NULL,NULL,'172.20.0.102',1,0,NULL,2,0,NULL,'Free',205,204,NULL),(105,NULL,NULL,'172.20.0.103',1,0,NULL,2,0,NULL,'Free',206,204,NULL),(106,NULL,NULL,'172.20.0.104',1,0,NULL,2,0,NULL,'Free',207,204,NULL),(107,NULL,NULL,'172.20.0.105',1,0,NULL,2,0,NULL,'Free',208,204,NULL),(108,NULL,NULL,'172.20.0.106',1,0,NULL,2,0,NULL,'Free',209,204,NULL),(109,NULL,NULL,'172.20.0.107',1,0,NULL,2,0,NULL,'Free',210,204,NULL),(110,NULL,NULL,'172.20.0.108',1,0,NULL,2,0,NULL,'Free',211,204,NULL),(111,NULL,NULL,'172.20.0.109',1,0,NULL,2,0,NULL,'Free',212,204,NULL),(112,NULL,NULL,'172.20.0.110',1,0,NULL,2,0,NULL,'Free',213,204,NULL),(113,NULL,NULL,'172.20.0.111',1,0,NULL,2,0,NULL,'Free',214,204,NULL),(114,NULL,NULL,'172.20.0.112',1,0,NULL,2,0,NULL,'Free',215,204,NULL),(115,NULL,NULL,'172.20.0.113',1,0,NULL,2,0,NULL,'Free',216,204,NULL),(116,NULL,NULL,'172.20.0.114',1,0,NULL,2,0,NULL,'Free',217,204,NULL),(117,NULL,NULL,'172.20.0.115',1,0,NULL,2,0,NULL,'Free',218,204,NULL),(118,NULL,NULL,'172.20.0.116',1,0,NULL,2,0,NULL,'Free',219,204,NULL),(119,NULL,NULL,'172.20.0.117',1,0,NULL,2,0,NULL,'Free',220,204,NULL),(120,NULL,NULL,'172.20.0.118',1,0,NULL,2,0,NULL,'Free',221,204,NULL),(121,NULL,NULL,'172.20.0.119',1,0,NULL,2,0,NULL,'Free',222,204,NULL),(122,NULL,NULL,'172.20.0.120',1,0,NULL,2,0,NULL,'Free',223,204,NULL),(123,2,1,'172.21.0.100',1,0,'2012-02-28 21:44:33',3,0,NULL,'Allocated',224,205,NULL),(124,1,1,'172.21.0.101',1,0,'2012-02-28 21:45:22',3,0,NULL,'Allocated',225,205,NULL),(125,3,2,'172.21.0.102',1,0,'2012-02-28 21:47:26',3,0,NULL,'Allocated',226,205,NULL),(126,NULL,NULL,'172.21.0.103',1,0,NULL,3,0,NULL,'Free',227,205,NULL),(127,NULL,NULL,'172.21.0.104',1,0,NULL,3,0,NULL,'Free',228,205,NULL),(128,NULL,NULL,'172.21.0.105',1,0,NULL,3,0,NULL,'Free',229,205,NULL),(129,NULL,NULL,'172.21.0.106',1,0,NULL,3,0,NULL,'Free',230,205,NULL),(130,NULL,NULL,'172.21.0.107',1,0,NULL,3,0,NULL,'Free',231,205,NULL),(131,NULL,NULL,'172.21.0.108',1,0,NULL,3,0,NULL,'Free',232,205,NULL),(132,NULL,NULL,'172.21.0.109',1,0,NULL,3,0,NULL,'Free',233,205,NULL),(133,NULL,NULL,'172.21.0.110',1,0,NULL,3,0,NULL,'Free',234,205,NULL),(134,NULL,NULL,'172.21.0.111',1,0,NULL,3,0,NULL,'Free',235,205,NULL),(135,NULL,NULL,'172.21.0.112',1,0,NULL,3,0,NULL,'Free',236,205,NULL),(136,NULL,NULL,'172.21.0.113',1,0,NULL,3,0,NULL,'Free',237,205,NULL),(137,NULL,NULL,'172.21.0.114',1,0,NULL,3,0,NULL,'Free',238,205,NULL),(138,NULL,NULL,'172.21.0.115',1,0,NULL,3,0,NULL,'Free',239,205,NULL),(139,NULL,NULL,'172.21.0.116',1,0,NULL,3,0,NULL,'Free',240,205,NULL),(140,NULL,NULL,'172.21.0.117',1,0,NULL,3,0,NULL,'Free',241,205,NULL),(141,NULL,NULL,'172.21.0.118',1,0,NULL,3,0,NULL,'Free',242,205,NULL),(142,NULL,NULL,'172.21.0.119',1,0,NULL,3,0,NULL,'Free',243,205,NULL),(143,NULL,NULL,'172.21.0.120',1,0,NULL,3,0,NULL,'Free',244,205,NULL),(144,2,1,'172.22.0.100',1,0,'2012-02-28 21:44:33',4,0,NULL,'Allocated',245,206,NULL),(145,3,2,'172.22.0.101',1,0,'2012-02-28 21:47:26',4,0,NULL,'Allocated',246,206,NULL),(146,1,1,'172.22.0.102',1,0,'2012-02-28 21:47:29',4,0,NULL,'Allocated',247,206,NULL),(147,NULL,NULL,'172.22.0.103',1,0,NULL,4,0,NULL,'Free',248,206,NULL),(148,NULL,NULL,'172.22.0.104',1,0,NULL,4,0,NULL,'Free',249,206,NULL),(149,NULL,NULL,'172.22.0.105',1,0,NULL,4,0,NULL,'Free',250,206,NULL),(150,NULL,NULL,'172.22.0.106',1,0,NULL,4,0,NULL,'Free',251,206,NULL),(151,NULL,NULL,'172.22.0.107',1,0,NULL,4,0,NULL,'Free',252,206,NULL),(152,NULL,NULL,'172.22.0.108',1,0,NULL,4,0,NULL,'Free',253,206,NULL),(153,NULL,NULL,'172.22.0.109',1,0,NULL,4,0,NULL,'Free',254,206,NULL),(154,NULL,NULL,'172.22.0.110',1,0,NULL,4,0,NULL,'Free',255,206,NULL),(155,NULL,NULL,'172.22.0.111',1,0,NULL,4,0,NULL,'Free',256,206,NULL),(156,NULL,NULL,'172.22.0.112',1,0,NULL,4,0,NULL,'Free',257,206,NULL),(157,NULL,NULL,'172.22.0.113',1,0,NULL,4,0,NULL,'Free',258,206,NULL),(158,NULL,NULL,'172.22.0.114',1,0,NULL,4,0,NULL,'Free',259,206,NULL),(159,NULL,NULL,'172.22.0.115',1,0,NULL,4,0,NULL,'Free',260,206,NULL),(160,NULL,NULL,'172.22.0.116',1,0,NULL,4,0,NULL,'Free',261,206,NULL),(161,NULL,NULL,'172.22.0.117',1,0,NULL,4,0,NULL,'Free',262,206,NULL),(162,NULL,NULL,'172.22.0.118',1,0,NULL,4,0,NULL,'Free',263,206,NULL),(163,NULL,NULL,'172.22.0.119',1,0,NULL,4,0,NULL,'Free',264,206,NULL),(164,NULL,NULL,'172.22.0.120',1,0,NULL,4,0,NULL,'Free',265,206,NULL),(165,3,2,'172.23.0.100',1,0,'2012-02-28 21:47:26',5,0,NULL,'Allocated',266,208,NULL),(166,1,1,'172.23.0.101',1,0,'2012-02-28 21:48:57',5,0,NULL,'Allocated',267,208,NULL),(167,NULL,NULL,'172.23.0.102',1,0,NULL,5,0,NULL,'Free',268,208,NULL),(168,NULL,NULL,'172.23.0.103',1,0,NULL,5,0,NULL,'Free',269,208,NULL),(169,NULL,NULL,'172.23.0.104',1,0,NULL,5,0,NULL,'Free',270,208,NULL),(170,NULL,NULL,'172.23.0.105',1,0,NULL,5,0,NULL,'Free',271,208,NULL),(171,NULL,NULL,'172.23.0.106',1,0,NULL,5,0,NULL,'Free',272,208,NULL),(172,NULL,NULL,'172.23.0.107',1,0,NULL,5,0,NULL,'Free',273,208,NULL),(173,NULL,NULL,'172.23.0.108',1,0,NULL,5,0,NULL,'Free',274,208,NULL),(174,NULL,NULL,'172.23.0.109',1,0,NULL,5,0,NULL,'Free',275,208,NULL),(175,NULL,NULL,'172.23.0.110',1,0,NULL,5,0,NULL,'Free',276,208,NULL),(176,NULL,NULL,'172.23.0.111',1,0,NULL,5,0,NULL,'Free',277,208,NULL),(177,NULL,NULL,'172.23.0.112',1,0,NULL,5,0,NULL,'Free',278,208,NULL),(178,NULL,NULL,'172.23.0.113',1,0,NULL,5,0,NULL,'Free',279,208,NULL),(179,NULL,NULL,'172.23.0.114',1,0,NULL,5,0,NULL,'Free',280,208,NULL),(180,NULL,NULL,'172.23.0.115',1,0,NULL,5,0,NULL,'Free',281,208,NULL),(181,NULL,NULL,'172.23.0.116',1,0,NULL,5,0,NULL,'Free',282,208,NULL),(182,NULL,NULL,'172.23.0.117',1,0,NULL,5,0,NULL,'Free',283,208,NULL),(183,NULL,NULL,'172.23.0.118',1,0,NULL,5,0,NULL,'Free',284,208,NULL),(184,NULL,NULL,'172.23.0.119',1,0,NULL,5,0,NULL,'Free',285,208,NULL),(185,NULL,NULL,'172.23.0.120',1,0,NULL,5,0,NULL,'Free',286,208,NULL);
/*!40000 ALTER TABLE `user_ip_address` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_statistics`
--

DROP TABLE IF EXISTS `user_statistics`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_statistics` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `data_center_id` bigint(20) unsigned NOT NULL,
  `account_id` bigint(20) unsigned NOT NULL,
  `public_ip_address` char(40) DEFAULT NULL,
  `device_id` bigint(20) unsigned NOT NULL,
  `device_type` varchar(32) NOT NULL,
  `network_id` bigint(20) unsigned DEFAULT NULL,
  `net_bytes_received` bigint(20) unsigned NOT NULL DEFAULT '0',
  `net_bytes_sent` bigint(20) unsigned NOT NULL DEFAULT '0',
  `current_bytes_received` bigint(20) unsigned NOT NULL DEFAULT '0',
  `current_bytes_sent` bigint(20) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`),
  UNIQUE KEY `account_id` (`account_id`,`data_center_id`,`public_ip_address`,`device_id`,`device_type`),
  KEY `i_user_statistics__account_id` (`account_id`),
  KEY `i_user_statistics__account_id_data_center_id` (`account_id`,`data_center_id`),
  CONSTRAINT `fk_user_statistics__account_id` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_statistics`
--

LOCK TABLES `user_statistics` WRITE;
/*!40000 ALTER TABLE `user_statistics` DISABLE KEYS */;
INSERT INTO `user_statistics` VALUES (1,1,2,NULL,7,'DomainRouter',207,0,0,0,0),(2,1,1,NULL,9,'DomainRouter',205,0,0,0,0),(3,1,3,NULL,11,'DomainRouter',209,0,0,2516,1275),(4,1,1,NULL,12,'DomainRouter',206,0,0,0,0),(5,1,1,NULL,13,'DomainRouter',208,0,0,0,0),(6,1,2,NULL,14,'DomainRouter',204,0,0,0,0);
/*!40000 ALTER TABLE `user_statistics` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_vm`
--

DROP TABLE IF EXISTS `user_vm`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_vm` (
  `id` bigint(20) unsigned NOT NULL,
  `iso_id` bigint(20) unsigned DEFAULT NULL,
  `display_name` varchar(255) DEFAULT NULL,
  `user_data` varchar(2048) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`),
  CONSTRAINT `fk_user_vm__id` FOREIGN KEY (`id`) REFERENCES `vm_instance` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_vm`
--

LOCK TABLES `user_vm` WRITE;
/*!40000 ALTER TABLE `user_vm` DISABLE KEYS */;
INSERT INTO `user_vm` VALUES (3,NULL,'i-2-3-TEST',NULL),(4,NULL,'i-2-4-TEST',NULL),(5,NULL,'i-2-5-TEST',NULL),(6,NULL,'i-2-6-st',NULL),(8,NULL,'multipleNetworksVm',NULL),(10,NULL,'1svm',NULL);
/*!40000 ALTER TABLE `user_vm` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_vm_details`
--

DROP TABLE IF EXISTS `user_vm_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_vm_details` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `vm_id` bigint(20) unsigned NOT NULL COMMENT 'vm id',
  `name` varchar(255) NOT NULL,
  `value` varchar(1024) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_user_vm_details__vm_id` (`vm_id`),
  CONSTRAINT `fk_user_vm_details__vm_id` FOREIGN KEY (`vm_id`) REFERENCES `vm_instance` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_vm_details`
--

LOCK TABLES `user_vm_details` WRITE;
/*!40000 ALTER TABLE `user_vm_details` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_vm_details` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `version`
--

DROP TABLE IF EXISTS `version`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `version` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
  `version` char(40) NOT NULL COMMENT 'version',
  `updated` datetime NOT NULL COMMENT 'Date this version table was updated',
  `step` char(32) NOT NULL COMMENT 'Step in the upgrade to this version',
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`),
  UNIQUE KEY `version` (`version`),
  KEY `i_version__version` (`version`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `version`
--

LOCK TABLES `version` WRITE;
/*!40000 ALTER TABLE `version` DISABLE KEYS */;
INSERT INTO `version` VALUES (1,'2.2.8.2012-02-28T19:10:16Z','2012-02-28 11:21:35','Complete');
/*!40000 ALTER TABLE `version` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vlan`
--

DROP TABLE IF EXISTS `vlan`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `vlan` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `vlan_id` varchar(255) DEFAULT NULL,
  `vlan_gateway` varchar(255) DEFAULT NULL,
  `vlan_netmask` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `vlan_type` varchar(255) DEFAULT NULL,
  `data_center_id` bigint(20) unsigned NOT NULL,
  `network_id` bigint(20) unsigned NOT NULL COMMENT 'id of corresponding network offering',
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`),
  KEY `fk_vlan__data_center_id` (`data_center_id`),
  CONSTRAINT `fk_vlan__data_center_id` FOREIGN KEY (`data_center_id`) REFERENCES `data_center` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vlan`
--

LOCK TABLES `vlan` WRITE;
/*!40000 ALTER TABLE `vlan` DISABLE KEYS */;
INSERT INTO `vlan` VALUES (1,'196','10.223.120.1','255.255.255.0','10.223.120.100-10.223.120.200','VirtualNetwork',1,200),(2,'20','172.20.0.1','255.255.0.0','172.20.0.100-172.20.0.120','DirectAttached',1,204),(3,'21','172.21.0.1','255.255.0.0','172.21.0.100-172.21.0.120','DirectAttached',1,205),(4,'22','172.22.0.1','255.255.0.0','172.22.0.100-172.22.0.120','DirectAttached',1,206),(5,'23','172.23.0.1','255.255.0.0','172.23.0.100-172.23.0.120','DirectAttached',1,208);
/*!40000 ALTER TABLE `vlan` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vm_instance`
--

DROP TABLE IF EXISTS `vm_instance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `vm_instance` (
  `id` bigint(20) unsigned NOT NULL,
  `name` varchar(255) NOT NULL,
  `instance_name` varchar(255) NOT NULL COMMENT 'name of the vm instance running on the hosts',
  `state` varchar(32) NOT NULL,
  `vm_template_id` bigint(20) unsigned DEFAULT NULL,
  `guest_os_id` bigint(20) unsigned NOT NULL,
  `private_mac_address` varchar(17) DEFAULT NULL,
  `private_ip_address` char(40) DEFAULT NULL,
  `private_netmask` varchar(15) DEFAULT NULL,
  `pod_id` bigint(20) unsigned DEFAULT NULL,
  `data_center_id` bigint(20) unsigned NOT NULL COMMENT 'Data Center the instance belongs to',
  `host_id` bigint(20) unsigned DEFAULT NULL,
  `last_host_id` bigint(20) unsigned DEFAULT NULL COMMENT 'tentative host for first run or last host that it has been running on',
  `proxy_id` bigint(20) unsigned DEFAULT NULL COMMENT 'console proxy allocated in previous session',
  `proxy_assign_time` datetime DEFAULT NULL COMMENT 'time when console proxy was assigned',
  `vnc_password` varchar(255) NOT NULL COMMENT 'vnc password',
  `ha_enabled` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Should HA be enabled for this VM',
  `limit_cpu_use` tinyint(1) unsigned NOT NULL DEFAULT '0' COMMENT 'Limit the cpu usage to service offering',
  `update_count` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT 'date state was updated',
  `update_time` datetime DEFAULT NULL COMMENT 'date the destroy was requested',
  `created` datetime NOT NULL COMMENT 'date created',
  `removed` datetime DEFAULT NULL COMMENT 'date removed if not null',
  `type` varchar(32) NOT NULL COMMENT 'type of vm it is',
  `vm_type` varchar(32) NOT NULL COMMENT 'vm type',
  `account_id` bigint(20) unsigned NOT NULL COMMENT 'user id of owner',
  `domain_id` bigint(20) unsigned NOT NULL,
  `service_offering_id` bigint(20) unsigned NOT NULL COMMENT 'service offering id',
  `reservation_id` char(40) DEFAULT NULL COMMENT 'reservation id',
  `hypervisor_type` char(32) DEFAULT NULL COMMENT 'hypervisor type',
  PRIMARY KEY (`id`),
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
  KEY `fk_vm_instance__account_id` (`account_id`),
  KEY `fk_vm_instance__service_offering_id` (`service_offering_id`),
  CONSTRAINT `fk_vm_instance__host_id` FOREIGN KEY (`host_id`) REFERENCES `host` (`id`),
  CONSTRAINT `fk_vm_instance__last_host_id` FOREIGN KEY (`last_host_id`) REFERENCES `host` (`id`),
  CONSTRAINT `fk_vm_instance__template_id` FOREIGN KEY (`vm_template_id`) REFERENCES `vm_template` (`id`),
  CONSTRAINT `fk_vm_instance__account_id` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`),
  CONSTRAINT `fk_vm_instance__service_offering_id` FOREIGN KEY (`service_offering_id`) REFERENCES `service_offering` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vm_instance`
--

LOCK TABLES `vm_instance` WRITE;
/*!40000 ALTER TABLE `vm_instance` DISABLE KEYS */;
INSERT INTO `vm_instance` VALUES (1,'s-1-TEST','s-1-TEST','Running',1,15,'06:d8:c8:00:00:10','10.223.48.115',NULL,1,1,2,2,NULL,NULL,'5cc396a1f83bc2a3',0,0,3,'2012-02-28 19:33:09','2012-02-28 19:30:42',NULL,'SecondaryStorageVm','SecondaryStorageVm',1,1,12,'c178233a-17c2-441a-98bf-a5591b3eaea7','XenServer'),(2,'v-2-TEST','v-2-TEST','Running',1,15,'06:69:86:00:00:44','10.223.48.167',NULL,1,1,2,2,NULL,NULL,'1128a80c20ebf56c',0,0,3,'2012-02-28 19:33:50','2012-02-28 19:30:42',NULL,'ConsoleProxy','ConsoleProxy',1,1,14,'b9e363d4-075f-46b3-a226-49ad82a3d56e','XenServer'),(3,'i-2-3-TEST','i-2-3-TEST','Expunging',2,12,NULL,NULL,NULL,NULL,1,NULL,NULL,NULL,NULL,'d6df632ca3b36c4c',0,0,4,'2012-02-28 21:44:19','2012-02-28 21:40:32','2012-02-28 21:44:19','User','User',2,1,7,'3b59b06c-6048-471e-a874-0c076e74aa8d','XenServer'),(4,'i-2-4-TEST','i-2-4-TEST','Expunging',2,12,NULL,NULL,NULL,NULL,1,NULL,NULL,NULL,NULL,'d5bd2fc7074feac6',0,0,4,'2012-02-28 21:44:19','2012-02-28 21:40:40','2012-02-28 21:44:19','User','User',2,1,7,'ea4b5549-d7ea-4574-8df8-b6f0f2a4dcce','XenServer'),(5,'i-2-5-TEST','i-2-5-TEST','Expunging',2,12,NULL,NULL,NULL,NULL,1,NULL,NULL,NULL,NULL,'36aba25f1e92499e',0,0,4,'2012-02-28 21:44:19','2012-02-28 21:42:08','2012-02-28 21:44:19','User','User',2,1,7,'08e1e22f-9091-4fce-b474-3dfe75f752f2','XenServer'),(6,'i-2-6-st','i-2-6-st','Running',2,12,'02:00:76:79:00:04','10.10.10.84',NULL,1,1,2,2,NULL,NULL,'bc675d5a5265f06',0,0,3,'2012-02-28 21:47:18','2012-02-28 21:44:11',NULL,'User','User',2,1,7,'cd4b11ce-ae75-44c7-89ad-609c76ac0238','XenServer'),(7,'r-7-st','r-7-st','Running',1,15,'0e:00:a9:fe:01:5c','169.254.1.92',NULL,1,1,2,2,NULL,NULL,'a6f05422d5d10fb7',1,0,3,'2012-02-28 21:44:58','2012-02-28 21:44:12',NULL,'DomainRouter','DomainRouter',2,1,13,'63fe2b29-2c2d-4299-b4bd-f1c2929d6e72','XenServer'),(8,'i-2-8-st','i-2-8-st','Running',2,12,'06:30:16:00:00:cb','172.20.0.100',NULL,1,1,2,2,NULL,NULL,'1bc1eef40ada25e9',0,0,3,'2012-02-28 21:51:33','2012-02-28 21:44:33',NULL,'User','User',2,1,7,'fabed3ae-abfc-46d9-a1f1-e435f3afaa70','XenServer'),(9,'r-9-st','r-9-st','Running',1,15,'0e:00:a9:fe:02:7d','169.254.2.125',NULL,1,1,2,2,NULL,NULL,'b351505e90223700',1,0,3,'2012-02-28 21:47:14','2012-02-28 21:45:22',NULL,'DomainRouter','DomainRouter',1,1,13,'a88f9e42-6bc0-4bb5-860b-e36a9c57f920','XenServer'),(10,'i-3-10-st','i-3-10-st','Running',2,12,'06:13:f4:00:00:f6','172.22.0.101',NULL,1,1,2,2,NULL,NULL,'2ed3a266ad37191f',0,0,3,'2012-02-28 21:51:37','2012-02-28 21:47:26',NULL,'User','User',3,2,1,'60fd9627-2eeb-4d6d-b9e2-f5a4c0808979','XenServer'),(11,'r-11-st','r-11-st','Running',1,15,'0e:00:a9:fe:02:f3','169.254.2.243',NULL,1,1,2,2,NULL,NULL,'489ee3a1711a7e82',1,0,3,'2012-02-28 21:48:15','2012-02-28 21:47:26',NULL,'DomainRouter','DomainRouter',3,2,13,'6eae5655-0926-40f0-9919-8a97d820781e','XenServer'),(12,'r-12-st','r-12-st','Running',1,15,'0e:00:a9:fe:02:e2','169.254.2.226',NULL,1,1,2,2,NULL,NULL,'b4f396f02388523b',1,0,3,'2012-02-28 21:48:46','2012-02-28 21:47:29',NULL,'DomainRouter','DomainRouter',1,1,13,'10da1e5c-4382-4dbc-89a8-c80dda46bae7','XenServer'),(13,'r-13-st','r-13-st','Running',1,15,'0e:00:a9:fe:02:a2','169.254.2.162',NULL,1,1,2,2,NULL,NULL,'cfa8afd2a123d88d',1,0,3,'2012-02-28 21:49:51','2012-02-28 21:48:56',NULL,'DomainRouter','DomainRouter',1,1,13,'958b5026-95f2-4f3a-8482-93246756bd89','XenServer'),(14,'r-14-st','r-14-st','Running',1,15,'0e:00:a9:fe:03:d3','169.254.3.211',NULL,1,1,2,2,NULL,NULL,'9054208114ae6864',1,0,3,'2012-02-28 21:50:45','2012-02-28 21:49:07',NULL,'DomainRouter','DomainRouter',2,1,13,'4157583e-79b2-48f0-be5d-6aad346c30b8','XenServer');
/*!40000 ALTER TABLE `vm_instance` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vm_template`
--

DROP TABLE IF EXISTS `vm_template`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `vm_template` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `unique_name` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `public` int(1) unsigned NOT NULL,
  `featured` int(1) unsigned NOT NULL,
  `type` varchar(32) DEFAULT NULL,
  `hvm` int(1) unsigned NOT NULL COMMENT 'requires HVM',
  `bits` int(6) unsigned NOT NULL COMMENT '32 bit or 64 bit',
  `url` varchar(255) DEFAULT NULL COMMENT 'the url where the template exists externally',
  `format` varchar(32) NOT NULL COMMENT 'format for the template',
  `created` datetime NOT NULL COMMENT 'Date created',
  `removed` datetime DEFAULT NULL COMMENT 'Date removed if not null',
  `account_id` bigint(20) unsigned NOT NULL COMMENT 'id of the account that created this template',
  `checksum` varchar(255) DEFAULT NULL COMMENT 'checksum for the template root disk',
  `display_text` varchar(4096) DEFAULT NULL COMMENT 'Description text set by the admin for display purpose only',
  `enable_password` int(1) unsigned NOT NULL DEFAULT '1' COMMENT 'true if this template supports password reset',
  `guest_os_id` bigint(20) unsigned NOT NULL COMMENT 'the OS of the template',
  `bootable` int(1) unsigned NOT NULL DEFAULT '1' COMMENT 'true if this template represents a bootable ISO',
  `prepopulate` int(1) unsigned NOT NULL DEFAULT '0' COMMENT 'prepopulate this template to primary storage',
  `cross_zones` int(1) unsigned NOT NULL DEFAULT '0' COMMENT 'Make this template available in all zones',
  `extractable` int(1) unsigned NOT NULL DEFAULT '0' COMMENT 'Is this template extractable',
  `hypervisor_type` varchar(32) DEFAULT NULL COMMENT 'hypervisor that the template belongs to',
  `source_template_id` bigint(20) unsigned DEFAULT NULL COMMENT 'Id of the original template, if this template is created from snapshot',
  `template_tag` varchar(255) DEFAULT NULL COMMENT 'template tag',
  PRIMARY KEY (`id`),
  KEY `i_vm_template__removed` (`removed`),
  KEY `i_vm_template__public` (`public`)
) ENGINE=InnoDB AUTO_INCREMENT=202 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vm_template`
--

LOCK TABLES `vm_template` WRITE;
/*!40000 ALTER TABLE `vm_template` DISABLE KEYS */;
INSERT INTO `vm_template` VALUES (1,'routing-1','SystemVM Template (XenServer)',0,0,'SYSTEM',0,64,'http://download.cloud.com/releases/2.2.0/systemvm.vhd.bz2','VHD','2012-02-28 11:21:37',NULL,1,'c33dfaf0937b35c25ef6a0fdd98f24d3','SystemVM Template (XenServer)',0,15,1,0,1,0,'XenServer',NULL,NULL),(2,'centos53-x86_64','CentOS 5.3(64-bit) no GUI (XenServer)',1,1,'BUILTIN',0,64,'http://download.cloud.com/templates/builtin/f59f18fb-ae94-4f97-afd2-f84755767aca.vhd.bz2','VHD','2012-02-28 11:21:37',NULL,1,'b63d854a9560c013142567bbae8d98cf','CentOS 5.3(64-bit) no GUI (XenServer)',0,12,1,0,1,1,'XenServer',NULL,NULL),(3,'routing-3','SystemVM Template (KVM)',0,0,'SYSTEM',0,64,'http://download.cloud.com/releases/2.2.0/systemvm.qcow2.bz2','QCOW2','2012-02-28 11:21:37',NULL,1,'ec463e677054f280f152fcc264255d2f','SystemVM Template (KVM)',0,15,1,0,1,0,'KVM',NULL,NULL),(4,'centos55-x86_64','CentOS 5.5(64-bit) no GUI (KVM)',1,1,'BUILTIN',0,64,'http://download.cloud.com/releases/2.2.0/eec2209b-9875-3c8d-92be-c001bd8a0faf.qcow2.bz2','QCOW2','2012-02-28 11:21:37',NULL,1,'ed0e788280ff2912ea40f7f91ca7a249','CentOS 5.5(64-bit) no GUI (KVM)',0,112,1,0,1,1,'KVM',NULL,NULL),(7,'centos53-x64','CentOS 5.3(64-bit) no GUI (vSphere)',1,1,'BUILTIN',0,64,'http://download.cloud.com/releases/2.2.0/CentOS5.3-x86_64.ova','OVA','2012-02-28 11:21:37',NULL,1,'f6f881b7f2292948d8494db837fe0f47','CentOS 5.3(64-bit) no GUI (vSphere)',0,12,1,0,1,1,'VMware',NULL,NULL),(8,'routing-8','SystemVM Template (vSphere)',0,0,'SYSTEM',0,32,'http://download.cloud.com/releases/2.2.0/systemvm.ova','OVA','2012-02-28 11:21:37',NULL,1,'3c9d4c704af44ebd1736e1bc78cec1fa','SystemVM Template (vSphere)',0,15,1,0,1,0,'VMware',NULL,NULL),(9,'routing-9','SystemVM Template (HyperV)',0,0,'SYSTEM',0,32,'http://download.cloud.com/releases/2.2.0/systemvm.vhd.bz2','VHD','2012-02-28 11:21:37',NULL,1,'c33dfaf0937b35c25ef6a0fdd98f24d3','SystemVM Template (HyperV)',0,15,1,0,1,0,'Hyperv',NULL,NULL),(200,'xs-tools.iso','xs-tools.iso',1,1,'PERHOST',1,64,NULL,'ISO','2012-02-28 19:29:22',NULL,1,NULL,'xen-pv-drv-iso',0,1,0,0,0,1,'XenServer',NULL,NULL),(201,'vmware-tools.iso','vmware-tools.iso',1,1,'PERHOST',1,64,NULL,'ISO','2012-02-28 19:29:22',NULL,1,NULL,'VMware Tools Installer ISO',0,1,0,0,0,1,'VMware',NULL,NULL);
/*!40000 ALTER TABLE `vm_template` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vm_template_details`
--

DROP TABLE IF EXISTS `vm_template_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `vm_template_details` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `template_id` bigint(20) unsigned NOT NULL COMMENT 'template id',
  `name` varchar(255) NOT NULL,
  `value` varchar(1024) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_vm_template_details__template_id` (`template_id`),
  CONSTRAINT `fk_vm_template_details__template_id` FOREIGN KEY (`template_id`) REFERENCES `vm_template` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vm_template_details`
--

LOCK TABLES `vm_template_details` WRITE;
/*!40000 ALTER TABLE `vm_template_details` DISABLE KEYS */;
/*!40000 ALTER TABLE `vm_template_details` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `volumes`
--

DROP TABLE IF EXISTS `volumes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `volumes` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
  `account_id` bigint(20) unsigned NOT NULL COMMENT 'owner.  foreign key to account table',
  `domain_id` bigint(20) unsigned NOT NULL COMMENT 'the domain that the owner belongs to',
  `pool_id` bigint(20) unsigned DEFAULT NULL COMMENT 'pool it belongs to. foreign key to storage_pool table',
  `instance_id` bigint(20) unsigned DEFAULT NULL COMMENT 'vm instance it belongs to. foreign key to vm_instance table',
  `device_id` bigint(20) unsigned DEFAULT NULL COMMENT 'which device inside vm instance it is ',
  `name` varchar(255) DEFAULT NULL COMMENT 'A user specified name for the volume',
  `size` bigint(20) unsigned NOT NULL COMMENT 'total size',
  `folder` varchar(255) DEFAULT NULL COMMENT 'The folder where the volume is saved',
  `path` varchar(255) DEFAULT NULL COMMENT 'Path',
  `pod_id` bigint(20) unsigned DEFAULT NULL COMMENT 'pod this volume belongs to',
  `data_center_id` bigint(20) unsigned NOT NULL COMMENT 'data center this volume belongs to',
  `iscsi_name` varchar(255) DEFAULT NULL COMMENT 'iscsi target name',
  `host_ip` char(40) DEFAULT NULL COMMENT 'host ip address for convenience',
  `volume_type` varchar(64) NOT NULL COMMENT 'root, swap or data',
  `pool_type` varchar(64) DEFAULT NULL COMMENT 'type of the pool',
  `disk_offering_id` bigint(20) unsigned NOT NULL COMMENT 'can be null for system VMs',
  `template_id` bigint(20) unsigned DEFAULT NULL COMMENT 'fk to vm_template.id',
  `first_snapshot_backup_uuid` varchar(255) DEFAULT NULL COMMENT 'The first snapshot that was ever taken for this volume',
  `recreatable` tinyint(1) unsigned NOT NULL DEFAULT '0' COMMENT 'Is this volume recreatable?',
  `created` datetime DEFAULT NULL COMMENT 'Date Created',
  `attached` datetime DEFAULT NULL COMMENT 'Date Attached',
  `updated` datetime DEFAULT NULL COMMENT 'Date updated for attach/detach',
  `removed` datetime DEFAULT NULL COMMENT 'Date removed.  not null if removed',
  `state` varchar(32) DEFAULT NULL COMMENT 'State machine',
  `chain_info` text COMMENT 'save possible disk chain info in primary storage',
  PRIMARY KEY (`id`),
  KEY `i_volumes__removed` (`removed`),
  KEY `i_volumes__pod_id` (`pod_id`),
  KEY `i_volumes__data_center_id` (`data_center_id`),
  KEY `i_volumes__account_id` (`account_id`),
  KEY `i_volumes__pool_id` (`pool_id`),
  KEY `i_volumes__instance_id` (`instance_id`),
  KEY `i_volumes__state` (`state`),
  CONSTRAINT `fk_volumes__account_id` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`),
  CONSTRAINT `fk_volumes__pool_id` FOREIGN KEY (`pool_id`) REFERENCES `storage_pool` (`id`),
  CONSTRAINT `fk_volumes__instance_id` FOREIGN KEY (`instance_id`) REFERENCES `vm_instance` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `volumes`
--

LOCK TABLES `volumes` WRITE;
/*!40000 ALTER TABLE `volumes` DISABLE KEYS */;
INSERT INTO `volumes` VALUES (1,1,1,200,1,0,'ROOT-1',2097152000,'/export/home/alena/secondary','bf472d45-ab0d-49a5-9218-5886cc522842',1,1,NULL,NULL,'ROOT','NetworkFilesystem',12,1,NULL,1,'2012-02-28 19:30:42',NULL,NULL,NULL,'Ready',NULL),(2,1,1,200,2,0,'ROOT-2',2097152000,'/export/home/alena/secondary','ff678911-8787-480f-9048-9f2515a36c1d',1,1,NULL,NULL,'ROOT','NetworkFilesystem',14,1,NULL,1,'2012-02-28 19:30:42',NULL,NULL,NULL,'Ready',NULL),(3,2,1,NULL,3,0,'ROOT-3',8589934592,NULL,NULL,NULL,1,NULL,NULL,'ROOT',NULL,7,2,NULL,0,'2012-02-28 21:40:32',NULL,NULL,'2012-02-28 21:44:17','Destroy',NULL),(4,2,1,NULL,4,0,'ROOT-4',8589934592,NULL,NULL,NULL,1,NULL,NULL,'ROOT',NULL,7,2,NULL,0,'2012-02-28 21:40:40',NULL,NULL,'2012-02-28 21:44:17','Destroy',NULL),(5,2,1,NULL,5,0,'ROOT-5',8589934592,NULL,NULL,NULL,1,NULL,NULL,'ROOT',NULL,7,2,NULL,0,'2012-02-28 21:42:08',NULL,NULL,'2012-02-28 21:44:17','Destroy',NULL),(6,2,1,200,6,0,'ROOT-6',8589934592,'/export/home/alena/secondary','5336dd64-4c54-492f-a7b3-0ae2602f7377',1,1,NULL,NULL,'ROOT','NetworkFilesystem',7,2,NULL,0,'2012-02-28 21:44:11',NULL,NULL,NULL,'Ready',NULL),(7,2,1,200,7,0,'ROOT-7',2097152000,'/export/home/alena/secondary','04dc59c6-f0ae-4a88-a927-239ca79363e2',1,1,NULL,NULL,'ROOT','NetworkFilesystem',13,1,NULL,1,'2012-02-28 21:44:12',NULL,NULL,NULL,'Ready',NULL),(8,2,1,200,8,0,'ROOT-8',8589934592,'/export/home/alena/secondary','45ca5f29-a4a1-4d04-b25e-9b230c81659e',1,1,NULL,NULL,'ROOT','NetworkFilesystem',7,2,NULL,0,'2012-02-28 21:44:33',NULL,NULL,NULL,'Ready',NULL),(9,1,1,200,9,0,'ROOT-9',2097152000,'/export/home/alena/secondary','cca6c2ec-cff9-4736-8711-76e1c6792fb2',1,1,NULL,NULL,'ROOT','NetworkFilesystem',13,1,NULL,1,'2012-02-28 21:45:22',NULL,NULL,NULL,'Ready',NULL),(10,3,2,200,10,0,'ROOT-10',8589934592,'/export/home/alena/secondary','c4712283-dcfe-492e-a5be-80536b806f57',1,1,NULL,NULL,'ROOT','NetworkFilesystem',1,2,NULL,0,'2012-02-28 21:47:26',NULL,NULL,NULL,'Ready',NULL),(11,3,2,200,11,0,'ROOT-11',2097152000,'/export/home/alena/secondary','c31d6671-b3ee-4654-b2f0-5c7e8d84e847',1,1,NULL,NULL,'ROOT','NetworkFilesystem',13,1,NULL,1,'2012-02-28 21:47:26',NULL,NULL,NULL,'Ready',NULL),(12,1,1,200,12,0,'ROOT-12',2097152000,'/export/home/alena/secondary','c1c5bd94-db92-4931-8269-57d7f3312267',1,1,NULL,NULL,'ROOT','NetworkFilesystem',13,1,NULL,1,'2012-02-28 21:47:29',NULL,NULL,NULL,'Ready',NULL),(13,1,1,200,13,0,'ROOT-13',2097152000,'/export/home/alena/secondary','05a81461-cc5f-4839-ac61-457d786c7b14',1,1,NULL,NULL,'ROOT','NetworkFilesystem',13,1,NULL,1,'2012-02-28 21:48:57',NULL,NULL,NULL,'Ready',NULL),(14,2,1,200,14,0,'ROOT-14',2097152000,'/export/home/alena/secondary','a3a89c6a-c3b6-4619-8930-29937c002217',1,1,NULL,NULL,'ROOT','NetworkFilesystem',13,1,NULL,1,'2012-02-28 21:49:07',NULL,NULL,NULL,'Ready',NULL);
/*!40000 ALTER TABLE `volumes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vpn_users`
--

DROP TABLE IF EXISTS `vpn_users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `vpn_users` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `owner_id` bigint(20) unsigned NOT NULL,
  `domain_id` bigint(20) unsigned NOT NULL,
  `username` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `state` char(32) NOT NULL COMMENT 'What state is this vpn user in',
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`),
  UNIQUE KEY `i_vpn_users__account_id__username` (`owner_id`,`username`),
  KEY `fk_vpn_users__domain_id` (`domain_id`),
  KEY `i_vpn_users_username` (`username`),
  CONSTRAINT `fk_vpn_users__owner_id` FOREIGN KEY (`owner_id`) REFERENCES `account` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_vpn_users__domain_id` FOREIGN KEY (`domain_id`) REFERENCES `domain` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vpn_users`
--

LOCK TABLES `vpn_users` WRITE;
/*!40000 ALTER TABLE `vpn_users` DISABLE KEYS */;
/*!40000 ALTER TABLE `vpn_users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Current Database: `cloud_usage`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `cloud_usage` /*!40100 DEFAULT CHARACTER SET latin1 */;

USE `cloud_usage`;

--
-- Table structure for table `account`
--

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

ALTER TABLE `cloud`.`networks` ADD COLUMN `switch_to_isolated` int(1) unsigned NOT NULL DEFAULT 0 COMMENT '1 if shared direct network has to be switched to isolated';
update `cloud`.`networks` set switch_to_isolated=1 where shared=0 and guest_type='Direct';
use cloud;
-- Dump completed on 2012-02-28 14:08:42
