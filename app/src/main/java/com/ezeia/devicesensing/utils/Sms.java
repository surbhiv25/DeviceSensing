package com.ezeia.devicesensing.utils;

class Sms {
    private String _id;
    private String _address;
    private String _msg;
    private String _readState; //"0" for have not read sms and "1" for have read sms
    private String _time;
    private String _folderName;
    //private String _thread_id;
    private String _person;
    private String _protocol;
    private String _status;
    private String _type;
    private String _reply_path_present;
    private String _subject;
    private String _service_center;
    private String _locked;

    public String getId(){
        return _id;
    }
    public String getAddress(){
        return _address;
    }
    public String getMsg(){
        return _msg;
    }
    public String getReadState(){
        return _readState;
    }
    public String getTime(){
        return _time;
    }
    public String getFolderName(){
        return _folderName;
    }
    public String getPerson(){
        return _person;
    }
    public String getProtocol(){
        return _protocol;
    }
    public String getStatus(){
        return _status;
    }
    public String getType(){
        return _type;
    }
    public String getReplyPath(){
        return _reply_path_present;
    }
    public String getSubject(){
        return _subject;
    }
    public String getServiceCenter(){
        return _service_center;
    }
    public String getLocked(){
        return _locked;
    }


    public void setId(String id){
        _id = id;
    }
    public void setAddress(String address){
        _address = address;
    }
    public void setMsg(String msg){
        _msg = msg;
    }
    public void setReadState(String readState){
        _readState = readState;
    }
    public void setTime(String time){
        _time = time;
    }
    public void setFolderName(String folderName){
        _folderName = folderName;
    }
    public void setPerson(String person){
        _person = person;
    }
    public void setProtocol(String protocol){
        _protocol = protocol;
    }
    public void setStatus(String status){ _status = status; }
    public void setType(String type){
        _type = type;
    }
    public void setReplyPath(String reply_path_present){ _reply_path_present = reply_path_present; }
    public void setSubject(String subject){
        _subject = subject;
    }
    public void setServiceCenter(String service_center){
        _service_center = service_center;
    }
    public void setLocked(String locked){
        _locked = locked;
    }


}