package comp1206.sushi.client;


import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import comp1206.sushi.SomeRequest;
import comp1206.sushi.SomeResponse;
import comp1206.sushi.common.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
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
	private com.esotericsoftware.kryonet.Client client;


	
	public Client() {
        logger.info("Starting up client...");

        Postcode restaurantPostcode = new Postcode("SO17 1BJ");
        restaurant = new Restaurant("Southampton Sushi",restaurantPostcode);
        dishes.add(new Dish("Test", "Desciprtio",1,2,3));
        postcodes.add(new Postcode("SO17 1BX", restaurant));

        //creation of comms client
		try {
			client = new com.esotericsoftware.kryonet.Client();
			client.start();
			client.connect(5000, "localhost", 54555, 54777);
		}catch (IOException e ){
			System.out.println("Something wrong the client comms");
		}

		Kryo kryo = client.getKryo();
		kryo.register(SomeRequest.class);
		kryo.register(SomeResponse.class);
		kryo.register(Dish.class);
        kryo.register(java.util.HashMap.class);
        kryo.register(java.util.ArrayList.class);
        kryo.register(Ingredient.class);
        kryo.register(Supplier.class);
        kryo.register(Postcode.class);
        kryo.register(Restaurant.class);
		SomeRequest request = new SomeRequest();
		request.text = "Here is the request";
		client.sendTCP(request);
		client.addListener(new Listener() {
			public void received (Connection connection, Object object) {
				if (object instanceof SomeResponse) {
					SomeResponse response = (SomeResponse)object;
					System.out.println(response.text);
				} else if (object instanceof Dish){
				    Dish dishToAdd = (Dish) object;
				    dishes.add(dishToAdd);
                    System.out.println("Added dish");
                }
			}
		});

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
