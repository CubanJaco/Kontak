package com.jaco.contact;

/**
 * Created by osvel on 7/16/16.
 */
public class ContactDetail {

    private String name;
    private String number;
    private String phone_id;
    private String contact_id;

    public ContactDetail(String contact_id, String name, String number, String phone_id) {
        this.name = name;
        this.number = number;
        this.phone_id = phone_id;
        this.contact_id = contact_id;
    }

    public ContactDetail(String name, String number) {
        this.name = name;
        this.number = number;
        contact_id = "";
        phone_id = "";
    }

    public String getPhoneId() {
        return phone_id;
    }

    public void setPhoneId(String phone_id) {
        this.phone_id = phone_id;
    }

    public String getContactId() {
        return contact_id;
    }

    public void setContactId(String contact_id) {
        this.contact_id = contact_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {

        String realNumber = number.startsWith("+") ? "+" : "";

        for (char c : number.toCharArray()) {
            try {
                int num = Integer.parseInt(""+c);
                realNumber += num;
            }
            catch (NumberFormatException e){
                e.printStackTrace();
            }
        }

        return realNumber;
    }

    public void setNumber(String number) {
        this.number = number;
    }

}
