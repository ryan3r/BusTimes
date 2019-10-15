package com.ryan3r.bustimes;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ryan3r.bustimes.nextbusclient.FavoriteInfo;
import com.ryan3r.bustimes.nextbusclient.NextBusInfo;
import com.ryan3r.bustimes.nextbusclient.NextBusPredictions;

import java.util.ArrayList;

public class FavoritesFragment extends Fragment implements NextBusPredictions.Handler {
    private ListView favorites;
    private NextBusPredictions nextBus;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_favorites, container, false);

        final TextView emptyMsg = rootView.findViewById(R.id.empty_msg);
        emptyMsg.setVisibility(View.GONE);

//        nextBus = new NextBusPredictions(getContext());
//        nextBus.startPredictions();
//        nextBus.setHandler(this);

        // show the favorites
        favorites = rootView.findViewById(R.id.list);

        NextBusInfo info = new NextBusInfo(getContext());
        info.getAllFavorites(new NextBusInfo.ResponseHandler<FavoriteInfo[]>() {
            @Override
            public void onResponse(final FavoriteInfo[] response) {
                // show the empty message
                if(response.length == 0) {
                    emptyMsg.setVisibility(View.VISIBLE);
                }

                ArrayList<String> titles = new ArrayList<>();
                for(FavoriteInfo favorite : response) {
                    titles.add(favorite.getTitle());
                }
                favorites.setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, titles));

                favorites.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        FavoriteInfo info = response[i];
                        Intent intent = new Intent(getContext(), StopActivity.class);
                        intent.putExtra("stop", info.getStopId());
                        getContext().startActivity(intent);
                    }
                });

//                favorites.setAdapter(new FavoriteAdapter(response, getContext()));

                // request predictions for the stops
//                ArrayList<String> stops = new ArrayList<>(response.length);
//
//                for(FavoriteInfo fav : response) {
//                    stops.add(fav.getRouteId() + "|" + fav.getStopId());
//                }
//
//                nextBus.setRoutes(stops);
            }
        });

        return rootView;
    }

    // only fetch predictions when we are visible
//    @Override
//    public void onHiddenChanged(boolean hidden) {
//        super.onHiddenChanged(hidden);
//
//        if(hidden) {
//            nextBus.stopPredictions();
//        }
//        else {
//            if(nextBus != null) nextBus.startPredictions();
//        }
//    }

    // we will already be showing a loader so stub this
    @Override
    public void onLoadStart() {}

    // something went wrong with the predictions
    @Override
    public void requestError(Throwable err) {
//        FavoriteAdapter adapter = (FavoriteAdapter) favorites.getAdapter();

//        adapter.setTimes(null);
//        favorites.setAdapter(adapter);
    }

    // we have the predictions
    @Override
    public void onPrediction(ArrayList<NextBusPredictions.Prediction> preds) {
//        FavoriteAdapter adapter = (FavoriteAdapter) favorites.getAdapter();
//
//        adapter.setTimes(preds);
//        if(favorites.getAdapter() == null) favorites.setAdapter(adapter);
    }
}
