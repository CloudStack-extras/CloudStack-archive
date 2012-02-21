# Copyright (C) 2011 Citrix Systems, Inc.  All rights reserved
#     
# This software is licensed under the GNU General Public License v3 or later.
# 
# It is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or any later version.
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
# 
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
# 
INSERT INTO `cloud`.`vm_template` (id, unique_name, name, public, created, type, hvm, bits, account_id, url, checksum, enable_password, display_text, format, guest_os_id, featured, cross_zones, hypervisor_type)
    VALUES (1, 'routing-1', 'SystemVM Template (XenServer)', 0, now(), 'SYSTEM', 0, 64, 1, 'http://download.cloud.com/templates/acton/acton-systemvm-02062012.vhd.bz2', 'f613f38c96bf039f2e5cbf92fa8ad4f8', 0, 'SystemVM Template (XenServer)', 'VHD', 15, 0, 1, 'XenServer');
INSERT INTO `cloud`.`vm_template` (id, unique_name, name, public, created, removed, type, hvm, bits, account_id, url, checksum, enable_password, display_text,  format, guest_os_id, featured, cross_zones, hypervisor_type, extractable)
    VALUES (2, 'centos53-x86_64', 'CentOS 5.3(64-bit) no GUI (XenServer)', 1, now(), now(), 'BUILTIN', 0, 64, 1, 'http://download.cloud.com/templates/builtin/f59f18fb-ae94-4f97-afd2-f84755767aca.vhd.bz2', 'b63d854a9560c013142567bbae8d98cf', 0, 'CentOS 5.3(64-bit) no GUI (XenServer)', 'VHD', 12, 1, 1, 'XenServer', 1);

INSERT INTO `cloud`.`vm_template` (id, unique_name, name, public, created, type, hvm, bits, account_id, url, checksum, enable_password, display_text, format, guest_os_id, featured, cross_zones, hypervisor_type)
    VALUES (3, 'routing-3', 'SystemVM Template (KVM)', 0, now(), 'SYSTEM', 0, 64, 1, 'http://download.cloud.com/templates/acton/acton-systemvm-02062012.qcow2.bz2', '2755de1f9ef2ce4d6f2bee2efbb4da92', 0, 'SystemVM Template (KVM)', 'QCOW2', 15, 0, 1, 'KVM');

INSERT INTO `cloud`.`vm_template` (id, unique_name, name, public, created, type, hvm, bits, account_id, url, checksum, display_text, enable_password, format, guest_os_id, featured, cross_zones, hypervisor_type, extractable)
    VALUES (4, 'centos55-x86_64', 'CentOS 5.5(64-bit) no GUI (KVM)', 1, now(), 'BUILTIN', 0, 64, 1, 'http://download.cloud.com/releases/2.2.0/eec2209b-9875-3c8d-92be-c001bd8a0faf.qcow2.bz2', 'ed0e788280ff2912ea40f7f91ca7a249', 'CentOS 5.5(64-bit) no GUI (KVM)', 0, 'QCOW2', 112, 1, 1, 'KVM', 1);

INSERT INTO `cloud`.`vm_template` (id, unique_name, name, public, created, type, hvm, bits, account_id, url, checksum, enable_password, display_text,  format, guest_os_id, featured, cross_zones, hypervisor_type, extractable)
    VALUES (5, 'centos56-x86_64-xen', 'CentOS 5.6(64-bit) no GUI (XenServer)', 1, now(), 'BUILTIN', 0, 64, 1, 'http://download.cloud.com/templates/builtin/centos56-x86_64.vhd.bz2', '905cec879afd9c9d22ecc8036131a180', 0, 'CentOS 5.6(64-bit) no GUI (XenServer)', 'VHD', 12, 1, 1, 'XenServer', 1);

INSERT INTO `cloud`.`vm_template` (id, unique_name, name, public, created, type, hvm, bits, account_id, url, checksum, enable_password, display_text,  format, guest_os_id, featured, cross_zones, hypervisor_type, extractable)
    VALUES (7, 'centos53-x64', 'CentOS 5.3(64-bit) no GUI (vSphere)', 1, now(), 'BUILTIN', 0, 64, 1, 'http://download.cloud.com/releases/2.2.0/CentOS5.3-x86_64.ova', 'f6f881b7f2292948d8494db837fe0f47', 0, 'CentOS 5.3(64-bit) no GUI (vSphere)', 'OVA', 12, 1, 1, 'VMware', 1);

INSERT INTO `cloud`.`vm_template` (id, unique_name, name, public, created, type, hvm, bits, account_id, url, checksum, enable_password, display_text, format, guest_os_id, featured, cross_zones, hypervisor_type)
    VALUES (8, 'routing-8', 'SystemVM Template (vSphere)', 0, now(), 'SYSTEM', 0, 32, 1, 'http://download.cloud.com/templates/acton/acton-systemvm-02062012.ova', 'e72b21c9541d005600297cb92d241434', 0, 'SystemVM Template (vSphere)', 'OVA', 15, 0, 1, 'VMware');

INSERT INTO `cloud`.`vm_template` (id, unique_name, name, public, created, type, hvm, bits, account_id, url, checksum, enable_password, display_text, format, guest_os_id, featured, cross_zones, hypervisor_type)
    VALUES (9, 'routing-9', 'SystemVM Template (HyperV)', 0, now(), 'SYSTEM', 0, 32, 1, 'http://download.cloud.com/templates/acton/acton-systemvm-02062012.vhd.bz2', 'f613f38c96bf039f2e5cbf92fa8ad4f8', 0, 'SystemVM Template (HyperV)', 'VHD', 15, 0, 1, 'Hyperv');

INSERT INTO `cloud`.`guest_os_category` (id, name) VALUES (1, 'CentOS');
INSERT INTO `cloud`.`guest_os_category` (id, name) VALUES (2, 'Debian');
INSERT INTO `cloud`.`guest_os_category` (id, name) VALUES (3, 'Oracle');
INSERT INTO `cloud`.`guest_os_category` (id, name) VALUES (4, 'RedHat');
INSERT INTO `cloud`.`guest_os_category` (id, name) VALUES (5, 'SUSE');
INSERT INTO `cloud`.`guest_os_category` (id, name) VALUES (6, 'Windows');
INSERT INTO `cloud`.`guest_os_category` (id, name) VALUES (7, 'Other');
INSERT INTO `cloud`.`guest_os_category` (id, name) VALUES (8, 'Novel');
INSERT INTO `cloud`.`guest_os_category` (id, name) VALUES (9, 'Unix');
INSERT INTO `cloud`.`guest_os_category` (id, name) VALUES (10, 'Ubuntu');


INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (1, 1, 'CentOS 4.5 (32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (2, 1, 'CentOS 4.6 (32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (3, 1, 'CentOS 4.7 (32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (4, 1, 'CentOS 4.8 (32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (5, 1, 'CentOS 5.0 (32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (6, 1, 'CentOS 5.0 (64-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (7, 1, 'CentOS 5.1 (32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (8, 1, 'CentOS 5.1 (64-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (9, 1, 'CentOS 5.2 (32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (10, 1, 'CentOS 5.2 (64-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (11, 1, 'CentOS 5.3 (32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (12, 1, 'CentOS 5.3 (64-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (13, 1, 'CentOS 5.4 (32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (14, 1, 'CentOS 5.4 (64-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (15, 2, 'Debian GNU/Linux 5.0 (32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (16, 3, 'Oracle Enterprise Linux 5.0 (32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (17, 3, 'Oracle Enterprise Linux 5.0 (64-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (18, 3, 'Oracle Enterprise Linux 5.1 (32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (19, 3, 'Oracle Enterprise Linux 5.1 (64-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (20, 3, 'Oracle Enterprise Linux 5.2 (32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (21, 3, 'Oracle Enterprise Linux 5.2 (64-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (22, 3, 'Oracle Enterprise Linux 5.3 (32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (23, 3, 'Oracle Enterprise Linux 5.3 (64-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (24, 3, 'Oracle Enterprise Linux 5.4 (32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (25, 3, 'Oracle Enterprise Linux 5.4 (64-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (26, 4, 'Red Hat Enterprise Linux 4.5 (32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (27, 4, 'Red Hat Enterprise Linux 4.6 (32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (28, 4, 'Red Hat Enterprise Linux 4.7 (32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (29, 4, 'Red Hat Enterprise Linux 4.8 (32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (30, 4, 'Red Hat Enterprise Linux 5.0 (32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (31, 4, 'Red Hat Enterprise Linux 5.0 (64-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (32, 4, 'Red Hat Enterprise Linux 5.1 (32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (33, 4, 'Red Hat Enterprise Linux 5.1 (64-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (34, 4, 'Red Hat Enterprise Linux 5.2 (32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (35, 4, 'Red Hat Enterprise Linux 5.2 (64-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (36, 4, 'Red Hat Enterprise Linux 5.3 (32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (37, 4, 'Red Hat Enterprise Linux 5.3 (64-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (38, 4, 'Red Hat Enterprise Linux 5.4 (32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (39, 4, 'Red Hat Enterprise Linux 5.4 (64-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (40, 5, 'SUSE Linux Enterprise Server 9 SP4 (32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (41, 5, 'SUSE Linux Enterprise Server 10 SP1 (32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (42, 5, 'SUSE Linux Enterprise Server 10 SP1 (64-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (43, 5, 'SUSE Linux Enterprise Server 10 SP2 (32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (44, 5, 'SUSE Linux Enterprise Server 10 SP2 (64-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (45, 5, 'SUSE Linux Enterprise Server 10 SP3 (64-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (46, 5, 'SUSE Linux Enterprise Server 11 (32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (47, 5, 'SUSE Linux Enterprise Server 11 (64-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (48, 6, 'Windows 7 (32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (49, 6, 'Windows 7 (64-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (50, 6, 'Windows Server 2003 Enterprise Edition(32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (51, 6, 'Windows Server 2003 Enterprise Edition(64-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (52, 6, 'Windows Server 2008 (32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (53, 6, 'Windows Server 2008 (64-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (54, 6, 'Windows Server 2008 R2 (64-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (55, 6, 'Windows 2000 Server SP4 (32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (56, 6, 'Windows Vista (32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (57, 6, 'Windows XP SP2 (32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (58, 6, 'Windows XP SP3 (32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (59, 10, 'Other Ubuntu (32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (60, 7, 'Other (32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (61, 6, 'Windows 2000 Server');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (62, 6, 'Windows 98');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (63, 6, 'Windows 95');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (64, 6, 'Windows NT 4');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (65, 6, 'Windows 3.1');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (66, 4, 'Red Hat Enterprise Linux 3(32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (67, 4, 'Red Hat Enterprise Linux 3(64-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (68, 7, 'Open Enterprise Server');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (69, 7, 'Asianux 3(32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (70, 7, 'Asianux 3(64-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (72, 2, 'Debian GNU/Linux 5(64-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (73, 2, 'Debian GNU/Linux 4(32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (74, 2, 'Debian GNU/Linux 4(64-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (75, 7, 'Other 2.6x Linux (32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (76, 7, 'Other 2.6x Linux (64-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (77, 8, 'Novell Netware 6.x');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (78, 8, 'Novell Netware 5.1');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (79, 9, 'Sun Solaris 10(32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (80, 9, 'Sun Solaris 10(64-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (81, 9, 'Sun Solaris 9(Experimental)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (82, 9, 'Sun Solaris 8(Experimental)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (83, 9, 'FreeBSD (32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (84, 9, 'FreeBSD (64-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (85, 9, 'SCO OpenServer 5');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (86, 9, 'SCO UnixWare 7');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (87, 6, 'Windows Server 2003 DataCenter Edition(32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (88, 6, 'Windows Server 2003 DataCenter Edition(64-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (89, 6, 'Windows Server 2003 Standard Edition(32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (90, 6, 'Windows Server 2003 Standard Edition(64-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (91, 6, 'Windows Server 2003 Web Edition');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (92, 6, 'Microsoft Small Bussiness Server 2003');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (93, 6, 'Windows XP (32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (94, 6, 'Windows XP (64-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (95, 6, 'Windows 2000 Advanced Server');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (96, 5, 'SUSE Linux Enterprise 8(32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (97, 5, 'SUSE Linux Enterprise 8(64-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (98, 7, 'Other Linux (32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (99, 7, 'Other Linux (64-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (100, 10, 'Other Ubuntu (64-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (101, 6, 'Windows Vista (64-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (102, 6, 'DOS');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (103, 7, 'Other (64-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (104, 7, 'OS/2');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (105, 6, 'Windows 2000 Professional');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (106, 4, 'Red Hat Enterprise Linux 4(64-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (107, 5, 'SUSE Linux Enterprise 9(32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (108, 5, 'SUSE Linux Enterprise 9(64-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (109, 5, 'SUSE Linux Enterprise 10(32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (110, 5, 'SUSE Linux Enterprise 10(64-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (111, 1, 'CentOS 5.5 (32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (112, 1, 'CentOS 5.5 (64-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (113, 4, 'Red Hat Enterprise Linux 5.5 (32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (114, 4, 'Red Hat Enterprise Linux 5.5 (64-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (115, 4, 'Fedora 13');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (116, 4, 'Fedora 12');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (117, 4, 'Fedora 11');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (118, 4, 'Fedora 10');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (119, 4, 'Fedora 9');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (120, 4, 'Fedora 8');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (121, 10, 'Ubuntu 10.04 (32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (122, 10, 'Ubuntu 9.10 (32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (123, 10, 'Ubuntu 9.04 (32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (124, 10, 'Ubuntu 8.10 (32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (125, 10, 'Ubuntu 8.04 (32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (126, 10, 'Ubuntu 10.04 (64-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (127, 10, 'Ubuntu 9.10 (64-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (128, 10, 'Ubuntu 9.04 (64-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (129, 10, 'Ubuntu 8.10 (64-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (130, 10, 'Ubuntu 8.04 (64-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (131, 4, 'Red Hat Enterprise Linux 2');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (132, 2, 'Debian GNU/Linux 6(32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (133, 2, 'Debian GNU/Linux 6(64-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (134, 3, 'Oracle Enterprise Linux 5.5 (32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (135, 3, 'Oracle Enterprise Linux 5.5 (64-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (136, 4, 'Red Hat Enterprise Linux 6.0 (32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (137, 4, 'Red Hat Enterprise Linux 6.0 (64-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (138, 7, 'None');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (139, 7, 'Other PV (32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (140, 7, 'Other PV (64-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (141, 1, 'CentOS 5.6 (32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (142, 1, 'CentOS 5.6 (64-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (143, 1, 'CentOS 6.0 (32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (144, 1, 'CentOS 6.0 (64-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (145, 3, 'Oracle Enterprise Linux 5.6 (32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (146, 3, 'Oracle Enterprise Linux 5.6 (64-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (147, 3, 'Oracle Enterprise Linux 6.0 (32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (148, 3, 'Oracle Enterprise Linux 6.0 (64-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (149, 4, 'Red Hat Enterprise Linux 5.6 (32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (150, 4, 'Red Hat Enterprise Linux 5.6 (64-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (151, 5, 'SUSE Linux Enterprise Server 10 SP3 (32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (152, 5, 'SUSE Linux Enterprise Server 10 SP4 (64-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (153, 5, 'SUSE Linux Enterprise Server 10 SP4 (32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (154, 5, 'SUSE Linux Enterprise Server 11 SP1 (64-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (155, 5, 'SUSE Linux Enterprise Server 11 SP1 (32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (156, 10, 'Ubuntu 10.10 (32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (157, 10, 'Ubuntu 10.10 (64-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (158, 9, 'Sun Solaris 11 (64-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (159, 9, 'Sun Solaris 11 (32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (160, 6, 'Windows PV');

INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (200, 1, 'Other CentOS (32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (201, 1, 'Other CentOS (64-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (202, 5, 'Other SUSE Linux(32-bit)');
INSERT INTO `cloud`.`guest_os` (id, category_id, display_name) VALUES (203, 5, 'Other SUSE Linux(64-bit)');

INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ("XenServer", 'CentOS 4.5 (32-bit)', 1);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ("XenServer", 'CentOS 4.6 (32-bit)', 2);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ("XenServer", 'CentOS 4.7 (32-bit)', 3);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ("XenServer", 'CentOS 4.8 (32-bit)', 4);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ("XenServer", 'CentOS 5.0 (32-bit)', 5);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ("XenServer", 'CentOS 5.0 (64-bit)', 6);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ("XenServer", 'CentOS 5.1 (32-bit)', 7);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ("XenServer", 'CentOS 5.1 (32-bit)', 8);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ("XenServer", 'CentOS 5.2 (32-bit)', 9);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ("XenServer", 'CentOS 5.2 (64-bit)', 10);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ("XenServer", 'CentOS 5.3 (32-bit)', 11);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ("XenServer", 'CentOS 5.3 (64-bit)', 12);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ("XenServer", 'CentOS 5.4 (32-bit)', 13);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ("XenServer", 'CentOS 5.4 (64-bit)', 14);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ("XenServer", 'Debian Lenny 5.0 (32-bit)', 15);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ("XenServer", 'Oracle Enterprise Linux 5.0 (32-bit)', 16);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ("XenServer", 'Oracle Enterprise Linux 5.0 (64-bit)', 17);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ("XenServer", 'Oracle Enterprise Linux 5.1 (32-bit)', 18);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ("XenServer", 'Oracle Enterprise Linux 5.1 (64-bit)', 19);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ("XenServer", 'Oracle Enterprise Linux 5.2 (32-bit)', 20);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ("XenServer", 'Oracle Enterprise Linux 5.2 (64-bit)', 21);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ("XenServer", 'Oracle Enterprise Linux 5.3 (32-bit)', 22);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ("XenServer", 'Oracle Enterprise Linux 5.3 (64-bit)', 23);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ("XenServer", 'Oracle Enterprise Linux 5.4 (32-bit)', 24);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ("XenServer", 'Oracle Enterprise Linux 5.4 (64-bit)', 25);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ("XenServer", 'Red Hat Enterprise Linux 4.5 (32-bit)', 26);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ("XenServer", 'Red Hat Enterprise Linux 4.6 (32-bit)', 27);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ("XenServer", 'Red Hat Enterprise Linux 4.7 (32-bit)', 28);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ("XenServer", 'Red Hat Enterprise Linux 4.8 (32-bit)', 29);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ("XenServer", 'Red Hat Enterprise Linux 5.0 (32-bit)', 30);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ("XenServer", 'Red Hat Enterprise Linux 5.0 (64-bit)', 31);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ("XenServer", 'Red Hat Enterprise Linux 5.1 (32-bit)', 32);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ("XenServer", 'Red Hat Enterprise Linux 5.1 (64-bit)', 33);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ("XenServer", 'Red Hat Enterprise Linux 5.2 (32-bit)', 34);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ("XenServer", 'Red Hat Enterprise Linux 5.2 (64-bit)', 35);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ("XenServer", 'Red Hat Enterprise Linux 5.3 (32-bit)', 36);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ("XenServer", 'Red Hat Enterprise Linux 5.3 (64-bit)', 37);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ("XenServer", 'Red Hat Enterprise Linux 5.4 (32-bit)', 38);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ("XenServer", 'Red Hat Enterprise Linux 5.4 (64-bit)', 39);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ("XenServer", 'SUSE Linux Enterprise Server 9 SP4 (32-bit)', 40);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ("XenServer", 'SUSE Linux Enterprise Server 10 SP1 (32-bit)', 41);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ("XenServer", 'SUSE Linux Enterprise Server 10 SP1 (64-bit)', 42);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ("XenServer", 'SUSE Linux Enterprise Server 10 SP2 (32-bit)', 43);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ("XenServer", 'SUSE Linux Enterprise Server 10 SP2 (64-bit)', 44);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ("XenServer", 'SUSE Linux Enterprise Server 10 SP3 (64-bit)', 45);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ("XenServer", 'SUSE Linux Enterprise Server 11 (32-bit)', 46);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ("XenServer", 'SUSE Linux Enterprise Server 11 (64-bit)', 47);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ("XenServer", 'Windows 7 (32-bit)', 48);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ("XenServer", 'Windows 7 (64-bit)', 49);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ("XenServer", 'Windows Server 2003 (32-bit)', 50);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ("XenServer", 'Windows Server 2003 (64-bit)', 51);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ("XenServer", 'Windows Server 2008 (32-bit)', 52);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ("XenServer", 'Windows Server 2008 (64-bit)', 53);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ("XenServer", 'Windows Server 2008 R2 (64-bit)', 54);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ("XenServer", 'Windows 2000 SP4 (32-bit)', 55);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ("XenServer", 'Windows Vista (32-bit)', 56);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ("XenServer", 'Windows XP SP2 (32-bit)', 57);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ("XenServer", 'Windows XP SP3 (32-bit)', 58);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ("XenServer", 'Other install media', 59);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ("XenServer", 'Other install media', 100);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ("XenServer", 'Other install media', 60);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ("XenServer", 'Other install media', 103);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ('XenServer', 'Other install media', 121);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ('XenServer', 'Other install media', 126);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ('XenServer', 'Other install media', 122);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ('XenServer', 'Other install media', 127);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ('XenServer', 'Other install media', 123);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ('XenServer', 'Other install media', 128);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ('XenServer', 'Other install media', 124);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ('XenServer', 'Other install media', 129);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ('XenServer', 'Other install media', 125);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ('XenServer', 'Other install media', 130);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ('XenServer', 'Other PV (32-bit)', 139);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES  ('XenServer', 'Other PV (64-bit)', 140);


INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Microsoft Windows 7(32-bit)', 48);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Microsoft Windows 7(64-bit)', 49);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Microsoft Windows Server 2008 R2(64-bit)', 54);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Microsoft Windows Server 2008(32-bit)', 52);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Microsoft Windows Server 2008(64-bit)', 53);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Microsoft Windows Server 2003, Enterprise Edition (32-bit)', 50);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Microsoft Windows Server 2003, Enterprise Edition (64-bit)', 51);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Microsoft Windows Server 2003, Datacenter Edition (32-bit)', 87);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Microsoft Windows Server 2003, Datacenter Edition (64-bit)', 88);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Microsoft Windows Server 2003, Standard Edition (32-bit)', 89);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Microsoft Windows Server 2003, Standard Edition (64-bit)', 90);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Microsoft Windows Server 2003, Web Edition', 91);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Microsoft Small Bussiness Server 2003', 92);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Microsoft Windows Vista (32-bit)', 56);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Microsoft Windows Vista (64-bit)', 101);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Microsoft Windows XP Professional (32-bit)', 93);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Microsoft Windows XP Professional (32-bit)', 57);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Microsoft Windows XP Professional (32-bit)', 58);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Microsoft Windows XP Professional (64-bit)', 94);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Microsoft Windows 2000 Advanced Server', 95);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Microsoft Windows 2000 Server', 61);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Microsoft Windows 2000 Professional', 105);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Microsoft Windows 2000 Server', 55);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Microsoft Windows 98', 62);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Microsoft Windows 95', 63);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Microsoft Windows NT 4', 64);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Microsoft Windows 3.1', 65);

INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Red Hat Enterprise Linux 5.0(32-bit)', 30);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Red Hat Enterprise Linux 5.1(32-bit)', 32);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Red Hat Enterprise Linux 5.2(32-bit)', 34);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Red Hat Enterprise Linux 5.3(32-bit)', 36);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Red Hat Enterprise Linux 5.4(32-bit)', 38);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Red Hat Enterprise Linux 5.0(64-bit)', 31);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Red Hat Enterprise Linux 5.1(64-bit)', 33);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Red Hat Enterprise Linux 5.2(64-bit)', 35);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Red Hat Enterprise Linux 5.3(64-bit)', 37);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Red Hat Enterprise Linux 5.4(64-bit)', 39);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Red Hat Enterprise Linux 4.5(32-bit)', 26);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Red Hat Enterprise Linux 4.6(32-bit)', 27);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Red Hat Enterprise Linux 4.7(32-bit)', 28);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Red Hat Enterprise Linux 4.8(32-bit)', 29);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Red Hat Enterprise Linux 4(64-bit)', 106);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Red Hat Enterprise Linux 3(32-bit)', 66);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Red Hat Enterprise Linux 3(64-bit)', 67);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Red Hat Enterprise Linux 2', 131);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Red Hat Enterprise Linux 6(32-bit)', 204);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Red Hat Enterprise Linux 6(64-bit)', 205);


INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Suse Linux Enterprise 11(32-bit)', 46);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Suse Linux Enterprise 11(64-bit)', 47);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Suse Linux Enterprise 10(32-bit)', 41);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Suse Linux Enterprise 10(32-bit)', 43);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Suse Linux Enterprise 10(64-bit)', 42);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Suse Linux Enterprise 10(64-bit)', 44);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Suse Linux Enterprise 10(64-bit)', 45);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Suse Linux Enterprise 10(32-bit)', 109);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Suse Linux Enterprise 10(64-bit)', 110);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Suse Linux Enterprise 8/9(32-bit)', 40);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Suse Linux Enterprise 8/9(32-bit)', 96);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Suse Linux Enterprise 8/9(64-bit)', 97);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Suse Linux Enterprise 8/9(32-bit)', 107);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Suse Linux Enterprise 8/9(64-bit)', 108);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Other Suse Linux Enterprise(32-bit)', 202);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Other Suse Linux Enterprise(64-bit)', 203);

INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Open Enterprise Server', 68);

INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Asianux 3(32-bit)', 69);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Asianux 3(64-bit)', 70);

INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Debian GNU/Linux 5(32-bit)', 15);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Debian GNU/Linux 5(64-bit)', 72);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Debian GNU/Linux 4(32-bit)', 73);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Debian GNU/Linux 4(64-bit)', 74);

INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Ubuntu 10.04 (32-bit)', 121);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Ubuntu 9.10 (32-bit)', 122);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Ubuntu 9.04 (32-bit)', 123);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Ubuntu 8.10 (32-bit)', 124);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Ubuntu 8.04 (32-bit)', 125);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Ubuntu 10.04 (64-bit)', 126);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Ubuntu 9.10 (64-bit)', 127);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Ubuntu 9.04 (64-bit)', 128);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Ubuntu 8.10 (64-bit)', 129);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Ubuntu 8.04 (64-bit)', 130);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Ubuntu 10.10 (32-bit)', 59);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Ubuntu 10.10 (64-bit)', 100);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Other Ubuntu Linux (32-bit)', 59);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Other Ubuntu (64-bit)', 100);

INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Other 2.6x Linux (32-bit)', 75);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Other 2.6x Linux (64-bit)', 76);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Other Linux (32-bit)', 98);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Other Linux (64-bit)', 99);

INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Novell Netware 6.x', 77);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Novell Netware 5.1', 78);

INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Sun Solaris 10(32-bit)', 79);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Sun Solaris 10(64-bit)', 80);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Sun Solaris 9(Experimental)', 81);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Sun Solaris 8(Experimental)', 82);

INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'FreeBSD (32-bit)', 83);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'FreeBSD (64-bit)', 84);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'OS/2', 104);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'SCO OpenServer 5', 85);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'SCO UnixWare 7', 86);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'DOS', 102);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Other (32-bit)', 60);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'Other (64-bit)', 103);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'CentOS (32-bit)', 200);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ("VmWare", 'CentOS (64-bit)', 201);


INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'CentOS 4.5', 1);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'CentOS 4.6', 2);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'CentOS 4.7', 3);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'CentOS 4.8', 4);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'CentOS 5.0', 5);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'CentOS 5.0', 6);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'CentOS 5.1', 7);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'CentOS 5.1', 8);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'CentOS 5.2', 9);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'CentOS 5.2', 10);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'CentOS 5.3', 11);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'CentOS 5.3', 12);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'CentOS 5.4', 13);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'CentOS 5.4', 14);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'CentOS 5.5', 111);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'CentOS 5.5', 112);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'Red Hat Enterprise Linux 4.5', 26);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'Red Hat Enterprise Linux 4.6', 27);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'Red Hat Enterprise Linux 4.7', 28);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'Red Hat Enterprise Linux 4.8', 29);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'Red Hat Enterprise Linux 5.0', 30);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'Red Hat Enterprise Linux 5.0', 31);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'Red Hat Enterprise Linux 5.1', 32);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'Red Hat Enterprise Linux 5.1', 33);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'Red Hat Enterprise Linux 5.2', 34);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'Red Hat Enterprise Linux 5.2', 35);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'Red Hat Enterprise Linux 5.3', 36);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'Red Hat Enterprise Linux 5.3', 37);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'Red Hat Enterprise Linux 5.4', 38);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'Red Hat Enterprise Linux 5.4', 39);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'Red Hat Enterprise Linux 5.5', 113);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'Red Hat Enterprise Linux 5.5', 114);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'Red Hat Enterprise Linux 4', 106);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'Red Hat Enterprise Linux 3', 66);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'Red Hat Enterprise Linux 3', 67);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'Red Hat Enterprise Linux 2', 131);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'Fedora 13', 115);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'Fedora 12', 116);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'Fedora 11', 117);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'Fedora 10', 118);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'Fedora 9', 119);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'Fedora 8', 120);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'Ubuntu 10.04', 121);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'Ubuntu 10.04', 126);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'Ubuntu 9.10', 122);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'Ubuntu 9.10', 127);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'Ubuntu 9.04', 123);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'Ubuntu 9.04', 128);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'Ubuntu 8.10', 124);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'Ubuntu 8.10', 129);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'Ubuntu 8.04', 125);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'Ubuntu 8.04', 130);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'Debian GNU/Linux 5', 15);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'Debian GNU/Linux 5', 72);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'Debian GNU/Linux 4', 73);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'Debian GNU/Linux 4', 74);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'Other Linux 2.6x', 75);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'Other Linux 2.6x', 76);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'Other Ubuntu', 59);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'Other Ubuntu', 100);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'Other Linux', 98);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'Other Linux', 99);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'Windows 7', 48);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'Windows 7', 49);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'Windows Server 2003', 50);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'Windows Server 2003', 51);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'Windows Server 2003', 87);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'Windows Server 2003', 88);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'Windows Server 2003', 89);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'Windows Server 2003', 90);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'Windows Server 2003', 91);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'Windows Server 2003', 92);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'Windows Server 2008', 52);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'Windows Server 2008', 53);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'Windows 2000', 55);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'Windows 2000', 61);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'Windows 2000', 95);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'Windows 98', 62);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'Windows Vista', 56);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'Windows Vista', 101);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'Windows XP SP2', 57);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'Windows XP SP3', 58);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'Windows XP ', 93);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'Windows XP ', 94);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'DOS', 102);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'Other', 60);
INSERT INTO `cloud`.`guest_os_hypervisor` (hypervisor_type, guest_os_name, guest_os_id) VALUES ('KVM', 'Other', 103);

