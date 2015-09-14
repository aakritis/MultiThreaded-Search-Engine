package searchengine;

import java.io.*;
import java.util.*;
import java.util.regex.*;

class SpellingSuggest {
	
	private final HashMap<String, Integer> wordsCounter = new HashMap<String, Integer>();
	
	public SpellingSuggest() {
		
	}

	//Constructor taking bag of words
	public SpellingSuggest(String dictionary) throws IOException {
		BufferedReader input = new BufferedReader(new FileReader(dictionary));
		Pattern pattern = Pattern.compile("\\w+");
		for(String eachLine = ""; eachLine != null; eachLine = input.readLine()){
			//Read each line in lowercase
			Matcher matcher = pattern.matcher(eachLine.toLowerCase());
			while(matcher.find()) 
				wordsCounter.put((eachLine = matcher.group()), wordsCounter.containsKey(eachLine) ? wordsCounter.get(eachLine) + 1 : 1);
		}
		input.close();
		//Print hash
		/*for (String name: nWords.keySet()){

            String key =name.toString();
            String value = nWords.get(name).toString();  
            System.out.println(key + " " + value);  
		}*/ 
	}

	private final ArrayList<String> wordCombinations(String eachToken) {
		ArrayList<String> result = new ArrayList<String>();
		//Take every combination of words initially
		for(int i=0; i < eachToken.length(); ++i) 
			result.add(eachToken.substring(0, i) + eachToken.substring(i+1));
		//Rotate among letters
		for(int i=0; i < eachToken.length()-1; ++i) 
			result.add(eachToken.substring(0, i) + eachToken.substring(i+1, i+2) + eachToken.substring(i, i+1) + eachToken.substring(i+2));
		//Try alphabet combinations
		for(int i=0; i < eachToken.length(); ++i) 
			for(char c='a'; c <= 'z'; ++c) 
				result.add(eachToken.substring(0, i) + String.valueOf(c) + eachToken.substring(i+1));
		for(int i=0; i <= eachToken.length(); ++i) 
			for(char c='a'; c <= 'z'; ++c) 
				result.add(eachToken.substring(0, i) + String.valueOf(c) + eachToken.substring(i));
		
		/*for (int i = 0; i < result.size(); i++) {
		     System.out.println(result.get(i));
		}*/
		return result;
	}

	public final String potentialWord(String word) {
		//Begin with best case
		if(wordsCounter.containsKey(word)) 
			return word;
		/**
		 * Fetch possible combinations and compare against the hashmap storing words, return potential candidate word
		 * */
		ArrayList<String> allCombinations = wordCombinations(word);
		HashMap<Integer, String> possibleWords = new HashMap<Integer, String>();
		for(String s : allCombinations) 
			if(wordsCounter.containsKey(s)) possibleWords.put(wordsCounter.get(s),s);
		if(possibleWords.size() > 0) 
			return possibleWords.get(Collections.max(possibleWords.keySet()));
		for(String s : allCombinations) 
			for(String w : wordCombinations(s)) 
				if(wordsCounter.containsKey(w)) 
					possibleWords.put(wordsCounter.get(w),w);
		return possibleWords.size() > 0 ? possibleWords.get(Collections.max(possibleWords.keySet())) : word;
	}
	
	
	public final static String finalGuess(String input){
		
		String guessWord;
		
		//Dictionary of words
		String bagOfWords = "/home/cis455/Downloads/bagofwords.txt";
		
		StringBuffer finalSuggestion = new StringBuffer();
		//Split input on spaces
		String[] inputArray = input.split("\\s+");
		
		for(String word : inputArray){
			try {
				guessWord = (new SpellingSuggest(bagOfWords).potentialWord(word)).trim();
				//Append it to final string
				finalSuggestion.append(guessWord + " ");
			} catch (IOException e) {
				e.printStackTrace();
				//Default case give back the input
				return input;
			}
		}
		//Send out suggestion
		String dispatch = finalSuggestion.toString().trim();
		return dispatch;
	}
	
	public static void main(String args[]) throws IOException {
		//Get this from user
		String searchStr = "hello wrld serch engne";
		//Call spelling suggestion
		String suggestion = finalGuess(searchStr);
		
		System.out.println("Did you mean: " + suggestion + "?");
	}

}