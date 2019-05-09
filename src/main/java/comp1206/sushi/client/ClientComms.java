package comp1206.sushi.client;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import comp1206.sushi.common.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;


public class ClientComms extends Listener {

    private Client client;
    private com.esotericsoftware.kryonet.Client kryoClient;

    public ClientComms(Client client){
        this.client = client;
        try {
            synchronized (this) {
                kryoClient = new com.esotericsoftware.kryonet.Client(32768,32768);
                kryoClient.start();
                kryoClient.connect(5000, "127.0.0.1", 54555, 54777);
            }
        }catch (IOException e ){
            System.out.println("Something wrong the client comms");
        }

        Kryo kryo = kryoClient.getKryo();
        kryo.register(RequestPacket.class);
        kryo.register(Dish.class);
        kryo.register(HashMap.class);
        kryo.register(ArrayList.class);
        kryo.register(Ingredient.class);
        kryo.register(Supplier.class);
        kryo.register(Postcode.class);
        kryo.register(Restaurant.class);
        kryo.register(Order.class);
        kryo.register(CopyOnWriteArrayList.class);
        kryo.register(User.class);
        kryo.register(Order.OrderStatus.class);
        kryo.register(Ingredient.IngredientStatus.class);

        String string = "getPostcodes";
        kryoClient.sendTCP(string);
        synchronized (this) {
            kryoClient.addListener(this);
        }
        System.out.println(kryoClient.getRemoteAddressTCP());
    }

    public void connected(Connection connection){
        System.out.println("The connection is complete");
    }
    public void disconnected(Connection connection){
        System.out.println("Disconnected");
    }
    public void received (Connection connection, Object object) {

        if (object instanceof Dish) {
            Dish dishToAdd = (Dish) object;
            boolean removed = false;

            for (Dish dish: client.getDishes()) {
                if (dish.getName().equals(dishToAdd.getName()) && dish.getDescription().equals(dishToAdd.getDescription())) {
                    client.removeDish(dish);
                    System.out.println("Removed Dish");
                    removed = true;
                }

            }

            if (!removed) {
                client.addDish(dishToAdd);
                System.out.println("Added dish");
            }




        } else if (object instanceof User) {
            System.out.println("I receive the User");
            client.setLoggedInUser((User) object);
        } else if (object instanceof String) {
            String string = (String) object;
            System.out.println(string);

        } else if (object instanceof Order) {
            boolean removed = false;


            Order order = (Order) object;
            User user = client.getLoggedInUser();

            if(user.getName().equals(order.getUser().getName())){

                for (Order cursor : user.getOrders()){
                    if (cursor.getName().equals(order.getName()) && cursor.getUser().getName().equals(order.getUser().getName())){

                        user.getOrders().remove(cursor);
                        removed = true;
                        System.out.println("Removed order");
                        client.notifyUpdate();
                    }
                }

                if (!removed) {
                    System.out.println(order.getUser().getName());
                    user.getOrders().add(order);
                    client.notifyUpdate();
                    System.out.println("Added Order");
                    System.out.println(order.getUser());
                    for (Map.Entry<Dish, Number> cursor : order.getDishes().entrySet()) {
                        System.out.println(cursor.getKey() + " " + cursor.getValue());
                    }
                }
            }

        }else if (object instanceof  Restaurant){
            Restaurant newRestaurant = (Restaurant) object;
            client.setRestaurant(newRestaurant);
        }else if(object instanceof  Postcode){
            Postcode postcode = (Postcode) object;
            client.addPostcode(postcode);
        }else if(object instanceof RequestPacket){
            RequestPacket orderStatusUpdate = (RequestPacket) object;
            System.out.println("I receive a comms object");
            if(client.isLoggedIn()) {
                if (orderStatusUpdate.isOrderStatusUpdate()) {
                    System.out.println("The status update is correct wola");
                    if (orderStatusUpdate.getUser().getName().equals(client.getLoggedInUser().getName())) {
                        System.out.println("the users are equals");
                        for (Order order : client.getLoggedInUser().getOrders()) {
                            if (order.getName().equals(orderStatusUpdate.getOrderString())) {

                                order.setStatus(orderStatusUpdate.getOrderStatus());
//                                this.notifyUpdate();
                            }
                        }
                    }

                }
            }
        }

    }

    public void requestLogin(User user){
        RequestPacket loginRequest = new RequestPacket(user);
        loginRequest.setLoginRequest(true);
        kryoClient.sendTCP(loginRequest);
    }

    public void registerUser(User user){
        RequestPacket registerRequest = new RequestPacket(user);
        registerRequest.setInitClientRequest(true);
        kryoClient.sendTCP(registerRequest);
    }

    public void requestOrder(User user, Map<Dish, Number> basket){
        StringBuilder orderString = new StringBuilder("ORDER:");
        orderString.append(user.getName()+ ":");
        for(Map.Entry<Dish, Number> cursor : basket.entrySet()){
            orderString.append(cursor.getValue()+" * "+cursor.getKey()+ ",");
        }
        RequestPacket orderRequest = new RequestPacket(orderString.toString());
        orderRequest.setOrderRequest(true);
        kryoClient.sendTCP(orderRequest);
    }

    public void cancelOrder(Order order){
        kryoClient.sendTCP(order);
    }
}
