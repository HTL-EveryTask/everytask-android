package com.example.everytask.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.everytask.AppointmentEditActivity
import com.example.everytask.databinding.RowAppointmentsBinding
import com.example.everytask.fragments.AppointmentsFragment
import com.example.everytask.models.response.appointments.Appointment

class AppointmentAdapter(val appointmentList: List<Appointment>, val appointmentsFragment: AppointmentsFragment): RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder>() {

    class AppointmentViewHolder(
        val appointmentsBinding: RowAppointmentsBinding,
        val appointmentsFragment: AppointmentsFragment,
    ): RecyclerView.ViewHolder(appointmentsBinding.root){
        fun bind(appointment: Appointment){
            appointmentsBinding.tvAppointmentTitle.text = appointment.title
            appointmentsBinding.tvAppointmentDate.text = appointment.start_time
            appointmentsBinding.flBtnDeleteContainer.setOnClickListener {
                appointmentsFragment.showDeleteAlert(appointment)
            }
            appointmentsBinding.llAppointmentTextContainer.setOnClickListener {
                val intent = Intent(appointmentsFragment.requireContext(), AppointmentEditActivity::class.java)
                intent.putExtra("APPOINTMENT", appointment)
                startActivity(appointmentsFragment.requireContext(), intent, null)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        return AppointmentViewHolder(
            RowAppointmentsBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            appointmentsFragment
        )
    }

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        val appointment = appointmentList[position]
        holder.bind(appointment)
    }

    override fun getItemCount(): Int {
        return appointmentList.size
    }
}