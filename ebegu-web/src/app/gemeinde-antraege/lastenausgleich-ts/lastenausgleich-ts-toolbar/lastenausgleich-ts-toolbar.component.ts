/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

import {ChangeDetectionStrategy, Component, Input, OnInit} from '@angular/core';
import {TSGemeinde} from '../../../../models/TSGemeinde';

@Component({
    selector: 'dv-lastenausgleich-ts-toolbar',
    templateUrl: './lastenausgleich-ts-toolbar.component.html',
    styleUrls: ['./lastenausgleich-ts-toolbar.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class LastenausgleichTsToolbarComponent implements OnInit {

    @Input()
    public gemeinde: TSGemeinde;

    public constructor() {
    }

    public ngOnInit(): void {
    }

}
