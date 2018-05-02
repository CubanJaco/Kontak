package com.jaco.contact;

/**
 * Created by osvel on 6/29/16.
 */
public class PhoneNumber {

    private String phone;

    public PhoneNumber(String phone) {
        if (phone == null)
            this.phone = "";
        else if (phone.startsWith("0"))
            this.phone = phone.substring(1);
        else
            this.phone = phone;
    }

    public static boolean isValidNumber(String phone){

        if (phone == null)
            return false;

        if (phone.length() == 0)
            return false;

        if (phone.startsWith("+") || phone.startsWith("*")){
            return allNumbers(phone.substring(1));
        }
        return allNumbers(phone);

    }

    public static boolean allNumbers(String cadena){

        for (int i = 0; i < cadena.length(); i++) {
            try {
                Integer.parseInt(""+cadena.charAt(i));
            }
            catch (NumberFormatException e){
                return false;
            }
        }

        return true;
    }

    public boolean is99(){
        return phone.startsWith("9953") && phone.endsWith("99");
    }

    public boolean isFree(){
        return phone.startsWith("+535");
    }

    public String getNumber(){

        if (phone == null)
            return null;

        if (phone.startsWith("53") && phone.length() == 10)
            return phone.substring(2);

        if (phone.startsWith("+53"))
            return phone.substring(3);

        if (phone.startsWith("9953"))
            return phone.substring(4, phone.length() - 2);

        if (phone.startsWith("*99"))
            return phone.substring(3);

        return phone;
    }

    public String getFixNumber(){
        if (isMovil())
            return "";//si es movil retorno cadena vacia
        if (getProvinceCode() == -1)
            return getNumber();//si esta en formato sin provincia retorno el mismo numero
        if (getProvinceCode() == 7)
            return getNumber().substring(1);//si la provincia es habana quito solo el primer caracter
        return getNumber().substring(2);//por defecto quito dos caracter
    }

    public boolean isMovil(){
        return (phone.startsWith("99") && phone.endsWith("99")) || phone.startsWith("+535")
                || phone.startsWith("535") || (phone.startsWith("5") && phone.length() == 8 );
    }

    public int getProvinceCode(){

        String cleanPhone = getNumber();
        if (isMovil() || getNumber().length() < 8){//es movil o no tiene el codigo de la provincia
            return -1;
        }
        if (cleanPhone.startsWith("7")){ //es de la habana
            return 7;
        }
        return Integer.parseInt(cleanPhone.substring(0, 2));

    }

}
