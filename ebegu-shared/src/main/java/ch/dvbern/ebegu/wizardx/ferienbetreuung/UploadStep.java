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

package ch.dvbern.ebegu.wizardx.ferienbetreuung;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.wizardx.WizardStateEnum;
import ch.dvbern.ebegu.wizardx.WizardStep;
import ch.dvbern.ebegu.wizardx.WizardTyp;

public class UploadStep implements WizardStep<FerienbetreuungWizard> {

	@Override
	public void next(@Nonnull FerienbetreuungWizard wizard) {
		wizard.setStep(new AbschlussStep());
	}

	@Override
	public void prev(@Nonnull FerienbetreuungWizard wizard) {
		wizard.setStep(new KostenEinnahmenStep());
	}

	@Override
	public WizardStateEnum getStatus(@Nonnull FerienbetreuungWizard wizard) {
		return wizard.getFerienbetreuungAngabenContainer().getDokumente() == null
			|| wizard.getFerienbetreuungAngabenContainer().getDokumente().isEmpty() ?
			WizardStateEnum.IN_BEARBEITUNG :
			WizardStateEnum.OK;
	}

	@Override
	public String getWizardStepName() {
		return FerienbetreuungWizardStepsEnum.UPLOAD.name();
	}

	@Override
	public boolean isDisabled(@Nonnull FerienbetreuungWizard wizard) {
		return false;
	}

	@Override
	public WizardTyp getWizardTyp() {
		return WizardTyp.FERIENBETREUUNG;
	}
}
