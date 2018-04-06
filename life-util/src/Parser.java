import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.ArrayUtils;

public class Parser {
	public static enum CharType {
		ALPHABET, NUMERIC, WHATESPACE, OTHER
	}

	public static void main(String[] args) {
		if (args.length < 1) {
			throw new RuntimeException("Usage - arg1: a file name for parsing");
		}

		try (InputStream is = new FileInputStream(args[0]);) {
			byte[] buffer = new byte[2048];
			int readLen = 0;
			CharType lastType = null;
			List<Byte> lastWord = new LinkedList<>();
			while ((readLen = is.read(buffer)) > 0) {
				for (int i = 0; i < readLen; i++) {
					char c = (char) buffer[i];
					CharType thisType;
					if (Character.isDigit(c)) {
						thisType = CharType.NUMERIC;
					} else if (Character.isWhitespace(c)) {
						thisType = CharType.WHATESPACE;
					} else if (Character.isLetter(c)) {
						thisType = CharType.ALPHABET;
					} else {
						thisType = CharType.OTHER;
					}

					if (lastWord.size() > 0) {
						if (lastType != thisType || thisType == CharType.OTHER) {
							Byte[] tempLastWord = lastWord.toArray(new Byte[lastWord.size()]);
							byte[] tempLastWord2 = ArrayUtils.toPrimitive(tempLastWord);
							System.out.println(bytesToHex(tempLastWord2) + "\t" + new String(tempLastWord2) + " <"
									+ lastType.toString() + ">");
							lastType = thisType;
							lastWord = new LinkedList<>();
						}
					} else {
						lastType = thisType;
					}
					lastWord.add(buffer[i]);
				}
				if (lastWord.size() > 0) {
					Byte[] tempLastWord = lastWord.toArray(new Byte[lastWord.size()]);
					byte[] tempLastWord2 = ArrayUtils.toPrimitive(tempLastWord);
					System.out.println(bytesToHex(tempLastWord2) + "\t" + new String(tempLastWord2) + " <"
							+ lastType.toString() + ">");
				}
				break;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

	private static String bytesToHex(byte[] tempLastWord2) {
		char[] result = new char[tempLastWord2.length * 2];
		for (int i = 0; i < tempLastWord2.length; i++) {
			int v = tempLastWord2[i] & 0xFF;
			result[i * 2] = hexArray[v >>> 4];
			result[i * 2 + 1] = hexArray[v & 0x0F];
		}
		return "0x" + new String(result);
	}

	public static String byteToHex(byte b) {
		int v = b & 0xFF;
		return new String(new char[] { hexArray[v >>> 4], hexArray[v & 0x0F] });
	}
}
