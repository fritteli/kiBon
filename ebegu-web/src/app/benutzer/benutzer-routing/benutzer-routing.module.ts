/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

import {NgModule} from '@angular/core';
import {Ng2StateDeclaration} from '@uirouter/angular';
import {UIRouterUpgradeModule} from '@uirouter/angular-hybrid';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {UiViewComponent} from '../../shared/ui-view/ui-view.component';
import {BenutzerEinladenComponent} from '../benutzer-einladen/benutzer-einladen.component';

const states: Ng2StateDeclaration[] = [
    {
        parent: 'app',
        name: 'benutzer',
        abstract: true,
        url: '/benutzer',
        component: UiViewComponent,
        data: {
            roles: TSRoleUtil.getAllAdministratorRevisorRole(),
        },
    },
    {
        name: 'benutzer.einladen',
        url: '/einladen',
        component: BenutzerEinladenComponent,
    },
];

@NgModule({
    imports: [
        UIRouterUpgradeModule.forChild({states}),
    ],
    exports: [
        UIRouterUpgradeModule,
    ],
    declarations: [],
})
export class BenutzerRoutingModule {
}
