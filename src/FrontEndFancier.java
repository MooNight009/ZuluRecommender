

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

import org.apache.commons.text.similarity.JaroWinklerDistance;

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
@WebServlet("/FrontEnd2")
public class FrontEndFancier extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private List<Genre> genres;
	private List<Movie> movies;
	private List<Map<Integer, Double>> UV;
	
	private Movie[] recommendedMovies= null;
	private Movie lastWatchedMovie= null;
	
	private boolean USOnly= false;
    
    public FrontEndFancier() {
        super();
        System.out.println("Got here");
        genres= JsonConverter.readGenres();	//	Reads the genres from json
		movies= JsonConverter.readMovies(genres);	//	Reads the movies from json
		System.out.println("Loaded movies");
		UV= IO.loadUV(Constants.user_values);
		System.out.println("Loaded user ratings");
		IO.loadSVDMV(movies, Constants.movie_values_SVD);
		System.out.println("Loaded movie values");
		IO.updateCluster(movies);
		System.out.println("Loaded movie cluster");
//		DBConnection.init();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		PrintWriter out= response.getWriter();
		response.setContentType("text/html");
		out.println("<head>");
		out.println("<link rel='stylesheet' href='../FYP/css/css.css'>");
		out.println("</head>");
		out.println("<body>");
		out.println("<div class='grid-container'>");	//	START GRID CONTRAINER
		
		
		
		out.println("<div class='Input'>");	//	START INPUT
		out.println("<form action= 'FrontEnd2' method= 'post'>");
		out.println("<br>Pleas Enter a movie<br>");
		out.println("<input type= 'text' name= 'entered_movie' class='MovieInput'/><br><br>");
		out.println("<input type= 'submit' name= 'like_movie' value= 'Like' class='SubmitButtons'>");
		out.println("<input type= 'submit' name= 'dislike_movie' value= 'Dislike' class='SubmitButtons'>");
		out.println("<input type= 'submit' name= 'remove_movie' value= 'Remove' class='SubmitButtons'>");
//		out.println("</form>");
		

		
		out.println("<br><br>");
//		out.println("<form action= 'FrontEnd2' method= 'post'>");
		out.println("<input type= 'submit' name= 'reset' value= 'Reset' class='SubmitButtons'>");
		out.println(" <b> US Only </b> ");
		if(request.getParameter("usOnly")!= null) {
			out.println("<label class='switch'>\r\n" + 
					"      <input type='checkbox' name= 'usOnly' onChange='this.form.submit()' checked>\r\n" + 
					"      <span class='slider round'></span>\r\n" + 
					"    </label>");
		}
		else{
			out.println("<label class='switch'>\r\n" + 
					"      <input type='checkbox' name= 'usOnly' onChange='this.form.submit()'>\r\n" + 
					"      <span class='slider round'></span>\r\n" + 
					"    </label>");
		}
		out.println("</form>");
		
		
		out.println("</div>");	//	END INPUT
		
		
		out.println("<div class='Content'>");	//	START CONTENT
		
		
		out.println("<div class='History'>");	//	START HISTORY
		
		out.println("<div class='Last-entered-movie'>");	//	START LAST ENTERED MOVIE
		out.println("<h2 style='font-size:30px; text-align:center;'>Last entered movie</h2><br>");
		if(lastWatchedMovie!= null) {
			System.out.println(request.getParameter("entered_movie"));
			
			out.println("<b>Name    :</b>"+ lastWatchedMovie.getName()+ "<br>");
			out.println("<b>Rating  :</b>"+ lastWatchedMovie.getAverageRating()+ "<br>");
			out.println("<b>Votes   :</b>"+ lastWatchedMovie.getNumberOfVotes()+ "<br>");
			out.println("<b>Year    :</b>"+ lastWatchedMovie.getYearOfRelease()+ "<br>");
			out.println("<b>Length  :</b>"+ lastWatchedMovie.getLength()+ "<br>");
			out.println("<b>Region  :</b>"+ lastWatchedMovie.getLanguage()+ "<br>");
			out.println("<b>Genres  :</b>");
			for(Genre genre: lastWatchedMovie.getGenres())	out.println(genre + " ");
			out.println("<br>");
			out.println("<br>");
			
			out.println("<b>Confidence in matching :</b>"+ (new JaroWinklerDistance()).apply(lastWatchedMovie.getName(), request.getParameter("entered_movie")));
			
		}
		out.println("</div>");	//	END LAST ENTERED MOVIE
		
		out.println("<div class='Current-List'>");	//	START CURRENT LIST
		out.println("<h2 style='font-size:30px; text-align:center;'>Current List</h2><br>");
		if(this.getLikedMovies().size()!= 0) {
			for(Movie movie: getLikedMovies()) {
				if(movie.getRating()== 1)	out.println("Liked    : "+ movie.getName());
				else						out.println("Disliked : "+ movie.getName());
				out.println("<br>");
			}
			
		}
		out.println("</div>");	//	END CURRENT LIST
		
		out.println("</div>");	//	END HISTORY
		
		
		
		out.println("<div class='Recommendation'>");	//	START RECOMMENDATION
		out.println("<h2 style='font-size:30px; text-align:center;'>Recommendation</h2><br>");
		if(recommendedMovies!= null) {
			for(int i= 0; i< recommendedMovies.length; i++) {
				if(recommendedMovies[i]== null)	continue;
				out.println("<b>"+ (i+ 1)+ ". </b>"+ recommendedMovies[i].getName());
				out.println("<br><br>");
			}
		}
		out.println("</div>");	//	END RECOMMENDATION
		
		out.println("</div>");	//	END CONTENT
		
		out.println("</div>");	//	END GRID CONTRAINER
		out.println("</body>");
	}

	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		if(request.getParameter("like_movie")!= null) {
			lastWatchedMovie= Movie.getMovieByName(movies, request.getParameter("entered_movie"), false);
			lastWatchedMovie.setRating(1);
			recommendedMovies= Engine.getRecommendation(movies, getLikedMovies(), UV, 9, USOnly);
		}
		if(request.getParameter("dislike_movie")!= null) {
			lastWatchedMovie= Movie.getMovieByName(movies, request.getParameter("entered_movie"), false);
			lastWatchedMovie.setRating(-1);
			recommendedMovies= Engine.getRecommendation(movies, getLikedMovies(), UV, 9, USOnly);
		}

		if(request.getParameter("remove_movie")!= null) {
			lastWatchedMovie= Movie.getMovieByName(movies, request.getParameter("entered_movie"), false);
			lastWatchedMovie.setRating(99);
			if(getLikedMovies().size()!= 0) {
				lastWatchedMovie= null;
				recommendedMovies= Engine.getRecommendation(movies, getLikedMovies(), UV, 9, USOnly);
			}
			else {
				lastWatchedMovie= null;
				recommendedMovies= null;
			}
		}
		
		if(request.getParameter("usOnly")== null) {
			if(USOnly!= false) {
				USOnly= false;
				if(getLikedMovies().size()!= 0) {
					recommendedMovies= Engine.getRecommendation(movies, getLikedMovies(), UV, 9, USOnly);
					lastWatchedMovie= null;
				}
			}
		}
		else{
			if(USOnly!= true) {
				USOnly= true;
				if(getLikedMovies().size()!= 0) {
					recommendedMovies= Engine.getRecommendation(movies, getLikedMovies(), UV, 9, USOnly);
					lastWatchedMovie= null;
				}
			}
		}
		System.out.println("USONLY IS "+ USOnly);
		
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
