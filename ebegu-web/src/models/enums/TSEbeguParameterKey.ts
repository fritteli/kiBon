export enum TSEbeguParameterKey {
    PARAM_FIXBETRAG_STADT_PRO_TAG_KITA = <any>'PARAM_FIXBETRAG_STADT_PRO_TAG_KITA',
    PARAM_ANZAL_TAGE_MAX_KITA = <any>'PARAM_ANZAL_TAGE_MAX_KITA',
    PARAM_STUNDEN_PRO_TAG_MAX_KITA = <any>'PARAM_STUNDEN_PRO_TAG_MAX_KITA',
    PARAM_KOSTEN_PRO_STUNDE_MAX = <any>'PARAM_KOSTEN_PRO_STUNDE_MAX',
    PARAM_KOSTEN_PRO_STUNDE_MAX_TAGESELTERN = <any>'PARAM_KOSTEN_PRO_STUNDE_MAX_TAGESELTERN',
    PARAM_KOSTEN_PRO_STUNDE_MIN = <any>'PARAM_KOSTEN_PRO_STUNDE_MIN',
    PARAM_MASSGEBENDES_EINKOMMEN_MIN = <any>'PARAM_MASSGEBENDES_EINKOMMEN_MIN',
    PARAM_MASSGEBENDES_EINKOMMEN_MAX = <any>'PARAM_MASSGEBENDES_EINKOMMEN_MAX',
    PARAM_ANZAHL_TAGE_KANTON = <any>'PARAM_ANZAHL_TAGE_KANTON',
    PARAM_STUNDEN_PRO_TAG_TAGI = <any>'PARAM_STUNDEN_PRO_TAG_TAGI',
    PARAM_PENSUM_TAGI_MIN = <any>'PARAM_PENSUM_TAGI_MIN',
    PARAM_PENSUM_KITA_MIN = <any>'PARAM_PENSUM_KITA_MIN',
    PARAM_PENSUM_TAGESELTERN_MIN = <any>'PARAM_PENSUM_TAGESELTERN_MIN',
    PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3 = <any>'PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3',
    PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4 = <any>'PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4',
    PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5 = <any>'PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5',
    PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6 = <any>'PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6',
    PARAM_MAX_TAGE_ABWESENHEIT = <any>'PARAM_MAX_TAGE_ABWESENHEIT',
    PARAM_ABGELTUNG_PRO_TAG_KANTON = <any>'PARAM_ABGELTUNG_PRO_TAG_KANTON',
    PARAM_MAXIMALER_ZUSCHLAG_ERWERBSPENSUM = <any>'PARAM_MAXIMALER_ZUSCHLAG_ERWERBSPENSUM',
}

export function getTSEbeguParameterKeyValues(): Array<TSEbeguParameterKey> {
    return [
        TSEbeguParameterKey.PARAM_FIXBETRAG_STADT_PRO_TAG_KITA,
        TSEbeguParameterKey.PARAM_ANZAL_TAGE_MAX_KITA,
        TSEbeguParameterKey.PARAM_STUNDEN_PRO_TAG_MAX_KITA,
        TSEbeguParameterKey.PARAM_KOSTEN_PRO_STUNDE_MAX,
        TSEbeguParameterKey.PARAM_KOSTEN_PRO_STUNDE_MAX_TAGESELTERN,
        TSEbeguParameterKey.PARAM_KOSTEN_PRO_STUNDE_MIN,
        TSEbeguParameterKey.PARAM_MASSGEBENDES_EINKOMMEN_MIN,
        TSEbeguParameterKey.PARAM_MASSGEBENDES_EINKOMMEN_MAX,
        TSEbeguParameterKey.PARAM_ANZAHL_TAGE_KANTON,
        TSEbeguParameterKey.PARAM_STUNDEN_PRO_TAG_TAGI,
        TSEbeguParameterKey.PARAM_PENSUM_TAGI_MIN,
        TSEbeguParameterKey.PARAM_PENSUM_KITA_MIN,
        TSEbeguParameterKey.PARAM_PENSUM_TAGESELTERN_MIN,
        TSEbeguParameterKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3,
        TSEbeguParameterKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4,
        TSEbeguParameterKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5,
        TSEbeguParameterKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6,
        TSEbeguParameterKey.PARAM_MAX_TAGE_ABWESENHEIT,
        TSEbeguParameterKey.PARAM_ABGELTUNG_PRO_TAG_KANTON,
        TSEbeguParameterKey.PARAM_MAXIMALER_ZUSCHLAG_ERWERBSPENSUM,
    ];
}
