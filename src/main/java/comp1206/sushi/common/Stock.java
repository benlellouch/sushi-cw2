package comp1206.sushi.common;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Stock {

    private Map<Ingredient, Number> ingredientStock;
    private Map<Dish, Number> dishStock;

    public Stock(){
        ingredientStock = new ConcurrentHashMap<>();
        dishStock = new ConcurrentHashMap<>();
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
