import {EbeguWebPosteingang} from '../posteingang.module';
import * as moment from 'moment';
import TSMitteilung from '../../models/TSMitteilung';
import TSUser from '../../models/TSUser';
import TSFall from '../../models/TSFall';
import {TSMitteilungTeilnehmerTyp} from '../../models/enums/TSMitteilungTeilnehmerTyp';
import {TSMitteilungStatus} from '../../models/enums/TSMitteilungStatus';

describe('posteingangFilter', function () {

    let posteingangFilter: any;
    let mitteilungArray: Array<TSMitteilung> = [];
    let mitteilung1: TSMitteilung;
    let mitteilung2: TSMitteilung;
    let mitteilung3: TSMitteilung;
    let mitteilung4: TSMitteilung;
    let mitteilung5: TSMitteilung;

    beforeEach(angular.mock.module(EbeguWebPosteingang.name));

    beforeEach(angular.mock.inject(function ($injector: any) {
        posteingangFilter = $injector.get('$filter')('posteingangFilter');

        let ja1 = new TSUser();
        ja1.nachname = 'Blaser';
        ja1.vorname = 'Kurt';

        let ja2 = new TSUser();
        ja2.nachname = 'Becker';
        ja2.vorname = 'Julian';

        let gesuchsteller1 = new TSUser();
        gesuchsteller1.nachname = 'Berger';
        gesuchsteller1.vorname = 'Michael';
        let fall1 = new TSFall();
        fall1.fallNummer = 112;
        fall1.besitzer = gesuchsteller1;

        let gesuchsteller2 = new TSUser();
        gesuchsteller2.nachname = 'Gerber';
        gesuchsteller2.vorname = 'Emma';
        let fall2 = new TSFall();
        fall2.fallNummer = 108;
        fall2.besitzer = gesuchsteller2;

        let fallNoBesitzer = new TSFall();
        fallNoBesitzer.fallNummer = 1010;
        fallNoBesitzer.besitzer = undefined;

        mitteilungArray = [];

        mitteilung1 = new TSMitteilung(fall1, undefined, TSMitteilungTeilnehmerTyp.GESUCHSTELLER, TSMitteilungTeilnehmerTyp.JUGENDAMT, gesuchsteller1,
            ja1, 'Frage zum IAM', 'Warum ist die Banane krumm?', TSMitteilungStatus.NEU, moment('2016-01-01'));
        mitteilungArray.push(mitteilung1);

        mitteilung2 = new TSMitteilung(fall1, undefined, TSMitteilungTeilnehmerTyp.GESUCHSTELLER, TSMitteilungTeilnehmerTyp.JUGENDAMT, gesuchsteller1,
            ja1, 'Adressänderung', 'Unsere neue Adresse lautet...', TSMitteilungStatus.NEU, moment('2016-02-02'));
        mitteilungArray.push(mitteilung2);

        mitteilung3 = new TSMitteilung(fall2, undefined, TSMitteilungTeilnehmerTyp.GESUCHSTELLER, TSMitteilungTeilnehmerTyp.JUGENDAMT, gesuchsteller2,
            ja2, 'Frage zu Dokumentupload', 'Welche Dokumente kann ich...', TSMitteilungStatus.NEU, moment('2016-03-03'));
        mitteilungArray.push(mitteilung3);

        mitteilung4 = new TSMitteilung(fall2, undefined, TSMitteilungTeilnehmerTyp.GESUCHSTELLER, TSMitteilungTeilnehmerTyp.JUGENDAMT, gesuchsteller2,
            ja2, 'Gesuch freigegeben', 'Was nun?', TSMitteilungStatus.NEU, moment('2016-02-02'));
        mitteilungArray.push(mitteilung4);

        mitteilung5 = new TSMitteilung(fallNoBesitzer, undefined, TSMitteilungTeilnehmerTyp.GESUCHSTELLER, TSMitteilungTeilnehmerTyp.JUGENDAMT, gesuchsteller2,
            ja2, 'Gesuch freigegeben', 'Was nun?', TSMitteilungStatus.NEU, moment('2016-02-02'));
        mitteilungArray.push(mitteilung5);

    }));

    describe('API usage', function () {
        it('should return an array with only the elements with the given Sender', function () {
            expect(posteingangFilter(mitteilungArray, {sender: 'berger'})).toEqual([mitteilung1, mitteilung2]);
            expect(posteingangFilter(mitteilungArray, {sender: 'er'})).toEqual([mitteilung1, mitteilung2, mitteilung3, mitteilung4, mitteilung5]);
            expect(posteingangFilter(mitteilungArray, {sender: ''})).toEqual([mitteilung1, mitteilung2, mitteilung3, mitteilung4, mitteilung5]);
            expect(posteingangFilter(mitteilungArray, {sender: 'rrr'})).toEqual([]); // no familienname with this pattern
        });
        it('should return an array with only the element with the given Fallnummer', function () {
            expect(posteingangFilter(mitteilungArray, {'fall': {'fallNummer': '000'}})).toEqual([mitteilung1, mitteilung2, mitteilung3, mitteilung4]);
            expect(posteingangFilter(mitteilungArray, {'fall': {'fallNummer': '0001'}})).toEqual([mitteilung1, mitteilung2, mitteilung3, mitteilung4]);
            expect(posteingangFilter(mitteilungArray, {'fall': {'fallNummer': '1'}})).toEqual([mitteilung1, mitteilung2, mitteilung3, mitteilung4, mitteilung5]);
            expect(posteingangFilter(mitteilungArray, {'fall': {'fallNummer': '12'}})).toEqual([mitteilung1, mitteilung2]);
        });
        it('should return an array with only the elements with the given Familie (Besitzer)', function () {
            expect(posteingangFilter(mitteilungArray, {'fall': {'besitzer': 'berger'}})).toEqual([mitteilung1, mitteilung2]);
            expect(posteingangFilter(mitteilungArray, {'fall': {'besitzer': 'er'}})).toEqual([mitteilung1, mitteilung2, mitteilung3, mitteilung4]);
            expect(posteingangFilter(mitteilungArray, {'fall': {'besitzer': ''}})).toEqual([mitteilung1, mitteilung2, mitteilung3, mitteilung4, mitteilung5]);
            expect(posteingangFilter(mitteilungArray, {'fall': {'besitzer': 'rrr'}})).toEqual([]);
        });
        it('should return an array with only the elements with the given subject', function () {
            expect(posteingangFilter(mitteilungArray, {subject: 'frage'})).toEqual([mitteilung1, mitteilung3]);
            expect(posteingangFilter(mitteilungArray, {subject: 'Dok'})).toEqual([mitteilung3]);
            expect(posteingangFilter(mitteilungArray, {subject: ''})).toEqual([mitteilung1, mitteilung2, mitteilung3, mitteilung4, mitteilung5]);
            expect(posteingangFilter(mitteilungArray, {subject: 'rrr'})).toEqual([]); // no familienname with this pattern
        });
        it('should return an array with only the elements of the given sentDatum', function () {
            expect(posteingangFilter(mitteilungArray, {sentDatum: '02.02.2016'})).toEqual([mitteilung2, mitteilung4, mitteilung5]);
            expect(posteingangFilter(mitteilungArray, {sentDatum: ''})).toEqual([mitteilung1, mitteilung2, mitteilung3, mitteilung4, mitteilung5]);
            expect(posteingangFilter(mitteilungArray, {sentDatum: '2016-05-05'})).toEqual([]);
        });
        it('should return an array with only the elements of the given verantwortlicher/empfaenger', function () {
            expect(posteingangFilter(mitteilungArray, {empfaenger: 'Julian Becker'})).toEqual([mitteilung3, mitteilung4, mitteilung5]);
            expect(posteingangFilter(mitteilungArray, {empfaenger: 'Blaser'})).toEqual([mitteilung1, mitteilung2]);
            expect(posteingangFilter(mitteilungArray, {empfaenger: 'ser'})).toEqual([mitteilung1, mitteilung2]);
            expect(posteingangFilter(mitteilungArray, {empfaenger: ''})).toEqual([mitteilung1, mitteilung2, mitteilung3, mitteilung4, mitteilung5]);
            expect(posteingangFilter(mitteilungArray, {empfaenger: 'rrr'})).toEqual([]);
        });
    });
});
