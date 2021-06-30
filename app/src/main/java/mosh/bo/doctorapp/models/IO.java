package mosh.bo.doctorapp.models;

import com.google.firebase.auth.FirebaseAuth;

public class IO {


    public static String PATIENTS = "patients";
    public static String DOCTOR = "doctor";
    public static String START = "Start a meeting";
    public static String END = "End a meeting";
    public static float INAPPOINTMENTTIME = (float) 0.4;
    public static float OFLINE = (float) 0.0;


    public static String getUid(){
        return FirebaseAuth.getInstance().getUid();
    }
    public static String getEmail(){
        return FirebaseAuth.getInstance().getCurrentUser().getEmail();
    }


}
