/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

import {ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnInit} from '@angular/core';
import {FormBuilder, FormGroup} from '@angular/forms';
import {combineLatest} from 'rxjs';
import {startWith} from 'rxjs/operators';
import {TSLastenausgleichTagesschuleAngabenGemeindeContainer} from '../../../../models/gemeindeantrag/TSLastenausgleichTagesschuleAngabenGemeindeContainer';
import {GemeindeAntragService} from '../../services/gemeinde-antrag.service';

@Component({
    selector: 'dv-gemeinde-angaben',
    templateUrl: './gemeinde-angaben.component.html',
    styleUrls: ['./gemeinde-angaben.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GemeindeAngabenComponent implements OnInit {

    @Input() public lastenausgleichID: string;

    public formGroup: FormGroup;

    public constructor(
        private readonly fb: FormBuilder,
        private readonly gemeindeAntraegeService: GemeindeAntragService,
        private readonly cd: ChangeDetectorRef
        ,
    ) {
    }

    public ngOnInit(): void {
        this.gemeindeAntraegeService.getGemeindeAngabenFor(this.lastenausgleichID)
            .subscribe((gemeindeAngabenContainer: TSLastenausgleichTagesschuleAngabenGemeindeContainer) => {
                const gemeindeAngaben = gemeindeAngabenContainer.angabenDeklaration;
                this.formGroup = this.fb.group({
                    // A
                    alleFaelleInKibon: [''],
                    angebotVerfuegbarFuerAlleSchulstufen: [gemeindeAngaben.angebotVerfuegbarFuerAlleSchulstufen],
                    begruendungWennAngebotNichtVerfuegbarFuerAlleSchulstufen:
                        [gemeindeAngaben.begruendungWennAngebotNichtVerfuegbarFuerAlleSchulstufen],
                    bedarfBeiElternAbgeklaert: [gemeindeAngaben.bedarfBeiElternAbgeklaert],
                    angebotFuerFerienbetreuungVorhanden: [gemeindeAngaben.angebotFuerFerienbetreuungVorhanden],
                    // B
                    geleisteteBetreuungsstundenOhneBesondereBeduerfnisse:
                        [gemeindeAngaben.geleisteteBetreuungsstundenOhneBesondereBeduerfnisse],
                    geleisteteBetreuungsstundenBesondereBeduerfnisse:
                        [gemeindeAngaben.geleisteteBetreuungsstundenBesondereBeduerfnisse],
                    davonStundenZuNormlohnMehrAls50ProzentAusgebildete:
                        [gemeindeAngaben.davonStundenZuNormlohnMehrAls50ProzentAusgebildete],
                    davonStundenZuNormlohnWenigerAls50ProzentAusgebildete:
                        [gemeindeAngaben.davonStundenZuNormlohnWenigerAls50ProzentAusgebildete],
                    einnahmenElterngebuehren: [gemeindeAngaben.einnahmenElterngebuehren],
                    // C
                    gesamtKostenTagesschule: [gemeindeAngaben.gesamtKostenTagesschule],
                    einnnahmenVerpflegung: [gemeindeAngaben.einnnahmenVerpflegung],
                    einnahmenSubventionenDritter: [gemeindeAngaben.einnahmenSubventionenDritter],
                    // D
                    bemerkungenWeitereKostenUndErtraege: [gemeindeAngaben.bemerkungenWeitereKostenUndErtraege],
                    // E
                    betreuungsstundenDokumentiertUndUeberprueft:
                        [gemeindeAngaben.betreuungsstundenDokumentiertUndUeberprueft],
                    elterngebuehrenGemaessVerordnungBerechnet:
                        [gemeindeAngaben.elterngebuehrenGemaessVerordnungBerechnet],
                    einkommenElternBelegt: [gemeindeAngaben.einkommenElternBelegt],
                    maximalTarif: [gemeindeAngaben.maximalTarif],
                    mindestens50ProzentBetreuungszeitDurchAusgebildetesPersonal:
                        [gemeindeAngaben.mindestens50ProzentBetreuungszeitDurchAusgebildetesPersonal],
                    ausbildungenMitarbeitendeBelegt: [gemeindeAngaben.ausbildungenMitarbeitendeBelegt],
                    // Bemerkungen
                    bemerkungen: [gemeindeAngaben.bemerkungen],
                    // calculated values
                    lastenausgleichberechtigteBetreuungsstunden: [{value: '', disabled: true}],
                    davonStundenZuNormlohnWenigerAls50ProzentAusgebildeteBerechnet: [{value: '', disabled: true}],
                    davonStundenZuNormlohnMehrAls50ProzentAusgebildeteBerechnet: [{value: '', disabled: true}],
                });

                combineLatest(
                    [
                        this.formGroup.get('geleisteteBetreuungsstundenOhneBesondereBeduerfnisse').valueChanges.pipe(
                            startWith(gemeindeAngaben.geleisteteBetreuungsstundenOhneBesondereBeduerfnisse),
                        ),
                        this.formGroup.get('geleisteteBetreuungsstundenBesondereBeduerfnisse').valueChanges.pipe(
                            startWith(gemeindeAngaben.geleisteteBetreuungsstundenBesondereBeduerfnisse),
                        ),
                    ],
                ).subscribe(formValues => {
                    this.formGroup.get('lastenausgleichberechtigteBetreuungsstunden')
                        .setValue(parseFloat(formValues[0] || 0) + parseFloat(formValues[1] || 0));
                });

                this.formGroup.get('davonStundenZuNormlohnWenigerAls50ProzentAusgebildete')
                    .valueChanges
                    .subscribe(value => {
                        // TODO: replace with config param
                        this.formGroup.get('davonStundenZuNormlohnWenigerAls50ProzentAusgebildeteBerechnet')
                            .setValue(value ? value * 5.25 : 0);
                    });

                this.formGroup.get('davonStundenZuNormlohnMehrAls50ProzentAusgebildete')
                    .valueChanges
                    .subscribe(value => {
                        // TODO: replace with config param
                        this.formGroup.get('davonStundenZuNormlohnMehrAls50ProzentAusgebildeteBerechnet')
                            .setValue(value ? value * 10.39 : 0);
                    });

                this.cd.markForCheck();
            });
    }

}
