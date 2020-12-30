import {
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    EventEmitter,
    Input,
    OnChanges,
    OnDestroy,
    OnInit,
    Output,
    SimpleChanges,
    ViewChild,
    ViewEncapsulation,
} from '@angular/core';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {Sort} from '@angular/material/sort';
import {MatTable, MatTableDataSource} from '@angular/material/table';
import {TranslateService} from '@ngx-translate/core';
import * as moment from 'moment';
import {BehaviorSubject, forkJoin, from, Observable, of, Subject} from 'rxjs';
import {map, takeUntil} from 'rxjs/operators';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {GemeindeRS} from '../../../gesuch/service/gemeindeRS.rest';
import {SearchRS} from '../../../gesuch/service/searchRS.rest';
import {getTSAntragStatusValuesByRole, TSAntragStatus} from '../../../models/enums/TSAntragStatus';
import {getNormalizedTSAntragTypValues, TSAntragTyp} from '../../../models/enums/TSAntragTyp';
import {
    getTSBetreuungsangebotTypValuesForMandant,
    TSBetreuungsangebotTyp,
} from '../../../models/enums/TSBetreuungsangebotTyp';
import {TSAntragDTO} from '../../../models/TSAntragDTO';
import {TSAntragSearchresultDTO} from '../../../models/TSAntragSearchresultDTO';
import {TSBenutzerNoDetails} from '../../../models/TSBenutzerNoDetails';
import {TSGemeinde} from '../../../models/TSGemeinde';
import {TSInstitution} from '../../../models/TSInstitution';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {LogFactory} from '../logging/LogFactory';
import {GesuchsperiodeRS} from '../service/gesuchsperiodeRS.rest';
import {InstitutionRS} from '../service/institutionRS.rest';

const LOG = LogFactory.createLog('DVAntragListController');

@Component({
    selector: 'dv-new-antrag-list',
    templateUrl: './new-antrag-list.component.html',
    styleUrls: ['./new-antrag-list.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    // we need this to overwrite angular material styles
    encapsulation: ViewEncapsulation.None,
})
export class NewAntragListComponent implements OnInit, OnDestroy, OnChanges {

    @ViewChild(MatPaginator) public paginator: MatPaginator;
    @ViewChild(MatTable) private readonly table: MatTable<DVAntragListItem>;

    /**
     * Emits when the user clicks on a row
     */
    @Output() public readonly rowClicked: EventEmitter<{ antrag: TSAntragDTO, event: Event }> = new EventEmitter<any>();

    /**
     * Can be one of
     * 'fallNummer',
     * 'gemeinde',
     * 'familienName',
     * 'kinder',
     * 'antragTyp',
     * 'periode',
     * 'aenderungsdatum',
     * 'status',
     * 'dokumenteHochgeladen',
     * 'angebote',
     * 'institutionen',
     * 'verantwortlicheTS',
     * 'verantwortlicheBG',
     *
     * Hides the column in the table
     */
    @Input() public hiddenColumns: string[] = [];

    /**
     * Used to provide other data than the default all faelle. Providing this input disables the provided filter and
     * pagination, meaning that instead of applying filter and pagination, they are emitted via their respective event
     * to enable server-side filtering and pagination. 
     */
    @Input() public data$: Observable<DVAntragListItem[]>;

    /**
     * Emits any time the filter changes
     */
    @Output() public readonly filterChange: EventEmitter<DVAntragListFilter> = new EventEmitter<DVAntragListFilter>();

    /**
     * Emits any time the user clicks on the pagination navigation
     */
    @Output() public readonly paginationEvent: EventEmitter<DVPaginationEvent> = new EventEmitter<DVPaginationEvent>();

    /**
     * The first page the list starts on
     */
    @Input() public page: number = 0;

    /**
     * How many items should be displayed per page
     */
    @Input() public pageSize: any = 20;

    public gesuchsperiodenList: Array<string> = [];
    private allInstitutionen: TSInstitution[];
    public institutionenList$: BehaviorSubject<TSInstitution[]> = new BehaviorSubject<TSInstitution[]>([]);
    public gemeindenList: Array<TSGemeinde> = [];

    private customData: boolean = false;

    public datasource: MatTableDataSource<DVAntragListItem>;
    public filterColumns: string[] = [
        'fallNummer-filter',
        'gemeinde-filter',
        'familienName-filter',
        'kinder-filter',
        'antragTyp-filter',
        'periode-filter',
        'aenderungsdatum-filter',
        'status-filter',
        'dokumenteHochgeladen-filter',
        'angebote-filter',
        'institutionen-filter',
        'verantwortlicheTS-filter',
        'verantwortlicheBG-filter',
    ];
    private allColumns = [
        'fallNummer',
        'gemeinde',
        'familienName',
        'kinder',
        'antragTyp',
        'periode',
        'aenderungsdatum',
        'status',
        'dokumenteHochgeladen',
        'angebote',
        'institutionen',
        'verantwortlicheTS',
        'verantwortlicheBG',
    ];

    public displayedColumns: string[] = this.allColumns;

    private readonly filterPredicate: DVAntragListFilter = {};

    private readonly unsubscribe$ = new Subject<void>();

    public totalItems: number = 0;


    private readonly sort: {
        predicate?: string,
        reverse?: boolean
    } = {};
    public paginationItems: number[];

    public constructor(
        private readonly institutionRS: InstitutionRS,
        private readonly gesuchsperiodeRS: GesuchsperiodeRS,
        private readonly gemeindeRS: GemeindeRS,
        private readonly searchRS: SearchRS,
        private readonly authServiceRS: AuthServiceRS,
        private readonly changeDetectorRef: ChangeDetectorRef,
        private readonly translate: TranslateService,
    ) {
    }

    public ngOnInit(): void {
        this.updateInstitutionenList();
        this.updateGesuchsperiodenList();
        this.updateGemeindenList();
        this.initTable();
    }

    public ngOnChanges(changes: SimpleChanges): void {
        if (changes.hiddenColumns) {
            this.displayedColumns = this.allColumns.filter(column => !this.hiddenColumns.includes(column));
            this.filterColumns = this.displayedColumns.map(column => `${column}-filter`);
        }

        if (changes.data$) {
            this.customData = !!this.data$;
        }
    }

    public updateInstitutionenList(): void {
        this.institutionRS.getInstitutionenReadableForCurrentBenutzer().then(response => {
            this.allInstitutionen = response;
            this.institutionenList$.next(this.allInstitutionen);
        });
    }

    public updateGesuchsperiodenList(): void {
        this.gesuchsperiodeRS.getAllGesuchsperioden().then(response => {
            response.forEach(gesuchsperiode => {
                this.gesuchsperiodenList.push(gesuchsperiode.gesuchsperiodeString);
            });
        });
    }

    private updateGemeindenList(): void {
        this.gemeindeRS.getGemeindenForPrincipal$()
            .pipe(takeUntil(this.unsubscribe$))
            .subscribe(gemeinden => {
                    this.gemeindenList = gemeinden;
                },
                err => LOG.error(err),
            );
    }

    public ngOnDestroy(): void {
        this.unsubscribe$.next();
        this.unsubscribe$.complete();
    }

    private initTable(): void {
        this.datasource = new MatTableDataSource<DVAntragListItem>([]);
        this.loadData();
    }

    private loadData(): void {
        const body = {
            pagination: {
                number: this.pageSize,
                start: this.page * this.pageSize,
            },
            search: {
                predicateObject: this.filterPredicate,
            },
            sort: this.sort,
        };
        const dataToLoad$: Observable<DVAntragListItem[]> = this.data$ ?
            this.data$ :
            from(this.searchRS.searchAntraege(body)).pipe(map((result: TSAntragSearchresultDTO) => {
                this.totalItems = result.totalResultSize;
                return result.antragDTOs.map(antragDto => {
                    return {
                        fallNummer: antragDto.fallNummer,
                        dossierId: antragDto.dossierId,
                        antragId: antragDto.antragId,
                        gemeinde: antragDto.gemeinde,
                        status: antragDto.status,
                        familienName: antragDto.familienName,
                        kinder: antragDto.kinder,
                        antragTyp: antragDto.antragTyp,
                        periode: antragDto.gesuchsperiodeString,
                        aenderungsdatum: antragDto.aenderungsdatum,
                        dokumenteHochgeladen: antragDto.dokumenteHochgeladen,
                        angebote: antragDto.angebote,
                        institutionen: antragDto.institutionen,
                        verantwortlicheTS: antragDto.verantwortlicherTS,
                        verantwortlicheBG: antragDto.verantwortlicherBG,
                        hasBesitzer: antragDto.hasBesitzer,
                    };
                });
            }));

        dataToLoad$.subscribe((result: DVAntragListItem[]) => {
            this.datasource.data = result;
            this.paginationItems = [];
            for (let i = 1; i <= Math.ceil(this.totalItems / this.pageSize); i++) {
                this.paginationItems.push(i);
            }
            console.log(this.paginationItems);
            // TODO: we need this because the angualarJS Service returns an IPromise. Angular does not detect changes in
            //  these since they are not zone-aware. Remove once the service is migrated
            this.changeDetectorRef.markForCheck();
        });
    }

    private applyFilter(): void {
        if (this.customData) {
            this.filterChange.emit(this.filterPredicate);
        } else {
            this.loadData();
        }
    }

    public handlePagination(pageEvent: Partial<PageEvent>): void {
        this.page = pageEvent.pageIndex;
        this.pageSize = pageEvent.pageSize;

        if (this.customData) {
            this.paginationEvent.emit({
                page: this.page,
                pageSize: this.pageSize,
            });
        }
        this.loadData();
    }

    public filterFall(query: string): void {
        this.filterPredicate.fallNummer = query.length > 0 ? query : null;
        this.applyFilter();
    }

    public filterGemeinde(gemeinde: string): void {
        this.filterPredicate.gemeinde = gemeinde;
        this.applyFilter();
    }

    public filterType(type: string): void {
        this.filterPredicate.antragTyp = type;
        this.applyFilter();
    }

    public filterPeriode(periode: string): void {
        this.filterPredicate.gesuchsperiodeString = periode;
        this.applyFilter();
    }

    public filterStatus(state: string): void {
        this.filterPredicate.status = state;
        this.applyFilter();
    }

    public filterDocumentsUploaded(documentsUploaded: boolean): void {
        this.filterPredicate.dokumenteHochgeladen = documentsUploaded;
        this.applyFilter();
    }

    public filterAngebot(angebot: string): void {
        this.filterPredicate.angebote = angebot;
        this.applyFilter();
    }

    public filterVerantwortlicheTS(verantwortliche: TSBenutzerNoDetails): void {
        this.filterPredicate.verantwortlicherTS = verantwortliche ? verantwortliche.getFullName() : null;
        this.applyFilter();
    }

    public filterVerantwortlicheBG(verantwortliche: TSBenutzerNoDetails): void {
        this.filterPredicate.verantwortlicherBG = verantwortliche ? verantwortliche.getFullName() : null;
        this.applyFilter();
    }

    public filterFamilie(query: string): void {
        this.filterPredicate.familienName = query.length > 0 ? query : null;
        this.applyFilter();
    }

    public filterKinder(query: string): void {
        this.filterPredicate.kinder = query.length > 0 ? query : null;
        this.applyFilter();
    }

    public filterGeaendert(query: string): void {
        this.filterPredicate.aenderungsdatum = query.length > 0 ? query : null;
        this.applyFilter();
    }

    public filterInstitution(query: string): void {
        // filter the institutitonen list for the autocomplete
        this.institutionenList$.next(
            query ?
                this.allInstitutionen.filter(institution => institution.name.toLocaleLowerCase()
                    .includes(query.toLocaleLowerCase())) :
                this.allInstitutionen,
        );
        this.filterPredicate.institutionen = query.length > 0 ? query : null;
        this.applyFilter();
    }

    public getAntragTypen(): TSAntragTyp[] {
        return getNormalizedTSAntragTypValues();
    }

    /**
     * Alle TSAntragStatus fuer das Filterdropdown
     */
    public getAntragStatus(): TSAntragStatus[] {
        return getTSAntragStatusValuesByRole(this.authServiceRS.getPrincipalRole());
    }

    /**
     * Alle Betreuungsangebot typen fuer das Filterdropdown
     */
    public getBetreuungsangebotTypen(): TSBetreuungsangebotTyp[] {
        return getTSBetreuungsangebotTypValuesForMandant(this.isTagesschulangebotEnabled());
    }

    private isTagesschulangebotEnabled(): boolean {
        return this.authServiceRS.hasMandantAngebotTS();
    }

    public sortData(sortEvent: Sort): void {
        this.sort.predicate = sortEvent.direction.length > 0 ? sortEvent.active : null;
        this.sort.reverse = sortEvent.direction === 'asc';
        this.loadData();
    }

    private onEditClicked(antrag: TSAntragDTO, event: Event): void {
        this.rowClicked.emit({antrag, event});
    }

    public addZerosToFallnummer(fallNummer: number): string {
        return EbeguUtil.addZerosToFallNummer(fallNummer);
    }

    public createAngeboteString(angebote: string[]): Observable<string> {
        if (!angebote) {
            return of('');
        }
        return forkJoin(angebote.map(angebot => this.translate.get(angebot)))
            .pipe(map(translatedAngebote => translatedAngebote.join(', '),
            ));
    }
}

interface DVAntragListItem {
    fallNummer?: number;
    dossierId?: string;
    antragId?: string;
    gemeinde?: string;
    status?: string;
    familienName?: string;
    kinder?: string[];
    antragTyp?: string;
    periode?: string;
    aenderungsdatum?: moment.Moment;
    dokumenteHochgeladen?: boolean;
    angebote?: TSBetreuungsangebotTyp[];
    institutionen?: string[];
    verantwortlicheTS?: string;
    verantwortlicheBG?: string;

    hasBesitzer?(): boolean;
}

interface DVAntragListFilter {
    fallNummer?: string;
    gemeinde?: string;
    familienName?: string;
    kinder?: string;
    antragTyp?: string;
    gesuchsperiodeString?: string;
    eingangsdatum?: string;
    eingangsdatumSTV?: string;
    aenderungsdatum?: string;
    status?: string;
    dokumenteHochgeladen?: boolean;
    angebote?: string;
    institutionen?: string;
    verantwortlicherTS?: string;
    verantwortlicherBG?: string;
    verantwortlicherGemeinde?: string;
}

interface DVPaginationEvent {
    pageSize: number;
    page: number;
}
