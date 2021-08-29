package com.calamus.easykorean.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import com.calamus.easykorean.ClassRoomActivity;
import com.calamus.easykorean.MyDiscussionActivity;
import com.calamus.easykorean.PhotoActivity;
import com.calamus.easykorean.R;
import com.calamus.easykorean.SavePostActivity;
import com.calamus.easykorean.SavedVideoActivity;
import com.calamus.easykorean.SettingActivity;
import com.calamus.easykorean.SplashScreenActivity;
import com.calamus.easykorean.UpdateActivity;
import com.calamus.easykorean.WebSiteActivity;
import com.calamus.easykorean.app.AppHandler;
import com.calamus.easykorean.app.Routing;
import org.jetbrains.annotations.NotNull;
import java.util.Objects;
import me.myatminsoe.mdetect.MDetect;
import static com.calamus.easykorean.app.AppHandler.setMyanmar;

public class FragmentFive extends Fragment {

    private View v;
    String imagePath;
    SharedPreferences sharedPreferences1;
    Long checkUpdate;
    final int callRequestCode = 123;
    RecyclerView menuRecycler;


    String[] title = {
            "Chat with\n"+"classmates",
            "Account Setting",
            "Like us\n"+"on facebook",
            "Pay For VIP",
            "Downloaded Videos",
            "Saved Posts",
            "Check Update",
            "Share",
            "Rate Us",
            "Call Center",
            "Sign Out"
    };

    String userName,phone;
    TextView tv_header,tv_vip,tv_phone;
    boolean isVip;
    ImageView iv;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_five, container, false);

        sharedPreferences1 = getActivity().getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        MDetect.INSTANCE.init(getActivity());

        checkUpdate=sharedPreferences1.getLong("checkUpdate",1);
        userName=sharedPreferences1.getString("Username",null);
        isVip=sharedPreferences1.getBoolean("isVIP",false);
        imagePath=sharedPreferences1.getString("imageUrl",null);
        phone=sharedPreferences1.getString("phone",null);

        menuRecycler=v.findViewById(R.id.menu_recycler);
        tv_header=v.findViewById(R.id.tv_header);
        tv_vip=v.findViewById(R.id.tv_vip);
        iv=v.findViewById(R.id.iv_header);
        tv_phone=v.findViewById(R.id.tv_phone);
        tv_phone.setText(phone);
        v.findViewById(R.id.layout_header).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getActivity(), MyDiscussionActivity.class);
                intent.putExtra("userId",phone);
                intent.putExtra("userName","My Discussion");
                startActivity(intent);
            }
        });

        StaggeredGridLayoutManager gm = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);


        menuRecycler.setLayoutManager(gm);
        menuRecycler.setItemAnimator(new DefaultItemAnimator());
        MyAdapter adapter=new MyAdapter(title);
        menuRecycler.setAdapter(adapter);

        if(userName!=null)tv_header.setText(setMyanmar(userName));
        if(isVip)tv_vip.setVisibility(View.VISIBLE);

        if(imagePath!=null) AppHandler.setPhotoFromRealUrl(iv,imagePath);
        iv.setOnClickListener(v -> {

            Intent intent=new Intent(getActivity(), PhotoActivity.class);
            intent.putExtra("image",imagePath);
            intent.putExtra("des","");
            startActivity(intent);


        });

        return v;
    }



    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.Holder> {

        private final String [] data;
        private final LayoutInflater mInflater;
        int[] resId = {
                R.drawable.ic_chat,
                R.drawable.ic_baseline_settings_24,
                R.drawable.ic_facebook,
                R.drawable.ic_attach_money_black_24dp,
                R.drawable.ic_downloaded_video,
                R.drawable.ic_save,
                R.drawable.ic_cloud_download_black_24dp,
                R.drawable.ic_share_black_24dp,
                R.drawable.ic_rate_review_black_24dp,
                R.drawable.ic_phone_green,
                R.drawable.ic_logout
        };

        public MyAdapter( String [] data) {
            this.data = data;
            this.mInflater = LayoutInflater.from(getActivity());
        }

        @Override
        public int getItemCount() {
            return data.length;
        }

        @NotNull
        @Override
        public MyAdapter.Holder onCreateViewHolder(@NotNull ViewGroup parent, int p2) {
            View view;
            if(p2>3){
                view = mInflater.inflate(R.layout.list_drawer_linear, parent, false);
            }else{
                view = mInflater.inflate(R.layout.list_drawer, parent, false);
            }
            return new MyAdapter.Holder(view);
        }

        @Override
        public void onBindViewHolder(@NotNull MyAdapter.Holder holder, final int i) {
            holder.tv.setText(data[i]);
            holder.iv.setBackgroundResource(resId[i]);

           if(i>3){
               StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
               layoutParams.setFullSpan(true);
           }
        }

        public class Holder extends RecyclerView.ViewHolder {
            TextView tv;
            ImageView iv;

            public Holder(View view) {
                super(view);
                tv=view.findViewById(R.id.list_tv);
                iv=view.findViewById(R.id.list_iv);
                view.setOnClickListener(v -> setting(getAdapterPosition()));

            }
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }
    }

    public void setting(int position){
        switch (position) {
            case 0:{
                Intent intent=new Intent(getActivity(), ClassRoomActivity.class);
                intent.putExtra("action","");
                startActivity(intent);
                break;
            }

            case 1:{
                Intent intent=new Intent(getActivity(), SettingActivity.class);
                startActivity(intent);
                break;
            }

            case 2:{
                openFacebookPage("easykoreancalamus");
                break;
            }

            case 3: {
                Intent i = new Intent(getActivity(), WebSiteActivity.class);
                i.putExtra("link", Routing.PAYMENT);
                startActivity(i);
                break;
            }
            case 4: {

                Intent intent=new Intent(getActivity(), SavedVideoActivity.class);
                startActivity(intent);

                break;
            }
            case 5:

                Intent i = new Intent(getActivity(), SavePostActivity.class);
                startActivity(i);
                break;
            case 6:
                Intent intent=new Intent(getActivity(), UpdateActivity.class);
                startActivity(intent);
                break;
            case 7:
                Intent shareingIntent = new Intent(Intent.ACTION_SEND);
                shareingIntent.setType("text/plain");
                String shareBody = "https://play.google.com/store/apps/details?id=" + requireActivity().getPackageName();
                shareingIntent.putExtra(Intent.EXTRA_SUBJECT, "Try out this best Language App.");
                shareingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(shareingIntent, "Share via"));

                break;
            case 8:
                goPlayStore();
                break;
            case 9:

                if (isPermissionGranted()) {
                    callCenter("09979638384");
                } else {
                    takePermission();
                }
                break;

            case 10:
                signOut();
                break;
        }
    }

    private boolean isPermissionGranted(){
        return ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED;
    }

    private void takePermission(){
        ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CALL_PHONE}, callRequestCode);
    }


    private void openFacebookPage(String pageId) {
        String pageUrl = "https://wwww.facebook.com/easykoreancalamus/";

        try {
            ApplicationInfo applicationInfo = requireActivity().getPackageManager().getApplicationInfo("com.facebook.katana", 0);

            if (applicationInfo.enabled) {
                int versionCode = getActivity().getPackageManager().getPackageInfo("com.facebook.katana", 0).versionCode;
                String url;

                if (versionCode >= 3002850) {
                    url = "fb://facewebmodal/f?href=" + pageUrl;
                } else {
                    url = "fb://page/" + pageId;
                }

                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            } else {
                throw new Exception("Facebook is disabled");
            }
        } catch (Exception e) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(pageUrl)));
        }
    }

    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = Objects.requireNonNull(cm).getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }


    private void signOut() {

        SharedPreferences.Editor editor1 = sharedPreferences1.edit();
        editor1.clear();
        editor1.apply();
        Intent intent = new Intent(getActivity(), SplashScreenActivity.class);
        startActivity(intent);
        requireActivity().finish();
    }


    private void callCenter(String ph) {
        ph = ph.replace("#", "%23");
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + ph));
        startActivity(intent);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == callRequestCode) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                callCenter("09979638384");
            } else {
                takePermission();
            }
        }
    }



    public void goPlayStore(){
        startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id="+ requireActivity().getPackageName())));
    }

}
