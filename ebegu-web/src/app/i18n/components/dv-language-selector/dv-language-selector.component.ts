/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import {IComponentOptions, IController} from 'angular';
import {take} from 'rxjs/operators';
import AuthServiceRS from '../../../../authentication/service/AuthServiceRS.rest';
import {TSBrowserLanguage} from '../../../../models/enums/TSBrowserLanguage';
import {LogFactory} from '../../../core/logging/LogFactory';
import {I18nServiceRSRest} from '../../services/i18nServiceRS.rest';
import ITranslateService = angular.translate.ITranslateService;

const LOG = LogFactory.createLog('DvLanguageSelectorComponent');

export class DvLanguageSelectorComponentConfig implements IComponentOptions {
    public transclude = true;
    public template = require('./dv-language-selector.component.html');
    public bindings = {
        hideForLoggedUser: '<',
    };
    public controller = DvLanguageSelectorComponent;
    public controllerAs = 'vm';
}

export class DvLanguageSelectorComponent implements IController {

    public static $inject: ReadonlyArray<string> = [
        'AuthServiceRS',
        '$translate',
        'I18nServiceRSRest',
    ];

    public readonly TSBrowserLanguage = TSBrowserLanguage;
    public readonly hideForLoggedUser: boolean;

    public constructor(
        private readonly authServiceRS: AuthServiceRS,
        private readonly $translate: ITranslateService,
        private readonly i18nServiceRS: I18nServiceRSRest,
    ) {
    }

    public changeLanguage(selectedLanguage: TSBrowserLanguage): void {
        this.i18nServiceRS.changeServerLanguage$(selectedLanguage)
            .pipe(take(1))
            .subscribe(
                () => {
                    this.i18nServiceRS.changeClientLanguage(selectedLanguage, this.$translate);
                    LOG.info('language changed', selectedLanguage);
                },
                err => LOG.error(err)
            );
    }

    public isLanguage(language: TSBrowserLanguage): boolean {
        return this.i18nServiceRS.currentLanguage() === language;
    }

    public hideLanguageSelector(): boolean {
        return this.hideForLoggedUser && !!this.authServiceRS.getPrincipalRole();
    }
}
