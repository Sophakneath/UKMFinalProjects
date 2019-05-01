package com.example.phakneath.jobber.sharePreferences;


public class AppSingleton {

    //private String playerId;
    private static AppSingleton instance;
    private AppSingleton(){

    }

    public static AppSingleton getInstance(){
        if(instance==null){
            instance=new AppSingleton();
        }
        return instance;
    }

    /*public String getPlayerId() {
        return playerId;
    }*/

    /*public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }*/

}
