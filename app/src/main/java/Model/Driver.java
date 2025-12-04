package Model;



public class Driver {
    private String firstName;
    private String lastName;
    private String email;
    private String carNumber;
    private String insuranceDate;
    //Empty constructor for firebase
    public Driver() {
    }
    public Driver(String firstName, String lastName, String email,
                  String carNumber, String insuranceDate) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.carNumber = carNumber;
        this.insuranceDate = insuranceDate;
    }
    // Getters and setters
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getCarNumber() { return carNumber; }
    public String getInsuranceDate() { return insuranceDate; }


    @Override
    public String toString() {
        return "Driver{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", carNumber='" + carNumber + '\'' +
                ", insuranceDate='" + insuranceDate + '\''+
                '}';
    }

}
