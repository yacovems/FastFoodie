package com.ykoa.yacov.fastfoodie;

import java.util.Comparator;

public class CustomComparator implements Comparator<RestaurantInfo>{
    private int sortBy;

    public CustomComparator(int sortBy) {
        this.sortBy = sortBy;
    }

    @Override
    public int compare(RestaurantInfo res1, RestaurantInfo res2) {

        switch (sortBy) {
            case 1:
                return res1.getDistance().compareTo(res2.getDistance());
            case 2:
                return res1.getCost().compareTo(res2.getCost());
            case 3:
                return res2.getRating().compareTo(res1.getRating());
            default:
                return 0;
        }
    }
}
