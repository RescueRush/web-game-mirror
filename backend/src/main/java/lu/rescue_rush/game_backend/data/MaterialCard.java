package lu.rescue_rush.game_backend.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public enum MaterialCard implements Localizable {

	// @formater:off
	BANDAGE(0, "Verband", "Bandage", "Bandage", "Verband", "De Verband fixéiert eng Kompress op enger grousser Wonn oder stabiliséiert Verletzungen, wéi zum Beispill bei engem ëmgeknéckste Knéchel.",
			"The bandage secures a compress on a large wound or stabilizes injuries, such as a sprained ankle.",
			"Le bandage fixe une compresse sur une grande plaie ou stabilise des blessures, comme par exemple une cheville foulée.",
			"Der Verband fixiert eine Kompresse auf einer großen Wunde oder stabilisiert Verletzungen, wie zum Beispiel bei einem umgeknickten Knöchel."),
	BAND_AID(1, "Plooschter", "Band-aid", "Pansement", "Pflaster",
			"Eng Plooschter kann ee fir kleng Wonne benotzen. D’Plooschter gëtt dofir esou opgepecht, dass just dee wäisse Polster vun der Plooschter d’Wonn beréiert.",
			"A plaster can be used for small wounds. It should be applied in such a way that only the white pad of the plaster comes into contact with the wound.",
			"Un pansement peut être utilisé pour de petites blessures. Le pansement doit être appliqué de manière à ce que seule la partie blanche touche la blessure.",
			"Ein Pflaster kann für kleine Wunden verwendet werden. Das Pflaster wird so aufgeklebt, dass nur das weiße Polster die Wunde berührt."),
	GLOVES(2, "Händschen", "Gloves", "Gants", "Handschuhe",
			"Ëmmer, wann een e Patient mat enger oppener Wonn behandelt, soll ee fir d’éischt Händschen undoen, fir sech viru méigleche Krankheeten, déi iwwert d’Blutt iwwerdroe ginn, ze schützen.",
			"Whenever treating a patient with an open wound, it is important to put on gloves first to protect yourself from diseases that can be transmitted through blood.",
			"Chaque fois que l'on traite un patient avec une blessure ouverte, il est important de mettre des gants d'abord, afin de se protéger contre les maladies qui peuvent être transmises par le sang.",
			"Immer, wenn man einen Patienten mit einer offenen Wunde behandelt, sollte man zuerst Handschuhe anziehen, um sich vor möglichen Krankheiten zu schützen, die über das Blut übertragen werden."),
	COMPRESS(3, "Kompress", "Compress", "Compresse", "Kompresse",
			"Kompresse gi benotzt, wann eng Wonn ze grouss fir eng Plooschter ass. Se gëtt op d’Wonn geluecht an de Verband ronderëm gewéckelt, fir d’Kompress ze befestegen.",
			"The compress is used when a wound is too large for a plaster. The compress is placed on the wound and secured with a bandage to keep it in place.",
			"Les compresses sont utilisées lorsqu'une blessure est trop grande pour un pansement. Elle est placée sur la blessure et fixée avec un bandage pour maintenir la compresse en place.",
			"Kompressen werden verwendet, wenn eine Wunde zu groß für ein Pflaster ist. Sie wird auf die Wunde gelegt und der Verband wird darum gewickelt, um die Kompresse zu fixieren."),
	HOSPITAL(4, "Klinick", "Hospital", "Hôpital", "Krankenhaus", "Wann en net op der Plaz behandelt ka ginn, muss de Patient an d’Klinick gefouert ginn.",
			"If the patient cannot be treated on site, the patient must be taken to the hospital.", "S'il ne peut pas être traité sur place, le patient doit être transporté à l'hôpital.",
			"Wenn er nicht vor Ort behandelt werden kann, muss der Patient ins Krankenhaus gebracht werden."),
	COLD_PACK(5, "Killpak", "Cold pack", "Pack de glace", "Kühlpack",
			"De Killpak hëlleft bei Prellungen oder Verstauchunge géint de Wéi a géint d’Schwellung. E Killpak muss ëmmer mat engem Dräiecksduch oder engem Kichenduch benotzt gi fir direkten Hautkontakt ze vermeiden.",
			"The cold pack helps relieve pain and reduce swelling in the case of bruises or sprains. A cold pack must always be used with a triangular bandage or a kitchen towel to avoid direct contact with the skin.",
			"La compresse froide aide à soulager la douleur et reduire l'enflure en cas de contusions ou de foulures. Une compresse froide doit toujours être utilisé avec une écharpe triangulaire ou un torchon de cuisine pour éviter le contact direct avec la peau.",
			"Das Kältepack hilft bei Prellungen oder Verstauchungen gegen den Schmerz und gegen die Schwellung. Ein Kältepack muss immer mit einem Dreiecktuch oder einem Küchentuch verwendet werden, um direkten Hautkontakt zu vermeiden."),
	SURVIVAL_BLANKET(6, "Rettungsdecken", "Survival blanket", "Couverture de survie", "Rettungsdecken",
			"D'Rettungsdecken hëlleft, eng ënnerkillte Persoun nees opzewiermen. Et ass wichteg, dass de Patient net naass ass. De Patient soll gutt agewéckelt ginn, an déi sëlwereg Säit muss no banne gedréint ginn.",
			"The emergency blanket helps to warm up a person. It is important that the patient is not wet. The patient should be well wrapped, and the silver side of the blanket should be facing inward.",
			"La couverture de sauvetage aide à réchauffer une personne. Il est important que le patient ne soit pas mouillé. Le patient doit être bien enveloppé, et le côté argenté de la couverture doit être tourné vers l'intérieur.",
			"Die Rettungsdecke hilft, eine Person wieder aufzuwärmen. Es ist wichtig, dass der Patient nicht nass ist. Der Patient sollte gut eingewickelt werden, und die silberne Seite muss nach innen gedreht werden."),
	TRIANGLE_BANDAGE(7, "Dräiecksduch", "Triangular bandage", "Écharpe triangulaire", "Dreieckstuch",
			"En Dräiecksduch huet vill verschidde Funktiounen. Et ka benotzt ginn, fir en Aarm ze stabiliséieren, wann e gebrach ass. Et kann awer och ronderëm e Killpak gewéckelt ginn, fir dass d’Haut net ze kal gëtt.",
			"A triangular bandage has many different functions. It can be used to stabilize an arm if it is broken. It can also be wrapped around a cold pack to protect the skin from getting too cold.",
			"Une écharpe triangulaire a de nombreuses fonctions différentes. Elle peut être utilisée pour stabiliser un bras en cas de fracture. Elle peut également être enroulée autour d'une compresse froide pour protéger la peau du froid.",
			"Ein Dreiecktuch hat viele verschiedene Funktionen. Es kann verwendet werden, um einen Arm zu stabilisieren, wenn er gebrochen ist. Es kann aber auch um ein Kältepack gewickelt werden, damit die Haut nicht zu kalt wird.");
	// @formater:on

	private static final Map<Integer, MaterialCard> MAP = new HashMap<>();

	static {
		for (MaterialCard card : values()) {
			MAP.put(card.getId(), card);
		}
	}

	private final int id;
	private String name_LU, name_EN, name_FR, name_DE;
	private String desc_LU, desc_EN, desc_FR, desc_DE;

	private MaterialCard(int id, String name_LU, String name_EN, String name_FR, String name_DE, String desc_LU, String desc_EN, String desc_FR, String desc_DE) {
		this.id = id;
		this.name_LU = name_LU;
		this.name_EN = name_EN;
		this.name_FR = name_FR;
		this.name_DE = name_DE;
		this.desc_LU = desc_LU;
		this.desc_EN = desc_EN;
		this.desc_FR = desc_FR;
		this.desc_DE = desc_DE;
	}

	public int getId() {
		return id;
	}

	public String getName_LU() {
		return name_LU;
	}

	public String getName_EN() {
		return name_EN;
	}

	public String getName_FR() {
		return name_FR;
	}

	public String getName_DE() {
		return name_DE;
	}

	public String getDesc_LU() {
		return desc_LU;
	}

	public String getDesc_EN() {
		return desc_EN;
	}

	public String getDesc_FR() {
		return desc_FR;
	}

	public String getDesc_DE() {
		return desc_DE;
	}

	@Override
	public String forLocale(Locale lang) {
		switch (Language.byLocale(lang)) {
		case ENGLISH:
			return name_EN;
		case FRENCH:
			return name_FR;
		case LUXEMBOURISH:
			return name_LU;
		case GERMAN:
			return name_DE;
		default:
			return null;
		}
	}

	public String descriptionForLocale(Locale lang) {
		switch (Language.byLocale(lang)) {
		case ENGLISH:
			return desc_EN;
		case FRENCH:
			return desc_FR;
		case LUXEMBOURISH:
			return desc_LU;
		case GERMAN:
			return desc_DE;
		default:
			return null;
		}
	}

	public static int computeCardsCount(int answers, int length) {
		return (int) IntStream.rangeClosed(0, length).filter(i -> (answers & (1 << i)) != 0).count();
	}

	public static String asString(int possibleAnswers, int length) {
		return IntStream.rangeClosed(0, length).mapToObj(i -> ((possibleAnswers & (1 << i)) != 0 ? "1" : "0")).collect(Collectors.joining());
	}

	public static MaterialCard byId(int id) {
		return MAP.get(id);
	}

	public static List<MaterialCard> unwrap(int answers) {
		List<MaterialCard> list = new ArrayList<>();
		for (int i = 0; i < Integer.BYTES * 8; i++) {
			if ((answers & (1 << i)) != 0) {
				list.add(MaterialCard.values()[i]);
			}
		}
		return list;
	}

	public static int wrap(List<Integer> numbers) {
		int answers = 0;
		for (int i : numbers) {
			answers |= 1 << i;
		}
		return answers;
	}

	public static int wrap(MaterialCard[] cards) {
		int answers = 0;
		for (MaterialCard card : cards) {
			answers |= 1 << card.getId();
		}
		return answers;
	}

	public static int wrap(int[] numbers) {
		int answers = 0;
		for (int i : numbers) {
			answers |= 1 << i;
		}
		return answers;
	}

}
