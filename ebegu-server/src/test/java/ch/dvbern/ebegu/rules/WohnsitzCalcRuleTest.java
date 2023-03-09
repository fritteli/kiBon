package ch.dvbern.ebegu.rules;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Supplier;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuungspensum;
import ch.dvbern.ebegu.entities.BetreuungspensumContainer;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.GesuchstellerAdresseContainer;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.services.GesuchService;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.MathUtil;
import org.easymock.EasyMock;
import org.easymock.EasyMockRunner;
import org.easymock.TestSubject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(EasyMockRunner.class)
public class WohnsitzCalcRuleTest {

	private final DateRange TEST_PERIODE = new DateRange(TestDataUtil.START_PERIODE, TestDataUtil.START_PERIODE);
	private Supplier<GesuchService> gesuchServiceSupplier;

	@TestSubject
	private WohnsitzCalcRule wohnsitzCalcRule = new WohnsitzCalcRule(TEST_PERIODE, Locale.GERMAN, gesuchServiceSupplier);


	@Before
	public void setUp() {
		GesuchService gesuchService= EasyMock.createMock(GesuchService.class);
		EasyMock.expect(gesuchService.getAllGesuchForFallAndGesuchsperiodeInUnterschiedlichenGemeinden(
				EasyMock.anyObject(), EasyMock.anyObject(), EasyMock.anyObject())).andReturn(populateGesuchsliste());

		EasyMock.replay(gesuchService);
		this.gesuchServiceSupplier = () ->  gesuchService;
		wohnsitzCalcRule = new WohnsitzCalcRule(TEST_PERIODE, Locale.GERMAN, gesuchServiceSupplier);
	}

	@After
	public void tearDown() {
	}


	@Test
	public void testExecuteRule() {
		BGCalculationInput inputData = prepareInputData();
		wohnsitzCalcRule.executeRule(preparePlatz(), inputData);
		assertNotNull(inputData);
	}

	@Test
	public void testExecuteRuleWithDossierservice() {
		BGCalculationInput inputData = prepareInputData();
		inputData.setWohnsitzNichtInGemeindeGS1(true);
		inputData.setPotentielleDoppelBetreuung(true);
		assertTrue(inputData.getParent().getBemerkungenDTOList().isEmpty());
		wohnsitzCalcRule = new WohnsitzCalcRule(TEST_PERIODE, Locale.GERMAN, gesuchServiceSupplier);
		assertNotNull(wohnsitzCalcRule);
		wohnsitzCalcRule.executeRule(preparePlatz(), inputData);
		assertFalse(inputData.getParent().getBemerkungenDTOList().isEmpty());
	}

	@Test
	public void testExecuteRuleWithDossierserviceNichtInGemeindeFalse() {
		BGCalculationInput inputData = prepareInputData();
		inputData.setWohnsitzNichtInGemeindeGS1(false);
		inputData.setPotentielleDoppelBetreuung(true);
		assertTrue(inputData.getParent().getBemerkungenDTOList().isEmpty());
		wohnsitzCalcRule = new WohnsitzCalcRule(TEST_PERIODE, Locale.GERMAN, gesuchServiceSupplier);
		assertNotNull(wohnsitzCalcRule);
		wohnsitzCalcRule.executeRule(preparePlatz(), inputData);
		assertFalse(inputData.getParent().getBemerkungenDTOList().isEmpty());
	}

	@Test
	public void testZuzug() {
		wohnsitzCalcRule= new WohnsitzCalcRule(TEST_PERIODE, Locale.GERMAN, gesuchServiceSupplier);
		Betreuung betreuung = (Betreuung) prepareZweiPlaetzeEinerVerfuegt();
		BGCalculationInput bgCalculationInput = prepareInputData();
		bgCalculationInput.setPotentielleDoppelBetreuung(true);
		assertFalse(bgCalculationInput.isAnspruchSinktDuringMonat());
		wohnsitzCalcRule.executeRule(betreuung, bgCalculationInput);
		assertTrue(bgCalculationInput.isAnspruchSinktDuringMonat());
		assertEquals(bgCalculationInput.getAnspruchspensumProzent(), 0);
	}

	private BGCalculationInput prepareInputData() {
		return new BGCalculationInput(new VerfuegungZeitabschnitt(TEST_PERIODE), RuleValidity.ASIV);
	}

	private AbstractPlatz preparePlatz() {
		AbstractPlatz betreuungMock  = EasyMock.createMock(Betreuung.class);
		Gesuch gesuchMock = EasyMock.createMock(Gesuch.class);
		Dossier dossierMock = EasyMock.createMock(Dossier.class);
		Gemeinde gemeindeMock = EasyMock.createMock(Gemeinde.class);
		Gesuchsperiode gesuchsPeriodeMock = EasyMock.createMock(Gesuchsperiode.class);
		KindContainer kindContainerMock = EasyMock.createMock(KindContainer.class);
		Fall fallMock = EasyMock.createMock(Fall.class);
		Kind kind = EasyMock.createMock(Kind.class);
		InstitutionStammdaten institutionStammdaten = EasyMock.createMock(InstitutionStammdaten.class);
		Institution institution = EasyMock.createMock(Institution.class);

		EasyMock.expect(gemeindeMock.getName()).andReturn("TEST_GEMAINDE_32");
		EasyMock.expect(dossierMock.getGemeinde()).andReturn(gemeindeMock);
		EasyMock.expect(dossierMock.getFall()).andReturn(fallMock);
		EasyMock.expect(fallMock.getFallNummer()).andReturn(5007L);
		EasyMock.expect(gesuchMock.getDossier()).andReturn(dossierMock);
		EasyMock.expect(gesuchMock.getFall()).andReturn(fallMock);
		EasyMock.expect(gesuchMock.getKindContainers()).andReturn(Set.of(kindContainerMock)).anyTimes();
		EasyMock.expect(betreuungMock.extractGesuchsperiode()).andReturn(gesuchsPeriodeMock).anyTimes();
		EasyMock.expect(betreuungMock.extractGesuch()).andReturn(gesuchMock).anyTimes();
		EasyMock.expect(betreuungMock.extractGemeinde()).andReturn(gemeindeMock).anyTimes();
		EasyMock.expect(betreuungMock.getKind()).andReturn(kindContainerMock).anyTimes();
		EasyMock.expect(betreuungMock.getInstitutionStammdaten()).andReturn(institutionStammdaten).anyTimes();
		Verfuegung verfuegung = EasyMock.createMock(Verfuegung.class);
		EasyMock.expect(betreuungMock.getVerfuegung()).andReturn(verfuegung).anyTimes();

		EasyMock.expect(institutionStammdaten.getInstitution()).andReturn(institution).anyTimes();
		EasyMock.expect(institution.getId()).andReturn("veryUniqueID").anyTimes();
		EasyMock.expect(kindContainerMock.getGesuch()).andReturn(gesuchMock);
		EasyMock.expect(kindContainerMock.getKindJA()).andReturn(kind).anyTimes();
		EasyMock.expect(kindContainerMock.getBetreuungen()).andReturn(Set.of((Betreuung) betreuungMock)).anyTimes();
		EasyMock.expect(kind.getNachname()).andReturn("Tester").anyTimes();
		EasyMock.expect(kind.getVorname()).andReturn("hans-ueli").anyTimes();
		EasyMock.expect(kind.getGeburtsdatum()).andReturn(LocalDate.of(2028, 3, 7)).anyTimes();
		EasyMock.replay(verfuegung);
		EasyMock.replay(betreuungMock);
		EasyMock.replay(gesuchMock);
		EasyMock.replay(gemeindeMock);
		EasyMock.replay(dossierMock);
		EasyMock.replay(kindContainerMock);
		EasyMock.replay(fallMock);
		EasyMock.replay(gesuchsPeriodeMock);
		EasyMock.replay(kind);
		EasyMock.replay(institution);
		EasyMock.replay(institutionStammdaten);
		return betreuungMock;
	}

	private AbstractPlatz prepareZweiPlaetzeEinerVerfuegt() {
		AbstractPlatz betreuungMock  = EasyMock.createMock(Betreuung.class);
		AbstractPlatz betreuungMock2  = EasyMock.createMock(Betreuung.class);

		Gesuch gesuchMock = EasyMock.createMock(Gesuch.class);
		Dossier dossierMock = EasyMock.createMock(Dossier.class);
		Gemeinde gemeindeMock = EasyMock.createMock(Gemeinde.class);
		Gesuchsperiode gesuchsPeriodeMock = EasyMock.createMock(Gesuchsperiode.class);
		KindContainer kindContainerMock = EasyMock.createMock(KindContainer.class);
		Fall fallMock = EasyMock.createMock(Fall.class);
		Kind kind = EasyMock.createMock(Kind.class);
		InstitutionStammdaten institutionStammdaten = EasyMock.createMock(InstitutionStammdaten.class);
		Institution institution = EasyMock.createMock(Institution.class);
		Verfuegung verfuegungBetreuung1Mock = EasyMock.createMock(Verfuegung.class);

		EasyMock.expect(gemeindeMock.getName()).andReturn("TEST_GEMAINDE_32");
		EasyMock.expect(dossierMock.getGemeinde()).andReturn(gemeindeMock);
		EasyMock.expect(dossierMock.getFall()).andReturn(fallMock);
		EasyMock.expect(fallMock.getFallNummer()).andReturn(5007L);
		EasyMock.expect(gesuchMock.getDossier()).andReturn(dossierMock);
		EasyMock.expect(gesuchMock.getFall()).andReturn(fallMock);
		EasyMock.expect(betreuungMock.extractGesuchsperiode()).andReturn(gesuchsPeriodeMock).anyTimes();
		EasyMock.expect(betreuungMock.extractGesuch()).andReturn(gesuchMock).anyTimes();
		EasyMock.expect(betreuungMock.extractGemeinde()).andReturn(gemeindeMock).anyTimes();
		EasyMock.expect(betreuungMock.getKind()).andReturn(kindContainerMock).anyTimes();
		EasyMock.expect(betreuungMock.getInstitutionStammdaten()).andReturn(institutionStammdaten).anyTimes();
		EasyMock.expect(betreuungMock.getVerfuegung()).andReturn(verfuegungBetreuung1Mock).anyTimes();

		EasyMock.expect(betreuungMock2.extractGesuchsperiode()).andReturn(gesuchsPeriodeMock).anyTimes();
		EasyMock.expect(betreuungMock2.extractGesuch()).andReturn(gesuchMock).anyTimes();
		EasyMock.expect(betreuungMock2.extractGemeinde()).andReturn(gemeindeMock).anyTimes();
		EasyMock.expect(betreuungMock2.getKind()).andReturn(kindContainerMock).anyTimes();
		EasyMock.expect(betreuungMock2.getInstitutionStammdaten()).andReturn(institutionStammdaten).anyTimes();
		EasyMock.expect(betreuungMock2.getVerfuegung()).andReturn(null);

		EasyMock.expect(institutionStammdaten.getInstitution()).andReturn(institution).anyTimes();
		EasyMock.expect(institution.getId()).andReturn("veryUniqueID").anyTimes();
		EasyMock.expect(kindContainerMock.getGesuch()).andReturn(gesuchMock);
		EasyMock.expect(kindContainerMock.getKindJA()).andReturn(kind).anyTimes();
		EasyMock.expect(kind.getNachname()).andReturn("Tester").anyTimes();
		EasyMock.expect(kind.getVorname()).andReturn("hans-ueli").anyTimes();
		EasyMock.expect(kind.getGeburtsdatum()).andReturn(LocalDate.of(2028, 3, 7)).anyTimes();

		EasyMock.replay(verfuegungBetreuung1Mock);
		EasyMock.replay(betreuungMock);
		EasyMock.replay(betreuungMock2);
		EasyMock.replay(gesuchMock);
		EasyMock.replay(gemeindeMock);
		EasyMock.replay(dossierMock);
		EasyMock.replay(kindContainerMock);
		EasyMock.replay(fallMock);
		EasyMock.replay(gesuchsPeriodeMock);
		EasyMock.replay(kind);
		EasyMock.replay(institution);
		EasyMock.replay(institutionStammdaten);
		return betreuungMock;
	}

	private List<Gesuch> populateGesuchsliste() {
		Betreuung betreuung = (Betreuung) preparePlatz();

		List<Gesuch>  gesuchListe = new LinkedList<>();
		gesuchListe.add(betreuung.extractGesuch());

		return gesuchListe;
	}

}
