package com.hutchgroup.elog.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.location.GpsSatellite;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hutchgroup.elog.R;
import com.hutchgroup.elog.beans.AppSettings;
import com.hutchgroup.elog.beans.EventBean;
import com.hutchgroup.elog.common.LogFile;
import com.hutchgroup.elog.common.Utility;
import com.hutchgroup.elog.fragments.ELogFragment;
import com.hutchgroup.elog.util.GnssType;
import com.hutchgroup.elog.util.GpsTestUtil;

import org.w3c.dom.Text;

import java.util.List;


public class GpsSatelliteAdapter extends ArrayAdapter<GpsSatellite> {

    static class ViewHolder {
        public TextView tvPRN;
        public ImageView ivFlags;
       // public TextView tvGNSS;
        public TextView tvSNR;
        public TextView tvELEVATION;
        public TextView tvAZIMUTH;
        public TextView tvFLAGS;
    }

    private int mSvCount, mPrns[];

    private float mSnrs[], mSvElevations[], mSvAzimuths[];

    private int mEphemerisMask, mAlmanacMask, mUsedInFixMask;

    private Drawable flagUsa, flagRussia, flagJapan, flagChina;

    private Context mContext;

    private List<GpsSatellite> satellites;


    public GpsSatelliteAdapter(Context c, List<GpsSatellite> sats) {
        super(c, -1, sats);
        mContext = c;
        satellites = sats;

        flagUsa = mContext.getResources().getDrawable(R.drawable.ic_flag_usa);
        flagRussia = mContext.getResources().getDrawable(R.drawable.ic_flag_russia);
        flagJapan = mContext.getResources().getDrawable(R.drawable.ic_flag_japan);
        flagChina = mContext.getResources().getDrawable(R.drawable.ic_flag_china);

        updateValues();
    }

    public void changeItems(List<GpsSatellite> list) {
        satellites.clear();
        satellites.addAll(list);

        updateValues();
        notifyDataSetChanged();
    }

    private void updateValues() {
        //if (mPrns == null) {
            int length = satellites.size();
            mPrns = new int[length];
            mSnrs = new float[length];
            mSvElevations = new float[length];
            mSvAzimuths = new float[length];
        //}

        mSvCount = 0;
        mEphemerisMask = 0;
        mAlmanacMask = 0;
        mUsedInFixMask = 0;
        for (int i = 0; i < satellites.size(); i++) {
            GpsSatellite satellite = satellites.get(i);
            int prn = satellite.getPrn();
            int prnBit = (1 << (prn - 1));
            mPrns[mSvCount] = prn;
            mSnrs[mSvCount] = satellite.getSnr();
            mSvElevations[mSvCount] = satellite.getElevation();
            mSvAzimuths[mSvCount] = satellite.getAzimuth();
            if (satellite.hasEphemeris()) {
                mEphemerisMask |= prnBit;
            }
            if (satellite.hasAlmanac()) {
                mAlmanacMask |= prnBit;
            }
            if (satellite.usedInFix()) {
                mUsedInFixMask |= prnBit;
            }
            mSvCount++;
        }
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        try {
            // reuse views
            if (rowView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                ViewHolder viewHolder = new ViewHolder();

                rowView = inflater.inflate(R.layout.gps_item, null);
                // configure view holder
                viewHolder.tvPRN = (TextView) rowView.findViewById(R.id.tvPRN);
                viewHolder.ivFlags = (ImageView) rowView.findViewById(R.id.ivFlags);
                viewHolder.tvSNR = (TextView) rowView.findViewById(R.id.tvSNR);
                viewHolder.tvELEVATION = (TextView) rowView.findViewById(R.id.tvELEVATION);
                viewHolder.tvAZIMUTH = (TextView) rowView.findViewById(R.id.tvAZIMUTH);
                viewHolder.tvFLAGS = (TextView) rowView.findViewById(R.id.tvFLAGS);

                rowView.setTag(viewHolder);
            }

            final ViewHolder holder = (ViewHolder) rowView.getTag();

            int pos = position;
            holder.tvPRN.setText(Integer.toString(mPrns[pos]));
            holder.ivFlags.setScaleType(ImageView.ScaleType.FIT_START);

            GnssType type = GpsTestUtil.getGnssType(this.mPrns[pos]);
            switch (type) {
                case NAVSTAR:
                    holder.ivFlags.setImageDrawable(this.flagUsa);
                    break;
                case GLONASS:
                    holder.ivFlags.setImageDrawable(this.flagRussia);
                    break;
                case QZSS:
                    holder.ivFlags.setImageDrawable(this.flagJapan);
                    break;
                case BEIDOU:
                    holder.ivFlags.setImageDrawable(this.flagChina);
                    break;
            }

            holder.tvSNR.setText(Float.toString(this.mSnrs[pos]));
            holder.tvELEVATION.setText(Float.toString(this.mSvElevations[pos]));
            holder.tvAZIMUTH.setText(Float.toString(this.mSvAzimuths[pos]));
            char[] flags = new char[3];
            flags[0] = ((this.mEphemerisMask & (1 << (this.mPrns[pos] - 1))) == 0 ? ' ' : 'E');
            flags[1] = ((this.mAlmanacMask & (1 << (this.mPrns[pos] - 1))) == 0 ? ' ' : 'A');
            flags[2] = ((this.mUsedInFixMask & (1 << (this.mPrns[pos] - 1))) == 0 ? ' ' : 'U');
            holder.tvFLAGS.setText(new String(flags));
        } catch (Exception e) {
            Log.i("GPS", "Error: " + e.getMessage());
        }

        return rowView;
    }
//
//            //----
//            TextView textView = null;
//            ImageView imageView = null;
//
//            //int row = position;
//            int row = position / COLUMN_COUNT;
//            int column = position % COLUMN_COUNT;
//
//            if (convertView != null) {
//                if (convertView instanceof ImageView) {
//                    imageView = (ImageView) convertView;
//                } else if (convertView instanceof TextView) {
//                    textView = (TextView) convertView;
//                }
//            }
//
//            CharSequence text = null;
//
//            if (row == 0) {
//                switch (column) {
//                    case PRN_COLUMN:
//                        text = getResources().getString(R.string.gps_prn_column_label);
//                        break;
//                    case FLAG_IMAGE_COLUMN:
//                        text = getResources().getString(R.string.gps_flag_image_label);
//                        break;
//                    case SNR_COLUMN:
//                        text = getResources().getString(R.string.gps_snr_column_label);
//                        break;
//                    case ELEVATION_COLUMN:
//                        text = getResources().getString(R.string.gps_elevation_column_label);
//                        break;
//                    case AZIMUTH_COLUMN:
//                        text = getResources().getString(R.string.gps_azimuth_column_label);
//                        break;
//                    case FLAGS_COLUMN:
//                        text = getResources().getString(R.string.gps_flags_column_label);
//                        break;
//                }
//            } else {
//                row--;
//                switch (column) {
//                    case PRN_COLUMN:
//                        text = Integer.toString(mPrns[row]);
//                        break;
//                    case FLAG_IMAGE_COLUMN:
//                        if (imageView == null) {
//                            imageView = new ImageView(mContext);
//                            imageView.setScaleType(ImageView.ScaleType.FIT_START);
//                        }
//                        GnssType type = GpsTestUtil.getGnssType(this.mPrns[row]);
//                        switch (type) {
//                            case NAVSTAR:
//                                imageView.setImageDrawable(this.flagUsa);
//                                break;
//                            case GLONASS:
//                                imageView.setImageDrawable(this.flagRussia);
//                                break;
//                            case QZSS:
//                                imageView.setImageDrawable(this.flagJapan);
//                                break;
//                            case BEIDOU:
//                                imageView.setImageDrawable(this.flagChina);
//                                break;
//                        }
//                        return imageView;
//                    case SNR_COLUMN:
//                        text = Float.toString(this.mSnrs[row]);
//                        break;
//                    case ELEVATION_COLUMN:
//                        text = Float.toString(this.mSvElevations[row]);
//                        break;
//                    case AZIMUTH_COLUMN:
//                        text = Float.toString(this.mSvAzimuths[row]);
//                        break;
//                    case FLAGS_COLUMN:
//                        char[] flags = new char[3];
//                        flags[0] = ((this.mEphemerisMask & (1 << (this.mPrns[row] - 1))) == 0 ? ' ' : 'E');
//                        flags[1] = ((this.mAlmanacMask & (1 << (this.mPrns[row] - 1))) == 0 ? ' ' : 'A');
//                        flags[2] = ((this.mUsedInFixMask & (1 << (this.mPrns[row] - 1))) == 0 ? ' ' : 'U');
//                        text = new String(flags);
//                        break;
//                }
//            }
//
//            if (textView == null) {
//                textView = new TextView(mContext);
//            }
//
//            textView.setText(text);
//            return textView;
//        }
}
