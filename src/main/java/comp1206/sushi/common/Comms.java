package comp1206.sushi.common;

import java.util.Map;

// this class is used as request message for the client
public class Comms {


    private boolean initClientRequest;
    private boolean loginRequest;
    private boolean orderRequest;
    private User user;
    private Map<Dish, Number> orderDishes;

    public Comms (User user, Map<Dish, Number> orderDishes){
        this.initClientRequest = false;
        this.loginRequest = false;
        this.orderRequest=false;
        this.user = user;
        this.orderDishes = orderDishes;
    }
    public Comms(User user){
        this.initClientRequest = false;
        this.loginRequest = false;
        this.orderRequest=false;
        this.user = user;
    }

    public Comms(){
    }

    public boolean isInitClientRequest() {
        return initClientRequest;
    }

    public void setInitClientRequest(boolean initClientRequest) {
        this.initClientRequest = initClientRequest;
    }

    public boolean isLoginRequest() {
        return loginRequest;
    }

    public void setLoginRequest(boolean loginRequest) {
        this.loginRequest = loginRequest;
    }

    public User getUser() {
        return user;
    }

    public Map<Dish, Number> getOrderDishes(){return orderDishes;}

    public boolean isOrderRequest() {
        return orderRequest;
    }

    public void setOrderRequest(boolean orderRequest) {
        this.orderRequest = orderRequest;
    }
}
