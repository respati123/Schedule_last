package com.android.victory.schedule.data;

/**
 * Created by PCJ on 1/24/2018.
 */

public class SalesMan {
    public String row_id, sal_code, sal_name, pict_data, self_flag, addr, phone, hp, geo_latt, geo_long, sync_date, ket, update_user, update_date;

    public String getRow_id(){return this.row_id;}
    public void setRow_id(String row_id){
        this.row_id = row_id;
    }

    public String getSal_code(){return this.sal_code;}
    public void setSal_code(String sal_code){
        this.sal_code = sal_code;
    }

    public String getSal_name(){return this.sal_name;}
    public void setSal_name(String sal_name){
        this.sal_name = sal_name;
    }

    public String getAddr(){return this.addr;}
    public void setAddr(String addr){
        this.addr = addr;
    }

    public String getPict_data(){return this.pict_data;}
    public void setPict_data(String pict_data){
        this.pict_data = pict_data;
    }

    public String getSelf_flag(){return this.self_flag;}
    public void setSelf_flag(String self_flag){
        this.self_flag = self_flag;
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

    public String getSync_date(){return this.sync_date;}
    public void setSync_date(String sync_date){
        this.sync_date = sync_date;
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
