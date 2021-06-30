package mosh.bo.doctorapp.patient.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;

import java.util.List;

import mosh.bo.doctorapp.R;
import mosh.bo.doctorapp.models.User;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.ViewHolder> {


    public List<User> patients;

    public AppointmentAdapter(List<User> patients) {
        this.patients = patients;
    }




    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.appointmen_item, parent, false);
        return new AppointmentAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(AppointmentAdapter.ViewHolder holder, int position) {
        User patient = patients.get(position);
        holder.tvNumber.setText((position == 0) ? "now" : "" + position);
        holder.tvName.setText(patient.getEmail());
    }

    @Override
    public int getItemCount() {
        return patients.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNumber;
        TextView tvName;
        public ViewHolder(View itemView) {
            super(itemView);
            tvNumber = itemView.findViewById(R.id.tvNumber);
            tvName = itemView.findViewById(R.id.tvName);
        }
    }
}
