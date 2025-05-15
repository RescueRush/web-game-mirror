package lu.rescue_rush.game_backend.data;

import java.util.HashMap;
import java.util.Locale;

public enum Language {

	LUXEMBOURISH(Locales.LUXEMBOURISH), ENGLISH(Locales.ENGLISH), GERMAN(Locales.GERMAN), FRENCH(Locales.FRENCH);

	private static final HashMap<String, Language> MAPCODE = new HashMap<>();
	static {
		for (Language lang : Language.values()) {
			MAPCODE.put(lang.getLocale().getLanguage().toLowerCase(), lang);
		}

		MAPCODE.put("lu".toLowerCase(), LUXEMBOURISH);
	}

	private Locale locale;

	private Language(Locale locale) {
		this.locale = locale;
	}

	public Locale getLocale() {
		return locale;
	}

	public String getCode() {
		return locale.getLanguage().toLowerCase();
	}

	public static Language byCode(String code) {
		if (code == null) {
			return null;
		}
		return MAPCODE.get(code.toLowerCase());
	}

	public static Language byLocale(Locale locale) {
		return MAPCODE.get(locale.getLanguage().toLowerCase());
	}

}
