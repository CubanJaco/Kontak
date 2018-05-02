package com.jaco.contact;

/**
 * Created by osvel on 7/26/16.
 */
public class AdvancedSearch {

    public static final int NUMBER_INDEX = 14;
    public static final int NAME_INDEX = 15;
    public static final int IDENTIFICATION_INDEX = 16;
    public static final int PROVINCE_INDEX = 17;

    public static final int NUMBER_EXACTLY_INDEX = 1;
    public static final int NUMBER_START_INDEX = 2;
    public static final int NUMBER_CONTAINS_INDEX = 3;
    public static final int NUMBER_END_INDEX = 4;
    public static final int NAME_EXACTLY_INDEX = 5;
    public static final int NAME_START_INDEX = 6;
    public static final int NAME_CONTAINS_INDEX = 7;
    public static final int NAME_END_INDEX = 8;
    public static final int IDENTIFICATION_EXACTLY_INDEX = 9;
    public static final int IDENTIFICATION_START_INDEX = 10;
    public static final int IDENTIFICATION_CONTAINS_INDEX = 11;
    public static final int IDENTIFICATION_END_INDEX = 12;
    public static final int PROVINCE_CODE = 13;

    private String number_exactly;
    private String number_start;
    private String number_contains;
    private String number_end;
    private String name_exactly;
    private String name_start;
    private String name_contains;
    private String name_end;
    private String identification_exactly;
    private String identification_start;
    private String identification_contains;
    private String identification_end;
    private int province_code;
    private PhoneType type;

    public AdvancedSearch() {
        number_exactly = "";
        number_start = "";
        number_contains = "";
        number_end = "";
        name_exactly = "";
        name_start = "";
        name_contains = "";
        name_end = "";
        identification_exactly = "";
        identification_start = "";
        identification_contains = "";
        identification_end = "";
        province_code = -1;
        type = null;
    }

    public void resetAllValues(){
        resetValues(NUMBER_INDEX);
        resetValues(NAME_INDEX);
        resetValues(IDENTIFICATION_INDEX);
        resetValues(PROVINCE_INDEX);
        type = null;
    }

    public PhoneType getType() {
        return type;
    }

    public void setType(PhoneType type) {
        this.type = type;
    }

    public boolean hasValues(){

        return (getString(NUMBER_INDEX)
                + getString(NAME_INDEX)
                + getString(IDENTIFICATION_INDEX)
                + getString(PROVINCE_INDEX))
                .length() != 0;

    }

    public void resetValues(int index){

        switch (index) {
            case NUMBER_INDEX: {
                number_exactly = "";
                number_start = "";
                number_contains = "";
                number_end = "";
                break;
            }
            case NAME_INDEX: {
                name_exactly = "";
                name_start = "";
                name_contains = "";
                name_end = "";
                break;
            }
            case IDENTIFICATION_INDEX: {
                identification_exactly = "";
                identification_start = "";
                identification_contains = "";
                identification_end = "";
                break;
            }
            case PROVINCE_INDEX: {
                province_code = -1;
            }
        }

    }

    public void set(int index, int value){

        switch (index){
            case PROVINCE_CODE: {
                province_code = value;
                break;
            }
        }

    }

    public void set(int index, String value){

        switch (index){
            case NUMBER_EXACTLY_INDEX: {
                number_exactly = value;
                break;
            }
            case NUMBER_START_INDEX: {
                number_start = value;
                break;
            }
            case NUMBER_CONTAINS_INDEX: {
                number_contains = value;
                break;
            }
            case NUMBER_END_INDEX: {
                number_end = value;
                break;
            }
            case NAME_EXACTLY_INDEX: {
                name_exactly = value;
                break;
            }
            case NAME_START_INDEX: {
                name_start = value;
                break;
            }
            case NAME_CONTAINS_INDEX: {
                name_contains = value;
                break;
            }
            case NAME_END_INDEX: {
                name_end = value;
                break;
            }
            case IDENTIFICATION_EXACTLY_INDEX: {
                identification_exactly = value;
                break;
            }
            case IDENTIFICATION_START_INDEX: {
                identification_start = value;
                break;
            }
            case IDENTIFICATION_CONTAINS_INDEX: {
                identification_contains = value;
                break;
            }
            case IDENTIFICATION_END_INDEX: {
                identification_end = value;
                break;
            }
        }

    }

    public String get(int index){

        switch (index){
            case NUMBER_EXACTLY_INDEX: {
                return number_exactly;
            }
            case NUMBER_START_INDEX: {
                return number_start;
            }
            case NUMBER_CONTAINS_INDEX: {
                return number_contains;
            }
            case NUMBER_END_INDEX: {
                return number_end;
            }
            case NAME_EXACTLY_INDEX: {
                return name_exactly;
            }
            case NAME_START_INDEX: {
                return name_start;
            }
            case NAME_CONTAINS_INDEX: {
                return name_contains;
            }
            case NAME_END_INDEX: {
                return name_end;
            }
            case IDENTIFICATION_EXACTLY_INDEX: {
                return identification_exactly;
            }
            case IDENTIFICATION_START_INDEX: {
                return identification_start;
            }
            case IDENTIFICATION_CONTAINS_INDEX: {
                return identification_contains;
            }
            case IDENTIFICATION_END_INDEX: {
                return identification_end;
            }
            case PROVINCE_CODE: {
                return String.format("%d", province_code);
            }
            default: {
                return null;
            }
        }
    }


    public String getString(int index){

        String string = "";

        switch (index) {
            case NUMBER_INDEX: {
                string += number_exactly.length() == 0 ? "" : number_exactly;
                string += number_start.length() != 0 && string.length() != 0 ? " | " : "";
                string += number_start.length() != 0 ? number_start : "";
                string += number_contains.length() != 0 && string.length() != 0 ? " | " : "";
                string += number_contains.length() != 0 ? number_contains : "";
                string += number_end.length() != 0 && string.length() != 0 ? " | " : "";
                string += number_end.length() != 0 ? number_end : "";
                return string;
            }
            case NAME_INDEX: {
                string += name_exactly.length() == 0 ? "" : name_exactly;
                string += name_start.length() != 0 && string.length() != 0 ? " | " : "";
                string += name_start.length() != 0 ? name_start : "";
                string += name_contains.length() != 0 && string.length() != 0 ? " | " : "";
                string += name_contains.length() != 0 ? name_contains : "";
                string += name_end.length() != 0 && string.length() != 0 ? " | " : "";
                string += name_end.length() != 0 ? name_end : "";
                return string;
            }
            case IDENTIFICATION_INDEX: {
                string += identification_exactly.length() == 0 ? "" : identification_exactly;
                string += identification_start.length() != 0 && string.length() != 0 ? " | " : "";
                string += identification_start.length() != 0 ? identification_start : "";
                string += identification_contains.length() != 0 && string.length() != 0 ? " | " : "";
                string += identification_contains.length() != 0 ? identification_contains : "";
                string += identification_end.length() != 0 && string.length() != 0 ? " | " : "";
                string += identification_end.length() != 0 ? identification_end : "";
                return string;
            }
            case PROVINCE_INDEX: {
                if (province_code != -1)
                    return String.format("%d", province_code);
                return "";
            }
            default: {
                return "";
            }
        }

    }
}
