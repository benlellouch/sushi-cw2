package comp1206.sushi.common;

import comp1206.sushi.common.Drone;
import comp1206.sushi.server.Server;

import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Drone extends Model implements Runnable, Serializable {


	private Number speed;
	private transient Number progress;
	
	private Number capacity;
	private Number battery;
	
	private transient String status;
	
	private transient Postcode source;
	private transient Postcode destination;

	private transient float distanceToDestination;
	private transient float distanceToRestaurant;
	private transient float destinationRestaurantDistance;

	private transient DroneStatus droneStatus;
	private volatile boolean enabled;

	private transient Server server;

	private Order orderToPrepare;
	private Ingredient ingredientToRestock;




	public Drone(Number speed, Server server) {
		this.setSpeed(speed);
		this.setCapacity(1);
		this.setBattery(100);
		this.server = server;
//		Postcode newPostcode = new Postcode("SO17 1BX");
//        User user = new User("User", "Password", "Lol", newPostcode);
//        Order order = new Order(user);
//		this.orderTask = new OrderTask();
//		this.orderTask.add(order);
		this.setStatus(DroneStatus.IDLE);
		enabled = true;

	}

	public synchronized void run(){


			while (enabled) {

				if (droneStatus == DroneStatus.IDLE) {

					if (battery.intValue() < 100){
						recharge();
						this.setStatus(DroneStatus.IDLE);
					}

					try{
						Thread.sleep(1000);
					}catch (InterruptedException e){

					}

					synchronized (server) {
						List<Order> orders = server.getOrders();
//						Map<Dish, Number> dishStock = server.getDishStock();
						Map<Ingredient, Number> ingredientStock = server.getIngredientStockLevels();
						synchronized (orders) {
							for (Order order : orders) {


								if (order.getOrderStatus() == Order.OrderStatus.BEING_PREPARED) {

									boolean orderReady = checkDishStock(order);
									if (orderReady) {
                                        orderToPrepare = order;
									    order.setStatus(Order.OrderStatus.BEING_DELIVERED);
                                        this.setStatus(DroneStatus.DELIVERING_ORDER);
										System.out.println("This order has an empty dish list? : " + order.getDishes().isEmpty());
										prepareOrder(order);
									} else if(!orderReady){
										partiallyPrepareOrder(order);
									}

								}
							}

						}

						if(server.isIngredientRestocking()) {
							if (droneStatus == DroneStatus.IDLE) {
								for (Map.Entry<Ingredient, Number> cursor : ingredientStock.entrySet()) {

									if (cursor.getKey().getStatus() != Ingredient.IngredientStatus.BEING_RESTOCKED) {

										if (cursor.getValue().intValue() < server.getRestockThreshold(cursor.getKey()).intValue()) {
											ingredientToRestock = cursor.getKey();
											ingredientToRestock.setStatus(Ingredient.IngredientStatus.BEING_RESTOCKED);
											System.out.println(this.speed + " " + ingredientToRestock.getName() + " " + ingredientToRestock.getStatus());
											distanceToDestination = cursor.getKey().getSupplier().getDistance().floatValue();
											destinationRestaurantDistance = distanceToDestination;
											distanceToRestaurant = 0;
											this.setStatus(DroneStatus.COLLECTING_INGREDIENTS);
											setDestination(ingredientToRestock.getSupplier().getPostcode());
											setSource(server.getRestaurantPostcode());
											break;
										}
									}

								}
							}
						}





					}


				} else if (droneStatus == DroneStatus.DELIVERING_ORDER) {


					goToDestination();

					if (distanceToDestination <= 0) {
						orderToPrepare.setStatus(Order.OrderStatus.COMPLETED);
						this.setStatus(DroneStatus.RETURNING_ORDER);
						setSource(orderToPrepare.getUser().getPostcode());
						setDestination(server.getRestaurantPostcode());
					}

				} else if (droneStatus == DroneStatus.RETURNING_ORDER) {

					goToRestaurant();

					if (distanceToRestaurant <= 0) {

						this.setStatus(DroneStatus.IDLE);
						this.setSource(null);
						this.setDestination(null);
						distanceToRestaurant = 0;
						distanceToDestination = 0;
						setProgress(null);
					}
				} else if (droneStatus == DroneStatus.COLLECTING_INGREDIENTS){

				    goToDestination();

                    if (distanceToDestination<= 0){
                        this.setStatus(DroneStatus.RETURNING_INGREDIENTS);
                        setSource(ingredientToRestock.getSupplier().getPostcode());
                        setDestination(server.getRestaurantPostcode());
                    }
                } else if (droneStatus == DroneStatus.RETURNING_INGREDIENTS){

				    goToRestaurant();

                    if (distanceToRestaurant <= 0 ){
                        restockIngredients(ingredientToRestock);
                        ingredientToRestock.setStatus(Ingredient.IngredientStatus.IN_STOCK);
                        this.setStatus(DroneStatus.IDLE);
                        this.setSource(null);
                        this.setDestination(null);
                        distanceToRestaurant = 0;
                        distanceToDestination = 0;
                        setProgress(null);
                    }
                }


			}

	}



    public void goToDestination(){
		DroneStatus statusBeforeCharging = this.getDroneStatus();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {

        }

		if(battery.intValue() == 0){

			this.setDestination(server.getRestaurantPostcode());
			this.setSource(new Postcode(""));
			this.setProgress(0);

			while( distanceToRestaurant > 0){

				goToRestaurant();

			}
			recharge();
			this.setStatus(statusBeforeCharging);

		}
		Number newBattery = battery.intValue() - 1;
		if (newBattery.intValue() <0){
			newBattery = 0;
		}
		battery = newBattery;
        distanceToDestination -= this.getSpeed().floatValue() * (1 / 1000d);
        distanceToRestaurant += this.getSpeed().floatValue() * (1 / 1000d);
        setProgress((int)((distanceToRestaurant/destinationRestaurantDistance)*100));
    }

    public void goToRestaurant(){
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {

        }

		Number newBattery = battery.intValue() - 1;
		if (newBattery.intValue() <0){
			newBattery = 0;
		}
		battery = newBattery;

        distanceToDestination += this.getSpeed().floatValue() * (1 / 1000d);
        distanceToRestaurant -= this.getSpeed().floatValue() * (1 / 1000d);
        setProgress((int)((distanceToDestination/destinationRestaurantDistance)*100));

    }

    public void recharge(){
		this.setStatus(DroneStatus.CHARGING);
			while(battery.intValue()<100) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ie) {

				}
				battery=battery.intValue()+10;
				if(battery.intValue()>100) {
					battery=100;
				}
			}

	}


    public  void restockIngredients(Ingredient ingredient){
	    server.getIngredientStockLevels().put(ingredient, ingredient.getRestockAmount().intValue() + server.getIngredientStockLevels().get(ingredient).intValue());

    }


	public synchronized boolean checkDishStock(Order order){
		Map<Dish, Number> dishesFromOrder = order.getDishes();
		Map<Dish, Number> dishStock = server.getDishStock();



		for (Dish dish: dishesFromOrder.keySet()) {

			if (dishStock.get(dish).intValue() < dishesFromOrder.get(dish).intValue()){
//				System.out.println("Cannot fulfill order because there aren't enough dishes in stock");
				return false;
			}

		}

		return true;


	}

	public synchronized void prepareOrder(Order order){
		Map<Dish, Number> dishesFromOrder = order.getDishes();

		for (Dish dish: dishesFromOrder.keySet()){
			int currentDishStock = server.getDishStock().get(dish).intValue();
			Number newStock = currentDishStock - dishesFromOrder.get(dish).intValue();
			System.out.println("this is the new stock");
			server.setDishStock(dish, newStock);
		}
		setSource(server.getRestaurantPostcode());
        setDestination(order.getUser().getPostcode());
        distanceToDestination = order.getDistance().floatValue();
        destinationRestaurantDistance = distanceToDestination;
        distanceToRestaurant = 0;
	}

	public synchronized void partiallyPrepareOrder(Order order){
		Map<Dish,Number> dishesFromOrder = order.getDishes();

		for (Map.Entry<Dish, Number> cursor : dishesFromOrder.entrySet()){
			int currentDishStock = server.getDishStock().get(cursor.getKey()).intValue();
			int orderValue = cursor.getValue().intValue();
			System.out.println(order.getUser() + "'s Order used to have " + cursor.getKey().getName() + " with " + cursor.getValue());
			cursor.setValue(orderValue - currentDishStock);
			System.out.println(order.getUser() + "'s Order now has " + cursor.getKey().getName() + " with " + cursor.getValue());
			server.setDishStock(cursor.getKey(), 0);
		}
	}






	public Number getSpeed() {
		return speed;
	}

	
	public Number getProgress() {
		return progress;
	}
	
	public void setProgress(Number progress) {
		this.progress = progress;
	}
	
	public void setSpeed(Number speed) {
		this.speed = speed;
	}
	
	@Override
	public String getName() {
		return "Drone (" + getSpeed() + " speed)";
	}

	public Postcode getSource() {
		return source;
	}

	public void setSource(Postcode source) {
		this.source = source;
	}

	public Postcode getDestination() {
		return destination;
	}

	public void setDestination(Postcode destination) {
		this.destination = destination;
	}

	public Number getCapacity() {
		return capacity;
	}

	public void setCapacity(Number capacity) {
		this.capacity = capacity;
	}

	public Number getBattery() {
		return battery;
	}

	public void setBattery(Number battery) {
		this.battery = battery;
	}

	public String getStatus() {
		return status;
	}

	public Number getDistanceToDestination() {
		return distanceToDestination;
	}

	public Number getDistanceToRestaurant() {
		return distanceToRestaurant;
	}

	public void setStatus(DroneStatus status) {
		this.droneStatus = status;
		if (status == DroneStatus.IDLE) {
			String idle = "Idle";
			notifyUpdate("status", this.status, idle );
			this.status = idle;
		} else if (status == DroneStatus.COLLECTING_INGREDIENTS ) {
			String collectingIngredients = "Collecting " + ingredientToRestock.getName();
			notifyUpdate("status", this.status, collectingIngredients );
			this.status = collectingIngredients;
		} else if (status == DroneStatus.RETURNING_INGREDIENTS){
			String returningIngredients = "Returning with " + ingredientToRestock.getName();
			notifyUpdate("status", this.status, returningIngredients );
			this.status = returningIngredients;
		} else if (status == DroneStatus.DELIVERING_ORDER){
			String deliveringOrder = "Delivering " + orderToPrepare.getUser() + "'s order";
			notifyUpdate("status", this.status, deliveringOrder );
			server.updateClientOrderStatus(orderToPrepare);
			this.status = deliveringOrder;
		} else if (status == DroneStatus.RETURNING_ORDER){
			String returningOrder = "Returning to Restaurant";
			notifyUpdate("status", this.status, returningOrder );
			server.updateClientOrderStatus(orderToPrepare);
			this.status = returningOrder;
		}else if (status == DroneStatus.CHARGING){
			String charging = "Charging";
			notifyUpdate("status", this.status, charging);
			this.status = charging;
		}
	}


	public enum DroneStatus{
		IDLE,
		COLLECTING_INGREDIENTS,
		RETURNING_INGREDIENTS,
		DELIVERING_ORDER,
		RETURNING_ORDER,
		CHARGING,

	}

	private void readObject(ObjectInputStream ois) throws Exception{
		ois.defaultReadObject();
		this.setSource(null);
		this.setDestination(null);
		this.setStatus(DroneStatus.IDLE);
	}

	public DroneStatus getDroneStatus() {
		return droneStatus;
	}

	public void setServer(Server server){
		this.server = server;
	}

	public void setEnabled(boolean enabled){
		this.enabled = enabled;
	}
}
