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

    public StopConfig() {

    }

    public List<RouteChoice> getFullRoutes() {
        ArrayList<RouteChoice> choice = new ArrayList<>();
        choice.add(new RouteChoice(false, new StopInfo.RouteInfo("1", "Red", "#ffffff", "idk")));
        choice.add(new RouteChoice(true, new StopInfo.RouteInfo("1", "Green", "#ffffff", "idk")));
        choice.add(new RouteChoice(true, new StopInfo.RouteInfo("1", "Blue", "#ffffff", "idk")));
        return choice;
    }
}
