/*
 * MIT License
 *
 * Copyright (c) 2020 NestedApi Project
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.neirth.nestedapi.Invoices.Templates;

import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.List;

public class Invoice {
    private String id;
    private Long userId;
    private Date creationDate;
    private String deliveryAddress;
    private String deliveryPostcode;
    private Country deliveryCountry;
    private Currency deliveryCurrency;
    private String deliveryAddressInformation;
    private List<Product> products;

    public static class Builder {
        private String id;
        private Long userId;
        private Date creationDate;
        private String deliveryAddress;
        private String deliveryPostcode;
        private Country deliveryCountry;
        private Currency deliveryCurrency;
        private String deliveryAddressInformation;
        private List<Product> products;

        public Builder(String id) {
            this.id = id;
            this.products = new ArrayList<>();
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

        public Builder setDeliveryCurrency(Currency deliveryCurrency) {
            this.deliveryCurrency = deliveryCurrency;

            return this;
        }

        public Builder setDeliveryAddressInformation(String deliveryAddressInformation) {
            this.deliveryAddressInformation = deliveryAddressInformation;

            return this;
        }

        public Builder setProducts(List<Product> products) {
            this.products = products;

            return this;
        }

        public Invoice build() {
            return new Invoice(id, userId, creationDate, deliveryAddress, deliveryPostcode, deliveryCountry, deliveryCurrency, deliveryAddressInformation, products);
        }
    }

    private Invoice(String id, Long userId, Date creationDate, String deliveryAddress, String deliveryPostcode,
                    Country deliveryCountry, Currency deliveryCurrency, String deliveryAddressInformation, List<Product> products) {
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

    public String getId() {
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

    public List<Product> getProducts() {
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