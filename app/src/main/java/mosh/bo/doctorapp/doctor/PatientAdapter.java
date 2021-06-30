package mosh.bo.doctorapp.doctor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import mosh.bo.doctorapp.R;
import mosh.bo.doctorapp.models.User;
import mosh.bo.doctorapp.patient.adapters.AppointmentAdapter;


public class PatientAdapter extends RecyclerView.Adapter<PatientAdapter.ViewHolder> {

    public List<User> patients;

    public PatientAdapter(List<User> patients) {
        this.patients = patients;
    }




    @Override
    public ViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.appointmen_item, parent, false);
        return new PatientAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder( ViewHolder holder, int position) {
        if(patients.size() > 1) {
            User patient = patients.get(position + 1);
            holder.tvNumber.setText((position == 1) ? "now" : "" + (position - 1));
            holder.tvName.setText(patient.getEmail());
        }
    }

    @Override
    public int getItemCount() {
        return (patients.size() > 0) ? patients.size() - 1 : 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNumber;
        TextView tvName;
        public ViewHolder( View itemView) {
            super(itemView);
            tvNumber = itemView.findViewById(R.id.tvNumber);
            tvName = itemView.findViewById(R.id.tvName);
        }
    }
}
