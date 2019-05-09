package comp1206.sushi.common;

import java.util.Map;

// this class is used as request message for the client
public class RequestPacket {


    private boolean initClientRequest;
    private boolean loginRequest;
    private boolean orderRequest;
    private boolean orderStatusUpdate;

    private User user;
    private Map<String, Number> orderDishes;
    private String orderString;
    private Order.OrderStatus orderStatus;

    public RequestPacket(User user, Map<String, Number> orderDishes){
        this.initClientRequest = false;
        this.loginRequest = false;
        this.orderRequest=false;
        this.orderStatusUpdate = false;
        this.user = user;
        this.orderDishes = orderDishes;
    }
    public RequestPacket(User user){
        this.initClientRequest = false;
        this.loginRequest = false;
        this.orderRequest=false;
        this.orderStatusUpdate = false;
        this.user = user;
    }

    public RequestPacket(String string){
        this.initClientRequest = false;
        this.loginRequest = false;
        this.orderRequest=false;
        this.orderStatusUpdate = false;
        this.orderString = string;
    }

    public RequestPacket(){}

    public RequestPacket(String orderName, User orderUser, Order.OrderStatus orderStatus ){
        this.initClientRequest = false;
        this.loginRequest = false;
        this.orderRequest=false;
        this.orderStatusUpdate = false;
        this.orderString = orderName;
        this.user = orderUser;
        this.orderStatus = orderStatus;
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

    public Map<String, Number> getOrderDishes(){return orderDishes;}

    public boolean isOrderRequest() {
        return orderRequest;
    }

    public void setOrderRequest(boolean orderRequest) {
        this.orderRequest = orderRequest;
    }

    public String getOrderString() {
        return orderString;
    }

    public Order.OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public boolean isOrderStatusUpdate() {
        return orderStatusUpdate;
    }

    public void setOrderStatusUpdate(boolean orderStatusUpdate) {
        this.orderStatusUpdate = orderStatusUpdate;
    }

    public void receiveMessage(){}
}
