package searchengine;

public class RankerBean {

    
    
    
	
	private float tfidf;
	private int bold;
	private int anchor;
	private float score;
	private float pageRank;

	
	int getBold() {
		return bold;
	}
	
	void setBold(int bold) {
		this.bold = bold;
	}
	
	int getAnchor() {
		return anchor;
	}
	
	void setAnchor(int anchor) {
		this.anchor = anchor;
	}
	
	float getTfidf() {
		return tfidf;
	}
	
	void setTfidf(float tfidf) {
		this.tfidf = tfidf;
	}
	
	void setPageRank(float pageRank) {
	    this.pageRank = pageRank;
	}
	
	float getPageRank() {
	    return pageRank;
	}

	public float getFinalScore() {
		if (score == 0) {
			score = (getAnchor() + getBold() + getTfidf()) * (getPageRank());
		}
		return score;
	}

	
	
	
}
