package lu.rescue_rush.game_backend.db.tables.monitor.email;

import java.util.logging.Level;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import lu.pcy113.pclib.db.DataBase;
import lu.pcy113.pclib.db.TableHelper;
import lu.pcy113.pclib.db.annotations.table.Column;
import lu.pcy113.pclib.db.annotations.table.Constraint;
import lu.pcy113.pclib.db.annotations.table.DB_Table;

import lu.rescue_rush.game_backend.db.data.monitor.email.VerifiedEmailData;
import lu.rescue_rush.game_backend.db.tables.R2DBTable;
import lu.rescue_rush.game_backend.utils.SpringUtils;
import lu.rescue_rush.game_backend.utils.annotation.RequestSafe;

//@formatter:off
@DB_Table(name = "verified_email", columns = {
		@Column(name = "id", type = "int", autoIncrement = true),
		@Column(name = "hash", type = "varchar(64)", notNull = true),
		@Column(name = "verified", type = "bit", default_ = "0x01"),
		@Column(name = "first_time", type = "timestamp", default_ = "CURRENT_TIMESTAMP")
}, constraints = {
		@Constraint(type = Constraint.Type.PRIMARY_KEY, name = "pk_verified_emails_id", columns = "id"),
		@Constraint(type = Constraint.Type.UNIQUE, name = "uq_verified_emails_hash", columns = "hash")
})
//@formatter:on
@Service
public class VerifiedEmailTable extends R2DBTable<VerifiedEmailData> {

	public VerifiedEmailTable(DataBase dbTest) {
		super(dbTest);
	}

	@RequestSafe
	@Cacheable(value = "verified-emails.hash", key = "T(lu.rescue_rush.game_backend.utils.SpringUtils).hash(#email)")
	public VerifiedEmailData requestSafe_byEmail(String email) {
		VerifiedEmailData ved = TableHelper.insertOrLoad(this, new VerifiedEmailData(email), () -> VerifiedEmailData.byEmail(email)).run();
		SpringUtils.notFound(ved == null, "Data not found.");
		return ved;
	}

	@NonNull
	@Cacheable(value = "verified-emails.hash", key = "T(lu.rescue_rush.game_backend.utils.SpringUtils).hash(#email)")
	public VerifiedEmailData loadOrInsertByEmail(String email) {
		if (this.requestSafe_existsEmail(email)) {
			return this.requestSafe_byEmail(email);
		}

		return super.insertAndReload(new VerifiedEmailData(email)).catch_(e -> LOGGER.log(Level.SEVERE, "Error while inserting/reloading data: '" + email + "'", e)).run();
	}

	@CachePut(value = "verified-emails.hash", key = "#ved.hash")
	public VerifiedEmailData updateVerifiedEmailData(VerifiedEmailData ved) {
		return super.update(ved).catch_(e -> LOGGER.log(Level.SEVERE, "Error while updating data: " + ved, e)).run();
	}

	@RequestSafe
	public boolean requestSafe_existsEmail(String email) {
		return super.exists(new VerifiedEmailData(email)).catch_(SpringUtils.catch_(LOGGER, "Couldn't verify email validity.")).run();
	}

	@RequestSafe
	public boolean requestSafe_existsHash(String hash) {
		return super.exists(VerifiedEmailData.ofHash(hash)).catch_(SpringUtils.catch_(LOGGER, "Couldn't verify email validity.")).run();
	}

}
