package mosh.bo.doctorapp.patient;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import mosh.bo.doctorapp.R;
import mosh.bo.doctorapp.models.IO;
import mosh.bo.doctorapp.models.User;
import mosh.bo.doctorapp.patient.adapters.AppointmentAdapter;


public class ListPatientsFragment extends Fragment {

    RecyclerView rvAppointmentList;
    AppointmentAdapter appointmentAdapter = new AppointmentAdapter(new ArrayList<>());
    List<User> patients = new ArrayList<>();
    User doctor;
    Button btnCancleAppointment;
    Fragment fragment = this;
    TextView tvDoctor;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list_patients, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();

        try {
            doctor = args.getParcelable(IO.DOCTOR);
            patients = args.getParcelableArrayList(IO.PATIENTS);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            if (patients == null) {
                patients = new ArrayList<>();
            }
            if (doctor != null) {
                tvDoctor = view.findViewById(R.id.tvDoctorTitle);
                tvDoctor.setText(doctor.getEmail());
                addToListAppointment(doctor.getId());
            }

            rvAppointmentList = view.findViewById(R.id.rvAppointmentList);
            rvAppointmentList
                    .setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
            rvAppointmentList.setAdapter(appointmentAdapter);
            appointmentAdapter.patients = patients;
            appointmentAdapter.notifyDataSetChanged();
        }
        btnCancleAppointment = view.findViewById(R.id.btnCancleAppointment);
        btnCancleAppointment.setOnClickListener(v -> {
            removedMyDoctor();
        });
    }

    // make An Appointment if the doctor is Available and the list patients is full
    public void addToListAppointment(String previous) {
        if (previous == null) return;

        String prev = previous;
        FirebaseDatabase
                .getInstance()
                .getReference()
                .child("users")
                .child(prev)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        User user = snapshot.getValue(User.class);

                        String userId = user.getId();
                        if (IO.getUid().equals(userId)) {
                            if (patients.get(patients.size() - 1).getId().equals(userId))
                                patients.remove(patients.size() - 1);
                            patients.add(user);
                            reloadData();
                        } else {
                            for (int i = 0; i < patients.size(); i++) {
                                if (patients.get(i).getId().equals(user.getId()))
                                    patients.remove(i);
                            }
                            patients.add(user);
                            addToListAppointment(user.getNext());
                            listenerAppointment(user.getId());
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT)
                                .show();
                    }
                });
    }


    // geting the user and go to removeNextUser
    private void getMyUserForRemovingAppointment(int i) {
        FirebaseDatabase
                .getInstance()
                .getReference()
                .child("users")
                .child(IO.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        User myUser = snapshot.getValue(User.class);
                        removeNextUser(myUser, i);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {

                    }
                });
    }

    // removing my doctor id
    private void removedMyDoctor() {
        FirebaseDatabase
                .getInstance()
                .getReference()
                .child("users")
                .child(IO.getUid())
                .child("myDoctor")
                .removeValue()
                .addOnSuccessListener(v -> {
                    for (int i = 0; i < patients.size(); i++) {
                        if (patients.get(i).getNext().equals(IO.getUid())) {
                            getMyUserForRemovingAppointment(i);
                        }
                    }
                });
    }

    //removing next user from previos user or change to my next user and moving to the doctor page
    void removeNextUser(User myUser, int index) {


        if (myUser.getNext() == null) {
            FirebaseDatabase
                    .getInstance()
                    .getReference()
                    .child("users")
                    .child(patients.get(index).getId())
                    .child("next")
                    .removeValue()
                    .addOnSuccessListener(v -> {
                        NavHostFragment.findNavController(fragment)
                                .navigate(R.id.action_ListPatients_to_ListDoctors);
                    });
        } else {
            FirebaseDatabase
                    .getInstance()
                    .getReference()
                    .child("users")
                    .child(patients.get(index).getId())
                    .child("next")
                    .setValue(myUser.getNext())
                    .addOnSuccessListener(v -> {
                        NavHostFragment.findNavController(fragment)
                                .navigate(R.id.action_ListPatients_to_ListDoctors);
                    });
        }
    }

    private void listenerAppointment(String prev) {
        if (prev == null) return;
        System.out.println();
        FirebaseDatabase
                .getInstance()
                .getReference()
                .child("users")
                .child(prev).
                addChildEventListener(new ChildEventListener() {

                    @Override
                    public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
                        System.out.println(snapshot.getValue());
                        System.out.println(snapshot.getKey());
                        String next = null;
                        for (int i = 0; i < patients.size(); i++) {
                            if (patients.get(i).getId().equals(prev)) {
                                switch (snapshot.getKey()) {
                                    case "id":
                                        patients.get(i).setId(snapshot.getValue(String.class));
                                        break;
                                    case "email":
                                        patients.get(i).setEmail(snapshot.getValue(String.class));
                                        break;
                                    case "available":
                                        patients.get(i).setAvailable(snapshot.getValue(Boolean.class));
                                        break;
                                    case "doctor":
                                        patients.get(i).setDoctor(snapshot.getValue(Boolean.class));
                                        break;
                                    case "next":
                                        if (!IO.getUid().equals(snapshot.getValue(String.class))) {
                                            next = snapshot.getValue(String.class);
                                            patients.get(i).setId(next);
                                        }
                                        break;
                                    case "myDoctor":
                                        patients.get(i).setId(snapshot.getValue(String.class));
                                        break;
                                }
                            }
                        }
                        if (next != null) {
                            boolean nextExist = false;
                            for (User patient : patients)
                                if (patient.getId().equals(next)) nextExist = true;
                        }
                    }

                    @Override
                    public void onChildChanged(DataSnapshot snapshot, String previousChildName) {
                        for (int i = 0; i < patients.size(); i++) {
                            if (patients.get(i).getId().equals(prev)) {
                                switch (snapshot.getKey()) {

                                    case "next":
                                        if (!IO.getUid().equals(snapshot.getValue(String.class))) {
                                            String next = snapshot.getValue(String.class);
                                            patients.get(i).setId(next);
                                            reloadData();
                                        }else if(doctor.getAvailable() && doctor.getId().equals(prev)){
                                            Bundle args = new Bundle();
                                            args.putParcelable(IO.DOCTOR, doctor);
                                            NavHostFragment.findNavController(fragment)
                                                    .navigate(R.id.action_ListPatients_to_AppointmentFragment, args);
                                        }
                                        break;
                                    case "myDoctor":
                                        String docId = snapshot.getValue(String.class);
                                        if (doctor.getId().equals(docId))
                                            patients.get(i).setId(docId);
                                        else patients.remove(i);
                                        reloadData();
                                        break;
                                }
                            }
                        }
                    }

                    @Override
                    public void onChildRemoved(DataSnapshot snapshot) {
                        for (int i = 0; i < patients.size(); i++) {
                            if (patients.get(i).getId().equals(prev)) {
                                switch (snapshot.getKey()) {
                                    case "next":
                                        if (!IO.getUid().equals(snapshot.getValue(String.class))) {
                                            if (patients.size() > i)
                                                patients.remove(i + 1);
                                            reloadData();
                                        }else if(doctor.getAvailable() && doctor.getId().equals(prev)){
                                            Bundle args = new Bundle();
                                            args.putParcelable(IO.DOCTOR, doctor);
                                        NavHostFragment.findNavController(fragment)
                                                .navigate(R.id.action_ListPatients_to_AppointmentFragment, args);
                                    }
                                        break;
                                    case "myDoctor":
                                        patients.remove(i);
                                        reloadData();
                                        break;
                                }
                            }
                        }
                    }

                    @Override
                    public void onChildMoved(DataSnapshot snapshot, String previousChildName) {
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                    }

                });
    }

    private void reloadData() {

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
        appointmentAdapter.patients = patients;
        appointmentAdapter.notifyDataSetChanged();
    }

}