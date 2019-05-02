package comp1206.sushi.common;


import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class User extends Model implements Serializable {
	
	private String name;
	private String password;
	private String address;
	private Postcode postcode;
	private Map<Dish, Number> basket;
	private List<Order> orders;

	public User(){}

	public User(String username, String password, String address, Postcode postcode) {
		this.name = username;
		this.password = password;
		this.address = address;
		this.postcode = postcode;
		this.basket = new HashMap<>();
		this.orders = new CopyOnWriteArrayList<>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Number getDistance() {
		return postcode.getDistance();
	}

	public Postcode getPostcode() {
		return this.postcode;
	}
	
	public void setPostcode(Postcode postcode) {
		this.postcode = postcode;
	}

    public Map<Dish, Number> getBasket() {
        return basket;
    }

    public void setBasket(Map<Dish, Number> basket) {
        this.basket = basket;
    }

	public List<Order> getOrders() {
		return orders;
	}

	public void setOrders(List<Order> orders) {
		this.orders = orders;
	}

	public String getPassword(){
		return password;
	}
}
