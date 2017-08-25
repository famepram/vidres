package ekalaya.id.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Femmy on 8/24/2017.
 */

public class RVAdapter extends RecyclerView.Adapter<RVAdapter.RVAdapterHolder>{

    private List<File> files;

    private Context ctx;

    int width_img = 108;

    @Override
    public RVAdapterHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_item,parent,false);
        return new RVAdapterHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RVAdapterHolder holder, int position) {
        File file = files.get(position);
        Bitmap bmp = BitmapFactory.decodeFile(file.getAbsolutePath());
        holder.img.setImageBitmap(bmp);
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    public class RVAdapterHolder extends RecyclerView.ViewHolder {
        ImageView img;
        public RVAdapterHolder(View itemView) {
            super(itemView);
//            DisplayMetrics displayMetrics = ctx.getResources().getDisplayMetrics();
//            float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
//            float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
//            float perImg  = dpWidth / 10* displayMetrics.density;
//            Log.d("TESTING", "dpWidth : "+dpWidth);
            img = (ImageView) itemView.findViewById(R.id.iv_rv_item);
            img.getLayoutParams().width =  width_img;
            img.getLayoutParams().height = 210;
            Log.d("TESTING", "dpWidth : "+width_img);
        }
    }


    public void setWidthImg(int w){
        width_img = w;
    }

    public RVAdapter(List<File> listfiles, Context ctx){
        files = listfiles == null ? new ArrayList<File>() : listfiles;
        this.ctx = ctx;
    }

    public void setFiles (List<File> files){
        this.files = files;
        this.notifyDataSetChanged();
    }
}
