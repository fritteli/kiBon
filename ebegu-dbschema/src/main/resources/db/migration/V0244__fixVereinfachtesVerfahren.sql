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

ALTER TABLE finanzielle_situation DROP COLUMN einkommen_in_vereinfachtem_verfahren_abgerechnet;
ALTER TABLE finanzielle_situation ADD COLUMN einkommen_in_vereinfachtem_verfahren_abgerechnet BIT;

ALTER TABLE finanzielle_situation_aud DROP COLUMN einkommen_in_vereinfachtem_verfahren_abgerechnet;
ALTER TABLE finanzielle_situation_aud ADD COLUMN einkommen_in_vereinfachtem_verfahren_abgerechnet BIT;

ALTER TABLE einkommensverschlechterung DROP COLUMN einkommen_in_vereinfachtem_verfahren_abgerechnet;
ALTER TABLE einkommensverschlechterung ADD COLUMN einkommen_in_vereinfachtem_verfahren_abgerechnet BIT;

ALTER TABLE einkommensverschlechterung_aud DROP COLUMN einkommen_in_vereinfachtem_verfahren_abgerechnet;
ALTER TABLE einkommensverschlechterung_aud ADD COLUMN einkommen_in_vereinfachtem_verfahren_abgerechnet BIT;
