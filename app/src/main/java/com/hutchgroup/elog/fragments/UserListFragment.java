package com.hutchgroup.elog.fragments;

import android.app.Service;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.hutchgroup.elog.R;
import com.hutchgroup.elog.beans.MessageBean;
import com.hutchgroup.elog.beans.UserBean;
import com.hutchgroup.elog.common.ChatClient;
import com.hutchgroup.elog.common.Utility;
import com.hutchgroup.elog.db.MessageDB;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class UserListFragment extends Fragment implements ChatClient.ChatMessageInterface {

    private EditText etSearch;
    ListView lvUser;
    ArrayList<UserBean> listUser;
    UserAdapter adapter;
    ImageButton fab;
    ImageView imgBack;

    private OnFragmentInteractionListener mListener;

    public UserListFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static UserListFragment newInstance() {
        UserListFragment fragment = new UserListFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_list, container, false);
        initialize(view);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Utility.hideKeyboard(getActivity(), getActivity().getCurrentFocus());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        ChatClient.mListener = null;
    }

    private void initialize(View view) {
        ChatClient.mListener = this;
        etSearch = (EditText) view.findViewById(R.id.etSearch);
        fab = (ImageButton) view.findViewById(R.id.fab);
        imgBack = (ImageView) view.findViewById(R.id.imgBack);
        etSearch.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                if (!s.equals("")) { //do your work here }

                }

            }

            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            public void afterTextChanged(Editable s) {
                String search = s.toString();
                getUserList(search);

            }
        });
        lvUser = (ListView) view.findViewById(R.id.lvUser);
        lvUser.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                UserBean bean = listUser.get(position);
                mListener.navigateToMessage(bean.getAccountId(), bean.getFirstName() + " " + bean.getLastName());
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetSearchOption(true);
            }
        });


        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetSearchOption(false);
            }
        });

        getUserList("");
        mListener.onMessageRead();
    }

    private void SetSearchOption(boolean flag) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Service.INPUT_METHOD_SERVICE);
        if (flag) {

            fab.setVisibility(View.GONE);
            imgBack.setVisibility(View.VISIBLE);
            etSearch.setVisibility(View.VISIBLE);
            etSearch.requestFocus();
            imm.showSoftInput(etSearch, 0);
        } else {

            fab.setVisibility(View.VISIBLE);
            imgBack.setVisibility(View.GONE);
            etSearch.setVisibility(View.GONE);
            etSearch.setText("");
            imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
        }
    }

    private void updateOnline(int userId, boolean onlineFg) {
        for (UserBean bean : listUser) {
            if (userId == bean.getAccountId()) {
                bean.setIsOnline(onlineFg);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void updateOnline(int userId, int userId2, boolean onlineFg) {

        for (UserBean bean : listUser) {
            if (userId == bean.getAccountId() || userId2 == bean.getAccountId()) {
                bean.setIsOnline(onlineFg);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void updateMessage(int userId, String message, String messageDate) {

        for (UserBean bean : listUser) {
            if (userId == bean.getAccountId()) {
                bean.setCurrentMessage(message);
                bean.setMessageDateTime(Utility.convertUTCToLocalDateTime(messageDate));
                int unreadCount = bean.getUnreadCount() + 1;
                bean.setUnreadCount(unreadCount);
            }
        }
        Collections.sort(listUser, UserBean.dateDesc);
        adapter.notifyDataSetChanged();
    }

    private void getUserList(String search) {
        listUser = MessageDB.getUserList(search);
        adapter = new UserAdapter(R.layout.user_row_layout, listUser);
        lvUser.setAdapter(adapter);
    }

    @Override
    public void onMessageUpdated(final MessageBean obj, final int flag) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (flag == ChatClient.ONLINE)
                    updateOnline(obj.getCreatedById(), true);
                else if (flag == ChatClient.OFFLINE)
                    updateOnline(obj.getCreatedById(), false);
                else if (flag == ChatClient.OFFLINE2) {
                    String arrUser[] = obj.getMessage().split(",");
                    if (arrUser.length == 2) {
                        int user1 = Integer.parseInt(arrUser[0]);
                        int user2 = Integer.parseInt(arrUser[1]);
                        updateOnline(user1, user2, false);
                    }
                } else if (flag == ChatClient.MESSAGE) {
                    updateMessage(obj.getCreatedById(), obj.getMessage(), obj.getMessageDate());
                }
            }
        });
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);

        void navigateToMessage(int userId, String userName);

        void onMessageRead();
    }

    /**
     * Created by Deepak.Sharma on 4/14/2016.
     */
    public class UserAdapter extends ArrayAdapter<UserBean> {
        ArrayList<UserBean> data;

        public UserAdapter(int resource,
                           ArrayList<UserBean> data) {
            super(Utility.context, resource, data);
            this.data = data;
        }

        @Override
        public View getView(final int position, View convertView,
                            ViewGroup parent) {
            ViewHolderItem viewHolder;
            if (convertView == null || convertView.getTag() == null) {

                LayoutInflater inflater = (LayoutInflater) Utility.context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(
                        R.layout.user_row_layout, parent,
                        false);
                viewHolder = new ViewHolderItem();
                viewHolder.tvUserName = (TextView) convertView.findViewById(R.id.tvDisplayName);
                viewHolder.tvCurrentMessage = (TextView) convertView.findViewById(R.id.tvCurrentMessage);
                viewHolder.tvMessageTime = (TextView) convertView.findViewById(R.id.tvMessageTime);
                viewHolder.tvUnreadCount = (TextView) convertView.findViewById(R.id.tvUnreadCount);
                viewHolder.imgOnline = (ImageView) convertView.findViewById(R.id.imgOnline);
                viewHolder.imgOffline = (ImageView) convertView.findViewById(R.id.imgOffline);
            } else {
                viewHolder = (ViewHolderItem) convertView.getTag();
            }

            UserBean bean = data.get(position);

            String messageDate = Utility.getDate(bean.getMessageDateTime());
            String messageTime = Utility.getTimeHHMM(bean.getMessageDateTime());
            viewHolder.tvUserName.setText(bean.getFirstName() + " " + bean.getLastName());
            viewHolder.tvCurrentMessage.setText(bean.getCurrentMessage());

            int unreadCount = bean.getUnreadCount();
            if (unreadCount > 0) {
                viewHolder.tvUnreadCount.setText(unreadCount + "");
                viewHolder.tvUnreadCount.setVisibility(View.VISIBLE);
            } else {
                viewHolder.tvUnreadCount.setVisibility(View.GONE);
            }
            if (messageDate.equals(Utility.getCurrentDate())) {
                viewHolder.tvMessageTime.setText(messageTime);
            } else if (messageDate.equals(Utility.sdf.format(Utility.addDays(new Date(), -1))))
                viewHolder.tvMessageTime.setText("Yesterday");
            else {
                viewHolder.tvMessageTime.setText(messageDate);
            }


            if (bean.isOnline()) {
                viewHolder.imgOnline.setVisibility(View.VISIBLE);
                viewHolder.imgOffline.setVisibility(View.GONE);
            } else {

                viewHolder.imgOnline.setVisibility(View.GONE);
                viewHolder.imgOffline.setVisibility(View.VISIBLE);
            }
            return convertView;
        }
    }

    static class ViewHolderItem {
        TextView tvUserName, tvCurrentMessage, tvMessageTime, tvUnreadCount;
        ImageView imgOffline, imgOnline;
    }


}
