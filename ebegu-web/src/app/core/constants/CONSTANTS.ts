/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2018 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

import {KiBonMandant} from './MANDANTS';

export const CONSTANTS = {
    name: 'EBEGU',
    REST_API: '/ebegu/api/v1/',
    MAX_LENGTH: 255,
    FALLNUMMER_LENGTH: 6,
    GEMEINDENUMMER_LENGTH: 3,
    ID_LENGTH: 36,
    PATTERN_ANY_NUMBER: /-?[0-9]+(\.[0-9]+)?/,
    PATTERN_ANY_INT: '^[0-9]+$',
    PATTERN_BETRAG: '([0-9]{0,12})',
    PATTERN_ONE_DECIMALS: '^[0-9]+(\\.[0-9])?$',
    PATTERN_TWO_DECIMALS: '^[0-9]+(\\.[0-9]{1,2})?$',
    PATTERN_PERCENTAGE: '^[0-9][0-9]?$|^100$',
    PATTERN_PHONE: '(0|\\+41|0041)\\s?([\\d]{2})\\s?([\\d]{3})\\s?([\\d]{2})\\s?([\\d]{2})',
    PATTERN_MOBILE: '(0|\\+41|0041)\\s?(74|75|76|77|78|79)\\s?([\\d]{3})\\s?([\\d]{2})\\s?([\\d]{2})',
    PATTERN_EMAIL: '[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}',
    PATTERN_ZEMIS_NUMMER: '(^0?\\d{8}\\.\\d$)|(^0\\d{2}\\.\\d{3}\\.\\d{3}[\\.-]\\d$)',
    PATTERN_HHHMM: '^([0-9]|0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]$',
    PARTS_OF_BETREUUNGSNUMMER: 5,
    END_OF_TIME_STRING: '31.12.9999',
    START_OF_TIME_STRING: '01.01.1000',
    DATE_FORMAT: 'DD.MM.YYYY',
    DATE_TIME_FORMAT: 'DD.MM.YYYY HH:mm',
    EARLIEST_DATE_OF_TS_ANMELDUNG: '2020-08-01',
    BERN_BFS_NUMMER: 351,
    MANDANT_LOCAL_STORAGE_KEY: 'mandant',
};

export const DEFAULT_LOCALE = 'de-CH';
export const LOCALSTORAGE_LANGUAGE_KEY = 'kibonLanguage';
export const HEADER_ACCEPT_LANGUAGE = 'Accept-Language';

// Maximale (upload) Filegrösse ist 10MB
export const MAX_FILE_SIZE = 10485760;

export function getUnknowKitaIdForMandant(mandant: KiBonMandant): string {
    switch (mandant) {
        case KiBonMandant.LU:
            return '00000000-0000-0000-0000-000000000003';
            break;
        case KiBonMandant.SO:
            return '00000000-0000-0000-0000-000000000006';
            break;
        case KiBonMandant.BE:
        default:
            return '00000000-0000-0000-0000-000000000000';
            break;
    }
}

// tslint:disable-next-line:no-identical-functions
export function getUnknowTFOIdForMandant(mandant: KiBonMandant): string {
    switch (mandant) {
        case KiBonMandant.LU:
            return '00000000-0000-0000-0000-000000000004';
            break;
        case KiBonMandant.SO:
            return '00000000-0000-0000-0000-000000000007';
            break;
        case KiBonMandant.BE:
        default:
            return '00000000-0000-0000-0000-000000000001';
            break;
    }
}

// tslint:disable-next-line:no-identical-functions
export function getUnknowTagesschuleIdForMandant(mandant: KiBonMandant): string {
    switch (mandant) {
        case KiBonMandant.LU:
            return '00000000-0000-0000-0000-000000000005';
            break;
        case KiBonMandant.SO:
            return '00000000-0000-0000-0000-000000000008';
            break;
        case KiBonMandant.BE:
        default:
            return '00000000-0000-0000-0000-000000000002';
            break;
    }
}

export const HTTP_ERROR_CODES = {
    BAD_REQUEST: 400,
    UNAUTHORIZED: 401,
    NOT_FOUND: 404,
    FORBIDDEN: 403,
    CONFLICT: 409,
};
