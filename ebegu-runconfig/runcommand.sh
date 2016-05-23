#!/bin/bash
#host-ip auslesen
alias hostip="ip route show 0.0.0.0/0 | grep -Eo 'via \S+' | awk '{ print \$2 }'"

#Datenbank initialisieren damit die daten ueber neustart gesichert werden sollte das -v volume von :var/lib/mysql  angepasst werden eg '-v  /my/own/datadir:/var/lib/mysql'
docker run -d --name EBEGU-MARIADB  -e MYSQL_DATABASE=ebegu  -e MYSQL_USER=ebegu -e MYSQL_PASSWORD=ebegu -e MYSQL_ROOT_PASSWORD=ebegu   mariadb:latest --character-set-server=utf8 --collation-server=utf8_unicode_ci --verbose || docker start EBEGU-MARIADB
#Auf Datenbank warten
docker run --rm --link EBEGU-MARIADB:mysql digit/wait-for-mysql
#Wildfly Server starten
docker run -dP --name EBEGU-WILDFLY -p 8080:8080 -p 8443:8443 -p 9990:9990  --link EBEGU-MARIADB:EBEGU-MARIADB docker.dvbern.ch:5000/stadt-bern/ebegu-wildfly
#Applikation starten
docker run -dP --name EBEGU-NGNIX --link EBEGU-WILDFLY:EBEGU-WILDFLY -p 80:80   docker.dvbern.ch:5000/stadt-bern/ebegu-ngnix