package comp1206.sushi.client;


import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import comp1206.sushi.common.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class  Client extends Listener implements ClientInterface {

    private static final Logger logger = LogManager.getLogger("Client");

	public Restaurant restaurant;
	public List<Dish> dishes = new CopyOnWriteArrayList<>();
	public List<Ingredient> ingredients = new ArrayList<Ingredient>();
	public List<Order> orders = new CopyOnWriteArrayList<>();
//	public ArrayList<User> users = new ArrayList<User>();
	public List<Postcode> postcodes = new ArrayList<Postcode>();
	private List<UpdateListener> listeners = new ArrayList<UpdateListener>();
	private com.esotericsoftware.kryonet.Client client;
	private User loggedInUser;


	
	public Client() {
        logger.info("Starting up client...");
        loggedInUser = null;




        //creation of comms client
		try {
		    synchronized (this) {
                client = new com.esotericsoftware.kryonet.Client(32768,32768);
                client.start();
                client.connect(5000, "127.0.0.1", 54555, 54777);
            }
		}catch (IOException e ){
			System.out.println("Something wrong the client comms");
		}

		Kryo kryo = client.getKryo();
		kryo.register(Comms.class);
		kryo.register(Dish.class);
        kryo.register(java.util.HashMap.class);
        kryo.register(java.util.ArrayList.class);
        kryo.register(Ingredient.class);
        kryo.register(Supplier.class);
        kryo.register(Postcode.class);
        kryo.register(Restaurant.class);
        kryo.register(Order.class);
        kryo.register(java.util.concurrent.CopyOnWriteArrayList.class);
        kryo.register(User.class);

        String string = "getPostcodes";
        client.sendTCP(string);
        synchronized (this) {
            client.addListener(this);
        }
        System.out.println(client.getRemoteAddressTCP());

	}
    public void connected(Connection connection){
        System.out.println("The connection is complete");
    }
    public void disconnected(Connection connection){
        System.out.println("Disconnected");
    }
    public void received (Connection connection, Object object) {

            if (object instanceof Dish) {
                Dish dishToAdd = (Dish) object;
                boolean removed = false;

                for (Dish dish: dishes) {
                    if (dish.getName().equals(dishToAdd.getName()) && dish.getDescription().equals(dishToAdd.getDescription())) {
                        this.removeDish(dish);
                        System.out.println("Removed Dish");
                        removed = true;
                    }

                }

                if (!removed) {
                    this.addDish(dishToAdd);
                    System.out.println("Added dish");
                }




            } else if (object instanceof User) {
                System.out.println("I receive the User");
                loggedInUser = (User) object;
            } else if (object instanceof String) {
                String string = (String) object;
                System.out.println(object);

            } else if (object instanceof Order) {
            	boolean removed = false;


                Order order = (Order) object;
                User user = loggedInUser;

                if(loggedInUser.getName().equals(order.getUser().getName())){

                	for (Order cursor : loggedInUser.getOrders()){
                		if (cursor.getName().equals(order.getName()) && cursor.getUser().getName().equals(order.getUser().getName())){

                			user.getOrders().remove(cursor);
                			removed = true;
							System.out.println("Removed order");
                			this.notifyUpdate();
						}
					}

                	if (!removed) {
						System.out.println(order.getUser().getName());
						user.getOrders().add(order);
						this.notifyUpdate();
						System.out.println("Added Order");
						System.out.println(order.getUser());
						for (Map.Entry<Dish, Number> cursor : order.getDishes().entrySet()) {
							System.out.println(cursor.getKey() + " " + cursor.getValue());
						}
					}
				}

            }else if (object instanceof  Restaurant){
            	Restaurant newRestaurant = (Restaurant) object;
            	restaurant = newRestaurant;
			}else if(object instanceof  Postcode){
                Postcode postcode = (Postcode) object;
                this.addPostcode(postcode);
            }

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
        Comms registerRequest = new Comms(newUser);
        registerRequest.setInitClientRequest(true);
        client.sendTCP(registerRequest);


	    return newUser;
	}

	@Override
	public User login(String username, String password) {
	    Postcode uselessPostcode = new Postcode("SO17 1BX", restaurant);
	    User tempLoginUser = new User(username, password, "useless", uselessPostcode);
	    Comms loginRequest = new Comms(tempLoginUser);
	    loginRequest.setLoginRequest(true);
        System.out.println(loginRequest.isLoginRequest());
        System.out.println(loginRequest.getUser().getName());
        System.out.println(loginRequest);
//	    client.sendTCP(loginRequest);
        String test = "login test";
	    client.sendTCP(loginRequest);
//        for (User user: users
//             ) {
//            if (username.equals(user.getName())){
//                return user;
//            }
//        }
        try{Thread.sleep(1000);}
        catch (InterruptedException e){

        }

		return loggedInUser;
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
	    Map<Dish, Number> basket = user.getBasket();
		Order order = new Order(user);
		order.setDishes(basket);
		user.getOrders().add(order);
		StringBuilder orderString = new StringBuilder("ORDER:");
		orderString.append(user.getName()+ ":");
		for(Map.Entry<Dish, Number> cursor : basket.entrySet()){
		    orderString.append(cursor.getValue()+" * "+cursor.getKey()+ ",");
        }
        System.out.println(orderString.toString());

		Comms orderRequest = new Comms(orderString.toString());
		orderRequest.setOrderRequest(true);

		client.sendTCP(orderRequest);




//		client.sendTCP(orderRequest);
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
        User user = loggedInUser;
        user.getOrders().remove(order);
        client.sendTCP(order);
        this.notifyUpdate();
	}

	@Override
	public void addUpdateListener(UpdateListener listener) {
        this.listeners.add(listener);
	}

	@Override
	public void notifyUpdate() {
		try {
			this.listeners.forEach(listener -> listener.updated(new UpdateEvent()));
		}catch(NullPointerException e){

		}
	}

	public void addDish(Dish dish){
	    this.dishes.add(dish);
	    this.notifyUpdate();

    }

    public void removeDish(Dish dish){
		this.dishes.remove(dish);
		this.notifyUpdate();

	}

	public void addPostcode(Postcode postcode){
	    this.postcodes.add(postcode);
	    this.notifyUpdate();
    }



}
