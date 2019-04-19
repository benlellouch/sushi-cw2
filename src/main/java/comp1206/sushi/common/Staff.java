package comp1206.sushi.common;

import comp1206.sushi.server.Server;

import java.util.Map;
import java.util.Random;

public class Staff extends Model implements Runnable{

	private String name;
	private String status;
	private Number fatigue;
	private Server server;
	private Random random = new Random();

	
	public Staff(String name, Server server) {
		this.setName(name);
		this.setFatigue(0);
		this.setStatus("Idle");
		this.server =server;


	}

	public synchronized void run(){
	    synchronized (this) {
            checkDishStock();
        }
    }

	public synchronized void checkDishStock(){
        Map<Dish, Number> dishStock = server.getDishStockLevels();

        while (true) {
            try {
                int randomsleep = random.nextInt(1000);
                Thread.sleep(randomsleep);
            }catch (InterruptedException e){
                System.out.println("Oof");
            }
                synchronized (this) {
                    for (Map.Entry<Dish, Number> dishNumberEntry : dishStock.entrySet()) {
                        synchronized (this) {
                            if (!server.isBeingMade(dishNumberEntry.getKey())) {
                                if (dishNumberEntry.getValue().intValue() <= dishNumberEntry.getKey().getRestockThreshold().intValue()) {
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




    public boolean checkIngredientStock(Dish dish){
	    Map<Ingredient, Number> ingredients = dish.getRecipe();
	    Map<Ingredient, Number> ingredientStock = server.getIngredientStockLevels();

	    for (Ingredient ingredient: ingredients.keySet()){
	        if (ingredientStock.get(ingredient).intValue() <  ingredients.get(ingredient).intValue() * dish.getRestockAmount().intValue()){
                System.out.println("Cannot make "+ dish.getName() +" because there aren't enough ingredients");
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

}
