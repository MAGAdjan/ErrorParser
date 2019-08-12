import java.io.*;
import java.util.*;

public class ErrorParser {
    private static volatile Map<String, Integer> errorsMap = new TreeMap<>();
    private static final File STATS_FILE = new File("src/main/resources/Statistics");
    private static final String ERROR_PATTERN = ".*java\\.lang\\..*Error.*";
    private static final String ERROR_PREFIX = "java.lang.";


    public static void main(String[] args) throws Exception {
        parseFiles(new File("src/main/resources/test"), new File("src/main/resources/test2"));
        BufferedReader reader = new BufferedReader(new FileReader(STATS_FILE));
            String nextRow;
            while ((nextRow = reader.readLine()) != null) {
                System.out.println(nextRow);
            }
    }

    public static synchronized void parseFiles(File...files) throws Exception {
            getErrorsFromFiles(files);
    }

    public static synchronized void getErrorsFromFiles(File...files) throws IOException {
        List<File> fileList = Collections.synchronizedList(Arrays.asList(files));
        fileList.forEach ( it -> {
            try (BufferedReader reader = new BufferedReader(new FileReader(it))) {
                String nextRow;
                while ((nextRow = reader.readLine()) != null) {
                    if (nextRow.matches(ERROR_PATTERN)) {
                        int parseStart = nextRow.indexOf(ERROR_PREFIX);
                        int parseEnd = nextRow.indexOf(" ", parseStart);
                        String exceptionName = parseEnd != -1 ? nextRow.substring(parseStart, parseEnd) :
                                nextRow.substring(parseStart);
                        if (errorsMap.containsKey(exceptionName)) {
                            errorsMap.put(exceptionName, errorsMap.get(exceptionName) + 1);
                        } else {
                            errorsMap.put(exceptionName, 1);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        writeErrors();
    }

    public static synchronized void writeErrors() throws IOException {
        FileWriter fileWriter = new FileWriter(STATS_FILE, false);
        PrintWriter printWriter = new PrintWriter(fileWriter, false);
        printWriter.flush();
        printWriter.close();
        fileWriter.close();

        try (FileWriter finalFileWriter = new FileWriter(STATS_FILE, false)) {
            for (String key : errorsMap.keySet()) {
                int value = errorsMap.get(key);
                finalFileWriter.write(key);
                finalFileWriter.write(" ");
                finalFileWriter.write(value + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
