package ch.dvbern.ebegu.rechner;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.tets.TestDataUtil;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

/**
 * Tests für FinanzielleSituationUtil
 */
public class FinanzielleSituationUtilTest {

	private static final BigDecimal EINKOMMEN_FINANZIELLE_SITUATION = new BigDecimal("100000");
	private static final BigDecimal EINKOMMEN_EKV_ABGELEHNT = new BigDecimal("80001");
	private static final BigDecimal EINKOMMEN_EKV_ANGENOMMEN = new BigDecimal("80000");
	private static final BigDecimal EINKOMMEN_EKV2_ANGENOMMEN = new BigDecimal("50000");
	private static final BigDecimal EINKOMMEN_EKV2_ABGELEHNT = new BigDecimal("64001");

	@Test
	public void testKeineEinkommensverschlechterung() throws Exception {
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(false);
		Gesuch gesuch = betreuung.extractGesuch();
		ch.dvbern.ebegu.tets.TestDataUtil.setFinanzielleSituation(gesuch, EINKOMMEN_FINANZIELLE_SITUATION);
		TestDataUtil.calculateFinanzDaten(gesuch);

		Assert.assertEquals(EINKOMMEN_FINANZIELLE_SITUATION, gesuch.getFinanzDatenDTO().getMassgebendesEinkommenBasisjahr());
		Assert.assertEquals(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigAb(), gesuch.getFinanzDatenDTO().getDatumVonBasisjahr());

		Assert.assertEquals(BigDecimal.ZERO, gesuch.getFinanzDatenDTO().getMassgebendesEinkommenBasisjahrPlus1());
		Assert.assertNull(gesuch.getFinanzDatenDTO().getDatumVonBasisjahrPlus1());

		Assert.assertEquals(BigDecimal.ZERO, gesuch.getFinanzDatenDTO().getMassgebendesEinkommenBasisjahrPlus2());
		Assert.assertNull(gesuch.getFinanzDatenDTO().getDatumVonBasisjahrPlus2());
	}

	@Test
	public void testEinkommensverschlechterung2016Abgelehnt() {
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(false);
		Gesuch gesuch = betreuung.extractGesuch();
		TestDataUtil.setFinanzielleSituation(gesuch, EINKOMMEN_FINANZIELLE_SITUATION);
		TestDataUtil.setEinkommensverschlechterung(gesuch, EINKOMMEN_EKV_ABGELEHNT, true);
		TestDataUtil.calculateFinanzDaten(gesuch);

		Assert.assertEquals(EINKOMMEN_FINANZIELLE_SITUATION, gesuch.getFinanzDatenDTO().getMassgebendesEinkommenBasisjahr());
		Assert.assertEquals(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigAb(), gesuch.getFinanzDatenDTO().getDatumVonBasisjahr());

		Assert.assertEquals(BigDecimal.ZERO, gesuch.getFinanzDatenDTO().getMassgebendesEinkommenBasisjahrPlus1());
		Assert.assertNull(gesuch.getFinanzDatenDTO().getDatumVonBasisjahrPlus1());

		Assert.assertEquals(BigDecimal.ZERO, gesuch.getFinanzDatenDTO().getMassgebendesEinkommenBasisjahrPlus2());
		Assert.assertNull(gesuch.getFinanzDatenDTO().getDatumVonBasisjahrPlus2());
	}

	@Test
	public void testEinkommensverschlechterung2016Angenommen() {
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(false);
		Gesuch gesuch = betreuung.extractGesuch();
		TestDataUtil.setFinanzielleSituation(gesuch, EINKOMMEN_FINANZIELLE_SITUATION);
		TestDataUtil.setEinkommensverschlechterung(gesuch, EINKOMMEN_EKV_ANGENOMMEN, true);
		TestDataUtil.calculateFinanzDaten(gesuch);

		Assert.assertEquals(EINKOMMEN_FINANZIELLE_SITUATION, gesuch.getFinanzDatenDTO().getMassgebendesEinkommenBasisjahr());
		Assert.assertEquals(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigAb(), gesuch.getFinanzDatenDTO().getDatumVonBasisjahr());

		Assert.assertEquals(EINKOMMEN_EKV_ANGENOMMEN, gesuch.getFinanzDatenDTO().getMassgebendesEinkommenBasisjahrPlus1());
		Assert.assertEquals(TestDataUtil.STICHTAG_EKV_1, gesuch.getFinanzDatenDTO().getDatumVonBasisjahrPlus1());

		Assert.assertEquals(BigDecimal.ZERO, gesuch.getFinanzDatenDTO().getMassgebendesEinkommenBasisjahrPlus2());
		Assert.assertNull(gesuch.getFinanzDatenDTO().getDatumVonBasisjahrPlus2());
	}

	@Test
	public void testEinkommensverschlechterung2016Abgelehnt2017Angenommen() {
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(false);
		Gesuch gesuch = betreuung.extractGesuch();
		TestDataUtil.setFinanzielleSituation(gesuch, EINKOMMEN_FINANZIELLE_SITUATION);
		TestDataUtil.setEinkommensverschlechterung(gesuch, EINKOMMEN_EKV_ABGELEHNT, true);
		TestDataUtil.setEinkommensverschlechterung(gesuch, EINKOMMEN_EKV2_ANGENOMMEN, false);
		TestDataUtil.calculateFinanzDaten(gesuch);

		Assert.assertEquals(EINKOMMEN_FINANZIELLE_SITUATION, gesuch.getFinanzDatenDTO().getMassgebendesEinkommenBasisjahr());
		Assert.assertEquals(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigAb(), gesuch.getFinanzDatenDTO().getDatumVonBasisjahr());

		Assert.assertEquals(BigDecimal.ZERO, gesuch.getFinanzDatenDTO().getMassgebendesEinkommenBasisjahrPlus1());
		Assert.assertNull(gesuch.getFinanzDatenDTO().getDatumVonBasisjahrPlus1());

		Assert.assertEquals(EINKOMMEN_EKV2_ANGENOMMEN, gesuch.getFinanzDatenDTO().getMassgebendesEinkommenBasisjahrPlus2());
		Assert.assertEquals(TestDataUtil.STICHTAG_EKV_2, gesuch.getFinanzDatenDTO().getDatumVonBasisjahrPlus2());
	}

	@Test
	public void testEinkommensverschlechterung2016Angenommen2017Angenommen() {
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(false);
		Gesuch gesuch = betreuung.extractGesuch();
		TestDataUtil.setFinanzielleSituation(gesuch, EINKOMMEN_FINANZIELLE_SITUATION);
		TestDataUtil.setEinkommensverschlechterung(gesuch, EINKOMMEN_EKV_ANGENOMMEN, true);
		TestDataUtil.setEinkommensverschlechterung(gesuch, EINKOMMEN_EKV2_ANGENOMMEN, false);
		TestDataUtil.calculateFinanzDaten(gesuch);

		Assert.assertEquals(EINKOMMEN_FINANZIELLE_SITUATION, gesuch.getFinanzDatenDTO().getMassgebendesEinkommenBasisjahr());
		Assert.assertEquals(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigAb(), gesuch.getFinanzDatenDTO().getDatumVonBasisjahr());

		Assert.assertEquals(EINKOMMEN_EKV_ANGENOMMEN, gesuch.getFinanzDatenDTO().getMassgebendesEinkommenBasisjahrPlus1());
		Assert.assertEquals(TestDataUtil.STICHTAG_EKV_1, gesuch.getFinanzDatenDTO().getDatumVonBasisjahrPlus1());

		Assert.assertEquals(EINKOMMEN_EKV2_ANGENOMMEN, gesuch.getFinanzDatenDTO().getMassgebendesEinkommenBasisjahrPlus2());
		Assert.assertEquals(TestDataUtil.STICHTAG_EKV_2, gesuch.getFinanzDatenDTO().getDatumVonBasisjahrPlus2());
	}

	@Test
	public void testEinkommensverschlechterung2016Abgelehnt2017Abgelehnt() {
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(false);
		Gesuch gesuch = betreuung.extractGesuch();
		TestDataUtil.setFinanzielleSituation(gesuch, EINKOMMEN_FINANZIELLE_SITUATION);
		TestDataUtil.setEinkommensverschlechterung(gesuch, EINKOMMEN_EKV_ABGELEHNT, true);
		TestDataUtil.setEinkommensverschlechterung(gesuch, EINKOMMEN_EKV2_ABGELEHNT, false);
		TestDataUtil.calculateFinanzDaten(gesuch);

		Assert.assertEquals(EINKOMMEN_FINANZIELLE_SITUATION, gesuch.getFinanzDatenDTO().getMassgebendesEinkommenBasisjahr());
		Assert.assertEquals(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigAb(), gesuch.getFinanzDatenDTO().getDatumVonBasisjahr());

		Assert.assertEquals(BigDecimal.ZERO, gesuch.getFinanzDatenDTO().getMassgebendesEinkommenBasisjahrPlus1());
		Assert.assertNull(gesuch.getFinanzDatenDTO().getDatumVonBasisjahrPlus1());

		Assert.assertEquals(BigDecimal.ZERO, gesuch.getFinanzDatenDTO().getMassgebendesEinkommenBasisjahrPlus2());
		Assert.assertNull(gesuch.getFinanzDatenDTO().getDatumVonBasisjahrPlus2());
	}
}
