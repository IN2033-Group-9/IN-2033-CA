import java.util.HashMap;
import java.util.Map;

// a lot of the class functions are used from from ord to speed things up and not recode things made before
public class CA_OnlineOrderAPI_Impl implements CA_OnlineOrderAPI {

    private SA_ORD_API ordApi;

    //Track order status
    private Map<String, String> orderStatus = new HashMap<>();

    public CA_OnlineOrderAPI_Impl(SA_ORD_API ordApi) {
        this.ordApi = ordApi;
    }

    /**
     * Process online order
     * basket format: 1:2,2:1 can be changed if it is too hard in front end to something different
     */
    @Override
    public void processOnlineOrder(String orderID, String basketOrder) {

        String[] items = basketOrder.split(",");

        int[] itemIDs = new int[items.length];
        int[] quantities = new int[items.length];

        for (int i = 0; i < items.length; i++) {
            String[] parts = items[i].split(":");
            itemIDs[i] = Integer.parseInt(parts[0]);
            quantities[i] = Integer.parseInt(parts[1]);
        }

        //Add items and submit using ord class
        ordApi.addItems(orderID, itemIDs, quantities);
        ordApi.submitOrder(orderID);

        //Update status for the order
        orderStatus.put(orderID, "PROCESSED");
    }

    /**
     * Check stock
     */
    @Override
    public int checkProductStock(String productID) {

        int id = Integer.parseInt(productID);

        //get readable stock map from the ord class
        Map<String, Integer> stockView = ordApi.viewStock();

        // match ID to name
        for (Map.Entry<Integer, String> entry : ordApi.getCatalogue().entrySet()) {
            if (entry.getKey() == id) {
                return stockView.getOrDefault(entry.getValue(), 0);//default to zero if it isnt in stock
            }
        }

        return 0;
    }

    /**
     * Search catalogue
     */
    @Override
    public String[] getMerchantCatalogue(String searchTerm) {

        return ordApi.getCatalogue().entrySet().stream()
                .filter(e -> e.getValue().toLowerCase().contains(searchTerm.toLowerCase()))
                .map(e -> e.getKey() + " - " + e.getValue())
                .toArray(String[]::new); // makse it so that the term something like para will return anything that is similar in this instance Paracetamol
    }

    /**
     * Pay by card checks for failure if none then status will be paid
     */
    @Override
    public boolean payByCard(String orderID, String cardNumber, String expiry) {

        if (!orderStatus.containsKey(orderID)) {
            System.out.println("Payment failed: order not found");
            return false;
        }

        if (cardNumber.length() < 8) {
            System.out.println("Payment failed: invalid card");
            return false;
        }

        orderStatus.put(orderID, "PAID");

        System.out.println("Payment successful for order: " + orderID);
        return true;
    }

    /**
     * Generate receipt only if it both real and already paid
     */
    @Override
    public String generateReceipt(String orderID) {

        if (!orderStatus.containsKey(orderID) ||
                !orderStatus.get(orderID).equals("PAID")) {

            return "Cannot generate receipt: payment not completed.";
        }

        Map<String, Integer> order = ordApi.viewOrder(orderID);// calls the view order to see in the receipt

        if (order == null) return "Order not found.";

        StringBuilder receipt = new StringBuilder();
        receipt.append("Receipt for Order: ").append(orderID).append("\n");

        int totalItems = 0;

        for (Map.Entry<String, Integer> entry : order.entrySet()) { // loop through items
            receipt.append(entry.getKey())
                    .append(" x ")
                    .append(entry.getValue())
                    .append("\n");

            totalItems += entry.getValue();
        }

        receipt.append("Total items: ").append(totalItems); // gets total items

        return receipt.toString();
    }

    /**
     * Track order status default is unknown
     */
    @Override
    public String getOrderStatus(String orderID) {

        return orderStatus.getOrDefault(orderID, "UNKNOWN");
    }

    /**
     * new order creation from ord class
     */
    public String createOrder() {
        String orderID = ordApi.newOrder();
        orderStatus.put(orderID, "CREATED");
        return orderID;
    }
}
