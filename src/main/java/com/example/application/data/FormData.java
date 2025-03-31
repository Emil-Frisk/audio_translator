package com.example.application.data;

 public class FormData {
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private Boolean canCall;
    private String password;
    private String repeatPassword;
    public String getFirstName() {
        return firstName;
    }

    public FormData() {
        
    }

    public FormData(String firstName, String lastName, String email, String phoneNumber, Boolean canCall,
        String password, String repeatPassword) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.canCall = canCall;
        this.password = password;
        this.repeatPassword = repeatPassword;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    public Boolean getCanCall() {
        return canCall;
    }
    public void setCanCall(Boolean canCall) {
        this.canCall = canCall;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getRepeatPassword() {
        return repeatPassword;
    }
    public void setRepeatPassword(String repeatPassword) {
        this.repeatPassword = repeatPassword;
    }
}
