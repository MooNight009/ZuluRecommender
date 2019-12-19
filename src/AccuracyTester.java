import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import config.Constants;
import core.DBConnection;
import core.EncogNeuralNet;
import core.Engine;
import core.EuclideanDistanceTester;
import entity.Actor;
import entity.Director;
import entity.Genre;
import entity.Movie;
import entity.TTUser;
import entity.Writer;
import utilities.IO;
import utilities.JsonConverter;
import utilities.MatrixManipulation;

public class AccuracyTester{

	public static void main(String[] args){
		DBConnection.init();
//		nnMain();
		euclideanSum();
		// euclideanConvFullValue();
		DBConnection.end();
	}

	public static void nnMainSparse(){
		List<Genre> genres= JsonConverter.readGenres(); // Reads the genres from json
		List<Movie> movies= JsonConverter.readMovies(genres); // Reads the movies from json
		IO.loadMV(movies, Constants.movie_values_TFIDF);
		Map<double[], double[]> ioTraining= new HashMap<>();
		Map<double[], double[]> ioTesting= new HashMap<>();
		List<TTUser> ttUsers= DBConnection.getMLUserbase();
		int trainingSize= 10;
		int testingSize= 5;

		for (int j= 0; j< trainingSize; j++){
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

			// Sum of previous

			for (int i= 0; i< outputs.size()- 1; i++){
				List<Map<Integer, Double>> tmpInput= new ArrayList<>();
				for (int k= i; k>= 0; k--)
					tmpInput.add(outputs.get(k));
				ioTraining.put(
						MatrixManipulation.average(
								MatrixManipulation.sparseVectorListToVectorList(tmpInput, movies.get(0).lastPosition)),
						MatrixManipulation.sparseVectorToVector(outputs.get(i+ 1), movies.get(i).lastPosition));
			}
		}

		for (int j= 0; j< testingSize; j++){
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

			// Sum of previous

			for (int i= 0; i< outputs.size()- 1; i++){
				List<Map<Integer, Double>> tmpInput= new ArrayList<>();
				for (int k= i; k>= 0; k--)
					tmpInput.add(outputs.get(k));
				ioTesting.put(
						MatrixManipulation.average(
								MatrixManipulation.sparseVectorListToVectorList(tmpInput, movies.get(0).lastPosition)),
						MatrixManipulation.sparseVectorToVector(outputs.get(i+ 1), movies.get(i).lastPosition));
			}
		}

		System.out.println("Training size "+ ioTraining.size());
		System.out.println("Testing  size "+ ioTesting.size());
		// NeuralNet nn= new NeuralNet();
		// nn.train(io);
		EncogNeuralNet.train(ioTraining, ioTesting);
	}

	public static void nnMain(){
		List<Genre> genres= JsonConverter.readGenres(); // Reads the genres from json
		List<Movie> movies= JsonConverter.readMovies(genres); // Reads the movies from json
		IO.loadSVDMV(movies, Constants.movie_values_SVD);
		Map<double[], double[]> ioTraining= new LinkedHashMap<>();
		Map<double[], double[]> ioTesting= new LinkedHashMap<>();
		List<TTUser> ttUsers= DBConnection.getMLUserbase();
		int trainingSize= 8000;
		int testingSize= 2000;

		loop: for (int j= 0; j< ttUsers.size(); j++){
			TTUser ttUser= ttUsers.get(j);
			List<double[]> outputs= new ArrayList<>();
			ArrayList<Entry<Integer, Double>> movieId_rating= new ArrayList<>(ttUser.getMovieIds().entrySet());
			System.out.println("User "+ j+ " with movie count "+ movieId_rating.size()+ " with training size "
					+ ioTraining.size()+ " and testing size "+ ioTesting.size());
			for (int i= 0; i< movieId_rating.size(); i++){
				int movieId= movieId_rating.get(i).getKey();
				Movie movie= Movie.getMovieById(movieId, movies);
				// If movie hasn't been retrieved from db
				if (movie== null){
					continue;
				}

				if (movieId_rating.get(i).getValue()> 2.5)
					outputs.add(movie.getValues());
				else
					outputs.add(MatrixManipulation.negativify(movie.getValues()));
			}
			if (outputs.size()== 0)
				continue;

			// Sum of previous

			for (int i= 0; i< outputs.size()- 1; i++){
				List<double[]> tmpInput= new ArrayList<>();
				for (int k= i; k>= 0; k--)
					tmpInput.add(outputs.get(k));

				if (ioTraining.size()< trainingSize)
					ioTraining.put(MatrixManipulation.sum(tmpInput), outputs.get(i+ 1));
				else if (ioTesting.size()< testingSize)
					ioTesting.put(MatrixManipulation.sum(tmpInput), outputs.get(i+ 1));
				else
					break loop;
			}
		}

		System.out.println("Training size "+ ioTraining.size());
		System.out.println("Testing  size "+ ioTesting.size());
		// NeuralNet nn= new NeuralNet();
		// nn.train(io);
		// EncogNeuralNet.train(ioTraining, ioTesting);
		EncogNeuralNet.trainRNN(ioTraining, ioTesting);
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
				data.put(
						MatrixManipulation.averageSparse(
								MatrixManipulation.sparseVectorListToVectorList(tmpInput, movies.get(0).lastPosition)),
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

	public static void euclideanConv(){
		List<Genre> genres= JsonConverter.readGenres(); // Reads the genres from json
		List<Movie> movies= JsonConverter.readMovies(genres); // Reads the movies from json
		IO.loadMV(movies, Constants.movie_values_TFIDF);
		for (int i= 0; i< movies.size(); i++){
			if (movies.get(i).getMlId().equals("0")){
				movies.remove(i);
				i--;
				continue;
			}

			movies.get(i).setValues(
					MatrixManipulation.sparseVectorToVector(movies.get(i).sparseVector, movies.get(i).lastPosition));
		}
		List<TTUser> ttUsers= DBConnection.getMLUserbase();
		int dataSize= 20;
		Random r= new Random();
		List<double[][]> filters= new ConvolutionalFilterCreater(3).getList();

		while (filters.size()> 0){
			int n= r.nextInt(filters.size());
			// double[][] filter= filters.get(n);
			double[][] filter= { { 0.0, 1.0, 2.0}, { 2.0, 0.0, 2.0}};
			filters.remove(n);

			Map<double[], double[]> data= new HashMap<>();
			for (int j= 0; j< dataSize; j++){
				TTUser ttUser= ttUsers.get(j);
				List<double[]> outputs= new ArrayList<>();
				ArrayList<Entry<Integer, Double>> movieId_rating= new ArrayList<>(ttUser.getMovieIds().entrySet());
				if (movieId_rating.size()> 100)
					continue;
				// System.out.println("User "+ j+ " with movie count "+ movieId_rating.size());
				for (int i= 0; i< movieId_rating.size(); i++){
					int movieId= movieId_rating.get(i).getKey();
					Movie movie= Movie.getMovieById(movieId, movies);
					// If movie hasn't been retrieved from db
					// if(movie== null) {
					// System.out.println("Movie is null "+ movieId_rating.get(i).getKey());
					// movie= DBConnection.updateMovieIdML(movies, movieId);
					// if(movie== null) {// If still couldn't be found fk it
					// System.out.println("Movie was null");
					// continue;
					// }
					// }

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
					data.put(MatrixManipulation.convlutionalFilter(tmpInput, filter), outputs.get(i+ 1));
				}

			}
			System.out.print("Results for ");
			for (double[] row : filter)
				System.out.print(Arrays.toString(row)+ ",");
			System.out.println(data.size());
			EuclideanDistanceTester.testOneForOne(data, movies);
		}
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

	public static void euclideanConvFullValue(){
		List<Genre> genres= JsonConverter.readGenres(); // Reads the genres from json
		List<Movie> movies= JsonConverter.readMovies(genres); // Reads the movies from json
		// IO.loadMV(movies, Constants.movie_values);
		IO.loadSVDMV(movies, Constants.movie_values_SVD);
		for (int i= 0; i< movies.size(); i++){
			if (movies.get(i).getMlId().equals("0")){
				movies.remove(i);
				i--;
				continue;
			}
		}

		List<TTUser> ttUsers= DBConnection.getMLUserbase();
		int dataSize= ttUsers.size()/ 10;
		Random r= new Random();
		List<double[][]> filters= new ConvolutionalFilterCreater(3).getList();
		double lowestDistance= Double.MAX_VALUE;
		double[][] bestFilter= null;

		while (filters.size()> 0){
			int n= r.nextInt(filters.size());
			double[][] filter= filters.get(n);
			// double[][] filter= {{0.0, 1.0, 2.0}, {2.0, 0.0, 2.0}};
			filters.remove(n);

			Map<double[], double[]> data= new HashMap<>();
			List<double[]> input= new ArrayList<>();
			List<double[]> output= new ArrayList<>();
			for (int j= 0; j< dataSize; j++){
				TTUser ttUser= ttUsers.get(j);
				List<double[]> outputs= new ArrayList<>();
				ArrayList<Entry<Integer, Double>> movieId_rating= new ArrayList<>(ttUser.getMovieIds().entrySet());
				// System.out.println("User "+ j+ " with movie count "+ movieId_rating.size());
				if (movieId_rating.size()> 200)
					continue;

				for (int i= 0; i< movieId_rating.size(); i++){
					int movieId= movieId_rating.get(i).getKey();
					Movie movie= Movie.getMovieById(movieId, movies);
					if (movie== null)
						continue;

					if (movieId_rating.get(i).getValue()> 2.5)
						outputs.add(movie.getValues());
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
					double[] xTest= MatrixManipulation.convlutionalFilter(tmpInput, filter);
					if (i== 0){
						data.put(tmpInput.get(0), outputs.get(i+ 1));
						input.add(tmpInput.get(0));
						output.add(outputs.get(i+ 1));
					} else{
						if (data.containsKey(xTest))
							System.out.println("Isue");
						data.put(xTest, outputs.get(i+ 1));
						input.add(xTest);
						output.add(outputs.get(i+ 1));
					}
				}

			}

			// System.out.print("Results for ");
			// for (double[] row : filter) System.out.print(Arrays.toString(row)+ ",");
			// System.out.println(data.size());
			double distance= EuclideanDistanceTester.testDistance(input, output, movies);
			// System.out.println("Distance is "+ distance);

			if (distance< lowestDistance){
				lowestDistance= distance;
				bestFilter= filter;
			}

			if (filters.size()% 10== 0){
				System.out.println("Lowest distance is "+ lowestDistance);
				for (double[] row : bestFilter)
					System.out.print(Arrays.toString(row)+ ",");
				System.out.println("----------------------");
			}
		}
	}

	public static void ressetEveryThing(List<Genre> genres, List<Movie> movies){
		for (Movie movie : movies){
			movie.setRating(99);
			movie.setRecommendationRating(0.0);
		}

		for (Genre genre : genres){
			genre.setSessionScore(0.0);
			genre.setSessionCount(0);
		}
	}

	private static void oldMain(){
		Random rand= new Random();

		List<Genre> genres= JsonConverter.readGenres(); // Reads the genres from json
		List<Movie> movies= JsonConverter.readMovies(genres); // Reads the movies from json

		List<TTUser> ttUsers= DBConnection.getMLUserbase();

		int n= 0;
		for (TTUser ttUser : ttUsers){
			if (n++== 10)
				break;
			ressetEveryThing(genres, movies);
			ArrayList<Entry<Integer, Double>> movieId_rating= new ArrayList<>(ttUser.getMovieIds().entrySet());
			for (int i= 0; i< ttUser.getTrainingMovieCount(); i++){
				int movieId= movieId_rating.get(i).getKey();
				Movie movie= Movie.getMovieById(movieId, movies);
				// If movie hasn't been retrieved from db
				if (movie== null){
					movie= DBConnection.updateMovieIdML(movies, movieId);
					if (movie== null){// If still couldn't be found fk it
						continue;
					}
				}

				int rating;
				if (movieId_rating.get(i).getValue()> 2.5)
					rating= 1;
				else
					rating= -1;

				movie.setRating(rating);
				for (Genre movieGenre : movie.getGenres())
					movieGenre.setSessionCount(movieGenre.getSessionCount()+ rating);
			}

			// Now we get actors and other stuff
			List<Writer> writers= new ArrayList<>();
			for (Movie movie : movies)
				if (movie.getRating()!= 99)
					DBConnection.getWriters(movie, writers);
			for (Writer writer : writers)
				DBConnection.getWriterMovies(movies, writer);

			List<Director> directors= new ArrayList<>();
			for (Movie movie : movies)
				if (movie.getRating()!= 99)
					DBConnection.getDirectors(movie, directors);
			for (Director director : directors)
				DBConnection.getDirectorMovies(movies, director);

			List<Actor> actors= new ArrayList<>();
			for (Movie movie : movies)
				if (movie.getRating()!= 99)
					DBConnection.getActors(movie, actors);
			for (Actor actor : actors)
				DBConnection.getActorMovies(movies, actor);

			// Run the engine
			Genre.updateScores(genres);
			// Engine.updateMovieRecommendation(movies);
			// Engine.updateMovieRecommendationBasedOnDirector(directors);
			// Engine.updateMovieRecommendationBasedOnWriter(writers);
			// Engine.updateMovieRecommendationBasedOnActor(actors);
			Engine.genreWDRecommendation(movies, directors, writers);
			Engine.actorRecommendation(actors);
			Engine.finalRecommendationUpdate(movies);

			// Sort the movies and see if they are recommended
			movies.sort(new Comparator<Movie>(){
				public int compare(Movie a, Movie b){
					return Double.compare(b.getRecommendationRating(), a.getRecommendationRating());
				}
			});

			for (int i= ttUser.getTrainingMovieCount(); i< movieId_rating.size(); i++){
				int movieId= movieId_rating.get(i).getKey();
				Movie movie= Movie.getMovieById(movieId, movies);
				// If movie hasn't been retrieved from db
				if (movie== null){
					movie= DBConnection.updateMovieIdML(movies, movieId);
					if (movie== null){// If still couldn't be found fk it
						continue;
					}
				}

				for (int j= 0; j< 10; j++)
					if (movies.get(j)== movie)
						ttUser.setTestingCorrectRecommendation(ttUser.getTestingCorrectRecommendation()+ 1);
			}
			System.out.println(
					"Predicted "+ ttUser.getTestingCorrectRecommendation()+ " from "+ ttUser.getTestingMovieCount());
		}

	}
}
