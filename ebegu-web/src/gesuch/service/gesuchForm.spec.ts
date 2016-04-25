import EbeguRestUtil from '../../utils/EbeguRestUtil';
import {EbeguWebCore} from '../../core/core.module';
import GesuchRS from './gesuchRS.rest';
import IHttpBackendService = angular.IHttpBackendService;

describe('gesuch', function () {

    let gesuchRS: GesuchRS;
    let $httpBackend: IHttpBackendService;
    let ebeguRestUtil: EbeguRestUtil;
    let REST_API: string;

    beforeEach(angular.mock.module(EbeguWebCore.name));

    beforeEach(angular.mock.inject(function ($injector: any) {
        gesuchRS = $injector.get('GesuchRS');
        $httpBackend = $injector.get('$httpBackend');
        ebeguRestUtil = $injector.get('EbeguRestUtil');
        REST_API = $injector.get('REST_API');
    }));

    describe('Public API', function () {

        it('should include a create() function', function () {
            expect(gesuchRS.create).toBeDefined();
        });

        it('should include a findGesuch() function', function () {
            expect(gesuchRS.findGesuch).toBeDefined();
        });

        it('should include a update() function', function () {
            expect(gesuchRS.update).toBeDefined();
        });
        

    });

    describe('API Usage', function () {

    });
});
