package core;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import config.Constants.DM;

import java.util.Set;

import org.apache.commons.math3.ml.distance.EuclideanDistance;

import entity.Movie;
import utilities.MatrixManipulation;

public class CollabRecommender{

	public static Movie[] getRecommendationsCluster(List<Movie> movies, List<Map<Integer, Double>> UV,
			Map<Integer, Integer> userRatings, Map<Integer, Integer> clusterScore, double[] likedMoviesVector,
			int recommendationSize, List<Movie> likedMovies, boolean USBased){
		Movie[] recommendedMovies= new Movie[recommendationSize];

		//	Duplicating the data
		List<Map<Integer, Double>> listCopy= new ArrayList<>();
		for(int i= 0; i< UV.size(); i++) {
			listCopy.add(new HashMap<Integer, Double>(UV.get(i)));
		}

		// First we remove irrelevant rows
		List<Integer> watchedMovieId= new ArrayList<>(userRatings.keySet());
		loop:
		for (int i= 0; i< listCopy.size(); i++){
			Map<Integer, Double> userMap= listCopy.get(i);

			for (Integer entry : watchedMovieId)
				if (userMap.containsKey(entry))
					continue loop;

			// If we got here it means it's not in
			listCopy.remove(i);
			i--;
		}
		
		// Now we set a score for each row and apply it to the content in the row
		for (int i= 0; i< listCopy.size(); i++){
			Map<Integer, Double> userMap= listCopy.get(i);
			int score= 0;

			for (Map.Entry<Integer, Integer> entry : userRatings.entrySet())
				if (userMap.containsKey(entry.getKey())){
					double userRating= userMap.get(entry.getKey());
					if ((userRating> 0&& entry.getValue()== 1)|| (userRating< 0&& entry.getValue()== -1))
						score++;
					else
						score--;

					// We remove the column we just matched to so it doesn't effect data later
					userMap.remove(entry.getKey());
				}

			if (score== 0){ // If the score is zero meaning liked and disliked cancelled each other out
				listCopy.remove(i);
				i--;
			} else{
				for (Map.Entry<Integer, Double> entry : userMap.entrySet()){
					entry.setValue(entry.getValue()* score);
				}
			}
		}
		
		// Now we set a score for each Column
		Map<Integer, Double> columnScores= new HashMap<>(); // This map contains score for each column which is a movie
															// index
		for (int i= 0; i< listCopy.size(); i++){
			Map<Integer, Double> userMap= listCopy.get(i);
			for (Map.Entry<Integer, Double> entry : userMap.entrySet()){
				if (columnScores.containsKey(entry.getKey()))
					columnScores.put(entry.getKey(), columnScores.get(entry.getKey())+ entry.getValue());
				else
					columnScores.put(entry.getKey(), entry.getValue());
			}
		}
		
		// Modifying based on cluster
		List<Entry<Integer, Double>> set= new ArrayList<>(columnScores.entrySet());
		for (int i= 0; i< set.size(); i++){
			Entry<Integer, Double> entry= set.get(i);
			if (clusterScore.containsKey(movies.get(entry.getKey()).getCluster())){
				columnScores.put(entry.getKey(),entry.getValue()* clusterScore.get(movies.get(entry.getKey()).getCluster()));
			}
			else {
				columnScores.remove(entry.getKey());
			}
			
			if(likedMovies.contains(movies.get(entry.getKey()))) {
				columnScores.remove(entry.getKey());
				continue;
			}
			
			if(USBased && !movies.get(entry.getKey()).getLanguage().equals("US")) {
				columnScores.remove(entry.getKey());
				continue;
			}
		}
		
		List<Entry<Integer, Double>> sortedList= new ArrayList<Entry<Integer, Double>>(columnScores.entrySet());
		
		// Apply distance before sorting
		for (int i= 0; i< sortedList.size(); i++){
			Entry<Integer, Double> entry= sortedList.get(i);

			double newScore= entry.getValue()/ MatrixManipulation.getVectorDistance(
					movies.get(entry.getKey()).getValues(), likedMoviesVector, DM.DM_EuclideanDistance);
			entry.setValue(newScore);
		}

		Collections.sort(sortedList, new Comparator<Entry<Integer, Double>>(){

			@Override
			public int compare(Entry<Integer, Double> o1, Entry<Integer, Double> o2){
				return Double.compare(o2.getValue(), o1.getValue());
			}
		});

		for (int i= 0; i< recommendationSize; i++){
			// System.out.println(movies.get(sortedList.get(i).getKey()));
			if (i>= sortedList.size())
				continue;
			recommendedMovies[i]= movies.get(sortedList.get(i).getKey());
		}
		
		return recommendedMovies;
	}

	public static Movie[] getRecommendationsClusterYear(List<Movie> movies, List<Map<Integer, Double>> UV,
			Map<Integer, Integer> userRatings, Map<Integer, Integer> clusterScore, double[] likedMoviesVector,
			int recommendationSize, int year, List<Movie> likedMovies){
		Movie[] recommendedMovies= new Movie[recommendationSize];

		List<Map<Integer, Double>> listCopy= new ArrayList<>(UV);

		// First we remove irrelevant rows
		loop: for (int i= 0; i< listCopy.size(); i++){
			Map<Integer, Double> userMap= listCopy.get(i);

			for (Map.Entry<Integer, Integer> entry : userRatings.entrySet())
				if (userMap.containsKey(entry.getKey()))
					continue loop;

			// If we got here it means it's not in
			listCopy.remove(i);
			i--;
		}

		// Now we set a score for each row and apply it to the content in the row
		for (int i= 0; i< listCopy.size(); i++){
			Map<Integer, Double> userMap= listCopy.get(i);
			int score= 0;

			for (Map.Entry<Integer, Integer> entry : userRatings.entrySet())
				if (userMap.containsKey(entry.getKey())){
					double userRating= userMap.get(entry.getKey());
					if ((userRating> 0&& entry.getValue()== 1)|| (userRating< 0&& entry.getValue()== -1))
						score++;
					else
						score--;

					// We remove the column we just matched to so it doesn't effect data later
					userMap.remove(entry.getKey());
				}

			if (score== 0){ // If the score is zero meaning liked and disliked cancelled each other out
				listCopy.remove(i);
				i--;
			} else{
				for (Map.Entry<Integer, Double> entry : userMap.entrySet()){
					entry.setValue(entry.getValue()* score);
				}
			}
		}

		// Now we set a score for each Column
		Map<Integer, Double> columnScores= new HashMap<>(); // This map contains score for each column which is a movie index
		for (int i= 0; i< listCopy.size(); i++){
			Map<Integer, Double> userMap= listCopy.get(i);
			for (Map.Entry<Integer, Double> entry : userMap.entrySet()){
				if (columnScores.containsKey(entry.getKey()))
					columnScores.put(entry.getKey(), columnScores.get(entry.getKey())+ entry.getValue());
				else
					columnScores.put(entry.getKey(), entry.getValue());
			}
		}

		// Modifying based on cluster
		List<Entry<Integer, Double>> set= new ArrayList<>(columnScores.entrySet());
		for (int i= 0; i< set.size(); i++){
			Entry<Integer, Double> entry= set.get(i);
			if (clusterScore.containsKey(movies.get(entry.getKey()).getCluster())) {
				columnScores.put(entry.getKey(), entry.getValue()* clusterScore.get(movies.get(entry.getKey()).getCluster()));
			}
			else {
				columnScores.remove(entry.getKey());
				continue;
			}
			
			if(movies.get(entry.getKey()).getYearOfRelease()> year) {
				columnScores.remove(entry.getKey());
				continue;
			}
			
			if(likedMovies.contains(movies.get(entry.getKey()))) {
				columnScores.remove(entry.getKey());
				continue;
			}

		}

		List<Entry<Integer, Double>> sortedList= new ArrayList<Entry<Integer, Double>>(columnScores.entrySet());

		// Apply distance before sorting
		for (int i= 0; i< sortedList.size(); i++){
			Entry<Integer, Double> entry= sortedList.get(i);

			double newScore= entry.getValue()/ MatrixManipulation.getVectorDistance(
					movies.get(entry.getKey()).getValues(), likedMoviesVector, DM.DM_EuclideanDistance);
			entry.setValue(newScore);
		}

		Collections.sort(sortedList, new Comparator<Entry<Integer, Double>>(){

			@Override
			public int compare(Entry<Integer, Double> o1, Entry<Integer, Double> o2){
				return Double.compare(o2.getValue(), o1.getValue());
			}
		});

		for (int i= 0; i< recommendationSize; i++){
			// System.out.println(movies.get(sortedList.get(i).getKey()));
			if (i>= sortedList.size())
				continue;
			recommendedMovies[i]= movies.get(sortedList.get(i).getKey());
		}

		return recommendedMovies;
	}

	//	Returns map of index and rating of liked movies
	public static Map<Integer, Integer> getUserRating(List<Movie> movies, List<Movie> likedMovies){
		Map<Integer, Integer> userRatings= new HashMap<>();

		for (Movie movie : likedMovies)
			userRatings.put(movies.indexOf(movie), movie.getRating());

		return userRatings;
	}
}
