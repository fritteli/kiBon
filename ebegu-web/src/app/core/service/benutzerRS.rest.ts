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

import {IHttpPromise, IHttpService, ILogService, IPromise} from 'angular';
import TSBenutzer from '../../../models/TSBenutzer';
import TSBerechtigungHistory from '../../../models/TSBerechtigungHistory';
import TSUserSearchresultDTO from '../../../models/TSUserSearchresultDTO';
import EbeguRestUtil from '../../../utils/EbeguRestUtil';
import {IEntityRS} from './iEntityRS.rest';

export default class BenutzerRS implements IEntityRS {

    public static $inject = ['$http', 'REST_API', 'EbeguRestUtil', '$log'];
    public serviceURL: string;

    public constructor(
        public $http: IHttpService,
        REST_API: string,
        public ebeguRestUtil: EbeguRestUtil,
        private readonly $log: ILogService,
    ) {
        this.serviceURL = `${REST_API}benutzer`;
    }

    /**
     * Gibt alle existierenden Benutzer mit den Rollen Sachbearbeiter_BG oder Admin_BG oder
     * Sachbearbeiter_Gemeinde oder Admin_Gemeinde zurueck.
     */
    public getBenutzerBgOrGemeinde(): IPromise<TSBenutzer[]> {
        return this.getBenutzer(`${this.serviceURL}/BgOrGemeinde`);
    }

    /**
     * Gibt alle existierenden Benutzer mit Rolle Sachbearbeiter_BG oder Admin_BG zurueck.
     */
    public getBenutzerJAorAdmin(): IPromise<TSBenutzer[]> {
        return this.getBenutzer(`${this.serviceURL}/JAorAdmin`);
    }

    /**
     * Gibt alle existierenden Benutzer mit Rolle ADMIN_TS oder SACHBEARBEITER_TS zurueck.
     */
    public getBenutzerSCHorAdminSCH(): IPromise<TSBenutzer[]> {
        return this.getBenutzer(`${this.serviceURL}/SCHorAdmin`);
    }

    public getAllGesuchsteller(): IPromise<TSBenutzer[]> {
        return this.getBenutzer(`${this.serviceURL}/gesuchsteller`);
    }

    private getBenutzer(url: string): IPromise<TSBenutzer[]> {
        return this.$http.get(url).then((response: any) => {
            this.$log.debug('PARSING benutzer REST array object', response.data);
            return this.ebeguRestUtil.parseUserList(response.data);
        });
    }

    private getSingleBenutzer(url: string): IPromise<TSBenutzer> {
        return this.$http.get(url)
            .then((response: any) => {
                this.$log.debug('PARSING benutzer REST object ', response.data);
                return this.ebeguRestUtil.parseUser(new TSBenutzer(), response.data);
            });
    }

    public searchUsers(userSearch: any): IPromise<TSUserSearchresultDTO> {
        return this.$http.post(`${this.serviceURL}/search/`, userSearch).then((response: any) => {
            this.$log.debug('PARSING benutzer REST array object', response.data);
            const tsBenutzers = this.ebeguRestUtil.parseUserList(response.data.benutzerDTOs);

            return new TSUserSearchresultDTO(tsBenutzers, response.data.paginationDTO.totalItemCount);
        });
    }

    public findBenutzer(username: string): IPromise<TSBenutzer> {
        return this.getSingleBenutzer(`${this.serviceURL}/username/${encodeURIComponent(username)}`);
    }

    public findBenutzerByEmail(email: string): IPromise<TSBenutzer | undefined> {
        return this.getSingleBenutzer(`${this.serviceURL}/email/${encodeURIComponent(email)}`);
    }

    public inactivateBenutzer(user: TSBenutzer): IPromise<TSBenutzer> {
        const userRest = this.ebeguRestUtil.userToRestObject({}, user);
        return this.$http.put(`${this.serviceURL}/inactivate/`, userRest).then((response: any) => {
            return this.ebeguRestUtil.parseUser(new TSBenutzer(), response.data);
        });
    }

    public reactivateBenutzer(benutzer: TSBenutzer): IPromise<TSBenutzer> {
        const benutzerRest = this.ebeguRestUtil.userToRestObject({}, benutzer);
        return this.$http.put(`${this.serviceURL}/reactivate/`, benutzerRest).then((response: any) => {
            return this.ebeguRestUtil.parseUser(new TSBenutzer(), response.data);
        });
    }

    public einladen(benutzer: TSBenutzer): IPromise<TSBenutzer> {
        const benutzerRest = this.ebeguRestUtil.userToRestObject({}, benutzer);
        return this.$http.post(`${this.serviceURL}/einladen/`, benutzerRest).then((response: any) => {
            return this.ebeguRestUtil.parseUser(new TSBenutzer(), response.data);
        });
    }

    public erneutEinladen(benutzer: TSBenutzer): IHttpPromise<any> {
        const benutzerRest = this.ebeguRestUtil.userToRestObject({}, benutzer);
        return this.$http.post(`${this.serviceURL}/erneutEinladen/`, benutzerRest);
    }

    public saveBenutzerBerechtigungen(benutzer: TSBenutzer): IPromise<TSBenutzer> {
        const benutzerRest = this.ebeguRestUtil.userToRestObject({}, benutzer);
        return this.$http.put(`${this.serviceURL}/saveBenutzerBerechtigungen/`, benutzerRest).then((response: any) => {
            return this.ebeguRestUtil.parseUser(new TSBenutzer(), response.data);
        });
    }

    public getBerechtigungHistoriesForBenutzer(username: string): IPromise<TSBerechtigungHistory[]> {
        return this.$http.get(`${this.serviceURL}/berechtigunghistory/${encodeURIComponent(username)}`)
            .then((response: any) => {
                this.$log.debug('PARSING benutzer REST object ', response.data);
                return this.ebeguRestUtil.parseBerechtigungHistoryList(response.data);
            });
    }

    public isBenutzerDefaultBenutzerOfAnyGemeinde(username: string): IPromise<boolean> {
        return this.$http.get(`${this.serviceURL}/isdefaultuser/${encodeURIComponent(username)}`)
            .then((response: any) => {
                return JSON.parse(response.data);
            });
    }
}