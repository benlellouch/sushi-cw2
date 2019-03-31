package comp1206.sushi;

import comp1206.sushi.common.*;
import comp1206.sushi.server.Server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;


public class Configuration {

    private BufferedReader reader;
    private FileReader file;
    private String fileName;
    ArrayList<Postcode> postcodeArrayList = new ArrayList<>();
    ArrayList<Supplier> supplierArrayList = new ArrayList<>();
    ArrayList<Ingredient> ingredientArrayList = new ArrayList<>();
    ArrayList<Drone> droneArrayList = new ArrayList<>();
    ArrayList<Staff> staffArrayList = new ArrayList<>();
    ArrayList<User> userArrayList = new ArrayList<>();
    ArrayList<Dish> dishArrayList = new ArrayList<>();
    ArrayList<Order> orderArrayList = new ArrayList<>();
    Restaurant restaurant;
    Server server;


    public Configuration(String fileName, Server server){
        this.server = server;
        this.fileName= fileName;
        getPostcodes();
        getRestaurant();
        getSuppliers();
        getIngredients();
        getDishes();
        getStock();
        getUsers();
        getDrone();
//        try {
//            file = new FileReader(fileName);
//            reader = new BufferedReader(file);
//        }catch(FileNotFoundException e){
//            System.err.println("The file that is trying to be read was not found." +
//                    "\n please make sure the file is placed in the correct directory.");
//            System.exit(0);
//        }

    }

    public ArrayList<Postcode> getPostcodes(){
        try {
            file = new FileReader(fileName);
            reader = new BufferedReader(file);
            String line;



            while ((line = reader.readLine()) != null) {
                String[] words = line.split(":");
                if (words[0].equals("POSTCODE")){
                    Postcode postcode = new Postcode(words[1]);
                    postcodeArrayList.add(postcode);
                }
            }
            file.close();
            reader.close();
            server.postcodes = postcodeArrayList;
            return postcodeArrayList;

        }catch(IOException e){

        }

        return null;

    }
    public Restaurant getRestaurant(){
        try {
            String line;
            file = new FileReader(fileName);
            reader = new BufferedReader(file);
            while ((line = reader.readLine()) != null) {
                String[] words = line.split(":");
                if (words[0].equals("RESTAURANT")){
                    restaurant = new Restaurant(words[1], postcodeArrayList.get(0));
                }
            }
            file.close();
            reader.close();
            server.restaurant = restaurant;
            return restaurant;
        }catch(IOException e){

        }
        return null;
    }

    public ArrayList<Supplier> getSuppliers(){
        try {
            String line;
            int index;
            file = new FileReader(fileName);
            reader = new BufferedReader(file);
            while ((line = reader.readLine()) != null) {
                String[] words = line.split(":");
                if (words[0].equals("SUPPLIER")){
                    for (Postcode postcode: postcodeArrayList
                         ) {
                        if (postcode.getName().equals(words[2])){
                            Supplier supplier = new Supplier(words[1],postcode);
                            supplierArrayList.add(supplier);
                        }
                    }
                }
            }
            file.close();
            reader.close();
            server.suppliers = supplierArrayList;
            return supplierArrayList;
        }catch(IOException e){

        }
        return null;
    }

    public ArrayList<Ingredient> getIngredients(){
        try {
            String line;
            int index;
            file = new FileReader(fileName);
            reader = new BufferedReader(file);
            while ((line = reader.readLine()) != null) {
                String[] words = line.split(":");
                if (words[0].equals("INGREDIENT")){
                    for (Supplier supplier: supplierArrayList
                    ) {
                        if (supplier.getName().equals(words[3])){
                            Ingredient ingredient = new Ingredient(words[1],words[2],supplier, NumberFormat.getInstance().parse(words[4]),NumberFormat.getInstance().parse(words[5]),NumberFormat.getInstance().parse(words[6]));
                            ingredientArrayList.add(ingredient);
                        }
                    }
                }
            }
            file.close();
            reader.close();
            server.ingredients = ingredientArrayList;
            return ingredientArrayList;
        }catch(IOException e){

        }catch (ParseException e){

        }
        return null;
    }

    public ArrayList<Staff> getStaff(){
        try {
            file = new FileReader(fileName);
            reader = new BufferedReader(file);
            String line;



            while ((line = reader.readLine()) != null) {
                String[] words = line.split(":");
                if (words[0].equals("STAFF")){
                    Staff staff = new Staff(words[1]);
                    staffArrayList.add(staff);
                }
            }
            file.close();
            reader.close();
            server.staff = staffArrayList;
            return staffArrayList;

        }catch(IOException e){

        }

        return null;

    }

    public ArrayList<Drone> getDrone(){
        try {
            file = new FileReader(fileName);
            reader = new BufferedReader(file);
            String line;



            while ((line = reader.readLine()) != null) {
                String[] words = line.split(":");
                if (words[0].equals("DRONE")){
                    Drone drone = new Drone(NumberFormat.getInstance().parse(words[1]));
                    droneArrayList.add(drone);
                }
            }
            file.close();
            reader.close();
            server.drones = droneArrayList;
            return droneArrayList;

        }catch(IOException e){

        }catch(ParseException e){}

        return null;

    }

    public ArrayList<User> getUsers(){
        try {
            file = new FileReader(fileName);
            reader = new BufferedReader(file);
            String line;



            while ((line = reader.readLine()) != null) {
                String[] words = line.split(":");
                if (words[0].equals("USER")){
                    for (Postcode postcode: postcodeArrayList
                         ) {
                        if (postcode.getName().equals(words[4])){
                            User user = new User(words[1],words[2],words[3],postcode);

                        }
                    }

                }
            }
            file.close();
            reader.close();
            server.users = userArrayList;
            return userArrayList;

        }catch(IOException e){

        }

        return null;

    }

    public ArrayList<Dish> getDishes(){
        try {
            file = new FileReader(fileName);
            reader = new BufferedReader(file);
            String line;



            while ((line = reader.readLine()) != null) {
                String[] words = line.split(":");
                if (words[0].equals("DISH")){
//                    Dish dish = new Dish(words[1],words[2],NumberFormat.getInstance().parse(words[3]),NumberFormat.getInstance().parse(words[4]),NumberFormat.getInstance().parse(words[5]));
                    Dish dish = server.addDish(words[1],words[2],NumberFormat.getInstance().parse(words[3]),NumberFormat.getInstance().parse(words[4]),NumberFormat.getInstance().parse(words[5]));
                    String[] ingredients = words[6].split(",");
                    for (String ingredient:ingredients
                         ) {
                        String[] amountAndName = ingredient.split(" ");
                        for(Ingredient newIngredient: ingredientArrayList) {
                            if (amountAndName[2].equals(newIngredient.getName())) {
                                server.addIngredientToDish(dish, newIngredient, NumberFormat.getInstance().parse(amountAndName[0]));
                            }
                        }
                    }
                }
            }
            file.close();
            reader.close();
            return dishArrayList;

        }catch(IOException e){

        }catch (ParseException e){

        }

        return null;

    }

    public ArrayList<Order> getOrders(){
        try {
            file = new FileReader(fileName);
            reader = new BufferedReader(file);
            String line;



            while ((line = reader.readLine()) != null) {
                String[] words = line.split(":");
                if (words[0].equals("ORDER")){

                }
            }
            file.close();
            reader.close();
            server.staff = staffArrayList;
            return orderArrayList;

        }catch(IOException e){

        }

        return null;

    }

    public void getStock(){
        try {
            file = new FileReader(fileName);
            reader = new BufferedReader(file);
            String line;



            while ((line = reader.readLine()) != null) {
                String[] words = line.split(":");
                if (words[0].equals("STOCK")){
                    for (Ingredient ingredient : server.getIngredients()){
                        if (words[1].equals(ingredient.getName())){
                            server.setStock(ingredient, NumberFormat.getInstance().parse(words[2]));
                        }
                    }

                    for (Dish dish : server.getDishes()){
                        if (words[1].equals(dish.getName())){
                            server.setStock(dish,NumberFormat.getInstance().parse(words[2]));
                        }
                    }
                }
            }
            file.close();
            reader.close();


        }catch(IOException e){

        } catch (ParseException e){

        }


    }

    public static void main(String[] args) {
        try {
            FileReader file = new FileReader("Configuration.txt");
            BufferedReader reader = new BufferedReader(file);
            String line;



            while ((line = reader.readLine()) != null) {
                String[] words = line.split(":");
                if (words[0].equals("DISH")) {
                   String[] ingredients = words[6].split(",");
                    for (String ingredient: ingredients
                         ) {
                        String[] AmountandName = ingredient.split(" ");
                        System.out.println(AmountandName[0] + " " + AmountandName[2]);
                    }
                }

            }


        }catch(IOException e){

        }

    }
}
