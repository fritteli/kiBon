import {IHttpService} from 'angular';
import EbeguRestUtil from '../../utils/EbeguRestUtil';
import TSEinkommensverschlechterungContainer from '../../models/TSEinkommensverschlechterungContainer';
import TSGesuchsteller from '../../models/TSGesuchsteller';
import TSGesuch from '../../models/TSGesuch';
import TSFinanzielleSituationResultateDTO from '../../models/dto/TSFinanzielleSituationResultateDTO';
import IPromise = angular.IPromise;
import ILogService = angular.ILogService;


export default class EinkommensverschlechterungContainerRS {
    serviceURL: string;
    http: IHttpService;
    ebeguRestUtil: EbeguRestUtil;
    log: ILogService;

    static $inject = ['$http', 'REST_API', 'EbeguRestUtil', '$log'];
    /* @ngInject */
    constructor($http: IHttpService, REST_API: string, ebeguRestUtil: EbeguRestUtil, $log: ILogService) {
        this.serviceURL = REST_API + 'einkommensverschlechterungContainer';
        this.http = $http;
        this.ebeguRestUtil = ebeguRestUtil;
        this.log = $log;
    }

    public saveEinkommensverschlechterungContainer(einkommensverschlechterungContainer: TSEinkommensverschlechterungContainer,
                                                   gesuchsteller: TSGesuchsteller): IPromise<TSEinkommensverschlechterungContainer> {
        let returnedEinkommensverschlechterungContainer = {};
        returnedEinkommensverschlechterungContainer =
            this.ebeguRestUtil.einkommensverschlechterungContainerToRestObject(returnedEinkommensverschlechterungContainer, einkommensverschlechterungContainer);
        return this.http.put(this.serviceURL + '/' + gesuchsteller.id, returnedEinkommensverschlechterungContainer, {
            headers: {
                'Content-Type': 'application/json'
            }
        }).then((httpresponse: any) => {
            this.log.debug('PARSING Einkommensverschlechterung Container REST object ', httpresponse.data);
            return this.ebeguRestUtil.parseEinkommensverschlechterungContainer(new TSEinkommensverschlechterungContainer(), httpresponse.data);
        });
    }

    public calculateEinkommensverschlechterung(gesuch: TSGesuch, basisJahrPlus: number): IPromise<TSFinanzielleSituationResultateDTO> {
        let gesuchToSend = {};
        gesuchToSend = this.ebeguRestUtil.gesuchToRestObject(gesuchToSend, gesuch);
        return this.http.post(this.serviceURL + '/calculate' + '/' + basisJahrPlus, gesuchToSend, {
            headers: {
                'Content-Type': 'application/json'
            }
        }).then((httpresponse: any) => {
            this.log.debug('PARSING Einkommensverschlechterung Result  REST object ', httpresponse.data);
            return this.ebeguRestUtil.parseFinanzielleSituationResultate(new TSFinanzielleSituationResultateDTO(), httpresponse.data);
        });
    }

    public findEinkommensverschlechterungContainer(einkommensverschlechterungID: string): IPromise<TSEinkommensverschlechterungContainer> {
        return this.http.get(this.serviceURL + '/' + encodeURIComponent(einkommensverschlechterungID)).then((httpresponse: any) => {
            this.log.debug('PARSING finanzielle Situation  REST object ', httpresponse.data);
            return this.ebeguRestUtil.parseEinkommensverschlechterungContainer(new TSEinkommensverschlechterungContainer(), httpresponse.data);
        });
    }
}
