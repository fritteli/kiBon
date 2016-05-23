import {IComponentOptions} from 'angular';
import {IStateService} from 'angular-ui-router';
import AbstractGesuchViewController from '../abstractGesuchView';
import GesuchModelManager from '../../service/gesuchModelManager';
import TSGesuchsteller from '../../../models/TSGesuchsteller';
import TSErwerbspensumContainer from '../../../models/TSErwerbspensumContainer';
import {DvDialog} from '../../../core/directive/dv-dialog/dv-dialog';
import {KindRemoveDialogController} from '../../dialog/KindRemoveDialogController';
import ILogService = angular.ILogService;
let template = require('./erwerbspensumListView.html');
let removeKindTemplate = require('../../dialog/removeKindDialogTemplate.html');


export class ErwerbspensumListViewComponentConfig implements IComponentOptions {
    transclude: boolean;
    bindings: any;
    template: string | Function;
    controller: any;
    controllerAs: string;


    constructor() {
        this.transclude = false;
        this.bindings = {};
        this.template = template;
        this.controller = ErwerbspensumListViewController;
        this.controllerAs = 'vm';
    }
}


export class ErwerbspensumListViewController extends AbstractGesuchViewController {

    erwerbspensenGS1: Array<TSErwerbspensumContainer> = undefined;
    erwerbspensenGS2: Array<TSErwerbspensumContainer>;

    static $inject: string[] = ['$state', 'GesuchModelManager', '$log', 'DvDialog'];
    /* @ngInject */
    constructor(state: IStateService, gesuchModelManager: GesuchModelManager, private $log: ILogService, private dvDialog: DvDialog) {
        super(state, gesuchModelManager);
        var vm = this;
    }


    getErwerbspensenListGS1(): Array<TSErwerbspensumContainer> {
        if (this.erwerbspensenGS1 === undefined) {
            //todo team, hier die daten vielleicht reingeben statt sie zu lesen
            if (this.gesuchModelManager.gesuch && this.gesuchModelManager.gesuch.gesuchsteller1 &&
                this.gesuchModelManager.gesuch.gesuchsteller1.erwerbspensenContainer) {
                let gesuchsteller1: TSGesuchsteller = this.gesuchModelManager.gesuch.gesuchsteller1;
                this.erwerbspensenGS1 = gesuchsteller1.erwerbspensenContainer;

            } else {
                this.erwerbspensenGS1 = [];
            }
        }
        return this.erwerbspensenGS1;
    }

    getErwerbspensenListGS2(): Array<TSErwerbspensumContainer> {
        if (this.erwerbspensenGS2 === undefined) {
            //todo team, hier die daten vielleicht reingeben statt sie zu lesen
            if (this.gesuchModelManager.gesuch && this.gesuchModelManager.gesuch.gesuchsteller2 &&
                this.gesuchModelManager.gesuch.gesuchsteller2.erwerbspensenContainer) {
                let gesuchsteller2: TSGesuchsteller = this.gesuchModelManager.gesuch.gesuchsteller2;
                this.erwerbspensenGS2 = gesuchsteller2.erwerbspensenContainer;

            } else {
                this.erwerbspensenGS2 = [];
            }
        }
        return this.erwerbspensenGS2;

    }


    createErwerbspensum(gesuchstellerNumber: number): void {
        this.openErwerbspensumView(gesuchstellerNumber, undefined);
    }

    removePensum(pensum: any, gesuchstellerNumber: number): void {
        //todo homa dialog anpassen, sollte generalisiert werden
        this.dvDialog.showDialog(removeKindTemplate, KindRemoveDialogController, {kindName: pensum.prozent})
            .then(() => {   //User confirmed removal
                this.gesuchModelManager.removeErwerbspensum(pensum);

            });

    }

    editPensum(pensum: any, gesuchstellerNumber: any): void {

        let index: number = this.gesuchModelManager.findIndexOfErwerbspensum(parseInt(gesuchstellerNumber), pensum);
        this.openErwerbspensumView(gesuchstellerNumber, index);

    }

    private openErwerbspensumView(gesuchstellerNumber: number, erwerbspensumNum: number): void {
        this.state.go('gesuch.erwerbsPensum', {
            gesuchstellerNumber: gesuchstellerNumber,
            erwerbspensumNum: erwerbspensumNum
        });
    }

    previousStep() {
        //todo team navigation hier auf betreuung
        this.state.go('gesuch.kinder');
    }

    nextStep() {
        this.state.go('gesuch.finanzielleSituation', {gesuchstellerNumber: '1'});
    }
}


