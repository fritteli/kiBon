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
import javax.persistence.Column;
import javax.persistence.Entity;
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
 * Anmeldung für Tagesschule
 */
@Audited
@Entity
@AssociationOverride(name="kind", joinColumns={@JoinColumn(name="ts_kind_id")}, foreignKey = @ForeignKey(name = "FK_anmeldung_tagesschule_kind_id"))
//@AssociationOverrides(
//	@AssociationOverride(name="kind", joinColumns=@JoinColumn(name="kind_id"), foreignKey = @ForeignKey(name = "FK_anmeldung_tagesschule_kind_id"))
//)
@Table(
	uniqueConstraints =
	@UniqueConstraint(columnNames = { "betreuungNummer", "ts_kind_id" }, name = "UK_anmeldung_tagesschule_kind_betreuung_nummer")
)
public class AnmeldungTagesschule extends AbstractAnmeldung {

	private static final long serialVersionUID = -9037857320548372570L;

	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_anmeldung_tagesschule_institution_stammdaten_id"), nullable = false)
	private InstitutionStammdatenTagesschule1 institutionStammdaten;

	@Nullable
	@OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_anmeldung_tagesschule_belegung_tagesschule_id"), nullable = true)
	private BelegungTagesschule belegungTagesschule;

	@Column(nullable = false)
	private boolean keineDetailinformationen = false;


	public AnmeldungTagesschule() {
	}

////	@Override
//	@AssociationOverride(name="kind", joinColumns=@JoinColumn(name="kind_id"), foreignKey = @ForeignKey(name = "FK_anmeldung_tagesschule_kind_id"))
//	@Override
//	@ManyToOne(optional = false)
//	@JoinColumn(foreignKey = @ForeignKey(name = "FK_anmeldung_tagesschule_kind_id"), nullable = false)
//	public KindContainer getKind() {
//		return super.getKind();
//	}

	@NotNull
	public InstitutionStammdatenTagesschule1 getInstitutionStammdaten() {
		return institutionStammdaten;
	}

	public void setInstitutionStammdaten(@NotNull InstitutionStammdatenTagesschule1 institutionStammdaten) {
		this.institutionStammdaten = institutionStammdaten;
	}

	@Nullable
	public BelegungTagesschule getBelegungTagesschule() {
		return belegungTagesschule;
	}

	public void setBelegungTagesschule(@Nullable BelegungTagesschule belegungTagesschule) {
		this.belegungTagesschule = belegungTagesschule;
	}

	public boolean isKeineDetailinformationen() {
		return keineDetailinformationen;
	}

	public void setKeineDetailinformationen(boolean keineDetailinformationen) {
		this.keineDetailinformationen = keineDetailinformationen;
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
		if (!(other instanceof AnmeldungTagesschule)) {
			return false;
		}
		final AnmeldungTagesschule otherBetreuung = (AnmeldungTagesschule) other;
		return this.getInstitutionStammdaten().isSame(otherBetreuung.getInstitutionStammdaten());
	}

	@Nonnull
	public AnmeldungTagesschule copyAnmeldungTagesschule(
		@Nonnull AnmeldungTagesschule target,
		@Nonnull AntragCopyType copyType,
		@Nonnull KindContainer targetKindContainer,
		@Nonnull Eingangsart targetEingangsart
	) {
		super.copyAbstractAnmeldung(target, copyType, targetKindContainer, targetEingangsart);
		switch (copyType) {
		case MUTATION:
			target.setInstitutionStammdaten(this.getInstitutionStammdaten());
			if (belegungTagesschule != null) {
				target.setBelegungTagesschule(belegungTagesschule.copyBelegungTagesschule(new BelegungTagesschule(), copyType));
			}
			target.setKeineDetailinformationen(this.isKeineDetailinformationen());
			break;
		case ERNEUERUNG:
		case MUTATION_NEUES_DOSSIER:
		case ERNEUERUNG_NEUES_DOSSIER:
			break;
		}
		return target;
	}

	// Funktion zum Kopieren von Tagesschule und Ferieninsel Angebote
	public void copyAnmeldung(@Nonnull AnmeldungTagesschule betreuung) {
		if (this.getAnmeldestatus() != betreuung.getAnmeldestatus()) {
			this.setAnmeldestatus(betreuung.getAnmeldestatus());
			this.setInstitutionStammdaten(betreuung.getInstitutionStammdaten());
			if (betreuung.getBelegungTagesschule() != null) {
				this.setBelegungTagesschule(betreuung.getBelegungTagesschule().copyBelegungTagesschule(new BelegungTagesschule(), AntragCopyType.MUTATION));
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
