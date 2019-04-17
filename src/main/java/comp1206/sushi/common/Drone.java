package comp1206.sushi.common;

import comp1206.sushi.common.Drone;
import comp1206.sushi.server.Server;

import java.util.List;
import java.util.Map;

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

	public Drone(Number speed, Server server) {
		this.setSpeed(speed);
		this.setCapacity(1);
		this.setBattery(100);
		this.server = server;
		this.setStatus(DroneStatus.IDLE);

	}

	public synchronized void run(){
		enabled = true;
		while (enabled){
			if (droneStatus == DroneStatus.IDLE) {
				Order orderToPrepare = checkForOrders();
				if (orderToPrepare != null) {
					prepareOrder(orderToPrepare);

				}
			}



		}
	}

	public Order checkForOrders(){
		List<Order> orders = server.getOrders();
		for (Order order: orders) {

			if (order.getOrderStatus() == Order.OrderStatus.BEING_PREPARED){
				if (checkDishStock(order)){
					return order;
				}
			}

		}

		return null;
	}

	public boolean checkDishStock(Order order){
		Map<Dish, Number> dishesFromOrder = order.getDishes();
		Map<Dish, Number> dishStock = server.getDishStock();

		for (Dish dish: dishesFromOrder.keySet()
			 ) {
			if (dishStock.get(dish).intValue() < dishesFromOrder.get(dish).intValue() * dish.getRestockAmount().intValue()){
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
			Number newStock = currentDishStock - dishesFromOrder.get(dish).intValue() * dish.getRestockAmount().intValue();
			server.setDishStock(dish, newStock);

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

	public float getDistanceToDestination() {
		return distanceToDestination;
	}

	public float getDistanceToRestaurant() {
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
