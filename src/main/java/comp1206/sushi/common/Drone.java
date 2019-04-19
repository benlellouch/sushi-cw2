package comp1206.sushi.common;

import comp1206.sushi.common.Drone;
import comp1206.sushi.server.Server;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class Drone extends Model implements Runnable{

	private Number speed;
	private Number progress;
	
	private Number capacity;
	private Number battery;
	
	private String status;
	
	private Postcode source;
	private Postcode destination;

	private float distanceToDestination;
	private float distanceToRestaurant;

	private DroneStatus droneStatus;
	private boolean enabled;

	private Server server;

	private Order orderToPrepare;




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

	}

	public synchronized void run(){
		enabled = true;

			while (enabled) {

				if (droneStatus == DroneStatus.IDLE) {

					synchronized (server) {

						if (!(server.getOrderQueue().isEmpty())) {

							orderToPrepare = server.getOrderQueue().peek();
							System.out.println("--------------------------------------------I am going to check for stock for" + orderToPrepare.getName());
							boolean orderInStock = checkDishStock(orderToPrepare);
							if(orderInStock){

								orderToPrepare = server.getOrderQueue().remove();
								System.out.println("Apparently everything is in stock so i'm going to prepare" + orderToPrepare.getName());
								prepareOrder(orderToPrepare);
							}
						} else {

						}
					}


				} else if (droneStatus == DroneStatus.DELIVERING_ORDER) {


					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {

					}

					distanceToDestination -= this.getSpeed().floatValue() * (1 / 1000d);
					distanceToRestaurant += this.getSpeed().floatValue() * (1 / 1000d);

					if (distanceToDestination <= 0) {
						orderToPrepare.setStatus(Order.OrderStatus.COMPLETED);
						this.setStatus(DroneStatus.RETURNING_ORDER);
					}

				} else if (droneStatus == DroneStatus.RETURNING_ORDER) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {

					}

					distanceToDestination += this.getSpeed().floatValue() * (1 / 1000d);
					distanceToRestaurant -= this.getSpeed().floatValue() * (1 / 1000d);

					if (distanceToRestaurant <= 0) {

						this.setStatus(DroneStatus.IDLE);
						this.setDestination(null);
						distanceToRestaurant = 0;
						distanceToDestination = 0;
					}
				}


			}

	}

//	public synchronized Order checkForOrders(){
//
//		List<Order> orders = server.getOrders();
//		for (Order order: orders) {
//
//			if (order.getOrderStatus() == Order.OrderStatus.BEING_PREPARED){
//
//				checkDishStock(order);
//				return order;
//			}
//
//		}
//
//		return null;
//	}


	public synchronized boolean checkDishStock(Order order){
		Map<Dish, Number> dishesFromOrder = order.getDishes();
		Map<Dish, Number> dishStock = server.getDishStock();



		for (Dish dish: dishesFromOrder.keySet()) {

			if (dishStock.get(dish).intValue() < dishesFromOrder.get(dish).intValue()){
				System.out.println("Cannot fulfill order because there aren't enough dishes in stock");
				return false;
			}

		}

		return true;


	}

	public void prepareOrder(Order order){
		Map<Dish, Number> dishesFromOrder = order.getDishes();

		for (Dish dish: dishesFromOrder.keySet()){
			int currentDishStock = server.getDishStock().get(dish).intValue();
			Number newStock = currentDishStock - dishesFromOrder.get(dish).intValue();
			System.out.println("this is the new stock");
			server.setDishStock(dish, newStock);
		}
        this.setStatus(DroneStatus.DELIVERING_ORDER);
        setDestination(order.getUser().getPostcode());
        distanceToDestination = order.getDistance().floatValue();
        distanceToRestaurant = 0;
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
			String collectingIngredients = "Collecting Ingredients";
			notifyUpdate("status", this.status, collectingIngredients );
			this.status = collectingIngredients;
		} else if (status == DroneStatus.RETURNING_INGREDIENTS){
			String returningIngredients = "Returning with Ingredients";
			notifyUpdate("status", this.status, returningIngredients );
			this.status = returningIngredients;
		} else if (status == DroneStatus.DELIVERING_ORDER){
			String deliveringOrder = "Delivering Order";
			notifyUpdate("status", this.status, deliveringOrder );
			this.status = deliveringOrder;
		} else if (status == DroneStatus.RETURNING_ORDER){
			String returningOrder = "Returning to Restaurant";
			notifyUpdate("status", this.status, returningOrder );
			this.status = returningOrder;
		}
	}

	public void setTask(){

	}

	public enum DroneStatus{
		IDLE,
		COLLECTING_INGREDIENTS,
		RETURNING_INGREDIENTS,
		DELIVERING_ORDER,
		RETURNING_ORDER,

	}

	public DroneStatus getDroneStatus() {
		return droneStatus;
	}
}
