package de.flowment.wanderlust;

import java.sql.Date;

/**
 * Created by Khaled Reguieg on 11.11.2015.
 * This class is a dataholding class for our users. It should although be saved in the database.
 */
public class User {

    private String id;
    private String name;
    private String prename;
    private Date dateOfBirth;
    private int weight;
    private int height;
}
