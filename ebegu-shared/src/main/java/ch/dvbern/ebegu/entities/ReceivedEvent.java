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

package ch.dvbern.ebegu.entities;

import java.time.LocalDateTime;
import java.util.StringJoiner;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;

import com.google.common.base.Objects;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import static ch.dvbern.ebegu.util.Constants.TEN_MB;

@Entity
@Table(indexes = {
	@Index(columnList = "eventId", name = "IX_received_event_event_id"),
	@Index(columnList = "eventKey", name = "IX_received_event_event_key"),
	@Index(columnList = "eventType", name = "IX_received_event_event_type"),
	@Index(columnList = "eventTimestamp", name = "IX_received_event_event_timestamp"),
	@Index(columnList = "success", name = "IX_received_event_success")
})
public class ReceivedEvent extends AbstractEntity {

	private static final long serialVersionUID = 4998440001747583997L;

	@Nonnull
	@Column(nullable = false, updatable = false)
	private final @NotEmpty String eventId;

	@Nonnull
	@Column(nullable = false, updatable = false)
	private final @NotEmpty String eventKey;

	@Nonnull
	@Column(nullable = false, updatable = false)
	private final @NotEmpty String eventType;

	@Nonnull
	@Column(nullable = false, updatable = false)
	private final LocalDateTime eventTimestamp;

	@Nonnull
	@Column(nullable = false, length = TEN_MB) // 10 megabytes
	@Lob
	@Basic(fetch = FetchType.LAZY)
	private final String eventDto;

	@Column(nullable = false, updatable = false)
	private boolean success = false;

	@Nullable
	@Column(updatable = false)
	private String error = null;

	/**
	 * just for JPA
	 */
	@SuppressWarnings("ConstantConditions")
	@SuppressFBWarnings(value = "NP_STORE_INTO_NONNULL_FIELD", justification = "just for JPA")
	protected ReceivedEvent() {
		this.eventId = "";
		this.eventKey = "";
		this.eventType = "";
		this.eventTimestamp = null;
		this.eventDto = null;
	}

	public ReceivedEvent(
		@Nonnull @NotEmpty String eventId,
		@Nonnull @NotEmpty String eventKey,
		@Nonnull @NotEmpty String eventType,
		@Nonnull LocalDateTime eventTimestamp,
		@Nonnull String eventDto) {
		this.eventId = eventId;
		this.eventKey = eventKey;
		this.eventType = eventType;
		this.eventTimestamp = eventTimestamp;
		this.eventDto = eventDto;
	}

	@Override
	public boolean isSame(AbstractEntity other) {
		return this.equals(other);
	}

	@Override
	public boolean equals(@Nullable Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || !getClass().equals(o.getClass())) {
			return false;
		}

		if (!super.equals(o)) {
			return false;
		}

		ReceivedEvent that = (ReceivedEvent) o;

		return Objects.equal(getEventDto(), that.getEventDto()) &&
			Objects.equal(getEventId(), that.getEventId()) &&
			Objects.equal(getEventType(), that.getEventType()) &&
			Objects.equal(getEventTimestamp(), that.getEventTimestamp()) &&
			Objects.equal(getEventKey(), that.getEventKey()) &&
			Objects.equal(isSuccess(), that.isSuccess());
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(
			super.hashCode(),
			getEventId(),
			getEventType(),
			getEventTimestamp(),
			getEventKey(),
			getEventDto(),
			isSuccess());
	}

	@Override
	@Nonnull
	public String toString() {
		return new StringJoiner(", ", ReceivedEvent.class.getSimpleName() + '[', "]")
			.add("eventId='" + eventId + '\'')
			.add("eventKey='" + eventKey + '\'')
			.add("eventType='" + eventType + '\'')
			.add("eventTimestamp=" + eventTimestamp)
			.add("success=" + success)
			.toString();
	}

	@Nonnull
	public String getEventId() {
		return eventId;
	}

	@Nonnull
	public String getEventKey() {
		return eventKey;
	}

	@Nonnull
	public String getEventType() {
		return eventType;
	}

	@Nonnull
	public LocalDateTime getEventTimestamp() {
		return eventTimestamp;
	}

	@Nonnull
	public String getEventDto() {
		return eventDto;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	@Nullable
	public String getError() {
		return error;
	}

	public void setError(@Nullable String error) {
		this.error = error;
	}
}
