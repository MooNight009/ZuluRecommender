

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import config.Constants;
import core.DBConnection;
import core.Engine;
import entity.Genre;
import entity.Movie;
import utilities.IO;
import utilities.JsonConverter;

/**
 * Servlet implementation class FrontEnd
 */
@WebServlet("/FrontEnd")
public class FrontEnd extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private List<Genre> genres;
	private List<Movie> movies;
	private List<Map<Integer, Double>> UV;
	
	private Movie[] recommendedMovies= null;
	private Movie lastWatchedMovie= null;
       
	private static String userId= "2";
    
    public FrontEnd() {
        super();
        System.out.println("Got here");
//        genres= JsonConverter.readGenres();	//	Reads the genres from json
//		movies= JsonConverter.readMovies(genres);	//	Reads the movies from json
//		System.out.println("Loaded movies");
//		UV= IO.loadUV(Constants.user_values);
//		System.out.println("Loaded user ratings");
//		IO.loadSVDMV(movies, Constants.movie_values_SVD);
//		System.out.println("Loaded movie values");
//		IO.updateCluster(movies);
//		System.out.println("Loaded movie cluster");
//		DBConnection.init();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		PrintWriter out= response.getWriter();
		response.setContentType("text/html");
		out.println("<head>");
//		out.println("<link rel='stylesheet' href='../FYP/css/css.css'>");
		out.println("</head>");
		out.println("<body>");
		
		out.println("<form action= 'FrontEnd' method= 'post'>");
		out.println("<input type= 'text' name= 'entered_movie'/>");
		out.println("<input type= 'submit' name= 'like_movie' value= 'Like'>");
		out.println("<input type= 'submit' name= 'dislike_movie' value= 'Dislike'>");
		out.println("<input type= 'submit' name= 'remove_movie' value= 'Remove'>");
		out.println("</form>");
		
		
		if(lastWatchedMovie!= null) {
			out.println("Last printed movie was: "+ lastWatchedMovie.getJson()+ "<br><br>");
		}
		
		if(this.getLikedMovies().size()!= 0) {
			out.println("Previous list<br>");
			
			for(Movie movie: getLikedMovies())	out.println(movie.getName()+ " : "+ movie.getRating()+ "<br>");
			
			out.println("<br><br>");
		}
		
		if(recommendedMovies!= null) {
			for(Movie movie: this.recommendedMovies)	if(movie!= null)	out.println(movie.toString()+ "<br>");
		}
		
		
		out.println("<form action= 'FrontEnd' method= 'post'>");
		out.println("<input type= 'submit' name= 'reset' value= 'Reset'>");
		out.println("</form>");
		
		
		out.println("</body>");
	}

	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println();
		if(request.getParameter("like_movie")!= null) {
			lastWatchedMovie= Movie.getMovieByName(movies, request.getParameter("entered_movie"), false);
			lastWatchedMovie.setRating(1);
//			recommendedMovies= Engine.getRecommendation(movies, getLikedMovies(), UV, 9);
		}
		if(request.getParameter("dislike_movie")!= null) {
			lastWatchedMovie= Movie.getMovieByName(movies, request.getParameter("entered_movie"), false);
			lastWatchedMovie.setRating(-1);
//			recommendedMovies= Engine.getRecommendation(movies, getLikedMovies(), UV, 9);
		}

		if(request.getParameter("remove_movie")!= null) {
			lastWatchedMovie= Movie.getMovieByName(movies, request.getParameter("entered_movie"), false);
			lastWatchedMovie.setRating(99);
			if(getLikedMovies().size()!= 0) {
				lastWatchedMovie= null;
//				recommendedMovies= Engine.getRecommendation(movies, getLikedMovies(), UV, 9);
			}
			else {
				lastWatchedMovie= null;
				recommendedMovies= null;
			}
		}
		
		if(request.getParameter("reset")!= null) {
			reset();
		}
		
		doGet(request, response);
	}
	
	
	private List<Movie> getLikedMovies(){
		if(this.movies== null)	return new ArrayList<>();
		List<Movie> likedMovies= new ArrayList<>();
		
		for(Movie movie: this.movies) {
			if(movie.getRating()!= 99)	likedMovies.add(movie);
		}
		
		
		return likedMovies;
	}
	
	private void reset() {
		for(Movie movie: this.movies)	movie.setRating(99);
		lastWatchedMovie= null;
		recommendedMovies= null;
	}

}
