package core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import config.Constants.DM;
import entity.Actor;
import entity.Director;
import entity.Genre;
import entity.Movie;
import entity.Writer;
import utilities.MatrixManipulation;

public class Engine{

	//	This is the main recommendation
	//	It returns 8 recommendations, 6 from content, 2 from collab
	public static Movie[] getRecommendation(List<Movie> movies, List<Movie> likedMovies, List<Map<Integer, Double>> UV, int recommendationSize, boolean USBased) {
		Movie[] recommendedMovies= new Movie[recommendationSize];
		
		Map<Integer, Integer> clusterScore= Cluster.getClusterScore(likedMovies);
		
		
		Movie[] cb= MatrixManipulation.getClosestMovieCluster(MatrixManipulation.sumMovieValues(likedMovies), movies, 5, clusterScore, DM.DM_EuclideanDistance, likedMovies, USBased);
		System.out.println(Arrays.toString(cb));
		for(int i= 0; i< cb.length; i++) {
			recommendedMovies[i]= cb[i];
		}
		System.out.println(Arrays.toString(recommendedMovies));
		
		
		
		Movie[] cbRating= MatrixManipulation.getClosestMovieClusterRatingEdition(MatrixManipulation.sumMovieValues(likedMovies), movies, recommendationSize, clusterScore, DM.DM_EuclideanDistance, likedMovies, USBased);
		System.out.println(Arrays.toString(cbRating));
		
		int n= 5;
		loop:
		for(int i= 0; i< cbRating.length; i++) {
			if(n== 7)	break;
			for(Movie movie: recommendedMovies)	if(movie!= null&& movie.equals(cbRating[i]))	continue loop;
			recommendedMovies[n]= cbRating[i];
			n++;
		}
		System.out.println(Arrays.toString(recommendedMovies));
		
		
		for(int i= 0; i< likedMovies.size(); i++) {
			if(likedMovies.get(i).getMlId().equals("0")) {
				System.out.println("Removed "+ likedMovies.get(i));
				likedMovies.remove(i);
				i--;
			}
		}
		
		if(likedMovies.size()!= 0) {
			Movie[] cf= CollabRecommender.getRecommendationsCluster(movies, UV, CollabRecommender.getUserRating(movies, likedMovies), Cluster.getClusterScore(likedMovies), MatrixManipulation.sumMovieValues(likedMovies), recommendationSize* 3, likedMovies, USBased);
			System.out.println(Cluster.getClusterScore(likedMovies));
			System.out.println(CollabRecommender.getUserRating(movies, likedMovies));
			System.out.println(likedMovies);
			System.out.println(Arrays.toString(cf));
			
			loop:
			for(int i= 0; i< cf.length; i++) {
				if(n== 9)	break;
	//			recommendedMovies[i+ cb.length]= cf[i];
				for(Movie movie: recommendedMovies)	if(movie!= null&& movie.equals(cf[i]))	continue loop;
				recommendedMovies[n]= cf[i];
				n++;
			}
			System.out.println(Arrays.toString(recommendedMovies));
		}
		
		return recommendedMovies;
	}
	
	public static Movie[] getRecommendationYearBased(List<Movie> movies, List<Movie> likedMovies, List<Map<Integer, Double>> UV, int recommendationSize, int year) {
		List<Movie> olderMovies= new ArrayList<>();
		for(Movie movie: movies) if(movie.getYearOfRelease()<= year)	olderMovies.add(movie);
		
		Movie[] recommendedMovies= new Movie[recommendationSize];
		
		Map<Integer, Integer> clusterScore= Cluster.getClusterScore(likedMovies);
		
		
		Movie[] cb= MatrixManipulation.getClosestMovieCluster(MatrixManipulation.sumMovieValues(likedMovies), olderMovies, 2* recommendationSize/ 3, clusterScore, DM.DM_EuclideanDistance, likedMovies, false);
		for(int i= 0; i< cb.length; i++) {
			recommendedMovies[i]= cb[i];
		}
		
//		MatrixManipulation.getClosestMovieClusterRatingEdition(MatrixManipulation.sumMovieValues(likedMovies), movies, 3, clusterScore, DM.DM_EuclideanDistance);
		
		Movie[] cf= CollabRecommender.getRecommendationsClusterYear(movies, UV, CollabRecommender.getUserRating(movies, likedMovies), clusterScore, MatrixManipulation.sumMovieValues(likedMovies), recommendationSize/ 3, year, likedMovies);
		for(int i= 0; i< cf.length; i++) {
			recommendedMovies[i+ cb.length]= cf[i];
		}
		
		return recommendedMovies;
	}
	
	public static void updateMovieRecommendation(List<Movie> movies) {
		for(Movie movie: movies) {
			movie.setRecommendationRating(0.0);
			if(movie.getRating()== 1|| movie.getRating()== -1)	continue;
			double recommendationScore= 0.0;
			for(Genre genre: movie.getGenres()) {
				recommendationScore+= genre.getSessionScore();
			}
			movie.setRecommendationRating(recommendationScore);
		}
	}
	
	public static void updateMovieRecommendationBasedOnDirector(List<Director> directors) {
		for(Director director: directors) {
			for(Movie movie: director.getMovies()) {
				movie.setRecommendationRating(movie.getRecommendationRating()* (1.3+ director.getMovieCount()* 0.2));
			}
		}
	}
	
	public static void updateMovieRecommendationBasedOnWriter(List<Writer> writers) {
		for(Writer writer: writers) {
			for(Movie movie: writer.getMovies()) {
				movie.setRecommendationRating(movie.getRecommendationRating()* (1.1+ writer.getMovieCount()* 0.1));
			}
		}
	}
	
	public static void updateMovieRecommendationBasedOnActor(List<Actor> actors) {
		for(Actor actor: actors) {
			for(Movie movie: actor.getMovies()) {
				movie.setRecommendationRating(movie.getRecommendationRating()* (1.2+ actor.getMovieCount()* 0.15));
			}
		}
	}
	
	//	Gets recommendation score for genre writer and director
	public static void genreWDRecommendation(List<Movie> movies, List<Director> directors, List<Writer> writers) {
		
		//	This sets the genre base rating
		for(Movie movie: movies) {
			movie.setGenreRecommendationRating(0.0);
			movie.setGenreDirectorRecommendationRating(0.0);
			movie.setGenreWriterRecommendationRating(0.0);
			movie.setActorRecommendationRating(0.0);
			if(movie.getRating()== 1|| movie.getRating()== -1)	continue;
			double recommendationScore= 0.0;
			for(Genre genre: movie.getGenres()) {
				recommendationScore+= genre.getSessionScore();
			}
			movie.setGenreRecommendationRating(recommendationScore);
		}
		
		//	Sort movies based on genreRecommendation and update position
		movies.sort(new Comparator<Movie>() {
			public int compare(Movie a, Movie b) {
				return Double.compare(b.getGenreRecommendationRating(), a.getGenreRecommendationRating());
			}
		});
		for(int i= 0; i< movies.size(); i++)	movies.get(i).setGenreRatingPosition(i);
		
		//	START UPDATE THE TOP X MOVIES WITH WRITER AND DIRECTOR
		int topX= 10000;
		int topY= 100;
		
		//	Director
		for(Director director: directors) {
			double movieGenreTotal= 0.0;
			for(Movie movie: director.getMovies()) {
				movieGenreTotal+= movie.getGenreRecommendationRating();
			}
			
			double directorScore= Math.pow(Math.pow(movieGenreTotal, 2)* director.getMovieCount(), 1D/2.0);
			directorScore= Math.max(directorScore, 1.0);
			for(Movie movie: director.getMovies()) {
				if(movie.getGenreRatingPosition()< topX) {
					movie.setGenreDirectorRecommendationRating(directorScore+ movie.getGenreDirectorRecommendationRating());
				}
			}
		}
		
		//	Writer
		for(Writer writer: writers) {
			double movieGenreTotal= 0.0;
			for(Movie movie: writer.getMovies()) {
				movieGenreTotal+= movie.getGenreRecommendationRating();
			}
			
			double writerScore= Math.pow(Math.pow(movieGenreTotal, 2)* writer.getMovieCount(), 1D/3.0);
			writerScore= Math.max(writerScore, 1.0);
			for(Movie movie: writer.getMovies()) {
				if(movie.getGenreRatingPosition()< topX) {
					movie.setGenreWriterRecommendationRating(writerScore+ movie.getGenreWriterRecommendationRating());
				}
			}
		}
		
		//	Sort movies based on director and writer and set the position
		movies.sort(new Comparator<Movie>() {
			public int compare(Movie a, Movie b) {
				return Double.compare(b.getGenreDirectorRecommendationRating(), a.getGenreDirectorRecommendationRating());
			}
		});
		for(int i= 0; i< movies.size(); i++)	movies.get(i).setGenreDirectorRatingPosition(i);
		
		movies.sort(new Comparator<Movie>() {
			public int compare(Movie a, Movie b) {
				return Double.compare(b.getGenreWriterRecommendationRating(), a.getGenreWriterRecommendationRating());
			}
		});
		for(int i= 0; i< movies.size(); i++)	movies.get(i).setGenreWriterRatingPosition(i);
		
		//	Now update the genreRating based on all
		for(Movie movie: movies) {
			if(movie.getGenreRatingPosition()< topX) {
				double genreRating= movie.getGenreRecommendationRating();
				if(movie.getGenreDirectorRatingPosition()< topY) {
					genreRating= genreRating* movie.getGenreDirectorRecommendationRating();
				}
				if(movie.getGenreWriterRatingPosition()< topY) {
					genreRating= genreRating* movie.getGenreWriterRecommendationRating();
				}
				if(movie.getGenreWriterRatingPosition()< topY&& movie.getGenreDirectorRatingPosition()< topY) {
					genreRating= genreRating* 2;
				}
				movie.setGenreRecommendationRating(genreRating);
			}
		}
		//		Sort movies based on genreRecommendation and update position
		movies.sort(new Comparator<Movie>() {
			public int compare(Movie a, Movie b) {
				return Double.compare(b.getGenreRecommendationRating(), a.getGenreRecommendationRating());
			}
		});
		for(int i= 0; i< movies.size(); i++)	movies.get(i).setGenreRatingPosition(i);
	}

	//	Gets recommendation score for actor
	public static void actorRecommendation(List<Actor> actors) {
		
		for(Actor actor: actors) {
			for(Movie movie: actor.getMovies()) {
				movie.setActorRecommendationRating(Math.pow(2, actor.getMovieCount())+ movie.getActorRecommendationRating());
			}
		}
	}
	
	//	This updates the finalScore
	public static void finalRecommendationUpdate(List<Movie> movies) {
		for(Movie movie: movies) {
			double actorScore= Math.max(movie.getActorRecommendationRating(), 1.0);
			movie.setRecommendationRating(actorScore* movie.getGenreRecommendationRating());
		}
		
//		movies.sort(new Comparator<Movie>() {
//			public int compare(Movie a, Movie b) {
//				return Double.compare(b.getRecommendationRating(), a.getRecommendationRating());
//			}
//		});
	}
}
