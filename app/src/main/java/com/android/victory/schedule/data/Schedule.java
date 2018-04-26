package com.android.victory.schedule.data;

/**
 * Created by PCJ on 1/24/2018.
 */

public class Schedule {
    public String _id,comp, ket, meet_name, client_name,client_id, job_name,job_id, sal_name,sal_id, srv_name, srv_id, sch_date,done_date,sync_date;

    public String get_id(){return this._id;}
    public void set_id(String _id){this._id = _id;}

    public String getComp(){return this.comp;}
    public void setComp(String comp){this.comp = comp;}

    public String getKet(){return this.ket;}
    public void setKet(String ket){this.ket = ket;}

    public String getMeet_name(){return this.meet_name;}
    public void setMeet_name(String meet_name){this.meet_name = meet_name;}

    public String getClient_name(){return this.client_name;}
    public void setClient_name(String client_name){this.client_name = client_name;}

    public String getClient_id(){return this.client_id;}
    public void setClient_id(String client_id){this.client_id = client_id;}

    public String getJob_name(){return this.job_name;}
    public void setJob_name(String job_name){this.job_name = job_name;}

    public String getJob_id(){return this.job_id;}
    public void setJob_id(String job_id){this.job_id = job_id;}

    public String getSal_name(){return this.sal_name;}
    public void setSal_name(String sal_name){this.sal_name = sal_name;}

    public String getSal_id(){return this.sal_id;}
    public void setSal_id(String sal_id){this.sal_id = sal_id;}

    public String getSrv_name(){return this.srv_name;}
    public void setSrv_name(String srv_name){this.srv_name = srv_name;}

    public String getSrv_id(){return this.srv_id;}
    public void setSrv_id(String srv_id){this.srv_id = srv_id;}

    public String getSch_date(){return this.sch_date;}
    public void setSch_date(String sch_date){this.sch_date = sch_date;}

    public String getDone_date(){return this.done_date;}
    public void setDone_date(String done_date){this.done_date = done_date;}

    public String getSync_date(){return this.sync_date;}
    public void setSync_date(String sync_date){this.sync_date = sync_date;}
}
