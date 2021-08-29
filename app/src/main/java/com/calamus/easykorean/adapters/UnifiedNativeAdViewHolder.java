package com.calamus.easykorean.adapters;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.calamus.easykorean.R;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;


public class UnifiedNativeAdViewHolder extends RecyclerView.ViewHolder {
    private final UnifiedNativeAdView adView;

    public UnifiedNativeAdView getAdView(){
        return adView;
    }

    public UnifiedNativeAdViewHolder(@NonNull View view) {
        super(view);
        adView=view.findViewById(R.id.ad_view);

        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_icon));
        adView.setPriceView(adView.findViewById(R.id.ad_price));
        adView.setStarRatingView(adView.findViewById(R.id.ad_stars));
        adView.setStoreView(adView.findViewById(R.id.ad_store));
        adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));

    }
}
