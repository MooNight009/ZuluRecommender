import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import config.Constants;
import entity.Genre;
import entity.Movie;
import utilities.IO;
import utilities.JsonConverter;

public class FeatureSelection{

	public static void main(String[] args) {
		useTFIDF(0.01);
	}
	
	//	TF-IDF of X%
	public static void useTFIDF(double percentage) {
		List<Genre> genres= JsonConverter.readGenres();	//	Reads the genres from json
		List<Movie> movies= JsonConverter.readMovies(genres);	//	Reads the movies from json
		IO.loadMV(movies, Constants.movie_values);
		
		List<Entry<Integer, Integer>> list= new ArrayList<>();
		for(int i= 0; i< Constants.vectorSize; i++)	list.add(new AbstractMap.SimpleEntry(i, 0));
		
		for(Movie movie: movies) {
			Map<Integer, Double> map= movie.sparseVector;
			
			for(Map.Entry<Integer, Double> entry: map.entrySet())	list.get(entry.getKey()).setValue(list.get(entry.getKey()).getValue()+ 1);
		}
		
		Collections.sort(list, new Comparator<Map.Entry<Integer, Integer>>(){

			@Override
			public int compare(Entry<Integer, Integer> o1, Entry<Integer, Integer> o2){
				// TODO Auto-generated method stub
				return Integer.compare(o2.getValue(), o1.getValue());
			}
			
		});
		
		
		List<Integer> acceptebleColumns= new ArrayList<>();
		for(int i= 0; i< list.size()* percentage; i++)	acceptebleColumns.add(list.get(i).getKey());
		
		for(Movie movie: movies) {
			Map<Integer, Double> map= movie.sparseVector;
			List<Integer> toBeRemovedList= new ArrayList<>();
			for(Map.Entry<Integer, Double> entry: map.entrySet())	if(!acceptebleColumns.contains(entry.getKey()))	toBeRemovedList.add(entry.getKey());
			
			for(Integer toBeRemovedColumn: toBeRemovedList)	movie.sparseVector.remove(toBeRemovedColumn);
			
			map= new HashMap<>();
			
			for(Map.Entry<Integer, Double> entry: movie.sparseVector.entrySet())	map.put(acceptebleColumns.indexOf(entry.getKey()), entry.getValue());
			
			movie.sparseVector= map;
			movie.lastPosition= acceptebleColumns.size();
		}
		
		IO.saveMV(movies, Constants.movie_values_TFIDF);
	}
}
