package mosh.bo.doctorapp.doctor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Guideline;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import mosh.bo.doctorapp.R;
import mosh.bo.doctorapp.models.IO;
import mosh.bo.doctorapp.models.User;

public class DoctorActivity extends AppCompatActivity {

    DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users");

    Timer timer = new Timer();

    TextView tvTime;
    TextView tvDoctor;
    TextView tvPatient;

    Button btnOnline;
    long starttime = 0;
    Guideline glAppointment;

    User doctor;
    List<User> patients = new ArrayList<>();

    RecyclerView rvPatientsForDoctor;
    PatientAdapter patientAdapter = new PatientAdapter(new ArrayList<>());

    private String status = "off";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor);

        btnOnline = findViewById(R.id.btnOnline);
        tvTime = findViewById(R.id.tvTime);
        tvDoctor = findViewById(R.id.tvDoctor);
        tvPatient = findViewById(R.id.tvPatient);
        glAppointment = findViewById(R.id.glAppointment);
        glAppointment.setGuidelinePercent(IO.OFLINE);
        rvPatientsForDoctor = findViewById(R.id.rvPatientsForDoctor);


        btnOnline.setText(IO.START);
        loadData();

    }


    @Override
    public void onPause() {
        super.onPause();
        timer.cancel();
        timer.purge();
        handler.removeCallbacks(run);
        btnOnline.setText(IO.START);
        goToOfline();
    }


    //runs without timer be reposting self
    Handler handler = new Handler();
    Runnable run = new Runnable() {

        @Override
        public void run() {
            long millis = System.currentTimeMillis() - starttime;
            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;

            tvTime.setText(String.format("%d:%02d", minutes, seconds));
            if (seconds >= 10) {
                goToNext();
            }


            handler.postDelayed(this, 500);
        }
    };

    private void goToNext() {

        if (patients.size() > 1) {
            String next = patients.get(0).getNext();
            String idPrev = patients.get(0).getId();
            if (next.equals(patients.get(1).getId())) {
                doctor.setNext(next);
                User user = patients.get(0);
                user.setNext(null);
                user.setMyDoctor(null);
                patients.remove(0);
                ref.child(idPrev).setValue(user).addOnSuccessListener(v->{
                    ref.child(IO.getUid()). child("next").setValue(next)
                            .addOnSuccessListener(v1->{
                                goToOnline();
                            });
                });

            } else {
                patients.remove(1);
                goToNext();
                return;
            }
        }
        if(patients.size() == 1){
            String next = patients.get(0).getNext();
            String idPrev = patients.get(0).getId();
            User user = new User(idPrev, patients.get(0).getEmail(), false, false, null, null);
            patients.remove(0);

            ref.child(idPrev).setValue(user).addOnSuccessListener(v->{
                ref.child(IO.getUid()). child("next").removeValue()
                        .addOnSuccessListener(v1->{
                            goToOnline();
                        });
            });
        }
        goToOnline();

    }

    private void loadData() {

        ref.child(IO.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    doctor = user;
                    goToOnline();
                    btnOnline.setOnClickListener(v -> {
                        if(status.equals("off")) goToOnline();
                        else  goToOfline();
                    });
                    addToListAppointment(IO.getUid());
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(getApplication(), error.getMessage(), Toast.LENGTH_SHORT).show();
                btnOnline.setOnClickListener(v -> {
                    loadData();
                });
            }
        });

    }


    private void goToOnline() {
        System.out.println("_______001");
        if (doctor != null) {
            System.out.println("_______002");

            ref.child(IO.getUid()).child("available")
                    .setValue(true)
                    .addOnSuccessListener(v->{
                        doctor.setAvailable(true);
                        timer.cancel();
                        timer.purge();
                        handler.removeCallbacks(run);

                        starttime = System.currentTimeMillis();
                        System.out.println("_______003");

                        timer = new Timer();
                        handler.postDelayed(run, 0);
                        btnOnline.setText(IO.END);
                        glAppointment.setGuidelinePercent(IO.INAPPOINTMENTTIME);
                        tvDoctor.setText(IO.getEmail());
                        System.out.println("_______004");

                        if (doctor.getNext() != null && patients != null && patients.size() > 0) {
                            System.out.println("_______005");
                            goToBusy();
                        } else {
                            status = "online";
                            tvPatient.setText("wating.....");
                        }
                    });

        }
    }

    private void goToOfline() {
        timer.cancel();
        timer.purge();
        handler.removeCallbacks(run);
        btnOnline.setText(IO.START);
        glAppointment.setGuidelinePercent(IO.OFLINE);
        ref.child(IO.getUid()).child("available").setValue(false)
                .addOnFailureListener(e -> {

                }).addOnSuccessListener(v -> {
            status = "off";
        });
    }

    private void goToBusy() {
        System.out.println("_______006");

        tvPatient.setText(patients.get(0).getEmail());
        status = "busy";
    }




    // make An Appointment if the doctor is Available and the list patients is full
    public void addToListAppointment(String previous) {
        if (previous == null) return;

        String prev = previous;
        ref
                .child(prev)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        User user = snapshot.getValue(User.class);
                        if (user != null) {

                            String userId = user.getId();
                            if (IO.getUid().equals(userId)) {
                                doctor = user;
                                listenerAppointment(user.getId());
                            } else {
                                System.out.println("_______0111");

                                patients.add(user);
                                reloadData();

                            }
                            listenerAppointment(user.getNext());
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(getApplication(), error.getMessage(), Toast.LENGTH_SHORT)
                                .show();
                    }
                });
    }

    // listener for change in patients

    private void listenerAppointment(String prev) {
        if (prev == null) return;
        ref.child(prev)
                .addChildEventListener(new ChildEventListener() {

                    @Override
                    public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
                        System.out.println(snapshot.getValue());
                        System.out.println(snapshot.getKey());
                        String next = null;

                        switch (snapshot.getKey()) {
                            case "next":

                                next = snapshot.getValue(String.class);
                                if (next != null) {
                                    addToListAppointment(next);
                                    //case is doctor event
                                    if (IO.getUid().equals(prev)) {
//                                        if (doctor.getAvailable())
//                                            goToOnline();
                                    } else {
                                        // case is patients
                                        for (int i = 0; i < patients.size(); i++) {
                                            if (patients.get(i).getId().equals(prev)) {
                                                if (!IO.getUid().equals(snapshot.getValue(String.class))) {
                                                    next = snapshot.getValue(String.class);
                                                    patients.get(i).setId(next);
                                                }
                                            }
                                        }
                                    }
                                }
                                break;
                        }
                    }

                    @Override
                    public void onChildChanged(DataSnapshot snapshot, String previousChildName) {


                        switch (snapshot.getKey()) {

                            case "next":
                                if (prev.equals(IO.getUid())) {
                                    if (!IO.getUid().equals(snapshot.getValue(String.class))) {
                                        String next = snapshot.getValue(String.class);
                                        if (next != null) doctor.setNext(next);
                                    }
                                } else {
                                    for (int i = 0; i < patients.size(); i++) {
                                        if (patients.get(i).getId().equals(prev)) {
                                            String next = snapshot.getValue(String.class);
                                            patients.get(i).setNext(next);
                                            reloadData();
                                            return;
                                        }
                                    }
                                }
                                break;
                            case "myDoctor":
                                for (int i = 0; i < patients.size(); i++) {
                                    if (patients.get(i).getId().equals(prev)) {
                                        String docId = snapshot.getValue(String.class);
                                        if (doctor.getId().equals(docId))
                                            patients.get(i).setId(docId);
                                        else patients.remove(i);
                                        reloadData();
                                        return;
                                    }
                                }
                        }
                    }


                    @Override
                    public void onChildRemoved(DataSnapshot snapshot) {
                        if (prev.equals(IO.getUid()) && snapshot.getKey().equals("myDoctor")) {
                            doctor.setNext(null);
                        }
                        for (int i = 0; i < patients.size(); i++) {
                            if (patients.get(i).getId().equals(prev)) {
                                switch (snapshot.getKey()) {
                                    case "next":
                                        if (!IO.getUid().equals(snapshot.getValue(String.class))) {
                                            if (patients.size() > i)
                                                patients.remove(i + 1);
                                            reloadData();
                                        }
                                        break;
                                    case "myDoctor":
                                        patients.remove(i);
                                        reloadData();
                                        return;
                                }
                            }
                        }
                    }

                    @Override
                    public void onChildMoved(DataSnapshot snapshot, String
                            previousChildName) {
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                    }

                });
    }


    private void reloadData() {
        System.out.println("___01");
        System.out.println(patients);
        int count = 0;
        for (int i = 0; i < patients.size(); i++) {
            User patient = patients.get(i);

            for (int j = i + 1; j < patients.size(); j++) {
                if (i != j && patient.getId().equals(patients.get(j).getId())) {
                    System.out.println(patient.toString() + "_1");
                    patients.remove(j);
                    reloadData();
                    return;
                }
            }
        }
        patientAdapter.patients = patients;
        patientAdapter.notifyDataSetChanged();
        System.out.println(patients);
        System.out.println(status);
        System.out.println(doctor.getAvailable());
        System.out.println(patients.size());
        if(doctor.getAvailable() && patients.size() >= 0 && status.equals("online")){
            System.out.println("_______011");

            goToOnline();
        }
    }


}