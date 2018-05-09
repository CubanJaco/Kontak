package com.jaco.contact;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.database.sqlite.SQLiteException;

import com.jaco.contact.preferences.mSharedPreferences;

import java.io.File;
import java.sql.SQLException;

/**
 * Created by osvel on 6/27/16.
 */
public class EtecsaDB {

    //Constants
    private final String FIX_NUMBER = "number";
    private final String FIX_NAME = "name";
    private final String FIX_PROVINCE = "province";
    private final String DATABASE_TABLE_FIX = "fix";
    private final int FIX_PROVINCE_INDEX = 3;
    private final int FIX_ADDRESS_INDEX = 2;
    private final int FIX_NAME_INDEX = 1;
    private final int FIX_NUMBER_INDEX = 0;

    private final String MOVIL_NUMBER = "number";
    private final String MOVIL_NAME = "name";
    private final String MOVIL_PROVINCE = "PROVINCE";
    private final String MOVIL_IDENT = "identification";
    private final String MOVIL_ROW_ID = "rowid";
    private final String DATABASE_TABLE_MOVIL = "movil";
    private final int MOVIL_PROVINCE_INDEX = 4;
    private final int MOVIL_ADDRESS_INDEX = 3;
    private final int MOVIL_IDENT_INDEX = 2;
    private final int MOVIL_NAME_INDEX = 1;
    private final int MOVIL_NUMBER_INDEX = 0;

    private String databasePath = null;
    private SQLiteDatabase db;
    private Context context;

    public EtecsaDB(Context context) {

        this.context = context;
        this.databasePath = mSharedPreferences.getDatabasePath(context);

    }

    public EtecsaDB(Context context, String databasePath) {

        this.context = context;
        this.databasePath = databasePath;

    }

    public void open() throws SQLException {

        if (new File(databasePath).exists() && (db != null && !db.isOpen() || db == null ) ) {
            try {
                this.db = SQLiteDatabase.openDatabase(databasePath, null, SQLiteDatabase.OPEN_READONLY);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean hasDatabase(){

        return new File(databasePath).exists();

    }

    public void close() {
        if (isOpen()) {
            this.db.close();
        }
    }

    public boolean isOpen() {
        if (this.db != null) {
            return this.db.isOpen();
        }
        return false;
    }

    public int getLastIndex() throws SQLiteException{

        int last_id = -1;

        try {
            if (!isOpen())
                open();
        } catch (SQLException e) {
            return last_id;
        }

        //obtener el ultimo id de la tabla
        //SELECT rowid FROM movil ORDER BY rowid DESC LIMIT 1
        Cursor cursor = this.db.query(DATABASE_TABLE_MOVIL, new String[]{MOVIL_ROW_ID}, "", null, null, null, MOVIL_ROW_ID + " DESC", "1");
        if (cursor.moveToFirst())
            last_id = Integer.parseInt(cursor.getString(0));
        cursor.close();
        close();

        return last_id;

    }

    public void updateNumberByID(String newNumber, int id) throws SQLiteException{

        //actualizar la tupla en la base de datos con el nuevo numero
        //UPDATE "movil" SET "number" = ? WHERE  "rowid" = ?
        if (!db.isReadOnly()){
            ContentValues values = new ContentValues();
            values.put(MOVIL_NUMBER, newNumber);
            db.update(DATABASE_TABLE_MOVIL, values, MOVIL_ROW_ID +" = "+ id, null);
        }
        close();

    }

    private String createFixConsult(AdvancedSearch advancedSearch){

        String consult = "";

        String aux;
        aux = advancedSearch.get(AdvancedSearch.NAME_EXACTLY_INDEX);
        if (aux.length() != 0)
            consult += FIX_NAME+" LIKE '"+aux+"' ";

        aux = advancedSearch.get(AdvancedSearch.NAME_START_INDEX);
        if (consult.length() != 0 && aux.length() != 0)
            consult += "AND "+FIX_NAME+" LIKE '"+aux+"%' ";
        else if (aux.length() != 0)
            consult += FIX_NAME+" LIKE '"+aux+"%' ";

        aux = advancedSearch.get(AdvancedSearch.NAME_CONTAINS_INDEX);
        String contains[] = aux.split(",");
        for (String contain: contains) {
            if (consult.length() != 0 && contain.length() != 0)
                consult += "AND "+ FIX_NAME+" LIKE '%"+contain+"%' ";
            else if (contain.length() != 0)
                consult += FIX_NAME+" LIKE '%"+contain+"%' ";
        }

        aux = advancedSearch.get(AdvancedSearch.NAME_END_INDEX);
        if (consult.length() != 0 && aux.length() != 0)
            consult += "AND "+ FIX_NAME+" LIKE '%"+aux+"' ";
        else if (aux.length() != 0)
            consult += FIX_NAME+" LIKE '%"+aux+"' ";

        aux = advancedSearch.get(AdvancedSearch.NUMBER_EXACTLY_INDEX);
        if (consult.length() != 0 && aux.length() != 0)
            consult += "AND "+ FIX_NUMBER+" LIKE '"+aux+"' ";
        else if (aux.length() != 0)
            consult += FIX_NUMBER+" LIKE '"+aux+"' ";

        aux = advancedSearch.get(AdvancedSearch.NUMBER_START_INDEX);
        if (consult.length() != 0 && aux.length() != 0)
            consult += "AND "+ FIX_NUMBER+" LIKE '"+aux+"%' ";
        else if (aux.length() != 0)
            consult += FIX_NUMBER+" LIKE '"+aux+"%' ";

        aux = advancedSearch.get(AdvancedSearch.NUMBER_CONTAINS_INDEX);
        contains = aux.split(",");
        for (String contain: contains) {
            if (consult.length() != 0 && contain.length() != 0)
                consult += "AND "+ FIX_NUMBER+" LIKE '%"+contain+"%' ";
            else if (contain.length() != 0)
                consult += FIX_NUMBER+" LIKE '%"+contain+"%' ";
        }

        aux = advancedSearch.get(AdvancedSearch.NUMBER_END_INDEX);
        if (consult.length() != 0 && aux.length() != 0)
            consult += "AND "+ FIX_NUMBER+" LIKE '%"+aux+"' ";
        else if (aux.length() != 0)
            consult += FIX_NUMBER+" LIKE '%"+aux+"' ";

        int province = Integer.parseInt(advancedSearch.get(AdvancedSearch.PROVINCE_CODE));
        if (consult.length() != 0 && province != -1){
            consult +="AND "+ FIX_PROVINCE+"="+province;
        }
        else if (province != -1){
            consult += FIX_PROVINCE+"="+province;
        }

        return consult;

    }

    private String createMovilConsult(AdvancedSearch advancedSearch){

        String consult = "";

        String aux;
        aux = advancedSearch.get(AdvancedSearch.NAME_EXACTLY_INDEX);
        if (aux.length() != 0)
            consult += MOVIL_NAME+" LIKE '"+aux+"' ";

        aux = advancedSearch.get(AdvancedSearch.NAME_START_INDEX);
        if (consult.length() != 0 && aux.length() != 0)
            consult += "AND "+MOVIL_NAME+" LIKE '"+aux+"%' ";
        else if (aux.length() != 0)
            consult += MOVIL_NAME+" LIKE '"+aux+"%' ";

        aux = advancedSearch.get(AdvancedSearch.NAME_CONTAINS_INDEX);
        String contains[] = aux.split(",");
        for (String contain: contains) {
            if (consult.length() != 0 && contain.length() != 0)
                consult += "AND "+ MOVIL_NAME+" LIKE '%"+contain+"%' ";
            else if (contain.length() != 0)
                consult += MOVIL_NAME+" LIKE '%"+contain+"%' ";
        }

        aux = advancedSearch.get(AdvancedSearch.NAME_END_INDEX);
        if (consult.length() != 0 && aux.length() != 0)
            consult += "AND "+ MOVIL_NAME+" LIKE '%"+aux+"' ";
        else if (aux.length() != 0)
            consult += MOVIL_NAME+" LIKE '%"+aux+"' ";

        aux = advancedSearch.get(AdvancedSearch.NUMBER_EXACTLY_INDEX);

        boolean alternative = mSharedPreferences.isAlternativeDatabase(context);

        if (!alternative && advancedSearch.get(AdvancedSearch.NUMBER_EXACTLY_INDEX).length() == 8)
            aux = "53"+aux;

        if (consult.length() != 0 && aux.length() != 0)
            consult += "AND "+ MOVIL_NUMBER+" LIKE '"+aux+"' ";
        else if (aux.length() != 0)
            consult += MOVIL_NUMBER+" LIKE '"+aux+"' ";

        if (alternative){
            aux = advancedSearch.get(AdvancedSearch.NUMBER_START_INDEX);
            if (consult.length() != 0 && aux.length() != 0)
                consult += "AND "+ MOVIL_NUMBER+" LIKE '"+aux+"%' ";
            else if (aux.length() != 0)
                consult += MOVIL_NUMBER+" LIKE '"+aux+"%' ";
        }
        else {
            aux = advancedSearch.get(AdvancedSearch.NUMBER_START_INDEX);
            if (consult.length() != 0 && aux.length() != 0)
                consult += "AND " + MOVIL_NUMBER + " LIKE '53" + aux + "%' ";
            else if (aux.length() != 0)
                consult += MOVIL_NUMBER + " LIKE '53" + aux + "%' ";
        }
        aux = advancedSearch.get(AdvancedSearch.NUMBER_CONTAINS_INDEX);
        contains = aux.split(",");
        for (String contain: contains) {
            if (consult.length() != 0 && contain.length() != 0)
                consult += "AND "+ MOVIL_NUMBER+" LIKE '%"+contain+"%' ";
            else if (contain.length() != 0)
                consult += MOVIL_NUMBER+" LIKE '%"+contain+"%' ";
        }

        aux = advancedSearch.get(AdvancedSearch.NUMBER_END_INDEX);
        if (consult.length() != 0 && aux.length() != 0)
            consult += "AND "+ MOVIL_NUMBER+" LIKE '%"+aux+"' ";
        else if (aux.length() != 0)
            consult += MOVIL_NUMBER+" LIKE '%"+aux+"' ";

        aux = advancedSearch.get(AdvancedSearch.IDENTIFICATION_EXACTLY_INDEX);
        if (consult.length() != 0 && aux.length() != 0)
            consult += "AND "+ MOVIL_IDENT+" LIKE '"+aux+"' ";
        else if (aux.length() != 0)
            consult += MOVIL_IDENT+" LIKE '"+aux+"' ";

        aux = advancedSearch.get(AdvancedSearch.IDENTIFICATION_START_INDEX);
        if (consult.length() != 0 && aux.length() != 0)
            consult += "AND "+ MOVIL_IDENT+" LIKE '"+aux+"%' ";
        else if (aux.length() != 0)
            consult += MOVIL_IDENT+" LIKE '"+aux+"%' ";

        aux = advancedSearch.get(AdvancedSearch.IDENTIFICATION_CONTAINS_INDEX);
        contains = aux.split(",");
        for (String contain: contains) {
            if (consult.length() != 0 && contain.length() != 0)
                consult += "AND "+ MOVIL_IDENT+" LIKE '%"+contain+"%' ";
            else if (contain.length() != 0)
                consult += MOVIL_IDENT+" LIKE '%"+contain+"%' ";
        }

        aux = advancedSearch.get(AdvancedSearch.IDENTIFICATION_END_INDEX);
        if (consult.length() != 0 && aux.length() != 0)
            consult += "AND "+ MOVIL_IDENT+" LIKE '%"+aux+"' ";
        else if (aux.length() != 0)
            consult += MOVIL_IDENT+" LIKE '%"+aux+"' ";

        int province = Integer.parseInt(advancedSearch.get(AdvancedSearch.PROVINCE_CODE));
        if (consult.length() != 0 && province != -1){
            consult +="AND "+ MOVIL_PROVINCE+"="+province;
        }
        else if (province != -1){
            consult += MOVIL_PROVINCE+"="+province;
        }

        return consult;

    }

    private PhoneEntry[] advancedSearchFix(AdvancedSearch advancedSearch){

        String consult = createFixConsult(advancedSearch);
        int limit = mSharedPreferences.getQueryLimit(context);

        Cursor matches;
        try {
            matches = this.db.query(this.DATABASE_TABLE_FIX, null,
                    consult, null, null, null, null, ""+limit);
        } catch (SQLiteException e) {
            return null;
        }

        int i = 0;
        PhoneEntry[] phones = new PhoneEntry[matches.getCount()];
        if (!matches.moveToFirst()) {
            return null;
        }
        do {
            phones[i] = new PhoneEntry();
            phones[i].setType(PhoneType.FIX);
            phones[i].setNumber(matches.getString(this.FIX_NUMBER_INDEX));
            phones[i].setName(matches.getString(this.FIX_NAME_INDEX));
            phones[i].setAddress(matches.getString(this.FIX_ADDRESS_INDEX));
            phones[i].setProvince(matches.getString(this.FIX_PROVINCE_INDEX));
            i++;
        } while (matches.moveToNext());

        matches.close();
        close();

        return phones;
    }

    private PhoneEntry[] advancedSearchMovil(AdvancedSearch advancedSearch){

        String consult = createMovilConsult(advancedSearch);
        int limit = mSharedPreferences.getQueryLimit(context);

        boolean alternative = mSharedPreferences.isAlternativeDatabase(context);

        Cursor matches;
        try {
            matches = this.db.query(this.DATABASE_TABLE_MOVIL, null, consult, null, null, null, null, ""+limit);
        } catch (SQLiteException e) {
            return null;
        }

        int i = 0;
        PhoneEntry[] phones = new PhoneEntry[matches.getCount()];
        if (!matches.moveToFirst()) {
            return null;
        }
        do {
            phones[i] = new PhoneEntry();
            phones[i].setType(PhoneType.MOVIL);
            if (!alternative)
                phones[i].setNumber(matches.getString(this.MOVIL_NUMBER_INDEX).substring(2));
            else
                phones[i].setNumber(matches.getString(this.MOVIL_NUMBER_INDEX));
            phones[i].setName(matches.getString(this.MOVIL_NAME_INDEX));
            phones[i].setIdentification(matches.getString(this.MOVIL_IDENT_INDEX));
            phones[i].setAddress(matches.getString(this.MOVIL_ADDRESS_INDEX));
            phones[i].setProvince(matches.getString(this.MOVIL_PROVINCE_INDEX));
            i++;
        } while (matches.moveToNext());

        matches.close();
        close();

        return phones;
    }

    public PhoneEntry[] advancedSearch(AdvancedSearch advancedSearch){

        if (!isOpen()) {
            return null;
        }

        if (advancedSearch.getType() == PhoneType.FIX)
            return advancedSearchFix(advancedSearch);
        else
            return advancedSearchMovil(advancedSearch);
    }

    private PhoneEntry[] searchMobileByNumber(String number) throws SQLiteException {
        if (!isOpen()) {
            return null;
        }

        boolean alternative = mSharedPreferences.isAlternativeDatabase(context);

        if (!alternative && number.length() == 8)
            number = "53"+number;

        Cursor matches = this.db.query(this.DATABASE_TABLE_MOVIL, null, this.MOVIL_NUMBER + " = ?", new String[]{number}, null, null, null);
        int i = 0;
        PhoneEntry[] phones = new PhoneEntry[matches.getCount()];

        if (!matches.moveToFirst()) {
            return null;
        }
        do {
            phones[i] = new PhoneEntry();
            phones[i].setType(PhoneType.MOVIL);
            if (!alternative)
                phones[i].setNumber(matches.getString(this.MOVIL_NUMBER_INDEX).substring(2));
            else
                phones[i].setNumber(matches.getString(this.MOVIL_NUMBER_INDEX));
            phones[i].setName(matches.getString(this.MOVIL_NAME_INDEX));
            phones[i].setIdentification(matches.getString(this.MOVIL_IDENT_INDEX));
            phones[i].setAddress(matches.getString(this.MOVIL_ADDRESS_INDEX));
            phones[i].setProvince(matches.getString(this.MOVIL_PROVINCE_INDEX));
            i++;
        } while (matches.moveToNext());

        matches.close();
        close();

        return phones;
    }

    private PhoneEntry[] searchFixByNumber(String number) throws SQLiteException{
        if (!isOpen()) {
            return null;
        }

        boolean alternative = mSharedPreferences.isAlternativeDatabase(context);

        PhoneNumber phoneNumber = new PhoneNumber(number);
        int province = phoneNumber.getProvinceCode();
        Cursor matches;
        if (alternative && province == 47){
            matches = this.db.query(this.DATABASE_TABLE_FIX, null,
                    this.FIX_NUMBER + " = ? AND (" + this.FIX_PROVINCE + " = 47 OR " + this.FIX_PROVINCE + " = 74 )",
                    new String[]{phoneNumber.getFixNumber()}, null, null, null);
        }
        else if (province != -1){
            matches = this.db.query(this.DATABASE_TABLE_FIX, null,
                    this.FIX_NUMBER + " = ? AND " + this.FIX_PROVINCE + " = ? ",
                    new String[]{phoneNumber.getFixNumber(), "" + province}, null, null, null);
        }
        else {
            matches = this.db.query(this.DATABASE_TABLE_FIX, null,
                    this.FIX_NUMBER + " = ?", new String[]{phoneNumber.getFixNumber()}, null, null, null);
        }

        int i = 0;
        PhoneEntry[] phones = new PhoneEntry[matches.getCount()];
        if (!matches.moveToFirst()) {
            return null;
        }
        do {
            phones[i] = new PhoneEntry();
            phones[i].setType(PhoneType.FIX);
            phones[i].setNumber(matches.getString(this.FIX_NUMBER_INDEX));
            phones[i].setName(matches.getString(this.FIX_NAME_INDEX));
            phones[i].setAddress(matches.getString(this.FIX_ADDRESS_INDEX));
            phones[i].setProvince(matches.getString(this.FIX_PROVINCE_INDEX));
            i++;
        } while (matches.moveToNext());

        matches.close();
        close();

        return phones;
    }

    public PhoneEntry[] searchByNumber(String number){

        if (!PhoneNumber.isValidNumber(number))
            return null;

        PhoneNumber phoneNumber = new PhoneNumber(number);
        number = phoneNumber.getNumber();

        try {
            open();
        } catch (SQLException e) {
            return null;
        }

        try {
            return phoneNumber.isMovil() ? searchMobileByNumber(number) : searchFixByNumber(number);
        } catch (SQLiteException e) {
            return null;
        }

    }

    public PhoneEntry[] searchMobileByName(String name){

        String sentence = MOVIL_NAME +" LIKE '";

        for (String subString :
                name.split(" ")) {
            sentence += "%"+subString;
        }

        boolean alternative = mSharedPreferences.isAlternativeDatabase(context);
        int limit = mSharedPreferences.getQueryLimit(context);

        sentence += "%'";

        Cursor matches;
        try {
            matches = this.db.query(this.DATABASE_TABLE_MOVIL, null, sentence, null, null, null, null, ""+limit);
        } catch (SQLiteException e) {
            return null;
        }

        int i = 0;
        PhoneEntry[] phones = new PhoneEntry[matches.getCount()];
        if (!matches.moveToFirst()) {
            return null;
        }
        do {
            phones[i] = new PhoneEntry();
            phones[i].setType(PhoneType.MOVIL);
            if (!alternative)
                phones[i].setNumber(matches.getString(this.MOVIL_NUMBER_INDEX).substring(2));
            else
                phones[i].setNumber(matches.getString(this.MOVIL_NUMBER_INDEX));
            phones[i].setName(matches.getString(this.MOVIL_NAME_INDEX));
            phones[i].setIdentification(matches.getString(this.MOVIL_IDENT_INDEX));
            phones[i].setAddress(matches.getString(this.MOVIL_ADDRESS_INDEX));
            phones[i].setProvince(matches.getString(this.MOVIL_PROVINCE_INDEX));
            i++;
        } while (matches.moveToNext());

        matches.close();
        return phones;

    }

    public String searchMobileById(int id){

        try {
            if (!isOpen())
                open();
        } catch (SQLException e) {
            return null;
        }

      	Cursor matches;
        try {
            matches = this.db.query(this.DATABASE_TABLE_MOVIL, null, MOVIL_ROW_ID+" = "+id, null, null, null, null, ""+1);
        }
        catch (SQLiteDatabaseCorruptException e){
            close();
            return null;
        }
        if (!matches.moveToFirst()) {
            return null;
        }
        String number = matches.getString(this.MOVIL_NUMBER_INDEX);
        matches.close();

        return number;

    }

    public PhoneEntry[] searchFixByName(String name){

        String sentence = MOVIL_NAME +" LIKE '";

        for (String subString :
                name.split(" ")) {
            sentence += "%"+subString;
        }

        sentence += "%'";
        int limit = mSharedPreferences.getQueryLimit(context);

        Cursor matches;
        try {
            matches = this.db.query(this.DATABASE_TABLE_FIX, null, sentence, null, null, null, null, "" + limit);
        } catch (SQLiteException e) {
            return null;
        }

        int i = 0;
        PhoneEntry[] phones = new PhoneEntry[matches.getCount()];
        if (!matches.moveToFirst()) {
            return null;
        }
        do {
            phones[i] = new PhoneEntry();
            phones[i].setType(PhoneType.FIX);
            phones[i].setNumber(matches.getString(this.FIX_NUMBER_INDEX));
            phones[i].setName(matches.getString(this.FIX_NAME_INDEX));
            phones[i].setAddress(matches.getString(this.FIX_ADDRESS_INDEX));
            phones[i].setProvince(matches.getString(this.FIX_PROVINCE_INDEX));
            i++;
        } while (matches.moveToNext());

        matches.close();
        return phones;

    }

    public PhoneEntry[] searchByName(String name){

        if (!isOpen()) {
            return null;
        }

        PhoneEntry[] mobile = searchMobileByName(name);
        PhoneEntry[] fix = searchFixByName(name);
        int size = (mobile != null ? mobile.length : 0) + (fix != null ? fix.length : 0);
        PhoneEntry[] all = new PhoneEntry[size];

        int count = 0;
        if (mobile != null)
            for (PhoneEntry phone : mobile)
                all[count++] = phone;
        if (fix != null)
            for (PhoneEntry phone : fix)
                all[count++] = phone;

        return all;

    }

}
