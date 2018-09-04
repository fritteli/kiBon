/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2018 City of Bern Switzerland
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

import {NgModule} from '@angular/core';
import {MatTableDataSource} from '@angular/material';
import {Ng2StateDeclaration} from '@uirouter/angular';
import {UIRouterUpgradeModule} from '@uirouter/angular-hybrid';
import {IPromise} from 'angular';
import {takeUntil} from 'rxjs/operators';
import {LogFactory} from '../app/core/logging/LogFactory';
import {TraegerschaftRS} from '../app/core/service/traegerschaftRS.rest';
import GemeindeRS from '../gesuch/service/gemeindeRS.rest';
import TSGemeinde from '../models/TSGemeinde';
import {TSRoleUtil} from '../utils/TSRoleUtil';
import {BatchjobTriggerViewComponent} from './component/batchjobTriggerView/batchjobTriggerView';
import {TestdatenViewComponent} from './component/testdatenView/testdatenView';
import {TraegerschaftViewComponent} from './component/traegerschaftView/traegerschaftView';
import {GemeindenViewComponent} from './component/gemeindenView/gemeindenView';


export const traegerschaftState: Ng2StateDeclaration = {
    name: 'admin.traegerschaft',
    url: '/traegerschaft',
    component: TraegerschaftViewComponent,
    resolve: [
        {
            token: 'traegerschaften',
            deps: [TraegerschaftRS],
            resolveFn: getTraegerschaften,
        }
    ],
    data: {
        roles: TSRoleUtil.getAdministratorRevisorRole(),
    }
};

export const gemeindenState: Ng2StateDeclaration = {
    name: 'admin.gemeinden',
    url: '/gemeinden',
    component: GemeindenViewComponent,
    data: {
        roles: TSRoleUtil.getAdministratorRevisorRole(),
    }
};

export const testdatenState: Ng2StateDeclaration = {
    name: 'admin.testdaten',
    url: '/testdaten',
    component: TestdatenViewComponent,
    data: {
        roles: TSRoleUtil.getSuperAdminRoles(),
    }
};

export const batchjobTriggerState: Ng2StateDeclaration = {
    name: 'admin.batchjobTrigger',
    url: '/batchjobTrigger',
    component: BatchjobTriggerViewComponent,
};

@NgModule({
    imports: [
        UIRouterUpgradeModule.forChild({states: [traegerschaftState, gemeindenState, testdatenState, batchjobTriggerState]}),
    ],
    exports: [
        UIRouterUpgradeModule
    ],
})
export class NgAdminRoutingModule {
}

function getTraegerschaften(traegerschaftRS: TraegerschaftRS) {
    return traegerschaftRS.getAllActiveTraegerschaften();
}
