import java.io.*;
import java.nio.file.*;
import java.util.regex.*;
import java.util.*;

public class BibFixer {

	private static final String PUBLICATIONS_TAG_OPEN = "<div id=\"refs\" class=\"references\">";
	private static final String PUBLICATIONS_TAG_CLOSE = "</div>";
	private static final String LINE_SEPARATOR = System.getProperty("line.separator");
	private static final String DESIRED_EMPH_NAME = "<b>Márton Búr</b>";

	public static void main(String[] args) throws IOException{
		/*
		 * Help print
		 */ 
		String fileName;
		if(args.length != 0){
			fileName = args[0];
		} else {
			fileName = null;
		}

		if (args.length == 0 || "-h".contentEquals(fileName) || "--help".contentEquals(fileName)){
			System.out.println("Usage: supply the filename of target html as parameter.");
			System.exit(0);
		}

		File f = new File(fileName);

		StringBuilder builder = new StringBuilder();

		/*
		 * Fixing and formatting my name
		 */ 

		try (BufferedReader r = Files.newBufferedReader(f.toPath())) {
			r.lines().forEach(s -> {
				builder.append(
					s
					.replace("Márton Búr", DESIRED_EMPH_NAME)
					.replace("———",DESIRED_EMPH_NAME)
					.replace("Búr, Márton", DESIRED_EMPH_NAME)).append(LINE_SEPARATOR);
			});
		}
		String newFileContents = builder.toString().replaceAll("<span class=\"citation\">.*</span>", "");

		/*
		 * Reorder publications and generate headers by year
		 */

		SortedMap<Integer, List<String>> publicationsByYear = new TreeMap<>();

		// Pattern to get divs
		String refSpecExpr = "(?s)(<div id=\"ref-((?!div).)*</div>)";
		
		// identify publication entries
		Pattern refPattern = Pattern.compile(refSpecExpr);
		Matcher refMatcher = refPattern.matcher(newFileContents);
		
		String yearSpecExpr = "([0-9][0-9][0-9][0-9])\\.";
		Pattern yearPattern = Pattern.compile(yearSpecExpr);
		while (refMatcher.find())
		{
			String reference = refMatcher.group(1);
			Matcher yearMatcher = yearPattern.matcher(reference);

			if(yearMatcher.find()){
				int publicationYear = Integer.valueOf(yearMatcher.group(1));
				List<String> publicationsThisYear = publicationsByYear.get(publicationYear);
				if(publicationsThisYear == null) {
					publicationsThisYear = new ArrayList<>();
					publicationsByYear.put(publicationYear,publicationsThisYear);
				}
				publicationsThisYear.add(reference);
			} else {
				throw new RuntimeException("No publication date was found in " + LINE_SEPARATOR + reference);
			}
		}

		// Throw away all publication entries from the main text
		String allRefsSpecExpr = "(?s)(<div id=\"refs\" class=\"references\">.*</div>)";
		Pattern allRefsPattern = Pattern.compile(allRefsSpecExpr);
		Matcher allRefsMatcher = allRefsPattern.matcher(newFileContents);
		
		if(allRefsMatcher.find()) {
			String toReplace = allRefsMatcher.group(1);
			newFileContents = newFileContents.replace(toReplace,PUBLICATIONS_TAG_OPEN + LINE_SEPARATOR + PUBLICATIONS_TAG_CLOSE);
		}

		// Build the new publications part
		StringBuilder publicationsBuilder = new StringBuilder();
		
		List<Integer> decreasingYears = new ArrayList<>();
		for(int year : publicationsByYear.keySet()){
			decreasingYears.add(0,year);
		}

		publicationsBuilder.append(PUBLICATIONS_TAG_OPEN).append(LINE_SEPARATOR);

		for(int i=0; i < decreasingYears.size(); i++){
			int year = decreasingYears.get(i);
			List<String> publications = publicationsByYear.get(year);

			publicationsBuilder.append("<h4 id=\"year-").append(year).append("\">").append(year).append("</h4>").append(LINE_SEPARATOR);

			for(String pub : publications) {
				publicationsBuilder.append(pub).append(LINE_SEPARATOR);
			}
			
			publicationsByYear.remove(year);
		}

		publicationsBuilder.append(PUBLICATIONS_TAG_CLOSE).append(LINE_SEPARATOR);

		newFileContents = newFileContents.replace(PUBLICATIONS_TAG_OPEN+LINE_SEPARATOR+PUBLICATIONS_TAG_CLOSE,publicationsBuilder.toString());

		/*
		 * Rename tmp file's name to old file name thus replacing its old contents
		 */

		File tempFile = new File(fileName + ".temp");


		try (BufferedWriter w = Files.newBufferedWriter(tempFile.toPath())) {
			w.write(newFileContents);
		}

		f.delete();
		tempFile.renameTo(f);

	}
}

