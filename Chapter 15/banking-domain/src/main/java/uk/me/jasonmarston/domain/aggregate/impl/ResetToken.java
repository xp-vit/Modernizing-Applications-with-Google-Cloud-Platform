package uk.me.jasonmarston.domain.aggregate.impl;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.springframework.stereotype.Service;

import uk.me.jasonmarston.domain.factory.aggregate.ResetTokenBuilderFactory;
import uk.me.jasonmarston.domain.value.impl.Token;
import uk.me.jasonmarston.framework.domain.aggregate.AbstractAggregate;
import uk.me.jasonmarston.framework.domain.builder.IBuilder;
import uk.me.jasonmarston.framework.domain.type.impl.EntityId;

@Entity
@Table(name = "RESET_TOKENS")
public class ResetToken extends AbstractAggregate {
	public static class Builder implements IBuilder<ResetToken> {
		private EntityId userId;

		private Builder() {
		}

		@Override
		public ResetToken build() {
			if(userId == null) {
				throw new IllegalArgumentException("A User ID is required");
			}

			final ResetToken resetToken = new ResetToken();
			resetToken.userId = userId;

			return resetToken;
		}

		public Builder forUserId(EntityId userId) {
			this.userId = userId;
			return this;
		}
	}

	@Service
	public static class Factory implements ResetTokenBuilderFactory {
		@Override
		public Builder create() {
			return new Builder();
		}
	}

	private static final long serialVersionUID = 1L;

	@NotNull
	private Token token;

	@AttributeOverride(name="id", column=@Column(name="userId", nullable = false))
	@NotNull
	private EntityId userId;

	@NotNull
	@Column(columnDefinition="TIMESTAMP", nullable = false)
	private ZonedDateTime expiryDate;

	private ResetToken() {
		super();
		this.token = new Token(UUID.randomUUID().toString());
		this.expiryDate = _calculateExpiryDate();
	}

	private ZonedDateTime _calculateExpiryDate() {
		final Instant inOneHour = Instant.now().plus(1, ChronoUnit.HOURS);
		final ZoneId utc = ZoneId.of("UTC");
		return ZonedDateTime.ofInstant(inOneHour, utc);
    }

	public ZonedDateTime getExpiryDate() {
		return expiryDate;
	}

	public Token getToken() {
		return token;
	}

	public EntityId getUserId() {
		return userId;
	}

	public boolean isExpired() {
		final Instant now = Instant.now();
		final ZoneId utc = ZoneId.of("UTC");
		final ZonedDateTime current = ZonedDateTime.ofInstant(now, utc);
		return current.isAfter(expiryDate);
	}
}