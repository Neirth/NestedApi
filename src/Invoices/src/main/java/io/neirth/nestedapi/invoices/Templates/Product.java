package io.neirth.nestedapi.invoices.templates;

public class Product {
    private String productName;
    private Double productPrice;
    private Integer productAmount;

    public static class Builder {
        private String productName;
        private Double productPrice;
        private Integer productAmount;

        public Builder setProductName(String productName) {
            this.productName = productName;

            return this;
        }
    
        public Builder setProductPrice(Double productPrice) {
            this.productPrice = productPrice;

            return this;
        }

        public Builder setProductAmount(Integer productAmount) {
            this.productAmount = productAmount;

            return this;
        }

        public Product bulid() {
            return new Product(productName, productPrice, productAmount);
        }
    }

    private Product(String productName, Double productPrice, Integer productAmount) {
        this.productName = productName;
        this.productPrice = productPrice;
        this.productAmount = productAmount;
    }

    public String getProductName() {
        return productName;
    }

    public Double getProductPrice() {
        return productPrice;
    }

    public Integer getProductAmount() {
        return productAmount;
    }    
}