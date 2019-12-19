import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import config.Constants;
import core.DBConnection;
import entity.Genre;
import entity.Movie;
import entity.TTUser;
import utilities.JsonConverter;

public class UserValueCreated{
	public static void main(String[] args){
		// DBConnection.init();
		List<Genre> genres= JsonConverter.readGenres(); // Reads the genres from json
		List<Movie> movies= JsonConverter.readMovies(genres); // Reads the movies from json

//		List<Map<Integer, Double>> list= new ArrayList<>();
		int[] map= new int[193886+ 1];
		for(int i= 0; i< map.length; i++)	map[i]= -1;
//		for(int i= 0; i< map.length; i++) {
//			if(movie)
//			map[i]= Movie.getMovieIndexById(i, moviesDuplicate);
//			moviesDuplicate.remove(i);
//		}
		for(int i= 0; i< movies.size(); i++) {
			if(!movies.get(i).getMlId().equals("0")) {
				map[Integer.parseInt(movies.get(i).getMlId())]= i;
			}
		}
		
		int min= 193886;
		for(Movie movie: movies)	if(Integer.parseInt(movie.getMlId())< min)	min= Integer.parseInt(movie.getMlId());
		System.out.println(min);
		System.out.println(movies.get(25603).getMlId());
		getTTUser(movies, map);

	}

	private static List<TTUser> getTTUser(List<Movie> movies, int[] map){
		List<TTUser> ttUsers= new ArrayList<>();

		try{
			BufferedReader myReader= new BufferedReader(new FileReader("D:\\FYP\\ml-20m\\ratings.csv"));

			Map<Integer, Double> movieIds= new HashMap<>();

			String myRead= myReader.readLine();
			int lastUserId= 0;
			int n= 0;
			while ((myRead= myReader.readLine())!= null){
				if (n++% 10000== 0){
					System.out.println(Instant.now()+ " "+ n);
					// System.gc();
				}
				// if(n% 100000== 0) {
				// System.gc();
				// }
				String[] data= myRead.split(",");
				//
				int movieId= Integer.parseInt(data[1]);
				int userId= Integer.parseInt(data[0]);
				double rating= Double.parseDouble(data[2]);
				//
				if (userId!= lastUserId){
					ttUsers.add(new TTUser(movieIds.size(), movieIds));
					movieIds= new HashMap<>();
					lastUserId= userId;
					if (ttUsers.size()> 100){
						writeToFile(ttUsers, movies, map);
						ttUsers= new ArrayList<>();
					}
				}
				movieIds.put(movieId, rating);
			}
			ttUsers.add(new TTUser(movieIds.size(), movieIds));
			writeToFile(ttUsers, movies, map);

			myReader.close();
		} catch (Exception e){
			System.out.println(e);
		}
		System.out.println(ttUsers.size());
		return ttUsers;
	}

	private static void writeToFile(List<TTUser> ttUsers, List<Movie> movies, int[] map0){
		List<Map<Integer, Double>> list= new ArrayList<>();

		int n= 0;
		for (TTUser ttUser : ttUsers){
			// if(n== 100) break;
			ArrayList<Entry<Integer, Double>> movieId_rating= new ArrayList<>(ttUser.getMovieIds().entrySet());

			Map<Integer, Double> tmpList= new HashMap<>();
			for (int i= 0; i< movieId_rating.size(); i++){
//				int movieId= movieId_rating.get(i).getKey();
//				int movieIndex= Movie.getMovieIndexById(movieId, movies);
				int movieIndex= map0[movieId_rating.get(i).getKey()];
				if (movieIndex== -1)
					continue;
				tmpList.put(movieIndex, movieId_rating.get(i).getValue());
			}

			list.add(tmpList);
		}
		try{
			BufferedWriter myWriter= new BufferedWriter(new FileWriter(Constants.user_values, true));

			for (Map<Integer, Double> map : list){
				for (Map.Entry<Integer, Double> entry : map.entrySet())
					myWriter.write(entry.getKey()+ ":"+ entry.getValue()+ ",");
				myWriter.newLine();
			}

			myWriter.close();
		} catch (Exception e){
			// TODO: handle exception
		}
	}
}
