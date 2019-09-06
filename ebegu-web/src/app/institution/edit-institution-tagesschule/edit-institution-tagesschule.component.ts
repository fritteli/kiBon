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

import {ChangeDetectionStrategy, Component, Input, OnInit} from '@angular/core';
import {ControlContainer, NgForm} from '@angular/forms';
import GemeindeRS from '../../../gesuch/service/gemeindeRS.rest';
import {TSDayOfWeek} from '../../../models/enums/TSDayOfWeek';
import {TSModulTagesschuleIntervall} from '../../../models/enums/TSModulTagesschuleIntervall';
import {getTSModulTagesschuleNameValues, TSModulTagesschuleName} from '../../../models/enums/TSModulTagesschuleName';
import TSGemeinde from '../../../models/TSGemeinde';
import TSGesuchsperiode from '../../../models/TSGesuchsperiode';
import TSInstitutionStammdaten from '../../../models/TSInstitutionStammdaten';
import TSInstitutionStammdatenTagesschule from '../../../models/TSInstitutionStammdatenTagesschule';
import TSModulTagesschule from '../../../models/TSModulTagesschule';
import TSModulTagesschuleGroup from '../../../models/TSModulTagesschuleGroup';
import EbeguUtil from '../../../utils/EbeguUtil';
import GesuchsperiodeRS from '../../core/service/gesuchsperiodeRS.rest';

@Component({
    selector: 'dv-edit-institution-tagesschule',
    templateUrl: './edit-institution-tagesschule.component.html',
    styleUrls: ['./edit-institution-tagesschule.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    viewProviders: [{provide: ControlContainer, useExisting: NgForm}],
})

export class EditInstitutionTagesschuleComponent implements OnInit {

    @Input() public stammdaten: TSInstitutionStammdaten;
    @Input() public editMode: boolean = false;

    public gesuchsperiodenList: TSGesuchsperiode[] = [];
    public gemeindeList: TSGemeinde[] = [];
    private moduleToEdit: Map<string, TSModulTagesschuleGroup[]> = new Map<string, TSModulTagesschuleGroup[]>();
    private modulePersisted: Map<string, TSModulTagesschuleGroup[]> = new Map<string, TSModulTagesschuleGroup[]>();

    public constructor(
        private readonly gemeindeRS: GemeindeRS,
        private readonly gesuchsperiodeRS: GesuchsperiodeRS,
    ) {
    }

    public ngOnInit(): void {
        if (EbeguUtil.isNullOrUndefined(this.stammdaten.institutionStammdatenTagesschule)) {
            this.stammdaten.institutionStammdatenTagesschule = new TSInstitutionStammdatenTagesschule();
            this.stammdaten.institutionStammdatenTagesschule.modulTagesschuleGroups = [];
        }
        this.gesuchsperiodeRS.getAllActiveGesuchsperioden().then(allGesuchsperioden => {
            this.gesuchsperiodenList = allGesuchsperioden;
            this.loadModuleTagesschule();
        });
        this.gemeindeRS.getAllGemeinden().then(allGemeinden => {
            this.gemeindeList = allGemeinden;
        });
    }

    public onPrePersist(): void {
        this.replaceTagesschulmoduleOnInstitutionStammdatenTagesschule();
    }

    /**
     * Gibt alle potenziell auszufüllenden Module für die GP zurück (also auch leere)
     */
    public getModuleTagesschuleForGesuchsperiodeToEdit(gesuchsperiodeId: string): TSModulTagesschuleGroup[] {
        return this.moduleToEdit.get(gesuchsperiodeId);
    }

    public getModuleTagesschuleForGesuchsperiodePersisted(gesuchsperiodeId: string): TSModulTagesschuleGroup[] {
        return this.modulePersisted.get(gesuchsperiodeId);
    }

    private loadModuleTagesschule(): void {
        const moduleFromServer = this.stammdaten.institutionStammdatenTagesschule.modulTagesschuleGroups;
        this.modulePersisted = this.toMapPerGesuchsperiode(moduleFromServer, false);
        this.moduleToEdit = new Map<string, TSModulTagesschuleGroup[]>();
        // tslint:disable-next-line:early-exit
        // tslint:disable-next-line:prefer-conditional-expression
        if (moduleFromServer && moduleFromServer.length > 0) {
            this.moduleToEdit = this.toMapPerGesuchsperiode(moduleFromServer, true);
        } else {
            this.moduleToEdit = this.toMapPerGesuchsperiode([], true);
        }
    }

    private toMapPerGesuchsperiode(moduleGroupList: TSModulTagesschuleGroup[], addMissing: boolean
    ): Map<string, TSModulTagesschuleGroup[]> {
        const mapPerGesuchsperiode = new Map<string, TSModulTagesschuleGroup[]>();
        this.gesuchsperiodenList.forEach((gp: TSGesuchsperiode) => {
            if (!mapPerGesuchsperiode.get(gp.id)) {
                mapPerGesuchsperiode.set(gp.id, []);
            }
            getTSModulTagesschuleNameValues().forEach((modulname: TSModulTagesschuleName) => {
                let foundmodul = moduleGroupList.filter(modul => (
                    modul.modulTagesschuleName === modulname &&
                    modul.gesuchsperiodeId === gp.id
                ))[0];
                // tslint:disable-next-line:early-exit
                if (addMissing && !foundmodul) {
                    foundmodul = this.createModulGroupTagesschule(gp.id, modulname);
                }
                if (foundmodul) {
                    mapPerGesuchsperiode.get(gp.id).push(foundmodul);
                }
            });
        });
        return mapPerGesuchsperiode;
    }

    private createModulGroupTagesschule(gesuchsperiodeId: string, modulname: TSModulTagesschuleName
    ): TSModulTagesschuleGroup {
        // Gespeichert wird das Modul dann fuer jeden Wochentag. Als Vertreter wird der Montag ausgefüllt
        const modul = new TSModulTagesschuleGroup();
        modul.gesuchsperiodeId = gesuchsperiodeId;
        modul.modulTagesschuleName = modulname;
        modul.bezeichnung = 'Testmodul';
        modul.intervall = TSModulTagesschuleIntervall.WOECHENTLICH;
        modul.wirdPaedagogischBetreut = true;
        modul.module = [];
        let montag = new TSModulTagesschule();
        montag.wochentag = TSDayOfWeek.MONDAY;
        modul.module.push(montag);
        return modul;
    }

    private replaceTagesschulmoduleOnInstitutionStammdatenTagesschule(): void {
        const definedModulTagesschule: TSModulTagesschuleGroup[] = [];
        this.moduleToEdit.forEach(mapOfModules => {
            mapOfModules.forEach(tempModul => {
                if (tempModul.zeitVon && tempModul.zeitBis) {
                    definedModulTagesschule.push(tempModul);
                }
            });
        });
        // tslint:disable-next-line:early-exit
        if (definedModulTagesschule.length > 0) {
            this.stammdaten.institutionStammdatenTagesschule.modulTagesschuleGroups = definedModulTagesschule;
        }
    }

    public compareGemeinde(b1: TSGemeinde, b2: TSGemeinde): boolean {
        return b1 && b2 ? b1.id === b2.id : b1 === b2;
    }
}
