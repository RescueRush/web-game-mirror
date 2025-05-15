package lu.rescue_rush.game_backend.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;

import org.apache.commons.text.StringEscapeUtils;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import lu.pcy113.pclib.PCUtils;
import lu.pcy113.pclib.impl.ExceptionConsumer;
import lu.pcy113.pclib.impl.ExceptionFunction;
import lu.pcy113.pclib.impl.ExceptionSupplier;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lu.rescue_rush.game_backend.R2ApiMain;
import lu.rescue_rush.game_backend.data.Language;
import lu.rescue_rush.game_backend.db.data.user.UserData;
import lu.rescue_rush.game_backend.sb.ws.R2WebSocketHandler;
import lu.rescue_rush.game_backend.sb.ws.WSMapping;
import lu.rescue_rush.game_backend.utils.annotation.RequestSafe;
import lu.rescue_rush.game_backend.utils.monitoring.InteractionMonitor;

public final class SpringUtils {

	public static final String TOKEN_COOKIE_NAME = "token";
	public static final int TOKEN_COOKIE_DAY_COUNT = 30;

	public static boolean extractFile(String inJarPath, File configDir, String configFileName) {
		try {
			ClassPathResource resource = new ClassPathResource(inJarPath);

			Path targetPath = Path.of(new File(configDir, configFileName).getPath());

			Files.createDirectories(targetPath.getParent());

			if (Files.exists(targetPath)) {
				return false;
			}

			try (InputStream inputStream = resource.getInputStream()) {
				Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
			}

			return true;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static Cookie newCookie(String name, String value, boolean httpOnly, int maxAgeSec) {
		Cookie cookie = new Cookie(name, value);
		cookie.setHttpOnly(httpOnly);
		cookie.setSecure(true);
		// cookie.setDomain("localhost");
		cookie.setPath("/");
		cookie.setMaxAge(maxAgeSec);

		return cookie;
	}

	public static Cookie getTokenCookie(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (TOKEN_COOKIE_NAME.equals(cookie.getName())) {
					return cookie;
				}
			}
		}
		return null;
	}

	public static Cookie newTokenCookie(String newToken) {
		return newCookie(TOKEN_COOKIE_NAME, newToken, false, (int) TimeUnit.SECONDS.convert(TOKEN_COOKIE_DAY_COUNT, TimeUnit.DAYS));
	}

	public static <T> T first(List<T> e) {
		return e.get(0);
	}

	public static <T> ExceptionFunction<List<T>, T> first() {
		return e -> e.get(0);
	}

	public static <T> ExceptionFunction<List<T>, T> first(ExceptionSupplier<T> orElse) {
		return e -> e.size() > 0 ? e.get(0) : orElse.get();
	}

	public static <T> boolean exists(List<T> e) {
		return !e.isEmpty();
	}

	public static <T> ExceptionFunction<List<T>, Boolean> exists() {
		return e -> !e.isEmpty();
	}

	public static void notFound(boolean b, String string) {
		if (b) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, string);
		}
	}

	public static void notFound(boolean b, String string, Runnable else_) {
		if (b) {
			else_.run();
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, string);
		}
	}

	public static void badRequest(boolean b, String string) {
		if (b) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, string);
		}
	}

	public static void badRequest(boolean b, String string, Runnable else_) {
		if (b) {
			else_.run();
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, string);
		}
	}

	public static void internalServerError(boolean b, String string) {
		if (b) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, string);
		}
	}

	public static void internalServerError(boolean b, String string, Runnable else_) {
		if (b) {
			else_.run();
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, string);
		}
	}

	public static void forbidden(String string) {
		throw new ResponseStatusException(HttpStatus.FORBIDDEN, string);
	}

	public static void forbidden(boolean b, String string) {
		if (b) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, string);
		}
	}

	public static void forbidden(boolean b, String string, Runnable else_) {
		if (b) {
			else_.run();
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, string);
		}
	}

	public static void unauthorized(boolean b, String string) {
		if (b) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, string);
		}
	}

	public static void unauthorized(boolean b, String string, Runnable else_) {
		if (b) {
			else_.run();
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, string);
		}
	}

	public static boolean validPass(String s) {
		return s != null && !s.isEmpty() && !s.isBlank() && !s.equals(UserData.hashPass(""));
	}

	public static boolean validString(String s) {
		return s != null && !s.isEmpty() && !s.isBlank();
	}

	public static String sanitizeHtml(String message) {
		return StringEscapeUtils.escapeHtml4(message);
	}

	public static String encodeURL(String text) {
		try {
			return URLEncoder.encode(text, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}

	public static ExceptionConsumer<Exception> catch_(Logger LOGGER, Function<Exception, String> handler) {
		return e -> {
			LOGGER.severe(handler.apply(e));
			if (R2ApiMain.DEBUG) {
				e.printStackTrace();
			}
		};
	}

	public static ExceptionConsumer<Exception> catch_(Logger LOGGER, Supplier<String> msgSupplier) {
		return e -> {
			LOGGER.severe(msgSupplier.get() + " (" + e.getMessage() + ")");
			if (R2ApiMain.DEBUG) {
				e.printStackTrace();
			}
		};
	}

	public static ExceptionConsumer<Exception> catch_(Logger LOGGER, String msg) {
		return e -> {
			LOGGER.severe(msg + " (" + e.getMessage() + ")");
			if (R2ApiMain.DEBUG) {
				e.printStackTrace();
			}
		};
	}

	public static void catch_(Logger LOGGER, Function<Exception, String> handler, Exception e) {
		LOGGER.severe(handler.apply(e));
		if (R2ApiMain.DEBUG) {
			e.printStackTrace();
		}
	}

	public static void catch_(Logger LOGGER, Supplier<String> msgSupplier, Exception e) {
		LOGGER.severe(msgSupplier.get() + " (" + e.getMessage() + ")");
		if (R2ApiMain.DEBUG) {
			e.printStackTrace();
		}
	}

	public static void catch_(Logger LOGGER, String msg, Exception e) {
		LOGGER.severe(msg + " (" + e.getMessage() + ")");
		if (R2ApiMain.DEBUG) {
			e.printStackTrace();
		}
	}

	public static String getContextPath() {
		return getFullPath(getLastEndpointCaller());
	}

	public static String getFullPath(Method method) {
		return getFullPath(method, method.getDeclaringClass());
	}

	public static String getFullPath(Method method, Class<?> controllerClass) {
		List<String> pathParts = new ArrayList<>();

		if (controllerClass.isAnnotationPresent(RequestMapping.class)) {
			RequestMapping classMapping = controllerClass.getAnnotation(RequestMapping.class);
			if (classMapping.value().length > 0) {
				pathParts.add(classMapping.value()[0]);
			}
		} else if (controllerClass.isAnnotationPresent(WSMapping.class)) {
			WSMapping classMapping = controllerClass.getAnnotation(WSMapping.class);
			pathParts.add(classMapping.path());
		}

		if (method.isAnnotationPresent(GetMapping.class)) {
			pathParts.add(getFirstPath(method.getAnnotation(GetMapping.class).value()));
		} else if (method.isAnnotationPresent(PostMapping.class)) {
			pathParts.add(getFirstPath(method.getAnnotation(PostMapping.class).value()));
		} else if (method.isAnnotationPresent(PutMapping.class)) {
			pathParts.add(getFirstPath(method.getAnnotation(PutMapping.class).value()));
		} else if (method.isAnnotationPresent(DeleteMapping.class)) {
			pathParts.add(getFirstPath(method.getAnnotation(DeleteMapping.class).value()));
		} else if (method.isAnnotationPresent(PatchMapping.class)) {
			pathParts.add(getFirstPath(method.getAnnotation(PatchMapping.class).value()));
		} else if (method.isAnnotationPresent(RequestMapping.class)) {
			pathParts.add(getFirstPath(method.getAnnotation(RequestMapping.class).value()));
		} else if (method.isAnnotationPresent(WSMapping.class)) {
			pathParts.add(method.getAnnotation(WSMapping.class).path());
		}

		return "/" + String.join("/", pathParts).replaceAll("//+", "/").replaceAll("^/|/$", "");
	}

	private static String getFirstPath(String[] paths) {
		return paths.length > 0 ? paths[0] : "";
	}

	public static Method getLastEndpointCaller() {
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

		for (int i = stackTrace.length - 1; i >= 0; i--) {
			StackTraceElement element = stackTrace[i];

			try {
				Class<?> clazz = Class.forName(element.getClassName());
				Method[] methods = clazz.getDeclaredMethods();

				for (Method method : methods) {
					if (method.getName().equals(element.getMethodName()) && hasMappingAnnotation(method)) {
						return method;
					}
				}
			} catch (ClassNotFoundException ignored) {
			}
		}

		return null;
	}

	private static boolean hasMappingAnnotation(Method method) {
		return method.isAnnotationPresent(GetMapping.class) || method.isAnnotationPresent(PostMapping.class) || method.isAnnotationPresent(PutMapping.class)
				|| method.isAnnotationPresent(DeleteMapping.class) || method.isAnnotationPresent(PatchMapping.class) || method.isAnnotationPresent(RequestMapping.class)
				|| method.isAnnotationPresent(WSMapping.class);
	}

	public static boolean isContextUser() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
			return false;
		}
		return true;
	}

	public static Authentication setContextUser(UserData user) {
		final UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(auth);
		return auth;
	}

	public static Authentication getContextAuthentication() {
		return SecurityContextHolder.getContext().getAuthentication();
	}

	public static Object getContextPrincipal() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated()) {
			return null;
		}
		return auth.getPrincipal();
	}

	public static UserData setContextAuthentication(Authentication auth) {
		SecurityContextHolder.getContext().setAuthentication(auth);
		return (UserData) auth.getPrincipal();
	}

	public static void setContextLanguage(Language lang) {
		LocaleContextHolder.setLocale(lang.getLocale());
	}

	public static void setContextLocale(Locale locale) {
		LocaleContextHolder.setLocale(locale);
	}

	public static void clearContextLocale() {
		LocaleContextHolder.setLocale(null);
	}

	public static Language getContextLanguage() {
		if (LocaleContextHolder.getLocaleContext() == null) {
			return null;
		}
		return Language.byLocale(LocaleContextHolder.getLocaleContext().getLocale());
	}

	public static Locale getContextLocale() {
		return LocaleContextHolder.getLocale();
	}

	public static void clearContextUser() {
		SecurityContextHolder.clearContext();
	}

	@RequestSafe
	public static UserData needContextUser() {
		UserData user = getContextUser();
		if (user == null) {
			SpringUtils.forbidden(true, "User not authenticated.");
		}
		return user;
	}

	public static UserData getContextUser() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
			return null;
		}
		return (UserData) auth.getPrincipal();
	}

	public static boolean isAnonymous() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		return auth != null && auth.isAuthenticated() && auth instanceof AnonymousAuthenticationToken;
	}

	public static void setRequestContext(HttpServletRequest attr) {
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(attr));
	}

	private static final ThreadLocal<String> requestSource = new ThreadLocal<String>();

	public static void setRequestSource(String remoteAddr) {
		requestSource.set(remoteAddr);
	}

	public static void clearRequestSource() {
		requestSource.remove();
	}

	public static String getContextSource() {
		if (isHttpRequest()) {
			HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
			return request.getRemoteAddr();
		} else {
			return requestSource.get();
		}
	}

	public static boolean isHttpRequest() {
		return RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes;
	}

	public static String getFilteredUtils() {
		return PCUtils.getCallerClassName(false, true, "lu\\.pcy113\\..*", "org\\.springframework\\..*", "java\\.lang\\..*", "lu\\.rescue_rush\\.game_backend\\.db\\..*", "jdk\\.internal\\..*",
				"lu\\.rescue_rush\\.game_backend\\.utils\\.tracker\\.InteractionMonitor", "lu\\.rescue_rush\\.game_backend\\.utils\\.SpringUtils");
	}

	public static void permissionAnd(String... permissions) {
		permissionAnd(needContextUser(), permissions);
	}

	public static void permissionOr(String... permissions) {
		permissionOr(needContextUser(), permissions);
	}

	public static void permissionAnd(UserData ud, String[] permissions) {
		for (String perm : permissions) {
			if (!ud.hasPermission(perm)) {
				InteractionMonitor.pushOutcomeDesc(ud, false, "Permission denied (missing required): " + perm + ".");
				SpringUtils.forbidden(true, "Permission denied.");
			}
		}
	}

	public static void permissionOr(UserData ud, String[] permissions) {
		if (!Arrays.stream(permissions).anyMatch(ud::hasPermission)) {
			InteractionMonitor.pushDeniedDesc("Permission denied (none of): " + String.join(", ", permissions));
			SpringUtils.forbidden(true, "Permission denied.");
		}
	}

	public static String getWSMapping(R2WebSocketHandler handler) {
		return handler.getClass().getAnnotation(WSMapping.class).path();
	}

	public static String hash(String str) {
		return PCUtils.hashString(str, "SHA-256");
	}

	/**
	 * Returns the input string, or "N/A" if it is null.
	 */
	public static String nullString(String str) {
		return str == null ? "N/A" : str;
	}

	/**
	 * @see {@link #nullString(String)}
	 */
	public static String nullString(ExceptionSupplier<String> str) {
		try {
			String data = str.get();
			if (data == null) {
				return "N/A";
			}
			return data;
		} catch (Exception e) {
			return "N/A";
		}
	}

	/**
	 * @see {@link #nullString(String)}
	 */
	public static String nullString(String str, String def) {
		return str == null ? def : str;
	}

	/**
	 * @see {@link #nullString(String)}
	 */
	public static String nullString(String str, Supplier<String> def) {
		return str == null ? def.get() : str;
	}

	/**
	 * Throws a RuntimeException if the string is null.
	 */
	public static String safeString(String str) {
		if (str == null) {
			PCUtils.throwRuntime(new NullPointerException("String is null"));
			return null;
		} else {
			return str;
		}
	}

	/**
	 * @see {@link #safeString(String)}
	 */
	public static String safeString(String str, ExceptionSupplier<String> def) {
		if (str == null) {
			try {
				return def.get();
			} catch (Exception e) {
				PCUtils.throwRuntime(e);
				return null;
			}
		} else {
			return str;
		}
	}

	/**
	 * @see {@link #safeString(String)}
	 */
	public static String safeString(ExceptionSupplier<String> str) {
		try {
			String data = str.get();
			if (data == null) {
				PCUtils.throwRuntime(new NullPointerException("String is null"));
			}
			return data;
		} catch (Exception e) {
			PCUtils.throwRuntime(e);
			return null;
		}
	}

}
