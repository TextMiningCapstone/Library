import java.util.*;
import java.io.*;

/**
 * This algorithm is using external sort to sort large data file exceed
 * computer's memory. To do that, we need to use merge sort and save some
 * temporary files.
 */
public class sortme {
	/**
	 * Main function to do external sort
	 * 
	 * @param args
	 *            arg[0] path of input file arg[1] path of output file
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		if (args.length < 2) {
			System.out.println("please provide input and output file names");
			return;
		}
		String inputfile = args[0];
		String outputfile = args[1];
		Comparator<String> comparator = new Comparator<String>() {
			public int compare(String r1, String r2) {
				return countS(r1) - countS(r2);
			}
		};
		List<File> l = sortInBatch(new File(inputfile), comparator);
		mergeSortedFiles(l, new File(outputfile), comparator);
	}

	/**
	 * Automatically get the suitable size for each temporary file The size of
	 * temporary file should not be more than half of JVM free memory.
	 * 
	 * @param filetobesorted
	 *            Input File
	 * @return Size of each temporary file
	 */
	private static long estimateBestSizeOfBlocks(File filetobesorted) {
		long sizeoffile = filetobesorted.length();
		// Input file is usually 100x of memory, so I initialize the max file
		// number as 200
		final int MAXTEMPFILES = 200;
		long blocksize = sizeoffile / MAXTEMPFILES;
		long freemem = Runtime.getRuntime().freeMemory();
		if (blocksize < freemem / 2)
			blocksize = freemem / 2;
		else {
			if (blocksize >= freemem)
				System.err.println("Out of memory.");
		}
		System.out.println("blockSize: " + blocksize);
		return blocksize;
	}

	/**
	 * This method will split the original input file into many sorted temporary
	 * files
	 * 
	 * @param file
	 *            Input file
	 * @param cmp
	 *            Comparator to sort content in file
	 * @return Temporary files list
	 * @throws IOException
	 */
	private static List<File> sortInBatch(File file, Comparator<String> cmp)
			throws IOException {
		List<File> files = new ArrayList<File>();
		BufferedReader fbr = new BufferedReader(new FileReader(file));
		long blocksize = estimateBestSizeOfBlocks(file);
		List<String> tmplist = new ArrayList<String>();
		String line = "";
		while (line != null) {
			long currentblocksize = 0;
			while ((currentblocksize < blocksize)
					&& ((line = fbr.readLine()) != null)) {
				tmplist.add(line);
				currentblocksize += line.length();
			}
			files.add(sortAndSave(tmplist, cmp));
			tmplist.clear();
		}
		fbr.close();
		System.out.println("temporary files:: " + files.size());
		return files;
	}

	/**
	 * Sort each temporary file and save in memory
	 * 
	 * @param tmplist
	 *            Content in each temporary file
	 * @param cmp
	 *            Comparator
	 * @return File of sorted file
	 * @throws IOException
	 */
	private static File sortAndSave(List<String> tmplist, Comparator<String> cmp)
			throws IOException {
		Collections.sort(tmplist, cmp);
		File newtmpfile = File.createTempFile("externalSort", "tmpFile");
		newtmpfile.deleteOnExit();
		BufferedWriter fbw = new BufferedWriter(new FileWriter(newtmpfile));
		for (String r : tmplist) {
			fbw.write(r + '\n');
		}
		fbw.close();
		return newtmpfile;
	}

	/**
	 * Merge all temporary files
	 * 
	 * @param files
	 *            Temporary files
	 * @param outputfile
	 *            File to output
	 * @param cmp
	 *            Comparator
	 * @return The number of lines sorted
	 * @throws IOException
	 */
	public static int mergeSortedFiles(List<File> files, File outputfile,
			final Comparator<String> cmp) throws IOException {
		PriorityQueue<BinaryFileBuffer> pq = new PriorityQueue<BinaryFileBuffer>(
				10, new Comparator<BinaryFileBuffer>() {
					public int compare(BinaryFileBuffer i, BinaryFileBuffer j) {
						return cmp.compare(i.peek(), j.peek());
					}
				});
		for (File f : files) {
			BinaryFileBuffer bfb = new BinaryFileBuffer(f);
			pq.add(bfb);
		}
		BufferedWriter fbw = new BufferedWriter(new FileWriter(outputfile));
		int rowcounter = 0;
		try {
			while (pq.size() > 0) {
				BinaryFileBuffer bfb = pq.poll();
				String r = bfb.pop();
				fbw.write(r);
				fbw.newLine();
				++rowcounter;
				if (bfb.empty()) {
					bfb.fbr.close();
					bfb.originalfile.delete();
				} else {
					pq.add(bfb);
				}
			}
		} finally {
			fbw.close();
			for (BinaryFileBuffer bfb : pq)
				bfb.close();
		}
		return rowcounter;
	}

	/**
	 * Count how many 's' in a string
	 * 
	 * @param input
	 *            string
	 * @return Number of s to return
	 */
	private static int countS(String s) {
		int cnt = 0;
		for (char c : s.toCharArray())
			if (c == 's')
				cnt++;
		return cnt;
	}
}

/**
 * This is a buffer to store each 10MB data Its size can be manually changed
 *
 */
class BinaryFileBuffer {
	public static int BUFFERSIZE = 1024 * 1024;
	public BufferedReader fbr;
	public File originalfile;
	private String cache;
	private boolean empty;

	public BinaryFileBuffer(File f) throws IOException {
		originalfile = f;
		fbr = new BufferedReader(new FileReader(f), BUFFERSIZE);
		reload();
	}

	public boolean empty() {
		return empty;
	}

	private void reload() throws IOException {
		if ((this.cache = fbr.readLine()) == null) {
			empty = true;
			cache = null;
		} else {
			empty = false;
		}
	}

	public void close() throws IOException {
		fbr.close();
	}

	public String peek() {
		if (empty())
			return null;
		return cache.toString();
	}

	public String pop() throws IOException {
		String answer = peek();
		reload();
		return answer;
	}
}
