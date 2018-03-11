import java.util.stream.IntStream;

public class StreamTest {
	public static void main(String[] args) {
		IntStream.range(1, 10).skip(5).parallel().mapToObj(i -> i + ", ").forEach(System.out::print);
		System.out.println();
		IntStream.range(1, 10).parallel().skip(5).mapToObj(i -> i + ", ").forEach(System.out::print);
	}
}
