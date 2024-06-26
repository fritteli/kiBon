/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
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

UPDATE einstellung
SET einstellung.einstellung_key = 'EINGEWOEHNUNG_TYP'
WHERE einstellung.einstellung_key = 'FKJV_EINGEWOEHNUNG';

UPDATE einstellung
SET einstellung.value = 'KEINE'
WHERE einstellung.value = 'false' AND einstellung.einstellung_key = 'EINGEWOEHNUNG_TYP';

UPDATE einstellung
	INNER JOIN ebegu.gesuchsperiode ON einstellung.gesuchsperiode_id = gesuchsperiode.id
	INNER JOIN mandant ON gesuchsperiode.mandant_id = mandant.id
SET einstellung.value = 'FKJV'
WHERE einstellung.value = 'true' AND mandant_identifier = 'BERN' AND einstellung.einstellung_key = 'EINGEWOEHNUNG_TYP';

UPDATE einstellung
	INNER JOIN ebegu.gesuchsperiode ON einstellung.gesuchsperiode_id = gesuchsperiode.id
	INNER JOIN mandant ON gesuchsperiode.mandant_id = mandant.id
SET einstellung.value = 'LUZERN'
WHERE einstellung.value = 'true' AND mandant_identifier = 'LUZERN' AND
	  einstellung.einstellung_key = 'EINGEWOEHNUNG_TYP';
