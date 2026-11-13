CREATE DATABASE IF NOT EXISTS maternal_child_saas DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE USER IF NOT EXISTS 'mc_app'@'localhost' IDENTIFIED BY 'McApp_2026Prod';
ALTER USER 'mc_app'@'localhost' IDENTIFIED BY 'McApp_2026Prod';
GRANT ALL PRIVILEGES ON maternal_child_saas.* TO 'mc_app'@'localhost';
FLUSH PRIVILEGES;
