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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import {Component, ChangeDetectionStrategy, ViewChild} from '@angular/core';
import {NgForm} from '@angular/forms';
import {Transition} from '@uirouter/core';
import {IPromise} from 'angular';
import {TSWizardStepName} from '../../../../../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../../../../../models/enums/TSWizardStepStatus';
import {TSEinkommensverschlechterungContainer} from '../../../../../models/TSEinkommensverschlechterungContainer';
import {TSFinanzModel} from '../../../../../models/TSFinanzModel';
import {EbeguUtil} from '../../../../../utils/EbeguUtil';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';
import {WizardStepManager} from '../../../../service/wizardStepManager';
import {AbstractGesuchViewX} from '../../../abstractGesuchViewX';
import {FinanzielleSituationLuzernService} from '../../../finanzielleSituation/luzern/finanzielle-situation-luzern.service';

@Component({
    selector: 'dv-einkommensverschlechterung-luzern-view',
    templateUrl: './einkommensverschlechterung-luzern-view.component.html',
    styleUrls: ['./einkommensverschlechterung-luzern-view.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EinkommensverschlechterungLuzernViewComponent extends AbstractGesuchViewX<TSFinanzModel> {

    @ViewChild(NgForm) private readonly form: NgForm;

    public constructor(
        protected gesuchModelManager: GesuchModelManager,
        protected wizardStepManager: WizardStepManager,
        protected finSitLuService: FinanzielleSituationLuzernService,
        private readonly $transition$: Transition,
    ) {
        super(gesuchModelManager, wizardStepManager, TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG_LUZERN);
        const parsedGesuchstelllerNum = parseInt(this.$transition$.params().gesuchstellerNumber, 10);
        const parsedBasisJahrPlusNum = parseInt(this.$transition$.params().basisjahrPlus, 10);
        this.gesuchModelManager.setGesuchstellerNumber(parsedGesuchstelllerNum);
        this.gesuchModelManager.setBasisJahrPlusNumber(parsedBasisJahrPlusNum);

        this.model = new TSFinanzModel(this.gesuchModelManager.getBasisjahr(),
            this.gesuchModelManager.isGesuchsteller2Required(),
            parsedGesuchstelllerNum);
        this.model.copyEkvDataFromGesuch(this.gesuchModelManager.getGesuch());
        this.model.copyFinSitDataFromGesuch(this.gesuchModelManager.getGesuch());
        this.model.initEinkommensverschlechterungContainer(parsedBasisJahrPlusNum, parsedGesuchstelllerNum);

        this.wizardStepManager.updateCurrentWizardStepStatusSafe(
            TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG_LUZERN,
            TSWizardStepStatus.IN_BEARBEITUNG);
    }

    public save(): IPromise<TSEinkommensverschlechterungContainer> {
        if (!this.isGesuchValid()) {
            return undefined;
        }

        if (!this.form.dirty) {
            // If there are no changes in form we don't need anything to update on Server and we could return the
            // promise immediately
            return Promise.resolve(this.model.getEkvContToWorkWith());
        }
        this.model.copyEkvSitDataToGesuch(this.gesuchModelManager.getGesuch());
        return this.gesuchModelManager.saveEinkommensverschlechterungContainer();
    }

    private isGesuchValid(): boolean {
        if (!this.form.valid) {
            for (const control in this.form.controls) {
                if (EbeguUtil.isNotNullOrUndefined(this.form.controls[control])) {
                    this.form.controls[control].markAsTouched({onlySelf: true});
                }
            }
            EbeguUtil.selectFirstInvalid();
        }

        return this.form.valid;
    }
}
