mysql -u root -p tags -e "Drop table TT; Drop table Track; Drop table Tag; Drop table Artist"
mysql -u root -p tags < db_schema
mysql -u root -p tags < raw_tags.sql
