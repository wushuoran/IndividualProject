package com.example.matchandride.tools;

public class RideObject {

    private String AVGspd, Climb, Distance, Duration;

    public RideObject(){}

    public RideObject(String AVGspd, String Climb, String Distance, String Duration){
        this.AVGspd = AVGspd;
        this.Climb = Climb;
        this.Distance = Distance;
        this.Duration = Duration;
    }

    public double getAVGspd(){
        double spd = 0;
        System.out.println("get speed " + AVGspd);
        if (!AVGspd.equals("NaN") && !AVGspd.equals("--")) spd = Double.parseDouble(AVGspd);
        return spd;
    }

    public double getClimb(){
        double climb = 0;
        if (!Climb.equals("NaN")) climb = Double.valueOf(Climb);
        return climb;
    }

    public double getDistance(){
        double dis = 0;
        if (!Distance.equals("NaN")) dis = Double.valueOf(Distance);
        return dis;
    }

    public String getDuration(){
        return Duration;
    }

}
