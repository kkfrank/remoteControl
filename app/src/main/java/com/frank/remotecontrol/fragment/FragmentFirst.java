package com.frank.remotecontrol.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.frank.remotecontrol.MainActivity;
import com.frank.remotecontrol.MessageEvent;
import com.frank.remotecontrol.R;
import com.frank.remotecontrol.utils.Constants;
import com.frank.remotecontrol.utils.WifiHelp;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this com.frank.remotecontrol.fragment must implement the
 * {@link FragmentFirst.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FragmentFirst#newInstance} factory method to
 * create an instance of this com.frank.remotecontrol.fragment.
 */
public class FragmentFirst extends Fragment  implements View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the com.frank.remotecontrol.fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private View view;
    // TODO: Rename and change types of parameters
    private String mParam1,mParam2;

    private TextView txReceive;
    private EditText edIP, edPort, edData, edTimer;
    private Button btnLightOn, btnLightOff, btnLightQuick, btnLightSlow,bnConnect,btnTimer;

    private Handler handler = new Handler(Looper.getMainLooper());
    private MainActivity activity;
    private OnFragmentInteractionListener mListener;

    public FragmentFirst() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this com.frank.remotecontrol.fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of com.frank.remotecontrol.fragment FragmentFirst.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentFirst newInstance(String param1, String param2) {
        FragmentFirst fragment = new FragmentFirst();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this com.frank.remotecontrol.fragment
        view= inflater.inflate(R.layout.fragment_first, container, false);
        activity = (MainActivity)getActivity();
        initView();
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
//    public void onButtonPressed(Uri uri) {
//        if (mListener != null) {
//            mListener.onFragmentInteraction(uri);
//        }
//    }

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
    public void onDetach() {
        super.onDetach();
        mListener = null;
        EventBus.getDefault().unregister(this);
    }

    /**
     * This interface must be implemented by activities that contain this
     * com.frank.remotecontrol.fragment to allow an interaction in this com.frank.remotecontrol.fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(int id);
    }

    private void initView() {

        view.findViewById(R.id.bn_send).setOnClickListener(this);
        bnConnect = (Button) view.findViewById(R.id.bn_connect);
        bnConnect.setOnClickListener(this);

        edIP = (EditText) view.findViewById(R.id.ed_ip);
        edPort = (EditText) view.findViewById(R.id.ed_port);
        edData = (EditText) view.findViewById(R.id.ed_dat);
        txReceive = (TextView) view.findViewById(R.id.tx_receive);
        txReceive.setOnClickListener(this);

        btnLightOn = (Button) view.findViewById(R.id.lighton);
        btnLightOn.setOnClickListener(this);

        btnLightOff = (Button) view.findViewById(R.id.lightoff);
        btnLightOff.setOnClickListener(this);
//
//        btnLightQuick = (Button) view.findViewById(R.id.lightquick);
//        btnLightQuick.setOnClickListener(this);
//
//        btnLightSlow = (Button) view.findViewById(R.id.lightslow);
//        btnLightSlow.setOnClickListener(this);

        edTimer = (EditText) view.findViewById(R.id.timer);
        btnTimer = (Button) view.findViewById(R.id.btn_timer);
        btnTimer.setOnClickListener(this);

//        edSaveName = (EditText) view.findViewById(R.id.save_name);
//        btnSave = (Button) view.findViewById(R.id.save_btn);
//        btnRead = (Button) view.findViewById(R.id.read_btn);
//
//        btnSave.setOnClickListener(this);
//        btnRead.setOnClickListener(this);

        String ip  = WifiHelp.getWifiRouteIPAddress(activity);
        //ip="10.86.35.77";
        //ip="192.168.1.103";
        edIP.setText(ip);
        edPort.setText(Integer.toString(8899));


        refreshUI(activity.socketService!=null && activity.socketService.isConnected());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bn_connect:
                connect();
                //sendStr();
                break;
            case R.id.bn_send:
                sendStr();
                break;
            case R.id.tx_receive:
                clear();
                break;
            case R.id.lighton:
                //  sendBytes(new byte[]{0x01, 0x55});
                break;
            case R.id.lightoff:
                //  sendBytes(new byte[]{0x00, 0x55});
                break;
            case R.id.lightquick:
                //  sendBytes(new byte[]{0x03, 0x55});
                break;
            case R.id.lightslow:
                //   sendBytes(new byte[]{0x02, 0x55});
                break;
            case R.id.btn_timer:
               // startTimeFn();
                break;
        }
    }
    public void connect(){
        if(activity.socketService==null || !activity.socketService.isConnected()){
            String ip =edIP.getText().toString();
            String port =edPort.getText().toString();
            activity.bindSocketService(ip,port);
        }

    }

    public void sendStr() {
        try {
            String data = edData.getText().toString();
             activity.socketService.sendOrder(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 清空接收框
     */
    private void clear() {
        new AlertDialog.Builder(activity).setTitle("确认清除?")
                .setNegativeButton("取消", null)
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        txReceive.setText("");
                    }
                }).show();
    }

    private void refreshUI(final boolean isConnected) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                edPort.setEnabled(!isConnected);
                edIP.setEnabled( !isConnected);
                bnConnect.setText(isConnected ? "断开" : "连接");
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        /* Do something */
        switch (event.getTag()){
            case Constants.CONNET_SUCCESS:
                refreshUI(true);
                break;
            case Constants.UNCONNET:
                refreshUI(false);
                break;
            case Constants.RECEIVE_SUCCESS:
                String str = event.getMsg();
                txReceive.append(str);
                break;
            default:
                break;
        }
    };
}
