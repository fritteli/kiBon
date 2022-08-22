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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';
import {StateService} from '@uirouter/angular';
import {IPromise} from 'angular';
import {Permission} from '../../../app/authorisation/Permission';
import {PERMISSIONS} from '../../../app/authorisation/Permissions';
import {LogFactory} from '../../../app/core/logging/LogFactory';
import {BenutzerRSX} from '../../../app/core/service/benutzerRSX.rest';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {TSBenutzer} from '../../../models/TSBenutzer';
import {TSUserSearchresultDTO} from '../../../models/TSUserSearchresultDTO';

const LOG = LogFactory.createLog('BenutzerListViewXComponent');

@Component({
    selector: 'dv-benutzer-list-view-x',
    templateUrl: './benutzer-list-view-x.component.html',
    styleUrls: ['./benutzer-list-view-x.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    host: {
        class: 'overflow-scroll',
        flex: 'auto'
    }
})
export class BenutzerListViewXComponent implements OnInit {

    public totalResultCount: number = 0;

    public constructor(
        private readonly state: StateService,
        private readonly benutzerRS: BenutzerRSX,
        private readonly authServiceRS: AuthServiceRS
    ) {
    }

    public ngOnInit(): void {
    }

    /**
     * Fuer Benutzer mit der Rolle SACHBEARBEITER_INSTITUTION oder SACHBEARBEITER_TRAEGERSCHAFT oeffnet es das Gesuch
     * mit beschraenkten Daten Fuer anderen Benutzer wird das Gesuch mit allen Daten geoeffnet
     */
    public editBenutzer(user?: TSBenutzer): void {
        if (user) {
            this.state.go('admin.benutzer', {benutzerId: user.username});
        }
    }

    public benutzerEinladen(): void {
        this.state.go('benutzer.einladen');
    }

    public passFilterToServer(tableFilterState: any): IPromise<TSUserSearchresultDTO> {
        LOG.debug('Triggering ServerFiltering with Filter Object', tableFilterState);

        return this.benutzerRS.searchUsers(tableFilterState).then((response: TSUserSearchresultDTO) => {
            this.totalResultCount = response.totalResultSize ? response.totalResultSize : 0;

            return response;
        });
    }

    public showEinladen(): boolean {
        return this.authServiceRS.isOneOfRoles(PERMISSIONS[Permission.BENUTZER_EINLADEN]);
    }
}
