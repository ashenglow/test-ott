# boot17-crud-template
## 데이터베이스 설정
```sql
mysql -u root -p
```
```sql
CREATE DATABASE dev_db;
CREATE USER 'dev_user'@'localhost' IDENTIFIED BY 'dev_pass';
GRANT ALL PRIVILEGES ON dev_db.* TO 'dev_user'@'localhost';
FLUSH PRIVILEGES;
```
