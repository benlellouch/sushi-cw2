package comp1206.sushi.server;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import comp1206.sushi.Configuration;
import comp1206.sushi.DataPersistence;
import comp1206.sushi.common.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server implements ServerInterface {

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
	private Comms server;
	private List<Dish> dishBeingMade = new CopyOnWriteArrayList<>();
	private Stock stock;





	public Server() {
		server = new Comms(this);
        logger.info("Starting up server...");
        Postcode postcode = new Postcode("SO17 1BX");
        restaurant = new Restaurant("Southampton Sushi", postcode);
        stock = new Stock();
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
		server.sendDish(newDish);
		return newDish;
	}
	
	@Override
	public void removeDish(Dish dish) {
		this.dishes.remove(dish);
		server.sendDish(dish);
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
		return stock.getDishStock();
	}
	
	@Override
	public void setRestockingIngredientsEnabled(boolean enabled) {
		
	}

	@Override
	public void setRestockingDishesEnabled(boolean enabled) {
		
	}
	
	@Override
	public void setStock(Dish dish, Number stock) {
		this.stock.getDishStock().put(dish,stock);
	}

	@Override
	public void setStock(Ingredient ingredient, Number stock) {
		this.stock.getIngredientStock().put(ingredient,stock);
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
		this.notifyUpdate();
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
		this.notifyUpdate();
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
		server.sendOrder(order);
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
		return stock.getIngredientStock();
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
	    this.notifyUpdate();
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
        stock.getIngredientStock().clear();
        stock.getDishStock().clear();
        suppliers.clear();
        postcodes.clear();
        dishBeingMade.clear();
        if(filename.contains(".txt")) {
			Configuration configuration = new Configuration(filename, this);
//			try {
//				File inFile = new File("SerialOutput.txt");
//				FileInputStream fis = new FileInputStream(inFile);
//				ObjectInputStream ois = new ObjectInputStream(fis);
//				this.postcodes = (CopyOnWriteArrayList<Postcode>) ois.readObject();
//				this.restaurant = (Restaurant) ois.readObject();
//				this.staff = (CopyOnWriteArrayList<Staff>) ois.readObject();
//				System.out.println("I do get here");
//				for (Staff cursor: staff
//				) {
//
//					System.out.println(cursor.getName());
//
//				}
//
//
//
//			}catch (IOException e){
//				e.printStackTrace();
//			} catch (ClassNotFoundException e){
//				e.printStackTrace();
//			}
		}else if(filename.contains(".data")){
			loadBackup();
		}

        this.notifyUpdate();

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
        backup();
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
		return stock.getDishStock();
	}
	public void setDishStock(Dish dish, Number number){
		this.stock.getDishStock().put(dish, number);
		this.notifyUpdate();
	}

	public void updateClientOrderStatus(Order order){


		server.updateOrder(order);

	}

	public void backup() {

		File f = new File("backup.data");
		FileOutputStream fos;
		ObjectOutputStream oos;

		try {
			fos = new FileOutputStream(f);


			oos = new ObjectOutputStream(fos);


			DataPersistence newPersistence = new DataPersistence(this);

			oos.writeObject(newPersistence);


			fos.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

//		System.out.println("Backed up the data.");
	}

	public void loadBackup(){
		File f = new File("backup.data");
		FileInputStream fis;
		ObjectInputStream ois;
		try {
			fis = new FileInputStream(f);
			ois = new ObjectInputStream(fis);
			System.out.println("Persistence found, loading...");

			DataPersistence backup = (DataPersistence) ois.readObject();
			System.out.println("read object");
			this.restaurant = backup.getRestaurant();
			System.out.println("got restaurant");
			this.postcodes = backup.getPostcodes();
			System.out.println("got postcodes");
			this.suppliers = backup.getSuppliers();
			System.out.println("get suppliers");
			this.ingredients = backup.getIngredients();
			System.out.println("got ingredients");
			this.dishes = backup.getDishes();
			System.out.println("got dishes");
			this.users = backup.getUsers();
			System.out.println("got users");
			this.orders = backup.getOrders();
			System.out.println("got orders");
			this.stock.setIngredientStock(backup.getIngredientStock());
			System.out.println("got ingredient stock");
			this.stock.setDishStock(backup.getDishStock());
			System.out.println("got dish stock");

			this.drones = backup.getDrones();
			System.out.println("got drones");
			this.staff = backup.getStaff();
			System.out.println("got Staff");

			for(Drone d : drones) {
				d.setServer(this);
				Thread newWorker = new Thread(d);
				newWorker.start();
			}
			System.out.println("started drones");
			for(Staff s : staff) {
				s.setServer(this);
				Thread newWorker = new Thread(s);
				newWorker.start();
			}
			System.out.println("started staff");
			this.notifyUpdate();
		} catch(Exception e) {
			System.out.println("Persistence error - loading default configuration");
			e.printStackTrace();

		}

	}


}
