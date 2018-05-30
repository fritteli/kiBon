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
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {StateService} from '@uirouter/core';

let template = require('./dv-home-icon.html');

export class DvHomeIconComponentConfig implements IComponentOptions {
    transclude = false;
    bindings: any = {};
    template = template;
    controller = DvHomeIconController;
    controllerAs = 'vm';
}

export class DvHomeIconController {

    TSRoleUtil: any;

    static $inject: any[] = ['$state'];

    constructor(private $state: StateService) {
        this.TSRoleUtil = TSRoleUtil;
    }

    public goBackHome(): void {
        this.$state.go('gesuchstellerDashboard');
    }

    public isCurrentPageGSDashboard(): boolean {
        return (this.$state.current && this.$state.current.name === 'gesuchstellerDashboard');
    }
}
