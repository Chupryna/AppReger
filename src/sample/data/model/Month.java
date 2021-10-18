package sample.data.model;

public enum Month {

	January("янв"), February("февр"), March("мар"),
	April("апр"), May("мая"), June("июн"),
	July("июл"), August("авг"), September("сент"),
	October("окт"), November("нояб"), December("дек");

	private final String name;

	Month(String name) {
		this.name = name;
	}

	public static String getMonthByNumber(int number) {
		return Month.values()[number - 1].name;
	}
}
