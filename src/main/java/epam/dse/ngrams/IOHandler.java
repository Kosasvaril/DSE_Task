package epam.dse.ngrams;

import java.io.*;
import java.util.List;

public class IOHandler {

    private final String inputPath;
    private final String outputPath;

    IOHandler(String inputPath, String outputPath){
        this.inputPath = inputPath;
        this.outputPath = outputPath;
    }


    public List<String> readJSON(){
        StringBuilder jsonLineReader = new StringBuilder();
        try(BufferedReader reader = new BufferedReader(new FileReader(this.inputPath))){
            String line = reader.readLine();
            handleInput(line, reader, jsonLineReader);
            return List.of(jsonLineReader.toString().split("\n"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleInput(String line, BufferedReader reader, StringBuilder jsonLineReader) throws IOException {
        while (line != null) {
            jsonLineReader.append(line);
            jsonLineReader.append("\n");
            line = reader.readLine();
        }
    }

    public void writeOut(List<String> authors, List<List<String>> nGrams) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(this.outputPath))) {
            for(int i=0; i<authors.size(); i++){
                if(isError(nGrams.get(i).get(0))){
                    continue;
                }
                String currentAuthor = authors.get(i);
                List<String> currentNGramLine = nGrams.get(i);
                String line = createLine(currentAuthor, currentNGramLine);
                writer.write(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String createLine(String author, List<String> nGramLine) {
        StringBuilder csvLineBuilder = new StringBuilder();

        csvLineBuilder.append(author);
        for(String nGram : nGramLine){
            csvLineBuilder.append(",").append(nGram);
        }
        csvLineBuilder.append("\n");
        return csvLineBuilder.toString();
    }

    private boolean isError(String nGram) {
        return nGram.equals("There are not enough words!") || nGram.equals("There are not enough consecutive words!");
    }
}
