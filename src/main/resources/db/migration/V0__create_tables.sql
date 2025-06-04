-- Create tables for the application

-- HQ.companies definition
CREATE TABLE `companies` (
                             `id` varchar(36) NOT NULL,
                             `name` varchar(255) NOT NULL,
                             `display_name` varchar(255) DEFAULT NULL,
                             `timestamp` datetime NOT NULL,
                             `logo_url` longtext DEFAULT NULL,
                             PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='List of all companies where farms exist';

-- HQ.farms definition
CREATE TABLE `farms` (
                         `id` varchar(36) NOT NULL,
                         `name` varchar(255) NOT NULL,
                         `display_name` varchar(255) DEFAULT NULL,
                         `timestamp` datetime NOT NULL,
                         `farm_type` longtext DEFAULT NULL,
                         `temp_source_id` varchar(36) DEFAULT NULL,
                         `is_temp_source` tinyint(1) NOT NULL DEFAULT 0,
                         PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='List of all farms where meters can exist at';

-- HQ.roles definition
CREATE TABLE `roles` (
                         `id` varchar(36) NOT NULL,
                         `name` varchar(255) NOT NULL,
                         `timestamp` datetime NOT NULL,
                         PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='List of all roles for users to be associated with';

-- HQ.users definition
CREATE TABLE `users` (
                         `id` varchar(36) NOT NULL,
                         `username` varchar(255) NOT NULL,
                         `first_name` varchar(255) DEFAULT NULL,
                         `last_name` varchar(255) DEFAULT NULL,
                         `password` varchar(255) NOT NULL,
                         `timestamp` datetime NOT NULL,
                         PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='List of all users and passwords';

-- HQ.user_contact_types definition
CREATE TABLE `user_contact_types` (
                                      `id` varchar(36) NOT NULL,
                                      `name` varchar(255) DEFAULT NULL,
                                      `timestamp` datetime NOT NULL,
                                      PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='List of all contact types';

-- HQ.company_farms definition
CREATE TABLE `company_farms` (
                                 `id` int(11) NOT NULL AUTO_INCREMENT,
                                 `company_id` varchar(36) NOT NULL,
                                 `farm_id` varchar(36) NOT NULL,
                                 `timestamp` datetime NOT NULL,
                                 PRIMARY KEY (`id`),
                                 KEY `company_id` (`company_id`) USING BTREE,
                                 KEY `farm_id` (`farm_id`) USING BTREE,
                                 CONSTRAINT `company_farms_company_id` FOREIGN KEY (`company_id`) REFERENCES `companies` (`id`),
                                 CONSTRAINT `company_farms_farm_id` FOREIGN KEY (`farm_id`) REFERENCES `farms` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Linking farms to companies, allowing multiple companies to be tied to one farm and vice versa';

-- HQ.company_users definition
CREATE TABLE `company_users` (
                                 `id` int(11) NOT NULL AUTO_INCREMENT,
                                 `company_id` varchar(36) NOT NULL,
                                 `user_id` varchar(36) NOT NULL,
                                 `timestamp` datetime NOT NULL,
                                 PRIMARY KEY (`id`),
                                 KEY `company_id` (`company_id`) USING BTREE,
                                 KEY `user_id` (`user_id`) USING BTREE,
                                 CONSTRAINT `company_users_company_id` FOREIGN KEY (`company_id`) REFERENCES `companies` (`id`),
                                 CONSTRAINT `company_users_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Linking users to companies, allowing multiple users to be tied to one company and vice versa';

-- HQ.user_contact_info definition
CREATE TABLE `user_contact_info` (
                                     `id` int(11) NOT NULL AUTO_INCREMENT,
                                     `user_id` varchar(36) NOT NULL,
                                     `type_id` varchar(36) NOT NULL,
                                     `value` varchar(255) NOT NULL,
                                     `timestamp` datetime NOT NULL,
                                     PRIMARY KEY (`id`),
                                     KEY `user_id` (`user_id`) USING BTREE,
                                     KEY `type_id` (`type_id`) USING BTREE,
                                     CONSTRAINT `user_contact_info_type_id` FOREIGN KEY (`type_id`) REFERENCES `user_contact_types` (`id`),
                                     CONSTRAINT `user_contact_info_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Linking users to contact types, allowing multiple users to be tied to one contact type and vice versa';

-- HQ.user_roles definition
CREATE TABLE `user_roles` (
                              `id` int(11) NOT NULL AUTO_INCREMENT,
                              `user_id` varchar(36) NOT NULL,
                              `role_id` varchar(36) NOT NULL,
                              `timestamp` datetime NOT NULL,
                              PRIMARY KEY (`id`),
                              KEY `user_id` (`user_id`) USING BTREE,
                              KEY `role_id` (`role_id`) USING BTREE,
                              CONSTRAINT `user_roles_role_id` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`),
                              CONSTRAINT `user_roles_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Linking users to roles, allowing multiple users to be tied to one role and vice versa';