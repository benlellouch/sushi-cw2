package comp1206.sushi.common;

import comp1206.sushi.server.ServerInterface;

import java.util.Map;
import java.util.Random;

public class Staff extends Model implements Runnable{

	private String name;
	private String status;
	private Number fatigue;
	private ServerInterface server;
	private Random random = new Random();
	
	public Staff(String name, ServerInterface server) {
		this.setName(name);
		this.setFatigue(0);
		this.setStatus("Idle");
		this.server =server;

	}

	public void run(){
        checkDishStock();
    }

	public void checkDishStock(){
        Map<Dish, Number> dishStock = server.getDishStockLevels();
        while (true) {
            synchronized (this) {
                for (Map.Entry<Dish, Number> dishNumberEntry : dishStock.entrySet()) {

                    if (dishNumberEntry.getValue().intValue() <= dishNumberEntry.getKey().getRestockThreshold().intValue()) {

                        if (checkIngredientStock(dishNumberEntry.getKey())) {
                            prepareDish(dishNumberEntry.getKey());

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

    public void prepareDish(Dish dish){
	    this.setStatus("Making: " + dish.getName());
        Map<Ingredient, Number> ingredients = dish.getRecipe();
        int timetoRestock = random.nextInt(6000);
        if (timetoRestock < 2000){
            timetoRestock+= 2000;
        }
        for (Ingredient ingredient: ingredients.keySet()){
            Number newStock = server.getIngredientStockLevels().get(ingredient).intValue() - ingredients.get(ingredient).intValue() * dish.getRestockAmount().intValue() ;
            server.setStock(ingredient, newStock);
        }
        try{
            Thread.sleep(timetoRestock);
            Number newDishStock = server.getDishStockLevels().get(dish).intValue() + dish.getRestockAmount().intValue();
            server.setStock(dish, newDishStock);
        } catch (InterruptedException e){
            e.printStackTrace();
        }
        this.setStatus("Idle");
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
