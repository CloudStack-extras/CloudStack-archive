# This file is obsolete!  Put what you need into create-schema.sql

ALTER TABLE `cloud`.`account` ADD CONSTRAINT `fk_account__domain_id` FOREIGN KEY `fk_account__domain_id` (`domain_id`) REFERENCES `domain` (`id`);
ALTER TABLE `cloud`.`account` ADD INDEX `i_account__domain_id`(`domain_id`);

ALTER TABLE `cloud`.`account` ADD INDEX `i_account__cleanup_needed`(`cleanup_needed`);
ALTER TABLE `cloud`.`account` ADD INDEX `i_account__account_name__domain_id__removed`(`account_name`, `domain_id`, `removed`); 

ALTER TABLE `cloud`.`resource_limit` ADD CONSTRAINT `fk_resource_limit__domain_id` FOREIGN KEY `fk_resource_limit__domain_id` (`domain_id`) REFERENCES `domain` (`id`) ON DELETE CASCADE;
ALTER TABLE `cloud`.`resource_limit` ADD INDEX `i_resource_limit__domain_id`(`domain_id`);
ALTER TABLE `cloud`.`resource_limit` ADD CONSTRAINT `fk_resource_limit__account_id` FOREIGN KEY `fk_resource_limit__account_id` (`account_id`) REFERENCES `account` (`id`) ON DELETE CASCADE;
ALTER TABLE `cloud`.`resource_limit` ADD INDEX `i_resource_limit__account_id`(`account_id`);
    
ALTER TABLE `cloud`.`event` ADD INDEX `i_event__created`(`created`);
ALTER TABLE `cloud`.`event` ADD INDEX `i_event__user_id`(`user_id`);
ALTER TABLE `cloud`.`event` ADD INDEX `i_event__account_id` (`account_id`);
ALTER TABLE `cloud`.`event` ADD INDEX `i_event__level_id`(`level`);
ALTER TABLE `cloud`.`event` ADD INDEX `i_event__type_id`(`type`);

ALTER TABLE `cloud`.`vm_template` ADD INDEX `i_vm_template__public`(`public`);

ALTER TABLE `cloud`.`upload` ADD CONSTRAINT `fk_upload__host_id` FOREIGN KEY `fk_upload__host_id` (`host_id`) REFERENCES `host` (`id`) ON DELETE CASCADE;
ALTER TABLE `cloud`.`upload` ADD INDEX `i_upload__host_id`(`host_id`);
ALTER TABLE `cloud`.`upload` ADD INDEX `i_upload__type_id`(`type_id`);

ALTER TABLE `cloud`.`user_statistics` ADD CONSTRAINT `fk_user_statistics__account_id` FOREIGN KEY `fk_user_statistics__account_id` (`account_id`) REFERENCES `account` (`id`) ON DELETE CASCADE;
ALTER TABLE `cloud`.`user_statistics` ADD INDEX `i_user_statistics__account_id`(`account_id`);

ALTER TABLE `cloud`.`user_statistics` ADD INDEX `i_user_statistics__account_id_data_center_id`(`account_id`, `data_center_id`);

ALTER TABLE `cloud`.`snapshots` ADD CONSTRAINT `fk_snapshots__account_id` FOREIGN KEY `fk_snapshots__account_id` (`account_id`) REFERENCES `account` (`id`);
ALTER TABLE `cloud`.`snapshots` ADD INDEX `i_snapshots__account_id`(`account_id`);
ALTER TABLE `cloud`.`snapshots` ADD INDEX `i_snapshots__volume_id`(`volume_id`);
ALTER TABLE `cloud`.`snapshots` ADD INDEX `i_snapshots__name`(`name`);
ALTER TABLE `cloud`.`snapshots` ADD INDEX `i_snapshots__snapshot_type`(`snapshot_type`);
ALTER TABLE `cloud`.`snapshots` ADD INDEX `i_snapshots__prev_snap_id`(`prev_snap_id`);

ALTER TABLE `cloud`.`snapshot_policy` ADD INDEX `i_snapshot_policy__volume_id`(`volume_id`);

ALTER TABLE `cloud`.`snapshot_schedule` ADD CONSTRAINT `fk__snapshot_schedule_volume_id` FOREIGN KEY `fk_snapshot_schedule__volume_id` (`volume_id`) REFERENCES `volumes` (`id`) ON DELETE CASCADE;
ALTER TABLE `cloud`.`snapshot_schedule` ADD INDEX `i_snapshot_schedule__volume_id`(`volume_id`);
ALTER TABLE `cloud`.`snapshot_schedule` ADD CONSTRAINT `fk__snapshot_schedule_policy_id` FOREIGN KEY `fk_snapshot_schedule__policy_id` (`policy_id`) REFERENCES `snapshot_policy` (`id`) ON DELETE CASCADE;
ALTER TABLE `cloud`.`snapshot_schedule` ADD INDEX `i_snapshot_schedule__policy_id`(`policy_id`);
ALTER TABLE `cloud`.`snapshot_schedule` ADD CONSTRAINT `fk__snapshot_schedule_async_job_id` FOREIGN KEY `fk_snapshot_schedule__async_job_id` (`async_job_id`) REFERENCES `async_job` (`id`) ON DELETE CASCADE;
ALTER TABLE `cloud`.`snapshot_schedule` ADD INDEX `i_snapshot_schedule__async_job_id`(`async_job_id`);
ALTER TABLE `cloud`.`snapshot_schedule` ADD CONSTRAINT `fk__snapshot_schedule_snapshot_id` FOREIGN KEY `fk_snapshot_schedule__snapshot_id` (`snapshot_id`) REFERENCES `snapshots` (`id`) ON DELETE CASCADE;
ALTER TABLE `cloud`.`snapshot_schedule` ADD INDEX `i_snapshot_schedule__snapshot_id`(`snapshot_id`);
ALTER TABLE `cloud`.`snapshot_schedule` ADD INDEX `i_snapshot_schedule__scheduled_timestamp`(`scheduled_timestamp`);


ALTER TABLE `cloud`.`stack_maid` ADD INDEX `i_stack_maid_msid_thread_id`(`msid`, `thread_id`);
ALTER TABLE `cloud`.`stack_maid` ADD INDEX `i_stack_maid_seq`(`msid`, `seq`);
ALTER TABLE `cloud`.`stack_maid` ADD INDEX `i_stack_maid_created`(`created`);

ALTER TABLE `cloud`.`launch_permission` ADD INDEX `i_launch_permission_template_id`(`template_id`);

ALTER TABLE `cloud`.`guest_os` ADD CONSTRAINT `fk_guest_os__category_id` FOREIGN KEY `fk_guest_os__category_id` (`category_id`) REFERENCES `guest_os_category` (`id`) ON DELETE CASCADE;

ALTER TABLE `cloud`.`security_group` ADD CONSTRAINT `fk_security_group__account_id` FOREIGN KEY `fk_security_group__account_id` (`account_id`) REFERENCES `account` (`id`) ON DELETE CASCADE;
ALTER TABLE `cloud`.`security_group` ADD CONSTRAINT `fk_security_group__domain_id` FOREIGN KEY `fk_security_group__domain_id` (`domain_id`) REFERENCES `domain` (`id`);
ALTER TABLE `cloud`.`security_group` ADD INDEX `i_security_group_name`(`name`);

ALTER TABLE `cloud`.`security_ingress_rule` ADD CONSTRAINT `fk_security_ingress_rule___security_group_id` FOREIGN KEY `fk_security_ingress_rule__security_group_id` (`security_group_id`) REFERENCES `security_group` (`id`) ON DELETE CASCADE;
ALTER TABLE `cloud`.`security_ingress_rule` ADD CONSTRAINT `fk_security_ingress_rule___allowed_network_id` FOREIGN KEY `fk_security_ingress_rule__allowed_network_id` (`allowed_network_id`) REFERENCES `security_group` (`id`) ON DELETE CASCADE;
ALTER TABLE `cloud`.`security_ingress_rule` ADD INDEX `i_security_ingress_rule_network_id`(`security_group_id`);
ALTER TABLE `cloud`.`security_ingress_rule` ADD INDEX `i_security_ingress_rule_allowed_network`(`allowed_network_id`);

ALTER TABLE `cloud`.`security_group_vm_map` ADD CONSTRAINT `fk_security_group_vm_map___security_group_id` FOREIGN KEY `fk_security_group_vm_map___security_group_id` (`security_group_id`) REFERENCES `security_group` (`id`) ON DELETE CASCADE;
ALTER TABLE `cloud`.`security_group_vm_map` ADD CONSTRAINT `fk_security_group_vm_map___instance_id` FOREIGN KEY `fk_security_group_vm_map___instance_id` (`instance_id`) REFERENCES `user_vm` (`id`) ON DELETE CASCADE;

ALTER TABLE `cloud`.`instance_group` ADD CONSTRAINT `fk_instance_group__account_id` FOREIGN KEY `fk_instance_group__account_id` (`account_id`) REFERENCES `account` (`id`);

ALTER TABLE `cloud`.`instance_group_vm_map` ADD CONSTRAINT `fk_instance_group_vm_map___group_id` FOREIGN KEY `fk_instance_group_vm_map___group_id` (`group_id`) REFERENCES `instance_group` (`id`) ON DELETE CASCADE;
ALTER TABLE `cloud`.`instance_group_vm_map` ADD CONSTRAINT `fk_instance_group_vm_map___instance_id` FOREIGN KEY `fk_instance_group_vm_map___instance_id` (`instance_id`) REFERENCES `user_vm` (`id`) ON DELETE CASCADE;

ALTER TABLE `cloud`.`ssh_keypairs` ADD CONSTRAINT `fk_ssh_keypairs__account_id` FOREIGN KEY `fk_ssh_keypairs__account_id` (`account_id`) REFERENCES `account` (`id`) ON DELETE CASCADE;
ALTER TABLE `cloud`.`ssh_keypairs` ADD CONSTRAINT `fk_ssh_keypairs__domain_id` FOREIGN KEY `fk_ssh_keypairs__domain_id` (`domain_id`) REFERENCES `domain` (`id`) ON DELETE CASCADE;


ALTER TABLE `cloud`.`usage_event` ADD INDEX `i_usage_event__created`(`created`);
