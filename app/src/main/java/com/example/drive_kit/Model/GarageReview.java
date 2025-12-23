package com.example.drive_kit.Model;

public class GarageReview {
    private String garageId;
    private String driverId;
    private int rate;
    private String comment;
    private long createdAt;

    public GarageReview() {}

    public GarageReview(String garageId, String driverId, int rate, String comment, long createdAt) {
        this.garageId = garageId;
        this.driverId = driverId;
        this.rate = rate;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public String getGarageId() { return garageId; }
    public void setGarageId(String garageId) { this.garageId = garageId; }

    public String getDriverId() { return driverId; }
    public void setDriverId(String driverId) { this.driverId = driverId; }

    public int getRate() { return rate; }
    public void setRate(int rate) { this.rate = rate; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}




//creat review for example
//String garageId = "garage123";
//String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
//
//GarageReview review = new GarageReview();
//review.setGarageId(garageId);
//review.setDriverId(driverId);
//review.setRate(5);
//review.setComment("שירות מצוין");
//review.setCreatedAt(System.currentTimeMillis());
//
//db.collection("garages")
//  .document(garageId)
//  .collection("reviews")
//  .document(driverId) // מונע דירוג כפול
//  .set(review);