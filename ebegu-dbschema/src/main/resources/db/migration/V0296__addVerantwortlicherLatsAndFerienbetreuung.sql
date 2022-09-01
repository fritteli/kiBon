/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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


ALTER TABLE lastenausgleich_tagesschule_angaben_gemeinde_container ADD COLUMN IF NOT EXISTS
	verantwortlicher_id BINARY(16);

ALTER TABLE lastenausgleich_tagesschule_angaben_gemeinde_container_aud  ADD COLUMN IF NOT EXISTS
	verantwortlicher_id BINARY(16);

ALTER TABLE lastenausgleich_tagesschule_angaben_gemeinde_container
ADD CONSTRAINT FK_lats_angaben_gemeinde_container_verantwortlicher_id
	FOREIGN KEY (verantwortlicher_id)
		REFERENCES benutzer(id);

ALTER TABLE ferienbetreuung_angaben_container ADD COLUMN IF NOT EXISTS
	verantwortlicher_id BINARY(16);
ALTER TABLE ferienbetreuung_angaben_container_aud ADD COLUMN IF NOT EXISTS
	verantwortlicher_id BINARY(16);

ALTER TABLE ferienbetreuung_angaben_container
ADD CONSTRAINT FK_ferienbetreuung_container_verantwortlicher_id
	FOREIGN KEY (verantwortlicher_id)
		REFERENCES benutzer(id);