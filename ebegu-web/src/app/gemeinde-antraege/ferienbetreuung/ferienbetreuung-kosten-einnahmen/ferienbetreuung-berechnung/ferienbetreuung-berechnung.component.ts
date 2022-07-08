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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

import {
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    Input,
    OnDestroy,
    OnInit,
    SimpleChanges,
} from '@angular/core';
import {FormGroup} from '@angular/forms';
import {combineLatest, Subscription} from 'rxjs';
import {mergeMap, startWith, tap} from 'rxjs/operators';
import {EinstellungRS} from '../../../../../admin/service/einstellungRS.rest';
import {TSFerienbetreuungAngaben} from '../../../../../models/gemeindeantrag/TSFerienbetreuungAngaben';
import {TSFerienbetreuungAngabenContainer} from '../../../../../models/gemeindeantrag/TSFerienbetreuungAngabenContainer';
import {LogFactory} from '../../../../core/logging/LogFactory';
import {FerienbetreuungService} from '../../services/ferienbetreuung.service';
import {TSFerienbetreuungBerechnung} from '../TSFerienbetreuungBerechnung';

const LOG = LogFactory.createLog('FerienbetreuungBerechnungComponent');

@Component({
    selector: 'dv-ferienbetreuung-berechnung',
    templateUrl: './ferienbetreuung-berechnung.component.html',
    styleUrls: ['./ferienbetreuung-berechnung.component.less'],
    changeDetection: ChangeDetectionStrategy.Default,
})
export class FerienbetreuungBerechnungComponent implements OnInit, OnDestroy {

    @Input()
    private readonly form: FormGroup;

    @Input()
    private container: TSFerienbetreuungAngabenContainer;

    private subscription: Subscription;

    public berechnung: TSFerienbetreuungBerechnung;

    public constructor(
        private readonly ferienbetreuungService: FerienbetreuungService,
        private readonly einstellungRS: EinstellungRS,
        private readonly cd: ChangeDetectorRef,
    ) {
    }

    public ngOnInit(): void {
        this.subscription = this.ferienbetreuungService.getFerienbetreuungContainer()
            .pipe(
                tap(container => {
                    this.container = container;
                }),
                mergeMap(() => this.einstellungRS.getPauschalbetraegeFerienbetreuung(this.container))
            ).subscribe(([pauschale, pauschaleSonderschueler]) => {
                this.berechnung = new TSFerienbetreuungBerechnung(pauschale, pauschaleSonderschueler);
                this.setUpValuesFromContainer();
                this.setUpValuesFromForm();
            }, error => {
                LOG.error(error);
            });
    }

    public ngOnChanges(changes: SimpleChanges): void {
        if (changes.form && changes.form.currentValue && !changes.form.firstChange) {
            this.setUpValuesFromForm();
        }
    }

    public ngOnDestroy(): void {
        this.subscription.unsubscribe();
    }

    private setUpValuesFromForm(): void {
        if (!this.form) {
            return;
        }
        const angaben = this.getAngabenForStatus();
        combineLatest([
            this.form.get('personalkosten').valueChanges.pipe(
                startWith(angaben?.kostenEinnahmen.personalkosten),
            ),
            this.form.get('sachkosten').valueChanges.pipe(
                startWith(angaben?.kostenEinnahmen.sachkosten),
            ),
            this.form.get('verpflegungskosten').valueChanges.pipe(
                startWith(angaben?.kostenEinnahmen.verpflegungskosten),
            ),
            this.form.get('weitereKosten').valueChanges.pipe(
                startWith(angaben?.kostenEinnahmen.weitereKosten),
            ),
            this.form.get('elterngebuehren').valueChanges.pipe(
                startWith(angaben?.kostenEinnahmen.elterngebuehren),
            ),
            this.form.get('weitereEinnahmen').valueChanges.pipe(
                startWith(angaben?.kostenEinnahmen.weitereEinnahmen),
            ),
            this.form.get('sockelbeitrag').valueChanges.pipe(
                startWith(angaben?.kostenEinnahmen.sockelbeitrag),
            ),
            this.form.get('beitraegeNachAnmeldungen').valueChanges.pipe(
                startWith(angaben?.kostenEinnahmen.beitraegeNachAnmeldungen),
            ),
            this.form.get('vorfinanzierteKantonsbeitraege').valueChanges.pipe(
                startWith(angaben?.kostenEinnahmen.vorfinanzierteKantonsbeitraege),
            ),
        ]).subscribe(formValues => {
            this.berechnung.personalkosten = formValues[0];
            this.berechnung.sachkosten = formValues[1];
            this.berechnung.verpflegungskosten = formValues[2];
            this.berechnung.weitereKosten = formValues[3];
            this.berechnung.einnahmenElterngebuehren = formValues[4];
            this.berechnung.weitereEinnahmen = formValues[5];
            this.berechnung.sockelbeitrag = formValues[6];
            this.berechnung.beitraegeNachAnmeldungen = formValues[7];
            this.berechnung.vorfinanzierteKantonsbeitraege = formValues[8];

            this.calculate();
        }, err => {
            LOG.error(err);
        });
    }

    private getAngabenForStatus(): TSFerienbetreuungAngaben {
        return this.container?.isAtLeastInPruefungKanton() ?
            this.container?.angabenKorrektur :
            this.container?.angabenDeklaration;
    }

    private setUpValuesFromContainer(): void {
        const angaben = this.getAngabenForStatus();
        this.berechnung.anzahlBetreuungstageKinderBern = angaben?.nutzung?.anzahlBetreuungstageKinderBern;
        this.berechnung.betreuungstageKinderDieserGemeinde = angaben?.nutzung?.betreuungstageKinderDieserGemeinde;
        this.berechnung.betreuungstageKinderDieserGemeindeSonderschueler =
            angaben?.nutzung?.betreuungstageKinderDieserGemeindeSonderschueler;
        this.berechnung.betreuungstageKinderAndererGemeinde =
            angaben?.nutzung?.davonBetreuungstageKinderAndererGemeinden;
        this.berechnung.betreuungstageKinderAndererGemeindenSonderschueler =
            angaben?.nutzung?.davonBetreuungstageKinderAndererGemeindenSonderschueler;
        this.berechnung.isDeleagtionsmodell = angaben?.isDelegationsmodell();

        this.calculate();
    }

    private calculate(): void {
        this.berechnung.calculate();
        this.cd.markForCheck();
    }
}
