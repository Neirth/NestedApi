package io.neirth.nestedapi.Invoices.Templates;

public class Product {
    private String productName;
    private Float productPrice;
    private Integer productAmount;

    public static class Builder {
        private String productName;
        private Float productPrice;
        private Integer productAmount;

        public Builder setProductName(String productName) {
            this.productName = productName;

            return this;
        }
    
        public Builder setProductPrice(Float productPrice) {
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

    private Product(String productName, Float productPrice, Integer productAmount) {
        this.productName = productName;
        this.productPrice = productPrice;
        this.productAmount = productAmount;
    }

    public String getProductName() {
        return productName;
    }

    public Float getProductPrice() {
        return productPrice;
    }

    public Integer getProductAmount() {
        return productAmount;
    }    
}