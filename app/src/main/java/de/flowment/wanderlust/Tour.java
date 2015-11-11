package de.flowment.wanderlust;

import java.sql.Time;

/**
 * Created by Khaled Reguieg on 11.11.2015.
 * Dataholding class for a walking tour. All data of a tour should be saved in a tour object,
 * which should be saved in database.
 */
public class Tour {
    private String id;
    private float[] position;
    private float distance;
    private Time duration;
    private int burnedCalories;
}
