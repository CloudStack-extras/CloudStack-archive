-- MySQL dump 10.11
--
-- Host: localhost    Database: cloud
-- ------------------------------------------------------
-- Server version   5.0.77

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
-- Table structure for table `account_vlan_map`
--

DROP TABLE IF EXISTS `account_vlan_map`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `account_vlan_map` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `account_id` bigint(20) unsigned default NULL COMMENT 'account id. foreign key to account table',
  `vlan_db_id` bigint(20) unsigned NOT NULL COMMENT 'database id of vlan. foreign key to vlan table',
  `domain_id` bigint(20) unsigned default NULL COMMENT 'domain id. foreign key to domain table',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`),
  KEY `i_account_vlan_map__account_id` (`account_id`),
  KEY `i_account_vlan_map__domain_id` (`domain_id`),
  KEY `i_account_vlan_map__vlan_id` (`vlan_db_id`),
  CONSTRAINT `fk_account_vlan_map__account_id` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_account_vlan_map__domain_id` FOREIGN KEY (`domain_id`) REFERENCES `domain` (`id`) ON DELETE CASCADE,
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
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `alert`
--

LOCK TABLES `alert` WRITE;
/*!40000 ALTER TABLE `alert` DISABLE KEYS */;
INSERT INTO `alert` VALUES (1,13,0,0,'Management server node 192.168.130.201 is up',1,'2011-04-23 02:13:06','2011-04-23 02:13:06',NULL),(2,13,0,0,'Management server node 192.168.130.201 is up',1,'2011-04-23 02:17:53','2011-04-23 02:17:53',NULL),(3,18,1,1,'Secondary Storage Vm up in zone: ZONE1, secStorageVm: s-1-VM, public IP: 172.16.36.42, private IP: 192.168.163.103',1,'2011-04-23 02:21:19','2011-04-23 02:21:19',NULL),(4,9,1,1,'Console proxy up in zone: ZONE1, proxy: v-2-VM, public IP: 172.16.36.27, private IP: 192.168.163.108',1,'2011-04-23 02:21:50','2011-04-23 02:21:50',NULL),(5,13,0,0,'Management server node 192.168.130.201 is up',1,'2011-04-23 03:03:24','2011-04-23 03:03:24',NULL),(6,12,0,0,'No usage server process running',1,'2011-04-23 04:03:22','2011-04-23 04:03:22',NULL),(7,9,1,1,'Console proxy down in zone: ZONE1, proxy: v-2-VM, public IP: 172.16.36.27, private IP: N/A',1,'2011-04-23 04:06:47','2011-04-23 04:06:47',NULL),(8,13,0,0,'Management server node 192.168.130.201 is up',1,'2011-04-23 04:07:43','2011-04-23 04:07:43',NULL),(9,9,1,1,'Console proxy down in zone: ZONE1, proxy: v-2-VM, public IP: 172.16.36.27, private IP: N/A',1,'2011-04-23 04:08:16','2011-04-23 04:08:16',NULL);
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
) ENGINE=InnoDB AUTO_INCREMENT=33 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `async_job`
--

LOCK TABLES `async_job` WRITE;
/*!40000 ALTER TABLE `async_job` DISABLE KEYS */;
INSERT INTO `async_job` VALUES (1,2,2,NULL,NULL,NULL,'DeployVM','virtualmachine','{\"accountId\":2,\"dataCenterId\":1,\"serviceOfferingId\":11,\"templateId\":2,\"diskOfferingId\":12,\"password\":\"pU7byhzvj\",\"displayName\":\"Admin-VM-1\",\"group\":\"Admin-VM-1\",\"domainId\":0,\"userId\":2,\"vmId\":0,\"eventId\":23}',0,0,NULL,1,1,0,'com.cloud.async.executor.DeployVMResultObject/{\"id\":3,\"name\":\"i-2-3-VM\",\"created\":\"Apr 22, 2011 10:24:06 PM\",\"zoneId\":1,\"zoneName\":\"ZONE1\",\"ipAddress\":\"10.1.1.2\",\"serviceOfferingId\":11,\"haEnabled\":false,\"state\":\"Running\",\"templateId\":2,\"templateName\":\"CentOS 5.3(x86_64) no GUI\",\"templateDisplayText\":\"CentOS 5.3(x86_64) no GUI\",\"passwordEnabled\":false,\"serviceOfferingName\":\"Little Instance, Virtual Networking\",\"cpuNumber\":\"1\",\"cpuSpeed\":\"100\",\"memory\":\"128\",\"displayName\":\"Admin-VM-1\",\"group\":\"Admin-VM-1\",\"domainId\":1,\"domain\":\"ROOT\",\"account\":\"admin\",\"hostname\":\"xenserver-chandan2\",\"hostid\":2,\"networkGroupList\":\"\"}',6603285398259,6603285398259,'2011-04-23 02:24:06','2011-04-23 02:25:53','2011-04-23 02:25:49',NULL),(2,2,2,NULL,NULL,NULL,'DeployVM','virtualmachine','{\"accountId\":2,\"dataCenterId\":1,\"serviceOfferingId\":11,\"templateId\":2,\"password\":\"vR9sxwqim\",\"domainId\":0,\"userId\":2,\"vmId\":0,\"eventId\":36}',0,0,NULL,1,1,0,'com.cloud.async.executor.DeployVMResultObject/{\"id\":5,\"name\":\"i-2-5-VM\",\"created\":\"Apr 22, 2011 10:26:35 PM\",\"zoneId\":1,\"zoneName\":\"ZONE1\",\"ipAddress\":\"10.1.1.3\",\"serviceOfferingId\":11,\"haEnabled\":false,\"state\":\"Running\",\"templateId\":2,\"templateName\":\"CentOS 5.3(x86_64) no GUI\",\"templateDisplayText\":\"CentOS 5.3(x86_64) no GUI\",\"passwordEnabled\":false,\"serviceOfferingName\":\"Little Instance, Virtual Networking\",\"cpuNumber\":\"1\",\"cpuSpeed\":\"100\",\"memory\":\"128\",\"displayName\":\"i-2-5-VM\",\"domainId\":1,\"domain\":\"ROOT\",\"account\":\"admin\",\"hostname\":\"xenserver-chandan2\",\"hostid\":2,\"networkGroupList\":\"\"}',6603285398259,6603285398259,'2011-04-23 02:26:35','2011-04-23 02:26:44',NULL,NULL),(3,2,2,NULL,NULL,NULL,'AssignToLoadBalancer',NULL,'{\"userId\":2,\"domainRouterId\":4,\"loadBalancerId\":1,\"instanceIdList\":[3]}',0,0,NULL,1,0,0,'java.lang.String/\"success\"',6603285398259,6603285398259,'2011-04-23 02:27:54','2011-04-23 02:27:57',NULL,NULL),(4,2,2,NULL,NULL,NULL,'AssignToLoadBalancer',NULL,'{\"userId\":2,\"domainRouterId\":4,\"loadBalancerId\":1,\"instanceIdList\":[5]}',0,0,NULL,1,0,0,'java.lang.String/\"success\"',6603285398259,6603285398259,'2011-04-23 02:28:02','2011-04-23 02:28:05',NULL,NULL),(5,2,2,NULL,NULL,NULL,'CreateSnapshot','snapshot','{\"accountId\":2,\"userId\":2,\"snapshotId\":0,\"policyIds\":[1],\"policyId\":0,\"volumeId\":6,\"eventId\":0}',0,0,NULL,1,1,0,'com.cloud.async.executor.CreateSnapshotResultObject/{\"id\":1,\"accountName\":\"admin\",\"volumeId\":6,\"domainId\":1,\"domainName\":\"ROOT\",\"created\":\"Apr 22, 2011 10:33:46 PM\",\"name\":\"i-2-5-VM_i-2-5-VM-ROOT_20110423023346\",\"snapshotType\":\"MANUAL\",\"volumeName\":\"i-2-5-VM-ROOT\",\"volumeType\":\"ROOT\"}',6603285398259,6603285398259,'2011-04-23 02:33:46','2011-04-23 02:34:04','2011-04-23 02:34:02',NULL),(6,2,2,NULL,NULL,NULL,'CreateSnapshot','snapshot','{\"accountId\":2,\"userId\":2,\"snapshotId\":0,\"policyIds\":[1],\"policyId\":0,\"volumeId\":4,\"eventId\":0}',0,0,NULL,1,1,0,'com.cloud.async.executor.CreateSnapshotResultObject/{\"id\":2,\"accountName\":\"admin\",\"volumeId\":4,\"domainId\":1,\"domainName\":\"ROOT\",\"created\":\"Apr 22, 2011 10:33:48 PM\",\"name\":\"Admin-VM-1_i-2-3-VM-ROOT_20110423023348\",\"snapshotType\":\"MANUAL\",\"volumeName\":\"i-2-3-VM-ROOT\",\"volumeType\":\"ROOT\"}',6603285398259,6603285398259,'2011-04-23 02:33:48','2011-04-23 02:34:05','2011-04-23 02:34:04',NULL),(7,2,2,NULL,NULL,NULL,'CreateSnapshot','snapshot','{\"accountId\":2,\"userId\":2,\"snapshotId\":0,\"policyIds\":[1],\"policyId\":0,\"volumeId\":5,\"eventId\":0}',0,0,NULL,1,1,0,'com.cloud.async.executor.CreateSnapshotResultObject/{\"id\":3,\"accountName\":\"admin\",\"volumeId\":5,\"domainId\":1,\"domainName\":\"ROOT\",\"created\":\"Apr 22, 2011 10:33:51 PM\",\"name\":\"Admin-VM-1_i-2-3-VM-DATA_20110423023351\",\"snapshotType\":\"MANUAL\",\"volumeName\":\"i-2-3-VM-DATA\",\"volumeType\":\"DATADISK\"}',6603285398259,6603285398259,'2011-04-23 02:33:51','2011-04-23 02:33:54',NULL,NULL),(8,2,2,NULL,NULL,NULL,'CreatePrivateTemplate','template','{\"userId\":2,\"volumeId\":6,\"snapshotId\":1,\"guestOsId\":12,\"name\":\"TemplateFromSnapshot-1\",\"description\":\"TemplateFromSnapshot-1\",\"passwordEnabled\":false,\"isPublic\":false,\"isFeatured\":false}',0,0,NULL,1,1,0,'com.cloud.async.executor.CreatePrivateTemplateResultObject/{\"id\":202,\"name\":\"TemplateFromSnapshot-1\",\"displayText\":\"TemplateFromSnapshot-1\",\"isPublic\":false,\"created\":\"Apr 22, 2011 10:40:33 PM\",\"isReady\":true,\"passwordEnabled\":false,\"osTypeId\":12,\"osTypeName\":\"CentOS 5.3 (64-bit)\",\"account\":\"admin\",\"zoneId\":1,\"zoneName\":\"ZONE1\",\"domainName\":\"ROOT\",\"domainId\":1}',6603285398259,6603285398259,'2011-04-23 02:36:35','2011-04-23 02:40:33','2011-04-23 02:37:50',NULL),(9,2,2,NULL,NULL,NULL,'CreatePrivateTemplate','template','{\"userId\":2,\"volumeId\":4,\"snapshotId\":2,\"guestOsId\":12,\"name\":\"TemplateFromSnapshot-2\",\"description\":\"TemplateFromSnapshot-2\",\"passwordEnabled\":false,\"isPublic\":false,\"isFeatured\":false}',0,0,NULL,1,1,0,'com.cloud.async.executor.CreatePrivateTemplateResultObject/{\"id\":203,\"name\":\"TemplateFromSnapshot-2\",\"displayText\":\"TemplateFromSnapshot-2\",\"isPublic\":false,\"created\":\"Apr 22, 2011 10:41:21 PM\",\"isReady\":true,\"passwordEnabled\":false,\"osTypeId\":12,\"osTypeName\":\"CentOS 5.3 (64-bit)\",\"account\":\"admin\",\"zoneId\":1,\"zoneName\":\"ZONE1\",\"domainName\":\"ROOT\",\"domainId\":1}',6603285398259,6603285398259,'2011-04-23 02:37:13','2011-04-23 02:41:21','2011-04-23 02:37:50',NULL),(10,2,2,NULL,NULL,NULL,'CreatePrivateTemplate','template','{\"userId\":2,\"volumeId\":5,\"snapshotId\":3,\"guestOsId\":12,\"name\":\"TemplateFromSnapshot-3-Data\",\"description\":\"TemplateFromSnapshot-3-Data\",\"passwordEnabled\":false,\"isPublic\":false,\"isFeatured\":false}',0,0,NULL,1,1,0,'com.cloud.async.executor.CreatePrivateTemplateResultObject/{\"id\":204,\"name\":\"TemplateFromSnapshot-3-Data\",\"displayText\":\"TemplateFromSnapshot-3-Data\",\"isPublic\":false,\"created\":\"Apr 22, 2011 10:37:52 PM\",\"isReady\":true,\"passwordEnabled\":false,\"osTypeId\":12,\"osTypeName\":\"CentOS 5.3 (64-bit)\",\"account\":\"admin\",\"zoneId\":1,\"zoneName\":\"ZONE1\",\"domainName\":\"ROOT\",\"domainId\":1}',6603285398259,6603285398259,'2011-04-23 02:37:47','2011-04-23 02:37:53','2011-04-23 02:37:50',NULL),(11,3,3,NULL,NULL,NULL,'DeployVM','virtualmachine','{\"accountId\":3,\"dataCenterId\":1,\"serviceOfferingId\":11,\"templateId\":2,\"diskOfferingId\":12,\"password\":\"tJ2yxzfrb\",\"displayName\":\"Nimbus-VM-1\",\"group\":\"Nimbus-VM-1\",\"domainId\":0,\"userId\":3,\"vmId\":0,\"eventId\":54}',0,0,NULL,1,1,0,'com.cloud.async.executor.DeployVMResultObject/{\"id\":6,\"name\":\"i-3-6-VM\",\"created\":\"Apr 22, 2011 10:38:51 PM\",\"zoneId\":1,\"zoneName\":\"ZONE1\",\"ipAddress\":\"10.1.1.2\",\"serviceOfferingId\":11,\"haEnabled\":false,\"state\":\"Running\",\"templateId\":2,\"templateName\":\"CentOS 5.3(x86_64) no GUI\",\"templateDisplayText\":\"CentOS 5.3(x86_64) no GUI\",\"passwordEnabled\":false,\"serviceOfferingName\":\"Little Instance, Virtual Networking\",\"cpuNumber\":\"1\",\"cpuSpeed\":\"100\",\"memory\":\"128\",\"displayName\":\"Nimbus-VM-1\",\"group\":\"Nimbus-VM-1\",\"domainId\":2,\"domain\":\"CHILD\",\"account\":\"nimbus\",\"networkGroupList\":\"\"}',6603285398259,6603285398259,'2011-04-23 02:38:51','2011-04-23 02:40:18','2011-04-23 02:40:12',NULL),(12,3,3,NULL,NULL,NULL,'DeployVM','virtualmachine','{\"accountId\":3,\"dataCenterId\":1,\"serviceOfferingId\":11,\"templateId\":2,\"password\":\"sB6ipujcx\",\"displayName\":\"Nimbus-VM-2\",\"group\":\"Nimbus-VM-2\",\"domainId\":0,\"userId\":3,\"vmId\":0,\"eventId\":63}',0,0,NULL,1,1,0,'com.cloud.async.executor.DeployVMResultObject/{\"id\":8,\"name\":\"i-3-8-VM\",\"created\":\"Apr 22, 2011 10:39:08 PM\",\"zoneId\":1,\"zoneName\":\"ZONE1\",\"ipAddress\":\"10.1.1.3\",\"serviceOfferingId\":11,\"haEnabled\":false,\"state\":\"Running\",\"templateId\":2,\"templateName\":\"CentOS 5.3(x86_64) no GUI\",\"templateDisplayText\":\"CentOS 5.3(x86_64) no GUI\",\"passwordEnabled\":false,\"serviceOfferingName\":\"Little Instance, Virtual Networking\",\"cpuNumber\":\"1\",\"cpuSpeed\":\"100\",\"memory\":\"128\",\"displayName\":\"Nimbus-VM-2\",\"group\":\"Nimbus-VM-2\",\"domainId\":2,\"domain\":\"CHILD\",\"account\":\"nimbus\",\"networkGroupList\":\"\"}',6603285398259,6603285398259,'2011-04-23 02:39:08','2011-04-23 02:40:25','2011-04-23 02:40:19',NULL),(13,3,3,NULL,NULL,NULL,'AssignToLoadBalancer',NULL,'{\"userId\":3,\"domainRouterId\":7,\"loadBalancerId\":2,\"instanceIdList\":[6]}',0,0,NULL,1,0,0,'java.lang.String/\"success\"',6603285398259,6603285398259,'2011-04-23 02:41:36','2011-04-23 02:41:39',NULL,NULL),(14,3,3,NULL,NULL,NULL,'AssignToLoadBalancer',NULL,'{\"userId\":3,\"domainRouterId\":7,\"loadBalancerId\":2,\"instanceIdList\":[8]}',0,0,NULL,1,0,0,'java.lang.String/\"success\"',6603285398259,6603285398259,'2011-04-23 02:41:44','2011-04-23 02:41:47',NULL,NULL),(15,3,3,NULL,NULL,NULL,'CreateSnapshot','snapshot','{\"accountId\":3,\"userId\":3,\"snapshotId\":0,\"policyIds\":[1],\"policyId\":0,\"volumeId\":10,\"eventId\":0}',0,0,NULL,1,1,0,'com.cloud.async.executor.CreateSnapshotResultObject/{\"id\":4,\"accountName\":\"nimbus\",\"volumeId\":10,\"domainId\":2,\"domainName\":\"CHILD\",\"created\":\"Apr 22, 2011 10:49:15 PM\",\"name\":\"Nimbus-VM-2_i-3-8-VM-ROOT_20110423024915\",\"snapshotType\":\"MANUAL\",\"volumeName\":\"i-3-8-VM-ROOT\",\"volumeType\":\"ROOT\"}',6603285398259,6603285398259,'2011-04-23 02:49:15','2011-04-23 02:49:33','2011-04-23 02:49:32',NULL),(16,3,3,NULL,NULL,NULL,'CreateSnapshot','snapshot','{\"accountId\":3,\"userId\":3,\"snapshotId\":0,\"policyIds\":[1],\"policyId\":0,\"volumeId\":8,\"eventId\":0}',0,0,NULL,1,1,0,'com.cloud.async.executor.CreateSnapshotResultObject/{\"id\":5,\"accountName\":\"nimbus\",\"volumeId\":8,\"domainId\":2,\"domainName\":\"CHILD\",\"created\":\"Apr 22, 2011 10:49:19 PM\",\"name\":\"Nimbus-VM-1_i-3-6-VM-ROOT_20110423024919\",\"snapshotType\":\"MANUAL\",\"volumeName\":\"i-3-6-VM-ROOT\",\"volumeType\":\"ROOT\"}',6603285398259,6603285398259,'2011-04-23 02:49:19','2011-04-23 02:49:41','2011-04-23 02:49:39',NULL),(17,3,3,NULL,NULL,NULL,'CreateSnapshot','snapshot','{\"accountId\":3,\"userId\":3,\"snapshotId\":0,\"policyIds\":[1],\"policyId\":0,\"volumeId\":9,\"eventId\":0}',0,0,NULL,1,1,0,'com.cloud.async.executor.CreateSnapshotResultObject/{\"id\":6,\"accountName\":\"nimbus\",\"volumeId\":9,\"domainId\":2,\"domainName\":\"CHILD\",\"created\":\"Apr 22, 2011 10:49:24 PM\",\"name\":\"Nimbus-VM-1_i-3-6-VM-DATA_20110423024924\",\"snapshotType\":\"MANUAL\",\"volumeName\":\"i-3-6-VM-DATA\",\"volumeType\":\"DATADISK\"}',6603285398259,6603285398259,'2011-04-23 02:49:23','2011-04-23 02:49:26',NULL,NULL),(18,3,3,NULL,NULL,NULL,'CreatePrivateTemplate','template','{\"userId\":3,\"volumeId\":10,\"snapshotId\":4,\"guestOsId\":12,\"name\":\"TemplateFromSnapshot-5\",\"description\":\"TemplateFromSnapshot-5\",\"passwordEnabled\":false,\"isPublic\":false,\"isFeatured\":false}',0,0,NULL,1,1,0,'com.cloud.async.executor.CreatePrivateTemplateResultObject/{\"id\":205,\"name\":\"TemplateFromSnapshot-5\",\"displayText\":\"TemplateFromSnapshot-5\",\"isPublic\":false,\"created\":\"Apr 22, 2011 10:58:50 PM\",\"isReady\":true,\"passwordEnabled\":false,\"osTypeId\":12,\"osTypeName\":\"CentOS 5.3 (64-bit)\",\"account\":\"nimbus\",\"zoneId\":1,\"zoneName\":\"ZONE1\",\"domainName\":\"CHILD\",\"domainId\":2}',6603285398259,6603285398259,'2011-04-23 02:55:38','2011-04-23 02:58:50','2011-04-23 02:58:49',NULL),(19,3,3,NULL,NULL,NULL,'CreatePrivateTemplate','template','{\"userId\":3,\"volumeId\":8,\"snapshotId\":5,\"guestOsId\":12,\"name\":\"TemplateFromSnapshot-6\",\"description\":\"TemplateFromSnapshot-6\",\"passwordEnabled\":false,\"isPublic\":false,\"isFeatured\":false}',0,0,NULL,1,1,0,'com.cloud.async.executor.CreatePrivateTemplateResultObject/{\"id\":206,\"name\":\"TemplateFromSnapshot-6\",\"displayText\":\"TemplateFromSnapshot-6\",\"isPublic\":false,\"created\":\"Apr 22, 2011 10:59:20 PM\",\"isReady\":true,\"passwordEnabled\":false,\"osTypeId\":12,\"osTypeName\":\"CentOS 5.3 (64-bit)\",\"account\":\"nimbus\",\"zoneId\":1,\"zoneName\":\"ZONE1\",\"domainName\":\"CHILD\",\"domainId\":2}',6603285398259,6603285398259,'2011-04-23 02:55:48','2011-04-23 02:59:20','2011-04-23 02:59:20',NULL),(20,3,3,NULL,NULL,NULL,'CreatePrivateTemplate','template','{\"userId\":3,\"volumeId\":9,\"snapshotId\":6,\"guestOsId\":60,\"name\":\"TemplateFromSnapshot-7-Data\",\"description\":\"TemplateFromSnapshot-7-Data\",\"passwordEnabled\":false,\"isPublic\":false,\"isFeatured\":false}',0,0,NULL,1,1,0,'com.cloud.async.executor.CreatePrivateTemplateResultObject/{\"id\":207,\"name\":\"TemplateFromSnapshot-7-Data\",\"displayText\":\"TemplateFromSnapshot-7-Data\",\"isPublic\":false,\"created\":\"Apr 22, 2011 10:57:52 PM\",\"isReady\":true,\"passwordEnabled\":false,\"osTypeId\":60,\"osTypeName\":\"Other\",\"account\":\"nimbus\",\"zoneId\":1,\"zoneName\":\"ZONE1\",\"domainName\":\"CHILD\",\"domainId\":2}',6603285398259,6603285398259,'2011-04-23 02:57:48','2011-04-23 02:57:52','2011-04-23 02:57:51',NULL),(21,3,3,NULL,NULL,NULL,'CreateVolumeFromSnapshot','volume','{\"accountId\":3,\"userId\":3,\"snapshotId\":4,\"policyId\":0,\"volumeId\":10,\"name\":\"VlmfromSnapshot-1\",\"eventId\":89}',0,0,NULL,2,1,0,'java.lang.String/\"Execution was cancelled because of server shutdown\"',6603285398259,6603285398259,'2011-04-23 02:59:06','2011-04-23 03:03:23','2011-04-23 02:59:31',NULL),(22,3,3,NULL,NULL,NULL,'CreateVolumeFromSnapshot','volume','{\"accountId\":3,\"userId\":3,\"snapshotId\":5,\"policyId\":0,\"volumeId\":8,\"name\":\"VlmfromSnapshot-2\",\"eventId\":91}',0,0,NULL,2,1,0,'java.lang.String/\"Execution was cancelled because of server shutdown\"',6603285398259,6603285398259,'2011-04-23 02:59:11','2011-04-23 04:07:42','2011-04-23 02:59:30',NULL),(23,3,3,NULL,NULL,NULL,'CreateVolumeFromSnapshot','volume','{\"accountId\":3,\"userId\":3,\"snapshotId\":6,\"policyId\":0,\"volumeId\":9,\"name\":\"VlmfromSnapshot-3-Data\",\"eventId\":94}',0,0,NULL,1,1,0,'com.cloud.async.executor.VolumeOperationResultObject/{\"id\":13,\"name\":\"VlmfromSnapshot-3-Data\",\"accountName\":\"nimbus\",\"domainId\":2,\"domain\":\"CHILD\",\"destroyed\":false,\"diskOfferingId\":12,\"volumeType\":\"DATADISK\",\"volumeSize\":1073741824,\"createdDate\":\"Apr 22, 2011 10:59:25 PM\",\"state\":\"Created\",\"storageType\":\"shared\",\"storage\":\"xenPrimary163\",\"zoneId\":1,\"zoneName\":\"ZONE1\"}',6603285398259,6603285398259,'2011-04-23 02:59:25','2011-04-23 02:59:44','2011-04-23 02:59:32',NULL),(24,2,2,NULL,'volume',14,'CreateVolumeFromSnapshot','volume','{\"accountId\":2,\"userId\":2,\"snapshotId\":1,\"policyId\":0,\"volumeId\":6,\"name\":\"CreateVlmFromSnp-1\",\"eventId\":99}',0,0,NULL,0,1,0,'java.lang.Long/14',6603285398259,NULL,'2011-04-23 03:00:47','2011-04-23 03:00:47','2011-04-23 03:01:04',NULL),(25,2,2,NULL,'volume',15,'CreateVolumeFromSnapshot','volume','{\"accountId\":2,\"userId\":2,\"snapshotId\":2,\"policyId\":0,\"volumeId\":4,\"name\":\"CreateVlmFromSnp-2\",\"eventId\":101}',0,0,NULL,0,1,0,'java.lang.Long/15',6603285398259,NULL,'2011-04-23 03:00:55','2011-04-23 03:00:55','2011-04-23 03:01:04',NULL),(26,1,3,NULL,NULL,NULL,'CreateSnapshot','snapshot','{\"accountId\":3,\"userId\":1,\"snapshotId\":0,\"policyIds\":[2],\"policyId\":0,\"volumeId\":10,\"eventId\":0}',0,0,NULL,1,1,0,'com.cloud.async.executor.CreateSnapshotResultObject/{\"id\":7,\"accountName\":\"nimbus\",\"volumeId\":10,\"domainId\":2,\"domainName\":\"CHILD\",\"created\":\"Apr 23, 2011 12:03:23 AM\",\"name\":\"Nimbus-VM-2_i-3-8-VM-ROOT_20110423040323\",\"snapshotType\":\"RECURRING\",\"volumeName\":\"i-3-8-VM-ROOT\",\"volumeType\":\"ROOT\"}',6603285398259,6603285398259,'2011-04-23 04:03:23','2011-04-23 04:03:45',NULL,NULL),(27,1,2,NULL,NULL,NULL,'CreateSnapshot','snapshot','{\"accountId\":2,\"userId\":1,\"snapshotId\":0,\"policyIds\":[6],\"policyId\":0,\"volumeId\":4,\"eventId\":0}',0,0,NULL,0,0,0,NULL,6603285398259,NULL,'2011-04-23 04:03:23',NULL,NULL,NULL),(28,2,3,NULL,NULL,NULL,'StopRouter','router','{\"userId\":0,\"vmId\":7,\"operation\":\"Noop\",\"eventId\":114}',0,0,NULL,1,0,0,'com.cloud.async.executor.RouterOperationResultObject/{\"id\":7,\"zoneId\":1,\"zoneName\":\"ZONE1\",\"dns1\":\"72.52.126.11\",\"dns2\":\"72.52.126.12\",\"networkDomain\":\"v3.myvm.com\",\"gateway\":\"172.16.36.1\",\"name\":\"r-7-VM\",\"podId\":1,\"privateMacAddress\":\"06:01:29:75:00:07\",\"privateNetMask\":\"255.255.0.0\",\"publicIp\":\"172.16.36.16\",\"publicMacAddress\":\"06:81:29:75:00:07\",\"publicNetMask\":\"255.255.0.0\",\"guestIp\":\"10.1.1.1\",\"guestMacAddress\":\"02:00:04:d9:00:01\",\"templateId\":1,\"created\":\"Apr 22, 2011 10:38:51 PM\",\"account\":\"nimbus\",\"domainId\":2,\"domain\":\"CHILD\",\"state\":\"Stopped\"}',6603285398259,6603285398259,'2011-04-23 04:04:52','2011-04-23 04:05:05','2011-04-23 04:05:03',NULL),(29,2,2,NULL,NULL,NULL,'StopRouter','router','{\"userId\":0,\"vmId\":4,\"operation\":\"Noop\",\"eventId\":116}',0,0,NULL,1,0,0,'com.cloud.async.executor.RouterOperationResultObject/{\"id\":4,\"zoneId\":1,\"zoneName\":\"ZONE1\",\"dns1\":\"72.52.126.11\",\"dns2\":\"72.52.126.12\",\"networkDomain\":\"v2.myvm.com\",\"gateway\":\"172.16.36.1\",\"name\":\"r-4-VM\",\"podId\":1,\"privateMacAddress\":\"06:01:23:a6:00:05\",\"privateNetMask\":\"255.255.0.0\",\"publicIp\":\"172.16.36.59\",\"publicMacAddress\":\"06:81:23:a6:00:05\",\"publicNetMask\":\"255.255.0.0\",\"guestIp\":\"10.1.1.1\",\"guestMacAddress\":\"02:00:04:d8:00:01\",\"templateId\":1,\"created\":\"Apr 22, 2011 10:24:07 PM\",\"account\":\"admin\",\"domainId\":1,\"domain\":\"ROOT\",\"state\":\"Stopped\"}',6603285398259,6603285398259,'2011-04-23 04:04:58','2011-04-23 04:05:18','2011-04-23 04:05:08',NULL),(30,2,1,NULL,NULL,NULL,'SystemVmCmd','systemvm','{\"userId\":0,\"vmId\":1,\"operation\":\"Stop\",\"eventId\":118}',0,0,NULL,1,0,0,'com.cloud.async.executor.SystemVmOperationResultObject/{\"id\":1,\"name\":\"s-1-VM\",\"zoneId\":1,\"zoneName\":\"ZONE1\",\"dns1\":\"192.168.110.253\",\"dns2\":\"192.168.110.254\",\"networkDomain\":\"foo.com\",\"gateway\":\"172.16.36.1\",\"podId\":1,\"privateMac\":\"06:01:de:49:00:01\",\"privateNetmask\":\"255.255.255.0\",\"publicIp\":\"172.16.36.42\",\"publicMac\":\"06:81:de:49:00:01\",\"publicNetmask\":\"255.255.252.0\",\"templateId\":1,\"created\":\"Apr 22, 2011 10:19:21 PM\",\"actionSessions\":0,\"state\":\"Stopped\"}',6603285398259,6603285398259,'2011-04-23 04:05:03','2011-04-23 04:05:34','2011-04-23 04:05:23',NULL),(31,2,1,NULL,NULL,NULL,'StopConsoleProxy','systemvm','{\"userId\":0,\"vmId\":2,\"operation\":\"Noop\",\"eventId\":123}',0,0,NULL,1,0,0,'com.cloud.async.executor.ConsoleProxyOperationResultObject/{\"id\":2,\"name\":\"v-2-VM\",\"zoneId\":1,\"zoneName\":\"ZONE1\",\"dns1\":\"72.52.126.11\",\"dns2\":\"72.52.126.12\",\"networkDomain\":\"foo.com\",\"gateway\":\"172.16.36.1\",\"podId\":1,\"privateMac\":\"06:01:ea:ec:00:03\",\"privateNetmask\":\"255.255.255.0\",\"publicIp\":\"172.16.36.27\",\"publicMac\":\"06:81:ea:ec:00:03\",\"publicNetmask\":\"255.255.252.0\",\"templateId\":1,\"created\":\"Apr 22, 2011 10:19:22 PM\",\"actionSessions\":0,\"state\":\"Stopped\"}',6603285398259,6603285398259,'2011-04-23 04:06:34','2011-04-23 04:06:47',NULL,NULL),(32,2,1,NULL,NULL,NULL,'StopConsoleProxy','systemvm','{\"userId\":0,\"vmId\":2,\"operation\":\"Noop\",\"eventId\":126}',0,0,NULL,1,0,0,'com.cloud.async.executor.ConsoleProxyOperationResultObject/{\"id\":2,\"name\":\"v-2-VM\",\"zoneId\":1,\"zoneName\":\"ZONE1\",\"dns1\":\"72.52.126.11\",\"dns2\":\"72.52.126.12\",\"networkDomain\":\"foo.com\",\"gateway\":\"172.16.36.1\",\"podId\":1,\"privateMac\":\"06:01:ea:ec:00:03\",\"privateNetmask\":\"255.255.255.0\",\"publicIp\":\"172.16.36.27\",\"publicMac\":\"06:81:ea:ec:00:03\",\"publicNetmask\":\"255.255.252.0\",\"templateId\":1,\"created\":\"Apr 22, 2011 10:19:22 PM\",\"actionSessions\":0,\"state\":\"Stopped\"}',6603285398259,6603285398259,'2011-04-23 04:08:04','2011-04-23 04:08:16',NULL,NULL);
/*!40000 ALTER TABLE `async_job` ENABLE KEYS */;
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
  `pod_id` bigint(20) unsigned NOT NULL COMMENT 'pod id',
  `data_center_id` bigint(20) unsigned NOT NULL COMMENT 'data center id',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`),
  UNIQUE KEY `i_cluster__pod_id__name` (`pod_id`,`name`),
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
INSERT INTO `cluster` VALUES (1,'FirstCluster',1,1);
/*!40000 ALTER TABLE `cluster` ENABLE KEYS */;
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
INSERT INTO `configuration` VALUES ('Advanced','DEFAULT','management-server','account.cleanup.interval','86400','The interval in seconds between cleanup for removed accounts'),('Alert','DEFAULT','management-server','alert.email.addresses',NULL,'Comma separated list of email addresses used for sending alerts.'),('Alert','DEFAULT','management-server','alert.email.sender',NULL,'Sender of alert email (will be in the From header of the email).'),('Alert','DEFAULT','management-server','alert.smtp.host',NULL,'SMTP hostname used for sending out email alerts.'),('Alert','DEFAULT','management-server','alert.smtp.password',NULL,'Password for SMTP authentication (applies only if alert.smtp.useAuth is true).'),('Alert','DEFAULT','management-server','alert.smtp.port','465','Port the SMTP server is listening on.'),('Alert','DEFAULT','management-server','alert.smtp.useAuth',NULL,'If true, use SMTP authentication when sending emails.'),('Alert','DEFAULT','management-server','alert.smtp.username',NULL,'Username for SMTP authentication (applies only if alert.smtp.useAuth is true).'),('Alert','DEFAULT','AgentManager','alert.wait',NULL,'Seconds to wait before alerting on a disconnected agent'),('Advanced','DEFAULT','management-server','allow.public.user.templates','true','If false, users will not be able to create public templates.'),('Usage','DEFAULT','management-server','capacity.check.period','300000','The interval in milliseconds between capacity checks'),('Usage','DEFAULT','management-server','capacity.skipcounting.hours','24','The interval in hours since VM has stopped to skip counting its allocated CPU/Memory capacity'),('Advanced','DEFAULT','management-server','check.pod.cidrs','true','If true, different pods must belong to different CIDR subnets.'),('Hidden','DEFAULT','management-server','cloud.identifier','15b1c778-342a-44b6-a4bb-1d2679195d0d','A unique identifier for the cloud.'),('Console Proxy','DEFAULT','AgentManager','consoleproxy.capacity.standby','10','The minimal number of console proxy viewer sessions that system is able to serve immediately(standby capacity)'),('Console Proxy','DEFAULT','AgentManager','consoleproxy.capacityscan.interval','30000','The time interval(in millisecond) to scan whether or not system needs more console proxy to ensure minimal standby capacity'),('Console Proxy','DEFAULT','AgentManager','consoleproxy.cmd.port','8001','Console proxy command port that is used to communicate with management server'),('Console Proxy','DEFAULT','AgentManager','consoleproxy.loadscan.interval','10000','The time interval(in milliseconds) to scan console proxy working-load info'),('Console Proxy','DEFAULT','AgentManager','consoleproxy.ram.size','1024','RAM size (in MB) used to create new console proxy VMs'),('Console Proxy','DEFAULT','AgentManager','consoleproxy.session.max','50','The max number of viewer sessions console proxy is configured to serve for'),('Console Proxy','DEFAULT','AgentManager','consoleproxy.session.timeout','300000','Timeout(in milliseconds) that console proxy tries to maintain a viewer session before it times out the session for no activity'),('Usage','DEFAULT','management-server','cpu.capacity.threshold','0.85','Percentage (as a value between 0 and 1) of cpu utilization above which alerts will be sent about low cpu available.'),('Advanced','DEFAULT','management-server','cpu.overprovisioning.factor','1','Used for CPU overprovisioning calculation; available CPU will be (actualCpuCapacity * cpu.overprovisioning.factor)'),('Advanced','DEFAULT','management-server','default.page.size','500','Default page size for API list* commands'),('Advanced','DEFAULT','management-server','direct.attach.network.externalIpAllocator.enabled','false','Direct-attach VMs using external DHCP server'),('Advanced','DEFAULT','management-server','direct.attach.network.externalIpAllocator.url',NULL,'Direct-attach VMs using external DHCP server (API url)'),('Advanced','DEFAULT','management-server','direct.attach.network.groups.enabled','false','Ec2-style distributed firewall for direct-attach VMs'),('Advanced','DEFAULT','management-server','direct.attach.untagged.vlan.enabled','false','Indicate whether the system supports direct-attached untagged vlan'),('Premium','DEFAULT','management-server','enable.usage.server','true','Flag for enabling usage'),('Advanced','DEFAULT','management-server','event.purge.delay','0','Events older than specified number days will be purged'),('Advanced','DEFAULT','UserVmManager','expunge.delay','60','Determines how long to wait before actually expunging destroyed vm. The default value = the default value of expunge.interval'),('Advanced','DEFAULT','UserVmManager','expunge.interval','60','The interval to wait before running the expunge thread.'),('Advanced','DEFAULT','UserVmManager','expunge.workers','1','Number of workers performing expunge '),('Advanced','DEFAULT','management-server','host','192.168.130.201','The ip address of management server'),('Advanced','DEFAULT','AgentManager','host.retry','2','Number of times to retry hosts for creating a volume'),('Advanced','DEFAULT','management-server','host.stats.interval','60000','The interval in milliseconds when host stats are retrieved from agents.'),('Advanced','DEFAULT','management-server','hypervisor.type','xenserver','The type of hypervisor that this deployment will use.'),('Hidden','DEFAULT','none','init','true',NULL),('Advanced','DEFAULT','AgentManager','instance.name','VM','Name of the deployment instance.'),('Advanced','DEFAULT','management-server','integration.api.port','8096','Defaul API port'),('Advanced','DEFAULT','HighAvailabilityManager','investigate.retry.interval','60','Time in seconds between VM pings when agent is disconnected'),('Advanced','DEFAULT','management-server','job.expire.minutes','1440','Time (in minutes) for async-jobs to be kept in system'),('Advanced','DEFAULT','management-server','linkLocalIp.nums','10','The number of link local ip that needed by domR(in power of 2)'),('Advanced','DEFAULT','management-server','max.template.iso.size','50','The maximum size for a downloaded template or ISO (in GB).'),('Storage','DEFAULT','management-server','max.volume.size.gb','2000','The maximum size for a volume in Gb.'),('Usage','DEFAULT','management-server','memory.capacity.threshold','0.85','Percentage (as a value between 0 and 1) of memory utilization above which alerts will be sent about low memory available.'),('Advanced','DEFAULT','HighAvailabilityManager','migrate.retry.interval','120','Time in seconds between migration retries'),('Advanced','DEFAULT','management-server','mount.parent','/var/lib/cloud/mnt','The mount point on the Management Server for Secondary Storage.'),('Network','DEFAULT','management-server','multicast.throttling.rate','10','Default multicast rate in megabits per second allowed.'),('Network','DEFAULT','management-server','network.throttling.rate','200','Default data transfer rate in megabits per second allowed.'),('Advanced','DEFAULT','management-server','network.type','vlan','The type of network that this deployment will use.'),('Advanced','DEFAULT','AgentManager','ping.interval','60','Ping interval in seconds'),('Advanced','DEFAULT','AgentManager','ping.timeout','2.5','Multiplier to ping.interval before announcing an agent has timed out'),('Advanced','DEFAULT','AgentManager','port','8250','Port to listen on for agent connection.'),('Usage','DEFAULT','management-server','private.ip.capacity.threshold','0.85','Percentage (as a value between 0 and 1) of private IP address space utilization above which alerts will be sent.'),('Usage','DEFAULT','management-server','public.ip.capacity.threshold','0.85','Percentage (as a value between 0 and 1) of public IP address space utilization above which alerts will be sent.'),('Advanced','DEFAULT','HighAvailabilityManager','restart.retry.interval','600','Time in seconds between retries to restart a vm'),('Advanced','DEFAULT','management-server','router.cleanup.interval','3600','Time in seconds identifies when to stop router when there are no user vms associated with it'),('Advanced','DEFAULT','none','router.ram.size','128','Default RAM for router VM in MB.'),('Advanced','DEFAULT','none','router.stats.interval','300','Interval to report router statistics.'),('Advanced','DEFAULT','none','router.template.id','1','Default ID for template.'),('Hidden','DEFAULT','database','schema.level','2.1.3','The schema level of this database'),('Advanced','DEFAULT','management-server','secondary.storage.vm','true','Deploys a VM per zone to manage secondary storage if true, otherwise secondary storage is mounted on management server'),('Advanced','DEFAULT','management-server','secstorage.allowed.internal.sites','192.168.110.231','Comma separated list of cidrs internal to the datacenter that can host template download servers'),('Hidden','DEFAULT','management-server','secstorage.copy.password','vW3zfstbc','Password used to authenticate zone-to-zone template copy requests'),('Advanced','DEFAULT','management-server','secstorage.encrypt.copy','true','Use SSL method used to encrypt copy traffic between zones'),('Advanced','DEFAULT','management-server','secstorage.ssl.cert.domain','realhostip.com','SSL certificate used to encrypt copy traffic between zones'),('Advanced','DEFAULT','AgentManager','secstorage.vm.ram.size',NULL,'RAM size (in MB) used to create new secondary storage vms'),('Hidden','DEFAULT','management-server','security.hash.key','f128657d-9d2c-4912-ad0a-42aa47094482','for generic key-ed hash'),('Hidden','DEFAULT','management-server','security.singlesignon.key','ZWGT__G3_UVfiJMGN7VQN3yF8VuWS18pblZ6F-F9ponXtyfC50n-k7ppsBgjiBnE00xCIuvYoJWSWC_XDfg2TA','A Single Sign-On key used for logging into the cloud'),('Advanced','DEFAULT','management-server','security.singlesignon.tolerance.millis','300000','The allowable clock difference in milliseconds between when an SSO login request is made and when it is received.'),('Snapshots','DEFAULT','none','snapshot.max.daily','8','Maximum dalily snapshots for a volume'),('Snapshots','DEFAULT','none','snapshot.max.hourly','8','Maximum hourly snapshots for a volume'),('Snapshots','DEFAULT','none','snapshot.max.monthly','8','Maximum hourly snapshots for a volume'),('Snapshots','DEFAULT','none','snapshot.max.weekly','8','Maximum hourly snapshots for a volume'),('Snapshots','DEFAULT','none','snapshot.poll.interval','300','The time interval in seconds when the management server polls for snapshots to be scheduled.'),('Hidden','DEFAULT','management-server','ssh.privatekey','-----BEGIN RSA PRIVATE KEY-----\nMIIEoQIBAAKCAQEA4gZlrwp8SJHiECUgiKGXf4P+zqUAt8tEH5nWzmVSBhuvQuSu\nS8lMuX8zwmKb5WvnFC3PMySoNe0gJBgg7g6q660oca1qQ1xgBatDI4BKs+wbDhm2\n2GEhRJuFze/DuqXyb18fSER/mp2GZSJOVo6eZ6qkkiGm4yjgzmZVJdJOOgkqpUZn\nAxOPa5bU6D0XhV1NoQS/AYTYL7P0c5Zoej/HGOZtRd67NE/5S4+FMWMzHuat96/j\nArAu3a4rUIle99q7I31JDFvFF2+iZI/a1glaHnOWPQ/X7dCjcrqytcL1wrF0YUMP\nl0WkjoaDQgzjoX/GtfYArivQzHgF+KElgsTGsQIBIwKCAQATX6JQ1QNWrWsl9I3C\nkYIDnZl/bTqTaTG5kNflziuaH6FO4Ga2CfCiL3l3D8Q4PHLkePSAuf/KG6OrUoZ6\nzg6mfI5wJM6YDztuM0BE3xxYkJSazwELO4aCOTday2iMV13sSftd9z4jMhLVd/gW\nDDl2mZkT1v+tEiHlzkHQCrY/fPeghDwZ/cCafpheR/vuoJCRkVdvLl/b4id2ExYU\nQdv2uFMcO1ltpOJF7zUlDYRlMEAKPPHQKtAqzMIQr6YGGhNghonGh8EhPldez8UK\nUD+71J98rWG+J6lvQwg9eeaGZIzTkIoLFkGisECAHUDQN46BDJDgMPPnp7u/zfnX\nd1z/AoGBAPyPz5NqfeTvwJ/3sPAofVybQr24LVnz3m/WElZlD7QMrvTB5EPfuYfS\nXX76fAOQd+sEUXbU9i3TF8YzH8zoSm3AdjskrGFK48gOIHP+swuV/UtB3CBrV4JO\n1KzC3hSVaHynRQTxBpYCEqDsmGTLL6x5R8ysaUeO8W5LmadmIiWLAoGBAOUaGsEU\nBTjGu+357wF85AUOIlqNj8qL3GLmpWXThQzN0QQ6m2QAEsxJS0Tb9Cbx5oJ77Lxb\nxoRsluJavavIYBX85XEw1OvzXzRZoha0WLxOcFuh95iCqmhnLVB7AV+4CTdrEmrP\nkLt3wPHUHzVZHUZbG5nKMhZZjF6Z+cX1h2QzAoGAFaXtOIV4gVZg98TGBfTXi5hH\njJrCDwZGRBmpzOQXSfJ1ZW+0etipgKuhnStptymj4PG91vxPnYcfS4C5lTh7aH42\nBRHFofB57JN/H+KhoehI6TGH5YWLKG0oLBCz8yK/0CuYM5+hepJ2oBRHk5xjLApl\nPW3dI2QGEMSmxTSj11UCgYEA3o5jIfYw9VNXeXZzJgRLOBxb4u/qxMJhD546Rajn\npgnDuvcam6hMxnMV6x7P61FjlLLl+M4uj0TqWDrkIzetzDd/zQrdd360QXukxZlA\nQeXMO8HpNRE30yJmiLIBVat2qt0Kk6UQQRU/Fs4PsCquuV/Rq1a0UDm7jxk7x54O\nfpcCgYB55e5G2SkAGQ+jfS15BWqoCcTQv+8nnv7kBTOu6xupQq58QmKmD0HdQ9VU\nmGwtD8/Ohvsb/2g+8w7T+fgtBFjYdihtNUYHGW8tlwzRz4Qy4QLTam9q+D3mSyz2\nMZ4Fd/iDtsGTbyW4bgh2if0ZLlBBqPXzQK9303Li6VXJ92tURw==\n-----END RSA PRIVATE KEY-----','Private key for the entire CloudStack'),('Hidden','DEFAULT','management-server','ssh.publickey','ssh-rsa AAAAB3NzaC1yc2EAAAABIwAAAQEA4gZlrwp8SJHiECUgiKGXf4P+zqUAt8tEH5nWzmVSBhuvQuSuS8lMuX8zwmKb5WvnFC3PMySoNe0gJBgg7g6q660oca1qQ1xgBatDI4BKs+wbDhm22GEhRJuFze/DuqXyb18fSER/mp2GZSJOVo6eZ6qkkiGm4yjgzmZVJdJOOgkqpUZnAxOPa5bU6D0XhV1NoQS/AYTYL7P0c5Zoej/HGOZtRd67NE/5S4+FMWMzHuat96/jArAu3a4rUIle99q7I31JDFvFF2+iZI/a1glaHnOWPQ/X7dCjcrqytcL1wrF0YUMPl0WkjoaDQgzjoX/GtfYArivQzHgF+KElgsTGsQ== cloud@i-11-246293-VM','Public key for the entire CloudStack'),('Advanced','DEFAULT','AgentManager','start.retry','10','Number of times to retry create and start commands'),('Advanced','DEFAULT','HighAvailabilityManager','stop.retry.interval','600','Time in seconds between retries to stop or destroy a vm'),('Usage','DEFAULT','management-server','storage.allocated.capacity.threshold','0.85','Percentage (as a value between 0 and 1) of allocated storage utilization above which alerts will be sent about low storage available.'),('Usage','DEFAULT','management-server','storage.capacity.threshold','0.85','Percentage (as a value between 0 and 1) of storage utilization above which alerts will be sent about low storage available.'),('Advanced','DEFAULT','none','storage.cleanup.enabled','true','Enables/disables the storage cleanup thread.'),('Advanced','DEFAULT','none','storage.cleanup.interval','86400','The interval to wait before running the storage cleanup thread.'),('Storage','DEFAULT','StorageAllocator','storage.overprovisioning.factor','2','Used for storage overprovisioning calculation; available storage will be (actualStorageSize * storage.overprovisioning.factor)'),('Storage','DEFAULT','management-server','storage.stats.interval','60000','The interval in milliseconds when storage stats (per host) are retrieved from agents.'),('Advanced','DEFAULT','management-server','system.vm.use.local.storage','false','Indicates whether to use local storage pools or shared storage pools for system VMs.'),('Storage','DEFAULT','AgentManager','total.retries','4','The number of times each command sent to a host should be retried in case of failure.'),('Advanced','DEFAULT','AgentManager','update.wait','600','Time to wait before alerting on a updating agent'),('Advanced','DEFAULT','management-server','upgrade.url','http://example.com:8080/client/agent/update.zip','The upgrade URL is the URL of the management server that agents will connect to in order to automatically upgrade.'),('Premium','DEFAULT','management-server','usage.execution.timezone',NULL,'The timezone to use for usage job execution time'),('Premium','DEFAULT','management-server','usage.stats.job.aggregation.range','1440','The range of time for aggregating the user statistics specified in minutes (e.g. 1440 for daily, 60 for hourly.'),('Premium','DEFAULT','management-server','usage.stats.job.exec.time','00:15','The time at which the usage statistics aggregation job will run as an HH24:MM time, e.g. 00:30 to run at 12:30am.'),('Premium','DEFAULT','management-server','use.local.storage','false','Should we use the local storage if it\'s available?'),('Advanced','DEFAULT','management-server','vm.allocation.algorithm','random','If \'random\', hosts within a pod will be randomly considered for VM/volume allocation. If \'firstfit\', they will be considered on a first-fit basis.'),('Advanced','DEFAULT','AgentManager','wait','1800','Time to wait for control commands to return'),('Advanced','DEFAULT','AgentManager','workers','5','Number of worker threads.'),('Advanced','DEFAULT','management-server','xen.bond.storage.nics',NULL,'Attempt to bond the two networks if found'),('Hidden','DEFAULT','management-server','xen.create.pools.in.pod','false','Should we automatically add XenServers into pools that are inside a Pod'),('Advanced','DEFAULT','management-server','xen.guest.network.device',NULL,'Specify when the guest network does not go over the private network'),('Advanced','DEFAULT','management-server','xen.heartbeat.interval','60','heartbeat to use when implementing XenServer Self Fencing'),('Advanced','DEFAULT','management-server','xen.max.product.version','5.6.0','Maximum XenServer version'),('Advanced','DEFAULT','management-server','xen.max.version','3.4.2','Maximum Xen version'),('Advanced','DEFAULT','management-server','xen.max.xapi.version','1.3','Maximum Xapi Tool Stack version'),('Advanced','DEFAULT','management-server','xen.min.product.version','0.1.1','Minimum XenServer version'),('Advanced','DEFAULT','management-server','xen.min.version','3.3.1','Minimum Xen version'),('Advanced','DEFAULT','management-server','xen.min.xapi.version','1.3','Minimum Xapi Tool Stack version'),('Advanced','DEFAULT','management-server','xen.preallocated.lun.size.range','.05','percentage to add to disk size when allocating'),('Network','DEFAULT','management-server','xen.private.network.device',NULL,'Specify when the private network name is different'),('Network','DEFAULT','management-server','xen.public.network.device',NULL,'[ONLY IF THE PUBLIC NETWORK IS ON A DEDICATED NIC]:The network name label of the physical device dedicated to the public network on a XenServer host'),('Advanced','DEFAULT','management-server','xen.setup.multipath','false','Setup the host to do multipath'),('Network','DEFAULT','management-server','xen.storage.network.device1','cloud-stor1','Specify when there are storage networks'),('Network','DEFAULT','management-server','xen.storage.network.device2','cloud-stor2','Specify when there are storage networks');
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
  `gateway` varchar(15) default NULL COMMENT 'gateway info for this console proxy towards public network interface',
  `dns1` varchar(15) default NULL COMMENT 'dns1',
  `dns2` varchar(15) default NULL COMMENT 'dns2',
  `domain` varchar(255) default NULL COMMENT 'domain',
  `public_mac_address` varchar(17) NOT NULL COMMENT 'mac address of the public facing network card',
  `public_ip_address` varchar(15) default NULL COMMENT 'public ip address for the console proxy',
  `public_netmask` varchar(15) default NULL COMMENT 'public netmask used for the console proxy',
  `guest_mac_address` varchar(17) NOT NULL COMMENT 'mac address of the guest facing network card',
  `guest_ip_address` varchar(15) default NULL COMMENT 'guest ip address for the console proxy',
  `guest_netmask` varchar(15) default NULL COMMENT 'guest netmask used for the console proxy',
  `vlan_db_id` bigint(20) unsigned default NULL COMMENT 'Foreign key into vlan id table',
  `vlan_id` varchar(255) default NULL COMMENT 'optional VLAN ID for console proxy that can be used',
  `ram_size` int(10) unsigned NOT NULL default '512' COMMENT 'memory to use in mb',
  `active_session` int(10) NOT NULL default '0' COMMENT 'active session number',
  `last_update` datetime default NULL COMMENT 'Last session update time',
  `session_details` blob COMMENT 'session detail info',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `public_mac_address` (`public_mac_address`),
  UNIQUE KEY `guest_mac_address` (`guest_mac_address`),
  UNIQUE KEY `public_ip_address` (`public_ip_address`),
  UNIQUE KEY `guest_ip_address` (`guest_ip_address`),
  KEY `i_console_proxy__vlan_id` (`vlan_db_id`),
  CONSTRAINT `fk_console_proxy__vlan_id` FOREIGN KEY (`vlan_db_id`) REFERENCES `vlan` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `console_proxy`
--

LOCK TABLES `console_proxy` WRITE;
/*!40000 ALTER TABLE `console_proxy` DISABLE KEYS */;
INSERT INTO `console_proxy` VALUES (2,'172.16.36.1','72.52.126.11','72.52.126.12','foo.com','06:81:ea:ec:00:03','172.16.36.27','255.255.252.0','06:01:e4:95:00:04',NULL,'255.255.0.0',1,'509',1024,0,'2011-04-23 02:52:19','{\"connections\":[]}\n');
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
  `guest_network_cidr` varchar(15) default NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `data_center`
--

LOCK TABLES `data_center` WRITE;
/*!40000 ALTER TABLE `data_center` DISABLE KEYS */;
INSERT INTO `data_center` VALUES (1,'ZONE1',NULL,'72.52.126.11','72.52.126.12','192.168.110.253','192.168.110.254',NULL,NULL,'1240-1319','02:00:00:00:00:01',9,'10.1.1.0/24');
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
  `display_text` varchar(4096) default NULL COMMENT 'Description text set by the admin for display purpose only',
  `disk_size` bigint(20) unsigned NOT NULL COMMENT 'disk space in mbs',
  `mirrored` tinyint(1) unsigned NOT NULL default '1' COMMENT 'Enable mirroring?',
  `type` varchar(32) default NULL COMMENT 'inheritted by who?',
  `tags` varchar(4096) default NULL COMMENT 'comma separated tags about the disk_offering',
  `recreatable` tinyint(1) unsigned NOT NULL default '0' COMMENT 'The root disk is always recreatable',
  `use_local_storage` tinyint(1) unsigned NOT NULL default '0' COMMENT 'Indicates whether local storage pools should be used',
  `unique_name` varchar(32) default NULL COMMENT 'unique name',
  `removed` datetime default NULL COMMENT 'date removed',
  `created` datetime default NULL COMMENT 'date the disk offering was created',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `unique_name` (`unique_name`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `disk_offering`
--

LOCK TABLES `disk_offering` WRITE;
/*!40000 ALTER TABLE `disk_offering` DISABLE KEYS */;
INSERT INTO `disk_offering` VALUES (1,NULL,'Small Instance, Virtual Networking','Small Instance, Virtual Networking, $0.05 per hour',0,0,'Service',NULL,0,0,NULL,NULL,'2011-04-23 02:12:58'),(2,NULL,'Medium Instance, Virtual Networking','Medium Instance, Virtual Networking, $0.10 per hour',0,0,'Service',NULL,0,0,NULL,NULL,'2011-04-23 02:12:58'),(3,NULL,'Small Instance, Direct Networking','Small Instance, Direct Networking, $0.05 per hour',0,0,'Service',NULL,0,0,NULL,NULL,'2011-04-23 02:12:58'),(4,NULL,'Medium Instance, Direct Networking','Medium Instance, Direct Networking, $0.10 per hour',0,0,'Service',NULL,0,0,NULL,NULL,'2011-04-23 02:12:58'),(5,1,'Small','Small Disk, 5 GB',5120,0,'Disk',NULL,0,0,NULL,NULL,'2011-04-23 02:12:58'),(6,1,'Medium','Medium Disk, 20 GB',20480,0,'Disk',NULL,0,0,NULL,NULL,'2011-04-23 02:12:58'),(7,1,'Large','Large Disk, 100 GB',102400,0,'Disk',NULL,0,0,NULL,NULL,'2011-04-23 02:12:58'),(8,NULL,'Fake Offering For DomR',NULL,0,0,'Service',NULL,1,0,'Cloud.Com-SoftwareRouter','2011-04-23 02:13:04','2011-04-23 02:13:04'),(9,NULL,'Fake Offering For Secondary Storage VM',NULL,0,0,'Service',NULL,1,0,'Cloud.com-SecondaryStorage','2011-04-23 02:13:04','2011-04-23 02:13:04'),(10,NULL,'Fake Offering For DomP',NULL,0,0,'Service',NULL,1,0,'Cloud.com-ConsoleProxy','2011-04-23 02:13:04','2011-04-23 02:13:04'),(11,NULL,'Little Instance, Virtual Networking','Little Instance, Virtual Networking',0,0,'Service',NULL,0,0,NULL,NULL,'2011-04-23 02:17:06'),(12,1,'Little','Little%20Disk%2C%201%20GB',1024,0,'Disk',NULL,0,0,NULL,NULL,'2011-04-23 02:23:42');
/*!40000 ALTER TABLE `disk_offering` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `disk_template_ref`
--

DROP TABLE IF EXISTS `disk_template_ref`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `disk_template_ref` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `description` varchar(255) NOT NULL,
  `host` varchar(255) NOT NULL COMMENT 'host on which the server exists',
  `parent` varchar(255) NOT NULL COMMENT 'parent path',
  `path` varchar(255) NOT NULL,
  `size` int(10) unsigned NOT NULL COMMENT 'size of the disk',
  `type` varchar(255) NOT NULL COMMENT 'file system type',
  `created` datetime NOT NULL COMMENT 'Date created',
  `removed` datetime default NULL COMMENT 'Date removed if not null',
  PRIMARY KEY  (`id`),
  KEY `i_disk_template_ref__removed` (`removed`),
  KEY `i_disk_template_ref__type__size` (`type`,`size`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `disk_template_ref`
--

LOCK TABLES `disk_template_ref` WRITE;
/*!40000 ALTER TABLE `disk_template_ref` DISABLE KEYS */;
/*!40000 ALTER TABLE `disk_template_ref` ENABLE KEYS */;
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
  `gateway` varchar(15) NOT NULL COMMENT 'ip address of the gateway to this domR',
  `ram_size` int(10) unsigned NOT NULL default '128' COMMENT 'memory to use in mb',
  `dns1` varchar(15) default NULL COMMENT 'dns1',
  `dns2` varchar(15) default NULL COMMENT 'dns2',
  `domain` varchar(255) default NULL COMMENT 'domain',
  `public_mac_address` varchar(17) default NULL COMMENT 'mac address of the public facing network card',
  `public_ip_address` varchar(15) default NULL COMMENT 'public ip address used for source net',
  `public_netmask` varchar(15) default NULL COMMENT 'netmask used for the domR',
  `guest_mac_address` varchar(17) NOT NULL COMMENT 'mac address of the pod facing network card',
  `guest_dc_mac_address` varchar(17) default NULL COMMENT 'mac address of the data center facing network card',
  `guest_netmask` varchar(15) NOT NULL COMMENT 'netmask used for the guest network',
  `guest_ip_address` varchar(15) NOT NULL COMMENT ' ip address in the guest network',
  `vnet` varchar(18) default NULL COMMENT 'vnet',
  `dc_vlan` varchar(18) default NULL COMMENT 'vnet',
  `vlan_db_id` bigint(20) unsigned default NULL COMMENT 'Foreign key into vlan id table',
  `vlan_id` varchar(255) default NULL COMMENT 'optional VLAN ID for DomainRouter that can be used in rundomr.sh',
  `account_id` bigint(20) unsigned NOT NULL COMMENT 'account id of owner',
  `domain_id` bigint(20) unsigned NOT NULL,
  `dhcp_ip_address` bigint(20) unsigned NOT NULL default '2' COMMENT 'next ip address for dhcp for this domR',
  `role` varchar(64) NOT NULL COMMENT 'type of role played by this router',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`),
  KEY `i_domain_router__public_ip_address` (`public_ip_address`),
  KEY `i_domain_router__account_id` (`account_id`),
  KEY `i_domain_router__vlan_id` (`vlan_db_id`),
  CONSTRAINT `fk_domain_router__account_id` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`),
  CONSTRAINT `fk_domain_router__id` FOREIGN KEY (`id`) REFERENCES `vm_instance` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_domain_router__public_ip_address` FOREIGN KEY (`public_ip_address`) REFERENCES `user_ip_address` (`public_ip_address`),
  CONSTRAINT `fk_domain_router__vlan_id` FOREIGN KEY (`vlan_db_id`) REFERENCES `vlan` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='information about the domR instance';
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `domain_router`
--

LOCK TABLES `domain_router` WRITE;
/*!40000 ALTER TABLE `domain_router` DISABLE KEYS */;
INSERT INTO `domain_router` VALUES (4,'172.16.36.1',128,'72.52.126.11','72.52.126.12','v2.myvm.com','06:81:23:a6:00:05','172.16.36.59','255.255.252.0','02:00:04:d8:00:01',NULL,'255.255.255.0','10.1.1.1',NULL,NULL,1,'509',2,1,2,'DHCP_FIREWALL_LB_PASSWD_USERDATA'),(7,'172.16.36.1',128,'72.52.126.11','72.52.126.12','v3.myvm.com','06:81:29:75:00:07','172.16.36.16','255.255.252.0','02:00:04:d9:00:01',NULL,'255.255.255.0','10.1.1.1',NULL,NULL,1,'509',3,2,2,'DHCP_FIREWALL_LB_PASSWD_USERDATA');
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
) ENGINE=InnoDB AUTO_INCREMENT=128 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `event`
--

LOCK TABLES `event` WRITE;
/*!40000 ALTER TABLE `event` DISABLE KEYS */;
INSERT INTO `event` VALUES (1,'SERVICE.OFFERING.CREATE','Completed','Successfully created new service offering with name: Small Instance, Virtual Networking.',1,1,'2011-04-23 02:12:58','INFO',0,'soId=1\nname=Small Instance, Virtual Networking\nnumCPUs=1\nram=512\ncpuSpeed=500\ndisplayText=Small Instance, Virtual Networking, $0.05 per hour\nguestIPType=Virtualized\nlocalStorageRequired=false\nofferHA=false\nuseVirtualNetwork=true\n'),(2,'SERVICE.OFFERING.CREATE','Completed','Successfully created new service offering with name: Medium Instance, Virtual Networking.',1,1,'2011-04-23 02:12:58','INFO',0,'soId=2\nname=Medium Instance, Virtual Networking\nnumCPUs=1\nram=1024\ncpuSpeed=1000\ndisplayText=Medium Instance, Virtual Networking, $0.10 per hour\nguestIPType=Virtualized\nlocalStorageRequired=false\nofferHA=false\nuseVirtualNetwork=true\n'),(3,'SERVICE.OFFERING.CREATE','Completed','Successfully created new service offering with name: Small Instance, Direct Networking.',1,1,'2011-04-23 02:12:58','INFO',0,'soId=3\nname=Small Instance, Direct Networking\nnumCPUs=1\nram=512\ncpuSpeed=500\ndisplayText=Small Instance, Direct Networking, $0.05 per hour\nguestIPType=DirectSingle\nlocalStorageRequired=false\nofferHA=false\nuseVirtualNetwork=false\n'),(4,'SERVICE.OFFERING.CREATE','Completed','Successfully created new service offering with name: Medium Instance, Direct Networking.',1,1,'2011-04-23 02:12:58','INFO',0,'soId=4\nname=Medium Instance, Direct Networking\nnumCPUs=1\nram=1024\ncpuSpeed=1000\ndisplayText=Medium Instance, Direct Networking, $0.10 per hour\nguestIPType=DirectSingle\nlocalStorageRequired=false\nofferHA=false\nuseVirtualNetwork=false\n'),(5,'CONFIGURATION.VALUE.EDIT','Completed','Successfully edited configuration value.',1,1,'2011-04-23 02:12:58','INFO',0,'name=mount.parent\nvalue=/var/lib/cloud/mnt'),(6,'CONFIGURATION.VALUE.EDIT','Completed','Successfully edited configuration value.',1,1,'2011-04-23 02:12:58','INFO',0,'name=host\nvalue=192.168.130.201'),(7,'ZONE.CREATE','Completed','Successfully created new zone with name: Default.',1,1,'2011-04-23 02:12:59','INFO',0,'dcId=1\ndns1=192.168.130.184\ninternalDns1=192.168.130.184\nvnetRange=1000-2000\nguestCidr=10.1.1.0/24'),(8,'POD.CREATE','Completed','Successfully created new pod with name: Default in zone: Default.',1,1,'2011-04-23 02:12:59','INFO',0,'podId=1\nzoneId=1\ngateway=192.168.130.1\ncidr=192.168.130.1/24\n'),(9,'ZONE.EDIT','Completed','Successfully edited zone with name: ZONE1.',1,1,'2011-04-23 02:17:06','INFO',0,'dcId=1\ndns1=72.52.126.11\ndns2=72.52.126.12\ninternalDns1=192.168.110.253\ninternalDns2=192.168.110.254\nvnetRange=1240-1319\nguestCidr=10.1.1.0/24'),(10,'POD.EDIT','Completed','Successfully edited pod. New pod name is: POD1 and new zone name is: ZONE1.',1,1,'2011-04-23 02:17:06','INFO',0,'podId=1\ndcId=1\ngateway=192.168.163.1\ncidr=192.168.163.0/24\nstartIp=192.168.163.100\nendIp=192.168.163.110'),(11,'VLAN.IP.RANGE.CREATE','Completed','Successfully created new VLAN (tag = 509, gateway = 172.16.36.1, netmask = 255.255.252.0, start IP = 172.16.36.2, end IP = 172.16.36.63.',1,1,'2011-04-23 02:17:06','INFO',0,'vlanType=VirtualNetwork\ndcId=1\nvlanId=509\nvlanGateway=172.16.36.1\nvlanNetmask=255.255.252.0\nstartIP=172.16.36.2\nendIP=172.16.36.63\n'),(12,'SERVICE.OFFERING.CREATE','Completed','Successfully created new service offering with name: Little Instance, Virtual Networking.',1,1,'2011-04-23 02:17:06','INFO',0,'soId=11\nname=Little Instance, Virtual Networking\nnumCPUs=1\nram=128\ncpuSpeed=100\ndisplayText=Little Instance, Virtual Networking\nguestIPType=Virtualized\nlocalStorageRequired=false\nofferHA=false\nuseVirtualNetwork=true\n'),(13,'CONFIGURATION.VALUE.EDIT','Completed','Successfully edited configuration value.',1,1,'2011-04-23 02:17:06','INFO',0,'name=expunge.delay\nvalue=60'),(14,'CONFIGURATION.VALUE.EDIT','Completed','Successfully edited configuration value.',1,1,'2011-04-23 02:17:06','INFO',0,'name=expunge.interval\nvalue=60'),(15,'TEMPLATE.DOWNLOAD.FAILED','Completed','Storage server nfs://192.168.110.232/export/home/chandan/eagle/secondary21x disconnected during download of template CentOS 5.3(x86_64) no GUI',1,1,'2011-04-23 02:18:42','WARN',0,NULL),(16,'TEMPLATE.DOWNLOAD.FAILED','Completed','CentOS 5.3(x86_64) no GUI failed to download to storage server nfs://192.168.110.232/export/home/chandan/eagle/secondary21x',1,1,'2011-04-23 02:18:42','ERROR',0,NULL),(17,'USER.LOGIN','Completed','user has logged in',2,2,'2011-04-23 02:19:21','INFO',0,NULL),(18,'SSVM.CREATE','Completed','New Secondary Storage VM created - s-1-VM',1,1,'2011-04-23 02:19:21','INFO',0,NULL),(19,'PROXY.CREATE','Completed','New console proxy created - v-2-VM',1,1,'2011-04-23 02:19:22','INFO',0,NULL),(20,'SSVM.START','Started','Starting secondary storage Vm Id: 1',1,1,'2011-04-23 02:20:46','INFO',0,NULL),(21,'SSVM.START','Completed','Secondary Storage VM started - s-1-VM',1,1,'2011-04-23 02:21:19','INFO',0,NULL),(22,'PROXY.START','Completed','Console proxy started - v-2-VM',1,1,'2011-04-23 02:21:50','INFO',0,NULL),(23,'VM.CREATE','Scheduled','Scheduled async job for deploying Vm',2,2,'2011-04-23 02:24:06','INFO',0,NULL),(24,'VM.CREATE','Started','Deploying Vm',2,2,'2011-04-23 02:24:06','INFO',23,NULL),(25,'NET.IPASSIGN','Completed','Acquired a public ip: 172.16.36.59',1,2,'2011-04-23 02:24:06','INFO',0,'address=172.16.36.59\nsourceNat=true\ndcId=1'),(26,'ROUTER.CREATE','Completed','successfully created Domain Router : r-4-VM with ip : 172.16.36.59',1,2,'2011-04-23 02:24:07','INFO',0,NULL),(27,'VOLUME.CREATE','Completed','Created volume: i-2-3-VM-ROOT with size: 8192 MB',2,2,'2011-04-23 02:25:11','INFO',0,'id=4\ndoId=-1\ntId=2\ndcId=1\nsize=8192'),(28,'VOLUME.CREATE','Completed','Created volume: i-2-3-VM-DATA with size: 1024 MB',2,2,'2011-04-23 02:25:11','INFO',0,'id=5\ndoId=12\ntId=-1\ndcId=1\nsize=1024'),(29,'VM.CREATE','Completed','successfully created VM instance : i-2-3-VM(Admin-VM-1)',2,2,'2011-04-23 02:25:11','INFO',23,'id=3\nvmName=i-2-3-VM\nsoId=11\ndoId=12\ntId=2\ndcId=1'),(30,'VM.START','Started','Starting Vm with Id: 3',2,2,'2011-04-23 02:25:11','INFO',0,NULL),(31,'ROUTER.START','Started','Starting Router with Id: 4',1,2,'2011-04-23 02:25:11','INFO',0,NULL),(32,'ROUTER.START','Completed','successfully started Domain Router: r-4-VM',1,2,'2011-04-23 02:25:46','INFO',0,NULL),(33,'VM.START','Completed','successfully started VM: i-2-3-VM(Admin-VM-1)',2,2,'2011-04-23 02:25:53','INFO',30,'id=3\nvmName=i-2-3-VM\nsoId=11\ndoId=-1\ntId=2\ndcId=1'),(34,'NET.IPASSIGN','Completed','Assigned a public IP address: 172.16.36.53',2,2,'2011-04-23 02:26:14','INFO',0,'address=172.16.36.53\nsourceNat=false\ndcId=1'),(35,'NET.RULEADD','Completed','created new ip forwarding rule [172.16.36.53:22]->[10.1.1.2:22] TCP',2,2,'2011-04-23 02:26:21','INFO',0,NULL),(36,'VM.CREATE','Scheduled','Scheduled async job for deploying Vm',2,2,'2011-04-23 02:26:35','INFO',0,NULL),(37,'VM.CREATE','Started','Deploying Vm',2,2,'2011-04-23 02:26:35','INFO',36,NULL),(38,'VOLUME.CREATE','Completed','Created volume: i-2-5-VM-ROOT with size: 8192 MB',2,2,'2011-04-23 02:26:36','INFO',0,'id=6\ndoId=-1\ntId=2\ndcId=1\nsize=8192'),(39,'VM.CREATE','Completed','successfully created VM instance : i-2-5-VM',2,2,'2011-04-23 02:26:36','INFO',36,'id=5\nvmName=i-2-5-VM\nsoId=11\ndoId=-1\ntId=2\ndcId=1'),(40,'VM.START','Started','Starting Vm with Id: 5',2,2,'2011-04-23 02:26:36','INFO',0,NULL),(41,'VM.START','Completed','successfully started VM: i-2-5-VM',2,2,'2011-04-23 02:26:44','INFO',40,'id=5\nvmName=i-2-5-VM\nsoId=11\ndoId=-1\ntId=2\ndcId=1'),(42,'LB.CREATE','Completed','Successfully created load balancer LBRule-1 on ip address 172.16.36.53[80->80]',2,2,'2011-04-23 02:27:48','INFO',0,'id=1\ndcId=1'),(43,'NET.RULEADD','Completed','created new load balancer rule [172.16.36.53:80]->[10.1.1.2:80] TCP',2,2,'2011-04-23 02:27:57','INFO',0,NULL),(44,'NET.RULEADD','Completed','created new load balancer rule [172.16.36.53:80]->[10.1.1.3:80] TCP',2,2,'2011-04-23 02:28:05','INFO',0,NULL),(45,'SNAPSHOT.CREATE','Completed','Backed up snapshot id: 3 to secondary for volume 5',2,2,'2011-04-23 02:33:54','INFO',0,'id=3\nssName=Admin-VM-1_i-2-3-VM-DATA_20110423023351\nsize=1073741824\ndcId=1'),(46,'SNAPSHOT.CREATE','Completed','Backed up snapshot id: 1 to secondary for volume 6',2,2,'2011-04-23 02:34:04','INFO',0,'id=1\nssName=i-2-5-VM_i-2-5-VM-ROOT_20110423023346\nsize=8589934592\ndcId=1'),(47,'DOMAIN.CREATE','Completed','Domain, CHILD created with owner id = 2 and parentId 1',1,2,'2011-04-23 02:34:05','INFO',0,NULL),(48,'SNAPSHOT.CREATE','Completed','Backed up snapshot id: 2 to secondary for volume 4',2,2,'2011-04-23 02:34:05','INFO',0,'id=2\nssName=Admin-VM-1_i-2-3-VM-ROOT_20110423023348\nsize=8589934592\ndcId=1'),(49,'USER.CREATE','Completed','User, nimbus for accountId = 3 and domainId = 2 was created.',1,1,'2011-04-23 02:34:39','INFO',0,NULL),(50,'TEMPLATE.DOWNLOAD.START','Completed','Storage server nfs://192.168.110.232/export/home/chandan/eagle/secondary21x started download of template systemvm-xenserver-2.2.4',1,2,'2011-04-23 02:35:51','INFO',0,NULL),(51,'USER.LOGOUT','Completed','user has logged out',2,2,'2011-04-23 02:37:51','INFO',0,NULL),(52,'TEMPLATE.CREATE','Completed','Created template TemplateFromSnapshot-3-Data from snapshot 3',2,2,'2011-04-23 02:37:52','INFO',0,'id=204\nname=TemplateFromSnapshot-3-Data\ndcId=1\nsize=1073741824'),(53,'USER.LOGIN','Completed','user has logged in',3,3,'2011-04-23 02:38:01','INFO',0,NULL),(54,'VM.CREATE','Scheduled','Scheduled async job for deploying Vm',3,3,'2011-04-23 02:38:51','INFO',0,NULL),(55,'VM.CREATE','Started','Deploying Vm',3,3,'2011-04-23 02:38:51','INFO',54,NULL),(56,'NET.IPASSIGN','Completed','Acquired a public ip: 172.16.36.16',1,3,'2011-04-23 02:38:51','INFO',0,'address=172.16.36.16\nsourceNat=true\ndcId=1'),(57,'ROUTER.CREATE','Completed','successfully created Domain Router : r-7-VM with ip : 172.16.36.16',1,3,'2011-04-23 02:38:54','INFO',0,NULL),(58,'VOLUME.CREATE','Completed','Created volume: i-3-6-VM-ROOT with size: 8192 MB',3,3,'2011-04-23 02:38:56','INFO',0,'id=8\ndoId=-1\ntId=2\ndcId=1\nsize=8192'),(59,'VOLUME.CREATE','Completed','Created volume: i-3-6-VM-DATA with size: 1024 MB',3,3,'2011-04-23 02:38:56','INFO',0,'id=9\ndoId=12\ntId=-1\ndcId=1\nsize=1024'),(60,'VM.CREATE','Completed','successfully created VM instance : i-3-6-VM(Nimbus-VM-1)',3,3,'2011-04-23 02:38:56','INFO',54,'id=6\nvmName=i-3-6-VM\nsoId=11\ndoId=12\ntId=2\ndcId=1'),(61,'VM.START','Started','Starting Vm with Id: 6',3,3,'2011-04-23 02:38:56','INFO',0,NULL),(62,'ROUTER.START','Started','Starting Router with Id: 7',1,3,'2011-04-23 02:38:56','INFO',0,NULL),(63,'VM.CREATE','Scheduled','Scheduled async job for deploying Vm',3,3,'2011-04-23 02:39:07','INFO',0,NULL),(64,'VM.CREATE','Started','Deploying Vm',3,3,'2011-04-23 02:39:08','INFO',63,NULL),(65,'ROUTER.START','Completed','successfully started Domain Router: r-7-VM',1,3,'2011-04-23 02:40:07','INFO',0,NULL),(66,'VOLUME.CREATE','Completed','Created volume: i-3-8-VM-ROOT with size: 8192 MB',3,3,'2011-04-23 02:40:12','INFO',0,'id=10\ndoId=-1\ntId=2\ndcId=1\nsize=8192'),(67,'VM.CREATE','Completed','successfully created VM instance : i-3-8-VM(Nimbus-VM-2)',3,3,'2011-04-23 02:40:12','INFO',63,'id=8\nvmName=i-3-8-VM\nsoId=11\ndoId=-1\ntId=2\ndcId=1'),(68,'VM.START','Started','Starting Vm with Id: 8',3,3,'2011-04-23 02:40:12','INFO',0,NULL),(69,'VM.START','Completed','successfully started VM: i-3-6-VM(Nimbus-VM-1)',3,3,'2011-04-23 02:40:18','INFO',61,'id=6\nvmName=i-3-6-VM\nsoId=11\ndoId=-1\ntId=2\ndcId=1'),(70,'VM.START','Completed','successfully started VM: i-3-8-VM(Nimbus-VM-2)',3,3,'2011-04-23 02:40:25','INFO',68,'id=8\nvmName=i-3-8-VM\nsoId=11\ndoId=-1\ntId=2\ndcId=1'),(71,'TEMPLATE.CREATE','Completed','Created template TemplateFromSnapshot-1 from snapshot 1',2,2,'2011-04-23 02:40:33','INFO',0,'id=202\nname=TemplateFromSnapshot-1\ndcId=1\nsize=8589934592'),(72,'NET.RULEADD','Completed','created new ip forwarding rule [172.16.36.16:22]->[10.1.1.2:22] TCP',3,3,'2011-04-23 02:40:37','INFO',0,NULL),(73,'TEMPLATE.CREATE','Completed','Created template TemplateFromSnapshot-2 from snapshot 2',2,2,'2011-04-23 02:41:21','INFO',0,'id=203\nname=TemplateFromSnapshot-2\ndcId=1\nsize=8589934592'),(74,'LB.CREATE','Completed','Successfully created load balancer LBRule-2 on ip address 172.16.36.16[80->80]',3,3,'2011-04-23 02:41:33','INFO',0,'id=2\ndcId=1'),(75,'NET.RULEADD','Completed','created new load balancer rule [172.16.36.16:80]->[10.1.1.2:80] TCP',3,3,'2011-04-23 02:41:39','INFO',0,NULL),(76,'NET.RULEADD','Completed','created new load balancer rule [172.16.36.16:80]->[10.1.1.3:80] TCP',3,3,'2011-04-23 02:41:47','INFO',0,NULL),(77,'TEMPLATE.DOWNLOAD.SUCCESS','Completed','systemvm-xenserver-2.2.4 successfully downloaded to storage server nfs://192.168.110.232/export/home/chandan/eagle/secondary21x',1,2,'2011-04-23 02:47:55','INFO',0,NULL),(78,'TEMPLATE.CREATE','Completed','Successfully created template systemvm-xenserver-2.2.4',1,2,'2011-04-23 02:47:55','INFO',0,'id=201\ndcId=1\nsize=2097152000'),(79,'SNAPSHOT.CREATE','Completed','Backed up snapshot id: 6 to secondary for volume 9',3,3,'2011-04-23 02:49:26','INFO',0,'id=6\nssName=Nimbus-VM-1_i-3-6-VM-DATA_20110423024924\nsize=1073741824\ndcId=1'),(80,'SNAPSHOT.CREATE','Completed','Backed up snapshot id: 4 to secondary for volume 10',3,3,'2011-04-23 02:49:33','INFO',0,'id=4\nssName=Nimbus-VM-2_i-3-8-VM-ROOT_20110423024915\nsize=8589934592\ndcId=1'),(81,'SNAPSHOT.CREATE','Completed','Backed up snapshot id: 5 to secondary for volume 8',3,3,'2011-04-23 02:49:41','INFO',0,'id=5\nssName=Nimbus-VM-1_i-3-6-VM-ROOT_20110423024919\nsize=8589934592\ndcId=1'),(82,'SNAPSHOTPOLICY.CREATE','Completed','Successfully created snapshot policy with Id: 2',3,3,'2011-04-23 02:50:21','INFO',0,NULL),(83,'SNAPSHOTPOLICY.CREATE','Completed','Successfully created snapshot policy with Id: 3',3,3,'2011-04-23 02:50:53','INFO',0,NULL),(84,'SNAPSHOTPOLICY.CREATE','Completed','Successfully created snapshot policy with Id: 4',3,3,'2011-04-23 02:51:13','INFO',0,NULL),(85,'SNAPSHOTPOLICY.CREATE','Completed','Successfully created snapshot policy with Id: 5',3,3,'2011-04-23 02:51:27','INFO',0,NULL),(86,'SNAPSHOTPOLICY.UPDATE','Completed','Successfully updated snapshot policy with Id: 2',3,3,'2011-04-23 02:52:25','INFO',0,NULL),(87,'TEMPLATE.CREATE','Completed','Created template TemplateFromSnapshot-7-Data from snapshot 6',3,3,'2011-04-23 02:57:52','INFO',0,'id=207\nname=TemplateFromSnapshot-7-Data\ndcId=1\nsize=1073741824'),(88,'TEMPLATE.CREATE','Completed','Created template TemplateFromSnapshot-5 from snapshot 4',3,3,'2011-04-23 02:58:50','INFO',0,'id=205\nname=TemplateFromSnapshot-5\ndcId=1\nsize=8589934592'),(89,'VOLUME.CREATE','Scheduled','Scheduled async job for creating volume from snapshot with id: 4',3,3,'2011-04-23 02:59:06','INFO',0,NULL),(90,'VOLUME.CREATE','Started','Creating volume from snapshot with id: 4',3,3,'2011-04-23 02:59:06','INFO',89,NULL),(91,'VOLUME.CREATE','Scheduled','Scheduled async job for creating volume from snapshot with id: 5',3,3,'2011-04-23 02:59:11','INFO',0,NULL),(92,'TEMPLATE.CREATE','Completed','Created template TemplateFromSnapshot-6 from snapshot 5',3,3,'2011-04-23 02:59:20','INFO',0,'id=206\nname=TemplateFromSnapshot-6\ndcId=1\nsize=8589934592'),(93,'VOLUME.CREATE','Started','Creating volume from snapshot with id: 5',3,3,'2011-04-23 02:59:21','INFO',91,NULL),(94,'VOLUME.CREATE','Scheduled','Scheduled async job for creating volume from snapshot with id: 6',3,3,'2011-04-23 02:59:25','INFO',0,NULL),(95,'VOLUME.CREATE','Started','Creating volume from snapshot with id: 6',3,3,'2011-04-23 02:59:25','INFO',94,NULL),(96,'USER.LOGOUT','Completed','user has logged out',3,3,'2011-04-23 02:59:32','INFO',0,NULL),(97,'VOLUME.CREATE','Completed','Created volume: VlmfromSnapshot-3-Data with size: 1024 MB in pool: xenPrimary163 from snapshot id: 6',3,3,'2011-04-23 02:59:44','INFO',94,'id=13\ndoId=12\ntId=-1\ndcId=1\nsize=1024'),(98,'USER.LOGIN','Completed','user has logged in',2,2,'2011-04-23 02:59:46','INFO',0,NULL),(99,'VOLUME.CREATE','Scheduled','Scheduled async job for creating volume from snapshot with id: 1',2,2,'2011-04-23 03:00:47','INFO',0,NULL),(100,'VOLUME.CREATE','Started','Creating volume from snapshot with id: 1',2,2,'2011-04-23 03:00:47','INFO',99,NULL),(101,'VOLUME.CREATE','Scheduled','Scheduled async job for creating volume from snapshot with id: 2',2,2,'2011-04-23 03:00:55','INFO',0,NULL),(102,'VOLUME.CREATE','Started','Creating volume from snapshot with id: 2',2,2,'2011-04-23 03:00:55','INFO',101,NULL),(103,'CONFIGURATION.VALUE.EDIT','Completed','Successfully edited configuration value.',2,2,'2011-04-23 03:02:12','INFO',0,'name=secstorage.allowed.internal.sites\nvalue=192.168.110.231'),(104,'USER.LOGIN','Completed','user has logged in',2,2,'2011-04-23 03:03:31','INFO',0,NULL),(105,'TEMPLATE.DOWNLOAD.START','Completed','Storage server nfs://192.168.110.232/export/home/chandan/eagle/secondary21x started download of template CentOS-5-5-ISO-Download',1,2,'2011-04-23 03:04:34','INFO',0,NULL),(106,'SNAPSHOTPOLICY.CREATE','Completed','Successfully created snapshot policy with Id: 6',2,2,'2011-04-23 03:06:31','INFO',0,NULL),(107,'SNAPSHOTPOLICY.CREATE','Completed','Successfully created snapshot policy with Id: 7',2,2,'2011-04-23 03:06:38','INFO',0,NULL),(108,'SNAPSHOTPOLICY.CREATE','Completed','Successfully created snapshot policy with Id: 8',2,2,'2011-04-23 03:06:49','INFO',0,NULL),(109,'SNAPSHOTPOLICY.CREATE','Completed','Successfully created snapshot policy with Id: 9',2,2,'2011-04-23 03:06:56','INFO',0,NULL),(110,'TEMPLATE.DOWNLOAD.SUCCESS','Completed','CentOS-5-5-ISO-Download successfully downloaded to storage server nfs://192.168.110.232/export/home/chandan/eagle/secondary21x',1,2,'2011-04-23 03:09:16','INFO',0,NULL),(111,'ISO.CREATE','Completed','Successfully created ISO CentOS-5-5-ISO-Download',1,2,'2011-04-23 03:09:16','INFO',0,'id=208\ndcId=1\nsize=4393723904'),(112,'USER.LOGIN','Completed','user has logged in',2,2,'2011-04-23 03:37:26','INFO',0,NULL),(113,'SNAPSHOT.CREATE','Completed','Backed up snapshot id: 7 to secondary for volume 10',1,3,'2011-04-23 04:03:45','INFO',0,'id=7\nssName=Nimbus-VM-2_i-3-8-VM-ROOT_20110423040323\nsize=8589934592\ndcId=1'),(114,'ROUTER.STOP','Scheduled','Scheduled async job for stopping Router with Id: 7',1,1,'2011-04-23 04:04:52','INFO',0,NULL),(115,'ROUTER.STOP','Started','Stopping Router with Id: 7',1,3,'2011-04-23 04:04:53','INFO',114,NULL),(116,'ROUTER.STOP','Scheduled','Scheduled async job for stopping Router with Id: 4',1,1,'2011-04-23 04:04:58','INFO',0,NULL),(117,'ROUTER.STOP','Started','Stopping Router with Id: 4',1,2,'2011-04-23 04:04:58','INFO',116,NULL),(118,'SSVM.STOP','Scheduled','Scheduled async job for stopping secondary storage Vm Id: 1',1,1,'2011-04-23 04:05:03','INFO',0,NULL),(119,'SSVM.STOP','Started','Stopping secondary storage Vm Id: 1',1,1,'2011-04-23 04:05:03','INFO',118,NULL),(120,'ROUTER.STOP','Completed','successfully stopped Domain Router : r-7-VM',1,3,'2011-04-23 04:05:05','INFO',114,NULL),(121,'ROUTER.STOP','Completed','successfully stopped Domain Router : r-4-VM',1,2,'2011-04-23 04:05:18','INFO',116,NULL),(122,'SSVM.STOP','Completed','Secondary Storage Vm stopped - s-1-VM',1,1,'2011-04-23 04:05:33','INFO',118,NULL),(123,'PROXY.STOP','Scheduled','Scheduled async job for stopping console proxy with Id: 2',1,1,'2011-04-23 04:06:34','INFO',0,NULL),(124,'PROXY.STOP','Completed','Console proxy stopped - v-2-VM',1,1,'2011-04-23 04:06:47','INFO',123,NULL),(125,'USER.LOGIN','Completed','user has logged in',2,2,'2011-04-23 04:07:48','INFO',0,NULL),(126,'PROXY.STOP','Scheduled','Scheduled async job for stopping console proxy with Id: 2',1,1,'2011-04-23 04:08:04','INFO',0,NULL),(127,'PROXY.STOP','Completed','Console proxy stopped - v-2-VM',1,1,'2011-04-23 04:08:16','INFO',126,NULL);
/*!40000 ALTER TABLE `event` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ext_lun_alloc`
--

DROP TABLE IF EXISTS `ext_lun_alloc`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `ext_lun_alloc` (
  `id` bigint(20) unsigned NOT NULL auto_increment COMMENT 'id',
  `size` bigint(20) unsigned NOT NULL COMMENT 'virtual size',
  `portal` varchar(255) NOT NULL COMMENT 'ip or host name to the storage server',
  `target_iqn` varchar(255) NOT NULL COMMENT 'target iqn',
  `data_center_id` bigint(20) unsigned NOT NULL COMMENT 'data center id this belongs to',
  `lun` int(11) NOT NULL COMMENT 'lun',
  `taken` datetime default NULL COMMENT 'time occupied',
  `volume_id` bigint(20) unsigned default NULL COMMENT 'vm taking this lun',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`),
  UNIQUE KEY `i_ext_lun_alloc__target_iqn__lun` (`target_iqn`,`lun`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `ext_lun_alloc`
--

LOCK TABLES `ext_lun_alloc` WRITE;
/*!40000 ALTER TABLE `ext_lun_alloc` DISABLE KEYS */;
/*!40000 ALTER TABLE `ext_lun_alloc` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ext_lun_details`
--

DROP TABLE IF EXISTS `ext_lun_details`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `ext_lun_details` (
  `id` bigint(20) unsigned NOT NULL auto_increment COMMENT 'id',
  `ext_lun_id` bigint(20) unsigned NOT NULL COMMENT 'lun id',
  `tag` varchar(255) default NULL COMMENT 'tags associated with this vm',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`),
  KEY `fk_ext_lun_details__ext_lun_id` (`ext_lun_id`),
  CONSTRAINT `fk_ext_lun_details__ext_lun_id` FOREIGN KEY (`ext_lun_id`) REFERENCES `ext_lun_alloc` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `ext_lun_details`
--

LOCK TABLES `ext_lun_details` WRITE;
/*!40000 ALTER TABLE `ext_lun_details` DISABLE KEYS */;
/*!40000 ALTER TABLE `ext_lun_details` ENABLE KEYS */;
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
  `name` varchar(255) NOT NULL,
  `display_name` varchar(255) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `fk_guest_os__category_id` (`category_id`),
  CONSTRAINT `fk_guest_os__category_id` FOREIGN KEY (`category_id`) REFERENCES `guest_os_category` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=61 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `guest_os`
--

LOCK TABLES `guest_os` WRITE;
/*!40000 ALTER TABLE `guest_os` DISABLE KEYS */;
INSERT INTO `guest_os` VALUES (1,1,'CentOS 4.5 (32-bit)','CentOS 4.5 (32-bit)'),(2,1,'CentOS 4.6 (32-bit)','CentOS 4.6 (32-bit)'),(3,1,'CentOS 4.7 (32-bit)','CentOS 4.7 (32-bit)'),(4,1,'CentOS 4.8 (32-bit)','CentOS 4.8 (32-bit)'),(5,1,'CentOS 5.0 (32-bit)','CentOS 5.0 (32-bit)'),(6,1,'CentOS 5.0 (64-bit)','CentOS 5.0 (64-bit)'),(7,1,'CentOS 5.1 (32-bit)','CentOS 5.1 (32-bit)'),(8,1,'CentOS 5.1 (64-bit)','CentOS 5.1 (64-bit)'),(9,1,'CentOS 5.2 (32-bit)','CentOS 5.2 (32-bit)'),(10,1,'CentOS 5.2 (64-bit)','CentOS 5.2 (64-bit)'),(11,1,'CentOS 5.3 (32-bit)','CentOS 5.3 (32-bit)'),(12,1,'CentOS 5.3 (64-bit)','CentOS 5.3 (64-bit)'),(13,1,'CentOS 5.4 (32-bit)','CentOS 5.4 (32-bit)'),(14,1,'CentOS 5.4 (64-bit)','CentOS 5.4 (64-bit)'),(15,2,'Debian Lenny 5.0 (32-bit)','Debian Lenny 5.0 (32-bit)'),(16,3,'Oracle Enterprise Linux 5.0 (32-bit)','Oracle Enterprise Linux 5.0 (32-bit)'),(17,3,'Oracle Enterprise Linux 5.0 (64-bit)','Oracle Enterprise Linux 5.0 (64-bit)'),(18,3,'Oracle Enterprise Linux 5.1 (32-bit)','Oracle Enterprise Linux 5.1 (32-bit)'),(19,3,'Oracle Enterprise Linux 5.1 (64-bit)','Oracle Enterprise Linux 5.1 (64-bit)'),(20,3,'Oracle Enterprise Linux 5.2 (32-bit)','Oracle Enterprise Linux 5.2 (32-bit)'),(21,3,'Oracle Enterprise Linux 5.2 (64-bit)','Oracle Enterprise Linux 5.2 (64-bit)'),(22,3,'Oracle Enterprise Linux 5.3 (32-bit)','Oracle Enterprise Linux 5.3 (32-bit)'),(23,3,'Oracle Enterprise Linux 5.3 (64-bit)','Oracle Enterprise Linux 5.3 (64-bit)'),(24,3,'Oracle Enterprise Linux 5.4 (32-bit)','Oracle Enterprise Linux 5.4 (32-bit)'),(25,3,'Oracle Enterprise Linux 5.4 (64-bit)','Oracle Enterprise Linux 5.4 (64-bit)'),(26,4,'Red Hat Enterprise Linux 4.5 (32-bit)','Red Hat Enterprise Linux 4.5 (32-bit)'),(27,4,'Red Hat Enterprise Linux 4.6 (32-bit)','Red Hat Enterprise Linux 4.6 (32-bit)'),(28,4,'Red Hat Enterprise Linux 4.7 (32-bit)','Red Hat Enterprise Linux 4.7 (32-bit)'),(29,4,'Red Hat Enterprise Linux 4.8 (32-bit)','Red Hat Enterprise Linux 4.8 (32-bit)'),(30,4,'Red Hat Enterprise Linux 5.0 (32-bit)','Red Hat Enterprise Linux 5.0 (32-bit)'),(31,4,'Red Hat Enterprise Linux 5.0 (64-bit)','Red Hat Enterprise Linux 5.0 (64-bit)'),(32,4,'Red Hat Enterprise Linux 5.1 (32-bit)','Red Hat Enterprise Linux 5.1 (32-bit)'),(33,4,'Red Hat Enterprise Linux 5.1 (64-bit)','Red Hat Enterprise Linux 5.1 (64-bit)'),(34,4,'Red Hat Enterprise Linux 5.2 (32-bit)','Red Hat Enterprise Linux 5.2 (32-bit)'),(35,4,'Red Hat Enterprise Linux 5.2 (64-bit)','Red Hat Enterprise Linux 5.2 (64-bit)'),(36,4,'Red Hat Enterprise Linux 5.3 (32-bit)','Red Hat Enterprise Linux 5.3 (32-bit)'),(37,4,'Red Hat Enterprise Linux 5.3 (64-bit)','Red Hat Enterprise Linux 5.3 (64-bit)'),(38,4,'Red Hat Enterprise Linux 5.4 (32-bit)','Red Hat Enterprise Linux 5.4 (32-bit)'),(39,4,'Red Hat Enterprise Linux 5.4 (64-bit)','Red Hat Enterprise Linux 5.4 (64-bit)'),(40,5,'SUSE Linux Enterprise Server 9 SP4 (32-bit)','SUSE Linux Enterprise Server 9 SP4 (32-bit)'),(41,5,'SUSE Linux Enterprise Server 10 SP1 (32-bit)','SUSE Linux Enterprise Server 10 SP1 (32-bit)'),(42,5,'SUSE Linux Enterprise Server 10 SP1 (64-bit)','SUSE Linux Enterprise Server 10 SP1 (64-bit)'),(43,5,'SUSE Linux Enterprise Server 10 SP2 (32-bit)','SUSE Linux Enterprise Server 10 SP2 (32-bit)'),(44,5,'SUSE Linux Enterprise Server 10 SP2 (64-bit)','SUSE Linux Enterprise Server 10 SP2 (64-bit)'),(45,5,'SUSE Linux Enterprise Server 10 SP3 (64-bit)','SUSE Linux Enterprise Server 10 SP3 (64-bit)'),(46,5,'SUSE Linux Enterprise Server 11 (32-bit)','SUSE Linux Enterprise Server 11 (32-bit)'),(47,5,'SUSE Linux Enterprise Server 11 (64-bit)','SUSE Linux Enterprise Server 11 (64-bit)'),(48,6,'Windows 7 (32-bit)','Windows 7 (32-bit)'),(49,6,'Windows 7 (64-bit)','Windows 7 (64-bit)'),(50,6,'Windows Server 2003 (32-bit)','Windows Server 2003 (32-bit)'),(51,6,'Windows Server 2003 (64-bit)','Windows Server 2003 (64-bit)'),(52,6,'Windows Server 2008 (32-bit)','Windows Server 2008 (32-bit)'),(53,6,'Windows Server 2008 (64-bit)','Windows Server 2008 (64-bit)'),(54,6,'Windows Server 2008 R2 (64-bit)','Windows Server 2008 R2 (64-bit)'),(55,6,'Windows 2000 SP4 (32-bit)','Windows 2000 SP4 (32-bit)'),(56,6,'Windows Vista (32-bit)','Windows Vista (32-bit)'),(57,6,'Windows XP SP2 (32-bit)','Windows XP SP2 (32-bit)'),(58,6,'Windows XP SP3 (32-bit)','Windows XP SP3 (32-bit)'),(59,7,'Other install media','Ubuntu'),(60,7,'Other install media','Other');
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
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `guest_os_category`
--

LOCK TABLES `guest_os_category` WRITE;
/*!40000 ALTER TABLE `guest_os_category` DISABLE KEYS */;
INSERT INTO `guest_os_category` VALUES (1,'CentOS'),(2,'Debian'),(3,'Oracle'),(4,'RedHat'),(5,'SUSE'),(6,'Windows'),(7,'Other');
/*!40000 ALTER TABLE `guest_os_category` ENABLE KEYS */;
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
  `sequence` bigint(20) unsigned NOT NULL default '1',
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
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `host`
--

LOCK TABLES `host` WRITE;
/*!40000 ALTER TABLE `host` DISABLE KEYS */;
INSERT INTO `host` VALUES (1,'nfs://192.168.110.232/export/home/chandan/eagle/secondary21x','Disconnected','SecondaryStorage','192.168.110.232','255.255.255.0','06:01:de:49:00:01','192.168.163.103','255.255.255.0','06:01:de:49:00:01',NULL,NULL,NULL,NULL,'172.16.36.42','255.255.252.0','06:81:de:49:00:01',NULL,1,1,NULL,NULL,'nfs://192.168.110.232/export/home/chandan/eagle/secondary21x',NULL,'None',0,NULL,'2.1.7',217,'/mnt/SecStorage/3c02a379',1930519904256,NULL,'nfs://192.168.110.232/export/home/chandan/eagle/secondary21x',1,0,0,1272979538,NULL,'2011-04-23 04:07:41','2011-04-23 02:18:41',NULL),(2,'xenserver-chandan2','Up','Routing','192.168.163.2','255.255.255.0','bc:30:5b:d4:1e:b8','192.168.163.2','255.255.255.0','bc:30:5b:d4:1e:b8','192.168.163.2','bc:30:5b:d4:1e:b8','255.255.255.0',1,NULL,NULL,NULL,NULL,1,1,4,2260,'iqn.2005-03.org.open-iscsi:46f2a43e8ac',NULL,'XenServer',3244655232,'com.cloud.hypervisor.xen.resource.XenServer56Resource','2.1.7',411,NULL,NULL,'xen-3.0-x86_64 , xen-3.0-x86_32p , hvm-3.0-x86_32 , hvm-3.0-x86_32p , hvm-3.0-x86_64','f9c3406e-3122-4dff-a39c-458820187328',1,1,0,1272980147,6603285398259,'2011-04-23 04:07:41','2011-04-23 02:18:50',NULL),(3,'v-2-VM','Up','ConsoleProxy','192.168.163.102','255.255.255.0','06:01:ea:ec:00:03','192.168.163.102','255.255.255.0','06:01:ea:ec:00:03',NULL,NULL,NULL,NULL,'172.16.36.27','255.255.252.0','06:81:ea:ec:00:03',80,1,1,NULL,NULL,'NoIqn',NULL,NULL,0,NULL,'2.1.7',4,NULL,NULL,NULL,'Proxy.2-ConsoleProxyResource',1,0,0,1272980144,6603285398259,'2011-04-23 04:07:41','2011-04-23 02:21:48',NULL);
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
) ENGINE=InnoDB AUTO_INCREMENT=55 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `host_details`
--

LOCK TABLES `host_details` WRITE;
/*!40000 ALTER TABLE `host_details` DISABLE KEYS */;
INSERT INTO `host_details` VALUES (1,1,'mount.parent','dummy'),(2,1,'mount.path','dummy'),(3,1,'orig.url','nfs://192.168.110.232/export/home/chandan/eagle/secondary21x'),(38,2,'com.cloud.network.NetworkEnums.RouterPrivateIpStrategy','DcGlobal'),(39,2,'public.network.device','Pool-wide network associated with eth0'),(40,2,'private.network.device','Pool-wide network associated with eth0'),(41,2,'Hypervisor.Version','3.4.2'),(42,2,'Host.OS','XenServer'),(43,2,'Host.OS.Kernel.Version','2.6.27.42-0.1.1.xs5.6.0.44.111158xen'),(44,2,'wait','1800'),(45,2,'storage.network.device2','cloud-stor2'),(46,2,'password','password'),(47,2,'storage.network.device1','cloud-stor1'),(48,2,'url','192.168.163.2'),(49,2,'username','root'),(50,2,'pool','b60b68fe-0165-308b-a4cb-dabd4c7060e9'),(51,2,'guest.network.device','Pool-wide network associated with eth0'),(52,2,'can_bridge_firewall','false'),(53,2,'Host.OS.Version','5.6.0'),(54,2,'instance.name','VM');
/*!40000 ALTER TABLE `host_details` ENABLE KEYS */;
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
INSERT INTO `host_pod_ref` VALUES (1,'POD1',1,'192.168.163.1','192.168.163.0',24,'192.168.163.100-192.168.163.110');
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
-- Table structure for table `ip_forwarding`
--

DROP TABLE IF EXISTS `ip_forwarding`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `ip_forwarding` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `group_id` bigint(20) unsigned default NULL,
  `public_ip_address` varchar(15) NOT NULL,
  `public_port` varchar(10) default NULL,
  `private_ip_address` varchar(15) NOT NULL,
  `private_port` varchar(10) default NULL,
  `enabled` tinyint(1) NOT NULL default '1',
  `protocol` varchar(16) NOT NULL default 'TCP',
  `forwarding` tinyint(1) NOT NULL default '1',
  `algorithm` varchar(255) default NULL,
  PRIMARY KEY  (`id`),
  KEY `i_ip_forwarding__forwarding` (`forwarding`),
  KEY `i_ip_forwarding__public_ip_address__public_port` (`public_ip_address`,`public_port`),
  KEY `i_ip_forwarding__public_ip_address` (`public_ip_address`),
  CONSTRAINT `fk_ip_forwarding__public_ip_address` FOREIGN KEY (`public_ip_address`) REFERENCES `user_ip_address` (`public_ip_address`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `ip_forwarding`
--

LOCK TABLES `ip_forwarding` WRITE;
/*!40000 ALTER TABLE `ip_forwarding` DISABLE KEYS */;
INSERT INTO `ip_forwarding` VALUES (1,NULL,'172.16.36.53','22','10.1.1.2','22',1,'TCP',1,NULL),(2,1,'172.16.36.53','80','10.1.1.2','80',1,'TCP',0,'roundrobin'),(3,1,'172.16.36.53','80','10.1.1.3','80',1,'TCP',0,'roundrobin'),(4,NULL,'172.16.36.16','22','10.1.1.2','22',1,'TCP',1,NULL),(5,2,'172.16.36.16','80','10.1.1.2','80',1,'TCP',0,'roundrobin'),(6,2,'172.16.36.16','80','10.1.1.3','80',1,'TCP',0,'roundrobin');
/*!40000 ALTER TABLE `ip_forwarding` ENABLE KEYS */;
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
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `load_balancer`
--

LOCK TABLES `load_balancer` WRITE;
/*!40000 ALTER TABLE `load_balancer` DISABLE KEYS */;
INSERT INTO `load_balancer` VALUES (1,'LBRule-1',NULL,2,'172.16.36.53','80','80','roundrobin'),(2,'LBRule-2',NULL,3,'172.16.36.16','80','80','roundrobin');
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
  `pending` tinyint(1) unsigned NOT NULL default '0' COMMENT 'whether the vm is being applied to the load balancer (pending=1) or has already been applied (pending=0)',
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `load_balancer_vm_map`
--

LOCK TABLES `load_balancer_vm_map` WRITE;
/*!40000 ALTER TABLE `load_balancer_vm_map` DISABLE KEYS */;
INSERT INTO `load_balancer_vm_map` VALUES (1,1,3,0),(2,1,5,0),(3,2,6,0),(4,2,8,0);
/*!40000 ALTER TABLE `load_balancer_vm_map` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mshost`
--

DROP TABLE IF EXISTS `mshost`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `mshost` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `msid` bigint(20) NOT NULL COMMENT 'management server id derived from MAC address',
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
INSERT INTO `mshost` VALUES (1,6603285398259,'i-11-246293-VM','2.1.7','192.168.130.201',9090,'2011-04-23 04:08:39',NULL,0);
/*!40000 ALTER TABLE `mshost` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `netapp_lun`
--

DROP TABLE IF EXISTS `netapp_lun`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `netapp_lun` (
  `id` bigint(20) unsigned NOT NULL auto_increment COMMENT 'id',
  `lun_name` varchar(255) NOT NULL COMMENT 'lun name',
  `target_iqn` varchar(255) NOT NULL COMMENT 'target iqn',
  `path` varchar(255) NOT NULL COMMENT 'lun path',
  `size` bigint(20) NOT NULL COMMENT 'lun size',
  `volume_id` bigint(20) unsigned NOT NULL COMMENT 'parent volume id',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`),
  KEY `i_netapp_lun__volume_id` (`volume_id`),
  KEY `i_netapp_lun__lun_name` (`lun_name`),
  CONSTRAINT `fk_netapp_lun__volume_id` FOREIGN KEY (`volume_id`) REFERENCES `netapp_volume` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

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
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `netapp_pool` (
  `id` bigint(20) unsigned NOT NULL auto_increment COMMENT 'id',
  `name` varchar(255) NOT NULL COMMENT 'name for the pool',
  `algorithm` varchar(255) NOT NULL COMMENT 'algorithm',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

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
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `netapp_volume` (
  `id` bigint(20) unsigned NOT NULL auto_increment COMMENT 'id',
  `ip_address` varchar(255) NOT NULL COMMENT 'ip address/fqdn of the volume',
  `pool_id` bigint(20) unsigned NOT NULL COMMENT 'id for the pool',
  `pool_name` varchar(255) NOT NULL COMMENT 'name for the pool',
  `aggregate_name` varchar(255) NOT NULL COMMENT 'name for the aggregate',
  `volume_name` varchar(255) NOT NULL COMMENT 'name for the volume',
  `volume_size` varchar(255) NOT NULL COMMENT 'volume size',
  `snapshot_policy` varchar(255) NOT NULL COMMENT 'snapshot policy',
  `snapshot_reservation` int(11) NOT NULL COMMENT 'snapshot reservation',
  `username` varchar(255) NOT NULL COMMENT 'username',
  `password` varchar(200) default NULL COMMENT 'password',
  `round_robin_marker` int(11) default NULL COMMENT 'This marks the volume to be picked up for lun creation, RR fashion',
  PRIMARY KEY  (`ip_address`,`aggregate_name`,`volume_name`),
  UNIQUE KEY `id` (`id`),
  KEY `i_netapp_volume__pool_id` (`pool_id`),
  CONSTRAINT `fk_netapp_volume__pool_id` FOREIGN KEY (`pool_id`) REFERENCES `netapp_pool` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `netapp_volume`
--

LOCK TABLES `netapp_volume` WRITE;
/*!40000 ALTER TABLE `netapp_volume` DISABLE KEYS */;
/*!40000 ALTER TABLE `netapp_volume` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `network_group`
--

DROP TABLE IF EXISTS `network_group`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `network_group` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `name` varchar(255) NOT NULL,
  `description` varchar(4096) default NULL,
  `domain_id` bigint(20) unsigned NOT NULL,
  `account_id` bigint(20) unsigned NOT NULL,
  `account_name` varchar(100) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `fk_network_group__account_id` (`account_id`),
  KEY `fk_network_group__domain_id` (`domain_id`),
  KEY `i_network_group_name` (`name`),
  CONSTRAINT `fk_network_group__domain_id` FOREIGN KEY (`domain_id`) REFERENCES `domain` (`id`),
  CONSTRAINT `fk_network_group___account_id` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `network_group`
--

LOCK TABLES `network_group` WRITE;
/*!40000 ALTER TABLE `network_group` DISABLE KEYS */;
/*!40000 ALTER TABLE `network_group` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `network_group_vm_map`
--

DROP TABLE IF EXISTS `network_group_vm_map`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `network_group_vm_map` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `network_group_id` bigint(20) unsigned NOT NULL,
  `instance_id` bigint(20) unsigned NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `fk_network_group_vm_map___network_group_id` (`network_group_id`),
  KEY `fk_network_group_vm_map___instance_id` (`instance_id`),
  CONSTRAINT `fk_network_group_vm_map___instance_id` FOREIGN KEY (`instance_id`) REFERENCES `user_vm` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_network_group_vm_map___network_group_id` FOREIGN KEY (`network_group_id`) REFERENCES `network_group` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `network_group_vm_map`
--

LOCK TABLES `network_group_vm_map` WRITE;
/*!40000 ALTER TABLE `network_group_vm_map` DISABLE KEYS */;
/*!40000 ALTER TABLE `network_group_vm_map` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `network_ingress_rule`
--

DROP TABLE IF EXISTS `network_ingress_rule`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `network_ingress_rule` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `network_group_id` bigint(20) unsigned NOT NULL,
  `start_port` varchar(10) default NULL,
  `end_port` varchar(10) default NULL,
  `protocol` varchar(16) NOT NULL default 'TCP',
  `allowed_network_id` bigint(20) unsigned default NULL,
  `allowed_network_group` varchar(255) default NULL COMMENT 'data duplicated from network_group table to avoid lots of joins when listing rules (the name of the group should be displayed rather than just id)',
  `allowed_net_grp_acct` varchar(100) default NULL COMMENT 'data duplicated from network_group table to avoid lots of joins when listing rules (the name of the group owner should be displayed)',
  `allowed_ip_cidr` varchar(44) default NULL,
  `create_status` varchar(32) default NULL COMMENT 'rule creation status',
  PRIMARY KEY  (`id`),
  KEY `i_network_ingress_rule_network_id` (`network_group_id`),
  KEY `i_network_ingress_rule_allowed_network` (`allowed_network_id`),
  CONSTRAINT `fk_network_ingress_rule___allowed_network_id` FOREIGN KEY (`allowed_network_id`) REFERENCES `network_group` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_network_ingress_rule___network_group_id` FOREIGN KEY (`network_group_id`) REFERENCES `network_group` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `network_ingress_rule`
--

LOCK TABLES `network_ingress_rule` WRITE;
/*!40000 ALTER TABLE `network_ingress_rule` DISABLE KEYS */;
/*!40000 ALTER TABLE `network_ingress_rule` ENABLE KEYS */;
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
  `taken` datetime default NULL COMMENT 'Date taken',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `i_op_dc_ip_address_alloc__ip_address__data_center_id` (`ip_address`,`data_center_id`),
  KEY `i_op_dc_ip_address_alloc__pod_id__data_center_id__taken` (`pod_id`,`data_center_id`,`taken`,`instance_id`),
  KEY `i_op_dc_ip_address_alloc__pod_id` (`pod_id`),
  CONSTRAINT `fk_op_dc_ip_address_alloc__pod_id` FOREIGN KEY (`pod_id`) REFERENCES `host_pod_ref` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `op_dc_ip_address_alloc`
--

LOCK TABLES `op_dc_ip_address_alloc` WRITE;
/*!40000 ALTER TABLE `op_dc_ip_address_alloc` DISABLE KEYS */;
INSERT INTO `op_dc_ip_address_alloc` VALUES (1,'192.168.163.100',1,1,NULL,NULL),(2,'192.168.163.101',1,1,NULL,NULL),(3,'192.168.163.102',1,1,NULL,NULL),(4,'192.168.163.103',1,1,NULL,NULL),(5,'192.168.163.104',1,1,NULL,NULL),(6,'192.168.163.105',1,1,NULL,NULL),(7,'192.168.163.106',1,1,NULL,NULL),(8,'192.168.163.107',1,1,NULL,NULL),(9,'192.168.163.108',1,1,NULL,NULL),(10,'192.168.163.109',1,1,NULL,NULL),(11,'192.168.163.110',1,1,NULL,NULL);
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
  `taken` datetime default NULL COMMENT 'Date taken',
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1022 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `op_dc_link_local_ip_address_alloc`
--

LOCK TABLES `op_dc_link_local_ip_address_alloc` WRITE;
/*!40000 ALTER TABLE `op_dc_link_local_ip_address_alloc` DISABLE KEYS */;
INSERT INTO `op_dc_link_local_ip_address_alloc` VALUES (1,'169.254.0.2',1,1,NULL,NULL),(2,'169.254.0.3',1,1,NULL,NULL),(3,'169.254.0.4',1,1,NULL,NULL),(4,'169.254.0.5',1,1,NULL,NULL),(5,'169.254.0.6',1,1,NULL,NULL),(6,'169.254.0.7',1,1,NULL,NULL),(7,'169.254.0.8',1,1,NULL,NULL),(8,'169.254.0.9',1,1,NULL,NULL),(9,'169.254.0.10',1,1,NULL,NULL),(10,'169.254.0.11',1,1,NULL,NULL),(11,'169.254.0.12',1,1,NULL,NULL),(12,'169.254.0.13',1,1,NULL,NULL),(13,'169.254.0.14',1,1,NULL,NULL),(14,'169.254.0.15',1,1,NULL,NULL),(15,'169.254.0.16',1,1,NULL,NULL),(16,'169.254.0.17',1,1,NULL,NULL),(17,'169.254.0.18',1,1,NULL,NULL),(18,'169.254.0.19',1,1,NULL,NULL),(19,'169.254.0.20',1,1,NULL,NULL),(20,'169.254.0.21',1,1,NULL,NULL),(21,'169.254.0.22',1,1,NULL,NULL),(22,'169.254.0.23',1,1,NULL,NULL),(23,'169.254.0.24',1,1,NULL,NULL),(24,'169.254.0.25',1,1,NULL,NULL),(25,'169.254.0.26',1,1,NULL,NULL),(26,'169.254.0.27',1,1,NULL,NULL),(27,'169.254.0.28',1,1,NULL,NULL),(28,'169.254.0.29',1,1,NULL,NULL),(29,'169.254.0.30',1,1,NULL,NULL),(30,'169.254.0.31',1,1,NULL,NULL),(31,'169.254.0.32',1,1,NULL,NULL),(32,'169.254.0.33',1,1,NULL,NULL),(33,'169.254.0.34',1,1,NULL,NULL),(34,'169.254.0.35',1,1,NULL,NULL),(35,'169.254.0.36',1,1,NULL,NULL),(36,'169.254.0.37',1,1,NULL,NULL),(37,'169.254.0.38',1,1,NULL,NULL),(38,'169.254.0.39',1,1,NULL,NULL),(39,'169.254.0.40',1,1,NULL,NULL),(40,'169.254.0.41',1,1,NULL,NULL),(41,'169.254.0.42',1,1,NULL,NULL),(42,'169.254.0.43',1,1,NULL,NULL),(43,'169.254.0.44',1,1,NULL,NULL),(44,'169.254.0.45',1,1,NULL,NULL),(45,'169.254.0.46',1,1,NULL,NULL),(46,'169.254.0.47',1,1,NULL,NULL),(47,'169.254.0.48',1,1,NULL,NULL),(48,'169.254.0.49',1,1,NULL,NULL),(49,'169.254.0.50',1,1,NULL,NULL),(50,'169.254.0.51',1,1,NULL,NULL),(51,'169.254.0.52',1,1,NULL,NULL),(52,'169.254.0.53',1,1,NULL,NULL),(53,'169.254.0.54',1,1,NULL,NULL),(54,'169.254.0.55',1,1,NULL,NULL),(55,'169.254.0.56',1,1,NULL,NULL),(56,'169.254.0.57',1,1,NULL,NULL),(57,'169.254.0.58',1,1,NULL,NULL),(58,'169.254.0.59',1,1,NULL,NULL),(59,'169.254.0.60',1,1,NULL,NULL),(60,'169.254.0.61',1,1,NULL,NULL),(61,'169.254.0.62',1,1,NULL,NULL),(62,'169.254.0.63',1,1,NULL,NULL),(63,'169.254.0.64',1,1,NULL,NULL),(64,'169.254.0.65',1,1,NULL,NULL),(65,'169.254.0.66',1,1,NULL,NULL),(66,'169.254.0.67',1,1,NULL,NULL),(67,'169.254.0.68',1,1,NULL,NULL),(68,'169.254.0.69',1,1,NULL,NULL),(69,'169.254.0.70',1,1,NULL,NULL),(70,'169.254.0.71',1,1,NULL,NULL),(71,'169.254.0.72',1,1,NULL,NULL),(72,'169.254.0.73',1,1,NULL,NULL),(73,'169.254.0.74',1,1,NULL,NULL),(74,'169.254.0.75',1,1,NULL,NULL),(75,'169.254.0.76',1,1,NULL,NULL),(76,'169.254.0.77',1,1,NULL,NULL),(77,'169.254.0.78',1,1,NULL,NULL),(78,'169.254.0.79',1,1,NULL,NULL),(79,'169.254.0.80',1,1,NULL,NULL),(80,'169.254.0.81',1,1,NULL,NULL),(81,'169.254.0.82',1,1,NULL,NULL),(82,'169.254.0.83',1,1,NULL,NULL),(83,'169.254.0.84',1,1,NULL,NULL),(84,'169.254.0.85',1,1,NULL,NULL),(85,'169.254.0.86',1,1,NULL,NULL),(86,'169.254.0.87',1,1,NULL,NULL),(87,'169.254.0.88',1,1,NULL,NULL),(88,'169.254.0.89',1,1,NULL,NULL),(89,'169.254.0.90',1,1,NULL,NULL),(90,'169.254.0.91',1,1,NULL,NULL),(91,'169.254.0.92',1,1,NULL,NULL),(92,'169.254.0.93',1,1,NULL,NULL),(93,'169.254.0.94',1,1,NULL,NULL),(94,'169.254.0.95',1,1,NULL,NULL),(95,'169.254.0.96',1,1,NULL,NULL),(96,'169.254.0.97',1,1,NULL,NULL),(97,'169.254.0.98',1,1,NULL,NULL),(98,'169.254.0.99',1,1,NULL,NULL),(99,'169.254.0.100',1,1,NULL,NULL),(100,'169.254.0.101',1,1,NULL,NULL),(101,'169.254.0.102',1,1,NULL,NULL),(102,'169.254.0.103',1,1,NULL,NULL),(103,'169.254.0.104',1,1,NULL,NULL),(104,'169.254.0.105',1,1,NULL,NULL),(105,'169.254.0.106',1,1,NULL,NULL),(106,'169.254.0.107',1,1,NULL,NULL),(107,'169.254.0.108',1,1,NULL,NULL),(108,'169.254.0.109',1,1,NULL,NULL),(109,'169.254.0.110',1,1,NULL,NULL),(110,'169.254.0.111',1,1,NULL,NULL),(111,'169.254.0.112',1,1,NULL,NULL),(112,'169.254.0.113',1,1,NULL,NULL),(113,'169.254.0.114',1,1,NULL,NULL),(114,'169.254.0.115',1,1,NULL,NULL),(115,'169.254.0.116',1,1,NULL,NULL),(116,'169.254.0.117',1,1,NULL,NULL),(117,'169.254.0.118',1,1,NULL,NULL),(118,'169.254.0.119',1,1,NULL,NULL),(119,'169.254.0.120',1,1,NULL,NULL),(120,'169.254.0.121',1,1,NULL,NULL),(121,'169.254.0.122',1,1,NULL,NULL),(122,'169.254.0.123',1,1,NULL,NULL),(123,'169.254.0.124',1,1,NULL,NULL),(124,'169.254.0.125',1,1,NULL,NULL),(125,'169.254.0.126',1,1,NULL,NULL),(126,'169.254.0.127',1,1,NULL,NULL),(127,'169.254.0.128',1,1,NULL,NULL),(128,'169.254.0.129',1,1,NULL,NULL),(129,'169.254.0.130',1,1,NULL,NULL),(130,'169.254.0.131',1,1,NULL,NULL),(131,'169.254.0.132',1,1,NULL,NULL),(132,'169.254.0.133',1,1,NULL,NULL),(133,'169.254.0.134',1,1,NULL,NULL),(134,'169.254.0.135',1,1,NULL,NULL),(135,'169.254.0.136',1,1,NULL,NULL),(136,'169.254.0.137',1,1,NULL,NULL),(137,'169.254.0.138',1,1,NULL,NULL),(138,'169.254.0.139',1,1,NULL,NULL),(139,'169.254.0.140',1,1,NULL,NULL),(140,'169.254.0.141',1,1,NULL,NULL),(141,'169.254.0.142',1,1,NULL,NULL),(142,'169.254.0.143',1,1,NULL,NULL),(143,'169.254.0.144',1,1,NULL,NULL),(144,'169.254.0.145',1,1,NULL,NULL),(145,'169.254.0.146',1,1,NULL,NULL),(146,'169.254.0.147',1,1,NULL,NULL),(147,'169.254.0.148',1,1,NULL,NULL),(148,'169.254.0.149',1,1,NULL,NULL),(149,'169.254.0.150',1,1,NULL,NULL),(150,'169.254.0.151',1,1,NULL,NULL),(151,'169.254.0.152',1,1,NULL,NULL),(152,'169.254.0.153',1,1,NULL,NULL),(153,'169.254.0.154',1,1,NULL,NULL),(154,'169.254.0.155',1,1,NULL,NULL),(155,'169.254.0.156',1,1,NULL,NULL),(156,'169.254.0.157',1,1,NULL,NULL),(157,'169.254.0.158',1,1,NULL,NULL),(158,'169.254.0.159',1,1,NULL,NULL),(159,'169.254.0.160',1,1,NULL,NULL),(160,'169.254.0.161',1,1,NULL,NULL),(161,'169.254.0.162',1,1,NULL,NULL),(162,'169.254.0.163',1,1,NULL,NULL),(163,'169.254.0.164',1,1,NULL,NULL),(164,'169.254.0.165',1,1,NULL,NULL),(165,'169.254.0.166',1,1,NULL,NULL),(166,'169.254.0.167',1,1,NULL,NULL),(167,'169.254.0.168',1,1,NULL,NULL),(168,'169.254.0.169',1,1,NULL,NULL),(169,'169.254.0.170',1,1,NULL,NULL),(170,'169.254.0.171',1,1,NULL,NULL),(171,'169.254.0.172',1,1,NULL,NULL),(172,'169.254.0.173',1,1,NULL,NULL),(173,'169.254.0.174',1,1,NULL,NULL),(174,'169.254.0.175',1,1,NULL,NULL),(175,'169.254.0.176',1,1,NULL,NULL),(176,'169.254.0.177',1,1,NULL,NULL),(177,'169.254.0.178',1,1,NULL,NULL),(178,'169.254.0.179',1,1,NULL,NULL),(179,'169.254.0.180',1,1,NULL,NULL),(180,'169.254.0.181',1,1,NULL,NULL),(181,'169.254.0.182',1,1,NULL,NULL),(182,'169.254.0.183',1,1,NULL,NULL),(183,'169.254.0.184',1,1,NULL,NULL),(184,'169.254.0.185',1,1,NULL,NULL),(185,'169.254.0.186',1,1,NULL,NULL),(186,'169.254.0.187',1,1,NULL,NULL),(187,'169.254.0.188',1,1,NULL,NULL),(188,'169.254.0.189',1,1,NULL,NULL),(189,'169.254.0.190',1,1,NULL,NULL),(190,'169.254.0.191',1,1,NULL,NULL),(191,'169.254.0.192',1,1,NULL,NULL),(192,'169.254.0.193',1,1,NULL,NULL),(193,'169.254.0.194',1,1,NULL,NULL),(194,'169.254.0.195',1,1,NULL,NULL),(195,'169.254.0.196',1,1,NULL,NULL),(196,'169.254.0.197',1,1,NULL,NULL),(197,'169.254.0.198',1,1,NULL,NULL),(198,'169.254.0.199',1,1,NULL,NULL),(199,'169.254.0.200',1,1,NULL,NULL),(200,'169.254.0.201',1,1,NULL,NULL),(201,'169.254.0.202',1,1,NULL,NULL),(202,'169.254.0.203',1,1,NULL,NULL),(203,'169.254.0.204',1,1,NULL,NULL),(204,'169.254.0.205',1,1,NULL,NULL),(205,'169.254.0.206',1,1,NULL,NULL),(206,'169.254.0.207',1,1,NULL,NULL),(207,'169.254.0.208',1,1,NULL,NULL),(208,'169.254.0.209',1,1,NULL,NULL),(209,'169.254.0.210',1,1,NULL,NULL),(210,'169.254.0.211',1,1,NULL,NULL),(211,'169.254.0.212',1,1,NULL,NULL),(212,'169.254.0.213',1,1,NULL,NULL),(213,'169.254.0.214',1,1,NULL,NULL),(214,'169.254.0.215',1,1,NULL,NULL),(215,'169.254.0.216',1,1,NULL,NULL),(216,'169.254.0.217',1,1,NULL,NULL),(217,'169.254.0.218',1,1,NULL,NULL),(218,'169.254.0.219',1,1,NULL,NULL),(219,'169.254.0.220',1,1,NULL,NULL),(220,'169.254.0.221',1,1,NULL,NULL),(221,'169.254.0.222',1,1,NULL,NULL),(222,'169.254.0.223',1,1,NULL,NULL),(223,'169.254.0.224',1,1,NULL,NULL),(224,'169.254.0.225',1,1,NULL,NULL),(225,'169.254.0.226',1,1,NULL,NULL),(226,'169.254.0.227',1,1,NULL,NULL),(227,'169.254.0.228',1,1,NULL,NULL),(228,'169.254.0.229',1,1,NULL,NULL),(229,'169.254.0.230',1,1,NULL,NULL),(230,'169.254.0.231',1,1,NULL,NULL),(231,'169.254.0.232',1,1,NULL,NULL),(232,'169.254.0.233',1,1,NULL,NULL),(233,'169.254.0.234',1,1,NULL,NULL),(234,'169.254.0.235',1,1,NULL,NULL),(235,'169.254.0.236',1,1,NULL,NULL),(236,'169.254.0.237',1,1,NULL,NULL),(237,'169.254.0.238',1,1,NULL,NULL),(238,'169.254.0.239',1,1,NULL,NULL),(239,'169.254.0.240',1,1,NULL,NULL),(240,'169.254.0.241',1,1,NULL,NULL),(241,'169.254.0.242',1,1,NULL,NULL),(242,'169.254.0.243',1,1,NULL,NULL),(243,'169.254.0.244',1,1,NULL,NULL),(244,'169.254.0.245',1,1,NULL,NULL),(245,'169.254.0.246',1,1,NULL,NULL),(246,'169.254.0.247',1,1,NULL,NULL),(247,'169.254.0.248',1,1,NULL,NULL),(248,'169.254.0.249',1,1,NULL,NULL),(249,'169.254.0.250',1,1,NULL,NULL),(250,'169.254.0.251',1,1,NULL,NULL),(251,'169.254.0.252',1,1,NULL,NULL),(252,'169.254.0.253',1,1,NULL,NULL),(253,'169.254.0.254',1,1,NULL,NULL),(254,'169.254.0.255',1,1,NULL,NULL),(255,'169.254.1.0',1,1,NULL,NULL),(256,'169.254.1.1',1,1,NULL,NULL),(257,'169.254.1.2',1,1,NULL,NULL),(258,'169.254.1.3',1,1,NULL,NULL),(259,'169.254.1.4',1,1,NULL,NULL),(260,'169.254.1.5',1,1,NULL,NULL),(261,'169.254.1.6',1,1,NULL,NULL),(262,'169.254.1.7',1,1,NULL,NULL),(263,'169.254.1.8',1,1,NULL,NULL),(264,'169.254.1.9',1,1,NULL,NULL),(265,'169.254.1.10',1,1,NULL,NULL),(266,'169.254.1.11',1,1,NULL,NULL),(267,'169.254.1.12',1,1,NULL,NULL),(268,'169.254.1.13',1,1,NULL,NULL),(269,'169.254.1.14',1,1,NULL,NULL),(270,'169.254.1.15',1,1,NULL,NULL),(271,'169.254.1.16',1,1,NULL,NULL),(272,'169.254.1.17',1,1,NULL,NULL),(273,'169.254.1.18',1,1,NULL,NULL),(274,'169.254.1.19',1,1,NULL,NULL),(275,'169.254.1.20',1,1,NULL,NULL),(276,'169.254.1.21',1,1,NULL,NULL),(277,'169.254.1.22',1,1,NULL,NULL),(278,'169.254.1.23',1,1,NULL,NULL),(279,'169.254.1.24',1,1,NULL,NULL),(280,'169.254.1.25',1,1,NULL,NULL),(281,'169.254.1.26',1,1,NULL,NULL),(282,'169.254.1.27',1,1,NULL,NULL),(283,'169.254.1.28',1,1,NULL,NULL),(284,'169.254.1.29',1,1,NULL,NULL),(285,'169.254.1.30',1,1,NULL,NULL),(286,'169.254.1.31',1,1,NULL,NULL),(287,'169.254.1.32',1,1,NULL,NULL),(288,'169.254.1.33',1,1,NULL,NULL),(289,'169.254.1.34',1,1,NULL,NULL),(290,'169.254.1.35',1,1,NULL,NULL),(291,'169.254.1.36',1,1,NULL,NULL),(292,'169.254.1.37',1,1,NULL,NULL),(293,'169.254.1.38',1,1,NULL,NULL),(294,'169.254.1.39',1,1,NULL,NULL),(295,'169.254.1.40',1,1,NULL,NULL),(296,'169.254.1.41',1,1,NULL,NULL),(297,'169.254.1.42',1,1,NULL,NULL),(298,'169.254.1.43',1,1,NULL,NULL),(299,'169.254.1.44',1,1,NULL,NULL),(300,'169.254.1.45',1,1,NULL,NULL),(301,'169.254.1.46',1,1,NULL,NULL),(302,'169.254.1.47',1,1,NULL,NULL),(303,'169.254.1.48',1,1,NULL,NULL),(304,'169.254.1.49',1,1,NULL,NULL),(305,'169.254.1.50',1,1,NULL,NULL),(306,'169.254.1.51',1,1,NULL,NULL),(307,'169.254.1.52',1,1,NULL,NULL),(308,'169.254.1.53',1,1,NULL,NULL),(309,'169.254.1.54',1,1,NULL,NULL),(310,'169.254.1.55',1,1,NULL,NULL),(311,'169.254.1.56',1,1,NULL,NULL),(312,'169.254.1.57',1,1,NULL,NULL),(313,'169.254.1.58',1,1,NULL,NULL),(314,'169.254.1.59',1,1,NULL,NULL),(315,'169.254.1.60',1,1,NULL,NULL),(316,'169.254.1.61',1,1,NULL,NULL),(317,'169.254.1.62',1,1,NULL,NULL),(318,'169.254.1.63',1,1,NULL,NULL),(319,'169.254.1.64',1,1,NULL,NULL),(320,'169.254.1.65',1,1,NULL,NULL),(321,'169.254.1.66',1,1,NULL,NULL),(322,'169.254.1.67',1,1,NULL,NULL),(323,'169.254.1.68',1,1,NULL,NULL),(324,'169.254.1.69',1,1,NULL,NULL),(325,'169.254.1.70',1,1,NULL,NULL),(326,'169.254.1.71',1,1,NULL,NULL),(327,'169.254.1.72',1,1,NULL,NULL),(328,'169.254.1.73',1,1,NULL,NULL),(329,'169.254.1.74',1,1,NULL,NULL),(330,'169.254.1.75',1,1,NULL,NULL),(331,'169.254.1.76',1,1,NULL,NULL),(332,'169.254.1.77',1,1,NULL,NULL),(333,'169.254.1.78',1,1,NULL,NULL),(334,'169.254.1.79',1,1,NULL,NULL),(335,'169.254.1.80',1,1,NULL,NULL),(336,'169.254.1.81',1,1,NULL,NULL),(337,'169.254.1.82',1,1,NULL,NULL),(338,'169.254.1.83',1,1,NULL,NULL),(339,'169.254.1.84',1,1,NULL,NULL),(340,'169.254.1.85',1,1,NULL,NULL),(341,'169.254.1.86',1,1,NULL,NULL),(342,'169.254.1.87',1,1,NULL,NULL),(343,'169.254.1.88',1,1,NULL,NULL),(344,'169.254.1.89',1,1,NULL,NULL),(345,'169.254.1.90',1,1,NULL,NULL),(346,'169.254.1.91',1,1,NULL,NULL),(347,'169.254.1.92',1,1,NULL,NULL),(348,'169.254.1.93',1,1,NULL,NULL),(349,'169.254.1.94',1,1,NULL,NULL),(350,'169.254.1.95',1,1,NULL,NULL),(351,'169.254.1.96',1,1,NULL,NULL),(352,'169.254.1.97',1,1,NULL,NULL),(353,'169.254.1.98',1,1,NULL,NULL),(354,'169.254.1.99',1,1,NULL,NULL),(355,'169.254.1.100',1,1,NULL,NULL),(356,'169.254.1.101',1,1,NULL,NULL),(357,'169.254.1.102',1,1,NULL,NULL),(358,'169.254.1.103',1,1,NULL,NULL),(359,'169.254.1.104',1,1,NULL,NULL),(360,'169.254.1.105',1,1,NULL,NULL),(361,'169.254.1.106',1,1,NULL,NULL),(362,'169.254.1.107',1,1,NULL,NULL),(363,'169.254.1.108',1,1,NULL,NULL),(364,'169.254.1.109',1,1,NULL,NULL),(365,'169.254.1.110',1,1,NULL,NULL),(366,'169.254.1.111',1,1,NULL,NULL),(367,'169.254.1.112',1,1,NULL,NULL),(368,'169.254.1.113',1,1,NULL,NULL),(369,'169.254.1.114',1,1,NULL,NULL),(370,'169.254.1.115',1,1,NULL,NULL),(371,'169.254.1.116',1,1,NULL,NULL),(372,'169.254.1.117',1,1,NULL,NULL),(373,'169.254.1.118',1,1,NULL,NULL),(374,'169.254.1.119',1,1,NULL,NULL),(375,'169.254.1.120',1,1,NULL,NULL),(376,'169.254.1.121',1,1,NULL,NULL),(377,'169.254.1.122',1,1,NULL,NULL),(378,'169.254.1.123',1,1,NULL,NULL),(379,'169.254.1.124',1,1,NULL,NULL),(380,'169.254.1.125',1,1,NULL,NULL),(381,'169.254.1.126',1,1,NULL,NULL),(382,'169.254.1.127',1,1,NULL,NULL),(383,'169.254.1.128',1,1,NULL,NULL),(384,'169.254.1.129',1,1,NULL,NULL),(385,'169.254.1.130',1,1,NULL,NULL),(386,'169.254.1.131',1,1,NULL,NULL),(387,'169.254.1.132',1,1,NULL,NULL),(388,'169.254.1.133',1,1,NULL,NULL),(389,'169.254.1.134',1,1,NULL,NULL),(390,'169.254.1.135',1,1,NULL,NULL),(391,'169.254.1.136',1,1,NULL,NULL),(392,'169.254.1.137',1,1,NULL,NULL),(393,'169.254.1.138',1,1,NULL,NULL),(394,'169.254.1.139',1,1,NULL,NULL),(395,'169.254.1.140',1,1,NULL,NULL),(396,'169.254.1.141',1,1,NULL,NULL),(397,'169.254.1.142',1,1,NULL,NULL),(398,'169.254.1.143',1,1,NULL,NULL),(399,'169.254.1.144',1,1,NULL,NULL),(400,'169.254.1.145',1,1,NULL,NULL),(401,'169.254.1.146',1,1,NULL,NULL),(402,'169.254.1.147',1,1,NULL,NULL),(403,'169.254.1.148',1,1,NULL,NULL),(404,'169.254.1.149',1,1,NULL,NULL),(405,'169.254.1.150',1,1,NULL,NULL),(406,'169.254.1.151',1,1,NULL,NULL),(407,'169.254.1.152',1,1,NULL,NULL),(408,'169.254.1.153',1,1,NULL,NULL),(409,'169.254.1.154',1,1,NULL,NULL),(410,'169.254.1.155',1,1,NULL,NULL),(411,'169.254.1.156',1,1,NULL,NULL),(412,'169.254.1.157',1,1,NULL,NULL),(413,'169.254.1.158',1,1,NULL,NULL),(414,'169.254.1.159',1,1,NULL,NULL),(415,'169.254.1.160',1,1,NULL,NULL),(416,'169.254.1.161',1,1,NULL,NULL),(417,'169.254.1.162',1,1,NULL,NULL),(418,'169.254.1.163',1,1,NULL,NULL),(419,'169.254.1.164',1,1,NULL,NULL),(420,'169.254.1.165',1,1,NULL,NULL),(421,'169.254.1.166',1,1,NULL,NULL),(422,'169.254.1.167',1,1,NULL,NULL),(423,'169.254.1.168',1,1,NULL,NULL),(424,'169.254.1.169',1,1,NULL,NULL),(425,'169.254.1.170',1,1,NULL,NULL),(426,'169.254.1.171',1,1,NULL,NULL),(427,'169.254.1.172',1,1,NULL,NULL),(428,'169.254.1.173',1,1,NULL,NULL),(429,'169.254.1.174',1,1,NULL,NULL),(430,'169.254.1.175',1,1,NULL,NULL),(431,'169.254.1.176',1,1,NULL,NULL),(432,'169.254.1.177',1,1,NULL,NULL),(433,'169.254.1.178',1,1,NULL,NULL),(434,'169.254.1.179',1,1,NULL,NULL),(435,'169.254.1.180',1,1,NULL,NULL),(436,'169.254.1.181',1,1,NULL,NULL),(437,'169.254.1.182',1,1,NULL,NULL),(438,'169.254.1.183',1,1,NULL,NULL),(439,'169.254.1.184',1,1,NULL,NULL),(440,'169.254.1.185',1,1,NULL,NULL),(441,'169.254.1.186',1,1,NULL,NULL),(442,'169.254.1.187',1,1,NULL,NULL),(443,'169.254.1.188',1,1,NULL,NULL),(444,'169.254.1.189',1,1,NULL,NULL),(445,'169.254.1.190',1,1,NULL,NULL),(446,'169.254.1.191',1,1,NULL,NULL),(447,'169.254.1.192',1,1,NULL,NULL),(448,'169.254.1.193',1,1,NULL,NULL),(449,'169.254.1.194',1,1,NULL,NULL),(450,'169.254.1.195',1,1,NULL,NULL),(451,'169.254.1.196',1,1,NULL,NULL),(452,'169.254.1.197',1,1,NULL,NULL),(453,'169.254.1.198',1,1,NULL,NULL),(454,'169.254.1.199',1,1,NULL,NULL),(455,'169.254.1.200',1,1,NULL,NULL),(456,'169.254.1.201',1,1,NULL,NULL),(457,'169.254.1.202',1,1,NULL,NULL),(458,'169.254.1.203',1,1,NULL,NULL),(459,'169.254.1.204',1,1,NULL,NULL),(460,'169.254.1.205',1,1,NULL,NULL),(461,'169.254.1.206',1,1,NULL,NULL),(462,'169.254.1.207',1,1,NULL,NULL),(463,'169.254.1.208',1,1,NULL,NULL),(464,'169.254.1.209',1,1,NULL,NULL),(465,'169.254.1.210',1,1,NULL,NULL),(466,'169.254.1.211',1,1,NULL,NULL),(467,'169.254.1.212',1,1,NULL,NULL),(468,'169.254.1.213',1,1,NULL,NULL),(469,'169.254.1.214',1,1,NULL,NULL),(470,'169.254.1.215',1,1,NULL,NULL),(471,'169.254.1.216',1,1,NULL,NULL),(472,'169.254.1.217',1,1,NULL,NULL),(473,'169.254.1.218',1,1,NULL,NULL),(474,'169.254.1.219',1,1,NULL,NULL),(475,'169.254.1.220',1,1,NULL,NULL),(476,'169.254.1.221',1,1,NULL,NULL),(477,'169.254.1.222',1,1,NULL,NULL),(478,'169.254.1.223',1,1,NULL,NULL),(479,'169.254.1.224',1,1,NULL,NULL),(480,'169.254.1.225',1,1,NULL,NULL),(481,'169.254.1.226',1,1,NULL,NULL),(482,'169.254.1.227',1,1,NULL,NULL),(483,'169.254.1.228',1,1,NULL,NULL),(484,'169.254.1.229',1,1,NULL,NULL),(485,'169.254.1.230',1,1,NULL,NULL),(486,'169.254.1.231',1,1,NULL,NULL),(487,'169.254.1.232',1,1,NULL,NULL),(488,'169.254.1.233',1,1,NULL,NULL),(489,'169.254.1.234',1,1,NULL,NULL),(490,'169.254.1.235',1,1,NULL,NULL),(491,'169.254.1.236',1,1,NULL,NULL),(492,'169.254.1.237',1,1,NULL,NULL),(493,'169.254.1.238',1,1,NULL,NULL),(494,'169.254.1.239',1,1,NULL,NULL),(495,'169.254.1.240',1,1,NULL,NULL),(496,'169.254.1.241',1,1,NULL,NULL),(497,'169.254.1.242',1,1,NULL,NULL),(498,'169.254.1.243',1,1,NULL,NULL),(499,'169.254.1.244',1,1,NULL,NULL),(500,'169.254.1.245',1,1,NULL,NULL),(501,'169.254.1.246',1,1,NULL,NULL),(502,'169.254.1.247',1,1,NULL,NULL),(503,'169.254.1.248',1,1,NULL,NULL),(504,'169.254.1.249',1,1,NULL,NULL),(505,'169.254.1.250',1,1,NULL,NULL),(506,'169.254.1.251',1,1,NULL,NULL),(507,'169.254.1.252',1,1,NULL,NULL),(508,'169.254.1.253',1,1,NULL,NULL),(509,'169.254.1.254',1,1,NULL,NULL),(510,'169.254.1.255',1,1,NULL,NULL),(511,'169.254.2.0',1,1,NULL,NULL),(512,'169.254.2.1',1,1,NULL,NULL),(513,'169.254.2.2',1,1,NULL,NULL),(514,'169.254.2.3',1,1,NULL,NULL),(515,'169.254.2.4',1,1,NULL,NULL),(516,'169.254.2.5',1,1,NULL,NULL),(517,'169.254.2.6',1,1,NULL,NULL),(518,'169.254.2.7',1,1,NULL,NULL),(519,'169.254.2.8',1,1,NULL,NULL),(520,'169.254.2.9',1,1,NULL,NULL),(521,'169.254.2.10',1,1,NULL,NULL),(522,'169.254.2.11',1,1,NULL,NULL),(523,'169.254.2.12',1,1,NULL,NULL),(524,'169.254.2.13',1,1,NULL,NULL),(525,'169.254.2.14',1,1,NULL,NULL),(526,'169.254.2.15',1,1,NULL,NULL),(527,'169.254.2.16',1,1,NULL,NULL),(528,'169.254.2.17',1,1,NULL,NULL),(529,'169.254.2.18',1,1,NULL,NULL),(530,'169.254.2.19',1,1,NULL,NULL),(531,'169.254.2.20',1,1,NULL,NULL),(532,'169.254.2.21',1,1,NULL,NULL),(533,'169.254.2.22',1,1,NULL,NULL),(534,'169.254.2.23',1,1,NULL,NULL),(535,'169.254.2.24',1,1,NULL,NULL),(536,'169.254.2.25',1,1,NULL,NULL),(537,'169.254.2.26',1,1,NULL,NULL),(538,'169.254.2.27',1,1,NULL,NULL),(539,'169.254.2.28',1,1,NULL,NULL),(540,'169.254.2.29',1,1,NULL,NULL),(541,'169.254.2.30',1,1,NULL,NULL),(542,'169.254.2.31',1,1,NULL,NULL),(543,'169.254.2.32',1,1,NULL,NULL),(544,'169.254.2.33',1,1,NULL,NULL),(545,'169.254.2.34',1,1,NULL,NULL),(546,'169.254.2.35',1,1,NULL,NULL),(547,'169.254.2.36',1,1,NULL,NULL),(548,'169.254.2.37',1,1,NULL,NULL),(549,'169.254.2.38',1,1,NULL,NULL),(550,'169.254.2.39',1,1,NULL,NULL),(551,'169.254.2.40',1,1,NULL,NULL),(552,'169.254.2.41',1,1,NULL,NULL),(553,'169.254.2.42',1,1,NULL,NULL),(554,'169.254.2.43',1,1,NULL,NULL),(555,'169.254.2.44',1,1,NULL,NULL),(556,'169.254.2.45',1,1,NULL,NULL),(557,'169.254.2.46',1,1,NULL,NULL),(558,'169.254.2.47',1,1,NULL,NULL),(559,'169.254.2.48',1,1,NULL,NULL),(560,'169.254.2.49',1,1,NULL,NULL),(561,'169.254.2.50',1,1,NULL,NULL),(562,'169.254.2.51',1,1,NULL,NULL),(563,'169.254.2.52',1,1,NULL,NULL),(564,'169.254.2.53',1,1,NULL,NULL),(565,'169.254.2.54',1,1,NULL,NULL),(566,'169.254.2.55',1,1,NULL,NULL),(567,'169.254.2.56',1,1,NULL,NULL),(568,'169.254.2.57',1,1,NULL,NULL),(569,'169.254.2.58',1,1,NULL,NULL),(570,'169.254.2.59',1,1,NULL,NULL),(571,'169.254.2.60',1,1,NULL,NULL),(572,'169.254.2.61',1,1,NULL,NULL),(573,'169.254.2.62',1,1,NULL,NULL),(574,'169.254.2.63',1,1,NULL,NULL),(575,'169.254.2.64',1,1,NULL,NULL),(576,'169.254.2.65',1,1,NULL,NULL),(577,'169.254.2.66',1,1,NULL,NULL),(578,'169.254.2.67',1,1,NULL,NULL),(579,'169.254.2.68',1,1,NULL,NULL),(580,'169.254.2.69',1,1,NULL,NULL),(581,'169.254.2.70',1,1,NULL,NULL),(582,'169.254.2.71',1,1,NULL,NULL),(583,'169.254.2.72',1,1,NULL,NULL),(584,'169.254.2.73',1,1,NULL,NULL),(585,'169.254.2.74',1,1,NULL,NULL),(586,'169.254.2.75',1,1,NULL,NULL),(587,'169.254.2.76',1,1,NULL,NULL),(588,'169.254.2.77',1,1,NULL,NULL),(589,'169.254.2.78',1,1,NULL,NULL),(590,'169.254.2.79',1,1,NULL,NULL),(591,'169.254.2.80',1,1,NULL,NULL),(592,'169.254.2.81',1,1,NULL,NULL),(593,'169.254.2.82',1,1,NULL,NULL),(594,'169.254.2.83',1,1,NULL,NULL),(595,'169.254.2.84',1,1,NULL,NULL),(596,'169.254.2.85',1,1,NULL,NULL),(597,'169.254.2.86',1,1,NULL,NULL),(598,'169.254.2.87',1,1,NULL,NULL),(599,'169.254.2.88',1,1,NULL,NULL),(600,'169.254.2.89',1,1,NULL,NULL),(601,'169.254.2.90',1,1,NULL,NULL),(602,'169.254.2.91',1,1,NULL,NULL),(603,'169.254.2.92',1,1,NULL,NULL),(604,'169.254.2.93',1,1,NULL,NULL),(605,'169.254.2.94',1,1,NULL,NULL),(606,'169.254.2.95',1,1,NULL,NULL),(607,'169.254.2.96',1,1,NULL,NULL),(608,'169.254.2.97',1,1,NULL,NULL),(609,'169.254.2.98',1,1,NULL,NULL),(610,'169.254.2.99',1,1,NULL,NULL),(611,'169.254.2.100',1,1,NULL,NULL),(612,'169.254.2.101',1,1,NULL,NULL),(613,'169.254.2.102',1,1,NULL,NULL),(614,'169.254.2.103',1,1,NULL,NULL),(615,'169.254.2.104',1,1,NULL,NULL),(616,'169.254.2.105',1,1,NULL,NULL),(617,'169.254.2.106',1,1,NULL,NULL),(618,'169.254.2.107',1,1,NULL,NULL),(619,'169.254.2.108',1,1,NULL,NULL),(620,'169.254.2.109',1,1,NULL,NULL),(621,'169.254.2.110',1,1,NULL,NULL),(622,'169.254.2.111',1,1,NULL,NULL),(623,'169.254.2.112',1,1,NULL,NULL),(624,'169.254.2.113',1,1,NULL,NULL),(625,'169.254.2.114',1,1,NULL,NULL),(626,'169.254.2.115',1,1,NULL,NULL),(627,'169.254.2.116',1,1,NULL,NULL),(628,'169.254.2.117',1,1,NULL,NULL),(629,'169.254.2.118',1,1,NULL,NULL),(630,'169.254.2.119',1,1,NULL,NULL),(631,'169.254.2.120',1,1,NULL,NULL),(632,'169.254.2.121',1,1,NULL,NULL),(633,'169.254.2.122',1,1,NULL,NULL),(634,'169.254.2.123',1,1,NULL,NULL),(635,'169.254.2.124',1,1,NULL,NULL),(636,'169.254.2.125',1,1,NULL,NULL),(637,'169.254.2.126',1,1,NULL,NULL),(638,'169.254.2.127',1,1,NULL,NULL),(639,'169.254.2.128',1,1,NULL,NULL),(640,'169.254.2.129',1,1,NULL,NULL),(641,'169.254.2.130',1,1,NULL,NULL),(642,'169.254.2.131',1,1,NULL,NULL),(643,'169.254.2.132',1,1,NULL,NULL),(644,'169.254.2.133',1,1,NULL,NULL),(645,'169.254.2.134',1,1,NULL,NULL),(646,'169.254.2.135',1,1,NULL,NULL),(647,'169.254.2.136',1,1,NULL,NULL),(648,'169.254.2.137',1,1,NULL,NULL),(649,'169.254.2.138',1,1,NULL,NULL),(650,'169.254.2.139',1,1,NULL,NULL),(651,'169.254.2.140',1,1,NULL,NULL),(652,'169.254.2.141',1,1,NULL,NULL),(653,'169.254.2.142',1,1,NULL,NULL),(654,'169.254.2.143',1,1,NULL,NULL),(655,'169.254.2.144',1,1,NULL,NULL),(656,'169.254.2.145',1,1,NULL,NULL),(657,'169.254.2.146',1,1,NULL,NULL),(658,'169.254.2.147',1,1,NULL,NULL),(659,'169.254.2.148',1,1,NULL,NULL),(660,'169.254.2.149',1,1,NULL,NULL),(661,'169.254.2.150',1,1,NULL,NULL),(662,'169.254.2.151',1,1,NULL,NULL),(663,'169.254.2.152',1,1,NULL,NULL),(664,'169.254.2.153',1,1,NULL,NULL),(665,'169.254.2.154',1,1,NULL,NULL),(666,'169.254.2.155',1,1,NULL,NULL),(667,'169.254.2.156',1,1,NULL,NULL),(668,'169.254.2.157',1,1,NULL,NULL),(669,'169.254.2.158',1,1,NULL,NULL),(670,'169.254.2.159',1,1,NULL,NULL),(671,'169.254.2.160',1,1,NULL,NULL),(672,'169.254.2.161',1,1,NULL,NULL),(673,'169.254.2.162',1,1,NULL,NULL),(674,'169.254.2.163',1,1,NULL,NULL),(675,'169.254.2.164',1,1,NULL,NULL),(676,'169.254.2.165',1,1,NULL,NULL),(677,'169.254.2.166',1,1,NULL,NULL),(678,'169.254.2.167',1,1,NULL,NULL),(679,'169.254.2.168',1,1,NULL,NULL),(680,'169.254.2.169',1,1,NULL,NULL),(681,'169.254.2.170',1,1,NULL,NULL),(682,'169.254.2.171',1,1,NULL,NULL),(683,'169.254.2.172',1,1,NULL,NULL),(684,'169.254.2.173',1,1,NULL,NULL),(685,'169.254.2.174',1,1,NULL,NULL),(686,'169.254.2.175',1,1,NULL,NULL),(687,'169.254.2.176',1,1,NULL,NULL),(688,'169.254.2.177',1,1,NULL,NULL),(689,'169.254.2.178',1,1,NULL,NULL),(690,'169.254.2.179',1,1,NULL,NULL),(691,'169.254.2.180',1,1,NULL,NULL),(692,'169.254.2.181',1,1,NULL,NULL),(693,'169.254.2.182',1,1,NULL,NULL),(694,'169.254.2.183',1,1,NULL,NULL),(695,'169.254.2.184',1,1,NULL,NULL),(696,'169.254.2.185',1,1,NULL,NULL),(697,'169.254.2.186',1,1,NULL,NULL),(698,'169.254.2.187',1,1,NULL,NULL),(699,'169.254.2.188',1,1,NULL,NULL),(700,'169.254.2.189',1,1,NULL,NULL),(701,'169.254.2.190',1,1,NULL,NULL),(702,'169.254.2.191',1,1,NULL,NULL),(703,'169.254.2.192',1,1,NULL,NULL),(704,'169.254.2.193',1,1,NULL,NULL),(705,'169.254.2.194',1,1,NULL,NULL),(706,'169.254.2.195',1,1,NULL,NULL),(707,'169.254.2.196',1,1,NULL,NULL),(708,'169.254.2.197',1,1,NULL,NULL),(709,'169.254.2.198',1,1,NULL,NULL),(710,'169.254.2.199',1,1,NULL,NULL),(711,'169.254.2.200',1,1,NULL,NULL),(712,'169.254.2.201',1,1,NULL,NULL),(713,'169.254.2.202',1,1,NULL,NULL),(714,'169.254.2.203',1,1,NULL,NULL),(715,'169.254.2.204',1,1,NULL,NULL),(716,'169.254.2.205',1,1,NULL,NULL),(717,'169.254.2.206',1,1,NULL,NULL),(718,'169.254.2.207',1,1,NULL,NULL),(719,'169.254.2.208',1,1,NULL,NULL),(720,'169.254.2.209',1,1,NULL,NULL),(721,'169.254.2.210',1,1,NULL,NULL),(722,'169.254.2.211',1,1,NULL,NULL),(723,'169.254.2.212',1,1,NULL,NULL),(724,'169.254.2.213',1,1,NULL,NULL),(725,'169.254.2.214',1,1,NULL,NULL),(726,'169.254.2.215',1,1,NULL,NULL),(727,'169.254.2.216',1,1,NULL,NULL),(728,'169.254.2.217',1,1,NULL,NULL),(729,'169.254.2.218',1,1,NULL,NULL),(730,'169.254.2.219',1,1,NULL,NULL),(731,'169.254.2.220',1,1,NULL,NULL),(732,'169.254.2.221',1,1,NULL,NULL),(733,'169.254.2.222',1,1,NULL,NULL),(734,'169.254.2.223',1,1,NULL,NULL),(735,'169.254.2.224',1,1,NULL,NULL),(736,'169.254.2.225',1,1,NULL,NULL),(737,'169.254.2.226',1,1,NULL,NULL),(738,'169.254.2.227',1,1,NULL,NULL),(739,'169.254.2.228',1,1,NULL,NULL),(740,'169.254.2.229',1,1,NULL,NULL),(741,'169.254.2.230',1,1,NULL,NULL),(742,'169.254.2.231',1,1,NULL,NULL),(743,'169.254.2.232',1,1,NULL,NULL),(744,'169.254.2.233',1,1,NULL,NULL),(745,'169.254.2.234',1,1,NULL,NULL),(746,'169.254.2.235',1,1,NULL,NULL),(747,'169.254.2.236',1,1,NULL,NULL),(748,'169.254.2.237',1,1,NULL,NULL),(749,'169.254.2.238',1,1,NULL,NULL),(750,'169.254.2.239',1,1,NULL,NULL),(751,'169.254.2.240',1,1,NULL,NULL),(752,'169.254.2.241',1,1,NULL,NULL),(753,'169.254.2.242',1,1,NULL,NULL),(754,'169.254.2.243',1,1,NULL,NULL),(755,'169.254.2.244',1,1,NULL,NULL),(756,'169.254.2.245',1,1,NULL,NULL),(757,'169.254.2.246',1,1,NULL,NULL),(758,'169.254.2.247',1,1,NULL,NULL),(759,'169.254.2.248',1,1,NULL,NULL),(760,'169.254.2.249',1,1,NULL,NULL),(761,'169.254.2.250',1,1,NULL,NULL),(762,'169.254.2.251',1,1,NULL,NULL),(763,'169.254.2.252',1,1,NULL,NULL),(764,'169.254.2.253',1,1,NULL,NULL),(765,'169.254.2.254',1,1,NULL,NULL),(766,'169.254.2.255',1,1,NULL,NULL),(767,'169.254.3.0',1,1,NULL,NULL),(768,'169.254.3.1',1,1,NULL,NULL),(769,'169.254.3.2',1,1,NULL,NULL),(770,'169.254.3.3',1,1,NULL,NULL),(771,'169.254.3.4',1,1,NULL,NULL),(772,'169.254.3.5',1,1,NULL,NULL),(773,'169.254.3.6',1,1,NULL,NULL),(774,'169.254.3.7',1,1,NULL,NULL),(775,'169.254.3.8',1,1,NULL,NULL),(776,'169.254.3.9',1,1,NULL,NULL),(777,'169.254.3.10',1,1,NULL,NULL),(778,'169.254.3.11',1,1,NULL,NULL),(779,'169.254.3.12',1,1,NULL,NULL),(780,'169.254.3.13',1,1,NULL,NULL),(781,'169.254.3.14',1,1,NULL,NULL),(782,'169.254.3.15',1,1,NULL,NULL),(783,'169.254.3.16',1,1,NULL,NULL),(784,'169.254.3.17',1,1,NULL,NULL),(785,'169.254.3.18',1,1,NULL,NULL),(786,'169.254.3.19',1,1,NULL,NULL),(787,'169.254.3.20',1,1,NULL,NULL),(788,'169.254.3.21',1,1,NULL,NULL),(789,'169.254.3.22',1,1,NULL,NULL),(790,'169.254.3.23',1,1,NULL,NULL),(791,'169.254.3.24',1,1,NULL,NULL),(792,'169.254.3.25',1,1,NULL,NULL),(793,'169.254.3.26',1,1,NULL,NULL),(794,'169.254.3.27',1,1,NULL,NULL),(795,'169.254.3.28',1,1,NULL,NULL),(796,'169.254.3.29',1,1,NULL,NULL),(797,'169.254.3.30',1,1,NULL,NULL),(798,'169.254.3.31',1,1,NULL,NULL),(799,'169.254.3.32',1,1,NULL,NULL),(800,'169.254.3.33',1,1,NULL,NULL),(801,'169.254.3.34',1,1,NULL,NULL),(802,'169.254.3.35',1,1,NULL,NULL),(803,'169.254.3.36',1,1,NULL,NULL),(804,'169.254.3.37',1,1,NULL,NULL),(805,'169.254.3.38',1,1,NULL,NULL),(806,'169.254.3.39',1,1,NULL,NULL),(807,'169.254.3.40',1,1,NULL,NULL),(808,'169.254.3.41',1,1,NULL,NULL),(809,'169.254.3.42',1,1,NULL,NULL),(810,'169.254.3.43',1,1,NULL,NULL),(811,'169.254.3.44',1,1,NULL,NULL),(812,'169.254.3.45',1,1,NULL,NULL),(813,'169.254.3.46',1,1,NULL,NULL),(814,'169.254.3.47',1,1,NULL,NULL),(815,'169.254.3.48',1,1,NULL,NULL),(816,'169.254.3.49',1,1,NULL,NULL),(817,'169.254.3.50',1,1,NULL,NULL),(818,'169.254.3.51',1,1,NULL,NULL),(819,'169.254.3.52',1,1,NULL,NULL),(820,'169.254.3.53',1,1,NULL,NULL),(821,'169.254.3.54',1,1,NULL,NULL),(822,'169.254.3.55',1,1,NULL,NULL),(823,'169.254.3.56',1,1,NULL,NULL),(824,'169.254.3.57',1,1,NULL,NULL),(825,'169.254.3.58',1,1,NULL,NULL),(826,'169.254.3.59',1,1,NULL,NULL),(827,'169.254.3.60',1,1,NULL,NULL),(828,'169.254.3.61',1,1,NULL,NULL),(829,'169.254.3.62',1,1,NULL,NULL),(830,'169.254.3.63',1,1,NULL,NULL),(831,'169.254.3.64',1,1,NULL,NULL),(832,'169.254.3.65',1,1,NULL,NULL),(833,'169.254.3.66',1,1,NULL,NULL),(834,'169.254.3.67',1,1,NULL,NULL),(835,'169.254.3.68',1,1,NULL,NULL),(836,'169.254.3.69',1,1,NULL,NULL),(837,'169.254.3.70',1,1,NULL,NULL),(838,'169.254.3.71',1,1,NULL,NULL),(839,'169.254.3.72',1,1,NULL,NULL),(840,'169.254.3.73',1,1,NULL,NULL),(841,'169.254.3.74',1,1,NULL,NULL),(842,'169.254.3.75',1,1,NULL,NULL),(843,'169.254.3.76',1,1,NULL,NULL),(844,'169.254.3.77',1,1,NULL,NULL),(845,'169.254.3.78',1,1,NULL,NULL),(846,'169.254.3.79',1,1,NULL,NULL),(847,'169.254.3.80',1,1,NULL,NULL),(848,'169.254.3.81',1,1,NULL,NULL),(849,'169.254.3.82',1,1,NULL,NULL),(850,'169.254.3.83',1,1,NULL,NULL),(851,'169.254.3.84',1,1,NULL,NULL),(852,'169.254.3.85',1,1,NULL,NULL),(853,'169.254.3.86',1,1,NULL,NULL),(854,'169.254.3.87',1,1,NULL,NULL),(855,'169.254.3.88',1,1,NULL,NULL),(856,'169.254.3.89',1,1,NULL,NULL),(857,'169.254.3.90',1,1,NULL,NULL),(858,'169.254.3.91',1,1,NULL,NULL),(859,'169.254.3.92',1,1,NULL,NULL),(860,'169.254.3.93',1,1,NULL,NULL),(861,'169.254.3.94',1,1,NULL,NULL),(862,'169.254.3.95',1,1,NULL,NULL),(863,'169.254.3.96',1,1,NULL,NULL),(864,'169.254.3.97',1,1,NULL,NULL),(865,'169.254.3.98',1,1,NULL,NULL),(866,'169.254.3.99',1,1,NULL,NULL),(867,'169.254.3.100',1,1,NULL,NULL),(868,'169.254.3.101',1,1,NULL,NULL),(869,'169.254.3.102',1,1,NULL,NULL),(870,'169.254.3.103',1,1,NULL,NULL),(871,'169.254.3.104',1,1,NULL,NULL),(872,'169.254.3.105',1,1,NULL,NULL),(873,'169.254.3.106',1,1,NULL,NULL),(874,'169.254.3.107',1,1,NULL,NULL),(875,'169.254.3.108',1,1,NULL,NULL),(876,'169.254.3.109',1,1,NULL,NULL),(877,'169.254.3.110',1,1,NULL,NULL),(878,'169.254.3.111',1,1,NULL,NULL),(879,'169.254.3.112',1,1,NULL,NULL),(880,'169.254.3.113',1,1,NULL,NULL),(881,'169.254.3.114',1,1,NULL,NULL),(882,'169.254.3.115',1,1,NULL,NULL),(883,'169.254.3.116',1,1,NULL,NULL),(884,'169.254.3.117',1,1,NULL,NULL),(885,'169.254.3.118',1,1,NULL,NULL),(886,'169.254.3.119',1,1,NULL,NULL),(887,'169.254.3.120',1,1,NULL,NULL),(888,'169.254.3.121',1,1,NULL,NULL),(889,'169.254.3.122',1,1,NULL,NULL),(890,'169.254.3.123',1,1,NULL,NULL),(891,'169.254.3.124',1,1,NULL,NULL),(892,'169.254.3.125',1,1,NULL,NULL),(893,'169.254.3.126',1,1,NULL,NULL),(894,'169.254.3.127',1,1,NULL,NULL),(895,'169.254.3.128',1,1,NULL,NULL),(896,'169.254.3.129',1,1,NULL,NULL),(897,'169.254.3.130',1,1,NULL,NULL),(898,'169.254.3.131',1,1,NULL,NULL),(899,'169.254.3.132',1,1,NULL,NULL),(900,'169.254.3.133',1,1,NULL,NULL),(901,'169.254.3.134',1,1,NULL,NULL),(902,'169.254.3.135',1,1,NULL,NULL),(903,'169.254.3.136',1,1,NULL,NULL),(904,'169.254.3.137',1,1,NULL,NULL),(905,'169.254.3.138',1,1,NULL,NULL),(906,'169.254.3.139',1,1,NULL,NULL),(907,'169.254.3.140',1,1,NULL,NULL),(908,'169.254.3.141',1,1,NULL,NULL),(909,'169.254.3.142',1,1,NULL,NULL),(910,'169.254.3.143',1,1,NULL,NULL),(911,'169.254.3.144',1,1,NULL,NULL),(912,'169.254.3.145',1,1,NULL,NULL),(913,'169.254.3.146',1,1,NULL,NULL),(914,'169.254.3.147',1,1,NULL,NULL),(915,'169.254.3.148',1,1,NULL,NULL),(916,'169.254.3.149',1,1,NULL,NULL),(917,'169.254.3.150',1,1,NULL,NULL),(918,'169.254.3.151',1,1,NULL,NULL),(919,'169.254.3.152',1,1,NULL,NULL),(920,'169.254.3.153',1,1,NULL,NULL),(921,'169.254.3.154',1,1,NULL,NULL),(922,'169.254.3.155',1,1,NULL,NULL),(923,'169.254.3.156',1,1,NULL,NULL),(924,'169.254.3.157',1,1,NULL,NULL),(925,'169.254.3.158',1,1,NULL,NULL),(926,'169.254.3.159',1,1,NULL,NULL),(927,'169.254.3.160',1,1,NULL,NULL),(928,'169.254.3.161',1,1,NULL,NULL),(929,'169.254.3.162',1,1,NULL,NULL),(930,'169.254.3.163',1,1,NULL,NULL),(931,'169.254.3.164',1,1,NULL,NULL),(932,'169.254.3.165',1,1,NULL,NULL),(933,'169.254.3.166',1,1,NULL,NULL),(934,'169.254.3.167',1,1,NULL,NULL),(935,'169.254.3.168',1,1,NULL,NULL),(936,'169.254.3.169',1,1,NULL,NULL),(937,'169.254.3.170',1,1,NULL,NULL),(938,'169.254.3.171',1,1,NULL,NULL),(939,'169.254.3.172',1,1,NULL,NULL),(940,'169.254.3.173',1,1,NULL,NULL),(941,'169.254.3.174',1,1,NULL,NULL),(942,'169.254.3.175',1,1,NULL,NULL),(943,'169.254.3.176',1,1,NULL,NULL),(944,'169.254.3.177',1,1,NULL,NULL),(945,'169.254.3.178',1,1,NULL,NULL),(946,'169.254.3.179',1,1,NULL,NULL),(947,'169.254.3.180',1,1,NULL,NULL),(948,'169.254.3.181',1,1,NULL,NULL),(949,'169.254.3.182',1,1,NULL,NULL),(950,'169.254.3.183',1,1,NULL,NULL),(951,'169.254.3.184',1,1,NULL,NULL),(952,'169.254.3.185',1,1,NULL,NULL),(953,'169.254.3.186',1,1,NULL,NULL),(954,'169.254.3.187',1,1,NULL,NULL),(955,'169.254.3.188',1,1,NULL,NULL),(956,'169.254.3.189',1,1,NULL,NULL),(957,'169.254.3.190',1,1,NULL,NULL),(958,'169.254.3.191',1,1,NULL,NULL),(959,'169.254.3.192',1,1,NULL,NULL),(960,'169.254.3.193',1,1,NULL,NULL),(961,'169.254.3.194',1,1,NULL,NULL),(962,'169.254.3.195',1,1,NULL,NULL),(963,'169.254.3.196',1,1,NULL,NULL),(964,'169.254.3.197',1,1,NULL,NULL),(965,'169.254.3.198',1,1,NULL,NULL),(966,'169.254.3.199',1,1,NULL,NULL),(967,'169.254.3.200',1,1,NULL,NULL),(968,'169.254.3.201',1,1,NULL,NULL),(969,'169.254.3.202',1,1,NULL,NULL),(970,'169.254.3.203',1,1,NULL,NULL),(971,'169.254.3.204',1,1,NULL,NULL),(972,'169.254.3.205',1,1,NULL,NULL),(973,'169.254.3.206',1,1,NULL,NULL),(974,'169.254.3.207',1,1,NULL,NULL),(975,'169.254.3.208',1,1,NULL,NULL),(976,'169.254.3.209',1,1,NULL,NULL),(977,'169.254.3.210',1,1,NULL,NULL),(978,'169.254.3.211',1,1,NULL,NULL),(979,'169.254.3.212',1,1,NULL,NULL),(980,'169.254.3.213',1,1,NULL,NULL),(981,'169.254.3.214',1,1,NULL,NULL),(982,'169.254.3.215',1,1,NULL,NULL),(983,'169.254.3.216',1,1,NULL,NULL),(984,'169.254.3.217',1,1,NULL,NULL),(985,'169.254.3.218',1,1,NULL,NULL),(986,'169.254.3.219',1,1,NULL,NULL),(987,'169.254.3.220',1,1,NULL,NULL),(988,'169.254.3.221',1,1,NULL,NULL),(989,'169.254.3.222',1,1,NULL,NULL),(990,'169.254.3.223',1,1,NULL,NULL),(991,'169.254.3.224',1,1,NULL,NULL),(992,'169.254.3.225',1,1,NULL,NULL),(993,'169.254.3.226',1,1,NULL,NULL),(994,'169.254.3.227',1,1,NULL,NULL),(995,'169.254.3.228',1,1,NULL,NULL),(996,'169.254.3.229',1,1,NULL,NULL),(997,'169.254.3.230',1,1,NULL,NULL),(998,'169.254.3.231',1,1,NULL,NULL),(999,'169.254.3.232',1,1,NULL,NULL),(1000,'169.254.3.233',1,1,NULL,NULL),(1001,'169.254.3.234',1,1,NULL,NULL),(1002,'169.254.3.235',1,1,NULL,NULL),(1003,'169.254.3.236',1,1,NULL,NULL),(1004,'169.254.3.237',1,1,NULL,NULL),(1005,'169.254.3.238',1,1,NULL,NULL),(1006,'169.254.3.239',1,1,NULL,NULL),(1007,'169.254.3.240',1,1,NULL,NULL),(1008,'169.254.3.241',1,1,NULL,NULL),(1009,'169.254.3.242',1,1,NULL,NULL),(1010,'169.254.3.243',1,1,NULL,NULL),(1011,'169.254.3.244',1,1,NULL,NULL),(1012,'169.254.3.245',1,1,NULL,NULL),(1013,'169.254.3.246',1,1,NULL,NULL),(1014,'169.254.3.247',1,1,NULL,NULL),(1015,'169.254.3.248',1,1,NULL,NULL),(1016,'169.254.3.249',1,1,NULL,NULL),(1017,'169.254.3.250',1,1,NULL,NULL),(1018,'169.254.3.251',1,1,NULL,NULL),(1019,'169.254.3.252',1,1,NULL,NULL),(1020,'169.254.3.253',1,1,NULL,NULL),(1021,'169.254.3.254',1,1,NULL,NULL);
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
  `account_id` bigint(20) unsigned default NULL COMMENT 'account the vnet belongs to right now',
  `taken` datetime default NULL COMMENT 'Date taken',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `i_op_dc_vnet_alloc__vnet__data_center_id` (`vnet`,`data_center_id`),
  UNIQUE KEY `i_op_dc_vnet_alloc__vnet__data_center_id__account_id` (`vnet`,`data_center_id`,`account_id`),
  KEY `i_op_dc_vnet_alloc__dc_taken` (`data_center_id`,`taken`)
) ENGINE=InnoDB AUTO_INCREMENT=1082 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `op_dc_vnet_alloc`
--

LOCK TABLES `op_dc_vnet_alloc` WRITE;
/*!40000 ALTER TABLE `op_dc_vnet_alloc` DISABLE KEYS */;
INSERT INTO `op_dc_vnet_alloc` VALUES (1002,'1240',1,2,'2011-04-23 02:25:11'),(1003,'1241',1,3,'2011-04-23 02:38:56'),(1004,'1242',1,NULL,NULL),(1005,'1243',1,NULL,NULL),(1006,'1244',1,NULL,NULL),(1007,'1245',1,NULL,NULL),(1008,'1246',1,NULL,NULL),(1009,'1247',1,NULL,NULL),(1010,'1248',1,NULL,NULL),(1011,'1249',1,NULL,NULL),(1012,'1250',1,NULL,NULL),(1013,'1251',1,NULL,NULL),(1014,'1252',1,NULL,NULL),(1015,'1253',1,NULL,NULL),(1016,'1254',1,NULL,NULL),(1017,'1255',1,NULL,NULL),(1018,'1256',1,NULL,NULL),(1019,'1257',1,NULL,NULL),(1020,'1258',1,NULL,NULL),(1021,'1259',1,NULL,NULL),(1022,'1260',1,NULL,NULL),(1023,'1261',1,NULL,NULL),(1024,'1262',1,NULL,NULL),(1025,'1263',1,NULL,NULL),(1026,'1264',1,NULL,NULL),(1027,'1265',1,NULL,NULL),(1028,'1266',1,NULL,NULL),(1029,'1267',1,NULL,NULL),(1030,'1268',1,NULL,NULL),(1031,'1269',1,NULL,NULL),(1032,'1270',1,NULL,NULL),(1033,'1271',1,NULL,NULL),(1034,'1272',1,NULL,NULL),(1035,'1273',1,NULL,NULL),(1036,'1274',1,NULL,NULL),(1037,'1275',1,NULL,NULL),(1038,'1276',1,NULL,NULL),(1039,'1277',1,NULL,NULL),(1040,'1278',1,NULL,NULL),(1041,'1279',1,NULL,NULL),(1042,'1280',1,NULL,NULL),(1043,'1281',1,NULL,NULL),(1044,'1282',1,NULL,NULL),(1045,'1283',1,NULL,NULL),(1046,'1284',1,NULL,NULL),(1047,'1285',1,NULL,NULL),(1048,'1286',1,NULL,NULL),(1049,'1287',1,NULL,NULL),(1050,'1288',1,NULL,NULL),(1051,'1289',1,NULL,NULL),(1052,'1290',1,NULL,NULL),(1053,'1291',1,NULL,NULL),(1054,'1292',1,NULL,NULL),(1055,'1293',1,NULL,NULL),(1056,'1294',1,NULL,NULL),(1057,'1295',1,NULL,NULL),(1058,'1296',1,NULL,NULL),(1059,'1297',1,NULL,NULL),(1060,'1298',1,NULL,NULL),(1061,'1299',1,NULL,NULL),(1062,'1300',1,NULL,NULL),(1063,'1301',1,NULL,NULL),(1064,'1302',1,NULL,NULL),(1065,'1303',1,NULL,NULL),(1066,'1304',1,NULL,NULL),(1067,'1305',1,NULL,NULL),(1068,'1306',1,NULL,NULL),(1069,'1307',1,NULL,NULL),(1070,'1308',1,NULL,NULL),(1071,'1309',1,NULL,NULL),(1072,'1310',1,NULL,NULL),(1073,'1311',1,NULL,NULL),(1074,'1312',1,NULL,NULL),(1075,'1313',1,NULL,NULL),(1076,'1314',1,NULL,NULL),(1077,'1315',1,NULL,NULL),(1078,'1316',1,NULL,NULL),(1079,'1317',1,NULL,NULL),(1080,'1318',1,NULL,NULL),(1081,'1319',1,NULL,NULL);
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
  KEY `i_op_ha_work__mgmt_server_id` (`mgmt_server_id`)
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
  `total_capacity` bigint(20) unsigned NOT NULL,
  `capacity_type` int(1) unsigned NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `i_op_host_capacity__host_type` (`host_id`,`capacity_type`),
  KEY `i_op_host_capacity__pod_id` (`pod_id`),
  KEY `i_op_host_capacity__data_center_id` (`data_center_id`),
  CONSTRAINT `fk_op_host_capacity__data_center_id` FOREIGN KEY (`data_center_id`) REFERENCES `data_center` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_op_host_capacity__pod_id` FOREIGN KEY (`pod_id`) REFERENCES `host_pod_ref` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `op_host_capacity`
--

LOCK TABLES `op_host_capacity` WRITE;
/*!40000 ALTER TABLE `op_host_capacity` DISABLE KEYS */;
INSERT INTO `op_host_capacity` VALUES (7,200,1,1,49980482560,3861039808512,3),(96,2,1,1,2147483648,3244655232,0),(97,2,1,1,400,9040,1),(98,NULL,1,NULL,5,62,4),(99,NULL,1,1,1,11,5);
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
-- Table structure for table `op_vm_host`
--

DROP TABLE IF EXISTS `op_vm_host`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `op_vm_host` (
  `id` bigint(20) unsigned NOT NULL COMMENT 'foreign key to host_id',
  `vnc_ports` bigint(20) unsigned NOT NULL default '0' COMMENT 'vnc ports open on the host',
  `start_at` int(5) unsigned NOT NULL default '0' COMMENT 'Start the vnc port look up at this bit',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`),
  CONSTRAINT `fk_op_vm_host__id` FOREIGN KEY (`id`) REFERENCES `host` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `op_vm_host`
--

LOCK TABLES `op_vm_host` WRITE;
/*!40000 ALTER TABLE `op_vm_host` DISABLE KEYS */;
/*!40000 ALTER TABLE `op_vm_host` ENABLE KEYS */;
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
-- Table structure for table `pricing`
--

DROP TABLE IF EXISTS `pricing`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `pricing` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `price` float unsigned NOT NULL,
  `price_unit` varchar(45) NOT NULL,
  `type` varchar(255) NOT NULL,
  `type_id` int(10) unsigned default NULL,
  `created` datetime NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `pricing`
--

LOCK TABLES `pricing` WRITE;
/*!40000 ALTER TABLE `pricing` DISABLE KEYS */;
/*!40000 ALTER TABLE `pricing` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `resource_count`
--

DROP TABLE IF EXISTS `resource_count`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `resource_count` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `account_id` bigint(20) unsigned NOT NULL,
  `type` varchar(255) default NULL,
  `count` bigint(20) NOT NULL default '0',
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `resource_count`
--

LOCK TABLES `resource_count` WRITE;
/*!40000 ALTER TABLE `resource_count` DISABLE KEYS */;
INSERT INTO `resource_count` VALUES (1,2,'public_ip',2),(2,2,'user_vm',2),(3,2,'volume',3),(4,2,'snapshot',3),(5,2,'template',5),(6,3,'public_ip',1),(7,3,'user_vm',2),(8,3,'volume',5),(9,3,'snapshot',3),(10,3,'template',3);
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
  `gateway` varchar(15) default NULL COMMENT 'gateway info for this sec storage vm towards public network interface',
  `dns1` varchar(15) default NULL COMMENT 'dns1',
  `dns2` varchar(15) default NULL COMMENT 'dns2',
  `domain` varchar(255) default NULL COMMENT 'domain',
  `public_mac_address` varchar(17) NOT NULL COMMENT 'mac address of the public facing network card',
  `public_ip_address` varchar(15) default NULL COMMENT 'public ip address for the sec storage vm',
  `public_netmask` varchar(15) default NULL COMMENT 'public netmask used for the sec storage vm',
  `guest_mac_address` varchar(17) NOT NULL COMMENT 'mac address of the guest facing network card',
  `guest_ip_address` varchar(15) default NULL COMMENT 'guest ip address for the console proxy',
  `guest_netmask` varchar(15) default NULL COMMENT 'guest netmask used for the console proxy',
  `vlan_db_id` bigint(20) unsigned default NULL COMMENT 'Foreign key into vlan id table',
  `vlan_id` varchar(255) default NULL COMMENT 'optional VLAN ID for sec storage vm that can be used',
  `ram_size` int(10) unsigned NOT NULL default '512' COMMENT 'memory to use in mb',
  `guid` varchar(255) NOT NULL COMMENT 'copied from guid of secondary storage host',
  `nfs_share` varchar(255) NOT NULL COMMENT 'server and path exported by the nfs server ',
  `last_update` datetime default NULL COMMENT 'Last session update time',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `public_mac_address` (`public_mac_address`),
  UNIQUE KEY `guest_mac_address` (`guest_mac_address`),
  UNIQUE KEY `public_ip_address` (`public_ip_address`),
  UNIQUE KEY `guest_ip_address` (`guest_ip_address`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `secondary_storage_vm`
--

LOCK TABLES `secondary_storage_vm` WRITE;
/*!40000 ALTER TABLE `secondary_storage_vm` DISABLE KEYS */;
INSERT INTO `secondary_storage_vm` VALUES (1,'172.16.36.1','192.168.110.253','192.168.110.254','foo.com','06:81:de:49:00:01','172.16.36.42','255.255.252.0','06:01:c9:6e:00:02',NULL,'255.255.0.0',1,'509',256,'nfs://192.168.110.232/export/home/chandan/eagle/secondary21x','nfs://192.168.110.232/export/home/chandan/eagle/secondary21x',NULL);
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
  `domain_id` bigint(20) unsigned default NULL,
  `account_id` bigint(20) unsigned default NULL,
  PRIMARY KEY  (`id`)
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
  `ip_address` varchar(15) NOT NULL,
  `instance_id` bigint(20) unsigned NOT NULL,
  PRIMARY KEY  (`id`)
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
INSERT INTO `sequence` VALUES ('private_mac_address_seq',1),('public_mac_address_seq',1),('storage_pool_seq',201),('vm_instance_seq',9),('vm_template_seq',209);
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
INSERT INTO `service_offering` VALUES (1,1,500,512,200,10,0,'Virtualized',NULL),(2,1,1000,1024,200,10,0,'Virtualized',NULL),(3,1,500,512,200,10,0,'DirectSingle',NULL),(4,1,1000,1024,200,10,0,'DirectSingle',NULL),(8,1,0,128,0,0,0,'Virtualized',NULL),(9,1,0,256,0,0,0,'Virtualized',NULL),(10,1,0,1024,0,0,0,'Virtualized',NULL),(11,1,100,128,200,10,0,'Virtualized',NULL);
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
  KEY `i_snapshot_policy__volume_id` (`volume_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `snapshot_policy`
--

LOCK TABLES `snapshot_policy` WRITE;
/*!40000 ALTER TABLE `snapshot_policy` DISABLE KEYS */;
INSERT INTO `snapshot_policy` VALUES (1,0,'00','GMT',4,0,1),(2,10,'03','America/Los_Angeles',0,8,1),(3,10,'02:02','America/Los_Angeles',1,8,1),(4,10,'01:01:1','America/Los_Angeles',2,8,1),(5,10,'00:00:1','America/Los_Angeles',3,8,1),(6,4,'00','Etc/GMT+12',0,8,1),(7,4,'00:00','Etc/GMT+12',1,8,1),(8,4,'00:00:1','Etc/GMT+12',2,8,1),(9,4,'00:00:1','Etc/GMT+12',3,8,1);
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
INSERT INTO `snapshot_policy_ref` VALUES (2,4,1),(3,5,1),(1,6,1),(5,8,1),(6,9,1),(4,10,1),(7,10,2);
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
  UNIQUE KEY `volume_id` (`volume_id`,`policy_id`),
  KEY `i_snapshot_schedule__volume_id` (`volume_id`),
  KEY `i_snapshot_schedule__policy_id` (`policy_id`),
  KEY `i_snapshot_schedule__async_job_id` (`async_job_id`),
  KEY `i_snapshot_schedule__snapshot_id` (`snapshot_id`),
  KEY `i_snapshot_schedule__scheduled_timestamp` (`scheduled_timestamp`),
  CONSTRAINT `fk__snapshot_schedule_async_job_id` FOREIGN KEY (`async_job_id`) REFERENCES `async_job` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk__snapshot_schedule_policy_id` FOREIGN KEY (`policy_id`) REFERENCES `snapshot_policy` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk__snapshot_schedule_snapshot_id` FOREIGN KEY (`snapshot_id`) REFERENCES `snapshots` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk__snapshot_schedule_volume_id` FOREIGN KEY (`volume_id`) REFERENCES `volumes` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `snapshot_schedule`
--

LOCK TABLES `snapshot_schedule` WRITE;
/*!40000 ALTER TABLE `snapshot_schedule` DISABLE KEYS */;
INSERT INTO `snapshot_schedule` VALUES (20,10,2,'2011-04-23 05:03:00',NULL,NULL),(21,10,3,'2011-04-23 09:02:00',NULL,NULL),(22,10,4,'2011-04-24 08:01:00',NULL,NULL),(23,10,5,'2011-05-01 07:00:00',NULL,NULL),(24,4,6,'2011-04-23 05:00:00',NULL,NULL),(25,4,7,'2011-04-23 12:00:00',NULL,NULL),(26,4,8,'2011-04-24 12:00:00',NULL,NULL),(27,4,9,'2011-05-01 12:00:00',NULL,NULL);
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
  PRIMARY KEY  (`id`),
  KEY `i_snapshots__account_id` (`account_id`),
  KEY `i_snapshots__volume_id` (`volume_id`),
  KEY `i_snapshots__removed` (`removed`),
  KEY `i_snapshots__name` (`name`),
  KEY `i_snapshots__snapshot_type` (`snapshot_type`),
  KEY `i_snapshots__prev_snap_id` (`prev_snap_id`),
  CONSTRAINT `fk_snapshots__account_id` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `snapshots`
--

LOCK TABLES `snapshots` WRITE;
/*!40000 ALTER TABLE `snapshots` DISABLE KEYS */;
INSERT INTO `snapshots` VALUES (1,2,6,'BackedUp','92f06300-f969-4163-b82a-294da346f6b0','i-2-5-VM_i-2-5-VM-ROOT_20110423023346',0,'MANUAL','2011-04-23 02:33:46',NULL,'12638721-b47a-4f3b-a2af-c275ccac8970',0),(2,2,4,'BackedUp','aadfca92-d81f-494b-b1a7-1aff4d853388','Admin-VM-1_i-2-3-VM-ROOT_20110423023348',0,'MANUAL','2011-04-23 02:33:48',NULL,'9da76023-17ce-4ca8-ae35-ef5dea6405b3',0),(3,2,5,'BackedUp','b4d6a5e7-5363-4648-8aab-6933124cf598','Admin-VM-1_i-2-3-VM-DATA_20110423023351',0,'MANUAL','2011-04-23 02:33:51',NULL,'374e8602-6864-4d9c-bebb-20ee50babc06',0),(4,3,10,'BackedUp','dbdd089b-e374-430d-a453-5c1d4a8f4a3e','Nimbus-VM-2_i-3-8-VM-ROOT_20110423024915',0,'MANUAL','2011-04-23 02:49:15',NULL,'f763298f-34f0-4d02-beb6-6bae9afd980b',0),(5,3,8,'BackedUp','2715c91f-426a-4930-9bcc-df59ba53af3e','Nimbus-VM-1_i-3-6-VM-ROOT_20110423024919',0,'MANUAL','2011-04-23 02:49:19',NULL,'00e8557d-18ee-4e48-9387-8dce6384d99a',0),(6,3,9,'BackedUp','89c27933-2000-4c4c-bd14-67d3d7b28d20','Nimbus-VM-1_i-3-6-VM-DATA_20110423024924',0,'MANUAL','2011-04-23 02:49:24',NULL,'d02ce704-8d06-4cd2-aa00-9f2371985b67',0),(7,3,10,'BackedUp','701efca2-8037-4cec-916e-836b3d802de8','Nimbus-VM-2_i-3-8-VM-ROOT_20110423040323',1,'RECURRING','2011-04-23 04:03:23',NULL,'7d155713-66fa-4525-be9c-4edd752616f2',4);
/*!40000 ALTER TABLE `snapshots` ENABLE KEYS */;
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
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`),
  UNIQUE KEY `uuid` (`uuid`),
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
INSERT INTO `storage_pool` VALUES (200,'xenPrimary163','f7c994e5-d205-3f85-bba7-e1258d227254','NetworkFilesystem',2049,1,1,1,1664234455040,1930519904256,'192.168.110.232','/export/home/chandan/eagle/primary21x','2011-04-23 02:18:52',NULL,NULL);
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
  KEY `i_storage_pool_details__name__value` (`name`,`value`),
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
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `storage_pool_host_ref`
--

LOCK TABLES `storage_pool_host_ref` WRITE;
/*!40000 ALTER TABLE `storage_pool_host_ref` DISABLE KEYS */;
INSERT INTO `storage_pool_host_ref` VALUES (1,2,200,'2011-04-23 02:18:54',NULL,'/mnt//f7c994e5-d205-3f85-bba7-e1258d227254');
/*!40000 ALTER TABLE `storage_pool_host_ref` ENABLE KEYS */;
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
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `sync_queue`
--

LOCK TABLES `sync_queue` WRITE;
/*!40000 ALTER TABLE `sync_queue` DISABLE KEYS */;
INSERT INTO `sync_queue` VALUES (1,'Router',4,6603285398259,3,NULL,'2011-04-23 02:27:54','2011-04-23 04:05:18'),(2,'Volume',6,6603285398259,3,'2011-04-23 03:00:47','2011-04-23 02:33:46','2011-04-23 03:00:47'),(3,'Volume',4,6603285398259,3,'2011-04-23 03:00:55','2011-04-23 02:33:48','2011-04-23 03:00:55'),(4,'Volume',5,6603285398259,2,NULL,'2011-04-23 02:33:51','2011-04-23 02:37:53'),(5,'Router',7,6603285398259,3,NULL,'2011-04-23 02:41:36','2011-04-23 04:05:05'),(6,'Volume',10,6603285398259,4,NULL,'2011-04-23 02:49:15','2011-04-23 04:03:45'),(7,'Volume',8,6603285398259,3,NULL,'2011-04-23 02:49:19','2011-04-23 04:07:42'),(8,'Volume',9,6603285398259,3,NULL,'2011-04-23 02:49:24','2011-04-23 02:59:44'),(9,'SystemVm',1,6603285398259,1,NULL,'2011-04-23 04:05:03','2011-04-23 04:05:34'),(10,'ConsoleProxy',2,6603285398259,2,NULL,'2011-04-23 04:06:34','2011-04-23 04:08:16');
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
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `sync_queue_item`
--

LOCK TABLES `sync_queue_item` WRITE;
/*!40000 ALTER TABLE `sync_queue_item` DISABLE KEYS */;
INSERT INTO `sync_queue_item` VALUES (20,2,'AsyncJob',24,6603285398259,3,'2011-04-23 03:00:47'),(21,3,'AsyncJob',25,6603285398259,3,'2011-04-23 03:00:55'),(23,3,'AsyncJob',27,NULL,NULL,'2011-04-23 04:03:23');
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
  CONSTRAINT `fk_template_host_ref__template_id` FOREIGN KEY (`template_id`) REFERENCES `vm_template` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `template_host_ref`
--

LOCK TABLES `template_host_ref` WRITE;
/*!40000 ALTER TABLE `template_host_ref` DISABLE KEYS */;
INSERT INTO `template_host_ref` VALUES (1,1,NULL,1,'2011-04-23 02:18:41','2011-04-23 03:03:31',NULL,100,2147483648,'DOWNLOADED',NULL,NULL,'template/tmpl/1/1//3fa5d745-00c2-320c-b9bb-6c1c6dfcf6bc.vhd','http://download.cloud.com/releases/2.0.0RC5/systemvm.vhd.bz2',0,0),(2,1,NULL,2,'2011-04-23 02:18:42','2011-04-23 03:03:31',NULL,100,8589934592,'DOWNLOADED','',NULL,'template/tmpl/1/2//b82efd5a-068f-35ff-9db7-1ea249dc563a.vhd','http://download.cloud.com/templates/builtin/f59f18fb-ae94-4f97-afd2-f84755767aca.vhd.bz2',0,0),(3,2,NULL,200,'2011-04-23 02:18:51','2011-04-23 02:18:51',NULL,100,66023424,'DOWNLOADED',NULL,NULL,'iso/users/2/xs-tools',NULL,0,0),(4,1,NULL,201,'2011-04-23 02:35:48','2011-04-23 03:03:32','gQYByW4AAgBL4A2jAAAAAQ==',100,2097152000,'DOWNLOADED','Install completed successfully at 4/22/11 7:47 PM','/mnt/SecStorage/3c02a379/template/tmpl/2/201/dnld4651971991873302998tmp_','template/tmpl/2/201//604f81bf-094a-3d81-96de-5a3bf2ce3298.vhd','http://download.cloud.com/releases/2.2.0/systemvm.vhd.bz2',0,0),(5,1,NULL,204,'2011-04-23 02:37:52','2011-04-23 03:03:33',NULL,100,1073741824,'DOWNLOADED',NULL,NULL,'template/tmpl/2/204//d33d191e-718d-479b-9dcc-20ac101450db.vhd',NULL,0,0),(6,1,NULL,202,'2011-04-23 02:40:33','2011-04-23 03:03:33',NULL,100,8589934592,'DOWNLOADED',NULL,NULL,'template/tmpl/2/202//71039055-6822-4098-96cd-f99660381fbc.vhd',NULL,0,0),(7,1,NULL,203,'2011-04-23 02:41:21','2011-04-23 03:03:33',NULL,100,8589934592,'DOWNLOADED',NULL,NULL,'template/tmpl/2/203//6da1b720-82e6-4b23-81cb-419a40a65c51.vhd',NULL,0,0),(8,1,NULL,207,'2011-04-23 02:57:52','2011-04-23 03:03:33',NULL,100,1073741824,'DOWNLOADED',NULL,NULL,'template/tmpl/3/207//c0df1d9d-fc71-4499-980c-275fd18993f2.vhd',NULL,0,0),(9,1,NULL,205,'2011-04-23 02:58:50','2011-04-23 03:03:33',NULL,100,8589934592,'DOWNLOADED',NULL,NULL,'template/tmpl/3/205//a73ac6a8-74d5-4ce5-9559-68ccb808426c.vhd',NULL,0,0),(10,1,NULL,206,'2011-04-23 02:59:20','2011-04-23 03:03:33',NULL,100,8589934592,'DOWNLOADED',NULL,NULL,'template/tmpl/3/206//e88cd318-e524-4a51-8b94-06f68e7c59e4.vhd',NULL,0,0),(11,1,NULL,208,'2011-04-23 03:04:30','2011-04-23 03:09:16','gQYByW4AAgBL4A2jAAAAAg==',100,4393723904,'DOWNLOADED','Install completed successfully at 4/22/11 8:09 PM','/mnt/SecStorage/3c02a379/template/tmpl/2/208/dnld3057435700407010179tmp_','template/tmpl/2/208//208-2-155c8bda-c3ab-34b3-93ac-58cb10e6e57f.iso','http://nfs1.lab.vmops.com/isos_64bit/CentOS-5.5-x86_64-bin-DVDs/CentOS-5.5-x86_64-bin-DVD-1of2.iso',0,0);
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
  UNIQUE KEY `i_template_spool_ref__template_id__pool_id` (`template_id`,`pool_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `template_spool_ref`
--

LOCK TABLES `template_spool_ref` WRITE;
/*!40000 ALTER TABLE `template_spool_ref` DISABLE KEYS */;
INSERT INTO `template_spool_ref` VALUES (1,200,1,'2011-04-23 02:19:22',NULL,NULL,0,'DOWNLOADED',NULL,'9409c3b2-2cf6-4520-b11d-5428781bcb6e','9409c3b2-2cf6-4520-b11d-5428781bcb6e',2101252608,0),(2,200,2,'2011-04-23 02:24:07',NULL,NULL,0,'DOWNLOADED',NULL,'e9322b6a-1bd8-46d2-9a87-c9715cd5e78e','e9322b6a-1bd8-46d2-9a87-c9715cd5e78e',1708331520,0);
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
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `template_zone_ref`
--

LOCK TABLES `template_zone_ref` WRITE;
/*!40000 ALTER TABLE `template_zone_ref` DISABLE KEYS */;
INSERT INTO `template_zone_ref` VALUES (1,1,1,'2011-04-23 02:18:42','2011-04-23 02:18:42',NULL),(2,1,2,'2011-04-23 02:18:42','2011-04-23 02:18:42',NULL),(3,1,201,'2011-04-23 02:35:48','2011-04-23 02:35:48',NULL),(4,1,204,'2011-04-23 02:37:52','2011-04-23 02:37:52',NULL),(5,1,202,'2011-04-23 02:40:33','2011-04-23 02:40:33',NULL),(6,1,203,'2011-04-23 02:41:21','2011-04-23 02:41:21',NULL),(7,1,207,'2011-04-23 02:57:52','2011-04-23 02:57:52',NULL),(8,1,205,'2011-04-23 02:58:50','2011-04-23 02:58:50',NULL),(9,1,206,'2011-04-23 02:59:20','2011-04-23 02:59:20',NULL),(10,1,208,'2011-04-23 03:04:30','2011-04-23 03:04:30',NULL);
/*!40000 ALTER TABLE `template_zone_ref` ENABLE KEYS */;
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
INSERT INTO `user` VALUES (1,'system','',1,'system','cloud',NULL,'enabled',NULL,NULL,'2011-04-22 22:12:56',NULL,NULL),(2,'admin','5f4dcc3b5aa765d61d8327deb882cf99',2,'admin','cloud','','enabled',NULL,NULL,'2011-04-22 22:12:56',NULL,NULL),(3,'nimbus','5f4dcc3b5aa765d61d8327deb882cf99',3,'nimbus','nimbuslast','nimbus@gmail.com','enabled',NULL,NULL,'2011-04-23 02:34:39',NULL,'America/Los_Angeles');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_ip_address`
--

DROP TABLE IF EXISTS `user_ip_address`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `user_ip_address` (
  `account_id` bigint(20) unsigned default NULL,
  `domain_id` bigint(20) unsigned default NULL,
  `public_ip_address` varchar(15) NOT NULL,
  `data_center_id` bigint(20) unsigned NOT NULL COMMENT 'zone that it belongs to',
  `source_nat` int(1) unsigned NOT NULL default '0',
  `allocated` datetime default NULL COMMENT 'Date this ip was allocated to someone',
  `vlan_db_id` bigint(20) unsigned NOT NULL,
  PRIMARY KEY  (`public_ip_address`),
  UNIQUE KEY `public_ip_address` (`public_ip_address`),
  KEY `i_user_ip_address__account_id` (`account_id`),
  KEY `i_user_ip_address__vlan_db_id` (`vlan_db_id`),
  KEY `i_user_ip_address__data_center_id` (`data_center_id`),
  KEY `i_user_ip_address__source_nat` (`source_nat`),
  KEY `i_user_ip_address__allocated` (`allocated`),
  KEY `i_user_ip_address__public_ip_address` (`public_ip_address`),
  CONSTRAINT `fk_user_ip_address__account_id` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`),
  CONSTRAINT `fk_user_ip_address__vlan_db_id` FOREIGN KEY (`vlan_db_id`) REFERENCES `vlan` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `user_ip_address`
--

LOCK TABLES `user_ip_address` WRITE;
/*!40000 ALTER TABLE `user_ip_address` DISABLE KEYS */;
INSERT INTO `user_ip_address` VALUES (NULL,NULL,'172.16.36.10',1,0,NULL,1),(NULL,NULL,'172.16.36.11',1,0,NULL,1),(NULL,NULL,'172.16.36.12',1,0,NULL,1),(NULL,NULL,'172.16.36.13',1,0,NULL,1),(NULL,NULL,'172.16.36.14',1,0,NULL,1),(NULL,NULL,'172.16.36.15',1,0,NULL,1),(3,2,'172.16.36.16',1,1,'2011-04-23 02:38:51',1),(NULL,NULL,'172.16.36.17',1,0,NULL,1),(NULL,NULL,'172.16.36.18',1,0,NULL,1),(NULL,NULL,'172.16.36.19',1,0,NULL,1),(NULL,NULL,'172.16.36.2',1,0,NULL,1),(NULL,NULL,'172.16.36.20',1,0,NULL,1),(NULL,NULL,'172.16.36.21',1,0,NULL,1),(NULL,NULL,'172.16.36.22',1,0,NULL,1),(NULL,NULL,'172.16.36.23',1,0,NULL,1),(NULL,NULL,'172.16.36.24',1,0,NULL,1),(NULL,NULL,'172.16.36.25',1,0,NULL,1),(NULL,NULL,'172.16.36.26',1,0,NULL,1),(1,1,'172.16.36.27',1,1,'2011-04-23 02:19:22',1),(NULL,NULL,'172.16.36.28',1,0,NULL,1),(NULL,NULL,'172.16.36.29',1,0,NULL,1),(NULL,NULL,'172.16.36.3',1,0,NULL,1),(NULL,NULL,'172.16.36.30',1,0,NULL,1),(NULL,NULL,'172.16.36.31',1,0,NULL,1),(NULL,NULL,'172.16.36.32',1,0,NULL,1),(NULL,NULL,'172.16.36.33',1,0,NULL,1),(NULL,NULL,'172.16.36.34',1,0,NULL,1),(NULL,NULL,'172.16.36.35',1,0,NULL,1),(NULL,NULL,'172.16.36.36',1,0,NULL,1),(NULL,NULL,'172.16.36.37',1,0,NULL,1),(NULL,NULL,'172.16.36.38',1,0,NULL,1),(NULL,NULL,'172.16.36.39',1,0,NULL,1),(NULL,NULL,'172.16.36.4',1,0,NULL,1),(NULL,NULL,'172.16.36.40',1,0,NULL,1),(NULL,NULL,'172.16.36.41',1,0,NULL,1),(1,1,'172.16.36.42',1,1,'2011-04-23 02:19:21',1),(NULL,NULL,'172.16.36.43',1,0,NULL,1),(NULL,NULL,'172.16.36.44',1,0,NULL,1),(NULL,NULL,'172.16.36.45',1,0,NULL,1),(NULL,NULL,'172.16.36.46',1,0,NULL,1),(NULL,NULL,'172.16.36.47',1,0,NULL,1),(NULL,NULL,'172.16.36.48',1,0,NULL,1),(NULL,NULL,'172.16.36.49',1,0,NULL,1),(NULL,NULL,'172.16.36.5',1,0,NULL,1),(NULL,NULL,'172.16.36.50',1,0,NULL,1),(NULL,NULL,'172.16.36.51',1,0,NULL,1),(NULL,NULL,'172.16.36.52',1,0,NULL,1),(2,1,'172.16.36.53',1,0,'2011-04-23 02:26:11',1),(NULL,NULL,'172.16.36.54',1,0,NULL,1),(NULL,NULL,'172.16.36.55',1,0,NULL,1),(NULL,NULL,'172.16.36.56',1,0,NULL,1),(NULL,NULL,'172.16.36.57',1,0,NULL,1),(NULL,NULL,'172.16.36.58',1,0,NULL,1),(2,1,'172.16.36.59',1,1,'2011-04-23 02:24:06',1),(NULL,NULL,'172.16.36.6',1,0,NULL,1),(NULL,NULL,'172.16.36.60',1,0,NULL,1),(NULL,NULL,'172.16.36.61',1,0,NULL,1),(NULL,NULL,'172.16.36.62',1,0,NULL,1),(NULL,NULL,'172.16.36.63',1,0,NULL,1),(NULL,NULL,'172.16.36.7',1,0,NULL,1),(NULL,NULL,'172.16.36.8',1,0,NULL,1),(NULL,NULL,'172.16.36.9',1,0,NULL,1);
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
  `net_bytes_received` bigint(20) unsigned NOT NULL default '0',
  `net_bytes_sent` bigint(20) unsigned NOT NULL default '0',
  `current_bytes_received` bigint(20) unsigned NOT NULL default '0',
  `current_bytes_sent` bigint(20) unsigned NOT NULL default '0',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`),
  KEY `i_user_statistics__account_id` (`account_id`),
  KEY `i_user_statistics__account_id_data_center_id` (`account_id`,`data_center_id`),
  CONSTRAINT `fk_user_statistics__account_id` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `user_statistics`
--

LOCK TABLES `user_statistics` WRITE;
/*!40000 ALTER TABLE `user_statistics` DISABLE KEYS */;
INSERT INTO `user_statistics` VALUES (1,1,2,23388236,538125,0,0),(2,1,3,23388401,554554,0,0);
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
  `domain_router_id` bigint(20) unsigned default NULL COMMENT 'router id',
  `service_offering_id` bigint(20) unsigned NOT NULL COMMENT 'service offering id',
  `vnet` varchar(18) default NULL COMMENT 'vnet',
  `dc_vlan` varchar(18) default NULL COMMENT 'zone vlan',
  `account_id` bigint(20) unsigned NOT NULL COMMENT 'user id of owner',
  `domain_id` bigint(20) unsigned NOT NULL,
  `guest_ip_address` varchar(15) default NULL COMMENT 'ip address within the guest network',
  `guest_mac_address` varchar(17) default NULL COMMENT 'mac address within the guest network',
  `guest_netmask` varchar(15) default NULL COMMENT 'netmask within the guest network',
  `external_ip_address` varchar(15) default NULL COMMENT 'ip address within the external network',
  `external_mac_address` varchar(17) default NULL COMMENT 'mac address within the external network',
  `external_vlan_db_id` bigint(20) unsigned default NULL COMMENT 'foreign key into vlan table',
  `user_data` varchar(2048) default NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`),
  KEY `i_user_vm__domain_router_id` (`domain_router_id`),
  KEY `i_user_vm__service_offering_id` (`service_offering_id`),
  KEY `i_user_vm__account_id` (`account_id`),
  KEY `i_user_vm__external_ip_address` (`external_ip_address`),
  KEY `i_user_vm__external_vlan_db_id` (`external_vlan_db_id`),
  CONSTRAINT `fk_user_vm__account_id` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`),
  CONSTRAINT `fk_user_vm__domain_router_id` FOREIGN KEY (`domain_router_id`) REFERENCES `domain_router` (`id`),
  CONSTRAINT `fk_user_vm__external_ip_address` FOREIGN KEY (`external_ip_address`) REFERENCES `user_ip_address` (`public_ip_address`),
  CONSTRAINT `fk_user_vm__external_vlan_db_id` FOREIGN KEY (`external_vlan_db_id`) REFERENCES `vlan` (`id`),
  CONSTRAINT `fk_user_vm__id` FOREIGN KEY (`id`) REFERENCES `vm_instance` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_user_vm__service_offering_id` FOREIGN KEY (`service_offering_id`) REFERENCES `service_offering` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `user_vm`
--

LOCK TABLES `user_vm` WRITE;
/*!40000 ALTER TABLE `user_vm` DISABLE KEYS */;
INSERT INTO `user_vm` VALUES (3,4,11,'1240',NULL,2,1,'10.1.1.2','02:01:00:00:01:02','255.255.255.0',NULL,NULL,NULL,NULL),(5,4,11,'1240',NULL,2,1,'10.1.1.3','02:00:04:d8:01:03','255.255.255.0',NULL,NULL,NULL,NULL),(6,7,11,'1241',NULL,3,2,'10.1.1.2','02:01:00:00:01:02','255.255.255.0',NULL,NULL,NULL,NULL),(8,7,11,'1241',NULL,3,2,'10.1.1.3','02:00:04:d9:01:03','255.255.255.0',NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `user_vm` ENABLE KEYS */;
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
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `vlan`
--

LOCK TABLES `vlan` WRITE;
/*!40000 ALTER TABLE `vlan` DISABLE KEYS */;
INSERT INTO `vlan` VALUES (1,'509','172.16.36.1','255.255.252.0','172.16.36.2-172.16.36.63','VirtualNetwork',1);
/*!40000 ALTER TABLE `vlan` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vm_disk`
--

DROP TABLE IF EXISTS `vm_disk`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `vm_disk` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `instance_id` bigint(20) unsigned NOT NULL,
  `disk_offering_id` bigint(20) unsigned NOT NULL,
  `removed` datetime default NULL COMMENT 'date removed',
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `vm_disk`
--

LOCK TABLES `vm_disk` WRITE;
/*!40000 ALTER TABLE `vm_disk` DISABLE KEYS */;
/*!40000 ALTER TABLE `vm_disk` ENABLE KEYS */;
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
  `display_name` varchar(255) default NULL,
  `group` varchar(255) default NULL,
  `instance_name` varchar(255) NOT NULL COMMENT 'name of the vm instance running on the hosts',
  `state` varchar(32) NOT NULL,
  `vm_template_id` bigint(20) unsigned default NULL,
  `iso_id` bigint(20) unsigned default NULL,
  `guest_os_id` bigint(20) unsigned NOT NULL,
  `private_mac_address` varchar(17) default NULL,
  `private_ip_address` varchar(15) default NULL,
  `private_netmask` varchar(15) default NULL,
  `pod_id` bigint(20) unsigned default NULL,
  `storage_ip` varchar(15) default NULL,
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
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`),
  KEY `i_vm_instance__removed` (`removed`),
  KEY `i_vm_instance__type` (`type`),
  KEY `i_vm_instance__pod_id` (`pod_id`),
  KEY `i_vm_instance__update_time` (`update_time`),
  KEY `i_vm_instance__update_count` (`update_count`),
  KEY `i_vm_instance__state` (`state`),
  KEY `i_vm_instance__data_center_id` (`data_center_id`),
  KEY `i_vm_instance__host_id` (`host_id`),
  KEY `i_vm_instance__last_host_id` (`last_host_id`),
  KEY `i_vm_instance__template_id` (`vm_template_id`),
  CONSTRAINT `fk_vm_instance__host_id` FOREIGN KEY (`host_id`) REFERENCES `host` (`id`),
  CONSTRAINT `fk_vm_instance__template_id` FOREIGN KEY (`vm_template_id`) REFERENCES `vm_template` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `vm_instance`
--

LOCK TABLES `vm_instance` WRITE;
/*!40000 ALTER TABLE `vm_instance` DISABLE KEYS */;
INSERT INTO `vm_instance` VALUES (1,'s-1-VM',NULL,NULL,'s-1-VM','Stopped',1,NULL,12,'06:01:de:49:00:01',NULL,'255.255.255.0',1,NULL,1,NULL,2,NULL,NULL,'7ff68998bc7ee99b',1,0,7,'2011-04-23 04:05:33','2011-04-23 02:19:21',NULL,'SecondaryStorageVm'),(2,'v-2-VM',NULL,NULL,'v-2-VM','Stopped',1,NULL,12,'06:01:ea:ec:00:03',NULL,'255.255.255.0',1,NULL,1,NULL,2,NULL,NULL,'8785193a4debf197',1,0,12,'2011-04-23 04:08:16','2011-04-23 02:19:22',NULL,'ConsoleProxy'),(3,'i-2-3-VM','Admin-VM-1','Admin-VM-1','i-2-3-VM-1240','Running',2,NULL,12,'02:01:00:00:01:02','10.1.1.2','255.255.255.0',1,NULL,1,2,2,2,'2011-04-23 02:28:16','ec8d9c3bfc544675',0,0,5,'2011-04-23 04:07:52','2011-04-23 02:24:06',NULL,'User'),(4,'r-4-VM',NULL,NULL,'r-4-VM-1240','Stopped',1,NULL,12,'06:01:23:a6:00:05',NULL,'255.255.0.0',1,NULL,1,NULL,2,NULL,NULL,'fb65ac8590c59754',1,0,7,'2011-04-23 04:05:18','2011-04-23 02:24:07',NULL,'DomainRouter'),(5,'i-2-5-VM','i-2-5-VM',NULL,'i-2-5-VM-1240','Running',2,NULL,12,'02:00:04:d8:01:03','10.1.1.3','255.255.255.0',1,NULL,1,2,2,2,'2011-04-23 02:28:15','b2c6099031774a53',0,0,5,'2011-04-23 04:07:52','2011-04-23 02:26:35',NULL,'User'),(6,'i-3-6-VM','Nimbus-VM-1','Nimbus-VM-1','i-3-6-VM-1241','Running',2,NULL,12,'02:01:00:00:01:02','10.1.1.2','255.255.255.0',1,NULL,1,2,2,2,'2011-04-23 02:41:55','c573df4b92f09e31',0,0,5,'2011-04-23 04:07:52','2011-04-23 02:38:51',NULL,'User'),(7,'r-7-VM',NULL,NULL,'r-7-VM-1241','Stopped',1,NULL,12,'06:01:29:75:00:07',NULL,'255.255.0.0',1,NULL,1,NULL,2,NULL,NULL,'3f55101c5960304f',1,0,7,'2011-04-23 04:05:05','2011-04-23 02:38:51',NULL,'DomainRouter'),(8,'i-3-8-VM','Nimbus-VM-2','Nimbus-VM-2','i-3-8-VM-1241','Running',2,NULL,12,'02:00:04:d9:01:03','10.1.1.3','255.255.255.0',1,NULL,1,2,2,2,'2011-04-23 02:41:55','fcd614f3f1f1c324',0,0,5,'2011-04-23 04:07:52','2011-04-23 02:39:08',NULL,'User');
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
INSERT INTO `vm_template` VALUES (1,'routing','SystemVM Template',0,0,'ext3',0,64,'http://download.cloud.com/releases/2.0.0RC5/systemvm.vhd.bz2','VHD','2011-04-22 22:11:51',NULL,1,'31cd7ce94fe68c973d5dc37c3349d02e','SystemVM Template',0,12,1,0,1),(2,'centos53-x86_64','CentOS 5.3(x86_64) no GUI',1,1,'ext3',0,64,'http://download.cloud.com/templates/builtin/f59f18fb-ae94-4f97-afd2-f84755767aca.vhd.bz2','VHD','2011-04-22 22:11:51',NULL,1,'b63d854a9560c013142567bbae8d98cf','CentOS 5.3(x86_64) no GUI',0,12,1,0,1),(200,'xs-tools.iso','xs-tools.iso',1,1,'cdfs',1,64,'/opt/xensource/packages/iso/xs-tools-5.5.0.iso','ISO','2011-04-23 02:18:51',NULL,1,NULL,'xen-pv-drv-iso',0,1,0,0,0),(201,'201-2-986b258a-89f7-3347-901e-db099b26809f','systemvm-xenserver-2.2.4',0,0,'ext3',1,64,'http://download.cloud.com/releases/2.2.0/systemvm.vhd.bz2','VHD','2011-04-23 02:35:48',NULL,2,NULL,'systemvm-xenserver-2.2.4',0,14,1,0,0),(202,'71039055-6822-4098-96cd-f99660381fbc','TemplateFromSnapshot-1',0,0,'ext3',0,64,NULL,'VHD','2011-04-23 02:36:35',NULL,2,NULL,'TemplateFromSnapshot-1',0,12,1,0,0),(203,'6da1b720-82e6-4b23-81cb-419a40a65c51','TemplateFromSnapshot-2',0,0,'ext3',0,64,NULL,'VHD','2011-04-23 02:37:13',NULL,2,NULL,'TemplateFromSnapshot-2',0,12,1,0,0),(204,'d33d191e-718d-479b-9dcc-20ac101450db','TemplateFromSnapshot-3-Data',0,0,'Unknown',1,64,NULL,'VHD','2011-04-23 02:37:47',NULL,2,NULL,'TemplateFromSnapshot-3-Data',0,12,1,0,0),(205,'a73ac6a8-74d5-4ce5-9559-68ccb808426c','TemplateFromSnapshot-5',0,0,'ext3',0,64,NULL,'VHD','2011-04-23 02:55:38',NULL,3,NULL,'TemplateFromSnapshot-5',0,12,1,0,0),(206,'e88cd318-e524-4a51-8b94-06f68e7c59e4','TemplateFromSnapshot-6',0,0,'ext3',0,64,NULL,'VHD','2011-04-23 02:55:48',NULL,3,NULL,'TemplateFromSnapshot-6',0,12,1,0,0),(207,'c0df1d9d-fc71-4499-980c-275fd18993f2','TemplateFromSnapshot-7-Data',0,0,'Unknown',1,64,NULL,'VHD','2011-04-23 02:57:48',NULL,3,NULL,'TemplateFromSnapshot-7-Data',0,60,1,0,0),(208,'208-2-155c8bda-c3ab-34b3-93ac-58cb10e6e57f','CentOS-5-5-ISO-Download',0,0,'cdfs',1,64,'http://nfs1.lab.vmops.com/isos_64bit/CentOS-5.5-x86_64-bin-DVDs/CentOS-5.5-x86_64-bin-DVD-1of2.iso','ISO','2011-04-23 03:04:30',NULL,2,NULL,'CentOS-5-5-ISO-Download',0,60,1,0,0);
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
  `volume_type` varchar(64) default NULL COMMENT 'root, swap or data',
  `resource_type` varchar(64) default NULL COMMENT 'pool-based or host-based',
  `pool_type` varchar(64) default NULL COMMENT 'type of the pool',
  `mirror_state` varchar(64) default NULL COMMENT 'not_mirrored, active or defunct',
  `mirror_vol` bigint(20) unsigned default NULL COMMENT 'the other half of the mirrored set if mirrored',
  `disk_offering_id` bigint(20) unsigned NOT NULL COMMENT 'can be null for system VMs',
  `template_id` bigint(20) unsigned default NULL COMMENT 'fk to vm_template.id',
  `first_snapshot_backup_uuid` varchar(255) default NULL COMMENT 'The first snapshot that was ever taken for this volume',
  `recreatable` tinyint(1) unsigned NOT NULL default '0' COMMENT 'Is this volume recreatable?',
  `destroyed` tinyint(1) default NULL COMMENT 'indicates whether the volume was destroyed by the user or not',
  `created` datetime default NULL COMMENT 'Date Created',
  `updated` datetime default NULL COMMENT 'Date updated for attach/detach',
  `removed` datetime default NULL COMMENT 'Date removed.  not null if removed',
  `status` varchar(32) default NULL COMMENT 'Async API volume creation status',
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
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `volumes`
--

LOCK TABLES `volumes` WRITE;
/*!40000 ALTER TABLE `volumes` DISABLE KEYS */;
INSERT INTO `volumes` VALUES (1,1,1,200,1,0,'s-1-VM-ROOT',2147483648,'/export/home/chandan/eagle/primary21x','94513471-b868-40fd-9480-adbabcbb37cc',1,1,NULL,NULL,'ROOT','STORAGE_POOL','NetworkFilesystem','NOT_MIRRORED',NULL,9,1,NULL,1,0,'2011-04-23 02:19:22',NULL,NULL,'Created'),(2,1,1,200,2,0,'v-2-VM-ROOT',2147483648,'/export/home/chandan/eagle/primary21x','c847344f-1d90-4379-995e-84c251d4e7d9',1,1,NULL,NULL,'ROOT','STORAGE_POOL','NetworkFilesystem','NOT_MIRRORED',NULL,10,1,NULL,1,0,'2011-04-23 02:19:22',NULL,NULL,'Created'),(3,2,1,200,4,0,'r-4-VM-ROOT',2147483648,'/export/home/chandan/eagle/primary21x','7feaaebc-f62b-44d4-ba9c-b6145ad43d98',1,1,NULL,NULL,'ROOT','STORAGE_POOL','NetworkFilesystem','NOT_MIRRORED',NULL,8,1,NULL,1,0,'2011-04-23 02:24:07',NULL,NULL,'Created'),(4,2,1,200,3,0,'i-2-3-VM-ROOT',8589934592,'/export/home/chandan/eagle/primary21x','d91773e6-4b62-4621-92b8-ffbc9ed91b08',1,1,NULL,NULL,'ROOT','STORAGE_POOL','NetworkFilesystem','NOT_MIRRORED',NULL,11,2,'9da76023-17ce-4ca8-ae35-ef5dea6405b3',0,0,'2011-04-23 02:24:07',NULL,NULL,'Created'),(5,2,1,200,3,1,'i-2-3-VM-DATA',1073741824,'/export/home/chandan/eagle/primary21x','aa3167c5-65b2-42e3-a6a3-d4b294ec04f3',1,1,NULL,NULL,'DATADISK','STORAGE_POOL','NetworkFilesystem','NOT_MIRRORED',NULL,12,NULL,'374e8602-6864-4d9c-bebb-20ee50babc06',0,0,'2011-04-23 02:24:07',NULL,NULL,'Created'),(6,2,1,200,5,0,'i-2-5-VM-ROOT',8589934592,'/export/home/chandan/eagle/primary21x','3a52ac65-af80-4309-a9f5-9a5f44d44dcb',1,1,NULL,NULL,'ROOT','STORAGE_POOL','NetworkFilesystem','NOT_MIRRORED',NULL,11,2,'12638721-b47a-4f3b-a2af-c275ccac8970',0,0,'2011-04-23 02:26:35',NULL,NULL,'Created'),(7,3,2,200,7,0,'r-7-VM-ROOT',2147483648,'/export/home/chandan/eagle/primary21x','9d041fea-b0bd-4311-be6e-6d4608c72bbe',1,1,NULL,NULL,'ROOT','STORAGE_POOL','NetworkFilesystem','NOT_MIRRORED',NULL,8,1,NULL,1,0,'2011-04-23 02:38:51',NULL,NULL,'Created'),(8,3,2,200,6,0,'i-3-6-VM-ROOT',8589934592,'/export/home/chandan/eagle/primary21x','73e9ffb0-8357-4adf-be2e-1d9f8dca70df',1,1,NULL,NULL,'ROOT','STORAGE_POOL','NetworkFilesystem','NOT_MIRRORED',NULL,11,2,'00e8557d-18ee-4e48-9387-8dce6384d99a',0,0,'2011-04-23 02:38:55',NULL,NULL,'Created'),(9,3,2,200,6,1,'i-3-6-VM-DATA',1073741824,'/export/home/chandan/eagle/primary21x','990df865-c602-443c-9bb8-69a495695d84',1,1,NULL,NULL,'DATADISK','STORAGE_POOL','NetworkFilesystem','NOT_MIRRORED',NULL,12,NULL,'d02ce704-8d06-4cd2-aa00-9f2371985b67',0,0,'2011-04-23 02:38:55',NULL,NULL,'Created'),(10,3,2,200,8,0,'i-3-8-VM-ROOT',8589934592,'/export/home/chandan/eagle/primary21x','c56201e1-4803-4654-9ad6-f1add6c3f8d5',1,1,NULL,NULL,'ROOT','STORAGE_POOL','NetworkFilesystem','NOT_MIRRORED',NULL,11,2,'f763298f-34f0-4d02-beb6-6bae9afd980b',0,0,'2011-04-23 02:40:07',NULL,NULL,'Created'),(11,3,2,NULL,NULL,NULL,'VlmfromSnapshot-1',8589934592,NULL,NULL,NULL,1,NULL,NULL,'DATADISK','STORAGE_POOL',NULL,'NOT_MIRRORED',NULL,11,NULL,NULL,0,0,'2011-04-23 02:59:06','2011-04-23 02:59:06',NULL,'Creating'),(12,3,2,NULL,NULL,NULL,'VlmfromSnapshot-2',8589934592,NULL,NULL,NULL,1,NULL,NULL,'DATADISK','STORAGE_POOL',NULL,'NOT_MIRRORED',NULL,11,NULL,NULL,0,0,'2011-04-23 02:59:21','2011-04-23 02:59:21',NULL,'Creating'),(13,3,2,200,NULL,NULL,'VlmfromSnapshot-3-Data',1073741824,'/export/home/chandan/eagle/primary21x','bf2ced04-6b7a-4899-9ca6-0e81a4500703',1,1,NULL,NULL,'DATADISK','STORAGE_POOL','NetworkFilesystem','NOT_MIRRORED',NULL,12,NULL,NULL,0,0,'2011-04-23 02:59:25','2011-04-23 02:59:25',NULL,'Created'),(14,2,1,NULL,NULL,NULL,'CreateVlmFromSnp-1',8589934592,NULL,NULL,NULL,1,NULL,NULL,'DATADISK','STORAGE_POOL',NULL,'NOT_MIRRORED',NULL,11,NULL,NULL,0,0,'2011-04-23 03:00:47','2011-04-23 03:00:47',NULL,'Creating'),(15,2,1,NULL,NULL,NULL,'CreateVlmFromSnp-2',8589934592,NULL,NULL,NULL,1,NULL,NULL,'DATADISK','STORAGE_POOL',NULL,'NOT_MIRRORED',NULL,11,NULL,NULL,0,0,'2011-04-23 03:00:55','2011-04-23 03:00:55',NULL,'Creating');
/*!40000 ALTER TABLE `volumes` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2011-04-23  4:11:20
