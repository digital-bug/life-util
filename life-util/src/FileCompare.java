import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

public class FileCompare {
	public static void main(String[] args) throws IOException {
		File dir = new File("D:\\10_외부 프로젝트\\");

		List<File> targets = new LinkedList<>();
		searchFiles(dir, targets);

		int cnt = targets.size();
		for (int i = 0; i < cnt; i++) {
			for (int j = i + 1; j < cnt; j++) {
				File f1 = targets.get(i);
				File f2 = targets.get(j);
				if (!getExt(f1).equals(getExt(f2)))
					continue;
				if (f1.length() != f2.length())
					continue;
				if (!equalFiles(f1, f2))
					continue;
				System.out.println(f1.getAbsolutePath() + " / " + f2.getAbsolutePath());
			}
		}
	}

	public static void searchFiles(File dir, List<File> targets) {
		File[] files = dir.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				searchFiles(files[i], targets);
			} else {
				targets.add(files[i]);
			}
		}
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
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}
}
