/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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

import {HttpClientModule} from '@angular/common/http';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {of} from 'rxjs';
import {SHARED_MODULE_OVERRIDES} from '../../../../hybridTools/mockUpgradedComponent';
import {TSFerienbetreuungAngabenContainer} from '../../../../models/gemeindeantrag/TSFerienbetreuungAngabenContainer';
import {ErrorService} from '../../../core/errors/service/ErrorService';
import {SharedModule} from '../../../shared/shared.module';
import {FerienbetreuungService} from '../services/ferienbetreuung.service';

import {FerienbetreuungNutzungComponent} from './ferienbetreuung-nutzung.component';

const ferienbetreuungServiceSpy = jasmine.createSpyObj<FerienbetreuungService>(
    FerienbetreuungService.name,
    ['getFerienbetreuungContainer'],
);
const errorServiceSpy = jasmine.createSpyObj<ErrorService>(ErrorService.name,
    ['addMesageAsError', 'addMesageAsInfo']);

describe('FerienbetreuungNutzungComponent', () => {
    let component: FerienbetreuungNutzungComponent;
    let fixture: ComponentFixture<FerienbetreuungNutzungComponent>;

    const container = new TSFerienbetreuungAngabenContainer();
    container.angabenDeklaration = null;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [FerienbetreuungNutzungComponent],
            imports: [
                FormsModule,
                ReactiveFormsModule,
                SharedModule,
                HttpClientModule,
            ],
            providers: [
                {provide: FerienbetreuungService, useValue: ferienbetreuungServiceSpy},
                {provide: ErrorService, useValue: errorServiceSpy},
            ],
        }).overrideModule(SharedModule, SHARED_MODULE_OVERRIDES)
            .compileComponents();
    });

    beforeEach(() => {
        ferienbetreuungServiceSpy.getFerienbetreuungContainer.and.returnValue(of(container));
        fixture = TestBed.createComponent(FerienbetreuungNutzungComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});