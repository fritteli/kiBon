/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

import TSAbstractEntity from './TSAbstractEntity';
import TSAdresse from './TSAdresse';
import TSBenutzer from './TSBenutzer';
import TSGemeinde from './TSGemeinde';
import TSGemeindeKonfiguration from './TSGemeindeKonfiguration';
import TSGesuchsperiode from './TSGesuchsperiode';
import TSTextRessource from './TSTextRessource';

export default class TSGemeindeStammdaten extends TSAbstractEntity {
    public administratoren: string; // read only
    public sachbearbeiter: string; // read only
    public defaultBenutzerBG: TSBenutzer;
    public defaultBenutzerTS: TSBenutzer;
    public defaultBenutzer: TSBenutzer;
    public gemeinde: TSGemeinde;
    public adresse: TSAdresse;
    public bgAdresse: TSAdresse;
    public tsAdresse: TSAdresse;
    public beschwerdeAdresse: TSAdresse;
    public mail: string;
    public telefon: string;
    public webseite: string;
    public korrespondenzspracheDe: boolean;
    public korrespondenzspracheFr: boolean;
    public benutzerListeBG: TSBenutzer[]; // read only
    public benutzerListeTS: TSBenutzer[]; // read only
    public kontoinhaber: string;
    public bic: string;
    public iban: string;
    public standardRechtsmittelbelehrung: boolean;
    public rechtsmittelbelehrung: TSTextRessource;
    // ---------- Konfiguration ----------
    public konfigurationsListe: TSGemeindeKonfiguration[];

    public getGemeindeKonfigurationForGesuchsperiode(gesuchsperiode: TSGesuchsperiode): TSGemeindeKonfiguration {
        for (const konfigurationsListeElement of this.konfigurationsListe) {
            if (konfigurationsListeElement.gesuchsperiode.id === gesuchsperiode.id) {
                return konfigurationsListeElement;
            }
        }
        return undefined;
    }
}
