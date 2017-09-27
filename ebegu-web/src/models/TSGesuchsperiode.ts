import {TSAbstractDateRangedEntity} from './TSAbstractDateRangedEntity';
import {TSDateRange} from './types/TSDateRange';
import {TSGesuchsperiodeStatus} from './enums/TSGesuchsperiodeStatus';
import * as moment from 'moment';
import DateUtil from '../utils/DateUtil';

export default class TSGesuchsperiode extends TSAbstractDateRangedEntity {

    private _status: TSGesuchsperiodeStatus;
    private _datumFreischaltungTagesschule: moment.Moment;

    constructor(status?: TSGesuchsperiodeStatus, gueltigkeit?: TSDateRange, datumFreischaltungTagesschule?: moment.Moment) {
        super(gueltigkeit);
        this._status = status;
        this._datumFreischaltungTagesschule = datumFreischaltungTagesschule;
    }


    get status(): TSGesuchsperiodeStatus {
        return this._status;
    }

    set status(value: TSGesuchsperiodeStatus) {
        this._status = value;
    }

    get datumFreischaltungTagesschule(): moment.Moment {
        return this._datumFreischaltungTagesschule;
    }

    set datumFreischaltungTagesschule(value: moment.Moment) {
        this._datumFreischaltungTagesschule = value;
    }

    get gesuchsperiodeString(): string {
        if (this.gueltigkeit && this.gueltigkeit.gueltigAb && this.gueltigkeit.gueltigBis) {
            return this.gueltigkeit.gueltigAb.year() + '/'
                + (this.gueltigkeit.gueltigBis.year() - 2000);
        }
        return undefined;
    }

    isTagesschulenAnmeldungKonfiguriert(): boolean {
        return this.hasTagesschulenAnmeldung()
            && this.datumFreischaltungTagesschule.isBefore(this.gueltigkeit.gueltigAb);
    }

    isTageschulenAnmeldungAktiv(): boolean {
        return this.isTagesschulenAnmeldungKonfiguriert() && this.datumFreischaltungTagesschule.isBefore(DateUtil.today());
    }

    hasTagesschulenAnmeldung(): boolean {
        return this._datumFreischaltungTagesschule !== null && this.datumFreischaltungTagesschule !== undefined;
    }

    isEntwurf(): boolean {
        return this.status === TSGesuchsperiodeStatus.ENTWURF;
    }
}
