/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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

import {ChangeDetectionStrategy, Component, Input, OnDestroy, OnInit} from '@angular/core';
import {ControlContainer, NgForm} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {Observable, Subject} from 'rxjs';
import {takeUntil} from 'rxjs/operators';
import TSGemeindeStammdaten from '../../../models/TSGemeindeStammdaten';
import {LogFactory} from '../../core/logging/LogFactory';

const LOG = LogFactory.createLog('EditGemeindeComponentStammdaten');

@Component({
    selector: 'dv-edit-gemeinde-stammdaten',
    templateUrl: './edit-gemeinde-stammdaten.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    viewProviders: [ { provide: ControlContainer, useExisting: NgForm } ],
})
export class EditGemeindeComponentStammdaten implements OnInit, OnDestroy {

    @Input() public stammdaten$: Observable<TSGemeindeStammdaten>;
    @Input() private readonly gemeindeId: string;
    @Input() public keineBeschwerdeAdresse: boolean;
    @Input() public editMode: boolean;
    public korrespondenzsprache: string;

    private readonly unsubscribe$ = new Subject<void>();

    public constructor(
        private readonly translate: TranslateService,
    ) {
    }

    public ngOnInit(): void {
        if (!this.gemeindeId) {
            return;
        }
        this.stammdaten$
            .pipe(takeUntil(this.unsubscribe$))
            .subscribe(
            stammdaten => this.initStrings(stammdaten),
            err => LOG.error(err)
        );
    }

    public ngOnDestroy(): void {
        this.unsubscribe$.next();
        this.unsubscribe$.complete();
    }

    private initStrings(stammdaten: TSGemeindeStammdaten): void {
        const languages: string[] = [];
        if (stammdaten.korrespondenzspracheDe) {
            languages.push(this.translate.instant('DEUTSCH'));
        }
        if (stammdaten.korrespondenzspracheFr) {
            languages.push(this.translate.instant('FRANZOESISCH'));
        }
        this.korrespondenzsprache = languages.join(', ');
    }
}