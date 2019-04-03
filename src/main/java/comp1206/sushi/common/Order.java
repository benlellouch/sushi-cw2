package comp1206.sushi.common;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class Order extends Model {

	private String status;
	private User user;
	private Map<Dish, Number> dishes;
	
	public Order(User user) {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/YYYY HH:mm:ss");  
		LocalDateTime now = LocalDateTime.now();  
		this.name = dtf.format(now);
		this.user = user;
		this.dishes = new HashMap<>();
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

	public void setStatus(String status) {
		notifyUpdate("status",this.status,status);
		this.status = status;
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
}
