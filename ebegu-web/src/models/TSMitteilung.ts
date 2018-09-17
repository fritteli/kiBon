/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
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

import * as moment from 'moment';
import {TSAmt} from './enums/TSAmt';
import {TSMitteilungStatus} from './enums/TSMitteilungStatus';
import {TSMitteilungTeilnehmerTyp} from './enums/TSMitteilungTeilnehmerTyp';
import {TSAbstractMutableEntity} from './TSAbstractMutableEntity';
import TSBetreuung from './TSBetreuung';
import TSDossier from './TSDossier';
import TSBenutzer from './TSBenutzer';

export default class TSMitteilung extends TSAbstractMutableEntity {

    private _dossier: TSDossier;
    private _betreuung: TSBetreuung;
    private _senderTyp: TSMitteilungTeilnehmerTyp;
    private _empfaengerTyp: TSMitteilungTeilnehmerTyp;
    private _sender: TSBenutzer;
    private _empfaenger: TSBenutzer;
    private _subject: string;
    private _message: string;
    private _mitteilungStatus: TSMitteilungStatus;
    private _sentDatum: moment.Moment;

    constructor(dossier?: TSDossier, betreuung?: TSBetreuung, senderTyp?: TSMitteilungTeilnehmerTyp, empfaengerTyp?: TSMitteilungTeilnehmerTyp, sender?: TSBenutzer,
                empfaenger?: TSBenutzer, subject?: string, message?: string, mitteilungStatus?: TSMitteilungStatus,
                sentDatum?: moment.Moment) {
        super();
        this._dossier = dossier;
        this._betreuung = betreuung;
        this._senderTyp = senderTyp;
        this._empfaengerTyp = empfaengerTyp;
        this._sender = sender;
        this._empfaenger = empfaenger;
        this._subject = subject;
        this._message = message;
        this._mitteilungStatus = mitteilungStatus;
        this._sentDatum = sentDatum;
    }

    get dossier(): TSDossier {
        return this._dossier;
    }

    set dossier(value: TSDossier) {
        this._dossier = value;
    }

    get betreuung(): TSBetreuung {
        return this._betreuung;
    }

    set betreuung(value: TSBetreuung) {
        this._betreuung = value;
    }

    get senderTyp(): TSMitteilungTeilnehmerTyp {
        return this._senderTyp;
    }

    set senderTyp(value: TSMitteilungTeilnehmerTyp) {
        this._senderTyp = value;
    }

    get empfaengerTyp(): TSMitteilungTeilnehmerTyp {
        return this._empfaengerTyp;
    }

    set empfaengerTyp(value: TSMitteilungTeilnehmerTyp) {
        this._empfaengerTyp = value;
    }

    get sender(): TSBenutzer {
        return this._sender;
    }

    set sender(value: TSBenutzer) {
        this._sender = value;
    }

    get empfaenger(): TSBenutzer {
        return this._empfaenger;
    }

    set empfaenger(value: TSBenutzer) {
        this._empfaenger = value;
    }

    get subject(): string {
        return this._subject;
    }

    set subject(value: string) {
        this._subject = value;
    }

    get message(): string {
        return this._message;
    }

    set message(value: string) {
        this._message = value;
    }

    get mitteilungStatus(): TSMitteilungStatus {
        return this._mitteilungStatus;
    }

    set mitteilungStatus(value: TSMitteilungStatus) {
        this._mitteilungStatus = value;
    }

    get sentDatum(): moment.Moment {
        return this._sentDatum;
    }

    set sentDatum(value: moment.Moment) {
        this._sentDatum = value;
    }

    get verantwortlicher(): string {
        if (this.dossier.getHauptverantwortlicher()) {
            return this.dossier.getHauptverantwortlicher().getFullName();
        }
        return '';
    }

    get senderAsString(): string {
        let senderAsString: string;
        if (this.sender.currentBerechtigung.institution) {
            senderAsString = this.sender.currentBerechtigung.institution.name + ', ';
        } else if (this.sender.currentBerechtigung.traegerschaft) {
            senderAsString = this.sender.currentBerechtigung.traegerschaft.name + ', ';
        }
        if (senderAsString) {
            return senderAsString + this.sender.getFullName();
        } else {
            return this.sender.getFullName();
        }
    }

    public isErledigt(): boolean {
        return this.mitteilungStatus === TSMitteilungStatus.ERLEDIGT;
    }

    public getEmpfaengerAmt(): TSAmt {
        return this.empfaenger.amt;
    }

    public getSenderAmt(): TSAmt {
        return this.sender.amt;
    }
}
