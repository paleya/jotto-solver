import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.io.IOException;
import java.util.*;

// words list obtained from https://github.com/charlesreid1/five-letter-words/blob/master/sgb-words.txt

public class Main {

    static String firstWord = "tears"; // first word the bot guesses, precomputed to save time

    // returns the jotto score for two strings
    public static int matchingCount(String guess, String word) {
        if(guess.equals(word)) return 6; // if the guess is correct
        for(int i = 0; i < 5; i++) {
            guess = guess.replaceFirst(word.substring(i,i+1),"");
        }
        return 5 - guess.length();
    }

    // takes in a guess & current valid words, then returns how good it is
    public static double expectedInformation(String guess, List<String> valid) {
        int[] bins = new int[7]; // 6 is if it is the correct word
        for(String word : valid) {
            bins[matchingCount(guess, word)]++;
        }
        double val = 0;
        double total = valid.size();
        for(int i : bins) {
            if(i == 0) continue;
            double prob = i / total;
            val -= prob * Math.log(prob);
        }
        return val;
    }

    // finds the best guess for the current set of valid words
    public static String bestGuess(List<String> valid, List<String> words) {
        String best = "";
        double bestEntropy = -1;
        for(String word : words) {
            double entropy = expectedInformation(word,valid);
            // slight edge to words in solution space to break ties
            if(valid.contains(word)) entropy += 0.0001;
            if(entropy > bestEntropy) {
                bestEntropy = entropy;
                best = word;
            }
        }
        return best;
    }

    // updates the remaining solution space
    public static void updateValid(List<String> valid, String guess, int result) {
        for(int i = valid.size() - 1; i >= 0; i--) {
            if(matchingCount(guess, valid.get(i)) != result) {
                valid.remove(i);
            }
        }
    }


    public static void main(String[] args) throws IOException {
        Path path = Paths.get("words.txt");
        List<String> words = Files.readAllLines(path);
        List<String> valid = Files.readAllLines(path);
        Scanner in = new Scanner(System.in);

        System.out.println("Do you want to put in the secret word?");
        System.out.println("Enter q if not and the actual word if yes.");
        String ans = in.next();
        int i = 0;

        while(true) {
            System.out.println("Words left: " + valid.size());
            i++;
            String guess = firstWord;
            if(i > 1 || guess.isEmpty()) guess = bestGuess(valid, words);

            int result;
            if(ans.equals("q")) {
                System.out.println("Guess " + i + ": " + guess);
                System.out.print("What's the result? q to quit: ");
                String input = in.next();
                if (input.equals("q")) {
                    System.out.println("Done!");
                    break;
                }
                result = Integer.parseInt(input);
            } else {
                System.out.println("Guess " + i + ": " + guess);
                result = matchingCount(guess, ans);
                System.out.println("Result: " + result);
            }


            updateValid(valid, guess, result);
            if(result == 6) {
                System.out.println("Done!");
                break;
            }

            if(valid.isEmpty()) {
                System.out.println("Error: word not found");
                break;
            }
        }
    }
}