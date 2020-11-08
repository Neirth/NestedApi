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
package io.neirth.nestedapi.invoices.templates;

/**
 * Country enumeration
 * 
 * This enumeration contains all country codes, as of the year 2020, present in the ISO-3166 Standard.
 * Also, include a country names for statistical purposes.
 */
public enum Country {
    AF("Afghanistan"), AX("Aland Islands"), AL("Albania"), DZ("Algeria"), AS("American Samoa"), AD("Andorra"),
    AO("Angola"), AI("Anguilla"), AQ("Antarctica"), AG("Antigua And Barbuda"), AR("Argentina"), AM("Armenia"),
    AW("Aruba"), AU("Australia"), AT("Austria"), AZ("Azerbaijan"), BS("Bahamas"), BH("Bahrain"), BD("Bangladesh"),
    BB("Barbados"), BY("Belarus"), BE("Belgium"), BZ("Belize"), BJ("Benin"), BM("Bermuda"), BT("Bhutan"), BO("Bolivia"),
    BA("Bosnia And Herzegovina"), BW("Botswana"), BV("Bouvet Island"), BR("Brazil"),
    IO("British Indian Ocean Territory"), BN("Brunei Darussalam"), BG("Bulgaria"), BF("Burkina Faso"), BI("Burundi"),
    KH("Cambodia"), CM("Cameroon"), CA("Canada"), CV("Cape Verde"), KY("Cayman Islands"),
    CF("Central African Republic"), TD("Chad"), CL("Chile"), CN("China"), CX("Christmas Island"),
    CC("Cocos Keeling Islands"), CO("Colombia"), KM("Comoros"), CG("Congo"), CD("Congo Democratic Republic"),
    CK("Cook Islands"), CR("Costa Rica"), CI("Cote D'Ivoire"), HR("Croatia"), CU("Cuba"), CY("Cyprus"),
    CZ("Czech Republic"), DK("Denmark"), DJ("Djibouti"), DM("Dominica"), DO("Dominican Republic"), EC("Ecuador"),
    EG("Egypt"), SV("El Salvador"), GQ("Equatorial Guinea"), ER("Eritrea"), EE("Estonia"), ET("Ethiopia"),
    FK("Falkland Islands"), FO("Faroe Islands"), FJ("Fiji"), FI("Finland"), FR("France"), GF("French Guiana"),
    PF("French Polynesia"), TF("French Southern Territories"), GA("Gabon"), GM("Gambia"), GE("Georgia"), DE("Germany"),
    GH("Ghana"), GI("Gibraltar"), GR("Greece"), GL("Greenland"), GD("Grenada"), GP("Guadeloupe"), GU("Guam"),
    GT("Guatemala"), GG("Guernsey"), GN("Guinea"), GW("Guinea Bissau"), GY("Guyana"), HT("Haiti"),
    HM("Heard Island Mcdonald Islands"), VA("Holy See Vatican City State"), HN("Honduras"), HK("Hong Kong"),
    HU("Hungary"), IS("Iceland"), IN("India"), ID("Indonesia"), IR("Iran"), IQ("Iraq"), IE("Ireland"),
    IM("Isle Of Man"), IL("Israel"), IT("Italy"), JM("Jamaica"), JP("Japan"), JE("Jersey"), JO("Jordan"),
    KZ("Kazakhstan"), KE("Kenya"), KI("Kiribati"), KR("Korea"), KW("Kuwait"), KG("Kyrgyzstan"),
    LA("Lao Peoples Democratic Republic"), LV("Latvia"), LB("Lebanon"), LS("Lesotho"), LR("Liberia"),
    LY("Libyan Arab Jamahiriya"), LI("Liechtenstein"), LT("Lithuania"), LU("Luxembourg"), MO("Macao"), MK("Macedonia"),
    MG("Madagascar"), MW("Malawi"), MY("Malaysia"), MV("Maldives"), ML("Mali"), MT("Malta"), MH("Marshall Islands"),
    MQ("Martinique"), MR("Mauritania"), MU("Mauritius"), YT("Mayotte"), MX("Mexico"), FM("Micronesia"), MD("Moldova"),
    MC("Monaco"), MN("Mongolia"), ME("Montenegro"), MS("Montserrat"), MA("Morocco"), MZ("Mozambique"), MM("Myanmar"),
    NA("Namibia"), NR("Nauru"), NP("Nepal"), NL("Netherlands"), AN("Netherlands Antilles"), NC("New Caledonia"),
    NZ("New Zealand"), NI("Nicaragua"), NE("Niger"), NG("Nigeria"), NU("Niue"), NF("Norfolk Island"),
    MP("Northern Mariana Islands"), NO("Norway"), OM("Oman"), PK("Pakistan"), PW("Palau"), PS("Palestinian Territory"),
    PA("Panama"), PG("Papua New Guinea"), PY("Paraguay"), PE("Peru"), PH("Philippines"), PN("Pitcairn"), PL("Poland"),
    PT("Portugal"), PR("Puerto Rico"), QA("Qatar"), RE("Reunion"), RO("Romania"), RU("Russian Federation"),
    RW("Rwanda"), BL("Saint Barthelemy"), SH("Saint Helena"), KN("Saint Kitts And Nevis"), LC("Saint Lucia"),
    MF("Saint Martin"), PM("Saint Pierre And Miquelon"), VC("Saint Vincent And Grenadines"), WS("Samoa"),
    SM("San Marino"), ST("Sao Tome And Principe"), SA("Saudi Arabia"), SN("Senegal"), RS("Serbia"), SC("Seychelles"),
    SL("Sierra Leone"), SG("Singapore"), SK("Slovakia"), SI("Slovenia"), SB("Solomon Islands"), SO("Somalia"),
    ZA("South Africa"), GS("South Georgia And Sandwich Isl"), ES("Spain"), LK("SriLanka"), SD("Sudan"), SR("Suriname"),
    SJ("Svalbard And Jan Mayen"), SZ("Swaziland"), SE("Sweden"), CH("Switzerland"), SY("Syrian Arab Republic"),
    TW("Taiwan"), TJ("Tajikistan"), TZ("Tanzania"), TH("Thailand"), TL("Timor Leste"), TG("Togo"), TK("Tokelau"),
    TO("Tonga"), TT("Trinidad And Tobago"), TN("Tunisia"), TR("Turkey"), TM("Turkmenistan"),
    TC("Turks And Caicos Islands"), TV("Tuvalu"), UG("Uganda"), UA("Ukraine"), AE("United Arab Emirates"),
    GB("United Kingdom"), US("United States"), UM("United States Outlying Islands"), UY("Uruguay"), UZ("Uzbekistan"),
    VU("Vanuatu"), VE("Venezuela"), VN("Vietnam"), VG("Virgin Islands British"), VI("Virgin Islands US"),
    WF("Wallis And Futuna"), EH("Western Sahara"), YE("Yemen"), ZM("Zambia"), ZW("Zimbabw");

    private final String name;

    private Country(String name) {
        this.name = name;
    }

    /**
     * Method for get the name, in English, of the country. Useful in stadistics
     * platforms, such as Power Bi or Metabase.
     * 
     * @return The country name.
     */
    public String getCountryName() {
        return name;
    }
}