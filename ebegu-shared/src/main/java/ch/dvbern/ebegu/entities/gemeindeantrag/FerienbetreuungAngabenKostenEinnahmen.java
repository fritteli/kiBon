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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.entities.gemeindeantrag;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.Size;

import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.enums.gemeindeantrag.FerienbetreuungFormularStatus;
import ch.dvbern.ebegu.util.Constants;
import org.hibernate.envers.Audited;

@Entity
@Audited
public class FerienbetreuungAngabenKostenEinnahmen extends AbstractEntity {

	private static final long serialVersionUID = -3214159076031094829L;

	@Nullable
	@Column()
	private BigDecimal personalkosten;

	@Nullable
	@Column()
	private BigDecimal personalkostenLeitungAdmin;

	@Nullable
	@Column()
	private BigDecimal sachkosten;

	@Nullable
	@Column()
	private BigDecimal verpflegungskosten;

	@Nullable
	@Column()
	private BigDecimal weitereKosten;

	@Size(max = Constants.DB_TEXTAREA_LENGTH)
	@Nullable
	@Column(length = Constants.DB_TEXTAREA_LENGTH)
	private String bemerkungenKosten;

	@Nullable
	@Column()
	private BigDecimal elterngebuehren;

	@Nullable
	@Column()
	private BigDecimal weitereEinnahmen;

	@Nullable
	@Column()
	private BigDecimal sockelbeitrag;

	@Nullable
	@Column()
	private BigDecimal beitraegeNachAnmeldungen;

	@Nullable
	@Column()
	private BigDecimal vorfinanzierteKantonsbeitraege;

	@Nullable
	@Column()
	private BigDecimal eigenleistungenGemeinde;

	@Nonnull
	@Column()
	@Enumerated(EnumType.STRING)
	private FerienbetreuungFormularStatus status = FerienbetreuungFormularStatus.IN_BEARBEITUNG_GEMEINDE;

	public FerienbetreuungAngabenKostenEinnahmen() {
	}

	public FerienbetreuungAngabenKostenEinnahmen(FerienbetreuungAngabenKostenEinnahmen toCopy) {
		this.personalkosten = toCopy.personalkosten;
		this.personalkostenLeitungAdmin = toCopy.personalkostenLeitungAdmin;
		this.sachkosten = toCopy.sachkosten;
		this.verpflegungskosten = toCopy.verpflegungskosten;
		this.weitereKosten = toCopy.weitereKosten;
		this.bemerkungenKosten = toCopy.bemerkungenKosten;
		this.elterngebuehren = toCopy.elterngebuehren;
		this.weitereEinnahmen = toCopy.weitereEinnahmen;
		this.sockelbeitrag = toCopy.sockelbeitrag;
		this.beitraegeNachAnmeldungen = toCopy.beitraegeNachAnmeldungen;
		this.vorfinanzierteKantonsbeitraege = toCopy.vorfinanzierteKantonsbeitraege;
		this.eigenleistungenGemeinde = toCopy.eigenleistungenGemeinde;
	}

	@Nullable
	public BigDecimal getPersonalkosten() {
		return personalkosten;
	}

	public void setPersonalkosten(@Nullable BigDecimal personalkosten) {
		this.personalkosten = personalkosten;
	}

	@Nullable
	public BigDecimal getPersonalkostenLeitungAdmin() {
		return personalkostenLeitungAdmin;
	}

	public void setPersonalkostenLeitungAdmin(@Nullable BigDecimal personalkostenLeitungAdmin) {
		this.personalkostenLeitungAdmin = personalkostenLeitungAdmin;
	}

	@Nullable
	public BigDecimal getSachkosten() {
		return sachkosten;
	}

	public void setSachkosten(@Nullable BigDecimal sachkosten) {
		this.sachkosten = sachkosten;
	}

	@Nullable
	public BigDecimal getVerpflegungskosten() {
		return verpflegungskosten;
	}

	public void setVerpflegungskosten(@Nullable BigDecimal verpflegungskosten) {
		this.verpflegungskosten = verpflegungskosten;
	}

	@Nullable
	public BigDecimal getWeitereKosten() {
		return weitereKosten;
	}

	public void setWeitereKosten(@Nullable BigDecimal weitereKosten) {
		this.weitereKosten = weitereKosten;
	}

	@Nullable
	public String getBemerkungenKosten() {
		return bemerkungenKosten;
	}

	public void setBemerkungenKosten(@Nullable String bemerkungenKosten) {
		this.bemerkungenKosten = bemerkungenKosten;
	}

	@Nullable
	public BigDecimal getElterngebuehren() {
		return elterngebuehren;
	}

	public void setElterngebuehren(@Nullable BigDecimal elterngebuehren) {
		this.elterngebuehren = elterngebuehren;
	}

	@Nullable
	public BigDecimal getWeitereEinnahmen() {
		return weitereEinnahmen;
	}

	public void setWeitereEinnahmen(@Nullable BigDecimal weitereEinnahmen) {
		this.weitereEinnahmen = weitereEinnahmen;
	}

	@Nullable
	public BigDecimal getSockelbeitrag() {
		return sockelbeitrag;
	}

	public void setSockelbeitrag(@Nullable BigDecimal sockelbeitrag) {
		this.sockelbeitrag = sockelbeitrag;
	}

	@Nullable
	public BigDecimal getBeitraegeNachAnmeldungen() {
		return beitraegeNachAnmeldungen;
	}

	public void setBeitraegeNachAnmeldungen(@Nullable BigDecimal beitraegeNachAnmeldungen) {
		this.beitraegeNachAnmeldungen = beitraegeNachAnmeldungen;
	}

	@Nullable
	public BigDecimal getVorfinanzierteKantonsbeitraege() {
		return vorfinanzierteKantonsbeitraege;
	}

	public void setVorfinanzierteKantonsbeitraege(@Nullable BigDecimal vorfinanzierteKantonsbeitraege) {
		this.vorfinanzierteKantonsbeitraege = vorfinanzierteKantonsbeitraege;
	}

	@Nullable
	public BigDecimal getEigenleistungenGemeinde() {
		return eigenleistungenGemeinde;
	}

	public void setEigenleistungenGemeinde(@Nullable BigDecimal eigenleistungenGemeinde) {
		this.eigenleistungenGemeinde = eigenleistungenGemeinde;
	}

	@Override
	public boolean isSame(AbstractEntity other) {
		return getId().equals(other.getId());
	}

	public boolean isReadyForFreigeben() {
		return checkPropertiesNotNull() && status == FerienbetreuungFormularStatus.ABGESCHLOSSEN;
	}

	public boolean isReadyForAbschluss() {
		return checkPropertiesNotNull();
	}

	private boolean checkPropertiesNotNull() {
		List<Serializable> nonNullObj = Arrays.asList(
			this.personalkosten,
			this.sachkosten,
			this.elterngebuehren,
			this.weitereEinnahmen
		);
		return nonNullObj.stream()
			.noneMatch(Objects::isNull);
	}

	@Nonnull
	public FerienbetreuungFormularStatus getStatus() {
		return status;
	}

	public void setStatus(@Nonnull FerienbetreuungFormularStatus status) {
		this.status = status;
	}

	public boolean isAbgeschlossen() {
		return status == FerienbetreuungFormularStatus.ABGESCHLOSSEN;
	}
}
