-- create table idam_role_management_config
CREATE TABLE idam_role_management_config(
   role_name text,
   user_type text,
   allow_delete_flag text NOT NULL,
   PRIMARY KEY (role_name, user_type)
);