package epam.dse.ngrams;

import java.util.List;

public class Main {

    private static final String INPUT_PATH = "src/main/resources/10K.github.jsonl";
    private static final String OUTPUT_PATH = "output.csv";


    public static void main(String[] args) {
       IOHandler ioHandler = new IOHandler(INPUT_PATH, OUTPUT_PATH);
       List<String> input = ioHandler.readJSON();
       NGramCreator nGrammCreator = new NGramCreator(input);
       ioHandler.writeOut(nGrammCreator.getAuthors(), nGrammCreator.getnGrams());
    }
}