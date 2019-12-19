package entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TTUser{
	
	private int movieEntryCount;
	
	private int testingMovieCount;
	private int trainingMovieCount;
	
	private int testingCorrectRecommendation= 0;
	
	private List<TTUserRating> userRatings= new ArrayList<>();
	
	private Map<Integer, Double> movieIds= new HashMap<>();

	public TTUser(int movieEntryCount, Map<Integer, Double> movieIds){
		this.movieEntryCount= movieEntryCount;
		this.movieIds= movieIds;
		this.testingMovieCount= (int) (this.movieEntryCount* 0.8);
		this.trainingMovieCount= this.movieEntryCount- this.testingMovieCount;
	}
	
	public TTUser(List<TTUserRating> userRatings) {
		this.userRatings= userRatings;
	}

	public int getMovieEntryCount(){
		return movieEntryCount;
	}

	public void setMovieEntryCount(int movieEntryCount){
		this.movieEntryCount= movieEntryCount;
	}

	public int getTestingMovieCount(){
		return testingMovieCount;
	}

	public void setTestingMovieCount(int testingMovieCount){
		this.testingMovieCount= testingMovieCount;
	}

	public int getTrainingMovieCount(){
		return trainingMovieCount;
	}

	public void setTrainingMovieCount(int trainingMovieCount){
		this.trainingMovieCount= trainingMovieCount;
	}

	public int getTestingCorrectRecommendation(){
		return testingCorrectRecommendation;
	}

	public void setTestingCorrectRecommendation(int testingCorrectRecommendation){
		this.testingCorrectRecommendation= testingCorrectRecommendation;
	}

	public Map<Integer, Double> getMovieIds(){
		return movieIds;
	}

	public void setMovieIds(Map<Integer, Double> movieIds){
		this.movieIds= movieIds;
	}

	public List<TTUserRating> getUserRatings(){
		return userRatings;
	}

	public void setUserRatings(List<TTUserRating> userRatings){
		this.userRatings= userRatings;
	}
	
	
}
