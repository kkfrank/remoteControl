package com.frank.remotecontrol.fragment;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;
import com.frank.remotecontrol.ArchiveWindow;
import com.frank.remotecontrol.MainActivity;
import com.frank.remotecontrol.MessageEvent;
import com.frank.remotecontrol.R;
import com.frank.remotecontrol.model.Massage;
import com.frank.remotecontrol.patternview.PatternView;
import com.frank.remotecontrol.patternview.cells.Cell;
import com.frank.remotecontrol.patternview.cells.CellManager;
import com.frank.remotecontrol.patternview.utils.CellUtils;
import com.frank.remotecontrol.utils.Constants;
import com.frank.remotecontrol.utils.SPUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FragmentSecond.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FragmentSecond#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentSecond extends Fragment implements View.OnClickListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private MainActivity activity;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private View view;
    private PatternView patternView;
    private String patternString;


    private EditText edSaveName;
    private Button btnSave,btnRead;


    private OnFragmentInteractionListener mListener;
    private PatternLockView mPatternLockView;
    public FragmentSecond() {
        // Required empty public constructor
    }
    private PatternLockViewListener mPatternLockViewListener = new PatternLockViewListener() {
        @Override
        public void onStarted() {
            Log.d(getClass().getName(), "Pattern drawing started");
        }

        @Override
        public void onProgress(List<PatternLockView.Dot> progressPattern) {
            Log.d(getClass().getName(), "Pattern progress: " +
                    PatternLockUtils.patternToString(mPatternLockView, progressPattern));
        }

        @Override
        public void onComplete(List<PatternLockView.Dot> pattern) {
            Log.d(getClass().getName(), "Pattern complete: " +
                    PatternLockUtils.patternToString(mPatternLockView, pattern));
        }

        @Override
        public void onCleared() {
            Log.d(getClass().getName(), "Pattern has been cleared");
        }
    };
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentSecond.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentSecond newInstance(String param1, String param2) {
        FragmentSecond fragment = new FragmentSecond();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        EventBus.getDefault().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        activity = (MainActivity)getActivity();
        view= inflater.inflate(R.layout.fragment_second, container, false);
        initView();
//        mPatternLockView = (PatternLockView)(view.findViewById(R.id.pattern_lock_view));
//        mPatternLockView.setDotCount(2);
//        mPatternLockView.addPatternLockListener(mPatternLockViewListener);
        patternView = (PatternView) (view.findViewById(R.id.patternView));
        patternView.setTactileFeedbackEnabled(false);
       // Toast.makeText(getApplicationContext(), "ENTER PATTERN", Toast.LENGTH_LONG).show();
//        patternView.setPathColor(Color.BLACK);
//        patternView.setDotColor(Color.BLACK);
//        patternView.setCircleColor(Color.BLACK);
        patternView.setOnPatternDetectedListener(new PatternView.OnPatternDetectedListener() {


            @Override
            public void onPatternDetected() {
                //Toast.makeText(getActivity(),patternView.getPatternString() , Toast.LENGTH_SHORT).show();
                patternString =new Gson().toJson(patternView.getPattern()) ;
                //int aa [] =patternView.patternToIntArray();
                // patternString = Arrays.toString(patternView.patternToIntArray());
               // Toast.makeText(getActivity(),patternString, Toast.LENGTH_SHORT).show();
//                Toast.makeText(getActivity(),patternView.patternToIntArray().toString() , Toast.LENGTH_SHORT).show();
//                if (patternString == null) {
//                    patternString = patternView.getPatternString();
//                    patternView.clearPattern();
//                    return;
//                }
//                if (patternString.equals(patternView.getPatternString())) {
//                    Toast.makeText(getApplicationContext(), "PATTERN CORRECT", Toast.LENGTH_SHORT).show();
//                    return;
//                }
                //Toast.makeText(getApplicationContext(), "PATTERN NOT CORRECT", Toast.LENGTH_SHORT).show();
//                patternView.clearPattern();
            }
        });

//        patternView.setPattern(PatternView.DisplayMode.Correct,
//                CellUtils.intArrayToPattern(ss.getSerializedPattern(), patternView.getCellManager()));
        return view;
    }

    private void initView(){
        edSaveName = (EditText) view.findViewById(R.id.save_name);
        btnSave = (Button) view.findViewById(R.id.save_btn);
        btnRead = (Button) view.findViewById(R.id.read_btn);

        btnSave.setOnClickListener(this);
        btnRead.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.save_btn:
                  saveCustomModel();
                break;
            case R.id.read_btn:
                  readCustomModel();
                break;
        }
    }

    private void saveCustomModel(){
        String name = edSaveName.getText().toString();
        if("".equals(name)){
            Toast.makeText(activity, "名字不能为空",Toast.LENGTH_SHORT).show();
            return;
        }
        if(patternString==null){
            Toast.makeText(activity, "顺序不能为空",Toast.LENGTH_SHORT).show();
            return;
        }
        Massage massage = new Massage();
        massage.setName(name);
        massage.setOrders(patternString);

        String strJson =(String) SPUtils.get(activity,"name","");
        Map<String,Massage> data = new Gson().fromJson(strJson, new TypeToken<Map<String,Massage>>() {}.getType());
        if(data ==null){
            data = new HashMap<String, Massage>();
        }

        data.put(name,massage);
        SPUtils.put(activity,"name",new Gson().toJson(data));
    }
    private void readCustomModel(){
        String strJson =(String)SPUtils.get(activity,"name","");
        Map<String,Massage> data = new Gson().fromJson(strJson, new TypeToken<Map<String,Massage>>() {}.getType());
        if(data==null || data.size()==0){
            Toast.makeText(activity, "没有存储数据",Toast.LENGTH_SHORT).show();
            return;
        }
        ArrayList<Massage> list = new ArrayList<Massage>(data.values());
        ArchiveWindow window = new ArchiveWindow(activity,list);
         window.showAtBottom(btnRead);
       //  window.showAtLocation(view,100,100,100);
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
    public void onDetach() {
        super.onDetach();
        mListener = null;
        EventBus.getDefault().unregister(this);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        /* Do something */
        switch (event.getTag()){
            case Constants.LOAD_MASSAGE:
                Massage massage =(Massage)event.getData();
                String patternString = massage.getOrders();
               // int [] a = string2IntArr(patternString);
//        patternView.setPattern(PatternView.DisplayMode.Correct,
                //CellManager manager = patternView.getCellManager();
               //List l =  CellUtils.intArrayToPattern(a, manager);
                //patternView.setPattern(PatternView.DisplayMode.Correct,CellUtils.intArrayToPattern(a, patternView.getCellManager()));
                List<Cell> list =  new Gson().fromJson(patternString, new TypeToken<List<Cell>>() {}.getType());
                patternView.setPattern(PatternView.DisplayMode.Correct,list);
                break;
            default:
                break;
        }
    };

    public int [] string2IntArr(String arr){
        String[] items = arr.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\\s", "").split(",");

        int[] results = new int[items.length];

        for (int i = 0; i < items.length; i++) {
            try {
                results[i] = Integer.parseInt(items[i]);
            } catch (NumberFormatException nfe) {
                //NOTE: write something here if you need to recover from formatting errors
            };
        }
        return results;
    }
}
