/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
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

import {downgradeComponent} from '@angular/upgrade/static';
import 'angular';
import {DvNgNavbar} from '../core/component/dv-ng-navbar/dv-ng-navbar';
import {EbeguWebCore} from '../core/core.module';
import {AbwesenheitViewComponentConfig} from './component/abwesenheitView/abwesenheitView';
import {BetreuungFerieninselViewComponentConfig} from './component/betreuungFerieninselView/betreuungFerieninselView';
import {BetreuungListViewComponentConfig} from './component/betreuungListView/betreuungListView';
import {BetreuungMitteilungViewComponentConfig} from './component/betreuungMitteilungView/betreuungMitteilungView';
import {BetreuungTagesschuleViewComponentConfig} from './component/betreuungTagesschuleView/betreuungTagesschuleView';
import {BetreuungViewComponentConfig} from './component/betreuungView/betreuungView';
import {DokumenteViewComponentConfig} from './component/DokumenteView/dokumenteView';
import {DvFinanzielleSituationRequire} from './component/dv-finanzielle-situation-require/dv-finanzielle-situation-require';
import {EinkommensverschlechterungInfoViewComponentConfig} from './component/einkommensverschlechterungInfoView/einkommensverschlechterungInfoView';
import {EinkommensverschlechterungResultateViewComponentConfig} from './component/einkommensverschlechterungResultateView/einkommensverschlechterungResultateView';
import {EinkommensverschlechterungSteuernViewComponentConfig} from './component/einkommensverschlechterungSteuernView/einkommensverschlechterungSteuernView';
import {EinkommensverschlechterungViewComponentConfig} from './component/einkommensverschlechterungView/einkommensverschlechterungView';
import {ErwerbspensumListViewComponentConfig} from './component/erwerbspensumListView/erwerbspensumListView';
import {ErwerbspensumViewComponentConfig} from './component/erwerbspensumView/erwerbspensumView';
import {FallCreationViewComponentConfig} from './component/fallCreationView/fallCreationView';
import {FamiliensituationViewComponentConfig} from './component/familiensituationView/familiensituationView';
import {FinanzielleSituationResultateViewComponentConfig} from './component/finanzielleSituationResultateView/finanzielleSituationResultateView';
import {FinanzielleSituationStartViewComponentConfig} from './component/finanzielleSituationStartView/finanzielleSituationStartView';
import {FinanzielleSituationViewComponentConfig} from './component/finanzielleSituationView/finanzielleSituationView';
import {FreigabeViewComponentConfig} from './component/freigabeView/freigabeView';
import {GesuchToolbarComponentConfig, GesuchToolbarGesuchstellerComponentConfig} from './component/gesuchToolbar/gesuchToolbar';
import {KinderListViewComponentConfig} from './component/kinderListView/kinderListView';
import {KindViewComponentConfig} from './component/kindView/kindView';
import {KommentarViewComponentConfig} from './component/kommentarView/kommentarView';
import {StammdatenViewComponentConfig} from './component/stammdatenView/stammdatenView';
import {UmzugViewComponentConfig} from './component/umzugView/umzugView';
import {VerfuegenListViewComponentConfig} from './component/verfuegenListView/verfuegenListView';
import {VerfuegenViewComponentConfig} from './component/verfuegenView/verfuegenView';
import './gesuch.module.less';
import {gesuchRun} from './gesuch.route';

export const EbeguWebGesuch =
    angular.module('ebeguWeb.gesuch', [EbeguWebCore.name])
        .run(gesuchRun)
        .component('familiensituationView', new FamiliensituationViewComponentConfig())
        .component('stammdatenView', new StammdatenViewComponentConfig)
        .component('umzugView', new UmzugViewComponentConfig())
        .component('kinderListView', new KinderListViewComponentConfig())
        .component('finanzielleSituationView', new FinanzielleSituationViewComponentConfig())
        .component('finanzielleSituationStartView', new FinanzielleSituationStartViewComponentConfig())
        .component('finanzielleSituationResultateView', new FinanzielleSituationResultateViewComponentConfig())
        .component('dvFinanzielleSituationRequire', new DvFinanzielleSituationRequire())
        .component('kindView', new KindViewComponentConfig())
        .component('betreuungListView', new BetreuungListViewComponentConfig())
        .component('betreuungView', new BetreuungViewComponentConfig())
        .component('betreuungTagesschuleView', new BetreuungTagesschuleViewComponentConfig())
        .component('abwesenheitView', new AbwesenheitViewComponentConfig())
        .component('erwerbspensumListView', new ErwerbspensumListViewComponentConfig())
        .component('erwerbspensumView', new ErwerbspensumViewComponentConfig())
        .component('fallCreationView', new FallCreationViewComponentConfig())
        .component('verfuegenListView', new VerfuegenListViewComponentConfig())
        .component('verfuegenView', new VerfuegenViewComponentConfig())
        .component('gesuchToolbar', new GesuchToolbarComponentConfig())
        .component('gesuchToolbarGesuchsteller', new GesuchToolbarGesuchstellerComponentConfig())
        .component('einkommensverschlechterungInfoView', new EinkommensverschlechterungInfoViewComponentConfig())
        .component('einkommensverschlechterungSteuernView', new EinkommensverschlechterungSteuernViewComponentConfig())
        .component('einkommensverschlechterungView', new EinkommensverschlechterungViewComponentConfig())
        .component('einkommensverschlechterungResultateView', new EinkommensverschlechterungResultateViewComponentConfig())
        .component('freigabeView', new FreigabeViewComponentConfig())
        .component('dokumenteView', new DokumenteViewComponentConfig())
        .component('kommentarView', new KommentarViewComponentConfig())
        .component('betreuungMitteilungView', new BetreuungMitteilungViewComponentConfig())
        .component('betreuungFerieninselView', new BetreuungFerieninselViewComponentConfig())
        .directive(
            'dvNgNavbar',
            downgradeComponent({component: DvNgNavbar}) as angular.IDirectiveFactory
        );

