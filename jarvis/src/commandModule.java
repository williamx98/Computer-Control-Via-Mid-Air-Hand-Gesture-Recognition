import java.io.*;

public class commandModule {
	private static String[] search   = {"get", "look", "result", "research", "search"};
	private static String[] question = {"where", "who", "how", "what", "when", "why"};
	private static String[] photo   = {"photos", "pictures", "images", "photographs"};
	private static String[] ignore   = {"the", "is", "a", "be"};
	private static String[] action = {"open", "close", "move", "expand", "fullsceen", "minimize", "rid", "delete", "next", "back", "go"};
	private static String[] identifier = {"up", "for", ""}
	private static String command = "";
		public static void main(String[] args) throws Exception {
	}

	public static void getCommand(String raw) { command = raw; }

	private static void processCommand() {
		while ()
			for (int x = 0; x < search.length; x++) {

			}
	}

	private static void openGoogle(String type, String search) throws Exception {
		String mainLink = "";
		switch (type) {
			case "search": mainLink = "https://www.google.com/search?q="; break;
			case "images": mainLink = "https://www.google.com/search?site=imghp&tbm=isch&q="; break;
			case "maps"  : mainLink = "https://www.google.com/maps/place/"; break;
		}

		for (String test: ignore) {
			if (search.contains(test)) {
				search = search.replace(test, "");
			}
		}

		if (search.charAt(0) == ' ') {
			search = search.substring(1);
		}

		if (search.charAt(search.length() - 1) == ' ') {
			search = search.substring(0, search.length() - 2);
		}

		search = search.replace(" ", "+");
		mainLink += search;
		Runtime.getRuntime().exec("cmd /c start chrome " + mainLink);
		System.out.println(mainLink);
	}
}