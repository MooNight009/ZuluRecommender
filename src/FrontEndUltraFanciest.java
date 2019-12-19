
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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import config.Constants;
import core.Crawler;
import core.DBConnection;
import core.Engine;
import entity.Genre;
import entity.Movie;
import utilities.IO;
import utilities.JsonConverter;

/**
 * Servlet implementation class FrontEnd
 */
@WebServlet("/FrontEnd6")
public class FrontEndUltraFanciest extends HttpServlet{
	private static final long serialVersionUID= 1L;

	private List<Genre> genres;
	private List<Movie> movies;
	private List<Map<Integer, Double>> UV;

	private Movie[] recommendedMovies= null;
	private Movie lastWatchedMovie= null;

	private boolean USOnly= false;

	public FrontEndUltraFanciest(){
		super();
		System.out.println("Got here");
		genres= JsonConverter.readGenres(); // Reads the genres from json
		movies= JsonConverter.readMovies(genres); // Reads the movies from json
		System.out.println("Loaded movies");
		 UV= IO.loadUV(Constants.user_values);
		System.out.println("Loaded user ratings");
		 IO.loadSVDMV(movies, Constants.movie_values_SVD);
		System.out.println("Loaded movie values");
		 IO.updateCluster(movies);
		System.out.println("Loaded movie cluster");
		// DBConnection.init();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		// TODO Auto-generated method stub
		PrintWriter out= response.getWriter();
		response.setContentType("text/html");
		out.println("<head>");
		out.println("<link rel='stylesheet' href='../FYP/css/css.css'>");
		out.println("<script>"); // START SCRIPT
		out.println("function openCity(evt, cityName) {\r\n"+ "  // Declare all variables\r\n"
				+ "  var i, tabcontent, tablinks;\r\n"+ "\r\n"
				+ "  // Get all elements with class=\"tabcontent\" and hide them\r\n"
				+ "  tabcontent = document.getElementsByClassName(\"tabcontent\");\r\n"
				+ "  for (i = 0; i < tabcontent.length; i++) {\r\n"+ "    tabcontent[i].style.display = \"none\";\r\n"
				+ "  }\r\n"+ "\r\n"+ "  // Get all elements with class=\"tablinks\" and remove the class \"active\"\r\n"
				+ "  tablinks = document.getElementsByClassName(\"tablinks\");\r\n"
				+ "  for (i = 0; i < tablinks.length; i++) {\r\n"
				+ "    tablinks[i].className = tablinks[i].className.replace(\" active\", \"\");\r\n"+ "  }\r\n"+ "\r\n"
				+ "  // Show the current tab, and add an \"active\" class to the button that opened the tab\r\n"
				+ "  document.getElementById(cityName).style.display = \"block\";\r\n"
				+ "  evt.currentTarget.className += \" active\";\r\n"+ "console.log(cityName== 'Card');"
//				+ "console.log(cityName);"+ "if(cityName=='Card'){"
//				+ "	document.getElementById('recommendationText').style.display= 'none';"+ "}"+ "else{"
//				+ "document.getElementById('recommendationText').style.display= 'block';"+ "}"+ "}");
				+"}");

		out.println("</script>"); // END SCRIPT
		out.println("</head>");
		out.println("<body>");
		out.println("<div class='grid-container'>"); // START GRID CONTRAINER

		out.println("<div class='Input'>"); // START INPUT
		out.println("<form action= 'FrontEnd6' method= 'post' style='margin-bottom: 0px;'>");
		out.println("<input type= 'text' name= 'entered_movie' class='MovieInput' autocomplete='off' placeholder='Please enter a movie'>");
		out.println("<input type= 'submit' name= 'like_movie' value= 'Like' class='SubmitButtons'>");
		out.println("<a style=\"font-size: 40px;color:  #808080;margin: 10;\">|</a>");
		out.println("<input type= 'submit' name= 'dislike_movie' value= 'Dislike' class='SubmitButtons'>");
		out.println("<a style=\"font-size: 40px;color:  #808080;margin: 10;\">|</a>");
		out.println("<input type= 'submit' name= 'remove_movie' value= 'Remove' class='SubmitButtons'>");
		out.println("<a style=\"font-size: 40px;color:  #808080;margin: 10;\">|</a>");
		out.println("<input type= 'submit' name= 'reset' value= 'Reset' class='SubmitButtons'>");
		out.println("<b style='font-size: 16px; margin-right: 15px; margin-left: 50px; color: white;'> US Only </b>");
		if (request.getParameter("usOnly")!= null) {
			out.println("<label class='switch'>\r\n"
					+ "      <input type='checkbox' name= 'usOnly' onChange='this.form.submit()' checked>\r\n"
					+ "      <span class='slider round'></span>\r\n"+ "    </label>");
		} else{
			out.println("<label class='switch'>\r\n"
					+ "      <input type='checkbox' name= 'usOnly' onChange='this.form.submit()'>\r\n"
					+ "      <span class='slider round'></span>\r\n"+ "    </label>");
		}
		out.println("</form>");

		out.println("</div>"); // END INPUT

		out.println("<div class='Content'>"); // START CONTENT

		out.println("<div class='History'>"); // START HISTORY

		out.println("<div class='Last-entered-movie'>"); // START LAST ENTERED MOVIE
		out.println("<h2 style='font-size:30px; text-align:center; background: #4d4d4d; margin: 0; color: white; padding: 15;''>Last entered movie</h2><br>");
		if (lastWatchedMovie!= null){
			String poster= Crawler.getDescAndPoster(lastWatchedMovie)[1];

			out.println("<div class='lastMovieCard'>"); // START MOVIE CARD

			out.println("<img style='display: block; float: left;' src='"+ poster+ "'>");

			out.println("<table style='padding-left: 5px; padding-top: 20px'>"); // START TABLE
			out.println("<tr><td style='width: 90px;'><b>Name  :</b></td><td>"+ lastWatchedMovie.getName()+ "</td></tr>");
			out.println("<tr><td style='width: 90px;'><b>Rating  :</b></td><td>"+ lastWatchedMovie.getAverageRating()+ "</td></tr>");
			out.println("<tr><td style='width: 90px;'><b>Votes   :</b></td><td>"+ lastWatchedMovie.getNumberOfVotes()+ "</td></tr>");
			out.println("<tr><td style='width: 90px;'><b>Year    :</b></td><td>"+ lastWatchedMovie.getYearOfRelease()+ "</td></tr>");
			out.println("<tr><td style='width: 90px;'><b>Length  :</b></td><td>"+ lastWatchedMovie.getLength()+ "</td></tr>");
			out.println("<tr><td style='width: 90px;'><b>Region  :</b></td><td>"+ lastWatchedMovie.getLanguage().replace("null", "Not US")+ "</td></tr>");
			out.println("<tr><td style='width: 90px;'><b>Genres  :</b></td><td>");
			for (int i= 0; i< lastWatchedMovie.getGenres().size(); i++) {
				Genre genre= lastWatchedMovie.getGenres().get(i);
				out.println(genre.toString().substring(0,1)+ genre.toString().toLowerCase().substring(1));
				if(i+1!= lastWatchedMovie.getGenres().size())	out.println(", ");
			}
			out.println("</td></tr>");
			if(request.getParameter("entered_movie")!= null&& request.getParameter("entered_movie").trim().length()!= 0) {
				out.println("<tr><td><b>Match :</b></td><td>"
						+ (new JaroWinklerDistance()).apply(lastWatchedMovie.getName(), request.getParameter("entered_movie"))+ "%</td></tr>");
			}
			out.println("</table>"); // END TABLE

			out.println("</div>"); // END MOVIE CARD
		}
		else {
//		 lastWatchedMovie= Movie.getMovieByName(movies, "Shrek", false);
//		
//		 String poster= Crawler.getDescAndPoster(lastWatchedMovie)[1];
//		
//		 out.println("<div class='lastMovieCard'>"); // START MOVIE CARD
//		
//		 out.println("<img style='display: block; float: left;' src='"+ poster+ "'>");
//		
//		 out.println("<table style='padding-left: 5px;'>"); // START TABLE
//		 out.println("<tr><td style='width: 90px;'><b>Name :</b></td><td>"+ lastWatchedMovie.getName()+"</td></tr>");
//		 out.println("<tr><td style='width: 90px;'><b>Rating :</b></td><td>"+ lastWatchedMovie.getAverageRating()+ "</td></tr>");
//		 out.println("<tr><td style='width: 90px;'><b>Votes :</b></td><td>"+ lastWatchedMovie.getNumberOfVotes()+ "</td></tr>");
//		 out.println("<tr><td style='width: 90px;'><b>Year :</b></td><td>"+ lastWatchedMovie.getYearOfRelease()+ "</td></tr>");
//		 out.println("<tr><td style='width: 90px;'><b>Length :</b></td><td>"+ lastWatchedMovie.getLength()+ "</td></tr>");
//		 out.println("<tr><td style='width: 90px;'><b>Region  :</b></td><td>"+ lastWatchedMovie.getLanguage().replace("null", "Not US")+ "</td></tr>");
//		 out.println("<tr><td style='width: 90px;'><b>Genres :</b></td><td>");
//		 for (int i= 0; i< lastWatchedMovie.getGenres().size(); i++) {
//				Genre genre= lastWatchedMovie.getGenres().get(i);
//				out.println(genre.toString().substring(0,1)+ genre.toString().toLowerCase().substring(1));
//				if(i+1!= lastWatchedMovie.getGenres().size())	out.println(", ");
//			}
//		 out.println("</td></tr>");
//		 out.println("<tr><td><b>Match :</b></td><td>"+ (new
//		 JaroWinklerDistance()).apply(lastWatchedMovie.getName(), "Shre")+ "%</td></tr>");
//		 out.println("</table>"); // END TABLE
//		
//		 out.println("</div>"); // END MOVIE CARD
		}
		out.println("</div>"); // END LAST ENTERED MOVIE

		out.println("<div class='Current-list'>"); // START CURRENT LIST
		out.println("<h2 style='font-size:30px; text-align:center; background: #4d4d4d; margin: 0; color: white; padding: 15;'>Current List</h2><br>");
		if (this.getLikedMovies().size()!= 0){
			out.println("<p style='padding-left: 10px'>");
			for (Movie movie : getLikedMovies()){
				if (movie.getRating()== 1)
					out.println("Liked    : "+ movie.getName());
				else
					out.println("Disliked : "+ movie.getName());
				out.println("<br>");
			}
			out.println("</p>");
		}
		out.println("</div>"); // END CURRENT LIST

		out.println("</div>"); // END HISTORY

		out.println("<div class='Recommendation'>"); // START RECOMMENDATION

		// START tab buttons
		out.println("<div class='tab'>");
		out.println(
				"<button class=\"tablinks\" onclick=\"openCity(event, 'Compact')\"  id='defaultOpen'>List</button>");
		out.println("<button class=\"tablinks\" onclick=\"openCity(event, 'Card')\" id='card'>Detailed</button>");
		out.println("<h2 style='font-size:30px; text-align:center; display: flex; padding-left: 10px; color:white; margin: 0px; padding-top: 15; padding-bottom: 15' id='recommendationText'>Recommendation</h2>");
		out.println("</div>");
		// END tab buttons

		
		if (recommendedMovies!= null){
			// recommendedMovies= new Movie[2];
			// recommendedMovies[0]= Movie.getMovieByName(movies, "shrek", false);
			// recommendedMovies[1]= Movie.getMovieByName(movies, "shrek 2", false);

			out.println("<div id='Compact' class='tabcontent'>"); // START COMPACT
			out.println("<div style='padding-left: 10px; padding-top: 30px;'>");
			for (int i= 0; i< recommendedMovies.length; i++){
				if (recommendedMovies[i]== null)
					continue;
				out.println("<span onclick='toDetailed(event,"+ (i+ 1)+ ")'><b>"+ (i+ 1)+ ". </b>"+ recommendedMovies[i].getName()+ "</span>");
				out.println("<br><br>");
			}
			out.println("</div>");
			out.println("</div>"); // END COMPACT

			out.println("<div id='Card' class='tabcontent'>"); // START CARD

			out.println("<div class=\"slideshow-container\">"); // START SLIDE SHOW

			for (int i= 0; i< recommendedMovies.length; i++){
				if (recommendedMovies[i]== null)
					continue;
				Movie m1= recommendedMovies[i];
				String[] dp= Crawler.getDescAndPoster(m1);
				String desc= dp[0];
				String poster= dp[1];
				out.println("<div class=\"mySlides fade\">");

				out.println("<div class='movieDetailedName'>");
				out.println("<b style='font-size: 30px; padding-left: 10px; line-height: 100%; -webkit-box-decoration-break: clone;'>"+ m1.getName()+ "</b><br>");
				out.println("</div>");
				
				out.println("<div class='recommendedCard'>");
				out.println("<img style='display: block; margin-left: auto; margin-right: auto;' src='"+ poster+ "'>");
				out.println("</div>");

				out.println("<p style='padding-right: 10px; padding-left: 10px; height: 90px; padding-top: 10px; text-align: justify;'>"+ desc+ "</p><br>");

				out.println("<table style='padding-left: 5px;'>"); // START TABLE
				out.println("<tr><td><b>Rating  :</b></td><td>"+ recommendedMovies[i].getAverageRating()+ "</td></tr>");
				out.println("<tr><td><b>Votes   :</b></td><td>"+ recommendedMovies[i].getNumberOfVotes()+ "</td></tr>");
				out.println("<tr><td><b>Year    :</b></td><td>"+ recommendedMovies[i].getYearOfRelease()+ "</td></tr>");
				out.println("<tr><td><b>Length  :</b></td><td>"+ recommendedMovies[i].getLength()+ "</td></tr>");
				out.println("<tr><td><b>Region  :</b></td><td>"+ recommendedMovies[i].getLanguage().replace("null", "Not US")+ "</td></tr>");
				out.println("<tr><td><b>Genres  :</b></td><td>");
				for (int j= 0; j< recommendedMovies[i].getGenres().size(); j++) {
					Genre genre= recommendedMovies[i].getGenres().get(j);
					out.println(genre.toString().substring(0,1)+ genre.toString().toLowerCase().substring(1));
					if(j+1!= recommendedMovies[i].getGenres().size())	out.println(", ");
				}
				out.println("</td></tr>");
				out.println("</table>"); // END TABLE

				out.println("</div>");
			}

			out.println("</div>"); // END SLIDE SHOW

			out.println("<div style=\"text-align:center; position: relative; padding-top: 10px\">"); // START DOTS

			out.println("<a class=\"prev\" onclick=\"plusSlides(-1)\">&#10094;</a>");
			for (int i= 0; i< recommendedMovies.length; i++) {
				if (recommendedMovies[i]== null)
					continue;
				out.println("<span class=\"dot\" onclick=\"currentSlide("+ (i+ 1)+ ")\"></span>");
			}
			out.println("<a class=\"next\" onclick=\"plusSlides(1)\">&#10095;</a>");

			out.println(); // END DOTS

			out.println("</div>"); // END CARD
		}
		else{
//			recommendedMovies= new Movie[2];
//			recommendedMovies[0]= Movie.getMovieByName(movies, "shrek", false);
//			recommendedMovies[1]= Movie.getMovieByName(movies, "Medically Yourrs", false);

			out.println("<div id='Compact' class='tabcontent'>"); // START COMPACT
//			out.println("<div style='padding-left: 10px; padding-top: 30px;'>");
//			for (int i= 0; i< recommendedMovies.length; i++){
//				if (recommendedMovies[i]== null)
//					continue;
//				out.println("<span  onclick='toDetailed(event,"+ (i+ 1)+ ")'><b>"+ (i+ 1)+ ". </b>"+ recommendedMovies[i].getName()+ "</span>");
//				out.println("<br><br>");
//			}
//			out.println("</div>");
			out.println("</div>"); // END COMPACT

			out.println("<div id='Card' class='tabcontent'>"); // START CARD

//			out.println("<div class=\"slideshow-container\">"); // START SLIDE SHOW
//
//			for (int i= 0; i< recommendedMovies.length; i++){
//				if (recommendedMovies[i]== null)
//					continue;
//				Movie m1= recommendedMovies[i];
//				String[] dp= Crawler.getDescAndPoster(m1);
//				String desc= dp[0];
//				String poster= dp[1];
//				out.println("<div class=\"mySlides fade\">");
//				
//				out.println("<div class='movieDetailedName'>");
//				out.println("<b style='font-size: 30px; padding-left: 10px; line-height: 100%; -webkit-box-decoration-break: clone;'>"+ m1.getName()+ "</b><br>");
//				out.println("</div>");
//				
//				out.println("<div class='recommendedCard'>");
//				out.println("<img style='display: block; margin-left: auto; margin-right: auto;' src='"+ poster+ "'>");
//				out.println("</div>");
//
//				out.println("<p style='padding-right: 10px; padding-left: 10px; height: 90px; padding-top: 10px; text-align: justify;'>"+ desc+ "</p><br>");
//
//				out.println("<table style='padding-left: 5px;'>"); // START TABLE
//				out.println("<tr><td><b>Rating  :</b></td><td>"+ recommendedMovies[i].getAverageRating()+ "</td></tr>");
//				out.println("<tr><td><b>Votes   :</b></td><td>"+ recommendedMovies[i].getNumberOfVotes()+ "</td></tr>");
//				out.println("<tr><td><b>Year    :</b></td><td>"+ recommendedMovies[i].getYearOfRelease()+ "</td></tr>");
//				out.println("<tr><td><b>Length  :</b></td><td>"+ recommendedMovies[i].getLength()+ "</td></tr>");
//				out.println("<tr><td><b>Region  :</b></td><td>"+ recommendedMovies[i].getLanguage().replace("null", "Not US")+ "</td></tr>");
//				out.println("<tr><td><b>Genres  :</b></td><td>");
//				for (int j= 0; j< recommendedMovies[i].getGenres().size(); j++) {
//					Genre genre= recommendedMovies[i].getGenres().get(j);
//					out.println(genre.toString().substring(0,1)+ genre.toString().toLowerCase().substring(1));
//					if(j+1!= recommendedMovies[i].getGenres().size())	out.println(", ");
//				}
//				out.println("</td></tr>");
//				out.println("</table>"); // END TABLE
//
//				out.println("</div>");
//			}
//
//			out.println("</div>"); // END SLIDE SHOW
//
//			out.println("<div style=\"text-align:center; position: relative; padding-top: 10px\">"); // START DOTS
//
//			out.println("<a class=\"prev\" onclick=\"plusSlides(-1)\">&#10094;</a>");
//			for (int i= 0; i< recommendedMovies.length; i++)
//				out.println("<span class=\"dot\" onclick=\"currentSlide("+ (i+ 1)+ ")\"></span>");
//			out.println("<a class=\"next\" onclick=\"plusSlides(1)\">&#10095;</a>");
//
//			out.println("</div>"); // END DOTS

			out.println("</div>"); // END CARD
		}
		out.println("</div>"); // END RECOMMENDATION

		out.println("</div>"); // END CONTENT

		out.println("</div>"); // END GRID CONTRAINER
		out.println("</body>");

		out.println("<script>"); // START SCRIPT
		out.println("document.getElementById(\"defaultOpen\").click();");

		out.println("var slideIndex = 1;\r\n"+ "showSlides(slideIndex);\r\n"+ "\r\n"+ "// Next/previous controls\r\n"
				+ "function plusSlides(n) {\r\n"+ "  showSlides(slideIndex += n);\r\n"+ "}\r\n"+ "\r\n"
				+ "// Thumbnail image controls\r\n"+ "function currentSlide(n) {\r\n"
				+ "  showSlides(slideIndex = n);\r\n"+ "}\r\n"+ "\r\n"+ "function showSlides(n) {\r\n"+ "  var i;\r\n"
				+ "  var slides = document.getElementsByClassName(\"mySlides\");\r\n"
				+ "  var dots = document.getElementsByClassName(\"dot\");\r\n"
				+ "  if (n > slides.length) {slideIndex = 1}\r\n"+ "  if (n < 1) {slideIndex = slides.length}\r\n"
				+ "  for (i = 0; i < slides.length; i++) {\r\n"+ "      slides[i].style.display = \"none\";\r\n"
				+ "  }\r\n"+ "  for (i = 0; i < dots.length; i++) {\r\n"
				+ "      dots[i].className = dots[i].className.replace(\" active\", \"\");\r\n"+ "  }\r\n"
				+ "  slides[slideIndex-1].style.display = \"block\";\r\n"
				+ "  dots[slideIndex-1].className += \" active\";\r\n"+ "}");
		
		out.println("function toDetailed(evt,slideIndex){");
//		out.println("openCity(evt, 'Card');");
//		out.println("document.getElementById('Compact').style.display = 'none';");
//		out.println("document.getElementById('Card').style.display = \"block\";");
//		out.println("document.getElementById('card').className += ' active';");
//		out.println("document.getElementById('defaultOpen').className.replace(\" active\", \"\");");
		out.println("document.getElementById(\"card\").click();");
		out.println("  var i;\r\n" + 
				"  var slides = document.getElementsByClassName(\"mySlides\");\r\n" + 
				"  var dots = document.getElementsByClassName(\"dot\");\r\n" + 
//				"  if (n > slides.length) {slideIndex = 1}\r\n" + 
//				"  if (n < 1) {slideIndex = slides.length}\r\n" + 
				"  for (i = 0; i < slides.length; i++) {\r\n" + 
				"      slides[i].style.display = \"none\";\r\n" + 
				"  }\r\n" + 
				"  for (i = 0; i < dots.length; i++) {\r\n" + 
				"      dots[i].className = dots[i].className.replace(\" active\", \"\");\r\n" + 
				"  }\r\n" + 
				"  slides[slideIndex-1].style.display = \"block\";\r\n" + 
				"  dots[slideIndex-1].className += \" active\";");
		out.println("console.log(slideIndex);");
		out.println("console.log(slides.length)");
		out.println("}");
		out.println("</script>"); // END SCRIPT
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException{

		if (request.getParameter("like_movie")!= null){
			lastWatchedMovie= Movie.getMovieByName(movies, request.getParameter("entered_movie"), false);
			if(lastWatchedMovie!= null) {
				lastWatchedMovie.setRating(1);
				recommendedMovies= Engine.getRecommendation(movies, getLikedMovies(), UV, 9, USOnly);
			}
		}
		if (request.getParameter("dislike_movie")!= null){
			lastWatchedMovie= Movie.getMovieByName(movies, request.getParameter("entered_movie"), false);
			if(lastWatchedMovie!= null) {
				lastWatchedMovie.setRating(-1);
				recommendedMovies= Engine.getRecommendation(movies, getLikedMovies(), UV, 9, USOnly);
			}
		}

		if (request.getParameter("remove_movie")!= null){
			Movie toBeRemovedMovie= Movie.getMovieByName(movies, request.getParameter("entered_movie"), false);
			if(toBeRemovedMovie!= null) {
				toBeRemovedMovie.setRating(99);
				if (getLikedMovies().size()!= 0){
					if(lastWatchedMovie== toBeRemovedMovie)	lastWatchedMovie= null;
					recommendedMovies= Engine.getRecommendation(movies, getLikedMovies(), UV, 9, USOnly);
				} else{
					lastWatchedMovie= null;
					recommendedMovies= null;
				}
			}
		}

		if (request.getParameter("usOnly")== null){
			if (USOnly!= false){
				USOnly= false;
				if (getLikedMovies().size()!= 0){
					recommendedMovies= Engine.getRecommendation(movies, getLikedMovies(), UV, 9, USOnly);
//					lastWatchedMovie= null;
				}
			}
		} else{
			if (USOnly!= true){
				USOnly= true;
				if (getLikedMovies().size()!= 0){
					recommendedMovies= Engine.getRecommendation(movies, getLikedMovies(), UV, 9, USOnly);
//					lastWatchedMovie= null;
				}
			}
		}
		System.out.println("USONLY IS "+ USOnly);

		if (request.getParameter("reset")!= null){
			reset();
		}

		doGet(request, response);
	}

	private List<Movie> getLikedMovies(){
		if (this.movies== null)
			return new ArrayList<>();
		List<Movie> likedMovies= new ArrayList<>();

		for (Movie movie : this.movies){
			if (movie.getRating()!= 99)
				likedMovies.add(movie);
		}

		return likedMovies;
	}

	private void reset(){
		for (Movie movie : this.movies)
			movie.setRating(99);
		lastWatchedMovie= null;
		recommendedMovies= null;
	}

}
