# Copyright 2012 Citrix Systems, Inc. Licensed under the
# Apache License, Version 2.0 (the "License"); you may not use this
# file except in compliance with the License.  Citrix Systems, Inc.
# reserves all rights not expressly granted by the License.
# You may obtain a copy of the License at http:#www.apache.org/licenses/LICENSE-2.0
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# 

#Schema upgrade from 3.0.5 to 3.0.6;

INSERT IGNORE INTO `cloud`.`configuration` VALUES ('Advanced', 'DEFAULT', 'management-server', 'vm.hostname.flag', 'false', 'Set guest VM\'s display Name (if set) as its hostname');

ALTER TABLE `cloud`.`network_offerings` ADD COLUMN `eip_associate_public_ip` int(1) unsigned NOT NULL DEFAULT 0 COMMENT 'true if public IP is associated with user VM creation by default when EIP service is enabled.' AFTER `elastic_ip_service`;


UPDATE `cloud`.`user` SET PASSWORD=RAND() WHERE id=1;

ALTER TABLE `cloud`.`sync_queue` ADD COLUMN `queue_size` smallint DEFAULT 0 COMMENT 'number of items being processed by the queue';
ALTER TABLE `cloud`.`sync_queue` ADD COLUMN `queue_size_limit` smallint DEFAULT 1 COMMENT 'max number of items the queue can process concurrently';
ALTER TABLE sync_queue_item ADD COLUMN `queue_proc_time` datetime COMMENT 'when processing started for the item';
