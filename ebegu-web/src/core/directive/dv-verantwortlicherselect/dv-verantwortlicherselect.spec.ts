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

import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import GesuchModelManager from '../../../gesuch/service/gesuchModelManager';
import {ngServicesMock} from '../../../hybridTools/ngServicesMocks';
import TSDossier from '../../../models/TSDossier';
import TSGesuch from '../../../models/TSGesuch';
import TSUser from '../../../models/TSUser';
import {EbeguWebCore} from '../../core.module';
import UserRS from '../../service/userRS.rest';
import {VerantwortlicherselectController} from './dv-verantwortlicherselect';
import ITranslateService = angular.translate.ITranslateService;

describe('dvVerantwortlicherSelect', function () {

    let gesuchModelManager: GesuchModelManager;
    let verantwortlicherselectController: VerantwortlicherselectController;
    let authServiceRS: AuthServiceRS;
    let userRS: UserRS;
    let user: TSUser;
    let $mdSidenav: angular.material.ISidenavService;
    let $translate: ITranslateService;

    beforeEach(angular.mock.module(EbeguWebCore.name));

    beforeEach(angular.mock.module(ngServicesMock));

    beforeEach(angular.mock.inject(function ($injector: angular.auto.IInjectorService) {
        gesuchModelManager = $injector.get('GesuchModelManager');
        authServiceRS = $injector.get('AuthServiceRS');
        userRS = $injector.get('UserRS');
        $mdSidenav = $injector.get('$mdSidenav');
        user = new TSUser('Emiliano', 'Camacho');
        $translate = $injector.get('$translate');

        verantwortlicherselectController = new VerantwortlicherselectController(userRS,
            authServiceRS, gesuchModelManager, $translate);
    }));

    describe('getVerantwortlicherFullName', () => {
        it('returns empty string for empty verantwortlicherBG', () => {
            expect(verantwortlicherselectController.getVerantwortlicherFullName()).toEqual('kein Verant.');
        });

        it('returns the fullname of the verantwortlicherBG', () => {
            let verantwortlicher: TSUser = new TSUser('Emiliano', 'Camacho');
            let gesuch: TSGesuch = new TSGesuch();
            gesuch.dossier = new TSDossier();
            gesuch.dossier.verantwortlicherBG = verantwortlicher;
            spyOn(gesuchModelManager, 'getGesuch').and.returnValue(gesuch);
            expect(verantwortlicherselectController.getVerantwortlicherFullName()).toEqual('Emiliano Camacho');
        });
    });
    describe('setVerantwortlicher()', () => {
        it('does nothing if the passed user is empty, verantwortlicherBG remains as it was before', () => {
            createGesuch();
            spyOn(gesuchModelManager, 'setUserAsFallVerantwortlicherBG');

            verantwortlicherselectController.setVerantwortlicher(undefined);
            expect(gesuchModelManager.getGesuch().dossier.verantwortlicherBG).toBe(user);
        });
        it('sets the user as the verantwortlicherBG of the current fall', () => {
            createGesuch();
            spyOn(gesuchModelManager, 'setUserAsFallVerantwortlicherBG');

            let newUser: TSUser = new TSUser('Adolfo', 'Contreras');
            verantwortlicherselectController.setVerantwortlicher(newUser);
            expect(gesuchModelManager.getGesuch().dossier.verantwortlicherBG).toBe(newUser);
        });
    });

    function createGesuch() {
        let gesuch: TSGesuch = new TSGesuch();
        let dossier: TSDossier = new TSDossier();
        dossier.verantwortlicherBG = user;
        gesuch.dossier = dossier;
        gesuchModelManager.setGesuch(gesuch);
    }

});
