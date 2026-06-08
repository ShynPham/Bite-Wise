package model;

public record MealDraftItem(String foodName, NutritionInfo nutrition, double confidence, double servingMultiplier) {

    public MealDraftItem withFood(String newFoodName, NutritionInfo newNutrition) {
        return new MealDraftItem(newFoodName, newNutrition, confidence, servingMultiplier);
    }

    public MealDraftItem withServingMultiplier(double newServingMultiplier) {
        return new MealDraftItem(foodName, nutrition, confidence, newServingMultiplier);
    }

    public int caloriesWithServing() {
        if (nutrition == null) {
            return 0;
        }
        return (int) Math.round(nutrition.getNutritionAsInt() * servingMultiplier);
    }

    @Override
    public String toString() {
        return foodName + "  |  " + Math.round(confidence * 100) + "%  |  " + servingMultiplier + "x  |  "
                + caloriesWithServing() + " cal";
    }
}
