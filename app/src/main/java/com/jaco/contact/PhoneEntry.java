package com.jaco.contact;

import android.content.Context;

import com.jaco.contact.preferences.mSharedPreferences;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by osvel on 6/27/16.
 */
public class PhoneEntry implements Serializable {

    private PhoneType type;
    private String address;
    private String identification;
    private String province;
    private String name;
    private String number;

    public PhoneEntry() {
        this.type = PhoneType.FIX;
        this.name = "";
        this.number = "";
        this.address = "";
        this.province = "";
        this.identification = "";
    }

    public PhoneEntry(String _number){
        this.type = PhoneType.UNKNOWN;
        this.name = "";
        this.number = _number;
        this.address = "";
        this.province = "";
        this.identification = "";
    }

    public PhoneEntry(PhoneType _type, String _name, String _number, String _address, String _location, String _identification) {
        this.type = _type;
        this.name = _name;
        this.number = _number;
        this.address = _address;
        this.province = _location;
        this.identification = _identification;
    }

    public PhoneType getType() {
        return type;
    }

    public void setType(PhoneType type) {
        this.type = type;
    }

    public void setNumber(String _number) {
        this.number = _number;
    }

    public String getNumber() {
        if (type == PhoneType.MOVIL)
            return this.number;

        return this.province+this.number;
    }

    public void setName(String _name) {
        this.name = _name;
    }

    public String getName() {
        return this.name;
    }

    public void setAddress(String _address) {
        this.address = _address;
    }

    public String getAddress() {
        return this.address;
    }

    public void setProvince(String _location) {
        if (_location.equals("74")) {
            this.province = "47";
        }
        else{
            this.province = _location;
        }
    }

    public int getProvinceNumber() {
        try {
            return Integer.parseInt(this.province);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public String getProvinceName(Context context){

        boolean alternative = mSharedPreferences.isAlternativeDatabase(context);

        switch (getProvinceNumber()){
            case 7:{
                return context.getResources().getString(R.string.habana);
            }
            case 21:{
                return context.getResources().getString(R.string.guantanamo);
            }
            case 22:{
                return context.getResources().getString(R.string.santiago);
            }
            case 23:{
                return context.getResources().getString(R.string.granma);
            }
            case 24:{
                return context.getResources().getString(R.string.holguin);
            }
            case 31:{
                return context.getResources().getString(R.string.tunas);
            }
            case 32:{
                return context.getResources().getString(R.string.camaguey);
            }
            case 33:{
                return context.getResources().getString(R.string.ciego);
            }
            case 41:{
                return context.getResources().getString(R.string.sancti_spiritus);
            }
            case 42:{
                return context.getResources().getString(R.string.villa_clara);
            }
            case 43:{
                return context.getResources().getString(R.string.cienfuegos);
            }
            case 45:{
                return context.getResources().getString(R.string.matanzas);
            }
            case 46:{
                return context.getResources().getString(R.string.isla);
            }
            case 47:{
                if (alternative && type == PhoneType.FIX)
                    return context.getResources().getString(R.string.artemisa);
                else
                    return context.getResources().getString(R.string.mayabeque_artemisa);
            }
            case 48:{
                return context.getResources().getString(R.string.pinar);
            }
            case 74:{
                if (alternative)
                    return context.getResources().getString(R.string.mayabeque);
                else
                    return "";
            }
            default:
                return "";
        }
    }

    public void setIdentification(String _identification) {
        this.identification = _identification;
    }

    public String getIdentification() {
        return this.identification;
    }

    public boolean isValidIdentification(){

        if (identification.length() == 11 && PhoneNumber.allNumbers(identification))
            return true;
        return false;

    }

    public String getBirthDateString() {

        if (type != PhoneType.MOVIL || !isValidIdentification())
            return null;

        if (PhoneNumber.allNumbers(identification) && identification.length() == 11){

            Calendar birthDate = getBirthDate();
            DateFormat df = new SimpleDateFormat("EEEE LLL dd, yyyy");

            return df.format(birthDate.getTime());
        }

        return null;

    }

    public GregorianCalendar getBirthDate(){
        if (type != PhoneType.MOVIL || !isValidIdentification())
            return null;

        int year, month, day;
        if (PhoneNumber.allNumbers(identification) && identification.length() == 11){
            year = Integer.parseInt(identification.substring(0,2));
            month = Integer.parseInt(identification.substring(2,4));
            day = Integer.parseInt(identification.substring(4,6));

            year = year < 20 ? 2000+year : 1900+year;

            return new GregorianCalendar(year, month-1, day, 0, 0, 0);
        }

        return null;
    }

    public int getAge() {

        if (type != PhoneType.MOVIL || !isValidIdentification())
            return -1;

        if (PhoneNumber.allNumbers(identification) && identification.length() == 11){
            Calendar realBirthday = getBirthDate();

            GregorianCalendar current = new GregorianCalendar();
            current.set(Calendar.HOUR_OF_DAY, 0);
            current.set(Calendar.MINUTE, 0);
            current.set(Calendar.SECOND, 0);

            realBirthday.setTimeInMillis(current.getTimeInMillis() - realBirthday.getTimeInMillis());

            return realBirthday.get(Calendar.YEAR)-1970;
        }

        return -1;

    }
}
