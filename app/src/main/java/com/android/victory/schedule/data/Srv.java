package com.android.victory.schedule.data;

/**
 * Created by PCJ on 1/24/2018.
 */

public class Srv {

    public String row_id, srv_code, srv_name, update_user, update_date;

    public String getRow_id(){return this.row_id;}
    public void setRow_id(String row_id){this.row_id = row_id;}

    public String getSrv_code(){return this.srv_code;}
    public void setSrv_code(String job_code){this.srv_code = srv_code;}

    public String getSrv_name(){return this.srv_name;}
    public void setSrv_name(String job_name){this.srv_name = srv_name;}

    public String getUpdate_date(){return this.update_date;}
    public void setUpdate_date(String update_date){this.update_date = update_date;}

    public String getUpdate_user(){return this.update_user;}
    public void setUpdate_user(String update_user){this.update_user = update_user;}
}
