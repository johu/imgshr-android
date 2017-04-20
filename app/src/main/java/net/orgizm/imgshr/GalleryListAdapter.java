package net.orgizm.imgshr;

import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class GalleryListAdapter extends RecyclerView.Adapter<GalleryListAdapter.ViewHolder> {
    private List<Gallery> galleriesList;
    private int position;

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        public TextView slug;

        public ViewHolder(View view) {
            super(view);

            slug = (TextView) view.findViewById(R.id.slug);

            view.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
            menu.setHeaderTitle(R.string.gallery_context_menu_title);
            menu.add(Menu.NONE, R.id.delete_from_list, Menu.NONE, R.string.delete_from_list);
        }
    }

    public GalleryListAdapter(List<Gallery> galleriesList) {
        this.galleriesList = galleriesList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.gallery_list_item, parent, false);

        return new ViewHolder(itemView);
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Gallery gallery = galleriesList.get(position);
        holder.slug.setText(gallery.getSlug());

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                setPosition(holder.getAdapterPosition());
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return galleriesList.size();
    }
}
