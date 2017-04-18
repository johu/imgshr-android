package net.orgizm.imgshr;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class GalleryListAdapter extends RecyclerView.Adapter<GalleryListAdapter.MyViewHolder> {
    private List<Gallery> galleriesList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView slug;

        public MyViewHolder(View view) {
            super(view);

            slug = (TextView) view.findViewById(R.id.slug);
        }
    }

    public GalleryListAdapter(List<Gallery> galleriesList) {
        this.galleriesList = galleriesList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.gallery_list_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Gallery gallery = galleriesList.get(position);
        holder.slug.setText(gallery.getSlug());
    }

    @Override
    public int getItemCount() {
        return galleriesList.size();
    }
}
