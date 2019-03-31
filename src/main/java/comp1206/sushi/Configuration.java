package comp1206.sushi;

import comp1206.sushi.common.Postcode;
import comp1206.sushi.common.Restaurant;
import comp1206.sushi.common.Supplier;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;


public class Configuration {

    private BufferedReader reader;
    private FileReader file;
    private String fileName;
    ArrayList<Postcode> postcodeArrayList = new ArrayList<>();
    ArrayList<Supplier> supplierArrayList = new ArrayList<>();
    Restaurant restaurant;


    public Configuration(String fileName){
        this.fileName= fileName;
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
            return supplierArrayList;
        }catch(IOException e){

        }
        return null;
    }

    public static void main(String[] args) {
        Configuration configuration = new Configuration("Configuration.txt");
        ArrayList<Postcode> postcodeArrayList = configuration.getPostcodes();
        for (Postcode postcode: postcodeArrayList
             ) {
            System.out.println(postcode.getName());
        }
        Restaurant restaurant = configuration.getRestaurant();
        System.out.println(restaurant.getName() + " " + restaurant.getLocation().getName());
        ArrayList<Supplier> supplierArrayList = configuration.getSuppliers();
        for (Supplier supplier: supplierArrayList
             ) {
            System.out.println(supplier.getName() + " " + supplier.getPostcode().getName());
        }


    }
}
