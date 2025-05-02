import java.util.Scanner;
import java.time.LocalDate;

public class AdminMenu {

    public static void run(BankService bankService, Scanner scanner) {

        boolean inAdminMenu = true;

        while (inAdminMenu) {
            System.out.println("\n--- Administrative Menu ---");
            System.out.println("1. Create new customer");
            System.out.println("2. Create new account");
            System.out.println("3. Create new card");
            System.out.println("4. Show all customers");
            System.out.println("5. Back to main menu");
            System.out.print("Choose an option: ");

            String option = scanner.nextLine();
            switch (option) {
                case "1":
                    createCustomer(bankService, scanner);
                    break;
                case "2":
                    createAccount(bankService, scanner);
                    break;
                case "3":
                    createCard(bankService, scanner);
                    break;
                case "4":
                    showAllCustomers(bankService);
                    break;
                case "5":
                    inAdminMenu = false;
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    public static String generateIban() {
        String countryCode = "RO";
        String bankCode = "BANK";
        int uniqueNumber = (int)(Math.random() * 900000) + 100000; // 6 cifre
        return countryCode + bankCode + uniqueNumber;
    }


    public static void createCustomer(BankService bankService, Scanner scanner) {
        System.out.print("First name: ");
        String firstName = scanner.nextLine();

        System.out.print("Last name: ");
        String lastName = scanner.nextLine();

        System.out.print("Age: ");
        int age = Integer.parseInt(scanner.nextLine());

        System.out.print("CNP (Personal ID Number): ");
        String cnp = scanner.nextLine();

        System.out.print("Address: ");
        String address = scanner.nextLine();

        System.out.print("Is this a company? (yes/no): ");
        String companyInput = scanner.nextLine().toLowerCase();
        boolean isCompany = companyInput.equals("yes");

        Customer newCustomer = new Customer(firstName, lastName, age, cnp, address, isCompany);
        bankService.addCustomer(newCustomer);
        System.out.println("New customer created.");
    }

    public static void createAccount(BankService bankService, Scanner scanner) {
        System.out.print("Enter customer CNP: ");
        String cnp = scanner.nextLine();

        Customer customer = bankService.findCustomerByCNP(cnp);
        if (customer == null) {
            System.out.println("Customer not found.");
            return;
        }

        System.out.print("Account type: (current/savings/business/investment): ");
        String type = scanner.nextLine().toLowerCase();

        String iban = generateIban();
        System.out.println("Generated IBAN: " + iban);

        System.out.print("Initial balance: ");
        double balance = Double.parseDouble(scanner.nextLine());

        System.out.println("Currency: ");
        String currency = scanner.nextLine().toLowerCase();

        BankAccount account = null;
        switch (type) {
            case "current":
                System.out.print("Overdraft limit: ");
                double overdraft = Double.parseDouble(scanner.nextLine());
                account = new CurrentAccount(iban, balance, customer, currency, overdraft);
                break;

            case "savings":
                System.out.print("Minimum balance: ");
                double minBalance = Double.parseDouble(scanner.nextLine());
                System.out.print("Interest rate: ");
                double interest = Double.parseDouble(scanner.nextLine());
                account = new SavingsAccount(iban, balance, customer, currency, minBalance, interest);
                break;

            case "business":
                if (!customer.getIsCompany()) {
                    System.out.println("Customer is not a company. Cannot create business account.");
                    return;
                }
                System.out.print("Company name: ");
                String companyName = scanner.nextLine();
                System.out.print("Registration number: ");
                String regNr = scanner.nextLine();
                System.out.print("VAT number: ");
                String vat = scanner.nextLine();
                account = new BusinessAccount(iban, balance, customer, currency, companyName, regNr, vat);
                break;

            case "investment":
                System.out.print("Portfolio value: ");
                double portfolio = Double.parseDouble(scanner.nextLine());
                System.out.print("Is investment locked? (yes/no): ");
                boolean locked = scanner.nextLine().equalsIgnoreCase("yes");
                account = new InvestmentAccount(iban, balance, customer, currency, portfolio, locked);
                break;

            default:
                System.out.println("Invalid account type.");
                return;
        }
        bankService.addAccount(account);
        System.out.println("Account created successfully.");
    }

    public static void createCard(BankService bankService, Scanner scanner) {
        System.out.print("Enter customer CNP: ");
        String cnp = scanner.nextLine();

        Customer customer = bankService.findCustomerByCNP(cnp);
        if(customer == null) {
            System.out.println("Customer not found.");
            return;
        }

        System.out.print("Enter IBAN for the account to link: ");
        String iban = scanner.nextLine();

        BankAccount account = bankService.findBankAccountByIban(iban);
        if (account == null) {
            System.out.println("Account not found.");
            return;
        }

        System.out.print("Card type (debit/credit/virtual/prepaid): ");
        String type = scanner.nextLine().toLowerCase();

        String cardNumber = "4" + (int)(Math.random() * 1_0000_0000); // simulare card
        String cvv = String.valueOf((int)(Math.random() * 900) + 100);
        LocalDate expiry = LocalDate.now().plusYears(4);
        String pin = null;

        BankCard card = null;

        switch (type) {
            case "debit":
                System.out.print("Enter PIN: ");
                pin = scanner.nextLine();
                card = new DebitCard(cardNumber, customer.getFirstName() + " " + customer.getLastName(), expiry, account, cvv, pin);
                break;
            case "credit":
                System.out.print("Enter PIN: ");
                pin = scanner.nextLine();
                System.out.print("Credit limit: ");
                double creditLimit = Double.parseDouble(scanner.nextLine());
                card = new CreditCard(cardNumber, customer.getFirstName() + " " + customer.getLastName(), expiry, account, cvv, pin, creditLimit);
                break;
            case "virtual":
                System.out.print("Usage limit (number of uses): ");
                int usageLimit = Integer.parseInt(scanner.nextLine());
                card = new VirtualCard(cardNumber, customer.getFirstName() + " " + customer.getLastName(), expiry, account, cvv, usageLimit);
                break;
            case "prepaid":
                System.out.print("Initial balance: ");
                double balance = Double.parseDouble(scanner.nextLine());
                System.out.print("Enter PIN: ");
                pin = scanner.nextLine();
                card = new PrepaidCard(cardNumber, customer.getFirstName() + " " + customer.getLastName(), expiry, cvv, pin, balance);
                break;
            default:
                System.out.println("Invalid card type.");
                return;
        }
        bankService.addCard(card);
        System.out.println("Card created successfully: " + cardNumber);
    }

    public static void showAllCustomers(BankService bankService) {
        System.out.println("\n--- All Registered Customers ---");

        if (bankService.getCustomers().isEmpty()) {
            System.out.println("No customers registered.");
            return;
        }

        for (Customer c : bankService.getCustomers()) {
            System.out.println(c);
            System.out.println("-----------------------------");
        }
    }
}
