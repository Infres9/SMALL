/*
 * This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

 /*
 * This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package sal.small;

import sal.util.*;

import java.io.BufferedReader;
import java.io.IOException;
import static sal.util.RE.*;

/**
 * This file contains the definition of the enum Token.
 * 
 * @author Simon
 * @author Azarias
 */
public enum Token implements Patterned {

    EOF,
    UNMATCHED,
    NUMBER(oneOf(some(DEC), "#" + some(HEX)), "<number>"),
    TYPE_INT("entier"),
    TYPE_STRING("chaine"),
    IDENTIFIER(ALPHA + any(in("A-Za-z0-9_" + RE.DOLLAR)), "<identifier>"),
    STRING(DQUOTE + any(BS + WILD, notIn(DQUOTE)) + DQUOTE, "<string>"),
    IF("si"), THEN("alors"), ELSE("sinon"), ELIF("sinonsi"), END("fin"),
    WHILE("tantque"), DO("faire"), UNTIL("jusqua"), FOR("pour"), SWITCH("aiguiller"), CASE("cas"), DEFAULT("defaut"),
    FUNCTION("fonction"), VOID("vide"),
    TRUE("vrai"),
    FALSE("faux"),
    HALT("arreter"),
    PRINT("ecrire"),
    MINIMUM("min"),
    MAXIMUM("max"),
    READ("lire"),
    ASSIGN("=" + RE.notBefore("="), "="),
    BREAK("rompre"),
    CONTINUE("continuer"),
    EQ("=="), NE("!="),
    GT(">" + notBefore(">", "="), ">"), GE(">="),
    LT("<" + notBefore("<", "="), "<"), LE("<="),
    SHL("<<"),
    SHR(">>" + notBefore(">"), ">>"),
    SHRS(">>>"),
    PLUS(RE.PLUS + notBefore(RE.PLUS), "+"), MINUS(RE.MINUS + notBefore("-"), "-"), NEGATE(null, "-"), TIMES(RE.STAR, "*"), DIVIDE("/"), MOD("%"),
    INCREMENT(RE.PLUS + RE.PLUS, "++"),
    DECREMENT(RE.MINUS + RE.MINUS, "--"),
    // punctuation ...
    SEMICOLON(";"), COMMA(","), LP(RE.LPAR, "("), RP(RE.RPAR, ")"), COLON(RE.COLON, ":"), QUERY(RE.QUERY, "?"), LSQ(RE.LSQ, "["), RSQ(RE.RSQ, "]"),
    // tokens used to represent syn tax features ...
    STATEMENTLIST, BLOCK, // lists of statements

    // tokens to annotate the parse tree with extra information
    TO_STR("achaine"), // int to string
    TO_INT("aentier"), // convert string to int
    LENGTH("lon"), // string length
    PRINT_STR, // print a string
    PRINT_INT, // print an int
    READ_STR, // read a string
    CONCAT, // join strings
    RIGHT_STR, // select rightmost chars
    LEFT_STR, // leftmost chars
    FORMAT_STR, // format a string
    FORMAT_INT, // format an int
    COMPARE_STR, // compare strings
    NEW_ARRAY, // Create array
    ARRAY_LENGTH,
    // for internal use - please don't change!
    ZERO, ONE, SWAP;

    //////////// end of constants //////////////////////////
    String pattern;     // REGEX used to match token
    String asText;      // text used to display token

    Token(String pattern, String text) {
        this.pattern = pattern;
        this.asText = text;
    }

    Token(String pattern) {
        this(pattern, true);
    }

    Token(String pattern, boolean isOwnText) {
        this(pattern, null);
        this.asText = isOwnText ? pattern : this.toString();
    }

    Token() {
        this(null, false);
    }

    public String pattern() {
        return pattern;
    }

    public String asText() {
        return asText;
    }

    /**
     * Initialise the lexer to take input from a BufferedReader via calls to
     * scan.
     *
     * @param inputReader
     */
    static public void startLexer(BufferedReader inputReader) {
        lexer.input(inputReader);
    }

    /**
     * Create the Lexer to be used by this program.
     *
     */
    static private final Lexer<Token> lexer = new Lexer(EOF, UNMATCHED, IDENTIFIER)
            .whiteSpace(WS, comment("//"));

    /**
     * Static version of {@link Lexer#currentText()   Lexer.currentText()}}
     *
     * @return the text of the current token
     */
    static public String currentText() {
        return lexer.currentText();
    }

    /**
     * Static version of {@link Lexer#currentToken()   Lexer.currentToken()}}
     *
     * @return the text of the current token
     */
    static public Token currentToken() {
        return lexer.currentToken();
    }

    /**
     * * static variant of the {@link sal.util.Lexer#scan scan} method in
     * {@link sal.util.Lexer Lexer}.
     *
     * @return the current token.
     *
     * scan calls the default lexer and acts on error cases (IOException or
     * unmatched input) by printing an error message.
     *
     */
    static public Token scan() {
        if (lexer.scan() == UNMATCHED) {
            // first check for I/O error
            IOException err = lexer.ioException();
            if (err != null) {
                parseError("I/O Exception: %s\n", err.getMessage());
            } else {
                CharView buff = lexer.tokenInLine();
                char errch = buff.charAt(0);
                String errStr;
                // an unmatched token is always 1 character long
                if (errch < ' ' || errch > '~') {
                    errStr = String.format("\\u%04x", (int) errch);
                } else {
                    errStr = buff.toString();
                }
                parseError("Unexpected character \'" + errStr + "\'\n");
            }
            scan(); // call recursively after error
        }
        return lexer.currentToken();
    }

    /**
     * Check that the current token is as expected.
     *
     * @param tokens symbol to ignore if found: good for lazy languages where,
     * maybe, the 'do' in {@code while ... do} is optional.
     * @return true if the expected token was fount, false otherwise.
     */
    static public boolean tokenIn(Token... tokens) {
        return currentToken().isIn(tokens);
    }

    /**
     * Skip any token in the given list.
     *
     * @param tokens list of tokens, any one of which can be skipped.
     * @return true if a token was skipped, false otherwise.
     */
    static public boolean skipToken(Token... tokens) {
        boolean check = currentToken().isIn(tokens);
        if (check) {
            scan();
        }
        return check;
    }

    /**
     * Check that the current token is as specified - produce an error message
     * if not found.
     *
     * @param tokens symbol to check for.
     */
    static public boolean mustBe(Token... tokens) {
        boolean check = skipToken(tokens);
        if (!check) {
            parseError("Found %s when expecting %s\n", lexer.currentText(), Patterned.expected(tokens));
        }
        return check;
    }

    /**
     * Log an error messages during parsing stage.
     *
     * @param format format string for call to printf.
     * @param args arguments to format string.
     *
     * This method is placed here only because (usually) it is the wrong token
     * which causes an error!
     */
    static public void parseError(String format, Object... args) {
        ErrorStream.log(lexer.lineNumber(), lexer.tokenInLine(), format, args);
    }
}
