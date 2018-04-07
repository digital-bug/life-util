import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

public class Parser {
	// interface SentenceStateEventListener {
	// public default SentenceStateEventListener onFollowDualbytes() {
	// return this;
	// }
	//
	// public default SentenceStateEventListener onFollowLowercases() {
	// return this;
	// }
	//
	// public default SentenceStateEventListener onFollowUPPERCASES() {}
	//
	// public default SentenceStateEventListener onFollowNUMERICS() {}
	//
	// public default SentenceStateEventListener onFollowUNDER_SCORE() {}
	//
	// public default SentenceStateEventListener onFollowAT() {}
	//
	// public default SentenceStateEventListener onFollowRETURNS(){}
	// public default SentenceStateEventListener onFollowCOMMA(){}
	// public default SentenceStateEventListener onFollowWHATESPACES(){}
	// public default SentenceStateEventListener onFollowSEMICOLON(){}
	// public default SentenceStateEventListener onFollowCOLON(){}
	// public default SentenceStateEventListener onFollowOP_DIV(){}
	// public default SentenceStateEventListener onFollowOP_MULTI(){}
	// public default SentenceStateEventListener onFollowOP_PLUS(){}
	// public default SentenceStateEventListener onFollowOP_MINUS(){}
	// public default SentenceStateEventListener onFollowOP_EQ(){}
	// public default SentenceStateEventListener onFollowOP_OR(){}
	// public default SentenceStateEventListener onFollowOP_AND(){}
	// public default SentenceStateEventListener onFollowOP_NOT(){}
	// public default SentenceStateEventListener onFollowOP_DOT(){}
	// public default SentenceStateEventListener onFollowOPEN_PARENTHESES(){}
	// public default SentenceStateEventListener onFollowCLOSE_PARENTHESES(){}
	// public default SentenceStateEventListener onFollowOPEN_CURLY_BRACE(){}
	// public default SentenceStateEventListener onFollowCLOSE_CURLY_BRACE(){}
	// public default SentenceStateEventListener onFollowOPEN_SQUARE_BRACKET(){}
	// public default SentenceStateEventListener onFollowCLOSE_SQUARE_BRACKET(){}
	// public default SentenceStateEventListener onFollowDBL_QUOTE(){}
	// public default SentenceStateEventListener onFollowSGL_QUOTE(){}
	// public default SentenceStateEventListener onFollowLETTER(){}
	// public default SentenceStateEventListener onFollowOTHER(){}
	// }
	//
	// enum State implements SentenceStateEventListener {
	//
	// }
	// initialState {
	// @Override
	// public void onEventX() {
	// // do whatever
	// }
	// // same for other events
	// },
	// // same for other states
	// }
	//
	// class StateMachine implements StateEventListener {
	// State currentState;
	//
	// @Override
	// public void onEventX() {
	// currentState.onEventX();
	// }
	//
	// @Override
	// public void onEventY(int x, int y) {
	// currentState.onEventY(x, y);
	// }
	//
	// }
	//
	// class StateMachine2 {
	// State currentState;
	//
	// final StateEventListener stateEventPublisher = buildStateEventForwarder();
	//
	// StateEventListener buildStateEventForwarder() {
	// Class<?>[] interfaces = { StateEventListener.class };
	// return (StateEventListener)
	// Proxy.newProxyInstance(getClass().getClassLoader(), interfaces,
	// new InvocationHandler() {
	// @Override
	// public Object invoke(Object proxy, Method method, Object[] args) throws
	// Throwable {
	// try {
	// return method.invoke(currentState, args);
	// } catch (InvocationTargetException e) {
	// throw e.getCause();
	// }
	// }
	// });
	// }
	// }

	private static enum PhraseType {
		INLINE_COMMENT(PhraseType::handlerInlineComment), //

		OP_DIV(PhraseType::handlerOpDiv), //

		OTHER(PhraseType::handlerOther); //

		private Function<CharType, PhraseType> followHandler;

		private PhraseType(Function<CharType, PhraseType> followHandler) {
			this.followHandler = followHandler;
		}

		public PhraseType onFollow(CharType ct) {
			return this.followHandler.apply(ct);
		}

		private static PhraseType handlerInlineComment(CharType ct) {
			switch (ct) {
			case RETURNS:
				return OTHER;
			default:
				return INLINE_COMMENT;
			}
		}

		private static PhraseType handlerOpDiv(CharType ct) {
			switch (ct) {
			case OP_DIV:
				return INLINE_COMMENT;
			default:
				return OTHER;
			}
		}

		private static PhraseType handlerOther(CharType ct) {
			switch (ct) {
			case OP_DIV:
				return OTHER;
			default:
				return INLINE_COMMENT;
			}
		}
	}

	private static enum CharType {
		DUALBYTES(c -> (c & 0x80) > 0, true), // 2바이트 문자
		LOWERCASES(Character::isLowerCase, true), // 소문자
		UPPERCASES(Character::isUpperCase, true), // 대문자
		NUMERICS(Character::isDigit, true), // 숫자
		UNDER_SCORE(c -> c == '_', true), //
		AT(c -> c == '@', false),

		RETURNS(c -> c == '\r' || c == '\n', true), // return
		COMMA(c -> c == ',', false), //
		WHATESPACES(Character::isWhitespace, true), // 공백

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
		 * 문자 판별 함수
		 */
		private Predicate<Character> predict;
		/**
		 * 동일 종류 문자 그룹핑 여부
		 */
		private boolean grouping;

		private CharType(Predicate<Character> p, boolean grouping) {
			this.predict = p;
			this.grouping = grouping;
		}

		public static CharType findCharType(char c) {
			return Arrays.stream(CharType.values()).filter(x -> x.predict.test(c)).findAny().orElse(CharType.OTHER);
		}

		public boolean isGrouping() {
			return grouping;
		}

	}

	private static class StreamWordGetter {
		private class StreamTracker implements Runnable {
			private InputStream is;

			public StreamTracker(InputStream is) {
				this.is = is;
			}

			@Override
			public void run() {
				try {
					byte[] buffer = new byte[2048];
					int readLen = 0;
					CharType lastType = null;
					List<Byte> lastWord = new LinkedList<>();
					while ((readLen = is.read(buffer)) > 0) {
						for (int i = 0; i < readLen; i++) {
							char c = (char) buffer[i];
							CharType thisType = CharType.findCharType(c);

							if (lastWord.size() > 0) {
								if (lastType != thisType || !thisType.isGrouping()) {
									// printWord(lastWord, lastType);
									lastType = thisType;
									lastWord = new LinkedList<>();
								}
							} else {
								lastType = thisType;
							}
							lastWord.add(buffer[i]);
						}
						if (lastWord.size() > 0) {
							printWord(lastWord, lastType);
						}
						break;
					}
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}

		public StreamWordGetter(InputStream is) {
			StreamTracker streamTracker = new StreamTracker(is);
			Thread t = new Thread(streamTracker);
			t.start();
		}

		public Pair<CharType, char[]> getNextWord() {
			
			/********/
			// toDo: next job from here. i have to concern thread and blocking.
			/********/
			
			return null;
		}
	}

	public static void main(String[] args) {
		if (args.length < 1) {
			throw new RuntimeException("Usage - args: a file name for parsing");
		}

		for (int j = 0; j < args.length; j++) {
			try (InputStream is = new FileInputStream(args[j]);) {
				StreamWordGetter streamWordGetter = new StreamWordGetter(is);
				Pair<CharType, char[]> word = null;
				PhraseType lastPType = PhraseType.OTHER;
				while ((word = streamWordGetter.getNextWord()) != null) {
					lastPType = lastPType.onFollow(word.getLeft());
					word.getRight();
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static void main1(String[] args) {
		if (args.length < 1) {
			throw new RuntimeException("Usage - args: a file name for parsing");
		}

		for (int j = 0; j < args.length; j++) {
			try (InputStream is = new FileInputStream(args[j]);) {
				byte[] buffer = new byte[2048];
				int readLen = 0;
				CharType lastType = null;
				List<Byte> lastWord = new LinkedList<>();
				PhraseType lastPType = PhraseType.OTHER;
				while ((readLen = is.read(buffer)) > 0) {
					for (int i = 0; i < readLen; i++) {
						char c = (char) buffer[i];
						CharType thisType = CharType.findCharType(c);

						if (lastWord.size() > 0) {
							if (lastType != thisType || !thisType.isGrouping()) {
								// printWord(lastWord, lastType);
								lastPType = lastPType.onFollow(lastType);
								lastType = thisType;
								lastWord = new LinkedList<>();
							}
						} else {
							lastType = thisType;
						}
						lastWord.add(buffer[i]);
					}
					if (lastWord.size() > 0) {
						printWord(lastWord, lastType);
					}
					break;
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 기본 문자형 데이터를 포멧에 맞춰 출력
	 * 
	 * @param lastWord
	 *            마지막 단어
	 * @param lastType
	 *            마지막 데이터 타입
	 */
	private static void printWord(List<Byte> lastWord, CharType lastType) {
		byte[] tempLastWord = ArrayUtils.toPrimitive(lastWord.toArray(new Byte[lastWord.size()]));
		System.out.println(
				"<" + lastType.toString() + ">\t" + new String(tempLastWord) + "\t" + bytesToHex(tempLastWord));
	}

	/**
	 * byte type을 Hex문자열로 바꾸기 위한 데이터 배열
	 */
	private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

	/**
	 * byte 배열을 Hex문자열로 변경
	 * 
	 * @param arrB
	 *            변환할 byte 배열
	 * @return Hex문자열
	 */
	private static String bytesToHex(byte[] arrB) {
		char[] result = new char[arrB.length * 3];
		for (int i = 0; i < arrB.length; i++) {
			int v = arrB[i] & 0xFF;
			result[i * 3] = hexArray[v >>> 4];
			result[i * 3 + 1] = hexArray[v & 0x0F];
			result[i * 3 + 2] = ' ';
		}
		return "0x" + new String(result);
	}

	/**
	 * byte문자를 Hex 문자열로 변경
	 * 
	 * @param b
	 *            byte 값
	 * 
	 * @return Hex문자열
	 */
	public static String byteToHex(byte b) {
		int v = b & 0xFF;
		return new String(new char[] { '0', 'x', hexArray[v >>> 4], hexArray[v & 0x0F] });
	}
}
