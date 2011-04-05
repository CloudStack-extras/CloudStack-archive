-- MySQL dump 10.13  Distrib 5.1.55, for redhat-linux-gnu (x86_64)
--
-- Host: 10.91.28.60    Database: cloud
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
-- Not dumping tablespaces as no INFORMATION_SCHEMA.FILES table on this server
--

--
-- Table structure for table `account`
--

DROP TABLE IF EXISTS `account`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `account`
--

LOCK TABLES `account` WRITE;
/*!40000 ALTER TABLE `account` DISABLE KEYS */;
INSERT INTO `account` VALUES (1,'system',1,1,'enabled',NULL,0),(2,'admin',1,1,'enabled',NULL,0),(3,'test',0,1,'enabled','2011-03-24 05:49:55',0),(4,'test',1,1,'enabled','2011-03-24 05:52:34',0),(5,'test1',1,1,'enabled','2011-03-24 07:03:30',0),(6,'d1',2,2,'enabled',NULL,0),(7,'d1',0,1,'disabled',NULL,0),(8,'d4',0,1,'enabled',NULL,0),(9,'d5',0,1,'enabled',NULL,0),(10,'d6',0,2,'enabled',NULL,0),(11,'d7',1,1,'enabled',NULL,0);
/*!40000 ALTER TABLE `account` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `account_vlan_map`
--

DROP TABLE IF EXISTS `account_vlan_map`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `alert`
--

LOCK TABLES `alert` WRITE;
/*!40000 ALTER TABLE `alert` DISABLE KEYS */;
INSERT INTO `alert` VALUES (1,13,0,0,'Management server node 10.91.28.53 is up',1,'2011-03-23 15:12:02','2011-03-23 15:12:02',NULL),(2,13,0,0,'Management server node 10.91.28.59 is up',1,'2011-03-23 15:12:08','2011-03-23 15:12:08',NULL),(3,13,0,0,'Management server node 10.91.28.53 is up',1,'2011-03-23 16:02:09','2011-03-23 16:02:09',NULL),(4,13,0,0,'Management server node 10.91.28.59 is up',1,'2011-03-23 16:02:25','2011-03-23 16:02:25',NULL),(5,12,0,0,'No usage server process running',1,'2011-03-23 17:02:09','2011-03-23 17:02:09',NULL),(6,12,0,0,'No usage server process running',1,'2011-03-23 17:02:24','2011-03-23 17:02:24',NULL),(7,18,1,1,'Secondary Storage Vm up in zone: Default, secStorageVm: s-1-VM, public IP: 10.91.30.104, private IP: 10.91.28.104',1,'2011-03-23 18:16:26','2011-03-23 18:16:26',NULL),(8,9,1,1,'Console proxy up in zone: Default, proxy: v-2-VM, public IP: 10.91.30.106, private IP: 10.91.28.108',1,'2011-03-23 18:17:08','2011-03-23 18:17:08',NULL),(9,13,0,0,'Management server node 10.91.28.53 is up',1,'2011-03-23 19:19:23','2011-03-23 19:19:23',NULL),(10,6,1,1,'Host is down, name: idc-ktxen19 (id:2), availability zone: Default, pod: Default',1,'2011-03-23 19:19:32','2011-03-23 19:19:32',NULL),(11,7,1,1,'Unable to restart s-1-VM which was running on host name: idc-ktxen19(id:2), availability zone: Default, pod: Default',1,'2011-03-23 19:19:32','2011-03-23 19:19:32',NULL),(12,9,1,1,'Unable to restart v-2-VM which was running on host name: idc-ktxen19(id:2), availability zone: Default, pod: Default',1,'2011-03-23 19:19:32','2011-03-23 19:19:32',NULL),(13,7,1,1,'Unable to restart i-2-3-VM which was running on host name: idc-ktxen19(id:2), availability zone: Default, pod: Default',1,'2011-03-23 19:19:32','2011-03-23 19:19:32',NULL),(14,8,1,1,'Unable to restart r-4-VM which was running on host name: idc-ktxen19(id:2), availability zone: Default, pod: Default',1,'2011-03-23 19:19:32','2011-03-23 19:19:32',NULL),(15,13,0,0,'Management server node 10.91.28.53 is down',1,'2011-03-23 19:41:09','2011-03-23 19:41:09',NULL),(16,0,NULL,1,'System Alert: Low Available Memory in availablity zone Default',1,'2011-03-24 05:43:19','2011-03-24 05:43:19','2011-03-25 07:59:31'),(17,0,NULL,1,'System Alert: Low Available Memory in availablity zone Default',1,'2011-03-24 05:44:29','2011-03-24 05:44:29',NULL),(18,6,1,1,'Migration Complete for host name: xenserver-shweta-16 (id:4), availability zone: Default, pod: Default',1,'2011-03-25 08:01:55','2011-03-25 08:01:55',NULL),(19,6,1,1,'Unable to eject host 50cb6cb1-65ea-4471-bdb8-76a185794d79',1,'2011-03-25 08:02:13','2011-03-25 08:02:13',NULL),(20,6,1,1,'Host is down, name: idc-ktxen19 (id:2), availability zone: Default, pod: Default',1,'2011-03-28 11:53:47','2011-03-28 11:53:47',NULL),(21,7,1,1,'Unable to restart s-1-VM which was running on host name: idc-ktxen19(id:2), availability zone: Default, pod: Default',1,'2011-03-28 11:53:47','2011-03-28 11:53:47',NULL),(22,9,1,1,'Unable to restart v-2-VM which was running on host name: idc-ktxen19(id:2), availability zone: Default, pod: Default',1,'2011-03-28 11:53:48','2011-03-28 11:53:48',NULL),(23,8,1,1,'Unable to restart r-4-VM which was running on host name: idc-ktxen19(id:2), availability zone: Default, pod: Default',1,'2011-03-28 11:53:48','2011-03-28 11:53:48',NULL),(24,7,1,1,'Unable to restart i-3-10-VM which was running on host name: idc-ktxen19(id:2), availability zone: Default, pod: Default',1,'2011-03-28 11:53:48','2011-03-28 11:53:48',NULL),(25,8,1,1,'Unable to restart r-21-VM which was running on host name: idc-ktxen19(id:2), availability zone: Default, pod: Default',1,'2011-03-28 11:53:48','2011-03-28 11:53:48',NULL),(26,7,1,1,'Unable to restart i-2-23-VM which was running on host name: idc-ktxen19(id:2), availability zone: Default, pod: Default',1,'2011-03-28 11:53:48','2011-03-28 11:53:48',NULL),(27,6,1,1,'Host is down, name: idc-ktxen19 (id:2), availability zone: Default, pod: Default',1,'2011-03-28 11:58:59','2011-03-28 11:58:59',NULL),(28,7,1,1,'Unable to restart s-1-VM which was running on host name: idc-ktxen19(id:2), availability zone: Default, pod: Default',1,'2011-03-28 11:58:59','2011-03-28 11:58:59',NULL),(29,9,1,1,'Unable to restart v-2-VM which was running on host name: idc-ktxen19(id:2), availability zone: Default, pod: Default',1,'2011-03-28 11:58:59','2011-03-28 11:58:59',NULL),(30,8,1,1,'Unable to restart r-4-VM which was running on host name: idc-ktxen19(id:2), availability zone: Default, pod: Default',1,'2011-03-28 11:58:59','2011-03-28 11:58:59',NULL),(31,7,1,1,'Unable to restart i-3-10-VM which was running on host name: idc-ktxen19(id:2), availability zone: Default, pod: Default',1,'2011-03-28 11:58:59','2011-03-28 11:58:59',NULL),(32,8,1,1,'Unable to restart r-21-VM which was running on host name: idc-ktxen19(id:2), availability zone: Default, pod: Default',1,'2011-03-28 11:58:59','2011-03-28 11:58:59',NULL),(33,7,1,1,'Unable to restart i-2-23-VM which was running on host name: idc-ktxen19(id:2), availability zone: Default, pod: Default',1,'2011-03-28 11:58:59','2011-03-28 11:58:59',NULL);
/*!40000 ALTER TABLE `alert` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `async_job`
--

DROP TABLE IF EXISTS `async_job`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `async_job`
--

LOCK TABLES `async_job` WRITE;
/*!40000 ALTER TABLE `async_job` DISABLE KEYS */;
INSERT INTO `async_job` VALUES (22,2,2,NULL,NULL,NULL,'VolumeOperation','volume','{\"op\":\"Create\",\"accountId\":2,\"userId\":2,\"zoneId\":1,\"name\":\"vv\",\"diskOfferingId\":5,\"vmId\":0,\"volumeId\":0,\"eventId\":185}',0,0,NULL,1,1,0,'com.cloud.async.executor.VolumeOperationResultObject/{\"id\":12,\"name\":\"vv\",\"accountName\":\"admin\",\"domainId\":1,\"domain\":\"ROOT\",\"destroyed\":false,\"diskOfferingId\":5,\"diskOfferingName\":\"Small\",\"diskOfferingDisplayText\":\"Small Disk, 5 GB\",\"volumeType\":\"DATADISK\",\"volumeSize\":5368709120,\"createdDate\":\"Mar 28, 2011 5:18:28 PM\",\"state\":\"Created\",\"storageType\":\"shared\",\"storage\":\"idc-ss\",\"zoneId\":1,\"zoneName\":\"Default\"}',222119676852735,222119676852735,'2011-03-28 11:48:28','2011-03-28 11:48:28',NULL,NULL),(23,6,6,NULL,NULL,NULL,'VolumeOperation','volume','{\"op\":\"Create\",\"accountId\":6,\"userId\":6,\"zoneId\":1,\"name\":\"v1\",\"diskOfferingId\":5,\"vmId\":0,\"volumeId\":0,\"eventId\":190}',0,0,NULL,1,1,0,'com.cloud.async.executor.VolumeOperationResultObject/{\"id\":13,\"name\":\"v1\",\"accountName\":\"d1\",\"domainId\":2,\"domain\":\"D1\",\"destroyed\":false,\"diskOfferingId\":5,\"diskOfferingName\":\"Small\",\"diskOfferingDisplayText\":\"Small Disk, 5 GB\",\"volumeType\":\"DATADISK\",\"volumeSize\":5368709120,\"createdDate\":\"Mar 28, 2011 5:19:13 PM\",\"state\":\"Created\",\"storageType\":\"shared\",\"storage\":\"idc-ss\",\"zoneId\":1,\"zoneName\":\"Default\"}',222119676852735,222119676852735,'2011-03-28 11:49:13','2011-03-28 11:49:14',NULL,NULL),(24,2,2,NULL,NULL,NULL,'DeployVM','virtualmachine','{\"accountId\":2,\"dataCenterId\":1,\"serviceOfferingId\":1,\"templateId\":2,\"diskOfferingId\":5,\"password\":\"eL9xsxtvs\",\"domainId\":0,\"userId\":2,\"vmId\":0,\"eventId\":195}',0,0,NULL,2,1,551,'java.lang.String/\"Create VM [User|i-2-22-VM] failed. There are no pods with enough CPU/memory\"',222119676852735,222119676852735,'2011-03-28 11:50:18','2011-03-28 11:50:18',NULL,NULL),(25,2,10,NULL,NULL,NULL,'DestroyVM',NULL,'{\"userId\":2,\"vmId\":20,\"operation\":\"Noop\",\"eventId\":198}',0,0,NULL,1,0,0,'java.lang.String/\"success\"',222119676852735,222119676852735,'2011-03-28 11:50:25','2011-03-28 11:50:49','2011-03-28 11:50:45',NULL),(26,2,2,NULL,NULL,NULL,'DeployVM','virtualmachine','{\"accountId\":2,\"dataCenterId\":1,\"serviceOfferingId\":1,\"templateId\":2,\"diskOfferingId\":5,\"password\":\"xD9imtqqe\",\"domainId\":0,\"userId\":2,\"vmId\":0,\"eventId\":203}',0,0,NULL,1,1,0,'com.cloud.async.executor.DeployVMResultObject/{\"id\":23,\"name\":\"i-2-23-VM\",\"created\":\"Mar 28, 2011 5:21:18 PM\",\"zoneId\":1,\"zoneName\":\"Default\",\"ipAddress\":\"10.1.1.2\",\"serviceOfferingId\":1,\"haEnabled\":false,\"state\":\"Running\",\"templateId\":2,\"templateName\":\"CentOS 5.3(x86_64) no GUI\",\"templateDisplayText\":\"CentOS 5.3(x86_64) no GUI\",\"passwordEnabled\":false,\"serviceOfferingName\":\"Small Instance, Virtual Networking\",\"cpuNumber\":\"1\",\"cpuSpeed\":\"500\",\"memory\":\"512\",\"displayName\":\"i-2-23-VM\",\"domainId\":1,\"domain\":\"ROOT\",\"account\":\"admin\",\"hostname\":\"idc-ktxen19\",\"hostid\":2,\"networkGroupList\":\"\"}',222119676852735,222119676852735,'2011-03-28 11:51:18','2011-03-28 11:51:59','2011-03-28 11:51:59',NULL),(27,2,2,NULL,NULL,NULL,'VolumeOperation','virtualmachine','{\"op\":\"Detach\",\"accountId\":0,\"userId\":0,\"zoneId\":0,\"diskOfferingId\":0,\"vmId\":0,\"volumeId\":15,\"eventId\":212}',0,0,NULL,1,1,0,'java.lang.Long/15',222119676852735,222119676852735,'2011-03-28 11:53:36','2011-03-28 11:53:37',NULL,NULL),(28,2,2,NULL,NULL,NULL,'VolumeOperation','virtualmachine','{\"op\":\"Attach\",\"accountId\":0,\"userId\":0,\"zoneId\":0,\"diskOfferingId\":0,\"vmId\":23,\"volumeId\":15,\"eventId\":215}',0,0,NULL,2,1,530,'java.lang.String/\"Failed to attach volume: i-2-23-VM-DATA to VM: i-2-23-VM due to: Host 2: Host is not in the right state\"',222119676852735,222119676852735,'2011-03-28 11:54:07','2011-03-28 11:54:07',NULL,NULL),(29,2,2,NULL,NULL,NULL,'VolumeOperation','virtualmachine','{\"op\":\"Attach\",\"accountId\":0,\"userId\":0,\"zoneId\":0,\"diskOfferingId\":0,\"vmId\":23,\"volumeId\":15,\"eventId\":217}',0,0,NULL,2,1,530,'java.lang.String/\"Failed to attach volume: i-2-23-VM-DATA to VM: i-2-23-VM due to: Host 2: Host is not in the right state\"',222119676852735,222119676852735,'2011-03-28 11:54:40','2011-03-28 11:54:40',NULL,NULL),(30,2,2,NULL,NULL,NULL,'VolumeOperation','virtualmachine','{\"op\":\"Attach\",\"accountId\":0,\"userId\":0,\"zoneId\":0,\"diskOfferingId\":0,\"vmId\":23,\"volumeId\":15,\"eventId\":219}',0,0,NULL,1,1,0,'com.cloud.async.executor.AttachVolumeOperationResultObject/{\"virtualMachineId\":23,\"vmName\":\"i-2-23-VM\",\"vmDisplayName\":\"i-2-23-VM\",\"vmState\":\"Running\",\"volumeId\":15,\"volumeName\":\"i-2-23-VM-DATA\",\"storageType\":\"shared\"}',222119676852735,222119676852735,'2011-03-28 11:57:58','2011-03-28 11:57:58',NULL,NULL),(31,2,2,NULL,NULL,NULL,'VolumeOperation','virtualmachine','{\"op\":\"Detach\",\"accountId\":0,\"userId\":0,\"zoneId\":0,\"diskOfferingId\":0,\"vmId\":0,\"volumeId\":15,\"eventId\":222}',0,0,NULL,2,1,530,'java.lang.String/\"Failed to detach volume: i-2-23-VM-DATA from VM: i-2-23-VM; com.cloud.utils.exception.CloudRuntimeException: Unable to make a connection to the server 10.91.28.19\"',222119676852735,222119676852735,'2011-03-28 11:58:19','2011-03-28 11:58:25','2011-03-28 11:58:24',NULL),(32,2,2,NULL,NULL,NULL,'CreateSnapshot','snapshot','{\"accountId\":2,\"userId\":2,\"snapshotId\":0,\"policyIds\":[1],\"policyId\":0,\"volumeId\":14,\"eventId\":0}',0,0,NULL,1,1,0,'com.cloud.async.executor.CreateSnapshotResultObject/{\"id\":1,\"accountName\":\"admin\",\"volumeId\":14,\"domainId\":1,\"domainName\":\"ROOT\",\"created\":\"Mar 29, 2011 3:12:14 PM\",\"name\":\"i-2-23-VM_i-2-23-VM-ROOT_20110329094214\",\"snapshotType\":\"MANUAL\",\"volumeName\":\"i-2-23-VM-ROOT\",\"volumeType\":\"ROOT\"}',222119676852735,222119676852735,'2011-03-29 09:42:14','2011-03-29 09:43:53','2011-03-29 09:43:50',NULL),(33,2,3,NULL,NULL,NULL,'DeleteTemplate','deletetemplateresponse','{\"userId\":2,\"templateId\":201,\"zoneId\":1,\"eventId\":230}',0,0,NULL,1,0,0,NULL,222119676852735,222119676852735,'2011-03-29 09:42:43','2011-03-29 09:42:43',NULL,NULL);
/*!40000 ALTER TABLE `async_job` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cluster`
--

DROP TABLE IF EXISTS `cluster`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cluster`
--

LOCK TABLES `cluster` WRITE;
/*!40000 ALTER TABLE `cluster` DISABLE KEYS */;
INSERT INTO `cluster` VALUES (1,'c',1,1),(2,'CCC',1,1);
/*!40000 ALTER TABLE `cluster` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `configuration`
--

DROP TABLE IF EXISTS `configuration`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `configuration` (
  `category` varchar(255) NOT NULL default 'Advanced',
  `instance` varchar(255) NOT NULL,
  `component` varchar(255) NOT NULL default 'management-server',
  `name` varchar(255) NOT NULL,
  `value` varchar(4095) default NULL,
  `description` varchar(1024) default NULL,
  PRIMARY KEY  (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `configuration`
--

LOCK TABLES `configuration` WRITE;
/*!40000 ALTER TABLE `configuration` DISABLE KEYS */;
INSERT INTO `configuration` VALUES ('Advanced','DEFAULT','management-server','account.cleanup.interval','86400','The interval in seconds between cleanup for removed accounts'),('Alert','DEFAULT','management-server','alert.email.addresses',NULL,'Comma separated list of email addresses used for sending alerts.'),('Alert','DEFAULT','management-server','alert.email.sender',NULL,'Sender of alert email (will be in the From header of the email).'),('Alert','DEFAULT','management-server','alert.smtp.host',NULL,'SMTP hostname used for sending out email alerts.'),('Alert','DEFAULT','management-server','alert.smtp.password',NULL,'Password for SMTP authentication (applies only if alert.smtp.useAuth is true).'),('Alert','DEFAULT','management-server','alert.smtp.port','465','Port the SMTP server is listening on.'),('Alert','DEFAULT','management-server','alert.smtp.useAuth',NULL,'If true, use SMTP authentication when sending emails.'),('Alert','DEFAULT','management-server','alert.smtp.username',NULL,'Username for SMTP authentication (applies only if alert.smtp.useAuth is true).'),('Alert','DEFAULT','AgentManager','alert.wait',NULL,'Seconds to wait before alerting on a disconnected agent'),('Advanced','DEFAULT','management-server','allow.public.user.templates','true','If false, users will not be able to create public templates.'),('Usage','DEFAULT','management-server','capacity.check.period','300000','The interval in milliseconds between capacity checks'),('Usage','DEFAULT','management-server','capacity.skipcounting.hours','24','The interval in hours since VM has stopped to skip counting its allocated CPU/Memory capacity'),('Advanced','DEFAULT','management-server','check.pod.cidrs','true','If true, different pods must belong to different CIDR subnets.'),('Hidden','DEFAULT','management-server','cloud.identifier','be43ad26-7ad1-4885-a462-d241d595e226','A unique identifier for the cloud.'),('Console Proxy','DEFAULT','AgentManager','consoleproxy.capacity.standby','10','The minimal number of console proxy viewer sessions that system is able to serve immediately(standby capacity)'),('Console Proxy','DEFAULT','AgentManager','consoleproxy.capacityscan.interval','30000','The time interval(in millisecond) to scan whether or not system needs more console proxy to ensure minimal standby capacity'),('Console Proxy','DEFAULT','AgentManager','consoleproxy.cmd.port','8001','Console proxy command port that is used to communicate with management server'),('Console Proxy','DEFAULT','AgentManager','consoleproxy.loadscan.interval','10000','The time interval(in milliseconds) to scan console proxy working-load info'),('Console Proxy','DEFAULT','AgentManager','consoleproxy.ram.size','1024','RAM size (in MB) used to create new console proxy VMs'),('Console Proxy','DEFAULT','AgentManager','consoleproxy.session.max','50','The max number of viewer sessions console proxy is configured to serve for'),('Console Proxy','DEFAULT','AgentManager','consoleproxy.session.timeout','300000','Timeout(in milliseconds) that console proxy tries to maintain a viewer session before it times out the session for no activity'),('Usage','DEFAULT','management-server','cpu.capacity.threshold','0.85','Percentage (as a value between 0 and 1) of cpu utilization above which alerts will be sent about low cpu available.'),('Advanced','DEFAULT','management-server','cpu.overprovisioning.factor','1','Used for CPU overprovisioning calculation; available CPU will be (actualCpuCapacity * cpu.overprovisioning.factor)'),('Advanced','DEFAULT','management-server','default.page.size','500','Default page size for API list* commands'),('Advanced','DEFAULT','management-server','direct.attach.network.externalIpAllocator.enabled','false','Direct-attach VMs using external DHCP server'),('Advanced','DEFAULT','management-server','direct.attach.network.externalIpAllocator.url',NULL,'Direct-attach VMs using external DHCP server (API url)'),('Advanced','DEFAULT','management-server','direct.attach.network.groups.enabled','false','Ec2-style distributed firewall for direct-attach VMs'),('Advanced','DEFAULT','management-server','direct.attach.untagged.vlan.enabled','false','Indicate whether the system supports direct-attached untagged vlan'),('Premium','DEFAULT','management-server','enable.usage.server','true','Flag for enabling usage'),('Advanced','DEFAULT','management-server','event.purge.delay','0','Events older than specified number days will be purged'),('Advanced','DEFAULT','UserVmManager','expunge.delay','10','Determines how long to wait before actually expunging destroyed vm. The default value = the default value of expunge.interval'),('Advanced','DEFAULT','UserVmManager','expunge.interval','10','The interval to wait before running the expunge thread.'),('Advanced','DEFAULT','UserVmManager','expunge.workers','1','Number of workers performing expunge '),('Advanced','DEFAULT','management-server','host','10.91.28.53','The ip address of management server'),('Advanced','DEFAULT','AgentManager','host.retry','2','Number of times to retry hosts for creating a volume'),('Advanced','DEFAULT','management-server','host.stats.interval','60000','The interval in milliseconds when host stats are retrieved from agents.'),('Advanced','DEFAULT','management-server','hypervisor.type','xenserver','The type of hypervisor that this deployment will use.'),('Hidden','DEFAULT','none','init','true',NULL),('Advanced','DEFAULT','AgentManager','instance.name','VM','Name of the deployment instance.'),('Advanced','DEFAULT','management-server','integration.api.port','8096','Defaul API port'),('Advanced','DEFAULT','HighAvailabilityManager','investigate.retry.interval','60','Time in seconds between VM pings when agent is disconnected'),('Advanced','DEFAULT','management-server','job.expire.minutes','1440','Time (in minutes) for async-jobs to be kept in system'),('Advanced','DEFAULT','management-server','linkLocalIp.nums','10','The number of link local ip that needed by domR(in power of 2)'),('Advanced','DEFAULT','management-server','max.template.iso.size','50','The maximum size for a downloaded template or ISO (in GB).'),('Storage','DEFAULT','management-server','max.volume.size.gb','2000','The maximum size for a volume in Gb.'),('Usage','DEFAULT','management-server','memory.capacity.threshold','0.85','Percentage (as a value between 0 and 1) of memory utilization above which alerts will be sent about low memory available.'),('Advanced','DEFAULT','HighAvailabilityManager','migrate.retry.interval','120','Time in seconds between migration retries'),('Advanced','DEFAULT','management-server','mount.parent','/var/lib/cloud/mnt','The mount point on the Management Server for Secondary Storage.'),('Network','DEFAULT','management-server','multicast.throttling.rate','10','Default multicast rate in megabits per second allowed.'),('Network','DEFAULT','management-server','network.throttling.rate','200','Default data transfer rate in megabits per second allowed.'),('Advanced','DEFAULT','management-server','network.type','vlan','The type of network that this deployment will use.'),('Advanced','DEFAULT','AgentManager','ping.interval','60','Ping interval in seconds'),('Advanced','DEFAULT','AgentManager','ping.timeout','2.5','Multiplier to ping.interval before announcing an agent has timed out'),('Advanced','DEFAULT','AgentManager','port','8250','Port to listen on for agent connection.'),('Usage','DEFAULT','management-server','private.ip.capacity.threshold','0.85','Percentage (as a value between 0 and 1) of private IP address space utilization above which alerts will be sent.'),('Usage','DEFAULT','management-server','public.ip.capacity.threshold','0.85','Percentage (as a value between 0 and 1) of public IP address space utilization above which alerts will be sent.'),('Advanced','DEFAULT','HighAvailabilityManager','restart.retry.interval','600','Time in seconds between retries to restart a vm'),('Advanced','DEFAULT','management-server','router.cleanup.interval','3600','Time in seconds identifies when to stop router when there are no user vms associated with it'),('Advanced','DEFAULT','none','router.ram.size','128','Default RAM for router VM in MB.'),('Advanced','DEFAULT','none','router.stats.interval','30','Interval to report router statistics.'),('Advanced','DEFAULT','none','router.template.id','1','Default ID for template.'),('Hidden','DEFAULT','database','schema.level','2.1.3','The schema level of this database'),('Advanced','DEFAULT','management-server','secondary.storage.vm','true','Deploys a VM per zone to manage secondary storage if true, otherwise secondary storage is mounted on management server'),('Advanced','DEFAULT','management-server','secstorage.allowed.internal.sites','10.91.28.0/24','Comma separated list of cidrs internal to the datacenter that can host template download servers'),('Hidden','DEFAULT','management-server','secstorage.copy.password','hF8yhqihf','Password used to authenticate zone-to-zone template copy requests'),('Advanced','DEFAULT','management-server','secstorage.encrypt.copy','true','Use SSL method used to encrypt copy traffic between zones'),('Advanced','DEFAULT','management-server','secstorage.ssl.cert.domain','realhostip.com','SSL certificate used to encrypt copy traffic between zones'),('Advanced','DEFAULT','AgentManager','secstorage.vm.ram.size',NULL,'RAM size (in MB) used to create new secondary storage vms'),('Hidden','DEFAULT','management-server','security.hash.key','a730167e-ea90-4e13-b9d3-89ae9844366d','for generic key-ed hash'),('Hidden','DEFAULT','management-server','security.singlesignon.key','MNUuvf6X-X5Z0pnqAbTwUgSPq4SAmxJo8uqayLkoRMbTDtqg6IeZy3GkoW7rMwGgye3ZbkNXRPiZDktfm26IOg','A Single Sign-On key used for logging into the cloud'),('Advanced','DEFAULT','management-server','security.singlesignon.tolerance.millis','300000','The allowable clock difference in milliseconds between when an SSO login request is made and when it is received.'),('Snapshots','DEFAULT','none','snapshot.max.daily','8','Maximum dalily snapshots for a volume'),('Snapshots','DEFAULT','none','snapshot.max.hourly','8','Maximum hourly snapshots for a volume'),('Snapshots','DEFAULT','none','snapshot.max.monthly','8','Maximum hourly snapshots for a volume'),('Snapshots','DEFAULT','none','snapshot.max.weekly','8','Maximum hourly snapshots for a volume'),('Snapshots','DEFAULT','none','snapshot.poll.interval','300','The time interval in seconds when the management server polls for snapshots to be scheduled.'),('Hidden','DEFAULT','management-server','ssh.privatekey','-----BEGIN RSA PRIVATE KEY-----\nMIIEoQIBAAKCAQEAo3JoUsPXeXt3yTJBnJw50A8+HTPi+Chd8YSmUxmwVAccFSwr\n5Qwf0zxRSXExIpMb7LkwToMJzBiZoTXlfYr6kVXO3jKlnXXYbRebAOjXvTF4eNWD\nM6OR63ElJs+B1KbdX8K+pRStmIpwQCK0D3Z2ACF4VoKmVMMjBpWP6vbID+P/drdS\n3+ZlbSWLGKrtfgojyUNbWj9dpm7AiCQqvRkR1t7PHhRi2yNFvadSKp71aKnRRcWl\nY/blgvK7Um1pfUd3fBBy4jVJThKT+W8LeSNiH4chPYSPtb7SSZ3UIC0XVKEypuiB\n2Lg3MZQ7mzYRP3pgz8jl1gmDRvlzAAIzMgW38QIBIwKCAQBPY3R/+Lkdv6CU7IZE\nwOjhZn1BYlhMpeR8n4P8ejES3uHBJBVR/pMddRGKEmhSnzly+tzy7zCk9gF6MB8C\ndrQ39npr7LbXc8Dkh87x0C5F83UHfaYgZWQh5n/CZMoIM8qcOgTT1tf5olPHYVAk\nxIJ1F5I4pdvCxS5FBtDf1vN1bfcpo9aGagXO9pXE4itg3wWG+1jXQo1/8Kr/hpRJ\nJs+9zF0S73z5+s/oi8GJRMHLdCNrZ8HdmJXbWgbbAyjdLKncLKQMoc5csgk4FTMX\nLCYOOtbeXyKmMnisp3XEw1QZ/yN2WDvXffHFQTUxXHmQsrjLZEGsfK0ZJUX0P1r3\nBlMXAoGBANCfZs2YyY3SdKnywEE4LE/QiDudYKiB+MmjQW96GcXNqP6Wuk8OewMT\n1nQUIya6nSCMfRiwByQGmkqe/FJW9p+c6utRF+KzTglwJA99wVW0mAN3gqq1bIkc\nflk36Uv6kvsXNsfKQS4RLme7vvjDM6arkwyo32/xGzvKEFFj5YFfAoGBAMiQpNqq\n2SX/piVFRjTYx18uHw56KILCs6Xl3Lck3JFGNraNiyGNwgAHR7kK7j+w0kmu+nYJ\n07oiJp7SapXXbVwKyZ8631RUZQ4k4xpTNFSn/YGDtn+MCH3nxrWedWl+4tBu3fOP\nClOZWAwBXlpwfGrb0Nl+v/pANx+ofoDgmrivAoGANaVU8woH8US+6d9WAiRjKndz\nfQs2HLO1AKZSp6MN8Qj/kew+iVt+t6YD8faMsi/8hLZpT324sX4KavWumNSIj3F+\nPIKRHQmJGF6qL96CK/s9CDSskkvotYr76w5gj+FY/rzTknXWPwu7ecKQMVbEI4s0\ncPg5dI50t5pb96v5L+UCgYALdfrKqq1SoOTsL9ggRubSPSZYmUQkuqsuDSKOH10P\nnZy5+XWqJVuK+RoKkukK9ClGCf+u+T9MdvrkgQ1npej98fzkhwVyiHrNm7U0tEwi\nGDpfLBkOmkmgy2pwxznoxWwL6RP/SgCXEBOo6iKmFQ5sgZbn21ttYsFSRCR8ZJsg\nfwKBgQDCPPwe9yUk+hDFVtVSpQgSxXc003eknBrrwtZPPTlKMcZv3p0UsfJXtqF7\np79RfPGl0qMaYTEGQrCfIo9iasAEgp+bdVoYq9okBNJdZTZODXZa2M4V44MNCsoE\nY+hX02YKHA6QHuqLg2BEIGsc++ZspePNv4lqQeNu1RN0ykS4/A==\n-----END RSA PRIVATE KEY-----','Private key for the entire CloudStack'),('Hidden','DEFAULT','management-server','ssh.publickey','ssh-rsa AAAAB3NzaC1yc2EAAAABIwAAAQEAo3JoUsPXeXt3yTJBnJw50A8+HTPi+Chd8YSmUxmwVAccFSwr5Qwf0zxRSXExIpMb7LkwToMJzBiZoTXlfYr6kVXO3jKlnXXYbRebAOjXvTF4eNWDM6OR63ElJs+B1KbdX8K+pRStmIpwQCK0D3Z2ACF4VoKmVMMjBpWP6vbID+P/drdS3+ZlbSWLGKrtfgojyUNbWj9dpm7AiCQqvRkR1t7PHhRi2yNFvadSKp71aKnRRcWlY/blgvK7Um1pfUd3fBBy4jVJThKT+W8LeSNiH4chPYSPtb7SSZ3UIC0XVKEypuiB2Lg3MZQ7mzYRP3pgz8jl1gmDRvlzAAIzMgW38Q== cloud@idc-2.1.7-mgmt1','Public key for the entire CloudStack'),('Advanced','DEFAULT','AgentManager','start.retry','10','Number of times to retry create and start commands'),('Advanced','DEFAULT','HighAvailabilityManager','stop.retry.interval','600','Time in seconds between retries to stop or destroy a vm'),('Usage','DEFAULT','management-server','storage.allocated.capacity.threshold','0.85','Percentage (as a value between 0 and 1) of allocated storage utilization above which alerts will be sent about low storage available.'),('Usage','DEFAULT','management-server','storage.capacity.threshold','0.85','Percentage (as a value between 0 and 1) of storage utilization above which alerts will be sent about low storage available.'),('Advanced','DEFAULT','none','storage.cleanup.enabled','true','Enables/disables the storage cleanup thread.'),('Advanced','DEFAULT','none','storage.cleanup.interval','86400','The interval to wait before running the storage cleanup thread.'),('Storage','DEFAULT','StorageAllocator','storage.overprovisioning.factor','2','Used for storage overprovisioning calculation; available storage will be (actualStorageSize * storage.overprovisioning.factor)'),('Storage','DEFAULT','management-server','storage.stats.interval','60000','The interval in milliseconds when storage stats (per host) are retrieved from agents.'),('Advanced','DEFAULT','management-server','system.vm.use.local.storage','false','Indicates whether to use local storage pools or shared storage pools for system VMs.'),('Storage','DEFAULT','AgentManager','total.retries','4','The number of times each command sent to a host should be retried in case of failure.'),('Advanced','DEFAULT','AgentManager','update.wait','600','Time to wait before alerting on a updating agent'),('Advanced','DEFAULT','management-server','upgrade.url','http://example.com:8080/client/agent/update.zip','The upgrade URL is the URL of the management server that agents will connect to in order to automatically upgrade.'),('Premium','DEFAULT','management-server','usage.execution.timezone',NULL,'The timezone to use for usage job execution time'),('Premium','DEFAULT','management-server','usage.stats.job.aggregation.range','1440','The range of time for aggregating the user statistics specified in minutes (e.g. 1440 for daily, 60 for hourly.'),('Premium','DEFAULT','management-server','usage.stats.job.exec.time','00:15','The time at which the usage statistics aggregation job will run as an HH24:MM time, e.g. 00:30 to run at 12:30am.'),('Premium','DEFAULT','management-server','use.local.storage','false','Should we use the local storage if it\'s available?'),('Advanced','DEFAULT','management-server','vm.allocation.algorithm','random','If \'random\', hosts within a pod will be randomly considered for VM/volume allocation. If \'firstfit\', they will be considered on a first-fit basis.'),('Advanced','DEFAULT','AgentManager','wait','1800','Time to wait for control commands to return'),('Advanced','DEFAULT','AgentManager','workers','5','Number of worker threads.'),('Advanced','DEFAULT','management-server','xen.bond.storage.nics',NULL,'Attempt to bond the two networks if found'),('Hidden','DEFAULT','management-server','xen.create.pools.in.pod','false','Should we automatically add XenServers into pools that are inside a Pod'),('Advanced','DEFAULT','management-server','xen.guest.network.device',NULL,'Specify when the guest network does not go over the private network'),('Advanced','DEFAULT','management-server','xen.heartbeat.interval','60','heartbeat to use when implementing XenServer Self Fencing'),('Advanced','DEFAULT','management-server','xen.max.product.version','5.6.0','Maximum XenServer version'),('Advanced','DEFAULT','management-server','xen.max.version','3.4.2','Maximum Xen version'),('Advanced','DEFAULT','management-server','xen.max.xapi.version','1.3','Maximum Xapi Tool Stack version'),('Advanced','DEFAULT','management-server','xen.min.product.version','0.1.1','Minimum XenServer version'),('Advanced','DEFAULT','management-server','xen.min.version','3.3.1','Minimum Xen version'),('Advanced','DEFAULT','management-server','xen.min.xapi.version','1.3','Minimum Xapi Tool Stack version'),('Advanced','DEFAULT','management-server','xen.preallocated.lun.size.range','.05','percentage to add to disk size when allocating'),('Network','DEFAULT','management-server','xen.private.network.device',NULL,'Specify when the private network name is different'),('Network','DEFAULT','management-server','xen.public.network.device',NULL,'[ONLY IF THE PUBLIC NETWORK IS ON A DEDICATED NIC]:The network name label of the physical device dedicated to the public network on a XenServer host'),('Advanced','DEFAULT','management-server','xen.setup.multipath','false','Setup the host to do multipath'),('Network','DEFAULT','management-server','xen.storage.network.device1','cloud-stor1','Specify when there are storage networks'),('Network','DEFAULT','management-server','xen.storage.network.device2','cloud-stor2','Specify when there are storage networks');
/*!40000 ALTER TABLE `configuration` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `console_proxy`
--

DROP TABLE IF EXISTS `console_proxy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `console_proxy`
--

LOCK TABLES `console_proxy` WRITE;
/*!40000 ALTER TABLE `console_proxy` DISABLE KEYS */;
INSERT INTO `console_proxy` VALUES (2,'10.91.30.1','4.2.2.2',NULL,'foo.com','06:81:93:ec:00:03','10.91.30.106','255.255.255.0','06:01:a6:50:00:04','169.254.0.104','255.255.0.0',1,'30',1024,0,'2011-03-28 09:47:22','{\"connections\":[]}\n');
/*!40000 ALTER TABLE `console_proxy` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `data_center`
--

DROP TABLE IF EXISTS `data_center`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `data_center`
--

LOCK TABLES `data_center` WRITE;
/*!40000 ALTER TABLE `data_center` DISABLE KEYS */;
INSERT INTO `data_center` VALUES (1,'Default',NULL,'4.2.2.2',NULL,'4.2.2.2',NULL,NULL,NULL,'500-504','02:00:00:00:00:01',16,'10.1.1.0/24');
/*!40000 ALTER TABLE `data_center` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `disk_offering`
--

DROP TABLE IF EXISTS `disk_offering`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `disk_offering`
--

LOCK TABLES `disk_offering` WRITE;
/*!40000 ALTER TABLE `disk_offering` DISABLE KEYS */;
INSERT INTO `disk_offering` VALUES (1,NULL,'Small Instance, Virtual Networking','Small Instance, Virtual Networking, $0.05 per hour',0,0,'Service',NULL,0,0,NULL,NULL,'2011-03-23 15:11:55'),(2,NULL,'Medium Instance, Virtual Networking','Medium Instance, Virtual Networking, $0.10 per hour',0,0,'Service',NULL,0,0,NULL,NULL,'2011-03-23 15:11:55'),(3,NULL,'Small Instance, Direct Networking','Small Instance, Direct Networking, $0.05 per hour',0,0,'Service',NULL,0,0,NULL,NULL,'2011-03-23 15:11:55'),(4,NULL,'Medium Instance, Direct Networking','Medium Instance, Direct Networking, $0.10 per hour',0,0,'Service',NULL,0,0,NULL,NULL,'2011-03-23 15:11:55'),(5,1,'Small','Small Disk, 5 GB',5120,0,'Disk',NULL,0,0,NULL,NULL,'2011-03-23 15:11:55'),(6,1,'Medium','Medium Disk, 20 GB',20480,0,'Disk',NULL,0,0,NULL,NULL,'2011-03-23 15:11:55'),(7,1,'Large','Large Disk, 100 GB',102400,0,'Disk',NULL,0,0,NULL,NULL,'2011-03-23 15:11:55'),(8,NULL,'Fake Offering For DomR',NULL,0,0,'Service',NULL,1,0,'Cloud.Com-SoftwareRouter','2011-03-23 15:12:00','2011-03-23 15:12:00'),(9,NULL,'Fake Offering For Secondary Storage VM',NULL,0,0,'Service',NULL,1,0,'Cloud.com-SecondaryStorage','2011-03-23 15:12:00','2011-03-23 15:12:00'),(10,NULL,'Fake Offering For DomP',NULL,0,0,'Service',NULL,1,0,'Cloud.com-ConsoleProxy','2011-03-23 15:12:00','2011-03-23 15:12:00');
/*!40000 ALTER TABLE `disk_offering` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `disk_template_ref`
--

DROP TABLE IF EXISTS `disk_template_ref`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

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
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `domain`
--

LOCK TABLES `domain` WRITE;
/*!40000 ALTER TABLE `domain` DISABLE KEYS */;
INSERT INTO `domain` VALUES (1,NULL,'ROOT',2,'/',0,1,2,NULL),(2,1,'D1',2,'/D1/',1,0,1,NULL);
/*!40000 ALTER TABLE `domain` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `domain_router`
--

DROP TABLE IF EXISTS `domain_router`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `domain_router`
--

LOCK TABLES `domain_router` WRITE;
/*!40000 ALTER TABLE `domain_router` DISABLE KEYS */;
INSERT INTO `domain_router` VALUES (4,'10.91.30.1',128,'4.2.2.2',NULL,'v2.myvm.com','06:81:7a:0f:00:05','10.91.30.103','255.255.255.0','02:00:01:f4:00:01',NULL,'255.255.255.0','10.1.1.1','500',NULL,1,'30',2,1,2,'DHCP_FIREWALL_LB_PASSWD_USERDATA'),(6,'10.91.30.1',128,'4.2.2.2',NULL,'v3.myvm.com','06:81:5f:36:00:07',NULL,'255.255.255.0','02:00:01:f5:00:01',NULL,'255.255.255.0','10.1.1.1',NULL,NULL,NULL,'30',3,1,2,'DHCP_FIREWALL_LB_PASSWD_USERDATA'),(11,'10.91.30.1',128,'4.2.2.2',NULL,'v3.myvm.com','06:81:55:38:00:09','10.91.30.108','255.255.255.0','02:00:01:f5:00:01',NULL,'255.255.255.0','10.1.1.1',NULL,NULL,1,'30',3,1,2,'DHCP_FIREWALL_LB_PASSWD_USERDATA'),(21,'10.91.30.1',128,'4.2.2.2',NULL,'va.myvm.com','06:81:59:3c:00:0d','10.91.30.101','255.255.255.0','02:00:01:f6:00:01',NULL,'255.255.255.0','10.1.1.1',NULL,NULL,1,'30',10,2,2,'DHCP_FIREWALL_LB_PASSWD_USERDATA');
/*!40000 ALTER TABLE `domain_router` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `event`
--

DROP TABLE IF EXISTS `event`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
) ENGINE=InnoDB AUTO_INCREMENT=237 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `event`
--

LOCK TABLES `event` WRITE;
/*!40000 ALTER TABLE `event` DISABLE KEYS */;
INSERT INTO `event` VALUES (1,'SERVICE.OFFERING.CREATE','Completed','Successfully created new service offering with name: Small Instance, Virtual Networking.',1,1,'2011-03-23 15:11:55','INFO',0,'soId=1\nname=Small Instance, Virtual Networking\nnumCPUs=1\nram=512\ncpuSpeed=500\ndisplayText=Small Instance, Virtual Networking, $0.05 per hour\nguestIPType=Virtualized\nlocalStorageRequired=false\nofferHA=false\nuseVirtualNetwork=true\n'),(2,'SERVICE.OFFERING.CREATE','Completed','Successfully created new service offering with name: Medium Instance, Virtual Networking.',1,1,'2011-03-23 15:11:55','INFO',0,'soId=2\nname=Medium Instance, Virtual Networking\nnumCPUs=1\nram=1024\ncpuSpeed=1000\ndisplayText=Medium Instance, Virtual Networking, $0.10 per hour\nguestIPType=Virtualized\nlocalStorageRequired=false\nofferHA=false\nuseVirtualNetwork=true\n'),(3,'SERVICE.OFFERING.CREATE','Completed','Successfully created new service offering with name: Small Instance, Direct Networking.',1,1,'2011-03-23 15:11:55','INFO',0,'soId=3\nname=Small Instance, Direct Networking\nnumCPUs=1\nram=512\ncpuSpeed=500\ndisplayText=Small Instance, Direct Networking, $0.05 per hour\nguestIPType=DirectSingle\nlocalStorageRequired=false\nofferHA=false\nuseVirtualNetwork=false\n'),(4,'SERVICE.OFFERING.CREATE','Completed','Successfully created new service offering with name: Medium Instance, Direct Networking.',1,1,'2011-03-23 15:11:55','INFO',0,'soId=4\nname=Medium Instance, Direct Networking\nnumCPUs=1\nram=1024\ncpuSpeed=1000\ndisplayText=Medium Instance, Direct Networking, $0.10 per hour\nguestIPType=DirectSingle\nlocalStorageRequired=false\nofferHA=false\nuseVirtualNetwork=false\n'),(5,'CONFIGURATION.VALUE.EDIT','Completed','Successfully edited configuration value.',1,1,'2011-03-23 15:11:55','INFO',0,'name=mount.parent\nvalue=/var/lib/cloud/mnt'),(6,'CONFIGURATION.VALUE.EDIT','Completed','Successfully edited configuration value.',1,1,'2011-03-23 15:11:56','INFO',0,'name=host\nvalue=10.91.28.53'),(7,'ZONE.CREATE','Completed','Successfully created new zone with name: Default.',1,1,'2011-03-23 15:11:56','INFO',0,'dcId=1\ndns1=4.2.2.2\ninternalDns1=4.2.2.2\nvnetRange=1000-2000\nguestCidr=10.1.1.0/24'),(8,'POD.CREATE','Completed','Successfully created new pod with name: Default in zone: Default.',1,1,'2011-03-23 15:11:57','INFO',0,'podId=1\nzoneId=1\ngateway=10.91.28.1\ncidr=10.91.28.1/24\n'),(9,'USER.LOGIN','Completed','user has logged in',2,2,'2011-03-23 15:57:57','INFO',0,NULL),(10,'ZONE.EDIT','Completed','Successfully edited zone with name: Default.',2,2,'2011-03-23 15:58:42','INFO',0,'dcId=1\ndns1=4.2.2.2\ninternalDns1=4.2.2.2\nvnetRange=500-504\nguestCidr=10.1.1.0/24'),(11,'VLAN.IP.RANGE.CREATE','Completed','Successfully created new VLAN (tag = 30, gateway = 10.91.30.1, netmask = 255.255.255.0, start IP = 10.91.30.100, end IP = 10.91.30.109.',2,2,'2011-03-23 15:59:09','INFO',0,'vlanType=VirtualNetwork\ndcId=1\nvlanId=30\nvlanGateway=10.91.30.1\nvlanNetmask=255.255.255.0\nstartIP=10.91.30.100\nendIP=10.91.30.109\n'),(12,'POD.EDIT','Completed','Successfully edited pod. New pod name is: Default and new zone name is: Default.',2,2,'2011-03-23 15:59:31','INFO',0,'podId=1\ndcId=1\ngateway=10.91.28.1\ncidr=10.91.28.1/24\nstartIp=10.91.28.100\nendIp=10.91.28.109'),(13,'CONFIGURATION.VALUE.EDIT','Completed','Successfully edited configuration value.',2,2,'2011-03-23 16:00:05','INFO',0,'name=expunge.delay\nvalue=10'),(14,'CONFIGURATION.VALUE.EDIT','Completed','Successfully edited configuration value.',2,2,'2011-03-23 16:00:15','INFO',0,'name=expunge.interval\nvalue=10'),(15,'CONFIGURATION.VALUE.EDIT','Completed','Successfully edited configuration value.',2,2,'2011-03-23 16:00:42','INFO',0,'name=router.stats.interval\nvalue=30'),(16,'CONFIGURATION.VALUE.EDIT','Completed','Successfully edited configuration value.',2,2,'2011-03-23 16:00:56','INFO',0,'name=secstorage.allowed.internal.sites\nvalue=10.91.28.0/24'),(17,'USER.LOGIN','Completed','user has logged in',2,2,'2011-03-23 17:49:19','INFO',0,NULL),(18,'TEMPLATE.DOWNLOAD.FAILED','Completed','Storage server nfs://10.91.28.6/export/home/idc218/secondary disconnected during download of template CentOS 5.3(x86_64) no GUI',1,1,'2011-03-23 18:00:49','WARN',0,NULL),(19,'TEMPLATE.DOWNLOAD.FAILED','Completed','CentOS 5.3(x86_64) no GUI failed to download to storage server nfs://10.91.28.6/export/home/idc218/secondary',1,1,'2011-03-23 18:00:49','ERROR',0,NULL),(20,'SSVM.CREATE','Completed','New Secondary Storage VM created - s-1-VM',1,1,'2011-03-23 18:09:53','INFO',0,NULL),(21,'PROXY.CREATE','Completed','New console proxy created - v-2-VM',1,1,'2011-03-23 18:09:53','INFO',0,NULL),(22,'SSVM.START','Started','Starting secondary storage Vm Id: 1',1,1,'2011-03-23 18:15:52','INFO',0,NULL),(23,'SSVM.START','Completed','Secondary Storage VM started - s-1-VM',1,1,'2011-03-23 18:16:26','INFO',0,NULL),(24,'TEMPLATE.DOWNLOAD.START','Completed','Storage server nfs://10.91.28.6/export/home/idc218/secondary started download of template CentOS 5.3(x86_64) no GUI',1,1,'2011-03-23 18:16:35','INFO',0,NULL),(25,'PROXY.START','Completed','Console proxy started - v-2-VM',1,1,'2011-03-23 18:17:08','INFO',0,NULL),(26,'TEMPLATE.DOWNLOAD.SUCCESS','Completed','CentOS 5.3(x86_64) no GUI successfully downloaded to storage server nfs://10.91.28.6/export/home/idc218/secondary',1,1,'2011-03-23 18:21:26','INFO',0,NULL),(27,'TEMPLATE.CREATE','Completed','Successfully created template CentOS 5.3(x86_64) no GUI',1,1,'2011-03-23 18:21:26','INFO',0,'id=2\ndcId=1\nsize=8589934592'),(28,'VM.CREATE','Scheduled','Scheduled async job for deploying Vm',2,2,'2011-03-23 18:30:54','INFO',0,NULL),(29,'VM.CREATE','Started','Deploying Vm',2,2,'2011-03-23 18:30:54','INFO',28,NULL),(30,'NET.IPASSIGN','Completed','Acquired a public ip: 10.91.30.103',1,2,'2011-03-23 18:30:54','INFO',0,'address=10.91.30.103\nsourceNat=true\ndcId=1'),(31,'ROUTER.CREATE','Completed','successfully created Domain Router : r-4-VM with ip : 10.91.30.103',1,2,'2011-03-23 18:30:54','INFO',0,NULL),(32,'VOLUME.CREATE','Completed','Created volume: i-2-3-VM-ROOT with size: 8192 MB',2,2,'2011-03-23 18:35:56','INFO',0,'id=4\ndoId=-1\ntId=2\ndcId=1\nsize=8192'),(33,'VM.CREATE','Completed','successfully created VM instance : i-2-3-VM',2,2,'2011-03-23 18:35:56','INFO',28,'id=3\nvmName=i-2-3-VM\nsoId=1\ndoId=-1\ntId=2\ndcId=1'),(34,'VM.START','Started','Starting Vm with Id: 3',2,2,'2011-03-23 18:35:56','INFO',0,NULL),(35,'ROUTER.START','Started','Starting Router with Id: 4',1,2,'2011-03-23 18:35:56','INFO',0,NULL),(36,'ROUTER.START','Completed','successfully started Domain Router: r-4-VM',1,2,'2011-03-23 18:36:32','INFO',0,NULL),(37,'VM.START','Completed','successfully started VM: i-2-3-VM',2,2,'2011-03-23 18:36:39','INFO',34,'id=3\nvmName=i-2-3-VM\nsoId=1\ndoId=-1\ntId=2\ndcId=1'),(38,'USER.LOGIN','Completed','user has logged in',2,2,'2011-03-24 05:17:47','INFO',0,NULL),(39,'USER.LOGIN','Completed','user has logged in',2,2,'2011-03-24 05:17:54','INFO',0,NULL),(40,'USER.CREATE','Completed','User, test for accountId = 3 and domainId = 1 was created.',1,1,'2011-03-24 05:37:23','INFO',0,NULL),(41,'USER.LOGOUT','Completed','user has logged out',2,2,'2011-03-24 05:42:23','INFO',0,NULL),(42,'USER.LOGIN','Completed','user has logged in',3,3,'2011-03-24 05:42:35','INFO',0,NULL),(43,'VM.CREATE','Scheduled','Scheduled async job for deploying Vm',3,3,'2011-03-24 05:42:44','INFO',0,NULL),(44,'VM.CREATE','Started','Deploying Vm',3,3,'2011-03-24 05:42:44','INFO',43,NULL),(45,'NET.IPASSIGN','Completed','Acquired a public ip: 10.91.30.107',1,3,'2011-03-24 05:42:44','INFO',0,'address=10.91.30.107\nsourceNat=true\ndcId=1'),(46,'ROUTER.CREATE','Completed','successfully created Domain Router : r-6-VM with ip : 10.91.30.107',1,3,'2011-03-24 05:42:45','INFO',0,NULL),(47,'VOLUME.CREATE','Completed','Created volume: i-3-5-VM-ROOT with size: 8192 MB',3,3,'2011-03-24 05:42:46','INFO',0,'id=6\ndoId=-1\ntId=2\ndcId=1\nsize=8192'),(48,'VM.CREATE','Completed','successfully created VM instance : i-3-5-VM',3,3,'2011-03-24 05:42:46','INFO',43,'id=5\nvmName=i-3-5-VM\nsoId=1\ndoId=-1\ntId=2\ndcId=1'),(49,'VM.START','Started','Starting Vm with Id: 5',3,3,'2011-03-24 05:42:46','INFO',0,NULL),(50,'ROUTER.START','Started','Starting Router with Id: 6',1,3,'2011-03-24 05:42:46','INFO',0,NULL),(51,'VM.CREATE','Scheduled','Scheduled async job for deploying Vm',3,3,'2011-03-24 05:42:50','INFO',0,NULL),(52,'VM.CREATE','Started','Deploying Vm',3,3,'2011-03-24 05:42:50','INFO',51,NULL),(53,'ROUTER.START','Completed','successfully started Domain Router: r-6-VM',1,3,'2011-03-24 05:43:20','INFO',0,NULL),(54,'VM.START','Completed','successfully started VM: i-3-5-VM',3,3,'2011-03-24 05:43:26','INFO',49,'id=5\nvmName=i-3-5-VM\nsoId=1\ndoId=-1\ntId=2\ndcId=1'),(55,'VM.CREATE','Scheduled','Scheduled async job for deploying Vm',3,3,'2011-03-24 05:43:27','INFO',0,NULL),(56,'VM.CREATE','Started','Deploying Vm',3,3,'2011-03-24 05:43:27','INFO',55,NULL),(57,'VM.CREATE','Completed','Failed to create VM: i-3-8-VM',3,3,'2011-03-24 05:43:27','ERROR',55,'id=8\nvmName=i-3-8-VM\nsoId=1\ndoId=-1\ntId=2\ndcId=1'),(58,'VM.CREATE','Scheduled','Scheduled async job for deploying Vm',3,3,'2011-03-24 05:43:40','INFO',0,NULL),(59,'VM.CREATE','Started','Deploying Vm',3,3,'2011-03-24 05:43:40','INFO',58,NULL),(60,'VM.CREATE','Completed','Failed to create VM: i-3-9-VM',3,3,'2011-03-24 05:43:40','ERROR',58,'id=9\nvmName=i-3-9-VM\nsoId=1\ndoId=-1\ntId=2\ndcId=1'),(61,'USER.CREATE','Completed','User, test1 for accountId = 4 and domainId = 1 was created.',1,1,'2011-03-24 05:50:07','INFO',0,NULL),(62,'VM.STOP','Completed','Successfully stopped VM instance : i-3-5-VM',1,3,'2011-03-24 05:50:18','INFO',0,'id=5\nvmName=i-3-5-VM\nsoId=1\ntId=2\ndcId=1'),(63,'VM.DESTROY','Completed','Successfully destroyed VM instance : i-3-5-VM',1,3,'2011-03-24 05:50:18','INFO',0,'id=5\nvmName=i-3-5-VM\nsoId=1\ntId=2\ndcId=1'),(64,'VOLUME.DELETE','Completed','Volume i-3-5-VM-ROOT deleted',1,3,'2011-03-24 05:50:18','INFO',0,'id=6'),(65,'ROUTER.STOP','Started','Stopping Router with Id: 6',1,3,'2011-03-24 05:50:18','INFO',0,NULL),(66,'ROUTER.STOP','Completed','successfully stopped Domain Router : r-6-VM',1,3,'2011-03-24 05:50:31','INFO',0,NULL),(67,'ROUTER.DESTROY','Completed','successfully destroyed router : r-6-VM',1,3,'2011-03-24 05:50:32','INFO',0,'id=6'),(68,'NET.IPRELEASE','Completed','released a public ip: 10.91.30.107',1,3,'2011-03-24 05:50:32','INFO',0,'address=10.91.30.107\nsourceNat=true'),(69,'USER.DELETE','Completed','User test (id: 3) for accountId = 3 and domainId = 1 was deleted.',1,1,'2011-03-24 05:50:32','INFO',0,NULL),(70,'VM.CREATE','Scheduled','Scheduled async job for deploying Vm',3,3,'2011-03-24 05:50:55','INFO',0,NULL),(71,'VM.CREATE','Started','Deploying Vm',3,3,'2011-03-24 05:50:55','INFO',70,NULL),(72,'NET.IPASSIGN','Completed','Acquired a public ip: 10.91.30.108',1,3,'2011-03-24 05:50:55','INFO',0,'address=10.91.30.108\nsourceNat=true\ndcId=1'),(73,'ROUTER.CREATE','Completed','successfully created Domain Router : r-11-VM with ip : 10.91.30.108',1,3,'2011-03-24 05:50:56','INFO',0,NULL),(74,'VOLUME.CREATE','Completed','Created volume: i-3-10-VM-ROOT with size: 8192 MB',3,3,'2011-03-24 05:50:56','INFO',0,'id=8\ndoId=-1\ntId=2\ndcId=1\nsize=8192'),(75,'VM.CREATE','Completed','successfully created VM instance : i-3-10-VM',3,3,'2011-03-24 05:50:56','INFO',70,'id=10\nvmName=i-3-10-VM\nsoId=1\ndoId=-1\ntId=2\ndcId=1'),(76,'VM.START','Started','Starting Vm with Id: 10',3,3,'2011-03-24 05:50:56','INFO',0,NULL),(77,'ROUTER.START','Started','Starting Router with Id: 11',1,3,'2011-03-24 05:50:56','INFO',0,NULL),(78,'ROUTER.START','Completed','successfully started Domain Router: r-11-VM',1,3,'2011-03-24 05:51:30','INFO',0,NULL),(79,'VM.START','Completed','successfully started VM: i-3-10-VM',3,3,'2011-03-24 05:51:36','INFO',76,'id=10\nvmName=i-3-10-VM\nsoId=1\ndoId=-1\ntId=2\ndcId=1'),(80,'USER.DELETE','Completed','User test1 (id: 4) for accountId = 4 and domainId = 1 was deleted.',1,1,'2011-03-24 05:52:34','INFO',0,NULL),(81,'USER.CREATE','Completed','User, test1 for accountId = 5 and domainId = 1 was created.',1,1,'2011-03-24 05:52:42','INFO',0,NULL),(82,'VM.CREATE','Scheduled','Scheduled async job for deploying Vm',3,3,'2011-03-24 05:52:51','INFO',0,NULL),(83,'VM.CREATE','Started','Deploying Vm',3,3,'2011-03-24 05:52:51','INFO',82,NULL),(84,'VM.CREATE','Completed','Failed to create VM: i-3-12-VM',3,3,'2011-03-24 05:52:51','ERROR',82,'id=12\nvmName=i-3-12-VM\nsoId=1\ndoId=-1\ntId=2\ndcId=1'),(85,'TEMPLATE.DOWNLOAD.START','Completed','Storage server nfs://10.91.28.6/export/home/idc218/secondary started download of template sd',1,3,'2011-03-24 05:54:12','INFO',0,NULL),(86,'TEMPLATE.DOWNLOAD.SUCCESS','Completed','sd successfully downloaded to storage server nfs://10.91.28.6/export/home/idc218/secondary',1,3,'2011-03-24 05:58:22','INFO',0,NULL),(87,'TEMPLATE.CREATE','Completed','Successfully created template sd',1,3,'2011-03-24 05:58:22','INFO',0,'id=201\ndcId=1\nsize=8589934592'),(88,'USER.LOGIN','Completed','user has logged in',5,5,'2011-03-24 06:18:46','INFO',0,NULL),(89,'USER.LOGIN','Completed','user has logged in',2,2,'2011-03-24 07:00:12','INFO',0,NULL),(90,'USER.DELETE','Completed','User test1 (id: 5) for accountId = 5 and domainId = 1 was deleted.',1,1,'2011-03-24 07:03:30','INFO',0,NULL),(91,'USER.LOGIN','Completed','user has logged in',2,2,'2011-03-25 08:01:15','INFO',0,NULL),(92,'USER.LOGIN','Completed','user has logged in',2,2,'2011-03-26 09:14:06','INFO',0,NULL),(93,'USER.LOGIN','Completed','user has logged in',2,2,'2011-03-28 09:22:26','INFO',0,NULL),(94,'USER.LOGIN','Completed','user has logged in',2,2,'2011-03-28 09:23:04','INFO',0,NULL),(95,'DOMAIN.CREATE','Completed','Domain, D1 created with owner id = 2 and parentId 1',1,2,'2011-03-28 09:26:05','INFO',0,NULL),(96,'USER.CREATE','Completed','User, d1 for accountId = 6 and domainId = 2 was created.',1,1,'2011-03-28 09:26:21','INFO',0,NULL),(97,'USER.LOGOUT','Completed','user has logged out',2,2,'2011-03-28 09:26:24','INFO',0,NULL),(98,'USER.LOGIN','Completed','user has logged in',6,6,'2011-03-28 09:26:37','INFO',0,NULL),(99,'USER.LOGOUT','Completed','user has logged out',6,6,'2011-03-28 09:26:47','INFO',0,NULL),(100,'USER.LOGIN','Completed','user has logged in',2,2,'2011-03-28 09:26:52','INFO',0,NULL),(101,'USER.CREATE','Completed','User, d2 for accountId = 7 and domainId = 1 was created.',1,1,'2011-03-28 09:27:05','INFO',0,NULL),(102,'USER.LOGOUT','Completed','user has logged out',2,2,'2011-03-28 09:27:12','INFO',0,NULL),(103,'USER.LOGIN','Completed','user has logged in',2,2,'2011-03-28 09:27:20','INFO',0,NULL),(104,'USER.CREATE','Completed','User, d3 for accountId = 6 and domainId = 2 was created.',1,1,'2011-03-28 09:27:47','INFO',0,NULL),(105,'USER.LOGOUT','Completed','user has logged out',2,2,'2011-03-28 09:27:50','INFO',0,NULL),(106,'USER.LOGIN','Completed','user has logged in',8,6,'2011-03-28 09:27:56','INFO',0,NULL),(107,'VM.CREATE','Scheduled','Scheduled async job for deploying Vm',8,6,'2011-03-28 09:28:04','INFO',0,NULL),(108,'VM.CREATE','Started','Deploying Vm',8,6,'2011-03-28 09:28:04','INFO',107,NULL),(109,'NET.IPASSIGN','Completed','Acquired a public ip: 10.91.30.109',1,6,'2011-03-28 09:28:04','INFO',0,'address=10.91.30.109\nsourceNat=true\ndcId=1'),(110,'ROUTER.CREATE','Completed','failed to create Domain Router : r-14-VM',1,6,'2011-03-28 09:28:04','ERROR',0,NULL),(111,'NET.IPRELEASE','Completed','released source nat ip 10.91.30.109 since router could not be started',1,6,'2011-03-28 09:28:04','INFO',0,'address=10.91.30.109\nsourceNat=true'),(112,'USER.LOGOUT','Completed','user has logged out',8,6,'2011-03-28 09:28:21','INFO',0,NULL),(113,'USER.LOGIN','Completed','user has logged in',2,2,'2011-03-28 09:28:35','INFO',0,NULL),(114,'USER.CREATE','Completed','User, d4 for accountId = 8 and domainId = 1 was created.',1,1,'2011-03-28 09:32:14','INFO',0,NULL),(115,'USER.LOGOUT','Completed','user has logged out',2,2,'2011-03-28 09:32:17','INFO',0,NULL),(116,'USER.LOGIN','Completed','user has logged in',9,8,'2011-03-28 09:32:20','INFO',0,NULL),(117,'VM.CREATE','Scheduled','Scheduled async job for deploying Vm',9,8,'2011-03-28 09:32:27','INFO',0,NULL),(118,'VM.CREATE','Started','Deploying Vm',9,8,'2011-03-28 09:32:27','INFO',117,NULL),(119,'NET.IPASSIGN','Completed','Acquired a public ip: 10.91.30.109',1,8,'2011-03-28 09:32:27','INFO',0,'address=10.91.30.109\nsourceNat=true\ndcId=1'),(120,'ROUTER.CREATE','Completed','failed to create Domain Router : r-16-VM',1,8,'2011-03-28 09:32:27','ERROR',0,NULL),(121,'NET.IPRELEASE','Completed','released source nat ip 10.91.30.109 since router could not be started',1,8,'2011-03-28 09:32:27','INFO',0,'address=10.91.30.109\nsourceNat=true'),(122,'USER.LOGOUT','Completed','user has logged out',9,8,'2011-03-28 09:32:42','INFO',0,NULL),(123,'USER.LOGIN','Completed','user has logged in',2,2,'2011-03-28 09:32:46','INFO',0,NULL),(124,'VM.CREATE','Scheduled','Scheduled async job for deploying Vm',2,2,'2011-03-28 09:32:53','INFO',0,NULL),(125,'VM.CREATE','Started','Deploying Vm',2,2,'2011-03-28 09:32:53','INFO',124,NULL),(126,'VM.CREATE','Completed','Failed to create VM: i-2-17-VM',2,2,'2011-03-28 09:32:53','ERROR',124,'id=17\nvmName=i-2-17-VM\nsoId=1\ndoId=-1\ntId=2\ndcId=1'),(127,'VM.DESTROY','Scheduled','Scheduled async job for destroying Vm with Id: 3',2,2,'2011-03-28 09:37:00','INFO',0,NULL),(128,'VM.DESTROY','Started','Starting to destroy VM with Id: 3',2,1,'2011-03-28 09:37:00','INFO',127,NULL),(129,'ROUTER.STOP','Scheduled','Scheduled async job for stopping Router with Id: 11',1,1,'2011-03-28 09:37:09','INFO',0,NULL),(130,'ROUTER.STOP','Started','Stopping Router with Id: 11',1,3,'2011-03-28 09:37:09','INFO',129,NULL),(131,'VM.STOP','Completed','Successfully stopped VM instance : i-2-3-VM',2,2,'2011-03-28 09:37:24','INFO',0,'id=3\nvmName=i-2-3-VM\nsoId=1\ntId=2\ndcId=1'),(132,'VM.DESTROY','Completed','successfully destroyed VM instance : i-2-3-VM',2,2,'2011-03-28 09:37:24','INFO',127,'id=3\nvmName=i-2-3-VM\nsoId=1\ntId=2\ndcId=1'),(133,'VOLUME.DELETE','Completed','Volume i-2-3-VM-ROOT deleted',1,2,'2011-03-28 09:37:24','INFO',0,'id=4'),(134,'VM.CREATE','Scheduled','Scheduled async job for deploying Vm',2,2,'2011-03-28 09:37:29','INFO',0,NULL),(135,'VM.CREATE','Started','Deploying Vm',2,2,'2011-03-28 09:37:29','INFO',134,NULL),(136,'VM.CREATE','Completed','Failed to create VM: i-2-18-VM',2,2,'2011-03-28 09:37:29','ERROR',134,'id=18\nvmName=i-2-18-VM\nsoId=1\ndoId=-1\ntId=2\ndcId=1'),(137,'ROUTER.STOP','Completed','successfully stopped Domain Router : r-11-VM',1,3,'2011-03-28 09:37:36','INFO',129,NULL),(138,'VM.CREATE','Scheduled','Scheduled async job for deploying Vm',2,2,'2011-03-28 09:38:15','INFO',0,NULL),(139,'VM.CREATE','Started','Deploying Vm',2,2,'2011-03-28 09:38:15','INFO',138,NULL),(140,'VOLUME.CREATE','Completed','Created volume: i-2-19-VM-ROOT with size: 8192 MB',2,2,'2011-03-28 09:43:32','INFO',0,'id=9\ndoId=-1\ntId=201\ndcId=1\nsize=8192'),(141,'VM.CREATE','Completed','successfully created VM instance : i-2-19-VM',2,2,'2011-03-28 09:43:32','INFO',138,'id=19\nvmName=i-2-19-VM\nsoId=1\ndoId=-1\ntId=201\ndcId=1'),(142,'VM.START','Started','Starting Vm with Id: 19',2,2,'2011-03-28 09:43:32','INFO',0,NULL),(143,'VM.START','Completed','successfully started VM: i-2-19-VM',2,2,'2011-03-28 09:43:39','INFO',142,'id=19\nvmName=i-2-19-VM\nsoId=1\ndoId=-1\ntId=201\ndcId=1'),(144,'VM.DESTROY','Scheduled','Scheduled async job for destroying Vm with Id: 19',2,2,'2011-03-28 09:44:14','INFO',0,NULL),(145,'VM.DESTROY','Started','Starting to destroy VM with Id: 19',2,1,'2011-03-28 09:44:14','INFO',144,NULL),(146,'VM.STOP','Completed','Successfully stopped VM instance : i-2-19-VM',2,2,'2011-03-28 09:44:38','INFO',0,'id=19\nvmName=i-2-19-VM\nsoId=1\ntId=201\ndcId=1'),(147,'VM.DESTROY','Completed','successfully destroyed VM instance : i-2-19-VM',2,2,'2011-03-28 09:44:38','INFO',144,'id=19\nvmName=i-2-19-VM\nsoId=1\ntId=201\ndcId=1'),(148,'VOLUME.DELETE','Completed','Volume i-2-19-VM-ROOT deleted',1,2,'2011-03-28 09:44:38','INFO',0,'id=9'),(149,'USER.CREATE','Completed','User, d5 for accountId = 6 and domainId = 2 was created.',1,1,'2011-03-28 09:45:20','INFO',0,NULL),(150,'USER.CREATE','Completed','User, d5 for accountId = 9 and domainId = 1 was created.',1,1,'2011-03-28 09:45:28','INFO',0,NULL),(151,'USER.CREATE','Completed','User, d6 for accountId = 10 and domainId = 2 was created.',1,1,'2011-03-28 09:45:41','INFO',0,NULL),(152,'USER.LOGOUT','Completed','user has logged out',2,2,'2011-03-28 09:45:52','INFO',0,NULL),(153,'USER.LOGIN','Completed','user has logged in',12,10,'2011-03-28 09:45:58','INFO',0,NULL),(154,'VM.CREATE','Scheduled','Scheduled async job for deploying Vm',12,10,'2011-03-28 09:46:07','INFO',0,NULL),(155,'VM.CREATE','Started','Deploying Vm',12,10,'2011-03-28 09:46:07','INFO',154,NULL),(156,'NET.IPASSIGN','Completed','Acquired a public ip: 10.91.30.101',1,10,'2011-03-28 09:46:07','INFO',0,'address=10.91.30.101\nsourceNat=true\ndcId=1'),(157,'ROUTER.CREATE','Completed','successfully created Domain Router : r-21-VM with ip : 10.91.30.101',1,10,'2011-03-28 09:46:08','INFO',0,NULL),(158,'VOLUME.CREATE','Completed','Created volume: i-10-20-VM-ROOT with size: 8192 MB',12,10,'2011-03-28 09:46:09','INFO',0,'id=11\ndoId=-1\ntId=2\ndcId=1\nsize=8192'),(159,'VM.CREATE','Completed','successfully created VM instance : i-10-20-VM',12,10,'2011-03-28 09:46:09','INFO',154,'id=20\nvmName=i-10-20-VM\nsoId=1\ndoId=-1\ntId=2\ndcId=1'),(160,'VM.START','Started','Starting Vm with Id: 20',12,10,'2011-03-28 09:46:09','INFO',0,NULL),(161,'ROUTER.START','Started','Starting Router with Id: 21',1,10,'2011-03-28 09:46:09','INFO',0,NULL),(162,'ROUTER.START','Completed','successfully started Domain Router: r-21-VM',1,10,'2011-03-28 09:46:44','INFO',0,NULL),(163,'VM.START','Completed','successfully started VM: i-10-20-VM',12,10,'2011-03-28 09:46:50','INFO',160,'id=20\nvmName=i-10-20-VM\nsoId=1\ndoId=-1\ntId=2\ndcId=1'),(164,'USER.LOGOUT','Completed','user has logged out',12,10,'2011-03-28 09:47:17','INFO',0,NULL),(165,'USER.LOGIN','Completed','user has logged in',2,2,'2011-03-28 09:47:22','INFO',0,NULL),(166,'USER.LOGOUT','Completed','user has logged out',2,2,'2011-03-28 09:47:32','INFO',0,NULL),(167,'USER.LOGIN','Completed','user has logged in',6,6,'2011-03-28 09:47:38','INFO',0,NULL),(168,'USER.LOGOUT','Completed','user has logged out',6,6,'2011-03-28 09:47:55','INFO',0,NULL),(169,'USER.LOGIN','Completed','user has logged in',2,2,'2011-03-28 09:47:59','INFO',0,NULL),(170,'USER.LOGOUT','Completed','user has logged out',2,2,'2011-03-28 09:49:13','INFO',0,NULL),(171,'USER.LOGIN','Completed','user has logged in',6,6,'2011-03-28 09:49:29','INFO',0,NULL),(172,'USER.LOGOUT','Completed','user has logged out',6,6,'2011-03-28 09:49:53','INFO',0,NULL),(173,'USER.LOGIN','Completed','user has logged in',12,10,'2011-03-28 09:50:02','INFO',0,NULL),(174,'USER.LOGOUT','Completed','user has logged out',12,10,'2011-03-28 09:50:06','INFO',0,NULL),(175,'USER.LOGIN','Completed','user has logged in',2,2,'2011-03-28 09:50:10','INFO',0,NULL),(176,'ROUTER.STOP','Started','Stopping Router with Id: 4',1,2,'2011-03-28 10:02:05','INFO',0,NULL),(177,'ROUTER.STOP','Completed','successfully stopped Domain Router : r-4-VM',1,2,'2011-03-28 10:02:18','INFO',0,NULL),(178,'USER.LOGOUT','Completed','user has logged out',2,2,'2011-03-28 10:05:17','INFO',0,NULL),(179,'USER.LOGIN','Completed','user has logged in',6,6,'2011-03-28 10:05:22','INFO',0,NULL),(180,'USER.LOGOUT','Completed','user has logged out',6,6,'2011-03-28 10:05:27','INFO',0,NULL),(181,'USER.LOGIN','Completed','user has logged in',2,2,'2011-03-28 10:42:28','INFO',0,NULL),(182,'USER.CREATE','Completed','User, d7 for accountId = 11 and domainId = 1 was created.',1,1,'2011-03-28 10:43:06','INFO',0,NULL),(183,'USER.LOGIN','Completed','user has logged in',6,6,'2011-03-28 10:50:32','INFO',0,NULL),(184,'USER.LOGIN','Completed','user has logged in',2,2,'2011-03-28 11:48:18','INFO',0,NULL),(185,'VOLUME.CREATE','Scheduled','Scheduled async job for creating volume',2,2,'2011-03-28 11:48:28','INFO',0,NULL),(186,'VOLUME.CREATE','Started','Creating volume',2,2,'2011-03-28 11:48:28','INFO',185,NULL),(187,'VOLUME.CREATE','Completed','Created volume: vv with size: 5120 MB in pool: idc-ss',2,2,'2011-03-28 11:48:28','INFO',185,'id=12\ndoId=5\ntId=-1\ndcId=1\nsize=5120'),(188,'USER.LOGOUT','Completed','user has logged out',2,2,'2011-03-28 11:49:01','INFO',0,NULL),(189,'USER.LOGIN','Completed','user has logged in',6,6,'2011-03-28 11:49:07','INFO',0,NULL),(190,'VOLUME.CREATE','Scheduled','Scheduled async job for creating volume',6,6,'2011-03-28 11:49:13','INFO',0,NULL),(191,'VOLUME.CREATE','Started','Creating volume',6,6,'2011-03-28 11:49:13','INFO',190,NULL),(192,'VOLUME.CREATE','Completed','Created volume: v1 with size: 5120 MB in pool: idc-ss',6,6,'2011-03-28 11:49:14','INFO',190,'id=13\ndoId=5\ntId=-1\ndcId=1\nsize=5120'),(193,'USER.LOGOUT','Completed','user has logged out',6,6,'2011-03-28 11:50:04','INFO',0,NULL),(194,'USER.LOGIN','Completed','user has logged in',2,2,'2011-03-28 11:50:08','INFO',0,NULL),(195,'VM.CREATE','Scheduled','Scheduled async job for deploying Vm',2,2,'2011-03-28 11:50:18','INFO',0,NULL),(196,'VM.CREATE','Started','Deploying Vm',2,2,'2011-03-28 11:50:18','INFO',195,NULL),(197,'VM.CREATE','Completed','Failed to create VM: i-2-22-VM',2,2,'2011-03-28 11:50:18','ERROR',195,'id=22\nvmName=i-2-22-VM\nsoId=1\ndoId=5\ntId=2\ndcId=1'),(198,'VM.DESTROY','Scheduled','Scheduled async job for destroying Vm with Id: 20',2,10,'2011-03-28 11:50:25','INFO',0,NULL),(199,'VM.DESTROY','Started','Starting to destroy VM with Id: 20',2,1,'2011-03-28 11:50:25','INFO',198,NULL),(200,'VM.STOP','Completed','Successfully stopped VM instance : i-10-20-VM',2,10,'2011-03-28 11:50:49','INFO',0,'id=20\nvmName=i-10-20-VM\nsoId=1\ntId=2\ndcId=1'),(201,'VM.DESTROY','Completed','successfully destroyed VM instance : i-10-20-VM',2,10,'2011-03-28 11:50:49','INFO',198,'id=20\nvmName=i-10-20-VM\nsoId=1\ntId=2\ndcId=1'),(202,'VOLUME.DELETE','Completed','Volume i-10-20-VM-ROOT deleted',1,10,'2011-03-28 11:50:49','INFO',0,'id=11'),(203,'VM.CREATE','Scheduled','Scheduled async job for deploying Vm',2,2,'2011-03-28 11:51:18','INFO',0,NULL),(204,'VM.CREATE','Started','Deploying Vm',2,2,'2011-03-28 11:51:18','INFO',203,NULL),(205,'VOLUME.CREATE','Completed','Created volume: i-2-23-VM-ROOT with size: 8192 MB',2,2,'2011-03-28 11:51:19','INFO',0,'id=14\ndoId=-1\ntId=2\ndcId=1\nsize=8192'),(206,'VOLUME.CREATE','Completed','Created volume: i-2-23-VM-DATA with size: 5120 MB',2,2,'2011-03-28 11:51:19','INFO',0,'id=15\ndoId=5\ntId=-1\ndcId=1\nsize=5120'),(207,'VM.CREATE','Completed','successfully created VM instance : i-2-23-VM',2,2,'2011-03-28 11:51:19','INFO',203,'id=23\nvmName=i-2-23-VM\nsoId=1\ndoId=5\ntId=2\ndcId=1'),(208,'VM.START','Started','Starting Vm with Id: 23',2,2,'2011-03-28 11:51:19','INFO',0,NULL),(209,'ROUTER.START','Started','Starting Router with Id: 4',1,2,'2011-03-28 11:51:19','INFO',0,NULL),(210,'ROUTER.START','Completed','successfully started Domain Router: r-4-VM',1,2,'2011-03-28 11:51:53','INFO',0,NULL),(211,'VM.START','Completed','successfully started VM: i-2-23-VM',2,2,'2011-03-28 11:51:59','INFO',208,'id=23\nvmName=i-2-23-VM\nsoId=1\ndoId=-1\ntId=2\ndcId=1'),(212,'VOLUME.DETACH','Scheduled','Scheduled async job for detaching volume: 15 from Vm: 23',1,2,'2011-03-28 11:53:36','INFO',0,NULL),(213,'VOLUME.DETACH','Started','Detaching volume: 15 from Vm: 23',1,2,'2011-03-28 11:53:36','INFO',212,NULL),(214,'VOLUME.DETACH','Completed','Volume: i-2-23-VM-DATA successfully detached from VM: i-2-23-VM',1,2,'2011-03-28 11:53:37','INFO',212,NULL),(215,'VOLUME.ATTACH','Scheduled','Scheduled async job for attaching volume: 15 to Vm: 23',1,2,'2011-03-28 11:54:07','INFO',0,NULL),(216,'VOLUME.ATTACH','Started','Attaching volume: 15 to Vm: 23',1,2,'2011-03-28 11:54:07','INFO',215,NULL),(217,'VOLUME.ATTACH','Scheduled','Scheduled async job for attaching volume: 15 to Vm: 23',1,2,'2011-03-28 11:54:40','INFO',0,NULL),(218,'VOLUME.ATTACH','Started','Attaching volume: 15 to Vm: 23',1,2,'2011-03-28 11:54:40','INFO',217,NULL),(219,'VOLUME.ATTACH','Scheduled','Scheduled async job for attaching volume: 15 to Vm: 23',1,2,'2011-03-28 11:57:58','INFO',0,NULL),(220,'VOLUME.ATTACH','Started','Attaching volume: 15 to Vm: 23',1,2,'2011-03-28 11:57:58','INFO',219,NULL),(221,'VOLUME.ATTACH','Completed','Volume: i-2-23-VM-DATA successfully attached to VM: i-2-23-VM',1,2,'2011-03-28 11:57:58','INFO',219,NULL),(222,'VOLUME.DETACH','Scheduled','Scheduled async job for detaching volume: 15 from Vm: 23',1,2,'2011-03-28 11:58:19','INFO',0,NULL),(223,'VOLUME.DETACH','Started','Detaching volume: 15 from Vm: 23',1,2,'2011-03-28 11:58:19','INFO',222,NULL),(224,'VOLUME.DETACH','Completed','Failed to detach volume: i-2-23-VM-DATA from VM: i-2-23- V M; com.cloud.utils.exception.CloudRuntimeException: Unable to make a connection to the server 10.91.28.19',1,2,'2011-03-28 11:58:25','ERROR',222,NULL),(225,'ROUTER.STOP','Started','Stopping Router with Id: 21',1,10,'2011-03-28 12:02:04','INFO',0,NULL),(226,'ROUTER.STOP','Completed','successfully stopped Domain Router : r-21-VM',1,10,'2011-03-28 12:02:19','INFO',0,NULL),(227,'USER.LOGIN','Completed','user has logged in',2,2,'2011-03-29 08:26:41','INFO',0,NULL),(228,'USER.LOGIN','Completed','user has logged in',2,2,'2011-03-29 09:41:14','INFO',0,NULL),(229,'VOLUME.DELETE','Completed','Volume v1 deleted',2,6,'2011-03-29 09:42:22','INFO',0,'id=13'),(230,'TEMPLATE.DELETE','Scheduled','Scheduled async job for deleting template with Id: 201',2,3,'2011-03-29 09:42:43','INFO',0,NULL),(231,'TEMPLATE.DELETE','Started','Deleting template with Id: 201',2,3,'2011-03-29 09:42:43','INFO',230,NULL),(232,'TEMPLATE.DELETE','Completed','Template sd succesfully deleted.',2,3,'2011-03-29 09:42:43','INFO',230,'id=201\ndcId=1'),(233,'SNAPSHOT.CREATE','Completed','Backed up snapshot id: 1 to secondary for volume 14',2,2,'2011-03-29 09:43:53','INFO',0,'id=1\nssName=i-2-23-VM_i-2-23-VM-ROOT_20110329094214\nsize=8589934592\ndcId=1'),(234,'NET.IPASSIGN','Completed','Assigned a public IP address: 10.91.30.107',2,2,'2011-03-29 09:48:16','INFO',0,'address=10.91.30.107\nsourceNat=false\ndcId=1'),(235,'NET.IPRELEASE','Completed','released a public ip: 10.91.30.107',2,2,'2011-03-29 09:48:24','INFO',0,'address=10.91.30.107\nsourceNat=false'),(236,'NET.IPASSIGN','Completed','Assigned a public IP address: 10.91.30.100',2,2,'2011-03-29 09:48:30','INFO',0,'address=10.91.30.100\nsourceNat=false\ndcId=1');
/*!40000 ALTER TABLE `event` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ext_lun_alloc`
--

DROP TABLE IF EXISTS `ext_lun_alloc`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

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
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ext_lun_details` (
  `id` bigint(20) unsigned NOT NULL auto_increment COMMENT 'id',
  `ext_lun_id` bigint(20) unsigned NOT NULL COMMENT 'lun id',
  `tag` varchar(255) default NULL COMMENT 'tags associated with this vm',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`),
  KEY `fk_ext_lun_details__ext_lun_id` (`ext_lun_id`),
  CONSTRAINT `fk_ext_lun_details__ext_lun_id` FOREIGN KEY (`ext_lun_id`) REFERENCES `ext_lun_alloc` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

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
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `guest_os` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `category_id` bigint(20) unsigned NOT NULL,
  `name` varchar(255) NOT NULL,
  `display_name` varchar(255) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `fk_guest_os__category_id` (`category_id`),
  CONSTRAINT `fk_guest_os__category_id` FOREIGN KEY (`category_id`) REFERENCES `guest_os_category` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=61 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

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
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `guest_os_category` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

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
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `host`
--

LOCK TABLES `host` WRITE;
/*!40000 ALTER TABLE `host` DISABLE KEYS */;
INSERT INTO `host` VALUES (1,'nfs://10.91.28.6/export/home/idc218/secondary','Up','SecondaryStorage','10.91.28.6','255.255.255.0','06:01:dd:c7:00:01','10.91.28.104','255.255.255.0','06:01:dd:c7:00:01',NULL,NULL,NULL,NULL,'10.91.30.104','255.255.255.0','06:81:dd:c7:00:01',NULL,1,1,NULL,NULL,'nfs://10.91.28.6/export/home/idc218/secondary',NULL,'None',0,NULL,'2.1.7.145',16210,'/mnt/SecStorage/1645b82a',1969009721344,NULL,'nfs://10.91.28.6/export/home/idc218/secondary',1,0,0,1270891360,47052492778003,'2011-03-23 19:41:09','2011-03-23 18:00:49',NULL),(2,'idc-ktxen19','Up','Routing','10.91.28.19','255.255.255.0','bc:30:5b:a0:c6:3f','10.91.28.19','255.255.255.0','bc:30:5b:a0:c6:3f','10.91.28.19','bc:30:5b:a0:c6:3f','255.255.255.0',1,NULL,NULL,NULL,NULL,1,1,2,2992,'iqn.2011-01.com.example:8d549784',NULL,'XenServer',3151124928,'com.cloud.hypervisor.xen.resource.XenServer56Resource','2.1.7.145',48643,NULL,NULL,'xen-3.0-x86_64 , xen-3.0-x86_32p , hvm-3.0-x86_32 , hvm-3.0-x86_32p , hvm-3.0-x86_64','d3a48dfe-88dd-4b2c-af6b-be301e574239',1,1,0,1270891342,222119676852735,'2011-03-28 11:58:59','2011-03-23 18:08:22',NULL),(3,'v-2-VM','Up','ConsoleProxy','10.91.28.108','255.255.255.0','06:01:93:ec:00:03','10.91.28.108','255.255.255.0','06:01:93:ec:00:03',NULL,NULL,NULL,NULL,'10.91.30.106','255.255.255.0','06:81:93:ec:00:03',80,1,1,NULL,NULL,'NoIqn',NULL,NULL,0,NULL,'2.1.7.145',3,NULL,NULL,NULL,'Proxy.2-ConsoleProxyResource',1,0,0,1270891359,47052492778003,'2011-03-23 19:41:09','2011-03-23 18:17:06',NULL),(4,'xenserver-shweta-16','Removed','Routing','10.91.28.16','255.255.255.0','00:25:90:04:09:62','10.91.28.16','255.255.255.0','00:25:90:04:09:62','10.91.28.16','00:25:90:04:09:62','255.255.255.0',NULL,NULL,NULL,NULL,NULL,1,1,4,2933,'iqn.2005-03.org.open-iscsi:602594bafb77',NULL,'XenServer',7539573888,'com.cloud.hypervisor.xen.resource.XenServer56Resource','2.1.7.145',5940,NULL,NULL,'xen-3.0-x86_64 , xen-3.0-x86_32p , hvm-3.0-x86_32 , hvm-3.0-x86_32p , hvm-3.0-x86_64',NULL,1,1,0,1270546930,NULL,'2011-03-25 08:02:13','2011-03-24 07:00:59','2011-03-25 08:02:13');
/*!40000 ALTER TABLE `host` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `host_details`
--

DROP TABLE IF EXISTS `host_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `host_details` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `host_id` bigint(20) unsigned NOT NULL COMMENT 'host id',
  `name` varchar(255) NOT NULL,
  `value` varchar(255) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `fk_host_details__host_id` (`host_id`),
  CONSTRAINT `fk_host_details__host_id` FOREIGN KEY (`host_id`) REFERENCES `host` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=106 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `host_details`
--

LOCK TABLES `host_details` WRITE;
/*!40000 ALTER TABLE `host_details` DISABLE KEYS */;
INSERT INTO `host_details` VALUES (1,1,'mount.parent','dummy'),(2,1,'mount.path','dummy'),(3,1,'orig.url','nfs://10.91.28.6/export/home/idc218/secondary'),(89,2,'com.cloud.network.NetworkEnums.RouterPrivateIpStrategy','DcGlobal'),(90,2,'public.network.device','Pool-wide network associated with eth0'),(91,2,'private.network.device','Pool-wide network associated with eth0'),(92,2,'Hypervisor.Version','3.4.2'),(93,2,'Host.OS','XenServer'),(94,2,'Host.OS.Kernel.Version','2.6.27.42-0.1.1.xs5.6.0.44.111158xen'),(95,2,'wait','1800'),(96,2,'storage.network.device2','cloud-stor2'),(97,2,'password','password'),(98,2,'storage.network.device1','cloud-stor1'),(99,2,'url','10.91.28.19'),(100,2,'username','root'),(101,2,'pool','0260e1c4-e46c-3686-443d-ccef5fa1b220'),(102,2,'guest.network.device','Pool-wide network associated with eth0'),(103,2,'can_bridge_firewall','false'),(104,2,'Host.OS.Version','5.6.0'),(105,2,'instance.name','VM');
/*!40000 ALTER TABLE `host_details` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `host_pod_ref`
--

DROP TABLE IF EXISTS `host_pod_ref`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `host_pod_ref`
--

LOCK TABLES `host_pod_ref` WRITE;
/*!40000 ALTER TABLE `host_pod_ref` DISABLE KEYS */;
INSERT INTO `host_pod_ref` VALUES (1,'Default',1,'10.91.28.1','10.91.28.1',24,'10.91.28.100-10.91.28.109');
/*!40000 ALTER TABLE `host_pod_ref` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `host_tags`
--

DROP TABLE IF EXISTS `host_tags`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `host_tags` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `host_id` bigint(20) unsigned NOT NULL COMMENT 'host id',
  `tag` varchar(255) NOT NULL,
  PRIMARY KEY  (`id`),
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
-- Table structure for table `ip_forwarding`
--

DROP TABLE IF EXISTS `ip_forwarding`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ip_forwarding`
--

LOCK TABLES `ip_forwarding` WRITE;
/*!40000 ALTER TABLE `ip_forwarding` DISABLE KEYS */;
/*!40000 ALTER TABLE `ip_forwarding` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `launch_permission`
--

DROP TABLE IF EXISTS `launch_permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `launch_permission` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `template_id` bigint(20) unsigned NOT NULL,
  `account_id` bigint(20) unsigned NOT NULL,
  PRIMARY KEY  (`id`),
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
-- Table structure for table `load_balancer`
--

DROP TABLE IF EXISTS `load_balancer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

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
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `load_balancer_vm_map` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `load_balancer_id` bigint(20) unsigned NOT NULL,
  `instance_id` bigint(20) unsigned NOT NULL,
  `pending` tinyint(1) unsigned NOT NULL default '0' COMMENT 'whether the vm is being applied to the load balancer (pending=1) or has already been applied (pending=0)',
  PRIMARY KEY  (`id`)
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
-- Table structure for table `mshost`
--

DROP TABLE IF EXISTS `mshost`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mshost`
--

LOCK TABLES `mshost` WRITE;
/*!40000 ALTER TABLE `mshost` DISABLE KEYS */;
INSERT INTO `mshost` VALUES (1,47052492778003,'idc-2.1.7-mgmt1','2.1.7.145','10.91.28.53',9090,'2011-03-29 09:59:17',NULL,0),(2,222119676852735,'idc-2.1.7-mgmt1','2.1.7.145','10.91.28.59',9090,'2011-03-29 09:59:17',NULL,0);
/*!40000 ALTER TABLE `mshost` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `netapp_lun`
--

DROP TABLE IF EXISTS `netapp_lun`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
  `id` bigint(20) unsigned NOT NULL auto_increment COMMENT 'id',
  `name` varchar(255) NOT NULL COMMENT 'name for the pool',
  `algorithm` varchar(255) NOT NULL COMMENT 'algorithm',
  PRIMARY KEY  (`id`),
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
/*!40101 SET character_set_client = @saved_cs_client */;

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
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

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
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

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
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

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
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `network_rule_config` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `security_group_id` bigint(20) unsigned NOT NULL,
  `public_port` varchar(10) default NULL,
  `private_port` varchar(10) default NULL,
  `protocol` varchar(16) NOT NULL default 'TCP',
  `create_status` varchar(32) default NULL COMMENT 'rule creation status',
  PRIMARY KEY  (`id`)
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
-- Table structure for table `op_dc_ip_address_alloc`
--

DROP TABLE IF EXISTS `op_dc_ip_address_alloc`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `op_dc_ip_address_alloc`
--

LOCK TABLES `op_dc_ip_address_alloc` WRITE;
/*!40000 ALTER TABLE `op_dc_ip_address_alloc` DISABLE KEYS */;
INSERT INTO `op_dc_ip_address_alloc` VALUES (1,'10.91.28.100',1,1,NULL,NULL),(2,'10.91.28.101',1,1,NULL,NULL),(3,'10.91.28.102',1,1,NULL,NULL),(4,'10.91.28.103',1,1,NULL,NULL),(5,'10.91.28.104',1,1,1,'2011-03-23 18:15:52'),(6,'10.91.28.105',1,1,NULL,NULL),(7,'10.91.28.106',1,1,NULL,NULL),(8,'10.91.28.107',1,1,NULL,NULL),(9,'10.91.28.108',1,1,2,'2011-03-23 18:15:53'),(10,'10.91.28.109',1,1,NULL,NULL);
/*!40000 ALTER TABLE `op_dc_ip_address_alloc` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `op_dc_link_local_ip_address_alloc`
--

DROP TABLE IF EXISTS `op_dc_link_local_ip_address_alloc`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `op_dc_link_local_ip_address_alloc` (
  `id` bigint(20) unsigned NOT NULL auto_increment COMMENT 'primary key',
  `ip_address` varchar(15) NOT NULL COMMENT 'ip address',
  `data_center_id` bigint(20) unsigned NOT NULL COMMENT 'data center it belongs to',
  `pod_id` bigint(20) unsigned NOT NULL COMMENT 'pod it belongs to',
  `instance_id` bigint(20) unsigned default NULL COMMENT 'instance id',
  `taken` datetime default NULL COMMENT 'Date taken',
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1022 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `op_dc_link_local_ip_address_alloc`
--

LOCK TABLES `op_dc_link_local_ip_address_alloc` WRITE;
/*!40000 ALTER TABLE `op_dc_link_local_ip_address_alloc` DISABLE KEYS */;
INSERT INTO `op_dc_link_local_ip_address_alloc` VALUES (1,'169.254.0.2',1,1,NULL,NULL),(2,'169.254.0.3',1,1,NULL,NULL),(3,'169.254.0.4',1,1,NULL,NULL),(4,'169.254.0.5',1,1,NULL,NULL),(5,'169.254.0.6',1,1,NULL,NULL),(6,'169.254.0.7',1,1,NULL,NULL),(7,'169.254.0.8',1,1,NULL,NULL),(8,'169.254.0.9',1,1,NULL,NULL),(9,'169.254.0.10',1,1,NULL,NULL),(10,'169.254.0.11',1,1,NULL,NULL),(11,'169.254.0.12',1,1,NULL,NULL),(12,'169.254.0.13',1,1,NULL,NULL),(13,'169.254.0.14',1,1,NULL,NULL),(14,'169.254.0.15',1,1,NULL,NULL),(15,'169.254.0.16',1,1,NULL,NULL),(16,'169.254.0.17',1,1,NULL,NULL),(17,'169.254.0.18',1,1,NULL,NULL),(18,'169.254.0.19',1,1,NULL,NULL),(19,'169.254.0.20',1,1,NULL,NULL),(20,'169.254.0.21',1,1,NULL,NULL),(21,'169.254.0.22',1,1,NULL,NULL),(22,'169.254.0.23',1,1,NULL,NULL),(23,'169.254.0.24',1,1,NULL,NULL),(24,'169.254.0.25',1,1,NULL,NULL),(25,'169.254.0.26',1,1,NULL,NULL),(26,'169.254.0.27',1,1,NULL,NULL),(27,'169.254.0.28',1,1,NULL,NULL),(28,'169.254.0.29',1,1,NULL,NULL),(29,'169.254.0.30',1,1,NULL,NULL),(30,'169.254.0.31',1,1,NULL,NULL),(31,'169.254.0.32',1,1,NULL,NULL),(32,'169.254.0.33',1,1,NULL,NULL),(33,'169.254.0.34',1,1,NULL,NULL),(34,'169.254.0.35',1,1,NULL,NULL),(35,'169.254.0.36',1,1,NULL,NULL),(36,'169.254.0.37',1,1,NULL,NULL),(37,'169.254.0.38',1,1,NULL,NULL),(38,'169.254.0.39',1,1,NULL,NULL),(39,'169.254.0.40',1,1,NULL,NULL),(40,'169.254.0.41',1,1,NULL,NULL),(41,'169.254.0.42',1,1,NULL,NULL),(42,'169.254.0.43',1,1,NULL,NULL),(43,'169.254.0.44',1,1,NULL,NULL),(44,'169.254.0.45',1,1,NULL,NULL),(45,'169.254.0.46',1,1,NULL,NULL),(46,'169.254.0.47',1,1,NULL,NULL),(47,'169.254.0.48',1,1,NULL,NULL),(48,'169.254.0.49',1,1,NULL,NULL),(49,'169.254.0.50',1,1,NULL,NULL),(50,'169.254.0.51',1,1,NULL,NULL),(51,'169.254.0.52',1,1,NULL,NULL),(52,'169.254.0.53',1,1,NULL,NULL),(53,'169.254.0.54',1,1,NULL,NULL),(54,'169.254.0.55',1,1,NULL,NULL),(55,'169.254.0.56',1,1,NULL,NULL),(56,'169.254.0.57',1,1,NULL,NULL),(57,'169.254.0.58',1,1,NULL,NULL),(58,'169.254.0.59',1,1,NULL,NULL),(59,'169.254.0.60',1,1,NULL,NULL),(60,'169.254.0.61',1,1,NULL,NULL),(61,'169.254.0.62',1,1,NULL,NULL),(62,'169.254.0.63',1,1,NULL,NULL),(63,'169.254.0.64',1,1,NULL,NULL),(64,'169.254.0.65',1,1,NULL,NULL),(65,'169.254.0.66',1,1,NULL,NULL),(66,'169.254.0.67',1,1,NULL,NULL),(67,'169.254.0.68',1,1,NULL,NULL),(68,'169.254.0.69',1,1,NULL,NULL),(69,'169.254.0.70',1,1,NULL,NULL),(70,'169.254.0.71',1,1,NULL,NULL),(71,'169.254.0.72',1,1,NULL,NULL),(72,'169.254.0.73',1,1,NULL,NULL),(73,'169.254.0.74',1,1,NULL,NULL),(74,'169.254.0.75',1,1,NULL,NULL),(75,'169.254.0.76',1,1,NULL,NULL),(76,'169.254.0.77',1,1,NULL,NULL),(77,'169.254.0.78',1,1,NULL,NULL),(78,'169.254.0.79',1,1,NULL,NULL),(79,'169.254.0.80',1,1,NULL,NULL),(80,'169.254.0.81',1,1,NULL,NULL),(81,'169.254.0.82',1,1,NULL,NULL),(82,'169.254.0.83',1,1,NULL,NULL),(83,'169.254.0.84',1,1,NULL,NULL),(84,'169.254.0.85',1,1,NULL,NULL),(85,'169.254.0.86',1,1,NULL,NULL),(86,'169.254.0.87',1,1,NULL,NULL),(87,'169.254.0.88',1,1,NULL,NULL),(88,'169.254.0.89',1,1,NULL,NULL),(89,'169.254.0.90',1,1,NULL,NULL),(90,'169.254.0.91',1,1,NULL,NULL),(91,'169.254.0.92',1,1,NULL,NULL),(92,'169.254.0.93',1,1,NULL,NULL),(93,'169.254.0.94',1,1,NULL,NULL),(94,'169.254.0.95',1,1,NULL,NULL),(95,'169.254.0.96',1,1,NULL,NULL),(96,'169.254.0.97',1,1,NULL,NULL),(97,'169.254.0.98',1,1,NULL,NULL),(98,'169.254.0.99',1,1,NULL,NULL),(99,'169.254.0.100',1,1,NULL,NULL),(100,'169.254.0.101',1,1,NULL,NULL),(101,'169.254.0.102',1,1,NULL,NULL),(102,'169.254.0.103',1,1,NULL,NULL),(103,'169.254.0.104',1,1,2,'2011-03-23 18:15:53'),(104,'169.254.0.105',1,1,NULL,NULL),(105,'169.254.0.106',1,1,NULL,NULL),(106,'169.254.0.107',1,1,NULL,NULL),(107,'169.254.0.108',1,1,NULL,NULL),(108,'169.254.0.109',1,1,NULL,NULL),(109,'169.254.0.110',1,1,NULL,NULL),(110,'169.254.0.111',1,1,NULL,NULL),(111,'169.254.0.112',1,1,NULL,NULL),(112,'169.254.0.113',1,1,NULL,NULL),(113,'169.254.0.114',1,1,NULL,NULL),(114,'169.254.0.115',1,1,NULL,NULL),(115,'169.254.0.116',1,1,NULL,NULL),(116,'169.254.0.117',1,1,NULL,NULL),(117,'169.254.0.118',1,1,NULL,NULL),(118,'169.254.0.119',1,1,NULL,NULL),(119,'169.254.0.120',1,1,NULL,NULL),(120,'169.254.0.121',1,1,NULL,NULL),(121,'169.254.0.122',1,1,NULL,NULL),(122,'169.254.0.123',1,1,NULL,NULL),(123,'169.254.0.124',1,1,NULL,NULL),(124,'169.254.0.125',1,1,NULL,NULL),(125,'169.254.0.126',1,1,NULL,NULL),(126,'169.254.0.127',1,1,NULL,NULL),(127,'169.254.0.128',1,1,NULL,NULL),(128,'169.254.0.129',1,1,NULL,NULL),(129,'169.254.0.130',1,1,NULL,NULL),(130,'169.254.0.131',1,1,NULL,NULL),(131,'169.254.0.132',1,1,NULL,NULL),(132,'169.254.0.133',1,1,NULL,NULL),(133,'169.254.0.134',1,1,NULL,NULL),(134,'169.254.0.135',1,1,NULL,NULL),(135,'169.254.0.136',1,1,NULL,NULL),(136,'169.254.0.137',1,1,NULL,NULL),(137,'169.254.0.138',1,1,NULL,NULL),(138,'169.254.0.139',1,1,NULL,NULL),(139,'169.254.0.140',1,1,NULL,NULL),(140,'169.254.0.141',1,1,NULL,NULL),(141,'169.254.0.142',1,1,NULL,NULL),(142,'169.254.0.143',1,1,NULL,NULL),(143,'169.254.0.144',1,1,NULL,NULL),(144,'169.254.0.145',1,1,NULL,NULL),(145,'169.254.0.146',1,1,NULL,NULL),(146,'169.254.0.147',1,1,NULL,NULL),(147,'169.254.0.148',1,1,NULL,NULL),(148,'169.254.0.149',1,1,NULL,NULL),(149,'169.254.0.150',1,1,NULL,NULL),(150,'169.254.0.151',1,1,NULL,NULL),(151,'169.254.0.152',1,1,NULL,NULL),(152,'169.254.0.153',1,1,NULL,NULL),(153,'169.254.0.154',1,1,NULL,NULL),(154,'169.254.0.155',1,1,NULL,NULL),(155,'169.254.0.156',1,1,NULL,NULL),(156,'169.254.0.157',1,1,NULL,NULL),(157,'169.254.0.158',1,1,NULL,NULL),(158,'169.254.0.159',1,1,NULL,NULL),(159,'169.254.0.160',1,1,NULL,NULL),(160,'169.254.0.161',1,1,NULL,NULL),(161,'169.254.0.162',1,1,NULL,NULL),(162,'169.254.0.163',1,1,NULL,NULL),(163,'169.254.0.164',1,1,NULL,NULL),(164,'169.254.0.165',1,1,NULL,NULL),(165,'169.254.0.166',1,1,NULL,NULL),(166,'169.254.0.167',1,1,NULL,NULL),(167,'169.254.0.168',1,1,NULL,NULL),(168,'169.254.0.169',1,1,NULL,NULL),(169,'169.254.0.170',1,1,NULL,NULL),(170,'169.254.0.171',1,1,NULL,NULL),(171,'169.254.0.172',1,1,NULL,NULL),(172,'169.254.0.173',1,1,NULL,NULL),(173,'169.254.0.174',1,1,NULL,NULL),(174,'169.254.0.175',1,1,NULL,NULL),(175,'169.254.0.176',1,1,NULL,NULL),(176,'169.254.0.177',1,1,NULL,NULL),(177,'169.254.0.178',1,1,NULL,NULL),(178,'169.254.0.179',1,1,NULL,NULL),(179,'169.254.0.180',1,1,NULL,NULL),(180,'169.254.0.181',1,1,NULL,NULL),(181,'169.254.0.182',1,1,NULL,NULL),(182,'169.254.0.183',1,1,NULL,NULL),(183,'169.254.0.184',1,1,NULL,NULL),(184,'169.254.0.185',1,1,NULL,NULL),(185,'169.254.0.186',1,1,NULL,NULL),(186,'169.254.0.187',1,1,NULL,NULL),(187,'169.254.0.188',1,1,NULL,NULL),(188,'169.254.0.189',1,1,NULL,NULL),(189,'169.254.0.190',1,1,NULL,NULL),(190,'169.254.0.191',1,1,NULL,NULL),(191,'169.254.0.192',1,1,NULL,NULL),(192,'169.254.0.193',1,1,NULL,NULL),(193,'169.254.0.194',1,1,NULL,NULL),(194,'169.254.0.195',1,1,NULL,NULL),(195,'169.254.0.196',1,1,NULL,NULL),(196,'169.254.0.197',1,1,NULL,NULL),(197,'169.254.0.198',1,1,NULL,NULL),(198,'169.254.0.199',1,1,NULL,NULL),(199,'169.254.0.200',1,1,NULL,NULL),(200,'169.254.0.201',1,1,NULL,NULL),(201,'169.254.0.202',1,1,NULL,NULL),(202,'169.254.0.203',1,1,NULL,NULL),(203,'169.254.0.204',1,1,NULL,NULL),(204,'169.254.0.205',1,1,NULL,NULL),(205,'169.254.0.206',1,1,NULL,NULL),(206,'169.254.0.207',1,1,NULL,NULL),(207,'169.254.0.208',1,1,NULL,NULL),(208,'169.254.0.209',1,1,NULL,NULL),(209,'169.254.0.210',1,1,NULL,NULL),(210,'169.254.0.211',1,1,NULL,NULL),(211,'169.254.0.212',1,1,NULL,NULL),(212,'169.254.0.213',1,1,NULL,NULL),(213,'169.254.0.214',1,1,NULL,NULL),(214,'169.254.0.215',1,1,NULL,NULL),(215,'169.254.0.216',1,1,NULL,NULL),(216,'169.254.0.217',1,1,NULL,NULL),(217,'169.254.0.218',1,1,NULL,NULL),(218,'169.254.0.219',1,1,NULL,NULL),(219,'169.254.0.220',1,1,NULL,NULL),(220,'169.254.0.221',1,1,NULL,NULL),(221,'169.254.0.222',1,1,NULL,NULL),(222,'169.254.0.223',1,1,NULL,NULL),(223,'169.254.0.224',1,1,NULL,NULL),(224,'169.254.0.225',1,1,NULL,NULL),(225,'169.254.0.226',1,1,NULL,NULL),(226,'169.254.0.227',1,1,NULL,NULL),(227,'169.254.0.228',1,1,NULL,NULL),(228,'169.254.0.229',1,1,NULL,NULL),(229,'169.254.0.230',1,1,NULL,NULL),(230,'169.254.0.231',1,1,NULL,NULL),(231,'169.254.0.232',1,1,NULL,NULL),(232,'169.254.0.233',1,1,NULL,NULL),(233,'169.254.0.234',1,1,NULL,NULL),(234,'169.254.0.235',1,1,NULL,NULL),(235,'169.254.0.236',1,1,NULL,NULL),(236,'169.254.0.237',1,1,NULL,NULL),(237,'169.254.0.238',1,1,NULL,NULL),(238,'169.254.0.239',1,1,NULL,NULL),(239,'169.254.0.240',1,1,NULL,NULL),(240,'169.254.0.241',1,1,1,'2011-03-23 18:15:52'),(241,'169.254.0.242',1,1,NULL,NULL),(242,'169.254.0.243',1,1,NULL,NULL),(243,'169.254.0.244',1,1,NULL,NULL),(244,'169.254.0.245',1,1,NULL,NULL),(245,'169.254.0.246',1,1,NULL,NULL),(246,'169.254.0.247',1,1,NULL,NULL),(247,'169.254.0.248',1,1,NULL,NULL),(248,'169.254.0.249',1,1,NULL,NULL),(249,'169.254.0.250',1,1,NULL,NULL),(250,'169.254.0.251',1,1,NULL,NULL),(251,'169.254.0.252',1,1,NULL,NULL),(252,'169.254.0.253',1,1,NULL,NULL),(253,'169.254.0.254',1,1,NULL,NULL),(254,'169.254.0.255',1,1,NULL,NULL),(255,'169.254.1.0',1,1,NULL,NULL),(256,'169.254.1.1',1,1,NULL,NULL),(257,'169.254.1.2',1,1,NULL,NULL),(258,'169.254.1.3',1,1,NULL,NULL),(259,'169.254.1.4',1,1,NULL,NULL),(260,'169.254.1.5',1,1,NULL,NULL),(261,'169.254.1.6',1,1,NULL,NULL),(262,'169.254.1.7',1,1,NULL,NULL),(263,'169.254.1.8',1,1,NULL,NULL),(264,'169.254.1.9',1,1,NULL,NULL),(265,'169.254.1.10',1,1,NULL,NULL),(266,'169.254.1.11',1,1,NULL,NULL),(267,'169.254.1.12',1,1,NULL,NULL),(268,'169.254.1.13',1,1,NULL,NULL),(269,'169.254.1.14',1,1,NULL,NULL),(270,'169.254.1.15',1,1,NULL,NULL),(271,'169.254.1.16',1,1,NULL,NULL),(272,'169.254.1.17',1,1,NULL,NULL),(273,'169.254.1.18',1,1,NULL,NULL),(274,'169.254.1.19',1,1,NULL,NULL),(275,'169.254.1.20',1,1,NULL,NULL),(276,'169.254.1.21',1,1,NULL,NULL),(277,'169.254.1.22',1,1,NULL,NULL),(278,'169.254.1.23',1,1,NULL,NULL),(279,'169.254.1.24',1,1,NULL,NULL),(280,'169.254.1.25',1,1,NULL,NULL),(281,'169.254.1.26',1,1,NULL,NULL),(282,'169.254.1.27',1,1,NULL,NULL),(283,'169.254.1.28',1,1,NULL,NULL),(284,'169.254.1.29',1,1,NULL,NULL),(285,'169.254.1.30',1,1,NULL,NULL),(286,'169.254.1.31',1,1,NULL,NULL),(287,'169.254.1.32',1,1,NULL,NULL),(288,'169.254.1.33',1,1,NULL,NULL),(289,'169.254.1.34',1,1,NULL,NULL),(290,'169.254.1.35',1,1,NULL,NULL),(291,'169.254.1.36',1,1,NULL,NULL),(292,'169.254.1.37',1,1,NULL,NULL),(293,'169.254.1.38',1,1,NULL,NULL),(294,'169.254.1.39',1,1,NULL,NULL),(295,'169.254.1.40',1,1,NULL,NULL),(296,'169.254.1.41',1,1,NULL,NULL),(297,'169.254.1.42',1,1,NULL,NULL),(298,'169.254.1.43',1,1,NULL,NULL),(299,'169.254.1.44',1,1,NULL,NULL),(300,'169.254.1.45',1,1,NULL,NULL),(301,'169.254.1.46',1,1,NULL,NULL),(302,'169.254.1.47',1,1,NULL,NULL),(303,'169.254.1.48',1,1,NULL,NULL),(304,'169.254.1.49',1,1,NULL,NULL),(305,'169.254.1.50',1,1,NULL,NULL),(306,'169.254.1.51',1,1,NULL,NULL),(307,'169.254.1.52',1,1,NULL,NULL),(308,'169.254.1.53',1,1,NULL,NULL),(309,'169.254.1.54',1,1,NULL,NULL),(310,'169.254.1.55',1,1,NULL,NULL),(311,'169.254.1.56',1,1,NULL,NULL),(312,'169.254.1.57',1,1,NULL,NULL),(313,'169.254.1.58',1,1,NULL,NULL),(314,'169.254.1.59',1,1,NULL,NULL),(315,'169.254.1.60',1,1,NULL,NULL),(316,'169.254.1.61',1,1,NULL,NULL),(317,'169.254.1.62',1,1,NULL,NULL),(318,'169.254.1.63',1,1,NULL,NULL),(319,'169.254.1.64',1,1,NULL,NULL),(320,'169.254.1.65',1,1,NULL,NULL),(321,'169.254.1.66',1,1,NULL,NULL),(322,'169.254.1.67',1,1,NULL,NULL),(323,'169.254.1.68',1,1,NULL,NULL),(324,'169.254.1.69',1,1,NULL,NULL),(325,'169.254.1.70',1,1,NULL,NULL),(326,'169.254.1.71',1,1,NULL,NULL),(327,'169.254.1.72',1,1,NULL,NULL),(328,'169.254.1.73',1,1,NULL,NULL),(329,'169.254.1.74',1,1,NULL,NULL),(330,'169.254.1.75',1,1,NULL,NULL),(331,'169.254.1.76',1,1,NULL,NULL),(332,'169.254.1.77',1,1,NULL,NULL),(333,'169.254.1.78',1,1,NULL,NULL),(334,'169.254.1.79',1,1,NULL,NULL),(335,'169.254.1.80',1,1,NULL,NULL),(336,'169.254.1.81',1,1,NULL,NULL),(337,'169.254.1.82',1,1,NULL,NULL),(338,'169.254.1.83',1,1,NULL,NULL),(339,'169.254.1.84',1,1,NULL,NULL),(340,'169.254.1.85',1,1,NULL,NULL),(341,'169.254.1.86',1,1,NULL,NULL),(342,'169.254.1.87',1,1,NULL,NULL),(343,'169.254.1.88',1,1,NULL,NULL),(344,'169.254.1.89',1,1,NULL,NULL),(345,'169.254.1.90',1,1,NULL,NULL),(346,'169.254.1.91',1,1,NULL,NULL),(347,'169.254.1.92',1,1,NULL,NULL),(348,'169.254.1.93',1,1,NULL,NULL),(349,'169.254.1.94',1,1,NULL,NULL),(350,'169.254.1.95',1,1,NULL,NULL),(351,'169.254.1.96',1,1,NULL,NULL),(352,'169.254.1.97',1,1,NULL,NULL),(353,'169.254.1.98',1,1,NULL,NULL),(354,'169.254.1.99',1,1,NULL,NULL),(355,'169.254.1.100',1,1,NULL,NULL),(356,'169.254.1.101',1,1,NULL,NULL),(357,'169.254.1.102',1,1,NULL,NULL),(358,'169.254.1.103',1,1,NULL,NULL),(359,'169.254.1.104',1,1,NULL,NULL),(360,'169.254.1.105',1,1,NULL,NULL),(361,'169.254.1.106',1,1,NULL,NULL),(362,'169.254.1.107',1,1,NULL,NULL),(363,'169.254.1.108',1,1,NULL,NULL),(364,'169.254.1.109',1,1,NULL,NULL),(365,'169.254.1.110',1,1,NULL,NULL),(366,'169.254.1.111',1,1,NULL,NULL),(367,'169.254.1.112',1,1,NULL,NULL),(368,'169.254.1.113',1,1,NULL,NULL),(369,'169.254.1.114',1,1,NULL,NULL),(370,'169.254.1.115',1,1,NULL,NULL),(371,'169.254.1.116',1,1,NULL,NULL),(372,'169.254.1.117',1,1,NULL,NULL),(373,'169.254.1.118',1,1,NULL,NULL),(374,'169.254.1.119',1,1,NULL,NULL),(375,'169.254.1.120',1,1,NULL,NULL),(376,'169.254.1.121',1,1,NULL,NULL),(377,'169.254.1.122',1,1,NULL,NULL),(378,'169.254.1.123',1,1,NULL,NULL),(379,'169.254.1.124',1,1,NULL,NULL),(380,'169.254.1.125',1,1,NULL,NULL),(381,'169.254.1.126',1,1,NULL,NULL),(382,'169.254.1.127',1,1,NULL,NULL),(383,'169.254.1.128',1,1,NULL,NULL),(384,'169.254.1.129',1,1,NULL,NULL),(385,'169.254.1.130',1,1,NULL,NULL),(386,'169.254.1.131',1,1,NULL,NULL),(387,'169.254.1.132',1,1,NULL,NULL),(388,'169.254.1.133',1,1,NULL,NULL),(389,'169.254.1.134',1,1,NULL,NULL),(390,'169.254.1.135',1,1,NULL,NULL),(391,'169.254.1.136',1,1,NULL,NULL),(392,'169.254.1.137',1,1,NULL,NULL),(393,'169.254.1.138',1,1,NULL,NULL),(394,'169.254.1.139',1,1,NULL,NULL),(395,'169.254.1.140',1,1,NULL,NULL),(396,'169.254.1.141',1,1,NULL,NULL),(397,'169.254.1.142',1,1,NULL,NULL),(398,'169.254.1.143',1,1,NULL,NULL),(399,'169.254.1.144',1,1,NULL,NULL),(400,'169.254.1.145',1,1,NULL,NULL),(401,'169.254.1.146',1,1,NULL,NULL),(402,'169.254.1.147',1,1,NULL,NULL),(403,'169.254.1.148',1,1,NULL,NULL),(404,'169.254.1.149',1,1,NULL,NULL),(405,'169.254.1.150',1,1,NULL,NULL),(406,'169.254.1.151',1,1,NULL,NULL),(407,'169.254.1.152',1,1,NULL,NULL),(408,'169.254.1.153',1,1,NULL,NULL),(409,'169.254.1.154',1,1,NULL,NULL),(410,'169.254.1.155',1,1,4,'2011-03-28 11:51:19'),(411,'169.254.1.156',1,1,NULL,NULL),(412,'169.254.1.157',1,1,NULL,NULL),(413,'169.254.1.158',1,1,NULL,NULL),(414,'169.254.1.159',1,1,NULL,NULL),(415,'169.254.1.160',1,1,NULL,NULL),(416,'169.254.1.161',1,1,NULL,NULL),(417,'169.254.1.162',1,1,NULL,NULL),(418,'169.254.1.163',1,1,NULL,NULL),(419,'169.254.1.164',1,1,NULL,NULL),(420,'169.254.1.165',1,1,NULL,NULL),(421,'169.254.1.166',1,1,NULL,NULL),(422,'169.254.1.167',1,1,NULL,NULL),(423,'169.254.1.168',1,1,NULL,NULL),(424,'169.254.1.169',1,1,NULL,NULL),(425,'169.254.1.170',1,1,NULL,NULL),(426,'169.254.1.171',1,1,NULL,NULL),(427,'169.254.1.172',1,1,NULL,NULL),(428,'169.254.1.173',1,1,NULL,NULL),(429,'169.254.1.174',1,1,NULL,NULL),(430,'169.254.1.175',1,1,NULL,NULL),(431,'169.254.1.176',1,1,NULL,NULL),(432,'169.254.1.177',1,1,NULL,NULL),(433,'169.254.1.178',1,1,NULL,NULL),(434,'169.254.1.179',1,1,NULL,NULL),(435,'169.254.1.180',1,1,NULL,NULL),(436,'169.254.1.181',1,1,NULL,NULL),(437,'169.254.1.182',1,1,NULL,NULL),(438,'169.254.1.183',1,1,NULL,NULL),(439,'169.254.1.184',1,1,NULL,NULL),(440,'169.254.1.185',1,1,NULL,NULL),(441,'169.254.1.186',1,1,NULL,NULL),(442,'169.254.1.187',1,1,NULL,NULL),(443,'169.254.1.188',1,1,NULL,NULL),(444,'169.254.1.189',1,1,NULL,NULL),(445,'169.254.1.190',1,1,NULL,NULL),(446,'169.254.1.191',1,1,NULL,NULL),(447,'169.254.1.192',1,1,NULL,NULL),(448,'169.254.1.193',1,1,NULL,NULL),(449,'169.254.1.194',1,1,NULL,NULL),(450,'169.254.1.195',1,1,NULL,NULL),(451,'169.254.1.196',1,1,NULL,NULL),(452,'169.254.1.197',1,1,NULL,NULL),(453,'169.254.1.198',1,1,NULL,NULL),(454,'169.254.1.199',1,1,NULL,NULL),(455,'169.254.1.200',1,1,NULL,NULL),(456,'169.254.1.201',1,1,NULL,NULL),(457,'169.254.1.202',1,1,NULL,NULL),(458,'169.254.1.203',1,1,NULL,NULL),(459,'169.254.1.204',1,1,NULL,NULL),(460,'169.254.1.205',1,1,NULL,NULL),(461,'169.254.1.206',1,1,NULL,NULL),(462,'169.254.1.207',1,1,NULL,NULL),(463,'169.254.1.208',1,1,NULL,NULL),(464,'169.254.1.209',1,1,NULL,NULL),(465,'169.254.1.210',1,1,NULL,NULL),(466,'169.254.1.211',1,1,NULL,NULL),(467,'169.254.1.212',1,1,NULL,NULL),(468,'169.254.1.213',1,1,NULL,NULL),(469,'169.254.1.214',1,1,NULL,NULL),(470,'169.254.1.215',1,1,NULL,NULL),(471,'169.254.1.216',1,1,NULL,NULL),(472,'169.254.1.217',1,1,NULL,NULL),(473,'169.254.1.218',1,1,NULL,NULL),(474,'169.254.1.219',1,1,NULL,NULL),(475,'169.254.1.220',1,1,NULL,NULL),(476,'169.254.1.221',1,1,NULL,NULL),(477,'169.254.1.222',1,1,NULL,NULL),(478,'169.254.1.223',1,1,NULL,NULL),(479,'169.254.1.224',1,1,NULL,NULL),(480,'169.254.1.225',1,1,NULL,NULL),(481,'169.254.1.226',1,1,NULL,NULL),(482,'169.254.1.227',1,1,NULL,NULL),(483,'169.254.1.228',1,1,NULL,NULL),(484,'169.254.1.229',1,1,NULL,NULL),(485,'169.254.1.230',1,1,NULL,NULL),(486,'169.254.1.231',1,1,NULL,NULL),(487,'169.254.1.232',1,1,NULL,NULL),(488,'169.254.1.233',1,1,NULL,NULL),(489,'169.254.1.234',1,1,NULL,NULL),(490,'169.254.1.235',1,1,NULL,NULL),(491,'169.254.1.236',1,1,NULL,NULL),(492,'169.254.1.237',1,1,NULL,NULL),(493,'169.254.1.238',1,1,NULL,NULL),(494,'169.254.1.239',1,1,NULL,NULL),(495,'169.254.1.240',1,1,NULL,NULL),(496,'169.254.1.241',1,1,NULL,NULL),(497,'169.254.1.242',1,1,NULL,NULL),(498,'169.254.1.243',1,1,NULL,NULL),(499,'169.254.1.244',1,1,NULL,NULL),(500,'169.254.1.245',1,1,NULL,NULL),(501,'169.254.1.246',1,1,NULL,NULL),(502,'169.254.1.247',1,1,NULL,NULL),(503,'169.254.1.248',1,1,NULL,NULL),(504,'169.254.1.249',1,1,NULL,NULL),(505,'169.254.1.250',1,1,NULL,NULL),(506,'169.254.1.251',1,1,NULL,NULL),(507,'169.254.1.252',1,1,NULL,NULL),(508,'169.254.1.253',1,1,NULL,NULL),(509,'169.254.1.254',1,1,NULL,NULL),(510,'169.254.1.255',1,1,NULL,NULL),(511,'169.254.2.0',1,1,NULL,NULL),(512,'169.254.2.1',1,1,NULL,NULL),(513,'169.254.2.2',1,1,NULL,NULL),(514,'169.254.2.3',1,1,NULL,NULL),(515,'169.254.2.4',1,1,NULL,NULL),(516,'169.254.2.5',1,1,NULL,NULL),(517,'169.254.2.6',1,1,NULL,NULL),(518,'169.254.2.7',1,1,NULL,NULL),(519,'169.254.2.8',1,1,NULL,NULL),(520,'169.254.2.9',1,1,NULL,NULL),(521,'169.254.2.10',1,1,NULL,NULL),(522,'169.254.2.11',1,1,NULL,NULL),(523,'169.254.2.12',1,1,NULL,NULL),(524,'169.254.2.13',1,1,NULL,NULL),(525,'169.254.2.14',1,1,NULL,NULL),(526,'169.254.2.15',1,1,NULL,NULL),(527,'169.254.2.16',1,1,NULL,NULL),(528,'169.254.2.17',1,1,NULL,NULL),(529,'169.254.2.18',1,1,NULL,NULL),(530,'169.254.2.19',1,1,NULL,NULL),(531,'169.254.2.20',1,1,NULL,NULL),(532,'169.254.2.21',1,1,NULL,NULL),(533,'169.254.2.22',1,1,NULL,NULL),(534,'169.254.2.23',1,1,NULL,NULL),(535,'169.254.2.24',1,1,NULL,NULL),(536,'169.254.2.25',1,1,NULL,NULL),(537,'169.254.2.26',1,1,NULL,NULL),(538,'169.254.2.27',1,1,NULL,NULL),(539,'169.254.2.28',1,1,NULL,NULL),(540,'169.254.2.29',1,1,NULL,NULL),(541,'169.254.2.30',1,1,NULL,NULL),(542,'169.254.2.31',1,1,NULL,NULL),(543,'169.254.2.32',1,1,NULL,NULL),(544,'169.254.2.33',1,1,NULL,NULL),(545,'169.254.2.34',1,1,NULL,NULL),(546,'169.254.2.35',1,1,NULL,NULL),(547,'169.254.2.36',1,1,NULL,NULL),(548,'169.254.2.37',1,1,NULL,NULL),(549,'169.254.2.38',1,1,NULL,NULL),(550,'169.254.2.39',1,1,NULL,NULL),(551,'169.254.2.40',1,1,NULL,NULL),(552,'169.254.2.41',1,1,NULL,NULL),(553,'169.254.2.42',1,1,NULL,NULL),(554,'169.254.2.43',1,1,NULL,NULL),(555,'169.254.2.44',1,1,NULL,NULL),(556,'169.254.2.45',1,1,NULL,NULL),(557,'169.254.2.46',1,1,NULL,NULL),(558,'169.254.2.47',1,1,NULL,NULL),(559,'169.254.2.48',1,1,NULL,NULL),(560,'169.254.2.49',1,1,NULL,NULL),(561,'169.254.2.50',1,1,NULL,NULL),(562,'169.254.2.51',1,1,NULL,NULL),(563,'169.254.2.52',1,1,NULL,NULL),(564,'169.254.2.53',1,1,NULL,NULL),(565,'169.254.2.54',1,1,NULL,NULL),(566,'169.254.2.55',1,1,NULL,NULL),(567,'169.254.2.56',1,1,NULL,NULL),(568,'169.254.2.57',1,1,NULL,NULL),(569,'169.254.2.58',1,1,NULL,NULL),(570,'169.254.2.59',1,1,NULL,NULL),(571,'169.254.2.60',1,1,NULL,NULL),(572,'169.254.2.61',1,1,NULL,NULL),(573,'169.254.2.62',1,1,NULL,NULL),(574,'169.254.2.63',1,1,NULL,NULL),(575,'169.254.2.64',1,1,NULL,NULL),(576,'169.254.2.65',1,1,NULL,NULL),(577,'169.254.2.66',1,1,NULL,NULL),(578,'169.254.2.67',1,1,NULL,NULL),(579,'169.254.2.68',1,1,NULL,NULL),(580,'169.254.2.69',1,1,NULL,NULL),(581,'169.254.2.70',1,1,NULL,NULL),(582,'169.254.2.71',1,1,NULL,NULL),(583,'169.254.2.72',1,1,NULL,NULL),(584,'169.254.2.73',1,1,NULL,NULL),(585,'169.254.2.74',1,1,NULL,NULL),(586,'169.254.2.75',1,1,NULL,NULL),(587,'169.254.2.76',1,1,NULL,NULL),(588,'169.254.2.77',1,1,NULL,NULL),(589,'169.254.2.78',1,1,NULL,NULL),(590,'169.254.2.79',1,1,NULL,NULL),(591,'169.254.2.80',1,1,NULL,NULL),(592,'169.254.2.81',1,1,NULL,NULL),(593,'169.254.2.82',1,1,NULL,NULL),(594,'169.254.2.83',1,1,NULL,NULL),(595,'169.254.2.84',1,1,NULL,NULL),(596,'169.254.2.85',1,1,NULL,NULL),(597,'169.254.2.86',1,1,NULL,NULL),(598,'169.254.2.87',1,1,NULL,NULL),(599,'169.254.2.88',1,1,NULL,NULL),(600,'169.254.2.89',1,1,NULL,NULL),(601,'169.254.2.90',1,1,NULL,NULL),(602,'169.254.2.91',1,1,NULL,NULL),(603,'169.254.2.92',1,1,NULL,NULL),(604,'169.254.2.93',1,1,NULL,NULL),(605,'169.254.2.94',1,1,NULL,NULL),(606,'169.254.2.95',1,1,NULL,NULL),(607,'169.254.2.96',1,1,NULL,NULL),(608,'169.254.2.97',1,1,NULL,NULL),(609,'169.254.2.98',1,1,NULL,NULL),(610,'169.254.2.99',1,1,NULL,NULL),(611,'169.254.2.100',1,1,NULL,NULL),(612,'169.254.2.101',1,1,NULL,NULL),(613,'169.254.2.102',1,1,NULL,NULL),(614,'169.254.2.103',1,1,NULL,NULL),(615,'169.254.2.104',1,1,NULL,NULL),(616,'169.254.2.105',1,1,NULL,NULL),(617,'169.254.2.106',1,1,NULL,NULL),(618,'169.254.2.107',1,1,NULL,NULL),(619,'169.254.2.108',1,1,NULL,NULL),(620,'169.254.2.109',1,1,NULL,NULL),(621,'169.254.2.110',1,1,NULL,NULL),(622,'169.254.2.111',1,1,NULL,NULL),(623,'169.254.2.112',1,1,NULL,NULL),(624,'169.254.2.113',1,1,NULL,NULL),(625,'169.254.2.114',1,1,NULL,NULL),(626,'169.254.2.115',1,1,NULL,NULL),(627,'169.254.2.116',1,1,NULL,NULL),(628,'169.254.2.117',1,1,NULL,NULL),(629,'169.254.2.118',1,1,NULL,NULL),(630,'169.254.2.119',1,1,NULL,NULL),(631,'169.254.2.120',1,1,NULL,NULL),(632,'169.254.2.121',1,1,NULL,NULL),(633,'169.254.2.122',1,1,NULL,NULL),(634,'169.254.2.123',1,1,NULL,NULL),(635,'169.254.2.124',1,1,NULL,NULL),(636,'169.254.2.125',1,1,NULL,NULL),(637,'169.254.2.126',1,1,NULL,NULL),(638,'169.254.2.127',1,1,NULL,NULL),(639,'169.254.2.128',1,1,NULL,NULL),(640,'169.254.2.129',1,1,NULL,NULL),(641,'169.254.2.130',1,1,NULL,NULL),(642,'169.254.2.131',1,1,NULL,NULL),(643,'169.254.2.132',1,1,NULL,NULL),(644,'169.254.2.133',1,1,NULL,NULL),(645,'169.254.2.134',1,1,NULL,NULL),(646,'169.254.2.135',1,1,NULL,NULL),(647,'169.254.2.136',1,1,NULL,NULL),(648,'169.254.2.137',1,1,NULL,NULL),(649,'169.254.2.138',1,1,NULL,NULL),(650,'169.254.2.139',1,1,NULL,NULL),(651,'169.254.2.140',1,1,NULL,NULL),(652,'169.254.2.141',1,1,NULL,NULL),(653,'169.254.2.142',1,1,NULL,NULL),(654,'169.254.2.143',1,1,NULL,NULL),(655,'169.254.2.144',1,1,NULL,NULL),(656,'169.254.2.145',1,1,NULL,NULL),(657,'169.254.2.146',1,1,NULL,NULL),(658,'169.254.2.147',1,1,NULL,NULL),(659,'169.254.2.148',1,1,NULL,NULL),(660,'169.254.2.149',1,1,NULL,NULL),(661,'169.254.2.150',1,1,NULL,NULL),(662,'169.254.2.151',1,1,NULL,NULL),(663,'169.254.2.152',1,1,NULL,NULL),(664,'169.254.2.153',1,1,NULL,NULL),(665,'169.254.2.154',1,1,NULL,NULL),(666,'169.254.2.155',1,1,NULL,NULL),(667,'169.254.2.156',1,1,NULL,NULL),(668,'169.254.2.157',1,1,NULL,NULL),(669,'169.254.2.158',1,1,NULL,NULL),(670,'169.254.2.159',1,1,NULL,NULL),(671,'169.254.2.160',1,1,NULL,NULL),(672,'169.254.2.161',1,1,NULL,NULL),(673,'169.254.2.162',1,1,NULL,NULL),(674,'169.254.2.163',1,1,NULL,NULL),(675,'169.254.2.164',1,1,NULL,NULL),(676,'169.254.2.165',1,1,NULL,NULL),(677,'169.254.2.166',1,1,NULL,NULL),(678,'169.254.2.167',1,1,NULL,NULL),(679,'169.254.2.168',1,1,NULL,NULL),(680,'169.254.2.169',1,1,NULL,NULL),(681,'169.254.2.170',1,1,NULL,NULL),(682,'169.254.2.171',1,1,NULL,NULL),(683,'169.254.2.172',1,1,NULL,NULL),(684,'169.254.2.173',1,1,NULL,NULL),(685,'169.254.2.174',1,1,NULL,NULL),(686,'169.254.2.175',1,1,NULL,NULL),(687,'169.254.2.176',1,1,NULL,NULL),(688,'169.254.2.177',1,1,NULL,NULL),(689,'169.254.2.178',1,1,NULL,NULL),(690,'169.254.2.179',1,1,NULL,NULL),(691,'169.254.2.180',1,1,NULL,NULL),(692,'169.254.2.181',1,1,NULL,NULL),(693,'169.254.2.182',1,1,NULL,NULL),(694,'169.254.2.183',1,1,NULL,NULL),(695,'169.254.2.184',1,1,NULL,NULL),(696,'169.254.2.185',1,1,NULL,NULL),(697,'169.254.2.186',1,1,NULL,NULL),(698,'169.254.2.187',1,1,NULL,NULL),(699,'169.254.2.188',1,1,NULL,NULL),(700,'169.254.2.189',1,1,NULL,NULL),(701,'169.254.2.190',1,1,NULL,NULL),(702,'169.254.2.191',1,1,NULL,NULL),(703,'169.254.2.192',1,1,NULL,NULL),(704,'169.254.2.193',1,1,NULL,NULL),(705,'169.254.2.194',1,1,NULL,NULL),(706,'169.254.2.195',1,1,NULL,NULL),(707,'169.254.2.196',1,1,NULL,NULL),(708,'169.254.2.197',1,1,NULL,NULL),(709,'169.254.2.198',1,1,NULL,NULL),(710,'169.254.2.199',1,1,NULL,NULL),(711,'169.254.2.200',1,1,NULL,NULL),(712,'169.254.2.201',1,1,NULL,NULL),(713,'169.254.2.202',1,1,NULL,NULL),(714,'169.254.2.203',1,1,NULL,NULL),(715,'169.254.2.204',1,1,NULL,NULL),(716,'169.254.2.205',1,1,NULL,NULL),(717,'169.254.2.206',1,1,NULL,NULL),(718,'169.254.2.207',1,1,NULL,NULL),(719,'169.254.2.208',1,1,NULL,NULL),(720,'169.254.2.209',1,1,NULL,NULL),(721,'169.254.2.210',1,1,NULL,NULL),(722,'169.254.2.211',1,1,NULL,NULL),(723,'169.254.2.212',1,1,NULL,NULL),(724,'169.254.2.213',1,1,NULL,NULL),(725,'169.254.2.214',1,1,NULL,NULL),(726,'169.254.2.215',1,1,NULL,NULL),(727,'169.254.2.216',1,1,NULL,NULL),(728,'169.254.2.217',1,1,NULL,NULL),(729,'169.254.2.218',1,1,NULL,NULL),(730,'169.254.2.219',1,1,NULL,NULL),(731,'169.254.2.220',1,1,NULL,NULL),(732,'169.254.2.221',1,1,NULL,NULL),(733,'169.254.2.222',1,1,NULL,NULL),(734,'169.254.2.223',1,1,NULL,NULL),(735,'169.254.2.224',1,1,NULL,NULL),(736,'169.254.2.225',1,1,NULL,NULL),(737,'169.254.2.226',1,1,NULL,NULL),(738,'169.254.2.227',1,1,NULL,NULL),(739,'169.254.2.228',1,1,NULL,NULL),(740,'169.254.2.229',1,1,NULL,NULL),(741,'169.254.2.230',1,1,NULL,NULL),(742,'169.254.2.231',1,1,NULL,NULL),(743,'169.254.2.232',1,1,NULL,NULL),(744,'169.254.2.233',1,1,NULL,NULL),(745,'169.254.2.234',1,1,NULL,NULL),(746,'169.254.2.235',1,1,NULL,NULL),(747,'169.254.2.236',1,1,NULL,NULL),(748,'169.254.2.237',1,1,NULL,NULL),(749,'169.254.2.238',1,1,NULL,NULL),(750,'169.254.2.239',1,1,NULL,NULL),(751,'169.254.2.240',1,1,NULL,NULL),(752,'169.254.2.241',1,1,NULL,NULL),(753,'169.254.2.242',1,1,NULL,NULL),(754,'169.254.2.243',1,1,NULL,NULL),(755,'169.254.2.244',1,1,NULL,NULL),(756,'169.254.2.245',1,1,NULL,NULL),(757,'169.254.2.246',1,1,NULL,NULL),(758,'169.254.2.247',1,1,NULL,NULL),(759,'169.254.2.248',1,1,NULL,NULL),(760,'169.254.2.249',1,1,NULL,NULL),(761,'169.254.2.250',1,1,NULL,NULL),(762,'169.254.2.251',1,1,NULL,NULL),(763,'169.254.2.252',1,1,NULL,NULL),(764,'169.254.2.253',1,1,NULL,NULL),(765,'169.254.2.254',1,1,NULL,NULL),(766,'169.254.2.255',1,1,NULL,NULL),(767,'169.254.3.0',1,1,NULL,NULL),(768,'169.254.3.1',1,1,NULL,NULL),(769,'169.254.3.2',1,1,NULL,NULL),(770,'169.254.3.3',1,1,NULL,NULL),(771,'169.254.3.4',1,1,NULL,NULL),(772,'169.254.3.5',1,1,NULL,NULL),(773,'169.254.3.6',1,1,NULL,NULL),(774,'169.254.3.7',1,1,NULL,NULL),(775,'169.254.3.8',1,1,NULL,NULL),(776,'169.254.3.9',1,1,NULL,NULL),(777,'169.254.3.10',1,1,NULL,NULL),(778,'169.254.3.11',1,1,NULL,NULL),(779,'169.254.3.12',1,1,NULL,NULL),(780,'169.254.3.13',1,1,NULL,NULL),(781,'169.254.3.14',1,1,NULL,NULL),(782,'169.254.3.15',1,1,NULL,NULL),(783,'169.254.3.16',1,1,NULL,NULL),(784,'169.254.3.17',1,1,NULL,NULL),(785,'169.254.3.18',1,1,NULL,NULL),(786,'169.254.3.19',1,1,NULL,NULL),(787,'169.254.3.20',1,1,NULL,NULL),(788,'169.254.3.21',1,1,NULL,NULL),(789,'169.254.3.22',1,1,NULL,NULL),(790,'169.254.3.23',1,1,NULL,NULL),(791,'169.254.3.24',1,1,NULL,NULL),(792,'169.254.3.25',1,1,NULL,NULL),(793,'169.254.3.26',1,1,NULL,NULL),(794,'169.254.3.27',1,1,NULL,NULL),(795,'169.254.3.28',1,1,NULL,NULL),(796,'169.254.3.29',1,1,NULL,NULL),(797,'169.254.3.30',1,1,NULL,NULL),(798,'169.254.3.31',1,1,NULL,NULL),(799,'169.254.3.32',1,1,NULL,NULL),(800,'169.254.3.33',1,1,NULL,NULL),(801,'169.254.3.34',1,1,NULL,NULL),(802,'169.254.3.35',1,1,NULL,NULL),(803,'169.254.3.36',1,1,NULL,NULL),(804,'169.254.3.37',1,1,NULL,NULL),(805,'169.254.3.38',1,1,NULL,NULL),(806,'169.254.3.39',1,1,NULL,NULL),(807,'169.254.3.40',1,1,NULL,NULL),(808,'169.254.3.41',1,1,NULL,NULL),(809,'169.254.3.42',1,1,NULL,NULL),(810,'169.254.3.43',1,1,NULL,NULL),(811,'169.254.3.44',1,1,NULL,NULL),(812,'169.254.3.45',1,1,NULL,NULL),(813,'169.254.3.46',1,1,NULL,NULL),(814,'169.254.3.47',1,1,NULL,NULL),(815,'169.254.3.48',1,1,NULL,NULL),(816,'169.254.3.49',1,1,NULL,NULL),(817,'169.254.3.50',1,1,NULL,NULL),(818,'169.254.3.51',1,1,NULL,NULL),(819,'169.254.3.52',1,1,NULL,NULL),(820,'169.254.3.53',1,1,NULL,NULL),(821,'169.254.3.54',1,1,NULL,NULL),(822,'169.254.3.55',1,1,NULL,NULL),(823,'169.254.3.56',1,1,NULL,NULL),(824,'169.254.3.57',1,1,NULL,NULL),(825,'169.254.3.58',1,1,NULL,NULL),(826,'169.254.3.59',1,1,NULL,NULL),(827,'169.254.3.60',1,1,NULL,NULL),(828,'169.254.3.61',1,1,NULL,NULL),(829,'169.254.3.62',1,1,NULL,NULL),(830,'169.254.3.63',1,1,NULL,NULL),(831,'169.254.3.64',1,1,NULL,NULL),(832,'169.254.3.65',1,1,NULL,NULL),(833,'169.254.3.66',1,1,NULL,NULL),(834,'169.254.3.67',1,1,NULL,NULL),(835,'169.254.3.68',1,1,NULL,NULL),(836,'169.254.3.69',1,1,NULL,NULL),(837,'169.254.3.70',1,1,NULL,NULL),(838,'169.254.3.71',1,1,NULL,NULL),(839,'169.254.3.72',1,1,NULL,NULL),(840,'169.254.3.73',1,1,NULL,NULL),(841,'169.254.3.74',1,1,NULL,NULL),(842,'169.254.3.75',1,1,NULL,NULL),(843,'169.254.3.76',1,1,NULL,NULL),(844,'169.254.3.77',1,1,NULL,NULL),(845,'169.254.3.78',1,1,NULL,NULL),(846,'169.254.3.79',1,1,NULL,NULL),(847,'169.254.3.80',1,1,NULL,NULL),(848,'169.254.3.81',1,1,NULL,NULL),(849,'169.254.3.82',1,1,NULL,NULL),(850,'169.254.3.83',1,1,NULL,NULL),(851,'169.254.3.84',1,1,NULL,NULL),(852,'169.254.3.85',1,1,NULL,NULL),(853,'169.254.3.86',1,1,NULL,NULL),(854,'169.254.3.87',1,1,NULL,NULL),(855,'169.254.3.88',1,1,NULL,NULL),(856,'169.254.3.89',1,1,NULL,NULL),(857,'169.254.3.90',1,1,NULL,NULL),(858,'169.254.3.91',1,1,NULL,NULL),(859,'169.254.3.92',1,1,NULL,NULL),(860,'169.254.3.93',1,1,NULL,NULL),(861,'169.254.3.94',1,1,NULL,NULL),(862,'169.254.3.95',1,1,NULL,NULL),(863,'169.254.3.96',1,1,NULL,NULL),(864,'169.254.3.97',1,1,NULL,NULL),(865,'169.254.3.98',1,1,NULL,NULL),(866,'169.254.3.99',1,1,NULL,NULL),(867,'169.254.3.100',1,1,NULL,NULL),(868,'169.254.3.101',1,1,NULL,NULL),(869,'169.254.3.102',1,1,NULL,NULL),(870,'169.254.3.103',1,1,NULL,NULL),(871,'169.254.3.104',1,1,NULL,NULL),(872,'169.254.3.105',1,1,NULL,NULL),(873,'169.254.3.106',1,1,NULL,NULL),(874,'169.254.3.107',1,1,NULL,NULL),(875,'169.254.3.108',1,1,NULL,NULL),(876,'169.254.3.109',1,1,NULL,NULL),(877,'169.254.3.110',1,1,NULL,NULL),(878,'169.254.3.111',1,1,NULL,NULL),(879,'169.254.3.112',1,1,NULL,NULL),(880,'169.254.3.113',1,1,NULL,NULL),(881,'169.254.3.114',1,1,NULL,NULL),(882,'169.254.3.115',1,1,NULL,NULL),(883,'169.254.3.116',1,1,NULL,NULL),(884,'169.254.3.117',1,1,NULL,NULL),(885,'169.254.3.118',1,1,NULL,NULL),(886,'169.254.3.119',1,1,NULL,NULL),(887,'169.254.3.120',1,1,NULL,NULL),(888,'169.254.3.121',1,1,NULL,NULL),(889,'169.254.3.122',1,1,NULL,NULL),(890,'169.254.3.123',1,1,NULL,NULL),(891,'169.254.3.124',1,1,NULL,NULL),(892,'169.254.3.125',1,1,NULL,NULL),(893,'169.254.3.126',1,1,NULL,NULL),(894,'169.254.3.127',1,1,NULL,NULL),(895,'169.254.3.128',1,1,NULL,NULL),(896,'169.254.3.129',1,1,NULL,NULL),(897,'169.254.3.130',1,1,NULL,NULL),(898,'169.254.3.131',1,1,NULL,NULL),(899,'169.254.3.132',1,1,NULL,NULL),(900,'169.254.3.133',1,1,NULL,NULL),(901,'169.254.3.134',1,1,NULL,NULL),(902,'169.254.3.135',1,1,NULL,NULL),(903,'169.254.3.136',1,1,NULL,NULL),(904,'169.254.3.137',1,1,NULL,NULL),(905,'169.254.3.138',1,1,NULL,NULL),(906,'169.254.3.139',1,1,NULL,NULL),(907,'169.254.3.140',1,1,NULL,NULL),(908,'169.254.3.141',1,1,NULL,NULL),(909,'169.254.3.142',1,1,NULL,NULL),(910,'169.254.3.143',1,1,NULL,NULL),(911,'169.254.3.144',1,1,NULL,NULL),(912,'169.254.3.145',1,1,NULL,NULL),(913,'169.254.3.146',1,1,NULL,NULL),(914,'169.254.3.147',1,1,NULL,NULL),(915,'169.254.3.148',1,1,NULL,NULL),(916,'169.254.3.149',1,1,NULL,NULL),(917,'169.254.3.150',1,1,NULL,NULL),(918,'169.254.3.151',1,1,NULL,NULL),(919,'169.254.3.152',1,1,NULL,NULL),(920,'169.254.3.153',1,1,NULL,NULL),(921,'169.254.3.154',1,1,NULL,NULL),(922,'169.254.3.155',1,1,NULL,NULL),(923,'169.254.3.156',1,1,NULL,NULL),(924,'169.254.3.157',1,1,NULL,NULL),(925,'169.254.3.158',1,1,NULL,NULL),(926,'169.254.3.159',1,1,NULL,NULL),(927,'169.254.3.160',1,1,NULL,NULL),(928,'169.254.3.161',1,1,NULL,NULL),(929,'169.254.3.162',1,1,NULL,NULL),(930,'169.254.3.163',1,1,NULL,NULL),(931,'169.254.3.164',1,1,NULL,NULL),(932,'169.254.3.165',1,1,NULL,NULL),(933,'169.254.3.166',1,1,NULL,NULL),(934,'169.254.3.167',1,1,NULL,NULL),(935,'169.254.3.168',1,1,NULL,NULL),(936,'169.254.3.169',1,1,NULL,NULL),(937,'169.254.3.170',1,1,NULL,NULL),(938,'169.254.3.171',1,1,NULL,NULL),(939,'169.254.3.172',1,1,NULL,NULL),(940,'169.254.3.173',1,1,NULL,NULL),(941,'169.254.3.174',1,1,NULL,NULL),(942,'169.254.3.175',1,1,NULL,NULL),(943,'169.254.3.176',1,1,NULL,NULL),(944,'169.254.3.177',1,1,NULL,NULL),(945,'169.254.3.178',1,1,NULL,NULL),(946,'169.254.3.179',1,1,NULL,NULL),(947,'169.254.3.180',1,1,NULL,NULL),(948,'169.254.3.181',1,1,NULL,NULL),(949,'169.254.3.182',1,1,NULL,NULL),(950,'169.254.3.183',1,1,NULL,NULL),(951,'169.254.3.184',1,1,NULL,NULL),(952,'169.254.3.185',1,1,NULL,NULL),(953,'169.254.3.186',1,1,NULL,NULL),(954,'169.254.3.187',1,1,NULL,NULL),(955,'169.254.3.188',1,1,NULL,NULL),(956,'169.254.3.189',1,1,NULL,NULL),(957,'169.254.3.190',1,1,NULL,NULL),(958,'169.254.3.191',1,1,NULL,NULL),(959,'169.254.3.192',1,1,NULL,NULL),(960,'169.254.3.193',1,1,NULL,NULL),(961,'169.254.3.194',1,1,NULL,NULL),(962,'169.254.3.195',1,1,NULL,NULL),(963,'169.254.3.196',1,1,NULL,NULL),(964,'169.254.3.197',1,1,NULL,NULL),(965,'169.254.3.198',1,1,NULL,NULL),(966,'169.254.3.199',1,1,NULL,NULL),(967,'169.254.3.200',1,1,NULL,NULL),(968,'169.254.3.201',1,1,NULL,NULL),(969,'169.254.3.202',1,1,NULL,NULL),(970,'169.254.3.203',1,1,NULL,NULL),(971,'169.254.3.204',1,1,NULL,NULL),(972,'169.254.3.205',1,1,NULL,NULL),(973,'169.254.3.206',1,1,NULL,NULL),(974,'169.254.3.207',1,1,NULL,NULL),(975,'169.254.3.208',1,1,NULL,NULL),(976,'169.254.3.209',1,1,NULL,NULL),(977,'169.254.3.210',1,1,NULL,NULL),(978,'169.254.3.211',1,1,NULL,NULL),(979,'169.254.3.212',1,1,NULL,NULL),(980,'169.254.3.213',1,1,NULL,NULL),(981,'169.254.3.214',1,1,NULL,NULL),(982,'169.254.3.215',1,1,NULL,NULL),(983,'169.254.3.216',1,1,NULL,NULL),(984,'169.254.3.217',1,1,NULL,NULL),(985,'169.254.3.218',1,1,NULL,NULL),(986,'169.254.3.219',1,1,NULL,NULL),(987,'169.254.3.220',1,1,NULL,NULL),(988,'169.254.3.221',1,1,NULL,NULL),(989,'169.254.3.222',1,1,NULL,NULL),(990,'169.254.3.223',1,1,NULL,NULL),(991,'169.254.3.224',1,1,NULL,NULL),(992,'169.254.3.225',1,1,NULL,NULL),(993,'169.254.3.226',1,1,NULL,NULL),(994,'169.254.3.227',1,1,NULL,NULL),(995,'169.254.3.228',1,1,NULL,NULL),(996,'169.254.3.229',1,1,NULL,NULL),(997,'169.254.3.230',1,1,NULL,NULL),(998,'169.254.3.231',1,1,NULL,NULL),(999,'169.254.3.232',1,1,NULL,NULL),(1000,'169.254.3.233',1,1,NULL,NULL),(1001,'169.254.3.234',1,1,NULL,NULL),(1002,'169.254.3.235',1,1,NULL,NULL),(1003,'169.254.3.236',1,1,NULL,NULL),(1004,'169.254.3.237',1,1,NULL,NULL),(1005,'169.254.3.238',1,1,NULL,NULL),(1006,'169.254.3.239',1,1,NULL,NULL),(1007,'169.254.3.240',1,1,NULL,NULL),(1008,'169.254.3.241',1,1,NULL,NULL),(1009,'169.254.3.242',1,1,NULL,NULL),(1010,'169.254.3.243',1,1,NULL,NULL),(1011,'169.254.3.244',1,1,NULL,NULL),(1012,'169.254.3.245',1,1,NULL,NULL),(1013,'169.254.3.246',1,1,NULL,NULL),(1014,'169.254.3.247',1,1,NULL,NULL),(1015,'169.254.3.248',1,1,NULL,NULL),(1016,'169.254.3.249',1,1,NULL,NULL),(1017,'169.254.3.250',1,1,NULL,NULL),(1018,'169.254.3.251',1,1,NULL,NULL),(1019,'169.254.3.252',1,1,NULL,NULL),(1020,'169.254.3.253',1,1,NULL,NULL),(1021,'169.254.3.254',1,1,NULL,NULL);
/*!40000 ALTER TABLE `op_dc_link_local_ip_address_alloc` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `op_dc_vnet_alloc`
--

DROP TABLE IF EXISTS `op_dc_vnet_alloc`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
) ENGINE=InnoDB AUTO_INCREMENT=1007 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `op_dc_vnet_alloc`
--

LOCK TABLES `op_dc_vnet_alloc` WRITE;
/*!40000 ALTER TABLE `op_dc_vnet_alloc` DISABLE KEYS */;
INSERT INTO `op_dc_vnet_alloc` VALUES (1002,'500',1,2,'2011-03-28 11:51:19'),(1003,'501',1,3,'2011-03-24 05:50:56'),(1004,'502',1,NULL,NULL),(1005,'503',1,NULL,NULL),(1006,'504',1,NULL,NULL);
/*!40000 ALTER TABLE `op_dc_vnet_alloc` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `op_ha_work`
--

DROP TABLE IF EXISTS `op_ha_work`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

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
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
) ENGINE=InnoDB AUTO_INCREMENT=14393 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `op_host_capacity`
--

LOCK TABLES `op_host_capacity` WRITE;
/*!40000 ALTER TABLE `op_host_capacity` DISABLE KEYS */;
INSERT INTO `op_host_capacity` VALUES (147,200,1,1,42464289792,3938019442688,3),(1388,201,1,1,0,3938019442688,3),(14389,2,1,1,2684354560,3151124928,0),(14390,2,1,1,1000,5984,1),(14391,NULL,1,NULL,6,10,4),(14392,NULL,1,1,2,10,5);
/*!40000 ALTER TABLE `op_host_capacity` ENABLE KEYS */;
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
  PRIMARY KEY  (`host_id`),
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
-- Table structure for table `op_lock`
--

DROP TABLE IF EXISTS `op_lock`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

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
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
  `id` bigint(20) unsigned NOT NULL auto_increment COMMENT 'primary id',
  `vlan` varchar(18) NOT NULL COMMENT 'vlan id',
  `data_center_id` bigint(20) unsigned NOT NULL COMMENT 'data center the pod belongs to',
  `pod_id` bigint(20) unsigned NOT NULL COMMENT 'pod the vlan belongs to',
  `account_id` bigint(20) unsigned default NULL COMMENT 'account the vlan belongs to right now',
  `taken` datetime default NULL COMMENT 'Date taken',
  PRIMARY KEY  (`id`)
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
-- Table structure for table `op_vm_host`
--

DROP TABLE IF EXISTS `op_vm_host`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `op_vm_host` (
  `id` bigint(20) unsigned NOT NULL COMMENT 'foreign key to host_id',
  `vnc_ports` bigint(20) unsigned NOT NULL default '0' COMMENT 'vnc ports open on the host',
  `start_at` int(5) unsigned NOT NULL default '0' COMMENT 'Start the vnc port look up at this bit',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`),
  CONSTRAINT `fk_op_vm_host__id` FOREIGN KEY (`id`) REFERENCES `host` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

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
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `op_vm_ruleset_log` (
  `id` bigint(20) unsigned NOT NULL auto_increment COMMENT 'id',
  `instance_id` bigint(20) unsigned NOT NULL COMMENT 'vm instance that needs rules to be synced.',
  `created` datetime NOT NULL COMMENT 'time the entry was requested',
  `logsequence` bigint(20) unsigned default NULL COMMENT 'seq number to be sent to agent, uniquely identifies ruleset update',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `id` (`id`)
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
-- Table structure for table `pod_vlan_map`
--

DROP TABLE IF EXISTS `pod_vlan_map`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

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
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `pricing` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `price` float unsigned NOT NULL,
  `price_unit` varchar(45) NOT NULL,
  `type` varchar(255) NOT NULL,
  `type_id` int(10) unsigned default NULL,
  `created` datetime NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

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
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `resource_count` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `account_id` bigint(20) unsigned NOT NULL,
  `type` varchar(255) default NULL,
  `count` bigint(20) NOT NULL default '0',
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `resource_count`
--

LOCK TABLES `resource_count` WRITE;
/*!40000 ALTER TABLE `resource_count` DISABLE KEYS */;
INSERT INTO `resource_count` VALUES (1,2,'public_ip',2),(2,2,'user_vm',1),(3,2,'volume',3),(4,3,'public_ip',1),(5,3,'user_vm',1),(6,3,'volume',1),(7,3,'template',0),(8,6,'public_ip',0),(9,8,'public_ip',0),(10,10,'public_ip',1),(11,10,'user_vm',0),(12,10,'volume',0),(13,6,'volume',0),(14,2,'snapshot',1);
/*!40000 ALTER TABLE `resource_count` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `resource_limit`
--

DROP TABLE IF EXISTS `resource_limit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `resource_limit`
--

LOCK TABLES `resource_limit` WRITE;
/*!40000 ALTER TABLE `resource_limit` DISABLE KEYS */;
INSERT INTO `resource_limit` VALUES (1,NULL,3,'user_vm',2);
/*!40000 ALTER TABLE `resource_limit` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `secondary_storage_vm`
--

DROP TABLE IF EXISTS `secondary_storage_vm`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `secondary_storage_vm`
--

LOCK TABLES `secondary_storage_vm` WRITE;
/*!40000 ALTER TABLE `secondary_storage_vm` DISABLE KEYS */;
INSERT INTO `secondary_storage_vm` VALUES (1,'10.91.30.1','4.2.2.2',NULL,'foo.com','06:81:dd:c7:00:01','10.91.30.104','255.255.255.0','06:01:ea:30:00:02','169.254.0.241','255.255.0.0',1,'30',256,'nfs://10.91.28.6/export/home/idc218/secondary','nfs://10.91.28.6/export/home/idc218/secondary',NULL);
/*!40000 ALTER TABLE `secondary_storage_vm` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `security_group`
--

DROP TABLE IF EXISTS `security_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `security_group` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `name` varchar(255) NOT NULL,
  `description` varchar(4096) default NULL,
  `domain_id` bigint(20) unsigned default NULL,
  `account_id` bigint(20) unsigned default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

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
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `security_group_vm_map` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `security_group_id` bigint(20) unsigned NOT NULL,
  `ip_address` varchar(15) NOT NULL,
  `instance_id` bigint(20) unsigned NOT NULL,
  PRIMARY KEY  (`id`)
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
-- Table structure for table `sequence`
--

DROP TABLE IF EXISTS `sequence`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sequence` (
  `name` varchar(64) NOT NULL COMMENT 'name of the sequence',
  `value` bigint(20) unsigned NOT NULL COMMENT 'sequence value',
  PRIMARY KEY  (`name`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sequence`
--

LOCK TABLES `sequence` WRITE;
/*!40000 ALTER TABLE `sequence` DISABLE KEYS */;
INSERT INTO `sequence` VALUES ('private_mac_address_seq',1),('public_mac_address_seq',1),('storage_pool_seq',202),('vm_instance_seq',24),('vm_template_seq',202);
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
  `nw_rate` smallint(5) unsigned default '200' COMMENT 'network rate throttle mbits/s',
  `mc_rate` smallint(5) unsigned default '10' COMMENT 'mcast rate throttle mbits/s',
  `ha_enabled` tinyint(1) unsigned NOT NULL default '0' COMMENT 'Enable HA',
  `guest_ip_type` varchar(255) NOT NULL default 'Virtualized' COMMENT 'Type of guest network -- direct or virtualized',
  `host_tag` varchar(255) default NULL COMMENT 'host tag specified by the service_offering',
  PRIMARY KEY  (`id`),
  CONSTRAINT `fk_service_offering__id` FOREIGN KEY (`id`) REFERENCES `disk_offering` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `service_offering`
--

LOCK TABLES `service_offering` WRITE;
/*!40000 ALTER TABLE `service_offering` DISABLE KEYS */;
INSERT INTO `service_offering` VALUES (1,1,500,512,200,10,0,'Virtualized',NULL),(2,1,1000,1024,200,10,0,'Virtualized',NULL),(3,1,500,512,200,10,0,'DirectSingle',NULL),(4,1,1000,1024,200,10,0,'DirectSingle',NULL),(8,1,0,128,0,0,0,'Virtualized',NULL),(9,1,0,256,0,0,0,'Virtualized',NULL),(10,1,0,1024,0,0,0,'Virtualized',NULL);
/*!40000 ALTER TABLE `service_offering` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `snapshot_policy`
--

DROP TABLE IF EXISTS `snapshot_policy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

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
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `snapshot_policy_ref` (
  `snap_id` bigint(20) unsigned NOT NULL,
  `volume_id` bigint(20) unsigned NOT NULL,
  `policy_id` bigint(20) unsigned NOT NULL,
  UNIQUE KEY `snap_id` (`snap_id`,`policy_id`),
  KEY `i_snapshot_policy_ref__snap_id` (`snap_id`),
  KEY `i_snapshot_policy_ref__volume_id` (`volume_id`),
  KEY `i_snapshot_policy_ref__policy_id` (`policy_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `snapshot_policy_ref`
--

LOCK TABLES `snapshot_policy_ref` WRITE;
/*!40000 ALTER TABLE `snapshot_policy_ref` DISABLE KEYS */;
INSERT INTO `snapshot_policy_ref` VALUES (1,14,1);
/*!40000 ALTER TABLE `snapshot_policy_ref` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `snapshot_schedule`
--

DROP TABLE IF EXISTS `snapshot_schedule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `snapshots`
--

LOCK TABLES `snapshots` WRITE;
/*!40000 ALTER TABLE `snapshots` DISABLE KEYS */;
INSERT INTO `snapshots` VALUES (1,2,14,'BackedUp','e50be7c2-9454-4d4d-99f7-b5f1659f200d','i-2-23-VM_i-2-23-VM-ROOT_20110329094214',0,'MANUAL','2011-03-29 09:42:14',NULL,'214d273e-52ab-4510-a7d7-6796558e4dbd',0);
/*!40000 ALTER TABLE `snapshots` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `stack_maid`
--

DROP TABLE IF EXISTS `stack_maid`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `storage_pool`
--

LOCK TABLES `storage_pool` WRITE;
/*!40000 ALTER TABLE `storage_pool` DISABLE KEYS */;
INSERT INTO `storage_pool` VALUES (200,'idc-ss','bdb54fc7-d959-339d-8a1d-9cf9daebd338','NetworkFilesystem',2049,1,1,1,1371631779840,1969009721344,'10.91.28.6','/export/home/idc218/primary','2011-03-23 18:09:46',NULL,NULL),(201,'CCC','87c9f181-ff49-3462-8e85-89647214d31a','NetworkFilesystem',2049,1,1,2,1410550726656,1969009721344,'10.91.28.6','/export/home/shweta/usage/primary2','2011-03-24 07:01:37',NULL,NULL);
/*!40000 ALTER TABLE `storage_pool` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `storage_pool_details`
--

DROP TABLE IF EXISTS `storage_pool_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

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
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `storage_pool_host_ref` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `host_id` bigint(20) unsigned NOT NULL,
  `pool_id` bigint(20) unsigned NOT NULL,
  `created` datetime NOT NULL,
  `last_updated` datetime default NULL,
  `local_path` varchar(255) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `storage_pool_host_ref`
--

LOCK TABLES `storage_pool_host_ref` WRITE;
/*!40000 ALTER TABLE `storage_pool_host_ref` DISABLE KEYS */;
INSERT INTO `storage_pool_host_ref` VALUES (1,2,200,'2011-03-23 18:09:47',NULL,'/mnt//bdb54fc7-d959-339d-8a1d-9cf9daebd338');
/*!40000 ALTER TABLE `storage_pool_host_ref` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sync_queue`
--

DROP TABLE IF EXISTS `sync_queue`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sync_queue`
--

LOCK TABLES `sync_queue` WRITE;
/*!40000 ALTER TABLE `sync_queue` DISABLE KEYS */;
INSERT INTO `sync_queue` VALUES (1,'UserVM',3,222119676852735,1,NULL,'2011-03-28 09:37:00','2011-03-28 09:37:24'),(2,'Router',11,222119676852735,1,NULL,'2011-03-28 09:37:09','2011-03-28 09:37:36'),(3,'UserVM',19,222119676852735,1,NULL,'2011-03-28 09:44:14','2011-03-28 09:44:38'),(4,'Volume',0,222119676852735,2,NULL,'2011-03-28 11:48:28','2011-03-28 11:49:14'),(5,'UserVM',20,222119676852735,1,NULL,'2011-03-28 11:50:25','2011-03-28 11:50:49'),(6,'Volume',15,222119676852735,5,NULL,'2011-03-28 11:53:36','2011-03-28 11:58:25'),(7,'Volume',14,222119676852735,1,NULL,'2011-03-29 09:42:14','2011-03-29 09:43:53');
/*!40000 ALTER TABLE `sync_queue` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sync_queue_item`
--

DROP TABLE IF EXISTS `sync_queue_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `template_host_ref`
--

LOCK TABLES `template_host_ref` WRITE;
/*!40000 ALTER TABLE `template_host_ref` DISABLE KEYS */;
INSERT INTO `template_host_ref` VALUES (1,1,NULL,1,'2011-03-23 18:00:49','2011-03-23 19:17:49',NULL,100,2147483648,'DOWNLOADED',NULL,NULL,'template/tmpl/1/1//1c282b5e-b602-4c96-97d6-794a0bd79fc0.vhd','http://10.91.28.6/releases/2.0.0RC5/systemvm.vhd.bz2',0,0),(2,1,NULL,2,'2011-03-23 18:00:49','2011-03-23 19:17:49','gQYB6jAAAgBLuPupAAAAAQ==',100,8589934592,'DOWNLOADED','Install completed successfully at 3/23/11 11:21 AM','/mnt/SecStorage/1645b82a/template/tmpl/1/2/dnld6962741531145405762tmp_','template/tmpl/1/2//13e31646-1766-3de9-89ce-143dbbaac262.vhd','http://10.91.28.6/templates/builtin/f59f18fb-ae94-4f97-afd2-f84755767aca.vhd.bz2',0,0),(3,2,NULL,200,'2011-03-23 18:08:23','2011-03-23 18:08:23',NULL,100,66023424,'DOWNLOADED',NULL,NULL,'iso/users/2/xs-tools',NULL,0,0),(4,1,NULL,201,'2011-03-24 05:54:08','2011-03-24 05:58:22','gQYB6jAAAgBLuPupAAAAAg==',100,8589934592,'DOWNLOADED','Install completed successfully at 3/23/11 10:58 PM','/mnt/SecStorage/1645b82a/template/tmpl/3/201/dnld785153796646785973tmp_','template/tmpl/3/201//45f3157a-de26-3d11-8484-621c8c7188a6.vhd','http://10.91.28.6/templates/centos53-x86_64/latest/f59f18fb-ae94-4f97-afd2-f84755767aca.vhd.bz2',1,0),(5,4,NULL,200,'2011-03-24 07:01:00','2011-03-24 07:01:00',NULL,100,66023424,'DOWNLOADED',NULL,NULL,'iso/users/2/xs-tools',NULL,0,0);
/*!40000 ALTER TABLE `template_host_ref` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `template_spool_ref`
--

DROP TABLE IF EXISTS `template_spool_ref`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `template_spool_ref`
--

LOCK TABLES `template_spool_ref` WRITE;
/*!40000 ALTER TABLE `template_spool_ref` DISABLE KEYS */;
INSERT INTO `template_spool_ref` VALUES (1,200,1,'2011-03-23 18:09:53',NULL,NULL,0,'DOWNLOADED',NULL,'3324e5ee-7ea9-42ba-a39b-45d4ee3fe9b7','3324e5ee-7ea9-42ba-a39b-45d4ee3fe9b7',2101252608,0),(2,200,2,'2011-03-23 18:30:54',NULL,NULL,0,'DOWNLOADED',NULL,'c6020cc1-04fe-4eed-8d48-5f6be1c6d998','c6020cc1-04fe-4eed-8d48-5f6be1c6d998',1708331520,0),(3,201,201,'2011-03-28 09:38:15',NULL,NULL,0,'NOT_DOWNLOADED',NULL,NULL,NULL,0,0),(5,201,2,'2011-03-28 11:51:18',NULL,NULL,0,'NOT_DOWNLOADED',NULL,NULL,NULL,0,0);
/*!40000 ALTER TABLE `template_spool_ref` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `template_zone_ref`
--

DROP TABLE IF EXISTS `template_zone_ref`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `template_zone_ref`
--

LOCK TABLES `template_zone_ref` WRITE;
/*!40000 ALTER TABLE `template_zone_ref` DISABLE KEYS */;
INSERT INTO `template_zone_ref` VALUES (1,1,1,'2011-03-23 18:00:49','2011-03-23 18:00:49',NULL),(2,1,2,'2011-03-23 18:00:49','2011-03-23 18:00:49',NULL),(3,1,201,'2011-03-24 05:54:08','2011-03-24 05:54:08','2011-03-29 09:42:43');
/*!40000 ALTER TABLE `template_zone_ref` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'system','',1,'system','cloud',NULL,'enabled',NULL,NULL,'2011-03-23 20:41:54',NULL,NULL),(2,'admin','5f4dcc3b5aa765d61d8327deb882cf99',2,'admin','cloud','','enabled',NULL,NULL,'2011-03-23 20:41:54',NULL,NULL),(3,'test','5f4dcc3b5aa765d61d8327deb882cf99',3,'test','test','test','enabled',NULL,NULL,'2011-03-24 05:37:23','2011-03-24 05:49:55',NULL),(4,'test1','5f4dcc3b5aa765d61d8327deb882cf99',4,'test1','test1','test1','enabled',NULL,NULL,'2011-03-24 05:50:07','2011-03-24 05:52:34',NULL),(5,'test1','5f4dcc3b5aa765d61d8327deb882cf99',5,'test1','test1','test1','enabled',NULL,NULL,'2011-03-24 05:52:42','2011-03-24 07:03:30',NULL),(6,'d1','9948c645c094247794f4c7acdbeb2bb6',6,'d1','d1','d1','enabled',NULL,NULL,'2011-03-28 09:26:21',NULL,NULL),(7,'d2','b25b0651e4b6e887e5194135d3692631',7,'d2','d2','d2','enabled',NULL,NULL,'2011-03-28 09:27:05',NULL,NULL),(8,'d3','e53125275854402400f74fd6ab3f7659',6,'d3','d3','d3','enabled',NULL,NULL,'2011-03-28 09:27:47',NULL,NULL),(9,'d4','ae11976937537e4c1206237dea035331',8,'d4','d4','d4','enabled',NULL,NULL,'2011-03-28 09:32:14',NULL,NULL),(10,'d5','b9884d9c846186c2a5426d7f46393de8',6,'d5','d5','d5','enabled',NULL,NULL,'2011-03-28 09:45:20',NULL,NULL),(11,'d5','b9884d9c846186c2a5426d7f46393de8',9,'d5','d5','d5','enabled',NULL,NULL,'2011-03-28 09:45:28',NULL,NULL),(12,'d6','45f9901d0370bc0facb9220619e6cbd7',10,'d6','d6','d6','enabled',NULL,NULL,'2011-03-28 09:45:41',NULL,NULL),(13,'d7','fa56b8fc724c2a681736c0d122336b98',11,'d7','d7','d7','enabled',NULL,NULL,'2011-03-28 10:43:06',NULL,NULL);
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_ip_address`
--

DROP TABLE IF EXISTS `user_ip_address`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_ip_address`
--

LOCK TABLES `user_ip_address` WRITE;
/*!40000 ALTER TABLE `user_ip_address` DISABLE KEYS */;
INSERT INTO `user_ip_address` VALUES (2,1,'10.91.30.100',1,0,'2011-03-29 09:48:28',1),(10,2,'10.91.30.101',1,1,'2011-03-28 09:46:07',1),(NULL,NULL,'10.91.30.102',1,0,NULL,1),(2,1,'10.91.30.103',1,1,'2011-03-23 18:30:54',1),(1,1,'10.91.30.104',1,1,'2011-03-23 18:09:53',1),(NULL,NULL,'10.91.30.105',1,0,NULL,1),(1,1,'10.91.30.106',1,1,'2011-03-23 18:09:53',1),(NULL,NULL,'10.91.30.107',1,0,NULL,1),(3,1,'10.91.30.108',1,1,'2011-03-24 05:50:55',1),(NULL,NULL,'10.91.30.109',1,0,NULL,1);
/*!40000 ALTER TABLE `user_ip_address` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_statistics`
--

DROP TABLE IF EXISTS `user_statistics`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_statistics`
--

LOCK TABLES `user_statistics` WRITE;
/*!40000 ALTER TABLE `user_statistics` DISABLE KEYS */;
INSERT INTO `user_statistics` VALUES (1,1,2,65653342,50181651,4915347,145449),(2,1,3,5675932,359865,0,0),(3,1,6,0,0,0,0),(4,1,8,0,0,0,0),(5,1,10,4730914,107149,0,0);
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_vm`
--

LOCK TABLES `user_vm` WRITE;
/*!40000 ALTER TABLE `user_vm` DISABLE KEYS */;
INSERT INTO `user_vm` VALUES (3,4,1,NULL,NULL,2,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(5,6,1,NULL,NULL,3,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(7,NULL,1,NULL,NULL,3,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(8,NULL,1,NULL,NULL,3,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(9,NULL,1,NULL,NULL,3,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(10,11,1,'501',NULL,3,1,'10.1.1.2','02:01:00:00:01:02','255.255.255.0',NULL,NULL,NULL,NULL),(12,NULL,1,NULL,NULL,3,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(13,NULL,1,NULL,NULL,6,2,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(15,NULL,1,NULL,NULL,8,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(17,NULL,1,NULL,NULL,2,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(18,NULL,1,NULL,NULL,2,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(19,4,1,NULL,NULL,2,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(20,21,1,NULL,NULL,10,2,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(22,NULL,1,NULL,NULL,2,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(23,4,1,'500',NULL,2,1,'10.1.1.2','02:00:01:f4:01:02','255.255.255.0',NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `user_vm` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vlan`
--

DROP TABLE IF EXISTS `vlan`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vlan`
--

LOCK TABLES `vlan` WRITE;
/*!40000 ALTER TABLE `vlan` DISABLE KEYS */;
INSERT INTO `vlan` VALUES (1,'30','10.91.30.1','255.255.255.0','10.91.30.100-10.91.30.109','VirtualNetwork',1);
/*!40000 ALTER TABLE `vlan` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vm_disk`
--

DROP TABLE IF EXISTS `vm_disk`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `vm_disk` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `instance_id` bigint(20) unsigned NOT NULL,
  `disk_offering_id` bigint(20) unsigned NOT NULL,
  `removed` datetime default NULL COMMENT 'date removed',
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

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
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vm_instance`
--

LOCK TABLES `vm_instance` WRITE;
/*!40000 ALTER TABLE `vm_instance` DISABLE KEYS */;
INSERT INTO `vm_instance` VALUES (1,'s-1-VM',NULL,NULL,'s-1-VM','Running',1,NULL,12,'06:01:dd:c7:00:01','10.91.28.104','255.255.255.0',1,NULL,1,2,2,NULL,NULL,'a7fede8b8463245d',1,0,8,'2011-03-28 12:01:57','2011-03-23 18:09:53',NULL,'SecondaryStorageVm'),(2,'v-2-VM',NULL,NULL,'v-2-VM','Running',1,NULL,12,'06:01:93:ec:00:03','10.91.28.108','255.255.255.0',1,NULL,1,2,2,NULL,NULL,'5a3acfb179fa911d',1,0,8,'2011-03-28 12:01:57','2011-03-23 18:09:53',NULL,'ConsoleProxy'),(3,'i-2-3-VM','i-2-3-VM',NULL,'i-2-3-VM-500','Expunging',2,NULL,12,NULL,NULL,NULL,1,NULL,1,NULL,NULL,NULL,NULL,'e8d001f34376a9cb',0,0,9,'2011-03-28 09:37:39','2011-03-23 18:30:54','2011-03-28 09:37:39','User'),(4,'r-4-VM',NULL,NULL,'r-4-VM-500','Running',1,NULL,12,'06:01:7a:0f:00:05','169.254.1.155','255.255.0.0',1,NULL,1,2,2,NULL,NULL,'de4353fbfebc736f',1,0,13,'2011-03-28 12:01:57','2011-03-23 18:30:54',NULL,'DomainRouter'),(5,'i-3-5-VM','i-3-5-VM',NULL,'i-3-5-VM-501','Expunging',2,NULL,12,NULL,NULL,NULL,1,NULL,1,NULL,NULL,NULL,NULL,'6669b513d2e798b7',0,0,8,'2011-03-24 05:50:31','2011-03-24 05:42:44','2011-03-24 05:50:31','User'),(6,'r-6-VM',NULL,NULL,'r-6-VM-501','Destroyed',1,NULL,12,'06:01:5f:36:00:07',NULL,'255.255.0.0',1,NULL,1,NULL,2,NULL,NULL,'11c9e4440a43a563',1,0,7,'2011-03-24 05:50:31','2011-03-24 05:42:44','2011-03-24 05:50:31','DomainRouter'),(7,'i-3-7-VM','i-3-7-VM',NULL,'i-3-7-VM','Expunging',2,NULL,12,NULL,NULL,NULL,NULL,NULL,1,NULL,NULL,NULL,NULL,'59a4545d54f5720f',0,0,2,'2011-03-24 05:43:05','2011-03-24 05:42:50','2011-03-24 05:43:05','User'),(8,'i-3-8-VM','i-3-8-VM',NULL,'i-3-8-VM','Expunging',2,NULL,12,NULL,NULL,NULL,NULL,NULL,1,NULL,NULL,NULL,NULL,'69a3672295ea37e0',0,0,2,'2011-03-24 05:43:40','2011-03-24 05:43:27','2011-03-24 05:43:40','User'),(9,'i-3-9-VM','i-3-9-VM',NULL,'i-3-9-VM','Expunging',2,NULL,12,NULL,NULL,NULL,NULL,NULL,1,NULL,NULL,NULL,NULL,'c68fe3b9a450a9c',0,0,2,'2011-03-24 05:43:55','2011-03-24 05:43:40','2011-03-24 05:43:55','User'),(10,'i-3-10-VM','i-3-10-VM',NULL,'i-3-10-VM-501','Running',2,NULL,12,'02:01:00:00:01:02','10.1.1.2','255.255.255.0',1,NULL,1,2,2,NULL,NULL,'9deff5fd381fd743',0,0,6,'2011-03-28 12:01:57','2011-03-24 05:50:55',NULL,'User'),(11,'r-11-VM',NULL,NULL,'r-11-VM-501','Stopped',1,NULL,12,'06:01:55:38:00:09',NULL,'255.255.0.0',1,NULL,1,NULL,2,NULL,NULL,'816044c48bd2a7d1',1,0,6,'2011-03-28 09:37:36','2011-03-24 05:50:55',NULL,'DomainRouter'),(12,'i-3-12-VM','i-3-12-VM',NULL,'i-3-12-VM','Expunging',2,NULL,12,NULL,NULL,NULL,NULL,NULL,1,NULL,NULL,NULL,NULL,'303105d267c3b6a5',0,0,2,'2011-03-24 05:53:02','2011-03-24 05:52:51','2011-03-24 05:53:02','User'),(13,'i-6-13-VM','i-6-13-VM',NULL,'i-6-13-VM','Expunging',2,NULL,12,NULL,NULL,NULL,NULL,NULL,1,NULL,NULL,NULL,NULL,'cb3b4320fd013115',0,0,2,'2011-03-28 09:28:19','2011-03-28 09:28:04','2011-03-28 09:28:19','User'),(15,'i-8-15-VM','i-8-15-VM',NULL,'i-8-15-VM','Expunging',2,NULL,12,NULL,NULL,NULL,NULL,NULL,1,NULL,NULL,NULL,NULL,'ca682b781cef8f64',0,0,2,'2011-03-28 09:32:39','2011-03-28 09:32:27','2011-03-28 09:32:39','User'),(17,'i-2-17-VM','i-2-17-VM',NULL,'i-2-17-VM','Expunging',2,NULL,12,NULL,NULL,NULL,NULL,NULL,1,NULL,NULL,NULL,NULL,'d03051a93f12d77d',0,0,2,'2011-03-28 09:33:04','2011-03-28 09:32:53','2011-03-28 09:33:04','User'),(18,'i-2-18-VM','i-2-18-VM',NULL,'i-2-18-VM','Expunging',2,NULL,12,NULL,NULL,NULL,NULL,NULL,1,NULL,NULL,NULL,NULL,'79805917b137d91c',0,0,2,'2011-03-28 09:37:45','2011-03-28 09:37:29','2011-03-28 09:37:45','User'),(19,'i-2-19-VM','i-2-19-VM',NULL,'i-2-19-VM-500','Expunging',201,NULL,12,NULL,NULL,NULL,1,NULL,1,NULL,NULL,NULL,NULL,'bf201bbe7080ff7f',0,0,8,'2011-03-28 09:44:50','2011-03-28 09:38:15','2011-03-28 09:44:50','User'),(20,'i-10-20-VM','i-10-20-VM',NULL,'i-10-20-VM-502','Expunging',2,NULL,12,NULL,NULL,NULL,1,NULL,1,NULL,NULL,NULL,NULL,'87ac6d15e4b76078',0,0,8,'2011-03-28 11:51:01','2011-03-28 09:46:07','2011-03-28 11:51:01','User'),(21,'r-21-VM',NULL,NULL,'r-21-VM-502','Stopped',1,NULL,12,'06:01:59:3c:00:0d',NULL,'255.255.0.0',1,NULL,1,NULL,2,NULL,NULL,'9c7a7520106fc635',1,0,8,'2011-03-28 12:02:19','2011-03-28 09:46:07',NULL,'DomainRouter'),(22,'i-2-22-VM','i-2-22-VM',NULL,'i-2-22-VM','Expunging',2,NULL,12,NULL,NULL,NULL,NULL,NULL,1,NULL,NULL,NULL,NULL,'cce5c7ced02ab95c',0,0,2,'2011-03-28 11:50:31','2011-03-28 11:50:18','2011-03-28 11:50:31','User'),(23,'i-2-23-VM','i-2-23-VM',NULL,'i-2-23-VM-500','Running',2,NULL,12,'02:00:01:f4:01:02','10.1.1.2','255.255.255.0',1,NULL,1,2,2,NULL,NULL,'f66fe09cbf786d0b',0,0,6,'2011-03-28 12:01:57','2011-03-28 11:51:18',NULL,'User');
/*!40000 ALTER TABLE `vm_instance` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vm_template`
--

DROP TABLE IF EXISTS `vm_template`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
) ENGINE=InnoDB AUTO_INCREMENT=202 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vm_template`
--

LOCK TABLES `vm_template` WRITE;
/*!40000 ALTER TABLE `vm_template` DISABLE KEYS */;
INSERT INTO `vm_template` VALUES (1,'routing','SystemVM Template',0,0,'ext3',0,64,'http://10.91.28.6/releases/2.0.0RC5/systemvm.vhd.bz2','VHD','2011-03-23 20:40:28',NULL,1,'31cd7ce94fe68c973d5dc37c3349d02e','SystemVM Template',0,12,1,0,1),(2,'centos53-x86_64','CentOS 5.3(x86_64) no GUI',1,1,'ext3',0,64,'http://10.91.28.6/templates/builtin/f59f18fb-ae94-4f97-afd2-f84755767aca.vhd.bz2','VHD','2011-03-23 20:40:28',NULL,1,'b63d854a9560c013142567bbae8d98cf','CentOS 5.3(x86_64) no GUI',0,12,1,0,1),(200,'xs-tools.iso','xs-tools.iso',1,1,'cdfs',1,64,'/opt/xensource/packages/iso/xs-tools-5.5.0.iso','ISO','2011-03-23 18:08:23',NULL,1,NULL,'xen-pv-drv-iso',0,1,0,0,0),(201,'201-3-8c0723e6-0516-3139-9262-ba6f873a2cd5','sd',1,0,'ext3',1,64,'http://10.91.28.6/templates/centos53-x86_64/latest/f59f18fb-ae94-4f97-afd2-f84755767aca.vhd.bz2','VHD','2011-03-24 05:54:08','2011-03-29 09:42:43',3,NULL,'df',0,12,1,0,0);
/*!40000 ALTER TABLE `vm_template` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `volumes`
--

DROP TABLE IF EXISTS `volumes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `volumes`
--

LOCK TABLES `volumes` WRITE;
/*!40000 ALTER TABLE `volumes` DISABLE KEYS */;
INSERT INTO `volumes` VALUES (1,1,1,200,1,0,'s-1-VM-ROOT',2147483648,'/export/home/idc218/primary','3edfad78-d136-4dfb-85d3-c89bdfce481f',1,1,NULL,NULL,'ROOT','STORAGE_POOL','NetworkFilesystem','NOT_MIRRORED',NULL,9,1,NULL,1,0,'2011-03-23 18:09:53',NULL,NULL,'Created'),(2,1,1,200,2,0,'v-2-VM-ROOT',2147483648,'/export/home/idc218/primary','71a4f832-d819-4ff7-8b20-724109167a3a',1,1,NULL,NULL,'ROOT','STORAGE_POOL','NetworkFilesystem','NOT_MIRRORED',NULL,10,1,NULL,1,0,'2011-03-23 18:09:53',NULL,NULL,'Created'),(3,2,1,200,4,0,'r-4-VM-ROOT',2147483648,'/export/home/idc218/primary','310eb11e-ee5d-467f-9f1c-5a31098b56c0',1,1,NULL,NULL,'ROOT','STORAGE_POOL','NetworkFilesystem','NOT_MIRRORED',NULL,8,1,NULL,1,0,'2011-03-23 18:30:54',NULL,NULL,'Created'),(4,2,1,200,NULL,NULL,'i-2-3-VM-ROOT',8589934592,'/export/home/idc218/primary','915e2579-a728-4681-8ea3-158fe0f1643b',1,1,NULL,NULL,'ROOT','STORAGE_POOL','NetworkFilesystem','NOT_MIRRORED',NULL,1,2,NULL,0,1,'2011-03-23 18:30:54','2011-03-28 09:37:39','2011-03-28 09:37:39','Created'),(5,3,1,200,NULL,NULL,'r-6-VM-ROOT',2147483648,'/export/home/idc218/primary','67bf631e-32cb-453e-8a72-8e9ec1097912',1,1,NULL,NULL,'ROOT','STORAGE_POOL','NetworkFilesystem','NOT_MIRRORED',NULL,8,1,NULL,1,1,'2011-03-24 05:42:44','2011-03-24 05:50:31','2011-03-24 05:50:32','Created'),(6,3,1,200,NULL,NULL,'i-3-5-VM-ROOT',8589934592,'/export/home/idc218/primary','c5bc62c2-7da9-4e37-9c80-89796f500d71',1,1,NULL,NULL,'ROOT','STORAGE_POOL','NetworkFilesystem','NOT_MIRRORED',NULL,1,2,NULL,0,1,'2011-03-24 05:42:45','2011-03-24 05:50:31','2011-03-24 05:50:31','Created'),(7,3,1,200,11,0,'r-11-VM-ROOT',2147483648,'/export/home/idc218/primary','1fbf0c50-9c6e-4789-b7f2-bcd4753ee554',1,1,NULL,NULL,'ROOT','STORAGE_POOL','NetworkFilesystem','NOT_MIRRORED',NULL,8,1,NULL,1,0,'2011-03-24 05:50:55',NULL,NULL,'Created'),(8,3,1,200,10,0,'i-3-10-VM-ROOT',8589934592,'/export/home/idc218/primary','de2d2dd9-a659-479d-9eb5-b50ca5e22529',1,1,NULL,NULL,'ROOT','STORAGE_POOL','NetworkFilesystem','NOT_MIRRORED',NULL,1,2,NULL,0,0,'2011-03-24 05:50:56',NULL,NULL,'Created'),(9,2,1,200,NULL,NULL,'i-2-19-VM-ROOT',8589934592,'/export/home/idc218/primary','d06a8f56-f2fd-406a-826d-c010365db9a7',1,1,NULL,NULL,'ROOT','STORAGE_POOL','NetworkFilesystem','NOT_MIRRORED',NULL,1,201,NULL,0,1,'2011-03-28 09:38:15','2011-03-28 09:44:50','2011-03-28 09:44:50','Created'),(10,10,2,200,21,0,'r-21-VM-ROOT',2147483648,'/export/home/idc218/primary','0be7c70c-92ab-4c84-92cc-1e486cd2912e',1,1,NULL,NULL,'ROOT','STORAGE_POOL','NetworkFilesystem','NOT_MIRRORED',NULL,8,1,NULL,1,0,'2011-03-28 09:46:07',NULL,NULL,'Created'),(11,10,2,200,NULL,NULL,'i-10-20-VM-ROOT',8589934592,'/export/home/idc218/primary','e9cf5ae8-1ba3-42a9-8fa9-a445bac61cb9',1,1,NULL,NULL,'ROOT','STORAGE_POOL','NetworkFilesystem','NOT_MIRRORED',NULL,1,2,NULL,0,1,'2011-03-28 09:46:08','2011-03-28 11:51:01','2011-03-28 11:51:01','Created'),(12,2,1,200,NULL,NULL,'vv',5368709120,'/export/home/idc218/primary','67f0ca0f-a679-4a37-ad46-6fbef5fc7b7d',1,1,NULL,NULL,'DATADISK','STORAGE_POOL','NetworkFilesystem','NOT_MIRRORED',NULL,5,NULL,NULL,0,0,'2011-03-28 11:48:28','2011-03-28 11:48:28',NULL,'Created'),(13,6,2,200,NULL,NULL,'v1',5368709120,'/export/home/idc218/primary','da1b51c3-4115-4631-b632-8eaddabf5f0e',1,1,NULL,NULL,'DATADISK','STORAGE_POOL','NetworkFilesystem','NOT_MIRRORED',NULL,5,NULL,NULL,0,1,'2011-03-28 11:49:13','2011-03-29 09:42:31','2011-03-29 09:42:42','Created'),(14,2,1,200,23,0,'i-2-23-VM-ROOT',8589934592,'/export/home/idc218/primary','a650c31f-4cac-45cc-9332-7024e8784f84',1,1,NULL,NULL,'ROOT','STORAGE_POOL','NetworkFilesystem','NOT_MIRRORED',NULL,1,2,'214d273e-52ab-4510-a7d7-6796558e4dbd',0,0,'2011-03-28 11:51:18',NULL,NULL,'Created'),(15,2,1,200,23,1,'i-2-23-VM-DATA',5368709120,'/export/home/idc218/primary','b55696c1-e635-4c6c-9c26-d95d39eb4847',1,1,NULL,NULL,'DATADISK','STORAGE_POOL','NetworkFilesystem','NOT_MIRRORED',NULL,5,NULL,NULL,0,0,'2011-03-28 11:51:18','2011-03-28 11:57:58',NULL,'Created');
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

-- Dump completed on 2011-03-29 15:29:18
