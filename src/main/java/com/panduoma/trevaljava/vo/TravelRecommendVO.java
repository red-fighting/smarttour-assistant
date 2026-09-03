package com.panduoma.trevaljava.vo;

import lombok.Data;

import java.util.List;

@Data
public class TravelRecommendVO {
    private Boolean success;
    private String city;
    private Integer days;
    private Double totalBudget;
    private List<DailyItinerary> dailyItinerary;
    private BudgetBreakdown budgetBreakdown;
    private List<String> tips;
    private List<String> warnings;
    private String error;
    private String rawResponse;
    private String response;

    @Data
    public static class DailyItinerary {
        private Integer day;
        private String date;
        private List<Timeslot> morning;
        private List<Timeslot> afternoon;
        private List<Timeslot> evening;
    }

    @Data
    public static class Timeslot {
        private String time;
        private String activity;
        private String spot;
        private String duration;
        private String transportation;
        private String description;
        private String ticket;
    }

    @Data
    public static class BudgetBreakdown {
        private Double accommmodation;
        private Double food;
        private Double transportation;
        private Double tickets;
        private Double other;
    }
}
