package com.example.spotifywrapped;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class CreateAdapter extends RecyclerView.Adapter<CreateAdapter.ViewHolder> {

    private List<SpotifyDataModel> spotifyDataList;
    public CreateAdapter(List<SpotifyDataModel> data) {
        spotifyDataList = data;
    }

    @NonNull
    @Override
    public CreateAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.past_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CreateAdapter.ViewHolder holder, int position) {
        SpotifyDataModel dataModel = spotifyDataList.get(position);
        switch (dataModel.type) {
            case "New Years":
                holder.cardTitle_textView.setText("To a New Year!");
                break;
            case "Halloween":
                holder.cardTitle_textView.setText("Boo your Taste!");
                break;
            case "Christmas":
                holder.cardTitle_textView.setText("Holly Jolly!");
                break;
            default:
                break;
        }
        String formatedDate = String
                .format("%s/%s/%s",
                        dataModel.documentId.substring(4,6),
                        dataModel.documentId.substring(6, 8),
                        dataModel.documentId.substring(0,4));
        holder.date_textView.setText(formatedDate); // Assuming documentId is the formatted date
        holder.type_textView.setText(dataModel.type);
        Random rnd = new Random();
        holder.card.setCardBackgroundColor(Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256)));
        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                Intent intent = new Intent(v.getContext(), SummaryPlayerActivity.class);
                intent.putExtra("documentId", dataModel.documentId);
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return spotifyDataList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView date_textView, type_textView, cardTitle_textView;
        CardView card;

        ViewHolder(View itemView) {
            super(itemView);
            date_textView = itemView.findViewById(R.id.date_textView);
            type_textView = itemView.findViewById(R.id.type_textView);
            cardTitle_textView = itemView.findViewById(R.id.cardTitle_textView);
            card = itemView.findViewById(R.id.pastWrapped_cardItem);
        }
    }

    public void updateData(List<SpotifyDataModel> newData) {
        spotifyDataList = newData;
        notifyDataSetChanged();
    }
}
