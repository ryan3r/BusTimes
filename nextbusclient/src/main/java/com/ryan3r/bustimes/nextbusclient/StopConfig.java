package com.ryan3r.bustimes.nextbusclient;

import java.util.ArrayList;
import java.util.List;

public class StopConfig {
    public static class RouteChoice {
        private boolean selected;
        private StopInfo.RouteInfo route;

        RouteChoice(boolean sel, StopInfo.RouteInfo r) {
            selected = sel;
            route = r;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public StopInfo.RouteInfo getRoute() {
            return route;
        }
    }

    private NextBusInfo mInfo;
    private String mStopId;

    public StopConfig(NextBusInfo info, String stopId) {
        mInfo = info;
        mStopId = stopId;
    }

    public void getFullRoutes(final NextBusInfo.ResponseHandler<ArrayList<RouteChoice>> handler) {
        mInfo.getStopInfo(mStopId, new NextBusInfo.ResponseHandler<StopInfo>() {
            @Override
            public void onResponse(StopInfo response) {
                ArrayList<RouteChoice> routeChoices = new ArrayList<>();

                for(StopInfo.RouteInfo route : response.getRoutes()) {
                    routeChoices.add(new RouteChoice(true, route));
                }

                handler.onResponse(routeChoices);
            }
        });
    }
}
