/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.ws.ewk.sts;

import ch.dvbern.ebegu.errors.STSZertifikatServiceException;
import ch.dvbern.ebegu.ws.tools.WSUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.Set;
import java.util.TreeSet;

@Stateless
@LocalBean
public class WSSSecurityGeresAssertionOutboundHandler implements SOAPHandler<SOAPMessageContext> {
	private static final String WSSE_NS_URI =
		"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
	private static final Logger LOGGER =
		LoggerFactory.getLogger(WSSSecurityGeresAssertionOutboundHandler.class.getSimpleName());

	@Inject
	private STSGeresAssertionManagerBean stsAssertionManager;

	@Override
	public boolean handleMessage(SOAPMessageContext context) {
		Boolean outboundProperty =
			(Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		if (outboundProperty) {
			try {
				SOAPEnvelope envelope = context.getMessage().getSOAPPart().getEnvelope();
				SOAPFactory factory = SOAPFactory.newInstance();
				String prefix = "wsse";
				SOAPElement securityElem =
					factory.createElement("Security", prefix, WSSE_NS_URI);

				if (envelope.getHeader() != null) {
					envelope.getHeader().detachNode();
				}

				SOAPHeader header = envelope.addHeader();

				Node assertionNode = stsAssertionManager.getValidSTSAssertionForWebserviceType();

				Node importedAssertionNode = securityElem.getOwnerDocument().importNode(assertionNode, true);
				if (importedAssertionNode.getNodeType() == Node.DOCUMENT_NODE) {
					importedAssertionNode = importedAssertionNode.getFirstChild();
				}
				securityElem.appendChild(importedAssertionNode);

				header.addChildElement(securityElem);

				WSUtil.correctAssertionNodes(header.getElementsByTagName("*"));
			} catch (SOAPException | STSZertifikatServiceException | DOMException e) {
				LOGGER.error(
					"Could not add the Assertion to the SOAP Request. This will probably lead to a Failure when "
						+ "calling the GERES Service",
					e);
			}
		}
		return true;
	}

	@Override
	public Set<QName> getHeaders() {
		return new TreeSet();
	}

	@Override
	public boolean handleFault(SOAPMessageContext context) {
		return false;
	}

	@Override
	public void close(MessageContext context) {
		//
	}
}
