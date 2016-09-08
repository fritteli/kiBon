import TSAbstractPersonEntity from './TSAbstractPersonEntity';
import {TSGeschlecht} from './enums/TSGeschlecht';
import {TSPensumFachstelle} from './TSPensumFachstelle';
import {TSKinderabzug} from './enums/TSKinderabzug';

export default class TSKind extends TSAbstractPersonEntity {

    private _wohnhaftImGleichenHaushalt: number;
    private _kinderabzug: TSKinderabzug;
    private _familienErgaenzendeBetreuung: boolean;
    private _mutterspracheDeutsch: boolean;
    private _pensumFachstelle: TSPensumFachstelle;

    constructor(vorname?: string, nachname?: string, geburtsdatum?: moment.Moment, geschlecht?: TSGeschlecht,
                wohnhaftImGleichenHaushalt?: number, kinderabzug?: TSKinderabzug, familienErgaenzendeBetreuung?: boolean,
                mutterspracheDeutsch?: boolean, pensumFachstelle?: TSPensumFachstelle) {

        super(vorname, nachname, geburtsdatum, geschlecht);
        this._kinderabzug = kinderabzug;
        this._familienErgaenzendeBetreuung = familienErgaenzendeBetreuung;
        this._mutterspracheDeutsch = mutterspracheDeutsch;
        this._pensumFachstelle = pensumFachstelle;
        this._wohnhaftImGleichenHaushalt = wohnhaftImGleichenHaushalt;
    }


    get wohnhaftImGleichenHaushalt(): number {
        return this._wohnhaftImGleichenHaushalt;
    }

    set wohnhaftImGleichenHaushalt(value: number) {
        this._wohnhaftImGleichenHaushalt = value;
    }

    get kinderabzug(): TSKinderabzug {
        return this._kinderabzug;
    }

    set kinderabzug(value: TSKinderabzug) {
        this._kinderabzug = value;
    }

    get familienErgaenzendeBetreuung(): boolean {
        return this._familienErgaenzendeBetreuung;
    }

    set familienErgaenzendeBetreuung(value: boolean) {
        this._familienErgaenzendeBetreuung = value;
    }

    get mutterspracheDeutsch(): boolean {
        return this._mutterspracheDeutsch;
    }

    set mutterspracheDeutsch(value: boolean) {
        this._mutterspracheDeutsch = value;
    }

    get pensumFachstelle(): TSPensumFachstelle {
        return this._pensumFachstelle;
    }

    set pensumFachstelle(value: TSPensumFachstelle) {
        this._pensumFachstelle = value;
    }
}
