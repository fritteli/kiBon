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

import {IController, IDirective, IDirectiveFactory} from 'angular';
import AuthServiceRS from '../../../../authentication/service/AuthServiceRS.rest';
import GesuchModelManager from '../../../../gesuch/service/gesuchModelManager';
import TSGesuch from '../../../../models/TSGesuch';
import TSBenutzer from '../../../../models/TSBenutzer';
import EbeguUtil from '../../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../../utils/TSRoleUtil';
import BenutzerRS from '../../service/benutzerRS.rest';
import ITranslateService = angular.translate.ITranslateService;

export class DvVerantwortlicherselect implements IDirective {
    public restrict = 'E';
    public require = {};
    public scope = {};
    public controller = VerantwortlicherselectController;
    public controllerAs = 'vm';
    public bindToController = {
        isSchulamt: '<',
        gemeindeId: '<',
    };
    public template = require('./dv-verantwortlicherselect.html');

    public static factory(): IDirectiveFactory {
        const directive = () => new DvVerantwortlicherselect();
        directive.$inject = [];
        return directive;
    }
}

export class VerantwortlicherselectController implements IController {

    public static $inject: string[] = ['BenutzerRS', 'AuthServiceRS', 'GesuchModelManager', '$translate'];

    public TSRoleUtil = TSRoleUtil;
    public isSchulamt: boolean;
    public gemeindeId: string;

    public userList: Array<TSBenutzer>;

    public constructor(private readonly benutzerRS: BenutzerRS,
                       private readonly authServiceRS: AuthServiceRS,
                       private readonly gesuchModelManager: GesuchModelManager,
                       private readonly $translate: ITranslateService) {
    }

    public $onChanges(changes: any) {
        if (changes.gemeindeId) {
            this.updateUserList();
        }
    }

    public getTitel(): string {
        return this.$translate.instant(EbeguUtil.getTitleVerantwortlicher(this.isSchulamt));
    }

    public getGesuch(): TSGesuch {
        return this.gesuchModelManager.getGesuch();
    }

    public getVerantwortlicherFullName(): string {
        if (this.getGesuch() && this.getGesuch().dossier) {
            if (this.isSchulamt && this.getGesuch().dossier.verantwortlicherTS) {
                return this.getGesuch().dossier.verantwortlicherTS.getFullName();
            }
            if (!this.isSchulamt && this.getGesuch().dossier.verantwortlicherBG) {
                return this.getGesuch().dossier.verantwortlicherBG.getFullName();
            }
        }
        return this.$translate.instant('NO_VERANTWORTLICHER_SELECTED');
    }

    /**
     * Sets the given user as the verantworlicher fuer den aktuellen Fall
     * @param verantwortlicher
     */
    public setVerantwortlicher(verantwortlicher: TSBenutzer): void {
        this.setVerantwortlicherGesuchModelManager(verantwortlicher);
        this.setUserAsFallVerantwortlicherLocal(verantwortlicher);
    }

    private setVerantwortlicherGesuchModelManager(verantwortlicher: TSBenutzer) {
        if (this.isSchulamt) {
            this.gesuchModelManager.setUserAsFallVerantwortlicherTS(verantwortlicher);
        } else {
            this.gesuchModelManager.setUserAsFallVerantwortlicherBG(verantwortlicher);
        }
    }

    public setUserAsFallVerantwortlicherLocal(user: TSBenutzer) {
        if (user && this.getGesuch() && this.getGesuch().dossier) {
            if (this.isSchulamt) {
                this.getGesuch().dossier.verantwortlicherTS = user;
            } else {
                this.getGesuch().dossier.verantwortlicherBG = user;
            }
        }
    }

    /**
     *
     * @param user
     * @returns {boolean} true if the given user is already the verantwortlicherBG of the current fall
     */
    public isCurrentVerantwortlicher(user: TSBenutzer): boolean {
        return (user && this.getFallVerantwortlicher() && this.getFallVerantwortlicher().username === user.username);
    }

    public getFallVerantwortlicher(): TSBenutzer {
        if (this.isSchulamt) {
            return this.gesuchModelManager.getFallVerantwortlicherTS();
        } else {
            return this.gesuchModelManager.getFallVerantwortlicherBG();
        }
    }

    private updateUserList(): void {
        if (!this.gemeindeId) {
            this.userList = [];

            return;
        }

        this.isSchulamt
            ? this.updateSchulamtUserList()
            : this.updateJugendAmtUserList();
    }

    private updateSchulamtUserList(): void {
        this.benutzerRS.getBenutzerSCHorAdminSCH().then(response => {
            this.userList = this.sortUsers(this.filterUsers(response, this.gemeindeId));
        });
    }

    private updateJugendAmtUserList(): void {
        this.benutzerRS.getBenutzerJAorAdmin().then(response => {
            this.userList = this.sortUsers(this.filterUsers(response, this.gemeindeId));
        });
    }

    private sortUsers(userList: Array<TSBenutzer>) {
        return userList.sort((a, b) => a.getFullName().localeCompare(b.getFullName()));
    }

    /**
     *  Filters out users that have no berechtigung on the current gemeinde
     */
    private filterUsers(userList: Array<TSBenutzer>, gemeindeId: string): Array<TSBenutzer> {
        return userList.filter(user => user.berechtigungen
            .some(berechtigung => berechtigung.gemeindeList
                .some(gemeinde => gemeindeId === gemeinde.id)));
    }
}
