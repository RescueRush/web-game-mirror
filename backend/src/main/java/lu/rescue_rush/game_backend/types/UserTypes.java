package lu.rescue_rush.game_backend.types;

import java.sql.Timestamp;
import java.util.List;

import lu.rescue_rush.game_backend.db.data.game.LeaderboardData;
import lu.rescue_rush.game_backend.db.data.user.UserSanctionData;
import lu.rescue_rush.game_backend.db.data.user.UserSanctionReasonData;

public final class UserTypes {

	public static class LeaderboardEntry {

		public int position;
		public int points;
		public String username;
		public Timestamp joinDate;

		public LeaderboardEntry(int position, int points, String username, Timestamp joinDate) {
			this.position = position;
			this.points = points;
			this.username = username;
			this.joinDate = joinDate;
		}

		public LeaderboardEntry(LeaderboardData data, Timestamp joinDate) {
			this.points = data.getPoints();
			this.position = data.getPosition();
			this.username = data.getUsername();
			this.joinDate = joinDate;
		}

		public LeaderboardEntry(LeaderboardData data) {
			this.points = data.getPoints();
			this.position = data.getPosition();
			this.username = data.getUsername();
		}

	}

	public static class LoginRequest {

		public String user;
		public String pass;

		public LoginRequest() {
		}

		public LoginRequest(String user, String pass) {
			this.user = user;
			this.pass = pass;
		}

	}

	public static class NewPasswordRequest {

		public String token;
		public String newPass;

		public NewPasswordRequest() {
		}

		public NewPasswordRequest(String token, String newPass) {
			this.token = token;
			this.newPass = newPass;
		}

	}

	public static class LogonRequest {

		public String email;
		public String user;
		public String pass;
		public String lang;

		public LogonRequest() {
		}

		public LogonRequest(String email, String user, String pass, String lang) {
			this.email = email;
			this.user = user;
			this.pass = pass;
			this.lang = lang;
		}

	}

	public static class LoginResponse {

		public String token;

		public LoginResponse(String token) {
			this.token = token;
		}

	}

	public static class LogonResponse {

		public String token;

		public LogonResponse(String token) {
			this.token = token;
		}

	}

	public static class UpdateNameRequest {

		public String newName;
		public String pass;

		public UpdateNameRequest() {
		}

		public UpdateNameRequest(String newName, String pass) {
			this.newName = newName;
			this.pass = pass;
		}

	}

	public static class UpdatePassRequest {

		public String currentPass, newPass, lang;

		public UpdatePassRequest() {
		}

		public UpdatePassRequest(String currentPass, String newPass, String lang) {
			this.currentPass = currentPass;
			this.newPass = newPass;
			this.lang = lang;
		}

	}

	public static class UpdateEmailRequest {

		public String newEmail;
		public String pass;
		public String lang;

		public UpdateEmailRequest() {
		}

		public UpdateEmailRequest(String newEmail, String pass, String lang) {
			this.newEmail = newEmail;
			this.pass = pass;
			this.lang = lang;
		}

	}

	public static class UpdateEmailResponse {

		public String email;
		public String token;

		public UpdateEmailResponse(String email, String token) {
			this.email = email;
			this.token = token;
		}

	}

	public static class ResetPassRequest {

		public String email;

		public ResetPassRequest() {
		}

		public ResetPassRequest(String email) {
			this.email = email;
		}

	}

	public static class ForgotPassRequest {

		public String token, newPass;

		public ForgotPassRequest() {
		}

		public ForgotPassRequest(String token, String newPass) {
			this.token = token;
			this.newPass = newPass;
		}

	}

	public static class SanctionReasonResponse {

		public String name;
		public String key;
		public String description;

		public SanctionReasonResponse(String name, String key, String description) {
			this.name = name;
			this.key = key;
			this.description = description;
		}

	}

	public static SanctionReasonResponse buildSanctionReasonResponse(UserSanctionReasonData reason) {
		return new SanctionReasonResponse(reason.getName(), reason.getKey(), reason.getDescription());
	}

	public static class BannedResponse {

		public SanctionReasonResponse reason;
		public String[] descriptions;

		public BannedResponse(SanctionReasonResponse reason, String[] descriptions) {
			this.reason = reason;
			this.descriptions = descriptions;
		}

		public BannedResponse(SanctionReasonResponse reason) {
			this.reason = reason;
		}

	}

	public static BannedResponse buildBannedResponse(SanctionReasonResponse reason, List<UserSanctionData> datas) {
		return new BannedResponse(reason, datas.stream().map(UserSanctionData::getDescription).toArray(String[]::new));
	}

	public static BannedResponse buildBannedResponse(SanctionReasonResponse reason) {
		return new BannedResponse(reason);
	}

}
