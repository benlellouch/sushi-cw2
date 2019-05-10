package comp1206.sushi.common;

import comp1206.sushi.server.Server;

import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.Random;

public class Staff extends Model implements Runnable, Serializable {

	private String name;
	private String status;
	private Number fatigue;
	private transient Server server;
	private volatile boolean enable;
	private  Random random = new Random();

	
	public Staff(String name, Server server) {
		this.setName(name);
		this.setFatigue(0);
		this.setStatus("Idle");
		this.server =server;
		enable = true;


	}

	public synchronized void run(){
	    synchronized (this) {
            checkDishStock();
        }
    }

	public synchronized void checkDishStock(){
        while (enable) {
            Map<Dish, Number> dishStock = server.getDishStockLevels();
            if (this.fatigue.intValue() >= 100){
                fatigue = 100;
                recharge();
                setStatus("Idle");

            }
            try {
                Thread.sleep(500);
            }catch (InterruptedException e){
                System.out.println("Oof");
            }
            if(server.isDishRestocking()) {
                synchronized (this) {
                    for (Map.Entry<Dish, Number> dishNumberEntry : dishStock.entrySet()) {
                        synchronized (this) {
                            if (!server.isBeingMade(dishNumberEntry.getKey())) {
                                if (dishNumberEntry.getValue().intValue() < dishNumberEntry.getKey().getRestockThreshold().intValue()) {
                                    if (checkIngredientStock(dishNumberEntry.getKey())) {
                                        synchronized (server) {
                                            server.addDishBeingMade(dishNumberEntry.getKey());
                                        }
                                        prepareDish(dishNumberEntry.getKey());
                                    }
                                }

                            }
                        }

                    }
                }
            }
        }

	}


	public void recharge(){
	    this.setStatus("Taking a break");
	    while(fatigue.intValue() > 0){
	        try{
	            Thread.sleep(1000);
            }catch (InterruptedException e){

            }
	        fatigue=fatigue.intValue()-10;
	        if(fatigue.intValue()<0){
	            fatigue = 0;
            }
        }
    }




    public synchronized boolean checkIngredientStock(Dish dish){
	    Map<Ingredient, Number> ingredients = dish.getRecipe();
        Map<Ingredient, Number> ingredientStock = server.getIngredientStockLevels();
//        System.out.println("The ingredients" + ingredients);
//        System.out.println("The Stock" + ingredientStock);
//        System.out.println("The dish restock amount" + dish.getRestockAmount());


	    for (Ingredient ingredient: ingredients.keySet()){
            try {
                int ingredientStockValue = ingredientStock.get(ingredient).intValue();
//                System.out.println(ingredient.getName() + " " + ingredientStockValue);
                int ingredienttobeRestockedValue = ingredients.get(ingredient).intValue() * dish.getRestockAmount().intValue();
                if (ingredientStockValue < ingredienttobeRestockedValue) {
//                System.out.println("Cannot make "+ dish.getName() +" because there aren't enough ingredients");
                    return false;
                }
            }catch (NullPointerException e){
                return false;
            }
        }

        return true;
    }

    public void  prepareDish(Dish dish){
        this.setStatus("Making: " + dish.getName());
        Map<Ingredient, Number> ingredients = dish.getRecipe();
        int timetoRestock = random.nextInt(60000);
        int dishRestockThreshold = dish.getRestockThreshold().intValue();
        int currentDishStock = server.getDishStockLevels().get(dish).intValue();
        Number newDishStock = currentDishStock + dish.getRestockAmount().intValue();
        if (timetoRestock < 20000){
            timetoRestock+= 20000;
        }
        for (Ingredient ingredient: ingredients.keySet()){
            int currentIngredientSock = server.getIngredientStockLevels().get(ingredient).intValue();
            Number newStock = currentIngredientSock - ingredients.get(ingredient).intValue() * dish.getRestockAmount().intValue() ;
            server.setStock(ingredient, newStock);
        }
        try{
            Thread.sleep(timetoRestock);
            server.setStock(dish, newDishStock);
        } catch (InterruptedException e){
            e.printStackTrace();
        }
        this.setStatus("Idle");
        this.setFatigue(fatigue.intValue() + 10);
        server.removeDishBeingMade(dish);

    }

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Number getFatigue() {
		return fatigue;
	}

	public void setFatigue(Number fatigue) {
		this.fatigue = fatigue;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		notifyUpdate("status",this.status,status);
		this.status = status;
	}

	private void readObject(ObjectInputStream ois) throws Exception{
	    ois.defaultReadObject();
	    this.setStatus("Idle");
    }

    public void setServer(Server server){
	    this.server=server;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }
}
