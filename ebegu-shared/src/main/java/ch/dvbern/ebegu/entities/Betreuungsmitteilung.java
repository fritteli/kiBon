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

package ch.dvbern.ebegu.entities;

import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.validation.Valid;

import ch.dvbern.ebegu.validators.CheckBetreuungMitteilungZeitraumInGesuchsperiode;
import ch.dvbern.ebegu.validators.CheckBetreuungMitteilungZeitraumInstitutionsStammdatenZeitraum;
import ch.dvbern.ebegu.validators.CheckBetreuungsmitteilung;
import ch.dvbern.ebegu.validators.CheckBetreuungsmitteilungDatesOverlapping;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.hibernate.envers.Audited;

/**
 * Entitaet zum Speichern von Betreuungsmitteilung in der Datenbank.
 */
@CheckBetreuungsmitteilung
@CheckBetreuungsmitteilungDatesOverlapping
@CheckBetreuungMitteilungZeitraumInGesuchsperiode
@CheckBetreuungMitteilungZeitraumInstitutionsStammdatenZeitraum
@Audited
@Entity
public class Betreuungsmitteilung extends Mitteilung {

	private static final long serialVersionUID = 489324250868016126L;

	@Valid
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "betreuungsmitteilung")
	private Set<BetreuungsmitteilungPensum> betreuungspensen = new TreeSet<>();

	private boolean applied;

	@Nullable
	@Transient
	private String errorMessage;

	private boolean betreuungStornieren = false;

	public Set<BetreuungsmitteilungPensum> getBetreuungspensen() {
		return betreuungspensen;
	}

	public void setBetreuungspensen(Set<BetreuungsmitteilungPensum> betreuungspensen) {
		this.betreuungspensen = betreuungspensen;
	}

	public boolean isApplied() {
		return applied;
	}

	public void setApplied(boolean applied) {
		this.applied = applied;
	}

	@Nullable
	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(@Nullable String errorMessage) {
		this.errorMessage = errorMessage;
	}

	@Override
	@SuppressWarnings("PMD.CompareObjectsWithEquals")
	@SuppressFBWarnings("BC_UNCONFIRMED_CAST")
	public boolean isSame(AbstractEntity other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (other == null || !getClass().equals(other.getClass())) {
			return false;
		}
		if (!super.isSame(other)) {
			return false;
		}
		final Betreuungsmitteilung otherBetreuungsmitteilung = (Betreuungsmitteilung) other;
		return isApplied() == otherBetreuungsmitteilung.isApplied();
	}

	public boolean isBetreuungStornieren() {
		return betreuungStornieren;
	}

	public void setBetreuungStornieren(@Nonnull boolean betreuungStornieren) {
		this.betreuungStornieren = betreuungStornieren;
	}
}
