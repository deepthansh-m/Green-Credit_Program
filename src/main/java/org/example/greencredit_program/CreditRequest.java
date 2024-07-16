package org.example.greencredit_program;

public class CreditRequest {
    private int id;
    private String companyName;
    private int amount;

    public CreditRequest(int id, String companyName, int amount) {
        this.id = id;
        this.companyName = companyName;
        this.amount = amount;
    }

    public int getId() {
        return id;
    }

    public String getCompanyName() {
        return companyName;
    }

    public int getAmount() {
        return amount;
    }
}
