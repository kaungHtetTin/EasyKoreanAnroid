package com.calamus.easykorean.fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.calamus.easykorean.R;
import com.calamus.easykorean.SearchingActivity;
import com.calamus.easykorean.TeacherActivity;
import com.calamus.easykorean.adapters.ConservationAdapter;
import com.calamus.easykorean.models.ConservationModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;


public class ChatOne extends Fragment {
    View v;
    RecyclerView recyclerView;
    private SwipeRefreshLayout swipe;
    private final ArrayList<ConservationModel> conservationList = new ArrayList<>();
    ConservationAdapter adapter;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    SQLiteDatabase db;
    String dbdir;
    final String dbName="conservation.db";
    String dbPath;
    String phone;
    SharedPreferences sharedPreferences;
    ConservationModel Teacher;
    ConservationModel Developer;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v=inflater.inflate(R.layout.fragment_chat_one,container,false);
        setHasOptionsMenu(true);
        dbdir= requireActivity().getFilesDir().getPath()+"/databases/";
        dbPath=dbdir+dbName;
        db=SQLiteDatabase.openDatabase(dbPath,null,SQLiteDatabase.OPEN_READWRITE);
        sharedPreferences=getActivity().getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        phone=Long.parseLong(sharedPreferences.getString("phone",null))+"";


        Teacher=new ConservationModel("10000","Teacher Group","https://www.calamuseducation.com/uploads/profiles/calamus.png","Ask a teacher for lessons",null,"10000","",0);
        Developer=new ConservationModel("10000","Developer Group","https://www.calamuseducation.com/appthumbs/kommmainicon.png","Ask a developer for help",null,"10000","",0);
        conservationList.add(0,Teacher);
        conservationList.add(1,Developer);

        setUpView();

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // checking for type intent filter
                if (intent.getAction().equals("Conservation")) {

                    fetchFromConservationTable();

                }
            }
        };


        return v;
    }

    private void setUpView(){
        recyclerView=v.findViewById(R.id.recycler);
        swipe=v.findViewById(R.id.swipe);


        LinearLayoutManager lm = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(lm);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        adapter=new ConservationAdapter(getActivity(),conservationList);
        recyclerView.setAdapter(adapter);



        conservationList.add(0,Teacher);
        conservationList.add(1,Developer);
        fetchFromConservationTable();
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipe.setRefreshing(false);
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();


        fetchFromConservationTable();

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter("Conservation"));

    }


    private void fetchConverstionFromTeacher(){

        DatabaseReference dbc= FirebaseDatabase.getInstance().getReference().child("korea").child("Teacher").child("Conservation");
        dbc.child(phone).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.child("message").getValue()!=null){
                    long seen   =(Long)snapshot.child("seen").getValue();
                    String msg=(String)snapshot.child("message").getValue();
                    Teacher.setMessage(msg);
                    Teacher.setSeen((int) seen);
                    Log.e("Status: ",seen+"");

                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) { }
        });

        DatabaseReference dbD= FirebaseDatabase.getInstance().getReference().child("korea").child("Developer").child("Conservation");
        dbD.child(phone).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.child("message").getValue()!=null){
                    long seen   =(Long)snapshot.child("seen").getValue();
                    String msg=(String)snapshot.child("message").getValue();

                   Developer.setSeen((int)seen);
                   Developer.setMessage(msg);
                   adapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) { }


        });


    }



    private void fetchFromConservationTable(){
        conservationList.clear();
        conservationList.add(0, Teacher);
        conservationList.add(1,Developer);
        fetchConverstionFromTeacher();
        String query="SELECT*FROM Conservations WHERE my_id="+phone+" ORDER BY time DESC;";
        Cursor cursor=db.rawQuery(query,null);

        if(cursor.moveToFirst()){

            for(int i=0;i<cursor.getCount();i++){

                String fri_Id=cursor.getString(1);
                String friName=cursor.getString(2);
                String friImage=cursor.getString(3);
                String msgBody=cursor.getString(4);
                String time=cursor.getString(5);
                String senderId=cursor.getString(6);
                int seen=cursor.getInt(7);
                String token=cursor.getString(8);

                Log.e("FriId : ",fri_Id);
                conservationList.add(new ConservationModel(fri_Id,friName,friImage,msgBody,time,senderId,token,seen));

                cursor.moveToNext();

            }

            adapter.notifyDataSetChanged();
        }
    }

    public void onCreateOptionsMenu(Menu menu, @NotNull MenuInflater inflater){
        menu.add("SEARCH")
                .setIcon(R.drawable.ic_search)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);


        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull @NotNull MenuItem item) {
        if (item.getTitle().toString().equals("SEARCH")){
            Intent intent=new Intent(getActivity(), SearchingActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}
