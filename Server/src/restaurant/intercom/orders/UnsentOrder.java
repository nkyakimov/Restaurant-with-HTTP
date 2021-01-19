package restaurant.intercom.orders;


import java.io.Serializable;

public class UnsentOrder implements Serializable {
    private String username;
    private Integer tableID;
    private String productID;
    private String comment;

    public UnsentOrder(String username, Integer tableID, String productID, String comment) {
        this.username = username;
        this.tableID = tableID;
        this.productID = productID;
        this.comment = comment;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getTableID() {
        return tableID;
    }

    public void setTableID(Integer tableID) {
        this.tableID = tableID;
    }

    public String getProductID() {
        return productID;
    }

    public void setProductID(String productID) {
        this.productID = productID;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
