import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class LexicalAnalyzer {

    public static boolean successful_read = true;
    public static final Set<String> RESERVED_WORDS = Set.of(
            "TAGS",
            "BEGIN",
            "SEQUENCE",
            "INTEGER",
            "DATE",
            "END"
    );
    public static final Map<Character, String> CHAR_TO_TOKEN = Map.of(
            '{', "LCURLY",
            '}', "RCURLY",
            ',', "COMMA",
            '(', "LPAREN",
            ')', "RPAREN",
            '|', "BAR"
    );

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java LexicalAnalyzer <filename>");
            System.exit(1);
        }

        String filename = args[0];
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                analyzeLine(line);
            }

            if (successful_read) System.out.println("\nSUCCESS");
            else System.out.println("\nFAIL");


        } catch (IOException e) {
            successful_read = false;
            System.out.println("\nFAIL");
            throw new RuntimeException(e);
        }
    }

    private static void analyzeLine(String line) {
        int index = 0;
        while (index < line.length()) {
            char c = line.charAt(index);

            // skip whitespace
            if (Character.isWhitespace(c)) {
                index++;
                continue;
            }

            // check for assignment operator
            if (isAssignmentOperator(line, index)) {
                System.out.println("TOKEN: ASSIGNMENT(\"::=\")");
                index += 3;
            }
            // check for TypeReference (starts with an uppercase letter)
            else if (Character.isLetter(c) && Character.isUpperCase(c)) {
                String typeReference = readTypeReference(line, index);
                if (typeReference != null) {
                    if (isReservedWord(typeReference)) {
                        System.out.println("RESERVED_WORD(\"" + typeReference + "\")");
                    }
                    else {
                        System.out.println("TOKEN: TYPEREFERENCE(\"" + typeReference + "\")");
                    }
                    index += typeReference.length();
                }
                else {
                    System.out.println("Error: Invalid type reference at index " + index);
                    successful_read = false;
                    break;
                }
            }
            // check for identifier (starts with lowercase letter)
            else if (Character.isLetter(c) && Character.isLowerCase(c)) {
                String identifier = readIdentifier(line, index);
                if (identifier != null) {
                    System.out.println("TOKEN: IDENTIFIER(\"" + identifier + "\")");
                    index += identifier.length();
                }
                else {
                    System.out.println("Error: Invalid identifier at index " + index);
                    successful_read = false;
                    break;
                }
            }
            // check for number (starts with a digit)
            else if (Character.isDigit(c)) {
                String number = readNumber(line, index);
                System.out.println("TOKEN: NUMBER(" + number + ")");
                index += number.length();
            }
            // check for range separator
            else if (isRangeSeparator(line, index)) {
                System.out.println("TOKEN: RANGE_SEPARATOR(\"..\")");
                index += 2;
            }
            // check for tokens like (, ), {, }, (comma), or |
            else if (CHAR_TO_TOKEN.containsKey(c)) {
                System.out.println("TOKEN: " + CHAR_TO_TOKEN.get(c) + "(\"" + c + "\")");
                index++;
            }
            else {
                System.out.println("Error: Invalid token '" + c + "' at index " + index);
                successful_read = false;
                index++;
            }
        }
    }

    private static String readTypeReference(String line, int index) {
        StringBuilder typeReference = new StringBuilder();
        typeReference.append(line.charAt(index++));  // already validated first character as uppercase
        boolean prevCharWasHypen = false;  // track if previous character was '-'

        while (index < line.length()) {
            char cur = line.charAt(index);

            // valid chars: letter, digit, or hyphen
            if (Character.isLetterOrDigit(cur)) {
                typeReference.append(cur);
                prevCharWasHypen = false;
            }
            else if (cur == '-') {
                if (prevCharWasHypen) {
                    // two consecutive hyphens not allowed
                    return null;
                }
                typeReference.append(cur);
                prevCharWasHypen = true;
            }
            else {
                break;  // invalid character encountered so break
            }
            index++;
        }

        // check if last character is a hyphen (which is not allowed)
        if (typeReference.charAt(typeReference.length() - 1) == '-') {
            return null;
        }
        return typeReference.toString();
    }

    private static String readIdentifier(String line, int index) {
        StringBuilder identifier = new StringBuilder();
        identifier.append(line.charAt(index++));   // already verified that first letter is lowercase
        boolean prevCharWasHyphen = false;

        while (index < line.length()) {
            char cur = line.charAt(index);
            if (Character.isLetterOrDigit(cur)) {
                identifier.append(cur);
                prevCharWasHyphen = false;
            }
            else if (cur == '-') {
                if (prevCharWasHyphen) {
                    return null;
                }
                identifier.append(cur);
                prevCharWasHyphen = true;
            }
            else {
                break;
            }
            index++;
        }

        // check if last character is a hyphen (not allowed)
        if (identifier.charAt(identifier.length() - 1) == '-') {
            return null;
        }
        return identifier.toString();
    }

    private static String readNumber(String line, int index) {
        StringBuilder number = new StringBuilder();

        while (index < line.length() && Character.isDigit(line.charAt(index))) {
            char cur = line.charAt(index++);
            number.append(cur);
        }
        return number.toString();
    }

    private static boolean isAssignmentOperator(String line, int index) {
        // check that there are at least 2 more characters in the line
        if (index + 2 < line.length()) {
            return line.charAt(index) == ':' && line.charAt(index+1) == ':' && line.charAt(index+2) == '=';
        }
        return false;
    }

    private static boolean isRangeSeparator(String line, int index) {
        if (index + 1 < line.length()) {
            return line.charAt(index) == '.' && line.charAt(index+1) == '.';
        }
        return false;
    }

    private static boolean isReservedWord(String typeReference) {
        return RESERVED_WORDS.contains(typeReference);
    }
}
