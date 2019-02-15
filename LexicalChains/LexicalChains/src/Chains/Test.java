package Chains;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.Pointer;
import edu.mit.jwi.morph.WordnetStemmer;

public class Test {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		IDictionary dict = new Dictionary (new File("dict"));
		dict.open ();
		
		//Load input file
		LinkedHashMap<String,Word> words = new LinkedHashMap<String,Word>();
		ArrayList<Sentence> senten = new ArrayList<Sentence>();
		loadInput("input.txt",words, senten, dict);

		//Produce lexical chains
		ArrayList<Chain> chainsList = new ArrayList<Chain>();
		produceChains(chainsList, words, dict);
		
		//Save the output
		saveOutput("output.txt", chainsList);
		
		//Calculate chain score
		for(int i=0;i<chainsList.size();i++) {
			chainsList.get(i).sumScore();
		}
		
		//Calculate sentence score
		for(int i=0;i<senten.size();i++) {
			for(int j=0;j<chainsList.size();j++) {
				senten.get(i).matchChain(chainsList.get(j));
			}
		}
		
		//Define comparators
		Comparator c1 = new Comparator<Sentence>() {  
			@Override
			public int compare(Sentence s1, Sentence s2) {
				// TODO Auto-generated method stub
				if(s1.getScore() > s2.getScore()) return -1;
				else return 1;
			}  
        }; 
        Comparator c2 = new Comparator<Sentence>() {  
			@Override
			public int compare(Sentence s1, Sentence s2) {
				// TODO Auto-generated method stub
				if(s1.getId() < s2.getId()) return -1;
				else return 1;
			}  
        }; 
        
        //Get the core sentences
		Collections.sort(senten, c1);
		ArrayList<Sentence> summ = new ArrayList<Sentence>();
		int extractSize = (int) (senten.size()*0.3 == 0?1:senten.size()*0.3);
		for(int i=0;i<extractSize;i++) {
			summ.add(senten.get(i));
		}
		//Restore the original order of sentences
		Collections.sort(summ, c2);
		for(int i=0;i<summ.size();i++) {
			System.out.println(summ.get(i).getSentence());
		}				
	}
	
	public static void loadInput(String filename, LinkedHashMap<String,Word> words, ArrayList<Sentence> senten, IDictionary dictionary) throws IOException {
		WordnetStemmer stemmer = new WordnetStemmer(dictionary);
		BufferedReader br = new BufferedReader(new FileReader(filename));
		String read = br.readLine();
		String sentence = "";
		
		String[] temp;
		int wordNo = 0;
		while(read != null) {
			sentence = sentence + read;
			
			//Extract words
			read = read.toLowerCase().replaceAll("[!.,]", "");
			temp = read.split(" ");
			for(int i=0;i<temp.length;i++) {
				//Filter non-noun
				String stemWord = stemmer.findStems(temp[i], POS.NOUN).size() == 0?temp[i]:stemmer.findStems(temp[i], POS.NOUN).get(0);
				if(dictionary.getIndexWord(stemWord, POS.NOUN) == null) continue;
				
				//Filter some function words
				if(temp[i].equals("the") || temp[i].equals("a") || temp[i].equals("this") || temp[i].equals("that") || temp[i].equals("at") || temp[i].equals("in")) 
					continue;
				
				if(!words.containsKey(temp[i])) {
					Word w = new Word(wordNo,temp[i],1);
					words.put(w.getWord(), w);
					wordNo++;
				}else {
					Word w = words.get(temp[i]);
					w.setNum(w.getNum()+1);
				}
			}
			read = br.readLine();
		}
		br.close();
		
		//Extract sentences
		BreakIterator boundary = BreakIterator.getWordInstance();
		boundary = BreakIterator.getSentenceInstance(Locale.US);
        boundary.setText(sentence);
        int start = boundary.first();
        for (int end = boundary.next(),i=0;end != BreakIterator.DONE;start = end, end = boundary.next(),i++){
        	senten.add(new Sentence(i,sentence.substring(start,end)));
        }
	}
	
	public static void saveOutput(String filename, ArrayList<Chain> chainsList) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
		bw.write(printResult(chainsList));
		bw.flush();
		bw.close();
	}
	
	public static void produceChains(ArrayList<Chain> chainsList, LinkedHashMap<String,Word> words, IDictionary dict){
		Chain chain = null;
		Iterator it = words.keySet().iterator();
		while(it.hasNext()) {
			boolean inChain = false;
			String curWord = (String)it.next();
			for(int j=0;j<chainsList.size();j++) {
				chain = chainsList.get(j);
				for(int k=0;k<chain.getSynsets().size();k++) {
					if(chain.getSynsets().contains(curWord)){
						inChain = true;			
						Word tempW = (Word)words.get(curWord);
						chain.addWord(tempW);
						addAllSynset(chain.getSynsets(), tempW.getWord(), dict);
						break;
					}
				}
				if(inChain) break;
			}
			if(!inChain) {
				chain = new Chain();
				chain.addWord((Word)words.get(curWord));
				addAllSynset(chain.getSynsets(),((Word)words.get(curWord)).getWord(), dict);
				chainsList.add(chain);
			}
		}
	}
	
	private static void addAllSynset(ArrayList<String> synsets, String chw, IDictionary dictionary) {
		// TODO Auto-generated method stub
		WordnetStemmer stemmer = new WordnetStemmer(dictionary);
		chw = stemmer.findStems(chw, POS.NOUN).size()==0?chw:stemmer.findStems(chw, POS.NOUN).get(0);
		
		IIndexWord indexWord = dictionary.getIndexWord(chw, POS.NOUN);
		List<IWordID> sensesID = indexWord.getWordIDs();
		List<ISynsetID> relaWordID = new ArrayList<ISynsetID>();
		List<IWord> words;
		
		for(IWordID set : sensesID) {
			IWord word = dictionary.getWord(set);
			
			//Add Synonym
			relaWordID.add(word.getSynset().getID());

			//Add Antonym
			relaWordID.addAll(word.getSynset().getRelatedSynsets(Pointer.ANTONYM));
			
			//Add Meronym
			relaWordID.addAll(word.getSynset().getRelatedSynsets(Pointer.MERONYM_MEMBER));
			relaWordID.addAll(word.getSynset().getRelatedSynsets(Pointer.MERONYM_PART));
			
			//Add Hypernym
			relaWordID.addAll(word.getSynset().getRelatedSynsets(Pointer.HYPERNYM));
			
			//Add Hyponym
			relaWordID.addAll(word.getSynset().getRelatedSynsets(Pointer.HYPONYM));
		}
		
		for(ISynsetID sid : relaWordID) {
			words = dictionary.getSynset(sid).getWords();
			for(Iterator<IWord> i = words.iterator(); i.hasNext();) {
				synsets.add(i.next().getLemma());
			}
		}
	}

	public static String printResult(ArrayList<Chain> chainsList) {
		String r = "";
		for(int i=0;i<chainsList.size();i++) {
			r = r + "Chain "+(i+1)+": ";
			for(int j=0;j<chainsList.get(i).size();j++) {
				r = r + chainsList.get(i).getWord(j).getWord()+"("+chainsList.get(i).getWord(j).getNum()+")";
				if(j != chainsList.get(i).size()-1) r = r + ", ";
			}
			r = r +"\n";
		}
		System.out.println(r);
		return r;
	}
}
