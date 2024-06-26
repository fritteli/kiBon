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

alter table gesuch add column interne_pendenz BIT NOT NULL;
alter table gesuch_aud add column interne_pendenz BIT;
update gesuch set interne_pendenz = false;
update gesuch_aud set interne_pendenz = false;
update interne_pendenz inner join gesuch g on interne_pendenz.gesuch_id = g.id set g.interne_pendenz = true where g.interne_pendenz = false AND erledigt = false;
update interne_pendenz inner join gesuch_aud g on interne_pendenz.gesuch_id = g.id set g.interne_pendenz = true where g.interne_pendenz = false AND erledigt = false;
