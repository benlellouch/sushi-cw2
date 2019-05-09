package comp1206.sushi.server;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import comp1206.sushi.common.*;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;

public class Comms extends Listener {
    private Server server;
    private com.esotericsoftware.kryonet.Server kryoServer;
    public Comms(Server server){
        this.server = server;
        try {
            kryoServer = new com.esotericsoftware.kryonet.Server();
            synchronized (this){kryoServer.start();}
            kryoServer.bind(54555, 54777);
        }catch (IOException e){
            e.printStackTrace();
        }
        Kryo kryo = kryoServer.getKryo();
        kryo.register(RequestPacket.class);
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
        kryoServer.addListener(this);
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
        if (object instanceof RequestPacket) {
            System.out.println("I do receive a comms object");
            RequestPacket request =  (RequestPacket) object;
            User user = request.getUser();
            System.out.println(request);
            System.out.println(request.isLoginRequest());
            if (request.isInitClientRequest()) {
                server.addUser(user);
                initialiseClient(connection, user);
            } else if (request.isLoginRequest()) {
                System.out.println("I get a login request with the username:" + user.getName());
                for (User cursor : server.getUsers()) {
                    if (user.getName().equals(cursor.getName()) && user.getPassword().equals(cursor.getPassword())) {
                        connection.sendTCP(cursor);
                        System.out.println("I sent out the user that the client wants: " + cursor.getName());
                        initialiseClient(connection, cursor);
                    }
                }

            }else if (request.isOrderRequest()){

                String orderString = (String) request.getOrderString();
                String[] words = orderString.split(":");

                if (words[0].equals("ORDER")){

                    Order order = new Order();


                    for (User cursor: server.getUsers()
                    ) {
                        if (words[1].equals(cursor.getName())) {
                            order = new Order(cursor);

                        }

                    }
                    String[] dishes = words[2].split(",");
                    for (String dish: dishes) {
                        String[] amountAndName = dish.split(" \\* ");

                        for(Dish newDish: server.getDishes()) {
//                            System.out.println(amountAndName[1] + " and the newDish: " + newDish.getName() +  " length of array");

                            if (amountAndName[1].equals(newDish.getName())) {
                                try {
                                    server.addDishtoOrder(order,newDish, NumberFormat.getInstance().parse(amountAndName[0]));
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                System.out.println("This order was parsed " + order.getName() + " for " + order.getUser() + " with an empty dish list?" + order.getDishes().isEmpty());
                            }
                        }
                    }
                    server.addOrder(order);
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
                connection.sendTCP(server.getRestaurant());
                for (Postcode postcode : server.getPostcodes()){
                    connection.sendTCP(postcode);
                }
            }
        } else if (object instanceof Order){
            System.out.println("I receive an order object");
            Order order = (Order) object;
            System.out.println(order.getName() + " " + order.getUser());
            for (Order cursor :server.getOrders()){
                System.out.println(cursor.getName() + " " + cursor.getUser());
                if (cursor.getName().equals(order.getName()) && cursor.getUser().getName().equals(order.getUser().getName())){
                    server.cancelOrder(cursor);
                    System.out.println("Removed order");
                }
            }

        }
    }

    public synchronized void initialiseClient(Connection connection, User user){

        for (Dish dish : server.getDishes()) {
            connection.sendTCP(dish);
        }
        for (Order order: server.getOrders()){
            if (order.getUser().equals(user)){
                connection.sendTCP(order);
                System.out.println("Order sent");
            }
        }
    }

    public void sendDish(Dish dish){
        if(kryoServer.getConnections().length > 0){
            kryoServer.sendToAllTCP(dish);
        }
    }

    public void sendOrder(Order order){
        if(kryoServer.getConnections().length > 0){
            kryoServer.sendToAllTCP(order);
        }
    }

    public void updateOrder(Order order){
        RequestPacket orderUpdate = new RequestPacket(order.getName(), order.getUser(), order.getOrderStatus());
        orderUpdate.setOrderStatusUpdate(true);
        kryoServer.sendToAllTCP(orderUpdate);
    }
}
