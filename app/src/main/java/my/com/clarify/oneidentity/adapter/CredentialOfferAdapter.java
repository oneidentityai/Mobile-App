package my.com.clarify.oneidentity.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import my.com.clarify.oneidentity.R;
import my.com.clarify.oneidentity.activity.ConnectionActivity;

public class CredentialOfferAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    public int VIEW_TYPE_DATA = 0;
    public ConnectionActivity activity;
    public int scrollCount = 0;
    public int index = 0;
    public CredentialOfferAdapter(ConnectionActivity activity, int index)
    {
        this.activity = activity;
        this.index = index;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        LayoutInflater mInflater = LayoutInflater.from(viewGroup.getContext());
        ViewGroup mainGroup = (ViewGroup) mInflater.inflate(R.layout.list_item_credential_offer, viewGroup, false);
        ItemViewHolder listHolder = new ItemViewHolder(mainGroup);
        return listHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position)
    {
        final ItemViewHolder viewHolder = (ItemViewHolder)holder;

        try {
            JSONObject data = new JSONObject(activity.credentialOfferList.get(index).get(position));
            String senderDid = data.getString("sender_did");
            String schemaId = data.getString("schema_id");
            String credDefId = data.getString("cred_def_id");
            String schemaIdArray[] = schemaId.split(":");
            viewHolder.textTitle.setText(schemaIdArray[2] + " Version: " + schemaIdArray[3]);
            String subtitle = "Attributes:\n";
            for(int i = 0; i < data.getJSONArray("attributes").length(); i++) {
                if(i > 0)
                    subtitle += ", ";
                subtitle += data.getJSONArray("attributes").getString(i);
            }
            viewHolder.textSubtitle.setText(subtitle);
            viewHolder.layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activity.alertSubItemMenu(1, index, position);
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount()
    {
        return activity.credentialOfferList.get(index).size();
    }

    @Override
    public int getItemViewType(int position) {
        return VIEW_TYPE_DATA;
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder
    {
        public LinearLayout layout;
        public TextView textTitle;
        public TextView textSubtitle;

        public ItemViewHolder(View view) {
            super(view);
            layout = view.findViewById(R.id.layout_region);
            textTitle = view.findViewById(R.id.text_title);
            textSubtitle = view.findViewById(R.id.text_subtitle);
        }
    }
}