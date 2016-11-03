package com.hutchgroup.elog.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.hutchgroup.elog.R;
import com.hutchgroup.elog.adapters.UnidentifiedAdapter;
import com.hutchgroup.elog.beans.EventBean;
import com.hutchgroup.elog.common.Utility;
import com.hutchgroup.elog.db.DailyLogDB;
import com.hutchgroup.elog.db.EventDB;
import com.hutchgroup.elog.tasks.ModifiedSyncData;

import java.util.ArrayList;


public class ModifiedFragment extends Fragment {

    public int selectedItemIndex = -1;
    ListView lvData;
    ArrayList<EventBean> eventList;
    UnidentifiedAdapter adapter;
    int driverId = 0;
    Button btnConfirm, btnReject;

    ModifiedSyncData.PostTaskListener<Boolean> postTaskListener = new ModifiedSyncData.PostTaskListener<Boolean>() {
        @Override
        public void onPostTask(Boolean result) {
            if (result) {
                EventBind();
            } else {
                Utility.showErrorMessage(Utility.context);
            }
        }
    };

    public ModifiedFragment() {
        // Required empty public constructor
    }

    private void initialize(View view) {
        driverId = Utility.user1.isOnScreenFg() ? Utility.user1.getAccountId() : Utility.user2.getAccountId();
        lvData = (ListView) view.findViewById(R.id.lvData);
        btnConfirm = (Button) view.findViewById(R.id.btnConfirm);
        btnReject = (Button) view.findViewById(R.id.btnReject);
        btnReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (EventBean bean : eventList) {
                    if (bean.getChecked()) {
                        int eventId = EventDB.getEventId(driverId, bean.getCreatedDate(), 1);
                        EventDB.EventUpdate(eventId, 1, driverId, bean.getDailyLogId());

                        eventId = bean.get_id();
                        EventDB.EventUpdate(eventId, 4, driverId, bean.getDailyLogId());
                        DailyLogDB.DailyLogCertifyRevert(driverId, bean.getDailyLogId());
                    }
                }
                EventBind();
            }
        });

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (EventBean bean : eventList) {
                    if (bean.getChecked()) {
                        int eventId = EventDB.getEventId(driverId, bean.getCreatedDate(), 1);
                        EventDB.EventUpdate(eventId, 2, driverId, bean.getDailyLogId());

                        eventId = bean.get_id();
                        //  EventDB.EventCopy(eventId, bean.getEventRecordOrigin(), 1, driverId, bean.getDailyLogId());
                        EventDB.EventUpdate(eventId, 1, driverId, bean.getDailyLogId());
                        DailyLogDB.DailyLogCertifyRevert(driverId, bean.getDailyLogId());

                    }
                }
                EventBind();
            }
        });

        lvData.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
        EventBind();
        new ModifiedSyncData(postTaskListener).execute();
    }

    private void EventBind() {
        selectedItemIndex = -1;
        eventList = EventDB.EventEditRequestedGet(driverId);
        adapter = new UnidentifiedAdapter(R.layout.unidentified_row_layout, eventList);
        lvData.setAdapter(adapter);

    }

    public static ModifiedFragment newInstance() {
      return new ModifiedFragment();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_modified, container, false);
        initialize(view);
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

}
