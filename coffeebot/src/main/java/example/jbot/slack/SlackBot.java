package example.jbot.slack;

import me.ramswaroop.jbot.core.slack.Bot;
import me.ramswaroop.jbot.core.slack.Controller;
import me.ramswaroop.jbot.core.slack.EventType;
import me.ramswaroop.jbot.core.slack.models.Event;
import me.ramswaroop.jbot.core.slack.models.Message;
import com.sun.xml.internal.ws.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.text.DecimalFormat;
import java.util.*;
import java.util.List;


/**
 * A Slack CoffeeBot. Create multiple bots by just extending {@link Bot} class like this one.
 * @author zhengyu
 */
@Component
public class SlackBot extends Bot {

    private static final Logger logger = LoggerFactory.getLogger(SlackBot.class);

    // Help message
    private static final String MESSAGE_HELP = "Examples to execute command!\n\n"
            + "To view menu:\n" + "`menu` [starbucks]\n\n"
            + "To add your order:\n" + "`add` [drink]\n\n"
            + "To clear your orders:\n" + "`clear`\n\n"
            + "To view your orders:\n" + "`view`\n\n"
            + "To list collated orders:\n" + "`list`\n\n"
            + "To confirm your order:\n" + "`order`\n\n";

    // Error message
    private static final String MESSAGE_COMMAND_ERROR = "Sorry, I didn't understand that! Could you try again?\n\n" + "Type `help` to view the commands!";
    private static final String MESSAGE_NO_ORDERS = "You have no orders! Try adding one with 'add'!";

    // Orders saved in memory
    private static List<Order> orders = new ArrayList<>();

    // SBMenu
    private static final List<String> MENU_STARBUCKS = new ArrayList<>(Arrays.asList("starbucks"));
    private static Map<String, SBMenu> menuMap = new HashMap<>();
    private String menuName;

    /**
     * Slack token from application.properties file. Get your slack token
     * next <a href="https://my.slack.com/services/new/bot">creating a new bot</a>.
     */
    @Value("${slackBotToken}")
    private String slackToken;

    @Override
    public String getSlackToken() {
        return slackToken;
    }

    @Override
    public Bot getSlackBot() {
        return this;
    }



    /** receives a direct mention (@ontheball) */
    @Controller(events = {EventType.DIRECT_MENTION})
    public void onReceiveDM(WebSocketSession session, Event event) {
        reply(session, event, new Message("Whats up! I'm " + slackService.getCurrentUser().getName() + "\n\n" + "Type `help` to view the commands!"));
    }

    /** help command */
    @Controller(events = {EventType.MESSAGE})
    public void onReceiveHelp(WebSocketSession session, Event event) {
        String text = event.getText();
        if (getFirstWord(text).toLowerCase().equals("help")) {
            logger.info("User typed: {}", event.getText());
            reply(session, event, new Message(loadHelp()));
        }
    }

    /** menu command */
    @Controller(events = {EventType.MESSAGE})
    public void onReceiveMenu(WebSocketSession session, Event event) {
        String text = event.getText();
        if (getFirstWord(text).toLowerCase().equals("menu")) {
            logger.info("User typed: {}", event.getText());
            if(removeFirstWord(text).toLowerCase().equals("starbucks")) {
                reply(session, event, new Message(loadSBMenu()));
            }
        }
    }

    /** add command */
    @Controller(events = {EventType.MESSAGE})
    public void onReceiveAdd(WebSocketSession session, Event event) {
        String text = event.getText();
//        User user = event.getUser();
//        logger.info("User: {}", user.getName());

        if (getFirstWord(text).toLowerCase().equals("add")) {
            logger.info("Adding this order: {}", text);

            String userId = event.getUserId();
            String drink = removeFirstWord(text).toLowerCase();
//            User userName = event.getUser();
            if(userId.equals("U41RNK43C")) {
                String userName = "Hazel";
                viewAdded(event, session, userId, drink, userName);
            }
            if(userId.equals("U41RLMS9Y")) {
                String userName = "Zheng Yu";
                viewAdded(event, session, userId, drink, userName);
            }
            //String userName = "Zheng Yu";
        }
    }

    private void viewAdded(Event event, WebSocketSession session, String userId, String drink, String userName) {
        logger.info("storing: {}, {}, {}", userId, userName, drink);
        orders.add(new Order(userId, userName, drink));

        //load menu
        SBMenu menu;

        if (menuMap.containsKey(getMenuName())) {
            logger.info("Found menu in cache: {}", getMenuName());
            menu = menuMap.get(getMenuName());
        } else {
            logger.warn("Did not find menu in cache: {}", getMenuName());
            menu = new SBMenu();
            setMenuName("starbucks");
            menuMap.put(getMenuName(), menu);
        }

        /** Build response **/
        StringBuilder builder = new StringBuilder();

        // Notification that order was added
        builder.append(userName + " added 1 " + drink + "\n");
        builder.append("\n");

        // Load current orders
        Map<String, List<Order>> ordersByUser = loadItemsByUser();

        // For each user
        for (String username : ordersByUser.keySet()) {
            builder.append("`" + username + "` ordered:\n");
            double totalPriceForUser = 0;

            // For each unique item ordered by user
            Map<String, Integer> ordersByItem = loadOrdersByItem(ordersByUser.get(username));

            for (String orderName : ordersByItem.keySet()) {
                int numOrders = ordersByItem.get(orderName);

                // Look for price
                double totalPriceForItem = menu.getPrice(orderName) * numOrders;
                totalPriceForUser += totalPriceForItem;
                builder.append(buildItemString(orderName, numOrders, totalPriceForItem));
            }
            builder.append("*$" + new DecimalFormat("0.00").format(totalPriceForUser) + "*\n");
            builder.append("\n");
        }

        logger.debug("This is the add view: {}", builder.toString());

        reply(session, event, new Message(builder.toString()));
    }

    /** clear command */
    @Controller(events = {EventType.MESSAGE})
    public void onReceiveClear(WebSocketSession session, Event event) {
        String text = event.getText();
        if (getFirstWord(text).toLowerCase().equals("clear")) {
            logger.info("User typed: {}", event.getText());
            orders.clear();
            reply(session, event, new Message("Cleared your orders!"));
        }
    }

    /** view orders */
    @Controller(events = {EventType.MESSAGE})
    public void onReceiveView(WebSocketSession session, Event event) {
        String text = event.getText();
        if(getFirstWord(text).toLowerCase().equals("view")) {
            if (orders.isEmpty()) {
                reply(session, event, new Message(getNoOrdersMessage()));
            }

            StringBuilder builder = new StringBuilder();
            builder.append("These are your orders so far:\n\n");
            for (Order order : orders) {
                builder.append(order.getViewString());
                builder.append('\n');
            }
            reply(session, event, new Message(builder.toString()));
        }
    }

    /** List collated orders */
    @Controller(events = {EventType.MESSAGE})
    public void onReceiveList(WebSocketSession session, Event event) {
        String text = event.getText();
        if(getFirstWord(text).toLowerCase().equals("list")) {
            if (orders.isEmpty()) {
                reply(session, event, new Message(getNoOrdersMessage()));
            }

            double totalPayable = 0;

            Map<String, Integer> collated = loadOrdersByItem(orders);
            StringBuilder builder = new StringBuilder();
            builder.append("Are we ready to order?\n\n");
            for (String key : collated.keySet()) {
                int numItems = collated.get(key);
                String drinkName = StringUtils.capitalize(key);
                builder.append(numItems);
                builder.append(" x " + drinkName);

                if (numItems > 1 && !key.endsWith("s")) {
                    builder.append("s");
                }

                totalPayable += numItems * menuMap.get(getMenuName()).getPrice(key);
                builder.append("\n");
            }
            builder.append("\n *$" + new DecimalFormat("0.00").format(totalPayable) + "*\n\n");
            builder.append("If you are ready to order, type the `order` command!");
            reply(session, event, new Message(builder.toString())   );
        }
    }

    /** order command */
    @Controller(events = {EventType.MESSAGE})
    public void onReceiveOrder(WebSocketSession session, Event event) {
        String text = event.getText();
        if(getFirstWord(text).toLowerCase().equals("order")) {
            reply(session, event, new Message("Your order has been placed!"));
        }
    }

    private Map<String, List<Order>> loadItemsByUser() {
        Map<String, List<Order>> result = new HashMap<>();

        // Load orders into Map
        for (Order order : orders) {
            if (result.containsKey(order.getUserName())) {
                // Already exists
                result.get(order.getUserName()).add(order);
            } else {
                // Found first order by this user
                result.put(order.getUserName(), new ArrayList<>(Arrays.asList(order)));
            }
        }

        return result;
    }

    // Load into map for collate
    private Map<String, Integer> loadOrdersByItem(List<Order> orders) {
        Map<String, Integer> result = new HashMap<>();
        // Load orders into map (collated)
        for (Order order : orders) {
            if (result.containsKey(order.getName())) {
                // Already exists in map

                Integer count = result.get(order.getName()) + 1;
                result.replace(order.getName(), count);
            } else {
                // Seeing this order for the first time

                result.put(order.getName(), 1);
            }
        }
        return result;
    }

    // load help message
    private String loadHelp() {
        logger.info("Opening help message");
        return getHelpMessage();
    }

    // load starbucks menu
    private String loadSBMenu() {
        logger.info("Loading menu url");
        return loadSBMenuURL();
    }

    public String getMenuName() {
        return this.menuName;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }

    // load orders
    private String loadOrders() {
        logger.info("Loading orders");
        return "computing orders";
//        return collateOrders();
    }

    // get first word
    public static String getFirstWord(String str) {
        if (str.indexOf(" ") > -1) {
            return str.substring(0, str.indexOf(" "));
        }
        else {
            return str;
        }
    }

    private String buildItemString(String orderName, int numOrders, double totalPrice) {
        // If more than 1 order, append s to food name unless it ends with s
        if (numOrders > 1 && !orderName.endsWith("s")) {
            orderName += 's';
        }

        return numOrders + " x " + StringUtils.capitalize(orderName) + " -- $" + new DecimalFormat("0.00").format(totalPrice) + "\n";
    }

    public static String removeFirstWord(String str) {
        return str.substring(str.indexOf(" ") + 1);
    }

    private static String getCommandErrorMessage() { return MESSAGE_COMMAND_ERROR; }

    private static String getHelpMessage() { return MESSAGE_HELP; }

    private static String getNoOrdersMessage() { return MESSAGE_NO_ORDERS; }

    private static String loadSBMenuURL() { return "https://www.menuwithprice.com/menu/starbucks/"; }

//    private static String collateOrders() {
//        for(int i=0; i<)
//    }

}

















//    /***************  Conversation ******************/
//
//    /**
//     * starting point of the conversation (as it calls {@link Bot#startConversation(Event, String)} within it. Chain methods which will be invoked
//     * one after the other leading to a conversation. You can chain methods with {@link Controller#next()} by
//     * specifying the method name to chain with.
//     */
//    @Controller(events = {EventType.DIRECT_MESSAGE}, next = "confirmTiming")
//    //@Controller(pattern = "(setup meeting)", next = "confirmTiming")
//    public void setupMeeting(WebSocketSession session, Event event) {
//        if (getFirstWord(event.getText()).equals("meeting")) {
//            logger.info("User typed: {}", event.getText());
//            startConversation(event, "confirmTiming");   // start conversation
//            reply(session, event, new Message("Cool! At what time (ex. 15:30) do you want me to set up the meeting?"));
//        }
//    }
//
//    /**
//     * This method is chained with {@link SlackBot#setupMeeting(WebSocketSession, Event)}.
//     */
//    @Controller(events = {EventType.DIRECT_MESSAGE}, next = "askTimeForMeeting")
//    public void confirmTiming(WebSocketSession session, Event event) {
//        reply(session, event, new Message("Your meeting is set at " + event.getText() +
//                ". Would you like to repeat it tomorrow? (yes/no)"));
//        nextConversation(event);    // jump to next question in conversation
//    }
//
//    /**
//     * This method is chained with {@link SlackBot#confirmTiming(WebSocketSession, Event)}.
//     *
//     * @param session
//     * @param event
//     */
//    @Controller(events = {EventType.DIRECT_MESSAGE}, next = "askWhetherToRepeat")
//    public void askTimeForMeeting(WebSocketSession session, Event event) {
//        String reply = event.getText();
//        if (reply.toLowerCase().equals("yes") && reply.indexOf(" ") < 3) {
//            reply(session, event, new Message("Okay. Would you like me to set a reminder for you?"));
//            nextConversation(event);    // jump to next question in conversation
//        } else if (reply.toLowerCase().equals("no") && reply.indexOf(" ") < 2) {
//            reply(session, event, new Message("No problem. You can always schedule one with 'meeting' command."));
//            stopConversation(event);    // stop conversation only if user says no
//        }
//        else {
//            reply(session, event, new Message(getCommandErrorMessage()));
//        }
//    }
//
//    /**
//     * This method is chained with {@link SlackBot#askTimeForMeeting(WebSocketSession, Event)}.
//     *
//     * @param session
//     * @param event
//     */
//    @Controller
//    public void askWhetherToRepeat(WebSocketSession session, Event event) {
//        if (event.getText().contains("yes")) {
//            reply(session, event, new Message("Great! I will remind you tomorrow before the meeting."));
//        } else {
//            reply(session, event, new Message("Oh! my boss is smart enough to remind himself :)"));
//        }
//        stopConversation(event);    // stop conversation
//    }


// Not applicable code

//    /**
//     * Invoked when bot receives an event of type message with text satisfying
//     * the pattern {@code ([a-z ]{2})(\d+)([a-z ]{2})}. For example,
//     * messages like "ab12xy" or "ab2bc" etc will invoke this method.
//     *
//     * @param session
//     * @param event
//     */
//    @Controller(events = EventType.MESSAGE, pattern = "^([a-z ]{2})(\\d+)([a-z ]{2})$")
//    public void onReceiveMessage(WebSocketSession session, Event event, Matcher matcher) {
//        reply(session, event, new Message("First group: " + matcher.group(0) + "\n" +
//                "Second group: " + matcher.group(1) + "\n" +
//                "Third group: " + matcher.group(2) + "\n" +
//                "Fourth group: " + matcher.group(3)));
//    }

//    /**
//     * Invoked when an item is pinned in the channel.
//     *
//     * @param session
//     * @param event
//     */
//    @Controller(events = EventType.PIN_ADDED)
//    public void onPinAdded(WebSocketSession session, Event event) {
//        reply(session, event, new Message("Thanks for the pin! You can find all pinned items under channel details."));
//    }
//
//    /**
//     * Invoked when bot receives an event of type file shared.
//     * NOTE: You can't reply to this event as slack doesn't send
//     * a channel id for this event type. You can learn more about
//     * <a href="https://api.slack.com/events/file_shared">file_shared</a>
//     * event from Slack's Api documentation.
//     *
//     * @param session
//     * @param event
//     */
//    @Controller(events = EventType.FILE_SHARED)
//    public void onFileShared(WebSocketSession session, Event event) {
//        logger.info("File shared: {}", event);
//    }

//    @Controller(events = {EventType.DIRECT_MESSAGE})
//    public void onReceiveSet(WebSocketSession session, Event event) {
//        if (event.getText().contains("setup meeting")) {
//            logger.info("User typed: {}", event.getText());
//            reply(session, event, new Message("`No` is not in my dictionary!"));
//        }
//    }