package Chains;

public class Sentence{
	private int id;
	private String sentence;
	private double score;
	
	public Sentence(int id,String sentence) {
		this.id = id;
		this.sentence = sentence;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getSentence() {
		return sentence;
	}
	public void setSentence(String sentence) {
		this.sentence = sentence;
	}
	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}
	
	public void matchChain(Chain chain) {
		for(int i=0;i<chain.getWords().size();i++) {
			if(sentence.contains(chain.getWord(i).getWord())) {
				score = score + chain.getScore();
			}
		}
	}
}
