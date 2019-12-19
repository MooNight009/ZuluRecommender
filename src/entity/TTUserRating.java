package entity;

import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TTUserRating{
	private int movieId;
	private int userId;
	private int timeStamp;
	private double rating;

	public TTUserRating(int movieId, int userId, int timeStamp, double rating){
		super();
		this.movieId= movieId;
		this.userId= userId;
		this.timeStamp= timeStamp;
		this.rating= rating;
	}

	public int getMovieId(){
		return movieId;
	}

	public void setMovieId(int movieId){
		this.movieId= movieId;
	}

	public int getUserId(){
		return userId;
	}

	public void setUserId(int userId){
		this.userId= userId;
	}

	public int getTimeStamp(){
		return timeStamp;
	}

	public void setTimeStamp(int timeStamp){
		this.timeStamp= timeStamp;
	}

	public double getRating(){
		return rating;
	}

	public void setRating(double rating){
		this.rating= rating;
	}

	// This method sorts the list by timestamp
	public static void sortByTimeStamp(List<TTUserRating> ttUserRating){
		Collections.sort(ttUserRating, new Comparator<TTUserRating>(){

			@Override
			public int compare(TTUserRating o1, TTUserRating o2){
				return Integer.compare(o1.getTimeStamp(), o2.getTimeStamp());
			}
		});
	}
	
	// This method returns the highest year of all the entries
	public static int getMaxYear(List<TTUserRating> ttUserRatings){
		int max= 0;
		
		Calendar cal = Calendar.getInstance();
		for(TTUserRating ttUserRating: ttUserRatings) {
			cal.setTimeInMillis(ttUserRating.getTimeStamp());
			if(cal.get(Calendar.YEAR)> max)	max= cal.get(Calendar.YEAR);
		}

		return max;
	}
}
