/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

-- GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_AKTIVIERT
INSERT INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						 einstellung_key, value, gemeinde_id, gesuchsperiode_id)
SELECT *
FROM (SELECT UNHEX(REPLACE(UUID() COLLATE utf8_unicode_ci, '-', '')) AS id,
		  '2022-09-09 12:12:12' AS timestamp_erstellt,
		  '2022-09-09 12:12:12' AS timestamp_mutiert,
		  'flyway' AS user_erstellt,
		  'flyway' AS user_mutiert,
		  0 AS version,
		  'GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_AKTIVIERT' AS einstellung_key,
		  'false' AS VALUE,
		NULL    AS gemeinde_id,
		gp.id   AS gesuchsperiode_id
	  FROM gesuchsperiode AS gp) AS tmp;

-- GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_BETRAG_KITA
INSERT INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						 einstellung_key, value, gemeinde_id, gesuchsperiode_id)
SELECT *
FROM (SELECT UNHEX(REPLACE(UUID() COLLATE utf8_unicode_ci, '-', '')) AS id,
		  '2022-09-09 12:12:12' AS timestamp_erstellt,
		  '2022-09-09 12:12:12' AS timestamp_mutiert,
		  'flyway' AS user_erstellt,
		  'flyway' AS user_mutiert,
		  0 AS version,
		  'GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_BETRAG_KITA' AS einstellung_key,
		  '0' AS VALUE,
		NULL    AS gemeinde_id,
		gp.id   AS gesuchsperiode_id
	  FROM gesuchsperiode AS gp) AS tmp;

-- GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_BETRAG_KITA_AB_PRIMARSCHULE
INSERT INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						 einstellung_key, value, gemeinde_id, gesuchsperiode_id)
SELECT *
FROM (SELECT UNHEX(REPLACE(UUID() COLLATE utf8_unicode_ci, '-', '')) AS id,
		  '2022-09-09 12:12:12' AS timestamp_erstellt,
		  '2022-09-09 12:12:12' AS timestamp_mutiert,
		  'flyway' AS user_erstellt,
		  'flyway' AS user_mutiert,
		  0 AS version,
		  'GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_BETRAG_KITA_AB_PRIMARSCHULE' AS einstellung_key,
		  '0' AS VALUE,
		NULL    AS gemeinde_id,
		gp.id   AS gesuchsperiode_id
	  FROM gesuchsperiode AS gp) AS tmp;


-- GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_BETRAG_TFO
INSERT INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						 einstellung_key, value, gemeinde_id, gesuchsperiode_id)
SELECT *
FROM (SELECT UNHEX(REPLACE(UUID() COLLATE utf8_unicode_ci, '-', '')) AS id,
		  '2022-09-09 12:12:12' AS timestamp_erstellt,
		  '2022-09-09 12:12:12' AS timestamp_mutiert,
		  'flyway' AS user_erstellt,
		  'flyway' AS user_mutiert,
		  0 AS version,
		  'GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_BETRAG_TFO' AS einstellung_key,
		  '0' AS VALUE,
		NULL    AS gemeinde_id,
		gp.id   AS gesuchsperiode_id
	  FROM gesuchsperiode AS gp) AS tmp;

-- GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_BETRAG_TFO_AB_PRIMARSCHULE
INSERT INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						 einstellung_key, value, gemeinde_id, gesuchsperiode_id)
SELECT *
FROM (SELECT UNHEX(REPLACE(UUID() COLLATE utf8_unicode_ci, '-', '')) AS id,
		  '2022-09-09 12:12:12' AS timestamp_erstellt,
		  '2022-09-09 12:12:12' AS timestamp_mutiert,
		  'flyway' AS user_erstellt,
		  'flyway' AS user_mutiert,
		  0 AS version,
		  'GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_BETRAG_TFO_AB_PRIMARSCHULE' AS einstellung_key,
		  '0' AS VALUE,
		NULL    AS gemeinde_id,
		gp.id   AS gesuchsperiode_id
	  FROM gesuchsperiode AS gp) AS tmp;

-- GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_MASSGEBENDEN_EINKOMMEN
INSERT INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						 einstellung_key, value, gemeinde_id, gesuchsperiode_id)
SELECT *
FROM (SELECT UNHEX(REPLACE(UUID() COLLATE utf8_unicode_ci, '-', '')) AS id,
		  '2022-09-09 12:12:12' AS timestamp_erstellt,
		  '2022-09-09 12:12:12' AS timestamp_mutiert,
		  'flyway' AS user_erstellt,
		  'flyway' AS user_mutiert,
		  0 AS version,
		  'GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_MASSGEBENDEN_EINKOMMEN' AS einstellung_key,
		  '0' AS VALUE,
		NULL    AS gemeinde_id,
		gp.id   AS gesuchsperiode_id
	  FROM gesuchsperiode AS gp) AS tmp;