import java.util.List;

public class StatementGenerator {

    public static void generate(BankAccount account, List<Transaction> transactions) {
        System.out.println("----- Statement for Account: " + account.getIban() + " -----");
        System.out.println("Account Holder: " + account.getCustomer().getFirstName() + " " + account.getCustomer().getLastName());
        System.out.println("Currency: " + account.getCurrency());
        System.out.println("Current Balance: " + account.getBalance());
        System.out.println("------------------------------------------------------------");
        System.out.println("Date\t\t\tType\tAmount\tOther IBAN\tDescription");

        for (Transaction t : transactions) {
            String type;
            String otherIban;

            if (t.getSourceAccount().equals(account)) {
                type = "OUT";
                otherIban = t.getDestinationAccount().getIban();
            } else if (t.getDestinationAccount().equals(account)) {
                type = "IN";
                otherIban = t.getSourceAccount().getIban();
            } else {
                continue;
            }

            System.out.printf("%s\t%s\t%.2f\t%s\t%s\n",
                    t.getTransactionDate(),
                    type,
                    t.getAmount(),
                    otherIban,
                    t.getDescription()
            );
        }

        System.out.println("------------------------------------------------------------\n");
    }
}
