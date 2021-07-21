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

import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {BehaviorSubject, combineLatest} from 'rxjs';
import {AuthServiceRS} from '../../../../../authentication/service/AuthServiceRS.rest';
import {TSSprache} from '../../../../../models/enums/TSSprache';
import {TSLastenausgleichTagesschuleAngabenGemeindeContainer} from '../../../../../models/gemeindeantrag/TSLastenausgleichTagesschuleAngabenGemeindeContainer';
import {TSBenutzer} from '../../../../../models/TSBenutzer';
import {TSRoleUtil} from '../../../../../utils/TSRoleUtil';
import {ErrorService} from '../../../../core/errors/service/ErrorService';
import {DownloadRS} from '../../../../core/service/downloadRS.rest';
import {LastenausgleichTSService} from '../../services/lastenausgleich-ts.service';

@Component({
    selector: 'dv-lastenausgleich-ts-berechnung',
    templateUrl: './lastenausgleich-ts-berechnung.component.html',
    styleUrls: ['./lastenausgleich-ts-berechnung.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class LastenausgleichTsBerechnungComponent implements OnInit {

    private static readonly FILENAME_DE = 'Verfügung Tagesschulen kiBon';
    private static readonly FILENAME_FR = 'Modèle Décisions EJC kibon';

    public canViewDokumentErstellenButton: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
    public downloadingDeFile: BehaviorSubject<boolean> = new BehaviorSubject(false);
    public downloadingFrFile: BehaviorSubject<boolean> = new BehaviorSubject(false);

    private latsContainer: TSLastenausgleichTagesschuleAngabenGemeindeContainer;
    private principal: TSBenutzer | null;

    public constructor(
        private readonly translate: TranslateService,
        private readonly errorService: ErrorService,
        private readonly latsService: LastenausgleichTSService,
        private readonly authService: AuthServiceRS,
        private readonly downloadRS: DownloadRS
    ) {
    }

    public ngOnInit(): void {
        combineLatest([
            this.latsService.getLATSAngabenGemeindeContainer(),
            this.authService.principal$,
        ]).subscribe(values => {
            this.latsContainer = values[0];
            this.principal = values[1];
            this.canViewDokumentErstellenButton.next(this.principal.hasOneOfRoles(TSRoleUtil.getMandantRoles()));
        }, () => this.errorService.addMesageAsInfo(this.translate.instant('DATA_RETRIEVAL_ERROR')));
    }

    public ascreateLatsDocumentDe(): void {
        this.downloadingDeFile.next(true);
        this.latsService.latsDocxErstellen(this.latsContainer, TSSprache.DEUTSCH)
            .subscribe(
                response => {
                    this.createDownloadFile(response, TSSprache.DEUTSCH);
                    this.downloadingFrFile.next(false);
                },
                async err => {
                    const message = JSON.parse(await err.error.text());
                    this.errorService.addMesageAsError(message.translatedMessage);
                });
    }

    public createLatsDocumentFr(): void {
        this.downloadingFrFile.next(true);
        this.latsService.latsDocxErstellen(this.latsContainer, TSSprache.FRANZOESISCH)
            .subscribe(
                response => {
                    this.createDownloadFile(response, TSSprache.DEUTSCH);
                    this.downloadingFrFile.next(false);
                },
                async err => {
                    const message = JSON.parse(await err.error.text());
                    this.errorService.addMesageAsError(message.translatedMessage);
                }
            );
    }

    private createDownloadFile(response: BlobPart, sprache: TSSprache): void {
        const file = new Blob([response], {type: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'});
        const filename = this.getFilename(sprache);
        this.downloadRS.openDownload(file, filename);
    }

    private getFilename(sprache: TSSprache): string {
        let filename;
        (sprache === TSSprache.DEUTSCH)
            ? filename = LastenausgleichTsBerechnungComponent.FILENAME_DE
            : filename = LastenausgleichTsBerechnungComponent.FILENAME_FR;

        return `${filename} ${this.latsContainer.gesuchsperiode.gesuchsperiodeString} ${this.latsContainer.gemeinde.name}.docx`;
    }
}
