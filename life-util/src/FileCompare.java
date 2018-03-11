import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class FileCompare {
	public static void main(String[] args) throws IOException {
		final File dir = new File("C:\\Users\\home\\Desktop\\새 폴더\\혜진 핸드폰");

		final List<File> targets = searchFiles(dir);

		System.out.println(targets.size() + " files");

		targets.sort(new Comparator<File>() {

			@Override
			public int compare(File o1, File o2) {
				return o1.getAbsolutePath().compareTo(o2.getAbsolutePath());
			}
		});
		// final List<File> skipList = new LinkedList<>();
		System.out.println("Sorted");

		int cnt = targets.size();
		for (int i = 0; i < 100; i++) {
			File f1 = targets.get(i);
			// if (skipList.contains(f1))
			// continue;
			// for (int j = i + 1; j < cnt; j++) {
			final String f1Ext = getExt(f1);
			final long f1Size = f1.length();
			targets.stream().skip(i + 1).parallel().filter(f2 -> f1Ext.equals(getExt(f2)))
					.filter(f2 -> f1Size == f2.length()).filter(f2 -> equalFiles(f1, f2)).forEach(f2 -> {
						// System.out.println(f1.getAbsolutePath() + " / " + f2.getAbsolutePath());
						System.out.println("del \"" + f2.getAbsolutePath() + "\"");
						// skipList.add(f2);
					});
			System.out.println("move \"" + f1.getAbsolutePath() + "\" \""
					+ f1.getAbsolutePath().replace("혜진 핸드폰", "혜진 핸드폰2") + "\"");

		}
	}

	public static List<File> searchFiles(final File dir) {
		return Arrays.stream(dir.listFiles()).parallel().map(f -> {
			if (!f.isDirectory()) {
				List<File> result = new LinkedList<>();
				result.add(f);
				return result;
			}
			return searchFiles(f);
		}).flatMap(List::stream).collect(Collectors.toList());
	}

	public static String getExt(File file) {
		String fileName = file.getName();
		int pos = fileName.lastIndexOf(".");
		if (pos != -1) {
			return fileName.substring(pos + 1);
		}
		return "";
	}

	public static boolean equalFiles(File f1, File f2) {
		try (InputStream is1 = new FileInputStream(f1); InputStream is2 = new FileInputStream(f2)) {
			int i1 = -1;
			int i2 = -1;
			while ((i1 = is1.read()) != -1 && (i2 = is2.read()) != -1) {
				if (i1 != i2) {
					return false;
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
