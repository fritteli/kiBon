/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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

import {ChangeDetectionStrategy, Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {ControlContainer, NgForm} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {StateDeclaration, Transition} from '@uirouter/core';
import {Moment} from 'moment';
import {Observable} from 'rxjs';
import {getTSEinschulungTypGemeindeValues, TSEinschulungTyp} from '../../../models/enums/TSEinschulungTyp';
import {TSEinstellungKey} from '../../../models/enums/TSEinstellungKey';
import {TSGemeindeStatus} from '../../../models/enums/TSGemeindeStatus';
import {TSGesuchsperiodeStatus} from '../../../models/enums/TSGesuchsperiodeStatus';
import {TSBenutzer} from '../../../models/TSBenutzer';
import {TSGemeindeKonfiguration} from '../../../models/TSGemeindeKonfiguration';
import {TSGemeindeStammdaten} from '../../../models/TSGemeindeStammdaten';
import {LogFactory} from '../../core/logging/LogFactory';

const LOG = LogFactory.createLog('EditGemeindeComponentBG');

@Component({
    selector: 'dv-edit-gemeinde-bg',
    templateUrl: './edit-gemeinde-bg.component.html',
    styleUrls: ['./edit-gemeinde-bg.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    viewProviders: [{provide: ControlContainer, useExisting: NgForm}],
})
export class EditGemeindeComponentBG implements OnInit {
    @Input() public stammdaten$: Observable<TSGemeindeStammdaten>;
    @Input() public beguStartStr: string;
    @Input() private readonly gemeindeId: string;
    @Input() public editMode: boolean;
    @Input() public altBGAdresse: boolean;
    @Input() public beguStartDatum: Moment;

    @Output() public readonly altBGAdresseChange: EventEmitter<boolean> = new EventEmitter();

    public konfigurationsListe: TSGemeindeKonfiguration[];
    public gemeindeStatus: TSGemeindeStatus;
    public einschulungTypGemeindeValues: Array<TSEinschulungTyp>;
    private navigationDest: StateDeclaration;

    public constructor(
        private readonly $transition$: Transition,
        private readonly translate: TranslateService,
    ) {

    }

    public ngOnInit(): void {
        if (!this.gemeindeId) {
            return;
        }
        this.stammdaten$.subscribe(stammdaten => {
                this.konfigurationsListe = stammdaten.konfigurationsListe;
                this.gemeindeStatus = stammdaten.gemeinde.status;
                this.initProperties();
                // todo: remove
                console.log(stammdaten);
            },
            err => LOG.error(err));

        this.navigationDest = this.$transition$.to();
        this.einschulungTypGemeindeValues = getTSEinschulungTypGemeindeValues();
    }

    public compareBenutzer(b1: TSBenutzer, b2: TSBenutzer): boolean {
        return b1 && b2 ? b1.username === b2.username : b1 === b2;
    }

    public altBGAdresseHasChange(newVal: boolean): void {
        this.altBGAdresseChange.emit(newVal);
    }

    public getKonfigKontingentierungString(): string {
        return this.translate.instant('KONTINGENTIERUNG');
    }

    public changeKonfigKontingentierung(gk: TSGemeindeKonfiguration): void {
        gk.konfigurationen
            .filter(property => TSEinstellungKey.GEMEINDE_KONTINGENTIERUNG_ENABLED === property.key)
            .forEach(property => {
                property.value = gk.konfigKontingentierung ? 'true' : 'false';
            });
    }

    public getKonfigBeguBisUndMitSchulstufeString(gk: TSGemeindeKonfiguration): string {
        const bgBisStr = this.translate.instant(gk.konfigBeguBisUndMitSchulstufe.toString());
        return bgBisStr;
    }

    public changeKonfigBeguBisUndMitSchulstufe(gk: TSGemeindeKonfiguration): void {
        gk.konfigurationen
            .filter(property => TSEinstellungKey.GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE === property.key)
            .forEach(property => {
                property.value = gk.konfigBeguBisUndMitSchulstufe;
            });
    }

    public changeKonfigErwerbspensumZuschlagOverriden(gk: TSGemeindeKonfiguration): void {
        // if the flag is unchecked, we need to restore the original value
        if (!gk.erwerbspensumZuschlagOverriden) {
            this.resetErwerbspensumZuschlag(gk);
        }
    }

    public changeErwerbspensumZuschlag(gk: TSGemeindeKonfiguration): void {
        gk.konfigurationen
            .filter(property => TSEinstellungKey.ERWERBSPENSUM_ZUSCHLAG === property.key)
            .forEach(property => {
                property.value = String(gk.erwerbspensumZuschlag);
            });
    }

    public changeKonfigZusaetzlicherGutscheinEnabled(gk: TSGemeindeKonfiguration): void {
        gk.konfigurationen
            .filter(property => TSEinstellungKey.GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_ENABLED === property.key)
            .forEach(property => {
                property.value = gk.konfigZusaetzlicherGutscheinEnabled;
            });
    }

    public changeKonfigZusaetzlicherGutscheinBetragKita(gk: TSGemeindeKonfiguration): void {
        gk.konfigurationen
            .filter(property => TSEinstellungKey.GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_KITA === property.key)
            .forEach(property => {
                property.value = String(gk.konfigZusaetzlicherGutscheinBetragKita);
            });
    }

    public changeKonfigZusaetzlicherGutscheinBetragTfo(gk: TSGemeindeKonfiguration): void {
        gk.konfigurationen
            .filter(property => TSEinstellungKey.GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_TFO === property.key)
            .forEach(property => {
                property.value = String(gk.konfigZusaetzlicherGutscheinBetragTfo);
            });
    }

    public changeKonfigZusaetzlicherGutscheinBisUndMitSchulstufeKita(gk: TSGemeindeKonfiguration): void {
        gk.konfigurationen
            .filter(property =>
                TSEinstellungKey.GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_KITA === property.key)
            .forEach(property => {
                property.value = String(gk.konfigZusaetzlicherGutscheinBisUndMitSchulstufeKita);
            });
    }

    public changeKonfigZusaetzlicherGutscheinBisUndMitSchulstufeTfo(gk: TSGemeindeKonfiguration): void {
        gk.konfigurationen
            .filter(property =>
                TSEinstellungKey.GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_TFO === property.key)
            .forEach(property => {
                property.value = String(gk.konfigZusaetzlicherGutscheinBisUndMitSchulstufeTfo);
            });
    }

    public changeKonfigZusaetzlicherBabybeitragEnabled(gk: TSGemeindeKonfiguration): void {
        gk.konfigurationen
            .filter(property =>
                TSEinstellungKey.GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_ENABLED === property.key)
            .forEach(property => {
                property.value = String(gk.konfigZusaetzlicherBabybeitragEnabled);
            });
    }

    public changeZusaetzlicherBabybeitragKita(gk: TSGemeindeKonfiguration): void {
        gk.konfigurationen
            .filter(property =>
                TSEinstellungKey.GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_KITA === property.key)
            .forEach(property => {
                property.value = String(gk.konfigZusaetzlicherBabybeitragBetragKita);
            });
    }

    public changeZusaetzlicherBabybeitragTfo(gk: TSGemeindeKonfiguration): void {
        gk.konfigurationen
            .filter(property =>
                TSEinstellungKey.GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_TFO === property.key)
            .forEach(property => {
                property.value = String(gk.konfigZusaetzlicherBabybeitragBetragTfo);
            });
    }

    public changeKonfigZusaetzlicherAnspruchFreiwilligenarbeitEnabled(gk: TSGemeindeKonfiguration): void {
        gk.konfigurationen
            .filter(property =>
                TSEinstellungKey.GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_ENABLED === property.key)
            .forEach(property => {
                property.value = String(gk.konfigZusaetzlicherAnspruchFreiwilligenarbeitEnabled);
            });
    }

    public changeKonfigZusaetzlicherAnspruchFreiwilligenarbeitMax(gk: TSGemeindeKonfiguration): void {
        gk.konfigurationen
            .filter(property =>
                TSEinstellungKey.GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_MAXPROZENT === property.key)
            .forEach(property => {
                property.value = String(gk.konfigZusaetzlicherAnspruchFreiwilligenarbeitMaxprozent);
            });
    }

    public resetErwerbspensumZuschlag(gk: TSGemeindeKonfiguration): void {
        gk.erwerbspensumZuschlag = gk.erwerbspensumZuschlagMax;
        this.changeErwerbspensumZuschlag(gk);
    }

    public isKonfigurationEditable(gk: TSGemeindeKonfiguration): boolean {
        return 'gemeinde.edit' === this.navigationDest.name
            && this.editMode
            && (TSGemeindeStatus.EINGELADEN === this.gemeindeStatus
                || (gk.gesuchsperiode && gk.gesuchsperiode.status &&
                    TSGesuchsperiodeStatus.GESCHLOSSEN !== gk.gesuchsperiode.status));
    }

    private initProperties(): void {
        this.konfigurationsListe.forEach(config => {
            config.initProperties();
        });
    }
}
