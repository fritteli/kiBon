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

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import ch.dvbern.ebegu.util.Constants;
import org.hibernate.envers.Audited;

import static ch.dvbern.ebegu.util.Constants.DB_DEFAULT_MAX_LENGTH;

/**
 * Entitaet zum Speichern von Traegerschaft in der Datenbank.
 */
@Audited
@Entity
@Table(
	uniqueConstraints =	@UniqueConstraint(columnNames = "name", name = "UK_Traegerschaft_name")
)
public class Traegerschaft extends AbstractMutableEntity implements Displayable, HasMandant {

	private static final long serialVersionUID = -8403454439884704618L;

	@Size(min = 1, max = DB_DEFAULT_MAX_LENGTH)
	@Column(nullable = false)
	@NotNull
	private String name;

	@NotNull
	@Column(nullable = false)
	private Boolean active = true;

	@Nullable
	@Column(nullable = true)
	@Pattern(regexp = Constants.REGEX_EMAIL, message = "{validator.constraints.email.message}")
	private String email;

	@NotNull
	@OneToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_traegerschaft_mandant_id"))
	private Mandant mandant;

	public Traegerschaft() {
	}

	@Override
	@Nonnull
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	@Nullable
	public String getEmail() {
		return email;
	}

	public void setEmail(@Nullable String email) {
		this.email = email;
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
		if (!(other instanceof Traegerschaft)) {
			return false;
		}
		final Traegerschaft otherTraegerschaft = (Traegerschaft) other;
		return Objects.equals(getName(), otherTraegerschaft.getName());
	}

	@Override
	@NotNull
	public Mandant getMandant() {
		return mandant;
	}

	@Override
	public void setMandant(@NotNull Mandant mandant) {
		this.mandant = mandant;
	}
}
