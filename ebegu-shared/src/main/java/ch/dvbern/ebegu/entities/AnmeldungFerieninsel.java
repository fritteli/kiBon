/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.entities;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.AssociationOverride;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.enums.AntragCopyType;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.Eingangsart;
import org.hibernate.envers.Audited;

/**
 * Anmeldung für Ferieninsel
 */
@Audited
@Entity
// Der ForeignKey-Name wird leider nicht richtig generiert, muss von Hand angepasst werden!
@AssociationOverride(name="kind", joinColumns=@JoinColumn(name="kind_id"), foreignKey = @ForeignKey(name = "FK_anmeldung_ferieninsel_kind_id"))
@Table(
	uniqueConstraints =
	@UniqueConstraint(columnNames = { "betreuungNummer", "kind_id" }, name = "UK_anmeldung_ferieninsel_kind_betreuung_nummer")
)
public class AnmeldungFerieninsel extends AbstractAnmeldung {

	private static final long serialVersionUID = -9037857320548372570L;

	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_anmeldung_ferieninsel_institution_stammdaten_id"), nullable = false)
	private InstitutionStammdatenFerieninsel1 institutionStammdaten;

	@Nullable
	@OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_anmeldung_ferieninsel_belegung_ferieninsel_id"), nullable = true)
	private BelegungFerieninsel belegungFerieninsel;


	public AnmeldungFerieninsel() {
	}


	@NotNull
	public InstitutionStammdatenFerieninsel1 getInstitutionStammdaten() {
		return institutionStammdaten;
	}

	public void setInstitutionStammdaten(@NotNull InstitutionStammdatenFerieninsel1 institutionStammdaten) {
		this.institutionStammdaten = institutionStammdaten;
	}

	@Nullable
	public BelegungFerieninsel getBelegungFerieninsel() {
		return belegungFerieninsel;
	}

	public void setBelegungFerieninsel(@Nullable BelegungFerieninsel belegungFerieninsel) {
		this.belegungFerieninsel = belegungFerieninsel;
	}

	@Override
	public boolean isSame(AbstractEntity other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (other == null || !getClass().equals(other.getClass())) {
			return false;
		}
		if (!(other instanceof AnmeldungFerieninsel)) {
			return false;
		}
		final AnmeldungFerieninsel otherBetreuung = (AnmeldungFerieninsel) other;
		return this.getInstitutionStammdaten().isSame(otherBetreuung.getInstitutionStammdaten());
	}

	@Nonnull
	public AnmeldungFerieninsel copyAnmeldungFerieninsel(
		@Nonnull AnmeldungFerieninsel target,
		@Nonnull AntragCopyType copyType,
		@Nonnull KindContainer targetKindContainer,
		@Nonnull Eingangsart targetEingangsart
	) {
		super.copyAbstractAnmeldung(target, copyType, targetKindContainer, targetEingangsart);
		switch (copyType) {
		case MUTATION:
			target.setInstitutionStammdaten(this.getInstitutionStammdaten());
			if (belegungFerieninsel != null) {
				target.setBelegungFerieninsel(belegungFerieninsel.copyBelegungFerieninsel(new BelegungFerieninsel(), copyType));
			}
			break;
		case ERNEUERUNG:
		case MUTATION_NEUES_DOSSIER:
		case ERNEUERUNG_NEUES_DOSSIER:
			break;
		}
		return target;
	}

	// Funktion zum Kopieren von Tagesschule und Ferieninsel Angebote
	public void copyAnmeldung(@Nonnull AnmeldungFerieninsel betreuung) {
		if (this.getAnmeldestatus() != betreuung.getAnmeldestatus()) {
			this.setAnmeldestatus(betreuung.getAnmeldestatus());
			this.setInstitutionStammdaten(betreuung.getInstitutionStammdaten());
			if (betreuung.getBelegungFerieninsel() != null) {
				this.setBelegungFerieninsel(betreuung.getBelegungFerieninsel().copyBelegungFerieninsel(new BelegungFerieninsel(), AntragCopyType.MUTATION));
			}
		}
	}

	@Nonnull
	@Override
	@Transient
	public BetreuungsangebotTyp getBetreuungsangebotTyp() {
		return getInstitutionStammdaten().getBetreuungsangebotTyp();
	}
}
