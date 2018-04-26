package com.android.victory.schedule.data;

/**
 * Created by PCJ on 1/24/2018.
 */

public class Client {
    public String row_id, client_code, client_name, addr, phone, hp, geo_latt, geo_long, ket, update_user, update_date;

    public String getRow_id(){return this.row_id;}
    public void setRow_id(String row_id){
        this.row_id = row_id;
    }

    public String getClient_code(){return this.client_code;}
    public void setClient_code(String client_code){
        this.client_code = client_code;
    }

    public String getClient_name(){return this.client_name;}
    public void setClient_name(String client_name){
        this.client_name = client_name;
    }

    public String getAddr(){return this.addr;}
    public void setAddr(String addr){
        this.addr = addr;
    }

    public String getPhone(){return this.phone;}
    public void setPhone(String phone){
        this.phone = phone;
    }

    public String getHP(){return this.hp;}
    public void setHP(String hp){
        this.hp = hp;
    }

    public String getGeo_latt(){return this.geo_latt;}
    public void setGeo_latt(String geo_latt){
        this.geo_latt = geo_latt;
    }

    public String getGeo_long(){return this.geo_long;}
    public void setGeo_long(String geo_long){
        this.geo_long = geo_long;
    }

    public String getKet(){return this.ket;}
    public void setKet(String ket){
        this.ket = ket;
    }

    public String getUpdate_user(){return this.update_user;}
    public void setUpdate_user(String update_user){
        this.update_user = update_user;
    }

    public String getUpdate_date(){return this.update_date;}
    public void setUpdate_date(String update_date){
        this.update_date = update_date;
    }
}
