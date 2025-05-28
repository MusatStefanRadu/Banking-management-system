package Model;

import java.time.LocalDateTime;

public class Customer {
    private int id;
    private String firstName;
    private String lastName;
    private int age;
    private String personalIdentificationNumber; // CNP
    private String address;
    private Boolean isCompany;
    private LocalDateTime registrationDate; // Data înregistrării în sistem



    //---------------------------------------------------Constructor al clasei-----------------------------

    public Customer(String firstName, String lastName, int age, String personalIdentificationNumber, String address, Boolean isCompany, LocalDateTime registrationDate) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.personalIdentificationNumber = personalIdentificationNumber;
        this.address = address;
        this.isCompany = isCompany;
        this.registrationDate = registrationDate;
    }


    public Customer(int id, String firstName, String lastName, int age, String personalIdentificationNumber, String address, Boolean isCompany, LocalDateTime registrationDate) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.personalIdentificationNumber = personalIdentificationNumber;
        this.address = address;
        this.isCompany = isCompany;
        this.registrationDate = registrationDate;
    }

    //-------------------------------------------------getters--------------------------------------------
    public String getFirstName()
    {
        return firstName;
    }
    public String getLastName()
    {
        return lastName;
    }
    public String getAddress()
    {
        return address;
    }
    public String getPersonalIdentificationNumber() { return personalIdentificationNumber; }
    public int getId() { return id; }
    public int getAge()
    {
        return age;
    }
    public Boolean getIsCompany()
    {
        return isCompany;
    }
    public LocalDateTime getRegistrationDate() { return registrationDate; }

    // -----------------------------------------------setters---------------------------------------------
    public void setAge(int age) {
        if (age < 0) {
            throw new IllegalArgumentException("Age cannot be negative");
        }
        this.age = age;
    }
    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
    }
    public void setLastName(String lastName)
    {
        this.lastName = lastName;
    }
    public void setAddress(String address)
    {
        this.address = address;
    }
    public void setId(int id) { this.id = id; }

    //----------------------------------------------------toString method------------------------------------
    @Override
    public String toString()
    {
        return "Model.Customer{" + "\n" +
                "firstName : " + firstName + "\n" +
                "lastName : " + lastName + "\n" +
                "personalIdentificationNumber : " + personalIdentificationNumber + "\n" +
                "address : " + address + "\n" +
                "age : " + age + "\n" +
                "isCompany : " + isCompany + "\n" +
                ", registrationDate=" + registrationDate +
                "}";
    }
}
