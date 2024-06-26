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

package ch.dvbern.ebegu.services;

import java.time.LocalDate;
import java.time.Month;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.jboss.ejb3.annotation.TransactionTimeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service fuer Batch-Jobs.
 * All services must be called in its own transaction. The reason is that the EntityManager seems to flush the
 * queries after the method is finished, which means that the Context no longer exists and the Principalbean cannot
 * be found, this implies a rollback of the Transaction and everything gets undone. Executing the service within a
 * transaction flushes the queries before the method finishes and the context still exists.
 */
@Stateless
@Local(DailyBatch.class)
public class DailyBatchBean implements DailyBatch {

	private static final Logger LOGGER = LoggerFactory.getLogger(DailyBatchBean.class);

	@Inject
	private DownloadFileService downloadFileService;

	@Inject
	private MahnungService mahnungService;

	@Inject
	private Persistence persistence;

	@Inject
	private GesuchService gesuchService;

	@Inject
	private GesuchsperiodeService gesuchsperiodeService;

	@Inject
	private WorkjobService workjobService;

	@Inject
	private BenutzerService benutzerService;

	@Inject
	private AuthService authService;

	@Inject
	private BetreuungService betreuungService;

	@Inject
	private InstitutionService institutionService;

	@Inject
	private InstitutionStammdatenService institutionStammdatenService;

	@Inject
	private GesuchsperiodeEmailService gesuchsperiodeEmailService;

	@Inject
	private MandantService mandantService;

	@Override
	@Asynchronous
	public Future<Boolean> runBatchCleanDownloadFiles() {
		try {
			LOGGER.info("Starting Job Cleanup Download-Files...");
			downloadFileService.cleanUp();
			// Hier hat's evtl. einen Bug im Wildfly 10, koennte aber auch korrekt sein:
			// Ohne dieses explizite Flush wird der EM erst so spaet geflusht,
			// dass der Request-Scope nicht mehr aktiv ist und somit das @RequestScoped PrincipalBean fuer die validierung
			// vom Mandanten nicht mehr zur Verfuegung steht.
			persistence.getEntityManager().flush();
			LOGGER.info("... Job Cleanup Download-Files finished");
			return new AsyncResult<>(Boolean.TRUE);
		} catch (RuntimeException e) {
			LOGGER.error("Batch-Job Cleanup Download-Files konnte nicht durchgefuehrt werden!", e);
			return new AsyncResult<>(Boolean.FALSE);
		}
	}

	@Override
	public void runBatchCleanWorkjobs() {
		try {
			LOGGER.info("Starting Job Cleanup Old Workjobs...");
			workjobService.removeOldWorkjobs();
			LOGGER.info("... Job Cleanup Old Workjobs finished");
		} catch (RuntimeException e) {
			LOGGER.error("Batch- Job Cleanup Old Workjobs konnte nicht durchgefuehrt werden!", e);
		}
	}

	@Override
	@Asynchronous
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public Future<Boolean> runBatchMahnungFristablauf() {
		try {
			LOGGER.info("Starting Job Fristablauf...");
			mahnungService.fristAblaufTimer();
			AsyncResult<Boolean> booleanAsyncResult = new AsyncResult<>(Boolean.TRUE);
			// Hier hat's evtl. einen Bug im Wildfly 10, koennte aber auch korrekt sein:
			// Ohne dieses explizite Flush wird der EM erst so spaet geflusht,
			// dass der Request-Scope nicht mehr aktiv ist und somit das @RequestScoped PrincipalBean fuer die validierung
			// vom Mandanten nicht mehr zur Verfuegung steht.
			persistence.getEntityManager().flush();
			LOGGER.info("... Job Fristablauf finished");
			return booleanAsyncResult;
		} catch (RuntimeException e) {
			LOGGER.error("Batch-Job Fristablauf konnte nicht durchgefuehrt werden!", e);
			return new AsyncResult<>(Boolean.FALSE);
		}
	}

	@Override
	@Asynchronous
	public void runBatchWarnungGesuchNichtFreigegeben() {
		try {
			LOGGER.info("Starting Job WarnungGesuchNichtFreigegeben...");
			mandantService.getAll().forEach(mandant -> {
				LOGGER.info("Gesuche für Mandant {}", mandant.getName());
				final int anzahl = gesuchService.findGesucheNichtFreigegebenAndWarn(mandant);
				LOGGER.info("Es wurden {} Gesuche gefunden, die noch nicht freigegeben wurden", anzahl);
			});
			LOGGER.info("... Job WarnungGesuchNichtFreigegeben finished");
		} catch (RuntimeException e) {
			LOGGER.error("Batch-Job WarnungGesuchNichtFreigegeben konnte nicht durchgefuehrt werden!", e);
		}
	}

	@Override
	@Asynchronous
	public void runBatchWarnungFreigabequittungFehlt() {
		try {
			LOGGER.info("Starting Job WarnungFreigabequittungFehlt...");
			mandantService.getAll().forEach(mandant -> {
				LOGGER.info("Gesuche für Mandant {}", mandant.getName());
				final int anzahl = gesuchService.findGesucheWithoutFreigabequittungenAndWarn(mandant);
				LOGGER.info("Es wurden {} Gesuche für {}, gefunden, bei denen die Freigabequittung fehlt", anzahl, mandant.getName());
			});
			LOGGER.info("... Job WarnungFreigabequittungFehlt finished");
		} catch (RuntimeException e) {
			LOGGER.error("Batch-Job WarnungFreigabequittungFehlt konnte nicht durchgefuehrt werden!", e);
		}
	}

	@Override
	@Asynchronous
	public void runBatchGesucheLoeschen() {
		try {
			LOGGER.info("Starting Job GesucheLoeschen...");
			mandantService.getAll().forEach(mandant -> {
				LOGGER.info("Gesuche für Mandant {}", mandant.getName());
				final int anzahl = gesuchService.deleteGesucheOhneFreigabeOderQuittung(mandant);
				LOGGER.info("Es wurden {} Gesuche ohne Freigabe oder Quittung gefunden, die geloescht werden muessen", anzahl);
			});
			LOGGER.info("... Job GesucheLoeschen finished");
		} catch (RuntimeException e) {
			LOGGER.error("Batch-Job GesucheLoeschen konnte nicht durchgefuehrt werden!", e);
		}
	}

	@Override
	public void runBatchGesuchsperiodeLoeschen() {
		try {
			LOGGER.info("Starting Job GesuchsperiodeLoeschen...");
			LocalDate stichtag = LocalDate.now().minusYears(10);
			LOGGER.info("... Deleting Gesuchsperioden older than {}", Constants.DATE_FORMATTER.format(stichtag));
			Collection<Gesuchsperiode> gesuchsperiodenBetween = gesuchsperiodeService.getGesuchsperiodenBetween(LocalDate.of(1900, Month.JANUARY, 1), stichtag);
			for (Gesuchsperiode gesuchsperiode : gesuchsperiodenBetween) {
				gesuchsperiodeService.removeGesuchsperiode(gesuchsperiode.getId());
			}
			LOGGER.info("... Job GesuchsperiodeLoeschen finished");
		} catch (RuntimeException e) {
			LOGGER.error("Batch-Job GesuchsperiodeLoeschen konnte nicht durchgefuehrt werden!", e);
		}
	}

	@Override
	public void runBatchAbgelaufeneRollen() {
		try {
			LOGGER.info("Starting Job AbgelaufeneRollen...");
			int abgelaufeneRollen = benutzerService.handleAbgelaufeneRollen(LocalDate.now());
			LOGGER.info("... Job AbgelaufeneRollen finished. Es wurden {} Benutzer zurückgesetzt", abgelaufeneRollen);
		} catch (RuntimeException e) {
			LOGGER.error("Batch-Job AbgelaufeneRollen konnte nicht durchgefuehrt werden!", e);
		}
	}

	@Override
	public void runBatchDeleteInvalidAuthTokens() {
		try {
			LOGGER.info("Starting Job DeleteInvalidAuthTokens...");
			int deletedTokens = authService.deleteInvalidAuthTokens();
			LOGGER.info("... Job DeleteInvalidAuthTokens finished. Es wurden {} Tokens gelöscht", deletedTokens);
		} catch (RuntimeException e) {
			LOGGER.error("Batch-Job DeleteInvalidAuthTokens konnte nicht durchgefuehrt werden!", e);
		}
	}

	@Override
	public void runBatchInfoOffenePendenzenNeueMitteilungInstitution() {
		try {
			LOGGER.info("Starting Job InfoOffenePendenzenNeueMitteilungInstitution...");
			betreuungService.sendInfoOffenePendenzenNeuMitteilungInstitution();
			LOGGER.info("... Job InfoOffenePendenzenNeueMitteilungInstitution finished");
		} catch (RuntimeException e) {
			LOGGER.error("Batch-Job InfoOffenePendenzenNeueMitteilungInstitution konnte nicht durchgefuehrt werden!", e);
		}
	}

	@Override
	public void deleteInstitutionKennzahlenFields() {
		try {
			LOGGER.info("Starting Job DeleteInstitutionKennzahlenFields...");
			institutionStammdatenService.deleteInstitutionKennzahlenFields();
			LOGGER.info("... Job DeleteInstitutionKennzahlenFields finished");
		} catch (RuntimeException e) {
			LOGGER.error("Batch-Job DeleteInstitutionKennzahlenFields konnte nicht durchgefuehrt werden!", e);
		}
	}


	@Override
	public void runBatchInstitutionCheckRequired() {
		try {
			LOGGER.info("Starting Job InstitutionCheckRequired...");
			institutionService.updateAllStammdatenCheckRequired();
			LOGGER.info("... Job InstitutionCheckRequired finished");
		} catch (RuntimeException e) {
			LOGGER.error("Batch-Job InstitutionCheckRequired konnte nicht durchgefuehrt werden!", e);
		}
	}

	@Override
	@Asynchronous
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@TransactionTimeout(unit = TimeUnit.HOURS, value = 1)
	public Future<Integer> runBatchUpdateGemeindeForBGInstitutionen() {
		try {
			LOGGER.info("Starting Job UpdateGemeindeForBGInstitutionen...");
			Set<InstitutionStammdaten> changed = institutionStammdatenService.updateGemeindeForBGInstitutionen();
			LOGGER.info("... Job UpdateGemeindeForBGInstitutionen finished");

			if (!changed.isEmpty()) {
				LOGGER.info("Starting InstitutionChangedEvent export...");
				changed.forEach(s -> institutionStammdatenService.fireStammdatenChangedEvent(s));
			}

			return new AsyncResult<>(changed.size());
		} catch (RuntimeException e) {
			LOGGER.error("Batch-Job UpdateGemeindeForBGInstitutionen konnte nicht durchgefuehrt werden!", e);
			return new AsyncResult<>(-1);
		}
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@TransactionTimeout(unit = TimeUnit.HOURS, value = 1)
	public void runBatchSendEmailsForNewGesuchsperiode() {
		try {
			LOGGER.info("Starting Job SendEmailsForNewGesuchsperiode...");
			gesuchsperiodeEmailService.sendMailsForNCandidates(200);
			LOGGER.info("... Job SendEmailsForNewGesuchsperiode finished");
		} catch (RuntimeException e) {
			LOGGER.error("Batch-Job SendEmailsForNewGesuchsperiode konnte nicht durchgefuehrt werden!", e);
		}
	}
}
