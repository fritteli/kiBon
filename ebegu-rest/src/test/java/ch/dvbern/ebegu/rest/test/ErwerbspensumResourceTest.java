package ch.dvbern.ebegu.rest.test;

import ch.dvbern.ebegu.api.converter.JaxBConverter;
import ch.dvbern.ebegu.api.dtos.JaxErwerbspensumContainer;
import ch.dvbern.ebegu.api.dtos.JaxGesuchsteller;
import ch.dvbern.ebegu.api.resource.ErwerbspensumResource;
import ch.dvbern.ebegu.api.resource.GesuchstellerResource;
import ch.dvbern.ebegu.errors.EbeguException;
import ch.dvbern.ebegu.rest.test.util.TestJaxDataUtil;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.EJBException;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

import static ch.dvbern.ebegu.rest.test.AbstractEbeguRestTest.createTestArchive;

/**
 * Testet die Erwerbspensum Resource
 */
@RunWith(Arquillian.class)
@Transactional(TransactionMode.DISABLED)
public class ErwerbspensumResourceTest {

	@Deployment
	public static Archive<?> createDeploymentEnvironment() {
		return createTestArchive();
	}

	@Inject
	private GesuchstellerResource gesuchstellerResource;

	@Inject
	private ErwerbspensumResource erwerbspensumResource;

	@Inject
	private JaxBConverter converter;

	@Test
	public void createGesuchstelelrWithErwerbspensumTest() {
		JaxGesuchsteller testJaxGesuchsteller = TestJaxDataUtil.createTestJaxGesuchstellerWithErwerbsbensum();
		JaxGesuchsteller jaxGesuchsteller = gesuchstellerResource.createGesuchsteller(testJaxGesuchsteller, null, null);
		Assert.assertNotNull(jaxGesuchsteller);

	}

	@Test
	public void createErwerbspensumTest() throws EbeguException {
		JaxGesuchsteller jaxGesuchsteller = TestJaxDataUtil.createTestJaxGesuchsteller();
		JaxGesuchsteller gesuchsteller = gesuchstellerResource.createGesuchsteller(jaxGesuchsteller, null, null);
		Response response = erwerbspensumResource.saveErwerbspensum(converter.toJaxId(gesuchsteller), TestJaxDataUtil.createTestJaxErwerbspensumContainer(), null, null);
		JaxErwerbspensumContainer jaxErwerbspensum = (JaxErwerbspensumContainer) response.getEntity();
		Assert.assertNotNull(jaxErwerbspensum);

	}

	@Test
	public void updateGesuchstellerTest() throws EbeguException {
		JaxGesuchsteller jaxGesuchsteller = TestJaxDataUtil.createTestJaxGesuchsteller();
		JaxGesuchsteller storedGS = gesuchstellerResource.createGesuchsteller(jaxGesuchsteller, null, null);
		Response response = erwerbspensumResource.saveErwerbspensum(converter.toJaxId(storedGS), TestJaxDataUtil.createTestJaxErwerbspensumContainer(), null, null);
		JaxErwerbspensumContainer jaxErwerbspensum = (JaxErwerbspensumContainer) response.getEntity();
		JaxErwerbspensumContainer loadedEwp = erwerbspensumResource.findErwerbspensum(converter.toJaxId(jaxErwerbspensum));
		Assert.assertNotNull(loadedEwp);
		Assert.assertNotEquals(Integer.valueOf(20), loadedEwp.getErwerbspensumGS().getZuschlagsprozent());

		jaxErwerbspensum.getErwerbspensumGS().setZuschlagsprozent(18);
		Response result = erwerbspensumResource.saveErwerbspensum(converter.toJaxId(storedGS), jaxErwerbspensum, null, null);
		JaxErwerbspensumContainer updatedContainer = (JaxErwerbspensumContainer) result.getEntity();
		Assert.assertEquals(Integer.valueOf(18), updatedContainer.getErwerbspensumGS().getZuschlagsprozent());

	}

	@Test
	public void invalidPercentErwerbspensumTest() throws EbeguException {
		JaxGesuchsteller jaxGesuchsteller = TestJaxDataUtil.createTestJaxGesuchsteller();
		JaxGesuchsteller storedGS = gesuchstellerResource.createGesuchsteller(jaxGesuchsteller, null, null);
		Response response = erwerbspensumResource.saveErwerbspensum(converter.toJaxId(storedGS), TestJaxDataUtil.createTestJaxErwerbspensumContainer(), null, null);
		JaxErwerbspensumContainer jaxErwerbspensum = (JaxErwerbspensumContainer) response.getEntity();
		JaxErwerbspensumContainer loadedEwp = erwerbspensumResource.findErwerbspensum(converter.toJaxId(jaxErwerbspensum));
		Assert.assertNotNull(loadedEwp);
		loadedEwp.getErwerbspensumGS().setZuschlagsprozent(50);
		try{
			erwerbspensumResource.saveErwerbspensum(converter.toJaxId(storedGS),loadedEwp,null,null);
			Assert.fail("50% is invalid");
		} catch (EJBException e){
			Assert.assertNotNull(e);
		}

	}

}