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

package ch.dvbern.ebegu.api.dtos;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import ch.dvbern.ebegu.util.mandant.MandantIdentifier;

/**
 * DTO fuer Mandanten
 */
@XmlRootElement(name = "mandant")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxMandant extends JaxAbstractDTO {

	private static final long serialVersionUID = -1297019601664134592L;

	@NotNull
	private String name;

	private boolean angebotTS = false;
	private boolean angebotFI = false;
	private MandantIdentifier mandantIdentifier;

	@Nonnull
	public String getName() {
		return name;
	}

	public void setName(@Nonnull String name) {
		this.name = name;
	}

	public boolean isAngebotTS() {
		return angebotTS;
	}

	public void setAngebotTS(boolean angebotTS) {
		this.angebotTS = angebotTS;
	}

	public boolean isAngebotFI() {
		return angebotFI;
	}

	public void setAngebotFI(boolean angebotFI) {
		this.angebotFI = angebotFI;
	}

	public MandantIdentifier getMandantIdentifier() {
		return mandantIdentifier;
	}

	public void setMandantIdentifier(MandantIdentifier mandantIdentifier) {
		this.mandantIdentifier = mandantIdentifier;
	}
}
