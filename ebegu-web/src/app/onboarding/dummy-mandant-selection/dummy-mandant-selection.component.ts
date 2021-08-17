import {Component, ChangeDetectionStrategy} from '@angular/core';
import {UIRouterGlobals} from '@uirouter/core';
import {MandantService} from '../../shared/services/mandant.service';

@Component({
    selector: 'dv-dummy-mandant-selection',
    templateUrl: './dummy-mandant-selection.component.html',
    styleUrls: ['./dummy-mandant-selection.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DummyMandantSelectionComponent {

    public constructor(
        private mandantService: MandantService,
        private routerGlobals: UIRouterGlobals
    ) {
    }

    public selectMandant(mandant: string): void {
        this.mandantService.selectMandant(mandant, this.routerGlobals.params.path);
    }
}
