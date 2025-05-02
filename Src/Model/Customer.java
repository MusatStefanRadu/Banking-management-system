package Model;

public class Customer {
    private String firstName;
    private String lastName;
    private int age;
    private String personalIdentificationNumber; // CNP
    private String address;
    private Boolean isCompany;


    //---------------------------------------------------Constructor al clasei-----------------------------
    public Customer(String firstName, String lastName, int age, String personalIdentificationNumber, String address, Boolean isCompany)
    {
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.personalIdentificationNumber = personalIdentificationNumber;
        this.address = address;
        this.isCompany = isCompany;
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
    public String getPersonalIdentificationNumber()
    {

        return personalIdentificationNumber;
    }
    public int getAge()
    {
        return age;
    }
    public Boolean getIsCompany()
    {
        return isCompany;
    }

    // -----------------------------------------------setters---------------------------------------------
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
                "}";
    }
}
