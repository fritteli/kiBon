/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2018 City of Bern Switzerland
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

package ch.dvbern.ebegu.batch.jobs.report;

import java.util.Locale;
import java.util.Objects;
import java.util.Properties;

import javax.batch.api.AbstractBatchlet;
import javax.batch.operations.JobOperator;
import javax.batch.runtime.BatchRuntime;
import javax.batch.runtime.BatchStatus;
import javax.batch.runtime.context.JobContext;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;

import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.enums.WorkJobConstants;
import ch.dvbern.ebegu.services.MailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named("sendEmailBatchlet")
@Dependent
public class SendEmailBatchlet extends AbstractBatchlet {

	private static final Logger LOG = LoggerFactory.getLogger(SendEmailBatchlet.class);

	@Inject
	private MailService mailService;

	@Inject
	private JobContext jobCtx;

	@Inject
	private EbeguConfiguration configuration;

	@Override
	public String process() {
		final String receiverEmail = getParameters().getProperty(WorkJobConstants.EMAIL_OF_USER);
		final String receiverLanguage = getParameters().getProperty(WorkJobConstants.LANGUAGE);
		LOG.debug("Sending mail with file for user to {}", receiverEmail);
		Objects.requireNonNull(receiverEmail, " Email muss gesetzt sein damit der Fertige Report an den Empfaenger geschickt werden kann");
		//	EBEGU-1663 Wildfly 10 hack, this can be removed as soon as WF11 runs, right now we create the download file right at the start and
		// can only change its content
		//		workJobService.addResultToWorkjob(workJob.getId(), downloadFile.getAccessToken());
		try {
			mailService.sendInfoStatistikGeneriert(receiverEmail, createStatistikPageLink(), Locale.forLanguageTag(receiverLanguage));
			return BatchStatus.COMPLETED.toString();
		} catch (Exception ignore) {
			return BatchStatus.FAILED.toString();
		}
	}

	private String createStatistikPageLink() {
		return configuration.getHostname() + "/statistik";
	}

	private Properties getParameters() {
		JobOperator operator = BatchRuntime.getJobOperator();
		return operator.getParameters(jobCtx.getExecutionId());
	}
}
