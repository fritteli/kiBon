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

import {IComponentOptions} from 'angular';
import TSPendenzInstitution from '../../../models/TSPendenzInstitution';
import TSGesuchsperiode from '../../../models/TSGesuchsperiode';
import EbeguUtil from '../../../utils/EbeguUtil';
import {TSBetreuungsangebotTyp} from '../../../models/enums/TSBetreuungsangebotTyp';
import TSInstitution from '../../../models/TSInstitution';
import {InstitutionRS} from '../../../core/service/institutionRS.rest';
import GesuchsperiodeRS from '../../../core/service/gesuchsperiodeRS.rest';
import GesuchModelManager from '../../../gesuch/service/gesuchModelManager';
import {IStateService} from 'angular-ui-router';
import BerechnungsManager from '../../../gesuch/service/berechnungsManager';
import PendenzInstitutionRS from '../../service/PendenzInstitutionRS.rest';
import {InstitutionStammdatenRS} from '../../../core/service/institutionStammdatenRS.rest';
import TSBetreuungsnummerParts from '../../../models/dto/TSBetreuungsnummerParts';
import ITimeoutService = angular.ITimeoutService;
let template = require('./pendenzenInstitutionListView.html');
require('./pendenzenInstitutionListView.less');

export class PendenzenInstitutionListViewComponentConfig implements IComponentOptions {
    transclude = false;
    template = template;
    controller = PendenzenInstitutionListViewController;
    controllerAs = 'vm';
}

export class PendenzenInstitutionListViewController {

    private pendenzenList: Array<TSPendenzInstitution>;
    selectedBetreuungsangebotTyp: string;
    selectedInstitution: string;
    selectedGesuchsperiode: string;
    institutionenList: Array<TSInstitution>;
    betreuungsangebotTypList: Array<TSBetreuungsangebotTyp>;
    activeGesuchsperiodenList: Array<string> = [];
    itemsByPage: number = 20;
    numberOfPages: number = 1;


    static $inject: string[] = ['PendenzInstitutionRS', 'EbeguUtil', 'InstitutionRS', 'InstitutionStammdatenRS', 'GesuchsperiodeRS',
        'GesuchModelManager', 'BerechnungsManager', '$state'];

    constructor(public pendenzInstitutionRS: PendenzInstitutionRS, private ebeguUtil: EbeguUtil, private institutionRS: InstitutionRS,
                private institutionStammdatenRS: InstitutionStammdatenRS, private gesuchsperiodeRS: GesuchsperiodeRS,
                private gesuchModelManager: GesuchModelManager, private berechnungsManager: BerechnungsManager,
                private $state: IStateService) {
    }

    $onInit() {
        this.updatePendenzenList();
        this.updateInstitutionenList();
        this.updateBetreuungsangebotTypList();
        this.updateActiveGesuchsperiodenList();
    }

    private getTotalResultCount(): number {
        if (this.pendenzenList && this.pendenzenList.length) {
            return this.pendenzenList.length;
        }
        return 0;
    }

    private updatePendenzenList() {
        this.pendenzInstitutionRS.getPendenzenList().then((response: any) => {
            this.pendenzenList = angular.copy(response);
            this.numberOfPages = this.pendenzenList.length / this.itemsByPage;
        });
    }

    public updateActiveGesuchsperiodenList(): void {
        this.gesuchsperiodeRS.getAllNichtAbgeschlosseneGesuchsperioden().then((response: TSGesuchsperiode[]) => {
            this.extractGesuchsperiodeStringList(response);
        });
    }

    private extractGesuchsperiodeStringList(allActiveGesuchsperioden: TSGesuchsperiode[]) {
        allActiveGesuchsperioden.forEach((gesuchsperiode: TSGesuchsperiode) => {
            this.activeGesuchsperiodenList.push(gesuchsperiode.gesuchsperiodeString);
        });
    }

    public updateInstitutionenList(): void {
        this.institutionRS.getInstitutionenForCurrentBenutzer().then((response: any) => {
            this.institutionenList = angular.copy(response);
        });
    }

    public updateBetreuungsangebotTypList(): void {
        this.institutionStammdatenRS.getBetreuungsangeboteForInstitutionenOfCurrentBenutzer().then((response: any) => {
            this.betreuungsangebotTypList = angular.copy(response);
        });
    }

    public getPendenzenList(): Array<TSPendenzInstitution> {
        return this.pendenzenList;
    }

    public editPendenzInstitution(pendenz: TSPendenzInstitution, event: any): void {
        if (pendenz) {
            let isCtrlKeyPressed: boolean = (event && event.ctrlKey);
            this.openBetreuung(pendenz, isCtrlKeyPressed);
        }
    }

    private openBetreuung(pendenz: TSPendenzInstitution, isCtrlKeyPressed: boolean): void {
        let numberParts: TSBetreuungsnummerParts = this.ebeguUtil.splitBetreuungsnummer(pendenz.betreuungsNummer);
        if (numberParts && pendenz) {
            let kindNumber: number = parseInt(numberParts.kindnummer);
            let betreuungNumber: number = parseInt(numberParts.betreuungsnummer);
            if (betreuungNumber > 0) {
                this.berechnungsManager.clear(); // nur um sicher zu gehen, dass alle alte Werte geloescht sind

                // Reload Gesuch in gesuchModelManager on Init in fallCreationView because it has been changed since last time
                this.gesuchModelManager.clearGesuch();
                let navObj: any = {
                    betreuungNumber: betreuungNumber,
                    kindNumber: kindNumber,
                    gesuchId: pendenz.gesuchId
                };
                if (isCtrlKeyPressed) {
                    let url = this.$state.href('gesuch.betreuung', navObj);
                    window.open(url, '_blank');
                } else {
                    this.$state.go('gesuch.betreuung', navObj);
                }
            }
        }
    }
}
