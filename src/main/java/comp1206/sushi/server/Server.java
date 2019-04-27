package comp1206.sushi.server;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import comp1206.sushi.Configuration;
import comp1206.sushi.DataPersistence;
import comp1206.sushi.common.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server extends Listener implements ServerInterface {

    private static final Logger logger = LogManager.getLogger("Server");
	
	public Restaurant restaurant;
	public List<Dish> dishes = new CopyOnWriteArrayList<>();
	public List<Drone> drones = new CopyOnWriteArrayList<>();
	public List<Ingredient> ingredients = new CopyOnWriteArrayList<>();
	public List<Order> orders = new CopyOnWriteArrayList<>();
	public List<Staff> staff = new CopyOnWriteArrayList<>();
	public List<Supplier> suppliers = new CopyOnWriteArrayList<>();
	public List<User> users = new CopyOnWriteArrayList<>();
	public List<Postcode> postcodes = new CopyOnWriteArrayList<>();
	private List<UpdateListener> listeners = new CopyOnWriteArrayList<>();
	private Map<Ingredient, Number> ingredientStock = new ConcurrentHashMap<>();
	private Map<Dish, Number> dishStock = new ConcurrentHashMap<>();
	private List<Dish> dishBeingMade = new CopyOnWriteArrayList<>();
	private com.esotericsoftware.kryonet.Server server;




	public Server() {
        logger.info("Starting up server...");
        Postcode postcode = new Postcode("SO17 1BX");
        restaurant = new Restaurant("Southampton Sushi", postcode);
        try {
			DataPersistence dataPersistence = new DataPersistence(this);
			Thread dataPersistenceThread = new Thread(dataPersistence);
			dataPersistenceThread.start();
		}catch (IOException e){
        	e.printStackTrace();

		}
//        this.loadConfiguration("Configuration.txt");
//		Configuration configuration = new Configuration("src/main/java/comp1206/sushi/Configuration.txt", this);



        //creation of the comms server
		try {
			server = new com.esotericsoftware.kryonet.Server();
			synchronized (this){server.start();}
			server.bind(54555, 54777);
		}catch (IOException e){
			e.printStackTrace();
		}
        Kryo kryo = server.getKryo();
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
        kryo.register(Order.OrderStatus.class);
		kryo.register(Ingredient.IngredientStatus.class);
        server.addListener(this);


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
    public void connected(Connection connection){
        System.out.println("The connection is complete");
        String string = "say hello to my little friend";
        connection.sendTCP(string);
        System.out.println("I have sent a string");
        System.out.println(connection.getRemoteAddressTCP());
    }
    public void disconnected(Connection connection){
        System.out.println("Disconnected");
    }
    public void received(Connection connection, Object object) {
        if (object instanceof Comms) {
            System.out.println("I do receive a comms object");
            Comms request =  (Comms) object;
            User user = request.getUser();
            System.out.println(request);
            System.out.println(request.isLoginRequest());
            if (request.isInitClientRequest()) {
                this.addUser(user);
                initialiseClient(connection, user);
            } else if (request.isLoginRequest()) {
                System.out.println("I get a login request with the username:" + user.getName());
                for (User cursor : users) {
                    if (user.getName().equals(cursor.getName()) && user.getPassword().equals(cursor.getPassword())) {
                        connection.sendTCP(cursor);
                        System.out.println("I sent out the user that the client wants: " + cursor.getName());
                        initialiseClient(connection, cursor);
                    }
                }

            }else if (request.isOrderRequest()){
//               Order order = new Order(user);
//               this.addOrder(order);
//               for (Entry<String, Number> cursor : request.getOrderDishes().entrySet()){
//                    for (Dish dish: dishes){
//                        if (cursor.getKey().equals(dish.getName())){
//                            this.addDishtoOrder(order,dish, cursor.getValue());
//                        }
//                    }
//               }
               String orderString = (String) request.getOrderString();
                String[] words = orderString.split(":");

                if (words[0].equals("ORDER")){

                    Order order =null;

                    for (User cursor: this.getUsers()
                    ) {
                        if (words[1].equals(cursor.getName())) {
                            order = this.addOrder(cursor);

                        }

                    }
                    String[] dishes = words[2].split(",");
                    for (String dish: dishes) {
                        String[] amountAndName = dish.split(" \\* ");

                        for(Dish newDish: this.getDishes()) {
//                            System.out.println(amountAndName[1] + " and the newDish: " + newDish.getName() +  " length of array");

                            if (amountAndName[1].equals(newDish.getName())) {
                                try {
                                    this.addDishtoOrder(order,newDish, NumberFormat.getInstance().parse(amountAndName[0]));
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                System.out.println(amountAndName[0]);
                            }
                        }
                    }

                }
            }

        } else if (object instanceof User){

            User user = (User) object;
            System.out.println(user.getName());
        } else if (object instanceof  String){
            String print = (String) object;
            System.out.println(print);
            String send = "And did you receive mine";
            System.out.println(connection.getRemoteAddressTCP());
            connection.sendTCP(send);
            if(print.equals("getPostcodes")){
                connection.sendTCP(restaurant);
                for (Postcode postcode : postcodes){
                    connection.sendTCP(postcode);
                }
            }
        } else if (object instanceof Order){
			System.out.println("I receive an order object");
            Order order = (Order) object;
			System.out.println(order.getName() + " " + order.getUser());
            for (Order cursor :orders){
				System.out.println(cursor.getName() + " " + cursor.getUser());
            	if (cursor.getName().equals(order.getName()) && cursor.getUser().getName().equals(order.getUser().getName())){
            		cancelOrder(cursor);
					System.out.println("Removed order");
				}
			}

        }
    }

	public synchronized void initialiseClient(Connection connection, User user){

        for (Dish dish : dishes) {
            connection.sendTCP(dish);
        }
        for (Order order: orders){
            if (order.getUser().equals(user)){
                connection.sendTCP(order);
                System.out.println("Order sent");
            }
        }
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
		try {
            server.sendToAllTCP(newDish);
        }catch (NullPointerException e){
            System.out.println("It's fine");
        }
		return newDish;
	}
	
	@Override
	public void removeDish(Dish dish) {
		this.dishes.remove(dish);
		server.sendToAllTCP(dish);
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
		Drone mock = new Drone(speed, this);
		this.drones.add(mock);
		Thread droneThread = new Thread(mock);
		droneThread.start();
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

		this.orders.remove(order);
		server.sendToAllTCP(order);
		this.notifyUpdate();
	}

	public void cancelOrder(Order order){
		this.orders.remove(order);
	}
	
	@Override
	public Number getOrderCost(Order order) {
	    double cost = 0;
		Map<Dish, Number> dishes = order.getDishes();

        for (Entry<Dish, Number> cursor: dishes.entrySet()) {
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
		Order mock = order;
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
        dishBeingMade.clear();
        if(filename.contains(".txt")) {
			Configuration configuration = new Configuration(filename, this);
		}else if(filename.contains(".data")){
			System.out.println("I do get here");
        	try {
				File inFile = new File("SerialOutput.data");
				FileInputStream fis = new FileInputStream(inFile);
				ObjectInputStream ois = new ObjectInputStream(fis);
				postcodes = (List<Postcode>) ois.readObject();
				restaurant = (Restaurant) ois.readObject();
				staff = (List<Staff>) ois.readObject();
				System.out.println("I do get here");
				for (Staff cursor: staff
					 ) {

					System.out.println(cursor.getName());

				}



			}catch (IOException e){
        		e.printStackTrace();
			} catch (ClassNotFoundException e){
				e.printStackTrace();
			}

		}

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
		if(order.getOrderStatus() == Order.OrderStatus.COMPLETED){
			return true;
		}
		return false;
	}

	@Override
	public String getOrderStatus(Order order) {

		return order.getStatus();
	}
	
	@Override
	public String getDroneStatus(Drone drone) {
		return drone.getStatus();
	}
	
	@Override
	public String getStaffStatus(Staff staff) {
		return staff.getStatus();
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
	    this.notifyUpdate();
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
	public synchronized void notifyUpdate() {
	    synchronized (this) {
	    	try {
				this.listeners.forEach(listener -> listener.updated(new UpdateEvent()));
			}catch (NullPointerException e){}
        }
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

	public synchronized void  addDishBeingMade(Dish dish){
	    this.dishBeingMade.add(dish);
	    synchronized (this) {
            this.notifyUpdate();
        }
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

    public Order addOrder(Order order){
		this.orders.add(order);
		this.notifyUpdate();
		return order;
	}

	public User addUser(User user){
	    this.users.add(user);
	    this.notifyUpdate();
	    return user;
    }

	public Map<Dish, Number> getDishStock() {
		return dishStock;
	}
	public void setDishStock(Dish dish, Number number){
		this.dishStock.put(dish, number);
		this.notifyUpdate();
	}

	public void updateClientOrderStatus(Order order){

		Comms orderUpdate = new Comms(order.getName(), order.getUser(), order.getOrderStatus());
		orderUpdate.setOrderStatusUpdate(true);
		server.sendToAllTCP(orderUpdate);

	}


}
