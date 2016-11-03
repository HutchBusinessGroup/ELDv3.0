package com.hutchgroup.elog.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hutchgroup.elog.R;
import com.hutchgroup.elog.beans.EventBean;
import com.hutchgroup.elog.common.LogFile;
import com.hutchgroup.elog.common.Utility;

import java.util.List;


public class DiagnosticMalfunctionAdapter extends ArrayAdapter<EventBean> {
    //private ItemClickListener mListener;

    private final Context context;

    private List<EventBean> listEvents;

    View selectedView = null;

    static class ViewHolder {
        public TextView tvEventDescription;
        public TextView tvInformation1;
        public TextView tvInformation2;
        public TextView tvInformation3;
        public TextView tvInformation4;
        public TextView tvTime;
        public Button butEventIcon;
        public LinearLayout infoLayout;
    }

    public DiagnosticMalfunctionAdapter(Context context, List<EventBean> values) {
        super(context, -1, values);
        this.context = context;
        this.listEvents = values;
    }

    public void changeItems(List<EventBean> list) {
        listEvents.clear();
        listEvents.addAll(list);

        notifyDataSetChanged();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        try {
            // reuse views
            if (rowView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                rowView = inflater.inflate(R.layout.diagnostic_item, null);
                // configure view holder
                ViewHolder viewHolder = new ViewHolder();
                viewHolder.tvEventDescription = (TextView) rowView.findViewById(R.id.tvEventDescription);
                viewHolder.tvInformation1 = (TextView) rowView.findViewById(R.id.tvInformation1);
                viewHolder.tvInformation2 = (TextView) rowView.findViewById(R.id.tvInformation2);
                viewHolder.tvInformation3 = (TextView) rowView.findViewById(R.id.tvInformation3);
                viewHolder.tvInformation4 = (TextView) rowView.findViewById(R.id.tvInformation4);
                viewHolder.tvTime = (TextView) rowView.findViewById(R.id.tvTime);
                viewHolder.butEventIcon = (Button) rowView.findViewById(R.id.butEventIcon);
                viewHolder.infoLayout = (LinearLayout) rowView.findViewById(R.id.layout_parent);
                rowView.setTag(viewHolder);
            }

            ViewHolder holder = (ViewHolder) rowView.getTag();

            EventBean eventBean = listEvents.get(position);
            String diagnosticCode = eventBean.getDiagnosticCode();
            String description = eventBean.getEventCodeDescription();

            holder.tvInformation1.setText(description);
            holder.tvEventDescription.setVisibility(View.GONE);
            holder.tvInformation2.setVisibility(View.GONE);
            holder.tvInformation3.setVisibility(View.GONE);
            holder.tvInformation4.setVisibility(View.GONE);

            String dateTime = Utility.getTimeByFormat(listEvents.get(position).getEventDateTime());
            holder.tvTime.setText(dateTime);


            String statusText = diagnosticCode;
            holder.butEventIcon.setText(statusText);
        } catch (Exception e) {
            LogFile.write(DiagnosticMalfunctionAdapter.class.getName() + "::getView Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }

        return rowView;
    }

//    public interface ItemClickListener {
//        void editEvent(EventBean bean);
//    }
}
