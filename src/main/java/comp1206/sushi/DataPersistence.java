package comp1206.sushi;


import comp1206.sushi.common.*;
import comp1206.sushi.server.Server;
import comp1206.sushi.server.ServerInterface;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class DataPersistence implements Serializable{

    private transient ServerInterface server;
    private Restaurant restaurant;
    private List<Dish> dishes;
    private List<Drone> drones;
    private List<Ingredient> ingredients;
    private List<Order> orders;
    private List<Staff> staff;
    private List<Supplier> suppliers;
    private List<User> users;
    private List<Postcode> postcodes;
    private Map<Ingredient, Number> ingredientStock;
    private Map<Dish, Number> dishStock;





    public DataPersistence(Server server) throws IOException {
        this.server = server;
        restaurant = server.getRestaurant();
        dishes = server.getDishes();
        drones = server.getDrones();
        ingredients = server.getIngredients();
        orders = server.getOrders();
        staff = server.getStaff();
        suppliers = server.getSuppliers();
        users = server.getUsers();
        postcodes = server.getPostcodes();
        ingredientStock = server.getIngredientStockLevels();
        dishStock = server.getDishStock();
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    public List<Dish> getDishes() {
        return dishes;
    }

    public void setDishes(List<Dish> dishes) {
        this.dishes = dishes;
    }

    public List<Drone> getDrones() {
        return drones;
    }

    public void setDrones(List<Drone> drones) {
        this.drones = drones;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    public List<Staff> getStaff() {
        return staff;
    }

    public void setStaff(List<Staff> staff) {
        this.staff = staff;
    }

    public List<Supplier> getSuppliers() {
        return suppliers;
    }

    public void setSuppliers(List<Supplier> suppliers) {
        this.suppliers = suppliers;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public List<Postcode> getPostcodes() {
        return postcodes;
    }

    public void setPostcodes(List<Postcode> postcodes) {
        this.postcodes = postcodes;
    }

    public Map<Ingredient, Number> getIngredientStock() {
        return ingredientStock;
    }

    public void setIngredientStock(Map<Ingredient, Number> ingredientStock) {
        this.ingredientStock = ingredientStock;
    }

    public Map<Dish, Number> getDishStock() {
        return dishStock;
    }

    public void setDishStock(Map<Dish, Number> dishStock) {
        this.dishStock = dishStock;
    }
}
