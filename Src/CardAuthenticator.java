public class CardAuthenticator {

    public static boolean authenticate(BankCard card, String enteredPin) {
        if (!card.isActive()) {
            System.out.println("Card is not active.");
            return false;
        }

        if (card.getPin() == null) {
            System.out.println("This card does not require PIN.");
            return true;
        }

        if (!card.getPin().equals(enteredPin)) {
            System.out.println("Incorrect PIN.");
            return false;
        }

        System.out.println("Authentication successful.");
        return true;
    }
}
