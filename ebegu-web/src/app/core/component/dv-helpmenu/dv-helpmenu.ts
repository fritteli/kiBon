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

import {Component} from '@angular/core';
import {MatDialog, MatDialogConfig} from '@angular/material';
import {DvNgHelpDialogComponent} from '../../../../gesuch/dialog/dv-ng-help-dialog.component';

@Component({
    selector: 'dv-helpmenu',
    templateUrl: './dv-helpmenu.html',
    styleUrls: ['./dv-helpmenu.less'],
})
export class DvHelpmenuComponent {

    display: boolean = false;

    constructor(private readonly dialog: MatDialog) {
    }

    public showDialog(): void {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.disableClose = false; // dialog is canceled by clicking outside
        dialogConfig.autoFocus = true;

        this.dialog.open(DvNgHelpDialogComponent, dialogConfig);
    }

}
