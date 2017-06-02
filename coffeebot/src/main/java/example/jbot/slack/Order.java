package example.jbot.slack;

//import me.ramswaroop.jbot.core.slack.models.User;

/**
 * Created by zhengyu on 6/1/17.
 */
public class Order {

    private String userId, username, drinkName;
    private double price;

    public Order(String userId, String userName, String drinkName) {
        this.userId = userId;
        this.username = userName;
        this.drinkName = drinkName;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return username;
    }

    public String getName() {
        return drinkName;
    }

    // For view command
    public String getViewString() {
        return "1 x " + getName() + " by " + getUserName() + "";
    }

    public void setUserid(String newUserid) {
        this.userId = newUserid;
    }

    public void setUsername(String newUsername) {
        this.username = newUsername;
    }

    public void setName(String newDrinkName) {
        this.drinkName = newDrinkName;
    }

    public void setPrice(double newPrice) {
        this.price = newPrice;
    }
}
