

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import entity.Genre;
import entity.Movie;
import utilities.JsonConverter;

/**
 * Servlet implementation class MovieImagesShowcase
 */
@WebServlet("/MovieImagesShowcase")
public class MovieImagesShowcase extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		List<Genre> genres= JsonConverter.readGenres();	//	Reads the genres from json
		List<Movie> movies= JsonConverter.readMovies(genres);	//	Reads the movies from json

		PrintWriter out= response.getWriter();
		response.setContentType("text/html");
		
//		Movie movie= Movie.getMovieByName(movies, "Thor ragnarok", true);
//		getPageHtml("https://www.imdb.com/title/"+ movie.getTconst()+ "/", movie, out);
		
		//Sort movies by views
		for(int i= 0; i< movies.size(); i++) {
			for(int j= i+ 1; j< movies.size(); j++) {
				if(movies.get(i).getNumberOfVotes()< movies.get(j).getNumberOfVotes()) {
					Movie m1= movies.get(i);
					movies.set(i, movies.get(j));
					movies.set(j, m1);
				}
			}
		}
		
		try {
			int n= 0;
			for(Movie movie: movies) {
				if(n++== 300)	break;
				System.out.println("Started "+ movie.getName()+ "-"+ movie.getNumberOfVotes());
				Document doc= Jsoup.connect("https://www.imdb.com/title/"+ movie.getTconst()+ "/").get();
				
				
				Elements imgs= doc.select("img");
				for(Element img: imgs) {
//					System.out.println(img.attr("title")+ "- For movie "+ movie.getName());
					if(img.attr("title").contains("Poster")) {
//					if(img.attr("title").contains(movie.getName())&& img.attr("title").contains("Poster")) {
						out.println("<img src='"+ img.absUrl("src")+ "'><br>");
						break;
					}
				}
				
				Elements summary= doc.getElementsByClass("summary_text");
				out.println(movie.getName()+ "<br>");
				out.println(summary.text()+ "<br>");
				out.println("-----------------------------------------------------<br>");
			}
			System.out.println("We here");
			
		}catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		
	}

	private void getPageHtml(String url, Movie movie, PrintWriter out) {
		
	}
}
