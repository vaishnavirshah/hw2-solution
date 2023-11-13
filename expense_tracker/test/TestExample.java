// package test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;

import javax.swing.JFormattedTextField;
import javax.swing.JTextField;
import javax.swing.JOptionPane;

import java.text.ParseException;

import org.junit.Before;
import org.junit.Test;

import controller.ExpenseTrackerController;
import model.ExpenseTrackerModel;
import model.Transaction;
import model.Filter.AmountFilter;
import view.ExpenseTrackerView;
import model.Filter.CategoryFilter;


public class TestExample {
  
  private ExpenseTrackerModel model;
  private ExpenseTrackerView view;
  private ExpenseTrackerController controller;

  @Before
  public void setup() {
    model = new ExpenseTrackerModel();
    view = new ExpenseTrackerView();
    controller = new ExpenseTrackerController(model, view);
  }

    public double getTotalCost() {
        double totalCost = 0.0;
        List<Transaction> allTransactions = model.getTransactions(); // Using the model's getTransactions method
        for (Transaction transaction : allTransactions) {
            totalCost += transaction.getAmount();
        }
        return totalCost;
    }


    public void checkTransaction(double amount, String category, Transaction transaction) {
	assertEquals(amount, transaction.getAmount(), 0.01);
        assertEquals(category, transaction.getCategory());
        String transactionDateString = transaction.getTimestamp();
        Date transactionDate = null;
        try {
            transactionDate = Transaction.dateFormatter.parse(transactionDateString);
        }
        catch (ParseException pe) {
            pe.printStackTrace();
            transactionDate = null;
        }
        Date nowDate = new Date();
        assertNotNull(transactionDate);
        assertNotNull(nowDate);
        // They may differ by 60 ms
        assertTrue(nowDate.getTime() - transactionDate.getTime() < 60000);
    }


    @Test
    public void testAddTransaction() {
        // Pre-condition: List of transactions is empty
        assertEquals(0, model.getTransactions().size());
    
        // Perform the action: Add a transaction
        double amount = 50.0;
        String category = "food";
        assertTrue(controller.addTransaction(amount, category));
    
        // Post-condition: List of transactions contains only
	    //                 the added transaction	
        assertEquals(1, model.getTransactions().size());
    
        // Check the contents of the list
        Transaction firstTransaction = model.getTransactions().get(0);
        checkTransaction(amount, category, firstTransaction);
	
	    // Check the total amount
        assertEquals(amount, getTotalCost(), 0.01);
    }


    @Test
    public void testRemoveTransaction() {
        // Pre-condition: List of transactions is empty
        assertEquals(0, model.getTransactions().size());
    
        // Perform the action: Add and remove a transaction
        double amount = 50.0;
        String category = "food";
        Transaction addedTransaction = new Transaction(amount, category);
        model.addTransaction(addedTransaction);
    
        // Pre-condition: List of transactions contains only
	    //                the added transaction
        assertEquals(1, model.getTransactions().size());
        Transaction firstTransaction = model.getTransactions().get(0);
        checkTransaction(amount, category, firstTransaction);

	    assertEquals(amount, getTotalCost(), 0.01);
	
	    // Perform the action: Remove the transaction
        model.removeTransaction(addedTransaction);
    
        // Post-condition: List of transactions is empty
        List<Transaction> transactions = model.getTransactions();
        assertEquals(0, transactions.size());
    
        // Check the total cost after removing the transaction
        double totalCost = getTotalCost();
        assertEquals(0.00, totalCost, 0.01);
    }


    @Test
    public void addTransactionView() {
        //Pre condition: Initial rows in the table should be zero
        assertEquals(0, view.getTableModel().getRowCount());

        //Performing actions to add the transaction

        // Setting amount in the view using a JFormattedTextField
        JFormattedTextField amountField = new JFormattedTextField();
        amountField.setValue(50.00);
        view.setAmountField(amountField);

        // Setting category in the view using a JTextField
        JTextField categoryField = new JTextField("food");
        view.setCategoryField(categoryField);

        //Clicking "Add Transaction" button to add the transaction
        view.getAddTransactionBtn().doClick();
        controller.addTransaction(view.getAmountField(), view.getCategoryField());

        /***
         * Validation
         */

        //Row count should be 2, one for the input entry and one for the total count field
        view.refreshTable(model.getTransactions());
        assertEquals(2, view.getTableModel().getRowCount());

        //Checking if the entries are correct
        double amt =(double)view.getTableModel().getValueAt(0, 1);
        assertEquals(50.00, amt, 0.01);
        String ctgry =(String)view.getTableModel().getValueAt(0, 2);
        assertEquals("food", ctgry);

        //Checking if total amount is matching or not
        double totalAmt = (double)view.getTableModel().getValueAt(1, 3);
        assertEquals(50.00, totalAmt, 0.01);
    }

    @Test
    public void invalidInputHandling() {
        // Pre condition: Initial rows in the table should be zero
        assertEquals(0, model.getTransactions().size());

        //Adding a valid item first
        double amount = 50.0;
        String category = "food";
        assertTrue(controller.addTransaction(amount, category));
    
        // Performing the action to add an invalid transaction
        double amt = -1;
        String ctgry = "fruits";

        /***
         * Validation
         */
        
        //Transaction should not be added; controller should return false
        assertFalse(controller.addTransaction(amt, ctgry));
    
        //Number of transactions should remain 1 and not 2
        assertEquals(1, model.getTransactions().size());

        //Transaction list should show only the valid previous entries
        Transaction firstTransaction = model.getTransactions().get(0);
        checkTransaction(amount, category, firstTransaction);

        //Total Cost should be 50.00 only for the first transaction
        assertEquals(amount, getTotalCost(), 0.01);
        
    }


    @Test
    public void filterByAmount() {
        // Pre condition: Initial rows in the table should be zero
        assertEquals(0, model.getTransactions().size());

        //Adding multiple items to the table
        double[] amount = {10, 100, 10, 50};
        String[] category = {"food", "travel", "bills", "other"};

        for(int i=0; i<4;i++)
            assertTrue(controller.addTransaction(amount[i], category[i]));

        //Filtering by amount=10.0 We should see only 2 rows with amount as 10
        AmountFilter amountFilter = new AmountFilter(10.0);
        List<Transaction> filteredList = amountFilter.filter(model.getTransactions());
        controller.setFilter(amountFilter);
        controller.applyFilter();

        /***
         * Validation
         */

        //Number of Transactions in the filtered list should only be 2
        assertEquals(2,filteredList.size());

        //Amount should be 10.00 for both the transactions
        for(int i=0;i<2;i++){
            Transaction t = filteredList.get(i);
            assertEquals(10.00, t.getAmount(), 0.01);
        }
    }

    @Test
    public void filterByCategory() {
        // Pre condition: Initial rows in the table should be zero
        assertEquals(0, model.getTransactions().size());

        //Adding multiple items to the table
        double[] amount = {10, 100, 10, 50};
        String[] category = {"food", "food", "bills", "other"};

        for(int i=0; i<4;i++)
            assertTrue(controller.addTransaction(amount[i], category[i]));

        //Filtering by category=food We should see only 2 rows with category as food
        CategoryFilter categoryFilter = new CategoryFilter("food");
        List<Transaction> filteredList = categoryFilter.filter(model.getTransactions());
        controller.setFilter(categoryFilter);
        controller.applyFilter();

        /***
         * Validation
         */

        //Number of Transactions in the filtered list should only be 2
        assertEquals(2,filteredList.size());

        //Category should be "food" for both the transactionss
        for(int i=0;i<2;i++){
            Transaction t = filteredList.get(i);
            assertEquals("food", t.getCategory());
        }
    }

    @Test
    public void undoAllowed() {
        // Pre condition: Initial rows in the table should be zero
        assertEquals(0, model.getTransactions().size());

        //Adding a valid item first
        double amount = 50.0;
        String category = "food";
        assertTrue(controller.addTransaction(amount, category));
        //Number of Transactions should be 1
        assertEquals(1, model.getTransactions().size());

        //Calculating Total cost
        Transaction firstTransaction = model.getTransactions().get(0);
        checkTransaction(amount, category, firstTransaction);
	    
        //Checking the total amount
        assertEquals(amount, getTotalCost(), 0.01);

        //Performing Undo
        Transaction t = model.getTransactions().get(0);
        model.removeTransaction(t);

        /***
         * Validation
         */

        //Number of transactions should be 0
        assertEquals(0, model.getTransactions().size());

	    //Total Cost should be 0
        assertEquals(0.00, getTotalCost(), 0.01);
    }

    @Test
    public void undoDisallowed() {
        // Pre condition: Initial rows in the table should be zero
        assertEquals(0, model.getTransactions().size());
        assertEquals(0.00, getTotalCost(), 0.01);

        //Performing Undo expectation is to catch the exception
        try {
            Transaction t = model.getTransactions().get(0);
            model.removeTransaction(t);
        }
        catch (IndexOutOfBoundsException e){
            System.out.println("Caught an ArrayIndexOutOfBoundsException:");
            System.out.println("Exception message: " + e.getMessage());
            System.out.println("Exception class: " + e.getClass().getName());
            e.printStackTrace();
        }

        /***
         * Validation
         */

        //Number of transactions should be 0
        assertEquals(0, model.getTransactions().size());

	    //Total amount should be 0
        assertEquals(0.00, getTotalCost(), 0.01);
    }

}