/*
Author : Thomas BAUQUIN
*/

package hsoft;

import com.hsoft.codingtest.DataProvider;
import com.hsoft.codingtest.DataProviderFactory;
import com.hsoft.codingtest.MarketDataListener;
import com.hsoft.codingtest.PricingDataListener;

import org.apache.log4j.LogManager;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;

public class VwapTrigger {

  private static Logger logger = Logger.getLogger(VwapTrigger.class);
  private static ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
  private static HashMap<String, Double> fairValueMap = new HashMap<String, Double>();
  private static HashMap<String, Double> vwapMap = new HashMap<String, Double>();
  private static ArrayList<Transaction> operations = new ArrayList<Transaction>();
  public static void main(String[] args) {



    DataProvider provider = DataProviderFactory.getDataProvider();
    provider.addMarketDataListener(new MarketDataListener() {
      /*
      * compute the vwap of a product given the 5 last transactions
      */
      public void transactionOccured(String productId, long quantity, double price) {
        //creates a new transaction
        Transaction t = new Transaction(productId, quantity, price);
        Transaction [] lastTransactions = new Transaction[5];
        lock.writeLock().lock();
        operations.add(t);
        lock.writeLock().unlock();
        int nbOp = 0;
        lock.readLock().lock();
        //retreive the last 5 transactions
        for(int i=operations.size()-1; i>=0; i--){
          if(nbOp>=5){break;}
          if(operations.get(i).getId() == productId){
            lastTransactions[nbOp] = t;
            nbOp++;
          }
        }
        lock.readLock().unlock();
        double vwap = 0;
        double num = 0;
        double denum = 0;
        // compute the vwap
        for(int i=0; i<lastTransactions.length; i++){
          if(lastTransactions[i] == null){break;}
          num += (lastTransactions[i].getQuantity() * lastTransactions[i].getPrice()); 
          denum += lastTransactions[i].getQuantity();
        }
        vwap = num / denum;
        // update the vwap of the product
        lock.writeLock().lock();
        vwapMap.put(productId, vwap);
        lock.writeLock().unlock();
        // compare the vwap and the fair value
        lock.readLock().lock();
        Object fairValue = fairValueMap.get(productId);
        lock.readLock().unlock();
        if(fairValue != null && vwap > (double)fairValue && productId.compareTo("TEST_PRODUCT")==0){
          logger.info("VWAP(" + vwap + ") > FairValue(" + fairValue + ")");
        }
      }
    });
    provider.addPricingDataListener(new PricingDataListener() {
      /*
      * Update the fair value of a given product
      */
      public void fairValueChanged(String productId, double fairValue) {
        //update the fair value of the product
        lock.writeLock().lock();
        fairValueMap.put(productId, fairValue);
        lock.writeLock().unlock();
        lock.readLock().lock();
        Object vwap = vwapMap.get(productId);
        lock.readLock().unlock();
        // compare the vwap and the fair value
        if(vwap != null && (double)vwap > fairValue && productId.compareTo("TEST_PRODUCT")==0){
          logger.info("VWAP(" + vwap + ") > FairValue(" + fairValue + ")");
        }
      }
    });

    provider.listen();
    // When this method returns, the test is finished and you can check your results in the console
    LogManager.shutdown();
  }
}