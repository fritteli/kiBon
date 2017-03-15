import {IComponentOptions} from 'angular';
import TSZahlungsauftrag from '../../models/TSZahlungsauftrag';
import EbeguUtil from '../../utils/EbeguUtil';
import ZahlungRS from '../../core/service/zahlungRS.rest';
import {DownloadRS} from '../../core/service/downloadRS.rest';
import TSDownloadFile from '../../models/TSDownloadFile';
import {ApplicationPropertyRS} from '../../admin/service/applicationPropertyRS.rest';
import {TSZahlungsauftragsstatus} from '../../models/enums/TSZahlungsauftragstatus';
import {ReportRS} from '../../core/service/reportRS.rest';
import {TSRole} from '../../models/enums/TSRole';
import AuthServiceRS from '../../authentication/service/AuthServiceRS.rest';
import ITimeoutService = angular.ITimeoutService;
import IPromise = angular.IPromise;
import ILogService = angular.ILogService;
import IQService = angular.IQService;
import IStateService = angular.ui.IStateService;
import IFormController = angular.IFormController;
let template = require('./zahlungsauftragView.html');
require('./zahlungsauftragView.less');

export class ZahlungsauftragViewComponentConfig implements IComponentOptions {
    transclude = false;
    template = template;
    controller = ZahlungsauftragViewController;
    controllerAs = 'vm';
}

export class ZahlungsauftragViewController {

    form: IFormController;
    private zahlungsauftragen: Array<TSZahlungsauftrag>;
    private zahlungsauftragToEdit: TSZahlungsauftrag;

    beschrieb: string;
    faelligkeitsdatum: moment.Moment;
    datumGeneriert: moment.Moment;

    itemsByPage: number = 12;
    testMode: boolean = false;

    static $inject: string[] = ['ZahlungRS', 'CONSTANTS', '$state', 'DownloadRS', 'ApplicationPropertyRS', 'ReportRS',
        'AuthServiceRS', 'EbeguUtil'];

    constructor(private zahlungRS: ZahlungRS, private CONSTANTS: any,
                private $state: IStateService, private downloadRS: DownloadRS, private applicationPropertyRS: ApplicationPropertyRS,
                private reportRS: ReportRS, private authServiceRS: AuthServiceRS, private ebeguUtil: EbeguUtil) {
        this.initViewModel();
    }

    public getZahlungsauftragen() {
        return this.zahlungsauftragen;
    }

    private initViewModel() {
        this.updateZahlungsauftrag();
        this.applicationPropertyRS.isZahlungenTestMode().then((response: any) => {
            this.testMode = response;
        });
    }

    private updateZahlungsauftrag() {

        switch (this.authServiceRS.getPrincipal().role) {

            case TSRole.SACHBEARBEITER_INSTITUTION:
            case TSRole.SACHBEARBEITER_TRAEGERSCHAFT: {
                this.zahlungRS.getAllZahlungsauftraegeInstitution().then((response: any) => {
                    this.zahlungsauftragen = angular.copy(response);
                });
                break;
            }
            case TSRole.SUPER_ADMIN:
            case TSRole.ADMIN:
            case TSRole.SACHBEARBEITER_JA: {
                this.zahlungRS.getAllZahlungsauftraege().then((response: any) => {
                    this.zahlungsauftragen = angular.copy(response);
                });
                break;
            }
            default:
                break;
        }
    }

    public gotoZahlung(zahlungsauftrag: TSZahlungsauftrag) {
        this.$state.go('zahlung', {
            zahlungsauftrag: zahlungsauftrag,
            zahlungsauftragId: zahlungsauftrag.id
        });
    }

    public createZahlungsauftrag() {
        if (this.form.$valid) {
            this.zahlungRS.createZahlungsauftrag(this.beschrieb, this.faelligkeitsdatum, this.datumGeneriert).then((response: TSZahlungsauftrag) => {
                this.zahlungsauftragen.push(response);
                this.resetEditZahlungsauftrag();
            });
        }
    }

    public downloadPain(zahlungsauftrag: TSZahlungsauftrag) {
        let win: Window = this.downloadRS.prepareDownloadWindow();
        return this.downloadRS.getPain001AccessTokenGeneratedDokument(zahlungsauftrag.id)
            .then((downloadFile: TSDownloadFile) => {
                this.downloadRS.startDownload(downloadFile.accessToken, downloadFile.filename, true, win);
            });
    }

    public downloadAllDetails(zahlungsauftrag: TSZahlungsauftrag) {
        let win: Window = this.downloadRS.prepareDownloadWindow();
        this.reportRS.getZahlungsauftragReportExcel(zahlungsauftrag.id)
            .then((downloadFile: TSDownloadFile) => {
                this.downloadRS.startDownload(downloadFile.accessToken, downloadFile.filename, false, win);
            });
    }

    public ausloesen(zahlungsauftragId: string) {
        this.zahlungRS.zahlungsauftragAusloesen(zahlungsauftragId).then((response: TSZahlungsauftrag) => {
            let index = EbeguUtil.getIndexOfElementwithID(response, this.zahlungsauftragen);
            if (index > -1) {
                this.zahlungsauftragen[index] = response;
            }
            this.ebeguUtil.handleSmarttablesUpdateBug(this.zahlungsauftragen);
        });
    }

    public edit(zahlungsauftrag: TSZahlungsauftrag) {
        this.zahlungsauftragToEdit = zahlungsauftrag;
    }

    public save(zahlungsauftrag: TSZahlungsauftrag) {
        if (this.isEditValid()) {
            this.zahlungRS.updateZahlungsauftrag(
                this.zahlungsauftragToEdit.beschrieb, this.zahlungsauftragToEdit.datumFaellig, this.zahlungsauftragToEdit.id).then((response: TSZahlungsauftrag) => {
                let index = EbeguUtil.getIndexOfElementwithID(response, this.zahlungsauftragen);
                if (index > -1) {
                    this.zahlungsauftragen[index] = response;
                }
                this.form.$setPristine(); // nach dem es gespeichert wird, muessen wir das Form wieder auf clean setzen
                this.resetEditZahlungsauftrag();
            });

        }
    }

    public isEditable(status: TSZahlungsauftragsstatus): boolean {
        if (status === TSZahlungsauftragsstatus.ENTWURF) {
            return true;
        }
        return false;
    }

    public isEditMode(zahlungsauftragId: string): boolean {
        if (this.zahlungsauftragToEdit && this.zahlungsauftragToEdit.id === zahlungsauftragId) {
            return true;
        }
        return false;
    }

    public isEditValid(): boolean {
        if (this.zahlungsauftragToEdit) {
            return this.zahlungsauftragToEdit.beschrieb && this.zahlungsauftragToEdit.beschrieb.length > 0 &&
                this.zahlungsauftragToEdit.datumFaellig !== null && this.zahlungsauftragToEdit.datumFaellig !== undefined;
        }
        return false;
    }


    private resetEditZahlungsauftrag() {
        this.zahlungsauftragToEdit = null;
    }

    public rowClass(zahlungsauftragId: string) {
        if (this.isEditMode(zahlungsauftragId) && !this.isEditValid()) {
            return 'errorrow';
        }
        return '';
    }

}