/*
Author : Thomas BAUQUIN
*/

package hsoft;

public class Transaction {
    
    private int transactionId;
    private String productId;
    private double price;
    private long quantity;

    public Transaction(String productId, long quantity, double price){
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
        this.transactionId++;
    }

    public String getId(){return productId;}
    public double getPrice(){return price;}
    public long getQuantity(){return quantity;}
    public int getTransactionId(){return transactionId;}

    

}
