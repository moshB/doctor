package mosh.bo.doctorapp.patient.adapters;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import mosh.bo.doctorapp.R;
import mosh.bo.doctorapp.models.IO;
import mosh.bo.doctorapp.models.User;

public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.ViewHolder> {

    public List<User> doctors;
    public boolean filter = true;
    boolean isClick = false;
    Fragment fragment;
    ArrayList<User> patients = new ArrayList<>();

    public DoctorAdapter(List<User> doctors, Fragment fragment) {
        this.doctors = doctors;
        this.fragment = fragment;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.doctor_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder( DoctorAdapter.ViewHolder holder, int position) {

        int newPosition = position;

        while (!(doctors.get(newPosition).getAvailable() || filter)) {
            newPosition++;
        }
        User doctor = doctors.get(position);
        holder.tvDoctorName.setText(doctor.getEmail());
        if (doctor.getAvailable()) {
            holder.ivDoctorAvailable.setImageResource(R.drawable.ic_boll_green);
            if(doctor.getNext() != null)
                holder.ivDoctorAvailable.setImageResource(R.drawable.ic_boll_orange);
        } else holder.ivDoctorAvailable.setImageResource(R.drawable.ic_boll_red);

        holder.btnMakeAnAppointment.setOnClickListener(v->{
            makeAnAppointment(doctor, v);
        });

    }


    @Override
    public int getItemCount() {
        if (filter) {
            return doctors.size();
        }
        int count = 0;
        for (User doctor : doctors) {
            if (doctor.getAvailable()) count++;
        }
        return count;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvDoctorName;
        ImageView ivDoctorAvailable;
        Button btnMakeAnAppointment;

        public ViewHolder( View itemView) {
            super(itemView);
            tvDoctorName = itemView.findViewById(R.id.tvDoctorName);
            btnMakeAnAppointment = itemView.findViewById(R.id.btnMakeAnAppointment);
            ivDoctorAvailable = itemView.findViewById(R.id.ivDoctorAvailable);
        }
    }


//    private methods


    private void makeAnAppointment(User doctor, View v){
        // make An Appointment if the doctor is Available and the list patients is empty
        if(!isClick) {
            isClick = true;
            if (doctor.getNext() == null && doctor.getAvailable()) {
                FirebaseDatabase
                        .getInstance()
                        .getReference()
                        .child("users")
                        .child(doctor.getId())
                        .child("next").setValue(IO.getUid()).addOnFailureListener(e -> {
                    Toast.makeText(v.getContext(), e.getMessage(), Toast.LENGTH_SHORT);
                    isClick = false;
                }).addOnSuccessListener(unused -> {
                    Bundle args = new Bundle();
                    args.putParcelable(IO.DOCTOR, doctor);
                    NavHostFragment.findNavController(fragment)
                            .navigate(R.id.action_ListDoctors_to_AppointmentFragment, args);

                });
            } else
                // make An Appointment if the doctor is Available and the list patients is full
                if (doctor.getAvailable()) addToList(doctor.getId(), v, doctor);
        }
    }

    // make An Appointment if the doctor is Available and the list patients is full
    public void addToList(String previous, View v, User doctor){
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
                        String nextPatient = user.getNext();
                        if(nextPatient == null || IO.getUid().equals(nextPatient)){
                            FirebaseDatabase
                                    .getInstance()
                                    .getReference()
                                    .child("users")
                                    .child(user.getId())
                                    .child("next")
                                    .setValue(IO.getUid())
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(v.getContext(),e.getMessage(), Toast.LENGTH_SHORT)
                                                .show();
                                    }).addOnSuccessListener(unused -> {
                                //add for my data that i whiting for doctor
                                FirebaseDatabase
                                        .getInstance()
                                        .getReference()
                                        .child("users")
                                        .child(IO.getUid())
                                        .child("myDoctor")
                                        .setValue(doctor.getId())
                                        .addOnFailureListener(e->{
                                            Toast.makeText(v.getContext(), e.getMessage(), Toast.LENGTH_SHORT)
                                                    .show();
                                        }).addOnSuccessListener(unused1 -> {
                                    Bundle bundle = new Bundle();
                                    patients.add(new User(IO.getUid(), IO.getEmail(), false, false, null, null));
                                    bundle.putParcelableArrayList(IO.PATIENTS, patients);
                                    bundle.putParcelable(IO.DOCTOR, doctor);
                                    NavHostFragment.findNavController(fragment)
                                            .navigate(R.id.action_ListDoctors_to_ListPatients, bundle);
                                });
                            });
                        }else {
                            patients.add(snapshot.getValue(User.class));
                            addToList(nextPatient, v, doctor);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(v.getContext(), error.getMessage(), Toast.LENGTH_SHORT)
                                .show();
                    }
                });
    }

}
