/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2018 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

import {TSGemeindeStatus} from './enums/TSGemeindeStatus';
import {TSGesuchBetreuungenStatus} from './enums/TSGesuchBetreuungenStatus';
import {TSAbstractMutableEntity} from './TSAbstractMutableEntity';

export default class TSGemeinde extends TSAbstractMutableEntity {

    private _name: string;
    private _gemeindeNummer: number;
    private _status: TSGemeindeStatus;

    public get name(): string {
        return this._name;
    }

    public set name(value: string) {
        this._name = value;
    }

    public get gemeindeNummer(): number {
        return this._gemeindeNummer;
    }

    public set gemeindeNummer(value: number) {
        this._gemeindeNummer = value;
    }

    get status(): TSGemeindeStatus {
        return this._status;
    }

    set status(value: TSGemeindeStatus) {
        this._status = value;
    }

}
