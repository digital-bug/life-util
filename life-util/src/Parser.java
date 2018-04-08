import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import digitalbug.com.github.parser.CharType;
import digitalbug.com.github.parser.WordReadStream;

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
		INLINE_COMMENT(PhraseType::handlerInlineComment, true), //
		MULTILINE_COMMENT0(PhraseType::handlerMultiLineComment, true), //
		MULTILINE_COMMENT1(PhraseType::handlerMultiLineCommentEnd, true), //
		MULTILINE_COMMENT2(PhraseType::handlerMultiLineCommentEnded, true), //

		IDENTIFIER(PhraseType::handlerIdentifier, false), //

		OP_ASSIGN(PhraseType::handlerOpAssign, false), //
		OP_DIV(PhraseType::handlerOpDiv, false), //

		OP_EQ(PhraseType::handlerOpEq, true), //
		OP_EQ_TYPE(PhraseType::handlerOpEqType, true), //

		OTHER(PhraseType::handlerOther, false); //

		private Function<CharType, PhraseType> followHandler;
		private boolean isPreConnect;

		private PhraseType(Function<CharType, PhraseType> followHandler, boolean isPreConnect) {
			this.followHandler = followHandler;
			this.isPreConnect = isPreConnect;
		}

		public boolean isPreConnect() {
			return isPreConnect;
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

		private static PhraseType handlerMultiLineComment(CharType ct) {
			switch (ct) {
			case OP_MULTI:
				return MULTILINE_COMMENT1;
			default:
				return MULTILINE_COMMENT0;
			}
		}

		private static PhraseType handlerMultiLineCommentEnd(CharType ct) {
			switch (ct) {
			case OP_DIV:
				return MULTILINE_COMMENT2;
			default:
				return MULTILINE_COMMENT0;
			}
		}

		private static PhraseType handlerMultiLineCommentEnded(CharType ct) {
			switch (ct) {
			default:
				return OTHER;
			}
		}

		private static PhraseType handlerOpDiv(CharType ct) {
			switch (ct) {
			case OP_DIV:
				return INLINE_COMMENT;
			case OP_MULTI:
				return MULTILINE_COMMENT0;
			case OP_EQ:
				return OTHER;
			default:
				return OTHER;
			}
		}

		private static PhraseType handlerOther(CharType ct) {
			switch (ct) {
			case OP_DIV:
				return OP_DIV;
			case UNDER_SCORE:
				return IDENTIFIER;
			case DOLLAR:
				return IDENTIFIER;
			case ALPHABET:
				return IDENTIFIER;
			case OP_EQ:
				return OP_ASSIGN;
			default:
				return OTHER;
			}
		}

		private static PhraseType handlerIdentifier(CharType ct) {
			switch (ct) {
			case UNDER_SCORE:
				return IDENTIFIER;
			case DOLLAR:
				return IDENTIFIER;
			case NUMERICS:
				return IDENTIFIER;
			case ALPHABET:
				return IDENTIFIER;
			case OP_EQ:
				return OP_ASSIGN;
			default:
				return OTHER;
			}
		}

		private static PhraseType handlerOpAssign(CharType ct) {
			switch (ct) {
			case OP_EQ:
				return OP_EQ;
			default:
				return OTHER;
			}
		}

		private static PhraseType handlerOpEq(CharType ct) {
			switch (ct) {
			case OP_EQ:
				return OP_EQ_TYPE;
			default:
				return OTHER;
			}
		}

		private static PhraseType handlerOpEqType(CharType ct) {
			switch (ct) {
			default:
				return OTHER;
			}
		}
	}

	private static class StreamWordGetter {

		private List<Pair<CharType, byte[]>> wordPair = new LinkedList<>();
		private Thread t;

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
									synchronized (StreamWordGetter.this) {
										if (wordPair.size() > 20) {
											try {
												this.wait();
											} catch (InterruptedException e) {
											}
										}
									}
									wordPair.add(new ImmutablePair<CharType, byte[]>(lastType,
											ArrayUtils.toPrimitive(lastWord.toArray(new Byte[lastWord.size()]))));
									wordPair.notify();

									lastType = thisType;
									lastWord = new LinkedList<>();
								}
							} else {
								lastType = thisType;
							}
							lastWord.add(buffer[i]);
						}
						if (lastWord.size() > 0) {
							synchronized (StreamWordGetter.this) {
								if (wordPair.size() > 20) {
									try {
										this.wait();
									} catch (InterruptedException e) {
									}
								}
							}
							wordPair.add(new ImmutablePair<CharType, byte[]>(lastType,
									ArrayUtils.toPrimitive(lastWord.toArray(new Byte[lastWord.size()]))));
							wordPair.notify();
						}
					}
				} catch (Exception e) {
					throw new RuntimeException(e);
				} finally {
					wordPair.notify();
				}
			}
		}

		public StreamWordGetter(InputStream is) {
			StreamTracker streamTracker = new StreamTracker(is);
			t = new Thread(streamTracker);
			t.start();
		}

		public Pair<CharType, byte[]> getNextWord() {
			synchronized (this) {
				if (t.isAlive()) {
					if (wordPair.size() == 0) {
						try {
							wordPair.wait();
						} catch (InterruptedException e) {
						}
					}
				}
			}
			Pair<CharType, byte[]> word = wordPair.get(0);
			wordPair.remove(0);
			t.notify();

			return word;
		}
	}

	public static void main1(String[] args) {
		if (args.length < 1) {
			throw new RuntimeException("Usage - args: a file name for parsing");
		}

		for (int j = 0; j < args.length; j++) {
			try (InputStream is = new FileInputStream(args[j]);) {
				StreamWordGetter streamWordGetter = new StreamWordGetter(is);
				Pair<CharType, byte[]> word = null;
				PhraseType lastPType = PhraseType.OTHER;
				while ((word = streamWordGetter.getNextWord()) != null) {
					lastPType = lastPType.onFollow(word.getLeft());
					word.getRight();
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		if (args.length < 1) {
			throw new RuntimeException("Usage - args: a file name for parsing");
		}

		List<String> ReservedWords = Arrays.asList(new String[] { "abstract", "alert", "all", "anchor", "anchors",
				"area", "arguments", "Array", "assign", "await", "blur", "boolean", "break", "button", "byte", "case",
				"catch", "char", "checkbox", "class", "clearInterval", "clearTimeout", "clientInformation", "close",
				"closed", "confirm", "const", "constructor", "continue", "crypto", "Date", "debugger", "decodeURI",
				"decodeURIComponent", "default", "defaultStatus", "delete", "do", "document", "double", "element",
				"elements", "else", "embed", "embeds", "encodeURI", "encodeURIComponent", "enum", "escape", "eval",
				"event", "export", "extends", "fileUpload", "final", "finally", "float", "focus", "for", "form",
				"forms", "frame", "frameRate", "frames", "function", "goto", "hasOwnProperty", "hidden", "history",
				"if", "image", "images", "implements", "import", "in", "Infinity", "innerHeight", "innerWidth",
				"instanceof", "int", "interface", "isFinite", "isNaN", "isPrototypeOf", "layer", "layers", "length",
				"let", "link", "location", "long", "Math", "mimeTypes", "name", "NaN", "native", "navigate",
				"navigator", "new", "null", "Number", "Object", "offscreenBuffering", "onblur", "onclick", "onerror",
				"onfocus", "onkeydown", "onkeypress", "onkeyup", "onload", "onmousedown", "onmouseover", "onmouseup",
				"onsubmit", "open", "opener", "option", "outerHeight", "outerWidth", "package", "packages",
				"pageXOffset", "pageYOffset", "parent", "parseFloat", "parseInt", "password", "pkcs11", "plugin",
				"private", "prompt", "propertyIsEnum", "protected", "prototype", "public", "radio", "reset", "return",
				"screenX", "screenY", "scroll", "secure", "select", "self", "setInterval", "setTimeout", "short",
				"static", "status", "String", "submit", "super", "switch", "synchronized", "taint", "text", "textarea",
				"this", "throw", "throws", "top", "toString", "transient", "try", "typeof", "undefined", "unescape",
				"untaint", "valueOf", "var", "void", "volatile", "while", "window", "with", "yield", "false", "true" });

		for (int j = 0; j < args.length; j++) {
			try (WordReadStream wrs = new WordReadStream(args[j]);) {
				Pair<CharType, byte[]> readWord = null;
				PhraseType lastType = PhraseType.OTHER;
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				while ((readWord = wrs.readWord()) != null) {
					PhraseType thisType = lastType.onFollow(readWord.getLeft());
					if (baos.size() > 0) {
						if (lastType != thisType) {
							if (thisType.isPreConnect()) {
								lastType = thisType;
							} else {
								byte[] bytes = baos.toByteArray();
								if (lastType == PhraseType.IDENTIFIER && ReservedWords.contains(new String(bytes)))
									System.out.println("<Reserved Words>" + new String(bytes));
								else
									System.out.println("<" + lastType + ">" + new String(bytes));
								lastType = thisType;
								baos = new ByteArrayOutputStream();
							}
						}
					} else {
						lastType = thisType;
					}
					baos.write(readWord.getRight());
				}
				if (baos.size() > 0) {
					byte[] bytes = baos.toByteArray();
					System.out.println("<" + lastType + ">" + new String(bytes));
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
	 * @param bs
	 *            마지막 단어
	 * @param lastType
	 *            마지막 데이터 타입
	 */
	private static void printWord(byte[] lastWord, CharType lastType) {
		if (lastType != CharType.OTHER || lastType != CharType.LETTER)
			return;
		System.out.println("<" + lastType + ">\t" + new String(lastWord) + "\t" + bytesToHex(lastWord));
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
