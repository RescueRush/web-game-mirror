package lu.rescue_rush.game_backend.sb.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lu.rescue_rush.game_backend.db.data.user.UserData;
import lu.rescue_rush.game_backend.db.tables.user.UserTable;

@Service
public class UserAuthenticator {

	@Autowired
	private UserTable users;

	public UserData cookies(final HttpServletRequest request) {
		return cookies(request.getCookies());
	}

	public UserData cookies(final Cookie[] cookies) {
		for (Cookie cookie : cookies) {
			if ("token".equals(cookie.getName())) {
				return token(cookie.getValue());
			}
		}

		return null;
	}

	public UserData token(final String token) {
		return users.byToken(token);
	}

	public UserData auth(final String auth) {
		if (!auth.startsWith("Token ")) {
			return null;
		}
		final String token = auth.substring("Token ".length());

		return token(token);
	}

	public UserData session(WebSocketSession session) {
		return auth(session.getHandshakeHeaders().get("Authorization").get(0));
	}

}
