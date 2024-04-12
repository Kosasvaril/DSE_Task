package epam.dse.ngrams;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NGramCreator {

    private static final String PUSH_EVENT = "PushEvent";
    public static final String EVENT_TYPE_TAG = "type";
    public static final String NONWORD_REGEX = ".*\\W.*";
    public static final String HASHTAGNUMBER_REGEX = "^#[\\d]{1,}";
    public static final String SEPARATOR_REGEX = "[|]";
    public static final String DOUBLE_SPACE_REGEX = " {2}";
    public static final String PUNCTUATION_REGEX = "[.\"]";
    public static final String PAYLOAD_TAG = "payload";
    public static final String COMMITS_TAG = "commits";
    public static final String MESSAGE_TAG = "message";
    public static final String AUTHOR_TAG = "author";
    public static final String AUTHOR_NAME_TAG = "name";
    public static final String NOT_ENOUGH_WORDS_MESSAGE = "There are not enough words!";
    public static final String CONSECUTIVE_WORDS_MESSAGE = "There are not enough consecutive words!";
    private final List<String> messages = new ArrayList<>();
    private final List<String> authors = new ArrayList<>();
    private final List<List<String>> nGrams = new ArrayList<>();

    public List<String> getAuthors() {
        return Collections.unmodifiableList(authors);
    }

    public List<List<String>> getnGrams() {
        return Collections.unmodifiableList(nGrams);
    }


    public NGramCreator(List<String> input){
        extractCommits(input);
        processMessages();
        checkForNGrams();
    }

    private void checkForNGrams() {
        for (String message : messages) {
            List<String> threeGrams = generateThreeGrams(message);
            nGrams.add(threeGrams);
        }
    }
    private List<String> generateThreeGrams(String message){
        String[] words = message.split(" ");
        String withoutSeparatorChar =
                message.replaceAll(SEPARATOR_REGEX, "").replaceAll(DOUBLE_SPACE_REGEX, " ").strip();

        if (withoutSeparatorChar.split(" ").length < 3) {
            return List.of(NOT_ENOUGH_WORDS_MESSAGE);
        }else {
            List<String> grams = createNGrams(words);
            if (grams.isEmpty()) {
                return List.of(CONSECUTIVE_WORDS_MESSAGE);
            }
            return grams;
        }
    }
    private List<String> createNGrams(String[] words) {
        int count = 0;
        List<String> tmpGrams = new ArrayList<>();
        for (int i = 0; i < words.length - 2 ; i++) {
            if (words[i].equals("|") || words[i+1].equals("|") || words[i+2].equals("|")) {
                continue;
            }
            if (count < 5) {
                String threeGram = words[i] + " " + words[i + 1] + " " + words[i + 2];
                tmpGrams.add(threeGram);
                count++;
            }
        }
        return tmpGrams;
    }
    private void processMessages() {
        messages.replaceAll(message -> filterAndSeparateStrings(preProcessMessage(message)));
    }

    private String filterAndSeparateStrings(String currentMessage) {
        StringBuilder stringBuilder = new StringBuilder();
        String gramSeparator = "| ";
        String lastOne = "";
        for(String str : currentMessage.split(" ")){
            str = str.toLowerCase();
            if(isValidString(str)){
                stringBuilder.append(str).append(" ");
                lastOne = str + " ";
            }else{
                if(!lastOne.equals(gramSeparator)){
                    stringBuilder.append(gramSeparator);
                    lastOne = gramSeparator;
                }
            }
        }
        return stringBuilder.toString();
    }

    private String preProcessMessage(String message) {
        return message.replaceAll(PUNCTUATION_REGEX, "");
    }

    private boolean isValidString(String str) {
        return !str.matches(NONWORD_REGEX) || str.matches(HASHTAGNUMBER_REGEX);
    }

    private void extractCommits(List<String> input){
        Gson gson = new Gson();
        for (String jsonLine : input) {
            JsonObject jsonObject = gson.fromJson(jsonLine, JsonObject.class);
            String type = jsonObject.get(EVENT_TYPE_TAG).getAsString();
            if(type.equals(PUSH_EVENT)){
                JsonObject payload = jsonObject.get(PAYLOAD_TAG).getAsJsonObject();
                JsonArray commits = payload.get(COMMITS_TAG).getAsJsonArray();
                getAuthorAndMessage(commits);
            }
        }
    }

    private void getAuthorAndMessage(JsonArray jsonCommits) {
        for(JsonElement element : jsonCommits){
            if(!element.isJsonNull()){
                JsonObject tmpObject = element.getAsJsonObject();
                messages.add(String.valueOf(tmpObject.get(MESSAGE_TAG)));
                authors.add(tmpObject.get(AUTHOR_TAG).getAsJsonObject().get(AUTHOR_NAME_TAG).getAsString());
            }
        }
    }
}
