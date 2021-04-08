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

import {ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {MatDialog} from '@angular/material/dialog';
import {TranslateService} from '@ngx-translate/core';
import {UIRouterGlobals} from '@uirouter/core';
import {combineLatest} from 'rxjs';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {TSFerienbetreuungAngabenContainer} from '../../../../models/gemeindeantrag/TSFerienbetreuungAngabenContainer';
import {TSFerienbetreuungAngabenKostenEinnahmen} from '../../../../models/gemeindeantrag/TSFerienbetreuungAngabenKostenEinnahmen';
import {ErrorService} from '../../../core/errors/service/ErrorService';
import {LogFactory} from '../../../core/logging/LogFactory';
import {WizardStepXRS} from '../../../core/service/wizardStepXRS.rest';
import {numberValidator, ValidationType} from '../../../shared/validators/number-validator.directive';
import {AbstractFerienbetreuungFormular} from '../abstract.ferienbetreuung-formular';
import {FerienbetreuungService} from '../services/ferienbetreuung.service';

const LOG = LogFactory.createLog('FerienbetreuungKostenEinnahmenComponent');

@Component({
    selector: 'dv-ferienbetreuung-kosten-einnahmen',
    templateUrl: './ferienbetreuung-kosten-einnahmen.component.html',
    styleUrls: ['./ferienbetreuung-kosten-einnahmen.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FerienbetreuungKostenEinnahmenComponent extends AbstractFerienbetreuungFormular implements OnInit {

    public form: FormGroup;
    public container: TSFerienbetreuungAngabenContainer;
    private kostenEinnahmen: TSFerienbetreuungAngabenKostenEinnahmen;

    public constructor(
        protected readonly cd: ChangeDetectorRef,
        protected readonly errorService: ErrorService,
        protected readonly translate: TranslateService,
        protected readonly dialog: MatDialog,
        protected readonly wizardRS: WizardStepXRS,
        protected readonly uiRouterGlobals: UIRouterGlobals,
        private readonly ferienbetreuungService: FerienbetreuungService,
        private readonly fb: FormBuilder,
        private readonly authService: AuthServiceRS,
    ) {
        super(errorService, translate, dialog, cd, wizardRS, uiRouterGlobals);
    }

    public ngOnInit(): void {
        combineLatest([
            this.ferienbetreuungService.getFerienbetreuungContainer(),
            this.authService.principal$,
        ]).subscribe(([container, principal]) => {
            this.container = container;
            this.kostenEinnahmen = container.angabenDeklaration?.kostenEinnahmen;

            this.setupFormAndPermissions(this.kostenEinnahmen, principal);
        }, error => {
            LOG.error(error);
        });
    }

    protected setupForm(kostenEinnahmen: TSFerienbetreuungAngabenKostenEinnahmen): void {
        if (!kostenEinnahmen) {
            return;
        }
        this.form = this.fb.group({
            id: [
                kostenEinnahmen.id,
            ],
            personalkosten: [
                kostenEinnahmen.personalkosten,
                numberValidator(ValidationType.INTEGER),
            ],
            personalkostenLeitungAdmin: [
                kostenEinnahmen.personalkostenLeitungAdmin,
                numberValidator(ValidationType.INTEGER),
            ],
            sachkosten: [
                kostenEinnahmen.sachkosten,
                numberValidator(ValidationType.INTEGER),
            ],
            verpflegungskosten: [
                kostenEinnahmen.verpflegungskosten,
                numberValidator(ValidationType.INTEGER),
            ],
            weitereKosten: [
                kostenEinnahmen.weitereKosten,
                numberValidator(ValidationType.INTEGER),
            ],
            bemerkungenKosten: [
                kostenEinnahmen.bemerkungenKosten,
                numberValidator(ValidationType.INTEGER),
            ],
            elterngebuehren: [
                kostenEinnahmen.elterngebuehren,
                numberValidator(ValidationType.INTEGER),
            ],
            weitereEinnahmen: [
                kostenEinnahmen.weitereEinnahmen,
                numberValidator(ValidationType.INTEGER),
            ],
        });
    }

    protected enableFormValidation(): void {
        this.form.get('personalkosten').setValidators([Validators.required, numberValidator(ValidationType.INTEGER)]);
        this.form.get('personalkostenLeitungAdmin').setValidators([numberValidator(ValidationType.INTEGER)]);
        this.form.get('sachkosten').setValidators([Validators.required, numberValidator(ValidationType.INTEGER)]);
        this.form.get('verpflegungskosten')
            .setValidators([Validators.required, numberValidator(ValidationType.INTEGER)]);
        this.form.get('weitereKosten').setValidators([numberValidator(ValidationType.INTEGER)]);
        this.form.get('elterngebuehren').setValidators([Validators.required, numberValidator(ValidationType.INTEGER)]);
        this.form.get('weitereEinnahmen').setValidators([Validators.required, numberValidator(ValidationType.INTEGER)]);
    }

    public save(): void {
        if (!this.form.valid) {
            this.showValidierungFehlgeschlagenErrorMessage();
            return;
        }
        this.ferienbetreuungService.saveKostenEinnahmen(this.container.id, this.extractFormValues())
            .subscribe(() => {
                this.ferienbetreuungService.updateFerienbetreuungContainerStore(this.container.id);
                this.errorService.addMesageAsInfo(this.translate.instant('SPEICHERN_ERFOLGREICH'));
            }, err => {
                LOG.error(err);
                this.errorService.addMesageAsError(this.translate.instant('FERIENBETREUUNG_PERSIST_ERROR'));
            });
    }

    public async onAbschliessen(): Promise<void> {
        if (await this.checkReadyForAbschliessen()) {
            this.ferienbetreuungService.kostenEinnahmenAbschliessen(this.container.id, this.form.value)
                .subscribe(() => this.handleSaveSuccess(), error => this.handleSaveError(error));
        }
    }

    private extractFormValues(): TSFerienbetreuungAngabenKostenEinnahmen {
        this.kostenEinnahmen.personalkosten = this.form.get('personalkosten').value;
        this.kostenEinnahmen.personalkostenLeitungAdmin = this.form.get('personalkostenLeitungAdmin').value;
        this.kostenEinnahmen.sachkosten = this.form.get('sachkosten').value;
        this.kostenEinnahmen.verpflegungskosten = this.form.get('verpflegungskosten').value;
        this.kostenEinnahmen.weitereKosten = this.form.get('weitereKosten').value;
        this.kostenEinnahmen.bemerkungenKosten = this.form.get('bemerkungenKosten').value;
        this.kostenEinnahmen.elterngebuehren = this.form.get('elterngebuehren').value;
        this.kostenEinnahmen.weitereEinnahmen = this.form.get('weitereEinnahmen').value;
        return this.kostenEinnahmen;
    }

    public onFalscheAngaben(): void {
        this.ferienbetreuungService.falscheAngabenKostenEinnahmen(this.container.id, this.kostenEinnahmen)
            .subscribe(() => this.handleSaveSuccess(), error => this.handleSaveError(error));
    }
}
