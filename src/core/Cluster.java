package core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import entity.Movie;

public class Cluster{

	public static Map<Integer, Integer> getClusterScore(List<Movie> likedMovies){
		Map<Integer, Integer> clusterScore= new HashMap<>();
		
		for(Movie movie: likedMovies) {
			if(clusterScore.containsKey(movie.getCluster()))	clusterScore.put(movie.getCluster(), clusterScore.get(movie.getCluster())+ movie.getRating());
			else	clusterScore.put(movie.getCluster(), movie.getRating());
		}
		
		
		return clusterScore;
	}
}
