package revxrsal.spec;

public class NameMapper {

    public static String kebabToCamel(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        StringBuilder result = new StringBuilder();
        boolean toUpper = false;

        for (char c : input.toCharArray()) {
            if (c == '-') {
                toUpper = true;
            } else {
                if (toUpper) {
                    result.append(Character.toUpperCase(c));
                    toUpper = false;
                } else {
                    result.append(c);
                }
            }
        }

        return result.toString();
    }


    public static String camelToKebab(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input
                .replaceAll("([a-z])([A-Z])", "$1-$2")
                .toLowerCase();
    }

}
