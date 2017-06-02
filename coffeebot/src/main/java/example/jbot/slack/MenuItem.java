package example.jbot.slack;

/**
 * Created by zhengyu on 6/1/17.
 */
public class MenuItem {
    private String drinkName;
    private double price;

    public MenuItem(String foodName, double price) {
        this.drinkName = foodName;
        this.price = price;
    }

    public String getDrinkName() {
        return drinkName;
    }

    public void setDrinkName(String drinkName) {
        this.drinkName = drinkName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
