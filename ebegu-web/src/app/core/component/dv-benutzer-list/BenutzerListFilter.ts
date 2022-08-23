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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
export interface BenutzerListFilter {
    username?: string;
    vorname?: string;
    name?: string;
    email?: string;
    rolle?: string;
    roleGueltigAb?: string;
    roleGueltigBis?: string;
    gemeinde?: string;
    institution?: string;
    traegerschaft?: string;
    sozialdienst?: string;
    status?: string;
}
