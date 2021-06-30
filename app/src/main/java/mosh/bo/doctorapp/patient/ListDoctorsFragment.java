package mosh.bo.doctorapp.patient;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

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
import mosh.bo.doctorapp.patient.adapters.DoctorAdapter;

public class ListDoctorsFragment extends Fragment {


    RecyclerView rvDoctor;
    DoctorAdapter doctorAdapter = new DoctorAdapter(new ArrayList<>(), this);
    List<User> doctors = new ArrayList<>();
    ImageView ivAvailable;
    Fragment fragment = this;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list_doctors, container, false);
    }

    @Override
    public void onViewCreated(/*@NonNull @org.jetbrains.annotations.NotNull*/ View view,/* @Nullable @org.jetbrains.annotations.Nullable*/ Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ivAvailable = view.findViewById(R.id.ivAvailable);
        rvDoctor = view.findViewById(R.id.rvDoctors);
        rvDoctor.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        rvDoctor.setAdapter(doctorAdapter);

        isInAppointmentOrWaitingOne();
        readAllData();
        ivAvailable.setOnClickListener(v -> {
            if (doctorAdapter.filter) {
                doctorAdapter.filter = false;
                ivAvailable.setImageResource(R.drawable.ic_boll_red);

            } else {
                doctorAdapter.filter = true;
                ivAvailable.setImageResource(R.drawable.ic_boll_green);
            }
            doctorAdapter.notifyDataSetChanged();
        });
    }

    // check if is In Appointment Or Waiting One
    private void isInAppointmentOrWaitingOne() {
        FirebaseDatabase
                .getInstance()
                .getReference()
                .child("users")
                .child(IO.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot mySnapshot) {
                        User myUser = mySnapshot.getValue(User.class);
                        if (myUser != null && myUser.getMyDoctor() != null) {
                            FirebaseDatabase
                                    .getInstance()
                                    .getReference()
                                    .child("users")
                                    .child(myUser.getMyDoctor())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot doctorSnapshot) {
                                            User myDoctor = doctorSnapshot.getValue(User.class);
                                            if (myDoctor != null && myDoctor.getNext() != null) {
                                                if (myDoctor.getNext().equals(myUser.getMyDoctor())) {
                                                    NavHostFragment
                                                            .findNavController(fragment)
                                                            .navigate(R.id.action_ListDoctors_to_AppointmentFragment);
                                                } else {
                                                    doctorAdapter.addToList(myDoctor.getId(), fragment.getView(), myDoctor);
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError error) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {

                    }
                });
    }

    //reload all doctors and fulling the page
    private void readAllData() {
        FirebaseDatabase
                .getInstance()
                .getReference()
                .child("users")
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
                        User user = snapshot.getValue(User.class);
                        if (user.getDoctor()) {
                            doctors.add(user);
                            doctorAdapter.doctors = doctors;
                            doctorAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onChildChanged(DataSnapshot snapshot, String previousChildName) {
                        User user = snapshot.getValue(User.class);
                        if (user.getDoctor()) {
                            for (User user1 : doctors) {
                                if (user.getId().equals(user1.getId())) {
                                    user1.setAvailable(user.getAvailable());
                                    doctorAdapter.doctors = doctors;
                                    doctorAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                    }

                    @Override
                    public void onChildRemoved(DataSnapshot snapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot snapshot, String previousChildName) {

                    }

                    @Override
                    public void onCancelled(DatabaseError error) {

                    }
                });
    }


}