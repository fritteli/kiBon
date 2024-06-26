/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.ws.sts;

import ch.dvbern.ebegu.errors.STSZertifikatServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.xml.soap.*;
import javax.xml.transform.TransformerException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Service zum aufrufen des Renewal Service von STS welcher eine SAML Assertion fuer den
 * Batchuser erneuert damit wir damit wieder GERES abfragen koennen
 */
@Dependent
public class RenewalAssertionWebService {

	private static final Logger LOGGER = LoggerFactory.getLogger(RenewalAssertionWebService.class.getSimpleName());

	public static final String METHOD_NAME_RENEW_ASSERTION = "renewAssertion";

	@Inject
	private STSConfigManager stsConfigManager;


	public STSAssertionExtractionResult renewAssertion(SOAPElement assertionElement, String renewalToken) throws STSZertifikatServiceException {
		// Assertion muss erneuert werden
		LOGGER.info("triggering renew of assertion using renewal token");
		try {
			SOAPMessage soapMessage = SAMLAuthenticationUtil.createRenewalSoapMessage(assertionElement, renewalToken);
			URL url = new URL(stsConfigManager.getEbeguSTSRenewalAssertionEndpoint());

			SOAPConnection connection = SOAPConnectionFactory.newInstance().createConnection();
			SOAPMessage response = connection.call(soapMessage, url.toExternalForm());
			maybeLogReceivedMessage(response);
			if (response.getSOAPBody().hasFault()) {
				throw new STSZertifikatServiceException(METHOD_NAME_RENEW_ASSERTION,
					"Could not renew Assertion: " + response.getSOAPBody().getFault().getTextContent());
			}

			return SAMLAuthenticationUtil.extractSamlAssertionFromRenewalResponse(response);

		} catch (SOAPException | MalformedURLException e) {
			LOGGER.error("Could not renew assertion");
			throw new STSZertifikatServiceException(METHOD_NAME_RENEW_ASSERTION, "Error handling soap call for Assertion renewal", e);
		}
	}

	private void maybeLogReceivedMessage(SOAPMessage response) {
		if (LOGGER.isDebugEnabled()) {
			try {
				LOGGER.debug("SOAP Response to renewal Request received the following response:\n {}",SAMLAuthenticationUtil.nodeToString(response.getSOAPPart()) );
			} catch (TransformerException e) {
				LOGGER.debug("Could not log received soap message to console");
			}
		}
	}
}
