package example.jbot.slack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhengyu on 6/1/17.
 */
public class SBMenu {
    private static final Logger LOGGER = LoggerFactory.getLogger(SBMenu.class);
    private Map<String, MenuItem> menuList;

    public SBMenu() {
        menuList = new HashMap<>();
        loadMenu();
    }

    public Map<String, MenuItem> getMenuList() {
        return menuList;
    }

    public void setMenuList(Map<String, MenuItem> menuList) {
        this.menuList = menuList;
    }

    public void loadMenu() {
        LOGGER.info("Loaded menu in Starbucks Menu.");

        MenuItem drink1 = new MenuItem("caffe latte", 2.95);
        MenuItem drink2 = new MenuItem("caffe mocha", 3.45);
        MenuItem drink3 = new MenuItem("white chocolate mocha", 3.75);
        MenuItem drink4 = new MenuItem("freshly brewed coffee", 1.85);
        MenuItem drink5 = new MenuItem("iced coffee", 2.25);
        MenuItem drink6 = new MenuItem("cinnamon dolce latte", 3.65);
        MenuItem drink7 = new MenuItem("skinny vanilla latte", 3.45);
        MenuItem drink8 = new MenuItem("caramel macchiato", 3.75);

        menuList.put(drink1.getDrinkName(), drink1);
        menuList.put(drink2.getDrinkName(), drink2);
        menuList.put(drink3.getDrinkName(), drink3);
        menuList.put(drink4.getDrinkName(), drink4);
        menuList.put(drink5.getDrinkName(), drink5);
        menuList.put(drink6.getDrinkName(), drink6);
        menuList.put(drink7.getDrinkName(), drink7);
        menuList.put(drink8.getDrinkName(), drink7);

    }

    public double getPrice(String drinkName) {
        LOGGER.debug("Trying to get the price of {}", drinkName.toLowerCase());

        Map<String, MenuItem> list = getMenuList();
        return list.get(drinkName.toLowerCase()).getPrice();
    }
}
