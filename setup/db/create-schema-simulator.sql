DROP TABLE IF EXISTS `cloud`.`mockhost`;
DROP TABLE IF EXISTS `cloud`.`mocksecstorage`;
DROP TABLE IF EXISTS `cloud`.`mockstoragepool`;
DROP TABLE IF EXISTS `cloud`.`mockvm`;
DROP TABLE IF EXISTS `cloud`.`mockvolume`;

CREATE TABLE  `cloud`.`mockhost` (
  `id` bigint unsigned NOT NULL auto_increment,
  `name` varchar(255) NOT NULL,
  `private_ip_address` char(40),
  `private_mac_address` varchar(17),
  `private_netmask` varchar(15),
  `storage_ip_address` char(40),
  `storage_netmask` varchar(15),
  `storage_mac_address` varchar(17),
  `public_ip_address` char(40),
  `public_netmask` varchar(15),
  `public_mac_address` varchar(17),
  `guid` varchar(255) UNIQUE,
  `version` varchar(40) NOT NULL,
  `data_center_id` bigint unsigned NOT NULL,
  `pod_id` bigint unsigned,
  `cluster_id` bigint unsigned COMMENT 'foreign key to cluster',
  `cpus` int(10) unsigned,
  `speed` int(10) unsigned,
  `ram` bigint unsigned,
  `capabilities` varchar(255) COMMENT 'host capabilities in comma separated list',
  `vm_id` bigint unsigned,
  `resource` varchar(255) DEFAULT NULL COMMENT 'If it is a local resource, this is the class name',
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `cloud`.`mocksecstorage` (
  `id` bigint unsigned NOT NULL auto_increment,
  `url` varchar(255),
  `capacity` bigint unsigned,
  `mount_point` varchar(255),
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `cloud`.`mockstoragepool` (
  `id` bigint unsigned NOT NULL auto_increment,
  `guid` varchar(255),
  `mount_point` varchar(255),
  `capacity` bigint,
  `pool_type` varchar(40),
  `hostguid` varchar(255) UNIQUE,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE `cloud`.`mockvm` (
  `id` bigint unsigned NOT NULL auto_increment,
  `name` varchar(255),
  `host_id` bigint unsigned,
  `type` varchar(40),
  `state` varchar(40),
  `vnc_port` bigint unsigned,
  `memory` bigint unsigned,
  `cpu` bigint unsigned,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE `cloud`.`mockvolume` (
  `id` bigint unsigned NOT NULL auto_increment,
  `name` varchar(255),
  `size` bigint unsigned,
  `path` varchar(255),
  `pool_id` bigint unsigned,
  `type` varchar(40),
  `status` varchar(40),
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE `cloud`.`mockconfiguration` (
  `id` bigint unsigned NOT NULL auto_increment,
  `data_center_id` bigint unsigned,
  `pod_id` bigint unsigned,
  `cluster_id` bigint unsigned,
  `host_id` bigint unsigned,
  `name` varchar(255),
  `values` varchar(4095),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `cloud`.`mocksecurityrules` (
  `id` bigint unsigned NOT NULL auto_increment,
  `vmid` bigint unsigned,
  `signature` varchar(255),
  `ruleset` varchar(4095),
  `hostid` bigint unsigned,
  `seqnum` bigint unsigned,
  `vmname` varchar(255),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
