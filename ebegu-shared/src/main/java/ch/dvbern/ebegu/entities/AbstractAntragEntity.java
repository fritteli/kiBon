package ch.dvbern.ebegu.entities;

import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.AntragTyp;
import ch.dvbern.ebegu.util.Constants;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Abstrakte Entitaet. Muss von Entitaeten erweitert werden, die einen Antrag sind z.B. Gesuch und Mutation
 */
@MappedSuperclass
public class AbstractAntragEntity extends AbstractEntity {

	private static final long serialVersionUID = -8203487739884704615L;

	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_gesuch_fall_id"))
	private Fall fall;

	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_antrag_gesuchsperiode_id"))
	private Gesuchsperiode gesuchsperiode;

	@NotNull //TODO (team) Wegnehmen: Online-Gesuch hat anfangs noch kein Eingangsdatum!
	@Column(nullable = false)
	private LocalDate eingangsdatum;

	@NotNull
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private AntragStatus status;

	@NotNull
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private AntragTyp typ = AntragTyp.GESUCH;

	public Fall getFall() {
		return fall;
	}

	public void setFall(Fall fall) {
		this.fall = fall;
	}

	public Gesuchsperiode getGesuchsperiode() {
		return gesuchsperiode;
	}

	public void setGesuchsperiode(Gesuchsperiode gesuchsperiode) {
		this.gesuchsperiode = gesuchsperiode;
	}

	public LocalDate getEingangsdatum() {
		return eingangsdatum;
	}

	public void setEingangsdatum(@Nonnull LocalDate eingangsdatum) {
		this.eingangsdatum = eingangsdatum;
	}

	public AntragStatus getStatus() {
		return status;
	}

	public void setStatus(AntragStatus status) {
		this.status = status;
	}


	public AntragTyp getTyp() {
		return typ;
	}

	public void setTyp(AntragTyp typ) {
		this.typ = typ;
	}


	@SuppressWarnings("ObjectEquality")
	public boolean isSame(AbstractAntragEntity otherAbstAntragEntity) {
		if (this == otherAbstAntragEntity) {
			return true;
		}
		if (otherAbstAntragEntity == null || getClass() != otherAbstAntragEntity.getClass()) {
			return false;
		}
		return (Objects.equals(this.getEingangsdatum(), otherAbstAntragEntity.getEingangsdatum())
			&& Objects.equals(this.getFall(), otherAbstAntragEntity.getFall())
			&& Objects.equals(this.getGesuchsperiode(), otherAbstAntragEntity.getGesuchsperiode()));
	}

	public String getAntragNummer() {
		return Integer.toString(getGesuchsperiode().getGueltigkeit().getGueltigAb().getYear()).substring(2)
			+ "." + StringUtils.leftPad("" + getFall().getFallNummer(), Constants.FALLNUMMER_LENGTH, '0');
	}
}
