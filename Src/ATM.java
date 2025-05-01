public class ATM {

    private BankCard insertedCard;
    private boolean authenticated;

    public void insertCard(BankCard card) {
        this.insertedCard = card;
        this.authenticated = false;
        System.out.println("Card inserted: " + card.getCardNumber());
    }

    public boolean authenticate(String pin) {
        if (insertedCard == null) {
            System.out.println("No card inserted.");
            return false;
        }

        if (insertedCard.getPin() == null) {
            if (insertedCard.isActive()) {
                System.out.println("Authentication bypassed: this card does not require a PIN.");
                authenticated = true;
                return true;
            } else {
                System.out.println("Card is not active.");
                return false;
            }
        }

        if (CardAuthenticator.authenticate(insertedCard, pin)) {
            authenticated = true;
            return true;
        }

        return false;
    }

    // consultare sold
    public void checkBalance() {
        if (!authenticated) {
            System.out.println("Not authenticated.");
            return;
        }

        try {
            BankAccount account = insertedCard.getLinkedAccount();
            System.out.println("Current balance: " + account.getBalance());
        } catch (UnsupportedOperationException e) {
            System.out.println("This card does not have a linked account.");
        }
    }

    // retragere bani
    public void withdraw(double amount) {
        if (!authenticated) {
            System.out.println("Not authenticated.");
            return;
        }

        try {
            BankAccount account = insertedCard.getLinkedAccount();
            account.withdraw(amount);
        } catch (UnsupportedOperationException e) {
            System.out.println("Cannot withdraw: this card is not linked to a bank account.");
        }
    }

    // scoatere card
    public void ejectCard() {
        if (insertedCard != null) {
            System.out.println("Card ejected: " + insertedCard.getCardNumber());
        }
        insertedCard = null;
        authenticated = false;
    }
}
