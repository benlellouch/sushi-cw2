package comp1206.sushi.common;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Postcode extends Model {

	private String name;
	private Map<String,Double> latLong;
	private Number distance;

	public Postcode(String code) {
		this.name = code;
		calculateLatLong();
		this.distance = Integer.valueOf(0);
	}
	
	public Postcode(String code, Restaurant restaurant) {
		this.name = code;
		calculateLatLong();
		calculateDistance(restaurant);
	}
	
	@Override
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public Number getDistance() {
		return this.distance;
	}

	public Map<String,Double> getLatLong() {
		return this.latLong;
	}
	
	protected void calculateDistance(Restaurant restaurant) {
        Double restaurantLat = null;
        Double restaurantLong = null ;
        Double postcodeLat = null;
        Double postcodeLong = null;
        Postcode destination = restaurant.getLocation();
        for (Map.Entry<String, Double> cursor: destination.getLatLong().entrySet()
        ) {
            if(cursor.getKey().equals("lat")){
                restaurantLat = cursor.getValue();
            } else {
                restaurantLong = cursor.getValue();
            }

        }
        for (Map.Entry<String, Double> cursor: this.getLatLong().entrySet()
        ) {
            if(cursor.getKey().equals("lat")){
                postcodeLat= cursor.getValue();
            } else {
                postcodeLong = cursor.getValue();
            }

        }
        int earthRadius = 6371;
        double dLat = (restaurantLat-postcodeLat) * (Math.PI/180);
        double dLon = (restaurantLong-postcodeLong) * (Math.PI/180);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(postcodeLat*(Math.PI/180)) * Math.cos(restaurantLat*(Math.PI/180)) *
                        Math.sin(dLon/2) * Math.sin(dLon/2)
                ;

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        Number d = earthRadius * c;
        this.distance = d;
	}
	
	protected void calculateLatLong() {
		try {
			String formattedPostCode = this.name.replace(" ", "%20");
			String url = "https://www.southampton.ac.uk/~ob1a12/postcode/postcode.php?postcode=" + formattedPostCode;
			URL link = new URL(url);
			URLConnection urlConnection = link.openConnection();
			InputStream inputStream = urlConnection.getInputStream();
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
			String string = bufferedReader.readLine();
			int indexstart = string.indexOf("\"lat\":\"");
			int indexend= string.indexOf("\",\"lo");
			Double lat = Double.valueOf(string.substring(indexstart+7,indexend));
			indexstart = string.indexOf("\"long\":\"");
			indexend = string.indexOf("\"}");
			Double longitude = Double.valueOf(string.substring(indexstart+8,indexend));
			this.latLong = new HashMap<String,Double>();
			latLong.put("lat", lat);
			latLong.put("lon", longitude);

		}catch (MalformedURLException e){
			System.out.println("I have no idea what to do");
		} catch (IOException e){
			System.out.println("In postcode.java");
		} catch (StringIndexOutOfBoundsException e){
            System.out.println("Something is going wrong with the calculation of distance");
		}
	}
	
}
