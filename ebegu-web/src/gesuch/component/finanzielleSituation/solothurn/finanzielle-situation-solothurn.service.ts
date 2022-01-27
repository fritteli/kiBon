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

import {Injectable} from '@angular/core';
import {TSFamilienstatus} from '../../../../models/enums/TSFamilienstatus';
import {GesuchModelManager} from '../../../service/gesuchModelManager';

@Injectable({
    providedIn: 'root',
})
export class FinanzielleSituationSolothurnService {

    public constructor() {
    }

    public static finSitIsGemeinsam(gesuchModelManager: GesuchModelManager): boolean {
        // TODO finsit Luzern: get this from server or improve
        const familiensituation = gesuchModelManager.getFamiliensituation().familienstatus;
        return familiensituation === TSFamilienstatus.VERHEIRATET
            || familiensituation === TSFamilienstatus.KONKUBINAT
            || (familiensituation === TSFamilienstatus.KONKUBINAT_KEIN_KIND && this.startKonkubinatMoreThan5YearsAgo());
    }

    public static finSitNeedsTwoAntragsteller(gesuchModelManager: GesuchModelManager): boolean {
        // TODO finsit Luzern: get this from server or improve
        return this.finSitIsGemeinsam(gesuchModelManager) && gesuchModelManager.getFamiliensituation().verguenstigungGewuenscht;
    }

    private static startKonkubinatMoreThan5YearsAgo(): boolean {
        // TODO finsit Luzern: migrate once Konkubinat time is in configuration
        return true;
    }
}
