package comp1206.sushi.common;

import com.sun.org.apache.xpath.internal.operations.Or;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class Order extends Model {

	private String status;
	private User user;
	private Map<Dish, Number> dishes;
	private OrderStatus orderStatus;

	public Order(){}
	
	public Order(User user) {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/YYYY HH:mm:ss");  
		LocalDateTime now = LocalDateTime.now();  
		this.name = dtf.format(now);
		this.user = user;
		this.dishes = new HashMap<>();
		this.setStatus(OrderStatus.BEING_PREPARED);

	}

	public Number getDistance() {
		return user.getDistance();
	}

	@Override
	public String getName() {
		return this.name;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(OrderStatus status) {
		this.orderStatus = status;
		if(status == OrderStatus.BEING_PREPARED) {
			String beingPrepared = "In preparation";
			notifyUpdate("status", this.status, beingPrepared);
			this.status = beingPrepared;
		} else if(status == OrderStatus.BEING_DELIVERED) {
			String beingDelivered = "Out for Delivery";
			notifyUpdate("status", this.status, beingDelivered);
			this.status = beingDelivered;
		} else if (status == OrderStatus.COMPLETED){
			String completed = "Completed";
			notifyUpdate("status", this.status, completed);
			this.status = completed;

		}
	}

	public OrderStatus getOrderStatus() {
		return orderStatus;
	}

	public Map<Dish, Number> getDishes() {
        return dishes;
    }


    public void setDishes(Map<Dish, Number> dishes) {
        this.dishes = dishes;
    }

	public User getUser() {
		return user;
	}

	public enum OrderStatus{
		BEING_PREPARED,
		BEING_DELIVERED,
		COMPLETED,
	}
}
