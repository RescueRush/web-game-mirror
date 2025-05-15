package lu.rescue_rush.game_backend.data;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class Locales {

	public static final Locale LUXEMBOURISH = new Locale("lb", "LU");
	public static final Locale ENGLISH = Locale.ENGLISH;
	public static final Locale FRENCH = Locale.FRENCH;
	public static final Locale GERMAN = Locale.GERMAN;

	public static final Locale[] SUPPORTED_LOCALES = new Locale[] { LUXEMBOURISH, ENGLISH, FRENCH, GERMAN };

	public static final List<Locale> SUPPORTED_LOCALES_LIST = Collections.unmodifiableList(Arrays.asList(SUPPORTED_LOCALES));

	private static Map<String, Locale> MAPCODE = new HashMap<>();
	static {
		for (Locale lang : SUPPORTED_LOCALES) {
			MAPCODE.put(lang.getLanguage().toLowerCase(), lang);
		}
		MAPCODE.put("lu".toLowerCase(), LUXEMBOURISH);
	}

	public static Locale byCode(String code) {
		return MAPCODE.get(code.toLowerCase());
	}

}
