package Chains;

import java.util.ArrayList;

public class Chain {
	private ArrayList<Word> words;
	private ArrayList<String> synsets;
	private double score;
	
	public Chain() {
		words = new ArrayList<Word>();
		synsets = new ArrayList<String>();
	}
	
	public ArrayList<Word> getWords() {
		return words;
	}
	public void setWords(ArrayList<Word> words) {
		this.words = words;
	}
	public Word getWord(int i) {
		return words.get(i);
	}
	public ArrayList<String> getSynsets() {
		return synsets;
	}
	public void setSynsets(ArrayList<String> synsets) {
		this.synsets = synsets;
	}
	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}
	public int size() {
		return words.size();
	}
	public void addWord(Word w) {
		words.add(w);
	}
	public void addSynet() {
		
	}
	public void sumScore() {
		for(int i=0;i<words.size();i++) {
			score = score + words.get(i).getNum();
		}
	}
}
