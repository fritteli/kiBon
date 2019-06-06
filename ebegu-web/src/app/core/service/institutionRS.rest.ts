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

import {IHttpService, IPromise} from 'angular';
import * as moment from 'moment';
import {TSBetreuungsangebotTyp} from '../../../models/enums/TSBetreuungsangebotTyp';
import TSInstitution from '../../../models/TSInstitution';
import DateUtil from '../../../utils/DateUtil';
import EbeguRestUtil from '../../../utils/EbeguRestUtil';

export class InstitutionRS {

    public static $inject = ['$http', 'REST_API', 'EbeguRestUtil'];
    public serviceURL: string;

    public constructor(
        public $http: IHttpService,
        REST_API: string,
        public ebeguRestUtil: EbeguRestUtil,
    ) {
        this.serviceURL = `${REST_API}institutionen`;
    }

    public findInstitution(institutionID: string): IPromise<TSInstitution> {
        return this.$http.get(`${this.serviceURL}/id/${encodeURIComponent(institutionID)}`)
            .then((response: any) => {
                return this.ebeguRestUtil.parseInstitution(new TSInstitution(), response.data);
            });
    }

    public updateInstitution(institution: TSInstitution): IPromise<TSInstitution> {
        let restInstitution = {};
        restInstitution = this.ebeguRestUtil.institutionToRestObject(restInstitution, institution);
        return this.$http.put(this.serviceURL, restInstitution).then((response: any) => {
            return this.ebeguRestUtil.parseInstitution(new TSInstitution(), response.data);
        });
    }

    /**
     * It sends all required parameters (new Institution, beguStartDatum, Betreuungsangebot and User) to the server so the server can
     * create all required objects within a single transaction.
     */
    public createInstitution(institution: TSInstitution,
                             beguStartDatum: moment.Moment,
                             betreuungsangebot: TSBetreuungsangebotTyp,
                             adminMail: string
    ): IPromise<TSInstitution> {
        const restInstitution = this.ebeguRestUtil.institutionToRestObject({}, institution);
        return this.$http.post(this.serviceURL, restInstitution,
            {
                params: {
                    date: DateUtil.momentToLocalDate(beguStartDatum),
                    betreuung: betreuungsangebot.toString(),
                    adminMail,
                },
            })
            .then(response => {
                return this.ebeguRestUtil.parseInstitution(new TSInstitution(), response.data);
            });
    }

    public getAllInstitutionen(): IPromise<TSInstitution[]> {
        return this.$http.get(this.serviceURL).then((response: any) => {
            return this.ebeguRestUtil.parseInstitutionen(response.data);
        });
    }

    public getAllActiveInstitutionen(): IPromise<TSInstitution[]> {
        return this.$http.get(`${this.serviceURL}/active`).then((response: any) => {
            return this.ebeguRestUtil.parseInstitutionen(response.data);
        });
    }

    public getInstitutionenForCurrentBenutzer(): IPromise<TSInstitution[]> {
        return this.$http.get(`${this.serviceURL}/currentuser`).then((response: any) => {
            return this.ebeguRestUtil.parseInstitutionen(response.data);
        });
    }

    public hasInstitutionenInStatusAngemeldet(): IPromise<boolean> {
        return this.$http.get(`${this.serviceURL}/hasEinladungen/currentuser`).then((response: any) => {
            return response.data;
        });
    }

    public getServiceName(): string {
        return 'InstitutionRS';
    }
}
