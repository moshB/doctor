package mosh.bo.doctorapp.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import mosh.bo.doctorapp.R;
import mosh.bo.doctorapp.databinding.FragmentSecondBinding;
import mosh.bo.doctorapp.doctor.DoctorActivity;
import mosh.bo.doctorapp.models.User;
import mosh.bo.doctorapp.patient.PatientActivity;

public class SingupFragment extends Fragment {

    Button btnSingUp;
    EditText etEmail;
    EditText etPassword;
    EditText etPasswordConfirm;
    RadioButton rbDoctor;
    RadioButton rbPatient;
    boolean isClick = true;


    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";


    private FragmentSecondBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentSecondBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated( View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnSingUp = view.findViewById(R.id.btnSingUp);
        etEmail = view.findViewById(R.id.etEmailSingup);
        etPassword = view.findViewById(R.id.etPasswordSingup);
        etPasswordConfirm = view.findViewById(R.id.etPasswordConfirm);

        rbDoctor = view.findViewById(R.id.rbDoctor);
        rbPatient = view.findViewById(R.id.rbPatient);


        btnSingUp.setOnClickListener(v -> {
            if (isClick) {
                isClick = false;
                if (checkValid()) {
                    FirebaseAuth
                            .getInstance()
                            .createUserWithEmailAndPassword(etEmail.getText().toString(),
                                    etPassword.getText().toString())
                            .addOnSuccessListener(this.getActivity(), authResult -> {
                                String uid = authResult.getUser().getUid();
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("email", etEmail.getText().toString());
                                hashMap.put("isDoctor", rbDoctor.isChecked());
                                hashMap.put("isAvailable", false);
                                User user = new User(uid,
                                        etEmail.getText().toString(),
                                        false,
                                        rbDoctor.isChecked(),
                                        null,
                                        null);
                                FirebaseDatabase
                                        .getInstance()
                                        .getReference()
                                        .child("users")
                                        .child(uid)
                                        .setValue(user, (error, ref) -> {
                                            if (error != null) {
                                                isClick = true;
                                                Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                                            } else {
                                                if (rbDoctor.isChecked()) {
                                                    Intent doctorIntent = new Intent(getContext(), DoctorActivity.class);
                                                    startActivity(doctorIntent);
                                                } else {
                                                    Intent patientIntent = new Intent(getContext(), PatientActivity.class);
                                                    startActivity(patientIntent);
                                                }
                                            }
                                        });

                            }).addOnFailureListener(this.getActivity(), e -> {
                        isClick = true;
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }

    // check if all the input is valid
    private boolean checkValid() {
        if (etEmail.getText().toString().matches(emailPattern)) {
            return etPassword.getText().toString().length() >= 6 &&
                    etPassword.getText().toString().equals(etPasswordConfirm.getText().toString()) &&
                    (rbDoctor.isChecked() || rbPatient.isChecked());
        }
        return false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}