import 'angular';
import './gesuch.module.less';
import {EbeguWebCore} from '../core/core.module';
import {gesuchRun} from './gesuch.route';
import {StammdatenViewComponentConfig} from './component/stammdatenView/stammdatenView';
import {FamiliensituationViewComponentConfig} from './component/familiensituationView/familiensituationView';
import {KinderListViewComponentConfig} from './component/kinderListView/kinderListView';
import {FinanzielleSituationViewComponentConfig} from './component/finanzielleSituationView/finanzielleSituationView';
import {KindViewComponentConfig} from './component/kindView/kindView';
import {ErwerbspensumListViewComponentConfig} from './component/erwerbspensumListView/erwerbspensumListView';
import {ErwerbspensumViewComponentConfig} from './component/erwerbspensumView/erwerbspensumView';
import {FinanzielleSituationStartViewComponentConfig} from './component/finanzielleSituationStartView/finanzielleSituationStartView';
import {FinanzielleSituationResultateViewComponentConfig} from './component/finanzielleSituationResultateView/finanzielleSituationResultateView';

export const EbeguWebGesuch =
    angular.module('ebeguWeb.gesuch', [EbeguWebCore.name])
    .run(gesuchRun)
    .component('familiensituationView', new FamiliensituationViewComponentConfig())
    .component('stammdatenView', new StammdatenViewComponentConfig)
    .component('kinderListView', new KinderListViewComponentConfig())
    .component('finanzielleSituationView', new FinanzielleSituationViewComponentConfig())
    .component('finanzielleSituationStartView', new FinanzielleSituationStartViewComponentConfig())
    .component('finanzielleSituationResultateView', new FinanzielleSituationResultateViewComponentConfig())
    .component('kindView', new KindViewComponentConfig())
    .component('erwerbspensumListView', new ErwerbspensumListViewComponentConfig())
    .component('erwerbspensumView', new ErwerbspensumViewComponentConfig());