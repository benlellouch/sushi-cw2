package comp1206.sushi.common;

import java.util.LinkedList;
import java.util.Queue;

/**
 * This class stores a queue of the orders that need to be fulfilled so
 * that (hopefully) only one drone delivers the same order.
 * **/

public class OrderTask {

    private Queue<Order> orderQueue;

    public OrderTask(){
        orderQueue = new LinkedList<>();
    }

    public boolean add(Order order) {
        return orderQueue.add(order);
    }

    public Order remove() {
        return orderQueue.remove();
    }

    public Order poll() {
        return orderQueue.poll();
    }

    public Order element() {
        return orderQueue.element();
    }

    public Order peek() {
        return orderQueue.peek();
    }

    public boolean isEmpty() {
        return orderQueue.isEmpty();
    }
}
