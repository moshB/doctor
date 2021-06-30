package mosh.bo.doctorapp.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import mosh.bo.doctorapp.R;
import mosh.bo.doctorapp.databinding.FragmentFirstBinding;
import mosh.bo.doctorapp.doctor.DoctorActivity;
import mosh.bo.doctorapp.patient.PatientActivity;

public class LoginFragment extends Fragment {
    Button btnSingup;
    Button btnLogin;
    EditText etEmail;
    EditText etPassword;
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

    boolean isClick = true;
    private FragmentFirstBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ){
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnSingup = view.findViewById(R.id.btnSingup);
        btnLogin = view.findViewById(R.id.btnLogin);
        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);

        //go to singup fragment
        btnSingup.setOnClickListener(v -> {
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_FirstFragment_to_SecondFragment);
        });

        btnLogin.setOnClickListener(v -> {
            if (isClick) {
                isClick = false;
                if (checkValid()) {
                    FirebaseAuth
                            .getInstance()
                            .signInWithEmailAndPassword(etEmail.getText().toString(),
                                    etPassword.getText().toString())
                            .addOnSuccessListener(this.getActivity(), authResult -> {
                                FirebaseDatabase
                                        .getInstance()
                                        .getReference()
                                        .child("users")
                                        .child(authResult.getUser().getUid())
                                        .child("doctor")
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot snapshot) {
                                                Boolean isDoctor = snapshot.getValue(Boolean.TYPE);
                                                if(isDoctor){
                                                    Intent doctorIntent = new Intent(getContext(), DoctorActivity.class);
                                                    startActivity(doctorIntent);
                                                }else {
                                                    Intent patientIntent = new Intent(getContext(), PatientActivity.class);
                                                    startActivity(patientIntent);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError error) {
                                                Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                                                isClick = true;
                                            }
                                        });
                            })
                            .addOnFailureListener(this.getActivity(), e -> {
                                Toast.makeText(this.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                isClick = true;
                            });

                }else {
                    Toast.makeText(this.getContext(), R.string.ceackValid, Toast.LENGTH_LONG).show();
                    isClick = true;
                }
            }
        });

    }
    // check if all the input is valid
    private boolean checkValid() {
        if (etEmail.getText().toString().matches(emailPattern)) {
            return etPassword.getText().toString().length() >= 6;
        }
        return false;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}