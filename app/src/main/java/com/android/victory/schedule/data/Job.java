package com.android.victory.schedule.data;

/**
 * Created by PCJ on 1/24/2018.
 */

public class Job {
     public String row_id, job_code, job_name, update_date;

     public String getRow_id(){return this.row_id;}
     public void setRow_id(String row_id){this.row_id = row_id;}

     public String getJob_code(){return this.job_code;}
     public void setJob_code(String job_code){this.job_code = job_code;}

     public String getJob_name(){return this.job_name;}
     public void setJob_name(String job_name){this.job_name = job_name;}

     public String getUpdate_date(){return this.update_date;}
     public void setUpdate_date(String update_date){this.update_date = update_date;}
}
