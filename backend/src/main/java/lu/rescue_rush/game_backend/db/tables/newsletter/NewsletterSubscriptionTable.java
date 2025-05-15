package lu.rescue_rush.game_backend.db.tables.newsletter;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

import org.springframework.stereotype.Service;

import lu.pcy113.pclib.PCUtils;
import lu.pcy113.pclib.db.DataBase;
import lu.pcy113.pclib.db.TableHelper;
import lu.pcy113.pclib.db.annotations.table.Column;
import lu.pcy113.pclib.db.annotations.table.Constraint;
import lu.pcy113.pclib.db.annotations.table.DB_Table;

import jakarta.annotation.Nonnull;
import lu.rescue_rush.game_backend.data.Language;
import lu.rescue_rush.game_backend.db.data.newsletter.NewsletterSubscriptionData;
import lu.rescue_rush.game_backend.db.tables.R2DBTable;
import lu.rescue_rush.game_backend.db.views.newsletter.NewsletterSubscription_CountPerSource_View;
import lu.rescue_rush.game_backend.db.views.newsletter.NewsletterSubscription_SourceAction_View;
import lu.rescue_rush.game_backend.db.views.newsletter.NewsletterSubscription_SourceApi_View;
import lu.rescue_rush.game_backend.utils.SpringUtils;
import lu.rescue_rush.game_backend.utils.annotation.RequestSafe;

//@formatter:off
@DB_Table(name = "newsletter_subscription", columns = {
		@Column(name = "id", type = "int", autoIncrement = true),
		@Column(name = "email", type = "varchar(320)", notNull = false), // email can be null if unsubscripted
		@Column(name = "lang", type = "enum('lb', 'en', 'fr', 'de')", notNull = false, default_ = "'lb'"),
		@Column(name = "hash", type = "varchar(64)", notNull = false),
		@Column(name = "since", type = "timestamp", default_ = "CURRENT_TIMESTAMP"),
		@Column(name = "source", type = "varchar(40)", notNull = false),
		@Column(name = "left", type = "timestamp", notNull = false)
}, constraints = {
		@Constraint(type = Constraint.Type.PRIMARY_KEY, name = "pk_newsletter_subscription_id", columns = "id"),
		@Constraint(type = Constraint.Type.UNIQUE, name = "uq_newsletter_subscription_email", columns = "email"),
		@Constraint(type = Constraint.Type.UNIQUE, name = "uq_newsletter_subscription_hash", columns = "hash")
})
//@formatter:on
@Service
public class NewsletterSubscriptionTable extends R2DBTable<NewsletterSubscriptionData> {

	public NewsletterSubscription_CountPerSource_View COUNT_PER_SOURCE;
	public NewsletterSubscription_SourceApi_View SOURCE_API;
	public NewsletterSubscription_SourceAction_View SOURCE_ACTION;

	public NewsletterSubscriptionTable(DataBase dbTest) {
		super(dbTest);
	}

	@RequestSafe
	public NewsletterSubscriptionData requestSafe_insertAndReload(String email, String source) {
		NewsletterSubscriptionData data = null;
		SpringUtils.internalServerError((data = TableHelper.insertOrLoad(this, new NewsletterSubscriptionData(email, source), () -> NewsletterSubscriptionData.byEmail(email)).run()) == null,
				"An error occured.");
		return data;
	}

	@RequestSafe
	public NewsletterSubscriptionData requestSafe_insertAndReload(String email, String source, String lang) {
		NewsletterSubscriptionData data = null;
		SpringUtils.internalServerError((data = TableHelper.insertOrLoad(this, new NewsletterSubscriptionData(email, source, lang), () -> NewsletterSubscriptionData.byEmail(email)).run()) == null,
				"An error occured.");
		return data;
	}

	@RequestSafe
	public NewsletterSubscriptionData requestSafe_byHash(@Nonnull String hash) {
		SpringUtils.badRequest(hash == null, "Invalid email.");
		final NewsletterSubscriptionData data = super.query(NewsletterSubscriptionData.byEmailHash(hash)).thenApply(SpringUtils.first()).run();
		SpringUtils.badRequest(data == null, "Unknown hash.");
		return data;
	}

	@RequestSafe
	public NewsletterSubscriptionData requestSafe_byEmail(@Nonnull String email) {
		SpringUtils.badRequest(email == null, "Invalid email.");
		SpringUtils.badRequest(!PCUtils.validEmail(email), "Invalid email.");
		final NewsletterSubscriptionData data = super.query(NewsletterSubscriptionData.byEmail(email)).thenApply(SpringUtils.first()).run();
		SpringUtils.badRequest(data == null, "Unknown email.");
		return data;
	}

	@RequestSafe
	public NewsletterSubscriptionData requestSafe_anonymize(NewsletterSubscriptionData data) {
		SpringUtils.badRequest(data == null, "Invalid data.");
		data.setEmail(null);
		data.setLeft(Timestamp.from(Instant.now()));
		return super.update(data).catch_(e -> LOGGER.log(Level.SEVERE, "Error while updating data: " + data, e)).run();
	}

	public boolean existsHash(String hash) {
		return super.query(NewsletterSubscriptionData.byEmailHash(hash)).thenApply(SpringUtils.exists()).run();
	}

	public boolean existsEmail(String email) {
		return super.query(NewsletterSubscriptionData.byEmail(email)).thenApply(SpringUtils.exists()).run();
	}

	public List<NewsletterSubscriptionData> all() {
		return super.query(NewsletterSubscriptionData.all()).catch_(e -> LOGGER.log(Level.SEVERE, "Error while querying data.", e)).run();
	}

	public List<NewsletterSubscriptionData> bySource(String source) {
		return super.query(NewsletterSubscriptionData.bySource(source)).catch_(e -> LOGGER.log(Level.SEVERE, "Error while querying data.", e)).run();
	}

	public List<NewsletterSubscriptionData> byLang(String language) {
		return super.query(NewsletterSubscriptionData.byLang(language)).catch_(e -> LOGGER.log(Level.SEVERE, "Error while querying data.", e)).run();
	}

	public List<NewsletterSubscriptionData> byLanguage(Language language) {
		return super.query(NewsletterSubscriptionData.byLang(language.getCode())).catch_(e -> LOGGER.log(Level.SEVERE, "Error while querying data.", e)).run();
	}

	public List<NewsletterSubscriptionData> byLocale(Locale locale) {
		return super.query(NewsletterSubscriptionData.byLang(locale.getLanguage())).catch_(e -> LOGGER.log(Level.SEVERE, "Error while querying data.", e)).run();
	}

	public NewsletterSubscriptionData byEmail(String email) {
		return super.query(NewsletterSubscriptionData.byEmail(email)).catch_(e -> LOGGER.log(Level.SEVERE, "Error while querying data.", e)).thenApply(SpringUtils.first(() -> null)).run();
	}

	public NewsletterSubscriptionData byHash(String hash) {
		return super.query(NewsletterSubscriptionData.byEmailHash(hash)).catch_(e -> LOGGER.log(Level.SEVERE, "Error while querying data.", e)).thenApply(SpringUtils.first(() -> null)).run();
	}

}
