package builder;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import utils.BuilderUtils;

/**
 * Class used to extract the functions from the MEOS library.
 * Run with directly with java through the main class
 *
 * @author Killian Monnier
 * @since 27/06/2023
 */
public class FunctionsExtractor {
	private static final String FUNCTION_PATTERN = "(extern\\s+(static\\s+|inline\\s+|const\\s+)?|static\\s+|inline\\s+|const\\s+)?[a-zA-Z0-9_*]+\\s*\\**\\s+[a-zA-Z0-9_*]+\\s*\\([^)]*\\);";
//	private static final String FUNCTION_PATTERN = "extern (static |inline |const )?[a-zA-Z0-9_*]+\\s*\\**\\s+[a-zA-Z0-9_*]+\\s*\\([^)]*\\);"; // RegEx model for recognizing a function in the meos.h file
	private static final String TYPES_PATTERN = "typedef\\s(?!struct|enum)\\w+\\s\\w+;"; // Type recognition RegEx pattern in meos.h file
	private List<Path> inputFilePaths = new ArrayList<>();	
	private Path outputFunctionsFilePath = null;
	private Path outputTypesFilePath = null;
	String currentDir = System.getProperty("user.dir");
	
	/**
	 * Constructor of {@link FunctionsExtractor}.
	 *
	 * @throws URISyntaxException thrown when resources not found
	 */
	public FunctionsExtractor() throws URISyntaxException {
		String[] files = {
            "src/main/java/builder/resources/meos.h",
            "src/main/java/builder/resources/meos_geo.h",
			"src/main/java/builder/resources/pg_types.h",
			"src/main/java/builder/resources/meos_internal.h"
        };
		for (String file : files) {
            inputFilePaths.add(Paths.get(currentDir, file));
        }
		//this.inputFilePath = Paths.get(Objects.requireNonNull(this.getClass().getResource("/meos.h")).toURI());
		this.outputFunctionsFilePath = Paths.get(new URI(Objects.requireNonNull(this.getClass().getResource("")) + "meos_functions.h"));
		this.outputTypesFilePath = Paths.get(new URI(Objects.requireNonNull(this.getClass().getResource("")) + "meos_types.h"));
	}
	
	/**
	 * Launch process of extraction.
	 *
	 * @param args arguments
	 * @throws URISyntaxException thrown when resources not found
	 */
	public static void main(String[] args) throws URISyntaxException {
		var extractor = new FunctionsExtractor();
		
		List<String> allFunctions = new ArrayList<>();
		List<String> allTypes = new ArrayList<>();

		for (Path inputPath : extractor.inputFilePaths) {
			allFunctions.addAll(BuilderUtils.extractPatternFromFile(inputPath.toString(), FUNCTION_PATTERN));
			allTypes.addAll(extractor.getTypesFromFile(inputPath));
		}

		BuilderUtils.writeFileFromArray(allFunctions, extractor.outputFunctionsFilePath.toString());
		BuilderUtils.writeFileFromArray(allTypes, extractor.outputTypesFilePath.toString());
		System.out.println("Feature extraction completed. The functions have been written to the file " + extractor.outputFunctionsFilePath);
	}
	
	/**
	 * Retrieves structure names from file.
	 *
	 * @param filePath file path
	 * @return list of structure names
	 */
	public static ArrayList<String> getStructureNames(String filePath) {
		List<String> structureNames = new ArrayList<>();
		try (var reader = new BufferedReader(new FileReader(filePath))) {
			var content = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				content.append(line).append("\n");
			}
			
			String regex = "typedef\\s+struct(\\s\\w+)?\\s*\\{[\\s\\S]*?}\\s*(\\w+)";
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(content.toString());
			
			while (matcher.find()) {
				String structureName = matcher.group(2);
				structureNames.add(structureName);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new ArrayList<>(structureNames);
	}
	
	/**
	 * Get types from a file.
	 *
	 * @return the list of lines
	 */
	private List<String> getTypesFromFile(Path path) {
        var rawTypes = BuilderUtils.extractPatternFromFile(path.toString(), TYPES_PATTERN);
        var structureNames = getStructureNames(path.toString());
        List<String> filteredTypes = new ArrayList<>();

        for (var rawType : rawTypes) {
            var words = rawType.trim().split("\\s+");
            if (words.length >= 2) {
                String typeName = words[1];
                if (!structureNames.contains(typeName)) {
                    filteredTypes.add(rawType);
                }
            }
        }
        return filteredTypes;
    }
}
