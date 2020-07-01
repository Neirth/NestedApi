package io.neirth.nestedapi.Invoices.Templates;

import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Invoice {
    private Long id;
    private Long userId;
    private Date creationDate;
    private String deliveryAddress;
    private String deliveryPostcode;
    private Country deliveryCountry;
    private Currency deliveryCurrency;
    private String deliveryAddressInformation;
    private Map<String, Map.Entry<Float, Integer>> products;

    public static class Builder {
        private Long id;
        private Long userId;
        private Date creationDate;
        private String deliveryAddress;
        private String deliveryPostcode;
        private Country deliveryCountry;
        private Currency deliveryCurrency;
        private String deliveryAddressInformation;
        private Map<String, Map.Entry<Float, Integer>> products;

        public Builder(Long id) {
            this.id = id;
            this.products = new HashMap<>();
        }

        public Builder setId(Long id) {
            this.id = id;

            return this;
        }

        public Builder setUserId(Long userId) {
            this.userId = userId;

            return this;
        }

        public Builder setCreationDate(Date creationDate) {
            this.creationDate = creationDate;

            return this;
        }

        public Builder setDeliveryAddress(String deliveryAddress) {
            this.deliveryAddress = deliveryAddress;

            return this;
        }

        public Builder setDeliveryPostcode(String deliveryPostcode) {
            this.deliveryPostcode = deliveryPostcode;

            return this;
        }

        public Builder setDeliveryCountry(Country deliveryCountry) {
            this.deliveryCountry = deliveryCountry;

            return this;
        }

        public void setDeliveryCurrency(Currency deliveryCurrency) {
            this.deliveryCurrency = deliveryCurrency;
        }

        public Builder setDeliveryAddressInformation(String deliveryAddressInformation) {
            this.deliveryAddressInformation = deliveryAddressInformation;

            return this;
        }

        public Builder addProduct(String productName, Map.Entry<Float, Integer> productProperties) {
            products.put(productName, productProperties);

            return this;
        }

        public Invoice build() {
            return new Invoice(id, userId, creationDate, deliveryAddress, deliveryPostcode, deliveryCountry, deliveryCurrency, deliveryAddressInformation, products);
        }
    }

    private Invoice(Long id, Long userId, Date creationDate, String deliveryAddress, String deliveryPostcode,
                    Country deliveryCountry, Currency deliveryCurrency, String deliveryAddressInformation, Map<String, Entry<Float, Integer>> products) {
        this.id = id;
        this.userId = userId;
        this.creationDate = creationDate;
        this.deliveryAddress = deliveryAddress;
        this.deliveryPostcode = deliveryPostcode;
        this.deliveryCountry = deliveryCountry;
        this.deliveryCurrency = deliveryCurrency;
        this.deliveryAddressInformation = deliveryAddressInformation;
        this.products = products;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public String getDeliveryAddressInformation() {
        return deliveryAddressInformation;
    }

    public Map<String, Map.Entry<Float, Integer>> getProducts() {
        return products;
    }

    public String getDeliveryPostcode() {
        return deliveryPostcode;
    }

    public Country getDeliveryCountry() {
        return deliveryCountry;
    }

    public Currency getDeliveryCurrency() {
        return deliveryCurrency;
    }
}