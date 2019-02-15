package Chains;

public class Word {
	private int id;
	private String word;
	private int num;
	
	public Word(int id, String word,int num) {
		this.id = id;
		this.word = word;
		this.num = num;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getWord() {
		return word;
	}
	public void setWord(String word) {
		this.word = word;
	}
	public int getNum() {
		return num;
	}
	public void setNum(int num) {
		this.num = num;
	}
}
