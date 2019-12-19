import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import config.Constants;
import core.DBConnection;
import core.Engine;
import core.EuclideanDistanceTester;
import entity.Genre;
import entity.Movie;
import entity.TTUser;
import entity.TTUserRating;
import utilities.IO;
import utilities.JsonConverter;
import utilities.MatrixManipulation;

public class AccuracyTesterFinal{

	public static void main(String[] args){
		DBConnection.init();
//		nnMain();
		getOrderDetails();
		// euclideanConvFullValue();
		DBConnection.end();
	}

	private static void getOrderDetails() {

		List<Genre> genres= JsonConverter.readGenres(); // Reads the genres from json
		List<Movie> movies= JsonConverter.readMovies(genres); // Reads the movies from json
		IO.loadSVDMV(movies, Constants.movie_values_SVD);
		IO.updateCluster(movies);
		List<Map<Integer, Double>> UV= IO.loadUV(Constants.user_values);
		
		int[] map= new int[193886+ 1];
		for(int i= 0; i< map.length; i++)	map[i]= -1;
		
		for(int i= 0; i< movies.size(); i++) {
			if(!movies.get(i).getMlId().equals("0")) {
				map[Integer.parseInt(movies.get(i).getMlId())]= i;
			}
		}
		
		List<TTUser> ttUsers= new ArrayList<>();
		
		int ttUserSize= 10000;
		//	Get User info
		//	Limit by the given number
		try{
			BufferedReader myReader= new BufferedReader(new FileReader("D:\\FYP\\ml-20m\\ratings.csv"));

			String myRead= myReader.readLine();
			int lastUserId= -1;
			
			List<TTUserRating> ttUserRatings= new ArrayList<>();
			int n= 0;
			while ((myRead= myReader.readLine())!= null){
				String[] data= myRead.split(",");
				
				int movieId= Integer.parseInt(data[1]);
				int userId= Integer.parseInt(data[0]);
				int timeStamp= Integer.parseInt(data[3]);
				double rating= Double.parseDouble(data[2]);
				
				if(map[movieId]== -1)	continue;
				
				if(lastUserId== userId) {
					ttUserRatings.add(new TTUserRating(map[movieId], userId, timeStamp, rating));
				}
				else{
					if(ttUsers.size()== ttUserSize)	break;
					n++;
					lastUserId= userId;
					ttUsers.add(new TTUser(ttUserRatings));
					ttUserRatings= new ArrayList<>();
					ttUserRatings.add(new TTUserRating(map[movieId], userId, timeStamp, rating));
//					if(n< 280000)	ttUsers.remove(ttUsers.size()- 1);
				}
			}
			ttUsers.add(new TTUser(ttUserRatings));
			
			myReader.close();
		} catch (Exception e){
			System.out.println(e);
			e.printStackTrace();
		}
		
		System.out.println(ttUsers.size());
		
		int trueRecommendation= 0;
		int totalPrediction= 0;
		
		//	Compile the data for testing
		for(int i= 0; i< ttUsers.size(); i++) {
			TTUser ttUser= ttUsers.get(i);
			if(ttUser.getUserRatings().size()> 100)	continue;
			System.out.println(i+ "-"+ trueRecommendation+ "-"+ totalPrediction);
			TTUserRating.sortByTimeStamp(ttUser.getUserRatings());
			int year= TTUserRating.getMaxYear(ttUser.getUserRatings());
			
			for(int j= 1; j< ttUser.getUserRatings().size(); j++) {
				System.out.println("\t"+ j+ "-"+ trueRecommendation+ "-"+ totalPrediction);
				if(ttUser.getUserRatings().get(j).getRating()<= 2.5)	continue;
				List<double[]> ds= new ArrayList<>();
				List<Movie> likedMovies= new ArrayList<>();
				for(int z= j- 1; z>= 0; z--) {
					TTUserRating ttUserRating= ttUser.getUserRatings().get(z);
					if(ttUserRating.getRating()> 2.5)	ds.add(movies.get(ttUserRating.getMovieId()).getValues());
					else	ds.add(MatrixManipulation.negativify(movies.get(ttUserRating.getMovieId()).getValues()));
					likedMovies.add(movies.get(ttUser.getUserRatings().get(z).getMovieId()));
				}
				if(likedMovies.size()== 0)	continue;
				
				Movie toBeRecommended= movies.get(ttUser.getUserRatings().get(j).getMovieId());
//				System.out.println(toBeRecommended);
				
				Movie[] recommendedMovies= Engine.getRecommendationYearBased(movies, likedMovies, UV, 15, year);
//				System.out.println(Arrays.toString(recommendedMovies));
//				System.out.println("----------------");
				for(Movie recommendedMovie: recommendedMovies)	if(toBeRecommended== recommendedMovie)	trueRecommendation++;
				totalPrediction++;
			}
		}
		
		System.out.println(trueRecommendation);
		System.out.println(totalPrediction);
	}
	
	// This prints out how many correct predictions we've had using euclidean
	public static void euclideanMain(){
		List<Genre> genres= JsonConverter.readGenres(); // Reads the genres from json
		List<Movie> movies= JsonConverter.readMovies(genres); // Reads the movies from json
		IO.loadMV(movies, Constants.movie_values_TFIDF);
		Map<Map<Integer, Double>, Map<Integer, Double>> data= new HashMap<>();
		List<TTUser> ttUsers= DBConnection.getMLUserbase();
		int dataSize= 10;

		for (int j= 0; j< dataSize; j++){
			TTUser ttUser= ttUsers.get(j);
			List<Map<Integer, Double>> outputs= new ArrayList<>();
			ArrayList<Entry<Integer, Double>> movieId_rating= new ArrayList<>(ttUser.getMovieIds().entrySet());
			System.out.println("User "+ j+ " with movie count "+ movieId_rating.size());
			for (int i= 0; i< movieId_rating.size(); i++){
				int movieId= movieId_rating.get(i).getKey();
				Movie movie= Movie.getMovieById(movieId, movies);
				// If movie hasn't been retrieved from db
				if (movie== null){
					System.out.println("Movie is null "+ movieId_rating.get(i).getKey());
					movie= DBConnection.updateMovieIdML(movies, movieId);
					if (movie== null){// If still couldn't be found fk it
						System.out.println("Movie was null");
						continue;
					}
				}

				if (movieId_rating.get(i).getValue()> 2.5)
					outputs.add(movie.sparseVector);
				else
					outputs.add(MatrixManipulation.negativify(movie.sparseVector));
			}
			if (outputs.size()== 0)
				continue;

			// Sum of previous - next
			for (int i= 0; i< outputs.size()- 1; i++){
				List<Map<Integer, Double>> tmpInput= new ArrayList<>();
				for (int k= i; k>= 0; k--)
					tmpInput.add(outputs.get(k));
				data.put( MatrixManipulation.averageSparse( MatrixManipulation.sparseVectorListToVectorList(tmpInput, movies.get(0).lastPosition)),
						outputs.get(i+ 1));
			}

			// Sum of all - one by one
			// for(int i= 0; i< outputs.size(); i++) {
			// data.put(MatrixManipulation.averageSparse(MatrixManipulation.sparseVectorListToVectorList(outputs,
			// movies.get(0).lastPosition)), outputs.get(i));
			// }

			//
		}
		EuclideanDistanceTester.testOneForOneSparse(data, movies);
		EuclideanDistanceTester.testOneForOneParallel(data, movies);
	}

	public static void euclideanSum(){
		List<Genre> genres= JsonConverter.readGenres(); // Reads the genres from json
		List<Movie> movies= JsonConverter.readMovies(genres); // Reads the movies from json
		IO.loadSVDMV(movies, Constants.movie_values_SVD);
		for (int i= 0; i< movies.size(); i++){
			if (movies.get(i).getMlId().equals("0")){
				movies.remove(i);
				i--;
				continue;
			}
		}
		List<TTUser> ttUsers= DBConnection.getMLUserbase();

		Map<double[], double[]> data= new HashMap<>();
		for (int j= 0; j< ttUsers.size(); j++){
			TTUser ttUser= ttUsers.get(j);
			List<double[]> outputs= new ArrayList<>();
			ArrayList<Entry<Integer, Double>> movieId_rating= new ArrayList<>(ttUser.getMovieIds().entrySet());
			
			System.out.println("User "+ j+ " with movie count "+ movieId_rating.size()+ " from size "+ ttUsers.size());
			
			for (int i= 0; i< movieId_rating.size(); i++){
				int movieId= movieId_rating.get(i).getKey();
				Movie movie= Movie.getMovieById(movieId, movies);
				// If movie hasn't been retrieved from db
				if (movie== null){
					continue;
				}

				if (movieId_rating.get(i).getValue()> 2.5)
					outputs.add(MatrixManipulation.positivify(movie.getValues()));
				else
					outputs.add(MatrixManipulation.negativify(movie.getValues()));
			}
			if (outputs.size()== 0)
				continue;

			// Sum of previous - next
			for (int i= 0; i< outputs.size()- 1; i++){
				List<double[]> tmpInput= new ArrayList<>();
				for (int k= i; k>= 0; k--)
					tmpInput.add(outputs.get(k));
				data.put(MatrixManipulation.sum(tmpInput), outputs.get(i+ 1));
			}
		}
		System.out.print("Results for ");
		System.out.println(data.size());
		EuclideanDistanceTester.testOneForOne(data, movies);
	}

	
}
