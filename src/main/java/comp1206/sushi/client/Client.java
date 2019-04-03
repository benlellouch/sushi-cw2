package comp1206.sushi.client;


import comp1206.sushi.common.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Client implements ClientInterface {

    private static final Logger logger = LogManager.getLogger("Client");

	public Restaurant restaurant;
	public ArrayList<Dish> dishes = new ArrayList<Dish>();
	public ArrayList<Ingredient> ingredients = new ArrayList<Ingredient>();
	public ArrayList<Order> orders = new ArrayList<Order>();
	public ArrayList<User> users = new ArrayList<User>();
	public ArrayList<Postcode> postcodes = new ArrayList<Postcode>();
	private ArrayList<UpdateListener> listeners = new ArrayList<UpdateListener>();


	
	public Client() {
        logger.info("Starting up client...");

        Postcode restaurantPostcode = new Postcode("SO17 1BJ");
        restaurant = new Restaurant("Southampton Sushi",restaurantPostcode);
        dishes.add(new Dish("Test", "Desciprtio",1,2,3));
        postcodes.add(new Postcode("SO17 1BX", restaurant));

	}
	
	@Override
	public Restaurant getRestaurant() {
		return restaurant;
	}
	
	@Override
	public String getRestaurantName() {
		return restaurant.getName();
	}

	@Override
	public Postcode getRestaurantPostcode() {
		return restaurant.getLocation();
	}
	
	@Override
	public User register(String username, String password, String address, Postcode postcode) {
	    User newUser = new User(username,password,address,postcode);
	    users.add(newUser);
	    return newUser;
	}

	@Override
	public User login(String username, String password) {
        for (User user: users
             ) {
            if (username.equals(user.getName())){
                return user;
            }
        }

		return null;
	}

	@Override
	public List<Postcode> getPostcodes() {
		return postcodes;
	}

	@Override
	public List<Dish> getDishes() {
		return dishes;
	}

	@Override
	public String getDishDescription(Dish dish) {
		return dish.getDescription();
	}

	@Override
	public Number getDishPrice(Dish dish) {
		return dish.getPrice();
	}

	@Override
	public Map<Dish, Number> getBasket(User user) {
		return user.getBasket();
	}

	@Override
	public Number getBasketCost(User user) {
        double cost = 0;
        Map<Dish, Number> basket = user.getBasket();
        for (Map.Entry<Dish, Number> cursor: basket.entrySet()
        ) {
            cost += (cursor.getKey().getPrice().doubleValue()) * (cursor.getValue().doubleValue());
        }
        return cost;
	}

	@Override
	public void addDishToBasket(User user, Dish dish, Number quantity) {
        user.getBasket().put(dish, quantity);
	}

	@Override
	public void updateDishInBasket(User user, Dish dish, Number quantity) {
        user.getBasket().put(dish,quantity);
	}

	@Override
	public Order checkoutBasket(User user) {
		Order order = new Order(user);
		order.setDishes(user.getBasket());
		user.getOrders().add(order);
		clearBasket(user);
		return order;
	}

	@Override
	public void clearBasket(User user) {
	    Map<Dish, Number> empty = new HashMap<>();
        user.setBasket(empty);
	}

	@Override
	public List<Order> getOrders(User user) {
		return user.getOrders();
	}

	@Override
	public boolean isOrderComplete(Order order) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getOrderStatus(Order order) {
		// TODO Auto-generated method stub
		return "test";
	}

	@Override
	public Number getOrderCost(Order order) {
		double cost = 0;
		Map<Dish, Number> dishes = order.getDishes();
		for (Map.Entry<Dish, Number> cursor: dishes.entrySet()
		) {
			cost += (cursor.getKey().getPrice().doubleValue()) * (cursor.getValue().doubleValue());
		}
		return  cost;
	}

	@Override
	public void cancelOrder(Order order) {
        User user = order.getUser();
        user.getOrders().remove(order);
	}

	@Override
	public void addUpdateListener(UpdateListener listener) {
        this.listeners.add(listener);
	}

	@Override
	public void notifyUpdate() {
        this.listeners.forEach(listener -> listener.updated(new UpdateEvent()));
	}

}
