package digitalbug.com.github.parser;

import java.util.Arrays;
import java.util.function.Predicate;

public enum CharType {
	DUALBYTES(c -> (c & 0x80) > 0, true), // 2����Ʈ ����
	ALPHABET(c -> (65 <= c && c <= 90) || (97 <= c && c <= 122), true), // ������
	NUMERICS(Character::isDigit, true), // ����
	UNDER_SCORE(c -> c == '_', true), //
	DOLLAR(c -> c == '$', false), // ����� ����
	AT(c -> c == '@', false), // ����� ����

	RETURNS(c -> c == '\r' || c == '\n', true), // return
	COMMA(c -> c == ',', false), //
	WHATESPACES(Character::isWhitespace, true), // ����

	SEMICOLON(c -> c == ';', false), //
	COLON(c -> c == ':', false), //

	OP_DIV(c -> c == '/', false), //
	OP_MULTI(c -> c == '*', false), //
	OP_PLUS(c -> c == '+', false), //
	OP_MINUS(c -> c == '-', false), //
	OP_EQ(c -> c == '=', false), //
	OP_OR(c -> c == '|', false), //
	OP_AND(c -> c == '&', false), //
	OP_NOT(c -> c == '!', false), //
	OP_DOT(c -> c == '.', false), //
	OP_MOD(c -> c == '%', false), //

	OPEN_PARENTHESES(c -> c == '(', false), //
	CLOSE_PARENTHESES(c -> c == ')', false), //
	OPEN_CURLY_BRACE(c -> c == '{', false), //
	CLOSE_CURLY_BRACE(c -> c == '}', false), //
	OPEN_SQUARE_BRACKET(c -> c == '[', false), //
	CLOSE_SQUARE_BRACKET(c -> c == ']', false), //

	DBL_QUOTE(c -> c == '"', false), //
	SGL_QUOTE(c -> c == '\'', false), //

	LETTER(Character::isLetter, false), //
	OTHER(c -> false, false);

	/**
	 * ���� �Ǻ� �Լ�
	 */
	private Predicate<Character> predict;
	/**
	 * ���� ���� ���� �׷��� ����
	 */
	private boolean isGrouping;

	private CharType(Predicate<Character> p, boolean isGrouping) {
		this.predict = p;
		this.isGrouping = isGrouping;
	}

	public static CharType findCharType(char c) {
		return Arrays.stream(CharType.values()).filter(x -> x.predict.test(c)).findAny().orElse(CharType.OTHER);
	}

	public boolean isGrouping() {
		return isGrouping;
	}
}
