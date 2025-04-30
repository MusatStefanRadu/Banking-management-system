import java.util.ArrayList;
import java.util.List;

public class BankService {

    //collections of data
    private List<Transaction> transactions;
    private List<BankAccount> accounts;
    private List<BankCard> cards;
    private List<Customer> customers;

    //constructor
    public BankService() {
        this.transactions = new ArrayList<>();
        this.accounts = new ArrayList<>();
        this.cards = new ArrayList<>();
        this.customers = new ArrayList<>();
    }

    //methods
    public void addCustomer(Customer customer) {
        customers.add(customer);
        System.out.println("Customer added: " + customer.getFirstName() + " " + customer.getLastName());
    }
    public void addAccount(BankAccount account) {
        accounts.add(account);
        System.out.println("Account added for IBAN: " + account.getIban());
    }
    public void addCard(BankCard card) {
        cards.add(card);
        System.out.println("Card added for customer: " + card.getCardHolderName());
    }
    // metoda de transfer de bani între conturi
    public void transferMoney(BankAccount from, BankAccount to, double amount, String description) {
        if (amount <= 0) {
            System.out.println("Amount must be positive.");
            return;
        }
        if (from.getBalance() < amount) {
            System.out.println("Insufficient funds in source account.");
            return;
        }

        from.withdraw(amount);
        to.deposit(amount);

        Transaction transaction = new Transaction(from, to, amount, description);
        transactions.add(transaction);

        System.out.println("Transfer successful. Transaction ID: " + transaction.getId());
    }

    // returnează tranzacțiile pentru un cont dat
    public List<Transaction> getTransactionsForAccount(BankAccount account) {
        List<Transaction> result = new ArrayList<>();
        for (Transaction t : transactions) {
            if (t.getSourceAccount().equals(account) || t.getDestinationAccount().equals(account)) {
                result.add(t);
            }
        }
        return result;
    }

    // afișează conturile unui client
    public void showCustomerAccounts(Customer customer) {
        System.out.println("Accounts for customer: " + customer.getFirstName() + " " + customer.getLastName());
        for (BankAccount acc : accounts) {
            if (acc.getCustomer().equals(customer)) {
                System.out.println(acc);
            }
        }
    }
}
