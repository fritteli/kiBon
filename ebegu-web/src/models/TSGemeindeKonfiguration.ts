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

import * as moment from 'moment';
import {CONSTANTS} from '../app/core/constants/CONSTANTS';
import {EbeguUtil} from '../utils/EbeguUtil';
import {TSEinschulungTyp} from './enums/TSEinschulungTyp';
import {TSEinstellungKey} from './enums/TSEinstellungKey';
import {TSEinstellung} from './TSEinstellung';
import {TSGesuchsperiode} from './TSGesuchsperiode';

export class TSGemeindeKonfiguration {
    public gesuchsperiodeName: string;
    public gesuchsperiodeStatusName: string;
    public gesuchsperiode: TSGesuchsperiode;
    public konfigKontingentierung: boolean; // only on client
    public konfigBeguBisUndMitSchulstufe: TSEinschulungTyp; // only on client
    public konfigTagesschuleAktivierungsdatum: moment.Moment;
    public konfigTagesschuleErsterSchultag: moment.Moment;
    public konfigZusaetzlicherGutscheinEnabled: boolean; // only on client
    public konfigZusaetzlicherGutscheinBetragKita: number; // only on client
    public konfigZusaetzlicherGutscheinBetragTfo: number; // only on client
    public konfigZusaetzlicherGutscheinBisUndMitSchulstufeKita: TSEinschulungTyp; // only on client
    public konfigZusaetzlicherGutscheinBisUndMitSchulstufeTfo: TSEinschulungTyp; // only on client
    public konfigZusaetzlicherBabybeitragEnabled: boolean; // only on client
    public konfigZusaetzlicherBabybeitragBetragKita: number; // only on client
    public konfigZusaetzlicherBabybeitragBetragTfo: number; // only on client
    public konfigZusaetzlicherAnspruchFreiwilligenarbeitEnabled: boolean; // only on client
    public konfigZusaetzlicherAnspruchFreiwilligenarbeitMaxprozent: number; // only on client
    public erwerbspensumZuschlag: number;
    // never override this property. we just load it for validation reasons
    public erwerbspensumZuschlagMax: number;
    public erwerbspensumZuschlagOverriden: boolean;
    public editMode: boolean; // only on client
    public konfigurationen: TSEinstellung[];

    /**
     * Wir muessen TS Anmeldungen nehmen ab das TagesschuleAktivierungsdatum
     * Es kann also sein das Kinder sich nach den ersten Schultag anmelden
     */
    public isTagesschulenAnmeldungKonfiguriert(): boolean {
        return this.hasTagesschulenAnmeldung()
            && (this.konfigTagesschuleAktivierungsdatum.isBefore(moment([]))
                || this.konfigTagesschuleAktivierungsdatum.isSame(moment([])));
    }

    public isTageschulenAnmeldungAktiv(): boolean {
        return this.isTagesschulenAnmeldungKonfiguriert()
            && this.konfigTagesschuleAktivierungsdatum.isBefore(moment());
    }

    public hasTagesschulenAnmeldung(): boolean {
        return EbeguUtil.isNotNullOrUndefined(this.konfigTagesschuleAktivierungsdatum);
    }

    public initProperties(): void {
        this.konfigBeguBisUndMitSchulstufe = TSEinschulungTyp.KINDERGARTEN2;
        this.konfigKontingentierung = false;
        this.konfigTagesschuleAktivierungsdatum = this.gesuchsperiode.gueltigkeit.gueltigAb;
        this.konfigTagesschuleErsterSchultag = this.gesuchsperiode.gueltigkeit.gueltigAb;

        this.konfigurationen.forEach(property => {
            switch (property.key) {
                case TSEinstellungKey.GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE: {
                    this.konfigBeguBisUndMitSchulstufe = (TSEinschulungTyp as any)[property.value];
                    break;
                }
                case TSEinstellungKey.GEMEINDE_KONTINGENTIERUNG_ENABLED: {
                    this.konfigKontingentierung = (property.value === 'true');
                    break;
                }
                case TSEinstellungKey.GEMEINDE_TAGESSCHULE_ANMELDUNGEN_DATUM_AB: {
                    this.konfigTagesschuleAktivierungsdatum = moment(property.value, CONSTANTS.DATE_FORMAT);
                    break;
                }
                case TSEinstellungKey.GEMEINDE_TAGESSCHULE_ERSTER_SCHULTAG: {
                    this.konfigTagesschuleErsterSchultag = moment(property.value, CONSTANTS.DATE_FORMAT);
                    break;
                }
                case TSEinstellungKey.ERWERBSPENSUM_ZUSCHLAG: {
                    this.erwerbspensumZuschlag = Number(property.value);
                    break;
                }
                case TSEinstellungKey.GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_ENABLED: {
                    this.konfigZusaetzlicherGutscheinEnabled = (property.value === 'true');
                    break;
                }
                case TSEinstellungKey.GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_KITA: {
                    this.konfigZusaetzlicherGutscheinBetragKita = Number(property.value);
                    break;
                }
                case TSEinstellungKey.GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_TFO: {
                    this.konfigZusaetzlicherGutscheinBetragTfo = Number(property.value);
                    break;
                }
                case TSEinstellungKey.GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_KITA: {
                    this.konfigZusaetzlicherGutscheinBisUndMitSchulstufeKita = (TSEinschulungTyp as any)[property.value];
                    break;
                }
                case TSEinstellungKey.GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_TFO: {
                    this.konfigZusaetzlicherGutscheinBisUndMitSchulstufeTfo = (TSEinschulungTyp as any)[property.value];
                    break;
                }
                case TSEinstellungKey.GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_ENABLED: {
                    this.konfigZusaetzlicherAnspruchFreiwilligenarbeitEnabled = (property.value === 'true');
                    break;
                }
                case TSEinstellungKey.GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_ENABLED: {
                    this.konfigZusaetzlicherBabybeitragEnabled = (property.value === 'true');
                    break;
                }
                case TSEinstellungKey.GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_KITA: {
                    this.konfigZusaetzlicherBabybeitragBetragKita = Number(property.value);
                    break;
                }
                case TSEinstellungKey.GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_TFO: {
                    this.konfigZusaetzlicherBabybeitragBetragTfo = Number(property.value);
                    break;
                }
                case TSEinstellungKey.GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_MAXPROZENT: {
                    this.konfigZusaetzlicherAnspruchFreiwilligenarbeitMaxprozent = Number(property.value);
                    break;
                }
                default: {
                    break;
                }
            }
        });

        this.erwerbspensumZuschlagOverriden = this.erwerbspensumZuschlag !== this.erwerbspensumZuschlagMax;
    }
}
