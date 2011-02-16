import java.io.*;
import java.util.*;

public class generateInput {
	public static void main(String[] args) throws IOException {
		File file = new File("input");
		file.createNewFile();
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		int count = 60000;
		while(count > 0) {
			int index = count;
			StringBuilder sb = new StringBuilder();
			for(int i = 0; i < index; i++) {
				sb.append('s');
			}
			bw.write(sb.toString() + '\n');
			count--;
		}
		bw.close();
	}
}
