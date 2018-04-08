package digitalbug.com.github.parser;

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class WordReadStream implements Closeable {
	private FileInputStream is;
	private byte[] buffer = new byte[2048];
	private int readLen = 0;
	private CharType lastType = CharType.OTHER;
	private List<Byte> lastWord = new LinkedList<>();
	private int i = 0;

	public WordReadStream(String fileName) throws FileNotFoundException {
		is = new FileInputStream(fileName);
	}

	public Pair<CharType, byte[]> readWord() throws IOException {
		while (i < readLen || (readLen = is.read(buffer)) > 0) {
			for (; i < readLen; i++) {
				char c = (char) buffer[i];
				CharType thisType = CharType.findCharType(c);

				if (lastWord.size() > 0) {
					if (lastType != thisType || !thisType.isGrouping()) {
						Pair<CharType, byte[]> result = new ImmutablePair<>(lastType,
								ArrayUtils.toPrimitive(lastWord.toArray(new Byte[lastWord.size()])));
						lastType = thisType;
						lastWord = new LinkedList<>();
						return result;
					}
				} else {
					lastType = thisType;
				}
				lastWord.add(buffer[i]);
			}

			i = 0;
			readLen = 0;
		}
		if (lastWord.size() > 0) {
			Pair<CharType, byte[]> result = new ImmutablePair<>(lastType,
					ArrayUtils.toPrimitive(lastWord.toArray(new Byte[lastWord.size()])));
			lastWord = new LinkedList<>();
			return result;
		}
		return null;
	}

	@Override
	public void close() throws IOException {
		is.close();
	}
}
