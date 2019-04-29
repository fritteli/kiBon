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

import {LogLevel} from '../app/core/logging/log-level';
import {Environment} from './IEnvironment';

// tslint:disable-next-line:naming-convention
export const environment: Environment = {
    production: true,
    test: false,
    hmr: false,
    logLevel: LogLevel.INFO,
    sentryDSN: 'https://fd0b368a6cee4b879a0ed06e66444c17@sentry.dvbern.ch/11'
};