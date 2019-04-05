package comp1206.sushi.server;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import comp1206.sushi.Configuration;
import comp1206.sushi.SomeRequest;
import comp1206.sushi.SomeResponse;
import comp1206.sushi.common.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server implements ServerInterface {

    private static final Logger logger = LogManager.getLogger("Server");
	
	public Restaurant restaurant;
	public ArrayList<Dish> dishes = new ArrayList<Dish>();
	public ArrayList<Drone> drones = new ArrayList<Drone>();
	public ArrayList<Ingredient> ingredients = new ArrayList<Ingredient>();
	public ArrayList<Order> orders = new ArrayList<Order>();
	public ArrayList<Staff> staff = new ArrayList<Staff>();
	public ArrayList<Supplier> suppliers = new ArrayList<Supplier>();
	public ArrayList<User> users = new ArrayList<User>();
	public ArrayList<Postcode> postcodes = new ArrayList<Postcode>();
	private ArrayList<UpdateListener> listeners = new ArrayList<UpdateListener>();
	private Map<Ingredient, Number> ingredientStock = new ConcurrentHashMap<>();
	private Map<Dish, Number> dishStock = new ConcurrentHashMap<>();
	private List<Dish> dishBeingMade = new CopyOnWriteArrayList<>();
	private com.esotericsoftware.kryonet.Server server;



	public Server() {
        logger.info("Starting up server...");
        loadConfiguration("Configuration.txt");

        //creation of the comms server
		try {
			server = new com.esotericsoftware.kryonet.Server();
			synchronized (this){server.start();}
			server.bind(54555, 54777);
		}catch (IOException e){
			System.out.println("Something wrong with the server comms");
		}
        Kryo kryo = server.getKryo();
        kryo.register(SomeRequest.class);
        kryo.register(SomeResponse.class);
        kryo.register(Dish.class);
        kryo.register(java.util.HashMap.class);
        kryo.register(java.util.ArrayList.class);
        kryo.register(Ingredient.class);
        kryo.register(Supplier.class);
        kryo.register(Postcode.class);
        kryo.register(Restaurant.class);
        server.addListener(new Listener() {
            public void received (Connection connection, Object object) {
                if (object instanceof SomeRequest) {
                    SomeRequest request = (SomeRequest)object;
                    System.out.println(request.text);

                    SomeResponse response = new SomeResponse();
                    response.text = "Thanks";
                    Dish dishToSend = dishes.get(1);
                    connection.sendTCP(dishToSend);

                }
            }
        });

//		Postcode restaurantPostcode = new Postcode("SO17 1BJ");
//		restaurant = new Restaurant("Southampton Sushi",restaurantPostcode);
//
////
////
////
////
////		Postcode postcode1 = addPostcode("SO17 1TJ");
////		Postcode postcode2 = addPostcode("SO17 1BX");
////		Postcode postcode3 = addPostcode("SO17 2NJ");
////		Postcode postcode4 = addPostcode("SO17 1TW");
////		Postcode postcode5 = addPostcode("SO17 2LB");
////
////		Supplier supplier1 = addSupplier("Supplier 1",postcode1);
////		Supplier supplier2 = addSupplier("Supplier 2",postcode2);
////		Supplier supplier3 = addSupplier("Supplier 3",postcode3);
////
////		Ingredient ingredient1 = addIngredient("Ingredient 1","grams",supplier1,1,5,1);
////		Ingredient ingredient2 = addIngredient("Ingredient 2","grams",supplier2,1,5,1);
////		Ingredient ingredient3 = addIngredient("Ingredient 3","grams",supplier3,1,5,1);
////
//		Dish dish1 = addDish("Dish 1","Dish 1",1,1,10);
////		Dish dish2 = addDish("Dish 2","Dish 2",2,1,10);
////		Dish dish3 = addDish("Dish 3","Dish 3",3,1,10);
////
//////		orders.add(new Order());
////
////		addIngredientToDish(dish1,ingredient1,1);
////		addIngredientToDish(dish1,ingredient2,2);
////		addIngredientToDish(dish2,ingredient2,3);
////		addIngredientToDish(dish2,ingredient3,1);
////		addIngredientToDish(dish3,ingredient1,2);
////		addIngredientToDish(dish3,ingredient3,1);
////
////		addStaff("Staff 1");
////		addStaff("Staff 2");
////		addStaff("Staff 3");
////
////		addDrone(1);
////		addDrone(2);
////		addDrone(3);
////
//        Postcode newPostcode = new Postcode("SO17 1BX", restaurant);
//        User user = new User("User", "Password", "Lol", newPostcode);
//        Order order = new Order(user);
//        orders.add(order);
//        addDishtoOrder(order,dish1,3);

	}

	@Override
	public List<Dish> getDishes() {
		return this.dishes;
	}

	@Override
	public Dish addDish(String name, String description, Number price, Number restockThreshold, Number restockAmount) {
		Dish newDish = new Dish(name,description,price,restockThreshold,restockAmount);
		this.dishes.add(newDish);
		this.setStock(newDish, 0);
		this.notifyUpdate();
		return newDish;
	}
	
	@Override
	public void removeDish(Dish dish) {
		this.dishes.remove(dish);
		this.notifyUpdate();
	}

	@Override
	public Map<Dish, Number> getDishStockLevels() {
//		Random random = new Random();
//		List<Dish> dishes = getDishes();
//		HashMap<Dish, Number> levels = new HashMap<Dish, Number>();
//		for(Dish dish : dishes) {
//			levels.put(dish,random.nextInt(50));
//		}
//		return levels;
		return dishStock;
	}
	
	@Override
	public void setRestockingIngredientsEnabled(boolean enabled) {
		
	}

	@Override
	public void setRestockingDishesEnabled(boolean enabled) {
		
	}
	
	@Override
	public void setStock(Dish dish, Number stock) {
		dishStock.put(dish, stock);
	}

	@Override
	public void setStock(Ingredient ingredient, Number stock) {
		ingredientStock.put(ingredient,stock);
	}

	@Override
	public List<Ingredient> getIngredients() {
		return this.ingredients;
	}

	@Override
	public Ingredient addIngredient(String name, String unit, Supplier supplier,
			Number restockThreshold, Number restockAmount, Number weight) {
		Ingredient mockIngredient = new Ingredient(name,unit,supplier,restockThreshold,restockAmount,weight);
		this.ingredients.add(mockIngredient);
		this.setStock(mockIngredient, 0);
		this.notifyUpdate();
		return mockIngredient;
	}

	@Override
	public void removeIngredient(Ingredient ingredient) {
		int index = this.ingredients.indexOf(ingredient);
		this.ingredients.remove(index);
		this.notifyUpdate();
	}

	@Override
	public List<Supplier> getSuppliers() {
		return this.suppliers;
	}

	@Override
	public Supplier addSupplier(String name, Postcode postcode) {
		Supplier mock = new Supplier(name,postcode);
		this.suppliers.add(mock);
		return mock;
	}


	@Override
	public void removeSupplier(Supplier supplier) {
		int index = this.suppliers.indexOf(supplier);
		this.suppliers.remove(index);
		this.notifyUpdate();
	}

	@Override
	public List<Drone> getDrones() {
		return this.drones;
	}

	@Override
	public Drone addDrone(Number speed) {
		Drone mock = new Drone(speed);
		this.drones.add(mock);
		return mock;
	}

	@Override
	public void removeDrone(Drone drone) {
		int index = this.drones.indexOf(drone);
		this.drones.remove(index);
		this.notifyUpdate();
	}

	@Override
	public List<Staff> getStaff() {
		return this.staff;
	}

	@Override
	public Staff addStaff(String name) {
		Staff mock = new Staff(name, this);
		this.staff.add(mock);
		Thread staffThread = new Thread(mock);
		staffThread.start();
		return mock;
	}

	@Override
	public void removeStaff(Staff staff) {
		this.staff.remove(staff);
		this.notifyUpdate();
	}

	@Override
	public List<Order> getOrders() {
		return this.orders;
	}

	@Override
	public void removeOrder(Order order) {
		int index = this.orders.indexOf(order);
		this.orders.remove(index);
		this.notifyUpdate();
	}
	
	@Override
	public Number getOrderCost(Order order) {
	    double cost = 0;
		Map<Dish, Number> dishes = order.getDishes();
        for (Entry<Dish, Number> cursor: dishes.entrySet()
             ) {
            cost += (cursor.getKey().getPrice().doubleValue()) * (cursor.getValue().doubleValue());
        }
        return  cost;
	}

	@Override
	public Map<Ingredient, Number> getIngredientStockLevels() {
//		Random random = new Random();
//		List<Ingredient> dishes = getIngredients();
//		HashMap<Ingredient, Number> levels = new HashMap<Ingredient, Number>();
//		for(Ingredient ingredient : ingredients) {
//			levels.put(ingredient,random.nextInt(50));
//		}
//		return levels;
		return ingredientStock;
	}

	@Override
	public Number getSupplierDistance(Supplier supplier) {
		return supplier.getDistance();
	}

	@Override
	public Number getDroneSpeed(Drone drone) {
		return drone.getSpeed();
	}

	@Override
	public Number getOrderDistance(Order order) {
		Order mock = (Order)order;
		return mock.getDistance();
	}

	@Override
	public void addIngredientToDish(Dish dish, Ingredient ingredient, Number quantity) {
		if(quantity == Integer.valueOf(0)) {
			removeIngredientFromDish(dish,ingredient);
		} else {
			dish.getRecipe().put(ingredient,quantity);
		}
	}

	@Override
	public void removeIngredientFromDish(Dish dish, Ingredient ingredient) {
		dish.getRecipe().remove(ingredient);
		this.notifyUpdate();
	}

	public void addDishtoOrder(Order order, Dish dish, Number quantity){
	    order.getDishes().put(dish,quantity);
    }

	@Override
	public Map<Ingredient, Number> getRecipe(Dish dish) {
		return dish.getRecipe();
	}

	@Override
	public List<Postcode> getPostcodes() {
		return this.postcodes;
	}

	@Override
	public Postcode addPostcode(String code) {
		Postcode mock = new Postcode(code, restaurant);
		this.postcodes.add(mock);
		this.notifyUpdate();
		return mock;
	}

	@Override
	public void removePostcode(Postcode postcode) throws UnableToDeleteException {
		this.postcodes.remove(postcode);
		this.notifyUpdate();
	}

	@Override
	public List<User> getUsers() {
		return this.users;
	}
	
	@Override
	public void removeUser(User user) {
		this.users.remove(user);
		this.notifyUpdate();
	}

	@Override
	public void loadConfiguration(String filename) {
	    System.out.println("Loaded configuration: " + filename);
//        Server newServer = new Server();
        dishes.clear();
        drones.clear();
        ingredients.clear();
        orders.clear();
        users.clear();
        staff.clear();
        ingredientStock.clear();
        dishStock.clear();
        suppliers.clear();
        postcodes.clear();
        Configuration configuration = new Configuration(filename, this);
//        SwingUtilities.invokeLater(()-> new ServerWindow(newServer));

	}

	@Override
	public void setRecipe(Dish dish, Map<Ingredient, Number> recipe) {
		for(Entry<Ingredient, Number> recipeItem : recipe.entrySet()) {
			addIngredientToDish(dish,recipeItem.getKey(),recipeItem.getValue());
		}
		this.notifyUpdate();
	}

	@Override
	public boolean isOrderComplete(Order order) {
		return true;
	}

	@Override
	public String getOrderStatus(Order order) {
		Random rand = new Random();
		if(rand.nextBoolean()) {
			return "Complete";
		} else {
			return "Pending";
		}
	}
	
	@Override
	public String getDroneStatus(Drone drone) {
		Random rand = new Random();
		if(rand.nextBoolean()) {
			return "Idle";
		} else {
			return "Flying";
		}
	}
	
	@Override
	public String getStaffStatus(Staff staff) {
		Random rand = new Random();
		if(rand.nextBoolean()) {
			return "Idle";
		} else {
			return "Working";
		}
	}

	@Override
	public void setRestockLevels(Dish dish, Number restockThreshold, Number restockAmount) {
		dish.setRestockThreshold(restockThreshold);
		dish.setRestockAmount(restockAmount);
		this.notifyUpdate();
	}

	@Override
	public void setRestockLevels(Ingredient ingredient, Number restockThreshold, Number restockAmount) {
		ingredient.setRestockThreshold(restockThreshold);
		ingredient.setRestockAmount(restockAmount);
		this.notifyUpdate();
	}

	public Order addOrder(User user){
	    Order order = new Order(user);
	    orders.add(order);
	    return order;
    }

	@Override
	public Number getRestockThreshold(Dish dish) {
		return dish.getRestockThreshold();
	}

	@Override
	public Number getRestockAmount(Dish dish) {
		return dish.getRestockAmount();
	}

	@Override
	public Number getRestockThreshold(Ingredient ingredient) {
		return ingredient.getRestockThreshold();
	}

	@Override
	public Number getRestockAmount(Ingredient ingredient) {
		return ingredient.getRestockAmount();
	}

	@Override
	public void addUpdateListener(UpdateListener listener) {
		this.listeners.add(listener);
	}
	
	@Override
	public void notifyUpdate() {
		this.listeners.forEach(listener -> listener.updated(new UpdateEvent()));
	}

	@Override
	public Postcode getDroneSource(Drone drone) {
		return drone.getSource();
	}

	@Override
	public Postcode getDroneDestination(Drone drone) {
		return drone.getDestination();
	}

	@Override
	public Number getDroneProgress(Drone drone) {
		return drone.getProgress();
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
	public Restaurant getRestaurant() {
		return restaurant;
	}

	public void addDishBeingMade(Dish dish){
	    this.dishBeingMade.add(dish);
	    this.notifyUpdate();
    }

    public void removeDishBeingMade(Dish dish){
	    this.dishBeingMade.remove(dish);
	    this.notifyUpdate();
    }

    public boolean isBeingMade(Dish dish){
	    return this.dishBeingMade.contains(dish);
    }

    public List<Dish> getDishBeingMade() {
        return dishBeingMade;
    }
}
