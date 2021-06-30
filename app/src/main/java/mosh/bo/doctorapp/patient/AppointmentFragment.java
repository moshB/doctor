package mosh.bo.doctorapp.patient;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

import mosh.bo.doctorapp.R;
import mosh.bo.doctorapp.models.IO;
import mosh.bo.doctorapp.models.User;


public class AppointmentFragment extends Fragment {

    User doctor;
    Fragment fragment = this;
    TextView tvTitle;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_appointment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvTitle = view.findViewById(R.id.tvTitle);
        Bundle args = getArguments();
        doctor = args.getParcelable(IO.DOCTOR);
        tvTitle.setText(doctor.getEmail());
        listenAppoinment();
    }
    private void moveToHomePage(){
        tvTitle.setText("End meeting");
        tvTitle.setBackgroundColor(Color.parseColor("#ec6a6a"));
        tvTitle.postDelayed(() -> {
            NavHostFragment.findNavController(fragment)
                    .navigate(R.id.action_AppointmentFragment_to_ListDoctors);
        }, 1000);
    }
    private void listenAppoinment(){
        FirebaseDatabase
                .getInstance()
                .getReference()
                .child("users")
                .child(doctor.getId())
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded( DataSnapshot snapshot, String previousChildName) {

                    }

                    @Override
                    public void onChildChanged(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {
                        if(snapshot.getKey().equals("next")) {
                            String next = snapshot.getValue(String.class);

                            if (next != null &&  IO.getUid().equals(next)) {
                               moveToHomePage();
                            }
                        }
                    }

                    @Override
                    public void onChildRemoved(@NonNull @NotNull DataSnapshot snapshot) {
                        if(snapshot.getKey().equals("next")) {
                            String next = snapshot.getValue(String.class);

                            if (next != null &&  IO.getUid().equals(next)) {
                                moveToHomePage();
                            }
                        }
                    }

                    @Override
                    public void onChildMoved( DataSnapshot snapshot, String previousChildName) {

                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });

    }
}