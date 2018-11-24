/*
 * AGPL File-Header
 *
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

import {AfterViewInit, ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit, ViewChild} from '@angular/core';
import {NgForm} from '@angular/forms';
import {MatDialog, MatDialogConfig, MatPaginator, MatSort, MatTableDataSource} from '@angular/material';
import {StateService} from '@uirouter/core';
import AbstractAdminViewController from '../../../admin/abstractAdminView';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import {TSInstitutionStatus} from '../../../models/enums/TSInstitutionStatus';
import {TSRole} from '../../../models/enums/TSRole';
import TSInstitution from '../../../models/TSInstitution';
import EbeguUtil from '../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {DvNgRemoveDialogComponent} from '../../core/component/dv-ng-remove-dialog/dv-ng-remove-dialog.component';
import {InstitutionRS} from '../../core/service/institutionRS.rest';

@Component({
    selector: 'dv-institution-list',
    templateUrl: './institution-list.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class InstitutionListComponent extends AbstractAdminViewController implements OnInit, AfterViewInit {

    public displayedColumns: string[] = [];
    public institutionen: TSInstitution[];
    public selectedInstitution: TSInstitution = undefined;
    public dataSource: MatTableDataSource<TSInstitution>;

    @ViewChild(NgForm) public form: NgForm;
    @ViewChild(MatSort) public sort: MatSort;
    @ViewChild(MatPaginator) public paginator: MatPaginator;

    public constructor(
        private readonly institutionRS: InstitutionRS,
        private readonly dialog: MatDialog,
        private readonly changeDetectorRef: ChangeDetectorRef,
        private readonly $state: StateService,
        authServiceRS: AuthServiceRS,
    ) {
        super(authServiceRS);
    }

    public ngOnInit(): void {
        this.updateInstitutionenList();
        this.sortTable();
        this.setDisplayedColumns();
    }

    public ngAfterViewInit(): void {
        if (!this.dataSource) {
            return;
        }
        this.dataSource.sort = this.sort;
        this.dataSource.paginator = this.paginator;
    }

    public updateInstitutionenList(): void {
        this.institutionRS.getInstitutionenForCurrentBenutzer()
            .then(insti => {
                this.dataSource = new MatTableDataSource(insti);
                this.dataSource.paginator = this.paginator;
                this.changeDetectorRef.markForCheck();
            });
    }

    private sortTable(): void {
        this.sort.sort({
                id: 'name',
                start: 'asc',
                disableClear: false,
            },
        );
    }

    public removeInstitution(institution: any): void {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
            title: 'LOESCHEN_DIALOG_TITLE',
        };
        this.dialog.open(DvNgRemoveDialogComponent, dialogConfig).afterClosed()
            .subscribe(userAccepted => {   // User confirmed removal
                if (!userAccepted) {
                    return;
                }
                this.selectedInstitution = undefined;
                this.institutionRS.removeInstitution(institution.id).then(() => {
                    const index = EbeguUtil.getIndexOfElementwithID(institution, this.institutionen);
                    if (index > -1) {
                        this.institutionen.splice(index, 1);
                    }
                });
            });
    }

    public createInstitution(): void {
        this.$state.go('institution.add');
    }

    /**
     * Institutions in status EINGELADEN cannot be opened from the list
     */
    public openInstitution(institution: TSInstitution): void {
        if (institution.status !== TSInstitutionStatus.EINGELADEN) {
            this.$state.go('institution.edit', {
                institutionId: institution.id,
            });
        }
        return;
    }

    public isCreateAllowed(): boolean {
        return this.authServiceRS.isOneOfRoles(TSRoleUtil.getMandantRoles());
    }

    public isDeleteAllowed(): boolean {
        return this.authServiceRS.isRole(TSRole.SUPER_ADMIN);
    }

    public showNoContentMessage(): boolean {
        return !this.dataSource || this.dataSource.data.length === 0;
    }

    private setDisplayedColumns(): void {
        this.displayedColumns = this.isDeleteAllowed()
            ? ['name', 'status', 'remove']
            : ['name', 'status'];
    }
}
